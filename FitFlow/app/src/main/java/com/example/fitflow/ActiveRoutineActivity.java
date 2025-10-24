package com.example.fitflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActiveRoutineActivity extends AppCompatActivity {

    public static final String EXTRA_ROUTINE_TITLE = "com.example.fitflow.EXTRA_ROUTINE_TITLE";
    public static final String EXTRA_ROUTINE_DURATION_STRING = "com.example.fitflow.EXTRA_ROUTINE_DURATION_STRING";
    public static final String EXTRA_ROUTINE_EXERCISES = "com.example.fitflow.EXTRA_ROUTINE_EXERCISES";

    private static final String TAG = "ActiveRoutineActivity";
    private static final String STATE_TIME_IN_MILLISECONDS = "timeInMilliseconds";
    private static final String STATE_START_TIME = "startTime";
    private static final String STATE_TIME_SWAP_BUFF = "timeSwapBuff";
    private static final String STATE_IS_TIMER_RUNNING = "isTimerRunning";
    private static final String STATE_WAS_TIMER_STARTED = "wasTimerStarted";

    private TextView activeRoutineTitleTextView;
    private RecyclerView activeExercisesRecyclerView;
    private Button finishRoutineButton;
    private YouTubePlayerView youTubePlayerView; // <-- 1. Variable para el reproductor

    // Vistas del temporizador
    private TextView timerTextView;
    private Button startButton, pauseButton, resetButton;

    private String routineTitle;
    private String routineDurationString; // Duración estimada original
    private ArrayList<Exercise> routineExercises;
    private ActiveExerciseAdapter exerciseAdapter;
    private WorkoutLogDao workoutLogDao;

    // Variables para el temporizador
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    private boolean isTimerRunning = false;
    private boolean wasTimerStarted = false; // Para saber si el temporizador se usó

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_routine);

        // Vistas existentes
        activeRoutineTitleTextView = findViewById(R.id.activeRoutineTitleTextView);
        activeExercisesRecyclerView = findViewById(R.id.activeExercisesRecyclerView);
        finishRoutineButton = findViewById(R.id.finishRoutineButton);

        // --- Integración del reproductor de YouTube ---
        youTubePlayerView = findViewById(R.id.youtube_player_view); // <-- 2. Encontrar la vista
        getLifecycle().addObserver(youTubePlayerView); // <-- 3. Añadir al ciclo de vida (¡MUY IMPORTANTE!)
        // ------------------------------------------

        // Nuevas vistas del temporizador
        timerTextView = findViewById(R.id.timerTextView);
        startButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);
        resetButton = findViewById(R.id.resetButton);

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        workoutLogDao = db.workoutLogDao();

        routineTitle = getIntent().getStringExtra(EXTRA_ROUTINE_TITLE);
        routineDurationString = getIntent().getStringExtra(EXTRA_ROUTINE_DURATION_STRING);
        routineExercises = getIntent().getParcelableArrayListExtra(EXTRA_ROUTINE_EXERCISES);

        if (routineTitle != null) {
            activeRoutineTitleTextView.setText(String.format(getString(R.string.active_routine_executing_format), routineTitle));
        } else {
            activeRoutineTitleTextView.setText(getString(R.string.active_routine_executing_unknown));
            Log.e(TAG, "Routine title not passed in intent");
        }

        if (routineExercises != null && !routineExercises.isEmpty()) {
            exerciseAdapter = new ActiveExerciseAdapter(this, routineExercises);
            activeExercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            activeExercisesRecyclerView.setAdapter(exerciseAdapter);
        } else {
            Log.d(TAG, "No exercises to display or routineExercises is null");
        }

        setupTimerControls();
        timerTextView.setText(getString(R.string.timer_default_time));

        if (savedInstanceState != null) {
            timeInMilliseconds = savedInstanceState.getLong(STATE_TIME_IN_MILLISECONDS);
            startTime = savedInstanceState.getLong(STATE_START_TIME);
            timeSwapBuff = savedInstanceState.getLong(STATE_TIME_SWAP_BUFF);
            isTimerRunning = savedInstanceState.getBoolean(STATE_IS_TIMER_RUNNING);
            wasTimerStarted = savedInstanceState.getBoolean(STATE_WAS_TIMER_STARTED);

            updateTimerDisplay(timeInMilliseconds);
            if (isTimerRunning) {
                startTime = SystemClock.uptimeMillis();
                startTimerRunnable();
                pauseButton.setText(getString(R.string.timer_button_pause));
            } else {
                updateTimerDisplay(timeInMilliseconds);
                if (wasTimerStarted && timeInMilliseconds > 0) {
                    pauseButton.setText(getString(R.string.timer_button_resume));
                } else {
                    pauseButton.setText(getString(R.string.timer_button_pause));
                }
            }
        } else {
             updateButtonStates();
        }

        finishRoutineButton.setOnClickListener(v -> {
            if(isTimerRunning) pauseTimer();
            saveWorkoutLog();
        });
    }

    private void setupTimerControls() {
        startButton.setOnClickListener(v -> startTimer());
        pauseButton.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                resumeTimer(); 
            }
        });
        resetButton.setOnClickListener(v -> resetTimer());
        updateButtonStates();
    }

    private void startTimer() {
        if (!isTimerRunning) {
            startTime = SystemClock.uptimeMillis();
            startTimerRunnable();
            isTimerRunning = true;
            wasTimerStarted = true;
            updateButtonStates();
        }
    }

    private void resumeTimer() {
        if (!isTimerRunning && wasTimerStarted) { 
            startTime = SystemClock.uptimeMillis();
            startTimerRunnable();
            isTimerRunning = true;
            updateButtonStates();
        }
    }

    private void pauseTimer() {
        if (isTimerRunning) {
            timerHandler.removeCallbacks(timerRunnable);
            timeSwapBuff = updatedTime; // Guardar el tiempo total hasta la pausa
            isTimerRunning = false;
            updateButtonStates();
        }
    }

    private void resetTimer() {
        timerHandler.removeCallbacks(timerRunnable);
        isTimerRunning = false;
        wasTimerStarted = false;
        timeInMilliseconds = 0L;
        startTime = 0L;
        timeSwapBuff = 0L;
        updatedTime = 0L;
        updateTimerDisplay(0L);
        updateButtonStates();
    }

    private void startTimerRunnable(){
        timerRunnable = new Runnable() {
            public void run() {
                timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
                updatedTime = timeSwapBuff + timeInMilliseconds;
                updateTimerDisplay(updatedTime);
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void updateTimerDisplay(long timeToDisplay) {
        int secs = (int) (timeToDisplay / 1000);
        int mins = secs / 60;
        int hours = mins / 60;
        secs = secs % 60;
        mins = mins % 60;
        timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, mins, secs));
    }

    private void updateButtonStates() {
        if (isTimerRunning) {
            startButton.setEnabled(false);
            pauseButton.setEnabled(true);
            pauseButton.setText(getString(R.string.timer_button_pause));
            resetButton.setEnabled(true);
        } else {
            startButton.setEnabled(!wasTimerStarted || timeInMilliseconds == 0);
            if (wasTimerStarted && timeInMilliseconds > 0) {
                pauseButton.setText(getString(R.string.timer_button_resume));
                pauseButton.setEnabled(true);
                startButton.setEnabled(false);
            } else {
                pauseButton.setText(getString(R.string.timer_button_pause));
                pauseButton.setEnabled(false);
            }
            resetButton.setEnabled(wasTimerStarted && timeInMilliseconds > 0);
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_TIME_IN_MILLISECONDS, updatedTime);
        outState.putLong(STATE_START_TIME, startTime);
        outState.putLong(STATE_TIME_SWAP_BUFF, timeSwapBuff);
        outState.putBoolean(STATE_IS_TIMER_RUNNING, isTimerRunning);
        outState.putBoolean(STATE_WAS_TIMER_STARTED, wasTimerStarted);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // El YouTubePlayerView se libera automáticamente gracias al LifecycleObserver.
        // No es necesario llamar a youTubePlayerView.release() manualmente.
        timerHandler.removeCallbacks(timerRunnable); 
    }

    private void saveWorkoutLog() {
        if (routineTitle == null) { 
            Toast.makeText(this, getString(R.string.active_routine_error_data_missing), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot save log, routine title is null.");
            finish();
            return;
        }

        int durationMinutes = 0;
        if (wasTimerStarted && updatedTime > 0) {
            durationMinutes = (int) (updatedTime / (1000 * 60));
        } else {
            try {
                if (routineDurationString != null) {
                    String durationNumericPart = routineDurationString.replaceAll("[^0-9]", "");
                    if (!durationNumericPart.isEmpty()) {
                        durationMinutes = Integer.parseInt(durationNumericPart);
                    }
                } else {
                    durationMinutes = 30; 
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Could not parse duration from string: " + routineDurationString, e);
                durationMinutes = 30;
            }
        }
        if (durationMinutes == 0 && wasTimerStarted && updatedTime > 0) {
             durationMinutes = 1;
        }

        int exercisesCompleted = 0;
        if (exerciseAdapter != null) {
            List<Exercise> currentExercisesState = exerciseAdapter.getExerciseList();
            if (currentExercisesState != null) {
                for (Exercise ex : currentExercisesState) {
                    if (ex.isCompleted()) {
                        exercisesCompleted++;
                    }
                }
            }
        } else {
             if (routineExercises != null && !routineExercises.isEmpty()) {
                exercisesCompleted = 0; 
            } else if (durationMinutes > 0) { 
                exercisesCompleted = Math.max(1, durationMinutes / 5);
            }
        }
        
        int caloriesBurned = durationMinutes * 7; 

        SharedPreferences prefs = getSharedPreferences(RegisterActivity.SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt(RegisterActivity.KEY_USER_ID, -1);

        if (currentUserId == -1) {
            Toast.makeText(this, getString(R.string.active_routine_user_not_logged_in), Toast.LENGTH_LONG).show();
            Log.w(TAG, "User not logged in, workout log not saved.");
            finish();
            return;
        }

        final int finalDurationMinutes = durationMinutes;
        final int finalCaloriesBurned = caloriesBurned;
        final int finalExercisesCompleted = exercisesCompleted;

        WorkoutLog newLog = new WorkoutLog(
                System.currentTimeMillis(),
                finalDurationMinutes,
                finalCaloriesBurned,
                finalExercisesCompleted,
                currentUserId
        );

        AppDatabase.databaseWriteExecutor.execute(() -> {
            workoutLogDao.insert(newLog);
            Log.d(TAG, "WorkoutLog saved: " + routineTitle + ", Duration: " + finalDurationMinutes + " mins, Exercises: " + finalExercisesCompleted);

            // Enviar respaldo al webhook con los datos reales (usuario, duración, ejercicios, calorías, y título de la rutina)
            WebhookSender.sendWithUserId(ActiveRoutineActivity.this.getApplicationContext(), newLog.getUserId(), finalExercisesCompleted, finalDurationMinutes, finalCaloriesBurned, routineTitle);

            runOnUiThread(() -> {
                Toast.makeText(ActiveRoutineActivity.this, 
                               String.format(getString(R.string.active_routine_completed_log_saved_format), routineTitle), 
                               Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }
}

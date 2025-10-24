package com.example.fitflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;

public class ProgressFragment extends Fragment {

    private static final String TAG = "ProgressFragment";

    // Existing Views for Stats Cards
    private TextView caloriesValueProgressTextView;
    private TextView hoursValueProgressTextView;

    // Existing Views for Monthly Goals Card
    private TextView trainingsGoalLabelTextView;
    private ProgressBar trainingsProgressBar;
    private TextView weightGoalLabelTextView;
    private ProgressBar weightGoalProgressBar;

    // Existing Views for Achievements
    private TextView fireStreakDescTextView;

    // NEW Views for Weekly Summary
    private TextView weeklyMinutesTextView;
    private TextView weeklyCaloriesTextView;
    private TextView weeklyExercisesTextView;
    private TextView weeklyDaysTextView;

    // NEW Views for Monthly Summary
    private TextView monthlyMinutesTextView;
    private TextView monthlyCaloriesTextView;
    private TextView monthlyExercisesTextView;
    private TextView monthlyDaysTextView;

    private WorkoutLogDao workoutLogDao;
    private int currentUserId = -1;

    public ProgressFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        if (getContext() != null) {
            AppDatabase db = AppDatabase.getDatabase(getContext().getApplicationContext());
            workoutLogDao = db.workoutLogDao();

            SharedPreferences prefs = getContext().getSharedPreferences(RegisterActivity.SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);
            currentUserId = prefs.getInt(RegisterActivity.KEY_USER_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setStaticPlaceholders(); // Set initial placeholders for existing and new views

        if (currentUserId != -1 && workoutLogDao != null) {
            loadProgressData();
        } else {
            Log.w(TAG, "User ID not found or DAO not initialized. Cannot load progress data.");
            // Optionally, display a message to the user
        }

        // NOTA: El envío al webhook ahora se realiza al guardar un WorkoutLog (ActiveRoutineActivity)
        // Eliminamos el envío aquí para evitar envíos duplicados cada vez que se abre la pantalla de progreso.
        // updateProgressAndSend();
    }

    private void initializeViews(@NonNull View view) {
        // Existing views
        caloriesValueProgressTextView = view.findViewById(R.id.caloriesValueProgressTextView);
        hoursValueProgressTextView = view.findViewById(R.id.hoursValueProgressTextView);
        trainingsGoalLabelTextView = view.findViewById(R.id.trainingsGoalLabelTextView);
        trainingsProgressBar = view.findViewById(R.id.trainingsProgressBar);
        weightGoalLabelTextView = view.findViewById(R.id.weightGoalLabelTextView);
        weightGoalProgressBar = view.findViewById(R.id.weightGoalProgressBar);
        fireStreakDescTextView = view.findViewById(R.id.fireStreakDescTextView);

        // New weekly views
        weeklyMinutesTextView = view.findViewById(R.id.weeklyMinutesTextView);
        weeklyCaloriesTextView = view.findViewById(R.id.weeklyCaloriesTextView);
        weeklyExercisesTextView = view.findViewById(R.id.weeklyExercisesTextView);
        weeklyDaysTextView = view.findViewById(R.id.weeklyDaysTextView);

        // New monthly views
        monthlyMinutesTextView = view.findViewById(R.id.monthlyMinutesTextView);
        monthlyCaloriesTextView = view.findViewById(R.id.monthlyCaloriesTextView);
        monthlyExercisesTextView = view.findViewById(R.id.monthlyExercisesTextView);
        monthlyDaysTextView = view.findViewById(R.id.monthlyDaysTextView);
    }

    private void setStaticPlaceholders() {
        // Existing placeholders
        caloriesValueProgressTextView.setText(getString(R.string.placeholder_progress_calories));
        hoursValueProgressTextView.setText(getString(R.string.placeholder_progress_hours));
        trainingsGoalLabelTextView.setText(String.format(getString(R.string.progress_trainings_label_format),
                getString(R.string.placeholder_progress_trainings_current),
                getString(R.string.placeholder_progress_trainings_total)));
        trainingsProgressBar.setProgress(0);
        weightGoalLabelTextView.setText(String.format(getString(R.string.progress_weight_goal_label_format),
                getString(R.string.placeholder_progress_weight_diff)));
        weightGoalProgressBar.setProgress(0);
        fireStreakDescTextView.setText(String.format(getString(R.string.progress_achievement_fire_streak_desc_format),
                getString(R.string.placeholder_progress_streak_days)));

        // New placeholders
        weeklyMinutesTextView.setText(String.format(Locale.getDefault(), "Minutos Entrenados: %d min", 0));
        weeklyCaloriesTextView.setText(String.format(Locale.getDefault(), "Calorías Quemadas: %d kcal", 0));
        weeklyExercisesTextView.setText(String.format(Locale.getDefault(), "Ejercicios Completados: %d", 0));
        weeklyDaysTextView.setText(String.format(Locale.getDefault(), "Días de Entrenamiento: %d días", 0));

        monthlyMinutesTextView.setText(String.format(Locale.getDefault(), "Minutos Entrenados: %d min", 0));
        monthlyCaloriesTextView.setText(String.format(Locale.getDefault(), "Calorías Quemadas: %d kcal", 0));
        monthlyExercisesTextView.setText(String.format(Locale.getDefault(), "Ejercicios Completados: %d", 0));
        monthlyDaysTextView.setText(String.format(Locale.getDefault(), "Días de Entrenamiento: %d días", 0));
    }

    private void loadProgressData() {
        // Calculate date ranges
        long[] weekRange = getStartAndEndOfWeek();
        long[] monthRange = getStartAndEndOfMonth();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Weekly Stats
            Integer weeklyMinutes = workoutLogDao.getSumDurationMinutesInRange(currentUserId, weekRange[0], weekRange[1]);
            Integer weeklyCalories = workoutLogDao.getSumCaloriesBurnedInRange(currentUserId, weekRange[0], weekRange[1]);
            Integer weeklyExercises = workoutLogDao.getSumExercisesCompletedInRange(currentUserId, weekRange[0], weekRange[1]);
            Integer weeklyDays = workoutLogDao.countDistinctWorkoutDaysInRange(currentUserId, weekRange[0], weekRange[1]);

            // Monthly Stats
            Integer monthlyMinutes = workoutLogDao.getSumDurationMinutesInRange(currentUserId, monthRange[0], monthRange[1]);
            Integer monthlyCalories = workoutLogDao.getSumCaloriesBurnedInRange(currentUserId, monthRange[0], monthRange[1]);
            Integer monthlyExercises = workoutLogDao.getSumExercisesCompletedInRange(currentUserId, monthRange[0], monthRange[1]);
            Integer monthlyDays = workoutLogDao.countDistinctWorkoutDaysInRange(currentUserId, monthRange[0], monthRange[1]);

            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded() || getContext() == null) return; // Ensure fragment is still attached

                // Update Weekly UI
                weeklyMinutesTextView.setText(String.format(Locale.getDefault(), "Minutos Entrenados: %d min", weeklyMinutes != null ? weeklyMinutes : 0));
                weeklyCaloriesTextView.setText(String.format(Locale.getDefault(), "Calorías Quemadas: %d kcal", weeklyCalories != null ? weeklyCalories : 0));
                weeklyExercisesTextView.setText(String.format(Locale.getDefault(), "Ejercicios Completados: %d", weeklyExercises != null ? weeklyExercises : 0));
                weeklyDaysTextView.setText(String.format(Locale.getDefault(), "Días de Entrenamiento: %d días", weeklyDays != null ? weeklyDays : 0));

                // Update Monthly UI
                monthlyMinutesTextView.setText(String.format(Locale.getDefault(), "Minutos Entrenados: %d min", monthlyMinutes != null ? monthlyMinutes : 0));
                monthlyCaloriesTextView.setText(String.format(Locale.getDefault(), "Calorías Quemadas: %d kcal", monthlyCalories != null ? monthlyCalories : 0));
                monthlyExercisesTextView.setText(String.format(Locale.getDefault(), "Ejercicios Completados: %d", monthlyExercises != null ? monthlyExercises : 0));
                monthlyDaysTextView.setText(String.format(Locale.getDefault(), "Días de Entrenamiento: %d días", monthlyDays != null ? monthlyDays : 0));
            });
        });
    }

    private long[] getStartAndEndOfWeek() {
        Calendar calendar = Calendar.getInstance();
        // Set to the first day of the week (e.g., Monday)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek()); // Or Calendar.MONDAY
        setTimeToBeginningOfDay(calendar);
        long startOfWeek = calendar.getTimeInMillis();

        // Set to the last day of the week (e.g., Sunday)
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        setTimeToEndOfDay(calendar);
        long endOfWeek = calendar.getTimeInMillis();
        return new long[]{startOfWeek, endOfWeek};
    }

    private long[] getStartAndEndOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        setTimeToBeginningOfDay(calendar);
        long startOfMonth = calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        setTimeToEndOfDay(calendar);
        long endOfMonth = calendar.getTimeInMillis();
        return new long[]{startOfMonth, endOfMonth};
    }

    private void setTimeToBeginningOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setTimeToEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when the fragment becomes visible, in case logs were added
        // from another part of the app while this fragment was in the background.
        if (currentUserId != -1 && workoutLogDao != null) {
            loadProgressData();
        }
    }

    private void sendProgressToWebhook(String userName, String date, int progress, int duration, int calories) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                Log.d(TAG, "Iniciando envío de datos al Webhook...");
                URL url = new URL("https://primary-production-9e43.up.railway.app/webhook-test/progresosFitFlow"); // Reemplaza con la URL del Webhook
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("userName", userName);
                json.put("date", date);
                json.put("progress", progress);
                json.put("duration", duration);
                json.put("calories", calories);

                Log.d(TAG, "Datos a enviar: " + json);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Código de respuesta del Webhook: " + responseCode);

                // Leer cuerpo de respuesta (tanto en éxito como en error) para diagnóstico
                InputStream responseStream = null;
                try {
                    if (responseCode >= 200 && responseCode < 300) {
                        responseStream = conn.getInputStream();
                    } else {
                        responseStream = conn.getErrorStream();
                    }

                    if (responseStream != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line).append('\n');
                            }
                            String responseBody = sb.toString().trim();
                            Log.d(TAG, "Cuerpo de respuesta del Webhook: " + responseBody);
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "No se pudo leer el cuerpo de respuesta: " + e.getMessage());
                }

                if (responseCode >= 200 && responseCode < 300) {
                    Log.d(TAG, "Datos enviados exitosamente al Webhook.");
                } else {
                    Log.e(TAG, "Error al enviar datos al Webhook. Código de respuesta: " + responseCode);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error al enviar datos al Webhook", e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void updateProgressAndSend() {
        // Recolectar datos reales del usuario y sus estadísticas del día actual, luego enviarlos al webhook
        if (currentUserId == -1) {
            Log.w(TAG, "updateProgressAndSend: currentUserId no está definido, no se enviará respaldo.");
            return;
        }

        // Guardar contexto localmente para evitar NPE si el fragmento se desprende
        final Context ctx = getContext();
        if (ctx == null) {
            Log.w(TAG, "updateProgressAndSend: getContext() es null, abortando envío.");
            return;
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Obtener nombre del usuario
                UserDao userDao = AppDatabase.getDatabase(ctx.getApplicationContext()).userDao();
                User user = userDao.getUserById(currentUserId);
                String userName;
                if (user != null) {
                    String first = user.getName() != null ? user.getName() : "";
                    String last = user.getSurname() != null ? user.getSurname() : "";
                    userName = (first + " " + last).trim();
                    if (userName.isEmpty()) userName = "Usuario_" + currentUserId;
                } else {
                    userName = "Usuario_" + currentUserId;
                }

                // Calcular rango del día actual
                Calendar cal = Calendar.getInstance();
                setTimeToBeginningOfDay(cal);
                long startOfDay = cal.getTimeInMillis();
                setTimeToEndOfDay(cal);
                long endOfDay = cal.getTimeInMillis();

                // Obtener estadísticas del día desde WorkoutLogDao
                int duration = 0;
                int calories = 0;
                int progress = 0; // Usamos como "progreso" los ejercicios completados hoy
                try {
                    duration = workoutLogDao.getTotalDurationMinutesToday(currentUserId, startOfDay, endOfDay);
                } catch (Exception e) {
                    Log.w(TAG, "No se pudo obtener duration hoy: " + e.getMessage());
                }
                try {
                    calories = workoutLogDao.getTotalCaloriesBurnedToday(currentUserId, startOfDay, endOfDay);
                } catch (Exception e) {
                    Log.w(TAG, "No se pudo obtener calories hoy: " + e.getMessage());
                }
                try {
                    progress = workoutLogDao.getTotalExercisesCompletedToday(currentUserId, startOfDay, endOfDay);
                } catch (Exception e) {
                    Log.w(TAG, "No se pudo obtener exercises hoy: " + e.getMessage());
                }

                String date = Calendar.getInstance(Locale.getDefault()).getTime().toString();

                Log.d(TAG, "Preparando envío al webhook: user=" + userName + ", date=" + date + ", progress=" + progress + ", duration=" + duration + ", calories=" + calories);

                // Enviar los datos reales al webhook
                sendProgressToWebhook(userName, date, progress, duration, calories);

            } catch (Exception e) {
                Log.e(TAG, "Error en updateProgressAndSend:", e);
            }
        });
    }
}

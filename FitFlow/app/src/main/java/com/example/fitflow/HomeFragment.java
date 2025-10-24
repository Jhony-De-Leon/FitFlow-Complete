package com.example.fitflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.Random;

public class HomeFragment extends Fragment {

    private ImageView userAvatarImageView;
    private TextView greetingTextView;
    private ImageView notificationIconImageView;
    private ImageView aiChatIconImageView;
    private ImageView logoutIconImageView;
    private TextView progressPercentageTextView;
    private TextView caloriesValueTextView;
    private TextView minutesValueTextView;
    private TextView exercisesValueTextView;
    private MaterialButton startRoutineButton;
    private MaterialButton viewProgressButton;
    private ProgressBar progressLoadingIndicator;
    private TextView tipDescriptionTextView; // Nueva variable para el tip

    private UserDao userDao;
    private WorkoutLogDao workoutLogDao;
    private static final String TAG = "HomeFragment";

    private static final int DAILY_WORKOUT_MINUTES_GOAL = 30;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
        if (getActivity() != null) {
            AppDatabase db = AppDatabase.getDatabase(getActivity().getApplicationContext());
            userDao = db.userDao();
            workoutLogDao = db.workoutLogDao();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated. IsAdded: " + isAdded());

        userAvatarImageView = view.findViewById(R.id.userAvatarImageView);
        greetingTextView = view.findViewById(R.id.greetingTextView);
        notificationIconImageView = view.findViewById(R.id.notificationIconImageView);
        aiChatIconImageView = view.findViewById(R.id.aiChatIconImageView);
        logoutIconImageView = view.findViewById(R.id.logoutIconImageView);
        progressPercentageTextView = view.findViewById(R.id.progressPercentageTextView);
        caloriesValueTextView = view.findViewById(R.id.caloriesValueTextView);
        minutesValueTextView = view.findViewById(R.id.minutesValueTextView);
        exercisesValueTextView = view.findViewById(R.id.exercisesValueTextView);
        startRoutineButton = view.findViewById(R.id.startRoutineButton);
        viewProgressButton = view.findViewById(R.id.viewProgressButton);
        progressLoadingIndicator = view.findViewById(R.id.progressLoadingIndicator);
        tipDescriptionTextView = view.findViewById(R.id.tipDescriptionTextView); // Inicializar el TextView del tip

        loadUserDataAndProgress();
        loadDynamicNutritionalTip(view); // Cargar el tip dinámico

        notificationIconImageView.setOnClickListener(v -> Toast.makeText(getContext(), getString(R.string.home_toast_notification_clicked), Toast.LENGTH_SHORT).show());

        aiChatIconImageView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FlowCoachActivity.class);
            startActivity(intent);
        });

        logoutIconImageView.setOnClickListener(v -> {
            if (getActivity() == null) return;
            SharedPreferences prefs = getActivity().getSharedPreferences(RegisterActivity.SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(RegisterActivity.KEY_IS_LOGGED_IN, false);
            editor.remove(RegisterActivity.KEY_USER_ID);
            editor.apply();

            Toast.makeText(getContext(), "Cierre de sesión exitoso", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finishAffinity();
            }
        });

        userAvatarImageView.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                 ((HomeActivity) getActivity()).navigateToTab(R.id.navigation_profile);
            }
        });

        startRoutineButton.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToTab(R.id.navigation_routines);
            } else {
                Toast.makeText(getContext(), getString(R.string.home_toast_cannot_navigate_routines), Toast.LENGTH_SHORT).show();
            }
        });

        viewProgressButton.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).navigateToTab(R.id.navigation_progress);
            }
        });
    }

    private void loadDynamicNutritionalTip(View view) {
        if (getContext() != null && tipDescriptionTextView != null) {
            try {
                Resources res = getResources();
                String[] tips = res.getStringArray(R.array.nutritional_tips);
                if (tips.length > 0) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(tips.length);
                    tipDescriptionTextView.setText(tips[randomIndex]);
                } else {
                    tipDescriptionTextView.setText(getString(R.string.home_placeholder_nutritional_tip));
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Nutritional tips array not found", e);
                tipDescriptionTextView.setText(getString(R.string.home_placeholder_nutritional_tip));
            }
        }
    }

    private void loadUserDataAndProgress() {
        if (getActivity() == null || userDao == null || workoutLogDao == null) {
            Log.w(TAG, "loadUserDataAndProgress: Activity, userDao, or workoutLogDao is null.");
            if (greetingTextView != null && getContext() != null) {
                 greetingTextView.setText(String.format(getString(R.string.home_greeting_user_format), getString(R.string.placeholder_user_name)));
            }
            setPlaceholderProgressData();
            return;
        }

        if (progressLoadingIndicator != null) {
            progressLoadingIndicator.setVisibility(View.VISIBLE);
        }
        setPlaceholderProgressData();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                RegisterActivity.SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean(RegisterActivity.KEY_IS_LOGGED_IN, false);
        int userId = sharedPreferences.getInt(RegisterActivity.KEY_USER_ID, -1);

        if (isLoggedIn && userId != -1) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                User currentUser = userDao.getUserById(userId);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded() && getActivity() != null && getContext() != null) {
                        if (currentUser != null) {
                            greetingTextView.setText(String.format(getString(R.string.home_greeting_user_format), currentUser.getName()));
                            Log.d(TAG, "User data loaded: " + currentUser.getName());
                        } else {
                            greetingTextView.setText(String.format(getString(R.string.home_greeting_user_format), getString(R.string.placeholder_user_name)));
                            Log.d(TAG, "User not found in DB, setting placeholder for greeting.");
                        }
                    } else {
                        Log.d(TAG, "loadUserData (greeting) callback: Fragment not attached or activity is null.");
                    }
                });
            });
        } else {
            Log.d(TAG, "User not logged in or userId invalid, setting placeholder for greeting.");
            if (greetingTextView != null && getContext() != null) {
                 greetingTextView.setText(String.format(getString(R.string.home_greeting_user_format), getString(R.string.placeholder_user_name)));
            }
        }

        if (userId != -1) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                long[] todayTimestamps = getTodayTimestampRange();
                long startOfDay = todayTimestamps[0];
                long endOfDay = todayTimestamps[1];

                int totalCalories = workoutLogDao.getTotalCaloriesBurnedToday(userId, startOfDay, endOfDay);
                int totalMinutes = workoutLogDao.getTotalDurationMinutesToday(userId, startOfDay, endOfDay);
                int totalExercises = workoutLogDao.getTotalExercisesCompletedToday(userId, startOfDay, endOfDay);

                int progressPercentage = 0;
                if (DAILY_WORKOUT_MINUTES_GOAL > 0) {
                    progressPercentage = (int) (((double) totalMinutes / DAILY_WORKOUT_MINUTES_GOAL) * 100);
                }
                if (progressPercentage > 100) progressPercentage = 100;

                final int finalTotalCalories = totalCalories;
                final int finalTotalMinutes = totalMinutes;
                final int finalTotalExercises = totalExercises;
                final int finalProgressPercentage = progressPercentage;

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded() && getActivity() != null && getContext() != null) {
                        if (caloriesValueTextView != null) caloriesValueTextView.setText(String.valueOf(finalTotalCalories));
                        if (minutesValueTextView != null) minutesValueTextView.setText(String.valueOf(finalTotalMinutes));
                        if (exercisesValueTextView != null) exercisesValueTextView.setText(String.valueOf(finalTotalExercises));
                        if (progressPercentageTextView != null) progressPercentageTextView.setText(String.format(getString(R.string.home_label_percentage_completed_format), String.valueOf(finalProgressPercentage)));
                        Log.d(TAG, "Progress data loaded: Cals=" + finalTotalCalories + ", Mins=" + finalTotalMinutes + ", Excs=" + finalTotalExercises + ", Prog%=" + finalProgressPercentage);
                    } else {
                        Log.d(TAG, "loadUserData (progress) callback: Fragment not attached or activity is null.");
                    }
                    if (progressLoadingIndicator != null) {
                        progressLoadingIndicator.setVisibility(View.GONE);
                    }
                });
            });
        } else {
            Log.w(TAG, "Cannot load progress data, userId is invalid.");
            setPlaceholderProgressData();
            if (progressLoadingIndicator != null) {
                progressLoadingIndicator.setVisibility(View.GONE);
            }
        }
    }

    private void setPlaceholderProgressData() {
        if (isAdded() && getActivity() != null && getContext() != null) {
            if (progressPercentageTextView != null) progressPercentageTextView.setText(String.format(getString(R.string.home_label_percentage_completed_format), getString(R.string.placeholder_progress_percentage_value)));
            if (caloriesValueTextView != null) caloriesValueTextView.setText(getString(R.string.placeholder_calories_value));
            if (minutesValueTextView != null) minutesValueTextView.setText(getString(R.string.placeholder_minutes_value));
            if (exercisesValueTextView != null) exercisesValueTextView.setText(getString(R.string.placeholder_exercises_value));
        }
    }

    private long[] getTodayTimestampRange() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endOfDay = calendar.getTimeInMillis();

        return new long[]{startOfDay, endOfDay};
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Reloading data");
        loadUserDataAndProgress();
        // Considerar si también se quiere refrescar el tip en onResume
        // loadDynamicNutritionalTip(getView()); 
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        userAvatarImageView = null;
        greetingTextView = null;
        notificationIconImageView = null;
        aiChatIconImageView = null;
        logoutIconImageView = null;
        progressPercentageTextView = null;
        caloriesValueTextView = null;
        minutesValueTextView = null;
        exercisesValueTextView = null;
        startRoutineButton = null;
        viewProgressButton = null;
        progressLoadingIndicator = null;
        tipDescriptionTextView = null; // Anular la referencia al TextView del tip
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
        userDao = null;
        workoutLogDao = null;
    }
}

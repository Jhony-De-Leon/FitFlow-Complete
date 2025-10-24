package com.example.fitflow;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WebhookSender {
    private static final String TAG = "WebhookSender";
    private static final String WEBHOOK_URL = "https://primary-production-9e43.up.railway.app/webhook-test/progresosFitFlow";

    public static void sendWithUserId(Context ctx, int userId, int progress, int duration, int calories, String routineTitle) {
        if (ctx == null) {
            Log.w(TAG, "Context is null, aborting webhook send");
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                UserDao userDao = AppDatabase.getDatabase(ctx.getApplicationContext()).userDao();
                User user = userDao.getUserById(userId);
                String userName = (user != null) ? ((user.getName() != null ? user.getName() : "") + " " + (user.getSurname() != null ? user.getSurname() : "")).trim() : "Usuario_" + userId;
                if (userName.isEmpty()) userName = "Usuario_" + userId;

                // Calcular resumen mensual usando WorkoutLogDao
                WorkoutLogDao workoutLogDao = AppDatabase.getDatabase(ctx.getApplicationContext()).workoutLogDao();
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                setTimeToBeginningOfDay(cal);
                long startOfMonth = cal.getTimeInMillis();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                setTimeToEndOfDay(cal);
                long endOfMonth = cal.getTimeInMillis();

                Integer monthlyMinutes = workoutLogDao.getSumDurationMinutesInRange(userId, startOfMonth, endOfMonth);
                Integer monthlyCalories = workoutLogDao.getSumCaloriesBurnedInRange(userId, startOfMonth, endOfMonth);
                Integer monthlyExercises = workoutLogDao.getSumExercisesCompletedInRange(userId, startOfMonth, endOfMonth);
                Integer monthlyDays = workoutLogDao.countDistinctWorkoutDaysInRange(userId, startOfMonth, endOfMonth);

                int monthlyMinutesVal = monthlyMinutes != null ? monthlyMinutes : 0;
                int monthlyCaloriesVal = monthlyCalories != null ? monthlyCalories : 0;
                int monthlyExercisesVal = monthlyExercises != null ? monthlyExercises : 0;
                int monthlyDaysVal = monthlyDays != null ? monthlyDays : 0;

                String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(new Date());
                send(userName, date, progress, duration, calories, routineTitle, monthlyMinutesVal, monthlyCaloriesVal, monthlyExercisesVal, monthlyDaysVal);

            } catch (Exception e) {
                Log.e(TAG, "Error fetching user or sending webhook", e);
            }
        });
    }

    private static void setTimeToBeginningOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private static void setTimeToEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    public static void send(String userName, String date, int progress, int duration, int calories, String routineTitle, int monthlyMinutes, int monthlyCalories, int monthlyExercises, int monthlyDays) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                Log.d(TAG, "Sending webhook payload for user=" + userName + " routine=" + routineTitle);
                URL url = new URL(WEBHOOK_URL);
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
                json.put("routineTitle", routineTitle != null ? routineTitle : "");

                // Añadir resumen mensual
                json.put("monthlyMinutes", monthlyMinutes);
                json.put("monthlyCalories", monthlyCalories);
                json.put("monthlyExercises", monthlyExercises);
                json.put("monthlyDays", monthlyDays);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Webhook response code: " + responseCode);

                InputStream responseStream = null;
                try {
                    if (responseCode >= 200 && responseCode < 300) responseStream = conn.getInputStream(); else responseStream = conn.getErrorStream();
                    if (responseStream != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line).append('\n');
                            }
                            String responseBody = sb.toString().trim();
                            Log.d(TAG, "Webhook response body: " + responseBody);
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Could not read webhook response: " + e.getMessage());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error sending webhook", e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}

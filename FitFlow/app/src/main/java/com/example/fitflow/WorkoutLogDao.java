package com.example.fitflow;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WorkoutLogDao {

    @Insert
    void insert(WorkoutLog workoutLog);

    // Get all logs for a specific user within a specific date range (e.g., for one day)
    @Query("SELECT * FROM workout_logs WHERE userId = :userId AND date >= :startOfDay AND date <= :endOfDay ORDER BY date DESC")
    List<WorkoutLog> getWorkoutLogsForUserByDateRange(int userId, long startOfDay, long endOfDay);

    // Get all workout logs for a specific user
    @Query("SELECT * FROM workout_logs WHERE userId = :userId ORDER BY date DESC")
    List<WorkoutLog> getAllWorkoutLogsForUser(int userId);

    // --- Methods for daily stats (as previously existed, can be used by HomeFragment) ---
    @Query("SELECT SUM(caloriesBurned) FROM workout_logs WHERE userId = :userId AND date >= :startOfDay AND date <= :endOfDay")
    int getTotalCaloriesBurnedToday(int userId, long startOfDay, long endOfDay);

    @Query("SELECT SUM(durationMinutes) FROM workout_logs WHERE userId = :userId AND date >= :startOfDay AND date <= :endOfDay")
    int getTotalDurationMinutesToday(int userId, long startOfDay, long endOfDay);

    @Query("SELECT SUM(exercisesCompleted) FROM workout_logs WHERE userId = :userId AND date >= :startOfDay AND date <= :endOfDay")
    int getTotalExercisesCompletedToday(int userId, long startOfDay, long endOfDay);

    // --- NEW Methods for aggregated stats over a generic date range (for ProgressFragment) ---
    @Query("SELECT SUM(durationMinutes) FROM workout_logs WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    Integer getSumDurationMinutesInRange(int userId, long startDate, long endDate);

    @Query("SELECT SUM(caloriesBurned) FROM workout_logs WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    Integer getSumCaloriesBurnedInRange(int userId, long startDate, long endDate);

    @Query("SELECT SUM(exercisesCompleted) FROM workout_logs WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    Integer getSumExercisesCompletedInRange(int userId, long startDate, long endDate);

    // Counts distinct days a user had a workout log within the given range
    // Divides timestamp by milliseconds in a day to group by day, then counts distinct results.
    @Query("SELECT COUNT(DISTINCT (date / (1000 * 60 * 60 * 24))) FROM workout_logs WHERE userId = :userId AND date >= :startDate AND date <= :endDate")
    Integer countDistinctWorkoutDaysInRange(int userId, long startDate, long endDate);

}

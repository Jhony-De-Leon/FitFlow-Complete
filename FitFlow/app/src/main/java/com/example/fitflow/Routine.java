package com.example.fitflow;

import java.util.List;

public class Routine {
    private String title;
    private String duration;
    private String difficulty;
    private String category;
    private List<Exercise> exercises; // Modificado de List<String> a List<Exercise>

    // Constructor actualizado
    public Routine(String title, String duration, String difficulty, String category, List<Exercise> exercises) {
        this.title = title;
        this.duration = duration;
        this.difficulty = difficulty;
        this.category = category;
        this.exercises = exercises;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Getter y Setter actualizados para List<Exercise>
    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public String getFormattedDetails() {
        // Podríamos añadir el número de ejercicios aquí si quisiéramos
        return String.format("%s • %s - %s", duration, difficulty, category);
    }
}

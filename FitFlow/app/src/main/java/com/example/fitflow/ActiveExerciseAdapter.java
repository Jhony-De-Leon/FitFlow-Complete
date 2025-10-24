package com.example.fitflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActiveExerciseAdapter extends RecyclerView.Adapter<ActiveExerciseAdapter.ExerciseViewHolder> {

    private List<Exercise> exerciseList;
    private LayoutInflater inflater;

    public ActiveExerciseAdapter(Context context, List<Exercise> exerciseList) {
        this.inflater = LayoutInflater.from(context);
        this.exerciseList = exerciseList;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.list_item_active_exercise, parent, false);
        return new ExerciseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise currentExercise = exerciseList.get(position);
        holder.exerciseNameTextView.setText(currentExercise.getName());
        holder.exerciseCheckBox.setChecked(currentExercise.isCompleted());

        // Actualizar el estado del ejercicio cuando se marca/desmarca el CheckBox
        holder.exerciseCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentExercise.setCompleted(isChecked);
            // Podríamos añadir un Log aquí si queremos verificar el cambio de estado
            // Log.d("ActiveExerciseAdapter", "Exercise: " + currentExercise.getName() + ", Completed: " + isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return exerciseList == null ? 0 : exerciseList.size();
    }

    // Método para obtener la lista actual de ejercicios (con sus estados actualizados)
    public List<Exercise> getExerciseList() {
        return exerciseList;
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        CheckBox exerciseCheckBox;
        TextView exerciseNameTextView;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseCheckBox = itemView.findViewById(R.id.exerciseCheckBox);
            exerciseNameTextView = itemView.findViewById(R.id.exerciseNameTextView);
        }
    }
}

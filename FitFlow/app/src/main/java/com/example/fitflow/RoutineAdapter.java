package com.example.fitflow;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    private List<Routine> routineList;
    private Context context;

    public RoutineAdapter(Context context, List<Routine> routineList) {
        this.context = context;
        this.routineList = routineList;
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        Routine routine = routineList.get(position);
        holder.titleTextView.setText(routine.getTitle());
        holder.detailsTextView.setText(routine.getFormattedDetails());

        holder.playIconImageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActiveRoutineActivity.class);
            intent.putExtra(ActiveRoutineActivity.EXTRA_ROUTINE_TITLE, routine.getTitle());
            intent.putExtra(ActiveRoutineActivity.EXTRA_ROUTINE_DURATION_STRING, routine.getDuration());
            // Pasar la lista de objetos Exercise
            if (routine.getExercises() != null) {
                // Asegurarse de que la lista sea un ArrayList para putParcelableArrayListExtra
                ArrayList<Exercise> exercisesToPass = new ArrayList<>(routine.getExercises());
                intent.putParcelableArrayListExtra(ActiveRoutineActivity.EXTRA_ROUTINE_EXERCISES, exercisesToPass);
            }
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            String toastMessage = String.format(context.getString(R.string.routines_adapter_toast_view_details_format), routine.getTitle());
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
            // TODO: Implementar navegación a una pantalla de detalles de la rutina si se desea
        });
    }

    @Override
    public int getItemCount() {
        return routineList.size();
    }

    static class RoutineViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView detailsTextView;
        ImageView playIconImageView;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.routineItemTitleTextView);
            detailsTextView = itemView.findViewById(R.id.routineItemDetailsTextView);
            playIconImageView = itemView.findViewById(R.id.routineItemPlayIconImageView);
        }
    }
}

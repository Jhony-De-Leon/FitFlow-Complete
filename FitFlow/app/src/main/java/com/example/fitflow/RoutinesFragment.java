package com.example.fitflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors; // Necesario para el mapeo

public class RoutinesFragment extends Fragment {

    private TextInputEditText searchRoutineEditText;
    private MaterialButton startRecommendedRoutineButton;
    private RecyclerView routinesRecyclerView;
    private RoutineAdapter routineAdapter;
    private List<Routine> routineList;

    public RoutinesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); 
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_routines, container, false);
    }

    private List<Exercise> createExerciseList(List<String> exerciseNames) {
        if (exerciseNames == null) {
            return new ArrayList<>();
        }
        return exerciseNames.stream().map(Exercise::new).collect(Collectors.toList());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchRoutineEditText = view.findViewById(R.id.searchRoutineEditText);
        startRecommendedRoutineButton = view.findViewById(R.id.startRecommendedRoutineButton);
        routinesRecyclerView = view.findViewById(R.id.routinesRecyclerView);

        String durationIntermediateRecommended = String.format(getString(R.string.routines_duration_difficulty_format),
                "45", getString(R.string.routines_level_intermediate));
        ((TextView) view.findViewById(R.id.recommendedRoutineDurationTextView)).setText(durationIntermediateRecommended);

        startRecommendedRoutineButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), getString(R.string.routines_toast_start_recommended_implement), Toast.LENGTH_SHORT).show();
        });

        routinesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        routineList = new ArrayList<>();
        
        // Crear listas de ejercicios para cada rutina
        List<Exercise> strengthExercises = createExerciseList(Arrays.asList(
            "Push-ups x12", "Squats x15", "Plank 60s", "Dumbbell Rows x10/arm", "Lunges x10/leg"
        ));
        List<Exercise> yogaExercises = createExerciseList(Arrays.asList(
            "Sun Salutation A x3", "Downward Dog 30s", "Warrior II 30s/side", "Triangle Pose 30s/side", "Child's Pose 60s"
        ));
        List<Exercise> hiitExercises = createExerciseList(Arrays.asList(
            "Burpees x15", "High Knees 45s", "Jump Squats x20", "Mountain Climbers 45s", "Rest 15s", "Repeat Circuit x3"
        ));
        List<Exercise> fullBodyExpressExercises = createExerciseList(Arrays.asList(
            "Jumping Jacks 60s", "Glute Bridges x15", "Russian Twists x20", "Superman x15", "Cool Down Stretches"
        ));
        List<Exercise> cardioBlastExercises = createExerciseList(Arrays.asList(
            "Running (steady pace) 25min", "Cycling (moderate) 20min", "Rowing (sprints) 5min"
        ));

        routineList.add(new Routine(getString(R.string.routines_strength_title), "45 min", getString(R.string.routines_level_intermediate), getString(R.string.routines_category_strength), strengthExercises));
        routineList.add(new Routine(getString(R.string.routines_yoga_title), "20 min", getString(R.string.routines_level_beginner), getString(R.string.routines_category_flexibility), yogaExercises));
        routineList.add(new Routine(getString(R.string.routines_hiit_title), "30 min", getString(R.string.routines_level_advanced), "HIIT", hiitExercises));
        routineList.add(new Routine(getString(R.string.routines_sample_full_body_express_title), "35 min", getString(R.string.routines_level_intermediate), "General", fullBodyExpressExercises));
        routineList.add(new Routine(getString(R.string.routines_sample_cardio_blast_title), "50 min", getString(R.string.routines_level_advanced), "Cardio", cardioBlastExercises));

        routineAdapter = new RoutineAdapter(getContext(), routineList);
        routinesRecyclerView.setAdapter(routineAdapter);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.routines_toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.action_search_routines);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.routines_search_hint)); 
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Toast.makeText(getContext(), String.format(getString(R.string.routines_toast_search_toolbar_format), query), Toast.LENGTH_SHORT).show();
                    searchView.clearFocus();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search_routines) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

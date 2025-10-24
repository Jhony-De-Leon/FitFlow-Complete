package com.example.fitflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterGoalsFragment extends Fragment {

    private TextInputLayout mainGoalInputLayout;
    private TextInputEditText mainGoalEditText;

    public RegisterGoalsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainGoalInputLayout = view.findViewById(R.id.mainGoalInputLayout);
        mainGoalEditText = view.findViewById(R.id.mainGoalEditText);
    }

    public String getMainGoalInput() {
        if (mainGoalEditText != null) {
            return mainGoalEditText.getText().toString().trim();
        }
        return "";
    }

    // Por ahora, la validación es simple ya que el campo es opcional
    // o no tiene reglas estrictas definidas aún.
    public boolean isGoalValid() {
        // String goal = getMainGoalInput();
        // if (goal.isEmpty()) { // Si quieres hacerlo obligatorio en el futuro
        //     if (mainGoalInputLayout != null) {
        //         mainGoalInputLayout.setError("El objetivo no puede estar vacío");
        //     }
        //     return false;
        // }
        if (mainGoalInputLayout != null) {
            mainGoalInputLayout.setError(null); // Clear error
        }
        return true; // Siempre válido por ahora
    }
}

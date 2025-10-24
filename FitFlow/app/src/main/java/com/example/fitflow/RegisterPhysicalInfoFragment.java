package com.example.fitflow;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterPhysicalInfoFragment extends Fragment {

    private TextInputLayout weightInputLayout;
    private TextInputEditText weightEditText;
    private TextInputLayout heightInputLayout;
    private TextInputEditText heightEditText;
    private Spinner objectiveSpinner;
    // private TextView objectiveErrorTextView; // Si necesitas un TextView para errores del Spinner

    public RegisterPhysicalInfoFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Corregir el layout al que contiene los campos de información física
        return inflater.inflate(R.layout.fragment_register_physical_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        weightInputLayout = view.findViewById(R.id.weightInputLayout);
        weightEditText = view.findViewById(R.id.weightEditText);
        heightInputLayout = view.findViewById(R.id.heightInputLayout);
        heightEditText = view.findViewById(R.id.heightEditText);
        objectiveSpinner = view.findViewById(R.id.objectiveSpinner);

        // Limpiar error del spinner cuando se selecciona un item válido
        objectiveSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Lógica para limpiar error si tienes un objectiveErrorTextView
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public String getWeightInput() {
        return weightEditText != null ? weightEditText.getText().toString().trim() : "";
    }

    public String getHeightInput() {
        return heightEditText != null ? heightEditText.getText().toString().trim() : "";
    }

    public String getMainGoalInput() {
        if (objectiveSpinner != null && objectiveSpinner.getSelectedItemPosition() > 0) {
            return objectiveSpinner.getSelectedItem().toString();
        }
        return ""; // O maneja el caso de "Selecciona tu objetivo"
    }

    public boolean isWeightValid() {
        String weightStr = getWeightInput();
        if (TextUtils.isEmpty(weightStr)) {
            if (weightInputLayout != null) weightInputLayout.setError("El peso no puede estar vacío");
            return false;
        }
        try {
            double weight = Double.parseDouble(weightStr);
            if (weight <= 0 || weight > 500) { // Rango de peso simple
                if (weightInputLayout != null) weightInputLayout.setError("Ingresa un peso válido");
                return false;
            }
        } catch (NumberFormatException e) {
            if (weightInputLayout != null) weightInputLayout.setError("Ingresa un número válido para el peso");
            return false;
        }
        if (weightInputLayout != null) weightInputLayout.setError(null);
        return true;
    }

    public boolean isHeightValid() {
        String heightStr = getHeightInput();
        if (TextUtils.isEmpty(heightStr)) {
            if (heightInputLayout != null) heightInputLayout.setError("La altura no puede estar vacía");
            return false;
        }
        try {
            int height = Integer.parseInt(heightStr);
            if (height <= 0 || height > 300) { // Rango de altura simple en cm
                if (heightInputLayout != null) heightInputLayout.setError("Ingresa una altura válida");
                return false;
            }
        } catch (NumberFormatException e) {
            if (heightInputLayout != null) heightInputLayout.setError("Ingresa un número válido para la altura");
            return false;
        }
        if (heightInputLayout != null) heightInputLayout.setError(null);
        return true;
    }

    public boolean isMainGoalValid() {
        if (objectiveSpinner != null && objectiveSpinner.getSelectedItemPosition() == 0) { // Posición 0 es el prompt
            // Similar al genderSpinner, mostrar error es preferible con un TextView
            // o cambiar el color del prompt.
            return false;
        }
        return true;
    }

    public boolean areAllInputsValid() {
        boolean weightValid = isWeightValid();
        boolean heightValid = isHeightValid();
        boolean goalValid = isMainGoalValid(); // Cambiado desde isGoalValid() a isMainGoalValid()
        return weightValid && heightValid && goalValid;
    }
}

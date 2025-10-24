package com.example.fitflow;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterPersonalInfoFragment extends Fragment {

    private TextInputLayout nameInputLayout;
    private TextInputEditText nameEditText;
    private TextInputLayout surnameInputLayout;
    private TextInputEditText surnameEditText;
    private TextInputLayout ageInputLayout;
    private TextInputEditText ageEditText;
    private Spinner genderSpinner;
    private TextView genderErrorTextView; // Para mostrar errores del Spinner

    public RegisterPersonalInfoFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Corregir el layout al que contiene todos los campos personales
        return inflater.inflate(R.layout.fragment_register_personal_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nameInputLayout = view.findViewById(R.id.nameInputLayout);
        nameEditText = view.findViewById(R.id.nameEditText);
        surnameInputLayout = view.findViewById(R.id.surnameInputLayout);
        surnameEditText = view.findViewById(R.id.surnameEditText);
        ageInputLayout = view.findViewById(R.id.ageInputLayout);
        ageEditText = view.findViewById(R.id.ageEditText);
        genderSpinner = view.findViewById(R.id.genderSpinner);

        // Crear un TextView para errores del Spinner si no existe en el XML
        // O encontrarlo si ya lo añadiste. Por ahora, asumimos que no está y lo manejaremos en la validación.
        // Si tu XML `fragment_register_personal_info` tiene un TextView para errores de género,
        // inicialízalo aquí: genderErrorTextView = view.findViewById(R.id.your_gender_error_textview_id);

        // Limpiar error del spinner cuando se selecciona un item válido
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    // Lógica para limpiar error si tienes un genderErrorTextView
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public String getNameInput() {
        return nameEditText != null ? nameEditText.getText().toString().trim() : "";
    }

    public String getSurnameInput() {
        return surnameEditText != null ? surnameEditText.getText().toString().trim() : "";
    }

    public String getAgeInput() {
        return ageEditText != null ? ageEditText.getText().toString().trim() : "";
    }

    public String getGenderInput() {
        if (genderSpinner != null && genderSpinner.getSelectedItemPosition() > 0) {
            return genderSpinner.getSelectedItem().toString();
        }
        return ""; // O maneja el caso de "Selecciona tu género"
    }

    public boolean isNameValid() {
        String name = getNameInput();
        if (TextUtils.isEmpty(name)) {
            if (nameInputLayout != null) nameInputLayout.setError("El nombre no puede estar vacío");
            return false;
        }
        if (nameInputLayout != null) nameInputLayout.setError(null);
        return true;
    }

    public boolean isSurnameValid() {
        String surname = getSurnameInput();
        if (TextUtils.isEmpty(surname)) {
            if (surnameInputLayout != null) surnameInputLayout.setError("El apellido no puede estar vacío");
            return false;
        }
        if (surnameInputLayout != null) surnameInputLayout.setError(null);
        return true;
    }

    public boolean isAgeValid() {
        String ageStr = getAgeInput();
        if (TextUtils.isEmpty(ageStr)) {
            if (ageInputLayout != null) ageInputLayout.setError("La edad no puede estar vacía");
            return false;
        }
        try {
            int age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 120) { // Rango de edad simple
                if (ageInputLayout != null) ageInputLayout.setError("Ingresa una edad válida");
                return false;
            }
        } catch (NumberFormatException e) {
            if (ageInputLayout != null) ageInputLayout.setError("Ingresa un número válido para la edad");
            return false;
        }
        if (ageInputLayout != null) ageInputLayout.setError(null);
        return true;
    }

    public boolean isGenderValid() {
        if (genderSpinner != null && genderSpinner.getSelectedItemPosition() == 0) { // Posición 0 es el prompt
            // Para mostrar un error en el Spinner, usualmente se usa un TextView debajo de él
            // o se cambia el color del texto del prompt.
            // Aquí solo retornamos false. La actividad puede mostrar un Toast.
            // Si tienes un genderErrorTextView:
            // genderErrorTextView.setVisibility(View.VISIBLE);
            // genderErrorTextView.setText("Selecciona un género");
            return false;
        }
        // if (genderErrorTextView != null) genderErrorTextView.setVisibility(View.GONE);
        return true;
    }

    public boolean areAllInputsValid() {
        // Llamar a todos los validadores individuales
        boolean nameValid = isNameValid();
        boolean surnameValid = isSurnameValid();
        boolean ageValid = isAgeValid();
        boolean genderValid = isGenderValid();
        return nameValid && surnameValid && ageValid && genderValid;
    }
}

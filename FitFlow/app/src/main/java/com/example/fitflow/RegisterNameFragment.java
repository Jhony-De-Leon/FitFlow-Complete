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

public class RegisterNameFragment extends Fragment {

    private TextInputLayout nameInputLayout;
    private TextInputEditText nameEditText;

    public RegisterNameFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_name, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nameInputLayout = view.findViewById(R.id.nameInputLayout);
        nameEditText = view.findViewById(R.id.nameEditText);
    }

    public String getNameInput() {
        if (nameEditText != null) {
            return nameEditText.getText().toString().trim();
        }
        return "";
    }

    public boolean isNameValid() {
        String name = getNameInput();
        if (name.isEmpty()) {
            if (nameInputLayout != null) {
                nameInputLayout.setError("El nombre no puede estar vacío");
            }
            return false;
        }
        // Puedes añadir más validaciones, como longitud mínima, si es necesario
        if (nameInputLayout != null) {
            nameInputLayout.setError(null); // Clear error
        }
        return true;
    }
}

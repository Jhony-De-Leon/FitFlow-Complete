package com.example.fitflow;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterCredentialsFragment extends Fragment {

    private TextInputLayout emailInputLayout;
    private TextInputEditText emailEditText;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordEditText;

    public RegisterCredentialsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_credentials, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emailInputLayout = view.findViewById(R.id.emailInputLayout);
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout);
        passwordEditText = view.findViewById(R.id.passwordEditText);
    }

    public String getEmailInput() {
        if (emailEditText != null) {
            return emailEditText.getText().toString().trim();
        }
        return "";
    }

    public boolean isEmailValid() {
        String email = getEmailInput();
        if (email.isEmpty()) {
            if (emailInputLayout != null) {
                emailInputLayout.setError("El email no puede estar vacío");
            }
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (emailInputLayout != null) {
                emailInputLayout.setError("Formato de email no válido");
            }
            return false;
        }
        if (emailInputLayout != null) {
            emailInputLayout.setError(null);
        }
        return true;
    }

    public String getPasswordInput() {
        if (passwordEditText != null) {
            return passwordEditText.getText().toString(); // No trimear la contraseña por si tiene espacios intencionales
        }
        return "";
    }

    public boolean isPasswordValid() {
        String password = getPasswordInput();
        if (password.isEmpty()) {
            if (passwordInputLayout != null) {
                passwordInputLayout.setError("La contraseña no puede estar vacía");
            }
            return false;
        }
        if (password.length() < 6) { // Ejemplo: longitud mínima de 6 caracteres
            if (passwordInputLayout != null) {
                passwordInputLayout.setError("La contraseña debe tener al menos 6 caracteres");
            }
            return false;
        }
        // Puedes añadir más validaciones: mayúsculas, números, símbolos, etc.
        if (passwordInputLayout != null) {
            passwordInputLayout.setError(null);
        }
        return true;
    }
}

package com.example.fitflow;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText newPasswordEditText;
    private TextInputEditText confirmNewPasswordEditText;
    private MaterialButton savePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        Toolbar toolbar = findViewById(R.id.toolbar_change_password);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_change_password_activity);
        }

        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText);
        savePasswordButton = findViewById(R.id.savePasswordButton);

        savePasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText() != null ? newPasswordEditText.getText().toString() : "";
            String confirmNewPassword = confirmNewPasswordEditText.getText() != null ? confirmNewPasswordEditText.getText().toString() : "";

            if (TextUtils.isEmpty(newPassword)) {
                newPasswordEditText.setError("La nueva contraseña no puede estar vacía");
                return;
            }

            if (newPassword.length() < 6) { // Example: Minimum password length
                newPasswordEditText.setError("La contraseña debe tener al menos 6 caracteres");
                return;
            }

            if (TextUtils.isEmpty(confirmNewPassword)) {
                confirmNewPasswordEditText.setError("Confirme la nueva contraseña");
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                confirmNewPasswordEditText.setError("Las contraseñas no coinciden");
                return;
            }

            // TODO: Implement actual password change logic (e.g., call API, update database)
            Toast.makeText(ChangePasswordActivity.this, "Contraseña cambiada exitosamente (Placeholder)", Toast.LENGTH_SHORT).show();
            
            // Example: Navigate to LoginActivity after successful password change
            // Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            // startActivity(intent);
            // finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Go back to the previous activity
        return true;
    }
}

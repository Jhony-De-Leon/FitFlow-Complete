package com.example.fitflow;

import android.content.Intent; // Added this line
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private MaterialButton sendLinkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Toolbar toolbar = findViewById(R.id.toolbar_forgot_password);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_forgot_password_activity);
        }

        emailEditText = findViewById(R.id.emailForgotPasswordEditText);
        sendLinkButton = findViewById(R.id.sendLinkButton);

        sendLinkButton.setOnClickListener(v -> {
            String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";

            if (email.isEmpty()) {
                emailEditText.setError("El correo electrónico no puede estar vacío");
                return;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Ingrese un correo electrónico válido");
                return;
            }

            // TODO: Implement actual password reset logic (e.g., call Firebase Auth or your backend)
            Toast.makeText(ForgotPasswordActivity.this, "Enlace para reestablecer enviado a " + email + " (Placeholder)", Toast.LENGTH_LONG).show();
            
            // Navigate to ChangePasswordActivity for testing the flow
            Intent intent = new Intent(ForgotPasswordActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            
            // Optionally, finish this activity after sending the link and navigating
            // finish(); 
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Go back to the previous activity
        return true;
    }
}

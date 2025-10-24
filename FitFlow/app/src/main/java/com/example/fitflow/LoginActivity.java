package com.example.fitflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton loginSubmitButton;
    private TextView registerHereTextView;
    private TextView forgotPasswordTextView;

    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        userDao = db.userDao();

        Toolbar toolbar = findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_login_activity);
        }

        emailEditText = findViewById(R.id.emailLoginEditText);
        passwordEditText = findViewById(R.id.passwordLoginEditText);
        loginSubmitButton = findViewById(R.id.loginSubmitButton);
        registerHereTextView = findViewById(R.id.registerHereLoginTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        loginSubmitButton.setOnClickListener(v -> {
            String email = emailEditText.getText() != null ? emailEditText.getText().toString().trim() : "";
            String password = passwordEditText.getText() != null ? passwordEditText.getText().toString() : "";

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(LoginActivity.this, "Por favor, ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Por favor, ingrese su contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ejecutar la verificación de credenciales en un hilo secundario
            AppDatabase.databaseWriteExecutor.execute(() -> {
                User user = userDao.getUserByEmail(email);

                if (user != null && PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
                    // Usuario encontrado y contraseña correcta
                    SharedPreferences sharedPreferences = getSharedPreferences(RegisterActivity.SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(RegisterActivity.KEY_IS_LOGGED_IN, true);
                    editor.putInt(RegisterActivity.KEY_USER_ID, user.getId()); // Guardar el ID del usuario
                    editor.apply();

                    // Navegar a HomeActivity en el hilo UI
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(getApplicationContext(), "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    // Usuario no encontrado o contraseña incorrecta
                    new Handler(Looper.getMainLooper()).post(() -> 
                        Toast.makeText(getApplicationContext(), "Email o contraseña incorrectos.", Toast.LENGTH_LONG).show());
                }
            });
        });

        registerHereTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

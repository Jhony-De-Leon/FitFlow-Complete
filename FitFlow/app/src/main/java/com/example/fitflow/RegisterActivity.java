package com.example.fitflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

public class RegisterActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private RegisterViewPagerAdapter pagerAdapter;
    private MaterialButton previousButton;
    private MaterialButton nextButton;
    private TextView loginHereTextView;

    private static final String TAG = "RegisterActivity";
    private static final int NUM_PAGES = 3; // 0: Personal, 1: Physical, 2: Account

    public static final String SHARED_PREFS_USER_DATA = "UserDataPrefs";
    public static final String KEY_USER_ID = "currentUserId";
    public static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        userDao = db.userDao();

        Toolbar toolbar = findViewById(R.id.toolbar_register);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_register_activity);
        }

        viewPager = findViewById(R.id.registerViewPager);
        pagerAdapter = new RegisterViewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(false); // Deshabilitar swipe entre fragments

        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        loginHereTextView = findViewById(R.id.loginHereTextView);

        setupButtonListeners();
        updateButtonVisibility(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonVisibility(position);
            }
        });
    }

    private void setupButtonListeners() {
        previousButton.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1);
            }
        });

        nextButton.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            Fragment currentFragment = pagerAdapter.getFragment(currentItem);
            boolean isValid = true;

            if (currentFragment instanceof RegisterPersonalInfoFragment) {
                isValid = ((RegisterPersonalInfoFragment) currentFragment).areAllInputsValid();
            } else if (currentFragment instanceof RegisterPhysicalInfoFragment) {
                isValid = ((RegisterPhysicalInfoFragment) currentFragment).areAllInputsValid();
            } else if (currentFragment instanceof RegisterAccountDetailsFragment) {
                // La validación de RegisterAccountDetailsFragment se hace en collectDataAndRegister
                // o podría tener su propio método areAllInputsValid() si se prefiere
                // Por ahora, su validación es parte del flujo de collectDataAndRegister.
            }

            if (!isValid) {
                 Toast.makeText(this, "Por favor, completa todos los campos requeridos correctamente.", Toast.LENGTH_SHORT).show();
                return; // No avanzar si no es válido
            }
            
            if (currentItem < NUM_PAGES - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                // Última página - Botón "¡Comenzar mi Journey!" presionado
                collectDataAndRegister();
            }
        });

        loginHereTextView.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void collectDataAndRegister() {
        String name = "", surname = "", gender = "", mainGoal = "", email = "", password = "";
        int age = 0, height = 0;
        double weight = 0.0;

        // --- Recolectar datos del Fragmento de Información Personal ---
        Fragment personalInfoFragmentInstance = pagerAdapter.getFragment(0);
        if (personalInfoFragmentInstance instanceof RegisterPersonalInfoFragment) {
            RegisterPersonalInfoFragment personalFrag = (RegisterPersonalInfoFragment) personalInfoFragmentInstance;
            if (!personalFrag.areAllInputsValid()) {
                Toast.makeText(this, "Revisa tu información personal.", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(0);
                return;
            }
            name = personalFrag.getNameInput();
            surname = personalFrag.getSurnameInput();
            gender = personalFrag.getGenderInput();
            try {
                String ageStr = personalFrag.getAgeInput();
                if (!TextUtils.isEmpty(ageStr)) age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error al parsear edad", e);
                Toast.makeText(this, "Edad inválida.", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(0);
                return;
            }
        } else {
            Log.e(TAG, "Error: No se pudo obtener la instancia de RegisterPersonalInfoFragment");
            return;
        }

        // --- Recolectar datos del Fragmento de Información Física ---
        Fragment physicalInfoFragmentInstance = pagerAdapter.getFragment(1);
        if (physicalInfoFragmentInstance instanceof RegisterPhysicalInfoFragment) {
            RegisterPhysicalInfoFragment physicalFrag = (RegisterPhysicalInfoFragment) physicalInfoFragmentInstance;
            if (!physicalFrag.areAllInputsValid()) {
                Toast.makeText(this, "Revisa tu información física.", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(1);
                return;
            }
            mainGoal = physicalFrag.getMainGoalInput();
            try {
                String weightStr = physicalFrag.getWeightInput();
                if (!TextUtils.isEmpty(weightStr)) weight = Double.parseDouble(weightStr);
                String heightStr = physicalFrag.getHeightInput();
                if (!TextUtils.isEmpty(heightStr)) height = Integer.parseInt(heightStr);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error al parsear peso o altura", e);
                Toast.makeText(this, "Peso o altura inválidos.", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(1);
                return;
            }
        } else {
            Log.e(TAG, "Error: No se pudo obtener la instancia de RegisterPhysicalInfoFragment");
            return;
        }

        // --- Recolectar datos del Fragmento de Detalles de Cuenta ---
        Fragment accountDetailsFragmentInstance = pagerAdapter.getFragment(2);
        if (accountDetailsFragmentInstance instanceof RegisterAccountDetailsFragment) {
            RegisterAccountDetailsFragment accountFrag = (RegisterAccountDetailsFragment) accountDetailsFragmentInstance;
            if (!accountFrag.isEmailValid() || !accountFrag.isPasswordValid()) { // Asumiendo que isPasswordValid revisa confirmación
                Toast.makeText(this, "Revisa los detalles de tu cuenta.", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(2);
                return;
            }
            email = accountFrag.getEmailInput();
            password = accountFrag.getPasswordInput();
        } else {
            Log.e(TAG, "Error: No se pudo obtener la instancia de RegisterAccountDetailsFragment");
            return;
        }

        // Validar que los campos obligatorios principales no estén vacíos (defensa adicional)
        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || gender.isEmpty() || mainGoal.isEmpty() || age == 0 || weight == 0.0 || height == 0) {
            Toast.makeText(this, "Faltan campos obligatorios. Por favor, revisa todos los pasos.", Toast.LENGTH_LONG).show();
            // Podrías intentar llevar al usuario al primer fragmento con error
            return;
        }

        String hashedPassword = PasswordUtils.hashPassword(password);
        if (hashedPassword == null) {
            Toast.makeText(this, "Error al procesar la contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(name, surname, email, hashedPassword, age, gender, weight, height, mainGoal);

        final String finalEmail = email;
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User existingUser = userDao.getUserByEmail(finalEmail);
            if (existingUser != null) {
                new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(getApplicationContext(), "Este correo electrónico ya está registrado.", Toast.LENGTH_LONG).show());
            } else {
                try {
                    userDao.registerUser(newUser); // Asumiendo que tienes un método insert en UserDao
                    User registeredUser = userDao.getUserByEmail(finalEmail);

                    SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (registeredUser != null) {
                        editor.putInt(KEY_USER_ID, registeredUser.getId());
                    }
                    editor.putBoolean(KEY_IS_LOGGED_IN, true);
                    editor.apply();

                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(getApplicationContext(), "¡Registro Exitoso!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error durante el registro en DB", e);
                    new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(getApplicationContext(), "Error durante el registro. Inténtalo de nuevo.", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void updateButtonVisibility(int position) {
        if (position == 0) {
            previousButton.setVisibility(View.INVISIBLE);
            nextButton.setText(R.string.button_next);
        } else if (position == NUM_PAGES - 1) {
            previousButton.setVisibility(View.VISIBLE);
            nextButton.setText(R.string.button_start_journey);
        } else {
            previousButton.setVisibility(View.VISIBLE);
            nextButton.setText(R.string.button_next);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (viewPager.getCurrentItem() == 0) {
            super.onSupportNavigateUp();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }
}

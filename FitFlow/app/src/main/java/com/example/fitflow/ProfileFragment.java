package com.example.fitflow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout; // Added for displayDataLayout
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.Arrays;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // Display Views
    private ConstraintLayout displayDataLayout;
    private MaterialTextView profileAvatarTextView;
    private TextView profileNameTextView;
    private TextView profileEmailTextView;
    private TextView statYearsValue, statYearsLabel;
    private TextView statWeightValue, statWeightLabel;
    private TextView statHeightValue, statHeightLabel;
    private TextView statObjectiveValue, statObjectiveLabel;
    // TODO: Consider adding TextViews for Gender if it's to be displayed in display mode stats

    // Edit Views
    private LinearLayout editDataLayout;
    private TextInputLayout editProfileNameLayout, editProfileSurnameLayout, editProfileEmailLayout, editProfileAgeLayout, editProfileWeightLayout, editProfileHeightLayout;
    private TextInputEditText editProfileNameEditText, editProfileSurnameEditText, editProfileEmailEditText, editProfileAgeEditText, editProfileWeightEditText, editProfileHeightEditText;
    private Spinner editProfileGenderSpinner, editProfileObjectiveSpinner;

    // Action Buttons
    private MaterialButton editProfileButton;
    private LinearLayout editActionsLayout;
    private MaterialButton saveChangesButton;
    private MaterialButton cancelEditButton;

    // Other existing views
    private LinearLayout optionSettings, optionDetailedStats, optionPersonalGoals, optionHelpSupport;
    private MaterialButton logoutButtonProfile;

    private UserDao userDao;
    private User currentUser;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
        if (getActivity() != null) {
            AppDatabase db = AppDatabase.getDatabase(getActivity().getApplicationContext());
            userDao = db.userDao();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Configuración de la opción 'Configuración' en el menú de perfil
        View optionSettings = view.findViewById(R.id.optionSettings);
        TextView optionSettingsText = optionSettings.findViewById(R.id.profileItemTextView);
        optionSettingsText.setText("Configuración");
        optionSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated. IsAdded: " + isAdded());

        initializeDisplayViews(view);
        initializeEditViews(view);
        initializeActionButtons(view);
        initializeOptionViews(view);

        populateSpinners();
        loadUserProfileData();
        setupClickListeners();
        setEditMode(false); // Start in display mode
    }

    private void initializeDisplayViews(@NonNull View view) {
        displayDataLayout = view.findViewById(R.id.displayDataLayout);
        profileAvatarTextView = view.findViewById(R.id.profileAvatarTextView);
        profileNameTextView = view.findViewById(R.id.profileNameTextView);
        profileEmailTextView = view.findViewById(R.id.profileEmailTextView);

        View statYearsView = view.findViewById(R.id.statYears);
        statYearsValue = statYearsView.findViewById(R.id.statItemValueTextView);
        statYearsLabel = statYearsView.findViewById(R.id.statItemLabelTextView);

        View statWeightView = view.findViewById(R.id.statWeight);
        statWeightValue = statWeightView.findViewById(R.id.statItemValueTextView);
        statWeightLabel = statWeightView.findViewById(R.id.statItemLabelTextView);

        View statHeightView = view.findViewById(R.id.statHeight);
        statHeightValue = statHeightView.findViewById(R.id.statItemValueTextView);
        statHeightLabel = statHeightView.findViewById(R.id.statItemLabelTextView);

        View statObjectiveView = view.findViewById(R.id.statObjective);
        statObjectiveValue = statObjectiveView.findViewById(R.id.statItemValueTextView);
        statObjectiveLabel = statObjectiveView.findViewById(R.id.statItemLabelTextView);
    }

    private void initializeEditViews(@NonNull View view) {
        editDataLayout = view.findViewById(R.id.editDataLayout);
        editProfileNameLayout = view.findViewById(R.id.editProfileNameLayout);
        editProfileNameEditText = view.findViewById(R.id.editProfileNameEditText);
        editProfileSurnameLayout = view.findViewById(R.id.editProfileSurnameLayout);
        editProfileSurnameEditText = view.findViewById(R.id.editProfileSurnameEditText);
        editProfileEmailLayout = view.findViewById(R.id.editProfileEmailLayout);
        editProfileEmailEditText = view.findViewById(R.id.editProfileEmailEditText);
        editProfileAgeLayout = view.findViewById(R.id.editProfileAgeLayout);
        editProfileAgeEditText = view.findViewById(R.id.editProfileAgeEditText);
        editProfileWeightLayout = view.findViewById(R.id.editProfileWeightLayout);
        editProfileWeightEditText = view.findViewById(R.id.editProfileWeightEditText);
        editProfileHeightLayout = view.findViewById(R.id.editProfileHeightLayout);
        editProfileHeightEditText = view.findViewById(R.id.editProfileHeightEditText);
        editProfileGenderSpinner = view.findViewById(R.id.editProfileGenderSpinner);
        editProfileObjectiveSpinner = view.findViewById(R.id.editProfileObjectiveSpinner);
    }

    private void initializeActionButtons(@NonNull View view) {
        editProfileButton = view.findViewById(R.id.editProfileButton);
        editActionsLayout = view.findViewById(R.id.editActionsLayout);
        saveChangesButton = view.findViewById(R.id.saveChangesButton);
        cancelEditButton = view.findViewById(R.id.cancelEditButton);
    }
    
    private void initializeOptionViews(@NonNull View view){
        optionSettings = view.findViewById(R.id.optionSettings);
        optionDetailedStats = view.findViewById(R.id.optionDetailedStats);
        optionPersonalGoals = view.findViewById(R.id.optionPersonalGoals);
        optionHelpSupport = view.findViewById(R.id.optionHelpSupport);
        logoutButtonProfile = view.findViewById(R.id.logoutButtonProfile);        
    }

    private void populateSpinners() {
        if (getContext() == null) return;
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.generos, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editProfileGenderSpinner.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> objectiveAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.objetivos, android.R.layout.simple_spinner_item);
        objectiveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editProfileObjectiveSpinner.setAdapter(objectiveAdapter);
    }

    private void loadUserProfileData() {
        if (getActivity() == null || userDao == null) {
            setPlaceholdersInDisplayMode();
            return;
        }
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(RegisterActivity.SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(RegisterActivity.KEY_USER_ID, -1);

        if (userId != -1) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                currentUser = userDao.getUserById(userId);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded() && getActivity() != null && currentUser != null) {
                        populateDisplayData(currentUser);
                        populateEditData(currentUser);
                    } else {
                        setPlaceholdersInDisplayMode();
                    }
                });
            });
        } else {
            setPlaceholdersInDisplayMode();
        }
        populateStaticLabels(); // Static labels for stats
    }

    private void populateDisplayData(User user) {
        profileNameTextView.setText(String.format("%s %s", user.getName(), user.getSurname()).trim());
        profileEmailTextView.setText(user.getEmail());
        updateAvatar(user.getName(), user.getSurname());

        statYearsValue.setText(String.valueOf(user.getAge()));
        statWeightValue.setText(String.format("%.1fkg", user.getWeight()));
        statHeightValue.setText(String.format("%dcm", user.getHeight()));
        statObjectiveValue.setText(user.getMainGoal());
        // TODO: Populate gender in display mode if a view for it exists
    }

    private void populateEditData(User user) {
        editProfileNameEditText.setText(user.getName());
        editProfileSurnameEditText.setText(user.getSurname());
        editProfileEmailEditText.setText(user.getEmail()); // Consider if email should be editable
        editProfileAgeEditText.setText(String.valueOf(user.getAge()));
        editProfileWeightEditText.setText(String.valueOf(user.getWeight()));
        editProfileHeightEditText.setText(String.valueOf(user.getHeight()));

        setSpinnerSelection(editProfileGenderSpinner, user.getGender(), R.array.generos);
        setSpinnerSelection(editProfileObjectiveSpinner, user.getMainGoal(), R.array.objetivos);
    }

    private void updateAvatar(String firstName, String lastName) {
        if (profileAvatarTextView == null || getContext() == null) return;
        StringBuilder initials = new StringBuilder();
        if (!TextUtils.isEmpty(firstName)) {
            initials.append(firstName.charAt(0));
        }
        if (!TextUtils.isEmpty(lastName)) {
            initials.append(lastName.charAt(0));
        } else if (TextUtils.isEmpty(firstName) && initials.length() == 0) {
             initials.append(getString(R.string.profile_avatar_placeholder_initials).charAt(0)); // Default if both empty
        }
        profileAvatarTextView.setText(initials.toString().toUpperCase());
    }

    private void setPlaceholdersInDisplayMode() {
        if (getContext() == null) return; // Guard against null context for getString
        profileNameTextView.setText(getString(R.string.profile_user_name_placeholder));
        profileEmailTextView.setText(getString(R.string.profile_user_email_placeholder));
        updateAvatar(null, null); // Will set default initials
        statYearsValue.setText(getString(R.string.profile_placeholder_years));
        statWeightValue.setText(getString(R.string.profile_placeholder_weight_card));
        statHeightValue.setText(getString(R.string.profile_placeholder_height_card));
        statObjectiveValue.setText(getString(R.string.profile_placeholder_objective_text));
    }
    
    private void populateStaticLabels(){
        if (getContext() == null) return;
        statYearsLabel.setText(getString(R.string.profile_label_years));
        statWeightLabel.setText(getString(R.string.profile_label_weight_card));
        statHeightLabel.setText(getString(R.string.profile_label_height_card));
        statObjectiveLabel.setText(getString(R.string.profile_label_objective_card));
        // Labels for non-editable stats like active days, achievements if they exist
        // View statActiveDaysView = getView().findViewById(R.id.statActiveDays); ... etc.
        
        // Option item labels
        View optionSettingsView = getView().findViewById(R.id.optionSettings);
        if (optionSettingsView != null) ((TextView)optionSettingsView.findViewById(R.id.profileItemTextView)).setText(getString(R.string.profile_option_settings));
        View optionDetailedStatsView = getView().findViewById(R.id.optionDetailedStats);
        if (optionDetailedStatsView != null) ((TextView)optionDetailedStatsView.findViewById(R.id.profileItemTextView)).setText(getString(R.string.profile_option_detailed_stats));
        View optionPersonalGoalsView = getView().findViewById(R.id.optionPersonalGoals);
        if (optionPersonalGoalsView != null) ((TextView)optionPersonalGoalsView.findViewById(R.id.profileItemTextView)).setText(getString(R.string.profile_option_personal_goals));
        View optionHelpSupportView = getView().findViewById(R.id.optionHelpSupport);
        if (optionHelpSupportView != null) ((TextView)optionHelpSupportView.findViewById(R.id.profileItemTextView)).setText(getString(R.string.profile_option_help_support));
    }

    private void setEditMode(boolean isEditing) {
        if (isEditing) {
            displayDataLayout.setVisibility(View.GONE);
            editDataLayout.setVisibility(View.VISIBLE);
            editProfileButton.setVisibility(View.GONE);
            editActionsLayout.setVisibility(View.VISIBLE);
            if (currentUser != null) populateEditData(currentUser); // Ensure edit fields have current data
        } else {
            displayDataLayout.setVisibility(View.VISIBLE);
            editDataLayout.setVisibility(View.GONE);
            editProfileButton.setVisibility(View.VISIBLE);
            editActionsLayout.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> setEditMode(true));
        saveChangesButton.setOnClickListener(v -> saveProfileChanges());
        cancelEditButton.setOnClickListener(v -> {
            setEditMode(false);
            // Optionally, re-populate edit fields from currentUser if changes were made but not saved
            if (currentUser != null) populateEditData(currentUser);
        });

        // Existing option listeners
        if (optionSettings != null) optionSettings.setOnClickListener(v -> Toast.makeText(getContext(), getString(R.string.profile_option_settings) + " clicked", Toast.LENGTH_SHORT).show());
        if (optionDetailedStats != null) optionDetailedStats.setOnClickListener(v -> Toast.makeText(getContext(), getString(R.string.profile_option_detailed_stats) + " clicked", Toast.LENGTH_SHORT).show());
        if (optionPersonalGoals != null) optionPersonalGoals.setOnClickListener(v -> Toast.makeText(getContext(), getString(R.string.profile_option_personal_goals) + " clicked", Toast.LENGTH_SHORT).show());
        if (optionHelpSupport != null) optionHelpSupport.setOnClickListener(v -> Toast.makeText(getContext(), getString(R.string.profile_option_help_support) + " clicked", Toast.LENGTH_SHORT).show());

        logoutButtonProfile.setOnClickListener(v -> {
            if (getActivity() == null) return;
            SharedPreferences prefs = getActivity().getSharedPreferences(RegisterActivity.SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(RegisterActivity.KEY_IS_LOGGED_IN, false);
            editor.remove(RegisterActivity.KEY_USER_ID);
            editor.apply();
            Toast.makeText(getContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finishAffinity();
        });
    }

    private void saveProfileChanges() {
        if (currentUser == null || !validateInputs()) {
            Toast.makeText(getContext(), "Por favor, corrige los errores.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update currentUser object with new values
        currentUser.setName(editProfileNameEditText.getText().toString().trim());
        currentUser.setSurname(editProfileSurnameEditText.getText().toString().trim());
        // currentUser.setEmail(editProfileEmailEditText.getText().toString().trim()); // Consider if email should be updatable
        currentUser.setAge(Integer.parseInt(editProfileAgeEditText.getText().toString().trim()));
        currentUser.setWeight(Double.parseDouble(editProfileWeightEditText.getText().toString().trim()));
        currentUser.setHeight(Integer.parseInt(editProfileHeightEditText.getText().toString().trim()));
        if (editProfileGenderSpinner.getSelectedItemPosition() > 0) {
            currentUser.setGender(editProfileGenderSpinner.getSelectedItem().toString());
        } else {
             currentUser.setGender(""); // Or handle as per your logic for unselected state
        }
        if (editProfileObjectiveSpinner.getSelectedItemPosition() > 0) {
            currentUser.setMainGoal(editProfileObjectiveSpinner.getSelectedItem().toString());
        } else {
            currentUser.setMainGoal(""); // Or handle
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateUser(currentUser); // Assumes updateUser method exists in UserDao
            new Handler(Looper.getMainLooper()).post(() -> {
                Toast.makeText(getContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                loadUserProfileData(); // Reload data to reflect changes in display mode
                setEditMode(false);
            });
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (TextUtils.isEmpty(editProfileNameEditText.getText())) {
            editProfileNameLayout.setError("El nombre no puede estar vacío");
            isValid = false;
        } else {
            editProfileNameLayout.setError(null);
        }

        if (TextUtils.isEmpty(editProfileSurnameEditText.getText())) {
            editProfileSurnameLayout.setError("El apellido no puede estar vacío");
            isValid = false;
        } else {
            editProfileSurnameLayout.setError(null);
        }
        
        // Basic email validation, you might want a more robust one
        String email = editProfileEmailEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editProfileEmailLayout.setError("Ingresa un correo válido");
            isValid = false;
        } else {
            editProfileEmailLayout.setError(null);
        }

        try {
            int age = Integer.parseInt(editProfileAgeEditText.getText().toString().trim());
            if (age <= 0 || age > 120) {
                editProfileAgeLayout.setError("Edad inválida");
                isValid = false;
            } else {
                editProfileAgeLayout.setError(null);
            }
        } catch (NumberFormatException e) {
            editProfileAgeLayout.setError("Ingresa un número para la edad");
            isValid = false;
        }

        try {
            double weight = Double.parseDouble(editProfileWeightEditText.getText().toString().trim());
            if (weight <= 0 || weight > 500) {
                editProfileWeightLayout.setError("Peso inválido");
                isValid = false;
            } else {
                editProfileWeightLayout.setError(null);
            }
        } catch (NumberFormatException e) {
            editProfileWeightLayout.setError("Ingresa un número para el peso");
            isValid = false;
        }

        try {
            int height = Integer.parseInt(editProfileHeightEditText.getText().toString().trim());
            if (height <= 0 || height > 300) {
                editProfileHeightLayout.setError("Altura inválida");
                isValid = false;
            } else {
                editProfileHeightLayout.setError(null);
            }
        } catch (NumberFormatException e) {
            editProfileHeightLayout.setError("Ingresa un número para la altura");
            isValid = false;
        }

        if (editProfileGenderSpinner.getSelectedItemPosition() == 0) {
            // You might want to show an error on the spinner itself (e.g., via a TextView)
            Toast.makeText(getContext(), "Selecciona un género", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (editProfileObjectiveSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Selecciona un objetivo principal", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void setSpinnerSelection(Spinner spinner, String value, int arrayResourceId) {
        if (getContext() == null || TextUtils.isEmpty(value) || spinner.getAdapter() == null) return;
        String[] array = getResources().getStringArray(arrayResourceId);
        int position = -1;
        for(int i=0; i<array.length; i++) {
            if(array[i].equalsIgnoreCase(value)) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            spinner.setSelection(position);
        } else {
            spinner.setSelection(0); // Default to prompt if value not found
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        displayDataLayout = null;
        profileAvatarTextView = null;
        profileNameTextView = null;
        profileEmailTextView = null;
        statYearsValue = null; statYearsLabel = null;
        statWeightValue = null; statWeightLabel = null;
        statHeightValue = null; statHeightLabel = null;
        statObjectiveValue = null; statObjectiveLabel = null;
        
        editDataLayout = null;
        editProfileNameLayout = null; editProfileNameEditText = null;
        editProfileSurnameLayout = null; editProfileSurnameEditText = null;
        editProfileEmailLayout = null; editProfileEmailEditText = null;
        editProfileAgeLayout = null; editProfileAgeEditText = null;
        editProfileWeightLayout = null; editProfileWeightEditText = null;
        editProfileHeightLayout = null; editProfileHeightEditText = null;
        editProfileGenderSpinner = null; editProfileObjectiveSpinner = null;

        editProfileButton = null;
        editActionsLayout = null;
        saveChangesButton = null;
        cancelEditButton = null;

        optionSettings = null; optionDetailedStats = null; optionPersonalGoals = null; optionHelpSupport = null;
        logoutButtonProfile = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        userDao = null;
        currentUser = null;
    }
}

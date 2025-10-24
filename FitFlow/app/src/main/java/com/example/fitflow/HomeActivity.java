package com.example.fitflow;

import android.os.Bundle;
import android.view.View; // Added for View.VISIBLE/GONE

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;

public class HomeActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_nav_view);

        if (savedInstanceState == null) {
            // Load HomeFragment by default and hide the main toolbar
            loadFragment(new HomeFragment(), getString(R.string.title_home_nav), true);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";
            boolean hideToolbar = false;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
                title = getString(R.string.title_home_nav);
                hideToolbar = true; // HomeFragment has its own custom top bar
            } else if (itemId == R.id.navigation_routines) {
                selectedFragment = new RoutinesFragment();
                title = getString(R.string.title_routines_nav);
                // RoutinesFragment will use the main toolbar
            } else if (itemId == R.id.navigation_progress) {
                selectedFragment = new ProgressFragment(); 
                title = getString(R.string.title_progress_nav);
                // ProgressFragment will use the main toolbar, so hideToolbar remains false
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment(); // Changed from HomeFragment placeholder
                title = getString(R.string.title_profile_nav);
                // ProfileFragment will use the main toolbar, so hideToolbar remains false
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, title, hideToolbar);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment, String title, boolean hideMainToolbar) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment_activity_home, fragment);
        fragmentTransaction.commit();

        if (getSupportActionBar() != null) {
            if (hideMainToolbar) {
                toolbar.setVisibility(View.GONE);
            } else {
                toolbar.setVisibility(View.VISIBLE);
                getSupportActionBar().setTitle(title);
            }
        }
        // Invalidate options menu to allow fragments to add their own menu items
        // or to clear previous fragment's menu items.
        invalidateOptionsMenu(); 
    }

    public void navigateToTab(int menuItemId) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(menuItemId);
        }
    }

    // Removed showToast as it's no longer needed for placeholder navigation
}

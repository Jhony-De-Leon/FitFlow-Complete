package com.example.fitflow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RegisterViewPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 3; // We have 3 steps/fragments
    private Fragment[] fragments; // Array to hold fragment instances

    public RegisterViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        fragments = new Fragment[NUM_PAGES];
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new RegisterPersonalInfoFragment(); // Anteriormente RegisterNameFragment
                break;
            case 1:
                // Usaremos RegisterPhysicalInfoFragment para los objetivos por ahora, como en RegisterGoalsFragment
                fragment = new RegisterPhysicalInfoFragment(); // Anteriormente RegisterGoalsFragment
                break;
            case 2:
                fragment = new RegisterAccountDetailsFragment(); // Anteriormente RegisterCredentialsFragment
                break;
            default:
                // Esto no debería suceder si NUM_PAGES es correcto
                throw new IllegalStateException("Unexpected position: " + position);
        }
        fragments[position] = fragment; // Guardar la instancia
        return fragment;
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    /**
     * Devuelve el fragmento en la posición especificada.
     * Puede devolver null si el fragmento aún no ha sido creado o está fuera de los límites.
     */
    public Fragment getFragment(int position) {
        if (position >= 0 && position < fragments.length) {
            return fragments[position];
        }
        return null;
    }
}

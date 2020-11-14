package com.garethevans.church.opensongtablet.preferences;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsCategoriesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class SettingsCategories extends Fragment {

    SettingsCategoriesBinding myView;
    MainActivityInterface mainActivityInterface;
    Preferences preferences;

    @Override
    public void onAttach(@NonNull Context context) {
        mainActivityInterface = (MainActivityInterface) context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        myView = SettingsCategoriesBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(null,requireContext().getResources().getString(R.string.settings));

        // Prepare helpers
        prepareHelpers();

        // Hide the features not available to this device
        hideUnavailable();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareHelpers() {
        preferences = new Preferences();
    }

    private void hideUnavailable() {
        // If the user doesn't have Google API availability, they can't use the connect feature
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext()) != ConnectionResult.SUCCESS) {
            myView.connectButton.setVisibility(View.GONE);
            myView.connectLine.setVisibility(View.GONE);
        }
        // If they don't have midi functionality, remove this
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            myView.midiButton.setVisibility(View.GONE);
        }

    }
    private void setListeners() {
        myView.ccliButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.nav_preference_ccli));
        myView.storageButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.nav_storageManagement));
        myView.displayButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.displayMenuFragment));
        myView.connectButton.setOnClickListener(v -> {
            // Check we have the required permissions
            if (mainActivityInterface.requestNearbyPermissions()) {
                mainActivityInterface.navigateToFragment(R.id.nearbyConnectionsFragment);
            }
        });
        myView.midiButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.midiFragment));
        myView.aboutButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.aboutAppFragment));
        myView.gesturesButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.controlMenuFragment));
    }
}
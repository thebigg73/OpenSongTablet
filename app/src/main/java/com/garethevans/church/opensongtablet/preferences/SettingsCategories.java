package com.garethevans.church.opensongtablet.preferences;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsCategoriesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class SettingsCategories extends Fragment {

    private SettingsCategoriesBinding myView;
    private MainActivityInterface mainActivityInterface;

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
        mainActivityInterface.updateToolbar(null,getString(R.string.settings));

        // Hide the features not available to this device
        hideUnavailable();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }



    private void hideUnavailable() {
        // If the user doesn't have Google API availability, they can't use the connect feature
        setPlayEnabled(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext()) == ConnectionResult.SUCCESS);
        // If they don't have midi functionality, remove this
        setMidiEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI));
    }

    private void setPlayEnabled(boolean enabled) {
        myView.connectButton.setEnabled(enabled);
        myView.connectLine.setEnabled(enabled);
        if (!enabled) {
            ((TextView)myView.connectButton.findViewById(R.id.subText)).setText(getString(R.string.play_services_error));
        }
    }

    private void setMidiEnabled(boolean enabled) {
        String message;
        if (enabled) {
            message = getString(R.string.midi_description);
        } else {
            message = getString(R.string.not_available);
        }
        myView.midiButton.setEnabled(enabled);
        ((TextView)myView.midiButton.findViewById(R.id.subText)).setText(message);
    }

    private void setListeners() {
        myView.ccliButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.settingsCCLI));
        myView.storageButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.storage_graph));
        myView.displayButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.display_graph));
        myView.connectButton.setOnClickListener(v -> {
            // Check we have the required permissions
            if (mainActivityInterface.requestNearbyPermissions()) {
                mainActivityInterface.navigateToFragment(null,R.id.nearbyConnectionsFragment);
            }
        });
        myView.profilesButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.profileFragment));
        myView.midiButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.midiFragment));
        myView.aboutButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.about_graph));
        myView.gesturesButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.control_graph));
        myView.actionsButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.actions_graph));
    }

}
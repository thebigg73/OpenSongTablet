package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsNearbyconnectionsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.preferences.TextInputDialogFragment;

public class NearbyConnectionsFragment extends Fragment {

    SettingsNearbyconnectionsBinding myView;
    MainActivityInterface mainActivityInterface;
    NearbyConnections nearbyConnections;
    Preferences preferences;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        nearbyConnections = mainActivityInterface.getNearbyConnections(mainActivityInterface);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsNearbyconnectionsBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(null,getString(R.string.settings) + " / " + getString(R.string.connections_connect));

        // Set the helpers
        setHelpers();

        // Update the views
        updateViews();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setHelpers() {
        preferences = new Preferences();
        mainActivityInterface.registerFragment(this,"NearbyConnectionsFragment");
    }

    public void updateViews() {
        ((TextView) myView.deviceButton.findViewById(R.id.subText)).setText(nearbyConnections.getUserNickname());
        myView.actAsHost.setChecked(StaticVariables.isHost);
        myView.receiveHostFiles.setChecked(StaticVariables.receiveHostFiles);
        myView.keepHostFiles.setChecked(StaticVariables.keepHostFiles);
        myView.enableNearby.setChecked(StaticVariables.usingNearby);
        updateConnectionsLog();
    }

    public void updateConnectionsLog() {
        if (StaticVariables.connectionLog ==null) {
            StaticVariables.connectionLog = "";
        }
        ((TextView)myView.connectionsLog.findViewById(R.id.subText)).setText(StaticVariables.connectionLog);
    }

    public void setListeners() {
        myView.deviceButton.setOnClickListener(v -> textInputDialog());
        myView.enableNearby.setOnCheckedChangeListener((buttonView, isChecked) -> {
            StaticVariables.usingNearby = isChecked;
            if (isChecked) {
                if (StaticVariables.isHost) {
                    nearbyConnections.startAdvertising();
                } else {
                    nearbyConnections.startDiscovery();
                }
            } else {
                // Beacause the host button can be switched on/off as well, when turning off, run both
                // These will catch errors
                nearbyConnections.stopDiscovery();
                nearbyConnections.stopAdvertising();
                nearbyConnections.turnOffNearby();
            }
        });
        myView.keepHostFiles.setOnCheckedChangeListener((buttonView, isChecked) -> StaticVariables.keepHostFiles = isChecked);
        myView.receiveHostFiles.setOnCheckedChangeListener((buttonView, isChecked) -> StaticVariables.receiveHostFiles = isChecked);
        myView.actAsHost.setOnCheckedChangeListener((buttonView, isChecked) -> StaticVariables.isHost = isChecked);
        myView.connectionsLog.setOnClickListener(v -> {
            StaticVariables.connectionLog = "";
            updateConnectionsLog();
        });
    }

    private void textInputDialog() {
        TextInputDialogFragment dialogFragment = new TextInputDialogFragment(preferences, this,
                "NearbyConnectionsFragment", getString(R.string.device_name), getString(R.string.device_name),
                "deviceName", StaticVariables.deviceName);
        dialogFragment.show(requireActivity().getSupportFragmentManager(), "textInputFragment");
    }

    // Called from MainActivity after TextInputDialogFragment save
    public void updateValue(String which, String value) {
        if (which.equals("deviceName")) {
            ((TextView) myView.deviceButton.findViewById(R.id.subText)).setText(value);
            StaticVariables.deviceName = value;
        }
    }
}

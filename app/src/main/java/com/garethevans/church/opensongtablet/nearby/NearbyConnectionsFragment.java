package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsNearbyconnectionsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.NearbyReturnActionsInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;

public class NearbyConnectionsFragment extends Fragment {

    private SettingsNearbyconnectionsBinding myView;
    private MainActivityInterface mainActivityInterface;
    private NearbyReturnActionsInterface nearbyReturnActionsInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        nearbyReturnActionsInterface = (NearbyReturnActionsInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsNearbyconnectionsBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.connections_connect));

        // Set the helpers
        setHelpers();

        // Update the views
        updateViews();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setHelpers() {
        mainActivityInterface.registerFragment(this,"NearbyConnectionsFragment");
        mainActivityInterface.setNearbyOpen(true);
        mainActivityInterface.getNearbyConnections().setNearbyReturnActionsInterface(nearbyReturnActionsInterface);
    }

    public void updateViews() {
        // Set the device name
        myView.deviceButton.setHint(mainActivityInterface.getNearbyConnections().getUserNickname(requireContext(),mainActivityInterface));

        // Set the default values for off/host/client
        if (mainActivityInterface.getNearbyConnections().isHost) {
            myView.connectionsHost.setChecked(true);
            offHostClient(true,false);
        } else if (mainActivityInterface.getNearbyConnections().usingNearby) {
            myView.connectionsClient.setChecked(true);
            offHostClient(false,true);
        } else {
            myView.connectionsOff.setChecked(true);
            offHostClient(false,false);
        }

        // IV - Display relevant options to process nearby Song Section changes and autoscroll
        if (mainActivityInterface.getMode().equals("Presenter")) {
            // This will work in Stage and Perfomance Mode
            // As will sections (if using pdf pages)
            myView.receiveAutoscroll.setEnabled(false);
        }

        // Set the host switches
        myView.nearbyHostMenuOnly.setChecked(mainActivityInterface.getNearbyConnections().getNearbyHostMenuOnly());
        myView.receiveHostFiles.setChecked(mainActivityInterface.getNearbyConnections().getReceiveHostFiles());
        myView.keepHostFiles.setChecked(mainActivityInterface.getNearbyConnections().getKeepHostFiles());
        myView.receiveAutoscroll.setChecked(mainActivityInterface.getNearbyConnections().getReceiveHostAutoscroll());
        myView.receiveHostSections.setChecked(mainActivityInterface.getNearbyConnections().getReceiveHostSongSections());

        // Show any connection log
        updateConnectionsLog();
    }

    private void offHostClient(boolean host, boolean client) {
        if (host) {
            myView.hostOptions.setVisibility(View.VISIBLE);
        } else {
            myView.hostOptions.setVisibility(View.GONE);
        }
        if (client) {
            myView.clientOptions.setVisibility(View.VISIBLE);
        } else {
            myView.clientOptions.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface.setNearbyOpen(false);
    }

    public void updateConnectionsLog() {
        if (mainActivityInterface.getNearbyConnections().connectionLog ==null) {
            mainActivityInterface.getNearbyConnections().connectionLog = "";
        }
        myView.connectionsLog.setHint(mainActivityInterface.getNearbyConnections().connectionLog);
    }

    public void setListeners() {
        // The deviceId
        myView.deviceButton.setOnClickListener(v -> textInputDialog());

        // The client/host options
        myView.keepHostFiles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getNearbyConnections().keepHostFiles = isChecked;
            if (isChecked) {
                // IV - Re-connect to apply setting
                Handler h = new Handler();
                h.postDelayed(() -> myView.searchForHosts.performClick(), 2000);
            }
        });
        myView.receiveHostFiles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getNearbyConnections().receiveHostFiles = isChecked;
            // IV - When off turn keep off - user must make an active choice to 'keep' as it may overwrite local songs
            if (!isChecked) {
                myView.keepHostFiles.setChecked(false);
                mainActivityInterface.getNearbyConnections().setKeepHostFiles(false);
            }
            // IV - Re-connect to apply setting
            Handler h = new Handler();
            h.postDelayed(() -> myView.searchForHosts.performClick(),2000);
        });
        myView.nearbyHostMenuOnly.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getNearbyConnections().setNearbyHostMenuOnly(requireContext(),mainActivityInterface,isChecked));
        myView.receiveAutoscroll.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getNearbyConnections().setReceiveHostAutoscroll(isChecked));
        myView.receiveHostSections.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getNearbyConnections().setReceiveHostSongSections(isChecked));

        // Changing the nearby connection
        myView.connectionsOff.setOnCheckedChangeListener((radioButton, isChecked) -> {
            if (isChecked) {
                mainActivityInterface.getNearbyConnections().isHost = false;
                mainActivityInterface.getNearbyConnections().usingNearby = false;
                offHostClient(false,false);
                mainActivityInterface.getNearbyConnections().stopDiscovery(requireContext());
                mainActivityInterface.getNearbyConnections().stopAdvertising(requireContext());
                mainActivityInterface.getNearbyConnections().turnOffNearby(requireContext());
                myView.connectionsHost.setChecked(false);
                myView.connectionsClient.setChecked(false);
            }
        });
        myView.connectionsHost.setOnCheckedChangeListener((radioButton, isChecked) -> {
            if (isChecked) {
                mainActivityInterface.getNearbyConnections().isHost = true;
                mainActivityInterface.getNearbyConnections().usingNearby = true;
                offHostClient(true,false);
                mainActivityInterface.getNearbyConnections().stopDiscovery(requireContext());
                mainActivityInterface.getNearbyConnections().startAdvertising(requireContext(),mainActivityInterface);
                myView.connectionsOff.setChecked(false);
                myView.connectionsClient.setChecked(false);
            }
        });
        myView.connectionsClient.setOnCheckedChangeListener((radioButton, isChecked) -> {
            if (isChecked) {
                mainActivityInterface.getNearbyConnections().isHost = false;
                mainActivityInterface.getNearbyConnections().usingNearby = true;
                offHostClient(false,true);
                mainActivityInterface.getNearbyConnections().stopAdvertising(requireContext());
                // IV - Short delay to help stability
                Handler h = new Handler();
                h.postDelayed(() -> myView.searchForHosts.performClick(),2000);
                myView.connectionsOff.setChecked(false);
                myView.connectionsHost.setChecked(false);
            } else {
                // IV - Reset the client options when leaving client mode
                mainActivityInterface.getNearbyConnections().setReceiveHostFiles(false);
                mainActivityInterface.getNearbyConnections().setKeepHostFiles(false);
                mainActivityInterface.getNearbyConnections().setReceiveHostSongSections(true);
                mainActivityInterface.getNearbyConnections().setReceiveHostAutoscroll(true);
                myView.receiveHostFiles.setChecked(false);
                myView.keepHostFiles.setChecked(false);
                myView.receiveHostSections.setChecked(true);
                myView.receiveAutoscroll.setChecked(true);
            }
        });

        // Discover hosts
        myView.searchForHosts.setOnClickListener(b -> {
            // IV - User can cause problems by clicking quickly between modes!  Make sure we are still in client mode.
            if (!mainActivityInterface.getNearbyConnections().isHost) {
                // Start discovery and turn it off again after 10 seconds
                myView.searchForHosts.setEnabled(false);
                myView.searchForHosts.setText(getString(R.string.connections_searching));
                mainActivityInterface.getNearbyConnections().startDiscovery(requireContext(),mainActivityInterface);
                myView.searchForHosts.postDelayed(() -> {
                    try {
                        myView.searchForHosts.setText(getString(R.string.connections_discover));
                        myView.searchForHosts.setEnabled(true);
                    } catch (Exception e) {
                        Log.d("OptionMenu","Lost reference to discovery button");
                    }
                },10000);
            }
        });

        // Clear the log
        myView.connectionsLog.setOnClickListener(v -> {
            mainActivityInterface.getNearbyConnections().connectionLog = "";
            updateConnectionsLog();
        });
    }

    private void textInputDialog() {
        TextInputBottomSheet dialogFragment = new TextInputBottomSheet(this,
                "NearbyConnectionsFragment", getString(R.string.connections_device_name), getString(R.string.connections_device_name),null,
                "deviceId", mainActivityInterface.getNearbyConnections().deviceId,true);
        dialogFragment.show(requireActivity().getSupportFragmentManager(), "textInputFragment");
    }

    // Called from MainActivity after TextInputDialogFragment save
    public void updateValue(String which, String value) {
        if (which.equals("deviceName")) {
            myView.deviceButton.setHint(value);
            mainActivityInterface.getNearbyConnections().deviceId = value;
        }
    }

}

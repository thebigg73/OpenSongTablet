package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsNearbyconnectionsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;

public class NearbyConnectionsFragment extends Fragment {

    private SettingsNearbyconnectionsBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final String TAG = "NearbyConnectionsFrag";
    private ColorStateList onColor, offColor;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private boolean advancedShown;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.getNearbyConnections().setConnectionsOpen(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsNearbyconnectionsBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.connections_connect));

        onColor = ColorStateList.valueOf(getResources().getColor(R.color.colorSecondary));
        offColor = ColorStateList.valueOf(getResources().getColor(R.color.colorAltPrimary));

        // Set the helpers
        setHelpers();

        // Update the views
        updateViews();

        // Set up the bottom sheet
        bottomSheetBar();

        // Set the listeners
        setListeners();

        // Run showcase
        showcase();

        return myView.getRoot();
    }

    private void setHelpers() {
        mainActivityInterface.registerFragment(this, "NearbyConnectionsFragment");
    }

    public void updateViews() {
        // Set the device name
        myView.deviceButton.setHint(mainActivityInterface.getNearbyConnections().getUserNickname());

        // Set the chosen strategy
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyStrategyCluster", true)) {
            myView.bottomSheet.clusterMode.setBackgroundTintList(onColor);
            myView.bottomSheet.starMode.setBackgroundTintList(offColor);
            mainActivityInterface.getNearbyConnections().setNearbyStrategy(Strategy.P2P_CLUSTER,false);
        } else {
            myView.bottomSheet.starMode.setBackgroundTintList(onColor);
            myView.bottomSheet.clusterMode.setBackgroundTintList(offColor);
            mainActivityInterface.getNearbyConnections().setNearbyStrategy(Strategy.P2P_STAR,false);
        }

        // Set the default values for off/host/client
        updateOffHostClient(mainActivityInterface.getNearbyConnections().isHost,
                mainActivityInterface.getNearbyConnections().usingNearby);

        // IV - Display relevant options to process nearby Song Section changes and autoscroll
        if (mainActivityInterface.getMode().equals("Presenter")) {
            // This will work in Stage and Perfomance Mode
            // As will sections (if using pdf pages)
            myView.bottomSheet.receiveAutoscroll.setEnabled(false);
        }

        // Set the host switches
        myView.bottomSheet.nearbyHostMenuOnly.setChecked(mainActivityInterface.getNearbyConnections().getNearbyHostMenuOnly());
        myView.bottomSheet.receiveHostFiles.setChecked(mainActivityInterface.getNearbyConnections().getReceiveHostFiles());
        myView.bottomSheet.keepHostFiles.setChecked(mainActivityInterface.getNearbyConnections().getKeepHostFiles());
        myView.bottomSheet.receiveAutoscroll.setChecked(mainActivityInterface.getNearbyConnections().getReceiveHostAutoscroll());
        myView.bottomSheet.receiveHostSections.setChecked(mainActivityInterface.getNearbyConnections().getReceiveHostSongSections());

        // Show any connection log
        updateConnectionsLog();
    }

    private void showcase() {
        ArrayList<View> targets = new ArrayList<>();
        targets.add(myView.deviceButton);
        targets.add(myView.off);
        targets.add(myView.host);
        targets.add(myView.client);
        targets.add(myView.bottomSheet.bottomSheetTab);
        ArrayList<String> dismisses = new ArrayList<>();
        dismisses.add(null);
        dismisses.add(null);
        dismisses.add(null);
        dismisses.add(null);
        dismisses.add(null);
        ArrayList<String> infos = new ArrayList<>();
        infos.add(getString(R.string.connections_device_name)+"\n"+getString(R.string.click_to_change));
        infos.add(getString(R.string.connections_off));
        infos.add(getString(R.string.connections_actashost_info));
        infos.add(getString(R.string.connections_actasclient_info));
        infos.add(getString(R.string.connections_advanced));
        ArrayList<Boolean> rects = new ArrayList<>();
        rects.add(true);
        rects.add(true);
        rects.add(true);
        rects.add(true);
        rects.add(true);
        mainActivityInterface.getShowCase().sequenceShowCase(requireActivity(),
                targets,dismisses,infos,rects,"connectionsShowCase");
    }

    private void bottomSheetBar() {
        bottomSheetBehavior = BottomSheetBehavior.from(myView.bottomSheet.bottomSheet);
        bottomSheetBehavior.setHideable(false);
        myView.bottomSheet.bottomSheetTab.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bottomSheetBehavior.setPeekHeight(myView.bottomSheet.bottomSheetTab.getMeasuredHeight());
                myView.bottomSheet.bottomSheetTab.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        //bottomSheetBehavior.setGestureInsetBottomIgnored(true);

        myView.bottomSheet.bottomSheetTab.setOnClickListener(v -> {
            advancedShown = !advancedShown;
            if (advancedShown) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        myView.dimBackground.setVisibility(View.GONE);
                        advancedShown = false;
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                myView.dimBackground.setVisibility(View.VISIBLE);
            }
        });
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setSkipCollapsed(true);
    }

    private void updateOffHostClient(boolean isHost, boolean isClient) {
        // Turn all off
        myView.off.setBackgroundTintList(offColor);
        myView.host.setBackgroundTintList(offColor);
        myView.client.setBackgroundTintList(offColor);
        myView.bottomSheet.hostOptions.setVisibility(View.GONE);
        myView.bottomSheet.clientOptions.setVisibility(View.GONE);
        myView.connectedToLayout.setVisibility(View.GONE);
        myView.connectInitiateButton.setVisibility(View.GONE);

        if (isHost) {
            myView.host.setBackgroundTintList(onColor);
            myView.bottomSheet.hostOptions.setVisibility(View.VISIBLE);
            myView.connectedTo.setHint(mainActivityInterface.getNearbyConnections().getConnectedDevicesAsString());
            myView.connectedToLayout.setVisibility(View.VISIBLE);
            myView.connectInitiateButton.setVisibility(View.VISIBLE);

        } else if (isClient) {
            myView.client.setBackgroundTintList(onColor);
            myView.bottomSheet.clientOptions.setVisibility(View.VISIBLE);
            myView.connectedTo.setHint(mainActivityInterface.getNearbyConnections().getConnectedDevicesAsString());
            myView.connectedToLayout.setVisibility(View.VISIBLE);
            myView.connectInitiateButton.setVisibility(View.VISIBLE);

        } else {
            myView.off.setBackgroundTintList(onColor);
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface.getNearbyConnections().setConnectionsOpen(false);
    }

    public void updateConnectionsLog() {
        if (mainActivityInterface.getNearbyConnections().connectionLog == null) {
            mainActivityInterface.getNearbyConnections().connectionLog = "";
        }
        myView.bottomSheet.connectionsLog.setHint(mainActivityInterface.getNearbyConnections().connectionLog);
        myView.connectedTo.setHint(mainActivityInterface.getNearbyConnections().getConnectedDevicesAsString());
    }

    public void setListeners() {
        // The deviceId
        myView.deviceButton.setOnClickListener(v -> textInputDialog());

        // The nearby strategy mode
        myView.bottomSheet.clusterMode.setOnClickListener(v -> {
            myView.bottomSheet.clusterMode.setBackgroundTintList(onColor);
            myView.bottomSheet.starMode.setBackgroundTintList(offColor);
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyStrategyCluster", true);
            mainActivityInterface.getNearbyConnections().setNearbyStrategy(Strategy.P2P_CLUSTER,true);
        });
        myView.bottomSheet.starMode.setOnClickListener(v -> {
            myView.bottomSheet.starMode.setBackgroundTintList(onColor);
            myView.bottomSheet.clusterMode.setBackgroundTintList(offColor);
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyStrategyCluster", false);
            mainActivityInterface.getNearbyConnections().setNearbyStrategy(Strategy.P2P_STAR,true);
        });

        // The client/host options
        myView.bottomSheet.keepHostFiles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getNearbyConnections().keepHostFiles = isChecked;
            if (isChecked) {
                // IV - Re-connect to apply setting
                Handler h = new Handler();
                h.postDelayed(() -> myView.connectInitiateButton.performClick(), 2000);
            }
        });
        myView.bottomSheet.receiveHostFiles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getNearbyConnections().receiveHostFiles = isChecked;
            // IV - When off turn keep off - user must make an active choice to 'keep' as it may overwrite local songs
            if (!isChecked) {
                myView.bottomSheet.keepHostFiles.setChecked(false);
                mainActivityInterface.getNearbyConnections().setKeepHostFiles(false);
            }
            // IV - Re-connect to apply setting
            Handler h = new Handler();
            h.postDelayed(() -> myView.connectInitiateButton.performClick(), 2000);
        });
        myView.bottomSheet.nearbyHostMenuOnly.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getNearbyConnections().setNearbyHostMenuOnly(isChecked));
        myView.bottomSheet.receiveAutoscroll.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getNearbyConnections().setReceiveHostAutoscroll(isChecked));
        myView.bottomSheet.receiveHostSections.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getNearbyConnections().setReceiveHostSongSections(isChecked));

        // Changing the nearby connection
        myView.off.setOnClickListener(v -> {
            updateOffHostClient(false, false);
            mainActivityInterface.getNearbyConnections().isHost = false;
            mainActivityInterface.getNearbyConnections().usingNearby = false;
            mainActivityInterface.getNearbyConnections().stopDiscovery();
            mainActivityInterface.getNearbyConnections().stopAdvertising();
            mainActivityInterface.getNearbyConnections().turnOffNearby();
            myView.connectedToLayout.setVisibility(View.GONE);
            resetClientOptions();
            myView.connectInitiateButton.setVisibility(View.GONE);
        });
        myView.host.setOnClickListener(v -> {
            updateOffHostClient(true, false);
            mainActivityInterface.getNearbyConnections().isHost = true;
            mainActivityInterface.getNearbyConnections().usingNearby = true;
            mainActivityInterface.getNearbyConnections().stopDiscovery();
            mainActivityInterface.getNearbyConnections().stopAdvertising();
            myView.connectedTo.setHint(mainActivityInterface.getNearbyConnections().getConnectedDevicesAsString());
            myView.connectedToLayout.setVisibility(View.VISIBLE);
            myView.connectInitiateButton.setText(getString(R.string.connections_advertise));
            myView.connectInitiateButton.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> myView.connectInitiateButton.performClick(), 1000);
            resetClientOptions();
        });
        myView.client.setOnClickListener(v -> {
            updateOffHostClient(false, true);
            mainActivityInterface.getNearbyConnections().isHost = false;
            mainActivityInterface.getNearbyConnections().usingNearby = true;
            mainActivityInterface.getNearbyConnections().stopAdvertising();
            mainActivityInterface.getNearbyConnections().stopDiscovery();
            myView.connectedTo.setHint(mainActivityInterface.getNearbyConnections().getConnectedDevicesAsString());
            myView.connectedToLayout.setVisibility(View.VISIBLE);

            // IV - Short delay to help stability
            myView.connectInitiateButton.setText(getString(R.string.connections_discover));
            myView.connectInitiateButton.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> myView.connectInitiateButton.performClick(), 1000);
        });

        // Discover hosts
        myView.connectInitiateButton.setOnClickListener(b -> {
            myView.connectInitiateButton.setEnabled(false);
            myView.connectInitiateButton.setAlpha(0.6f);
            long timeToEnable = 10000;
            // If we are in host mode
            if (mainActivityInterface.getNearbyConnections().isHost) {
                // Broadcast for 20s
                timeToEnable = 20000;
                myView.connectInitiateButton.setText(getString(R.string.connections_advertising));
                mainActivityInterface.getNearbyConnections().startAdvertising();
            } else if (mainActivityInterface.getNearbyConnections().usingNearby) {
                myView.connectInitiateButton.setText(getString(R.string.connections_searching));
                mainActivityInterface.getNearbyConnections().startDiscovery();
            }
            myView.connectInitiateButton.setVisibility(View.VISIBLE);

            myView.connectInitiateButton.postDelayed(() -> {
                try {
                    if (mainActivityInterface.getNearbyConnections().isHost && !mainActivityInterface.getNearbyConnections().isDiscovering) {
                        myView.connectInitiateButton.setText(getString(R.string.connections_advertise));
                        mainActivityInterface.getNearbyConnections().stopAdvertising();
                    } else if (mainActivityInterface.getNearbyConnections().usingNearby && !mainActivityInterface.getNearbyConnections().isAdvertising) {
                        myView.connectInitiateButton.setText(getString(R.string.connections_discover));
                        mainActivityInterface.getNearbyConnections().stopDiscovery();
                    }
                    myView.connectInitiateButton.setAlpha(1f);
                    myView.connectInitiateButton.setEnabled(true);
                } catch (Exception e) {
                    // Probably because we closed the Fragment, it's fine!
                    e.printStackTrace();
                }
            }, timeToEnable);
        });

        myView.dimBackground.setOnClickListener(v -> myView.bottomSheet.bottomSheetTab.performClick());

        // Clear the log
        myView.bottomSheet.connectionsLog.setOnClickListener(v -> {
            mainActivityInterface.getNearbyConnections().connectionLog = "";
            updateConnectionsLog();
        });
    }



    private void resetClientOptions() {
        // IV - Reset the client options when leaving client mode
        mainActivityInterface.getNearbyConnections().setReceiveHostFiles(false);
        mainActivityInterface.getNearbyConnections().setKeepHostFiles(false);
        mainActivityInterface.getNearbyConnections().setReceiveHostSongSections(true);
        mainActivityInterface.getNearbyConnections().setReceiveHostAutoscroll(true);
        myView.bottomSheet.receiveHostFiles.setChecked(false);
        myView.bottomSheet.keepHostFiles.setChecked(false);
        myView.bottomSheet.receiveHostSections.setChecked(true);
        myView.bottomSheet.receiveAutoscroll.setChecked(true);
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

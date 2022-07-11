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
import com.google.android.material.button.MaterialButton;

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
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_nearby));

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
        showcase1();

        return myView.getRoot();
    }

    private void setHelpers() {
        mainActivityInterface.registerFragment(this, "NearbyConnectionsFragment");
    }

    public void updateViews() {
        // Set the device name
        myView.deviceButton.setHint(mainActivityInterface.getNearbyConnections().getUserNickname());

        // Set the chosen strategy
        updateStrategyButtons();


        // Change the advertise/discover button colors
        myView.advertiseButton.setBackgroundTintList(offColor);
        myView.discoverButton.setBackgroundTintList(offColor);

        // Set the default values for off/host/client
        updateOffHostClient(mainActivityInterface.getNearbyConnections().getIsHost(),
                mainActivityInterface.getNearbyConnections().hasValidConnections());

        // IV - Display relevant options to process nearby Song Section changes and autoscroll
        if (mainActivityInterface.getMode().equals("Presenter")) {
            // This will work in Stage and Perfomance Mode
            // As will sections (if using pdf pages)
            myView.bottomSheet.receiveAutoscroll.setEnabled(false);
        }

        // Set the host/client switches
        myView.bottomSheet.nearbyHostMenuOnly.setChecked(mainActivityInterface.getNearbyConnections().getNearbyHostMenuOnly());
        myView.bottomSheet.hostPassthrough.setChecked(mainActivityInterface.getNearbyConnections().getNearbyHostPassthrough());
        myView.bottomSheet.receiveHostFiles.setChecked(mainActivityInterface.getNearbyConnections().getReceiveHostFiles());
        myView.bottomSheet.keepHostFiles.setChecked(mainActivityInterface.getNearbyConnections().getKeepHostFiles());
        myView.bottomSheet.receiveAutoscroll.setChecked(mainActivityInterface.getNearbyConnections().getReceiveHostAutoscroll());
        myView.bottomSheet.receiveHostSections.setChecked(mainActivityInterface.getNearbyConnections().getReceiveHostSongSections());

        // Show any connection log
        updateConnectionsLog();
    }

    private void updateStrategyButtons() {
        myView.bottomSheet.clusterMode.setBackgroundTintList(offColor);
        myView.bottomSheet.starMode.setBackgroundTintList(offColor);
        myView.bottomSheet.singleMode.setBackgroundTintList(offColor);
        switch(mainActivityInterface.getNearbyConnections().getNearbyStrategyType()) {
            case "cluster":
            default:
                myView.bottomSheet.clusterMode.setBackgroundTintList(onColor);
                break;
            case "star":
                myView.bottomSheet.starMode.setBackgroundTintList(onColor);
                break;
            case "single":
                myView.bottomSheet.singleMode.setBackgroundTintList(onColor);
                break;
        }
    }

    private void showcase1() {
        ArrayList<View> targets = new ArrayList<>();
        targets.add(myView.deviceButton);
        targets.add(myView.off);
        targets.add(myView.host);
        targets.add(myView.client);
        targets.add(myView.bottomSheet.bottomSheetTab);
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
                targets,null,infos,rects,"connectionsShowCase");
    }
    private void showcase2() {
        ArrayList<View> targets = new ArrayList<>();
        targets.add(myView.connectedTo);
        targets.add(myView.advertiseButton);
        targets.add(myView.discoverButton);
        ArrayList<String> infos = new ArrayList<>();
        infos.add(getString(R.string.connections_connected_devices_info));
        infos.add(getString(R.string.connections_advertise_info));
        infos.add(getString(R.string.connections_discover_info));
        ArrayList<Boolean> rects = new ArrayList<>();
        rects.add(true);
        rects.add(true);
        rects.add(true);
        mainActivityInterface.getShowCase().sequenceShowCase(requireActivity(),
                targets,null,infos,rects,"connectionsShowCase2");
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
        myView.connectInitiateButtons.setVisibility(View.GONE);
        mainActivityInterface.getNearbyConnections().clearTimer();

        if (isHost) {
            myView.host.setBackgroundTintList(onColor);
            myView.bottomSheet.hostOptions.setVisibility(View.VISIBLE);
            myView.connectedTo.setHint(mainActivityInterface.getNearbyConnections().getConnectedDevicesAsString());
            myView.connectedToLayout.setVisibility(View.VISIBLE);
            myView.connectInitiateButtons.setVisibility(View.VISIBLE);
            showcase2();

        } else if (isClient) {
            myView.client.setBackgroundTintList(onColor);
            myView.bottomSheet.clientOptions.setVisibility(View.VISIBLE);
            myView.connectedTo.setHint(mainActivityInterface.getNearbyConnections().getConnectedDevicesAsString());
            myView.connectedToLayout.setVisibility(View.VISIBLE);
            myView.connectInitiateButtons.setVisibility(View.VISIBLE);
            showcase2();

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
        if (mainActivityInterface.getNearbyConnections().getConnectionLog() == null) {
            mainActivityInterface.getNearbyConnections().setConnectionLog("");
        }
        myView.bottomSheet.connectionsLog.setHint(mainActivityInterface.getNearbyConnections().getConnectionLog());
        myView.connectedTo.setHint(mainActivityInterface.getNearbyConnections().getConnectedDevicesAsString());
    }

    public void setListeners() {
        // The deviceId
        myView.deviceButton.setOnClickListener(v -> textInputDialog());

        // The nearby strategy mode
        myView.bottomSheet.clusterMode.setOnClickListener(v -> {
            mainActivityInterface.getPreferences().setMyPreferenceString("nearbyStrategy", "cluster");
            mainActivityInterface.getNearbyConnections().setNearbyStrategy(Strategy.P2P_CLUSTER);
            updateStrategyButtons();
            myView.off.performClick();
        });
        myView.bottomSheet.starMode.setOnClickListener(v -> {
            mainActivityInterface.getPreferences().setMyPreferenceString("nearbyStrategy", "star");
            mainActivityInterface.getNearbyConnections().setNearbyStrategy(Strategy.P2P_STAR);
            updateStrategyButtons();
            myView.off.performClick();
        });
        myView.bottomSheet.singleMode.setOnClickListener(v -> {
            mainActivityInterface.getPreferences().setMyPreferenceString("nearbyStrategy", "single");
            mainActivityInterface.getNearbyConnections().setNearbyStrategy(Strategy.P2P_POINT_TO_POINT);
            updateStrategyButtons();
            myView.off.performClick();
        });

        // The client/host options
        myView.bottomSheet.keepHostFiles.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getNearbyConnections().setKeepHostFiles(isChecked));
        myView.bottomSheet.hostPassthrough.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyHostPassthrough",isChecked);
            mainActivityInterface.getNearbyConnections().setNearbyHostPassthrough(isChecked);
        });
        myView.bottomSheet.receiveHostFiles.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getNearbyConnections().setReceiveHostFiles(isChecked);
            // IV - When off turn keep off - user must make an active choice to 'keep' as it may overwrite local songs
            if (!isChecked) {
                myView.bottomSheet.keepHostFiles.setChecked(false);
                mainActivityInterface.getNearbyConnections().setKeepHostFiles(false);
            }
        });
        myView.bottomSheet.nearbyHostMenuOnly.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getNearbyConnections().setNearbyHostMenuOnly(isChecked));
        myView.bottomSheet.receiveAutoscroll.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getNearbyConnections().setReceiveHostAutoscroll(isChecked));
        myView.bottomSheet.receiveHostSections.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getNearbyConnections().setReceiveHostSongSections(isChecked));

        // Changing the nearby connection
        myView.off.setOnClickListener(v -> {
            updateOffHostClient(false, false);
            mainActivityInterface.getNearbyConnections().setIsHost(false);
            mainActivityInterface.getNearbyConnections().setUsingNearby(false);
            mainActivityInterface.getNearbyConnections().stopDiscovery();
            mainActivityInterface.getNearbyConnections().stopAdvertising();
            mainActivityInterface.getNearbyConnections().turnOffNearby();
            myView.connectedToLayout.setVisibility(View.GONE);
            resetClientOptions();
            enableConnectionButtons();
            myView.connectInitiateButtons.setVisibility(View.GONE);
        });
        myView.host.setOnClickListener(v -> {
            updateOffHostClient(true, false);
            mainActivityInterface.getNearbyConnections().setIsHost(true);
            mainActivityInterface.getNearbyConnections().setUsingNearby(true);
            myView.connectInitiateButtons.setVisibility(View.VISIBLE);
            myView.connectedTo.setHint(mainActivityInterface.getNearbyConnections().getConnectedDevicesAsString());
            myView.connectedToLayout.setVisibility(View.VISIBLE);
            resetClientOptions();
        });
        myView.client.setOnClickListener(v -> {
            updateOffHostClient(false, true);
            mainActivityInterface.getNearbyConnections().setIsHost(false);
            mainActivityInterface.getNearbyConnections().setUsingNearby(true);
            myView.connectInitiateButtons.setVisibility(View.VISIBLE);
            myView.connectedTo.setHint(mainActivityInterface.getNearbyConnections().getConnectedDevicesAsString());
            myView.connectedToLayout.setVisibility(View.VISIBLE);
        });

        // The advertise/discover buttons
        myView.advertiseButton.setOnClickListener(view -> doAdvertiseAction());
        myView.discoverButton.setOnClickListener(view -> doDiscoverAction());

        // Close the bottom sheet
        myView.dimBackground.setOnClickListener(v -> myView.bottomSheet.bottomSheetTab.performClick());

        // Clear the log
        myView.bottomSheet.connectionsLog.setOnClickListener(v -> {
            mainActivityInterface.getNearbyConnections().setConnectionLog("");
            updateConnectionsLog();
        });
    }

    private void doAdvertiseAction() {
        // Stop advertising/discovering if we were already doing that
        mainActivityInterface.getNearbyConnections().stopAdvertising();
        mainActivityInterface.getNearbyConnections().stopDiscovery();

        // Initialise the countdown
        mainActivityInterface.getNearbyConnections().initialiseCountdown();

        // Disable the other button
        myView.discoverButton.setEnabled(false);
        myView.advertiseButton.setBackgroundTintList(onColor);

        // After a short delay, advertise
        new Handler().postDelayed(() -> {
            try {
                mainActivityInterface.getNearbyConnections().startAdvertising();
                myView.advertiseButton.setOnClickListener(view -> enableConnectionButtons());
                mainActivityInterface.getNearbyConnections().setTimer(true, myView.advertiseButton);
            } catch (Exception e) {
                e.printStackTrace();
                mainActivityInterface.getNearbyConnections().clearTimer();
            }
        },200);
    }
    private void doDiscoverAction() {
        // Stop advertising/discovering if we were already doing that
        mainActivityInterface.getNearbyConnections().stopAdvertising();
        mainActivityInterface.getNearbyConnections().stopDiscovery();

        // Initialise the countdown
        mainActivityInterface.getNearbyConnections().initialiseCountdown();

        // Disable the other button
        myView.advertiseButton.setEnabled(false);
        myView.discoverButton.setBackgroundTintList(onColor);

        // After a short delay, discover
        new Handler().postDelayed(() -> {
            try {
                mainActivityInterface.getNearbyConnections().startDiscovery();
                myView.discoverButton.setOnClickListener(view -> enableConnectionButtons());
                mainActivityInterface.getNearbyConnections().setTimer(false, myView.discoverButton);
            } catch (Exception e) {
                e.printStackTrace();
                mainActivityInterface.getNearbyConnections().clearTimer();
            }
        }, 200);
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
                "deviceId", mainActivityInterface.getNearbyConnections().getDeviceId(),true);
        dialogFragment.show(requireActivity().getSupportFragmentManager(), "textInputFragment");
    }

    // Called from MainActivity after TextInputDialogFragment save
    public void updateValue(String which, String value) {
        if (which.equals("deviceName")) {
            myView.deviceButton.post(() -> myView.deviceButton.setHint(value));
            mainActivityInterface.getNearbyConnections().setDeviceId(value);
        }
    }

    public void enableConnectionButtons() {
        mainActivityInterface.getNearbyConnections().clearTimer();
        mainActivityInterface.getNearbyConnections().initialiseCountdown();
        mainActivityInterface.getNearbyConnections().stopAdvertising();
        mainActivityInterface.getNearbyConnections().stopDiscovery();
        myView.discoverButton.post(() -> {
            myView.discoverButton.setEnabled(true);
            myView.discoverButton.setBackgroundTintList(offColor);
            myView.discoverButton.setText(getString(R.string.connections_discover));
            myView.discoverButton.setOnClickListener(view -> doDiscoverAction());
        });
        myView.advertiseButton.post(() -> {
            myView.advertiseButton.setEnabled(true);
            myView.advertiseButton.setBackgroundTintList(offColor);
            myView.advertiseButton.setText(getString(R.string.connections_advertise));
            myView.advertiseButton.setOnClickListener(view -> doAdvertiseAction());
        });
    }


    public void updateCountdownText(boolean advertise, MaterialButton materialButton) {
        String text;
        if (advertise) {
            text = getString(R.string.connections_advertising) + "\n" + mainActivityInterface.getNearbyConnections().getCountdown();
        } else {
            text = getString(R.string.connections_searching) + "\n" + mainActivityInterface.getNearbyConnections().getCountdown();
        }
        materialButton.post(() -> materialButton.setText(text));
        mainActivityInterface.getNearbyConnections().doCountdown();
    }
}

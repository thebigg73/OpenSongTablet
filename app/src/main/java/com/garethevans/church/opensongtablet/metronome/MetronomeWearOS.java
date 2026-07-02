package com.garethevans.church.opensongtablet.metronome;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MetronomeWearOS {

    private final String TAG = "WearMetronomeOS";
    private static final String BEAT_PATH_TICK = "/metronome/beat/tick";
    private static final String BEAT_PATH_TOCK = "/metronome/beat/tock";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private boolean metronomeWearOS;
    private boolean isRunning = false;
    private boolean wearOSValid = false;
    private static final String METRONOME_STATE_PATH = "/metronome/state";

    public MetronomeWearOS(Context c) {
        this.c = c;
        this.mainActivityInterface = (MainActivityInterface) c;
        getPreferences();
    }

    private void getPreferences() {
        metronomeWearOS = mainActivityInterface.getPreferences().getMyPreferenceBoolean("metronomeWearOS", false);
    }

    public boolean getMetronomeWearOS() {
        return metronomeWearOS;
    }
    public void setMetronomeWearOS(boolean metronomeWearOS) {
        this.metronomeWearOS = metronomeWearOS;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("metronomeWearOS",metronomeWearOS);
    }

    public boolean getWearOSValid() {
        return wearOSValid;
    }
    public void setWearOSValid(boolean wearOSValid) {
        this.wearOSValid = wearOSValid;
    }
    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    public boolean getIsRunning() {
        return isRunning;
    }

    public void checkWearOSValid(Context context) {
        Wearable.getCapabilityClient(context)
                .getCapability("metronome_wear_app", CapabilityClient.FILTER_REACHABLE)
                .addOnSuccessListener(capabilityInfo -> {
                    boolean isWatchReachable = !capabilityInfo.getNodes().isEmpty();

                    if (isWatchReachable) {
                        Log.d(TAG, "Wear OS device is paired, reachable, and has the app installed.");
                        wearOSValid = true;
                    } else {
                        Log.d(TAG, "No reachable Wear OS devices found.");
                        wearOSValid = false;
                    }

                    // Update your UI/Fragment here
                    if (mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeFragment() != null) {
                        mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeFragment().updateWearOS(wearOSValid);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG,"Checked for WearOS compatibility on this device, but not found");
                    wearOSValid = false;
                });
    }


    public void checkWearConnection(@NonNull java.util.function.Consumer<Boolean> callback) {
        // Step 1: Check Google Play Services
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(c);
        if (status != ConnectionResult.SUCCESS) {
            Log.w(TAG, "Play Services not available: " + status);
            callback.accept(false);
            return;
        }

        // Step 2: Check if the device even supports the Wearable API
        try {
            Task<List<Node>> task = Wearable.getNodeClient(c).getConnectedNodes();
            task.addOnSuccessListener(nodes -> {
                boolean connected = nodes != null && !nodes.isEmpty();
                Log.d(TAG, "Wear nodes connected: " + connected);
                callback.accept(connected);
            }).addOnFailureListener(e -> {
                Log.w(TAG, "Wearable API unavailable on this device");
                callback.accept(false);
            });
        } catch (Exception e) {
            Log.w(TAG, "Wearable API not supported");
            callback.accept(false);
        }
    }


    /**
     * Call this whenever playback toggles, or the user shifts the BPM tempo.
     * This updates a single sync map immediately.
     */
    public void updateMetronomeState(boolean isPlaying, int bpm) {
        // Inside your Phone/Tablet app's sync method:
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/metronome/state");
        DataMap dataMap = dataMapRequest.getDataMap();

        dataMap.putBoolean("isPlaying", isPlaying);
        dataMap.putInt("bpm", bpm);
        dataMap.putInt("beatsPerBar", mainActivityInterface.getDrumViewModel().getThisBeats());
        dataMap.putInt("beatDenominator", mainActivityInterface.getDrumViewModel().getThisDivisions());

        // CRITICAL: Guarantees the data package fingerprint is unique every time,
        // bypassing the phone-side cache check so it actually transmits!
        dataMap.putLong("sync_timestamp", System.currentTimeMillis());

        // Fire it off urgently with no battery delays
        PutDataRequest request = dataMapRequest.asPutDataRequest();
        request.setUrgent();

        Wearable.getDataClient(c).putDataItem(request);
        // FIX: Change asPutDataItem() to asPutDataRequest()
        Wearable.getDataClient(c).putDataItem(dataMapRequest.asPutDataRequest())
                .addOnSuccessListener(unused -> Log.d(TAG, "State successfully pushed to Wearable Data Layer"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to push metronome state", e));

    }
}

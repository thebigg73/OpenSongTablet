package com.garethevans.church.opensongtablet.metronome;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class WearConnectionHelper {

    private final String TAG = "WearConnectionHelper";
    private static final String BEAT_PATH_TICK = "/metronome/beat/tick";
    private static final String BEAT_PATH_TOCK = "/metronome/beat/tock";
    private final Context c;
    public WearConnectionHelper(Context c) {
        this.c = c;
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
     * Sends a beat message to all connected Wear OS devices.
     * Should be called once per metronome tick.
     */
    public void sendBeat(boolean tickBeat) {
        Wearable.getNodeClient(c).getConnectedNodes()
                .addOnSuccessListener(nodes -> {
                    if (nodes == null || nodes.isEmpty()) {
                        // Skip sending; no nodes connected
                        Log.d("WatchMessenger", "No nodes connected, skipping beat");
                        return;
                    }
                    for (Node node : nodes) {
                        Wearable.getMessageClient(c)
                                .sendMessage(node.getId(), tickBeat ? BEAT_PATH_TICK : BEAT_PATH_TOCK, null)
                                .addOnSuccessListener(aVoid -> Log.d("WatchMessenger", "Beat sent"))
                                .addOnFailureListener(e -> Log.e("WatchMessenger", "Failed to send beat", e));
                    }
                });
    }
}

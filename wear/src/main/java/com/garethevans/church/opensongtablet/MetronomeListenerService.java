package com.garethevans.church.opensongtablet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Listens for metronome beat messages from the phone and vibrates the watch in time.
 * No deprecated APIs; compatible with Play Services 18+ and Wear OS 4+.
 */
public class MetronomeListenerService extends WearableListenerService {

    private static final String TAG = "MetronomeService";
    public static final String BEAT_PATH_TICK = "/metronome/beat/tick";
    public static final String BEAT_PATH_TOCK = "/metronome/beat/tock";
    private static final String METRONOME_STATE_PATH = "/metronome/state";
    public static final String ACTION_METRONOME_SYNC = "com.garethevans.church.opensongtablet.METRONOME_SYNC";
    @SuppressWarnings("FieldCanBeLocal")
    private final int tickLength = 50;
    @SuppressWarnings("FieldCanBeLocal")
    private final int tockLength = 30;
    @SuppressWarnings("FieldCanBeLocal")
    private final int tickAmplitude = 255;
    @SuppressWarnings("FieldCanBeLocal")
    private final int tockAmplitude = 120;

    private Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        Log.d(TAG, "Listener service created (emulator-friendly)");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Optional: trigger a test beat from intent extras
        if (intent != null && intent.hasExtra("test_beat")) {
            String path = intent.getStringExtra("test_beat");
            simulateBeat(path);
        }
        return START_STICKY;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    METRONOME_STATE_PATH.equals(event.getDataItem().getUri().getPath())) {

                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                boolean isPlaying = dataMap.getBoolean("isPlaying");
                int bpm = dataMap.getInt("bpm");
                int beatsPerBar = dataMap.getInt("beatsPerBar", 4);
                int beatDenominator = dataMap.getInt("beatDenominator", 4); // Pulls 4, 8, etc.

                Log.d(TAG, "Sync Event from Phone -> Playing: " + isPlaying + ", BPM: " + bpm + ", Signature: " + beatsPerBar + "/4");

                Intent intent = new Intent(ACTION_METRONOME_SYNC);
                intent.putExtra("isPlaying", isPlaying);
                intent.putExtra("bpm", bpm);
                intent.putExtra("beatsPerBar", beatsPerBar);
                intent.putExtra("beatDenominator", beatDenominator);


                //Intent intent = new Intent("com.opensong.METRONOME_UPDATE");
                //intent.putExtra("isPlaying", isPlaying);
                //intent.putExtra("bpm", bpm);
                //intent.putExtra("beatsPerBar", beatsPerBar);
                //intent.putExtra("beatDenominator", beatDenominator);
                sendBroadcast(intent);
                Log.d("MetronomeService", "📣 Broadcast dispatched on channel: " + ACTION_METRONOME_SYNC);
            }
        }
    }

    /**
     * Call this to simulate a beat for emulator testing
     */
    public void simulateBeat(String path) {
        Log.d(TAG, "Simulating beat: " + path);
        if (BEAT_PATH_TICK.equals(path)) {
            triggerVibration(true);  // main beat
        } else if (BEAT_PATH_TOCK.equals(path)) {
            triggerVibration(false); // offbeat
        } else {
            Log.d(TAG, "Unknown beat path: " + path);
        }
    }

    private void triggerVibration(boolean mainBeat) {
        if (vibrator == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect effect = mainBeat
                    ? VibrationEffect.createOneShot(tickLength, tickAmplitude)
                    : VibrationEffect.createOneShot(tockLength, tockAmplitude); // weaker offbeat
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(mainBeat ? 50 : 30);
        }
        Log.d(TAG, "Vibration triggered: " + (mainBeat ? "TICK" : "TOCK"));
    }


    @Override
    public void onMessageReceived(@NonNull MessageEvent event) {
        Log.d(TAG, "messageReceived");
        if (BEAT_PATH_TICK.equals(event.getPath())) {
            triggerVibration(50, 255);
        } else if (BEAT_PATH_TOCK.equals(event.getPath())) {
            triggerVibration(30, 120);
        } else {
            Log.d(TAG, "Unknown message path: " + event.getPath());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Listener service destroyed");
    }

    private void triggerVibration(int length, int amplitude) {
        Log.d(TAG, "Vibrate triggered");
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            // Short, crisp vibration for each beat
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createOneShot(length, amplitude));
            } else {
                vibrator.vibrate(length);  // fallback for older devices
            }
            Log.d(TAG, "Vibrate triggered");

        } else {
            Log.w(TAG, "No vibrator available on this device");
        }
    }



}

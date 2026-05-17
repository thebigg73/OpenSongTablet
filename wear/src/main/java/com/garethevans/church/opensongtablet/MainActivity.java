package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.wear.widget.BoxInsetLayout;

import com.garethevans.church.opensongtablet.wear.R;

public class MainActivity extends Activity {

    private TextView txtBpm, txtSignature;
    private Button btnTogglePlay, btnTempoUp, btnTempoDown, btnSigUp, btnSigDown;
    private BoxInsetLayout boxInsetLayout;
    private int theme_onbeat_flash, theme_offbeat_flash, theme_red, theme_green,
            theme_main, theme_button;
    // CRITICAL: Ensure this string perfectly matches the action string in your MetronomeListenerService
    private static final String ACTION_METRONOME_SYNC = "com.garethevans.church.opensongtablet.METRONOME_SYNC";

    // Metronome States
    private boolean useGroupedCompoundMeter = false; // Flips 6/8 into 2 macro-pulses

    private final TimeSignature[] standardSignatures = new TimeSignature[] {
            new TimeSignature(2, 4),
            new TimeSignature(3, 4),
            new TimeSignature(4, 4),
            new TimeSignature(6, 8)
    };
    private int currentSigIndex = 2;

    private int beatsPerBar = 4;
    private int beatDenominator = 4;
    private int currentBeatInBar = 1;
    private int currentBpm = 120;
    private boolean isPlaying = false;
    private Button btnToggleFlash;
    private boolean isFlashEnabled = true; // Default to enabled

    // Tap-Tempo tracking variables
    private final long[] tapTimestamps = new long[4]; // Tracks the last 4 taps
    private int tapCount = 0;

    private Vibrator vibrator;
    private final Handler metronomeHandler = new Handler(Looper.getMainLooper());
    private Runnable metronomeRunnable;

    private final BroadcastReceiver phoneSyncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MainActivity", "📥 RECEIVED SYNC BROADCAST FROM SERVICE!");
            if (intent != null) {
                currentBpm = intent.getIntExtra("bpm", currentBpm);
                beatsPerBar = intent.getIntExtra("beatsPerBar", beatsPerBar);
                beatDenominator = intent.getIntExtra("beatDenominator", beatDenominator);
                boolean phoneIsPlaying = intent.getBooleanExtra("isPlaying", false);

                // FORCE ONTO MAIN THREAD: Ensures your text updates safely render instantly
                runOnUiThread(() -> {
                    txtBpm.setText(String.valueOf(currentBpm));
                    txtSignature.setText(beatsPerBar + "/" + beatDenominator);
                });

                currentSigIndex = -1;
                for (int i = 0; i < standardSignatures.length; i++) {
                    if (standardSignatures[i].numerator == beatsPerBar &&
                            standardSignatures[i].denominator == beatDenominator) {
                        currentSigIndex = i;
                        break;
                    }
                }

                if (phoneIsPlaying != isPlaying) {
                    // The tablet state changed! Force the watch engine to match
                    forceTogglePlayback(phoneIsPlaying);
                } else if (isPlaying) {
                    // The metronome was already running, but the user changed the BPM on the tablet.
                    // Restart the local watch timer to apply the new tempo instantly.
                    restartTimer();
                }

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        theme_onbeat_flash = ResourcesCompat.getColor(getResources(), R.color.theme_onbeat_flash, getTheme());
        theme_offbeat_flash = ResourcesCompat.getColor(getResources(), R.color.theme_offbeat_flash, getTheme());
        theme_red = ResourcesCompat.getColor(getResources(), R.color.theme_red, getTheme());
        theme_green = ResourcesCompat.getColor(getResources(), R.color.theme_green, getTheme());
        theme_main = ResourcesCompat.getColor(getResources(), R.color.theme_main, getTheme());
        theme_button = ResourcesCompat.getColor(getResources(), R.color.theme_button, getTheme());

        boxInsetLayout = findViewById(R.id.boxInsetLayout);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        txtBpm = findViewById(R.id.txt_bpm);
        txtSignature = findViewById(R.id.txt_signature);
        btnTogglePlay = findViewById(R.id.btn_toggle_play);
        btnTempoUp = findViewById(R.id.btn_tempo_up);
        btnTempoDown = findViewById(R.id.btn_tempo_down);
        btnSigUp = findViewById(R.id.btn_sig_up);
        btnSigDown = findViewById(R.id.btn_sig_down);
        btnToggleFlash = findViewById(R.id.btn_toggle_flash);

        // Load saved preference (defaults to true if never set)
        isFlashEnabled = getSharedPreferences("OpenSongWearPrefs", MODE_PRIVATE)
                .getBoolean("flash_enabled", true);
        updateFlashButtonUi();

        txtBpm.setText(String.valueOf(currentBpm));
        txtSignature.setText(beatsPerBar + "/" + beatDenominator);

        // Toggle preference on click
        btnToggleFlash.setOnClickListener(v -> {
            isFlashEnabled = !isFlashEnabled;

            // Save preference instantly
            getSharedPreferences("OpenSongWearPrefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("flash_enabled", isFlashEnabled)
                    .apply();

            updateFlashButtonUi();
        });

        // Step FORWARD through standard signatures (with infinite looping)
        btnSigUp.setOnClickListener(v -> {
            if (currentSigIndex == -1) {
                // Fallback: If currently on an external custom signature, snap to standard 4/4
                currentSigIndex = 2;
            } else {
                // Increment the index. If it goes past the last item, wrap back to 0
                currentSigIndex++;
                if (currentSigIndex >= standardSignatures.length) {
                    currentSigIndex = 0;
                }
            }
            applyManualTimeSignature(standardSignatures[currentSigIndex]);
        });

        // Step BACKWARD through standard signatures (with infinite looping)
        btnSigDown.setOnClickListener(v -> {
            if (currentSigIndex == -1) {
                // Fallback: If currently on an external custom signature, snap to standard 4/4
                currentSigIndex = 2;
            } else {
                // Decrement the index. If it drops below 0, wrap to the last item in the array
                currentSigIndex--;
                if (currentSigIndex < 0) {
                    currentSigIndex = standardSignatures.length - 1;
                }
            }
            applyManualTimeSignature(standardSignatures[currentSigIndex]);
        });

        btnTogglePlay.setOnClickListener(v -> forceTogglePlayback(!isPlaying));

        setupMetronomeTicker();

        // Make the root container focusable so it can capture physical rotary scrolls
        boxInsetLayout.setFocusable(true);
        boxInsetLayout.setFocusableInTouchMode(true);
        boxInsetLayout.requestFocus();

        // ==========================================
        // CONTROL OPTION 1: ROTARY / BEZEL SCROLLING
        // ==========================================
        boxInsetLayout.setOnGenericMotionListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_SCROLL &&
                    androidx.core.view.MotionEventCompat.isFromSource(event, android.view.InputDevice.SOURCE_ROTARY_ENCODER) &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                // Inverse the axis value depending on scroll wheel direction orientation
                float axisValue = -event.getAxisValue(MotionEvent.AXIS_SCROLL);

                // Sweep by 2 BPM per detent notch for swift adjustments
                int tempoChange = Math.round(axisValue) * 2;
                adjustBpmSafely(tempoChange);
                return true;
            }
            return false;
        });

        // ==========================================
        // CONTROL OPTION 2: CLICK & LONG-PRESS TURBO
        // ==========================================
        // Single tap up/down for precise 1 BPM adjustments
        btnTempoUp.setOnClickListener(v -> adjustBpmSafely(1));
        btnTempoDown.setOnClickListener(v -> adjustBpmSafely(-1));

        // Long press for rapid 10 BPM macro-jumps
        btnTempoUp.setOnLongClickListener(v -> {
            adjustBpmSafely(10);
            triggerQuickHapticFeedback(15, 255); // Short sharp confirmation buzz
            return true;
        });
        btnTempoDown.setOnLongClickListener(v -> {
            adjustBpmSafely(-10);
            triggerQuickHapticFeedback(15, 255);
            return true;
        });

        // ==========================================
        // CONTROL OPTION 3: TAP-TEMPO ON BPM TEXT
        // ==========================================
        txtBpm.setClickable(true);
        txtBpm.setOnClickListener(v -> handleTapTempoInput());
    }

    private void forceTogglePlayback(boolean start) {
        isPlaying = start;
        if (isPlaying) {
            currentBeatInBar = 1;
            btnTogglePlay.setText("STOP");
            btnTogglePlay.setBackgroundColor(theme_red);
            metronomeHandler.post(metronomeRunnable);
        } else {
            btnTogglePlay.setText("START");
            btnTogglePlay.setBackgroundColor(theme_green);
            metronomeHandler.removeCallbacks(metronomeRunnable);
            // Reset layout color cleanly when stopping
            if (boxInsetLayout != null) {
                boxInsetLayout.setBackgroundColor(theme_main);
            }
        }
    }

    private void restartTimer() {
        if (isPlaying) {
            metronomeHandler.removeCallbacks(metronomeRunnable);
            metronomeHandler.post(metronomeRunnable);
        }
    }

    private void setupMetronomeTicker() {
        metronomeRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPlaying) return;

                // 1. Unified Engine Controller (Handles haptics AND layout flash)
                boolean isDownbeat = (currentBeatInBar == 1);

                if (beatDenominator == 8 && useGroupedCompoundMeter && beatsPerBar == 6) {
                    // Compound 6/8 Grouped Pulse Mode
                    if (currentBeatInBar == 1) {
                        executeBeatSequence(true, 65, 255);
                    } else if (currentBeatInBar == 4) {
                        executeBeatSequence(false, 40, 160);
                    } else {
                        executeBeatSequence(false, 20, 80);
                    }
                } else {
                    // Standard Meter Pulse Mode (2/4, 3/4, 4/4, un-grouped 6/8)
                    executeBeatSequence(isDownbeat, isDownbeat ? 60 : 25, isDownbeat ? 255 : 100);
                }

                // 2. Increment Step
                currentBeatInBar++;
                if (currentBeatInBar > beatsPerBar) {
                    currentBeatInBar = 1;
                }

                // 3. Interval Timing Calibration
                long baseInterval = 60000 / currentBpm;
                double adjustmentFactor = 4.0 / beatDenominator;
                long scaledIntervalMs = Math.round(baseInterval * adjustmentFactor);

                if (scaledIntervalMs < 50) scaledIntervalMs = 50;

                metronomeHandler.postDelayed(this, scaledIntervalMs);
            }
        };
    }

    private void adjustBpmSafely(int amount) {
        int targetBpm = currentBpm + amount;
        if (targetBpm >= 30 && targetBpm <= 300) {
            currentBpm = targetBpm;
            txtBpm.setText(String.valueOf(currentBpm));
            restartTimer();

            // Reset the tap counter context if the user switches back to button adjustments
            tapCount = 0;
        }
    }

    private void handleTapTempoInput() {
        long currentTimestamp = System.currentTimeMillis();
        triggerQuickHapticFeedback(10, 180); // Little structural click confirmation

        // Shift previous timestamps down
        System.arraycopy(tapTimestamps, 1, tapTimestamps, 0, tapTimestamps.length - 1);
        tapTimestamps[tapTimestamps.length - 1] = currentTimestamp;

        tapCount++;
        if (tapCount >= 2) {
            int activeIntervals = Math.min(tapCount - 1, tapTimestamps.length - 1);
            long totalDelta = 0;

            // Accumulate time distances between sequential taps
            for (int i = tapTimestamps.length - 1; i > tapTimestamps.length - 1 - activeIntervals; i--) {
                totalDelta += (tapTimestamps[i] - tapTimestamps[i - 1]);
            }

            long averageDeltaMs = totalDelta / activeIntervals;

            // Clear calculation out if user paused tapping for more than 2 seconds
            if (tapTimestamps[tapTimestamps.length - 1] - tapTimestamps[tapTimestamps.length - 2] > 2000) {
                tapCount = 1; // Restart cycle tracking
                return;
            }

            // Convert ms interval to BPM (60,000ms / Average Interval)
            int calculatedBpm = (int) (60000 / averageDeltaMs);

            if (calculatedBpm >= 30 && calculatedBpm <= 300) {
                currentBpm = calculatedBpm;
                txtBpm.setText(String.valueOf(currentBpm));
                restartTimer();
            }
        }
    }

    private void triggerQuickHapticFeedback(int length, int amplitude) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createOneShot(length, amplitude));
            } else {
                vibrator.vibrate(length);
            }
        }
    }

    /**
     * Combined engine execution to run haptic vibration and color flash together
     */
    private void executeBeatSequence(boolean isDownbeat, int vibrationLength, int amplitude) {
        Log.d("MainActivity", "Beat Step: " + (currentBeatInBar) + (isDownbeat ? " [TICK]" : " [TOCK]"));

        // 1. Run Haptics
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createOneShot(vibrationLength, amplitude));
            } else {
                vibrator.vibrate(vibrationLength);
            }
        }

        // 2. Run Visual Flash (ONLY if enabled by the user toggle)
        if (isFlashEnabled && boxInsetLayout != null) {
            boxInsetLayout.setBackgroundColor(isDownbeat ? theme_onbeat_flash : theme_offbeat_flash);

            // Reset back to main background theme color after 40ms
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isPlaying && boxInsetLayout != null) {
                    boxInsetLayout.setBackgroundColor(theme_main);
                }
            }, 40);
        }
    }

    private void applyManualTimeSignature(TimeSignature sig) {
        beatsPerBar = sig.numerator;
        beatDenominator = sig.denominator;
        currentBeatInBar = 1;

        txtSignature.setText(sig.toString());
        Log.d("MainActivity", "Manual Time Signature changed to: " + sig.toString());

        // Restart ticker smoothly with new tracking math parameters if running
        restartTimer();
    }

    private void updateFlashButtonUi() {
        if (btnToggleFlash != null) {
            btnToggleFlash.setText("⚡"); // Text removed for a cleaner look

            if (isFlashEnabled) {
                btnToggleFlash.setBackgroundColor(theme_button); // Pops out when armed
            } else {
                btnToggleFlash.setBackgroundColor(theme_main);   // Blends into background when disarmed
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Set up the matching filter frequency channel
        IntentFilter filter = new IntentFilter(ACTION_METRONOME_SYNC);

        // Android 13+ safety compatibility framework rule
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(phoneSyncReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(phoneSyncReceiver, filter);
        }
        Log.d("MainActivity", "🟢 phoneSyncReceiver successfully armed and listening.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister to protect watch battery when screen turns off
        if (phoneSyncReceiver != null) {
            unregisterReceiver(phoneSyncReceiver);
            Log.d("MainActivity", "🔴 phoneSyncReceiver disarmed.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPlaying = false;
        metronomeHandler.removeCallbacks(metronomeRunnable);
    }
}
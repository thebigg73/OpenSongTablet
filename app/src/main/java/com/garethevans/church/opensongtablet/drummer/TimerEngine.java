package com.garethevans.church.opensongtablet.drummer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TimerEngine {
    private final String TAG = "TimerEngine";

    // MIDI Message Constants
    private static final byte[] TICK = {(byte) 0xF8}; // Timing Clock
    private static final byte[] START = {(byte) 0xFA};
    private static final byte[] STOP = {(byte) 0xFC};
    private static final int PPQN = 24;
    private int tickCounter = 0;
    private int pulsesPerStep = 6; // Default to 16th notes (Straight)

    private ScheduledThreadPoolExecutor executor;
    private ScheduledThreadPoolExecutor turnOffExecutor;
    private ScheduledFuture<?> clockTask;

    private final MainActivityInterface mainActivityInterface;
    private int bpm = 120;
    private boolean isRunning = false;

    private boolean midiClock = false;
    private boolean midiClockStartStop = false;

    public TimerEngine(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        // Create a single-threaded executor for timing
        executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r);
            // Set to maximum priority to prevent CPU "napping"
            t.setPriority(Thread.MAX_PRIORITY);
            return t;
        });
        turnOffExecutor = new ScheduledThreadPoolExecutor(1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            executor.setRemoveOnCancelPolicy(true);
        }
    }

    @SuppressLint("DiscouragedApi")
    public void start() {
        if (isRunning) return;
        //tickCounter = 0;
        isRunning = true;

        // 1. Send MIDI START if requested
        midiClock = mainActivityInterface.getDrumViewModel().getMidiClock().getMidiClock();
        midiClockStartStop = mainActivityInterface.getDrumViewModel().getMidiClock().getMidiClockStartStop();

        if (midiClock && midiClockStartStop && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(START);
        }

        // 2. Schedule the MIDI 5-second cutoff task if we are using midiClockBurstMode
        if (midiClock && mainActivityInterface.getDrumViewModel().getMidiClock().getMidiClockBurstMode()) {
            turnOffExecutor.schedule(() -> {
                mainActivityInterface.getDrumViewModel().getMidiClock().setMidiClock(false);
                mainActivityInterface.getDrumViewModel().getMidiClock().setIsRunning(false);
                midiClock = false; // Stop sending TICK messages in the main loop

                // Check if we should shut down the whole engine
                // This calls back to the ViewModel to see if Drummer/Metronome are off
                mainActivityInterface.getDrumViewModel().stopTimerEngine();
            }, 5, TimeUnit.SECONDS);
        }

        startTask();

        Log.d(TAG, "Timer engine started at " + bpm + " BPM");
    }

    public void stop() {
        if (!isRunning) return;
        isRunning = false;

        // 1. Cancel the ticking task, but DO NOT shutdown the executor
        if (clockTask != null) {
            clockTask.cancel(false);
            clockTask = null;
        }

        if (midiClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            mainActivityInterface.getDrumViewModel().getMidiClock().getMidiClockStartStop()) {
            mainActivityInterface.getMidi().sendMidi(STOP);
        }

        midiClock = false;
        Log.d(TAG, "Timer engine stopped (Task cancelled, executor kept alive)");
    }

    private void startTask() {
        // 3. Main Timing Loop
        // Calculate nanoseconds per MIDI pulse
        // This ensures the new BPM is applied to the timer
        // 60s / BPM / 24 PPQN = time per MIDI pulse in nanoseconds
        long intervalNanos = (60L * 1_000_000_000L) / ((long) bpm * PPQN);
        clockTask = executor.scheduleAtFixedRate(() -> {
            try {
                // This will stop firing after 5 seconds if we are using the midiClockBurstMode because midiClock becomes false above
                if (midiClock && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mainActivityInterface.getMidi().sendMidi(TICK);
                }

                if (pulsesPerStep > 0 && tickCounter % pulsesPerStep == 0) {
                    if (stepListener != null) {
                        stepListener.onStep(tickCounter / pulsesPerStep);
                    }
                }
                tickCounter++;
            } catch (Exception e) {
                Log.e(TAG, "Error sending MIDI clock", e);
            }
        }, 0, intervalNanos, TimeUnit.NANOSECONDS);
    }

    public void setBpm(int newBpm) {
        this.bpm = newBpm;
        if (isRunning && clockTask != null) {
            clockTask.cancel(false); // Stop the current ticking
            startTask(); // A helper method to just trigger scheduleAtFixedRate
        }
    }

    public int getTickCounter() {
        return tickCounter;
    }
    public boolean isRunning() {
        return isRunning;
    }

    public interface OnStepListener {
        void onStep(int totalSteps);
    }

    private OnStepListener stepListener;

    public void setOnStepListener(OnStepListener listener) {
        this.stepListener = listener;
    }

    public void setPulsesPerStep(int pulses) {
        this.pulsesPerStep = pulses;
    }

    public void resetTickCounter() {
        this.tickCounter = 0;
    }

    /**
     * Updates the engine parameters and restarts the task if running
     * to apply the new interval immediately.
     */
    public void refresh(int newBpm, int newPulsesPerStep) {
        // If the values are the same, IGNORE the request to avoid stuttering
        if (this.bpm == newBpm && this.pulsesPerStep == newPulsesPerStep) {
            return;
        }

        this.bpm = newBpm;
        this.pulsesPerStep = newPulsesPerStep;

        if (isRunning && clockTask != null) {
            clockTask.cancel(false);
            // Note: Do NOT reset tickCounter here if you want seamless BPM changes
            startTask();
        }
    }

}
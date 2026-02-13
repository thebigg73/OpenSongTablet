package com.garethevans.church.opensongtablet.metronome;

import android.content.Context;
import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import com.garethevans.church.opensongtablet.drummer.DrumSoundManager;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class Metronome {

    // This object holds all of the metronome activity

    // It is now obsolete and we use the DrumViewModel.class in Drummer instead
    private final MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "Metronome";

    private boolean isRunning = false;
    private float metronomeTickVol;
    private float metronomeTickVolLeft, metronomeTickVolRight;
    private float metronomeTockVolLeft, metronomeTockVolRight;
    private String metronomeTickSound, metronomeTockSound;
    private float metronomeTockVol;
    private String metronomePan;
    private int metronomeLength;
    private boolean metronomeAudio, metronomeShowVisual, metronomeMidi,
            metronomeAutoStart, metronomeUseDefaults;
    private int tickColor = Color.RED;
    private int tockColor = Color.WHITE;
    private VisualListener visualListener;
    private MetronomeFragment metronomeFragment;
    private int totalStepsProcessed = 0;
    // Pre-calculate these once when the tempo/time signature changes
    private int cachedInterval = 4;
    private int cachedMaxSteps = -1;

    public Metronome(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        metronomeTickVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("metronomeTickVol",1f);
        metronomeTockVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("metronomeTockVol",1f);
        metronomePan = mainActivityInterface.getPreferences().getMyPreferenceString("metronomePan","C");
        metronomeAudio = mainActivityInterface.getPreferences().getMyPreferenceBoolean("metronomeAudio",true);
        metronomeShowVisual = mainActivityInterface.getPreferences().getMyPreferenceBoolean("metronomeShowVisual",true);
        metronomeMidi = mainActivityInterface.getPreferences().getMyPreferenceBoolean("metronomeMidi",false);
        metronomeLength = mainActivityInterface.getPreferences().getMyPreferenceInt("metronomeLength",0);
        metronomeAutoStart = mainActivityInterface.getPreferences().getMyPreferenceBoolean("metronomeAutoStart",false);
        metronomeTickSound = mainActivityInterface.getPreferences().getMyPreferenceString("metronomeTickSound","digital_high");
        metronomeTockSound = mainActivityInterface.getPreferences().getMyPreferenceString("metronomeTockSound","digital_low");
        metronomeUseDefaults = mainActivityInterface.getPreferences().getMyPreferenceBoolean("metronomeUseDefaults",false);
        calculateVolumes();
        checkTickTockColors();
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    public boolean getIsRunning() {
        return isRunning;
    }

    public void checkTickTockColors() {
        tickColor = mainActivityInterface.getMyThemeColors().getMetronomeColor();
        tockColor = ColorUtils.blendARGB(tickColor, mainActivityInterface.getPalette().surface, 0.4f);
    }

    public void prepare(int denominator, int metronomeLength, int stepsPerBar) {
        this.cachedInterval = (denominator == 8) ? 2 : 4;
        this.cachedMaxSteps = (metronomeLength > 0) ? metronomeLength * stepsPerBar : -1;
        this.totalStepsProcessed = 0;
    }

    public void onStep(int totalSteps, int stepsPerBar, long beatDuration) {
        if (!isRunning) return;

        // 1. Check stop condition using totalSteps to align with the sequencer
        // This prevents the "separate clock" issue.
        if (cachedMaxSteps != -1 && totalSteps >= cachedMaxSteps) {
            mainActivityInterface.getDrumViewModel().stopMetronome();
            return;
        }

        // 2. Get the pulse interval directly from the shared Drummer logic
        // In 6/8, this will return 2 (every 2nd step)
        int interval = mainActivityInterface.getDrumViewModel().getThisStepsPerPulse();
        int stepInBar = totalSteps % stepsPerBar;

        // 3. Click logic: Use 'interval' for the modulo check
        if (stepInBar % interval == 0) {
            int beatNumber = (stepInBar / interval) + 1;
            boolean isPrimary = (stepInBar == 0);

            // 4. Accent logic for 6/8 (Denominator 8)
            boolean isSecondary = false;
            if (mainActivityInterface.getDrumViewModel().getThisDivisions() == 8) {
                // Beat 1 is Primary, Beat 4 is the middle pulse in 6/8
                isSecondary = (beatNumber == 4);
            }
            boolean isAccent = isPrimary || isSecondary;

            // Trigger Audio and Visuals
            if (metronomeAudio) {
                playAudio(isPrimary); // Accent sound on Beat 1
            }
            if (metronomeMidi) {
                playMidi(isAccent); // MIDI accent on 1 and 4
            }
            if (metronomeShowVisual && visualListener != null) {
                // Pass the beat number (1-6) and the accent status
                visualListener.onVisualBeat(beatNumber, isAccent, beatDuration);
            }
        }
    }

    /*public void onStep(int stepInBar, int stepsPerBar, int denominator, long beatDuration) {
        // Check if we should stop the metronome (metronomeLength)
        if (metronomeLength>0) {
            int maxSteps = metronomeLength * stepsPerBar;

            if (totalStepsProcessed >= maxSteps) {
                // Trigger the stop command
                mainActivityInterface.getDrumViewModel().stopMetronome();
                totalStepsProcessed = 0; // Reset for next time
                return; // Exit early so no more sounds play
            }
        }

        // 2. Increment our counter
        totalStepsProcessed++;

        // Determine how many steps make a beat (pulse).
        // In 4/4 or 3/4 (denominator 4), this is 4 steps.
        int interval = (denominator == 8) ? 2 : 4;

        // Use the local variable instead of the class field 'stepsPerPulse'
        if (stepInBar % interval == 0) {

            int beatNumber = (stepInBar / interval) + 1;
            boolean isPrimary = (stepInBar == 0);

            // Secondary Accent logic
            boolean isSecondary = false;
            if (denominator == 8) {
                // For 6/8, 9/8, 12/8, we usually want an accent every 3 beats
                // (e.g., Beat 1 and Beat 4 in 6/8)
                isSecondary = (beatNumber > 1 && (beatNumber - 1) % 3 == 0);
            }
            boolean isAccent = isPrimary || isSecondary;

            // The audio logic
            if (metronomeAudio) {
                playAudio(isPrimary);
            }

            // The MIDI logic
            if (metronomeMidi) {
                playMidi(isAccent);
            }

            // The visual Logic
            if (metronomeShowVisual && visualListener != null) {
                // Pass true for big accents, false for standard tocks
                // Pass the calculated duration (e.g. 500ms for 4/4 at 120bpm)
                visualListener.onVisualBeat(beatNumber, isAccent, beatDuration);
            }
        }
    }*/

    // The user preferences
    public int getTickColor() {
        return tickColor;
    }
    public int getTockColor() {
        return tockColor;
    }
    public void setMetronomeTickVol(float metronomeTickVol) {
        this.metronomeTickVol = metronomeTickVol;
        mainActivityInterface.getPreferences().setMyPreferenceFloat("metronomeTickVol",metronomeTickVol);
        calculateVolumes();
    }
    public void setMetronomeTockVol(float metronomeTockVol) {
        this.metronomeTockVol = metronomeTockVol;
        mainActivityInterface.getPreferences().setMyPreferenceFloat("metronomeTockVol",metronomeTockVol);
        calculateVolumes();
    }
    public float getMetronomeTickVol() {
        return metronomeTickVol;
    }
    public float getMetronomeTockVol() {
        return metronomeTockVol;
    }
    public String getMetronomePan() {
        return metronomePan;
    }
    public void setMetronomePan(String metronomePan) {
        this.metronomePan = metronomePan;
        mainActivityInterface.getPreferences().setMyPreferenceString("metronomePan",metronomePan);
        calculateVolumes();
    }
    public int getMetronomeLength() {
        return metronomeLength;
    }
    public void setMetronomeLength(int metronomeLength) {
        this.metronomeLength = metronomeLength;
        mainActivityInterface.getPreferences().setMyPreferenceInt("metronomeLength",metronomeLength);
    }
    public boolean getMetronomeAutoStart() {
        return metronomeAutoStart;
    }
    public void setMetronomeAutoStart(boolean metronomeAutoStart) {
        this.metronomeAutoStart = metronomeAutoStart;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("metronomeAutoStart",metronomeAutoStart);
    }
    public boolean getMetronomeMidi() {
        return metronomeMidi;
    }
    public void setMetronomeMidi(boolean metronomeMidi) {
        this.metronomeMidi = metronomeMidi;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("metronomeMidi",metronomeMidi);
    }
    public String getMetronomeTickSound() {
        return metronomeTickSound;
    }
    public String getMetronomeTockSound() {
        return metronomeTockSound;
    }
    public void setMetronomeTickSound(String metronomeTickSound) {
        this.metronomeTickSound = metronomeTickSound;
        mainActivityInterface.getPreferences().setMyPreferenceString("metronomeTickSound",metronomeTickSound);
    }
    public void setMetronomeTockSound(String metronomeTockSound) {
        this.metronomeTockSound = metronomeTockSound;
        mainActivityInterface.getPreferences().setMyPreferenceString("metronomeTockSound",metronomeTockSound);
    }
    public boolean getMetronomeUseDefaults() {
        return metronomeUseDefaults;
    }
    public void setMetronomeUseDefaults(boolean metronomeUseDefaults) {
        this.metronomeUseDefaults = metronomeUseDefaults;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("metronomeUseDefaults",metronomeUseDefaults);
    }
    public boolean getMetronomeShowVisual() {
        return metronomeShowVisual;
    }
    public void setMetronomeShowVisual(boolean metronomeShowVisual) {
        this.metronomeShowVisual = metronomeShowVisual;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("metronomeShowVisual", metronomeShowVisual);
    }
    public boolean getMetronomeAudio() {
        return metronomeAudio;
    }
    public void setMetronomeAudio(boolean metronomeAudio) {
        this.metronomeAudio = metronomeAudio;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("metronomeAudio",metronomeAudio);
    }
    public void resetTotalStepsProcessed() {
        totalStepsProcessed = 0;
    }

    // Update the volumes based on volume and pan
    public void calculateVolumes() {
        metronomeTickVolLeft = metronomeTickVol;
        metronomeTickVolRight = metronomeTickVol;
        metronomeTockVolLeft = metronomeTockVol;
        metronomeTockVolRight = metronomeTockVol;
        switch (metronomePan) {
            case "L":
                metronomeTickVolRight = 0.0f;
                metronomeTockVolRight = 0.0f;
                break;
            case "R":
                metronomeTickVolLeft = 0.0f;
                metronomeTockVolLeft = 0.0f;
                break;
        }
    }
    private void playAudio(boolean accent) {
        // Use the soundManager instance held in the ViewModel
        DrumSoundManager soundManager = mainActivityInterface.getDrumViewModel().getDrumSoundManager();

        if (soundManager != null) {
            soundManager.playMetronome(accent, metronomeTickVolLeft, metronomeTickVolRight, metronomeTockVolLeft, metronomeTockVolRight);
        }
    }


    // If we are using the metronomeFragment, we keep a reference
    public MetronomeFragment getMetronomeFragment() {
        return metronomeFragment;
    }
    public void setMetronomeFragment(MetronomeFragment metronomeFragment) {
        this.metronomeFragment = metronomeFragment;
    }
    public void updateStartStopButton() {
        if (metronomeFragment!=null) {
            metronomeFragment.setStartStopIcon(isRunning);
        }
    }

    // Send the MIDI
    private void playMidi(boolean accent) {
        // Send MIDI Note for metronome
        byte note = (byte) (accent ? 37 : 36);
        mainActivityInterface.getMidi().sendMidi(note);
    }

    public interface VisualListener {
        /**
         * @param beatNumber The current musical beat (1, 2, 3...)
         * @param isAccent True if it's the start of the bar or a major division
         * @param beatDurationMs The duration of the beat in milliseconds
         */
        void onVisualBeat(int beatNumber, boolean isAccent, long beatDurationMs);
    }

    public void setVisualListener(VisualListener listener) {
        this.visualListener = listener;
    }

}

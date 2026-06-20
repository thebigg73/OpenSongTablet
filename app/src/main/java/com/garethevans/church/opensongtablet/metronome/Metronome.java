package com.garethevans.church.opensongtablet.metronome;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

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
    private int metronomeLength, metronomeVisualLength;
    private boolean metronomeAudio, metronomeShowVisual, metronomeMidi,
            metronomeAutoStart, metronomeUseDefaults;
    private int tickColor = Color.RED;
    private int tockColor = Color.WHITE;
    private VisualListener visualListener;
    private MetronomeFragment metronomeFragment;
    private int totalStepsProcessed = 0;
    private int cachedMaxSteps = -1;
    private int cachedMaxVisualSteps = -1;

    public Metronome(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        metronomeTickVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("metronomeTickVol",1f);
        metronomeTockVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("metronomeTockVol",1f);
        metronomePan = mainActivityInterface.getPreferences().getMyPreferenceString("metronomePan","C");
        metronomeAudio = mainActivityInterface.getPreferences().getMyPreferenceBoolean("metronomeAudio",true);
        metronomeShowVisual = mainActivityInterface.getPreferences().getMyPreferenceBoolean("metronomeShowVisual",true);
        metronomeMidi = mainActivityInterface.getPreferences().getMyPreferenceBoolean("metronomeMidi",false);
        metronomeLength = mainActivityInterface.getPreferences().getMyPreferenceInt("metronomeLength",0);
        metronomeVisualLength = mainActivityInterface.getPreferences().getMyPreferenceInt("metronomeVisualLength",0);
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

    public void prepare(int denominator, int stepsPerBar) {
        // Pre-calculate these once when the tempo/time signature changes
        int cachedInterval = (denominator == 8) ? 2 : 4;
        this.cachedMaxSteps = (metronomeLength > 0) ? metronomeLength * stepsPerBar : -1;
        this.cachedMaxVisualSteps = (metronomeVisualLength > 0) ? metronomeVisualLength * stepsPerBar : -1;
        this.totalStepsProcessed = 0;
    }

    public void onStep(int totalSteps, int stepsPerBar, long beatDuration) {
        if (!isRunning) return;

        // 1. Manually increment the counter every time a step occurs
        totalStepsProcessed++;

        // 2. Check stop condition using our local counter
        boolean continueMetronome = cachedMaxSteps == -1 || (totalStepsProcessed < cachedMaxSteps);
        boolean continueVisualMetronome = cachedMaxVisualSteps == -1 || (totalStepsProcessed < cachedMaxVisualSteps);

        // We use totalStepsProcessed because it counts continuously from the moment we hit Start
        if (!continueMetronome && !continueVisualMetronome) {
            Log.d(TAG, "Limit reached: " + totalStepsProcessed + "/" + Math.max(cachedMaxSteps,cachedMaxVisualSteps) + ". Stopping.");

            // Use a Runnable or post to the main thread if the ViewModel call
            // involves UI updates, or call it directly if thread-safe:
            mainActivityInterface.getDrumViewModel().stopMetronome();

            // Reset local state
            isRunning = false;
            totalStepsProcessed = 0;
            return;
        }

        // 3. Get the pulse interval directly from the shared Drummer logic
        // In 6/8, this will return 2 (every 2nd step)
        int interval = mainActivityInterface.getDrumViewModel().getThisStepsPerPulse();
        int stepInBar = totalSteps % stepsPerBar;

        // 4. Click logic: Use 'interval' for the modulo check
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

            // Trigger Audio and Visual
            // Because audio and visual can be different lengths, check both
            if (metronomeAudio && continueMetronome) {
                playAudio(isPrimary); // Accent sound on Beat 1
            }
            if (metronomeMidi && continueMetronome) {
                playMidi(isAccent); // MIDI accent on 1 and 4
            }
            if (metronomeShowVisual && visualListener != null && continueVisualMetronome) {
                // Pass the beat number (1-6) and the accent status
                visualListener.onVisualBeat(beatNumber, isAccent, beatDuration);
            }
        }
    }


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
    public int getMetronomeVisualLength() {
        return metronomeVisualLength;
    }
    public void setMetronomeVisualLength(int metronomeVisualLength) {
        this.metronomeVisualLength = metronomeVisualLength;
        mainActivityInterface.getPreferences().setMyPreferenceInt("metronomeVisualLength",metronomeVisualLength);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(note);
        }
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

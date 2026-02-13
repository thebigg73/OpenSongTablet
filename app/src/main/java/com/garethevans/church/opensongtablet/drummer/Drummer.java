package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.Map;

public class Drummer {

    // This class is to emulate a drum machine that you can play along with.
    // There are basic midi files available to begin with, but ultimately the user can create their own!
    // Also looking at using Oboe to access low latency audio

    private final String TAG = "Drummer";
    private final MainActivityInterface mainActivityInterface;
    private DrumPatternJson currentPattern;

    private boolean isRunning = false;
    private boolean isCountIn = false;
    private int startStep = -1;
    private int lastKnownStep = 0; // Local tracker to decouple from ViewModel

    private DrumSection activeSection = DrumSection.MAIN;
    private DrumSection pendingSection = null;
    private DrumSection sectionBeforeFill = DrumSection.MAIN;
    private DrumSection nextSectionAfterFill = null; // New variable
    private boolean crashOnNextBar = false;

    // The map currently being read by the playback loop
    private Map<String, int[]> activeMap;

    // Initialise the class and get a MainActivityInterface reference
    public Drummer(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean getIsRunning() {
        return isRunning;
    }

    public void setPattern(DrumPatternJson pattern) {
        this.currentPattern = pattern;
    }

    private void handleCountIn(int stepInBar, int stepsPerBar, int stepsPerPulse) {
        // Only play a sound on the "Click" steps
        if (stepInBar % stepsPerPulse == 0) {
            int currentBeat = (stepInBar / stepsPerPulse) + 1;
            int totalBeatsInBar = stepsPerBar / stepsPerPulse;

            if (currentBeat < totalBeatsInBar) {
                // Beats 1, 2, 3...
                playCountInSound("HatClosed");
            } else {
                // The very last beat of the bar (e.g., Beat 4 or Beat 6)
                playCountInSound("HatOpen");
            }
        }
    }

    private void playCountInSound(String partName) {
        DrumSoundManager soundManager = mainActivityInterface.getDrumViewModel().getDrumSoundManager();
        if (soundManager != null) {
            // Trigger the specific sample
            soundManager.playDrum(partName,100);
        }
    }

    /**
     * Called by the TimerEngine via DrumViewModel on every sequencer step.
     */
    public void onStep(int totalSteps) {
        if (!isRunning) return;

        // Send the step to the ViewModel so the UI can observe it
        mainActivityInterface.getDrumViewModel().updateStepCount(totalSteps);

        int stepsPerBar = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();
        int stepInBar = totalSteps % stepsPerBar;

        // A. MID-BAR FILL ENTRANCE (Step 8)
        if (stepInBar == 8 && (pendingSection == DrumSection.FILL_MAIN || pendingSection == DrumSection.FILL_VARIATION)) {
            activeSection = pendingSection;
            pendingSection = null;
            updateActiveMap();
        }

        // B. BAR START LOGIC (Step 0)
        if (stepInBar == 0) {
            // 1. Handle Crash
            if (crashOnNextBar) {
                triggerSound("Crash", 115);
                crashOnNextBar = false;
            }

            // 2. Handle Exit from Fill
            if (activeSection == DrumSection.FILL_MAIN || activeSection == DrumSection.FILL_VARIATION) {
                activeSection = (nextSectionAfterFill != null) ? nextSectionAfterFill : DrumSection.MAIN;
                nextSectionAfterFill = null;
                updateActiveMap();
            }
            // 3. Handle Normal Transitions (Main -> Variation)
            else if (pendingSection != null) {
                activeSection = pendingSection;
                pendingSection = null;
                updateActiveMap();
            }
        }

        if (isCountIn && stepInBar == stepsPerBar - 1) {
            isCountIn = false;
            // IMPORTANT: We need to make sure we are ready for Step 0 of the MAIN pattern
            activeSection = DrumSection.MAIN;
            updateActiveMap();
        }

        // C. PLAYBACK
        if (isCountIn) {
            handleCountIn(stepInBar, stepsPerBar, mainActivityInterface.getDrumViewModel().getThisStepsPerPulse());
        } else {
            playActivePattern(stepInBar);
        }
    }

    private void updateActiveMap() {
        if (currentPattern == null) return;

        switch (activeSection) {
            case MAIN:
                activeMap = currentPattern.getMainPattern();
                break;
            case VARIATION:
                activeMap = currentPattern.getVariationPattern();
                break;
            case FILL_MAIN:
                activeMap = currentPattern.getFillMainPattern(); // The standard fill
                break;
            case FILL_VARIATION:
                activeMap = currentPattern.getFillVariationPattern(); // The busier fill
                break;
        }

        mainActivityInterface.getDrumViewModel().updateActiveSection(activeSection);
    }

    private void playActivePattern(int stepInBar) {
        if (activeMap == null) return;

        for (Map.Entry<String, int[]> entry : activeMap.entrySet()) {
            int velocity = entry.getValue()[stepInBar];
            if (velocity > 0) {
                triggerSound(entry.getKey(), velocity);
            }
        }
    }

    public void fill(int currentStep) {
        int stepsPerBar = mainActivityInterface.getDrumViewModel().getThisStepsPerBar();
        int stepInBar = currentStep % stepsPerBar;

        // Determine which fill to use based on where we are coming FROM
        DrumSection fillToUse;
        if (activeSection == DrumSection.VARIATION) {
            fillToUse = DrumSection.FILL_VARIATION;
        } else {
            fillToUse = DrumSection.FILL_MAIN;
        }

        if (activeSection != DrumSection.FILL_MAIN && activeSection != DrumSection.FILL_VARIATION) {
            sectionBeforeFill = activeSection;
        }

        DrumSection returnTo = (nextSectionAfterFill != null) ? nextSectionAfterFill : sectionBeforeFill;

        if (stepInBar > 11) {
            // LATE: Switch immediately
            this.activeSection = fillToUse;
            updateActiveMap();
            this.pendingSection = returnTo;
        } else {
            // EARLY: Queue the switch for Beat 3
            this.pendingSection = fillToUse;
            this.nextSectionAfterFill = returnTo;
        }
        this.crashOnNextBar = true;
    }

    public synchronized void transition() {
        // Toggle between MAIN and VARIATION
        DrumSection target = (activeSection == DrumSection.VARIATION) ?
                DrumSection.MAIN : DrumSection.VARIATION;

        this.nextSectionAfterFill = target;

        // Call the updated fill logic
        fill(this.lastKnownStep);
    }

    private void triggerSound(String instrument, int velocity) {
        // Route the trigger back to the SoundManager in the ViewModel
        if (mainActivityInterface.getDrumViewModel() != null &&
                mainActivityInterface.getDrumViewModel().getDrumSoundManager() != null) {
            mainActivityInterface.getDrumViewModel().getDrumSoundManager().playDrum(instrument, velocity);
        }
    }

    public void reset(int currentTotalSteps) {
        this.isCountIn = true;
        this.startStep = currentTotalSteps;
        this.isRunning = true;
    }

    public void setIsCountIn(boolean isCountIn) {
        this.isCountIn = isCountIn;
    }

    public void setStartStep(int startStep) {
        this.startStep = startStep;
    }

    public String getDrummerStyle() {
        Log.d(TAG,"TODO getDrummerStyle");
        return "Standard";
    }
}

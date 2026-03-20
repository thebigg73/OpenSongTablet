package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.MainActivity;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.Map;

public class Drummer {

    // This class is to emulate a drum machine that you can play along with.
    // There are basic midi files available to begin with, but ultimately the user can create their own!
    // Also looking at using Oboe to access low latency audio

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Drummer";
    private final MainActivityInterface mainActivityInterface;
    private DrumPatternJson currentPattern;

    private boolean isRunning = false;
    private boolean isCountIn = false;
    private int startStep = -1;
    private boolean sequencerMode = false;

    private DrumSection activeSection = DrumSection.MAIN;
    private DrumSection pendingSection = null;
    private DrumSection sectionBeforeFill = DrumSection.MAIN;
    private DrumSection nextSectionAfterFill = null; // New variable
    private boolean crashOnNextBar = false;
    private String drummerStyle="Standard";
    private final String drum_kit_acoustic;
    private final String drum_kit_cajon;
    private final String drum_kit_percussion;

    // The map currently being read by the playback loop
    private Map<String, int[]> activeMap;

    // Initialise the class and get a MainActivityInterface reference
    public Drummer(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        drum_kit_acoustic = c.getString(R.string.drum_kit_acoustic);
        drum_kit_cajon = c.getString(R.string.drum_kit_cajon);
        drum_kit_percussion = c.getString(R.string.drum_kit_percussion);
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
            soundManager.playDrum(getCajonPrefixIfNeeded()+partName, 100);
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
        if (stepInBar == 8 && !sequencerMode && (pendingSection == DrumSection.FILL_MAIN || pendingSection == DrumSection.FILL_VARIATION)) {
            activeSection = pendingSection;
            pendingSection = null;
            updateActiveMap();
        }

        // B. BAR START LOGIC (Step 0)
        if (stepInBar == 0 && !sequencerMode) {
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

        if (isCountIn && stepInBar == stepsPerBar - 1 && !sequencerMode) {
            isCountIn = false;
            // IMPORTANT: We need to make sure we are ready for Step 0 of the MAIN pattern
            activeSection = DrumSection.MAIN;
            updateActiveMap();
        }

        // If we are using the sequencer, just stay here
        if (sequencerMode) {
            isCountIn = false;
            activeSection = mainActivityInterface.getDrumViewModel().getActiveSection().getValue();
            pendingSection = activeSection;
            updateActiveMap();
        }

        // C. PLAYBACK
        if (isCountIn) {
            handleCountIn(stepInBar, stepsPerBar, mainActivityInterface.getDrumViewModel().getThisStepsPerPulse());
        } else {
            playActivePattern(stepInBar);
        }
    }

    public void updateActiveMap() {
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

        String cajonPrefix = getCajonPrefixIfNeeded();

        for (Map.Entry<String, int[]> entry : activeMap.entrySet()) {
            int velocity = entry.getValue()[stepInBar];
            if (velocity > 0) {
                triggerSound(cajonPrefix + entry.getKey(), velocity);
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
        // Local tracker to decouple from ViewModel
        int lastKnownStep = 0;
        fill(lastKnownStep);
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
        if (drummerStyle==null || drummerStyle.isEmpty()) {
            drummerStyle = "Acoustic";
        }
        Log.d(TAG,"getDrummerStyle:"+drummerStyle);
        return drummerStyle;
    }

    public void setDrummerStyle(String drummerStyle) {
        Log.d(TAG,"setDrummerStyle("+drummerStyle+")");
        this.drummerStyle = drummerStyle;
    }

    public String getDrummerStyleForSongXML(String drummerStyle) {
        if (drummerStyle.equals(drum_kit_cajon) || drummerStyle.equals("Cajon") || drummerStyle.equals(drum_kit_percussion) || drummerStyle.equals("Percussion")) {
            return "Percussion";
        } else {
            return "Acoustic";
        }
    }
    public String getDrummerStyleFromXML(String drummerStyle) {
        if (drummerStyle.equals(drum_kit_cajon) || drummerStyle.equals("Cajon") || drummerStyle.equals(drum_kit_percussion) || drummerStyle.equals("Percussion")) {
            return drum_kit_percussion;
        } else {
            return drum_kit_acoustic;
        }
    }

    public void setSequencerMode(boolean sequencerMode) {
        this.sequencerMode = sequencerMode;
    }

    public boolean getSequencerMode() {
        return sequencerMode;
    }

    public ArrayList<String> getDrummerFiles(String timeSig, boolean niceNames) {
        Uri drumFolder = mainActivityInterface.getStorageAccess().getUriForItem("Drummer","","");
        ArrayList<String> drumFiles = mainActivityInterface.getStorageAccess().listFilesAtUri(drumFolder);
        ArrayList<String> drummerFileNames = new ArrayList<>();

        String matchText = timeSig!=null && timeSig.contains("/") ? timeSig.replace("/","_")+".json" : null;

        for (String drumFile : drumFiles) {
            if (matchText==null || drumFile.contains(matchText)) {
                drummerFileNames.add(niceNames ? getNiceNameFromFilename(drumFile) : drumFile);
            }
        }
        return drummerFileNames;
    }

    public void loadDrummerFile(String filename) {
        try {
            // Get the original bpm as this isn't saved in the drummer file
            int bpm = mainActivityInterface.getDrumViewModel().getThisBpm();

            // 1. Get the URI for the file
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Drummer", "", filename);

            // 2. Read the JSON string from the stream
            String jsonString = mainActivityInterface.getStorageAccess().readTextFileToString(
                    mainActivityInterface.getStorageAccess().getInputStream(uri));

            if (jsonString != null && !jsonString.isEmpty()) {
                // 3. Deserialize using the global Gson instance
                DrumPatternJson pattern = MainActivity.gson.fromJson(jsonString, DrumPatternJson.class);

                // 4. Update the ViewModel
                mainActivityInterface.getDrumViewModel().setDrumPatternJson(pattern);
                mainActivityInterface.getDrumViewModel().setCurrentPattern(pattern);
                mainActivityInterface.getDrumViewModel().updateAllTimingValues(pattern.getBeats(),pattern.getDivisions(),bpm);

                mainActivityInterface.getDrumViewModel().stopDrummer();
                updateActiveMap();

            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading custom drum pattern: " + filename, e);
            // Fallback to default if loading fails
            mainActivityInterface.getDrumViewModel().prepareSongValues(mainActivityInterface.getSong());
        }

    }

    public void saveDrummerFile(String filename) {
        // Reassign the file to the current one
        mainActivityInterface.getDrumViewModel().setDrumPatternJson(mainActivityInterface.getDrumViewModel().getCurrentPattern().getValue());

        //Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Drummer","",filename);
        //mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,uri,null,"Drummer","",filename);
        String gsonString = MainActivity.gson.toJson(mainActivityInterface.getDrumViewModel().getCurrentPattern().getValue());
        //mainActivityInterface.getStorageAccess().writeFileFromString(gsonString,mainActivityInterface.getStorageAccess().getOutputStream(uri));
        mainActivityInterface.getStorageAccess().writeFileFromString("Drummer","",filename,gsonString);
    }

    public String getNiceNameFromFilename(String filename) {
        String niceName = filename.replace(".json","");
        String timeSig = "";
        String[] parts = niceName.split("_");
        // We should have a minimum of 3 parts (more if underscore in name)
        if (parts.length >=3) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i <= parts.length - 3; i++) {
                // We only need to do this in case the name intentionally has underscores in it
                stringBuilder.append(parts[i]).append(" ");
            }
            niceName = stringBuilder.toString().trim();
            timeSig = "(" + parts[parts.length - 2] + "/" + parts[parts.length - 1]+")";
        }
        return niceName + " " + timeSig;
    }

    public String getFilenameFromNiceName(String niceName) {
        // Replace the last ) with .json
        String filename = niceName.replace(")",".json");
        // Replace the ' (' with _ and the '/' with _
        filename = filename.replace(" (", "_");
        filename = filename.replace("/", "_");
        if (!filename.endsWith(".json")) {
            // Final check for .json ending
            filename = filename + ".json";
        }
        return filename;
    }

    public String getFilenameFromBasics(String name, String timeSig) {
        timeSig = timeSig==null ? "" : timeSig.replace("/","_");
        return name + "_" + timeSig + ".json";
    }

    public String getNiceNameFromBasics(String name, String timeSig) {
        timeSig = timeSig==null ? "" : " (" + timeSig + ")";
        return name + timeSig;
    }

    public String getCajonPrefixIfNeeded() {
        // The asset filenames need Cajon_ at the start if we are using the percussion kit
        if (drummerStyle==null || drummerStyle.isEmpty()) {
            drummerStyle = "Acoustic";
        }
        if (drummerStyle.equals("Acoustic") || drummerStyle.equals("Standard")) {
            return "";
        } else {
            return "Cajon_";
        }
    }
}

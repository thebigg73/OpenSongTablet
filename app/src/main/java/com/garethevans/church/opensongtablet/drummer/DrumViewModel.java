package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.metronome.Metronome;
import com.garethevans.church.opensongtablet.metronome.MetronomeWearOS;
import com.garethevans.church.opensongtablet.midi.MidiClock;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class DrumViewModel extends ViewModel {
    private final String TAG = "DrumViewModel";

    private MainActivityInterface mainActivityInterface;

    // Core Engine Components
    private TimerEngine timerEngine;
    private DrumSoundManager drumSoundManager;
    private Drummer drummer;
    private Metronome metronome;
    private MidiClock midiClock;
    private MetronomeWearOS metronomeWearOS;

    // State Observables for UI
    private final MutableLiveData<Integer> currentStep = new MutableLiveData<>(-1);
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<DrumSection> activeSection = new MutableLiveData<>(DrumSection.MAIN);
    private final MutableLiveData<DrumPatternJson> currentPattern = new MutableLiveData<>();

    // Song values (current song, next song and temporary song (for saving, etc)
    private int thisBpm = 120;
    private int thisBeats = -1;
    private int thisDivisions = -1;
    private int thisPulsesPerStep = 4;
    private int thisStepsPerBar;
    private int thisStepsPerPulse = 4;
    private long thisBeatDuration = 100;
    private final MutableLiveData<Boolean> drummerRunning = new MutableLiveData<>(false);
    private DrumPatternJson drumPatternJson;

    public void initialiseDrums(Context c) {
        mainActivityInterface = (MainActivityInterface) c;

        // Initialise the classes
        this.metronome = new Metronome(c);
        this.drummer = new Drummer(c);
        this.timerEngine = new TimerEngine(c);
        this.drumSoundManager = new DrumSoundManager(c);
        this.midiClock = new MidiClock(c);
        this.metronomeWearOS = new MetronomeWearOS(c);
        // Check for a physical connection (optional, for UI feedback)
        checkWearOSValid();

        timerEngine.setOnStepListener(totalSteps -> {
            // Check if the song has actually been loaded/initialized
            if (thisStepsPerBar <= 0) {
                return;
            }

            // Pass the RAW totalSteps to the drummer so it can sync
            if (drummer != null && drummer.getIsRunning()) {
                drummer.onStep(totalSteps);
            }

            // Calculate the wrapped step ONLY for the metronome and UI
            int stepInBar = totalSteps % thisStepsPerBar;

            // 1. Prevent divide by zero and handle BPM safely
            int safeBpm = Math.max(thisBpm, 1);

            // 2. Get the beat duration
            thisBeatDuration = DrumCalculations.getBeatDurationMs(safeBpm, thisDivisions);

            // 3. Audio & Metronome Logic
            if (metronome != null && metronome.getIsRunning() && thisStepsPerPulse > 0) {
                metronome.onStep(stepInBar, thisStepsPerBar, thisBeatDuration);

                // 2. WearOS Haptic (NEW)
                if (metronomeWearOS!=null && metronomeWearOS.getWearOSValid() &&
                        metronomeWearOS.getMetronomeWearOS() && metronomeWearOS.getIsRunning()) {
                    // This runs on a background task so it won't block the audio timing
                    metronomeWearOS.sendBeat(stepInBar==0);
                }

                metronome.setVisualListener((beatNumber, isAccent, thisBeatDuration) -> {
                    int color = isAccent ? metronome.getTickColor() : metronome.getTockColor();

                    mainActivityInterface.getMainHandler().post(() -> {
                        if (mainActivityInterface.getToolbar() != null) {
                            mainActivityInterface.getToolbar().highlightBeat(beatNumber, color, thisBeatDuration);
                        }
                    });
                });
            }
            // Update UI for sequencer
            currentStep.postValue(stepInBar);
        });
    }

    // Get the helper classes
    public DrumSoundManager getDrumSoundManager() {
        return drumSoundManager;
    }
    public Metronome getMetronome() {
        return metronome;
    }
    public Drummer getDrummer() {
        return drummer;
    }
    public MidiClock getMidiClock() {
        return midiClock;
    }
    public MetronomeWearOS getMetronomeWearOS() {
        return metronomeWearOS;
    }

    // The current song values (can be changed from the metronome fragment)
    /**
     @param song the song to be processed for tempo and time signature
     */
    public void prepareSongValues(Song song) {
        // Only proceed if we aren't editing the sequence
        boolean loadedDrummer = false;
        if (!drummer.getSequencerMode()) {
            // Work out the values for the loaded song.  Called after song has settled, metronome starts
            if (metronome != null) {
                metronome.checkTickTockColors();
            }

            if (song != null) {
                if (song.getDrummer()!=null && !song.getDrummer().isEmpty()) {
                    // Check the file exists
                    Uri drummerUri = mainActivityInterface.getStorageAccess().getUriForItem("Drummer", "", song.getDrummer());
                    if (mainActivityInterface.getStorageAccess().uriExists(drummerUri)) {
                        drummer.loadDrummerFile(song.getDrummer());
                        loadedDrummer = true;
                    }
                }

                if (song.getDrummerKit()==null || song.getDrummerKit().isEmpty()) {
                    drummer.setDrummerStyle("Acoustic");
                } else if (song.getDrummerKit().equals("Acoustic") || song.getDrummerKit().equals("Standard")) {
                    drummer.setDrummerStyle("Acoustic");
                } else if (song.getDrummerKit().equals("Percussion") || song.getDrummerKit().equals("Cajon")) {
                    drummer.setDrummerStyle("Percussion");
                } else {
                    drummer.setDrummerStyle("Acoustic");
                }

                if (loadedDrummer) {
                    thisBeats = getDrumPatternJson().getBeats();
                    thisDivisions = getDrumPatternJson().getDivisions();
                    setCurrentPattern(drumPatternJson);
                } else {
                    int[] sig = DrumCalculations.getFixedTimeSignature(song.getTimesig());
                    thisBeats = sig[0];
                    thisDivisions = sig[1];
                }
                thisBpm = DrumCalculations.getFixedTempo(song.getTempo(), true);
            }
        }

        // 1. Update the interval for the audio click (Metronome)
        // This replaces the old fixed 60000 / bpm math
        thisBeatDuration = DrumCalculations.getBeatDurationMs(thisBpm, thisDivisions);

        // 4. Bar Math
        thisPulsesPerStep = DrumCalculations.getPulsesPerStep(thisDivisions);
        thisStepsPerPulse = DrumCalculations.getStepsPerPulse(thisDivisions);
        thisStepsPerBar = DrumCalculations.getTotalStepsInBar(thisBeats, thisDivisions);


        // Make sure the visual metronome has the correct number of beats
        mainActivityInterface.getToolbar().setUpMetronomeBar(thisBeats);

        if (!drummer.getSequencerMode() && !loadedDrummer) {
            // Build an empty drum pattern
            buildEmptyPattern();

            // Now add in the default pattern
            addDefaultPattern();
        }

        // Update the drummer class and timer class to use this as the basis
        updateDrummerAndTimer();
    }

    public void buildEmptyPattern() {
        // FORCE REBUILD: Reset the pattern object so it's fresh for the new song
        drumPatternJson = new DrumPatternJson(
                drumSoundManager.getKit().getDrumParts(),
                this.thisStepsPerBar, thisBeats, thisDivisions
        );
    }
    public void addDefaultPattern() {
        // Repopulate with the default patterns for the new time signature
        DrumPatternBuilder.buildStandardPattern(
                drumPatternJson,
                this.thisBeats,
                this.thisDivisions,
                this.thisStepsPerPulse
        );
    }
    public void updateDrummerAndTimer() {
        // Update the drummer with the new pattern
        if (drummer != null) {
            drummer.setPattern(drumPatternJson);
            setCurrentPattern(drumPatternJson);
        }

        // Update the timer
        if (timerEngine != null) {
            // This now updates both BPM and the 6/8 vs 4/4 resolution in one go
            timerEngine.refresh(thisBpm, thisPulsesPerStep);
            timerEngine.resetTickCounter();
        }
    }
    public void setThisBpm(int thisBpm) {
        this.thisBpm = thisBpm;
        if (timerEngine != null) {
            timerEngine.setBpm(thisBpm);
        }
    }
    public int getThisBpm() {
        return thisBpm;
    }
    public void setThisBeats(int thisBeats) {
        this.thisBeats = thisBeats;
    }
    public int getThisBeats() {
        return thisBeats;
    }
    public void setThisDivisions(int thisDivisions) {
        this.thisDivisions = thisDivisions;
    }
    public int getThisDivisions() {
        return thisDivisions;
    }
    public int getThisStepsPerBar() {
        return thisStepsPerBar;
    }
    public int getThisStepsPerPulse() {
        return thisStepsPerPulse;
    }

    // Metronome control
    public void toggleMetronome() {
        if (metronome.getIsRunning()) {
            stopMetronome();
        } else {
            startMetronome();
        }
    }
    public void startMetronome() {
        Log.d(TAG, "startMetronome()");
        metronome.setIsRunning(true);
        metronome.resetTotalStepsProcessed();

        // 1. Prepare values
        if (!drummer.getIsRunning()) {
            // The drummer hadn't sorted this, so make sure we do it now
            prepareSongValues(mainActivityInterface.getSong());
        }

        // 2. Pre-calculate the duration ONCE here, not in the loop
        thisBeatDuration = DrumCalculations.getBeatDurationMs(thisBpm, thisDivisions);

        // 3. Configure Metronome state
        metronome.setIsRunning(true);

        // 4. Attach Listeners (Better to move these to a constructor, but keep them here if you prefer)
        timerEngine.setOnStepListener(totalSteps -> {
            if (thisStepsPerBar <= 0) return;
            int stepInBar = totalSteps % thisStepsPerBar;

            if (metronome != null && metronome.getIsRunning()) {
                metronome.onStep(stepInBar, thisStepsPerBar, thisBeatDuration);
            }

            if (drummer !=null && drummer.getIsRunning()) {
                drummer.onStep(stepInBar);
            }

            currentStep.postValue(stepInBar);
        });

        metronome.setVisualListener((beatNumber, isAccent, duration) -> {
            int color = isAccent ? metronome.getTickColor() : metronome.getTockColor();
            mainActivityInterface.getMainHandler().post(() -> {
                if (mainActivityInterface.getToolbar() != null) {
                    // Keep your +1 mapping if Metronome sends 0-based,
                    // or just 'beatNumber' if it sends 1-based.
                    mainActivityInterface.getToolbar().highlightBeat(beatNumber, color, duration);
                }
            });
        });

        // 5. Engine Config
        timerEngine.setBpm(thisBpm);
        timerEngine.setPulsesPerStep(thisPulsesPerStep);

        // 6. UI Prep
        mainActivityInterface.getToolbar().setUpMetronomeBar(thisBeats);
        isPlaying.postValue(true);

        // 7. Start
        // --- FINAL STEP: Start Engine with a tiny "Warm-up" delay ---
        // This gives the SoundPool and the UI thread time to settle
        mainActivityInterface.getMainHandler().postDelayed(() -> {
            if (metronome.getIsRunning()) { // Double-check we haven't stopped already
                timerEngine.resetTickCounter();
                timerEngine.start();
            }
        }, 100); // 100ms is perfect for audio stabilization
    }
    public void stopMetronome() {
        Log.d(TAG,"stopMetronome()");

        // Tell the metronome we are no longer running
        metronome.setIsRunning(false);
        metronome.resetTotalStepsProcessed();

        // Check if we need to stop the timerEngine
        stopTimerEngine();

        isPlaying.postValue(false);
        currentStep.postValue(-1); // Reset playhead

        // Clear the toolbar visual
        mainActivityInterface.getToolbar().makeAllBeatsTransparent();

        // Fix the metronome start/stop icon if available
        metronome.updateStartStopButton();
    }

    public void checkWearOSValid() {
        if (metronomeWearOS != null) {
            metronomeWearOS.checkWearConnection(connected -> {
                Log.d(TAG, "WearOS Connection status: " + connected);
                // If you have a fragment reference, you can update its UI here
                if (metronome.getMetronomeFragment() != null) {
                    metronome.getMetronomeFragment().updateWearOS(connected);
                    metronomeWearOS.setWearOSValid(connected);
                }
            });
        }
    }

    // MIDI clock control
    public void startMidiClock() {
        // 1. Prepare values
        prepareSongValues(mainActivityInterface.getSong());
        midiClock.setIsRunning(true);
        timerEngine.setBpm(thisBpm);
        timerEngine.setPulsesPerStep(thisPulsesPerStep);

        startTimerEngine();
    }
    public void stopMidiClock() {
        midiClock.setIsRunning(false);
        stopTimerEngine();
    }

    // Drummer control
    public void startDrummer() {
        Log.d(TAG,"startDrummer()");
        prepareSongValues(mainActivityInterface.getSong());

        if (drummer != null) {
            drummer.setPattern(getDrumPatternJson()); // Ensure this method exists and sets the internal pattern
        }

        if (drummer != null) {
            // 1. Check engine state BEFORE we do anything else
            boolean metronomeAlreadyPlaying = metronome.getIsRunning();

            // 2. Configure Drummer based on that check
            if (!metronomeAlreadyPlaying) {
                // This is a FRESH START
                timerEngine.resetTickCounter();
                drummer.setIsCountIn(!drummer.getSequencerMode());
                drummer.setStartStep(0);

            } else {
                // This is JOINING mid-stream
                drummer.setIsCountIn(false);

                // Sync logic
                int currentTotalSteps = timerEngine.getTickCounter() / thisPulsesPerStep;
                int stepInPulse = currentTotalSteps % thisStepsPerPulse;
                drummer.setStartStep(currentTotalSteps + (thisStepsPerPulse - stepInPulse));
            }

            // 3. Set standard flags
            drummer.setIsRunning(true);
            isPlaying.postValue(true);
            drummerRunning.postValue(true);

            timerEngine.setBpm(thisBpm);
            timerEngine.setPulsesPerStep(thisPulsesPerStep);

            // 4. NOW start the engine only if it wasn't already running
            if (!metronomeAlreadyPlaying) {
                timerEngine.start();
            }
        }
    }
    public void stopDrummer() {
        Log.d(TAG, "stopDrummer()");
        drummer.setIsRunning(false);
        drummerRunning.postValue(false);
        drummer.setIsCountIn(!drummer.getSequencerMode());

        stopTimerEngine();

        // Only reset UI if nothing else is playing
        if (!metronome.getIsRunning()) {
            isPlaying.postValue(false);
            currentStep.postValue(-1);
        }
    }
    public void toggleDrummer() {
        boolean newState = !drummer.getIsRunning();
        drummer.setIsRunning(newState);
        drummerRunning.postValue(newState);

        if (newState) {
            startDrummer();
        } else {
            stopTimerEngine();
        }
    }
    public void drummerFill() {
        int currStep = 0;
        if (currentStep!=null && currentStep.getValue()!=null) {
            currStep = currentStep.getValue();
        }
        drummer.fill(currStep);
    }
    public void drummerTransition() {
        drummer.transition();
    }
    public LiveData<Boolean> getIsDrummerPlaying() {
        return isPlaying;
    }
    public void updateAllTimingValues(int thisBeats, int thisDivisions, int thisBpm) {
        this.thisBeats = thisBeats;
        this.thisDivisions = thisDivisions;
        this.thisBpm = thisBpm;
        thisBeatDuration = DrumCalculations.getBeatDurationMs(thisBpm, thisDivisions);
        thisPulsesPerStep = DrumCalculations.getPulsesPerStep(thisDivisions);
        thisStepsPerPulse = DrumCalculations.getStepsPerPulse(thisDivisions);
        thisStepsPerBar = DrumCalculations.getTotalStepsInBar(thisBeats, thisDivisions);
        currentStep.postValue(0);
        updateDrummerAndTimer();
    }


    public DrumPatternJson getDrumPatternJson() {
        // Build the default pattern if it doesn't exist yet
        if (drumPatternJson == null && thisBeats > 0) {
            drumPatternJson = new DrumPatternJson(drumSoundManager.getKit().getDrumParts(), thisStepsPerBar, thisBeats, thisDivisions);
            DrumPatternBuilder.buildStandardPattern(drumPatternJson, thisBeats, thisDivisions, thisStepsPerPulse);
        }
        return drumPatternJson;
    }
    public void setDrumPatternJson(DrumPatternJson drumPatternJson) {
        this.drumPatternJson = drumPatternJson;
        setCurrentPattern(drumPatternJson);
    }
    public LiveData<Integer> getCurrentStep() {
        return currentStep;
    }
    public void updateStepCount(int step) {
        // Correct: call .postValue() on the LiveData object
        currentStep.postValue(step);
    }
    public LiveData<DrumSection> getActiveSection() {
        return activeSection;
    }
    public void updateActiveSection(DrumSection section) {
        // postValue ensures the UI thread picks up the change from the Drummer thread
        activeSection.postValue(section);
    }
    public MutableLiveData<DrumPatternJson> getCurrentPattern() {
        return currentPattern;
    }
    public void setCurrentPattern(DrumPatternJson pattern) {
        currentPattern.postValue(pattern);
    }


    // The timer engine control - use for all!
    // Ensure the engine starts/stops based on all active components
    public void startTimerEngine() {
        // Only start if not already running
        if (!timerEngine.isRunning()) {
            timerEngine.setBpm(thisBpm);
            timerEngine.setPulsesPerStep(thisPulsesPerStep);
            timerEngine.start();
        }
    }
    public void stopTimerEngine() {
        // ONLY stop the physical engine if BOTH are now off
        boolean metronomeActive = (metronome != null && metronome.getIsRunning());
        boolean drummerActive = (drummer != null && drummer.getIsRunning());
        boolean midiClockActive = (midiClock != null && midiClock.getIsRunning());

        if (!metronomeActive && !drummerActive && !midiClockActive) {
            timerEngine.stop();
            isPlaying.postValue(false);
        }
    }

    public void stopAll() {
        // 1. UI: Tell the toolbar to clear and set isPlaying to false
        if (mainActivityInterface.getToolbar() != null) {
            mainActivityInterface.getToolbar().makeAllBeatsTransparent();
        }
        isPlaying.postValue(false);

        // 2. Logic: Mark the objects as stopped
        metronome.setIsRunning(false);
        drummer.setIsRunning(false);
        midiClock.setIsRunning(false);

        // 3. Hardware: Shutdown the high-precision executor
        timerEngine.stop();
    }


}

package com.garethevans.church.opensongtablet.metronome;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.ActionBar;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.screensetup.AppActionBar;

public class Metronome {

    // This object holds all of the metronome activity

    private final String TAG = "Metronome";
    private final int sampleRate = 8000;
    private long beatTimeNext;
    private long postDelayedTime;
    private int beatTimeLength;
    private int beats;
    private int divisions;
    private int metronomeFlashOnColor;
    private int metronomeFlashOffColor;
    private int beatsRequired;
    private int beatsRunningTotal;
    private int beat;
    private float volumeLeft = 0.0f, volumeRight = 0.0f;
    private boolean visualMetronome = false, isRunning = false, validTimeSig = false, validTempo = false;
    private double[] tick, tock;
    private final AudioGenerator audioGenerator = new AudioGenerator(sampleRate);
    private Activity activity;  // For run on UI updates
    private Handler metronomeHandler;
    private Thread metronomeThread;
    private ActionBar actionBar;
    private AppActionBar appActionBar;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (beatsRunningTotal <= beatsRequired) {
                try {

                    // If this is beat 1, it is a tick, if not a tock
                    double[] sound;
                    if (beat == 1) {
                        sound = tick;
                    } else {
                        sound = tock;
                    }
                    // Play the sound after getting the time
                    setBeatSystemTimes();
                    audioGenerator.writeSound(volumeLeft, volumeRight, sound);

                    if (visualMetronome) {
                        appActionBar.doFlash(beatTimeLength/2);
                    }
                    // Set the correct beat
                    beat++;
                    beatsRunningTotal++;
                    if (beat > divisions) {
                        beat = 1;
                    }

                    // This calculates how long from the now until we need to start the beeps again.
                    // If it is a negative number, it will be sent immediately!!
                    // Get the current time again as we will be later after playing the sound
                    postDelayedTime = beatTimeNext - System.currentTimeMillis();
                    if (postDelayedTime < 0) {
                        postDelayedTime = 0;
                    }

                    while (isRunning) {
                        metronomeHandler.postAtTime(this, postDelayedTime);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public boolean getIsRunning() {
        return isRunning;
    }
    public void startMetronome(Activity activity, Context c, MainActivityInterface mainActivityInterface) {
        // This starts the metronome activity
        this.activity = activity;  // Destroyed on stop
        initialiseMetronome(c, mainActivityInterface);

        // First beep & actionbar update
        if (visualMetronome) {
            mainActivityInterface.getAppActionBar().doFlash(metronomeFlashOnColor);
        } else {
            mainActivityInterface.getAppActionBar().doFlash(metronomeFlashOffColor);
        }

        if (metronomeValid()) {
            isRunning = true;
            postDelayedTime = 0;
            metronomeHandler = new Handler(Looper.getMainLooper());
            metronomeThread = new Thread(runnable);
            metronomeThread.start();
            //metronomeHandler.postAtTime(runnable, postDelayedTime);
        }
    }

    public void stopMetronome() {
        activity = null;
        isRunning = false;
    }

    private void initialiseMetronome(Context c, MainActivityInterface mainActivityInterface) {
        // Does the user want the visual metronome?
        setVisualMetronome(c, mainActivityInterface);

        // Get the volume and pan of the metronome and bars required
        setVolumes(c, mainActivityInterface);

        // Get the song tempo and time signatures
        // TODO remove
        mainActivityInterface.getSong().setTimesig("4/4");
        mainActivityInterface.getSong().setTempo("100");
        setSongValues(mainActivityInterface);

        // Get the bars and beats required
        setBarsAndBeats(c, mainActivityInterface);

        // Now get the audioPlayer ready
        audioGenerator.createPlayer(volumeLeft, volumeRight);

        // Get the tick and tock samples
        setTickTock(mainActivityInterface);

        // Set beatLast and beatNext times using the system timer
        setBeatSystemTimes();
    }
    private void setVisualMetronome(Context c, MainActivityInterface mainActivityInterface) {
        visualMetronome = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean(c, "metronomeShowVisual", false);

        metronomeFlashOffColor = c.getResources().getColor(R.color.colorAltPrimary);
        metronomeFlashOnColor = mainActivityInterface.getMyThemeColors().getMetronomeColor();

        mainActivityInterface.getAppActionBar().setMetronomeColors(metronomeFlashOnColor,metronomeFlashOffColor);
    }
    private void setVolumes(Context c, MainActivityInterface mainActivityInterface) {
        String pan = mainActivityInterface.getPreferences().getMyPreferenceString(c,"metronomePan","C");
        float vol = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "metronomeVol",0.5f);
        switch (pan) {
            case "C":
            default:
                volumeLeft = vol;
                volumeRight = vol;
                break;
            case "L":
                volumeLeft = vol;
                volumeRight = 0.0f;
                break;
            case "R":
                volumeLeft = 0.0f;
                volumeRight = vol;
        }
    }
    private void setSongValues(MainActivityInterface mainActivityInterface) {
        // First up the tempo
        validTempo = false;
        String t = mainActivityInterface.getSong().getTempo();
        // Check for text version from desktop app
        t = t.replace("Very Fast", "140");
        t = t.replace("Fast", "120");
        t = t.replace("Moderate", "100");
        t = t.replace("Slow", "80");
        t = t.replace("Very Slow", "60");
        t = t.replaceAll("[\\D]", "");
        int tempo;
        try {
            tempo = (short) Integer.parseInt(t);
            validTempo = true;
        } catch (NumberFormatException nfe) {
            tempo = 0;
        }

        // Check the tempo is within the permitted range
        if (tempo <40 || tempo >299) {
            tempo = 0;
            validTempo = false;
        }

        if (tempo >0) {
            // Calculate the time between beats in ms
            // We base this on a 6th of a beat (IV) and then scale later depending on time sig
            beatTimeLength = Math.round(((60.0f / (float) tempo) * 1000.0f)/6.0f);
        } else {
            beatTimeLength = 0;
        }

        // Now the time signature

        // 3/4 3 beats per bar, each quarter notes
        // 3/8 3 beats per bar, each eigth notes
        // 6/8 2 beats per bar. each dotted quarter note
        // tempo is taken as being given for the beat of the time signature

        // Time signatures that I have
        // 2/2   2/4   3/2    3/4   3/8   4/4   5/4   5/8   6/4   6/8   7/4   7/8   1/4

        String ts = mainActivityInterface.getSong().getTimesig();
        if (ts!=null && !ts.isEmpty() && ts.contains("/") && ts.length()==3) {
            validTimeSig = true;
            try {
                beats = Integer.parseInt(ts.substring(0, 1));
                divisions = Integer.parseInt(ts.substring(2));
            } catch (Exception e) {
                // Badly formatted time signature
                validTimeSig = false;
                beats = 0;
                divisions = 1;  // So we don't divide by 0 accidentally!
            }
        } else {
            validTimeSig = false;
            beats = 0;
            divisions = 1;  // So we don't divide by 0 accidentally!
        }

        // IV  - Override for 6/8 compound time signature to give 3 sounds (triplet) per beat
        // Otherwise one sound per beat
        if (beats == 6 && divisions == 8) {
            beatTimeLength = beatTimeLength * 2;
            divisions = 2;
            tempo = tempo * 3;
        } else {
            beatTimeLength = beatTimeLength * 6;
        }
        // 1/4 timing should be single beats
        if (beats == 1) {
            beats = 4;
        }
    }
    private void setBarsAndBeats(Context c, MainActivityInterface mainActivityInterface) {
        int barsRequired = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "metronomeLength", 0);
        beatsRequired = barsRequired * beats;
    }
    private void setTickTock(MainActivityInterface mainActivityInterface) {
        int tickTockSamples = 600;
        int soundTock = 1200;
        if (mainActivityInterface.getSong().getTimesig().equals("1/4")) {
            tick = audioGenerator.getSineWave(tickTockSamples, sampleRate, soundTock);
            tock = audioGenerator.getSineWave(tickTockSamples, sampleRate, soundTock);
        } else {
            tick = audioGenerator.getSineWave(tickTockSamples, sampleRate, soundTock);
            int soundTick = 1600;
            tock = audioGenerator.getSineWave(tickTockSamples, sampleRate, soundTick);
        }
    }
    private void setBeatSystemTimes() {
        // This is based on the system clock to keep better sync
        long beatTimeLast = System.currentTimeMillis();
        beatTimeNext = beatTimeLast + beatTimeLength;
    }
    private boolean metronomeValid() {
        Log.d(TAG, "validTempo: "+validTempo);
        Log.d(TAG, "validTimeSig: "+validTimeSig);
        return validTempo && validTimeSig;
    }




}

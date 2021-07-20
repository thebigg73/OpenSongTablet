package com.garethevans.church.opensongtablet.metronome;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.Timer;
import java.util.TimerTask;

public class Metronome {

    // This object holds all of the metronome activity

    private final String TAG = "Metronome";
    private Activity activity;  // For run on UI updates
    private int beat, beats, divisions, beatTimeLength, beatsRequired, beatsRunningTotal,
            metronomeFlashOnColor, metronomeFlashOffColor;
    private float volumeTickLeft = 1.0f, volumeTickRight = 1.0f, volumeTockLeft = 1.0f, volumeTockRight = 1.0f;
    private boolean visualMetronome = false, isRunning = false, validTimeSig = false,
            validTempo = false, tickPlayerReady, tockPlayerReady;
    private String tickSound, tockSound;
    private MediaPlayer tickPlayer, tockPlayer;
    private Timer metronomeTimer, visualTimer;
    private TimerTask metronomeTimerTask, visualTimerTask;
    private final Handler metronomeTimerHandler = new Handler();
    private final Handler visualTimerHandler = new Handler();

    // The call to start and stop the metronome called from MainActivity
    public void startMetronome(Activity activity, Context c, MainActivityInterface mainActivityInterface) {
        // This starts the metronome activity
        Log.d(TAG,"Metronome started");
        this.activity = activity;  // Destroyed on stop to avoid memory leaks

        // Initialise the varibles
        initialiseMetronome(c, mainActivityInterface);

        Log.d(TAG,"metronomeValid()="+metronomeValid()+"  isRunning="+isRunning);
        // If the metronome is valid and not running, start. If not stop
        if (metronomeValid() && !isRunning){
            // Get the tick and tock sounds ready
            setupPlayers(c,mainActivityInterface);
        } else {
            stopMetronome(mainActivityInterface);
        }
    }
    public void stopMetronome(MainActivityInterface mainActivityInterface) {
        Log.d(TAG,"Metronome stopped");
        activity = null;
        isRunning = false;
        // Make sure the action bar resets to the off color
        mainActivityInterface.getAppActionBar().doFlash(metronomeFlashOffColor);

        // Stop the metronome timer stuff
        if (metronomeTimerTask != null) {
            metronomeTimerTask.cancel();
            metronomeTimerTask = null;
        }
        if (metronomeTimer != null) {
            metronomeTimer.cancel();
            metronomeTimer.purge();
        }

        // Stop the visual metronome timer stuff
        if (visualTimerTask!=null) {
            visualTimerTask.cancel();
            visualTimerTask = null;
        }
        if (visualTimer != null) {
            visualTimer.cancel();
            visualTimer.purge();
        }
    }

    // Set up the metronome values (tempo, time signature, user preferences, etc)
    private void initialiseMetronome(Context c, MainActivityInterface mainActivityInterface) {
        // Does the user want the visual metronome?
        setVisualMetronome(c, mainActivityInterface);

        // Reset the beats
        beatsRunningTotal = 0;
        beat = 1;

        // Get the volume and pan of the metronome and bars required
        setVolumes(c, mainActivityInterface);

        // Get the song tempo and time signatures
        setSongValues(mainActivityInterface);

        // Get the bars and beats required
        setBarsAndBeats(c, mainActivityInterface);
    }
    private void setupPlayers(Context c, MainActivityInterface mainActivityInterface) {
        setTickTockSounds(c,mainActivityInterface);

        tickPlayer = new MediaPlayer();
        tockPlayer = new MediaPlayer();
        tickPlayer.setOnPreparedListener(mediaPlayer -> {
            tickPlayerReady = true;
            checkPlayersReady(mainActivityInterface);
        });
        tockPlayer.setOnPreparedListener(mediaPlayer -> {
            tockPlayerReady = true;
            checkPlayersReady(mainActivityInterface);
        });
        tickPlayerReady = false;
        tockPlayerReady = false;
        try {
            if (tickSound!=null && !tickSound.isEmpty()) {
                AssetFileDescriptor tickFile = c.getAssets().openFd("metronome/" + tickSound + ".mp3");
                tickPlayer.setDataSource(tickFile.getFileDescriptor(), tickFile.getStartOffset(), tickFile.getLength());
                tickPlayer.prepareAsync();
            } else {
                tickPlayer = null;
            }
            if (tockSound!=null && !tockSound.isEmpty()) {
                AssetFileDescriptor tockFile = c.getAssets().openFd("metronome/" + tockSound + ".mp3");
                tockPlayer.setDataSource(tockFile.getFileDescriptor(), tockFile.getStartOffset(), tockFile.getLength());
                tockPlayer.prepareAsync();
            } else {
                tockPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setTickTockSounds(Context c, MainActivityInterface mainActivityInterface) {
        tickSound = mainActivityInterface.getPreferences().getMyPreferenceString(c,"metronomeTickSound","digital_high");
        tockSound = mainActivityInterface.getPreferences().getMyPreferenceString(c,"metronomeTockSound", "digital_low");
    }
    public void setVisualMetronome(Context c, MainActivityInterface mainActivityInterface) {
        visualMetronome = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean(c, "metronomeShowVisual", false);

        metronomeFlashOffColor = c.getResources().getColor(R.color.colorAltPrimary);
        metronomeFlashOnColor = mainActivityInterface.getMyThemeColors().getMetronomeColor();
    }
    public void setVolumes(Context c, MainActivityInterface mainActivityInterface) {
        String pan = mainActivityInterface.getPreferences().getMyPreferenceString(c,"metronomePan","C");
        float tickVol = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "metronomeTickVol",0.8f);
        float tockVol = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "metronomeTockVol",0.6f);
        volumeTickLeft = tickVol;
        volumeTickRight = tickVol;
        volumeTockLeft = tockVol;
        volumeTockRight = tockVol;
        switch (pan) {
            case "L":
                volumeTickRight = 0.0f;
                volumeTockRight = 0.0f;
                break;
            case "R":
                volumeTickLeft = 0.0f;
                volumeTockLeft = 0.0f;
                break;
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

        // Rather than getting the time, rounding then scaling the rounding error,
        // Deal with the timings here
        if (tempo >0) {
            // Calculate the time between beats in ms
            // We base this on a 6th of a beat (IV) and then scale later depending on time sig
            if (beats == 6 && divisions == 8) {
                beatTimeLength = Math.round(((60.0f / (float) tempo) * 1000.0f) / 3.0f);
                divisions = 2;
            } else {
                beatTimeLength = Math.round((60.0f / (float) tempo) * 1000.0f);
            }
        } else {
            beatTimeLength = 0;
        }

        // 1/4 timing should be single beats
        if (beats == 1) {
            beats = 4;
        }
    }
    public void setBarsAndBeats(Context c, MainActivityInterface mainActivityInterface) {
        int barsRequired = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "metronomeLength", 0);
        beatsRequired = barsRequired * beats;  // If 0, that's fine
    }

    // Checks to the metronome
    public boolean metronomeValid() {
        Log.d(TAG, "validTempo: "+validTempo);
        Log.d(TAG, "validTimeSig: "+validTimeSig);
        return validTempo && validTimeSig;
    }
    public boolean getIsRunning() {
        return isRunning;
    }
    private void checkPlayersReady(MainActivityInterface mainActivityInterface) {
        // Called when the mediaPlayer are prepared
        if (tickPlayerReady && tockPlayerReady) {
            timerMetronome(mainActivityInterface);
            if (visualMetronome) {
                timerVisual(mainActivityInterface);
            }
        }
    }

    // The metronome timers and runnables
    private void timerMetronome(MainActivityInterface mainActivityInterface) {
        isRunning = true;
        metronomeTimer = new Timer();
        metronomeTimerTask = new TimerTask() {
            public void run() {
                metronomeTimerHandler.post(() -> {

                    if (beat>divisions) {
                        beat = 1;
                    }
                    if (beat==1 && tickPlayer!=null) {
                        tickPlayer.seekTo(0);
                        tickPlayer.setVolume(volumeTickLeft,volumeTickRight);
                        tickPlayer.start();
                    } else if (beat!=1 && tockPlayer!=null) {
                        tockPlayer.seekTo(0);
                        tockPlayer.setVolume(volumeTockLeft,volumeTockRight);
                        tockPlayer.start();
                    }
                    if (visualMetronome) {
                        activity.runOnUiThread(() -> mainActivityInterface.getAppActionBar().doFlash(metronomeFlashOnColor));
                    }
                    beat ++;
                    beatsRunningTotal ++;
                    if (beatsRequired>0 && beatsRunningTotal>beatsRequired) {
                        // Stop the metronome (beats and visual)
                        stopMetronome(mainActivityInterface);
                    }
                });
            }
        };
        metronomeTimer.scheduleAtFixedRate(metronomeTimerTask, 0, beatTimeLength);
    }
    private void timerVisual(MainActivityInterface mainActivityInterface) {
        // The flash on is handled in the metronome.
        // This timer is runs half way through the beat to turn the flash off
        visualTimer = new Timer();
        visualTimerTask = new TimerTask() {
            public void run() {
                visualTimerHandler.post(() -> activity.runOnUiThread(() -> mainActivityInterface.getAppActionBar().doFlash(metronomeFlashOffColor)));
            }
        };
        visualTimer.scheduleAtFixedRate(visualTimerTask, beatTimeLength/2, beatTimeLength);
    }

}

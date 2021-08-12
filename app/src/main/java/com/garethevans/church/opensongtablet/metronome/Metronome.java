package com.garethevans.church.opensongtablet.metronome;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Metronome {

    // This object holds all of the metronome activity

    private final String TAG = "Metronome";
    private Activity activity;  // For run on UI updates
    private int beat, beats, divisions, beatTimeLength, beatsRequired, beatsRunningTotal,
            metronomeFlashOnColor, metronomeFlashOffColor;
    private float volumeTickLeft = 1.0f, volumeTickRight = 1.0f, volumeTockLeft = 1.0f,
            volumeTockRight = 1.0f, meterTimeDivision = 1.0f;
    private boolean visualMetronome = false, isRunning = false, validTimeSig = false,
            validTempo = false, tickPlayerReady, tockPlayerReady;
    private String tickSound, tockSound;
    private MediaPlayer tickPlayer, tockPlayer;
    private Timer metronomeTimer, visualTimer;
    private TimerTask metronomeTimerTask, visualTimerTask;
    private final Handler metronomeTimerHandler = new Handler();
    private final Handler visualTimerHandler = new Handler();
    private ArrayList<Integer> tickBeats;

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
        beatsRunningTotal = 1;
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
        if (tickPlayer!=null) {
            try {
                tickPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (tockPlayer!=null) {
            try {
                tockPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

        // This bit splits the time signature into beats and divisions
        // We then deal with compound time signatures and get a division factor
        // Compound and complex time signatures can have additional emphasis beats (not just beat 1)
        processTimeSignature(mainActivityInterface);
        meterTimeFactor(); // 1.0f for simple signatures, 2.0f or 3.0f for compound ones
        getEmphasisBeats();   // Always has beat 1, but can have more

        if (tempo >0) {
            beatTimeLength = Math.round(((60.0f / (float) tempo) * 1000.0f) / meterTimeDivision);
        } else {
            beatTimeLength = 0;
        }
    }
    public ArrayList<String> processTimeSignature(MainActivityInterface mainActivityInterface) {
        ArrayList<String> timeSignature = new ArrayList<>();
        String ts = mainActivityInterface.getSong().getTimesig();
        if (ts!=null && !ts.isEmpty() && ts.contains("/")) {
            validTimeSig = true;
            try {
                String[] splits = ts.split("/");
                beats = Integer.parseInt(splits[0]);
                divisions = Integer.parseInt(splits[1]);
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
        timeSignature.add(""+beats);
        timeSignature.add(""+divisions);
        return timeSignature;  // Used when editing
    }

    public float meterTimeFactor() {
        // Compound times are when beats are split into triplets (divide by 3)
        // Complex times are split differently, but usually into eighth notes (divide by 2)
        // All versions with x/1, x/2 and x/4 are simple time, so only some x/8 are compound
        // The if statements are explicit and could be simplified, but wanted to show all variations
        // The factor is used to divide the time
        meterTimeDivision = 1.0f;
        if (divisions==8) {
            if (beats==3 || beats==6 || beats==9 || beats==12) {
                meterTimeDivision = 3.0f;
            } else if (beats==5 || beats==7 || beats==11 || beats==13 || beats==15) {
                meterTimeDivision = 2.0f;
            }
            // beats==2 || beats==4 || beats==8 || beats==10 || beats==14 || beats==16
            // meterTimeDivision = 1.0f;
        }
        return meterTimeDivision;
    }

    public ArrayList<Integer> getEmphasisBeats() {
        // This is only necessary for compound times only
        tickBeats = new ArrayList<>();
        tickBeats.add(1);
        if (divisions==8) {
            if (beats==5 || beats==6 || beats==7 || beats==9 || beats==12 ||
                    beats==14 || beats==15) {
                tickBeats.add(4);
            }
            if (beats==8 || beats==10 || beats==11 || beats==13 || beats==16) {
                tickBeats.add(5);
            }
            if (beats==7) {
                tickBeats.add(6);
            }
            if (beats==9 || beats==12 || beats==14 || beats==15) {
                tickBeats.add(7);
            }
            if (beats==11 || beats==13) {
                tickBeats.add(8);
            }
            if (beats==10 || beats==16) {
                tickBeats.add(9);
            }
            if (beats==11 || beats==12 || beats==13 || beats==14 || beats==15) {
                tickBeats.add(10);
            }
            if (beats==13) {
                tickBeats.add(12);
            }
            if (beats==14 || beats==15 || beats==16) {
                tickBeats.add(13);
            }
        }
        return tickBeats;
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

                    if (beat>beats) {
                        beat = 1;
                    }
                    if (tickBeats.contains(beat) && tickPlayer!=null) {
                        tickPlayer.seekTo(0);
                        tickPlayer.setVolume(volumeTickLeft,volumeTickRight);
                        try {
                            tickPlayer.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (tockPlayer!=null) {
                        tockPlayer.seekTo(0);
                        tockPlayer.setVolume(volumeTockLeft,volumeTockRight);
                        try {
                            tockPlayer.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

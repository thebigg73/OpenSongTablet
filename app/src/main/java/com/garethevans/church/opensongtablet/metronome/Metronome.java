package com.garethevans.church.opensongtablet.metronome;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.graphics.ColorUtils;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDown;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Metronome {

    // This object holds all of the metronome activity
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final String TAG = "Metronome";
    private int beat;
    private int beats;
    private int beatVisual;
    private int divisions;
    private int beatTimeLength=0;
    private int beatsRequired;
    private int beatsRunningTotal;
    private int metronomeFlashOnColor;
    private int metronomeFlashOnColorDarker;
    private int tickClip;
    private int tockClip;
    private float volumeTickLeft = 1.0f, volumeTickRight = 1.0f, volumeTockLeft = 1.0f,
            volumeTockRight = 1.0f, meterTimeDivision = 1.0f;
    private boolean visualMetronome = false, isRunning = false, validTimeSig = false,
            validTempo = false, tickPlayerReady, tockPlayerReady, metronomeAutoStart;
    private String tickSound, tockSound;
    private SoundPool soundPool;
    private Timer metronomeTimer, visualTimerOn, visualTimerOff;
    private TimerTask metronomeTimerTask, visualTimerTaskOn, visualTimerTaskOff;
    private final Handler metronomeTimerHandler = new Handler();
    private final Handler visualTimerHandlerOn = new Handler();
    private final Handler visualTimerHandlerOff = new Handler();
    private ArrayList<Integer> tickBeats;

    private ExposedDropDown beatsView, divisionsView, timeSigView, tempoView;
    private long old_time = 0L;
    private int total_calc_bpm = 0, total_counts = 0;
    private Handler tapTempoHandlerCheck, tapTempoHandlerReset;
    private Runnable tapTempoRunnableCheck, tapTempoRunnableReset;


    public Metronome(Activity activity) {
        c = (Context) activity;
        mainActivityInterface = (MainActivityInterface) c;
        metronomeAutoStart = mainActivityInterface.getPreferences().getMyPreferenceBoolean("metronomeAutoStart",false);
    }
    // The call to start and stop the metronome called from MainActivity
    public void startMetronome() {
        // Initialise the varibles
        initialiseMetronome();
        mainActivityInterface.getToolbar().setUpMetronomeBar(beats);

        // If the metronome is valid and not running, start. If not stop
        if (metronomeValid() && !isRunning){
            // Get the tick and tock sounds ready
            setupPlayers();
        } else {
            stopMetronome();
        }
    }
    public void stopMetronome() {
        isRunning = false;
        stopTimers(false);

        // Make sure the action bar resets to the off color
        new Handler(Looper.getMainLooper()).postDelayed(() -> mainActivityInterface.getToolbar().hideMetronomeBar(),beatTimeLength);
        new Handler(Looper.getMainLooper()).postDelayed(() -> mainActivityInterface.getToolbar().hideMetronomeBar(),beatTimeLength*2L);

        // Clean up the soundPool
        if (soundPool!=null) {
            soundPool.release();
            soundPool = null;
        }
        mainActivityInterface.getToolbar().hideMetronomeBar();
    }

    // Set up the metronome values (tempo, time signature, user preferences, etc)
    private void initialiseMetronome() {
        // Does the user want the visual metronome?
        setVisualMetronome();

        // Reset the beats
        beatsRunningTotal = 1;
        beat = 1;
        beatVisual = 1;

        // Get the volume and pan of the metronome and bars required
        setVolumes();

        // Get the song tempo and time signatures
        setSongValues();

        // Get the bars and beats required
        setBarsAndBeats();
    }
    private void setupPlayers() {
        tickPlayerReady = false;
        tockPlayerReady = false;

        setTickTockSounds();

        setupSoundPool();

        try {
            if (tickSound!=null && !tickSound.isEmpty()) {
                AssetFileDescriptor tickFile = c.getAssets().openFd("metronome/" + tickSound + ".mp3");
                tickClip = soundPool.load(tickFile,0);
            }
            if (tockSound!=null && !tockSound.isEmpty()) {
                AssetFileDescriptor tockFile = c.getAssets().openFd("metronome/" + tockSound + ".mp3");
                tockClip = soundPool.load(tockFile,0);
            }
            soundPool.setOnLoadCompleteListener((soundPool, i, i1) -> {
                if (i == tickClip) {
                    tickPlayerReady = true;
                } else if (i == tockClip) {
                    tockPlayerReady = true;
                }
                checkPlayersReady();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTickTockSounds() {
        tickSound = mainActivityInterface.getPreferences().getMyPreferenceString("metronomeTickSound","digital_high");
        tockSound = mainActivityInterface.getPreferences().getMyPreferenceString("metronomeTockSound", "digital_low");
    }

    private void setupSoundPool() {
        int maxStreams = 8;
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                    .build();
            soundPool = new SoundPool.Builder().setMaxStreams(maxStreams)
                    .setAudioAttributes(audioAttributes).build();
        } else if (mainActivityInterface.getStorageAccess().lollipopOrLater()) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            soundPool = new SoundPool.Builder().setMaxStreams(maxStreams)
                    .setAudioAttributes(audioAttributes).build();
        } else {
            soundPool = new SoundPool(maxStreams,
                    AudioManager.STREAM_MUSIC, 0);
        }
    }
    public void setVisualMetronome() {
        visualMetronome = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean("metronomeShowVisual", false);

        metronomeFlashOnColor = mainActivityInterface.getMyThemeColors().getMetronomeColor();
        metronomeFlashOnColorDarker = ColorUtils.blendARGB(metronomeFlashOnColor, Color.BLACK, 0.3f);
    }
    public void setVolumes() {
        String pan = mainActivityInterface.getPreferences().getMyPreferenceString("metronomePan","C");
        float tickVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("metronomeTickVol",0.8f);
        float tockVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("metronomeTockVol",0.6f);
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
    public void setSongValues() {
        // First up the tempo
        int tempo;
        validTempo = false;
        String t = mainActivityInterface.getSong().getTempo();
        try {
            // Check for text version from desktop app
            t = t.replace("Very Fast", "140").
                    replace("Fast", "120").
                    replace("Moderate", "100").
                    replace("Slow", "80").
                    replace("Very Slow", "60").
                    replaceAll("[\\D]", "");
            try {
                tempo = (short) Integer.parseInt(t);
                validTempo = true;
            } catch (NumberFormatException nfe) {
                tempo = 0;
            }

            // Check the tempo is within the permitted range
            if (tempo <40 || tempo >300) {
                tempo = 0;
                validTempo = false;
            }

            // This bit splits the time signature into beats and divisions
            // We then deal with compound time signatures and get a division factor
            // Compound and complex time signatures can have additional emphasis beats (not just beat 1)
            processTimeSignature();
            meterTimeFactor(); // 1.0f for simple signatures, 2.0f or 3.0f for compound ones
            getEmphasisBeats();   // Always has beat 1, but can have more

            if (tempo >0) {
                beatTimeLength = Math.round(((60.0f / (float) tempo) * 1000.0f) / meterTimeDivision);
            } else {
                beatTimeLength = 0;
            }
        } catch (Exception e) {
            // Badly formatted tempo
        }
    }
    public ArrayList<String> processTimeSignature() {
        ArrayList<String> timeSignature = new ArrayList<>();
        String ts = mainActivityInterface.getSong().getTimesig();
        if (ts != null && ts.contains("/")) {
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

    public void getEmphasisBeats() {
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
        //Log.d(TAG,"tickBeats:"+tickBeats);
    }
    public void setBarsAndBeats() {
        int barsRequired = mainActivityInterface.getPreferences().getMyPreferenceInt("metronomeLength", 0);
        beatsRequired = barsRequired * beats;  // If 0, that's fine
    }

    // Checks to the metronome
    public boolean metronomeValid() {
        return validTempo && validTimeSig;
    }
    public boolean getIsRunning() {
        return isRunning;
    }
    private void checkPlayersReady() {
        // Called when the mediaPlayer are prepared
        if (tickPlayerReady && tockPlayerReady) {
            timerMetronome();
            if (visualMetronome) {
                timerVisual();
            }
        }
    }

    // The metronome timers and runnables
    private void timerMetronome() {
        isRunning = true;
        metronomeTimer = new Timer();

        metronomeTimerTask = new TimerTask() {
            public void run() {
                metronomeTimerHandler.post(() -> {
                    // For proof of scheduler
                    // Expected time is a running total of start time + beatTimeLength each loop
                    long t = System.currentTimeMillis();
                    long latency = t-startTime;
                    Log.d(TAG,"latency (ms):"+latency+"  System.currenTimeMillis():"+System.currentTimeMillis()+"  this event should be at:"+startTime + "  beatTimeLength:"+beatTimeLength);
                    if (beat>beats) {
                        beat = 1;
                    }
                    // If more than 60ms out - skip beat (too far out)
                    if (Math.abs(t-startTime)<=60) {
                        if (tickBeats.contains(beat)) {
                            if (soundPool != null) {
                                soundPool.play(tickClip, volumeTickLeft, volumeTickRight, 0, 0, 1);
                            }
                        } else {
                            if (soundPool != null) {
                                soundPool.play(tockClip, volumeTockLeft, volumeTockRight, 0, 0, 1);
                            }
                        }
                    }
                    beat ++;
                    beatsRunningTotal ++;

                    if (beatsRequired>0 && beatsRunningTotal>beatsRequired) {
                        // Stop the metronome (beats and visual)
                        stopMetronome();
                    }
                    startTime += beatTimeLength;
                });
            }
        };
        startTime = System.currentTimeMillis();
        metronomeTimer.scheduleAtFixedRate(metronomeTimerTask, 0, beatTimeLength);
    }
    private long startTime;
    private void timerVisual() {
        // The flash on and off are handled separately.
        // This timer off is runs half way through the beat to turn the flash off
        visualTimerOn = new Timer();
        visualTimerOff = new Timer();
        visualTimerTaskOn = new TimerTask() {
            public void run() {
                if (beatVisual>beats) {
                    beatVisual = 1;
                }
                visualTimerHandlerOn.removeCallbacks(visualTimerTaskOn);
                visualTimerHandlerOff.removeCallbacks(visualTimerTaskOff);
                if (tickBeats.contains(beatVisual)) {
                    mainActivityInterface.getToolbar().highlightBeat(beatVisual,metronomeFlashOnColor);
                } else {
                    mainActivityInterface.getToolbar().highlightBeat(beatVisual,metronomeFlashOnColorDarker);
                }
                beatVisual ++;
            }
        };

        visualTimerOn.scheduleAtFixedRate(visualTimerTaskOn,0,beatTimeLength);
    }

    public void stopTimers(boolean nullTimer) {
        Log.d(TAG,"stop called");
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
        if (visualTimerTaskOn!=null) {
            visualTimerTaskOn.cancel();
            visualTimerTaskOn = null;
        }
        if (visualTimerTaskOff!=null) {
            visualTimerTaskOff.cancel();
            visualTimerTaskOff = null;
        }
        if (visualTimerOn != null) {
            visualTimerOn.cancel();
            visualTimerOn.purge();
        }
        if (visualTimerOff != null) {
            visualTimerOff.cancel();
            visualTimerOff.purge();
        }

        if (nullTimer) {
            metronomeTimer = null;
            visualTimerOn = null;
            visualTimerOff = null;
        }
    }

    public void initialiseTapTempo(MaterialButton tapButton, ExposedDropDown timeSigView,
                                   ExposedDropDown beatsView, ExposedDropDown divisionsView,
                                   ExposedDropDown tempoView) {
        this.timeSigView = timeSigView;
        this.beatsView = beatsView;
        this.divisionsView = divisionsView;
        this.tempoView = tempoView;

        // Initialise the tapTempo values
        total_calc_bpm = 0;
        total_counts = 0;
        tapTempoRunnableCheck = () -> {
            // This is called after 2 seconds when a tap is initiated
            // Any previous instance is of course cancelled first
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    tapButton.setEnabled(false);
                    tapButton.setText(c.getString(R.string.reset));
                    tapButton.setBackgroundColor(c.getResources().getColor(R.color.colorPrimary));
                    // Waited too long, reset count
                    total_calc_bpm = 0;
                    total_counts = 0;
                });
            });
            if (tapTempoHandlerReset != null) {
                tapTempoHandlerReset.removeCallbacks(tapTempoRunnableReset);
            }
            tapTempoHandlerReset = new Handler();
            tapTempoHandlerReset.postDelayed(tapTempoRunnableReset, 500);
        };
        tapTempoRunnableReset = () -> {
            // Reset the tap tempo timer
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    tapButton.setEnabled(true);
                    tapButton.setText(c.getString(R.string.tap_tempo));
                    tapButton.setBackgroundColor(c.getResources().getColor(R.color.colorSecondary));
                });
            });
            // Start the metronome if we are in the metronome fragment where divisions isn't null
            if (divisionsView!=null) {
                startMetronome();
            }
        };
    }

    public void tapTempo() {
        // This function checks the previous tap_tempo time and calculates the bpm
        // Variables for tap tempo

        // When tapping for compound/complex time signatures
        // They sometimes go in double or triple time
        if (getIsRunning()) {
            stopMetronome();
        }

        long new_time = System.currentTimeMillis();
        long time_passed = new_time - old_time;
        int calc_bpm = Math.round((1 / ((float) time_passed / 1000)) * 60);

        // Need to decide on the time sig.
        // If it ends in /2, then double the tempo
        // If it ends in /4, then leave as is
        // If it ends in /8, then half it
        // If it isn't set, set it to default as 4/4
        String timeSig = mainActivityInterface.getSong().getTimesig();
        if (timeSig.isEmpty()) {
            if (beatsView!=null && divisionsView!=null) {
                beatsView.setText("4");
                divisionsView.setText("4");
            } else if (timeSigView!=null) {
                timeSigView.setText("4/4");
            }
            mainActivityInterface.getSong().setTimesig("4/4");
        }

        if (time_passed < 1500) {
            total_calc_bpm += calc_bpm;
            total_counts++;
        } else {
            // Waited too long, reset count
            total_calc_bpm = 0;
            total_counts = 0;
        }

        // Based on the time signature, get a meterDivisionFactor
        float meterTimeFactor = mainActivityInterface.getMetronome().meterTimeFactor();
        int av_bpm = Math.round(((float) total_calc_bpm / (float) total_counts) / meterTimeFactor);

        if (av_bpm < 300 && av_bpm >= 40) {
            tempoView.setText(""+av_bpm);
            mainActivityInterface.getSong().setTempo(""+av_bpm);

        } else if (av_bpm <40) {
            tempoView.setText("40");
            mainActivityInterface.getSong().setTempo("40");
        }  else {
            tempoView.setText("300");
            mainActivityInterface.getSong().setTempo("300");
        }

        old_time = new_time;

        // Set a handler to check the button tap.
        // If the counts haven't increased after 1.5 seconds, reset it
        if (tapTempoHandlerCheck!=null) {
            tapTempoHandlerCheck.removeCallbacks(tapTempoRunnableCheck);
        }
        tapTempoHandlerCheck = new Handler();
        tapTempoHandlerCheck.postDelayed(tapTempoRunnableCheck,1500);
    }

    public void setMetronomeAutoStart(boolean metronomeAutoStart) {
        this.metronomeAutoStart = metronomeAutoStart;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("metronomeAutoStart",metronomeAutoStart);
    }
    public boolean getMetronomeAutoStart() {
        return metronomeAutoStart;
    }
}

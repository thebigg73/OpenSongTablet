package com.garethevans.church.opensongtablet.pads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.textview.MaterialTextView;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Pad {
    private final String TAG = "Pad";
    public boolean orientationChanged;
    public boolean pad1Pause, pad2Pause;
    public float pad1FadeVolume, pad2FadeVolume;
    public int padInQuickFade;
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private MediaPlayer pad1, pad2;
    private boolean pad1Fading, pad2Fading;
    private int currentOrientation;
    private float pad1VolL, pad1VolR, pad2VolL, pad2VolR;
    private Timer pad1FadeTimer, pad2FadeTimer;
    private TimerTask pad1FadeTimerTask, pad2FadeTimerTask;
    private final Handler pad1FadeTimerHandler = new Handler();
    private final Handler pad2FadeTimerHandler = new Handler();
    private final LinearLayout pad;
    private final MaterialTextView padTime;
    private final MaterialTextView padTotalTime;
    private int padLength;
    private float pad1VolDrop, pad2VolDrop;
    private boolean padsActivated = false;
    private CharSequence padPauseTime;
    private Timer padPlayTimer;
    private TimerTask padPlayTimerTask;
    private final Handler padPlayTimerHandler = new Handler();

    public Pad(Context c, LinearLayout pad) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        this.pad = pad;
        padTime = pad.findViewById(R.id.padTime);
        padTotalTime = pad.findViewById(R.id.padTotalTime);
    }

    public void startPad() {
        // IV - managePads will fade all running pads
        // If padsActivated is true then it will start the new pad when a pad player is free
        padsActivated = true;
        managePads();
    }

    public void stopPad() {
        // IV - managePads will fade all running pads
        padsActivated = false;
        managePads();
        // IV - Configure padPlayTimer for stopping
        padTime.setText("");
        padTotalTime.setVisibility(View.GONE);
        padTotalTime.setText("Stopping");
    }

    private void stopAndReset(int padNum) {
        switch (padNum) {
            case 1:
                if (pad1 != null) {
                    try {
                        pad1Fading = false;
                        pad1Pause = false;
                        pad1.reset();
                        pad1.release();
                        pad1 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2:
                if (pad2 != null) {
                    try {
                        pad2Fading = false;
                        pad2Pause = false;
                        pad2.reset();
                        pad2.release();
                        pad2 = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private String keyToFlat(String key) {
        return key.replace("A#","Bb").replace("C#","Db")
                .replace("D#","Eb").replace("F#","Gb")
                .replace("G#","Ab");
    }

    public boolean isAutoPad() {
        String padFile = mainActivityInterface.getSong().getPadfile();
        String key = mainActivityInterface.getSong().getKey();
        return (padFile.isEmpty() || padFile.equals("auto") || padFile.equals(c.getString(R.string.pad_auto))) &&
                key!=null && !key.isEmpty();
    }

    public boolean isCustomAutoPad() {
        String key = mainActivityInterface.getSong().getKey();
        String customPad = mainActivityInterface.getPreferences().getMyPreferenceString("customPad"+keyToFlat(key),"");
        return isAutoPad() && customPad!=null && !customPad.isEmpty();
    }

    public boolean isLinkAudio() {
        String padFile = mainActivityInterface.getSong().getPadfile();
        String linkAudio = mainActivityInterface.getSong().getLinkaudio();
        return (padFile.equals("link") || padFile.equals(c.getString(R.string.link_audio))) &&
                linkAudio!=null && !linkAudio.isEmpty();
    }

    public Uri getPadUri() {
        Uri padUri = null;
        if (isCustomAutoPad()) {
            padUri = mainActivityInterface.getStorageAccess().fixLocalisedUri(
                    mainActivityInterface.getPreferences().getMyPreferenceString(
                            "customPad"+keyToFlat(mainActivityInterface.getSong().getKey()),""));
        } else if (isLinkAudio()) {
            padUri = mainActivityInterface.getStorageAccess().fixLocalisedUri(
                    mainActivityInterface.getSong().getLinkaudio());
        }
        // If none of the above, we assume an auto pad if the key has been set.
        // Since autopads are raw asset files, we use assetfiledescriptor instead.
        // A null padUri, will trigger that
        return padUri;
    }

    private boolean isPadValid(Uri padUri) {
        return mainActivityInterface.getStorageAccess().uriExists(padUri);
    }

    private void loadAndStart(int padNum) {
        try {
            Uri padUri = getPadUri();

            // If the padUri is null, we likely need a default autopad assuming the key is set
            AssetFileDescriptor assetFileDescriptor = null;
            if (padUri==null && !mainActivityInterface.getSong().getKey().isEmpty()) {
                assetFileDescriptor = getAssetPad(mainActivityInterface.getSong().getKey());
            }

            // Decide if pad should loop
            boolean padLoop = mainActivityInterface.getSong().getPadloop().equals("true");

            // Decide if the pad is valid
            boolean padValid = (assetFileDescriptor!=null || isPadValid(padUri)) &&
                    !mainActivityInterface.getSong().getPadfile().equals("off");

            // Prepare any error message
            if (!padValid) {
                if (mainActivityInterface.getSong().getKey().isEmpty()) {
                    mainActivityInterface.getShowToast().doIt(c.getString(R.string.pad_key_error));
                } else if (isCustomAutoPad()) {
                    mainActivityInterface.getShowToast().doIt(c.getString(R.string.pad_file_error));
                } else if (isLinkAudio()) {
                    mainActivityInterface.getShowToast().doIt(c.getString(R.string.pad_custom_pad_error));
                } else if (mainActivityInterface.getSong().getPadfile().equals("off")) {
                    mainActivityInterface.getShowToast().doIt(c.getString(R.string.pad_off));
                }
            }

            if (padValid) {
                switch (padNum) {
                    case 1:
                        pad1 = new MediaPlayer();
                        pad1.setOnCompletionListener(mediaPlayer -> stopPad());
                        pad1.setOnPreparedListener(mediaPlayer -> doPlay(1));
                        if (assetFileDescriptor != null) {
                            pad1.setDataSource(assetFileDescriptor.getFileDescriptor(),
                                    assetFileDescriptor.getStartOffset(),
                                    assetFileDescriptor.getLength());
                            assetFileDescriptor.close();
                        } else if (padUri != null) {
                            pad1.setDataSource(c, padUri);
                        }
                        pad1.setLooping(padLoop);
                        pad1.prepareAsync();
                        break;
                    case 2:
                        pad2 = new MediaPlayer();
                        pad2.setOnCompletionListener(mediaPlayer -> stopPad());
                        pad2.setOnPreparedListener(mediaPlayer -> doPlay(2));
                        if (assetFileDescriptor != null) {
                            pad2.setDataSource(assetFileDescriptor.getFileDescriptor(),
                                    assetFileDescriptor.getStartOffset(),
                                    assetFileDescriptor.getLength());
                            assetFileDescriptor.close();
                        } else if (padUri != null) {
                            pad2.setDataSource(c, padUri);
                        }
                        pad2.setLooping(padLoop);
                        pad2.prepareAsync();
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void managePads() {
        Log.d(TAG,"------");
        Log.d(TAG, "managePads pad1 is active '" + (pad1 != null) + "'. pad1 is fading '" + pad1Fading + "'.");
        Log.d(TAG, "managePads pad2 is active '" + (pad2 != null) + "'. pad2 is fading '" + pad2Fading + "'.");

        // Fade any running pads
        if ((pad1 != null && !pad1Fading) || (pad2 != null && !pad2Fading)) {
            // If both pads are fading choose one for 'quick fade'
            if (padInQuickFade == 0) {
                if (pad1Fading) {
                    padInQuickFade = 1;
                }
                if (pad2Fading) {
                    padInQuickFade = 2;
                }
            }
            Log.d(TAG, "Pad padInQuickFade " + padInQuickFade);

            final int fadeTime = mainActivityInterface.getPreferences().getMyPreferenceInt("padCrossFadeTime", 8000);
            int thisfadetime;

            if (pad1 != null && !pad1Fading) {
                // Set left and right volumes
                final float padVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("padVol", 1.0f);
                switch (mainActivityInterface.getPreferences().getMyPreferenceString("padPan", "C")) {
                    case "L":
                        pad1VolL = padVol;
                        pad1VolR = 0.0f;
                        break;
                    case "R":
                        pad1VolL = 0.0f;
                        pad1VolR = padVol;
                        break;
                    default:
                        pad1VolL = padVol;
                        pad1VolR = padVol;
                }

                // Determine fade time
                if (pad1Pause) {
                    thisfadetime = 1000;
                } else {
                    thisfadetime = fadeTime;
                }

                // How much to drop the vol by each step
                pad1VolDrop = padVol / 50;

                Log.d(TAG, "Pad1 fading");
                // IV - Needed here
                pad1Fading = true;
                pad1Pause = false;

                pad1FadeTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        pad1FadeTimerHandler.post(() -> {
                            // IV - Needed here
                            pad1Fading = true;
                            if (padInQuickFade == 1) {
                                Log.d(TAG,"Pad1 quick fading");
                                pad1VolDrop = pad1VolDrop * 5;
                                padInQuickFade = -1;
                            }
                            pad1VolL = newVol(pad1VolL, pad1VolDrop);
                            pad1VolR = newVol(pad1VolR, pad1VolDrop);
                            setVolume(1, pad1VolL, pad1VolR);
                            if (pad1VolL == 0 && pad1VolR == 0) {
                                // Finished fading
                                pad1Fading = false;
                                stopAndReset(1);
                                if (padInQuickFade == -1) {
                                    if (padsActivated) {
                                        Log.d(TAG, "Pad1 start requested after quick fade");
                                        loadAndStart(1);
                                    }
                                    padInQuickFade = 0;
                                }
                                endFadeTimer(1);
                            }
                            pad1FadeVolume = Math.max(pad1VolL, pad1VolR);
                        });
                    }
                };
                pad1FadeTimer = new Timer();
                pad1FadeTimer.scheduleAtFixedRate(pad1FadeTimerTask, 0, thisfadetime / 50);
            }

            if (pad2 != null && !pad2Fading) {
                // We do not double fade so the running pad will be at the settings volume
                final float padVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("padVol", 1.0f);
                // Set left and right volumes
                switch (mainActivityInterface.getPreferences().getMyPreferenceString("padPan", "C")) {
                    case "L":
                        pad2VolL = padVol;
                        pad2VolR = 0.0f;
                        break;
                    case "R":
                        pad2VolL = 0.0f;
                        pad2VolR = padVol;
                        break;
                    default:
                        pad2VolL = padVol;
                        pad2VolR = padVol;
                }

                // Determine fade time
                if (pad2Pause) {
                    thisfadetime = 1000;
                } else {
                    thisfadetime = fadeTime;
                }
                // How much to drop the vol by each step
                pad2VolDrop = padVol / 50;

                Log.d(TAG, "Pad2 Fading");
                // IV - Needed here
                pad2Fading = true;
                pad2Pause = false;

                pad2FadeTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        pad2FadeTimerHandler.post(() -> {
                            // IV - Needed here
                            pad2Fading = true;
                            if (padInQuickFade == 2) {
                                Log.d(TAG,"Pad2 quick fading");
                                pad2VolDrop = pad2VolDrop * 5;
                                padInQuickFade = -2;
                            }
                            pad2VolL = newVol(pad2VolL, pad2VolDrop);
                            pad2VolR = newVol(pad2VolR, pad2VolDrop);
                            setVolume(2, pad2VolL, pad2VolR);
                            if (pad2VolL == 0 && pad2VolR == 0) {
                                // Finished fading
                                pad2Fading = false;
                                stopAndReset(2);
                                if (padInQuickFade == -2) {
                                    if (padsActivated) {
                                        Log.d(TAG, "Pad2 start requested after quick fade");
                                        loadAndStart(2);
                                    }
                                    padInQuickFade = 0;
                                }
                                endFadeTimer(2);
                            }
                            pad2FadeVolume = Math.max(pad2VolL, pad2VolR);
                        });
                    }
                };
                pad2FadeTimer = new Timer();
                pad2FadeTimer.scheduleAtFixedRate(pad2FadeTimerTask, 0, thisfadetime / 50);
            }
        }

        // IV - If there is a pad timer in quick fade and padsActivated is true, it will start the pad at the end of the quick fade
        if (padInQuickFade == 0) {
            if (padsActivated) {
                if (!pad1Fading) {
                    Log.d(TAG, "Pad1 start requested. Pad free for immediate use.");
                    loadAndStart(1);
                } else {
                    Log.d(TAG, "Pad2 start requested. Pad free for immediate use.");
                    loadAndStart(2);
                }
            }
        }
    }

    private AssetFileDescriptor getAssetPad(String key) {
        // Using the built in pad
        key = key.replace("Ab","G#");
        key = key.replace("Bb","A#");
        key = key.replace("Db","C#");
        key = key.replace("Eb","D#");
        key = key.replace("Gb","F#");
        key = key.replace("#","sharp");
        key = key.toLowerCase(Locale.ROOT);

        int path = c.getResources().getIdentifier(key.toLowerCase(Locale.ROOT), "raw", c.getPackageName());
        return c.getResources().openRawResourceFd(path);
    }
    private void endFadeTimer(int padNum) {
        switch (padNum) {
            case 1:
                if (pad1FadeTimerTask!=null) {
                    pad1FadeTimerTask.cancel();
                    pad1FadeTimerTask = null;
                }
                if (pad1FadeTimer != null) {
                    pad1FadeTimer.cancel();
                    pad1FadeTimer.purge();
                }
                pad1Fading = false;
                break;
            case 2:
                if (pad2FadeTimerTask!=null) {
                    pad2FadeTimerTask.cancel();
                    pad2FadeTimerTask = null;
                }
                if (pad2FadeTimer != null) {
                    pad2FadeTimer.cancel();
                    pad2FadeTimer.purge();
                }
                pad2Fading = false;
                break;
        }
    }
    public void setVolume(int padNum, float volL, float volR) {
        switch (padNum) {
            case 1:
                if (pad1!=null) {
                    try {
                        pad1.setVolume(volL, volR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2:
                if (pad2!=null) {
                    try {
                        pad2.setVolume(volL, volR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
    }
    private float newVol(float currVol, float dropVol) {
        currVol = currVol - dropVol;
        if (currVol < 0) {
            return 0.0f;
        } else {
            return currVol;
        }
    }
    @SuppressLint("SetTextI18n")
    private void doPlay(int padNum) {
        try {
            Log.d(TAG,"doPlay Pad" + padNum);
            switch (padNum) {
                case 1:
                    padLength = (int)(pad1.getDuration()/1000f);
                    pad1Fading = false;
                    pad1Pause = false;
                    pad1.start();
                    break;
                case 2:
                    padLength = (int)(pad2.getDuration()/1000f);
                    pad2Fading = false;
                    pad2Pause = false;
                    pad2.start();
                    break;
            }
            pad.setOnClickListener(v -> playStopOrPause(padNum));
            pad.setOnLongClickListener(v -> longClick(padNum));

            // IV - We setup the on-screen timer display
            padTime.setText("0:00");
            padTotalTime.setText(" / " + mainActivityInterface.getTimeTools().timeFormatFixer(padLength));
            padTotalTime.setVisibility(View.VISIBLE);
            pad.setVisibility(View.VISIBLE);

            // IV - Schedule a new timer when the timer not already running
            if (padPlayTimerTask == null) {
                padPlayTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        padPlayTimerHandler.post(() -> {
                            // IV - If stopping
                            if (padTotalTime.getText().equals("Stopping")) {
                                if ((pad1 == null || !pad1.isPlaying()) && (pad2 == null || !pad2.isPlaying())) {
                                    stopPadPlayTimer();
                                }
                            } else {
                                // IV - A pad should be active - if not start one!
                                if (pad1 == null && pad2 == null) {
                                    Log.d(TAG, "Pad cross fade fail! Requesting pad start.");
                                    startPad();
                                }

                                // IV - If paused
                                if (pad1Pause || pad2Pause) {
                                    padTime.setText(padPauseTime);
                                    if (padTime.getCurrentTextColor() == Color.TRANSPARENT) {
                                        padTime.setTextColor(Color.WHITE);
                                    } else {
                                        padTime.setTextColor(Color.TRANSPARENT);
                                    }
                                // IV - If running normally
                                } else {
                                    String text = "0:00";
                                    try {
                                        if (pad1 != null && pad1.isPlaying() && !pad1Fading) {
                                            text = mainActivityInterface.getTimeTools().timeFormatFixer((int) (pad1.getCurrentPosition() / 1000f));
                                        }
                                    } catch (Exception e) {
                                        // Nothing to do
                                    }
                                    try {
                                        if (pad2 != null && pad2.isPlaying() && !pad2Fading) {
                                            text = mainActivityInterface.getTimeTools().timeFormatFixer((int) (pad2.getCurrentPosition() / 1000f));
                                        }
                                    } catch (Exception e) {
                                        // Nothing to do
                                    }
                                    padTime.setText(text);
                                    padTime.setTextColor(Color.WHITE);
                                }
                            }
                        });
                    }
                };
                padPlayTimer = new Timer();
                padPlayTimer.scheduleAtFixedRate(padPlayTimerTask, 1000, 1000);
            }
            mainActivityInterface.updateOnScreenInfo("showhide");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Get info about the pads
    public boolean isPadPlaying() {
        return (pad1!=null && pad1.isPlaying() && !pad1Fading) || (pad2!=null && pad2.isPlaying() && !pad2Fading);
    }
    public boolean isPadPrepared() {
        return padTime.getText()!=null && !padTime.getText().toString().isEmpty() &&
                ((pad1!=null && pad1.getDuration()>0 && (pad1.isPlaying() || pad1Pause)) ||
                (pad2!=null && pad2.getDuration()>0 && (pad2.isPlaying() || pad2Pause)));
    }
    public void autoStartPad() {
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("padAutoStart",false) && padsActivated) {
            startPad();
        }
    }
    private void playStopOrPause(int padNum) {
        switch (padNum) {
            case 1:
                pad1Pause = false;
                if (pad1!=null && pad1.isPlaying() && pad1Fading) {
                    // Just stop the pad
                    stopAndReset(1);
                    padsActivated = false;
                } else if (pad1!=null && pad1.isPlaying() && !pad1Fading) {
                    // Pause the pad
                    pad1.pause();
                    pad1Pause = true;
                    padPauseTime = padTime.getText();
                } else if (pad1!=null && !pad1Fading) {
                    pad1.start();
                }
                break;
            case 2:
                pad2Pause = false;
                if (pad2!=null && pad2.isPlaying() && pad2Fading) {
                    // Just stop the pad
                    stopAndReset(2);
                    padsActivated = false;
                } else if (pad2!=null && pad2.isPlaying() && !pad2Fading) {
                    // Pause the pad
                    pad2.pause();
                    pad2Pause = true;
                    padPauseTime = padTime.getText();
                } else if (pad2!=null && !pad2Fading) {
                    pad2.start();
                }
                break;
        }
    }

    public void panicStop() {
        // Emergency stop all pads - no fade
        stopAndReset(1);
        stopAndReset(2);
        stopPadPlayTimer();
        padsActivated = false;
    }

    // Orientation changes
    public void setOrientationChanged(boolean orientationChanged) {
        this.orientationChanged = orientationChanged;
    }
    public boolean getOrientationChanged() {
        return orientationChanged;
    }
    public void setCurrentOrientation(int currentOrientation) {
        this.currentOrientation = currentOrientation;
    }
    public int getCurrentOrientation() {
        return currentOrientation;
    }

    private boolean longClick(int padNum) {
        stopAndReset(padNum);
        stopPadPlayTimer();
        return true;
    }

    public void stopPadPlayTimer() {
        if (padPlayTimerTask != null) {
            padPlayTimerTask.cancel();
            padPlayTimerTask = null;
        }
        if (padPlayTimer != null) {
            padPlayTimer.cancel();
            padPlayTimer.purge();
        }
        padTime.setText("");
        padTotalTime.setText("");
        pad.setVisibility(View.GONE);
    }
}
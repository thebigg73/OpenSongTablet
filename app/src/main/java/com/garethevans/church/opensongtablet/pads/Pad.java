package com.garethevans.church.opensongtablet.pads;

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
    public boolean orientationChanged;
    public boolean pad1Pause, pad2Pause;
    public int padInQuickFade;
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Pad";
    private final MediaPlayer pad1 = new MediaPlayer();
    private final MediaPlayer pad2 = new MediaPlayer();
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
    private boolean pad1Prepared, pad2Prepared;
    private final String pad_auto, link_audio, pad_key_error, pad_file_error,
            pad_custom_pad_error, pad_off, stopping;

    public Pad(Context c, LinearLayout pad) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        this.pad = pad;
        padTime = pad.findViewById(R.id.padTime);
        padTotalTime = pad.findViewById(R.id.padTotalTime);
        pad_auto = c.getString(R.string.pad_auto);
        link_audio = c.getString(R.string.link_audio);
        pad_key_error = c.getString(R.string.pad_key_error);
        pad_file_error = c.getString(R.string.pad_file_error);
        pad_custom_pad_error = c.getString(R.string.pad_custom_pad_error);
        pad_off = c.getString(R.string.pad_off);
        stopping = c.getString(R.string.stopping);
    }

    public void startPad() {
        Log.d("TAG","managePads StartPad");
        // IV - managePads will fade all running pads
        // managePads will start the new pad if/when a pad player is free
        padsActivated = true;
        managePads();
    }

    public void stopPad() {
        Log.d(TAG,"managePads StopPad");
        padsActivated = false;
        // IV - managePads fades all running pads
        managePads();
        stopPadPlay();
    }

    private void stopAndReset(int padNum) {
        switch (padNum) {
            case 1:
                try {
                    pad1Pause = false;
                    if (pad1Prepared) {
                        pad1.stop();
                    }
                    pad1.reset();
                    pad1Prepared = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    pad2Pause = false;
                    if (pad2Prepared) {
                        pad2.stop();
                    }
                    pad2.reset();
                    pad2Prepared = false;
                } catch (Exception e) {
                    e.printStackTrace();
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
        if (padFile == null) {
            padFile = "";
        }
        String key = mainActivityInterface.getSong().getKey();
        if (key == null) {
            key = "";
        }
        return (padFile.isEmpty() || padFile.equals("auto") || padFile.equals(pad_auto)) && !key.isEmpty();
    }

    public boolean isCustomAutoPad() {
        String key = mainActivityInterface.getSong().getKey();
        if (key == null) {
            key = "";
        }
        String customPad = "";
        if (!key.isEmpty()) {
            customPad = mainActivityInterface.getPreferences().getMyPreferenceString("customPad" + keyToFlat(key) ,"");
        }
        return isAutoPad() && customPad!=null && !customPad.isEmpty();
    }

    public boolean isLinkAudio() {
        String padFile = mainActivityInterface.getSong().getPadfile();
        if (padFile == null) {
            padFile = "";
        }
        String linkAudio = mainActivityInterface.getSong().getLinkaudio();
        return (padFile.equals("link") || padFile.equals(link_audio)) &&
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
        Uri padUri = getPadUri();
        String padFile = mainActivityInterface.getSong().getPadfile();
        if (padFile == null) {
            padFile = "";
        }
        String key = mainActivityInterface.getSong().getKey();
        if (key == null) {
            key = "";
        }

        // If the padUri is null, we likely need a default autopad assuming the key is set
        AssetFileDescriptor assetFileDescriptor = null;
        if (padUri==null && !key.isEmpty()) {
            assetFileDescriptor = getAssetPad(key);
        }

        // Decide if pad should loop
        String padLoop = mainActivityInterface.getSong().getPadloop();

        // Decide if the pad is valid
        boolean padValid = (assetFileDescriptor!=null || isPadValid(padUri)) &&
                !padFile.equals("off");

        // Prepare any error message
        if (!padValid) {
            if (key.isEmpty()) {
                mainActivityInterface.getShowToast().doIt(pad_key_error);
            } else if (isCustomAutoPad()) {
                mainActivityInterface.getShowToast().doIt(pad_file_error);
            } else if (isLinkAudio()) {
                mainActivityInterface.getShowToast().doIt(pad_custom_pad_error);
            } else if (padFile.equals("off")) {
                mainActivityInterface.getShowToast().doIt(pad_off);
            }
            stopPadPlay();
        }

        if (padValid) {
            switch (padNum) {
                case 1:
                    stopAndReset(1);
                    pad1Prepared = false;
                    pad1.setOnCompletionListener(mediaPlayer -> {
                        stopAndReset(1);
                        stopPadPlay();
                    });
                    pad1.setOnPreparedListener(mediaPlayer -> {
                        pad1Prepared = true;
                        doPlay(1);
                    });
                    if (assetFileDescriptor != null) {
                        try {
                            pad1.setDataSource(assetFileDescriptor.getFileDescriptor(),
                                    assetFileDescriptor.getStartOffset(),
                                    assetFileDescriptor.getLength());
                            assetFileDescriptor.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (padUri != null) {
                        try {
                            pad1.setDataSource(c, padUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    pad1.setLooping(padLoop != null && padLoop.equals("true"));
                    pad1.prepareAsync();
                    pad1.setOnErrorListener((mp, what, extra) -> {
                        // IV - Could not prepare pad - try again!
                        Log.d(TAG, "managePads Pad1 Fail in prepare of MediaPlayer! Try again.");
                        if (padsActivated) {
                            stopAndReset(1);
                            loadAndStart(1);
                        }
                        return false;
                    });
                    break;
                case 2:
                    stopAndReset(2);
                    pad2Prepared = false;
                    pad2.setOnCompletionListener(mediaPlayer -> {
                        stopAndReset(2);
                        stopPadPlay();
                    });
                    pad2.setOnPreparedListener(mediaPlayer -> {
                        pad2Prepared = true;
                        doPlay(2);
                    });
                    if (assetFileDescriptor != null) {
                        try {
                            pad2.setDataSource(assetFileDescriptor.getFileDescriptor(),
                                    assetFileDescriptor.getStartOffset(),
                                    assetFileDescriptor.getLength());
                            assetFileDescriptor.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (padUri != null) {
                        try {
                            pad2.setDataSource(c, padUri);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    pad2.setLooping(padLoop != null && padLoop.equals("true"));
                    pad2.prepareAsync();
                    pad2.setOnErrorListener((mp, what, extra) -> {
                        // IV - Could not prepare pad - try again!
                        Log.d(TAG, "managePads Pad2 Fail in prepare of MediaPlayer! Try again.");
                        if (padsActivated) {
                            stopAndReset(2);
                            loadAndStart(2);
                        }
                        return false;
                    });
                    break;
            }
        }
    }
    private void managePads() {
        Log.d(TAG, "managePads ------");
        Log.d(TAG, "managePads PadActivated " + padsActivated);
        Log.d(TAG, "managePads Pad1 is active '" + (pad1 != null) + "'. pad1 is fading '" + (pad1FadeTimerTask != null) + "'.");
        Log.d(TAG, "managePads Pad2 is active '" + (pad2 != null) + "'. pad2 is fading '" + (pad2FadeTimerTask != null) + "'.");

        // IV - If pad is free, not playing or fading, use it . If not free, fade it.
        if (!pad1.isPlaying() && pad1FadeTimerTask == null) {
            if (padsActivated) {
                Log.d(TAG, "managePads Pad1 Start requested as free for use.");
                loadAndStart(1);
            }
        } else if (pad1FadeTimerTask == null) {
            Log.d(TAG, "managePads Pad1 Fading");
            // IV - Reset the pad volumes from preferences - pad1VolL and pad1VolR are set
            setVolume(1, -1, -1);

            // IV - We are starting a fade of the active pad, if the other pad is fading set it to 'quick fade'
            if (pad2FadeTimerTask != null && padInQuickFade == 0) {
                padInQuickFade = 2;
            }

            pad1FadeTimerTask = new TimerTask() {
                @Override
                public void run() {
                    pad1FadeTimerHandler.post(() -> {
                        if (!(pad1VolL == 0 && pad1VolR == 0)) {
                            // IV - Fading
                            // IV - If quick fade has been requested
                            if (padInQuickFade == 1) {
                                Log.d(TAG, "managePads Pad1 Quick fade started");
                                pad1VolDrop = pad1VolDrop * 3;
                                padInQuickFade = -1;
                            }
                            // IV - Give a gentle end to the fade
                            if (Math.max(pad1VolL,pad1VolR) < 0.05f) {
                                pad1VolDrop = 0.01f;
                            }
                            // IV - Set lower volume. pad1VolL and pad1VolR are set to the new values.
                            setVolume(1, newVol(pad1VolL, pad1VolDrop), newVol(pad1VolR, pad1VolDrop));
                        } else {
                            // IV - Faded
                            stopAndReset(1);
                            Log.d(TAG, "managePads Pad1 stopped");
                            // If both pads are fading this is the first to finish, start the new pad if needed
                            if (pad2FadeTimerTask !=null && padsActivated) {
                                Log.d(TAG, "managePads Pad1 start requested after quick fade");
                                loadAndStart(1);
                            }
                            padInQuickFade = 0;
                            Log.d(TAG, "managePads Pad1 endFadeTimer");
                            new Handler().post(() -> endFadeTimer(1));
                        }
                    });
                }
            };

            // How much to drop the vol by each step
            pad1VolDrop = Math.max(pad1VolL,pad1VolR) / 20;

            pad1FadeTimer = new Timer();
            pad1FadeTimer.scheduleAtFixedRate(pad1FadeTimerTask, 0,
                    mainActivityInterface.getPreferences().getMyPreferenceInt("padCrossFadeTime", 8000) / 20);
        }

        // IV - If pad is free, not playing or fading, use it . If not free, fade it.
        if (!pad2.isPlaying() && pad2FadeTimerTask == null) {
            // IV - If pad1 is fading then consider starting pad2
            if (pad1FadeTimerTask !=null && padsActivated) {
                Log.d(TAG, "managePads Pad2 Start requested as free for use");
                loadAndStart(2);
            }
        } else if (pad2FadeTimerTask == null) {
            Log.d(TAG, "managePads Pad2 Fading");
            // IV - Reset the pad volumes from preferences - pad2VolL and pad2VolR are set
            setVolume(2, -1, -1);

            // IV - We are starting a fade of the active pad, if the other pad is fading set it to 'quick fade'
            if (pad1FadeTimerTask != null && padInQuickFade == 0) {
                padInQuickFade = 1;
            }

            pad2FadeTimerTask = new TimerTask() {
                @Override
                public void run() {
                    pad2FadeTimerHandler.post(() -> {
                        if (!(pad2VolL == 0 && pad2VolR == 0f)) {
                            // IV - Fading
                            // IV - If quick fade has been requested
                            if (padInQuickFade == 2) {
                                Log.d(TAG, "managePads Pad2 Quick fade started");
                                pad2VolDrop = pad2VolDrop * 3;
                                padInQuickFade = -2;
                            }
                            // IV - Give a gentle end to the fade
                            if (Math.max(pad2VolL,pad2VolR) < 0.05f) {
                                pad2VolDrop = 0.01f;
                            }
                            // IV - Set lower volume. pad2VolL and pad2VolR are set to the new values.
                            setVolume(2, newVol(pad2VolL, pad2VolDrop), newVol(pad2VolR, pad2VolDrop));
                        } else {
                            // IV - Faded
                            stopAndReset(2);
                            Log.d(TAG, "managePads Pad2 Stopped");
                            // If both pads are fading this is the first to finish, start the new pad if needed
                            if (pad1FadeTimerTask !=null  && padsActivated) {
                                Log.d(TAG, "managePads Pad2 start requested after quick fade");
                                loadAndStart(2);
                            }
                            padInQuickFade = 0;
                            Log.d(TAG, "managePads Pad2 endFadeTimer");
                            new Handler().post(() -> endFadeTimer(2));
                        }
                    });
                }
            };

            // How much to drop the vol by each step
            pad2VolDrop = Math.max(pad2VolL,pad2VolR) / 20;

            pad2FadeTimer = new Timer();
            pad2FadeTimer.scheduleAtFixedRate(pad2FadeTimerTask, 0,
            mainActivityInterface.getPreferences().getMyPreferenceInt("padCrossFadeTime", 8000) / 20);
        }

        Log.d(TAG, ("managePads Pad" + padInQuickFade + " Quick fading").replace("Pad0 Quick f","No Quick f").replace("-",""));
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
                break;
        }
    }
    private void setVolume(int padNum, float volL, float volR) {
        // IV - This function always updates padNVolL and padNVolR (N is padNum) with the set values
        // IV - '-1' will set a volume using preferences
        if (volL == -1 || volR == -1) {
            final float padVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("padVol", 1.0f);
            final String padPan = mainActivityInterface.getPreferences().getMyPreferenceString("padPan", "C");
            if (volL == -1) {
                volL = "CL".contains(padPan) ? padVol : 0;
            }
            if (volR == -1)
                volR = "CR".contains(padPan) ? padVol : 0;
        }

        switch (padNum) {
            case 1:
                try {
                    pad1.setVolume(volL, volR);
                    pad1VolL = volL;
                    pad1VolR = volR;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    pad2.setVolume(volL, volR);
                    pad2VolL = volL;
                    pad2VolR = volR;
                } catch (Exception e) {
                    e.printStackTrace();
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
    private void doPlay(int padNum) {
        Log.d(TAG,"managePads doPlay Pad" + padNum);
        switch (padNum) {
            case 1:
                pad1Pause = false;
                // IV - Set to the preference volumes
                setVolume(1,-1,-1);
                if (pad1Prepared) {
                    pad1.start();
                    padLength = (int) (pad1.getDuration() / 1000f);
                }
                break;
            case 2:
                pad2Pause = false;
                // IV - Set to the preference volumes
                setVolume(2,-1,-1);
                if (pad2Prepared) {
                    pad2.start();
                    padLength = (int) (pad2.getDuration() / 1000f);
                }
                break;
        }
        pad.setOnClickListener(v -> playStopOrPause(padNum));
        pad.setOnLongClickListener(v -> panicStop());

        // IV - We setup the on-screen timer display
        padTime.setText(mainActivityInterface.getTimeTools().timeFormatFixer(0));
        String total = " / " + mainActivityInterface.getTimeTools().timeFormatFixer(padLength);
        padTotalTime.setText(total);
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
                            if ((pad1 == null || (!pad1.isPlaying() && pad1FadeTimerTask == null)) && (pad2 == null || (!pad2.isPlaying() && pad2FadeTimerTask == null))) {
                                stopPadPlayTimer();
                            }
                        } else {
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
                                    if (pad1 != null && pad1Prepared && pad1.isPlaying() && pad1FadeTimerTask == null) {
                                        text = mainActivityInterface.getTimeTools().timeFormatFixer((int) (pad1.getCurrentPosition() / 1000f));
                                    }
                                    if (pad2 != null && pad2Prepared && pad2.isPlaying() && pad2FadeTimerTask == null) {
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
    }

    // Get info about the pads
    public boolean isPadPlaying() {
        return (pad1!=null && pad1Prepared && pad1.isPlaying() && pad1FadeTimerTask == null) || (pad2!=null && pad2Prepared && pad2.isPlaying() && (pad2FadeTimerTask == null));
    }
    public boolean isPadPrepared() {
        return padTime.getText()!=null && !padTime.getText().toString().isEmpty() &&
                ((pad1!=null && pad1Prepared && (pad1.isPlaying() || pad1Pause)) && pad1.getDuration()>0 ||
                (pad2!=null && pad2Prepared && (pad2.isPlaying() || pad2Pause) && pad2.getDuration()>0));
    }
    public void autoStartPad() {
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("padAutoStart",false) && padsActivated) {
            startPad();
        }
    }
    private void playStopOrPause(int padNum) {
        // IV - Both pads, active and fading, are considered
        switch (padNum) {
            case 1:
                if (pad1!=null && pad1Prepared && pad1.isPlaying() && pad1FadeTimerTask != null) {
                    panicStop();
                } else if (pad1!=null && pad1Prepared && pad1.isPlaying() && pad1FadeTimerTask == null) {
                    // Stop any fading pad
                    stopAndReset(2);
                    // Pause the active pad
                    pad1.pause();
                    pad1Pause = true;
                    padPauseTime = padTime.getText();
                } else if (pad1!=null && pad1Prepared && pad1FadeTimerTask == null) {
                    pad1.start();
                    pad1Pause = false;
                }
                break;
            case 2:
                pad2Pause = false;
                if (pad2!=null && pad2Prepared && pad2.isPlaying() && pad2FadeTimerTask != null) {
                    panicStop();
                } else if (pad2!=null && pad2Prepared && pad2.isPlaying() && pad2FadeTimerTask == null) {
                    // Stop any fading pad
                    stopAndReset(1);
                    // Pause the active pad
                    pad2.pause();
                    pad2Pause = true;
                    padPauseTime = padTime.getText();
                } else if (pad2!=null && pad2Prepared && pad2FadeTimerTask == null) {
                    pad2.start();
                    pad2Pause = false;
                }
                break;
        }
    }

    public boolean panicStop() {
        // Stop all pads - no fade
        stopAndReset(1);
        stopAndReset(2);
        stopPadPlayTimer();
        padsActivated = false;
        return true;
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
    
    private void stopPadPlay() {
        // IV - Configure padPlayTimer for stopping
        padTime.setText("");
        padTotalTime.setVisibility(View.GONE);
        padTotalTime.setText(stopping);
    }
}
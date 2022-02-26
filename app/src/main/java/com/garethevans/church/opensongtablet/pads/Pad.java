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

    private MediaPlayer pad1, pad2;
    private boolean pad1Fading, pad2Fading, orientationChanged;
    private int padToUse, currentOrientation;
    private float pad1VolL, pad1VolR, pad2VolL, pad2VolR;
    private Timer pad1PlayTimer, pad2PlayTimer, pad1FadeTimer, pad2FadeTimer;
    private TimerTask pad1PlayTimerTask, pad2PlayTimerTask, pad1FadeTimerTask, pad2FadeTimerTask;
    private final Handler pad1FadeTimerHandler = new Handler();
    private final Handler pad2FadeTimerHandler = new Handler();
    private final Handler pad1PlayTimerHandler = new Handler();
    private final Handler pad2PlayTimerHandler = new Handler();
    private boolean pad1Pause, pad2Pause;
    private final LinearLayout pad;
    private final MaterialTextView padTime;
    private final MaterialTextView padTotalTime;
    private int padLength;
    private int pad1CurrentTime = 0, pad2CurrentTime = 0;
    private final String TAG = "Pad";
    private int lastPadPlaying = 0;
    private float pad1VolDrop, pad2VolDrop;
    private final MainActivityInterface mainActivityInterface;
    private boolean padsActivated = false;

    public Pad(MainActivityInterface mainActivityInterface, LinearLayout pad) {
        this.mainActivityInterface = mainActivityInterface;
        this.pad = pad;
        padTime = pad.findViewById(R.id.padTime);
        padTotalTime = pad.findViewById(R.id.padTotalTime);
    }

    public void startPad(Context c) {
        // Decide which pad to fade (if any)
        padsActivated = true;
        whichPadToFade(c);
        endTimer(1);
        endTimer(2);
        stopAndReset(padToUse);
        loadAndStart(c, padToUse);
    }
    public void stopPad(Context c) {
        padsActivated = false;
        whichPadToFade(c);
    }

    private void stopAndReset(int padNum) {
        switch (padNum) {
            case 1:
                if (pad1 != null) {
                    try {
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

    public boolean isAutoPad(Context c) {
        String padFile = mainActivityInterface.getSong().getPadfile();
        String key = mainActivityInterface.getSong().getKey();
        return (padFile.isEmpty() || padFile.equals("auto") || padFile.equals(c.getString(R.string.pad_auto))) &&
                key!=null && !key.isEmpty();
    }

    public boolean isCustomAutoPad(Context c) {
        String key = mainActivityInterface.getSong().getKey();
        String customPad = mainActivityInterface.getPreferences().getMyPreferenceString("customPad"+keyToFlat(key),"");
        return isAutoPad(c) && customPad!=null && !customPad.isEmpty();
    }

    public boolean isLinkAudio(Context c) {
        String padFile = mainActivityInterface.getSong().getPadfile();
        String linkAudio = mainActivityInterface.getSong().getLinkaudio();
        return (padFile.equals("link") || padFile.equals(c.getString(R.string.link_audio))) &&
                linkAudio!=null && !linkAudio.isEmpty();
    }

    public Uri getPadUri(Context c) {
        Uri padUri = null;
        if (isCustomAutoPad(c)) {
            padUri = mainActivityInterface.getStorageAccess().fixLocalisedUri(
                    mainActivityInterface.getPreferences().getMyPreferenceString(
                            "customPad"+keyToFlat(mainActivityInterface.getSong().getKey()),""));
        } else if (isLinkAudio(c)) {
            padUri = mainActivityInterface.getStorageAccess().fixLocalisedUri(
                    mainActivityInterface.getSong().getLinkaudio());
        }
        // If none of the above, we assume an auto pad if the key has been set.
        // Since autopads are raw asset files, we use assetfiledescriptor instead.
        // A null padUri, will trigger that
        return padUri;
    }

    private boolean isPadValid(Context c, Uri padUri) {
        return mainActivityInterface.getStorageAccess().uriExists(padUri);
    }

    private void loadAndStart(Context c, int padNum) {
        Uri padUri = getPadUri(c);

        // If the padUri is null, we likely need a default autopad assuming the key is set
        AssetFileDescriptor assetFileDescriptor = null;
        if (padUri==null && !mainActivityInterface.getSong().getKey().isEmpty()) {
            assetFileDescriptor = getAssetPad(c,mainActivityInterface.getSong().getKey());
        }

        // Decide if pad should loop
        boolean padLoop = mainActivityInterface.getSong().getPadloop().equals("true");

        // Decide if the pad is valid
        boolean padValid = (assetFileDescriptor!=null || isPadValid(c,padUri)) &&
                !mainActivityInterface.getSong().getPadfile().equals("off");

        // Prepare any error message
        if (!padValid) {
            if (mainActivityInterface.getSong().getKey().isEmpty()) {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.pad_key_error));
            } else if (isCustomAutoPad(c)) {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.pad_file_error));
            } else if (isLinkAudio(c)) {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.pad_custom_pad_error));
            } else if (mainActivityInterface.getSong().getPadfile().equals("off")) {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.pad_off));
            }
        }

        if (padValid) {
            switch (padNum) {
                case 1:
                    pad1 = new MediaPlayer();
                    pad1.setOnCompletionListener(mediaPlayer -> {
                        stopAndReset(1);
                        endFadeTimer(1);
                        endTimer(1);
                    });
                    pad1.setOnPreparedListener(mediaPlayer -> doPlay(mainActivityInterface,1));
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
                    pad1.setLooping(padLoop);
                    pad1.prepareAsync();
                    break;
                case 2:
                    pad2 = new MediaPlayer();
                    pad2.setOnCompletionListener(mediaPlayer -> {
                        stopAndReset(2);
                        endFadeTimer(2);
                        endTimer(2);
                    });
                    pad2.setOnPreparedListener(mediaPlayer -> doPlay(mainActivityInterface, 2));
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
                    pad2.setLooping(padLoop);
                    pad2.prepareAsync();
                    break;
            }
        }
    }
    private void fadePad(Context c, int padNum) {
        endTimer(padNum);

        // Get the current volume
        final float padVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("padVol",1.0f);
        final int fadeTime = mainActivityInterface.getPreferences().getMyPreferenceInt("padCrossFadeTime",8000);

        // Set left and right volumes
        float padVolL = padVol;
        float padVolR = padVol;
        switch (mainActivityInterface.getPreferences().getMyPreferenceString("padPan","C")) {
            case "L":
                padVolR = 0.0f;
                break;
            case "R":
                padVolL = 0.0f;
                break;
        }
        setVolume(padNum, padVolL, padVolR);

        // Decide the time required between calls to run 20 fade events (smooth enough)
        final int delayTime = fadeTime / 20;

        switch (padNum) {
            case 1:
                // How much to drop the vol by each time over 20 steps
                pad1VolDrop = padVol / 20;
                endTimer(1);
                pad1FadeTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        pad1FadeTimerHandler.post(() -> {
                            float newLeft = newVol(pad1VolL, pad1VolDrop);
                            float newRight = newVol(pad1VolR, pad1VolDrop);
                            setVolume(1, newLeft, newRight);
                            if (newLeft == 0 && newRight == 0) {
                                // Finished fading
                                stopAndReset(1);
                                endFadeTimer(1);
                                pad1Fading = false;
                            }
                        });
                    }
                };
                pad1FadeTimer = new Timer();
                pad1FadeTimer.scheduleAtFixedRate(pad1FadeTimerTask,0,delayTime);
                pad1Fading = true;
                break;
            case 2:
                pad2VolDrop = padVol / 20;
                endTimer(2);
                pad2FadeTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        pad2FadeTimerHandler.post(() -> {
                            float newLeft = newVol(pad2VolL, pad2VolDrop);
                            float newRight = newVol(pad2VolR, pad2VolDrop);
                            setVolume(2, newLeft, newRight);
                            if (newLeft == 0 && newRight == 0) {
                                // Finished fading
                                stopAndReset(2);
                                endFadeTimer(2);
                                pad2Fading = false;
                            }
                        });
                    }
                };
                pad2FadeTimer = new Timer();
                pad2FadeTimer.scheduleAtFixedRate(pad2FadeTimerTask,0,delayTime);
                pad2Fading = true;
                break;
        }
    }

    private AssetFileDescriptor getAssetPad(Context c, String key) {
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
    private void endTimer(int padNum) {
        padTime.setText("");
        padTotalTime.setText("");
        pad.setVisibility(View.GONE);
        stopTimers(padNum);
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
    private void setVolume(int padNum, float volL, float volR) {
        switch (padNum) {
            case 1:
                pad1VolL = volL;
                pad1VolR = volR;
                if (pad1!=null) {
                    try {
                        pad1.setVolume(volL, volR);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2:
                pad2VolL = volL;
                pad2VolR = volR;
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
    private void doPlay(MainActivityInterface mainActivityInterface, int padNum) {
        lastPadPlaying = padNum;
        Log.d(TAG,"doPlay: padNum="+padNum);
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
        Log.d(TAG,"padLength:"+padLength);
        String padLengthText = " / " + mainActivityInterface.getTimeTools().timeFormatFixer(padLength);
        String display = mainActivityInterface.getTimeTools().timeFormatFixer(0);
        padTime.setText(display);
        padTotalTime.setText(padLengthText);

        switch (padNum) {
            case 1:
                pad1CurrentTime = 0;
                pad.setOnClickListener(v -> playStopOrPause(1));
                pad.setOnLongClickListener(v -> longClick(1));
                pad1PlayTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        pad1PlayTimerHandler.post(() -> {
                            if (pad1!=null && !pad1Pause) {
                                pad1CurrentTime = (int)(pad1.getCurrentPosition()/1000f);
                                //pad1CurrentTime++;
                                String display = mainActivityInterface.getTimeTools().timeFormatFixer(pad1CurrentTime);
                                padTime.setText(display);
                            } else {
                                if (padTime.getCurrentTextColor()==Color.TRANSPARENT) {
                                    padTime.setTextColor(Color.WHITE);
                                } else {
                                    padTime.setTextColor(Color.TRANSPARENT);
                                }
                            }
                        });
                    }
                };
                pad1PlayTimer = new Timer();
                pad1PlayTimer.scheduleAtFixedRate(pad1PlayTimerTask,1000,1000);
                break;
            case 2:
                pad2CurrentTime = 0;
                pad.setOnClickListener(v -> playStopOrPause(2));
                pad.setOnLongClickListener(v -> longClick(2));
                pad2PlayTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        pad2PlayTimerHandler.post(() -> {
                            if (pad2!=null && !pad2Pause) {
                                pad2CurrentTime = (int)(pad2.getCurrentPosition()/1000f);
                                String display = mainActivityInterface.getTimeTools().timeFormatFixer(pad2CurrentTime);
                                padTime.setText(display);
                            } else {
                                if (padTime.getCurrentTextColor()==Color.TRANSPARENT) {
                                    padTime.setTextColor(Color.WHITE);
                                } else {
                                    padTime.setTextColor(Color.TRANSPARENT);
                                }
                            }
                        });
                    }
                };
                pad2PlayTimer = new Timer();
                pad2PlayTimer.scheduleAtFixedRate(pad2PlayTimerTask,500,500);
                break;
        }
        mainActivityInterface.updateOnScreenInfo("showhide");
    }

    // Get info about the pads
    private boolean isPadPlaying(int padNum) {
        switch (padNum) {
            case 1:
            default:
                return pad1!=null && pad1.isPlaying();
            case 2:
                return pad2!=null && pad2.isPlaying();
        }
    }
    public boolean isPadPlaying() {
        return (pad1!=null && pad1.isPlaying() && !pad1Fading) || (pad2!=null && pad2.isPlaying() && !pad2Fading);
    }
    public boolean isPadPrepared() {
        return padTime.getText()!=null && !padTime.getText().toString().isEmpty() &&
                ((pad1!=null && pad1.getDuration()>0 && (pad1.isPlaying() || pad1Pause)) ||
                (pad2!=null && pad2.getDuration()>0 && (pad2.isPlaying() || pad2Pause)));
    }
    public void autoStartPad(Context c) {
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("padAutoStart",false) && padsActivated) {
            startPad(c);
        }
    }
    private void whichPadToFade(Context c) {
        if (isPadPlaying(1) && pad1Fading && isPadPlaying(2) && pad2Fading) {
            // Both pads are fading, so stop the other one that wasn't the last pad to start
            if (lastPadPlaying==1) {
                padToUse = 2;
                stopAndReset(1);
            } else {
                padToUse = 1;
                stopAndReset(2);
            }
        } else if (isPadPlaying(1) && !pad1Fading) {
            padToUse = 2;
            fadePad(c,1);
        } else if (isPadPlaying(2) && !pad2Fading) {
            fadePad(c,2);
            padToUse = 1;
        } else {
            padToUse = 1;
        }
    }

    private void playStopOrPause(int padNum) {
        switch (padNum) {
            case 1:
                if (pad1!=null && pad1.isPlaying() && pad1Fading) {
                    // Just stop the pad
                    stopAndReset(1);
                    endTimer(1);
                    padsActivated = false;
                } else if (pad1!=null && pad1.isPlaying() && !pad1Fading) {
                    // Pause the pad
                    padTime.setTextColor(Color.TRANSPARENT);
                    pad1.pause();
                    pad1Pause = true;
                } else if (pad1!=null && !pad1Fading) {
                    padTime.setTextColor(Color.WHITE);
                    pad1.start();
                    pad1Pause = false;
                }
                break;
            case 2:
                if (pad2!=null && pad2.isPlaying() && pad2Fading) {
                    // Just stop the pad
                    stopAndReset(2);
                    endTimer(2);
                    padsActivated = false;
                } else if (pad2!=null && pad2.isPlaying() && !pad2Fading) {
                    // Pause the pad
                    padTime.setTextColor(Color.TRANSPARENT);
                    pad2.pause();
                    pad2Pause = true;
                } else if (pad2!=null && !pad2Fading) {
                    padTime.setTextColor(Color.WHITE);
                    pad2.start();
                    pad2Pause = false;
                }
                break;
        }
    }

    public void panicStop() {
        // Emergency stop all pads - no fade
        stopAndReset(1);
        stopAndReset(2);
        endTimer(1);
        endTimer(2);
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
        endTimer(padNum);
        return true;
    }

    public void stopTimers(int padNum) {
        switch (padNum) {
            case 1:
                if (pad1PlayTimerTask!=null) {
                    pad1PlayTimerTask.cancel();
                    pad1PlayTimerTask = null;
                }
                if (pad1PlayTimer != null) {
                    pad1PlayTimer.cancel();
                    pad1PlayTimer.purge();
                }
                break;
            case 2:
                if (pad2PlayTimerTask!=null) {
                    pad2PlayTimerTask.cancel();
                    pad2PlayTimerTask = null;
                }
                if (pad2PlayTimer != null) {
                    pad2PlayTimer.cancel();
                    pad2PlayTimer.purge();
                }
                break;
        }
    }
}
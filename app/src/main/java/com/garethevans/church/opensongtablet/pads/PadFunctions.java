package com.garethevans.church.opensongtablet.pads;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

public class PadFunctions {

    private boolean orientationChanged;
    private int currentOrientation;

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



    // TODO STill to work through these
    float getVol(String pan, float padvol, int w) {
        float vol = 0.0f;
        switch (w) {
            case 0:
                // This is the left vol
                if (!pan.equals("R")) {
                    vol = padvol;
                }
                break;
            case 1:
                // This is the right vol
                if (!pan.equals("L")) {
                    vol = padvol;
                }
                break;
        }
        return vol;
    }

    public boolean getLoop(Song song) {
        return song.getPadloop()!=null && song.getPadloop().equals("true");
    }

    public boolean getPadStatus(MediaPlayer mediaPlayer) {
        return mediaPlayer!=null && mediaPlayer.isPlaying();
    }

    public boolean isPadValid(Context c, StorageAccess storageAccess, Preferences preferences, Song song) {
        // If we are using auto, key needs to be set
        // If we are using audio file link, it needs to exist
        // If we are set to OFF then nope!

        if (song.getPadfile()!=null && song.getPadfile().equals(c.getResources().getString(R.string.off))) {
            return false;
        } else if (song.getPadfile()!=null && song.getPadfile().equals(c.getResources().getString(R.string.link_audio)) &&
                song.getLinkaudio()!=null && !song.getLinkaudio().isEmpty()) {
            Uri uri = storageAccess.fixLocalisedUri(c, preferences, song.getLinkaudio());
            return storageAccess.uriExists(c,uri);
        } else {
            // Using auto
            return song.getKey()!=null && !song.getKey().isEmpty();
        }
    }

    ArrayList<Boolean> pauseOrResumePad(MediaPlayer mPlayer1, MediaPlayer mPlayer2, boolean mPlayer1Paused, boolean mPlayer2Paused, boolean pad1Fading, boolean pad2Fading) {
        ArrayList<Boolean> padsPaused = new ArrayList<>();  // 0=mPlayer1Paused, 1=mPlayer2Paused
        try {
            if (mPlayer1Paused) {
                // Restart pad 1
                mPlayer1.start();
                padsPaused.add(false);
                padsPaused.add(mPlayer2Paused);
                // IV - Addition to tests to prevent volume slider affecting a fading pad
            } else if (getPadStatus(mPlayer1) && !mPlayer1Paused && !pad1Fading) {
                // Pause pad 1
                mPlayer1.pause();
                padsPaused.add(true);
                padsPaused.add(mPlayer2Paused);
            } else if (mPlayer2Paused) {
                // Restart pad 2
                mPlayer2.start();
                padsPaused.add(mPlayer1Paused);
                padsPaused.add(false);
            } else if (getPadStatus(mPlayer2) && !mPlayer2Paused && !pad2Fading) {
                // Pause pad 2
                mPlayer2.pause();
                padsPaused.add(mPlayer1Paused);
                padsPaused.add(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            padsPaused.add(mPlayer1Paused);
            padsPaused.add(mPlayer2Paused);
        }
        return padsPaused;
    }

}
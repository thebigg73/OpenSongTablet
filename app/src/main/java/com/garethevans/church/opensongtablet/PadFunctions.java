package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;

class PadFunctions {

    static float getVol(int w) {
        float vol = 0.0f;
        switch (w) {
            case 0:
                // This is the left vol
                if (!FullscreenActivity.padpan.equals("right")) {
                    vol = FullscreenActivity.padvol;
                }
                break;
            case 1:
                // This is the right vol
                if (!FullscreenActivity.padpan.equals("left")) {
                    vol = FullscreenActivity.padvol;
                }
                break;
        }
        return vol;
    }

    static boolean getLoop() {
        // Set the looping value
        boolean shouldloop = false;
        if (FullscreenActivity.mLoopAudio.equals("true")) {
            shouldloop = true;
        }
        return shouldloop;
    }

    static boolean getPad1Status() {
        boolean result;
        try {
            result = FullscreenActivity.mPlayer1 != null && FullscreenActivity.mPlayer1.isPlaying();
        } catch (Exception e) {
            result = false;
        }
        FullscreenActivity.pad1Playing = result;
        return result;
    }

    static boolean getPad2Status() {
        boolean result;
        try {
            result = FullscreenActivity.mPlayer2 != null && FullscreenActivity.mPlayer2.isPlaying();
        } catch (Exception e) {
            result = false;
        }
        FullscreenActivity.pad2Playing = result;
        return result;
    }

    static boolean isPadValid(Context c, Preferences preferences) {
        // If we are using auto, key needs to be set
        // If we are using audio file link, it needs to exist
        // If we are set to OFF then nope!

        boolean isvalid = false;

        if (FullscreenActivity.mPadFile.equals(c.getResources().getString(R.string.off))) {
            isvalid = false;
        } else if (FullscreenActivity.mPadFile.equals(c.getResources().getString(R.string.link_audio)) &&
                !FullscreenActivity.mLinkAudio.isEmpty() && !FullscreenActivity.mLinkAudio.equals("")) {
            StorageAccess storageAccess = new StorageAccess();
            Uri uri = storageAccess.fixLocalisedUri(c, preferences, FullscreenActivity.mLinkAudio);
            isvalid = storageAccess.uriExists(c,uri);
        } else if (!FullscreenActivity.mKey.isEmpty()){
            // Using auto
            isvalid = true;
        }
        return isvalid;
    }

    static void pauseOrResumePad() {
        try {
            if (FullscreenActivity. mPlayer1Paused) {
                // Restart pad 1
                FullscreenActivity.mPlayer1.start();
                FullscreenActivity.mPlayer1Paused = false;
            } else if (getPad1Status() && !FullscreenActivity.mPlayer1Paused) {
                // Pause pad 1
                FullscreenActivity.mPlayer1.pause();
                FullscreenActivity.mPlayer1Paused = true;
            } else if (FullscreenActivity.mPlayer2Paused) {
                // Restart pad 1
                FullscreenActivity.mPlayer2.start();
                FullscreenActivity.mPlayer2Paused = false;
            } else if (getPad2Status() && !FullscreenActivity.mPlayer2Paused) {
                // Pause pad 2
                FullscreenActivity.mPlayer2.pause();
                FullscreenActivity.mPlayer2Paused = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
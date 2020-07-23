package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.net.Uri;

class PadFunctions {

    static float getVol(String pan, float padvol, int w) {
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

    static boolean getLoop() {
        // Set the looping value
        boolean shouldloop = false;
        if (StaticVariables.mLoopAudio.equals("true")) {
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
        StaticVariables.pad1Playing = result;
        return result;
    }

    static boolean getPad2Status() {
        boolean result;
        try {
            result = FullscreenActivity.mPlayer2 != null && FullscreenActivity.mPlayer2.isPlaying();
        } catch (Exception e) {
            result = false;
        }
        StaticVariables.pad2Playing = result;
        return result;
    }

    static boolean isPadValid(Context c, Preferences preferences) {
        // If we are using auto, key needs to be set
        // If we are using audio file link, it needs to exist
        // If we are set to OFF then nope!

        boolean isvalid;

        if (StaticVariables.mPadFile.equals(c.getResources().getString(R.string.off))) {
            isvalid = false;
        } else if (StaticVariables.mPadFile.equals(c.getResources().getString(R.string.link_audio)) && !StaticVariables.mLinkAudio.isEmpty()) {
            StorageAccess storageAccess = new StorageAccess();
            Uri uri = storageAccess.fixLocalisedUri(c, preferences, StaticVariables.mLinkAudio);
            isvalid = storageAccess.uriExists(c,uri);
        } else {
            // Using auto
            isvalid = !StaticVariables.mKey.isEmpty();
        }

        return isvalid;
    }

    static void pauseOrResumePad() {
        try {
            if (FullscreenActivity. mPlayer1Paused) {
                // Restart pad 1
                FullscreenActivity.mPlayer1.start();
                FullscreenActivity.mPlayer1Paused = false;
            // IV - Addition to tests to prevent volume slider affecting a fading pad
            } else if (getPad1Status() && !FullscreenActivity.mPlayer1Paused && !StaticVariables.pad1Fading) {
                // Pause pad 1
                FullscreenActivity.mPlayer1.pause();
                FullscreenActivity.mPlayer1Paused = true;
            } else if (FullscreenActivity.mPlayer2Paused) {
                // Restart pad 1
                FullscreenActivity.mPlayer2.start();
                FullscreenActivity.mPlayer2Paused = false;
            } else if (getPad2Status() && !FullscreenActivity.mPlayer2Paused && !StaticVariables.pad2Fading) {
                // Pause pad 2
                FullscreenActivity.mPlayer2.pause();
                FullscreenActivity.mPlayer2Paused = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
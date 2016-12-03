package com.garethevans.church.opensongtablet;

import android.content.Context;

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

    static boolean getLoop(Context c) {
        // Set the looping value
        boolean shouldloop = false;
        if (FullscreenActivity.mPadFile.equals(c.getResources().getString(R.string.pad_auto)) ||
                FullscreenActivity.mLoopAudio.equals("true")) {
            shouldloop = true;
        }
        return shouldloop;
    }

    static boolean getPad1Status() {
        boolean result = false;
        try {
            FullscreenActivity.pad1Playing = FullscreenActivity.mPlayer1 != null && FullscreenActivity.mPlayer1.isPlaying();
            result = true;
        } catch (Exception e) {
            FullscreenActivity.pad1Playing = false;
        }
        return result;
    }

    static boolean getPad2Status() {
        boolean result = false;
        try {
            FullscreenActivity.pad2Playing = FullscreenActivity.mPlayer2 != null && FullscreenActivity.mPlayer2.isPlaying();
            result = true;
        } catch (Exception e) {
            FullscreenActivity.pad2Playing = false;
        }
        return result;
    }

}
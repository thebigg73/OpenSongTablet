package com.garethevans.church.opensongtablet;

import android.content.Context;

import java.io.File;

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

    static boolean isPadValid(Context c) {
        // If we are using auto, key needs to be set
        // If we are using audio file link, it needs to exist
        // If we are set to OFF then nope!

        boolean isvalid = false;

        if (FullscreenActivity.mPadFile.equals(c.getResources().getString(R.string.off))) {
            isvalid = false;
        } else if (FullscreenActivity.mPadFile.equals(c.getResources().getString(R.string.link_audio)) &&
                !FullscreenActivity.mLinkAudio.isEmpty() && !FullscreenActivity.mLinkAudio.equals("")) {
            String filetext = FullscreenActivity.mLinkAudio;
            // If this is a localised file, we need to unlocalise it to enable it to be read
            if (filetext.startsWith("../OpenSong/")) {
                filetext = "file://" + filetext.replace("../OpenSong/",FullscreenActivity.homedir+"/");
            }
            File file = new File (filetext);
            isvalid = file.exists() && file.isFile();
        } else if (!FullscreenActivity.mKey.isEmpty()){
            isvalid = true;
        }
         return isvalid;
    }

}
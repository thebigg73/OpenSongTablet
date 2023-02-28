package com.garethevans.church.opensongtablet.midi;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class Drummer {

    // This class is to emulate a drum machine that you can play along with.
    // There are basic midi files available to begin with, but ultimately the user can create their own!

    private MediaPlayer part1, part1Fill1, part1Fill2, part1Fill3, part2, part2Fill1, part2Fill2, part2Fill3;
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final String TAG = "Drummer";
    private String bpm_string, bpm_hex, timesig_string, timesig_hex;
    private int bpm_int;

    public Drummer(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    public void setupSongValues() {
        // Set the tempo
        setTempo();
        // Set the time signature
        setTimeSig();
    }

    private void setTempo() {
        bpm_string = mainActivityInterface.getSong().getTempo();
        if (bpm_string!=null && !bpm_string.isEmpty()) {
            bpm_int = Integer.parseInt(bpm_string);
        } else {
            bpm_string = "120";
            bpm_int = 120;
        }
        bpm_hex = mainActivityInterface.getMidi().getTempoByteString(bpm_int);

        Log.d(TAG,"bpm_string:"+bpm_string+"  bpm_int:"+bpm_int+"  bpm_hex:"+bpm_hex);
    }

    private void setTimeSig() {
        timesig_string = mainActivityInterface.getSong().getTimesig();
        if (timesig_string==null || timesig_string.isEmpty()) {
            // Assume 4/4
            timesig_string = "4/4";
        }
        timesig_hex = mainActivityInterface.getMidi().getTimeSigByteString(timesig_string);
        Log.d(TAG,"timesig_string:"+timesig_string+"  timesig_hex:"+timesig_hex);
    }

}

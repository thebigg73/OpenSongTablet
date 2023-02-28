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
    // Drum voices recognised
    private final int bass_drum = 35;
    private final int rim_shot = 37;
    private final int snare_drum = 38;
    private final int low_floor_tom = 41;
    private final int high_floor_tom = 43;
    private final int low_tom = 45;
    private final int low_mid_tom = 47;
    private final int high_mid_tom = 48;
    private final int high_tom = 50;
    private final int hat_closed = 42;
    private final int hat_pedal = 44;
    private final int hat_open = 46;
    private final int crash_1 = 49;
    private final int crash_2 = 57;
    private final int ride_1 = 51;
    private final int ride_2 = 59;
    private final int ride_bell = 53;
    private final int splash = 55;

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

    private String instrumentHex(int keyNum) {
        return String.format("%02X",keyNum);
    }
}

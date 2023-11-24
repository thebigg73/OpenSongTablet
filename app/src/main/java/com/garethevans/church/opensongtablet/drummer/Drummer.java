package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.File;
import java.io.FileOutputStream;

public class Drummer {

    // This class is to emulate a drum machine that you can play along with.
    // There are basic midi files available to begin with, but ultimately the user can create their own!
    // Also looking at using Oboe to access low latency audio
    // Currently this will create a basic midi file that works, but looped playback is terrible

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

    // Drum volumes based on dynamics
    private final int level_ppp = 16;
    private final int level_pp = 33;
    private final int level_p = 49;
    private final int level_mp = 64;
    private final int level_mf = 80;
    private final int level_f = 96;
    private final int level_ff = 112;
    private final int level_fff = 127;

    private final String note_on = "99 ";
    private final int beat_4 = 32;
    private final int half_4 = 16;
    private final int quarter_4 = 8;
    private final int eigth_4 = 4;
    private final int sixteenth_4 = 2;

    /*
    1  -  2  -  3  -  4  - ..
    0 16 16 16 16 16 16 16 16

    B           B
          S           S
    H  H  H  H  H  H  H  H
     */
    private String simple_1_4_4() {
        // Make a simple midi file
        String bass = note_on + intToHex(bass_drum) + intToHex(level_ff);
        String snare = note_on + intToHex(snare_drum) + intToHex(level_ff);
        String hatff = note_on + intToHex(hat_closed) + intToHex(level_ff);
        String hatf = note_on + intToHex(hat_closed) + intToHex(level_f);

        String events = bpm_hex + timesig_hex +
                intToHex(0) + bass + intToHex(0) + hatff +
                intToHex(beat_4*2) + hatf +
                intToHex(beat_4*2) + snare + intToHex(0) + hatf +
                intToHex(beat_4*2) + hatf +
                intToHex(beat_4*2) + bass + intToHex(0) + hatff +
                intToHex(beat_4*2) + hatf +
                intToHex(beat_4*2) + snare + intToHex(0) + hatff +
                intToHex(beat_4*2) + hatf +
                intToHex(beat_4*2) + mainActivityInterface.getMidi().getAllOff() +
                mainActivityInterface.getMidi().getMidiFileTrackOut();

        int size = events.split(" ").length;

        return mainActivityInterface.getMidi().getMidiFileHeader() +
                mainActivityInterface.getMidi().getMidiFileTrackHeader() +
                intToHex(size) + events;
    }


    public Drummer(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    public void setupSongValues() {
        // Set the tempo
        setTempo();
        // Set the time signature
        setTimeSig();

        String drums = simple_1_4_4();
        File drumFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Midi","","simple_1_4_4.mid");
        //File drumFile = new File(c.getExternalFilesDir("Midi"),"simple_1_4_4.mid");
        try (FileOutputStream fileOutputStream = new FileOutputStream(drumFile,false)) {
            fileOutputStream.write(mainActivityInterface.getMidi().returnBytesFromHexText(drums));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (part1!=null) {
            part1.release();
            part1 = null;
        }
        part1 = new MediaPlayer();
        part1.setLooping(true);
        part1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                part1.start();
            }
        });
        Uri uri = Uri.parse(drumFile.getPath());
        try {
            part1.setDataSource(c, uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        part1.prepareAsync();

        Log.d(TAG,"simple_1_4_4:"+simple_1_4_4());
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

    private String intToHex(int value) {
        return String.format("%02X",value) + " ";
    }
}

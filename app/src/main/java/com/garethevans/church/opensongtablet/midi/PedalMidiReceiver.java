package com.garethevans.church.opensongtablet.midi;

import android.media.midi.MidiReceiver;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PedalMidiReceiver extends MidiReceiver {

    Midi midi;

    PedalMidiReceiver (Midi midi) {
        this.midi = midi;
    }

    @Override
    public void onSend(byte[] msg, int offset, int count, long timestamp) {
        Log.d("PedalMidiReceiver","msg="+ Arrays.toString(msg));
        Log.d("PedalMidiReceiver","msg.length="+ msg.length);
        if (msg.length>=4) {
            int bittoadd = 0xFF;
            int byte1 = msg[1] & 0xFF;  // This determines action and channel
            int byte2 = msg[2] & 0xFF;  // THis is the note
            int byte3 = msg[3] & 0xFF;  // This is the velocity

            Log.d("d","byte1="+byte1);
            Log.d("d","byte2="+byte2);
            Log.d("d","byte3="+byte3);

            if (byte1>=144 && byte1<=159) {
                // This is a note on.  Don't need this
                Log.d("d","Note on channel="+((byte1-144)+1));
            } else if (byte1>=128 && byte1<=143) {
                // This is a note off.  Don't need this
                Log.d("d","Note off channel="+((byte1-128)+1));
            }
            String note = midi.getNoteFromInt(byte2);
            Log.d("d","Note="+byte2);
            Log.d("d","Velocity="+byte3);

            String b0 = Integer.toString(msg[0],16);
            String b1 = Integer.toString(msg[1],16);
            String b2 = Integer.toString(msg[2],16);
            Log.d("d","b0="+b0+"  b1="+b1+"  b2="+b2);
        }
    }
}


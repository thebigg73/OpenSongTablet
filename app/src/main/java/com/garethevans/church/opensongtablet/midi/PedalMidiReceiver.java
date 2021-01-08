package com.garethevans.church.opensongtablet.midi;

import android.media.midi.MidiReceiver;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PedalMidiReceiver extends MidiReceiver {

    private final Midi midi;
    private final MainActivityInterface mainActivityInterface;
    private long downTime, upTime;

    PedalMidiReceiver (Midi midi, MainActivityInterface mainActivityInterface) {
        this.midi = midi;
        this.mainActivityInterface = mainActivityInterface;
    }

    @Override
    public void onSend(byte[] msg, int offset, int count, long timestamp) {
        Log.d("PedalMidiReceiver","msg="+ Arrays.toString(msg));
        Log.d("PedalMidiReceiver","msg.length="+ msg.length);
        if (msg.length>=4) {
            int byte1 = msg[1] & 0xFF;  // This determines action and channel
            int byte2 = msg[2] & 0xFF;  // This is the note
            int byte3 = msg[3] & 0xFF;  // This is the velocity

            Log.d("d","byte1="+byte1);
            Log.d("d","byte2="+byte2);
            Log.d("d","byte3="+byte3);

            boolean actionDown = false;
            boolean actionUp = false;
            boolean actionLong = false;

            if (byte1>=144 && byte1<=159) {
                // This is a note on.  Don't need this
                Log.d("d","Note on channel="+((byte1-144)+1));
                downTime = System.currentTimeMillis();
                upTime = downTime;
                actionDown = true;
                actionUp = false;
                actionLong = false;

            } else if (byte1>=128 && byte1<=143) {
                // This is a note off.  Don't need this
                Log.d("d","Note off channel="+((byte1-128)+1));
                upTime = System.currentTimeMillis();
                actionDown = false;
                actionUp = true;
                actionLong = false;
            }

            if (actionUp) {
                if (upTime - downTime > 500) {
                    actionUp = false;
                    actionLong = true;
                }
            }

            String note = midi.getNoteFromInt(byte2);
            Log.d("d","Note="+byte2);
            Log.d("d","Velocity="+byte3);

            String b0 = Integer.toString(msg[0],16);
            String b1 = Integer.toString(msg[1],16);
            String b2 = Integer.toString(msg[2],16);
            Log.d("d","b0="+b0+"  b1="+b1+"  b2="+b2);

            mainActivityInterface.registerMidiAction(actionDown,actionUp,actionLong,note);
        }
    }
}


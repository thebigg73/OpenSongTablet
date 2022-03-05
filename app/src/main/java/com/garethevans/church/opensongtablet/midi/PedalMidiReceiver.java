package com.garethevans.church.opensongtablet.midi;

import android.media.midi.MidiReceiver;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PedalMidiReceiver extends MidiReceiver {

    // TODO Test this
    private final Midi midi;
    private final MainActivityInterface mainActivityInterface;
    private long downTime, upTime;
    private int downByte;
    private final String TAG = "PedalMidiReceiver";
    private String actionDown;
    private long actionDownTime;

    PedalMidiReceiver (Midi midi, MainActivityInterface mainActivityInterface) {
        this.midi = midi;
        this.mainActivityInterface = mainActivityInterface;
    }

    @Override
    public void onSend(byte[] msg, int offset, int count, long timestamp) {
        Log.d(TAG,"msg="+ Arrays.toString(msg));
        Log.d(TAG,"msg.length="+ msg.length);
        if (msg.length>=4) {
            int byte1 = msg[1] & 0xFF;  // This determines action and channel
            int byte2 = msg[2] & 0xFF;  // This is the note
            int byte3 = msg[3] & 0xFF;  // This is the velocity - if 0 then action up

            Log.d(TAG,"byte1="+byte1);
            Log.d(TAG,"byte2="+byte2);
            Log.d(TAG,"byte3="+byte3);

            boolean actionDown = false;
            boolean actionUp = false;
            boolean actionLong = false;

            if (byte1>=144 && byte1<=159) {
                if (byte3 > 0) {
                    Log.d(TAG, "Note on channel=" + ((byte1 - 144) + 1));
                    downByte = byte2;
                    actionDown = true;
                    downTime = System.currentTimeMillis();
                } else if (byte2 == downByte) {
                    Log.d(TAG, "Note off channel=" + ((byte1 - 144) + 1));
                    // This is action up or long press
                    upTime = System.currentTimeMillis();
                    if (upTime - downTime > 1000 && upTime - downTime < 5000) {
                        // If between 1 and 5 secs, it is a long press
                        actionLong = true;
                        actionUp = false;
                    } else {
                        actionUp = true;
                    }
                }
            } else if (byte1>=128 && byte1<=143) {
                // This is a note off.  Don't need this
                Log.d(TAG,"Note off channel="+((byte1-128)+1));
                upTime = System.currentTimeMillis();
                actionDown = false;
                actionUp = true;
                actionLong = false;
            }

            String note = midi.getNoteFromInt(byte2);
            Log.d(TAG,"Note="+byte2);
            Log.d(TAG,"Velocity="+byte3);

            String b0 = Integer.toString(msg[0],16);
            String b1 = Integer.toString(msg[1],16);
            String b2 = Integer.toString(msg[2],16);
            Log.d(TAG,"b0="+b0+"  b1="+b1+"  b2="+b2);
            Log.d(TAG, "actionDown="+actionDown+"  actionUp="+actionUp+" actionLong="+actionLong);
            Log.d(TAG, "note="+note);

            mainActivityInterface.registerMidiAction(actionDown,actionUp,actionLong,note);
        }
    }
}


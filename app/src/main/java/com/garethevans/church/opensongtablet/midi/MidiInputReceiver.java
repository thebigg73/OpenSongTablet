package com.garethevans.church.opensongtablet.midi;

import android.content.Context;
import android.media.midi.MidiReceiver;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MidiInputReceiver extends MidiReceiver {

    private final MainActivityInterface mainActivityInterface;
    private final String TAG = "MidiInputReceiver";
    private ArrayList<Byte> receivedMessage;
    private final int nx_start=128, nx_end=143, no_start=144, no_end=159,
            cc_start=176, cc_end=191, pc_start=192, pc_end=207;
    private int msbChosen, pcChosen;
    private final Handler songMessageHander = new Handler();
    private final Runnable songMessageRunnable;
    private final Handler longPressHandler = new Handler();
    private final Runnable longPressRunnable;
    private byte[] longPressMessage;
    private int longPressOffset;
    private int longPressCount;
    private long longPressTimeStamp;
    private boolean listeningForLongPress = false;
    private boolean isLongPress = false;

    MidiInputReceiver(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        songMessageRunnable = () -> {
            // We took too long (runnable called 1sec after msbChosen is set)
            // Reset the msbChosen, so any PC after this is ignored
            try {
                msbChosen = -1;
                pcChosen = -1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        longPressRunnable = () -> {
            // If we haven't received the NX yet, but did receive the NO 1 sec ago, it's a long press
            isLongPress = true;
            onSend(longPressMessage,longPressOffset,longPressCount,longPressTimeStamp+1000);
            longPressMessage = null;
            listeningForLongPress = false;
        };
    }

    @Override
    // Confusing, but this is triggered when we receive a MIDI input message
    public void onSend(byte[] msg, int offset, int count, long timestamp) {
        try {
            // Keep a reference of the midi message (so we can record incoming messages)
            addReceivedMessage(msg);
            int[] bytes = new int[msg.length];
            for (int i = 0; i < msg.length; i++) {
                bytes[i] = msg[i] & 0xFF;
            }

            boolean midiStart = isMidiStart(bytes);
            boolean midiStop = isMidiStop(bytes);
            String messageType = getMessageType(bytes);
            int midiChannel = getMidiChannelFromBytes(bytes);
            // We are only ever interested in data1 for these actions
            int data1 = getData1(bytes);
            int data2 = getData2(bytes);

            // Only do something if we are listening on these channels and the message is appropriate
            if (midiChannel == mainActivityInterface.getMidi().getMidiInputChannelPedal() &&
                    messageType.equals("NO") || messageType.equals("NX")) {
                // This is a likely foot pedal command - note on==action down, note off=action up
                // We also check for a long press
                boolean actionDown = messageType.equals("NO");
                boolean actionUp = messageType.equals("NX");
                boolean actionLong = false;

                if (actionDown) {
                    if (!listeningForLongPress) {
                        // Clear existing listener and listen for no NX within 1 sec
                        // Only do this once per 1 second
                        longPressMessage = new byte[msg.length];
                        System.arraycopy(msg, 0, longPressMessage, 0, msg.length);
                        longPressMessage[1] = (byte) (nx_start+midiChannel-1);
                        longPressCount = count;
                        longPressOffset = offset;
                        longPressTimeStamp = timestamp;
                        isLongPress = false;
                        listeningForLongPress = true;
                        longPressHandler.removeCallbacks(longPressRunnable);
                        longPressHandler.postDelayed(longPressRunnable, 1000);
                    }
                }

                if (isLongPress) {
                    actionDown = false;
                    actionUp = false;
                }

                mainActivityInterface.registerMidiPedalAction(actionDown, actionUp, actionLong,
                        mainActivityInterface.getMidi().getNoteFromInt(data1));


            } else if (midiChannel == mainActivityInterface.getMidi().getMidiInputChannelSong()) {
                // This is a likely a change song command or a start/stop for autoscroll
                // This is in multiple parts so only proceed if within time and first part is received
                if (midiStart) {
                    // Try to start the autoscroll/metronome/pad
                    Log.d(TAG, "Start the autoscroll/metronome/pad");
                    if (mainActivityInterface.getMidi().getMidiInputAutoscroll()) {
                        Log.d(TAG,"Start autoscroll");
                        mainActivityInterface.getAutoscroll().startAutoscroll();
                    }
                    if (mainActivityInterface.getMidi().getMidiInputMetronome()) {
                        Log.d(TAG,"Stop metronome");
                        mainActivityInterface.getMetronome().startMetronome();
                    }
                    if (mainActivityInterface.getMidi().getMidiInputPad()) {
                        Log.d(TAG,"Start pad");
                        int whichPad = mainActivityInterface.getPad().whichPadPlaying();
                        if (whichPad==0) {
                            whichPad = 1;
                        }
                        mainActivityInterface.getPad().playStopOrPause(whichPad);
                    }

                } else if (midiStop) {
                    // Try to pause the autoscroll/metronome/pad
                    if (mainActivityInterface.getMidi().getMidiInputAutoscroll()) {
                        Log.d(TAG,"Pause autoscroll");
                        mainActivityInterface.getAutoscroll().pauseAutoscroll();
                    }
                    if (mainActivityInterface.getMidi().getMidiInputMetronome()) {
                        Log.d(TAG,"Stop metronome");
                        mainActivityInterface.getMetronome().stopMetronome();
                    }
                    if (mainActivityInterface.getMidi().getMidiInputPad()) {
                        Log.d(TAG,"Pause pad");
                        mainActivityInterface.getPad().playStopOrPause(mainActivityInterface.getPad().whichPadPlaying());
                    }


                } else if (messageType.equals("CC") && data1 == 0) {
                    // This is the bank select on the MSB
                    // Set the handler to clear the MSB value after 1s.  Time for PC to arrive
                    songMessageHander.removeCallbacks(songMessageRunnable);
                    songMessageHander.postDelayed(songMessageRunnable, 1000);
                    Log.d(TAG,"MSB chosen:" + data2);
                    msbChosen = data2;

                } else if (messageType.equals("PC") && msbChosen >= 0) {
                    // We have received the PC song number and also have the MSB chosen - song chosen
                    songMessageHander.removeCallbacks(songMessageRunnable);
                    int songNumber = getSongNumber();
                    // Clear the chosen values
                    msbChosen = -1;
                    pcChosen = -1;
                    Log.d(TAG, "songNumber:" + songNumber);
                    Song songToLoad = mainActivityInterface.getSQLiteHelper().getSongFromMidiIndex(songNumber);
                    if (songToLoad!=null && songToLoad.getFilename()!=null && !songToLoad.getFilename().isEmpty() &&
                        songToLoad.getFolder()!=null && !songToLoad.getFolder().isEmpty()) {
                        // A matching song has been found, so try to load it!
                        Log.d(TAG,"loading song "+songToLoad.getFolder()+"/"+songToLoad.getFilename());
                        mainActivityInterface.doSongLoad(songToLoad.getFolder(),songToLoad.getFilename(),false);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*
    if (msg.length >= 4) {
                int byte1 = msg[1] & 0xFF;  // This determines action and channel
                int byte2 = msg[2] & 0xFF;  // This is the note
                int byte3 = msg[3] & 0xFF;  // This is the velocity - if 0 then action up

                boolean actionDown = false;
                boolean actionUp = false;
                boolean actionLong = false;
                int incomingChannel = -1;

                long upTime;
                upTime = System.currentTimeMillis();
                if (upTime - downTime > 1000 && upTime - downTime < 5000) {
                    // If between 1 and 5 secs, it is a long press
                    actionLong = true;
                } else {
                    actionUp = true;
                }

                if (byte1 >= 144 && byte1 <= 159) {
                    incomingChannel = ((byte1 - 144) + 1);

                    if (byte3 > 0) {
                        incomingChannel = ((byte1 - 144) + 1);
                        Log.d(TAG, "Note on channel:" + incomingChannel);
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
                        } else {
                            actionUp = true;
                        }
                    }
                } else if (byte1 >= 128 && byte1 <= 143) {
                    // This is a note off.  Don't need this
                    Log.d(TAG,"Note off channel="+((byte1-128)+1));
                    //upTime = System.currentTimeMillis();
                    actionUp = true;
                }

                String note = midi.getNoteFromInt(byte2);
                //Log.d(TAG,"Note="+byte2);
                //Log.d(TAG,"Velocity="+byte3);

                //String b0 = Integer.toString(msg[0], 16);
                //String b1 = Integer.toString(msg[1], 16);
                //String b2 = Integer.toString(msg[2], 16);
                //Log.d(TAG,"b0="+b0+"  b1="+b1+"  b2="+b2);
                //Log.d(TAG, "actionDown="+actionDown+"  actionUp="+actionUp+" actionLong="+actionLong);
                //Log.d(TAG, "note="+note);

                mainActivityInterface.registerMidiPedalAction(actionDown, actionUp, actionLong, note);

            }
     */

    // MIDI message logging
    public void resetReceivedMessage() {
        receivedMessage = new ArrayList<>();
    }
    @SuppressWarnings("unused")
    public ArrayList<Byte> getReceivedMessage() {
        return receivedMessage;
    }
    private void addReceivedMessage(byte[] bytes) {
        Log.d(TAG,"addReceivedMessage()");
        if (receivedMessage==null) {
            resetReceivedMessage();
        }
        for (byte thisByte:bytes) {
            //Log.d(TAG,"Adding byte:"+thisByte);
            receivedMessage.add(thisByte);
        }
    }


    // Get the MIDI message information from the received bytes
    // If there is an issue, return a sensible non-null value
    private String getMessageType(int[] bytes) {
        if (bytes.length>=2) {
            // Byte 1 is the action and MIDI channel in hex format
            // byte[1] is the action and MIDI channel in hex format
            if (bytes[1] >= nx_start && bytes[1] <= nx_end) {
                // This is a note off message
                return "NX";
            } else if (bytes[1] >= no_start && bytes[1] <= no_end) {
                // This is a note on message
                return "NO";
            } else if (bytes[1] >= cc_start && bytes[1] <= cc_end) {
                // This is a control change message
                return "CC";
            } else if (bytes[1] >= pc_start && bytes[1] <= pc_end) {
                // This is a program change message
                return "PC";
            }
        }
        return "";
    }
    private int getMidiChannelFromBytes(int[] bytes) {
        if (bytes.length>=2) {
            // byte[1] is the action and MIDI channel in hex format
            if (bytes[1] >= nx_start && bytes[1] <= nx_end) {
                return (bytes[1] - nx_start) + 1;
            } else if (bytes[1] >= no_start && bytes[1] <= no_end) {
                return (bytes[1] - no_start) + 1;
            } else if (bytes[1] >= cc_start && bytes[1] <= cc_end) {
                return (bytes[1] - cc_start) + 1;
            } else if (bytes[1] >= pc_start && bytes[1] <= pc_end) {
                return (bytes[1] - pc_start) + 1;
            }
        }
        // Use default MIDI input channel if we can't get it from the message
        return -1;
    }
    private int getData1(int[] bytes) {
        if (bytes.length>=3) {
            return bytes[3];
        }
        return 0;
    }
    private int getData2(int[] bytes) {
        if (bytes.length >= 4) {
            return bytes[4];
        }
        return 0;
    }
    private boolean isMidiStart(int[] bytes) {
        // Could be a MIDI start (0xFA)
        if (bytes.length>=2) {
            int decimal = bytes[1] & 0xFF;
            String hexCode = "0x" + String.format("%02X", decimal);
            return hexCode.equals("0xFA");
        }
        return false;
    }
    private boolean isMidiStop(int[] bytes) {
        // Could be a MIDI stop (0xFC)
        if (bytes.length>=2) {
            int decimal = bytes[1] & 0xFF;
            String hexCode = "0x" + String.format("%02X", decimal);
            return hexCode.equals("0xFC");
        }
        return false;
    }

    private int getSongNumber() {
        // Use the MSB bank number and PC to get the song number
        if (msbChosen>=0 && pcChosen>=0) {
            int songNumber =  (msbChosen * 128) + pcChosen;
            Log.d(TAG,"songNumber:"+songNumber);
            return songNumber;
        }
        return -1;
    }
}

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
import java.util.Arrays;

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
    private int longPressNote = -1;
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
            if (listeningForLongPress) {
                // This stops us recording the next actionUp message
                isLongPress = true;
                mainActivityInterface.registerMidiPedalAction(false, false, true,
                        mainActivityInterface.getMidi().getNoteFromInt(longPressNote));
            }
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

            Log.d(TAG,"message:"+ Arrays.toString(bytes));
            boolean midiStart = isMidiStart(bytes);
            boolean midiStop = isMidiStop(bytes);
            boolean songSelect = isSongSelect(bytes);
            String messageType = getMessageType(bytes);
            int midiChannel = getMidiChannelFromBytes(bytes);
            // The data is numerical from 0-127
            int data1 = getData1(bytes);
            int data2 = getData2(bytes);

            Log.d(TAG,"messageType:"+messageType+"  midiChannel:"+midiChannel+"  data1:"+data1+"  data2:"+data2);

            // Only do something if we are listening on these channels and the message is appropriate
            if (midiChannel == mainActivityInterface.getMidi().getMidiInputChannelPedal() &&
                    messageType.equals("NO") || messageType.equals("NX")) {
                // This is a likely foot pedal command - note on==actionDown==NO, note off=actionUp==NX
                // We also check for a long press using a handler
                // Long press means the actionDown is recieved and no actionUp is received within 1 second
                boolean actionDown = messageType.equals("NO");
                boolean actionUp = messageType.equals("NX");

                if (actionDown) {
                    // Just in case this is a long press, keep a note of the data 1 (note pressed)
                    longPressNote = data1;

                    // Set the values to start waiting for a long press
                    // Clear any current runnables from the handler and listen for 1 second
                    isLongPress = false;
                    listeningForLongPress = true;
                    longPressHandler.removeCallbacks(longPressRunnable);
                    longPressHandler.postDelayed(longPressRunnable, 1000);
                } else {
                    // This is an actionUp, so we don't need the long press check
                    longPressHandler.removeCallbacks(longPressRunnable);
                }

                // Check if this is an actionUp called immediately after a long press
                // Because the long press handler dealt with it, ignore this one.
                // If we didn't we'd trigger both a long press and single press action
                boolean falseActionUp = actionUp && isLongPress;

                // Now we can reset the longPress flag ready for another go later
                if (actionUp) {
                    isLongPress = false;
                    listeningForLongPress = false;
                }

                // This only sends simple actionDown/actionUp.
                // Long presses are sent from the handler directly, so use the check
                if (!falseActionUp) {
                    mainActivityInterface.registerMidiPedalAction(actionDown, actionUp, false,
                            mainActivityInterface.getMidi().getNoteFromInt(data1));
                }


            } else if ((midiChannel == mainActivityInterface.getMidi().getMidiInputChannelSong()) || songSelect) {
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
                        Log.d(TAG,"Start metronome");
                        mainActivityInterface.getDrumViewModel().startMetronome();
                        //mainActivityInterface.getMetronome().startMetronome();
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
                        mainActivityInterface.getDrumViewModel().stopMetronome();
                        //mainActivityInterface.getMetronome().stopMetronome();
                    }
                    if (mainActivityInterface.getMidi().getMidiInputPad()) {
                        Log.d(TAG,"Pause pad");
                        mainActivityInterface.getPad().playStopOrPause(mainActivityInterface.getPad().whichPadPlaying());
                    }

                } else if (messageType.equals("CC") && (data1 == 0 || data1 == 32)) {
                    // This is the bank select on the MSB
                    // Set the handler to clear the MSB value after 1s.  Time for PC to arrive
                    songMessageHander.removeCallbacks(songMessageRunnable);
                    songMessageHander.postDelayed(songMessageRunnable, 1000);
                    Log.d(TAG,"MSB chosen:" + data1 + "," + data2);
                    msbChosen = data2;

                } else if (messageType.equals("PC") || songSelect) {
                    // We have received the PC song number and may also have the MSB chosen - song chosen
                    songMessageHander.removeCallbacks(songMessageRunnable);
                    pcChosen = data1;
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
        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
        }
    }

    // MIDI message logging
    public void resetReceivedMessage() {
        receivedMessage = new ArrayList<>();
    }
    @SuppressWarnings("unused")
    public ArrayList<Byte> getReceivedMessage() {
        return receivedMessage;
    }
    private void addReceivedMessage(byte[] bytes) {
        resetReceivedMessage();
        for (byte thisByte:bytes) {
            try {
                receivedMessage.add(thisByte);
            } catch (Exception | OutOfMemoryError e) {
                receivedMessage = null;
                resetReceivedMessage();
                e.printStackTrace();
            }
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
            return bytes[2];
        }
        return 0;
    }
    private int getData2(int[] bytes) {
        if (bytes.length >= 4) {
            return bytes[3];
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
    private boolean isSongSelect(int[] bytes) {
        // Could be a Song Select message (0xF3)
        if (bytes.length>=2) {
            int decimal = bytes[1] & 0xFF;
            String hexCode = "0x" + String.format("%02X", decimal);
            return hexCode.equals("0xF3");
        }
        return false;
    }

    private int getSongNumber() {
        // Use the MSB bank number and PC to get the song number
        // If not MSB received, we assume bank 0 and only use the first 127 songs
        // Song 1 will have msbChosen = 0, pcChosen = 0, so (msbChosen * 128) + pcChosen + 1 = 1
        // Song 128 will have msbChosen = 0, pcChosen = 127, so (msbChosen * 128) + pcChosen + 1 = 128
        // Song 129 will have msbChosen = 1, pcChosen = 0, so (msbChosen * 128) + pcChosen + 1 = 129
        // The final +1 is to change 0 into 1, etc.
        int songNumber = -1;
        if (pcChosen>=0) {
            if (msbChosen>=0) {
                songNumber = (msbChosen * 128) + pcChosen + 1;
            } else {
                songNumber = pcChosen + 1;
            }
        }
        Log.d(TAG,"songNumber:"+songNumber);
        return songNumber;
    }
}

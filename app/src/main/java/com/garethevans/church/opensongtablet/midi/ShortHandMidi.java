package com.garethevans.church.opensongtablet.midi;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.Locale;

public class ShortHandMidi {

    // This class is used to interpret shorthand MIDI messages and convert back to actual MIDI

    // Inline simplified MIDI messages
    // Rather than expecting users to have to type actual MIDI messages, include shorthand
    // These can be added inline to song sections that can be triggered by selecting them
    // Each piece of information is separated by a colon
    // MIDI{1-16}:{CC,PC,NO,NX,MSB,LSB,BB..}{0-127}:V{0-127}
    // CC = Control change, PC = Program change, NO = note on, NX = note off,
    // MSB = Most significant bit, LSB = Least significant bit, V = Value

    // Specialised BeatBuddy commands
    // BBT{1-128} = BeatBuddy transition, BBTX = BeatBuddy transition exit
    // BBTN = BeatBuddy transition next, BBTP = BeatBuddy transition previous,
    // BBTE{1-128} = BeatBuddy transition exclusive, BBTEX = BeatBuddy transition exclusive exit
    // BBTEN =  BeatBuddy transition exclusive next, BBTEP = BeatBuddy transition exclusive previous
    // BBH = BeatBuddy half time, BBHX = BeatBuddy half time exit,
    // BBD = BeatBuddy double time, BBDX = BeatBuddy double time exit,
    // BBBPM{40-300} = BeatBuddy tempo change, BBV{0-100} BeatBuddy volume
    // BBS{1-127}/{1-127} = BeatBuddy folder/song
    // BBI = BeatBuddy intro, BBO = BeatBuddy outro, BBP = BeatBuddy pause
    // BBF = BeatBuddy fill, BBA = BeatBuddy accent

    // Examples
    // MIDI9:CC106:100      MIDI channel 9, controller change 106, value 100
    // MIDI1:PC100          MIDI channel 1, program change 100
    // MIDI4:NO100:50       MIDI channel 4, note on note 100, value 50
    // MIDI2:NX100          MIDI channel 2, note off note 100
    // MIDI5:BBT1           MIDI channel 5, BeatBuddy transition 1
    // MIDI5:BBTX           MIDI channel 5, BeatBuddy transition end (any)
    // MIDI8:BBBPM100       MIDI channel 8, BeatBuddy tempo change to 100bpm
    // MIDI5:BBF50          MIDI channel 5, BeatBuddy folder select 50
    // MIDI5:BBS80          MIDI channel 5, BeatBuddy song select 80

    private final MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ShorthandMidi";

    public ShortHandMidi(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }

    public String convertShorthandToMIDI(String textToCheck) {
        Log.d(TAG,"processing:"+textToCheck);
        // The MIDI messages might be fine to go.  We only need to act/check if we find (MIDI and :)
        if (textToCheck.contains("MIDI") && textToCheck.contains(":")) {
            // Split the lines up
            String[] lines = textToCheck.split("\n");
            StringBuilder fixedLines = new StringBuilder();
            for (String line:lines) {
                if ((line.trim().startsWith(";MIDI") || line.trim().startsWith("MIDI")) &&
                    line.contains(":")) {
                    // This line looks like it has shorthand MIDI
                    // Split by bit (:)
                    String[] bits = line.split(":");
                    String midiChannel = "";
                    String commandPart1 = "";
                    String commandPart2 = "";
                    String commandPart3 = "";
                    for (String bit:bits) {
                       if (bit.contains("MIDI")) {
                           midiChannel = valueToHexSingle(valueFromString(bit, "MIDI"));

                       } else if (bit.contains("NO")) {
                           commandPart1 = "0x9";
                           commandPart2 = valueToHex(valueFromString(bit,"NO"),false);

                       } else if (bit.contains("NX")) {
                           commandPart1 = "0x8";
                           commandPart2 = valueToHex(valueFromString(bit,"NX"),false);

                       } else if (bit.contains("PC")) {
                           commandPart1 = "0xC";
                           commandPart2 = valueToHex(valueFromString(bit,"PC"),false);

                       } else if (bit.contains("CC")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(valueFromString(bit,"CC"),false);

                       } else if (bit.contains("MSB")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(0,false);
                           commandPart3 = valueToHex(valueFromString(bit,"MSB"),false);

                       } else if (bit.contains("LSB")) {
                           commandPart1 = "0xB";
                           commandPart2 = "0x20";
                           commandPart3 = valueToHex(valueFromString(bit,"LSB"),false);

                       } else if (bit.contains("BBTX")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Transition(),false);
                           commandPart3 = valueToHex(0,false);

                       } else if (bit.contains("BBTN")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Transition(),false);
                           commandPart3 = valueToHex(127,false);

                       } else if (bit.contains("BBTP")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Transition(),false);
                           commandPart3 = valueToHex(126,false);

                       } else if (bit.contains("BBTEX")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Exclusive_transition(),false);
                           commandPart3 = valueToHex(0,false);

                       } else if (bit.contains("BBTEN")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Exclusive_transition(),false);
                           commandPart3 = valueToHex(127,false);

                       } else if (bit.contains("BBTEP")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Exclusive_transition(),false);
                           commandPart3 = valueToHex(126,false);

                       } else if (bit.contains("BBTE")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Exclusive_transition(),false);
                           commandPart3 = valueToHex(valueFromString(bit,"BBTE"),false);

                       } else if (bit.contains("BBT")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Transition(),false);
                           commandPart3 = valueToHex(valueFromString(bit,"BBT"),false);

                       } else if (bit.contains("BBHX")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Half_time(),false);
                           commandPart3 = valueToHex(0,false);

                       } else if (bit.contains("BBH")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Half_time(),false);
                           commandPart3 = valueToHex(1,false);

                       } else if (bit.contains("BBDX")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Double_time(),false);
                           commandPart3 = valueToHex(0,false);

                       } else if (bit.contains("BBD")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Double_time(),false);
                           commandPart3 = valueToHex(1,false);

                       } else if (bit.contains("BBBPM")) {
                           // This has two different messages combined
                           if (!midiChannel.isEmpty() && valueFromHex(midiChannel)>=0) {
                               // Temporarily change the BeatBuddy saved channel
                               int savedChannel = mainActivityInterface.getBeatBuddy().getBeatBuddyChannel();
                               mainActivityInterface.getBeatBuddy().setBeatBuddyChannel(valueFromHex(midiChannel));
                               commandPart1 = mainActivityInterface.getBeatBuddy().getTempoCode(valueFromString(bit,"BBBPM"));
                               commandPart2 = "_"; // So it is valid -  removed later
                               commandPart3 = "";
                               // Put the channel back
                               mainActivityInterface.getBeatBuddy().setBeatBuddyChannel(savedChannel);
                           }


                       } else if (bit.contains("BBS") && bit.contains("/")) {
                           // This has two different messages combined
                           bit = bit.replace("BBS", "");
                           String[] folderAndSong = bit.split("/");
                           if (folderAndSong.length == 2) {
                               if (!folderAndSong[0].replaceAll("\\D", "").isEmpty() &&
                                       !folderAndSong[1].replaceAll("\\D", "").isEmpty()) {
                                   int folder = Integer.parseInt(folderAndSong[0].replaceAll("\\D", ""));
                                   int song = Integer.parseInt(folderAndSong[1].replaceAll("\\D", ""));
                                   if (!midiChannel.isEmpty() && valueFromHex(midiChannel) >= 0) {
                                       // Temporarily change the BeatBuddy saved channel
                                       int savedChannel = mainActivityInterface.getBeatBuddy().getBeatBuddyChannel();
                                       mainActivityInterface.getBeatBuddy().setBeatBuddyChannel(valueFromHex(midiChannel));
                                       commandPart1 = mainActivityInterface.getBeatBuddy().getSongCode(folder, song);
                                       commandPart2 = "_"; // So it is valid -  removed later
                                       commandPart3 = "";
                                       mainActivityInterface.getBeatBuddy().setBeatBuddyChannel(savedChannel);
                                   }
                               }
                           }

                       } else if (bit.contains("BBV")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Mix_vol(),false);
                           commandPart3 = valueToHex(valueFromString(bit,"BBV"),false);

                       } else if (bit.contains("BBI")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Intro(),false);
                           commandPart3 = valueToHex(1, false);

                       } else if (bit.contains("BBO")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Outro(),false);
                           commandPart3 = valueToHex(1,false);

                       } else if (bit.contains("BBP")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Pause_unpause(),false);
                           commandPart3 = valueToHex(127,false);

                       } else if (bit.contains("BBF")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Drum_fill(),false);
                           commandPart3 = valueToHex(Math.round((mainActivityInterface.getBeatBuddy().getBeatBuddyVolume() / 100f) * 127f),false);

                       } else if (bit.contains("BBA")) {
                           commandPart1 = "0xB";
                           commandPart2 = valueToHex(mainActivityInterface.getBeatBuddy().getCC_Accent_hit(),false);
                           commandPart3 = valueToHex(Math.round((mainActivityInterface.getBeatBuddy().getBeatBuddyVolume() / 100f) * 127f),false);


                       } else if (!bit.isEmpty() && !bit.replaceAll("\\D","").isEmpty()) {
                           // This is the value part - the other bits were gathered already (hopefully!)
                           commandPart3 = valueToHex(valueFromString(bit,""),false);
                       }
                    }

                    // Now build the message back up (if ok)
                    StringBuilder newCommand = new StringBuilder();
                    if (!midiChannel.isEmpty() && !commandPart1.isEmpty() &&
                            (!commandPart2.isEmpty() || !commandPart3.isEmpty()))  {

                        // Add on the MIDI channel to command 1 if not prebuilt
                        if (!commandPart2.equals("_")) {
                            newCommand.append(commandPart1).append(midiChannel);
                        } else {
                            newCommand.append(commandPart1);
                        }

                        // Get rid of holders that aren't needed
                        commandPart2 = commandPart2.replace("_","");

                        if (!commandPart2.trim().isEmpty()) {
                            newCommand.append(" ").append(commandPart2.trim());
                        }

                        if (!commandPart3.trim().isEmpty()) {
                            newCommand.append(" ").append(commandPart3.trim());
                        }

                        fixedLines.append(newCommand).append("\n");

                        Log.d(TAG,"converted to:"+newCommand);
                    } else {
                        // Just put the line back as there was an issue
                        fixedLines.append(line).append("\n");
                    }

                } else {
                    // Just add the line back
                    fixedLines.append(line).append("\n");
                }
            }
            return fixedLines.toString();

        } else {
            // Just return the text
            return textToCheck;
        }
    }

    private int valueFromString(String bitToFix, String bitToRemove) {
        bitToFix = bitToFix.replace(bitToRemove,"").trim();
        bitToFix = bitToFix.replaceAll("\\D","").trim();
        if (!bitToFix.isEmpty()) {
            return Integer.parseInt(bitToFix);
        } else {
            return -1;
        }
    }

    private String valueToHex(int value, boolean decreaseNum) {
        if (decreaseNum) {
            // Decrease the number to match computer numbering starting at 0
            value = value - 1;
        }
        if (value >= 0) {
            return "0x" + String.format("%02X", value);
        } else {
            return "";
        }
    }

    private String valueToHexSingle(int value) {
        // Decrease the number to match computer numbering starting at 0
            value = value - 1;
        if (value>=0) {
            return Integer.toHexString(value).toUpperCase(Locale.ROOT);
        } else {
            return "";
        }
    }

    private int valueFromHex(String hex) {
        try {
            if (!hex.isEmpty()) {
                return Integer.parseInt(hex, 16);
            } else {
                return -1;
            }
        } catch (Exception e) {
            return -1;
        }

    }
}

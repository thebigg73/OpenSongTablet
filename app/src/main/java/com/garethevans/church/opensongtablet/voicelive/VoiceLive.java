package com.garethevans.church.opensongtablet.voicelive;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.HashMap;

public class VoiceLive {

    // This class is used to send automatic key and harmony to the VoiceLive3 Extreme
    // It takes the key from the song and converts it to a MIDI message

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "VoiceLive";
    private final MainActivityInterface mainActivityInterface;
    private int voiceLiveChannel = 1;
    private boolean voiceLiveSendKey = false;
    private boolean voiceLiveOverrideChannel = true;
    private String voiceLiveMajorHarmony, voiceLiveMinorHarmony;

    // The key information uses CC 30 and the values for the keys are as follows
    // C:0, C#:1, D:2, D#:3, E:4, F:5, F#:6, G:7, G#:8, A:9, A#:10, B:11
    @SuppressWarnings("FieldCanBeLocal")
    private final HashMap<String,Integer> keyMap = new HashMap<>();

    // The harmony information uses CC 31 and the values are as follows
    // MAJ1:0, MAJ2:1, MAJ3:2, MIN1:3, MIN2:4, MIN3:5, CUST:6
    @SuppressWarnings("FieldCanBeLocal")
    private final HashMap<String,Integer> majorHarmonies = new HashMap<>();
    private final HashMap<String,Integer> minorHarmonies = new HashMap<>();
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int customHarmonies;

    // CC values used in the VoiceLive Extreme 3
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_HarmonyVibratoBoost = 1, CC_GuitarRhythmic = 16, CC_GuitarDelay = 17,
            CC_GuitarCompressor = 19, CC_GuitarMod = 21, CC_GuitarOctaver = 23, CC_GuitarAmp = 25,
            CC_GuitarWah = 27, CC_GuitarBoost = 29, CC_VocalHarmonyKey = 30, CC_VocalHarmonyScale = 31,
            CC_GuitarReverb = 46, CC_GuitarHIT = 47, CC_VocalVocoderSynth = 50, CC_VocalRhythmic = 51,
            CC_VocalHIT = 56, CC_VocalChoir = 104, CC_VocalHarmony = 110, CC_VocalDouble = 111,
            CC_VocalReverb = 112, CC_VocalHardTune = 113, CC_Step = 115, CC_VocalMod = 116,
            CC_VocalDelay = 117, CC_VocalTransducer = 118, CC_HarmonyHold = 119, CC_AllNotesOff = 123;

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int value_on = 127, value_off = 0;

    public VoiceLive(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        getPreferences();

        // Set the keyMap
        keyMap.put("C", 0);
        keyMap.put("Cm", 0);
        keyMap.put("C#", 1);
        keyMap.put("C#m", 1);
        keyMap.put("Db", 1);
        keyMap.put("Dbm", 1);
        keyMap.put("D", 2);
        keyMap.put("Dm", 2);
        keyMap.put("D#", 3);
        keyMap.put("D#b", 3);
        keyMap.put("Eb", 3);
        keyMap.put("Ebm", 3);
        keyMap.put("E", 4);
        keyMap.put("Em", 4);
        keyMap.put("F", 5);
        keyMap.put("Fm", 5);
        keyMap.put("F#", 6);
        keyMap.put("F#m", 6);
        keyMap.put("Gb", 6);
        keyMap.put("Gbm", 6);
        keyMap.put("G", 7);
        keyMap.put("Gm", 7);
        keyMap.put("G#", 8);
        keyMap.put("G#m", 8);
        keyMap.put("Ab", 8);
        keyMap.put("Abm", 8);
        keyMap.put("A", 9);
        keyMap.put("Am", 9);
        keyMap.put("A#", 10);
        keyMap.put("A#m", 10);
        keyMap.put("Bb", 10);
        keyMap.put("Bbm", 10);
        keyMap.put("B", 11);
        keyMap.put("Bm", 11);

        // Set the majorHarmonies
        majorHarmonies.put("MAJ1", 0);
        majorHarmonies.put("MAJ2", 1);
        majorHarmonies.put("MAJ3", 2);

        // Set the minorHarmonies
        minorHarmonies.put("MIN1", 3);
        minorHarmonies.put("MIN2", 4);
        minorHarmonies.put("MIN3", 5);

        // Set the customHarmonies
        customHarmonies = 6;
    }

    private void getPreferences() {
        voiceLiveChannel = mainActivityInterface.getPreferences().getMyPreferenceInt("voiceLiveChannel", 1);
        voiceLiveSendKey = mainActivityInterface.getPreferences().getMyPreferenceBoolean("voiceLiveSendKey", false);
        voiceLiveOverrideChannel = mainActivityInterface.getPreferences().getMyPreferenceBoolean("voiceLiveOverrideChannel", true);
        voiceLiveMinorHarmony = mainActivityInterface.getPreferences().getMyPreferenceString("voiceLiveMinorHarmony", "MIN1");
        voiceLiveMajorHarmony = mainActivityInterface.getPreferences().getMyPreferenceString("voiceLiveMajorHarmony", "MAJ1");
    }

    public int getVoiceLiveChannel() {
        return voiceLiveChannel;
    }

    public void setVoiceLiveChannel(int voiceLiveChannel) {
        this.voiceLiveChannel = voiceLiveChannel;
        mainActivityInterface.getPreferences().setMyPreferenceInt("voiceLiveChannel", voiceLiveChannel);
    }

    public boolean getVoiceLiveOverrideChannel() {
        return voiceLiveOverrideChannel;
    }

    public void setVoiceLiveOverrideChannel(boolean voiceLiveOverrideChannel) {
        this.voiceLiveOverrideChannel = voiceLiveOverrideChannel;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("voiceLiveOverrideChannel", voiceLiveOverrideChannel);
    }

    // Get the midi channel to use (default or actual one sent)
    private int midiChannelToUse(int midiChannelSent) {
        return voiceLiveOverrideChannel ? voiceLiveChannel - 1 : midiChannelSent - 1;
    }

    public boolean getVoiceLiveSendKey() {
        return voiceLiveSendKey;
    }

    public void setVoiceLiveSendKey(boolean voiceLiveSendKey) {
        this.voiceLiveSendKey = voiceLiveSendKey;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("voiceLiveSendKey", voiceLiveSendKey);
    }

    public String getVoiceLiveMajorHarmony() {
        return voiceLiveMajorHarmony;
    }

    public void setVoiceLiveMajorHarmony(String voiceLiveMajorHarmony) {
        this.voiceLiveMajorHarmony = voiceLiveMajorHarmony;
        mainActivityInterface.getPreferences().setMyPreferenceString("voiceLiveMajorHarmony", voiceLiveMajorHarmony);
    }

    public String getVoiceLiveMinorHarmony() {
        return voiceLiveMinorHarmony;
    }

    public void setVoiceLiveMinorHarmony(String voiceLiveMinorHarmony) {
        this.voiceLiveMinorHarmony = voiceLiveMinorHarmony;
        mainActivityInterface.getPreferences().setMyPreferenceString("voiceLiveMinorHarmony", voiceLiveMinorHarmony);
    }

    public int tryAutoSend(Song song) {
        // Try sending the auto key message and return the delay for the number of messages
        return mainActivityInterface.getMidi().sendMidiHexSequence(getVoiceLiveKeyMessage(song));
    }

    // The default key message (uses the MIDI channel preference)
    @SuppressWarnings("NullPointerException")
    public String getVoiceLiveKeyMessage(Song song) {
        // Get the song key if it exists
        if (voiceLiveSendKey && song.getKey()!=null &&
            !song.getKey().isEmpty()) {

            // Get the key default harmony based on the song key
            int defHarmony;
            if (song.getKey().contains("m")) {
                Integer harmony = minorHarmonies.get(voiceLiveMinorHarmony);
                defHarmony = harmony!=null ? harmony : 3;
            } else {
                Integer harmony = majorHarmonies.get(voiceLiveMajorHarmony);
                defHarmony = harmony!=null ? harmony : 0;
            }
            // If the song has a user 1, 2, or 3 value that has the harmony scale, then use that
            if (song.getUser1().contains("MAJ1") || song.getUser2().contains("MAJ1") || song.getUser3().contains("MAJ1")) {
                defHarmony = 0;
            } else if (song.getUser1().contains("MAJ2") || song.getUser2().contains("MAJ2") || song.getUser3().contains("MAJ2")) {
                defHarmony = 1;
            } else if (song.getUser1().contains("MAJ3") || song.getUser2().contains("MAJ3") || song.getUser3().contains("MAJ3")) {
                defHarmony = 2;
            } else if (song.getUser1().contains("MIN1") || song.getUser2().contains("MIN1") || song.getUser3().contains("MIN1")) {
                defHarmony = 3;
            } else if (song.getUser1().contains("MIN2") || song.getUser2().contains("MIN2") || song.getUser3().contains("MIN2")) {
                defHarmony = 4;
            } else if (song.getUser1().contains("MIN3") || song.getUser2().contains("MIN3") || song.getUser3().contains("MIN3")) {
                defHarmony = 5;
            }

            int defKey;
            Integer key = keyMap.get(song.getKey().replace("m", ""));
            defKey = key!=null ? key:-1;


            if (defKey>-1) {
                return mainActivityInterface.getMidi().buildMidiString("CC", voiceLiveChannel - 1, CC_VocalHarmonyKey, defKey) +
                        "\n" + mainActivityInterface.getMidi().buildMidiString("CC", voiceLiveChannel - 1, CC_VocalHarmonyScale, defHarmony);
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public String getVoiceLivePresetMessage(int midiChannelSent, int presetNumber) {
        // The preset numbers are achieved by using a combination of MSB,LSB,Program Change
        // Patches 1-128 use LSB 0
        // Patches 129-256 use LSB1
        // Patches 257-384 use LSB2
        // Patches 385-500 use LSB3

        // Get the MSB code
        String msbCode = mainActivityInterface.getMidi().buildMidiString("MSB", midiChannelToUse(midiChannelSent), 0, 0);

        // Get the LSB code
        String lsbCode;
        String programCode;
        if (presetNumber < 1 || presetNumber > 500) {
            presetNumber = 1;
        }

        // Preset info sends on the third byte for MSB,LBS,PC
        if (presetNumber >= 385) {
            lsbCode = mainActivityInterface.getMidi().buildMidiString("LSB", midiChannelToUse(midiChannelSent), 0, 3);
            programCode = mainActivityInterface.getMidi().buildMidiString("PC", midiChannelToUse(midiChannelSent), 0, presetNumber - 384 - 1);
        } else if (presetNumber >= 257) {
            lsbCode = mainActivityInterface.getMidi().buildMidiString("LSB", midiChannelToUse(midiChannelSent), 0, 2);
            programCode = mainActivityInterface.getMidi().buildMidiString("PC", midiChannelToUse(midiChannelSent), 0, presetNumber - 256 - 1);
        } else if (presetNumber >= 129) {
            lsbCode = mainActivityInterface.getMidi().buildMidiString("LSB", midiChannelToUse(midiChannelSent), 0, 1);
            programCode = mainActivityInterface.getMidi().buildMidiString("PC", midiChannelToUse(midiChannelSent), 0, presetNumber - 128 - 1);
        } else {
            lsbCode = mainActivityInterface.getMidi().buildMidiString("LSB", midiChannelToUse(midiChannelSent), 0, 0);
            programCode = mainActivityInterface.getMidi().buildMidiString("PC", midiChannelToUse(midiChannelSent), 0, presetNumber - 1);
        }

        return msbCode + "\n" + lsbCode + "\n" + programCode;
    }

    public String getMessageFromShortHand(int midiChannelSent, String shorthand) {
        // A list of suitable shorthand MIDI messages are found on the Midi.java class
        // VL signifies they are for the voicelive
        if (shorthand.startsWith("VL")) {
            // Get rid of the VoiceLive VL part
            shorthand = shorthand.substring(2);

            // Get the on/off command
            boolean on = !shorthand.endsWith("X");
            shorthand = shorthand.replace("X","");

            // Get any numerical value from the message
            int num = -1;
            boolean isHarmonyScale = shorthand.contains("VHS");
            if (!shorthand.replaceAll("\\D","").isEmpty() && !isHarmonyScale) {
                num = Integer.parseInt(shorthand.replaceAll("\\D",""));
                shorthand = shorthand.replace(String.valueOf(num),"");
            }

            // Get any key from the message
            int keyNum = -1;
            if (shorthand.startsWith("VHK")) {
                String keyText = shorthand.replace("VHK","");
                shorthand = shorthand.replace(keyText,"");
                Integer key = keyMap.get(keyText.replace("m", ""));
                keyNum = key!=null ? key:-1;
            }

            // Get any harmony from the message
            int harmonyNum = -1;
            if (shorthand.startsWith("VHS")) {
                String harmonyText = shorthand.replace("VHS","").toUpperCase();
                shorthand = shorthand.replace(harmonyText,"");
                if (harmonyText.startsWith("MAJ")) {
                    Integer harmony = majorHarmonies.get(harmonyText);
                    harmonyNum = harmony!=null ? harmony:-1;

                } else if (harmonyText.startsWith("MIN")) {
                    Integer harmony = minorHarmonies.get(harmonyText);
                    harmonyNum = harmony!=null ? harmony:-1;

                } else {
                    harmonyNum = customHarmonies;
                }
            }

            // If it now starts with 'G' it is guitar, if 'V' it is vocal,
            // if it is 'P' it is a preset, if it is 'S' it is a step
            switch (shorthand) {
                case "GR":
                    return getGuitarRhythmicMessage(midiChannelSent,on);
                case "GD":
                    return getGuitarDelayMessage(midiChannelSent, on);
                case "GC":
                    return getGuitarCompressorMessage(midiChannelSent,on);
                case "GM":
                    return getGuitarModMessage(midiChannelSent,on);
                case "GO":
                    return getGuitarOctaverMessage(midiChannelSent,on);
                case "GA":
                    return getGuitarAmpMessage(midiChannelSent,on);
                case "GW":
                    return getGuitarWahMessage(midiChannelSent,on);
                case "GB":
                    return getGuitarBoostMessage(midiChannelSent,on);
                case "GHIT":
                    return getGuitarHITMessage(midiChannelSent,on);
                case "GRV":
                    return getGuitarReverbMessage(midiChannelSent,on);
                case "VH":
                    return getVocalHarmonyMessage(midiChannelSent,on);
                case "VHK":
                    if (keyNum>-1) {
                        return getVocalHarmonyKeyMessage(midiChannelSent, keyNum);
                    } else {
                        return "";
                    }
                case "VHS":
                    if (harmonyNum>-1) {
                        return getVocalHarmonyScaleMessage(midiChannelSent, harmonyNum);
                    } else {
                        return "";
                    }
                case "VHVB":
                    return getHarmonyVibratoBoostMessage(midiChannelSent,on);
                case "VV":
                    return getVocalVocoderSynthMessage(midiChannelSent,on);
                case "VR":
                    return getVocalRhythmicMessage(midiChannelSent,on);
                case "VHIT":
                    return getVocalHITMessage(midiChannelSent,on);
                case "VCH":
                    return getVocalChoirMessage(midiChannelSent,on);
                case "VDB":
                    return getVocalDoubleMessage(midiChannelSent,on);
                case "VRV":
                    return getVocalReverbMessage(midiChannelSent,on);
                case "VHT":
                    return getVocalHardTuneMessage(midiChannelSent,on);
                case "VM":
                    return getVocalModMessage(midiChannelSent,on);
                case "VD":
                    return getVocalDelayMessage(midiChannelSent,on);
                case "VT":
                    return getVocalTransducerMessage(midiChannelSent,on);
                case "VHH":
                    return getHarmonyHoldMessage(midiChannelSent,on);
                case "NX":
                case "N":
                    return getAllNotesOffMessage(midiChannelSent);
                case "P":
                    if (num>-1) {
                        return getVoiceLivePresetMessage(midiChannelSent, num);
                    } else {
                        return "";
                    }
                case "S":
                    if (num>-1) {
                        return getStepMessage(midiChannelSent, num);
                    } else {
                        return "";
                    }
                default:
                    return "";
            }


        } else {
            return "";
        }
    }

    public String getHarmonyVibratoBoostMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_HarmonyVibratoBoost, on ? value_on:value_off);
    }

    public String getGuitarRhythmicMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_GuitarRhythmic, on ? value_on:value_off);
    }

    public String getGuitarDelayMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_GuitarDelay, on ? value_on:value_off);
    }

    public String getGuitarCompressorMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_GuitarCompressor, on ? value_on:value_off);
    }

    public String getGuitarModMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_GuitarMod, on ? value_on:value_off);
    }

    public String getGuitarOctaverMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_GuitarOctaver, on ? value_on:value_off);
    }

    public String getGuitarAmpMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_GuitarAmp, on ? value_on : value_off);
    }

    public String getGuitarWahMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_GuitarWah, on ? value_on : value_off);
    }

    public String getGuitarBoostMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_GuitarBoost, on ? value_on : value_off);
    }

    public String getGuitarHITMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_GuitarHIT, on ? value_on : value_off);
    }

    public String getGuitarReverbMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_GuitarReverb, on ? value_on : value_off);
    }

    public String getVocalHarmonyMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalHarmony, on ? value_on : value_off);
    }

    public String getVocalHarmonyKeyMessage(int midiChannelSent, int keyNum) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalHarmonyKey, keyNum);
    }

    public String getVocalHarmonyScaleMessage(int midiChannelSent, int harmonyNum) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalHarmonyScale, harmonyNum);
    }

    public String getVocalVocoderSynthMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalVocoderSynth, on ? value_on : value_off);
    }

    public String getVocalRhythmicMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalRhythmic, on ? value_on : value_off);
    }

    public String getVocalHITMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalHIT, on ? value_on : value_off);
    }

    public String getVocalChoirMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalChoir, on ? value_on : value_off);
    }

    public String getVocalDoubleMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalDouble, on ? value_on : value_off);
    }

    public String getVocalReverbMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalReverb, on ? value_on : value_off);
    }

    public String getVocalHardTuneMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalHardTune, on ? value_on : value_off);
    }

    public String getVocalModMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalMod, on ? value_on : value_off);
    }


    public String getVocalDelayMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalDelay, on ? value_on : value_off);
    }

    public String getVocalTransducerMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_VocalTransducer, on ? value_on : value_off);
    }

    public String getHarmonyHoldMessage(int midiChannelSent, boolean on) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_HarmonyHold, on ? value_on : value_off);
    }

    public String getStepMessage(int midiChannelSent, int value) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_Step, value<1||value>127 ? 1:value);
    }

    public String getAllNotesOffMessage(int midiChannelSent) {
        return mainActivityInterface.getMidi().buildMidiString("CC", midiChannelToUse(midiChannelSent), CC_AllNotesOff, value_off);
    }
}

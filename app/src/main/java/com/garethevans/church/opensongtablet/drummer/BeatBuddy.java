package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class BeatBuddy {

    // This holds the settings for the BeatBuddy MIDI companion
    // These are edited by the BeatBuddyFragment

    final private MainActivityInterface mainActivityInterface;
    final private String TAG = "BeatBuddy";

    // Control options are:
    private int beatBuddyChannel = 1, beatBuddyVolume = 100, beatBuddyDrumKit = 1;
    private byte[] commandStart, commandStop, commandAccent, commandFill, commandTransition1,
            commandTransition2, commandTransition3, commandTransitionNext, commandTransitionExit,
            commandHalfTime, commandHalfTimeExit, commandDoubleTime, commandDoubleTimeExit, commandPauseToggle;

    private boolean beatBuddyIncludeSong, beatBuddyIncludeTempo, beatBuddyIncludeVolume, beatBuddyIncludeDrumKit;

    public BeatBuddy(Context c) {
        this.mainActivityInterface = (MainActivityInterface) c;
        setPrefs();
        buildCommands();
    }

    public void setPrefs() {
        beatBuddyChannel = mainActivityInterface.getPreferences().getMyPreferenceInt("beatBuddyChannel",1);
        beatBuddyIncludeSong = mainActivityInterface.getPreferences().getMyPreferenceBoolean("beatBuddyIncludeSong",false);
        beatBuddyIncludeTempo = mainActivityInterface.getPreferences().getMyPreferenceBoolean("beatBuddyIncludeTempo",false);
        beatBuddyIncludeVolume = mainActivityInterface.getPreferences().getMyPreferenceBoolean("beatBuddyIncludeVolume",false);
        beatBuddyIncludeDrumKit = mainActivityInterface.getPreferences().getMyPreferenceBoolean("beatBuddyIncludeDrumKit",false);
    }

    // Commands received from gestures and sent via MIDI to connected BeatBuddy
    public void buildCommands() {
        commandStart = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", beatBuddyChannel, 114,beatBuddyVolume));
        commandStop = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,115,beatBuddyVolume));
        commandAccent = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,110,beatBuddyVolume));
        commandFill = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,112,beatBuddyVolume));
        commandTransition1 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,113,1));
        commandTransition2 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,113,2));
        commandTransition3 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,113,3));
        commandTransitionNext = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,113,127));
        commandTransitionExit = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,113,0));
        commandHalfTime = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,82,1));
        commandHalfTimeExit = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,82,0));
        commandDoubleTime = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,83,1));
        commandDoubleTimeExit = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,83,0));
        commandPauseToggle = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,111,127));
    }
    public void beatBuddyStart() {
        // Send a MIDI command to start the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandStart);
        }
    }
    public void beatBuddyStop() {
        // Send a MIDI command to stop the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandStop);
        }
    }
    public void beatBuddyAccent() {
        // Send a MIDI command to play an accent on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandAccent);
        }
    }
    public void beatBuddyFill() {
        // Send a MIDI command to play an fill on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandFill);
        }
    }
    public void beatBuddyPause() {
        // Send a MIDI command to toggle pause on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandPauseToggle);
        }
    }
    public void beatBuddyTransition1() {
        // Send a MIDI command to start transition 1 on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandTransition1);
        }
    }
    public void beatBuddyTransition2() {
        // Send a MIDI command to start transition 2 on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandTransition2);
        }
    }
    public void beatBuddyTransition3() {
        // Send a MIDI command to start transition 3 on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandTransition3);
        }
    }
    public void beatBuddyTransitionNext() {
        // Send a MIDI command to start the next transition on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandTransitionNext);
        }
    }
    public void beatBuddyTransitionExit() {
        // Send a MIDI command to stop the transition on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandTransitionExit);
        }
    }

    public void beatBuddyHalfTime() {
        // Send a MIDI command to play half time on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandHalfTime);
        }
    }
    public void beatBuddyHalfTimeExit() {
        // Send a MIDI command to stop half time on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandHalfTimeExit);
        }
    }

    public void beatBuddyDoubleTime() {
        // Send a MIDI command to play double time on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandDoubleTime);
        }
    }
    public void beatBuddyDoubleTimeExit() {
        // Send a MIDI command to stop double time on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandDoubleTimeExit);
        }
    }


    // The getters and setters
    public int getBeatBuddyChannel() {
        return beatBuddyChannel;
    }
    public int getBeatBuddyVolume() {
        return beatBuddyVolume;
    }
    public boolean getBeatBuddyIncludeSong() {
        return beatBuddyIncludeSong;
    }
    public boolean getBeatBuddyIncludeTempo() {
        return beatBuddyIncludeTempo;
    }
    public boolean getBeatBuddyIncludeVolume() {
        return beatBuddyIncludeVolume;
    }
    public boolean getBeatBuddyIncludeDrumKit() {
        return beatBuddyIncludeDrumKit;
    }
    public int getBeatBuddyDrumKit() {
        return beatBuddyDrumKit;
    }

    public void setBeatBuddyChannel(int beatBuddyChannel) {
        this.beatBuddyChannel = beatBuddyChannel;
        mainActivityInterface.getPreferences().setMyPreferenceInt("beatBuddyChannel",beatBuddyChannel);
    }
    public void setBeatBuddyVolume(int beatBuddyVolume) {
        this.beatBuddyVolume = beatBuddyVolume;
    }
    public void setBeatBuddyIncludeSong(boolean beatBuddyIncludeSong) {
        this.beatBuddyIncludeSong = beatBuddyIncludeSong;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("beatBuddyIncludeSong",beatBuddyIncludeSong);
    }
    public void setBeatBuddyIncludeTempo(boolean beatBuddyIncludeTempo) {
        this.beatBuddyIncludeTempo = beatBuddyIncludeTempo;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("beatBuddyIncludeTempo",beatBuddyIncludeTempo);
    }
    public void setBeatBuddyIncludeVolume(boolean beatBuddyIncludeVolume) {
        this.beatBuddyIncludeVolume = beatBuddyIncludeVolume;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("beatBuddyIncludeVolume",beatBuddyIncludeVolume);
    }
    public void setBeatBuddyIncludeDrumKit(boolean beatBuddyIncludeDrumKit) {
        this.beatBuddyIncludeDrumKit = beatBuddyIncludeDrumKit;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("beatBuddyIncludeDrumKit",beatBuddyIncludeDrumKit);
    }
    public void setBeatBuddyDrumKit(int beatBuddyDrumKit) {
        this.beatBuddyDrumKit = beatBuddyDrumKit;
    }

    // Calculations
    public String getSongCode(int folderNumber, int songNumber) {
        // Folder code is two parts
        // Part 1 =FLOOR((B4-1)/128)
        // Part 2 =MOD(B4-1,128)
        int part1Dec = (int)Math.floor((float)(folderNumber-1)/128f);
        int part2Dec = Math.floorMod(folderNumber-1,128);
        Log.d(TAG,"part1Dec:"+part1Dec+"  part2Dec:"+part2Dec);

        // Song number is simpler.  Just take away 1
        songNumber = songNumber - 1;

        String code = mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel-1,0,part1Dec);
        code += "\n" + mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel-1,32,part2Dec);
        code += "\n" + mainActivityInterface.getMidi().buildMidiString("PC",beatBuddyChannel-1,-1,songNumber);
        Log.d(TAG,"code:"+code);
        return code;
    }

    public String getTempoCode(int tempo) {
        // Tempo code is two parts
        // Part 1 =FLOOR((tempo)/128)
        // Part 2 =MOD(tempo,128)
        int part1Dec = (int)Math.floor((float)tempo/128f);
        int part2Dec = Math.floorMod(tempo,128);
        String code = mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel-1,106,part1Dec);
        code += "\n" + mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel-1,107,part2Dec);
        Log.d(TAG,"code:"+code);
        return code;
    }

    public String getVolumeCode() {
        // Volumes will be between 0 and 100
        return mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel-1,108,beatBuddyVolume);
    }

    public String getDrumKitCode() {
        // The drumKitCode will be between 1 and 128.  Decrease by 1 for MIDI
        return mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel-1,116,beatBuddyDrumKit-1);
    }
}

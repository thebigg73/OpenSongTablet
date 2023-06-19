package com.garethevans.church.opensongtablet.beatbuddy;

import android.content.Context;
import android.os.Build;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class BeatBuddy {

    // This holds the settings for the BeatBuddy MIDI companion
    // These are edited by the BeatBuddyFragment

    final private MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    final private String TAG = "BeatBuddy";

    // Control options are:
    private int beatBuddyChannel = 1, beatBuddyVolume = 100, beatBuddyDrumKit = 1;
    private byte[] commandStart, commandStop, commandAccent, commandFill, commandTransition1,
            commandTransition2, commandTransition3, commandTransitionNext, commandTransitionPrev,
            commandTransitionExit, commandExclusiveTransition1, commandExclusiveTransition2,
            commandExclusiveTransition3, commandExclusiveTransitionNext, commandExclusiveTransitionPrev,
            commandExclusiveTransitionExit, commandHalfTime, commandHalfTimeExit, commandDoubleTime,
            commandDoubleTimeExit, commandPauseToggle;

    private boolean beatBuddyIncludeSong, beatBuddyIncludeTempo, beatBuddyIncludeVolume,
            beatBuddyIncludeDrumKit, beatBuddyAerosMode, beatBuddyUseImported, beatBuddyAutoLookup,
            metronomeSyncWithBeatBuddy;

    // The commond CC commands.  In one easy readable place!
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Folder_MSB = 0;                // val 0-127
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Half_time = 82;                // val 1-127=on   val 0=off
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Double_time = 83;              // val 1-127=on   val 0=off
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Folder_LSB = 32;               // val 0-127
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Tempo_MSB = 106;               // val 0-127 (BeatBuddy only listens for 0-2 though as max tempo is 300)
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Tempo_LSB = 107;               // val 0-127 (BeatBuddy only listens for 40-127 though as min tempo is 40)
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Mix_vol = 108;                 // val 1-100
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_HP_vol = 109;                  // val 1-100
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Accent_hit = 110;              // val 0-127 (any to trigger at that volume relative to output vol)
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Pause_unpause = 111;           // val 0=unpause, 1=pause, 2-127=toggle
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Drum_fill = 112;               // val 0-127 (any to trigger at that volume relative to output vol)
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Transition = 113;              // val 1-33 for part, val 126=prev, val 127=next, val 0=end transition and move to part
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Intro = 114;                   // val 1-127=on val 0=ignored
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Outro = 115;                   // val 1-127=on val 0=ignored
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Drum_kit = 116;                 // val 1-127 for that drum kit
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int CC_Exclusive_transition = 121;    // val 1-33 for part, val 126=prev, val 127=next, val 0=end transition and move to part

    public int getCC_Mix_vol() {
        return CC_Mix_vol;
    }
    public int getCC_Tempo_MSB() {
        return CC_Tempo_MSB;
    }
    public int getCC_Tempo_LSB() {
        return CC_Tempo_LSB;
    }
    public int getCC_Drum_kit() {
        return CC_Drum_kit;
    }
    // Initialise this helper class
    public BeatBuddy(Context c) {
        this.mainActivityInterface = (MainActivityInterface) c;
        setPrefs();
        buildCommands();
    }

    // Load in the user preferences.  These can be changed from the helper class fragment
    // This updates the references here and saves the user preference in the set..() methods
    public void setPrefs() {
        beatBuddyChannel = mainActivityInterface.getPreferences().getMyPreferenceInt("beatBuddyChannel",1);
        beatBuddyIncludeSong = mainActivityInterface.getPreferences().getMyPreferenceBoolean("beatBuddyIncludeSong",false);
        beatBuddyIncludeTempo = mainActivityInterface.getPreferences().getMyPreferenceBoolean("beatBuddyIncludeTempo",false);
        beatBuddyIncludeVolume = mainActivityInterface.getPreferences().getMyPreferenceBoolean("beatBuddyIncludeVolume",false);
        beatBuddyIncludeDrumKit = mainActivityInterface.getPreferences().getMyPreferenceBoolean("beatBuddyIncludeDrumKit",false);
        beatBuddyAerosMode = mainActivityInterface.getPreferences().getMyPreferenceBoolean("beatBuddyAerosMode",true);
        beatBuddyUseImported = mainActivityInterface.getPreferences().getMyPreferenceBoolean("beatBuddyUseImported",false);
        beatBuddyAutoLookup = mainActivityInterface.getPreferences().getMyPreferenceBoolean("beatBuddyAutoLookup",true);
        metronomeSyncWithBeatBuddy = mainActivityInterface.getPreferences().getMyPreferenceBoolean("metronomeSyncWithBeatBuddy",false);
    }

    // Commands received from gestures and sent via MIDI to connected BeatBuddy
    // These are prebuilt and stored rather than building each time to avoid any delay when performing
    // These get rebuilt when any changes are made to the MIDI channel or volume via the helper fragment
    public void buildCommands() {
        commandStart = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", beatBuddyChannel, CC_Intro,1));
        commandStop = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Outro,1));
        commandAccent = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Accent_hit,Math.round((beatBuddyVolume/100f)*127f)));
        commandFill = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Drum_fill,Math.round((beatBuddyVolume/100f)*127f)));
        commandTransition1 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Transition,1));
        commandTransition2 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Transition,2));
        commandTransition3 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Transition,3));
        commandTransitionPrev = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Transition,126));
        commandTransitionNext = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Transition,127));
        commandTransitionExit = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Transition,0));
        commandExclusiveTransition1 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Exclusive_transition,1));
        commandExclusiveTransition2 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Exclusive_transition,2));
        commandExclusiveTransition3 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Exclusive_transition,3));
        commandExclusiveTransitionPrev = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Exclusive_transition,126));
        commandExclusiveTransitionNext = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Exclusive_transition,127));
        commandExclusiveTransitionExit = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Exclusive_transition,0));
        commandHalfTime = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Half_time,1));
        commandHalfTimeExit = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Half_time,0));
        commandDoubleTime = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Double_time,1));
        commandDoubleTimeExit = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Double_time,0));
        commandPauseToggle = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,CC_Pause_unpause,127));
    }

    public int tryAutoSend(Context c,MainActivityInterface mainActivityInterface, Song thisSong) {
        if (getBeatBuddyAutoLookup()) {
            try (BBSQLite bbsqLite = new BBSQLite(c)) {
                return bbsqLite.checkAutoBeatBuddy(c, mainActivityInterface, thisSong);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            return 0;
        }
    }
    // These are the controls called by the PerformanceGestures (pedals, page buttons or gestures)
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
    public void beatBuddyTransitionPrev() {
        // Send a MIDI command to start the next transition on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandTransitionPrev);
        }
    }
    public void beatBuddyTransitionExit() {
        // Send a MIDI command to stop the transition on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandTransitionExit);
        }
    }
    public void beatBuddyExclusiveTransition1() {
        // Send a MIDI command to start transition 1 on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandExclusiveTransition1);
        }
    }
    public void beatBuddyExclusiveTransition2() {
        // Send a MIDI command to start transition 2 on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandExclusiveTransition2);
        }
    }
    public void beatBuddyExclusiveTransition3() {
        // Send a MIDI command to start transition 3 on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandExclusiveTransition3);
        }
    }
    public void beatBuddyExclusiveTransitionNext() {
        // Send a MIDI command to start the next transition on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandExclusiveTransitionNext);
        }
    }
    public void beatBuddyExclusiveTransitionPrev() {
        // Send a MIDI command to start the next transition on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandExclusiveTransitionPrev);
        }
    }
    public void beatBuddyExclusiveTransitionExit() {
        // Send a MIDI command to stop the transition on the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandExclusiveTransitionExit);
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


    // The getters and setters for the helper fragment
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
    public boolean getBeatBuddyAerosMode() {
        return beatBuddyAerosMode;
    }
    public boolean getBeatBuddyUseImported() {
        return beatBuddyUseImported;
    }
    public boolean getBeatBuddyAutoLookup() {
        return beatBuddyAutoLookup;
    }
    public boolean getMetronomeSyncWithBeatBuddy() {
        return metronomeSyncWithBeatBuddy;
    }
    public void setBeatBuddyChannel(int beatBuddyChannel) {
        this.beatBuddyChannel = beatBuddyChannel;
        mainActivityInterface.getPreferences().setMyPreferenceInt("beatBuddyChannel",beatBuddyChannel);
        buildCommands();
    }
    public void setBeatBuddyVolume(int beatBuddyVolume) {
        this.beatBuddyVolume = beatBuddyVolume;
        buildCommands();
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
    public void setBeatBuddyAerosMode(boolean beatBuddyAerosMode) {
        this.beatBuddyAerosMode = beatBuddyAerosMode;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("beatBuddyAerosMode",beatBuddyAerosMode);
    }
    public void setBeatBuddyUseImported(boolean beatBuddyUseImported) {
        this.beatBuddyUseImported = beatBuddyUseImported;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("beatBuddyUseImported",beatBuddyUseImported);
    }
    public void setBeatBuddyAutoLookup(boolean beatBuddyAutoLookup) {
        this.beatBuddyAutoLookup = beatBuddyAutoLookup;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("beatBuddyAutoLookup",beatBuddyAutoLookup);
        // If we switch this on manually, also switch on the send MIDI automatically as they are linked
        if (beatBuddyAutoLookup) {
            mainActivityInterface.getMidi().setMidiSendAuto(true);
        }
    }
    public void setMetronomeSyncWithBeatBuddy(boolean metronomeSyncWithBeatBuddy) {
        this.metronomeSyncWithBeatBuddy = metronomeSyncWithBeatBuddy;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("metronomeSyncWithBeatBuddy",metronomeSyncWithBeatBuddy);
    }

    // Calculations for working out codes
    public String getSongCode(int folderNumber, int songNumber) {
        // Folder code is two parts unless we are in AerosMode (no MSB)
        // Part 1 =FLOOR((B4-1)/128)
        // Part 2 =MOD(B4-1,128)
        int part1Dec = 0;
        if (!beatBuddyAerosMode) {
            part1Dec = (int) Math.floor((float) (folderNumber - 1) / 128f);
        }
        int part2Dec = Math.floorMod(folderNumber-1,128);

        // Song number is simpler.  Just take away 1
        songNumber = songNumber - 1;

        String code = "";
        if (!beatBuddyAerosMode) {
            code = mainActivityInterface.getMidi().buildMidiString("CC", beatBuddyChannel - 1, CC_Folder_MSB, part1Dec);
        }
        code += "\n" + mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel-1,CC_Folder_LSB,part2Dec);
        code += "\n" + mainActivityInterface.getMidi().buildMidiString("PC",beatBuddyChannel-1,-1,songNumber);
        return code.trim();
    }

    public String getTempoCode(int tempo) {
        // Tempo code is two parts
        // Part 1 =FLOOR((tempo)/128)
        // Part 2 =MOD(tempo,128)
        int part1Dec = (int)Math.floor((float)tempo/128f);
        int part2Dec = Math.floorMod(tempo,128);
        String code = mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel-1,CC_Tempo_MSB,part1Dec);
        code += "\n" + mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel-1,CC_Tempo_LSB,part2Dec);
        return code;
    }

    public String getVolumeCode() {
        // Volumes will be between 0 and 100
        return mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel-1,CC_Mix_vol,beatBuddyVolume);
    }

    public String getDrumKitCode() {
        // The drumKitCode will be between 1 and 127.
        return mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel-1,CC_Drum_kit,beatBuddyDrumKit);
    }
    public String getDrumKitCode(int kitNum) {
        // The drumKitCode will be between 1 and 127.
        return mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel-1,CC_Drum_kit,kitNum);
    }

    public int getCC_Transition() {
        return CC_Transition;
    }
    public int getCC_Exclusive_transition() {
        return CC_Exclusive_transition;
    }
    public int getCC_Half_time() {
        return CC_Half_time;
    }
    public int getCC_Double_time() {
        return CC_Double_time;
    }
    public int getCC_Pause_unpause() {
        return CC_Pause_unpause;
    }
    public int getCC_Intro() {
        return CC_Intro;
    }
    public int getCC_Outro() {
        return CC_Outro;
    }
    public int getCC_Drum_fill() {
        return CC_Drum_fill;
    }
    public int getCC_Accent_hit() {
        return CC_Accent_hit;
    }
}

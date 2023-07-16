package com.garethevans.church.opensongtablet.aeros;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class Aeros {

    // This holds the settings for the Aeros loop studio MIDI companion
    // These are edited by the AerosFragment

    final private MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    final private String TAG = "Aeros";

    private int aerosChannel = 1;

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private byte[] commandSave, commandNewSong2x2, commandNewSong6x6, commandScrollUp,
            commandScrollDown, commandSelect, commandDelete, commandTop, commandBottom,
            commandNavSongs, commandNavLoop, commandNavHome, commandNavSettings, commandNavUpdate,
            commandUndoRedo, commandUndoRedo1, commandUndoRedo2, commandUndoRedo3, commandUndoRedo4,
            commandUndoRedo5, commandUndoRedo6, commandScreenshot, commandMuteAll, commandUnmuteAll,
            commandMuteToggle1, commandMuteToggle2, commandMuteToggle3, commandMuteToggle4,
            commandMuteToggle5, commandMuteToggle6, commandSolo1, commandSolo2, commandSolo3,
            commandSolo4, commandSolo5, commandSolo6, commandUnsolo, commandNewPart,
            commandRecordNew, commandRecordCommit, commandRecordSelected, commandRecordTrack1,
            commandRecordTrack2, commandRecordTrackNext, commandClear, commandStart, commandStop,
            commandStopImmediate, commandRPO, commandROP, commandReverse, commandReverse1,
            commandReverse2, commandReverse3, commandReverse4, commandReverse5, commandReverse6,
            commandTransitionExit, commandTransition1, commandTransition2, commandTransition3,
            commandTransition4, commandTransition5, commandTransition6, commandTransitionCancel,
            commandTransitionPrev, commandTransitionNext;

    // The commond CC commands.  In one easy readable place!
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final int   CC_Folder_MSB = 0,                // 0-127
                        CC_Save = 33,                     // 0=action
                        CC_New_Song = 34,                 // 0=2x2  1=6x6
                        CC_Navigate = 35,                 // 0=songs list, 1=loop studio mode, 2=home, 3=settings, 4=updates
                        CC_Song_List = 36,                // 0=scroll down, 1=scroll up, 2=select, 3=delete, 4=top, 5=bottom
                        CC_Undo_Redo = 37,                // 0=undo/redo top, 1-6=undo/redo tracks 1-6
                        CC_Mute = 38,                     // 0=mute all, 1-6=mute/unmute tracks, 11-16=mute tracks, 21-26=unmute tracks, 31-36=mute tracks EOL, 41-46=unmute tracks EOL, 51-56=mute tracks EOM, 61-66=unmute tracks EOM, 71-76=mute tracks immediately, 81-86 mute tracks immediately, 127=unmute all
                        CC_Solo = 39,                     // 0=stop immediately, 1-6=solo track, 127=unsolo
                        CC_New_Part = 40,                 // 0=action
                        CC_Record = 41,                   // 0=record new track, 20=commit recording, 100=RPO on selected track (6x6), 101-102=RPO on tracks 1/2 (2x2), 103=next track
                        CC_Clear = 42,                    // 0=clear song immediately (when stopped)
                        CC_Start_Stop = 43,               // 0=stop, 1=start, 2=cancel stop, 10=open previous song, 20=open next song, 127=stop immediately
                        CC_RPO_ROP = 45,                  // 0=set to RPO, 1=set to ROP
                        CC_Reverse = 46,                  // 0=reverse selected track, 1-6=reverse tracks
                        CC_Transition_Override = 102,     // 0-32=immediate transition to song part
                        CC_Transition = 113,              // 0=transition to selected part (after other command), 1-6=change to part + beatbuddy, 101-106=change to part aeros only, 125=cancel transition, 126=previous part, 127=next part
                        CC_Screenshot = 127;              // 127=save screenshot to SD card

    public Aeros(Context c) {
        this.mainActivityInterface = (MainActivityInterface) c;
        aerosChannel = mainActivityInterface.getPreferences().getMyPreferenceInt("aerosChannel",1);
        buildCommands();
    }

    // Commands received from gestures and sent via MIDI to connected Aeros
    // These are prebuilt and stored rather than building each time to avoid any delay when performing
    // These get rebuilt when any changes are made to the MIDI channel via the helper fragment
    public void buildCommands() {
        commandSave = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Save,0));
        commandNewSong2x2 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_New_Song,0));
        commandNewSong6x6 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_New_Song,1));
        commandScrollDown = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Song_List,0));
        commandScrollUp = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Song_List,1));
        commandSelect = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Song_List,2));
        commandDelete = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Song_List,3));
        commandTop = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Song_List,4));
        commandBottom = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Song_List,5));
        commandNavSongs = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Navigate, 0));
        commandNavLoop = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Navigate, 1));
        commandNavHome = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Navigate, 2));
        commandNavSettings = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Navigate, 3));
        commandNavUpdate = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Navigate, 4));
        commandUndoRedo = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Undo_Redo, 0));
        commandUndoRedo1 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Undo_Redo, 1));
        commandUndoRedo2 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Undo_Redo, 2));
        commandUndoRedo3 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Undo_Redo, 3));
        commandUndoRedo4 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Undo_Redo, 4));
        commandUndoRedo5 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Undo_Redo, 5));
        commandUndoRedo6 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Undo_Redo, 6));
        commandScreenshot = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Screenshot, 127));
        commandMuteAll = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Mute, 0));
        commandUnmuteAll = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Mute, 127));
        commandMuteToggle1 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Mute, 1));
        commandMuteToggle2 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Mute, 2));
        commandMuteToggle3 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Mute, 2));
        commandMuteToggle4 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Mute, 4));
        commandMuteToggle5 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Mute, 5));
        commandMuteToggle6 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Mute, 6));
        commandSolo1 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Solo, 1));
        commandSolo2 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Solo, 2));
        commandSolo3 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Solo, 3));
        commandSolo4 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Solo, 4));
        commandSolo5 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Solo, 5));
        commandSolo6 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Solo, 6));
        commandUnsolo = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Solo, 127));
        commandNewPart = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_New_Part, 0));
        commandRecordNew = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Record, 0));
        commandRecordCommit = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Record, 20));
        commandRecordSelected = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Record, 100));
        commandRecordTrack1 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Record, 101));
        commandRecordTrack2 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Record, 102));
        commandRecordTrackNext = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Record, 103));
        commandClear = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Clear, 0));
        commandStop = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Start_Stop, 0));
        commandStart = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Start_Stop, 1));
        commandStopImmediate = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_Start_Stop, 127));
        commandRPO = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_RPO_ROP, 0));
        commandROP = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",aerosChannel-1, CC_RPO_ROP, 1));
        commandReverse = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Reverse, 0));
        commandReverse1 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Reverse, 1));
        commandReverse2 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Reverse, 2));
        commandReverse3 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Reverse, 3));
        commandReverse4 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Reverse, 4));
        commandReverse5 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Reverse, 5));
        commandReverse6 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Reverse, 6));
        commandTransitionExit = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Transition, 0));
        commandTransition1 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Transition, 1));
        commandTransition2 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Transition, 2));
        commandTransition3 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Transition, 3));
        commandTransition4 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Transition, 4));
        commandTransition5 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Transition, 5));
        commandTransition6 = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Transition, 6));
        commandTransitionCancel = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Transition, 125));
        commandTransitionPrev = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Transition, 126));
        commandTransitionNext = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC", aerosChannel-1, CC_Transition, 127));
    }

    // The getters
    public int getAerosChannel() {
        return aerosChannel;
    }

    // The setters
    public void setAerosChannel(int aerosChannel) {
        this.aerosChannel = aerosChannel;
        mainActivityInterface.getPreferences().setMyPreferenceInt("aerosChannel",aerosChannel);
    }

}

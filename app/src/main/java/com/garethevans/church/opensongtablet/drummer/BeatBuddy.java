package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.os.Build;
import android.os.Handler;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class BeatBuddy {

    // This holds the settings for the BeatBuddy MIDI companion
    // These are edited by the BeatBuddyFragment

    final private MainActivityInterface mainActivityInterface;

    // Control options are:
    private int beatBuddyChannel = 1, beatBuddyVolume = 100, beatBuddyHeadphones = 100, beatBuddyDrumKit = 1;
    private byte[] commandStart, commandStop, commandAccent, commandFill, commandTransition1,
            commandTransition2, commandTransition3, commandTransitionNext, commandTransitionExit,
            commandVolume, commandHeadphones, commandTempo, commandHalfTime, commandHalfTimeExit,
            commandDoubleTime, commandDoubleTimeExit, commandPauseToggle, commandDrumKit;

    private boolean volumesSent = false, headphonesSent = false, beatBuddyIncludeSong;

    public BeatBuddy(Context c) {
        this.mainActivityInterface = (MainActivityInterface) c;
        setPrefs();
        buildCommands();
    }

    public void setPrefs() {
        beatBuddyChannel = mainActivityInterface.getPreferences().getMyPreferenceInt("beatBuddyChannel",1);
        beatBuddyVolume = mainActivityInterface.getPreferences().getMyPreferenceInt("beatBuddyVolume",100);
        beatBuddyHeadphones = mainActivityInterface.getPreferences().getMyPreferenceInt("beatBuddyHeadphones",100);
        beatBuddyIncludeSong = mainActivityInterface.getPreferences().getMyPreferenceBoolean("beatBuddyIncludeSong",false);
    }

    public void saveTempoToSong(int bpm) {
        // This sets the song tempo to match and saves this as a BeatBuddy MIDI message with the song (if not already set)
    }

    public void getSongTempo() {
        // Get any BeatBuddy MIDI messages that are for the song tempo
        // Compare with the song tempo
        // If they are both set, but different, give the user an option
    }



    // Commands send via MIDI to connected BeatBuddy
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
        commandVolume = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,108,beatBuddyVolume));
        commandHeadphones = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,108,beatBuddyHeadphones));
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
        commandDrumKit = mainActivityInterface.getMidi().returnBytesFromHexText(
                mainActivityInterface.getMidi().buildMidiString("CC",beatBuddyChannel,116,beatBuddyDrumKit));
    }
    public void beatBuddyStart() {
        // Send a MIDI command to start the BeatBuddy and send the main and headphone volumes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!volumesSent || !headphonesSent) {
                volumesSent = true;
                headphonesSent = true;
                new Handler().post(this::beatBuddyVolume);
                new Handler().postDelayed(this::beatBuddyHeadphones, mainActivityInterface.getMidi().getMidiDelay());
                new Handler().postDelayed(() -> mainActivityInterface.getMidi().sendMidi(commandStart), mainActivityInterface.getMidi().getMidiDelay());
            } else {
                mainActivityInterface.getMidi().sendMidi(commandStart);
            }
        }
    }
    public void beatBuddyStop() {
        // Send a MIDI command to start the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandStop);
        }
    }
    public void beatBuddyAccent() {
        // Send a MIDI command to play an accent the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandAccent);
        }
    }
    public void beatBuddyFill() {
        // Send a MIDI command to play an accent the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandFill);
        }
    }
    public void beatBuddyTransition1() {
        // Send a MIDI command to play an accent the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandTransition1);
        }
    }
    public void beatBuddyTransition2() {
        // Send a MIDI command to play an accent the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandTransition2);
        }
    }
    public void beatBuddyTransition3() {
        // Send a MIDI command to play an accent the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandTransition3);
        }
    }
    public void beatBuddyTransitionNext() {
        // Send a MIDI command to play an accent the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandTransitionNext);
        }
    }
    public void beatBuddyTransitionExit() {
        // Send a MIDI command to play an accent the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandTransitionExit);
        }
    }
    public void beatBuddyVolume() {
        // Send a MIDI command to play an accent the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandVolume);
        }
        volumesSent = true;
    }
    public void beatBuddyHeadphones() {
        // Send a MIDI command to play an accent the BeatBuddy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainActivityInterface.getMidi().sendMidi(commandHeadphones);
        }
        headphonesSent = true;
    }


    // The getters and setters
    public int getBeatBuddyChannel() {
        return beatBuddyChannel;
    }
    public int getBeatBuddyVolume() {
        return beatBuddyVolume;
    }
    public int getBeatBuddyHeadphones() {
        return beatBuddyHeadphones;
    }
    public boolean getBeatBuddyIncludeSong() {
        return beatBuddyIncludeSong;
    }
    public void setBeatBuddyChannel(int beatBuddyChannel) {
        this.beatBuddyChannel = beatBuddyChannel;
        mainActivityInterface.getPreferences().setMyPreferenceInt("beatBuddyChannel",beatBuddyChannel);
    }
    public void setBeatBuddyVolume(int beatBuddyVolume) {
        this.beatBuddyVolume = beatBuddyVolume;
        mainActivityInterface.getPreferences().setMyPreferenceInt("beatBuddyVolume",beatBuddyVolume);
    }
    public void setBeatBuddyHeadphones(int beatBuddyHeadphones) {
        this.beatBuddyHeadphones = beatBuddyHeadphones;
        mainActivityInterface.getPreferences().setMyPreferenceInt("beatBuddyHeadphones",beatBuddyHeadphones);
    }
    public void setBeatBuddyIncludeSong(boolean beatBuddyIncludeSong) {
        this.beatBuddyIncludeSong = beatBuddyIncludeSong;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("beatBuddyIncludeSong",beatBuddyIncludeSong);
    }
    public void setBeatBuddyTempo(int tempo) {
        if (tempo<40) {
            commandTempo = null;
        } else {
            // TODO Code to build beatBuddy tempo using MSB and LSB
        }
    }
}

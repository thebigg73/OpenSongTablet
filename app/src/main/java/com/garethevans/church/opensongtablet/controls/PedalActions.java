package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class PedalActions {

    // Actions triggered here are sent to the PerformanceGestures to be acted upon there (one place!)

    /*
    AirTurn pedals send repeated key down information a time apart (keyrepeattime)
    For this mode, we only act on keyUp for short presses
    To differentiate between long and short presses, we need to do the following:
        onKeyDown > Set a boolean that the key is down
                    Get a note of the system time for this pedal down
                    Because this can be sent multiple times, do nothing if key is already down
                    Check the time to see if longPress time has elapsed
                    If it has, record as a longPress action and do the action

        onKeyUp   > if we weren't registered as a longPress action, then send the keyCode up
                    if we were registered as a longPress, reset the keyDown so we can listen again
     */

    private final String TAG = "PedalActions";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private ArrayList<String> actions, actionCodes;

    // 8 buttons, but ignoring item 0
    private final int[] pedalCode = new int[9];
    private final String[] pedalMidi = new String[9];
    private final String[] pedalShortPressAction = new String[9];
    private final String[] pedalLongPressAction = new String[9];
    public Boolean[] pedalDown = new Boolean[9];
    public Long[] pedalDownTime = new Long[9];
    public Boolean[] pedalWasLongPressed = new Boolean[9];

    public final int[] defPedalCodes = new int[]{-1,21,22,19,20,92,93,-1,-1};
    public final String[] defPedalMidis = new String[]{"","C3","D3","E3","F3","G3","A3","B3","C4"};
    public final String[] defShortActions = new String[]{"","prev","next","up","down","","","",""};
    public final String[] defLongActions  = new String[] {"", "songmenu", "set", "", "", "", "", "", ""};
    private int airTurnLongPressTime;
    private boolean airTurnMode, pedalScrollBeforeMove, pedalShowWarningBeforeMove, midiAsPedal;

    public PedalActions(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        setUpPedalActions();
    }

    public void setUpPedalActions() {
        setActions();
        setPrefs();
    }

    public ArrayList<String> getActions() {
        return actions;
    }
    public ArrayList<String> getActionCodes() {
        return actionCodes;
    }

    private void setActions() {
        actions = new ArrayList<>();
        actionCodes = new ArrayList<>();
        String startstop = " (" + c.getString(R.string.start) + " / " + c.getString(R.string.stop) + ")";
        String showhide = " (" + c.getString(R.string.show) + " / " + c.getString(R.string.hide) + ")";
        String autoscroll = c.getString(R.string.autoscroll) + startstop;
        String pad = c.getString(R.string.pad) + startstop;
        String metronome = c.getString(R.string.metronome) + startstop;
        addString("","");
        addString("prev",c.getString(R.string.previous));
        addString("next",c.getString(R.string.next));
        addString("up",c.getString(R.string.scroll_up));
        addString("down",c.getString(R.string.scroll_down));
        addString("pad",pad);
        addString("autoscroll",autoscroll);
        addString("metronome",metronome);
        addString("pad_autoscroll",pad + " & " + c.getString(R.string.autoscroll));
        addString("pad_metronome",pad + " & " + c.getString(R.string.metronome));
        addString("autoscroll_metronome",autoscroll + " & " + c.getString(R.string.metronome));
        addString("pad_autoscroll_metronome",pad + " & " + c.getString(R.string.autoscroll) + " & " + c.getString(R.string.metronome));
        addString("editsong",c.getString(R.string.edit));
        addString("transpose",c.getString(R.string.transpose));
        addString("showchords",c.getString(R.string.show_chords));
        addString("showcapo",c.getString(R.string.show_capo));
        addString("showlyrics",c.getString(R.string.show_lyrics));
        addString("randomsong",c.getString(R.string.random_song));
        addString("abcnotation",c.getString(R.string.music_score));
        addString("highlight",c.getString(R.string.highlight));
        addString("sticky",c.getString(R.string.song_notes));
        addString("speedup",c.getString(R.string.inc_autoscroll_speed));
        addString("slowdown",c.getString(R.string.dec_autoscroll_speed));
        addString("pause",c.getString(R.string.autoscroll_pause));
        addString("songmenu",c.getString(R.string.songs) + showhide);
        addString("set",c.getString(R.string.set_current) + showhide);
        addString("refreshsong",c.getString(R.string.refresh_song));
        addString("addsongtoset",c.getString(R.string.add_song_to_set));
    }
    private void addString(String id, String val) {
        actionCodes.add(id);
        actions.add(val);
    }
    private void setPrefs() {
        for (int w=1; w<=8; w++) {
            pedalCode[w] = mainActivityInterface.getPreferences().getMyPreferenceInt("pedal"+w+"Code", defPedalCodes[w]);
            pedalMidi[w] = mainActivityInterface.getPreferences().getMyPreferenceString("pedal"+w+"Midi",defPedalMidis[w]);
            pedalShortPressAction[w] = mainActivityInterface.getPreferences().getMyPreferenceString("pedal"+w+"ShortPressAction",defShortActions[w]);
            pedalLongPressAction[w] = mainActivityInterface.getPreferences().getMyPreferenceString("pedal"+w+"LongPressAction",defLongActions[w]);
        }
        airTurnMode = mainActivityInterface.getPreferences().getMyPreferenceBoolean("airTurnMode", false);
        airTurnLongPressTime = mainActivityInterface.getPreferences().getMyPreferenceInt("airTurnLongPressTime", 1000);
        pedalScrollBeforeMove = mainActivityInterface.getPreferences().getMyPreferenceBoolean("pedalScrollBeforeMove",true);
        pedalShowWarningBeforeMove = mainActivityInterface.getPreferences().getMyPreferenceBoolean("pedalShowWarningBeforeMove",false);
        midiAsPedal = mainActivityInterface.getPreferences().getMyPreferenceBoolean("midiAsPedal", false);
    }

    public void commonEventDown(int keyCode, String keyMidi) {
        // Using AirTurnMode for keyboard pedal, deal with this separately, otherwise, do nothing
        if (airTurnMode && (keyMidi==null || keyMidi.isEmpty())) {
            doAirTurnDetectionDown(keyCode);
        }
    }

    public void commonEventUp(int keyCode, String keyMidi) {
        // Using AirTurnMode for keyboard pedal, deal with this separately, otherwise, send the action
        if (airTurnMode && (keyMidi==null || keyMidi.isEmpty())) {
            doAirTurnDetectionUp(keyCode);
        } else {
            whichEventTriggered(true, keyCode, keyMidi);
        }
    }

    public void commonEventLong(int keyCode, String keyMidi) {
        whichEventTriggered(false,keyCode,keyMidi);
    }

    public void whichEventTriggered(boolean shortpress, int keyCode, String keyMidi) {
        int pedal = getButtonNumber(keyCode, keyMidi);
        String desiredAction = getDesiredAction(shortpress,pedal);
        if (desiredAction==null) {
            desiredAction = "";
        }

        switch (desiredAction) {
            case "prev":
                // If the menu isn't open
                if (!mainActivityInterface.getMenuOpen()) {
                    mainActivityInterface.getPerformanceGestures().prevSong();
                } else {
                    // If it is, scroll
                    mainActivityInterface.scrollOpenMenu(false);
                }
                break;

            case "next":
                // If the menu isn't open
                if (!mainActivityInterface.getMenuOpen()) {
                    mainActivityInterface.getPerformanceGestures().nextSong();
                } else {
                    // If it is, scroll
                    mainActivityInterface.scrollOpenMenu(true);
                }
                break;

            case "down":
                // If the menu isn't open
                if (!mainActivityInterface.getMenuOpen()) {
                    mainActivityInterface.getPerformanceGestures().scroll(true);
                } else {
                    // If it is, scroll
                    mainActivityInterface.scrollOpenMenu(true);
                }
                break;

            case "up":
                // If the menu isn't open
                if (!mainActivityInterface.getMenuOpen()) {
                    mainActivityInterface.getPerformanceGestures().scroll(false);
                } else {
                    // If it is, scroll
                    mainActivityInterface.scrollOpenMenu(false);
                }
                break;

            case "pad":
                mainActivityInterface.getPerformanceGestures().togglePad();
                break;

            case "autoscroll":
                mainActivityInterface.getPerformanceGestures().toggleAutoscroll();
                break;

            case "metronome":
                mainActivityInterface.getPerformanceGestures().toggleMetronome();
                break;

            case "pad_autoscroll":
                mainActivityInterface.getPerformanceGestures().togglePad();
                mainActivityInterface.getPerformanceGestures().toggleAutoscroll();
                break;

            case "pad_metronome":
                mainActivityInterface.getPerformanceGestures().togglePad();
                mainActivityInterface.getPerformanceGestures().toggleMetronome();
                break;

            case "autoscroll_metronome":
                mainActivityInterface.getPerformanceGestures().toggleMetronome();
                mainActivityInterface.getPerformanceGestures().toggleAutoscroll();
                break;

            case "pad_autoscroll_metronome":
                mainActivityInterface.getPerformanceGestures().togglePad();
                mainActivityInterface.getPerformanceGestures().toggleMetronome();
                mainActivityInterface.getPerformanceGestures().toggleAutoscroll();
                break;

            case "editsong":
                mainActivityInterface.getPerformanceGestures().editSong();
                break;

            case "randomsong":
                mainActivityInterface.getPerformanceGestures().randomSong();
                break;

            case "transpose":
                mainActivityInterface.getPerformanceGestures().transpose();
                break;

            case "showchords":
                mainActivityInterface.getPerformanceGestures().showChords();
                break;

            case "showcapo":
                mainActivityInterface.getPerformanceGestures().showCapo();
                break;

            case "showlyrics":
                mainActivityInterface.getPerformanceGestures().showLyrics();
                break;

            case "abcnotation":
                mainActivityInterface.getPerformanceGestures().showABCNotation();
                break;

            case "highlight":
                mainActivityInterface.getPerformanceGestures().showHighlight();
                break;

            case "sticky":
                mainActivityInterface.getPerformanceGestures().showSticky();
                break;

            case "speedup":
                mainActivityInterface.getPerformanceGestures().speedUpAutoscroll();
                break;

            case "slowdown":
                mainActivityInterface.getPerformanceGestures().slowDownAutoscroll();
                break;

            case "pause":
                mainActivityInterface.getPerformanceGestures().pauseAutoscroll();
                break;

            case "songmenu":
                mainActivityInterface.getPerformanceGestures().songMenu();
                break;

            case "set":
                mainActivityInterface.getPerformanceGestures().setMenu();
                break;

            case "refreshsong":
                mainActivityInterface.getPerformanceGestures().loadSong();
                break;

            case "addsongtoset":
                mainActivityInterface.getPerformanceGestures().addToSet();
                break;
        }
    }

    public int getButtonNumber(int keyCode, String keyMidi) {
        int pedal = 0;
        if (keyMidi != null) {
            for (int w = 1; w <= 8; w++) {
                if (pedalMidi[w].equals(keyMidi)) {
                    pedal = w;
                }
            }
        } else {
            for (int w = 1; w <= 8; w++) {
                if (pedalCode[w]==keyCode) {
                    pedal = w;
                }
            }
        }
        return pedal;
    }
    private String getDesiredAction(boolean shortpress, int pedal) {
        if (shortpress) {
            return pedalShortPressAction[pedal];
        } else {
            return pedalLongPressAction[pedal];
        }
    }


    // Getters and setters
    public boolean getPedalScrollBeforeMove() {
        return pedalScrollBeforeMove;
    }
    public boolean getPedalShowWarningBeforeMove() {
        return pedalShowWarningBeforeMove;
    }
    public boolean getAirTurnMode() {
        return airTurnMode;
    }
    public int getAirTurnLongPressTime() {
        return airTurnLongPressTime;
    }
    public int getPedalCode(int which) {
        return pedalCode[which];
    }
    public String getMidiCode(int which) {
        return pedalMidi[which];
    }
    public boolean getMidiAsPedal() {
        return midiAsPedal;
    }
    public String getPedalShortPressAction(int which) {
        return pedalShortPressAction[which];
    }
    public String getPedalLongPressAction(int which) {
        return pedalLongPressAction[which];
    }

    public void setPreferences(String which, boolean bool) {
        switch (which) {
            case "pedalScrollBeforeMove":
                this.pedalScrollBeforeMove = bool;
                break;
            case "pedalShowWarningBeforeMove":
                this.pedalShowWarningBeforeMove = bool;
                break;
            case "airTurnMode":
                this.airTurnMode = bool;
                break;
            case "midiAsPedal":
                this.midiAsPedal = bool;
                break;
        }
        // Save the preference
        mainActivityInterface.getPreferences().setMyPreferenceBoolean(which, bool);
    }
    public void setPreferences(String which, int val) {
        airTurnLongPressTime = val;
        // Save the preference
        mainActivityInterface.getPreferences().setMyPreferenceInt(which, val);
    }
    public void setPedalCode(int which, int newCode) {
        pedalCode[which] = newCode;
        mainActivityInterface.getPreferences().setMyPreferenceInt("pedal"+which+"Code",newCode);
    }
    public void setMidiCode(int which, String newCode) {
        pedalMidi[which] = newCode;
        mainActivityInterface.getPreferences().setMyPreferenceString("pedal"+which+"Midi",newCode);
    }
    public void setPedalPreference(int which, boolean shortPress, String action) {
        String pref = "pedal"+which;
        if (shortPress) {
            pedalShortPressAction[which] = action;
            pref = pref + "ShortPressAction";
        } else {
            pedalLongPressAction[which] = action;
            pref = pref + "LongPressAction";
        }
        // Save the preference
        mainActivityInterface.getPreferences().setMyPreferenceString(pref, action);
    }
    public void setMidiAsPedal(boolean midiAsPedal) {
        this.midiAsPedal = midiAsPedal;
    }

    private void doAirTurnDetectionDown(int keyCode) {
        // Figure out which pedal is being pressed
        int keyPedalNum = getPedalFromKeyCode(keyCode);
        boolean isKeyPedal = keyPedalNum > 0;

        // Only proceed if we know which pedal
        if (isKeyPedal) {
            // Check the status of this pedal
            if (pedalDown[keyPedalNum] == null || !pedalDown[keyPedalNum]) {
                // Set this pedal as being pressed for the first time
                pedalDown[keyPedalNum] = true;
                // Set the system time
                pedalDownTime[keyPedalNum] = System.currentTimeMillis();
            }
            boolean pedalIsDown = pedalDown[keyPedalNum] != null && pedalDown[keyPedalNum];
            boolean longTimeHasPassed = pedalDownTime[keyPedalNum]!=null &&
                    pedalDownTime[keyPedalNum]!=0 &&
                    System.currentTimeMillis() > (pedalDownTime[keyPedalNum]+1000);
            boolean notAlreadyLongPressed = pedalWasLongPressed[keyPedalNum] == null || !pedalWasLongPressed[keyPedalNum];
            // Check if the pedal is down and longPress time has elapsed and isn't already registered
            if (pedalIsDown && longTimeHasPassed && notAlreadyLongPressed) {
                // Register this as a new long press.  This stops the ACTION_UP being run too
                pedalWasLongPressed[keyPedalNum] = true;
                // Do the long press action
                commonEventLong(keyCode, null);
            }
        }
    }

    private void doAirTurnDetectionUp(int keyCode) {
        // Figure out which pedal is being pressed
        int keyPedalNum = getPedalFromKeyCode(keyCode);
        boolean isKeyPedal = keyPedalNum > 0;

        // Only proceed if we know which pedal
        if (isKeyPedal) {
            if (pedalWasLongPressed[keyPedalNum] != null && pedalWasLongPressed[keyPedalNum]) {
                // This pedal was registered as a long press, do nothing other than reset it
                Log.d(TAG, "Long press happened already, do nothing");
                } else {
                // Not a long press, so action the shortPress
                whichEventTriggered(true, keyCode, null);
            }
            pedalDown[keyPedalNum] = false;
            pedalDownTime[keyPedalNum] = 0L; // This means not valid time;
            pedalWasLongPressed[keyPedalNum] = false;
        }
    }

    private int getPedalFromKeyCode(int keyCode) {
        // Go through the pedal codes and return the matching event
        int pedal = 0;
        for (int ped=1; ped<pedalCode.length; ped++) {
            if (pedalCode[ped] == keyCode) {
                pedal = ped;
                break;
            }
        }
        return pedal;
    }
}

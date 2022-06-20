package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PedalActions {

    // Actions triggered here are sent to the PerformanceGestures to be acted upon there (one place!)

    // TODO Airturn
    private final String TAG = "PedalActions";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private ArrayList<String> actions, actionCodes;
    private final int[] pedalCode = new int[9]; // 8 buttons, but ignore item 0
    private final String[] pedalMidi = new String[9];
    private final String[] pedalShortPressAction = new String[9];
    private final String[] pedalLongPressAction = new String[9];
    public final int[] defPedalCodes = new int[]{-1,21,22,19,20,92,93,-1,-1};
    public final String[] defPedalMidis = new String[]{"","C3","D3","E3","F3","G3","A3","B3","C4"};
    public final String[] defShortActions = new String[]{"","prev","next","up","down","","","",""};
    public final String[] defLongActions  = new String[] {"", "songmenu", "set", "", "", "", "", "", ""};
    private boolean longpress;
    private int repeatsRecorded;
    private int keyRepeatCount;
    private int keyRepeatTime;
    private boolean airTurnMode, airTurnPaused, pedalScrollBeforeMove, pedalShowWarningBeforeMove, midiAsPedal;
    private final Runnable releaseAirTurn = new Runnable() {
        @Override
        public void run() {
            airTurnPaused = false;
        }
    };

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
        keyRepeatCount = mainActivityInterface.getPreferences().getMyPreferenceInt("keyRepeatCount",20);
        pedalScrollBeforeMove = mainActivityInterface.getPreferences().getMyPreferenceBoolean("pedalScrollBeforeMove",true);
        pedalShowWarningBeforeMove = mainActivityInterface.getPreferences().getMyPreferenceBoolean("pedalShowWarningBeforeMove",false);
        keyRepeatTime = mainActivityInterface.getPreferences().getMyPreferenceInt("keyRepeatTime", 400);
        midiAsPedal = mainActivityInterface.getPreferences().getMyPreferenceBoolean("midiAsPedal", false);
    }

    public void commonEventDown(int keyCode, String keyMidi) {
        // If using AirTurnMode, grab this and start counting for long press mode
        // Otherwise, we ignore it
        longpress = false;
        if (airTurnMode && !airTurnPaused && keyRepeatCount<repeatsRecorded) {
            repeatsRecorded++;
            if (repeatsRecorded > keyRepeatCount) {
                // This should be set to a long press action
                airTurnPaused = true;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        airTurnPaused = false;
                    }
                },1500);
                commonEventLong(keyCode,keyMidi);
            }
        } else {
            repeatsRecorded = 0;
            airTurnPaused = false;
        }
    }

    public void commonEventUp(int keyCode, String keyMidi) {
        Log.d(TAG,"commonEventUp() longress"+longpress);

        if (!longpress) {
            whichEventTriggered(true,keyCode,keyMidi);
        }
    }

    public void commonEventLong(int keyCode, String keyMidi) {
        longpress = false;
        whichEventTriggered(false,keyCode,keyMidi);
    }

    public void whichEventTriggered(boolean shortpress, int keyCode, String keyMidi) {
        int pedal = getButtonNumber(keyCode, keyMidi);
        String desiredAction = getDesiredAction(shortpress,pedal);
        if (desiredAction==null) {
            desiredAction = "";
        }

        Log.d(TAG,"pedal="+pedal+"  desiredAction="+desiredAction);
        switch (desiredAction) {
            case "prev":
                mainActivityInterface.getPerformanceGestures().prevSong();
                break;

            case "next":
                mainActivityInterface.getPerformanceGestures().nextSong();
                break;

            case "down":
                mainActivityInterface.getPerformanceGestures().scroll(true);
                break;

            case "up":
                mainActivityInterface.getPerformanceGestures().scroll(false);
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
    public int getKeyRepeatCount() {
        return keyRepeatCount;
    }
    public int getKeyRepeatTime() {
        return keyRepeatTime;
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
        switch (which) {
            case "keyRepeatCount":
                this.keyRepeatCount = val;
                break;
            case "keyRepeatTime":
                this.keyRepeatTime = val;
                break;
        }
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
}

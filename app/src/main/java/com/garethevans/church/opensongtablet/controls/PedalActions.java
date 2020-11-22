package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PedalActions {

    ArrayList<String> actions, actionCodes;
    Context c;
    Preferences preferences;
    MainActivityInterface mainActivityInterface;
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
    private boolean airTurnMode, airTurnPaused, pedalScrollBeforeMove, pedalShowWarningBeforeMove;
    private Runnable releaseAirTurn = new Runnable() {
        @Override
        public void run() {
            airTurnPaused = false;
        }
    };
    public PedalActions(Context c, Preferences preferences) {
        this.c = c;
        this.preferences = preferences;
        setActions();
        setPrefs();
    }
    public void setInterface(MainActivityInterface mainActivityInterface) {
        if (mainActivityInterface!=null) {
            this.mainActivityInterface = mainActivityInterface;
        }
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
        addString("","");
        addString("prev",c.getString(R.string.pageturn_previous));
        addString("next",c.getString(R.string.pageturn_next));
        addString("up",c.getString(R.string.pageturn_up));
        addString("down",c.getString(R.string.pageturn_down));
        addString("pad",c.getString(R.string.padPedalText));
        addString("autoscroll",c.getString(R.string.autoscrollPedalText));
        addString("metronome",c.getString(R.string.metronomePedalText));
        addString("pad_autoscroll",c.getString(R.string.padPedalText) + " & " + c.getString(R.string.autoscrollPedalText));
        addString("pad_metronome",c.getString(R.string.padPedalText) + " & " + c.getString(R.string.metronomePedalText));
        addString("autoscroll_metronome",c.getString(R.string.autoscrollPedalText) + " & " + c.getString(R.string.metronomePedalText));
        addString("pad_autoscroll_metronome",c.getString(R.string.padPedalText) + " & " + c.getString(R.string.autoscrollPedalText) + " & " + c.getString(R.string.metronomePedalText));
        addString("editsong",c.getString(R.string.edit));
        addString("transpose",c.getString(R.string.transpose));
        addString("showchords",c.getString(R.string.showchords));
        addString("showcapo",c.getString(R.string.showcapo));
        addString("showlyrics",c.getString(R.string.showlyrics));
        addString("randomsong",c.getString(R.string.random_song));
        addString("abcnotation",c.getString(R.string.music_score));
        addString("highlight",c.getString(R.string.highlight));
        addString("sticky",c.getString(R.string.stickynotes));
        addString("speedup",c.getString(R.string.inc_autoscroll_speed));
        addString("slowdown",c.getString(R.string.dec_autoscroll_speed));
        addString("pause",c.getString(R.string.toggle_autoscroll_pause));
        addString("songmenu",c.getString(R.string.gesture1));
        addString("set",c.getString(R.string.currentset));
        addString("refreshsong",c.getString(R.string.gesture4));
        addString("addsongtoset",c.getString(R.string.add_song_to_set));
    }
    private void addString(String id, String val) {
        actionCodes.add(id);
        actions.add(val);
    }
    private void setPrefs() {
        for (int w=1; w<=8; w++) {
            pedalCode[w] = preferences.getMyPreferenceInt(c, "pedal"+w+"Code", defPedalCodes[w]);
            pedalMidi[w] = preferences.getMyPreferenceString(c,"pedal"+w+"Midi",defPedalMidis[w]);
            pedalShortPressAction[w] = preferences.getMyPreferenceString(c,"pedal"+w+"ShortPressAction",defShortActions[w]);
            pedalLongPressAction[w] = preferences.getMyPreferenceString(c,"pedal"+w+"LongPressAction",defLongActions[w]);
        }
        airTurnMode = preferences.getMyPreferenceBoolean(c,"airTurnMode", false);
        keyRepeatCount = preferences.getMyPreferenceInt(c,"keyRepeatCount",20);
        pedalScrollBeforeMove = preferences.getMyPreferenceBoolean(c,"pedalScrollBeforeMove",true);
        pedalShowWarningBeforeMove = preferences.getMyPreferenceBoolean(c,"pedalShowWarningBeforeMove",false);
    }

    public void commonEventDown(int keyCode, String keyMidi) {
        // If using AirTurnMode, grab this and start counting for long press mode
        if (airTurnMode && !airTurnPaused && keyRepeatCount<repeatsRecorded) {
            repeatsRecorded++;
            if (repeatsRecorded==keyRepeatCount) {
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
        if (!longpress) {
            whichEventTriggered(true,keyCode,keyMidi);
        }
    }

    public void commonEventLong(int keyCode, String keyMidi) {
        longpress = true;
        whichEventTriggered(false,keyCode,keyMidi);
    }

    public void whichEventTriggered(boolean shortpress, int keyCode, String keyMidi) {
        int pedal = getButtonNumber(keyCode, keyMidi);
        String desiredAction = getDesiredAction(shortpress,pedal);
        if (desiredAction==null) {
            desiredAction = "";
        }
        Log.d("PedalActions","pedal="+pedal+"  desiredAction="+desiredAction);
        switch (desiredAction) {
            case "prev":
                // TODO prev
                break;

            case "next":
                // TODO next
                break;

            case "down":
                // TODO down
                break;

            case "up":
                // TODO up
                break;

            case "pad":
                // TODO pad
                break;

            case "autoscroll":
                // TODO autoscroll
                break;

            case "metronome":
                // TODO metronome
                break;

            case "pad_autoscroll":
                // TODO pad_autoscroll
                break;

            case "pad_metronome":
                // TODO pad_metronome
                break;

            case "autoscroll_metronome":
                // TODO autoscroll_metronome
                break;

            case "pad_autoscroll_metronome":
                // TODO pad_autoscroll_metronome
                break;

            case "editsong":
                // TODO editsong
                break;

            case "randomsong":
                // TODO randomsong
                break;

            case "transpose":
                // TODO transpose
                break;

            case "showchords":
                // TODO showchords
                break;

            case "showcapo":
                // TODO showcapo
                break;

            case "showlyrics":
                // TODO showlyrics
                break;

            case "abcnotation":
                // TODO abcnotation
                break;

            case "highlight":
                // TODO highlight
                break;

            case "sticky":
                // TODO sticky
                break;

            case "speedup":
                // TODO speedup
                break;

            case "slowdown":
                // TODO slowdown
                break;

            case "pause":
                // TODO pause
                break;

            case "songmenu":
                // TODO songmenu
                break;

            case "set":
                // TODO set
                break;

            case "refreshsong":
                // TODO refreshsong
                break;

            case "addsongtoset":
                // TODO addsongtoset
                break;
        }
    }

    private int getButtonNumber(int keyCode, String keyMidi) {
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
}

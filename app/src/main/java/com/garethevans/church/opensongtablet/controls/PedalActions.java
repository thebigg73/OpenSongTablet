package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.chords.TransposeBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songsandsetsmenu.RandomSongBottomSheet;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PedalActions {

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
    private boolean airTurnMode, airTurnPaused, pedalScrollBeforeMove, pedalShowWarningBeforeMove;
    private Runnable releaseAirTurn = new Runnable() {
        @Override
        public void run() {
            airTurnPaused = false;
        }
    };

    public PedalActions(MainActivityInterface mainActivityInterface) {
        this.mainActivityInterface = mainActivityInterface;
    }

    public void setUpPedalActions(Context c, MainActivityInterface mainActivityInterface) {
        setActions(c);
        setPrefs(c,mainActivityInterface);
    }

    public ArrayList<String> getActions() {
        return actions;
    }
    public ArrayList<String> getActionCodes() {
        return actionCodes;
    }

    private void setActions(Context c) {
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
    private void setPrefs(Context c, MainActivityInterface mainActivityInterface) {
        for (int w=1; w<=8; w++) {
            pedalCode[w] = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "pedal"+w+"Code", defPedalCodes[w]);
            pedalMidi[w] = mainActivityInterface.getPreferences().getMyPreferenceString(c,"pedal"+w+"Midi",defPedalMidis[w]);
            pedalShortPressAction[w] = mainActivityInterface.getPreferences().getMyPreferenceString(c,"pedal"+w+"ShortPressAction",defShortActions[w]);
            pedalLongPressAction[w] = mainActivityInterface.getPreferences().getMyPreferenceString(c,"pedal"+w+"LongPressAction",defLongActions[w]);
        }
        airTurnMode = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"airTurnMode", false);
        keyRepeatCount = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"keyRepeatCount",20);
        pedalScrollBeforeMove = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"pedalScrollBeforeMove",true);
        pedalShowWarningBeforeMove = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"pedalShowWarningBeforeMove",false);
    }

    public void commonEventDown(int keyCode, String keyMidi) {
        // If using AirTurnMode, grab this and start counting for long press mode
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
                mainActivityInterface.getDisplayPrevNext().moveToPrev();
                break;

            case "next":
                mainActivityInterface.getDisplayPrevNext().moveToNext();
                break;

            case "down":
                // TODO down
                break;

            case "up":
                // TODO up
                break;

            case "pad":
                mainActivityInterface.playPad();
                break;

            case "autoscroll":
                // TODO autoscroll
                break;

            case "metronome":
                mainActivityInterface.toggleMetronome();
                break;

            case "pad_autoscroll":
                mainActivityInterface.playPad();
                // TODO pad_autoscroll
                break;

            case "pad_metronome":
                mainActivityInterface.playPad();
                mainActivityInterface.toggleMetronome();
                // TODO pad_metronome
                break;

            case "autoscroll_metronome":
                mainActivityInterface.toggleMetronome();
                // TODO autoscroll_metronome
                break;

            case "pad_autoscroll_metronome":
                mainActivityInterface.playPad();
                mainActivityInterface.toggleMetronome();
                // TODO pad_autoscroll_metronome
                break;

            case "editsong":
                mainActivityInterface.navigateToFragment("opensongapp;//settings/song/edit",0);
                break;

            case "randomsong":
                // TODO decide if we are in song or set menu.  For now assume song
                RandomSongBottomSheet randomSongBottomSheet = new RandomSongBottomSheet("song");
                randomSongBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"randomSongBottomSheet");
                break;

            case "transpose":
                TransposeBottomSheet transposeBottomSheet = new TransposeBottomSheet(false);
                transposeBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"transposeBottomSheet");
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

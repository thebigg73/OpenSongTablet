package com.garethevans.church.opensongtablet.controls;

import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

// This holds the references for gestures
// The actions are called in GestureListener

public class Gestures {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Gestures";

    // This deals with on screen gestures (double tap and long press)
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private ArrayList<String> gestures;
    private ArrayList<String> gestureDescriptions;
    private String doubleTap;
    private String longPress;
    private boolean swipeEnabled;
    private float scrollDistance;

    // Initialise the class
    public Gestures(Context c) {
        // On start, set up the arrays
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        setGestures();
        getPreferences();
    }
    private void setGestures() {
        // Set the gesture names for storing in preferences
        // (used to be int for position, but was a pain in adding/removing options)

        gestures = new ArrayList<>();
        gestureDescriptions = new ArrayList<>();

        // Set up the dropdown options
        String startstop = " (" + c.getString(R.string.start) + " / " + c.getString(R.string.stop) + ")";
        String showhide = " (" + c.getString(R.string.show) + " / " + c.getString(R.string.hide) + ")";
        String onoff = " (" + c.getString(R.string.on)+" / "+c.getString(R.string.off) + ")";
        String settings = " " + c.getString(R.string.settings);
        String autoscroll = c.getString(R.string.autoscroll);
        String metronome = c.getString(R.string.metronome);
        String pad = c.getString(R.string.pad);

        // Set actions
        addString("set",c.getString(R.string.set_current) + showhide);
        addString("inlineset",c.getString(R.string.set_inline) + showhide);
        addString("inlinesetsettings",c.getString(R.string.set_inline)+ settings);
        addString("addtoset",c.getString(R.string.add_song_to_set));
        addString("addtosetvariation",c.getString(R.string.variation_make));

        addString("","");

        // Song actions
        addString("pad",pad+startstop);
        addString("padsettings",c.getString(R.string.pad)+settings);
        addString("metronome",metronome+startstop);
        addString("metronomesettings",c.getString(R.string.metronome)+settings);
        addString("autoscroll",autoscroll+startstop);
        addString("autoscrollsettings",c.getString(R.string.autoscroll)+settings);
        addString("inc_autoscroll_speed",c.getString(R.string.inc_autoscroll_speed));
        addString("dec_autoscroll_speed",c.getString(R.string.dec_autoscroll_speed));
        addString("toggle_autoscroll_pause",c.getString(R.string.autoscroll_pause));
        addString("pad_autoscroll",pad + " & " + autoscroll+startstop);
        addString("pad_metronome",pad + " & " + metronome+startstop);
        addString("autoscroll_metronome",autoscroll + " & " + metronome+startstop);
        addString("pad_autoscroll_metronome",pad + " & " + autoscroll + " & " + metronome+startstop);
        addString("editsong",c.getString(R.string.edit));
        addString("share_song",c.getString(R.string.export)+" "+c.getString(R.string.song));
        addString("importoptions",c.getString(R.string.import_main));
        addString("importonline",c.getString(R.string.import_basic)+" "+c.getString(R.string.online_services));
        addString("refreshsong",c.getString(R.string.refresh_song));
        addString("","");

        // Song navigation
        addString("songmenu",c.getString(R.string.show_songs) + showhide);
        addString("scrolldown",c.getString(R.string.scroll_down));
        addString("scrollup",c.getString(R.string.scroll_up));
        addString("next",c.getString(R.string.next));
        addString("previous",c.getString(R.string.previous));
        addString("randomsong",c.getString(R.string.random_song));

        addString("","");

        // Chords
        addString("transpose",c.getString(R.string.transpose));
        addString("transposesettings",c.getString(R.string.chord_settings));
        addString("chordfingerings",c.getString(R.string.chord_fingering)+showhide);
        addString("customchords",c.getString(R.string.custom_chords));

        addString("","");

        // Song information
        addString("link",c.getString(R.string.link));
        addString("stickynotes",c.getString(R.string.song_notes)+showhide);
        addString("stickynotessettings",c.getString(R.string.song_notes_edit));
        addString("highlight",c.getString(R.string.highlight)+showhide);
        addString("highlightedit",c.getString(R.string.highlight_info));
        addString("abc",c.getString(R.string.music_score));
        addString("abcedit",c.getString(R.string.music_score_info));

        addString("","");

        // Display
        addString("profiles",c.getString(R.string.profile));
        addString("showchords",c.getString(R.string.show_chords));
        addString("showcapo",c.getString(R.string.show_capo));
        addString("showlyrics",c.getString(R.string.show_lyrics));
        addString("theme",c.getString(R.string.theme_choose));
        addString("togglescale",c.getString(R.string.scale_auto));
        addString("autoscalesettings",c.getString(R.string.scaling_info));
        addString("pdfpage",c.getString(R.string.select_page));
        addString("invertpdf",c.getString(R.string.invert_PDF));
        addString("fonts",c.getString(R.string.font_choose));

        addString("","");

        // Controls
        addString("nearby",c.getString(R.string.connections_discover));
        addString("nearbysettings",c.getString(R.string.connections_connect)+settings);
        addString("gestures",c.getString(R.string.custom_gestures));
        addString("pedals",c.getString(R.string.pedal)+settings);
        addString("midi",c.getString(R.string.midi_send));
        addString("midisettings",c.getString(R.string.midi)+settings);
        addString("midisend",c.getString(R.string.midi_auto)+onoff);

        addString("","");

        // Utilities
        addString("soundlevel",c.getString(R.string.sound_level_meter));
        addString("tuner",c.getString(R.string.tuner));
        addString("bible",c.getString(R.string.bible_verse));

        addString("","");

        // Exit
        addString("exit",c.getString(R.string.exit) + " " + c.getString(R.string.app_name));
    }

    private void addString(String gesture,String description) {
        gestures.add(gesture);
        gestureDescriptions.add(description);
    }
    private void getPreferences() {
        doubleTap = mainActivityInterface.getPreferences().getMyPreferenceString("gestureDoubleTap","editsong");
        longPress = mainActivityInterface.getPreferences().getMyPreferenceString("gestureLongPress","");
        swipeEnabled = mainActivityInterface.getPreferences().getMyPreferenceBoolean("swipeForSongs",true);
        scrollDistance = mainActivityInterface.getPreferences().getMyPreferenceFloat("scrollDistance", 0.7f);
    }
    public void setScrollDistance(float scrollDistance) {
        this.scrollDistance = scrollDistance;
    }

    // The getters and setters called by other classes
    public ArrayList<String> getGestures() {
        return gestures;
    }
    public ArrayList<String> getGestureDescriptions() {
        return gestureDescriptions;
    }
    public String getGestureFromDescription(String description) {
        int pos = gestureDescriptions.indexOf(description);
        return gestures.get(pos);
    }
    public String getDescriptionFromGesture(String gesture) {
        int pos = gestures.indexOf(gesture);
        return gestureDescriptions.get(pos);
    }

    public String getDoubleTap() {
        return doubleTap;
    }
    public String getLongPress() {
        return longPress;
    }
    public boolean getSwipeEnabled() {
        return swipeEnabled;
    }
    public float getScrollDistance() {
        return scrollDistance;
    }
    public void setPreferences(String which, String val) {
        if (which.equals("gestureDoubleTap")) {
            this.doubleTap = val;
        } else {
            this.longPress = val;
        }
        // Save the preference
        mainActivityInterface.getPreferences().setMyPreferenceString(which, val);
    }
    public void setPreferences(String which, boolean bool) {
        if ("swipeForSongs".equals(which)) {
            this.swipeEnabled = bool;
        }
        // Save the preference
        mainActivityInterface.getPreferences().setMyPreferenceBoolean(which, bool);
    }

}

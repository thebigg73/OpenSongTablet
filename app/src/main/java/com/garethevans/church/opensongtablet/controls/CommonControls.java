package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;

import java.util.ArrayList;

public class CommonControls {

    // Holds the references for the common controls (page gestures, hot zones, pedals)
    // Page buttons are dealt with separately as they can't choose short/long press independently

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "CommonControls";
    private final ArrayList<String> gestures, gestureDescriptions;

    public CommonControls(Context c) {
        gestures = new ArrayList<>();
        gestureDescriptions = new ArrayList<>();
        setGestures(c);
    }

    private void setGestures(Context c) {
        // Set the gesture names for storing in preferences
        // (used to be int for position, but was a pain in adding/removing options)

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
        addString("exportset",c.getString(R.string.export_current_set));

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
        addString("prev",c.getString(R.string.previous));
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
        addString("showlogo",c.getString(R.string.show_logo) + " (" + c.getString(R.string.connected_display) + ")");

        addString("","");

        // Controls
        addString("nearby",c.getString(R.string.connections_discover));
        addString("nearbysettings",c.getString(R.string.connections_connect)+settings);
        addString("gestures",c.getString(R.string.custom_gestures));
        addString("pedals",c.getString(R.string.pedal)+settings);
        addString("midi",c.getString(R.string.midi_send));
        addString("midisettings",c.getString(R.string.midi)+settings);
        addString("midisend",c.getString(R.string.midi_auto)+onoff);
        addString("beatbuddystart",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.start));
        addString("beatbuddystop",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.stop));
        addString("beatbuddypause",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.pause));
        addString("beatbuddyaccent",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.accent));
        addString("beatbuddyfill",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.fill));
        addString("beatbuddytrans1",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.transition)+" 1");
        addString("beatbuddytrans2",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.transition)+" 2");
        addString("beatbuddytrans3",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.transition)+" 3");
        addString("beatbuddytransnext",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.transition)+" "+c.getString(R.string.next));
        addString("beatbuddytransprev",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.transition)+" "+c.getString(R.string.previous));
        addString("beatbuddytransexit",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.transition)+" "+c.getString(R.string.exit));
        addString("beatbuddyxtrans1",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.exclusive)+" "+c.getString(R.string.transition)+" 1");
        addString("beatbuddyxtrans2",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.exclusive)+" "+c.getString(R.string.transition)+" 2");
        addString("beatbuddyxtrans3",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.exclusive)+" "+c.getString(R.string.transition)+" 3");
        addString("beatbuddyxtransnext",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.exclusive)+" "+c.getString(R.string.transition)+" "+c.getString(R.string.next));
        addString("beatbuddyxtransprev",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.exclusive)+" "+c.getString(R.string.transition)+" "+c.getString(R.string.previous));
        addString("beatbuddyxtransexit",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.exclusive)+" "+c.getString(R.string.transition)+" "+c.getString(R.string.exit));
        addString("beatbuddyhalf",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.half_time));
        addString("beatbuddyhalfexit",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.half_time)+" "+c.getString(R.string.exit));
        addString("beatbuddydouble",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.double_time));
        addString("beatbuddydoubleexit",c.getString(R.string.beat_buddy)+" "+c.getString(R.string.double_time)+" "+c.getString(R.string.exit));
        addString("midiaction1",c.getString(R.string.midi_action)+" "+1);
        addString("midiaction2",c.getString(R.string.midi_action)+" "+2);
        addString("midiaction3",c.getString(R.string.midi_action)+" "+3);
        addString("midiaction4",c.getString(R.string.midi_action)+" "+4);
        addString("midiaction5",c.getString(R.string.midi_action)+" "+5);
        addString("midiaction6",c.getString(R.string.midi_action)+" "+6);
        addString("midiaction7",c.getString(R.string.midi_action)+" "+7);
        addString("midiaction8",c.getString(R.string.midi_action)+" "+8);

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

    public ArrayList<String> getGestures() {
        return gestures;
    }

    public ArrayList<String> getGestureDescriptions() {
        return gestureDescriptions;
    }

    public String getDescriptionFromCode(String code) {
        Log.d(TAG,"getDescriptionFromCode("+code+"):"+getGestureDescriptions().get(gestures.indexOf(code)));
        if (gestures!=null && gestureDescriptions!=null && gestures.contains(code)) {
            return gestureDescriptions.get(gestures.indexOf(code));
        } else {
            return "";
        }
    }

    public String getCodeFromDescription(String description) {
        Log.d(TAG,"getCodeFromDescription("+description+"):"+getGestures().get(gestureDescriptions.indexOf(description)));
        if (gestures!=null && gestureDescriptions!=null && gestureDescriptions.contains(description)) {
            return gestures.get(gestureDescriptions.indexOf(description));
        } else {
            return "";
        }
    }
}

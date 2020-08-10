package com.garethevans.church.opensongtablet.pagebuttons;

// This is used to set up the correct page button icons based on what the user wants them to be

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class PageButtons {

    Context c;
    Preferences preferences;
    MainActivityInterface mainActivityInterface;
    float pageButtonAlpha;
    int pageButtonSize;
    String pageButtonCustom1Action, pageButtonCustom2Action, pageButtonCustom3Action,
            pageButtonCustom4Action, pageButtonCustom5Action, pageButtonCustom6Action;

    PageButtons(Context c, Preferences preferences, MainActivityInterface mainActivityInterface) {
        this.c = c;
        this.preferences = preferences;
        this.mainActivityInterface = mainActivityInterface;
        getPreferences();
    }

    private void getPreferences() {
        pageButtonAlpha = preferences.getMyPreferenceFloat(c,"pageButtonAlpha",0.6f);
        pageButtonCustom1Action = preferences.getMyPreferenceString(c,"pageButtonCustom1Action","");
        pageButtonCustom2Action = preferences.getMyPreferenceString(c,"pageButtonCustom2Action","");
        pageButtonCustom3Action = preferences.getMyPreferenceString(c,"pageButtonCustom3Action","");
        pageButtonCustom4Action = preferences.getMyPreferenceString(c,"pageButtonCustom4Action","");
        pageButtonCustom5Action = preferences.getMyPreferenceString(c,"pageButtonCustom5Action","");
        pageButtonCustom6Action = preferences.getMyPreferenceString(c,"pageButtonCustom6Action","");
        pageButtonSize = preferences.getMyPreferenceInt(c,"pageButtonSize",FloatingActionButton.SIZE_NORMAL);
    }

    public void updateAlpha(float pageButtonAlpha) {
        this.pageButtonAlpha = pageButtonAlpha;
        preferences.setMyPreferenceFloat(c,"pageButtonAlpha",pageButtonAlpha);
    }

    public ArrayList<String> setupButtonActions() {
        ArrayList<String> buttonActions = new ArrayList<>();
        buttonActions.add("");
        buttonActions.add("transpose");
        buttonActions.add("set");
        buttonActions.add("pad");
        buttonActions.add("metronome");
        buttonActions.add("autoscroll");
        buttonActions.add("link");
        buttonActions.add("chordfingerings");
        buttonActions.add("stickynotes");
        buttonActions.add("pdfpage");
        buttonActions.add("highlight");
        buttonActions.add("editsong");
        buttonActions.add("theme");
        buttonActions.add("autoscale");
        buttonActions.add("fonts");
        buttonActions.add("profiles");
        buttonActions.add("gestures");
        buttonActions.add("pedal");
        buttonActions.add("showchords");
        buttonActions.add("showcapo");
        buttonActions.add("showlyrics");
        buttonActions.add("search");
        buttonActions.add("randomsong");
        buttonActions.add("abc");
        buttonActions.add("inc_autoscroll_speed");
        buttonActions.add("dec_autoscroll_speed");
        buttonActions.add("toggle_autoscroll_pause");
        buttonActions.add("midi");
        buttonActions.add("bible");
        buttonActions.add("exit");
        return buttonActions;
    }

    public ArrayList<String> setUpButtonText() {
        ArrayList<String> buttonText = new ArrayList<>();
        buttonText.add("");
        buttonText.add(c.getResources().getString(R.string.transpose));
        buttonText.add(c.getResources().getString(R.string.set));
        buttonText.add(c.getResources().getString(R.string.pad));
        buttonText.add(c.getResources().getString(R.string.metronome));
        buttonText.add(c.getResources().getString(R.string.autoscroll));
        buttonText.add(c.getResources().getString(R.string.link));
        buttonText.add(c.getResources().getString(R.string.chord_fingering));
        buttonText.add(c.getResources().getString(R.string.stickynotes));
        buttonText.add(c.getResources().getString(R.string.pdf_selectpage));
        buttonText.add(c.getResources().getString(R.string.highlight));
        buttonText.add(c.getResources().getString(R.string.edit));
        buttonText.add(c.getResources().getString(R.string.choose_theme));
        buttonText.add(c.getResources().getString(R.string.autoscale_toggle));
        buttonText.add(c.getResources().getString(R.string.choose_fonts));
        buttonText.add(c.getResources().getString(R.string.profile));
        buttonText.add(c.getResources().getString(R.string.customgestures));
        buttonText.add(c.getResources().getString(R.string.pedal));
        buttonText.add(c.getResources().getString(R.string.showchords));
        buttonText.add(c.getResources().getString(R.string.showcapo));
        buttonText.add(c.getResources().getString(R.string.showlyrics));
        buttonText.add(c.getResources().getString(R.string.search));
        buttonText.add(c.getResources().getString(R.string.random_song));
        buttonText.add(c.getResources().getString(R.string.music_score));
        buttonText.add(c.getResources().getString(R.string.inc_autoscroll_speed));
        buttonText.add(c.getResources().getString(R.string.dec_autoscroll_speed));
        buttonText.add(c.getResources().getString(R.string.toggle_autoscroll_pause));
        buttonText.add(c.getResources().getString(R.string.midi));
        buttonText.add(c.getResources().getString(R.string.bible_search));
        buttonText.add(c.getResources().getString(R.string.exit));
        return buttonText;
    }

    public ArrayList<String> shortActionText() {
        ArrayList<String> text = new ArrayList<>();
        text.add("");
        text.add("Open the transpose window");
        text.add("Display the current set");
        text.add("Start/stop the pad");
        text.add("Start/stop the metronome");
        text.add("Start/stop autoscrolling");
        text.add("Display/change the links for the song");
        text.add("Display the chord fingerings");
        text.add("Show/hide the sticky note");
        text.add("Display the PDF page chooser");
        text.add("Show/hide the highlighter notes");
        text.add("Edit the song");
        text.add("Select a theme");
        text.add("Select autoscale type");
        text.add("Select fonts");
        text.add("Display profile options");
        text.add("Display custom gesture options");
        text.add("Configure pedal options");
        text.add("Show/hide the chords");
        text.add("Show/hide the capo chords (if available)");
        text.add("Show/hide the lyrics");
        text.add("Display song search menu");
        text.add("Pick a random song");
        text.add("Display the score/notation");
        text.add("Increase the autoscroll speed");
        text.add("Decrease the autoscroll speed");
        text.add("Pause/resume autoscroll");
        text.add("Midi settings");
        text.add("Bible search");
        text.add("Exit app");
        return text;
    }

    public ArrayList<String> longActionText() {
        ArrayList<String> text = new ArrayList<>();
        text.add("");
        text.add("Change chord settings");
        text.add("");
        text.add("Display the settings");
        text.add("Display the settings");
        text.add("Display the settings");
        text.add("");
        text.add("Display chord fingerings for capo chords (if available)");
        text.add("Edit the sticky note");
        text.add("");
        text.add("Create/edit the highlighter notes");
        text.add("");
        text.add("Edit the theme");
        text.add("");
        text.add("");
        text.add("");
        text.add("");
        text.add("");
        text.add("");
        text.add("");
        text.add("");
        text.add("Configure random song settings");
        text.add("Edit the score/notation");
        text.add("");
        text.add("");
        text.add("");
        text.add("");
        text.add("");
        text.add("");
        return text;
    }

    public void setupButton(FloatingActionButton fab, String action) {
        fab.setAlpha(pageButtonAlpha);
        Drawable drawable;
        switch (action) {
            case "":
            default:
                drawable = c.getResources().getDrawable(R.drawable.ic_help_outline_white_36dp);
                fab.setOnClickListener(new NavigateListener(R.id.nav_performance));
                break;

            case "set":
                drawable = c.getResources().getDrawable(R.drawable.ic_format_list_numbers_white_36dp);
                break;

            case "pad":
                drawable = c.getResources().getDrawable(R.drawable.ic_amplifier_white_36dp);
                break;

            case "metronome":
                drawable = c.getResources().getDrawable(R.drawable.ic_pulse_white_36dp);
                break;

            case "autoscroll":
                drawable = c.getResources().getDrawable(R.drawable.ic_rotate_right_white_36dp);
                break;

            case "link":
                drawable = c.getResources().getDrawable(R.drawable.ic_link_white_36dp);
                break;

            case "chordfingerings":
                drawable = c.getResources().getDrawable(R.drawable.ic_guitar_electric_white_36dp);
                break;

            case "stickynotes":
                drawable = c.getResources().getDrawable(R.drawable.ic_note_text_white_36dp);
                break;

            case "pdfpage":
                drawable = c.getResources().getDrawable(R.drawable.ic_book_white_36dp);
                break;

            case "highlight":
                drawable = c.getResources().getDrawable(R.drawable.ic_highlighter_white_36dp);
                break;

            case "editsong":
                drawable = c.getResources().getDrawable(R.drawable.ic_table_edit_white_36dp);
                break;

            case "theme":
                drawable = c.getResources().getDrawable(R.drawable.ic_theme_light_dark_white_36dp);
                break;

            case "autoscale":
                drawable = c.getResources().getDrawable(R.drawable.ic_arrow_expand_white_36dp);
                break;

            case "fonts":
                drawable = c.getResources().getDrawable(R.drawable.ic_format_text_white_36dp);
                break;

            case "profiles":
                drawable = c.getResources().getDrawable(R.drawable.ic_account_white_36dp);
                break;

            case "gestures":
                drawable = c.getResources().getDrawable(R.drawable.ic_fingerprint_white_36dp);
                break;

            case "pedal":
                drawable = c.getResources().getDrawable(R.drawable.ic_pedal_white_36dp);
                break;

            case "transpose":
                drawable = c.getResources().getDrawable(R.drawable.ic_transpose_white_36dp);
                break;

            case "showchords":
                drawable = c.getResources().getDrawable(R.drawable.ic_guitar_electric_white_36dp);
                break;

            case "showcapo":
                drawable = c.getResources().getDrawable(R.drawable.ic_capo_white_36dp);
                break;

            case "showlyrics":
                drawable = c.getResources().getDrawable(R.drawable.ic_voice_white_36dp);
                break;

            case "search":
                drawable = c.getResources().getDrawable(R.drawable.ic_magnify_white_36dp);
                break;

            case "randomsong":
                drawable = c.getResources().getDrawable(R.drawable.ic_shuffle_white_36dp);
                break;

            case "abc":
                drawable = c.getResources().getDrawable(R.drawable.ic_clef_white_36dp);
                break;

            case "inc_autoscroll_speed":
                drawable = c.getResources().getDrawable(R.drawable.ic_autoscroll_plus_white_36dp);
                break;

            case "dec_autoscroll_speed":
                drawable = c.getResources().getDrawable(R.drawable.ic_autoscroll_minus_white_36dp);
                break;

            case "toggle_autoscroll_pause":
                drawable = c.getResources().getDrawable(R.drawable.ic_autoscroll_pause_white_36dp);
                break;

            case "midi":
                drawable = c.getResources().getDrawable(R.drawable.ic_midi_white_36dp);
                break;

            case "bible":
                drawable = c.getResources().getDrawable(R.drawable.ic_bible_white_36dp);
                break;

            case "exit":
                drawable = c.getResources().getDrawable(R.drawable.ic_exit_to_app_white_36dp);
                break;
        }

        fab.setImageDrawable(drawable);

    }

    private class NavigateListener implements View.OnClickListener {

        int locationId;

        NavigateListener(int locationId) {
            this.locationId = locationId;
        }

        @Override
        public void onClick(View v) {
            mainActivityInterface.navigateToFragment(locationId);
        }
    }
}

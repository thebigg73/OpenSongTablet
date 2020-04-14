package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpThemeChooserFragment extends DialogFragment {

    private Preferences preferences;
    private View dark_font, dark_background, dark_verse, dark_chorus, dark_prechorus, dark_bridge,
            dark_comment, dark_tag, dark_chord, dark_custom, dark_capo, dark_presofont,
            dark_presoinfofont, dark_presoshadow, dark_presoalertfont, dark_metronome,
            dark_pagebuttons, dark_stickytext, dark_stickybg, dark_extrainfobg, dark_extrainfo;
    private View light_font, light_background, light_verse, light_chorus, light_prechorus, light_bridge,
            light_comment, light_tag, light_chord, light_custom, light_capo, light_presofont,
            light_presoinfofont, light_presoshadow, light_presoalertfont, light_metronome,
            light_pagebuttons, light_stickytext, light_stickybg, light_extrainfobg, light_extrainfo;
    private View custom1_font, custom1_background, custom1_verse, custom1_chorus, custom1_prechorus, custom1_bridge,
            custom1_comment, custom1_tag, custom1_chord, custom1_custom, custom1_capo, custom1_presofont,
            custom1_presoinfofont, custom1_presoshadow, custom1_presoalertfont, custom1_metronome,
            custom1_pagebuttons, custom1_stickytext, custom1_stickybg, custom1_extrainfobg, custom1_extrainfo;
    private View custom2_font, custom2_background, custom2_verse, custom2_chorus, custom2_prechorus, custom2_bridge,
            custom2_comment, custom2_tag, custom2_chord, custom2_custom, custom2_capo, custom2_presofont,
            custom2_presoinfofont, custom2_presoshadow, custom2_presoalertfont, custom2_metronome,
            custom2_pagebuttons, custom2_stickytext, custom2_stickybg, custom2_extrainfobg, custom2_extrainfo;
    private TextView dark_theme_heading, light_theme_heading, custom1_theme_heading, custom2_theme_heading, title,
            pagebg_TextView, lyrics_TextView, chords_TextView, capo_TextView, verse_TextView, chorus_TextView,
            prechorus_TextView, bridge_TextView, tag_TextView, custom_TextView, comment_TextView,
            preso_TextView, presoinfo_TextView, presoalert_TextView, presoshadow_TextView, metronome_TextView,
            pagebuttons_TextView, notes_TextView, notesbg_TextView, padbg_TextView, pad_TextView, item_TextView;
    private Button resetcolours;

    private int initialcolor = StaticVariables.black;
    private String buttonClicked = "";
    private MyInterface mListener;
    private int dark_lyricsBackgroundColor, dark_lyricsBridgeColor, dark_lyricsCapoColor, dark_lyricsChordsColor,
            dark_lyricsChorusColor, dark_lyricsCommentColor, dark_lyricsCustomColor, dark_lyricsPreChorusColor,
            dark_lyricsTagColor, dark_lyricsTextColor, dark_lyricsVerseColor, dark_presoFontColor,
            dark_presoShadowColor, dark_presoInfoColor, dark_presoAlertColor, dark_metronomeColor,
            dark_pageButtonsColor, dark_stickyTextColor, dark_stickyBackgroundColor,
            dark_extraInfoTextColor, dark_extraInfoBgColor;
    private int light_lyricsBackgroundColor, light_lyricsBridgeColor, light_lyricsCapoColor, light_lyricsChordsColor,
            light_lyricsChorusColor, light_lyricsCommentColor, light_lyricsCustomColor, light_lyricsPreChorusColor,
            light_lyricsTagColor, light_lyricsTextColor, light_lyricsVerseColor, light_presoFontColor,
            light_presoShadowColor, light_presoInfoColor, light_presoAlertColor, light_metronomeColor,
            light_pageButtonsColor, light_stickyTextColor, light_stickyBackgroundColor,
            light_extraInfoTextColor, light_extraInfoBgColor;
    private int custom1_lyricsBackgroundColor, custom1_lyricsBridgeColor, custom1_lyricsCapoColor, custom1_lyricsChordsColor,
            custom1_lyricsChorusColor, custom1_lyricsCommentColor, custom1_lyricsCustomColor, custom1_lyricsPreChorusColor,
            custom1_lyricsTagColor, custom1_lyricsTextColor, custom1_lyricsVerseColor, custom1_presoFontColor,
            custom1_presoShadowColor, custom1_presoInfoColor, custom1_presoAlertColor, custom1_metronomeColor,
            custom1_pageButtonsColor, custom1_stickyTextColor, custom1_stickyBackgroundColor,
            custom1_extraInfoTextColor, custom1_extraInfoBgColor;
    private int custom2_lyricsBackgroundColor, custom2_lyricsBridgeColor, custom2_lyricsCapoColor, custom2_lyricsChordsColor,
            custom2_lyricsChorusColor, custom2_lyricsCommentColor, custom2_lyricsCustomColor, custom2_lyricsPreChorusColor,
            custom2_lyricsTagColor, custom2_lyricsTextColor, custom2_lyricsVerseColor, custom2_presoFontColor,
            custom2_presoShadowColor, custom2_presoInfoColor, custom2_presoAlertColor, custom2_metronomeColor,
            custom2_pageButtonsColor, custom2_stickyTextColor, custom2_stickyBackgroundColor,
            custom2_extraInfoTextColor, custom2_extraInfoBgColor;

    private void updateFontSizes() {
        float menuFontSize = preferences.getMyPreferenceFloat(getActivity(),"songMenuAlphaIndexSize",14.0f);
        ResizeMenuItems resizeMenuItems = new ResizeMenuItems();
        resizeMenuItems.updateTextViewSize(title,menuFontSize,"L",false);
        resizeMenuItems.updateTextViewSize(item_TextView,menuFontSize,"",false);
        resizeMenuItems.updateTextViewSize(dark_theme_heading,menuFontSize,"",false);
        resizeMenuItems.updateTextViewSize(light_theme_heading,menuFontSize,"",false);
        resizeMenuItems.updateTextViewSize(custom1_theme_heading,menuFontSize,"",false);
        resizeMenuItems.updateTextViewSize(custom2_theme_heading,menuFontSize,"",false);
        resizeMenuItems.updateTextViewSize(pagebg_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(lyrics_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(chords_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(capo_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(verse_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(chorus_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(prechorus_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(bridge_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(tag_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(custom_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(comment_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(preso_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(presoinfo_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(presoalert_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(presoshadow_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(metronome_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(pagebuttons_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(notes_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(notesbg_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(padbg_TextView,menuFontSize,"s",false);
        resizeMenuItems.updateTextViewSize(pad_TextView,menuFontSize,"s",false);

        resizeMenuItems.updateButtonTextSize(resetcolours,menuFontSize,"",true);
    }

    static PopUpThemeChooserFragment newInstance() {
        PopUpThemeChooserFragment frag;
        frag = new PopUpThemeChooserFragment();
        return frag;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_themechooser, container, false);

        title = V.findViewById(R.id.dialogtitle);
        title.setText(getResources().getString(R.string.choose_theme));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                doClose();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();

        // Load up the preferences
        loadUpPreferences();

        // Identify the views
        identifyViews(V);

        // Update the text size
        updateFontSizes();

        // Run the script to set the button colours
        setButtonColors();

        // Set the appropriate theme button based on what is already set
        setUpButtons();

        PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog(), preferences);
        return V;
    }

    private void identifyViews(View V) {
        item_TextView = V.findViewById(R.id.item_TextView);
        pagebg_TextView = V.findViewById(R.id.pagebg_TextView);
        lyrics_TextView = V.findViewById(R.id.lyrics_TextView);
        chords_TextView = V.findViewById(R.id.chords_TextView);
        capo_TextView = V.findViewById(R.id.capo_TextView);
        verse_TextView = V.findViewById(R.id.verse_TextView);
        chorus_TextView = V.findViewById(R.id.chorus_TextView);
        prechorus_TextView = V.findViewById(R.id.prechorus_TextView);
        bridge_TextView = V.findViewById(R.id.bridge_TextView);
        tag_TextView = V.findViewById(R.id.tag_TextView);
        custom_TextView = V.findViewById(R.id.custom_TextView);
        comment_TextView = V.findViewById(R.id.comment_TextView);
        preso_TextView = V.findViewById(R.id.preso_TextView);
        presoinfo_TextView = V.findViewById(R.id.presoinfo_TextView);
        presoalert_TextView = V.findViewById(R.id.presoalert_TextView);
        presoshadow_TextView = V.findViewById(R.id.presoshadow_TextView);
        metronome_TextView = V.findViewById(R.id.metronome_TextView);
        pagebuttons_TextView = V.findViewById(R.id.pagebuttons_TextView);
        notes_TextView = V.findViewById(R.id.notes_TextView);
        notesbg_TextView = V.findViewById(R.id.notesbg_TextView);
        padbg_TextView = V.findViewById(R.id.padbg_TextView);
        pad_TextView = V.findViewById(R.id.pad_TextView);

        String s = getResources().getString(R.string.stickynotes);
        notes_TextView.setText(s);
        s = s + "\n" + getResources().getString(R.string.presoBackground);
        notesbg_TextView.setText(s);

        // Extra info text
        String eit = getString(R.string.pad) + " / " + getString(R.string.autoscroll) + " / " + getString(R.string.capo_color);
        String eit2 = getString(R.string.pad) + " / " + getString(R.string.autoscroll) + " / " + getString(R.string.capo_color) +
                " (" + getString(R.string.presoBackground) + ")";
        pad_TextView.setText(eit);
        padbg_TextView.setText(eit2);

        // Define the buttons
        dark_background = V.findViewById(R.id.page_dark);
        dark_font = V.findViewById(R.id.lyrics_dark);
        dark_verse = V.findViewById(R.id.verse_dark);
        dark_chorus = V.findViewById(R.id.chorus_dark);
        dark_prechorus = V.findViewById(R.id.prechorus_dark);
        dark_bridge = V.findViewById(R.id.bridge_dark);
        dark_comment = V.findViewById(R.id.comment_dark);
        dark_tag = V.findViewById(R.id.tag_dark);
        dark_chord = V.findViewById(R.id.chords_dark);
        dark_custom = V.findViewById(R.id.custom_dark);
        dark_capo = V.findViewById(R.id.capo_dark);
        dark_presofont = V.findViewById(R.id.presofont_dark);
        dark_presoinfofont = V.findViewById(R.id.presoinfofont_dark);
        dark_presoalertfont = V.findViewById(R.id.presoalertfont_dark);
        dark_presoshadow = V.findViewById(R.id.presofontshadow_dark);
        dark_metronome = V.findViewById(R.id.metronome_dark);
        dark_pagebuttons = V.findViewById(R.id.pagebuttons_dark);
        dark_stickytext = V.findViewById(R.id.stickytext_dark);
        dark_stickybg = V.findViewById(R.id.stickybg_dark);
        dark_extrainfobg = V.findViewById(R.id.dark_extrainfobg);
        dark_extrainfo = V.findViewById(R.id.dark_extrainfo);

        light_background = V.findViewById(R.id.page_light);
        light_font = V.findViewById(R.id.lyrics_light);
        light_verse = V.findViewById(R.id.verse_light);
        light_chorus = V.findViewById(R.id.chorus_light);
        light_prechorus = V.findViewById(R.id.prechorus_light);
        light_bridge = V.findViewById(R.id.bridge_light);
        light_comment = V.findViewById(R.id.comment_light);
        light_tag = V.findViewById(R.id.tag_light);
        light_chord = V.findViewById(R.id.chords_light);
        light_custom = V.findViewById(R.id.custom_light);
        light_capo = V.findViewById(R.id.capo_light);
        light_presofont = V.findViewById(R.id.presofont_light);
        light_presoinfofont = V.findViewById(R.id.presoinfofont_light);
        light_presoalertfont = V.findViewById(R.id.presoalertfont_light);
        light_presoshadow = V.findViewById(R.id.presofontshadow_light);
        light_metronome = V.findViewById(R.id.metronome_light);
        light_pagebuttons = V.findViewById(R.id.pagebuttons_light);
        light_stickytext = V.findViewById(R.id.stickytext_light);
        light_stickybg = V.findViewById(R.id.stickybg_light);
        light_extrainfobg = V.findViewById(R.id.light_extrainfobg);
        light_extrainfo = V.findViewById(R.id.light_extrainfo);

        custom1_background = V.findViewById(R.id.page_custom1);
        custom1_font = V.findViewById(R.id.lyrics_custom1);
        custom1_verse = V.findViewById(R.id.verse_custom1);
        custom1_chorus = V.findViewById(R.id.chorus_custom1);
        custom1_prechorus = V.findViewById(R.id.prechorus_custom1);
        custom1_bridge = V.findViewById(R.id.bridge_custom1);
        custom1_comment = V.findViewById(R.id.comment_custom1);
        custom1_tag = V.findViewById(R.id.tag_custom1);
        custom1_chord = V.findViewById(R.id.chords_custom1);
        custom1_custom = V.findViewById(R.id.custom_custom1);
        custom1_capo = V.findViewById(R.id.capo_custom1);
        custom1_presofont = V.findViewById(R.id.presofont_custom1);
        custom1_presoinfofont = V.findViewById(R.id.presoinfofont_custom1);
        custom1_presoalertfont = V.findViewById(R.id.presoalertfont_custom1);
        custom1_presoshadow = V.findViewById(R.id.presofontshadow_custom1);
        custom1_metronome = V.findViewById(R.id.metronome_custom1);
        custom1_pagebuttons = V.findViewById(R.id.pagebuttons_custom1);
        custom1_stickytext = V.findViewById(R.id.stickytext_custom1);
        custom1_stickybg = V.findViewById(R.id.stickybg_custom1);
        custom1_extrainfobg = V.findViewById(R.id.custom1_extrainfobg);
        custom1_extrainfo = V.findViewById(R.id.custom1_extrainfo);

        custom2_background = V.findViewById(R.id.page_custom2);
        custom2_font = V.findViewById(R.id.lyrics_custom2);
        custom2_verse = V.findViewById(R.id.verse_custom2);
        custom2_chorus = V.findViewById(R.id.chorus_custom2);
        custom2_prechorus = V.findViewById(R.id.prechorus_custom2);
        custom2_bridge = V.findViewById(R.id.bridge_custom2);
        custom2_comment = V.findViewById(R.id.comment_custom2);
        custom2_tag = V.findViewById(R.id.tag_custom2);
        custom2_chord = V.findViewById(R.id.chords_custom2);
        custom2_custom = V.findViewById(R.id.custom_custom2);
        custom2_capo = V.findViewById(R.id.capo_custom2);
        custom2_presofont = V.findViewById(R.id.presofont_custom2);
        custom2_presoinfofont = V.findViewById(R.id.presoinfofont_custom2);
        custom2_presoalertfont = V.findViewById(R.id.presoalertfont_custom2);
        custom2_presoshadow = V.findViewById(R.id.presofontshadow_custom2);
        custom2_metronome = V.findViewById(R.id.metronome_custom2);
        custom2_pagebuttons = V.findViewById(R.id.pagebuttons_custom2);
        custom2_stickytext = V.findViewById(R.id.stickytext_custom2);
        custom2_stickybg = V.findViewById(R.id.stickybg_custom2);
        custom2_extrainfobg = V.findViewById(R.id.custom2_extrainfobg);
        custom2_extrainfo = V.findViewById(R.id.custom2_extrainfo);

        dark_theme_heading = V.findViewById(R.id.dark_theme_heading);
        light_theme_heading = V.findViewById(R.id.light_theme_heading);
        custom1_theme_heading = V.findViewById(R.id.custom1_theme_heading);
        custom2_theme_heading = V.findViewById(R.id.custom2_theme_heading);

        resetcolours = V.findViewById(R.id.resetcolours);
        resetcolours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "resetcolours";
                resetColours();
                saveAllColors();
                mListener.refreshAll();
                setButtonColors();
            }
        });

        // Listen for the theme changing
        dark_theme_heading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticVariables.mDisplayTheme = "dark";
                doThemeSwitch("dark");
            }
        });
        light_theme_heading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticVariables.mDisplayTheme = "light";
                doThemeSwitch("light");
            }
        });
        custom1_theme_heading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticVariables.mDisplayTheme = "custom1";
                doThemeSwitch("custom1");
            }
        });
        custom2_theme_heading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticVariables.mDisplayTheme = "custom2";
                doThemeSwitch("custom2");
            }
        });
    }

    private void loadUpPreferences() {
        dark_lyricsBackgroundColor = preferences.getMyPreferenceInt(getActivity(), "dark_lyricsBackgroundColor", StaticVariables.black);
        dark_lyricsTextColor = preferences.getMyPreferenceInt(getActivity(), "dark_lyricsTextColor", StaticVariables.white);
        dark_lyricsVerseColor = preferences.getMyPreferenceInt(getActivity(), "dark_lyricsVerseColor", StaticVariables.black);
        dark_lyricsChorusColor = preferences.getMyPreferenceInt(getActivity(), "dark_lyricsChorusColor", StaticVariables.vdarkblue);
        dark_lyricsPreChorusColor = preferences.getMyPreferenceInt(getActivity(), "dark_lyricsPreChorusColor", StaticVariables.darkishgreen);
        dark_lyricsBridgeColor = preferences.getMyPreferenceInt(getActivity(), "dark_lyricsBridgeColor", StaticVariables.vdarkred);
        dark_lyricsCommentColor = preferences.getMyPreferenceInt(getActivity(), "dark_lyricsCommentColor", StaticVariables.vdarkgreen);
        dark_lyricsTagColor = preferences.getMyPreferenceInt(getActivity(), "dark_lyricsTagColor", StaticVariables.darkpurple);
        dark_lyricsChordsColor = preferences.getMyPreferenceInt(getActivity(), "dark_lyricsChordsColor", StaticVariables.yellow);
        dark_lyricsCustomColor = preferences.getMyPreferenceInt(getActivity(), "dark_lyricsCustomColor", StaticVariables.vdarkyellow);
        dark_lyricsCapoColor = preferences.getMyPreferenceInt(getActivity(), "dark_lyricsCapoColor", StaticVariables.red);
        dark_presoFontColor = preferences.getMyPreferenceInt(getActivity(), "dark_presoFontColor", StaticVariables.white);
        dark_presoShadowColor = preferences.getMyPreferenceInt(getActivity(), "dark_presoShadowColor", StaticVariables.black);
        dark_presoInfoColor = preferences.getMyPreferenceInt(getActivity(), "dark_presoInfoColor", StaticVariables.white);
        dark_presoAlertColor = preferences.getMyPreferenceInt(getActivity(), "dark_presoAlertColor", StaticVariables.red);
        dark_metronomeColor = preferences.getMyPreferenceInt(getActivity(), "dark_metronomeColor", StaticVariables.darkishred);
        dark_pageButtonsColor = preferences.getMyPreferenceInt(getActivity(), "dark_pageButtonsColor", StaticVariables.purplyblue);
        dark_stickyTextColor = preferences.getMyPreferenceInt(getActivity(), "dark_stickyTextColor", StaticVariables.black);
        dark_stickyBackgroundColor = preferences.getMyPreferenceInt(getActivity(), "dark_stickyBackgroundColor", StaticVariables.lightyellow);
        dark_extraInfoTextColor = preferences.getMyPreferenceInt(getActivity(), "dark_extraInfoTextColor", StaticVariables.white);
        dark_extraInfoBgColor = preferences.getMyPreferenceInt(getActivity(), "dark_extraInfoBgColor", StaticVariables.grey);

        light_lyricsBackgroundColor = preferences.getMyPreferenceInt(getActivity(), "light_lyricsBackgroundColor", StaticVariables.white);
        light_lyricsTextColor = preferences.getMyPreferenceInt(getActivity(), "light_lyricsTextColor", StaticVariables.black);
        light_lyricsVerseColor = preferences.getMyPreferenceInt(getActivity(), "light_lyricsVerseColor", StaticVariables.white);
        light_lyricsChorusColor = preferences.getMyPreferenceInt(getActivity(), "light_lyricsChorusColor", StaticVariables.vlightpurple);
        light_lyricsPreChorusColor = preferences.getMyPreferenceInt(getActivity(), "light_lyricsPreChorusColor", StaticVariables.lightgreen);
        light_lyricsBridgeColor = preferences.getMyPreferenceInt(getActivity(), "light_lyricsBridgeColor", StaticVariables.vlightcyan);
        light_lyricsCommentColor = preferences.getMyPreferenceInt(getActivity(), "light_lyricsCommentColor", StaticVariables.vlightblue);
        light_lyricsTagColor = preferences.getMyPreferenceInt(getActivity(), "light_lyricsTagColor", StaticVariables.vlightgreen);
        light_lyricsChordsColor = preferences.getMyPreferenceInt(getActivity(), "light_lyricsChordsColor", StaticVariables.darkblue);
        light_lyricsCustomColor = preferences.getMyPreferenceInt(getActivity(), "light_lyricsCustomColor", StaticVariables.lightishcyan);
        light_lyricsCapoColor = preferences.getMyPreferenceInt(getActivity(), "light_lyricsCapoColor", StaticVariables.red);
        light_presoFontColor = preferences.getMyPreferenceInt(getActivity(), "light_presoFontColor", StaticVariables.black);
        light_presoShadowColor = preferences.getMyPreferenceInt(getActivity(), "light_presoShadowColor", StaticVariables.white);
        light_presoInfoColor = preferences.getMyPreferenceInt(getActivity(), "light_presoInfoColor", StaticVariables.black);
        light_presoAlertColor = preferences.getMyPreferenceInt(getActivity(), "light_presoAlertColor", StaticVariables.red);
        light_metronomeColor = preferences.getMyPreferenceInt(getActivity(), "light_metronomeColor", StaticVariables.darkishred);
        light_pageButtonsColor = preferences.getMyPreferenceInt(getActivity(), "light_pageButtonsColor", StaticVariables.purplyblue);
        light_stickyTextColor = preferences.getMyPreferenceInt(getActivity(), "light_stickyTextColor", StaticVariables.black);
        light_stickyBackgroundColor = preferences.getMyPreferenceInt(getActivity(), "light_stickyBackgroundColor", StaticVariables.lightyellow);
        light_extraInfoTextColor = preferences.getMyPreferenceInt(getActivity(), "light_extraInfoTextColor", StaticVariables.white);
        light_extraInfoBgColor = preferences.getMyPreferenceInt(getActivity(), "light_extraInfoBgColor", StaticVariables.grey);

        custom1_lyricsBackgroundColor = preferences.getMyPreferenceInt(getActivity(), "custom1_lyricsBackgroundColor", StaticVariables.black);
        custom1_lyricsTextColor = preferences.getMyPreferenceInt(getActivity(), "custom1_lyricsTextColor", StaticVariables.white);
        custom1_lyricsVerseColor = preferences.getMyPreferenceInt(getActivity(), "custom1_lyricsVerseColor", StaticVariables.black);
        custom1_lyricsChorusColor = preferences.getMyPreferenceInt(getActivity(), "custom1_lyricsChorusColor", StaticVariables.black);
        custom1_lyricsPreChorusColor = preferences.getMyPreferenceInt(getActivity(), "custom1_lyricsPreChorusColor", StaticVariables.black);
        custom1_lyricsBridgeColor = preferences.getMyPreferenceInt(getActivity(), "custom1_lyricsBridgeColor", StaticVariables.black);
        custom1_lyricsCommentColor = preferences.getMyPreferenceInt(getActivity(), "custom1_lyricsCommentColor", StaticVariables.black);
        custom1_lyricsTagColor = preferences.getMyPreferenceInt(getActivity(), "custom1_lyricsTagColor", StaticVariables.black);
        custom1_lyricsChordsColor = preferences.getMyPreferenceInt(getActivity(), "custom1_lyricsChordsColor", StaticVariables.yellow);
        custom1_lyricsCustomColor = preferences.getMyPreferenceInt(getActivity(), "custom1_lyricsCustomColor", StaticVariables.black);
        custom1_lyricsCapoColor = preferences.getMyPreferenceInt(getActivity(), "custom1_lyricsCapoColor", StaticVariables.red);
        custom1_presoFontColor = preferences.getMyPreferenceInt(getActivity(), "custom1_presoFontColor", StaticVariables.white);
        custom1_presoShadowColor = preferences.getMyPreferenceInt(getActivity(), "custom1_presoShadowColor", StaticVariables.black);
        custom1_presoInfoColor = preferences.getMyPreferenceInt(getActivity(), "custom1_presoInfoColor", StaticVariables.white);
        custom1_presoAlertColor = preferences.getMyPreferenceInt(getActivity(), "custom1_presoAlertColor", StaticVariables.red);
        custom1_metronomeColor = preferences.getMyPreferenceInt(getActivity(), "custom1_metronomeColor", StaticVariables.darkishred);
        custom1_pageButtonsColor = preferences.getMyPreferenceInt(getActivity(), "custom1_pageButtonsColor", StaticVariables.purplyblue);
        custom1_stickyTextColor = preferences.getMyPreferenceInt(getActivity(), "custom1_stickyTextColor", StaticVariables.black);
        custom1_stickyBackgroundColor = preferences.getMyPreferenceInt(getActivity(), "custom1_stickyBackgroundColor", StaticVariables.lightyellow);
        custom1_extraInfoTextColor = preferences.getMyPreferenceInt(getActivity(), "custom1_extraInfoTextColor", StaticVariables.white);
        custom1_extraInfoBgColor = preferences.getMyPreferenceInt(getActivity(), "custom1_extraInfoBgColor", StaticVariables.grey);

        custom2_lyricsBackgroundColor = preferences.getMyPreferenceInt(getActivity(), "custom2_lyricsBackgroundColor", StaticVariables.white);
        custom2_lyricsTextColor = preferences.getMyPreferenceInt(getActivity(), "custom2_lyricsTextColor", StaticVariables.black);
        custom2_lyricsVerseColor = preferences.getMyPreferenceInt(getActivity(), "custom2_lyricsVerseColor", StaticVariables.white);
        custom2_lyricsChorusColor = preferences.getMyPreferenceInt(getActivity(), "custom2_lyricsChorusColor", StaticVariables.white);
        custom2_lyricsPreChorusColor = preferences.getMyPreferenceInt(getActivity(), "custom2_lyricsPreChorusColor", StaticVariables.white);
        custom2_lyricsBridgeColor = preferences.getMyPreferenceInt(getActivity(), "custom2_lyricsBridgeColor", StaticVariables.white);
        custom2_lyricsCommentColor = preferences.getMyPreferenceInt(getActivity(), "custom2_lyricsCommentColor", StaticVariables.white);
        custom2_lyricsTagColor = preferences.getMyPreferenceInt(getActivity(), "custom2_lyricsTagColor", StaticVariables.white);
        custom2_lyricsChordsColor = preferences.getMyPreferenceInt(getActivity(), "custom2_lyricsChordsColor", StaticVariables.darkblue);
        custom2_lyricsCustomColor = preferences.getMyPreferenceInt(getActivity(), "custom2_lyricsCustomColor", StaticVariables.white);
        custom2_lyricsCapoColor = preferences.getMyPreferenceInt(getActivity(), "custom2_lyricsCapoColor", StaticVariables.red);
        custom2_presoFontColor = preferences.getMyPreferenceInt(getActivity(), "custom2_presoFontColor", StaticVariables.white);
        custom2_presoShadowColor = preferences.getMyPreferenceInt(getActivity(), "custom2_presoShadowColor", StaticVariables.black);
        custom2_presoInfoColor = preferences.getMyPreferenceInt(getActivity(), "custom2_presoInfoColor", StaticVariables.white);
        custom2_presoAlertColor = preferences.getMyPreferenceInt(getActivity(), "custom2_presoAlertColor", StaticVariables.red);
        custom2_metronomeColor = preferences.getMyPreferenceInt(getActivity(), "custom2_metronomeColor", StaticVariables.darkishred);
        custom2_pageButtonsColor = preferences.getMyPreferenceInt(getActivity(), "custom2_pageButtonsColor", StaticVariables.purplyblue);
        custom2_stickyTextColor = preferences.getMyPreferenceInt(getActivity(), "custom2_stickyTextColor", StaticVariables.black);
        custom2_stickyBackgroundColor = preferences.getMyPreferenceInt(getActivity(), "custom2_stickyBackgroundColor", StaticVariables.lightyellow);
        custom2_extraInfoTextColor = preferences.getMyPreferenceInt(getActivity(), "custom2_extraInfoTextColor", StaticVariables.white);
        custom2_extraInfoBgColor = preferences.getMyPreferenceInt(getActivity(), "custom2_extraInfoBgColor", StaticVariables.grey);
    }

    private void resetColours() {
        preferences.setMyPreferenceInt(getActivity(), "dark_lyricsBackgroundColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "dark_lyricsTextColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "dark_lyricsVerseColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "dark_lyricsChorusColor", StaticVariables.vdarkblue);
        preferences.setMyPreferenceInt(getActivity(), "dark_lyricsPreChorusColor", StaticVariables.darkishgreen);
        preferences.setMyPreferenceInt(getActivity(), "dark_lyricsBridgeColor", StaticVariables.vdarkred);
        preferences.setMyPreferenceInt(getActivity(), "dark_lyricsCommentColor", StaticVariables.vdarkgreen);
        preferences.setMyPreferenceInt(getActivity(), "dark_lyricsTagColor", StaticVariables.darkpurple);
        preferences.setMyPreferenceInt(getActivity(), "dark_lyricsChordsColor", StaticVariables.yellow);
        preferences.setMyPreferenceInt(getActivity(), "dark_lyricsCustomColor", StaticVariables.vdarkyellow);
        preferences.setMyPreferenceInt(getActivity(), "dark_lyricsCapoColor", StaticVariables.red);
        preferences.setMyPreferenceInt(getActivity(), "dark_presoFontColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "dark_presoShadowColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "dark_presoInfoColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "dark_presoAlertColor", StaticVariables.red);
        preferences.setMyPreferenceInt(getActivity(), "dark_metronomeColor", StaticVariables.darkishred);
        preferences.setMyPreferenceInt(getActivity(), "dark_pageButtonsColor", StaticVariables.purplyblue);
        preferences.setMyPreferenceInt(getActivity(), "dark_stickyTextColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "dark_stickyBackgroundColor", StaticVariables.lightyellow);
        preferences.setMyPreferenceInt(getActivity(), "dark_extraInfoTextColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "dark_extraInfoBgColor", StaticVariables.grey);

        preferences.setMyPreferenceInt(getActivity(), "light_lyricsBackgroundColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "light_lyricsTextColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "light_lyricsVerseColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "light_lyricsChorusColor", StaticVariables.vlightpurple);
        preferences.setMyPreferenceInt(getActivity(), "light_lyricsPreChorusColor", StaticVariables.lightgreen);
        preferences.setMyPreferenceInt(getActivity(), "light_lyricsBridgeColor", StaticVariables.vlightcyan);
        preferences.setMyPreferenceInt(getActivity(), "light_lyricsCommentColor", StaticVariables.vlightblue);
        preferences.setMyPreferenceInt(getActivity(), "light_lyricsTagColor", StaticVariables.vlightgreen);
        preferences.setMyPreferenceInt(getActivity(), "light_lyricsChordsColor", StaticVariables.darkblue);
        preferences.setMyPreferenceInt(getActivity(), "light_lyricsCustomColor", StaticVariables.lightishcyan);
        preferences.setMyPreferenceInt(getActivity(), "light_lyricsCapoColor", StaticVariables.red);
        preferences.setMyPreferenceInt(getActivity(), "light_presoFontColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "light_presoShadowColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "light_presoInfoColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "light_presoAlertColor", StaticVariables.red);
        preferences.setMyPreferenceInt(getActivity(), "light_metronomeColor", StaticVariables.darkishred);
        preferences.setMyPreferenceInt(getActivity(), "light_pageButtonsColor", StaticVariables.purplyblue);
        preferences.setMyPreferenceInt(getActivity(), "light_stickyTextColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "light_stickyBackgroundColor", StaticVariables.lightyellow);
        preferences.setMyPreferenceInt(getActivity(), "light_extraInfoTextColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "light_extraInfoBgColor", StaticVariables.grey);

        preferences.setMyPreferenceInt(getActivity(), "custom1_lyricsBackgroundColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "custom1_lyricsTextColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom1_lyricsVerseColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "custom1_lyricsChorusColor", StaticVariables.vdarkblue);
        preferences.setMyPreferenceInt(getActivity(), "custom1_lyricsPreChorusColor", StaticVariables.darkishgreen);
        preferences.setMyPreferenceInt(getActivity(), "custom1_lyricsBridgeColor", StaticVariables.vdarkred);
        preferences.setMyPreferenceInt(getActivity(), "custom1_lyricsCommentColor", StaticVariables.vdarkgreen);
        preferences.setMyPreferenceInt(getActivity(), "custom1_lyricsTagColor", StaticVariables.darkpurple);
        preferences.setMyPreferenceInt(getActivity(), "custom1_lyricsChordsColor", StaticVariables.yellow);
        preferences.setMyPreferenceInt(getActivity(), "custom1_lyricsCustomColor", StaticVariables.vdarkyellow);
        preferences.setMyPreferenceInt(getActivity(), "custom1_lyricsCapoColor", StaticVariables.red);
        preferences.setMyPreferenceInt(getActivity(), "custom1_presoFontColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom1_presoShadowColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "custom1_presoInfoColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom1_presoAlertColor", StaticVariables.red);
        preferences.setMyPreferenceInt(getActivity(), "custom1_metronomeColor", StaticVariables.darkishred);
        preferences.setMyPreferenceInt(getActivity(), "custom1_pageButtonsColor", StaticVariables.purplyblue);
        preferences.setMyPreferenceInt(getActivity(), "custom1_stickyTextColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "custom1_stickyBackgroundColor", StaticVariables.lightyellow);
        preferences.setMyPreferenceInt(getActivity(), "custom1_extraInfoTextColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom1_extraInfoBgColor", StaticVariables.grey);

        preferences.setMyPreferenceInt(getActivity(), "custom2_lyricsBackgroundColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom2_lyricsTextColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "custom2_lyricsVerseColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom2_lyricsChorusColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom2_lyricsPreChorusColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom2_lyricsBridgeColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom2_lyricsCommentColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom2_lyricsTagColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom2_lyricsChordsColor", StaticVariables.darkblue);
        preferences.setMyPreferenceInt(getActivity(), "custom2_lyricsCustomColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom2_lyricsCapoColor", StaticVariables.red);
        preferences.setMyPreferenceInt(getActivity(), "custom2_presoFontColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom2_presoShadowColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "custom2_presoInfoColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom2_presoAlertColor", StaticVariables.red);
        preferences.setMyPreferenceInt(getActivity(), "custom2_metronomeColor", StaticVariables.darkishred);
        preferences.setMyPreferenceInt(getActivity(), "custom2_pageButtonsColor", StaticVariables.purplyblue);
        preferences.setMyPreferenceInt(getActivity(), "custom2_stickyTextColor", StaticVariables.black);
        preferences.setMyPreferenceInt(getActivity(), "custom2_stickyBackgroundColor", StaticVariables.lightyellow);
        preferences.setMyPreferenceInt(getActivity(), "custom2_extraInfoTextColor", StaticVariables.white);
        preferences.setMyPreferenceInt(getActivity(), "custom2_extraInfoBgColor", StaticVariables.grey);
    }

    private void setUpButtons() {
        dark_theme_heading.setBackgroundColor(StaticVariables.lightgrey);
        dark_theme_heading.setTextColor(StaticVariables.yellow);
        light_theme_heading.setBackgroundColor(StaticVariables.lightgrey);
        light_theme_heading.setTextColor(StaticVariables.yellow);
        custom1_theme_heading.setBackgroundColor(StaticVariables.lightgrey);
        custom1_theme_heading.setTextColor(StaticVariables.yellow);
        custom2_theme_heading.setBackgroundColor(StaticVariables.lightgrey);
        custom2_theme_heading.setTextColor(StaticVariables.yellow);

        switch (StaticVariables.mDisplayTheme) {
            case "dark":
            default:
                dark_theme_heading.setBackgroundColor(StaticVariables.yellow);
                dark_theme_heading.setTextColor(StaticVariables.black);
                break;

            case "light":
                light_theme_heading.setBackgroundColor(StaticVariables.yellow);
                light_theme_heading.setTextColor(StaticVariables.black);
                break;

            case "custom1":
                custom1_theme_heading.setBackgroundColor(StaticVariables.yellow);
                custom1_theme_heading.setTextColor(StaticVariables.black);
                break;

            case "custom2":
                if (custom2_theme_heading != null) {
                    custom2_theme_heading.setBackgroundColor(StaticVariables.yellow);
                    custom2_theme_heading.setTextColor(StaticVariables.black);
                }
                break;
        }

        // Set up the Tags
        dark_background.setTag("dark_lyricsBackgroundColor");
        dark_font.setTag("dark_lyricsTextColor");
        dark_verse.setTag("dark_lyricsVerseColor");
        dark_chorus.setTag("dark_lyricsChorusColor");
        dark_bridge.setTag("dark_lyricsBridgeColor");
        dark_comment.setTag("dark_lyricsCommentColor");
        dark_prechorus.setTag("dark_lyricsPreChorusColor");
        dark_tag.setTag("dark_lyricsTagColor");
        dark_chord.setTag("dark_lyricsChordsColor");
        dark_custom.setTag("dark_lyricsCustomColor");
        dark_presofont.setTag("dark_presoFontColor");
        dark_presoinfofont.setTag("dark_presoInfoFontColor");
        dark_presoalertfont.setTag("dark_presoAlertColor");
        dark_presoshadow.setTag("dark_presoShadowColor");
        dark_capo.setTag("dark_lyricsCapoColor");
        dark_metronome.setTag("dark_metronomeColor");
        dark_pagebuttons.setTag("dark_pageButtonsColor");
        dark_stickytext.setTag("dark_stickyTextColor");
        dark_stickybg.setTag("dark_stickyBackgroundColor");
        dark_extrainfobg.setTag("dark_extraInfoBgColor");
        dark_extrainfo.setTag("dark_extraInfoTextColor");

        light_background.setTag("light_lyricsBackgroundColor");
        light_font.setTag("light_lyricsTextColor");
        light_verse.setTag("light_lyricsVerseColor");
        light_chorus.setTag("light_lyricsChorusColor");
        light_bridge.setTag("light_lyricsBridgeColor");
        light_comment.setTag("light_lyricsCommentColor");
        light_prechorus.setTag("light_lyricsPreChorusColor");
        light_tag.setTag("light_lyricsTagColor");
        light_chord.setTag("light_lyricsChordsColor");
        light_custom.setTag("light_lyricsCustomColor");
        light_capo.setTag("light_lyricsCapoColor");
        light_presofont.setTag("light_presoFontColor");
        light_presoinfofont.setTag("light_presoInfoFontColor");
        light_presoalertfont.setTag("light_presoAlertColor");
        light_presoshadow.setTag("light_presoShadowColor");
        light_metronome.setTag("light_metronomeColor");
        light_pagebuttons.setTag("light_pageButtonsColor");
        light_stickytext.setTag("light_stickyTextColor");
        light_stickybg.setTag("light_stickyBackgroundColor");
        light_extrainfobg.setTag("light_extraInfoBgColor");
        light_extrainfo.setTag("light_extraInfoTextColor");

        custom1_background.setTag("custom1_lyricsBackgroundColor");
        custom1_font.setTag("custom1_lyricsTextColor");
        custom1_verse.setTag("custom1_lyricsVerseColor");
        custom1_chorus.setTag("custom1_lyricsChorusColor");
        custom1_bridge.setTag("custom1_lyricsBridgeColor");
        custom1_comment.setTag("custom1_lyricsCommentColor");
        custom1_prechorus.setTag("custom1_lyricsPreChorusColor");
        custom1_tag.setTag("custom1_lyricsTagColor");
        custom1_chord.setTag("custom1_lyricsChordsColor");
        custom1_custom.setTag("custom1_lyricsCustomColor");
        custom1_capo.setTag("custom1_lyricsCapoColor");
        custom1_presofont.setTag("custom1_presoFontColor");
        custom1_presoinfofont.setTag("custom1_presoInfoFontColor");
        custom1_presoalertfont.setTag("custom1_presoAlertColor");
        custom1_presoshadow.setTag("custom1_presoShadowColor");
        custom1_metronome.setTag("custom1_metronomeColor");
        custom1_pagebuttons.setTag("custom1_pageButtonsColor");
        custom1_stickytext.setTag("custom1_stickyTextColor");
        custom1_stickybg.setTag("custom1_stickyBackgroundColor");
        custom1_extrainfobg.setTag("custom1_extraInfoBgColor");
        custom1_extrainfo.setTag("custom1_extraInfoTextColor");

        custom2_background.setTag("custom2_lyricsBackgroundColor");
        custom2_font.setTag("custom2_lyricsTextColor");
        custom2_verse.setTag("custom2_lyricsVerseColor");
        custom2_chorus.setTag("custom2_lyricsChorusColor");
        custom2_bridge.setTag("custom2_lyricsBridgeColor");
        custom2_comment.setTag("custom2_lyricsCommentColor");
        custom2_prechorus.setTag("custom2_lyricsPreChorusColor");
        custom2_tag.setTag("custom2_lyricsTagColor");
        custom2_chord.setTag("custom2_lyricsChordsColor");
        custom2_custom.setTag("custom2_lyricsCustomColor");
        custom2_capo.setTag("custom2_lyricsCapoColor");
        custom2_presofont.setTag("custom2_presoFontColor");
        custom2_presoinfofont.setTag("custom2_presoInfoFontColor");
        custom2_presoalertfont.setTag("custom2_presoAlertColor");
        custom2_presoshadow.setTag("custom2_presoShadowColor");
        custom2_metronome.setTag("custom2_metronomeColor");
        custom2_pagebuttons.setTag("custom2_pageButtonsColor");
        custom2_stickytext.setTag("custom2_stickyTextColor");
        custom2_stickybg.setTag("custom2_stickyBackgroundColor");
        custom2_extrainfobg.setTag("custom2_extraInfoBgColor");
        custom2_extrainfo.setTag("custom2_extraInfoTextColor");

        // Set up the listeners
        dark_background.setOnClickListener(new ChangeColorListener("dark_lyricsBackgroundColor"));
        dark_font.setOnClickListener(new ChangeColorListener("dark_lyricsTextColor"));
        dark_verse.setOnClickListener(new ChangeColorListener("dark_lyricsVerseColor"));
        dark_chorus.setOnClickListener(new ChangeColorListener("dark_lyricsChorusColor"));
        dark_bridge.setOnClickListener(new ChangeColorListener("dark_lyricsBridgeColor"));
        dark_comment.setOnClickListener(new ChangeColorListener("dark_lyricsCommentColor"));
        dark_prechorus.setOnClickListener(new ChangeColorListener("dark_lyricsPreChorusColor"));
        dark_tag.setOnClickListener(new ChangeColorListener("dark_lyricsTagColor"));
        dark_chord.setOnClickListener(new ChangeColorListener("dark_lyricsChordsColor"));
        dark_custom.setOnClickListener(new ChangeColorListener("dark_lyricsCustomColor"));
        dark_capo.setOnClickListener(new ChangeColorListener("dark_lyricsCapoColor"));
        dark_presofont.setOnClickListener(new ChangeColorListener("dark_presoFontColor"));
        dark_presoinfofont.setOnClickListener(new ChangeColorListener("dark_presoInfoFontColor"));
        dark_presoalertfont.setOnClickListener(new ChangeColorListener("dark_presoAlertColor"));
        dark_presoshadow.setOnClickListener(new ChangeColorListener("dark_presoShadowColor"));
        dark_metronome.setOnClickListener(new ChangeColorListener("dark_metronomeColor"));
        dark_pagebuttons.setOnClickListener(new ChangeColorListener("dark_pageButtonsColor"));
        dark_stickytext.setOnClickListener(new ChangeColorListener("dark_stickyTextColor"));
        dark_stickybg.setOnClickListener(new ChangeColorListener("dark_stickyBackgroundColor"));
        dark_extrainfobg.setOnClickListener(new ChangeColorListener("dark_extraInfoBgColor"));
        dark_extrainfo.setOnClickListener(new ChangeColorListener("dark_extraInfoTextColor"));

        light_background.setOnClickListener(new ChangeColorListener("light_lyricsBackgroundColor"));
        light_font.setOnClickListener(new ChangeColorListener("light_lyricsTextColor"));
        light_verse.setOnClickListener(new ChangeColorListener("light_lyricsVerseColor"));
        light_chorus.setOnClickListener(new ChangeColorListener("light_lyricsChorusColor"));
        light_bridge.setOnClickListener(new ChangeColorListener("light_lyricsBridgeColor"));
        light_comment.setOnClickListener(new ChangeColorListener("light_lyricsCommentColor"));
        light_prechorus.setOnClickListener(new ChangeColorListener("light_lyricsPreChorusColor"));
        light_tag.setOnClickListener(new ChangeColorListener("light_lyricsTagColor"));
        light_chord.setOnClickListener(new ChangeColorListener("light_lyricsChordsColor"));
        light_custom.setOnClickListener(new ChangeColorListener("light_lyricsCustomColor"));
        light_capo.setOnClickListener(new ChangeColorListener("light_lyricsCapoColor"));
        light_presofont.setOnClickListener(new ChangeColorListener("light_presoFontColor"));
        light_presoinfofont.setOnClickListener(new ChangeColorListener("light_presoInfoFontColor"));
        light_presoalertfont.setOnClickListener(new ChangeColorListener("light_presoAlertColor"));
        light_presoshadow.setOnClickListener(new ChangeColorListener("light_presoShadowColor"));
        light_metronome.setOnClickListener(new ChangeColorListener("light_metronomeColor"));
        light_pagebuttons.setOnClickListener(new ChangeColorListener("light_pageButtonsColor"));
        light_stickytext.setOnClickListener(new ChangeColorListener("light_stickyTextColor"));
        light_stickybg.setOnClickListener(new ChangeColorListener("light_stickyBackgroundColor"));
        light_extrainfobg.setOnClickListener(new ChangeColorListener("light_extraInfoBgColor"));
        light_extrainfo.setOnClickListener(new ChangeColorListener("light_extraInfoTextColor"));

        custom1_background.setOnClickListener(new ChangeColorListener("custom1_lyricsBackgroundColor"));
        custom1_font.setOnClickListener(new ChangeColorListener("custom1_lyricsTextColor"));
        custom1_verse.setOnClickListener(new ChangeColorListener("custom1_lyricsVerseColor"));
        custom1_chorus.setOnClickListener(new ChangeColorListener("custom1_lyricsChorusColor"));
        custom1_bridge.setOnClickListener(new ChangeColorListener("custom1_lyricsBridgeColor"));
        custom1_comment.setOnClickListener(new ChangeColorListener("custom1_lyricsCommentColor"));
        custom1_prechorus.setOnClickListener(new ChangeColorListener("custom1_lyricsPreChorusColor"));
        custom1_tag.setOnClickListener(new ChangeColorListener("custom1_lyricsTagColor"));
        custom1_chord.setOnClickListener(new ChangeColorListener("custom1_lyricsChordsColor"));
        custom1_custom.setOnClickListener(new ChangeColorListener("custom1_lyricsCustomColor"));
        custom1_capo.setOnClickListener(new ChangeColorListener("custom1_lyricsCapoColor"));
        custom1_presofont.setOnClickListener(new ChangeColorListener("custom1_presoFontColor"));
        custom1_presoinfofont.setOnClickListener(new ChangeColorListener("custom1_presoInfoFontColor"));
        custom1_presoalertfont.setOnClickListener(new ChangeColorListener("custom1_presoAlertColor"));
        custom1_presoshadow.setOnClickListener(new ChangeColorListener("custom1_presoShadowColor"));
        custom1_metronome.setOnClickListener(new ChangeColorListener("custom1_metronomeColor"));
        custom1_pagebuttons.setOnClickListener(new ChangeColorListener("custom1_pageButtonsColor"));
        custom1_stickytext.setOnClickListener(new ChangeColorListener("custom1_stickyTextColor"));
        custom1_stickybg.setOnClickListener(new ChangeColorListener("custom1_stickyBackgroundColor"));
        custom1_extrainfobg.setOnClickListener(new ChangeColorListener("custom1_extraInfoBgColor"));
        custom1_extrainfo.setOnClickListener(new ChangeColorListener("custom1_extraInfoTextColor"));

        custom2_background.setOnClickListener(new ChangeColorListener("custom2_lyricsBackgroundColor"));
        custom2_font.setOnClickListener(new ChangeColorListener("custom2_lyricsTextColor"));
        custom2_verse.setOnClickListener(new ChangeColorListener("custom2_lyricsVerseColor"));
        custom2_chorus.setOnClickListener(new ChangeColorListener("custom2_lyricsChorusColor"));
        custom2_bridge.setOnClickListener(new ChangeColorListener("custom2_lyricsBridgeColor"));
        custom2_comment.setOnClickListener(new ChangeColorListener("custom2_lyricsCommentColor"));
        custom2_prechorus.setOnClickListener(new ChangeColorListener("custom2_lyricsPreChorusColor"));
        custom2_tag.setOnClickListener(new ChangeColorListener("custom2_lyricsTagColor"));
        custom2_chord.setOnClickListener(new ChangeColorListener("custom2_lyricsChordsColor"));
        custom2_custom.setOnClickListener(new ChangeColorListener("custom2_lyricsCustomColor"));
        custom2_capo.setOnClickListener(new ChangeColorListener("custom2_lyricsCapoColor"));
        custom2_presofont.setOnClickListener(new ChangeColorListener("custom2_presoFontColor"));
        custom2_presoinfofont.setOnClickListener(new ChangeColorListener("custom2_presoInfoFontColor"));
        custom2_presoalertfont.setOnClickListener(new ChangeColorListener("custom2_presoAlertColor"));
        custom2_presoshadow.setOnClickListener(new ChangeColorListener("custom2_presoShadowColor"));
        custom2_metronome.setOnClickListener(new ChangeColorListener("custom2_metronomeColor"));
        custom2_pagebuttons.setOnClickListener(new ChangeColorListener("custom2_pageButtonsColor"));
        custom2_stickytext.setOnClickListener(new ChangeColorListener("custom2_stickyTextColor"));
        custom2_stickybg.setOnClickListener(new ChangeColorListener("custom2_stickyBackgroundColor"));
        custom2_extrainfobg.setOnClickListener(new ChangeColorListener("custom2_extraInfoBgColor"));
        custom2_extrainfo.setOnClickListener(new ChangeColorListener("custom2_extraInfoTextColor"));
    }

    private void doThemeSwitch(String which) {
        setUpButtons();
        preferences.setMyPreferenceString(getActivity(), "appTheme", which);
        loadUpPreferences();
        if (mListener != null) {
            mListener.refreshAll();
            mListener.setUpPageButtonsColors();
        }
    }

    private void doClose() {
        if (mListener != null) {
            mListener.refreshAll();
            mListener.setUpPageButtonsColors();
        }
        dismiss();
    }

    private void setButtonColors() {
        // Set the buttons to the right colours

        dark_background.setBackgroundColor(dark_lyricsBackgroundColor);
        dark_font.setBackgroundColor(dark_lyricsTextColor);
        dark_capo.setBackgroundColor(dark_lyricsCapoColor);
        dark_verse.setBackgroundColor(dark_lyricsVerseColor);
        dark_chorus.setBackgroundColor(dark_lyricsChorusColor);
        dark_prechorus.setBackgroundColor(dark_lyricsPreChorusColor);
        dark_bridge.setBackgroundColor(dark_lyricsBridgeColor);
        dark_comment.setBackgroundColor(dark_lyricsCommentColor);
        dark_tag.setBackgroundColor(dark_lyricsTagColor);
        dark_chord.setBackgroundColor(dark_lyricsChordsColor);
        dark_custom.setBackgroundColor(dark_lyricsCustomColor);
        dark_presoalertfont.setBackgroundColor(dark_presoAlertColor);
        dark_presofont.setBackgroundColor(dark_presoFontColor);
        dark_presoinfofont.setBackgroundColor(dark_presoInfoColor);
        dark_presoshadow.setBackgroundColor(dark_presoShadowColor);
        dark_metronome.setBackgroundColor(dark_metronomeColor);
        dark_pagebuttons.setBackgroundColor(dark_pageButtonsColor);
        dark_stickytext.setBackgroundColor(dark_stickyTextColor);
        dark_stickybg.setBackgroundColor(dark_stickyBackgroundColor);
        dark_extrainfobg.setBackgroundColor(dark_extraInfoBgColor);
        dark_extrainfo.setBackgroundColor(dark_extraInfoTextColor);

        light_background.setBackgroundColor(light_lyricsBackgroundColor);
        light_font.setBackgroundColor(light_lyricsTextColor);
        light_capo.setBackgroundColor(light_lyricsCapoColor);
        light_verse.setBackgroundColor(light_lyricsVerseColor);
        light_chorus.setBackgroundColor(light_lyricsChorusColor);
        light_prechorus.setBackgroundColor(light_lyricsPreChorusColor);
        light_bridge.setBackgroundColor(light_lyricsBridgeColor);
        light_comment.setBackgroundColor(light_lyricsCommentColor);
        light_tag.setBackgroundColor(light_lyricsTagColor);
        light_chord.setBackgroundColor(light_lyricsChordsColor);
        light_custom.setBackgroundColor(light_lyricsCustomColor);
        light_metronome.setBackgroundColor(light_metronomeColor);
        light_pagebuttons.setBackgroundColor(light_pageButtonsColor);
        light_presoalertfont.setBackgroundColor(light_presoAlertColor);
        light_presofont.setBackgroundColor(light_presoFontColor);
        light_presoinfofont.setBackgroundColor(light_presoInfoColor);
        light_presoshadow.setBackgroundColor(light_presoShadowColor);
        light_stickytext.setBackgroundColor(light_stickyTextColor);
        light_stickybg.setBackgroundColor(light_stickyBackgroundColor);
        light_extrainfobg.setBackgroundColor(light_extraInfoBgColor);
        light_extrainfo.setBackgroundColor(light_extraInfoTextColor);

        custom1_background.setBackgroundColor(custom1_lyricsBackgroundColor);
        custom1_font.setBackgroundColor(custom1_lyricsTextColor);
        custom1_capo.setBackgroundColor(custom1_lyricsCapoColor);
        custom1_verse.setBackgroundColor(custom1_lyricsVerseColor);
        custom1_chorus.setBackgroundColor(custom1_lyricsChorusColor);
        custom1_prechorus.setBackgroundColor(custom1_lyricsPreChorusColor);
        custom1_bridge.setBackgroundColor(custom1_lyricsBridgeColor);
        custom1_comment.setBackgroundColor(custom1_lyricsCommentColor);
        custom1_tag.setBackgroundColor(custom1_lyricsTagColor);
        custom1_chord.setBackgroundColor(custom1_lyricsChordsColor);
        custom1_custom.setBackgroundColor(custom1_lyricsCustomColor);
        custom1_metronome.setBackgroundColor(custom1_metronomeColor);
        custom1_pagebuttons.setBackgroundColor(custom1_pageButtonsColor);
        custom1_presoalertfont.setBackgroundColor(custom1_presoAlertColor);
        custom1_presofont.setBackgroundColor(custom1_presoFontColor);
        custom1_presoinfofont.setBackgroundColor(custom1_presoInfoColor);
        custom1_presoshadow.setBackgroundColor(custom1_presoShadowColor);
        custom1_stickytext.setBackgroundColor(custom1_stickyTextColor);
        custom1_stickybg.setBackgroundColor(custom1_stickyBackgroundColor);
        custom1_extrainfobg.setBackgroundColor(custom1_extraInfoBgColor);
        custom1_extrainfo.setBackgroundColor(custom1_extraInfoTextColor);

        custom2_background.setBackgroundColor(custom2_lyricsBackgroundColor);
        custom2_font.setBackgroundColor(custom2_lyricsTextColor);
        custom2_capo.setBackgroundColor(custom2_lyricsCapoColor);
        custom2_verse.setBackgroundColor(custom2_lyricsVerseColor);
        custom2_chorus.setBackgroundColor(custom2_lyricsChorusColor);
        custom2_prechorus.setBackgroundColor(custom2_lyricsPreChorusColor);
        custom2_bridge.setBackgroundColor(custom2_lyricsBridgeColor);
        custom2_comment.setBackgroundColor(custom2_lyricsCommentColor);
        custom2_tag.setBackgroundColor(custom2_lyricsTagColor);
        custom2_chord.setBackgroundColor(custom2_lyricsChordsColor);
        custom2_custom.setBackgroundColor(custom2_lyricsCustomColor);
        custom2_metronome.setBackgroundColor(custom2_metronomeColor);
        custom2_pagebuttons.setBackgroundColor(custom2_pageButtonsColor);
        custom2_presoalertfont.setBackgroundColor(custom2_presoAlertColor);
        custom2_presofont.setBackgroundColor(custom2_presoFontColor);
        custom2_presoinfofont.setBackgroundColor(custom2_presoInfoColor);
        custom2_presoshadow.setBackgroundColor(custom2_presoShadowColor);
        custom2_stickytext.setBackgroundColor(custom2_stickyTextColor);
        custom2_stickybg.setBackgroundColor(custom2_stickyBackgroundColor);
        custom2_extrainfobg.setBackgroundColor(custom2_extraInfoBgColor);
        custom2_extrainfo.setBackgroundColor(custom2_extraInfoTextColor);

    }

    private void doDisplay(final View v) {
        Context c;
        try {
            if (getActivity() == null) {
                c = FullscreenActivity.mContext;
            } else {
                c = getActivity();
            }
            AmbilWarnaDialog dialog = new AmbilWarnaDialog(c, initialcolor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {

                    // Decide which one we're changing and save the appropriate value
                    preferences.setMyPreferenceInt(getActivity(), buttonClicked, color);

                    // Set the button colours up to match any changes
                    //setButtonColors();
                    v.setBackgroundColor(color);
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // User cancelled, do nothing
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    private void saveTheColor(Preferences preferences, String whichColor, int value) {
        preferences.setMyPreferenceInt(getActivity(), whichColor, value);
    }

    private void saveAllColors() {
        saveTheColor(preferences, "dark_lyricsBackgroundColor", dark_lyricsBackgroundColor);
        saveTheColor(preferences, "dark_lyricsBridgeColor", dark_lyricsBridgeColor);
        saveTheColor(preferences, "dark_lyricsCapoColor", dark_lyricsCapoColor);
        saveTheColor(preferences, "dark_lyricsChordsColor", dark_lyricsChordsColor);
        saveTheColor(preferences, "dark_lyricsChorusColor", dark_lyricsChorusColor);
        saveTheColor(preferences, "dark_lyricsCommentColor", dark_lyricsCommentColor);
        saveTheColor(preferences, "dark_lyricsCustomColor", dark_lyricsCustomColor);
        saveTheColor(preferences, "dark_lyricsPreChorusColor", dark_lyricsPreChorusColor);
        saveTheColor(preferences, "dark_lyricsTagColor", dark_lyricsTagColor);
        saveTheColor(preferences, "dark_lyricsTextColor", dark_lyricsTextColor);
        saveTheColor(preferences, "dark_lyricsVerseColor", dark_lyricsVerseColor);
        saveTheColor(preferences, "dark_presoFontColor", dark_presoFontColor);
        saveTheColor(preferences, "dark_presoShadowColor", dark_presoShadowColor);
        saveTheColor(preferences, "dark_presoInfoColor", dark_presoInfoColor);
        saveTheColor(preferences, "dark_presoAlertColor", dark_presoAlertColor);
        saveTheColor(preferences, "dark_metronomeColor", dark_metronomeColor);
        saveTheColor(preferences, "dark_pageButtonsColor", dark_pageButtonsColor);
        saveTheColor(preferences, "dark_stickyTextColor", dark_stickyTextColor);
        saveTheColor(preferences, "dark_stickyBackgroundColor", dark_stickyBackgroundColor);
        saveTheColor(preferences, "dark_extraInfoTextColor", dark_extraInfoTextColor);
        saveTheColor(preferences, "dark_extraInfoBgColor", dark_extraInfoBgColor);

        saveTheColor(preferences, "light_lyricsBackgroundColor", light_lyricsBackgroundColor);
        saveTheColor(preferences, "light_lyricsBridgeColor", light_lyricsBridgeColor);
        saveTheColor(preferences, "light_lyricsCapoColor", light_lyricsCapoColor);
        saveTheColor(preferences, "light_lyricsChordsColor", light_lyricsChordsColor);
        saveTheColor(preferences, "light_lyricsChorusColor", light_lyricsChorusColor);
        saveTheColor(preferences, "light_lyricsCommentColor", light_lyricsCommentColor);
        saveTheColor(preferences, "light_lyricsCustomColor", light_lyricsCustomColor);
        saveTheColor(preferences, "light_lyricsPreChorusColor", light_lyricsPreChorusColor);
        saveTheColor(preferences, "light_lyricsTagColor", light_lyricsTagColor);
        saveTheColor(preferences, "light_lyricsTextColor", light_lyricsTextColor);
        saveTheColor(preferences, "light_lyricsVerseColor", light_lyricsVerseColor);
        saveTheColor(preferences, "light_presoFontColor", light_presoFontColor);
        saveTheColor(preferences, "light_presoShadowColor", light_presoShadowColor);
        saveTheColor(preferences, "light_presoInfoColor", light_presoInfoColor);
        saveTheColor(preferences, "light_presoAlertColor", light_presoAlertColor);
        saveTheColor(preferences, "light_metronomeColor", light_metronomeColor);
        saveTheColor(preferences, "light_pageButtonsColor", light_pageButtonsColor);
        saveTheColor(preferences, "light_stickyTextColor", light_stickyTextColor);
        saveTheColor(preferences, "light_stickyBackgroundColor", light_stickyBackgroundColor);
        saveTheColor(preferences, "light_extraInfoTextColor", light_extraInfoTextColor);
        saveTheColor(preferences, "light_extraInfoBgColor", light_extraInfoBgColor);

        saveTheColor(preferences, "custom1_lyricsBackgroundColor", custom1_lyricsBackgroundColor);
        saveTheColor(preferences, "custom1_lyricsBridgeColor", custom1_lyricsBridgeColor);
        saveTheColor(preferences, "custom1_lyricsCapoColor", custom1_lyricsCapoColor);
        saveTheColor(preferences, "custom1_lyricsChordsColor", custom1_lyricsChordsColor);
        saveTheColor(preferences, "custom1_lyricsChorusColor", custom1_lyricsChorusColor);
        saveTheColor(preferences, "custom1_lyricsCommentColor", custom1_lyricsCommentColor);
        saveTheColor(preferences, "custom1_lyricsCustomColor", custom1_lyricsCustomColor);
        saveTheColor(preferences, "custom1_lyricsPreChorusColor", custom1_lyricsPreChorusColor);
        saveTheColor(preferences, "custom1_lyricsTagColor", custom1_lyricsTagColor);
        saveTheColor(preferences, "custom1_lyricsTextColor", custom1_lyricsTextColor);
        saveTheColor(preferences, "custom1_lyricsVerseColor", custom1_lyricsVerseColor);
        saveTheColor(preferences, "custom1_presoFontColor", custom1_presoFontColor);
        saveTheColor(preferences, "custom1_presoShadowColor", custom1_presoShadowColor);
        saveTheColor(preferences, "custom1_presoInfoColor", custom1_presoInfoColor);
        saveTheColor(preferences, "custom1_presoAlertColor", custom1_presoAlertColor);
        saveTheColor(preferences, "custom1_metronomeColor", custom1_metronomeColor);
        saveTheColor(preferences, "custom1_pageButtonsColor", custom1_pageButtonsColor);
        saveTheColor(preferences, "custom1_stickyTextColor", custom1_stickyTextColor);
        saveTheColor(preferences, "custom1_stickyBackgroundColor", custom1_stickyBackgroundColor);
        saveTheColor(preferences, "custom1_extraInfoTextColor", custom1_extraInfoTextColor);
        saveTheColor(preferences, "custom1_extraInfoBgColor", custom1_extraInfoBgColor);

        saveTheColor(preferences, "custom2_lyricsBackgroundColor", custom2_lyricsBackgroundColor);
        saveTheColor(preferences, "custom2_lyricsBridgeColor", custom2_lyricsBridgeColor);
        saveTheColor(preferences, "custom2_lyricsCapoColor", custom2_lyricsCapoColor);
        saveTheColor(preferences, "custom2_lyricsChordsColor", custom2_lyricsChordsColor);
        saveTheColor(preferences, "custom2_lyricsChorusColor", custom2_lyricsChorusColor);
        saveTheColor(preferences, "custom2_lyricsCommentColor", custom2_lyricsCommentColor);
        saveTheColor(preferences, "custom2_lyricsCustomColor", custom2_lyricsCustomColor);
        saveTheColor(preferences, "custom2_lyricsPreChorusColor", custom2_lyricsPreChorusColor);
        saveTheColor(preferences, "custom2_lyricsTagColor", custom2_lyricsTagColor);
        saveTheColor(preferences, "custom2_lyricsTextColor", custom2_lyricsTextColor);
        saveTheColor(preferences, "custom2_lyricsVerseColor", custom2_lyricsVerseColor);
        saveTheColor(preferences, "custom2_presoFontColor", custom2_presoFontColor);
        saveTheColor(preferences, "custom2_presoShadowColor", custom2_presoShadowColor);
        saveTheColor(preferences, "custom2_presoInfoColor", custom2_presoInfoColor);
        saveTheColor(preferences, "custom2_presoAlertColor", custom2_presoAlertColor);
        saveTheColor(preferences, "custom2_metronomeColor", custom2_metronomeColor);
        saveTheColor(preferences, "custom2_pageButtonsColor", custom2_pageButtonsColor);
        saveTheColor(preferences, "custom2_stickyTextColor", custom2_stickyTextColor);
        saveTheColor(preferences, "custom2_stickyBackgroundColor", custom2_stickyBackgroundColor);
        saveTheColor(preferences, "custom2_extraInfoTextColor", custom2_extraInfoTextColor);
        saveTheColor(preferences, "custom2_extraInfoBgColor", custom2_extraInfoBgColor);
    }

    public interface MyInterface {
        //void prepareView();
        void refreshAll();

        void setUpPageButtonsColors();
    }

    private class ChangeColorListener implements View.OnClickListener {
        ChangeColorListener(String which) {
            buttonClicked = which;
        }

        @Override
        public void onClick(View view) {
            buttonClicked = view.getTag().toString();
            switch (buttonClicked) {
                case "dark_lyricsBackgroundColor":
                    initialcolor = dark_lyricsBackgroundColor;
                    break;
                case "dark_lyricsTextColor":
                default:
                    initialcolor = dark_lyricsTextColor;
                    break;
                case "dark_lyricsVerseColor":
                    initialcolor = dark_lyricsVerseColor;
                    break;
                case "dark_lyricsChorusColor":
                    initialcolor = dark_lyricsChorusColor;
                    break;
                case "dark_lyricsBridgeColor":
                    initialcolor = dark_lyricsBridgeColor;
                    break;
                case "dark_lyricsCommentColor":
                    initialcolor = dark_lyricsCommentColor;
                    break;
                case "dark_lyricsPreChorusColor":
                    initialcolor = dark_lyricsPreChorusColor;
                    break;
                case "dark_lyricsTagColor":
                    initialcolor = dark_lyricsTagColor;
                    break;
                case "dark_lyricsChordsColor":
                    initialcolor = dark_lyricsChordsColor;
                    break;
                case "dark_lyricsCustomColor":
                    initialcolor = dark_lyricsCustomColor;
                    break;
                case "dark_lyricsCapoColor":
                    initialcolor = dark_lyricsCapoColor;
                    break;
                case "dark_metronomeColor":
                    initialcolor = dark_metronomeColor;
                    break;
                case "dark_pageButtonsColor":
                    initialcolor = dark_pageButtonsColor;
                    break;
                case "dark_presoAlertColor":
                    initialcolor = dark_presoAlertColor;
                    break;
                case "dark_presoFontColor":
                    initialcolor = dark_presoFontColor;
                    break;
                case "dark_presoInfoColor":
                    initialcolor = dark_presoInfoColor;
                    break;
                case "dark_presoShadowColor":
                    initialcolor = dark_presoShadowColor;
                    break;
                case "dark_stickyTextColor":
                    initialcolor = dark_stickyTextColor;
                    break;
                case "dark_stickyBackgroundColor":
                    initialcolor = dark_stickyBackgroundColor;
                    break;
                case "dark_extraInfoBgColor":
                    initialcolor = dark_extraInfoBgColor;
                    break;
                case "dark_extraInfoTextColor":
                    initialcolor = dark_extraInfoTextColor;
                    break;

                case "light_lyricsBackgroundColor":
                    initialcolor = light_lyricsBackgroundColor;
                    break;
                case "light_lyricsTextColor":
                    initialcolor = light_lyricsTextColor;
                    break;
                case "light_lyricsVerseColor":
                    initialcolor = light_lyricsVerseColor;
                    break;
                case "light_lyricsChorusColor":
                    initialcolor = light_lyricsChorusColor;
                    break;
                case "light_lyricsBridgeColor":
                    initialcolor = light_lyricsBridgeColor;
                    break;
                case "light_lyricsCommentColor":
                    initialcolor = light_lyricsCommentColor;
                    break;
                case "light_lyricsPreChorusColor":
                    initialcolor = light_lyricsPreChorusColor;
                    break;
                case "light_lyricsTagColor":
                    initialcolor = light_lyricsTagColor;
                    break;
                case "light_lyricsChordsColor":
                    initialcolor = light_lyricsChordsColor;
                    break;
                case "light_lyricsCustomColor":
                    initialcolor = light_lyricsCustomColor;
                    break;
                case "light_lyricsCapoColor":
                    initialcolor = light_lyricsCapoColor;
                    break;
                case "light_metronomeColor":
                    initialcolor = light_metronomeColor;
                    break;
                case "light_pageButtonsColor":
                    initialcolor = light_pageButtonsColor;
                    break;
                case "light_presoAlertColor":
                    initialcolor = light_presoAlertColor;
                    break;
                case "light_presoFontColor":
                    initialcolor = light_presoFontColor;
                    break;
                case "light_presoInfoColor":
                    initialcolor = light_presoInfoColor;
                    break;
                case "light_presoShadowColor":
                    initialcolor = light_presoShadowColor;
                    break;
                case "light_stickyTextColor":
                    initialcolor = light_stickyTextColor;
                    break;
                case "light_stickyBackgroundColor":
                    initialcolor = light_stickyBackgroundColor;
                    break;
                case "light_extraInfoBgColor":
                    initialcolor = light_extraInfoBgColor;
                    break;
                case "light_extraInfoTextColor":
                    initialcolor = light_extraInfoTextColor;
                    break;

                case "custom1_lyricsBackgroundColor":
                    initialcolor = custom1_lyricsBackgroundColor;
                    break;
                case "custom1_lyricsTextColor":
                    initialcolor = custom1_lyricsTextColor;
                    break;
                case "custom1_lyricsVerseColor":
                    initialcolor = custom1_lyricsVerseColor;
                    break;
                case "custom1_lyricsChorusColor":
                    initialcolor = custom1_lyricsChorusColor;
                    break;
                case "custom1_lyricsBridgeColor":
                    initialcolor = custom1_lyricsBridgeColor;
                    break;
                case "custom1_lyricsCommentColor":
                    initialcolor = custom1_lyricsCommentColor;
                    break;
                case "custom1_lyricsPreChorusColor":
                    initialcolor = custom1_lyricsPreChorusColor;
                    break;
                case "custom1_lyricsTagColor":
                    initialcolor = custom1_lyricsTagColor;
                    break;
                case "custom1_lyricsChordsColor":
                    initialcolor = custom1_lyricsChordsColor;
                    break;
                case "custom1_lyricsCustomColor":
                    initialcolor = custom1_lyricsCustomColor;
                    break;
                case "custom1_lyricsCapoColor":
                    initialcolor = custom1_lyricsCapoColor;
                    break;
                case "custom1_metronomeColor":
                    initialcolor = custom1_metronomeColor;
                    break;
                case "custom1_pageButtonsColor":
                    initialcolor = custom1_pageButtonsColor;
                    break;
                case "custom1_presoAlertColor":
                    initialcolor = custom1_presoAlertColor;
                    break;
                case "custom1_presoFontColor":
                    initialcolor = custom1_presoFontColor;
                    break;
                case "custom1_presoInfoColor":
                    initialcolor = custom1_presoInfoColor;
                    break;
                case "custom1_presoShadowColor":
                    initialcolor = custom1_presoShadowColor;
                    break;
                case "custom1_stickyTextColor":
                    initialcolor = custom1_stickyTextColor;
                    break;
                case "custom1_stickyBackgroundColor":
                    initialcolor = custom1_stickyBackgroundColor;
                    break;
                case "custom1_extraInfoBgColor":
                    initialcolor = custom1_extraInfoBgColor;
                    break;
                case "custom1_extraInfoTextColor":
                    initialcolor = custom1_extraInfoTextColor;
                    break;

                case "custom2_lyricsBackgroundColor":
                    initialcolor = custom2_lyricsBackgroundColor;
                    break;
                case "custom2_lyricsTextColor":
                    initialcolor = custom2_lyricsTextColor;
                    break;
                case "custom2_lyricsVerseColor":
                    initialcolor = custom2_lyricsVerseColor;
                    break;
                case "custom2_lyricsChorusColor":
                    initialcolor = custom2_lyricsChorusColor;
                    break;
                case "custom2_lyricsBridgeColor":
                    initialcolor = custom2_lyricsBridgeColor;
                    break;
                case "custom2_lyricsCommentColor":
                    initialcolor = custom2_lyricsCommentColor;
                    break;
                case "custom2_lyricsPreChorusColor":
                    initialcolor = custom2_lyricsPreChorusColor;
                    break;
                case "custom2_lyricsTagColor":
                    initialcolor = custom2_lyricsTagColor;
                    break;
                case "custom2_lyricsChordsColor":
                    initialcolor = custom2_lyricsChordsColor;
                    break;
                case "custom2_lyricsCustomColor":
                    initialcolor = custom2_lyricsCustomColor;
                    break;
                case "custom2_lyricsCapoColor":
                    initialcolor = custom2_lyricsCapoColor;
                    break;
                case "custom2_metronomeColor":
                    initialcolor = custom2_metronomeColor;
                    break;
                case "custom2_pageButtonsColor":
                    initialcolor = custom2_pageButtonsColor;
                    break;
                case "custom2_presoAlertColor":
                    initialcolor = custom2_presoAlertColor;
                    break;
                case "custom2_presoFontColor":
                    initialcolor = custom2_presoFontColor;
                    break;
                case "custom2_presoInfoColor":
                    initialcolor = custom2_presoInfoColor;
                    break;
                case "custom2_presoShadowColor":
                    initialcolor = custom2_presoShadowColor;
                    break;
                case "custom2_stickyTextColor":
                    initialcolor = custom2_stickyTextColor;
                    break;
                case "custom2_stickyBackgroundColor":
                    initialcolor = custom2_stickyBackgroundColor;
                    break;
                case "custom2_extraInfoBgColor":
                    initialcolor = custom2_extraInfoBgColor;
                    break;
                case "custom2_extraInfoTextColor":
                    initialcolor = custom2_extraInfoTextColor;
                    break;
            }
            doDisplay(view);
        }
    }
}

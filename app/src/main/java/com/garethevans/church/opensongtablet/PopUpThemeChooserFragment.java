package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class PopUpThemeChooserFragment extends DialogFragment {

    View dark_font;
    View dark_background;
    View dark_verse;
    View dark_chorus;
    View dark_prechorus;
    View dark_bridge;
    View dark_comment;
    View dark_tag;
    View dark_chord;
    View dark_custom;
    View dark_capo;
    View dark_presofont;
    View dark_presoinfofont;
    View dark_presoshadow;
    View dark_presoalertfont;
    View dark_metronome;
    View dark_pagebuttons;
    View dark_stickytext;
    View dark_stickybg;
    View dark_extrainfobg;
    View dark_extrainfo;
    View light_font;
    View light_background;
    View light_verse;
    View light_chorus;
    View light_prechorus;
    View light_bridge;
    View light_comment;
    View light_tag;
    View light_chord;
    View light_custom;
    View light_capo;
    View light_presofont;
    View light_presoinfofont;
    View light_presoalertfont;
    View light_presoshadow;
    View light_metronome;
    View light_pagebuttons;
    View light_stickytext;
    View light_stickybg;
    View light_extrainfobg;
    View light_extrainfo;
    View custom1_font;
    View custom1_background;
    View custom1_verse;
    View custom1_chorus;
    View custom1_prechorus;
    View custom1_bridge;
    View custom1_comment;
    View custom1_tag;
    View custom1_chord;
    View custom1_custom;
    View custom1_capo;
    View custom1_presofont;
    View custom1_presoinfofont;
    View custom1_presoalertfont;
    View custom1_presoshadow;
    View custom1_metronome;
    View custom1_pagebuttons;
    View custom1_stickytext;
    View custom1_stickybg;
    View custom1_extrainfobg;
    View custom1_extrainfo;
    View custom2_font;
    View custom2_background;
    View custom2_verse;
    View custom2_chorus;
    View custom2_prechorus;
    View custom2_bridge;
    View custom2_comment;
    View custom2_tag;
    View custom2_chord;
    View custom2_custom;
    View custom2_capo;
    View custom2_presofont;
    View custom2_presoinfofont;
    View custom2_presoalertfont;
    View custom2_presoshadow;
    View custom2_metronome;
    View custom2_pagebuttons;
    View custom2_stickytext;
    View custom2_stickybg;
    View custom2_extrainfobg;
    View custom2_extrainfo;
    Button resetcolours;
    TextView dark_theme_heading;
    TextView light_theme_heading;
    TextView custom1_theme_heading;
    TextView custom2_theme_heading;
    TextView stickynote_text;
    TextView stickynote_bg;
    int initialcolor = 0xff000000;
    int newcolor = 0xff000000;
    String buttonClicked = "";

    static PopUpThemeChooserFragment newInstance() {
        PopUpThemeChooserFragment frag;
        frag = new PopUpThemeChooserFragment();
        return frag;
    }

    public interface MyInterface {
        //void prepareView();
        void refreshAll();
        void setUpPageButtonsColors();
    }

    private MyInterface mListener;

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
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_themechooser, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.options_options_theme));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                doClose();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        // Initialise the views
        Preferences.loadPreferences();

        stickynote_text = V.findViewById(R.id.stickynote_text);
        stickynote_bg = V.findViewById(R.id.stickynote_bg);
        String s = getActivity().getResources().getString(R.string.stickynotes);
        stickynote_text.setText(s);
        s = s + "\n" + getActivity().getResources().getString(R.string.presoBackground);
        stickynote_bg.setText(s);

        // Extra info text
        String eit = getString(R.string.pad) + " / " + getString(R.string.autoscroll) + " / " + getString(R.string.capo_color);
        String eit2 = getString(R.string.pad) + " / " + getString(R.string.autoscroll) + " / " + getString(R.string.capo_color) +
                " (" + getString(R.string.presoBackground) + ")";
        TextView extrafont = V.findViewById(R.id.timerandcapo);
        TextView extrabg = V.findViewById(R.id.timerandcapobg);
        extrafont.setText(eit);
        extrabg.setText(eit2);

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
                getDefaultColours();
                Preferences.savePreferences();
                mListener.refreshAll();
                setButtonColors();
            }
        });

        // Listen for the theme changing
        dark_theme_heading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.mDisplayTheme = "Theme_Holo";
                doThemeSwitch();
            }
        });
        light_theme_heading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.mDisplayTheme = "Theme_Holo_Light";
                doThemeSwitch();
            }
        });
        custom1_theme_heading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.mDisplayTheme = "custom1";
                doThemeSwitch();
            }
        });
        custom2_theme_heading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.mDisplayTheme = "custom2";
                doThemeSwitch();
            }
        });

        // Run the script to set the button colours
        setButtonColors();

        // Set the appropriate theme button based on what is already set
        setUpButtons();

        return V;
    }

    public void setUpButtons() {
        dark_theme_heading.setBackgroundColor(0xff222222);
        dark_theme_heading.setTextColor(0xffffff00);
        light_theme_heading.setBackgroundColor(0xff222222);
        light_theme_heading.setTextColor(0xffffff00);
        custom1_theme_heading.setBackgroundColor(0xff222222);
        custom1_theme_heading.setTextColor(0xffffff00);
        custom2_theme_heading.setBackgroundColor(0xff222222);
        custom2_theme_heading.setTextColor(0xffffff00);

        Log.d("d","mDisplayTheme="+FullscreenActivity.mDisplayTheme);
        switch (FullscreenActivity.mDisplayTheme) {
            case "Theme_Holo":
            case "Theme.Holo":
            case "dark":
                dark_theme_heading.setBackgroundColor(0xffffff00);
                dark_theme_heading.setTextColor(0xff000000);
                break;

            case "Theme_Holo_Light":
            case "Theme.Holo.Light":
            case "Theme.Holo_Light":
            case "light":
                light_theme_heading.setBackgroundColor(0xffffff00);
                light_theme_heading.setTextColor(0xff000000);
                break;

            case "custom1":
                custom1_theme_heading.setBackgroundColor(0xffffff00);
                custom1_theme_heading.setTextColor(0xff000000);
                break;

            case "custom2":
                if (custom2_theme_heading != null) {
                    custom2_theme_heading.setBackgroundColor(0xffffff00);
                    custom2_theme_heading.setTextColor(0xff000000);
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
        dark_presoalertfont.setTag("dark_presoAlertFontColor");
        dark_presoshadow.setTag("dark_presoShadowFontColor");
        dark_capo.setTag("dark_lyricsCapoColor");
        dark_metronome.setTag("dark_metronome");
        dark_pagebuttons.setTag("dark_pagebuttons");
        dark_stickytext.setTag("dark_stickytext");
        dark_stickybg.setTag("dark_stickybg");
        dark_extrainfobg.setTag("dark_extrainfobg");
        dark_extrainfo.setTag("dark_extrainfo");

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
        light_presoalertfont.setTag("light_presoAlertFontColor");
        light_presoshadow.setTag("light_presoShadowFontColor");
        light_metronome.setTag("light_metronome");
        light_pagebuttons.setTag("light_pagebuttons");
        light_stickytext.setTag("light_stickytext");
        light_stickybg.setTag("light_stickybg");
        light_extrainfobg.setTag("light_extrainfobg");
        light_extrainfo.setTag("light_extrainfo");

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
        custom1_presoalertfont.setTag("custom1_presoAlertFontColor");
        custom1_presoshadow.setTag("custom1_presoShadowFontColor");
        custom1_metronome.setTag("custom1_metronome");
        custom1_pagebuttons.setTag("custom1_pagebuttons");
        custom1_stickytext.setTag("custom1_stickytext");
        custom1_stickybg.setTag("custom1_stickybg");
        custom1_extrainfobg.setTag("custom1_extrainfobg");
        custom1_extrainfo.setTag("custom1_extrainfo");

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
        custom2_presoalertfont.setTag("custom2_presoAlertFontColor");
        custom2_presoshadow.setTag("custom2_presoShadowFontColor");
        custom2_metronome.setTag("custom2_metronome");
        custom2_pagebuttons.setTag("custom2_pagebuttons");
        custom2_stickytext.setTag("custom2_stickytext");
        custom2_stickybg.setTag("custom2_stickybg");
        custom2_extrainfobg.setTag("custom2_extrainfobg");
        custom2_extrainfo.setTag("custom2_extrainfo");

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
        dark_presoalertfont.setOnClickListener(new ChangeColorListener("dark_presoAlertFontColor"));
        dark_presoshadow.setOnClickListener(new ChangeColorListener("dark_presoShadowFontColor"));
        dark_metronome.setOnClickListener(new ChangeColorListener("dark_metronome"));
        dark_pagebuttons.setOnClickListener(new ChangeColorListener("dark_pagebuttons"));
        dark_stickytext.setOnClickListener(new ChangeColorListener("dark_stickytext"));
        dark_stickybg.setOnClickListener(new ChangeColorListener("dark_stickybg"));
        dark_extrainfobg.setOnClickListener(new ChangeColorListener("dark_extrainfobg"));
        dark_extrainfo.setOnClickListener(new ChangeColorListener("dark_extrainfo"));

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
        light_presoalertfont.setOnClickListener(new ChangeColorListener("light_presoAlertFontColor"));
        light_presoshadow.setOnClickListener(new ChangeColorListener("light_presoShadowFontColor"));
        light_metronome.setOnClickListener(new ChangeColorListener("light_metronome"));
        light_pagebuttons.setOnClickListener(new ChangeColorListener("light_pagebuttons"));
        light_stickytext.setOnClickListener(new ChangeColorListener("light_stickytext"));
        light_stickybg.setOnClickListener(new ChangeColorListener("light_stickybg"));
        light_extrainfobg.setOnClickListener(new ChangeColorListener("light_extrainfobg"));
        light_extrainfo.setOnClickListener(new ChangeColorListener("light_extrainfo"));

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
        custom1_presoalertfont.setOnClickListener(new ChangeColorListener("custom1_presoAlertFontColor"));
        custom1_presoshadow.setOnClickListener(new ChangeColorListener("custom1_presoShadowFontColor"));
        custom1_metronome.setOnClickListener(new ChangeColorListener("custom1_metronome"));
        custom1_pagebuttons.setOnClickListener(new ChangeColorListener("custom1_pagebuttons"));
        custom1_stickytext.setOnClickListener(new ChangeColorListener("custom1_stickytext"));
        custom1_stickybg.setOnClickListener(new ChangeColorListener("custom1_stickybg"));
        custom1_extrainfobg.setOnClickListener(new ChangeColorListener("custom1_extrainfobg"));
        custom1_extrainfo.setOnClickListener(new ChangeColorListener("custom1_extrainfo"));

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
        custom2_presoalertfont.setOnClickListener(new ChangeColorListener("custom2_presoAlertFontColor"));
        custom2_presoshadow.setOnClickListener(new ChangeColorListener("custom2_presoShadowFontColor"));
        custom2_metronome.setOnClickListener(new ChangeColorListener("custom2_metronome"));
        custom2_pagebuttons.setOnClickListener(new ChangeColorListener("custom2_pagebuttons"));
        custom2_stickytext.setOnClickListener(new ChangeColorListener("custom2_stickytext"));
        custom2_stickybg.setOnClickListener(new ChangeColorListener("custom2_stickybg"));
        custom2_extrainfobg.setOnClickListener(new ChangeColorListener("custom2_extrainfobg"));
        custom2_extrainfo.setOnClickListener(new ChangeColorListener("custom2_extrainfo"));

    }

    public void doThemeSwitch() {
        setUpButtons();
        Preferences.savePreferences();
        SetUpColours.colours();
        if (mListener!=null) {
            mListener.refreshAll();
            mListener.setUpPageButtonsColors();
        }
    }

    public void doClose() {
        Preferences.savePreferences();
        SetUpColours.colours();
        if (mListener!=null) {
            mListener.refreshAll();
            mListener.setUpPageButtonsColors();
        }
        dismiss();
    }

    public void setButtonColors() {
        // Set the buttons to the right colours

        dark_background.setBackgroundColor(FullscreenActivity.dark_lyricsBackgroundColor);
        dark_font.setBackgroundColor(FullscreenActivity.dark_lyricsTextColor);
        dark_capo.setBackgroundColor(FullscreenActivity.dark_lyricsCapoColor);
        dark_verse.setBackgroundColor(FullscreenActivity.dark_lyricsVerseColor);
        dark_chorus.setBackgroundColor(FullscreenActivity.dark_lyricsChorusColor);
        dark_prechorus.setBackgroundColor(FullscreenActivity.dark_lyricsPreChorusColor);
        dark_bridge.setBackgroundColor(FullscreenActivity.dark_lyricsBridgeColor);
        dark_comment.setBackgroundColor(FullscreenActivity.dark_lyricsCommentColor);
        dark_tag.setBackgroundColor(FullscreenActivity.dark_lyricsTagColor);
        dark_chord.setBackgroundColor(FullscreenActivity.dark_lyricsChordsColor);
        dark_custom.setBackgroundColor(FullscreenActivity.dark_lyricsCustomColor);
        dark_presoalertfont.setBackgroundColor(FullscreenActivity.dark_presoAlertFont);
        dark_presofont.setBackgroundColor(FullscreenActivity.dark_presoFont);
        dark_presoinfofont.setBackgroundColor(FullscreenActivity.dark_presoInfoFont);
        dark_presoshadow.setBackgroundColor(FullscreenActivity.dark_presoShadow);
        dark_metronome.setBackgroundColor(FullscreenActivity.dark_metronome);
        dark_pagebuttons.setBackgroundColor(FullscreenActivity.dark_pagebuttons);
        dark_stickytext.setBackgroundColor(FullscreenActivity.dark_stickytext);
        dark_stickybg.setBackgroundColor(FullscreenActivity.dark_stickybg);
        dark_extrainfobg.setBackgroundColor(FullscreenActivity.dark_extrainfobg);
        dark_extrainfo.setBackgroundColor(FullscreenActivity.dark_extrainfo);

        light_background.setBackgroundColor(FullscreenActivity.light_lyricsBackgroundColor);
        light_font.setBackgroundColor(FullscreenActivity.light_lyricsTextColor);
        light_capo.setBackgroundColor(FullscreenActivity.light_lyricsCapoColor);
        light_verse.setBackgroundColor(FullscreenActivity.light_lyricsVerseColor);
        light_chorus.setBackgroundColor(FullscreenActivity.light_lyricsChorusColor);
        light_prechorus.setBackgroundColor(FullscreenActivity.light_lyricsPreChorusColor);
        light_bridge.setBackgroundColor(FullscreenActivity.light_lyricsBridgeColor);
        light_comment.setBackgroundColor(FullscreenActivity.light_lyricsCommentColor);
        light_tag.setBackgroundColor(FullscreenActivity.light_lyricsTagColor);
        light_chord.setBackgroundColor(FullscreenActivity.light_lyricsChordsColor);
        light_custom.setBackgroundColor(FullscreenActivity.light_lyricsCustomColor);
        light_metronome.setBackgroundColor(FullscreenActivity.light_metronome);
        light_pagebuttons.setBackgroundColor(FullscreenActivity.light_pagebuttons);
        light_presoalertfont.setBackgroundColor(FullscreenActivity.light_presoAlertFont);
        light_presofont.setBackgroundColor(FullscreenActivity.light_presoFont);
        light_presoinfofont.setBackgroundColor(FullscreenActivity.light_presoInfoFont);
        light_presoshadow.setBackgroundColor(FullscreenActivity.light_presoShadow);
        light_stickytext.setBackgroundColor(FullscreenActivity.light_stickytext);
        light_stickybg.setBackgroundColor(FullscreenActivity.light_stickybg);
        light_extrainfobg.setBackgroundColor(FullscreenActivity.light_extrainfobg);
        light_extrainfo.setBackgroundColor(FullscreenActivity.light_extrainfo);

        custom1_background.setBackgroundColor(FullscreenActivity.custom1_lyricsBackgroundColor);
        custom1_font.setBackgroundColor(FullscreenActivity.custom1_lyricsTextColor);
        custom1_capo.setBackgroundColor(FullscreenActivity.custom1_lyricsCapoColor);
        custom1_verse.setBackgroundColor(FullscreenActivity.custom1_lyricsVerseColor);
        custom1_chorus.setBackgroundColor(FullscreenActivity.custom1_lyricsChorusColor);
        custom1_prechorus.setBackgroundColor(FullscreenActivity.custom1_lyricsPreChorusColor);
        custom1_bridge.setBackgroundColor(FullscreenActivity.custom1_lyricsBridgeColor);
        custom1_comment.setBackgroundColor(FullscreenActivity.custom1_lyricsCommentColor);
        custom1_tag.setBackgroundColor(FullscreenActivity.custom1_lyricsTagColor);
        custom1_chord.setBackgroundColor(FullscreenActivity.custom1_lyricsChordsColor);
        custom1_custom.setBackgroundColor(FullscreenActivity.custom1_lyricsCustomColor);
        custom1_metronome.setBackgroundColor(FullscreenActivity.custom1_metronome);
        custom1_pagebuttons.setBackgroundColor(FullscreenActivity.custom1_pagebuttons);
        custom1_presoalertfont.setBackgroundColor(FullscreenActivity.custom1_presoAlertFont);
        custom1_presofont.setBackgroundColor(FullscreenActivity.custom1_presoFont);
        custom1_presoinfofont.setBackgroundColor(FullscreenActivity.custom1_presoInfoFont);
        custom1_presoshadow.setBackgroundColor(FullscreenActivity.custom1_presoShadow);
        custom1_stickytext.setBackgroundColor(FullscreenActivity.custom1_stickytext);
        custom1_stickybg.setBackgroundColor(FullscreenActivity.custom1_stickybg);
        custom1_extrainfobg.setBackgroundColor(FullscreenActivity.custom1_extrainfobg);
        custom1_extrainfo.setBackgroundColor(FullscreenActivity.custom1_extrainfo);

        custom2_background.setBackgroundColor(FullscreenActivity.custom2_lyricsBackgroundColor);
        custom2_font.setBackgroundColor(FullscreenActivity.custom2_lyricsTextColor);
        custom2_capo.setBackgroundColor(FullscreenActivity.custom2_lyricsCapoColor);
        custom2_verse.setBackgroundColor(FullscreenActivity.custom2_lyricsVerseColor);
        custom2_chorus.setBackgroundColor(FullscreenActivity.custom2_lyricsChorusColor);
        custom2_prechorus.setBackgroundColor(FullscreenActivity.custom2_lyricsPreChorusColor);
        custom2_bridge.setBackgroundColor(FullscreenActivity.custom2_lyricsBridgeColor);
        custom2_comment.setBackgroundColor(FullscreenActivity.custom2_lyricsCommentColor);
        custom2_tag.setBackgroundColor(FullscreenActivity.custom2_lyricsTagColor);
        custom2_chord.setBackgroundColor(FullscreenActivity.custom2_lyricsChordsColor);
        custom2_custom.setBackgroundColor(FullscreenActivity.custom2_lyricsCustomColor);
        custom2_metronome.setBackgroundColor(FullscreenActivity.custom2_metronome);
        custom2_pagebuttons.setBackgroundColor(FullscreenActivity.custom2_pagebuttons);
        custom2_presoalertfont.setBackgroundColor(FullscreenActivity.custom2_presoAlertFont);
        custom2_presofont.setBackgroundColor(FullscreenActivity.custom2_presoFont);
        custom2_presoinfofont.setBackgroundColor(FullscreenActivity.custom2_presoInfoFont);
        custom2_presoshadow.setBackgroundColor(FullscreenActivity.custom2_presoShadow);
        custom2_stickytext.setBackgroundColor(FullscreenActivity.custom2_stickytext);
        custom2_stickybg.setBackgroundColor(FullscreenActivity.custom2_stickybg);
        custom2_extrainfobg.setBackgroundColor(FullscreenActivity.custom2_extrainfobg);
        custom2_extrainfo.setBackgroundColor(FullscreenActivity.custom2_extrainfo);

        dark_presoalertfont.setBackgroundColor(FullscreenActivity.dark_presoAlertFont);
        dark_presofont.setBackgroundColor(FullscreenActivity.dark_presoFont);
        dark_presoinfofont.setBackgroundColor(FullscreenActivity.dark_presoInfoFont);
        dark_presoshadow.setBackgroundColor(FullscreenActivity.dark_presoShadow);
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
                    initialcolor = FullscreenActivity.dark_lyricsBackgroundColor;
                    break;
                case "dark_lyricsTextColor":
                    default:
                    initialcolor = FullscreenActivity.dark_lyricsTextColor;
                    break;
                case "dark_lyricsVerseColor":
                    initialcolor = FullscreenActivity.dark_lyricsVerseColor;
                    break;
                case "dark_lyricsChorusColor":
                    initialcolor = FullscreenActivity.dark_lyricsChorusColor;
                    break;
                case "dark_lyricsBridgeColor":
                    initialcolor = FullscreenActivity.dark_lyricsBridgeColor;
                    break;
                case "dark_lyricsCommentColor":
                    initialcolor = FullscreenActivity.dark_lyricsCommentColor;
                    break;
                case "dark_lyricsPreChorusColor":
                    initialcolor = FullscreenActivity.dark_lyricsPreChorusColor;
                    break;
                case "dark_lyricsTagColor":
                    initialcolor = FullscreenActivity.dark_lyricsTagColor;
                    break;
                case "dark_lyricsChordsColor":
                    initialcolor = FullscreenActivity.dark_lyricsChordsColor;
                    break;
                case "dark_lyricsCustomColor":
                    initialcolor = FullscreenActivity.dark_lyricsCustomColor;
                    break;
                case "dark_lyricsCapoColor":
                    initialcolor = FullscreenActivity.dark_lyricsCapoColor;
                    break;
                case "dark_metronome":
                    initialcolor = FullscreenActivity.dark_metronome;
                    break;
                case "dark_pagebuttons":
                    initialcolor = FullscreenActivity.dark_pagebuttons;
                    break;
                case "dark_presoAlertFontColor":
                    initialcolor = FullscreenActivity.dark_presoAlertFont;
                    break;
                case "dark_presoFontColor":
                    initialcolor = FullscreenActivity.dark_presoFont;
                    break;
                case "dark_presoInfoFontColor":
                    initialcolor = FullscreenActivity.dark_presoInfoFont;
                    break;
                case "dark_presoShadowFontColor":
                    initialcolor = FullscreenActivity.dark_presoShadow;
                    break;
                case "dark_stickytext":
                    initialcolor = FullscreenActivity.dark_stickytext;
                    break;
                case "dark_stickybg":
                    initialcolor = FullscreenActivity.dark_stickybg;
                    break;
                case "dark_extrainfobg":
                    initialcolor = FullscreenActivity.dark_extrainfobg;
                    break;
                case "dark_extrainfo":
                    initialcolor = FullscreenActivity.dark_extrainfo;
                    break;

                case "light_lyricsBackgroundColor":
                    initialcolor = FullscreenActivity.light_lyricsBackgroundColor;
                    break;
                case "light_lyricsTextColor":
                    initialcolor = FullscreenActivity.light_lyricsTextColor;
                    break;
                case "light_lyricsVerseColor":
                    initialcolor = FullscreenActivity.light_lyricsVerseColor;
                    break;
                case "light_lyricsChorusColor":
                    initialcolor = FullscreenActivity.light_lyricsChorusColor;
                    break;
                case "light_lyricsBridgeColor":
                    initialcolor = FullscreenActivity.light_lyricsBridgeColor;
                    break;
                case "light_lyricsCommentColor":
                    initialcolor = FullscreenActivity.light_lyricsCommentColor;
                    break;
                case "light_lyricsPreChorusColor":
                    initialcolor = FullscreenActivity.light_lyricsPreChorusColor;
                    break;
                case "light_lyricsTagColor":
                    initialcolor = FullscreenActivity.light_lyricsTagColor;
                    break;
                case "light_lyricsChordsColor":
                    initialcolor = FullscreenActivity.light_lyricsChordsColor;
                    break;
                case "light_lyricsCustomColor":
                    initialcolor = FullscreenActivity.light_lyricsCustomColor;
                    break;
                case "light_lyricsCapoColor":
                    initialcolor = FullscreenActivity.light_lyricsCapoColor;
                    break;
                case "light_metronome":
                    initialcolor = FullscreenActivity.light_metronome;
                    break;
                case "light_pagebuttons":
                    initialcolor = FullscreenActivity.light_pagebuttons;
                    break;
                case "light_presoAlertFontColor":
                    initialcolor = FullscreenActivity.light_presoAlertFont;
                    break;
                case "light_presoFontColor":
                    initialcolor = FullscreenActivity.light_presoFont;
                    break;
                case "light_presoInfoFontColor":
                    initialcolor = FullscreenActivity.light_presoInfoFont;
                    break;
                case "light_presoShadowFontColor":
                    initialcolor = FullscreenActivity.light_presoShadow;
                    break;
                case "light_stickytext":
                    initialcolor = FullscreenActivity.light_stickytext;
                    break;
                case "light_stickybg":
                    initialcolor = FullscreenActivity.light_stickybg;
                    break;
                case "light_extrainfobg":
                    initialcolor = FullscreenActivity.light_extrainfobg;
                    break;
                case "light_extrainfo":
                    initialcolor = FullscreenActivity.light_extrainfo;
                    break;

                case "custom1_lyricsBackgroundColor":
                    initialcolor = FullscreenActivity.custom1_lyricsBackgroundColor;
                    break;
                case "custom1_lyricsTextColor":
                    initialcolor = FullscreenActivity.custom1_lyricsTextColor;
                    break;
                case "custom1_lyricsVerseColor":
                    initialcolor = FullscreenActivity.custom1_lyricsVerseColor;
                    break;
                case "custom1_lyricsChorusColor":
                    initialcolor = FullscreenActivity.custom1_lyricsChorusColor;
                    break;
                case "custom1_lyricsBridgeColor":
                    initialcolor = FullscreenActivity.custom1_lyricsBridgeColor;
                    break;
                case "custom1_lyricsCommentColor":
                    initialcolor = FullscreenActivity.custom1_lyricsCommentColor;
                    break;
                case "custom1_lyricsPreChorusColor":
                    initialcolor = FullscreenActivity.custom1_lyricsPreChorusColor;
                    break;
                case "custom1_lyricsTagColor":
                    initialcolor = FullscreenActivity.custom1_lyricsTagColor;
                    break;
                case "custom1_lyricsChordsColor":
                    initialcolor = FullscreenActivity.custom1_lyricsChordsColor;
                    break;
                case "custom1_lyricsCustomColor":
                    initialcolor = FullscreenActivity.custom1_lyricsCustomColor;
                    break;
                case "custom1_lyricsCapoColor":
                    initialcolor = FullscreenActivity.custom1_lyricsCapoColor;
                    break;
                case "custom1_metronome":
                    initialcolor = FullscreenActivity.custom1_metronome;
                    break;
                case "custom1_pagebuttons":
                    initialcolor = FullscreenActivity.custom1_pagebuttons;
                    break;
                case "custom1_presoAlertFontColor":
                    initialcolor = FullscreenActivity.custom1_presoAlertFont;
                    break;
                case "custom1_presoFontColor":
                    initialcolor = FullscreenActivity.custom1_presoFont;
                    break;
                case "custom1_presoInfoFontColor":
                    initialcolor = FullscreenActivity.custom1_presoInfoFont;
                    break;
                case "custom1_presoShadowFontColor":
                    initialcolor = FullscreenActivity.custom1_presoShadow;
                    break;
                case "custom1_stickytext":
                    initialcolor = FullscreenActivity.custom1_stickytext;
                    break;
                case "custom1_stickybg":
                    initialcolor = FullscreenActivity.custom1_stickybg;
                    break;
                case "custom1_extrainfobg":
                    initialcolor = FullscreenActivity.custom1_extrainfobg;
                    break;
                case "custom1_extrainfo":
                    initialcolor = FullscreenActivity.custom1_extrainfo;
                    break;

                case "custom2_lyricsBackgroundColor":
                    initialcolor = FullscreenActivity.custom2_lyricsBackgroundColor;
                    break;
                case "custom2_lyricsTextColor":
                    initialcolor = FullscreenActivity.custom2_lyricsTextColor;
                    break;
                case "custom2_lyricsVerseColor":
                    initialcolor = FullscreenActivity.custom2_lyricsVerseColor;
                    break;
                case "custom2_lyricsChorusColor":
                    initialcolor = FullscreenActivity.custom2_lyricsChorusColor;
                    break;
                case "custom2_lyricsBridgeColor":
                    initialcolor = FullscreenActivity.custom2_lyricsBridgeColor;
                    break;
                case "custom2_lyricsCommentColor":
                    initialcolor = FullscreenActivity.custom2_lyricsCommentColor;
                    break;
                case "custom2_lyricsPreChorusColor":
                    initialcolor = FullscreenActivity.custom2_lyricsPreChorusColor;
                    break;
                case "custom2_lyricsTagColor":
                    initialcolor = FullscreenActivity.custom2_lyricsTagColor;
                    break;
                case "custom2_lyricsChordsColor":
                    initialcolor = FullscreenActivity.custom2_lyricsChordsColor;
                    break;
                case "custom2_lyricsCustomColor":
                    initialcolor = FullscreenActivity.custom2_lyricsCustomColor;
                    break;
                case "custom2_lyricsCapoColor":
                    initialcolor = FullscreenActivity.custom2_lyricsCapoColor;
                    break;
                case "custom2_metronome":
                    initialcolor = FullscreenActivity.custom2_metronome;
                    break;
                case "custom2_pagebuttons":
                    initialcolor = FullscreenActivity.custom2_pagebuttons;
                    break;
                case "custom2_presoAlertFontColor":
                    initialcolor = FullscreenActivity.custom2_presoAlertFont;
                    break;
                case "custom2_presoFontColor":
                    initialcolor = FullscreenActivity.custom2_presoFont;
                    break;
                case "custom2_presoInfoFontColor":
                    initialcolor = FullscreenActivity.custom2_presoInfoFont;
                    break;
                case "custom2_presoShadowFontColor":
                    initialcolor = FullscreenActivity.custom2_presoShadow;
                    break;
                case "custom2_stickytext":
                    initialcolor = FullscreenActivity.custom2_stickytext;
                    break;
                case "custom2_stickybg":
                    initialcolor = FullscreenActivity.custom2_stickybg;
                    break;
                case "custom2_extrainfobg":
                    initialcolor = FullscreenActivity.custom2_extrainfobg;
                    break;
                case "custom2_extrainfo":
                    initialcolor = FullscreenActivity.custom2_extrainfo;
                    break;

            }
            doDisplay();
        }
    }

    public void doDisplay() {
        Context c;
        try {
            if (getActivity()==null) {
                c = FullscreenActivity.mContext;
            } else {
                c = getActivity();
            }
            AmbilWarnaDialog dialog = new AmbilWarnaDialog(c, initialcolor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {

                    // Decide which one we're changing
                    switch (buttonClicked) {
                        case "dark_lyricsTextColor":
                            FullscreenActivity.dark_lyricsTextColor = color;
                            break;
                        case "dark_lyricsBackgroundColor":
                            FullscreenActivity.dark_lyricsBackgroundColor = color;
                            break;
                        case "dark_lyricsVerseColor":
                            FullscreenActivity.dark_lyricsVerseColor = color;
                            break;
                        case "dark_lyricsChorusColor":
                            FullscreenActivity.dark_lyricsChorusColor = color;
                            break;
                        case "dark_lyricsBridgeColor":
                            FullscreenActivity.dark_lyricsBridgeColor = color;
                            break;
                        case "dark_lyricsCommentColor":
                            FullscreenActivity.dark_lyricsCommentColor = color;
                            break;
                        case "dark_lyricsPreChorusColor":
                            FullscreenActivity.dark_lyricsPreChorusColor = color;
                            break;
                        case "dark_lyricsTagColor":
                            FullscreenActivity.dark_lyricsTagColor = color;
                            break;
                        case "dark_lyricsChordsColor":
                            FullscreenActivity.dark_lyricsChordsColor = color;
                            break;
                        case "dark_lyricsCustomColor":
                            FullscreenActivity.dark_lyricsCustomColor = color;
                            break;
                        case "dark_lyricsCapoColor":
                            FullscreenActivity.dark_lyricsCapoColor = color;
                            break;
                        case "dark_presoFontColor":
                            FullscreenActivity.dark_presoFont = color;
                            break;
                        case "dark_presoInfoFontColor":
                            FullscreenActivity.dark_presoInfoFont = color;
                            break;
                        case "dark_presoAlertFontColor":
                            FullscreenActivity.dark_presoAlertFont = color;
                            break;
                        case "dark_metronome":
                            FullscreenActivity.dark_metronome = color;
                            break;
                        case "dark_pagebuttons":
                            FullscreenActivity.dark_pagebuttons = color;
                            break;
                        case "dark_stickytext":
                            FullscreenActivity.dark_stickytext = color;
                            break;
                        case "dark_stickybg":
                            FullscreenActivity.dark_stickybg = color;
                            break;
                        case "dark_extrainfobg":
                            FullscreenActivity.dark_extrainfobg = color;
                            break;
                        case "dark_extrainfo":
                            FullscreenActivity.dark_extrainfo = color;
                            break;

                        case "light_lyricsTextColor":
                            FullscreenActivity.light_lyricsTextColor = color;
                            break;
                        case "light_lyricsBackgroundColor":
                            FullscreenActivity.light_lyricsBackgroundColor = color;
                            break;
                        case "light_lyricsVerseColor":
                            FullscreenActivity.light_lyricsVerseColor = color;
                            break;
                        case "light_lyricsChorusColor":
                            FullscreenActivity.light_lyricsChorusColor = color;
                            break;
                        case "light_lyricsBridgeColor":
                            FullscreenActivity.light_lyricsBridgeColor = color;
                            break;
                        case "light_lyricsCommentColor":
                            FullscreenActivity.light_lyricsCommentColor = color;
                            break;
                        case "light_lyricsPreChorusColor":
                            FullscreenActivity.light_lyricsPreChorusColor = color;
                            break;
                        case "light_lyricsTagColor":
                            FullscreenActivity.light_lyricsTagColor = color;
                            break;
                        case "light_lyricsChordsColor":
                            FullscreenActivity.light_lyricsChordsColor = color;
                            break;
                        case "light_lyricsCustomColor":
                            FullscreenActivity.light_lyricsCustomColor = color;
                            break;
                        case "light_lyricsCapoColor":
                            FullscreenActivity.light_lyricsCapoColor = color;
                            break;
                        case "light_presoFontColor":
                            FullscreenActivity.dark_presoFont = color;
                            break;
                        case "light_presoInfoFontColor":
                            FullscreenActivity.dark_presoInfoFont = color;
                            break;
                        case "light_presoAlertFontColor":
                            FullscreenActivity.dark_presoAlertFont = color;
                            break;
                        case "light_metronome":
                            FullscreenActivity.light_metronome = color;
                            break;
                        case "light_pagebuttons":
                            FullscreenActivity.light_pagebuttons = color;
                            break;
                        case "light_stickytext":
                            FullscreenActivity.light_stickytext = color;
                            break;
                        case "light_stickybg":
                            FullscreenActivity.light_stickybg = color;
                            break;
                        case "light_extrainfobg":
                            FullscreenActivity.light_extrainfobg = color;
                            break;
                        case "light_extrainfo":
                            FullscreenActivity.light_extrainfo = color;
                            break;

                        case "custom1_lyricsTextColor":
                            FullscreenActivity.custom1_lyricsTextColor = color;
                            break;
                        case "custom1_lyricsBackgroundColor":
                            FullscreenActivity.custom1_lyricsBackgroundColor = color;
                            break;
                        case "custom1_lyricsVerseColor":
                            FullscreenActivity.custom1_lyricsVerseColor = color;
                            break;
                        case "custom1_lyricsChorusColor":
                            FullscreenActivity.custom1_lyricsChorusColor = color;
                            break;
                        case "custom1_lyricsBridgeColor":
                            FullscreenActivity.custom1_lyricsBridgeColor = color;
                            break;
                        case "custom1_lyricsCommentColor":
                            FullscreenActivity.custom1_lyricsCommentColor = color;
                            break;
                        case "custom1_lyricsPreChorusColor":
                            FullscreenActivity.custom1_lyricsPreChorusColor = color;
                            break;
                        case "custom1_lyricsTagColor":
                            FullscreenActivity.custom1_lyricsTagColor = color;
                            break;
                        case "custom1_lyricsChordsColor":
                            FullscreenActivity.custom1_lyricsChordsColor = color;
                            break;
                        case "custom1_lyricsCustomColor":
                            FullscreenActivity.custom1_lyricsCustomColor = color;
                            break;
                        case "custom1_lyricsCapoColor":
                            FullscreenActivity.custom1_lyricsCapoColor = color;
                            break;
                        case "custom1_presoFontColor":
                            FullscreenActivity.dark_presoFont = color;
                            break;
                        case "custom1_presoInfoFontColor":
                            FullscreenActivity.dark_presoInfoFont = color;
                            break;
                        case "custom1_presoAlertFontColor":
                            FullscreenActivity.dark_presoAlertFont = color;
                            break;
                        case "custom1_metronome":
                            FullscreenActivity.custom1_metronome = color;
                            break;
                        case "custom1_pagebuttons":
                            FullscreenActivity.custom1_pagebuttons = color;
                            break;
                        case "custom1_stickytext":
                            FullscreenActivity.custom1_stickytext = color;
                            break;
                        case "custom1_stickybg":
                            FullscreenActivity.custom1_stickybg = color;
                            break;
                        case "custom1_extrainfobg":
                            FullscreenActivity.custom1_extrainfobg = color;
                            break;
                        case "custom1_extrainfo":
                            FullscreenActivity.custom1_extrainfo = color;
                            break;

                        case "custom2_lyricsTextColor":
                            FullscreenActivity.custom2_lyricsTextColor = color;
                            break;
                        case "custom2_lyricsBackgroundColor":
                            FullscreenActivity.custom2_lyricsBackgroundColor = color;
                            break;
                        case "custom2_lyricsVerseColor":
                            FullscreenActivity.custom2_lyricsVerseColor = color;
                            break;
                        case "custom2_lyricsChorusColor":
                            FullscreenActivity.custom2_lyricsChorusColor = color;
                            break;
                        case "custom2_lyricsBridgeColor":
                            FullscreenActivity.custom2_lyricsBridgeColor = color;
                            break;
                        case "custom2_lyricsCommentColor":
                            FullscreenActivity.custom2_lyricsCommentColor = color;
                            break;
                        case "custom2_lyricsPreChorusColor":
                            FullscreenActivity.custom2_lyricsPreChorusColor = color;
                            break;
                        case "custom2_lyricsTagColor":
                            FullscreenActivity.custom2_lyricsTagColor = color;
                            break;
                        case "custom2_lyricsChordsColor":
                            FullscreenActivity.custom2_lyricsChordsColor = color;
                            break;
                        case "custom2_lyricsCustomColor":
                            FullscreenActivity.custom2_lyricsCustomColor = color;
                            break;
                        case "custom2_lyricsCapoColor":
                            FullscreenActivity.custom2_lyricsCapoColor = color;
                            break;
                        case "custom2_presoFontColor":
                            FullscreenActivity.dark_presoFont = color;
                            break;
                        case "custom2_presoInfoFontColor":
                            FullscreenActivity.dark_presoInfoFont = color;
                            break;
                        case "custom2_presoAlertFontColor":
                            FullscreenActivity.dark_presoAlertFont = color;
                            break;
                        case "custom2_metronome":
                            FullscreenActivity.custom2_metronome = color;
                            break;
                        case "custom2_pagebuttons":
                            FullscreenActivity.custom2_pagebuttons = color;
                            break;
                        case "custom2_stickytext":
                            FullscreenActivity.custom2_stickytext = color;
                            break;
                        case "custom2_stickybg":
                            FullscreenActivity.custom2_stickybg = color;
                            break;
                        case "custom2_extrainfobg":
                            FullscreenActivity.custom2_extrainfobg = color;
                            break;
                        case "custom2_extrainfo":
                            FullscreenActivity.custom2_extrainfo = color;
                            break;

                    }
                    // Save the preferences and set the button colour
                    Preferences.savePreferences();
                    setButtonColors();
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // User cancelled, do nothing
                    newcolor = initialcolor;
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getDefaultColours() {
        FullscreenActivity.dark_lyricsTextColor = Preferences.default_dark_lyricsTextColor;
        FullscreenActivity.dark_lyricsVerseColor = Preferences.default_dark_lyricsVerseColor;
        FullscreenActivity.dark_lyricsCapoColor = Preferences.default_dark_lyricsCapoColor;
        FullscreenActivity.dark_lyricsBackgroundColor = Preferences.default_dark_lyricsBackgroundColor;
        FullscreenActivity.dark_lyricsChorusColor = Preferences.default_dark_lyricsChorusColor;
        FullscreenActivity.dark_lyricsPreChorusColor = Preferences.default_dark_lyricsPreChorusColor;
        FullscreenActivity.dark_lyricsBridgeColor = Preferences.default_dark_lyricsBridgeColor;
        FullscreenActivity.dark_lyricsTagColor = Preferences.default_dark_lyricsTagColor;
        FullscreenActivity.dark_lyricsCommentColor = Preferences.default_dark_lyricsCommentColor;
        FullscreenActivity.dark_lyricsChordsColor = Preferences.default_dark_lyricsChordsColor;
        FullscreenActivity.dark_lyricsCustomColor = Preferences.default_dark_lyricsCustomColor;
        FullscreenActivity.dark_metronome = Preferences.default_metronomeColor;
        FullscreenActivity.dark_pagebuttons = Preferences.default_pagebuttonsColor;
        FullscreenActivity.dark_presoAlertFont = Preferences.default_dark_presoAlertColor;
        FullscreenActivity.dark_presoFont = Preferences.default_dark_presoFontColor;
        FullscreenActivity.dark_presoInfoFont = Preferences.default_dark_presoInfoFontColor;
        FullscreenActivity.dark_presoShadow = Preferences.default_dark_presoShadowFontColor;
        FullscreenActivity.dark_stickytext = Preferences.default_stickyNotes;
        FullscreenActivity.dark_stickybg = Preferences.default_stickyNotesBG;
        FullscreenActivity.dark_extrainfobg = Preferences.default_extrainfoBG;
        FullscreenActivity.dark_extrainfo = Preferences.default_extrainfo;

        FullscreenActivity.light_lyricsTextColor = Preferences.default_light_lyricsTextColor;
        FullscreenActivity.light_lyricsVerseColor = Preferences.default_light_lyricsVerseColor;
        FullscreenActivity.light_lyricsCapoColor = Preferences.default_light_lyricsCapoColor;
        FullscreenActivity.light_lyricsBackgroundColor = Preferences.default_light_lyricsBackgroundColor;
        FullscreenActivity.light_lyricsChorusColor = Preferences.default_light_lyricsChorusColor;
        FullscreenActivity.light_lyricsPreChorusColor = Preferences.default_light_lyricsPreChorusColor;
        FullscreenActivity.light_lyricsBridgeColor = Preferences.default_light_lyricsBridgeColor;
        FullscreenActivity.light_lyricsTagColor = Preferences.default_light_lyricsTagColor;
        FullscreenActivity.light_lyricsCommentColor = Preferences.default_light_lyricsCommentColor;
        FullscreenActivity.light_lyricsChordsColor = Preferences.default_light_lyricsChordsColor;
        FullscreenActivity.light_lyricsCustomColor = Preferences.default_light_lyricsCustomColor;
        FullscreenActivity.light_metronome = Preferences.default_metronomeColor;
        FullscreenActivity.light_pagebuttons = Preferences.default_pagebuttonsColor;
        FullscreenActivity.light_presoAlertFont = Preferences.default_dark_presoAlertColor;
        FullscreenActivity.light_presoFont = Preferences.default_dark_presoFontColor;
        FullscreenActivity.light_presoInfoFont = Preferences.default_dark_presoInfoFontColor;
        FullscreenActivity.light_presoShadow = Preferences.default_dark_presoShadowFontColor;
        FullscreenActivity.light_stickytext = Preferences.default_stickyNotes;
        FullscreenActivity.light_stickybg = Preferences.default_stickyNotesBG;
        FullscreenActivity.light_extrainfobg = Preferences.default_extrainfoBG;
        FullscreenActivity.light_extrainfo = Preferences.default_extrainfo;

        FullscreenActivity.custom1_lyricsTextColor = Preferences.default_dark_lyricsTextColor;
        FullscreenActivity.custom1_lyricsVerseColor = Preferences.default_dark_lyricsVerseColor;
        FullscreenActivity.custom1_lyricsCapoColor = Preferences.default_dark_lyricsCapoColor;
        FullscreenActivity.custom1_lyricsBackgroundColor = Preferences.default_dark_lyricsBackgroundColor;
        FullscreenActivity.custom1_lyricsChorusColor = Preferences.default_dark_lyricsBackgroundColor;
        FullscreenActivity.custom1_lyricsPreChorusColor = Preferences.default_dark_lyricsBackgroundColor;
        FullscreenActivity.custom1_lyricsBridgeColor = Preferences.default_dark_lyricsBackgroundColor;
        FullscreenActivity.custom1_lyricsTagColor = Preferences.default_dark_lyricsBackgroundColor;
        FullscreenActivity.custom1_lyricsCommentColor = Preferences.default_dark_lyricsBackgroundColor;
        FullscreenActivity.custom1_lyricsChordsColor = Preferences.default_dark_lyricsChordsColor;
        FullscreenActivity.custom1_lyricsCustomColor = Preferences.default_dark_lyricsBackgroundColor;
        FullscreenActivity.custom1_metronome = Preferences.default_metronomeColor;
        FullscreenActivity.custom1_pagebuttons = Preferences.default_pagebuttonsColor;
        FullscreenActivity.custom1_presoAlertFont = Preferences.default_dark_presoAlertColor;
        FullscreenActivity.custom1_presoFont = Preferences.default_dark_presoFontColor;
        FullscreenActivity.custom1_presoInfoFont = Preferences.default_dark_presoInfoFontColor;
        FullscreenActivity.custom1_presoShadow = Preferences.default_dark_presoShadowFontColor;
        FullscreenActivity.custom1_stickytext = Preferences.default_stickyNotes;
        FullscreenActivity.custom1_stickybg = Preferences.default_stickyNotesBG;
        FullscreenActivity.custom1_extrainfobg = Preferences.default_extrainfoBG;
        FullscreenActivity.custom1_extrainfo = Preferences.default_extrainfo;

        FullscreenActivity.custom2_lyricsTextColor = Preferences.default_light_lyricsTextColor;
        FullscreenActivity.custom2_lyricsVerseColor = Preferences.default_light_lyricsVerseColor;
        FullscreenActivity.custom2_lyricsCapoColor = Preferences.default_light_lyricsCapoColor;
        FullscreenActivity.custom2_lyricsBackgroundColor = Preferences.default_light_lyricsBackgroundColor;
        FullscreenActivity.custom2_lyricsChorusColor = Preferences.default_light_lyricsBackgroundColor;
        FullscreenActivity.custom2_lyricsPreChorusColor = Preferences.default_light_lyricsBackgroundColor;
        FullscreenActivity.custom2_lyricsBridgeColor = Preferences.default_light_lyricsBackgroundColor;
        FullscreenActivity.custom2_lyricsTagColor = Preferences.default_light_lyricsBackgroundColor;
        FullscreenActivity.custom2_lyricsCommentColor = Preferences.default_light_lyricsBackgroundColor;
        FullscreenActivity.custom2_lyricsChordsColor = Preferences.default_light_lyricsChordsColor;
        FullscreenActivity.custom2_lyricsCustomColor = Preferences.default_light_lyricsBackgroundColor;
        FullscreenActivity.custom2_metronome = Preferences.default_metronomeColor;
        FullscreenActivity.custom2_pagebuttons = Preferences.default_pagebuttonsColor;
        FullscreenActivity.custom2_presoAlertFont = Preferences.default_dark_presoAlertColor;
        FullscreenActivity.custom2_presoFont = Preferences.default_dark_presoFontColor;
        FullscreenActivity.custom2_presoInfoFont = Preferences.default_dark_presoInfoFontColor;
        FullscreenActivity.custom2_presoShadow = Preferences.default_dark_presoShadowFontColor;
        FullscreenActivity.custom2_stickytext = Preferences.default_stickyNotes;
        FullscreenActivity.custom2_stickybg = Preferences.default_stickyNotesBG;
        FullscreenActivity.custom2_extrainfobg = Preferences.default_extrainfoBG;
        FullscreenActivity.custom2_extrainfo = Preferences.default_extrainfo;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}

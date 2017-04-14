package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
    View dark_metronome;
    View dark_pagebuttons;
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
    View light_metronome;
    View light_pagebuttons;
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
    View custom1_metronome;
    View custom1_pagebuttons;
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
    View custom2_metronome;
    View custom2_pagebuttons;
    Button resetcolours;
    TextView dark_theme_heading;
    TextView light_theme_heading;
    TextView custom1_theme_heading;
    TextView custom2_theme_heading;
    int initialcolor;
    int newcolor;
    String buttonClicked = "";

    static PopUpThemeChooserFragment newInstance() {
        PopUpThemeChooserFragment frag;
        frag = new PopUpThemeChooserFragment();
        return frag;
    }

    public interface MyInterface {
        void prepareView();
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
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            title.setText(getActivity().getResources().getString(R.string.options_options_theme));
            FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doClose();
                }
            });
            FloatingActionButton saveMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.saveMe);
            saveMe.setVisibility(View.GONE);
        } else {
            getDialog().setTitle(getActivity().getResources().getString(R.string.options_options_theme));
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
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_themechooser, container, false);

        // Initialise the views
        Preferences.loadPreferences();

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
        dark_metronome = V.findViewById(R.id.metronome_dark);
        dark_pagebuttons = V.findViewById(R.id.pagebuttons_dark);

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
        light_metronome = V.findViewById(R.id.metronome_light);
        light_pagebuttons = V.findViewById(R.id.pagebuttons_light);

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
        custom1_metronome = V.findViewById(R.id.metronome_custom1);
        custom1_pagebuttons = V.findViewById(R.id.pagebuttons_custom1);

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
        custom2_metronome = V.findViewById(R.id.metronome_custom2);
        custom2_pagebuttons = V.findViewById(R.id.pagebuttons_custom2);

        dark_theme_heading = (TextView) V.findViewById(R.id.dark_theme_heading);
        light_theme_heading = (TextView) V.findViewById(R.id.light_theme_heading);
        custom1_theme_heading = (TextView) V.findViewById(R.id.custom1_theme_heading);
        custom2_theme_heading = (TextView) V.findViewById(R.id.custom2_theme_heading);

        // Set the appropriate theme button based on what is already set
        setUpButtons();

        resetcolours = (Button) V.findViewById(R.id.resetcolours);
        resetcolours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDefaultColours();
                Preferences.savePreferences();
                mListener.prepareView();
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

        switch (FullscreenActivity.mDisplayTheme) {
            case "Theme_Holo":
                dark_theme_heading.setBackgroundColor(0xffffff00);
                dark_theme_heading.setTextColor(0xff000000);
                break;

            case "Theme_Holo_Light":
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
        dark_capo.setTag("dark_lyricsCapoColor");
        dark_metronome.setTag("dark_metronome");
        dark_pagebuttons.setTag("dark_pagebuttons");

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
        light_metronome.setTag("light_metronome");
        light_pagebuttons.setTag("light_pagebuttons");

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
        custom1_metronome.setTag("custom1_metronome");
        custom1_pagebuttons.setTag("custom1_pagebuttons");

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
        custom2_metronome.setTag("custom2_metronome");
        custom2_pagebuttons.setTag("custom2_pagebuttons");

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
        dark_metronome.setOnClickListener(new ChangeColorListener("dark_metronome"));
        dark_pagebuttons.setOnClickListener(new ChangeColorListener("dark_pagebuttons"));

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
        light_metronome.setOnClickListener(new ChangeColorListener("light_metronome"));
        light_pagebuttons.setOnClickListener(new ChangeColorListener("light_pagebuttons"));

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
        custom1_metronome.setOnClickListener(new ChangeColorListener("custom1_metronome"));
        custom1_pagebuttons.setOnClickListener(new ChangeColorListener("custom1_pagebuttons"));

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
        custom2_metronome.setOnClickListener(new ChangeColorListener("custom2_metronome"));
        custom2_pagebuttons.setOnClickListener(new ChangeColorListener("custom2_pagebuttons"));
    }

    public void doThemeSwitch() {
        setUpButtons();
        Preferences.savePreferences();
        SetUpColours.colours();
        if (mListener!=null) {
            mListener.prepareView();
            mListener.setUpPageButtonsColors();
        }
    }

    public void doClose() {
        Preferences.savePreferences();
        SetUpColours.colours();
        if (mListener!=null) {
            mListener.prepareView();
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
        dark_metronome.setBackgroundColor(FullscreenActivity.dark_metronome);
        dark_pagebuttons.setBackgroundColor(FullscreenActivity.dark_pagebuttons);
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
            }
            doDisplay();
        }
    }

    public void doDisplay() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(getActivity(), initialcolor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
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
                    case "dark_metronome":
                        FullscreenActivity.dark_metronome = color;
                        break;
                    case "dark_pagebuttons":
                        FullscreenActivity.dark_pagebuttons = color;
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
                    case "light_metronome":
                        FullscreenActivity.light_metronome = color;
                        break;
                    case "light_pagebuttons":
                        FullscreenActivity.light_pagebuttons = color;
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
                    case "custom1_metronome":
                        FullscreenActivity.custom1_metronome = color;
                        break;
                    case "custom1_pagebuttons":
                        FullscreenActivity.custom1_pagebuttons = color;
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
                    case "custom2_metronome":
                        FullscreenActivity.custom2_metronome = color;
                        break;
                    case "custom2_pagebuttons":
                        FullscreenActivity.custom2_pagebuttons = color;
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
    }

    public void getDefaultColours() {
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
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}

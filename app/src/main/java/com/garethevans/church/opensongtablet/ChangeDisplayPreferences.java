package com.garethevans.church.opensongtablet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ChangeDisplayPreferences extends AppCompatActivity {

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
    Button goback;

    public static TextView dark_theme_heading;
    public static TextView light_theme_heading;
    public static TextView custom1_theme_heading;
    public static TextView custom2_theme_heading;

	int initialcolor;
	int newcolor;
	String whichone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the user preferences
		Preferences.loadPreferences();
		// Set the screen and title
		//setContentView(R.layout.custom_display);
		setContentView(R.layout.popup_switchtheme);

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();

        TextView title = (TextView) findViewById(R.id.songandauthor);
        if (ab != null && title != null) {
            ab.setTitle("");
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setDisplayShowTitleEnabled(false);
            //title.setText(getResources().getString(R.string.colorchooser));
			title.setText(getString(R.string.options_options_theme));
        }

		// Define the buttons
		dark_background = findViewById(R.id.page_dark);
		dark_font = findViewById(R.id.lyrics_dark);
		dark_verse = findViewById(R.id.verse_dark);
		dark_chorus = findViewById(R.id.chorus_dark);
		dark_prechorus = findViewById(R.id.prechorus_dark);
		dark_bridge = findViewById(R.id.bridge_dark);
		dark_comment = findViewById(R.id.comment_dark);
		dark_tag = findViewById(R.id.tag_dark);
		dark_chord = findViewById(R.id.chords_dark);
		dark_custom = findViewById(R.id.custom_dark);
		dark_capo = findViewById(R.id.capo_dark);
		dark_metronome = findViewById(R.id.metronome_dark);

		light_background = findViewById(R.id.page_light);
		light_font = findViewById(R.id.lyrics_light);
		light_verse = findViewById(R.id.verse_light);
		light_chorus = findViewById(R.id.chorus_light);
		light_prechorus = findViewById(R.id.prechorus_light);
		light_bridge = findViewById(R.id.bridge_light);
		light_comment = findViewById(R.id.comment_light);
		light_tag = findViewById(R.id.tag_light);
		light_chord = findViewById(R.id.chords_light);
		light_custom = findViewById(R.id.custom_light);
		light_capo = findViewById(R.id.capo_light);
		light_metronome = findViewById(R.id.metronome_light);
		custom1_background = findViewById(R.id.page_custom1);
		custom1_font = findViewById(R.id.lyrics_custom1);
		custom1_verse = findViewById(R.id.verse_custom1);
		custom1_chorus = findViewById(R.id.chorus_custom1);
		custom1_prechorus = findViewById(R.id.prechorus_custom1);
		custom1_bridge = findViewById(R.id.bridge_custom1);
		custom1_comment = findViewById(R.id.comment_custom1);
		custom1_tag = findViewById(R.id.tag_custom1);
		custom1_chord = findViewById(R.id.chords_custom1);
		custom1_custom = findViewById(R.id.custom_custom1);
		custom1_capo = findViewById(R.id.capo_custom1);
		custom1_metronome = findViewById(R.id.metronome_custom1);
		custom2_background = findViewById(R.id.page_custom2);
		custom2_font = findViewById(R.id.lyrics_custom2);
		custom2_verse = findViewById(R.id.verse_custom2);
		custom2_chorus = findViewById(R.id.chorus_custom2);
		custom2_prechorus = findViewById(R.id.prechorus_custom2);
		custom2_bridge = findViewById(R.id.bridge_custom2);
		custom2_comment = findViewById(R.id.comment_custom2);
		custom2_tag = findViewById(R.id.tag_custom2);
		custom2_chord = findViewById(R.id.chords_custom2);
		custom2_custom = findViewById(R.id.custom_custom2);
		custom2_capo = findViewById(R.id.capo_custom2);
		custom2_metronome = findViewById(R.id.metronome_custom2);

        dark_theme_heading = (TextView) findViewById(R.id.dark_theme_heading);
        light_theme_heading = (TextView) findViewById(R.id.light_theme_heading);
        custom1_theme_heading = (TextView) findViewById(R.id.custom1_theme_heading);
        custom2_theme_heading = (TextView) findViewById(R.id.custom2_theme_heading);

        // Set the appropriate theme button based on what is already set
        setUpButtons();

        goback = (Button) findViewById(R.id.closebutton);

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
    }

    public void doThemeSwitch() {
        setUpButtons();
        Preferences.savePreferences();
        SetUpColours.colours();
    }

	@Override
	public void onBackPressed() {
		Intent viewsong = new Intent(this, FullscreenActivity.class);
		startActivity(viewsong);
		finish();
	}

	public void doDisplay(View view) {
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(ChangeDisplayPreferences.this, initialcolor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {

				// Decide which one we're changing
				switch (whichone) {
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

    public void light_metronome_background(View view) {
        // Set global initialcolor
        initialcolor = FullscreenActivity.light_metronome;
        whichone = "light_metronome";
        doDisplay(view);
    }

    public void dark_metronome_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.dark_metronome;
		whichone = "dark_metronome";
		doDisplay(view);
	}

	public void custom1_metronome_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom1_metronome;
		whichone = "custom1_metronome";
		doDisplay(view);
	}

	public void custom2_metronome_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom2_metronome;
		whichone = "custom2_metronome";
		doDisplay(view);
	}

    public void light_background(View view) {
        // Set global initialcolor
        initialcolor = FullscreenActivity.light_lyricsBackgroundColor;
        whichone = "light_lyricsBackgroundColor";
        doDisplay(view);
    }

    public void dark_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.dark_lyricsBackgroundColor;
		whichone = "dark_lyricsBackgroundColor";
		doDisplay(view);
	}

	public void custom1_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom1_lyricsBackgroundColor;
		whichone = "custom1_lyricsBackgroundColor";
		doDisplay(view);
	}

	public void custom2_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom2_lyricsBackgroundColor;
		whichone = "custom2_lyricsBackgroundColor";
		doDisplay(view);
	}

    public void light_lyrics_font(View view) {
        // Set global initialcolor
        initialcolor = FullscreenActivity.light_lyricsTextColor;
        whichone = "light_lyricsTextColor";
        doDisplay(view);
    }

    public void dark_lyrics_font(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.dark_lyricsTextColor;
		whichone = "dark_lyricsTextColor";
		doDisplay(view);
	}

	public void custom1_lyrics_font(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom1_lyricsTextColor;
		whichone = "custom1_lyricsTextColor";
		doDisplay(view);
	}

	public void custom2_lyrics_font(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom2_lyricsTextColor;
		whichone = "custom2_lyricsTextColor";
		doDisplay(view);
	}

    public void light_chord_font(View view) {
        // Set global initialcolor
        initialcolor = FullscreenActivity.light_lyricsChordsColor;
        whichone = "light_lyricsChordsColor";
        doDisplay(view);
    }

    public void dark_chord_font(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.dark_lyricsChordsColor;
		whichone = "dark_lyricsChordsColor";
		doDisplay(view);
	}

	public void custom1_chord_font(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom1_lyricsChordsColor;
		whichone = "custom1_lyricsChordsColor";
		doDisplay(view);
	}

	public void custom2_chord_font(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom2_lyricsChordsColor;
		whichone = "custom2_lyricsChordsColor";
		doDisplay(view);
	}

	public void dark_capo_font(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.dark_lyricsCapoColor;
		whichone = "dark_lyricsCapoColor";
		doDisplay(view);
	}

	public void custom1_capo_font(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom1_lyricsCapoColor;
		whichone = "custom1_lyricsCapoColor";
		doDisplay(view);
	}

	public void custom2_capo_font(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom2_lyricsCapoColor;
		whichone = "custom2_lyricsCapoColor";
		doDisplay(view);
	}

	public void light_capo_font(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.light_lyricsCapoColor;
		whichone = "light_lyricsCapoColor";
		doDisplay(view);
	}

    public void light_verse_background(View view) {
        // Set global initialcolor
        initialcolor = FullscreenActivity.light_lyricsVerseColor;
        whichone = "light_lyricsVerseColor";
        doDisplay(view);
    }

    public void dark_verse_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.dark_lyricsVerseColor;
		whichone = "dark_lyricsVerseColor";
		doDisplay(view);
	}

	public void custom1_verse_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom1_lyricsVerseColor;
		whichone = "custom1_lyricsVerseColor";
		doDisplay(view);
	}

	public void custom2_verse_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom2_lyricsVerseColor;
		whichone = "custom2_lyricsVerseColor";
		doDisplay(view);
	}

    public void light_chorus_background(View view) {
        // Set global initialcolor
        initialcolor = FullscreenActivity.light_lyricsChorusColor;
        whichone = "light_lyricsChorusColor";
        doDisplay(view);
    }

    public void dark_chorus_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.dark_lyricsChorusColor;
		whichone = "dark_lyricsChorusColor";
		doDisplay(view);
	}

	public void custom1_chorus_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom1_lyricsChorusColor;
		whichone = "custom1_lyricsChorusColor";
		doDisplay(view);
	}

	public void custom2_chorus_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom2_lyricsChorusColor;
		whichone = "custom2_lyricsChorusColor";
		doDisplay(view);
	}

    public void light_prechorus_background(View view) {
        // Set global initialcolor
        initialcolor = FullscreenActivity.light_lyricsPreChorusColor;
        whichone = "light_lyricsPreChorusColor";
        doDisplay(view);
    }

    public void dark_prechorus_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.dark_lyricsPreChorusColor;
		whichone = "dark_lyricsPreChorusColor";
		doDisplay(view);
	}

	public void custom1_prechorus_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom1_lyricsPreChorusColor;
		whichone = "custom1_lyricsPreChorusColor";
		doDisplay(view);
	}

	public void custom2_prechorus_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom2_lyricsPreChorusColor;
		whichone = "custom2_lyricsPreChorusColor";
		doDisplay(view);
	}

    public void light_bridge_background(View view) {
        // Set global initialcolor
        initialcolor = FullscreenActivity.light_lyricsBridgeColor;
        whichone = "light_lyricsBridgeColor";
        doDisplay(view);
    }

    public void dark_bridge_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.dark_lyricsBridgeColor;
		whichone = "dark_lyricsBridgeColor";
		doDisplay(view);
	}

	public void custom1_bridge_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom1_lyricsBridgeColor;
		whichone = "custom1_lyricsBridgeColor";
		doDisplay(view);
	}

	public void custom2_bridge_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom2_lyricsBridgeColor;
		whichone = "custom2_lyricsBridgeColor";
		doDisplay(view);
	}

    public void light_tag_background(View view) {
        // Set global initialcolor
        initialcolor = FullscreenActivity.light_lyricsTagColor;
        whichone = "light_lyricsTagColor";
        doDisplay(view);
    }

    public void dark_tag_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.dark_lyricsTagColor;
		whichone = "dark_lyricsTagColor";
		doDisplay(view);
	}

	public void custom1_tag_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom1_lyricsTagColor;
		whichone = "custom1_lyricsTagColor";
		doDisplay(view);
	}

	public void custom2_tag_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom2_lyricsTagColor;
		whichone = "custom2_lyricsTagColor";
		doDisplay(view);
	}

    public void light_comment_background(View view) {
        // Set global initialcolor
        initialcolor = FullscreenActivity.light_lyricsCommentColor;
        whichone = "light_lyricsCommentColor";
        doDisplay(view);
    }

    public void dark_comment_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.dark_lyricsCommentColor;
		whichone = "dark_lyricsCommentColor";
		doDisplay(view);
	}

	public void custom1_comment_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom1_lyricsCommentColor;
		whichone = "custom1_lyricsCommentColor";
		doDisplay(view);
	}

	public void custom2_comment_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom2_lyricsCommentColor;
		whichone = "custom2_lyricsCommentColor";
		doDisplay(view);
	}

    public void light_custom_background(View view) {
        // Set global initialcolor
        initialcolor = FullscreenActivity.light_lyricsCustomColor;
        whichone = "light_lyricsCustomColor";
        doDisplay(view);
    }

    public void dark_custom_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.dark_lyricsCustomColor;
		whichone = "dark_lyricsCustomColor";
		doDisplay(view);
	}

	public void custom1_custom_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom1_lyricsCustomColor;
		whichone = "custom1_lyricsCustomColor";
		doDisplay(view);
	}

	public void custom2_custom_background(View view) {
		// Set global initialcolor
		initialcolor = FullscreenActivity.custom2_lyricsCustomColor;
		whichone = "custom2_lyricsCustomColor";
		doDisplay(view);
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
	}

	public void resetColors(View view) {
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

		Preferences.savePreferences();
		setButtonColors();
	}

	public void gotosongs(View view) {
        finish();
		Intent intent = new Intent();
		intent.setClass(ChangeDisplayPreferences.this, FullscreenActivity.class);
		startActivity(intent);
	}
}
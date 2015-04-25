package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class ChangeFonts extends Activity {

	// Set the defaults
	View view;
	Button lyrics_default_button;
	Button lyrics_monospace_button;
	Button lyrics_sans_button;
	Button lyrics_serif_button;
	Button lyrics_firasanslight_button;
	Button lyrics_firasansregular_button;
	Button lyrics_kaushanscript_button;
	Button lyrics_latolight_button;
	Button lyrics_latoregular_button;
	Button chords_default_button;
	Button chords_monospace_button;
	Button chords_sans_button;
	Button chords_serif_button;
	Button chords_firasanslight_button;
	Button chords_firasansregular_button;
	Button chords_kaushanscript_button;
	Button chords_latolight_button;
	Button chords_latoregular_button;
	Typeface firasanslight;
	Typeface firasansregular;
	Typeface kaushanscript;
	Typeface latolight;
	Typeface latoregular;
	TextView lyrics1;
	TextView lyrics2;
	TextView chords1;
	TextView chords2;
	TextView linespacingtext;
	private boolean PresentMode;
	SeekBar linespacing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = findViewById(R.layout.choose_font);
		// Load the user preferences
		Preferences.loadPreferences();
        PresentMode = getIntent().getBooleanExtra("PresentMode", false);

		// Set the screen and title
		setContentView(R.layout.choose_font);
		getActionBar().setTitle(getResources().getString(R.string.changefonts));
		
		lyrics_default_button 			= (Button) findViewById(R.id.lyrics_default_button);
		lyrics_monospace_button 		= (Button) findViewById(R.id.lyrics_monospace_button);
		lyrics_sans_button 				= (Button) findViewById(R.id.lyrics_sans_button);
		lyrics_serif_button 			= (Button) findViewById(R.id.lyrics_serif_button);
		lyrics_firasanslight_button   	= (Button) findViewById(R.id.lyrics_firasanslight_button);
		lyrics_firasansregular_button 	= (Button) findViewById(R.id.lyrics_firasansregular_button);
		lyrics_kaushanscript_button 	= (Button) findViewById(R.id.lyrics_kaushanscript_button);
		lyrics_latolight_button 		= (Button) findViewById(R.id.lyrics_latolight_button);
		lyrics_latoregular_button 		= (Button) findViewById(R.id.lyrics_latoregular_button);
		chords_default_button 			= (Button) findViewById(R.id.chords_default_button);
		chords_monospace_button 		= (Button) findViewById(R.id.chords_monospace_button);
		chords_sans_button 				= (Button) findViewById(R.id.chords_sans_button);
		chords_serif_button 			= (Button) findViewById(R.id.chords_serif_button);
		chords_firasanslight_button   	= (Button) findViewById(R.id.chords_firasanslight_button);
		chords_firasansregular_button 	= (Button) findViewById(R.id.chords_firasansregular_button);
		chords_kaushanscript_button 	= (Button) findViewById(R.id.chords_kaushanscript_button);
		chords_latolight_button 		= (Button) findViewById(R.id.chords_latolight_button);
		chords_latoregular_button 		= (Button) findViewById(R.id.chords_latoregular_button);
		
		linespacing = (SeekBar) findViewById(R.id.linespacing);
		linespacingtext = (TextView) findViewById(R.id.linespacingtext);
		
		firasanslight = Typeface.createFromAsset(getAssets(), "fonts/FiraSansOT-Light.otf");
		firasansregular = Typeface.createFromAsset(getAssets(), "fonts/FiraSansOT-Regular.otf");
		kaushanscript = Typeface.createFromAsset(getAssets(), "fonts/KaushanScript-Regular.otf");
		latolight = Typeface.createFromAsset(getAssets(), "fonts/Lato-Lig.ttf");
		latoregular = Typeface.createFromAsset(getAssets(), "fonts/Lato-Reg.ttf");

		// Set the buttons to the correct fonts
		lyrics_default_button.setTypeface(Typeface.DEFAULT);
		lyrics_monospace_button.setTypeface(Typeface.MONOSPACE);
		lyrics_sans_button.setTypeface(Typeface.SANS_SERIF);
		lyrics_serif_button.setTypeface(Typeface.SERIF);
		lyrics_firasanslight_button.setTypeface(firasanslight);
		lyrics_firasansregular_button.setTypeface(firasansregular);
		lyrics_kaushanscript_button.setTypeface(kaushanscript);
		lyrics_latolight_button.setTypeface(latolight);
		lyrics_latoregular_button.setTypeface(latoregular);
		chords_default_button.setTypeface(Typeface.DEFAULT);
		chords_monospace_button.setTypeface(Typeface.MONOSPACE);
		chords_sans_button.setTypeface(Typeface.SANS_SERIF);
		chords_serif_button.setTypeface(Typeface.SERIF);
		chords_firasanslight_button.setTypeface(firasanslight);
		chords_firasansregular_button.setTypeface(firasansregular);
		chords_kaushanscript_button.setTypeface(kaushanscript);
		chords_latolight_button.setTypeface(latolight);
		chords_latoregular_button.setTypeface(latoregular);

		// Load up the users font preference as it stands
		Preferences.loadPreferences();

		linespacing.setProgress(FullscreenActivity.linespacing);
		linespacing.setOnSeekBarChangeListener(new linespacingListener());
        linespacingtext.setText(FullscreenActivity.linespacing+" %");

		// Update the preview bit
		updatePreview(view);
		
	}
	
	public void updatePreview(View view) {
		// Set the chords and lyrics to match the current preferences
		lyrics1 = (TextView) findViewById(R.id.font_lyrics_line1);
		lyrics2 = (TextView) findViewById(R.id.font_lyrics_line2);
		chords1 = (TextView) findViewById(R.id.font_chords_line1);
		chords2 = (TextView) findViewById(R.id.font_chords_line2);
		lyrics1.setTextColor(FullscreenActivity.dark_lyricsTextColor);
		lyrics2.setTextColor(FullscreenActivity.dark_lyricsTextColor);
		chords1.setTextColor(FullscreenActivity.dark_lyricsChordsColor);
		chords2.setTextColor(FullscreenActivity.dark_lyricsChordsColor);
		lyrics1.setBackgroundColor(FullscreenActivity.dark_lyricsVerseColor);
		lyrics2.setBackgroundColor(FullscreenActivity.dark_lyricsVerseColor);
		chords1.setBackgroundColor(FullscreenActivity.dark_lyricsVerseColor);
		chords2.setBackgroundColor(FullscreenActivity.dark_lyricsVerseColor);
		lyrics1.setPadding(0, -(int) ((float)FullscreenActivity.linespacing/3.0f), 0, 0);
		lyrics2.setPadding(0, -(int) ((float)FullscreenActivity.linespacing/3.0f), 0, 0);
		chords1.setPadding(0, -(int) ((float)FullscreenActivity.linespacing/3.0f), 0, 0);
		chords2.setPadding(0, -(int) ((float)FullscreenActivity.linespacing/3.0f), 0, 0);
		
		if (FullscreenActivity.mylyricsfontnum == 1) {
			lyrics1.setTypeface(Typeface.MONOSPACE);
			lyrics2.setTypeface(Typeface.MONOSPACE);
		} else if (FullscreenActivity.mylyricsfontnum == 2) {
			lyrics1.setTypeface(Typeface.SANS_SERIF);
			lyrics2.setTypeface(Typeface.SANS_SERIF);
		} else if (FullscreenActivity.mylyricsfontnum == 3) {
			lyrics1.setTypeface(Typeface.SERIF);
			lyrics2.setTypeface(Typeface.SERIF);
		} else if (FullscreenActivity.mylyricsfontnum == 4) {
			lyrics1.setTypeface(firasanslight);
			lyrics2.setTypeface(firasanslight);
		} else if (FullscreenActivity.mylyricsfontnum == 5) {
			lyrics1.setTypeface(firasansregular);
			lyrics2.setTypeface(firasansregular);
		} else if (FullscreenActivity.mylyricsfontnum == 6) {
			lyrics1.setTypeface(kaushanscript);
			lyrics2.setTypeface(kaushanscript);
		} else if (FullscreenActivity.mylyricsfontnum == 7) {
			lyrics1.setTypeface(latolight);
			lyrics2.setTypeface(latolight);
		} else if (FullscreenActivity.mylyricsfontnum == 8) {
			lyrics1.setTypeface(latoregular);
			lyrics2.setTypeface(latoregular);
		} else {
			lyrics1.setTypeface(Typeface.DEFAULT);
			lyrics2.setTypeface(Typeface.DEFAULT);
		}

		if (FullscreenActivity.mychordsfontnum == 1) {
			chords1.setTypeface(Typeface.MONOSPACE);
			chords2.setTypeface(Typeface.MONOSPACE);
		} else if (FullscreenActivity.mychordsfontnum == 2) {
			chords1.setTypeface(Typeface.SANS_SERIF);
			chords2.setTypeface(Typeface.SANS_SERIF);
		} else if (FullscreenActivity.mychordsfontnum == 3) {
			chords1.setTypeface(Typeface.SERIF);
			chords2.setTypeface(Typeface.SERIF);
		} else if (FullscreenActivity.mychordsfontnum == 4) {
			chords1.setTypeface(firasanslight);
			chords2.setTypeface(firasanslight);
		} else if (FullscreenActivity.mychordsfontnum == 5) {
			chords1.setTypeface(firasansregular);
			chords2.setTypeface(firasansregular);
		} else if (FullscreenActivity.mychordsfontnum == 6) {
			chords1.setTypeface(kaushanscript);
			chords2.setTypeface(kaushanscript);
		} else if (FullscreenActivity.mychordsfontnum == 7) {
			chords1.setTypeface(latolight);
			chords2.setTypeface(latolight);
		} else if (FullscreenActivity.mychordsfontnum == 8) {
			chords1.setTypeface(latoregular);
			chords2.setTypeface(latoregular);
		} else {
			chords1.setTypeface(Typeface.DEFAULT);
			chords2.setTypeface(Typeface.DEFAULT);
		}
		
	}
	
	@Override
	public void onBackPressed() {
		Intent viewsong = new Intent(this, FullscreenActivity.class);
		if (PresentMode) {
			viewsong.setClass(ChangeFonts.this, PresentMode.class);			
			startActivity(viewsong);
			this.finish();
		} else {
			viewsong.setClass(ChangeFonts.this, FullscreenActivity.class);			
			startActivity(viewsong);
			this.finish();
		}

		startActivity(viewsong);
		finish();
	    return;
	}

	
	public void gotosongs(View view) {
		Intent intent = new Intent();
		intent.setClass(ChangeFonts.this, FullscreenActivity.class);
		if (PresentMode) {
			intent.setClass(ChangeFonts.this, PresentMode.class);			
			startActivity(intent);
			this.finish();
		} else {
			intent.setClass(ChangeFonts.this, FullscreenActivity.class);			
			startActivity(intent);
			this.finish();
		}
	    return;
	}

	
	public void lyrics_default(View view) {
		FullscreenActivity.mylyricsfontnum = 0;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void lyrics_monospace(View view) {
		FullscreenActivity.mylyricsfontnum = 1;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}
	public void lyrics_sans(View view) {
		FullscreenActivity.mylyricsfontnum = 2;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void lyrics_serif(View view) {
		FullscreenActivity.mylyricsfontnum = 3;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void lyrics_firasanslight(View view) {
		FullscreenActivity.mylyricsfontnum = 4;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void lyrics_firasansregular(View view) {
		FullscreenActivity.mylyricsfontnum = 5;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void lyrics_kaushanscript(View view) {
		FullscreenActivity.mylyricsfontnum = 6;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void lyrics_latolight(View view) {
		FullscreenActivity.mylyricsfontnum = 7;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void lyrics_latoregular(View view) {
		FullscreenActivity.mylyricsfontnum = 8;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void chords_default(View view) {
		FullscreenActivity.mychordsfontnum = 0;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void chords_monospace(View view) {
		FullscreenActivity.mychordsfontnum = 1;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}
	public void chords_sans(View view) {
		FullscreenActivity.mychordsfontnum = 2;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void chords_serif(View view) {
		FullscreenActivity.mychordsfontnum = 3;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void chords_firasanslight(View view) {
		FullscreenActivity.mychordsfontnum = 4;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void chords_firasansregular(View view) {
		FullscreenActivity.mychordsfontnum = 5;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void chords_kaushanscript(View view) {
		FullscreenActivity.mychordsfontnum = 6;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void chords_latolight(View view) {
		FullscreenActivity.mychordsfontnum = 7;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}

	public void chords_latoregular(View view) {
		FullscreenActivity.mychordsfontnum = 8;
		// Save preferences
		Preferences.savePreferences();
		// update the preview
		updatePreview(view);
	}
	
	
	
	
	private class linespacingListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            linespacingtext.setText(progress+" %");
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
        	FullscreenActivity.linespacing = linespacing.getProgress();
    		// Save preferences
    		Preferences.savePreferences();
            updatePreview(view);
        }
    }
	
}

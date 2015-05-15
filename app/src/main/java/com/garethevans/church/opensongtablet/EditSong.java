package com.garethevans.church.opensongtablet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class EditSong extends Activity {

	// Prepare the views of things that can be edited
	TextView song_title;
	TextView song_author;
	TextView song_copyright;
	TextView song_presentation;
	TextView song_hymn_number;
	Spinner song_capo;
	Spinner song_capo_print;
	int temposlider;
	SeekBar song_tempo;
	TextView song_tempo_text;
	Spinner song_time_signature;
	Spinner song_pad_file;
	Spinner song_key;
	TextView song_CCLI;
	TextView song_theme;
	TextView song_user1;
	TextView song_user2;
	TextView song_user3;
	TextView song_aka;
	TextView song_key_line;
	TextView song_lyrics;
	TextView song_notes;
	TextView song_duration;
	TextView song_midi;
	TextView song_midi_index;
	TextView song_restrictions;
	TextView song_books;
	TextView song_pitch;
	CheckBox song_theme_christ_attributes;
	CheckBox song_theme_christ_birth;
	CheckBox song_theme_christ_death_atonement;
	CheckBox song_theme_christ_power_majesty;
	CheckBox song_theme_christ_love_mercy;
	CheckBox song_theme_christ_resurrection;
	CheckBox song_theme_christ_second_coming;
	CheckBox song_theme_christ_victory;
	CheckBox song_theme_church_commitment_obedience;
	CheckBox song_theme_church_country;
	CheckBox song_theme_church_eternal_life_heaven;
	CheckBox song_theme_church_evangelism;
	CheckBox song_theme_church_family_fellowship;
	CheckBox song_theme_church_fellowship_w_god;
	CheckBox song_theme_church_purity_holiness;
	CheckBox song_theme_church_renewal;
	CheckBox song_theme_church_repentance_salvation;
	CheckBox song_theme_church_service_ministry;
	CheckBox song_theme_church_spiritual_hunger;
	CheckBox song_theme_fruit_faith_hope;
	CheckBox song_theme_fruit_humility_meekness;
	CheckBox song_theme_fruit_joy;
	CheckBox song_theme_fruit_love;
	CheckBox song_theme_fruit_patience_kindness;
	CheckBox song_theme_fruit_peace_comfort;
	CheckBox song_theme_god_attributes;
	CheckBox song_theme_god_creator_creation;
	CheckBox song_theme_god_father;
	CheckBox song_theme_god_guidance_care;
	CheckBox song_theme_god_holiness;
	CheckBox song_theme_god_holy_spirit;
	CheckBox song_theme_god_love_mercy;
	CheckBox song_theme_god_power_majesty;
	CheckBox song_theme_god_promises;
	CheckBox song_theme_god_victory;
	CheckBox song_theme_god_word;
	CheckBox song_theme_worship_assurance_trust;
	CheckBox song_theme_worship_call_opening;
	CheckBox song_theme_worship_celebration;
	CheckBox song_theme_worship_declaration;
	CheckBox song_theme_worship_intimacy;
	CheckBox song_theme_worship_invitation;
	CheckBox song_theme_worship_praise_adoration;
	CheckBox song_theme_worship_prayer_devotion;
	CheckBox song_theme_worship_provision_deliverance;
	CheckBox song_theme_worship_thankfulness;
	private boolean PresenterMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        PresenterMode = getIntent().getBooleanExtra("PresenterMode", false);

		setContentView(R.layout.edit_song);
		getActionBar().setTitle(FullscreenActivity.songfilename);

		// By default, the main edit boxes are visible and the advanced ones hidden.
		// The user can change this by clicking on the switch general/advanced button

		try {
			LoadXML.loadXML();
		} catch (XmlPullParserException | IOException e1) {
			e1.printStackTrace();
		}

		try {
			showLyrics(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		Intent viewsong = new Intent();

		if (PresenterMode) {
			viewsong.setClass(EditSong.this, PresenterMode.class);
			startActivity(viewsong);
			this.finish();
		} else {
			viewsong.setClass(EditSong.this, FullscreenActivity.class);			
			startActivity(viewsong);
			this.finish();
		}
	}

	// This bit draws the lyrics stored in the variable to the page.
	public void showLyrics(View view) throws IOException {

		// Try to show the soft keyboard by default
		InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 

		// Now update the input fields
		song_title = (TextView) findViewById(R.id.edit_song_title);
		mgr.showSoftInput(song_title, InputMethodManager.SHOW_IMPLICIT);
		song_author = (TextView) findViewById(R.id.edit_song_author);
		mgr.showSoftInput(song_author, InputMethodManager.SHOW_IMPLICIT);
		song_copyright = (TextView) findViewById(R.id.edit_song_copyright);
		mgr.showSoftInput(song_copyright, InputMethodManager.SHOW_IMPLICIT);
		song_lyrics = (TextView) findViewById(R.id.edit_song_lyrics);
		mgr.showSoftInput(song_lyrics, InputMethodManager.SHOW_IMPLICIT);
		song_presentation = (TextView) findViewById(R.id.edit_song_presentation);
		mgr.showSoftInput(song_presentation, InputMethodManager.SHOW_IMPLICIT);
		song_hymn_number = (TextView) findViewById(R.id.edit_song_hymn);
		mgr.showSoftInput(song_hymn_number, InputMethodManager.SHOW_IMPLICIT);
		song_capo = (Spinner) findViewById(R.id.edit_song_capo);
		song_capo_print = (Spinner) findViewById(R.id.edit_song_capo_print);
		song_tempo = (SeekBar) findViewById(R.id.edit_song_tempo);
		song_tempo_text = (TextView) findViewById(R.id.tempo_text);
		song_time_signature = (Spinner) findViewById(R.id.edit_song_timesig);
		song_pad_file = (Spinner) findViewById(R.id.edit_pad_file);
	    
		song_notes = (TextView) findViewById(R.id.edit_song_notes);
		mgr.showSoftInput(song_notes, InputMethodManager.SHOW_IMPLICIT);
		song_midi = (TextView) findViewById(R.id.edit_song_midi);
		mgr.showSoftInput(song_midi, InputMethodManager.SHOW_IMPLICIT);
		song_midi_index = (TextView) findViewById(R.id.edit_song_midi_index);
		mgr.showSoftInput(song_midi_index, InputMethodManager.SHOW_IMPLICIT);
		song_duration = (TextView) findViewById(R.id.edit_song_duration);
		mgr.showSoftInput(song_duration, InputMethodManager.SHOW_IMPLICIT);
		song_restrictions = (TextView) findViewById(R.id.edit_song_restrictions);
		mgr.showSoftInput(song_restrictions, InputMethodManager.SHOW_IMPLICIT);
		song_books = (TextView) findViewById(R.id.edit_song_books);
		mgr.showSoftInput(song_books, InputMethodManager.SHOW_IMPLICIT);
		song_pitch = (TextView) findViewById(R.id.edit_song_pitch);
		mgr.showSoftInput(song_pitch, InputMethodManager.SHOW_IMPLICIT);
		
		song_CCLI = (TextView) findViewById(R.id.edit_song_ccli);
		mgr.showSoftInput(song_CCLI, InputMethodManager.SHOW_IMPLICIT);
		song_user1 = (TextView) findViewById(R.id.edit_song_user1);
		mgr.showSoftInput(song_user1, InputMethodManager.SHOW_IMPLICIT);
		song_user2 = (TextView) findViewById(R.id.edit_song_user2);
		mgr.showSoftInput(song_user2, InputMethodManager.SHOW_IMPLICIT);
		song_user3 = (TextView) findViewById(R.id.edit_song_user3);
		mgr.showSoftInput(song_user3, InputMethodManager.SHOW_IMPLICIT);
		song_key = (Spinner) findViewById(R.id.edit_song_key);
		song_aka = (TextView) findViewById(R.id.edit_song_aka);
		mgr.showSoftInput(song_aka, InputMethodManager.SHOW_IMPLICIT);
		song_key_line = (TextView) findViewById(R.id.edit_song_keyline);
		mgr.showSoftInput(song_key_line, InputMethodManager.SHOW_IMPLICIT);
		song_theme_christ_attributes = (CheckBox) findViewById(R.id.edit_song_theme_christ_attributes);
		song_theme_christ_birth = (CheckBox) findViewById(R.id.edit_song_theme_christ_birth);
		song_theme_christ_death_atonement = (CheckBox) findViewById(R.id.edit_song_theme_christ_death_atonement);
		song_theme_christ_power_majesty = (CheckBox) findViewById(R.id.edit_song_theme_christ_power_majesty);
		song_theme_christ_love_mercy = (CheckBox) findViewById(R.id.edit_song_theme_christ_love_mercy);
		song_theme_christ_resurrection = (CheckBox) findViewById(R.id.edit_song_theme_christ_resurrection);
		song_theme_christ_second_coming = (CheckBox) findViewById(R.id.edit_song_theme_christ_second_coming);
		song_theme_christ_victory = (CheckBox) findViewById(R.id.edit_song_theme_christ_victory);
		song_theme_church_commitment_obedience = (CheckBox) findViewById(R.id.edit_song_theme_church_commitment_obedience);
		song_theme_church_country = (CheckBox) findViewById(R.id.edit_song_theme_church_country);
		song_theme_church_eternal_life_heaven = (CheckBox) findViewById(R.id.edit_song_theme_church_eternal_life_heaven);
		song_theme_church_evangelism = (CheckBox) findViewById(R.id.edit_song_theme_church_evangelism);
		song_theme_church_family_fellowship = (CheckBox) findViewById(R.id.edit_song_theme_church_family_fellowship);
		song_theme_church_fellowship_w_god = (CheckBox) findViewById(R.id.edit_song_theme_church_fellowship_w_god);
		song_theme_church_purity_holiness = (CheckBox) findViewById(R.id.edit_song_theme_church_purity_holiness);
		song_theme_church_renewal = (CheckBox) findViewById(R.id.edit_song_theme_church_renewal);
		song_theme_church_repentance_salvation = (CheckBox) findViewById(R.id.edit_song_theme_church_repentance_salvation);
		song_theme_church_service_ministry = (CheckBox) findViewById(R.id.edit_song_theme_church_service_ministry);
		song_theme_church_spiritual_hunger = (CheckBox) findViewById(R.id.edit_song_theme_church_spiritual_hunger);
		song_theme_fruit_faith_hope = (CheckBox) findViewById(R.id.edit_song_theme_fruit_faith_hope);
		song_theme_fruit_humility_meekness = (CheckBox) findViewById(R.id.edit_song_theme_fruit_humility_meekness);
		song_theme_fruit_joy = (CheckBox) findViewById(R.id.edit_song_theme_fruit_joy);
		song_theme_fruit_love = (CheckBox) findViewById(R.id.edit_song_theme_fruit_love);
		song_theme_fruit_patience_kindness = (CheckBox) findViewById(R.id.edit_song_theme_fruit_patience_kindness);
		song_theme_fruit_peace_comfort = (CheckBox) findViewById(R.id.edit_song_theme_fruit_peace_comfort);
		song_theme_god_attributes = (CheckBox) findViewById(R.id.edit_song_theme_god_attributes);
		song_theme_god_creator_creation = (CheckBox) findViewById(R.id.edit_song_theme_god_creator_creation);
		song_theme_god_father = (CheckBox) findViewById(R.id.edit_song_theme_god_father);
		song_theme_god_guidance_care = (CheckBox) findViewById(R.id.edit_song_theme_god_guidance_care);
		song_theme_god_holiness = (CheckBox) findViewById(R.id.edit_song_theme_god_holiness);
		song_theme_god_holy_spirit = (CheckBox) findViewById(R.id.edit_song_theme_god_holy_spirit);
		song_theme_god_love_mercy = (CheckBox) findViewById(R.id.edit_song_theme_god_love_mercy);
		song_theme_god_power_majesty = (CheckBox) findViewById(R.id.edit_song_theme_god_power_majesty);
		song_theme_god_promises = (CheckBox) findViewById(R.id.edit_song_theme_god_promises);
		song_theme_god_victory = (CheckBox) findViewById(R.id.edit_song_theme_god_victory);
		song_theme_god_word = (CheckBox) findViewById(R.id.edit_song_theme_god_word);
		song_theme_worship_assurance_trust = (CheckBox) findViewById(R.id.edit_song_theme_worship_assurance_trust);
		song_theme_worship_call_opening = (CheckBox) findViewById(R.id.edit_song_theme_worship_call_opening);
		song_theme_worship_celebration = (CheckBox) findViewById(R.id.edit_song_theme_worship_celebration);
		song_theme_worship_declaration = (CheckBox) findViewById(R.id.edit_song_theme_worship_declaration);
		song_theme_worship_intimacy = (CheckBox) findViewById(R.id.edit_song_theme_worship_intimacy);
		song_theme_worship_invitation = (CheckBox) findViewById(R.id.edit_song_theme_worship_invitation);
		song_theme_worship_praise_adoration = (CheckBox) findViewById(R.id.edit_song_theme_worship_praise_adoration);
		song_theme_worship_prayer_devotion = (CheckBox) findViewById(R.id.edit_song_theme_worship_prayer_devotion);
		song_theme_worship_provision_deliverance = (CheckBox) findViewById(R.id.edit_song_theme_worship_provision_deliverance);
		song_theme_worship_thankfulness = (CheckBox) findViewById(R.id.edit_song_theme_worship_thankfulness);


		// Get the lyrics into a temp string (so we can get rid of rubbish tabs, etc)
		String editBoxLyrics = FullscreenActivity.mLyrics;
		editBoxLyrics = editBoxLyrics.replaceAll("\r\n", "\n");
		editBoxLyrics = editBoxLyrics.replaceAll("\n\r", "\n");
		editBoxLyrics = editBoxLyrics.replaceAll("\t", "    ");
		editBoxLyrics = editBoxLyrics.replaceAll("\b", "    ");
		editBoxLyrics = editBoxLyrics.replaceAll("\f", "    ");

		// General settings
		song_title.setText(FullscreenActivity.mTitle.toString());
		song_author.setText(FullscreenActivity.mAuthor.toString());
		song_copyright.setText(FullscreenActivity.mCopyright.toString());
		song_lyrics.setText(editBoxLyrics);
		song_lyrics.setTypeface(Typeface.MONOSPACE);
		song_lyrics.setTextSize(18.0f);
		song_CCLI.setText(FullscreenActivity.mCCLI);
		song_presentation.setText(FullscreenActivity.mPresentation);


		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.timesig,
                R.layout.my_spinner);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		song_time_signature.setAdapter(adapter2);

		ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this,
                R.array.capo,
                R.layout.my_spinner);
		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		song_capo.setAdapter(adapter3);

		ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(this,
                R.array.capoprint,
                R.layout.my_spinner);
		adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		song_capo_print.setAdapter(adapter4);

		// Pad file options
		ArrayList<String> padoptions = new ArrayList<>();
		padoptions.add(getResources().getString(R.string.off));
		padoptions.add(getResources().getString(R.string.pad_auto));
		
		ArrayAdapter<String> adapter5 = new ArrayAdapter<>(this,
			     android.R.layout.simple_spinner_item, padoptions);
		adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		song_pad_file.setAdapter(adapter5);

		ArrayAdapter<CharSequence> adapter6 = ArrayAdapter.createFromResource(this,
                R.array.key_choice,
                R.layout.my_spinner);
		adapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		song_key.setAdapter(adapter6);

		// Advanced settings
		song_aka.setText(FullscreenActivity.mAka);
		song_key_line.setText(FullscreenActivity.mKeyLine);
		song_hymn_number.setText(FullscreenActivity.mHymnNumber);
		song_user1.setText(FullscreenActivity.mUser1);
		song_user2.setText(FullscreenActivity.mUser2);
		song_user3.setText(FullscreenActivity.mUser3);
		
		// Set song key
		int keyindex=0;
		switch (FullscreenActivity.mKey) {
			case "A":
				keyindex = 1;
				break;
			case "A#":
				keyindex = 2;
				break;
			case "Bb":
				keyindex = 3;
				break;
			case "B":
				keyindex = 4;
				break;
			case "C":
				keyindex = 5;
				break;
			case "C#":
				keyindex = 6;
				break;
			case "Db":
				keyindex = 7;
				break;
			case "D":
				keyindex = 8;
				break;
			case "D#":
				keyindex = 9;
				break;
			case "Eb":
				keyindex = 10;
				break;
			case "E":
				keyindex = 11;
				break;
			case "F":
				keyindex = 12;
				break;
			case "F#":
				keyindex = 13;
				break;
			case "Gb":
				keyindex = 14;
				break;
			case "G":
				keyindex = 15;
				break;
			case "G#":
				keyindex = 16;
				break;
			case "Ab":
				keyindex = 17;
				break;
			case "Am":
				keyindex = 18;
				break;
			case "A#b":
				keyindex = 19;
				break;
			case "Bbm":
				keyindex = 20;
				break;
			case "Bm":
				keyindex = 21;
				break;
			case "Cm":
				keyindex = 22;
				break;
			case "C#m":
				keyindex = 23;
				break;
			case "Dbm":
				keyindex = 24;
				break;
			case "Dm":
				keyindex = 25;
				break;
			case "D#m":
				keyindex = 26;
				break;
			case "Ebm":
				keyindex = 27;
				break;
			case "Em":
				keyindex = 28;
				break;
			case "Fm":
				keyindex = 29;
				break;
			case "F#m":
				keyindex = 30;
				break;
			case "Gbm":
				keyindex = 31;
				break;
			case "Gm":
				keyindex = 32;
				break;
			case "G#m":
				keyindex = 33;
				break;
			case "Abm":
				keyindex = 34;
				break;
		}
		song_key.setSelection(keyindex);
		//song_key.setText(FullscreenActivity.mKey.toString());
		
		song_duration.setText(FullscreenActivity.mDuration);
		song_notes.setText(FullscreenActivity.mNotes);
		song_midi.setText(FullscreenActivity.mMidi);
		song_midi_index.setText(FullscreenActivity.mMidiIndex);
		song_books.setText(FullscreenActivity.mBooks);
		song_restrictions.setText(FullscreenActivity.mRestrictions);
		song_pitch.setText(FullscreenActivity.mPitch);
		
		String temp_tempo = FullscreenActivity.mTempo;
		temp_tempo = temp_tempo.replace("Very Fast", "140");
		temp_tempo = temp_tempo.replace("Fast", "120");
		temp_tempo = temp_tempo.replace("Moderate", "100");
		temp_tempo = temp_tempo.replace("Slow", "80");
		temp_tempo = temp_tempo.replace("Very Slow", "60");
		temp_tempo = temp_tempo.replaceAll("[\\D]", "");
		// Get numerical value for slider
		
		try {
			temposlider = Integer.parseInt(temp_tempo);
		} catch(NumberFormatException nfe) {
		   System.out.println("Could not parse " + nfe);
		   temposlider = 39;
		}
		temposlider = temposlider - 39;

		if (temposlider<1) {
			temposlider=0;
	    	song_tempo_text.setText(getResources().getString(R.string.notset));
		} else {
			song_tempo_text.setText(temp_tempo+" "+getResources().getString(R.string.bpm));
		}
		song_tempo.setProgress(temposlider);	
		song_tempo.setOnSeekBarChangeListener(new song_tempoListener());
    	
		int timesigindex = 0;
		switch (FullscreenActivity.mTimeSig) {
			case "2/4":
				timesigindex = 1;
				break;
			case "3/4":
				timesigindex = 2;
				break;
			case "3/8":
				timesigindex = 3;
				break;
			case "4/4":
				timesigindex = 4;
				break;
			case "5/4":
				timesigindex = 5;
				break;
			case "5/8":
				timesigindex = 6;
				break;
			case "6/4":
				timesigindex = 7;
				break;
			case "6/8":
				timesigindex = 8;
				break;
			case "7/4":
				timesigindex = 9;
				break;
			case "8/8":
				timesigindex = 10;
				break;
		}

		song_time_signature.setSelection(timesigindex);

		int capoindex = 0;
		switch (FullscreenActivity.mCapo) {
			case "1":
				capoindex = 1;
				break;
			case "2":
				capoindex = 2;
				break;
			case "3":
				capoindex = 3;
				break;
			case "4":
				capoindex = 4;
				break;
			case "5":
				capoindex = 5;
				break;
			case "6":
				capoindex = 6;
				break;
			case "7":
				capoindex = 7;
				break;
			case "8":
				capoindex = 8;
				break;
			case "9":
				capoindex = 9;
				break;
			case "10":
				capoindex = 10;
				break;
			case "11":
				capoindex = 11;
				break;
		}
		
		song_capo.setSelection(capoindex);
		int capoprintindex = 0;
		if (FullscreenActivity.mCapoPrint.equals("true")) {
			capoprintindex = 1;
		} else if (FullscreenActivity.mCapoPrint.equals("false")) {
			capoprintindex = 2;
		}
		song_capo_print.setSelection(capoprintindex);

		int padfileindex = 0;
		if (FullscreenActivity.mPadFile.equals(getResources().getString(R.string.pad_auto))) {
			capoprintindex = 1;
		}
		song_pad_file.setSelection(padfileindex);

		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_christ_attributes))) {
			song_theme_christ_attributes.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_christ_birth))) {
			song_theme_christ_birth.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_christ_death_atonement))) {
			song_theme_christ_death_atonement.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_christ_love_mercy))) {
			song_theme_christ_love_mercy.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_christ_power_majesty))) {
			song_theme_christ_power_majesty.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_christ_resurrection))) {
			song_theme_christ_resurrection.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_christ_second_coming))) {
			song_theme_christ_second_coming.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_christ_victory))) {
			song_theme_christ_victory.setChecked(true);
		}

		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_church_commitment_obedience))) {
			song_theme_church_commitment_obedience.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_church_country))) {
			song_theme_church_country.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_church_eternal_life_heaven))) {
			song_theme_church_eternal_life_heaven.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_church_evangelism))) {
			song_theme_church_evangelism.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_church_family_fellowship))) {
			song_theme_church_family_fellowship.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_church_fellowship_w_god))) {
			song_theme_church_fellowship_w_god.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_church_purity_holiness))) {
			song_theme_church_purity_holiness.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_church_renewal))) {
			song_theme_church_renewal.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_church_repentance_salvation))) {
			song_theme_church_repentance_salvation.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_church_service_ministry))) {
			song_theme_church_service_ministry.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_church_spiritual_hunger))) {
			song_theme_church_spiritual_hunger.setChecked(true);
		}

		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_fruit_faith_hope))) {
			song_theme_fruit_faith_hope.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_fruit_humility_meekness))) {
			song_theme_fruit_humility_meekness.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_fruit_joy))) {
			song_theme_fruit_joy.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_fruit_love))) {
			song_theme_fruit_love.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_fruit_patience_kindness))) {
			song_theme_fruit_patience_kindness.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_fruit_peace_comfort))) {
			song_theme_fruit_peace_comfort.setChecked(true);
		}

		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_god_attributes))) {
			song_theme_god_attributes.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_god_creator_creation))) {
			song_theme_god_creator_creation.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_god_father))) {
			song_theme_god_father.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_god_guidance_care))) {
			song_theme_god_guidance_care.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_god_holiness))) {
			song_theme_god_holiness.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_god_holy_spirit))) {
			song_theme_god_holy_spirit.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_god_love_mercy))) {
			song_theme_god_love_mercy.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_god_power_majesty))) {
			song_theme_god_power_majesty.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_god_promises))) {
			song_theme_god_promises.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_god_victory))) {
			song_theme_god_victory.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_god_word))) {
			song_theme_god_word.setChecked(true);
		}

		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_worship_assurance_trust))) {
			song_theme_worship_assurance_trust.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_worship_call_opening))) {
			song_theme_worship_call_opening.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_worship_celebration))) {
			song_theme_worship_celebration.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_worship_declaration))) {
			song_theme_worship_declaration.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_worship_intimacy))) {
			song_theme_worship_intimacy.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_worship_invitation))) {
			song_theme_worship_invitation.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_worship_praise_adoration))) {
			song_theme_worship_praise_adoration.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_worship_prayer_devotion))) {
			song_theme_worship_prayer_devotion.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_worship_provision_deliverance))) {
			song_theme_worship_provision_deliverance.setChecked(true);
		}
		if (FullscreenActivity.mTheme.contains(getResources().getString(
				R.string.theme_worship_thankfulness))) {
			song_theme_worship_thankfulness.setChecked(true);
		}

	}

	private class song_tempoListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        	temposlider = song_tempo.getProgress()+39;
        	if (temposlider>39) {
            	song_tempo_text.setText(temposlider+" "+getResources().getString(R.string.bpm));
        	} else {
            	song_tempo_text.setText(getResources().getString(R.string.notset));
        	}
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

	public void cancelEdit(View view) {
		Intent viewsong = new Intent(this, FullscreenActivity.class);
		startActivity(viewsong);
		finish();

	}

	public void showGeneralAdvanced(View view) {
		final LinearLayout generalSettings = (LinearLayout) findViewById(R.id.general_settings);
		final LinearLayout advancedSettings = (LinearLayout) findViewById(R.id.advanced_settings);
		final LinearLayout buttons = (LinearLayout) findViewById(R.id.buttons);

		if (generalSettings.getVisibility() == View.VISIBLE) {
			// Slide the general settings left
			generalSettings.startAnimation(AnimationUtils.loadAnimation(
					getApplicationContext(), R.anim.slide_out_left));
			buttons.startAnimation(AnimationUtils.loadAnimation(
					getApplicationContext(), R.anim.slide_out_left));
			// Wait 300ms before hiding the general settings and unhiding the advanced settings
			Handler delayfadeinredraw = new Handler();
			delayfadeinredraw.postDelayed(new Runnable() {
				@Override
				public void run() {
					generalSettings.setVisibility(View.GONE);
					buttons.setVisibility(View.GONE);
				}
			}, 300); // 300ms

			// Wait 300ms before sliding in the advanced settings
			Handler delayfadeinredraw2 = new Handler();
			delayfadeinredraw2.postDelayed(new Runnable() {
				@Override
				public void run() {
					advancedSettings.startAnimation(AnimationUtils
							.loadAnimation(getApplicationContext(),
									R.anim.slide_in_right));
					buttons.startAnimation(AnimationUtils.loadAnimation(
							getApplicationContext(), R.anim.slide_in_right));
					advancedSettings.setVisibility(View.VISIBLE);
					buttons.setVisibility(View.VISIBLE);
				}
			}, 600); // 300ms

		} else {
			// Slide the advanced settings right
			advancedSettings.startAnimation(AnimationUtils.loadAnimation(
					getApplicationContext(), R.anim.slide_out_right));
			buttons.startAnimation(AnimationUtils.loadAnimation(
					getApplicationContext(), R.anim.slide_out_right));
			// Wait 300ms before hiding the advanced settings and unhiding the general settings
			Handler delayfadeinredraw = new Handler();
			delayfadeinredraw.postDelayed(new Runnable() {
				@Override
				public void run() {
					advancedSettings.setVisibility(View.GONE);
					buttons.setVisibility(View.GONE);
				}
			}, 300); // 300ms

			// Wait 300ms before sliding in the general settings
			Handler delayfadeinredraw2 = new Handler();
			delayfadeinredraw2.postDelayed(new Runnable() {
				@Override
				public void run() {
					generalSettings.startAnimation(AnimationUtils
							.loadAnimation(getApplicationContext(),
									R.anim.slide_in_left));
					buttons.startAnimation(AnimationUtils.loadAnimation(
							getApplicationContext(), R.anim.slide_in_left));
					generalSettings.setVisibility(View.VISIBLE);
					buttons.setVisibility(View.VISIBLE);
				}
			}, 600); // 300ms
		}
	}

	public void deleteThisSong(View view) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					// Yes button clicked
					String setFileLocation = FullscreenActivity.dir + "/"
							+ FullscreenActivity.songfilename;
					File filetoremove = new File(setFileLocation);
					// Don't allow the user to delete Love Everlasting
					// This is to stop there being no song to show after
					// deleting the currently viewed one
					if (FullscreenActivity.songfilename
							.equals("Love everlasting")) {
						FullscreenActivity.myToastMessage = "\"Love everlasting\" "
								+ getResources().getString(
										R.string.shouldnotbedeleted);
						ShowToast.showToast(EditSong.this);
					} else {
						boolean deleted = filetoremove.delete();
						if (deleted) {
							FullscreenActivity.myToastMessage = "\""
									+ FullscreenActivity.songfilename
									+ "\" "
									+ getResources().getString(
											R.string.songhasbeendeleted);
						} else {
							FullscreenActivity.myToastMessage = getResources()
									.getString(R.string.deleteerror_start)
									+ " \""
									+ FullscreenActivity.songfilename
									+ "\" "
									+ getResources().getString(
											R.string.deleteerror_end_song);
						}
						ShowToast.showToast(EditSong.this);
						Intent viewsong = new Intent(EditSong.this,
								FullscreenActivity.class);
						startActivity(viewsong);
						finish();
					}
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// No button clicked
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.areyousure))
				.setPositiveButton(getResources().getString(R.string.yes),
						dialogClickListener)
				.setNegativeButton(getResources().getString(R.string.no),
						dialogClickListener).show();
	}

	public static void prepareSongXML() {
		// Prepare the new XML file
		String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		myNEWXML += "<song>\n";
		myNEWXML += "  <title>" + FullscreenActivity.mTitle.toString() + "</title>\n";
		myNEWXML += "  <author>" + FullscreenActivity.mAuthor + "</author>\n";
		myNEWXML += "  <copyright>" + FullscreenActivity.mCopyright + "</copyright>\n";
		myNEWXML += "  <presentation>" + FullscreenActivity.mPresentation + "</presentation>\n";
		myNEWXML += "  <hymn_number>" + FullscreenActivity.mHymnNumber + "</hymn_number>\n";
		myNEWXML += "  <capo print=\"" + FullscreenActivity.mCapoPrint + "\">" + FullscreenActivity.mCapo + "</capo>\n";
		myNEWXML += "  <tempo>" + FullscreenActivity.mTempo + "</tempo>\n";
		myNEWXML += "  <time_sig>" + FullscreenActivity.mTimeSig + "</time_sig>\n";
		myNEWXML += "  <duration>" + FullscreenActivity.mDuration + "</duration>\n";
		myNEWXML += "  <ccli>" + FullscreenActivity.mCCLI + "</ccli>\n";
		myNEWXML += "  <theme>" + FullscreenActivity.mTheme + "</theme>\n";
		myNEWXML += "  <alttheme>" + FullscreenActivity.mAltTheme + "</alttheme>\n";
		myNEWXML += "  <user1>" + FullscreenActivity.mUser1 + "</user1>\n";
		myNEWXML += "  <user2>" + FullscreenActivity.mUser2 + "</user2>\n";
		myNEWXML += "  <user3>" + FullscreenActivity.mUser3 + "</user3>\n";
		myNEWXML += "  <key>" + FullscreenActivity.mKey + "</key>\n";
		myNEWXML += "  <aka>" + FullscreenActivity.mAka + "</aka>\n";
		myNEWXML += "  <key_line>" + FullscreenActivity.mKeyLine + "</key_line>\n";
		myNEWXML += "  <books>" + FullscreenActivity.mBooks + "</books>\n";
		myNEWXML += "  <midi>" + FullscreenActivity.mMidi + "</midi>\n";
		myNEWXML += "  <midi_index>" + FullscreenActivity.mMidiIndex + "</midi_index>\n";
		myNEWXML += "  <pitch>" + FullscreenActivity.mPitch + "</pitch>\n";
		myNEWXML += "  <restrictions>" + FullscreenActivity.mRestrictions + "</restrictions>\n";
		myNEWXML += "  <notes>" + FullscreenActivity.mNotes + "</notes>\n";
		myNEWXML += "  <lyrics>" + FullscreenActivity.mLyrics + "</lyrics>\n";
		myNEWXML += "  <linked_songs>" + FullscreenActivity.mLinkedSongs + "</linked_songs>\n";
		myNEWXML += "  <pad_file>" + FullscreenActivity.mPadFile + "</pad_file>\n";
        myNEWXML += "  <custom_chords>" + FullscreenActivity.mCustomChords + "</custom_chords>\n";

        if (!FullscreenActivity.mExtraStuff1.isEmpty()) {
			myNEWXML += "  " + FullscreenActivity.mExtraStuff1 + "\n";
		}
		if (!FullscreenActivity.mExtraStuff2.isEmpty()) {
			myNEWXML += "  " + FullscreenActivity.mExtraStuff2 + "\n";
		}
		myNEWXML += "</song>";

		FullscreenActivity.mynewXML = myNEWXML;
		

	}

	public static void prepareBlankSongXML() {
		// Prepare the new XML file
		String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		myNEWXML += "<song>\n";
		myNEWXML += "  <title>" + FullscreenActivity.songfilename + "</title>\n";
		myNEWXML += "  <author></author>\n";
		myNEWXML += "  <copyright></copyright>\n";
		myNEWXML += "  <presentation></presentation>\n";
		myNEWXML += "  <hymn_number></hymn_number>\n";
		myNEWXML += "  <capo print=\"\"></capo>\n";
		myNEWXML += "  <tempo></tempo>\n";
		myNEWXML += "  <time_sig></time_sig>\n";
		myNEWXML += "  <duration></duration>\n";
		myNEWXML += "  <ccli></ccli>\n";
		myNEWXML += "  <theme></theme>\n";
		myNEWXML += "  <alttheme></alttheme>\n";
		myNEWXML += "  <user1></user1>\n";
		myNEWXML += "  <user2></user2>\n";
		myNEWXML += "  <user3></user3>\n";
		myNEWXML += "  <key></key>\n";
		myNEWXML += "  <aka></aka>\n";
		myNEWXML += "  <key_line></key_line>\n";
		myNEWXML += "  <books></books>\n";
		myNEWXML += "  <midi></midi>\n";
		myNEWXML += "  <midi_index></midi_index>\n";
		myNEWXML += "  <pitch></pitch>\n";
		myNEWXML += "  <restrictions></restrictions>\n";
		myNEWXML += "  <notes></notes>\n";
		myNEWXML += "  <lyrics></lyrics>\n";
		myNEWXML += "  <linked_songs></linked_songs>\n";
		myNEWXML += "  <pad_file></pad_file>\n";
		myNEWXML += "  <custom_chords></custom_chords>\n";
		myNEWXML += "</song>";
		FullscreenActivity.mynewXML = myNEWXML;
	}

	public void saveEdit(View view) throws IOException {
		// Get the variables
		// Set the newtext to the FullscreenActivity variables
		FullscreenActivity.mTitle = song_title.getText().toString();
		FullscreenActivity.mAuthor = song_author.getText().toString();
		FullscreenActivity.mCopyright = song_copyright.getText().toString();
		FullscreenActivity.mLyrics = song_lyrics.getText().toString();
		FullscreenActivity.mPresentation = song_presentation.getText().toString();
		FullscreenActivity.mHymnNumber = song_hymn_number.getText().toString();
		FullscreenActivity.mCCLI = song_CCLI.getText().toString();
		FullscreenActivity.mUser1 = song_user1.getText().toString();
		FullscreenActivity.mUser2 = song_user2.getText().toString();
		FullscreenActivity.mUser3 = song_user3.getText().toString();
		FullscreenActivity.mAka = song_aka.getText().toString();
		FullscreenActivity.mKeyLine = song_key_line.getText().toString();
		FullscreenActivity.mKey = song_key.getItemAtPosition(song_key.getSelectedItemPosition()).toString();
		FullscreenActivity.mDuration = song_duration.getText().toString();
		FullscreenActivity.mBooks = song_books.getText().toString();
		FullscreenActivity.mMidi = song_midi.getText().toString();
		FullscreenActivity.mMidiIndex = song_midi_index.getText().toString();
		FullscreenActivity.mPitch = song_pitch.getText().toString();
		FullscreenActivity.mRestrictions = song_restrictions.getText().toString();
		FullscreenActivity.mNotes = song_notes.getText().toString();
		FullscreenActivity.mPadFile = song_pad_file.getItemAtPosition(song_pad_file.getSelectedItemPosition()).toString();

		FullscreenActivity.mCapo = song_capo.getItemAtPosition(song_capo.getSelectedItemPosition()).toString();
		int tempmCapoPrint = song_capo_print.getSelectedItemPosition();
		if (tempmCapoPrint==1) {
			FullscreenActivity.mCapoPrint="true";			
		} else if (tempmCapoPrint==2) {
			FullscreenActivity.mCapoPrint="false";			
		} else {
			FullscreenActivity.mCapoPrint="";			
		}
		int valoftempobar = song_tempo.getProgress() + 39;
		if (valoftempobar>39) {
			FullscreenActivity.mTempo = ""+valoftempobar;	
		} else {
			FullscreenActivity.mTempo = "";	
		}
		FullscreenActivity.mTimeSig = song_time_signature.getItemAtPosition(song_time_signature.getSelectedItemPosition()).toString();
		
		FullscreenActivity.mTheme = "";
		
		if (song_theme_christ_attributes.isChecked()) {
			FullscreenActivity.mTheme += song_theme_christ_attributes.getText().toString() + "; ";
		}
		if (song_theme_christ_birth.isChecked()) {
			FullscreenActivity.mTheme += song_theme_christ_birth.getText().toString() + "; ";
		}
		if (song_theme_christ_death_atonement.isChecked()) {
			FullscreenActivity.mTheme += song_theme_christ_death_atonement.getText().toString() + "; ";
		}
		if (song_theme_christ_love_mercy.isChecked()) {
			FullscreenActivity.mTheme += song_theme_christ_love_mercy.getText().toString() + "; ";
		}
		if (song_theme_christ_power_majesty.isChecked()) {
			FullscreenActivity.mTheme += song_theme_christ_power_majesty.getText().toString() + "; ";
		}
		if (song_theme_christ_resurrection.isChecked()) {
			FullscreenActivity.mTheme += song_theme_christ_resurrection.getText().toString() + "; ";
		}
		if (song_theme_christ_second_coming.isChecked()) {
			FullscreenActivity.mTheme += song_theme_christ_second_coming.getText().toString() + "; ";
		}
		if (song_theme_christ_victory.isChecked()) {
			FullscreenActivity.mTheme += song_theme_christ_victory.getText().toString() + "; ";
		}

		if (song_theme_church_commitment_obedience.isChecked()) {
			FullscreenActivity.mTheme += song_theme_church_commitment_obedience.getText().toString() + "; ";
		}
		if (song_theme_church_country.isChecked()) {
			FullscreenActivity.mTheme += song_theme_church_country.getText().toString() + "; ";
		}
		if (song_theme_church_eternal_life_heaven.isChecked()) {
			FullscreenActivity.mTheme += song_theme_church_eternal_life_heaven.getText().toString() + "; ";
		}
		if (song_theme_church_evangelism.isChecked()) {
			FullscreenActivity.mTheme += song_theme_church_evangelism.getText().toString() + "; ";
		}
		if (song_theme_church_family_fellowship.isChecked()) {
			FullscreenActivity.mTheme += song_theme_church_family_fellowship.getText().toString() + "; ";
		}
		if (song_theme_church_fellowship_w_god.isChecked()) {
			FullscreenActivity.mTheme += song_theme_church_fellowship_w_god.getText().toString() + "; ";
		}
		if (song_theme_church_purity_holiness.isChecked()) {
			FullscreenActivity.mTheme += song_theme_church_purity_holiness.getText().toString() + "; ";
		}
		if (song_theme_church_renewal.isChecked()) {
			FullscreenActivity.mTheme += song_theme_church_renewal.getText().toString() + "; ";
		}
		if (song_theme_church_repentance_salvation.isChecked()) {
			FullscreenActivity.mTheme += song_theme_church_repentance_salvation.getText().toString() + "; ";
		}
		if (song_theme_church_service_ministry.isChecked()) {
			FullscreenActivity.mTheme += song_theme_church_service_ministry.getText().toString() + "; ";
		}
		if (song_theme_church_spiritual_hunger.isChecked()) {
			FullscreenActivity.mTheme += song_theme_church_spiritual_hunger.getText().toString() + "; ";
		}

		if (song_theme_fruit_faith_hope.isChecked()) {
			FullscreenActivity.mTheme += song_theme_fruit_faith_hope.getText().toString() + "; ";
		}
		if (song_theme_fruit_humility_meekness.isChecked()) {
			FullscreenActivity.mTheme += song_theme_fruit_humility_meekness.getText().toString() + "; ";
		}
		if (song_theme_fruit_joy.isChecked()) {
			FullscreenActivity.mTheme += song_theme_fruit_joy.getText().toString() + "; ";
		}
		if (song_theme_fruit_love.isChecked()) {
			FullscreenActivity.mTheme += song_theme_fruit_love.getText().toString() + "; ";
		}
		if (song_theme_fruit_patience_kindness.isChecked()) {
			FullscreenActivity.mTheme += song_theme_fruit_patience_kindness.getText().toString() + "; ";
		}
		if (song_theme_fruit_peace_comfort.isChecked()) {
			FullscreenActivity.mTheme += song_theme_fruit_peace_comfort.getText().toString() + "; ";
		}

		if (song_theme_god_attributes.isChecked()) {
			FullscreenActivity.mTheme += song_theme_god_attributes.getText().toString() + "; ";
		}
		if (song_theme_god_creator_creation.isChecked()) {
			FullscreenActivity.mTheme += song_theme_god_creator_creation.getText().toString() + "; ";
		}
		if (song_theme_god_father.isChecked()) {
			FullscreenActivity.mTheme += song_theme_god_father.getText().toString() + "; ";
		}
		if (song_theme_god_guidance_care.isChecked()) {
			FullscreenActivity.mTheme += song_theme_god_guidance_care.getText().toString() + "; ";
		}
		if (song_theme_god_holiness.isChecked()) {
			FullscreenActivity.mTheme += song_theme_god_holiness.getText().toString() + "; ";
		}
		if (song_theme_god_holy_spirit.isChecked()) {
			FullscreenActivity.mTheme += song_theme_god_holy_spirit.getText().toString() + "; ";
		}
		if (song_theme_god_love_mercy.isChecked()) {
			FullscreenActivity.mTheme += song_theme_god_love_mercy.getText().toString() + "; ";
		}
		if (song_theme_god_power_majesty.isChecked()) {
			FullscreenActivity.mTheme += song_theme_god_power_majesty.getText().toString() + "; ";
		}
		if (song_theme_god_promises.isChecked()) {
			FullscreenActivity.mTheme += song_theme_god_promises.getText().toString() + "; ";
		}
		if (song_theme_god_victory.isChecked()) {
			FullscreenActivity.mTheme += song_theme_god_victory.getText().toString() + "; ";
		}
		if (song_theme_god_word.isChecked()) {
			FullscreenActivity.mTheme += song_theme_god_word.getText().toString() + "; ";
		}

		if (song_theme_worship_assurance_trust.isChecked()) {
			FullscreenActivity.mTheme += song_theme_worship_assurance_trust.getText().toString() + "; ";
		}
		if (song_theme_worship_call_opening.isChecked()) {
			FullscreenActivity.mTheme += song_theme_worship_call_opening.getText().toString() + "; ";
		}
		if (song_theme_worship_celebration.isChecked()) {
			FullscreenActivity.mTheme += song_theme_worship_celebration.getText().toString() + "; ";
		}
		if (song_theme_worship_declaration.isChecked()) {
			FullscreenActivity.mTheme += song_theme_worship_declaration.getText().toString() + "; ";
		}
		if (song_theme_worship_intimacy.isChecked()) {
			FullscreenActivity.mTheme += song_theme_worship_intimacy.getText().toString() + "; ";
		}
		if (song_theme_worship_invitation.isChecked()) {
			FullscreenActivity.mTheme += song_theme_worship_invitation.getText().toString() + "; ";
		}
		if (song_theme_worship_praise_adoration.isChecked()) {
			FullscreenActivity.mTheme += song_theme_worship_praise_adoration.getText().toString() + "; ";
		}
		if (song_theme_worship_prayer_devotion.isChecked()) {
			FullscreenActivity.mTheme += song_theme_worship_prayer_devotion.getText().toString() + "; ";
		}
		if (song_theme_worship_provision_deliverance.isChecked()) {
			FullscreenActivity.mTheme += song_theme_worship_provision_deliverance.getText().toString() + "; ";
		}
		if (song_theme_worship_thankfulness.isChecked()) {
			FullscreenActivity.mTheme += song_theme_worship_thankfulness.getText().toString() + "; ";
		}

		// Set the AltTheme to the same as the Theme?
		FullscreenActivity.mAltTheme = FullscreenActivity.mTheme;

		// Prepare the new XML file
		prepareSongXML();

		// Makes sure all & are replaced with &amp;
		FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&amp;","&");
		FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&","&amp;");

		// Now write the modified song
		FileOutputStream overWrite = new FileOutputStream(
				FullscreenActivity.dir + "/" + FullscreenActivity.songfilename,
				false);
		overWrite.write(FullscreenActivity.mynewXML.getBytes());
		overWrite.flush();
		overWrite.close();

		FullscreenActivity.mynewXML = "";
		Intent viewsong = new Intent();

		if (PresenterMode) {
			viewsong.setClass(EditSong.this, PresenterMode.class);
			startActivity(viewsong);
			this.finish();
		} else {
			viewsong.setClass(EditSong.this, FullscreenActivity.class);			
			startActivity(viewsong);
			this.finish();
		}
	}

	public static void justSaveSongXML() throws IOException {
		// Makes sure all & are replaced with &amp;
		FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&amp;","&");
		FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("&","&amp;");

		// Now write the modified song
		String filename;
		if (FullscreenActivity.whichSongFolder.equals("") || FullscreenActivity.whichSongFolder.isEmpty()
				|| FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)
				|| FullscreenActivity.whichSongFolder.equals("("+FullscreenActivity.mainfoldername+")")) {
			filename = FullscreenActivity.dir + "/" + FullscreenActivity.songfilename;
		} else {
			filename = FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename;
		}

		FileOutputStream overWrite = new FileOutputStream(filename,	false);
		overWrite.write(FullscreenActivity.mynewXML.getBytes());
		overWrite.flush();
		overWrite.close();
	}
}
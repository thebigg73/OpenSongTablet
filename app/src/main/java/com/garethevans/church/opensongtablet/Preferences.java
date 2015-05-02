package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

public class Preferences extends Activity {

	// Set the default colours here
	static int default_metronomeColor = 0xffaa1212;
    static int default_dark_lyricsTextColor = 0xffffffff;
    static int default_dark_lyricsCapoColor = 0xffff0000;
	static int default_dark_lyricsBackgroundColor = 0xff000000;
	static int default_dark_lyricsVerseColor = 0xff000000;
	static int default_dark_lyricsChorusColor = 0xff000033;
	static int default_dark_lyricsBridgeColor = 0xff330000;
	static int default_dark_lyricsCommentColor = 0xff003300;
	static int default_dark_lyricsPreChorusColor = 0xff112211;
	static int default_dark_lyricsTagColor = 0xff330033;
	static int default_dark_lyricsChordsColor = 0xffffff00;
	static int default_dark_lyricsCustomColor = 0xff222200;

	static int default_light_lyricsTextColor = 0xff000000;
    static int default_light_lyricsCapoColor = 0xffff0000;
	static int default_light_lyricsBackgroundColor = 0xffffffff;
	static int default_light_lyricsVerseColor = 0xffffffff;
	static int default_light_lyricsChorusColor = 0xffffddff;
	static int default_light_lyricsBridgeColor = 0xffddffff;
	static int default_light_lyricsCommentColor = 0xffddddff;
	static int default_light_lyricsPreChorusColor = 0xffeeccee;
	static int default_light_lyricsTagColor = 0xffddffdd;
	static int default_light_lyricsChordsColor = 0xff0000dd;
	static int default_light_lyricsCustomColor = 0xffccddff;
		
	
	
	public static void loadPreferences() {
		// Load up the user preferences
		// Set to blank if not used before
		
		Log.d("Preferences","Loading");

        FullscreenActivity.showNextInSet = FullscreenActivity.myPreferences.getString("showNextInSet","bottom");
		FullscreenActivity.capoDisplay = FullscreenActivity.myPreferences.getString("capoDisplay", "both");
		FullscreenActivity.languageToLoad = FullscreenActivity.myPreferences.getString("languageToLoad", "");
		FullscreenActivity.mylyricsfontnum = FullscreenActivity.myPreferences.getInt("mylyricsfontnum", 7);
		FullscreenActivity.mychordsfontnum = FullscreenActivity.myPreferences.getInt("mychordsfontnum", 8);
		FullscreenActivity.linespacing = FullscreenActivity.myPreferences.getInt("linespacing", 0);
		
		FullscreenActivity.pageturner_NEXT = FullscreenActivity.myPreferences.getInt("pageturner_NEXT", 22);
		FullscreenActivity.pageturner_PREVIOUS = FullscreenActivity.myPreferences.getInt("pageturner_PREVIOUS", 21);
		FullscreenActivity.pageturner_UP = FullscreenActivity.myPreferences.getInt("pageturner_UP", 19);
		FullscreenActivity.pageturner_DOWN = FullscreenActivity.myPreferences.getInt("pageturner_DOWN", 20);
		FullscreenActivity.pageturner_PAD = FullscreenActivity.myPreferences.getInt("pageturner_PAD", -1);
		FullscreenActivity.pageturner_AUTOSCROLL = FullscreenActivity.myPreferences.getInt("pageturner_AUTOSCROLL", -1);
		FullscreenActivity.pageturner_METRONOME = FullscreenActivity.myPreferences.getInt("pageturner_METRONOME", -1);

		FullscreenActivity.toggleScrollBeforeSwipe = FullscreenActivity.myPreferences.getString("toggleScrollBeforeSwipe", "Y");
		FullscreenActivity.togglePageButtons = FullscreenActivity.myPreferences.getString("togglePageButtons", "Y");

		FullscreenActivity.alwaysPreferredChordFormat = FullscreenActivity.myPreferences.getString("alwaysPreferredChordFormat", "N");
		
		FullscreenActivity.gesture_doubletap = FullscreenActivity.myPreferences.getString("gesture_doubletap", "2");

		FullscreenActivity.gesture_longpress = FullscreenActivity.myPreferences.getString("gesture_longpress", "1");

		FullscreenActivity.swipeDrawer = FullscreenActivity.myPreferences.getString("swipeDrawer", "Y");

		FullscreenActivity.presenterChords = FullscreenActivity.myPreferences.getString("presenterChords", "N");

		FullscreenActivity.whichMode = FullscreenActivity.myPreferences.getString("whichMode", "Performance");

		FullscreenActivity.backgroundImage1 = FullscreenActivity.myPreferences.getString("backgroundImage1", "ost_bg.png");
		FullscreenActivity.backgroundImage2 = FullscreenActivity.myPreferences.getString("backgroundImage2", "ost_bg.png");
		FullscreenActivity.backgroundVideo1 = FullscreenActivity.myPreferences.getString("backgroundVideo1", "");
		FullscreenActivity.backgroundVideo2 = FullscreenActivity.myPreferences.getString("backgroundVideo2", "");
		FullscreenActivity.backgroundToUse = FullscreenActivity.myPreferences.getString("backgroundToUse", "img1");
		FullscreenActivity.backgroundTypeToUse = FullscreenActivity.myPreferences.getString("backgroundTypeToUse", "image");


		FullscreenActivity.bibleFile = FullscreenActivity.myPreferences.getString("bibleFile", "");

		FullscreenActivity.prefStorage = FullscreenActivity.myPreferences.getString("prefStorage", "");

		FullscreenActivity.autoScrollDelay = FullscreenActivity.myPreferences.getInt("autoScrollDelay", 10);

		FullscreenActivity.metronomepan = FullscreenActivity.myPreferences.getString("metronomepan", "both");
		FullscreenActivity.padpan = FullscreenActivity.myPreferences.getString("padpan", "both");
		FullscreenActivity.metronomevol = FullscreenActivity.myPreferences.getFloat("metronomevol", 0.5f);
		FullscreenActivity.padvol = FullscreenActivity.myPreferences.getFloat("padvol", 1.0f);
		//FullscreenActivity.beatoncolour = FullscreenActivity.myPreferences.getInt("beatoncolour", 0xffaa1212);
		FullscreenActivity.visualmetronome = FullscreenActivity.myPreferences.getBoolean("visualmetronome", false);
		
		FullscreenActivity.chordFormat = FullscreenActivity.myPreferences.getString("chordFormat", "1");

		FullscreenActivity.xmargin_presentation = FullscreenActivity.myPreferences.getInt("xmargin_presentation", 50);
		FullscreenActivity.ymargin_presentation = FullscreenActivity.myPreferences.getInt("ymargin_presentation", 25);

		
		FullscreenActivity.dark_lyricsTextColor = FullscreenActivity.myPreferences.getInt("dark_lyricsTextColor", default_dark_lyricsTextColor);
		FullscreenActivity.dark_lyricsCapoColor = FullscreenActivity.myPreferences.getInt("dark_lyricsCapoColor", default_dark_lyricsCapoColor);
		FullscreenActivity.light_lyricsTextColor = FullscreenActivity.myPreferences.getInt("light_lyricsTextColor", default_light_lyricsTextColor);
		FullscreenActivity.light_lyricsCapoColor = FullscreenActivity.myPreferences.getInt("light_lyricsCapoColor", default_light_lyricsCapoColor);
		FullscreenActivity.dark_lyricsBackgroundColor = FullscreenActivity.myPreferences.getInt("dark_lyricsBackgroundColor", default_dark_lyricsBackgroundColor);
		FullscreenActivity.light_lyricsBackgroundColor = FullscreenActivity.myPreferences.getInt("light_lyricsBackgroundColor", default_light_lyricsBackgroundColor);
		FullscreenActivity.dark_lyricsVerseColor = FullscreenActivity.myPreferences.getInt("dark_lyricsVerseColor", default_dark_lyricsVerseColor);
		FullscreenActivity.light_lyricsVerseColor = FullscreenActivity.myPreferences.getInt("light_lyricsVerseColor", default_light_lyricsVerseColor);
		FullscreenActivity.dark_lyricsChorusColor = FullscreenActivity.myPreferences.getInt("dark_lyricsChorusColor", default_dark_lyricsChorusColor);
		FullscreenActivity.light_lyricsChorusColor = FullscreenActivity.myPreferences.getInt("light_lyricsChorusColor", default_light_lyricsChorusColor);
		FullscreenActivity.dark_lyricsBridgeColor = FullscreenActivity.myPreferences.getInt("dark_lyricsBridgeColor", default_dark_lyricsBridgeColor);
		FullscreenActivity.light_lyricsBridgeColor = FullscreenActivity.myPreferences.getInt("light_lyricsBridgeColor", default_light_lyricsBridgeColor);
		FullscreenActivity.dark_lyricsCommentColor = FullscreenActivity.myPreferences.getInt("dark_lyricsCommentColor", default_dark_lyricsCommentColor);
		FullscreenActivity.light_lyricsCommentColor = FullscreenActivity.myPreferences.getInt("light_lyricsCommentColor", default_light_lyricsCommentColor);
		FullscreenActivity.dark_lyricsPreChorusColor = FullscreenActivity.myPreferences.getInt("dark_lyricsPreChorusColor", default_dark_lyricsPreChorusColor);
		FullscreenActivity.light_lyricsPreChorusColor = FullscreenActivity.myPreferences.getInt("light_lyricsPreChorusColor", default_light_lyricsPreChorusColor);
		FullscreenActivity.dark_lyricsTagColor = FullscreenActivity.myPreferences.getInt("dark_lyricsTagColor", default_dark_lyricsTagColor);
		FullscreenActivity.light_lyricsTagColor = FullscreenActivity.myPreferences.getInt("light_lyricsTagColor", default_light_lyricsTagColor);
		FullscreenActivity.dark_lyricsChordsColor = FullscreenActivity.myPreferences.getInt("dark_lyricsChordsColor", default_dark_lyricsChordsColor);
		FullscreenActivity.light_lyricsChordsColor = FullscreenActivity.myPreferences.getInt("light_lyricsChordsColor", default_light_lyricsChordsColor);
		FullscreenActivity.dark_lyricsCustomColor = FullscreenActivity.myPreferences.getInt("dark_lyricsCustomColor", default_dark_lyricsCustomColor);
		FullscreenActivity.light_lyricsCustomColor = FullscreenActivity.myPreferences.getInt("light_lyricsCustomColor", default_light_lyricsCustomColor);
		FullscreenActivity.dark_metronome = FullscreenActivity.myPreferences.getInt("dark_metronome", default_metronomeColor);
		FullscreenActivity.light_metronome = FullscreenActivity.myPreferences.getInt("light_metronome", default_metronomeColor);

		FullscreenActivity.custom1_lyricsTextColor = FullscreenActivity.myPreferences.getInt("custom1_lyricsTextColor", default_dark_lyricsTextColor);
		FullscreenActivity.custom1_lyricsCapoColor = FullscreenActivity.myPreferences.getInt("custom1_lyricsCapoColor", default_dark_lyricsCapoColor);
		FullscreenActivity.custom2_lyricsTextColor = FullscreenActivity.myPreferences.getInt("custom2_lyricsTextColor", default_light_lyricsTextColor);
		FullscreenActivity.custom2_lyricsCapoColor = FullscreenActivity.myPreferences.getInt("custom2_lyricsCapoColor", default_light_lyricsCapoColor);
		FullscreenActivity.custom1_lyricsBackgroundColor = FullscreenActivity.myPreferences.getInt("custom1_lyricsBackgroundColor", default_dark_lyricsBackgroundColor);
		FullscreenActivity.custom2_lyricsBackgroundColor = FullscreenActivity.myPreferences.getInt("custom2_lyricsBackgroundColor", default_light_lyricsBackgroundColor);
		FullscreenActivity.custom1_lyricsVerseColor = FullscreenActivity.myPreferences.getInt("custom1_lyricsVerseColor", default_dark_lyricsBackgroundColor);
		FullscreenActivity.custom2_lyricsVerseColor = FullscreenActivity.myPreferences.getInt("custom2_lyricsVerseColor", default_light_lyricsBackgroundColor);
		FullscreenActivity.custom1_lyricsChorusColor = FullscreenActivity.myPreferences.getInt("custom1_lyricsChorusColor", default_dark_lyricsBackgroundColor);
		FullscreenActivity.custom2_lyricsChorusColor = FullscreenActivity.myPreferences.getInt("custom2_lyricsChorusColor", default_light_lyricsBackgroundColor);
		FullscreenActivity.custom1_lyricsBridgeColor = FullscreenActivity.myPreferences.getInt("custom1_lyricsBridgeColor", default_dark_lyricsBackgroundColor);
		FullscreenActivity.custom2_lyricsBridgeColor = FullscreenActivity.myPreferences.getInt("custom2_lyricsBridgeColor", default_light_lyricsBackgroundColor);
		FullscreenActivity.custom1_lyricsCommentColor = FullscreenActivity.myPreferences.getInt("custom1_lyricsCommentColor", default_dark_lyricsBackgroundColor);
		FullscreenActivity.custom2_lyricsCommentColor = FullscreenActivity.myPreferences.getInt("custom2_lyricsCommentColor", default_light_lyricsBackgroundColor);
		FullscreenActivity.custom1_lyricsPreChorusColor = FullscreenActivity.myPreferences.getInt("custom1_lyricsPreChorusColor", default_dark_lyricsBackgroundColor);
		FullscreenActivity.custom2_lyricsPreChorusColor = FullscreenActivity.myPreferences.getInt("custom2_lyricsPreChorusColor", default_light_lyricsBackgroundColor);
		FullscreenActivity.custom1_lyricsTagColor = FullscreenActivity.myPreferences.getInt("custom1_lyricsTagColor", default_dark_lyricsBackgroundColor);
		FullscreenActivity.custom2_lyricsTagColor = FullscreenActivity.myPreferences.getInt("custom2_lyricsTagColor", default_light_lyricsBackgroundColor);
		FullscreenActivity.custom1_lyricsChordsColor = FullscreenActivity.myPreferences.getInt("custom1_lyricsChordsColor", default_dark_lyricsChordsColor);
		FullscreenActivity.custom2_lyricsChordsColor = FullscreenActivity.myPreferences.getInt("custom2_lyricsChordsColor", default_light_lyricsChordsColor);
		FullscreenActivity.custom1_lyricsCustomColor = FullscreenActivity.myPreferences.getInt("custom1_lyricsCustomColor", default_dark_lyricsBackgroundColor);
		FullscreenActivity.custom2_lyricsCustomColor = FullscreenActivity.myPreferences.getInt("custom2_lyricsCustomColor", default_light_lyricsBackgroundColor);
		FullscreenActivity.custom1_metronome = FullscreenActivity.myPreferences.getInt("custom1_metronome", default_metronomeColor);
		FullscreenActivity.custom2_metronome = FullscreenActivity.myPreferences.getInt("custom2_metronome", default_metronomeColor);

		FullscreenActivity.mFontSize = FullscreenActivity.myPreferences.getFloat("mFontSize", 42.0f);
        FullscreenActivity.mMaxFontSize = FullscreenActivity.myPreferences.getInt("mMaxFontSize", 50);
		FullscreenActivity.usePresentationOrder = FullscreenActivity.myPreferences.getBoolean("usePresentationOrder",true);

		//Now activity resizes to fit the x scale - option to also fit to the Y scale
		FullscreenActivity.toggleYScale = FullscreenActivity.myPreferences.getString("toggleYScale", "Y");
				
		FullscreenActivity.swipeSet = FullscreenActivity.myPreferences.getString("swipeSet", "Y");

		FullscreenActivity.hideactionbaronoff = FullscreenActivity.myPreferences.getString("hideactionbaronoff", "N");
		
		FullscreenActivity.songfilename = FullscreenActivity.myPreferences.getString("songfilename", "Love everlasting");
		FullscreenActivity.mAuthor = FullscreenActivity.myPreferences.getString("mAuthor", "Gareth Evans");
		FullscreenActivity.mTitle = FullscreenActivity.myPreferences.getString("mTitle", "Love everlasting");
		FullscreenActivity.mCopyright = FullscreenActivity.myPreferences.getString("mCopyright","Copyright 1996 New Life Music Ministries");
		FullscreenActivity.transposeStyle = FullscreenActivity.myPreferences.getString("transposeStyle", "sharps");
		FullscreenActivity.mySet = FullscreenActivity.myPreferences.getString("mySet", "");
		FullscreenActivity.showChords = FullscreenActivity.myPreferences.getString("showChords", "Y");
		FullscreenActivity.mDisplayTheme = FullscreenActivity.myPreferences.getString("mDisplayTheme", "Theme.Holo");
		FullscreenActivity.whichSongFolder = FullscreenActivity.myPreferences.getString("whichSongFolder", FullscreenActivity.mainfoldername);
		
		FullscreenActivity.chordInstrument = FullscreenActivity.myPreferences.getString("chordInstrument", "g");

		// This bit purges old set details and puts in the newer format menu
		// It is done to ensure that menu items are always written at the start of the saved set!
		// Not any more!!!!!!!
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.savethisset+"_**$")) {
			// Old 'Save this set' text.
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.savethisset+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.clearthisset+"_**$")) {
			// Old 'Save this set' text.
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.clearthisset+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.backtooptions+"_**$")) {
			// Old 'Save this set' text.
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.backtooptions+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**__**$")) {
			// Blank entry
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**__**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.set_edit+"_**$")) {
			// Set save
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.set_edit+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.set_save+"_**$")) {
			// Set save
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.set_save+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.set_load+"_**$")) {
			// Set load
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.set_load+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.set_clear+"_**$")) {
			// Set clear
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.set_clear+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.set_export+"_**$")) {
			// menu button
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.set_export+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.menu_menutitle+"_**$")) {
			// menu button
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.menu_menutitle+"_**$", "");
		}
		
	}

	
	
	
	
	public static void savePreferences() {

		Log.d("Preferences", "Saving");

		SharedPreferences.Editor editor = FullscreenActivity.myPreferences.edit();

		editor.putString("capoDisplay", FullscreenActivity.capoDisplay);
		editor.putString("languageToLoad", FullscreenActivity.languageToLoad);
		editor.putInt("mylyricsfontnum", FullscreenActivity.mylyricsfontnum);
		editor.putInt("mychordsfontnum", FullscreenActivity.mychordsfontnum);
		editor.putInt("linespacing", FullscreenActivity.linespacing);

		editor.putInt("pageturner_NEXT", FullscreenActivity.pageturner_NEXT);
		editor.putInt("pageturner_PREVIOUS", FullscreenActivity.pageturner_PREVIOUS);
		editor.putInt("pageturner_UP", FullscreenActivity.pageturner_UP);
		editor.putInt("pageturner_DOWN", FullscreenActivity.pageturner_DOWN);
		editor.putInt("pageturner_PAD", FullscreenActivity.pageturner_PAD);
		editor.putInt("pageturner_AUTOSCROLL", FullscreenActivity.pageturner_AUTOSCROLL);
		editor.putInt("pageturner_METRONOME", FullscreenActivity.pageturner_METRONOME);
		
		editor.putString("toggleScrollBeforeSwipe", FullscreenActivity.toggleScrollBeforeSwipe);
		editor.putString("togglePageButtons", FullscreenActivity.togglePageButtons);

		editor.putString("alwaysPreferredChordFormat", FullscreenActivity.alwaysPreferredChordFormat);

		editor.putString("presenterChords", FullscreenActivity.presenterChords);
		editor.putBoolean("usePresentationOrder", FullscreenActivity.usePresentationOrder);

		editor.putString("backgroundImage1", FullscreenActivity.backgroundImage1);
		editor.putString("backgroundImage2", FullscreenActivity.backgroundImage2);
		editor.putString("backgroundVideo1", FullscreenActivity.backgroundVideo1);
		editor.putString("backgroundVideo2", FullscreenActivity.backgroundVideo2);
		editor.putString("backgroundToUse", FullscreenActivity.backgroundToUse);
		editor.putString("backgroundTypeToUse", FullscreenActivity.backgroundTypeToUse);

		editor.putString("bibleFile", FullscreenActivity.bibleFile);
		editor.putString("prefStorage", FullscreenActivity.prefStorage);

		editor.putString("whichMode", FullscreenActivity.whichMode);

		editor.putString("chordFormat", FullscreenActivity.chordFormat);

		editor.putInt("autoScrollDelay", FullscreenActivity.autoScrollDelay);

		editor.putString("metronomepan", FullscreenActivity.metronomepan);
		editor.putString("padpan", FullscreenActivity.padpan);
		editor.putFloat("metronomevol", FullscreenActivity.metronomevol);
		editor.putFloat("padvol", FullscreenActivity.padvol);
		//editor.putInt("beatoncolour", FullscreenActivity.beatoncolour);
		editor.putBoolean("visualmetronome", FullscreenActivity.visualmetronome);
		
		editor.putInt("xmargin_presentation", FullscreenActivity.xmargin_presentation);
		editor.putInt("ymargin_presentation", FullscreenActivity.ymargin_presentation);

		editor.putInt("dark_lyricsTextColor", FullscreenActivity.dark_lyricsTextColor);
		editor.putInt("dark_lyricsCapoColor", FullscreenActivity.dark_lyricsCapoColor);
		editor.putInt("dark_lyricsBackgroundColor", FullscreenActivity.dark_lyricsBackgroundColor);
		editor.putInt("dark_lyricsVerseColor", FullscreenActivity.dark_lyricsVerseColor);
		editor.putInt("dark_lyricsChorusColor", FullscreenActivity.dark_lyricsChorusColor);
		editor.putInt("dark_lyricsBridgeColor", FullscreenActivity.dark_lyricsBridgeColor);
		editor.putInt("dark_lyricsCommentColor", FullscreenActivity.dark_lyricsCommentColor);
		editor.putInt("dark_lyricsPreChorusColor", FullscreenActivity.dark_lyricsPreChorusColor);
		editor.putInt("dark_lyricsTagColor", FullscreenActivity.dark_lyricsTagColor);
		editor.putInt("dark_lyricsChordsColor", FullscreenActivity.dark_lyricsChordsColor);
		editor.putInt("dark_lyricsCustomColor", FullscreenActivity.dark_lyricsCustomColor);
		editor.putInt("dark_metronome", FullscreenActivity.dark_metronome);

		editor.putInt("light_lyricsTextColor", FullscreenActivity.light_lyricsTextColor);
		editor.putInt("light_lyricsCapoColor", FullscreenActivity.light_lyricsCapoColor);
		editor.putInt("light_lyricsBackgroundColor", FullscreenActivity.light_lyricsBackgroundColor);
		editor.putInt("light_lyricsVerseColor", FullscreenActivity.light_lyricsVerseColor);
		editor.putInt("light_lyricsChorusColor", FullscreenActivity.light_lyricsChorusColor);
		editor.putInt("light_lyricsBridgeColor", FullscreenActivity.light_lyricsBridgeColor);
		editor.putInt("light_lyricsCommentColor", FullscreenActivity.light_lyricsCommentColor);
		editor.putInt("light_lyricsPreChorusColor", FullscreenActivity.light_lyricsPreChorusColor);
		editor.putInt("light_lyricsTagColor", FullscreenActivity.light_lyricsTagColor);
		editor.putInt("light_lyricsChordsColor", FullscreenActivity.light_lyricsChordsColor);
		editor.putInt("light_lyricsCustomColor", FullscreenActivity.light_lyricsCustomColor);
		editor.putInt("light_metronome", FullscreenActivity.light_metronome);

		editor.putInt("custom1_lyricsTextColor", FullscreenActivity.custom1_lyricsTextColor);
		editor.putInt("custom1_lyricsCapoColor", FullscreenActivity.custom1_lyricsCapoColor);
		editor.putInt("custom1_lyricsBackgroundColor", FullscreenActivity.custom1_lyricsBackgroundColor);
		editor.putInt("custom1_lyricsVerseColor", FullscreenActivity.custom1_lyricsVerseColor);
		editor.putInt("custom1_lyricsChorusColor", FullscreenActivity.custom1_lyricsChorusColor);
		editor.putInt("custom1_lyricsBridgeColor", FullscreenActivity.custom1_lyricsBridgeColor);
		editor.putInt("custom1_lyricsCommentColor", FullscreenActivity.custom1_lyricsCommentColor);
		editor.putInt("custom1_lyricsPreChorusColor", FullscreenActivity.custom1_lyricsPreChorusColor);
		editor.putInt("custom1_lyricsTagColor", FullscreenActivity.custom1_lyricsTagColor);
		editor.putInt("custom1_lyricsChordsColor", FullscreenActivity.custom1_lyricsChordsColor);
		editor.putInt("custom1_lyricsCustomColor", FullscreenActivity.custom1_lyricsCustomColor);
		editor.putInt("custom1_metronome", FullscreenActivity.custom1_metronome);

		editor.putInt("custom2_lyricsTextColor", FullscreenActivity.custom2_lyricsTextColor);
		editor.putInt("custom2_lyricsCapoColor", FullscreenActivity.custom2_lyricsCapoColor);
		editor.putInt("custom2_lyricsBackgroundColor", FullscreenActivity.custom2_lyricsBackgroundColor);
		editor.putInt("custom2_lyricsVerseColor", FullscreenActivity.custom2_lyricsVerseColor);
		editor.putInt("custom2_lyricsChorusColor", FullscreenActivity.custom2_lyricsChorusColor);
		editor.putInt("custom2_lyricsBridgeColor", FullscreenActivity.custom2_lyricsBridgeColor);
		editor.putInt("custom2_lyricsCommentColor", FullscreenActivity.custom2_lyricsCommentColor);
		editor.putInt("custom2_lyricsPreChorusColor", FullscreenActivity.custom2_lyricsPreChorusColor);
		editor.putInt("custom2_lyricsTagColor", FullscreenActivity.custom2_lyricsTagColor);
		editor.putInt("custom2_lyricsChordsColor", FullscreenActivity.custom2_lyricsChordsColor);
		editor.putInt("custom2_lyricsCustomColor", FullscreenActivity.custom2_lyricsCustomColor);
		editor.putInt("custom2_metronome", FullscreenActivity.custom2_metronome);

        editor.putString("chordInstrument", FullscreenActivity.chordInstrument);

        editor.putString("showNextInSet", FullscreenActivity.showNextInSet);

		editor.putString("hideactionbaronoff", FullscreenActivity.hideactionbaronoff);
		editor.putString("mStorage", FullscreenActivity.mStorage);
		editor.putFloat("mFontSize", FullscreenActivity.mFontSize);
        editor.putInt("mMaxFontSize", FullscreenActivity.mMaxFontSize);
		editor.putString("toggleYScale", FullscreenActivity.toggleYScale);
		editor.putString("swipeSet", FullscreenActivity.swipeSet);
		editor.putString("swipeDrawer", FullscreenActivity.swipeDrawer);
		editor.putString("songfilename", FullscreenActivity.songfilename);
		editor.putString("mAuthor", FullscreenActivity.mAuthor.toString());
		editor.putString("mCopyright", FullscreenActivity.mCopyright.toString());
		editor.putString("mTitle", FullscreenActivity.mTitle.toString());
		editor.putString("transposeStyle", FullscreenActivity.transposeStyle);
		editor.putString("showChords", FullscreenActivity.showChords);
		editor.putString("mDisplayTheme", FullscreenActivity.mDisplayTheme);
		editor.putString("whichSongFolder", FullscreenActivity.whichSongFolder);
		editor.putString("gesture_doubletap", FullscreenActivity.gesture_doubletap);
		editor.putString("gesture_longpress", FullscreenActivity.gesture_longpress);
		

		//Strip out any old menu items from the set
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.savethisset+"_**$")) {
			// Old 'Save this set' text.
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.savethisset+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.clearthisset+"_**$")) {
			// Old 'Save this set' text.
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.clearthisset+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.backtooptions+"_**$")) {
			// Old 'Save this set' text.
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.backtooptions+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**__**$")) {
			// Blank entry
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**__**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.set_edit+"_**$")) {
			// Set edit
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.set_edit+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.set_save+"_**$")) {
			// Set save
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.set_save+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.set_load+"_**$")) {
			// Set load
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.set_load+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.set_clear+"_**$")) {
			// Set clear
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.set_clear+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.set_export+"_**$")) {
			// Set clear
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.set_export+"_**$", "");
		}
		if (FullscreenActivity.mySet.contains("$**_"+FullscreenActivity.set_menutitle+"_**$")) {
			// menu button
			FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**_"+FullscreenActivity.set_menutitle+"_**$", "");
		}

		// Save the set without the menus
		editor.putString("mySet", FullscreenActivity.mySet);
		editor.commit();
		
	}

}
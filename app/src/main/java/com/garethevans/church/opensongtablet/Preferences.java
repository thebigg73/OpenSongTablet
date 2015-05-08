package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import static com.garethevans.church.opensongtablet.FullscreenActivity.*;

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

		presoAutoScale = myPreferences.getBoolean("presoAutoScale", true);
		presoFontSize = myPreferences.getInt("presoFontSize", 4);
		presoShowChords = myPreferences.getBoolean("presoShowChords", false);
		xmargin_presentation = myPreferences.getInt("xmargin_presentation", 50);
		ymargin_presentation = myPreferences.getInt("ymargin_presentation", 25);
		myAlert = myPreferences.getString("myAlert","");
        showNextInSet = myPreferences.getString("showNextInSet","bottom");
		capoDisplay = myPreferences.getString("capoDisplay", "both");
		languageToLoad = myPreferences.getString("languageToLoad", "");
		mylyricsfontnum = myPreferences.getInt("mylyricsfontnum", 7);
		mychordsfontnum = myPreferences.getInt("mychordsfontnum", 8);
		linespacing = myPreferences.getInt("linespacing", 0);
		
		pageturner_NEXT = myPreferences.getInt("pageturner_NEXT", 22);
		pageturner_PREVIOUS = myPreferences.getInt("pageturner_PREVIOUS", 21);
		pageturner_UP = myPreferences.getInt("pageturner_UP", 19);
		pageturner_DOWN = myPreferences.getInt("pageturner_DOWN", 20);
		pageturner_PAD = myPreferences.getInt("pageturner_PAD", -1);
		pageturner_AUTOSCROLL = myPreferences.getInt("pageturner_AUTOSCROLL", -1);
		pageturner_METRONOME = myPreferences.getInt("pageturner_METRONOME", -1);

		toggleScrollBeforeSwipe = myPreferences.getString("toggleScrollBeforeSwipe", "Y");
		togglePageButtons = myPreferences.getString("togglePageButtons", "Y");

		alwaysPreferredChordFormat = myPreferences.getString("alwaysPreferredChordFormat", "N");
		
		gesture_doubletap = myPreferences.getString("gesture_doubletap", "2");

		gesture_longpress = myPreferences.getString("gesture_longpress", "1");

		swipeDrawer = myPreferences.getString("swipeDrawer", "Y");

		presenterChords = myPreferences.getString("presenterChords", "N");

		whichMode = myPreferences.getString("whichMode", "Performance");

		backgroundImage1 = myPreferences.getString("backgroundImage1", "ost_bg.png");
		backgroundImage2 = myPreferences.getString("backgroundImage2", "ost_bg.png");
		backgroundVideo1 = myPreferences.getString("backgroundVideo1", "");
		backgroundVideo2 = myPreferences.getString("backgroundVideo2", "");
		backgroundToUse = myPreferences.getString("backgroundToUse", "img1");
		backgroundTypeToUse = myPreferences.getString("backgroundTypeToUse", "image");

		bibleFile = myPreferences.getString("bibleFile", "");

		prefStorage = myPreferences.getString("prefStorage", "");

		autoScrollDelay = myPreferences.getInt("autoScrollDelay", 10);

		metronomepan = myPreferences.getString("metronomepan", "both");
		padpan = myPreferences.getString("padpan", "both");
		metronomevol = myPreferences.getFloat("metronomevol", 0.5f);
		padvol = myPreferences.getFloat("padvol", 1.0f);
		visualmetronome = myPreferences.getBoolean("visualmetronome", false);
		
		chordFormat = myPreferences.getString("chordFormat", "1");

		dark_lyricsTextColor = myPreferences.getInt("dark_lyricsTextColor", default_dark_lyricsTextColor);
		dark_lyricsCapoColor = myPreferences.getInt("dark_lyricsCapoColor", default_dark_lyricsCapoColor);
		light_lyricsTextColor = myPreferences.getInt("light_lyricsTextColor", default_light_lyricsTextColor);
		light_lyricsCapoColor = myPreferences.getInt("light_lyricsCapoColor", default_light_lyricsCapoColor);
		dark_lyricsBackgroundColor = myPreferences.getInt("dark_lyricsBackgroundColor", default_dark_lyricsBackgroundColor);
		light_lyricsBackgroundColor = myPreferences.getInt("light_lyricsBackgroundColor", default_light_lyricsBackgroundColor);
		dark_lyricsVerseColor = myPreferences.getInt("dark_lyricsVerseColor", default_dark_lyricsVerseColor);
		light_lyricsVerseColor = myPreferences.getInt("light_lyricsVerseColor", default_light_lyricsVerseColor);
		dark_lyricsChorusColor = myPreferences.getInt("dark_lyricsChorusColor", default_dark_lyricsChorusColor);
		light_lyricsChorusColor = myPreferences.getInt("light_lyricsChorusColor", default_light_lyricsChorusColor);
		dark_lyricsBridgeColor = myPreferences.getInt("dark_lyricsBridgeColor", default_dark_lyricsBridgeColor);
		light_lyricsBridgeColor = myPreferences.getInt("light_lyricsBridgeColor", default_light_lyricsBridgeColor);
		dark_lyricsCommentColor = myPreferences.getInt("dark_lyricsCommentColor", default_dark_lyricsCommentColor);
		light_lyricsCommentColor = myPreferences.getInt("light_lyricsCommentColor", default_light_lyricsCommentColor);
		dark_lyricsPreChorusColor = myPreferences.getInt("dark_lyricsPreChorusColor", default_dark_lyricsPreChorusColor);
		light_lyricsPreChorusColor = myPreferences.getInt("light_lyricsPreChorusColor", default_light_lyricsPreChorusColor);
		dark_lyricsTagColor = myPreferences.getInt("dark_lyricsTagColor", default_dark_lyricsTagColor);
		light_lyricsTagColor = myPreferences.getInt("light_lyricsTagColor", default_light_lyricsTagColor);
		dark_lyricsChordsColor = myPreferences.getInt("dark_lyricsChordsColor", default_dark_lyricsChordsColor);
		light_lyricsChordsColor = myPreferences.getInt("light_lyricsChordsColor", default_light_lyricsChordsColor);
		dark_lyricsCustomColor = myPreferences.getInt("dark_lyricsCustomColor", default_dark_lyricsCustomColor);
		light_lyricsCustomColor = myPreferences.getInt("light_lyricsCustomColor", default_light_lyricsCustomColor);
		dark_metronome = myPreferences.getInt("dark_metronome", default_metronomeColor);
		light_metronome = myPreferences.getInt("light_metronome", default_metronomeColor);

		custom1_lyricsTextColor = myPreferences.getInt("custom1_lyricsTextColor", default_dark_lyricsTextColor);
		custom1_lyricsCapoColor = myPreferences.getInt("custom1_lyricsCapoColor", default_dark_lyricsCapoColor);
		custom2_lyricsTextColor = myPreferences.getInt("custom2_lyricsTextColor", default_light_lyricsTextColor);
		custom2_lyricsCapoColor = myPreferences.getInt("custom2_lyricsCapoColor", default_light_lyricsCapoColor);
		custom1_lyricsBackgroundColor = myPreferences.getInt("custom1_lyricsBackgroundColor", default_dark_lyricsBackgroundColor);
		custom2_lyricsBackgroundColor = myPreferences.getInt("custom2_lyricsBackgroundColor", default_light_lyricsBackgroundColor);
		custom1_lyricsVerseColor = myPreferences.getInt("custom1_lyricsVerseColor", default_dark_lyricsBackgroundColor);
		custom2_lyricsVerseColor = myPreferences.getInt("custom2_lyricsVerseColor", default_light_lyricsBackgroundColor);
		custom1_lyricsChorusColor = myPreferences.getInt("custom1_lyricsChorusColor", default_dark_lyricsBackgroundColor);
		custom2_lyricsChorusColor = myPreferences.getInt("custom2_lyricsChorusColor", default_light_lyricsBackgroundColor);
		custom1_lyricsBridgeColor = myPreferences.getInt("custom1_lyricsBridgeColor", default_dark_lyricsBackgroundColor);
		custom2_lyricsBridgeColor = myPreferences.getInt("custom2_lyricsBridgeColor", default_light_lyricsBackgroundColor);
		custom1_lyricsCommentColor = myPreferences.getInt("custom1_lyricsCommentColor", default_dark_lyricsBackgroundColor);
		custom2_lyricsCommentColor = myPreferences.getInt("custom2_lyricsCommentColor", default_light_lyricsBackgroundColor);
		custom1_lyricsPreChorusColor = myPreferences.getInt("custom1_lyricsPreChorusColor", default_dark_lyricsBackgroundColor);
		custom2_lyricsPreChorusColor = myPreferences.getInt("custom2_lyricsPreChorusColor", default_light_lyricsBackgroundColor);
		custom1_lyricsTagColor = myPreferences.getInt("custom1_lyricsTagColor", default_dark_lyricsBackgroundColor);
		custom2_lyricsTagColor = myPreferences.getInt("custom2_lyricsTagColor", default_light_lyricsBackgroundColor);
		custom1_lyricsChordsColor = myPreferences.getInt("custom1_lyricsChordsColor", default_dark_lyricsChordsColor);
		custom2_lyricsChordsColor = myPreferences.getInt("custom2_lyricsChordsColor", default_light_lyricsChordsColor);
		custom1_lyricsCustomColor = myPreferences.getInt("custom1_lyricsCustomColor", default_dark_lyricsBackgroundColor);
		custom2_lyricsCustomColor = myPreferences.getInt("custom2_lyricsCustomColor", default_light_lyricsBackgroundColor);
		custom1_metronome = myPreferences.getInt("custom1_metronome", default_metronomeColor);
		custom2_metronome = myPreferences.getInt("custom2_metronome", default_metronomeColor);

		mFontSize = myPreferences.getFloat("mFontSize", 42.0f);
        mMaxFontSize = myPreferences.getInt("mMaxFontSize", 50);
		usePresentationOrder = myPreferences.getBoolean("usePresentationOrder",false);

		//Now activity resizes to fit the x scale - option to also fit to the Y scale
		toggleYScale = myPreferences.getString("toggleYScale", "Y");
				
		swipeSet = myPreferences.getString("swipeSet", "Y");

		hideactionbaronoff = myPreferences.getString("hideactionbaronoff", "N");
		
		songfilename = myPreferences.getString("songfilename", "Love everlasting");
		mAuthor = myPreferences.getString("mAuthor", "Gareth Evans");
		mTitle = myPreferences.getString("mTitle", "Love everlasting");
		mCopyright = myPreferences.getString("mCopyright","Copyright 1996 New Life Music Ministries");
		transposeStyle = myPreferences.getString("transposeStyle", "sharps");
		mySet = myPreferences.getString("mySet", "");
		showChords = myPreferences.getString("showChords", "Y");
		mDisplayTheme = myPreferences.getString("mDisplayTheme", "Theme.Holo");
		whichSongFolder = myPreferences.getString("whichSongFolder", mainfoldername);
		
		chordInstrument = myPreferences.getString("chordInstrument", "g");

		// This bit purges old set details and puts in the newer format menu
		// It is done to ensure that menu items are always written at the start of the saved set!
		// Not any more!!!!!!!
		if (mySet.contains("$**_"+ savethisset+"_**$")) {
			// Old 'Save this set' text.
			mySet = mySet.replace("$**_"+ savethisset+"_**$", "");
		}
		if (mySet.contains("$**_"+ clearthisset+"_**$")) {
			// Old 'Save this set' text.
			mySet = mySet.replace("$**_"+ clearthisset+"_**$", "");
		}
		if (mySet.contains("$**_"+ backtooptions+"_**$")) {
			// Old 'Save this set' text.
			mySet = mySet.replace("$**_"+ backtooptions+"_**$", "");
		}
		if (mySet.contains("$**__**$")) {
			// Blank entry
			mySet = mySet.replace("$**__**$", "");
		}
		if (mySet.contains("$**_"+ set_edit+"_**$")) {
			// Set save
			mySet = mySet.replace("$**_"+ set_edit+"_**$", "");
		}
		if (mySet.contains("$**_"+ set_save+"_**$")) {
			// Set save
			mySet = mySet.replace("$**_"+ set_save+"_**$", "");
		}
		if (mySet.contains("$**_"+ set_load+"_**$")) {
			// Set load
			mySet = mySet.replace("$**_"+ set_load+"_**$", "");
		}
		if (mySet.contains("$**_"+ set_clear+"_**$")) {
			// Set clear
			mySet = mySet.replace("$**_"+ set_clear+"_**$", "");
		}
		if (mySet.contains("$**_"+ set_export+"_**$")) {
			// menu button
			mySet = mySet.replace("$**_"+ set_export+"_**$", "");
		}
		if (mySet.contains("$**_"+ menu_menutitle+"_**$")) {
			// menu button
			mySet = mySet.replace("$**_"+ menu_menutitle+"_**$", "");
		}
		
	}

	public static void savePreferences() {

		Log.d("Preferences", "Saving");

		SharedPreferences.Editor editor = myPreferences.edit();

		editor.putBoolean("presoAutoScale", FullscreenActivity.presoAutoScale);
		editor.putInt("presoFontSize", FullscreenActivity.presoFontSize);
		editor.putBoolean("presoShowChords", FullscreenActivity.presoShowChords);

		editor.putString("myAlert", myAlert);
		editor.putString("capoDisplay", capoDisplay);
		editor.putString("languageToLoad", languageToLoad);
		editor.putInt("mylyricsfontnum", mylyricsfontnum);
		editor.putInt("mychordsfontnum", mychordsfontnum);
		editor.putInt("linespacing", linespacing);

		editor.putInt("pageturner_NEXT", pageturner_NEXT);
		editor.putInt("pageturner_PREVIOUS", pageturner_PREVIOUS);
		editor.putInt("pageturner_UP", pageturner_UP);
		editor.putInt("pageturner_DOWN", pageturner_DOWN);
		editor.putInt("pageturner_PAD", pageturner_PAD);
		editor.putInt("pageturner_AUTOSCROLL", pageturner_AUTOSCROLL);
		editor.putInt("pageturner_METRONOME", pageturner_METRONOME);
		
		editor.putString("toggleScrollBeforeSwipe", toggleScrollBeforeSwipe);
		editor.putString("togglePageButtons", togglePageButtons);

		editor.putString("alwaysPreferredChordFormat", alwaysPreferredChordFormat);

		editor.putString("presenterChords", presenterChords);
		editor.putBoolean("usePresentationOrder", usePresentationOrder);

		editor.putString("backgroundImage1", backgroundImage1);
		editor.putString("backgroundImage2", backgroundImage2);
		editor.putString("backgroundVideo1", backgroundVideo1);
		editor.putString("backgroundVideo2", backgroundVideo2);
		editor.putString("backgroundToUse", backgroundToUse);
		editor.putString("backgroundTypeToUse", backgroundTypeToUse);

		editor.putString("bibleFile", bibleFile);
		editor.putString("prefStorage", prefStorage);

		editor.putString("whichMode", whichMode);

		editor.putString("chordFormat", chordFormat);

		editor.putInt("autoScrollDelay", autoScrollDelay);

		editor.putString("metronomepan", metronomepan);
		editor.putString("padpan", padpan);
		editor.putFloat("metronomevol", metronomevol);
		editor.putFloat("padvol", padvol);
		//editor.putInt("beatoncolour", FullscreenActivity.beatoncolour);
		editor.putBoolean("visualmetronome", visualmetronome);
		
		editor.putInt("xmargin_presentation", xmargin_presentation);
		editor.putInt("ymargin_presentation", ymargin_presentation);

		editor.putInt("dark_lyricsTextColor", dark_lyricsTextColor);
		editor.putInt("dark_lyricsCapoColor", dark_lyricsCapoColor);
		editor.putInt("dark_lyricsBackgroundColor", dark_lyricsBackgroundColor);
		editor.putInt("dark_lyricsVerseColor", dark_lyricsVerseColor);
		editor.putInt("dark_lyricsChorusColor", dark_lyricsChorusColor);
		editor.putInt("dark_lyricsBridgeColor", dark_lyricsBridgeColor);
		editor.putInt("dark_lyricsCommentColor", dark_lyricsCommentColor);
		editor.putInt("dark_lyricsPreChorusColor", dark_lyricsPreChorusColor);
		editor.putInt("dark_lyricsTagColor", dark_lyricsTagColor);
		editor.putInt("dark_lyricsChordsColor", dark_lyricsChordsColor);
		editor.putInt("dark_lyricsCustomColor", dark_lyricsCustomColor);
		editor.putInt("dark_metronome", dark_metronome);

		editor.putInt("light_lyricsTextColor", light_lyricsTextColor);
		editor.putInt("light_lyricsCapoColor", light_lyricsCapoColor);
		editor.putInt("light_lyricsBackgroundColor", light_lyricsBackgroundColor);
		editor.putInt("light_lyricsVerseColor", light_lyricsVerseColor);
		editor.putInt("light_lyricsChorusColor", light_lyricsChorusColor);
		editor.putInt("light_lyricsBridgeColor", light_lyricsBridgeColor);
		editor.putInt("light_lyricsCommentColor", light_lyricsCommentColor);
		editor.putInt("light_lyricsPreChorusColor", light_lyricsPreChorusColor);
		editor.putInt("light_lyricsTagColor", light_lyricsTagColor);
		editor.putInt("light_lyricsChordsColor", light_lyricsChordsColor);
		editor.putInt("light_lyricsCustomColor", light_lyricsCustomColor);
		editor.putInt("light_metronome", light_metronome);

		editor.putInt("custom1_lyricsTextColor", custom1_lyricsTextColor);
		editor.putInt("custom1_lyricsCapoColor", custom1_lyricsCapoColor);
		editor.putInt("custom1_lyricsBackgroundColor", custom1_lyricsBackgroundColor);
		editor.putInt("custom1_lyricsVerseColor", custom1_lyricsVerseColor);
		editor.putInt("custom1_lyricsChorusColor", custom1_lyricsChorusColor);
		editor.putInt("custom1_lyricsBridgeColor", custom1_lyricsBridgeColor);
		editor.putInt("custom1_lyricsCommentColor", custom1_lyricsCommentColor);
		editor.putInt("custom1_lyricsPreChorusColor", custom1_lyricsPreChorusColor);
		editor.putInt("custom1_lyricsTagColor", custom1_lyricsTagColor);
		editor.putInt("custom1_lyricsChordsColor", custom1_lyricsChordsColor);
		editor.putInt("custom1_lyricsCustomColor", custom1_lyricsCustomColor);
		editor.putInt("custom1_metronome", custom1_metronome);

		editor.putInt("custom2_lyricsTextColor", custom2_lyricsTextColor);
		editor.putInt("custom2_lyricsCapoColor", custom2_lyricsCapoColor);
		editor.putInt("custom2_lyricsBackgroundColor", custom2_lyricsBackgroundColor);
		editor.putInt("custom2_lyricsVerseColor", custom2_lyricsVerseColor);
		editor.putInt("custom2_lyricsChorusColor", custom2_lyricsChorusColor);
		editor.putInt("custom2_lyricsBridgeColor", custom2_lyricsBridgeColor);
		editor.putInt("custom2_lyricsCommentColor", custom2_lyricsCommentColor);
		editor.putInt("custom2_lyricsPreChorusColor", custom2_lyricsPreChorusColor);
		editor.putInt("custom2_lyricsTagColor", custom2_lyricsTagColor);
		editor.putInt("custom2_lyricsChordsColor", custom2_lyricsChordsColor);
		editor.putInt("custom2_lyricsCustomColor", custom2_lyricsCustomColor);
		editor.putInt("custom2_metronome", custom2_metronome);

        editor.putString("chordInstrument", chordInstrument);

        editor.putString("showNextInSet", showNextInSet);

		editor.putString("hideactionbaronoff", hideactionbaronoff);
		editor.putString("mStorage", mStorage);
		editor.putFloat("mFontSize", mFontSize);
        editor.putInt("mMaxFontSize", mMaxFontSize);
		editor.putString("toggleYScale", toggleYScale);
		editor.putString("swipeSet", swipeSet);
		editor.putString("swipeDrawer", swipeDrawer);
		editor.putString("songfilename", songfilename);
		editor.putString("mAuthor", mAuthor.toString());
		editor.putString("mCopyright", mCopyright.toString());
		editor.putString("mTitle", mTitle.toString());
		editor.putString("transposeStyle", transposeStyle);
		editor.putString("showChords", showChords);
		editor.putString("mDisplayTheme", mDisplayTheme);
		editor.putString("whichSongFolder", whichSongFolder);
		editor.putString("gesture_doubletap", gesture_doubletap);
		editor.putString("gesture_longpress", gesture_longpress);
		

		//Strip out any old menu items from the set
		if (mySet.contains("$**_"+ savethisset+"_**$")) {
			// Old 'Save this set' text.
			mySet = mySet.replace("$**_"+ savethisset+"_**$", "");
		}
		if (mySet.contains("$**_"+ clearthisset+"_**$")) {
			// Old 'Save this set' text.
			mySet = mySet.replace("$**_"+ clearthisset+"_**$", "");
		}
		if (mySet.contains("$**_"+ backtooptions+"_**$")) {
			// Old 'Save this set' text.
			mySet = mySet.replace("$**_"+ backtooptions+"_**$", "");
		}
		if (mySet.contains("$**__**$")) {
			// Blank entry
			mySet = mySet.replace("$**__**$", "");
		}
		if (mySet.contains("$**_"+ set_edit+"_**$")) {
			// Set edit
			mySet = mySet.replace("$**_"+ set_edit+"_**$", "");
		}
		if (mySet.contains("$**_"+ set_save+"_**$")) {
			// Set save
			mySet = mySet.replace("$**_"+ set_save+"_**$", "");
		}
		if (mySet.contains("$**_"+ set_load+"_**$")) {
			// Set load
			mySet = mySet.replace("$**_"+ set_load+"_**$", "");
		}
		if (mySet.contains("$**_"+ set_clear+"_**$")) {
			// Set clear
			mySet = mySet.replace("$**_"+ set_clear+"_**$", "");
		}
		if (mySet.contains("$**_"+ set_export+"_**$")) {
			// Set clear
			mySet = mySet.replace("$**_"+ set_export+"_**$", "");
		}
		if (mySet.contains("$**_"+ set_menutitle+"_**$")) {
			// menu button
			mySet = mySet.replace("$**_"+ set_menutitle+"_**$", "");
		}

		// Save the set without the menus
		editor.putString("mySet", mySet);
		editor.commit();
		
	}

}
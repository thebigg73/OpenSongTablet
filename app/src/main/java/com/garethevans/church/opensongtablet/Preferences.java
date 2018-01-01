package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;

import java.util.Locale;

import static com.garethevans.church.opensongtablet.FullscreenActivity.myPreferences;

public class Preferences extends Activity {

    // Set the default colours here
    static int default_metronomeColor = 0xffaa1212;
    static int default_pagebuttonsColor = 0xff452277;
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
    static int default_dark_presoFontColor = 0xffffffff;
    static int default_dark_presoInfoFontColor = 0xffffffff;
    static int default_dark_presoAlertColor = 0xffff0000;
    static int default_dark_presoShadowFontColor = 0xff000000;

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

    static int default_stickyNotes = 0xff000000;
    static int default_stickyNotesBG = 0xffddaa00;

    public static void loadFolderName() {
        try {
            FullscreenActivity.whichSongFolder = myPreferences.getString("whichSongFolder", FullscreenActivity.mainfoldername);
        } catch (Exception e) {
            FullscreenActivity.whichSongFolder = "MAIN";
        }
    }

    public static Locale getStoredLocale() {
        try {
            String locale = myPreferences.getString("locale", getDefaultLocaleString().toString());
            if (locale == null || locale.isEmpty() || locale.equals("")) {
                locale = getDefaultLocaleString().toString();
            }
            return new Locale(locale);
        } catch (Exception e) {
            e.printStackTrace();
            return new Locale("en");
        }
    }

    public static Locale getDefaultLocaleString() {
        Locale locale = Locale.getDefault();
        if  (locale==null) {
            locale = new Locale(Locale.getDefault().getDisplayLanguage());
        }

        if (!locale.toString().equals("af") && !locale.toString().equals("cz") && !locale.toString().equals("de") &&
                !locale.toString().equals("el") && !locale.toString().equals("es") && !locale.toString().equals("fr") &&
                !locale.toString().equals("hu") && !locale.toString().equals("it") && !locale.toString().equals("ja") &&
                !locale.toString().equals("pl") && !locale.toString().equals("pt") && !locale.toString().equals("ru") &&
                !locale.toString().equals("sr") && !locale.toString().equals("sv") && !locale.toString().equals("zh")) {
            locale = new Locale("en");
        }
        return locale;
    }

    public static void loadPreferences() {
        // Load up the user preferences
        // Set to blank if not used before
        try {
            FullscreenActivity.ab_titleSize = myPreferences.getFloat("ab_titleSize", 13.0f);
            FullscreenActivity.ab_authorSize = myPreferences.getFloat("ab_authorSize", 11.0f);
            FullscreenActivity.alphabeticalSize = myPreferences.getFloat("alphabeticalSize", 14.0f);
            FullscreenActivity.alwaysPreferredChordFormat = myPreferences.getString("alwaysPreferredChordFormat", "N");
            FullscreenActivity.autoProject = myPreferences.getBoolean("autoProject", false);
            FullscreenActivity.autoscroll_default_or_prompt = myPreferences.getString("autoscroll_default_or_prompt", "prompt");
            FullscreenActivity.autoScrollDelay = myPreferences.getInt("autoScrollDelay", 10);
            FullscreenActivity.autostartautoscroll = myPreferences.getBoolean("autostartautoscroll", false);
            FullscreenActivity.backgroundImage1 = myPreferences.getString("backgroundImage1", "ost_bg.png");
            FullscreenActivity.backgroundImage2 = myPreferences.getString("backgroundImage2", "ost_bg.png");
            FullscreenActivity.backgroundVideo1 = myPreferences.getString("backgroundVideo1", "");
            FullscreenActivity.backgroundVideo2 = myPreferences.getString("backgroundVideo2", "");
            FullscreenActivity.backgroundToUse = myPreferences.getString("backgroundToUse", "img1");
            FullscreenActivity.backgroundTypeToUse = myPreferences.getString("backgroundTypeToUse", "image");
            FullscreenActivity.batteryDialOn = myPreferences.getBoolean("batteryDialOn", true);
            FullscreenActivity.batteryLine = myPreferences.getInt("batteryLine", 4);
            FullscreenActivity.batteryOn = myPreferences.getBoolean("batteryOn", true);
            FullscreenActivity.batterySize = myPreferences.getFloat("batterySize", 9.0f);
            FullscreenActivity.bibleFile = myPreferences.getString("bibleFile", "");
            FullscreenActivity.capoDisplay = myPreferences.getString("capoDisplay", "both");
            FullscreenActivity.chordfontscalesize = myPreferences.getFloat("chordfontscalesize", 0.8f);
            FullscreenActivity.chordFormat = myPreferences.getString("chordFormat", "1");
            FullscreenActivity.chordInstrument = myPreferences.getString("chordInstrument", "g");
            FullscreenActivity.commentfontscalesize = myPreferences.getFloat("commentfontscalesize", 0.8f);
            FullscreenActivity.custom1_lyricsBackgroundColor = myPreferences.getInt("custom1_lyricsBackgroundColor", default_dark_lyricsBackgroundColor);
            FullscreenActivity.custom1_lyricsBridgeColor = myPreferences.getInt("custom1_lyricsBridgeColor", default_dark_lyricsBackgroundColor);
            FullscreenActivity.custom1_lyricsCapoColor = myPreferences.getInt("custom1_lyricsCapoColor", default_dark_lyricsCapoColor);
            FullscreenActivity.custom1_lyricsChordsColor = myPreferences.getInt("custom1_lyricsChordsColor", default_dark_lyricsChordsColor);
            FullscreenActivity.custom1_lyricsChorusColor = myPreferences.getInt("custom1_lyricsChorusColor", default_dark_lyricsBackgroundColor);
            FullscreenActivity.custom1_lyricsCommentColor = myPreferences.getInt("custom1_lyricsCommentColor", default_dark_lyricsBackgroundColor);
            FullscreenActivity.custom1_lyricsCustomColor = myPreferences.getInt("custom1_lyricsCustomColor", default_dark_lyricsBackgroundColor);
            FullscreenActivity.custom1_lyricsPreChorusColor = myPreferences.getInt("custom1_lyricsPreChorusColor", default_dark_lyricsBackgroundColor);
            FullscreenActivity.custom1_lyricsTagColor = myPreferences.getInt("custom1_lyricsTagColor", default_dark_lyricsBackgroundColor);
            FullscreenActivity.custom1_lyricsTextColor = myPreferences.getInt("custom1_lyricsTextColor", default_dark_lyricsTextColor);
            FullscreenActivity.custom1_lyricsVerseColor = myPreferences.getInt("custom1_lyricsVerseColor", default_dark_lyricsBackgroundColor);
            FullscreenActivity.custom1_metronome = myPreferences.getInt("custom1_metronome", default_metronomeColor);
            FullscreenActivity.custom1_pagebuttons = myPreferences.getInt("custom1_pagebuttons", default_pagebuttonsColor);
            FullscreenActivity.custom1_presoAlertFont = myPreferences.getInt("custom1_presoAlertFont", default_dark_presoAlertColor);
            FullscreenActivity.custom1_presoFont = myPreferences.getInt("custom1_presoFont", default_dark_presoFontColor);
            FullscreenActivity.custom1_presoInfoFont = myPreferences.getInt("custom1_presoInfoFont", default_dark_presoInfoFontColor);
            FullscreenActivity.custom1_presoShadow = myPreferences.getInt("custom1_presoShadow", default_dark_presoShadowFontColor);
            FullscreenActivity.custom1_stickytext = myPreferences.getInt("custom1_stickytext", default_stickyNotes);
            FullscreenActivity.custom1_stickybg = myPreferences.getInt("custom1_stickybg", default_stickyNotesBG);
            FullscreenActivity.custom2_lyricsBackgroundColor = myPreferences.getInt("custom2_lyricsBackgroundColor", default_light_lyricsBackgroundColor);
            FullscreenActivity.custom2_lyricsBridgeColor = myPreferences.getInt("custom2_lyricsBridgeColor", default_light_lyricsBackgroundColor);
            FullscreenActivity.custom2_lyricsCapoColor = myPreferences.getInt("custom2_lyricsCapoColor", default_light_lyricsCapoColor);
            FullscreenActivity.custom2_lyricsChordsColor = myPreferences.getInt("custom2_lyricsChordsColor", default_light_lyricsChordsColor);
            FullscreenActivity.custom2_lyricsChorusColor = myPreferences.getInt("custom2_lyricsChorusColor", default_light_lyricsBackgroundColor);
            FullscreenActivity.custom2_lyricsCommentColor = myPreferences.getInt("custom2_lyricsCommentColor", default_light_lyricsBackgroundColor);
            FullscreenActivity.custom2_lyricsCustomColor = myPreferences.getInt("custom2_lyricsCustomColor", default_light_lyricsBackgroundColor);
            FullscreenActivity.custom2_lyricsPreChorusColor = myPreferences.getInt("custom2_lyricsPreChorusColor", default_light_lyricsBackgroundColor);
            FullscreenActivity.custom2_lyricsTagColor = myPreferences.getInt("custom2_lyricsTagColor", default_light_lyricsBackgroundColor);
            FullscreenActivity.custom2_lyricsTextColor = myPreferences.getInt("custom2_lyricsTextColor", default_light_lyricsTextColor);
            FullscreenActivity.custom2_lyricsVerseColor = myPreferences.getInt("custom2_lyricsVerseColor", default_light_lyricsBackgroundColor);
            FullscreenActivity.custom2_metronome = myPreferences.getInt("custom2_metronome", default_metronomeColor);
            FullscreenActivity.custom2_pagebuttons = myPreferences.getInt("custom2_pagebuttons", default_pagebuttonsColor);
            FullscreenActivity.custom2_presoAlertFont = myPreferences.getInt("custom2_presoAlertFont", default_dark_presoAlertColor);
            FullscreenActivity.custom2_presoFont = myPreferences.getInt("custom2_presoFont", default_dark_presoFontColor);
            FullscreenActivity.custom2_presoInfoFont = myPreferences.getInt("custom2_presoInfoFont", default_dark_presoInfoFontColor);
            FullscreenActivity.custom2_presoShadow = myPreferences.getInt("custom2_presoShadow", default_dark_presoShadowFontColor);
            FullscreenActivity.custom2_stickytext = myPreferences.getInt("custom2_stickytext", default_stickyNotes);
            FullscreenActivity.custom2_stickybg = myPreferences.getInt("custom2_stickybg", default_stickyNotesBG);
            FullscreenActivity.customfontname = myPreferences.getString("customfontname", "");
            FullscreenActivity.customLogo = myPreferences.getString("customLogo","ost_logo.png");
            FullscreenActivity.customLogoSize = myPreferences.getFloat("customLogoSize", 0.5f);
            FullscreenActivity.customStorage = myPreferences.getString("customStorage", Environment.getExternalStorageDirectory().getAbsolutePath());
            FullscreenActivity.dark_lyricsBackgroundColor = myPreferences.getInt("dark_lyricsBackgroundColor", default_dark_lyricsBackgroundColor);
            FullscreenActivity.dark_lyricsBridgeColor = myPreferences.getInt("dark_lyricsBridgeColor", default_dark_lyricsBridgeColor);
            FullscreenActivity.dark_lyricsCapoColor = myPreferences.getInt("dark_lyricsCapoColor", default_dark_lyricsCapoColor);
            FullscreenActivity.dark_lyricsChordsColor = myPreferences.getInt("dark_lyricsChordsColor", default_dark_lyricsChordsColor);
            FullscreenActivity.dark_lyricsChorusColor = myPreferences.getInt("dark_lyricsChorusColor", default_dark_lyricsChorusColor);
            FullscreenActivity.dark_lyricsCommentColor = myPreferences.getInt("dark_lyricsCommentColor", default_dark_lyricsCommentColor);
            FullscreenActivity.dark_lyricsCustomColor = myPreferences.getInt("dark_lyricsCustomColor", default_dark_lyricsCustomColor);
            FullscreenActivity.dark_lyricsPreChorusColor = myPreferences.getInt("dark_lyricsPreChorusColor", default_dark_lyricsPreChorusColor);
            FullscreenActivity.dark_lyricsTagColor = myPreferences.getInt("dark_lyricsTagColor", default_dark_lyricsTagColor);
            FullscreenActivity.dark_lyricsTextColor = myPreferences.getInt("dark_lyricsTextColor", default_dark_lyricsTextColor);
            FullscreenActivity.dark_lyricsVerseColor = myPreferences.getInt("dark_lyricsVerseColor", default_dark_lyricsVerseColor);
            FullscreenActivity.dark_metronome = myPreferences.getInt("dark_metronome", default_metronomeColor);
            FullscreenActivity.dark_pagebuttons = myPreferences.getInt("dark_pagebuttons", default_pagebuttonsColor);
            FullscreenActivity.dark_presoAlertFont = myPreferences.getInt("dark_presoAlertFont", default_dark_presoAlertColor);
            FullscreenActivity.dark_presoFont = myPreferences.getInt("dark_presoFont", default_dark_presoFontColor);
            FullscreenActivity.dark_presoInfoFont = myPreferences.getInt("dark_presoInfoFont", default_dark_presoInfoFontColor);
            FullscreenActivity.dark_presoShadow = myPreferences.getInt("dark_presoShadow", default_dark_presoShadowFontColor);
            FullscreenActivity.dark_stickytext = myPreferences.getInt("dark_stickytext", default_stickyNotes);
            FullscreenActivity.dark_stickybg = myPreferences.getInt("dark_stickybg", default_stickyNotesBG);
            FullscreenActivity.default_autoscroll_predelay = myPreferences.getInt("default_autoscroll_predelay", 10);
            FullscreenActivity.default_autoscroll_predelay_max = myPreferences.getInt("default_autoscroll_predelay_max", 30);
            FullscreenActivity.default_autoscroll_songlength = myPreferences.getInt("default_autoscroll_songlength", 180);
            FullscreenActivity.drawingEraserSize = myPreferences.getInt("drawingEraserSize",20);
            FullscreenActivity.drawingHighlightColor = myPreferences.getString("drawingHighlightColor", "yellow");
            FullscreenActivity.drawingHighlightSize = myPreferences.getInt("drawingHighlightSize", 20);
            FullscreenActivity.drawingPenColor = myPreferences.getString("drawingPenColor", "yellow");
            FullscreenActivity.drawingPenSize = myPreferences.getInt("drawingPenSize", 20);
            FullscreenActivity.drawingTool = myPreferences.getString("drawingTool", "highlighter");
            FullscreenActivity.exportOpenSongAppSet = myPreferences.getBoolean("exportOpenSongAppSet", true);
            FullscreenActivity.exportOpenSongApp = myPreferences.getBoolean("exportOpenSongApp", true);
            FullscreenActivity.exportDesktop = myPreferences.getBoolean("exportDesktop", false);
            FullscreenActivity.exportText = myPreferences.getBoolean("exportText", false);
            FullscreenActivity.exportChordPro = myPreferences.getBoolean("exportChordPro", false);
            FullscreenActivity.exportOnSong = myPreferences.getBoolean("exportOnSong", false);
            FullscreenActivity.exportImage = myPreferences.getBoolean("exportImage", false);
            FullscreenActivity.exportPDF = myPreferences.getBoolean("exportPDF", false);
            FullscreenActivity.fabSize = myPreferences.getInt("fabSize", FloatingActionButton.SIZE_MINI);
            FullscreenActivity.gesture_doubletap = myPreferences.getString("gesture_doubletap", "2");
            FullscreenActivity.gesture_longpress = myPreferences.getString("gesture_longpress", "1");
            FullscreenActivity.grouppagebuttons = myPreferences.getBoolean("grouppagebuttons", false);
            FullscreenActivity.headingfontscalesize = myPreferences.getFloat("headingfontscalesize", 0.6f);
            FullscreenActivity.hideActionBar = myPreferences.getBoolean("hideActionBar", false);
            FullscreenActivity.highlightShowSecs = myPreferences.getInt("highlightShowSecs", 0);
            FullscreenActivity.languageToLoad = myPreferences.getString("languageToLoad", "");
            FullscreenActivity.lastSetName = myPreferences.getString("lastSetName", "");
            FullscreenActivity.light_lyricsBackgroundColor = myPreferences.getInt("light_lyricsBackgroundColor", default_light_lyricsBackgroundColor);
            FullscreenActivity.light_lyricsBridgeColor = myPreferences.getInt("light_lyricsBridgeColor", default_light_lyricsBridgeColor);
            FullscreenActivity.light_lyricsCapoColor = myPreferences.getInt("light_lyricsCapoColor", default_light_lyricsCapoColor);
            FullscreenActivity.light_lyricsChordsColor = myPreferences.getInt("light_lyricsChordsColor", default_light_lyricsChordsColor);
            FullscreenActivity.light_lyricsChorusColor = myPreferences.getInt("light_lyricsChorusColor", default_light_lyricsChorusColor);
            FullscreenActivity.light_lyricsCommentColor = myPreferences.getInt("light_lyricsCommentColor", default_light_lyricsCommentColor);
            FullscreenActivity.light_lyricsCustomColor = myPreferences.getInt("light_lyricsCustomColor", default_light_lyricsCustomColor);
            FullscreenActivity.light_lyricsPreChorusColor = myPreferences.getInt("light_lyricsPreChorusColor", default_light_lyricsPreChorusColor);
            FullscreenActivity.light_lyricsTagColor = myPreferences.getInt("light_lyricsTagColor", default_light_lyricsTagColor);
            FullscreenActivity.light_lyricsTextColor = myPreferences.getInt("light_lyricsTextColor", default_light_lyricsTextColor);
            FullscreenActivity.light_lyricsVerseColor = myPreferences.getInt("light_lyricsVerseColor", default_light_lyricsVerseColor);
            FullscreenActivity.light_metronome = myPreferences.getInt("light_metronome", default_metronomeColor);
            FullscreenActivity.light_pagebuttons = myPreferences.getInt("light_pagebuttons", default_pagebuttonsColor);
            FullscreenActivity.light_presoAlertFont = myPreferences.getInt("light_presoAlertFont", default_dark_presoAlertColor);
            FullscreenActivity.light_presoFont = myPreferences.getInt("light_presoFont", default_dark_presoFontColor);
            FullscreenActivity.light_presoInfoFont = myPreferences.getInt("light_presoInfoFont", default_dark_presoInfoFontColor);
            FullscreenActivity.light_presoShadow = myPreferences.getInt("light_presoShadow", default_dark_presoShadowFontColor);
            FullscreenActivity.light_stickytext = myPreferences.getInt("light_stickytext", default_stickyNotes);
            FullscreenActivity.light_stickybg = myPreferences.getInt("light_stickybg", default_stickyNotesBG);
            FullscreenActivity.linespacing = myPreferences.getInt("linespacing", 0);
            FullscreenActivity.locale = getStoredLocale();
            FullscreenActivity.longpressdownpedalgesture = myPreferences.getString("longpressdownpedalgesture", "0");
            FullscreenActivity.longpressnextpedalgesture = myPreferences.getString("longpressnextpedalgesture", "4");
            FullscreenActivity.longpresspreviouspedalgesture = myPreferences.getString("longpresspreviouspedalgesture", "1");
            FullscreenActivity.longpressuppedalgesture = myPreferences.getString("longpressuppedalgesture", "0");
            FullscreenActivity.maxvolrange = myPreferences.getInt("maxvolrange", 400);
            FullscreenActivity.mediaStore = myPreferences.getString("mediaStore", "int");
            FullscreenActivity.menuSize = myPreferences.getFloat("menuSize",0.6f);
            FullscreenActivity.metronomepan = myPreferences.getString("metronomepan", "both");
            FullscreenActivity.metronomevol = myPreferences.getFloat("metronomevol", 0.5f);
            FullscreenActivity.mAuthor = myPreferences.getString("mAuthor", "Gareth Evans");
            FullscreenActivity.mCopyright = myPreferences.getString("mCopyright", "");
            FullscreenActivity.mDisplayTheme = myPreferences.getString("mDisplayTheme", "Theme.Holo");
            FullscreenActivity.mFontSize = myPreferences.getFloat("mFontSize", 42.0f);
            FullscreenActivity.mMaxFontSize = myPreferences.getInt("mMaxFontSize", 50);
            FullscreenActivity.mMinFontSize = myPreferences.getInt("mMinFontSize", 8);
            FullscreenActivity.mStorage = myPreferences.getString("mStorage", "int");
            FullscreenActivity.mTitle = myPreferences.getString("mTitle", "Welcome to OpenSongApp");
            FullscreenActivity.myAlert = myPreferences.getString("myAlert", "");
            FullscreenActivity.mychordsfontnum = myPreferences.getInt("mychordsfontnum", 8);
            FullscreenActivity.mylyricsfontnum = myPreferences.getInt("mylyricsfontnum", 8);
            FullscreenActivity.mypresofontnum = myPreferences.getInt("mypresofontnum", 8);
            FullscreenActivity.mypresoinfofontnum = myPreferences.getInt("mypresoinfofontnum", 8);
            FullscreenActivity.mySet = myPreferences.getString("mySet", "");
            FullscreenActivity.override_fullscale = myPreferences.getBoolean("override_fullscale", true);
            FullscreenActivity.override_widthscale = myPreferences.getBoolean("override_widthscale", false);
            FullscreenActivity.padpan = myPreferences.getString("padpan", "both");
            FullscreenActivity.padvol = myPreferences.getFloat("padvol", 1.0f);
            FullscreenActivity.page_autoscroll_visible = myPreferences.getBoolean("page_autoscroll_visible",true);
            FullscreenActivity.page_custom_visible = myPreferences.getBoolean("page_custom_visible",true);
            FullscreenActivity.page_chord_visible = myPreferences.getBoolean("page_chord_visible",true);
            FullscreenActivity.page_custom1_visible = myPreferences.getBoolean("page_custom1_visible",true);
            FullscreenActivity.page_custom2_visible = myPreferences.getBoolean("page_custom2_visible",true);
            FullscreenActivity.page_custom3_visible = myPreferences.getBoolean("page_custom3_visible",true);
            FullscreenActivity.page_custom4_visible = myPreferences.getBoolean("page_custom4_visible",true);
            FullscreenActivity.page_custom_grouped = myPreferences.getBoolean("page_custom_grouped",true);
            FullscreenActivity.page_extra_grouped = myPreferences.getBoolean("page_extra_grouped",true);
            FullscreenActivity.page_extra_visible = myPreferences.getBoolean("page_extra_visible",true);
            FullscreenActivity.page_highlight_visible = myPreferences.getBoolean("page_highlight_visible",true);
            FullscreenActivity.page_links_visible = myPreferences.getBoolean("page_links_visible",true);
            FullscreenActivity.page_metronome_visible = myPreferences.getBoolean("page_metronome_visible",true);
            FullscreenActivity.page_notation_visible = myPreferences.getBoolean("page_notation_visible",true);
            FullscreenActivity.page_pad_visible = myPreferences.getBoolean("page_pad_visible",true);
            FullscreenActivity.page_pages_visible = myPreferences.getBoolean("page_pages_visible",true);
            FullscreenActivity.page_set_visible = myPreferences.getBoolean("page_set_visible",true);
            FullscreenActivity.page_sticky_visible = myPreferences.getBoolean("page_sticky_visible",true);
            FullscreenActivity.pagebutton_position = myPreferences.getString("pagebutton_position", "right");
            FullscreenActivity.pagebutton_scale = myPreferences.getString("pagebutton_scale", "M");
            FullscreenActivity.pageButtonAlpha = myPreferences.getFloat("pageButtonAlpha", 0.3f);
            /*FullscreenActivity.pageturner_AUTOSCROLL = myPreferences.getInt("pageturner_AUTOSCROLL", -1);
            FullscreenActivity.pageturner_AUTOSCROLLPAD = myPreferences.getInt("pageturner_AUTOSCROLLPAD", -1);
            FullscreenActivity.pageturner_AUTOSCROLLMETRONOME = myPreferences.getInt("pageturner_AUTOSCROLLMETRONOME", -1);
            FullscreenActivity.pageturner_AUTOSCROLLPADMETRONOME = myPreferences.getInt("pageturner_AUTOSCROLLPADMETRONOME", -1);
            FullscreenActivity.pageturner_DOWN = myPreferences.getInt("pageturner_DOWN", 20);
            FullscreenActivity.pageturner_METRONOME = myPreferences.getInt("pageturner_METRONOME", -1);
            FullscreenActivity.pageturner_NEXT = myPreferences.getInt("pageturner_NEXT", 22);
            FullscreenActivity.pageturner_PAD = myPreferences.getInt("pageturner_PAD", -1);
            FullscreenActivity.pageturner_PADMETRONOME = myPreferences.getInt("pageturner_PADMETRONOME", -1);
            FullscreenActivity.pageturner_PREVIOUS = myPreferences.getInt("pageturner_PREVIOUS", 21);
            FullscreenActivity.pageturner_UP = myPreferences.getInt("pageturner_UP", 19);*/

            FullscreenActivity.pedal1 = myPreferences.getInt("pedal1", 21);
            FullscreenActivity.pedal2 = myPreferences.getInt("pedal2", 22);
            FullscreenActivity.pedal3 = myPreferences.getInt("pedal3", 19);
            FullscreenActivity.pedal4 = myPreferences.getInt("pedal4", 20);
            FullscreenActivity.pedal5 = myPreferences.getInt("pedal5", -1);
            FullscreenActivity.pedal6 = myPreferences.getInt("pedal6", -1);
            FullscreenActivity.pedal1longaction = myPreferences.getString("pedal1longaction", "songmenu");
            FullscreenActivity.pedal2longaction = myPreferences.getString("pedal2longaction", "set");
            FullscreenActivity.pedal3longaction = myPreferences.getString("pedal3longaction", "");
            FullscreenActivity.pedal4longaction = myPreferences.getString("pedal4longaction", "");
            FullscreenActivity.pedal5longaction = myPreferences.getString("pedal5longaction", "");
            FullscreenActivity.pedal6longaction = myPreferences.getString("pedal6longaction", "");
            FullscreenActivity.pedal1shortaction = myPreferences.getString("pedal1shortaction", "prev");
            FullscreenActivity.pedal2shortaction = myPreferences.getString("pedal2shortaction", "next");
            FullscreenActivity.pedal3shortaction = myPreferences.getString("pedal3shortaction", "up");
            FullscreenActivity.pedal4shortaction = myPreferences.getString("pedal4shortaction", "down");
            FullscreenActivity.pedal5shortaction = myPreferences.getString("pedal5shortaction", "");
            FullscreenActivity.pedal6shortaction = myPreferences.getString("pedal6shortaction", "");
            FullscreenActivity.popupAlpha_All = myPreferences.getFloat("popupAlpha_All", 0.9f);
            FullscreenActivity.popupAlpha_Set = myPreferences.getFloat("popupAlpha_Set", 0.9f);
            FullscreenActivity.popupDim_All = myPreferences.getFloat("popupDim_All", 0.9f);
            FullscreenActivity.popupDim_Set = myPreferences.getFloat("popupDim_Set", 0.9f);
            FullscreenActivity.popupPosition_All = myPreferences.getString("popupPosition_All", "c");
            FullscreenActivity.popupPosition_Set = myPreferences.getString("popupPosition_Set", "c");
            FullscreenActivity.popupScale_All = myPreferences.getFloat("popupScale_All", 0.8f);
            FullscreenActivity.popupScale_Set = myPreferences.getFloat("popupScale_Set", 0.8f);
            FullscreenActivity.prefChord_Aflat_Gsharp = myPreferences.getString("prefChord_Aflat_Gsharp", "b");
            FullscreenActivity.prefChord_Bflat_Asharp = myPreferences.getString("prefChord_Bflat_Asharp", "b");
            FullscreenActivity.prefChord_Dflat_Csharp = myPreferences.getString("prefChord_Dflat_Csharp", "b");
            FullscreenActivity.prefChord_Eflat_Dsharp = myPreferences.getString("prefChord_Eflat_Dsharp", "b");
            FullscreenActivity.prefChord_Gflat_Fsharp = myPreferences.getString("prefChord_Gflat_Fsharp", "b");
            FullscreenActivity.prefChord_Aflatm_Gsharpm = myPreferences.getString("prefChord_Aflatm_Gsharpm", "#");
            FullscreenActivity.prefChord_Bflatm_Asharpm = myPreferences.getString("prefChord_Bflatm_Asharpm", "b");
            FullscreenActivity.prefChord_Dflatm_Csharpm = myPreferences.getString("prefChord_Dflatm_Csharpm", "#");
            FullscreenActivity.prefChord_Eflatm_Dsharpm = myPreferences.getString("prefChord_Eflatm_Dsharpm", "b");
            FullscreenActivity.prefChord_Gflatm_Fsharpm = myPreferences.getString("prefChord_Gflatm_Fsharpm", "#");
            FullscreenActivity.prefStorage = myPreferences.getString("prefStorage", "");
            FullscreenActivity.presenterChords = myPreferences.getString("presenterChords", "N");
            FullscreenActivity.presoAlpha = myPreferences.getFloat("presoAlpha", 1.0f);
            FullscreenActivity.presoAutoScale = myPreferences.getBoolean("presoAutoScale", true);
            FullscreenActivity.presoFontSize = myPreferences.getInt("presoFontSize", 12);
            FullscreenActivity.presoInfoAlign = myPreferences.getInt("presoInfoAlign", Gravity.RIGHT);
            FullscreenActivity.presoLyricsAlign = myPreferences.getInt("presoLyricsAlign", Gravity.CENTER_HORIZONTAL);
            FullscreenActivity.presoMaxFontSize = myPreferences.getInt("presoMaxFontSize", 40);
            FullscreenActivity.presoShowChords = myPreferences.getBoolean("presoShowChords", false);
            FullscreenActivity.presoTransitionTime = myPreferences.getInt("presoTransitionTime", 800);
            FullscreenActivity.profile = myPreferences.getString("profile", "");
            FullscreenActivity.quickLaunchButton_1 = myPreferences.getString("quickLaunchButton_1", "");
            FullscreenActivity.quickLaunchButton_2 = myPreferences.getString("quickLaunchButton_2", "");
            FullscreenActivity.quickLaunchButton_3 = myPreferences.getString("quickLaunchButton_3", "");
            FullscreenActivity.quickLaunchButton_4 = myPreferences.getString("quickLaunchButton_4", "");
            FullscreenActivity.randomFolders = myPreferences.getString("randomFolders", "");
            FullscreenActivity.showCapoAsNumerals = myPreferences.getBoolean("showCapoAsNumerals", false);
            FullscreenActivity.scrollDistance = myPreferences.getFloat("scrollDistance", 0.6f);
            FullscreenActivity.scrollSpeed = myPreferences.getInt("scrollSpeed", 1500);
            FullscreenActivity.showAlphabeticalIndexInSongMenu = myPreferences.getBoolean("showAlphabeticalIndexInSongMenu", true);
            FullscreenActivity.showCapoChords = myPreferences.getBoolean("showCapoChords", true);
            FullscreenActivity.showLyrics = myPreferences.getBoolean("showLyrics", true);
            FullscreenActivity.showNativeAndCapoChords = myPreferences.getBoolean("showNativeAndCapoChords", true);
            FullscreenActivity.showNextInSet = myPreferences.getString("showNextInSet", "bottom");
            FullscreenActivity.showSetTickBoxInSongMenu = myPreferences.getBoolean("showSetTickBoxInSongMenu", true);
            FullscreenActivity.showSplashVersion = myPreferences.getInt("showSplashVersion", FullscreenActivity.version);
            FullscreenActivity.songfilename = myPreferences.getString("songfilename", "Welcome to OpenSongApp");
            FullscreenActivity.stagemodeScale = myPreferences.getFloat("stagemodeScale", 0.7f);
            FullscreenActivity.stickyNotesShowSecs = myPreferences.getInt("stickyNotesShowSecs",5);
            FullscreenActivity.stickyWidth = myPreferences.getInt("stickyWidth",400);
            FullscreenActivity.stickyOpacity = myPreferences.getFloat("stickyOpacity", 0.8f);
            FullscreenActivity.stickyTextSize = myPreferences.getFloat("stickyTextSize", 14.0f);
            FullscreenActivity.SWIPE_MAX_OFF_PATH = myPreferences.getInt("SWIPE_MAX_OFF_PATH", 200);
            FullscreenActivity.SWIPE_MIN_DISTANCE = myPreferences.getInt("SWIPE_MIN_DISTANCE", 250);
            FullscreenActivity.SWIPE_THRESHOLD_VELOCITY = myPreferences.getInt("SWIPE_THRESHOLD_VELOCITY", 600);
            FullscreenActivity.swipeDrawer = myPreferences.getString("swipeDrawer", "Y");
            FullscreenActivity.swipeForMenus = myPreferences.getBoolean("swipeForMenus", true);
            FullscreenActivity.swipeForSongs = myPreferences.getBoolean("swipeForSongs", true);
            FullscreenActivity.swipeSet = myPreferences.getString("swipeSet", "Y");
            FullscreenActivity.timerFontSizeAutoScroll = myPreferences.getFloat("timerFontSizeAutoScroll",14.0f);
            FullscreenActivity.timerFontSizePad = myPreferences.getFloat("timerFontSizePad",14.0f);
            FullscreenActivity.timeFormat24h = myPreferences.getBoolean("timeFormat24h", true);
            FullscreenActivity.timeOn = myPreferences.getBoolean("timeOn", true);
            FullscreenActivity.timeSize = myPreferences.getFloat("timeSize", 9.0f);
            FullscreenActivity.toggleAutoHighlight = myPreferences.getBoolean("toggleAutoHighlight", true);
            FullscreenActivity.toggleAutoSticky = myPreferences.getString("toggleAutoSticky", "N");
            FullscreenActivity.togglePageButtons = myPreferences.getString("togglePageButtons", "Y");
            FullscreenActivity.toggleScrollArrows = myPreferences.getString("toggleScrollArrows", "S");
            FullscreenActivity.toggleScrollBeforeSwipe = myPreferences.getString("toggleScrollBeforeSwipe", "Y");
            FullscreenActivity.toggleYScale = myPreferences.getString("toggleYScale", "W");
            FullscreenActivity.transposeStyle = myPreferences.getString("transposeStyle", "sharps");
            FullscreenActivity.trimSections = myPreferences.getBoolean("trimSections", false);
            FullscreenActivity.trimSectionSpace = myPreferences.getBoolean("trimSectionSpace", false);
            String uriTreeTemp = myPreferences.getString("uriTree", "");
            if (uriTreeTemp.equals("") || uriTreeTemp.isEmpty()) {
                FullscreenActivity.uriTree = null;
            } else {
                FullscreenActivity.uriTree = Uri.parse(uriTreeTemp);
            }
            FullscreenActivity.usePresentationOrder = myPreferences.getBoolean("usePresentationOrder", false);
            FullscreenActivity.visualmetronome = myPreferences.getBoolean("visualmetronome", false);
            FullscreenActivity.whichMode = myPreferences.getString("whichMode", "Performance");
            FullscreenActivity.whichSetCategory = myPreferences.getString("whichSetCategory", FullscreenActivity.mainfoldername);
            FullscreenActivity.whichSongFolder = myPreferences.getString("whichSongFolder", FullscreenActivity.mainfoldername);
            FullscreenActivity.xmargin_presentation = myPreferences.getInt("xmargin_presentation", 50);
            FullscreenActivity.ymargin_presentation = myPreferences.getInt("ymargin_presentation", 25);

            // Leaving these as try/catches until v4+ (changed types from old preferences to new)
            // This will autoconvert them for now.  Eventually after users upgrade, these can be put
            // back as standard preferences being loaded.

            try {
                FullscreenActivity.presoAlertSize = myPreferences.getFloat("presoAlertSize", 12.0f);
            } catch (Exception e) {
                FullscreenActivity.presoAlertSize = 12.0f;
            }
            try {
                FullscreenActivity.presoAuthorSize = myPreferences.getFloat("presoAuthorSize", 12.0f);
            } catch (Exception e) {
                FullscreenActivity.presoAuthorSize = 12.0f;
            }
            try {
                FullscreenActivity.presoCopyrightSize = myPreferences.getFloat("presoCopyrightSize", 14.0f);
            } catch (Exception e) {
                FullscreenActivity.presoTitleSize = 14.0f;
            }
            try {
                FullscreenActivity.presoTitleSize = myPreferences.getFloat("presoTitleSize", 14.0f);
            } catch (Exception e) {
                FullscreenActivity.presoTitleSize = 14.0f;
            }
            try {
                FullscreenActivity.showChords = myPreferences.getBoolean("showChords", true);
            } catch (Exception e) {
                FullscreenActivity.showChords = true;
            }

        } catch (Exception e) {
            // Error loading the preferences
            e.printStackTrace();
            // Try restarting the app
            FullscreenActivity.restart(FullscreenActivity.mContext);
        }

    }

    public static void savePreferences() {

        try {
            SharedPreferences.Editor editor = myPreferences.edit();

            editor.putFloat("ab_titleSize", FullscreenActivity.ab_titleSize);
            editor.putFloat("ab_authorSize", FullscreenActivity.ab_authorSize);
            editor.putFloat("alphabeticalSize", FullscreenActivity.alphabeticalSize);
            editor.putString("alwaysPreferredChordFormat", FullscreenActivity.alwaysPreferredChordFormat);
            editor.putBoolean("autoProject", FullscreenActivity.autoProject);
            editor.putString("autoscroll_default_or_prompt", FullscreenActivity.autoscroll_default_or_prompt);
            editor.putInt("autoScrollDelay", FullscreenActivity.autoScrollDelay);
            editor.putBoolean("autostartautoscroll", FullscreenActivity.autostartautoscroll);
            editor.putString("backgroundImage1", FullscreenActivity.backgroundImage1);
            editor.putString("backgroundImage2", FullscreenActivity.backgroundImage2);
            editor.putString("backgroundToUse", FullscreenActivity.backgroundToUse);
            editor.putString("backgroundTypeToUse", FullscreenActivity.backgroundTypeToUse);
            editor.putString("backgroundVideo1", FullscreenActivity.backgroundVideo1);
            editor.putString("backgroundVideo2", FullscreenActivity.backgroundVideo2);
            editor.putBoolean("batteryDialOn", FullscreenActivity.batteryDialOn);
            editor.putInt("batteryLine", FullscreenActivity.batteryLine);
            editor.putBoolean("batteryOn", FullscreenActivity.batteryOn);
            editor.putFloat("batterySize", FullscreenActivity.batterySize);
            editor.putString("bibleFile", FullscreenActivity.bibleFile);
            editor.putString("capoDisplay", FullscreenActivity.capoDisplay);
            editor.putFloat("chordfontscalesize", FullscreenActivity.chordfontscalesize);
            editor.putString("chordFormat", FullscreenActivity.chordFormat);
            editor.putString("chordInstrument", FullscreenActivity.chordInstrument);
            editor.putFloat("commentfontscalesize", FullscreenActivity.commentfontscalesize);
            editor.putInt("custom1_lyricsBackgroundColor", FullscreenActivity.custom1_lyricsBackgroundColor);
            editor.putInt("custom1_lyricsBridgeColor", FullscreenActivity.custom1_lyricsBridgeColor);
            editor.putInt("custom1_lyricsCapoColor", FullscreenActivity.custom1_lyricsCapoColor);
            editor.putInt("custom1_lyricsChordsColor", FullscreenActivity.custom1_lyricsChordsColor);
            editor.putInt("custom1_lyricsChorusColor", FullscreenActivity.custom1_lyricsChorusColor);
            editor.putInt("custom1_lyricsCommentColor", FullscreenActivity.custom1_lyricsCommentColor);
            editor.putInt("custom1_lyricsCustomColor", FullscreenActivity.custom1_lyricsCustomColor);
            editor.putInt("custom1_lyricsPreChorusColor", FullscreenActivity.custom1_lyricsPreChorusColor);
            editor.putInt("custom1_lyricsTagColor", FullscreenActivity.custom1_lyricsTagColor);
            editor.putInt("custom1_lyricsTextColor", FullscreenActivity.custom1_lyricsTextColor);
            editor.putInt("custom1_lyricsVerseColor", FullscreenActivity.custom1_lyricsVerseColor);
            editor.putInt("custom1_metronome", FullscreenActivity.custom1_metronome);
            editor.putInt("custom1_pagebuttons", FullscreenActivity.custom1_pagebuttons);
            editor.putInt("custom1_presoAlertFont", FullscreenActivity.custom1_presoAlertFont);
            editor.putInt("custom1_presoFont", FullscreenActivity.custom1_presoFont);
            editor.putInt("custom1_presoInfoFont", FullscreenActivity.custom1_presoInfoFont);
            editor.putInt("custom1_presoShadow", FullscreenActivity.custom1_presoShadow);
            editor.putInt("custom1_stickytext", FullscreenActivity.custom1_stickytext);
            editor.putInt("custom1_stickybg", FullscreenActivity.custom1_stickybg);
            editor.putInt("custom2_lyricsBackgroundColor", FullscreenActivity.custom2_lyricsBackgroundColor);
            editor.putInt("custom2_lyricsBridgeColor", FullscreenActivity.custom2_lyricsBridgeColor);
            editor.putInt("custom2_lyricsCapoColor", FullscreenActivity.custom2_lyricsCapoColor);
            editor.putInt("custom2_lyricsChordsColor", FullscreenActivity.custom2_lyricsChordsColor);
            editor.putInt("custom2_lyricsChorusColor", FullscreenActivity.custom2_lyricsChorusColor);
            editor.putInt("custom2_lyricsCommentColor", FullscreenActivity.custom2_lyricsCommentColor);
            editor.putInt("custom2_lyricsCustomColor", FullscreenActivity.custom2_lyricsCustomColor);
            editor.putInt("custom2_lyricsPreChorusColor", FullscreenActivity.custom2_lyricsPreChorusColor);
            editor.putInt("custom2_lyricsTagColor", FullscreenActivity.custom2_lyricsTagColor);
            editor.putInt("custom2_lyricsTextColor", FullscreenActivity.custom2_lyricsTextColor);
            editor.putInt("custom2_lyricsVerseColor", FullscreenActivity.custom2_lyricsVerseColor);
            editor.putInt("custom2_metronome", FullscreenActivity.custom2_metronome);
            editor.putInt("custom2_pagebuttons", FullscreenActivity.custom2_pagebuttons);
            editor.putInt("custom2_presoAlertFont", FullscreenActivity.custom2_presoAlertFont);
            editor.putInt("custom2_presoFont", FullscreenActivity.custom2_presoFont);
            editor.putInt("custom2_presoInfoFont", FullscreenActivity.custom2_presoInfoFont);
            editor.putInt("custom2_presoShadow", FullscreenActivity.custom2_presoShadow);
            editor.putInt("custom2_stickytext", FullscreenActivity.custom2_stickytext);
            editor.putInt("custom2_stickybg", FullscreenActivity.custom2_stickybg);
            editor.putString("customfontname", FullscreenActivity.customfontname);
            editor.putString("customLogo", FullscreenActivity.customLogo);
            editor.putFloat("customLogoSize", FullscreenActivity.customLogoSize);
            editor.putString("customStorage", FullscreenActivity.customStorage);
            editor.putInt("dark_lyricsBackgroundColor", FullscreenActivity.dark_lyricsBackgroundColor);
            editor.putInt("dark_lyricsBridgeColor", FullscreenActivity.dark_lyricsBridgeColor);
            editor.putInt("dark_lyricsCapoColor", FullscreenActivity.dark_lyricsCapoColor);
            editor.putInt("dark_lyricsChordsColor", FullscreenActivity.dark_lyricsChordsColor);
            editor.putInt("dark_lyricsChorusColor", FullscreenActivity.dark_lyricsChorusColor);
            editor.putInt("dark_lyricsCommentColor", FullscreenActivity.dark_lyricsCommentColor);
            editor.putInt("dark_lyricsCustomColor", FullscreenActivity.dark_lyricsCustomColor);
            editor.putInt("dark_lyricsPreChorusColor", FullscreenActivity.dark_lyricsPreChorusColor);
            editor.putInt("dark_lyricsTagColor", FullscreenActivity.dark_lyricsTagColor);
            editor.putInt("dark_lyricsTextColor", FullscreenActivity.dark_lyricsTextColor);
            editor.putInt("dark_lyricsVerseColor", FullscreenActivity.dark_lyricsVerseColor);
            editor.putInt("dark_metronome", FullscreenActivity.dark_metronome);
            editor.putInt("dark_pagebuttons", FullscreenActivity.dark_pagebuttons);
            editor.putInt("dark_presoAlertFont", FullscreenActivity.dark_presoAlertFont);
            editor.putInt("dark_presoFont", FullscreenActivity.dark_presoFont);
            editor.putInt("dark_presoInfoFont", FullscreenActivity.dark_presoInfoFont);
            editor.putInt("dark_presoShadow", FullscreenActivity.dark_presoShadow);
            editor.putInt("dark_stickytext", FullscreenActivity.dark_stickytext);
            editor.putInt("dark_stickybg", FullscreenActivity.dark_stickybg);
            editor.putInt("default_autoscroll_predelay", FullscreenActivity.default_autoscroll_predelay);
            editor.putInt("default_autoscroll_songlength", FullscreenActivity.default_autoscroll_songlength);
            editor.putInt("drawingEraserSize", FullscreenActivity.drawingEraserSize);
            editor.putString("drawingHighlightColor", FullscreenActivity.drawingHighlightColor);
            editor.putInt("drawingHighlightSize", FullscreenActivity.drawingHighlightSize);
            editor.putString("drawingPenColor", FullscreenActivity.drawingPenColor);
            editor.putInt("drawingPenSize", FullscreenActivity.drawingPenSize);
            editor.putString("drawingTool", FullscreenActivity.drawingTool);
            editor.putBoolean("exportOpenSongAppSet", FullscreenActivity.exportOpenSongAppSet);
            editor.putBoolean("exportOpenSongApp", FullscreenActivity.exportOpenSongApp);
            editor.putBoolean("exportDesktop", FullscreenActivity.exportDesktop);
            editor.putBoolean("exportText", FullscreenActivity.exportText);
            editor.putBoolean("exportChordPro", FullscreenActivity.exportChordPro);
            editor.putBoolean("exportOnSong", FullscreenActivity.exportOnSong);
            editor.putBoolean("exportImage", FullscreenActivity.exportImage);
            editor.putBoolean("exportPDF", FullscreenActivity.exportPDF);
            editor.putInt("fabSize", FullscreenActivity.fabSize);
            editor.putString("gesture_doubletap", FullscreenActivity.gesture_doubletap);
            editor.putString("gesture_longpress", FullscreenActivity.gesture_longpress);
            editor.putBoolean("grouppagebuttons", FullscreenActivity.grouppagebuttons);
            editor.putFloat("headingfontscalesize", FullscreenActivity.headingfontscalesize);
            editor.putBoolean("hideActionBar", FullscreenActivity.hideActionBar);
            editor.putInt("highlightShowSecs", FullscreenActivity.highlightShowSecs);
            editor.putString("languageToLoad", FullscreenActivity.languageToLoad);
            editor.putString("lastSetName", FullscreenActivity.lastSetName);
            editor.putInt("light_lyricsBridgeColor", FullscreenActivity.light_lyricsBridgeColor);
            editor.putInt("light_lyricsCapoColor", FullscreenActivity.light_lyricsCapoColor);
            editor.putInt("light_lyricsChordsColor", FullscreenActivity.light_lyricsChordsColor);
            editor.putInt("light_lyricsChorusColor", FullscreenActivity.light_lyricsChorusColor);
            editor.putInt("light_lyricsCommentColor", FullscreenActivity.light_lyricsCommentColor);
            editor.putInt("light_lyricsCustomColor", FullscreenActivity.light_lyricsCustomColor);
            editor.putInt("light_lyricsBackgroundColor", FullscreenActivity.light_lyricsBackgroundColor);
            editor.putInt("light_lyricsPreChorusColor", FullscreenActivity.light_lyricsPreChorusColor);
            editor.putInt("light_lyricsTagColor", FullscreenActivity.light_lyricsTagColor);
            editor.putInt("light_lyricsTextColor", FullscreenActivity.light_lyricsTextColor);
            editor.putInt("light_lyricsVerseColor", FullscreenActivity.light_lyricsVerseColor);
            editor.putInt("light_metronome", FullscreenActivity.light_metronome);
            editor.putInt("light_pagebuttons", FullscreenActivity.light_pagebuttons);
            editor.putInt("light_presoAlertFont", FullscreenActivity.light_presoAlertFont);
            editor.putInt("light_presoFont", FullscreenActivity.light_presoFont);
            editor.putInt("light_presoInfoFont", FullscreenActivity.light_presoInfoFont);
            editor.putInt("light_presoShadow", FullscreenActivity.light_presoShadow);
            editor.putInt("light_stickytext", FullscreenActivity.light_stickytext);
            editor.putInt("light_stickybg", FullscreenActivity.light_stickybg);
            editor.putInt("linespacing", FullscreenActivity.linespacing);
            editor.putString("locale", FullscreenActivity.locale.toString());
            editor.putString("longpressdownpedalgesture", FullscreenActivity.longpressdownpedalgesture);
            editor.putString("longpressnextpedalgesture", FullscreenActivity.longpressnextpedalgesture);
            editor.putString("longpresspreviouspedalgesture", FullscreenActivity.longpresspreviouspedalgesture);
            editor.putString("longpressuppedalgesture", FullscreenActivity.longpressuppedalgesture);
            editor.putInt("maxvolrange", FullscreenActivity.maxvolrange);
            editor.putString("mediaStore", FullscreenActivity.mediaStore);
            editor.putFloat("menuSize", FullscreenActivity.menuSize);
            editor.putString("metronomepan", FullscreenActivity.metronomepan);
            editor.putFloat("metronomevol", FullscreenActivity.metronomevol);
            editor.putString("mAuthor", FullscreenActivity.mAuthor.toString());
            editor.putString("mCopyright", FullscreenActivity.mCopyright.toString());
            editor.putString("mDisplayTheme", FullscreenActivity.mDisplayTheme);
            editor.putFloat("mFontSize", FullscreenActivity.mFontSize);
            editor.putInt("mMaxFontSize", FullscreenActivity.mMaxFontSize);
            editor.putInt("mMinFontSize", FullscreenActivity.mMinFontSize);
            editor.putString("mStorage", FullscreenActivity.mStorage);
            editor.putString("mTitle", FullscreenActivity.mTitle.toString());
            editor.putString("myAlert", FullscreenActivity.myAlert);
            editor.putInt("mychordsfontnum", FullscreenActivity.mychordsfontnum);
            editor.putInt("mylyricsfontnum", FullscreenActivity.mylyricsfontnum);
            editor.putInt("mypresofontnum", FullscreenActivity.mypresofontnum);
            editor.putInt("mypresoinfofontnum", FullscreenActivity.mypresoinfofontnum);
            if (FullscreenActivity.mySet.contains("$**__**$")) {
                // Blank entry
                FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**__**$", "");
            }
            editor.putString("mySet", FullscreenActivity.mySet);editor.putBoolean("override_fullscale", FullscreenActivity.override_fullscale);
            editor.putBoolean("override_widthscale", FullscreenActivity.override_widthscale);
            editor.putString("padpan", FullscreenActivity.padpan);
            editor.putFloat("padvol", FullscreenActivity.padvol);
            editor.putBoolean("page_autoscroll_visible", FullscreenActivity.page_autoscroll_visible);
            editor.putBoolean("page_chord_visible", FullscreenActivity.page_chord_visible);
            editor.putBoolean("page_custom_grouped", FullscreenActivity.page_custom_grouped);
            editor.putBoolean("page_custom_visible", FullscreenActivity.page_custom_visible);
            editor.putBoolean("page_custom1_visible", FullscreenActivity.page_custom1_visible);
            editor.putBoolean("page_custom2_visible", FullscreenActivity.page_custom2_visible);
            editor.putBoolean("page_custom3_visible", FullscreenActivity.page_custom3_visible);
            editor.putBoolean("page_custom4_visible", FullscreenActivity.page_custom4_visible);
            editor.putBoolean("page_extra_grouped", FullscreenActivity.page_extra_grouped);
            editor.putBoolean("page_extra_visible", FullscreenActivity.page_extra_visible);
            editor.putBoolean("page_highlight_visible", FullscreenActivity.page_highlight_visible);
            editor.putBoolean("page_links_visible", FullscreenActivity.page_links_visible);
            editor.putBoolean("page_metronome_visible", FullscreenActivity.page_metronome_visible);
            editor.putBoolean("page_notation_visible", FullscreenActivity.page_notation_visible);
            editor.putBoolean("page_pad_visible", FullscreenActivity.page_pad_visible);
            editor.putBoolean("page_pages_visible", FullscreenActivity.page_pages_visible);
            editor.putBoolean("page_set_visible", FullscreenActivity.page_set_visible);
            editor.putBoolean("page_sticky_visible", FullscreenActivity.page_sticky_visible);
            editor.putFloat("pageButtonAlpha", FullscreenActivity.pageButtonAlpha);
            editor.putString("pagebutton_position", FullscreenActivity.pagebutton_position);
            editor.putString("pagebutton_scale", FullscreenActivity.pagebutton_scale);
            /*editor.putInt("pageturner_AUTOSCROLL", FullscreenActivity.pageturner_AUTOSCROLL);
            editor.putInt("pageturner_AUTOSCROLLPAD", FullscreenActivity.pageturner_AUTOSCROLLPAD);
            editor.putInt("pageturner_AUTOSCROLLMETRONOME", FullscreenActivity.pageturner_AUTOSCROLLMETRONOME);
            editor.putInt("pageturner_AUTOSCROLLPADMETRONOME", FullscreenActivity.pageturner_AUTOSCROLLPADMETRONOME);
            editor.putInt("pageturner_DOWN", FullscreenActivity.pageturner_DOWN);
            editor.putInt("pageturner_METRONOME", FullscreenActivity.pageturner_METRONOME);
            editor.putInt("pageturner_NEXT", FullscreenActivity.pageturner_NEXT);
            editor.putInt("pageturner_PAD", FullscreenActivity.pageturner_PAD);
            editor.putInt("pageturner_PADMETRONOME", FullscreenActivity.pageturner_PADMETRONOME);
            editor.putInt("pageturner_PREVIOUS", FullscreenActivity.pageturner_PREVIOUS);
            editor.putInt("pageturner_UP", FullscreenActivity.pageturner_UP);
            */
            editor.putInt("pedal1", FullscreenActivity.pedal1);
            editor.putInt("pedal2", FullscreenActivity.pedal2);
            editor.putInt("pedal3", FullscreenActivity.pedal3);
            editor.putInt("pedal4", FullscreenActivity.pedal4);
            editor.putInt("pedal5", FullscreenActivity.pedal5);
            editor.putInt("pedal6", FullscreenActivity.pedal6);
            editor.putString("pedal1longaction", FullscreenActivity.pedal1longaction);
            editor.putString("pedal2longaction", FullscreenActivity.pedal2longaction);
            editor.putString("pedal3longaction", FullscreenActivity.pedal3longaction);
            editor.putString("pedal4longaction", FullscreenActivity.pedal4longaction);
            editor.putString("pedal5longaction", FullscreenActivity.pedal5longaction);
            editor.putString("pedal6longaction", FullscreenActivity.pedal6longaction);
            editor.putString("pedal1shortaction", FullscreenActivity.pedal1shortaction);
            editor.putString("pedal2shortaction", FullscreenActivity.pedal2shortaction);
            editor.putString("pedal3shortaction", FullscreenActivity.pedal3shortaction);
            editor.putString("pedal4shortaction", FullscreenActivity.pedal4shortaction);
            editor.putString("pedal5shortaction", FullscreenActivity.pedal5shortaction);
            editor.putString("pedal6shortaction", FullscreenActivity.pedal6shortaction);
            editor.putFloat("popupAlpha_All", FullscreenActivity.popupAlpha_All);
            editor.putFloat("popupAlpha_Set", FullscreenActivity.popupAlpha_Set);
            editor.putFloat("popupDim_All", FullscreenActivity.popupDim_All);
            editor.putFloat("popupDim_Set", FullscreenActivity.popupDim_Set);
            editor.putString("popupPosition_All", FullscreenActivity.popupPosition_All);
            editor.putString("popupPosition_Set", FullscreenActivity.popupPosition_Set);
            editor.putFloat("popupScale_All", FullscreenActivity.popupScale_All);
            editor.putFloat("popupScale_Set", FullscreenActivity.popupScale_Set);
            editor.putString("prefChord_Aflat_Gsharp", FullscreenActivity.prefChord_Aflat_Gsharp);
            editor.putString("prefChord_Bflat_Asharp", FullscreenActivity.prefChord_Bflat_Asharp);
            editor.putString("prefChord_Dflat_Csharp", FullscreenActivity.prefChord_Dflat_Csharp);
            editor.putString("prefChord_Eflat_Dsharp", FullscreenActivity.prefChord_Eflat_Dsharp);
            editor.putString("prefChord_Gflat_Fsharp", FullscreenActivity.prefChord_Gflat_Fsharp);
            editor.putString("prefChord_Aflatm_Gsharpm", FullscreenActivity.prefChord_Aflatm_Gsharpm);
            editor.putString("prefChord_Bflatm_Asharpm", FullscreenActivity.prefChord_Bflatm_Asharpm);
            editor.putString("prefChord_Dflatm_Csharpm", FullscreenActivity.prefChord_Dflatm_Csharpm);
            editor.putString("prefChord_Eflatm_Dsharpm", FullscreenActivity.prefChord_Eflatm_Dsharpm);
            editor.putString("prefChord_Gflatm_Fsharpm", FullscreenActivity.prefChord_Gflatm_Fsharpm);
            editor.putString("prefStorage", FullscreenActivity.prefStorage);
            editor.putString("presenterChords", FullscreenActivity.presenterChords);
            editor.putFloat("presoAlertSize", FullscreenActivity.presoAlertSize);
            editor.putFloat("presoAlpha", FullscreenActivity.presoAlpha);
            editor.putFloat("presoAuthorSize", FullscreenActivity.presoAuthorSize);
            editor.putBoolean("presoAutoScale", FullscreenActivity.presoAutoScale);
            editor.putFloat("presoCopyrightSize", FullscreenActivity.presoCopyrightSize);
            editor.putInt("presoFontSize", FullscreenActivity.presoFontSize);
            editor.putInt("presoInfoAlign", FullscreenActivity.presoInfoAlign);
            editor.putInt("presoLyricsAlign", FullscreenActivity.presoLyricsAlign);
            editor.putInt("presoMaxFontSize", FullscreenActivity.presoMaxFontSize);
            editor.putBoolean("presoShowChords", FullscreenActivity.presoShowChords);
            editor.putFloat("presoTitleSize", FullscreenActivity.presoTitleSize);
            editor.putInt("presoTransitionTime", FullscreenActivity.presoTransitionTime);
            editor.putString("profile", FullscreenActivity.profile);
            editor.putString("quickLaunchButton_1", FullscreenActivity.quickLaunchButton_1);
            editor.putString("quickLaunchButton_2", FullscreenActivity.quickLaunchButton_2);
            editor.putString("quickLaunchButton_3", FullscreenActivity.quickLaunchButton_3);
            editor.putString("quickLaunchButton_4", FullscreenActivity.quickLaunchButton_4);
            editor.putString("randomFolders", FullscreenActivity.randomFolders);
            editor.putFloat("scrollDistance", FullscreenActivity.scrollDistance);
            editor.putInt("scrollSpeed", FullscreenActivity.scrollSpeed);
            editor.putBoolean("showAlphabeticalIndexInSongMenu",FullscreenActivity.showAlphabeticalIndexInSongMenu);
            editor.putBoolean("showCapoAsNumerals",FullscreenActivity.showCapoAsNumerals);
            editor.putBoolean("showCapoChords", FullscreenActivity.showCapoChords);
            editor.putBoolean("showChords", FullscreenActivity.showChords);
            editor.putBoolean("showLyrics", FullscreenActivity.showLyrics);
            editor.putBoolean("showNativeAndCapoChords", FullscreenActivity.showNativeAndCapoChords);
            editor.putString("showNextInSet", FullscreenActivity.showNextInSet);
            editor.putBoolean("showSetTickBoxInSongMenu",FullscreenActivity.showSetTickBoxInSongMenu);
            editor.putInt("showSplashVersion", FullscreenActivity.showSplashVersion);
            editor.putString("songfilename", FullscreenActivity.songfilename);
            editor.putFloat("stagemodeFloat", FullscreenActivity.stagemodeScale);
            editor.putInt("stickyNotesShowSecs", FullscreenActivity.stickyNotesShowSecs);
            editor.putFloat("stickyOpacity", FullscreenActivity.stickyOpacity);
            editor.putFloat("stickyTextSize", FullscreenActivity.stickyTextSize);
            editor.putInt("stickyWidth", FullscreenActivity.stickyWidth);
            editor.putInt("SWIPE_MAX_OFF_PATH", FullscreenActivity.SWIPE_MAX_OFF_PATH);
            editor.putInt("SWIPE_MIN_DISTANCE", FullscreenActivity.SWIPE_MIN_DISTANCE);
            editor.putInt("SWIPE_THRESHOLD_VELOCITY", FullscreenActivity.SWIPE_THRESHOLD_VELOCITY);
            editor.putString("swipeDrawer", FullscreenActivity.swipeDrawer);
            editor.putBoolean("swipeForMenus", FullscreenActivity.swipeForMenus);
            editor.putBoolean("swipeForSongs", FullscreenActivity.swipeForSongs);
            editor.putString("swipeSet", FullscreenActivity.swipeSet);
            editor.putFloat("timerFontSizeAutoScroll", FullscreenActivity.timerFontSizeAutoScroll);
            editor.putFloat("timerFontSizePad", FullscreenActivity.timerFontSizePad);
            editor.putBoolean("timeFormat24h", FullscreenActivity.timeFormat24h);
            editor.putBoolean("timeOn", FullscreenActivity.timeOn);
            editor.putFloat("timeSize", FullscreenActivity.timeSize);
            editor.putBoolean("toggleAutoHighlight", FullscreenActivity.toggleAutoHighlight);
            editor.putString("toggleAutoSticky", FullscreenActivity.toggleAutoSticky);
            editor.putString("togglePageButtons", FullscreenActivity.togglePageButtons);
            editor.putString("toggleScrollArrows", FullscreenActivity.toggleScrollArrows);
            editor.putString("toggleScrollBeforeSwipe", FullscreenActivity.toggleScrollBeforeSwipe);
            editor.putString("toggleYScale", FullscreenActivity.toggleYScale);
            editor.putString("transposeStyle", FullscreenActivity.transposeStyle);
            editor.putBoolean("trimSections", FullscreenActivity.trimSections);
            editor.putBoolean("trimSectionSpace", FullscreenActivity.trimSectionSpace);
            if (FullscreenActivity.uriTree!=null) {
                editor.putString("uriTree", FullscreenActivity.uriTree.toString());
            }
            editor.putBoolean("usePresentationOrder", FullscreenActivity.usePresentationOrder);
            editor.putBoolean("visualmetronome", FullscreenActivity.visualmetronome);
            editor.putString("whichMode", FullscreenActivity.whichMode);
            editor.putString("whichSetCategory", FullscreenActivity.whichSetCategory);
            editor.putString("whichSongFolder", FullscreenActivity.whichSongFolder);
            editor.putInt("xmargin_presentation", FullscreenActivity.xmargin_presentation);
            editor.putInt("ymargin_presentation", FullscreenActivity.ymargin_presentation);

            //editor.apply();
            editor.apply();
        } catch (Exception e) {
            // Error saving.  Normally happens if app was closed before this happens
            e.printStackTrace();
            // Try restarting the app
            FullscreenActivity.restart(FullscreenActivity.mContext);
        }
    }

    public static void loadSongPrep() {
        try {
            SharedPreferences.Editor editor = myPreferences.edit();
            editor.putBoolean("songloadsuccess", false);
            editor.apply();
        } catch (Exception e) {
            // Error here
            e.printStackTrace();
        }
    }

    public static void loadSongSuccess() {
        try {
            SharedPreferences.Editor editor = myPreferences.edit();
            editor.putBoolean("songloadsuccess", true);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean wasSongLoaded() {
        return myPreferences.getBoolean("songloadsuccess", false);
    }

}
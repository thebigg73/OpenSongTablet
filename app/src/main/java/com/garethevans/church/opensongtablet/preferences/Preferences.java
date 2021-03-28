package com.garethevans.church.opensongtablet.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Preferences extends Activity {
    // This is the way that preferences will be stored
    private SharedPreferences sharedPref;

    // Get the saved preference values
    public String getMyPreferenceString(Context c, String prefname, String fallback) {
        // Return a string from saved preference
        if (c!=null && prefname!=null) {
            sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
            return sharedPref.getString(prefname, fallback);
        } else {
            return fallback;
        }
    }
    public int getMyPreferenceInt(Context c, String prefname, int fallback) {
        // Return an int from saved preference
        if (c!=null && prefname!=null) {
            sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
            return sharedPref.getInt(prefname, fallback);
        } else {
            return fallback;
        }
    }
    public float getMyPreferenceFloat (Context c, String prefname, float fallback) {
        // Return a float from saved preferences
        if (c!=null && prefname!=null) {
            sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
            return sharedPref.getFloat(prefname, fallback);
        } else {
            return fallback;
        }
    }
    public boolean getMyPreferenceBoolean(Context c, String prefname, boolean fallback) {
        // Return a boolean from saved preference
        if (c!=null && prefname!=null) {
            sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
            return sharedPref.getBoolean(prefname, fallback);
        } else {
            return fallback;
        }
    }

    // Set the preference values
    public void setMyPreferenceString(Context c, String prefname, String value) {
        // Identify the preferences
        if (c!=null && prefname!=null) {
            sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
            sharedPref.edit().putString(prefname, value).apply();
        }
    }
    public void setMyPreferenceInt (Context c, String prefname, int value) {
        // Identify the preferences
        if (c!=null && prefname!=null) {
            sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
            sharedPref.edit().putInt(prefname, value).apply();
        }
    }
    public void setMyPreferenceFloat (Context c, String prefname, float value) {
        // Identify the preferences
        if (c!=null && prefname!=null) {
            sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
            sharedPref.edit().putFloat(prefname, value).apply();
        }
    }
    public void setMyPreferenceBoolean(Context c, String prefname, boolean value) {
        // Identify the preferences
        if (c!=null && prefname!=null) {
            sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
            sharedPref.edit().putBoolean(prefname, value).apply();
        }
    }

    // The ints used in the app
    public int getFinalInt(String which) {
        int value = -1;
        switch (which) {
            case "REQUEST_FILE_CHOOSER":
                value = 5500;
                break;
            case "REQUEST_OSB_FILE":
                value = 5501;
                break;
            case "REQUEST_IOS_FILE":
                value = 5502;
                break;
        }
        return value;
    }





    // Below is an alphabetical list of all the user preferences stored in the app!
    //TODO
    // customLogoSize is specified, but no option in the app to change it
    /*

    Variable name                   Type        What
    addSectionSpace                 boolean     Should a spacing line be added between sections to improve readability (def:true)
    airTurnMode                     boolean     Should autorepeat onKeyUp (multiple from keyRepeatCount variable) be converted to longKeyPress actions for AirTurn pedals (def:false)
    appTheme                        String      The theme to use (dark, light, custom1, custom2) (def:dark)
    autoscrollAutoStart             boolean     Should autoscroll start on page load (needs to be started manually the first time) (def:false)
    autoscrollDefaultSongLength     int         The default length of a song to use for autoscroll (def:180)
    autoscrollDefaultSongPreDelay   int         The default length of the predelay to use with autoscroll (default:20)
    autoscrollDefaultMaxPreDelay    int         The default max of the autoscroll predelay slider (default:30)
    autoscrollLargeFontInfoBar      boolean     The text size of the floating autoscroll info bar (default:true = 20.0f.  false = 14.0f)
    autoscrollUseDefaultTime        boolean     If not time has been set for the song, should we use the default when starting (def:false)
    backgroundImage1                String      The uri of the background image 1 for presentations (def: ost_bg.png)
    backgroundImage2                String      The uri of the background image 2 for presentations (def: ost_bg.png)
    backgroundVideo1                String      The uri of the background video 1 for presentations (def:"")
    backgroundVideo2                String      The uri of the background video 2 for presentations (def:"")
    backgroundToUse                 String      Which background are we using (img1, img2, vid1, vid2) (def:img1)
    backgroundTypeToUse             String      Is the background an image or a video (def:image)
    batteryDialOn                   boolean     Should the battery circle be shown in the action bar (def:true)
    batteryDialThickness            int         The thickness of the battery dial in the action bar (def:4)
    batteryTextOn                   boolean     Should the battery percentage text be shown in the action bar (def:true)
    batteryTextSize                 float       The size of the battery text (def:9.0f)
    bibleCurrentFile                String      The last used local bible XML file (def:"")
    blockShadow                     boolean     Should second screen text be displayed on block shadowed text boxes (def:false)
    blockShadowAlpha                float       Alpha of block shadow (def:0.7f)
    capoInfoAsNumerals              boolean     Should the capo info bar use Roman numerals (def:false)
    capoLargeFontInfoBar            boolean     The text size of the floating capo info bar (def:true is 20.0f false is 14.0f)
    castRotation                    float       The rotation of the cast display 0, 90, 180, 270.  (def:0.0f)
    ccliAutomaticLogging            boolean     Should we automatically write to the ccli log (def:false)
    ccliChurchName                  String      The name of the church for CCLI logging (def:"")
    ccliLicence                     String      The CCLI licence number (def:"")
    chordFormat                     int         My preferred chord format (def:1=normal, 2=Bb->B and B->H, 3=same as 2, but with is/es/as. 4=doremi, 5=nashvillenumber 6=nashvillenumeral) (def:1)
    chordFormatUsePreferred         boolean     When transposing, should we assume we are using preferred chord format (def:true)
    chordInstrument                 String      The default instrument for showing chords (def:g)
    chosenstorage                   String      The uri of the document tree (Storage Access Framework) (def:null)
    clock24hFormat                  boolean     Should the clock be shown in 24hr format (def:true)
    clockOn                         boolean     Should the clock be shown in the action bar (def:true)
    clockTextSize                   float       The size of the clock font (def:9.0f)
    custom1_lyricsBackgroundColor   int         The color for the lyrics background in the custom1 theme
    custom1_lyricsBridgeColor       int         The color for the background for the bridge in the custom1 theme
    custom1_lyricsCapoColor         int         The color for the capo text in the custom1 theme
    custom1_lyricsChordsColor       int         The color for the chords text in the custom1 theme
    custom1_lyricsChorusColor       int         The color for the background for the chorus in the custom1 theme
    custom1_lyricsCommentColor      int         The color for the background for the comment in the custom1 theme
    custom1_lyricsCustomColor       int         The color for the background for the custom section in the custom1 theme
    custom1_lyricsPreChorusColor    int         The color for the background for the prechorus in the custom1 theme
    custom1_lyricsTagColor          int         The color for the background for the tag in the custom1 theme
    custom1_lyricsTextColor         int         The color for the lyrics text in the custom1 theme
    custom1_lyricsVerseColor        int         The color for the background for the verse in the custom1 theme,
    custom1_presoFontColor          int         The color for the presentation text in the custom1 theme
    custom1_presoShadowColor        int         The color for the presentation text shadow in the custom1 theme
    custom1_presoInfoColor          int         The color for the presentation info text in the custom1 theme
    custom1_presoAlertColor         int         The color for the presentation alert text in the custom1 theme
    custom1_metronomeColor          int         The color for the metronome background in the custom1 theme
    custom1_pageButtonsColor        int         The color for the page buttons info text in the custom1 theme
    custom1_stickyTextColor         int         The color for the sticky note text info text in the custom1 theme
    custom1_stickyBackgroundColor   int         The color for the sticky note background info text in the custom1 theme
    custom1_extraInfoTextColor      int         The color for the extra info text in the custom1 theme
    custom1_extraInfoBgColor        int         The color for the extra info background in the custom1 theme
    custom2_lyricsBackgroundColor   int         The color for the lyrics background in the custom2 theme
    custom2_lyricsBridgeColor       int         The color for the background for the bridge in the custom2 theme
    custom2_lyricsCapoColor         int         The color for the capo text in the custom2 theme
    custom2_lyricsChordsColor       int         The color for the chords text in the custom2 theme
    custom2_lyricsChorusColor       int         The color for the background for the chorus in the custom2 theme
    custom2_lyricsCommentColor      int         The color for the background for the comment in the custom2 theme
    custom2_lyricsCustomColor       int         The color for the background for the custom section in the custom2 theme
    custom2_lyricsPreChorusColor    int         The color for the background for the prechorus in the custom2 theme
    custom2_lyricsTagColor          int         The color for the background for the tag in the custom2 theme
    custom2_lyricsTextColor         int         The color for the lyrics text in the custom2 theme
    custom2_lyricsVerseColor        int         The color for the background for the verse in the custom2 theme,
    custom2_presoFontColor          int         The color for the presentation text in the custom2 theme
    custom2_presoShadowColor        int         The color for the presentation text shadow in the custom2 theme
    custom2_presoInfoColor          int         The color for the presentation info text in the custom2 theme
    custom2_presoAlertColor         int         The color for the presentation alert text in the custom2 theme
    custom2_metronomeColor          int         The color for the metronome background in the custom2 theme
    custom2_pageButtonsColor        int         The color for the page buttons info text in the custom2 theme
    custom2_stickyTextColor         int         The color for the sticky note text info text in the custom2 theme
    custom2_stickyBackgroundColor   int         The color for the sticky note background info text in the custom2 theme
    custom2_extraInfoTextColor      int         The color for the extra info text in the custom2 theme
    custom2_extraInfoBgColor        int         The color for the extra info background in the custom2 theme
    customLogo                      String      The uri of the user logo for presentations (def:"")
    customLogoSize                  float       Size of the custom logo (% of screen)
    customPadAb                     String      Custom pad uri for the key specified
    customPadA                      String      Custom pad uri for the key specified
    customPadBb                     String      Custom pad uri for the key specified
    customPadB                      String      Custom pad uri for the key specified
    customPadC                      String      Custom pad uri for the key specified
    customPadDb                     String      Custom pad uri for the key specified
    customPadD                      String      Custom pad uri for the key specified
    customPadEb                     String      Custom pad uri for the key specified
    customPadE                      String      Custom pad uri for the key specified
    customPadF                      String      Custom pad uri for the key specified
    customPadGb                     String      Custom pad uri for the key specified
    customPadG                      String      Custom pad uri for the key specified
    customPadAbm                    String      Custom pad uri for the key specified
    customPadAm                     String      Custom pad uri for the key specified
    customPadBbm                    String      Custom pad uri for the key specified
    customPadBm                     String      Custom pad uri for the key specified
    customPadCm                     String      Custom pad uri for the key specified
    customPadDbm                    String      Custom pad uri for the key specified
    customPadDm                     String      Custom pad uri for the key specified
    customPadEbm                    String      Custom pad uri for the key specified
    customPadEm                     String      Custom pad uri for the key specified
    customPadFm                     String      Custom pad uri for the key specified
    customPadGbm                    String      Custom pad uri for the key specified
    customPadGm                     String      Custom pad uri for the key specified
    dark_lyricsBackgroundColor      int         The color for the lyrics background in the dark theme
    dark_lyricsBridgeColor          int         The color for the background for the bridge in the dark theme
    dark_lyricsCapoColor            int         The color for the capo text in the dark theme
    dark_lyricsChordsColor          int         The color for the chords text in the dark theme
    dark_lyricsChorusColor          int         The color for the background for the chorus in the dark theme
    dark_lyricsCommentColor         int         The color for the background for the comment in the dark theme
    dark_lyricsCustomColor          int         The color for the background for the custom section in the dark theme
    dark_lyricsPreChorusColor       int         The color for the background for the prechorus in the dark theme
    dark_lyricsTagColor             int         The color for the background for the tag in the dark theme
    dark_lyricsTextColor            int         The color for the lyrics text in the dark theme
    dark_lyricsVerseColor           int         The color for the background for the verse in the dark theme,
    dark_presoFontColor             int         The color for the presentation text in the dark theme
    dark_presoShadowColor           int         The color for the presentation text shadow in the dark theme
    dark_presoInfoColor             int         The color for the presentation info text in the dark theme
    dark_presoAlertColor            int         The color for the presentation alert text in the dark theme
    dark_metronomeColor             int         The color for the metronome background in the dark theme
    dark_pageButtonsColor           int         The color for the page buttons info text in the dark theme
    dark_stickyTextColor            int         The color for the sticky note text info text in the dark theme
    dark_stickyBackgroundColor      int         The color for the sticky note background info text in the dark theme
    dark_extraInfoTextColor         int         The color for the extra info text in the dark theme
    dark_extraInfoBgColor           int         The color for the extra info background in the dark theme
    deviceId                        String      The device name for Nearby Connections (def:Bluetooth name/device manufacturer+model)
    displayCapoChords               boolean     Should capo chords be shown (def:true)
    displayCapoAndNativeChords      boolean     Should both chords be shown at once (def:false)
    displayChords                   boolean     Decides if chords should be shown (def:true)
    displayLyrics                   boolean     Decides if lyrics should be shown (def:true)
    displayNextInSet                String      Should the next song in set be shown (N)o, (T)op inline, (B)ottom inline (def:B)
    displayBoldChordsHeadings       boolean     Should the chords and headings be shown in a bold font (def:false)
    drawingAutoDisplay              boolean     Should the highlighter drawings be shown on page load (def:true)
    drawingEraserSize               int         The default size of the eraser (def:20)
    drawingHighlighterColor         int         The color of the highlighter (StaticVariables.highlighteryellow)
    drawingHighlighterSize          int         The default size of the highlighter (def:20)
    drawingPenColor                 int         The colour of the pen (def:StaticVariables.black)
    drawingPenSize                  int         The default size of the pen (def:20)
    drawingTool                     String      The current drawing tool (def:hghlighter)
    editAsChordPro                  boolean     Should the song edit window be ChordPro format (def:false)
    exportOpenSongAppSet            boolean     Should we export .osts file (def:true)
    exportOpenSongApp               boolean     Should we export .ost file (def:true)
    exportDesktop                   boolean     Should we export desktop xml file (def:true)
    exportText                      boolean     Should we export .txt file (def:false)
    exportChordPro                  boolean     Should we export .chopro file (def:false)
    exportOnSong                    boolean     Should we export .onsong file (def:false)
    exportImage                     boolean     Should we export .png file (def:false)
    exportPDF                       boolean     Should we export .pdf file (def:false)
    fontSize                        float       The non-scaled font size (def:42.0f)
    fontSizeMax                     float       The max font size (def:50.0f)
    fontSizeMin                     float       The min font size (def:8.0f)
    fontChord                       String      The name of the font used for the chords.  From fonts.google.com (def:lato)
    fontLyric                       String      The name of the font used for the lyrics.  From fonts.google.com (def:lato)
    fontPreso                       String      The name of the font used for the preso.  From fonts.google.com (def:lato)
    fontPresoInfo                   String      The name of the font used for the presoinfo.  From fonts.google.com (def:lato)
    fontSizePreso                   float       The non-scale presentation font size (def:14.0f)
    fontSizePresoMax                float       The maximum autoscaled font size (def:40.0f)
    fontSticky                      String      The name of the font used for the sticky notes.  From fonts.google.com (def:lato)
    gestureScreenDoubleTap          int         The action for double tapping on the song screen (def 2 = edit song - based on menu position)
    gestureScreenLongPress          int         The action for long pressing on the song screen (def 3 = add song to set - based on menu position)
    hideActionBar                   boolean     Should the action bar auto hide (def:false)
    hideLyricsBox                   boolean     Should we hide the box around the lyrics (def:false)
    keyRepeatCount                  int         The number of key repeats to count as a long press for AirTurn pedals (def:20)
    language                        String      The locale set in the menu (def:en)
    lastUsedVersion                 int         The app version number the last time the app ran (def:0)
    light_lyricsBackgroundColor     int         The color for the lyrics background in the light theme
    light_lyricsBridgeColor         int         The color for the background for the bridge in the light theme
    light_lyricsCapoColor           int         The color for the capo text in the light theme
    light_lyricsChordsColor         int         The color for the chords text in the light theme
    light_lyricsChorusColor         int         The color for the background for the chorus in the light theme
    light_lyricsCommentColor        int         The color for the background for the comment in the light theme
    light_lyricsCustomColor         int         The color for the background for the custom section in the light theme
    light_lyricsPreChorusColor      int         The color for the background for the prechorus in the light theme
    light_lyricsTagColor            int         The color for the background for the tag in the light theme
    light_lyricsTextColor           int         The color for the lyrics text in the light theme
    light_lyricsVerseColor          int         The color for the background for the verse in the light theme,
    light_presoFontColor            int         The color for the presentation text in the light theme
    light_presoShadowColor          int         The color for the presentation text shadow in the light theme
    light_presoInfoColor            int         The color for the presentation info text in the light theme
    light_presoAlertColor           int         The color for the presentation alert text in the light theme
    light_metronomeColor            int         The color for the metronome background in the light theme
    light_pageButtonsColor          int         The color for the page buttons info text in the light theme
    light_stickyTextColor           int         The color for the sticky note text info text in the light theme
    light_stickyBackgroundColor     int         The color for the sticky note background info text in the light theme
    light_extraInfoTextColor        int         The color for the extra info text in the light theme
    light_extraInfoBgColor          int         The color for the extra info background in the light theme
    lineSpacing                     float       The line spacing trim value to use (def:0.1f)
    menuSize                        int         The width of the side menus (min 100 max 400) (def:250)
    metronomeAutoStart              boolean     Should the metronome autostart with song (after manually starting first time) (def:false)
    metronomeLength                 int         Number of bars the metronome stays on for (0=indefinitely) (def:0)
    metronomePan                    String      The panning of the metronome sound L, C, R (def:C)
    metronomeVol                    float       The volume of the metronome (def:0.5f)
    metronomeShowVisual             boolean     Should the metronome be visual (flash action bar) (def:false)
    midiAsPedal                     boolean     Should the midi device trigger pedal commands (def:false)
    midiSendAuto                    boolean     Should the midi info in the song be sent on song load automatically (def:false)
    multiLineVerseKeepCompact       boolean     Should multiline verses be kept compact (def:false)
    nearbyHostMenuOnly              boolean     Should the host only listen for clients when the nearby menu is open (def:false)
    padAutoStart                    boolean     Should the pad autostart with song (after manually starting first time) (def:false)
    padCrossFadeTime                int         The time in ms used to fade out a pad.  Set in the PopUpCrossFade fragment (def::8000)
    padLargeFontInfoBar             boolean     The text size of the floating pad info bar (def:true is 20.0f false is 14.0f)
    padPan                          String      The panning of the pad (L, C or R) (def:C)
    PadVol                          float       The volume of the pad (def:1.0f)
    pageButtonAlpha                 float       The opacity of the page/quicklaunnch button (0.6f)
    pageButtonGroupCustom           boolean     Group the custom page buttons (def:false)
    pageButtonGroupExtra            boolean     Group the extra info page buttons (def:false)
    pageButtonGroupMain             boolean     Group the main page buttons (def:false)
    pageButtonSize                  int         The size of the page/quicklaunch buttons (SIZE_NORMAL or SIZE_MINI) (def:FloatingActionButton.SIZE_NORMAL)
    pageButtonShowAutoscroll        boolean     Should the autroscroll page button be shown (def:true)
    pageButtonShowChords            boolean     Should the chord symbol page button be shown (def:false)
    pageButtonShowCustom1           boolean     Should the custom1 page button be shown (def:true)
    pageButtonShowCustom2           boolean     Should the custom2 page button be shown (def:true)
    pageButtonShowCustom3           boolean     Should the custom3 page button be shown (def:true)
    pageButtonShowCustom4           boolean     Should the custom4 page button be shown (def:true)
    pageButtonShowHighlighter       boolean     Should the highlighter page button be shown (def:false)
    pageButtonShowLinks             boolean     Should the links page button be shown (def:false)
    pageButtonShowMetronome         boolean     Should the metronome page button be shown (def:false)
    pageButtonShowNotation          boolean     Should the notation page button be shown (def:false)
    pageButtonShowPad               boolean     Should the pad page button be shown (def:true)
    pageButtonShowPageSelect        boolean     Should the page select page button be shown (def:false)
    pageButtonShowScroll            boolean     Should the scroll page buttons be shown (def:true)
    pageButtonShowSet               boolean     Should the set page button be shown (def:true)
    pageButtonShowSetMove           boolean     Should the set forward/back page buttons be shown (def:true)
    pageButtonShowSticky            boolean     Should the sticky notes page button be shown (def:false)
    pageButtonCustom1Action         String      The action for custom button 1 (def:transpose)
    pageButtonCustom2Action         String      The action for custom button 2 (def:"")
    pageButtonCustom3Action         String      The action for custom button 3 (def:"")
    pageButtonCustom4Action         String      The action for custom button 4 (def:"")
    pageButtonCustom5Action         String      The action for custom button 5 (def:"")
    pageButtonCustom6Action         String      The action for custom button 6 (def:"")
    pedal1Code                      int         The keyboard int code assigned to pedal 1 (default is 21 - left arrow)
    pedal1Midi                      String      The midi note assigned to pedal 1 (default:C3)
    pedal1LongPressAction           String      The action called when pedal 1 is long pressed (default is songmenu)
    pedal1ShortPressAction          String      The action called when pedal 1 is short pressed (default is prev)
    pedal2Code                      int         The keyboard int code assigned to pedal 2 (default is 22 - right arrow)
    pedal2Midi                      String      The midi note assigned to pedal 1 (default:C3)
    pedal2LongPressAction           String      The action called when pedal 2 is long pressed (default is set)
    pedal2ShortPressAction          String      The action called when pedal 2 is short pressed (default is next)
    pedal3Code                      int         The keyboard int code assigned to pedal 3 (default is 19 - up arrow)
    pedal3Midi                      String      The midi note assigned to pedal 1 (default:C3)
    pedal3LongPressAction           String      The action called when pedal 3 is long pressed (default is "")
    pedal3ShortPressAction          String      The action called when pedal 3 is short pressed (default is "")
    pedal4Code                      int         The keyboard int code assigned to pedal 4 (default is 20 - down arrow)
    pedal4Midi                      String      The midi note assigned to pedal 1 (default:C3)
    pedal4LongPressAction           String      The action called when pedal 4 is long pressed (default is "")
    pedal4ShortPressAction          String      The action called when pedal 4 is short pressed (default is "")
    pedal5Code                      int         The keyboard int code assigned to pedal 5 (default is 92 - page up)
    pedal5Midi                      String      The midi note assigned to pedal 1 (default:C3)
    pedal5LongPressAction           String      The action called when pedal 5 is long pressed (default is "")
    pedal5ShortPressAction          String      The action called when pedal 5 is short pressed (default is "")
    pedal6Code                      int         The keyboard int code assigned to pedal 6 (default is 93 - page down)
    pedal6Midi                      String      The midi note assigned to pedal 1 (default:C3)
    pedal6LongPressAction           String      The action called when pedal 6 is long pressed (default is "")
    pedal6ShortPressAction          String      The action called when pedal 6 is short pressed (default is "")
    pedal7Code                      int         The keyboard int code assigned to pedal 7 (default is -1 = nothing)
    pedal7Midi                      String      The midi note assigned to pedal 1 (default:C3)
    pedal7LongPressAction           String      The action called when pedal 7 is long pressed (default is "")
    pedal7ShortPressAction          String      The action called when pedal 7 is short pressed (default is "")
    pedal8Code                      int         The keyboard int code assigned to pedal 8 (default is -1 = nothing)
    pedal8Midi                      String      The midi note assigned to pedal 1 (default:C3)
    pedal8LongPressAction           String      The action called when pedal 8 is long pressed (default is "")
    pedal8ShortPressAction          String      The action called when pedal 8 is short pressed (default is "")
    pedalScrollBeforeMove           boolean     Should the prev/next pedal buttons try to scroll first (makes 2 pedals into 4) (def:true)
    pedalShowWarningBeforeMove      boolean     Should an 'are you sure' toast warning be shown before moving to next item in the set (def:false)
    popupAlpha                      float       The opacity of the popup windows (def:0.8f)
    popupDim                        float       The darkness of the main window when the popup is open (def:0.8f)
    popupPosition                   String      The position of the popups (tl, tc, tr, l, c, r, bl, bc, br) (def:c)
    popupScale                      float       The size of the popup relative to the page size (def:0.7f)
    prefKey_Ab                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps) (def:true)
    prefKey_Bb                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps) (def:true)
    prefKey_Db                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps) (def:false)
    prefKey_Eb                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps) (def:true)
    prefKey_Gb                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps) (def:false)
    prefKey_Abm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps) (def:false)
    prefKey_Bbm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps) (def:true)
    prefKey_Dbm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps) (def:false)
    prefKey_Ebm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps) (def:true)
    prefKey_Gbm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps) (def:false)
    presoAlertText                  String      The text for the alert in Presentation mode (def:"")
    presoAlertTextSize              float       The size of the alert text in Presentation mode (def:12.0f)
    presoAuthorTextSize             float       The size of the alert text in Presentation mode (def:12.0f)
    presoAutoScale                  boolean     Should the presenter window use autoscale for text (def:true)
    presoAutoUpdateProjector        boolean     Should the projector be updated automatically in PresenterMode when something changes (def:true)
    presoBackgroundAlpha            float       The alpha value for the presentation background (def:1.0f)
    presoCopyrightTextSize          float       The size of the alert text in Presentation mode (def:12.0f)
    presoInfoAlign                  int         The align gravity of the info in presentation mode (Gravity.END)
    presoLyricsAlign                int         The align gravity of the lyrics in presentation mode (Gravity.HORIZONTAL_CENTER)
    presoLyricsVAlign               int         The vertical align gravity of the lyrics in presentation mode (Gravity.TOP)
    presoLyricsBold                 boolean     Should the presentation lyrics be displayed in bold (def:false)
    presoShowChords                 boolean     Should chords be shown in the presentation window (def:false)
    presoTitleTextSize              float       The size of the alert text in Presentation mode (def:14.0f)
    presoTransitionTime             int         The time for transitions between items in presenter mode (ms) (def:800)
    presoXMargin                    int         The margin for the X axis on the presentation window (def:20)
    presoYMargin                    int         The margin for the Y axis on the presentation window (def:10)
    profileName                     String      The last loaded or saved profile name (def:"")
    randomSongFolderChoice          String      A list of folders to include in the random song generation ($$_folder_$$) (def:"")
    runswithoutbackup               int         The number of times the app has opened without backup (prompt the user after 10) (def:0)
    scaleChords                     float       The scale factor for chords relative to the lyrics (def:0.8f)
    scaleComments                   float       The scale factor for comments relative to the lyrics (def:0.8f)
    scaleHeadings                   float       The scale factor for headings relative to the lyrics (def:0.6f)
    scrollDistance                  float       The percentage of the screen that is scrolled using the scroll buttons/pedals (def:0.7f)
    scrollSpeed                     int         How quick should the scroll animation be (def:1500)
    searchAka                       boolean     Should the aka be included in the search (def:true)
    searchAuthor                    boolean     Should the author be included in the search (def:true)
    searchCCLI                      boolean     Should the ccli be included in the search (def:true)
    searchCopyright                 boolean     Should the copyright be included in the search (def:true)
    searchFilename                  boolean     Should the filename be included in the search (def:true)
    searchFolder                    boolean     Should the folder be included in the search (def:true)
    searchHymn                      boolean     Should the hymn number be included in the search (def:true)
    searchKey                       boolean     Should the key be included in the search (def:true)
    searchLyrics                    boolean     Should the lyrics be included in the search (def:true)
    searchTheme                     boolean     Should the theme be included in the search (def:true)
    searchTitle                     boolean     Should the title be included in the search (def:true)
    searchUser1                     boolean     Should the user1 be included in the search (def:true)
    searchUser2                     boolean     Should the user2 be included in the search (def:true)
    searchUser3                     boolean     Should the user3 be included in the search (def:true)
    setCurrent                      String      The current set (each item enclosed in $**_folder/song_**$) - gets parsed on loading app (def:"")
    setCurrentBeforeEdits           String      The current set before edits.  Used as a comparison to decide save action(def:"")
    setCurrentLastName              String      The last name used when saving or loading a set(def:"")
    songAuthorSize                  float       The size of the song author text in the action bar (def:11.0f)
    songAutoScale                   String      Choice of autoscale mode (Y)es, (W)idth only or (N)one (def:W)
    songAutoScaleColumnMaximise     boolean     When autoscale is on full and columns are used, should each column scale independently to maximise font size (def:true)
    songAutoScaleOverrideFull       boolean     If the app can override full autoscale if the font is too small (def:true)
    songAutoScaleOverrideWidth      boolean     If the app can override width autoscale if the font is too small (def:false)
    songfilename                    String      The name of the current song file (def:"")
    songListSearchByFolder          boolean     Should we search in the song list using a custom folder (def:true)
    songListSearchByArtist          boolean     Should we search in the song list using a custom artist (def:false)
    songListSearchByKey             boolean     Should we search in the song list using a custom key (def:false)
    songListSearchByTag             boolean     Should we search in the song list using a custom folder (def:false)
    songListSearchByFilter          boolean     Should we search in the song list using a custom filter (def:false)
    songLoadSuccess                 boolean     Indicates if the song loaded correctly (won't load a song next time if it crashed) (def:false)
    songMenuAlphaIndexShow          boolean     Should we show the alphabetical index in the song menu (def:true)
    songMenuAlphaIndexSize          float       The text size for the alphabetical index in the song menu (def:14.0f)
    songMenuSetTicksShow            boolean     Should we show the ticks identifying song is in the set in the song menu (def:true)
    songTitleSize                   float       The size of the song title text in the action bar (def:13.0f)
    soundMeterRange                 int         The volume range of the sound meter (def:400)
    stageModeScale                  float       The max height of each stage mode section (to allow next section to peek at bottom) (def:0.80f)
    stickyAutoDisplay               String      Where should sticky notes be shown (N)one, (T)op inline, (B)ottom inline, (F)loating window (def:F)
    stickyWidth                     int         The width of popup sticky notes (def:400)
    stickyAlpha                     float       The alpha of popup sticky notes (def:0.8f)
    stickyLargeFont                 boolean     The text size for the popup sticky notes (true=20.0f, def:false=14.0f)
    stickyXPosition                 int         The x position of the popup sticky note (def: -1 which means figure it out first)
    stickyYPosition                 int         The y position of the popup sticky note (def: -1 which means figure it out first)
    swipeForMenus                   boolean     Can we swipe the menus in or out (def:true)
    swipeForSongs                   boolean     Can we swipe to move between song items (def:true)
    swipeMinimumDistance            int         The minimum distance for a swipe to be registered (dp) (def:250)
    swipeMinimumVelocity            int         The minimum speed for a swipe to be registered (dp/s) (def:600)
    swipeMaxDistanceYError          int         The maximum Y movement in a swipe allowed for it to be recognised (def:200)
    timeToDisplayHighlighter        int         The time to show highlighter notes for before hiding (def=0 means keep on) ms
    timeToDisplaySticky             int         The time to show sticky notes for before hiding (def=0 means keep on) s
    trimSections                    boolean     Should whitespace be removed from song sections (def:true)
    trimLines                       boolean     Should the lines be trimmed (using the lineSpacing) value (def:true)
    uriTree                         String      A string representation of the user root location (may be the OpenSong folder or its parent) (def:"")
    uriTreeHome                     String      A string representation of the user home location (The OpenSong folder) (def:"")
    usePresentationOrder            boolean     Should the song be parsed into the specified presentation order
    whichSetCategory                String      Which set category are we browsing (category___setname) (def:c.getString(R.string.mainfoldername))
    whichMode                       String      Which app mode - Stage, Performance, Presentation (def:Performance)
    whichSongFolder                 String      The song folder we are currently in (def:c.getString(R.string.mainfoldername))

    */

}
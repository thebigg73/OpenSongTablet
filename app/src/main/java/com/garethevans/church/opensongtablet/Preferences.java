package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Preferences extends Activity {
    // This is the way that preferences will be stored
    private SharedPreferences sharedPref;

    // Get the saved preference values
    String getMyPreferenceString (Context c, String prefname, String fallback) {
        // Return a string from saved preference
        // Identify the preferences
        sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
        return sharedPref.getString(prefname, fallback);
    }
    int getMyPreferenceInt (Context c, String prefname, int fallback) {
        // Return an int from saved preference
        // Identify the preferences
        //sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
        return sharedPref.getInt(prefname, fallback);
    }
    float getMyPreferenceFloat (Context c, String prefname, float fallback) {
        // Return a float from saved preferences
        sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
        return sharedPref.getFloat(prefname, fallback);
    }
    boolean getMyPreferenceBoolean(Context c, String prefname, boolean fallback) {
        // Return a boolean from saved preference
        // Identify the preferences
        //sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
        return sharedPref.getBoolean(prefname, fallback);
    }

    // Set the preference values
    void setMyPreferenceString (Context c, String prefname, String value) {
        // Identify the preferences
        sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
        sharedPref.edit().putString(prefname, value).apply();
    }
    void setMyPreferenceInt (Context c, String prefname, int value) {
        // Identify the preferences
        //sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
        sharedPref.edit().putInt(prefname, value).apply();
    }
    void setMyPreferenceFloat (Context c, String prefname, float value) {
        // Identify the preferences
        sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
        sharedPref.edit().putFloat(prefname, value).apply();
    }
    void setMyPreferenceBoolean (Context c, String prefname, boolean value) {
        // Identify the preferences
        //sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        sharedPref = c.getSharedPreferences("CurrentPreferences", Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean(prefname, value).apply();
    }


    // Below is an alphabetical list of all the user preferences stored in the app!
    //TODO
    // customLogoSize is specified, but no option in the app to change it
    /*

    Variable name                   Type        What
    addSectionSpace                 boolean     Should a spacing line be added between sections to improve readability
    appTheme                        String      The theme to use (dark, light, custom1, custom2)
    autoscrollAutoStart             boolean     Should autoscroll start on page load (needs to be started manually the first time)
    autoscrollDefaultSongLength     int         The default length of a song to use for autoscroll
    autoscrollDefaultSongPreDelay   int         The default length of the predelay to use with autoscroll
    autoscrollDefaultMaxPreDelay    int         The default max of the autoscroll predelay slider
    autoscrollLargeFontInfoBar      boolean     The text size of the floating autoscroll info bar (true is 20.0f false is 14.0f)
    autoscrollUseDefaultTime        boolean     If not time has been set for the song, should we use the default when starting
    backgroundImage1                String      The uri of the background image 1 for presentations
    backgroundImage2                String      The uri of the background image 2 for presentations
    backgroundVideo1                String      The uri of the background video 1 for presentations
    backgroundVideo2                String      The uri of the background video 2 for presentations
    backgroundToUse                 String      Which background are we using (img1, img2, vid1, vid2)
    backgroundTypeToUse             String      Is the background an image or a video
    batteryDialOn                   boolean     Should the battery circle be shown in the action bar
    batteryDialThickness            int         The thickness of the battery dial in the action bar
    batteryTextOn                   boolean     Should the battery percentage text be shown in the action bar
    batteryTextSize                 float       The size of the battery text
    bibleCurrentFile                String      The last used local bible XML file
    capoInfoAsNumerals              boolean     Should the capo info bar use Roman numerals
    capoLargeFontInfoBar            boolean     The text size of the floating capo info bar (true is 20.0f false is 14.0f)
    ccliAutomaticLogging            boolean     Should we automatically write to the ccli log
    ccliChurchName                  String      The name of the church for CCLI logging
    ccliLicence                     String      The CCLI licence number
    chordFormat                     int         My preferred chord format (1=normal, 2=Bb->B and B->H, 3=same as 2, but with is/es/as. 4=doremi, 5=nashvillenumber 6=nashvillenumeral)
    chordFormatUsePreferred         boolean     When transposing, should we assume we are using preferred chord format
    chordInstrument                 String      The default instrument for showing chords
    chosenstorage                   String      The uri of the document tree (Storage Access Framework)
    clock24hFormat                  boolean     Should the clock be shown in 24hr format
    clockOn                         boolean     Should the clock be shown in the action bar
    clockTextSize                   float       The size of the clock font
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
    customLogo                      String      The uri of the user logo for presentations
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
    displayCapoChords               boolean     Should capo chords be shown
    displayCapoAndNativeChords      boolean     Should both chords be shown at once
    displayChords                   boolean     Decides if chords should be shown
    displayLyrics                   boolean     Decides if lyrics should be shown
    displayNextInSet                String      Should the next song in set be shown (N)o, (T)op inline, (B)ottom inline
    drawingAutoDisplay              boolean     Should the highlighter drawings be shown on page load
    drawingEraserSize               int         The default size of the eraser
    drawingHighlighterColor         int         The color of the highlighter
    drawingHighlighterSize          int         The default size of the highlighter
    drawingPenColor                 int         The colour of the pen
    drawingPenSize                  int         The default size of the pen
    drawingTool                     String      The current drawing tool
    editAsChordPro                  boolean     Should the song edit window be ChordPro format
    exportOpenSongAppSet            boolean     Should we export .osts file
    exportOpenSongApp               boolean     Should we export .ost file
    exportDesktop                   boolean     Should we export desktop xml file
    exportText                      boolean     Should we export .txt file
    exportChordPro                  boolean     Should we export .chopro file
    exportOnSong                    boolean     Should we export .onsong file
    exportImage                     boolean     Should we export .png file
    exportPDF                       boolean     Should we export .pdf file
    fontSize                        float       The non-scaled font size
    fontSizeMax                     float       The max font size
    fontSizeMin                     float       The min font size
    fontChord                       String      The name of the font used for the chords.  From fonts.google.com
    fontCustom                      String      The name of the font used for custom fonts.  From fonts.google.com
    fontLyric                       String      The name of the font used for the lyrics.  From fonts.google.com
    fontPreso                       String      The name of the font used for the preso.  From fonts.google.com
    fontPresoInfo                   String      The name of the font used for the presoinfo.  From fonts.google.com
    fontSizePreso                   float       The non-scale presentation font size
    fontSizePresoMax                float       The maximum autoscaled font size
    gestureScreenDoubleTap          int         The action for double tapping on the song screen (def 2 = edit song - based on menu position)
    gestureScreenLongPress          int         The action for long pressing on the song screen (def 3 = add song to set - based on menu position)
    hideActionBar                   boolean     Should the action bar auto hide
    hideLyricsBox                   boolean     Should we hide the box around the lyrics
    language                        String      The locale set in the menu
    lastUsedVersion                 int         The app version number the last time the app ran
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
    lineSpacing                     float       The line spacing trim value to use
    menuSize                        int         The width of the side menus (min 100 max 400)
    metronomeAutoStart              boolean     Should the metronome autostart with song (after manually starting first time)
    metronomePan                    String      The panning of the metronome sound L, C, R
    metronomeVol                    float       The volume of the metronome
    metronomeShowVisual             boolean     Should the metronome be visual (flash action bar)
    midiSendAuto                    boolean     Should the midi info in the song be sent on song load automatically
    multiLineVerseKeepCompact       boolean     Should multiline verses be kept compact
    padAutoStart                    boolean     Should the pad autostart with song (after manually starting first time)
    padLargeFontInfoBar             boolean     The text size of the floating pad info bar (true is 20.0f false is 14.0f)
    padPan                          String      The panning of the pad (L, C or R)
    PadVol                          float       The volume of the pad
    pageButtonAlpha                 float       The opacity of the page/quicklaunnch button
    pageButtonGroupCustom           boolean     Group the custom page buttons
    pageButtonGroupExtra            boolean     Group the extra info page buttons
    pageButtonGroupMain             boolean     Group the main page buttons
    pageButtonSize                  int         The size of the page/quicklaunch buttons (SIZE_NORMAL or SIZE_MINI)
    pageButtonShowAutoscroll        boolean     Should the autroscroll page button be shown
    pageButtonShowChords            boolean     Should the chord symbol page button be shown
    pageButtonShowCustom1           boolean     Should the custom1 page button be shown
    pageButtonShowCustom2           boolean     Should the custom2 page button be shown
    pageButtonShowCustom3           boolean     Should the custom3 page button be shown
    pageButtonShowCustom4           boolean     Should the custom4 page button be shown
    pageButtonShowHighlighter       boolean     Should the highlighter page button be shown
    pageButtonShowLinks             boolean     Should the links page button be shown
    pageButtonShowMetronome         boolean     Should the metronome page button be shown
    pageButtonShowNotation          boolean     Should the notation page button be shown
    pageButtonShowPad               boolean     Should the pad page button be shown
    pageButtonShowPageSelect        boolean     Should the page select page button be shown
    pageButtonShowScroll            boolean     Should the scroll page buttons be shown
    pageButtonShowSet               boolean     Should the set page button be shown
    pageButtonShowSetMove           boolean     Should the set forward/back page buttons be shown
    pageButtonShowSticky            boolean     Should the sticky notes page button be shown
    pageButtonCustom1Action         String      The action for custom button 1
    pageButtonCustom2Action         String      The action for custom button 2
    pageButtonCustom3Action         String      The action for custom button 3
    pageButtonCustom4Action         String      The action for custom button 4
    pedal1Code                      int         The keyboard int code assigned to pedal 1 (default is 21 - left arrow)
    pedal1LongPressAction           String      The action called when pedal 1 is long pressed (default is songmenu)
    pedal1ShortPressAction          String      The action called when pedal 1 is short pressed (default is prev)
    pedal2Code                      int         The keyboard int code assigned to pedal 2 (default is 22 - right arrow)
    pedal2LongPressAction           String      The action called when pedal 2 is long pressed (default is set)
    pedal2ShortPressAction          String      The action called when pedal 2 is short pressed (default is next)
    pedal3Code                      int         The keyboard int code assigned to pedal 3 (default is 19 - up arrow)
    pedal3LongPressAction           String      The action called when pedal 3 is long pressed (default is songmenu)
    pedal3ShortPressAction          String      The action called when pedal 3 is short pressed (default is prev)
    pedal4Code                      int         The keyboard int code assigned to pedal 4 (default is 20 - down arrow)
    pedal4LongPressAction           String      The action called when pedal 4 is long pressed (default is set)
    pedal4ShortPressAction          String      The action called when pedal 4 is short pressed (default is next)
    pedal5Code                      int         The keyboard int code assigned to pedal 5 (default is 92 - page up)
    pedal5LongPressAction           String      The action called when pedal 5 is long pressed (default is songmenu)
    pedal5ShortPressAction          String      The action called when pedal 5 is short pressed (default is prev)
    pedal6Code                      int         The keyboard int code assigned to pedal 6 (default is 93 - page down)
    pedal6LongPressAction           String      The action called when pedal 6 is long pressed (default is set)
    pedal6ShortPressAction          String      The action called when pedal 6 is short pressed (default is next)
    pedalScrollBeforeMove           boolean     Should the prev/next pedal buttons try to scroll first (makes 2 pedals into 4)
    popupAlpha                      float       The opacity of the popup windows
    popupDim                        float       The darkness of the main window when the popup is open
    popupPosition                   String      The position of the popups (tl, tc, tr, l, c, r, bl, bc, br)
    popupScale                      float       The size of the popup relative to the page size
    prefKey_Ab                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
    prefKey_Bb                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
    prefKey_Db                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
    prefKey_Eb                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
    prefKey_Gb                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
    prefKey_Abm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
    prefKey_Bbm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
    prefKey_Dbm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
    prefKey_Ebm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
    prefKey_Gbm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
    presoAlertText                  String      The text for the alert in Presentation mode
    presoAlertTextSize              float       The size of the alert text in Presentation mode
    presoAutoScale                  boolean     Should the presenter window use autoscale for text
    presoAutoUpdateProjector        boolean     Should the projector be updated automatically in PresenterMode when something changes
    presoBackgroundAlpha            float       The alpha value for the presentation background
    presoInfoAlign                  int         The align gravity of the info in presentation mode
    presoLyricsAlign                int         The align gravity of the lyrics in presentation mode
    presoShowChords                 boolean     Should chords be shown in the presentation window
    presoTransitionTime             int         The time for transitions between items in presenter mode (ms)
    presoXMargin                    int         The margin for the X axis on the presentation window
    presoYMargin                    int         The margin for the Y axis on the presentation window
    profileName                     String      The last loaded or saved profile name
    randomSongFolderChoice          String      A list of folders to include in the random song generation ($$_folder_$$)
    runswithoutbackup               int         The number of times the app has opened without backup (prompt the user after 10)
    scaleChords                     float       The scale factor for chords relative to the lyrics
    scaleComments                   float       The scale factor for comments relative to the lyrics
    scaleHeadings                   float       The scale factor for headings relative to the lyrics
    scrollDistance                  float       The percentage of the screen that is scrolled using the scroll buttons/pedals
    scrollSpeed                     int         How quick should the scroll animation be
    searchAka                       boolean     Should the aka be included in the search
    searchAuthor                    boolean     Should the author be included in the search
    searchCCLI                      boolean     Should the ccli be included in the search
    searchCopyright                 boolean     Should the copyright be included in the search
    searchFilename                  boolean     Should the filename be included in the search
    searchFolder                    boolean     Should the folder be included in the search
    searchHymn                      boolean     Should the hymn number be included in the search
    searchKey                       boolean     Should the key be included in the search
    searchLyrics                    boolean     Should the lyrics be included in the search
    searchTheme                     boolean     Should the theme be included in the search
    searchTitle                     boolean     Should the title be included in the search
    searchUser1                     boolean     Should the user1 be included in the search
    searchUser2                     boolean     Should the user2 be included in the search
    searchUser3                     boolean     Should the user3 be included in the search
    setCurrent                      String      The current set (each item enclosed in $**_folder/song_**$) - gets parsed on loading app
    setCurrentBeforeEdits           String      The current set before edits.  Used as a comparison to decide save action
    setCurrentLastName              String      The last name used when saving or loading a set
    songAuthorSize                  float       The size of the song author text in the action bar
    songAutoScale                   String      Choice of autoscale mode (Y)es, (W)idth only or (N)one
    songAutoScaleOverrideFull       boolean     If the app can override full autoscale if the font is too small
    songAutoScaleOverrideWidth      boolean     If the app can override width autoscale if the font is too small
    songfilename                    String      The name of the current song file
    songLoadSuccess                 boolean     Indicates if the song loaded correctly (won't load a song next time if it crashed)
    songMenuAlphaIndexShow          boolean     Should we show the alphabetical index in the song menu
    songMenuAlphaIndexSize          float       The text size for the alphabetical index in the song menu
    songMenuSetTicksShow            boolean     Should we show the ticks identifying song is in the set in the song menu
    songTitleSize                   float       The size of the song title text in the action bar
    soundMeterRange                 int         The volume range of the sound meter
    stageModeScale                  float       The max height of each stage mode section (to allow next section to peek at bottom)
    stickyAutoDisplay               String      Where should sticky notes be shown (N)one, (T)op inline, (B)ottom inline, (F)loating window
    stickyWidth                     int         The width of popup sticky notes
    stickyAlpha                     float       The alpha of popup sticky notes
    stickyLargeFont                 boolean     The text size for the popup sticky notes (true=20.0f, false=14.0f)
    swipeForMenus                   boolean     Can we swipe the menus in or out
    swipeForSongs                   boolean     Can we swipe to move between song items
    swipeMinimumDistance            int         The minimum distance for a swipe to be registered (dp)
    swipeMinimumVelocity            int         The minimum speed for a swipe to be registered (dp/s)
    swipeMaxDistanceYError          int         The maximum Y movement in a swipe allowed for it to be recognised
    timeToDisplayHighlighter        int         The time to show highlighter notes for before hiding (def=0 means keep on) ms
    timeToDisplaySticky             int         The time to show sticky notes for before hiding (def=0 means keep on) s
    trimSections                    boolean     Should whitespace be removed from song sections
    trimLines                       boolean     Should the lines be trimmed (using the lineSpacing) value
    uriTree                         String      A string representation of the user root location (may be the OpenSong folder or its parent)
    uriTreeHome                     String      A string representation of the user home location (The OpenSong folder)
    usePresentationOrder            boolean     Should the song be parsed into the specified presentation order
    whichSetCategory                String      Which set category are we browsing (category___setname)
    whichMode                       String      Which app mode - Stage, Performance, Presentation
    whichSongFolder                 String      The song folder we are currently in

    */

}
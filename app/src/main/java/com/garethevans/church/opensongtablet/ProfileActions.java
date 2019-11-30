package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;

import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import static android.provider.DocumentsContract.EXTRA_INITIAL_URI;

class ProfileActions {

    boolean doSaveProfile(Context c, Preferences preferences, StorageAccess storageAccess, Uri to) {
        boolean result = true;  // Returns true on success.  Catches throw to false
        try {
            // This is used to copy the current preferences xml file to the chosen name / location
            // Check the file exists, if not create it
            if (!storageAccess.uriExists(c, to)) {
                String name = to.getLastPathSegment();
                storageAccess.lollipopCreateFileForOutputStream(c, preferences, to, null, "Profiles", "", name);
            }

            // Different versions of Android save the preferences in different locations.
            Uri prefsFile = getPrefsFile(c, storageAccess);

            InputStream inputStream = storageAccess.getInputStream(c, prefsFile);
            OutputStream outputStream = storageAccess.getOutputStream(c, to);


            storageAccess.copyFile(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    @SuppressLint("SdCardPath")
    private Uri getPrefsFile(Context c, StorageAccess storageAccess) {
        Uri uri;
        File root;

        //Try the Samsung version
        root = new File("/dbdata/databases/" + c.getPackageName() + "/shared_prefs/");
        uri = Uri.fromFile(root);

        // If not there, try the default
        if (uri==null || !storageAccess.uriExists(c,uri)) {
            // Use the default method
            root = new File("/data/data/" + c.getPackageName() + "/shared_prefs/CurrentPreferences.xml");
            uri = Uri.fromFile(root);
        }
        return uri;
    }

    boolean doLoadProfile(Context c, Preferences preferences, StorageAccess storageAccess, Uri uri) {
        // This class will import saved profiles/settings.
        // Old settings will work up to a point

        boolean result = true;  // Returns true on success.  Catches throw to false;
        try {
            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp;
            xpp = factory.newPullParser();

            InputStream inputStream = storageAccess.getInputStream(c, uri);
            xpp.setInput(inputStream, null);

            int eventType;
            eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    // Try to extract the xpp value and next text if they exists
                    String xppGetName = xpp.getName();
                    String xppValue = "";
                    String xppNextText = "";
                    if (xppGetName.equals("int") || xppGetName.equals("float") ||
                            xppGetName.equals("boolean") || xppGetName.equals("string")) {
                        // This is a newer XML preferences file where the values are stored like
                        // <boolean name="showChords" value="true" />
                        for (int i=0;i<xpp.getAttributeCount();i++) {
                            if (xpp.getAttributeName(i).equals("name")) {
                                xppGetName = xpp.getAttributeValue(i);
                            } else if (xpp.getAttributeName(i).equals("value")) {
                                xppValue = xpp.getAttributeValue(i);
                            }
                            xppNextText = getXppNextText(xpp);
                        }
                    } else {
                        // This is an older XML preferences file where the values are stored like
                        // <showChords>"true"</showChords>
                        xppValue = getXppValue(xpp);
                        xppNextText = getXppNextText(xpp);
                    }

                    // By default, we want to use the xppValue.
                    if (xppValue.equals("")) {
                        xppValue = xppNextText;
                    }

                    switch (xppGetName) {

                        case "addSectionSpace":                 // New preference
                        case "trimSectionSpace":                // Old preference
                            // addSectionsSpace               boolean     Should a spacing line be added between sections to improve readability
                            preferences.setMyPreferenceBoolean(c,"addSectionSpace", getBooleanValue(xppValue, false));
                            break;

                        case "appTheme":                        // New preference
                        case "mDisplayTheme":                   // Old preference
                            //appTheme                        String      The theme to use (dark, light, custom1, custom2
                            if (xppValue.equals("Theme_Holo_Light")) {
                                xppValue = "light";
                            } else if (xppValue.equals("Theme.Holo")) {
                                xppValue = "dark";
                            }
                            StaticVariables.mDisplayTheme = getTextValue(xppValue,"dark");
                            preferences.setMyPreferenceString(c,"appTheme",StaticVariables.mDisplayTheme);
                            break;

                        case "autoscrollAutoStart":             // New preference
                        case "autostartautoscroll":             // Old preference
                            //autoscrollAutoStart             boolean     Should autoscroll start on page load (needs to be started manually the first time)
                            preferences.setMyPreferenceBoolean(c,xppValue,false);
                            break;

                        case "autoscrollDefaultSongLength":     // New preference
                        case "default_autoscroll_songlength":   // Old preference
                            //autoscrollDefaultSongLength     int         The default length of a song to use for autoscroll
                            preferences.setMyPreferenceInt(c,"autoscrollDefaultSongLength",getIntegerValue(xppValue,180));
                            break;

                        case "autoscrollDefaultSongPreDelay":   // New preference
                        case "default_autoscroll_predelay":     // Old preference
                            //autoscrollDefaultSongPreDelay   int         The default length of the predelay to use with autoscroll
                            preferences.setMyPreferenceInt(c,"autoscrollDefaultSongPreDelay",getIntegerValue(xppValue,20));
                            break;

                        case "autoscrollDefaultMaxPreDelay":     // New preference
                        case "default_autoscroll_predelay_max":
                            //autoscrollDefaultMaxPreDelay    int         The default max of the autoscroll predelay slider
                            preferences.setMyPreferenceInt(c,"autoscrollDefaultMaxPreDelay",getIntegerValue(xppValue,30));
                            break;

                        case "autoscrollLargeFontInfoBar":      // New preference only
                        case "timerFontSizeAutoScroll":
                            if (xppValue.startsWith("20")) {
                                xppValue = "true";
                            } else if (xppValue.startsWith("14")) {
                                xppValue = "false";
                            }
                            //autoscrollLargeFontInfoBar      boolean     The text size of the floating autoscroll info bar (default is true = 20.0f.  false = 14.0f)
                            preferences.setMyPreferenceBoolean(c,"autoscrollLargeFontInfoBar",getBooleanValue(xppValue,true));
                            break;

                        case "autoscrollUseDefaultTime":        // New preference
                        case "autoscroll_default_or_prompt":    // Old preference
                            //autoscrollUseDefaultTime        boolean     If not time has been set for the song, should we use the default when starting (def:false)
                            if (xppValue.equals("prompt")) { // old
                                xppValue = "false";
                            } else if (xppValue.equals("default")) {
                                xppValue = "true";
                            }
                            preferences.setMyPreferenceBoolean(c,"autoscrollUseDefaultTime",getBooleanValue(xppValue,false));
                            break;

                        case "backgroundImage1":        // New and old preference
                            //backgroundImage1                String      The uri of the background image 1 for presentations
                            preferences.setMyPreferenceString(c,"backgroundImage1",getTextValue(xppValue,"ost_bg.png"));
                            break;

                        case "backgroundImage2":        // New and old preference
                            //backgroundImage2                String      The uri of the background image 2 for presentations
                            preferences.setMyPreferenceString(c,"backgroundImage2",getTextValue(xppValue,"ost_bg.png"));
                            break;

                        case "backgroundVideo1":        // New and old preference
                            //backgroundVideo1                String      The uri of the background video 1 for presentations
                            preferences.setMyPreferenceString(c,"backgroundVideo1",getTextValue(xppValue,""));
                            break;

                        case "backgroundVideo2":        // New and old preference
                            //backgroundVideo2                String      The uri of the background video 2 for presentations
                            preferences.setMyPreferenceString(c,"backgroundVideo2",getTextValue(xppValue,""));
                            break;

                        case "backgroundToUse":        // New and old preference
                            //backgroundToUse                 String      Which background are we using (img1, img2, vid1, vid2)
                            preferences.setMyPreferenceString(c,"backgroundToUse",getTextValue(xppValue,"img1"));
                            break;

                        case "backgroundTypeToUse":
                            //backgroundTypeToUse             String      Is the background an image or a video
                            preferences.setMyPreferenceString(c,"backgroundTypeToUse",getTextValue(xppValue,"image"));
                            break;

                        case "batteryDialOn":           // New and old preference
                            //batteryDialOn                   boolean     Should the battery circle be shown in the action bar
                            preferences.setMyPreferenceBoolean(c,"batteryDialOn",getBooleanValue(xppValue,true));
                            break;

                        case "batteryDialThickness":    // New and old preference
                        case "batteryLine":
                            //batteryDialThickness            int         The thickness of the battery dial in the action bar
                            preferences.setMyPreferenceInt(c,"batteryDialThickness",getIntegerValue(xppValue,4));
                            break;

                        case "batteryTextOn":        // New and old preference
                        case "batteryOn":
                            //batteryTextOn                   boolean     Should the battery percentage text be shown in the action bar
                            preferences.setMyPreferenceBoolean(c,"batteryTextOn",getBooleanValue(xppValue,true));
                            break;

                        case "batteryTextSize":        // New and old preference
                        case "batterySize":
                            //batteryTextSize                 float       The size of the battery text
                            preferences.setMyPreferenceFloat(c,"batteryTextSize",getFloatValue(xppValue,9.0f));
                            break;

                        case "bibleCurrentFile":        // New preference
                        case "bibleFile":               // Old preference
                            //bibleCurrentFile                String      The last used local bible XML file
                            preferences.setMyPreferenceString(c,"bibleCurrentFile",getTextValue(xppValue,""));
                            break;

                        case "blockShadow":             // New preference
                            //blockShadow                       boolean     Should second screen text be displayed on block shadowed text boxes (def:false)
                            preferences.setMyPreferenceBoolean(c,"blockShadow",getBooleanValue(xppValue,false));
                            break;

                        case "blockShadowAlpha":
                            //blockShadowAlpha                float       Alpha of block shadow (def:0.7f)
                            preferences.setMyPreferenceFloat(c,"blockShadowAlpha",getFloatValue(xppValue,0.7f));
                            break;

                        case "capoInfoAsNumerals":        // New preference
                        case "showCapoAsNumerals":        // Old preference
                            //capoInfoAsNumerals              boolean     Should the capo info bar use Roman numerals
                            preferences.setMyPreferenceBoolean(c,"capoInfoAsNumerals",getBooleanValue(xppValue,false));
                            break;

                        case "capoLargeFontInfoBar":        // New preference
                        case "capoFontSizeInfoBar":         // Old preference
                            //capoLargeFontInfoBar            boolean     The text size of the floating capo info bar (true is 20.0f false is 14.0f)
                            if (xppValue.startsWith("14")) {
                                xppValue = "false";
                            } else if (xppValue.startsWith("20")) {
                                xppValue = "true";
                            }
                            preferences.setMyPreferenceBoolean(c,"capoLargeFontInfoBar",getBooleanValue(xppValue,true));
                            break;

                        case "ccliAutomaticLogging":        // New preference
                        case "ccli_automatic":              // Old preference
                            //ccliAutomaticLogging            boolean     Should we automatically write to the ccli log
                            preferences.setMyPreferenceBoolean(c,"ccliAutomaticLogging",getBooleanValue(xppValue,false));
                            break;

                        case "ccliChurchName":        // New preference
                        case "ccli_church":        // Old preference
                            //ccliChurchName                  String      The name of the church for CCLI logging
                            preferences.setMyPreferenceString(c,"ccliChurchName",getTextValue(xppValue,""));
                            break;

                        case "ccliLicence":        // New preference
                        case "ccli_licence":       // Old preference
                            //ccliLicence                     String      The CCLI licence number
                            preferences.setMyPreferenceString(c,"ccliLicence",getTextValue(xppValue,""));
                            break;

                        case "chordFormat":        // New and old preference
                            //chordFormat                     int         My preferred chord format (1=normal, 2=Bb->B and B->H, 3=same as 2, but with is/es/as. 4=doremi, 5=nashvillenumber 6=nashvillenumeral)
                            preferences.setMyPreferenceInt(c,"chordFormat",getIntegerValue(xppValue,1));
                            break;

                        case "chordFormatUsePreferred":        // New preference
                        case "alwaysPreferredChordFormat":     // Old preference
                            //chordFormatUsePreferred         boolean     When transposing, should we assume we are using preferred chord format
                            if (xppValue.equals("Y")) {
                                xppValue = "true";
                            } else if (xppValue.equals("N")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"chordFormatUsePreferred",getBooleanValue(xppValue,true));
                            break;

                        case "chordInstrument":        // New and old preference
                            //chordInstrument                 String      The default instrument for showing chords
                            preferences.setMyPreferenceString(c,"chordInstrument",getTextValue(xppValue,"g"));
                            break;

                        case "chosenstorage":        // New preference only
                            //chosenstorage                   String      The uri of the document tree (Storage Access Framework)
                            preferences.setMyPreferenceString(c,"chosenstorage",getTextValue(xppValue,null));
                            break;

                        case "clock24hFormat":        // New preference only
                        case "timeFormat24h":
                            //clock24hFormat                  boolean     Should the clock be shown in 24hr format
                            preferences.setMyPreferenceBoolean(c,"clock24hFormat",getBooleanValue(xppValue,true));
                            break;

                        case "clockOn":        // New preference only
                        case "timeOn":
                            //clockOn                         boolean     Should the clock be shown in the action bar
                            preferences.setMyPreferenceBoolean(c,"clockOn",getBooleanValue(xppValue,true));
                            break;

                        case "clockTextSize":        // New preference only
                        case "timeSize":
                            //clockTextSize                   float       The size of the clock font
                            preferences.setMyPreferenceFloat(c,"clockTextSize",getFloatValue(xppValue,9.0f));
                            break;

                        case "custom1_lyricsBackgroundColor":        // New preference only
                            //custom1_lyricsBackgroundColor   int         The color for the lyrics background in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_lyricsBackgroundColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom1_lyricsBridgeColor":        // New preference only
                            //custom1_lyricsBridgeColor       int         The color for the background for the bridge in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_lyricsBridgeColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom1_lyricsCapoColor":        // New preference only
                            //custom1_lyricsCapoColor         int         The color for the capo text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_lyricsCapoColor",getIntegerValue(xppValue,StaticVariables.red));
                            break;
                        case "custom1_lyricsChordsColor":        // New preference only
                            //custom1_lyricsChordsColor       int         The color for the chords text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_lyricsChordsColor",getIntegerValue(xppValue,StaticVariables.yellow));
                            break;
                        case "custom1_lyricsChorusColor":        // New preference only
                            //custom1_lyricsChorusColor       int         The color for the background for the chorus in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_lyricsChorusColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom1_lyricsCommentColor":        // New preference only
                            //custom1_lyricsCommentColor      int         The color for the background for the comment in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_lyricsCommentColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom1_lyricsCustomColor":        // New preference only
                            //custom1_lyricsCustomColor       int         The color for the background for the custom section in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_lyricsCustomColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom1_lyricsPreChorusColor":        // New preference only
                            //custom1_lyricsPreChorusColor    int         The color for the background for the prechorus in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_lyricsPreChorusColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom1_lyricsTagColor":        // New preference only
                            //custom1_lyricsTagColor          int         The color for the background for the tag in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_lyricsTagColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom1_lyricsTextColor":        // New preference only
                            //custom1_lyricsTextColor         int         The color for the lyrics text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_lyricsTextColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom1_lyricsVerseColor":        // New preference only
                            //custom1_lyricsVerseColor        int         The color for the background for the verse in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_lyricsVerseColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom1_presoFontColor":        // New preference only
                        case "custom1_presoFont":
                            //custom1_presoFontColor          int         The color for the presentation text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_presoFontColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom1_presoShadowColor":        // New preference only
                        case "custom1_presoShadow":
                            //custom1_presoShadowColor        int         The color for the presentation text shadow in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_presoShadowColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom1_presoInfoColor":        // New preference only
                        case "custom1_presoInfoFont":
                            //custom1_presoInfoColor          int         The color for the presentation info text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_presoInfoColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom1_presoAlertColor":        // New preference only
                        case "custom1_presoAlertFont":
                            //custom1_presoAlertColor         int         The color for the presentation alert text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_presoAlertColor",getIntegerValue(xppValue,StaticVariables.red));
                            break;
                        case "custom1_metronomeColor":        // New preference only
                        case "custom1_metronome":
                            //custom1_metronomeColor          int         The color for the metronome background in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_metronomeColor",getIntegerValue(xppValue,StaticVariables.darkishred));
                            break;
                        case "custom1_pageButtonsColor":        // New preference only
                        case "custom1_pagebuttons":
                            //custom1_pageButtonsColor        int         The color for the page buttons info text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_pageButtonsColor",getIntegerValue(xppValue,StaticVariables.purplyblue));
                            break;
                        case "custom1_stickyTextColor":        // New preference only
                            //custom1_stickyTextColor         int         The color for the sticky note text info text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_stickyTextColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom1_stickyBackgroundColor":        // New preference only
                            //custom1_stickyBackgroundColor   int         The color for the sticky note background info text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_stickyBackgroundColor",getIntegerValue(xppValue,StaticVariables.lightyellow));
                            break;
                        case "custom1_extraInfoTextColor":        // New preference only
                        case "custom1_extrainfo":
                            //custom1_extraInfoTextColor      int         The color for the extra info text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_extraInfoTextColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom1_extraInfoBgColor":        // New preference only
                        case "custom1_extrainfobg":
                            //custom1_extraInfoBgColor        int         The color for the extra info background in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom1_extraInfoBgColor",getIntegerValue(xppValue,StaticVariables.grey));
                            break;

                        case "custom2_lyricsBackgroundColor":        // New preference only
                            //custom2_lyricsBackgroundColor   int         The color for the lyrics background in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_lyricsBackgroundColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom2_lyricsBridgeColor":        // New preference only
                            //custom2_lyricsBridgeColor       int         The color for the background for the bridge in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_lyricsBridgeColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom2_lyricsCapoColor":        // New preference only
                            //custom2_lyricsCapoColor         int         The color for the capo text in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_lyricsCapoColor",getIntegerValue(xppValue,StaticVariables.red));
                            break;
                        case "custom2_lyricsChordsColor":        // New preference only
                            //custom2_lyricsChordsColor       int         The color for the chords text in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_lyricsChordsColor",getIntegerValue(xppValue,StaticVariables.darkblue));
                            break;
                        case "custom2_lyricsChorusColor":        // New preference only
                            //custom2_lyricsChorusColor       int         The color for the background for the chorus in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_lyricsChorusColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom2_lyricsCommentColor":        // New preference only
                            //custom2_lyricsCommentColor      int         The color for the background for the comment in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_lyricsCommentColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom2_lyricsCustomColor":        // New preference only
                            //custom2_lyricsCustomColor       int         The color for the background for the custom section in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_lyricsCustomColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom2_lyricsPreChorusColor":        // New preference only
                            //custom2_lyricsPreChorusColor    int         The color for the background for the prechorus in the custom1 theme
                            preferences.setMyPreferenceInt(c,"custom2_lyricsPreChorusColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom2_lyricsTagColor":        // New preference only
                            //custom2_lyricsTagColor          int         The color for the background for the tag in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_lyricsTagColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom2_lyricsTextColor":        // New preference only
                            //custom2_lyricsTextColor         int         The color for the lyrics text in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_lyricsTextColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom2_lyricsVerseColor":        // New preference only
                            //custom2_lyricsVerseColor        int         The color for the background for the verse in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_lyricsVerseColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom2_presoFontColor":        // New preference only
                        case "custom2_presoFont":
                            //custom2_presoFontColor          int         The color for the presentation text in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_presoFontColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom2_presoShadowColor":        // New preference only
                        case "custom2_presoShadow":
                            //custom2_presoShadowColor        int         The color for the presentation text shadow in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_presoShadowColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom2_presoInfoColor":        // New preference only
                        case "custom2_presoInfoFont":
                            //custom2_presoInfoColor          int         The color for the presentation info text in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_presoInfoColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom2_presoAlertColor":        // New preference only
                        case "custom2_presoAlertFont":
                            //custom2_presoAlertColor         int         The color for the presentation alert text in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_presoAlertColor",getIntegerValue(xppValue,StaticVariables.red));
                            break;
                        case "custom2_metronomeColor":        // New preference only
                        case "custom2_metronome":
                            //custom2_metronomeColor          int         The color for the metronome background in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_metronomeColor",getIntegerValue(xppValue,StaticVariables.darkishred));
                            break;
                        case "custom2_pageButtonsColor":        // New preference only
                        case "custom2_pagebuttons":
                            //custom2_pageButtonsColor        int         The color for the page buttons info text in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_pageButtonsColor",getIntegerValue(xppValue,StaticVariables.purplyblue));
                            break;
                        case "custom2_stickyTextColor":        // New preference only
                            //custom2_stickyTextColor         int         The color for the sticky note text info text in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_stickyTextColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "custom2_stickyBackgroundColor":        // New preference only
                            //custom2_stickyBackgroundColor   int         The color for the sticky note background info text in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_stickyBackgroundColor",getIntegerValue(xppValue,StaticVariables.lightyellow));
                            break;
                        case "custom2_extraInfoTextColor":        // New preference only
                        case "custom2_extrainfo":
                            //custom2_extraInfoTextColor      int         The color for the extra info text in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_extraInfoTextColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "custom2_extraInfoBgColor":        // New preference only
                        case "custom2_extrainfobg":
                            //custom2_extraInfoBgColor        int         The color for the extra info background in the custom2 theme
                            preferences.setMyPreferenceInt(c,"custom2_extraInfoBgColor",getIntegerValue(xppValue,StaticVariables.grey));
                            break;

                        case "customLogo":        // New and old preference
                            //customLogo                      String      The uri of the user logo for presentations
                            if (xppValue.contains("file:")) {
                                xppValue="";
                            }
                            preferences.setMyPreferenceString(c,"customLogo",getTextValue(xppValue,""));
                            break;

                        case "customLogoSize":        // New and old preference
                            //customLogoSize                  float       Size of the custom logo (% of screen)
                            preferences.setMyPreferenceFloat(c,"customLogoSize",getFloatValue(xppValue,0.5f));
                            break;

                        case "customPadAb":        // New and old preference
                            //customPadAb                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadAb",getTextValue(xppValue,""));
                            break;
                        case "customPadA":        // New and old preference
                            //customPadA                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadA",getTextValue(xppValue,""));
                            break;
                        case "customPadBb":        // New and old preference
                            //customPadBb                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadBb",getTextValue(xppValue,""));
                            break;
                        case "customPadB":        // New and old preference
                            //customPadB                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadB",getTextValue(xppValue,""));
                            break;
                        case "customPadC":        // New and old preference
                            //customPadC                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadC",getTextValue(xppValue,""));
                            break;
                        case "customPadDb":        // New and old preference
                            //customPadDb                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadDb",getTextValue(xppValue,""));
                            break;
                        case "customPadD":        // New and old preference
                            //customPadD                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadD",getTextValue(xppValue,""));
                            break;
                        case "customPadEb":        // New and old preference
                            //customPadEb                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadEb",getTextValue(xppValue,""));
                            break;
                        case "customPadE":        // New and old preference
                            //customPadE                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadE",getTextValue(xppValue,""));
                            break;
                        case "customPadF":        // New and old preference
                            //customPadF                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadF",getTextValue(xppValue,""));
                            break;
                        case "customPadGb":        // New and old preference
                            //customPadGb                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadGb",getTextValue(xppValue,""));
                            break;
                        case "customPadG":        // New and old preference
                            //customPadG                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadG",getTextValue(xppValue,""));
                            break;

                        case "customPadAbm":        // New and old preference
                            //customPadAbm                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadAbm",getTextValue(xppValue,""));
                            break;
                        case "customPadAm":        // New and old preference
                            //customPadAm                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadAm",getTextValue(xppValue,""));
                            break;
                        case "customPadBbm":        // New and old preference
                            //customPadBbm                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadBbm",getTextValue(xppValue,""));
                            break;
                        case "customPadBm":        // New and old preference
                            //customPadBm                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadBm",getTextValue(xppValue,""));
                            break;
                        case "customPadCm":        // New and old preference
                            //customPadCm                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadCm",getTextValue(xppValue,""));
                            break;
                        case "customPadDbm":        // New and old preference
                            //customPadDbm                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadDbm",getTextValue(xppValue,""));
                            break;
                        case "customPadDm":        // New and old preference
                            //customPadDm                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadDm",getTextValue(xppValue,""));
                            break;
                        case "customPadEbm":        // New and old preference
                            //customPadEbm                    String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadEbm",getTextValue(xppValue,""));
                            break;
                        case "customPadEm":        // New and old preference
                            //customPadEm                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadEm",getTextValue(xppValue,""));
                            break;
                        case "customPadFm":        // New and old preference
                            //customPadFm                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadFm",getTextValue(xppValue,""));
                            break;
                        case "customPadGbm":        // New and old preference
                            //customPadGbm                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadGbm",getTextValue(xppValue,""));
                            break;
                        case "customPadGm":        // New and old preference
                            //customPadGm                     String      Custom pad uri for the key specified
                            if (xppValue.startsWith("file:")) {
                                xppValue = "";
                            }
                            preferences.setMyPreferenceString(c,"customPadGm",getTextValue(xppValue,""));
                            break;

                        case "dark_lyricsBackgroundColor":        // New and old preference only
                            //dark_lyricsBackgroundColor   int         The color for the lyrics background in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_lyricsBackgroundColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "dark_lyricsBridgeColor":        // New and old preference only
                            //dark_lyricsBridgeColor       int         The color for the background for the bridge in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_lyricsBridgeColor",getIntegerValue(xppValue,StaticVariables.vdarkred));
                            break;
                        case "dark_lyricsCapoColor":        // New preference only
                            //dark_lyricsCapoColor         int         The color for the capo text in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_lyricsCapoColor",getIntegerValue(xppValue,StaticVariables.red));
                            break;
                        case "dark_lyricsChordsColor":        // New preference only
                            //dark_lyricsChordsColor       int         The color for the chords text in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_lyricsChordsColor",getIntegerValue(xppValue,StaticVariables.yellow));
                            break;
                        case "dark_lyricsChorusColor":        // New preference only
                            //dark_lyricsChorusColor       int         The color for the background for the chorus in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_lyricsChorusColor",getIntegerValue(xppValue,StaticVariables.vdarkblue));
                            break;
                        case "dark_lyricsCommentColor":        // New preference only
                            //dark_lyricsCommentColor      int         The color for the background for the comment in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_lyricsCommentColor",getIntegerValue(xppValue,StaticVariables.vdarkgreen));
                            break;
                        case "dark_lyricsCustomColor":        // New preference only
                            //dark_lyricsCustomColor       int         The color for the background for the custom section in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_lyricsCustomColor",getIntegerValue(xppValue,StaticVariables.vdarkyellow));
                            break;
                        case "dark_lyricsPreChorusColor":        // New and old preference only
                            //dark_lyricsPreChorusColor    int         The color for the background for the prechorus in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_lyricsPreChorusColor",getIntegerValue(xppValue,StaticVariables.darkishgreen));
                            break;
                        case "dark_lyricsTagColor":        // New and old preference only
                            //dark_lyricsTagColor          int         The color for the background for the tag in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_lyricsTagColor",getIntegerValue(xppValue,StaticVariables.darkpurple));
                            break;
                        case "dark_lyricsTextColor":        // New and old preference only
                            //dark_lyricsTextColor         int         The color for the lyrics text in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_lyricsTextColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "dark_lyricsVerseColor":        // New and old preference only
                            //dark_lyricsVerseColor        int         The color for the background for the verse in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_lyricsVerseColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "dark_presoFontColor":        // New and old preference only
                        case "dark_presoFont":
                            //dark_presoFontColor          int         The color for the presentation text in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_presoFontColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "dark_presoShadowColor":        // New preference only
                        case "dark_presoShadow":
                            //dark_presoShadowColor        int         The color for the presentation text shadow in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_presoShadowColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "dark_presoInfoColor":        // New preference only
                        case "dark_presoInfoFont":
                            //dark_presoInfoColor          int         The color for the presentation info text in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_presoInfoColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "dark_presoAlertColor":        // New preference only
                        case "dark_presoAlertFont":
                            //dark_presoAlertColor         int         The color for the presentation alert text in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_presoAlertColor",getIntegerValue(xppValue,StaticVariables.red));
                            break;
                        case "dark_metronomeColor":        // New preference only
                        case "dark_metronome":
                            //dark_metronomeColor          int         The color for the metronome background in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_metronomeColor",getIntegerValue(xppValue,StaticVariables.darkishred));
                            break;

                        case "dark_pageButtonsColor":        // New preference only
                        case "dark_pagebuttons":
                            //dark_pageButtonsColor        int         The color for the page buttons info text in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_pageButtonsColor",getIntegerValue(xppValue,StaticVariables.purplyblue));
                            break;
                        case "dark_stickyTextColor":        // New preference only
                            //dark_stickyTextColor         int         The color for the sticky note text info text in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_stickyTextColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "dark_stickyBackgroundColor":        // New preference only
                            //dark_stickyBackgroundColor   int         The color for the sticky note background info text in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_stickyBackgroundColor",getIntegerValue(xppValue,StaticVariables.lightyellow));
                            break;
                        case "dark_extraInfoTextColor":        // New preference only
                        case "dark_extrainfo":
                            //dark_extraInfoTextColor      int         The color for the extra info text in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_extraInfoTextColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "dark_extraInfoBgColor":        // New preference only
                        case "dark_extrainfobg":
                            //dark_extraInfoBgColor        int         The color for the extra info background in the dark theme
                            preferences.setMyPreferenceInt(c,"dark_extraInfoBgColor",getIntegerValue(xppValue,StaticVariables.grey));
                            break;

                        case "displayCapoChords":        // New preference
                        case "showCapoChords":           // Old preference
                            //displayCapoChords               boolean     Should capo chords be shown
                            preferences.setMyPreferenceBoolean(c,"displayCapoChords",getBooleanValue(xppValue,true));
                            break;

                        case "displayCapoAndNativeChords":        // New preference
                        case "showNativeAndCapoChords":           // Old preference
                            //displayCapoAndNativeChords      boolean     Should both chords be shown at once
                            preferences.setMyPreferenceBoolean(c,"displayCapoAndNativeChords",getBooleanValue(xppValue,false));
                            break;

                        case "displayChords":        // New preference
                        case "showChords":        // Old preference
                            //displayChords                   boolean     Decides if chords should be shown
                            preferences.setMyPreferenceBoolean(c,"displayChords",getBooleanValue(xppValue,true));
                            break;

                        case "displayLyrics":        // New preference
                        case "showLyrics":           // Old preference
                            //displayLyrics                   boolean     Decides if lyrics should be shown
                            preferences.setMyPreferenceBoolean(c,"displayLyrics",getBooleanValue(xppValue,true));
                            break;

                        case "displayNextInSet":        // New preference
                        case "showNextInSet":           // Old preference
                            if (xppValue.equals("off")) {
                                xppValue = "N";
                            }
                            //displayNextInSet                String      Should the next song in set be shown (N)o, (T)op inline, (B)ottom inline
                            preferences.setMyPreferenceString(c,"displayNextInSet",getTextValue(xppValue,"B"));
                            break;

                        case "drawingAutoDisplay":        // New preference
                        case "toggleAutoHighlight":       // Old preference
                            //drawingAutoDisplay              boolean     Should the highlighter drawings be shown on page load
                            preferences.setMyPreferenceBoolean(c,"drawingAutoDisplay",getBooleanValue(xppValue,true));
                            break;

                        case "drawingEraserSize":        // New and old preference
                            //drawingEraserSize               int         The default size of the eraser
                            preferences.setMyPreferenceInt(c,"drawingEraserSize",getIntegerValue(xppValue,20));
                            break;

                        case "drawingHighlighterColor":        // New preference
                        case "drawingHighlightColor":          // Old preference
                            //drawingHighlighterColor         int         The color of the highlighter
                            preferences.setMyPreferenceInt(c,"drawingHighlighterColor",getIntegerValue(xppValue,StaticVariables.highighteryellow));
                            break;

                        case "drawingHighlighterSize":        // New preference
                        case "drawingHighlightSize":          // Old preference
                            //drawingHighlighterSize          int         The default size of the highlighter
                            preferences.setMyPreferenceInt(c,"drawingHighlighterSize",getIntegerValue(xppValue,20));
                            break;

                        case "drawingPenColor":        // New and old preference
                            //drawingPenColor                 int         The colour of the pen
                            preferences.setMyPreferenceInt(c,"drawingPenColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;

                        case "drawingPenSize":        // New and old preference
                            //drawingPenSize                  int         The default size of the pen
                            preferences.setMyPreferenceInt(c,"drawingPenSize",getIntegerValue(xppValue,20));
                            break;

                        case "drawingTool":        // New and old preference
                            //drawingTool                     String      The current drawing tool
                            preferences.setMyPreferenceString(c,"drawingTool",getTextValue(xppValue,"highlighter"));
                            break;

                        case "editAsChordPro":        // New and old preference
                            //editAsChordPro                  boolean     Should the song edit window be ChordPro format
                            preferences.setMyPreferenceBoolean(c,"editAsChordPro",getBooleanValue(xppValue,false));
                            break;

                        case "exportOpenSongAppSet":        // New and old preference
                            //exportOpenSongAppSet            boolean     Should we export .osts file
                            preferences.setMyPreferenceBoolean(c,"exportOpenSongAppSet",getBooleanValue(xppValue,true));
                            break;

                        case "exportOpenSongApp":        // New and old preference
                            //exportOpenSongApp               boolean     Should we export .ost file
                            preferences.setMyPreferenceBoolean(c,"exportOpenSongApp",getBooleanValue(xppValue,true));
                            break;

                        case "exportDesktop":        // New and old preference
                            //exportDesktop                   boolean     Should we export desktop xml file
                            preferences.setMyPreferenceBoolean(c,"exportDesktop",getBooleanValue(xppValue,true));
                            break;

                        case "exportText":        // New and old preference
                            //exportText                      boolean     Should we export .txt file
                            preferences.setMyPreferenceBoolean(c,"exportText",getBooleanValue(xppValue,false));
                            break;

                        case "exportChordPro":        // New and old preference
                            //exportChordPro                  boolean     Should we export .chopro file
                            preferences.setMyPreferenceBoolean(c,"exportChordPro",getBooleanValue(xppValue,false));
                            break;

                        case "exportOnSong":        // New and old preference
                            //exportOnSong                    boolean     Should we export .onsong file
                            preferences.setMyPreferenceBoolean(c,"exportOnSong",getBooleanValue(xppValue,false));
                            break;

                        case "exportImage":        // New and old preference
                            //exportImage                     boolean     Should we export .png file
                            preferences.setMyPreferenceBoolean(c,"exportImage",getBooleanValue(xppValue,false));
                            break;

                        case "exportPDF":        // New and old preference
                            //exportPDF                       boolean     Should we export .pdf file
                            preferences.setMyPreferenceBoolean(c,"exportPDF",getBooleanValue(xppValue,false));
                            break;

                        case "fontSize":         // New preference
                        case "mFontSize":        // Old preference
                            //fontSize                        float       The font size
                            preferences.setMyPreferenceFloat(c,"fontSize",getFloatValue(xppValue,42.0f));
                            break;

                        case "fontSizeMax":        // New preference
                        case "mMaxFontSize":       // Old preference
                            //fontSizeMax                     float       The max font size
                            preferences.setMyPreferenceFloat(c,"fontSizeMax",getFloatValue(xppValue,50.0f));
                            break;

                        case "fontSizeMin":        // New preference
                        case "mMinFontSize":       // Old preference
                            //fontSizeMin                     float       The min font size
                            preferences.setMyPreferenceFloat(c,"fontSizeMin",getFloatValue(xppValue,8.0f));
                            break;

                        case "fontChord":        // New preference only
                            //fontChord                       String      The name of the font used for the chords.  From fonts.google.com
                            preferences.setMyPreferenceString(c,"fontChord",getTextValue(xppValue,"lato"));
                            break;

                        case "fontCustom":        // New preference only
                            //fontCustom                      String      The name of the font used for custom fonts.  From fonts.google.com
                            preferences.setMyPreferenceString(c,"fontCustom",getTextValue(xppValue,"lato"));
                            break;

                        case "fontLyric":        // New preference only
                            //fontLyric                       String      The name of the font used for the lyrics.  From fonts.google.com
                            preferences.setMyPreferenceString(c,"fontLyric",getTextValue(xppValue,"lato"));
                            break;

                        case "fontPreso":           // New preference only
                            //fontPreso                       String      The name of the font used for the preso.  From fonts.google.com
                            preferences.setMyPreferenceString(c,"fontPreso",getTextValue(xppValue,"lato"));
                            break;

                        case "fontPresoInfo":        // New preference only
                            // fontPresoInfo                   String      The name of the font used for the presoinfo.  From fonts.google.com
                            preferences.setMyPreferenceString(c,"fontPresoInfo",getTextValue(xppValue,"lato"));
                            break;

                        case "fontSizePreso":        // New preference
                        case "presoFontSize":        // Old preference
                            //fontSizePreso                   float       The non-scale presentation font size
                            preferences.setMyPreferenceFloat(c,"fontSizePreso",getFloatValue(xppValue,14.0f));
                            break;

                        case "fontSizePresoMax":        // New preference only
                        case "presoMaxFontSize":
                            //fontSizePresoMax                float       The maximum autoscaled font size
                            preferences.setMyPreferenceFloat(c,"fontSizePresoMax",getFloatValue(xppValue,40.0f));
                            break;

                        case "fontSticky":        // New preference only
                            //fontSticky                      String      The name of the font used for the sticky notes.  From fonts.google.com
                            preferences.setMyPreferenceString(c,"fontSticky",getTextValue(xppValue,"lato"));
                            break;

                        case "gestureScreenDoubleTap":        // New preference
                        case "gesture_doubletap":             // Old preference
                            //gestureScreenDoubleTap          int         The action for double tapping on the song screen (def 2 = edit song - based on menu position)
                            preferences.setMyPreferenceInt(c,"gestureScreenDoubleTap",getIntegerValue(xppValue,2));
                            break;

                        case "gestureScreenLongPress":        // New preference
                        case "gesture_longpress":             // Old preference
                            //gestureScreenLongPress          int         The action for long pressing on the song screen (def 3 = add song to set - based on menu position)
                            preferences.setMyPreferenceInt(c,"gestureScreenLongPress",getIntegerValue(xppValue,3));
                            break;

                        case "hideActionBar":        // New and old preference
                            //hideActionBar                   boolean     Should the action bar auto hide
                            preferences.setMyPreferenceBoolean(c,"hideActionBar",getBooleanValue(xppValue,false));
                            break;

                        case "hideLyricsBox":        // New and old preference
                            //hideLyricsBox                   boolean     Should we hide the box around the lyrics
                            preferences.setMyPreferenceBoolean(c,"hideLyricsBox",getBooleanValue(xppValue,false));
                            break;

                        case "language":        // New preference
                        case "locale":          // Old preference
                            //language                        String      The locale set in the menu
                            preferences.setMyPreferenceString(c,"language",getTextValue(xppValue,"en"));
                            break;

                        case "lastUsedVersion":        // New preference
                            //lastUsedVersion                 int         The app version number the last time the app ran
                            preferences.setMyPreferenceInt(c,"lastUsedVersion",getIntegerValue(xppValue,0));
                            break;

                        case "light_lyricsBackgroundColor":        // New preference only
                            //light_lyricsBackgroundColor   int         The color for the lyrics background in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_lyricsBackgroundColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "light_lyricsBridgeColor":        // New preference only
                            //light_lyricsBridgeColor       int         The color for the background for the bridge in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_lyricsBridgeColor",getIntegerValue(xppValue,StaticVariables.vlightcyan));
                            break;
                        case "light_lyricsCapoColor":        // New preference only
                            //light_lyricsCapoColor         int         The color for the capo text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_lyricsCapoColor",getIntegerValue(xppValue,StaticVariables.red));
                            break;
                        case "light_lyricsChordsColor":        // New preference only
                            //light_lyricsChordsColor       int         The color for the chords text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_lyricsChordsColor",getIntegerValue(xppValue,StaticVariables.darkblue));
                            break;
                        case "light_lyricsChorusColor":        // New preference only
                            //light_lyricsChorusColor       int         The color for the background for the chorus in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_lyricsChorusColor",getIntegerValue(xppValue,StaticVariables.vlightpurple));
                            break;
                        case "light_lyricsCommentColor":        // New preference only
                            //light_lyricsCommentColor      int         The color for the background for the comment in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_lyricsCommentColor",getIntegerValue(xppValue,StaticVariables.vlightblue));
                            break;
                        case "light_lyricsCustomColor":        // New preference only
                            //light_lyricsCustomColor       int         The color for the background for the custom section in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_lyricsCustomColor",getIntegerValue(xppValue,StaticVariables.lightishcyan));
                            break;
                        case "light_lyricsPreChorusColor":        // New preference only
                            //light_lyricsPreChorusColor    int         The color for the background for the prechorus in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_lyricsPreChorusColor",getIntegerValue(xppValue,StaticVariables.lightgreen));
                            break;
                        case "light_lyricsTagColor":        // New preference only
                            //light_lyricsTagColor          int         The color for the background for the tag in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_lyricsTagColor",getIntegerValue(xppValue,StaticVariables.vlightgreen));
                            break;
                        case "light_lyricsTextColor":        // New preference only
                            //light_lyricsTextColor         int         The color for the lyrics text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_lyricsTextColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "light_lyricsVerseColor":        // New preference only
                            //light_lyricsVerseColor        int         The color for the background for the verse in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_lyricsVerseColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "light_presoFontColor":        // New preference only
                        case "light_presoFont":
                            //light_presoFontColor          int         The color for the presentation text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_presoFontColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "light_presoShadowColor":        // New preference only
                        case "light_presoShadow":
                            //light_presoShadowColor        int         The color for the presentation text shadow in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_presoShadowColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "light_presoInfoColor":        // New preference only
                        case "light_presoInfoFont":
                            //light_presoInfoColor          int         The color for the presentation info text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_presoInfoColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "light_presoAlertColor":        // New preference only
                        case "light_presoAlertFont":
                            //light_presoAlertColor         int         The color for the presentation alert text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_presoAlertColor",getIntegerValue(xppValue,StaticVariables.red));
                            break;
                        case "light_metronomeColor":        // New preference only
                        case "light_metronome":
                            //light_metronomeColor          int         The color for the metronome background in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_metronomeColor",getIntegerValue(xppValue,StaticVariables.darkishred));
                            break;
                        case "light_pageButtonsColor":        // New preference only
                        case "light_pagebuttons":
                            //light_pageButtonsColor        int         The color for the page buttons info text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_pageButtonsColor",getIntegerValue(xppValue,StaticVariables.purplyblue));
                            break;
                        case "light_stickyTextColor":        // New preference only
                            //light_stickyTextColor         int         The color for the sticky note text info text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_stickyTextColor",getIntegerValue(xppValue,StaticVariables.black));
                            break;
                        case "light_stickyBackgroundColor":        // New preference only
                            //light_stickyBackgroundColor   int         The color for the sticky note background info text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_stickyBackgroundColor",getIntegerValue(xppValue,StaticVariables.lightyellow));
                            break;
                        case "light_extraInfoTextColor":        // New preference only
                        case "light_extrainfo":
                            //light_extraInfoTextColor      int         The color for the extra info text in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_extraInfoTextColor",getIntegerValue(xppValue,StaticVariables.white));
                            break;
                        case "light_extraInfoBgColor":        // New preference only
                        case "light_extrainfobg":
                            //light_extraInfoBgColor        int         The color for the extra info background in the custom1 theme
                            preferences.setMyPreferenceInt(c,"light_extraInfoBgColor",getIntegerValue(xppValue,StaticVariables.grey));
                            break;

                        case "lineSpacing":        // New preference
                        case "linespacing":        // Old preference
                            //lineSpacing                     float       The line spacing trim value to use
                            // Old value was an integer, so divide by 100 to convert to a float.
                            float f = getFloatValue(xppValue,0.1f);
                            if (f>1) {
                                f = f/100.0f;
                            }
                            preferences.setMyPreferenceFloat(c,"lineSpacing",f);
                            break;

                        case "menuSize":        // New preference only
                            //menuSize                        int         The width of the side menus (min 100 max 400)
                            int i = getIntegerValue(xppValue,250);
                            if (i<100) {
                                i = 200;
                            } else if (i>400) {
                                i=400;
                            }
                            preferences.setMyPreferenceInt(c,"menuSize",i);
                            break;

                        case "metronomeAutoStart":        // New preference
                        case "autostartmetronome":        // Old preference
                            //metronomeAutoStart              boolean     Should the metronome autostart with song (after manually starting first time)
                            preferences.setMyPreferenceBoolean(c,"metronomeAutoStart",getBooleanValue(xppValue,false));
                            break;

                        case "metronomeLength":             // New preference
                            //metronomeLength                 int         Number of bars the metronome stays on for (0=indefinitely) (def:0)
                            preferences.setMyPreferenceInt(c,"metronomeLength",getIntegerValue(xppValue,0));
                            break;

                        case "metronomePan":        // New preference
                        case "metronomepan":        // Old preference
                            //metronomePan                    String      The panning of the metronome sound L, C, R
                            switch (xppValue) {
                                case "both":
                                    xppValue = "C";
                                    break;
                                case "left":
                                    xppValue = "L";
                                    break;
                                case "right":
                                    xppValue = "R";
                                    break;
                            }
                            preferences.setMyPreferenceString(c,"metronomePan",getTextValue(xppValue,"C"));
                            break;

                        case "metronomeVol":        // New preference
                        case "metronomevol":        // Old preference
                            //metronomeVol                    float       The volume of the metronome
                            preferences.setMyPreferenceFloat(c,"metronomeVol",getFloatValue(xppValue,0.5f));
                            break;

                        case "metronomeShowVisual":        // New preference
                        case "visualmetronome":            // Old preference
                            //metronomeShowVisual             boolean     Should the metronome be visual (flash action bar)
                            preferences.setMyPreferenceBoolean(c,"metronomeShowVisual",getBooleanValue(xppValue,false));
                            break;

                        case "midiSendAuto":        // New preference
                        case "midiAuto":        // Old preference
                            //midiSendAuto                    boolean     Should the midi info in the song be sent on song load automatically
                            preferences.setMyPreferenceBoolean(c,"midiSendAuto",getBooleanValue(xppValue,false));
                            break;

                        case "multiLineVerseKeepCompact":        // New preference
                        case "multilineCompact":        // Old preference
                            //multiLineVerseKeepCompact       boolean     Should multiline verses be kept compact
                            preferences.setMyPreferenceBoolean(c,"multiLineVerseKeepCompact",getBooleanValue(xppValue,false));
                            break;

                        case "padAutoStart":        // New preference
                        case "autostartpad":        // Old preference
                            //padAutoStart                    boolean     Should the pad autostart with song (after manually starting first time)
                            preferences.setMyPreferenceBoolean(c,"padAutoStart",getBooleanValue(xppValue,false));
                            break;

                        case "padCrossFadeTime":    // New preference
                            // padCrossFadeTime                int         The time in ms used to fade out a pad.  Set in the PopUpCrossFade fragment (def:8000)
                            preferences.setMyPreferenceInt(c,"padCrossFadeTime",getIntegerValue(xppValue,8000));
                            break;

                        case "padLargeFontInfoBar":        // New preference
                        case "timerFontSizePad":        // Old preference
                            //padLargeFontInfoBar             boolean     The text size of the floating pad info bar (true is 20.0f false is 14.0f)
                            if (xppValue.startsWith("20")) {
                                xppValue = "true";
                            } else if (xppValue.startsWith("14")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"padLargeFontInfoBar",getBooleanValue(xppValue,true));
                            break;

                        case "padPan":        // New preference
                        case "padpan":        // Old preference
                            //padPan                          String      The panning of the pad (L, C or R)
                            switch (xppValue) {
                                case "both":
                                    xppValue = "C";
                                    break;
                                case "left":
                                    xppValue = "L";
                                    break;
                                case "right":
                                    xppValue = "R";
                                    break;
                            }
                            preferences.setMyPreferenceString(c,"padPan",getTextValue(xppValue,"C"));
                            break;

                        case "padVol":        // New preference
                        case "padvol":        // Old preference
                            //padVol                          float       The volume of the pad
                            preferences.setMyPreferenceFloat(c,"padVol",getFloatValue(xppValue,1.0f));
                            break;

                        case "pageButtonAlpha":        // New preference
                            //pageButtonAlpha                 float       The opacity of the page/quicklaunnch button
                            preferences.setMyPreferenceFloat(c,"pageButtonAlpha",getFloatValue(xppValue,0.5f));
                            break;

                        case "pageButtonGroupCustom":        // New preference
                        case "page_custom_grouped":          // Old preference
                            //pageButtonGroupCustom           boolean     Group the custom page buttons
                            preferences.setMyPreferenceBoolean(c,"pageButtonGroupCustom",getBooleanValue(xppValue,false));
                            break;

                        case "pageButtonGroupExtra":        // New preference
                        case "page_extra_grouped":          // Old preference
                            //pageButtonGroupExtra            boolean     Group the extra info page buttons
                            preferences.setMyPreferenceBoolean(c,"pageButtonGroupExtra",getBooleanValue(xppValue,false));
                            break;

                        case "pageButtonGroupMain":        // New preference
                        case "grouppagebuttons":
                            //pageButtonGroupMain             boolean     Group the main page buttons
                            preferences.setMyPreferenceBoolean(c,"pageButtonGroupMain",getBooleanValue(xppValue,false));
                            break;

                        case "pageButtonSize":        // New preference
                        case "fabSize":               // Old preference
                            //pageButtonSize                  int         The size of the page/quicklaunch buttons (SIZE_NORMAL or SIZE_MINI)
                            preferences.setMyPreferenceInt(c,"pageButtonSize",getIntegerValue(xppValue, FloatingActionButton.SIZE_NORMAL));
                            break;

                        case "pageButtonShowAutoscroll":        // New preference
                        case "page_autoscroll_visible":         // Old preference
                            //pageButtonShowAutoscroll        boolean     Should the autroscroll page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowAutoscroll",getBooleanValue(xppValue,true));
                            break;

                        case "pageButtonShowChords":        // New preference
                        case "page_chord_visible":        // Old preference
                            //pageButtonShowChords            boolean     Should the chord symbol page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowChords",getBooleanValue(xppValue,false));
                            break;

                        case "pageButtonShowCustom1":        // New preference
                        case "page_custom1_visible":         // Old preference
                            //pageButtonShowCustom1           boolean     Should the custom1 page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowCustom1",getBooleanValue(xppValue,true));
                            break;

                        case "pageButtonShowCustom2":        // New preference
                        case "page_custom2_visible":         // Old preference
                            //pageButtonShowCustom2           boolean     Should the custom2 page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowCustom2",getBooleanValue(xppValue,true));
                            break;

                        case "pageButtonShowCustom3":        // New preference
                        case "page_custom3_visible":         // Old preference
                            //pageButtonShowCustom3           boolean     Should the custom3 page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowCustom3",getBooleanValue(xppValue,true));
                            break;

                        case "pageButtonShowCustom4":        // New preference
                        case "page_custom4_visible":         // Old preference
                            //pageButtonShowCustom4           boolean     Should the custom4 page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowCustom4",getBooleanValue(xppValue,true));
                            break;

                        case "pageButtonShowHighlighter":        // New preference
                        case "page_highlight_visible":           // Old preference
                            //pageButtonShowHighlighter       boolean     Should the highlighter page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowHighlighter",getBooleanValue(xppValue,false));
                            break;

                        case "pageButtonShowLinks":        // New preference
                        case "page_links_visible":         // Old preference
                            //pageButtonShowLinks             boolean     Should the links page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowLinks",getBooleanValue(xppValue,false));
                            break;

                        case "pageButtonShowMetronome":        // New preference
                        case "page_metronome_visible":         // Old preference
                            //pageButtonShowMetronome         boolean     Should the metronome page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowMetronome",getBooleanValue(xppValue,false));
                            break;

                        case "pageButtonShowNotation":        // New preference
                        case "page_notation_visible":         // Old preference
                            //pageButtonShowNotation          boolean     Should the notation page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowNotation",getBooleanValue(xppValue,false));
                            break;

                        case "pageButtonShowPad":        // New preference
                        case "page_pad_visible":        // Old preference
                            //pageButtonShowPad               boolean     Should the pad page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowPad",getBooleanValue(xppValue,true));
                            break;

                        case "pageButtonShowPageSelect":        // New preference
                        case "page_pages_visible":              // Old preference
                            //pageButtonShowPageSelect        boolean     Should the page select page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowPageSelect",getBooleanValue(xppValue,false));
                            break;

                        case "pageButtonShowScroll":        // New preference
                            //pageButtonShowScroll            boolean     Should the scroll page buttons be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowScroll",getBooleanValue(xppValue,true));
                            break;

                        case "pageButtonShowSet":        // New preference
                        case "page_set_visible":         // Old preference
                            //pageButtonShowSet               boolean     Should the set page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowSet",getBooleanValue(xppValue,true));
                            break;

                        case "pageButtonShowSetMove":        // New preference
                            //pageButtonShowSetMove           boolean     Should the set forward/back page buttons be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowSetMove",getBooleanValue(xppValue,true));
                            break;

                        case "pageButtonShowSticky":        // New preference
                        case "page_sticky_visible":         // Old preference
                            //pageButtonShowSticky            boolean     Should the sticky notes page button be shown
                            preferences.setMyPreferenceBoolean(c,"pageButtonShowSticky",getBooleanValue(xppValue,false));
                            break;

                        case "pageButtonCustom1Action":        // New preference
                        case "quickLaunchButton_1":
                            //pageButtonCustom1Action         String      The action for custom button 1
                            preferences.setMyPreferenceString(c,"pageButtonCustom1Action",getTextValue(xppValue,"transpose"));
                            break;

                        case "pageButtonCustom2Action":        // New preference
                        case "quickLaunchButton_2":
                            //pageButtonCustom2Action         String      The action for custom button 2
                            preferences.setMyPreferenceString(c,"pageButtonCustom2Action",getTextValue(xppValue,""));
                            break;

                        case "pageButtonCustom3Action":        // New preference
                        case "quickLaunchButton_3":
                            //pageButtonCustom3Action         String      The action for custom button 3
                            preferences.setMyPreferenceString(c,"pageButtonCustom3Action",getTextValue(xppValue,""));
                            break;

                        case "pageButtonCustom4Action":        // New preference
                        case "quickLaunchButton_4":
                            //pageButtonCustom4Action         String      The action for custom button 4
                            preferences.setMyPreferenceString(c,"pageButtonCustom4Action",getTextValue(xppValue,""));
                            break;

                        case "pedal1Code":        // New preference
                        case "pedal1":        // Old preference
                            //pedal1Code                      int         The keyboard int code assigned to pedal 1 (default is 21 - left arrow)
                            preferences.setMyPreferenceInt(c,"pedal1Code",getIntegerValue(xppValue,21));
                            break;

                        case "pedal1LongPressAction":        // New preference
                        case "pedal1longaction":        // Old preference
                            //pedal1LongPressAction           String      The action called when pedal 1 is long pressed (default is songmenu)
                            preferences.setMyPreferenceString(c,"pedal1LongPressAction",getTextValue(xppValue,"songmenu"));
                            break;

                        case "pedal1ShortPressAction":        // New preference
                        case "pedal1shortaction":        // Old preference
                            //pedal1ShortPressAction          String      The action called when pedal 1 is short pressed (default is prev)
                            preferences.setMyPreferenceString(c,"pedal1ShortPressAction",getTextValue(xppValue,"prev"));
                            break;

                        case "pedal2Code":        // New preference
                        case "pedal2":        // Old preference
                            //pedal2Code                      int         The keyboard int code assigned to pedal 2 (default is 22 - right arrow)
                            preferences.setMyPreferenceInt(c,"pedal2Code",getIntegerValue(xppValue,22));
                            break;

                        case "pedal2LongPressAction":        // New preference
                        case "pedal2longaction":        // Old preference
                            //pedal2LongPressAction           String      The action called when pedal 2 is long pressed (default is set)
                            preferences.setMyPreferenceString(c,"pedal2LongPressAction",getTextValue(xppValue,"set"));
                            break;

                        case "pedal2ShortPressAction":        // New preference
                        case "pedal2shortaction":        // Old preference
                            //pedal2ShortPressAction          String      The action called when pedal 2 is short pressed (default is next)
                            preferences.setMyPreferenceString(c,"pedal2ShortPressAction",getTextValue(xppValue,"next"));
                            break;

                        case "pedal3Code":        // New preference
                        case "pedal3":        // Old preference
                            //pedal3Code                      int         The keyboard int code assigned to pedal 3 (default is 19 - up arrow)
                            preferences.setMyPreferenceInt(c,"pedal3Code",getIntegerValue(xppValue,19));
                            break;

                        case "pedal3LongPressAction":        // New preference
                        case "pedal3longaction":        // Old preference
                            //pedal3LongPressAction           String      The action called when pedal 3 is long pressed (default is songmenu)
                            preferences.setMyPreferenceString(c,"pedal3LongPressAction",getTextValue(xppValue,"songmenu"));
                            break;

                        case "pedal3ShortPressAction":        // New preference
                        case "pedal3shortaction":        // Old preference
                            //pedal3ShortPressAction          String      The action called when pedal 3 is short pressed (default is prev)
                            preferences.setMyPreferenceString(c,"pedal3ShortPressAction",getTextValue(xppValue,"prev"));
                            break;

                        case "pedal4Code":        // New preference
                        case "pedal4":        // Old preference
                            //pedal4Code                      int         The keyboard int code assigned to pedal 4 (default is 20 - down arrow)
                            preferences.setMyPreferenceInt(c,"pedal4Code",getIntegerValue(xppValue,20));
                            break;

                        case "pedal4LongPressAction":        // New preference
                        case "pedal4longaction":        // Old preference
                            //pedal4LongPressAction           String      The action called when pedal 4 is long pressed (default is set)
                            preferences.setMyPreferenceString(c,"pedal4LongPressAction",getTextValue(xppValue,"set"));
                            break;

                        case "pedal4ShortPressAction":        // New preference
                        case "pedal4shortaction":        // Old preference
                            //pedal4ShortPressAction          String      The action called when pedal 4 is short pressed (default is next)
                            preferences.setMyPreferenceString(c,"pedal4ShortPressAction",getTextValue(xppValue,"next"));
                            break;

                        case "pedal5Code":        // New preference
                        case "pedal5":        // Old preference
                            //pedal5Code                      int         The keyboard int code assigned to pedal 5 (default is 92 - page up)
                            preferences.setMyPreferenceInt(c,"pedal5Code",getIntegerValue(xppValue,92));
                            break;

                        case "pedal5LongPressAction":        // New preference
                        case "pedal5longaction":        // Old preference
                            //pedal5LongPressAction           String      The action called when pedal 5 is long pressed (default is songmenu)
                            preferences.setMyPreferenceString(c,"pedal5LongPressAction",getTextValue(xppValue,"songmenu"));
                            break;

                        case "pedal5ShortPressAction":        // New preference
                        case "pedal5shortaction":        // Old preference
                            //pedal5ShortPressAction          String      The action called when pedal 5 is short pressed (default is prev)
                            preferences.setMyPreferenceString(c,"pedal5ShortPressAction",getTextValue(xppValue,"prev"));
                            break;

                        case "pedal6Code":        // New preference
                        case "pedal6":        // Old preference
                            //pedal6Code                      int         The keyboard int code assigned to pedal 6 (default is 93 - page down)
                            preferences.setMyPreferenceInt(c,"pedal6Code",getIntegerValue(xppValue,6));
                            break;

                        case "pedal6LongPressAction":        // New preference
                        case "pedal6longaction":        // Old preference
                            //pedal6LongPressAction           String      The action called when pedal 6 is long pressed (default is set)
                            preferences.setMyPreferenceString(c,"pedal6LongPressAction",getTextValue(xppValue,"set"));
                            break;

                        case "pedal6ShortPressAction":        // New preference
                        case "pedal6shortaction":        // Old preference
                            //pedal6ShortPressAction          String      The action called when pedal 6 is short pressed (default is next)
                            preferences.setMyPreferenceString(c,"pedal6ShortPressAction",getTextValue(xppValue,"next"));
                            break;

                        case "pedalScrollBeforeMove":        // New preference
                        case "toggleScrollBeforeSwipe":        // Old preference
                            //pedalScrollBeforeMove           boolean     Should the prev/next pedal buttons try to scroll first (makes 2 pedals into 4)
                            if (xppValue.equals("Y")) {
                                xppValue = "true";
                            } else if (xppValue.equals("N")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"pedalScrollBeforeMove",getBooleanValue(xppValue,true));
                            break;

                        case "popupAlpha":        // New preference
                        case "popupAlpha_All":        // Old preference
                            //popupAlpha                      float       The opacity of the popup windows
                            preferences.setMyPreferenceFloat(c,"popupAlpha",getFloatValue(xppValue,0.8f));
                            break;

                        case "popupDim":        // New preference
                        case "popupDim_All":        // Old preference
                            //popupDim                        float       The darkness of the main window when the popup is open
                            preferences.setMyPreferenceFloat(c,"popupDim",getFloatValue(xppValue,0.8f));
                            break;

                        case "popupPosition":        // New preference
                        case "popupPosition_All":        // Old preference
                            //popupPosition                   String      The position of the popups (tl, tc, tr, l, c, r, bl, bc, br)
                            preferences.setMyPreferenceString(c,"popupPosition",getTextValue(xppValue,"c"));
                            break;

                        case "popupScale":        // New preference
                        case "popupScale_All":        // Old preference
                            //popupScale                      float       The size of the popup relative to the page size
                            preferences.setMyPreferenceFloat(c,"popupScale",getFloatValue(xppValue,0.7f));
                            break;

                        case "prefKey_Ab":        // New preference
                        case "prefChord_Aflat_Gsharp":        // Old preference
                            //prefKey_Ab                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
                            if (xppValue.equals("b")) {
                                xppValue = "true";
                            } else if (xppValue.equals("#")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"prefKey_Ab",getBooleanValue(xppValue,true));
                            break;

                        case "prefKey_Bb":        // New preference
                        case "prefChord_Bflat_Asharp":        // Old preference
                            //prefKey_Bb                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
                            if (xppValue.equals("b")) {
                                xppValue = "true";
                            } else if (xppValue.equals("#")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"prefKey_Bb",getBooleanValue(xppValue,true));
                            break;

                        case "prefKey_Db":        // New preference
                        case "prefChord_Dflat_Csharp":        // Old preference
                            //prefKey_Db                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
                            if (xppValue.equals("b")) {
                                xppValue = "true";
                            } else if (xppValue.equals("#")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"prefKey_Db",getBooleanValue(xppValue,false));
                            break;

                        case "prefKey_Eb":        // New preference
                        case "prefChord_Eflat_Dsharp":        // Old preference
                            //prefKey_Eb                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
                            if (xppValue.equals("b")) {
                                xppValue = "true";
                            } else if (xppValue.equals("#")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"prefKey_Eb",getBooleanValue(xppValue,true));
                            break;

                        case "prefKey_Gb":        // New preference
                        case "prefChord_Gflat_Fsharp":        // Old preference
                            //prefKey_Gb                      boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
                            if (xppValue.equals("b")) {
                                xppValue = "true";
                            } else if (xppValue.equals("#")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"prefKey_Gb",getBooleanValue(xppValue,false));
                            break;

                        case "prefKey_Abm":        // New preference
                        case "prefChord_Aflatm_Gsharpm":        // Old preference
                            //prefKey_Abm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
                            if (xppValue.equals("b")) {
                                xppValue = "true";
                            } else if (xppValue.equals("#")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"prefKey_Abm",getBooleanValue(xppValue,false));
                            break;

                        case "prefKey_Bbm":        // New preference
                        case "prefChord_Bflatm_Asharpm":        // Old preference
                            //prefKey_Bbm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
                            if (xppValue.equals("b")) {
                                xppValue = "true";
                            } else if (xppValue.equals("#")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"prefKey_Bbm",getBooleanValue(xppValue,true));
                            break;

                        case "prefKey_Dbm":        // New preference
                        case "prefChord_Dflatm_Csharpm":        // Old preference
                            //prefKey_Dbm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
                            if (xppValue.equals("b")) {
                                xppValue = "true";
                            } else if (xppValue.equals("#")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"prefKey_Dbm",getBooleanValue(xppValue,false));
                            break;

                        case "prefKey_Ebm":        // New preference
                        case "prefChord_Eflatm_Dsharpm":        // Old preference
                            //prefKey_Ebm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
                            if (xppValue.equals("b")) {
                                xppValue = "true";
                            } else if (xppValue.equals("#")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"prefKey_Ebm",getBooleanValue(xppValue,true));
                            break;

                        case "prefKey_Gbm":        // New preference
                        case "prefChord_Gflatm_Fsharpm":        // Old preference
                            //prefKey_Gbm                     boolean     Prefer the key using flats if true (otherwise prefer the alternative key using sharps)
                            if (xppValue.equals("b")) {
                                xppValue = "true";
                            } else if (xppValue.equals("#")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"prefKey_Gbm",getBooleanValue(xppValue,false));
                            break;

                        case "presoAlertText":        // New preference
                        case "myAlert":        // Old preference
                            //presoAlertText                  String      The text for the alert in Presentation mode
                            preferences.setMyPreferenceString(c,"presoAlertText",getTextValue(xppValue,""));
                            break;

                        case "presoAlertTextSize":        // New preference
                        case "presoAlertSize":        // Old preference
                            //presoAlertTextSize              float       The size of the alert text in Presentation mode
                            preferences.setMyPreferenceFloat(c,"presoAlertTextSize",getFloatValue(xppValue,12.0f));
                            break;

                        case "presoAutoScale":        // New preference
                            //presoAutoScale                  boolean     Should the presenter window use autoscale for text
                            preferences.setMyPreferenceBoolean(c,"presoAutoScale",getBooleanValue(xppValue,true));
                            break;

                        case "presoAuthorTextSize":        // New preference
                        case "presoAuthorSize":        // Old preference
                            //presoAuthorTextSize             float       The size of the alert text in Presentation mode (def:12.0f)
                            preferences.setMyPreferenceFloat(c,"presoAuthorTextSize",getFloatValue(xppValue,12.0f));
                            break;

                        case "presoAutoUpdateProjector":        // New preference
                        case "autoProject":        // Old preference
                            //presoAutoUpdateProjector        boolean     Should the projector be updated automatically in PresenterMode when something changes
                            preferences.setMyPreferenceBoolean(c,"presoAutoUpdateProjector",getBooleanValue(xppValue,true));
                            break;

                        case "presoBackgroundAlpha":        // New preference
                        case "presoAlpha":        // Old preference
                            //presoBackgroundAlpha            float       The alpha value for the presentation background
                            preferences.setMyPreferenceFloat(c,"presoBackgroundAlpha",getFloatValue(xppValue,1.0f));
                            break;

                        case "presoCopyrightTextSize":        // New preference
                        case "presoCopyrightSize":        // Old preference
                            //presoCopyrightTextSize          float       The size of the alert text in Presentation mode (def:12.0f)
                            preferences.setMyPreferenceFloat(c,"presoCopyrightTextSize",getFloatValue(xppValue,12.0f));
                            break;

                        case "presoInfoAlign":         // New preference
                            //presoInfoAlign                  int         The align gravity of the info in presentation mode
                            preferences.setMyPreferenceInt(c,"presoInfoAlign",getIntegerValue(xppValue, Gravity.END));
                            break;

                        case "presoLyricsAlign":        // New preference
                            //presoLyricsAlign                int         The align gravity of the lyrics in presentation mode
                            preferences.setMyPreferenceInt(c,"presoLyricsAlign",getIntegerValue(xppValue,Gravity.CENTER_HORIZONTAL));
                            break;

                        case "presoLyricsVAlign":        // New preference
                            //presoLyricsVAlign                int         The vertical align gravity of the lyrics in presentation mode
                            preferences.setMyPreferenceInt(c,"presoLyricsVAlign",getIntegerValue(xppValue,Gravity.TOP));
                            break;

                        case "presoShowChords":        // New preference
                        case "presenterChords":        // Old preference
                            //presoShowChords                 boolean     Should chords be shown in the presentation window
                            if (xppValue.equals("Y")) {
                                xppValue = "true";
                            } else if (xppValue.equals("N")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"presoShowChords",getBooleanValue(xppValue,false));
                            break;

                        case "presoTitleTextSize":        // New preference
                        case "presoTitleSize":        // Old preference
                            //presoTitleTextSize              float       The size of the alert text in Presentation mode (def:14.0f)
                            preferences.setMyPreferenceFloat(c,"presoTitleTextSize",getFloatValue(xppValue,14.0f));
                            break;

                        case "presoTransitionTime":        // New preference
                            //presoTransitionTime             int         The time for transitions between items in presenter mode (ms)
                            preferences.setMyPreferenceInt(c,"presoTransitionTime",getIntegerValue(xppValue,800));
                            break;

                        case "presoXMargin":        // New preference
                        case "xmargin_presentation":        // Old preference
                            //presoXMargin                    int         The margin for the X axis on the presentation window
                            preferences.setMyPreferenceInt(c,"presoXMargin",getIntegerValue(xppValue,20));
                            break;

                        case "presoYMargin":        // New preference
                        case "ymargin_presentation":        // Old preference
                            //presoYMargin                    int         The margin for the Y axis on the presentation window
                            preferences.setMyPreferenceInt(c,"presoYMargin",getIntegerValue(xppValue,10));
                            break;

                        case "profileName":        // New preference
                        case "profile":        // Old preference
                            //profileName                     String      The last loaded or saved profile name
                            preferences.setMyPreferenceString(c,"profileName",getTextValue(xppValue,""));
                            break;

                        case "randomSongFolderChoice":        // New preference
                        case "randomFolders":        // Old preference
                            //randomSongFolderChoice          String      A list of folders to include in the random song generation ($$_folder_$$)
                            preferences.setMyPreferenceString(c,"randomSongFolderChoice",getTextValue(xppValue,""));
                            break;

                        case "runswithoutbackup":        // New preference
                            //runswithoutbackup               int         The number of times the app has opened without backup (prompt the user after 10)
                            preferences.setMyPreferenceInt(c,"runswithoutbackup",getIntegerValue(xppValue,0));
                            break;

                        case "scaleChords":        // New preference
                        case "chordfontscalesize":        // Old preference
                            //scaleChords                     float       The scale factor for chords relative to the lyrics
                            preferences.setMyPreferenceFloat(c,"scaleChords",getFloatValue(xppValue,0.8f));
                            break;

                        case "scaleComments":        //  New preference
                        case "commentfontscalesize":        // Old preference
                            //scaleComments                   float       The scale factor for comments relative to the lyrics
                            preferences.setMyPreferenceFloat(c,"scaleComments",getFloatValue(xppValue,0.8f));
                            break;

                        case "scaleHeadings":        // New preference
                        case "headingfontscalesize":        // Old preference
                            //scaleHeadings                   float       The scale factor for headings relative to the lyrics
                            preferences.setMyPreferenceFloat(c,"scaleHeadings",getFloatValue(xppValue,0.6f));
                            break;

                        case "scrollDistance":        // New preference
                            //scrollDistance                  float       The percentage of the screen that is scrolled using the scroll buttons/pedals
                            preferences.setMyPreferenceFloat(c,"scrollDistance",getFloatValue(xppValue,0.7f));
                            break;

                        case "scrollSpeed":        // New preference
                            //scrollSpeed                     int         How quick should the scroll animation be
                            preferences.setMyPreferenceInt(c,"scrollSpeed",getIntegerValue(xppValue,1500));
                            break;

                        case "searchAka":        // New preference
                            //searchAka                       boolean     Should the aka be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchAka",getBooleanValue(xppValue,true));
                            break;

                        case "searchAuthor":        // New preference
                            //searchAuthor                    boolean     Should the author be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchAuthor",getBooleanValue(xppValue,true));
                            break;

                        case "searchCCLI":        // New preference
                            //searchCCLI                      boolean     Should the ccli be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchCCLI",getBooleanValue(xppValue,true));
                            break;

                        case "searchCopyright":        // New preference
                            //searchCopyright                 boolean     Should the copyright be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchCopyright",getBooleanValue(xppValue,true));
                            break;

                        case "searchFilename":        // New preference
                            //searchFilename                  boolean     Should the filename be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchFilename",getBooleanValue(xppValue,true));
                            break;

                        case "searchFolder":        // New preference
                            //searchFolder                    boolean     Should the folder be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchFolder",getBooleanValue(xppValue,true));
                            break;

                        case "searchHymn":        // New preference
                            //searchHymn                      boolean     Should the hymn number be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchHymn",getBooleanValue(xppValue,true));
                            break;

                        case "searchKey":        // New preference
                            //searchKey                       boolean     Should the key be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchKey",getBooleanValue(xppValue,true));
                            break;

                        case "searchLyrics":        // New preference
                            //searchLyrics                    boolean     Should the lyrics be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchLyrics",getBooleanValue(xppValue,true));
                            break;

                        case "searchTheme":        // New preference
                            //searchTheme                     boolean     Should the theme be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchTheme",getBooleanValue(xppValue,true));
                            break;

                        case "searchTitle":        // New preference
                            //searchTitle                     boolean     Should the title be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchTitle",getBooleanValue(xppValue,true));
                            break;

                        case "searchUser1":        // New preference
                            //searchUser1                     boolean     Should the user1 be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchUser1",getBooleanValue(xppValue,true));
                            break;

                        case "searchUser2":        // New preference
                            //searchUser2                     boolean     Should the user2 be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchUser2",getBooleanValue(xppValue,true));
                            break;

                        case "searchUser3":        // New preference
                            //searchUser3                     boolean     Should the user3 be included in the search
                            preferences.setMyPreferenceBoolean(c,"searchUser3",getBooleanValue(xppValue,true));
                            break;

                        case "setCurrent":        // New preference
                        case "mySet":        // Old preference
                            //setCurrent                      String      The current set (each item enclosed in $**_folder/song_**$) - gets parsed on loading app
                            preferences.setMyPreferenceString(c,"setCurrent",getTextValue(xppValue,""));
                            break;

                        case "setCurrentBeforeEdits":        // New preference
                            //setCurrentBeforeEdits           String      The current set before edits.  Used as a comparison to decide save action
                            preferences.setMyPreferenceString(c,"setCurrentBeforeEdits",getTextValue(xppValue,""));
                            break;

                        case "setCurrentLastName":        // New preference
                        case "lastSetName":        // Old preference
                            //setCurrentLastName              String      The last name used when saving or loading a set
                            preferences.setMyPreferenceString(c,"setCurrentLastName",getTextValue(xppValue,""));
                            break;

                        case "songAuthorSize":        // New preference
                        case "ab_authorSize":
                            //songAuthorSize                  float       The size of the song author text in the action bar
                            preferences.setMyPreferenceFloat(c,"songAuthorSize",getFloatValue(xppValue,11.0f));
                            break;

                        case "songAutoScale":        // New preference
                        case "toggleYScale":        // Old preference
                            //songAutoScale                   String      Choice of autoscale mode (Y)es, (W)idth only or (N)one
                            preferences.setMyPreferenceString(c,"songAutoScale",getTextValue(xppValue,"W"));
                            break;

                        case "songAutoScaleColumnMaximise":     // New preference only
                            preferences.setMyPreferenceBoolean(c,"songAutoScaleColumnMaximise",getBooleanValue(xppValue,true));
                            break;

                        case "songAutoScaleOverrideFull":        // New preference
                        case "override_fullscale":        // Old preference
                            //songAutoScaleOverrideFull       boolean     If the app can override full autoscale if the font is too small
                            preferences.setMyPreferenceBoolean(c,"songAutoScaleOverrideFull",getBooleanValue(xppValue,true));
                            break;

                        case "songAutoScaleOverrideWidth":        // New preference
                        case "override_widthscale":        // Old preference
                            //songAutoScaleOverrideWidth      boolean     If the app can override width autoscale if the font is too small
                            preferences.setMyPreferenceBoolean(c,"songAutoScaleOverrideWidth",getBooleanValue(xppValue,false));
                            break;

                            // Don't include the songfilename
                       /* case "songfilename":        // New preference
                            //songfilename                    String      The name of the current song file
                            preferences.setMyPreferenceString(c,"songfilename",getTextValue(xppValue,""));
                            break;*/

                        case "songLoadSuccess":        // New preference
                            //songLoadSuccess                 boolean     Indicates if the song loaded correctly (won't load a song next time if it crashed)
                            preferences.setMyPreferenceBoolean(c,"songLoadSuccess",getBooleanValue(xppValue,false));
                            break;

                        case "songMenuAlphaIndexShow":        // New preference
                        case "showAlphabeticalIndexInSongMenu":        // Old preference
                            //songMenuAlphaIndexShow          boolean     Should we show the alphabetical index in the song menu
                            preferences.setMyPreferenceBoolean(c,"songMenuAlphaIndexShow",getBooleanValue(xppValue,true));
                            break;

                        case "songMenuAlphaIndexSize":      // New preference
                        case "alphabeticalSize":            // Old preference
                            //songMenuAlphaIndexSize        float       The text size for the alphabetical index in the song menu
                            preferences.setMyPreferenceFloat(c,"songMenuAlphaIndexSize", getFloatValue(xppValue, 14.0f));
                            break;

                        case "songMenuSetTicksShow":        // New preference
                        case "showSetTickBoxInSongMenu":        // Old preference
                            //songMenuSetTicksShow            boolean     Should we show the ticks identifying song is in the set in the song menu
                            preferences.setMyPreferenceBoolean(c,"songMenuSetTicksShow",getBooleanValue(xppValue,true));
                            break;

                        case "songTitleSize":        // New preference
                        case "ab_titleSize":
                            //songTitleSize                   float       The size of the song title text in the action bar
                            preferences.setMyPreferenceFloat(c,"songTitleSize",getFloatValue(xppValue,13.0f));
                            break;

                        case "soundMeterRange":        // New preference
                        case "maxvolrange":        // Old preference
                            //soundMeterRange                 int         The volume range of the sound meter
                            preferences.setMyPreferenceInt(c,"soundMeterRange",getIntegerValue(xppValue,400));
                            break;

                        case "stageModeScale":        // New preference
                        case "stagemodeScale":        // Old preference
                            //stageModeScale                  float       The max height of each stage mode section (to allow next section to peek at bottom)
                            preferences.setMyPreferenceFloat(c,"stageModeScale",getFloatValue(xppValue,0.7f));
                            break;

                        case "stickyAutoDisplay":        // New preference
                        case "toggleAutoSticky":         // Old preference
                            //stickyAutoDisplay               String      Where should sticky notes be shown (N)one, (T)op inline, (B)ottom inline, (F)loating window
                            preferences.setMyPreferenceString(c,"stickyAutoDisplay",getTextValue(xppValue,"F"));
                            break;

                        case "stickyWidth":        // New preference
                            //stickyWidth                     int         The width of popup sticky notes
                            preferences.setMyPreferenceInt(c,"stickyWidth",getIntegerValue(xppValue,400));
                            break;

                        case "stickyAlpha":        // New preference
                        case "stickyOpacity":        // Old preference
                            //stickyAlpha                     float       The alpha of popup sticky notes
                            preferences.setMyPreferenceFloat(c,"stickyAlpha",getFloatValue(xppValue,0.8f));
                            break;

                        case "stickyLargeFont":        // New preference
                        case "stickyTextSize":         // Old preference
                            //stickyLargeFont                 boolean     The text size for the popup sticky notes (true=20.0f, false=14.0f)
                            if (xppValue.equals("Y") || xppValue.startsWith("20")) {
                                xppValue = "true";
                            } else if (xppValue.equals("N") || xppValue.startsWith("14")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"stickyLargeFont",getBooleanValue(xppValue,false));
                            break;

                        case "swipeForMenus":        // New preference
                            //swipeForMenus                   boolean     Can we swipe the menus in or out
                            if (xppValue.equals("Y")) {
                                xppValue = "true";
                            } else if (xppValue.equals("N")) {
                                xppValue = "false";
                            }
                            preferences.setMyPreferenceBoolean(c,"swipeForMenus",getBooleanValue(xppValue,true));
                            break;

                        case "swipeForSongs":        // New preference
                            //swipeForSongs                   boolean     Can we swipe to move between song items
                            preferences.setMyPreferenceBoolean(c,"swipeForSongs",getBooleanValue(xppValue,true));
                            break;

                        case "swipeMinimumDistance":        // New preference
                        case "SWIPE_MIN_DISTANCE":        // Old preference
                            //swipeMinimumDistance            int         The minimum distance for a swipe to be registered (dp)
                            preferences.setMyPreferenceInt(c,"swipeMinimumDistance",getIntegerValue(xppValue,250));
                            break;

                        case "swipeMinimumVelocity":        // New preference
                        case "SWIPE_THRESHOLD_VELOCITY":        // Old preference
                            //swipeMinimumVelocity            int         The minimum speed for a swipe to be registered (dp/s)
                            preferences.setMyPreferenceInt(c,"swipeMinimumVelocity",getIntegerValue(xppValue,600));
                            break;

                        case "swipeMaxDistanceYError":        // New preference
                        case "SWIPE_MAX_OFF_PATH":        // Old preference
                            //swipeMaxDistanceYError          int         The maximum Y movement in a swipe allowed for it to be recognised
                            preferences.setMyPreferenceInt(c,"swipeMaxDistanceYError",getIntegerValue(xppValue,200));
                            break;

                        case "timeToDisplayHighlighter":        // New preference
                        case "highlightShowSecs":        // Old preference
                            //timeToDisplayHighlighter        int         The time to show highlighter notes for before hiding (def=0 means keep on) ms
                            preferences.setMyPreferenceInt(c,"timeToDisplayHighlighter",getIntegerValue(xppValue,0));
                            break;

                        case "timeToDisplaySticky":        // New preference
                        case "stickyNotesShowSecs":        // Old preference
                            //timeToDisplaySticky             int         The time to show sticky notes for before hiding (def=0 means keep on) s
                            preferences.setMyPreferenceInt(c,"timeToDisplaySticky",getIntegerValue(xppValue,0));
                            break;

                        case "trimSections":        // New preference
                            //trimSections                    boolean     Should whitespace be removed from song sections
                            preferences.setMyPreferenceBoolean(c,"trimSections",getBooleanValue(xppValue,true));
                            break;

                        case "trimLines":        // New preference
                            //trimLines                       boolean     Should the lines be trimmed (using the lineSpacing) value
                            preferences.setMyPreferenceBoolean(c,"trimLines",getBooleanValue(xppValue,true));
                            break;

                            // Don't include the old storage!!!!!
                        /*case "uriTree":        // New preference
                            //uriTree                         String      A string representation of the user root location (may be the OpenSong folder or its parent)
                            preferences.setMyPreferenceString(c,"uriTree",getTextValue(xppValue,""));
                            break;

                        case "uriTreeHome":        // New preference
                            //uriTreeHome                     String      A string representation of the user home location (The OpenSong folder)
                            preferences.setMyPreferenceString(c,"uriTreeHome",getTextValue(xppValue,""));
                            break;*/

                        case "usePresentationOrder":        // New preference
                            //usePresentationOrder            boolean     Should the song be parsed into the specified presentation order
                            preferences.setMyPreferenceBoolean(c,"usePresentationOrder",getBooleanValue(xppValue,false));
                            break;

                        case "whichSetCategory":        // New preference
                            //whichSetCategory                String      Which set category are we browsing (category___setname)
                            preferences.setMyPreferenceString(c,"whichSetCategory",getTextValue(xppValue,c.getString(R.string.mainfoldername)));
                            break;

                        case "whichMode":        // New preference
                            //whichMode                       String      Which app mode - Stage, Performance, Presentation
                            preferences.setMyPreferenceString(c,"whichMode",getTextValue(xppValue,"Performance"));
                            break;

                            // Don't include the song folder
                        /*case "whichSongFolder":        // New preference
                            //whichSongFolder                 String      The song folder we are currently in
                            preferences.setMyPreferenceString(c,"whichSongFolder",getTextValue(xppValue,c.getString(R.string.mainfoldername)));
                            break;*/

                    }
                }

                try {
                    eventType = xpp.next();
                } catch (Exception e) {
                    //Ooops!
                    Log.d("d", "error in file, or not xml");
                    result = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    Intent openProfile(Context c, Preferences preferences, StorageAccess storageAccess) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String [] mimeTypes = {"application/*", "application/xml", "text/xml"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            Uri uri = storageAccess.getUriForItem(c, preferences, "Profiles", "", "");
            DocumentFile file = DocumentFile.fromTreeUri(c, uri);
            if (file!=null) {
                intent.putExtra(EXTRA_INITIAL_URI, file.getUri());
                file = file.findFile("Profiles");
                if (file!=null) {
                    intent.putExtra(EXTRA_INITIAL_URI, file.getUri());
                }
            }
        }
        return intent;
    }

    Intent saveProfile(Context c, Preferences preferences, StorageAccess storageAccess) {
        Intent intent = new Intent();
        intent.setType("application/*");
        String [] mimeTypes = {"application/*", "application/xml", "text/xml"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.setAction(Intent.ACTION_CREATE_DOCUMENT);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            Uri uri = storageAccess.getUriForItem(c, preferences, "Profiles", "", "");
            DocumentFile file = DocumentFile.fromTreeUri(c, uri);
            if (file!=null) {
                intent.putExtra(EXTRA_INITIAL_URI, file.getUri());
                file = file.findFile("Profiles");
                if (file!=null) {
                    intent.putExtra(EXTRA_INITIAL_URI, file.getUri());
                }
            }
        }
        intent.putExtra(Intent.EXTRA_TITLE, preferences.getMyPreferenceString(c,"profileName","Profile"));
    return intent;
    }

    private String getXppValue(XmlPullParser xpp) {
        // Try to extract the xpp value attribute if it exists
        String xppValue = "";
        try {
            if (xpp.getAttributeCount()>0) {
                xppValue = xpp.getAttributeValue(0);
            }
        } catch (Exception e) {
            xppValue = "";
        }
        return xppValue;
    }

    private String getXppNextText(XmlPullParser xpp) {
        // Try to extract the xpp value attribute if it exists
        String xppNextText;
        try {
            xppNextText = xpp.nextText();
            if (xppNextText==null) {
                xppNextText = "";
            }
        } catch (Exception e) {
            xppNextText = "";
        }
        return xppNextText;
    }

    private float getFloatValue(String s, float def) {
        if (s!=null && !s.equals("")) {
            try {
                return Float.parseFloat(s.replace("f",""));
            } catch (Exception e) {
                return def;
            }
        } else {
            return def;
        }
    }

    private boolean getBooleanValue(String s, boolean def) {
        boolean trueorfalse = def;
        if (s!=null && !s.equals("")) {
            trueorfalse = s.equals("true");
        }
        return trueorfalse;
    }

    private int getIntegerValue(String s, int def) {
        int integer;
        if (s!=null && !s.equals("")) {
            try {
                integer = Integer.parseInt(s);
            } catch (Exception e) {
                integer = def;
            }
        } else {
            integer = def;
        }
        return integer;
    }

    private String getTextValue(String s, String def) {
        String text = def;
        if (s!=null && !s.equals("")) {
            text = s;
        }
        return text;
    }

}
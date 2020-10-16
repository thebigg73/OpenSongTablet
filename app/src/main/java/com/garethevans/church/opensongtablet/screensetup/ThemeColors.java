package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;

import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

import java.util.HashMap;
import java.util.Map;

public class ThemeColors {

    // Set the colours from preferences
    private int lyricsTextColor;
    private int lyricsBackgroundColor;
    private int lyricsCapoColor;
    private int lyricsVerseColor;
    private int lyricsChorusColor;
    private int lyricsBridgeColor;
    private int lyricsCommentColor;
    private int lyricsPreChorusColor;
    private int lyricsTagColor;
    private int lyricsChordsColor;
    private int lyricsCustomColor;
    private int presoFontColor;
    private int metronomeColor;
    private int pageButtonsColor;
    private int stickyTextColor;
    private int stickyBackgroundColor;
    private int extraInfoBgColor;
    private int extraInfoTextColor;

    public Map<String,Integer> getDefaultColors(Context c, Preferences preferences) {
        String theme = preferences.getMyPreferenceString(c,"appTheme","dark");
        switch (theme) {
            case "dark":
            default:
                setThemeDark(c, preferences);
                break;
            case "light":
                setThemeLight(c, preferences);
                break;
            case "custom1":
                setThemeCustom1(c, preferences);
                break;
            case "custom2":
                setThemeCustom2(c, preferences);
                break;
        }
        return returnColors();
    }

    private void setThemeDark(Context c, Preferences preferences) {
        metronomeColor          = preferences.getMyPreferenceInt(c, "dark_metronomeColor",          StaticVariables.darkishred);
        pageButtonsColor        = preferences.getMyPreferenceInt(c, "dark_pageButtonsColor",        StaticVariables.purplyblue);
        stickyTextColor         = preferences.getMyPreferenceInt(c, "dark_stickyTextColor",         StaticVariables.black);
        stickyBackgroundColor   = preferences.getMyPreferenceInt(c, "dark_stickyBackgroundColor",   StaticVariables.lightyellow);
        extraInfoBgColor        = preferences.getMyPreferenceInt(c, "dark_extraInfoBgColor",        StaticVariables.grey);
        extraInfoTextColor      = preferences.getMyPreferenceInt(c, "dark_extraInfoTextColor",      StaticVariables.white);
        lyricsTextColor         = preferences.getMyPreferenceInt(c, "dark_lyricsTextColor",         StaticVariables.white);
        lyricsCapoColor         = preferences.getMyPreferenceInt(c, "dark_lyricsCapoColor",         StaticVariables.red);
        lyricsBackgroundColor   = preferences.getMyPreferenceInt(c, "dark_lyricsBackgroundColor",   StaticVariables.black);
        lyricsVerseColor        = preferences.getMyPreferenceInt(c, "dark_lyricsVerseColor",        StaticVariables.black);
        lyricsChorusColor       = preferences.getMyPreferenceInt(c, "dark_lyricsChorusColor",       StaticVariables.vdarkblue);
        lyricsBridgeColor       = preferences.getMyPreferenceInt(c, "dark_lyricsBridgeColor",       StaticVariables.vdarkred);
        lyricsCommentColor      = preferences.getMyPreferenceInt(c, "dark_lyricsCommentColor",      StaticVariables.vdarkgreen);
        lyricsPreChorusColor    = preferences.getMyPreferenceInt(c, "dark_lyricsPreChorusColor",    StaticVariables.darkishgreen);
        lyricsTagColor          = preferences.getMyPreferenceInt(c, "dark_lyricsTagColor",          StaticVariables.darkpurple);
        lyricsChordsColor       = preferences.getMyPreferenceInt(c, "dark_lyricsChordsColor",       StaticVariables.yellow);
        lyricsCustomColor       = preferences.getMyPreferenceInt(c, "dark_lyricsCustomColor",       StaticVariables.vdarkyellow);
        presoFontColor          = preferences.getMyPreferenceInt(c, "dark_presoFontColor",          StaticVariables.white);
    }
    private void setThemeLight(Context c, Preferences preferences) {
        metronomeColor          = preferences.getMyPreferenceInt(c,"light_metronomeColor",         StaticVariables.darkishred);
        pageButtonsColor        = preferences.getMyPreferenceInt(c,"light_pageButtonsColor",       StaticVariables.purplyblue);
        stickyTextColor         = preferences.getMyPreferenceInt(c,"light_stickyTextColor",        StaticVariables.black);
        stickyBackgroundColor   = preferences.getMyPreferenceInt(c,"light_stickyBackgroundColor",  StaticVariables.lightyellow);
        extraInfoBgColor        = preferences.getMyPreferenceInt(c,"light_extraInfoBgColor",       StaticVariables.grey);
        extraInfoTextColor      = preferences.getMyPreferenceInt(c,"light_extraInfoTextColor",     StaticVariables.white);
        lyricsTextColor         = preferences.getMyPreferenceInt(c,"light_lyricsTextColor",        StaticVariables.black);
        lyricsCapoColor         = preferences.getMyPreferenceInt(c,"light_lyricsCapoColor",        StaticVariables.red);
        lyricsBackgroundColor   = preferences.getMyPreferenceInt(c,"light_lyricsBackgroundColor",  StaticVariables.white);
        lyricsVerseColor        = preferences.getMyPreferenceInt(c,"light_lyricsVerseColor",       StaticVariables.white);
        lyricsChorusColor       = preferences.getMyPreferenceInt(c,"light_lyricsChorusColor",      StaticVariables.vlightpurple);
        lyricsBridgeColor       = preferences.getMyPreferenceInt(c,"light_lyricsBridgeColor",      StaticVariables.vlightcyan);
        lyricsCommentColor      = preferences.getMyPreferenceInt(c,"light_lyricsCommentColor",     StaticVariables.vlightblue);
        lyricsPreChorusColor    = preferences.getMyPreferenceInt(c,"light_lyricsPreChorusColor",   StaticVariables.lightgreen);
        lyricsTagColor          = preferences.getMyPreferenceInt(c,"light_lyricsTagColor",         StaticVariables.vlightgreen);
        lyricsChordsColor       = preferences.getMyPreferenceInt(c,"light_lyricsChordsColor",      StaticVariables.darkblue);
        lyricsCustomColor       = preferences.getMyPreferenceInt(c,"light_lyricsCustomColor",      StaticVariables.lightishcyan);
        presoFontColor          = preferences.getMyPreferenceInt(c,"light_presoFontColor",         StaticVariables.white);
    }
    private void setThemeCustom1(Context c, Preferences preferences) {
        metronomeColor          = preferences.getMyPreferenceInt(c,"custom1_metronomeColor",       StaticVariables.darkishred);
        pageButtonsColor        = preferences.getMyPreferenceInt(c,"custom1_pageButtonsColor",     StaticVariables.purplyblue);
        stickyTextColor         = preferences.getMyPreferenceInt(c,"custom1_stickyTextColor",      StaticVariables.black);
        stickyBackgroundColor   = preferences.getMyPreferenceInt(c,"custom1_stickyBackgroundColor",StaticVariables.lightyellow);
        extraInfoBgColor        = preferences.getMyPreferenceInt(c,"custom1_extraInfoBgColor",     StaticVariables.grey);
        extraInfoTextColor      = preferences.getMyPreferenceInt(c,"custom1_extraInfoTextColor",   StaticVariables.white);
        lyricsTextColor         = preferences.getMyPreferenceInt(c,"custom1_lyricsTextColor",      StaticVariables.white);
        lyricsCapoColor         = preferences.getMyPreferenceInt(c,"custom1_lyricsCapoColor",      StaticVariables.red);
        lyricsBackgroundColor   = preferences.getMyPreferenceInt(c,"custom1_lyricsBackgroundColor",StaticVariables.black);
        lyricsVerseColor        = preferences.getMyPreferenceInt(c,"custom1_lyricsVerseColor",     StaticVariables.black);
        lyricsChorusColor       = preferences.getMyPreferenceInt(c,"custom1_lyricsChorusColor",    StaticVariables.black);
        lyricsBridgeColor       = preferences.getMyPreferenceInt(c,"custom1_lyricsBridgeColor",    StaticVariables.black);
        lyricsCommentColor      = preferences.getMyPreferenceInt(c,"custom1_lyricsCommentColor",   StaticVariables.black);
        lyricsPreChorusColor    = preferences.getMyPreferenceInt(c,"custom1_lyricsPreChorusColor", StaticVariables.black);
        lyricsTagColor          = preferences.getMyPreferenceInt(c,"custom1_lyricsTagColor",       StaticVariables.black);
        lyricsChordsColor       = preferences.getMyPreferenceInt(c,"custom1_lyricsChordsColor",    StaticVariables.yellow);
        lyricsCustomColor       = preferences.getMyPreferenceInt(c,"custom1_lyricsCustomColor",    StaticVariables.black);
        presoFontColor          = preferences.getMyPreferenceInt(c,"custom1_presoFontColor",       StaticVariables.white);
    }
    private void setThemeCustom2(Context c, Preferences preferences) {
        metronomeColor          = preferences.getMyPreferenceInt(c,"custom2_metronomeColor",       StaticVariables.darkishred);
        pageButtonsColor        = preferences.getMyPreferenceInt(c,"custom2_pageButtonsColor",     StaticVariables.purplyblue);
        stickyTextColor         = preferences.getMyPreferenceInt(c,"custom2_stickyTextColor",      StaticVariables.black);
        stickyBackgroundColor   = preferences.getMyPreferenceInt(c,"custom2_stickyBackgroundColor",StaticVariables.lightyellow);
        extraInfoBgColor        = preferences.getMyPreferenceInt(c,"custom2_extraInfoBgColor",     StaticVariables.grey);
        extraInfoTextColor      = preferences.getMyPreferenceInt(c,"custom2_extraInfoTextColor",   StaticVariables.white);
        lyricsTextColor         = preferences.getMyPreferenceInt(c,"custom2_lyricsTextColor",      StaticVariables.black);
        lyricsCapoColor         = preferences.getMyPreferenceInt(c,"custom2_lyricsCapoColor",      StaticVariables.red);
        lyricsBackgroundColor   = preferences.getMyPreferenceInt(c,"custom2_lyricsBackgroundColor",StaticVariables.white);
        lyricsVerseColor        = preferences.getMyPreferenceInt(c,"custom2_lyricsVerseColor",     StaticVariables.white);
        lyricsChorusColor       = preferences.getMyPreferenceInt(c,"custom2_lyricsChorusColor",    StaticVariables.white);
        lyricsBridgeColor       = preferences.getMyPreferenceInt(c,"custom2_lyricsBridgeColor",    StaticVariables.white);
        lyricsCommentColor      = preferences.getMyPreferenceInt(c,"custom2_lyricsCommentColor",   StaticVariables.white);
        lyricsPreChorusColor    = preferences.getMyPreferenceInt(c,"custom2_lyricsPreChorusColor", StaticVariables.white);
        lyricsTagColor          = preferences.getMyPreferenceInt(c,"custom2_lyricsTagColor",       StaticVariables.white);
        lyricsChordsColor       = preferences.getMyPreferenceInt(c,"custom2_lyricsChordsColor",    StaticVariables.darkblue);
        lyricsCustomColor       = preferences.getMyPreferenceInt(c,"custom2_lyricsCustomColor",    StaticVariables.white);
        presoFontColor          = preferences.getMyPreferenceInt(c,"custom2_presoFontColor",       StaticVariables.white);
    }

    private Map<String,Integer> returnColors() {
        Map<String,Integer> colorMap = new HashMap<>();
        colorMap.put("lyricsTextColor",lyricsTextColor);
        colorMap.put("lyricsVerseColor",lyricsVerseColor);
        colorMap.put("lyricsChorusColor",lyricsChorusColor);
        colorMap.put("lyricsBridgeColor",lyricsBridgeColor);
        colorMap.put("lyricsCommentColor",lyricsCommentColor);
        colorMap.put("lyricsPreChorusColor",lyricsPreChorusColor);
        colorMap.put("lyricsTagColor",lyricsTagColor);
        colorMap.put("lyricsChordsColor",lyricsChordsColor);
        colorMap.put("lyricsCustomColor",lyricsCustomColor);
        colorMap.put("lyricsCapoColor",lyricsCapoColor);
        colorMap.put("lyricsBackgroundColor",lyricsBackgroundColor);
        colorMap.put("presoFontColor",presoFontColor);
        colorMap.put("metronomeColor",metronomeColor);
        colorMap.put("pageButtonsColor",pageButtonsColor);
        colorMap.put("stickyTextColor",stickyTextColor);
        colorMap.put("stickyBackgroundColor",stickyBackgroundColor);
        colorMap.put("extraInfoBgColor",extraInfoBgColor);
        colorMap.put("extraInfoTextColor",extraInfoTextColor);
        return colorMap;
    }
}
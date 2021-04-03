package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.Preferences;

public class ThemeColors {

    // This object holds the user theme colours
    private String themeName;

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
    private int presoInfoFontColor;
    private int presoAlertColor;
    private int presoShadowColor;
    private int metronomeColor;
    private int pageButtonsColor;
    private int stickyTextColor;
    private int stickyBackgroundColor;
    private int extraInfoBgColor;
    private int extraInfoTextColor;

    // Set the values with updates
    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }
    public void setLyricsTextColor(int i) {
        this.lyricsTextColor = i;
    }
    public void setLyricsBackgroundColor(int i) {
        this.lyricsBackgroundColor = i;
    }
    public void setLyricsCapoColor(int i) {
        this.lyricsCapoColor = i;
    }
    public void setLyricsVerseColor(int i) {
        this.lyricsVerseColor = i;
    }
    public void setLyricsChorusColor(int i) {
        this.lyricsChorusColor = i;
    }
    public void setLyricsBridgeColor(int i) {
        this.lyricsBridgeColor = i;
    }
    public void setLyricsCommentColor(int i) {
        this.lyricsCommentColor = i;
    }
    public void setLyricsPreChorusColor(int i) {
        this.lyricsPreChorusColor = i;
    }
    public void setLyricsTagColor(int i) {
        this.lyricsTagColor = i;
    }
    public void setLyricsChordsColor(int i) {
        this.lyricsChordsColor = i;
    }
    public void setLyricsCustomColor(int i) {
        this.lyricsCustomColor = i;
    }
    public void setPresoFontColor(int i) {
        this.presoFontColor = i;
    }
    public void setPresoInfoFontColor(int i) {
        this.presoInfoFontColor = i;
    }
    public void setPresoAlertColor(int i) {
        this.presoAlertColor = i;
    }
    public  void setPresoShadowColor(int i) {
        this.presoShadowColor = i;
    }
    public void setPageButtonsColor(int i) {
        this.pageButtonsColor = i;
    }
    public void setMetronomeColor(int i) {
        this.metronomeColor = i;
    }
    public void setStickyTextColor(int i) {
        this.stickyTextColor = i;
    }
    public void setStickyBackgroundColor(int i) {
        this.stickyBackgroundColor = i;
    }
    public void setExtraInfoBgColor(int i) {
        this.extraInfoBgColor = i;
    }
    public void setExtraInfoTextColor(int i) {
        this.extraInfoTextColor = i;
    }
    
    // Get the values
    public String getThemeName() {
        return themeName;
    }
    public int getLyricsTextColor() {
        return lyricsTextColor;
    }
    public int getLyricsBackgroundColor() {
        return lyricsBackgroundColor;
    }
    public int getLyricsCapoColor() {
        return lyricsCapoColor;
    }
    public int getLyricsVerseColor() {
        return lyricsVerseColor;
    }
    public int getLyricsChorusColor() {
        return lyricsChorusColor;
    }
    public int getLyricsBridgeColor() {
        return lyricsBridgeColor;
    }
    public int getLyricsCommentColor() {
        return lyricsCommentColor;
    }
    public int getLyricsPreChorusColor() {
        return lyricsPreChorusColor;
    }
    public int getLyricsTagColor() {
        return lyricsTagColor;
    }
    public int getLyricsChordsColor() {
        return lyricsChordsColor;
    }
    public int getLyricsCustomColor() {
        return lyricsCustomColor;
    }
    public int getPresoFontColor() {
        return presoFontColor;
    }
    public int getPresoInfoFontColor() {
        return presoInfoFontColor;
    }
    public int getPresoAlertColor() {
        return presoAlertColor;
    }
    public int getPresoShadowColor() {
        return presoShadowColor;
    }
    public int getPageButtonsColor() {
        return pageButtonsColor;
    }
    public int getMetronomeColor() {
        return metronomeColor;
    }
    public int getStickyBackgroundColor() {
        return stickyBackgroundColor;
    }
    public int getStickyTextColor() {
        return stickyTextColor;
    }
    public int getExtraInfoBgColor() {
        return extraInfoBgColor;
    }
    public int getExtraInfoTextColor() {
        return extraInfoTextColor;
    }

    public void getDefaultColors(Context c, Preferences preferences) {
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
    }

    private void setThemeDark(Context c, Preferences preferences) {
        setMetronomeColor(preferences.getMyPreferenceInt(c, "dark_metronomeColor",          darkishred));
        setPageButtonsColor(preferences.getMyPreferenceInt(c, "dark_pageButtonsColor",        secondary));
        setStickyTextColor(preferences.getMyPreferenceInt(c, "dark_stickyTextColor",         black));
        setStickyBackgroundColor(preferences.getMyPreferenceInt(c, "dark_stickyBackgroundColor",   lightyellow));
        setExtraInfoBgColor(preferences.getMyPreferenceInt(c, "dark_extraInfoBgColor",        grey));
        setExtraInfoTextColor(preferences.getMyPreferenceInt(c, "dark_extraInfoTextColor",      white));
        setLyricsTextColor(preferences.getMyPreferenceInt(c, "dark_lyricsTextColor",         white));
        setLyricsCapoColor(preferences.getMyPreferenceInt(c, "dark_lyricsCapoColor",         red));
        setLyricsBackgroundColor(preferences.getMyPreferenceInt(c, "dark_lyricsBackgroundColor",   black));
        setLyricsVerseColor(preferences.getMyPreferenceInt(c, "dark_lyricsVerseColor",        black));
        setLyricsChorusColor(preferences.getMyPreferenceInt(c, "dark_lyricsChorusColor",       vdarkblue));
        setLyricsBridgeColor(preferences.getMyPreferenceInt(c, "dark_lyricsBridgeColor",       vdarkred));
        setLyricsCommentColor(preferences.getMyPreferenceInt(c, "dark_lyricsCommentColor",      vdarkgreen));
        setLyricsPreChorusColor(preferences.getMyPreferenceInt(c, "dark_lyricsPreChorusColor",    darkishgreen));
        setLyricsTagColor(preferences.getMyPreferenceInt(c, "dark_lyricsTagColor",          darkpurple));
        setLyricsChordsColor(preferences.getMyPreferenceInt(c, "dark_lyricsChordsColor",       yellow));
        setLyricsCustomColor(preferences.getMyPreferenceInt(c, "dark_lyricsCustomColor",       vdarkyellow));
        setPresoFontColor(preferences.getMyPreferenceInt(c, "dark_presoFontColor",          white));
        setPresoInfoFontColor(preferences.getMyPreferenceInt(c, "dark_presoInfoFontColor", white));
        setPresoAlertColor(preferences.getMyPreferenceInt(c, "dark_presoAlertColor", red));
        setPresoShadowColor(preferences.getMyPreferenceInt(c, "dark_presoShadowColor", grey));
    }
    private void setThemeLight(Context c, Preferences preferences) {
        setMetronomeColor(preferences.getMyPreferenceInt(c,"light_metronomeColor",         darkishred));
        setPageButtonsColor(preferences.getMyPreferenceInt(c,"light_pageButtonsColor",       secondary));
        setStickyTextColor(preferences.getMyPreferenceInt(c,"light_stickyTextColor",        black));
        setStickyBackgroundColor(preferences.getMyPreferenceInt(c,"light_stickyBackgroundColor",  lightyellow));
        setExtraInfoBgColor(preferences.getMyPreferenceInt(c,"light_extraInfoBgColor",       grey));
        setExtraInfoTextColor(preferences.getMyPreferenceInt(c,"light_extraInfoTextColor",     white));
        setLyricsTextColor(preferences.getMyPreferenceInt(c,"light_lyricsTextColor",        black));
        setLyricsCapoColor(preferences.getMyPreferenceInt(c,"light_lyricsCapoColor",        red));
        setLyricsBackgroundColor(preferences.getMyPreferenceInt(c,"light_lyricsBackgroundColor",  white));
        setLyricsVerseColor(preferences.getMyPreferenceInt(c,"light_lyricsVerseColor",       white));
        setLyricsChorusColor(preferences.getMyPreferenceInt(c,"light_lyricsChorusColor",      vlightpurple));
        setLyricsBridgeColor(preferences.getMyPreferenceInt(c,"light_lyricsBridgeColor",      vlightcyan));
        setLyricsCommentColor(preferences.getMyPreferenceInt(c,"light_lyricsCommentColor",     vlightblue));
        setLyricsPreChorusColor(preferences.getMyPreferenceInt(c,"light_lyricsPreChorusColor",   lightgreen));
        setLyricsTagColor(preferences.getMyPreferenceInt(c,"light_lyricsTagColor",         vlightgreen));
        setLyricsChordsColor(preferences.getMyPreferenceInt(c,"light_lyricsChordsColor",      darkblue));
        setLyricsCustomColor(preferences.getMyPreferenceInt(c,"light_lyricsCustomColor",      lightishcyan));
        setPresoFontColor(preferences.getMyPreferenceInt(c,"light_presoFontColor",         white));
        setPresoInfoFontColor(preferences.getMyPreferenceInt(c, "light_presoInfoFontColor", white));
        setPresoAlertColor(preferences.getMyPreferenceInt(c, "light_presoAlertColor", red));
        setPresoShadowColor(preferences.getMyPreferenceInt(c, "light_presoShadowColor", grey));
    }
    private void setThemeCustom1(Context c, Preferences preferences) {
        setMetronomeColor(preferences.getMyPreferenceInt(c,"custom1_metronomeColor",       darkishred));
        setPageButtonsColor(preferences.getMyPreferenceInt(c,"custom1_pageButtonsColor",     secondary));
        setStickyTextColor(preferences.getMyPreferenceInt(c,"custom1_stickyTextColor",      black));
        setStickyBackgroundColor(preferences.getMyPreferenceInt(c,"custom1_stickyBackgroundColor",lightyellow));
        setExtraInfoBgColor(preferences.getMyPreferenceInt(c,"custom1_extraInfoBgColor",     grey));
        setExtraInfoTextColor(preferences.getMyPreferenceInt(c,"custom1_extraInfoTextColor",   white));
        setLyricsTextColor(preferences.getMyPreferenceInt(c,"custom1_lyricsTextColor",      white));
        setLyricsCapoColor(preferences.getMyPreferenceInt(c,"custom1_lyricsCapoColor",      red));
        setLyricsBackgroundColor(preferences.getMyPreferenceInt(c,"custom1_lyricsBackgroundColor",black));
        setLyricsVerseColor(preferences.getMyPreferenceInt(c,"custom1_lyricsVerseColor",     black));
        setLyricsChorusColor(preferences.getMyPreferenceInt(c,"custom1_lyricsChorusColor",    black));
        setLyricsBridgeColor(preferences.getMyPreferenceInt(c,"custom1_lyricsBridgeColor",    black));
        setLyricsCommentColor(preferences.getMyPreferenceInt(c,"custom1_lyricsCommentColor",   black));
        setLyricsPreChorusColor(preferences.getMyPreferenceInt(c,"custom1_lyricsPreChorusColor", black));
        setLyricsTagColor(preferences.getMyPreferenceInt(c,"custom1_lyricsTagColor",       black));
        setLyricsChordsColor(preferences.getMyPreferenceInt(c,"custom1_lyricsChordsColor",    yellow));
        setLyricsCustomColor(preferences.getMyPreferenceInt(c,"custom1_lyricsCustomColor",    black));
        setPresoFontColor(preferences.getMyPreferenceInt(c,"custom1_presoFontColor",       white));
        setPresoInfoFontColor(preferences.getMyPreferenceInt(c, "custom1_presoInfoFontColor", white));
        setPresoAlertColor(preferences.getMyPreferenceInt(c, "custom1_presoAlertColor", red));
        setPresoShadowColor(preferences.getMyPreferenceInt(c, "custom1_presoShadowColor", grey));
    }
    private void setThemeCustom2(Context c, Preferences preferences) {
        setMetronomeColor(preferences.getMyPreferenceInt(c,"custom2_metronomeColor",       darkishred));
        setPageButtonsColor(preferences.getMyPreferenceInt(c,"custom2_pageButtonsColor",     secondary));
        setStickyTextColor(preferences.getMyPreferenceInt(c,"custom2_stickyTextColor",      black));
        setStickyBackgroundColor(preferences.getMyPreferenceInt(c,"custom2_stickyBackgroundColor",lightyellow));
        setExtraInfoBgColor(preferences.getMyPreferenceInt(c,"custom2_extraInfoBgColor",     grey));
        setExtraInfoTextColor(preferences.getMyPreferenceInt(c,"custom2_extraInfoTextColor",   white));
        setLyricsTextColor(preferences.getMyPreferenceInt(c,"custom2_lyricsTextColor",      black));
        setLyricsCapoColor(preferences.getMyPreferenceInt(c,"custom2_lyricsCapoColor",      red));
        setLyricsBackgroundColor(preferences.getMyPreferenceInt(c,"custom2_lyricsBackgroundColor",white));
        setLyricsVerseColor(preferences.getMyPreferenceInt(c,"custom2_lyricsVerseColor",     white));
        setLyricsChorusColor(preferences.getMyPreferenceInt(c,"custom2_lyricsChorusColor",    white));
        setLyricsBridgeColor(preferences.getMyPreferenceInt(c,"custom2_lyricsBridgeColor",    white));
        setLyricsCommentColor(preferences.getMyPreferenceInt(c,"custom2_lyricsCommentColor",   white));
        setLyricsPreChorusColor(preferences.getMyPreferenceInt(c,"custom2_lyricsPreChorusColor", white));
        setLyricsTagColor(preferences.getMyPreferenceInt(c,"custom2_lyricsTagColor",       white));
        setLyricsChordsColor(preferences.getMyPreferenceInt(c,"custom2_lyricsChordsColor",    darkblue));
        setLyricsCustomColor(preferences.getMyPreferenceInt(c,"custom2_lyricsCustomColor",    white));
        setPresoFontColor(preferences.getMyPreferenceInt(c,"custom2_presoFontColor",       white));
        setPresoInfoFontColor(preferences.getMyPreferenceInt(c, "custom2_presoInfoFontColor", white));
        setPresoAlertColor(preferences.getMyPreferenceInt(c, "custom2_presoAlertColor", red));
        setPresoShadowColor(preferences.getMyPreferenceInt(c, "custom2_presoShadowColor", grey));
    }

    public int getValue(String what) {
        switch(what) {
            case "lyricsTextColor":
            default:
                return getLyricsTextColor();
            case "lyricsBackgroundColor":
                return getLyricsBackgroundColor();
            case "lyricsCapoColor":
                return getLyricsCapoColor();
            case "lyricsVerseColor":
                return getLyricsVerseColor();
            case "lyricsChorusColor":
                return getLyricsChorusColor();
            case "lyricsBridgeColor":
                return getLyricsBridgeColor();
            case "lyricsCommentColor":
                return getLyricsCommentColor();
            case "lyricsPreChorusColor":
                return getLyricsPreChorusColor();
            case "lyricsTagColor":
                return getLyricsTagColor();
            case "lyricsChordsColor":
                return getLyricsChordsColor();
            case "lyricsCustomColor":
                return getLyricsCustomColor();
            case "presoFontColor":
                return getPresoFontColor();
            case "presoInfoFontColor":
                return getPresoInfoFontColor();
            case "presoAlertColor":
                return getPresoAlertColor();
            case "presoShadowColor":
                return getPresoShadowColor();
            case "metronomeColor":
                return getMetronomeColor();
            case "pageButtonsColor":
                return getPageButtonsColor();
            case "stickyTextColor":
                return getStickyTextColor();
            case "stickyBackgroundColor":
                return getStickyBackgroundColor();
            case "extraInfoBgColor":
                return getExtraInfoBgColor();
            case "extraInfoTextColor":
                return getExtraInfoTextColor();
        }
    }
    private String which;
    public void setWhich(String which) {
        this.which = which;
    }
    public String getWhich() {
        return which;
    }

    // Default colours
    private final int secondary = R.color.colorSecondary;
    private final int darkblue = 0xff0000dd;
    private final int vdarkblue = 0xff000022;
    private final int purplyblue = 0xff452277;
    private final int vlightcyan = 0xffeeffff;
    private final int vlightblue = 0xffeeeeff;
    private final int blue = 0xff0000ff;
    private final int black = 0xff000000;
    private final int white = 0xffffffff;
    private final int grey = 0xff666666;
    private final int lightgrey = 0xff222222;
    private final int lightyellow = 0xffddaa00;
    private final int yellow = 0xffffff00;
    private final int vdarkyellow = 0xff111100;
    private final int red = 0xffff0000;
    private final int vdarkred = 0xff220000;
    private final int darkishred = 0xffaa1212;
    private final int transparent = 0x00000000;
    private final int vdarkgreen = 0xff002200;
    private final int darkishgreen = 0xff112211;
    private final int lightgreen = 0xffeeddee;
    private final int vlightgreen = 0xffeeffee;
    private final int green = 0xff00ff00;
    private final int darkpurple = 0xff220022;
    private final int vlightpurple = 0xffffeeff;
    private final int lightishcyan = 0xffddeeff;
    private final int highlighterblack = 0x66000000;
    private final int highlighterwhite = 0x66ffffff;
    private final int highlighterblue = 0x660000ff;
    private final int highlighterred = 0x66ff0000;
    private final int highlightergreen = 0x6600ff00;
    private final int highighteryellow = 0x66ffff00;
}
package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;

import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

public class ThemeColors {

    // This object holds the user theme colours


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
    private int metronomeColor;
    private int pageButtonsColor;
    private int stickyTextColor;
    private int stickyBackgroundColor;
    private int extraInfoBgColor;
    private int extraInfoTextColor;

    // Set the values with updates
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
        setMetronomeColor(preferences.getMyPreferenceInt(c, "dark_metronomeColor",          StaticVariables.darkishred));
        setPageButtonsColor(preferences.getMyPreferenceInt(c, "dark_pageButtonsColor",        StaticVariables.purplyblue));
        setStickyTextColor(preferences.getMyPreferenceInt(c, "dark_stickyTextColor",         StaticVariables.black));
        setStickyBackgroundColor(preferences.getMyPreferenceInt(c, "dark_stickyBackgroundColor",   StaticVariables.lightyellow));
        setExtraInfoBgColor(preferences.getMyPreferenceInt(c, "dark_extraInfoBgColor",        StaticVariables.grey));
        setExtraInfoTextColor(preferences.getMyPreferenceInt(c, "dark_extraInfoTextColor",      StaticVariables.white));
        setLyricsTextColor(preferences.getMyPreferenceInt(c, "dark_lyricsTextColor",         StaticVariables.white));
        setLyricsCapoColor(preferences.getMyPreferenceInt(c, "dark_lyricsCapoColor",         StaticVariables.red));
        setLyricsBackgroundColor(preferences.getMyPreferenceInt(c, "dark_lyricsBackgroundColor",   StaticVariables.black));
        setLyricsVerseColor(preferences.getMyPreferenceInt(c, "dark_lyricsVerseColor",        StaticVariables.black));
        setLyricsChorusColor(preferences.getMyPreferenceInt(c, "dark_lyricsChorusColor",       StaticVariables.vdarkblue));
        setLyricsBridgeColor(preferences.getMyPreferenceInt(c, "dark_lyricsBridgeColor",       StaticVariables.vdarkred));
        setLyricsCommentColor(preferences.getMyPreferenceInt(c, "dark_lyricsCommentColor",      StaticVariables.vdarkgreen));
        setLyricsPreChorusColor(preferences.getMyPreferenceInt(c, "dark_lyricsPreChorusColor",    StaticVariables.darkishgreen));
        setLyricsTagColor(preferences.getMyPreferenceInt(c, "dark_lyricsTagColor",          StaticVariables.darkpurple));
        setLyricsChordsColor(preferences.getMyPreferenceInt(c, "dark_lyricsChordsColor",       StaticVariables.yellow));
        setLyricsCustomColor(preferences.getMyPreferenceInt(c, "dark_lyricsCustomColor",       StaticVariables.vdarkyellow));
        setPresoFontColor(preferences.getMyPreferenceInt(c, "dark_presoFontColor",          StaticVariables.white));
        setPresoInfoFontColor(preferences.getMyPreferenceInt(c, "dark_presoInfoFontColor", StaticVariables.white));
    }
    private void setThemeLight(Context c, Preferences preferences) {
        setMetronomeColor(preferences.getMyPreferenceInt(c,"light_metronomeColor",         StaticVariables.darkishred));
        setPageButtonsColor(preferences.getMyPreferenceInt(c,"light_pageButtonsColor",       StaticVariables.purplyblue));
        setStickyTextColor(preferences.getMyPreferenceInt(c,"light_stickyTextColor",        StaticVariables.black));
        setStickyBackgroundColor(preferences.getMyPreferenceInt(c,"light_stickyBackgroundColor",  StaticVariables.lightyellow));
        setExtraInfoBgColor(preferences.getMyPreferenceInt(c,"light_extraInfoBgColor",       StaticVariables.grey));
        setExtraInfoTextColor(preferences.getMyPreferenceInt(c,"light_extraInfoTextColor",     StaticVariables.white));
        setLyricsTextColor(preferences.getMyPreferenceInt(c,"light_lyricsTextColor",        StaticVariables.black));
        setLyricsCapoColor(preferences.getMyPreferenceInt(c,"light_lyricsCapoColor",        StaticVariables.red));
        setLyricsBackgroundColor(preferences.getMyPreferenceInt(c,"light_lyricsBackgroundColor",  StaticVariables.white));
        setLyricsVerseColor(preferences.getMyPreferenceInt(c,"light_lyricsVerseColor",       StaticVariables.white));
        setLyricsChorusColor(preferences.getMyPreferenceInt(c,"light_lyricsChorusColor",      StaticVariables.vlightpurple));
        setLyricsBridgeColor(preferences.getMyPreferenceInt(c,"light_lyricsBridgeColor",      StaticVariables.vlightcyan));
        setLyricsCommentColor(preferences.getMyPreferenceInt(c,"light_lyricsCommentColor",     StaticVariables.vlightblue));
        setLyricsPreChorusColor(preferences.getMyPreferenceInt(c,"light_lyricsPreChorusColor",   StaticVariables.lightgreen));
        setLyricsTagColor(preferences.getMyPreferenceInt(c,"light_lyricsTagColor",         StaticVariables.vlightgreen));
        setLyricsChordsColor(preferences.getMyPreferenceInt(c,"light_lyricsChordsColor",      StaticVariables.darkblue));
        setLyricsCustomColor(preferences.getMyPreferenceInt(c,"light_lyricsCustomColor",      StaticVariables.lightishcyan));
        setPresoFontColor(preferences.getMyPreferenceInt(c,"light_presoFontColor",         StaticVariables.white));
        setPresoInfoFontColor(preferences.getMyPreferenceInt(c, "light_presoInfoFontColor", StaticVariables.white));
    }
    private void setThemeCustom1(Context c, Preferences preferences) {
        setMetronomeColor(preferences.getMyPreferenceInt(c,"custom1_metronomeColor",       StaticVariables.darkishred));
        setPageButtonsColor(preferences.getMyPreferenceInt(c,"custom1_pageButtonsColor",     StaticVariables.purplyblue));
        setStickyTextColor(preferences.getMyPreferenceInt(c,"custom1_stickyTextColor",      StaticVariables.black));
        setStickyBackgroundColor(preferences.getMyPreferenceInt(c,"custom1_stickyBackgroundColor",StaticVariables.lightyellow));
        setExtraInfoBgColor(preferences.getMyPreferenceInt(c,"custom1_extraInfoBgColor",     StaticVariables.grey));
        setExtraInfoTextColor(preferences.getMyPreferenceInt(c,"custom1_extraInfoTextColor",   StaticVariables.white));
        setLyricsTextColor(preferences.getMyPreferenceInt(c,"custom1_lyricsTextColor",      StaticVariables.white));
        setLyricsCapoColor(preferences.getMyPreferenceInt(c,"custom1_lyricsCapoColor",      StaticVariables.red));
        setLyricsBackgroundColor(preferences.getMyPreferenceInt(c,"custom1_lyricsBackgroundColor",StaticVariables.black));
        setLyricsVerseColor(preferences.getMyPreferenceInt(c,"custom1_lyricsVerseColor",     StaticVariables.black));
        setLyricsChorusColor(preferences.getMyPreferenceInt(c,"custom1_lyricsChorusColor",    StaticVariables.black));
        setLyricsBridgeColor(preferences.getMyPreferenceInt(c,"custom1_lyricsBridgeColor",    StaticVariables.black));
        setLyricsCommentColor(preferences.getMyPreferenceInt(c,"custom1_lyricsCommentColor",   StaticVariables.black));
        setLyricsPreChorusColor(preferences.getMyPreferenceInt(c,"custom1_lyricsPreChorusColor", StaticVariables.black));
        setLyricsTagColor(preferences.getMyPreferenceInt(c,"custom1_lyricsTagColor",       StaticVariables.black));
        setLyricsChordsColor(preferences.getMyPreferenceInt(c,"custom1_lyricsChordsColor",    StaticVariables.yellow));
        setLyricsCustomColor(preferences.getMyPreferenceInt(c,"custom1_lyricsCustomColor",    StaticVariables.black));
        setPresoFontColor(preferences.getMyPreferenceInt(c,"custom1_presoFontColor",       StaticVariables.white));
        setPresoInfoFontColor(preferences.getMyPreferenceInt(c, "custom1_presoInfoFontColor", StaticVariables.white));
    }
    private void setThemeCustom2(Context c, Preferences preferences) {
        setMetronomeColor(preferences.getMyPreferenceInt(c,"custom2_metronomeColor",       StaticVariables.darkishred));
        setPageButtonsColor(preferences.getMyPreferenceInt(c,"custom2_pageButtonsColor",     StaticVariables.purplyblue));
        setStickyTextColor(preferences.getMyPreferenceInt(c,"custom2_stickyTextColor",      StaticVariables.black));
        setStickyBackgroundColor(preferences.getMyPreferenceInt(c,"custom2_stickyBackgroundColor",StaticVariables.lightyellow));
        setExtraInfoBgColor(preferences.getMyPreferenceInt(c,"custom2_extraInfoBgColor",     StaticVariables.grey));
        setExtraInfoTextColor(preferences.getMyPreferenceInt(c,"custom2_extraInfoTextColor",   StaticVariables.white));
        setLyricsTextColor(preferences.getMyPreferenceInt(c,"custom2_lyricsTextColor",      StaticVariables.black));
        setLyricsCapoColor(preferences.getMyPreferenceInt(c,"custom2_lyricsCapoColor",      StaticVariables.red));
        setLyricsBackgroundColor(preferences.getMyPreferenceInt(c,"custom2_lyricsBackgroundColor",StaticVariables.white));
        setLyricsVerseColor(preferences.getMyPreferenceInt(c,"custom2_lyricsVerseColor",     StaticVariables.white));
        setLyricsChorusColor(preferences.getMyPreferenceInt(c,"custom2_lyricsChorusColor",    StaticVariables.white));
        setLyricsBridgeColor(preferences.getMyPreferenceInt(c,"custom2_lyricsBridgeColor",    StaticVariables.white));
        setLyricsCommentColor(preferences.getMyPreferenceInt(c,"custom2_lyricsCommentColor",   StaticVariables.white));
        setLyricsPreChorusColor(preferences.getMyPreferenceInt(c,"custom2_lyricsPreChorusColor", StaticVariables.white));
        setLyricsTagColor(preferences.getMyPreferenceInt(c,"custom2_lyricsTagColor",       StaticVariables.white));
        setLyricsChordsColor(preferences.getMyPreferenceInt(c,"custom2_lyricsChordsColor",    StaticVariables.darkblue));
        setLyricsCustomColor(preferences.getMyPreferenceInt(c,"custom2_lyricsCustomColor",    StaticVariables.white));
        setPresoFontColor(preferences.getMyPreferenceInt(c,"custom2_presoFontColor",       StaticVariables.white));
        setPresoInfoFontColor(preferences.getMyPreferenceInt(c, "custom2_presoInfoFontColor", StaticVariables.white));
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
                return lyricsVerseColor;
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
}
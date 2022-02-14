package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.graphics.Color;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ThemeColors {

    private final String TAG = "ThemeColors";

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
    private float pageButtonsSplitAlpha;
    private int pageButtonsSplitColor;
    private int stickyTextColor;
    private int stickyBackgroundColor;
    private int stickyBackgroundSplitColor;
    private float stickyBackgroundSplitAlpha;
    private int extraInfoBgColor;
    private int extraInfoBgSplitColor;
    private float extraInfoBgSplitAlpha;
    private int extraInfoTextColor;
    private int highlightChordColor;
    private int highlightHeadingColor;

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
    public void setPresoShadowColor(int i) {
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
    public void setHighlightChordColor(int i) {
        this.highlightChordColor = i;
    }
    public void setHighlightHeadingColor(int i) {
        this.highlightHeadingColor = i;
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
    public float getPageButtonsSplitAlpha() {
        return pageButtonsSplitAlpha;
    }
    public int getPageButtonsSplitColor() {
        return pageButtonsSplitColor;
    }
    public int getMetronomeColor() {
        return metronomeColor;
    }
    public int getStickyBackgroundColor() {
        return stickyBackgroundColor;
    }
    public int getStickyBackgroundSplitColor() {
        return stickyBackgroundSplitColor;
    }
    public float getStickyBackgroundSplitAlpha() {
        return stickyBackgroundSplitAlpha;
    }
    public int getStickyTextColor() {
        return stickyTextColor;
    }
    public int getExtraInfoBgColor() {
        return extraInfoBgColor;
    }
    public int getExtraInfoBgSplitColor() {
        return extraInfoBgSplitColor;
    }
    public float getExtraInfoBgSplitAlpha() {
        return extraInfoBgSplitAlpha;
    }
    public int getExtraInfoTextColor() {
        return extraInfoTextColor;
    }
    public int getHighlightChordColor() {
        return highlightChordColor;
    }
    public int getHighlightHeadingColor() {
        return highlightHeadingColor;
    }

    public void getDefaultColors(Context c, MainActivityInterface mainActivityInterface) {
        themeName = mainActivityInterface.getPreferences().getMyPreferenceString(c,"appTheme","dark");
        switch (themeName) {
            case "dark":
            default:
                setThemeDark(c, mainActivityInterface);
                break;
            case "light":
                setThemeLight(c, mainActivityInterface);
                break;
            case "custom1":
                setThemeCustom1(c, mainActivityInterface);
                break;
            case "custom2":
                setThemeCustom2(c, mainActivityInterface);
                break;
        }
        splitColorAndAlpha(mainActivityInterface);
    }

    public void resetTheme(Context c, MainActivityInterface mainActivityInterface) {
        String theme = mainActivityInterface.getPreferences().getMyPreferenceString(c,"appTheme","dark");

        // Some colours are the same regardless of mode
        theme = theme + "_";
        mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"metronomeColor",             darkishred);
        mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"pageButtonsColor",           pageButtonColor);
        mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"stickyTextColor",            black);
        mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"stickyBackgroundColor",      lightyellow);
        mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"extraInfoBgColor",           grey);
        mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"extraInfoTextColor",         white);
        mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsCapoColor",            red);
        mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"presoAlertColor",            red);
        mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"presoHighlightChordColor",   transparent);
        mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"presoHighlightHeadingColor", transparent);
        mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"presoFontColor",             white);
        mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"presoInfoFontColor",         white);

        // Others are theme specific
        switch(theme) {
            case "dark_":
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsTextColor",        white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsBackgroundColor",  black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsVerseColor",       black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsChorusColor",      vdarkblue);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsBridgeColor",      vdarkred);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsCommentColor",     vdarkgreen);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsPreChorusColor",   darkishgreen);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsTagColor",         darkpurple);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsChordsColor",      yellow);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsCustomColor",      vdarkyellow);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"presoShadowColor",       translucentDark);
                break;

            case "light_":
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsTextColor",        black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsBackgroundColor",  white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsVerseColor",       white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsChorusColor",      vlightpurple);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsBridgeColor",      vlightcyan);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsCommentColor",     vlightblue);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsPreChorusColor",   lightgreen);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsTagColor",         vlightgreen);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsChordsColor",      darkblue);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsCustomColor",      lightishcyan);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"presoShadowColor",       translucentLight);
                break;

            case "custom1_":
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsTextColor",        white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsBackgroundColor",  black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsVerseColor",       black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsChorusColor",      black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsBridgeColor",      black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsCommentColor",     black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsPreChorusColor",   black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsTagColor",         black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsChordsColor",      white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsCustomColor",      black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"presoShadowColor",       translucentDark);
                break;

            case "custom2_":
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsTextColor",        black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsBackgroundColor",  white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsVerseColor",       white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsChorusColor",      white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsBridgeColor",      white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsCommentColor",     white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsPreChorusColor",   white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsTagColor",         white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsChordsColor",      black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"lyricsCustomColor",      white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(c,theme+"presoShadowColor",       translucentLight);
                break;

        }



    }
    private void setThemeDark(Context c, MainActivityInterface mainActivityInterface) {
        setMetronomeColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_metronomeColor",               darkishred));
        setPageButtonsColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_pageButtonsColor",           pageButtonColor));
        setStickyTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_stickyTextColor",             black));
        setStickyBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_stickyBackgroundColor", stickybg));
        setExtraInfoBgColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_extraInfoBgColor",           grey));
        setExtraInfoTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_extraInfoTextColor",       white));
        setLyricsTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsTextColor",             white));
        setLyricsCapoColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsCapoColor",             red));
        setLyricsBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsBackgroundColor", black));
        setLyricsVerseColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsVerseColor",           black));
        setLyricsChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsChorusColor",         vdarkblue));
        setLyricsBridgeColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsBridgeColor",         vdarkred));
        setLyricsCommentColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsCommentColor",       vdarkgreen));
        setLyricsPreChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsPreChorusColor",   darkishgreen));
        setLyricsTagColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsTagColor",               darkpurple));
        setLyricsChordsColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsChordsColor",         yellow));
        setLyricsCustomColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsCustomColor",         vdarkyellow));
        setPresoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_presoFontColor",               white));
        setPresoInfoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_presoInfoFontColor",       white));
        setPresoAlertColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_presoAlertColor",             red));
        setPresoShadowColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_presoShadowColor",           translucentDark));
        setHighlightChordColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_highlightChordColor",     transparent));
        setHighlightHeadingColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_highlightHeadingColor", transparent));
    }
    private void setThemeLight(Context c, MainActivityInterface mainActivityInterface) {
        setMetronomeColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_metronomeColor",               darkishred));
        setPageButtonsColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_pageButtonsColor",           pageButtonColor));
        setStickyTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_stickyTextColor",             black));
        setStickyBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_stickyBackgroundColor", stickybg));
        setExtraInfoBgColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_extraInfoBgColor",           grey));
        setExtraInfoTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_extraInfoTextColor",       white));
        setLyricsTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsTextColor",             black));
        setLyricsCapoColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsCapoColor",             red));
        setLyricsBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsBackgroundColor", white));
        setLyricsVerseColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsVerseColor",           white));
        setLyricsChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsChorusColor",         vlightpurple));
        setLyricsBridgeColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsBridgeColor",         vlightcyan));
        setLyricsCommentColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsCommentColor",       vlightblue));
        setLyricsPreChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsPreChorusColor",   lightgreen));
        setLyricsTagColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsTagColor",               vlightgreen));
        setLyricsChordsColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsChordsColor",         darkblue));
        setLyricsCustomColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsCustomColor",         lightishcyan));
        setPresoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_presoFontColor",               white));
        setPresoInfoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "light_presoInfoFontColor",      white));
        setPresoAlertColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "light_presoAlertColor",            red));
        setPresoShadowColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "light_presoShadowColor",          translucentLight));
        setHighlightChordColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "light_highlightChordColor",    transparent));
        setHighlightHeadingColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "light_highlightHeadingColor",transparent));
    }
    private void setThemeCustom1(Context c, MainActivityInterface mainActivityInterface) {
        setMetronomeColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_metronomeColor",             darkishred));
        setPageButtonsColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_pageButtonsColor",         pageButtonColor));
        setStickyTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_stickyTextColor",           black));
        setStickyBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_stickyBackgroundColor",stickybg));
        setExtraInfoBgColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_extraInfoBgColor",         grey));
        setExtraInfoTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_extraInfoTextColor",     white));
        setLyricsTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsTextColor",           white));
        setLyricsCapoColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsCapoColor",           red));
        setLyricsBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsBackgroundColor",black));
        setLyricsVerseColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsVerseColor",         black));
        setLyricsChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsChorusColor",       black));
        setLyricsBridgeColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsBridgeColor",       black));
        setLyricsCommentColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsCommentColor",     black));
        setLyricsPreChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsPreChorusColor", black));
        setLyricsTagColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsTagColor",             black));
        setLyricsChordsColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsChordsColor",       yellow));
        setLyricsCustomColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsCustomColor",       black));
        setPresoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_presoFontColor",             white));
        setPresoInfoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom1_presoInfoFontColor",    white));
        setPresoAlertColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom1_presoAlertColor",          red));
        setPresoShadowColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom1_presoShadowColor",        translucentDark));
        setHighlightChordColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom1_highlightChordColor",  transparent));
        setHighlightHeadingColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom1_highlightHeadingColor",transparent));

    }
    private void setThemeCustom2(Context c, MainActivityInterface mainActivityInterface) {
        setMetronomeColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_metronomeColor",             darkishred));
        setPageButtonsColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_pageButtonsColor",         pageButtonColor));
        setStickyTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_stickyTextColor",           black));
        setStickyBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_stickyBackgroundColor",stickybg));
        setExtraInfoBgColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_extraInfoBgColor",         grey));
        setExtraInfoTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_extraInfoTextColor",     white));
        setLyricsTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsTextColor",           black));
        setLyricsCapoColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsCapoColor",           red));
        setLyricsBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsBackgroundColor",white));
        setLyricsVerseColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsVerseColor",         white));
        setLyricsChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsChorusColor",       white));
        setLyricsBridgeColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsBridgeColor",       white));
        setLyricsCommentColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsCommentColor",     white));
        setLyricsPreChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsPreChorusColor", white));
        setLyricsTagColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsTagColor",             white));
        setLyricsChordsColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsChordsColor",       darkblue));
        setLyricsCustomColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsCustomColor",       white));
        setPresoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_presoFontColor",             white));
        setPresoInfoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom2_presoInfoFontColor",    white));
        setPresoAlertColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom2_presoAlertColor",          red));
        setPresoShadowColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom2_presoShadowColor",        translucentLight));
        setHighlightChordColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom2_highlightChordColor",  transparent));
        setHighlightHeadingColor(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom2_highlightHeadingColor",transparent));
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
            case "highlightChordColor":
                return getHighlightChordColor();
            case "highlightHeadingColor":
                return getHighlightHeadingColor();
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
    private final int pageButtonColor = 0xdd294959;  // Lower opacity secondary
    private final int darkblue = 0xff0000dd;
    private final int vdarkblue = 0xff000022;
    private final int purplyblue = 0xff452277;
    private final int vlightcyan = 0xffeeffff;
    private final int vlightblue = 0xffeeeeff;
    private final int blue = 0xff0000ff;
    private final int black = 0xff000000;
    private final int white = 0xffffffff;
    private final int grey = 0xff666666;
    private final int translucentDark = 0xdd000000;
    private final int translucentLight = 0xddffffff;
    private final int lightgrey = 0xff222222;
    private final int vlightgrey = R.color.vlightgrey;
    private final int lightyellow = 0xffddaa00;
    private final int yellow = 0xffffff00;
    private final int darkyellow = 0xffaaaa00;;
    private final int stickybg = 0xddddaa00;
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

    public int getColorInt(String which) {
        int color = white;
        switch (which) {
            case "black":
                color = black;
                break;
            case "darkishgreen":
                color = darkishgreen;
                break;
            case "darkpurple":
                color = darkpurple;
                break;
            case "lightgreen":
                color = lightgreen;
                break;
            case "lightishcyan":
                color = lightishcyan;
                break;
            case "red":
                color = red;
                break;
            case "vdarkblue":
                color = vdarkblue;
                break;
            case "vdarkgreen":
                color = vdarkgreen;
                break;
            case "vdarkred":
                color = vdarkred;
                break;
            case "vdarkyellow":
                color = vdarkyellow;
                break;
            case "vlightblue":
                color = vlightblue;
                break;
            case "vlightcyan":
                color = vlightcyan;
                break;
            case "vlightgreen":
                color = vlightgreen;
                break;
            case "vlightpurple":
                color = vlightpurple;
                break;
            case "white":
                color = white;
                break;
            case "yellow":
                color = yellow;
                break;
            case "transparent":
                color = transparent;
                break;
            case "vlightgrey":
                color = vlightgrey;
        }
        return color;
    }

    public void splitColorAndAlpha(MainActivityInterface mainActivityInterface) {
        // The colour will include alpha.  Strip this out
        int alpha = Math.round(Color.alpha(pageButtonsColor));
        int red = Color.red(pageButtonsColor);
        int green = Color.green(pageButtonsColor);
        int blue = Color.blue(pageButtonsColor);
        pageButtonsSplitColor = Color.argb(255, red, green, blue);
        pageButtonsSplitAlpha = alpha / 255f;
        // Update page buttons and extra info
        mainActivityInterface.getPageButtons().updateColors(mainActivityInterface);
        mainActivityInterface.getDisplayPrevNext().updateColors();
        mainActivityInterface.updateOnScreenInfo("alpha");

        // Repeat for the extra info
        alpha = Math.round(Color.alpha(extraInfoBgColor));
        red = Color.red(extraInfoBgColor);
        green = Color.green(extraInfoBgColor);
        blue = Color.blue(extraInfoBgColor);
        extraInfoBgSplitColor = Color.argb(255,red,green,blue);
        extraInfoBgSplitAlpha = alpha / 255f;

        // Repeat for the sticky notes
        alpha = Math.round(Color.alpha(stickyBackgroundColor));
        red = Color.red(stickyBackgroundColor);
        green = Color.green(stickyBackgroundColor);
        blue = Color.blue(stickyBackgroundColor);
        stickyBackgroundSplitColor = Color.argb(255,red,green,blue);
        stickyBackgroundSplitAlpha = alpha / 255f;
    }
}
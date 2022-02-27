package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.graphics.Color;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ThemeColors {

    private final String TAG = "ThemeColors";
    private final MainActivityInterface mainActivityInterface;

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

    public ThemeColors(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }
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

    public void getDefaultColors() {
        themeName = mainActivityInterface.getPreferences().getMyPreferenceString("appTheme","dark");
        switch (themeName) {
            case "dark":
            default:
                setThemeDark();
                break;
            case "light":
                setThemeLight();
                break;
            case "custom1":
                setThemeCustom1();
                break;
            case "custom2":
                setThemeCustom2();
                break;
        }
        splitColorAndAlpha();
    }

    public void resetTheme() {
        String theme = mainActivityInterface.getPreferences().getMyPreferenceString("appTheme","dark");

        // Some colours are the same regardless of mode
        theme = theme + "_";
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"metronomeColor",             darkishred);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"pageButtonsColor",           pageButtonColor);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"stickyTextColor",            black);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"stickyBackgroundColor",      lightyellow);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"extraInfoBgColor",           grey);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"extraInfoTextColor",         white);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsCapoColor",            red);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"presoAlertColor",            red);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"presoHighlightChordColor",   transparent);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"presoHighlightHeadingColor", transparent);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"presoFontColor",             white);
        mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"presoInfoFontColor",         white);

        // Others are theme specific
        switch(theme) {
            case "dark_":
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsTextColor",        white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsBackgroundColor",  black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsVerseColor",       black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsChorusColor",      vdarkblue);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsBridgeColor",      vdarkred);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsCommentColor",     vdarkgreen);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsPreChorusColor",   darkishgreen);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsTagColor",         darkpurple);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsChordsColor",      yellow);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsCustomColor",      vdarkyellow);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"presoShadowColor",       translucentDark);
                break;

            case "light_":
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsTextColor",        black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsBackgroundColor",  white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsVerseColor",       white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsChorusColor",      vlightpurple);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsBridgeColor",      vlightcyan);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsCommentColor",     vlightblue);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsPreChorusColor",   lightgreen);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsTagColor",         vlightgreen);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsChordsColor",      darkblue);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsCustomColor",      lightishcyan);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"presoShadowColor",       translucentLight);
                break;

            case "custom1_":
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsTextColor",        white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsBackgroundColor",  black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsVerseColor",       black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsChorusColor",      black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsBridgeColor",      black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsCommentColor",     black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsPreChorusColor",   black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsTagColor",         black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsChordsColor",      white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsCustomColor",      black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"presoShadowColor",       translucentDark);
                break;

            case "custom2_":
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsTextColor",        black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsBackgroundColor",  white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsVerseColor",       white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsChorusColor",      white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsBridgeColor",      white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsCommentColor",     white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsPreChorusColor",   white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsTagColor",         white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsChordsColor",      black);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"lyricsCustomColor",      white);
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"presoShadowColor",       translucentLight);
                break;

        }



    }
    private void setThemeDark() {
        setMetronomeColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_metronomeColor",               darkishred));
        setPageButtonsColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_pageButtonsColor",           pageButtonColor));
        setStickyTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_stickyTextColor",             black));
        setStickyBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_stickyBackgroundColor", stickybg));
        setExtraInfoBgColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_extraInfoBgColor",           pageButtonColor));
        setExtraInfoTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_extraInfoTextColor",       white));
        setLyricsTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_lyricsTextColor",             white));
        setLyricsCapoColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_lyricsCapoColor",             red));
        setLyricsBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_lyricsBackgroundColor", black));
        setLyricsVerseColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_lyricsVerseColor",           black));
        setLyricsChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_lyricsChorusColor",         vdarkblue));
        setLyricsBridgeColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_lyricsBridgeColor",         vdarkred));
        setLyricsCommentColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_lyricsCommentColor",       vdarkgreen));
        setLyricsPreChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_lyricsPreChorusColor",   darkishgreen));
        setLyricsTagColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_lyricsTagColor",               darkpurple));
        setLyricsChordsColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_lyricsChordsColor",         yellow));
        setLyricsCustomColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_lyricsCustomColor",         vdarkyellow));
        setPresoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_presoFontColor",               white));
        setPresoInfoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_presoInfoFontColor",       white));
        setPresoAlertColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_presoAlertColor",             red));
        setPresoShadowColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_presoShadowColor",           translucentDark));
        setHighlightChordColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_highlightChordColor",     transparent));
        setHighlightHeadingColor(mainActivityInterface.getPreferences().getMyPreferenceInt("dark_highlightHeadingColor", transparent));
    }
    private void setThemeLight() {
        setMetronomeColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_metronomeColor",               darkishred));
        setPageButtonsColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_pageButtonsColor",           pageButtonColor));
        setStickyTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_stickyTextColor",             black));
        setStickyBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_stickyBackgroundColor", stickybg));
        setExtraInfoBgColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_extraInfoBgColor",           pageButtonColor));
        setExtraInfoTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_extraInfoTextColor",       white));
        setLyricsTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_lyricsTextColor",             black));
        setLyricsCapoColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_lyricsCapoColor",             red));
        setLyricsBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_lyricsBackgroundColor", white));
        setLyricsVerseColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_lyricsVerseColor",           white));
        setLyricsChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_lyricsChorusColor",         vlightpurple));
        setLyricsBridgeColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_lyricsBridgeColor",         vlightcyan));
        setLyricsCommentColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_lyricsCommentColor",       vlightblue));
        setLyricsPreChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_lyricsPreChorusColor",   lightgreen));
        setLyricsTagColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_lyricsTagColor",               vlightgreen));
        setLyricsChordsColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_lyricsChordsColor",         darkblue));
        setLyricsCustomColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_lyricsCustomColor",         lightishcyan));
        setPresoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_presoFontColor",               white));
        setPresoInfoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_presoInfoFontColor",      white));
        setPresoAlertColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_presoAlertColor",            red));
        setPresoShadowColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_presoShadowColor",          translucentLight));
        setHighlightChordColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_highlightChordColor",    transparent));
        setHighlightHeadingColor(mainActivityInterface.getPreferences().getMyPreferenceInt("light_highlightHeadingColor",transparent));
    }
    private void setThemeCustom1() {
        setMetronomeColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_metronomeColor",             darkishred));
        setPageButtonsColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_pageButtonsColor",         pageButtonColor));
        setStickyTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_stickyTextColor",           black));
        setStickyBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_stickyBackgroundColor",stickybg));
        setExtraInfoBgColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_extraInfoBgColor",         pageButtonColor));
        setExtraInfoTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_extraInfoTextColor",     white));
        setLyricsTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_lyricsTextColor",           white));
        setLyricsCapoColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_lyricsCapoColor",           red));
        setLyricsBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_lyricsBackgroundColor",black));
        setLyricsVerseColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_lyricsVerseColor",         black));
        setLyricsChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_lyricsChorusColor",       black));
        setLyricsBridgeColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_lyricsBridgeColor",       black));
        setLyricsCommentColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_lyricsCommentColor",     black));
        setLyricsPreChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_lyricsPreChorusColor", black));
        setLyricsTagColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_lyricsTagColor",             black));
        setLyricsChordsColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_lyricsChordsColor",       yellow));
        setLyricsCustomColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_lyricsCustomColor",       black));
        setPresoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_presoFontColor",             white));
        setPresoInfoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_presoInfoFontColor",    white));
        setPresoAlertColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_presoAlertColor",          red));
        setPresoShadowColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_presoShadowColor",        translucentDark));
        setHighlightChordColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_highlightChordColor",  transparent));
        setHighlightHeadingColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom1_highlightHeadingColor",transparent));

    }
    private void setThemeCustom2() {
        setMetronomeColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_metronomeColor",             darkishred));
        setPageButtonsColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_pageButtonsColor",         pageButtonColor));
        setStickyTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_stickyTextColor",           black));
        setStickyBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_stickyBackgroundColor",stickybg));
        setExtraInfoBgColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_extraInfoBgColor",         pageButtonColor));
        setExtraInfoTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_extraInfoTextColor",     white));
        setLyricsTextColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_lyricsTextColor",           black));
        setLyricsCapoColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_lyricsCapoColor",           red));
        setLyricsBackgroundColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_lyricsBackgroundColor",white));
        setLyricsVerseColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_lyricsVerseColor",         white));
        setLyricsChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_lyricsChorusColor",       white));
        setLyricsBridgeColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_lyricsBridgeColor",       white));
        setLyricsCommentColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_lyricsCommentColor",     white));
        setLyricsPreChorusColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_lyricsPreChorusColor", white));
        setLyricsTagColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_lyricsTagColor",             white));
        setLyricsChordsColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_lyricsChordsColor",       darkblue));
        setLyricsCustomColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_lyricsCustomColor",       white));
        setPresoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_presoFontColor",             white));
        setPresoInfoFontColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_presoInfoFontColor",    white));
        setPresoAlertColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_presoAlertColor",          red));
        setPresoShadowColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_presoShadowColor",        translucentLight));
        setHighlightChordColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_highlightChordColor",  transparent));
        setHighlightHeadingColor(mainActivityInterface.getPreferences().getMyPreferenceInt("custom2_highlightHeadingColor",transparent));
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

    public void splitColorAndAlpha() {
        // The colour will include alpha.  Strip this out
        int alpha = Math.round(Color.alpha(pageButtonsColor));
        int red = Color.red(pageButtonsColor);
        int green = Color.green(pageButtonsColor);
        int blue = Color.blue(pageButtonsColor);
        pageButtonsSplitColor = Color.argb(255, red, green, blue);
        pageButtonsSplitAlpha = alpha / 255f;
        // Update page buttons and extra info
        mainActivityInterface.getPageButtons().updateColors();
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

    public int changePageButtonAlpha(float alpha) {
        int red = Color.red(pageButtonsColor);
        int green = Color.green(pageButtonsColor);
        int blue = Color.blue(pageButtonsColor);
        pageButtonsColor = Color.argb((Math.round(alpha*255f)),red,green,blue);
        pageButtonsSplitAlpha = alpha;
        pageButtonsSplitColor = Color.argb(255,red,green,blue);
        return pageButtonsColor;
    }
}
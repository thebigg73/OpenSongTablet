package com.garethevans.church.opensongtablet;

import android.graphics.Typeface;

import java.io.File;

class SetTypeFace {

    public static void setTypeface() {
        // Set up the custom font (if it has been set)
        FullscreenActivity.customfont = setCustomFont(FullscreenActivity.customfontname);

        switch (FullscreenActivity.mylyricsfontnum) {
            case 1:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface1;
                FullscreenActivity.commentfont = FullscreenActivity.typeface1;
                break;
            case 2:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface2;
                FullscreenActivity.commentfont = FullscreenActivity.typeface2;
                break;
            case 3:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface3;
                FullscreenActivity.commentfont = FullscreenActivity.typeface3;
                break;
            case 4:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface4;
                FullscreenActivity.commentfont = FullscreenActivity.typeface4i;
                break;
            case 5:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface5;
                FullscreenActivity.commentfont = FullscreenActivity.typeface5i;
                break;
            case 6:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface6;
                FullscreenActivity.commentfont = FullscreenActivity.typeface6;
                break;
            case 7:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface7;
                FullscreenActivity.commentfont = FullscreenActivity.typeface7i;
                break;
            case 8:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface8;
                FullscreenActivity.commentfont = FullscreenActivity.typeface8i;
                break;
            case 9:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface9;
                FullscreenActivity.commentfont = FullscreenActivity.typeface9i;
                break;
            case 10:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface10;
                FullscreenActivity.commentfont = FullscreenActivity.typeface10i;
                break;
            case 11:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface11;
                FullscreenActivity.commentfont = FullscreenActivity.typeface11i;
                break;
            case 12:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface12;
                FullscreenActivity.commentfont = FullscreenActivity.typeface12i;
                break;
            case 13:
                FullscreenActivity.lyricsfont = FullscreenActivity.customfont;
                FullscreenActivity.commentfont = FullscreenActivity.customfont;
                break;
            default:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface0;
                FullscreenActivity.commentfont = FullscreenActivity.typeface0;
                break;
        }

        switch (FullscreenActivity.mychordsfontnum) {
            case 1:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface1;
                break;
            case 2:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface2;
                break;
            case 3:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface3;
                break;
            case 4:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface4;
                break;
            case 5:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface5;
                break;
            case 6:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface6;
                break;
            case 7:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface7;
                break;
            case 8:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface8;
                break;
            case 9:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface9;
                break;
            case 10:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface10;
                break;
            case 11:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface11;
                break;
            case 12:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface12;
                break;
            case 13:
                FullscreenActivity.chordsfont = FullscreenActivity.customfont;
                break;
            default:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface0;
                break;
        }

        switch (FullscreenActivity.mypresofontnum) {
            case 1:
                FullscreenActivity.presofont = FullscreenActivity.typeface1;
                break;
            case 2:
                FullscreenActivity.presofont = FullscreenActivity.typeface2;
                break;
            case 3:
                FullscreenActivity.presofont = FullscreenActivity.typeface3;
                break;
            case 4:
                FullscreenActivity.presofont = FullscreenActivity.typeface4;
                break;
            case 5:
                FullscreenActivity.presofont = FullscreenActivity.typeface5;
                break;
            case 6:
                FullscreenActivity.presofont = FullscreenActivity.typeface6;
                break;
            case 7:
                FullscreenActivity.presofont = FullscreenActivity.typeface7;
                break;
            case 8:
                FullscreenActivity.presofont = FullscreenActivity.typeface8;
                break;
            case 9:
                FullscreenActivity.presofont = FullscreenActivity.typeface9;
                break;
            case 10:
                FullscreenActivity.presofont = FullscreenActivity.typeface10;
                break;
            case 11:
                FullscreenActivity.presofont = FullscreenActivity.typeface11;
                break;
            case 12:
                FullscreenActivity.presofont = FullscreenActivity.typeface12;
                break;
            case 13:
                FullscreenActivity.presofont = FullscreenActivity.customfont;
                break;
            default:
                FullscreenActivity.presofont = FullscreenActivity.typeface0;
                break;
        }

        switch (FullscreenActivity.mypresoinfofontnum) {
            case 1:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface1;
                break;
            case 2:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface2;
                break;
            case 3:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface3;
                break;
            case 4:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface4;
                break;
            case 5:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface5;
                break;
            case 6:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface6;
                break;
            case 7:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface7;
                break;
            case 8:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface8;
                break;
            case 9:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface9;
                break;
            case 10:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface10;
                break;
            case 11:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface11;
                break;
            case 12:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface12;
                break;
            case 13:
                FullscreenActivity.presoInfoFont = FullscreenActivity.customfont;
                break;
            default:
                FullscreenActivity.presoInfoFont = FullscreenActivity.typeface0;
                break;
        }

    }

    static Typeface setCustomFont(String ff) {
        Typeface tf = FullscreenActivity.typeface0;
        String fl = FullscreenActivity.dirfonts + "/" + ff;
        File cf = new File(fl);
        if (cf.exists() && (ff.endsWith(".ttf") || ff.endsWith(".otf"))) {
            tf = Typeface.createFromFile(cf);
        } else {
            FullscreenActivity.customfontname = "";
            Preferences.savePreferences();
        }
        return tf;
    }

    static String setupWebViewLyricFont(int fontnum) {

        String fontcode = "";

        switch (fontnum) {
            case 1:
                // Monospace
                fontcode += ".lyric      {font-family: Monospace; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: Monospace; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            case 2:
                // sans-serif
                fontcode += ".lyric      {font-family: Sans-serif; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: Sans-serif; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            case 3:
                // serif
                fontcode += ".lyric      {font-family: Serif; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: Serif; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            case 4:
                // firasans light
                fontcode += "@font-face  {font-family: 'firasanslight'; src: url('fonts/FiraSansOT-Light.otf');}\n";
                fontcode += "@font-face  {font-family: 'firasanslightitalic'; src: url('fonts/FiraSans-Italic.otf');}\n";
                fontcode += ".lyric      {font-family: 'firasanslight'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: 'firasanslightitalic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            case 5:
                // firasans regular
                fontcode += "@font-face  {font-family: 'firasans'; src: url('fonts/FiraSansOT-Regular.otf');}\n";
                fontcode += "@font-face  {font-family: 'firasansitalic'; src: url('fonts/FiraSans-LightItalic.otf');}\n";
                fontcode += ".lyric      {font-family: 'firasans'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: 'firasansitalic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            case 6:
                // kaushan
                fontcode += "@font-face  {font-family: 'kaushan'; src: url('fonts/KaushanScript-Regular.otf');}\n";
                fontcode += ".lyric      {font-family: 'kaushan'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: 'kaushan'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            case 7:
                // lato light
                fontcode += "@font-face  {font-family: 'latolight'; src: url('fonts/Lato-Lig.ttf');}\n";
                fontcode += "@font-face  {font-family: 'latolightitalic'; src: url('fonts/Lato-LigIta.ttf');}\n";
                fontcode += ".lyric      {font-family: 'latolight'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: 'latolightitalic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            case 8:
                // lato regular
                fontcode += "@font-face  {font-family: 'latoreg'; src: url('fonts/Lato-Reg.ttf');}\n";
                fontcode += "@font-face  {font-family: 'latoregitalic'; src: url('fonts/Lato-RegIta.ttf');}\n";
                fontcode += ".lyric      {font-family: 'latoreg'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: 'latoregitalic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            case 9:
                // league gothic
                fontcode += "@font-face  {font-family: 'leaguegothic'; src: url('fonts/LeagueGothic-Regular.otf');}\n";
                fontcode += "@font-face  {font-family: 'leaguegothicitalic'; src: url('fonts/LeagueGothic-Italic.otf');}\n";
                fontcode += ".lyric      {font-family: 'leaguegothic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: 'leaguegothicitalic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            case 10:
                // roboto light
                fontcode += "@font-face  {font-family: 'robotolight'; src: url('fonts/Roboto-Light.ttf');}\n";
                fontcode += "@font-face  {font-family: 'robotolightitalic'; src: url('fonts/Roboto-LightItalic.ttf');}\n";
                fontcode += ".lyric      {font-family: 'robotolight'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: 'robotolightitalic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            case 11:
                // roboto thin
                fontcode += "@font-face  {font-family: 'robotothin'; src: url('fonts/Roboto-Thin.ttf');}\n";
                fontcode += "@font-face  {font-family: 'robotothinitalic'; src: url('fonts/Roboto-ThinItalic.ttf');}\n";
                fontcode += ".lyric      {font-family: 'robotothin'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: 'robotothinitalic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            case 12:
                // roboto medium
                fontcode += "@font-face  {font-family: 'robotomedium'; src: url('fonts/Roboto-Medium.ttf');}\n";
                fontcode += "@font-face  {font-family: 'robotomediumitalic'; src: url('fonts/Roboto-MediumItalic.ttf');}\n";
                fontcode += ".lyric      {font-family: 'robotomedium'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: 'robotomediumitalic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            default:
                // lato light
                fontcode += "@font-face  {font-family: 'latolight'; src: url('fonts/Lato-Lig.ttf');}\n";
                fontcode += "@font-face  {font-family: 'latolightitalic'; src: url('fonts/Lato-LigIta.ttf');}\n";
                fontcode += ".lyric      {font-family: 'latolight'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: 'latolightitalic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
        }

        return fontcode;
    }

    static String setupWebViewChordFont(int fontnum) {

        String fontcode = "";
        boolean already = false;
        if (FullscreenActivity.mylyricsfontnum==FullscreenActivity.mychordsfontnum) {
            already = true;
        }

        switch (fontnum) {
            case 1:
                // Monospace
                fontcode += ".chord      {font-family: Monospace; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            case 2:
                // sans-serif
                fontcode += ".chord      {font-family: Sans-serif; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            case 3:
                // serif
                fontcode += ".chord      {font-family: Serif; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            case 4:
                // firasans light
                if (!already) {
                    fontcode += "@font-face  {font-family: 'firasanslight'; src: url('fonts/FiraSansOT-Light.otf');}\n";
                }
                fontcode += ".chord      {font-family: 'firasanslight'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            case 5:
                // firasans regular
                if (!already) {
                    fontcode += "@font-face  {font-family: 'firasans'; src: url('fonts/FiraSansOT-Regular.otf');}\n";
                }
                fontcode += ".chord      {font-family: 'firasans'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            case 6:
                // kaushan
                if (!already) {
                    fontcode += "@font-face  {font-family: 'kaushan'; src: url('fonts/KaushanScript-Regular.otf');}\n";
                }
                fontcode += ".chord      {font-family: 'kaushan'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            case 7:
                // lato light
                if (!already) {
                    fontcode += "@font-face  {font-family: 'latolight'; src: url('fonts/Lato-Lig.ttf');}\n";
                }
                fontcode += ".chord      {font-family: 'latolight'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            case 8:
                // lato regular
                if (!already) {
                    fontcode += "@font-face  {font-family: 'latoreg'; src: url('fonts/Lato-Reg.ttf');}\n";
                }
                fontcode += ".chord      {font-family: 'latoreg'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            case 9:
                // league gothic
                if (!already) {
                    fontcode += "@font-face  {font-family: 'leaguegothic'; src: url('fonts/LeagueGothic-Regular.otf');}\n";
                }
                fontcode += ".chord      {font-family: 'leaguegothic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            case 10:
                // roboto light
                if (!already) {
                    fontcode += "@font-face  {font-family: 'robotolight'; src: url('fonts/Roboto-Light.ttf');}\n";
                }
                fontcode += ".chord      {font-family: 'robotolight'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            case 11:
                // roboto thin
                if (!already) {
                    fontcode += "@font-face  {font-family: 'robotothin'; src: url('fonts/Roboto-Thin.ttf');}\n";
                }
                fontcode += ".chord      {font-family: 'robotothin'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            case 12:
                // roboto medium
                if (!already) {
                    fontcode += "@font-face  {font-family: 'robotomedium'; src: url('fonts/Roboto-Medium.ttf');}\n";
                }
                fontcode += ".chord      {font-family: 'robotomedium'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            default:
                // lato light
                if (!already) {
                    fontcode += "@font-face  {font-family: 'latolight'; src: url('fonts/Lato-Lig.ttf');}\n";
                }
                fontcode += ".chord      {font-family: 'latolight'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
        }

        return fontcode;
    }

}
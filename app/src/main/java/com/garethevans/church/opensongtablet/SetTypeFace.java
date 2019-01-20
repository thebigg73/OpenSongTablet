package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.provider.FontRequest;
import android.support.v4.provider.FontsContractCompat;
import android.support.v4.provider.FontsContractCompat.FontRequestCallback;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

class SetTypeFace {

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

    void setUpAppFonts(Context c, Preferences preferences, Handler lyrichandler, Handler chordhandler,
                       Handler presohandler, Handler presoinfohandler, Handler customhandler, Handler monohandler) {
        // Load up the user preferences
        String fontLyric = preferences.getMyPreferenceString(c, "fontLyric", "Lato");
        String fontChord = preferences.getMyPreferenceString(c, "fontChord", "Lato");
        String fontPreso = preferences.getMyPreferenceString(c, "fontPreso", "Lato");
        String fontPresoInfo = preferences.getMyPreferenceString(c, "fontPresoInfo", "Lato");
        String fontCustom = preferences.getMyPreferenceString(c, "fontCustom", "Lato");

        // Set the values to the custom font if they are set to Custom...
        fontLyric = fixFont(c, fontLyric, fontCustom);
        fontChord = fixFont(c, fontChord, fontCustom);
        fontPreso = fixFont(c, fontPreso, fontCustom);
        fontPresoInfo = fixFont(c, fontPresoInfo, fontCustom);

        // Set the values
        setChosenFont(c, preferences, fontLyric, "lyric", null, null, lyrichandler);
        setChosenFont(c, preferences, fontChord, "chord", null, null, chordhandler);
        setChosenFont(c, preferences, fontPreso, "preso", null, null, presohandler);
        setChosenFont(c, preferences, fontPresoInfo, "presoinfo", null, null, presoinfohandler);
        setChosenFont(c, preferences, fontCustom, "custom", null, null, customhandler);

        // The monospace font for tab
        setChosenFont(c, preferences, "Fira Mono", "mono", null, null, monohandler);
    }

    private String fixFont(Context c, String which, String custom) {
        if (which.equals(c.getString(R.string.custom) + "...")) {
            return custom;
        } else {
            return which;
        }
    }

    private FontRequest getFontRequest(String fontnamechosen) {
        return new FontRequest("com.google.android.gms.fonts",
                "com.google.android.gms", fontnamechosen,
                R.array.com_google_android_gms_fonts_certs);
    }

    private FontRequestCallback getFontRequestCallback(final Context c, final Preferences preferences, final String what,
                                                       final String fontname,
                                                       final TextView textView, final ProgressBar progressBar) {
        return new FontRequestCallback() {
            @Override
            public void onTypefaceRetrieved(Typeface typeface) {
                switch (what) {
                    case "mono":
                        FullscreenActivity.monofont = typeface;
                        break;
                    case "lyric":
                        FullscreenActivity.lyricsfont = typeface;
                        preferences.setMyPreferenceString(c, "fontLyric", fontname);
                        break;
                    case "chord":
                        FullscreenActivity.chordsfont = typeface;
                        preferences.setMyPreferenceString(c, "fontChord", fontname);
                        break;
                    case "preso":
                        FullscreenActivity.presofont = typeface;
                        preferences.setMyPreferenceString(c, "fontPreso", fontname);
                        break;
                    case "presoinfo":
                        FullscreenActivity.presoInfoFont = typeface;
                        preferences.setMyPreferenceString(c, "fontPresoInfo", fontname);
                        break;
                    case "custom":
                        FullscreenActivity.customfont = typeface;
                        preferences.setMyPreferenceString(c, "fontCustom", fontname);
                        break;
                }
                // If we are previewing the font, update the text and hide the progressBar (these will be null otherwise)
                if (textView != null) {
                    textView.setTypeface(typeface);
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTypefaceRequestFailed(int reason) {
                // Your code to deal with the failure goes here
                Typeface typeface = Typeface.createFromAsset(c.getAssets(), "font/lato.ttf");
                FullscreenActivity.myToastMessage = fontname + ": " + c.getString(R.string.file_type_unknown);
                ShowToast.showToast(c);

                switch (what) {
                    case "lyric":
                        FullscreenActivity.lyricsfont = typeface;
                        preferences.setMyPreferenceString(c, "fontLyric", "Lato");
                        break;
                    case "chord":
                        FullscreenActivity.chordsfont = typeface;
                        preferences.setMyPreferenceString(c, "fontChord", "Lato");
                        break;
                    case "preso":
                        FullscreenActivity.presofont = typeface;
                        preferences.setMyPreferenceString(c, "fontPreso", "Lato");
                        break;
                    case "presoinfo":
                        FullscreenActivity.presoInfoFont = typeface;
                        preferences.setMyPreferenceString(c, "fontPresoInfo", "Lato");
                        break;
                    case "custom":
                        FullscreenActivity.customfont = typeface;
                        preferences.setMyPreferenceString(c, "fontCustom", "Lato");
                        break;
                }

                if (textView != null) {
                    textView.setTypeface(FullscreenActivity.customfont);
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        };
    }

    void setChosenFont(final Context c, final Preferences preferences, String fontnamechosen, String which,
                       final TextView textView, final ProgressBar progressBar, Handler handler) {
        FontRequest fontRequest = getFontRequest(fontnamechosen);
        FontRequestCallback fontRequestCallback = getFontRequestCallback(c, preferences, which,
                fontnamechosen, textView, progressBar);
        FontsContractCompat.requestFont(c, fontRequest, fontRequestCallback, handler);
    }

    ArrayList<String> googleFontsAllowed(Context c) {
        ArrayList<String> f = new ArrayList<>();
        f.add("Abel");
        f.add("Actor");
        f.add("Amiri");
        f.add("Assistant");
        f.add("Atma");
        f.add("Baloo");
        f.add("Basic");
        f.add("Comfortaa");
        f.add("Encode Sans");
        f.add("Fira Mono");
        f.add("Gidugu");
        f.add("Gloria Hallelujah");
        f.add("Gochi Hand");
        f.add("Gurajada");
        f.add("Hind Madurai");
        f.add("Imprima");
        f.add("Lato");
        f.add("Noto Sans");
        f.add("Paprika");
        f.add("Roboto");
        f.add("Roboto Mono");
        f.add("Ubuntu");
        f.add("Ubuntu Mono");
        f.add(c.getString(R.string.custom) + "...");

        return f;
    }

}
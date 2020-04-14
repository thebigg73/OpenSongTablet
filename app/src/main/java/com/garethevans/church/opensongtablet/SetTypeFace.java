package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import androidx.core.provider.FontRequest;
import androidx.core.provider.FontsContractCompat;
import androidx.core.provider.FontsContractCompat.FontRequestCallback;

import android.os.Looper;
import android.widget.TextView;

import java.util.ArrayList;

public class SetTypeFace {


/*
    // Not using this, but keep kust on case
    static String setupWebViewLyricFont(Context c, StorageAccess storageAccess, Preferences preferences, int lyricsTextColor,) {

        String fontcode = ".lyric      {font-family:"+preferences.getMyPreferenceString(c,"fontLyric","Lato")+"; color:" +
                String.format("#%06X", (0xFFFFFF & lyricsTextColor)) + "; padding: 0px; text-size:12.0pt;}\n";


        fontcode += ".comment   {font-family:"+preferences.getMyPreferenceString(c,"fontLyric","Lato")+"; color:" +
                String.format("#%06X", (0xFFFFFF & lyricsTextColor)) + "; padding: 0px; text-size:12.0pt;}\n";
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
                fontcode += "@font-face  {font-family: 'latolight'; src: url('fonts/lato-Lig.ttf');}\n";
                fontcode += "@font-face  {font-family: 'latolightitalic'; src: url('fonts/lato-LigIta.ttf');}\n";
                fontcode += ".lyric      {font-family: 'latolight'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: 'latolightitalic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
            case 8:
                // lato regular
                fontcode += "@font-face  {font-family: 'latoreg'; src: url('fonts/lato-Reg.ttf');}\n";
                fontcode += "@font-face  {font-family: 'latoregitalic'; src: url('fonts/lato-RegIta.ttf');}\n";
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
                fontcode += "@font-face  {font-family: 'latolight'; src: url('fonts/lato-Lig.ttf');}\n";
                fontcode += "@font-face  {font-family: 'latolightitalic'; src: url('fonts/lato-LigIta.ttf');}\n";
                fontcode += ".lyric      {font-family: 'latolight'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                fontcode += ".comment    {font-family: 'latolightitalic'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
        }


        return fontcode;
    }
*/

    // Not used, but keeping just in case!
/*
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
                    fontcode += "@font-face  {font-family: 'latolight'; src: url('fonts/lato-Lig.ttf');}\n";
                }
                fontcode += ".chord      {font-family: 'latolight'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsChordsColor)) + "; padding: 0px;}\n";
                break;
            case 8:
                // lato regular
                if (!already) {
                    fontcode += "@font-face  {font-family: 'latoreg'; src: url('fonts/lato-Reg.ttf');}\n";
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
                    fontcode += "@font-face  {font-family: 'latolight'; src: url('fonts/lato-Lig.ttf');}\n";
                }
                fontcode += ".chord      {font-family: 'latolight'; color:" + String.format("#%06X", (0xFFFFFF & FullscreenActivity.lyricsTextColor)) + "; padding: 0px;}\n";
                break;
        }

        return fontcode;
    }

*/

    public void setUpAppFonts(Context c, Preferences preferences, Handler lyrichandler, Handler chordhandler, Handler stickyHandler,
                              Handler presohandler, Handler presoinfohandler, Handler customhandler) {
        // Load up the user preferences
        String fontLyric = preferences.getMyPreferenceString(c, "fontLyric", "Lato");
        String fontChord = preferences.getMyPreferenceString(c, "fontChord", "Lato");
        String fontSticky = preferences.getMyPreferenceString(c, "fontSticky", "Lato");
        String fontPreso = preferences.getMyPreferenceString(c, "fontPreso", "Lato");
        String fontPresoInfo = preferences.getMyPreferenceString(c, "fontPresoInfo", "Lato");
        String fontCustom = preferences.getMyPreferenceString(c, "fontCustom", "Lato");

        // Set the values  (if Lato, use the bundled font
        // The reason is that KiKat devices don't load the Google Font resource automatically (it requires manually selecting it).
        if (fontLyric.equals("Lato")) {
            StaticVariables.typefaceLyrics = Typeface.createFromAsset(c.getAssets(),"font/lato.ttf");
        } else {
            setChosenFont(c, preferences, fontLyric, "lyric", null, lyrichandler);
        }
        if (fontChord.equals("Lato")) {
            StaticVariables.typefaceChords = Typeface.createFromAsset(c.getAssets(),"font/lato.ttf");
        } else {
            setChosenFont(c, preferences, fontChord, "chord", null, chordhandler);
        }
        if (fontSticky.equals("Lato")) {
            StaticVariables.typefaceSticky = Typeface.createFromAsset(c.getAssets(), "font/lato.ttf");
        } else {
            setChosenFont(c, preferences, fontSticky, "sticky", null, stickyHandler);
        }
        if (fontPreso.equals("Lato")) {
            StaticVariables.typefacePreso = Typeface.createFromAsset(c.getAssets(),"font/lato.ttf");
        } else {
            setChosenFont(c, preferences, fontPreso, "preso", null, presohandler);
        }
        if (fontPresoInfo.equals("Lato")) {
            StaticVariables.typefacePresoInfo = Typeface.createFromAsset(c.getAssets(),"font/lato.ttf");
        } else {
            setChosenFont(c, preferences, fontPresoInfo, "presoinfo", null, presoinfohandler);
        }
        if (fontCustom.equals("Lato")) {
            StaticVariables.typefaceCustom = Typeface.createFromAsset(c.getAssets(),"font/lato.ttf");
        } else {
            setChosenFont(c, preferences, fontCustom, "custom", null, customhandler);
        }

        StaticVariables.typefaceMono = Typeface.MONOSPACE;

    }


    private FontRequest getFontRequest(String fontnamechosen) {
        return new FontRequest("com.google.android.gms.fonts",
                "com.google.android.gms", fontnamechosen,
                R.array.com_google_android_gms_fonts_certs);
    }

    private FontRequestCallback getFontRequestCallback(final Context c, final Preferences preferences, final String what,
                                                       final String fontname,
                                                       final TextView textView) {
        return new FontRequestCallback() {
            @Override
            public void onTypefaceRetrieved(Typeface typeface) {
                switch (what) {
                    case "mono":
                        StaticVariables.typefaceMono = typeface;
                        break;
                    case "lyric":
                        StaticVariables.typefaceLyrics = typeface;
                        preferences.setMyPreferenceString(c, "fontLyric", fontname);
                        break;
                    case "chord":
                        StaticVariables.typefaceChords = typeface;
                        preferences.setMyPreferenceString(c, "fontChord", fontname);
                        break;
                    case "sticky":
                        StaticVariables.typefaceSticky = typeface;
                        preferences.setMyPreferenceString(c, "fontSticky", fontname);
                        break;
                    case "preso":
                        StaticVariables.typefacePreso = typeface;
                        preferences.setMyPreferenceString(c, "fontPreso", fontname);
                        break;
                    case "presoinfo":
                        StaticVariables.typefacePresoInfo = typeface;
                        preferences.setMyPreferenceString(c, "fontPresoInfo", fontname);
                        break;
                    case "custom":
                        StaticVariables.typefaceCustom = typeface;
                        preferences.setMyPreferenceString(c, "fontCustom", fontname);
                        break;
                }
                // If we are previewing the font, update the text and hide the progressBar (these will be null otherwise)
                if (textView != null) {
                    textView.setTypeface(typeface);
                }
            }

            @Override
            public void onTypefaceRequestFailed(int reason) {
                // Your code to deal with the failure goes here
                Typeface typeface;
                if (what.equals("mono")) {
                    typeface = Typeface.MONOSPACE;
                } else {
                    typeface = Typeface.createFromAsset(c.getAssets(), "font/lato.ttf");
                }
                StaticVariables.myToastMessage = fontname + ": " + c.getString(R.string.error);
                ShowToast.showToast(c);

                switch (what) {
                    case "lyric":
                        StaticVariables.typefaceLyrics = typeface;
                        preferences.setMyPreferenceString(c, "fontLyric", "Lato");
                        break;
                    case "chord":
                        StaticVariables.typefaceChords = typeface;
                        preferences.setMyPreferenceString(c, "fontChord", "Lato");
                        break;
                    case "sticky":
                        StaticVariables.typefaceSticky = typeface;
                        preferences.setMyPreferenceString(c, "fontSticky", "Lato");
                        break;
                    case "preso":
                        StaticVariables.typefacePreso = typeface;
                        preferences.setMyPreferenceString(c, "fontPreso", "Lato");
                        break;
                    case "presoinfo":
                        StaticVariables.typefacePresoInfo = typeface;
                        preferences.setMyPreferenceString(c, "fontPresoInfo", "Lato");
                        break;
                    case "custom":
                        StaticVariables.typefaceCustom = typeface;
                        preferences.setMyPreferenceString(c, "fontCustom", "Lato");
                        break;
                }

                if (textView != null) {
                    textView.setTypeface(StaticVariables.typefaceCustom);
                }

            }
        };
    }

    void setChosenFont(final Context c, final Preferences preferences, String fontnamechosen, String which,
                       final TextView textView, Handler handler) {
        if (handler==null) {
            handler = new Handler();
        }
        FontRequest fontRequest = getFontRequest(fontnamechosen);
        FontRequestCallback fontRequestCallback = getFontRequestCallback(c, preferences, which,
                fontnamechosen, textView);
        FontsContractCompat.requestFont(c, fontRequest, fontRequestCallback, handler);
    }

    ArrayList<String> googleFontsAllowed() {
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

        return f;
    }

}
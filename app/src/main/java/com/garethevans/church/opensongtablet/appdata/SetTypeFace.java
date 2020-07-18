package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.widget.TextView;

import androidx.core.provider.FontRequest;
import androidx.core.provider.FontsContractCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;

import java.util.ArrayList;

public class SetTypeFace {

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

    private FontsContractCompat.FontRequestCallback getFontRequestCallback(final Context c, final Preferences preferences, final String what,
                                                                           final String fontname,
                                                                           final TextView textView) {
        return new FontsContractCompat.FontRequestCallback() {
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
                ShowToast.showToast(c,fontname + ": " + c.getString(R.string.error));

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
        FontsContractCompat.FontRequestCallback fontRequestCallback = getFontRequestCallback(c, preferences, which,
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

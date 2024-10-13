package com.garethevans.church.opensongtablet.abcnotation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.Locale;

public class ABCNotation {

    @SuppressWarnings("unused")
    private final String TAG = "ABCNotation";

    private String songTitle, songKey, songAbc, songTimeSig;
    private int songAbcTranspose, abcZoom;
    private float abcPopupWidth;
    private boolean isPopup, abcAutoTranspose, autoshowMusicScore;
    private final MainActivityInterface mainActivityInterface;

    public ABCNotation(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        getUpdatedPreferences();
    }

    // If we change load in a profile, this is called
    public void getUpdatedPreferences() {
        abcAutoTranspose = mainActivityInterface.getPreferences().getMyPreferenceBoolean("abcAutoTranspose",true);
        abcPopupWidth = mainActivityInterface.getPreferences().getMyPreferenceFloat("abcPopupWidth",abcPopupWidth);
        abcZoom = mainActivityInterface.getPreferences().getMyPreferenceInt("abcZoom",2);
        autoshowMusicScore = mainActivityInterface.getPreferences().getMyPreferenceBoolean("autoshowMusicScore",false);
    }

    // This is set when a song is set for editing or displaying the Abc notation
    public void prepareSongValues(Song thisSong, boolean isPopup) {
        this.isPopup = isPopup;
        songTitle = thisSong.getTitle();
        songKey = thisSong.getKey();
        songAbc = thisSong.getAbc();
        String abcTranspose = thisSong.getAbcTranspose();
        if (abcTranspose==null || abcTranspose.isEmpty()) {
            songAbcTranspose = 0;
        } else {
            try {
                songAbcTranspose = Integer.parseInt(abcTranspose);
            } catch (Exception e) {
                songAbcTranspose = 0;
            }
        }
        songTimeSig = thisSong.getTimesig();
        // Check for default abcText
        getSongAbcOrDefault();

        // If we are autotransposing and have keys set, get the transpose value
        getABCTransposeFromSongKey();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void setWebView(WebView webView) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.getSettings().getJavaScriptEnabled();
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setInitialScale(1);
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setLoadWithOverviewMode(true);
                webView.getSettings().setUseWideViewPort(true);
                webView.getSettings().setSupportZoom(true);
                webView.getSettings().setBuiltInZoomControls(true);
                webView.getSettings().setDisplayZoomControls(false);
                webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
                webView.setScrollbarFadingEnabled(false);
                webView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                        return super.onConsoleMessage(consoleMessage);
                    }
                });
                webView.setWebViewClient(new MyWebViewClient() {});
                webView.loadUrl("file:///android_asset/ABC/abc.html");
            }
        });
    }

    // Listener for the webview drawing the abc score
    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
            updateContent(webView);

            // Set to view only
            webView.loadUrl("javascript:displayOnly();");

            // We could have a split view/edit, but prefer just viewing in html
            // webView.loadUrl("javascript:displayAndEdit();");
        }
    }

    // Return the songAbc
    public void getSongAbcOrDefault() {
        // This is used to add in default song settings for various Abc properties
        // Only called if songAbc is empty/null
        if (songAbc==null || songAbc.isEmpty()) {

            // Add a title
            songAbc = "T:" + songTitle + "\n";

            // Add the song time signature
            if (songTimeSig==null || songTimeSig.isEmpty()) {
                songAbc += "M:4/4\n";
            } else {
                songAbc += "M:" + songTimeSig + "\n";
            }

            // Add the note length
            songAbc += "L:1/8\n";

            // Add the song key
            if (songKey==null || songKey.isEmpty()) {
                songAbc += "K:C treble %treble or bass clef\n";
            } else {
                songAbc += "K: " + songKey + " %treble or bass clef\n";
            }

            // Add the first measure
            songAbc += "|";
        }
    }


    // Save the edits to the song XML file (and in the database)
    public void saveAbcContent(MainActivityInterface mainActivityInterface, Song thisSong) {
        thisSong.setAbc(songAbc);
        thisSong.setAbcTranspose(String.valueOf(songAbcTranspose));
        mainActivityInterface.getSaveSong().updateSong(thisSong, false);
    }



    // Update the output display for the abc score
    private void updateContent(WebView webView) {
        String newContent = songAbc;
        try {
            // Encode the abc text for passing to the webview
            newContent = Uri.encode(songAbc,"UTF-8");
        } catch  (Exception e) {
            e.printStackTrace();
        }

        webView.loadUrl("javascript:setTranspose("+songAbcTranspose+");");

        if (isPopup) {
            webView.loadUrl("javascript:setWidth("+(int)(mainActivityInterface.getDisplayMetrics()[0] *
                            mainActivityInterface.getPreferences().getMyPreferenceFloat("abcPopupWidth",0.95f))+","+
                    mainActivityInterface.getPreferences().getMyPreferenceInt("abcZoom",2)+");");
        } else {
            webView.loadUrl("javascript:setWidth("+mainActivityInterface.getDisplayMetrics()[0]+","+
                    mainActivityInterface.getPreferences().getMyPreferenceInt("abcZoom",2)+");");
        }

        webView.evaluateJavascript("javascript:updateABC('"+newContent+"');",null);
        webView.loadUrl("javascript:setTranspose(" + songAbcTranspose + ");");
        webView.loadUrl("javascript:initEditor()");
    }

    public void updateZoom(WebView webView) {
        webView.post(() -> {
            webView.loadUrl("javascript:setZoom("+abcZoom+");");
            webView.loadUrl("javascript:initEditor()");
        });
    }

    // Using the song key and abc key, decide an automatic transpose value
    public void getABCTransposeFromSongKey() {
        // This gets the key from existing abc notation (if set)
        // We then compare to the actual song key (if set)
        // If they are different, set the transpose value, if requested
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("abcAutoTranspose", true)) {
            String[] abcLines = songAbc.split("\n");
            String abcKey = "";
            for (String abcLine : abcLines) {
                if (abcLine.startsWith("K:")) {
                    abcKey = keybits(abcLine);
                    if (abcKey != null) {
                        // This is the actual key line, so stop looking
                        // We can have multiple key lines (e.g. for clef, etc).
                        break;
                    }
                    // Otherwise, we continue looking!
                }
            }

            // If no key was set in the ABC, we assume 'C'
            if (abcKey==null || abcKey.isEmpty()) {
                abcKey = "C";
            }



            if (songKey!=null && !songKey.isEmpty() && !songKey.equals(abcKey)) {
                songAbcTranspose = mainActivityInterface.getTranspose().getTransposeTimes(
                        abcKey, songKey);

                // Go for the smallest transpose change, e.g. 10 -> -2
                if (songAbcTranspose > 6) {
                    songAbcTranspose = songAbcTranspose - 12;
                }
            } else {
                songAbcTranspose = 0;
            }
        }
    }


    // This is used to gradually strip out extra info from the key line
    private String keybits(String keyline) {
        // Key lines can have many bits of extra information!!!
        // Because keylines are case insensitive, make everything lowercase
        keyline = keyline.toLowerCase(Locale.ROOT);

        keyline = keyline.replace("minor","m");
        // If the key contains certain bits, we only need stuff up to this point
        // The key should always be first K: <tone>
        keyline = substringUpTo(keyline,"maj"); // Major
        keyline = substringUpTo(keyline,"ion"); // Ionian
        keyline = substringUpTo(keyline,"mix"); // Mixolydian
        keyline = substringUpTo(keyline,"aeo"); // Aeolian
        keyline = substringUpTo(keyline,"dor"); // Dorian
        keyline = substringUpTo(keyline,"phr"); // Phrygian
        keyline = substringUpTo(keyline,"lyd"); // Lydian
        keyline = substringUpTo(keyline,"loc"); // Locrian
        keyline = substringUpTo(keyline,"clef"); // Clef
        keyline = substringUpTo(keyline,"bass"); // Bass
        keyline = substringUpTo(keyline,"bass2"); // Bass
        keyline = substringUpTo(keyline,"bass3"); // Baritone
        keyline = substringUpTo(keyline,"tenor"); // Tenor
        keyline = substringUpTo(keyline,"treble"); // Treble
        keyline = substringUpTo(keyline,"alto"); // Alto
        keyline = substringUpTo(keyline,"alto1"); // Soprano
        keyline = substringUpTo(keyline,"alto2"); // Mezzosoprano
        keyline = substringUpTo(keyline,"exp"); // Explicit
        keyline = substringUpTo(keyline,"__"); // Accidental
        keyline = substringUpTo(keyline,"_"); // Accidental
        keyline = substringUpTo(keyline,"="); // Accidental
        keyline = substringUpTo(keyline,"^"); // Accidental
        keyline = substringUpTo(keyline,"^^"); // Accidental
        keyline = substringUpTo(keyline,"perc"); // Percussion
        keyline = substringUpTo(keyline,"staff"); // Staff lines
        keyline = substringUpTo(keyline,"mid"); // Middle note
        keyline = substringUpTo(keyline,"tran"); // Transpose
        keyline = substringUpTo(keyline,"oct"); // Octave
        keyline = substringUpTo(keyline, "%"); // Comment

        if (keyline.contains("minor")) {
            keyline = substringUpTo(keyline,"minor") + "m";
        }

        keyline = keyline.replace("k:","");
        keyline = keyline.replace(" ","");

        // Now compare with known keys
        // Go through in order to look for minors, sharps, flats, naturals in that order
        String[] keysToCheck = new String[] {"A#m","C#m","D#m","F#m","G#m",
                "Abm","Bbm","Dbm","Ebm","Gbm",
                "Am","Bm","Cm","Dm","Em","Fm","Gm",
                "A#","C#","D#","F#","G#",
                "Ab","Bb","Db","Eb","Gb",
                "A","B","C","D","E","F","G"};

        String foundKey = null;
        for (String s : keysToCheck) {
            if (keyline.startsWith(s.toLowerCase())) {
                foundKey = s;
                break;
            }
        }
        return foundKey;
    }

    // Gradually strip out useful bits of the set key
    private String substringUpTo(String string, String lookfor) {
        if (string.contains(lookfor)) {
            string = string.substring(0,string.indexOf(lookfor));
        }
        return string;
    }


    // Tell the webview to update
    public void updateWebView(WebView webView) {
        updateContent(webView);
    }

    // The getters
    public String getSongAbc() {
        return songAbc;
    }
    public int getSongAbcTranspose() {
        return songAbcTranspose;
    }
    public boolean getAbcAutoTranspose() {
        return abcAutoTranspose;
    }
    public float getAbcPopupWidth() {
        return abcPopupWidth;
    }
    public int getAbcZoom() {
        return abcZoom;
    }
    public boolean getAutoshowMusicScore() {
        return autoshowMusicScore;
    }

    // The setters
    // Update the string value for the songAbc (due to editing it in the MusicScoreFragment)
    // This doesn't save to the song, but updates the display
    public void setSongAbc(String songAbc) {
        this.songAbc = songAbc;
    }
    public void setSongAbcTranspose(int songAbcTranspose) {
        this.songAbcTranspose = songAbcTranspose;
    }
    public void setAbcAutoTranspose(boolean abcAutoTranspose) {
        this.abcAutoTranspose = abcAutoTranspose;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("abcAutoTranspose",abcAutoTranspose);
    }
    public void setAbcPopupWidth(float abcPopupWidth) {
        this.abcPopupWidth = abcPopupWidth;
        mainActivityInterface.getPreferences().setMyPreferenceFloat("abcPopupWidth",abcPopupWidth);
    }
    public void setAbcZoom(int abcZoom) {
        this.abcZoom = abcZoom;
        mainActivityInterface.getPreferences().setMyPreferenceInt("abcZoom",abcZoom);
    }
    public void setAutoshowMusicScore(boolean autoshowMusicScore) {
        this.autoshowMusicScore = autoshowMusicScore;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("autoshowMusicScore",autoshowMusicScore);
    }
}

package com.garethevans.church.opensongtablet.abcnotation;

import android.content.Context;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.InlineAbcWebView;
import com.garethevans.church.opensongtablet.drummer.DrumCalculations;
import com.garethevans.church.opensongtablet.export.ExportFragment;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;
import java.util.Locale;

public class ABCNotation {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ABCNotation";

    private String songTitle, songKey, songAbc, songTimeSig, abcInstrumentTab;
    private int songAbcTranspose;
    private float abcPopupWidth=0.5f;
    private boolean abcAutoTranspose;
    private boolean autoshowMusicScore;
    private boolean abcIncludeTab;
    private boolean abcWebViewsDrawn = false;
    private final MainActivityInterface mainActivityInterface;
    private final String guitar, guitar_drop_d, guitar_open_g, banjo4, banjo5, mandolin, cavaquinho, ukulele, bass4, bass5, violin, fiddle;
    private int abcInlineWidth;
    @SuppressWarnings("FieldCanBeLocal")
    private final String inlineAbcLineIndicator = ";#:";
    private final Context c;
    private ArrayList<InlineAbcObject> inlineAbcObjects = new ArrayList<>();
    private int secondaryInlineAbcObjectPosition = 0;
    private ABCPopup abcPopup;
    private ExportFragment exportFragment;

    public ABCNotation(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        guitar = c.getString(R.string.guitar);
        guitar_drop_d = c.getString(R.string.guitar_drop_d);
        guitar_open_g = c.getString(R.string.guitar_open_g);
        banjo4 = c.getString(R.string.banjo4);
        banjo5 = c.getString(R.string.banjo5);
        mandolin = c.getString(R.string.mandolin);
        cavaquinho = c.getString(R.string.cavaquinho);
        ukulele = c.getString(R.string.ukulele);
        bass4 = c.getString(R.string.bass4);
        bass5 = c.getString(R.string.bass5);
        violin = c.getString(R.string.violin);
        fiddle = c.getString(R.string.fiddle);
        getUpdatedPreferences();
    }

    // If we change load in a profile, this is called
    public void getUpdatedPreferences() {
        abcAutoTranspose = mainActivityInterface.getPreferences().getMyPreferenceBoolean("abcAutoTranspose",true);
        abcPopupWidth = mainActivityInterface.getPreferences().getMyPreferenceFloat("abcPopupWidth",0.5f);
        autoshowMusicScore = mainActivityInterface.getPreferences().getMyPreferenceBoolean("autoshowMusicScore",false);
        abcIncludeTab = mainActivityInterface.getPreferences().getMyPreferenceBoolean("abcIncludeTab",false);
        abcInstrumentTab = mainActivityInterface.getPreferences().getMyPreferenceString("abcInstrumentTab","guitar");
        abcInlineWidth = mainActivityInterface.getPreferences().getMyPreferenceInt("abcInlineWidth",480);
    }

    // If we need to update the popup, we need a reference to it
    public void setAbcPopup(ABCPopup abcPopup) {
        this.abcPopup = abcPopup;
    }

    public void allowPopupToContinue(int width, int height) {
        if (abcPopup!=null) {
            abcPopup.setMeasured(width, height);
        }
    }
    // This is set when a song is set for editing or displaying the Abc notation
    public void prepareSongValues(Song thisSong) {
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
        // Prepare this song's values
        songTimeSig = DrumCalculations.getFixedTimeSignatureString(thisSong.getTimesig(),true);

        //songTimeSig = mainActivityInterface.getMetronome().fixInvalidTimeSignature(thisSong.getTimesig(),true);
        // Check for default abcText
        getSongAbcOrDefault();

        // If we are autotransposing and have keys set, get the transpose value
        getABCTransposeFromSongKey();
    }

    public void setWebView(InlineAbcWebView inlineAbcWebView) {
        inlineAbcWebView.post(new Runnable() {
            @Override
            public void run() {
                inlineAbcWebView.setInitialScale(1);
                inlineAbcWebView.getSettings().setDomStorageEnabled(true);
                inlineAbcWebView.getSettings().setLoadWithOverviewMode(true);
                inlineAbcWebView.getSettings().setUseWideViewPort(true);
                inlineAbcWebView.getSettings().setSupportZoom(true);
                inlineAbcWebView.getSettings().setBuiltInZoomControls(true);
                inlineAbcWebView.getSettings().setDisplayZoomControls(false);
                inlineAbcWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                inlineAbcWebView.setScrollbarFadingEnabled(false);
                inlineAbcWebView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                        return super.onConsoleMessage(consoleMessage);
                    }
                });
                inlineAbcWebView.loadUrl("file:///android_asset/ABC/abc.html");

            }
        });
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

    // Get the list of ABC tab instruments
    public String[] getABCInstruments() {
        return new String[]{guitar, guitar_drop_d, guitar_open_g, banjo4, banjo5, mandolin, cavaquinho, ukulele, bass4, bass5, violin, fiddle};
    }

    // Get the default tuning options for the instrument
    public String[] getAbcInstrumentTuning() {
        // Check we have the correct number of strings for our instrument
        // Convert our preference which is a single string with commas into an array

        switch (abcInstrumentTab) {
            case "guitar":
            default:
                return new String[] {"E,","A,","D","G","B","e"};
            case "guitardropd":
                return new String[] {"D,","A,","D","G","B","e"};
            case "guitaropeng":
                return new String[] {"D,","G,","D","G","B","d"};
            case "banjo4":
            case "cavaquinho":
                return new String[] {"D","G","B","d"};
            case "banjo5":
                return new String[] {"G,","D","G","B","d"};
            case "ukulele":
                return new String[] {"G","c","e","a"};
            case "mandolin":
            case "fiddle":
            case "violin":
                return new String[] {"G","d","a","e'"};
            case "bass4":
                return new String[] {"E,,","A,,","D,","G,"};
            case "bass5":
                return new String[] {"B,,,","E,,","A,,","D,","G,"};
        }
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
    public int getAbcPopupScreenWidth() {
        return Math.round(c.getResources().getDisplayMetrics().widthPixels * abcPopupWidth);
    }
    public boolean getAutoshowMusicScore() {
        return autoshowMusicScore;
    }
    public boolean getAbcIncludeTab() {
        return abcIncludeTab;
    }
    public String getAbcInstrumentNice() {
        // The preference is a non translated simple text string
        // Get a nice translated string from this
        switch (abcInstrumentTab) {
            case "guitar":
            default:
                return guitar;
            case "guitardropd":
                return guitar_drop_d;
            case "guitaropeng":
                return guitar_open_g;
            case "banjo4":
                return banjo4;
            case "banjo5":
                return banjo5;
            case "mandolin":
                return mandolin;
            case "cavaquinho":
                return cavaquinho;
            case "violin":
                return violin;
            case "fiddle":
                return fiddle;
            case "ukulele":
                return ukulele;
            case "bass4":
                return bass4;
            case "bass5":
                return bass5;
        }
    }
    public String getAbcInstrumentPrefFromNice(String niceInstrument) {
        // The user picks the instrument from a dropdown using translated strings
        // We need to get the preference version which is a lowercase text string
        if (niceInstrument.equals(banjo4)) {
            return "banjo4";
        } else if (niceInstrument.equals(banjo5)) {
            return "banjo5";
        } else if (niceInstrument.equals(mandolin)) {
            return "mandolin";
        } else if (niceInstrument.equals(cavaquinho)) {
            return "cavaquinho";
        } else if (niceInstrument.equals(violin)) {
            return "violin";
        } else if (niceInstrument.equals(fiddle)) {
            return "fiddle";
        } else if (niceInstrument.equals(ukulele)) {
            return "ukulele";
        } else if (niceInstrument.equals(bass4)) {
            return "bass4";
        } else if (niceInstrument.equals(bass5)) {
            return "bass5";
        } else if (niceInstrument.equals(guitar_drop_d)) {
            return "guitardropd";
        } else if (niceInstrument.equals(guitar_open_g)) {
            return "guitaropeng";
        } else {
            return guitar;
        }
    }

    public String getAbcIntrumentTabForABCJS() {
        // ABC notation only allows some instrument types
        // We fudge this by grouping them into available options
        switch (abcInstrumentTab) {
            case "guitar":
            case "guitardropd":
            case "guitaropeng":
            default:
                return "guitar";
            case "banjo4":
            case "bass4":
            case "cavaquinho":
            case "mandolin":
            case "ukulele":
                return "mandolin";
            case "violin":
                return "violin";
            case "fiddle":
                return "fiddle";
            case "banjo5":
            case "bass5":
                return "fiveString";
        }
    }
    public String getAbcInstrumentLabelABCJS() {
        // The instrument label uses the nice instrument and the tuning
        return getAbcInstrumentNice() + " (%T)";
    }
    public String[] getAbcInstrumentTuningABCJS() {
        String[] instrumentTuningArray = getAbcInstrumentTuning();
        String[] returnStrings = new String[7];
        if (instrumentTuningArray.length == 6) {
            returnStrings[6] = instrumentTuningArray[0];
            returnStrings[5] = instrumentTuningArray[1];
            returnStrings[4] = instrumentTuningArray[2];
            returnStrings[3] = instrumentTuningArray[3];
            returnStrings[2] = instrumentTuningArray[4];
            returnStrings[1] = instrumentTuningArray[5];
        } else if (instrumentTuningArray.length == 5) {
            returnStrings[6] = "";
            returnStrings[5] = instrumentTuningArray[0];
            returnStrings[4] = instrumentTuningArray[1];
            returnStrings[3] = instrumentTuningArray[2];
            returnStrings[2] = instrumentTuningArray[3];
            returnStrings[1] = instrumentTuningArray[4];
        } else {
            returnStrings[6] = "";
            returnStrings[5] = "";
            returnStrings[4] = instrumentTuningArray[0];
            returnStrings[3] = instrumentTuningArray[1];
            returnStrings[2] = instrumentTuningArray[2];
            returnStrings[1] = instrumentTuningArray[3];
        }
        return returnStrings;
    }
    public int getAbcInlineWidth() {
        return abcInlineWidth;
    }
    public String getInlineAbcLineIndicator() {
        return inlineAbcLineIndicator;
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
    public void setAutoshowMusicScore(boolean autoshowMusicScore) {
        this.autoshowMusicScore = autoshowMusicScore;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("autoshowMusicScore",autoshowMusicScore);
    }
    public void setAbcIncludeTab(boolean abcIncludeTab) {
        this.abcIncludeTab = abcIncludeTab;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("abcIncludeTab",abcIncludeTab);
    }
    public void setAbcInstrumentTab(String abcInstrumentTab) {
        this.abcInstrumentTab = abcInstrumentTab;
        mainActivityInterface.getPreferences().setMyPreferenceString("abcInstrumentTab",abcInstrumentTab);
    }
    public void setAbcInlineWidth(int abcInlineWidth) {
        this.abcInlineWidth = abcInlineWidth;
        mainActivityInterface.getPreferences().setMyPreferenceInt("abcInlineWidth",abcInlineWidth);
    }
    public void setPopupAbcReady(int width, int height) {
        if (abcPopup != null) {
            abcPopup.setMeasured(width,height);
        }
    }

    public void resetInlineAbcObjects() {
        inlineAbcObjects = new ArrayList<>();
    }

    public int countInlineAbcObjects() {
        return inlineAbcObjects.size();
    }

    public void addInlineAbcObject(InlineAbcObject inlineAbcObject) {
        int item = countInlineAbcObjects();
        inlineAbcObject.setAbcItem(item);
        inlineAbcObjects.add(inlineAbcObject);
    }

    public ArrayList<InlineAbcObject> getInlineAbcObjects() {
        return inlineAbcObjects;
    }

    public void setAbcWebViewsDrawn(boolean abcWebViewsDrawn) {
        this.abcWebViewsDrawn = abcWebViewsDrawn;
    }

    public boolean getAbcWebViewsDrawn() {
        return abcWebViewsDrawn;
    }

    public void setSecondaryInlineAbcObjectPosition(int secondaryInlineAbcObjectPosition) {
        this.secondaryInlineAbcObjectPosition = secondaryInlineAbcObjectPosition;
    }
    public int getSecondaryInlineAbcObjectPosition() {
        return secondaryInlineAbcObjectPosition;
    }
    public void increaseSecondaryInlineAbcObjectPosition() {
        secondaryInlineAbcObjectPosition ++;
    }

    public void setExportFragment(ExportFragment exportFragment) {
        this.exportFragment = exportFragment;
    }

    public ExportFragment getExportFragment() {
        return exportFragment;
    }
}

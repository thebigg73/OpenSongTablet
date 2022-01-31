package com.garethevans.church.opensongtablet.performance;

// The gestures used in the app

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.abcnotation.MusicScoreBottomSheet;
import com.garethevans.church.opensongtablet.autoscroll.AutoscrollBottomSheet;
import com.garethevans.church.opensongtablet.chords.ChordFingeringBottomSheet;
import com.garethevans.church.opensongtablet.chords.TransposeBottomSheet;
import com.garethevans.church.opensongtablet.customviews.MyZoomLayout;
import com.garethevans.church.opensongtablet.interfaces.ActionInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.midi.MidiSongBottomSheet;
import com.garethevans.church.opensongtablet.pads.PadsBottomSheet;
import com.garethevans.church.opensongtablet.pdf.PDFPageBottomSheet;
import com.garethevans.church.opensongtablet.songmenu.RandomSongBottomSheet;
import com.garethevans.church.opensongtablet.tools.SoundLevelBottomSheet;

public class PerformanceGestures {

    private final String TAG = "PerformanceGestures";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final ActionInterface actionInterface;
    private MyZoomLayout myZoomLayout;
    private RecyclerView pdfRecycler;

    // Initialise
    public PerformanceGestures(Context c, MainActivityInterface mainActivityInterface) {
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
        actionInterface = (ActionInterface) c;
    }
    public void setZoomLayout(MyZoomLayout myZoomLayout) {
        this.myZoomLayout = myZoomLayout;
    }
    public void setPDFRecycler(RecyclerView pdfRecycler) {
        this.pdfRecycler = pdfRecycler;
    }

    // The following are called from GestureListener, PedalActions, PageButtons

    // Edit page buttons
    public void editPageButtons() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/controls/pagebuttons",0);
    }

    // Song menu
    public void songMenu() {
        mainActivityInterface.chooseMenu(false);
    }

    // Set menu
    public void setMenu() {
        mainActivityInterface.chooseMenu(true);
    }

    // Edit song
    public void editSong() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/edit",0);
    }

    // Add to set
    public void addToSet() {
        String itemForSet = mainActivityInterface.getSetActions().whatToLookFor(mainActivityInterface.getSong());

        Log.d(TAG,"itemForSet="+itemForSet);
        // Allow the song to be added, even if it is already there
        String val = mainActivityInterface.getPreferences().getMyPreferenceString(c,"setCurrent","") + itemForSet;
        mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrent",val);

        // Tell the user that the song has been added.
        mainActivityInterface.getShowToast().doIt("\"" + mainActivityInterface.getSong().getFilename() + "\" " +
                c.getString(R.string.added_to_set));

        // Vibrate to let the user know something happened
        mainActivityInterface.getDoVibrate().vibrate(c, 50);

        mainActivityInterface.getCurrentSet().addSetItem(itemForSet);
        mainActivityInterface.getCurrentSet().addSetValues(mainActivityInterface.getSong().getFolder(),
                mainActivityInterface.getSong().getFilename(), mainActivityInterface.getSong().getKey());
        mainActivityInterface.updateSetList();
    }

    // Redraw the lyrics page
    public void loadSong() {
        mainActivityInterface.doSongLoad(mainActivityInterface.getSong().getFolder(),mainActivityInterface.getSong().getFilename(),true);
    }

    // Stop or start autoscroll
    public void toggleAutoscroll() {
        mainActivityInterface.toggleAutoscroll();
    }

    // Autoscroll settings
    public void autoscrollSettings() {
        AutoscrollBottomSheet autoscrollBottomSheet = new AutoscrollBottomSheet();
        autoscrollBottomSheet.show(actionInterface.getMyFragmentManager(),"AutoscrollBottomSheet");
    }

    // Stop or start pads
    public void togglePad() {
        mainActivityInterface.playPad();
    }

    // Show the pad bottom sheet
    public void padSettings() {
        PadsBottomSheet padsBottomSheet = new PadsBottomSheet();
        padsBottomSheet.show(actionInterface.getMyFragmentManager(),"padsBottomSheet");
    }

    // Start or stop the metronome
    public void toggleMetronome() {
        actionInterface.metronomeToggle();
    }

    // Open the metronome settings
    public void metronomeSettings() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/actions/metronome",0);
    }

    // Next song
    public void nextSong() {
        mainActivityInterface.getDisplayPrevNext().moveToNext();
    }

    // Next song
    public void prevSong() {
        mainActivityInterface.getDisplayPrevNext().moveToPrev();
    }

    // Scroll up/down
    public void scroll(boolean scrollDown) {
        if (myZoomLayout!=null && pdfRecycler!=null && mainActivityInterface.getMode().equals("Performance")) {
            try {
                if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                    int height = (int)(mainActivityInterface.getGestures().getScrollDistance()*pdfRecycler.getHeight());
                    if (!scrollDown) {
                        height = - height;
                    }
                    pdfRecycler.smoothScrollBy(0,height);
                } else {
                    myZoomLayout.animateScrollBy(mainActivityInterface.getGestures().getScrollDistance(), scrollDown);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Find a random song
    public void randomSong() {
        RandomSongBottomSheet randomSongBottomSheet;
        if (mainActivityInterface.getShowSetMenu()) {
            randomSongBottomSheet = new RandomSongBottomSheet("set");
        } else {
            randomSongBottomSheet = new RandomSongBottomSheet("song");
        }
        randomSongBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"randomSongBottomSheet");
    }

    // Transpose the chords
    public void transpose() {
        TransposeBottomSheet transposeBottomSheet = new TransposeBottomSheet(false);
        transposeBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"transposeBottomSheet");
    }

    // Show chord fingering in the song
    public void showChordFingerings() {
        ChordFingeringBottomSheet chordFingeringBottomSheet = new ChordFingeringBottomSheet();
        chordFingeringBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "chordFingeringBottomSheet");
    }

    // Show chords in the song display
    public void showChords() {
        boolean displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"displayChords", true);
        mainActivityInterface.getPreferences().setMyPreferenceBoolean(c,"displayChords", !displayChords);
        mainActivityInterface.navHome();
    }

    // Show the chord settings
    public void chordSettings() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/actions/chords",0);
    }

    // Toggle between native, capo and both
    public void showCapo() {
        // TODO
    }

    // Show or hide the lyrics
    public void showLyrics() {
        // TODO
    }

    // Show the abc notation
    public void showABCNotation() {
        MusicScoreBottomSheet musicScoreBottomSheet = new MusicScoreBottomSheet();
        musicScoreBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"MusicScoreBottomSheet");
    }

    // Abc notation settings
    public void abcEdit() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/actions/abcnotation",0);
    }

    // Show the highlighter notes
    public void showHighlight() {
        // TODO
    }

    // Highlighter edit
    public void highlighterEdit() {
        actionInterface.navigateToFragment("opensongapp://songactions/highlighter/edit",0);
    }

    // Show the song sticky notes
    public void showSticky() {
        // Toggle the force show (hide is for moving away from performace/stage mode)
        actionInterface.showSticky(true,false);
    }

    // Sticky notes settings
    public void stickySettings() {
        actionInterface.navigateToFragment("opensongapp://settings/actions/stickynotes",0);
    }

    // Increase the autoscroll speed
    public void speedUpAutoscroll() {
        mainActivityInterface.getAutoscroll().speedUpAutoscroll();
    }

    // Decrease the autoscroll speed
    public void slowDownAutoscroll() {
        mainActivityInterface.getAutoscroll().slowDownAutoscroll();
    }

    // Pause autoscrolling
    public void pauseAutoscroll() {
        mainActivityInterface.getAutoscroll().pauseAutoscroll();
    }

    // Open links
    public void openLinks() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/actions/links",0);
    }

    // PDF page chooser
    public void pdfPage() {
        if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
            PDFPageBottomSheet pdfPageBottomSheet = new PDFPageBottomSheet();
            pdfPageBottomSheet.show(actionInterface.getMyFragmentManager(),"PDFPageBottomSheet");
        }
    }

    // Open the theme chooser
    public void editTheme() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/display/theme",0);
    }

    // Open the autoscale options
    public void editAutoscale() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/display/scaling",0);
    }

    // Edit the fonts
    public void editFonts() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/display/fonts",0);
    }

    // Open the gestures settings
    public void editGestures() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/controls/gestures",0);
    }

    // Open the profile settings
    public void editProfiles() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/profiles",0);
    }

    // Open the pedal settings
    public void editPedals() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/controls/pedals",0);
    }

    // Open the song midi bottom sheet
    public void songMidi() {
        MidiSongBottomSheet midiSongBottomSheet = new MidiSongBottomSheet();
        midiSongBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"midiSongBottomSheet");
    }

    // Open the midi settings
    public void editMidi() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/midi",0);
    }

    // Get the bible settings
    public void bibleSettings() {
        mainActivityInterface.navigateToFragment("opensongapp://settings/bible",0);
    }

    // Sound level
    public void soundLevel() {
        SoundLevelBottomSheet soundLevelBottomSheet = new SoundLevelBottomSheet();
        soundLevelBottomSheet.show(actionInterface.getMyFragmentManager(),"SoundLevelBottomSheet");
    }

    // Back pressed
    public void onBackPressed() {
        actionInterface.onBackPressed();
    }
}

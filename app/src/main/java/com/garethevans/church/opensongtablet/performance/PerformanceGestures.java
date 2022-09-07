package com.garethevans.church.opensongtablet.performance;

// The gestures used in the app

import android.content.Context;
import android.os.Build;
import android.view.View;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.autoscroll.AutoscrollBottomSheet;
import com.garethevans.church.opensongtablet.chords.ChordFingeringBottomSheet;
import com.garethevans.church.opensongtablet.chords.TransposeBottomSheet;
import com.garethevans.church.opensongtablet.customviews.MyRecyclerView;
import com.garethevans.church.opensongtablet.customviews.MyZoomLayout;
import com.garethevans.church.opensongtablet.interfaces.ActionInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.midi.MidiSongBottomSheet;
import com.garethevans.church.opensongtablet.pads.PadsBottomSheet;
import com.garethevans.church.opensongtablet.pdf.PDFPageAdapter;
import com.garethevans.church.opensongtablet.pdf.PDFPageBottomSheet;
import com.garethevans.church.opensongtablet.songmenu.RandomSongBottomSheet;
import com.garethevans.church.opensongtablet.stage.StageSectionAdapter;
import com.garethevans.church.opensongtablet.utilities.SoundLevelBottomSheet;

public class PerformanceGestures {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final ActionInterface actionInterface;
    private MyZoomLayout myZoomLayout;
    private MyRecyclerView recyclerView;

    // Initialise
    public PerformanceGestures(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        actionInterface = (ActionInterface) c;
    }
    public void setZoomLayout(MyZoomLayout myZoomLayout) {
        this.myZoomLayout = myZoomLayout;
    }
    public void setRecyclerView(MyRecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    // The following are called from GestureListener, PedalActions, PageButtons

    // Edit page buttons
    public void editPageButtons() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_page_buttons),0);
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
        if (mainActivityInterface.getProcessSong().isValidSong(mainActivityInterface.getSong())) {
            mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_edit), 0);
        } else {
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.not_allowed));
        }
    }

    // Add to set
    public void addToSet() {
        String itemForSet = mainActivityInterface.getSetActions().whatToLookFor(mainActivityInterface.getSong());

        // Allow the song to be added, even if it is already there
        String val = mainActivityInterface.getPreferences().getMyPreferenceString("setCurrent","") + itemForSet;
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrent",val);

        // Tell the user that the song has been added.
        mainActivityInterface.getShowToast().doIt("\"" + mainActivityInterface.getSong().getFilename() + "\" " +
                c.getString(R.string.added_to_set));
        
        mainActivityInterface.getCurrentSet().addSetItem(itemForSet);
        mainActivityInterface.getCurrentSet().addSetValues(mainActivityInterface.getSong());
        mainActivityInterface.updateSetList();
        mainActivityInterface.updateCheckForThisSong(mainActivityInterface.getSong());
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
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_metronome),0);
    }

    // Next song
    public void nextSong() {
        if (mainActivityInterface.getPedalActions().getPedalScrollBeforeMove() && canScroll(true)) {
            scroll(true);
        } else {
            mainActivityInterface.getDisplayPrevNext().moveToNext();
        }
    }

    // Next song
    public void prevSong() {
        if (mainActivityInterface.getPedalActions().getPedalScrollBeforeMove() && canScroll(false)) {
            scroll(false);
        } else {
            mainActivityInterface.getDisplayPrevNext().moveToPrev();
        }
    }

    // This is for the scroll before move
    public boolean canScroll(boolean scrollDown) {
        // Check the ZoomLayout for XML
        if (myZoomLayout != null && myZoomLayout.getVisibility() == View.VISIBLE) {
            // Can we scroll down?
            if (scrollDown && !myZoomLayout.getScrolledToBottom()) {
                return true;
            } else {
                return !scrollDown && !myZoomLayout.getScrolledToTop();
            }

        // Check the recyclerView for images/pdfs
        } else if (recyclerView != null && recyclerView.getVisibility() == View.VISIBLE) {
            if (mainActivityInterface.getMode().equals("Stage")) {
                int currentPos, finalPos;
                if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                    currentPos = mainActivityInterface.getSong().getPdfPageCurrent();
                    finalPos = mainActivityInterface.getSong().getPdfPageCount()-1;
                } else {
                    currentPos = mainActivityInterface.getSong().getCurrentSection();
                    finalPos = mainActivityInterface.getSong().getPresoOrderSongSections().size()-1;
                }
                if (scrollDown && currentPos<finalPos) {
                    return true;
                } else {
                    return !scrollDown && currentPos > 0;
                }
            } else {
                if (scrollDown && !recyclerView.getScrolledToBottom()) {
                    return true;
                } else {
                    return !scrollDown && !recyclerView.getScrolledToTop();
                }
            }
        } else {
            return false;
        }
    }

    // Scroll up/down
    public void scroll(boolean scrollDown) {
        if (myZoomLayout != null && myZoomLayout.getVisibility() == View.VISIBLE) {
            myZoomLayout.animateScrollBy(mainActivityInterface.getGestures().getScrollDistance(), scrollDown);
        } else if (mainActivityInterface.getMode().equals("Performance") &&
                recyclerView != null && recyclerView.getVisibility() == View.VISIBLE) {
            int height = (int)(mainActivityInterface.getGestures().getScrollDistance()*recyclerView.getHeight());
            if (!scrollDown) {
                height = - height;
            }
            recyclerView.smoothScrollBy(0,height);

        } else if (mainActivityInterface.getMode().equals("Stage") &&
                recyclerView != null && recyclerView.getVisibility() == View.VISIBLE) {
            int currentPosition = mainActivityInterface.getSong().getCurrentSection();
            int finalPosition = mainActivityInterface.getSong().getPresoOrderSongSections().size() - 1;

            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                currentPosition = mainActivityInterface.getSong().getPdfPageCurrent();
                finalPosition = mainActivityInterface.getSong().getPdfPageCount() - 1;
            }
            int newPosition = currentPosition;

            if (scrollDown) {
                if (currentPosition < finalPosition) {
                    if (recyclerView.getLayoutManager() != null) {
                        recyclerView.getLayoutManager().scrollToPosition(currentPosition + 1);
                        newPosition++;
                    }
                }
            } else {
                if (currentPosition > 0) {
                    if (recyclerView.getLayoutManager() != null) {
                        recyclerView.getLayoutManager().scrollToPosition(currentPosition - 1);
                        newPosition--;
                    }
                }
            }

            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                mainActivityInterface.getSong().setPdfPageCurrent(newPosition);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                        recyclerView.getAdapter()!=null) {
                    ((PDFPageAdapter)recyclerView.getAdapter()).clickOnSection(newPosition);
                }
            } else {
                mainActivityInterface.getSong().setCurrentSection(newPosition);
                if (recyclerView.getAdapter()!=null) {
                    ((StageSectionAdapter)recyclerView.getAdapter()).clickOnSection(newPosition);
                }
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
        transposeBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "transposeBottomSheet");
    }

    // Show chord fingering in the song
    public void showChordFingerings() {
        ChordFingeringBottomSheet chordFingeringBottomSheet = new ChordFingeringBottomSheet();
        chordFingeringBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "chordFingeringBottomSheet");
    }

    // Show chords in the song display
    public void showChords() {
        boolean displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean("displayChords", true);
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("displayChords", !displayChords);
        mainActivityInterface.getProcessSong().updateProcessingPreferences();
        mainActivityInterface.navHome();
    }

    // Show the chord settings
    public void chordSettings() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_chords),0);
    }

    // Toggle between native, capo and both
    public void showCapo() {
        boolean displayCapoChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean("displayCapoChords", true);
        boolean displayCapoAndNativeChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean("displayCapoAndNativeChords", false);
        if (displayCapoAndNativeChords) {
            displayCapoAndNativeChords = false;
            displayCapoChords = false;
        } else if (displayCapoChords) {
            displayCapoAndNativeChords = true;
        } else {
            displayCapoChords = true;
            //displayCapoAndNativeChords = false; // Already set to this
        }
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("displayCapoChords", displayCapoChords);
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("displayCapoAndNativeChords", displayCapoAndNativeChords);
        mainActivityInterface.getProcessSong().updateProcessingPreferences();
        mainActivityInterface.navHome();
    }

    // Show or hide the lyrics
    public void showLyrics() {
        boolean displayLyrics = mainActivityInterface.getPreferences().getMyPreferenceBoolean("displayLyrics", true);
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("displayLyrics", !displayLyrics);
        mainActivityInterface.getProcessSong().updateProcessingPreferences();
        mainActivityInterface.navHome();
    }

    // Show the abc notation
    public void showABCNotation() {
        //MusicScoreBottomSheet musicScoreBottomSheet = new MusicScoreBottomSheet();
        //musicScoreBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"MusicScoreBottomSheet");
        actionInterface.showAbc(true, false);
    }

    // Abc notation settings
    public void abcEdit() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_abc),0);
    }

    // Show the highlighter notes
    public void showHighlight() {
        actionInterface.toggleHighlighter();
    }

    // Highlighter edit
    public void highlighterEdit() {
        if (ifPDFAndAllowed() || !mainActivityInterface.getSong().getFiletype().equals("PDF")) {
            actionInterface.navigateToFragment(c.getString(R.string.deeplink_highlighter), 0);
        }
    }

    // Show the song sticky notes
    public void showSticky() {
        // Toggle the force show (hide is for moving away from performace/stage mode)
        actionInterface.showSticky(true,false);
    }

    // Sticky notes settings
    public void stickySettings() {
        actionInterface.navigateToFragment(c.getString(R.string.deeplink_sticky_notes),0);
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
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_links),0);
    }

    // PDF page chooser
    public void pdfPage() {
        if (ifPDFAndAllowed()) {
            PDFPageBottomSheet pdfPageBottomSheet = new PDFPageBottomSheet();
            pdfPageBottomSheet.show(actionInterface.getMyFragmentManager(),"PDFPageBottomSheet");
        }
    }

    // Open the theme chooser
    public void editTheme() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_theme),0);
    }

    // Open the autoscale options
    public void editAutoscale() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_scaling),0);
    }

    // Edit the fonts
    public void editFonts() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_fonts),0);
    }

    // Open the gestures settings
    public void editGestures() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_gestures),0);
    }

    // Open the profile settings
    public void editProfiles() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_profiles),0);
    }

    // Open the pedal settings
    public void editPedals() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_pedals),0);
    }

    // Open the song midi bottom sheet
    public void songMidi() {
        MidiSongBottomSheet midiSongBottomSheet = new MidiSongBottomSheet();
        midiSongBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"midiSongBottomSheet");
    }

    // Open the midi settings
    public void editMidi() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_midi),0);
    }

    // Get the bible settings
    public void bibleSettings() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_bible),0);
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


    // The checks
    private boolean ifPDFAndAllowed() {
        if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return true;
            } else {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.not_high_enough_api));
                return false;
            }
        } else {
            return false;
        }
    }

}

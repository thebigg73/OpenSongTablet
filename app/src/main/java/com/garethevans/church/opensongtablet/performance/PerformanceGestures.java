package com.garethevans.church.opensongtablet.performance;

// The gestures used in the app

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.autoscroll.AutoscrollBottomSheet;
import com.garethevans.church.opensongtablet.chords.ChordFingeringBottomSheet;
import com.garethevans.church.opensongtablet.chords.TransposeBottomSheet;
import com.garethevans.church.opensongtablet.customviews.MyRecyclerView;
import com.garethevans.church.opensongtablet.customviews.MyZoomLayout;
import com.garethevans.church.opensongtablet.interfaces.ActionInterface;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.midi.MidiSongBottomSheet;
import com.garethevans.church.opensongtablet.pads.PadsBottomSheet;
import com.garethevans.church.opensongtablet.pdf.PDFPageAdapter;
import com.garethevans.church.opensongtablet.pdf.PDFPageBottomSheet;
import com.garethevans.church.opensongtablet.presenter.SongSectionsAdapter;
import com.garethevans.church.opensongtablet.songmenu.RandomSongBottomSheet;
import com.garethevans.church.opensongtablet.stage.StageSectionAdapter;
import com.garethevans.church.opensongtablet.utilities.SoundLevelBottomSheet;
import com.garethevans.church.opensongtablet.utilities.TunerBottomSheet;
import com.google.android.material.button.MaterialButton;

public class PerformanceGestures {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final DisplayInterface displayInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "PerformanceGestures";
    private final ActionInterface actionInterface;
    private MyZoomLayout myZoomLayout;
    private MyRecyclerView recyclerView;
    private RecyclerView presenterRecyclerView;

    // Initialise
    public PerformanceGestures(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        actionInterface = (ActionInterface) c;
        displayInterface = (DisplayInterface) c;
    }
    public void setZoomLayout(MyZoomLayout myZoomLayout) {
        this.myZoomLayout = myZoomLayout;
    }
    public void setRecyclerView(MyRecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        if (mainActivityInterface!=null) {
            this.recyclerView.initialiseRecyclerView(mainActivityInterface);
        }
    }
    public void setPresenterRecyclerView(RecyclerView presenterRecyclerView) {
        this.presenterRecyclerView = presenterRecyclerView;
    }


    public void doAction(String action, boolean isLongPress) {
        Log.d(TAG,"action:"+action+"  isLongPress:"+isLongPress);
        // Get the action we are trying to run
        switch(action) {
            case "pageButtons":
                editPageButtons();
                break;

            // Set actions
            case "set":
                setMenu();
                break;
            case "inlineset":
                if (isLongPress) {
                    inlineSetSettings();
                } else {
                    inlineSet();
                }
                break;
            case "inlinesetsettings":
                inlineSetSettings();
                break;
            case "addtoset":
                if (isLongPress) {
                    addToSetAsVariation();
                } else {
                    addToSet();
                }
                break;
            case "addtosetvariation":
                addToSetAsVariation();
                break;
            case "exportset":
                if (isLongPress) {
                    manageSets();
                } else {
                    exportSet();
                }
                break;

            // Song actions
            case "pad":
                if (isLongPress) {
                    padSettings();
                } else {
                    togglePad();
                }
                break;
            case "padsettings":
                padSettings();
                break;
            case "metronome":
                if (isLongPress) {
                    metronomeSettings();
                } else {
                    toggleMetronome();
                }
                break;
            case "metronomesettings":
                metronomeSettings();
                break;
            case "autoscroll":
                if (isLongPress) {
                    autoscrollSettings();
                } else {
                    toggleAutoscroll();
                }
                break;
            case "autoscrollsettings":
                autoscrollSettings();
                break;
            case "inc_autoscroll_speed":
                speedUpAutoscroll();
                break;
            case "dec_autoscroll_speed":
                slowDownAutoscroll();
                break;
            case "toggle_autoscroll_pause":
                pauseAutoscroll();
                break;
            case "pad_autoscroll":
                togglePad();
                toggleAutoscroll();
                break;
            case "pad_metronome":
                togglePad();
                toggleMetronome();
                break;
            case "autoscroll_metronome":
                toggleMetronome();
                toggleAutoscroll();
                break;
            case "pad_autoscroll_metronome":
                togglePad();
                toggleMetronome();
                toggleAutoscroll();
                break;
            case "editsong":
                editSong();
                break;
            case "share_song":
                shareSong();
                break;
            case "import":
                if (isLongPress) {
                    addSongs();
                } else {
                    onlineImport();
                }
                break;
            case "importonline":
                onlineImport();
                break;
            case "importoptions":
                addSongs();
                break;


            // Song navigation
            case "songmenu":
                songMenu();
                break;
            case "scrolldown":
            case "down":
                scroll(true);
                break;
            case "scrollup":
            case "up":
                scroll(false);
                break;
            case "next":
                nextSong();
                break;
            case "prev":
            case "previous":
                prevSong();
                break;
            case "randomsong":
                randomSong();
                break;
            case "scrollmenuup":
                mainActivityInterface.scrollOpenMenu(false);
                break;
            case "scrollmenudown":
                mainActivityInterface.scrollOpenMenu(true);
                break;


            // Chords
            case "transpose":
                if (isLongPress) {
                    mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_chords),0);
                } else {
                    transpose();
                }
                break;
            case "transposesettings":
                mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_chords),0);
                break;
            case "chordfingerings":
                if (isLongPress) {
                    customChords();
                } else {
                    showChordFingerings();
                }
                break;
            case "customchords":
                customChords();
                break;


            // Song information
            case "link":
                openLinks();
                break;
            case "stickynotes":
                if (isLongPress) {
                    stickySettings();
                } else {
                    showSticky();
                }
                break;
            case "stickynotessettings":
                stickySettings();
                break;
            case "highlight":
                if (isLongPress) {
                    highlighterEdit();
                } else {
                    showHighlight();
                }
                break;
            case "highlightedit":
                highlighterEdit();
                break;
            case "abc":
                if (isLongPress) {
                    abcEdit();
                } else {
                    showABCNotation();
                }
                break;
            case "abcedit":
                abcEdit();
                break;


            // Display
            case "profiles":
                editProfiles();
                break;
            case "showchords":
                showChords();
                break;
            case "showcapo":
                showCapo();
                break;
            case "showlyrics":
                showLyrics();
                break;
            case "theme":
                editTheme();
                break;
            case "togglescale":
                if (isLongPress) {
                    editAutoscale();
                } else {
                    toggleScale();
                }
                break;
            case "autoscalesettings":
                editAutoscale();
                break;
            case "pdfpage":
                pdfPage();
                break;
            case "invertpdf":
                invertPDF();
                break;
            case "fonts":
                editFonts();
                break;
            case "refreshsong":
                loadSong();
                break;
            case "showlogo":
                showLogo();
                break;


            // Controls
            case "nearby":
                if (isLongPress) {
                    nearbySettings();
                } else {
                    nearbyDiscover();
                }
                break;
            case "nearbysettings":
                nearbySettings();
                break;
            case "gestures":
                editGestures();
                break;
            case "pedals":
                editPedals();
                break;
            case "midi":
                if (isLongPress) {
                    toggleMidiSend();
                } else {
                    songMidi();
                }
                break;
            case "midisend":
                toggleMidiSend();
                break;
            case "midisettings":
                editMidi();
                break;
            case "beatbuddystart":
                if (isLongPress) {
                    beatBuddyStop();
                } else {
                    beatBuddyStart();
                }
                break;
            case "beatbuddystop":
                beatBuddyStop();
                break;
            case "beatbuddypause":
                beatBuddyPause();
                break;
            case "beatbuddyaccent":
                if (isLongPress) {
                    beatBuddyFill();
                } else {
                    beatBuddyAccent();
                }
                break;
            case "beatbuddyfill":
                if (isLongPress) {
                    beatBuddyAccent();
                } else {
                    beatBuddyFill();
                }
                break;
            case "beatbuddytrans1":
                if (isLongPress) {
                    beatBuddyTransitionExit();
                } else {
                    beatBuddyTransition1();
                }
                break;
            case "beatbuddytrans2":
                if (isLongPress) {
                    beatBuddyTransitionExit();
                } else {
                    beatBuddyTransition2();
                }
                break;
            case "beatbuddytrans3":
                if (isLongPress) {
                    beatBuddyTransitionExit();
                } else {
                    beatBuddyTransition3();
                }
                break;
            case "beatbuddytransnext":
                if (isLongPress) {
                    beatBuddyTransitionExit();
                } else {
                    beatBuddyTransitionNext();
                }
                break;
            case "beatbuddytransprev":
                if (isLongPress) {
                    beatBuddyTransitionExit();
                } else {
                    beatBuddyTransitionPrev();
                }
                break;
            case "beatbuddytransexit":
                beatBuddyTransitionExit();
                break;
            case "beatbuddyxtrans1":
                if (isLongPress) {
                    beatBuddyExclusiveTransitionExit();
                } else {
                    beatBuddyExclusiveTransition1();
                }
                break;
            case "beatbuddyxtrans2":
                if (isLongPress) {
                    beatBuddyExclusiveTransitionExit();
                } else {
                    beatBuddyExclusiveTransition2();
                }
                break;
            case "beatbuddyxtrans3":
                if (isLongPress) {
                    beatBuddyExclusiveTransitionExit();
                } else {
                    beatBuddyExclusiveTransition3();
                }
                break;
            case "beatbuddyxtransnext":
                if (isLongPress) {
                    beatBuddyExclusiveTransitionExit();
                } else {
                    beatBuddyExclusiveTransitionNext();
                }
                break;
            case "beatbuddyxtransprev":
                if (isLongPress) {
                    beatBuddyExclusiveTransitionExit();
                } else {
                    beatBuddyExclusiveTransitionPrev();
                }
                break;
            case "beatbuddyxtransexit":
                beatBuddyExclusiveTransitionExit();
                break;
            case "beatbuddyhalf":
                if (isLongPress) {
                    beatBuddyHalfTimeExit();
                } else {
                    beatBuddyHalfTime();
                }
                break;
            case "beatbuddyhalfexit":
                beatBuddyHalfTimeExit();
                break;
            case "beatbuddydouble":
                if (isLongPress) {
                    beatBuddyDoubleTimeExit();
                } else {
                    beatBuddyDoubleTime();
                }
                break;
            case "beatbuddydoubleexit":
                beatBuddyDoubleTimeExit();
                break;
            case "midiaction1":
                midiAction(1);
                break;
            case "midiaction2":
                midiAction(2);
                break;
            case "midiaction3":
                midiAction(3);
                break;
            case "midiaction4":
                midiAction(4);
                break;
            case "midiaction5":
                midiAction(5);
                break;
            case "midiaction6":
                midiAction(6);
                break;
            case "midiaction7":
                midiAction(7);
                break;
            case "midiaction8":
                midiAction(8);
                break;

            // Utilities
            case "soundlevel":
                soundLevel();
                break;
            case "tuner":
                showTuner();
                break;
            case "bible":
                bibleSettings();
                break;


            // Exit
            case "exit":
                onBackPressed();
                break;
        }
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

    // Inline set
    public void inlineSet() {
        mainActivityInterface.toggleInlineSet();
    }
    public void inlineSetSettings() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_inlineset),0);
    }

    // Edit song
    public void editSong() {
        if (mainActivityInterface!=null && mainActivityInterface.getProcessSong().isValidSong(mainActivityInterface.getSong())) {
            // The song is a valid XML file
            // If this is in a set and it is a temp variation, we need to edit the original instead
            int positionInSet = mainActivityInterface.getCurrentSet().getIndexSongInSet();
            if (positionInSet>-1 && mainActivityInterface.getCurrentSet().getSetItems().size()>positionInSet) {
                if (!mainActivityInterface.getCurrentSet().getFolder(positionInSet).equals(mainActivityInterface.getSong().getFolder())) {
                    mainActivityInterface.getSong().setFolder(mainActivityInterface.getCurrentSet().getFolder(positionInSet));
                    mainActivityInterface.getSong().setFilename(mainActivityInterface.getCurrentSet().getFilename(positionInSet));
                    mainActivityInterface.getLoadSong().doLoadSongFile(mainActivityInterface.getSong(),false);
                    mainActivityInterface.setWhattodo("editTempVariation");
                } else if (mainActivityInterface.getCurrentSet().getFolder(positionInSet).contains("**Variation")) {
                    mainActivityInterface.setWhattodo("editActualVariation");
                }
            }
            mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_edit), 0);
        } else {
            if (mainActivityInterface!=null) {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.not_allowed));
            }
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

    // Add to set as a variation
    public void addToSetAsVariation() {
        // Make a copy of this song in the variations folder
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Variations","",
                mainActivityInterface.getSong().getFilename(),mainActivityInterface.getProcessSong().getXML(mainActivityInterface.getSong()));
        mainActivityInterface.getSong().setFolder("**Variations");

        String itemForSet = mainActivityInterface.getSetActions().whatToLookFor(mainActivityInterface.getSong());

        // Allow the song to be added, even if it is already there
        String val = mainActivityInterface.getPreferences().getMyPreferenceString("setCurrent","") + itemForSet;
        mainActivityInterface.getPreferences().setMyPreferenceString("setCurrent",val);

        // Tell the user that the song has been added.
        mainActivityInterface.getShowToast().doIt("\"" + mainActivityInterface.getSong().getFilename() + "\" " +
                c.getString(R.string.added_to_set)+" (" + c.getString(R.string.variation) + " )");

        mainActivityInterface.getCurrentSet().addSetItem(itemForSet);
        mainActivityInterface.getCurrentSet().addSetValues(mainActivityInterface.getSong());
        mainActivityInterface.updateSetList();
        mainActivityInterface.updateCheckForThisSong(mainActivityInterface.getSong());
    }

    public void exportSet() {
        mainActivityInterface.setWhattodo("exportset");
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_sets_manage),0);
    }

    public void manageSets() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_sets),0);
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
            if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter))) {
                int pos = mainActivityInterface.getCurrentSet().getIndexSongInSet();
                if (pos==-1) {
                    pos = mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
                }
                if (pos>-1 && pos<mainActivityInterface.getCurrentSet().getSetItems().size()) {
                    mainActivityInterface.loadSongFromSet(pos+1);
                } else {
                    mainActivityInterface.getShowToast().doIt(c.getString(R.string.last_song));
                }
            } else {
                mainActivityInterface.getDisplayPrevNext().moveToNext();
            }
        }
    }

    // Next song
    public void prevSong() {
        if (mainActivityInterface.getPedalActions().getPedalScrollBeforeMove() && canScroll(false)) {
            scroll(false);
        } else {
            if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter))) {
                int pos = mainActivityInterface.getCurrentSet().getIndexSongInSet();
                if (pos==-1) {
                    pos = mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
                }
                if (pos>0) {
                    mainActivityInterface.loadSongFromSet(pos-1);
                } else {
                    mainActivityInterface.getShowToast().doIt(c.getString(R.string.first_song));
                }
            } else {
                mainActivityInterface.getDisplayPrevNext().moveToPrev();
            }
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
            if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage))) {
                int currentPos, finalPos;
                if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                    currentPos = mainActivityInterface.getSong().getPdfPageCurrent();
                    finalPos = mainActivityInterface.getSong().getPdfPageCount() - 1;
                } else {
                    currentPos = mainActivityInterface.getSong().getCurrentSection();
                    finalPos = mainActivityInterface.getSong().getPresoOrderSongSections().size() - 1;
                }
                if (scrollDown && currentPos < finalPos) {
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
        } else if (presenterRecyclerView!=null) {
            int currentPos, finalPos;
            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                currentPos = mainActivityInterface.getSong().getPdfPageCurrent();
                finalPos = mainActivityInterface.getSong().getPdfPageCount() - 1;
            } else {
                currentPos = mainActivityInterface.getSong().getCurrentSection();
                finalPos = mainActivityInterface.getSong().getPresoOrderSongSections().size() - 1;
            }
            if (scrollDown && currentPos < finalPos) {
                return true;
            } else {
                return !scrollDown && currentPos > 0;
            }
        } else {
            return false;
        }
    }

    // Scroll up/down
    public void scroll(boolean scrollDown) {
        Log.d(TAG,"getMode():"+mainActivityInterface.getMode());
        Log.d(TAG,"recyclerView:"+recyclerView);

        Log.d(TAG,"scrollDown:"+scrollDown);
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter)) &&
                presenterRecyclerView!=null) {
            int currentPosition = mainActivityInterface.getSong().getCurrentSection();
            int finalPosition = mainActivityInterface.getSong().getPresoOrderSongSections().size() - 1;

            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                currentPosition = mainActivityInterface.getSong().getPdfPageCurrent();
                finalPosition = mainActivityInterface.getSong().getPdfPageCount() - 1;
            }

            Log.d(TAG,"currentPosition:"+currentPosition+"  finalPosition:"+finalPosition);

            int newPosition = currentPosition;

            if (scrollDown) {
                if (currentPosition < finalPosition) {
                    newPosition++;
                }
            } else {
                if (currentPosition > 0) {
                    newPosition--;
                }
            }

            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                mainActivityInterface.getSong().setPdfPageCurrent(newPosition);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                        presenterRecyclerView.getAdapter()!=null) {
                    ((PDFPageAdapter)recyclerView.getAdapter()).clickOnSection(newPosition);
                }
            } else {
                mainActivityInterface.getSong().setCurrentSection(newPosition);
                if (presenterRecyclerView.getAdapter()!=null) {
                    ((SongSectionsAdapter)presenterRecyclerView.getAdapter()).itemSelected(newPosition);
                }
            }

        } else if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage)) &&
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
                    newPosition++;
                }
            } else {
                if (currentPosition > 0) {
                    newPosition--;
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


        } else if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                recyclerView != null && recyclerView.getVisibility() == View.VISIBLE) {
            int height = (int)(mainActivityInterface.getGestures().getScrollDistance()*recyclerView.getHeight());
            if (!scrollDown) {
                height = - height;
            }
            recyclerView.smoothScrollBy(0,height);
            // We will also send this to nearby devices if we are a host
            mainActivityInterface.getNearbyConnections().sendScrollByPayload(scrollDown,
                    mainActivityInterface.getGestures().getScrollDistance());


        } else if (myZoomLayout != null && myZoomLayout.getVisibility() == View.VISIBLE) {
            myZoomLayout.animateScrollBy(mainActivityInterface,
                    mainActivityInterface.getGestures().getScrollDistance(), scrollDown);
            // We will also send this to nearby devices if we are a host
            mainActivityInterface.getNearbyConnections().sendScrollByPayload(scrollDown,
                    mainActivityInterface.getGestures().getScrollDistance());
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

    // Custom chords
    public void customChords() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_chords_custom),0);
    }

    // Toggle the show logo on the secondary screen
    public void showLogo() {
        mainActivityInterface.getPresenterSettings().setLogoOn(!mainActivityInterface.getPresenterSettings().getLogoOn());
        displayInterface.updateDisplay("showLogo");
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

    // Show the tuner bottom sheet
    public void showTuner() {
        TunerBottomSheet tunerBottomSheet = new TunerBottomSheet();
        tunerBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "tunerBottomSheet");
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
        mainActivityInterface.getNearbyConnections().increaseAutoscrollPayload();
    }

    // Decrease the autoscroll speed
    public void slowDownAutoscroll() {
        mainActivityInterface.getAutoscroll().slowDownAutoscroll();
        mainActivityInterface.getNearbyConnections().decreaseAutoscrollPayload();
    }

    // Pause autoscrolling
    public void pauseAutoscroll() {
        mainActivityInterface.getAutoscroll().pauseAutoscroll();
        mainActivityInterface.getNearbyConnections().sendAutoscrollPausePayload();
    }

    // Open links
    public void openLinks() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_links),0);
    }

    // Nearby connections
    public void nearbySettings() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_nearby),0);
    }
    public void nearbyDiscover() {
        // Run a 10 second discovery attempt
        // Stop advertising/discovering if we were already doing that
        mainActivityInterface.getNearbyConnections().stopAdvertising();
        mainActivityInterface.getNearbyConnections().stopDiscovery();

        // Initialise the countdown
        mainActivityInterface.getNearbyConnections().initialiseCountdown();

        // After a short delay, discover
        new Handler().postDelayed(() -> {
            try {
                mainActivityInterface.getNearbyConnections().startDiscovery();
                mainActivityInterface.getNearbyConnections().setTimer(false, new MaterialButton(c));
            } catch (Exception e) {
                e.printStackTrace();
                mainActivityInterface.getNearbyConnections().clearTimer();
            }
        }, 200);
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
    public void toggleScale() {
        mainActivityInterface.toggleScale();
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

    // Toggle auto MIDI send on/off
    public void toggleMidiSend() {
        boolean newPref = !mainActivityInterface.getMidi().getMidiSendAuto();
        if (newPref) {
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.midi_auto)+": "+c.getString(R.string.on));
        } else {
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.midi_auto)+": "+c.getString(R.string.off));
        }
        mainActivityInterface.getMidi().setMidiSendAuto(newPref);
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

    // Import online song or files
    public void onlineImport() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_import_online),0);
    }
    public void addSongs() {
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_import),0);
    }

    public void invertPDF() {
        mainActivityInterface.getMyThemeColors().setInvertPDF(!mainActivityInterface.getMyThemeColors().getInvertPDF());
        mainActivityInterface.navHome();
    }

    public void shareSong() {
        mainActivityInterface.setWhattodo("exportSong");
        mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_export),0);
    }


    // The BeatBuddy stuff
    public void beatBuddyStart() {
        mainActivityInterface.getBeatBuddy().beatBuddyStart();
    }
    public void beatBuddyStop() {
        mainActivityInterface.getBeatBuddy().beatBuddyStop();
    }
    public void beatBuddyPause() {
        mainActivityInterface.getBeatBuddy().beatBuddyPause();
    }
    public void beatBuddyAccent() {
        mainActivityInterface.getBeatBuddy().beatBuddyAccent();
    }
    public void beatBuddyFill() {
        mainActivityInterface.getBeatBuddy().beatBuddyFill();
    }
    public void beatBuddyTransition1() {
        mainActivityInterface.getBeatBuddy().beatBuddyTransition1();
    }
    public void beatBuddyTransition2() {
        mainActivityInterface.getBeatBuddy().beatBuddyTransition2();
    }
    public void beatBuddyTransition3() {
        mainActivityInterface.getBeatBuddy().beatBuddyTransition3();
    }
    public void beatBuddyTransitionNext() {
        mainActivityInterface.getBeatBuddy().beatBuddyTransitionNext();
    }
    public void beatBuddyTransitionPrev() {
        mainActivityInterface.getBeatBuddy().beatBuddyTransitionPrev();
    }
    public void beatBuddyTransitionExit() {
        mainActivityInterface.getBeatBuddy().beatBuddyTransitionExit();
    }
    public void beatBuddyExclusiveTransition1() {
        mainActivityInterface.getBeatBuddy().beatBuddyExclusiveTransition1();
    }
    public void beatBuddyExclusiveTransition2() {
        mainActivityInterface.getBeatBuddy().beatBuddyExclusiveTransition2();
    }
    public void beatBuddyExclusiveTransition3() {
        mainActivityInterface.getBeatBuddy().beatBuddyExclusiveTransition3();
    }
    public void beatBuddyExclusiveTransitionNext() {
        mainActivityInterface.getBeatBuddy().beatBuddyExclusiveTransitionNext();
    }
    public void beatBuddyExclusiveTransitionPrev() {
        mainActivityInterface.getBeatBuddy().beatBuddyExclusiveTransitionPrev();
    }
    public void beatBuddyExclusiveTransitionExit() {
        mainActivityInterface.getBeatBuddy().beatBuddyExclusiveTransitionExit();
    }
    public void beatBuddyHalfTime() {
        mainActivityInterface.getBeatBuddy().beatBuddyHalfTime();
    }
    public void beatBuddyHalfTimeExit() {
        mainActivityInterface.getBeatBuddy().beatBuddyHalfTimeExit();
    }
    public void beatBuddyDoubleTime() {
        mainActivityInterface.getBeatBuddy().beatBuddyDoubleTime();
    }
    public void beatBuddyDoubleTimeExit() {
        mainActivityInterface.getBeatBuddy().beatBuddyDoubleTimeExit();
    }
    public void midiAction(int which) {
        mainActivityInterface.getMidi().sendMidiHexSequence(mainActivityInterface.getMidi().getMidiAction(which));
    }

    // The checks
    private boolean ifPDFAndAllowed() {
        if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return true;
            } else {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.not_allowed));
                return false;
            }
        } else {
            return false;
        }
    }

}

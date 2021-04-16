package com.garethevans.church.opensongtablet.performance;

// The gestures used in the app

import android.content.Context;
import android.media.MediaPlayer;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

public class PerformanceGestures {

    private final MainActivityInterface mainActivityInterface;
    private final DrawerLayout drawerLayout;
    private final MediaPlayer mPlayer1;
    private final MediaPlayer mPlayer2;
    private int defmetronomecolor;

    PerformanceGestures(Context c, MainActivityInterface mainActivityInterface,
                        DrawerLayout drawerLayout, MediaPlayer mPlayer1, MediaPlayer mPlayer2,
                        int defmetronomecolor) {
        this.mainActivityInterface = mainActivityInterface;
        this.drawerLayout = drawerLayout;
        this.mPlayer1 = mPlayer1;
        this.mPlayer2 = mPlayer2;
    }

    // Open/close the drawers
    void gesture1() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainActivityInterface.closeDrawer(true);
        } else {
            mainActivityInterface.closeDrawer(false);
        }
        mainActivityInterface.getAutoscrollActions().setWasScrolling(false);
        try {
            //appActionBar.removeCallBacks();
            //appActionBar.showActionBar(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Edit song
    private void gesture2() {
        mainActivityInterface.navigateToFragment(null,R.id.editSongFragment);
    }

    // Add to set
    private void gesture3(Context c,Song song) {
        String itemForSet = mainActivityInterface.getSetActions().whatToLookFor(c,song.getFolder(),song.getFilename());

        // Allow the song to be added, even if it is already there
        String val = mainActivityInterface.getPreferences().getMyPreferenceString(c,"setCurrent","") + itemForSet;
        mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrent",val);

        // Tell the user that the song has been added.
        mainActivityInterface.getShowToast().doIt(c,"\"" + song.getFilename() + "\" " +
                c.getString(R.string.addedtoset));

        // Vibrate to let the user know something happened
        mainActivityInterface.getDoVibrate().vibrate(c, 50);

        //TODO Add the song to the set and prepare the new set list
        //setActions.prepareSetList(c,preferences);
        mainActivityInterface.updateSetList();
    }

    // Redraw the lyrics page
    private void gesture4(Song song) {
        mainActivityInterface.doSongLoad(song.getFolder(),song.getFilename());
    }

    // Stop or start autoscroll
    public boolean gesture5(Context c, MainActivityInterface mainActivityInterface) {
        mainActivityInterface.getDoVibrate().vibrate(c, 50);
        if (mainActivityInterface.getAutoscrollActions().getIsAutoscrolling()) {
            mainActivityInterface.stopAutoscroll();
            return false;  // value for clickedOnAutoScrollStart
        } else {
            if (mainActivityInterface.getAutoscrollActions().getAutoscrollOK() || mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "autoscrollUseDefaultTime", true)) {
                mainActivityInterface.startAutoscroll();
                return true;  // value for clickedOnAutoScrollStart
            } else {
                mainActivityInterface.getShowToast().doIt(c,c.getString(R.string.autoscroll) + " - " +
                        c.getString(R.string.not_set));
                return false;  // value for clickedOnAutoScrollStart
            }
        }
    }

    // Stop or start pads
    public void gesture6(Context c, MainActivityInterface mainActivityInterface,
                         Song song, boolean pad1Fading, boolean pad2Fading) {
        ArrayList<Boolean> padsPlaying = new ArrayList<>();
        boolean pad1Playing = mainActivityInterface.getPadFunctions().getPadStatus(mPlayer1);
        boolean pad2Playing = mainActivityInterface.getPadFunctions().getPadStatus(mPlayer2);
        // IV - If playing pads then fade to stop
        if ((pad1Playing && !pad1Fading)  || (pad2Playing && !pad2Fading)) {
            mainActivityInterface.getDoVibrate().vibrate(c, 50);
            mainActivityInterface.fadeoutPad();
        } else {
            if (mainActivityInterface.getPadFunctions().isPadValid(c,mainActivityInterface)) {
                mainActivityInterface.getDoVibrate().vibrate(c, 50);
                mainActivityInterface.playPad();
            } else {
                // We inform the user - 'Not set' which can be valid
                // IV - gesture6 is now used in page_pad - a page_pad call may result in a loop!
                mainActivityInterface.getShowToast().doIt(c,c.getString(R.string.pad) + " - " +
                        c.getString(R.string.not_set));
            }
        }
    }

    // Start or stop the metronome
    public void gesture7(Context c, Song song) {
        mainActivityInterface.getDoVibrate().vibrate(c, 50);
        boolean metronomeok = mainActivityInterface.getMetronome().isMetronomeValid(song);
        if (metronomeok || mainActivityInterface.getMetronome().getClickedOnMetronomeStart()) {
            // IV - clickedOnMetronomeStart is set elsewhere (Metronome class)
            mainActivityInterface.getMetronome().startstopMetronome(c,song,
                    mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "metronomeShowVisual", false),
                    defmetronomecolor, mainActivityInterface.getPreferences().getMyPreferenceString(c, "metronomePan", "C"),
                    mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "metronomeVol", 0.5f),
                    mainActivityInterface.getPreferences().getMyPreferenceInt(c, "metronomeLength", 0));
        } else {
            mainActivityInterface.getShowToast().doIt(c,c.getString(R.string.metronome) + " - " +
                    c.getString(R.string.not_set));
        }
    }






}

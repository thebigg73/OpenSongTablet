package com.garethevans.church.opensongtablet.performance;

// The gestures used in the app

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.metronome.Metronome;
import com.garethevans.church.opensongtablet.pads.PadFunctions;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.screensetup.DoVibrate;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songsandsets.SetActions;

import java.util.ArrayList;

public class PerformanceGestures {

    Context c;
    PerformanceFragment performanceFragment;
    Preferences preferences;
    SetActions setActions;
    StorageAccess storageAccess;
    PadFunctions padFunctions;
    Metronome metronome;
    ShowToast showToast;
    DoVibrate doVibrate;
    MainActivityInterface mainActivityInterface;
    DrawerLayout drawerLayout;
    Handler delayactionBarHide;
    Runnable hideActionBarRunnable;
    MediaPlayer mPlayer1,mPlayer2;
    int defmetronomecolor;

    PerformanceGestures(Context c, Preferences preferences, StorageAccess storageAccess, SetActions setActions, PadFunctions padFunctions, Metronome metronome,
                        PerformanceFragment performanceFragment, MainActivityInterface mainActivityInterface,
                        ShowToast showToast, DoVibrate doVibrate, DrawerLayout drawerLayout, MediaPlayer mPlayer1,
                        MediaPlayer mPlayer2, Handler delayactionBarHide, Runnable hideActionBarRunnable, int defmetronomecolor) {
        this.c = c;
        this.preferences = preferences;
        this.storageAccess = storageAccess;
        this.setActions = setActions;
        this.performanceFragment = performanceFragment;
        this.mainActivityInterface = mainActivityInterface;
        this.padFunctions = padFunctions;
        this.metronome = metronome;
        this.showToast = showToast;
        this.doVibrate = doVibrate;
        this.drawerLayout = drawerLayout;
        this.delayactionBarHide = delayactionBarHide;
        this.hideActionBarRunnable = hideActionBarRunnable;
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
        StaticVariables.wasscrolling = false;
        try {
            delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Edit song
    private void gesture2() {
        mainActivityInterface.navigateToFragment(R.id.nav_editSong);
    }

    // Add to set
    private void gesture3() {
        String itemForSet = setActions.whatToLookFor(c,StaticVariables.whichSongFolder,StaticVariables.songfilename);

        // Allow the song to be added, even if it is already there
        String val = preferences.getMyPreferenceString(c,"setCurrent","") + itemForSet;
        preferences.setMyPreferenceString(c,"setCurrent",val);

        // Tell the user that the song has been added.
        showToast.doIt(c,"\"" + StaticVariables.songfilename + "\" " +
                c.getResources().getString(R.string.addedtoset));

        // Vibrate to let the user know something happened
        doVibrate.vibrate(c, 50);

        setActions.prepareSetList(c,preferences);
        mainActivityInterface.updateSetList();
    }

    // Redraw the lyrics page
    private void gesture4() {
        mainActivityInterface.doSongLoad();
    }

    // Stop or start autoscroll
    public boolean gesture5(boolean isAutoscrolling) {
        doVibrate.vibrate(c, 50);
        if (isAutoscrolling) {
            mainActivityInterface.stopAutoscroll();
            return false;  // value for clickedOnAutoScrollStart
        } else {
            if (StaticVariables.autoscrollok || preferences.getMyPreferenceBoolean(c, "autoscrollUseDefaultTime", true)) {
                mainActivityInterface.startAutoscroll();
                return true;  // value for clickedOnAutoScrollStart
            } else {
                showToast.doIt(c,c.getResources().getString(R.string.autoscroll) + " - " +
                        c.getResources().getString(R.string.notset));
                return false;  // value for clickedOnAutoScrollStart
            }
        }
    }

    // Stop or start pads
    public void gesture6(Song song, boolean pad1Fading, boolean pad2Fading) {
        ArrayList<Boolean> padsPlaying = new ArrayList<>();
        boolean pad1Playing = padFunctions.getPadStatus(mPlayer1);
        boolean pad2Playing = padFunctions.getPadStatus(mPlayer2);
        // IV - If playing pads then fade to stop
        // IV - StaticVariables.clickedOnPadStart handled elsewhere
        if ((pad1Playing && !pad1Fading)  || (pad2Playing && !pad2Fading)) {
            doVibrate.vibrate(c, 50);
            mainActivityInterface.fadeoutPad();
        } else {
            if (padFunctions.isPadValid(c,storageAccess,preferences,song)) {
                doVibrate.vibrate(c, 50);
                mainActivityInterface.playPad();
            } else {
                // We inform the user - 'Not set' which can be valid
                // IV - gesture6 is now used in page_pad - a page_pad call may result in a loop!
                showToast.doIt(c,c.getResources().getString(R.string.pad) + " - " +
                        c.getResources().getString(R.string.notset));
            }
        }
    }

    // Start or stop the metronome
    public void gesture7(Song song) {
        doVibrate.vibrate(c, 50);
        boolean metronomeok = metronome.isMetronomeValid(song);
        if (metronomeok || metronome.getClickedOnMetronomeStart()) {
            // IV - clickedOnMetronomeStart is set elsewhere (Metronome class)
            metronome.startstopMetronome(c,song,
                    preferences.getMyPreferenceBoolean(c, "metronomeShowVisual", false),
                    defmetronomecolor, preferences.getMyPreferenceString(c, "metronomePan", "C"),
                    preferences.getMyPreferenceFloat(c, "metronomeVol", 0.5f),
                    preferences.getMyPreferenceInt(c, "metronomeLength", 0));
        } else {
            showToast.doIt(c,c.getResources().getString(R.string.metronome) + " - " +
                    c.getResources().getString(R.string.notset));
        }
    }

}

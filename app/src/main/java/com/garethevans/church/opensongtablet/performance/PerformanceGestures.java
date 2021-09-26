package com.garethevans.church.opensongtablet.performance;

// The gestures used in the app

import android.content.Context;
import android.util.Log;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.ActionInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class PerformanceGestures {

    private final String TAG = "PerformanceGestures";
    private final MainActivityInterface mainActivityInterface;
    private final ActionInterface actionInterface;
    private final DrawerLayout drawerLayout;

    PerformanceGestures(Context c, MainActivityInterface mainActivityInterface,
                        DrawerLayout drawerLayout) {
        this.mainActivityInterface = mainActivityInterface;
        this.drawerLayout = drawerLayout;
        actionInterface = (ActionInterface) c;
    }

    // Open/close the drawers
    void gesture1() {
        mainActivityInterface.closeDrawer(drawerLayout.isDrawerOpen(GravityCompat.START));
        mainActivityInterface.getAutoscroll().setWasScrolling(false);
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
    private void gesture3(Context c, Song song) {
        String itemForSet = mainActivityInterface.getSetActions().whatToLookFor(song);

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
        mainActivityInterface.doSongLoad(song.getFolder(),song.getFilename(),true);
    }

    // Stop or start autoscroll
    public void gesture5(Context c, MainActivityInterface mainActivityInterface) {
        mainActivityInterface.getDoVibrate().vibrate(c, 50);
        mainActivityInterface.toggleAutoscroll();
    }

    // Stop or start pads
    public void gesture6(MainActivityInterface mainActivityInterface) {
        mainActivityInterface.playPad();
    }

    // Start or stop the metronome
    public void gesture7() {
        Log.d(TAG,"gesture7()");
        actionInterface.metronomeToggle();
    }






}

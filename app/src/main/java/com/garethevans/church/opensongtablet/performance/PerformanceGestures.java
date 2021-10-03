package com.garethevans.church.opensongtablet.performance;

// The gestures used in the app

import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.ActionInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class PerformanceGestures {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final ActionInterface actionInterface;

    public PerformanceGestures(Context c, MainActivityInterface mainActivityInterface) {
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
        actionInterface = (ActionInterface) c;
    }

    // Song menu
    public void songMenu() {
        mainActivityInterface.chooseMenu(false);
    }

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

        // Allow the song to be added, even if it is already there
        String val = mainActivityInterface.getPreferences().getMyPreferenceString(c,"setCurrent","") + itemForSet;
        mainActivityInterface.getPreferences().setMyPreferenceString(c,"setCurrent",val);

        // Tell the user that the song has been added.
        mainActivityInterface.getShowToast().doIt(c,"\"" + mainActivityInterface.getSong().getFilename() + "\" " +
                c.getString(R.string.addedtoset));

        // Vibrate to let the user know something happened
        mainActivityInterface.getDoVibrate().vibrate(c, 50);

        mainActivityInterface.getCurrentSet().addToCurrentSet(itemForSet);
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

    // Stop or start pads
    public void togglePad() {
        mainActivityInterface.playPad();
    }

    // Start or stop the metronome
    public void toggleMetronome() {
        actionInterface.metronomeToggle();
    }

}

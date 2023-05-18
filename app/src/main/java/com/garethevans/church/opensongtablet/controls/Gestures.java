package com.garethevans.church.opensongtablet.controls;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

// This holds the references for gestures
// The gestures are based on CommonControls (along with hot zones and pedal actions)
// The actions are called in GestureListener

public class Gestures {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Gestures";

    // This deals with on screen gestures (double tap and long press)
    private final MainActivityInterface mainActivityInterface;
    private String doubleTap;
    private String longPress;
    private boolean swipeEnabled;
    private float scrollDistance;

    // Initialise the class
    public Gestures(Context c) {
        // On start, set up the arrays
        mainActivityInterface = (MainActivityInterface) c;
        getPreferences();
    }

    private void getPreferences() {
        doubleTap = mainActivityInterface.getPreferences().getMyPreferenceString("gestureDoubleTap","editsong");
        longPress = mainActivityInterface.getPreferences().getMyPreferenceString("gestureLongPress","");
        swipeEnabled = mainActivityInterface.getPreferences().getMyPreferenceBoolean("swipeForSongs",true);
        scrollDistance = mainActivityInterface.getPreferences().getMyPreferenceFloat("scrollDistance", 0.7f);
    }

    public void setScrollDistance(float scrollDistance) {
        this.scrollDistance = scrollDistance;
    }

    // The getters and setters called by other classes

    public String getDoubleTap() {
        return doubleTap;
    }
    public String getLongPress() {
        return longPress;
    }
    public boolean getSwipeEnabled() {
        return swipeEnabled;
    }
    public float getScrollDistance() {
        return scrollDistance;
    }
    public void setPreferences(String which, String val) {
        if (which.equals("gestureDoubleTap")) {
            this.doubleTap = val;
        } else {
            this.longPress = val;
        }
        // Save the preference
        mainActivityInterface.getPreferences().setMyPreferenceString(which, val);
    }
    public void setPreferences(String which, boolean bool) {
        if ("swipeForSongs".equals(which)) {
            this.swipeEnabled = bool;
        }
        // Save the preference
        mainActivityInterface.getPreferences().setMyPreferenceBoolean(which, bool);
    }

}

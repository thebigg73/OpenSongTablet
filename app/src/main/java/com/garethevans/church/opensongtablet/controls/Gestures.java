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
    private boolean swipeEnabled, pdfLandscapeView, pdfAllVisible, pdfStart, pdfEnd;
    private float scrollDistance;

    // Initialise the class
    public Gestures(Context c) {
        // On start, set up the arrays
        mainActivityInterface = (MainActivityInterface) c;
        getPreferences();
    }

    public void getPreferences() {
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

    // If we enable this option and have a PDF file and are in landscape and full autoscale
    // We need to note this as we have to disable normal swipe gestures side to side
    // This is set in Performance Mode before loading a song as false
    // Then if the criteria matches, it is set to true
    // The PerformanceGestures check this (as do other scrolling tasks in the recyclerview adapters)
    public void setPdfLandscapeView(boolean pdfLandscapeView) {
        this.pdfLandscapeView = pdfLandscapeView;
    }
    public boolean getPdfLandscapeView() {
        return pdfLandscapeView;
    }
    public void setPdfAllVisible(boolean pdfAllVisible) {
        this.pdfAllVisible = pdfAllVisible;
    }
    public boolean getPdfAllVisible() {
        return pdfAllVisible;
    }
    public void setPdfStart(boolean pdfStart) {
        // From the recyclerView when we have a scroll position of 0
        this.pdfStart = pdfStart;
    }
    public boolean getPdfStart() {
        return pdfStart;
    }
    public void setPdfEnd(boolean pdfEnd) {
        // From the recyclerView when we have a scroll position of the max allowed for the view
        this.pdfEnd = pdfEnd;
    }
    public boolean getPdfEnd() {
        return pdfEnd;
    }
}

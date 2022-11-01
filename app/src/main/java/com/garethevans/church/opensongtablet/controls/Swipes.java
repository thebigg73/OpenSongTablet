package com.garethevans.church.opensongtablet.controls;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class Swipes {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Swipes";

    private final MainActivityInterface mainActivityInterface;
    private int viewWidth;            // The width of the view on screen
    private int viewHeight;           // The height of the view on screem
    private int minWidth;             // The min distance required for an active swipe
    private int maxWidth;             // The max distance required for an active swipe
    private int minHeight;            // The min value a user can set for an acceptable y axis deviation
    private int maxHeight;            // The max value a user can set for an acceptable y axis deviation
    private int minTime;              // The min time (ms) a user can set for a swipe total time
    private int maxTime;              // The max time (ms) a user can set for a swipe total time
    private int widthPx;              // User distance in px
    private int heightPx;             // User height in px
    private int timeMs;               // User time in ms
    private float swipeWidth;         // User preference for min swipe distance required(ratio of width/screenwidth)
    private float swipeHeight;        // User preference for max swipe y axis deviation to ignore (ratio of height:screenwidth)
    private float swipeTime;          // User preference for acceptable swipe time (ratio of time:maxTime);

    public Swipes(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        loadPreferences();
    }

    // The setters
    public void setSizes(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        minWidth = (int) (0.2f * viewWidth);
        maxWidth = (int) (0.8f * viewWidth);
        minHeight = (int) (0.05f * viewHeight);
        maxHeight = (int) (0.5f * viewHeight);
        minTime = 100;
        maxTime = 800;
        widthPx = (int)(swipeWidth * viewWidth);
        heightPx = (int)(swipeHeight * viewHeight);
        timeMs = (int)(swipeTime * maxTime);
    }
    public void loadPreferences() {
        swipeWidth = mainActivityInterface.getPreferences().getMyPreferenceFloat("swipeWidth",0.4f);
        swipeHeight = mainActivityInterface.getPreferences().getMyPreferenceFloat("swipeHeight",0.2f);
        swipeTime = mainActivityInterface.getPreferences().getMyPreferenceFloat("swipeTime",0.6f);
    }
    public void fixWidth(int width) {
        widthPx = width;
        swipeWidth = (float)widthPx/(float)viewWidth;
        mainActivityInterface.getPreferences().setMyPreferenceFloat("swipeWidth",swipeWidth);
    }
    public void fixHeight(int height) {
        heightPx = height;
        swipeHeight = (float)heightPx/(float)viewHeight;
        mainActivityInterface.getPreferences().setMyPreferenceFloat("swipeHeight",swipeHeight);
    }
    public void fixTime(int time) {
        timeMs = time;
        swipeTime = (float)timeMs/(float)maxTime;
        mainActivityInterface.getPreferences().setMyPreferenceFloat("swipeTime",swipeTime);
    }

    // The getters
    public int getWidthPx() {
        return widthPx;
    }
    public int getMinWidth() {
        return minWidth;
    }
    public int getMaxWidth() {
        return maxWidth;
    }
    public int getHeightPx() {
        return heightPx;
    }
    public int getMinHeight() {
        return minHeight;
    }
    public int getMaxHeight() {
        return maxHeight;
    }
    public int getTimeMs() {
        return timeMs;
    }
    public int getMinTime() {
        return minTime;
    }
    public int getMaxTime() {
        return maxTime;
    }

}

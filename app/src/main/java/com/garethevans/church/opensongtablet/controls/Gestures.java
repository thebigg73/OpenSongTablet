package com.garethevans.church.opensongtablet.controls;

import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

// This holds the references for gestures
// The actions are called in GestureListener

public class Gestures {

    // This deals with on screen gestures (double tap and long press)
    private ArrayList<String> gestures;
    private ArrayList<String> gestureDescriptions;
    private String doubleTap;
    private String longPress;
    private boolean swipeEnabled;
    private float scrollDistance;

    // Initialise the class
    public Gestures(Context c) {
        // On start, set up the arrays
        setGestures();
        setGestureDescriptions(c);
        getPreferences(c,(MainActivityInterface) c);
    }
    private void setGestures() {
        // Set the gesture names for storing in preferences
        // (used to be int for position, but was a pain in adding/removing options)

        gestures = new ArrayList<>();
        gestures.add("");
        gestures.add("songmenu");
        gestures.add("setmenu");
        gestures.add("editsong");
        gestures.add("addtoset");
        gestures.add("refreshsong");
        gestures.add("autoscroll");
        gestures.add("metronome");
        gestures.add("pad");
        gestures.add("autoscroll_pad");
        gestures.add("autoscroll_metronome");
        gestures.add("metronome_pad");
        gestures.add("autoscroll_metronome_pad");
    }
    private void setGestureDescriptions(Context c) {
        // Set up the dropdown options
        String startstop = " (" + c.getString(R.string.start) + " / " + c.getString(R.string.stop) + ")";
        String showhide = " (" + c.getString(R.string.show) + " / " + c.getString(R.string.hide) + ")";

        String autoscroll = c.getString(R.string.autoscroll);
        String metronome = c.getString(R.string.metronome);
        String pad = c.getString(R.string.pad);

        gestureDescriptions = new ArrayList<>();
        gestureDescriptions.add(c.getString(R.string.off));
        gestureDescriptions.add(c.getString(R.string.open_song_menu));
        gestureDescriptions.add(c.getString(R.string.set_current) + showhide);
        gestureDescriptions.add(c.getString(R.string.edit_song));
        gestureDescriptions.add(c.getString(R.string.add_song_to_set));
        gestureDescriptions.add(c.getString(R.string.refresh_song));
        gestureDescriptions.add(autoscroll + startstop);
        gestureDescriptions.add(metronome + startstop);
        gestureDescriptions.add(pad + startstop);
        gestureDescriptions.add(autoscroll + " + " + pad + startstop);
        gestureDescriptions.add(autoscroll + " + " + metronome + startstop);
        gestureDescriptions.add(metronome + " + " + pad + startstop);
        gestureDescriptions.add(autoscroll + " + " + metronome + " + " + pad + startstop);
    }
    private void getPreferences(Context c, MainActivityInterface mainActivityInterface) {
        doubleTap = mainActivityInterface.getPreferences().getMyPreferenceString(c,"gestureDoubleTap","editsong");
        longPress = mainActivityInterface.getPreferences().getMyPreferenceString(c,"gestureLongPress","addtoset");
        swipeEnabled = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "swipeForSongs",true);
        scrollDistance = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "scrollDistance", 0.7f);
    }
    public void setScrollDistance(float scrollDistance) {
        this.scrollDistance = scrollDistance;
    }

    // The getters and setters called by other classes
    public ArrayList<String> getGestures() {
        return gestures;
    }
    public ArrayList<String> getGestureDescriptions() {
        return gestureDescriptions;
    }
    public String getGestureFromDescription(String description) {
        int pos = gestureDescriptions.indexOf(description);
        return gestures.get(pos);
    }
    public String getDescriptionFromGesture(String gesture) {
        int pos = gestures.indexOf(gesture);
        return gestureDescriptions.get(pos);
    }
    public int getPositionFromText(String description) {
        return gestureDescriptions.indexOf(description);
    }
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
    public void setPreferences(Context c, MainActivityInterface mainActivityInterface, String which, String val) {
        if (which.equals("gestureDoubleTap")) {
            this.doubleTap = val;
        } else {
            this.longPress = val;
        }
        // Save the preference
        mainActivityInterface.getPreferences().setMyPreferenceString(c, which, val);
    }
    public void setPreferences(Context c, MainActivityInterface mainActivityInterface, String which, boolean bool) {
        if ("swipeForSongs".equals(which)) {
            this.swipeEnabled = bool;
        }
        // Save the preference
        mainActivityInterface.getPreferences().setMyPreferenceBoolean(c, which, bool);
    }

}

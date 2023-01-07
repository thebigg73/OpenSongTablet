package com.garethevans.church.opensongtablet.controls;

import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

// This holds the references for gestures
// The actions are called in GestureListener

public class Gestures {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Gestures";

    // This deals with on screen gestures (double tap and long press)
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private ArrayList<String> gestures;
    private ArrayList<String> gestureDescriptions;
    private String doubleTap;
    private String longPress;
    private boolean swipeEnabled;
    private float scrollDistance;

    // Initialise the class
    public Gestures(Context c) {
        // On start, set up the arrays
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        setGestures();
        setGestureDescriptions();
        getPreferences();
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
        gestures.add("togglescale");
        gestures.add("scrolldown");
        gestures.add("scrollup");
        gestures.add("next");
        gestures.add("previous");
        gestures.add("refreshsong");
        gestures.add("autoscroll");
        gestures.add("metronome");
        gestures.add("pad");
        gestures.add("autoscroll_pad");
        gestures.add("autoscroll_metronome");
        gestures.add("metronome_pad");
        gestures.add("autoscroll_metronome_pad");
    }
    private void setGestureDescriptions() {
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
        gestureDescriptions.add(c.getString(R.string.scale_style));
        gestureDescriptions.add(c.getString(R.string.scroll_down));
        gestureDescriptions.add(c.getString(R.string.scroll_up));
        gestureDescriptions.add(c.getString(R.string.next));
        gestureDescriptions.add(c.getString(R.string.previous));
        gestureDescriptions.add(c.getString(R.string.refresh_song));
        gestureDescriptions.add(autoscroll + startstop);
        gestureDescriptions.add(metronome + startstop);
        gestureDescriptions.add(pad + startstop);
        gestureDescriptions.add(autoscroll + " + " + pad + startstop);
        gestureDescriptions.add(autoscroll + " + " + metronome + startstop);
        gestureDescriptions.add(metronome + " + " + pad + startstop);
        gestureDescriptions.add(autoscroll + " + " + metronome + " + " + pad + startstop);
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

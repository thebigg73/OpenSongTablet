package com.garethevans.church.opensongtablet.controls;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.performance.PerformanceGestures;

public class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private final String TAG = "GestureListener";
    private final int swipeMinimumDistance, swipeMaxDistanceYError, swipeMinimumVelocity;
    private final MainActivityInterface mainActivityInterface;
    private final PerformanceGestures performanceGestures;

    public GestureListener(MainActivityInterface mainActivityInterface, PerformanceGestures performanceGestures,
                    int swipeMinimumDistance, int swipeMaxDistanceYError, int swipeMinimumVelocity) {
        this.mainActivityInterface = mainActivityInterface;
        this.performanceGestures = performanceGestures;
        this.swipeMinimumDistance = swipeMinimumDistance;
        this.swipeMaxDistanceYError = swipeMaxDistanceYError;
        this.swipeMinimumVelocity = swipeMinimumVelocity;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.d(TAG, "onDoubleTapEvent()");
        return performAction(mainActivityInterface.getGestures().getDoubleTap());
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
        Log.d(TAG, "onLongPress()");
        performAction(mainActivityInterface.getGestures().getLongPress());
    }

    private boolean performAction(String whichAction) {
        // Gestures available from controls>Gestures
        switch (whichAction) {
            case "songmenu":
                performanceGestures.songMenu();
                break;
            case "setmenu":
                performanceGestures.setMenu();
                break;
            case "editsong":
                performanceGestures.editSong();
                break;
            case "addtoset":
                performanceGestures.addToSet();
                break;
            case "refreshsong":
                performanceGestures.loadSong();
                break;
            case "autoscroll":
                performanceGestures.toggleAutoscroll();
                break;
            case "pad":
                performanceGestures.togglePad();
                break;
            case "metronome":
                performanceGestures.toggleMetronome();
                break;
            case "autoscroll_pad":
                performanceGestures.togglePad();
                performanceGestures.toggleAutoscroll();
                break;
            case "autoscroll_metronome":
                performanceGestures.toggleMetronome();
                performanceGestures.toggleAutoscroll();
                break;
            case "metronome_pad":
                performanceGestures.togglePad();
                performanceGestures.toggleMetronome();
                break;
            case "autoscroll_metronome_pad":
                performanceGestures.togglePad();
                performanceGestures.toggleMetronome();
                performanceGestures.toggleAutoscroll();
                break;
            default:
                Log.d(TAG,"Gesture not recognised");
                return false;
        }
        return true;
    }

    // The listener for swiping between songs
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > swipeMaxDistanceYError) {
                return false;

            } else if (mainActivityInterface.getGestures().getSwipeEnabled() &&
                    e1.getX() - e2.getX() > swipeMinimumDistance
                    && Math.abs(velocityX) > swipeMinimumVelocity) {
                Log.d(TAG, "Right to Left");
                mainActivityInterface.getDisplayPrevNext().setSwipeDirection("R2L");
                //mainActivityInterface.getDisplayPrevNext().swipeNextCheck();
                mainActivityInterface.getDisplayPrevNext().moveToNext();
                return true;

            } else if (mainActivityInterface.getGestures().getSwipeEnabled() &&
                    e2.getX() - e1.getX() > swipeMinimumDistance
                    && Math.abs(velocityX) > swipeMinimumVelocity) {
                Log.d(TAG, "Left to Right");
                mainActivityInterface.getDisplayPrevNext().setSwipeDirection("L2R");
                //mainActivityInterface.getDisplayPrevNext().swipePrevCheck();
                mainActivityInterface.getDisplayPrevNext().moveToPrev();
                return true;

            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public int getSwipeMinimumDistance() {
        return swipeMinimumDistance;
    }

    public int getSwipeMaxDistanceYError() {
        return swipeMaxDistanceYError;
    }

    public int getSwipeMinimumVelocity() {
        return swipeMinimumVelocity;
    }
}

package com.garethevans.church.opensongtablet.controls;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private final int swipeMinimumDistance, swipeMaxDistanceYError, swipeMinimumVelocity;
    private final MainActivityInterface mainActivityInterface;
    private boolean doubleTapping, longPressing;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "GestureListener";

    public GestureListener(MainActivityInterface mainActivityInterface,
                           int swipeMinimumDistance, int swipeMaxDistanceYError, int swipeMinimumVelocity) {
        this.mainActivityInterface = mainActivityInterface;
        this.swipeMinimumDistance = swipeMinimumDistance;
        this.swipeMaxDistanceYError = swipeMaxDistanceYError;
        this.swipeMinimumVelocity = swipeMinimumVelocity;
        doubleTapping = false;
        longPressing = false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // Make sure this isn't sent while we are already dealing with it
        Log.d(TAG,"onDoubleTap");
        if (!doubleTapping && !longPressing) {
            doubleTapping = true;
            // Turn off record of double tapping in 200ms
            new Handler(Looper.getMainLooper()).postDelayed(() -> doubleTapping = false,800);
            return performAction(mainActivityInterface.getGestures().getDoubleTap());
        } else {
            doubleTapping = false;
            return true;
        }
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
        if (doubleTapping) {
            longPressing = false;
        } else if (!longPressing) {
            longPressing = true;
            Log.d(TAG,"onLongPress");
            // Turn off record of long pressing in 200ms
            new Handler(Looper.getMainLooper()).postDelayed(() -> longPressing = false,800);
            performAction(mainActivityInterface.getGestures().getLongPress());
        }
    }

    private boolean performAction(String whichAction) {
        // Send the gesture to the Performance gestures to run
        // isLongPress is for page button, not the long press gesture!
        mainActivityInterface.getPerformanceGestures().doAction(whichAction,false);
        return true;
    }

    // The listener for swiping between songs
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        boolean okForPrev = (mainActivityInterface.getGestures().getPdfLandscapeView() &&
                (mainActivityInterface.getGestures().getPdfAllVisible() ||
                        mainActivityInterface.getGestures().getPdfStart())) ||
                !mainActivityInterface.getGestures().getPdfLandscapeView();

        boolean okForNext = (mainActivityInterface.getGestures().getPdfLandscapeView() &&
                (mainActivityInterface.getGestures().getPdfAllVisible() ||
                        mainActivityInterface.getGestures().getPdfEnd())) ||
                !mainActivityInterface.getGestures().getPdfLandscapeView();

        try {
            if (Math.abs(e1.getY() - e2.getY()) > swipeMaxDistanceYError) {
                return false;

            } else if (mainActivityInterface.getGestures().getSwipeEnabled() &&
                    okForNext &&
                    e1.getX() - e2.getX() > swipeMinimumDistance
                    && Math.abs(velocityX) > swipeMinimumVelocity) {
                    mainActivityInterface.getDisplayPrevNext().setSwipeDirection("R2L");
                    mainActivityInterface.getDisplayPrevNext().moveToNext();
                return true;

            } else if (mainActivityInterface.getGestures().getSwipeEnabled() &&
                    okForPrev &&
                    e2.getX() - e1.getX() > swipeMinimumDistance
                    && Math.abs(velocityX) > swipeMinimumVelocity) {
                mainActivityInterface.getDisplayPrevNext().setSwipeDirection("L2R");
                mainActivityInterface.getDisplayPrevNext().moveToPrev();
                return true;

            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

}

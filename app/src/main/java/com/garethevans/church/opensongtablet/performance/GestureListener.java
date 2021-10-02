package com.garethevans.church.opensongtablet.performance;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import com.garethevans.church.opensongtablet.customviews.MyZoomLayout;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private final String TAG = "GestureListener";
    private ScrollView songScrollView;
    private HorizontalScrollView horizontalScrollView;
    private final MyZoomLayout zoomLayout;
    private final int swipeMinimumDistance, swipeMaxDistanceYError, swipeMinimumVelocity;
    private boolean okToRegisterGesture = true;
    private boolean wasScaling = false;
    //private final PerformanceGestures performanceGestures;
    //private final int doubleTapAction;
    private final MainActivityInterface mainActivityInterface;

    /*GestureListener(ScrollView songScrollView, HorizontalScrollView horizontalScrollView,
                    int swipeMinimumDistance, int swipeMaxDistanceYError, int swipeMinimumVelocity,
                    boolean oktoregistergesture, int doubleTapAction, PerformanceGestures performanceGestures) {
        this.songScrollView = songScrollView;
        this.horizontalScrollView = horizontalScrollView;
        this.swipeMinimumDistance = swipeMinimumDistance;
        this.swipeMaxDistanceYError = swipeMaxDistanceYError;
        this.swipeMinimumVelocity = swipeMinimumVelocity;
        this.oktoregistergesture = oktoregistergesture;
        this.doubleTapAction = doubleTapAction;
        this.performanceGestures = performanceGestures;
    }*/

    /*GestureListener(MyZoomLayout zoomLayout, int swipeMinimumDistance, int swipeMaxDistanceYError, int swipeMinimumVelocity,
                    boolean oktoregistergesture, int doubleTapAction, PerformanceGestures performanceGestures) {
        this.zoomLayout = zoomLayout;
        this.swipeMinimumDistance = swipeMinimumDistance;
        this.swipeMaxDistanceYError = swipeMaxDistanceYError;
        this.swipeMinimumVelocity = swipeMinimumVelocity;
        this.oktoregistergesture = oktoregistergesture;
        this.doubleTapAction = doubleTapAction;
        this.performanceGestures = performanceGestures;
    }*/

    GestureListener(MainActivityInterface mainActivityInterface, MyZoomLayout zoomLayout,
                    int swipeMinimumDistance, int swipeMaxDistanceYError, int swipeMinimumVelocity) {
        this.mainActivityInterface = mainActivityInterface;
        this.zoomLayout = zoomLayout;
        this.swipeMinimumDistance = swipeMinimumDistance;
        this.swipeMaxDistanceYError = swipeMaxDistanceYError;
        this.swipeMinimumVelocity = swipeMinimumVelocity;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.d(TAG, "onDoubleTapEvent()");
        // Decide what the double tap action is
        // 1 = both menus
        // 2 = edit song
        // 3 = add to set
        // 4 = redraw
        // 5 = start/stop autoscroll
        // 6 = start/stop pad
        // 7 = start/stop metronome
        // 8 = start/stop autoscroll + pad
        // 9 = start/stop autoscroll + metronome
        //10 = start/stop autoscroll + pad + metronome
        //11 = start/stop autoscroll + pad + metronome
        // 0/else = off (highest menu item)

        // First test conditions
        if (okToRegisterGesture) {

            /*// Now find out which gesture we've gone for
            switch (doubleTapAction) {
                case 1:
                    performanceGestures.gesture1();
            }*/
        }

        /*if (!PerformanceFragment.wasScaling) {
            if (e.getAction() == 1) {
                //Do your action on double tap
                Log.e("onDoubleTapEvent", e.getAction() + "");
            }
        }*/
        return super.onDoubleTapEvent(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        Log.d(TAG,"onFling()");
        boolean justFlingScroll = false;

        try {
            if (Math.abs(e1.getY() - e2.getY()) > swipeMaxDistanceYError) {
                justFlingScroll = true;
            }
            if (!justFlingScroll && e1.getX() - e2.getX() > swipeMinimumDistance
                    && Math.abs(velocityX) > swipeMinimumVelocity) {
                Log.i("d", "Right to Left");
                mainActivityInterface.getDisplayPrevNext().setSwipeDirection("R2L");
                mainActivityInterface.getDisplayPrevNext().moveToNext();
                justFlingScroll = false;
            } else if (!justFlingScroll && e2.getX() - e1.getX() > swipeMinimumDistance
                    && Math.abs(velocityX) > swipeMinimumVelocity) {
                Log.i("d", "Left to Right");
                mainActivityInterface.getDisplayPrevNext().setSwipeDirection("L2R");
                mainActivityInterface.getDisplayPrevNext().moveToPrev();
                justFlingScroll = false;
            }
            if (justFlingScroll) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

}

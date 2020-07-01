package com.garethevans.church.opensongtablet.performance;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

public class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private ScrollView songScrollView;
    private HorizontalScrollView horizontalScrollView;
    private int swipeMinimumDistance, swipeMaxDistanceYError, swipeMinimumVelocity;

    GestureListener(ScrollView songScrollView, HorizontalScrollView horizontalScrollView,
                    int swipeMinimumDistance, int swipeMaxDistanceYError, int swipeMinimumVelocity) {
        this.songScrollView = songScrollView;
        this.horizontalScrollView = horizontalScrollView;
        this.swipeMinimumDistance = swipeMinimumDistance;
        this.swipeMaxDistanceYError = swipeMaxDistanceYError;
        this.swipeMinimumVelocity = swipeMinimumVelocity;
    }
    
    @Override
    public boolean onDown(MotionEvent e) {
        if (!PerformanceFragment.wasScaling) {
            songScrollView.fling(0);
            horizontalScrollView.fling(0);
            songScrollView.scrollTo((int) e.getX(), (int) e.getY());
        }
        return true;
    }
/*

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (!PerformanceFragment.wasScaling) {
            Log.e("onSingleTapUp", e.getAction() + "");
        }
        return super.onSingleTapUp(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (!PerformanceFragment.wasScaling) {
            Log.e("onSingleTapConfirmed", e.getAction() + "");
            //Do your action on single tap
        }
        return super.onSingleTapConfirmed(e);
    }
*/

/*
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (!PerformanceFragment.wasScaling) {
            Log.e("onDoubleTap", e.getAction() + "");
        }
        return true;
    }
*/

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        if (!PerformanceFragment.wasScaling) {
            if (e.getAction() == 1) {
                //Do your action on double tap
                Log.e("onDoubleTapEvent", e.getAction() + "");
            }
        }
        return super.onDoubleTapEvent(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (!PerformanceFragment.wasScaling) {
            songScrollView.scrollBy(0, (int) distanceY);
            horizontalScrollView.scrollBy((int) distanceX, 0);
        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        boolean justFlingScroll = false;
        try {
            if (!PerformanceFragment.wasScaling) {
                if (Math.abs(e1.getY() - e2.getY()) > swipeMaxDistanceYError) {
                    justFlingScroll = true;
                }
                if (!justFlingScroll && e1.getX() - e2.getX() > swipeMinimumDistance
                        && Math.abs(velocityX) > swipeMinimumVelocity) {
                    Log.i("d", "Right to Left");
                    PerformanceFragment.loadNextSong = true;
                    PerformanceFragment.R2L = true;
                    songScrollView.fling(0);
                    horizontalScrollView.fling(0);
                    justFlingScroll = false;
                } else if (!justFlingScroll && e2.getX() - e1.getX() > swipeMinimumDistance
                        && Math.abs(velocityX) > swipeMinimumVelocity) {
                    Log.i("d", "Left to Right");
                    PerformanceFragment.loadPrevSong = true;
                    PerformanceFragment.R2L = false;
                    songScrollView.fling(0);
                    horizontalScrollView.fling(0);
                    justFlingScroll = false;
                }

                if (justFlingScroll) {
                    horizontalScrollView.fling((int) -velocityX);
                    songScrollView.fling((int) -velocityY);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}

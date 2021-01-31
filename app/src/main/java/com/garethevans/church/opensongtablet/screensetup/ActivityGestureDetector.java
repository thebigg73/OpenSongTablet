package com.garethevans.church.opensongtablet.screensetup;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class ActivityGestureDetector extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d("ActivityGestureDetector","onDown()");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d("ActivityGestureDetector","onShowPress()");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d("ActivityGestureDetector","onSingleTapUp()");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d("ActivityGestureDetector","onScroll()");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d("ActivityGestureDetector","onLongPress()");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d("ActivityGestureDetector","onFling()");
        return false;
    }
}

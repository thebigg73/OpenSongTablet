package com.garethevans.church.opensongtablet.performance;

import android.view.ScaleGestureDetector;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class PinchToZoomGestureListener implements ScaleGestureDetector.OnScaleGestureListener {

    private RelativeLayout pageHolder;

    PinchToZoomGestureListener(RelativeLayout pageHolder) {
        this.pageHolder = pageHolder;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        PerformanceFragment.wasScaling = true;

        PerformanceFragment.scaleFactor *= detector.getScaleFactor();
        pageHolder.setPivotX(0);
        pageHolder.setPivotY(0);
        pageHolder.setScaleX(PerformanceFragment.scaleFactor);
        pageHolder.setScaleY(PerformanceFragment.scaleFactor);
        int viewWidth = (int)(PerformanceFragment.songViewWidth * PerformanceFragment.scaleFactor + 0.5);
        int viewHeight = (int)(PerformanceFragment.songViewHeight * PerformanceFragment.scaleFactor + 0.5);

        ViewGroup.LayoutParams slp = pageHolder.getLayoutParams();
        slp.width = viewWidth;
        slp.height = viewHeight;
        pageHolder.setLayoutParams(slp);

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        PerformanceFragment.wasScaling = true;
        if (PerformanceFragment.songViewWidth == 0) {
            PerformanceFragment.songViewWidth = pageHolder.getWidth();
        }
        if (PerformanceFragment.songViewHeight == 0) {
            PerformanceFragment.songViewHeight = pageHolder.getHeight();
        }
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        PerformanceFragment.wasScaling = false;
    }
}
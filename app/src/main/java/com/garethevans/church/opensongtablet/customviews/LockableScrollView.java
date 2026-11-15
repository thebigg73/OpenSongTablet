package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class LockableScrollView extends ScrollView {

    private boolean scrollEnabled = true; // Default to scrollable or change as needed

    public LockableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollEnabled(boolean enabled) {
        this.scrollEnabled = enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // If scroll is disabled, do not intercept touches; pass them straight down to DrawNotes
        if (!scrollEnabled) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!scrollEnabled) {
            return false;
        }
        return super.onTouchEvent(ev);
    }
}
package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;


public class MyZoomLayout extends NestedScrollView {

    boolean isUserTouching = false;

    public MyZoomLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyZoomLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyZoomLayout(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (RuntimeException e) {
                // ignore runtime error
                int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
                setMeasuredDimension(widthSize, heightSize);
            }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN ||
        ev.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
            isUserTouching = true;
            this.performClick();
        } else if (ev.getAction() == MotionEvent.ACTION_UP ||
                ev.getAction() == MotionEvent.ACTION_BUTTON_RELEASE) {
            isUserTouching = false;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public int getScrollPos() {
        return this.getScrollY();
    }
    public boolean getIsUserTouching() {
        return isUserTouching;
    }
    public void setIsUserTouching(boolean isUserTouching) {
        this.isUserTouching = isUserTouching;
    }
}

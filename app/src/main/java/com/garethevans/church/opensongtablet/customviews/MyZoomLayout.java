package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import androidx.annotation.Nullable;

public class MyZoomLayout extends FrameLayout {

    boolean isUserTouching = false, isScaling = false, isScrolling = false, isFirstScrollEvent = true;
    private final ScaleGestureDetector scaleDetector;
    private final String TAG = "MyZoomLayout";
    private float scaleFactor = 1.0f;
    private float focusX = 0;
    private float focusY = 0;
    private float minScale;
    private final GestureDetector gestureDetector;
    private final OverScroller overScroller;
    private int viewWidth, viewHeight, maxScrollX, maxScrollY, overShootX, overShootY,
            songWidth, songHeight, originalSongWidth, originalSongHeight;
    private boolean scrolledToTop, scrolledToBottom;

    public MyZoomLayout(Context c, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(c, attrs, defStyleAttr);
        scaleDetector = new ScaleGestureDetector(c, new ScaleListener());
        gestureDetector = new GestureDetector(c, new GestureListener());
        overScroller = new OverScroller(c,new AccelerateDecelerateInterpolator());
        this.setOverScrollMode(OVER_SCROLL_ALWAYS);
        setClipChildren(false);
    }

    public MyZoomLayout(Context c, @Nullable AttributeSet attrs) {
        super(c, attrs);
        scaleDetector = new ScaleGestureDetector(c, new ScaleListener());
        gestureDetector = new GestureDetector(c, new GestureListener());
        overScroller = new OverScroller(c,new AccelerateDecelerateInterpolator());
        this.setOverScrollMode(OVER_SCROLL_ALWAYS);
        setClipChildren(false);
    }

    public MyZoomLayout(Context c) {
        super(c);
        scaleDetector = new ScaleGestureDetector(c, new ScaleListener());
        gestureDetector = new GestureDetector(c, new GestureListener());
        overScroller = new OverScroller(c,new AccelerateDecelerateInterpolator());
        this.setOverScrollMode(OVER_SCROLL_ALWAYS);
        setClipChildren(false);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        try {
            super.dispatchDraw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (overScroller.computeScrollOffset()) {
            scrollTo(overScroller.getCurrX(), overScroller.getCurrY());
            scrolledToBottom = overScroller.getCurrY()>=maxScrollY;
            scrolledToTop = overScroller.getCurrY()<=0;
            invalidate();
        }
        if (isUserTouching && isScaling) {
            canvas.save();
            getChildAt(0).setTranslationX(0);
            getChildAt(0).setTranslationY(0);
            getChildAt(0).setPivotX(0);
            getChildAt(0).setPivotY(0);
            getChildAt(0).setScaleX(scaleFactor);
            getChildAt(0).setScaleY(scaleFactor);
            canvas.scale(scaleFactor, scaleFactor, focusX, focusY);
            calculateMaxScrolls();
            isScaling = false;
            Log.d(TAG, "scaleFactor=" + scaleFactor + "  focusX=" + focusX + "  focusY=" + focusY);
            canvas.restore();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        //overShootX = (int) ((float)viewWidth/32f);
        overShootX = 0;
        //overShootY = (int) ((float)viewHeight/32f);
        overShootY = 0;
        overScroller.notifyHorizontalEdgeReached(0,maxScrollX,overShootX);
        overScroller.notifyVerticalEdgeReached(0,maxScrollY,overShootY);
    }

    private void calculateMaxScrolls() {
        maxScrollX = Math.max(0,songWidth - viewWidth);
        maxScrollY = Math.max(0,songHeight - viewHeight);
        minScale = Math.min((float)viewWidth/(float)originalSongWidth,(float)viewHeight/(float)originalSongHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN ||
                event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
            isUserTouching = true;
            isFirstScrollEvent = true;
            calculateMaxScrolls();
            this.performClick();
        } else if (event.getAction() == MotionEvent.ACTION_UP ||
                event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE ||
                event.getAction() == MotionEvent.ACTION_CANCEL) {
            isUserTouching = false;
            isFirstScrollEvent = true;
        }

        boolean b0 = gestureDetector.onTouchEvent(event);
        boolean b1 = scaleDetector.onTouchEvent(event);
        return b0 || b1;
        //return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public int getScrollPos() {
        return getScrollY();
    }

    public boolean getIsUserTouching() {
        return isUserTouching;
    }

    public void setIsUserTouching(boolean isUserTouching) {
        this.isUserTouching = isUserTouching;
    }

    public void autoscrollTo(float newY) {
        scrollTo(0,(int)(newY));
    }

    public float getScaleFactor() {
        // So we can adjust the autoscroll
        return scaleFactor;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.d(TAG, "onScale() called");
            scaleFactor *= detector.getScaleFactor();
            if (scaleFactor > 2) {
                scaleFactor = 2;
            }
            if (scaleFactor < minScale) {
                scaleFactor = minScale;
            }

            scrollTo(0,0);
            songWidth = (int) (originalSongWidth * scaleFactor);
            songHeight = (int) (originalSongHeight * scaleFactor);
            focusX = detector.getFocusX();
            focusY = detector.getFocusY();
            isScaling = true;
            isUserTouching = true;
            invalidate();

            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // First scroll event should be ignored because of bad distanceX
            if (isFirstScrollEvent) {
                isFirstScrollEvent = false;
                return true;
            }
            isUserTouching = true;
            isScrolling = true;

            // Decide if distanceX and distanceY are valid
            if (getScrollX()<0 || getScrollX()+distanceX<0) {
                setScrollX(0);
                distanceX = 0;
            } else if (getScrollX()+distanceX>maxScrollX) {
                setScrollX(maxScrollX);
                distanceX = 0;
            }
            if (getScrollY()<0 || getScrollY()+distanceY<0) {
                setScrollY(0);
                distanceY = 0;
            } else if (getScrollY()+distanceY>maxScrollY) {
                setScrollY(maxScrollY);
                distanceY = 0;
            }

            // Scroll if we need to
            scrollBy((int)distanceX,(int)distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            overScroller.fling(getScrollX(), getScrollY(), (int)-velocityX, (int)-velocityY,
                    0, maxScrollX, 0, maxScrollY,overShootX,overShootY);
            invalidate();
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            if (!overScroller.isFinished()) {
                overScroller.forceFinished(true);
            }
            return true;
        }
    }

    public void animateScrollBy(float scrollFloat, boolean scrollDown) {
        float velocityY = (viewHeight*scrollFloat)/0.3f;
        if (scrollDown) {
            overScroller.fling(getScrollX(), getScrollY(), 0, (int)velocityY,
                    0, maxScrollX, 0, maxScrollY,overShootX,overShootY);
        } else {
            overScroller.fling(getScrollX(), getScrollY(), 0, (int)-velocityY,
                    0, maxScrollX, 0, maxScrollY,overShootX,overShootY);
        }
        invalidate();
    }

    public void setPageSize(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    public void setSongSize(int songWidth, int songHeight) {
        this.songWidth = songWidth;
        this.songHeight = songHeight;
        this.originalSongWidth = songWidth;
        this.originalSongHeight = songHeight;
        resetLayout();
    }

    private void resetLayout() {
        scaleFactor = 1.0f;
        focusX = 0;
        focusY = 0;
        calculateMaxScrolls();
        scrollTo(0,0);
        scrolledToBottom = getScrollY()==maxScrollY;
        scrolledToTop = true;
    }

    public boolean getScrolledToTop() {
        return scrolledToTop;
    }
    public boolean getScrolledToBottom() {
        return scrolledToBottom;
    }
}

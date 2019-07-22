package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;


public class MyZoomLayout extends RelativeLayout implements ScaleGestureDetector.OnScaleGestureListener {

    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 4.0f;
    // We can be in one of these 3 states
    private int NONE = 0;
    private int DRAG = 1;
    private int ZOOM = 2;
    int mode = NONE;
    private float scale = 1.0f;
    private float lastScaleFactor = 0f;
    private float startX = 0f;
    private float startY = 0f;
    private float dx = 0f;
    private float dy = 0f;
    private float prevDx = 0f;
    private float prevDy = 0f;


    public MyZoomLayout(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
        init(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onScale(final ScaleGestureDetector scaleGestureDetector) {
        Log.d("d","onScale");
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d("d","motionEvent="+motionEvent);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if (scale > MIN_ZOOM) {
                            mode = DRAG;
                            startX = motionEvent.getX() - prevDx;
                            startY = motionEvent.getY() - prevDy;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            dx = motionEvent.getX() - startX;
                            dy = motionEvent.getY() - startY;
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mode = ZOOM;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_UP:
                        mode = NONE;
                        prevDx = dx;
                        prevDy = dy;
                        break;
                    default:
                        mode = NONE;
                        prevDx = dx;
                        prevDy = dy;
                        break;
                }
                scaleGestureDetector.onTouchEvent(motionEvent);

                if ((mode == DRAG && scale >= MIN_ZOOM) || mode == ZOOM) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    float maxDx = (child().getWidth() - (child().getWidth() / scale)) / 2 * scale;
                    float maxDy = (child().getHeight() - (child().getHeight() / scale)) * scale;
                    dx = Math.min(Math.max(dx, -maxDx), maxDx);
                    dy = Math.min(Math.max(dy, -maxDy), maxDy);
                    applyScaleAndTranslation();
                }
                return true;
            }
        });
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        Log.d("d","onScaleBegin");
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        Log.d("d","onScaleEnd");

    }

    @SuppressLint("ClickableViewAccessibility")
    public void init(Context context) {
        final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(context, this);
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d("d","motionEvent="+motionEvent);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if (scale > MIN_ZOOM) {
                            mode = DRAG;
                            startX = motionEvent.getX() - prevDx;
                            startY = motionEvent.getY() - prevDy;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            dx = motionEvent.getX() - startX;
                            dy = motionEvent.getY() - startY;
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mode = ZOOM;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = DRAG;
                        break;
                    case MotionEvent.ACTION_UP:
                        mode = NONE;
                        prevDx = dx;
                        prevDy = dy;
                        break;
                    default:
                        mode = NONE;
                        prevDx = dx;
                        prevDy = dy;
                        break;
                }
                scaleDetector.onTouchEvent(motionEvent);

                if ((mode == DRAG && scale >= MIN_ZOOM) || mode == ZOOM) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    float maxDx = (child().getWidth() - (child().getWidth() / scale)) / 2 * scale;
                    float maxDy = (child().getHeight() - (child().getHeight() / scale)) * scale;
                    dx = Math.min(Math.max(dx, -maxDx), maxDx);
                    dy = Math.min(Math.max(dy, -maxDy), maxDy);
                    applyScaleAndTranslation();
                }
                return true;
            }
        });
    }

    private View child() {
        return getChildAt(0);
    }

    private void applyScaleAndTranslation() {
        child().setScaleX(scale);
        child().setScaleY(scale);
        child().setTranslationX(dx);
        child().setTranslationY(dy);
    }
}
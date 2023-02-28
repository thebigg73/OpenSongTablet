package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class MyRecyclerView extends RecyclerView {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyRecyclerView";
    private MainActivityInterface mainActivityInterface;
    private boolean isUserTouching;
    private boolean scrolledToTop=true;
    private boolean scrolledToBottom=false;
    private int maxScrollY;
    private GestureDetector gestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private float floatScrollPos;

    // For pinch to zoom hopefully
    private float mScaleFactor = 1.f;
    private float maxWidth = 0.0f;
    private float maxHeight = 0.0f;
    private float mLastTouchX;
    private float mLastTouchY;
    private float mPosX;
    private float mPosY;
    private float width;
    private float height;
    private int mActivePointerId;
    private boolean allowPinchToZoom;
    private boolean gestureControl;


    private final LinearInterpolator linearInterpolator = new LinearInterpolator();
    private final ScrollListener scrollListener;
    private final ItemTouchListener itemTouchListener;

    RecyclerView.SmoothScroller smoothScroller;

    public MyRecyclerView(@NonNull Context context) {
        super(context);
        scrollListener = new ScrollListener();
        itemTouchListener = new ItemTouchListener();
        addOnScrollListener(scrollListener);
        addOnItemTouchListener(itemTouchListener);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
        setClipChildren(false);
        setClipToPadding(false);
        setItemAnimator(null);
        floatScrollPos = 0;
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    public MyRecyclerView(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        scrollListener = new ScrollListener();
        itemTouchListener = new ItemTouchListener();
        addOnScrollListener(scrollListener);
        addOnItemTouchListener(itemTouchListener);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
        setClipChildren(false);
        floatScrollPos = 0;
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    public void setAllowPinchToZoom(boolean allowPinchToZoom) {
        this.allowPinchToZoom = allowPinchToZoom;
    }

    public void setSectionScrollSize(int availableHeight, int maxSectionSize) {

    }
    public void initialiseRecyclerView(MainActivityInterface mainActivityInterface) {
        this.mainActivityInterface = mainActivityInterface;
    }

    public void setUserTouching(boolean isUserTouching) {
        this.isUserTouching = isUserTouching;
    }

    public boolean getIsUserTouching() {
        return isUserTouching;
    }
    public void scrollToTop() {
        try {
            scrollToPosition(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        scrolledToTop = true;
        scrolledToBottom = false;
        floatScrollPos = 0;
    }

    public void doScrollBy(float dy, int duration) {
        // Only do this if we aren't touching the screen!
        // Because scroll is an int, but getting passed a float, we need to keep track
        // If we fall behind (or ahead), add this on when it becomes above 1f

        int currentActual = (int)floatScrollPos;

        float currentWanted = floatScrollPos;

        // How far behind are we?  Add this on
        float behind = (currentWanted - currentActual);

        floatScrollPos += dy;

        int scrollAmount = (int)(dy+behind);
        if (!isUserTouching) {
            smoothScrollBy(0,scrollAmount,linearInterpolator,duration);
        }
    }

    public void smoothScrollTo(Context c, LayoutManager layoutManager, int position) {
        smoothScroller = new LinearSmoothScroller(c) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_END;
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 100f / displayMetrics.densityDpi;
            }

        };
        smoothScroller.setTargetPosition(position);
        layoutManager.startSmoothScroll(smoothScroller);
    }


    public void setMaxScrollY(int maxScrollY) {
        this.maxScrollY = maxScrollY;
    }

    public boolean getScrolledToTop() {
        return scrolledToTop;
    }

    public boolean getScrolledToBottom() {
        return scrolledToBottom;
    }

    public void setGestureDetector(GestureDetector gestureDetector) {
        this.gestureDetector = gestureDetector;
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        public ScrollListener() {
            super();
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (isUserTouching) {
                // User has scrolled, so check for actionbar and prev/next
                onTouchAction();
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (isUserTouching) {
                floatScrollPos = floatScrollPos + dy;
            }
            scrolledToTop = recyclerView.computeVerticalScrollOffset() == 0;
            scrolledToBottom = (maxScrollY-recyclerView.computeVerticalScrollOffset()) <= 1;
        }
    }
    private void onTouchAction() {
        if (mainActivityInterface!=null) {
            mainActivityInterface.getDisplayPrevNext().showAndHide();
            mainActivityInterface.updateOnScreenInfo("showcapo");
            mainActivityInterface.showActionBar();
        }
    }

    private class ItemTouchListener extends RecyclerView.SimpleOnItemTouchListener {

        public ItemTouchListener() {
            super();
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            final int action = e.getAction();
            if (allowPinchToZoom) {
                mScaleDetector.onTouchEvent(e);
            }
            float x;
            float y;
            int pointerIndex, pointerId;

            int INVALID_POINTER_ID = -1;
            switch (action & MotionEvent.ACTION_MASK) {
                case (MotionEvent.ACTION_DOWN):
                    isUserTouching = true;
                    x = e.getX();
                    y = e.getY();
                    mLastTouchX = x;
                    mLastTouchY = y;
                    mActivePointerId = e.getPointerId(0);
                    break;

                case (MotionEvent.ACTION_MOVE):
                    isUserTouching = true;
                    pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    x = e.getX(pointerIndex);
                    y = e.getY(pointerIndex);
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    if (mPosX > 0.0f) {
                        mPosX = 0.0f;
                    } else if (mPosX < maxWidth) {
                        mPosX = maxWidth;
                    }

                    if (mPosY > 0.0f) {
                        mPosY = 0.0f;
                    } else if (mPosY < maxHeight) {
                        mPosY = maxHeight;
                    }

                    mLastTouchX = x;
                    mLastTouchY = y;

                    invalidate();
                    break;

                case (MotionEvent.ACTION_BUTTON_PRESS):
                    isUserTouching = true;
                    break;

                case (MotionEvent.ACTION_UP):
                    isUserTouching = false;
                    pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    pointerId = e.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        if (newPointerIndex!=1) {
                            mLastTouchX = e.getX(newPointerIndex);
                            mLastTouchY = e.getY(newPointerIndex);
                            mActivePointerId = e.getPointerId(newPointerIndex);
                        }
                    }
                    break;

                case (MotionEvent.ACTION_CANCEL):
                case (MotionEvent.ACTION_BUTTON_RELEASE):
                    isUserTouching = false;
                    mActivePointerId = INVALID_POINTER_ID;
                    break;

            }

//            if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
//                isUserTouching = true;
//            } else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_BUTTON_RELEASE || e.getAction() == MotionEvent.ACTION_CANCEL) {
//                isUserTouching = false;
//            }

            // Deal with performance mode gestures
            if (gestureDetector!=null) {
                return gestureDetector.onTouchEvent(e);
            } else {
                return super.onInterceptTouchEvent(rv, e);
            }
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(1.0f, Math.min(mScaleFactor, 3.0f));
            maxWidth = width - (width * mScaleFactor);
            maxHeight = height - (height * mScaleFactor);
            invalidate();
            return true;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);
        canvas.restore();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        canvas.save();
        if (mScaleFactor == 1.0f) {
            mPosX = 0.0f;
            mPosY = 0.0f;
        }
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);
        super.dispatchDraw(canvas);
        canvas.restore();
        invalidate();
    }

    public void toggleScale() {
        if (!gestureControl) {
            isUserTouching = false;
            gestureControl = true;
            scrollTo(0,0);
            // Set a timer to enable it again
            postDelayed(() -> gestureControl = false,600);
            // This is called from a gesture or page button
            // It toggles between current zoom and zoom off
            mScaleFactor = 1.0f;
            mPosX = 0;
            mPosY = 0;
            invalidate();
        }
    }
}

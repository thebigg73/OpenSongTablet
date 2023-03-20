package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
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

import java.util.ArrayList;

public class MyRecyclerView extends RecyclerView  implements RecyclerView.SmoothScroller.ScrollVectorProvider{

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
    private RecyclerView.SmoothScroller smoothScroller;

    RecyclerView.SmoothScroller smoothScroller;

    public MyRecyclerView(@NonNull Context context) {
        super(context);
        scrollListener = new ScrollListener();
        itemTouchListener = new ItemTouchListener();
        addOnScrollListener(scrollListener);
        addOnItemTouchListener(itemTouchListener);
        setOverScrollMode(OVER_SCROLL_NEVER);
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
        setOverScrollMode(OVER_SCROLL_NEVER);
        setClipChildren(false);
        floatScrollPos = 0;
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    public void setAllowPinchToZoom(boolean allowPinchToZoom) {
        this.allowPinchToZoom = allowPinchToZoom;
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
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 100f / displayMetrics.densityDpi;
            }

        };
        smoothScroller.setTargetPosition(position);
        layoutManager.startSmoothScroll(smoothScroller);
    }

    public void doSmoothScrollTo(RecyclerLayoutManager recyclerLayoutManager, int position) {
        try {
            // Try to work out scrolling amount
            // Get the top of each view by taking running total of the heights
            // e.g. child 1 is at the height of child 0
            // Child 2 is at the height of child 0 + child 1
            int rollingTotal = 0;
            ArrayList<Integer> yPositions = new ArrayList<>();
            for (int y : recyclerLayoutManager.getChildSizes()) {
                yPositions.add(rollingTotal);
                rollingTotal += y;
            }

            int viewHeight;
            int spaceAbove = (int)((1.0f - mainActivityInterface.getPreferences().getMyPreferenceFloat("stageModeScale",0.8f)) * getHeight());
            // Work out the space to try to leave above the view
            if (recyclerLayoutManager.getChildSizes().size()>position) {
                viewHeight = recyclerLayoutManager.getChildSizes().get(position);
                spaceAbove = (int) ((1.0f - mainActivityInterface.getPreferences().getMyPreferenceFloat("stageModeScale",0.8f)) * (getHeight() - viewHeight));
            }

            // Get the current scroll position, the position we need to get to and how far this is
            int currScroll = recyclerLayoutManager.getScrollY();
            int scrollToY = yPositions.get(position);
            int scrollAmount = scrollToY - currScroll - spaceAbove;
            Log.d(TAG,"scrollAmount = " + scrollAmount);

            // Do the scrolling, but not if small movement
            if (Math.abs(scrollAmount) > 25) {
                smoothScrollBy(0, scrollAmount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        return null;
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

package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.pdf.PDFPageAdapter;

import java.util.ArrayList;

public class MyRecyclerView extends RecyclerView  implements RecyclerView.SmoothScroller.ScrollVectorProvider {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyRecyclerView";
    private MainActivityInterface mainActivityInterface;
    private boolean isUserTouching;
    private boolean scrolledToTop=true;
    private boolean scrolledToStart=true;
    // Also left
    private boolean scrolledToBottom=false;
    private boolean scrolledToEnd=false;
    private int maxScrollY;
    private GestureDetector gestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private float floatScrollXPos, floatScrollYPos;

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
    private final Handler checkScrollPosIsRight1 = new Handler();
    private final Handler checkScrollPosIsRight2 = new Handler();
    private Runnable checkPosRunnable;
    @SuppressWarnings("FieldCanBeLocal")
    private final int smoothScrollDuration = 120;

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
        floatScrollXPos = 0;
        floatScrollYPos = 0;
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        setFitsSystemWindows(false);
    }

    public MyRecyclerView(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        scrollListener = new ScrollListener();
        itemTouchListener = new ItemTouchListener();
        addOnScrollListener(scrollListener);
        addOnItemTouchListener(itemTouchListener);
        setOverScrollMode(OVER_SCROLL_NEVER);
        setClipChildren(false);
        floatScrollXPos = 0;
        floatScrollYPos = 0;
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        setFitsSystemWindows(false);
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

    // Is the scroll at the top/start
    public void scrollToTop() {
        try {
            scrollToPosition(0);
        } catch (Exception e) {
            Log.d(TAG,"Can't scrollToPosition(0)");
        }
        scrolledToTop = true;
        scrolledToBottom = false;
        floatScrollXPos = 0;
        floatScrollYPos = 0;
    }

    // Scroll by a set number of pixels (horizontally or vertically)
    public void doScrollBy(float dxy, int duration) {
        // Only do this if we aren't touching the screen!
        // Because scroll is an int, but getting passed a float, we need to keep track
        // If we fall behind (or ahead), add this on when it becomes above 1f

        int currentXActual = (int)floatScrollXPos;
        int currentYActual = (int)floatScrollYPos;

        float currentXWanted = floatScrollXPos;
        float currentYWanted = floatScrollYPos;

        // How far behind are we?  Add this on
        float behindX = (currentXWanted - currentXActual);
        float behindY = (currentYWanted - currentYActual);

        int scrollXAmount = 0;
        int scrollYAmount = 0;
        if (mainActivityInterface.getGestures().getPdfLandscapeView()) {
            // We must be scrolling horizontally
            floatScrollXPos += dxy;
            scrollXAmount = (int)(dxy+behindX);
        } else {
            // We must be scrolling vertically (normal)
            floatScrollYPos += dxy;
            scrollYAmount = (int)(dxy+behindY);
        }

        if (!isUserTouching) {
            smoothScrollBy(scrollXAmount,scrollYAmount,linearInterpolator,duration);
        } else {
            mainActivityInterface.getDisplayPrevNext().showAndHide();
            mainActivityInterface.updateOnScreenInfo("showhide");
            mainActivityInterface.showActionBar();
        }
    }

    public void smoothScrollTo(Context c, LayoutManager layoutManager, int position) {
        SmoothScroller smoothScroller = new LinearSmoothScroller(c) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override
            protected int getHorizontalSnapPreference() {
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
        // Cancel any post delayed check
        if (checkPosRunnable!=null) {
            checkScrollPosIsRight1.removeCallbacks(checkPosRunnable);
            checkScrollPosIsRight2.removeCallbacks(checkPosRunnable);
        }

        try {
            // Try to work out scrolling amount
            // If vertical, get the top of each view by taking running total of the heights
            // e.g. child 1 is at the height of child 0
            // Child 2 is at the height of child 0 + child 1
            // If horizontal, get the left of each view by taking running totals of the widths
            // e.g. child 1 is at the width of child 1
            int rollingHTotal = 0;
            int rollingVTotal = 0;
            ArrayList<Integer> xPositions = new ArrayList<>();
            ArrayList<Integer> yPositions = new ArrayList<>();
            for (int z=0; z<recyclerLayoutManager.getChildVSizes().size(); z++) {
                int hSize = recyclerLayoutManager.getChildHSizes().get(z);
                int vSize = recyclerLayoutManager.getChildVSizes().get(z);
                xPositions.add(rollingHTotal);
                yPositions.add(rollingVTotal);
                rollingHTotal += hSize;
                rollingVTotal += vSize;
            }

            int viewWidth;
            int viewHeight;
            int spaceRight = (int)((1.0f - mainActivityInterface.getPreferences().getMyPreferenceFloat("stageModeScale",0.8f)) * getWidth());
            int spaceAbove = (int)((1.0f - mainActivityInterface.getPreferences().getMyPreferenceFloat("stageModeScale", 0.8f)) * getHeight());

            // Work out the space to try to leave above/to the right the view
            if (recyclerLayoutManager.getChildVSizes().size()>position) {
                viewWidth = recyclerLayoutManager.getChildHSizes().get(position);
                viewHeight = recyclerLayoutManager.getChildVSizes().get(position);
                spaceRight = (getWidth() - viewWidth)/2;
                spaceAbove = (int) ((1.0f - mainActivityInterface.getPreferences().getMyPreferenceFloat("stageModeScale",0.8f)) * (getHeight() - viewHeight));
            }

            // Get the current scroll position, the position we need to get to and how far this is
            int currXScroll = computeHorizontalScrollOffset();
            int currYScroll = computeVerticalScrollOffset();
            if (yPositions.size()>0 && yPositions.size()>position) {
                int scrollToX = xPositions.get(position);
                int scrollToY = yPositions.get(position);
                int scrollXAmount = scrollToX - currXScroll - spaceRight;
                int scrollYAmount = scrollToY - currYScroll - spaceAbove;

                if (mainActivityInterface.getGestures().getPdfLandscapeView()) {
                    scrollYAmount = 0;
                } else {
                    scrollXAmount = 0;
                }

                // Do the scrolling, but not if small movement
                if (Math.abs(scrollXAmount) > 25 || Math.abs(scrollYAmount) > 25) {
                    smoothScrollBy(scrollXAmount, scrollYAmount,linearInterpolator,smoothScrollDuration);
                }

                // Check the chosen view is actually visible
                checkPosRunnable = () -> {
                    int firstPosition = recyclerLayoutManager.findFirstCompletelyVisibleItemPosition();
                    if (firstPosition>position || firstPosition==-1) {
                        // Snap to that position
                        Log.d(TAG,"SNAP");
                        recyclerLayoutManager.scrollToPosition(position);
                    }
                };

                // Post delayed checks that the view is fully visible
                // Run twice as a backup catch
                checkScrollPosIsRight1.postDelayed(checkPosRunnable,(int)(1.5f * smoothScrollDuration));
                checkScrollPosIsRight2.postDelayed(checkPosRunnable,(int)(2.5f * smoothScrollDuration));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void setMaxScrollY(int maxScrollY) {
        this.maxScrollY = maxScrollY;
    }

    public boolean getScrolledToTop() {
        if (mainActivityInterface.getGestures().getPdfLandscapeView()) {
            // Scrolling horizontally
            return scrolledToStart;
        } else {
            // Scrolling vertically
            return scrolledToTop;
        }
    }

    public boolean getScrolledToBottom() {
        if (mainActivityInterface.getGestures().getPdfLandscapeView()) {
            // Scrolling horizontally
            return scrolledToEnd;
        } else {
            // Scrolling vertically
            return scrolledToBottom;
        }
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
            if (mainActivityInterface!=null) {
                super.onScrolled(recyclerView, dx, dy);

                if (isUserTouching) {
                    floatScrollXPos = floatScrollXPos + dx;
                    floatScrollYPos = floatScrollYPos + dy;
                }
                scrolledToTop = recyclerView.computeVerticalScrollOffset() == 0;
                scrolledToStart = recyclerView.computeHorizontalScrollExtent() == 0;
                scrolledToBottom = (maxScrollY - recyclerView.computeVerticalScrollOffset() - (4f * mainActivityInterface.getDisplayDensity())) <= 1;
                if (recyclerView.getLayoutManager() != null && recyclerView.getLayoutManager() instanceof RecyclerLayoutManager) {
                    int firstVisiblePosition = ((RecyclerLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    int lastVisiblePosition = ((RecyclerLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                    scrolledToStart = firstVisiblePosition == 0;
                    scrolledToEnd = lastVisiblePosition == mainActivityInterface.getSong().getPdfPageCount() - 1;
                } else {
                    scrolledToEnd = false;
                    scrolledToStart = false;
                }
                mainActivityInterface.getGestures().setPdfStart(scrolledToStart);
                mainActivityInterface.getGestures().setPdfEnd(scrolledToEnd);
            }
        }
    }
    private void onTouchAction() {
        if (mainActivityInterface!=null) {
            mainActivityInterface.getDisplayPrevNext().showAndHide();
            mainActivityInterface.updateOnScreenInfo("showcapo");
            mainActivityInterface.showActionBar();
            mainActivityInterface.getHotZones().checkScrollButtonOn(null,this);
        }
    }

    private class ItemTouchListener extends RecyclerView.SimpleOnItemTouchListener {

        public ItemTouchListener() {
            super();
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            final int action = e.getAction();
            if (allowPinchToZoom && mainActivityInterface.getMode()!=null &&
                    mainActivityInterface.getSong()!=null &&
                    !(mainActivityInterface.getMode().equals(getContext().getString(R.string.mode_stage)) &&
                            mainActivityInterface.getSong().getFiletype()!=null &&
                            mainActivityInterface.getSong().getFiletype().equals("XML"))) {
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

            if (mainActivityInterface!=null) {
                mainActivityInterface.getHotZones().checkScrollButtonOn(null, MyRecyclerView.this);
            }

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
            scrollToTop();
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

    public int getScrollPos() {
        float scale = 1f;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getAdapter()!=null && getAdapter() instanceof PDFPageAdapter) {
            scale = ((PDFPageAdapter) getAdapter()).getPdfHorizontalScale();
        }
        int pos;
        if (getLayoutManager()!=null && getLayoutManager() instanceof RecyclerLayoutManager) {
            try {
                if (mainActivityInterface.getGestures().getPdfLandscapeView()) {
                    // Scrolling horizontally
                    pos = computeHorizontalScrollExtent();
                } else {
                    // Scrolling vertically
                    pos = computeVerticalScrollOffset();
                }
            } catch (Exception e) {
                pos = 0;
            }
        } else {
            pos = 0;
        }
        pos = (int)(pos * scale);
        mainActivityInterface.getGestures().setPdfStart(scrolledToStart);
        mainActivityInterface.getGestures().setPdfEnd(scrolledToEnd);
        return pos;
    }
    public int getScrollYMax() {
        if (getLayoutManager()!=null && getLayoutManager() instanceof RecyclerLayoutManager) {
            try {
                if (mainActivityInterface.getGestures().getPdfLandscapeView()) {
                    // Scrolling horizontally
                    return ((RecyclerLayoutManager) getLayoutManager()).getScrollXMax();
                } else {
                    // Scrolling vertically
                    return ((RecyclerLayoutManager) getLayoutManager()).getScrollYMax();
                }
            } catch (Exception e) {
                return 0;
            }
        } else {
            return 0;
        }
    }


    @Override
    public void scrollTo(int x, int y) {
        Log.d(TAG, "MyRecyclerView does not support scrolling to an absolute position.");
        // Either don't call super here or call just for some phones, or try catch it. From default implementation we have removed the Runtime Exception trown
    }

}

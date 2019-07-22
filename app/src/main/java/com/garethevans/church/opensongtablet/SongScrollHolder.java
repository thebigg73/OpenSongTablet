package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.Scroller;

import java.util.Locale;
import java.util.jar.Attributes;

public class SongScrollHolder extends FrameLayout {

    static final int ANIMATED_SCROLL_GAP = 250;
    public static final String TWO_DSCROLL_VIEW_CAN_HOST_ONLY_ONE_DIRECT_CHILD = "TwoDScrollView can host only one direct child";
    private long mLastScroll;
    private Scroller mScroller;

    // Position of the last motion event.
    private float mLastMotionY;
    private float mLastMotionX;

    /**
     * True if the user is currently dragging this TwoDScrollView around. This is
     * not the same as 'is being flinged', which can be checked by
     * mScroller.isFinished() (flinging begins when the user lifts his finger).
     */

    private boolean mIsBeingDragged = false;

    /**
     * Determines speed during touch scrolling
     */

    private VelocityTracker mVelocityTracker;

    /**
     * Whether arrow scrolling is animated.
     */

    private int mTouchSlop;
    private int mMinimumVelocity;
    private float mScale = 1f;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector gestureDetector;

    private boolean mEnableScaling = false;     // scaling is buggy when you click on child views

    public SongScrollHolder(Context context) {
        super(context);
        initTwoDScrollView();
    }

    public SongScrollHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTwoDScrollView();
    }

    public SongScrollHolder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initTwoDScrollView();
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }

        final int length = getVerticalFadingEdgeLength();

        if (getScrollY() < length) {
            return getScrollY() / (float) length;
        }
        return 1.0f;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }

        final int length = getVerticalFadingEdgeLength();
        final int bottomEdge = getHeight() - getPaddingBottom();
        final int span = getChildAt(0).getBottom() - getScrollY() - bottomEdge;

        if (span < length) {
            return span / (float) length;
        }

        return 1.0f;
    }

    @Override
    protected float getLeftFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }

        final int length = getHorizontalFadingEdgeLength();

        if (getScrollX() < length) {
            return getScrollX() / (float) length;
        }
        return 1.0f;
    }

    @Override
    protected float getRightFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }
        final int length = getHorizontalFadingEdgeLength();
        final int rightEdge = getWidth() - getPaddingRight();
        final int span = getChildAt(0).getRight() - getScrollX() - rightEdge;

        if (span < length) {
            return span / (float) length;
        }
        return 1.0f;
    }

    private void initTwoDScrollView() {
        mScroller = new Scroller(getContext());
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();

        if (mEnableScaling) {
            gestureDetector = new GestureDetector(getContext(), new GestureListener());
        }
    }

    @Override
    public void addView(View child) {

        if (getChildCount() > 0) {
            throw new IllegalStateException(TWO_DSCROLL_VIEW_CAN_HOST_ONLY_ONE_DIRECT_CHILD);
        }

        super.addView(child);

        if (mEnableScaling) {
            createScaleGestureDetector(child);
        }
    }

    @Override
    public void addView(View child, int index) {

        if (getChildCount() > 0) {
            throw new IllegalStateException(TWO_DSCROLL_VIEW_CAN_HOST_ONLY_ONE_DIRECT_CHILD);
        }

        super.addView(child, index);

        if (mEnableScaling) {
            createScaleGestureDetector(child);
        }
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {

        if (getChildCount() > 0) {
            throw new IllegalStateException(TWO_DSCROLL_VIEW_CAN_HOST_ONLY_ONE_DIRECT_CHILD);
        }

        super.addView(child, params);

        if (mEnableScaling) {
            createScaleGestureDetector(child);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException(TWO_DSCROLL_VIEW_CAN_HOST_ONLY_ONE_DIRECT_CHILD);
        }

        super.addView(child, index, params);

        if (mEnableScaling) {
            createScaleGestureDetector(child);
        }
    }

    private void createScaleGestureDetector(final View childLayout) {

        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scale = 1 - detector.getScaleFactor();
                float prevScale = mScale;
                mScale += scale;
                if (mScale < 0.5f) // Minimum scale condition:
                    mScale = 0.5f;

                if (mScale > 1.5f) // Maximum scale condition:
                    mScale = 1.5f;

                ScaleAnimation scaleAnimation =
                        new ScaleAnimation(1f / prevScale, 1f / mScale,
                                1f / prevScale, 1f /
                                mScale,
                                detector.getFocusX(), detector.getFocusY());
                scaleAnimation.setDuration(0);
                scaleAnimation.setFillAfter(true);
                childLayout.startAnimation(scaleAnimation);
                return true;
            }
        });
    }

    /**
     *
     @return Returns true this TwoDScrollView can be scrolled
     */

    private boolean canScroll() {
        View child = getChildAt(0);
        if (child != null) {
            int childHeight = child.getHeight();
            int childWidth = child.getWidth();

            return (getHeight() < childHeight + getPaddingTop() + getPaddingBottom()) ||
            (getWidth() < childWidth + getPaddingLeft() + getPaddingRight());
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */
        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }

        if (!canScroll()) {
            mIsBeingDragged = false;
            return false;
        }

        final float y = ev.getY();
        final float x = ev.getX();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */
                /*
                 * Locally do absolute value. mLastMotionY is set to the y value
                 * of the down event.
                 */
                final int yDiff = (int) Math.abs(y - mLastMotionY);
                final int xDiff = (int) Math.abs(x - mLastMotionX);

                if (yDiff > mTouchSlop && xDiff > mTouchSlop) {
                    mIsBeingDragged = true;
                }
                break;

            case MotionEvent.ACTION_DOWN:
                /* Remember location of down touch */
                mLastMotionY = y;
                mLastMotionX = x;
                /*
                 * If being flinged and user touches the screen, initiate drag...
                 * otherwise don't.  mScroller.isFinished should be false when
                 * being flinged.
                 */

                mIsBeingDragged = !mScroller.isFinished();
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                /* Release the drag */
                mIsBeingDragged = false;
                break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */

        return mIsBeingDragged;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
            // Don't handle edge touches immediately -- they may actually belong to one of our descendants.
            return false;
        }

        if (!canScroll()) {
            return false;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction();
        final float y = ev.getY();
        final float x = ev.getX();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */

                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                // Remember where the motion event started
                mLastMotionY = y;
                mLastMotionX = x;
                break;

            case MotionEvent.ACTION_MOVE:
                // Scroll to follow the motion event
                int deltaX = (int) (mLastMotionX - x);
                int deltaY = (int) (mLastMotionY - y);
                mLastMotionX = x;
                mLastMotionY = y;
                if (deltaX < 0) {
                    if (getScrollX() < 0) {
                        deltaX = 0;
                    }
                } else if (deltaX > 0) {
                    final int rightEdge = getWidth() - getPaddingRight();
                    final int availableToScroll = getChildAt(0).getRight() - getScrollX() - rightEdge;

                    if (availableToScroll > 0) {
                        deltaX = Math.min(availableToScroll, deltaX);
                    } else {
                        deltaX = 0;
                    }
                }

                if (deltaY < 0) {
                    if (getScrollY() < 0) {
                        deltaY = 0;
                    }
                } else if (deltaY > 0) {
                    final int bottomEdge = getHeight() - getPaddingBottom();
                    final int availableToScroll = getChildAt(0).getBottom() - getScrollY() - bottomEdge;

                    if (availableToScroll > 0) {
                        deltaY = Math.
                                min(availableToScroll, deltaY);
                    } else {
                        deltaY = 0;
                    }
                }

                if (deltaY != 0 && deltaX != 0)
                scrollBy(deltaX, deltaY);
                break;

            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int initialXVelocity = (int) velocityTracker.getXVelocity();
                int initialYVelocity = (int) velocityTracker.getYVelocity();
                if ((Math.abs(initialXVelocity) + Math.abs(initialYVelocity) > mMinimumVelocity) && getChildCount() > 0) {
                    fling(-initialXVelocity, -initialYVelocity);
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
        }
        return true;
    }

    @Override

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mEnableScaling) {
            super.dispatchTouchEvent(event);
            mScaleDetector.onTouchEvent(event);

            // scale event positions according to scale before passing them to children
            Log.d("d", String.format(Locale.getDefault(), "Position (%.2f,%.2f) ScrollOffset (%d,%d) Scale %.2f",
                                    event.getX(), event.getY(), getScrollX(), getScrollY(),
                                    mScale));
            event.setLocation((getScrollX() + event.getX()) *
                    mScale, (getScrollY() + event.getY()) * mScale);
            return gestureDetector.onTouchEvent(event);
        } else {
            return super.dispatchTouchEvent(event);
        }
    }

    public boolean fullScroll(int directionVert, int directionHorz) {
        int scrollAmountY = 0, scrollAmountX = 0;

        //  vertical
        switch (directionVert) {
            case View.FOCUS_UP:
                scrollAmountY = -getScrollY();
                break;

            case View.FOCUS_DOWN:
                int count = getChildCount();
                if (count > 0) {
                    View view = getChildAt(count - 1);
                    scrollAmountY = (view.getBottom() - getHeight()) - getScrollY();
                }
                break;
        }

        // horizontal
        switch (directionHorz) {
            case View.FOCUS_LEFT:
                scrollAmountX = -getScrollX();
                break;

            case View.FOCUS_RIGHT:
                int count = getChildCount();
                if (count > 0) {
                    View view = getChildAt(count - 1);
                    scrollAmountX = (view.getRight() - getWidth()) - getScrollX();
                }
                break;
        }

        boolean handled = (scrollAmountX != 0) && (scrollAmountY != 0);

        if (handled)
            doScroll(scrollAmountX, scrollAmountY);

        return handled;
    }


    private void doScroll(int deltaX, int deltaY) {
        if (deltaX != 0 && deltaY != 0) {
            smoothScrollBy(deltaX, deltaY);
        }
    }

    public final void smoothScrollBy(int dx, int dy) {

        long duration = AnimationUtils.currentAnimationTimeMillis() - mLastScroll;

        if (duration > ANIMATED_SCROLL_GAP) {
            mScroller.startScroll(getScrollX(), getScrollY(), dx, dy);
            awakenScrollBars(
                    mScroller.getDuration());
            invalidate();
        } else {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            scrollBy(dx, dy);
        }

        mLastScroll = AnimationUtils.currentAnimationTimeMillis();
    }

    public final void smoothScrollTo(int x, int y) {
        smoothScrollBy(x - getScrollX(), y - getScrollY());
    }

    /**
     * <p>The scroll range of a scroll view is the overall height of all of its
     * children.</p>
     */

    @Override
    protected int computeVerticalScrollRange() {
        int count = getChildCount();
        return count == 0 ? getHeight() : (getChildAt(0)).getBottom();
    }

    @Override
    protected int computeHorizontalScrollRange() {
        int count = getChildCount();
        return count == 0 ? getWidth() : (getChildAt(0)).getRight();
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;
        childWidthMeasureSpec =
                getChildMeasureSpec(parentWidthMeasureSpec, getPaddingLeft() + getPaddingRight(), lp.width);
        childHeightMeasureSpec = MeasureSpec.
                makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.leftMargin + lp.rightMargin, MeasureSpec.UNSPECIFIED);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }
    @Override

    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            // This is called at drawing time by ViewGroup.  We don't want to
            // re-show the scrollbars at this point, which scrollTo will do,
            // so we replicate most of scrollTo here.
            //
            //         It's a little odd to call onScrollChanged from inside the drawing.
            //
            //         It is, except when you remember that computeScroll() is used to
            //         animate scrolling. So unless we want to defer the onScrollChanged()
            //         until the end of the animated scrolling, we don't really have a
            //         choice here.
            //
            //         I agree.  The alternative, which I think would be worse, is to post
            //         something and tell the subclasses later.  This is bad because there
            //         will be a window where mScrollX/Y is different from what the app
            //         thinks it is.
            //

            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (getChildCount() > 0) {
                View child = getChildAt(0);
                scrollTo(clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), child.getWidth()),
                        clamp(y, getHeight() - getPaddingBottom() - getPaddingTop(), child.getHeight()));
            } else {
                scrollTo(x, y);
            }

            if (oldX != getScrollX() && oldY != getScrollY()) {
                onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
            }

            // Keep on drawing until the animation has finished.
            postInvalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // Calling this with the present values causes it to re-clam them
        scrollTo(getScrollX(), getScrollY());
    }

    public void fling(int velocityX, int velocityY) {

        if (getChildCount() > 0) {
            int height = getHeight() - getPaddingBottom() - getPaddingTop();
            int bottom = getChildAt(0).getHeight();
            int width = getWidth() - getPaddingRight() - getPaddingLeft();
            int right = getChildAt(0).getWidth();
            mScroller.fling(getScrollX(), getScrollY(), velocityX, velocityY, 0, right - width, 0, bottom - height);
            awakenScrollBars(
                    mScroller.getDuration());
            invalidate();
        }
    }

    public void scrollTo(int x, int y) {
        // we rely on the fact the View.scrollBy calls scrollTo.
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            x = clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), child.getWidth());
            y = clamp(y, getHeight() - getPaddingBottom() - getPaddingTop(), child.getHeight());

            if (x != getScrollX() && y != getScrollY()) {
                super.scrollTo(x, y);
            }
        }
    }

    private int clamp(int n, int my, int child) {

        if (my >= child && n < 0) {

/* my >= child is this case:
             *
--------------- me ---------------
             *
------ child ------
             * or
             *
--------------- me ---------------
             *
------ child ------
             * or
             *
--------------- me ---------------
             *
------ child ------
             *
             * n < 0 is this case:
             *
------ me ------
             *
-------- child --------
             *
-- mScrollX --
             */

            return 0;
        }

        if ((my+n) > child) {

/* this case:
             *
------ me ------
             *
------ child ------
             *
-- mScrollX --
             */
            return child-my;
        }
        return n;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // double tap fired.
            return true;
        }
    }
}

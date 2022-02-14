package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class MyRecyclerView extends RecyclerView {

    private final String TAG = "MyRecyclerView";
    private final MainActivityInterface mainActivityInterface;
    private boolean isUserTouching;
    private boolean scrolledToTop=true;
    private boolean scrolledToBottom=false;
    private int maxScrollY;
    private GestureDetector gestureDetector;

    private final LinearInterpolator linearInterpolator = new LinearInterpolator();
    private int scrollPosition;
    private ScrollListener scrollListener;
    private ItemTouchListener itemTouchListener;

    public MyRecyclerView(@NonNull Context c) {
        super(c);
        mainActivityInterface = (MainActivityInterface) c;
        scrollListener = new ScrollListener();
        itemTouchListener = new ItemTouchListener();
        addOnScrollListener(scrollListener);
        addOnItemTouchListener(itemTouchListener);
        this.setOverScrollMode(OVER_SCROLL_ALWAYS);
        setClipChildren(false);
        scrollPosition = 0;
    }

    public void removeListeners() {
        removeOnScrollListener(scrollListener);
        scrollListener = null;
        removeOnItemTouchListener(itemTouchListener);
        itemTouchListener = null;
    }

    public MyRecyclerView(@NonNull Context c, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(c, attrs);
        mainActivityInterface = (MainActivityInterface) c;
        addOnScrollListener(new ScrollListener());
        addOnItemTouchListener(new ItemTouchListener());
        this.setOverScrollMode(OVER_SCROLL_ALWAYS);
        setClipChildren(false);
        scrollPosition = 0;
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
            scrollPosition = scrollPosition + dy;
            scrolledToTop = scrollPosition == 0;
            scrolledToBottom = (maxScrollY-scrollPosition) <= 0;
        }
    }


    private class ItemTouchListener extends RecyclerView.SimpleOnItemTouchListener {

        public ItemTouchListener() {
            super();
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
                isUserTouching = true;
            } else if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_BUTTON_RELEASE || e.getAction() == MotionEvent.ACTION_CANCEL) {
                isUserTouching = false;
            }

            // Deal with performance mode gestures
            if (gestureDetector!=null) {
                return gestureDetector.onTouchEvent(e);
            } else {
                return super.onInterceptTouchEvent(rv, e);
            }
        }
    }

    private void onTouchAction() {
        mainActivityInterface.getDisplayPrevNext().showAndHide();
        mainActivityInterface.updateOnScreenInfo("showcapo");
        mainActivityInterface.showHideActionBar();
    }

    public void doScrollBy(int dy, int duration) {
        // Only do this if we aren't touching the screen!
        if (!isUserTouching) {
            smoothScrollBy(0,dy,linearInterpolator,duration);
        }
    }

    public void scrollToTop() {
        try {
            scrollToPosition(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        scrolledToTop = true;
        scrolledToBottom = false;
        scrollPosition = 0;
    }


    public void setMaxScrollY(int maxScrollY) {
        this.maxScrollY = maxScrollY;
    }

    public void setUserTouching(boolean isUserTouching) {
        this.isUserTouching = isUserTouching;
    }
    public boolean getIsUserTouching() {
        return isUserTouching;
    }
    public boolean getScrolledToTop() {
        return scrolledToTop;
    }
    public boolean getScrolledToBottom() {
        return scrolledToBottom;
    }
}

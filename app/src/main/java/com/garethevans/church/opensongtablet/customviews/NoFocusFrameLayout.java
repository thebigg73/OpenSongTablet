package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class NoFocusFrameLayout extends FrameLayout {

    private View focusDummy;

    public NoFocusFrameLayout(Context context) {
        super(context);
        init(context);
    }
    public NoFocusFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public NoFocusFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setFocusable(false);
        setFocusableInTouchMode(false);
        createFocusDummy(context);
    }

    private void createFocusDummy(Context context) {
        focusDummy = new View(context) {
            @Override
            public boolean onCheckIsTextEditor() {
                return false; // tells Android this is NOT a text input
            }
        };
        LayoutParams lp = new LayoutParams(1, 1);
        focusDummy.setLayoutParams(lp);
        focusDummy.setFocusable(true);
        focusDummy.setFocusableInTouchMode(true);
        focusDummy.setVisibility(View.INVISIBLE);
        focusDummy.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        addView(focusDummy);

        // set dummy as default focus after layout is attached
        post(() -> focusDummy.requestFocus());
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        // refuse focus
        return false;
    }

    @Override
    public View focusSearch(View focused, int direction) {
        // prevent DPAD / Tab from moving focus to this root
        View next = super.focusSearch(focused, direction);
        return (next == this) ? null : next;
    }

    /** Optional: expose the dummy in case you want to manually redirect focus elsewhere */
    public View getFocusDummy() {
        return focusDummy;
    }
}
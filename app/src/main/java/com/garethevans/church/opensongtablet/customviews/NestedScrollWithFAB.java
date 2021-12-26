package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NestedScrollWithFAB extends NestedScrollView {

    private FloatingActionButton fab = null;
    private ExtendedFloatingActionButton extendedFab = null;

    public NestedScrollWithFAB(@NonNull Context context) {
        super(context);
        new NestedScrollView(context);
    }

    public NestedScrollWithFAB(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (fab!=null || extendedFab!=null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_SCROLL:
                case MotionEvent.ACTION_MOVE:
                    if (fab!=null) {
                        fab.hide();
                    }
                    if (extendedFab!=null) {
                        extendedFab.hide();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (fab!=null) {
                        fab.show();
                    }
                    if (extendedFab!=null) {
                        extendedFab.show();
                    }
                    break;
            }
            performClick();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void setFabToAnimate(FloatingActionButton fab) {
        this.fab = fab;
    }

    public void setExtendedFabToAnimate(ExtendedFloatingActionButton extendedFab) {
        this.extendedFab = extendedFab;
    }
}

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
    private FloatingActionButton fab2 = null;
    private FloatingActionButton fab3 = null;
    private ExtendedFloatingActionButton extendedFab = null;
    private ExtendedFloatingActionButton extendedFab2 = null;

    public NestedScrollWithFAB(@NonNull Context context) {
        super(context);
        new NestedScrollView(context);
    }

    public NestedScrollWithFAB(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (fab!=null || fab2!=null || fab3!=null ||  extendedFab!=null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_SCROLL:
                case MotionEvent.ACTION_MOVE:
                    if (fab!=null) {
                        fab.hide();
                    }
                    if (fab2!=null) {
                        fab2.hide();
                    }
                    if (fab3!=null) {
                        fab3.hide();
                    }
                    if (extendedFab!=null) {
                        extendedFab.hide();
                    }
                    if (extendedFab2!=null) {
                        extendedFab2.hide();
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (fab!=null) {
                        fab.show();
                    }
                    if (fab2!=null) {
                        fab2.show();
                    }
                    if (fab3!=null) {
                        fab3.show();
                    }
                    if (extendedFab!=null) {
                        extendedFab.show();
                    }
                    if (extendedFab2!=null) {
                        extendedFab2.show();
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
    public void setFab2ToAnimate(FloatingActionButton fab2) {
        this.fab2 = fab2;
    }
    public void setFab3ToAnimate(FloatingActionButton fab3) {
        this.fab3 = fab3;
    }

    public void setExtendedFabToAnimate(ExtendedFloatingActionButton extendedFab) {
        this.extendedFab = extendedFab;
    }
    public void setExtendedFab2ToAnimate(ExtendedFloatingActionButton extendedFab2) {
        this.extendedFab2 = extendedFab2;
    }
}

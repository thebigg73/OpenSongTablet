package com.garethevans.church.opensongtablet.animation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.core.view.ViewCompat;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PageButtonFAB {

    private final FloatingActionButton actionButton;
    private final FloatingActionButton custom1;
    private final FloatingActionButton custom2;
    private final FloatingActionButton custom3;
    private final FloatingActionButton custom4;
    private final FloatingActionButton custom5;
    private final FloatingActionButton custom6;
    public PageButtonFAB(FloatingActionButton actionButton, FloatingActionButton custom1,
                         FloatingActionButton custom2, FloatingActionButton custom3,
                         FloatingActionButton custom4, FloatingActionButton custom5,
                         FloatingActionButton custom6) {
        this.actionButton = actionButton;
        this.custom1 = custom1;
        this.custom2 = custom2;
        this.custom3 = custom3;
        this.custom4 = custom4;
        this.custom5 = custom5;
        this.custom6 = custom6;
    }

    private final OvershootInterpolator interpolator = new OvershootInterpolator();

    public void animatePageButton(Context c, boolean open) {
        if (open) {
            ViewCompat.animate(actionButton).rotation(45f).withLayer().setDuration(500).
                    setInterpolator(interpolator).start();
            actionButton.setBackgroundTintList(ColorStateList.valueOf(c.getResources().getColor(R.color.red)));
        } else {
            ViewCompat.animate(actionButton).rotation(0f).withLayer().setDuration(500).
                    setInterpolator(interpolator).start();
            actionButton.setBackgroundTintList(ColorStateList.valueOf(c.getResources().getColor(R.color.secondary)));
        }
        animateView(custom1,open);
        animateView(custom2,open);
        animateView(custom3,open);
        animateView(custom4,open);
        animateView(custom5,open);
        animateView(custom6,open);
    }

    private void animateView(View view, boolean animateIn) {
        float alpha = 0f;
        int translationBy = 500;
        Runnable endRunnable = hideView(view, animateIn);
        Runnable startRunnable = hideView(view, animateIn);

        if (animateIn) {
            alpha = 1f;
            translationBy = -500;
            endRunnable = () -> {};
        } else {
            startRunnable = () -> {};
        }
        ViewCompat.animate(view).alpha(alpha).translationYBy(translationBy).setDuration(500).
                setInterpolator(interpolator).withStartAction(startRunnable).withEndAction(endRunnable).start();
    }
    private Runnable hideView(View view,boolean show) {
        return () -> {
            if (show) {
                view.setVisibility(View.VISIBLE);
            } else {
                view.setVisibility(View.GONE);
            }
        };
    }
}

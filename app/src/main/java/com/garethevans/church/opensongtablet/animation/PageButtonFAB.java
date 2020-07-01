package com.garethevans.church.opensongtablet.animation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.animation.OvershootInterpolator;

import androidx.core.view.ViewCompat;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PageButtonFAB {

    private FloatingActionButton actionButton, custom1, custom2, custom3, custom4, custom5, custom6;
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

    public void animatePageButton(Context c, boolean open) {
        OvershootInterpolator interpolator = new OvershootInterpolator();
        if (open) {
            ViewCompat.animate(actionButton).rotation(45f).withLayer().setDuration(500).
                    setInterpolator(interpolator).start();
            actionButton.setBackgroundTintList(ColorStateList.valueOf(c.getResources().getColor(R.color.red)));
            ViewCompat.animate(custom1).alpha(1f).translationYBy(-500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(custom2).alpha(1f).translationYBy(-500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(custom3).alpha(1f).translationYBy(-500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(custom4).alpha(1f).translationYBy(-500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(custom5).alpha(1f).translationYBy(-500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(custom6).alpha(1f).translationYBy(-500).setDuration(500).
                    setInterpolator(interpolator).start();
        } else {
            ViewCompat.animate(actionButton).rotation(0f).withLayer().setDuration(500).
                    setInterpolator(interpolator).start();
            actionButton.setBackgroundTintList(ColorStateList.valueOf(c.getResources().getColor(R.color.secondary)));
            ViewCompat.animate(custom1).alpha(0f).translationYBy(500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(custom2).alpha(0f).translationYBy(500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(custom3).alpha(0f).translationYBy(500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(custom4).alpha(0f).translationYBy(500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(custom5).alpha(0f).translationYBy(500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(custom6).alpha(0f).translationYBy(500).setDuration(500).
                    setInterpolator(interpolator).start();
        }
    }
}

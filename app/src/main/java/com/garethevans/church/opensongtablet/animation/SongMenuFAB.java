package com.garethevans.church.opensongtablet.animation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.animation.OvershootInterpolator;

import androidx.core.view.ViewCompat;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SongMenuFAB {

    private FloatingActionButton actionButton;
    private ExtendedFloatingActionButton editSong, createSong, importSong, exportSong;
    public SongMenuFAB(FloatingActionButton actionButton, ExtendedFloatingActionButton editSong,
                       ExtendedFloatingActionButton createSong, ExtendedFloatingActionButton importSong,
                       ExtendedFloatingActionButton exportSong) {
        this.actionButton = actionButton;
        this.editSong = editSong;
        this.createSong = createSong;
        this.importSong = importSong;
        this.exportSong = exportSong;
    }

    public void animateFABButton(Context c, boolean open) {
        OvershootInterpolator interpolator = new OvershootInterpolator();
        if (open) {
            ViewCompat.animate(actionButton).rotation(45f).withLayer().setDuration(500).
                    setInterpolator(interpolator).start();
            actionButton.setBackgroundTintList(ColorStateList.valueOf(c.getResources().getColor(R.color.red)));
            ViewCompat.animate(editSong).alpha(1f).translationYBy(-500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(createSong).alpha(1f).translationYBy(-500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(importSong).alpha(1f).translationYBy(-500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(exportSong).alpha(1f).translationYBy(-500).setDuration(500).
                    setInterpolator(interpolator).start();
        } else {
            ViewCompat.animate(actionButton).rotation(0f).withLayer().setDuration(500).
                    setInterpolator(interpolator).start();
            actionButton.setBackgroundTintList(ColorStateList.valueOf(c.getResources().getColor(R.color.secondary)));
            ViewCompat.animate(editSong).alpha(0f).translationYBy(500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(createSong).alpha(0f).translationYBy(500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(importSong).alpha(0f).translationYBy(500).setDuration(500).
                    setInterpolator(interpolator).start();
            ViewCompat.animate(exportSong).alpha(0f).translationYBy(500).setDuration(500).
                    setInterpolator(interpolator).start();
        }
    }
}

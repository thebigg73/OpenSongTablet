package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ScrollView;

import androidx.core.view.ViewCompat;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SongMenuFAB {

    private FloatingActionButton actionButton;
    OvershootInterpolator interpolator = new OvershootInterpolator();
    private ExtendedFloatingActionButton editSong, createSong, importSong, exportSong;
    private ScrollView dialogActions;
    public SongMenuFAB(FloatingActionButton actionButton, ExtendedFloatingActionButton editSong,
                       ExtendedFloatingActionButton createSong, ExtendedFloatingActionButton importSong,
                       ExtendedFloatingActionButton exportSong, ScrollView dialogActions) {
        this.actionButton = actionButton;
        this.editSong = editSong;
        this.createSong = createSong;
        this.importSong = importSong;
        this.exportSong = exportSong;
        this.dialogActions = dialogActions;
    }

    public void showFAB(boolean show) {
        if (show) {
            actionButton.show();
        } else {
            actionButton.hide();
        }
    }

    public void animateFABButton(Context c, boolean open) {
        if (open) {
            ViewCompat.animate(actionButton).rotation(45f).withLayer().setDuration(500).
                    setInterpolator(interpolator).start();
            actionButton.setBackgroundTintList(ColorStateList.valueOf(c.getResources().getColor(R.color.red)));
        } else {
            ViewCompat.animate(actionButton).rotation(0f).withLayer().setDuration(500).
                    setInterpolator(interpolator).start();
            actionButton.setBackgroundTintList(ColorStateList.valueOf(c.getResources().getColor(R.color.secondary)));
        }
        animateView(dialogActions,open);
        animateView(editSong,open);
        animateView(createSong,open);
        animateView(importSong,open);
        animateView(exportSong,open);
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

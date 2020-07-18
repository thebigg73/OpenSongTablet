/*
package com.garethevans.church.opensongtablet;

import android.os.Handler;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.View;
import android.widget.LinearLayout;

import static androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;

class DrawerTweaks {

    private static final Handler songMenuFlickClosed = new Handler();
    private static final Handler optionMenuFlickClosed = new Handler();

    static DrawerLayout.LayoutParams resizeMenu(View thismenu, int width) {
        if (thismenu!=null) {
            DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams) thismenu.getLayoutParams();
            lp.width = width;
            return lp;
        } else {
            return null;
        }
    }

    static void openMyDrawers(DrawerLayout drawer,
                              LinearLayout song,
                              LinearLayout option,
                              String which) {
        switch (which) {
            case "both":
                drawer.openDrawer(song);
                drawer.openDrawer(option);
                break;
            case "song":
                drawer.openDrawer(song);
                break;
            case "option":
                drawer.openDrawer(option);
                break;
            case "unlocked":
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                drawer.openDrawer(song);
                drawer.openDrawer(option);
                closeMyDrawers(drawer,song,option,"song_delayed");
                closeMyDrawers(drawer,song,option,"option_delayed");
                break;
            case "song_toggle":
                if (drawer.isDrawerOpen(song)) {
                    drawer.closeDrawer(song);
                } else {
                    drawer.openDrawer(song);
                }
                break;
            case "option_toggle":
                if (drawer.isDrawerOpen(option)) {
                    drawer.closeDrawer(option);
                } else {
                    drawer.openDrawer(option);
                }
                break;
        }
    }

    static void closeMyDrawers(final DrawerLayout drawer,
                               final LinearLayout song,
                               final LinearLayout option,
                               String which) {
        switch (which) {
            case "both":
                drawer.closeDrawer(song);
                drawer.closeDrawer(option);
                break;
            case "song":
                drawer.closeDrawer(song);
                break;
            case "option":
                drawer.closeDrawer(option);
                break;
            case "option_delayed":
                optionMenuFlickClosed.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawer.closeDrawer(option);
                    }
                }, 1000);
                break;
            case "song_delayed":
                songMenuFlickClosed.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawer.closeDrawer(song);
                    }
                }, 1000);
                break;
            case "locked":
                drawer.closeDrawer(song);
                drawer.closeDrawer(option);
                drawer.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
        }
    }

}*/

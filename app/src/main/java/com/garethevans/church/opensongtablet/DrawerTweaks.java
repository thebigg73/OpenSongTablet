package com.garethevans.church.opensongtablet;

import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

class DrawerTweaks {

    private static Handler songMenuFlickClosed = new Handler();
    private static Handler optionMenuFlickClosed = new Handler();

    static DrawerLayout.LayoutParams resizeMenu(LinearLayout thismenu, int width) {
        DrawerLayout.LayoutParams lp = (DrawerLayout.LayoutParams) thismenu.getLayoutParams();
        lp.width = width;
        return lp;
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

    static void openMyDrawersFS(DrawerLayout drawer,
                                LinearLayout song,
                                ExpandableListView option,
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
                closeMyDrawersFS(drawer,song,option,"song_delayed");
                closeMyDrawersFS(drawer,song,option,"option_delayed");
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
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    static void closeMyDrawersFS(final DrawerLayout drawer,
                                 final LinearLayout song,
                                 final ExpandableListView option,
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
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }
}
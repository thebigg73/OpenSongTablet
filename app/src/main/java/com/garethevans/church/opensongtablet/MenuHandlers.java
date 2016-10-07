package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MenuHandlers extends Activity {

    public interface MyInterface {
        void callIntent(String what, Intent i);
        void openMyDrawers(String what);
        void shareSong();
        void openFragment();
        void prepareOptionMenu();
        void doMoveInSet();
    }

    public static MyInterface mListener;

    public static boolean setSetButtonVisibility() {
        // If we are not in set mode, this icons visibility should be set to GONE
        // Otherwise, set it to visible
        return FullscreenActivity.setSize > 0 && FullscreenActivity.setView.equals("Y");
    }

    public static int setBackAlpha() {
        // Three options here.  Depends on sections and song in set
        boolean anothersection = false;
        boolean anothersong    = false;
        boolean anotherpdfpage = false;

        if (FullscreenActivity.currentSection>-1) {
            anothersection = FullscreenActivity.currentSection > 0;
        }
        if (FullscreenActivity.indexSongInSet>-1) {
            anothersong = FullscreenActivity.indexSongInSet > 0;
        }
        if (FullscreenActivity.pdfPageCurrent>-1) {
            anotherpdfpage = FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent > 0;
        }

        if (anothersection || anothersong || anotherpdfpage) {
            return 255;
        } else {
            return 30;
        }
    }

    public static int setForwardAlpha() {
        // Three options here.  Depends on sections and song in set
        boolean anothersection = false;
        boolean anothersong    = false;
        boolean anotherpdfpage = false;

        if (FullscreenActivity.currentSection>-1 && FullscreenActivity.songSections!=null) {
            anothersection = FullscreenActivity.currentSection < FullscreenActivity.songSections.length - 1;
        }
        if (FullscreenActivity.indexSongInSet>-1 && FullscreenActivity.mSetList!=null) {
            anothersong = FullscreenActivity.indexSongInSet < FullscreenActivity.mSetList.length - 1;
        }
        if (FullscreenActivity.pdfPageCurrent>-1 && FullscreenActivity.pdfPageCount>-1) {
            anotherpdfpage = FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent < FullscreenActivity.pdfPageCount - 1;
        }

        if (anothersection || anothersong || anotherpdfpage) {
            return 255;
        } else {
            return 30;
        }
    }

    public static boolean setBackEnabled() {
        // Three options here.  Depends on sections and song in set
        boolean anothersection = false;
        boolean anothersong = false;
        boolean anotherpdfpage = false;

        if (FullscreenActivity.currentSection>-1) {
            anothersection = FullscreenActivity.currentSection > 0;
        }
        if (FullscreenActivity.indexSongInSet>-1) {
            anothersong = FullscreenActivity.indexSongInSet > 0;
        }
        if (FullscreenActivity.pdfPageCurrent>-1) {
            anotherpdfpage = FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent > 0;
        }

        return (anothersection || anothersong || anotherpdfpage);
    }

    public static boolean setForwardEnabled() {
        // Three options here.  Depends on sections and song in set
        boolean anothersection = false;
        boolean anothersong = false;
        boolean anotherpdfpage = false;
        if (FullscreenActivity.songSections!=null) {
            anothersection = FullscreenActivity.currentSection < FullscreenActivity.songSections.length - 1;
        }
        if (FullscreenActivity.mSetList!=null) {
            anothersong = FullscreenActivity.indexSongInSet < FullscreenActivity.mSetList.length - 1;
        }
        if (FullscreenActivity.pdfPageCurrent>-1) {
            anotherpdfpage = FullscreenActivity.isPDF && FullscreenActivity.pdfPageCurrent < FullscreenActivity.pdfPageCount - 1;
        }
        return (anothersection || anothersong || anotherpdfpage);
    }

    public static int dualScreenAlpha() {
        if (FullscreenActivity.dualDisplayCapable.equals("Y")) {
            return 255;
        } else {
            return 30;
        }
    }

    public static void actOnClicks(Context c, int menuitem) {
        mListener = (MyInterface) c;
        FullscreenActivity.setMoveDirection = "";

        switch (menuitem) {
            case R.id.perform_mode:
                // Switch to performance mode
                FullscreenActivity.whichMode = "Performance";
                Preferences.savePreferences();
                Intent performmode = new Intent();
                performmode.setClass(c, FullscreenActivity.class);
                if (mListener!=null) {
                    mListener.callIntent("activity", performmode);
                }
                break;

            case R.id.stage_mode:
                // Switch to performance mode
                FullscreenActivity.whichMode = "Stage";
                Preferences.savePreferences();
                Intent stagemode = new Intent();
                stagemode.setClass(c, StageMode.class);
                if (mListener!=null) {
                    mListener.callIntent("activity", stagemode);
                }
                break;

            case R.id.present_mode:
                // Switch to presentation mode
                FullscreenActivity.whichMode = "Presentation";
                Preferences.savePreferences();
                Intent presentmode = new Intent();
                presentmode.setClass(c, PresenterMode.class);
                if (mListener!=null) {
                    mListener.callIntent("activity", presentmode);
                }
                break;

            case R.id.action_search:
                // Open/close the song drawer
                if (mListener!=null) {
                    mListener.openMyDrawers("song_toggle");
                }
                break;

            case R.id.action_settings:
                // Open/close the option drawer
                if (mListener!=null) {
                    mListener.openMyDrawers("option_toggle");
                }
                break;

            case R.id.song_share:
                // Share this song
                if (mListener!=null) {
                    mListener.shareSong();
                }
                break;

            case R.id.action_fullsearch:
                // Full search window
                FullscreenActivity.whattodo = "fullsearch";
                mListener.openFragment();
                break;

            case R.id.youtube_websearch:
                // Open a youtube search for the current song
                Intent youtube = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/results?search_query=" + FullscreenActivity.mTitle + "+" + FullscreenActivity.mAuthor));
                if (mListener!=null) {
                    mListener.callIntent("web", youtube);
                }
                break;

            case R.id.web_search:
                // Open a web search for the current song
                Intent web = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/search?q=" + FullscreenActivity.mTitle + "+" + FullscreenActivity.mAuthor));
                if (mListener!=null) {
                    mListener.callIntent("web", web);
                }
                break;

            case R.id.chordie_websearch:
                // Search Chordie
                FullscreenActivity.whattodo = "chordie";
                if (mListener!=null) {
                    mListener.openFragment();
                }
                break;

            case R.id.ultimateguitar_websearch:
                // Search Chordie
                FullscreenActivity.whattodo = "ultimate-guitar";
                if (mListener!=null) {
                    mListener.openFragment();
                }
                break;

            case R.id.set_add:
                if ((FullscreenActivity.isSong || FullscreenActivity.isPDF) && !FullscreenActivity.whichSongFolder.startsWith("..")) {
                    if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                        FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.songfilename + "_**$";
                    } else {
                        FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.whichSongFolder + "/"
                                + FullscreenActivity.songfilename + "_**$";
                    }
                    // Allow the song to be added, even if it is already there
                    FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;
                    // Tell the user that the song has been added.
                    FullscreenActivity.myToastMessage = "\"" + FullscreenActivity.songfilename + "\" "
                            + c.getResources().getString(R.string.addedtoset);
                    ShowToast.showToast(c);
                    // Vibrate to indicate something has happened
                    Vibrator vb = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
                    vb.vibrate(50);

                    // Save the set and other preferences
                    Preferences.savePreferences();
                    if (mListener!=null) {
                        mListener.prepareOptionMenu();
                    }
                }
                break;

            case R.id.set_back:
                // Move backwards in the set if possible
                if (!FullscreenActivity.tempswipeSet.equals("disable")) {
                    FullscreenActivity.tempswipeSet = "disable";
                    // reset the tempswipeset after 1sec
                    Handler delayfadeinredraw = new Handler();
                    delayfadeinredraw.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            FullscreenActivity.tempswipeSet = "enable";
                        }
                    }, FullscreenActivity.delayswipe_time);

                    // Set the swipe direction to right to left
                    FullscreenActivity.whichDirection = "L2R";
                    FullscreenActivity.setMoveDirection = "back";
                    if (mListener!=null) {
                        mListener.doMoveInSet();
                    }
                }
                break;

            case R.id.set_forward:
                // Move forwards in the set if possible
                if (!FullscreenActivity.tempswipeSet.equals("disable")) {
                    FullscreenActivity.tempswipeSet = "disable";
                    // reset the tempswipeset after 1sec
                    Handler delayfadeinredraw = new Handler();
                    delayfadeinredraw.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            FullscreenActivity.tempswipeSet = "enable";
                        }
                    }, FullscreenActivity.delayswipe_time);

                    // Set the swipe direction to right to left
                    FullscreenActivity.whichDirection = "R2L";
                    FullscreenActivity.setMoveDirection = "forward";
                    if (mListener!=null) {
                        mListener.doMoveInSet();
                    }
                }
                break;
        }
    }

    public static void forceOverFlow(Context c, ActionBar ab, Menu menu) {
        try {
            ViewConfiguration config = ViewConfiguration.get(c);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        if (ab != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException e) {
                    Log.e("menu", "onMenuOpened", e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
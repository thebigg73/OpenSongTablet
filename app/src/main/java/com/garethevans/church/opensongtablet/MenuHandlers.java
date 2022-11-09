package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.ViewConfiguration;

import androidx.appcompat.app.ActionBar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class MenuHandlers {

    public interface MyInterface {
        void openMyDrawers(String what);
        void prepareOptionMenu();
        void fixSet();
    }

    static void actOnClicks(Context c, Preferences preferences, int menuitem) {
        MyInterface mListener = (MyInterface) c;
        StaticVariables.setMoveDirection = "";

        final int search = R.id.action_search;
        final int settings = R.id.action_settings;
        final int setadd = R.id.set_add;

        switch (menuitem) {

            case search:
                // Open/close the song drawer
                if (mListener !=null) {
                    mListener.openMyDrawers("song_toggle");
                }
                break;

            case settings:
                // Open/close the option drawer
                if (mListener !=null) {
                    mListener.openMyDrawers("option_toggle");
                }
                break;

            case setadd:
                if (!StaticVariables.whichSongFolder.startsWith("..")) {
                    // Add to end of set
                    String newval = preferences.getMyPreferenceString(c,"setCurrent","") + StaticVariables.whatsongforsetwork;
                    preferences.setMyPreferenceString(c,"setCurrent",newval);
                    // Tell the user that the song has been added.
                    StaticVariables.myToastMessage = "\"" + StaticVariables.songfilename + "\" "
                            + c.getResources().getString(R.string.addedtoset);
                    ShowToast.showToast(c);
                    // Vibrate to indicate something has happened
                    DoVibrate.vibrate(c,50);

                    try {
                        mListener.prepareOptionMenu();
                        mListener.fixSet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    StaticVariables.myToastMessage = c.getResources().getString(R.string.not_allowed);
                    ShowToast.showToast(c);
                }
                break;
        }
    }

    static void forceOverFlow(Context c, ActionBar ab, Menu menu) {
        try {
            ViewConfiguration config = ViewConfiguration.get(c);
            if (Build.VERSION.SDK_INT<Build.VERSION_CODES.R) {
                Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
                if (menuKeyField != null) {
                    menuKeyField.setAccessible(true);
                    menuKeyField.setBoolean(config, false);
                }
            }
        } catch (Exception ex) {
            // Ignore
        }

        if (ab != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    @SuppressLint("PrivateApi") Method m = menu.getClass().getDeclaredMethod(
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
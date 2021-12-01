package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.ViewConfiguration;

import androidx.appcompat.app.ActionBar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class MenuHandlers {

    public interface MyInterface {
        void openMyDrawers(String what);
        void openFragment();
    }

    static void actOnClicks(Context c, Preferences preferences, int menuitem) {
        MyInterface mListener = (MyInterface) c;
        StaticVariables.setMoveDirection = "";

        final int settings = R.id.action_settings;
        final int fullsearch = R.id.action_fullsearch;

        switch (menuitem) {
            case settings:
                // Open/close the option drawer
                if (mListener !=null) {
                    mListener.openMyDrawers("option_toggle");
                }
                break;

            case fullsearch:
                // Full search window
                FullscreenActivity.whattodo = "fullsearch";
                mListener.openFragment();
                break;
        }
    }

    static void forceOverFlow(Context c, ActionBar ab, Menu menu) {
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
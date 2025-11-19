package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeKeeper {

    private static final String PREFS = "theme_prefs";
    private static final String KEY_MODE = "night_mode";

    private static int lastMode = AppCompatDelegate.MODE_NIGHT_YES;

    private ThemeKeeper() {}

    public static void save(Context context, int mode) {
        lastMode = mode;
        AppCompatDelegate.setDefaultNightMode(mode);
        if (context!=null) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(KEY_MODE, mode)
                    .apply();
        }
    }

    public static int load(Context context) {
        if (lastMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            lastMode = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getInt(KEY_MODE, AppCompatDelegate.MODE_NIGHT_YES);
        }
        return lastMode;
    }
}

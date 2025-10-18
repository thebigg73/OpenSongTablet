package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.garethevans.church.opensongtablet.R;

// Because Android Day/Night is horrible (and breaks with WebView), we do it manually
// Each view checks this class when it gets created
// It is a pain, but it works

public final class Palette {
    public int background;
    public int onBackground;
    public int surface;
    public int onSurface;
    public int primary;
    public int onPrimary;
    public int secondary;
    public int textColor;
    public int hintColor;
    public int errorColor;
    public int secondaryVariant;
    public int secondaryFixed;
    public int primaryVariant;

    private final String PREF_NAME="theme_choice", DARK="dark";
    public boolean dark = false;

    public Palette(Context context) {
        dark = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(DARK, false);
        setColors(context);
    }

    public void savePref(Context context, boolean dark) {
        this.dark = dark;
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).
                edit().putBoolean(DARK,dark).apply();
        setColors(context);
    }

    public void setColors(Context context) {
        background = ContextCompat.getColor(context, dark ? R.color.dark_primary : R.color.light_primary);
        onBackground = ContextCompat.getColor(context, dark ? R.color.dark_color : R.color.light_color);
        surface = ContextCompat.getColor(context, dark ? R.color.dark_surface : R.color.light_surface);
        onSurface = ContextCompat.getColor(context, dark ? R.color.dark_color : R.color.light_color);
        primary = ContextCompat.getColor(context, dark ? R.color.dark_primary : R.color.light_primary);
        primaryVariant = ContextCompat.getColor(context, dark ? R.color.dark_primary_variant : R.color.light_primary_variant);
        onPrimary = ContextCompat.getColor(context, dark ? R.color.dark_color : R.color.light_color);
        textColor = ContextCompat.getColor(context, dark ? R.color.dark_color : R.color.light_color);
        hintColor = ContextCompat.getColor(context, dark ? R.color.dark_hint : R.color.light_hint);
        secondary = ContextCompat.getColor(context, dark ? R.color.dark_secondary : R.color.light_secondary);
        secondaryVariant = ContextCompat.getColor(context, dark ? R.color.dark_secondary_variant : R.color.light_secondary_variant);
        secondaryFixed = ContextCompat.getColor(context, dark ? R.color.dark_secondary_fixed : R.color.light_secondary_fixed);
        errorColor = ContextCompat.getColor(context, dark ? R.color.dark_error : R.color.light_error);

    }

}

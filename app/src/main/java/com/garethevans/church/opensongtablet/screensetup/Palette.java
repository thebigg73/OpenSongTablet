package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

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
        background = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_primary : R.color.light_primary, null);
        onBackground = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_color : R.color.light_color, null);
        surface = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_surface : R.color.light_surface, null);
        onSurface = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_color : R.color.light_color, null);
        primary = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_primary : R.color.light_primary, null);
        primaryVariant = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_primary_variant : R.color.light_primary_variant, null);
        onPrimary = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_color : R.color.light_color, null);
        textColor = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_color : R.color.light_color, null);
        hintColor = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_hint : R.color.light_hint, null);
        secondary = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_secondary : R.color.light_secondary, null);
        secondaryVariant = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_secondary_variant : R.color.light_secondary_variant, null);
        secondaryFixed = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_secondary_fixed : R.color.light_secondary_fixed, null);
        errorColor = ResourcesCompat.getColor(context.getResources(), dark ? R.color.dark_error : R.color.light_error, null);
    }

}

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
    public boolean dark;

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
            int darkPrimary = dark ? R.color.dark_primary : R.color.light_primary;
            int darkColor = dark ? R.color.dark_color : R.color.light_color;
            int darkSurface = dark ? R.color.dark_surface : R.color.light_surface;
            int darkHint = dark ? R.color.dark_hint : R.color.light_hint;
            int darkPrimaryVariant = dark ? R.color.dark_primary_variant : R.color.light_primary_variant;
            int darkSecondary = dark ? R.color.dark_secondary : R.color.light_secondary;
            int darkSecondaryVariant = dark ? R.color.dark_secondary_variant : R.color.light_secondary_variant;
            int darkSecondaryFixed = dark ? R.color.dark_secondary_fixed : R.color.light_secondary_fixed;
            int darkError = dark ? R.color.dark_error : R.color.light_error;

            // context.getResources().getColor(int) is safe on all API levels (API 1+)
            background = context.getResources().getColor(darkPrimary);
            onBackground = context.getResources().getColor(darkColor);
            surface = context.getResources().getColor(darkSurface);
            onSurface = context.getResources().getColor(darkColor);
            primary = context.getResources().getColor(darkPrimary);
            primaryVariant = context.getResources().getColor(darkPrimaryVariant);
            onPrimary = context.getResources().getColor(darkColor);
            textColor = context.getResources().getColor(darkColor);
            hintColor = context.getResources().getColor(darkHint);
            secondary = context.getResources().getColor(darkSecondary);
            secondaryVariant = context.getResources().getColor(darkSecondaryVariant);
            secondaryFixed = context.getResources().getColor(darkSecondaryFixed);
            errorColor = context.getResources().getColor(darkError);

        /*try {
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
        } catch (Throwable t) {
            // Catches both missing method Errors on API 22 and any runtime Exceptions cleanly
            background = context.getResources().getColor(dark ? R.color.dark_primary : R.color.light_primary);
            onBackground = context.getResources().getColor(dark ? R.color.dark_color : R.color.light_color);
            surface = context.getResources().getColor(dark ? R.color.dark_surface : R.color.light_surface);
            onSurface = context.getResources().getColor(dark ? R.color.dark_color : R.color.light_color);
            primary = context.getResources().getColor(dark ? R.color.dark_primary : R.color.light_primary);
            primaryVariant = context.getResources().getColor(dark ? R.color.dark_primary_variant : R.color.light_primary_variant);
            onPrimary = context.getResources().getColor(dark ? R.color.dark_color : R.color.light_color);
            textColor = context.getResources().getColor(dark ? R.color.dark_color : R.color.light_color);
            hintColor = context.getResources().getColor(dark ? R.color.dark_hint : R.color.light_hint);
            secondary = context.getResources().getColor(dark ? R.color.dark_secondary : R.color.light_secondary);
            secondaryVariant = context.getResources().getColor(dark ? R.color.dark_secondary_variant : R.color.light_secondary_variant);
            secondaryFixed = context.getResources().getColor(dark ? R.color.dark_secondary_fixed : R.color.light_secondary_fixed);
            errorColor = context.getResources().getColor(dark ? R.color.dark_error : R.color.light_error);
        }*/
        /*background = ContextCompat.getColor(context, dark ? R.color.dark_primary : R.color.light_primary);
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
        errorColor = ContextCompat.getColor(context, dark ? R.color.dark_error : R.color.light_error);*/
    }

}

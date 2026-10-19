package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

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

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Palette";

    private final String PREF_NAME="theme_choice",
            DISPLAY_MODE="displayMode";
    public boolean dark;
    public boolean amoled;
    public String theme;

    public Palette(Context c) {
        theme = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                        .getString(DISPLAY_MODE,"");
        // If the theme hasn't been set, look for the device day/night
        if (theme.isEmpty()) {
            // Get the default theme based on device setting
            // This then saves this an sets up the colours
            getDefaultTheme(c);
        } else {
            // Go straight to setup
            setDisplayVariables(c);
            setColors(c);
        }
    }

    public void getDefaultTheme(Context c) {
        // Because we haven't set a theme yet, look for the device settings
        int nightModeFlags = c.getApplicationContext().getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        theme = "dark";
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                // Dark mode is active
                theme = "dark";
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                // Light mode is active
                theme = "light";
                break;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                // Mode is unknown (very rare)
                theme = "dark";
        }
        savePref(c,theme);
    }

    public void savePref(Context c, String theme) {
        this.theme = theme;
        // Save the new preference
        c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).
                edit().putString(DISPLAY_MODE,theme).apply();
        setDisplayVariables(c);
        setColors(c);
    }

    private void setDisplayVariables(Context c) {
        // Theme can be dark, light, custom1, custom2, amoled
        // If set to amoled, then use the amoled colours
        if (theme!=null && !theme.isEmpty()) {
            switch (theme) {
                case "amoled":
                    //Use the AMOLED colours
                    dark = false;
                    amoled = true;
                    break;

                case "dark":
                case "custom1":
                    // Use the dark colours
                    dark = true;
                    amoled = false;
                    break;

                case "light":
                case "custom2":
                    // Use the light colours
                    dark = false;
                    amoled = false;
                    break;
            }
        } else {
            getDefaultTheme(c);
        }
    }

    public String getDefaultTheme() {
        // Returns a basic theme - called on app boot, particularly when no theme has yet been set
        if (amoled) {
            return "amoled";
        } else if (dark) {
            return "dark";
        } else {
            return "light";
        }

    }

    @SuppressWarnings("deprecation")
    public void setColors(Context context) {
        try {
            background = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_primary : dark ? R.color.dark_primary : R.color.light_primary, null);
            onBackground = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_color : dark ? R.color.dark_color : R.color.light_color, null);
            surface = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_surface : dark ? R.color.dark_surface : R.color.light_surface, null);
            onSurface = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_color : dark ? R.color.dark_color : R.color.light_color, null);
            primary = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_primary : dark ? R.color.dark_primary : R.color.light_primary, null);
            primaryVariant = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_primary_variant : dark ? R.color.dark_primary_variant : R.color.light_primary_variant, null);
            onPrimary = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_color : dark ? R.color.dark_color : R.color.light_color, null);
            textColor = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_color : dark ? R.color.dark_color : R.color.light_color, null);
            hintColor = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_hint : dark ? R.color.dark_hint : R.color.light_hint, null);
            secondary = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_secondary : dark ? R.color.dark_secondary : R.color.light_secondary, null);
            secondaryVariant = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_secondary_variant : dark ? R.color.dark_secondary_variant : R.color.light_secondary_variant, null);
            secondaryFixed = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_secondary_fixed : dark ? R.color.dark_secondary_fixed : R.color.light_secondary_fixed, null);
            errorColor = ResourcesCompat.getColor(context.getResources(), amoled ? R.color.amoled_error : dark ? R.color.dark_error : R.color.light_error, null);
        } catch (Throwable t) {
            // Catches both missing method Errors on API 22 and any runtime Exceptions cleanly
            background = context.getResources().getColor(amoled ? R.color.amoled_primary : dark ? R.color.dark_primary : R.color.light_primary);
            onBackground = context.getResources().getColor(amoled ? R.color.amoled_color : dark ? R.color.dark_color : R.color.light_color);
            surface = context.getResources().getColor(amoled ? R.color.amoled_surface : dark ? R.color.dark_surface : R.color.light_surface);
            onSurface = context.getResources().getColor(amoled ? R.color.amoled_color : dark ? R.color.dark_color : R.color.light_color);
            primary = context.getResources().getColor(amoled ? R.color.amoled_primary : dark ? R.color.dark_primary : R.color.light_primary);
            primaryVariant = context.getResources().getColor(amoled ? R.color.amoled_primary_variant : dark ? R.color.dark_primary_variant : R.color.light_primary_variant);
            onPrimary = context.getResources().getColor(amoled ? R.color.amoled_color : dark ? R.color.dark_color : R.color.light_color);
            textColor = context.getResources().getColor(amoled ? R.color.amoled_color : dark ? R.color.dark_color : R.color.light_color);
            hintColor = context.getResources().getColor(amoled ? R.color.amoled_hint : dark ? R.color.dark_hint : R.color.light_hint);
            secondary = context.getResources().getColor(amoled ? R.color.amoled_secondary : dark ? R.color.dark_secondary : R.color.light_secondary);
            secondaryVariant = context.getResources().getColor(amoled ? R.color.amoled_secondary_variant : dark ? R.color.dark_secondary_variant : R.color.light_secondary_variant);
            secondaryFixed = context.getResources().getColor(amoled ? R.color.amoled_secondary_fixed : dark ? R.color.dark_secondary_fixed : R.color.light_secondary_fixed);
            errorColor = context.getResources().getColor(amoled ? R.color.amoled_error : dark ? R.color.dark_error : R.color.light_error);
        }
    }

}

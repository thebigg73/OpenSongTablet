package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.content.res.Configuration;

import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

import java.util.Locale;

public class FixLocale {

    public void fixLocale(Context c, Preferences preferences) {
        // Locale
        try {
            // Get the user's preference
            String val = preferences.getMyPreferenceString(c,"language",null);

            // If this is already set, that' what we will use
            if (val!=null) {
                StaticVariables.locale = new Locale(val);

            } else {
                // No locale is set, so let's see if the user's language is supported
                // Get device setting
                String deviceval = Locale.getDefault().getLanguage();

                // If this is supported, set the user preference, otherwise use English
                String translations = "af cs de el es fr hu it ja pl pt ru sr sv zh";
                if (!translations.contains(deviceval)) {
                    deviceval = "en";
                }

                StaticVariables.locale = new Locale(deviceval);

                // Save our preference
                preferences.setMyPreferenceString(c,"language",deviceval);
            }

            // Load the appropriate translations
            Configuration configuration = new Configuration();
            Locale.setDefault(StaticVariables.locale);
            configuration.setLocale(StaticVariables.locale);
            c.getResources().updateConfiguration(configuration, c.getResources().getDisplayMetrics());

        } catch (Exception e) {
            e.printStackTrace();
            StaticVariables.locale = new Locale("en");
        }
    }
}
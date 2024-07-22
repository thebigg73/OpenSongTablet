package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.content.res.Configuration;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.Locale;

public class FixLocale {

    // By default the app runs in the device language (if available)
    // English is the fall back default
    // If a user chooses a different language in the app, this is used regardless of the device settings

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "FixLocale";
    private Locale userLocale;
    private String language;
    private final Context c;
    private final MainActivityInterface mainActivityInterface;


    public Locale getLocale() {
        return userLocale;
    }

    public FixLocale(Context c) {
        this.c = c;
        this.mainActivityInterface = (MainActivityInterface) c;
        getUpdatedPreferences();
    }

    // If we change load in a profile, this is called
    public void getUpdatedPreferences() {
        language = mainActivityInterface.getPreferences().getMyPreferenceString("language",null);
        setLocale();
    }

    public void setLocale() {
        // Locale
        boolean wasset = false;
        try {
            // Get the user's preference
            language = mainActivityInterface.getPreferences().getMyPreferenceString("language",null);

            // If this is already set, that's what we will use
            if (language!=null) {
                userLocale = new Locale(language);

            } else {
                // No locale is set, so let's see if the user's language is supported
                // Get device setting
                String deviceval = Locale.getDefault().getLanguage();

                // If this is supported, set the user preference, otherwise use English
                String translations = "af cs de el es fr hu it ja pl pt ru sr sv uk zh";
                if (!translations.contains(deviceval)) {
                    deviceval = "en";
                }

                userLocale = new Locale(deviceval);

            }

            // Load the appropriate translations
            Configuration configuration = new Configuration();
            Locale.setDefault(userLocale);
            configuration.setLocale(userLocale);
            c.getResources().updateConfiguration(configuration, c.getResources().getDisplayMetrics());
            wasset = true;
        } catch (Exception e) {
            e.printStackTrace();
            userLocale = new Locale("en");
        }

        if (!wasset) {
            try {
                Configuration configuration = new Configuration();
                Locale.setDefault(userLocale);
                configuration.setLocale(userLocale);
                c.getResources().updateConfiguration(configuration, c.getResources().getDisplayMetrics());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
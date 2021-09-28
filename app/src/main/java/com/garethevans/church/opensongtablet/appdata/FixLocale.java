package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.Locale;

public class FixLocale {

    private Locale userLocale;
    private final String TAG = "FixLocale";

    public Locale getLocale() {
        return userLocale;
    }

    public void setLocale(Context c, MainActivityInterface mainActivityInterface) {
        // Locale
        try {
            // Get the user's preference
            String val = mainActivityInterface.getPreferences().getMyPreferenceString(c,"language",null);

            Log.d(TAG,"val="+val);
            // If this is already set, that' what we will use
            if (val!=null) {
                userLocale = new Locale(val);

            } else {
                // No locale is set, so let's see if the user's language is supported
                // Get device setting
                String deviceval = Locale.getDefault().getLanguage();

                // If this is supported, set the user preference, otherwise use English
                String translations = "af cs de el es fr hu it ja pl pt ru sr sv zh";
                if (!translations.contains(deviceval)) {
                    deviceval = "en";
                }

                userLocale = new Locale(deviceval);

                // Save our preference
                mainActivityInterface.getPreferences().setMyPreferenceString(c,"language",deviceval);
            }

            // Load the appropriate translations
            Configuration configuration = new Configuration();
            Locale.setDefault(userLocale);
            configuration.setLocale(userLocale);
            c.getResources().updateConfiguration(configuration, c.getResources().getDisplayMetrics());

        } catch (Exception e) {
            e.printStackTrace();
            userLocale = new Locale("en");
        }
    }
}
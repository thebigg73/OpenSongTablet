package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

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
    private final MainActivityInterface mainActivityInterface;


    public Locale getLocale() {
        return userLocale;
    }

    public FixLocale(Context c) {
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
                language = deviceval;

                // If this is supported, set the user preference, otherwise use English
                String translations = "af cs de el es fr hu it iw ja pl pt ru si sr sv uk zh";
                if (!translations.contains(deviceval)) {
                    deviceval = "en";
                    language = "en";
                }

                userLocale = new Locale(deviceval);

            }

            // Load the appropriate translations
            // The old method no longer works on newer devices
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(language);
            AppCompatDelegate.setApplicationLocales(appLocale);

            wasset = true;
        } catch (Exception e) {
            e.printStackTrace();
            userLocale = new Locale("en");
            language = "en";
        }

        if (!wasset) {
            try {
                LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(language);
                AppCompatDelegate.setApplicationLocales(appLocale);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isTextRTL(String text) {
        if (text == null || text.isEmpty()) {
            Log.d(TAG,"isTextRTL:false");
            return false;
        }
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            int direction = Character.getDirectionality(codePoint);

            // Only return true for *strong* RTL characters (Hebrew/Arabic letters),
            // ignoring neutral punctuation, brackets, and spaces.
            if (direction == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                    direction == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC) {
                Log.d(TAG,"isTextRTL:true");
                return true;
            }
            i += Character.charCount(codePoint);
        }
        Log.d(TAG,"isTextRTL:false");
        return false;
    }

    private boolean isLocaleRTL() {
        if (language==null) {
            setLocale();
        }
        if (language!=null) {
            return language.equals("iw");
        } else {
            if (language==null) {
                language = "en";
            }
            return false;
        }
    }

    private boolean isDeviceRTL(Context c) {
        Configuration config = c.getResources().getConfiguration();
        boolean isRTL = config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        Log.d(TAG,"isDeviceRTL:"+isRTL);
        return isRTL;
    }

    public boolean displaySongAsRTL(Context c, String lyrics) {
        // If we are using localeRTL or deviceRTL, use RTL if the lyrics contain RTL or are empty
        if ((isLocaleRTL() || isDeviceRTL(c)) && (isTextRTL(lyrics) || lyrics.isEmpty())) {
            return true;
        } else {
            return isTextRTL(lyrics);
        }
    }
}
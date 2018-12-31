package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

public class SetLocale extends Activity {

    public static void setLocale(Context c) {
        if (!FullscreenActivity.languageToLoad.isEmpty()) {
            Locale locale;
            locale = new Locale(FullscreenActivity.languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            c.getResources().updateConfiguration(config,
                    c.getResources().getDisplayMetrics());
        }
    }
}
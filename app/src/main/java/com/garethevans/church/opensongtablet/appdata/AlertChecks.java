package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.widget.Button;

import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;

public class AlertChecks {

    // This class deals with checking if the app should show alerts

    public boolean showPlayServicesAlert(Context c) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(c) != ConnectionResult.SUCCESS;
    }

    public String updateInfo(Context c, Preferences preferences) {
        // Decide if the current app version is newer than the previous version.
        // If so, we want the user to be notified of changed

        String returnVal = null;

        // Get last saved version
        int lastUsedVersion = preferences.getMyPreferenceInt(c, "lastUsedVersion", 0);

        // Get the current version
        int thisVersion;
        String versionCode;
        PackageInfo pInfo;
        try {
            pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                thisVersion = (int) pInfo.getLongVersionCode();
            } else {
                thisVersion = pInfo.versionCode;
            }
            versionCode = "V."+pInfo.versionName;
        } catch (Exception e) {
            thisVersion = 0;
            versionCode = "";
        }

        // Add the info into the return variable arraylist
        if (thisVersion>lastUsedVersion) {
            // Update has happened, send the new info
            returnVal = versionCode+" ("+thisVersion+")";
        }
        return returnVal;
    }

    public boolean showBackup(Context c, Preferences preferences) {
        // Check for the number of times the app has run without the user backing up their songs
        // If this is 10 (or more) show the backup prompt window.
        int runssincebackup = preferences.getMyPreferenceInt(c, "runssincebackup", 0);
        return runssincebackup >=10;
    }
}

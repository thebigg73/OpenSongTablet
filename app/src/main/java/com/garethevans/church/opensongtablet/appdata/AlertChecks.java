package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;

import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class AlertChecks {

    // This class deals with checking if the app should show alerts

    public boolean showPlayServicesAlert(Context c) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(c) != ConnectionResult.SUCCESS;
    }

    public boolean showUpdateInfo(Context c, Preferences preferences, int currentVersion) {
        // Decide if the current app version is newer than the previous version.
        // If so, we want the user to be notified of changed

        // Get last saved version
        int lastUsedVersion = preferences.getMyPreferenceInt(c, "lastUsedVersion", 0);

        // Get the current version
        return currentVersion > lastUsedVersion;
    }

    public boolean showBackup(Context c, Preferences preferences) {
        // Check for the number of times the app has run without the user backing up their songs
        // If this is 10 (or more) show the backup prompt window.
        int runssincebackup = preferences.getMyPreferenceInt(c, "runssincebackup", 0);
        return runssincebackup >=10;
    }
}

package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class AlertChecks {

    // This class deals with checking if the app should show alerts

    public boolean showPlayServicesAlert(Context c) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(c) != ConnectionResult.SUCCESS;
    }

    public boolean showUpdateInfo(int lastUsedVersion, int currentVersion) {
        // Decide if the current app version is newer than the previous version.
        // If so, we want the user to be notified of changed

        return currentVersion > lastUsedVersion;
    }

    public boolean showBackup(int runssincebackup) {
        // Check for the number of times the app has run without the user backing up their songs
        // If this is 10 (or more) show the backup prompt window.
        return runssincebackup >=10;
    }
}

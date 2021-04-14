package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class AlertChecks {

    // This class deals with checking if the app should show alerts

    public boolean showPlayServicesAlert(Context c) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(c) != ConnectionResult.SUCCESS;
    }

    public boolean showUpdateInfo(Context c, MainActivityInterface mainActivityInterface) {
        // Decide if the current app version is newer than the previous version.
        // If so, we want the user to be notified of changed

        return mainActivityInterface.getVersionNumber().getVersionCode() >
                mainActivityInterface.getPreferences().getMyPreferenceInt(c,"lastUsedVersion",0);
    }

    public boolean showBackup(Context c, MainActivityInterface mainActivityInterface) {
        // Check for the number of times the app has run without the user backing up their songs
        // If this is 10 (or more) show the backup prompt window.
        return mainActivityInterface.getPreferences().getMyPreferenceInt(c,"runssincebackup",0) >=10;
    }
}

package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class AlertChecks {

    // This class deals with checking if the app should show alerts
    private final String TAG = "AlertChecks";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    public AlertChecks(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    public boolean showPlayServicesAlert() {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(c) != ConnectionResult.SUCCESS) {
            Log.d(TAG, "Need to show alert because there is an issue with Google Play Services");
        }
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(c) != ConnectionResult.SUCCESS;
    }

    public boolean showUpdateInfo() {
        int currentVersion = mainActivityInterface.getVersionNumber().getVersionCode();
        int lastUsedVersion = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"lastUsedVersion",0);

        Log.d(TAG,"current: "+currentVersion+"  last: "+lastUsedVersion);
        // Decide if the current app version is newer than the previous version.
        // If so, we want the user to be notified of changed.  The prefs are updated when the button is clicked
        if (currentVersion>lastUsedVersion) {
            Log.d(TAG, "Need to show alert because this is a new version");
        }
        return currentVersion > lastUsedVersion;
    }

    public boolean showBackup() {
        // Check for the number of times the app has run without the user backing up their songs
        // If this is 10 (or more) show the backup prompt window.
        int runssincebackup = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"runssincebackup",0);
        if (runssincebackup>=10) {
            Log.d(TAG, "Need to show alert because we haven't backed up recently");
        }

        return runssincebackup >=10;
    }
}

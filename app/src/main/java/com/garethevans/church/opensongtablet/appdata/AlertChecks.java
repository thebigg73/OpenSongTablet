package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class AlertChecks {

    // This class deals with checking if the app should show alerts
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "AlertChecks";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private boolean alreadySeen = false, ignorePlayServicesWarning, firstcheck = true,
            hasPlayServices = false;
    public AlertChecks(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        ignorePlayServicesWarning = mainActivityInterface.getPreferences().getMyPreferenceBoolean("ignorePlayServicesWarning",false);
    }

    public boolean showPlayServicesAlert() {
        hasPlayServices = false;

        if (firstcheck || !ignorePlayServicesWarning) {
            firstcheck = false;
            hasPlayServices = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(c) == ConnectionResult.SUCCESS;
        }

        if (hasPlayServices) {
            setIgnorePlayServicesWarning(false);
        }
        boolean dontNeed = hasPlayServices || ignorePlayServicesWarning || alreadySeen;
        return !dontNeed;
    }

    public boolean getHasPlayServices() {
        return hasPlayServices;
    }

    public void setIgnorePlayServicesWarning(boolean ignorePlayServicesWarning) {
        this.ignorePlayServicesWarning = ignorePlayServicesWarning;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("ignorePlayServicesWarning",ignorePlayServicesWarning);
    }

    public boolean getIgnorePlayServicesWarning() {
        return ignorePlayServicesWarning;
    }

    public boolean showUpdateInfo() {
        int currentVersion = mainActivityInterface.getVersionNumber().getVersionCode();
        int lastUsedVersion = mainActivityInterface.getPreferences().getMyPreferenceInt("lastUsedVersion",0);

        // Decide if the current app version is newer than the previous version.
        // If so, we want the user to be notified of changed.  The prefs are updated when the button is clicked

        return !alreadySeen && currentVersion > lastUsedVersion;
    }

    public boolean showBackup() {
        // Check for the number of times the app has run without the user backing up their songs
        // If this is 10 (or more) show the backup prompt window.
        int runssincebackup = mainActivityInterface.getPreferences().getMyPreferenceInt("runssincebackup",0);
        int runssincebackupdismissed = mainActivityInterface.getPreferences().getMyPreferenceInt("runssincebackupdismissed",0);
        return !alreadySeen && runssincebackup >= 10 && runssincebackupdismissed >= 10;
    }

    public void setAlreadySeen(boolean alreadySeen) {
        this.alreadySeen = alreadySeen;
    }
}

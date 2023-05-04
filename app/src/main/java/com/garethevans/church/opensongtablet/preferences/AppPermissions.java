package com.garethevans.church.opensongtablet.preferences;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.InformationBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class AppPermissions {

    private final Context context;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = "Permissions";
    private String permissionsLog = "";

    public AppPermissions(Context context) {
        // This class is used to keep all the permissions in the same place
        this.context = context;
    }

    // Location
    public boolean locationEnabled(Context c, MainActivityInterface mainActivityInterface) {
        // IV - Nearby requires Location services with network access to discover devices
        boolean network_enabled = false;

        try {
            LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
            Log.d(TAG,"lm:"+lm+"   providers:"+lm.getAllProviders());
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.d(TAG, "Could not check NETWORK_PROVIDER is enabled");
        }

        if (!network_enabled) {
            // notify user
            InformationBottomSheet informationBottomSheet = new InformationBottomSheet(c.getString(R.string.location),
                    c.getString(R.string.location_not_enabled), c.getString(R.string.settings), "locPrefs");
            informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "InformationBottomSheet");
            return false;
        } else {
            return true;
        }
    }

    // Nearby
    public String[] getNearbyPermissions() {
        if (Build.VERSION.SDK_INT >= 33) { //
            return new String[]{Manifest.permission.NEARBY_WIFI_DEVICES,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE};
        } else if (Build.VERSION.SDK_INT >= 31) { // Android S / 12
            return new String[]{Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE};
        } else if (Build.VERSION.SDK_INT >= 29) { // Android Q / 10
            return new String[]{Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};
        } else { // Older versions!
            return new String[]{Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION};
        }
    }

    public boolean hasGooglePlay() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

    public boolean hasNearbyPermissions() {
        return checkForPermissions(getNearbyPermissions());
    }

    // MIDI
    public String[] getMidiScanPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION};
        } else {
            return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
        }
    }

    public boolean hasMidiScanPermissions() {
        return checkForPermissions(getMidiScanPermissions());
    }

    // AUDIO
    public String getAudioPermissions() {
        return Manifest.permission.RECORD_AUDIO;
    }

    public boolean hasAudioPermissions() {
        return checkForPermission(getAudioPermissions());
    }

    // STORAGE
    public String getStoragePermissions() {
        return Manifest.permission.WRITE_EXTERNAL_STORAGE;
    }

    public boolean hasStoragePermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP || checkForPermission(getStoragePermissions());
    }

    // CAMERA
    public String getCameraPermissions() {
        return Manifest.permission.CAMERA;
    }

    public boolean hasCameraPermission() {
        return checkForPermission(getCameraPermissions());
    }

    // GENERAL CHECK
    public boolean checkForPermission(String permission) {
        boolean granted = ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        permissionsLog += "permission: " + permission + "   granted:" + granted + "\n";
        return granted;
    }

    public boolean checkForPermissions(String[] permissions) {
        boolean returnVal = true;
        StringBuilder stringBuilder = new StringBuilder();
        if (permissions != null) {
            for (String permission : permissions) {
                boolean thisPermission = checkForPermission(permission);
                stringBuilder.append("permission: ").append(permission).append("   granted:").append(thisPermission).append("\n");
                returnVal = returnVal && thisPermission;
                Log.d(TAG, "permission:" + permission + "  returnVal:" + returnVal);
            }
        } else {
            // No additional permissions required
            return true;
        }
        permissionsLog += stringBuilder;
        return returnVal;
    }

    public String getPermissionsLog() {
        return permissionsLog;
    }

    public void resetPermissionsLog() {
        permissionsLog = "";
    }

}

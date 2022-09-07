package com.garethevans.church.opensongtablet.preferences;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class AppPermissions {

    private final Context context;
    private final String TAG = "Permissions";

    public AppPermissions(Context context) {
        // This class is used to keep all the permissions in the same place
        this.context = context;
    }

    // Nearby
    public String[] getNearbyPermissions() {
        if (Build.VERSION.SDK_INT>=33) {
            return new String[] {Manifest.permission.NEARBY_WIFI_DEVICES};
        } else if (Build.VERSION.SDK_INT>30) {
            return new String[] {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION};
        } else if (Build.VERSION.SDK_INT==29 || Build.VERSION.SDK_INT==30) {
            return new String[] {Manifest.permission.ACCESS_FINE_LOCATION};
        } else {
            return new String[] {Manifest.permission.ACCESS_COARSE_LOCATION};
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
        if (Build.VERSION.SDK_INT>30) {
            return new String[] {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION};
        } else {
            return null;
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
        return Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP || checkForPermission(getStoragePermissions());
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
        return ActivityCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED;
    }
    public boolean checkForPermissions(String[] permissions) {
        boolean returnVal = true;
        if (permissions!=null) {
            for (String permission:permissions) {
                returnVal = returnVal && checkForPermission(permission);
            }
        } else {
            // No additional permissions required
            return true;
        }
        return returnVal;
    }

}

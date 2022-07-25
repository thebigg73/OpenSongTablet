package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

public class Permissions {

    // Nearby
    private String[] nearbyPermissionsString;
    public void setNearbyPermissionsString() {
        if (Build.VERSION.SDK_INT >= 33) {
            nearbyPermissionsString = new String[]{Manifest.permission_group.NEARBY_DEVICES};
        } else if (Build.VERSION.SDK_INT > 30) {
            nearbyPermissionsString = new String[]{Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT};
        } else if (Build.VERSION.SDK_INT == 29 || Build.VERSION.SDK_INT == 30) {
            nearbyPermissionsString = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        } else {
            nearbyPermissionsString = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
        }
    }
    public String[] getNearbyPermissionsString() {
        return nearbyPermissionsString;
    }
    public boolean hasNearbyPermissions(Context c) {
        boolean granted = true;
        for (String permission:nearbyPermissionsString) {
            if (!checkForPermission(c,permission)) {
                granted = false;
            }
        }
        return granted;
    }
    public boolean requestNearbyPermissions(Activity activity, int requestCode) {
        // Determine if there is an issue with any of the preferences
        boolean granted = hasNearbyPermissions(activity);

        // If permission isn't granted - ask
        if (!granted) {
            requestForPermissions(activity,nearbyPermissionsString,requestCode);
        }
        return granted;
    }


    // General checks and requests for permission
    public boolean checkForPermission(Context c, String permission) {
        return ActivityCompat.checkSelfPermission(c,permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestForPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions,requestCode);
    }

    public boolean shouldShowRequestRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,permission);
    }
}

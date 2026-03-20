package com.garethevans.church.opensongtablet.preferences;

import android.Manifest;
import android.bluetooth.BluetoothManager;
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
import com.google.android.play.agesignals.AgeSignalsManager;
import com.google.android.play.agesignals.AgeSignalsManagerFactory;
import com.google.android.play.agesignals.AgeSignalsRequest;
import com.google.android.play.agesignals.model.AgeSignalsVerificationStatus;


public class AppPermissions {

    private final Context context;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String TAG = "Permissions";
    private String permissionsLog = "";
    private boolean ageVerificationPass = true;
    private final MainActivityInterface mainActivityInterface;

    public AppPermissions(Context context) {
        // This class is used to keep all the permissions in the same place
        this.context = context;
        mainActivityInterface = (MainActivityInterface) context;
    }

    // Location
    public boolean locationEnabled(Context c, MainActivityInterface mainActivityInterface) {
        // IV - Nearby requires Location services with network access to discover devices
        boolean network_enabled = false;

        try {
            LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android T / 13  SDK_INT=33
            return new String[]{Manifest.permission.NEARBY_WIFI_DEVICES,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android S / 12  SDK_INT=31
            return new String[]{Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android Q / 10  SDK_INT=29
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

    public String[] getWebServerPermission() {
        return new String[]{
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE};
    }

    public String[] getLocalHostSpotPermission() {
        if (Build.VERSION.SDK_INT >= 33) { //
            return new String[]{Manifest.permission.NEARBY_WIFI_DEVICES,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE};
        } else if (Build.VERSION.SDK_INT >= 31) { // Android S / 12
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE};
        } else if (Build.VERSION.SDK_INT >= 29) { // Android Q / 10
            return new String[]{Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};
        } else { // Older versions!
            return new String[]{Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION};
        }
    }

    public boolean hasWebServerPermission() {
        return checkForPermissions(getWebServerPermission());
    }

    public boolean hasHotSpotPermission() {
        return checkForPermissions(getLocalHostSpotPermission());
    }

    public boolean hasNearbyPermissions() {
        return checkForPermissions(getNearbyPermissions());
    }

    // MIDI
    public String[] getMidiScanPermissions() {
        Log.d(TAG,"checking permissions");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            BluetoothManager bluetoothManager = context.getSystemService(BluetoothManager.class);
            if (bluetoothManager != null) {
                if (bluetoothManager.getAdapter() != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_ADVERTISE};
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_ADVERTISE};
                    } else {
                        return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                    }
                }
            }
        }
        return new String[] {""};
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
        if (permission!=null && !permission.isEmpty()) {
            boolean granted = ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
            permissionsLog += "permission: " + permission + "   granted:" + granted + "\n";
            return granted;
        } else {
            return true;
        }
    }

    public boolean checkForPermissions(String[] permissions) {
        boolean returnVal = true;
        StringBuilder stringBuilder = new StringBuilder();
        if (permissions != null && permissions.length > 0) {
            for (String permission : permissions) {
                boolean thisPermission = checkForPermission(permission);
                stringBuilder.append("permission: ").append(permission).append("   granted:").append(thisPermission).append("\n");
                returnVal = returnVal && thisPermission;
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


    public void checkAgeVerification() {
        // From 1st Jan 2026 Texas requires that apps check age from Google Play API
        if (hasGooglePlay()) {
            // 1. Initialize the manager - use a fake one if testing
            AgeSignalsManager ageSignalsManager = AgeSignalsManagerFactory.create(context);
            //FakeAgeSignalsManager ageSignalsManager = new FakeAgeSignalsManager();

            // 2. Set fake age data if testing
            /*AgeSignalsResult testResult = AgeSignalsResult.builder()
                    .setUserStatus(AgeSignalsVerificationStatus.SUPERVISED)
                    .setAgeLower(13)
                    .setAgeUpper(15)
                    .build();
            ageSignalsManager.setNextAgeSignalsResult(testResult);
            */

            // Create the request
            AgeSignalsRequest request = AgeSignalsRequest.builder().build();

            // 3. Initiate the handshake
            ageSignalsManager.checkAgeSignals(request)
                    .addOnSuccessListener(result -> {
                        Log.d(TAG,"Handshake result:"+result);
                        // Handshake Successful
                        if (result != null) {
                            Integer status = result.userStatus();

                            if (status == null) {
                                // IMPORTANT: If status is null, the user is NOT in a restricted region.
                                Log.d(TAG, "User is not in a regulated region. Full access.");
                                ageVerificationPass = true;
                                return;
                            }

                            // Handle known statuses
                            switch (status) {
                                case AgeSignalsVerificationStatus.VERIFIED:
                                    Log.d(TAG, "User is 18+ verified.");
                                    ageVerificationPass = true;
                                    break;
                                case AgeSignalsVerificationStatus.SUPERVISED:
                                    Log.d(TAG, "User is a supervised minor.");
                                    ageVerificationPass = false;
                                    break;
                                case AgeSignalsVerificationStatus.UNKNOWN:
                                    Log.d(TAG, "Regulated region but status unknown.");
                                    prompUserToVerifyInPlayStore();
                                    ageVerificationPass = false;
                                    break;
                                default:
                                    ageVerificationPass = true;
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handshake Failed (e.g. no internet or service unavailable)
                        Log.d(TAG, "AgeAPI handshake failed: " + e.getMessage());
                        ageVerificationPass = true;
                    });
        }
    }


    private void prompUserToVerifyInPlayStore() {
        Log.d(TAG,"Tell the user to verify their age in the PlayStore");
        // This is the official deep-link/URL for Google Account Age Verification
        mainActivityInterface.openDocument("https://myaccount.google.com/age-verification");
    }

    public boolean ageVerificationPass() {
        return ageVerificationPass;
    }
}

package com.garethevans.church.opensongtablet.preferences;

import android.Manifest;
import android.app.Activity;
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
import com.google.android.play.agesignals.AgeSignalsAccessRequest;
import com.google.android.play.agesignals.AgeSignalsManager;
import com.google.android.play.agesignals.AgeSignalsManagerFactory;
import com.google.android.play.agesignals.AgeSignalsRequest;
import com.google.android.play.agesignals.model.AgeSignalsStatus;


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
        BluetoothManager bm = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        boolean hasBluetooth = (bm != null && bm.getAdapter() != null);

        // 1. Android 13 and Higher (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasBluetooth) {
                return new String[]{
                        Manifest.permission.NEARBY_WIFI_DEVICES,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                };
            } else {
                // Emulators / devices without bluetooth still require location for startDiscovery()
                return new String[]{
                        Manifest.permission.NEARBY_WIFI_DEVICES,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                };
            }
        }

        // 2. Android 12 (API 31 & 32)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (hasBluetooth) {
                return new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADVERTISE,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                };
            } else {
                return new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                };
            }
        }

        // 3. Android 10 & 11 (API 29 & 30)
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        // 4. Legacy Versions (Android 9 and Older)
        else {
            return new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
    }

    public boolean hasGooglePlay() {
        Log.d(TAG,"has GooglePlay:"+(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS));
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

    public void checkAgeVerification(Activity activity) {
        // From 1st Jan 2026 Texas requires that apps check age from Google Play API
        // By default we have permission
        ageVerificationPass = true;

        if (hasGooglePlay()) {
            // 1. Initialize the manager - use a fake one if testing
            AgeSignalsManager ageSignalsManager = AgeSignalsManagerFactory.create(context);
            //FakeAgeSignalsManager ageSignalsManager = new FakeAgeSignalsManager();

            // 2. Set fake age data if testing (if using FakeAgeSignalsManager)
            /*AgeSignalsResult testResult = AgeSignalsResult.builder()
                .setAgeRangeSource(AgeRangeSource.TIER_B) // e.g. Supervised
                .setAgeLower(13)
                .setAgeUpper(15)
                .build();
            ageSignalsManager.setNextAgeSignalsResult(testResult);
            */

            // 3. Request access first (version 0.0.4 two-function architecture requirement)
            // Passing the current Activity allows the Play Store to render the age sharing prompt UI if required.
            AgeSignalsAccessRequest accessRequest = AgeSignalsAccessRequest.builder()
                    .setActivity(activity) // Pass your current Activity reference here
                    .build();

            ageSignalsManager.requestAgeSignalsAccess(accessRequest)
                    .addOnSuccessListener(accessResult -> {
                        Integer accessStatus = accessResult.ageSignalsStatus();
                        if (accessStatus == null) {
                            // Not required
                            Log.d(TAG,"age verification not required");
                            ageVerificationPass = true;

                        } else if (accessStatus != AgeSignalsStatus.SHARED) {
                            // User did not share age, declined, or is in verification-required state
                            Log.d(TAG, "Age signals access not shared or required: " + accessStatus);

                            if (accessStatus == AgeSignalsStatus.VERIFICATION_REQUIRED) {
                                ageVerificationPass = false;
                                prompUserToVerifyInPlayStore();
                            }

                            return;
                        }

                        // 4. Access is granted; retrieve the actual age signals
                        ageSignalsManager.checkAgeSignals(AgeSignalsRequest.builder().build())
                                .addOnSuccessListener(result -> {
                                    Log.d(TAG, "Age signals result: " + result);
                                    if (result != null) {
                                        Integer ageLower = result.ageLower();
                                        Integer ageUpper = result.ageUpper();
                                        Integer ageRangeSource = result.ageRangeSource();

                                        if (ageRangeSource == null|| ageLower == null || ageUpper == null) {
                                            Log.d(TAG, "User is not in a regulated region or data unavailable. Full access.");
                                            ageVerificationPass = true;
                                            return;
                                        }

                                        // Evaluate based on age range or source tier
                                        // Example: checking if lower bound is 18+ or inspecting tier
                                        if (ageLower >= 18) {
                                            Log.d(TAG, "User is 18+ verified/declared.");
                                            ageVerificationPass = true;
                                        } else {
                                            Log.d(TAG, "User is a minor/supervised (" + ageLower + " - " + ageUpper + ").");
                                            ageVerificationPass = false;
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.d(TAG, "checkAgeSignals failed");
                                    ageVerificationPass = true;
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Handshake/Access Request Failed (e.g. no internet or service unavailable)
                        Log.d(TAG, "AgeAPI requestAgeSignalsAccess failed");
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

package com.garethevans.church.opensongtablet.nearby;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.CustomAlertDialog;
import com.garethevans.church.opensongtablet.customviews.MyMaterialButton;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.NearbyConnectionsManagementInterface;
import com.garethevans.church.opensongtablet.interfaces.NearbyReturnActionsInterface;
import com.garethevans.church.opensongtablet.preferences.AreYouSureBottomSheet;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class NearbyConnectionManager implements NearbyConnectionsManagementInterface {

    // This class deals with making and closing connections from the NearbyConnections class
    // Helps keeps the files manageable!

    private final Activity activity;
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final NearbyReturnActionsInterface nearbyReturnActionsInterface;
    private final NearbyActions nearbyActions;

    private final String TAG = "NearbyConnectionManager";

    // General variables
    private boolean usingNearby = false;                // If Nearby connections are initiated (not necessarily connected)
    private boolean isHost = false;                     // Are we acting as a host
    private boolean nearbyStartOnBoot = false;          // Should we start NearbyConnections on app boot
    private boolean firstBoot = true;                   // We have just booted the app
    private boolean nearbyPreferredHost = false;        // Should we be the host
    private final String serviceId = "com.garethevans.church.opensongtablet";
    private boolean nearbyHostPassthrough;              // If we are a host and we hear a payload, do we echo it for others
    private boolean nearbyHostMenuOnly;                 // Do we only advertise when showing the Nearby connections page
    private Strategy nearbyStrategy = Strategy.P2P_CLUSTER;  // Which strategy to use when connecting
    private String deviceId;                            // The ID of this device
    private boolean connectionsOpen = false;            // Do we have the connections fragment page open?
    private int countdown = 0;                          // How many seconds until we stop advertising/discovering
    private Timer timer;
    private TimerTask timerTask;
    private boolean nearbyFileSharing;                  // Allow connected users to get my files
    private SimpleArrayMap<String, String> discoveredDevices;  // The discovered and permission granted devices
    private SimpleArrayMap<String, String> connectedDevices;   // The currently connected devices

    // Advertising variables
    private boolean nearbyTemporaryAdvertise = false;   // Do we only advertise for 10 secs?
    private boolean advertiseInfoRequired = true;       // Alert to let the user know what settings will be used when advertising
    private boolean isAdvertising = false;              // Are we advertising?
    private AdvertisingOptions advertisingOptions;      // Advertising options
    private boolean tempAdvertiseShowStart = true;      // Show the start advertise text
    private boolean tempAdvertiseShowStop = false;      // Show the stop advertise text
    private int countAdvertise = 0;                     // How many times we have attempted advertising (so we don't keep trying indefinitely)

    // Discovering variables
    private boolean discoverInfoRequired = true;        // Alert to let the user know what settings will be used when discovering
    private boolean isDiscovering = false;              // If we are currently discovering devices
    private boolean tempDiscoverShowStart = true;       // Should we show the start discovery?
    private boolean tempDiscoverShowStop = false;       // Should we show the stop discovery?
    private DiscoveryOptions discoveryOptions;          // Discovery options
    private int countDiscovery = 0;                     // How many times we have attempted discovery (so we don't keep trying indefinitely)

    NearbyConnectionManager(Activity activity, Context c, NearbyActions nearbyActions) {
        this.activity = activity;
        this.c = c;
        this.mainActivityInterface = (MainActivityInterface) c;
        this.nearbyReturnActionsInterface = (NearbyReturnActionsInterface) c;
        this.nearbyActions = nearbyActions;

        // Initialise the connections
        connectedDevices = new SimpleArrayMap<>();
        discoveredDevices = new SimpleArrayMap<>();

        getUpdatedPreferences();
    }


    // If we change something or load in a profile, this is called
    public void getUpdatedPreferences() {
        try {
            nearbyHostPassthrough = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyHostPassthrough", true);
            nearbyHostMenuOnly = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyHostMenuOnly", false);
            String preference = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyStrategy", "cluster");
            switch (preference) {
                case "star":
                    nearbyStrategy = Strategy.P2P_STAR;
                    break;
                case "single":
                    nearbyStrategy = Strategy.P2P_POINT_TO_POINT;
                    break;
                case "cluster":
                default:
                    nearbyStrategy = Strategy.P2P_CLUSTER;
                    break;
            }
            setNearbyStrategy(nearbyStrategy);
            nearbyPreferredHost = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyPreferredHost", false);
            if (nearbyPreferredHost && firstBoot) {
                isHost = true;
                usingNearby = true;
            }
            nearbyStartOnBoot = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyStartOnBoot", false);
            if (nearbyStartOnBoot && mainActivityInterface.getAppPermissions().hasNearbyPermissions()) {
                usingNearby = true;
            }
            nearbyTemporaryAdvertise = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyTemporaryAdvertise", false);
            nearbyFileSharing = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyFileSharing",true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        advertisingOptions = new AdvertisingOptions.Builder().setStrategy(nearbyStrategy).build();
        discoveryOptions = new DiscoveryOptions.Builder().setStrategy(nearbyStrategy).build();

        // If we have chosen to, start the default advertise/discover action
        if (firstBoot && nearbyStartOnBoot && nearbyPreferredHost && mainActivityInterface.getAppPermissions().hasNearbyPermissions()) {
            // We want to be the host (based on previous choice)
            if (nearbyTemporaryAdvertise) {
                doTempAdvertise();
            } else {
                startAdvertising();
            }
        } else if (firstBoot && nearbyStartOnBoot && mainActivityInterface.getAppPermissions().hasNearbyPermissions()) {
            // We want to be a client
            doTempDiscover();
        }

        firstBoot = false;
    }


    // Set the strategy as either cluster (many to many) or star (one to many).
    public void setNearbyStrategy(Strategy nearbyStrategy) {
        this.nearbyStrategy = nearbyStrategy;
        advertisingOptions = new AdvertisingOptions.Builder().setStrategy(nearbyStrategy).build();
        discoveryOptions = new DiscoveryOptions.Builder().setStrategy(nearbyStrategy).build();
        if (nearbyStrategy.equals(Strategy.P2P_CLUSTER)) {
            nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_mode) + ": " + c.getString(R.string.connections_mode_cluster));
        } else if (nearbyStrategy.equals(Strategy.P2P_STAR)) {
            nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_mode) + ": " + c.getString(R.string.connections_mode_star));
        } else {
            nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_mode) + ": " + c.getString(R.string.connections_mode_single));
        }
    }
    public String getNearbyStrategyType() {
        if (nearbyStrategy == Strategy.P2P_STAR) {
            return "star";
        } else if (nearbyStrategy == Strategy.P2P_POINT_TO_POINT) {
            return "single";
        } else {
            return "cluster";
        }
    }
    public String getNearbyStrategyStringForMessage() {
        if (nearbyStrategy == Strategy.P2P_STAR) {
            return c.getString(R.string.connections_mode_star);
        } else if (nearbyStrategy == Strategy.P2P_POINT_TO_POINT) {
            return c.getString(R.string.connections_mode_single);
        } else {
            return c.getString(R.string.connections_mode_cluster);
        }
    }


    public String getUserNickname() {
        // To get this far, we have the required permissions
        // A user could have saved their default id in which case use it
        // If not, use Bluetooth or model in that order
        // Get the saved user nickname - the preferred value
        deviceId = mainActivityInterface.getPreferences().getMyPreferenceString("deviceId", "");
        String bluetoothName = "";
        String model;
        // If the deviceId is empty, look for an alternative as a backup
        if (deviceId==null || deviceId.isEmpty()) {
            try {
                model = android.os.Build.MODEL;
                model = model.trim();
            } catch (Exception e) {
                model = c.getString(R.string.unknown);
            }

            try {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null && mainActivityInterface.getAppPermissions().checkForPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                    bluetoothName = bluetoothAdapter.getName();
                }
            } catch (Exception e) {
                e.printStackTrace();
                bluetoothName = "";
            }

            // By default, use the Bluetooth name, but if not, use the model name
            if (bluetoothName!=null && !bluetoothName.isEmpty()) {
                mainActivityInterface.getPreferences().setMyPreferenceString("deviceId",bluetoothName);
                deviceId = bluetoothName;
            } else {
                mainActivityInterface.getPreferences().setMyPreferenceString("deviceId",model);
                deviceId = model;
            }
        }


        // Don't need to save the device name unless the user edits it to make it custom
        return deviceId;
    }


    // Discovery
    @Override
    public void startDiscovery() {
        String message = getSettingsForToast();
        if (discoverInfoRequired) {
            discoverInfoRequired = false;
            try {
                AreYouSureBottomSheet areYouSureBottomSheet =
                        new AreYouSureBottomSheet("NearbyDiscover", message, null,
                                "NearbyConnections", null, null);
                areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "AreYouSure");
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (usingNearby) {
            // The connections fragment turns this off using a system timer
            if (!isDiscovering) {
                if (tempDiscoverShowStart) {
                    mainActivityInterface.getShowToast().doIt(c.getString(R.string.connections_discover));
                    tempDiscoverShowStart = false;
                }
                Nearby.getConnectionsClient(activity)
                        .startDiscovery(serviceId, endpointDiscoveryCallback(), discoveryOptions)
                        .addOnSuccessListener(
                                (Void unused) -> {
                                    // We're discovering!
                                    nearbyActions.getNearbyLogs().updateConnectionLog(c.getResources().getString(R.string.connections_discover));
                                    isDiscovering = true;
                                })
                        .addOnFailureListener(
                                (Exception e) -> {
                                    // We're unable to start discovering.
                                    stopDiscovery();
                                    Log.d(TAG, "startDiscovery() - failure: " + e);
                                });
            }
        }

    }
    @Override
    public void stopDiscovery() {
        if (isDiscovering) {
            if (tempDiscoverShowStop) {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.connections_discover) + ": " + c.getString(R.string.stop));
                tempDiscoverShowStop = false;
            }
            try {
                Nearby.getConnectionsClient(activity).stopDiscovery();
                nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_discover_stop));
            } catch (Exception e) {
                Log.d(TAG, "stopDiscovery() - failure: " + e);
            }
        }
        isDiscovering = false;
        tempDiscoverShowStart = true;
        tempDiscoverShowStop = false;
    }
    public void doTempDiscover() {
        // Stop advertising/discovering if we were already doing that
        stopAdvertising();
        stopDiscovery();

        // If we haven't accepted the info, do that by calling discover (it checks)
        if (discoverInfoRequired) {
            startDiscovery();
        } else {
            // After a short delay, discover
            mainActivityInterface.getMainHandler().postDelayed(() -> {
                try {
                    startDiscovery();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 200);

            // After 10 seconds, stop discovering
            mainActivityInterface.getMainHandler().postDelayed(() -> {
                tempDiscoverShowStop = countDiscovery >= 2;
                if (hasValidConnections()) {
                    tempDiscoverShowStop = true;
                }
                try {
                    stopDiscovery();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countDiscovery++;
                if (countDiscovery < 3 && !hasValidConnections()) {
                    // Repeat the process again
                    doTempDiscover();
                }
            }, 10000);
        }
    }


    // Advertising
    @Override
    public void startAdvertising() {
        String message = getSettingsForToast();
        if (advertiseInfoRequired) {
            try {
                advertiseInfoRequired = false;
                AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("NearbyAdvertise", message, null, "NearbyConnections", null, null);
                areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "AreYouSure");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (!isAdvertising) {
            if (!nearbyTemporaryAdvertise || tempAdvertiseShowStart) {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.connections_advertising));
                tempAdvertiseShowStart = false;
            }
            Nearby.getConnectionsClient(activity)
                    .startAdvertising(getUserNickname(), serviceId, connectionLifecycleCallback(), advertisingOptions)
                    .addOnSuccessListener(
                            (Void unused) -> {
                                // We're advertising!
                                nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_advertise_name) + " " + getUserNickname());
                                isAdvertising = true;
                            })
                    .addOnFailureListener(
                            (Exception e) -> {
                                // We were unable to start advertising.
                                nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_advertising) + " " + c.getString(R.string.error));
                                Log.d(TAG, "startAdvertising() - failure: " + e);
                            });
        }
    }
    public void doTempAdvertise() {
        // Stop advertising/discovering if we were already doing that
        stopAdvertising();
        stopDiscovery();

        // If we haven't accepted the info, do that by calling advertise (it checks)
        if (advertiseInfoRequired) {
            startAdvertising();
        } else {
            // After a short delay, advertise
            new Handler().postDelayed(() -> {
                try {
                    startAdvertising();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 200);

            // After 10 seconds, stop advertising
            new Handler().postDelayed(() -> {
                try {
                    tempAdvertiseShowStop = countAdvertise >= 2;
                    if (hasValidConnections()) {
                        tempAdvertiseShowStop = true;
                    }

                    stopAdvertising();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countAdvertise++;
                if (countAdvertise < 3 && !hasValidConnections()) {
                    // Repeat the process again
                    doTempAdvertise();
                }
            }, 10000);
        }
    }
    @Override
    public void stopAdvertising() {
        if (isAdvertising) {
            if (!nearbyTemporaryAdvertise || tempAdvertiseShowStop) {
                mainActivityInterface.getShowToast().doIt(c.getString(R.string.connections_advertise) + ": " + c.getString(R.string.stop));
                tempAdvertiseShowStop = false;
            }
            isAdvertising = false;
            try {
                Nearby.getConnectionsClient(activity).stopAdvertising();
                nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_service_stop));
            } catch (Exception e) {
                Log.d(TAG, "stopAdvertising() - failure: " + e);
            }
        }
        tempAdvertiseShowStart = true;
        tempAdvertiseShowStop = false;
    }



    @Override
    public void turnOffNearby() {
        try {
            Nearby.getConnectionsClient(activity).stopAllEndpoints();
        } catch (Exception e) {
            Log.d(TAG, "Can't turn off nearby");
        }
        clearTimer();
        initialiseCountdown();
        stopAdvertising();
        stopDiscovery();
        isHost = false;
        usingNearby = false;
        connectedDevices.clear();
    }

    // The bottom sheet content for starting Nearby on boot
    private String getSettingsForToast() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(c.getString(R.string.connections_description)).append("\n\n");
        String on_string = c.getString(R.string.on);
        String off_string = c.getString(R.string.off);
        // Show the user's preferences
        if (isHost || (nearbyStartOnBoot && nearbyPreferredHost)) {
            isHost = true;
            stringBuilder.append(c.getString(R.string.connections_actashost)).append("\n");
            stringBuilder.append(c.getString(R.string.connections_mode)).append(": ").append(getNearbyStrategyStringForMessage()).append("\n");
            stringBuilder.append(c.getString(R.string.nearby_host_menu_only_info_warning)).append(": ").append(nearbyHostMenuOnly ? on_string : off_string).append("\n");
            stringBuilder.append(c.getString(R.string.connections_host_passthrough)).append(": ").append(nearbyHostPassthrough ? on_string : off_string).append("\n");
            stringBuilder.append(c.getString(R.string.connections_advertise_temporary)).append(": ").append(nearbyTemporaryAdvertise ? on_string : off_string).append("\n");
            stringBuilder.append(c.getString(R.string.connections_start_on_boot)).append(": ").append(nearbyStartOnBoot ? on_string : off_string);
        } else {
            stringBuilder.append(c.getString(R.string.connections_actasclient)).append("\n");
            stringBuilder.append(c.getString(R.string.connections_mode)).append(": ").append(getNearbyStrategyStringForMessage()).append("\n");
            stringBuilder.append(c.getString(R.string.connections_receive_host)).append(": ").append(nearbyActions.getNearbyReceivePayloads().getNearbyReceiveHostFiles() ? on_string : off_string).append("\n");
            stringBuilder.append(c.getString(R.string.connections_keephostsongs)).append(": ").append(nearbyActions.getNearbyReceivePayloads().getNearbyKeepHostFiles() ? on_string : off_string).append("\n");
            stringBuilder.append(c.getString(R.string.connection_scroll_info)).append(": ").append(nearbyActions.getNearbyReceivePayloads().getNearbyReceiveHostScroll() ? on_string : off_string).append("\n");
            stringBuilder.append(c.getString(R.string.connection_autoscroll_info)).append(": ").append(nearbyActions.getNearbyReceivePayloads().getNearbyReceiveHostAutoscroll() ? on_string : off_string).append("\n");
            stringBuilder.append(c.getString(R.string.connections_song_sections_info)).append(": ").append(nearbyActions.getNearbyReceivePayloads().getNearbyReceiveHostSongSections() ? on_string : off_string).append("\n");
            stringBuilder.append(c.getString(R.string.connection_match_to_pdf_song)).append(": ").append(nearbyActions.getNearbyReceivePayloads().getNearbyMatchToPDFSong() ? on_string : off_string);
        }

        return stringBuilder.toString();
    }



    // Make the connections
    // THIS IS USED IF WE ARE ADVERTISING AND A CLIENT INITIATES THE CONNECTION
    private ConnectionLifecycleCallback connectionLifecycleCallback() {
        return new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                // Client has tried to connect.  Get a string for the connection
                String endpointName = connectionInfo.getEndpointName();
                Log.d(TAG, "Connection initiated   endpontId:"+endpointId+"  ("+endpointName+")");

                // Check to see if this device was already discovered (permission granted)
                // If the device was previously registered, try to reconnect silently
                if (recognisedDiscoveredDevice(endpointId, connectionInfo.getEndpointName())) {
                    Log.d(TAG, "We have previously connected to " + endpointId + " ("+ endpointName+").  Attempt reconnect");
                    delayAcceptConnection(endpointId,connectionInfo.getEndpointName());

                } else {
                    Log.d(TAG, "Device wasn't previously connected: " + endpointId + " ("+endpointName+").  Get connection permission");
                    // Allow clients to connect to the host when the Connect menu is open, or the user switches off the requirement for the Connect menu to be open
                    if (connectionsOpen || !mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyHostMenuOnly", false)) {

                        CustomAlertDialog.showStyledDialog(
                                c, mainActivityInterface,
                                c.getString(R.string.connections_accept) + " " + endpointName,
                                c.getString(R.string.connections_accept_code) + " " + connectionInfo.getAuthenticationDigits(),
                                (DialogInterface d, int which) -> {
                                    nearbyActions.getNearbyReceivePayloads().setForceReload(true);
                                    delayAcceptConnection(endpointId,endpointName);
                                },
                                (DialogInterface d, int which) ->
                                        // The user canceled, so we should reject the connection.
                                        Nearby.getConnectionsClient(activity).rejectConnection(endpointId),
                                R.drawable.alert
                        );
                    } else {
                        // The user is not accepting new connections, so we should reject the connection.
                        Log.d(TAG, "reject connection to " + endpointId + " ("+endpointName+").  User not accepting new connections");
                        Nearby.getConnectionsClient(activity).rejectConnection(endpointId);
                    }
                }
            }

            @Override
            public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution connectionResolution) {
                String endpointName = getNameMatchingId(endpointId);
                Log.d(TAG,"onConnectionResult()  endpointName:"+endpointName);
                switch (connectionResolution.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                    case ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT:
                        Log.d(TAG, "connections status either ok or already connected");
                        // Add connection record if it doesn't exist
                        updateConnectedEndpoints(endpointId, endpointName, true);
                        updateDiscoveredEndpoints(endpointId, endpointName, true);
                        nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_connected) + " " + endpointId + " ("+endpointName+")");
                        nearbyActions.getNearbyReceivePayloads().setForceReload(true);
                        if (isHost) {
                            // try to send the current song payload
                            nearbyActions.getNearbySendPayloads().sendSongPayload();
                        }
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        Log.d(TAG, "Rejected");
                        // Remove connection from all records - need to try again
                        updateConnectedEndpoints(endpointId, endpointName, false);
                        updateDiscoveredEndpoints(endpointId, endpointName, false);
                        nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.cancel));
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        Log.d(TAG, "Error status code");
                        // The connection broke before it was able to be accepted.
                        // The connection was rejected by one or both sides.
                        nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_failure) + " " + getUserNickname() +
                                " <-> " + endpointName);
                        // Remove connection from all records - need to try again
                        updateDiscoveredEndpoints(endpointId, endpointName, false);
                        updateConnectedEndpoints(endpointId, endpointName, false);
                        break;
                    default:
                        // Unknown status code
                        Log.d(TAG, "Unknown status code");
                        break;
                }
            }

            @Override
            public void onDisconnected(@NonNull String endpointId) {
                // The endpointId is just the id, so we need to find a nice name
                Log.d(TAG,"onDisconnected: " + endpointId + "  Check the discoveredDevices");
                for (int i=0; i<discoveredDevices.size(); i++) {
                    Log.d(TAG,"discoveredDevices: " + discoveredDevices.keyAt(i) + " ("+discoveredDevices.valueAt(i)+")");
                }
                Log.d(TAG,"onDisconnected: " + endpointId + "  Check the connectedDevices");
                for (int i=0; i<connectedDevices.size(); i++) {
                    Log.d(TAG,"connectedDevices: " + connectedDevices.keyAt(i) + " ("+connectedDevices.valueAt(i)+")");
                }
                String endpointName = getNameMatchingId(endpointId);
                Log.d(TAG, "On disconnected: " + endpointId + " ("+endpointName+")");

                // Remove from the connectedDevices but not discoveredDevices
                updateConnectedEndpoints(endpointId, endpointName, false);
                nearbyActions.getNearbyLogs().updateConnectionLog(c.getResources().getString(R.string.connections_disconnect) +
                        " " + endpointName);

                Handler h = new Handler();
                if (!isHost) {
                    // Clients should try to silently connect again after 2 seconds
                    h.postDelayed(() -> {
                        countDiscovery = 0;
                        doTempDiscover();
                    }, 2000);
                } else {
                    // Hosts should advertise again
                    h.postDelayed(() -> {
                        countAdvertise = 0;
                        doTempAdvertise();
                    }, 2000);
                }
            }
        };
    }
    // THIS IS USED IF WE ARE DISCOVERING AND HAVE DISCOVERED A DEVICE ADVERTISING A CONNECTION
    private EndpointDiscoveryCallback endpointDiscoveryCallback() {
        return new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                String endpointName = discoveredEndpointInfo.getEndpointName();
                Log.d(TAG, "EndpointDiscoveryCallback  endpoint:" + endpointId + " ("+endpointName+")");
                if (!recognisedDiscoveredDevice(endpointId, endpointName)) {

                    Log.d(TAG, endpointId + " ("+endpointName+") is not found a recognised device, so attempt connection with permission");
                    // Only attempt a connection if we aren't already connected
                    Nearby.getConnectionsClient(activity)
                            .requestConnection(getUserNickname(), endpointId, connectionLifecycleCallback())
                            .addOnSuccessListener(
                                    (Void unused) -> {
                                        Log.d(TAG, "On success.  Trying to connect to host : " + endpointId + " ("+endpointName+")");
                                        // We successfully requested a connection. Now both sides
                                        // must accept before the connection is established.
                                        nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_searching));
                                    })
                            .addOnFailureListener(
                                    (Exception e) -> {
                                        Log.d(TAG, "On failure: " + (((ApiException) e).getStatus().getStatusMessage()));

                                        // IV - Added handling of when already connected
                                        if (((ApiException) e).getStatusCode() == ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT) {
                                            Log.d(TAG, endpointId + " ("+endpointName+") was already connected");
                                            // Check we have both records
                                            updateConnectedEndpoints(endpointId, endpointName, true);
                                            updateDiscoveredEndpoints(endpointId, endpointName, true);
                                            nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_connected) + " " + endpointName);

                                            // IV - Already connected so replay last incoming song
                                            nearbyActions.getNearbyReceivePayloads().loadLastSong();

                                            // We can stop discovery now
                                            stopDiscovery();
                                        } else {
                                            // Nearby Connections failed to request the connection.
                                            Log.d(TAG, "A general error");
                                            nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_failure) + " " + endpointName);
                                        }
                                        Log.d(TAG, "Connections failure: " + e);
                                    });
                } else {
                    Log.d(TAG, endpointId + " ("+endpointName+") already a recognised device.  Try to connect automatically");
                    delayAcceptConnection(endpointId, endpointName);
                }
            }

            @Override
            public void onEndpointLost(@NonNull String endpointId) {
                String endpointName = getNameMatchingId(endpointId);
                Log.d(TAG, "onEndPointlost: " + endpointId + " ("+endpointName+")");
                // Remove from the connected devices (but keep in recognised devices)
                updateConnectedEndpoints(endpointId, endpointName, false);
                nearbyActions.getNearbyLogs().updateConnectionLog(c.getString(R.string.connections_disconnect) + " " + endpointName);
                // Try to connect again after 2 seconds
                if (!isHost) {
                    Handler h = new Handler();
                    h.postDelayed(() -> {
                        countDiscovery = 0;
                        doTempDiscover();
                    }, 2000);
                }
            }


        };
    }
    // ONCE PERMISSION FOR CONNECTIONS HAVE BEEN ACCEPTED, CONNECT!
    private void delayAcceptConnection(String endpointId, String endpointName) {
        // For stability add a small delay
        Handler waitAccept = new Handler();
        waitAccept.postDelayed(() -> {
            // Add a note of the nice name on to the endpointId
            Log.d(TAG, "about to try and accept " + endpointId + " ("+endpointName+")");
            updateDiscoveredEndpoints(endpointId, endpointName, true);
            updateConnectedEndpoints(endpointId, endpointName, true);
            // The user confirmed, so we can accept the connection.
            Nearby.getConnectionsClient(activity)
                    .acceptConnection(endpointId, nearbyActions.getNearbyReceivePayloads().payloadCallback());
        }, 200);
    }


    // Deal with endpoints -  the identifiers for connected devices
    // The endpointId is a random bit of code that identifies a device
    // The connectionInfo.getEndpointName() is a user readable name of a device
    // Once a connection is made we store both as a string like id__name
    // These strings are stored in the connectedEndpoints arraylist
    // Any device we discover is stored in discoveredEndpoints arraylist so we can get a name
    private void updateConnectedEndpoints(String endpointId, String endpointName, boolean addEndpoint) {
        if (endpointName==null || endpointName.isEmpty()) {
            endpointName = getNameMatchingId(endpointId);
        }
        if (addEndpoint) {
            // Add to the connected list if not already there
            if (!recognisedConnectedDevice(endpointId, endpointName)) {
                Log.d(TAG, "ADD: " + endpointId + " ("+endpointName+") was not in connectedEndpoints - adding");
                connectedDevices.put(endpointId, endpointName);
                discoveredDevices.put(endpointId, endpointName);
            } else {
                Log.d(TAG, "ADD: " + endpointId + " ("+endpointName+") was already in connectedEndpoints - skip");
            }
        } else {
            if (recognisedConnectedDevice(endpointId, endpointName)) {
                // Remove the connected device
                Log.d(TAG, "REMOVE: " + endpointId + " ("+endpointName+") was already in connectedEndpoints - remove");
                removeRecognisedConnectedDevice(endpointId, endpointName);
            } else {
                Log.d(TAG, "REMOVE: " + endpointId + " ("+ endpointName+") was not in connectedEndpoints - ignore");
            }
        }
    }
    private void updateDiscoveredEndpoints(String endpointId, String endpointName, boolean addEndpoint) {
        if (addEndpoint) {
            // Add to the discovered lists (recognised devices) if not already there
            if (!recognisedDiscoveredDevice(endpointId, endpointName)) {
                Log.d(TAG, "ADD: " + endpointId + " ("+endpointName+") was not in discoveredEndpoints - adding");
                discoveredDevices.put(endpointId,endpointName);
            } else {
                Log.d(TAG, "ADD: " + endpointId + " ("+endpointName+") was already in discoveredEndpoints - skip");
            }
        } else {
            if (recognisedDiscoveredDevice(endpointId,endpointName)) {
                Log.d(TAG, "REMOVE: " + endpointId + " ("+endpointName+") was in discoveredEndpoints - remove");
                discoveredDevices.remove(endpointId,endpointName);
            } else {
                Log.d(TAG, "REMOVE: " + endpointId + " ("+endpointName+") was not in discoveredEndpoints - skip");
            }
        }
    }


    private boolean recognisedConnectedDevice(String endpointId, String endpointName) {
        Log.d(TAG,"recognisedConnectedDevice("+endpointId+", "+endpointName);
        // A full match is if we have both the id and the name
        String nameMatchingEndpointId = "";
        String idMatchingEndpointName = "";
        boolean idExists = false;
        boolean nameExists = false;
        String oldKey = "";

        // Go through the discoveredDevices and get any matches
        for (int i=0; i<connectedDevices.size(); i++) {
            if (connectedDevices.keyAt(i).equals(endpointId)) {
                idExists = true;
                nameMatchingEndpointId = connectedDevices.valueAt(i);
            }
            if (connectedDevices.valueAt(i).equals(endpointName)) {
                nameExists = true;
                oldKey = connectedDevices.keyAt(i);
                idMatchingEndpointName = connectedDevices.keyAt(i);
            }
            if (nameExists && idExists) {
                break;
            }
        }

        // A full match is if all Strings are equal
        boolean fullMatch = nameMatchingEndpointId.equals(endpointName) &&
                idMatchingEndpointName.equals(endpointId);
        Log.d(TAG,"fullMatch:"+fullMatch);
        boolean fixedMatch = false;
        Log.d(TAG,"nameExists:"+nameExists+"  oldKey:"+oldKey);
        if (!fullMatch && nameExists && !oldKey.isEmpty()) {
            // If we have a matching name but with a different id, we need to update the id
            connectedDevices.remove(oldKey);
            connectedDevices.put(endpointId, endpointName);
            fixedMatch = true;
        }
        return fullMatch || fixedMatch;
    }
    private boolean recognisedDiscoveredDevice(String endpointId, String endpointName) {
        Log.d(TAG,"recognisedDiscoveredDevice("+endpointId+", "+endpointName);
        // A full match is if we have both the id and the name
        String nameMatchingEndpointId = "";
        String idMatchingEndpointName = "";
        boolean idExists = false;
        boolean nameExists = false;
        String oldKey = "";

        // Go through the discoveredDevices and get any matches
        for (int i=0; i<discoveredDevices.size(); i++) {
            if (discoveredDevices.keyAt(i).equals(endpointId)) {
                idExists = true;
                nameMatchingEndpointId = discoveredDevices.valueAt(i);
                Log.d(TAG,"matching endpointId:"+endpointId+"  discoveredDevices.valueAt(i):"+discoveredDevices.valueAt(i));
            }
            if (discoveredDevices.valueAt(i).equals(endpointName)) {
                nameExists = true;
                oldKey = discoveredDevices.keyAt(i);
                idMatchingEndpointName = discoveredDevices.keyAt(i);
                Log.d(TAG,"matching endpointName:"+endpointName+"  discoveredDevices.keyAt(i):"+discoveredDevices.keyAt(i));
            }
            if (nameExists && idExists) {
                break;
            }
        }

        // A full match is if all Strings are equal
        boolean fullMatch = nameMatchingEndpointId.equals(endpointName) &&
                idMatchingEndpointName.equals(endpointId);
        Log.d(TAG,"fullMatch:"+fullMatch);
        boolean fixedMatch = false;
        Log.d(TAG,"nameExists:"+nameExists+"  oldKey:"+oldKey);
        if (!fullMatch && nameExists && !oldKey.isEmpty()) {
            // If we have a matching name but with a different id, we need to update the id
            discoveredDevices.remove(oldKey);
            discoveredDevices.put(endpointId, endpointName);
            fixedMatch = true;
        }
        return fullMatch || fixedMatch;
    }
    private void removeRecognisedConnectedDevice(String endpointId, String endpointName) {
        if (connectedDevices.containsKey(endpointId)) {
            connectedDevices.remove(endpointId);
        } else if (connectedDevices.containsValue(endpointName)) {
            for (int i = 0; i < connectedDevices.size(); i++) {
                String value = connectedDevices.valueAt(i);
                if (value.equals(endpointName)) {
                    String id = connectedDevices.keyAt(i);
                    connectedDevices.remove(id);
                    break;
                }
            }
        }
    }


    public boolean hasValidConnections() {
        if (usingNearby) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                if (!connectedDevices.isEmpty()) {
                    for (int i = 0; i < connectedDevices.size(); i++) {
                        if (connectedDevices.valueAt(i)!=null) {
                            stringBuilder.append(connectedDevices.valueAt(i));
                        }
                    }
                }
                return !stringBuilder.toString().isEmpty();
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }
    private String getNameMatchingId(String endpointId) {
        String nicename = "";
        // Use discovered endpoints as we may have not fully established the connection yet
        for (int i=0; i<discoveredDevices.size(); i++) {
            Log.d(TAG,"discoveredDevice:"+discoveredDevices.keyAt(i)+ " ("+discoveredDevices.valueAt(i)+")");
            if (discoveredDevices.keyAt(i).equals(endpointId)) {
                nicename = discoveredDevices.valueAt(i);
                break;
            }
        }
        return nicename;
    }
    public String getConnectedDevicesAsString() {
        if (connectedDevices == null || connectedDevices.isEmpty()) {
            return c.getString(R.string.connections_no_devices);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i=0; i<connectedDevices.size(); i++) {
                if (connectedDevices.valueAt(i)!=null) {
                    stringBuilder.append(connectedDevices.valueAt(i)).append("\n");
                }
            }
            return stringBuilder.toString().trim();
        }
    }


    // The timer to stop advertising/discovery
    public void initialiseCountdown() {
        // Timer for stop of discovery and advertise (only one can happen at a time)
        countdown = 10;
    }
    public void setTimer(boolean advertise, MyMaterialButton materialButton) {
        clearTimer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (countdown == 0) {
                        nearbyReturnActionsInterface.nearbyEnableConnectionButtons();
                    } else {
                        nearbyReturnActionsInterface.nearbyUpdateCountdownText(advertise, materialButton);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);
    }
    public void clearTimer() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timer != null) {
            timer.purge();
        }
    }
    public int getCountdown() {
        return countdown;
    }
    public void doCountdown() {
        countdown--;
    }


    public void clearEndpoints() {
        // Do this with a delay
        new Handler().postDelayed(() -> {
            connectedDevices.clear();
            discoveredDevices.clear();
        },500);
    }


    public boolean sendAsHost() {
        return hasValidConnections() && isHost;
    }


    // The getters
    public boolean getUsingNearby() {
        return usingNearby;
    }
    public boolean getIsHost() {
        return isHost;
    }
    public boolean getConnectionsOpen() {
        return connectionsOpen;
    }
    public SimpleArrayMap<String,String> getConnectedDevices() {
        return connectedDevices;
    }

    public ArrayList<String> getBundleDiscoveredDevices() {
        ArrayList<String> bundle = new ArrayList<>();
        for (int i=0; i<discoveredDevices.size(); i++) {
           bundle.add(discoveredDevices.keyAt(i) + nearbyActions.endpointSplit + discoveredDevices.valueAt(i));
        }
        return bundle;
    }
    public ArrayList<String> getBundleConnectedDevices() {
        ArrayList<String> bundle = new ArrayList<>();
        for (int i=0; i<connectedDevices.size(); i++) {
            bundle.add(connectedDevices.keyAt(i) + nearbyActions.endpointSplit + connectedDevices.valueAt(i));
        }
        return bundle;
    }

    public boolean getNearbyTemporaryAdvertise() {
        return nearbyTemporaryAdvertise;
    }
    public String getDeviceId() {
        return deviceId;
    }
    public boolean getNearbyHostMenuOnly() {
        return nearbyHostMenuOnly;
    }
    public boolean getNearbyStartOnBoot() {
        return nearbyStartOnBoot;
    }
    public boolean getNearbyPreferredHost() {
        return nearbyPreferredHost;
    }
    public boolean getIsAdvertising() {
        return isAdvertising;
    }
    public Strategy getNearbyStrategy() {
        return nearbyStrategy;
    }
    public boolean getNearbyHostPassthrough() {
        return nearbyHostPassthrough;
    }
    public boolean getNearbyFileSharing() {
        return nearbyFileSharing;
    }

    // The setters
    public void setUsingNearby(boolean usingNearby) {
        this.usingNearby = usingNearby;
    }
    public void setIsHost(boolean isHost) {
        this.isHost = isHost;
        setNearbyPreferredHost(isHost);
    }
    public void setConnectionsOpen(boolean connectionsOpen) {
        this.connectionsOpen = connectionsOpen;
    }
    public void setDiscoveredEndpoints(ArrayList<String> discoveredEndpointsBundled) {
        // Called on resume from saved bundle.  Reset the temp counts
        countAdvertise = 0;
        countDiscovery = 0;
        Log.d(TAG,"TODO: setDiscoveredEndpoints()");
        if (discoveredEndpointsBundled != null && !discoveredEndpointsBundled.isEmpty()) {
            if (discoveredDevices == null) {
                discoveredDevices = new SimpleArrayMap<>();
            }
            for (String de : discoveredEndpointsBundled) {
                String[] deSplit = de.split(nearbyActions.endpointSplit);
                String id  = deSplit[0];
                String name = deSplit[1];
                if (!discoveredDevices.containsKey(id)) {
                    discoveredDevices.put(id, name);
                }
                if (!discoveredDevices.containsValue(name)) {
                    discoveredDevices.put(id, name);
                }
            }
        }
    }
    public void setConnectedEndpoints(ArrayList<String> connectedEndpointsBundled) {
        Log.d(TAG,"TODO: setConnectedendpoints()");
        if (connectedEndpointsBundled != null && !connectedEndpointsBundled.isEmpty()) {
            if (connectedDevices == null) {
                connectedDevices = new SimpleArrayMap<>();
            }
            for (String de : connectedEndpointsBundled) {
                String[] deSplit = de.split(nearbyActions.endpointSplit);
                String id  = deSplit[0];
                String name = deSplit[1];
                if (!connectedDevices.containsKey(id)) {
                    connectedDevices.put(id, name);
                }
                if (!connectedDevices.containsValue(name)) {
                    connectedDevices.put(id, name);
                }
            }
        }
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        mainActivityInterface.getPreferences().getMyPreferenceString("deviceId", deviceId);
    }
    public void setNearbyHostMenuOnly(boolean nearbyHostMenuOnly) {
        this.nearbyHostMenuOnly = nearbyHostMenuOnly;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyHostMenuOnly", nearbyHostMenuOnly);
    }
    public void setNearbyPreferredHost(boolean nearbyPreferredHost) {
        this.nearbyPreferredHost = nearbyPreferredHost;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyPreferredHost",nearbyPreferredHost);
    }
    public void setNearbyHostPassthrough(boolean nearbyHostPassthrough) {
        this.nearbyHostPassthrough = nearbyHostPassthrough;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyHostPassthrough",nearbyHostPassthrough);
    }
    public void setNearbyTemporaryAdvertise(boolean nearbyTemporaryAdvertise) {
        this.nearbyTemporaryAdvertise = nearbyTemporaryAdvertise;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyTemporaryAdvertise",nearbyTemporaryAdvertise);
    }
    public void setNearbyStartOnBoot(boolean nearbyStartOnBoot) {
        this.nearbyStartOnBoot = nearbyStartOnBoot;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyStartOnBoot",nearbyStartOnBoot);
    }
    public void setNearbyFileSharing(boolean nearbyFileSharing) {
        this.nearbyFileSharing = nearbyFileSharing;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyFileSharing",nearbyFileSharing);
    }
}

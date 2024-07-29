package com.garethevans.church.opensongtablet.nearby;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.collection.SimpleArrayMap;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.NearbyInterface;
import com.garethevans.church.opensongtablet.interfaces.NearbyReturnActionsInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
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
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.material.button.MaterialButton;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

// Big updates here
// Users can specify Nearby connection strategy as cluster, star or single
// P2P_CLUSTER:  Mesh style anyone can connect to anyone.  Clients repeat payload they receive
// P2P_STAR:  Only one device (host) is the centre of the star, the clients are all spokes
// P2P_POINT_TO_POINT:  Only two devices allowed (end-to-end) named as single here.
// Regardless if devices are set as hosts or clients, they can advertise or discover to help connect
// Changing the mode doesn't automatically advertise/discover.
// Once connection has been established, the host/client button is an internal check (not affected by mode)


public class NearbyConnections implements NearbyInterface {

    private final Activity activity;
    private final Context c;
    private final String TAG = "NearbyConnections", sectionTag = "___section___",
            scrollByTag = "___scrollby___", scrollToTag = "___scrollto___",
            autoscrollPause = "___autoscrollpause___", autoscrollincrease = "___autoscrollincrease___",
            autoscrolldecrease = "___autoscrolldecrease___", profiles = "___profiles___",
            songTag = "_xx____xx_", endpointSplit = "__", sets = "___sets___",
            hostRequest = "___hostRequest___", hostItems = "___hostItems___",
            messageTag = "___message___", songs = "___songs___", currentset = "___currentset___",
            requestFileTag = "___requestFile___", requestFileSeparator = "___rFS___",
            serviceId = "com.garethevans.church.opensongtablet";
    private int countDiscovery = 0, countAdvertise = 0;
    private ArrayList<String> connectedEndpoints;  // CODE_DeviceName - currently connected
    private ArrayList<String> discoveredEndpoints; // CODE__DeviceName - permission already given
    private final NearbyReturnActionsInterface nearbyReturnActionsInterface;
    private final MainActivityInterface mainActivityInterface;
    private BrowseHostFragment browseHostFragment;
    private boolean forceReload = false;

    private Timer timer;
    private TimerTask timerTask;
    private SimpleArrayMap<Long, Payload> incomingFilePayloads = new SimpleArrayMap<>();
    private SimpleArrayMap<Long, String> fileNewLocation = new SimpleArrayMap<>();
    private Payload requestedFilePayload;
    private boolean isHost, receiveHostFiles, keepHostFiles, usingNearby, temporaryAdvertise,
            isAdvertising = false, isDiscovering = false, nearbyHostMenuOnly,
            receiveHostAutoscroll = true, receiveHostSongSections = true, connectionsOpen,
            nearbyHostPassthrough, receiveHostScroll, matchToPDFSong, nearbyMessageSticky,
            nearbyMessageMIDIAction;
    private String nearbyMessage1, nearbyMessage2, nearbyMessage3, nearbyMessage4, nearbyMessage5,
            nearbyMessage6, nearbyMessage7, nearbyMessage8;

    private AdvertisingOptions advertisingOptions;
    private DiscoveryOptions discoveryOptions;
    // The stuff used for Google Nearby for connecting devices
    private String payloadTransferIds = "", receivedSongFilename, connectionLog, deviceId,
            incomingPrevious;
    private Payload previousPayload;
    private int pendingSection = 0, countdown;
    private boolean sendSongDelayActive;
    private Strategy nearbyStrategy = Strategy.P2P_CLUSTER;


    // Initialise the class, preferences and interfaces
    public NearbyConnections(Activity activity, Context c) {
        this.activity = activity;
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        nearbyReturnActionsInterface = (NearbyReturnActionsInterface) c;

        // Initialise the connections
        connectedEndpoints = new ArrayList<>();
        discoveredEndpoints = new ArrayList<>();
        connectionLog = "";

        // Get user preferences
        getUpdatedPreferences();
    }

    // If we change load in a profile, this is called
    public void getUpdatedPreferences() {
        try {
            nearbyHostPassthrough = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyHostPassthrough", true);
            nearbyHostMenuOnly = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyHostMenuOnly", false);
            temporaryAdvertise = mainActivityInterface.getPreferences().getMyPreferenceBoolean("temporaryAdvertise", false);
            String preference = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyStrategy", "cluster");
            switch (preference) {
                case "cluster":
                default:
                    nearbyStrategy = Strategy.P2P_CLUSTER;
                    break;
                case "star":
                    nearbyStrategy = Strategy.P2P_STAR;
                    break;
                case "single":
                    nearbyStrategy = Strategy.P2P_POINT_TO_POINT;
                    break;
            }
            setNearbyStrategy(nearbyStrategy);
            matchToPDFSong = mainActivityInterface.getPreferences().getMyPreferenceBoolean("matchToPDFSong", false);
            nearbyMessageSticky = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyMessageSticky", false);
            nearbyMessage1 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage1", "");
            nearbyMessage2 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage2", "");
            nearbyMessage3 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage3", "");
            nearbyMessage4 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage4", "");
            nearbyMessage5 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage5", "");
            nearbyMessage6 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage6", "");
            nearbyMessage7 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage7", "");
            nearbyMessage8 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage8", "");
            nearbyMessageMIDIAction = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyMessageMIDIAction", true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        advertisingOptions = new AdvertisingOptions.Builder().setStrategy(nearbyStrategy).build();
        discoveryOptions = new DiscoveryOptions.Builder().setStrategy(nearbyStrategy).build();
    }

    public void clearEndpoints() {
        connectedEndpoints.clear();
        discoveredEndpoints.clear();
    }

    // Our preferences for using Nearby
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

        Log.d(TAG,"deviceId: " + deviceId);

        // Don't need to save the device name unless the user edits it to make it custom
        return deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setReceiveHostFiles(boolean receiveHostFiles) {
        this.receiveHostFiles = receiveHostFiles;
    }

    public boolean getReceiveHostFiles() {
        return receiveHostFiles;
    }

    public void setKeepHostFiles(boolean keepHostFiles) {
        this.keepHostFiles = keepHostFiles;
    }

    public boolean getKeepHostFiles() {
        return keepHostFiles;
    }

    public void setReceiveHostSongSections(boolean receiveHostSongSections) {
        this.receiveHostSongSections = receiveHostSongSections;
    }

    public boolean getReceiveHostSongSections() {
        return receiveHostSongSections;
    }

    public void setConnectionsOpen(boolean connectionsOpen) {
        this.connectionsOpen = connectionsOpen;
    }

    public boolean getConnectionsOpen() {
        return connectionsOpen;
    }

    public void setNearbyHostMenuOnly(boolean nearbyHostMenuOnly) {
        this.nearbyHostMenuOnly = nearbyHostMenuOnly;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyHostMenuOnly", nearbyHostMenuOnly);
    }

    public boolean getNearbyHostMenuOnly() {
        return nearbyHostMenuOnly;
    }

    public boolean getIsHost() {
        return isHost;
    }

    public void setIsHost(boolean isHost) {
        this.isHost = isHost;
    }

    public boolean getUsingNearby() {
        return usingNearby;
    }

    public void setUsingNearby(boolean usingNearby) {
        this.usingNearby = usingNearby;
    }

    public boolean getNearbyHostPassthrough() {
        return nearbyHostPassthrough;
    }

    public void setNearbyHostPassthrough(boolean nearbyHostPassthrough) {
        this.nearbyHostPassthrough = nearbyHostPassthrough;
    }

    public boolean getTemporaryAdvertise() {
        return temporaryAdvertise;
    }

    public void setTemporaryAdvertise(boolean temporaryAdvertise) {
        this.temporaryAdvertise = temporaryAdvertise;
    }

    public boolean getMatchToPDFSong() {
        return matchToPDFSong;
    }

    public void setMatchToPDFSong(boolean matchToPDFSong) {
        this.matchToPDFSong = matchToPDFSong;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("matchToPDFSong", matchToPDFSong);
    }

    // Set the strategy as either cluster (many to many) or star (one to many).
    public void setNearbyStrategy(Strategy nearbyStrategy) {
        this.nearbyStrategy = nearbyStrategy;
        advertisingOptions = new AdvertisingOptions.Builder().setStrategy(nearbyStrategy).build();
        discoveryOptions = new DiscoveryOptions.Builder().setStrategy(nearbyStrategy).build();
        if (nearbyStrategy.equals(Strategy.P2P_CLUSTER)) {
            updateConnectionLog(c.getString(R.string.connections_mode) + ": " + c.getString(R.string.connections_mode_cluster));
        } else if (nearbyStrategy.equals(Strategy.P2P_STAR)) {
            updateConnectionLog(c.getString(R.string.connections_mode) + ": " + c.getString(R.string.connections_mode_star));
        } else {
            updateConnectionLog(c.getString(R.string.connections_mode) + ": " + c.getString(R.string.connections_mode_single));
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


    // The timer to stop advertising/discovery
    public void initialiseCountdown() {
        // Timer for stop of discovery and advertise (only one can happen at a time)
        countdown = 10;
    }

    public void setTimer(boolean advertise, MaterialButton materialButton) {
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


    // Start or stop the advertising/discovery of devices to initiate connections
    @Override
    public void startAdvertising() {
        if (!isAdvertising) {
            Nearby.getConnectionsClient(activity)
                    .startAdvertising(getUserNickname(), serviceId, connectionLifecycleCallback(), advertisingOptions)
                    .addOnSuccessListener(
                            (Void unused) -> {
                                // We're advertising!
                                updateConnectionLog(c.getString(R.string.connections_advertise_name) + " " + getUserNickname());
                                isAdvertising = true;
                            })
                    .addOnFailureListener(
                            (Exception e) -> {
                                // We were unable to start advertising.
                                updateConnectionLog(c.getString(R.string.connections_advertising) + " " + c.getString(R.string.error));
                                Log.d(TAG, "startAdvertising() - failure: " + e);
                            });
        }
    }

    @Override
    public void startDiscovery() {
        // IV - Only if still in use
        // The connections fragment turns this off using a system timer
        if (usingNearby) {
            if (!isDiscovering) {
                Nearby.getConnectionsClient(activity)
                        .startDiscovery(serviceId, endpointDiscoveryCallback(), discoveryOptions)
                        .addOnSuccessListener(
                                (Void unused) -> {
                                    // We're discovering!
                                    updateConnectionLog(c.getResources().getString(R.string.connections_discover));
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
    public void stopAdvertising() {
        if (isAdvertising) {
            isAdvertising = false;
            try {
                Nearby.getConnectionsClient(activity).stopAdvertising();
                updateConnectionLog(c.getString(R.string.connections_service_stop));
            } catch (Exception e) {
                Log.d(TAG, "stopAdvertising() - failure: " + e);
            }
        }
    }

    @Override
    public void stopDiscovery() {
        if (isDiscovering) {
            try {
                Nearby.getConnectionsClient(activity).stopDiscovery();
                updateConnectionLog(c.getString(R.string.connections_discover_stop));
            } catch (Exception e) {
                Log.d(TAG, "stopDiscovery() - failure: " + e);
            }
        }
        isDiscovering = false;
    }


    // THIS IS USED IF WE ARE THE HOST AND A CLIENT INITIATES THE CONNECTION
    private ConnectionLifecycleCallback connectionLifecycleCallback() {
        return new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                // Client has tried to connect.  Get a string for the connection
                String endpointString = getEndpointString(endpointId, connectionInfo.getEndpointName());
                Log.d(TAG, "connection initiated.  endpointString:" + endpointString + "   endpointId=" + endpointId);

                // Check to see if this device was already discovered (permission granted)
                // If the device was previously registered, try to reconnect silently
                if (recognisedDevice(endpointString)) {
                    Log.d(TAG, "We have previously connected to " + endpointString + ".  Attempt reconnect");
                    delayAcceptConnection(endpointString);

                } else {
                    Log.d(TAG, "Device wasn't previously connected: " + endpointString + ".  Get connection permission");
                    // Allow clients to connect to the host when the Connect menu is open, or the user switches off the requirement for the Connect menu to be open
                    if (connectionsOpen || !mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyHostMenuOnly", false)) {
                        new AlertDialog.Builder(c)
                                .setTitle(c.getString(R.string.connections_accept) + " " + getEndpointSplit(endpointString)[1])
                                .setMessage(c.getString(R.string.connections_accept_code) + " " + connectionInfo.getAuthenticationDigits())
                                .setPositiveButton(
                                        c.getString(R.string.okay),
                                        (DialogInterface dialog, int which) -> delayAcceptConnection(endpointString))
                                .setNegativeButton(
                                        c.getString(R.string.cancel),
                                        (DialogInterface dialog, int which) ->
                                                // The user canceled, so we should reject the connection.
                                                Nearby.getConnectionsClient(activity).rejectConnection(endpointString))
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    } else {
                        // The user is not accepting new connections, so we should reject the connection.
                        Log.d(TAG, "reject connection to " + endpointString);
                        Nearby.getConnectionsClient(activity).rejectConnection(endpointString);
                    }
                }
            }

            @Override
            public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution connectionResolution) {
                String endpointString = getEndpointString(endpointId, getNameMatchingId(endpointId));
                switch (connectionResolution.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                    case ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT:
                        Log.d(TAG, "connections status either ok or already connected");
                        // Add connection record if it doesn't exist
                        updateConnectedEndpoints(endpointString, true);
                        updateDiscoveredEndpoints(endpointString, true);
                        updateConnectionLog(c.getString(R.string.connections_connected) + " " + getEndpointSplit(endpointString)[1]);
                        if (isHost) {
                            // try to send the current song payload
                            Log.d(TAG, "Sending payload successful:" + sendSongPayload());
                        }
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        Log.d(TAG, "Rejected");
                        // Remove connection from all records - need to try again
                        updateConnectedEndpoints(endpointString, false);
                        updateDiscoveredEndpoints(endpointString, false);
                        updateConnectionLog(c.getString(R.string.cancel));
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        Log.d(TAG, "Error status code");
                        // The connection broke before it was able to be accepted.
                        // The connection was rejected by one or both sides.
                        updateConnectionLog(c.getString(R.string.connections_failure) + " " + getUserNickname() +
                                " <-> " + getEndpointSplit(endpointString)[1]);
                        // Remove connection from all records - need to try again
                        updateDiscoveredEndpoints(endpointString, false);
                        updateConnectedEndpoints(endpointString, false);
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
                String disconnectedFrom = getNameMatchingId(endpointId);
                String endpointString = getEndpointString(endpointId, disconnectedFrom);
                Log.d(TAG, "On disconnected: " + endpointString);

                // Remove from the connectedDevices but not discoveredDevices
                updateConnectedEndpoints(endpointString, false);
                updateConnectionLog(c.getResources().getString(R.string.connections_disconnect) +
                        " " + disconnectedFrom);

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


    // THIS IS USED IF WE ARE A CLIENT AND HAVE DISCOVERED A HOST CONNECTION
    private EndpointDiscoveryCallback endpointDiscoveryCallback() {
        return new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                String endpointString = getEndpointString(endpointId, discoveredEndpointInfo.getEndpointName());
                Log.d(TAG, "EndpointDiscoveryCallback  endpointString:" + endpointString);
                if (!recognisedDevice(endpointString)) {
                    Log.d(TAG, endpointString + "is not found a recognised device, so attempt connection with permission");
                    // Only attempt a connection if we aren't already connected
                    Nearby.getConnectionsClient(activity)
                            .requestConnection(getUserNickname(), endpointId, connectionLifecycleCallback())
                            .addOnSuccessListener(
                                    (Void unused) -> {
                                        Log.d(TAG, "On success.  Trying to connect to host string : " + endpointString);
                                        // We successfully requested a connection. Now both sides
                                        // must accept before the connection is established.
                                        updateConnectionLog(c.getString(R.string.connections_searching));
                                    })
                            .addOnFailureListener(
                                    (Exception e) -> {
                                        Log.d(TAG, "On failure: " + (((ApiException) e).getStatus().getStatusMessage()));

                                        // IV - Added handling of when already connected
                                        if (((ApiException) e).getStatusCode() == ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT) {
                                            Log.d(TAG, endpointString + " was already connected");
                                            // Check we have both records
                                            updateConnectedEndpoints(endpointString, true);
                                            updateDiscoveredEndpoints(endpointString, true);
                                            updateConnectionLog(c.getString(R.string.connections_connected) + " " + discoveredEndpointInfo.getEndpointName());

                                            // IV - Already connected so replay last incoming song
                                            if (incomingPrevious != null && !incomingPrevious.isEmpty()) {
                                                String incoming = incomingPrevious;
                                                incomingPrevious = null;
                                                payloadOpenSong(incoming);
                                            }
                                            // We can stop discovery now
                                            stopDiscovery();
                                        } else {
                                            // Nearby Connections failed to request the connection.
                                            Log.d(TAG, "A general error");
                                            updateConnectionLog(c.getString(R.string.connections_failure) + " " + discoveredEndpointInfo.getEndpointName());
                                        }
                                        Log.d(TAG, "Connections failure: " + e);
                                    });
                } else {
                    Log.d(TAG, endpointString + " already a recognised device.  Try to connect automatically");
                    delayAcceptConnection(endpointString);
                }
            }

            @Override
            public void onEndpointLost(@NonNull String endpointId) {
                String endpointName = getNameMatchingId(endpointId);
                String endpointString = getEndpointString(endpointId, endpointName);
                Log.d(TAG, "onEndPointlost: " + endpointString);
                // Remove from the connected devices (but keep in recognised devices)
                updateConnectedEndpoints(endpointString, false);
                updateConnectionLog(c.getString(R.string.connections_disconnect) + " " + endpointName);
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
    private void delayAcceptConnection(String endpointString) {
        // For stability add a small delay
        Handler waitAccept = new Handler();
        waitAccept.postDelayed(() -> {
            // Add a note of the nice name on to the endpointId
            Log.d(TAG, "about to try and accept " + endpointString);
            updateDiscoveredEndpoints(endpointString, true);

            // The user confirmed, so we can accept the connection.
            Nearby.getConnectionsClient(activity)
                    .acceptConnection(getEndpointSplit(endpointString)[0], payloadCallback());
        }, 200);
    }


    // Triggered when a host has sent a payload - this is where clients listen out!
    // If the host is allowing passthrough, it doesn't listen, but passes it on
    private PayloadCallback payloadCallback() {
        Log.d(TAG, "payloadCallback()");
        return new PayloadCallback() {
            @Override
            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                // If we are a host and this is a request to send a file, deal with that separately
                boolean dealWithAsHostRequestFile = false;
                if (payload.getType() == Payload.Type.BYTES && isHost && payload.asBytes() != null) {
                    byte[] bytes = payload.asBytes();
                    if (bytes != null) {
                        String requestPayload = new String(bytes);
                        dealWithAsHostRequestFile = requestPayload.startsWith(requestFileTag);
                        if (dealWithAsHostRequestFile) {
                            hostSendFile(requestPayload);
                        }
                    }
                }


                if (!dealWithAsHostRequestFile) {
                    // Deal with this if is a normal song request
                    // To avoid send loops, only devices set as clients act onPayloadReceived
                    // However if we are set as cluster strategy, we should echo what we have received
                    // This is because clients can be connected to the host through another client
                    Log.d(TAG, "s: " + s + "  payload received:" + payload);

                    // Check if we've already received/sent this out.  Only proceed if not
                    if (previousPayload == null || !previousPayload.equals(payload)) {
                        // Keep a note of this payload
                        previousPayload = payload;

                        // If we are a client or a passthrough host, send on the information
                        if (!isHost || nearbyHostPassthrough) {
                            // Send the payload out again in case any of my connected devices need to hear it from me
                            // This will only happen in Strategy.P2P_CLUSTER or P2P_STAR
                            repeatPayload(payload);
                        }

                        // Now deal with what has come in if I am a client
                        if (!isHost) {
                            // We can deal with the incoming payload!
                            if (payload.getType() == Payload.Type.FILE) {
                                requestedFilePayload = payload;
                                // Make a note of it.  Nothing happens until complete
                                Log.d(TAG, "Payload.Type: FILE");
                                incomingFilePayloads.put(payload.getId(), payload);

                            } else if (payload.getType() == Payload.Type.BYTES) {
                                // We're dealing with bytes
                                Log.d(TAG, "Payload.Type: BYTES");
                                String incoming = null;
                                if (payload.asBytes() != null) {
                                    byte[] bytes = payload.asBytes();
                                    if (bytes != null) {
                                        incoming = new String(bytes);
                                    }
                                }
                                if (incoming != null && incoming.startsWith(currentset)) {
                                    dealWithHostCurrentSet(incoming);

                                } else if (incoming != null && incoming.startsWith("FILE:")) {
                                    // Add the file location to the arraymap
                                    incoming = incoming.replaceFirst("FILE:", "");
                                    String id = incoming.substring(0, incoming.indexOf(":"));
                                    id = id.replace(":", "");
                                    String foldernamepair = incoming.substring(incoming.indexOf(":"));
                                    foldernamepair = foldernamepair.replace(":", "");
                                    fileNewLocation.put(Long.parseLong(id), foldernamepair);

                                } else if (incoming != null && incoming.contains("autoscroll_")) {
                                    // IV - Autoscroll only in Performance mode when user option is selected
                                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) && receiveHostAutoscroll) {
                                        payloadAutoscroll(incoming);
                                    }
                                } else if (incoming != null && incoming.equals(autoscrollPause)) {
                                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                                            receiveHostAutoscroll) {
                                        mainActivityInterface.getAutoscroll().pauseAutoscroll();
                                    }

                                } else if (incoming != null && incoming.equals(autoscrollincrease)) {
                                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                                            receiveHostAutoscroll) {
                                        mainActivityInterface.getAutoscroll().speedUpAutoscroll();
                                    }

                                } else if (incoming != null && incoming.equals(autoscrolldecrease)) {
                                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                                            receiveHostAutoscroll) {
                                        mainActivityInterface.getAutoscroll().slowDownAutoscroll();
                                    }

                                } else if (incoming != null && incoming.contains(sectionTag)) {
                                    // IV - Section change only in Stage and Presentation mode (Song or PDF) when user option is selected
                                    if ((!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) ||
                                            mainActivityInterface.getSong().getFiletype().equals("PDF")) &&
                                            receiveHostSongSections) {
                                        Log.d(TAG, "call payloadSection " + incoming);
                                        payloadSection(incoming);
                                    }
                                } else if (incoming != null && incoming.contains(songTag)) {
                                    payloadOpenSong(incoming);
                                } else if (incoming != null && incoming.contains(scrollByTag)) {
                                    // We have received a scroll by amount command.  Check we want this
                                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                                            receiveHostScroll) {
                                        Log.d(TAG, "call payloadScrollBy");
                                        payloadScrollBy(incoming);
                                    }
                                } else if (incoming != null && incoming.contains(scrollToTag)) {
                                    // We have received a scroll to instruction.  Check we want this
                                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                                            receiveHostScroll) {
                                        Log.d(TAG, "call payloadScrollTo");
                                        payloadScrollTo(incoming);
                                    }
                                } else if (incoming != null && incoming.startsWith(messageTag)) {
                                    // We have received an alert message
                                    Log.d(TAG, "call payloadMessage");
                                    payloadMessage(incoming);
                                } else if (incoming != null && incoming.startsWith(hostItems + deviceId)) {
                                    // This is a list of host items returned for this device
                                    Log.d(TAG, "call payloadHostItems");
                                    payloadHostItems(incoming);
                                }
                            }
                            // not dealing with files as it is complex with scoped storage access
                            // also don't want user's download folder getting clogged!
                        } else {
                            // If we are the host, we could have been asked to synchronise storage
                            // TODO
                            Log.d(TAG, "Running as a host");
                            String incoming = null;
                            if (payload.getType() == Payload.Type.BYTES) {
                                // We're dealing with bytes
                                Log.d(TAG, "Payload.Type: BYTES");
                                if (payload.asBytes() != null) {
                                    byte[] bytes = payload.asBytes();
                                    if (bytes != null) {
                                        incoming = new String(bytes);
                                    }
                                }
                            }
                            if (incoming != null && incoming.startsWith(hostRequest)) {
                                incoming = incoming.replaceFirst(hostRequest, "");
                                String message = null;
                                if (incoming.startsWith(sets)) {
                                    // Get the requesting deviceID
                                    incoming = incoming.replaceFirst(sets, "");
                                    // Get this device's items
                                    message = hostItems + incoming + "\n" + getHostItems("browsesets");
                                } else if (incoming.startsWith(currentset)) {
                                    // Get the requesting deviceID
                                    incoming = incoming.replaceFirst(currentset, "");
                                    // Get this device's items
                                    message = currentset + incoming + "\n" + mainActivityInterface.getCurrentSet().getSetCurrent();
                                } else if (incoming.startsWith(profiles)) {
                                    // Get the requesting deviceID
                                    incoming = incoming.replaceFirst(profiles, "");
                                    // Get this device's items
                                    message = hostItems + incoming + "\n" + getHostItems("browseprofiles");
                                } else if (incoming.startsWith(songs)) {
                                    // Get the requesting deviceID
                                    incoming = incoming.replaceFirst(songs, "");
                                    // Get this device's items
                                    message = hostItems + incoming + "\n" + getHostItems("browsesongs");
                                }
                                if (message != null) {
                                    // Send the message to the listening clients
                                    // Only the one with this deviceID will act on it though
                                    doSendPayloadBytes(message, false);
                                }

                            }
                        }
                    }
                }
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                // If we are requesting host files, we deal with this separately
                if (browseHostFragment != null && browseHostFragment.getWaitingForFiles() && !isHost &&
                        requestedFilePayload != null) {
                    dealWithRequestedFile();
                } else {
                    // IV - If we are a client and not 'receiving host files' then cancel these uneeded FILE transfers
                    if (!isHost && !receiveHostFiles) {
                        if (incomingFilePayloads.containsKey(payloadTransferUpdate.getPayloadId())) {
                            Log.d(TAG, "Cancelled Id " + payloadTransferUpdate.getPayloadId());
                            Nearby.getConnectionsClient(activity).cancelPayload(payloadTransferUpdate.getPayloadId());
                        }
                    } else {
                        if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                            // For bytes this is sent automatically, but it's the file we are interested in here
                            Payload payload;
                            Log.d(TAG, "update payloadId()=" + payloadTransferUpdate.getPayloadId());
                            if (incomingFilePayloads.containsKey(payloadTransferUpdate.getPayloadId())) {
                                payload = incomingFilePayloads.get(payloadTransferUpdate.getPayloadId());
                                String foldernamepair = fileNewLocation.get(payloadTransferUpdate.getPayloadId());
                                if (foldernamepair == null) {
                                    foldernamepair = "../Received" + songTag + "ReceivedSong";
                                }
                                incomingFilePayloads.remove(payloadTransferUpdate.getPayloadId());
                                fileNewLocation.remove(payloadTransferUpdate.getPayloadId());

                                payloadFile(payload, foldernamepair);
                            }

                            // IV - Keep a record of Ids
                            if (payloadTransferIds == null) {
                                payloadTransferIds = "";
                            }
                            if (!(payloadTransferIds.contains(payloadTransferUpdate.getPayloadId() + " "))) {
                                payloadTransferIds = payloadTransferIds + payloadTransferUpdate.getPayloadId() + " ";
                                Log.d("NearbyConnections", "Id History " + payloadTransferIds);
                            }
                        }
                    }
                }
            }
        };
    }


    // Deal with endpoints -  the identifiers for connected devices
    // The endpointId is a random bit of code that identifies a device
    // The connectionInfo.getEndpointName() is a user readable name of a device
    // Once a connection is made we store both as a string like id__name
    // These strings are stored in the connectedEndpoints arraylist
    // Any device we discover is stored in discoveredEndpoints arraylist so we can get a name
    private String getEndpointString(String endpointId, String connectedDeviceName) {
        return endpointId + endpointSplit + connectedDeviceName;
    }

    private String[] getEndpointSplit(String endpointString) {
        if (!endpointString.contains(endpointSplit)) {
            endpointString = endpointString + endpointSplit + c.getString(R.string.unknown);
        }
        String[] returnVal = new String[2];
        String[] split = endpointString.split(endpointSplit);

        if (split.length > 0 && split[0] != null) {
            returnVal[0] = split[0];
        } else {
            returnVal[0] = "0000";
        }
        if (split.length > 1 && split[1] != null) {
            returnVal[1] = split[1];
        } else {
            returnVal[1] = "Unknown";
        }
        return returnVal;
    }

    private void updateConnectedEndpoints(String endpointString, boolean addEndpoint) {
        if (addEndpoint) {
            // Add to the connected list if not already there
            if (!connectedEndpoints.contains(endpointString)) {
                Log.d(TAG, "ADD: " + endpointString + " was not in connectedEndpoints - adding");
                connectedEndpoints.add(endpointString);
            } else {
                Log.d(TAG, "ADD: " + endpointString + " was already in connectedEndpoints - skip");
            }
        } else {
            if (connectedEndpoints.contains(endpointString)) {
                Log.d(TAG, "REMOVE: " + endpointString + " was already in connectedEndpoints - remove");
                connectedEndpoints.remove(endpointString);
            } else {
                Log.d(TAG, "REMOVE: " + endpointString + " was not in connectedEndpoints - ignore");
            }
        }
    }

    private void updateDiscoveredEndpoints(String endpointString, boolean addEndpoint) {
        if (addEndpoint) {
            // Add to the discovered lists (recognised devices) if not already there
            if (!discoveredEndpoints.contains(endpointString)) {
                Log.d(TAG, "ADD: " + endpointString + " was not in discoveredEndpoints - adding");
                discoveredEndpoints.add(endpointString);
            } else {
                Log.d(TAG, "ADD: " + endpointString + " was already in discoveredEndpoints - skip");
            }
        } else {
            if (discoveredEndpoints.contains(endpointString)) {
                Log.d(TAG, "REMOVE: " + endpointString + " was in discoveredEndpoints - remove");
                discoveredEndpoints.remove(endpointString);
            } else {
                Log.d(TAG, "REMOVE: " + endpointString + " was not in discoveredEndpoints - skip");
            }
        }
    }

    private boolean recognisedDevice(String endpointString) {
        return discoveredEndpoints.contains(endpointString);
    }

    public boolean hasValidConnections() {
        if (usingNearby) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                if (!connectedEndpoints.isEmpty()) {
                    for (String s : connectedEndpoints) {
                        if (s != null && !s.isEmpty()) {
                            stringBuilder.append(s);
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
        String nicename = endpointId;
        // Use discovered endpoints as we may have not fully established the connection yet
        for (String ep : discoveredEndpoints) {
            if (ep.startsWith(endpointId)) {
                nicename = getEndpointSplit(ep)[1];
            }
        }
        return nicename;
    }

    public String getConnectedDevicesAsString() {
        if (connectedEndpoints == null || connectedEndpoints.isEmpty()) {
            return c.getString(R.string.connections_no_devices);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (String endpointString : connectedEndpoints) {
                stringBuilder.append(getEndpointSplit(endpointString)[1])
                        .append("\n");
            }
            return stringBuilder.toString().trim();
        }
    }


    // Deal with sending payloads as a host for clients to listen for
    @Override
    public void doSendPayloadBytes(String infoPayload, boolean clientSend) {
        Log.d(TAG, "infoPayload:" + infoPayload);

        if (sendAsHost() || clientSend) {
            for (String endpointString : connectedEndpoints) {
                String endpointId = getEndpointSplit(endpointString)[0];
                Nearby.getConnectionsClient(activity).sendPayload(endpointId, Payload.fromBytes(infoPayload.getBytes()));
            }
        }
    }


    private void repeatPayload(Payload payload) {
        if (nearbyStrategy == Strategy.P2P_CLUSTER) {
            for (String connectedEndpoint : connectedEndpoints) {
                Nearby.getConnectionsClient(activity).sendPayload(connectedEndpoint, payload);
            }
        }
    }

    public boolean sendSongPayload() {
        // IV - HOST: Cancel previous song transfers - a new song is being sent
        cancelTransferIds();

        String infoPayload;
        String infoFilePayload = null;
        Payload payloadFile;
        boolean largePayLoad = false;

        // IV - Process each end point - we need a unique ParcelFileDescriptor if a file is sent
        for (String endpointString : connectedEndpoints) {
            String endpointId = getEndpointSplit(endpointString)[0];

            // IV - Send the current section as a pending section change (encode as -ve offset by 1) for action on next song load on the client
            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                infoPayload = sectionTag + "-" + (1 + mainActivityInterface.getSong().getPdfPageCurrent());
            } else {
                infoPayload = sectionTag + "-" + (1 + mainActivityInterface.getSong().getCurrentSection());
            }
            Nearby.getConnectionsClient(activity).sendPayload(endpointId, Payload.fromBytes(infoPayload.getBytes()));
            Log.d(TAG, "sendSongPayload =" + infoPayload);

            infoPayload = null;

            // New method sends OpenSong songs in the format of
            // FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>
            // songTag = "_xx____xx_";

            if (mainActivityInterface.getSong().getFiletype().equals("XML") &&
                    mainActivityInterface.getSong().getFilename() != null &&
                    !mainActivityInterface.getSong().getFilename().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                // By default, this should be smaller than 32kb, so probably going to send as bytes
                // We'll measure the actual size to check though
                infoPayload = mainActivityInterface.getSong().getFolder() + songTag +
                        mainActivityInterface.getSong().getFilename() + songTag +
                        mainActivityInterface.getDisplayPrevNext().getSwipeDirection() + songTag +
                        mainActivityInterface.getProcessSong().getXML(mainActivityInterface.getSong());

                // Check the size.  If it is bigger than the 32kb (go 30kb to play safe!) allowed for bytes, switch to file
                byte[] mybytes = infoPayload.getBytes();
                if (mybytes.length > 30000) {
                    infoPayload = null;
                } else {
                    // Just send the bytes
                    Nearby.getConnectionsClient(activity).sendPayload(endpointId, Payload.fromBytes(infoPayload.getBytes()));
                }
            }

            if (infoPayload == null) {
                payloadFile = null;
                // We will send as a file
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(
                        "Songs", mainActivityInterface.getSong().getFolder(),
                        mainActivityInterface.getSong().getFilename());

                try {
                    ParcelFileDescriptor parcelFileDescriptor = c.getContentResolver().openFileDescriptor(uri, "r");
                    if (parcelFileDescriptor != null) {
                        payloadFile = Payload.fromFile(parcelFileDescriptor);
                        infoFilePayload = "FILE:" + payloadFile.getId() + ":" +
                                mainActivityInterface.getSong().getFolder() + songTag +
                                mainActivityInterface.getSong().getFilename() + songTag +
                                mainActivityInterface.getDisplayPrevNext().getSwipeDirection();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error trying to send file: " + e);
                    payloadFile = null;
                }
                Log.d(TAG, "payloadFile=" + payloadFile);
                if (payloadFile != null) {
                    // Send the file descriptor as bytes, then the file
                    Nearby.getConnectionsClient(activity).sendPayload(endpointId, Payload.fromBytes(infoFilePayload.getBytes()));
                    Nearby.getConnectionsClient(activity).sendPayload(endpointId, payloadFile);
                    // IV - Check the size.  If it is large then indicate to inform user
                    if (Objects.requireNonNull(payloadFile.asFile()).getSize() > 30000) {
                        largePayLoad = true;
                    }
                }
            }
        }
        return largePayLoad;
    }

    public void sendSongSectionPayload() {
        if (sendAsHost()) {
            // IV - Send if we are not delaying - a delayed song send sends the current section
            if (!sendSongDelayActive) {
                String infoPayload;
                if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                    infoPayload = sectionTag + (mainActivityInterface.getSong().getPdfPageCurrent());
                } else {
                    infoPayload = sectionTag + (mainActivityInterface.getSong().getCurrentSection());
                }
                doSendPayloadBytes(infoPayload, false);
                Log.d(TAG, "sendSongSectionPayLoad " + infoPayload);
            }
        }
    }

    public void sendAutoscrollPayload(String message) {
        doSendPayloadBytes(message, false);
    }

    public void sendScrollByPayload(boolean scrollDown, float scrollProportion) {
        if (sendAsHost()) {
            String infoPayload = scrollByTag;
            if (scrollDown) {
                infoPayload += scrollProportion;
            } else {
                infoPayload += -scrollProportion;
            }
            doSendPayloadBytes(infoPayload, false);
        }
    }

    public void sendScrollToPayload(float scrollProportion) {
        if (sendAsHost()) {
            String infoPayload = scrollToTag + scrollProportion;
            doSendPayloadBytes(infoPayload, false);
        }
    }

    public void sendAutoscrollPausePayload() {
        if (sendAsHost()) {
            doSendPayloadBytes(autoscrollPause, false);
        }
    }

    public void increaseAutoscrollPayload() {
        if (sendAsHost()) {
            doSendPayloadBytes(autoscrollincrease, false);
        }
    }

    public void decreaseAutoscrollPayload() {
        if (sendAsHost()) {
            doSendPayloadBytes(autoscrolldecrease, false);
        }
    }

    // Deal with actions received as a client device
    public void doSectionChange(int mysection) {
        boolean onSectionAlready;
        int totalSections;
        if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
            onSectionAlready = mainActivityInterface.getSong().getPdfPageCurrent() == mysection;
            totalSections = mainActivityInterface.getSong().getPdfPageCount();
        } else {
            onSectionAlready = mainActivityInterface.getSong().getCurrentSection() == mysection;
            totalSections = mainActivityInterface.getSong().getPresoOrderSongSections().size();
        }
        Log.d(TAG, "doSectionChange().  onSectionAlready=" + onSectionAlready);
        Log.d(TAG, "totalSections=" + totalSections + "  mysection=" + mysection);
        if (!onSectionAlready && nearbyReturnActionsInterface != null && totalSections > mysection) {
            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                mainActivityInterface.getSong().setPdfPageCurrent(mysection);
            } else {
                mainActivityInterface.getSong().setCurrentSection(mysection);
            }
            nearbyReturnActionsInterface.selectSection(mysection);
        }
    }

    private void payloadOpenSong(String incoming) {
        // IV - CLIENT: Cancel previous song transfers - a new song has arrived
        cancelTransferIds();

        // New method sends OpenSong songs in the format of
        //  FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>
        // For v6.1.6 the key is also included:
        //  FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>_xx____xx_KEY

        ArrayList<String> receivedBits = getNearbyIncoming(incoming);
        //Log.d(TAG,"incoming: "+incoming+"\nprevious: "+incomingPrevious);
        boolean incomingChange = (!incoming.equals(incomingPrevious));

        Log.d(TAG, "incomingChange=" + incomingChange);

        if (incomingChange) {
            incomingPrevious = incoming;
            OutputStream outputStream;

            // If 'Receiving host songs' then only BYTES songs with xml arrive here - we use the  4th <?xml> bit
            // If not 'Receiving host songs' then all songs arrive here including FILES: which have a dummy 4th <?xml> bit - we do not use xml
            boolean songReceived = (receivedBits.size() >= 4);

            Log.d(TAG, "songReceived=" + songReceived);
            Log.d(TAG, "receivedBits.size()=" + receivedBits.size());

            Log.d(TAG, "isHost=" + isHost + "  hasValidConnections()=" + hasValidConnections() + "  receiveHostFiles=" + receiveHostFiles + "  keepHostFiles=" + keepHostFiles);
            if (songReceived) {
                // Remove the current set position otherwise the client can be confused
                Log.d(TAG,"removing song index");
                mainActivityInterface.getCurrentSet().setIndexSongInSet(-1);
                Log.d(TAG,"getting updated set index");
                mainActivityInterface.getSetActions().indexSongInSet(receivedBits.get(0), receivedBits.get(1),"");
                Log.d(TAG,"index now:"+mainActivityInterface.getCurrentSet().getIndexSongInSet());
                Log.d(TAG,"received, now update highlights");
                mainActivityInterface.getSetMenuFragment().setHighlightChangeAllowed(true);
                mainActivityInterface.getSetMenuFragment().updateHighlight();
                mainActivityInterface.getSetMenuFragment().removeHighlight();
                mainActivityInterface.getSetMenuFragment().setHighlightChangeAllowed(false);

                // Set the reload flag
                forceReload = true;

                if (!isHost && hasValidConnections() && receiveHostFiles) {
                    // We want to receive host files (we aren't the host either!) and an OpenSong song has been sent/received
                    mainActivityInterface.getDisplayPrevNext().setSwipeDirection(receivedBits.get(2));

                    // Set the reload flag
                    forceReload = true;

                    // If the user wants to keep the host file, we will save it to our storage.
                    // If we already have it, it will overwrite it, if not, we add it
                    Uri newLocation;
                    if (keepHostFiles) {
                        // Prepare the output stream in the client Songs folder
                        // Check the folder exists, if not, create it
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadOpenSong createFile Songs/" + receivedBits.get(0));
                        mainActivityInterface.getStorageAccess().createFile(DocumentsContract.Document.MIME_TYPE_DIR, "Songs", receivedBits.get(0), "");
                        newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Songs", receivedBits.get(0), receivedBits.get(1));
                        // Create the file if it doesn't exist
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payLoadOpenSong() keepHostFiles Create Songs/" + receivedBits.get(0) + "/" + receivedBits.get(1) + "  deleteOld=true");
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newLocation, null, "Songs", receivedBits.get(0), receivedBits.get(1));
                        outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newLocation);
                        mainActivityInterface.getSong().setFolder(receivedBits.get(0));
                        mainActivityInterface.getSong().setFilename(receivedBits.get(1));
                        // Add to the sql database
                        mainActivityInterface.getSQLiteHelper().createSong(mainActivityInterface.getSong().getFolder(), mainActivityInterface.getSong().getFilename());
                        Log.d(TAG, "keepFile: " + newLocation);
                    } else {
                        newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", "ReceivedSong");
                        // Prepare the output stream in the Received folder - just keep a temporary version
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payLoadOpenSong !keepHostFiles Create Songs/" + receivedBits.get(0) + "/" + receivedBits.get(1) + " deleteOld=true");
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newLocation, null, "Received", "", "ReceivedSong");
                        outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newLocation);
                        mainActivityInterface.getSong().setFolder("../Received");
                        mainActivityInterface.getSong().setFilename("ReceivedSong");
                        // IV - Store the received song filename in case the user wants to duplicate the received song
                        receivedSongFilename = receivedBits.get(1);

                        Log.d(TAG, "receiveFile: " + newLocation);
                    }

                    Log.d(TAG, "outputstream: " + outputStream);

                    // Write the file to the desired output stream and load
                    if (nearbyReturnActionsInterface != null) {
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadOpenSong writeFileFromString " + newLocation + " with: " + receivedBits.get(3));
                        Log.d(TAG, "write the file: " + mainActivityInterface.getStorageAccess().writeFileFromString(receivedBits.get(3), outputStream));

                        // If we are keeping the song, update the database song first
                        if (receiveHostFiles && keepHostFiles) {
                            mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSongFile(mainActivityInterface.getSong(), false));
                            mainActivityInterface.getSQLiteHelper().updateSong(mainActivityInterface.getSong());
                            mainActivityInterface.updateSongMenu(mainActivityInterface.getSong());
                        }
                        mainActivityInterface.getSong().setCurrentSection(getHostPendingSection());
                        nearbyReturnActionsInterface.loadSong(true);
                    }
                } else if (!isHost && hasValidConnections()) {
                    // We just want to trigger loading the song on our device (if we have it).
                    // If not, we get notified it doesn't exits
                    mainActivityInterface.getSong().setFolder(receivedBits.get(0));
                    mainActivityInterface.getSong().setFilename(receivedBits.get(1));
                    mainActivityInterface.getDisplayPrevNext().setSwipeDirection(receivedBits.get(2));

                    // If we want to use PDF versions of songs instead, change the filename
                    if (matchToPDFSong && !receivedBits.get(1).endsWith(".pdf") && !receivedBits.get(1).endsWith(".PDF")) {
                        String newPDFFilename = receivedBits.get(1) + ".pdf";
                        Uri newPDFUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", receivedBits.get(0), newPDFFilename);
                        if (mainActivityInterface.getStorageAccess().uriExists(newPDFUri)) {
                            mainActivityInterface.getSong().setFilename(receivedBits.get(1) + ".pdf");
                        }
                    }

                    // Now load the song if we are displaying the performance/stage/presenter fragment
                    if (nearbyReturnActionsInterface != null) {
                        mainActivityInterface.getSong().setCurrentSection(getHostPendingSection());
                        nearbyReturnActionsInterface.loadSong(true);
                    }
                }
            }

        } else {
            Log.d(TAG, "payloadOpenSong - no change as unchanged payload");
        }
    }

    private ArrayList<String> getNearbyIncoming(String incoming) {
        // New method sends OpenSong songs in the format of
        // FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>
        // V6.1.6 also has the key
        // FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>_xx____xx_KEY
        // songTag = "_xx____xx_";
        // Four distict parts
        ArrayList<String> arrayList = new ArrayList<>();
        String[] bits = incoming.split(songTag);

        for (int i = 0; i < 5; i++) {
            if (bits.length > i) {
                arrayList.add(i, bits[i]);
                if (bits[i].contains("<title>")) {
                    Log.d(TAG, "Incoming bits[" + i + "] Song title=" + (bits[i] + "<title>Invalid</title>").substring(bits[i].indexOf("<title>") + 7, bits[i].indexOf("</title>")));
                } else {
                    Log.d(TAG, "Incoming bits[" + i + "]=" + bits[i]);
                }
            } else {
                // Old format or something not right.  Avoid null values returned
                arrayList.add(i, "");
            }
        }

        // bits[0] = foldername
        // bits[1] = filename
        // bits[2] = swipe direction
        // bits[3] = song XML
        // bits[4] = key to use (v6.1.6+)
        return arrayList;
    }

    private void payloadFile(Payload payload, String foldernamepair) {
        try {
            Log.d(TAG, "payload=" + payload + " foldernamepair=" + foldernamepair);
            // IV - CLIENT: Cancel previous song transfers - a new song has arrived
            cancelTransferIds();
            Log.d(TAG, "file payload.getId(): " + payload.getId() + "  foldernamepair:" + foldernamepair);
            // If songs are too big, then we receive them as a file rather than bytes
            String[] bits = foldernamepair.split(songTag);
            if (bits.length < 3) {
                bits = new String[3];
                bits[0] = "";
                bits[1] = "";
                bits[2] = "R2L";
            }
            String folder = bits[0];
            String filename = bits[1];
            Log.d(TAG, "folder:" + bits[0] + "  filename:" + bits[1]);
            mainActivityInterface.getDisplayPrevNext().setSwipeDirection(bits[2]);
            Uri newLocation = null;
            if (!isHost && hasValidConnections() && receiveHostFiles && keepHostFiles && filename != null && !filename.isEmpty()) {
                // The new file goes into our main Songs folder if we don't already have it
                newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Songs", folder, filename);
                if (!mainActivityInterface.getStorageAccess().uriExists(newLocation)) {
                    Log.d(TAG, "Client doesn't have the song, so create it");
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadFile Create Songs/" + folder + "/" + filename + " deleteOld=false");
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, newLocation, null, "Songs", folder, filename);
                } else {
                    // Check it isn't a zero filesize/corrupt
                    if (mainActivityInterface.getStorageAccess().getFileSizeFromUri(newLocation) == 0) {
                        Log.d(TAG, "0b file - bring in this one");
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadFile 0kb file Create Songs/" + folder + "/" + filename + " deleteOld=true");
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newLocation, null, "Songs", folder, filename);
                    } else {
                        // Set to null as we don't need to create it as we already have this song
                        Log.d(TAG, "Client already has the song.  Do nothing");
                        newLocation = null;
                    }
                }
            } else if (!isHost && hasValidConnections() && receiveHostFiles && filename != null && !filename.isEmpty()) {
                // The new file goes into our Received folder
                folder = "../Received";
                // IV - Store the received song filename in case the user wants to duplicate the received song
                receivedSongFilename = filename;
                newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", filename);
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadFile Create Received/" + folder + "/" + filename + " deleteOld=true");
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newLocation, null, "Received", "", filename);
            }
            mainActivityInterface.getSong().setFolder(folder);
            mainActivityInterface.getSong().setFilename(filename);
            if (newLocation != null && payload.asFile() != null) { // i.e. we have received the file by choice
                ParcelFileDescriptor parcelFileDescriptor = (Objects.requireNonNull(payload.asFile())).asParcelFileDescriptor();
                InputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                Uri originalUri = Uri.parse(parcelFileDescriptor.getFileDescriptor().toString());
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newLocation);
                Log.d(TAG, "receiving File.  Original uri=" + originalUri);
                Log.d(TAG, "new location = " + newLocation);
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadFile copyFile from " + originalUri + " to " + newLocation);
                if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                    if (nearbyReturnActionsInterface != null) {
                        // Make sure song is in the database (but not for received folder!
                        if (!folder.startsWith("../") && !folder.startsWith("**") &&
                                !mainActivityInterface.getSQLiteHelper().songExists(folder, filename)) {
                            Log.d(TAG, "About to add to the database - folder: " + folder + "  filename:" + filename);
                            mainActivityInterface.getSQLiteHelper().createSong(folder, filename);
                            // Set the filetype
                            mainActivityInterface.getStorageAccess().isIMGorPDF(mainActivityInterface.getSong());
                            mainActivityInterface.getSong().setTitle(filename);
                            mainActivityInterface.getSQLiteHelper().updateSong(mainActivityInterface.getSong());
                            // Refresh the song menu
                            mainActivityInterface.updateSongList();
                        }
                        mainActivityInterface.getSong().setCurrentSection(getHostPendingSection());
                        nearbyReturnActionsInterface.loadSong(true);
                    }
                }
                parcelFileDescriptor.close();

                try {
                    if (mainActivityInterface.getStorageAccess().uriExists(originalUri)) {
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadFile deleteFile " + originalUri);
                        mainActivityInterface.getStorageAccess().deleteFile(originalUri);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error trying to delete originalUri");
                }
            } else {
                if (nearbyReturnActionsInterface != null && filename != null && !filename.isEmpty()) {
                    mainActivityInterface.getSong().setCurrentSection(getHostPendingSection());
                    nearbyReturnActionsInterface.loadSong(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getReceivedSongFilename() {
        return receivedSongFilename;
    }

    private int getNearbySection(String incoming) {
        if (incoming != null && incoming.startsWith(sectionTag)) {
            incoming = incoming.replace(sectionTag, "");
            try {
                return Integer.parseInt(incoming);
            } catch (Exception e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private void payloadSection(String incoming) {
        if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) ||
                mainActivityInterface.getSong().getFiletype().equals("PDF")) {
            int mysection = getNearbySection(incoming);
            if (mysection >= 0) {
                if (pendingSection < 0) {
                    // We are pending, continue pending
                    pendingSection = -(mysection + 1);
                } else {
                    // IV - Do the section change assuming we have this many sections
                    doSectionChange(mysection);
                }
            } else {
                // IV - A Host has passed a section directly into 'pending' state to used to set section in the next song load (which it will send)
                pendingSection = mysection;
            }
        }
    }

    public int getHostPendingSection() {
        // IV -  Decode and return the required section number
        // A pendingSection value of 0 returns -1 and means no pending.
        // A negative pendingSection value is unencoded to give the section requested by the host
        return -(1 + pendingSection);
    }

    public void resetHostPendingSection() {
        // IV - Reset to indicate no host pending section to process
        this.pendingSection = 0;
    }

    public void setPendingSection(int sectionNumber) {
        // IV - Encode and store a pending section number as -ve offset by 1
        this.pendingSection = -(sectionNumber + 1);
    }

    public boolean getSendSongDelayActive() {
        return this.sendSongDelayActive;
    }

    public void setSendSongDelayActive(boolean value) {
        this.sendSongDelayActive = value;
    }

    private void payloadAutoscroll(String incoming) {
        // It sends autoscroll startstops as autoscroll_start or autoscroll_stop
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
            // Adjust only when not already in the correct state
            if (nearbyReturnActionsInterface != null &&
                    !(mainActivityInterface.getAutoscroll().getIsAutoscrolling() == incoming.equals("autoscroll_start"))) {
                nearbyReturnActionsInterface.toggleAutoscroll();
            }
        }
    }

    public void setReceiveHostAutoscroll(boolean receiveHostAutoscroll) {
        this.receiveHostAutoscroll = receiveHostAutoscroll;
    }

    public boolean getReceiveHostAutoscroll() {
        return receiveHostAutoscroll;
    }

    private void payloadScrollBy(String incoming) {
        // It sends the scrollProportion as a ratio of scrollAmount/songHeight
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                nearbyReturnActionsInterface != null) {
            incoming = incoming.replace(scrollByTag, "");
            try {
                float proportion = Float.parseFloat(incoming);
                nearbyReturnActionsInterface.doScrollByProportion(proportion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void payloadScrollTo(String incoming) {
        // It sends the scrollProportion as a ratio of scrollAmount/songHeight
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                nearbyReturnActionsInterface != null) {
            incoming = incoming.replace(scrollToTag, "");
            try {
                float proportion = Float.parseFloat(incoming);
                nearbyReturnActionsInterface.doScrollToProportion(proportion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setReceiveHostScroll(boolean receiveHostScroll) {
        this.receiveHostScroll = receiveHostScroll;
    }

    public boolean getReceiveHostScroll() {
        return receiveHostScroll;
    }

    public void payloadMessage(String incoming) {
        incoming = incoming.replace(messageTag, "");

        // Show an alert to the client
        if (!mainActivityInterface.getMode().equals("Presenter") && nearbyMessageSticky) {
            // Show a sticky note alert
            mainActivityInterface.showNearbyAlertPopUp(incoming);
        } else {
            // Show a toast message
            mainActivityInterface.getShowToast().doIt(incoming);
        }
    }

    // Deal with turning off Nearby and cleaning up transferIds
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
        incomingPrevious = "";
        connectedEndpoints.clear();
    }

    public void cancelTransferIds() {
        // IV - Used to cancel earlier transfer Ids
        if (payloadTransferIds != null && !payloadTransferIds.isEmpty()) {
            String[] ids = payloadTransferIds.trim().split(" ");
            payloadTransferIds = "";
            for (String Id : ids) {
                Nearby.getConnectionsClient(activity).cancelPayload(Long.parseLong((Id.trim())));
            }
            incomingFilePayloads = new SimpleArrayMap<>();
            fileNewLocation = new SimpleArrayMap<>();
        }
    }


    // Updates the connnection log with this message.  This also updates the connected devices note
    private void updateConnectionLog(String newMessage) {
        if (newMessage != null && mainActivityInterface != null) {
            connectionLog += newMessage + "\n";
            try {
                mainActivityInterface.updateConnectionsLog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getConnectionLog() {
        return connectionLog;
    }

    public void setConnectionLog(String connectionLog) {
        this.connectionLog = connectionLog;
    }

    private boolean sendAsHost() {
        return hasValidConnections() && isHost;
    }

    public ArrayList<String> getDiscoveredEndpoints() {
        return discoveredEndpoints;
    }

    public void setDiscoveredEndpoints(ArrayList<String> discoveredEndpointsBundled) {
        // Called on resume from saved bundle.  Reset the temp counts
        countAdvertise = 0;
        countDiscovery = 0;
        if (discoveredEndpointsBundled != null && !discoveredEndpointsBundled.isEmpty()) {
            if (discoveredEndpoints == null) {
                discoveredEndpoints = new ArrayList<>();
            }
            for (String de : discoveredEndpointsBundled) {
                if (!discoveredEndpoints.contains(de)) {
                    discoveredEndpoints.add(de);
                }
            }
        }
    }

    public ArrayList<String> getConnectedEndpoints() {
        return connectedEndpoints;
    }

    public void setConnectedEndpoints(ArrayList<String> connectedEndpointsBundled) {
        if (connectedEndpointsBundled != null && !connectedEndpointsBundled.isEmpty()) {
            if (connectedEndpoints == null) {
                connectedEndpoints = new ArrayList<>();
            }
            for (String ce : connectedEndpointsBundled) {
                if (!connectedEndpoints.contains(ce)) {
                    connectedEndpoints.add(ce);
                }
            }
        }
    }

    public void doTempAdvertise() {
        // Stop advertising/discovering if we were already doing that
        stopAdvertising();
        stopDiscovery();

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

    public void doTempDiscover() {
        // Stop advertising/discovering if we were already doing that
        stopAdvertising();
        stopDiscovery();

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

    public String getNearbyMessage(int which) {
        switch (which) {
            case 1:
                return nearbyMessage1;
            case 2:
                return nearbyMessage2;
            case 3:
                return nearbyMessage3;
            case 4:
                return nearbyMessage4;
            case 5:
                return nearbyMessage5;
            case 6:
                return nearbyMessage6;
            case 7:
                return nearbyMessage7;
            case 8:
                return nearbyMessage8;
        }
        return "";
    }

    public void setNearbyMessage(int which, String nearbyMessage) {
        switch (which) {
            case 1:
                nearbyMessage1 = nearbyMessage;
                break;
            case 2:
                nearbyMessage2 = nearbyMessage;
                break;
            case 3:
                nearbyMessage3 = nearbyMessage;
                break;
            case 4:
                nearbyMessage4 = nearbyMessage;
                break;
            case 5:
                nearbyMessage5 = nearbyMessage;
                break;
            case 6:
                nearbyMessage6 = nearbyMessage;
                break;
            case 7:
                nearbyMessage7 = nearbyMessage;
                break;
            case 8:
                nearbyMessage8 = nearbyMessage;
                break;
        }
        if (which > 0 && which < 9) {
            mainActivityInterface.getPreferences().setMyPreferenceString("nearbyMessage" + which, nearbyMessage);
        }
    }

    public void sendMessage(int which) {
        String message = getNearbyMessage(which);

        // Show the message on this screen
        if (nearbyMessageSticky) {
            mainActivityInterface.showNearbyAlertPopUp(message);
        } else {
            mainActivityInterface.getShowToast().doIt(message);
        }

        // Send as a payload
        String payload = messageTag + message;
        doSendPayloadBytes(payload, false);
    }

    public void setNearbyMessageMIDIAction(boolean nearbyMessageMIDIAction) {
        this.nearbyMessageMIDIAction = nearbyMessageMIDIAction;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyMessageMIDIAction", nearbyMessageMIDIAction);
    }

    public boolean getNearbyMessageMIDIAction() {
        return nearbyMessageMIDIAction;
    }

    public boolean getNearbyMessageSticky() {
        return nearbyMessageSticky;
    }

    public void setNearbyMessageSticky(boolean nearbyMessageSticky) {
        this.nearbyMessageSticky = nearbyMessageSticky;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyMessageSticky", nearbyMessageSticky);
    }


    public void setBrowseHostFragment(BrowseHostFragment browseHostFragment) {
        this.browseHostFragment = browseHostFragment;
    }

    // If we are a host, we might be asked to return an list of items
    // This list will be built from the arraylists but passed as a string split by lines
    public String getHostItems(String what) {
        ArrayList<String> hostItems;
        switch (what) {
            case "browsesets":
                hostItems = mainActivityInterface.getStorageAccess().listFilesInFolder("Sets", "");
                // Add the current set to the list if it isn't empty
                if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
                    hostItems.add(0, "["+c.getString(R.string.set_current)+"]");
                }
                break;
            case "browsesongs":
            default:
                hostItems = mainActivityInterface.getStorageAccess().getSongIDsFromFile();
                break;
            case "browseprofiles":
                hostItems = mainActivityInterface.getStorageAccess().listFilesInFolder("Profiles", "");
                break;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String item : hostItems) {
            stringBuilder.append(item).append("\n");
        }
        return stringBuilder.toString().trim();
    }

    // This is where the client deals with the list of items it has received back from the host
    public void payloadHostItems(String incoming) {
        // Remove the header and device id
        incoming = incoming.replaceFirst(hostItems, "").replaceFirst(deviceId, "").trim();
        String[] hostItems = incoming.split("\n");
        if (browseHostFragment != null) {
            browseHostFragment.displayHostItems(hostItems);
        }
    }

    // This is the client sending a request to connected hosts
    // This is called from the browseHostFragment
    public void sendRequestHostItems() {
        switch (mainActivityInterface.getWhattodo()) {
            case "browsesets":
                doSendPayloadBytes(hostRequest + sets + deviceId, true);
                break;
            case "browseprofiles":
                doSendPayloadBytes(hostRequest + profiles + deviceId, true);
                break;
            case "browsesongs":
                doSendPayloadBytes(hostRequest + songs + deviceId, true);
                break;
        }
    }

    // This is where the client requests the host's current set
    // Called for the set manage page
    public void requestHostCurrentSet() {
        for (String endpointString : connectedEndpoints) {
            Log.d(TAG,"requestCurrentSet");
            String endpointId = getEndpointSplit(endpointString)[0];
            // Send the request
            String requestPayload = hostRequest + currentset + deviceId;
            Log.d(TAG,"requestPayload:"+requestPayload);
            Nearby.getConnectionsClient(activity).sendPayload(endpointId, Payload.fromBytes(requestPayload.getBytes()));
        }
    }

    public void dealWithHostCurrentSet(String requestPayload) {
        if (!isHost && requestPayload.contains(deviceId)) {
            // This is for us!  Remove the unnecessary stuff
            requestPayload = requestPayload.replace(hostItems,"").
                    replace(currentset,"").replace(deviceId,"");
            // What is left is the current set
            Log.d(TAG,"setCurrent:"+requestPayload);
            // Initialise the current set
            //mainActivityInterface.getCurrentSet().initialiseTheSet();
            //mainActivityInterface.getCurrentSet().setSetCurrent("");
            //mainActivityInterface.getCurrentSet().setSetCurrentBeforeEdits("");
            //mainActivityInterface.getPreferences().setMyPreferenceString("setCurrent",requestPayload);
            //mainActivityInterface.getCurrentSet().loadCurrentSet();
        }
    }

    private void initialiseCurrentSet() {

    }
    // This is where the client requests a file from the host
    public void requestHostFile(String folder, String subfolder, String filename) {
        for (String endpointString : connectedEndpoints) {
            Log.d(TAG,"requestHostFile:"+folder+"  / " + subfolder + " / "+ filename);
            String endpointId = getEndpointSplit(endpointString)[0];
            // Send the file request
            String requestPayload = requestFileTag + deviceId + requestFileSeparator +
                    folder + requestFileSeparator + subfolder + requestFileSeparator + filename;
            Log.d(TAG,"requestPayload:"+requestPayload);
            Nearby.getConnectionsClient(activity).sendPayload(endpointId, Payload.fromBytes(requestPayload.getBytes()));
        }
    }

    // This is for the host to send the requested file to the calling device
    public void hostSendFile(String requestPayload) {
        Log.d(TAG,"HOST requestPayload:"+requestPayload);
        if (isHost) {
            // Break apart the requestPayload
            requestPayload = requestPayload.replace(requestFileTag, "");
            String[] bits = requestPayload.split(requestFileSeparator);
            // There should be 4 bits: calling deviceID, folder, subfolder, filename
            if (bits.length == 4) {
                Uri uri;
                if (bits[1].equals("Sets") && bits[2].equals("["+c.getString(R.string.set_current)+"]")) {
                    String currentSetXML = mainActivityInterface.getSetActions().createSetXML();
                    uri = mainActivityInterface.getStorageAccess().getUriForItem("Export","","currentSet.xml");
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,uri,null,"Export","","currentSet.xml");
                    OutputStream currentSetOutputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                    mainActivityInterface.getStorageAccess().writeFileFromString(currentSetXML,currentSetOutputStream);
                }
                uri = mainActivityInterface.getStorageAccess().getUriForItem(
                        bits[1], bits[2], bits[3]);
                Payload payloadFile = null;
                String payloadInfo = "";
                try {
                    ParcelFileDescriptor parcelFileDescriptor = c.getContentResolver().openFileDescriptor(uri, "r");
                    if (parcelFileDescriptor != null) {
                        payloadFile = Payload.fromFile(parcelFileDescriptor);
                        payloadInfo = requestFileTag + bits[0] +
                                requestFileSeparator + bits[1] +
                                requestFileSeparator + bits[2] +
                                requestFileSeparator + bits[3];
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error trying to send file: " + e);
                }
                Log.d(TAG,"payloadFile:"+payloadFile+"\nuri:"+uri);
                if (payloadFile != null) {
                    // Send the info lead then file to the requesting device
                    for (String endpointString : connectedEndpoints) {
                        String endpointId = getEndpointSplit(endpointString)[1];
                        Log.d(TAG, "endpointId:" + endpointId + "  requestingId:" + bits[0]);
                        if (endpointId.equals(bits[0])) {
                            // Get the endpointCode not the name
                            String endpointCode = getEndpointSplit(endpointString)[0];
                            Log.d(TAG,"try sending bytes:"+ Arrays.toString(payloadInfo.getBytes()));
                            Nearby.getConnectionsClient(activity).sendPayload(endpointCode,
                                    Payload.fromBytes(payloadInfo.getBytes()));
                            Nearby.getConnectionsClient(activity).sendPayload(endpointId, payloadFile);
                        }
                    }
                }
            }
        }

    }

    // This is where the client saves the payload requested file
    public void dealWithRequestedFile() {
        Log.d(TAG,"Client receiving dealWithRequestedFile()\nrequestedFilePayload:"+requestedFilePayload+"\nbrowseHostFragment:"+browseHostFragment);
        if (requestedFilePayload != null && browseHostFragment != null) {
            // Get the file folder, subfolder and filename from the BrowseHostFragment
            String folder = browseHostFragment.getRequestedFolder();
            String subfolder = browseHostFragment.getRequestedSubfolder();
            String filename = browseHostFragment.getRequestedFilename();

            if (folder != null && subfolder != null && filename != null && !filename.isEmpty()) {
                boolean receivedCurrentSet = false;
                if (folder.equals("Sets") && filename.equals("["+c.getString(R.string.set_current)+"]")) {
                    folder = "Received";
                    subfolder = "";
                    filename = "currentSet.xml";
                    receivedCurrentSet = true;
                }
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(folder, subfolder, filename);
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null, folder, subfolder, filename);

                try {
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " requested file created at /" + folder + "/" + subfolder + "/" + filename);
                    if (uri != null && requestedFilePayload.asFile() != null) { // i.e. we have received the file by choice
                        ParcelFileDescriptor parcelFileDescriptor = (Objects.requireNonNull(requestedFilePayload.asFile())).asParcelFileDescriptor();
                        InputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                        Uri originalUri = Uri.parse(parcelFileDescriptor.getFileDescriptor().toString());
                        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadFile copyFile from " + originalUri + " to " + uri);
                        if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                            if (nearbyReturnActionsInterface != null) {
                                // If the file is being placed in the Songs folder, update the database
                                if (folder.equals("Songs")) {
                                    // Make sure song is in the database (but not for received folder!
                                    if (!mainActivityInterface.getSQLiteHelper().songExists(subfolder, filename)) {
                                        mainActivityInterface.getSQLiteHelper().createSong(subfolder, filename);
                                        // Set the filetype
                                        Song tempSong = new Song();
                                        tempSong.setFolder(subfolder);
                                        tempSong.setFilename(filename);
                                        mainActivityInterface.getLoadSong().doLoadSongFile(tempSong, false);
                                        mainActivityInterface.getSQLiteHelper().updateSong(tempSong);
                                        // Refresh the song menu
                                        mainActivityInterface.updateSongList();
                                    }
                                } else if (receivedCurrentSet) {
                                    ArrayList<Uri> uris = new ArrayList<>();
                                    uris.add(uri);
                                    // Get a note of how many items were in the currently loaded set
                                    int oldSize = mainActivityInterface.getCurrentSet().getCurrentSetSize();

                                    // Initialise the current set
                                    mainActivityInterface.getCurrentSet().initialiseTheSet();
                                    mainActivityInterface.getCurrentSet().setSetCurrent("");
                                    mainActivityInterface.getCurrentSet().setSetCurrentBeforeEdits("");

                                    // Notify the set menu to update to an empty set
                                    mainActivityInterface.notifySetFragment("clear",oldSize);

                                    // Set this as our current set
                                    mainActivityInterface.getSetActions().loadSets(uris,null);
                                }
                            }
                        }
                        parcelFileDescriptor.close();

                        try {
                            if (mainActivityInterface.getStorageAccess().uriExists(originalUri)) {
                                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadFile deleteFile " + originalUri);
                                mainActivityInterface.getStorageAccess().deleteFile(originalUri);
                            }
                        } catch (Exception e) {
                            Log.d(TAG, "Error trying to delete originalUri");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setForceReload(boolean forceReload) {
        this.forceReload = forceReload;
    }
    public boolean getForceReload() {
        return forceReload;
    }
}
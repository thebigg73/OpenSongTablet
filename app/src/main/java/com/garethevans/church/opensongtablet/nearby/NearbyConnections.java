package com.garethevans.church.opensongtablet.nearby;

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

    private final Context c;
    private final String TAG = "NearbyConnections", sectionTag  = "___section___",
            songTag = "_xx____xx_", endpointSplit = "__",
            serviceId = "com.garethevans.church.opensongtablet";
    private final ArrayList<String> connectedEndpoints, discoveredEndpoints; //  CODE__DeviceName
    private final NearbyReturnActionsInterface nearbyReturnActionsInterface;
    private final MainActivityInterface mainActivityInterface;

    private Timer timer;
    private TimerTask timerTask;
    private SimpleArrayMap<Long, Payload> incomingFilePayloads = new SimpleArrayMap<>();
    private SimpleArrayMap<Long, String> fileNewLocation = new SimpleArrayMap<>();
    private boolean isHost, receiveHostFiles, keepHostFiles, usingNearby, temporaryAdvertise,
            isAdvertising = false, isDiscovering = false, nearbyHostMenuOnly,
            receiveHostAutoscroll = true, receiveHostSongSections = true, connectionsOpen,
            waitingForSectionChange = false, nearbyHostPassthrough;
    private AdvertisingOptions advertisingOptions;
    private DiscoveryOptions discoveryOptions;
    // The stuff used for Google Nearby for connecting devices
    private String payloadTransferIds = "", receivedSongFilename, connectionLog, deviceId,
            incomingPrevious;
    private Payload previousPayload;
    private int pendingCurrentSection = 0, hostSection = 0, countdown;
    private Strategy nearbyStrategy = Strategy.P2P_CLUSTER;


    // Initialise the class, preferences and interfaces
    public NearbyConnections(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        nearbyReturnActionsInterface = (NearbyReturnActionsInterface) c;

        // Initialise the connections
        connectedEndpoints = new ArrayList<>();
        discoveredEndpoints = new ArrayList<>();
        connectionLog = "";

        if (mainActivityInterface != null) {
            try {
                nearbyHostPassthrough = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyHostPassthrough",true);
                nearbyHostMenuOnly = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyHostMenuOnly", false);
                temporaryAdvertise = mainActivityInterface.getPreferences().getMyPreferenceBoolean("temporaryAdvertise",false);
                String preference = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyStrategy","cluster");
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

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        advertisingOptions = new AdvertisingOptions.Builder().setStrategy(nearbyStrategy).build();
        discoveryOptions = new DiscoveryOptions.Builder().setStrategy(nearbyStrategy).build();
    }


    // Our preferences for using Nearby
    public String getUserNickname() {
        String model = android.os.Build.MODEL.trim();
        // If the user has saved a value for their device name, use that instead
        // Don't need to save the device name unless the user edits it to make it custom
        return deviceId = mainActivityInterface.getPreferences().getMyPreferenceString("deviceId", model);
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

    // Set the strategy as either cluster (many to many) or star (one to many).
    public void setNearbyStrategy(Strategy nearbyStrategy) {
        this.nearbyStrategy = nearbyStrategy;
        advertisingOptions = new AdvertisingOptions.Builder().setStrategy(nearbyStrategy).build();
        discoveryOptions = new DiscoveryOptions.Builder().setStrategy(nearbyStrategy).build();
        if (nearbyStrategy.equals(Strategy.P2P_CLUSTER)) {
            updateConnectionLog(c.getString(R.string.connections_mode) + ": " + c.getString(R.string.connections_mode_cluster));
        } else if (nearbyStrategy.equals(Strategy.P2P_STAR)) {
            updateConnectionLog(c.getString(R.string.connections_mode)+ ": " + c.getString(R.string.connections_mode_star));
        } else {
            updateConnectionLog(c.getString(R.string.connections_mode)+ ": " + c.getString(R.string.connections_mode_single));
        }
    }
    public String getNearbyStrategyType() {
        if (nearbyStrategy==Strategy.P2P_STAR) {
            return "star";
        } else if (nearbyStrategy==Strategy.P2P_POINT_TO_POINT) {
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
                    if (countdown==0) {
                        nearbyReturnActionsInterface.nearbyEnableConnectionButtons();
                    } else {
                        nearbyReturnActionsInterface.nearbyUpdateCountdownText(advertise,materialButton);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask,0,1000);
    }
    public void clearTimer() {
        if (timerTask!=null) {
            timerTask.cancel();
        }
        if (timer!=null) {
            timer.purge();
        }
    }
    public int getCountdown() {
        return countdown;
    }
    public void doCountdown() {
        countdown --;
    }


    // Start or stop the advertising/discovery of devices to initiate connections
    @Override
    public void startAdvertising() {
        if (!isAdvertising) {
            Nearby.getConnectionsClient(c)
                    .startAdvertising(getUserNickname(), serviceId, connectionLifecycleCallback(), advertisingOptions)
                    .addOnSuccessListener(
                            (Void unused) -> {
                                // We're advertising!
                                updateConnectionLog(c.getString(R.string.connections_advertise_name) + " " + getUserNickname());
                                Log.d(TAG,"Advertising: "+getUserNickname());
                                isAdvertising = true;
                            })
                    .addOnFailureListener(
                            (Exception e) -> {
                                // We were unable to start advertising.
                                updateConnectionLog(c.getString(R.string.connections_failure) + " " + getUserNickname());
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
                Nearby.getConnectionsClient(c)
                        .startDiscovery(serviceId, endpointDiscoveryCallback(), discoveryOptions)
                        .addOnSuccessListener(
                                (Void unused) -> {
                                    // We're discovering!
                                    updateConnectionLog(c.getResources().getString(R.string.connections_discover));
                                    isDiscovering = true;
                                    Log.d(TAG, "startDiscovery() - success");
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
                Nearby.getConnectionsClient(c).stopAdvertising();
                updateConnectionLog(c.getString(R.string.connections_service_stop));
                Log.d(TAG, "stopAdvertising() - success");
            } catch (Exception e) {
                Log.d(TAG, "stopAdvertising() - failure: " + e);
            }
        }
    }
    @Override
    public void stopDiscovery() {
        if (isDiscovering) {
            try {
                Nearby.getConnectionsClient(c).stopDiscovery();
                updateConnectionLog(c.getString(R.string.connections_discover_stop));
                Log.d(TAG,"stopDiscovery() - success");
            } catch (Exception e) {
                Log.d(TAG, "stopDiscovery() - failure: " + e);
            }
        }
        isDiscovering = false;
    }


    // Once devices are found, deal with connecting them
    // Deals with connecting, initiating, ending connections
    private ConnectionLifecycleCallback connectionLifecycleCallback() {
        return new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                // Get a string for the connection
                String endpointString = getEndpointString(endpointId,connectionInfo.getEndpointName());
                Log.d(TAG,"connection initiated.  endpointString:"+endpointString+ "   endpointId="+endpointId);

                // Check to see if this device was already registered (permission granted)
                // If the device was previously registered, try to reconnect silently
                if (endpointRegistered(endpointString)) {
                    Log.d(TAG,"Previously connected to "+endpointString);
                    delayAcceptConnection(endpointString);

                } else {
                    Log.d(TAG,"Device wasn't previously connected: "+endpointString);
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
                                                Nearby.getConnectionsClient(c).rejectConnection(endpointString))
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    } else {
                        // The user is not accepting new connections, so we should reject the connection.
                        Log.d(TAG,"reject connection to "+endpointString);
                        Nearby.getConnectionsClient(c).rejectConnection(endpointString);
                    }
                }
            }

            @Override
            public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution connectionResolution) {
                String endpointString = getEndpointString(endpointId,getNameMatchingId(endpointId));
                Log.d(TAG,"endpointString:"+endpointString);
                Log.d(TAG,"Connection result: "+connectionResolution.getStatus().getStatusMessage());
                switch (connectionResolution.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                    case ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT:
                        Log.d(TAG,"connections status either ok or already connected");

                        // IV - Added handling of when already connected
                        if (!endpointRegistered(endpointString)) {
                            Log.d(TAG,"adding string: "+endpointString);
                            connectedEndpoints.add(endpointString);
                        }

                        if (isHost) {
                            // try to send the current song payload
                            Log.d(TAG,"Sending payload successful:" + sendSongPayload());
                        }
                        updateConnectionLog(c.getString(R.string.connections_connected) + " " + getEndpointSplit(endpointString)[1]);
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        Log.d(TAG,"Rejected");
                        if (endpointRegistered(endpointString)) {
                            Log.d(TAG,"removing string: "+endpointString);
                            connectedEndpoints.remove(endpointString);
                        }
                        updateConnectionLog(c.getString(R.string.cancel));


                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        Log.d(TAG,"Error status code");
                        // The connection broke before it was able to be accepted.
                        // The connection was rejected by one or both sides.
                        updateConnectionLog(c.getString(R.string.connections_failure) + " " + getUserNickname() +
                                " <-> " + getEndpointSplit(endpointString)[1]);
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
                String endpointString = getEndpointString(endpointId,disconnectedFrom);
                Log.d(TAG, "On disconnect: "+endpointString);

                int position = endpointPositionInArray(endpointString);
                if (usingNearby && position>=0) {
                    connectedEndpoints.remove(position);
                    updateConnectionLog(c.getResources().getString(R.string.connections_disconnect) +
                            " " + disconnectedFrom);
                }

                if (!isHost) {
                    // Clients should try to silently connect again after 2 seconds
                    Handler h = new Handler();
                    h.postDelayed(() -> startDiscovery(), 2000);
                }
            }
        };
    }
    // This is called when devices/endpoints are discovered
    private EndpointDiscoveryCallback endpointDiscoveryCallback() {
        return new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                String endpointString = getEndpointString(endpointId,discoveredEndpointInfo.getEndpointName());
                Log.d(TAG,"EndpointDiscoveryCallback  endpointString:"+endpointString);
                if (!endpointRegistered(endpointString)) {
                    // Only attempt a connection if we aren't already connected
                    Nearby.getConnectionsClient(c)
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
                                        Log.d(TAG,"On failure: "+(((ApiException) e).getStatus().getStatusMessage()));

                                        // IV - Added handling of when already connected
                                        if (((ApiException) e).getStatusCode() == ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT) {
                                            if (!endpointRegistered(endpointString)) {
                                                connectedEndpoints.add(endpointString);
                                            }
                                            updateConnectionLog(c.getString(R.string.connections_connected) + " " + discoveredEndpointInfo.getEndpointName());
                                            // IV - Already connected so replay last incoming song
                                            if (incomingPrevious != null && !incomingPrevious.equals("")) {
                                                String incoming = incomingPrevious;
                                                incomingPrevious = null;
                                                payloadOpenSong(incoming);
                                            }
                                            // We can stop discovery now
                                            stopDiscovery();
                                        } else {
                                            // Nearby Connections failed to request the connection.
                                            Log.d(TAG,"A general error");
                                            updateConnectionLog(c.getString(R.string.connections_failure) + " " + discoveredEndpointInfo.getEndpointName());
                                        }
                                        Log.d(TAG, "Connections failure: " + e);
                                    });
                }
            }

            @Override
            public void onEndpointLost(@NonNull String endpointId) {
                String endpointName = getNameMatchingId(endpointId);
                String endpointString = getEndpointString(endpointId,endpointName);
                Log.d(TAG, "onEndPointlost: "+endpointString);

                updateConnectionLog(c.getString(R.string.connections_disconnect) + " " + endpointName);
                // Try to connect again after 2 seconds
                if (!isHost) {
                    Handler h = new Handler();
                    h.postDelayed(() -> startDiscovery(), 2000);
                }
            }
        };
    }
    // A delayed connections function
    private void delayAcceptConnection(String endpointString) {
        // For stability add a small delay
        Handler waitAccept = new Handler();
        waitAccept.postDelayed(() -> {
            // Add a note of the nice name on to the endpointId
            Log.d(TAG, "about to try and accept " + endpointString);
            if (!discoveredEndpoints.contains(endpointString)) {
                discoveredEndpoints.add(endpointString);
            }

            // The user confirmed, so we can accept the connection.
            Nearby.getConnectionsClient(c)
                    .acceptConnection(getEndpointSplit(endpointString)[0], payloadCallback());
        }, 200);
    }


    // Triggered when a host has sent a payload - this is where clients listen out!
    // If the host is allowing passthrough, it doesn't listen, but passes it on
    private PayloadCallback payloadCallback() {
        return new PayloadCallback() {
            @Override
            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                // To avoid send loops, only devices set as clients act onPayloadReceived
                // However if we are set as cluster strategy, we should echo what we have received
                // This is because clients can be connected to the host through another client
                Log.d(TAG,"s: "+s+"  payload received:"+payload);

                // Check if we've already received/sent this out.  Only proceed if not
                if (previousPayload==null || !previousPayload.equals(payload)) {
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
                            if (incoming != null && incoming.startsWith("FILE:")) {
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
                            } else if (incoming != null && incoming.contains(sectionTag)) {
                                // IV - Section change only in Stage and Presentation mode (or PDF) when user option is selected
                                if ((!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) ||
                                        mainActivityInterface.getSong().getFiletype().equals("PDF")) &&
                                        receiveHostSongSections) {
                                    Log.d(TAG,"call payloadSection");
                                    payloadSection(incoming);
                                }
                            } else if (incoming != null && incoming.contains(songTag)) {
                                payloadOpenSong(incoming);
                            }
                            Log.d(TAG, "incoming=" + incoming);
                        }
                        // not dealing with files as it is complex with scoped storage access
                        // also don't want user's download folder getting clogged!
                    }
                }
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                // IV - If we are a client and not 'receiving host files' then cancel these uneeded FILE transfers
                if (!isHost && !receiveHostFiles) {
                    if (incomingFilePayloads.containsKey(payloadTransferUpdate.getPayloadId())) {
                        Log.d(TAG, "Cancelled Id " + payloadTransferUpdate.getPayloadId());
                        Nearby.getConnectionsClient(c).cancelPayload(payloadTransferUpdate.getPayloadId());
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
                        if (payloadTransferIds==null) {
                            payloadTransferIds = "";
                        }
                        if (!(payloadTransferIds.contains(payloadTransferUpdate.getPayloadId() + " "))) {
                            payloadTransferIds = payloadTransferIds + payloadTransferUpdate.getPayloadId() + " ";
                            Log.d("NearbyConnections", "Id History " + payloadTransferIds);
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

        if (split.length>0 && split[0]!=null) {
            returnVal[0] = split[0];
        } else {
            returnVal[0] = "0000";
        }
        if (split.length>1 && split[1]!=null) {
            returnVal[1] = split[1];
        } else {
            returnVal[1] = "Unknown";
        }
        return returnVal;
    }
    private boolean endpointRegistered(String endpointString) {
        if (!endpointString.contains(endpointSplit)) {
            endpointString = endpointString + endpointSplit;
        }
        boolean found = false;
        for (String string:connectedEndpoints) {
            if (string.contains(endpointString)) {
                found = true;
            }
            Log.d(TAG,"array item: "+string+"  looking for"+endpointString);
        }
        return found;
    }
    private int endpointPositionInArray(String endpointString) {
        return connectedEndpoints.indexOf(endpointString);
    }
    public boolean hasValidConnections() {
        if (usingNearby) {
            try {
            StringBuilder stringBuilder = new StringBuilder();
                if (connectedEndpoints.size() >= 1) {
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
        for (String ep:discoveredEndpoints) {
            if (ep.startsWith(endpointId)) {
                nicename = getEndpointSplit(ep)[1];
            }
        }
        return nicename;
    }
    public String getConnectedDevicesAsString() {
        if (connectedEndpoints==null || connectedEndpoints.isEmpty()) {
            return c.getString(R.string.connections_no_devices);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (String endpointString:connectedEndpoints) {
                stringBuilder.append(getEndpointSplit(endpointString)[1])
                        .append("\n");
            }
            return stringBuilder.toString().trim();
        }
    }


    // Deal with sending payloads as a host for clients to listen for
    @Override
    public void doSendPayloadBytes(String infoPayload) {
        if (isHost) {
            for (String endpointString : connectedEndpoints) {
                String endpointId = getEndpointSplit(endpointString)[0];
                Nearby.getConnectionsClient(c).sendPayload(endpointId, Payload.fromBytes(infoPayload.getBytes()));
            }
        }
    }


    private void repeatPayload(Payload payload) {
        if (nearbyStrategy==Strategy.P2P_CLUSTER) {
            for (String connectedEndpoint : connectedEndpoints) {
                Nearby.getConnectionsClient(c).sendPayload(connectedEndpoint, payload);
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
        waitingForSectionChange = true;
        pendingCurrentSection = -1;

        // IV - Process each end point - we need a unique ParcelFileDescriptor if a file is sent
        for (String endpointString : connectedEndpoints) {
            String endpointId = getEndpointSplit(endpointString)[0];

            // IV - Send current section as a pending section change (-ve offset by 1) for use by CLIENT song load
            infoPayload = sectionTag + (1 + mainActivityInterface.getSong().getCurrentSection());
            Nearby.getConnectionsClient(c).sendPayload(endpointId, Payload.fromBytes(infoPayload.getBytes()));

            infoPayload = null;

            // New method sends OpenSong songs in the format of
            // FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>
            // songTag = "_xx____xx_";

            if (mainActivityInterface.getSong().getFiletype().equals("XML")) {
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
                    Nearby.getConnectionsClient(c).sendPayload(endpointId, Payload.fromBytes(infoPayload.getBytes()));
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
                Log.d(TAG, "payloadFile="+payloadFile);
                if (payloadFile != null) {
                    // Send the file descriptor as bytes, then the file
                    Nearby.getConnectionsClient(c).sendPayload(endpointId, Payload.fromBytes(infoFilePayload.getBytes()));
                    Nearby.getConnectionsClient(c).sendPayload(endpointId, payloadFile);
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
        String infoPayload;
        if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
            infoPayload = sectionTag + (mainActivityInterface.getSong().getPdfPageCurrent());
        } else {
            infoPayload = sectionTag + (mainActivityInterface.getSong().getCurrentSection());
        }
        doSendPayloadBytes(infoPayload);
    }

    public void sendAutoscrollPayload(String message) {
        doSendPayloadBytes(message);
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
            totalSections = mainActivityInterface.getSong().getSongSections().size();
        }
        Log.d(TAG,"doSectionChange().  onSectionAlready="+onSectionAlready);
        Log.d(TAG,"totalSections="+totalSections+"  mysection="+mysection);
        if (!onSectionAlready && nearbyReturnActionsInterface != null && totalSections>mysection) {
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

        ArrayList<String> receivedBits = getNearbyIncoming(incoming);
        Log.d(TAG,"incoming: "+incoming+"\nprevious: "+incomingPrevious);
        boolean incomingChange = (!incoming.equals(incomingPrevious));

        Log.d(TAG,"incomingChange="+incomingChange);

        if (incomingChange) {
            incomingPrevious = incoming;
            OutputStream outputStream;

            // If 'Receiving host songs' then only BYTES songs with xml arrive here - we use the  4th <?xml> bit
            // If not 'Receiving host songs' then all songs arrive here including FILES: which have a dummy 4th <?xml> bit - we do not use xml
            boolean songReceived = (receivedBits.size() >= 4);

            Log.d(TAG,"songReceived="+songReceived);
            Log.d(TAG,"receivedBits.size()="+receivedBits.size());

            Log.d(TAG,"isHost="+isHost+"  hasValidConnections()="+hasValidConnections()+"  receiveHostFiles="+receiveHostFiles+"  keepHostFiles="+keepHostFiles);
            if (songReceived) {
                if (!isHost && hasValidConnections() && receiveHostFiles) {
                    // We want to receive host files (we aren't the host either!) and an OpenSong song has been sent/received
                    mainActivityInterface.getDisplayPrevNext().setSwipeDirection(receivedBits.get(2));

                    // If the user wants to keep the host file, we will save it to our storage.
                    // If we already have it, it will overwrite it, if not, we add it
                    Uri newLocation;
                    if (keepHostFiles) {
                        // Prepare the output stream in the client Songs folder
                        // Check the folder exists, if not, create it
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" payloadOpenSong createFile Songs/"+receivedBits.get(0));
                        mainActivityInterface.getStorageAccess().createFile(DocumentsContract.Document.MIME_TYPE_DIR, "Songs", receivedBits.get(0), "");
                        newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Songs", receivedBits.get(0), receivedBits.get(1));
                        // Create the file if it doesn't exist
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" payLoadOpenSong() keepHostFiles Create Songs/"+receivedBits.get(0)+"/"+receivedBits.get(1)+"  deleteOld=true");
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newLocation, null, "Songs", receivedBits.get(0), receivedBits.get(1));
                        outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newLocation);
                        mainActivityInterface.getSong().setFolder(receivedBits.get(0));
                        mainActivityInterface.getSong().setFilename(receivedBits.get(1));
                        // Add to the sqldatabase
                        mainActivityInterface.getSQLiteHelper().createSong(mainActivityInterface.getSong().getFolder(), mainActivityInterface.getSong().getFilename());
                        Log.d(TAG,"keepFile: "+newLocation);
                    } else {
                        newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", "ReceivedSong");
                        // Prepare the output stream in the Received folder - just keep a temporary version
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" payLoadOpenSong !keepHostFiles Create Songs/"+receivedBits.get(0)+"/"+receivedBits.get(1)+" deleteOld=true");
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newLocation, null, "Received", "", "ReceivedSong");
                        outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newLocation);
                        mainActivityInterface.getSong().setFolder("../Received");
                        mainActivityInterface.getSong().setFilename("ReceivedSong");
                        // IV - Store the received song filename in case the user wants to duplicate the received song
                        receivedSongFilename = receivedBits.get(1);

                        Log.d(TAG,"receiveFile: "+newLocation);
                    }

                    Log.d(TAG,"outputstream: "+outputStream);

                    // Write the file to the desired output stream and load
                    if (nearbyReturnActionsInterface != null) {
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" payloadOpenSong writeFileFromString "+newLocation+" with: "+receivedBits.get(3));
                        Log.d(TAG,"write the file: "+mainActivityInterface.getStorageAccess().writeFileFromString(receivedBits.get(3), outputStream));
                        mainActivityInterface.getSong().setCurrentSection(pendingCurrentSection);

                        // If we are keeping the song, update the database song first
                        if (receiveHostFiles && keepHostFiles) {
                            mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSongFile(mainActivityInterface.getSong(),false));
                            mainActivityInterface.getSQLiteHelper().updateSong(mainActivityInterface.getSong());
                            mainActivityInterface.updateSongMenu(mainActivityInterface.getSong());
                        }
                        nearbyReturnActionsInterface.loadSong();
                    }
                } else if (!isHost && hasValidConnections()) {
                    // We just want to trigger loading the song on our device (if we have it).
                    // If not, we get notified it doesn't exits
                    mainActivityInterface.getSong().setFolder(receivedBits.get(0));
                    mainActivityInterface.getSong().setFilename(receivedBits.get(1));
                    mainActivityInterface.getDisplayPrevNext().setSwipeDirection(receivedBits.get(2));

                    // Now load the song if we are displaying the performace/stage/presenter fragment
                    if (nearbyReturnActionsInterface != null) {
                        mainActivityInterface.getSong().setCurrentSection(pendingCurrentSection);
                        nearbyReturnActionsInterface.loadSong();
                    }
                }
            }

        } else {
            Log.d(TAG, "payloadOpenSong - no change as unchanged payload");
        }
        // IV - 0 is no pending
        pendingCurrentSection = 0;
    }
    private ArrayList<String> getNearbyIncoming(String incoming) {
        // New method sends OpenSong songs in the format of
        // FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>
        // songTag = "_xx____xx_";
        // Four distict parts
        ArrayList<String> arrayList = new ArrayList<>();
        String[] bits = incoming.split(songTag);
        for (int i=0; i<4; i++) {
            if (bits.length>i) {
                arrayList.add(i,bits[i]);
                Log.d(TAG,"bits["+i+"]="+bits[i]);
            } else {
                // Old format or something not right.  Avoid null values returned
                arrayList.add(i,"");
            }
        }

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
            boolean movepage = false;
            if ((folder.equals(mainActivityInterface.getSong().getFolder()) || mainActivityInterface.getSong().getFolder().equals("../Received"))
                    && filename.equals(mainActivityInterface.getSong().getFilename()) && filename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                // We are likely trying to move page to an already received file
                movepage = true;
            } else {
                mainActivityInterface.getSong().setPdfPageCurrent(0);
            }
            Uri newLocation = null;
            if (!isHost && hasValidConnections() && receiveHostFiles && keepHostFiles && filename!=null && !filename.isEmpty()) {
                // The new file goes into our main Songs folder if we don't already have it
                newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Songs", folder, filename);
                if (!mainActivityInterface.getStorageAccess().uriExists(newLocation)) {
                    Log.d(TAG,"Client doesn't have the song, so create it");
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" payloadFile Create Songs/"+folder+"/"+filename+" deleteOld=false");
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, newLocation, null, "Songs", folder, filename);
                } else {
                    // Check it isn't a zero filesize/corrupt
                    if (mainActivityInterface.getStorageAccess().getFileSizeFromUri(newLocation)==0) {
                        Log.d(TAG,"0b file - bring in this one");
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" payloadFile 0kb file Create Songs/"+folder+"/"+filename+" deleteOld=true");
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newLocation, null, "Songs", folder, filename);
                    } else {
                        // Set to null as we don't need to create it as we already have this song
                        Log.d(TAG, "Client already has the song.  Do nothing");
                        newLocation = null;
                    }
                }
            } else if (!isHost && hasValidConnections() && receiveHostFiles && filename!=null && !filename.isEmpty()) {
                // The new file goes into our Received folder
                folder = "../Received";
                // IV - Store the received song filename in case the user wants to duplicate the received song
                receivedSongFilename = filename;
                newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", filename);
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" payloadFile Create Received/"+folder+"/"+filename+" deleteOld=true");
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newLocation, null, "Received", "", filename);
            }
            mainActivityInterface.getSong().setFolder(folder);
            mainActivityInterface.getSong().setFilename(filename);
            if (movepage) {
                if (mainActivityInterface.getDisplayPrevNext().getSwipeDirection().equals("L2R")) {
                    // Go back
                    if (nearbyReturnActionsInterface != null) {
                        nearbyReturnActionsInterface.goToPreviousPage();
                    }
                } else {
                    // Go forward
                    if (nearbyReturnActionsInterface != null) {
                        nearbyReturnActionsInterface.goToNextPage();
                    }
                }
            } else if (newLocation != null && payload.asFile() != null) { // i.e. we have received the file by choice
                InputStream inputStream = new FileInputStream(Objects.requireNonNull(payload.asFile()).asParcelFileDescriptor().getFileDescriptor());
                Uri originalUri = Uri.parse(Objects.requireNonNull(payload.asFile()).asParcelFileDescriptor().getFileDescriptor().toString());
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newLocation);
                Log.d(TAG, "receiving File.  Original uri=" + originalUri);
                Log.d(TAG, "new location = " + newLocation);
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" payloadFile copyFile from "+originalUri+" to "+newLocation);
                if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                    if (nearbyReturnActionsInterface != null) {
                        mainActivityInterface.getSong().setCurrentSection(pendingCurrentSection);
                        // Make sure song is in the database (but not for received folder!
                        if (!folder.startsWith("../") && !folder.startsWith("**") &&
                                !mainActivityInterface.getSQLiteHelper().songExists(folder,filename)) {
                            Log.d(TAG,"About to add to the database - folder: " + folder + "  filename:"+filename);
                            mainActivityInterface.getSQLiteHelper().createSong(folder,filename);
                            // Set the filetype
                            mainActivityInterface.getStorageAccess().isIMGorPDF(mainActivityInterface.getSong());
                            mainActivityInterface.getSong().setTitle(filename);
                            mainActivityInterface.getSQLiteHelper().updateSong(mainActivityInterface.getSong());
                            // Refresh the song menu
                            mainActivityInterface.updateSongList();
                        }

                        nearbyReturnActionsInterface.loadSong();

                        // IV - End pending
                        pendingCurrentSection = 0;
                    }
                }
                try {
                    Log.d(TAG, "try to remove originalUri");
                    if (mainActivityInterface.getStorageAccess().uriExists(originalUri)) {
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" payloadFile deleteFile "+originalUri);
                        mainActivityInterface.getStorageAccess().deleteFile(originalUri);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error trying to delete originalUri");
                }
            } else {
                if (nearbyReturnActionsInterface != null && filename!=null && !filename.isEmpty()) {
                    Log.d(TAG,"No song copied as already have it");
                    mainActivityInterface.getSong().setCurrentSection(pendingCurrentSection);
                    nearbyReturnActionsInterface.loadSong();
                    // IV - End pending
                    pendingCurrentSection = 0;
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
        if (incoming!=null && incoming.startsWith(sectionTag)) {
            incoming = incoming.replace(sectionTag,"");
            try {
                return Integer.parseInt(incoming);
            } catch (Exception e) {
                return 0;
            }
        } else {
            return 0;
        }
    }
    public void setHostSection(int hostSection) {
        this.hostSection = hostSection;
    }
    public int getHostSection() {
        return hostSection;
    }
    private void payloadSection(String incoming) {
        if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) ||
                mainActivityInterface.getSong().getFiletype().equals("PDF")) {
            int mysection = getNearbySection(incoming);
            if (mainActivityInterface.getSong().getCurrentlyLoading()) {
                // IV - A song load is pending - 'Store' the section change for use by song load
                pendingCurrentSection = mysection;
                waitingForSectionChange = true;
                Log.d(TAG,"Song is still loading, but received section "+mysection);
            } else {
                pendingCurrentSection = -1;
                waitingForSectionChange = false;
                // IV - Do the section change assuming we have this many sections
                Log.d(TAG,"Song has finished loading and received section "+mysection);
                doSectionChange(mysection);
            }
        }
    }
    // If the client received a section before a song has finished loading
    public boolean getWaitingForSectionChange() {
        return waitingForSectionChange;
    }
    public void setWaitingForSectionChange(boolean waitingForSectionChange) {
        this.waitingForSectionChange = waitingForSectionChange;
    }
    public int getPendingCurrentSection() {
        return pendingCurrentSection;
    }
    public void setPendingCurrentSection(int pendingCurrentSection) {
        this.pendingCurrentSection = pendingCurrentSection;
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


    // Deal with turning off Nearby and cleaning up transferIds
    @Override
    public void turnOffNearby() {
        try {
            Nearby.getConnectionsClient(c).stopAllEndpoints();
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
        Log.d(TAG, "Cancel Ids " + payloadTransferIds);
        if (payloadTransferIds!=null && !payloadTransferIds.isEmpty()) {
            String[] ids = payloadTransferIds.trim().split(" ");
            payloadTransferIds = "";
            for (String Id : ids) {
                Nearby.getConnectionsClient(c).cancelPayload(Long.parseLong((Id.trim())));
            }
            incomingFilePayloads = new SimpleArrayMap<>();
            fileNewLocation = new SimpleArrayMap<>();
        }
    }


    // Updates the connnection log with this message.  This also updates the connected devices note
    private void updateConnectionLog(String newMessage) {
        Log.d(TAG,"Connection Log update: "+newMessage);
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
}
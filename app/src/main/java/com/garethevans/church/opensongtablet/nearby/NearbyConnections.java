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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

// Big updates here
// Users can specify Nearby connection strategy as either cluster or star
// Regardless if devices are set as hosts or clients, they can advertise or discover
// Changing the mode doesn't automatically advertise/discover/

public class NearbyConnections implements NearbyInterface {

    // TODO Still to implement
    private final Context c;
    private final String TAG = "NearbyConnections";
    //private final ArrayList<String> connectedEndPoints;
    private final ArrayList<String> connectedEndpoints; //  CODE__DeviceName
    private final ArrayList<String> discoveredEndpoints;
    //private final ArrayList<String> connectedDeviceIds;
    private final String sectionTag  = "___section___";
    private final String endpointTag = "_ep__ep_";
    private final String songTag = "_xx____xx_";
    private final String endpointSplit = "__";

    // Handler for stop of discovery
    private final Handler stopDiscoveryHandler = new Handler();
    private final Runnable stopDiscoveryRunnable;
    private SimpleArrayMap<Long, Payload> incomingFilePayloads = new SimpleArrayMap<>();
    private SimpleArrayMap<Long, String> fileNewLocation = new SimpleArrayMap<>();
    public String deviceId, connectionLog, incomingPrevious, connectionId;
    public boolean isConnected, isHost, receiveHostFiles, keepHostFiles, usingNearby,
            isAdvertising = false, isDiscovering = false, nearbyHostMenuOnly,
            receiveHostAutoscroll = true,
            receiveHostSongSections = true;
    private final NearbyReturnActionsInterface nearbyReturnActionsInterface;
    private AdvertisingOptions advertisingOptions;
    private DiscoveryOptions discoveryOptions;
    // The stuff used for Google Nearby for connecting devices
    private final String serviceId = "com.garethevans.church.opensongtablet";
    private String receivedSongFilename;
    private final MainActivityInterface mainActivityInterface;
    private int hostSection = 0;
    private String payLoadTransferIds = "";
    private String latestfoldernamepair = "";
    private int pendingCurrentSection = 0;
    private boolean connectionsOpen, waitingForSectionChange = false;
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
                nearbyHostMenuOnly = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyHostMenuOnly", false);
                if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyStrategyCluster",true)) {
                    nearbyStrategy = Strategy.P2P_CLUSTER;
                } else {
                    nearbyStrategy = Strategy.P2P_STAR;
                }
                setNearbyStrategy(nearbyStrategy);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        advertisingOptions = new AdvertisingOptions.Builder().setStrategy(nearbyStrategy).build();
        discoveryOptions = new DiscoveryOptions.Builder().setStrategy(nearbyStrategy).build();
        stopDiscoveryRunnable = this::stopDiscovery;
    }

    // Is the connections fragment open (as host can set to only listen when it is to avoid interruptions)
    public void setConnectionsOpen(boolean connectionsOpen) {
        this.connectionsOpen = connectionsOpen;
    }
    public boolean getConnectionsOpen() {
        return connectionsOpen;
    }

    // Set the strategy as either cluster (many to many) or star (one to many).
    public void setNearbyStrategy(Strategy nearbyStrategy) {
        this.nearbyStrategy = nearbyStrategy;
        advertisingOptions = new AdvertisingOptions.Builder().setStrategy(nearbyStrategy).build();
        discoveryOptions = new DiscoveryOptions.Builder().setStrategy(nearbyStrategy).build();
        if (nearbyStrategy.equals(Strategy.P2P_CLUSTER)) {
            updateConnectionLog(c.getString(R.string.connections_mode) + ": " + c.getString(R.string.connections_cluster));
        } else {
            updateConnectionLog(c.getString(R.string.connections_mode)+ ": " + c.getString(R.string.connections_star));
        }
        Log.d(TAG,"Strategy set to: "+nearbyStrategy);
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
            // IV - Stop 30s after this (latest) call
            if (isDiscovering) {
                stopDiscoveryHandler.removeCallbacks(stopDiscoveryRunnable);
                stopDiscoveryHandler.postDelayed(stopDiscoveryRunnable, 30000);
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




    // Deal with endpointIds.
    // The endpointId is a random bit of code that identifies a device
    // The connectionInfo.getEndpointName() is a user readable name of a device
    // Once a connection is made we store both as a string like id__name
    // These strings are stored in the connectedEndpoints arraylist
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
        if (!endpointString.contains(endpointTag)) {
            endpointString = endpointString + endpointTag;
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
    private boolean stillValidConnections() {
        try {
            if (connectedEndpoints.size() >= 1) {
                for (String s : connectedEndpoints) {
                    if (s != null && !s.isEmpty()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
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
                        // We're connected! Can now start sending and receiving data.
                        isConnected = true;

                        if (isHost) {
                            // try to send the current song payload
                            Log.d(TAG,"Sending payload successful:" + sendSongPayload());
                        }

                        // Send a note of all connected devices to all connected devices
                        // Gives all devices a record of each other
                        StringBuilder stringBuilder = new StringBuilder();
                        for (String ep: connectedEndpoints) {
                            Log.d(TAG,"building list of endpoints to send: "+ep);
                            stringBuilder.append(endpointTag).append(ep);
                        }
                        doSendPayloadBytes(stringBuilder.toString());
                        Log.d(TAG,"endpoints to send: "+stringBuilder);
                        updateConnectionLog(c.getString(R.string.connections_connected) + " " + getEndpointSplit(endpointString)[1]);
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        Log.d(TAG,"Rejected");
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
                isConnected = false;
                Log.d(TAG, "On disconnect: "+endpointString);

                int position = endpointPositionInArray(endpointString);
                if (usingNearby && position>=0) {
                    connectedEndpoints.remove(position);
                    updateConnectionLog(c.getResources().getString(R.string.connections_disconnect) +
                            " " + disconnectedFrom);
                }

                if (isHost) {
                    // Check if we have valid connections
                    isConnected = stillValidConnections();
                } else {
                    // Clients should try to connect again after 2 seconds
                    isConnected = false;
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
                                            isConnected = true;
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
                // Check if we have valid connections
                isConnected = stillValidConnections();
                // Try to connect again after 2 seconds
                if (!isHost) {
                    Handler h = new Handler();
                    h.postDelayed(() -> startDiscovery(), 2000);
                }
            }
        };
    }
    // A delayed connections
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




    // Deal with sending payloads as a host for clients to listen for
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
                                mainActivityInterface.getDisplayPrevNext().getSwipeDirection() + "<?xml>";
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
        String infoPayload = sectionTag + (mainActivityInterface.getSong().getCurrentSection());
        doSendPayloadBytes(infoPayload);
    }
    @Override
    public void doSendPayloadBytes(String infoPayload) {
        if (isHost) {
            for (String endpointString : connectedEndpoints) {
                String endpointId = getEndpointSplit(endpointString)[0];
                Nearby.getConnectionsClient(c).sendPayload(endpointId, Payload.fromBytes(infoPayload.getBytes()));
            }
        }
    }

    // Triggered when a host has sent a payload - this is where clients listen out!
    private PayloadCallback payloadCallback() {
        return new PayloadCallback() {
            @Override
            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                // To avoid send loops, only devices set as clients act onPayloadReceived
                Log.d(TAG,"s: "+s+"  payload received:"+payload);
                if (!isHost) {
                    // We can deal with the incoming payload!
                    if (payload.getType() == Payload.Type.FILE) {
                        // Make a note of it.  Nothing happens until complete
                        Log.d(TAG,"Payload.Type: FILE");
                        incomingFilePayloads.put(payload.getId(), payload);

                    } else if (payload.getType() == Payload.Type.BYTES) {
                        // We're dealing with bytes
                        Log.d(TAG,"Payload.Type: BYTES");
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
                            if (mainActivityInterface.getMode().equals("Performance") && receiveHostAutoscroll) {
                                payloadAutoscroll(incoming);
                            }
                        } else if (incoming != null && incoming.contains(sectionTag)) {
                            // IV - Section change only in Stage and Presentation mode when user option is selected
                            if (!mainActivityInterface.getMode().equals("Performance") && receiveHostSongSections) {
                                payloadSection(incoming);
                            }
                        } else if (incoming != null && incoming.contains(endpointTag)) {
                            payloadEndpoints(incoming);
                        } else if (incoming != null && incoming.contains(songTag)) {
                            payloadOpenSong(incoming);
                        }
                        Log.d(TAG,"incoming="+incoming);
                    }
                    // not dealing with files as it is complex with scoped storage access
                    // also don't want user's download folder getting clogged!
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
                        Log.d(TAG,"update payloadId()="+ payloadTransferUpdate.getPayloadId());
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
                    }
                }
            }
        };
    }
    private void payloadAutoscroll(String incoming) {
        // It sends autoscroll startstops as autoscroll_start or autoscroll_stop
        if (mainActivityInterface.getMode().equals("Performance")) {
            // Adjust only when not already in the correct state
            if (nearbyReturnActionsInterface != null &&
                    !(mainActivityInterface.getAutoscroll().getIsAutoscrolling() == incoming.equals("autoscroll_start"))) {
                nearbyReturnActionsInterface.toggleAutoscroll();
            }
        }
    }
    private void payloadSection(String incoming) {
        if (!mainActivityInterface.getMode().equals("Performance") ||
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



    public void doSectionChange(int mysection) {
        if (mainActivityInterface.getSong().getCurrentSection() != mysection &&
                nearbyReturnActionsInterface != null &&
                mainActivityInterface.getSong().getSongSections().size()>mysection) {
            mainActivityInterface.getSong().setCurrentSection(mysection);
            nearbyReturnActionsInterface.selectSection(mysection);
        }
    }
    private void payloadOpenSong(String incoming) {
        // IV - CLIENT: Cancel previous song transfers - a new song has arrived
        cancelTransferIds();

        // New method sends OpenSong songs in the format of
        //  FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>

        ArrayList<String> receivedBits = getNearbyIncoming(incoming);
        boolean incomingChange = (!incoming.equals(incomingPrevious));

        if (incomingChange) {
            incomingPrevious = incoming;
            OutputStream outputStream;

            // If 'Receiving host songs' then only BYTES songs with xml arrive here - we use the  4th <?xml> bit
            // If not 'Receiving host songs' then all songs arrive here including FILES: which have a dummy 4th <?xml> bit - we do not use xml
            boolean songReceived = (receivedBits.size() >= 4);

            if (songReceived) {
                if (!isHost && isConnected && receiveHostFiles) {
                    // We want to receive host files (we aren't the host either!) and an OpenSong song has been sent/received
                    mainActivityInterface.getDisplayPrevNext().setSwipeDirection(receivedBits.get(2));

                    // If the user wants to keep the host file, we will save it to our storage.
                    // If we already have it, it will overwrite it, if not, we add it
                    Uri newLocation;
                    if (keepHostFiles) {
                        // Prepare the output stream in the client Songs folder
                        // Check the folder exists, if not, create it
                        mainActivityInterface.getStorageAccess().createFile(DocumentsContract.Document.MIME_TYPE_DIR, "Songs", receivedBits.get(0), "");
                        newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Songs", receivedBits.get(0), receivedBits.get(1));
                        // Create the file if it doesn't exist
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, newLocation, null, "Songs", receivedBits.get(0), receivedBits.get(1));
                        outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newLocation);
                        mainActivityInterface.getSong().setFolder(receivedBits.get(0));
                        mainActivityInterface.getSong().setFilename(receivedBits.get(1));
                        // Add to the sqldatabase
                        mainActivityInterface.getSQLiteHelper().createSong(mainActivityInterface.getSong().getFolder(), mainActivityInterface.getSong().getFilename());

                    } else {
                        newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", "ReceivedSong");
                        // Prepare the output stream in the Received folder - just keep a temporary version
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, newLocation, null, "Received", "", "ReceivedSong");
                        outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newLocation);
                        mainActivityInterface.getSong().setFolder("../Received");
                        mainActivityInterface.getSong().setFilename("ReceivedSong");
                        // IV - Store the received song filename in case the user wants to duplicate the received song
                        receivedSongFilename = receivedBits.get(1);
                    }

                    // Write the file to the desired output stream and load
                    if (nearbyReturnActionsInterface != null) {
                        mainActivityInterface.getStorageAccess().writeFileFromString(receivedBits.get(3), outputStream);
                        nearbyReturnActionsInterface.prepareSongMenu();
                        mainActivityInterface.getSong().setCurrentSection(pendingCurrentSection);
                        nearbyReturnActionsInterface.loadSong();
                    }
                } else if (!isHost && isConnected) {
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
    private void payloadFile(Payload payload, String foldernamepair) {
        // IV - CLIENT: Cancel previous song transfers - a new song has arrived
        cancelTransferIds();

        // If songs are too big, then we receive them as a file rather than bytes
        String[] bits = foldernamepair.split(songTag);
        String folder = bits[0];
        String filename = bits[1];
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
        if (!isHost && isConnected && receiveHostFiles && keepHostFiles) {
            // The new file goes into our main Songs folder
            newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Songs", folder, filename);
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, newLocation, null, "Songs", folder, filename);
        } else if (!isHost && isConnected && receiveHostFiles) {
            // The new file goes into our Received folder
            folder = "../Received";
            // IV - Store the received song filename in case the user wants to duplicate the received song
            receivedSongFilename = filename;
            newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", filename);
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newLocation, null, "Received", "", filename);
        }
        mainActivityInterface.getSong().setFolder(folder);
        mainActivityInterface.getSong().setFilename(filename);
        if (movepage) {
            if (mainActivityInterface.getDisplayPrevNext().getSwipeDirection().equals("L2R")) {
                // Go back
                if (nearbyReturnActionsInterface != null) {
                    nearbyReturnActionsInterface.goToPreviousItem();
                }
            } else {
                // Go forward
                if (nearbyReturnActionsInterface != null) {
                    nearbyReturnActionsInterface.goToNextItem();
                }
            }
        } else if (newLocation != null && payload != null && payload.asFile() != null) { // i.e. we have received the file by choice
            InputStream inputStream = new FileInputStream(Objects.requireNonNull(payload.asFile()).asParcelFileDescriptor().getFileDescriptor());
            Uri originalUri = Uri.parse(Objects.requireNonNull(payload.asFile()).asParcelFileDescriptor().getFileDescriptor().toString());
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newLocation);
            if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                if (nearbyReturnActionsInterface != null) {
                    mainActivityInterface.getSong().setCurrentSection(pendingCurrentSection);
                    nearbyReturnActionsInterface.loadSong();
                    // IV - End pending
                    pendingCurrentSection = 0;
                }
            }
            try {
                if (mainActivityInterface.getStorageAccess().uriExists(originalUri)) {
                    mainActivityInterface.getStorageAccess().deleteFile(originalUri);
                }
            } catch (Exception e) {
                Log.d(TAG, "Error trying to delete originalUri");
            }
        } else {
            if (nearbyReturnActionsInterface != null) {
                mainActivityInterface.getSong().setCurrentSection(pendingCurrentSection);
                nearbyReturnActionsInterface.loadSong();
                // IV - End pending
                pendingCurrentSection = 0;
            }
        }
    }


    // TODO what happens if we are using nearby and presentation order - the section numbers won't match
    // Nearby should looks for matching section names

    // Getting a note of connected devices between host and client
    private void payloadEndpoints(String incomingEndpoints) {
        if (!isHost) {
            Log.d(TAG, "I am a client, but the host has sent me this info about connected endpoints: " + incomingEndpoints);
            // The host has sent a note of the connected endpoints in case we become the host
            String[] incomingEPs = incomingEndpoints.split(endpointTag);
            // TODO testing just now
            for (String incomingEP: incomingEPs) {
                Log.d(TAG,"incomingEP: "+incomingEP);
            }
            //connectedEndpoints.clear()
            //Collections.addAll(connectedEndpoints, incomingEPs);
        }
    }

    // Deal with turning off Nearby and cleaning up transferIds
    @Override
    public void turnOffNearby() {
        try {
            Log.d(TAG, "turnOffNearby()");
            Nearby.getConnectionsClient(c).stopAllEndpoints();
        } catch (Exception e) {
            Log.d(TAG, "Can't turn off nearby");
        }
        // IV - Sets isAdvertising = false;
        stopAdvertising();
        stopDiscoveryHandler.removeCallbacks(stopDiscoveryRunnable);
        // IV - Sets isDiscovering = false;
        stopDiscovery();
        isHost = false;
        isConnected = false;
        usingNearby = false;
        incomingPrevious = "";
        connectedEndpoints.clear();
    }
    public void cancelTransferIds() {
        // IV - Used to cancel earlier transfer Ids
        Log.d(TAG, "Cancel Ids " + payLoadTransferIds);
        if (!payLoadTransferIds.equals("")) {
            String [] Ids = payLoadTransferIds.trim().split(" ");
            payLoadTransferIds = "";
            for (String Id : Ids) {
                Nearby.getConnectionsClient(c).cancelPayload(Long.parseLong((Id.trim())));
            }
            incomingFilePayloads = new SimpleArrayMap<>();
            fileNewLocation = new SimpleArrayMap<>();
        }
    }


    // The getters for the variables used in the NearbyConnections object
    public boolean getNearbyHostMenuOnly() {
        return nearbyHostMenuOnly;
    }
    public boolean getReceiveHostFiles() {
        return receiveHostFiles;
    }
    public boolean getKeepHostFiles() {
        return keepHostFiles;
    }
    public boolean getReceiveHostAutoscroll() {
        return receiveHostAutoscroll;
    }
    public boolean getReceiveHostSongSections() {
        return receiveHostSongSections;
    }
    public String getReceivedSongFilename() {
        return receivedSongFilename;
    }
    public String getUserNickname() {
        String model = android.os.Build.MODEL.trim();
        // If the user has saved a value for their device name, use that instead
        // Don't need to save the device name unless the user edits it to make it custom
        return deviceId = mainActivityInterface.getPreferences().getMyPreferenceString("deviceId", model);
    }
    public int getHostSection() {
        return hostSection;
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

    // The setters
    public void setNearbyHostMenuOnly(boolean nearbyHostMenuOnly) {
        this.nearbyHostMenuOnly = nearbyHostMenuOnly;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyHostMenuOnly", nearbyHostMenuOnly);
    }
    public void setReceiveHostFiles(boolean receiveHostFiles) {
        this.receiveHostFiles = receiveHostFiles;
    }
    public void setKeepHostFiles(boolean keepHostFiles) {
        this.keepHostFiles = keepHostFiles;
    }
    public void setReceiveHostAutoscroll(boolean receiveHostAutoscroll) {
        this.receiveHostAutoscroll = receiveHostAutoscroll;
    }
    public void setReceiveHostSongSections(boolean receiveHostSongSections) {
        this.receiveHostSongSections = receiveHostSongSections;
    }
    public void setHostSection(int hostSection) {
        this.hostSection = hostSection;
    }


    // These functions deal with nearby navigations
    private int getNearbySection(String incoming) {
        if (incoming!=null && incoming.startsWith(sectionTag)) {
            incoming = incoming.replace(sectionTag,"");
            return Integer.parseInt(incoming);
        } else {
            return 0;
        }
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

}
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
import com.garethevans.church.opensongtablet.autoscroll.AutoscrollActions;
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
import java.util.Collections;
import java.util.Objects;

public class NearbyConnections implements NearbyInterface {

    private final String TAG = "NearbyConnections";
    private final ArrayList<String> connectedEndPoints;
    private final ArrayList<String> connectedEndPointsNames;
    private final ArrayList<String> connectedDeviceIds;
    // Handler for stop of discovery
    private final Handler stopDiscoveryHandler = new Handler();
    private final Runnable stopDiscoveryRunnable;
    private final SimpleArrayMap<Long, Payload> incomingFilePayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> fileNewLocation = new SimpleArrayMap<>();
    public String deviceId, connectionLog, incomingPrevious, connectionId, connectionEndPointName;
    public boolean isConnected, isHost, receiveHostFiles, keepHostFiles, usingNearby,
            isAdvertising = false, isDiscovering = false, nearbyHostMenuOnly;
    NearbyReturnActionsInterface nearbyReturnActionsInterface;
    AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
    DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
    // The stuff used for Google Nearby for connecting devices
    String serviceId = "com.garethevans.church.opensongtablet";
    private String receivedSongFilename;
    private MainActivityInterface mainActivityInterface;

    public NearbyConnections(Context c, MainActivityInterface mainActivityInterface) {
        connectedEndPoints = new ArrayList<>();
        connectedEndPointsNames = new ArrayList<>();
        connectedDeviceIds = new ArrayList<>();
        if (mainActivityInterface == null) {
            try {
                mainActivityInterface = (MainActivityInterface) c;
                nearbyHostMenuOnly = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "nearbyHostMenuOnly", false);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopDiscoveryRunnable = () -> stopDiscovery(c);
    }

    private void updateConnectionLog(String newMessage) {
        Log.d(TAG, "message=" + newMessage + "   mainActivityInterface=" + mainActivityInterface);
        if (newMessage != null && mainActivityInterface != null) {
            connectionLog += newMessage + "\n";
            try {
                mainActivityInterface.updateConnectionsLog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setMainActivityInterface(MainActivityInterface mainActivityInterface) {
        this.mainActivityInterface = mainActivityInterface;
    }

    public void setNearbyReturnActionsInterface(NearbyReturnActionsInterface nearbyReturnActionsInterface) {
        this.nearbyReturnActionsInterface = nearbyReturnActionsInterface;
    }

    // Start or stop the broadcadst/discovery
    @Override
    public void startAdvertising(Context c, MainActivityInterface mainActivityInterface) {
        if (!isAdvertising) {
            Log.d(TAG, "Nearby.getConnectionsClient(context)=" + Nearby.getConnectionsClient(c));
            Log.d(TAG, "startAdvertising()");
            Nearby.getConnectionsClient(c)
                    .startAdvertising(getUserNickname(c, mainActivityInterface), serviceId, connectionLifecycleCallback(c, mainActivityInterface), advertisingOptions)
                    .addOnSuccessListener(
                            (Void unused) -> {
                                // We're advertising!
                                updateConnectionLog(c.getString(R.string.connections_advertise) + " " + getUserNickname(c, mainActivityInterface));
                                Log.d(TAG, "startAdvertising() - success");
                                isAdvertising = true;
                            })
                    .addOnFailureListener(
                            (Exception e) -> {
                                // We were unable to start advertising.
                                updateConnectionLog(c.getString(R.string.connections_failure) + " " + getUserNickname(c, mainActivityInterface));
                                Log.d(TAG, "startAdvertising() - failure: " + e);
                            });
        } else {
            Log.d(TAG, "startAdvertising() - already advertising");
        }
    }

    @Override
    public void startDiscovery(Context c, MainActivityInterface mainActivityInterface) {
        Log.d(TAG, "startDiscovery()");
        if (!isDiscovering) {
            Nearby.getConnectionsClient(c)
                    .startDiscovery(serviceId, endpointDiscoveryCallback(c, mainActivityInterface), discoveryOptions)
                    .addOnSuccessListener(
                            (Void unused) -> {
                                // We're discovering!
                                updateConnectionLog(c.getString(R.string.connections_discover));
                                isDiscovering = true;
                                Log.d(TAG, "startDiscovery() - success");
                            })
                    .addOnFailureListener(
                            (Exception e) -> {
                                // We're unable to start discovering.
                                stopDiscovery(c);
                                Log.d(TAG, "startDiscovery() - failure: " + e);
                            });
        } else {
            Log.d(TAG, "startDiscovery() - already discovering");
        }
        // IV - Stop 30s after this (latest) call
        if (isDiscovering) {
            stopDiscoveryHandler.removeCallbacks(stopDiscoveryRunnable);
            stopDiscoveryHandler.postDelayed(stopDiscoveryRunnable, 30000);
        }
    }

    @Override
    public void stopAdvertising(Context c) {
        Log.d(TAG, "stopAdvertising()");
        if (isAdvertising) {
            isAdvertising = false;
            try {
                Nearby.getConnectionsClient(c).stopAdvertising();
                updateConnectionLog(c.getString(R.string.connections_service_stop));
                Log.d(TAG, "stopAdvertising() - success");
            } catch (Exception e) {
                Log.d(TAG, "stopAdvertising() - failure: " + e);
            }

        } else {
            Log.d(TAG, "stopAdvertising() - wasn't advertising");
        }
    }

    @Override
    public void stopDiscovery(Context c) {
        Log.d(TAG, "stopDiscovery()");
        if (isDiscovering) {
            try {
                Nearby.getConnectionsClient(c).stopDiscovery();
                updateConnectionLog(c.getString(R.string.connections_discover_stop));
            } catch (Exception e) {
                Log.d(TAG, "stopDiscovery() - failure: " + e);
            }
        } else {
            Log.d(TAG, "stopDiscovery() -  wasn't discovering");
        }
        isDiscovering = false;
    }

    private String getDeviceNameFromId(String endpointId) {
        // When requesting connections, the proper device name is stored in an arraylist like endpointId__deviceName
        for (String s : connectedEndPointsNames) {
            if (s.startsWith(endpointId + "__")) {
                return s.replace(endpointId + "__", "");
            }
        }
        return endpointId;
    }

    public String getUserNickname(Context c, MainActivityInterface mainActivityInterface) {
        String model = android.os.Build.MODEL.trim();
        // If the user has saved a value for their device name, use that instead
        // Don't need to save the device name unless the user edits it to make it custom
        return deviceId = mainActivityInterface.getPreferences().getMyPreferenceString(c, "deviceId", model);
    }

    public void setNearbyHostMenuOnly(Context c, MainActivityInterface mainActivityInterface, boolean nearbyHostMenuOnly) {
        this.nearbyHostMenuOnly = nearbyHostMenuOnly;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean(c, "nearbyHostMenuOnly", nearbyHostMenuOnly);
    }

    private String userConnectionInfo(String endpointId, ConnectionInfo connectionInfo) {
        Log.d(TAG, "endpointId=" + endpointId + "  name=" + connectionInfo.getEndpointName() + "  getAuthenticationToken=" + connectionInfo.getAuthenticationToken());
        return endpointId + "__" + connectionInfo.getEndpointName();
    }

    private void delayAcceptConnection(Context c, MainActivityInterface mainActivityInterface, String endpointId, ConnectionInfo connectionInfo) {
        // For stability add a small delay
        Handler waitAccept = new Handler();
        waitAccept.postDelayed(() -> {
            // Add a note of the nice name matching the endpointId
            String id = userConnectionInfo(endpointId, connectionInfo);
            Log.d(TAG, "looking for " + id);

            // Take a note of the nice name matching the endpointId to use on connection STATUS_OK
            connectionId = userConnectionInfo(endpointId, connectionInfo);
            connectionEndPointName = connectionInfo.getEndpointName();

            // The user confirmed, so we can accept the connection.
            Nearby.getConnectionsClient(c)
                    .acceptConnection(endpointId, payloadCallback(c, mainActivityInterface));
        }, 200);
    }

    private ConnectionLifecycleCallback connectionLifecycleCallback(Context c, MainActivityInterface mainActivityInterface) {
        return new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                // If the device was previously connected, try to reconnect silently
                if (connectedDeviceIds.contains(connectionInfo.getEndpointName())) {
                    delayAcceptConnection(c, mainActivityInterface, endpointId, connectionInfo);
                } else {
                    // Allow clients to connect to the host when the Connect menu is open, or the user switches off the requirement for the Connect menu to be open
                    if (mainActivityInterface.getFragmentOpen() == R.id.nearbyConnectionsFragment ||
                            (isHost && !mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "nearbyHostMenuOnly", false))) {
                        new AlertDialog.Builder(c)
                                .setTitle(c.getString(R.string.connections_accept) + " " + connectionInfo.getEndpointName())
                                .setMessage(c.getString(R.string.connections_accept_code) + " " + connectionInfo.getAuthenticationToken())
                                .setPositiveButton(
                                        c.getString(R.string.ok),
                                        (DialogInterface dialog, int which) -> delayAcceptConnection(c, mainActivityInterface, endpointId, connectionInfo))
                                .setNegativeButton(
                                        c.getString(R.string.cancel),
                                        (DialogInterface dialog, int which) ->
                                                // The user canceled, so we should reject the connection.
                                                Nearby.getConnectionsClient(c).rejectConnection(endpointId))
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    } else {
                        // The user is not accepting new connections, so we should reject the connection.
                        Nearby.getConnectionsClient(c).rejectConnection(endpointId);
                    }
                }
            }

            @Override
            public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution connectionResolution) {
                switch (connectionResolution.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                    case ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT:
                        // IV - Added handling of when already connected
                        // We're connected! Can now start sending and receiving data.
                        connectedEndPoints.add(endpointId);
                        isConnected = true;
                        updateConnectionLog(c.getString(R.string.connections_connected) + " " + connectionEndPointName);
                        if (!connectedEndPointsNames.contains(connectionId)) {
                            connectedEndPointsNames.add(connectionId);
                            Log.d(TAG, connectionId + " not found, adding");
                        }
                        if (!connectedDeviceIds.contains(connectionEndPointName)) {
                            connectedDeviceIds.add(connectionEndPointName);
                            Log.d(TAG, connectionEndPointName + " not found, adding");
                        }

                        if (isHost) {
                            // try to send the current song payload
                            sendSongPayload(c, mainActivityInterface);
                        } else {
                            // We can stop discovery now
                            stopDiscovery(c);
                        }
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        // The connection broke before it was able to be accepted.
                        // The connection was rejected by one or both sides.
                        updateConnectionLog(c.getString(R.string.connections_failure) + " " + getUserNickname(c, mainActivityInterface) + " <-> " + getDeviceNameFromId(endpointId));
                        break;
                    default:
                        // Unknown status code
                        Log.d(TAG, "Unknown status code");
                        break;
                }
            }

            @Override
            public void onDisconnected(@NonNull String endpointId) {
                isConnected = false;
                Log.d(TAG, "On disconnect");
                connectedEndPoints.remove(endpointId);
                String deviceName = getDeviceNameFromId(endpointId);
                connectedEndPointsNames.remove(endpointId + "__" + deviceName);
                updateConnectionLog(c.getString(R.string.connections_disconnect) + " " + deviceName);

                if (isHost) {
                    // Check if we have valid connections
                    isConnected = stillValidConnections();
                } else {
                    // Clients should try to connect again after 2 seconds
                    isConnected = false;
                    Handler h = new Handler();
                    h.postDelayed(() -> startDiscovery(c, mainActivityInterface), 2000);
                }
            }
        };
    }

    private boolean stillValidConnections() {
        try {
            if (connectedEndPoints.size() >= 1) {
                for (String s : connectedEndPoints) {
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

    private EndpointDiscoveryCallback endpointDiscoveryCallback(Context c, MainActivityInterface mainActivityInterface) {
        return new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                if (!findEndpoints(endpointId, discoveredEndpointInfo)) {
                    // Only attempt a connection if we aren't already connected
                    Nearby.getConnectionsClient(c)
                            .requestConnection(getUserNickname(c, mainActivityInterface), endpointId, connectionLifecycleCallback(c, mainActivityInterface))
                            .addOnSuccessListener(
                                    (Void unused) -> {
                                        Log.d(TAG, "Trying to connect to host: " + endpointId);
                                        // We successfully requested a connection. Now both sides
                                        // must accept before the connection is established.
                                        updateConnectionLog(c.getString(R.string.connections_searching));
                                    })
                            .addOnFailureListener(
                                    (Exception e) -> {
                                        // IV - Added handling of when already connected
                                        if (((ApiException) e).getStatusCode() == ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT) {
                                            isConnected = true;
                                            updateConnectionLog(c.getString(R.string.connections_connected) + " " + getDeviceNameFromId(endpointId));
                                            // IV - Already connected so replay last incoming song
                                            if (incomingPrevious != null && !incomingPrevious.equals("")) {
                                                String incoming = incomingPrevious;
                                                incomingPrevious = null;
                                                payloadOpenSong(c, mainActivityInterface, incoming);
                                            }
                                            // We can stop discovery now
                                            stopDiscovery(c);
                                        } else {
                                            // Nearby Connections failed to request the connection.
                                            updateConnectionLog(c.getString(R.string.connections_failure) + " " + getDeviceNameFromId(endpointId));
                                        }
                                        Log.d(TAG, "Connections failure: " + e);
                                    });
                }
            }

            @Override
            public void onEndpointLost(@NonNull String endpointId) {
                Log.d(TAG, "onEndPointlost");
                updateConnectionLog(c.getString(R.string.connections_disconnect) + " " + getDeviceNameFromId(endpointId));
                // Check if we have valid connections
                isConnected = stillValidConnections();
                // Try to connect again after 2 seconds
                if (!isHost) {
                    Handler h = new Handler();
                    h.postDelayed(() -> startDiscovery(c, mainActivityInterface), 2000);
                }
            }
        };
    }

    public void sendSongPayload(Context c, MainActivityInterface mainActivityInterface) {
        String infoPayload = null;
        Payload payloadFile = null;
        String infoFilePayload = mainActivityInterface.getSong().getFolder() + "_xx____xx_" + mainActivityInterface.getSong().getFilename() +
                "_xx____xx_" + mainActivityInterface.getSong().getNextDirection();

        if (mainActivityInterface.getSong().getFiletype().equals("XML")) {
            // By default, this should be smaller than 32kb, so probably going to send as bytes
            // We'll measure the actual size later to check though
            infoPayload = mainActivityInterface.getSong().getFolder() + "_xx____xx_" +
                    mainActivityInterface.getSong().getFilename() + "_xx____xx_" +
                    mainActivityInterface.getSong().getNextDirection() + "_xx____xx_" +
                    mainActivityInterface.getProcessSong().getXML(c, mainActivityInterface, mainActivityInterface.getSong());
        }

        if (infoPayload != null) {
            // Check the size.  If it is bigger than the 32kb (go 30kb to play safe!) allowed for bytes, switch to file
            byte[] mybytes = infoPayload.getBytes();
            if (mybytes.length > 30000) {
                infoPayload = null;
            }
        }

        if (infoPayload == null) {
            // We will send as a file
            try {
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Songs", mainActivityInterface.getSong().getFolder(), mainActivityInterface.getSong().getFilename());
                ParcelFileDescriptor parcelFileDescriptor = c.getContentResolver().openFileDescriptor(uri, "r");
                if (parcelFileDescriptor != null) {
                    payloadFile = Payload.fromFile(parcelFileDescriptor);
                    infoFilePayload = "FILE:" + payloadFile.getId() + ":" +
                            mainActivityInterface.getSong().getFolder() + "_xx____xx_" + mainActivityInterface.getSong().getFilename() +
                            "_xx____xx_" + mainActivityInterface.getSong().getNextDirection();
                }
            } catch (Exception e) {
                Log.d(TAG, "Error trying to send file: " + e);
                payloadFile = null;
            }
        }

        if (isHost) {
            for (String endpointId : connectedEndPoints) {
                if (payloadFile != null) {
                    // Send the file descriptor as bytes, then the file
                    Nearby.getConnectionsClient(c).sendPayload(endpointId, Payload.fromBytes(infoFilePayload.getBytes()));
                    Nearby.getConnectionsClient(c).sendPayload(endpointId, payloadFile);
                } else if (infoPayload != null) {
                    // Just send the bytes
                    Nearby.getConnectionsClient(c).sendPayload(endpointId, Payload.fromBytes(infoPayload.getBytes()));
                }
            }
        }
    }

    @Override
    public void doSendPayloadBytes(Context c, String infoPayload) {
        if (isHost) {
            for (String endpointId : connectedEndPoints) {
                Nearby.getConnectionsClient(c).sendPayload(endpointId, Payload.fromBytes(infoPayload.getBytes()));
            }
        }
    }

    private void payloadAutoscroll(AutoscrollActions autoScrollActions, String incoming) {
        // It sends autoscroll startstops as autoscroll_start or autoscroll_stop
        if (mainActivityInterface.getMode().equals("Performance")) {
            // Adjust only when not already in the correct state
            if (nearbyReturnActionsInterface != null && !(autoScrollActions.getIsAutoscrolling() == incoming.equals("autoscroll_start"))) {
                nearbyReturnActionsInterface.gesture5();
            }
        }
    }

    private void payloadSection(String incoming) {
        if (!mainActivityInterface.getMode().equals("Performance")) {
            int mysection = mainActivityInterface.getProcessSong().getNearbySection(incoming);
            if (mysection >= 0) {
                if (nearbyReturnActionsInterface != null) {
                    // Look for a section being sent
                    mainActivityInterface.getSong().setCurrentSection(mysection);
                    nearbyReturnActionsInterface.selectSection(mysection);
                }
            }
        }
    }

    private void payloadOpenSong(Context c, MainActivityInterface mainActivityInterface, String incoming) {
        // New method sends OpenSong songs in the format of
        //  FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>

        ArrayList<String> receivedBits = mainActivityInterface.getProcessSong().getNearbyIncoming(incoming);
        boolean incomingChange = (!incoming.equals(incomingPrevious));

        if (incomingChange) {
            incomingPrevious = incoming;
            Log.d(TAG, "payloadOpenSong");
            OutputStream outputStream;

            // Only songs sent via bytes payload trigger this.
            // Receiving an OpenSong file via bytes.  PDFs etc are sent separately
            boolean songReceived = (receivedBits.size() >= 4);

            Log.d(TAG, "isHost=" + isHost + "   isConnected=" + isConnected + "  usingNearby=" + usingNearby);
            Log.d(TAG, "receiveHostFiles=" + receiveHostFiles + "   keepHostFiles=" + keepHostFiles + "  songReceived=" + songReceived);

            if (!isHost && isConnected && songReceived && receiveHostFiles) {
                // We want to receive host files (we aren't the host either!) and an OpenSong song has been sent/received
                mainActivityInterface.getSong().setNextDirection(receivedBits.get(2));

                // If the user wants to keep the host file, we will save it to our storage.
                // If we already have it, it will overwrite it, if not, we add it
                Uri newLocation;
                if (keepHostFiles) {
                    // Prepare the output stream in the client Songs folder
                    // Check the folder exists, if not, create it
                    mainActivityInterface.getStorageAccess().createFile(c, mainActivityInterface, DocumentsContract.Document.MIME_TYPE_DIR, "Songs", receivedBits.get(0), "");
                    newLocation = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Songs", receivedBits.get(0), receivedBits.get(1));

                    // Create the file if it doesn't exist
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c, mainActivityInterface, newLocation, null, "Songs", receivedBits.get(0), receivedBits.get(1));
                    outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, newLocation);
                    mainActivityInterface.getSong().setFolder(receivedBits.get(0));
                    mainActivityInterface.getSong().setFilename(receivedBits.get(1));
                    // Add to the sqldatabase
                    mainActivityInterface.getSQLiteHelper().createSong(c, mainActivityInterface, mainActivityInterface.getSong().getFolder(), mainActivityInterface.getSong().getFilename());

                } else {
                    // Prepare the output stream in the Received folder - just keep a temporary version
                    newLocation = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Received", "", "ReceivedSong");
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c, mainActivityInterface, newLocation, null, "Received", "", "ReceivedSong");
                    outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, newLocation);
                    mainActivityInterface.getSong().setFolder("../Received");
                    mainActivityInterface.getSong().setFilename("ReceivedSong");
                }

                // Write the file to the desired output stream and load
                if (nearbyReturnActionsInterface != null) {
                    mainActivityInterface.getStorageAccess().writeFileFromString(receivedBits.get(3), outputStream);
                    nearbyReturnActionsInterface.prepareSongMenu();
                    nearbyReturnActionsInterface.loadSong();
                }


            } else if (!isHost && isConnected && songReceived) {
                // We just want to trigger loading the song on our device (if we have it).
                // If not, we get notified it doesn't exits
                mainActivityInterface.getSong().setFolder(receivedBits.get(0));
                mainActivityInterface.getSong().setFilename(receivedBits.get(1));
                Log.d(TAG, "received: " + mainActivityInterface.getSong().getFolder() + "/" + mainActivityInterface.getSong().getFilename());
                mainActivityInterface.getSong().setNextDirection(receivedBits.get(2));
                // Now load the song
                if (nearbyReturnActionsInterface != null) {
                    nearbyReturnActionsInterface.loadSong();
                }
            }

            // IV - Store the received song filename in case the user wants to duplicate the received song
            receivedSongFilename = receivedBits.get(1);

        } else {
            Log.d(TAG, "payloadOpenSong - no change as unchanged payload");
        }
    }

    public String getReceivedSongFilename() {
        return getReceivedSongFilename();
    }
    private void payloadFile(Context c, MainActivityInterface mainActivityInterface, Payload payload, String foldernamepair) {
        // If songs are too big, then we receive them as a file rather than bytes
        Log.d(TAG, "foldernamepair=" + foldernamepair);
        String[] bits = foldernamepair.split("_xx____xx_");
        String folder = bits[0];
        String filename = bits[1];
        mainActivityInterface.getSong().setNextDirection(bits[2]);
        boolean movepage = false;
        if ((folder.equals(mainActivityInterface.getSong().getFolder()) || mainActivityInterface.getSong().getFolder().equals("../Received"))
                && filename.equals(mainActivityInterface.getSong().getFilename()) && filename.toLowerCase(mainActivityInterface.getLocale()).endsWith(".pdf")) {
            // We are likely trying to move page to an already received file
            movepage = true;
        } else {
            mainActivityInterface.getSong().setPdfPageCurrent(0);
        }
        Uri newLocation = null;
        if (!isHost && isConnected && receiveHostFiles && keepHostFiles) {
            // The new file goes into our main Songs folder
            newLocation = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Songs", folder, filename);
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c, mainActivityInterface, newLocation, null, "Songs", folder, filename);
        } else if (!isHost && isConnected && receiveHostFiles) {
            // The new file goes into our Received folder
            folder = "../Received";
            newLocation = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Received", "", filename);
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c, mainActivityInterface, newLocation, null, "Received", "", filename);
        }
        mainActivityInterface.getSong().setFolder(folder);
        mainActivityInterface.getSong().setFilename(filename);
        Log.d(TAG, "newLocation=" + newLocation);
        if (movepage) {
            if (mainActivityInterface.getSong().getNextDirection().equals("L2R")) {
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
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, newLocation);
            if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                if (nearbyReturnActionsInterface != null) {
                    nearbyReturnActionsInterface.loadSong();
                }
            }
            Log.d(TAG, "originalUri=" + originalUri);
            mainActivityInterface.getStorageAccess().deleteFile(c, originalUri);
        } else {
            if (nearbyReturnActionsInterface != null) {
                nearbyReturnActionsInterface.loadSong();
            }
        }
    }

    private void payloadEndpoints(String incomingEndpoints) {
        if (!isHost) {
            Log.d(TAG, "I am a client, but the host has sent me this info about connected endpoints: " + incomingEndpoints);
            // The host has sent a note of the connected endpoints in case we become the host
            connectedEndPoints.clear();
            String[] incomingEPs = incomingEndpoints.split("_ep__ep_");
            Collections.addAll(connectedEndPoints, incomingEPs);
        }
    }

    private boolean findEndpoints(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
        Log.d("findEndPoints", "endpointId=" + endpointId);
        Log.d("findEndPoints", "discoveredEnpointInfo.getEndpointName()=" + discoveredEndpointInfo.getEndpointName());
        for (String s : connectedEndPoints) {
            Log.d("findEnpoints", "ArrayList connectedEndPoints:" + s);
        }
        for (String s : connectedEndPointsNames) {
            Log.d("findEnpoints", "ArrayList connectedEndPointsNames:" + s);
        }
        return connectedEndPointsNames.contains(discoveredEndpointInfo.getEndpointName());
    }

    private PayloadCallback payloadCallback(Context c, MainActivityInterface mainActivityInterface) {
        return new PayloadCallback() {
            @Override
            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                if (!isHost) {
                    // We can deal with the incoming payload!
                    if (payload.getType() == Payload.Type.FILE) {
                        // Make a note of it.  Nothing happens until complete
                        incomingFilePayloads.put(payload.getId(), payload);

                    } else if (payload.getType() == Payload.Type.BYTES) {
                        // We're dealing with bytes
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
                            payloadAutoscroll(mainActivityInterface.getAutoscrollActions(), incoming);
                        } else if (incoming != null && incoming.contains("___section___")) {
                            payloadSection(incoming);
                        } else if (incoming != null && incoming.contains("_ep__ep_")) {
                            payloadEndpoints(incoming);
                        } else if (incoming != null && incoming.contains("_xx____xx_")) {
                            payloadOpenSong(c, mainActivityInterface, incoming);
                        }
                    }
                    // not dealing with files as it is complex with scoped storage access
                    // also don't want user's download folder getting clogged!
                }
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                    // For bytes this is sent automatically, but it's the file we are interested in here
                    Payload payload;
                    if (incomingFilePayloads.containsKey(payloadTransferUpdate.getPayloadId())) {
                        payload = incomingFilePayloads.get(payloadTransferUpdate.getPayloadId());
                        String foldernamepair = fileNewLocation.get(payloadTransferUpdate.getPayloadId());
                        if (foldernamepair == null) {
                            foldernamepair = "../Received_xx____xx_ReceivedSong";
                        }
                        incomingFilePayloads.remove(payloadTransferUpdate.getPayloadId());
                        fileNewLocation.remove(payloadTransferUpdate.getPayloadId());

                        payloadFile(c, mainActivityInterface, payload, foldernamepair);
                    }
                }
            }
        };
    }


    @Override
    public void turnOffNearby(Context c) {
        try {
            Log.d(TAG, "turnOffNearby()");
            Nearby.getConnectionsClient(c).stopAllEndpoints();
        } catch (Exception e) {
            Log.d(TAG, "Can't turn off nearby");
        }
        // IV - Sets isAdvertising = false;
        stopAdvertising(c);
        stopDiscoveryHandler.removeCallbacks(stopDiscoveryRunnable);
        // IV - Sets isDiscovering = false;
        stopDiscovery(c);
        isHost = false;
        isConnected = false;
        usingNearby = false;
        incomingPrevious = "";
    }
}
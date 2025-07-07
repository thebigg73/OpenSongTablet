package com.garethevans.church.opensongtablet.nearby;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.garethevans.church.opensongtablet.MainActivity;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;

import java.util.ArrayList;

public class NearbySendPayloads {

    // This class deals with sending Payloads
    private final String TAG = "NearbySendPayloads";
    private final Activity activity;
    private final Context c;
    private final NearbyActions nearbyActions;
    private final MainActivityInterface mainActivityInterface;
    private boolean sendSongDelayActive = false;
    private int pendingSection = -1;
    private boolean syncAllowed;

    // We only send payloads if we haven't sent them already

    NearbySendPayloads(Activity activity, Context c, NearbyActions nearbyActions) {
        this.activity = activity;
        this.c = c;
        this.nearbyActions = nearbyActions;
        this.mainActivityInterface = (MainActivityInterface) c;
        getUpdatedPreferences();
    }

    // Get the preferences for sending payloads
    public void getUpdatedPreferences() {
        syncAllowed = mainActivityInterface.getPreferences().getMyPreferenceBoolean("syncAllowed",true);
    }

    // This deals with sending simple autoscrollStart, autoscrollStop, autoscrollPause, autoscrollIncrease,
    // autoscrollDecrease bytes commands as the host device (with checks)
    // Each simple command is converted to a nearbyJson to make it easy to process when received
    public void sendCommandIfHost(String simpleCommand) {
        if (nearbyActions.getNearbyConnectionManagement().getUsingNearby() &&
                nearbyActions.getNearbyConnectionManagement().getIsHost()) {
            NearbyJson nearbyJson = new NearbyJson();
            nearbyJson.setWhat(simpleCommand);
            sendToConnected(Payload.fromBytes(MainActivity.gson.toJson(nearbyJson).getBytes()));
        }
    }

    // Send payload with scroll information
    public void sendScrollByPayload(boolean scrollDown, float scrollProportion) {
        if (nearbyActions.getNearbyConnectionManagement().getIsHost()) {
            NearbyJson nearbyJson = new NearbyJson();
            nearbyJson.setWhat(nearbyActions.scrollByTag);
            if (scrollDown) {
                nearbyJson.setScrollProportion(scrollProportion);
            } else {
                nearbyJson.setScrollProportion(-scrollProportion);
            }
            sendToConnected(Payload.fromBytes(MainActivity.gson.toJson(nearbyJson).getBytes()));
        }
    }
    public void sendScrollToPayload(float scrollProportion) {
        if (nearbyActions.getNearbyConnectionManagement().getIsHost()) {
            NearbyJson nearbyJson = new NearbyJson();
            nearbyJson.setWhat(nearbyActions.scrollToTag);
            nearbyJson.setScrollProportion(scrollProportion);
            sendToConnected(Payload.fromBytes(MainActivity.gson.toJson(nearbyJson).getBytes()));
        }
    }

    // Send nearbyMessages
    public void sendMessage(int which) {
        NearbyJson nearbyJson = new NearbyJson();
        nearbyJson.setWhat(nearbyActions.messageTag);
        String message = nearbyActions.getNearbyMessages().getNearbyMessage(which);
        nearbyJson.setMessage(message);

        // Show the message on this screen
        if (nearbyActions.getNearbyReceivePayloads().getNearbyMessageSticky()) {
            mainActivityInterface.showNearbyAlertPopUp(message);
        } else {
            mainActivityInterface.getShowToast().doIt(message);
        }

        // Send as a payload
        sendToConnected(Payload.fromBytes(MainActivity.gson.toJson(nearbyJson).getBytes()));
    }

    // This deals with sending synchronisation requests and responses to a specific device
    public void sendSyncInfoRequest(String deviceToAction) {
        Log.d(TAG,"sendSyncInfoRequest("+deviceToAction+")");
        NearbyJson nearbyJson = new NearbyJson();
        nearbyJson.setWhat(nearbyActions.syncRequestInfo);
        nearbyJson.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
        nearbyJson.setDeviceToAction(deviceToAction);
        sendPayloadToSelected(deviceToAction, Payload.fromBytes(MainActivity.gson.toJson(nearbyJson).getBytes()));
    }
    public void sendSyncInfo(String requestingDevice) {
        // Get a note of our sync content
        // Go through our songs and create an array of objects

        NearbyJson nearbyJson = new NearbyJson();
        nearbyJson.setWhat(nearbyActions.syncReturnedInfo);
        nearbyJson.setFolder("../Received");
        nearbyJson.setFilename("nearbyShareableList.json");
        nearbyJson.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
        nearbyJson.setDeviceToAction(requestingDevice);

        // Add the songs
        nearbyJson.setShareableSongObjects(mainActivityInterface.getSQLiteHelper().getShareableSongs());

        // Now add the sets
        ArrayList<ShareableObject> shareableSetObjects = new ArrayList<>();
        ArrayList<String> sets = mainActivityInterface.getStorageAccess().listFilesInFolder("Sets", "");

        for (String set : sets) {
            ShareableObject shareableObject = new ShareableObject();
            shareableObject.setFilename(set);
            shareableObject.setFolder("../Sets");

            // Add object to the shareable sets
            shareableSetObjects.add(shareableObject);
        }
        nearbyJson.setShareableSetObjects(shareableSetObjects);

        // Now add our profiles
        ArrayList<ShareableObject> shareableProfileObjects = new ArrayList<>();
        ArrayList<String> profiles = mainActivityInterface.getStorageAccess().listFilesInFolder("Profiles", "");

        for (String profile : profiles) {
            ShareableObject shareableObject = new ShareableObject();
            shareableObject.setFilename(profile);
            shareableObject.setFolder("../Profiles");

            // Add object to the shareable sets
            shareableProfileObjects.add(shareableObject);
        }
        nearbyJson.setShareableProfileObjects(shareableProfileObjects);

        // Now create a json string object and save to a file in the Export folder
        String jsonString = MainActivity.gson.toJson(nearbyJson);
        Log.d(TAG, "jsonString.size():" + (jsonString.getBytes().length / 1000f) + "kb)");
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Export", "", nearbyActions.sharableObjectFile, jsonString);
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", nearbyActions.sharableObjectFile);
        ParcelFileDescriptor pfd;
        try {
            pfd = new ParcelFileDescriptor(c.getContentResolver().openFileDescriptor(uri, "r"));
            Payload payloadFile = Payload.fromFile(pfd);
            // Create a json for the info bytes
            NearbyJson nearbyJsonInfo = new NearbyJson();
            nearbyJsonInfo.setWhat(nearbyActions.syncReturnedInfo);
            nearbyJsonInfo.setId(payloadFile.getId());
            nearbyJsonInfo.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
            nearbyJsonInfo.setDeviceToAction(requestingDevice);
            nearbyJsonInfo.setFolder("../Received");
            nearbyJsonInfo.setFilename(nearbyActions.sharableObjectFile);

            // Send the file info
            sendPayloadToSelected(requestingDevice, Payload.fromBytes(MainActivity.gson.toJson(nearbyJson).getBytes()));
            // Send the actual json file
            sendPayloadToSelected(requestingDevice, payloadFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendSyncContent() {
       // TODO
    }
    public void sendSyncDenied() {
        // TODO
    }
    public void sendSyncProcessingInfo() {
        // TODO
    }
    public void sendSyncProcessingContent() {
        // TODO
    }
    public void sendPayloadToSelected(String whichDevice, Payload payload) {
        // Only send if we haven't already sent it
        String type="BYTES";
        if (payload.getType()==Payload.Type.FILE) {
            type = "FILE";
        }
        Log.d(TAG, "sendPayloadToSelected("+whichDevice+", "+payload+")");
        if (!nearbyActions.getNearbyTransferRecords().getAlreadySentPayload(payload)) {
            // Add a record of this and set it to delete after delay (to clear memory)
            nearbyActions.getNearbyTransferRecords().addAlreadySentPayload(payload);
            nearbyActions.getNearbyTransferRecords().removeAlreadySentPayload(payload.getId());
            Log.d(TAG, " - not sent, so sending");

            // Here we send a request only to the requested device
            for (int i=0; i<nearbyActions.getNearbyConnectionManagement().getConnectedDevices().size(); i++) {
                String id = nearbyActions.getNearbyConnectionManagement().getConnectedDevices().keyAt(i);
                String name = nearbyActions.getNearbyConnectionManagement().getConnectedDevices().valueAt(i);
                if (id.equals(whichDevice) || name.equals(whichDevice)) {
                    Log.d(TAG, "Sending payload:" + payload + "  (type:"+type+")  to device:" + id +" ("+name+")");
                    Nearby.getConnectionsClient(activity).sendPayload(id, payload);
                    break;
                }
            }
        } else {
            Log.d(TAG,"We've already sent the payload with id:"+payload.getId());
        }
    }

    // Song delay information
    public boolean getSendSongDelayActive() {
        return this.sendSongDelayActive;
    }
    public void setSendSongDelayActive(boolean value) {
        this.sendSongDelayActive = value;
    }

    // Deal with sending the current song
    public boolean sendSongPayload() {
        // HOST: Cancel previous song transfers - a new song is being sent
        //nearbyActions.getNearbyTransferRecords().cancelTransferIds();
        // New method sends bytes as a json
        // We will send the current section as a pending section change (encode as -ve offset by 1) for action on next song load on the client
        NearbyJson nearbyJsonToSend = new NearbyJson();
        if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
            nearbyJsonToSend.setWhat(nearbyActions.sectionTag + "-" + (1 + mainActivityInterface.getSong().getPdfPageCurrent()));
        } else {
            nearbyJsonToSend.setWhat(nearbyActions.sectionTag + "-" + (1 + mainActivityInterface.getSong().getCurrentSection()));
        }
        // Send the json to trigger the current song sections
        sendToConnected(Payload.fromBytes(MainActivity.gson.toJson(nearbyJsonToSend).getBytes()));

        boolean sendingFile = false;
        nearbyJsonToSend = new NearbyJson();
        nearbyJsonToSend.setWhat(nearbyActions.songTag);
        nearbyJsonToSend.setFolder(mainActivityInterface.getSong().getFolder());
        nearbyJsonToSend.setFilename(mainActivityInterface.getSong().getFilename());
        nearbyJsonToSend.setSwipeDirection(mainActivityInterface.getDisplayPrevNext().getSwipeDirection());
        nearbyJsonToSend.setKey(mainActivityInterface.getSong().getKey());
        nearbyJsonToSend.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
        String xml = mainActivityInterface.getProcessSong().getXML(mainActivityInterface.getSong());

        if (mainActivityInterface.getStorageAccess().isIMGorPDF(mainActivityInterface.getSong().getFilename())) {
            nearbyJsonToSend.setSong(mainActivityInterface.getSong());
            xml = null;
        }
        if (xml!=null && xml.getBytes().length < 30000 && mainActivityInterface.getSong().getFiletype().equals("XML") &&
                mainActivityInterface.getSong().getFilename() != null &&
                !mainActivityInterface.getStorageAccess().isIMGorPDF(mainActivityInterface.getSong())) {
            nearbyJsonToSend.setXml(mainActivityInterface.getProcessSong().getXML(mainActivityInterface.getSong()));
            sendToConnected(Payload.fromBytes(MainActivity.gson.toJson(nearbyJsonToSend).getBytes()));

        } else {
            // Prepare a payload file and get the id so we can prepare the receiving device
            nearbyJsonToSend.setWhat(nearbyActions.fileTag);
            nearbyJsonToSend.setXml(null);
            // We will send as a file
            Payload payloadFile = null;
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(
                    "Songs", mainActivityInterface.getSong().getFolder(),
                    mainActivityInterface.getSong().getFilename());

            try {
                ParcelFileDescriptor parcelFileDescriptor = c.getContentResolver().openFileDescriptor(uri, "r");
                if (parcelFileDescriptor != null) {
                    payloadFile = Payload.fromFile(parcelFileDescriptor);
                    nearbyJsonToSend.setId(payloadFile.getId());
                }
            } catch (Exception e) {
                Log.d(TAG, "Error trying to send file: " + e);
                payloadFile = null;
            }
            if (payloadFile != null) {
                // Send the file descriptor as bytes, then the file
                sendToConnected(Payload.fromBytes(MainActivity.gson.toJson(nearbyJsonToSend).getBytes()));
                sendToConnected(payloadFile);
                sendingFile = true;
            }
        }
        return sendingFile;
    }

    // Send the current section being viewed in the song
    public void sendSongSectionPayload() {
        if (nearbyActions.getNearbyConnectionManagement().sendAsHost()) {
            // IV - Send if we are not delaying - a delayed song send sends the current section
            if (!sendSongDelayActive) {
                String simpleCommand;
                if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                    simpleCommand = nearbyActions.sectionTag + (mainActivityInterface.getSong().getPdfPageCurrent());
                } else {
                    simpleCommand = nearbyActions.sectionTag + (mainActivityInterface.getSong().getCurrentSection());
                }
                sendCommandIfHost(simpleCommand);
            }
        }
    }






    // This is the logic that sends the required payload to all connected devices
    public void sendToConnected(Payload payload) {
        String type = "FILE";
        if (payload.getType()==Payload.Type.BYTES) {
            type = "BYTES";
        }
        // Only send if we haven't sent it already
        if (!nearbyActions.getNearbyTransferRecords().getAlreadySentPayload(payload)) {
            // Add a record of this and set it to delete after delay (to clear memory)
            nearbyActions.getNearbyTransferRecords().addAlreadySentPayload(payload);
            nearbyActions.getNearbyTransferRecords().removeAlreadySentPayload(payload.getId());
            for (int i=0; i<nearbyActions.getNearbyConnectionManagement().getConnectedDevices().size(); i++) {
                String id = nearbyActions.getNearbyConnectionManagement().getConnectedDevices().keyAt(i);
                String name = nearbyActions.getNearbyConnectionManagement().getConnectedDevices().valueAt(i);
                Nearby.getConnectionsClient(activity).sendPayload(id, payload);

                Log.d(TAG,"sending the payload with id:"+payload.getId()+" (type:"+type+") to device "+id+" ("+name+")");
            }
        } else {
            Log.d(TAG, "We've already sent the payload with id:"+payload.getId());
        }
    }






    // TODO get rid of or fix the functions below


    /*// Deal with sending payloads as a host for clients to listen for

    This used the interface: NearbySendPayloadInterface - we can probable remove this too
    @Override
    public void doSendPayloadBytes(String infoPayload, boolean clientSend) {
        Log.d(TAG, "doSendPayloadBytes("+infoPayload+"," + clientSend);
        if (nearbyActions.getNearbyConnectionManagement().sendAsHost() || clientSend) {
            sendToConnected(Payload.fromBytes(infoPayload.getBytes()));
        }
    }*/

    /*public void repeatPayload(Payload payload) {
        if (nearbyActions.getNearbyTransferRecords().getPreviousPayload()!=payload) {
            Log.d(TAG,"repeatPayload("+payload+")");
            if (nearbyActions.getNearbyConnectionManagement().getNearbyStrategy() == Strategy.P2P_CLUSTER) {
                sendToConnected(payload);
            }
        }
    }*/








    // This is for the host to send the requested file to the calling device
    /*public void hostSendFile(String requestPayload) {
        Log.d(TAG,"hostSendFile("+requestPayload+")");
        if (nearbyActions.getNearbyConnectionManagement().getIsHost()) {
            Log.d(TAG,"We are the host and have been asked for a file");
            // Break apart the requestPayload
            requestPayload = requestPayload.replace(nearbyActions.requestFileTag, "");
            String[] bits = requestPayload.split(nearbyActions.requestFileSeparator);
            // There should be 4 bits: calling deviceID, folder, subfolder, filename
            if (bits.length == 4) {
                Uri uri;
                if (bits[1].equals("Sets") && bits[2].equals("["+c.getString(R.string.set_current)+"]")) {
                    String currentSetXML = mainActivityInterface.getSetActions().createSetXML(mainActivityInterface.getCurrentSet());
                    uri = mainActivityInterface.getStorageAccess().getUriForItem("Export","","currentSet.xml");
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,uri,null,"Export","","currentSet.xml");
                    OutputStream currentSetOutputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                    mainActivityInterface.getStorageAccess().writeFileFromString(currentSetXML,currentSetOutputStream);
                }
                if (bits[1].equals("Songs") && bits[2].equals(mainActivityInterface.getMainfoldername())) {
                    bits[2] = "";
                }
                uri = mainActivityInterface.getStorageAccess().getUriForItem(
                        bits[1], bits[2], bits[3]);
                Payload payloadFile = null;
                String payloadInfo = "";
                try {
                    ParcelFileDescriptor parcelFileDescriptor = c.getContentResolver().openFileDescriptor(uri, "r");
                    if (parcelFileDescriptor != null) {
                        payloadFile = Payload.fromFile(parcelFileDescriptor);
                        payloadInfo = nearbyActions.requestFileTag + bits[0] +
                                nearbyActions.requestIdSeparator + payloadFile.getId() +
                                nearbyActions.requestFileSeparator + bits[1] +
                                nearbyActions.requestFileSeparator + bits[2] +
                                nearbyActions.requestFileSeparator + bits[3];
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error trying to send file: " + e);
                }
                if (payloadFile != null) {
                    // Send the info lead then file to the requesting device
                    for (String endpointString : nearbyActions.getNearbyConnectionManagement().getConnectedEndpoints()) {
                        String endpointId = nearbyActions.getNearbyConnectionManagement().getEndpointSplit(endpointString)[1];
                        if (endpointId.equals(bits[0])) {
                            // Get the endpointCode not the name
                            String endpointCode = nearbyActions.getNearbyConnectionManagement().getEndpointSplit(endpointString)[0];
                            Payload tempPayLoadBytes = Payload.fromBytes(payloadInfo.getBytes());

                            // We will now try to send the requested file to the requesting client
                            Nearby.getConnectionsClient(activity).sendPayload(endpointCode, tempPayLoadBytes);
                            Nearby.getConnectionsClient(activity).sendPayload(endpointCode, payloadFile);

                            Log.d(TAG,"We send a header as bytes with id:"+tempPayLoadBytes.getId()+" and the content:"+payloadInfo);
                            Log.d(TAG,"We send the file with id:"+payloadFile.getId());
                        }
                    }
                }
            }
        }
    }*/





    //

    // The song delay active
    /**/

}

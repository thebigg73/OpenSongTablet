package com.garethevans.church.opensongtablet.nearby;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.garethevans.church.opensongtablet.MainActivity;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;

import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

public class NearbySendPayloads {

    // This class deals with sending Payloads
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "NearbySendPayloads";
    private final Activity activity;
    private final Context c;
    private final NearbyActions nearbyActions;
    private final MainActivityInterface mainActivityInterface;
    private boolean sendSongDelayActive = false;

    NearbySendPayloads(Activity activity, Context c, NearbyActions nearbyActions) {
        this.activity = activity;
        this.c = c;
        this.nearbyActions = nearbyActions;
        this.mainActivityInterface = (MainActivityInterface) c;
    }

    /* Firstly these actions are our device sending commands (normally as a host) for another device to action
       This deals with sending simple autoscrollStart, autoscrollStop, autoscrollPause, autoscrollIncrease,
       autoscrollDecrease bytes commands as the host device (with checks)
       Each simple command is converted to a nearbyJson to make it easy to process when received */
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
    public void sendWebServerMessage(int which) {
        NearbyJson nearbyJson = new NearbyJson();
        nearbyJson.setWhat(nearbyActions.messageTag);
        String message = mainActivityInterface.getWebServer().getWebServerMessage(which);
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

    /* This deals with sending synchronisation requests and responses to a specific device
       These are our device requesting information from another device */
    // This device is requesting file information from a connected device
    public void sendSyncInfoRequest(String deviceToAction) {
        NearbyJson nearbyJson = new NearbyJson();
        nearbyJson.setWhat(nearbyActions.syncRequestInfo);
        nearbyJson.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
        nearbyJson.setDeviceToAction(deviceToAction);
        sendPayloadToSelected(deviceToAction, Payload.fromBytes(MainActivity.gson.toJson(nearbyJson).getBytes()));
    }
    public void sendSyncContentRequest(String deviceToAction, String filename, NearbyJson nearbyJson) {
        // We have chosen which files we want and have them bundled in the json
        // Now create a json string object and save to a file in the Export folder after clearing it
        // Empty the export folder
        mainActivityInterface.getStorageAccess().wipeFolder("Export","");
        String jsonString = MainActivity.gson.toJson(nearbyJson);
        mainActivityInterface.getStorageAccess().writeFileFromString("Export", "", filename, jsonString);
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", filename);
        ParcelFileDescriptor pfd;
        try {
            pfd = new ParcelFileDescriptor(c.getContentResolver().openFileDescriptor(uri, "r"));
            Payload payloadFile = Payload.fromFile(pfd);
            // Create a json for the info bytes
            NearbyJson nearbyJsonInfo = new NearbyJson();
            nearbyJsonInfo.setWhat(nearbyActions.syncRequestContent);
            nearbyJsonInfo.setId(payloadFile.getId());
            nearbyJsonInfo.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
            nearbyJsonInfo.setDeviceToAction(deviceToAction);
            nearbyJsonInfo.setFolder("../Received");
            nearbyJsonInfo.setFilename(filename);

            // Send the file info
            sendPayloadToSelected(deviceToAction, Payload.fromBytes(MainActivity.gson.toJson(nearbyJsonInfo).getBytes()));
            // Send the actual json file
            sendPayloadToSelected(deviceToAction, payloadFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // These are our device returning information to a connected device requesting stuff
    // This device is returning syncable information to a connected device who has requested it as a json
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

        // If our current set isn't empty, add a reference
        if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
            ShareableObject currentSet = new ShareableObject();
            // The current set isn't a file, so just make it easy to identify!
            currentSet.setFilename(nearbyActions.currentSetFile);
            currentSet.setFolder(nearbyActions.currentSetFile);
            currentSet.setTitle(c.getString(R.string.set_current));
            shareableSetObjects.add(currentSet);
        }

        // Now get any saved set files
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
        // Empty the export folder
        mainActivityInterface.getStorageAccess().wipeFolder("Export","");
        String jsonString = MainActivity.gson.toJson(nearbyJson);
        mainActivityInterface.getStorageAccess().writeFileFromString("Export", "", nearbyActions.sharableObjectFile, jsonString);
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
    // This device is asking a connected device to send a zip file back to us by sending a json of selected items back to them
    public void sendSyncDenied(String requestingDevice) {
        NearbyJson nearbyJson = new NearbyJson();
        nearbyJson.setWhat(nearbyActions.syncRequestDenied);
        nearbyJson.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
        nearbyJson.setDeviceToAction(requestingDevice);
        sendPayloadToSelected(requestingDevice, Payload.fromBytes(MainActivity.gson.toJson(nearbyJson).getBytes()));
    }
    public void sendSyncProcessingInfo(String requestingDevice) {
        NearbyJson nearbyJson = new NearbyJson();
        if (nearbyActions.getNearbyConnectionManagement().getNearbyFileSharing()) {
            nearbyJson.setWhat(nearbyActions.syncProcessingInfo);
            nearbyJson.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
            nearbyJson.setDeviceToAction(requestingDevice);
            nearbyActions.getNearbySendPayloads().sendPayloadToSelected(nearbyJson.getDeviceToAction(),
                    Payload.fromBytes(MainActivity.gson.toJson(nearbyJson).getBytes()));
            // Now, we can deal with the actual task and send that when ready
            nearbyActions.getNearbySendPayloads().sendSyncInfo(nearbyJson.getDeviceToAction());
        } else {
            // Let them know the bad news...
            sendSyncDenied(requestingDevice);
        }
    }
    public void sendSyncContent(NearbyJson nearbyRequestJson, String filename) {
        if (nearbyRequestJson!=null) {
            // A connected device has chosen some of our files
            // We need to package them up in a zip file and send them over
            // Empty the export folder
            mainActivityInterface.getStorageAccess().wipeFolder("Export","");
            mainActivityInterface.getStorageAccess().makeSureFileIsRegistered("Export","",filename,true);
            Uri shareZip = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", filename);
            ZipOutputStream zipOutputStream = null;
            try {
                zipOutputStream = new ZipOutputStream(mainActivityInterface.getStorageAccess().getOutputStream(shareZip));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Now go through each item requested in turn and add it
            if (nearbyRequestJson.getShareableSetObjects() != null && !nearbyRequestJson.getShareableSetObjects().isEmpty()) {
                for (ShareableObject shareableObject : nearbyRequestJson.getShareableSetObjects()) {
                    if ((shareableObject.getFilename()).equals(nearbyActions.currentSetFile)) {
                        // Make a json file for the current set
                        mainActivityInterface.getSetActions().setUseThisLastModifiedDate(mainActivityInterface.getTimeTools().getNowIsoTime());
                        String currentSetXML = mainActivityInterface.getSetActions().createSetXML(mainActivityInterface.getCurrentSet());
                        mainActivityInterface.getSetActions().setUseThisLastModifiedDate(null);

                        Uri currentSetUri = mainActivityInterface.getStorageAccess().getUriForItem("Export", "", nearbyActions.currentSetFile);
                        // Empty the export folder
                        mainActivityInterface.getStorageAccess().wipeFolder("Export","");
                        mainActivityInterface.getStorageAccess().writeFileFromString("Export", "", nearbyActions.currentSetFile, currentSetXML);
                        mainActivityInterface.getStorageAccess().addItemToZip(zipOutputStream, "Export", "", nearbyActions.currentSetFile);

                    } else {
                        mainActivityInterface.getStorageAccess().addItemToZip(zipOutputStream, "Sets", "", shareableObject.getFilename());
                    }
                }
            } else if (nearbyRequestJson.getShareableProfileObjects() != null && !nearbyRequestJson.getShareableProfileObjects().isEmpty()) {
                for (ShareableObject shareableObject : nearbyRequestJson.getShareableProfileObjects()) {
                    mainActivityInterface.getStorageAccess().addItemToZip(zipOutputStream, "Profiles", "", shareableObject.getFilename());
                }
            } else if (nearbyRequestJson.getShareableSongObjects() != null && !nearbyRequestJson.getShareableSongObjects().isEmpty()) {
                for (ShareableObject shareableObject : nearbyRequestJson.getShareableSongObjects()) {
                    mainActivityInterface.getStorageAccess().addItemToZip(zipOutputStream, "Songs", shareableObject.getFolder(), shareableObject.getFilename());
                    // If this is an image or PDF, we need to attach a song object as a json file
                    if (mainActivityInterface.getStorageAccess().isIMGorPDF(shareableObject.getFilename())) {
                        Song thisNonOSSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(shareableObject.getFolder(),shareableObject.getFilename());
                        String newFilename = thisNonOSSong.getFolder().replace("/","_____")+"_____"+thisNonOSSong.getFilename()+".json";
                        String songJson = MainActivity.gson.toJson(thisNonOSSong);
                        // Create a file for this content
                        // Empty the export folder
                        mainActivityInterface.getStorageAccess().wipeFolder("Export","");
                        Uri thisNonOSSongUri = mainActivityInterface.getStorageAccess().getUriForItem("Export","", newFilename);
                        mainActivityInterface.getStorageAccess().writeFileFromString("Export","",newFilename,songJson);
                        mainActivityInterface.getStorageAccess().addItemToZip(zipOutputStream,"Export","",newFilename);
                    }
                }
            }

            // Now close the zip file outputstream
            try {
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Now create the payload file so we can get the id
            ParcelFileDescriptor pfd;
            try {
                pfd = new ParcelFileDescriptor(c.getContentResolver().openFileDescriptor(shareZip, "r"));
                Payload payloadFile = Payload.fromFile(pfd);

                // Now prepare the file info using the payload file id and send that
                NearbyJson nearbyContentJson = new NearbyJson();
                nearbyContentJson.setId(payloadFile.getId());
                nearbyContentJson.setWhat(nearbyActions.syncReturnedContent);
                nearbyContentJson.setFolder("../Received");
                nearbyContentJson.setFilename(filename);
                nearbyContentJson.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
                nearbyContentJson.setDeviceToAction(nearbyRequestJson.getDeviceSending());

                // Send the info bytes so we can prepare the user with the filename
                sendPayloadToSelected(nearbyRequestJson.getDeviceSending(), Payload.fromBytes(MainActivity.gson.toJson(nearbyContentJson).getBytes()));

                // Send the actual file
                sendPayloadToSelected(nearbyRequestJson.getDeviceSending(), payloadFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void sendPayloadToSelected(String whichDevice, Payload payload) {
        // Only send if we haven't already sent it
        if (nearbyActions.getNearbyTransferRecords().getNotAlreadySentPayload(payload)) {
            // Add a record of this and set it to delete after delay (to clear memory)
            nearbyActions.getNearbyTransferRecords().addAlreadySentPayload(payload);
            nearbyActions.getNearbyTransferRecords().removeAlreadySentPayload(payload.getId());

            // Here we send a request only to the requested device
            for (int i=0; i<nearbyActions.getNearbyConnectionManagement().getConnectedDevices().size(); i++) {
                String id = nearbyActions.getNearbyConnectionManagement().getConnectedDevices().keyAt(i);
                String name = nearbyActions.getNearbyConnectionManagement().getConnectedDevices().valueAt(i);
                if (id.equals(whichDevice) || name.equals(whichDevice)) {
                    Nearby.getConnectionsClient(activity).sendPayload(id, payload);
                    break;
                }
            }
        }
    }

    // Song delay information
    public void setSendSongDelayActive(boolean value) {
        this.sendSongDelayActive = value;
    }

    // Deal with sending the current song
    public boolean sendSongPayload() {
        setSendSongDelayActive(false);
        // HOST: Cancel previous song transfers - a new song is being sent
        // nearbyActions.getNearbyTransferRecords().cancelTransferIds();
        // New method sends bytes as a json
        NearbyJson nearbyJsonToSend = new NearbyJson();
        boolean sendingFile = false;
        nearbyJsonToSend.setWhat(nearbyActions.songTag);

        // Add the current section we are using
        addSongSection(nearbyJsonToSend, mainActivityInterface.getSong());

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
            // Simply send the song as BYTES
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


    private void addSongSection(NearbyJson nearbyJson, Song song) {
        if (song.getFiletype().equals("PDF") || mainActivityInterface.getStorageAccess().isSpecificFileExtension("PDF",song.getFilename())) {
            nearbyJson.setSection(song.getPdfPageCurrent());
        } else {
            nearbyJson.setSection(song.getCurrentSection());
        }
        nearbyActions.getNearbyReceivePayloads().setPendingSection(nearbyJson.getSection()==null ? 0:nearbyJson.getSection());
    }

    // Send the current section being viewed in the song
    public void sendSongSectionPayload() {
        if (nearbyActions.getNearbyConnectionManagement().sendAsHost()) {
            NearbyJson nearbyJson = new NearbyJson();
            nearbyJson.setWhat(nearbyActions.sectionTag);
            nearbyJson.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
            addSongSection(nearbyJson, mainActivityInterface.getSong());
            if (!sendSongDelayActive) {
                sendToConnected(Payload.fromBytes(MainActivity.gson.toJson(nearbyJson).getBytes()));
            } else {
                sendSongDelayActive = false;
            }
        }
    }

    // This is the logic that sends the required payload to all connected devices
    public void sendToConnected(Payload payload) {
        // Only send if we haven't sent it already
        if (nearbyActions.getNearbyTransferRecords().getNotAlreadySentPayload(payload)) {
            // Add a record of this and set it to delete after delay (to clear memory)
            nearbyActions.getNearbyTransferRecords().addAlreadySentPayload(payload);
            nearbyActions.getNearbyTransferRecords().removeAlreadySentPayload(payload.getId());
            for (int i=0; i<nearbyActions.getNearbyConnectionManagement().getConnectedDevices().size(); i++) {
                String id = nearbyActions.getNearbyConnectionManagement().getConnectedDevices().keyAt(i);
                Nearby.getConnectionsClient(activity).sendPayload(id, payload);
            }
        }
    }

}

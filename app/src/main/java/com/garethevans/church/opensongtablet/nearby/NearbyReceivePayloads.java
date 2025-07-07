package com.garethevans.church.opensongtablet.nearby;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.MainActivity;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.NearbyReturnActionsInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class NearbyReceivePayloads {

    // This class deals with receiving payloads from Nearby devices
    // Payloads are sent as NearbyJson bytes object
    // If they are bigger than 32kb, then we get a simplified version with file info, then the file

    // We only deal with received payloads if we haven't already received them and if we didn't send them


    private final String TAG = "NearbyReceivePayloads";
    private final Activity activity;
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final NearbyActions nearbyActions;

    private String incomingPrevious;                // The last song received
    private boolean nearbyReceiveHostFiles;
    private boolean nearbyReceiveHostAutoscroll;
    private boolean nearbyReceiveHostScroll;
    private boolean nearbyKeepHostFiles;
    private boolean nearbyMatchToPDFSong;
    private boolean nearbyMessageSticky;
    private boolean nearbyReceiveHostSongSections;
    private Payload requestedFilePayload;
    private boolean forceReload = false;
    private String receivedSongFilename;
    private int pendingSection = 0;
    private boolean waitingForSyncFile = false;

    private final NearbyReturnActionsInterface nearbyReturnActionsInterface;


    NearbyReceivePayloads(Activity activity, Context c, NearbyActions nearbyActions) {
        this.activity = activity;
        this.c = c;
        this.mainActivityInterface = (MainActivityInterface) c;
        this.nearbyReturnActionsInterface = (NearbyReturnActionsInterface) c;
        this.nearbyActions = nearbyActions;
        getUpdatedPreferences();
    }

    // Get the preferences for receiving payloads
    public void getUpdatedPreferences() {
        nearbyReceiveHostAutoscroll = mainActivityInterface.getPreferences().getMyPreferenceBoolean("receiveHostAutoscroll", true);
        nearbyReceiveHostFiles = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyReceiveHostFiles", true);
        nearbyReceiveHostScroll = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyReceiveHostScroll", true);
        nearbyKeepHostFiles = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyKeepHostFiles", false);
        nearbyMatchToPDFSong = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyMatchToPDFSong", false);
        nearbyMessageSticky = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyMessageSticky", false);
        nearbyReceiveHostSongSections = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyReceiveHostSongSections", true);
    }


    // This is triggered when we receive a payload.
    // We then send it off for processing
    public PayloadCallback payloadCallback() {
        return new PayloadCallback() {
            @Override
            public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                // If this is bytes, we can deal with it now
                if (payload.getType() == Payload.Type.BYTES) {
                    Log.d(TAG,"bytes:  onPayloadReceived("+endpointId+", BYTES, "+payload+")");
                    processPayload(endpointId, payload);
                } else {
                    // Keep a record of this for when it is complete
                    Log.d(TAG,"file:  onPayloadReceived("+endpointId+", FILE, "+payload+")");
                    nearbyActions.getNearbyTransferRecords().addAlreadyReceivedPayload(payload);
                }
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                // Now we deal with the payload if the transfer was successful and the payload wasn't bytes (i.e. it was stored)
                if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS &&
                        nearbyActions.getNearbyTransferRecords().getAlreadyReceivedFilePayload(payloadTransferUpdate.getPayloadId())) {
                    // Send the information to the processing function
                    Log.d(TAG,"file:  onPayloadTransferUpdate("+endpointId+")");
                    processPayload(endpointId, nearbyActions.getNearbyTransferRecords().getAlreadyReceivedPayload(payloadTransferUpdate.getPayloadId()));
                }
            }
        };
    }


    private void processPayload(String endpointId, Payload payloadReceived) {
        // Only process this if we haven't already received this payload
        String type = "FILE";
        if (payloadReceived.getType()==Payload.Type.BYTES) {
            type = "BYTES";
        }
        Log.d(TAG,"processPayload("+endpointId+", "+type+", "+payloadReceived+")");
        if (type.equals("FILE") || !nearbyActions.getNearbyTransferRecords().getAlreadyReceivedPayload(payloadReceived)) {
            // If we are a host, but passing info through, or a client on cluster mode.  Resend this if we haven't already - check
            if ((nearbyActions.getNearbyConnectionManagement().getIsHost() &&
                    nearbyActions.getNearbyConnectionManagement().getNearbyHostPassthrough()) ||
                    nearbyActions.getNearbyConnectionManagement().getNearbyStrategy() == Strategy.P2P_CLUSTER) {
                if (!nearbyActions.getNearbyTransferRecords().getAlreadySentPayload(payloadReceived)) {
                    // Add a record and resend, then prepare to remove the record after a delay
                    nearbyActions.getNearbyTransferRecords().addAlreadySentPayload(payloadReceived);
                    nearbyActions.getNearbyTransferRecords().removeAlreadySentPayload(payloadReceived.getId());
                    nearbyActions.getNearbySendPayloads().sendToConnected(payloadReceived);
                }
            }

            // Now add a record that we have received this and remove the record after a delay
            nearbyActions.getNearbyTransferRecords().addAlreadyReceivedPayload(payloadReceived);
            nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(payloadReceived.getId());

            // If we have received bytes, we process that by getting the string message
            if (payloadReceived.getType() == Payload.Type.BYTES) {
                String json = new String(payloadReceived.asBytes(), StandardCharsets.UTF_8);
                if (json.contains("\"what\":")) {
                    NearbyJson nearbyJson = null;
                    try {
                        nearbyJson = MainActivity.gson.fromJson(json, NearbyJson.class);
                        if (nearbyJson.getId() == null) {
                            nearbyJson.setId(payloadReceived.getId());
                        }
                        // Only deal with this if this is for our device to action (or isn't set - for all)
                        if (nearbyJson.getDeviceToAction() == null ||
                                nearbyJson.getDeviceToAction().equals(nearbyActions.getNearbyConnectionManagement().getDeviceId())) {
                            String what = nearbyJson.getWhat();
                            Log.d(TAG, "received what:" + what);
                            // Deal with payload bytes messages
                            // (some only actioned if we aren't the host or we specified are asked to process them)
                            if (what != null && (!nearbyActions.getNearbyConnectionManagement().getIsHost() ||
                                    (nearbyJson.getDeviceToAction() != null) && nearbyJson.getDeviceToAction().equals(
                                            nearbyActions.getNearbyConnectionManagement().getDeviceId()))) {
                                if (what.equals(nearbyActions.autoscrollStart)) {
                                    autoscrollStart();
                                } else if (what.equals(nearbyActions.autoscrollStop)) {
                                    autoscrollStop();
                                } else if (what.equals(nearbyActions.autoscrollPause)) {
                                    autoscrollPause();
                                } else if (what.equals(nearbyActions.autoscrollIncrease)) {
                                    autoscrollIncrease();
                                } else if (what.equals(nearbyActions.autoscrollDecrease)) {
                                    autoscrollDecrease();
                                } else if (what.equals(nearbyActions.scrollByTag)) {
                                    scrollByProportion(nearbyJson);
                                } else if (what.equals(nearbyActions.scrollToTag)) {
                                    scrollToProportion(nearbyJson);
                                } else if (what.equals(nearbyActions.messageTag)) {
                                    messageDisplay(nearbyJson);
                                } else if (what.equals(nearbyActions.sectionTag)) {
                                    selectSection(nearbyJson);
                                } else if (what.equals(nearbyActions.songTag)) {
                                    if (!nearbyReceiveHostFiles) {
                                        // Use our song library and use the information sent
                                        loadSongFromMyLibrary(nearbyJson);
                                    } else {
                                        // Use the song XML received here (or get the info and wait for the file)
                                        loadSongFromReceivedXML(nearbyJson);
                                    }
                                } else if (what.equals(nearbyActions.fileTag)) {
                                    // This has the info needed for the file
                                    Log.d(TAG,"we have the information to receive the file");
                                    nearbyActions.getNearbyTransferRecords().addAlreadyReceivedFileInformation(nearbyJson);
                                    // If we are receiving the host files, we need to wait for the file as well
                                    // Because the file might arrive before the bytes, check and process if needed
                                    if (nearbyReceiveHostFiles) {
                                        checkForFileReceived(nearbyJson.getId());
                                    } else {
                                        // We can just load our version of the song
                                        Log.d(TAG,"just load our version - "+nearbyJson.getFilename());
                                        loadSongFromMyLibrary(nearbyJson);
                                    }
                                } else if (what.equals(nearbyActions.syncRequestInfo)) {
                                    Log.d(TAG, "payloadReceived.getId():" + payloadReceived.getId());
                                    dealWithSyncRequestInfo(nearbyJson);
                                } else if (what.equals(nearbyActions.syncProcessingInfo)) {
                                    dealWithSyncRequestProcessingInfo(nearbyJson);
                                } else if (what.equals(nearbyActions.syncRequestDenied)) {
                                    dealWithSyncRequestDenied();
                                } else if (what.equals(nearbyActions.syncReturnedInfo)) {
                                    Log.d(TAG,"Sync returned info...");
                                    Log.d(TAG,"id:"+nearbyJson.getId()+"  folder:"+nearbyJson.getId()+"  filename:"+nearbyJson.getFilename());
                                    nearbyActions.getNearbyTransferRecords().addAlreadyReceivedFileInformation(nearbyJson);
                                    checkForFileReceived(nearbyJson.getId());
                                }
                            }
                        }
                    } catch (Exception e) {
                        // TODO - tell the user that the sender is using an older version of the app and needs to update it!
                        mainActivityInterface.getShowToast().doIt(c.getString(R.string.connection_host_needs_update));
                    }
                }

                // Bytes are dealt with.  Keep a note that we've dealt with it
                nearbyActions.getNearbyTransferRecords().addIncomingDealtWith(payloadReceived.getId(),true);


                // Deal with payload files
            } else if (payloadReceived.getType() == Payload.Type.FILE) {
                // Check if the FILE and BYTES info have both arrived.  If so, process the file
                Log.d(TAG, "checkForFileReceived(" + payloadReceived.getId() + ")");
                checkForFileReceived(payloadReceived.getId());
            }
        }
    }


    // Autoscroll actions
    private void autoscrollStart() {
        if (nearbyReceiveHostAutoscroll && !nearbyActions.getNearbyConnectionManagement().getIsHost() &&
                !mainActivityInterface.getAutoscroll().getIsAutoscrolling()) {
            mainActivityInterface.getAutoscroll().startAutoscroll();
        }
    }
    private void autoscrollStop() {
        if (nearbyReceiveHostAutoscroll && !nearbyActions.getNearbyConnectionManagement().getIsHost() &&
                mainActivityInterface.getAutoscroll().getIsAutoscrolling()) {
            mainActivityInterface.getAutoscroll().stopAutoscroll();
        }
    }
    private void autoscrollPause() {
        if (nearbyReceiveHostAutoscroll && !nearbyActions.getNearbyConnectionManagement().getIsHost()) {
            mainActivityInterface.getAutoscroll().pauseAutoscroll();
        }
    }
    private void autoscrollIncrease() {
        if (nearbyReceiveHostAutoscroll && !nearbyActions.getNearbyConnectionManagement().getIsHost()) {
            mainActivityInterface.getAutoscroll().speedUpAutoscroll();
        }
    }
    private void autoscrollDecrease() {
        if (nearbyReceiveHostAutoscroll && !nearbyActions.getNearbyConnectionManagement().getIsHost()) {
            mainActivityInterface.getAutoscroll().slowDownAutoscroll();
        }
    }


    // Scroll to actions
    private void scrollByProportion(NearbyJson nearbyJson) {
        if (nearbyReceiveHostScroll && mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                !nearbyActions.getNearbyConnectionManagement().getIsHost() &&
                nearbyReturnActionsInterface != null && nearbyJson.getScrollProportion()!=null) {
            nearbyReturnActionsInterface.doScrollByProportion(nearbyJson.getScrollProportion());
        }
    }
    private void scrollToProportion(NearbyJson nearbyJson) {
        // It sends the scrollProportion as a ratio of scrollAmount/songHeight
        if (nearbyReceiveHostScroll && mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                !nearbyActions.getNearbyConnectionManagement().getIsHost() &&
                nearbyReturnActionsInterface != null && nearbyJson.getScrollProportion()!=null) {
            nearbyReturnActionsInterface.doScrollToProportion(nearbyJson.getScrollProportion());
        }
    }


    // A nearby message has been received
    private void messageDisplay(NearbyJson nearbyJson) {
        // Show an alert to the client
        if (!mainActivityInterface.getMode().equals("Presenter") && nearbyMessageSticky) {
            // Show a sticky note alert
            mainActivityInterface.showNearbyAlertPopUp(nearbyJson.getMessage());
        } else {
            // Show a toast message
            mainActivityInterface.getShowToast().doIt(nearbyJson.getMessage());
        }
    }


    // Change sections
    private void selectSection(NearbyJson nearbyJson) {
        if (!nearbyActions.getNearbyConnectionManagement().getIsHost() && nearbyJson.getSection() != null) {
            boolean onSectionAlready;
            int totalSections;
            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                onSectionAlready = mainActivityInterface.getSong().getPdfPageCurrent() == nearbyJson.getSection();
                totalSections = mainActivityInterface.getSong().getPdfPageCount();
            } else {
                onSectionAlready = mainActivityInterface.getSong().getCurrentSection() == nearbyJson.getSection();
                totalSections = mainActivityInterface.getSong().getPresoOrderSongSections().size();
            }
            if (!onSectionAlready && nearbyReturnActionsInterface != null && totalSections > nearbyJson.getSection()) {
                if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                    mainActivityInterface.getSong().setPdfPageCurrent(nearbyJson.getSection());
                } else {
                    mainActivityInterface.getSong().setCurrentSection(nearbyJson.getSection());
                }
                nearbyReturnActionsInterface.selectSection(nearbyJson.getSection());
            }
        }
    }
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
        if (!onSectionAlready && nearbyReturnActionsInterface != null && totalSections > mysection) {
            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                mainActivityInterface.getSong().setPdfPageCurrent(mysection);
            } else {
                mainActivityInterface.getSong().setCurrentSection(mysection);
            }
            nearbyReturnActionsInterface.selectSection(mysection);
        }
    }


    // Load songs - either using the info, or the file sent
    private void loadSongFromMyLibrary(NearbyJson nearbyJson) {
        setForceReload(true);
        // We just want to trigger loading the song on our device (if we have it).
        // If not, we get notified it doesn't exits
        if (nearbyReturnActionsInterface!=null && !nearbyActions.getNearbyConnectionManagement().getIsHost()) {
            if (nearbyJson.getFolder() != null && nearbyJson.getFilename()!=null) {
                mainActivityInterface.getSong().setFolder(nearbyJson.getFolder());
                mainActivityInterface.getSong().setFilename(nearbyJson.getFilename());
                if (nearbyJson.getSwipeDirection() != null) {
                    mainActivityInterface.getDisplayPrevNext().setSwipeDirection(nearbyJson.getSwipeDirection());
                } else {
                    mainActivityInterface.getDisplayPrevNext().setSwipeDirection("R2L");
                }

                // Check if we need to load the song in a different key.
                boolean needToTempTranspose = false;
                if (nearbyJson.getKey() != null) {
                    // Get the key of our song
                    needToTempTranspose = !nearbyJson.getKey().equals(mainActivityInterface.getSQLiteHelper().getKey(nearbyJson.getFolder(), nearbyJson.getFilename()));
                }

                // If we want to use PDF versions of songs instead, change the filename
                if (nearbyMatchToPDFSong && !nearbyJson.getFilename().toLowerCase().endsWith(".pdf")) {
                    String newPDFFilename = nearbyJson.getFilename() + ".pdf";
                    Uri newPDFUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", nearbyJson.getFolder(), newPDFFilename);
                    if (mainActivityInterface.getStorageAccess().uriExists(newPDFUri)) {
                        mainActivityInterface.getSong().setFilename(newPDFFilename);
                    }
                }

                // We can ignore any temp transpose if this is an image or a pdf
                if (mainActivityInterface.getStorageAccess().isIMGorPDF(mainActivityInterface.getSong())) {
                    needToTempTranspose = false;
                }

                if (!needToTempTranspose) {
                    // Now load the song if we are displaying the performance/stage/presenter fragment
                    if (nearbyJson.getSection() != null) {
                        mainActivityInterface.getSong().setCurrentSection(nearbyJson.getSection());
                    }
                    mainActivityInterface.getSong().setCurrentSection(getHostPendingSection());
                    nearbyReturnActionsInterface.loadSong(true);

                } else {
                    Song quickSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(
                            mainActivityInterface.getSong().getFolder(),
                            mainActivityInterface.getSong().getFilename());
                    quickSong = mainActivityInterface.getVariations().makeKeyVariation(quickSong,nearbyJson.getKey(),false, false);
                    quickSong.setFolder(mainActivityInterface.getVariations().getKeyVariationsFolder());
                    quickSong.setFilename(mainActivityInterface.getVariations().getKeyVariationFilename(mainActivityInterface.getSong().getFolder(),mainActivityInterface.getSong().getFilename(),nearbyJson.getKey()));
                    // Save the temp song
                    mainActivityInterface.getStorageAccess().saveThisSongFile(quickSong);
                    mainActivityInterface.getSong().setFolder(quickSong.getFolder());
                    mainActivityInterface.getSong().setFilename(quickSong.getFilename());
                    mainActivityInterface.getSong().setCurrentSection(getHostPendingSection());
                    nearbyReturnActionsInterface.loadSong(true);
                }
            }
        }
    }
    private void loadSongFromReceivedXML(NearbyJson nearbyJson) {
        if (nearbyReturnActionsInterface!=null && !nearbyActions.getNearbyConnectionManagement().getIsHost() &&
                nearbyReceiveHostFiles) {
            setForceReload(true);
            if (nearbyJson.getXml()!=null) {
                String folder = "Received";
                String subfolder = "../Received";
                String subfolderforuri = "";
                String filename = "ReceivedSong";

                if (nearbyKeepHostFiles) {
                    folder = "Songs";
                    subfolder = nearbyJson.getFolder();
                    subfolderforuri = nearbyJson.getFolder();
                    filename = nearbyJson.getFilename();
                }

                Uri newLocation = mainActivityInterface.getStorageAccess().getUriForItem(folder, subfolderforuri, filename);
                // Prepare the output stream in the Received folder - just keep a temporary version
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " creating temporary song file from XML received from connected host:  "+folder+"/" + subfolderforuri + "/" + filename);
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newLocation, null, folder, subfolderforuri, filename);
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newLocation);
                mainActivityInterface.getSong().setFolder(subfolder);
                mainActivityInterface.getSong().setFilename(filename);

                // IV - Store the received song filename in case the user wants to duplicate the received song
                receivedSongFilename = nearbyJson.getFilename();

                // Write the file to the desired output stream and load
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " write the file content: " + newLocation + " with: " + nearbyJson.getXml());

                if (mainActivityInterface.getStorageAccess().writeFileFromString(nearbyJson.getXml(), outputStream)) {
                    if (nearbyJson.getSection()!=null) {
                        mainActivityInterface.getSong().setCurrentSection(nearbyJson.getSection());
                    } else {
                        mainActivityInterface.getSong().setCurrentSection(-1);
                    }
                    if (nearbyJson.getSwipeDirection()!=null) {
                        mainActivityInterface.getDisplayPrevNext().setSwipeDirection(nearbyJson.getSwipeDirection());
                    } else {
                        mainActivityInterface.getDisplayPrevNext().setSwipeDirection("R2L");
                    }

                    // If we are keeping the song, update the database song first
                    if (nearbyReceiveHostFiles && nearbyKeepHostFiles) {
                        mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSongFile(mainActivityInterface.getSong(), false));
                        mainActivityInterface.getSQLiteHelper().updateSong(mainActivityInterface.getSong());
                        mainActivityInterface.updateSongMenu(mainActivityInterface.getSong());
                    }

                    nearbyReturnActionsInterface.loadSong(true);
                }
            } else {
                // No XML sent as we are awaiting a file.  Check for arrival
                checkForFileReceived(nearbyJson.getId());
            }
        }
    }
    private void checkForFileReceived(Long payloadId) {
        // Check for payload file and matching bytes
        Payload filePayload = nearbyActions.getNearbyTransferRecords().getAlreadyReceivedPayload(payloadId);
        NearbyJson fileInfo = nearbyActions.getNearbyTransferRecords().getAlreadyReceivedFileInformation(payloadId);

        Log.d(TAG,"filePayload:"+filePayload+"  fileInfo:"+fileInfo);
        if (filePayload!=null && fileInfo!=null) {
            // Both have arrived!!!
            Log.d(TAG,"Both have arrived!");

            Log.d(TAG,"fileInfo id:"+fileInfo.getId()+"  file:"+fileInfo.getFolder()+"/"+fileInfo.getFilename());
            // Add a record that we have dealt with them and set calls to remove the records after a delay
            nearbyActions.getNearbyTransferRecords().addIncomingDealtWith(payloadId,true);
            nearbyActions.getNearbyTransferRecords().removeAlreadyDealtWith(payloadId);
            nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(filePayload.getId());
            if (fileInfo.getId()!=null) {
                nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(fileInfo.getId());
            }

            Log.d(TAG,"fileInfo.getWhat():"+fileInfo.getWhat());
            if (fileInfo.getWhat()!=null) {
                if (fileInfo.getWhat().equals(nearbyActions.fileTag)) {
                    loadSongFromReceivedFile(filePayload, fileInfo.getFolder(), fileInfo.getFilename(), fileInfo);
                } else if (fileInfo.getWhat().equals(nearbyActions.syncReturnedInfo)) {
                    dealWithSyncInfoReturned(filePayload,fileInfo,fileInfo.getId());
                }
            }
        }
    }
    private void loadSongFromReceivedFile(Payload payloadReceived, String folder, String filename, NearbyJson nearbyJson) {
        // Get the received file (which will be in the Downloads folder)
        // Because of https://developer.android.com/preview/privacy/scoped-storage, we are not
        // allowed to access filepaths from another process directly. Instead, we must open the
        // uri using our ContentResolver.
        Payload.File payloadFile = payloadReceived.asFile();
        setForceReload(true);
        if (payloadFile!=null) {
            Uri inputUri = payloadFile.asUri();
            Uri outputUri;
            String songFolder;
            String songSubfolder;
            String folderToUseForSongLoad;
            if (nearbyKeepHostFiles) {
                songFolder = "Songs";
                songSubfolder = folder;
                folderToUseForSongLoad = folder;
            } else {
                songFolder = "Received";
                songSubfolder = "";
                folderToUseForSongLoad = "../Received";
            }

            outputUri = mainActivityInterface.getStorageAccess().getUriForItem(songFolder,songSubfolder,filename);
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,outputUri,null,songFolder,songSubfolder,filename);

            try {
                // Copy the file to a new location.
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(inputUri);
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(outputUri);
                mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream);

                mainActivityInterface.getSong().setFolder(songSubfolder);
                mainActivityInterface.getSong().setFilename(filename);

                // If we are keeping the song, update the database song first
                if (nearbyReceiveHostFiles && nearbyKeepHostFiles) {
                    if (mainActivityInterface.getStorageAccess().isIMGorPDF(filename)) {
                        // Get any extra info for this image/pdf file
                        Song tempSong = nearbyJson.getSong();
                        if (tempSong != null) {
                            tempSong.setFolder(folder);
                            tempSong.setFilename(filename);
                            // Add info to the persistent database
                            mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(tempSong);
                            mainActivityInterface.getSQLiteHelper().updateSong(tempSong);
                            mainActivityInterface.setSong(tempSong);
                        }
                    }
                    // Add the content to the temporary database
                    if (mainActivityInterface.getSQLiteHelper().getSpecificSong(songSubfolder, filename) == null) {
                        mainActivityInterface.getSQLiteHelper().createSong(songSubfolder, filename);
                    }
                    mainActivityInterface.getSQLiteHelper().updateSong(mainActivityInterface.getSong());

                    // Update the song menu
                    mainActivityInterface.updateSongMenu(mainActivityInterface.getSong());
                }

                // Now set the song to load
                mainActivityInterface.getSong().setFolder(folderToUseForSongLoad);
                mainActivityInterface.getSong().setFilename(filename);
                mainActivityInterface.getSong().setCurrentSection(getHostPendingSection());
                mainActivityInterface.getDisplayPrevNext().setSwipeDirection(nearbyJson.getSwipeDirection());
                setForceReload(true);
                nearbyReturnActionsInterface.loadSong(true);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Delete the original file.
                c.getContentResolver().delete(inputUri, null, null);
            }
        }
    }


    // Deal with synchronisation actions
    private void dealWithSyncRequestInfo(NearbyJson nearbyJson) {
        // This device has been asked for information on available sync items
        Log.d(TAG,"dealWithSyncRequestInfo()");
        Log.d(TAG,"nearbyJson.getId():"+nearbyJson.getId());
        String deviceRequesting = nearbyJson.getDeviceSending();
        Log.d(TAG,"deviceRequesting:"+deviceRequesting);
        Log.d(TAG,"deviceToAction"+nearbyJson.getDeviceToAction());
        Log.d(TAG,"deviceId():"+nearbyActions.getNearbyConnectionManagement().getDeviceId());
        // Only action this if we haven't already done so
        if (nearbyJson.getId()==null || !nearbyActions.getNearbyTransferRecords().getAlreadyReceivedFileInformation(nearbyJson)) {
            if (nearbyJson.getId()!=null) {
                nearbyActions.getNearbyTransferRecords().addAlreadyReceivedFileInformation(nearbyJson);
            }
            // Send a message back to tell them we are processing it, or that we have denied it
            nearbyJson = new NearbyJson();
            if (nearbyActions.getNearbyConnectionManagement().getNearbyFileSharing()) {
                nearbyJson.setWhat(nearbyActions.syncProcessingInfo);
                nearbyJson.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
                nearbyJson.setDeviceToAction(deviceRequesting);
                nearbyActions.getNearbySendPayloads().sendPayloadToSelected(nearbyJson.getDeviceToAction(),
                        Payload.fromBytes(MainActivity.gson.toJson(nearbyJson).getBytes()));
                // Now, we can deal with the actual task and send that when ready
                nearbyActions.getNearbySendPayloads().sendSyncInfo(nearbyJson.getDeviceToAction());
            } else {
                // Let them know the bad news...
                nearbyJson.setWhat(nearbyActions.syncRequestDenied);
                nearbyJson.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
                nearbyJson.setDeviceToAction(deviceRequesting);
                nearbyActions.getNearbySendPayloads().sendPayloadToSelected(nearbyJson.getDeviceToAction(),
                        Payload.fromBytes(MainActivity.gson.toJson(nearbyJson).getBytes()));
            }
        }

    }
    private void dealWithSyncRequestProcessingInfo(NearbyJson nearbyJson) {
        // Check we haven't dealt with this already
        Log.d(TAG,"dealWithSyncRequestProcessingInfo()");
        Log.d(TAG,"nearbyJson.getId():"+nearbyJson.getId());
        Log.d(TAG,"nearbyJson.getWhat():"+nearbyJson.getWhat());
        Log.d(TAG,"nearbyJson.getDeviceSending():"+nearbyJson.getDeviceSending());
        Log.d(TAG,"nearbyJson.getDeviceToAction():"+nearbyJson.getDeviceToAction());
        if (nearbyJson.getId()==null || !nearbyActions.getNearbyTransferRecords().getAlreadyReceivedFilePayload(nearbyJson.getId())) {
            Log.d(TAG,"we haven't received this before!");
            if (nearbyJson.getId()!=null) {
                nearbyActions.getNearbyTransferRecords().addAlreadyReceivedFileInformation(nearbyJson);
            }
            // The device we requested info from is processing!  Let the user know
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.sync_device_processing));
            if (nearbyActions.getSyncNearbyFragment() != null) {
                nearbyActions.getSyncNearbyFragment().showProgress(false);
            }
        } else {
            // We've already received this one
        }
    }
    private void dealWithSyncRequestDenied() {
        // The device we tried to sync with has denied nearbyFileSharing
        mainActivityInterface.getShowToast().doIt(c.getString(R.string.sync_device_denied));
        if (nearbyActions.getSyncNearbyFragment()!=null) {
            nearbyActions.getSyncNearbyFragment().showProgress(false);
        }
    }
    private void dealWithSyncInfoReturned(Payload filePayload, NearbyJson fileInfo, Long payloadId) {
        // The connected device has sent us information!
        // We can clear the received info
        Payload.File file = filePayload.asFile();
        Log.d(TAG,"dealWithSyncInfoReturned()");
        if (file != null) {
            Uri uri = file.asUri();
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
            NearbyJson nearbyJson = MainActivity.gson.fromJson(
                    mainActivityInterface.getStorageAccess().readTextFileToString(inputStream), NearbyJson.class);
            if (nearbyJson.getShareableSongObjects() != null) {
                Log.d(TAG, "number of songs:" + nearbyJson.getShareableSongObjects().size());
            }
        }
        nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(filePayload.getId());
        nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedFileInformation(fileInfo.getId());
        nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(payloadId);

    }
    private void dealWithSyncRequestForContent(NearbyJson nearbyJson) {
        // TODO
    }
    private void dealWithSyncContentReturned(NearbyJson nearbyJson) {
        // TODO
    }
    private void dealWithSyncDenied(NearbyJson nearbyJson) {
        // TODO
    }


    // Host pending sections (for delayed load)
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


    // If we should force reloading of the song
    public boolean getForceReload() {
        return forceReload;
    }
    public void setForceReload(boolean forceReload) {
        this.forceReload = forceReload;
    }


    // The getters (for user preferences)
    public boolean getNearbyReceiveHostFiles() {
        return nearbyReceiveHostFiles;
    }
    public boolean getNearbyReceiveHostAutoscroll() {
        return nearbyReceiveHostAutoscroll;
    }
    public boolean getNearbyReceiveHostScroll() {
        return nearbyReceiveHostScroll;
    }
    public boolean getNearbyKeepHostFiles() {
        return nearbyKeepHostFiles;
    }
    public boolean getNearbyMatchToPDFSong() {
        return nearbyMatchToPDFSong;
    }
    public boolean getNearbyMessageSticky() {
        return nearbyMessageSticky;
    }
    public String getIncomingPrevious() {
        return incomingPrevious;
    }
    public boolean getNearbyReceiveHostSongSections() {
        return nearbyReceiveHostSongSections;
    }


    // The setters (also saves the preference if required
    public void setNearbyReceiveHostFiles(boolean nearbyReceiveHostFiles) {
        this.nearbyReceiveHostFiles = nearbyReceiveHostFiles;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyReceiveHostFiles",nearbyReceiveHostFiles);
    }
    public void setIncomingPrevious(String incomingPrevious) {
        this.incomingPrevious = incomingPrevious;
    }
    public void setNearbyReceiveHostAutoscroll(boolean nearbyReceiveHostAutoscroll) {
        this.nearbyReceiveHostAutoscroll = nearbyReceiveHostAutoscroll;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyReceiveHostAutoscroll",nearbyReceiveHostAutoscroll);
    }
    public void setNearbyReceiveHostScroll(boolean nearbyReceiveHostScroll) {
        this.nearbyReceiveHostScroll = nearbyReceiveHostScroll;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyReceiveHostScroll",nearbyReceiveHostScroll);
    }
    public void setNearbyKeepHostFiles(boolean nearbyKeepHostFiles) {
        this.nearbyKeepHostFiles = nearbyKeepHostFiles;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyKeepHostFiles",nearbyKeepHostFiles);
    }
    public void setNearbyMatchToPDFSong(boolean nearbyMatchToPDFSong) {
        this.nearbyMatchToPDFSong = nearbyMatchToPDFSong;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyMatchToPDFSong",nearbyMatchToPDFSong);
    }
    public void setNearbyMessageSticky(boolean nearbyMessageSticky) {
        this.nearbyMessageSticky = nearbyMessageSticky;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyMessageSticky",nearbyMessageSticky);
    }
    public void setNearbyReceiveHostSongSections(boolean nearbyReceiveHostSongSections) {
        this.nearbyReceiveHostSongSections = nearbyReceiveHostSongSections;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyReceiveHostSongSections",nearbyReceiveHostSongSections);
    }









    // TODO fix or remove the stuff below
    public String getReceivedSongFilename() {
        return receivedSongFilename;
    }




    /*private String[] getSongInformation(NearbyJson nearbyJson) {
        String[] returnBits = null;
        // If it is bytes and has song information, get the folder/filename pair
        if (payloadReceived.getType() == Payload.Type.BYTES) {
            String payloadString = new String(payloadReceived.asBytes(), StandardCharsets.UTF_8);
            if (payloadString.contains(nearbyActions.songTag)) {
                returnBits = new String[5];

                // New method sends OpenSong songs in the format of
                //  FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>
                // For v6.1.6 the key is also included:
                //  FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>_xx____xx_KEY
                String[] bits = payloadString.split(nearbyActions.songTag);

                if (bits.length>=3) {
                    returnBits[0] = bits[0];
                    returnBits[1] = bits[1];
                    returnBits[2] = bits[2];
                    returnBits[3] = null;
                    returnBits[4] = null;
                    if (bits.length>3) {
                        for (int i = 3; i < bits.length; i++) {
                            if (bits[i].contains("<?xml")) {
                                returnBits[3] = bits[i];
                            } else {
                                returnBits[4] = bits[i];
                            }
                        }
                    }
                }
            }
        }
        return returnBits;
    }*/


    // Deal with actions received as a client device
    // Triggered when a host has sent a payload - this is where clients listen out!
    // If the host is allowing passthrough, it doesn't listen, but passes it on
    // Deal with actions received as a client device
    // Triggered when a host has sent a payload - this is where clients listen out!
    // If the host is allowing passthrough, it doesn't listen, but passes it on
    /*PayloadCallback payloadCallback_OLD() {
        return new PayloadCallback() {
            @Override
            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                byte[] bytes;
                String payLoadAsString = null;
                boolean dealWithAsHostRequestFile = false;
                boolean getMyInfo = false;
                String deviceRequesting = null;
                String deviceToGetFrom;
                boolean getFromThisDevice = false;
                boolean waitingForSyncInfo = false;
                boolean receivedSyncInfo = false;

                Log.d(TAG, "payload:" + payload);

                if (payload.getType() == Payload.Type.BYTES && payload.asBytes() != null) {
                    bytes = payload.asBytes();
                    if (bytes != null) {
                        payLoadAsString = new String(bytes);
                        Log.d(TAG, "payLoadAsString:" + payLoadAsString);
                    }
                } else if (payload.getType() == Payload.Type.FILE) {
                    Payload.File payloadFile = payload.asFile();
                    Uri payloadFileUri = null;
                    if (payloadFile != null) {
                        payloadFileUri = payloadFile.asUri();
                    }
                    Log.d(TAG, "payload.getId():" + payload.getId() + "  payloadFile:" + payloadFile + "  payloadFileUri:" + payloadFileUri);
                }

                // If we are a host and this is a request to send a file, deal with that separately
                if (payLoadAsString != null && payLoadAsString.startsWith(nearbyActions.requestFileTag)) {
                    dealWithAsHostRequestFile = true;
                    nearbyActions.getNearbyTransferRecords().getIncomingFilePayloads().put(payload.getId(), payload);
                    nearbyActions.getNearbySendPayloads().hostSendFile(payLoadAsString);

                } else if (payLoadAsString != null && payLoadAsString.contains(nearbyActions.getItemInfo)) {
                    getMyInfo = true;
                    String[] bits = payLoadAsString.split(nearbyActions.getItemInfo);
                    deviceRequesting = bits[0];
                    nearbyActions.getNearbyConnectionManagement().setRequestingDevice(deviceRequesting);
                    deviceToGetFrom = bits[1];
                    getFromThisDevice = deviceToGetFrom.equals(nearbyActions.getNearbyConnectionManagement().getDeviceId());
                    Log.d(TAG, "deviceRequesting:" + deviceRequesting);
                    Log.d(TAG, "deviceToGetFrom:" + deviceToGetFrom);

                } else if (nearbyActions.getSyncNearbyFragment() != null && payLoadAsString != null && payLoadAsString.contains(nearbyActions.processingItemInfo) && payLoadAsString.contains(nearbyActions.getNearbyConnectionManagement().getDeviceId())) {
                    waitingForSyncInfo = true;
                }

                if (getMyInfo && getFromThisDevice && deviceRequesting != null) {
                    // We need to send our info to the requesting device
                    // Send info back to the device to tell them we are processing
                    if (!nearbyActions.getNearbyConnectionManagement().getNearbyFileSharing()) {
                        // Tell them that we are not permitting file sharing
                        nearbyActions.getNearbySendPayloads().doSendPayloadBytes(nearbyActions.denyItemInfo + deviceRequesting, true);
                    } else {
                        // Tell them that we are processing the sharing info
                        nearbyActions.getNearbySendPayloads().doSendPayloadBytes(nearbyActions.processingItemInfo + deviceRequesting, true);

                        // Do the next bit asynchronously and prepare the json file to send
                        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                            Uri uriToSend = createShareableObjectsForRequester();
                            if (uriToSend != null) {
                                Log.d(TAG, "infoToSend:" + uriToSend);
                                for (String endpointString : nearbyActions.getNearbyConnectionManagement().getConnectedEndpoints()) {
                                    if (nearbyActions.getNearbyConnectionManagement().getRequestingDevice()!=null &&
                                            !nearbyActions.getNearbyConnectionManagement().getRequestingDevice().isEmpty() &&
                                            endpointString.contains(nearbyActions.getNearbyConnectionManagement().getRequestingDevice())) {
                                        Log.d(TAG, "endpointString:" + endpointString);
                                        ParcelFileDescriptor pfd;
                                        try {
                                            pfd = c.getContentResolver().openFileDescriptor(uriToSend, "r");
                                            if (pfd != null) {
                                                Payload filePayload = Payload.fromFile(pfd);
                                                Nearby.getConnectionsClient(c).sendPayload(endpointString, filePayload);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            }
                        });
                    }

                } else if (waitingForSyncInfo) {
                    // We have received a message from the device that it is processing
                    nearbyActions.getSyncNearbyFragment().waitingOnNearbyDeviceSendingInfo();

                } else if (receivedSyncInfo) {


                } else if (!dealWithAsHostRequestFile) {
                    Log.d(TAG,"dealWithAsHostRequestFile:"+dealWithAsHostRequestFile);
                    // Deal with this if is a normal song request
                    // To avoid send loops, only devices set as clients act onPayloadReceived
                    // However if we are set as cluster strategy, we should echo what we have received
                    // This is because clients can be connected to the host through another client
                    // Check if we've already received/sent this out.  Only proceed if not
                    if (nearbyActions.getNearbyTransferRecords().getPreviousPayload() == null ||
                            !nearbyActions.getNearbyTransferRecords().getPreviousPayload().equals(payload)) {
                        // Keep a note of this payload
                        Log.d(TAG,"Keep a note of this payload");
                        nearbyActions.getNearbyTransferRecords().setPreviousPayload(payload);

                        // If we are a client or a passthrough host, send on the information
                        if (!nearbyActions.getNearbyConnectionManagement().getIsHost() ||
                                nearbyActions.getNearbyConnectionManagement().getNearbyHostPassthrough()) {
                            // Send the payload out again in case any of my connected devices need to hear it from me
                            // This will only happen in Strategy.P2P_CLUSTER or P2P_STAR
                            nearbyActions.getNearbySendPayloads().repeatPayload(payload);
                        }

                        // Now deal with what has come in if I am a client
                        if (!nearbyActions.getNearbyConnectionManagement().getIsHost()) {
                            // We can deal with the incoming payload!
                            if (payload.getType() == Payload.Type.FILE) {
                                requestedFilePayload = payload;
                                // Make a note of it.  Nothing happens until complete
                                Log.d(TAG, "Payload.Type: FILE");
                                nearbyActions.getNearbyTransferRecords().getIncomingFilePayloads().put(payload.getId(), payload);

                            } else if (payload.getType() == Payload.Type.BYTES) {
                                // We're dealing with bytes
                                Log.d(TAG, "Payload.Type: BYTES");
                                String incoming = payLoadAsString;
                                if (incoming != null && incoming.startsWith(nearbyActions.currentset)) {
                                    dealWithHostCurrentSet(incoming);

                                } else if (incoming != null && incoming.startsWith(nearbyActions.requestFileTag)) {
                                    String id = "-1";
                                    incoming = incoming.replace(nearbyActions.requestFileTag, "");
                                    if (incoming.contains(nearbyActions.requestIdSeparator) && incoming.contains(nearbyActions.requestFileSeparator)) {
                                        id = incoming.substring(incoming.indexOf(nearbyActions.requestIdSeparator), incoming.indexOf(nearbyActions.requestFileSeparator));
                                        id = id.replace(nearbyActions.requestIdSeparator, "");
                                        incoming = incoming.replace(nearbyActions.requestIdSeparator + id, "");
                                    }
                                    if (incoming.contains(nearbyActions.getNearbyConnectionManagement().getDeviceId())) {
                                        Log.d(TAG, "contains my device, so for me!!");
                                        // Get the file expected location
                                        String fileinfo = incoming.substring(incoming.indexOf(nearbyActions.requestFileSeparator) + nearbyActions.requestFileSeparator.length());
                                        String[] bits = fileinfo.split(nearbyActions.requestFileSeparator);
                                        String foldernamepair = "../" + bits[0];
                                        if (!bits[1].isEmpty()) {
                                            foldernamepair += "/" + bits[1];
                                        } else {
                                            foldernamepair += "/" + mainActivityInterface.getMainfoldername();
                                        }
                                        foldernamepair += nearbyActions.songTag + bits[2];
                                        nearbyActions.getNearbyTransferRecords().getFileNewLocation().put(Long.parseLong(id), foldernamepair);
                                    }


                                } else if (incoming != null && incoming.startsWith("FILE:")) {
                                    // Add the file location to the arraymap
                                    incoming = incoming.replaceFirst("FILE:", "");
                                    String id = incoming.substring(0, incoming.indexOf(":"));
                                    id = id.replace(":", "");
                                    String foldernamepair = incoming.substring(incoming.indexOf(":"));
                                    foldernamepair = foldernamepair.replace(":", "");
                                    Log.d(TAG,"keep record id:"+Long.parseLong(id)+","+foldernamepair);
                                    nearbyActions.getNearbyTransferRecords().getFileNewLocation().put(Long.parseLong(id), foldernamepair);
                                    Log.d(TAG,"foldernamepair:"+foldernamepair);
                                    // If we aren't receiving host songs (using our own), try to load this song
                                    if (!nearbyReceiveHostFiles) {
                                        Log.d(TAG,"try payloadOpenSong("+foldernamepair+")");
                                        payloadOpenSong(foldernamepair);
                                    }


                                } else if (incoming != null && incoming.contains("autoscroll")) {
                                    // IV - Autoscroll only in Performance mode when user option is selected
                                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) && nearbyReceiveHostAutoscroll) {
                                        payloadAutoscroll(incoming);
                                    }
                                } else if (incoming != null && incoming.equals(nearbyActions.autoscrollPause)) {
                                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                                            nearbyReceiveHostAutoscroll) {
                                        mainActivityInterface.getAutoscroll().pauseAutoscroll();
                                    }

                                } else if (incoming != null && incoming.equals(nearbyActions.autoscrollincrease)) {
                                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                                            nearbyReceiveHostAutoscroll) {
                                        mainActivityInterface.getAutoscroll().speedUpAutoscroll();
                                    }

                                } else if (incoming != null && incoming.equals(nearbyActions.autoscrolldecrease)) {
                                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                                            nearbyReceiveHostAutoscroll) {
                                        mainActivityInterface.getAutoscroll().slowDownAutoscroll();
                                    }

                                } else if (incoming != null && incoming.contains(nearbyActions.sectionTag)) {
                                    // IV - Section change only in Stage and Presentation mode (Song or PDF) when user option is selected
                                    if ((!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) ||
                                            mainActivityInterface.getSong().getFiletype().equals("PDF")) &&
                                            nearbyReceiveHostSongSections) {
                                        Log.d(TAG, "call payloadSection " + incoming);
                                        payloadSection(incoming);
                                    }
                                } else if (incoming != null && incoming.contains(nearbyActions.songTag)) {
                                    payloadOpenSong(incoming);
                                } else if (incoming != null && incoming.contains(nearbyActions.scrollByTag)) {
                                    // We have received a scroll by amount command.  Check we want this
                                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                                            nearbyReceiveHostScroll) {
                                        Log.d(TAG, "call payloadScrollBy");
                                        payloadScrollBy(incoming);
                                    }
                                } else if (incoming != null && incoming.contains(nearbyActions.scrollToTag)) {
                                    // We have received a scroll to instruction.  Check we want this
                                    if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                                            nearbyReceiveHostScroll) {
                                        Log.d(TAG, "call payloadScrollTo");
                                        payloadScrollTo(incoming);
                                    }
                                } else if (incoming != null && incoming.startsWith(nearbyActions.messageTag)) {
                                    // We have received an alert message
                                    Log.d(TAG, "call payloadMessage");
                                    payloadMessage(incoming);
                                } else if (incoming != null && incoming.startsWith(nearbyActions.hostItems + nearbyActions.getNearbyConnectionManagement().getDeviceId())) {
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
                                    incoming = payLoadAsString;
                                }
                            }
                            if (incoming != null && incoming.startsWith(nearbyActions.hostRequest)) {
                                incoming = incoming.replaceFirst(nearbyActions.hostRequest, "");
                                String message = null;
                                if (incoming.startsWith(nearbyActions.sets)) {
                                    // Get the requesting deviceID
                                    incoming = incoming.replaceFirst(nearbyActions.sets, "");
                                    // Get this device's items
                                    message = nearbyActions.hostItems + incoming + "\n" + getHostItems("browsesets");
                                } else if (incoming.startsWith(nearbyActions.currentset)) {
                                    // Get the requesting deviceID
                                    incoming = incoming.replaceFirst(nearbyActions.currentset, "");
                                    // Get this device's items
                                    message = nearbyActions.currentset + incoming + "\n" + mainActivityInterface.getCurrentSet().getSetCurrent();
                                } else if (incoming.startsWith(nearbyActions.profiles)) {
                                    // Get the requesting deviceID
                                    incoming = incoming.replaceFirst(nearbyActions.profiles, "");
                                    // Get this device's items
                                    message = nearbyActions.hostItems + incoming + "\n" + getHostItems("browseprofiles");
                                } else if (incoming.startsWith(nearbyActions.songs)) {
                                    // Get the requesting deviceID
                                    incoming = incoming.replaceFirst(nearbyActions.songs, "");
                                    // Get this device's items
                                    message = nearbyActions.hostItems + incoming + "\n" + getHostItems("browsesongs");
                                }
                                if (message != null) {
                                    // Send the message to the listening clients
                                    // Only the one with this deviceID will act on it though
                                    nearbyActions.getNearbySendPayloads().doSendPayloadBytes(message, false);
                                }

                            }
                        }
                    }
                }
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                // If we are requesting host files, we deal with this separately
                if (nearbyActions.getSyncNearbyFragment() != null &&
                        nearbyActions.getSyncNearbyFragment().getWaitingForFiles() &&
                        !nearbyActions.getNearbyConnectionManagement().getIsHost() &&
                        nearbyActions.getNearbyTransferRecords().getIncomingFilePayloads().containsKey(payloadTransferUpdate.getPayloadId())) {
                    Log.d(TAG,"try dealWithRequestedFile()");
                    dealWithRequestedFile(payloadTransferUpdate.getPayloadId());
                } else {
                    // IV - If we are a client and not 'receiving host files' then cancel these uneeded FILE transfers
                    if (!nearbyActions.getNearbyConnectionManagement().getIsHost() && !nearbyReceiveHostFiles) {
                        if (nearbyActions.getNearbyTransferRecords().getIncomingFilePayloads().containsKey(payloadTransferUpdate.getPayloadId())) {
                            Nearby.getConnectionsClient(activity).cancelPayload(payloadTransferUpdate.getPayloadId());
                        }
                    } else {
                        if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                            // For bytes this is sent automatically, but it's the file we are interested in here
                            Payload payload;
                            if (nearbyActions.getNearbyTransferRecords().getIncomingFilePayloads().containsKey(payloadTransferUpdate.getPayloadId())) {
                                payload = nearbyActions.getNearbyTransferRecords().getIncomingFilePayloads().get(payloadTransferUpdate.getPayloadId());
                                String foldernamepair = nearbyActions.getNearbyTransferRecords().getFileNewLocation().get(payloadTransferUpdate.getPayloadId());
                                if (foldernamepair == null) {
                                    foldernamepair = "../Received" + nearbyActions.songTag + "ReceivedSong";
                                }
                                nearbyActions.getNearbyTransferRecords().getIncomingFilePayloads().remove(payloadTransferUpdate.getPayloadId());
                                nearbyActions.getNearbyTransferRecords().getFileNewLocation().remove(payloadTransferUpdate.getPayloadId());

                                payloadFile(payload, foldernamepair);
                            }

                            // IV - Keep a record of Ids
                            if (nearbyActions.getNearbyTransferRecords().getPayloadTransferIds() == null) {
                                nearbyActions.getNearbyTransferRecords().setPayloadTransferIds("");
                            }
                            if (!(nearbyActions.getNearbyTransferRecords().getPayloadTransferIds().contains(payloadTransferUpdate.getPayloadId() + " "))) {
                                nearbyActions.getNearbyTransferRecords().setPayloadTransferIds(nearbyActions.getNearbyTransferRecords().getPayloadTransferIds() + payloadTransferUpdate.getPayloadId() + " ");
                                Log.d(TAG, "Id History " + nearbyActions.getNearbyTransferRecords().getPayloadTransferIds());
                            }
                        }
                    }
                }
            }
        };
    }

*/

    // Actions when receiving payloads






    void payloadOpenSong(String incoming) {
        /*// IV - CLIENT: Cancel previous song transfers - a new song has arrived
        nearbyActions.getNearbyTransferRecords().cancelTransferIds();

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

            Log.d(TAG, "isHost=" + nearbyActions.getNearbyConnectionManagement().getIsHost() + "  hasValidConnections()=" + nearbyActions.getNearbyConnectionManagement().hasValidConnections() + "  nearbyReceiveHostFiles=" + nearbyReceiveHostFiles + "  nearbyKeepHostFiles=" + nearbyKeepHostFiles);
            if (songReceived) {
                // Remove the current set position otherwise the client can be confused
                mainActivityInterface.getCurrentSet().setIndexSongInSet(-1);
                mainActivityInterface.getSetActions().indexSongInSet(receivedBits.get(0), receivedBits.get(1),"");
                mainActivityInterface.getSetMenuFragment().setHighlightChangeAllowed(true);
                mainActivityInterface.getSetMenuFragment().updateHighlight();
                mainActivityInterface.getSetMenuFragment().removeHighlight();
                mainActivityInterface.getSetMenuFragment().setHighlightChangeAllowed(false);

                // Set the reload flag
                forceReload = true;

                if (!nearbyActions.getNearbyConnectionManagement().getIsHost() && nearbyActions.getNearbyConnectionManagement().hasValidConnections() && nearbyReceiveHostFiles) {
                    // We want to receive host files (we aren't the host either!) and an OpenSong song has been sent/received
                    mainActivityInterface.getDisplayPrevNext().setSwipeDirection(receivedBits.get(2));

                    // Set the reload flag
                    forceReload = true;

                    // If the user wants to keep the host file, we will save it to our storage.
                    // If we already have it, it will overwrite it, if not, we add it
                    Uri newLocation;
                    if (nearbyKeepHostFiles) {
                        // Prepare the output stream in the client Songs folder
                        // Check the folder exists, if not, create it
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadOpenSong createFile Songs/" + receivedBits.get(0));
                        mainActivityInterface.getStorageAccess().createFile(DocumentsContract.Document.MIME_TYPE_DIR, "Songs", receivedBits.get(0), "");
                        newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Songs", receivedBits.get(0), receivedBits.get(1));
                        // Create the file if it doesn't exist
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payLoadOpenSong() nearbyKeepHostFiles Create Songs/" + receivedBits.get(0) + "/" + receivedBits.get(1) + "  deleteOld=true");
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
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payLoadOpenSong !nearbyKeepHostFiles Create Songs/" + receivedBits.get(0) + "/" + receivedBits.get(1) + " deleteOld=true");
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
                        boolean writeSuccess = mainActivityInterface.getStorageAccess().writeFileFromString(receivedBits.get(3), outputStream);
                        Log.d(TAG, "write the file: " + writeSuccess);

                        // If we are keeping the song, update the database song first
                        if (nearbyReceiveHostFiles && nearbyKeepHostFiles) {
                            mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSongFile(mainActivityInterface.getSong(), false));
                            mainActivityInterface.getSQLiteHelper().updateSong(mainActivityInterface.getSong());
                            mainActivityInterface.updateSongMenu(mainActivityInterface.getSong());
                        }
                        mainActivityInterface.getSong().setCurrentSection(getHostPendingSection());
                        nearbyReturnActionsInterface.loadSong(true);
                    }
                } else if (!nearbyActions.getNearbyConnectionManagement().getIsHost() && nearbyActions.getNearbyConnectionManagement().hasValidConnections()) {
                    // We just want to trigger loading the song on our device (if we have it).
                    // If not, we get notified it doesn't exits
                    mainActivityInterface.getSong().setFolder(receivedBits.get(0));
                    mainActivityInterface.getSong().setFilename(receivedBits.get(1));
                    mainActivityInterface.getDisplayPrevNext().setSwipeDirection(receivedBits.get(2));

                    // If we want to use PDF versions of songs instead, change the filename
                    if (nearbyMatchToPDFSong && !receivedBits.get(1).endsWith(".pdf") && !receivedBits.get(1).endsWith(".PDF")) {
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
        }*/
    }
    private ArrayList<String> getNearbyIncoming(String incoming) {
        // New method sends OpenSong songs in the format of
        // FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>
        // V6.1.6 also has the key
        // FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>_xx____xx_KEY
        // songTag = "_xx____xx_";
        // Four distict parts
        ArrayList<String> arrayList = new ArrayList<>();
        String[] bits = incoming.split(nearbyActions.songTag);

        for (int i = 0; i < 5; i++) {
            if (bits.length > i) {
                arrayList.add(i, bits[i]);
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
    /*private void payloadFile(Payload payload, String foldernamepair) {
        try {
            // IV - CLIENT: Cancel previous song transfers - a new song has arrived
            nearbyActions.getNearbyTransferRecords().cancelTransferIds();
            // If songs are too big, then we receive them as a file rather than bytes
            String[] bits = foldernamepair.split(nearbyActions.songTag);
            if (bits.length < 3) {
                bits = new String[3];
                bits[0] = "";
                bits[1] = "";
                bits[2] = "R2L";
            }
            String folder = bits[0];
            String filename = bits[1];
            mainActivityInterface.getDisplayPrevNext().setSwipeDirection(bits[2]);
            Uri newLocation = null;
            if (!nearbyActions.getNearbyConnectionManagement().getIsHost() && nearbyActions.getNearbyConnectionManagement().hasValidConnections() && nearbyReceiveHostFiles && nearbyKeepHostFiles && filename != null && !filename.isEmpty()) {
                // The new file goes into our main Songs folder if we don't already have it
                newLocation = mainActivityInterface.getStorageAccess().getUriForItem("Songs", folder, filename);
                if (!mainActivityInterface.getStorageAccess().uriExists(newLocation)) {
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadFile Create Songs/" + folder + "/" + filename + " deleteOld=false");
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, newLocation, null, "Songs", folder, filename);
                } else {
                    // Check it isn't a zero filesize/corrupt
                    if (mainActivityInterface.getStorageAccess().getFileSizeFromUri(newLocation) == 0) {
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadFile 0kb file Create Songs/" + folder + "/" + filename + " deleteOld=true");
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newLocation, null, "Songs", folder, filename);
                    } else {
                        // Set to null as we don't need to create it as we already have this song
                        newLocation = null;
                    }
                }
            } else if (!nearbyActions.getNearbyConnectionManagement().getIsHost() && nearbyActions.getNearbyConnectionManagement().hasValidConnections() && nearbyReceiveHostFiles && filename != null && !filename.isEmpty()) {
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
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadFile copyFile from " + originalUri + " to " + newLocation);
                if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                    if (nearbyReturnActionsInterface != null) {
                        // Make sure song is in the database (but not for received folder!
                        if (!folder.startsWith("../") && !folder.startsWith("**") &&
                                !mainActivityInterface.getSQLiteHelper().songExists(folder, filename)) {
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
    }*/

    /*private int getNearbySection(String incoming) {
        if (incoming != null && incoming.startsWith(nearbyActions.sectionTag)) {
            incoming = incoming.replace(nearbyActions.sectionTag, "");
            try {
                return Integer.parseInt(incoming);
            } catch (Exception e) {
                return 0;
            }
        } else {
            return 0;
        }
    }
    */
    /*private void payloadSection(String incoming) {
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
    }*/

    /*private void payloadAutoscroll(String incoming) {
        // It sends autoscroll startstops as autoscrollStart or autoscrollStop
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
            // Adjust only when not already in the correct state
            if (nearbyReturnActionsInterface != null &&
                    !(mainActivityInterface.getAutoscroll().getIsAutoscrolling() == incoming.equals(nearbyActions.autoscrollStart))) {
                nearbyReturnActionsInterface.toggleAutoscroll();
            }
        }
    }*/
    /*private void payloadScrollBy(String incoming) {
        // It sends the scrollProportion as a ratio of scrollAmount/songHeight
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                nearbyReturnActionsInterface != null) {
            incoming = incoming.replace(nearbyActions.scrollByTag, "");
            try {
                float proportion = Float.parseFloat(incoming);
                nearbyReturnActionsInterface.doScrollByProportion(proportion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/
    /*private void payloadScrollTo(String incoming) {
        // It sends the scrollProportion as a ratio of scrollAmount/songHeight
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                nearbyReturnActionsInterface != null) {
            incoming = incoming.replace(nearbyActions.scrollToTag, "");
            try {
                float proportion = Float.parseFloat(incoming);
                nearbyReturnActionsInterface.doScrollToProportion(proportion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/
    /*public void payloadMessage(String incoming) {
        incoming = incoming.replace(nearbyActions.messageTag, "");

        // Show an alert to the client
        if (!mainActivityInterface.getMode().equals("Presenter") && nearbyMessageSticky) {
            // Show a sticky note alert
            mainActivityInterface.showNearbyAlertPopUp(incoming);
        } else {
            // Show a toast message
            mainActivityInterface.getShowToast().doIt(incoming);
        }
    }*/

/*

    // Get a current set from the host
    public void dealWithHostCurrentSet(String requestPayload) {
        if (!nearbyActions.getNearbyConnectionManagement().getIsHost() && requestPayload.contains(nearbyActions.getNearbyConnectionManagement().getDeviceId())) {
            // This is for us!  Remove the unnecessary stuff
            requestPayload = requestPayload.replace(nearbyActions.hostItems,"").
                    replace(nearbyActions.currentset,"").replace(nearbyActions.getNearbyConnectionManagement().getDeviceId(),"");
            // What is left is the current set
            // Split the set into an array
            String[] requestPayloadArray = requestPayload.split(Pattern.quote(mainActivityInterface.getSetActions().getItemEnd()));
            if (nearbyActions.getSyncNearbyFragment() !=null) {
                nearbyActions.getSyncNearbyFragment().setNearbyCurrentSet(requestPayload);
                nearbyActions.getSyncNearbyFragment().displayHostItems(requestPayloadArray);
            }





        }
    }

    // This is where the client saves the payload requested file
    public void dealWithRequestedFile(long payloadId) {
        Log.d(TAG,"dealWithRequestedFile("+payloadId+")");
        boolean okToProceed = false;
        if (nearbyActions.getSyncNearbyFragment() != null) {
            boolean overwrite = nearbyActions.getSyncNearbyFragment().getOverwrite();
            Payload payload = nearbyActions.getNearbyTransferRecords().getIncomingFilePayloads().get(payloadId);

            Log.d(TAG,"payload:"+payload);
            if (payload!=null) {
                Log.d(TAG,"Client receiving dealWithRequestedFile()\npayload:"+payload+"\nbrowseHostFragment:"+ nearbyActions.getSyncNearbyFragment());
                String fileLocation = nearbyActions.getNearbyTransferRecords().getFileNewLocation().get(payloadId);

                // The file location will look like
                // ../FOLDER/SUBFOLDER_xx____xx_FILENAME;
                // Set the defaults
                String folder = "";
                String subfolder = "";
                String filename = "";

                if (fileLocation != null && fileLocation.contains("../") && fileLocation.contains(nearbyActions.songTag)) {
                    fileLocation = fileLocation.replace("../", "");
                    folder = fileLocation.substring(0, fileLocation.indexOf("/"));
                    folder = folder.replace("/", "");
                    fileLocation = fileLocation.replace(folder + "/", "");

                    // Get the filename
                    filename = fileLocation.substring(fileLocation.indexOf(nearbyActions.songTag));
                    filename = filename.replace(nearbyActions.songTag, "");
                    subfolder = fileLocation.replace(nearbyActions.songTag + filename, "");
                }

                if (!filename.isEmpty()) {
                    // Remove the ids from the arrays as no longer needed
                    nearbyActions.getNearbyTransferRecords().getIncomingFilePayloads().remove(payloadId);
                    nearbyActions.getNearbyTransferRecords().getFileNewLocation().remove(payloadId);

                    boolean receivedCurrentSet = false;
                    Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(folder, subfolder, filename);

                    String logFileLocation = folder + "/";
                    if (!subfolder.isEmpty()) {
                        logFileLocation += subfolder + "/";
                    }
                    logFileLocation += filename;


                    if (!mainActivityInterface.getStorageAccess().uriExists(uri) || overwrite) {
                        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uri, null, folder, subfolder, filename);

                        try {
                            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " requested file created at /" + folder + "/" + subfolder + "/" + filename);
                            if (uri != null && payload.asFile() != null) { // i.e. we have received the file by choice
                                ParcelFileDescriptor parcelFileDescriptor = (Objects.requireNonNull(payload.asFile())).asParcelFileDescriptor();
                                InputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                                Uri originalUri = Uri.parse(parcelFileDescriptor.getFileDescriptor().toString());
                                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " payloadFile copyFile from " + originalUri + " to " + uri);
                                if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                                    if (nearbyActions.getSyncNearbyFragment() !=null) {
                                        nearbyActions.getSyncNearbyFragment().addFilesCopied(logFileLocation);
                                        okToProceed = true;
                                    }
                                    if (nearbyReturnActionsInterface != null) {
                                        // If the file is being placed in the Songs folder, update the database
                                        if (folder.equals("Songs")) {
                                            // Make sure song is in the database (but not for received folder!)
                                            if (!mainActivityInterface.getSQLiteHelper().songExists(subfolder, filename)) {
                                                mainActivityInterface.getSQLiteHelper().createSong(subfolder, filename);

                                                // Set the filetype
                                                Song tempSong = new Song();
                                                tempSong.setFolder(subfolder);
                                                tempSong.setFilename(filename);
                                                mainActivityInterface.getLoadSong().doLoadSongFile(tempSong, false);

                                                if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("PDF",filename)) {
                                                    // Add to the NonOpenSongSongs.db
                                                    if (!mainActivityInterface.getNonOpenSongSQLiteHelper().songExists(subfolder,filename)) {
                                                        mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(subfolder, filename);
                                                    }
                                                    mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(tempSong);
                                                }
                                                mainActivityInterface.getSQLiteHelper().updateSong(tempSong);
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
                                            mainActivityInterface.notifySetFragment("clear", oldSize);

                                            // Set this as our current set
                                            mainActivityInterface.getSetActions().loadSets(uris, mainActivityInterface.getCurrentSet(),null);
                                        }
                                    }
                                } else {
                                    if (nearbyActions.getSyncNearbyFragment() !=null) {
                                        okToProceed = true;
                                        nearbyActions.getSyncNearbyFragment().addFilesFailed(logFileLocation);
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
                    } else if (nearbyActions.getSyncNearbyFragment() !=null) {
                        nearbyActions.getSyncNearbyFragment().addFilesSkipped(logFileLocation);
                        okToProceed = true;
                    }

                }
            }

            if (okToProceed && nearbyActions.getSyncNearbyFragment() !=null) {
                nearbyActions.getSyncNearbyFragment().continueGetFiles();
            }
        }
    }

*/


/*

*/





/*
    // Getting info on shareable songs/sets between hosts/sets
    public Uri createShareableObjectsForRequester() {
        // TODO

        // We have been asked to provide a list of shareable items
        // Only proceed if the users has allowed this!
        if (nearbyActions.getNearbyConnectionManagement().getNearbyFileSharing()) {
            // TODO Send a message to the requester that we're working on preparing a list of items available

            // Go through our songs and create an array of objects
            ArrayList<ShareableObject> shareableObjects = mainActivityInterface.getSQLiteHelper().getShareableSongs();

            // Now add the sets
            //ArrayList<String> sets = mainActivityInterface.getStorageAccess().listFilesInFolder("Sets", "");
                *//*for (String set:sets) {
                    ShareableObject shareableObject = new ShareableObject();
                    shareableObject.setFilename(set);
                    shareableObject.setFolder("../Sets");

                    // This is a newer method that parsers the set into a setObject first
                    //SetObject setObject = mainActivityInterface.getSetActions().createSetObjectFromFilename(set);
                    //shareableObject.setLastModified(setObject.getLastModified());
                    //shareableObject.setUuid(setObject.getUuid());
                    //shareableObject.setTitle(setObject.getSetName());

                    // Add object to the shareable sets
                    shareableObjects.add(shareableObject);
                }*//*

            // Now create a zip file and add these items together as json objects
            String jsonString = MainActivity.gson.toJson(shareableObjects);

            mainActivityInterface.getStorageAccess().doStringWriteToFile("Export","","nearbyShareableList.json",jsonString);
            return mainActivityInterface.getStorageAccess().getUriForItem("Export","","nearbyShareableList.json");

        } else {
            // TODO return a message to say that user has not allowed sharing of files
            // This should also stop their progress bar from spinning
            return null;
        }
        return null;
    }
*/
    // This is where the client deals with the list of items it has received back from the host
   /* public void payloadHostItems(String incoming) {
        // TODO
        // Remove the header and device id
        incoming = incoming.replaceFirst(nearbyActions.hostItems, "").replaceFirst(nearbyActions.getNearbyConnectionManagement().getDeviceId(), "").trim();
        String[] hostItems = incoming.split("\n");
        if (nearbyActions.getSyncNearbyFragment() != null) {
            if (mainActivityInterface.getWhattodo().equals("browsecurrentset")) {
                nearbyActions.getSyncNearbyFragment().setNearbyCurrentSet(incoming);
            }
            nearbyActions.getSyncNearbyFragment().displayHostItems(hostItems);
        }
    }

    */

    // If we are connected, we might be asked to return an list of items
    // This list will be built from the arraylists but passed as a string split by lines
    /*public String getHostItems(String what) {

        // TODO
        ArrayList<String> hostItems;
        switch (what) {
            case "browsesets":
                hostItems = mainActivityInterface.getStorageAccess().listFilesInFolder("Sets", "");
                // Add the current set to the list if it isn't empty
                if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
                    hostItems.add(0, "["+c.getString(R.string.set_current)+"]");
                }
                break;
            case "browseprofiles":
                hostItems = mainActivityInterface.getStorageAccess().listFilesInFolder("Profiles", "");
                break;
            case "browsesongs":
            default:
                hostItems = mainActivityInterface.getStorageAccess().getSongIDsFromFile();
                break;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String item : hostItems) {
            stringBuilder.append(item).append("\n");
        }
        return stringBuilder.toString().trim();


    }
*/




}

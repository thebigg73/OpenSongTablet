package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.MainActivity;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.NearbyReturnActionsInterface;
import com.garethevans.church.opensongtablet.preferences.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class NearbyReceivePayloads {

    // This class deals with receiving payloads from Nearby devices
    // Payloads are sent as NearbyJson bytes object
    // If they are bigger than 32kb, then we get a simplified version with file info, then the file

    // We only deal with received payloads if we haven't already received them and if we didn't send them
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "NearbyReceivePayloads";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final NearbyActions nearbyActions;

    private boolean nearbyReceiveHostFiles;
    private boolean nearbyReceiveHostAutoscroll;
    private boolean nearbyReceiveHostScroll;
    private boolean nearbyKeepHostFiles;
    private boolean nearbyMatchToPDFSong;
    private boolean nearbyMessageSticky;
    private boolean nearbyReceiveHostSongSections;
    private boolean forceReload = false;
    private String receivedSongFilename;
    private int pendingSection = 0;         // When a song is received, we also get the section
    private NearbyJson lastSongInfo = null;
    private Payload lastSongPayload = null;

    private final NearbyReturnActionsInterface nearbyReturnActionsInterface;

    NearbyReceivePayloads(Context c, NearbyActions nearbyActions) {
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
                    processPayload(endpointId, payload);
                } else if (payload.getType() == Payload.Type.FILE) {
                    if (payload.asFile()!=null) {
                        nearbyActions.getNearbyTransferRecords().setLastFileReceivedURI(Objects.requireNonNull(payload.asFile()).asUri());
                        // Keep a record of this for when it is complete
                        nearbyActions.getNearbyTransferRecords().addAlreadyReceivedPayload(payload);
                    }
                }
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                // Now we deal with the payload if the transfer was successful and the payload wasn't bytes (i.e. it was stored)
                if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS &&
                        nearbyActions.getNearbyTransferRecords().getAlreadyReceivedFilePayload(payloadTransferUpdate.getPayloadId())) {
                    // Send the information to the processing function
                    long thisId = payloadTransferUpdate.getPayloadId();
                    Payload payload = nearbyActions.getNearbyTransferRecords().getAlreadyReceivedPayload(thisId);
                    if (payload.asFile()!=null) {
                        nearbyActions.getNearbyTransferRecords().setLastFileReceivedURI(Objects.requireNonNull(payload.asFile()).asUri());
                    }
                    processPayload(endpointId, payload);
                }
            }
        };
    }

    private void processPayload(String endpointId, Payload payloadReceived) {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            Log.d(TAG,"Received payload from endpointId:"+endpointId);
            // Only process this if we haven't already received this payload
            String type = "FILE";
            if (payloadReceived.getType() == Payload.Type.BYTES) {
                type = "BYTES";
            }
            if (type.equals("FILE") || !nearbyActions.getNearbyTransferRecords().getAlreadyReceivedPayload(payloadReceived)) {
                // If we are a host, but passing info through, or a client on cluster mode.  Resend this if we haven't already - check
                if ((nearbyActions.getNearbyConnectionManagement().getIsHost() &&
                        nearbyActions.getNearbyConnectionManagement().getNearbyHostPassthrough()) ||
                        nearbyActions.getNearbyConnectionManagement().getNearbyStrategy() == Strategy.P2P_CLUSTER) {
                    if (nearbyActions.getNearbyTransferRecords().getNotAlreadySentPayload(payloadReceived)) {
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
                        NearbyJson nearbyJson;
                        try {
                            nearbyJson = MainActivity.gson.fromJson(json, NearbyJson.class);
                            if (nearbyJson.getId() == null) {
                                nearbyJson.setId(payloadReceived.getId());
                            }
                            // Only deal with this if this is for our device to action (or isn't set - for all)
                            if (nearbyJson.getDeviceToAction() == null ||
                                    nearbyJson.getDeviceToAction().equals(nearbyActions.getNearbyConnectionManagement().getDeviceId())) {
                                String what = nearbyJson.getWhat();
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
                                    } else if (what.startsWith(nearbyActions.scrollByTag)) {
                                        scrollByProportion(nearbyJson);
                                    } else if (what.startsWith(nearbyActions.scrollToTag)) {
                                        scrollToProportion(nearbyJson);
                                    } else if (what.startsWith(nearbyActions.messageTag)) {
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
                                        nearbyActions.getNearbyTransferRecords().addAlreadyReceivedFileInformation(nearbyJson);
                                        // If we are receiving the host files, we need to wait for the file as well
                                        // Because the file might arrive before the bytes, check and process if needed
                                        if (nearbyReceiveHostFiles) {
                                            lastSongInfo = nearbyJson;
                                            lastSongPayload = null;
                                            checkForFileReceived(nearbyJson.getId());
                                        } else {
                                            // We can just load our version of the song
                                            loadSongFromMyLibrary(nearbyJson);
                                            lastSongPayload = null;
                                            lastSongInfo = nearbyJson;
                                        }
                                    } else if (what.equals(nearbyActions.syncRequestInfo)) {
                                        // We have been asked to send our available item list to a connected device
                                        dealWithSyncRequestInfo(nearbyJson);
                                    } else if (what.equals(nearbyActions.syncProcessingInfo) ||
                                            what.equals(nearbyActions.syncProcessingContent)) {
                                        // We have been told by a connected device that they are processing our info/content request
                                        dealWithSyncRequestProcessingInfo(nearbyJson);
                                    } else if (what.equals(nearbyActions.syncRequestDenied)) {
                                        // The connected device has denied our request to sync
                                        dealWithSyncRequestDenied();
                                    } else if (what.equals(nearbyActions.syncReturnedInfo)) {
                                        // The connected device has sent us a list of available items for sync
                                        // Because the info could be bigger than BYTES allows,
                                        // We receive file info as a json BYTES and the content as a json FILE
                                        nearbyActions.getNearbyTransferRecords().addAlreadyReceivedFileInformation(nearbyJson);
                                        checkForFileReceived(nearbyJson.getId());
                                    } else if (what.equals(nearbyActions.syncRequestContent)) {
                                        // A connected device has created a request for some of our items
                                        // Because the info could be bigger than BYTES allows,
                                        // We receive file info as a json BYTES and the content as a json FILE
                                        nearbyActions.getNearbyTransferRecords().addAlreadyReceivedFileInformation(nearbyJson);
                                        checkForFileReceived(nearbyJson.getId());
                                    } else if (what.equals(nearbyActions.syncReturnedContent)) {
                                        // The connected device has sent us the items we requested
                                        // Because the info could be bigger than BYTES allows,
                                        // We receive file info as a json BYTES and the content as a json FILE
                                        nearbyActions.getNearbyTransferRecords().addAlreadyReceivedFileInformation(nearbyJson);
                                        checkForFileReceived(nearbyJson.getId());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (c != null && nearbyActions.getSyncNearbyFragment() != null) {
                                nearbyActions.getSyncNearbyFragment().updateProgressText(c.getString(R.string.connection_host_needs_update));
                            }
                        }
                    }

                    // Bytes are dealt with.  Keep a note that we've dealt with it
                    nearbyActions.getNearbyTransferRecords().addIncomingDealtWith(payloadReceived.getId(), true);


                    // Deal with payload files
                } else if (payloadReceived.getType() == Payload.Type.FILE) {
                    // Check if the FILE and BYTES info have both arrived.  If so, process the file
                    checkForFileReceived(payloadReceived.getId());
                }
            }
        });
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
                nearbyReturnActionsInterface != null && nearbyJson.getScrollProportion() != null) {
            nearbyReturnActionsInterface.doScrollByProportion(nearbyJson.getScrollProportion());
        }
    }
    private void scrollToProportion(NearbyJson nearbyJson) {
        // It sends the scrollProportion as a ratio of scrollAmount/songHeight
        if (nearbyReceiveHostScroll && mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance)) &&
                !nearbyActions.getNearbyConnectionManagement().getIsHost() &&
                nearbyReturnActionsInterface != null && nearbyJson.getScrollProportion() != null) {
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
    public void selectSection(NearbyJson nearbyJson) {
        if (!nearbyActions.getNearbyConnectionManagement().getIsHost() && nearbyJson.getSection() != null &&
                nearbyJson.getSection()>=0) {
            boolean onSectionAlready;
            int totalSections;
            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                onSectionAlready = mainActivityInterface.getSong().getPdfPageCurrent() == nearbyJson.getSection();
                totalSections = mainActivityInterface.getSong().getPdfPageCount();
            } else {
                onSectionAlready = mainActivityInterface.getSong().getCurrentSection() == nearbyJson.getSection();
                totalSections = mainActivityInterface.getSong().getPresoOrderSongSections().size();
            }
            if ((!onSectionAlready || mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter))) && nearbyReturnActionsInterface != null
                    && totalSections > nearbyJson.getSection()) {
                if (mainActivityInterface.getSong().getFiletype().equals("PDF")||mainActivityInterface.getStorageAccess().isSpecificFileExtension("PDF",mainActivityInterface.getSong().getFilename())) {
                    mainActivityInterface.getSong().setPdfPageCurrent(nearbyJson.getSection());
                } else {
                    mainActivityInterface.getSong().setCurrentSection(nearbyJson.getSection());
                }
                nearbyReturnActionsInterface.selectSection(nearbyJson.getSection());
            }
        }
    }

    // Load songs - either using the info, or the file sent
    private void loadSongFromMyLibrary(NearbyJson nearbyJson) {
        setForceReload(true);
        // We just want to trigger loading the song on our device (if we have it).
        // If not, we get notified it doesn't exits
        if (nearbyReturnActionsInterface != null && !nearbyActions.getNearbyConnectionManagement().getIsHost()) {
            if (nearbyJson.getFolder() != null && nearbyJson.getFilename() != null) {
                mainActivityInterface.getSong().setFolder(nearbyJson.getFolder());
                mainActivityInterface.getSong().setFilename(nearbyJson.getFilename());
                setSongSection(mainActivityInterface.getSong(),nearbyJson.getSection());
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

                if (needToTempTranspose) {
                    Song quickSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(
                            mainActivityInterface.getSong().getFolder(),
                            mainActivityInterface.getSong().getFilename());
                    quickSong = mainActivityInterface.getVariations().makeKeyVariation(quickSong, nearbyJson.getKey(), false, false);
                    quickSong.setFolder(mainActivityInterface.getVariations().getKeyVariationsFolder());
                    quickSong.setFilename(mainActivityInterface.getVariations().getKeyVariationFilename(mainActivityInterface.getSong().getFolder(), mainActivityInterface.getSong().getFilename(), nearbyJson.getKey()));
                    // Save the temp song
                    mainActivityInterface.getStorageAccess().writeSongFile(quickSong);
                    mainActivityInterface.getSong().setFolder(quickSong.getFolder());
                    mainActivityInterface.getSong().setFilename(quickSong.getFilename());
                    // Set the song section
                    setSongSection(mainActivityInterface.getSong(), nearbyJson.getSection());
                }
                if (!nearbyActions.getNearbyReceivePayloads().nearbyReceiveHostFiles) {
                    mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
                }
                // Only load the song if we aren't in a settings window
                if (!mainActivityInterface.getSettingsOpen()) {
                    nearbyReturnActionsInterface.loadSong(true);
                }
            }
        }
    }
    private void loadSongFromReceivedXML(NearbyJson nearbyJson) {
        if (nearbyReturnActionsInterface != null && !nearbyActions.getNearbyConnectionManagement().getIsHost() &&
                nearbyReceiveHostFiles) {
            setForceReload(true);
            if (nearbyJson.getXml() != null) {
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
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " creating temporary song file from XML received from connected host:  " + folder + "/" + subfolderforuri + "/" + filename);
                //mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, newLocation, null, folder, subfolderforuri, filename);
                //OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(newLocation);
                mainActivityInterface.getSong().setFolder(subfolder);
                mainActivityInterface.getSong().setFilename(filename);

                // IV - Store the received song filename in case the user wants to duplicate the received song
                receivedSongFilename = nearbyJson.getFilename();

                // Write the file to the desired output stream and load
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " write the file content: " + newLocation + " with: " + nearbyJson.getXml());

                if (mainActivityInterface.getStorageAccess().writeFileFromString(folder, subfolderforuri, filename, nearbyJson.getXml())) {
                    // Set the song section
                    setSongSection(mainActivityInterface.getSong(), nearbyJson.getSection());
                    if (nearbyJson.getSwipeDirection() != null) {
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

                    if (!nearbyActions.getNearbyReceivePayloads().nearbyReceiveHostFiles) {
                        mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
                    }
                    if (!mainActivityInterface.getSettingsOpen()) {
                        nearbyReturnActionsInterface.loadSong(true);
                    }
                }
            } else {
                // No XML sent as we are awaiting a file.  Check for arrival
                checkForFileReceived(nearbyJson.getId());
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
        if (payloadFile != null) {
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

            outputUri = mainActivityInterface.getStorageAccess().getUriForItem(songFolder, songSubfolder, filename);
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, outputUri, null, songFolder, songSubfolder, filename);

            try {
                // Copy the file to a new location.
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(inputUri);
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(outputUri);
                mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);

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
                // Set the song section
                setSongSection(mainActivityInterface.getSong(), nearbyJson.getSection());
                mainActivityInterface.getDisplayPrevNext().setSwipeDirection(nearbyJson.getSwipeDirection());
                setForceReload(true);
                if (!nearbyActions.getNearbyReceivePayloads().nearbyReceiveHostFiles) {
                    mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
                }
                if (!mainActivityInterface.getSettingsOpen()) {
                    nearbyReturnActionsInterface.loadSong(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Delete the original file.
                c.getContentResolver().delete(inputUri, null, null);
            }
        }
    }

    private void setSongSection(Song song, Integer getSection) {
        // If null, set to 0, the first section
        pendingSection = getSection==null ? 0 : getSection;
        if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("PDF",song.getFilename())) {
            song.setPdfPageCurrent(pendingSection);
        } else {
            song.setCurrentSection(pendingSection);
        }
    }

    // A check that both BYTES file info and FILE content has been received
    private void checkForFileReceived(Long payloadId) {
        // Check for payload file and matching bytes
        Payload filePayload = nearbyActions.getNearbyTransferRecords().getAlreadyReceivedPayload(payloadId);
        NearbyJson fileInfo = nearbyActions.getNearbyTransferRecords().getAlreadyReceivedFileInformation(payloadId);

        if (filePayload != null && fileInfo != null && nearbyActions.getNearbyTransferRecords().getLastFileReceivedURI()!=null) {
            // Both have arrived!!!
            // Add a record that we have dealt with them and set calls to remove the records after a delay
            nearbyActions.getNearbyTransferRecords().addIncomingDealtWith(payloadId, true);
            nearbyActions.getNearbyTransferRecords().removeAlreadyDealtWith(payloadId);
            nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(filePayload.getId());
            if (fileInfo.getId() != null) {
                nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(fileInfo.getId());
            }

            if (fileInfo.getWhat() != null) {
                if (fileInfo.getWhat().equals(nearbyActions.fileTag)) {
                    lastSongPayload = filePayload;
                    lastSongInfo = fileInfo;
                    loadSongFromReceivedFile(filePayload, fileInfo.getFolder(), fileInfo.getFilename(), fileInfo);
                } else if (fileInfo.getWhat().equals(nearbyActions.syncReturnedInfo)) {
                    dealWithSyncInfoReturned(nearbyActions.getNearbyTransferRecords().getLastFileReceivedURI(),
                            filePayload.getId(), fileInfo, fileInfo.getId());
                } else if (fileInfo.getWhat().equals(nearbyActions.syncRequestContent)) {
                    dealWithSyncRequestForContent(nearbyActions.getNearbyTransferRecords().getLastFileReceivedURI(),
                            filePayload.getId(), fileInfo, fileInfo.getId());
                } else if (fileInfo.getWhat().equals(nearbyActions.syncReturnedContent)) {
                    dealWithSyncContentReturned(nearbyActions.getNearbyTransferRecords().getLastFileReceivedURI(),
                            filePayload.getId(), fileInfo, fileInfo.getId());
                }
            }
        }
    }

    // Deal with synchronisation actions
    private void dealWithSyncRequestInfo(NearbyJson nearbyJson) {
        // This device has been asked for information on available sync items
        String requestingDevice = nearbyJson.getDeviceSending();
        // Only action this if we haven't already done so
        if (nearbyJson.getId() == null || !nearbyActions.getNearbyTransferRecords().getAlreadyReceivedFileInformation(nearbyJson)) {
            if (nearbyJson.getId() != null) {
                nearbyActions.getNearbyTransferRecords().addAlreadyReceivedFileInformation(nearbyJson);
            }
            // Send a message back to tell them we are processing it, or that we have denied it
            nearbyActions.getNearbySendPayloads().sendSyncProcessingInfo(requestingDevice);
        }
    }

    private void dealWithSyncRequestProcessingInfo(NearbyJson nearbyJson) {
        // Check we haven't dealt with this already
        if (nearbyJson.getId() == null || !nearbyActions.getNearbyTransferRecords().getAlreadyReceivedFilePayload(nearbyJson.getId())) {
            if (nearbyJson.getId() != null) {
                nearbyActions.getNearbyTransferRecords().addAlreadyReceivedFileInformation(nearbyJson);
            }
            // The device we requested info from is processing!  Let the user know
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.sync_device_processing));
            if (nearbyActions.getSyncNearbyFragment() != null) {
                nearbyActions.getSyncNearbyFragment().showProgress(false);
            }
        }
    }

    private void dealWithSyncRequestDenied() {
        // The device we tried to sync with has denied nearbyFileSharing
        mainActivityInterface.getShowToast().doIt(c.getString(R.string.sync_device_denied));
        if (nearbyActions.getSyncNearbyFragment() != null) {
            nearbyActions.getSyncNearbyFragment().showProgress(false);
        }
    }

    private void dealWithSyncInfoReturned(Uri payloadFileUri, long payloadFileId, NearbyJson fileInfo, Long payloadId) {
        // The connected device has sent us information!
        // We can clear the received info
        if (payloadFileUri != null) {
            // Copy the file to our storage Received
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(payloadFileUri);
            Uri outputUri = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", fileInfo.getFilename());
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, outputUri, null, "Received", "", fileInfo.getFilename());
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(outputUri);
            if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                inputStream = mainActivityInterface.getStorageAccess().getInputStream(outputUri);
                NearbyJson nearbyJson;
                try {
                    nearbyJson = MainActivity.gson.fromJson(
                            mainActivityInterface.getStorageAccess().readTextFileToString(inputStream), NearbyJson.class);
                } catch (Exception e) {
                    mainActivityInterface.getShowToast().doIt(c.getString(R.string.sync_content_error));
                    nearbyJson = null;
                    e.printStackTrace();
                }

                // Now we can deal with this content
                if (nearbyActions.getSyncNearbyFragment() != null && nearbyJson!=null) {
                    nearbyActions.getSyncNearbyFragment().dealWithNearbyInfoReceived(nearbyJson);
                }

                nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(payloadFileId);
                nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedFileInformation(fileInfo.getId());
                nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(payloadId);
            }
        }
    }

    private void dealWithSyncRequestForContent(Uri payloadFileUri, long payloadFileId, NearbyJson fileInfo, Long payloadId) {
        // The connected device has sent us a request for some files (song, sets or profiles)
        // We can clear the received info
        // Remove the record of this as we are now dealing with it
        nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(payloadFileId);
        nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedFileInformation(fileInfo.getId());
        nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(payloadId);

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            if (payloadFileUri != null) {
                // Copy the file to our storage Received
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(payloadFileUri);
                Uri outputUri = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", fileInfo.getFilename());
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, outputUri, null, "Received", "", fileInfo.getFilename());
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(outputUri);
                NearbyJson nearbyJson;
                if (mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                    inputStream = mainActivityInterface.getStorageAccess().getInputStream(outputUri);
                    nearbyJson = MainActivity.gson.fromJson(
                            mainActivityInterface.getStorageAccess().readTextFileToString(inputStream), NearbyJson.class);

                    nearbyActions.getNearbyTransferRecords().addAlreadyReceivedFileInformation(fileInfo);
                    String filename = fileInfo.getFilename();
                    String deviceRequesting = fileInfo.getDeviceSending();

                    if (filename != null && nearbyJson != null) {
                        // Send a message back to tell them we are processing it
                        NearbyJson messageBackJson = new NearbyJson();
                        messageBackJson.setWhat(nearbyActions.syncProcessingContent);
                        messageBackJson.setDeviceSending(nearbyActions.getNearbyConnectionManagement().getDeviceId());
                        messageBackJson.setDeviceToAction(deviceRequesting);
                        nearbyActions.getNearbySendPayloads().sendPayloadToSelected(deviceRequesting,
                                Payload.fromBytes(MainActivity.gson.toJson(messageBackJson).getBytes()));

                        // Now, we can deal with the actual task and send that when ready
                        String filenameToUse = fileInfo.getFilename();
                        if (filenameToUse!=null) {
                            if (filenameToUse.equals(nearbyActions.requestProfilesFile)) {
                                filenameToUse = nearbyActions.contentZipProfiles;
                            } else if (filenameToUse.equals(nearbyActions.requestSetsFile)) {
                                filenameToUse = nearbyActions.contentZipSets;
                            } else {
                                filenameToUse = nearbyActions.contentZipSongs;
                            }
                        }
                        nearbyActions.getNearbySendPayloads().sendSyncContent(nearbyJson,filenameToUse);
                    }
                }
                // Remove the record of this payload
                nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(payloadFileId);
                nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedFileInformation(fileInfo.getId());
                nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(payloadId);
            }
        });
    }


    private void dealWithSyncContentReturned(Uri payloadFileUri, long payloadFileId, NearbyJson fileInfo, Long payloadId) {
        // We have received a zip file.  We copy it to our Received folder
        if (payloadFileUri != null) {
            // Copy the file to our storage Received
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(payloadFileUri);
            // Create a zip file in our Received folder
            Uri outputUri = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", fileInfo.getFilename());
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, outputUri, null, "Received", "", fileInfo.getFilename());
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(outputUri);
            if (fileInfo.getFilename()!=null && mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream)) {
                String what = null;
                if (fileInfo.getFilename().equals(nearbyActions.contentZipProfiles)) {
                    what = "profiles";
                } else if (fileInfo.getFilename().equals(nearbyActions.contentZipSets)) {
                    what = "sets";
                } else if (fileInfo.getFilename().equals(nearbyActions.contentZipSongs)){
                    what = "songs";
                }
                if (what!=null) {
                    // Give the user a chance to cancel - are you sure?
                    ArrayList<String> arguments = new ArrayList<>();
                    arguments.add(String.valueOf(outputUri));
                    arguments.add(what);
                    AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("syncNearbyZip",c.getString(R.string.overwrite_info),arguments,"NearbyReceivePayloads",nearbyActions.getSyncNearbyFragment(),null);
                    areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"AreYouSureBottomSheet");
                    //nearbyActions.getSyncNearbyFragment().doExtractFromZip(zipUri, what);
                } else {
                    mainActivityInterface.getShowToast().error();
                }
            } else {
                mainActivityInterface.getShowToast().error();
            }
            nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(payloadFileId);
            nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedFileInformation(fileInfo.getId());
            nearbyActions.getNearbyTransferRecords().removeAlreadyReceivedPayload(payloadId);
        }
    }


    // Host pending sections (for delayed load)
    public int getPendingSection() {
        return pendingSection;
    }
    public void resetPendingSection() {
        this.pendingSection = 0;
    }
    public void setPendingSection(int sectionNumber) {
        this.pendingSection = sectionNumber;
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
    public boolean getNearbyReceiveHostSongSections() {
        return nearbyReceiveHostSongSections;
    }


    // The setters (also saves the preference if required
    public void setNearbyReceiveHostFiles(boolean nearbyReceiveHostFiles) {
        this.nearbyReceiveHostFiles = nearbyReceiveHostFiles;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyReceiveHostFiles",nearbyReceiveHostFiles);
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


    public void loadLastSong() {
        if (lastSongInfo!=null) {
            if (nearbyReceiveHostFiles && lastSongPayload!=null) {
                loadSongFromReceivedFile(lastSongPayload,lastSongInfo.getFolder(), lastSongInfo.getFilename(), lastSongInfo);
            } else {
                // We can just load our version of the song
                loadSongFromMyLibrary(lastSongInfo);
            }
        }
    }

    public String getReceivedSongFilename() {
        return receivedSongFilename;
    }

}

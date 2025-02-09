package com.garethevans.church.opensongtablet.openchords;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsOpenchordsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class OpenChordsFragment extends Fragment {
    // This class is where we trigger sync with the OpenChords server

    private final String TAG = "OpenChordsFragment";
    private MainActivityInterface mainActivityInterface;
    private String openchords_string = "", webAddress = "", folder_doesnt_exist_string = "",
            folder_created_on_download_string = "", folder_exists_but_is_different_string = "",
            processing_string = "", updating_string = "", skipping_string = "", creating_string = "",
            removing_string = "", openchords_comparing_string = "", openchords_no_changes_string = "",
            openchords_folder_doesnt_exist_string = "";
    private SettingsOpenchordsBinding myView;
    private ArrayList<String> folders;
    private boolean doDownload = false, doUpload = false;
    private final String songStart = "start__", songEnd ="__end";

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(openchords_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.getOpenChordsAPI().setOpenChordsFragment(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsOpenchordsBinding.inflate(inflater, container, false);

        prepareStrings();
        setupViews();
        setupListeners();

        mainActivityInterface.getOpenChordsAPI().setIsServerResponse(true);
        queryOpenChordsServer();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext() != null) {
            openchords_string = getString(R.string.openchords);
            webAddress = getString(R.string.website_openchords);
            folder_doesnt_exist_string = getString(R.string.folder_doesnt_exist);
            openchords_folder_doesnt_exist_string = getString(R.string.openchords_folder_doesnt_exist);
            folder_created_on_download_string = getString(R.string.folder_created_on_download);
            folder_exists_but_is_different_string = getString(R.string.folder_exists_but_is_different);
            processing_string = getString(R.string.processing);
            updating_string = getString(R.string.sync_updating);
            skipping_string = getString(R.string.openchords_skipping);
            creating_string = getString(R.string.sync_creating);
            removing_string = getString(R.string.openchords_deleting);
            openchords_comparing_string = getString(R.string.openchords_comparing);
            openchords_no_changes_string = getString(R.string.sync_no_changes_required);
        }
    }

    private void setupViews() {
        // Get the folders available on the device
        folders = mainActivityInterface.getSQLiteHelper().getFolders();
        if (getContext() != null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.folderToSync, R.layout.view_exposed_dropdown_item, folders);
            myView.folderToSync.setAdapter(exposedDropDownArrayAdapter);
            myView.folderToSync.setText(mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName());
            Glide.with(getContext()).load(mainActivityInterface.getOpenChordsAPI().getOpenChordsQRCode()).into(myView.openChordsQRImage);
            checkFolderMessage();
        }

        // TODO if we have got here from a link, we query the server to get the folder name
        // If it doesn't exist on our device, let the user know and hide the upload button

    }

    private void setupListeners() {
        myView.folderToSync.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Update our choice and get the new uuid
                if (getContext() != null && myView != null && myView.folderToSync.getText() != null &&
                        !mainActivityInterface.getOpenChordsAPI().getIsServerResponse()) {
                    // Reset the openChordsFolder object to force checking
                    mainActivityInterface.getOpenChordsAPI().setOpenChordsFolderName(myView.folderToSync.getText().toString());
                    Glide.with(getContext()).load(mainActivityInterface.getOpenChordsAPI().getOpenChordsQRCode()).into(myView.openChordsQRImage);
                    mainActivityInterface.getOpenChordsAPI().getFolderContentsFromUUID();
                    checkFolderMessage();
                }
                mainActivityInterface.getOpenChordsAPI().setIsServerResponse(false);
            }
        });
        myView.openChordsQRImage.setOnClickListener(view -> mainActivityInterface.openDocument(mainActivityInterface.getOpenChordsAPI().getOpenChordsAddress()));
        myView.differencesOpenChordsLayout.setOnClickListener(view -> {
            OpenChordsBottomSheet openChordsBottomSheet = new OpenChordsBottomSheet(this,"differences");
            openChordsBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "OpenChordsBottomSheet");
        });
        myView.downloadLayout.setOnClickListener(view -> {
            OpenChordsBottomSheet openChordsBottomSheet = new OpenChordsBottomSheet(this,"download");
            openChordsBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "OpenChordsBottomSheet");
        });
        myView.uploadLayout.setOnClickListener(view -> {
            OpenChordsBottomSheet openChordsBottomSheet = new OpenChordsBottomSheet(this,"upload");
            openChordsBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "OpenChordsBottomSheet");
        //    mainActivityInterface.getOpenChordsAPI().prepareMyTagsJson();
        });
        myView.linkLayout.setOnClickListener(view -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mainActivityInterface.getOpenChordsAPI().getOpenChordsAddress());
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });
    }

    public void openChordsFolderNotFound() {
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView != null) {
                myView.folderMessage.setText(openchords_folder_doesnt_exist_string);
                myView.downloadLayout.setVisibility(View.GONE);
                changeButtonsEnable(true);
            }
        });
    }

    public void changeButtonsEnable(boolean enable) {
        myView.folderToSync.setEnabled(enable);
        myView.uploadLayout.setEnabled(enable);
        myView.downloadLayout.setEnabled(enable);
        myView.linkLayout.setEnabled(enable);
        myView.scrimOverlay.setVisibility(enable ? View.GONE : View.VISIBLE);
        myView.progressLayout.setVisibility(enable ? View.GONE : View.VISIBLE);
    }

    private void checkFolderMessage() {
        if (folders == null) {
            folders = mainActivityInterface.getSQLiteHelper().getFolders();
        }
        if (folders != null && myView.folderToSync.getText() != null) {
            String folderToSync = myView.folderToSync.getText().toString();
            if (folders != null && !folders.contains(folderToSync)) {
                String message = folder_doesnt_exist_string + ". " +
                        folder_created_on_download_string;
                myView.folderMessage.setText(message);
                myView.uploadLayout.setVisibility(View.GONE);
            } else if (folders != null && folders.contains(folderToSync) &&
                    !mainActivityInterface.getStorageAccess().getUUIDForSongFolder(folderToSync).equals(mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderUuid())) {
                // We already have the folder, but it has a different UUID
                // Warn the user that downloading will replace the content of this folder
                myView.folderMessage.setText(folder_exists_but_is_different_string);
                myView.uploadLayout.setVisibility(View.GONE);
            } else {
                myView.folderMessage.setText("");
                myView.uploadLayout.setVisibility(View.VISIBLE);
            }
        } else {
            myView.folderMessage.setText("");
            myView.uploadLayout.setVisibility(View.VISIBLE);
        }
    }

    public void updateFolderTitle(String title) {
        Log.d(TAG, "updateFolderTitle(" + title + ")");
        mainActivityInterface.getMainHandler().post(() -> {
            mainActivityInterface.getOpenChordsAPI().setIsServerResponse(true);
            myView.folderToSync.setText(title);
            mainActivityInterface.getOpenChordsAPI().setIsServerResponse(false);
            checkFolderMessage();
            changeButtonsEnable(true);
        });
    }

    public void queryOpenChordsServer() {
        // Use the folder chosen to query the server and get the results
        if (myView != null && myView.folderToSync.getText() != null) {
            changeButtonsEnable(false);
            doDownload = false;
            doUpload = false;
            if (!mainActivityInterface.getWhattodo().equals("openchordsintent")) {
                mainActivityInterface.getOpenChordsAPI().setOpenChordsFolderName(myView.folderToSync.getText().toString());
            }
            mainActivityInterface.getOpenChordsAPI().getFolderContentsFromUUID();
        }
    }

    // We are sent here after hearing back from the server
    public void logChanges() {
        if (mainActivityInterface.getOpenChordsAPI().getServerFolder() != null) {
            // Do this on a new thread
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                String folderName = mainActivityInterface.getOpenChordsAPI().getServerFolder().getTitle();
                String localFolderName = mainActivityInterface.getStorageAccess().getSongFolderForUUID(null,mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderUuid());
                Log.d(TAG,"localFolderName:"+localFolderName);
                if (mainActivityInterface.getOpenChordsAPI().getServerSongs() != null) {





                }

                mainActivityInterface.getMainHandler().post(() -> {
                    if (myView != null) {
                        changeButtonsEnable(true);
                        myView.differencesCount.setText(String.valueOf(mainActivityInterface.getOpenChordsAPI().getTotalChanges()));
                    }
                });
            });
        }
    }

    private String songEntryString(String songtitle) {
        return "song:" + songtitle.trim() + "\n";
    }

    public void downloadFolder() {
        changeButtonsEnable(false);

        // Do this on a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {

                });

        /*if (mainActivityInterface.getOpenChordsAPI().getTotalChanges()==0) {
            mainActivityInterface.getShowToast().doIt(openchords_no_changes_string);
        } else if (mainActivityInterface.getOpenChordsAPI().getServerFolder() != null) {
            changeButtonsEnable(false);

            // Do this on a new thread
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {

                // Make sure we actually have the folder in local storage!
                if (folders != null && mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName() != null &&
                        !folders.contains(mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName())) {
                    Log.d(TAG, "folder didn't exist on local, so create");
                    mainActivityInterface.getStorageAccess().createFolder("Songs", "",
                            mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName(), false);
                    // Now also make sure we have an entry in the UUID.txt file
                    mainActivityInterface.getStorageAccess().checkSongFolderUUIDExist(
                            mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName(),
                            mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderUuid());
                    Log.d(TAG, "updated uuid file");
                }

                Song existingSong = null;
                String folder = mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName();

                // Let's work through the items we need to download from the server
                for (OpenChordsSong openChordsSong : mainActivityInterface.getOpenChordsAPI().getServerSongs()) {
                    boolean dealtwith = false;
                    for (OpenChordsCompareObject notOnLocalObject : mainActivityInterface.getOpenChordsAPI().getNotOnLocal()) {
                        if (notOnLocalObject.getUuid().equals(openChordsSong.getId())) {
                            // Add this songs to the local folder
                            String title = notOnLocalObject.getTitle();
                            String filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title);
                            updateProgress(openChordsSong.getTitle() + ": " + creating_string);
                            Log.d(TAG, "Song:" + openChordsSong.getTitle() + " didn't exist, so create it");
                            Uri songUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                                    folder, filename);
                            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, songUri, null,
                                    "Songs", folder, filename);
                            Log.d(TAG, "songUri:" + songUri);
                            existingSong = new Song();
                            existingSong.setFolder(folder);
                            existingSong.setFilename(filename);
                            existingSong.setTitle(title);
                            existingSong.setLastModified(notOnLocalObject.getLastModified());
                            existingSong = mainActivityInterface.getOpenChordsAPI().updateOpenSongSong(existingSong, openChordsSong);
                            // Save the song
                            mainActivityInterface.getSQLiteHelper().createSong(folder, filename);
                            mainActivityInterface.getSaveSong().setResetLastModified(false);
                            mainActivityInterface.getSaveSong().updateSong(existingSong, false);
                            mainActivityInterface.getSaveSong().setResetLastModified(true);
                            dealtwith = true;
                            break;
                        }
                    }

                    if (!dealtwith) {
                        for (OpenChordsCompareObject localOlderObject : mainActivityInterface.getOpenChordsAPI().getLocalOlder()) {
                            if (localOlderObject.getUuid().equals(openChordsSong.getId())) {
                                // Update these songs on the local folder
                                String title = localOlderObject.getTitle();
                                String filename = mainActivityInterface.getStorageAccess().removeWhiteSpaceFromFilename(title);
                                updateProgress(openChordsSong.getTitle() + ": " + updating_string);
                                existingSong = new Song();
                                existingSong.setFolder(folder);
                                existingSong.setFilename(filename);
                                existingSong.setTitle(title);
                                existingSong = mainActivityInterface.getOpenChordsAPI().updateOpenSongSong(existingSong, openChordsSong);
                                Log.d(TAG, "updating song:" + filename);
                                mainActivityInterface.getSaveSong().setResetLastModified(false);
                                mainActivityInterface.getSaveSong().updateSong(existingSong, false);
                                mainActivityInterface.getSaveSong().setResetLastModified(true);
                                break;
                            }
                        }
                    }
                }

                // Now delete the songs we have on our local folder that aren't on the server
                if (!mainActivityInterface.getOpenChordsAPI().getNotOnServer().isEmpty()) {
                    for (OpenChordsCompareObject openChordsCompareObject : mainActivityInterface.getOpenChordsAPI().getNotOnServer()) {
                        // Delete this song from our local folder as it isn't on the server
                        Log.d(TAG,"deleting song as not on server:"+openChordsCompareObject.getTitle());
                        *//*mainActivityInterface.getStorageAccess().doDeleteFile("Songs",
                                mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName(),
                                openChordsCompareObject.getTitle());
                        mainActivityInterface.getSQLiteHelper().deleteSong(
                                mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName(),
                                openChordsCompareObject.getTitle());*//*
                    }
                }

                // Now update the song menu
                mainActivityInterface.getSongListBuildIndex().setIndexRequired(true);
                mainActivityInterface.getSongListBuildIndex().setFullIndexRequired(true);
                mainActivityInterface.fullIndex();

                // Now query the server again to compare
                queryOpenChordsServer();

            });
        }*/
    }

    public void updateProgress(String progress) {
        if (myView != null) {
            myView.progressText.post(() -> {
                try {
                    myView.progressText.setText(progress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivityInterface.getOpenChordsAPI().setOpenChordsFragment(null);
        mainActivityInterface.getOpenChordsAPI().clearSyncObjects();
    }
}

package com.garethevans.church.opensongtablet.openchords;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class OpenChordsFragment extends Fragment {
    // This class is where we trigger sync with the OpenChords server

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "OpenChordsFragment";
    private MainActivityInterface mainActivityInterface;
    private String openchords_string = "";
    private String webAddress = "";
    private String folder_doesnt_exist_string = "";
    private String folder_created_on_download_string = "";
    private String folder_exists_but_is_different_string = "";
    private String openchords_folder_doesnt_exist_string = "";
    private String sync_no_changes_required_string ="";
    private String wait_string = "";
    private String index_songs_wait_string = "";
    private String sync_querying_remote_string = "";
    private SettingsOpenchordsBinding myView;
    private ArrayList<String> folders;
    private boolean folderChangedProgrammatically = false;
    private Handler checkQueryHandler = new Handler();
    private Runnable checkQueryRunnable;

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

        changeButtonsEnable(false);
        updateProgress(wait_string+"\n");
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
            wait_string = getString(R.string.wait);
            sync_no_changes_required_string = getString(R.string.sync_no_changes_required);
            sync_querying_remote_string = getString(R.string.sync_querying_remote);
            index_songs_wait_string = getString(R.string.index_songs_wait);
            checkQueryRunnable = () -> {
                if (mainActivityInterface!=null && mainActivityInterface.getSongListBuildIndex()!=null &&
                        mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                    // Keep the user posted
                    String progressText = index_songs_wait_string;
                    if (mainActivityInterface.getSongMenuFragment() != null) {
                        MaterialTextView progressView = mainActivityInterface.getSongMenuFragment().getProgressText();
                        if (progressView != null && progressView.getText() != null) {
                            progressText += "\n" + progressView.getText().toString();
                        }
                        updateProgress(progressText);
                        checkQueryHandler.postDelayed(checkQueryRunnable, 100);
                    }
                } else {
                    queryOpenChordsServer();
                }
            };
        }
    }

    private void setupViews() {
        // Get the folders available on the device
        ArrayList<String> allFolders = mainActivityInterface.getSQLiteHelper().getFolders();
        // Because we might have sub/sub folders, only allow those without the / in them
        folders = new ArrayList<>();
        for (String folder : allFolders) {
            if (!folder.contains("/")) {
                folders.add(folder);
            }
        }
        if (getContext() != null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.folderToSync, R.layout.view_exposed_dropdown_item, folders);
            myView.folderToSync.setAdapter(exposedDropDownArrayAdapter);
            // Setting this text here triggers the server query
            myView.folderToSync.setText(mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName());
            Glide.with(getContext()).load(mainActivityInterface.getOpenChordsAPI().getOpenChordsQRCode()).into(myView.openChordsQRImage);
            //checkFolderMessage();
        }
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
                    if (!folderChangedProgrammatically) {
                        mainActivityInterface.getOpenChordsAPI().setOpenChordsFolderName(myView.folderToSync.getText().toString());
                        Glide.with(getContext()).load(mainActivityInterface.getOpenChordsAPI().getOpenChordsQRCode()).into(myView.openChordsQRImage);
                    }
                    checkFolderMessage();
                }
                mainActivityInterface.getOpenChordsAPI().setIsServerResponse(false);
                if (!folderChangedProgrammatically) {
                    queryOpenChordsServer();
                }
                folderChangedProgrammatically = false;
            }
        });
        myView.openChordsQRImage.setOnClickListener(view -> mainActivityInterface.openDocument(mainActivityInterface.getOpenChordsAPI().getOpenChordsAddress()));
        myView.refresh.setOnClickListener(view -> {
            if (mainActivityInterface.getSongListBuildIndex()!=null && mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                checkQueryHandler.removeCallbacks(checkQueryRunnable);
                checkQueryHandler.post(checkQueryRunnable);
            } else {
                queryOpenChordsServer();
            }
        });
        myView.downloadLayout.setOnClickListener(view -> {
            if (mainActivityInterface.getSongListBuildIndex()!=null && mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                checkQueryHandler.removeCallbacks(checkQueryRunnable);
                checkQueryHandler.post(checkQueryRunnable);
            } else {
                OpenChordsBottomSheet openChordsBottomSheet = new OpenChordsBottomSheet(this, "download");
                openChordsBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "OpenChordsBottomSheet");
            }
        });
        myView.uploadLayout.setOnClickListener(view -> {
            if (mainActivityInterface.getSongListBuildIndex()!=null && mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                checkQueryHandler.removeCallbacks(checkQueryRunnable);
                checkQueryHandler.post(checkQueryRunnable);
            } else {
                OpenChordsBottomSheet openChordsBottomSheet = new OpenChordsBottomSheet(this, "upload");
                openChordsBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "OpenChordsBottomSheet");
            }
        });
        myView.linkLayout.setOnClickListener(view -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mainActivityInterface.getOpenChordsAPI().getOpenChordsAddress());
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        });
        myView.forceChanges.setOnClickListener(view -> {
            if (mainActivityInterface.getSongListBuildIndex()!=null && mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                checkQueryHandler.removeCallbacks(checkQueryRunnable);
                checkQueryHandler.post(checkQueryRunnable);
            } else {
                OpenChordsForceBottomSheet openChordsForceBottomSheet = new OpenChordsForceBottomSheet(this);
                openChordsForceBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "OpenChordsForceBottomSheet");
            }
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
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView != null) {
                myView.folderToSync.setEnabled(enable);
                myView.uploadLayout.setEnabled(enable);
                myView.downloadLayout.setEnabled(enable);
                if (!enable) {
                    myView.uploadLayout.setVisibility(View.GONE);
                    myView.downloadLayout.setVisibility(View.GONE);
                }
                myView.linkLayout.setEnabled(enable);
                myView.scrimOverlay.setVisibility(enable ? View.GONE : View.VISIBLE);
                myView.progressLayout.setVisibility(enable ? View.GONE : View.VISIBLE);
            }
        });
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
            } else if (mainActivityInterface.getOpenChordsAPI().getServerFolder()==null) {
                // The remote folder isn't there
                openChordsFolderNotFound();

            } else {
                myView.folderMessage.setText("");
                myView.downloadLayout.setVisibility(View.VISIBLE);
                myView.uploadLayout.setVisibility(View.VISIBLE);
            }
        } else if (mainActivityInterface.getOpenChordsAPI().getServerFolder()==null) {
            openChordsFolderNotFound();

        } else {
            myView.folderMessage.setText("");
            myView.uploadLayout.setVisibility(View.VISIBLE);
        }
    }

    public void updateFolderTitle(String title) {
        Log.d(TAG, "updateFolderTitle(" + title + ")");
        mainActivityInterface.getMainHandler().post(() -> {
            mainActivityInterface.getOpenChordsAPI().setIsServerResponse(true);
            folderChangedProgrammatically = true;
            myView.folderToSync.setText(title);
            folderChangedProgrammatically = false;
            mainActivityInterface.getOpenChordsAPI().setIsServerResponse(false);
            checkFolderMessage();
            changeButtonsEnable(true);
        });
    }

    public void queryOpenChordsServer() {
        checkQueryHandler.removeCallbacks(checkQueryRunnable);
        Log.d(TAG,"queryOpenChordsServer()");
        // Use the folder chosen to query the server and get the results
        mainActivityInterface.getMainHandler().post(() -> {
            if (mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                changeButtonsEnable(false);
                checkQueryHandler.postDelayed(checkQueryRunnable, 100);

            } else {
                if (myView != null && myView.folderToSync.getText() != null) {
                    changeButtonsEnable(false);
                    updateProgress(sync_querying_remote_string + "\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        myView.folderMessage.setText("");
                        mainActivityInterface.getOpenChordsAPI().setOpenChordsFolderName(myView.folderToSync.getText().toString());
                        mainActivityInterface.getOpenChordsAPI().getFolderContentsFromUUID();
                    }, 1000);
                }
            }
        });
    }

    // We are sent here after hearing back from the server
    public void logChanges() {
        Log.d(TAG,"logChanges()");
        // Do this on a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Update the change number identifiers
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView != null) {
                    changeButtonsEnable(true);
                    myView.uploadCount.setText(String.valueOf(mainActivityInterface.getOpenChordsAPI().getUploadCount()));
                    myView.uploadLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getUploadCount()>0 ? View.VISIBLE:View.GONE);
                    myView.downloadCount.setText(String.valueOf(mainActivityInterface.getOpenChordsAPI().getDownloadCount()));
                    myView.downloadLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getDownloadCount()>0 ? View.VISIBLE:View.GONE);
                    if (mainActivityInterface.getOpenChordsAPI().getUploadCount()==0 && mainActivityInterface.getOpenChordsAPI().getDownloadCount()==0) {
                        myView.folderMessage.setText(sync_no_changes_required_string);
                    }
                }
            });
        });
    }

    public void updateProgress(String progress) {
        if (myView != null && progress!=null) {
            myView.progressText.post(() -> {
                try {
                    String[] progressBits = progress.split("\n");
                    if (progressBits.length>0) {
                        myView.progressText.setText(progressBits[0]);
                    } else {
                        myView.progressText.setText("");
                    }
                    if (progressBits.length>1) {
                        myView.progressSubText.setText(progressBits[1]);
                    } else {
                        myView.progressSubText.setText("");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    // Force changes confirmed by the user!
    public void doForceChanges(String which) {
        Log.d(TAG,"doForceChanges("+which+")");
        switch (which) {
            case "openChordsForcePull":
                // We have forced a pull.
                // This wipes our local items and downloads everything from the remote folder
                changeButtonsEnable(false);
                mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                    mainActivityInterface.getOpenChordsAPI().forcePull();
                    mainActivityInterface.getMainHandler().post(this::queryOpenChordsServer);
                });
                break;

            case "openChordsForcePush":
                // We have forced a push.
                // This wipes the remote items and uploads everything from the local folder
                changeButtonsEnable(false);
                mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                    mainActivityInterface.getOpenChordsAPI().forcePush();
                    mainActivityInterface.getMainHandler().post(this::queryOpenChordsServer);
                });
                break;
        }
    }

    public void prepareDownload(boolean newSongs, boolean updateSongs, boolean newSetLists, boolean updateSetLists) {
        changeButtonsEnable(false);

        // Do this on a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            mainActivityInterface.getOpenChordsAPI().prepareDownload(newSongs,updateSongs,newSetLists,updateSetLists);
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView!=null) {
                    changeButtonsEnable(true);
                }
            });
            queryOpenChordsServer();
        });
    }
    public void prepareUpload(boolean newSongs, boolean updateSongs, boolean newSetLists, boolean updateSetLists) {
        changeButtonsEnable(false);
        // Do this on a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            mainActivityInterface.getOpenChordsAPI().prepareUpload(newSongs,updateSongs,newSetLists,updateSetLists);
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView!=null) {
                    changeButtonsEnable(true);
                }
            });
            queryOpenChordsServer();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        checkQueryHandler.removeCallbacks(checkQueryRunnable);
        checkQueryHandler = null;
        mainActivityInterface.getOpenChordsAPI().setOpenChordsFragment(null);
        mainActivityInterface.getOpenChordsAPI().clearSyncObjects();
    }
}

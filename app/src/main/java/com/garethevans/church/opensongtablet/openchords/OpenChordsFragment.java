package com.garethevans.church.opensongtablet.openchords;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.databinding.SettingsOpenchordsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class OpenChordsFragment extends Fragment {
    // This class is where we trigger sync with the OpenChords server

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "OpenChordsFragment";
    private MainActivityInterface mainActivityInterface;
    private String openchords_string = "";
    private String webAddress = "";
    private String folder_exists_but_is_different_string = "";
    private String openchords_folder_doesnt_exist_string = "";
    private String sync_no_changes_required_string ="";
    private String wait_string = "";
    private String index_songs_wait_string = "";
    private String sync_querying_remote_string = "";
    private String owner_string, not_owner_string, read_only_string;
    private SettingsOpenchordsBinding myView;
    private boolean folderChangedProgrammatically = false, changingReadOnlyProgrammatically = false;
    private Handler checkQueryHandler = new Handler();
    private Runnable checkQueryRunnable;
    private String keepLocalFolderName;
    private boolean alreadyQuerying = false;
    private Handler alreadyQueryingHandler;
    private Runnable alreadyQueryingReset = new Runnable() {
        @Override
        public void run() {
            alreadyQuerying = false;
        }
    };

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
        alreadyQueryingHandler = mainActivityInterface.getMainHandler();
        mainActivityInterface.getOpenChordsAPI().setOpenChordsFragment(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsOpenchordsBinding.inflate(inflater, container, false);

        myView.getRoot().setBackgroundColor(mainActivityInterface.getPalette().background);

        // Tint the progressBar as the secondary color
        mainActivityInterface.getMyThemeColors().tintProgressBar(myView.progressBar);

        changeButtonsEnable(false);
        updateProgress(wait_string+"\n");
        prepareStrings();

        // Check we have an up to date record of our folders
        mainActivityInterface.getOpenChordsAPI().initialiseRecords();
        mainActivityInterface.getOpenChordsAPI().initialiseOpenChordsFolderAndUuid();

        setupViews();
        setupListeners();

        // Now query the server based on the folder uuid we have in the OpenChordsAPI
        queryOpenChordsServer();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext() != null) {
            if (mainActivityInterface.getPalette().textColor == R.color.dark_color) {
                myView.openChordsLogo.setImageDrawable(AppCompatResources.getDrawable(getContext(),R.drawable.openchords_logo_white_blue));
            } else {
                Drawable drawable = AppCompatResources.getDrawable(getContext(),R.drawable.openchords_logo_white);
                if (drawable!=null) {
                    DrawableCompat.setTint(drawable,mainActivityInterface.getPalette().textColor);
                    myView.openChordsLogo.setImageDrawable(drawable);
                }
                myView.openChordsLogo.setImageDrawable(AppCompatResources.getDrawable(getContext(),R.drawable.openchords_logo_white));
            }

            Drawable popup = AppCompatResources.getDrawable(getContext(), R.drawable.popup_bg);
            if (popup!=null) {
                DrawableCompat.setTint(popup,mainActivityInterface.getPalette().secondary);
            }
            myView.fakeToastLayout.setBackground(popup);

            openchords_string = getString(R.string.openchords);
            webAddress = getString(R.string.website_openchords);
            openchords_folder_doesnt_exist_string = getString(R.string.openchords_folder_doesnt_exist);
            folder_exists_but_is_different_string = getString(R.string.folder_exists_but_is_different);
            wait_string = getString(R.string.wait);
            sync_no_changes_required_string = getString(R.string.sync_no_changes_required);
            sync_querying_remote_string = getString(R.string.sync_querying_remote);
            index_songs_wait_string = getString(R.string.index_songs_wait);
            owner_string = getString(R.string.openchords_owner);
            not_owner_string = getString(R.string.openchords_not_owner);
            read_only_string = getString(R.string.openchords_readonly);

            checkQueryRunnable = () -> {
                if (mainActivityInterface!=null && mainActivityInterface.getSongListBuildIndex()!=null &&
                        mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                    // Keep the user posted
                    String progressText = index_songs_wait_string;
                    if (mainActivityInterface.getSongMenuFragment() != null) {
                        MyMaterialSimpleTextView progressView = mainActivityInterface.getSongMenuFragment().getProgressText();
                        if (progressView != null && progressView.getText() != null) {
                            progressText += "\n" + progressView.getText().toString();
                        }
                        updateProgress(progressText);
                        checkQueryHandler.postDelayed(checkQueryRunnable, 100);
                    }
                } else {
                    alreadyQueryingHandler.removeCallbacks(alreadyQueryingReset);
                    alreadyQuerying = false;
                    queryOpenChordsServer();
                }
            };
        }
    }

    private void setupViews() {
        if (getContext() != null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter =
                    new ExposedDropDownArrayAdapter(getContext(), myView.folderToSync,
                            R.layout.view_exposed_dropdown_item,
                            mainActivityInterface.getOpenChordsAPI().getValidFolders());
            myView.folderToSync.setAdapter(exposedDropDownArrayAdapter);
            folderChangedProgrammatically = true;
            if (mainActivityInterface.getWhattodo().equals("openchordsintent")) {
                // We got here via an intent
                // Look for a local folder that matches the intent uuid
                // If not, set it to null
                mainActivityInterface.setWhattodo("");
                folderChangedProgrammatically = true;
                myView.folderToSync.setText(mainActivityInterface.getOpenChordsAPI().
                        getOpenSongFolderNameFromUUID(
                                mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderUuid()));
                Log.d(TAG,"here via the intent");
            } else {
                // Just set it to our preference
                myView.folderToSync.setText(mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName());
            }

            folderChangedProgrammatically = false;

            // Set up the QR code
            Glide.with(getContext()).load(mainActivityInterface.getOpenChordsAPI().getOpenChordsQRCode()).into(myView.openChordsQRImage);
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
                    // If we manually changed this, save the new folder name preference
                    // Otherwise we are just updating the folder name from the server response
                    if (!folderChangedProgrammatically) {
                        // We have manually changed the folder name
                        String folderName = myView.folderToSync.getText().toString();
                        mainActivityInterface.getOpenChordsAPI().setReceivedFolderLink(false);
                        // Save our preference
                        mainActivityInterface.getPreferences().setMyPreferenceString(
                                "openChordsFolderName",folderName);
                        // Update the API with our folderName and the uuid of the matching folder
                        mainActivityInterface.getOpenChordsAPI().setOpenChordsFolderName(folderName);
                        if (mainActivityInterface.getOpenChordsAPI().
                                getOpenSongFolderUuidFromName(folderName)!=null) {
                            mainActivityInterface.getOpenChordsAPI().setOpenChordsFolderUuid(
                                    mainActivityInterface.getOpenChordsAPI().
                                            getOpenSongFolderUuidFromName(folderName));
                        }
                        // Assume we want to get the foldername from the server
                        keepLocalFolderName = null;
                        mainActivityInterface.getOpenChordsAPI().setLocalFolderName(null);
                        // Get the new QR code
                        Glide.with(getContext()).load(mainActivityInterface.getOpenChordsAPI().
                                getOpenChordsQRCode()).into(myView.openChordsQRImage);

                        // Query the server
                        queryOpenChordsServer();
                    }
                } else if (myView!=null && myView.folderToSync.getText() != null && getContext()!=null) {
                    String folderName = myView.folderToSync.getText().toString();
                    Log.d(TAG, "server returned folder sent back from server:" + folderName);
                    mainActivityInterface.getOpenChordsAPI().setOpenChordsFolderName(folderName);
                }
                mainActivityInterface.getOpenChordsAPI().setIsServerResponse(false);
                folderChangedProgrammatically = false;
            }
        });
        myView.openChordsQRImage.setOnClickListener(view -> mainActivityInterface.openDocument(mainActivityInterface.getOpenChordsAPI().getOpenChordsAddress()));
        myView.refresh.setOnClickListener(view -> queryOpenChordsServer());
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
        myView.readOnly.setOnCheckedChangeListener((compoundButton, readOnly) -> {
            // If we are the owner, we can push this change
            // We need to check we aren't just changing this programmatically
            if (!changingReadOnlyProgrammatically &&
                mainActivityInterface.getOpenChordsAPI().getIsOwner()) {
                mainActivityInterface.getOpenChordsAPI().changeReadOnly(readOnly);
            }
        });
    }

    public void openChordsFolderNotFound() {
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView != null) {
                myView.folderMessage.setText(openchords_folder_doesnt_exist_string);
                //myView.downloadLayout.setVisibility(View.GONE);
                changeButtonsEnable(true);
            }
        });
    }

    public void openChordsFolderDifferentFromLocal() {
        // We already have the folder, but it has a different UUID
        // Warn the user that downloading will replace the content of this folder
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView != null) {
                myView.folderMessage.setText(folder_exists_but_is_different_string);
                //myView.uploadLayout.setVisibility(View.GONE);
            }
        });
    }

    public void openChordsFolderFullySynced() {
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView != null) {
                if (mainActivityInterface.getOpenChordsAPI().getUploadCount() == 0 && mainActivityInterface.getOpenChordsAPI().getDownloadCount() == 0) {
                    myView.folderMessage.setText(sync_no_changes_required_string);
                }
            }
        });
    }

    public void changeButtonsEnable(boolean enable) {
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView != null) {
                myView.folderToSync.setEnabled(enable);
                myView.uploadLayout.setEnabled(enable);
                myView.downloadLayout.setEnabled(enable);
                myView.linkLayout.setEnabled(enable);
                myView.scrimOverlay.setVisibility(enable ? View.GONE : View.VISIBLE);
                myView.progressLayout.setVisibility(enable ? View.GONE : View.VISIBLE);
            }
        });
    }

    public void justUpdateTitle(String title) {
        mainActivityInterface.getMainHandler().post(() -> myView.folderToSync.setText(title));
    }
    public void updateFolderTitle(String title) {
        mainActivityInterface.getMainHandler().post(() -> {
            mainActivityInterface.getOpenChordsAPI().setIsServerResponse(true);
            folderChangedProgrammatically = true;
            String titleToShow = title;
            if (keepLocalFolderName!=null) {
                mainActivityInterface.getOpenChordsAPI().setLocalFolderName(keepLocalFolderName);
                titleToShow = keepLocalFolderName;
            }
            // If the server has a different folder title than our one
            // We should prompt the user to either update the server one or rename our folder
            // If the user decides to change the local folder, we need to query again
            if (keepLocalFolderName==null && myView.folderToSync.getText() != null && titleToShow!=null && !titleToShow.isEmpty() && !myView.folderToSync.getText().toString().isEmpty() &&
                    !myView.folderToSync.getText().toString().equals(titleToShow)) {
                OpenChordsFolderNameChangeBottomSheet openChordsFolderNameChangeBottomSheet = new OpenChordsFolderNameChangeBottomSheet(this,myView.folderToSync.getText().toString());
                openChordsFolderNameChangeBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "OpenChordsFolderNameChangeBottomSheet");

            } else {
                // Either the folder names are the same, or we didn't have a folder set (i.e. intent)
                if (titleToShow!=null && !titleToShow.isEmpty()) {
                    myView.folderToSync.setText(titleToShow);
                }
                folderChangedProgrammatically = false;
                mainActivityInterface.getOpenChordsAPI().setIsServerResponse(false);
                changeButtonsEnable(true);
            }
        });
    }

    public void queryOpenChordsServer() {
        if (!alreadyQuerying) {
            alreadyQueryingHandler.removeCallbacks(alreadyQueryingReset);
            alreadyQuerying = true;
            checkQueryHandler.removeCallbacks(checkQueryRunnable);
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
                            mainActivityInterface.getOpenChordsAPI().getFolderContentsFromUUID();
                        }, 100);
                    }
                }
            });
        }
    }

    // We are sent here after hearing back from the server
    public void logChanges() {
        // Do this on a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Update the change number identifiers
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView != null) {
                    if (keepLocalFolderName!=null) {
                        folderChangedProgrammatically = true;
                        myView.folderToSync.setText(keepLocalFolderName);
                    }
                    changeButtonsEnable(true);
                    boolean isOwner = mainActivityInterface.getOpenChordsAPI().getIsOwner();
                    boolean isReadOnly = mainActivityInterface.getOpenChordsAPI().getIsReadOnly();

                    // If we are the owner, we can upload fine.  If not, we can only upload if the folder isn't read only
                    boolean canUpload = isOwner || !isReadOnly;
                    myView.uploadCount.setText(String.valueOf(mainActivityInterface.getOpenChordsAPI().getUploadCount()));
                    myView.uploadLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getUploadCount()>0 && canUpload? View.VISIBLE:View.GONE);
                    myView.downloadCount.setText(String.valueOf(mainActivityInterface.getOpenChordsAPI().getDownloadCount()));
                    myView.downloadLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getDownloadCount()>0 ? View.VISIBLE:View.GONE);
                    myView.readOnly.setVisibility(isOwner ? View.VISIBLE : View.GONE);
                    myView.readOnlyDivider.setVisibility(isOwner ? View.VISIBLE : View.GONE);
                    changingReadOnlyProgrammatically = true;
                    myView.readOnly.setChecked(isReadOnly);
                    myView.readOnly.postDelayed(() -> changingReadOnlyProgrammatically = false,500);
                    updateFolderMessage();
                }
            });
        });
        alreadyQueryingHandler.postDelayed(alreadyQueryingReset,1000);
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

    public void setKeepLocalFolderName(String keepLocalFolderName) {
        this.keepLocalFolderName = keepLocalFolderName;
    }
    // Force changes confirmed by the user!
    public void doForceChanges(String which) {
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
    public void deleteLocalSongs() {
        mainActivityInterface.getOpenChordsAPI().deleteLocalSongs();
    }
    public void deleteLocalSets() {
        mainActivityInterface.getOpenChordsAPI().deleteLocalSets();
    }
    public void deleteRemoteSongs() {
        mainActivityInterface.getOpenChordsAPI().deleteRemoteSongs();
    }
    public void deleteRemoteSets() {
        mainActivityInterface.getOpenChordsAPI().deleteRemoteSets();
    }

    public void updateFolderMessage() {
        // Try to update the folder message sensibly
        int downloadCount = mainActivityInterface.getOpenChordsAPI().getDownloadCount();
        int uploadCount = mainActivityInterface.getOpenChordsAPI().getUploadCount();

        String ownerInfo = mainActivityInterface.getOpenChordsAPI().getIsOwner() ? owner_string + "\n\n" : not_owner_string + "\n\n";
        String readOnlyInfo = mainActivityInterface.getOpenChordsAPI().getIsReadOnly() ? read_only_string + "\n\n" : "";

        String folderInfo = "";
        if (mainActivityInterface.getOpenChordsAPI().getServerFolder()==null) {
            folderInfo = openchords_folder_doesnt_exist_string;
            ownerInfo = "";
            readOnlyInfo = "";
        } else if (mainActivityInterface.getOpenChordsAPI().getFolderIsDifferentUuid()) {
            folderInfo = folder_exists_but_is_different_string;
            ownerInfo = "";
            readOnlyInfo = "";
        } else if (downloadCount==0 && uploadCount==0) {
            folderInfo = sync_no_changes_required_string;
        }
        String message = ownerInfo + readOnlyInfo + folderInfo;

        myView.folderMessage.postDelayed(() -> myView.folderMessage.setText(message),500);
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

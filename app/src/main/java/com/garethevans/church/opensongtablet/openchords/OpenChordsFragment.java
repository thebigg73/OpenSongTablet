package com.garethevans.church.opensongtablet.openchords;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
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
    private String processing_string;
    private String index_songs_wait_string = "";
    private String sync_querying_remote_string = "";
    private String owner_string, not_owner_string, read_only_string;
    private SettingsOpenchordsBinding myView;
    private boolean changingReadOnlyProgrammatically = false;
    private Handler checkQueryHandler;
    private Runnable checkQueryRunnable;
    private String keepLocalFolderName;
    private MyMaterialSimpleTextView progressView;

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
        mainActivityInterface.updateToolbar(openchords_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.getOpenChordsAPI().setOpenChordsFragment(this);
        mainActivityInterface.getOpenChordsAPI().removeCallbacks();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsOpenchordsBinding.inflate(inflater, container, false);

        // Everything else triggered on background thread with UI updates posted there
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Check we have an up to date record of our folders
            mainActivityInterface.getOpenChordsAPI().initialiseRecords();
            mainActivityInterface.getOpenChordsAPI().initialiseOpenChordsFolderAndUuid();

            // Prepare the strings needed for the app
            prepareStrings();

            mainActivityInterface.updateToolbar(openchords_string);

            // Tint and theme checks
            if (myView!=null) {
                myView.getRoot().post(() -> {
                    if (myView != null) {
                                myView.getRoot().setBackgroundColor(mainActivityInterface.getPalette().background);
                    }
                });
                myView.progressBar.post(()-> {
                    if (myView!=null) {
                        mainActivityInterface.getMyThemeColors().tintProgressBar(myView.progressBar);
                    }
                });

                // Get the folder needed for sync
                if (mainActivityInterface.getWhattodo().equals("openchordsintent")) {
                    // We got here via an intent
                    // Look for a local folder that matches the intent uuid
                    // If not, set it to null
                    mainActivityInterface.setWhattodo("");
                    myView.folderToSync.post(() -> {
                        myView.folderToSync.setText(mainActivityInterface.getOpenChordsAPI().
                                getOpenSongFolderNameFromUUID(
                                        mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderUuid()));
                        Log.d(TAG, "here via the intent");
                    });
                } else {
                    // Just set it to our preference
                    Log.d(TAG,"folderToSync:"+mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName());
                    myView.folderToSync.post(() -> myView.folderToSync.setText(mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName()));
                }

                // Disable the buttons while stuff is prepared
                changeButtonsEnable(false);
                updateProgress(wait_string+"\n");

                setupRunnables();
                setupViews();
                setupListeners();


                // This is our first call to the server now we have dealt with everything else
                myView.folderToSync.post(()-> Log.d(TAG,"first query using folder:"+myView.folderToSync.getText().toString()));
                mainActivityInterface.getOpenChordsAPI().delayedQueryServer(1000);
            }
        });

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext() != null) {
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
            processing_string = getString(R.string.processing);
            progressView = mainActivityInterface.getSongMenuFragment().getProgressText();
        }
    }

    private void setupRunnables() {
        if (checkQueryHandler==null) {
            checkQueryHandler = mainActivityInterface.getMainHandler();
        }
        checkQueryRunnable = () -> {
            if (mainActivityInterface!=null && mainActivityInterface.getSongListBuildIndex()!=null &&
                    mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                // Keep the user posted
                String progressText = index_songs_wait_string;
                if (mainActivityInterface.getSongMenuFragment() != null) {
                    if (progressView != null && progressView.getText() != null) {
                        progressText += "\n" + progressView.getText().toString();
                    }
                    updateProgress(progressText);
                    checkQueryHandler.removeCallbacks(checkQueryRunnable);
                    checkQueryHandler.postDelayed(checkQueryRunnable, 1000);
                }
            }
        };
    }
    private void setupViews() {
        if (getContext() != null) {
            // Decide if we need a light or dark OpenChords logo
            if (mainActivityInterface.getPalette().textColor == R.color.dark_color) {
                myView.openChordsLogo.post(() -> myView.openChordsLogo.setImageDrawable(AppCompatResources.getDrawable(getContext(),R.drawable.openchords_logo_white_blue)));
            } else {
                Drawable drawable = AppCompatResources.getDrawable(getContext(),R.drawable.openchords_logo_white);
                if (drawable!=null) {
                    DrawableCompat.setTint(drawable,mainActivityInterface.getPalette().textColor);
                    myView.openChordsLogo.post(() -> myView.openChordsLogo.setImageDrawable(drawable));
                }
            }

            Drawable popup = AppCompatResources.getDrawable(getContext(), R.drawable.popup_bg);
            if (popup!=null) {
                DrawableCompat.setTint(popup,mainActivityInterface.getPalette().secondary);
            }
            myView.fakeToastLayout.post(() -> myView.fakeToastLayout.setBackground(popup));

            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter =
                    new ExposedDropDownArrayAdapter(getContext(), myView.folderToSync,
                            R.layout.view_exposed_dropdown_item,
                            mainActivityInterface.getOpenChordsAPI().getValidFolders());
            myView.folderToSync.post(() -> {
                myView.folderToSync.setProgrammaticChange(true);
                myView.folderToSync.setAdapter(exposedDropDownArrayAdapter);
                if (mainActivityInterface.getWhattodo().equals("openchordsintent")) {
                    // We got here via an intent
                    // Look for a local folder that matches the intent uuid
                    // If not, set it to null
                    mainActivityInterface.setWhattodo("");
                    myView.folderToSync.setText(mainActivityInterface.getOpenChordsAPI().
                            getOpenSongFolderNameFromUUID(
                                    mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderUuid()));
                    Log.d(TAG, "here via the intent");
                } else {
                    // Just set it to our preference
                    myView.folderToSync.setText(mainActivityInterface.getOpenChordsAPI().getOpenChordsFolderName());
                }
                myView.folderToSync.setProgrammaticChange(false);
            });

            // Tint the background circle for the file counter
            //ColorStateList secondaryTint = ColorStateList.valueOf(mainActivityInterface.getPalette().secondary);
            ColorStateList secondaryTint = new ColorStateList(
                    new int[][]{new int[0]},
                    new int[]{mainActivityInterface.getPalette().secondary}
            );

            myView.uploadCount.post(() -> ViewCompat.setBackgroundTintList(myView.uploadCount, secondaryTint));
            myView.downloadCount.post(() -> ViewCompat.setBackgroundTintList(myView.downloadCount, secondaryTint));

            // Set up the QR code
            myView.openChordsQRImage.post(() -> Glide.with(getContext()).load(mainActivityInterface.getOpenChordsAPI().getOpenChordsQRCode()).into(myView.openChordsQRImage));
        }
    }

    private void setupListeners() {
        myView.folderToSync.post(() -> myView.folderToSync.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!myView.folderToSync.getProgrammaticChange()) {
                    checkForUpdates();
                } else {
                    Log.d(TAG,"don't query as programmatic change");
                }
            }
        }));

        myView.openChordsQRImage.post(() -> {
                    myView.openChordsQRImage.setOnClickListener(view -> mainActivityInterface.openDocument(mainActivityInterface.getOpenChordsAPI().getOpenChordsAddress()));
                    myView.refresh.setOnClickListener(view -> {
                        // Query the server again
                        Log.d(TAG, "clicked on update.  Should we query the server again? -yes");
                        mainActivityInterface.getOpenChordsAPI().delayedQueryServer(200);
                    });
                });

        myView.downloadLayout.post(() -> myView.downloadLayout.setOnClickListener(view -> {
            if (mainActivityInterface.getSongListBuildIndex() != null && mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                checkQueryHandler.removeCallbacks(checkQueryRunnable);
                checkQueryHandler.post(checkQueryRunnable);
            } else {
                OpenChordsBottomSheet openChordsBottomSheet = new OpenChordsBottomSheet(this, "download");
                openChordsBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "OpenChordsBottomSheet");
            }
        }));

        myView.uploadLayout.post(() -> myView.uploadLayout.setOnClickListener(view -> {
            if (mainActivityInterface.getSongListBuildIndex() != null && mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                checkQueryHandler.removeCallbacks(checkQueryRunnable);
                checkQueryHandler.post(checkQueryRunnable);
            } else {
                OpenChordsBottomSheet openChordsBottomSheet = new OpenChordsBottomSheet(this, "upload");
                openChordsBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "OpenChordsBottomSheet");
            }
        }));

        myView.linkLayout.post(() -> myView.linkLayout.setOnClickListener(view -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, mainActivityInterface.getOpenChordsAPI().getOpenChordsAddress());
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        }));

        myView.forceChanges.post(() -> myView.forceChanges.setOnClickListener(view -> {
            if (mainActivityInterface.getSongListBuildIndex() != null && mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                checkQueryHandler.removeCallbacks(checkQueryRunnable);
                checkQueryHandler.post(checkQueryRunnable);
            } else {
                OpenChordsForceBottomSheet openChordsForceBottomSheet = new OpenChordsForceBottomSheet(this);
                openChordsForceBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "OpenChordsForceBottomSheet");
            }
        }));

        myView.readOnly.post(() -> myView.readOnly.setOnCheckedChangeListener((compoundButton, readOnly) -> {
            // If we are the owner, we can push this change
            // We need to check we aren't just changing this programmatically
            if (!changingReadOnlyProgrammatically &&
                    mainActivityInterface.getOpenChordsAPI().getIsOwner()) {
                mainActivityInterface.getOpenChordsAPI().changeReadOnly(readOnly);
            }
        }));

        myView.openChordsSettings.post(() -> myView.openChordsSettings.setOnClickListener(view -> {
            OpenChordsUUIDBottomSheet openChordsUUIDBottomSheet = new OpenChordsUUIDBottomSheet(getContext());
            openChordsUUIDBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"OpenChordsUUIDsBS");
        }));
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
        mainActivityInterface.getMainHandler().post(() -> {
            myView.folderToSync.setProgrammaticChange(true);
            myView.folderToSync.setText(title);
            myView.folderToSync.postDelayed(() -> myView.folderToSync.setProgrammaticChange(false),200);
        });
    }
    public void updateFolderTitle(String title) {
        mainActivityInterface.getMainHandler().post(() -> {
            myView.folderToSync.setProgrammaticChange(true);
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
                    myView.folderToSync.setProgrammaticChange(true);
                    myView.folderToSync.setText(titleToShow);
                    myView.folderToSync.postDelayed(() -> myView.folderToSync.setProgrammaticChange(false),200);
                }
                changeButtonsEnable(true);
            }
        });
    }

    public void queryOpenChordsServer() {
        Log.d(TAG, "queryOpenChordsServer()");
        checkQueryHandler.removeCallbacks(checkQueryRunnable);
        // Use the folder chosen to query the server and get the results
        mainActivityInterface.getMainHandler().post(() -> {
            if (mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
                changeButtonsEnable(false);
                checkQueryHandler.postDelayed(checkQueryRunnable, 500);

            } else {
                if (myView != null && myView.folderToSync.getText() != null) {
                    changeButtonsEnable(false);
                    updateProgress(sync_querying_remote_string + "\n");
                    mainActivityInterface.getMainHandler().postDelayed(() -> {
                        myView.folderMessage.setText("");
                        mainActivityInterface.getOpenChordsAPI().getFolderContentsFromUUID();
                    }, 500);
                }
            }
        });
    }

    // We are sent here after hearing back from the server
    public void logChanges() {
        Log.d(TAG,"Log changes called");
        // Do this on a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Update the change number identifiers
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView != null) {
                    if (keepLocalFolderName!=null) {
                        myView.folderToSync.setProgrammaticChange(true);
                        myView.folderToSync.setText(keepLocalFolderName);
                        mainActivityInterface.getOpenChordsAPI().setLocalFolderName(keepLocalFolderName);
                        myView.folderToSync.postDelayed(() -> myView.folderToSync.setProgrammaticChange(false),200);
                    }
                    changeButtonsEnable(true);
                    boolean isOwner = mainActivityInterface.getOpenChordsAPI().getIsOwner();
                    boolean isReadOnly = mainActivityInterface.getOpenChordsAPI().getIsReadOnly();
                    boolean folderExists = mainActivityInterface.getOpenChordsAPI().getFolderExists();

                    Log.d(TAG,"isOwner:"+isOwner+"  isReadOnly:"+isReadOnly+"  folderExists:"+folderExists);
                    Log.d(TAG,"localFolderName:"+mainActivityInterface.getOpenChordsAPI().getLocalFolderName());
                    Log.d(TAG,"songsNotOnServerString:"+mainActivityInterface.getOpenChordsAPI().getSongsNotOnServerString());
                    Log.d(TAG,"myView.folderToSync.getText().toString():"+myView.folderToSync.getText().toString());
                    for (OpenChordsCompareObject openChordsCompareObject:mainActivityInterface.getOpenChordsAPI().getLocalSongsCompareObjects()) {
                        Log.d(TAG,"localSong:"+openChordsCompareObject.getTitle() + "  "+ openChordsCompareObject.getLastModified());
                    }
                    for (OpenChordsCompareObject openChordsCompareObject:mainActivityInterface.getOpenChordsAPI().getServerSongsCompareObjects()) {
                        Log.d(TAG,"serverSong:"+openChordsCompareObject.getTitle() + "  "+ openChordsCompareObject.getLastModified());
                    }

                    // If we are the owner, we can upload fine.  If not, we can only upload if the folder isn't read only
                    boolean canUpload = isOwner || !isReadOnly || !folderExists;
                    Log.d(TAG,"uploadCount:"+mainActivityInterface.getOpenChordsAPI().getUploadCount());
                    Log.d(TAG,"downlaodCount:"+mainActivityInterface.getOpenChordsAPI().getDownloadCount());
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

                    mainActivityInterface.getOpenChordsAPI().removeCallbacks();
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
                        myView.progressText.setText(processing_string);
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
                mainActivityInterface.getThreadPoolExecutor().execute(() -> mainActivityInterface.getOpenChordsAPI().forcePull());
                break;

            case "openChordsForcePush":
                // We have forced a push.
                // This wipes the remote items and uploads everything from the local folder
                changeButtonsEnable(false);
                mainActivityInterface.getThreadPoolExecutor().execute(() -> mainActivityInterface.getOpenChordsAPI().forcePush());
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
            // Query the server
            Log.d(TAG,"prepareDownload() completed.  Should we query the server again?");

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

    // Returns the string in the message
    public String getMessage() {
        if (myView!=null && myView.progressLayout.getVisibility()==View.VISIBLE && myView.progressText.getText()!=null) {
            return myView.progressText.getText().toString();
        } else {
            return "";
        }
    }

    public void updateFolderMessage() {
        // Try to update the folder message sensibly
        int downloadCount = mainActivityInterface.getOpenChordsAPI().getDownloadCount();
        int uploadCount = mainActivityInterface.getOpenChordsAPI().getUploadCount();

        String ownerInfo = mainActivityInterface.getOpenChordsAPI().getIsOwner() ? owner_string + "\n\n" : not_owner_string + "\n\n";
        String readOnlyInfo = mainActivityInterface.getOpenChordsAPI().getIsReadOnly() ? read_only_string + "\n\n" : "";
        Log.d(TAG,"folderExists:"+mainActivityInterface.getOpenChordsAPI().getFolderExists());

        String folderInfo = "";
        if (!mainActivityInterface.getOpenChordsAPI().getFolderExists()) {
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
        mainActivityInterface.getOpenChordsAPI().removeCallbacks();
        mainActivityInterface.setWhattodo("");
    }

    private void checkForUpdates() {
        // Deal with a change in the foldername.
        // Only proceed if we haven't just changed and waiting on a response
        if (getContext()!=null && myView!=null && myView.folderToSync.getText()!=null) {
            // We have manually changed the folder name
            String folderName = myView.folderToSync.getText().toString();
            if (!folderName.isEmpty()) {
                Log.d(TAG,"Starting to sync check with "+folderName);
                mainActivityInterface.getOpenChordsAPI().setReceivedFolderLink(false);
                // Save our preference by updating the API
                Log.d(TAG,"setting the folderName:"+folderName);
                mainActivityInterface.getOpenChordsAPI().setOpenChordsFolderName(folderName);

                if (mainActivityInterface.getOpenChordsAPI().
                        getOpenSongFolderUuidFromName(folderName) != null) {
                    mainActivityInterface.getOpenChordsAPI().setOpenChordsFolderUuid(
                            mainActivityInterface.getOpenChordsAPI().
                                    getOpenSongFolderUuidFromName(folderName));
                }

                // Get the new QR code
                Glide.with(getContext()).load(mainActivityInterface.getOpenChordsAPI().
                        getOpenChordsQRCode()).into(myView.openChordsQRImage);

                // These are carried out on a background thread
                mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                    // 1. Query the server
                    Log.d(TAG, "Querying the server");
                    mainActivityInterface.getOpenChordsAPI().delayedQueryServer(0);
                });
            }


        }
    }
}

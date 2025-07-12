package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.net.Uri;
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
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.MainActivity;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsSyncBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SyncNearbyFragment extends Fragment {
    // This fragment is used to request, then display a list of files available on the host device
    // It is only accessible on devices that are connected and are not running as hosts themselves
    // This fragment can be called from the SetActionsFragment, ProfileActionsFragment and SongActionsFragment

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "BrowseHostFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsSyncBinding myView;
    @SuppressWarnings("FieldCanBeLocal")
    private String browse_host_files_string = "", sets_string = "", profiles_string = "", songs_string = "",
            set_current_string = "", set_is_empty_string = "", processing_string = "",
            nearby_files_copied_string = "", nearby_files_skipped_string = "",
            nearby_files_failed_string = "", no_response_string = "", sync_waiting_for_info_string = "",
            sync_info_received_string = "", new_songs_string = "", updated_songs_string = "",
            new_files_string = "", updated_files_string = "", chosenDevice = "", sync_extracting_string="";
    private boolean syncSongPrepared = false;
    private boolean syncSetPrepared = false;
    private boolean syncProfilePrepared = false;
    private NearbyJson nearbyJson;
    private ArrayList<NearbySyncItem> checkedItems = new ArrayList<>();
    private boolean waitingForFiles = false, overwrite = false;
    private String requestedFolder, requestedSubfolder, requestedFilename, folder;
    private String nearbyCurrentSet = null;
    private final ArrayList<String> filesCopied = new ArrayList<>();
    private final ArrayList<String> filesSkipped = new ArrayList<>();
    private final ArrayList<String> filesFailed = new ArrayList<>();
    private SyncViewPagerAdapter syncViewPagerAdapter;
    private SyncItemsFragment syncSongFragment, syncSetFragment, syncProfileFragment;
    private final ArrayList<String> connectedDeviceCodes = new ArrayList<>();
    private final ArrayList<String> connectedDeviceNames = new ArrayList<>();
    private boolean timeout = false;
    private final Handler progressTextClearHandler = new Handler();
    private final Runnable progressTextClearRunnable = new Runnable() {
        @Override
        public void run() {
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView!=null) {
                    myView.hostProgressTextView.setText("");
                    myView.hostProgressTextView.setVisibility(View.GONE);
                    showProgress(false);
                }
            });
        }
    };
    private final Handler timeoutHandler = new Handler();
    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (timeout) {
                showProgress(false);
                if (myView != null) {
                    myView.chooseConnected.post(() -> myView.chooseConnected.setText(""));
                    mainActivityInterface.getShowToast().doIt(no_response_string);
                }
                timeout = false;
            }
        }
    };


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.getNearbyActions().setSyncNearbyFragment(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsSyncBinding.inflate(inflater, container, false);
        return myView.getRoot();
    }

    @Override
    public void onResume() {
        prepareStrings();
        setupViews();
        setupListeners();
        mainActivityInterface.getNearbyActions().setSyncNearbyFragment(this);
        // Empty the export folder
        mainActivityInterface.getStorageAccess().wipeFolder("Export","");
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mainActivityInterface.getNearbyActions().setSyncNearbyFragment(null);
        mainActivityInterface.setWhattodo("");
        super.onDestroy();
    }

    private void prepareStrings() {
        if (getContext() != null && mainActivityInterface != null && myView != null) {
            browse_host_files_string = getString(R.string.connections_browse_host);
            sets_string = getString(R.string.set_lists);
            profiles_string = getString(R.string.profile);
            songs_string = getString(R.string.songs);
            set_current_string = getString(R.string.set_current);
            processing_string = getString(R.string.processing);
            nearby_files_copied_string = getString(R.string.nearby_files_copied);
            nearby_files_skipped_string = getString(R.string.nearby_files_skipped);
            nearby_files_failed_string = getString(R.string.nearby_files_failed);
            no_response_string = getString(R.string.sync_server_noresponse_error);
            String title_string = getString(R.string.sync);
            String web_help = getString(R.string.website_sync);
            sync_waiting_for_info_string = getString(R.string.sync_waiting_for_info);
            sync_info_received_string = getString(R.string.sync_info_received);
            new_files_string = getString(R.string.sync_new_files_available);
            updated_files_string = getString(R.string.sync_updated_files_available);
            sync_extracting_string = getString(R.string.sync_extracting);
            set_is_empty_string = getString(R.string.set_is_empty);
            mainActivityInterface.updateToolbar(title_string);
            mainActivityInterface.updateToolbarHelp(web_help);
        }
    }

    private void setupViews() {
        // Show the progress bar
        showProgress(true);

        if (getActivity() != null) {
            if (syncViewPagerAdapter == null) {
                syncViewPagerAdapter = new SyncViewPagerAdapter(getActivity().getSupportFragmentManager(), this.getLifecycle());
                syncViewPagerAdapter.createFragment(0);
            }
            if (syncSongFragment == null) {
                syncSongFragment = (SyncItemsFragment) syncViewPagerAdapter.menuFragments[0];
            }
            if (syncSetFragment == null) {
                syncSetFragment = (SyncItemsFragment) syncViewPagerAdapter.createFragment(1);
            }
            if (syncProfileFragment == null) {
                syncProfileFragment = (SyncItemsFragment) syncViewPagerAdapter.createFragment(2);
            }

            // Give a reference back to this fragment
            syncSongFragment.setMainFragment(this);
            syncSetFragment.setMainFragment(this);
            syncProfileFragment.setMainFragment(this);

            myView.syncPager.setAdapter(syncViewPagerAdapter);
            myView.syncPager.setOffscreenPageLimit(2);
            TabLayout tabLayout = myView.syncTabs;
            new TabLayoutMediator(tabLayout, myView.syncPager, (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText(songs_string);
                        tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.music_note, null));
                        break;
                    case 1:
                        tab.setText(sets_string);
                        tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.list_number, null));
                        break;
                    case 2:
                        tab.setText(profiles_string);
                        tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.settings, null));
                        break;
                }
                // "removing" tooltip
                TooltipCompat.setTooltipText(tab.view, null);
            }).attach();
        }

        // Get a note of the connected devices into the exposedDropdown
        if (getContext() != null) {
            // Go through the connected devices and get the device names
            for (int i = 0; i < mainActivityInterface.getNearbyActions().getNearbyConnectionManagement().getConnectedDevices().size(); i++) {
                connectedDeviceCodes.add(mainActivityInterface.getNearbyActions().getNearbyConnectionManagement().getConnectedDevices().keyAt(i));
                connectedDeviceNames.add(mainActivityInterface.getNearbyActions().getNearbyConnectionManagement().getConnectedDevices().valueAt(i));
            }
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.chooseConnected, R.layout.view_exposed_dropdown_item, connectedDeviceNames);
            myView.chooseConnected.setAdapter(exposedDropDownArrayAdapter);
            myView.chooseConnected.setText("");
        }

        showContentInfo(false);

        showProgress(false);
    }

    private void setupListeners() {
        myView.chooseConnected.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                showContentInfo(false);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Only proceed if the device name isn't empty
                if (editable != null && !editable.toString().isEmpty()) {
                    // Wait for info from the required device.  Have a 10 sec timeout
                    try {
                        chosenDevice = editable.toString();
                        int pos = connectedDeviceNames.indexOf(editable.toString());
                        if (mainActivityInterface.getNearbyActions().getNearbyConnectionManagement().getConnectedDevices().size() >= pos) {
                            showProgress(true);
                            mainActivityInterface.getNearbyActions().getNearbySendPayloads().sendSyncInfoRequest(editable.toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    chosenDevice = "";
                }
            }
        });

        myView.checkForUpdates.setOnClickListener(view -> {

            showContentInfo(false);
            if (myView.chooseConnected.getText() != null) {
                try {
                    chosenDevice = myView.chooseConnected.getText().toString();
                    if (!chosenDevice.isEmpty()) {
                        // Wait for info from the required device.  Have a 10 sec timeout
                        int pos = connectedDeviceNames.indexOf(chosenDevice);
                        if (mainActivityInterface.getNearbyActions().getNearbyConnectionManagement().getConnectedDevices().size() >= pos) {
                            showProgress(true);
                            mainActivityInterface.getNearbyActions().getNearbySendPayloads().sendSyncInfoRequest(chosenDevice);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                chosenDevice = "";
            }
        });
    }

    public void showProgress(boolean show) {
        if (myView != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeout = false;
            if (show) {
                timeout = true;
                timeoutHandler.postDelayed(timeoutRunnable, 10000);
            }
            mainActivityInterface.getMainHandler().post(() -> {
                myView.hostProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                myView.dimBackground.setVisibility(show ? View.VISIBLE : View.GONE);
            });
        }
    }

    // Listening from NearbyConnections
    public void dealWithNearbyInfoReceived(NearbyJson nearbyJson) {
        Log.d(TAG,"info received");
        showProgress(true);
        this.nearbyJson = nearbyJson;
        mainActivityInterface.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // Update each fragment
                if (myView!=null) {
                    showContentInfo(true);
                    syncSongPrepared = false;
                    syncSetPrepared = false;
                    syncProfilePrepared = false;
                    syncSongFragment.prepareRecycler();
                    syncSetFragment.prepareRecycler();
                    syncProfileFragment.prepareRecycler();
                }
            }
        });
    }

    public void announceNotPrepared(String what) {
        switch (what) {
            case "songs":
                syncSongPrepared = false;
                break;
            case "sets":
                syncSetPrepared = false;
                break;
            case "profiles":
                syncProfilePrepared = false;
                break;
        }
        showProgress(true);
    }

    public void announcePrepared(String what) {
        switch (what) {
            case "songs":
                syncSongPrepared = true;
                break;
            case "sets":
                syncSetPrepared = true;
                break;
            case "profiles":
                syncProfilePrepared = true;
                break;
        }
        if (syncSongPrepared && syncSetPrepared && syncProfilePrepared) {
            showProgress(false);
        }
    }


    public void updateProgressText(int current, int total, String filename) {
        // Must do this on the UI
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView != null) {
                String current_string = processing_string + "\n" + current + "/" + total + ": " + filename;
                myView.hostProgressTextView.setText(current_string);
            }
        });
    }

    private void doImportCurrentSet() {
        if (nearbyCurrentSet != null && !nearbyCurrentSet.isEmpty()) {
            // Initialise the current set
            mainActivityInterface.getCurrentSet().setSetCurrent(nearbyCurrentSet);
            mainActivityInterface.getCurrentSet().setSetCurrentBeforeEdits("");
            // Wait before continuing (to ensure the current set preference is saved)
            mainActivityInterface.getMainHandler().postDelayed(() -> {
                mainActivityInterface.getSetActions().parseCurrentSet();
                if (mainActivityInterface.getCurrentSet().getCurrentSetSize() > 0) {
                    mainActivityInterface.getShowToast().success();
                } else {
                    mainActivityInterface.getShowToast().error();
                }
            }, 500);
        } else {
            mainActivityInterface.getShowToast().doIt(set_is_empty_string);
        }
    }


    public NearbyJson getNearbyJson() {
        return nearbyJson;
    }

    public String getChosenDevice() {
        return chosenDevice;
    }


    private void showContentInfo(boolean showContentInfo) {
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView != null) {
                myView.syncTabs.setVisibility(showContentInfo ? View.VISIBLE : View.GONE);
                myView.syncPager.setVisibility(showContentInfo ? View.VISIBLE : View.GONE);
            }
        });
    }
    public void doExtractFromZip(Uri zipUri, String what) {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            ArrayList<ShareableObject> songObjectsReceived = new ArrayList<>();
            if (what.equals("songs")) {
                Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Received","",mainActivityInterface.getNearbyActions().sharableObjectFile);
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
                if (inputStream!=null) {
                    // Get a record of the requested songs uuids and lastModified values
                    NearbyJson hostSongsJson = MainActivity.gson.fromJson(
                            mainActivityInterface.getStorageAccess().readTextFileToString(inputStream), NearbyJson.class);
                    if (hostSongsJson.getShareableSongObjects() != null) {
                        songObjectsReceived = hostSongsJson.getShareableSongObjects();
                    }
                }
                // Keep a note that we need to fully reindex
                mainActivityInterface.getSongListBuildIndex().setFullIndexRequired(true);
                mainActivityInterface.getSongListBuildIndex().setIndexRequired(true);
            }
            // Count the items
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(mainActivityInterface.getStorageAccess().getInputStream(zipUri)));
            int totalItemCount = countZipItems(zipInputStream);

            // Prepare to extract (the previous stream gets closed)
            zipInputStream = new ZipInputStream(new BufferedInputStream(mainActivityInterface.getStorageAccess().getInputStream(zipUri)));

            // Go through each entry and copy to the desired location
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            long starttime = System.currentTimeMillis();
            Log.d(TAG,"START now:" + starttime);
            int thisItem = 0;
            try {
                while ((ze = zipInputStream.getNextEntry()) != null) {
                    if (!ze.isDirectory()) {
                        thisItem++;
                        String folderToUse = null;
                        String subfolderToUse = "";
                        String filenameToUse = ze.getName();
                        updateProgressText(sync_extracting_string + " ("+ thisItem + "/"+ totalItemCount + "):\n" + filenameToUse);

                        if (filenameToUse.startsWith("/")) {
                            filenameToUse = filenameToUse.substring(1);
                        }

                        switch (what) {
                            case "songs":
                                folderToUse = "Songs";
                                subfolderToUse = mainActivityInterface.getMainfoldername();
                                if (ze.getName().contains("/")) {
                                    subfolderToUse = ze.getName().substring(0, ze.getName().lastIndexOf("/"));
                                    filenameToUse = filenameToUse.replace(subfolderToUse + "/", "");
                                }
                                break;
                            case "sets":
                                folderToUse = "Sets";
                                subfolderToUse = "";
                                break;
                            case "profiles":
                                folderToUse = "Profiles";
                                subfolderToUse = "";
                                break;
                        }

                        if (folderToUse != null && subfolderToUse != null) {
                            Uri uriForNewItem = mainActivityInterface.getStorageAccess().getUriForItem(folderToUse, subfolderToUse, filenameToUse);
                            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uriForNewItem, null, folderToUse, subfolderToUse, filenameToUse);
                            OutputStream outputStreamForNewItem = mainActivityInterface.getStorageAccess().getOutputStream(uriForNewItem);
                            // Write the file
                            int count;
                            StringBuilder errors = new StringBuilder();
                            //Log.d(TAG, "outputStreamForNewItem:" + outputStreamForNewItem);
                            //Log.d(TAG, "ze:" + ze.getName() + "  " + ze.getSize() + "kB  " + ze.getCompressedSize());
                            try {
                                if (outputStreamForNewItem != null && myView != null) {
                                    while ((count = zipInputStream.read(buffer)) != -1) {
                                        //Log.d(TAG, "Writing the buffer");
                                        outputStreamForNewItem.write(buffer, 0, count);
                                    }
                                    if (what.equals("songs")) {
                                        // Update or create an entry in the songs database
                                        Song existingSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(subfolderToUse,filenameToUse);
                                        if (existingSong == null) {
                                            existingSong = new Song();
                                            existingSong.setFilename(filenameToUse);
                                            existingSong.setFolder(folderToUse);
                                            mainActivityInterface.getSQLiteHelper().createSong(folderToUse,filenameToUse);
                                        }
                                        // Try to get the UUID and the lastModified values of the song we've received
                                        for (ShareableObject songObject : songObjectsReceived) {
                                            if (songObject.getFilename().equals(filenameToUse) && songObject.getFolder().equals(subfolderToUse)) {
                                                existingSong.setUuid(songObject.getUuid());
                                                existingSong.setLastModified(songObject.getLastModified());
                                                break;
                                            }
                                        }
                                        mainActivityInterface.getSQLiteHelper().updateSong(existingSong);
                                    }
                                } else if (myView==null) {
                                    // The user closed the window, so stop
                                    ze = null;
                                } else {
                                    Log.d(TAG, "error = " + ze.getName());
                                    errors.append(ze.getName()).append("\n");
                                    mainActivityInterface.getStorageAccess().updateCrashLog(ze.getName() + " - error synchronising file");
                                }
                            } catch (Exception e) {
                                mainActivityInterface.getStorageAccess().updateCrashLog("Synchronising item: " + ze.getName() + "\n" + e);
                                e.printStackTrace();
                            } finally {
                                if (outputStreamForNewItem!=null) {
                                    try {
                                        outputStreamForNewItem.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "Zip file finished!");
            }
            try {
                zipInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            long endtime = System.currentTimeMillis();
            Log.d(TAG,"END now: "+endtime);
            Log.d(TAG,"total time for buffer size "+buffer.length+"B:"+(endtime - starttime)+"ms");
            // Now refresh the matching items
            announceNotPrepared(what);
            switch (what) {
                case "songs":
                    syncSongFragment.prepareRecycler();
                    break;
                case "sets":
                    syncSetFragment.prepareRecycler();
                    break;
                case "profiles":
                    syncProfileFragment.prepareRecycler();
                    break;
            }
        });
    }

    private int countZipItems(ZipInputStream zipInputStream) {
        int totalZipItems = 0;
        if (zipInputStream != null) {
            try {
                while (zipInputStream.getNextEntry() != null) {
                    totalZipItems++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                zipInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG,"totalZipItems:"+totalZipItems);
        return totalZipItems;
    }

    public void updateProgressText(String progressText) {
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView!=null) {
                progressTextClearHandler.removeCallbacks(progressTextClearRunnable);
                myView.dimBackground.setVisibility(View.VISIBLE);
                myView.hostProgressBar.setVisibility(View.VISIBLE);
                myView.hostProgressTextView.setVisibility(View.VISIBLE);
                myView.hostProgressTextView.setText(progressText);
                progressTextClearHandler.postDelayed(progressTextClearRunnable,2000);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
        progressTextClearHandler.removeCallbacks(progressTextClearRunnable);
        // Empty the export folder
        mainActivityInterface.getStorageAccess().wipeFolder("Export","");
        // Rebuild the song index
        if (mainActivityInterface.getSongListBuildIndex().getFullIndexRequired()) {
            mainActivityInterface.indexSongs();
        }
    }
}

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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SyncNearbyFragment extends Fragment {
    // This fragment is used to request, then display a list of files available on the host device
    // It is only accessible on devices that are connected and are not running as hosts themselves
    // This fragment can be called from the SetActionsFragment, ProfileActionsFragment and SongActionsFragment

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "SyncNearbyFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsSyncBinding myView;
    @SuppressWarnings("FieldCanBeLocal")
    private String  sets_string = "", profiles_string = "", songs_string = "",
            no_response_string = "", info_string="", content_error_string = "",
            chosenDevice = "", sync_extracting_string="";
    private boolean syncSongPrepared = false;
    private boolean syncSetPrepared = false;
    private boolean syncProfilePrepared = false;
    private NearbyJson nearbyJson;
    private SyncViewPagerAdapter syncViewPagerAdapter;
    private SyncItemsFragment syncSongFragment, syncSetFragment, syncProfileFragment;
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
                    mainActivityInterface.getShowToast().doIt(no_response_string);
                }
            }
            timeout = false;
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

        // Tint the progressBar as the secondary color
        mainActivityInterface.getMyThemeColors().tintProgressBar(myView.hostProgressBar);

        prepareStrings();
        setupViews();
        setupListeners();
        mainActivityInterface.getNearbyActions().setSyncNearbyFragment(this);
        return myView.getRoot();
    }

    @Override
    public void onResume() {
        prepareStrings();
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
            sets_string = getString(R.string.set_lists);
            profiles_string = getString(R.string.profile);
            songs_string = getString(R.string.songs);
            no_response_string = getString(R.string.sync_server_noresponse_error);
            content_error_string  = getString(R.string.sync_content_error);
            String title_string = getString(R.string.sync);
            String web_help = getString(R.string.website_sync);
            sync_extracting_string = getString(R.string.sync_extracting);
            mainActivityInterface.updateToolbar(title_string);
            mainActivityInterface.updateToolbarHelp(web_help);
            info_string = getString(R.string.information);
        }
    }

    private void setupViews() {
        // Show the progress bar
        showProgress(true);

        if (getActivity() != null) {
            if (syncViewPagerAdapter == null) {
                syncViewPagerAdapter = new SyncViewPagerAdapter(getActivity().getSupportFragmentManager(), this.getLifecycle());
                //syncViewPagerAdapter.setContext(getContext());
            }
            if (syncSongFragment == null) {
                syncSongFragment = (SyncItemsFragment) syncViewPagerAdapter.createFragment(0);
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
                timeoutHandler.postDelayed(timeoutRunnable, 15000);
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
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Update each fragment
            Log.d(TAG,"myView:"+myView);
            if (myView!=null) {
                showContentInfo(true);
                syncSongPrepared = false;
                syncSetPrepared = false;
                syncProfilePrepared = false;
                syncSongFragment.prepareRecycler(getContext());
                syncSetFragment.prepareRecycler(getContext());
                syncProfileFragment.prepareRecycler(getContext());
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
        Log.d(TAG,"what:"+what+"  prepared");
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
        Log.d(TAG,"syncSongPrepared:"+syncSongPrepared+"  syncSetPrepared:"+syncSetPrepared+"  syncProfilePrepared:"+syncProfilePrepared);
        if (syncSongPrepared && syncSetPrepared && syncProfilePrepared) {
            showProgress(false);
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

            // Each item reminds the progress bar to stay visible
            try {
                while ((ze = zipInputStream.getNextEntry()) != null) {
                    announceNotPrepared(what);
                    if (!ze.isDirectory()) {
                        thisItem++;
                        String folderToUse = null;
                        String subfolderToUse = "";
                        String filenameToUse = ze.getName();
                        if (filenameToUse.endsWith(".json")) {
                            updateProgressText(sync_extracting_string + " ("+thisItem + "/"+ totalItemCount + "):\n" + filenameToUse.replace("_____","/").replace(".json","")+" "+info_string);
                        } else {
                            updateProgressText(sync_extracting_string + " (" + thisItem + "/" + totalItemCount + "):\n" + filenameToUse);
                        }
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
                                Log.d(TAG,"filenameToUse:"+filenameToUse);
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
                            Uri uriForNewItem;
                            if (filenameToUse.equals(mainActivityInterface.getNearbyActions().currentSetFile)) {
                                Log.d(TAG,"This is the current set");
                                folderToUse = "Received";
                                subfolderToUse = "";
                                filenameToUse = mainActivityInterface.getNearbyActions().currentSetFile;
                            }
                            if (filenameToUse.endsWith(".json") && filenameToUse.contains("_____")) {
                                // This is the info for the database
                                mainActivityInterface.getStorageAccess().makeSureFileIsRegistered("Received","",filenameToUse,true);
                                uriForNewItem = mainActivityInterface.getStorageAccess().getUriForItem("Received","",filenameToUse);
                            } else {
                                mainActivityInterface.getStorageAccess().makeSureFileIsRegistered(folderToUse,subfolderToUse,filenameToUse,true);
                                uriForNewItem = mainActivityInterface.getStorageAccess().getUriForItem(folderToUse, subfolderToUse, filenameToUse);
                            }
                            Log.d(TAG,"Extracting zip item to this Uri:"+uriForNewItem);
                            OutputStream outputStreamForNewItem = mainActivityInterface.getStorageAccess().getOutputStream(uriForNewItem);
                            // Write the file
                            int count;
                            StringBuilder errors = new StringBuilder();
                            try {
                                if (outputStreamForNewItem != null && myView != null) {
                                    while ((count = zipInputStream.read(buffer)) != -1) {
                                        outputStreamForNewItem.write(buffer, 0, count);
                                    }

                                    if (what.equals("songs") && !filenameToUse.endsWith(".json")) {
                                        // Update or create an entry in the songs database
                                        Song existingSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(subfolderToUse, filenameToUse);
                                        if (existingSong == null) {
                                            existingSong = new Song();
                                            existingSong.setFilename(filenameToUse);
                                            existingSong.setFolder(folderToUse);
                                            mainActivityInterface.getSQLiteHelper().createSong(folderToUse, filenameToUse);
                                        }
                                        // Get the new song content loaded ready for the database update
                                        existingSong = mainActivityInterface.getLoadSong().doLoadSongFile(existingSong, true);
                                        mainActivityInterface.getSQLiteHelper().updateSong(existingSong);
                                        mainActivityInterface.updateSongMenu(existingSong);

                                    } else if (what.equals("songs") && filenameToUse.endsWith(".json") && filenameToUse.contains("_____")) {
                                        // Update the songs database
                                        String songSubfolder = filenameToUse.substring(0, filenameToUse.lastIndexOf("_____")).replace("_____","/");
                                        String songFilename = filenameToUse.substring(filenameToUse.lastIndexOf("_____")).replace("_____","");
                                        Log.d(TAG,"songSubfolder:"+songSubfolder+"  songFilename:"+songFilename);
                                        String content = mainActivityInterface.getStorageAccess().readTextFileToString(
                                                mainActivityInterface.getStorageAccess().getInputStream(uriForNewItem));
                                        Song thisNonOSSong = MainActivity.gson.fromJson(content, Song.class);
                                        Log.d(TAG,"thisNonOSSong:"+thisNonOSSong);
                                        if (thisNonOSSong!=null) {
                                            Log.d(TAG,"thisNonOSSong.getUuid():"+thisNonOSSong.getUuid());

                                            // Now update this item in our databases
                                            if (mainActivityInterface.getSQLiteHelper().getSpecificSong(thisNonOSSong.getFolder(), thisNonOSSong.getFilename())==null) {
                                                Log.d(TAG,"Create song in our database");
                                                mainActivityInterface.getSQLiteHelper().createSong(thisNonOSSong.getFolder(), thisNonOSSong.getFilename());
                                            }
                                            if (mainActivityInterface.getNonOpenSongSQLiteHelper().getSpecificSong(thisNonOSSong.getFolder(), thisNonOSSong.getFilename())==null) {
                                                Log.d(TAG,"Create song in our NonOSSong database");
                                                mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(thisNonOSSong.getFolder(), thisNonOSSong.getFilename());
                                            }
                                            Log.d(TAG,"updating song in our database");
                                            mainActivityInterface.getSQLiteHelper().updateSong(thisNonOSSong);
                                            mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(thisNonOSSong);
                                        }

                                    } else if (filenameToUse.equals(mainActivityInterface.getNearbyActions().currentSetFile)) {
                                        // Load this set object into our current set
                                        mainActivityInterface.getSetActions().clearCurrentSet();
                                        ArrayList<Uri> setsToLoad = new ArrayList<>();
                                        setsToLoad.add(uriForNewItem);
                                        mainActivityInterface.getSetActions().loadSets(setsToLoad, mainActivityInterface.getCurrentSet(), "");
                                        mainActivityInterface.getSetActions().parseCurrentSet();
                                    }

                                } else if (myView==null) {
                                    // The user closed the window, so stop
                                    ze = null;

                                }
                            } catch (Exception e) {
                                Log.d(TAG, "error = " + ze.getName());
                                errors.append(ze.getName()).append("\n");
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

            endSync(what);

        });
    }

    private void endSync(String what) {
        long endtime = System.currentTimeMillis();
        Log.d(TAG, "END now: " + endtime);
        // Now refresh the matching items
        Log.d(TAG, "announce not prepared:" + what);
        mainActivityInterface.getMainHandler().postDelayed(() -> {
            if (myView!=null) {
                announceNotPrepared(what);
                showProgress(true);
                myView.dimBackground.setVisibility(View.VISIBLE);
                myView.hostProgressBar.setVisibility(View.VISIBLE);
                switch (what) {
                    case "songs":
                        syncSongFragment.prepareRecycler(getContext());
                        break;
                    case "sets":
                        syncSetFragment.prepareRecycler(getContext());
                        break;
                    case "profiles":
                        syncProfileFragment.prepareRecycler(getContext());
                        break;
                }
            }
        },500);
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
        // Empty the export folder and the received folder for json and zip file
        mainActivityInterface.getStorageAccess().wipeFolder("Export","");
        ArrayList<String> receivedItems = mainActivityInterface.getStorageAccess().listFilesInFolder("Received","");
        for (int i=0; i<receivedItems.size(); i++) {
            if (receivedItems.get(i).endsWith(".json") || receivedItems.get(i).endsWith(".zip")) {
                mainActivityInterface.getStorageAccess().doDeleteFile("Received", "", receivedItems.get(i));
            }
        }
        mainActivityInterface.getStorageAccess().wipeFolder("Received","");
        // Rebuild the song index
        if (mainActivityInterface.getSongListBuildIndex().getFullIndexRequired()) {
            mainActivityInterface.indexSongs();
        }
    }
}

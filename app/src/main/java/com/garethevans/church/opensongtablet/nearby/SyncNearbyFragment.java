package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.InformationBottomSheet;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsSyncBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class SyncNearbyFragment extends Fragment {
    // This fragment is used to request, then display a list of files available on the host device
    // It is only accessible on devices that are connected and are not running as hosts themselves
    // This fragment can be called from the SetActionsFragment, ProfileActionsFragment and SongActionsFragment

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BrowseHostFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsSyncBinding myView;
    @SuppressWarnings("FieldCanBeLocal")
    private String browse_host_files_string="", sets_string="", profiles_string="", songs_string="",
            set_current_string="", set_is_empty_string="", processing_string="",
            nearby_files_copied_string="", nearby_files_skipped_string="",
            nearby_files_failed_string="", no_response_string="";
    private int currentFile=0;
    private BrowseHostAdapter browseHostAdapter;
    private ArrayList<HostItem> checkedItems = new ArrayList<>();
    private boolean waitingForFiles = false, overwrite = false;
    private String requestedFolder, requestedSubfolder, requestedFilename, folder;
    private String nearbyCurrentSet=null;
    private final ArrayList<String> filesCopied = new ArrayList<>();
    private final ArrayList<String> filesSkipped = new ArrayList<>();
    private final ArrayList<String> filesFailed = new ArrayList<>();
    private SyncViewPagerAdapter syncViewPagerAdapter;
    private SyncSongFragment syncSongFragment;
    private SyncSetFragment syncSetFragment;
    private SyncProfileFragment syncProfileFragment;
    private ArrayList<String> connectedDeviceCodes = new ArrayList<>();
    private ArrayList<String> connectedDeviceNames = new ArrayList<>();
    private boolean timeout = false;
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (timeout) {
                showProgress(false);
                if (myView != null) {
                    myView.chooseConnected.setText("");
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
        mainActivityInterface.getNearbyConnections().setNearbySyncFragment(this);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsSyncBinding.inflate(inflater, container, false);
        return myView.getRoot();
    }

    @Override
    public void onResume() {
        prepareStrings();
        setupViews();
        setupListeners();
        mainActivityInterface.getNearbyConnections().setNearbySyncFragment(this);
        // Now request the files from the host and wait for a response
        //mainActivityInterface.getNearbyConnections().sendRequestHostItems();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mainActivityInterface.getNearbyConnections().setNearbySyncFragment(null);
        mainActivityInterface.setWhattodo("");
        super.onDestroy();
    }

    private void prepareStrings() {
        if (getContext()!=null && mainActivityInterface!=null && myView!=null) {
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

            /*switch (mainActivityInterface.getWhattodo()) {
                case "browsesets":
                default:
                    folder = "Sets";
                    title_string = browse_host_files_string + ": " + set_string;
                    web_help = getString(R.string.website_browse_host_files_set);
                    break;
                case "browseprofiles":
                    folder = "Profiles";
                    title_string = browse_host_files_string + ": " + profile_string;
                    web_help = getString(R.string.website_profiles);
                    break;
                case "browsesongs":
                    folder = "Songs";
                    title_string = browse_host_files_string + ": " + song_string;
                    web_help = getString(R.string.website_browse_host_files_songs);
                    break;
                case "browsecurrentset":
                    folder = "CurrentSet";
                    title_string = browse_host_files_string + ": " + set_current_string;
                    web_help = getString(R.string.website_browse_host_files_set);
            }*/
            set_is_empty_string = getString(R.string.set_is_empty);
            mainActivityInterface.updateToolbar(title_string);
            mainActivityInterface.updateToolbarHelp(web_help);
        }
    }

    private void setupViews() {
        // Show the progress bar
        showProgress(true);

        if (getActivity()!=null) {
            if (syncViewPagerAdapter == null) {
                syncViewPagerAdapter = new SyncViewPagerAdapter(getActivity().getSupportFragmentManager(), this.getLifecycle());
                syncViewPagerAdapter.createFragment(0);
            }
            if (syncSongFragment == null) {
                syncSongFragment = (SyncSongFragment) syncViewPagerAdapter.menuFragments[0];
            }
            if (syncSetFragment == null) {
                syncSetFragment = (SyncSetFragment) syncViewPagerAdapter.createFragment(1);
            }
            if (syncProfileFragment == null) {
                syncProfileFragment = (SyncProfileFragment) syncViewPagerAdapter.createFragment(2);

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
        if (getContext()!=null) {
            // Go through the connected devices and split the names/codes
            for (String endpoint:mainActivityInterface.getNearbyConnections().getConnectedEndpoints()) {
                if (!endpoint.contains("__")) {
                    endpoint = "UNKNOWN__" + endpoint;
                }
                String[] endpointSplit = endpoint.split("__");
                connectedDeviceCodes.add(endpointSplit[0]);
                connectedDeviceNames.add(endpointSplit[1]);
            }
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.chooseConnected, R.layout.view_exposed_dropdown_item, connectedDeviceNames);
            myView.chooseConnected.setAdapter(exposedDropDownArrayAdapter);
            myView.chooseConnected.setText("");
        }

        myView.syncTabs.setVisibility(View.GONE);
        myView.syncPager.setVisibility(View.VISIBLE);

        showProgress(false);
    }

    private void setupListeners() {
        //myView.nearbyBrowseSelectAll.setOnClickListener(view -> browseHostAdapter.selectAll(myView.nearbyBrowseSelectAll.isChecked()));
        /*myView.importNearbyFiles.setOnClickListener(view -> {
            myView.hostProgressTextView.setVisibility(View.VISIBLE);
            startGetFiles();
        });
        myView.importNearbyCurrentSet.setOnClickListener(view -> doImportCurrentSet());
    */

        myView.chooseConnected.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                // Only proceed if the device name isn't empty
                if (editable!=null && !editable.toString().isEmpty()) {
                    // Wait for info from the required device.  Have a 10 sec timeout
                    try {
                        int pos = connectedDeviceNames.indexOf(editable.toString());
                        if (mainActivityInterface.getNearbyConnections().getConnectedEndpoints().size()>=pos) {
                            showProgress(true);
                            timeout = true;
                            timeoutHandler.postDelayed(timeoutRunnable, 10000);
                            mainActivityInterface.getNearbyConnections().sendRequestHostItems(editable.toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void showProgress(boolean show) {
        if (myView!=null) {
            myView.hostProgressBar.setVisibility(show ? View.VISIBLE:View.GONE);
            myView.dimBackground.setVisibility(show ? View.VISIBLE:View.GONE);
        }
    }

    public void setNearbyCurrentSet(String nearbyCurrentSet) {
        this.nearbyCurrentSet = nearbyCurrentSet;
    }
    public void displayHostItems(String[] hostItems) {
        // We can now update the arrayAdapter on the main UI
        mainActivityInterface.getMainHandler().post(() -> {
            //myView.hostFilesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            //browseHostAdapter = new BrowseHostAdapter(getContext(),hostItems,folder);
            //myView.hostFilesRecycler.setAdapter(browseHostAdapter);
            //myView.dimBackground.setVisibility(View.GONE);
            //myView.hostProgressBar.setVisibility(View.GONE);

            /*if (nearbyCurrentSet!=null && !nearbyCurrentSet.isEmpty()) {
                myView.importNearbyCurrentSet.setVisibility(View.VISIBLE);
            }
            if (hostItems.length>0) {
                myView.importNearbyFiles.setVisibility(View.VISIBLE);
            }*/
            //myView.nearbyBrowseSelectAll.setVisibility(View.VISIBLE);
        });

    }

    public boolean getOverwrite() {
        return overwrite;
    }

    public void addFilesCopied(String filelocation) {
        filesCopied.add(filelocation);
    }

    public void addFilesSkipped(String filelocation) {
        filesSkipped.add(filelocation);
    }

    public void addFilesFailed(String filelocation) {
        filesFailed.add(filelocation);
    }

    private void startGetFiles() {
        // Get the checked items from the array
        filesCopied.clear();
        filesSkipped.clear();
        filesFailed.clear();

        checkedItems = browseHostAdapter.getCheckedItems();
        currentFile = 0;
        waitingForFiles = true;
        //overwrite = myView.nearbyOverwrite.getChecked();
        // Get the first file if chosen
        if (!checkedItems.isEmpty()) {
            getFile();
        }
    }

    private void getFile() {
        if (currentFile<checkedItems.size()) {
            // Tell the user what we are doing
            requestedFolder = checkedItems.get(currentFile).getFolder().trim();
            requestedSubfolder = checkedItems.get(currentFile).getSubfolder().trim();
            requestedFilename = checkedItems.get(currentFile).getFilename().trim();
            currentFile += 1;
            updateProgressText(currentFile,checkedItems.size(),requestedFilename);
            // Initiate the nearby request with a short delay
            mainActivityInterface.getMainHandler().postDelayed(() ->
            mainActivityInterface.getNearbyConnections().requestHostFile(
                    requestedFolder, requestedSubfolder, requestedFilename),50);
        } else {
            // We have finished!
            myView.hostProgressTextView.setText("");
            myView.hostProgressTextView.setVisibility(View.GONE);
            waitingForFiles = false;

            StringBuilder stringBuilder = getStringBuilder();

            // Update the song menu
            mainActivityInterface.updateSongList();

            // Show the results in an info bottom sheet
            InformationBottomSheet informationBottomSheet = new InformationBottomSheet(browse_host_files_string,stringBuilder.toString(),null,null);
            informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"InformationBottomSheet");
        }
    }

    private StringBuilder getStringBuilder() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(nearby_files_copied_string).append(":\n");
        for (String copied:filesCopied) {
            stringBuilder.append(copied).append("\n");
        }
        stringBuilder.append("\n");
        stringBuilder.append(nearby_files_skipped_string).append(":\n");
        for (String skipped:filesSkipped) {
            stringBuilder.append(skipped).append("\n");
        }
        stringBuilder.append("\n");
        stringBuilder.append(nearby_files_failed_string).append(":\n");
        for (String failed:filesFailed) {
            stringBuilder.append(failed).append("\n");
        }
        stringBuilder.append("\n");
        return stringBuilder;
    }

    public void updateProgressText(int current, int total, String filename) {
        // Must do this on the UI
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView!=null) {
                String current_string = processing_string + "\n" + current + "/" + total + ": "+filename;
                myView.hostProgressTextView.setText(current_string);
            }
        });
    }

    public String getRequestedFolder() {
        return requestedFolder;
    }
    public String getRequestedSubfolder() {
        return requestedSubfolder;
    }
    public String getRequestedFilename() {
        return requestedFilename;
    }

    public boolean getWaitingForFiles() {
        return waitingForFiles;
    }

    // Called from NearbyConnections once a file has been received
    public void continueGetFiles() {
        // When the payload has been received and dealt with, move on to the next file
        getFile();
    }

    private void doImportCurrentSet() {
        if (nearbyCurrentSet!=null && !nearbyCurrentSet.isEmpty()) {
            // Initialise the current set
            mainActivityInterface.getCurrentSet().setSetCurrent(nearbyCurrentSet);
            mainActivityInterface.getCurrentSet().setSetCurrentBeforeEdits("");
            // Wait before continuing (to ensure the current set preference is saved)
            mainActivityInterface.getMainHandler().postDelayed(() -> {
                mainActivityInterface.getSetActions().parseCurrentSet();
                if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
                    mainActivityInterface.getShowToast().success();
                } else {
                    mainActivityInterface.getShowToast().error();
                }
            },500);
        } else {
            mainActivityInterface.getShowToast().doIt(set_is_empty_string);
        }
    }
}

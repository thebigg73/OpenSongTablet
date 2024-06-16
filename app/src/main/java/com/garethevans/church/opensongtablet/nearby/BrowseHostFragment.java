package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsNearbyBrowseBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class BrowseHostFragment extends Fragment {
    // This fragment is used to request, then display a list of files available on the host device
    // It is only accessible on devices that are connected and are not running as hosts themselves
    // This fragment can be called from the SetActionsFragment, ProfileActionsFragment and SongActionsFragment

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BrowseHostFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsNearbyBrowseBinding myView;
    @SuppressWarnings("FieldCanBeLocal")
    private String browse_host_files_string="", set_string="", profile_string="", song_string="";
    private int currentFile=0;
    private BrowseHostAdapter browseHostAdapter;
    private ArrayList<HostItem> checkedItems = new ArrayList<>();
    private boolean waitingForFiles = false;
    private String requestedFolder, requestedSubfolder, requestedFilename, folder;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.getNearbyConnections().setBrowseHostFragment(this);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsNearbyBrowseBinding.inflate(inflater, container, false);
        return myView.getRoot();
    }

    @Override
    public void onResume() {
        prepareStrings();
        setupViews();
        setupListeners();
        browseHostAdapter = new BrowseHostAdapter(getContext());
        mainActivityInterface.getNearbyConnections().setBrowseHostFragment(this);
        // Now request the files from the host and wait for a response
        mainActivityInterface.getNearbyConnections().sendRequestHostItems();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mainActivityInterface.getNearbyConnections().setBrowseHostFragment(null);
        super.onDestroy();
    }

    private void prepareStrings() {
        if (getContext()!=null && mainActivityInterface!=null && myView!=null) {
            browse_host_files_string = getString(R.string.connections_browse_host);
            set_string = getString(R.string.set);
            profile_string = getString(R.string.profile);
            song_string = getString(R.string.song);
            String title_string;
            String web_help = "";
            switch (mainActivityInterface.getWhattodo()) {
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
            }
            mainActivityInterface.updateToolbar(title_string);
            mainActivityInterface.updateToolbarHelp(web_help);
        }
    }

    private void setupViews() {
        // When we start up, we need to show the dimmed background and progress bar and hide the rest
        myView.dimBackground.setVisibility(View.VISIBLE);
        myView.hostProgressBar.setVisibility(View.VISIBLE);
        myView.hostFilesRecycler.setVisibility(View.GONE);
    }

    private void setupListeners() {

    }

    public void displayHostItems(String[] hostItems) {
        // We can now update the arrayAdapter
        myView.hostFilesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        myView.hostFilesRecycler.setAdapter(browseHostAdapter);

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            for (String hostItem:hostItems) {
                Log.d(TAG,hostItem);
            }
            browseHostAdapter.prepareItems(hostItems,folder);
            mainActivityInterface.getMainHandler().post(() -> {
                myView.dimBackground.setVisibility(View.GONE);
                myView.hostProgressBar.setVisibility(View.GONE);
                myView.hostFilesRecycler.setVisibility(View.VISIBLE);
                Log.d(TAG,"adapterSize:"+browseHostAdapter.getItemCount());
            });
        });

    }

    private void startGetFiles() {
        // Get the checked items from the array
        checkedItems = browseHostAdapter.getCheckedItems();
        currentFile = 0;
        waitingForFiles = true;
        // Get the first file if chosen
        if (checkedItems.size()>0) {
            getFile();
        }
    }

    private void getFile() {
        if (currentFile<checkedItems.size()) {
            requestedFolder = checkedItems.get(currentFile).getFolder();
            requestedSubfolder = checkedItems.get(currentFile).getSubfolder();
            requestedFilename = checkedItems.get(currentFile).getFilename();
            currentFile += 1;
            // Initiated the nearby request
            mainActivityInterface.getNearbyConnections().requestHostFile(
                    requestedFolder, requestedSubfolder, requestedFilename);
        } else {
            waitingForFiles = false;
        }
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
}

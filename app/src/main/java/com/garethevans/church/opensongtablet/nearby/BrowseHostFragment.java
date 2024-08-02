package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.InformationBottomSheet;
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
    private String browse_host_files_string="", set_string="", profile_string="", song_string="",
            set_current_string="", set_is_empty_string="", processing_string="",
            nearby_files_copied_string="", nearby_files_skipped_string="",
            nearby_files_failed_string="";
    private int currentFile=0;
    private BrowseHostAdapter browseHostAdapter;
    private ArrayList<HostItem> checkedItems = new ArrayList<>();
    private boolean waitingForFiles = false, overwrite = false;
    private String requestedFolder, requestedSubfolder, requestedFilename, folder;
    private String nearbyCurrentSet=null;
    private final ArrayList<String> filesCopied = new ArrayList<>();
    private final ArrayList<String> filesSkipped = new ArrayList<>();
    private final ArrayList<String> filesFailed = new ArrayList<>();

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
        mainActivityInterface.getNearbyConnections().setBrowseHostFragment(this);
        // Now request the files from the host and wait for a response
        mainActivityInterface.getNearbyConnections().sendRequestHostItems();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mainActivityInterface.getNearbyConnections().setBrowseHostFragment(null);
        mainActivityInterface.setWhattodo("");
        super.onDestroy();
    }

    private void prepareStrings() {
        if (getContext()!=null && mainActivityInterface!=null && myView!=null) {
            browse_host_files_string = getString(R.string.connections_browse_host);
            set_string = getString(R.string.set);
            profile_string = getString(R.string.profile);
            song_string = getString(R.string.song);
            set_current_string = getString(R.string.set_current);
            processing_string = getString(R.string.processing);
            nearby_files_copied_string = getString(R.string.nearby_files_copied);
            nearby_files_skipped_string = getString(R.string.nearby_files_skipped);
            nearby_files_failed_string = getString(R.string.nearby_files_failed);
            String title_string;
            String web_help;
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
                case "browsecurrentset":
                    folder = "CurrentSet";
                    title_string = browse_host_files_string + ": " + set_current_string;
                    web_help = getString(R.string.website_browse_host_files_set);
            }
            set_is_empty_string = getString(R.string.set_is_empty);
            mainActivityInterface.updateToolbar(title_string);
            mainActivityInterface.updateToolbarHelp(web_help);
        }
    }

    private void setupViews() {
        // When we start up, we need to show the dimmed background and progress bar and hide the rest
        myView.dimBackground.setVisibility(View.VISIBLE);
        myView.hostProgressBar.setVisibility(View.VISIBLE);
        myView.hostFilesRecycler.setVisibility(View.VISIBLE);
        myView.importNearbyCurrentSet.setVisibility(View.GONE);
        myView.importNearbyCurrentSet.setVisibility(View.GONE);
    }

    private void setupListeners() {
        myView.nearbyBrowseSelectAll.setOnClickListener(view -> browseHostAdapter.selectAll(myView.nearbyBrowseSelectAll.isChecked()));
        myView.importNearbyFiles.setOnClickListener(view -> {
            myView.hostProgressTextView.setVisibility(View.VISIBLE);
            startGetFiles();
        });
        myView.importNearbyCurrentSet.setOnClickListener(view -> doImportCurrentSet());
    }

    public void setNearbyCurrentSet(String nearbyCurrentSet) {
        this.nearbyCurrentSet = nearbyCurrentSet;
    }
    public void displayHostItems(String[] hostItems) {
        // We can now update the arrayAdapter on the main UI
        mainActivityInterface.getMainHandler().post(() -> {
            myView.hostFilesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
            browseHostAdapter = new BrowseHostAdapter(getContext(),hostItems,folder);
            myView.hostFilesRecycler.setAdapter(browseHostAdapter);
            myView.dimBackground.setVisibility(View.GONE);
            myView.hostProgressBar.setVisibility(View.GONE);

            if (nearbyCurrentSet!=null && !nearbyCurrentSet.isEmpty()) {
                myView.importNearbyCurrentSet.setVisibility(View.VISIBLE);
            }
            if (hostItems.length>0) {
                myView.importNearbyFiles.setVisibility(View.VISIBLE);
            }
            myView.nearbyBrowseSelectAll.setVisibility(View.VISIBLE);
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
        overwrite = myView.nearbyOverwrite.getChecked();
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

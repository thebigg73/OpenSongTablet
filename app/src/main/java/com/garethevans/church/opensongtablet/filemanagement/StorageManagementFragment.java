package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.StorageFolderDisplayBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StorageManagementFragment extends Fragment {

    private StorageFolderDisplayBinding myView;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "StorageManagement";
    private MainActivityInterface mainActivityInterface;
    private ArrayList<String> infos;
    private ArrayList<View> views = new ArrayList<>();
    private ArrayList<Boolean> rects = new ArrayList<>();
    private String currentSubDir, storage_manage_string="", website_storage_overview_string="",
            root_string="", songs_string="", mainfoldername_string="", storage_reset_string="",
            storage_main_string="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = StorageFolderDisplayBinding.inflate(inflater, container, false);

        prepareStrings();

        mainActivityInterface.updateToolbar(storage_manage_string);
        mainActivityInterface.updateToolbarHelp(website_storage_overview_string);

        // Do this as separate tasks in a new thread
        setUpThread();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            storage_manage_string = getString(R.string.storage_manage);
            website_storage_overview_string = getString(R.string.website_storage_overview);
            root_string = getString(R.string.root);
            songs_string = getString(R.string.songs);
            mainfoldername_string = getString(R.string.mainfoldername);
            storage_reset_string = getString(R.string.storage_reset);
            storage_main_string = getString(R.string.storage_main);
        }
    }
    private void setUpThread() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                myView.progressBar.setVisibility(View.VISIBLE);
                String text = "OpenSong\n("+root_string+")";
                myView.rootFolder.setText(text);
                myView.rootFolder.setOnClickListener(v -> showActionDialog(true,false,""));
                text = songs_string+"\n("+mainfoldername_string+")";
                myView.mainFolder.setText(text);
                myView.mainFolder.setOnClickListener(v -> showActionDialog(false,true,""));
                // Now look for subfolders
                createNodes();
                myView.getRoot().invalidate();
                myView.folderList.invalidate();
                myView.storageGraph.setVisibility(View.VISIBLE);
                myView.progressBar.setVisibility(View.GONE);
                mainActivityInterface.getWindowFlags().hideKeyboard();
            });

            // Prepare the showcase
            initialiseShowcaseArrays();
            handler.post(() -> {
                prepareShowcaseViews();
                if (getActivity()!=null) {
                    mainActivityInterface.getShowCase().sequenceShowCase(getActivity(), views, null, infos, rects, "storageManagement");
                }
            });
        });
    }

    private void createNodes() {
        ArrayList<String> availableFolders = getFoldersFromFile();

        if (getContext()!=null) {
            for (String folder : availableFolders) {
                TextView textView = new TextView(getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(24, 0, 24, 24);
                lp.gravity = Gravity.CENTER_HORIZONTAL;
                textView.setLayoutParams(lp);
                textView.setText(folder);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setPadding(48, 48, 48, 48);
                textView.setBackgroundColor(getResources().getColor(R.color.blue));
                textView.setOnClickListener(v -> showActionDialog(false, false, folder));
                myView.folderList.addView(textView);
            }
            if (availableFolders.size() > 0) {
                myView.subFolderArrow.setVisibility(View.VISIBLE);
                myView.folderList.setVisibility(View.VISIBLE);

            } else {
                myView.subFolderArrow.setVisibility(View.GONE);
                myView.folderList.setVisibility(View.GONE);
            }
        }
    }


    private void initialiseShowcaseArrays() {
        views = new ArrayList<>();
        infos = new ArrayList<>();
        rects = new ArrayList<>();
        infos.add(storage_reset_string);
        rects.add(true);
        infos.add(storage_main_string);
        rects.add(true);
    }

    private void prepareShowcaseViews() {
        views.add(myView.rootFolder);
        views.add(myView.mainFolder);
    }

    private void showActionDialog(boolean root, boolean songs, String folder) {
        currentSubDir = folder;
        if (getActivity() != null) {
            FolderManagementBottomSheet dialogFragment = new FolderManagementBottomSheet(this, root, songs, folder);
            dialogFragment.show(getActivity().getSupportFragmentManager(), "folderManagementDialog");
        }
    }

    public void updateFragment() {
        // Called from MainActivity when change has been made from DialogFragment
        myView.folderList.removeAllViews();
        setUpThread();
    }

    private ArrayList<String> getFoldersFromFile() {
        // Scan the storage
        ArrayList<String> songIDs = mainActivityInterface.getStorageAccess().listSongs();
        mainActivityInterface.getStorageAccess().writeSongIDFile(songIDs);
        // Each subdir ends with /
        return mainActivityInterface.getStorageAccess().getSongFolders(songIDs,false,null);
    }


    // Received back from TextInputBottomSheet via MainActivity
    public void createNewFolder(String foldername) {
        // Get the current sub dir
        String safeFolder = mainActivityInterface.getStorageAccess().safeFilename(foldername);
        if (mainActivityInterface.getStorageAccess().createFolder("Songs", currentSubDir,
                safeFolder,true)) {
            updateFragment();
        }
    }

    public void renameFolder(String foldername) {
        String safeFolder = mainActivityInterface.getStorageAccess().safeFilename(foldername);
        // Try to rename.  This will check if it already exists and will return success on creation
        // This also displays the desired toast message
        if (mainActivityInterface.getStorageAccess().renameFolder(currentSubDir, safeFolder,true)) {
                updateFragment();
        }
    }

    private boolean uriExists(String newFolder) {
        // Return true if it doesn't already exist.
        String folder = getNewSubfolder(newFolder);
        Uri newUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",folder,"");
        return mainActivityInterface.getStorageAccess().uriExists(newUri);
    }

    private String getNewSubfolder(String newFolder) {
        String folder = "";
        if (currentSubDir != null && !currentSubDir.isEmpty()
                && !currentSubDir.equals(mainfoldername_string)) {
            folder += currentSubDir + "/";
        } else {
            folder += newFolder;
        }
        return folder.replace("//", "/");
    }
}

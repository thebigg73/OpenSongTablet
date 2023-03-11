package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.StorageKitkatChooserBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class KitKatFolderChooseFragment extends Fragment {
    // I had used external libraries, but wanted to break free!!!

    private MainActivityInterface mainActivityInterface;
    private File currentFolder;
    private ArrayList<File> foldersInDirectory;
    private StorageKitkatChooserBinding myView;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "KitKatChooser";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = StorageKitkatChooserBinding.inflate(inflater, container, false);

        // Prepare the gridview with the available folders
        getFoldersInDirectory(Environment.getExternalStorageDirectory().toString(),Environment.getExternalStorageDirectory().toString());

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void getFoldersInDirectory(String previous, String directory) {
        // Get the items
        File testLocation = new File(directory);
        File[] items = testLocation.listFiles();
        if (items!=null) {
            foldersInDirectory = new ArrayList<>();
            ArrayList<String> folderNames = new ArrayList<>();
            currentFolder = new File(directory);

            for (File item : items) {
                Log.d(TAG,"item:"+item);
                if (item.isDirectory() && !item.getName().startsWith(".")) {
                    // If it is a folder, add it
                    foldersInDirectory.add(item);
                }
            }
            // Sort them alphabetically
            Collections.sort(foldersInDirectory);

            // Now we have them in order, create a short name array
            for (File folder:foldersInDirectory) {
                Log.d(TAG,"folder:"+folder+"  .getName():"+folder.getName());
                folderNames.add(folder.getName());
            }

            // Put the root name in the top text
            myView.locationText.setText(currentFolder.getPath());

            // Add the array to the gridview
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), R.layout.view_kitkatfolder, folderNames);
            myView.gridView.setAdapter(arrayAdapter);
            myView.gridView.invalidate();

            // Set the listener for the gridview items
            myView.gridView.setOnItemClickListener((parent, view, position, id) -> {
                // Change the top level to this
                getFoldersInDirectory(currentFolder.getPath(),foldersInDirectory.get(position).getPath());
            });

            // Decide if we can enable the select button
            if (currentFolder.canWrite()) {
                myView.select.setVisibility(View.VISIBLE);
            } else {
                myView.select.setVisibility(View.GONE);
            }
        } else {
            // In case there was nothing we are allowed to see!
            currentFolder = new File(previous);
        }
    }

    private void setListeners() {
        myView.locationText.setOnClickListener(v -> {
            // Remove the last folder
            if (currentFolder.getParent()!=null) {
                getFoldersInDirectory(currentFolder.getPath(), currentFolder.getParent());
            }
        });

        myView.select.setOnClickListener(v -> {
            // Get a reference to the selected folder
            mainActivityInterface.setWhattodo("kitkat:"+currentFolder.getPath());

            // Open the storage location fragment and write it there
            if (getContext()!=null) {
                mainActivityInterface.navigateToFragment(getString(R.string.deeplink_set_storage), 0);
            }
        });
    }
}
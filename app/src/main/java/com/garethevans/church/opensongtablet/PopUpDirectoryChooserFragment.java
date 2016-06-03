package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PopUpDirectoryChooserFragment extends DialogFragment {

    static PopUpDirectoryChooserFragment newInstance() {
        PopUpDirectoryChooserFragment frag;
        frag = new PopUpDirectoryChooserFragment();
        return frag;
    }

    public interface MyInterface {
        void updateCustomStorage();
        void updateLinksPopUp();
    }

    private MyInterface mListener;

    @Override
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    ImageView navigateUp;
    public static TextView currentFolder;
    ListView directoryList;
    Button closeButton;
    Button selectButton;
    public static File location = StorageChooser.customStorageLoc;
    public static String[] splitlocation;
    public static List<String> tempProperDirectories;
    public static List<String> tempProperDirectoriesAndFiles;
    public static List<String> tempProperFolders;
    public static List<String> tempProperFiles;
    static Collator coll;
    String chooserAction;
    public static Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle(getActivity().getResources().getString(R.string.storage_choose));
        View V = inflater.inflate(R.layout.popup_folderexplorer, container, false);
        context = getActivity().getBaseContext();

        FullscreenActivity.filechosen = null;

        // Set the emulated storage as the default location if it is empty or not valid
        if (!location.isDirectory() || !location.canWrite()) {
            //location = Environment.getExternalStorageDirectory();
            location = FullscreenActivity.homedir;
        }
        currentFolder = (TextView) V.findViewById(R.id.currentFolderText);
        currentFolder.setText(location.toString());

        // Identify the listview which will either just show folders, or folders and files
        directoryList = (ListView) V.findViewById(R.id.folderListView);

        // Set up the close fragment button
        closeButton = (Button) V.findViewById(R.id.folderChooseCancel);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FullscreenActivity.myToastMessage = "link_other";
                dismiss();
            }
        });

        // Set up the select/ok button used to select the current folder
        // If we are using the fragment as a file chooser, this button is hidden later
        selectButton = (Button) V.findViewById(R.id.folderChooseOk);
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageChooser.customStorageLoc = location;
                mListener.updateCustomStorage();
                dismiss();
            }
        });

        // Set up the navigate up arrow button
        navigateUp = (ImageView) V.findViewById(R.id.upFolderButton);
        navigateUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doNavigateUp();
            }
        });

        // Decide if we are using the fragment as a folder chooser or a file chooser
        // If it is a file chooser, we need to hide the select/ok button
        // We need to set the appropriate action for the navigate up and list views
        // tempProperDirectoriesAndFiles or tempProperDirectories will populate the listview
        chooserAction = getArguments().getString("type");
        if (chooserAction!=null && chooserAction.equals("file")) {
            // File browser mode (navigate through folders, clicking on a file gets its path
            // Hide the ok button
            selectButton.setVisibility(View.GONE);
            // List all folders and files in the current location
            listFoldersAndFiles();

        } else {
            // Folder browser mode (navigate through folders, clicking on ok gets its path)
            // List all folders in the current location
            listFolders();
        }

        directoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the item clicked
                String itemclicked;

                if (chooserAction.equals("file")) {
                    if (tempProperDirectoriesAndFiles.size()>position) {
                        itemclicked = tempProperDirectoriesAndFiles.get(position);
                    } else {
                        listFoldersAndFiles();
                        itemclicked="";
                    }
                } else {
                    if (tempProperDirectories.size()>position) {
                        itemclicked = tempProperDirectories.get(position);
                    } else {
                        listFolders();
                        itemclicked="";
                    }
                }
                itemclicked = itemclicked.replace("/","");
                // Check if link clicked is a folder or a file.
                location = new File(location + "/" + itemclicked);
                if (location.isDirectory()) {
                    // List the new folder contents
                    if (chooserAction.equals("file")) {
                        listFoldersAndFiles();
                    } else {
                        listFolders();
                    }
                } else {
                    // This is the file we want (folder chooser won't list files!)
                    FullscreenActivity.filechosen = location;
                    Log.d("d",""+location.toString());
                    mListener.updateCustomStorage();
                    if (FullscreenActivity.filetoselect.equals("audiolink") || FullscreenActivity.filetoselect.equals("otherlink")) {
                        mListener.updateLinksPopUp();
                    }
                    dismiss();
                }
            }
        });

        return V;
    }

    public void doNavigateUp() {
        // If we can, remove the last part of the folder name
        if (splitlocation.length>1) {
            String newlocation = location.toString().replace("/"+splitlocation[splitlocation.length-1],"");
            if (!newlocation.contains("/")) {
                newlocation = "/" + newlocation;
            }
            try {
                location = new File(newlocation);
                if (chooserAction.equals("file")) {
                    listFoldersAndFiles();
                } else {
                    listFolders();
                }
            } catch (Exception e) {
                FullscreenActivity.myToastMessage = getResources().getString(R.string.pad_error);
                ShowToast.showToast(getActivity());
            }
        }
    }

    public void listFoldersAndFiles() {
        splitlocation = location.toString().split("/");
        File[] tempmyitems = location.listFiles();

        //Now set the size of the temp arrays
        tempProperFolders = new ArrayList<>();
        tempProperFiles = new ArrayList<>();
        tempProperDirectoriesAndFiles = new ArrayList<>();

        //Now read the stuff into the temp array
        if (tempmyitems!=null) {
            for (File tempmyitem : tempmyitems) {
                if (tempmyitem != null) {
                    if (tempmyitem.isDirectory()) {
                        tempProperFolders.add(tempmyitem.getName() + "/");
                    } else {
                        tempProperFiles.add(tempmyitem.getName());
                    }
                }
            }

            //Sort these arrays
            // Add locale sort
            coll = Collator.getInstance(FullscreenActivity.locale);
            coll.setStrength(Collator.SECONDARY);
            Collections.sort(tempProperFolders, coll);
            Collections.sort(tempProperFiles, coll);

            tempProperDirectoriesAndFiles.addAll(tempProperFolders);
            tempProperDirectoriesAndFiles.addAll(tempProperFiles);

            // Update the listView with the folders
            ArrayAdapter<String> listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, tempProperDirectoriesAndFiles);
            directoryList.setAdapter(listAdapter);
            currentFolder.setText(location.toString());
        }
    }

    public void listFolders() {
        splitlocation = location.toString().split("/");
        File[] tempmyitems = location.listFiles();
        int tempnumitems;
        if (tempmyitems != null && tempmyitems.length>0) {
            tempnumitems = tempmyitems.length;
        } else {
            tempnumitems = 0;
        }
        int numactualdirs  = 0;
        for (int x=0; x<tempnumitems; x++) {
            if (tempmyitems[x] != null && tempmyitems[x].isDirectory()){
                numactualdirs ++;
            }
        }

        //Now set the size of the temp arrays
        tempProperDirectories = new ArrayList<>();

        //Now read the stuff into the temp array
        for (int x=0; x<numactualdirs; x++) {
            if (tempmyitems[x] != null && tempmyitems[x].isDirectory()) {
                tempProperDirectories.add(tempmyitems[x].getName());
            }
        }

        //Sort these arrays
        // Add locale sort
        coll = Collator.getInstance(FullscreenActivity.locale);
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(tempProperDirectories, coll);

        // Update the listView with the folders
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, tempProperDirectories);
        directoryList.setAdapter(listAdapter);

        currentFolder.setText(location.toString());
    }

    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null) {
            return;
        }
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

}

package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PopUpDirectoryChooserFragment extends DialogFragment {

    static PopUpDirectoryChooserFragment newInstance() {
        PopUpDirectoryChooserFragment frag;
        frag = new PopUpDirectoryChooserFragment();
        return frag;
    }

    public interface MyInterface {
        void updateCustomStorage();
        //void updateLinksPopUp();
        void openFragment();
    }

    interface SettingsInterface {
        void openStorageFragment();
    }

    private MyInterface mListener;
    private SettingsInterface sListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        if (FullscreenActivity.whattodo.equals("splashpagestorage")) {
            sListener = (SettingsInterface) activity;
        } else {
            mListener = (MyInterface) activity;
        }
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    ImageView navigateUp;
    public TextView currentFolder;
    ListView directoryList;
    public static File location = Environment.getExternalStorageDirectory();
    public static String[] splitlocation;
    public static List<String> tempProperDirectories;
    public static List<String> tempProperDirectoriesAndFiles;
    public static List<String> tempProperFolders;
    public static List<String> tempProperFiles;
    static Collator coll;
    String chooserAction;
    public Context context;
    FloatingActionButton selectButton;
    TextView isWritableText;

    public void onStart() {
        super.onStart();

        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            if (chooserAction!=null && chooserAction.equals("findosbfile")) {
                title.setText(getActivity().getResources().getString(R.string.backup_import));
            } else {
                title.setText(getActivity().getResources().getString(R.string.storage_choose));
            }
            final FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomAnimations.animateFAB(closeMe,getActivity());
                    closeMe.setEnabled(false);
                    dismiss();
                }
            });
            selectButton = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.saveMe);
            selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomAnimations.animateFAB(selectButton,getActivity());
                    selectButton.setEnabled(false);
                    doSave();
                }
            });
            if (chooserAction != null && chooserAction.contains("file")) {
                selectButton.setVisibility(View.GONE);
            }
        } else {
            getDialog().setTitle(getActivity().getResources().getString(R.string.storage_choose));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle(getActivity().getResources().getString(R.string.storage_choose));
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_folderexplorer, container, false);
        context = getActivity().getBaseContext();

        FullscreenActivity.filechosen = null;

        // Set the emulated storage as the default location if it is empty or not valid
        if (!location.isDirectory() || !location.canWrite()) {
            location = FullscreenActivity.homedir;
        }
        currentFolder = (TextView) V.findViewById(R.id.currentFolderText);
        currentFolder.setText(location.toString());
        isWritableText = (TextView) V.findViewById(R.id.isWritableText);

        // Identify the listview which will either just show folders, or folders and files
        directoryList = (ListView) V.findViewById(R.id.folderListView);

        // Set up the navigate up arrow button
        navigateUp = (FloatingActionButton) V.findViewById(R.id.upFolderButton);
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
            // List all folders and files in the current location
            listFoldersAndFiles();

        } else if (chooserAction!=null && chooserAction.equals("findosbfiles")) {
            // File browser mode (navigate through folders, clicking on a file gets its path
            // List all folders and files in the current location
            listFoldersAndFilesOSB();

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

                if (chooserAction.contains("file")) {
                    if (tempProperDirectoriesAndFiles.size()>position) {
                        itemclicked = tempProperDirectoriesAndFiles.get(position);
                    } else {
                        if (chooserAction.equals("file")) {
                            listFoldersAndFiles();
                        } else {
                            listFoldersAndFilesOSB();
                        }
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
                    if (chooserAction.contains("file")) {
                        if (chooserAction.equals(("file"))) {
                            listFoldersAndFiles();
                        } else {
                            listFoldersAndFilesOSB();
                        }
                    } else {
                        listFolders();
                    }
                } else {
                    // This is the file we want (folder chooser won't list files!)
                    FullscreenActivity.filechosen = location;
                    Log.d("d",""+location.toString());
                    if (chooserAction.equals("file")) {
                        mListener.updateCustomStorage();
                    } else if (chooserAction.equals("findosbfiles")) {
                        FullscreenActivity.whattodo = "processimportosb";
                        mListener.openFragment();
                    }
                    if (FullscreenActivity.filetoselect.equals("audiolink") || FullscreenActivity.filetoselect.equals("otherlink")) {
                        //mListener.updateLinksPopUp();
                        FullscreenActivity.whattodo = "page_links";
                        mListener.openFragment();

                    }
                    dismiss();
                }
            }
        });

        checkCanWrite();

        return V;
    }

    public void doSave() {
        //StorageChooser.customStorageLoc = location;
        FullscreenActivity.customStorage = location.toString();
        if (FullscreenActivity.whattodo.equals("splashpagestorage")) {
            sListener.openStorageFragment();
        } else {
            mListener.updateCustomStorage();
        }

        dismiss();
    }

    public void checkCanWrite() {
        // If folder is writeable, set the button to OK
        // If not, disable it.
        if (location.canWrite()) {
            if (selectButton!=null) {
                selectButton.setVisibility(View.VISIBLE);
                selectButton.setEnabled(true);
            }
            isWritableText.setVisibility(View.INVISIBLE);
            Log.d("d",location+" is writable");
        } else {
            if (selectButton!=null) {
                selectButton.setVisibility(View.GONE);
                selectButton.setEnabled(false);
            }
            isWritableText.setVisibility(View.VISIBLE);
            Log.d("d",location+" is NOT writable");
        }
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
                if (chooserAction.contains("file")) {
                    if (chooserAction.equals("file")) {
                        listFoldersAndFiles();
                    } else {
                        listFoldersAndFilesOSB();
                    }
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
            if  (FullscreenActivity.locale==null) {
                FullscreenActivity.locale = new Locale(Locale.getDefault().getDisplayLanguage());
            }
            coll = Collator.getInstance(FullscreenActivity.locale);
            coll.setStrength(Collator.SECONDARY);
            Collections.sort(tempProperFolders, coll);
            Collections.sort(tempProperFiles, coll);

            if (tempProperFolders!=null) {
                tempProperDirectoriesAndFiles.addAll(tempProperFolders);
            }
            if (tempProperFiles!=null) {
                tempProperDirectoriesAndFiles.addAll(tempProperFiles);
            }

            // Update the listView with the folders
            ArrayAdapter<String> listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, tempProperDirectoriesAndFiles);
            directoryList.setAdapter(listAdapter);
            currentFolder.setText(location.toString());
        }
    }

    public void listFoldersAndFilesOSB() {
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
                        if (tempmyitem.getName().endsWith(".osb")) {
                            tempProperFiles.add(tempmyitem.getName());
                        }
                    }
                }
            }

            //Sort these arrays
            // Add locale sort
            if  (FullscreenActivity.locale==null) {
                FullscreenActivity.locale = new Locale(Locale.getDefault().getDisplayLanguage());
            }
            coll = Collator.getInstance(FullscreenActivity.locale);
            coll.setStrength(Collator.SECONDARY);
            Collections.sort(tempProperFolders, coll);
            Collections.sort(tempProperFiles, coll);

            if (tempProperFolders!=null) {
                tempProperDirectoriesAndFiles.addAll(tempProperFolders);
            }
            if (tempProperFiles!=null) {
                tempProperDirectoriesAndFiles.addAll(tempProperFiles);
            }

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

        checkCanWrite();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}

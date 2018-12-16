package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.provider.DocumentFile;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PopUpImportExportOSBFragment extends DialogFragment {

    static PopUpImportExportOSBFragment newInstance() {
        PopUpImportExportOSBFragment frag;
        frag = new PopUpImportExportOSBFragment();
        return frag;
    }

    public interface MyInterface {
        void openFragment();
        void backupInstall(String m);
        void selectAFileUri(String s);
    }

    private PopUpImportExportOSBFragment.MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (PopUpImportExportOSBFragment.MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    LinearLayout importfilechooser;
    LinearLayout zipcontents;
    TextView chooseosbfile;
    ListView folderlist;
    String selectednote;
    String mTitle;
    ArrayAdapter<String> adapter;
    PrepareFolderListImport prepare_folder_list_import;
    SelectedFolderImport selected_folder_import;
    ArrayList<String> foldersfoundinzip, createdfolders;
    String[] foldersselectedtoimport;
    String message = "";
    ProgressBar waiting;
    FloatingActionButton closeMe;
    FloatingActionButton saveMe;
    SwitchCompat overwrite;
    StorageAccess storageAccess;
    ExportPreparer exportPreparer;
    ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Decide if we are importing or exporting
        if (FullscreenActivity.whattodo.equals("processimportosb")) {
            mTitle = getActivity().getResources().getString(R.string.backup_import);
        } else {
            mTitle = getActivity().getResources().getString(R.string.backup_export);
        }
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_importexportosb, container, false);

        storageAccess = new StorageAccess();

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(mTitle);
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopUpImportExportOSBFragment.this.dismiss();
            }
        });
        saveMe = V.findViewById(R.id.saveMe);
        if (FullscreenActivity.whattodo.equals("processimportosb")) {
            saveMe.setVisibility(View.GONE);
            saveMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomAnimations.animateFAB(saveMe, PopUpImportExportOSBFragment.this.getActivity());
                    saveMe.setEnabled(false);
                    PopUpImportExportOSBFragment.this.doTheImporting();
                }
            });
        } else {
            saveMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomAnimations.animateFAB(saveMe, PopUpImportExportOSBFragment.this.getActivity());
                    saveMe.setEnabled(false);
                    PopUpImportExportOSBFragment.this.doTheExporting();
                }
            });
        }

        // Initialise the views
        importfilechooser = V.findViewById(R.id.importfilechooser);
        zipcontents = V.findViewById(R.id.zipcontents);
        chooseosbfile = V.findViewById(R.id.chooseosbfile);
        folderlist = V.findViewById(R.id.folderlist);
        waiting = V.findViewById(R.id.waiting);
        overwrite = V.findViewById(R.id.overwrite);
        progressBar = V.findViewById(R.id.progressBar);

        // Listener for choose osb file
        chooseosbfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.selectAFileUri(getActivity().getString(R.string.backup_import));
                    mListener.openFragment();
                }
                PopUpImportExportOSBFragment.this.dismiss();
            }
        });


        // Set up the folder list and hide the required layouts
        if (FullscreenActivity.whattodo.equals("processimportosb")) {
            importfilechooser.setVisibility(View.VISIBLE);
            overwrite.setVisibility(View.GONE);
            zipcontents.setVisibility(View.GONE);
        } else {
            importfilechooser.setVisibility(View.GONE);
            overwrite.setVisibility(View.GONE);
            zipcontents.setVisibility(View.VISIBLE);
            prepareFolderListExport();
        }

        if (FullscreenActivity.file_uri!=null && FullscreenActivity.whattodo.equals("processimportosb")) {
            // We must be importing and have selected an appropriate .osb file
            importfilechooser.setVisibility(View.VISIBLE);
            chooseosbfile.setText(FullscreenActivity.file_uri.getLastPathSegment());
            waiting.setVisibility(View.VISIBLE);
            prepareFolderListImport();

        }
        // Reset the folders selected text note
        selectednote = "";

        folderlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (folderlist.isItemChecked(i)) {
                    // Add the folder if it isn't there already
                    if (!selectednote.contains("%__" + FullscreenActivity.mSongFolderNames[i] + "__%")) {
                        selectednote = selectednote + "%__" + FullscreenActivity.mSongFolderNames[i] + "__%";
                    }

                } else {
                    // Remove the folder if it is there already
                    if (selectednote.contains("%__" + FullscreenActivity.mSongFolderNames[i] + "__%")) {
                        selectednote = selectednote.replace("%__" + FullscreenActivity.mSongFolderNames[i] + "__%", "");
                    }
                }
            }
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void prepareFolderListExport() {
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, FullscreenActivity.mSongFolderNames);
        folderlist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        folderlist.setAdapter(adapter);
    }

    public void doTheExporting() {
        // Get a note of the folders chosen and add them to a string
        progressBar.setVisibility(View.VISIBLE);
        exportPreparer = new ExportPreparer();
        new Thread(new Runnable() {
            @Override
            public void run() {
                exportPreparer.createSelectedOSB(getActivity(),selectednote, storageAccess);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void prepareFolderListImport() {
        // We need to parse the .osb (zip) file to extract a list of folders it contains as AsyncTask
        prepare_folder_list_import = new PrepareFolderListImport();
        waiting.setVisibility(View.VISIBLE);
        prepare_folder_list_import.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    @SuppressLint("StaticFieldLeak")
    private class PrepareFolderListImport extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            ZipInputStream zis = null;
            foldersfoundinzip = new ArrayList<>();
            foldersfoundinzip.clear();
            try {
                InputStream inputStream = storageAccess.getInputStream(getActivity(),FullscreenActivity.file_uri);
                zis = new ZipInputStream(new BufferedInputStream(inputStream));
                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    if (ze.toString().contains("/")) {
                        int i = ze.toString().lastIndexOf("/");
                        String tf = ze.toString().substring(0,i);
                        if (!foldersfoundinzip.contains(tf)) {
                            foldersfoundinzip.add(tf);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                message = getActivity().getResources().getString(R.string.backup_error);
            } finally {
                if (zis!=null) {
                    try {
                        zis.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        message = getActivity().getString(R.string.backup_error);
                    }
                }
            }
            return message;
        }

        @Override
        protected void onPostExecute(String s) {

            if (s!=null && getActivity()!=null && !s.equals(getActivity().getResources().getString(R.string.backup_error))) {
                // Sort the folders
                Collator coll = Collator.getInstance(FullscreenActivity.locale);
                coll.setStrength(Collator.SECONDARY);
                Collections.sort(foldersfoundinzip, coll);

                // Add the main folder
                foldersfoundinzip.add(0, FullscreenActivity.mainfoldername);
                waiting.setVisibility(View.GONE);

                zipcontents.setVisibility(View.VISIBLE);
                overwrite.setVisibility(View.VISIBLE);
                saveMe.setVisibility(View.GONE);  // Will be available when a folder is selected
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, foldersfoundinzip);
                folderlist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                folderlist.setAdapter(adapter);
                folderlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        // Check to see if we have selected any folders.  If not, hide the save button
                        boolean oktoproceed = false;
                        for (int y = 0; y < foldersfoundinzip.size(); y++) {
                            if (folderlist.isItemChecked(y)) {
                                oktoproceed = true;
                            }
                        }
                        if (oktoproceed) {
                            saveMe.setVisibility(View.VISIBLE);
                        } else {
                            saveMe.setVisibility(View.GONE);
                        }
                    }
                });
            } else {
                // There was an error, so show the message and hide the progressbar
                waiting.setVisibility(View.GONE);
                FullscreenActivity.myToastMessage = message;
                ShowToast.showToast(getActivity());
            }
        }
    }
    public void doTheImporting() {
        // Send an alert to the screen
        FullscreenActivity.myToastMessage = getActivity().getString(R.string.wait);
        ShowToast.showToast(getActivity());
        // Check the selected folders
        selectednote = "";
        StringBuilder sb = new StringBuilder();

        for (int i=0; i<foldersfoundinzip.size(); i++) {
            if (folderlist.isItemChecked(i)) {
                sb.append("%__").append(foldersfoundinzip.get(i)).append("/__%");
            }
        }

        selectednote = sb.toString();

        // Split
        foldersselectedtoimport = selectednote.split("__%%__");
        for (int i=0; i<foldersselectedtoimport.length; i++) {
            foldersselectedtoimport[i] = foldersselectedtoimport[i].replace("%__","");
            foldersselectedtoimport[i] = foldersselectedtoimport[i].replace("__%","");
        }

        // Now import the stuff via an AsyncTask
        selected_folder_import = new SelectedFolderImport();
        selected_folder_import.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class SelectedFolderImport extends AsyncTask<String, Void, String> {

        boolean canoverwrite;
        @Override
        protected void onPreExecute() {
            canoverwrite = overwrite.isChecked();
        }

        @Override
        protected String doInBackground(String... strings) {

            ZipInputStream zis = null;
            createdfolders = new ArrayList<>();
            createdfolders.clear();
            Uri songsuri = storageAccess.getUriForItem(getActivity(),"Songs","","");
            try {
                Log.d("d","file_uri="+FullscreenActivity.file_uri);
                InputStream inputStream = storageAccess.getInputStream(getActivity(),FullscreenActivity.file_uri);
                zis = new ZipInputStream(new BufferedInputStream(inputStream));
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                while ((ze = zis.getNextEntry()) != null) {
                    // Look to see if ze is in one of the folders we are wanting to import
                    boolean oktoimportthisone = false;
                    for (String aFoldersselectedtoimport : foldersselectedtoimport) {
                        // Is it in the main folder
                        if (aFoldersselectedtoimport.equals(FullscreenActivity.mainfoldername + "/") && !ze.getName().contains("/")) {
                            oktoimportthisone = true;
                            // Or another folder
                        } else if (ze.getName().contains(aFoldersselectedtoimport)) {
                            oktoimportthisone = true;
                        }
                    }

                    Log.d("d","ze.getName()="+ze.getName());

                    if (oktoimportthisone) {
                        if (ze.isDirectory()) {
                            // Make sure the directory exists in the new location
                            storageAccess.createFile(getActivity(),null,"Songs",ze.getName(),"");
                            createdfolders.add(ze.getName());
                        } else {
                            // Copy the file
                            Uri file_uri = storageAccess.getUriForItem(getActivity(),"Songs","",ze.getName());
                            Log.d("d","file_uri="+file_uri);


                            if (!storageAccess.uriExists(getActivity(),file_uri) ||
                                    (storageAccess.uriExists(getActivity(),file_uri) && canoverwrite)) {

                                if (storageAccess.lollipopOrLater()) {
                                    // Check any folder bits exist first
                                    splitZEandCheckExists(ze.getName());
                                }

                                OutputStream outputStream = storageAccess.getOutputStream(getActivity(),file_uri);

                                try {
                                    if (outputStream!=null) {
                                        while ((count = zis.read(buffer)) != -1) {
                                            outputStream.write(buffer, 0, count);
                                        }
                                    } else {
                                        Log.d("d","outputStream=null");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        if (outputStream!=null) {
                                            outputStream.close();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                message = getActivity().getResources().getString(R.string.backup_error);
            } finally {
                if (zis!=null) {
                    try {
                        zis.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        message = getActivity().getString(R.string.backup_error);
                    }
                }
            }
            return message;
        }

        @Override
        protected void onPostExecute(String s) {
            if (mListener != null) {
                mListener.backupInstall(s);
            }
            dismiss();

        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    void splitZEandCheckExists(String name) {
        // Split the name
        String[] n = name.split("/");
        Uri uri = storageAccess.getUriForItem(getActivity(), "Songs", "", "");
        Uri testuri = uri;
        DocumentFile df = storageAccess.documentFileFromUri(getActivity(),uri,uri.getPath());
        if (n.length>1) {
            // Must have sub folders
            for (int x=0;x<n.length-1;x++) {
                testuri = Uri.withAppendedPath(uri,n[x]);
                if (!storageAccess.uriExists(getActivity(),testuri)) {
                        df = df.createDirectory(n[x]);
                        uri = df.getUri();
                    }
                }
            }

        testuri = Uri.withAppendedPath(uri,n[n.length-1]);
        if (!storageAccess.uriExists(getActivity(),testuri)) {
            df.createFile(null,n[n.length-1]);
        }
    }
}
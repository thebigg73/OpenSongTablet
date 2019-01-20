package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class PopUpImportExternalFile extends DialogFragment {

    static PopUpImportExternalFile newInstance() {
        PopUpImportExternalFile frag;
        frag = new PopUpImportExternalFile();
        return frag;
    }

    SetActions setActions;

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    // The views
    TextView fileTitle_TextView, fileType_TextView, title;
    Spinner chooseFolder_Spinner;
    CheckBox overWrite_CheckBox;
    ProgressBar progressBar;
    FloatingActionButton saveMe, closeMe;
    View V;

    // Helper classes
    Bible bibleC;
    StorageAccess storageAccess;
    Preferences preferences;
    SongFolders songFolders;
    ListSongFiles listSongFiles;
    ImportOnSongBackup import_os;
    String what, errormessage = "", filetype, chosenfolder;

    // Folder variables
    ArrayList<String> folderlist;
    ArrayAdapter<String> arrayAdapter;

    // Other variables
    boolean overwrite, ok, error;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        bibleC = new Bible();
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        songFolders = new SongFolders();
        listSongFiles = new ListSongFiles();
        setActions = new SetActions();

        V = inflater.inflate(R.layout.popup_importexternalfile, container, false);

        initialiseViews(V);
        setAction();
        updateTextViews();
        initialiseLocationsToSave();
        PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());

        return V;
    }

    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    public void setTitle(String s) {
        try {
            if (title != null) {
                title.setText(s);
            } else {
                if (getDialog() != null) {
                    getDialog().setTitle(s);
                }
            }
        } catch (Exception e) {
            Log.d("d","Problem with title");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    void setAction() {
        String s = getActivity().getString(R.string.importnewsong);
        String ext;
        if (FullscreenActivity.whattodo.equals("doimportset")) {
            s = getActivity().getString(R.string.importnewset);
            what = "set";
            filetype = getActivity().getString(R.string.options_set);
        } else {
            what = "song";
            filetype = getActivity().getString(R.string.options_song);
        }
        if (FullscreenActivity.file_uri!=null && FullscreenActivity.file_uri.getPath()!=null) {
            ext = FullscreenActivity.file_uri.getPath();
            if (ext != null) {
                ext = ext.toLowerCase();
                if (ext.endsWith(".backup")) {
                    s = getActivity().getString(R.string.import_onsong_choose);
                    what = "onsongbackup";
                    filetype = getActivity().getString(R.string.import_onsong_choose);
                } else if (ext.endsWith(".osts")) {
                    s = getActivity().getString(R.string.importnewset);
                    what = "set";
                    filetype = getActivity().getString(R.string.export_set);
                } else if (ext.endsWith(".onsong")) {
                    what = "onsongfile";
                    filetype = getActivity().getString(R.string.export_onsong);
                } else if (ext.endsWith(".pro") || ext.endsWith(".cho") || ext.endsWith(".chopro") || ext.endsWith("chordpro")) {
                    what = "chordpro";
                    filetype = getActivity().getString(R.string.export_chordpro);
                } else if (ext.endsWith(".usr")) {
                    what = "songselect";
                    filetype = getActivity().getString(R.string.songselect);
                } else if (ext.endsWith(".gif") || ext.endsWith(".jpg") || ext.endsWith(".png")) {
                    what = "image";
                    filetype = getActivity().getString(R.string.image);
                } else if (ext.endsWith(".pdf")) {
                    what = "pdf";
                    filetype = "PDF";
                } else if (ext.endsWith(".txt")) {
                    what = "text";
                    filetype = getActivity().getString(R.string.export_text);
                } else if (bibleC.isYouVersionScripture(FullscreenActivity.incoming_text)) {
                    what = "bible";
                    filetype = getActivity().getString(R.string.scripture);
                } else {
                    // Need to check that this is an OpenSong xml file (may have .ost extension though)
                    if (storageAccess.containsXMLTags(getActivity(), FullscreenActivity.file_uri)) {
                        if (FullscreenActivity.myToastMessage.equals("foundset")) {
                            what = "set";
                            filetype = getActivity().getString(R.string.options_set);
                        } else {
                            what = "song";
                            filetype = getActivity().getString(R.string.options_song);
                        }
                    } else {
                        notValid();
                    }
                }
            }
            try {
                title.setText(s);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            notValid();
        }
    }

    void initialiseViews(View v) {
        fileTitle_TextView = v.findViewById(R.id.fileTitle_TextView);
        fileType_TextView = v.findViewById(R.id.fileType_TextView);
        title = v.findViewById(R.id.dialogtitle);
        chooseFolder_Spinner = v.findViewById(R.id.chooseFolder_Spinner);
        overWrite_CheckBox = v.findViewById(R.id.overWrite_CheckBox);
        progressBar = v.findViewById(R.id.progressBar);
        saveMe = v.findViewById(R.id.saveMe);
        closeMe = v.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe, getActivity());
                saveMe.setEnabled(false);
                overwrite = overWrite_CheckBox.isChecked();
                getChosenFolder();
                setUpSaveAction();
            }
        });
    }

    void notValid() {
        // Not a valid or recognised file, so warn the user and close the popup
        FullscreenActivity.myToastMessage = getActivity().getString(R.string.file_type_unknown);
        if (mListener != null) {
            mListener.showToastMessage(FullscreenActivity.myToastMessage);
        }
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updateTextViews() {
        if ((FullscreenActivity.file_uri != null) && (FullscreenActivity.file_uri.getLastPathSegment() != null)) {
            fileTitle_TextView.setText(FullscreenActivity.file_uri.getLastPathSegment());
        }
        fileType_TextView.setText(filetype);
    }

    void importOnSongBackup() {
        // Hide the cancel button
        if (closeMe != null) {
            closeMe.setVisibility(View.GONE);
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (saveMe != null) {
            saveMe.setClickable(false);
        }

        // Now start the AsyncTask
        import_os = new ImportOnSongBackup();
        try {
            import_os.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d", "Error importing");
        }
    }

    void initialiseLocationsToSave() {
        if (what.equals("set")) {
            folderlist = new ArrayList<>();
            folderlist.add(getActivity().getString(R.string.options_set));
        } else {
            songFolders.prepareSongFolders(getActivity(), storageAccess, preferences);
            folderlist = new ArrayList<>(Arrays.asList(FullscreenActivity.mSongFolderNames));
            if (what.contains("onsong") && !folderlist.contains("OnSong")) {
                folderlist.add(0, "OnSong");
            }
            if (what.contains("onsong")) {
                folderlist.remove(getActivity().getString(R.string.mainfoldername));
            }
        }
        arrayAdapter = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,folderlist);
        chooseFolder_Spinner.setAdapter(arrayAdapter);
    }

    void setUpSaveAction() {
        switch (what) {
            case "onsongbackup":
                importOnSongBackup();
                break;

            case "set":
                importOpenSongSet();
                break;

            case "bible":
                importBibleText();
                break;

            default:
                importFile();
                break;
        }
    }

    void importOpenSongSet() {
        // Get the new file name
        String filename = FullscreenActivity.file_uri.getLastPathSegment();
        if (filename!=null && filename.endsWith(".osts")) {
            filename = filename.replace(".osts","");
        }

        progressBar.setVisibility(View.VISIBLE);

        // Copy the set and to open in the app
        completedImportSet(filename);
    }

    void importBibleText() {
        String translation = FullscreenActivity.scripture_title.substring(FullscreenActivity.scripture_title.lastIndexOf(" "));
        String verses = FullscreenActivity.scripture_title.replace(translation, "");

        // Since the scripture is one big line, split it up a little (50 chars max)
        FullscreenActivity.mScripture = bibleC.shortenTheLines(FullscreenActivity.mScripture,50,8);

        String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<song>" +
                "  <title>"+verses+"</title>\n" +
                "  <author>"+translation+"</author>\n" +
                "  <user1></user1>\n" +
                "  <user2>false</user2>\n" +
                "  <user3></user3>\n" +
                "  <aka></aka>\n" +
                "  <key_line></key_line>\n" +
                "  <hymn_number></hymn_number>\n" +
                "  <lyrics>"+FullscreenActivity.mScripture+"</lyrics>\n" +
                "</song>";

        // Write the file
        Uri scripturefile = storageAccess.getUriForItem(getActivity(), preferences, "Scripture", "", "YouVersion");

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, scripturefile, null,
                "Scripture", "", "YouVersion");

        OutputStream outputStream = storageAccess.getOutputStream(getActivity(),scripturefile);
        storageAccess.writeFileFromString(text,outputStream);
        completedImport("YouVersion","../Scripture");
    }

    void copyFile(String folder, String subfolder, String filename) {
        Uri newfile = storageAccess.getUriForItem(getActivity(), preferences, folder, subfolder, filename);
        if (!storageAccess.uriExists(getActivity(), newfile) || overwrite) {
            InputStream inputStream = storageAccess.getInputStream(getActivity(), FullscreenActivity.file_uri);

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, newfile, null,
                    folder, subfolder, filename);

            OutputStream outputStream = storageAccess.getOutputStream(getActivity(), newfile);
            error = false;
            ok = storageAccess.copyFile(inputStream, outputStream);
            if (!ok) {
                errormessage = errormessage + filename + ": " + getActivity().getString(R.string.backup_error) + "\n";
            }
        } else {
            error = true;
            errormessage = errormessage + filename + ": " + getActivity().getString(R.string.file_exists) + "\n";
        }
    }

    void importFile() {
        // Get the new file name
        String filename = FullscreenActivity.file_uri.getLastPathSegment();
        if (filename!=null && filename.endsWith(".ost")) {
            filename = filename.replace(".ost","");
        }

        // Set the variable to initialise the search index
        FullscreenActivity.needtorefreshsongmenu = true;

        // Copy the file
        copyFile("Songs", chosenfolder, filename);

        // Set up the file ready to open in the app
        completedImport(filename, chosenfolder);
    }

    void getChosenFolder() {
        int i = chooseFolder_Spinner.getSelectedItemPosition();
        chosenfolder = folderlist.get(i);
    }

    void completedImport(String song, String subfolder) {
        FullscreenActivity.songfilename = song;
        FullscreenActivity.whichSongFolder = subfolder;
        storageAccess.listSongs(getActivity(), preferences);
        listSongFiles.getAllSongFiles(getActivity(), preferences, storageAccess);
        if (ok && !error) {
            FullscreenActivity.myToastMessage = getActivity().getString(R.string.success);
        } else {
            FullscreenActivity.myToastMessage = errormessage;
        }
        if (mListener != null) {
            mListener.showToastMessage(FullscreenActivity.myToastMessage);
            mListener.rebuildSearchIndex();
        }
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void completedImportSet(final String set) {
        ImportSet importSet = new ImportSet(set);
        importSet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface MyInterface {
        void refreshAll();

        void rebuildSearchIndex();
        void onSongImportDone(String message);
        void openFragment();
        void showToastMessage(String message);
    }

    @SuppressLint("StaticFieldLeak")
    private class ImportSet extends AsyncTask<String, Void, String> {

        String setname;

        ImportSet(String set) {
            setname = set;
        }

        @Override
        protected void onPreExecute() {
            FullscreenActivity.settoload = setname;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                // Copy the file
                copyFile("Sets", "", setname);

                if (!error) {
                    setActions.loadASet(getActivity(), preferences, storageAccess);
                    FullscreenActivity.setView = true;

                    // Get the set first item
                    setActions.prepareFirstItem(getActivity(), preferences, listSongFiles, storageAccess);

                    // Save the new set to the preferences
                    Preferences.savePreferences();

                    FullscreenActivity.myToastMessage = getActivity().getString(R.string.success);

                    FullscreenActivity.abort = false;

                } else {

                    FullscreenActivity.myToastMessage = errormessage;
                }

            } catch (Exception e) {
                FullscreenActivity.myToastMessage = getActivity().getString(R.string.error);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                mListener.showToastMessage(FullscreenActivity.myToastMessage);
                if (!error) {
                    mListener.refreshAll();
                    FullscreenActivity.whattodo = "editset";
                    mListener.openFragment();
                }
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ImportOnSongBackup extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return storageAccess.extractOnSongZipFile(getActivity(), preferences, FullscreenActivity.file_uri);
        }

        @Override
        protected void onPostExecute(String doneit) {
            try {
                if (mListener != null) {
                    mListener.onSongImportDone(doneit);
                }
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (import_os!=null) {
            import_os.cancel(true);
        }
        this.dismiss();
    }

}
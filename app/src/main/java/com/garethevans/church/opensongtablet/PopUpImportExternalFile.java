package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PopUpImportExternalFile extends DialogFragment {

    static PopUpImportExternalFile newInstance() {
        PopUpImportExternalFile frag;
        frag = new PopUpImportExternalFile();
        return frag;
    }

    private SetActions setActions;

    private MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    // The views
    private TextView fileTitle_TextView, fileType_TextView, title, progressText;
    private Spinner chooseFolder_Spinner;
    private CheckBox overWrite_CheckBox;
    private ProgressBar progressBarH;
    private LinearLayout progressLinearLayout;
    private FloatingActionButton saveMe;
    private FloatingActionButton closeMe;

    // Helper classes
    private Bible bibleC;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private SongFolders songFolders;
    private OnSongConvert onSongConvert;
    private ChordProConvert chordProConvert;
    private SongXML songXML;
    private SQLiteHelper sqLiteHelper;

    private String what;
    private String errormessage = "";
    private String filetype;
    private String chosenfolder;

    // Folder variables
    private ArrayList<String> folderlist;

    // Other variables
    private boolean overwrite, ok, error;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bibleC = new Bible();
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        songFolders = new SongFolders();
        setActions = new SetActions();
        onSongConvert = new OnSongConvert();
        chordProConvert = new ChordProConvert();
        songXML = new SongXML();
        sqLiteHelper = new SQLiteHelper(getContext());

        View v = inflater.inflate(R.layout.popup_importexternalfile, container, false);
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(false);
        }

        initialiseViews(v);

        setAction();

        updateTextViews();

        initialiseLocationsToSave();

        PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog(), preferences);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    private void setAction() {
        String s = getString(R.string.importnewsong);
        String ext;

        // Defaults
        overWrite_CheckBox.setChecked(false);
        overWrite_CheckBox.setVisibility(View.VISIBLE);
        overWrite_CheckBox.setEnabled(true);

        if (FullscreenActivity.whattodo.equals("doimportset")) {
            s = getString(R.string.importnewset);
            what = "set";
            filetype = getString(R.string.set);
        } else {
            what = "song";
            filetype = getString(R.string.song);
        }
        if (FullscreenActivity.file_uri!=null && FullscreenActivity.file_uri.getPath()!=null) {
            ext = storageAccess.getActualFilename(requireContext(),FullscreenActivity.file_uri.toString());
            if (ext != null) {
                ext = ext.toLowerCase(Locale.ROOT);
                Log.d("PopUpImport","ext="+ext);

                if (ext.endsWith(".backup")) {
                    s = getString(R.string.import_onsong_choose);
                    what = "onsongbackup";
                    filetype = getString(R.string.import_onsong_choose);
                    overWrite_CheckBox.setChecked(false);
                    overWrite_CheckBox.setEnabled(false);
                } else if (ext.endsWith(".osts")) {
                    s = getString(R.string.importnewset);
                    what = "set";
                    filetype = getString(R.string.export_set);
                } else if ((ext.endsWith(".onsong") || ext.endsWith(".xml")) && FullscreenActivity.whattodo.equals("doimportset")) {
                    what = "onsongset";
                    filetype = "OnSong";
                    overWrite_CheckBox.setChecked(false);
                    overWrite_CheckBox.setEnabled(true);
                } else if (ext.endsWith(".onsong")) {
                    what = "onsongfile";
                    filetype = getString(R.string.export_onsong);
                    overWrite_CheckBox.setChecked(false);
                    overWrite_CheckBox.setEnabled(false);
                } else if (ext.endsWith(".pro") || ext.endsWith(".cho") || ext.endsWith(".chopro") || ext.endsWith("chordpro")) {
                    what = "chordpro";
                    filetype = getString(R.string.export_chordpro);
                    overWrite_CheckBox.setChecked(false);
                    overWrite_CheckBox.setEnabled(false);
                } else if (ext.endsWith(".usr")) {
                    what = "songselect";
                    filetype = getString(R.string.songselect);
                    overWrite_CheckBox.setChecked(false);
                    overWrite_CheckBox.setEnabled(false);
                } else if (ext.endsWith(".gif") || ext.endsWith(".jpg") || ext.endsWith(".png")) {
                    what = "image";
                    filetype = getString(R.string.image);
                } else if (ext.endsWith(".pdf")) {
                    what = "pdf";
                    filetype = "PDF";
                } else if (ext.endsWith(".txt")) {
                    what = "text";
                    filetype = getString(R.string.export_text);
                } else if (bibleC.isYouVersionScripture(FullscreenActivity.incoming_text)) {
                    what = "bible";
                    filetype = getString(R.string.scripture);
                } else {
                    // Need to check that this is an OpenSong xml file (may have .ost extension though)
                    if (storageAccess.containsXMLTags(getContext(), FullscreenActivity.file_uri)) {
                        if (StaticVariables.myToastMessage.equals("foundset")) {
                            what = "set";
                            filetype = getString(R.string.set);
                        } else {
                            what = "song";
                            filetype = getString(R.string.song);
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

    private void initialiseViews(View v) {
        fileTitle_TextView = v.findViewById(R.id.fileTitle_TextView);
        fileType_TextView = v.findViewById(R.id.fileType_TextView);
        title = v.findViewById(R.id.dialogtitle);
        chooseFolder_Spinner = v.findViewById(R.id.chooseFolder_Spinner);
        overWrite_CheckBox = v.findViewById(R.id.overWrite_CheckBox);
        progressBarH = v.findViewById(R.id.progressBarH);
        progressText = v.findViewById(R.id.progressText);
        progressLinearLayout = v.findViewById(R.id.progressLinearLayout);
        saveMe = v.findViewById(R.id.saveMe);
        closeMe = v.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getContext());
            closeMe.setEnabled(false);
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe, getContext());
            saveMe.setEnabled(false);
            overwrite = overWrite_CheckBox.isChecked();
            getChosenFolder();
            setUpSaveAction();
        });
    }

    private void notValid() {
        // Not a valid or recognised file, so warn the user and close the popup
        StaticVariables.myToastMessage = getString(R.string.file_type_unknown);
        if (mListener != null) {
            mListener.showToastMessage(StaticVariables.myToastMessage);
        }
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTextViews() {
        if ((FullscreenActivity.file_uri != null) && (FullscreenActivity.file_uri.getLastPathSegment() != null)) {
            fileTitle_TextView.setText(improveFileName(FullscreenActivity.file_uri));
        }
        fileType_TextView.setText(filetype);
    }

    private void importOnSongBackup() {
        // Hide the cancel button
        if (closeMe != null) {
            closeMe.hide();
        }

        if (progressLinearLayout != null) {
            progressLinearLayout.setVisibility(View.VISIBLE);
        }
        if (saveMe != null) {
            saveMe.setClickable(false);
        }

        // Now start the AsyncTask
        ImportOnSongBackup import_os = new ImportOnSongBackup(FullscreenActivity.file_uri);
        try {
            import_os.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("d", "Error importing");
        }
    }

    private void initialiseLocationsToSave() {
        if (what.equals("set") || what.equals("onsongset")) {
            folderlist = new ArrayList<>();
            folderlist.add(getString(R.string.set));
        } else if (what.contains("onsong")) {
            folderlist = new ArrayList<>();
            folderlist = songFolders.prepareSongFolders(getContext(),preferences);
            folderlist.add(0, "OnSong");
        } else {
            folderlist = songFolders.prepareSongFolders(getContext(),preferences);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireActivity(), R.layout.my_spinner, folderlist);
        chooseFolder_Spinner.setAdapter(arrayAdapter);
    }

    private void setUpSaveAction() {
        switch (what) {
            case "onsongbackup":
                importOnSongBackup();
                break;

            case "set":
                importOpenSongSet();
                break;

            case "onsongset":
                convertOnSongSet();
                break;

            case "bible":
                importBibleText();
                break;

            default:
                importFile();
                break;
        }
    }

    private void convertOnSongSet() {
        String ext = FullscreenActivity.file_uri.getPath();
        // Check that the OnSong folder exists.  If not, create it
        Uri onSong = storageAccess.getUriForItem(getContext(),preferences,"Songs","OnSong",null);
        storageAccess.lollipopCreateFileForOutputStream(getContext(),preferences,onSong, DocumentsContract.Document.MIME_TYPE_DIR,"Songs","OnSong",null);

        if (ext!=null) {
            // Read in the file as text ready for parsing
            InputStream inputStream = storageAccess.getInputStream(getContext(),FullscreenActivity.file_uri);
            String text = storageAccess.readTextFileToString(inputStream);
            String[] setsongs;
            StringBuilder openSongSetText = new StringBuilder();

            if (ext.endsWith(".onsong")) {
                // Songs are split with ---
                setsongs = text.split("---");
                Log.d("d","text:"+text);

                Log.d("ImportExternal","setsongs.length"+setsongs.length);
                // Go through each song and get the contents and write to the file in the OnSong folder
                for (String song:setsongs) {
                    song = song.trim();
                    if (!song.startsWith("Title:")) {
                        song = "Title:"+song;
                    }
                    // Get the song title
                    String title="unknown";
                    if (song.startsWith("Title:")||song.startsWith("Title :")) {
                        int startpos = song.indexOf("Title");
                        startpos = song.indexOf(":"+1,startpos);
                        int endpos = song.indexOf("\n",startpos);
                        if (startpos>-1 && endpos>-1 && endpos>startpos) {
                            title = song.substring(startpos, endpos);
                            Log.d("d", "title=" + title);
                        }
                    }

                    // Save the onsong formatted song
                    Uri uri = saveTheSong(title+".onsong",song);
                    Log.d("d","uri of saved song="+uri);
                    // Convert the song to OpenSong format
                    ArrayList<String> songtags = onSongConvert.convertTextToTags(getContext(),storageAccess,preferences,songXML,chordProConvert,uri,song);

                    // Build openSongSetText
                    openSongSetText.append("     <slide_group name=\"").append(storageAccess.safeFilename(songtags.get(0))).append("\" type=\"song\" presentation=\"\" path=\"OnSong/\"/>\n");
                }

            } else if (ext.endsWith("xml")) {
                // Songs are split by <song>
                text = text.substring(text.indexOf("<set>"));
                text = text.replace("<set>","");
                text = text.replace("</set>","");

                setsongs = text.split("<song>");

                // Go through each one
                for (String song:setsongs) {
                    song = song.trim();
                    if (!song.isEmpty()) {
                        String title = "unknown";
                        if (song.contains("<title>") && song.contains("</title>") && song.indexOf("<title>") < song.indexOf("</title>")) {
                            title = song.substring(song.indexOf("<title>") + 7, song.indexOf("</title>"));
                        }
                        song = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<song>\n  " + song;

                        // Save the song
                        saveTheSong(title, song);

                        // Build openSongSetText
                        openSongSetText.append("     <slide_group name=\"").append(storageAccess.safeFilename(title)).append("\" type=\"song\" presentation=\"\" path=\"OnSong/\"/>\n");
                    }
                }
            }

            // Now build the OpenSongSet file, load it and then parse it
            String currSet = openSongSetText.toString();
            if (!currSet.isEmpty()) {
                // Save the set
                convertOnSongSetToOpenSongSet(currSet);
            }
        }
    }

    private Uri saveTheSong(String title, String contents) {
        // Check the OnSong folder exixts
        Uri uri = storageAccess.getUriForItem(getContext(),preferences,"Songs","OnSong",storageAccess.safeFilename(title));
        // If the song doesn't exist, create it
        boolean exists = storageAccess.uriExists(getContext(),uri);
        if (!exists | overWrite_CheckBox.isChecked()) {
            storageAccess.lollipopCreateFileForOutputStream(getContext(),preferences,uri,null,"Songs","OnSong",storageAccess.safeFilename(title));
            OutputStream outputStream = storageAccess.getOutputStream(getContext(), uri);
            storageAccess.writeFileFromString(contents,outputStream);
        }
        return uri;
    }

    private void convertOnSongSetToOpenSongSet(String currSet) {
        // Add the start and end bits
        String setname = fileTitle_TextView.getText().toString();
        setname = setname.replace(".onsong","");
        setname = setname.replace(".xml","");
        setname = storageAccess.safeFilename(setname);
        currSet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<set name=\""+
                setname+"\">\n  <slide_groups>\n" + currSet + "  </slide_groups>\n</set>";
        // Create the file
        Uri setUri = storageAccess.getUriForItem(getContext(),preferences,"Sets","",setname);
        if (overWrite_CheckBox.isChecked()||!storageAccess.uriExists(getContext(),setUri)) {
            storageAccess.lollipopCreateFileForOutputStream(getContext(),preferences,setUri,null,"Sets","",setname);
            OutputStream outputStreamSet = storageAccess.getOutputStream(getContext(),setUri);
            storageAccess.writeFileFromString(currSet,outputStreamSet);
        }

        // close the popupwindow and get tha app to load the set.
        preferences.setMyPreferenceString(getContext(),"setCurrent","");
        StaticVariables.mSetList = null;
        StaticVariables.setView = false;
        preferences.setMyPreferenceString(getContext(),"setCurrentLastName","");
        LoadASet loadASet = new LoadASet(setname);
        loadASet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void importOpenSongSet() {
        // Get the new file name
        String filename = improveFileName(FullscreenActivity.file_uri);
        if (filename.endsWith(".osts")) {
            filename = filename.replace(".osts","");
        }

        progressLinearLayout.setVisibility(View.VISIBLE);

        // Copy the set and to open in the app
        completedImportSet(filename);
    }

    private void importBibleText() {
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
        Uri scripturefile = storageAccess.getUriForItem(getContext(), preferences, "Scripture", "", "YouVersion");

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(getContext(), preferences, scripturefile, null,
                "Scripture", "", "YouVersion");

        OutputStream outputStream = storageAccess.getOutputStream(getContext(),scripturefile);
        storageAccess.writeFileFromString(text,outputStream);
        completedImport("YouVersion","../Scripture");
    }

    private void copyFile(String folder, String subfolder, String filename) {
        filename = storageAccess.safeFilename(filename);
        Uri newfile = storageAccess.getUriForItem(getContext(), preferences, folder, subfolder, filename);
        if (!storageAccess.uriExists(getContext(), newfile) || overwrite) {

            InputStream inputStream = storageAccess.getInputStream(getContext(), FullscreenActivity.file_uri);

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(getContext(), preferences, newfile, null,
                    folder, subfolder, filename);

            OutputStream outputStream = storageAccess.getOutputStream(getContext(), newfile);

            error = false;
            ok = storageAccess.copyFile(inputStream, outputStream);

            if (!ok) {
                errormessage = errormessage + filename + ": " + getString(R.string.backup_error) + "\n";
            }
        } else {
            error = true;
            errormessage = errormessage + filename + ": " + getString(R.string.file_exists) + "\n";
        }
    }

    private void importFile() {
        // Get the new file name
        String filename = improveFileName(FullscreenActivity.file_uri);
        if (filename.endsWith(".ost")) {
            filename = filename.replace(".ost","");
        }


        // Set the variable to initialise the search index
        FullscreenActivity.needtorefreshsongmenu = true;

        // Copy the file
        copyFile("Songs", chosenfolder, filename);

        // Set up the file ready to open in the app
        completedImport(filename, chosenfolder);
    }

    private void getChosenFolder() {
        int i = chooseFolder_Spinner.getSelectedItemPosition();
        chosenfolder = folderlist.get(i);
    }

    private void completedImport(String song, String subfolder) {
        StaticVariables.songfilename = song;
        StaticVariables.whichSongFolder = subfolder;

        // Add the songs to the SQLite database if it isn't a custom slide/scripture
        if (!subfolder.startsWith("../")) {
            SQLiteHelper sqLiteHelper = new SQLiteHelper(getContext());
            sqLiteHelper.createSong(getContext(), subfolder, song);
        }

        if (ok && !error) {
            StaticVariables.myToastMessage = getString(R.string.success);
        } else {
            StaticVariables.myToastMessage = errormessage;
        }
        if (mListener != null) {
            try {
                mListener.showToastMessage(StaticVariables.myToastMessage);
                mListener.refreshAll();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void completedImportSet(final String set) {
        ImportSet importSet = new ImportSet(set);
        importSet.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface MyInterface {
        void refreshAll();
        void onSongImportDone();
        void openFragment();
        void showToastMessage(String message);
    }

    @SuppressLint("StaticFieldLeak")
    private class ImportSet extends AsyncTask<String, Void, String> {

        final String setname;

        ImportSet(String set) {
            setname = set;
        }

        @Override
        protected void onPreExecute() {
            StaticVariables.settoload = setname;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                // Copy the file
                copyFile("Sets", "", setname);

                if (!error) {
                    setActions.loadASet(getContext(), preferences, storageAccess);
                    StaticVariables.setView = true;

                    // Prepare the set
                    setActions.prepareSetList(getContext(), preferences);
                    setActions.indexSongInSet();

                    // IV - For presentation mode - Get the set first item
                    if (StaticVariables.whichMode.equals("Presentation")) {
                        setActions.prepareFirstItem(getContext(), preferences);
                    }

                    StaticVariables.myToastMessage = getString(R.string.success);

                } else {

                    StaticVariables.myToastMessage = errormessage;
                }

            } catch (Exception e) {
                StaticVariables.myToastMessage = getString(R.string.error);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                mListener.showToastMessage(StaticVariables.myToastMessage);
                if (!error) {
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
    private class LoadASet extends AsyncTask<String, Void, String> {

        final String setname;

        LoadASet(String set) {
            setname = set;
        }

        @Override
        protected void onPreExecute() {
            StaticVariables.settoload = setname;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                setActions.loadASet(getContext(), preferences, storageAccess);
                StaticVariables.setView = true;

                // Prepare the set
                setActions.prepareSetList(getContext(), preferences);
                setActions.indexSongInSet();

                // IV - For presentation mode -  Get the set first item
                if (StaticVariables.whichMode.equals("Presentation")) {
                    setActions.prepareFirstItem(getContext(), preferences);
                }

                StaticVariables.myToastMessage = getString(R.string.success);

            }catch (Exception e) {
                StaticVariables.myToastMessage = getString(R.string.error);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                mListener.showToastMessage(StaticVariables.myToastMessage);
                if (!error) {
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
    private class ImportOnSongBackup extends AsyncTask<Object, String, String> {
        String message;
        InputStream is;
        ZipInputStream zis;
        String filename;
        final Uri zipUri;
        int numitems;
        int curritem;

        ImportOnSongBackup(Uri zU) {
            zipUri = zU;
        }

        @Override
        protected void onPreExecute() {
            progressText.setVisibility(View.VISIBLE);
            progressBarH.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(String... string) {
            try {
                progressText.setText(string[0]);
                progressBarH.setMax(numitems);
                progressBarH.setProgress(curritem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            File onsongdbfile = new File(requireActivity().getExternalFilesDir("OnSong"), "OnSong.Backup.sqlite3");
            try {
                if (!onsongdbfile.createNewFile()) {
                    Log.d("PopUpImportExternal","Error creating file");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Uri chosenfolderuri = storageAccess.getUriForItem(getContext(), preferences, "Songs", chosenfolder, "");

            // Create the chosen folder (if it doesn't already exist)
            if (chosenfolder.equals("OnSong")) {
                storageAccess.lollipopCreateFileForOutputStream(getContext(), preferences, chosenfolderuri,
                        DocumentsContract.Document.MIME_TYPE_DIR, "Songs", chosenfolder, "");
            }

            try {
                is = storageAccess.getInputStream(getContext(), zipUri);
                zis = new ZipInputStream(new BufferedInputStream(is));

                ZipEntry ze;
                while ((ze = zis.getNextEntry()) != null) {
                    final byte[] buffer = new byte[2048];
                    int count;
                    filename = ze.getName();
                    filename = storageAccess.safeFilename(filename);
                    if (!filename.startsWith("Media")) {
                        // The Media folder throws errors (it has zero length files sometimes
                        // It also contains stuff that is irrelevant for OpenSongApp importing
                        // Only process stuff that isn't in that folder!
                        // It will also ignore any song starting with 'Media' - not worth a check for now!

                        OutputStream out;
                        if (filename.equals("OnSong.Backup.sqlite3") || filename.equals("OnSong.sqlite3")) {
                            onsongdbfile = new File(requireContext().getExternalFilesDir("OnSong"), "OnSong.Backup.sqlite3");
                            Uri outuri = Uri.fromFile(onsongdbfile);
                            if (!onsongdbfile.mkdirs()) {
                                Log.d("PopUpImport", "Database file already exists - ok");
                            }
                            out = storageAccess.getOutputStream(getContext(), outuri);

                        } else if (!filename.endsWith(".doc") && !filename.endsWith(".docx") && !filename.endsWith(".sqlite3") &&
                                !filename.endsWith(".preferences")) {
                            Uri outuri = storageAccess.getUriForItem(getContext(), preferences, "Songs", chosenfolder, filename);
                            storageAccess.lollipopCreateFileForOutputStream(getContext(), preferences,
                                    outuri, null, "Songs", chosenfolder, filename);
                            out = storageAccess.getOutputStream(getContext(), outuri);
                            publishProgress(filename);

                        } else {
                            // Don't copy this
                            out = null;
                        }

                        if (out != null) {
                            final BufferedOutputStream bout = new BufferedOutputStream(out);
                            try {
                                while ((count = zis.read(buffer)) != -1) {
                                    bout.write(buffer, 0, count);
                                }
                                bout.flush();
                            } catch (Exception e) {
                                message = getString(R.string.file_type_unknown);
                                e.printStackTrace();
                            } finally {
                                try {
                                    bout.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                   zis.closeEntry();
                }
                zis.close();

            } catch (Exception e) {
                e.printStackTrace();
                message = getString(R.string.import_onsong_error);
                publishProgress(message);
                return message;
            }

            SQLiteDatabase onsongdb = SQLiteDatabase.openOrCreateDatabase(onsongdbfile, null);

            // Go through each row and read in the content field
            // Save the files with the .onsong extension

            String query = "SELECT title,content FROM Song";

            //Cursor points to a location in your results
            Cursor cursor;
            String str_title;
            String str_content;

            try {
                cursor = onsongdb.rawQuery(query, null);

                numitems = cursor.getCount();
                curritem = 0;

                // Move to first row
                cursor.moveToFirst();

                while (cursor.moveToNext()) {
                    curritem++;

                    // Extract data
                    str_title = cursor.getString(cursor.getColumnIndex("title"));
                    // Make sure title doesn't have /
                    str_title = str_title.replace("/", "_");
                    str_title = TextUtils.htmlEncode(str_title);
                    str_content = cursor.getString(cursor.getColumnIndex("content"));

                    try {
                        message = curritem + "/" + numitems + "  " + getString(R.string.extracting) +
                                ": " + str_title + ".onsong";
                        publishProgress(message);

                        // Prepare the OnSong file
                        Uri oldsong = storageAccess.getUriForItem(getContext(), preferences, "Songs",
                                chosenfolder, str_title + ".onsong");
                        storageAccess.lollipopCreateFileForOutputStream(getContext(), preferences,
                                oldsong, null, "Songs", chosenfolder, str_title + ".onsong");

                        // Now write the modified song
                        message = curritem + "/" + numitems + "  " + getString(R.string.converting) +
                                ": " + str_title + ".onsong";
                        publishProgress(message);
                        ArrayList<String> returnedstuff = onSongConvert.convertTextToTags(getContext(), storageAccess, preferences,
                                songXML, chordProConvert, oldsong, str_content);

                        // Write the song to the database
                        sqLiteHelper.createImportedSong(getContext(), chosenfolder, returnedstuff.get(0),
                                returnedstuff.get(1), returnedstuff.get(2), returnedstuff.get(3),
                                returnedstuff.get(4), returnedstuff.get(5), returnedstuff.get(6),
                                returnedstuff.get(7));

                    } catch (Exception e) {
                        e.printStackTrace();
                        message = str_title + ".onsong --> " + requireContext().getString(R.string.error);
                        publishProgress(message);
                    }
                }
                cursor.close();
                onsongdb.close();
            } catch (Exception e) {
                // Error with sql database
                e.printStackTrace();
                return "Error";
            }

            return getString(R.string.success);
        }

        @Override
        protected void onPostExecute(String s) {
            // This bit will take a while, so will be called in an async task
            try {
                ShowToast.showToast(getContext());
                if (mListener != null) {
                    mListener.onSongImportDone();
                }
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private String improveFileName(Uri fileUri) {
        String betterFilename = "";
        if (fileUri!=null && fileUri.getLastPathSegment()!=null) {
            betterFilename = storageAccess.getActualFilename(requireContext(),fileUri.toString());
        }
        // IV - Adjusted to handle files at root of drive
        betterFilename = betterFilename.replace(":","/");

        if (betterFilename.contains("/")) {
            betterFilename = betterFilename.substring(betterFilename.lastIndexOf("/"));
            betterFilename = betterFilename.replace("/","");
        }
        return betterFilename;
    }
}
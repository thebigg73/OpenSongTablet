package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class PopUpSongCreateFragment extends DialogFragment {
    // This is a quick popup to enter a new song folder name, or rename a current one
    // Once it has been completed positively (i.e. ok was clicked) it sends a refreshAll() interface call

    private ArrayList<String> foldernames;
    private Spinner newFolderSpinner;
    private EditText newSongNameEditText;
    private ProgressBar progressBar;
    private MyInterface mListener;
    private AsyncTask<Object, Void, String> getfolders;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private SongFolders songFolders;
    private SongXML songXML;

    static PopUpSongCreateFragment newInstance() {
        PopUpSongCreateFragment frag;
        frag = new PopUpSongCreateFragment();
        return frag;
    }

    public interface MyInterface {
        void loadSong();
        void prepareSongMenu();
    }

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

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View V = inflater.inflate(R.layout.popup_songcreate, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getResources().getString(R.string.createanewsong));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                //saveMe.setEnabled(false);
                doSave();
            }
        });

        storageAccess = new StorageAccess();
        preferences = new Preferences();
        songFolders = new SongFolders();
        songXML = new SongXML();

        // Initialise the views
        newFolderSpinner = V.findViewById(R.id.newFolderSpinner);
        newSongNameEditText = V.findViewById(R.id.newSongNameEditText);
        progressBar = V.findViewById(R.id.progressBar);

        if (FullscreenActivity.whattodo.equals("savecameraimage")) {
            try {
                Log.d("PopUpSongCreate", "mCurrentPhotoPath=" + FullscreenActivity.mCurrentPhotoPath);
                String currimagename = FullscreenActivity.mCurrentPhotoPath.
                        substring(FullscreenActivity.mCurrentPhotoPath.lastIndexOf("/") + 1);
                    newSongNameEditText.setText(currimagename);
            } catch (Exception e) {
                newSongNameEditText.setText(FullscreenActivity.imagetext);
            }
        } else {
            newSongNameEditText.setText("");
        }

        // Set up the folderspinner
        // Populate the list view with the current song folders
        // Reset to the main songs folder, so we can list them
        FullscreenActivity.currentFolder = StaticVariables.whichSongFolder;
        FullscreenActivity.newFolder = StaticVariables.whichSongFolder;
        getfolders = new GetFolders();
        try {
            getfolders.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Probably closed popup before folders listed\n"+e);
        }

        // Set the newFolderSpinnerListener
        newFolderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    FullscreenActivity.newFolder = foldernames.get(position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);
        return V;

    }

    private void doSave() {
        // Show the progressBar (to indicate something is happening)
        progressBar.setVisibility(View.VISIBLE);

        // Try to do this as an AsyncTask!
        DoSave doSave = new DoSave();
        doSave.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @SuppressLint("StaticFieldLeak")
    private class DoSave extends AsyncTask<Object, String, String> {

        String tempNewSong;

        @Override
        protected void onPreExecute() {
            // Prepare the app to rebuild the search index after loading the song
            FullscreenActivity.needtorefreshsongmenu = true;
            tempNewSong = newSongNameEditText.getText().toString().trim();
            StaticVariables.myToastMessage = "";
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Get the variables
            Uri to = storageAccess.getUriForItem(getActivity(), preferences, "Songs", FullscreenActivity.newFolder, tempNewSong);
            Log.d("PopUpCreate", "to=" + to);

            // Decide if this song already exists.  If so, alert the user and do nothing more
            StaticVariables.myToastMessage = "";
            if (storageAccess.uriExists(getActivity(), to)) {
                StaticVariables.myToastMessage = getString(R.string.songnamealreadytaken);
            } else {

                if (FullscreenActivity.whattodo.equals("savecameraimage")) {
                    Uri from = Uri.parse(FullscreenActivity.mCurrentPhotoPath);
                    Log.d("PopUpCreate", "from=" + from);
                    String currimagename = from.getLastPathSegment();
                    Log.d("PopUpCreate", "currimagename=" + currimagename);

                    // If no name is specified, use the original ugly one
                    if (tempNewSong == null || tempNewSong.isEmpty()) {
                        tempNewSong = currimagename;
                    }

                    // Check the camera image ends with .jpg.  If not, add it!
                    if (tempNewSong != null && !tempNewSong.endsWith(".jpg")) {
                        tempNewSong = tempNewSong + ".jpg";
                    }

                    Log.d("PopUpCreate", "tempNewSong=" + tempNewSong);

                    InputStream inputStream = storageAccess.getInputStream(getActivity(), from);

                    Log.d("PopUpCreate", "inputStream=" + inputStream);

                    // Check the uri exists for the outputstream to be valid
                    storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, to, null,
                            "Songs", FullscreenActivity.newFolder, tempNewSong);

                    OutputStream outputStream = storageAccess.getOutputStream(getActivity(), to);

                    Log.d("PopUpCreate", "oututStream=" + outputStream);

                    // Copy the image file and then remove the original (don't need to keep it in the media folder)
                    Log.d("PopUpCreate", "copying file");
                    storageAccess.copyFile(inputStream, outputStream);
                    Log.d("PopUpCreate", "deleting original");
                    storageAccess.deleteFile(getActivity(), from);

                    // Add the new song to the SQLite database
                    SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity());
                    sqLiteHelper.createSong(getActivity(),FullscreenActivity.newFolder,tempNewSong);

                    try {
                        if (mListener != null) {
                            mListener.prepareSongMenu();
                            Log.d("PopUpCreate", "setting songfilename=" + tempNewSong);
                            StaticVariables.songfilename = tempNewSong;
                            Log.d("PopUpCreate", "setting whichSongFolder=" + FullscreenActivity.newFolder);
                            StaticVariables.whichSongFolder = FullscreenActivity.newFolder;
                        }

                    } catch (Exception e) {
                        StaticVariables.myToastMessage = getResources().getString(R.string.error);
                        e.printStackTrace();
                    }

                } else {
                    if (!tempNewSong.equals("") && !tempNewSong.isEmpty()
                            && !tempNewSong.contains("/") && !storageAccess.uriExists(getActivity(), to)
                            && !tempNewSong.equals(getString(R.string.mainfoldername))) {

                        StaticVariables.whichSongFolder = FullscreenActivity.newFolder;

                        // Try to create
                        if (tempNewSong.endsWith(".pdf") || tempNewSong.endsWith(".PDF")) {
                            // Naughty, naughty, it shouldn't be a pdf extension
                            tempNewSong = tempNewSong.replace(".pdf", "");
                            tempNewSong = tempNewSong.replace(".PDF", "");
                        }

                        songXML.initialiseSongTags();

                        // Prepare the XML
                        StaticVariables.songfilename = tempNewSong;
                        StaticVariables.mTitle = tempNewSong;

                        FullscreenActivity.mynewXML = songXML.prepareBlankSongXML();

                        // If this is an import from text intent, add the text to the lyrics
                        if (FullscreenActivity.scripture_title != null &&
                                FullscreenActivity.scripture_title.equals("importedtext_in_scripture_verse") &&
                                FullscreenActivity.scripture_verse != null && !FullscreenActivity.scripture_verse.equals("")) {
                            StaticVariables.mLyrics = FullscreenActivity.scripture_verse;
                            FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("<lyrics>[V]\n</lyrics>",
                                    "<lyrics>[V]\n" + FullscreenActivity.scripture_verse + "</lyrics>");
                        }

                        // Save the file
                        storageAccess.createFile(getActivity(), preferences, null, "Songs", StaticVariables.whichSongFolder, StaticVariables.songfilename);
                        Uri uri = storageAccess.getUriForItem(getActivity(), preferences, "Songs", StaticVariables.whichSongFolder,
                                StaticVariables.songfilename);

                        // Check the uri exists for the outputstream to be valid
                        storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, uri, null,
                                "Songs", StaticVariables.whichSongFolder, StaticVariables.songfilename);

                        OutputStream outputStream = storageAccess.getOutputStream(getActivity(), uri);
                        storageAccess.writeFileFromString(FullscreenActivity.mynewXML, outputStream);

                        // If we are autologging CCLI information
                        if (preferences.getMyPreferenceBoolean(getActivity(),"ccliAutomaticLogging",false)) {
                            PopUpCCLIFragment.addUsageEntryToLog(getActivity(), preferences, StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename,
                                    StaticVariables.songfilename, "",
                                    "", "", "1"); // Created
                        }

                        // Add the new song to the SQLite database
                        SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity());
                        sqLiteHelper.createSong(getActivity(),StaticVariables.whichSongFolder,StaticVariables.songfilename);
                    } else {
                        StaticVariables.myToastMessage = getResources().getString(R.string.notset);
                    }
                }
            }

            return null;
        }

        protected void onPostExecute(String s) {
            try {
                if (!StaticVariables.myToastMessage.equals(getString(R.string.error)) &&
                        !StaticVariables.myToastMessage.equals(getString(R.string.notset)) &&
                        !StaticVariables.myToastMessage.equals(getString(R.string.songnamealreadytaken))) {

                    if (mListener != null) {
                        mListener.prepareSongMenu();
                        if (FullscreenActivity.whattodo.equals("savecameraimage")) {
                            mListener.loadSong();
                        } else {
                            // Prepare the app to open the edit page after loading
                            FullscreenActivity.needtorefreshsongmenu = false;  // This will happen after editing
                            FullscreenActivity.needtoeditsong = true;
                            mListener.loadSong();
                        }
                    }
                }

                if (!StaticVariables.myToastMessage.equals("")) {
                    ShowToast.showToast(getActivity());
                }
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class GetFolders extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            foldernames = songFolders.prepareSongFolders(getActivity(),preferences);
            return null;
        }

        protected void onPostExecute(String s) {

            ArrayAdapter<String> folders = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.my_spinner, foldernames);
            folders.setDropDownViewResource(R.layout.my_spinner);
            newFolderSpinner.setAdapter(folders);

            // Select the current folder as the preferred one - i.e. rename into the same folder
            newFolderSpinner.setSelection(0);
            for (int w = 0; w < foldernames.size(); w++) {
                if (FullscreenActivity.currentFolder.equals(foldernames.get(w)) ||
                        FullscreenActivity.currentFolder.equals("(" + foldernames.get(w) + ")")) {
                    newFolderSpinner.setSelection(w);
                    FullscreenActivity.newFolder = foldernames.get(w);
                }
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (getfolders!=null) {
            getfolders.cancel(true);
        }
        this.dismiss();
    }

}

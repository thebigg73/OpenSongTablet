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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class PopUpSongCreateFragment extends DialogFragment {
    // This is a quick popup to enter a new song folder name, or rename a current one
    // Once it has been completed positively (i.e. ok was clicked) it sends a refreshAll() interface call

    static ArrayList<String> newtempfolders;
    Spinner newFolderSpinner;
    EditText newSongNameEditText;
    ProgressBar progressBar;
    private MyInterface mListener;
    AsyncTask<Object, Void, String> getfolders;
    StorageAccess storageAccess;
    Preferences preferences;
    SongFolders songFolders;
    ListSongFiles listSongFiles;
    SongXML songXML;

    static PopUpSongCreateFragment newInstance() {
        PopUpSongCreateFragment frag;
        frag = new PopUpSongCreateFragment();
        return frag;
    }

    public interface MyInterface {
        void doEdit();
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
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
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
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View V = inflater.inflate(R.layout.popup_songcreate, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.createanewsong));
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
        listSongFiles = new ListSongFiles();
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
        FullscreenActivity.currentFolder = FullscreenActivity.whichSongFolder;
        FullscreenActivity.newFolder = FullscreenActivity.whichSongFolder;
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
                FullscreenActivity.newFolder = newtempfolders.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        return V;

    }

    public void doSave() {
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
            FullscreenActivity.myToastMessage = "";
        }

        @Override
        protected String doInBackground(Object... objects) {
            // Get the variables
            Uri to = storageAccess.getUriForItem(getActivity(), preferences, "Songs", FullscreenActivity.newFolder, tempNewSong);
            Log.d("PopUpCreate", "to=" + to);
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
                storageAccess.deleteFile(getActivity(), to);

                try {
                    if (mListener != null) {
                        Log.d("PopUpCreate", "setting songfilename=" + tempNewSong);
                        FullscreenActivity.songfilename = tempNewSong;
                        Log.d("PopUpCreate", "setting whichSongFolder=" + FullscreenActivity.newFolder);
                        FullscreenActivity.whichSongFolder = FullscreenActivity.newFolder;
                    }

                } catch (Exception e) {
                    FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.error);
                    e.printStackTrace();
                }

            } else {
                if (!tempNewSong.equals("") && !tempNewSong.isEmpty()
                        && !tempNewSong.contains("/") && !storageAccess.uriExists(getActivity(), to)
                        && !tempNewSong.equals(FullscreenActivity.mainfoldername)) {

                    FullscreenActivity.whichSongFolder = FullscreenActivity.newFolder;

                    // Try to create
                    if (tempNewSong.endsWith(".pdf") || tempNewSong.endsWith(".PDF")) {
                        // Naughty, naughty, it shouldn't be a pdf extension
                        tempNewSong = tempNewSong.replace(".pdf", "");
                        tempNewSong = tempNewSong.replace(".PDF", "");
                    }

                    songXML.initialiseSongTags();

                    // Prepare the XML
                    FullscreenActivity.songfilename = tempNewSong;
                    FullscreenActivity.mTitle = tempNewSong;

                    Preferences.savePreferences();

                    FullscreenActivity.mynewXML = songXML.prepareBlankSongXML();

                    // If this is an import from text intent, add the text to the lyrics
                    if (FullscreenActivity.scripture_title != null &&
                            FullscreenActivity.scripture_title.equals("importedtext_in_scripture_verse") &&
                            FullscreenActivity.scripture_verse != null && !FullscreenActivity.scripture_verse.equals("")) {
                        FullscreenActivity.mLyrics = FullscreenActivity.scripture_verse;
                        FullscreenActivity.mynewXML = FullscreenActivity.mynewXML.replace("<lyrics>[V]\n</lyrics>",
                                "<lyrics>[V]\n" + FullscreenActivity.scripture_verse + "</lyrics>");
                    }

                    // Save the file
                    storageAccess.createFile(getActivity(), preferences, null, "Songs", FullscreenActivity.whichSongFolder, FullscreenActivity.songfilename);
                    Uri uri = storageAccess.getUriForItem(getActivity(), preferences, "Songs", FullscreenActivity.whichSongFolder,
                            FullscreenActivity.songfilename);

                    // Check the uri exists for the outputstream to be valid
                    storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, uri, null,
                            "Songs", FullscreenActivity.whichSongFolder, FullscreenActivity.songfilename);

                    OutputStream outputStream = storageAccess.getOutputStream(getActivity(), uri);
                    storageAccess.writeFileFromString(FullscreenActivity.mynewXML, outputStream);

                    // If we are autologging CCLI information
                    if (FullscreenActivity.ccli_automatic) {
                        PopUpCCLIFragment.addUsageEntryToLog(getActivity(), preferences, FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename,
                                FullscreenActivity.songfilename, "",
                                "", "", "1"); // Created
                    }
                } else {
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.error_notset);
                }
            }

            return null;
        }

        protected void onPostExecute(String s) {
            try {
                if (!FullscreenActivity.myToastMessage.equals("")) {
                    ShowToast.showToast(getActivity());
                }
                if (!FullscreenActivity.myToastMessage.equals(getActivity().getString(R.string.error)) &&
                        FullscreenActivity.myToastMessage.equals(getActivity().getString(R.string.error_notset))) {
                    if (mListener != null) {
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
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class GetFolders extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            songFolders.prepareSongFolders(getActivity(), storageAccess, preferences);
            return null;
        }

        protected void onPostExecute(String s) {
            // The song folder
            newtempfolders = new ArrayList<>();
            if (FullscreenActivity.mainfoldername!=null) {
                newtempfolders.add(FullscreenActivity.mainfoldername);
            }
            for (int e = 0; e < FullscreenActivity.mSongFolderNames.length; e++) {
                if (FullscreenActivity.mSongFolderNames[e] != null &&
                        !FullscreenActivity.mSongFolderNames[e].equals(FullscreenActivity.mainfoldername)) {
                    newtempfolders.add(FullscreenActivity.mSongFolderNames[e]);
                }
            }
            if (newtempfolders==null) {
                newtempfolders = new ArrayList<>();
                newtempfolders.add("");
            }
            ArrayAdapter<String> folders = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, newtempfolders);
            folders.setDropDownViewResource(R.layout.my_spinner);
            newFolderSpinner.setAdapter(folders);

            // Select the current folder as the preferred one - i.e. rename into the same folder
            newFolderSpinner.setSelection(0);
            for (int w = 0; w < newtempfolders.size(); w++) {
                if (FullscreenActivity.currentFolder.equals(newtempfolders.get(w)) ||
                        FullscreenActivity.currentFolder.equals("(" + newtempfolders.get(w) + ")")) {
                    newFolderSpinner.setSelection(w);
                    FullscreenActivity.newFolder = newtempfolders.get(w);
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

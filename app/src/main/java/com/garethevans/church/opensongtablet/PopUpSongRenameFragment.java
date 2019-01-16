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
import android.widget.Spinner;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class PopUpSongRenameFragment extends DialogFragment {
    // This is a quick popup to enter a new song folder name, or rename a current one
    // Once it has been completed positively (i.e. ok was clicked) it sends a refreshAll() interface call

    static ArrayList<String> newtempfolders;
    Spinner newFolderSpinner;
    EditText newSongNameEditText;
    boolean isPDF;
    String oldsongname;
    AsyncTask<Object, Void, String> getfolders;
    StorageAccess storageAccess;
    Preferences preferences;
    SongFolders songFolders;

    public interface MyInterface {
        void refreshAll();
    }

    private MyInterface mListener;

    static PopUpSongRenameFragment newInstance() {
        PopUpSongRenameFragment frag;
        frag = new PopUpSongRenameFragment();
        return frag;
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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_songrename, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.options_song_rename));
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
        songFolders = new SongFolders();
        preferences = new Preferences();

        // Initialise the views
        newFolderSpinner = V.findViewById(R.id.newFolderSpinner);
        newSongNameEditText = V.findViewById(R.id.newSongNameEditText);

        oldsongname = FullscreenActivity.songfilename;
        newSongNameEditText.setText(oldsongname);
        isPDF = oldsongname.endsWith(".pdf") || oldsongname.endsWith(".PDF");

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
        // Get the variables
        String tempNewSong = newSongNameEditText.getText().toString().trim();

        String tempOldFolder = FullscreenActivity.currentFolder;
        String tempNewFolder = FullscreenActivity.newFolder;

        // Try to rename
        if (isPDF) {
            if (!tempNewSong.endsWith(".pdf") && !tempNewSong.endsWith(".PDF")) {
                // Naughty, naughty, it should have a pdf extension
                tempNewSong = tempNewSong + ".pdf";
            }
        }

        storageAccess = new StorageAccess();
        preferences = new Preferences();

        Uri from = storageAccess.getUriForItem(getActivity(), preferences, "Songs", tempOldFolder, oldsongname);
        Uri to = storageAccess.getUriForItem(getActivity(), preferences, "Songs", tempNewFolder, tempNewSong);

        if (!storageAccess.uriExists(getActivity(),to)) {
            try {
                InputStream inputStream = storageAccess.getInputStream(getActivity(), from);

                // Check the uri exists for the outputstream to be valid
                storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, to, null,
                        "Songs", tempNewFolder, tempNewSong);

                OutputStream outputStream = storageAccess.getOutputStream(getActivity(), to);

                // Copy
                storageAccess.copyFile(inputStream, outputStream);

                // Remove the original if it is a new file location
                if (to.getPath()!=null && !to.getPath().equals(from.getPath())) {
                    storageAccess.deleteFile(getActivity(), from);
                }

                FullscreenActivity.whichSongFolder = tempNewFolder;
                FullscreenActivity.songfilename = tempNewSong;

                // Save preferences
                Preferences.savePreferences();

                // Rebuild the song list
                storageAccess.listSongs(getActivity(), preferences);
                ListSongFiles listSongFiles = new ListSongFiles();
                listSongFiles.songUrisInFolder(getActivity(), preferences);

                mListener.refreshAll();

                dismiss();
            } catch (Exception e) {
                Log.d("d","Error renaming");
            }

        } else {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.file_exists);
            ShowToast.showToast(getActivity());

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

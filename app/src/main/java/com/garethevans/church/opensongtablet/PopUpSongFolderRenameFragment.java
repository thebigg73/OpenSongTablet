package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

import static android.provider.DocumentsContract.Document.MIME_TYPE_DIR;

public class PopUpSongFolderRenameFragment extends DialogFragment {
    // This is a quick popup to enter a new song folder name, or rename a current one
    // Once it has been completed positively (i.e. ok was clicked) it sends a refreshAll() interface call

    private static String myTask;
    private String mTitle;
    private static ArrayList<String> oldtempfolders;
    private GetFoldersAsync getFolders_async;
    private String tempNewFolder;
    private String tempOldFolder;
    private TextView title;
    private ProgressBar progressBar;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private SongFolders songFolders;
    private SQLiteHelper sqLiteHelper;

    static PopUpSongFolderRenameFragment newInstance(String message) {
        myTask = message;
        PopUpSongFolderRenameFragment frag;
        frag = new PopUpSongFolderRenameFragment();
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.rename));
        if ("create".equals(myTask)) {
            mTitle = getString(R.string.folder_new);
        } else {
            mTitle = getString(R.string.rename);
        }
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View V = inflater.inflate(R.layout.popup_songfolderrename, container, false);

        title = V.findViewById(R.id.dialogtitle);
        doSetTitle();
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getActivity());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe,getActivity());
            createOrRename();
        });

        storageAccess = new StorageAccess();
        preferences = new Preferences();
        songFolders = new SongFolders();
        sqLiteHelper = new SQLiteHelper(getActivity());

        // Initialise the views
        oldFolderNameSpinner = V.findViewById(R.id.oldFolderNameSpinner);
        newFolderNameEditText = V.findViewById(R.id.newFolderNameEditText);
        progressBar = V.findViewById(R.id.progressBar);
        TextView currentFolder_TextView = V.findViewById(R.id.currentFolder_TextView);

        // Set up the folderspinner
        // Set up the spinner
        // Populate the list view with the current song folders
        // Reset to the main songs folder, so we can list them
        FullscreenActivity.currentFolder = StaticVariables.whichSongFolder;
        FullscreenActivity.newFolder = StaticVariables.whichSongFolder;
        //FullscreenActivity.whichSongFolder = "";

        // Do the time consuming bit as an asynctask
        getFolders_async = new GetFoldersAsync();
        try {
            getFolders_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Probably closed popup before folders listed\n"+e);
        }

        if (myTask.equals("create")) {
            // Hide the spinner
            oldFolderNameSpinner.setVisibility(View.GONE);

            // Show the current folder location (to allow subfolder creation)
            currentFolder_TextView.setVisibility(View.VISIBLE);
            String s;
            if (StaticVariables.whichSongFolder.equals("") ||
                    StaticVariables.whichSongFolder.equals(getString(R.string.mainfoldername)) ||
                    StaticVariables.whichSongFolder.equals("MAIN")) {
                s = "../OpenSong/Songs/";
            } else {
                s = "../OpenSong/Songs/" + StaticVariables.whichSongFolder + "/";
            }
            currentFolder_TextView.setText(s);
        }

        // Set the oldFolderNameSpinnerListener
        oldFolderNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FullscreenActivity.currentFolder = oldtempfolders.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);
        return V;
    }

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

    private Spinner oldFolderNameSpinner;
    private EditText newFolderNameEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    private void createOrRename() {
        progressBar.setVisibility(View.VISIBLE);
        getVariables();
        if (myTask.equals("create")) {
            doCreateFolder();
        } else {
            doRenameFolder();
        }
    }

    private void doSetTitle() {
        title.setText(mTitle);
    }

    private void doCreateFolder() {
        // Try to create inside the current folder
        @SuppressLint("StaticFieldLeak") AsyncTask<Object, String, String> docreation = new AsyncTask<Object, String, String>() {
            @Override
            protected String doInBackground(Object... objects) {
                if (!StaticVariables.whichSongFolder.equals(getString(R.string.mainfoldername)) &&
                        !StaticVariables.whichSongFolder.equals("") && !StaticVariables.whichSongFolder.equals("MAIN")) {
                    tempNewFolder = StaticVariables.whichSongFolder + "/" + tempNewFolder;
                }
                if (storageAccess.createFile(getActivity(), preferences, MIME_TYPE_DIR, "Songs", tempNewFolder, null)) {
                    StaticVariables.myToastMessage = getResources().getString(R.string.newfolder) + " - " + getResources().getString(R.string.ok);
                    StaticVariables.whichSongFolder = tempNewFolder;

                    // Add the new folder to the SQLite database
                    sqLiteHelper.createSong(getActivity(),StaticVariables.whichSongFolder,"");

                } else {
                    StaticVariables.myToastMessage = getResources().getString(R.string.newfolder) + " - " + getResources().getString(R.string.createfoldererror);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                ShowToast.showToast(getActivity());

                progressBar.setVisibility(View.GONE);

                // Let the app know we need to rebuild the database
                FullscreenActivity.needtorefreshsongmenu = true;

                if (mListener != null) {
                    mListener.loadSong();
                }
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        docreation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public interface MyInterface {
        void rebuildSearchIndex();
        void loadSong();
    }

    private void doRenameFolder() {

        tempOldFolder = FullscreenActivity.currentFolder;
        // Try to rename
        @SuppressLint("StaticFieldLeak") AsyncTask<Object, String, String> renamefolders = new AsyncTask<Object, String, String>() {

            @Override
            protected String doInBackground(Object... objects) {
                if (storageAccess.renameFolder(getActivity(), preferences, tempOldFolder, tempNewFolder)) {
                    sqLiteHelper.updateFolderName(getActivity(),tempOldFolder,tempNewFolder);
                    return "success";
                } else {
                    return "error";
                }
            }

            @Override
            protected void onPostExecute(String s) {
                if (mListener != null) {
                    mListener.loadSong();
                    mListener.rebuildSearchIndex();
                }
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        renamefolders.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getVariables() {
        // Get the variables
        tempNewFolder = newFolderNameEditText.getText().toString().trim();
        tempNewFolder = storageAccess.safeFilename(tempNewFolder);
        newFolderNameEditText.setText(tempNewFolder);
        Uri uri = storageAccess.getUriForItem(getActivity(), preferences, "Songs", tempNewFolder, "");

        if (!tempNewFolder.equals("") && !tempNewFolder.isEmpty() && !tempNewFolder.contains("/") &&
                !storageAccess.uriExists(getActivity(),uri) &&
                !tempNewFolder.equals(getString(R.string.mainfoldername)) &&
                !tempNewFolder.equals("MAIN")) {
            FullscreenActivity.newFolder = tempNewFolder;
            tempOldFolder = FullscreenActivity.currentFolder;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFoldersAsync extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            ArrayList<String> initialoldtempfolders = songFolders.prepareSongFolders(getActivity(),preferences);
            // We don't want the main folder to show here
            oldtempfolders = new ArrayList<>();
            for (String s:initialoldtempfolders) {
                if (!s.equals(getString(R.string.mainfoldername)) && !s.equals("MAIN") && !s.equals((""))) {
                    oldtempfolders.add(s);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                ArrayAdapter<String> folders = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.my_spinner, oldtempfolders);
                folders.setDropDownViewResource(R.layout.my_spinner);
                oldFolderNameSpinner.setAdapter(folders);

                // Select the current folder as the preferred one
                oldFolderNameSpinner.setSelection(0);
                for (int w = 0; w < oldtempfolders.size(); w++) {
                    if (StaticVariables.whichSongFolder.equals(oldtempfolders.get(w))) {
                        oldFolderNameSpinner.setSelection(w);
                        FullscreenActivity.currentFolder = oldtempfolders.get(w);
                    }
                }
            } catch (Exception e) {
                // Error
            }
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        if (getFolders_async!=null) {
            getFolders_async.cancel(true);
        }
        this.dismiss();
    }
}

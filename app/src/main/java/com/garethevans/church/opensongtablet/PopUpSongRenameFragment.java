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
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class PopUpSongRenameFragment extends DialogFragment {
    // This is a quick popup to enter a new song folder name, or rename a current one, or duplicate
    // Once it has been completed positively (i.e. ok was clicked) it sends a rebuildSongIndex() interface call

    private static ArrayList<String> foldernames;
    private Spinner newFolderSpinner;
    private EditText newSongNameEditText;
    private boolean isPDF;
    private String oldsongname;
    private AsyncTask<Object, Void, String> getfolders;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private SongFolders songFolders;
    private SQLite sqLite;
    private SQLiteHelper sqLiteHelper;

    private void doSave() {
        // Get the variables
        String tempNewSong = newSongNameEditText.getText().toString().trim();
        tempNewSong = storageAccess.safeFilename(tempNewSong);
        newSongNameEditText.setText(tempNewSong);
        String tempOldFolder = FullscreenActivity.currentFolder;
        String tempNewFolder = FullscreenActivity.newFolder;

        // Try to rename
        if (isPDF) {
            if (!tempNewSong.endsWith(".pdf") && !tempNewSong.endsWith(".PDF")) {
                // Naughty, naughty, it should have a pdf extension
                tempNewSong = tempNewSong + ".pdf";
            }
        }

        Uri from = storageAccess.getUriForItem(getContext(), preferences, "Songs", tempOldFolder, oldsongname);
        Uri to = storageAccess.getUriForItem(getContext(), preferences, "Songs", tempNewFolder, tempNewSong);

        if (!storageAccess.uriExists(getContext(), to)) {
            try {
                InputStream inputStream = storageAccess.getInputStream(getContext(), from);

                // Check the uri exists for the outputstream to be valid
                storageAccess.lollipopCreateFileForOutputStream(getContext(), preferences, to, null,
                        "Songs", tempNewFolder, tempNewSong);

                OutputStream outputStream = storageAccess.getOutputStream(getContext(), to);

                // Copy
                storageAccess.copyFile(inputStream, outputStream);

                StaticVariables.whichSongFolder = tempNewFolder;
                StaticVariables.songfilename = tempNewSong;

                // Update the SQLite database
                // IV - Duplicate a received or variation song (the variation is still needed by the set)
                if (FullscreenActivity.whattodo.equals("duplicate") | oldsongname.equals("ReceivedSong") | tempOldFolder.equals("../Variations")) {
                    sqLiteHelper.createSong(getContext(),StaticVariables.whichSongFolder,StaticVariables.songfilename);
                    String songId = StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;
                    sqLite = sqLiteHelper.getSong(getContext(), songId);
                    sqLite.setFolder(StaticVariables.whichSongFolder);
                    sqLite.setLyrics(StaticVariables.mLyrics);
                    sqLite.setTitle(StaticVariables.mTitle);
                    sqLite.setFilename(StaticVariables.songfilename);
                    sqLite.setAka(StaticVariables.mAka);
                    sqLite.setAlttheme(StaticVariables.mAltTheme);
                    sqLite.setAuthor(StaticVariables.mAuthor);
                    sqLite.setCcli(StaticVariables.mCCLI);
                    sqLite.setCopyright(StaticVariables.mCopyright);
                    sqLite.setHymn_num(StaticVariables.mHymnNumber);
                    sqLite.setKey(StaticVariables.mKey);
                    sqLite.setTheme(StaticVariables.mTheme);
                    sqLite.setTimesig(StaticVariables.mTimeSig);
                    sqLite.setUser1(StaticVariables.mUser1);
                    sqLite.setUser2(StaticVariables.mUser2);
                    sqLite.setUser3(StaticVariables.mUser3);
                    sqLite.setSongid(songId);
                } else {
                    // Remove the original if it is a new file location
                    if (to.getPath() != null && !to.getPath().equals(from.getPath())) {
                        storageAccess.deleteFile(getContext(), from);
                    }
                    String songId = StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;
                    sqLite.setSongid(songId);
                    sqLite.setFolder(StaticVariables.whichSongFolder);
                    sqLite.setFilename(StaticVariables.songfilename);
                }
                sqLiteHelper.updateSong(getContext(), sqLite);

                if (mListener!=null) {
                    mListener.loadSong();
                }
            } catch (Exception e) {
                Log.d("d", "Error renaming");
            }
            try {
                dismiss();
            } catch (Exception e) {
                Log.d("PopUpSongRename", "Popup already closed");
            }
        } else {
            StaticVariables.myToastMessage = getString(R.string.file_exists);
            ShowToast.showToast(getContext());
        }
    }

    private MyInterface mListener;

    static PopUpSongRenameFragment newInstance() {
        PopUpSongRenameFragment frag;
        frag = new PopUpSongRenameFragment();
        return frag;
    }

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

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }
        View V = inflater.inflate(R.layout.popup_songrename, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        if (FullscreenActivity.whattodo.equals("duplicate")) {
            title.setText(R.string.duplicate);
        } else {
            title.setText(getString(R.string.rename));
        }
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe,getContext());
            doSave();
        });

        storageAccess = new StorageAccess();
        songFolders = new SongFolders();
        preferences = new Preferences();

        // Get the song details
        sqLiteHelper = new SQLiteHelper(getContext());
        String songId = StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;
        sqLite = sqLiteHelper.getSong(getContext(),songId);

        // Initialise the views
        newFolderSpinner = V.findViewById(R.id.newFolderSpinner);
        newSongNameEditText = V.findViewById(R.id.newSongNameEditText);

        oldsongname = StaticVariables.songfilename;
        // IV - When a received song - use the stored received song filename
        if (StaticVariables.songfilename.equals("ReceivedSong")) {
            newSongNameEditText.setText(storageAccess.safeFilename(StaticVariables.receivedSongfilename));
        } else {
            newSongNameEditText.setText(storageAccess.safeFilename(StaticVariables.songfilename ));
        }
        isPDF = oldsongname.endsWith(".pdf") || oldsongname.endsWith(".PDF");

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
                FullscreenActivity.newFolder = foldernames.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);
        return V;
    }

    public interface MyInterface {
        void loadSong();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFolders extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            foldernames = songFolders.prepareSongFolders(getContext(),preferences);
            return null;
        }

        protected void onPostExecute(String s) {
            // The song folder
            // IV - Commented out as preparesongfolders already adds mainfoldername at 0
            //foldernames.add(0, getString(R.string.mainfoldername));
            ArrayAdapter<String> folders = new ArrayAdapter<>(requireContext(), R.layout.my_spinner, foldernames);
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
    public void onCancel(@NonNull DialogInterface dialog) {
        if (getfolders!=null) {
            getfolders.cancel(true);
        }
        this.dismiss();
    }

}

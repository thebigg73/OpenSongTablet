package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
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

import java.io.File;
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

        TextView title = (TextView) V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.options_song_rename));
        final FloatingActionButton closeMe = (FloatingActionButton) V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = (FloatingActionButton) V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                //saveMe.setEnabled(false);
                doSave();
            }
        });

        // Initialise the views
        newFolderSpinner = (Spinner) V.findViewById(R.id.newFolderSpinner);
        newSongNameEditText = (EditText) V.findViewById(R.id.newSongNameEditText);

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

        return V;
    }

    public void doSave() {
        // Get the variables
        String tempNewSong = newSongNameEditText.getText().toString().trim();

        String tempOldFolder = FullscreenActivity.currentFolder;
        String tempNewFolder = FullscreenActivity.newFolder;

        File to;
        if (tempNewFolder.equals(FullscreenActivity.mainfoldername)) {
            to = new File(FullscreenActivity.dir + "/" + tempNewSong);
        } else {
            to = new File(FullscreenActivity.dir + "/" + tempNewFolder + "/" + tempNewSong);
        }

        File from;
        if (tempOldFolder.equals("") || tempOldFolder.equals(FullscreenActivity.mainfoldername)) {
            from = new File(FullscreenActivity.dir + "/" + oldsongname);
        } else {
            from = new File(FullscreenActivity.dir + "/" + tempOldFolder + "/" + oldsongname);
        }

        if (!tempNewSong.equals("") && !tempNewSong.isEmpty()
                && !tempNewSong.contains("/") && !to.exists()
                && !tempNewSong.equals(FullscreenActivity.mainfoldername)) {

            // Try to rename
            if (isPDF) {
                if (!tempNewSong.endsWith(".pdf") && !tempNewSong.endsWith(".PDF")) {
                    // Naughty, naughty, it should be a pdf extensions
                    tempNewSong = tempNewSong + ".pdf";
                }
            }

            if (from.renameTo(to)) {
                FullscreenActivity.myToastMessage = getResources().getString(R.string.renametitle) + " - " + getResources().getString(R.string.ok);
            } else {
                FullscreenActivity.myToastMessage = getResources().getString(R.string.renametitle) + " - " + getResources().getString(R.string.error_notset);
            }

            FullscreenActivity.whichSongFolder = tempNewFolder;
            FullscreenActivity.songfilename = tempNewSong;

            // Save preferences
            Preferences.savePreferences();

            mListener.refreshAll();

            dismiss();

        } else if (to.exists()) {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.file_exists);
            ShowToast.showToast(getActivity());

        } else {
            FullscreenActivity.myToastMessage = getResources().getString(R.string.no);
            ShowToast.showToast(getActivity());
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFolders extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            ListSongFiles.getAllSongFolders();
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

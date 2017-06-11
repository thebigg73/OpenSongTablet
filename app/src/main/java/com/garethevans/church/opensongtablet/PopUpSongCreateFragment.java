package com.garethevans.church.opensongtablet;

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

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class PopUpSongCreateFragment extends DialogFragment {
    // This is a quick popup to enter a new song folder name, or rename a current one
    // Once it has been completed positively (i.e. ok was clicked) it sends a refreshAll() interface call

    static ArrayList<String> newtempfolders;
    Spinner newFolderSpinner;
    EditText newSongNameEditText;
    private MyInterface mListener;
    AsyncTask<Object, Void, String> getfolders;

    static PopUpSongCreateFragment newInstance() {
        PopUpSongCreateFragment frag;
        frag = new PopUpSongCreateFragment();
        return frag;
    }

    public interface MyInterface {
        void openSongEdit();
        void loadSong();
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

        TextView title = (TextView) V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.createanewsong));
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

        if (FullscreenActivity.whattodo.equals("savecameraimage")) {
            try {
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

        return V;

    }

    public void doSave() {
        // Get the variables
        String tempNewSong = newSongNameEditText.getText().toString().trim();

        if (FullscreenActivity.whattodo.equals("savecameraimage")) {
            File from = new File(FullscreenActivity.mCurrentPhotoPath);
            String currimagename = FullscreenActivity.mCurrentPhotoPath.
                    substring(FullscreenActivity.mCurrentPhotoPath.lastIndexOf("/")+1);

            Log.d("d","currimagename="+currimagename);
            Log.d("d","from="+FullscreenActivity.mCurrentPhotoPath);

            // If no name is specified, use the original ugly one
            if (tempNewSong.isEmpty() || tempNewSong.equals("")) {
                Log.d("d","currimagename="+currimagename);
                tempNewSong = currimagename;
            }

            // Check the camera image ends with .jpg.  If not, add it!
            if (!tempNewSong.endsWith(".jpg")) {
                tempNewSong = tempNewSong + ".jpg";
            }
            File to;
            if (FullscreenActivity.newFolder.equals(FullscreenActivity.mainfoldername)) {
                to = new File(FullscreenActivity.dir + "/" + tempNewSong);
            } else {
                to = new File(FullscreenActivity.dir + "/" + FullscreenActivity.newFolder + "/" + tempNewSong);
            }

            Log.d("d","to="+to);

            try {
                FileChannel inChannel = new FileInputStream(from).getChannel();
                FileChannel outChannel = new FileOutputStream(to).getChannel();
                try {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                } finally {
                    if (inChannel != null)
                        inChannel.close();
                    outChannel.close();
                }

                FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.success);
                if (!from.delete()) {
                    Log.d("d","Error deleting");
                }

                if (mListener != null) {
                    FullscreenActivity.songfilename = tempNewSong;
                    FullscreenActivity.whichSongFolder = FullscreenActivity.newFolder;
                    mListener.loadSong();
                }

            } catch (Exception e) {
                if (!from.delete()) {
                    Log.d("d","Error deleting");
                }
                FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.error);
                ShowToast.showToast(getActivity());
            }
            // Remove the file anyway as a tidy up

            ShowToast.showToast(getActivity());
            dismiss();

        } else {
            File to;
            if (FullscreenActivity.newFolder.equals(FullscreenActivity.mainfoldername)) {
                to = new File(FullscreenActivity.dir + "/" + tempNewSong);
            } else {
                to = new File(FullscreenActivity.dir + "/" + FullscreenActivity.newFolder + "/" + tempNewSong);
            }

            if (!tempNewSong.equals("") && !tempNewSong.isEmpty()
                    && !tempNewSong.contains("/") && !to.exists()
                    && !tempNewSong.equals(FullscreenActivity.mainfoldername)) {

                FullscreenActivity.whichSongFolder = FullscreenActivity.newFolder;

                // Try to create
                if (tempNewSong.endsWith(".pdf") || tempNewSong.endsWith(".PDF")) {
                    // Naughty, naughty, it shouldn't be a pdf extension
                    tempNewSong = tempNewSong.replace(".pdf", "");
                    tempNewSong = tempNewSong.replace(".PDF", "");
                }

                LoadXML.initialiseSongTags();

                // Prepare the XML
                FullscreenActivity.songfilename = tempNewSong;
                FullscreenActivity.mTitle = tempNewSong;
                Preferences.savePreferences();

                PopUpEditSongFragment.prepareBlankSongXML();

                // Save the file
                try {
                    PopUpEditSongFragment.justSaveSongXML();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Load the XML up into memory
                try {
                    LoadXML.loadXML(getActivity());
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }

                // Tell the main page to now edit the song
                if (mListener != null) {
                    mListener.openSongEdit();
                }

                // Close the popup
                dismiss();
            } else {
                FullscreenActivity.myToastMessage = getResources().getString(R.string.error_notset);
                ShowToast.showToast(getActivity());
            }
        }
    }

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

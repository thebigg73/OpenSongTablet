package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class PopUpRandomSongFragment extends DialogFragment {

    static PopUpRandomSongFragment newInstance() {
        PopUpRandomSongFragment frag;
        frag = new PopUpRandomSongFragment();
        return frag;
    }

    public interface MyInterface {
        void loadSong();
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    private LinearLayout foundSong_Button;
    private TextView foundSongTitle_TextView;
    private TextView foundSongFolder_TextView;
    private ListView chooseFolders_ListView;
    private boolean songisvalid = false;
    private Preferences preferences;
    private ArrayList<String> foldernames;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_randomsong, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.random_song));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getActivity());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();
        SongFolders songFolders = new SongFolders();

        // Initialise the views
        foundSong_Button = V.findViewById(R.id.foundSong_Button);
        foundSongTitle_TextView = V.findViewById(R.id.foundSongTitle_TextView);
        foundSongFolder_TextView = V.findViewById(R.id.foundSongFolder_TextView);
        Button generateRandom_Button = V.findViewById(R.id.generateRandom_Button);
        chooseFolders_ListView = V.findViewById(R.id.chooseFolders_ListView);

        // Update the song folders
        //FullscreenActivity.songfilelist = new SongFileList();
        foldernames = songFolders.prepareSongFolders(getActivity(),preferences);

        // Try to generate the file folders available to choose from and highlight the ones already specified
        generateFolderList();

        // Update the random songs available
        AsyncTask<Void,Void,String> update = new BuildSongsToChooseFrom();
        update.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // Add a listener to the buttons
        generateRandom_Button.setOnClickListener(view -> updateRandomSong(getRandomSong()));
        foundSong_Button.setOnClickListener(view -> {
            if (songisvalid) {
                StaticVariables.whichSongFolder = foundSongFolder_TextView.getText().toString();
                StaticVariables.songfilename = foundSongTitle_TextView.getText().toString();
                if (mListener!=null) {
                    mListener.loadSong();
                    try {
                        dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void generateFolderList() {
        // Since users change their folders, update this chosen list with those actually available

        // Due to a previous bug where $$__ wasn't added to some folders in the preference, meaning it can't be removed
        // Tidy up the preferences
        String prefs = preferences.getMyPreferenceString(getActivity(),"randomSongFolderChoice","");
        prefs = prefs.replace("$$__","%%");
        prefs = prefs.replace("__$$","%%");
        String[] bits = prefs.split("%%");
        StringBuilder sb = new StringBuilder();
        for (String bit:bits) {
            if (bit!=null && !bit.isEmpty()) {
                sb.append("$$__").append(bit).append("__$$");
            }
        }
        preferences.setMyPreferenceString(getActivity(),"randomSongFolderChoice",sb.toString());

        StringBuilder newRandomFoldersChosen = new StringBuilder();
        if (foldernames!=null) {
            ArrayAdapter<String> songfolders = new ArrayAdapter<>(Objects.requireNonNull(getActivity()),
                    android.R.layout.simple_list_item_checked, foldernames);
            chooseFolders_ListView.setAdapter(songfolders);

            // Go through each folder available and tick it if the folder is in the randomFolders string
            for (int i = 0; i < foldernames.size(); i++) {
                if (preferences.getMyPreferenceString(getContext(),"randomSongFolderChoice","").contains("$$__"+foldernames.get(i)+"__$$")) {
                    chooseFolders_ListView.setItemChecked(i,true);
                    newRandomFoldersChosen.append("$$__").append(foldernames.get(i)).append("__$$");
                } else {
                    chooseFolders_ListView.setItemChecked(i,false);
                }
            }

            chooseFolders_ListView.setOnItemClickListener((adapterView, view, i, l) -> {
                if (chooseFolders_ListView.isItemChecked(i)) {
                    if (!preferences.getMyPreferenceString(getActivity(),"randomSongFolderChoice","").contains("$$__"+foldernames.get(i)+"__$$")) {
                        // Not there, so add it
                        String rf = preferences.getMyPreferenceString(getActivity(),"randomSongFolderChoice","") +
                                "$$__" + foldernames.get(i)+"__$$";
                        preferences.setMyPreferenceString(getActivity(),"randomSongFolderChoice",rf);
                    }
                } else {
                    // Trying to remove it
                    if (preferences.getMyPreferenceString(getActivity(),"randomSongFolderChoice","").contains("$$__"+foldernames.get(i)+"__$$")) {
                        // There, so remove it
                        String rf = preferences.getMyPreferenceString(getActivity(),"randomSongFolderChoice","");
                        rf = rf.replace("$$__"+foldernames.get(i)+"__$$","");
                        preferences.setMyPreferenceString(getActivity(),"randomSongFolderChoice",rf);
                    }
                }
            });
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class BuildSongsToChooseFrom extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() { }

        @Override
        protected String doInBackground(Void... voids) {
            return getRandomSong();
        }

        @Override
        protected void onPostExecute(String song) {
            updateRandomSong(song);
        }
    }

    private void updateRandomSong(String song) {
        // Update the button with the randomly found song
        if (!song.equals("")) {
            String[] r_song = song.split("_%%_");
            if (r_song.length>=2 && r_song[0]!=null && r_song[1]!=null && !r_song[0].equals("") && !r_song[1].equals("")) {
                foundSongTitle_TextView.setText(r_song[1]);
                foundSongFolder_TextView.setText(r_song[0]);
                foundSong_Button.setVisibility(View.VISIBLE);
                songisvalid = true;

            } else {
                foundSong_Button.setVisibility(View.GONE);
                foundSongTitle_TextView.setText(getString(R.string.error));
                foundSongFolder_TextView.setText("");
                songisvalid = false;
            }
        }
    }

    private String getRandomSong() {
        try {
            // This feature randomly picks a song from the user's database
            ArrayList<String> songstochoosefrom = new ArrayList<>();
            SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity());
            ArrayList<SQLite> allsongs = sqLiteHelper.getAllSongs(getActivity());

            for (SQLite check : allsongs) {
                if (preferences.getMyPreferenceString(getActivity(), "randomSongFolderChoice", "").
                        contains(check.getFolder()) && check.getFilename()!=null && !check.getFilename().isEmpty()) {
                    songstochoosefrom.add(check.getFolder() + "_%%_" + check.getFilename());
                }
            }
            int rand = getRandomNumber(songstochoosefrom.size());
            if (rand > -1 && songstochoosefrom.size() > rand) {
                return songstochoosefrom.get(rand);
            } else {
                return "";
            }

        } catch (Exception e) {
            StaticVariables.myToastMessage = getString(R.string.search_index_error);
            return "";
        }
    }

    private int getRandomNumber(int max) {
        return (new Random()).nextInt(max+1);
    }

}


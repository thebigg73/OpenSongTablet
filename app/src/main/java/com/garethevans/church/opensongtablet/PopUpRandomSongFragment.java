package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
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

    LinearLayout foundSong_Button;
    TextView foundSongTitle_TextView;
    TextView foundSongFolder_TextView;
    Button generateRandom_Button;
    ListView chooseFolders_ListView;
    boolean songisvalid = false;
    boolean iserror = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_randomsong, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.profile));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        // Initialise the views
        foundSong_Button = V.findViewById(R.id.foundSong_Button);
        foundSongTitle_TextView = V.findViewById(R.id.foundSongTitle_TextView);
        foundSongFolder_TextView = V.findViewById(R.id.foundSongFolder_TextView);
        generateRandom_Button = V.findViewById(R.id.generateRandom_Button);
        chooseFolders_ListView = V.findViewById(R.id.chooseFolders_ListView);

        // Try to generate the file folders available to choose from and highlight the ones already specified
        generateFolderList();

        // Update the random songs available
        AsyncTask<Void,Void,String> update = new BuildSongsToChooseFrom();
        update.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // Add a listener to the buttons
        generateRandom_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateRandomSong(getRandomSong());
            }
        });
        foundSong_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (songisvalid) {
                    FullscreenActivity.whichSongFolder = foundSongFolder_TextView.getText().toString();
                    FullscreenActivity.songfilename = foundSongTitle_TextView.getText().toString();
                    Preferences.savePreferences();
                    if (mListener!=null) {
                        mListener.loadSong();
                        dismiss();
                    }
                }
            }
        });

        return V;
    }

    public void generateFolderList() {
        // Since users change their folders, update this chosen list with those actually available
        String newRandomFoldersChosen = "";
        if (FullscreenActivity.mSongFolderNames!=null) {
            ArrayAdapter<String> songfolders = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_checked, FullscreenActivity.mSongFolderNames);
            chooseFolders_ListView.setAdapter(songfolders);

            // Go through each folder available and tick it if the folder is in the randomFolders string
            for (int i = 0; i < FullscreenActivity.mSongFolderNames.length; i++) {
                if (FullscreenActivity.randomFolders.contains("$$__"+FullscreenActivity.mSongFolderNames[i]+"__$$")) {
                    chooseFolders_ListView.setItemChecked(i,true);
                    newRandomFoldersChosen += "$$__"+FullscreenActivity.mSongFolderNames[i]+"__$$";
                } else {
                    chooseFolders_ListView.setItemChecked(i,false);
                }
            }

            chooseFolders_ListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d("d","selected i="+i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            chooseFolders_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d("d","itemclicked i="+i);
                    if (chooseFolders_ListView.isItemChecked(i)) {
                        if (!FullscreenActivity.randomFolders.contains("$$__"+FullscreenActivity.mSongFolderNames[i]+"__$$")) {
                            // Not there, so add it
                            FullscreenActivity.randomFolders += "$$__"+FullscreenActivity.mSongFolderNames[i]+"__$$";
                            Preferences.savePreferences();
                        }
                    } else {
                        // Trying to remove it
                        if (FullscreenActivity.randomFolders.contains("$$__"+FullscreenActivity.mSongFolderNames[i]+"__$$")) {
                            // There, so remove it
                            FullscreenActivity.randomFolders =
                                    FullscreenActivity.randomFolders.replace("$$__"+FullscreenActivity.mSongFolderNames[i]+"__$$","");
                            Preferences.savePreferences();
                        }
                    }
                }
            });

            // Update the chosen folders based on what is actually available
            FullscreenActivity.randomFolders = newRandomFoldersChosen;
            Preferences.savePreferences();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class BuildSongsToChooseFrom extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... voids) {
            return getRandomSong();
        }

        @Override
        protected void onPostExecute(String song) {
            updateRandomSong(song);
        }
    }

    public void updateRandomSong(String song) {
        // Update the button with the randomly found song
        if (!song.equals("")) {
            String[] r_song = song.split("__");
            if (r_song.length>=2 && r_song[0]!=null && r_song[1]!=null && !r_song[0].equals("") && !r_song[1].equals("")) {
                foundSongTitle_TextView.setText(r_song[1]);
                foundSongFolder_TextView.setText(r_song[0]);
                foundSong_Button.setVisibility(View.VISIBLE);
                songisvalid = true;

            } else {
                foundSong_Button.setVisibility(View.GONE);
                foundSongTitle_TextView.setText(getActivity().getString(R.string.error));
                foundSongFolder_TextView.setText("");
                songisvalid = false;
            }
        }
    }

    public String getRandomSong() {
        // This feature randomly picks a song from the user's database
        ArrayList<String> songstochoosefrom = new ArrayList<>();
        if (FullscreenActivity.search_database!=null && FullscreenActivity.search_database.size()>0) {
            Log.d("d","search_database.size()="+FullscreenActivity.search_database.size());
            for (String check : FullscreenActivity.search_database) {
                String[] bits = check.split("_%%%_");
                if (bits.length >= 2 && bits[0] != null && bits[1] != null &&
                        FullscreenActivity.randomFolders.contains("$$__" + bits[1].trim() + "__$$")) {
                    songstochoosefrom.add(bits[1].trim() + "__" + bits[0].trim());
                }
            }
            iserror = false;
            int rand = getRandomNumber(songstochoosefrom.size());
            if (rand > -1 && songstochoosefrom.size()>rand) {
                return songstochoosefrom.get(rand);
            } else {
                return "";
            }
        } else {
            FullscreenActivity.myToastMessage = getActivity().getString(R.string.search_index_error);
            iserror = true;
            return "";
        }
    }

    private int getRandomNumber(int max) {
        return (new Random()).nextInt(max+1);
    }

}


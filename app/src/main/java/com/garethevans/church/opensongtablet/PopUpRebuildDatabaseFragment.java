package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
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
import android.widget.ProgressBar;
import android.widget.TextView;

// This is called when something requires a database rebuild (something in the song menu has changed)

public class PopUpRebuildDatabaseFragment extends DialogFragment {

    public static MyInterface mListener;
    private TextView progressText;
    private ProgressBar progressBar, simpleProgressBar;
    FloatingActionButton closeMe, saveMe;
    View V;
    StorageAccess storageAccess;
    Preferences preferences;
    IndexSongs indexSongs;
    SongXML songXML;
    ChordProConvert chordProConvert;
    OnSongConvert onSongConvert;
    TextSongConvert textSongConvert;
    UsrConvert usrConvert;
    ProcessSong processSong;
    Uri uri;

    static PopUpRebuildDatabaseFragment newInstance() {
        PopUpRebuildDatabaseFragment frag;
        frag = new PopUpRebuildDatabaseFragment();
        return frag;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        if (getDialog() == null) {
            dismiss();
        }

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        V = inflater.inflate(R.layout.popup_rebuilddatabase, container, false);

        // Initialise the views
        initialiseViews();

        // Set the helper classes
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        indexSongs = new IndexSongs();
        songXML = new SongXML();
        chordProConvert = new ChordProConvert();
        onSongConvert = new OnSongConvert();
        textSongConvert = new TextSongConvert();
        usrConvert = new UsrConvert();
        processSong = new ProcessSong();

        // Decorate the popup
        Dialog dialog = getDialog();
        if (dialog != null && getActivity() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), dialog, preferences);
        }

        BuildIndex buildIndex = new BuildIndex();
        try {
            buildIndex.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return V;
    }

    private void initialiseViews() {
        simpleProgressBar = V.findViewById(R.id.simpleProgressBar);
        progressBar = V.findViewById(R.id.progressBar);
        progressText = V.findViewById(R.id.progressText);
        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getResources().getString(R.string.search_rebuild));
        closeMe = V.findViewById(R.id.closeMe);
        saveMe = V.findViewById(R.id.saveMe);
        closeMe.hide();
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mListener != null) {
                        mListener.refreshAll();
                    }
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        saveMe.hide();
    }

    public interface MyInterface {
        void refreshAll();
    }

    @SuppressLint("StaticFieldLeak")
    private class BuildIndex extends AsyncTask<Object, String, String> {

        int numSongs;
        int currentSongNum;
        String message;
        boolean settingProgressBarUp = false;

        @Override
        protected String doInBackground(Object... objects) {
/*
                    // TODO hopefully don't need this anymore - in fact, any of this file!

            try {
                message = getString(R.string.initialisesongs_start).replace("-", "").trim();
                publishProgress("setmessage");


                // Search for the user's songs
                try {
                    storageAccess.listSongs(getActivity(), preferences);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Show how many songs have been found and display this to the user
                // This will remain as until the current folder is build
                numSongs = FullscreenActivity.songIds.size();
                settingProgressBarUp = true;
                message = numSongs + " " + getString(R.string.processing) + "\n" + getString(R.string.wait);
                publishProgress("setupprogressbar");

                try {
                    indexSongs.initialiseIndexStuff();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Listener for conversion of files
                boolean hadtoconvert = false;
                for (currentSongNum = 0; currentSongNum < numSongs; currentSongNum++) {
                    currentSongName = FullscreenActivity.songIds.get(currentSongNum);
                    if (currentSongName.contains("OpenSong/Songs/")) {
                        currentSongName = currentSongName.substring(currentSongName.lastIndexOf("OpenSong/Songs/") + 15);
                    }
                    message = currentSongName + "\n(" + currentSongNum + "/" + numSongs + ")";
                    publishProgress(currentSongName);
                    boolean converted = indexSongs.doIndexThis(getActivity(), storageAccess, preferences, processSong,
                            songXML, chordProConvert, usrConvert, onSongConvert, textSongConvert, currentSongNum);
if (converted) {
                        message = "Converted song...";
                        publishProgress("setmessage");
                        hadtoconvert = true;
                    }


                }

                indexSongs.completeLog();

                // If we had to convert songs from OnSong, ChordPro, etc, we need to reindex to get sorted songs again
                // This is because the filename will likely have changed alphabetical position
                // Alert the user to the need for rebuilding and repeat the above
                if (hadtoconvert) {
                    message = "Updating indexes of converted songs...";
                    publishProgress("setmessage");

                    try {
                        indexSongs.initialiseIndexStuff();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Listener for conversion of files
                    for (currentSongNum = 0; currentSongNum < numSongs; currentSongNum++) {
                        currentSongName = FullscreenActivity.songIds.get(currentSongNum);
                        if (currentSongName.contains("OpenSong/Songs/")) {
                            currentSongName = currentSongName.substring(currentSongName.lastIndexOf("OpenSong/Songs/") + 15);
                        }
                        message = currentSongName + "\n(" + currentSongNum + "/" + numSongs + ")";
                        publishProgress(currentSongName);
                        indexSongs.doIndexThis(getActivity(), storageAccess, preferences, processSong, songXML,
                                chordProConvert, usrConvert, onSongConvert, textSongConvert, currentSongNum);
                    }
                    indexSongs.completeLog();
                }

                indexSongs.getSongDetailsFromIndex();

                // Finished indexing
                message = getString(R.string.success);
                publishProgress("setmessage");

            } catch (Exception e) {
                Log.d("PopUpRebuildDatabase", "User cancelled database - caught crash!");
            }
            */
            return null;
        }

        @Override
        protected void onProgressUpdate(String... string) {
            try {
                if (settingProgressBarUp) {
                    settingProgressBarUp = false;
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setMax(numSongs);
                }
                if (currentSongNum > 0) {
                    progressBar.setProgress(currentSongNum);
                }
                progressText.setText(message);
            } catch (Exception e) {
                Log.d("PopUpRebuildDatabase", "Crash caught while updating progress");
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                simpleProgressBar.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                closeMe.show();
                FullscreenActivity.needtorefreshsongmenu = false;
                if (mListener != null) {
                    mListener.refreshAll();
                }
                dismiss();
            } catch (Exception e) {
                Log.d("PopUpRebuildDatabase", "Crash caught while finishing indexing");
            }
        }
    }
}
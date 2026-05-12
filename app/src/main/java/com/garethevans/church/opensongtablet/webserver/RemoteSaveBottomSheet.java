package com.garethevans.church.opensongtablet.webserver;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.databinding.BottomSheetRemoteSaveBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class RemoteSaveBottomSheet extends BottomSheetCommon {

    private final String TAG = "RemoteSaveBottomSheet";
    private MainActivityInterface mainActivityInterface;
    private BottomSheetRemoteSaveBinding myView;
    private Song songToSave = null, songToCompare = null;
    private CurrentSet setToSave = null, setToCompare = null;
    private String song_doesnt_exist = "", sync_remote_new_item = "", sync_remote_update_item = "",
            sync_remote_replace_item = "", sync_remote_confirm_save = "";
    private boolean isNewSong = false, songNewer = false, songUuidMatches = false;

    RemoteSaveBottomSheet(Song songToSave, CurrentSet setToSave) {
        this.songToSave = songToSave;
        this.setToSave = setToSave;
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetRemoteSaveBinding.inflate(inflater, container, false);
        myView.dialogHeading.setClose(this);

        prepareStrings();

        setupViews();

        // Set up the listeners
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext() != null) {
            sync_remote_confirm_save = getContext().getString(R.string.sync_remote_confirm_save);
            song_doesnt_exist = getContext().getString(R.string.song_doesnt_exist);
            sync_remote_new_item = getContext().getString(R.string.sync_remote_new_item) + ".  " + sync_remote_confirm_save;
            sync_remote_update_item = getContext().getString(R.string.sync_remote_update_item) + ".  " + sync_remote_confirm_save;
            sync_remote_replace_item = getContext().getString(R.string.sync_remote_replace_item) + ".  " + sync_remote_confirm_save;
        }
    }

    private void setupViews() {
        // Hide the okButton until we have checked!
        myView.okButton.setVisibility(View.VISIBLE);
        myView.progressBar.setVisibility(View.VISIBLE);
        myView.songContent.setVisibility(View.GONE);
        // Do this on a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Decide if we are receiving a song or a set
            if (songToSave != null && songToSave.getFilename() != null && songToSave.getFolder() != null) {
                // Ok, we need to decide if the song exists already, or if it is a new song
                songToCompare = mainActivityInterface.getSQLiteHelper().getSpecificSong(songToSave.getFolder(), songToSave.getFilename());

                // Firstly, it the lyrics contains 'that song isn't on your device' it is a new song
                isNewSong = songToCompare.getLyrics().contains(song_doesnt_exist);
                songUuidMatches = !isNewSong && songToSave.getUuid() != null && songToSave.getUuid().isEmpty() && songToSave.getUuid().equals(songToCompare.getUuid());
                songNewer = false;

                Log.d(TAG, "songToSave folder:" + songToSave.getFolder() + "  filename:" + songToSave.getFilename() + "  lyrics:" + songToSave.getLyrics() + "  uuid:" + songToSave.getUuid() + "  lastMod:" + songToSave.getLastModified());
                Log.d(TAG, "songToCompare folder:" + songToCompare.getFolder() + "  filename:" + songToCompare.getFilename() + "  lyrics:" + songToCompare.getLyrics() + "  uuid:" + songToCompare.getUuid() + "  lastMod:" + songToCompare.getLastModified());
                if (songToSave.getLastModified() != null && songToCompare.getLastModified() != null) {
                    // Compare the time stamps
                    long songToSaveMillis = mainActivityInterface.getTimeTools().getMillisFromIsoTime(songToSave.getLastModified());
                    long songToCompareMillis = mainActivityInterface.getTimeTools().getMillisFromIsoTime(songToCompare.getLastModified());
                    songNewer = songUuidMatches && songToCompareMillis > songToSaveMillis;
                }

                Log.d(TAG, "isNewSong:" + isNewSong + "  songUuidMatches:" + songUuidMatches + "  songNewer:" + songNewer);

                // Now update on the UI
                mainActivityInterface.getMainHandler().post(() -> {
                    if (myView != null) {
                        myView.information.setText(isNewSong ? sync_remote_new_item : songNewer ? sync_remote_update_item : sync_remote_replace_item);
                        myView.folder.setHint(songToSave.getFolder());
                        myView.filename.setHint(songToSave.getFilename());
                        myView.lyrics.setHint(songToSave.getLyrics());
                        if (getContext() != null) {
                            myView.lyrics.setHintMonospace(getContext());
                        }
                        myView.okButton.setVisibility(View.VISIBLE);
                        myView.progressBar.setVisibility(View.GONE);
                        myView.songContent.setVisibility(View.VISIBLE);
                    }
                });


            } else if (setToSave != null) {
                // TODO nothing yet!
            } else {
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupListeners() {
        myView.okButton.setOnClickListener((buttonView) -> {
            myView.progressBar.setVisibility(View.VISIBLE);
            myView.songContent.setVisibility(View.GONE);

            // Do this on a new thread
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                if (isNewSong) {
                    Log.d(TAG, "SAVE THE NEW SONG");
                    // Save this to a new file (this method also creates the xml)
                    if (mainActivityInterface.getStorageAccess().writeSongFile(songToSave)) {
                        // The song was saved - now update the database
                        mainActivityInterface.getSQLiteHelper().createSong(songToSave.getFolder(), songToSave.getFilename());
                        mainActivityInterface.getSQLiteHelper().updateSong(songToSave);
                        // Now load the song
                        mainActivityInterface.getLoadSong().doLoadSong(songToSave, false);
                    }

                } else if (songNewer && songUuidMatches) {
                    // Update the song
                    songToSave.setSongXML(mainActivityInterface.getProcessSong().getXML(songToSave));
                    mainActivityInterface.getSaveSong().updateSong(songToSave, true);
                }
                mainActivityInterface.getSongMenuFragment().refreshSongList();
                mainActivityInterface.getMainHandler().post(() -> {
                    try {
                        dismiss();
                        mainActivityInterface.doSongLoad(songToSave.getFolder(),songToSave.getFilename(),true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        });
    }
}

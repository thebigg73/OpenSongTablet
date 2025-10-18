package com.garethevans.church.opensongtablet.openchords;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.databinding.BottomSheetOpenchordsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class OpenChordsBottomSheet extends BottomSheetCommon {

    private MainActivityInterface mainActivityInterface;
    private BottomSheetOpenchordsBinding myView;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "OpenChordsBottomSheet";
    private final String what;
    private final OpenChordsFragment openChordsFragment;
    private String download_title_string="", upload_title_string="", download_new_items_string="",
            upload_new_items_string="", update_local_items_string="", update_remote_items_string="",
            sync_items_not_on_local_string="", sync_items_not_on_remote_string="",
            sync_local_items_older_string="", sync_remote_items_older_string="",
            sync_song_downloaded_string="", sync_song_uploaded_string="",
            sync_song_update_downloaded_string="", sync_song_update_uploaded_string="",
            sync_set_downloaded_string="", sync_set_uploaded_string="",
            sync_set_update_downloaded_string="", sync_set_update_uploaded_string="",
            sync_delete_local_not_in_remote="", sync_delete_local_not_in_remote_info="",
            sync_delete_remote_not_in_local="", sync_delete_remote_not_in_local_info="";
    private Drawable upload_icon, download_icon;
    OpenChordsBottomSheet(OpenChordsFragment openChordsFragment, String what) {
        this.openChordsFragment = openChordsFragment;
        this.what = what;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = BottomSheetOpenchordsBinding.inflate(inflater, null, false);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        prepareStrings();

        setupViews();
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            download_title_string = getString(R.string.sync_download_from_openchords);
            upload_title_string = getString(R.string.sync_upload_to_openchords);
            upload_new_items_string = getString(R.string.sync_upload_new_items);
            download_new_items_string = getString(R.string.sync_download_new_items);
            update_local_items_string = getString(R.string.sync_update_local_items);
            update_remote_items_string = getString(R.string.sync_update_remote_items);
            sync_items_not_on_local_string = getString(R.string.sync_items_not_on_local);
            sync_items_not_on_remote_string = getString(R.string.sync_items_not_on_remote);
            sync_local_items_older_string = getString(R.string.sync_local_items_older);
            sync_remote_items_older_string = getString(R.string.sync_remote_items_older);
            upload_icon = AppCompatResources.getDrawable(getContext(),R.drawable.upload);
            download_icon = AppCompatResources.getDrawable(getContext(),R.drawable.download);
            sync_song_downloaded_string = getString(R.string.sync_last_download_new_songs);
            sync_song_update_downloaded_string = getString(R.string.sync_last_download_update_songs);
            sync_set_downloaded_string = getString(R.string.sync_last_download_new_sets);
            sync_set_update_downloaded_string = getString(R.string.sync_last_download_update_sets);

            sync_song_uploaded_string = getString(R.string.sync_last_upload_new_songs);
            sync_song_update_uploaded_string = getString(R.string.sync_last_upload_update_songs);
            sync_set_uploaded_string = getString(R.string.sync_last_upload_new_sets);
            sync_set_update_uploaded_string = getString(R.string.sync_last_upload_update_sets);
            sync_delete_local_not_in_remote = getString(R.string.sync_delete_local_not_in_remote);
            sync_delete_local_not_in_remote_info = getString(R.string.sync_delete_local_not_in_remote_info);
            sync_delete_remote_not_in_local = getString(R.string.sync_delete_remote_not_in_local);
            sync_delete_remote_not_in_local_info = getString(R.string.sync_delete_remote_not_in_local_info);
        }
    }
    private void setupViews() {
        String value;
        switch (what) {
            case "download":
                // Sort the title
                myView.dialogHeading.setText(download_title_string);

                // New songs on the server that need downloaded
                myView.newSongsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSongsNotOnLocalCount()>0 ? View.VISIBLE:View.GONE);
                myView.newSongs.setText(sync_items_not_on_local_string);
                value = mainActivityInterface.getOpenChordsAPI().getSongsNotOnLocalString() +
                        "\n\n" + sync_song_downloaded_string + ": " + mainActivityInterface.getOpenChordsAPI().getLastModified("lastDownloadNewSongs");
                myView.newSongs.setHint(value);
                myView.newSongsAction.setText(download_new_items_string);
                myView.newSongsAction.setIcon(download_icon);

                // Songs that need updated in the local folder
                myView.updateSongsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSongsOnLocalOlderCount()>0 ? View.VISIBLE:View.GONE);
                myView.updateSongs.setText(sync_local_items_older_string);
                value = mainActivityInterface.getOpenChordsAPI().getSongsOnLocalOlderString() +
                        "\n\n" + sync_song_update_downloaded_string + ": " + mainActivityInterface.getOpenChordsAPI().getLastModified("lastDownloadSongChanges");
                myView.updateSongs.setHint(value);
                myView.updateSongsAction.setText(update_local_items_string);

                // Songs that need deleted in the local folder because they aren't in the remote folder
                myView.deleteSongsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSongsNotOnServerCount()>0 ? View.VISIBLE:View.GONE);
                myView.songsToDelete.setText(sync_delete_local_not_in_remote_info);
                myView.songsToDelete.setHint(mainActivityInterface.getOpenChordsAPI().getSongsNotOnServerString());
                myView.deleteSongsAction.setText(sync_delete_local_not_in_remote);

                // New sets on the server that need downloaded
                myView.newSetsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSetListsNotOnLocalCount()>0 ? View.VISIBLE:View.GONE);
                myView.newSets.setText(sync_items_not_on_local_string);
                value = mainActivityInterface.getOpenChordsAPI().getSetListsNotOnLocalString() +
                        "\n\n" + sync_set_downloaded_string + ": " + mainActivityInterface.getOpenChordsAPI().getLastModified("lastDownloadNewSets");
                myView.newSets.setHint(value);
                myView.newSetsAction.setText(download_new_items_string);
                myView.newSetsAction.setIcon(download_icon);

                // Sets that need updated in the local folder
                myView.updateSetsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSetListsOnLocalOlderCount()>0 ? View.VISIBLE:View.GONE);
                myView.updateSets.setText(sync_local_items_older_string);
                value = mainActivityInterface.getOpenChordsAPI().getSetListsOnLocalOlderString() +
                        "\n\n" + sync_set_update_downloaded_string + ": " + mainActivityInterface.getOpenChordsAPI().getLastModified("lastDownloadSetChanges");
                myView.updateSets.setHint(value);
                myView.updateSetsAction.setText(update_local_items_string);

                // Sets that need deleted in the local folder because they aren't in the remote folder
                myView.deleteSetsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSetListsNotOnServerCount()>0 ? View.VISIBLE:View.GONE);
                myView.setsToDelete.setText(sync_delete_local_not_in_remote_info);
                myView.setsToDelete.setHint(mainActivityInterface.getOpenChordsAPI().getSetListsNotOnServerString());
                myView.deleteSetsAction.setText(sync_delete_local_not_in_remote);
                break;

            case "upload":
                // Sort the title
                myView.dialogHeading.setText(upload_title_string);

                // New songs in the local folder that need uploaded
                myView.newSongsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSongsNotOnServerCount()>0 ? View.VISIBLE:View.GONE);
                myView.newSongs.setText(sync_items_not_on_remote_string);
                value = mainActivityInterface.getOpenChordsAPI().getSongsNotOnServerString() +
                        "\n\n" + sync_song_uploaded_string + ": " + mainActivityInterface.getOpenChordsAPI().getLastModified("lastUploadNewSongs");
                myView.newSongs.setHint(value);
                myView.newSongsAction.setText(upload_new_items_string);
                myView.newSongsAction.setIcon(upload_icon);

                // Songs that need updated in the remote folder
                myView.updateSongsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSongsOnServerOlderCount()>0 ? View.VISIBLE:View.GONE);
                myView.updateSongs.setText(sync_remote_items_older_string);
                value = mainActivityInterface.getOpenChordsAPI().getSongsOnServerOlderString() +
                        "\n\n" + sync_song_update_uploaded_string + ": " + mainActivityInterface.getOpenChordsAPI().getLastModified("lastUploadSongChanges");
                myView.updateSongs.setHint(value);
                myView.updateSongsAction.setText(update_remote_items_string);

                // Songs that need deleted in the remote folder because they aren't in the local folder
                myView.deleteSongsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSongsNotOnLocalCount()>0 ? View.VISIBLE:View.GONE);
                myView.songsToDelete.setText(sync_delete_remote_not_in_local_info);
                myView.songsToDelete.setHint(mainActivityInterface.getOpenChordsAPI().getSongsNotOnLocalString());
                myView.deleteSongsAction.setText(sync_delete_remote_not_in_local);

                // New sets in the local folder that need uploads
                myView.newSetsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSetListsNotOnServerCount()>0 ? View.VISIBLE:View.GONE);
                myView.newSets.setText(sync_items_not_on_remote_string);
                value = mainActivityInterface.getOpenChordsAPI().getSetListsNotOnServerString() +
                        "\n\n" + sync_set_uploaded_string + ": " + mainActivityInterface.getOpenChordsAPI().getLastModified("lastUploadNewSets");
                myView.newSets.setHint(value);
                myView.newSetsAction.setText(upload_new_items_string);
                myView.newSetsAction.setIcon(upload_icon);

                // Sets that need updated in the remote folder
                myView.updateSetsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSetListsOnServerOlderCount()>0 ? View.VISIBLE:View.GONE);
                myView.updateSets.setText(sync_remote_items_older_string);
                value = mainActivityInterface.getOpenChordsAPI().getSetListsOnServerOlderString() +
                        "\n\n" + sync_set_update_uploaded_string + ": " + mainActivityInterface.getOpenChordsAPI().getLastModified("lastUploadSetChanges");
                myView.updateSets.setHint(value);
                myView.updateSetsAction.setText(update_remote_items_string);

                // Sets that need deleted in the remote folder because they aren't in the local folder
                myView.deleteSetsLayout.setVisibility(mainActivityInterface.getOpenChordsAPI().getSetListsNotOnLocalCount()>0 ? View.VISIBLE:View.GONE);
                myView.setsToDelete.setText(sync_delete_remote_not_in_local_info);
                myView.setsToDelete.setHint(mainActivityInterface.getOpenChordsAPI().getSetListsNotOnLocalString());
                myView.deleteSetsAction.setText(sync_delete_remote_not_in_local);
                break;
        }

        // Show the number of changes required if appropriate
        myView.songsNoChangesRequired.setVisibility(
                myView.newSongsLayout.getVisibility()==View.VISIBLE ||
                        myView.updateSongsLayout.getVisibility()==View.VISIBLE ?
                        View.GONE:View.VISIBLE);
        myView.setsNoChangesRequired.setVisibility(
                myView.newSetsLayout.getVisibility()==View.VISIBLE ||
                        myView.updateSetsLayout.getVisibility()==View.VISIBLE ?
                        View.GONE:View.VISIBLE);

    }

    private void setListeners() {
        switch (what) {
            case "download":
                myView.newSongsAction.setOnClickListener((v) ->    prepareDownload(true,false,false,false));
                myView.updateSongsAction.setOnClickListener((v) -> prepareDownload(false,true,false,false));
                myView.newSetsAction.setOnClickListener((v) ->     prepareDownload(false,false,true,false));
                myView.updateSetsAction.setOnClickListener((v) ->  prepareDownload(false,false,false,true));
                myView.deleteSongsAction.setOnClickListener((v) -> deleteLocalSongs());
                myView.deleteSetsAction.setOnClickListener((v) -> deleteLocalSets());
                break;

            case "upload":
                myView.newSongsAction.setOnClickListener((v) ->    prepareUpload(true,false,false,false));
                myView.updateSongsAction.setOnClickListener((v) -> prepareUpload(false,true,false,false));
                myView.newSetsAction.setOnClickListener((v) ->     prepareUpload(false,false,true,false));
                myView.updateSetsAction.setOnClickListener((v) ->  prepareUpload(false,false,false,true));
                myView.deleteSongsAction.setOnClickListener((v) -> deleteRemoteSongs());
                myView.deleteSetsAction.setOnClickListener((v) -> deleteRemoteSets());
                break;
        }
    }

    private void prepareDownload(boolean newSongs, boolean updateSongs, boolean newSets, boolean updateSets) {
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (openChordsFragment!=null) {
            try {
                openChordsFragment.prepareDownload(newSongs,updateSongs,newSets,updateSets);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void prepareUpload(boolean newSongs, boolean updateSongs, boolean newSets, boolean updateSets) {
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (openChordsFragment!=null) {
            try {
                openChordsFragment.prepareUpload(newSongs,updateSongs,newSets,updateSets);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void deleteLocalSongs() {
        // Delete local songs that aren't in the remote folder
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (openChordsFragment!=null) {
            try {
                openChordsFragment.deleteLocalSongs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void deleteLocalSets() {
        // Delete local sets that aren't in the remote folder
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (openChordsFragment!=null) {
            try {
                openChordsFragment.deleteLocalSets();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void deleteRemoteSongs() {
        // Delete remote songs that aren't in the local folder folder
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (openChordsFragment!=null) {
            try {
                openChordsFragment.deleteRemoteSongs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void deleteRemoteSets() {
        // Delete remote sets that aren't in the local folder folder
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (openChordsFragment!=null) {
            try {
                openChordsFragment.deleteRemoteSets();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

}

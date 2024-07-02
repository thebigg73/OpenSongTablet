package com.garethevans.church.opensongtablet.songmenu;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetMenuSongsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textview.MaterialTextView;

public class SongMenuBottomSheet extends BottomSheetDialogFragment {

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "SongMenuBottomSheet";
    private BottomSheetMenuSongsBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String file_string="", deeplink_export_string="", deeplink_edit_string="",
            deeplink_song_actions_string="", deeplink_import_string="", search_index_wait_string="",
            added_to_set_string="", variation_string="", index_rebuild_string="", quick_string="", full_string="";

    private final String songTitle;

    public SongMenuBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        songTitle = "";
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    SongMenuBottomSheet(String songTitle) {
        this.songTitle = songTitle;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetMenuSongsBinding.inflate(inflater, container, false);

        prepareStrings();

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        // Set up the views
        setupViews();
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            file_string = getString(R.string.file);
            deeplink_export_string = getString(R.string.deeplink_export);
            deeplink_edit_string = getString(R.string.deeplink_edit);
            deeplink_song_actions_string = getString(R.string.deeplink_song_actions);
            deeplink_import_string = getString(R.string.deeplink_import);
            search_index_wait_string = getString(R.string.index_songs_wait);
            added_to_set_string = getString(R.string.added_to_set);
            variation_string = getString(R.string.variation);
            index_rebuild_string = getString(R.string.index_songs_rebuild);
            quick_string = getString(R.string.index_songs_quick);
            full_string = getString(R.string.index_songs_full);
        }
    }
    private void setupViews() {
        // Set up the song title
        if (!mainActivityInterface.getProcessSong().isValidSong(mainActivityInterface.getSong())) {
            myView.songSpecificActions.setVisibility(View.GONE);
            myView.otherOptions.setVisibility(View.GONE);
        } else {
            myView.songSpecificActions.setVisibility(View.VISIBLE);
            myView.otherOptions.setVisibility(View.VISIBLE);
            myView.songTitle.setHint(file_string+": "+songTitle);
        }
        // Check we have songs in the menu
        if (!mainActivityInterface.getSongsFound("song").isEmpty()) {
            myView.randomSong.setVisibility(View.VISIBLE);
        } else {
            myView.randomSong.setVisibility(View.GONE);
        }
        String text = index_rebuild_string + " (" + quick_string + ")";
        myView.rebuildIndexQuick.setText(text);
        text = index_rebuild_string + " (" + full_string + ")";
        myView.rebuildIndexFull.setText(text);
    }

    private void setListeners() {
        // Listener for buttons
        myView.songLoad.setOnClickListener(v -> {
            mainActivityInterface.doSongLoad(mainActivityInterface.getSong().getFolder(),
                    mainActivityInterface.getSong().getFilename(),true);
            dismiss();
        });
        myView.songShare.setOnClickListener(v -> navigateTo(deeplink_export_string));
        myView.songEdit.setOnClickListener(v -> navigateTo(deeplink_edit_string));
        myView.songActions.setOnClickListener(v -> navigateTo(deeplink_song_actions_string));
        myView.newSongs.setOnClickListener(v -> navigateTo(deeplink_import_string));
        myView.addToSet.setOnClickListener(v -> addToSet());
        myView.addVariationToSet.setOnClickListener(v -> addVariationToSet());
        myView.randomSong.setOnClickListener(v -> {
            if (getActivity()!=null) {
                RandomSongBottomSheet randomSongBottomSheet = new RandomSongBottomSheet("song");
                randomSongBottomSheet.show(getActivity().getSupportFragmentManager(), "RandomBottomSheet");
                dismiss();
            }
        });
        myView.rebuildIndexFull.setOnClickListener(v -> {
            if (mainActivityInterface.getSongListBuildIndex().getIndexComplete()) {
                // Make this a complete rebuild of the database, rather than an update scan
                mainActivityInterface.getStorageAccess().setDatabaseLastUpdate(0);
                mainActivityInterface.getSQLiteHelper().resetDatabase();
                mainActivityInterface.getSongListBuildIndex().setFullIndexRequired(true);
                mainActivityInterface.getSongListBuildIndex().setIndexRequired(true);
                mainActivityInterface.getPreferences().setMyPreferenceBoolean("indexSkipAllowed",false);
                mainActivityInterface.getSongListBuildIndex().buildBasicFromFiles();
                mainActivityInterface.indexSongs();
                dismiss();
            } else {
                dismiss();
                String progressText = "";
                if (mainActivityInterface.getSongMenuFragment()!=null) {
                    MaterialTextView progressView = mainActivityInterface.getSongMenuFragment().getProgressText();
                    if (progressView!=null && progressView.getText()!=null) {
                        progressText = " " + progressView.getText().toString();
                    }
                }
                mainActivityInterface.getShowToast().doItBottomSheet(search_index_wait_string+progressText,myView.getRoot());
            }
        });
        myView.rebuildIndexQuick.setOnClickListener(v -> {
            if (mainActivityInterface.getSongListBuildIndex().getIndexComplete()) {
                // Make this a complete rebuild of the database, rather than an update scan
                mainActivityInterface.getSongListBuildIndex().setFullIndexRequired(false);
                mainActivityInterface.getSongListBuildIndex().setIndexRequired(true);
                mainActivityInterface.indexSongs();
                dismiss();
            } else {
                dismiss();
                String progressText = "";
                if (mainActivityInterface.getSongMenuFragment()!=null) {
                    MaterialTextView progressView = mainActivityInterface.getSongMenuFragment().getProgressText();
                    if (progressView!=null && progressView.getText()!=null) {
                        progressText = " " + progressView.getText().toString();
                    }
                }
                mainActivityInterface.getShowToast().doItBottomSheet(search_index_wait_string+progressText,myView.getRoot());
            }
        });
    }

    private void navigateTo(String deepLink) {
        mainActivityInterface.closeDrawer(true);
        if (deepLink!=null) {
            mainActivityInterface.navigateToFragment(deepLink, 0);
        }
        dismiss();
    }

    private void addToSet() {
        // For a received song (which is about to become a variation) use the stored received song filename
        if (mainActivityInterface.getSong().getFilename().equals("ReceivedSong")) {
            mainActivityInterface.getSong().setFilename(mainActivityInterface.getSong().getTitle());
        }

        // Add the song to the current set
        addToCurrentSet();

        // Let the user know and close
        alertSuccess(mainActivityInterface.getSong().getFilename() + " " + added_to_set_string);
    }

    private void addVariationToSet() {
        // For a received song (which is about to become a variation) use the stored received song filename
        if (mainActivityInterface.getSong().getFilename().equals("ReceivedSong")) {
            mainActivityInterface.getSong().setFilename(mainActivityInterface.getSong().getTitle());
        }

        // Add the song to the current set
        addToCurrentSet();

        // Now change it to a variation
        int position = mainActivityInterface.getCurrentSet().getCurrentSetSize() - 1;
        if (position>=0) {
            mainActivityInterface.getSetActions().makeVariation(position);
        }

        // Let the user know and close
        alertSuccess(variation_string + " " +
                mainActivityInterface.getSong().getFilename() + " " + added_to_set_string);
    }

    private void addToCurrentSet() {
        mainActivityInterface.getCurrentSet().addItemToSet(mainActivityInterface.getSong());
    }

    private void alertSuccess(String message) {
        mainActivityInterface.getShowToast().doItBottomSheet(message,myView.getRoot());

        // Now send the call to update the set menu fragment
        //mainActivityInterface.updateSetList();

        dismiss();
    }
}
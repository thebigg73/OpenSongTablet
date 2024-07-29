package com.garethevans.church.opensongtablet.utilities;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.databinding.BottomSheetDatabaseCleanBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class CleanDatabaseBottomSheet extends BottomSheetDialogFragment {

    private MainActivityInterface mainActivityInterface;
    private BottomSheetDatabaseCleanBinding myView;
    private boolean updateSongMenuRequired = false;

    private final ArrayList<Song> uselessSongs, usefulSongs;

    public CleanDatabaseBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        usefulSongs = null;
        uselessSongs = null;
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CleanDatabaseBottomSheet(ArrayList<Song> uselessSongs, ArrayList<Song> usefulSongs) {
        this.usefulSongs = usefulSongs;
        this.uselessSongs = uselessSongs;
    }

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
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetDatabaseCleanBinding.inflate(inflater,container,false);
        myView.dialogHeading.setClose(this);

        setupViews();

        // Set up the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        // Show the useless songs
        StringBuilder uselessSongList = new StringBuilder();
        boolean hasUselessSongs = true;
        if (uselessSongs!=null) {
            for (Song uselessSong : uselessSongs) {
                uselessSongList.append(uselessSong.getFolder()).append("/").append(uselessSong.getFilename()).append("\n");
            }
        }
        if (uselessSongList.toString().isEmpty()) {
            // Hide the stuff we don't need
            myView.uselessSongInfo.setVisibility(View.GONE);
            myView.uselessSongDelete.setVisibility(View.GONE);
            myView.uselessSongSeparator.setVisibility(View.GONE);
            hasUselessSongs = false;
        } else {
            myView.uselessSongInfo.setHint(uselessSongList.toString().trim());
        }

        // Show the useful songs
        StringBuilder usefulSongList = new StringBuilder();
        boolean hasUsefulSongs = true;
        if (uselessSongs!=null) {
            for (Song usefulSong : usefulSongs) {
                usefulSongList.append(usefulSong.getFolder()).append("/").append(usefulSong.getFilename()).append("\n");
            }
        }
        if (usefulSongList.toString().isEmpty()) {
            // Hide the stuff we don't need
            myView.usefulSongInfo.setVisibility(View.GONE);
            myView.usefulSongDelete.setVisibility(View.GONE);
            myView.usefulSongSeparator.setVisibility(View.GONE);
            hasUsefulSongs = false;
        } else {
            myView.usefulSongInfo.setHint(usefulSongList.toString().trim());
        }

        if (!hasUsefulSongs && !hasUselessSongs) {
            // Show the congratulations message
            myView.congratulations.setVisibility(View.VISIBLE);
        }
    }

    private void setListeners() {
        myView.uselessSongDelete.setOnClickListener(view -> {
            // Do this on a new thread (no warning needed)
            myView.uselessSongDelete.setEnabled(false);
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                mainActivityInterface.getNonOpenSongSQLiteHelper().removeUselessEntries(uselessSongs,true,CleanDatabaseBottomSheet.this);
                mainActivityInterface.getMainHandler().post(() -> {
                    if (myView!=null) {
                        myView.uselessSongDelete.setEnabled(true);
                    }
                });
            });
        });
        myView.usefulSongDelete.setOnClickListener(view -> {
            // Do this on a new thread (no warning needed)
            myView.usefulSongDelete.setEnabled(false);
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                mainActivityInterface.getNonOpenSongSQLiteHelper().removeUselessEntries(usefulSongs,false,CleanDatabaseBottomSheet.this);
                mainActivityInterface.getMainHandler().post(() -> {
                    if (myView!=null) {
                        myView.usefulSongDelete.setEnabled(true);
                    }
                });
            });
        });
    }

    // If we clear the useless songs, update the view here
    public void clearUseless() {
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView!=null) {
                myView.uselessSongInfo.setVisibility(View.GONE);
                myView.uselessSongDelete.setVisibility(View.GONE);
                myView.uselessSongSeparator.setVisibility(View.GONE);
                myView.cleanDBScrollView.setScrollY(0);

                uselessSongs.clear();
                checkForCongratulations();
            }
            updateSongMenuRequired = true;
        });
    }

    // If we clear the useless songs, update the view here
    public void clearUseful() {
        mainActivityInterface.getMainHandler().post(() -> {
            if (myView!=null) {
                myView.usefulSongInfo.setVisibility(View.GONE);
                myView.usefulSongDelete.setVisibility(View.GONE);
                myView.usefulSongSeparator.setVisibility(View.GONE);
                myView.cleanDBScrollView.setScrollY(0);

                usefulSongs.clear();
                checkForCongratulations();
            }
            updateSongMenuRequired = true;
        });
    }

    private void checkForCongratulations() {
        // If we have cleared useless and useful songs, show congratulations
        if (uselessSongs.isEmpty() && usefulSongs.isEmpty()) {
            myView.congratulations.setVisibility(View.VISIBLE);
        }
        myView.cleanDBScrollView.post(() -> {
            if (myView!=null) {
                myView.cleanDBScrollView.fullScroll(View.FOCUS_UP);
                myView.cleanDBScrollView.scrollTo(0,0);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
        // Update the song menu if required on close
        if (updateSongMenuRequired && mainActivityInterface!=null) {
            mainActivityInterface.getSongListBuildIndex().setIndexRequired(true);
            mainActivityInterface.getSongListBuildIndex().setFullIndexRequired(true);
            mainActivityInterface.updateSongMenu(null,null,null);
        }
    }
}

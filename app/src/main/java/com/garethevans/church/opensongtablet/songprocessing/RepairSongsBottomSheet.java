package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.databinding.BottomSheetRepairSongsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class RepairSongsBottomSheet extends BottomSheetCommon {

    private BottomSheetRepairSongsBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String repair_songs_found="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetRepairSongsBinding.inflate(inflater,container,false);
        prepareStrings();
        setupViews();
        setupListeners();

        return myView.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            repair_songs_found = getString(R.string.repair_songs_found);
        }
    }

    private void setupViews() {
        ArrayList<Song> songsToFix = mainActivityInterface.getLoadSong().getSongsToFix();
        if (songsToFix!=null) {
            String found = repair_songs_found + ": " + songsToFix.size();
            myView.songsCount.setText(found);

            StringBuilder stringBuilder = new StringBuilder();
            for (int i=0; i<songsToFix.size(); i++) {
                stringBuilder.append(songsToFix.get(i).getFolder())
                        .append("/").append(songsToFix.get(i).getFilename()).append("\n");
            }
            myView.songsFound.setText(stringBuilder.toString());
        }
    }

    private void setupListeners() {
        myView.dialogHeading.setClose(this);
        myView.okButton.setOnClickListener((button) -> {
            if (getContext()!=null) {
                if (mainActivityInterface == null) {
                    mainActivityInterface = (MainActivityInterface) getContext();
                }
                dismiss();
                // Now do the task on a background thread
                mainActivityInterface.getThreadPoolExecutor().execute(() -> mainActivityInterface.getLoadSong().fixSongs());
            }
        });
    }
}

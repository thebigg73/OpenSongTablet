package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.MenuSongsContextDialogBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

class SongMenuContextDialog extends DialogFragment {

    private String folder;
    private String song;

    SongMenuContextDialog (String folder, String song) {
        this.folder = folder;
        this.song = song;
    }
    MenuSongsContextDialogBinding myView;
    MainActivityInterface mainActivityInterface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        dismiss();
        mainActivityInterface.songMenuActionButtonShow(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = MenuSongsContextDialogBinding.inflate(inflater, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        SongForSet songForSet = new SongForSet();
        Preferences preferences = new Preferences();
        SetActions setActions = new SetActions();

        String songchosen = requireContext().getResources().getString(R.string.song) + ": " +
                song;
        myView.songSelected.setText(songchosen);
        myView.editButton.setOnClickListener(new ActionClickListener(R.id.nav_editSong,"editSong"));
        myView.duplicateButton.setOnClickListener(new ActionClickListener(0, "duplicateSong"));
        myView.deleteButton.setOnClickListener(new ActionClickListener(0, "deleteSong"));
        myView.setButton.setOnClickListener(v -> {
            songForSet.addToSet(requireContext(),preferences,setActions,StaticVariables.whichSongFolder,StaticVariables.songfilename);
            mainActivityInterface.refreshSetList();
            dismiss();
        });
        myView.exportButton.setOnClickListener(new ActionClickListener(0,"exportSong"));
        return myView.getRoot();
    }

    private class ActionClickListener implements View.OnClickListener {

        final int id;
        final String str;
        ActionClickListener(int id, String str) {
            this.id = id;
            this.str = str;
        }
        @Override
        public void onClick(View v) {
            switch (str) {
                case "deleteSong":
                    String str = getString(R.string.delete) + ": " +
                            StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;
                    mainActivityInterface.displayAreYouSure("deleteSong", str,null, "SongMenuFragment",null);
                    break;

                case "editSong":
                    StaticVariables.whichSongFolder = folder;
                    StaticVariables.songfilename = song;
                    mainActivityInterface.navigateToFragment(id);
                    break;

                default:
                    mainActivityInterface.navigateToFragment(id);
            }
            dismiss();
        }
    }
}

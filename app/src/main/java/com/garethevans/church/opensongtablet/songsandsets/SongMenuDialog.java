package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.MenuSongsDialogBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

public class SongMenuDialog extends DialogFragment {

    MenuSongsDialogBinding myView;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = MenuSongsDialogBinding.inflate(inflater,container,false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setGravity(Gravity.BOTTOM|Gravity.END);

        myView.createButton.setOnClickListener(new ActionClickListener(0,"createSong"));
        myView.manageStorage.setOnClickListener(new ActionClickListener(0,"manageStorage"));
        myView.importButton.setOnClickListener(new ActionClickListener(0,"importSong"));

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
                case "createSong":
                    // This opens the edit page, but appends '*NEWSONG* to the current song
                    // This will be looked for before trying to load.  This way we keep a reference to the current song
                    // should the user cancel the create process.
                    StaticVariables.songfilename += "*NEWSONG*";
                    mainActivityInterface.navigateToFragment(R.id.nav_editSong);
                    break;

                case "manageStorage":
                    // This will open the storage overview.  This allows creating, deleting, renaming folders and file management.
                    //mainActivityInterface.navigateToFragment(R.id.nav_songStorage);
                    mainActivityInterface.navigateToFragment(R.id.nav_storageManagement);
                    break;

                case "importSong":
                    // This will give the option of importing/downloading my songs, finding a file or downloading from an online service.
                    // mainActivityInterface.navigateToFragment(R.id.importSongs);
                    break;
            }
            dismiss();
        }
    }
}
package com.garethevans.church.opensongtablet.filemanagement;

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
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.StorageFolderDialogBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class FolderManagementDialog extends DialogFragment {

    StorageFolderDialogBinding myView;
    MainActivityInterface mainActivityInterface;
    boolean root, songs;
    String subdir;
    Fragment callingFragment;

    FolderManagementDialog(Fragment callingFragment, boolean root, boolean songs, String subdir) {
        this.callingFragment = callingFragment;
        this.root = root;
        this.songs = songs;
        this.subdir = subdir;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        mainActivityInterface.songMenuActionButtonShow(true);
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = StorageFolderDialogBinding.inflate(inflater,container,false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (root) {
            myView.backupFolder.setVisibility(View.GONE);
            myView.createSubdirectory.setVisibility(View.GONE);
            myView.moveContents.setVisibility(View.GONE);
            myView.renameFolder.setVisibility(View.GONE);
            myView.deleteSubdirectory.setVisibility(View.GONE);
            myView.changeLocation.setOnClickListener(new ActionClickListener("resetStorage", R.id.nav_storage));
        } else if (songs) {
            myView.changeLocation.setVisibility(View.GONE);
            myView.renameFolder.setVisibility(View.GONE);
            myView.moveContents.setVisibility(View.GONE);
            myView.deleteSubdirectory.setVisibility(View.GONE);
        } else {
            myView.changeLocation.setVisibility(View.GONE);
            myView.backupFolder.setVisibility(View.GONE);
            //myView.backupFolder.setOnClickListener(new ActionClickListener(R.id.nav_storage));
            //myView.createSubdirectory.setOnClickListener(new ActionClickListener(R.id.nav_storage));
            //myView.moveContents.setOnClickListener(new ActionClickListener(R.id.nav_storage));
            //myView.renameFolder.setOnClickListener(new ActionClickListener(R.id.nav_storage));
            myView.deleteSubdirectory.setOnClickListener(new ActionClickListener("deleteItem", 0));
        }

        return myView.getRoot();
    }

    private class ActionClickListener implements View.OnClickListener {
        String what;
        int id;
        String action;
        ArrayList<String> arguments;

        ActionClickListener(String what, int id) {

            this.what = what;
            this.id = id;
        }
        @Override
        public void onClick(View v) {
            switch (what) {
                case "resetStorage":
                    mainActivityInterface.navigateToFragment(id);
                    break;

                case "deleteItem":
                    action = getActivity().getResources().getString(R.string.delete) + ": " + "OpenSong/Songs/"+ subdir + "\n" +
                        getActivity().getResources().getString(R.string.delete_folder_warning);
                    arguments = new ArrayList<>();
                    arguments.add("Songs");
                    arguments.add(subdir);
                    arguments.add("");
                    mainActivityInterface.displayAreYouSure(what, action, arguments);
                    mainActivityInterface.updateFragment("StorageManagementFragment",callingFragment);
                    break;
            }

            dismiss();
        }
    }
}

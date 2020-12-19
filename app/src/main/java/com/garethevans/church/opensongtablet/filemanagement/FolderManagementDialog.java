package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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
import com.garethevans.church.opensongtablet.preferences.Preferences;

import java.util.ArrayList;

public class FolderManagementDialog extends DialogFragment {

    StorageFolderDialogBinding myView;
    MainActivityInterface mainActivityInterface;
    boolean root, songs;
    String subdir;
    Fragment callingFragment;
    StorageAccess storageAccess;
    Preferences preferences;

    FolderManagementDialog(Fragment callingFragment, boolean root, boolean songs, String subdir) {
        this.callingFragment = callingFragment;
        this.root = root;
        this.songs = songs;
        if (songs || root) {
            subdir = "";  // Songs is passed as the main folder location, so don't need this as well
        }
        this.subdir = subdir;
        Log.d("d","root="+root+"   songs="+songs+"   subdir="+subdir);
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

        storageAccess = new StorageAccess();
        preferences = new Preferences();

        if (root) {
            myView.currentLocation.setText(storageAccess.niceUriTree(getActivity(),preferences,storageAccess.homeFolder(getActivity(),null,preferences))[1]);
            myView.backupFolder.setVisibility(View.GONE);
            myView.createSubdirectory.setVisibility(View.GONE);
            myView.moveContents.setVisibility(View.GONE);
            myView.renameFolder.setVisibility(View.GONE);
            myView.deleteSubdirectory.setVisibility(View.GONE);
            myView.changeLocation.setOnClickListener(new ActionClickListener("resetStorage", R.id.nav_storage));
        } else if (songs) {
            String s = "OpenSong/Songs";
            myView.currentLocation.setText(s);
            myView.changeLocation.setVisibility(View.GONE);
            myView.renameFolder.setVisibility(View.GONE);
            myView.moveContents.setVisibility(View.GONE);
            myView.deleteSubdirectory.setVisibility(View.GONE);
            myView.createSubdirectory.setOnClickListener(new ActionClickListener("createItem", 0));
            //myView.backupFolder.setOnClickListener(new ActionClickListener(R.id.nav_storage));
        } else {
            String s = "OpenSong/Songs/" + subdir;
            myView.currentLocation.setText(s);
            myView.changeLocation.setVisibility(View.GONE);
            myView.backupFolder.setVisibility(View.GONE);
            myView.createSubdirectory.setOnClickListener(new ActionClickListener("createItem", 0));
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
                    mainActivityInterface.displayAreYouSure(what, action, arguments, "StorageManagementFragment", callingFragment,null);
                    break;

                case "createItem":
                    DialogFragment dialogFragment = new NewNameDialog(callingFragment,"StorageManagementFragment",false,"Songs",subdir,null);
                    dialogFragment.show(getActivity().getSupportFragmentManager(),"createItem");
            }

            dismiss();
        }
    }


}

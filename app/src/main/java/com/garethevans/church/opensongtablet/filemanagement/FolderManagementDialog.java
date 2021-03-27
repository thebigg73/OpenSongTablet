package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.StorageFolderDialogBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class FolderManagementDialog extends DialogFragment {

    private MainActivityInterface mainActivityInterface;
    private final boolean root;
    private final boolean songs;
    private final String subdir;
    private final Fragment callingFragment;

    FolderManagementDialog(Fragment callingFragment, boolean root, boolean songs, String subdir) {
        this.callingFragment = callingFragment;
        this.root = root;
        this.songs = songs;
        if (songs || root) {
            subdir = "";  // Songs is passed as the main folder location, so don't need this as well
        }
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
        com.garethevans.church.opensongtablet.databinding.StorageFolderDialogBinding myView = StorageFolderDialogBinding.inflate(inflater, container, false);
        Window w = requireDialog().getWindow();
        if (w!=null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        StorageAccess storageAccess = mainActivityInterface.getStorageAccess();
        Preferences preferences = mainActivityInterface.getPreferences();

        ((FloatingActionButton)myView.dialogHeading.findViewById(R.id.close)).setOnClickListener(b -> dismiss());
        if (root) {
            ((TextView)myView.dialogHeading.findViewById(R.id.title)).setText(storageAccess.niceUriTree(getContext(), preferences, storageAccess.homeFolder(getContext(),null, preferences))[1]);
            myView.backupFolder.setVisibility(View.GONE);
            myView.createSubdirectory.setVisibility(View.GONE);
            myView.moveContents.setVisibility(View.GONE);
            myView.renameFolder.setVisibility(View.GONE);
            myView.deleteSubdirectory.setVisibility(View.GONE);
            myView.changeLocation.setOnClickListener(new ActionClickListener("resetStorage", R.id.setStorageLocationFragment));
        } else if (songs) {
            String s = "OpenSong/Songs";
            ((TextView)myView.dialogHeading.findViewById(R.id.title)).setText(s);
            myView.changeLocation.setVisibility(View.GONE);
            myView.renameFolder.setVisibility(View.GONE);
            myView.moveContents.setOnClickListener(new ActionClickListener("moveContents",0));
            myView.deleteSubdirectory.setVisibility(View.GONE);
            myView.createSubdirectory.setOnClickListener(new ActionClickListener("createItem", 0));
            myView.backupFolder.setOnClickListener(new ActionClickListener("backupOSB", 0));
        } else {
            String s = "OpenSong/Songs/" + subdir;
            ((TextView)myView.dialogHeading.findViewById(R.id.title)).setText(s);
            myView.changeLocation.setVisibility(View.GONE);
            myView.backupFolder.setVisibility(View.GONE);
            myView.createSubdirectory.setOnClickListener(new ActionClickListener("createItem", 0));
            myView.moveContents.setOnClickListener(new ActionClickListener("moveContents",0));
            myView.renameFolder.setOnClickListener(new ActionClickListener("renameFolder",0));
            myView.deleteSubdirectory.setOnClickListener(new ActionClickListener("deleteItem", 0));
        }

        return myView.getRoot();
    }

    private class ActionClickListener implements View.OnClickListener {
        String what;
        int id;
        String action;
        ArrayList<String> arguments;
        DialogFragment dialogFragment;


        ActionClickListener(String what, int id) {
            this.what = what;
            this.id = id;
        }
        @Override
        public void onClick(View v) {
            switch (what) {
                case "resetStorage":
                    NavOptions navOptions = new NavOptions.Builder()
                            .setPopUpTo(R.id.setStorageLocationFragment, true)
                            .build();
                    NavHostFragment.findNavController(callingFragment)
                            .navigate(R.id.setStorageLocationFragment,null,navOptions);
                    break;

                case "backupOSB":
                    NavHostFragment.findNavController(callingFragment)
                            .navigate(R.id.backupOSBFragment,null,null);
                    break;

                case "deleteItem":
                    action = getString(R.string.delete) + ": " + "OpenSong/Songs/" + subdir + "\n" + getString(R.string.delete_folder_warning);
                    arguments = new ArrayList<>();
                    arguments.add("Songs");
                    arguments.add(subdir);
                    arguments.add("");
                    mainActivityInterface.displayAreYouSure(what, action, arguments, "StorageManagementFragment", callingFragment,null);
                    break;

                case "createItem":
                    dialogFragment = new NewNameDialog(callingFragment,"StorageManagementFragment",false,"Songs",subdir,null, false);
                    dialogFragment.show(requireActivity().getSupportFragmentManager(),"createItem");
                    break;

                case "renameFolder":
                    dialogFragment = new NewNameDialog(callingFragment,"StorageManagementFragment",false,"Songs",subdir,null, true);
                    dialogFragment.show(requireActivity().getSupportFragmentManager(),"renameFolder");
                    break;

                case "moveContents":
                    Bundle bundle = new Bundle();
                    bundle.putString("subdir", subdir);
                    NavHostFragment.findNavController(callingFragment)
                            .navigate(R.id.moveContentFragment,bundle,null);
                    break;
            }

            dismiss();
        }
    }


}

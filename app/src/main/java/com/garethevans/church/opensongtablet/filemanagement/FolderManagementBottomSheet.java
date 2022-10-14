package com.garethevans.church.opensongtablet.filemanagement;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetStorageFolderBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class FolderManagementBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetStorageFolderBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final boolean root, songs;
    private final String subdir;
    private final Fragment callingFragment;

    FolderManagementBottomSheet(Fragment callingFragment, boolean root, boolean songs, String subdir) {
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
        dismiss();
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
        myView = BottomSheetStorageFolderBinding.inflate(inflater, container, false);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        setupView();

        return myView.getRoot();
    }

    private void setupView() {
        myView.backupFolder.setVisibility(View.VISIBLE);
        myView.backupFolder.setOnClickListener(new ActionClickListener("backupOSB"));

        if (root) {
            myView.dialogHeading.setText(mainActivityInterface.getStorageAccess().
                    niceUriTree(mainActivityInterface.getStorageAccess().homeFolder(null))[1]);
            myView.createSubdirectory.setVisibility(View.GONE);
            myView.moveContents.setVisibility(View.GONE);
            myView.renameFolder.setVisibility(View.GONE);
            myView.deleteSubdirectory.setVisibility(View.GONE);
            myView.changeLocation.setOnClickListener(new ActionClickListener("resetStorage"));
            myView.exportSongList.setVisibility(View.VISIBLE);
        } else if (songs) {
            String s = "OpenSong/Songs";
            myView.dialogHeading.setText(s);
            myView.changeLocation.setVisibility(View.GONE);
            myView.renameFolder.setVisibility(View.GONE);
            myView.moveContents.setOnClickListener(new ActionClickListener("moveContents"));
            myView.deleteSubdirectory.setVisibility(View.GONE);
            myView.createSubdirectory.setOnClickListener(new ActionClickListener("createItem"));
            myView.exportSongList.setVisibility(View.VISIBLE);
            myView.exportSongList.setOnClickListener(v -> {
                ExportSongListBottomSheet exportSongListBottomSheet = new ExportSongListBottomSheet();
                exportSongListBottomSheet.show(requireActivity().getSupportFragmentManager(),"ExportSongListBottomSheet");
                dismiss();
            });

        } else {
            String s = "OpenSong/Songs/" + subdir;
            myView.dialogHeading.setText(s);
            myView.changeLocation.setVisibility(View.GONE);
            myView.createSubdirectory.setOnClickListener(new ActionClickListener("createItem"));
            myView.moveContents.setOnClickListener(new ActionClickListener("moveContents"));
            myView.renameFolder.setOnClickListener(new ActionClickListener("renameFolder"));
            myView.deleteSubdirectory.setOnClickListener(new ActionClickListener("deleteItem"));
            myView.exportSongList.setVisibility(View.VISIBLE);
        }
    }

    private class ActionClickListener implements View.OnClickListener {
        private final String what;

        private ActionClickListener(String what) {
            this.what = what;
        }
        @Override
        public void onClick(View v) {
            switch (what) {
                case "resetStorage":
                    mainActivityInterface.setWhattodo("storageOk");
                    mainActivityInterface.navigateToFragment(getString(R.string.deeplink_set_storage),0);
                    break;

                case "backupOSB":
                    mainActivityInterface.navigateToFragment(getString(R.string.deeplink_backup),0);
                    break;

                case "deleteItem":
                    String action = getString(R.string.delete) + ": " + "OpenSong/Songs/" + subdir + "\n" + getString(R.string.delete_folder_warning);
                    ArrayList<String> arguments = new ArrayList<>();
                    arguments.add("Songs");
                    arguments.add(subdir);
                    arguments.add("");
                    mainActivityInterface.displayAreYouSure(what, action, arguments, "StorageManagementFragment", callingFragment,null);
                    break;

                case "createItem":
                    // Current sub directory is passed as we will create inside this
                    mainActivityInterface.setWhattodo("newfolder");
                    TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(callingFragment,"StorageManagementFragment",getString(R.string.new_folder),getString(R.string.new_folder_name),null,null,null,true);
                    textInputBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"StorageManagementFragment");
                    break;

                case "renameFolder":
                    // The current sub directory is stored here
                    mainActivityInterface.setWhattodo("renamefolder");
                    String lastpart;
                    if (subdir.contains("/")) {
                        lastpart = subdir.substring(subdir.lastIndexOf("/"));
                        lastpart = lastpart.replace("/","");
                    } else {
                        lastpart = subdir;
                    }
                    TextInputBottomSheet textInputBottomSheet2 = new TextInputBottomSheet(callingFragment,"StorageManagementFragment",getString(R.string.folder_rename),getString(R.string.new_folder_name),lastpart,null,null,true);
                    textInputBottomSheet2.show(mainActivityInterface.getMyFragmentManager(),"StorageManagementFragment");
                    break;

                case "moveContents":
                    Bundle bundle = new Bundle();
                    bundle.putString("subdir", subdir);
                    mainActivityInterface.navigateToFragment(getString(R.string.deeplink_move),0);
                    break;
            }

            dismiss();
        }
    }

}

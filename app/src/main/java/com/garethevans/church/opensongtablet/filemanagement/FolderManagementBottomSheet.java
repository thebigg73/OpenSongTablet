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
    private String deeplink_set_storage_string="", deeplink_backup_string="", delete_string="",
            delete_folder_warning_string="", new_folder_string="", new_folder_name_string="",
            folder_rename_string="", deeplink_move_string="";
    private final Fragment callingFragment;

    public FolderManagementBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        callingFragment = null;
        root = false;
        songs = false;
        subdir = "";
        dismiss();
    }

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
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetStorageFolderBinding.inflate(inflater, container, false);

        prepareStrings();

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        setupView();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            deeplink_set_storage_string = getString(R.string.deeplink_set_storage);
            deeplink_backup_string = getString(R.string.deeplink_backup);
            delete_string = getString(R.string.delete);
            delete_folder_warning_string = getString(R.string.delete_folder_warning);
            new_folder_string = getString(R.string.new_folder);
            new_folder_name_string = getString(R.string.new_folder_name);
            folder_rename_string = getString(R.string.folder_rename);
            deeplink_move_string = getString(R.string.deeplink_move);
        }
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

        } else if (songs) {
            String s = "OpenSong/Songs";
            myView.dialogHeading.setText(s);
            myView.changeLocation.setVisibility(View.GONE);
            myView.renameFolder.setVisibility(View.GONE);
            myView.moveContents.setOnClickListener(new ActionClickListener("moveContents"));
            myView.deleteSubdirectory.setVisibility(View.GONE);
            myView.createSubdirectory.setOnClickListener(new ActionClickListener("createItem"));


        } else {
            String s = "OpenSong/Songs/" + subdir;
            myView.dialogHeading.setText(s);
            myView.changeLocation.setVisibility(View.GONE);
            myView.createSubdirectory.setOnClickListener(new ActionClickListener("createItem"));
            myView.moveContents.setOnClickListener(new ActionClickListener("moveContents"));
            myView.renameFolder.setOnClickListener(new ActionClickListener("renameFolder"));
            myView.deleteSubdirectory.setOnClickListener(new ActionClickListener("deleteItem"));
        }

        myView.exportSongList.setVisibility(View.VISIBLE);
        myView.exportSongList.setOnClickListener(v -> {
            if (getActivity()!=null) {
                ExportSongListBottomSheet exportSongListBottomSheet = new ExportSongListBottomSheet();
                exportSongListBottomSheet.show(getActivity().getSupportFragmentManager(), "ExportSongListBottomSheet");
                dismiss();
            }
        });
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
                    mainActivityInterface.navigateToFragment(deeplink_set_storage_string,0);
                    break;

                case "backupOSB":
                    mainActivityInterface.navigateToFragment(deeplink_backup_string,0);
                    break;

                case "deleteItem":
                    if (callingFragment!=null) {
                        String action = delete_string + ": " + "OpenSong/Songs/" + subdir + "\n" + delete_folder_warning_string;
                        ArrayList<String> arguments = new ArrayList<>();
                        arguments.add("Songs");
                        arguments.add(subdir);
                        arguments.add("");
                        mainActivityInterface.displayAreYouSure(what, action, arguments, "StorageManagementFragment", callingFragment, null);
                    }
                    break;

                case "createItem":
                    // Current sub directory is passed as we will create inside this
                    if (callingFragment!=null) {
                        mainActivityInterface.setWhattodo("newfolder");
                        TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(callingFragment, "StorageManagementFragment", new_folder_string, new_folder_name_string, null, null, null, true);
                        textInputBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "StorageManagementFragment");
                    }
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
                    if (callingFragment!=null) {
                        TextInputBottomSheet textInputBottomSheet2 = new TextInputBottomSheet(callingFragment, "StorageManagementFragment", folder_rename_string, new_folder_name_string, lastpart, null, null, true);
                        textInputBottomSheet2.show(mainActivityInterface.getMyFragmentManager(), "StorageManagementFragment");
                    }
                    break;

                case "moveContents":
                    Bundle bundle = new Bundle();
                    bundle.putString("subdir", subdir);
                    mainActivityInterface.navigateToFragment(deeplink_move_string,0);
                    break;
            }

            dismiss();
        }
    }

}

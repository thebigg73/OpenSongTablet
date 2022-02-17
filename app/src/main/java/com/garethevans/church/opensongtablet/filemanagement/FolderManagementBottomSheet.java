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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetStorageFolderBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
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

        new Thread(() -> requireActivity().runOnUiThread(this::setupView)).start();

        return myView.getRoot();
    }

    private void setupView() {
        myView.backupFolder.setVisibility(View.VISIBLE);
        myView.backupFolder.setOnClickListener(new ActionClickListener("backupOSB", 0));

        if (root) {
            myView.dialogHeading.setText(mainActivityInterface.getStorageAccess().
                    niceUriTree(getContext(), mainActivityInterface,
                            mainActivityInterface.getStorageAccess().homeFolder(getContext(),
                                    null, mainActivityInterface))[1]);
            myView.createSubdirectory.setVisibility(View.GONE);
            myView.moveContents.setVisibility(View.GONE);
            myView.renameFolder.setVisibility(View.GONE);
            myView.deleteSubdirectory.setVisibility(View.GONE);
            myView.changeLocation.setOnClickListener(new ActionClickListener("resetStorage", R.id.setStorageLocationFragment));
            myView.exportSongList.setVisibility(View.GONE);
        } else if (songs) {
            String s = "OpenSong/Songs";
            myView.dialogHeading.setText(s);
            myView.changeLocation.setVisibility(View.GONE);
            myView.renameFolder.setVisibility(View.GONE);
            myView.moveContents.setOnClickListener(new ActionClickListener("moveContents",0));
            myView.deleteSubdirectory.setVisibility(View.GONE);
            myView.createSubdirectory.setOnClickListener(new ActionClickListener("createItem", 0));
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
            myView.createSubdirectory.setOnClickListener(new ActionClickListener("createItem", 0));
            myView.moveContents.setOnClickListener(new ActionClickListener("moveContents",0));
            myView.renameFolder.setOnClickListener(new ActionClickListener("renameFolder",0));
            myView.deleteSubdirectory.setOnClickListener(new ActionClickListener("deleteItem", 0));
            myView.exportSongList.setVisibility(View.GONE);
        }
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
                    dialogFragment = new NewNameBottomSheet(callingFragment,"StorageManagementFragment",false,"Songs",subdir,null, false);
                    dialogFragment.show(requireActivity().getSupportFragmentManager(),"createItem");
                    break;

                case "renameFolder":
                    dialogFragment = new NewNameBottomSheet(callingFragment,"StorageManagementFragment",false,"Songs",subdir,null, true);
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

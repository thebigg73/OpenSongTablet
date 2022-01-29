package com.garethevans.church.opensongtablet.filemanagement;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetExportSongListBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

public class ExportSongListBottomSheet extends BottomSheetDialogFragment {

    private MainActivityInterface mainActivityInterface;
    private BottomSheetExportSongListBinding myView;
    private String selectedFolders = "";
    private final String TAG = "ExportSongListBS";

    @Override
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
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetExportSongListBinding.inflate(inflater, container, false);
        myView.dialogHeader.setClose(this);

        // Build the list of folders
        setupViews();

        return myView.getRoot();
    }

    private void setupViews() {
        // We'll get these from the database
        ArrayList<String> folders = mainActivityInterface.getSQLiteHelper().getFolders(requireContext(),mainActivityInterface);
        for (String folder:folders) {
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(folder);
            checkBox.setChecked(false);
            checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    if (!selectedFolders.contains("$"+compoundButton.getText()+"$")) {
                        selectedFolders = selectedFolders + "$"+compoundButton.getText()+"$";
                    }
                } else {
                    selectedFolders = selectedFolders.replace("$"+compoundButton.getText()+"$","");
                }
            });
            myView.songFolders.addView(checkBox);
        }

        myView.export.setOnClickListener(view -> {
            if (!selectedFolders.isEmpty()) {
                doExport();
            } else {
                mainActivityInterface.getShowToast().doIt(getString(R.string.nothing_selected));
            }
        });
    }

    private void doExport() {
        // Get the folders chosen
        String[] folders = selectedFolders.split("\\$$");
        StringBuilder songContents = new StringBuilder();
        for (String folder:folders) {
            folder = folder.replace("$","");
            // For each selected directory, list the songs that exist.
            if (folder.equals(getString(R.string.mainfoldername)) || folder.equals("MAIN")) {
                    folder = "";
            }
            ArrayList<String> files_ar = mainActivityInterface.getStorageAccess().listFilesInFolder(getContext(), mainActivityInterface, "Songs", folder);
            songContents.append(getString(R.string.songsinfolder)).append(" \"");
            if (folder.equals("")) {
                songContents.append(getString(R.string.mainfoldername));
            } else {
                songContents.append(folder);
            }
            songContents.append("\":\n\n");
            try {
                Collator coll = Collator.getInstance(mainActivityInterface.getLocale());
                coll.setStrength(Collator.SECONDARY);
                Collections.sort(files_ar, coll);
                for (int l = 0; l < files_ar.size(); l++) {
                    songContents.append(files_ar.get(l)).append("\n");
                }
            } catch (Exception e) {
                // Error sorting
            }
            songContents.append("\n\n\n\n");
        }

        Log.d(TAG,songContents.toString());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, requireActivity().getString(R.string.app_name) + " " +
                getString(R.string.exportsongdirectory));
        intent.putExtra(Intent.EXTRA_TEXT, songContents.toString());

        String title = getString(R.string.export);
        Intent chooser = Intent.createChooser(intent, title);
        startActivity(chooser);
    }

}

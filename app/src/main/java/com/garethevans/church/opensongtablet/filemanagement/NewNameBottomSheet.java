package com.garethevans.church.opensongtablet.filemanagement;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetNewNameBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class NewNameBottomSheet extends BottomSheetDialogFragment {

    private MainActivityInterface mainActivityInterface;
    private BottomSheetNewNameBinding myView;
    private final boolean isfile;
    private final String currentDir;
    private final String currentSubDir;
    private final String fragName;
    private final Fragment callingFragment;
    private String songContent;
    private final Song song;
    private final boolean rename;
    private String parentFolder = "";

    public NewNameBottomSheet(Fragment callingFragment, String fragName, boolean isfile,
                         String currentDir, String currentSubDir, Song song, boolean rename) {
        this.isfile = isfile;  // True to create a file, false to create a folder
        this.currentDir = currentDir;
        this.currentSubDir = currentSubDir;
        this.fragName = fragName;
        this.callingFragment = callingFragment;
        this.song = song;
        this.rename = rename;
    }

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
        super.onCreateView(inflater, container, savedInstanceState);

        myView = BottomSheetNewNameBinding.inflate(inflater,container,false);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        // Get the current songXML to pass back as an argument if we need it (good for duplicating!)
        if (song!=null) {
            songContent = mainActivityInterface.getProcessSong().getXML(song);
        }

        // Set listeners
        myView.okButton.setOnClickListener(v -> doSave());
        if (rename) {
            String currentName = currentSubDir;
            // Only show the last section
            if (currentSubDir.contains("/")) {
                parentFolder = currentSubDir.substring(0,currentSubDir.lastIndexOf("/"))+"/";
                currentName = currentSubDir.substring(currentSubDir.lastIndexOf("/"));
                currentName = currentName.replace("/","");
            }
            myView.newName.setText(currentName);
        }

        myView.newName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                    actionId == EditorInfo.IME_ACTION_DONE) {
                myView.okButton.performClick();
            }
            return false;
        });

        myView.newName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null) {
                    String string = mainActivityInterface.getStorageAccess().safeFilename(s.toString());
                    if (!s.toString().equals(string)) {
                        myView.newName.setText(string);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        return myView.getRoot();
    }

    private void doSave() {
        // Check if the file/folder already exists
        boolean exists;
        String newName;
        String message = getString(R.string.error);
        String success = getString(R.string.success);

        if (!myView.newName.getText().toString().isEmpty()) {
            newName = myView.newName.getText().toString();
            newName = mainActivityInterface.getStorageAccess().safeFilename(newName);
            myView.newName.setText(newName);
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(currentDir, currentSubDir, newName);
            exists = mainActivityInterface.getStorageAccess().uriExists(uri);
            if (rename) {
                if (!parentFolder.isEmpty()) {
                    newName = parentFolder + "/" + newName;
                }
                mainActivityInterface.getStorageAccess().renameFolder(song,currentSubDir,newName);
                message = success;
            } else {
                if (isfile && !exists) {
                    if (mainActivityInterface.getStorageAccess().createFile(null, currentDir, currentSubDir, newName)) {
                        message = success;
                    }
                } else if (!isfile && !exists) {
                    if (mainActivityInterface.getStorageAccess().createFolder(currentDir, currentSubDir, newName)) {
                        message = success;
                    }
                } else {
                    message = getString(R.string.song_name_already_taken);
                }
            }
        }
        mainActivityInterface.getShowToast().doIt(message);
        if (message.equals(success)) {
            ArrayList<String> result = new ArrayList<>();
            result.add("success");
            result.add(songContent);
            mainActivityInterface.updateSongMenu(fragName,callingFragment,result);
            dismiss();
        }
    }

}

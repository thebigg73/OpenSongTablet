package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.NewNameDialogBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

public class NewNameDialog extends DialogFragment {

    private MainActivityInterface mainActivityInterface;
    private Preferences preferences;
    private StorageAccess storageAccess;
    private ProcessSong processSong;
    private NewNameDialogBinding myView;
    private ShowToast showToast;
    private final boolean isfile;
    private final String currentDir;
    private final String currentSubDir;
    private final String fragName;
    private final Fragment callingFragment;
    private String songContent;
    private final Song song;
    private final boolean rename;
    private String parentFolder = "";

    public NewNameDialog(Fragment callingFragment, String fragName, boolean isfile, String currentDir, String currentSubDir, Song song, boolean rename) {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Window w = requireDialog().getWindow();
        if (w!=null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        myView = NewNameDialogBinding.inflate(inflater,container,false);

        setHelpers();

        // Get the current songXML to pass back as an argument if we need it (good for duplicating!)
        if (song!=null) {
            songContent = processSong.getXML(song);
        }

        // Set listeners
        myView.okButton.setOnClickListener(v -> doSave());
        myView.cancelButton.setOnClickListener(v -> dismiss());
        if (rename) {
            String currentName = currentSubDir;
            // Only show the last section
            if (currentSubDir.contains("/")) {
                parentFolder = currentSubDir.substring(0,currentSubDir.lastIndexOf("/"))+"/";
                Log.d("NewName","parentFolder="+parentFolder);
                currentName = currentSubDir.substring(currentSubDir.lastIndexOf("/"));
                currentName = currentName.replace("/","");
                Log.d("NewName","currentName="+currentName);
            }
            myView.title.setText(currentName);
        }

        myView.title.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                    actionId == EditorInfo.IME_ACTION_DONE) {
                myView.okButton.performClick();
            }
            return false;
        });

        myView.title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null) {
                    String string = storageAccess.safeFilename(s.toString());
                    if (!s.toString().equals(string)) {
                        myView.title.setText(string);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        return myView.getRoot();
    }

    private void setHelpers() {
        storageAccess = mainActivityInterface.getStorageAccess();
        preferences = mainActivityInterface.getPreferences();
        processSong = mainActivityInterface.getProcessSong();
        showToast = mainActivityInterface.getShowToast();
    }

    private void doSave() {
        // Check if the file/folder already exists
        boolean exists;
        String newName;
        String message = getString(R.string.error);
        String success = getString(R.string.success);

        if (myView.title!=null && myView.title.getText()!=null && !myView.title.getText().toString().isEmpty()) {
            newName = myView.title.getText().toString();
            newName = storageAccess.safeFilename(newName);
            myView.title.setText(newName);
            Uri uri = storageAccess.getUriForItem(getContext(), preferences, currentDir, currentSubDir, newName);
            exists = storageAccess.uriExists(getContext(),uri);
            if (rename) {
                if (!parentFolder.isEmpty()) {
                    newName = parentFolder + "/" + newName;
                }
                Log.d("d","currentSubDir="+currentSubDir+"  newName="+newName);
                storageAccess.renameFolder(requireContext(),preferences,showToast,song,currentSubDir,newName);
                message = success;
            } else {
                if (isfile && !exists) {
                    if (storageAccess.createFile(getContext(), preferences, null, currentDir, currentSubDir, newName)) {
                        message = success;
                    }
                } else if (!isfile && !exists) {
                    if (storageAccess.createFolder(getContext(), preferences, currentDir, currentSubDir, newName)) {
                        message = success;
                    }
                } else {
                    message = getString(R.string.file_exists);
                }
            }
        }
        showToast.doIt(requireContext(),message);
        if (message.equals(success)) {
            ArrayList<String> result = new ArrayList<>();
            result.add("success");
            result.add(songContent);
            mainActivityInterface.updateSongMenu(fragName,callingFragment,result);
            dismiss();
        }
    }


}

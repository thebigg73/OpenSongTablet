package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.garethevans.church.opensongtablet.songprocessing.SongXML;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class NewNameDialog extends DialogFragment {

    MainActivityInterface mainActivityInterface;
    Preferences preferences;
    StorageAccess storageAccess;
    SongXML songXML;
    ProcessSong processSong;
    NewNameDialogBinding myView;
    TextInputEditText title;
    MaterialButton okButton, cancelButton;
    boolean isfile;
    String currentDir, currentSubDir, fragName;
    Fragment callingFragment;
    String songContent;
    Song song;

    public NewNameDialog(Fragment callingFragment, String fragName, boolean isfile, String currentDir, String currentSubDir, Song song) {
        this.isfile = isfile;  // True to create a file, false to create a folder
        this.currentDir = currentDir;
        this.currentSubDir = currentSubDir;
        this.fragName = fragName;
        this.callingFragment = callingFragment;
        this.song = song;
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
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        myView = NewNameDialogBinding.inflate(inflater,container,false);

        setHelpers();
        setViews();

        // Get the current songXML to pass back as an argument if we need it (good for duplicating!)
        songContent = songXML.getXML(song,processSong);

        // Set listeners
        okButton.setOnClickListener(v -> doSave());
        cancelButton.setOnClickListener(v -> dismiss());
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null) {
                    String string = storageAccess.safeFilename(s.toString());
                    if (!s.toString().equals(string)) {
                        title.setText(string);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        return myView.getRoot();
    }

    private void setViews() {
        okButton = myView.okButton;
        cancelButton = myView.cancelButton;
        title = myView.title;
    }

    private void setHelpers() {
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        songXML = new SongXML();
        processSong = new ProcessSong();
    }

    private void doSave() {
        // Check if the file/folder already exists
        boolean exists;
        String newName;
        String message = getActivity().getResources().getString(R.string.error);
        String success = getActivity().getResources().getString(R.string.success);

        if (title!=null && title.getText()!=null && title.getText()!=null && title.getText().toString()!=null && !title.getText().toString().isEmpty()) {
            newName = title.getText().toString();
            newName = storageAccess.safeFilename(newName);
            title.setText(newName);
            Uri uri = storageAccess.getUriForItem(getActivity(), preferences, currentDir, currentSubDir, newName);
            exists = storageAccess.uriExists(getActivity(),uri);
            if (isfile && !exists) {
                if (storageAccess.createFile(getActivity(),preferences,null, currentDir, currentSubDir, newName)) {
                    message = success;
                }
            } else if (!isfile && !exists) {
                if (storageAccess.createFolder(getActivity(),preferences,currentDir,currentSubDir,newName)) {
                    message = success;
                }
            } else if (exists) {
                message = getActivity().getResources().getString(R.string.file_exists);
            }
        }
        ShowToast.showToast(getActivity(),message);
        if (message.equals(success)) {
            ArrayList<String> result = new ArrayList<>();
            result.add("success");
            result.add(songContent);
            mainActivityInterface.updateSongMenu(fragName,callingFragment,result);
            dismiss();
        }
    }
}

package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.EditSongMainBinding;
import com.garethevans.church.opensongtablet.interfaces.EditSongFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;

import java.util.ArrayList;

public class EditSongFragmentMain extends Fragment  {

    // The variable used in this fragment
    private EditSongMainBinding myView;
    private MainActivityInterface mainActivityInterface;
    private EditSongFragmentInterface editSongFragmentInterface;
    private String newFolder;
    private TextInputBottomSheet textInputBottomSheet;
    private ArrayList<String> folders;
    ExposedDropDownArrayAdapter arrayAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        editSongFragmentInterface = (EditSongFragmentInterface) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = requireActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = EditSongMainBinding.inflate(inflater, container, false);

        // Initialise the views
        setupValues();

        // Set listeners
        setUpListeners();

        return myView.getRoot();
    }

    // Initialise the views
    private void setupValues() {
        myView.title.setText(mainActivityInterface.getTempSong().getTitle());
        myView.author.setText(mainActivityInterface.getTempSong().getAuthor());
        myView.copyright.setText(mainActivityInterface.getTempSong().getCopyright());
        myView.songNotes.setText(mainActivityInterface.getTempSong().getNotes());
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.songNotes);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.songNotes,5);
        myView.filename.setText(mainActivityInterface.getTempSong().getFilename());
        folders = mainActivityInterface.getSQLiteHelper().getFolders(getContext(), mainActivityInterface);
        newFolder = "+ " + getString(R.string.new_folder_add);
        folders.add(newFolder);
        arrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.folder, R.layout.view_exposed_dropdown_item, folders);
        myView.folder.setAdapter(arrayAdapter);
        myView.folder.setText(mainActivityInterface.getTempSong().getFolder());
        textInputBottomSheet = new TextInputBottomSheet(this,"EditSongFragmentMain",getString(R.string.new_folder),getString(R.string.new_folder_name),null,"","",true);
    }

    // Sor the view visibility, listeners, etc.
    private void setUpListeners() {
        myView.title.addTextChangedListener(new MyTextWatcher("title"));
        myView.folder.addTextChangedListener(new MyTextWatcher("folder"));
        myView.filename.addTextChangedListener(new MyTextWatcher("filename"));
        myView.author.addTextChangedListener(new MyTextWatcher("author"));
        myView.copyright.addTextChangedListener(new MyTextWatcher("copyright"));
        myView.songNotes.addTextChangedListener(new MyTextWatcher("notes"));

        // Scroll listener
        myView.nestedScrollView.setExtendedFabToAnimate(editSongFragmentInterface.getSaveButton());
    }

    private class MyTextWatcher implements TextWatcher {

        String what;
        String value = "";

        MyTextWatcher(String what) {
            this.what = what;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != null) {
                value = s.toString();
                if (what.equals("filename")) {
                    value = mainActivityInterface.getStorageAccess().safeFilename(value);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (what.equals("folder") && value.equals(newFolder)) {
                myView.folder.setText(mainActivityInterface.getTempSong().getFolder());
                textInputBottomSheet.show(requireActivity().getSupportFragmentManager(),
                        "TextInputBottomSheet");
            } else {
                updateTempSong();
            }
        }

        public void updateTempSong() {
            switch (what) {
                case "folder":
                    if (!what.equals(newFolder)) {
                        mainActivityInterface.getTempSong().setFolder(value);
                    }
                    break;
                case "filename":
                    mainActivityInterface.getTempSong().setFilename(value);
                    break;
                case "title":
                    mainActivityInterface.getTempSong().setTitle(value);
                    break;
                case "copyright":
                    mainActivityInterface.getTempSong().setCopyright(value);
                    break;
                case "author":
                    mainActivityInterface.getTempSong().setAuthor(value);
                    break;
                case "notes":
                    mainActivityInterface.getTempSong().setNotes(value);
                    break;
            }
        }
    }


    public void updateValue(String value) {
        // New folder name given.  If it isn't null try to create it and select it
        boolean selectNewFolder = false;
        if (value!=null && !value.isEmpty()) {
            // Try to create the new folder if it doesn't exist
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(requireContext(),
                    mainActivityInterface,"Songs",value,null);
            if (!mainActivityInterface.getStorageAccess().uriExists(requireContext(),uri)) {
                selectNewFolder = mainActivityInterface.getStorageAccess().createFolder(requireActivity(),
                        mainActivityInterface,"Songs","",value);
            }
        }

        if (selectNewFolder) {
            ArrayList<String> songIds = mainActivityInterface.getStorageAccess().listSongs(requireContext(),mainActivityInterface);
            mainActivityInterface.getStorageAccess().writeSongIDFile(requireContext(),mainActivityInterface,songIds);
            folders = mainActivityInterface.getStorageAccess().getSongFolders(requireContext(),songIds,true,null);
            folders.add(newFolder);
            arrayAdapter.notifyDataSetChanged();
            myView.folder.setText(value);
        } else {
            myView.folder.setText(mainActivityInterface.getTempSong().getFolder());
        }
    }

    // Finished with this view
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

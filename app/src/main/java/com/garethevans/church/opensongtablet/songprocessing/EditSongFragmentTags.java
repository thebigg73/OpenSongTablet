package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.EditSongTagsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class EditSongFragmentTags extends Fragment {

    MainActivityInterface mainActivityInterface;
    EditSongTagsBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = requireActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = EditSongTagsBinding.inflate(inflater, container, false);

        // Set up the current values
        setupValues();

        // Set up the listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupValues() {
        myView.tags.setText(mainActivityInterface.getTempSong().getTheme());
        myView.ccli.setText(mainActivityInterface.getTempSong().getCcli());
        myView.user1.setText(mainActivityInterface.getTempSong().getUser1());
        myView.user2.setText(mainActivityInterface.getTempSong().getUser2());
        myView.user3.setText(mainActivityInterface.getTempSong().getUser3());
        myView.hymnnum.setText(mainActivityInterface.getTempSong().getHymnnum());
        myView.presorder.setText(mainActivityInterface.getTempSong().getPresentationorder());
    }

    private void setupListeners() {
        myView.ccli.addTextChangedListener(new MyTextWatcher("ccli"));
        myView.user1.addTextChangedListener(new MyTextWatcher("user1"));
        myView.user2.addTextChangedListener(new MyTextWatcher("user2"));
        myView.user3.addTextChangedListener(new MyTextWatcher("user3"));
        myView.hymnnum.addTextChangedListener(new MyTextWatcher("hymnnum"));

    }
    private class MyTextWatcher implements TextWatcher {

        String what;
        MyTextWatcher(String what) {
            this.what = what;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            mainActivityInterface.showSaveAllowed(mainActivityInterface.songChanged());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (what) {
                case "tags":
                    mainActivityInterface.getTempSong().setTheme(editable.toString());
                    break;
                case "user1":
                    mainActivityInterface.getTempSong().setUser1(editable.toString());
                    break;
                case "user2":
                    mainActivityInterface.getTempSong().setUser2(editable.toString());
                    break;
                case "user3":
                    mainActivityInterface.getTempSong().setUser3(editable.toString());
                    break;
                case "ccli":
                    mainActivityInterface.getTempSong().setCcli(editable.toString());
                    break;
                case "presorder":
                    mainActivityInterface.getTempSong().setPresentationorder(editable.toString());
                    break;
            }
        }
    }
}
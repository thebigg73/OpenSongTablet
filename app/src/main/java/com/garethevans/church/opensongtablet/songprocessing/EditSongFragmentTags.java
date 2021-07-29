package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
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
}
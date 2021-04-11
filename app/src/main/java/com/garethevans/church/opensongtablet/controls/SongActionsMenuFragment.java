package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsSongactionsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class SongActionsMenuFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsSongactionsBinding myView;
    private Song song;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsSongactionsBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(null,getString(R.string.song_actions));

        // Set helpers
        setHelpers();

        // Set Listeners
        setListeners();

        return myView.getRoot();
    }

    private void setHelpers() {
        song = mainActivityInterface.getSong();
    }

    private void setListeners() {
        myView.highlighter.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.highlighterFragment));
        //myView.autoscroll.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.autoscrollFragment));
        //myView.metronome.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.metronomeFragment));
        //myView.notation.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.notationFragment));
        //myView.stickyNotes.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.stickynotesFragment));
        //myView.pad.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.padFragment));
    }

}

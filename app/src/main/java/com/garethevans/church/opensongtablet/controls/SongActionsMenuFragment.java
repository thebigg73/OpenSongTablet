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

public class SongActionsMenuFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsSongactionsBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsSongactionsBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(getString(R.string.song_actions));

        // Set Listeners
        setListeners();

        return myView.getRoot();
    }

    private void setListeners() {
        myView.edit.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.editSongFragment));
        //myView.delete.setOnClickListener(v -> );
        myView.share.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.exportFragment));
        myView.highlighter.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.highlighterFragment));
        // TODO add pages for the settings
        //myView.autoscroll.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.autoscrollFragment));
        myView.metronome.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.metronomeFragment));
        myView.notation.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.musicScoreFragment));
        myView.stickyNotes.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.stickyNotesFragment));
        //myView.pad.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.padFragment));
        //myView.midi.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.songMidiFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

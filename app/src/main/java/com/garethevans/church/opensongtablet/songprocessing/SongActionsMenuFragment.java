package com.garethevans.church.opensongtablet.songprocessing;

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
import com.garethevans.church.opensongtablet.midi.MidiSongBottomSheet;

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

        // Add the current song title to the menu
        addCurrentSong();

        // Set Listeners
        setListeners();

        return myView.getRoot();
    }

    private void addCurrentSong() {
        String currentSong = mainActivityInterface.getSong().getTitle();
        if (currentSong!=null && !currentSong.isEmpty()) {
            currentSong = " (" + currentSong + ")";
            String newText = getString(R.string.edit_song) + currentSong;
            myView.delete.setHint(newText);
            newText = getString(R.string.export_current_song) + currentSong;
            myView.share.setHint(newText);
        }
    }

    private void setListeners() {
        // TODO add pages for the settings
        myView.importButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.import_graph));
        myView.edit.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.editsong_graph));
        myView.delete.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("deleteSong",getString(R.string.delete_song_warning),null,"SongActionsMenuFragment",this,mainActivityInterface.getSong()));
        myView.share.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.exportFragment));
        myView.pad.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.pads_graph));
        myView.autoscroll.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.autoscrollSettingsFragment));
        myView.metronome.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.metronomeFragment));
        myView.stickyNotes.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.stickyNotesFragment));
        myView.highlighter.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.highlighterFragment));
        myView.links.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.linksFragment));
        myView.chords.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.chords_graph));
        myView.notation.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.musicScoreFragment));
        myView.midi.setOnClickListener(v -> {
            mainActivityInterface.navHome();
            MidiSongBottomSheet midiSongBottomSheet = new MidiSongBottomSheet();
            midiSongBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"midiSongBottomSheet");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

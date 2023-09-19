package com.garethevans.church.opensongtablet.midi;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsMidiOptionsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class MidiOptionsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsMidiOptionsBinding myView;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MidiOptionsFragment";
    private String midi_string="";

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(midi_string);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        midi_string = context.getString(R.string.midi);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsMidiOptionsBinding.inflate(inflater,container,false);

        setListeners();

        return myView.getRoot();
    }

    private void setListeners() {
        myView.midiSettings.setOnClickListener(view -> mainActivityInterface.navigateToFragment(null,R.id.midiFragment));
        myView.midiSongCommands.setOnClickListener(view -> {
            MidiSongBottomSheet midiSongBottomSheet = new MidiSongBottomSheet();
            midiSongBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"MidiSongBottomSheet");
        });
        myView.midiCustomActions.setOnClickListener(view -> {
            MidiActionBottomSheet midiActionBottomSheet = new MidiActionBottomSheet();
            midiActionBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"MidiActionBottomSheet");
        });
        myView.midiBoard.setOnClickListener(view -> {
            MidiBoardBottomSheet midiBoardBottomSheet = new MidiBoardBottomSheet();
            midiBoardBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"MidiBoardBottomSheet");
        });
    }
}

package com.garethevans.church.opensongtablet.chords;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsChordsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ChordSettingsFragment extends DialogFragment {

    private SettingsChordsBinding myView;
    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsChordsBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.chords));
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_chords));

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupListeners() {
        myView.chordsView.setOnClickListener(v -> {
            mainActivityInterface.navHome();
            ChordFingeringBottomSheet chordFingeringBottomSheet = new ChordFingeringBottomSheet();
            chordFingeringBottomSheet.show(requireActivity().getSupportFragmentManager(),"ChordFingeringBottomSheet");
        });
        myView.chordsCustom.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.customChordsFragment));
        myView.chordsFormat.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.chordFormatFragment));
        myView.chordsTranspose.setOnClickListener(v -> {
            TransposeBottomSheet transposeBottomSheet = new TransposeBottomSheet();
            transposeBottomSheet.show(requireActivity().getSupportFragmentManager(),"TransposeBottomSheet");
        });
    }
}

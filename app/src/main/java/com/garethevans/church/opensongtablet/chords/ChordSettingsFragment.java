package com.garethevans.church.opensongtablet.chords;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupListeners() {
        //myView.chordsView.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.chordViewFragment));
        //myView.chordsCustom.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.customChordsFragment));
        //myView.chordsSettings.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.chordSettingsFragment));
        myView.chordsTranspose.setOnClickListener(v -> {
            TransposeBottomSheet transposeBottomSheet = new TransposeBottomSheet();
            transposeBottomSheet.show(requireActivity().getSupportFragmentManager(),"TransposeBottomSheet");
        });
    }
}

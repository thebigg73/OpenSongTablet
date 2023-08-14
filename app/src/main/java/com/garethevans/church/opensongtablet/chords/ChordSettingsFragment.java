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

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ChordSettingsFrag";

    private SettingsChordsBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String chords_string="", website_chords_string="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(chords_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsChordsBinding.inflate(inflater,container,false);

        prepareStrings();

        webAddress = website_chords_string;

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            chords_string = getString(R.string.chords);
            website_chords_string = getString(R.string.website_chords);
        }
    }

    private void setupListeners() {
        myView.chordsView.setOnClickListener(v -> {
            if (getActivity()!=null) {
                mainActivityInterface.navHome();
                ChordFingeringBottomSheet chordFingeringBottomSheet = new ChordFingeringBottomSheet();
                chordFingeringBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "ChordFingeringBottomSheet");
            }
        });
        myView.chordsCustom.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.customChordsFragment));
        myView.chordsFormat.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null, R.id.chordFormatFragment));
        myView.chordsTranspose.setOnClickListener(v -> {
            if (getActivity()!=null) {
                TransposeBottomSheet transposeBottomSheet = new TransposeBottomSheet(false);
                transposeBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "TransposeBottomSheet");
            }
        });
    }
}

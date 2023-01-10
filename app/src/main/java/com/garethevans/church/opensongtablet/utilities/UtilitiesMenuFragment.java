package com.garethevans.church.opensongtablet.utilities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsUtilitiesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class UtilitiesMenuFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsUtilitiesBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsUtilitiesBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.utilities));
        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupListeners() {
        myView.soundMeter.setOnClickListener(v -> {
            SoundLevelBottomSheet soundLevelBottomSheet = new SoundLevelBottomSheet();
            soundLevelBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"soundLevelBottomSheet");
        });
        myView.tuner.setOnClickListener(v -> {
            TunerBottomSheet tunerBottomSheet = new TunerBottomSheet();
            tunerBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"tunerBottomSheet");
        });
    }

}

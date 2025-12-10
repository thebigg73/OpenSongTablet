package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.SettingsDrummerBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class DrummerFragment extends Fragment {

    private final String TAG = "DrummerFragment";
    private SettingsDrummerBinding myView;
    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        mainActivityInterface = (MainActivityInterface) context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public @org.jetbrains.annotations.Nullable View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsDrummerBinding.inflate(inflater,container,false);

        prepareViews();
        prepareListeners();

        return myView.getRoot();
    }

    private void prepareViews() {
        myView.songTempo.setHint(mainActivityInterface.getSong().getTempo());
    }

    private void prepareListeners() {
        myView.prepareFiles.setOnClickListener(view -> {
            mainActivityInterface.getDrummer().setupDrums("4_4_Basic");
        });
        myView.startDrummer.setOnClickListener(view -> {
            mainActivityInterface.getDrummer().startDrummer();
        });
        myView.stopDrummer.setOnClickListener(view -> {
            mainActivityInterface.getDrummer().stopAll();
        });
        myView.intro.setOnClickListener(view -> mainActivityInterface.getDrummer().setNextPart("intro"));
        myView.mainBeat.setOnClickListener(view -> mainActivityInterface.getDrummer().setNextPart("main_beat"));
        myView.mainStart.setOnClickListener(view -> mainActivityInterface.getDrummer().setNextPart("main_start"));
        myView.mainFill1.setOnClickListener(view -> mainActivityInterface.getDrummer().setNextPart("main_fill_1"));
        myView.mainFill2.setOnClickListener(view -> mainActivityInterface.getDrummer().setNextPart("main_fill_2"));
        myView.variationBeat.setOnClickListener(view -> mainActivityInterface.getDrummer().setNextPart("variation_beat"));
        myView.variationStart.setOnClickListener(view -> mainActivityInterface.getDrummer().setNextPart("variation_start"));
        myView.variationFill1.setOnClickListener(view -> mainActivityInterface.getDrummer().setNextPart("variation_fill_1"));
        myView.variationFill2.setOnClickListener(view -> mainActivityInterface.getDrummer().setNextPart("variation_fill_2"));
    }
}

package com.garethevans.church.opensongtablet.voicelive;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsVoiceliveOptionsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class VoiceLiveFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "VoiceLiveFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsVoiceliveOptionsBinding myView;
    private String voicelive_string="", voicelive_address="";

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings();
        mainActivityInterface.updateToolbar(voicelive_string);
        mainActivityInterface.updateToolbarHelp(voicelive_address);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsVoiceliveOptionsBinding.inflate(inflater, container, false);

        prepareStrings();

        // Setup views
        setupViews();

        // Listeners
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            voicelive_string = getString(R.string.voicelive);
            voicelive_address = getString(R.string.website_forum);
        }
    }

    private void setupViews() {
        myView.voiceLiveChannel.setValue(mainActivityInterface.getVoiceLive().getVoiceLiveChannel());
        myView.voiceLiveChannel.setHint(String.valueOf(mainActivityInterface.getVoiceLive().getVoiceLiveChannel()));
        myView.voiceLiveChannel.setAdjustableButtons(true);
        myView.voiceLiveOverrideChannel.setChecked(mainActivityInterface.getVoiceLive().getVoiceLiveOverrideChannel());
        myView.voiceLiveSendKey.setChecked(mainActivityInterface.getVoiceLive().getVoiceLiveSendKey());
        switch (mainActivityInterface.getVoiceLive().getVoiceLiveMajorHarmony()) {
            case "MAJ1":
            default:
                myView.voiceLiveMajorHarmony.setSliderPos(0);
                break;
            case "MAJ2":
                myView.voiceLiveMajorHarmony.setSliderPos(1);
                break;
            case "MAJ3":
                myView.voiceLiveMajorHarmony.setSliderPos(2);
                break;
        }
        switch (mainActivityInterface.getVoiceLive().getVoiceLiveMinorHarmony()) {
            case "MIN1":
            default:
                myView.voiceLiveMinorHarmony.setSliderPos(0);
                break;
            case "MIN2":
                myView.voiceLiveMinorHarmony.setSliderPos(1);
                break;
            case "MIN3":
                myView.voiceLiveMinorHarmony.setSliderPos(2);
                break;
        }
    }

    public void setupListeners() {
        myView.voiceLiveChannel.addOnChangeListener((slider, value, fromUser) -> {
            mainActivityInterface.getVoiceLive().setVoiceLiveChannel((int) value);
            myView.voiceLiveChannel.setHint(String.valueOf((int) value));
        });
        myView.voiceLiveSendKey.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getVoiceLive().setVoiceLiveSendKey(b));
        myView.voiceLiveOverrideChannel.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getVoiceLive().setVoiceLiveOverrideChannel(b));
        myView.voiceLiveMajorHarmony.addOnChangeListener((slider, value, fromUser) -> {
            switch ((int) value) {
                case 0:
                default:
                    mainActivityInterface.getVoiceLive().setVoiceLiveMajorHarmony("MAJ1");
                    break;
                case 1:
                    mainActivityInterface.getVoiceLive().setVoiceLiveMajorHarmony("MAJ2");
                    break;
                case 2:
                    mainActivityInterface.getVoiceLive().setVoiceLiveMajorHarmony("MAJ3");
                    break;
            }
        });
        myView.voiceLiveMinorHarmony.addOnChangeListener((slider, value, fromUser) -> {
            switch ((int) value) {
                case 0:
                default:
                    mainActivityInterface.getVoiceLive().setVoiceLiveMinorHarmony("MIN1");
                    break;
                case 1:
                    mainActivityInterface.getVoiceLive().setVoiceLiveMinorHarmony("MIN2");
                    break;
                case 2:
                    mainActivityInterface.getVoiceLive().setVoiceLiveMinorHarmony("MIN3");
                    break;
            }
        });
    }
}

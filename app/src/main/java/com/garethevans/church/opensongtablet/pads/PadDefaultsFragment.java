package com.garethevans.church.opensongtablet.pads;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsPadsDefaultsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class PadDefaultsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsPadsDefaultsBinding myView;
    private boolean padPlaying;
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
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
        myView = SettingsPadsDefaultsBinding.inflate(inflater, container, false);

        if (getContext()!=null) {
            mainActivityInterface.updateToolbar(getString(R.string.pad_settings_info));
            webAddress = getString(R.string.website_pad);
        }

        // Set up views based on preferences
        setupViews();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        float padVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("padVol",1.0f);
        myView.padVolume.setValue(padVol*100f);
        myView.padVolume.setHint((int)(padVol*100f)+"%");
        myView.padVolume.setLabelFormatter(value -> ((int)value)+"%");

        String padPan = mainActivityInterface.getPreferences().getMyPreferenceString("padPan","C");
        switch (padPan) {
            case "L":
                myView.padPan.setSliderPos(0);
                break;
            case "C":
            default:
                myView.padPan.setSliderPos(1);
                break;
            case "R":
                myView.padPan.setSliderPos(2);
                break;
        }

        boolean padAutoStart = mainActivityInterface.getPreferences().getMyPreferenceBoolean("padAutoStart", false);
        myView.crossFadePad.setChecked(padAutoStart);
        if (padAutoStart) {
            myView.crossFadeTime.setVisibility(View.VISIBLE);
        } else {
            myView.crossFadeTime.setVisibility(View.GONE);
        }

        int padCrossFadeTime = mainActivityInterface.getPreferences().getMyPreferenceInt("padCrossFadeTime",8000);
        myView.crossFadeTime.setValue(padCrossFadeTime/1000f);
        myView.crossFadeTime.setHint((padCrossFadeTime/1000f)+"ms");
        myView.crossFadeTime.setLabelFormatter(value -> value+"ms");

        myView.onscreenPadHide.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "onscreenPadHide",true));
    }

    private void setupListeners() {
        myView.padVolume.addOnSliderTouchListener(new MySliderTouchListener("padVol"));
        myView.padVolume.addOnChangeListener(new MyOnChangeListener("padVol"));
        myView.padPan.addOnChangeListener(new MyOnChangeListener("padPan"));
        myView.crossFadeTime.addOnSliderTouchListener(new MySliderTouchListener("padCrossFadeTime"));
        myView.crossFadeTime.addOnChangeListener(new MyOnChangeListener("padCrossFadeTime"));
        myView.crossFadePad.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("padAutoStart",b);
            if (b) {
                myView.crossFadeTime.setVisibility(View.VISIBLE);
            } else {
                myView.crossFadeTime.setVisibility(View.GONE);
            }
        });
        myView.onscreenPadHide.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                    "onscreenPadHide",b);
            mainActivityInterface.
                    updateOnScreenInfo("setpreferences");
        });
        padPlaying = mainActivityInterface.getPad().isPadPlaying();
        changePlayIcon();

        myView.startStopButton.setOnClickListener(v -> {
            padPlaying = mainActivityInterface.playPad();
            changePlayIcon();
        });
    }

    private void changePlayIcon() {
        if (padPlaying) {
            myView.startStopButton.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.stop,null));
        } else {
            myView.startStopButton.setImageDrawable(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.play,null));
        }
    }

    private class MySliderTouchListener implements Slider.OnSliderTouchListener {

        private final String prefName;

        MySliderTouchListener(String prefName) {
            this.prefName = prefName;
        }

        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {}

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            // Save the value
            switch (prefName) {
                case "padVol":
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(prefName,myView.padVolume.getValue()/100f);
                    // IV - When playing, set the volumes to the new preferences
                    if (mainActivityInterface.getPad().isPadPlaying()) {
                        mainActivityInterface.getPad().setVolume(1,-1,-1);
                        mainActivityInterface.getPad().setVolume(2,-1,-1);
                    }
                    break;

                case "padCrossFadeTime":
                    mainActivityInterface.getPreferences().setMyPreferenceInt(prefName, (int)(myView.crossFadeTime.getValue()*1000f));
                    break;
            }
        }
    }
    private class MyOnChangeListener implements Slider.OnChangeListener {

        private final String prefName;
        MyOnChangeListener(String prefName) {
            this.prefName = prefName;
        }

        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            switch (prefName) {
                case "padVol":
                    myView.padVolume.setHint(value+"%");
                    break;

                case "padPan":
                    mainActivityInterface.getPreferences().setMyPreferenceString(prefName,"LCR".substring((int) value, (int) value + 1));
                    // IV - When playing, set the volumes to the new preferences
                    if (mainActivityInterface.getPad().isPadPlaying()) {
                        mainActivityInterface.getPad().setVolume(1,-1,-1);
                        mainActivityInterface.getPad().setVolume(2,-1,-1);
                    }
                    break;

                case "padCrossFadeTime":
                    myView.crossFadeTime.setHint(value+"s");
            }
        }
    }
}

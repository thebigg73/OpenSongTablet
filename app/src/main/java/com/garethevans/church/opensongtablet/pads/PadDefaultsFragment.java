package com.garethevans.church.opensongtablet.pads;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsPadsDefaultsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class PadDefaultsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsPadsDefaultsBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsPadsDefaultsBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.pad_settings_info));
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_pad));

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
                    break;

                case "padCrossFadeTime":
                    mainActivityInterface.getPreferences().setMyPreferenceInt("padCrossFadeTime", (int)(myView.crossFadeTime.getValue()*1000f));
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
                    int pos = myView.padPan.getValue();
                    String pan;
                    switch (pos) {
                        case 0:
                            pan = "L";
                            break;
                        case 1:
                        default:
                            pan = "C";
                            break;
                        case 2:
                            pan = "R";
                            break;
                    }
                    mainActivityInterface.getPreferences().setMyPreferenceString("padPan",pan);
                    break;

                case "padCrossFadeTime":
                    myView.crossFadeTime.setHint(value+"s");
            }
        }
    }

}

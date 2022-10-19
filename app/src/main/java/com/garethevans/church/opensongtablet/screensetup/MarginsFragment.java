package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsMarginsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class MarginsFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsMarginsBinding myView;
    private int marginLeft, marginRight, marginBottom;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsMarginsBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.margins));
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_margins));

        setupViews();

        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        // Set the margins of the nestedScrollView to 100px (programmatically so not dp)
        myView.nestedScrollView.setPadding(100,100,100,100);

        marginLeft = mainActivityInterface.getPreferences().getMyPreferenceInt("marginLeft",0);
        marginRight = mainActivityInterface.getPreferences().getMyPreferenceInt("marginRight",0);
        //marginTop = mainActivityInterface.getPreferences().getMyPreferenceInt("marginTop",0);
        marginBottom = mainActivityInterface.getPreferences().getMyPreferenceInt("marginBottom",0);

        myView.leftMargin.setValue(marginLeft);
        myView.rightMargin.setValue(marginRight);
        //myView.topMargin.setValue(marginTop);
        myView.bottomMargin.setValue(marginBottom);

        myView.leftMargin.setLabelFormatter(value -> (int)value+" px");
        myView.rightMargin.setLabelFormatter(value -> (int)value+" px");
        //myView.topMargin.setLabelFormatter(value -> (int)value+" px");
        myView.bottomMargin.setLabelFormatter(value -> (int)value+" px");

        myView.leftMargin.setHint((int)marginLeft+" px");
        myView.rightMargin.setHint((int)marginRight+" px");
        //myView.topMargin.setHint((int)marginTop+" px");
        myView.bottomMargin.setHint((int)marginBottom+" px");
    }

    private void setupListeners() {
        myView.leftMargin.addOnSliderTouchListener(new MySliderTouch("marginLeft"));
        myView.rightMargin.addOnSliderTouchListener(new MySliderTouch("marginRight"));
        //myView.topMargin.addOnSliderTouchListener(new MySliderTouch("marginTop"));
        myView.bottomMargin.addOnSliderTouchListener(new MySliderTouch("marginBottom"));
        myView.leftMargin.addOnChangeListener(new MySliderChange("marginLeft"));
        myView.rightMargin.addOnChangeListener(new MySliderChange("marginRight"));
        //myView.topMargin.addOnChangeListener(new MySliderChange("marginTop"));
        myView.bottomMargin.addOnChangeListener(new MySliderChange("marginBottom"));
    }

    private class MySliderTouch implements Slider.OnSliderTouchListener {

        private final String pref;
        MySliderTouch(String pref) {
            this.pref = pref;
        }

        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {}

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            mainActivityInterface.getPreferences().setMyPreferenceInt(pref,(int)slider.getValue());
        }
    }

    private class MySliderChange implements Slider.OnChangeListener {

        private final String pref;

        MySliderChange(String pref) {
            this.pref = pref;
        }

        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            String hint = ((int)value + " px");
            int val = (int)value;

            switch (pref) {
                case "marginLeft":
                    myView.leftMargin.setHint(hint);
                    marginLeft = val;
                    break;
                case "marginRight":
                    myView.rightMargin.setHint(hint);
                    marginRight = val;
                    break;
                case "marginBottom":
                    myView.bottomMargin.setHint(hint);
                    marginBottom = val;
                    break;
            }

            mainActivityInterface.getPreferences().setMyPreferenceInt(pref,val);
            mainActivityInterface.updateInsetPrefs();
            mainActivityInterface.deviceInsets();
        }
    }
}

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
        //myView.nestedScrollView.setPadding(0,0,0,0);

        myView.ignoreCutouts.setChecked(mainActivityInterface.getWindowFlags().getIgnoreCutouts());

        myView.navBarKeepSpace.setChecked(mainActivityInterface.getWindowFlags().getNavBarKeepSpace());

        myView.immersiveMode.setChecked(mainActivityInterface.getWindowFlags().getImmersiveMode());
        checkVisibilityChange();

        myView.leftMargin.setValue(mainActivityInterface.getWindowFlags().getCustomMarginLeft());
        myView.rightMargin.setValue(mainActivityInterface.getWindowFlags().getCustomMarginRight());
        myView.topMargin.setValue(mainActivityInterface.getWindowFlags().getCustomMarginTop());
        myView.bottomMargin.setValue(mainActivityInterface.getWindowFlags().getCustomMarginBottom());

        myView.leftMargin.setLabelFormatter(value -> (int) value + " px");
        myView.rightMargin.setLabelFormatter(value -> (int) value + " px");
        myView.topMargin.setLabelFormatter(value -> (int) value + " px");
        myView.bottomMargin.setLabelFormatter(value -> (int) value + " px");

        myView.leftMargin.setHint((int) mainActivityInterface.getWindowFlags().getCustomMarginLeft() + " px");
        myView.rightMargin.setHint((int) mainActivityInterface.getWindowFlags().getCustomMarginRight() + " px");
        myView.topMargin.setHint((int) mainActivityInterface.getWindowFlags().getCustomMarginTop() + " px");
        myView.bottomMargin.setHint((int) mainActivityInterface.getWindowFlags().getCustomMarginBottom() + " px");
    }

    private void setupListeners() {
        myView.leftMargin.addOnSliderTouchListener(new MySliderTouch("marginLeft"));
        myView.rightMargin.addOnSliderTouchListener(new MySliderTouch("marginRight"));
        myView.topMargin.addOnSliderTouchListener(new MySliderTouch("marginTop"));
        myView.bottomMargin.addOnSliderTouchListener(new MySliderTouch("marginBottom"));
        myView.leftMargin.addOnChangeListener(new MySliderChange("marginLeft"));
        myView.rightMargin.addOnChangeListener(new MySliderChange("marginRight"));
        myView.topMargin.addOnChangeListener(new MySliderChange("marginTop"));
        myView.bottomMargin.addOnChangeListener(new MySliderChange("marginBottom"));
        myView.immersiveMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getWindowFlags().setImmersiveMode(isChecked);
            mainActivityInterface.getWindowFlags().hideOrShowSystemBars();
            mainActivityInterface.getWindowFlags().setMargins();
            mainActivityInterface.updateMargins();
            checkVisibilityChange();
        });
        myView.navBarKeepSpace.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getWindowFlags().setNavBarKeepSpace(isChecked);
            mainActivityInterface.getWindowFlags().hideOrShowSystemBars();
            mainActivityInterface.getWindowFlags().setMargins();
            mainActivityInterface.updateMargins();
        });
        myView.ignoreCutouts.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            mainActivityInterface.getWindowFlags().setIgnoreCutouts(isChecked);
            mainActivityInterface.getWindowFlags().hideOrShowSystemBars();
            mainActivityInterface.getWindowFlags().setMargins();
            mainActivityInterface.updateMargins();
        }));
    }

    private class MySliderTouch implements Slider.OnSliderTouchListener {

        private final String pref;

        MySliderTouch(String pref) {
            this.pref = pref;
        }

        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            int val = (int) slider.getValue();

            switch (pref) {
                case "marginLeft":
                    mainActivityInterface.getWindowFlags().setCustomMarginLeft(val, true);
                    break;
                case "marginRight":
                    mainActivityInterface.getWindowFlags().setCustomMarginRight(val, true);
                    break;
                case "marginBottom":
                    mainActivityInterface.getWindowFlags().setCustomMarginBottom(val, true);
                    break;
                case "marginTop":
                    mainActivityInterface.getWindowFlags().setCustomMarginTop(val, true);
                    break;
            }
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
                    mainActivityInterface.getWindowFlags().setCustomMarginLeft(val,false);
                    break;
                case "marginRight":
                    myView.rightMargin.setHint(hint);
                    mainActivityInterface.getWindowFlags().setCustomMarginRight(val,false);
                    break;
                case "marginBottom":
                    myView.bottomMargin.setHint(hint);
                    mainActivityInterface.getWindowFlags().setCustomMarginBottom(val,false);
                    break;
                case "marginTop":
                    myView.topMargin.setHint(hint);
                    mainActivityInterface.getWindowFlags().setCustomMarginTop(val,false);
                    break;
            }

            mainActivityInterface.getWindowFlags().setMargins();
            mainActivityInterface.updateMargins();
        }
    }

    private void checkVisibilityChange() {
        if (mainActivityInterface.getWindowFlags().getImmersiveMode()) {
            myView.navBarKeepSpace.setVisibility(View.VISIBLE);
            if (mainActivityInterface.getWindowFlags().getHasCutouts()) {
                myView.ignoreCutouts.setVisibility(View.VISIBLE);
            }
        } else {
            myView.navBarKeepSpace.setVisibility(View.GONE);
            myView.ignoreCutouts.setVisibility(View.GONE);
        }
    }
}

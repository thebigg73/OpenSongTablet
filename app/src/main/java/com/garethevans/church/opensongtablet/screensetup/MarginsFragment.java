package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MarginsFragment";
    private SettingsMarginsBinding myView;
    private String margins_string="", website_margins_string="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(margins_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

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

        prepareStrings();
        webAddress = website_margins_string;

        setupViews();

        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            margins_string = getString(R.string.margins);
            website_margins_string = getString(R.string.website_margins);
        }
    }
    private void setupViews() {
        myView.ignoreCutouts.setChecked(mainActivityInterface.getWindowFlags().getIgnoreCutouts());
        myView.ignoreRoundedCorners.setChecked(mainActivityInterface.getWindowFlags().getIgnoreRoundedCorners());
        myView.navBarKeepSpace.setChecked(mainActivityInterface.getWindowFlags().getNavBarKeepSpace());
        myView.immersiveMode.setChecked(mainActivityInterface.getWindowFlags().getImmersiveMode());
        myView.gestureNavigation.setVisibility(myView.navBarKeepSpace.getChecked() ? View.VISIBLE:View.GONE);
        myView.gestureNavigation.setChecked(mainActivityInterface.getWindowFlags().getGestureNavigation());

        myView.actionbarLeft.setValue(mainActivityInterface.getWindowFlags().getMarginToolbarLeft());
        myView.actionbarRight.setValue(mainActivityInterface.getWindowFlags().getMarginToolbarRight());
        myView.leftMargin.setValue(mainActivityInterface.getWindowFlags().getCustomMarginLeft());
        myView.rightMargin.setValue(mainActivityInterface.getWindowFlags().getCustomMarginRight());
        myView.topMargin.setValue(mainActivityInterface.getWindowFlags().getCustomMarginTop());
        myView.bottomMargin.setValue(mainActivityInterface.getWindowFlags().getCustomMarginBottom());

        myView.actionbarLeft.setLabelFormatter(value -> (int) value + " px");
        myView.actionbarRight.setLabelFormatter(value -> (int) value + " px");
        myView.leftMargin.setLabelFormatter(value -> (int) value + " px");
        myView.rightMargin.setLabelFormatter(value -> (int) value + " px");
        myView.topMargin.setLabelFormatter(value -> (int) value + " px");
        myView.bottomMargin.setLabelFormatter(value -> (int) value + " px");

        myView.actionbarLeft.setHint(mainActivityInterface.getWindowFlags().getMarginToolbarLeft() + "px");
        myView.actionbarRight.setHint(mainActivityInterface.getWindowFlags().getMarginToolbarRight() + "px");
        myView.leftMargin.setHint(mainActivityInterface.getWindowFlags().getCustomMarginLeft() + " px");
        myView.rightMargin.setHint(mainActivityInterface.getWindowFlags().getCustomMarginRight() + " px");
        myView.topMargin.setHint(mainActivityInterface.getWindowFlags().getCustomMarginTop() + " px");
        myView.bottomMargin.setHint(mainActivityInterface.getWindowFlags().getCustomMarginBottom() + " px");

        checkVisibilityChange();
    }

    private void setupListeners() {
        myView.actionbarLeft.addOnSliderTouchListener(new MySliderTouch("marginToolbarLeft"));
        myView.actionbarRight.addOnSliderTouchListener(new MySliderTouch("marginToolbarRight"));
        myView.leftMargin.addOnSliderTouchListener(new MySliderTouch("marginLeft"));
        myView.rightMargin.addOnSliderTouchListener(new MySliderTouch("marginRight"));
        myView.topMargin.addOnSliderTouchListener(new MySliderTouch("marginTop"));
        myView.bottomMargin.addOnSliderTouchListener(new MySliderTouch("marginBottom"));
        myView.actionbarLeft.addOnChangeListener(new MySliderChange("marginToolbarLeft"));
        myView.actionbarRight.addOnChangeListener(new MySliderChange("marginToolbarRight"));
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
            if (!isChecked) {
                myView.getRoot().setTranslationY(mainActivityInterface.getWindowFlags().getStatusHeight());
            } else {
                myView.getRoot().setTranslationY(0);
            }

        });
        myView.navBarKeepSpace.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getWindowFlags().setNavBarKeepSpace(isChecked);
            mainActivityInterface.getWindowFlags().hideOrShowSystemBars();
            mainActivityInterface.getWindowFlags().setMargins();
            mainActivityInterface.updateMargins();
            myView.gestureNavigation.setVisibility(isChecked ? View.VISIBLE:View.GONE);
        });
        myView.gestureNavigation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getWindowFlags().setGestureNavigation(isChecked);
            mainActivityInterface.getWindowFlags().checkNavBarHeight();
            mainActivityInterface.getWindowFlags().hideOrShowSystemBars();
            mainActivityInterface.getWindowFlags().setMargins();
            mainActivityInterface.updateMargins();
        });
        myView.ignoreCutouts.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            mainActivityInterface.getWindowFlags().setIgnoreCutouts(isChecked);
            mainActivityInterface.getWindowFlags().hideOrShowSystemBars();
            mainActivityInterface.getWindowFlags().setMargins();
            myView.ignoreCutouts.postDelayed(() -> {
                // Need a delay so this works properly due to hiding/showing animation of the bars
                // First pass for quick animation
                mainActivityInterface.updateMargins();
            },500);
            myView.ignoreCutouts.postDelayed(() -> {
                // Need a delay so this works properly due to hiding/showing animation of the bars
                // Second pass incase the first was too early
                mainActivityInterface.updateMargins();
            },1000);
        }));
        myView.ignoreRoundedCorners.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getWindowFlags().setIgnoreRoundedCorners(isChecked);
            mainActivityInterface.getWindowFlags().hideOrShowSystemBars();
            mainActivityInterface.getWindowFlags().setMargins();
            mainActivityInterface.updateMargins();
        });
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
            String hint = (val + " px");

            Log.d(TAG,"saving: "+val);
            switch (pref) {
                case "marginToolbarLeft":
                    myView.actionbarLeft.setHint(hint);
                    mainActivityInterface.getWindowFlags().setMarginToolbarLeft(val, true);
                    break;
                case "marginToolbarRight":
                    myView.actionbarRight.setHint(hint);
                    mainActivityInterface.getWindowFlags().setMarginToolbarRight(val, true);
                    break;
                case "marginLeft":
                    myView.leftMargin.setHint(hint);
                    mainActivityInterface.getWindowFlags().setCustomMarginLeft(val, true);
                    break;
                case "marginRight":
                    myView.rightMargin.setHint(hint);
                    mainActivityInterface.getWindowFlags().setCustomMarginRight(val, true);
                    break;
                case "marginBottom":
                    myView.bottomMargin.setHint(hint);
                    mainActivityInterface.getWindowFlags().setCustomMarginBottom(val, true);
                    break;
                case "marginTop":
                    myView.topMargin.setHint(hint);
                    mainActivityInterface.getWindowFlags().setCustomMarginTop(val, true);
                    break;
            }
            mainActivityInterface.getWindowFlags().setMargins();
            mainActivityInterface.updateMargins();
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
                case "marginToolbarLeft":
                    myView.actionbarLeft.setHint(hint);
                    mainActivityInterface.getWindowFlags().setMarginToolbarLeft(val,false);
                    break;
                case "marginToolbarRight":
                    myView.actionbarRight.setHint(hint);
                    mainActivityInterface.getWindowFlags().setMarginToolbarRight(val,false);
                    break;
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
        } else {
            myView.navBarKeepSpace.setVisibility(View.GONE);
        }

        if (mainActivityInterface.getWindowFlags().getHasCutouts()) {
            myView.ignoreCutouts.setVisibility(View.VISIBLE);
        }

        if (mainActivityInterface.getWindowFlags().getHasRoundedCorners()) {
            myView.ignoreRoundedCorners.setVisibility(View.VISIBLE);
        } else {
            myView.ignoreRoundedCorners.setVisibility(View.GONE);
        }

        if (mainActivityInterface.getWindowFlags().getHasCutouts()) {
            myView.ignoreCutouts.setVisibility(View.VISIBLE);
            myView.actionbarLeft.setVisibility(View.VISIBLE);
            myView.actionbarRight.setVisibility(View.VISIBLE);
        } else {
            myView.ignoreCutouts.setVisibility(View.GONE);
            myView.actionbarLeft.setVisibility(View.GONE);
            myView.actionbarRight.setVisibility(View.GONE);
        }
    }
}

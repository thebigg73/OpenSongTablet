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
import com.garethevans.church.opensongtablet.customviews.MaterialSlider;
import com.garethevans.church.opensongtablet.databinding.SettingsDisplayScalingBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class DisplayScalingFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsDisplayScalingBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsDisplayScalingBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(getString(R.string.scaling));

        // Set up the views
        setViews();

        // Set up the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setViews() {
        // The switches
        setAutoscaleMode();
        myView.scaleColumns.setChecked(getChecked("songAutoScaleColumnMaximise",true));
        myView.overrideFull.setChecked(getChecked("songAutoScaleOverrideFull",true));
        myView.overrideWidthSwitch.setChecked(getChecked("songAutoScaleOverrideWidth",false));

        // The seekbars
        setSliderValue(myView.manualFontSize,"fontSize",20.0f,1,"px");
        setSliderValue(myView.minFontSize,"fontSizeMin",10.0f,1,"px");
        setSliderValue(myView.maxFontSize,"fontSizeMax",50.0f,1,"px");
        setSliderValue(myView.scaleHeading,"scaleHeadings",0.6f,100,"%");
        setSliderValue(myView.scaleChords,"scaleChords",0.8f,100,"%");
        setSliderValue(myView.scaleComments,"scaleChords",0.8f,100,"%");
    }

    private void setAutoscaleMode() {
        // Autoscale can be Y(es) W(idth) N(o)
        String mode = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"songAutoScale","W");
        switch (mode) {
            case "Y":
                modeSwitches(true,false);
                visibilityByBoolean(myView.manualFontSize,false);
                visibilityByBoolean(myView.autoFontSizeLayout,true);
                visibilityByBoolean(myView.overrideFull,true);
                visibilityByBoolean(myView.overrideWidthSwitch,true);
                break;
            case "W":
                modeSwitches(true,true);
                visibilityByBoolean(myView.manualFontSize,false);
                visibilityByBoolean(myView.autoFontSizeLayout,true);
                visibilityByBoolean(myView.overrideFull,false);
                visibilityByBoolean(myView.overrideWidthSwitch,true);
                break;
            case "N":
                modeSwitches(false,false);
                visibilityByBoolean(myView.manualFontSize,true);
                visibilityByBoolean(myView.autoFontSizeLayout,false);
                break;
        }
    }
    private void modeSwitches(boolean useAutoScale, boolean widthOnly) {
        myView.useAutoscale.setChecked(useAutoScale);
        myView.scaleWidth.setChecked(widthOnly);
    }
    private void visibilityByBoolean(View view, boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void setSliderValue(MaterialSlider slider, String prefName, float fallback,
                                int multiplier, String unit) {
        // Get the float (% values need to be scaled by 100 multiplier
        float val = multiplier * mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),prefName,fallback);
        updateHint(slider,val,unit);
        slider.setValue(val);
    }

    private void updateHint(MaterialSlider slider, float size, String unit) {
        if (unit.equals("px")) {
            slider.setHintTextSize(size);
        }
        String hint = (int)size + unit;
        slider.setHint(hint);
    }

    private void getAutoscaleMode() {
        // Autoscale can be Y(es) W(idth) N(o)
        boolean useAutoscale = myView.useAutoscale.isChecked();
        boolean scaleWidth = myView.scaleWidth.isChecked();
        String val;
        if (useAutoscale && scaleWidth) {
            val = "W";
            visibilityByBoolean(myView.manualFontSize,false);
            visibilityByBoolean(myView.autoFontSizeLayout,true);
            visibilityByBoolean(myView.overrideFull,false);
            visibilityByBoolean(myView.overrideWidthSwitch,true);
        } else if (useAutoscale) {
            val = "Y";
            visibilityByBoolean(myView.manualFontSize,false);
            visibilityByBoolean(myView.autoFontSizeLayout,true);
            visibilityByBoolean(myView.overrideFull,true);
            visibilityByBoolean(myView.overrideWidthSwitch,true);
        } else {
            val = "N";
            visibilityByBoolean(myView.manualFontSize,true);
            visibilityByBoolean(myView.autoFontSizeLayout,false);
        }
        mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),"songAutoScale",val);
    }

    private boolean getChecked(String prefName, boolean fallback) {
        return mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),prefName,fallback);
    }
    private void checkMinMaxSizes() {
        // If the min size is bigger than the max size, then swap them
        float minSize = myView.minFontSize.getValue();
        float maxSize = myView.maxFontSize.getValue();
        if (minSize>maxSize) {
            myView.minFontSize.setValue(maxSize);
            myView.maxFontSize.setValue(minSize);
        }
    }

    private void setListeners() {
        // The switches
        myView.useAutoscale.setOnCheckedChangeListener((buttonView, isChecked) -> getAutoscaleMode());
        myView.scaleWidth.setOnCheckedChangeListener(((buttonView, isChecked) -> getAutoscaleMode()));
        myView.scaleColumns.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("songAutoScaleColumnMaximise",isChecked));
        myView.overrideFull.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("songAutoScaleOverrideFull",isChecked));
        myView.overrideWidthSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("songAutoScaleOverrideWidth",isChecked));

        // The sliders
        setSliderListeners(myView.manualFontSize, "fontSize", 1.0f, "px");
        setSliderListeners(myView.minFontSize, "fontSizeMin", 1.0f, "px");
        setSliderListeners(myView.maxFontSize, "fontSizeMax", 1.0f, "px");
        setSliderListeners(myView.scaleHeading, "scaleHeadings", 100.f, "%");
        setSliderListeners(myView.scaleChords, "scaleChords", 100.f, "%");
        setSliderListeners(myView.scaleComments, "scaleComments", 100.f, "%");
    }

    private void updateBooleanPreference(String prefName, boolean isChecked) {
        mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),prefName,isChecked);
    }

    private void updateSlider(MaterialSlider slider, String prefName, float multiplier, String unit) {
        // The float to store could be out of 100, or 1.  Use the multiplier to convert
        float sliderVal = slider.getValue();
        mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),prefName, sliderVal/multiplier);
        updateHint(slider, sliderVal, unit);
    }

    private void setSliderListeners(MaterialSlider slider, String pref, float multiplier, String unit) {
        slider.addOnSliderTouchListener(new MyOnSliderTouchListener(slider,pref,unit,multiplier));
        slider.addOnChangeListener(new MyOnChangeListener(slider,unit));
    }
    private class MyOnSliderTouchListener implements Slider.OnSliderTouchListener {
        MaterialSlider materialSlider;
        String pref;
        String unit;
        float multiplier;

        MyOnSliderTouchListener (MaterialSlider materialSlider, String pref, String unit, float multiplier) {
            this.materialSlider = materialSlider;
            this.pref = pref;
            this.unit = unit;
            this.multiplier = multiplier;
        }
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) { }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            updateSlider(materialSlider,pref, multiplier,unit);
            // If we changed the min and max font sizes, make sure they are the right way
            if (materialSlider==myView.minFontSize || materialSlider==myView.maxFontSize) {
                checkMinMaxSizes();
            }
        }
    }
    private class MyOnChangeListener implements Slider.OnChangeListener {
        MaterialSlider materialSlider;
        String unit;

        MyOnChangeListener (MaterialSlider materialSlider, String unit) {
            this.materialSlider = materialSlider;
            this.unit = unit;
        }
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            updateHint(materialSlider,value,unit);
        }
    }
}
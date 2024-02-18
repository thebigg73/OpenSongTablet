package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsSetsInlineBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class InlineSetFragment extends Fragment {
    // This class gives the settings for displaying an inline set list

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "InlineSetFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsSetsInlineBinding myView;
    private String set_inline_string="", website_inline_set_string="", performance_mode_string="",
            stage_mode_string="", presenter_mode_string="";
    private String webAddress;
    boolean inlineSet, isInlineSetPresenter;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(set_inline_string);
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
        myView = SettingsSetsInlineBinding.inflate(inflater,container,false);

        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            prepareStrings();
            webAddress = website_inline_set_string;

            // Set up the views
            setupViews();

            // Set up listeners
            setupListeners();

            // Check the hot zone warnings
            checkHotZoneConflict();
        });

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            set_inline_string = getString(R.string.set_inline);
            website_inline_set_string = getString(R.string.website_inline_set);
            performance_mode_string = getString(R.string.performance_mode);
            stage_mode_string = getString(R.string.stage_mode);
            presenter_mode_string = getString(R.string.presenter_mode);
        }
    }
    private void setupViews() {
        String text1 = set_inline_string + " (" + performance_mode_string +
                " / " + stage_mode_string + ")";
        String text2 = set_inline_string + " (" + presenter_mode_string + ")";

        int value1 = (int)(mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidth",0.20f)*100);
        int value2 = (int)(mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidthPresenter",0.3f)*100);
        int value3 = (int)(mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetTextSize",12f));
        int value4 = (int)(mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetTextSizePresenter",12f));

        mainActivityInterface.getMainHandler().post(() -> {
            myView.showInlineSet.setText(text1);
            myView.showInlineSet.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("inlineSet",false));
            myView.showInlineSetPresenter.setText(text2);
            myView.showInlineSetPresenter.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("inlineSetPresenter",true));
            myView.widthSlider.setLabelFormatter(value -> ((int)value)+"%");
            myView.widthSlider.setValue(value1);
            myView.widthSlider.setHint(value1+"%");
            myView.widthSliderPresenter.setLabelFormatter(value -> ((int)value)+"%");
            myView.widthSliderPresenter.setValue(value2);
            myView.widthSliderPresenter.setHint(value2+"%");
            myView.textSizeSlider.setLabelFormatter(value -> ((int)value)+"sp");
            myView.textSizeSlider.setValue(value3);
            myView.textSizeSlider.setHint(value3+"sp");
            myView.textSizeSlider.setHintTextSize(value3);
            myView.textSizeSliderPresenter.setLabelFormatter(value -> ((int)value)+"sp");
            myView.textSizeSliderPresenter.setValue(value4);
            myView.textSizeSliderPresenter.setHint(value4+"sp");
            myView.textSizeSliderPresenter.setHintTextSize(value4);
            checkChanged(null,myView.sliderLayout,myView.showInlineSet.getChecked());
            checkChanged(null,myView.sliderLayoutPresenter,myView.showInlineSetPresenter.getChecked());
        });
    }

    private void setupListeners() {
        mainActivityInterface.getMainHandler().post(() -> {
            myView.showInlineSet.setOnCheckedChangeListener((buttonView, isChecked) -> checkChanged("inlineSet",myView.sliderLayout,isChecked));
            myView.showInlineSetPresenter.setOnCheckedChangeListener((buttonView, isChecked) -> checkChanged("inlineSetPresenter",myView.sliderLayoutPresenter,isChecked));
            myView.widthSlider.addOnChangeListener(new MyChangeListener("performance"));
            myView.widthSliderPresenter.addOnChangeListener(new MyChangeListener("presenter"));
            myView.widthSlider.addOnSliderTouchListener(new MySliderTouchListener("inlineSetWidth"));
            myView.widthSliderPresenter.addOnSliderTouchListener(new MySliderTouchListener("inlineSetWidthPresenter"));
            myView.textSizeSlider.addOnChangeListener(new MyChangeListener("inlineSetTextSize"));
            myView.textSizeSliderPresenter.addOnChangeListener(new MyChangeListener("inlineSetTextSizePresenter"));
            myView.textSizeSlider.addOnSliderTouchListener(new MySliderTouchListener("inlineSetTextSize"));
            myView.textSizeSliderPresenter.addOnSliderTouchListener(new MySliderTouchListener("inlineSetTextSizePresenter"));
        });
    }

    private void checkChanged(String pref, LinearLayout linearLayout, boolean isChecked) {
        if (pref!=null) {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(pref, isChecked);
        }
        linearLayout.post(() -> linearLayout.setVisibility(isChecked ? View.VISIBLE:View.GONE));

        checkHotZoneConflict();
    }
    private class MyChangeListener implements Slider.OnChangeListener {
        private final String which;
        MyChangeListener(String which) {
            this.which = which;
        }
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            String hint1 = (int)value + "%";
            String hint2 = (int)value + "sp";
            switch (which) {
                case "performance":
                default:
                    myView.widthSlider.setHint(hint1);
                    break;
                case "presenter":
                    myView.widthSliderPresenter.setHint(hint1);
                    break;
                case "inlineSetTextSize":
                    myView.textSizeSlider.setHint(hint2);
                    myView.textSizeSlider.setHintTextSize(value);
                    break;
                case "inlineSetTextSizePresenter":
                    myView.textSizeSliderPresenter.setHint(hint2);
                    myView.textSizeSliderPresenter.setHintTextSize(value);
                    break;
            }
            checkHotZoneConflict();
        }
    }
    private class MySliderTouchListener implements Slider.OnSliderTouchListener {
        private final String pref;
        MySliderTouchListener(String pref) {
            this.pref = pref;
        }
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {}

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            switch (pref) {
                case "inlineSetWidth":
                default:
                    mainActivityInterface.getPreferences().setMyPreferenceFloat("inlineSetWidth", myView.widthSlider.getValue() / 100f);
                    break;
                case "inlineSetWidthPresenter":
                    mainActivityInterface.getPreferences().setMyPreferenceFloat("inlineSetWidthPresenter", myView.widthSliderPresenter.getValue() / 100f);
                    break;
                case "inlineSetTextSize":
                    mainActivityInterface.getPreferences().setMyPreferenceFloat("inlineSetTextSize",myView.textSizeSlider.getValue());
                    break;
                case "inlineSetTextSizePresenter":
                    mainActivityInterface.getPreferences().setMyPreferenceFloat("inlineSetTextSizePresenter",myView.textSizeSliderPresenter.getValue());
                    break;
            }
            checkHotZoneConflict();
        }
    }

    private void checkHotZoneConflict() {
        myView.disableLeftHotZone.post(() -> myView.disableLeftHotZone.setVisibility(myView.showInlineSet.getChecked() ? View.VISIBLE:View.GONE));
        myView.disableCenterHotZone.post(() -> myView.disableCenterHotZone.setVisibility((myView.showInlineSet.getChecked() && myView.widthSlider.getValue()>45) ? View.VISIBLE:View.GONE));
    }
}

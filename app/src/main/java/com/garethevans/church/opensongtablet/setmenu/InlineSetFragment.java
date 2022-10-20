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

    private MainActivityInterface mainActivityInterface;
    private SettingsSetsInlineBinding myView;

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

        mainActivityInterface.updateToolbar(getString(R.string.set_inline));
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_inline_set));

        // Set up the views
        setupViews();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        String text = getString(R.string.set_inline) + " (" + getString(R.string.performance_mode) +
                " / " + getString(R.string.stage_mode) + ")";
        myView.showInlineSet.setText(text);
        text = getString(R.string.set_inline) + " (" + getString(R.string.presenter_mode) + ")";
        myView.showInlineSetPresenter.setText(text);

        myView.showInlineSet.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("inlineSet",false));
        myView.showInlineSetPresenter.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("inlineSetPresenter",true));

        myView.widthSlider.setLabelFormatter(value -> ((int)value)+"%");
        myView.widthSliderPresenter.setLabelFormatter(value -> ((int)value)+"%");

        int value = (int)(mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidth",0.3f)*100);
        myView.widthSlider.setValue(value);
        myView.widthSlider.setHint(value+"%");
        value = (int)(mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidthPresenter",0.3f)*100);
        myView.widthSliderPresenter.setValue(value);
        myView.widthSliderPresenter.setHint(value+"%");

        checkChanged(null,myView.sliderLayout,myView.showInlineSet.getChecked());
        checkChanged(null,myView.sliderLayoutPresenter,myView.showInlineSetPresenter.getChecked());
    }

    private void setupListeners() {
        myView.showInlineSet.setOnCheckedChangeListener((buttonView, isChecked) -> checkChanged("inlineSet",myView.sliderLayout,isChecked));
        myView.showInlineSetPresenter.setOnCheckedChangeListener((buttonView, isChecked) -> checkChanged("inlineSetPresenter",myView.sliderLayoutPresenter,isChecked));

        myView.widthSlider.addOnChangeListener(new MyChangeListener("performance"));
        myView.widthSliderPresenter.addOnChangeListener(new MyChangeListener("presenter"));

        myView.widthSlider.addOnSliderTouchListener(new MySliderTouchListener("inlineSetWidth"));
        myView.widthSliderPresenter.addOnSliderTouchListener(new MySliderTouchListener("inlineSetWidthPresenter"));
    }

    private void checkChanged(String pref, LinearLayout linearLayout, boolean isChecked) {
        if (pref!=null) {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(pref, isChecked);
        }
        if (isChecked) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
    }
    private class MyChangeListener implements Slider.OnChangeListener {
        private final String which;
        MyChangeListener(String which) {
            this.which = which;
        }
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            String hint = (int)value + "%";
            switch (which) {
                case "performance":
                default:
                    myView.widthSlider.setHint(hint);
                    break;
                case "presenter":
                    myView.widthSliderPresenter.setHint(hint);
                    break;
            }
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
            }
        }
    }
}

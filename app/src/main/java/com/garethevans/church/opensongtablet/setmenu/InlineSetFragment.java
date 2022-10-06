package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        myView.showInlineSet.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("inlineSet",false));
        myView.widthSlider.setLabelFormatter(value -> ((int)value)+"%");
        int value = (int)(mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidth",0.3f)*100);
        myView.widthSlider.setValue(value);
        myView.widthSlider.setHint(value+"%");
        if (myView.showInlineSet.getChecked()) {
            myView.sliderLayout.setVisibility(View.VISIBLE);
        } else {
            myView.sliderLayout.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        myView.showInlineSet.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("inlineSet",isChecked);
            if (isChecked) {
                myView.sliderLayout.setVisibility(View.VISIBLE);
            } else {
                myView.sliderLayout.setVisibility(View.GONE);
            }
        });
        myView.widthSlider.addOnChangeListener((slider, value, fromUser) -> myView.widthSlider.setHint((int)value+"%"));
        myView.widthSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getPreferences().setMyPreferenceFloat("inlineSetWidth",myView.widthSlider.getValue()/100f);
            }
        });
    }
}

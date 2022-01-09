package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.databinding.ModePresenterSettingsBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class SettingsFragment extends Fragment {

    private final String TAG = "SettingsFragment";
    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private ModePresenterSettingsBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        displayInterface = (DisplayInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = ModePresenterSettingsBinding.inflate(inflater,container,false);

        // Update the currently chosen logo and background
        mainActivityInterface.getPresenterSettings().getAllPreferences(requireContext(),mainActivityInterface);
        updateLogo();
        updateBackground();

        // Set initial views
        setViews();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    public void updateLogo() {
        Log.d(TAG,"updateLogo()");
        // Get the current logo and preview in the button
        RequestOptions options = new RequestOptions().override(128, 72).centerInside();
        GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getLogo()).apply(options).into(myView.currentLogo);
        myView.logoSize.setValue(100*mainActivityInterface.getPresenterSettings().getLogoSize());
    }

    public void updateBackground() {
        Log.d(TAG,"updateBackground()");
        // Get the current backgrounds and update the chosen one into the button
        RequestOptions options = new RequestOptions().override(128, 72).centerInside();
        switch (mainActivityInterface.getPresenterSettings().getBackgroundToUse()) {
            case "img1":
                GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundImage1()).apply(options).into(myView.currentBackground);
                myView.videoBackgroundIcon.hide();
                Log.d(TAG,"should update to img1");
                break;
            case "img2":
                GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundImage2()).apply(options).into(myView.currentBackground);
                myView.videoBackgroundIcon.hide();
                break;
            case "vid1":
                GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundVideo1()).apply(options).into(myView.currentBackground);
                myView.videoBackgroundIcon.show();
                break;
            case "vid2":
                GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundVideo2()).apply(options).into(myView.currentBackground);
                myView.videoBackgroundIcon.show();
                break;
            case "color":
                Drawable drawable = ContextCompat.getDrawable(requireContext(),R.drawable.simple_rectangle);
                if (drawable!=null) {
                    GradientDrawable gradientDrawable = (GradientDrawable) drawable.mutate();
                    gradientDrawable.setColor(mainActivityInterface.getPresenterSettings().getBackgroundColor());
                    GlideApp.with(requireContext()).load(gradientDrawable).apply(options).into(myView.currentBackground);
                }
                myView.videoBackgroundIcon.hide();
        }
    }


    private void setViews() {
        myView.presoBackgroundAlpha.setValue((int)(mainActivityInterface.getPresenterSettings().getPresoBackgroundAlpha()*100));
        myView.presoBackgroundAlpha.setHint((int)(mainActivityInterface.getPresenterSettings().getPresoBackgroundAlpha()*100)+"%");

        myView.logoSize.setValue((int)(mainActivityInterface.getPresenterSettings().getLogoSize()*100));
        myView.logoSize.setHint((int)(mainActivityInterface.getPresenterSettings().getLogoSize()*100) + "%");

        myView.crossFadeTime.setValue(mainActivityInterface.getPresenterSettings().getPresoTransitionTime());
        myView.crossFadeTime.setHint(mainActivityInterface.getPresenterSettings().getPresoTransitionTime() + "ms");

        myView.rotateDisplay.setValue(mainActivityInterface.getPresenterSettings().getCastRotation());
        myView.rotateDisplay.setHint((int)mainActivityInterface.getPresenterSettings().getCastRotation()+"°");

        myView.horizontalMargin.setValue(mainActivityInterface.getPresenterSettings().getPresoXMargin());
        myView.horizontalMargin.setHint((int)mainActivityInterface.getPresenterSettings().getPresoXMargin()+"px");
        myView.verticalMargin.setValue(mainActivityInterface.getPresenterSettings().getPresoYMargin());
        myView.verticalMargin.setHint((int)mainActivityInterface.getPresenterSettings().getPresoYMargin()+"px");
    }
    private void setListeners() {
        myView.currentBackground.setOnClickListener(view -> {
            ImageChooserBottomSheet imageChooserBottomSheet = new ImageChooserBottomSheet(this,"presenterFragmentSettings");
            imageChooserBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"ImageChooserBottomSheet");
        });

        myView.presoBackgroundAlpha.addOnSliderTouchListener(new SliderTouchListener("presoBackgroundAlpha"));
        myView.presoBackgroundAlpha.addOnChangeListener(new SliderChangeListener("presoBackgroundAlpha"));

        myView.logoSize.addOnSliderTouchListener(new SliderTouchListener("logoSize"));
        myView.logoSize.addOnChangeListener(new SliderChangeListener("logoSize"));

        myView.crossFadeTime.addOnSliderTouchListener(new SliderTouchListener("presoTransitionTime"));
        myView.crossFadeTime.addOnChangeListener(new SliderChangeListener("presoTransitionTime"));

        myView.rotateDisplay.addOnSliderTouchListener(new SliderTouchListener("castRotation"));
        myView.rotateDisplay.addOnChangeListener(new SliderChangeListener("castRotation"));

        myView.horizontalMargin.addOnSliderTouchListener(new SliderTouchListener("presoXMargin"));
        myView.horizontalMargin.addOnChangeListener(new SliderChangeListener("presoXMargin"));

        myView.verticalMargin.addOnSliderTouchListener(new SliderTouchListener("presoYMargin"));
        myView.verticalMargin.addOnChangeListener(new SliderChangeListener("presoYMargin"));
    }

    private float floatToDecPlaces(float floatNum, int decPlaces) {
        floatNum = floatNum * (float)Math.pow(10,decPlaces);
        floatNum = Math.round(floatNum);
        return floatNum / (float)Math.pow(10,decPlaces);
    }

    private class SliderTouchListener implements Slider.OnSliderTouchListener {

        private final String prefName;

        SliderTouchListener(String prefName) {
            this.prefName = prefName;
        }

        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {}

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            // Save the preference and update the screen
            switch (prefName) {
                case "logoSize":
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),
                            prefName, floatToDecPlaces(slider.getValue()/100f,2));
                    mainActivityInterface.getPresenterSettings().setLogoSize(floatToDecPlaces(slider.getValue()/100f,2));
                    displayInterface.updateDisplay("changeLogo");
                    break;
                case "presoTransitionTime":
                    // The slider gives values between 200 and 3000ms
                    mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(),
                            prefName, (int)slider.getValue());
                    mainActivityInterface.getPresenterSettings().setPresoTransitionTime((int)(slider.getValue()));
                    break;
                case "castRotation":
                    // The slider gives values between 0 and 360
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),
                            prefName, slider.getValue());
                    mainActivityInterface.getPresenterSettings().setCastRotation(slider.getValue());
                    displayInterface.updateDisplay("screenSizes");
                    break;
                case "presoXMargin":
                case "presoYMargin":
                    // The sliders gives values between -50 and 50
                    mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(),
                            prefName, (int)slider.getValue());
                    if (prefName.contains("X")) {
                        mainActivityInterface.getPresenterSettings().setPresoXMargin((int)slider.getValue());
                    } else {
                        mainActivityInterface.getPresenterSettings().setPresoYMargin((int)slider.getValue());
                    }
                    displayInterface.updateDisplay("screenSizes");
                    break;
                case "presoBackgroundAlpha":
                    // The slider goes from 0 to 100
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),
                            "presoBackgroundAlpha", slider.getValue()/100f);
                    mainActivityInterface.getPresenterSettings().setPresoBackgroundAlpha(slider.getValue()/100f);
                    displayInterface.updateDisplay("changeBackground");
                    break;
            }

        }
    }

    private class SliderChangeListener implements Slider.OnChangeListener {

        private final String prefName;
        SliderChangeListener(String prefName) {
            this.prefName = prefName;
        }

        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            // Don't save the preference, but update the text
            switch (prefName) {
                case "logoSize":
                    myView.logoSize.setHint((int)value + "%");
                    break;
                case "presoTransitionTime":
                    myView.crossFadeTime.setHint((int)value + "ms");
                    break;
                case "castRotation":
                    myView.rotateDisplay.setHint((int)value + "°");
                    break;
                case "presoXMargin":
                case "presoYMargin":
                    // The sliders gives values between -100 and 100
                    if (prefName.contains("X")) {
                        myView.horizontalMargin.setHint((int)slider.getValue() + "px");
                    } else {
                        myView.verticalMargin.setHint((int)slider.getValue() + "px");
                    }
                    break;
                case "presoBackgroundAlpha":
                    myView.presoBackgroundAlpha.setHint((int)slider.getValue() + "%");
                    break;
            }
        }
    }
}

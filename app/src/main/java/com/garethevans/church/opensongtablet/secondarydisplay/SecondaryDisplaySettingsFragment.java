package com.garethevans.church.opensongtablet.secondarydisplay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsDisplayConnectedBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.screensetup.ChooseColorBottomSheet;
import com.google.android.material.slider.Slider;

public class SecondaryDisplaySettingsFragment extends Fragment {

    private final String TAG = "SettingsFragment";
    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private SettingsDisplayConnectedBinding myView;
    private String connected_display_string="", website_connected_display_string="",
            mode_performance_string="", mode_stage_string="";

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
        myView = SettingsDisplayConnectedBinding.inflate(inflater,container,false);

        prepareStrings();

        mainActivityInterface.updateToolbar(connected_display_string);
        mainActivityInterface.updateToolbarHelp(website_connected_display_string);

        // Update the currently chosen logo and backgrounds
        mainActivityInterface.getPresenterSettings().getAllPreferences();
        updateLogo();
        updateBackground();
        updateInfoBackground();

        // Set initial views
        setViews();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            connected_display_string = getString(R.string.connected_display);
            website_connected_display_string = getString(R.string.website_connected_display);
            mode_performance_string = getString(R.string.mode_performance);
            mode_stage_string = getString(R.string.mode_stage);
        }
    }
    public void updateLogo() {
        Log.d(TAG,"updateLogo()");
        if (getContext()!=null) {
            // Get the current logo and preview in the button
            RequestOptions options = new RequestOptions().override(128, 72).centerInside();
            Glide.with(getContext()).load(mainActivityInterface.getPresenterSettings().getLogo()).apply(options).into(myView.currentLogo);
            myView.logoSize.setValue(100 * mainActivityInterface.getPresenterSettings().getLogoSize());
        }
    }

    public void updateBackground() {
        // Get the current backgrounds and update the chosen one into the button
        // Do this for the info background preview too
        if (getContext()!=null) {
            RequestOptions options = new RequestOptions().override(136, 72).centerCrop();
            switch (mainActivityInterface.getPresenterSettings().getBackgroundToUse()) {
                case "img1":
                    Glide.with(getContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundImage1()).apply(options).into(myView.currentBackground);
                    Glide.with(getContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundImage1()).apply(options).into(myView.infoBackgroundColor);
                    myView.videoBackgroundIcon.hide();
                    Log.d(TAG, "should update to img1");
                    break;
                case "img2":
                    Glide.with(getContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundImage2()).apply(options).into(myView.currentBackground);
                    Glide.with(getContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundImage2()).apply(options).into(myView.infoBackgroundColor);
                    myView.videoBackgroundIcon.hide();
                    break;
                case "vid1":
                    Glide.with(getContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundVideo1()).apply(options).into(myView.currentBackground);
                    Glide.with(getContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundVideo1()).apply(options).into(myView.infoBackgroundColor);
                    myView.videoBackgroundIcon.show();
                    break;
                case "vid2":
                    Glide.with(getContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundVideo2()).apply(options).into(myView.currentBackground);
                    Glide.with(getContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundVideo2()).apply(options).into(myView.infoBackgroundColor);
                    myView.videoBackgroundIcon.show();
                    break;
                case "color":
                    Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.simple_rectangle);
                    if (drawable != null) {
                        GradientDrawable gradientDrawable = (GradientDrawable) drawable.mutate();
                        gradientDrawable.setColor(mainActivityInterface.getPresenterSettings().getBackgroundColor());
                        Glide.with(getContext()).load(gradientDrawable).apply(options).into(myView.currentBackground);
                        Glide.with(getContext()).load(gradientDrawable).apply(options).into(myView.infoBackgroundColor);
                    }
                    myView.videoBackgroundIcon.hide();
            }
        }
    }

    public void updateInfoBackground() {
        // Update the info bar background colour
        if (getContext()!=null) {
            RequestOptions options = new RequestOptions().override(128, 72).centerInside();
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.simple_rectangle);
            if (drawable != null) {
                GradientDrawable gradientDrawable = (GradientDrawable) drawable.mutate();
                gradientDrawable.setColor(mainActivityInterface.getMyThemeColors().getPresoShadowColor());
                Glide.with(getContext()).load(gradientDrawable).apply(options).into(myView.infoBackgroundColor);
            }
        }
    }


    private void setViews() {
        // These settings can be called from any mode, hide what we don't need
        if (mainActivityInterface.getMode().equals(mode_performance_string)) {
            myView.backgroundLayout.setVisibility(View.GONE);
            myView.presoBackgroundAlpha.setVisibility(View.GONE);
            myView.presoBackgroundDivider.setVisibility(View.GONE);
            myView.logoLayout.setVisibility(View.GONE);
            myView.logoSize.setVisibility(View.GONE);
            myView.logoDivider.setVisibility(View.GONE);
            myView.contentHorizontalAlign.setVisibility(View.GONE);
            myView.contentVerticalAlign.setVisibility(View.GONE);
            myView.infoSizes.setVisibility(View.GONE);
            myView.blockShadow.setVisibility(View.GONE);
            myView.blockShadowAlpha.setVisibility(View.GONE);
        } else if (mainActivityInterface.getMode().equals(mode_stage_string)) {
            myView.logoLayout.setVisibility(View.GONE);
            myView.logoSize.setVisibility(View.GONE);
            myView.logoDivider.setVisibility(View.GONE);
        }

        myView.presoBackgroundAlpha.setValue((int)(mainActivityInterface.getPresenterSettings().getPresoBackgroundAlpha()*100));
        myView.presoBackgroundAlpha.setHint((int)(mainActivityInterface.getPresenterSettings().getPresoBackgroundAlpha()*100)+"%");
        myView.presoBackgroundAlpha.setLabelFormatter(value -> ((int)value)+"%");

        myView.logoSize.setValue((int)(mainActivityInterface.getPresenterSettings().getLogoSize()*100));
        myView.logoSize.setHint((int)(mainActivityInterface.getPresenterSettings().getLogoSize()*100) + "%");
        myView.logoSize.setLabelFormatter(value -> ((int)value)+"%");

        myView.crossFadeTime.setValue(mainActivityInterface.getPresenterSettings().getPresoTransitionTime());
        myView.crossFadeTime.setHint(mainActivityInterface.getPresenterSettings().getPresoTransitionTime() + "ms");
        myView.crossFadeTime.setLabelFormatter(value -> ((int)value)+"ms");

        myView.rotateDisplay.setValue(mainActivityInterface.getPresenterSettings().getCastRotation());
        myView.rotateDisplay.setHint((int)mainActivityInterface.getPresenterSettings().getCastRotation()+"°");
        myView.rotateDisplay.setLabelFormatter(value -> ((int)value)+"°");

        myView.horizontalMargin.setValue(mainActivityInterface.getPresenterSettings().getPresoXMargin());
        myView.horizontalMargin.setHint(mainActivityInterface.getPresenterSettings().getPresoXMargin() +"px");
        myView.horizontalMargin.setLabelFormatter(value -> ((int)value)+"px");
        myView.verticalMargin.setValue(mainActivityInterface.getPresenterSettings().getPresoYMargin());
        myView.verticalMargin.setHint(mainActivityInterface.getPresenterSettings().getPresoYMargin() +"px");
        myView.verticalMargin.setLabelFormatter(value -> ((int)value)+"px");

        myView.infoAlign.setSliderPos(gravityToSliderPosition(mainActivityInterface.getPresenterSettings().getPresoInfoAlign()));
        myView.hideInfoBar.setChecked(mainActivityInterface.getPresenterSettings().getHideInfoBar());

        myView.titleTextSize.setValue(mainActivityInterface.getPresenterSettings().getPresoTitleTextSize());
        myView.authorTextSize.setValue(mainActivityInterface.getPresenterSettings().getPresoAuthorTextSize());
        myView.copyrightTextSize.setValue(mainActivityInterface.getPresenterSettings().getPresoCopyrightTextSize());

        myView.clockOn.setChecked(mainActivityInterface.getPresenterSettings().getPresoShowClock());
        myView.clock24hr.setChecked(mainActivityInterface.getPresenterSettings().getPresoClock24h());
        myView.clockSeconds.setChecked(mainActivityInterface.getPresenterSettings().getPresoClockSeconds());
        myView.clockTextSize.setValue(mainActivityInterface.getPresenterSettings().getPresoClockSize());
        updateClockSettings();

        myView.maxFontSize.setValue(mainActivityInterface.getPresenterSettings().getFontSizePresoMax());
        myView.maxFontSize.setHint(((int)mainActivityInterface.getPresenterSettings().getFontSizePresoMax())+"sp");
        myView.maxFontSize.setLabelFormatter(value -> ((int)value)+"sp");

        myView.showChords.setChecked(mainActivityInterface.getPresenterSettings().getPresoShowChords());

        myView.contentHorizontalAlign.setSliderPos(gravityToSliderPosition(mainActivityInterface.getPresenterSettings().getPresoLyricsAlign()));
        myView.contentVerticalAlign.setSliderPos(gravityToSliderPosition(mainActivityInterface.getPresenterSettings().getPresoLyricsVAlign()));

        myView.blockShadow.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("blockShadow",false));
        myView.blockShadowAlpha.setValue((int)(100*mainActivityInterface.getPreferences().getMyPreferenceFloat("blockShadowAlpha",0.5f)));
        myView.blockShadowAlpha.setHint((int)(100*mainActivityInterface.getPreferences().getMyPreferenceFloat("blockShadowAlpha",0.5f)) + "%");
        myView.blockShadowAlpha.setLabelFormatter(value -> ((int)value)+"%");
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setListeners() {
        myView.currentBackground.setOnClickListener(view -> {
            ImageChooserBottomSheet imageChooserBottomSheet = new ImageChooserBottomSheet(this,"presenterFragmentSettings");
            imageChooserBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"ImageChooserBottomSheet");
        });
        myView.infoBackgroundColor.setOnClickListener(view -> {
            ChooseColorBottomSheet chooseColorBottomSheet = new ChooseColorBottomSheet(this, "presenterFragmentSettings", "presoShadowColor");
            chooseColorBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"ChooseColorBottomSheet");
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

        myView.infoAlign.addOnChangeListener(new SliderChangeListener("presoInfoAlign"));
        myView.hideInfoBar.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("hideInfoBar",b);
            mainActivityInterface.getPresenterSettings().setHideInfoBar(b);
            displayInterface.updateDisplay("initialiseInfoBarRequired");
            displayInterface.updateDisplay("checkSongInfoShowHide");
        });

        myView.titleTextSize.addOnChangeListener(new SliderChangeListener("presoTitleTextSize"));
        myView.titleTextSize.addOnSliderTouchListener(new SliderTouchListener("presoTitleTextSize"));
        myView.authorTextSize.addOnChangeListener(new SliderChangeListener("presoAuthorTextSize"));
        myView.authorTextSize.addOnSliderTouchListener(new SliderTouchListener("presoAuthorTextSize"));
        myView.copyrightTextSize.addOnChangeListener(new SliderChangeListener("presoCopyrightTextSize"));
        myView.copyrightTextSize.addOnSliderTouchListener(new SliderTouchListener("presoCopyrightTextSize"));

        myView.clockOn.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("presoShowClock",b);
            mainActivityInterface.getPresenterSettings().setPresoShowClock(b);
            updateClockSettings();
        });
        myView.clock24hr.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("presoClock24h",b);
            mainActivityInterface.getPresenterSettings().setPresoClock24h(b);
        });
        myView.clockSeconds.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("presoClockSeconds",b);
            mainActivityInterface.getPresenterSettings().setPresoClockSeconds(b);
        });
        myView.clockTextSize.addOnSliderTouchListener(new SliderTouchListener("presoClockSize"));
        myView.clockTextSize.addOnChangeListener(new SliderChangeListener("presoClockSize"));

        myView.maxFontSize.addOnSliderTouchListener(new SliderTouchListener("fontSizePresoMax"));
        myView.maxFontSize.addOnChangeListener(new SliderChangeListener("fontSizePresoMax"));

        myView.showChords.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("presoShowChords",b);
                mainActivityInterface.getPresenterSettings().setPresoShowChords(b);
                mainActivityInterface.updateFragment("presenterFragmentSongSections",null,null);
                displayInterface.updateDisplay("setSongContent");
                try {
                    if (mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getItemCount() > 0) {
                        mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().notifyItemRangeChanged(0,
                                mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getItemCount());
                    }
                } catch (Exception e) {
                        Log.d(TAG, "No song section adapter at this point.");
                }
        });
        myView.contentHorizontalAlign.addOnSliderTouchListener(new SliderTouchListener("presoLyricsAlign"));
        myView.contentHorizontalAlign.addOnChangeListener(new SliderChangeListener("presoLyricsAlign"));
        myView.contentVerticalAlign.addOnSliderTouchListener(new SliderTouchListener("presoLyricsVAlign"));
        myView.contentVerticalAlign.addOnChangeListener(new SliderChangeListener("presoLyricsVAlign"));

        myView.blockShadow.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("blockShadow",b);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
        });
        myView.blockShadowAlpha.addOnChangeListener(new SliderChangeListener("blockShadowAlpha"));
    }

    private void updateClockSettings() {
        int visibility = View.GONE;
        if (mainActivityInterface.getPresenterSettings().getPresoShowClock()) {
            visibility = View.VISIBLE;
        }
        myView.clock24hr.setVisibility(visibility);
        myView.clockSeconds.setVisibility(visibility);
        myView.clockTextSize.setVisibility(visibility);
    }
    private float floatToDecPlaces(float floatNum) {
        floatNum = floatNum * (float)Math.pow(10, 2);
        floatNum = Math.round(floatNum);
        return floatNum / (float)Math.pow(10, 2);
    }
    @SuppressLint("RtlHardcoded")
    private int gravityToSliderPosition(int gravity) {
        switch (gravity) {
            case Gravity.START:
            case Gravity.LEFT:
            case Gravity.TOP:
                return 0;
            case Gravity.CENTER:
            case Gravity.CENTER_HORIZONTAL:
            case Gravity.CENTER_VERTICAL:
            default:
                return 1;
            case Gravity.END:
            case Gravity.RIGHT:
            case Gravity.BOTTOM:
                return 2;
        }
    }
    private int sliderPositionToGravity(boolean vertical, int position) {


        switch (position) {
            case 0:
                if (vertical) {
                    Log.d(TAG,"top");
                    return Gravity.TOP;
                } else {
                    Log.d(TAG,"start");
                    return Gravity.START;
                }
            case 1:
            default:
                if (vertical) {
                    Log.d(TAG,"center vertical");
                    return Gravity.CENTER_VERTICAL;
                } else {
                    Log.d(TAG,"center horizontal");
                    return Gravity.CENTER_HORIZONTAL;
                }
            case 2:
                if (vertical) {
                    Log.d(TAG,"bottom");
                    return Gravity.BOTTOM;
                } else {
                    Log.d(TAG,"end");
                    return Gravity.END;
                }
        }
    }


    private class SliderTouchListener implements Slider.OnSliderTouchListener {

        private final String prefName;

        SliderTouchListener(String prefName) {
            this.prefName = prefName;
        }


        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
            //myView.nestedScrollView.setScrollingEnabled(false);
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            // Save the preference and update the screen
            //myView.nestedScrollView.setScrollingEnabled(true);
            switch (prefName) {
                case "logoSize":
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(
                            prefName, floatToDecPlaces(slider.getValue()/100f));
                    mainActivityInterface.getPresenterSettings().setLogoSize(floatToDecPlaces(slider.getValue()/100f));
                    displayInterface.updateDisplay("changeLogo");
                    break;
                case "presoTransitionTime":
                    // The slider gives values between 200 and 3000ms
                    mainActivityInterface.getPreferences().setMyPreferenceInt(
                            prefName, (int)slider.getValue());
                    mainActivityInterface.getPresenterSettings().setPresoTransitionTime((int)(slider.getValue()));
                    break;
                case "castRotation":
                    // The slider gives values between 0 and 360
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(
                            prefName, slider.getValue());
                    mainActivityInterface.getPresenterSettings().setCastRotation(slider.getValue());
                    displayInterface.updateDisplay("changeRotation");
                    break;
                case "presoXMargin":
                case "presoYMargin":
                    // The sliders gives values between -50 and 50
                    mainActivityInterface.getPreferences().setMyPreferenceInt(
                            prefName, (int)slider.getValue());
                    if (prefName.contains("X")) {
                        mainActivityInterface.getPresenterSettings().setPresoXMargin((int)slider.getValue());
                    } else {
                        mainActivityInterface.getPresenterSettings().setPresoYMargin((int)slider.getValue());
                    }
                    displayInterface.updateDisplay("changeRotation");
                    break;
                case "presoBackgroundAlpha":
                    // The slider goes from 0 to 100
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(
                            prefName, slider.getValue()/100f);
                    mainActivityInterface.getPresenterSettings().setPresoBackgroundAlpha(slider.getValue()/100f);
                    displayInterface.updateDisplay("changeBackground");
                    break;
                case "fontSizePresoMax":
                    // The slider goes from 10 to 100
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(
                            prefName, slider.getValue());
                    mainActivityInterface.getPresenterSettings().setFontSizePresoMax(slider.getValue());
                    displayInterface.updateDisplay("setSongContent");
                    break;
                case "blockShadowAlpha":
                    // The slider goes from 0 to 100
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(
                            prefName, slider.getValue()/100f);
                    mainActivityInterface.getProcessSong().updateProcessingPreferences();
                    displayInterface.updateDisplay("setSongContent");
                    break;
                case "presoClockSize":
                    // The slider goes from 6 to 22
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(
                            prefName, slider.getValue());
                    mainActivityInterface.getPresenterSettings().setPresoClockSize(slider.getValue());
                    displayInterface.updateDisplay("setSongInfo");
                    break;
                case "presoTitleTextSize":
                    // The slider goes from 6 to 22
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(
                            prefName,slider.getValue());
                    mainActivityInterface.getPresenterSettings().setPresoTitleTextSize(slider.getValue());
                    displayInterface.updateDisplay("setSongInfo");
                    break;
                case "presoAuthorTextSize":
                    // The slider goes from 6 to 22
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(
                            prefName,slider.getValue());
                    mainActivityInterface.getPresenterSettings().setPresoAuthorTextSize(slider.getValue());
                    displayInterface.updateDisplay("setSongInfo");
                    break;
                case "presoCopyrightTextSize":
                    // The slider goes from 6 to 22
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(
                            prefName,slider.getValue());
                    mainActivityInterface.getPresenterSettings().setPresoCopyrightTextSize(slider.getValue());
                    displayInterface.updateDisplay("setSongInfo");
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
            int gravity;
            //myView.nestedScrollView.setScrollingEnabled(false);

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
                case "presoInfoAlign":
                    // The slider goes from 0 to 2.  We need to look up the gravity
                    gravity = sliderPositionToGravity(false, (int) slider.getValue());
                    mainActivityInterface.getPreferences().setMyPreferenceInt(
                            "presoInfoAlign", gravity);
                    mainActivityInterface.getPresenterSettings().setPresoInfoAlign(gravity);
                    displayInterface.updateDisplay("changeInfoAlignment");
                    break;
                case "presoLyricsAlign":
                    // The slider goes from 0 to 2.  We need to look up the gravity
                    gravity = sliderPositionToGravity(false,(int)slider.getValue());
                    mainActivityInterface.getPreferences().setMyPreferenceInt(
                            "presoLyricsAlign", gravity);
                    mainActivityInterface.getPresenterSettings().setPresoLyricsAlign(gravity);
                    displayInterface.updateDisplay("setSongContent");
                    break;
                case "presoLyricsVAlign":
                    // The slider goes from 0 to 2.  We need to look up the gravity
                    gravity = sliderPositionToGravity(true,(int)slider.getValue());
                    mainActivityInterface.getPreferences().setMyPreferenceInt(
                            "presoLyricsVAlign", gravity);
                    mainActivityInterface.getPresenterSettings().setPresoLyricsVAlign(gravity);
                    displayInterface.updateDisplay("setSongContent");
                    break;
                case "fontSizePresoMax":
                    // The slider goes from 10 to 100 as the font size
                    myView.maxFontSize.setHint(((int)slider.getValue())+"sp");
                    break;
                case "blockShadowAlpha":
                    // The slider goes from 0 to 100
                    myView.blockShadowAlpha.setHint(((int)slider.getValue())+"%");
                    break;
                case "presoClockSize":
                    // The slider goes from 6 to 22
                    myView.clockTextSize.setHint(((int)slider.getValue())+"sp");
                    break;
                case "presoTitleTextSize":
                    // The slider goes from 6 to 22
                    myView.titleTextSize.setHint(((int)slider.getValue())+"sp");
                    break;
                case "presoAuthorTextSize":
                    // The slider goes from 6 to 22
                    myView.authorTextSize.setHint(((int)slider.getValue())+"sp");
                    break;
                case "presoCopyrightTextSize":
                    // The slider goes from 6 to 22
                    myView.copyrightTextSize.setHint(((int)slider.getValue())+"sp");
                    break;
            }
        }
    }

}

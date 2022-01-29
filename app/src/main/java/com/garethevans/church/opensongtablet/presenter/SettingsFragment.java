package com.garethevans.church.opensongtablet.presenter;

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

import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.databinding.ModePresenterSettingsBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.screensetup.ChooseColorBottomSheet;
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

        // Update the currently chosen logo and backgrounds
        mainActivityInterface.getPresenterSettings().getAllPreferences(requireContext(),mainActivityInterface);
        updateLogo();
        updateBackground();
        updateInfoBackground();

        // Set initial views
        setViews();

        // Set the listeners
        setListeners();

        Log.d(TAG,"start: "+Gravity.START+"  center_horizontal: "+Gravity.CENTER_HORIZONTAL+"  end:"+Gravity.END);
        Log.d(TAG,"top: "+Gravity.TOP+"  center_horizontal: "+Gravity.CENTER_VERTICAL+"  end:"+Gravity.BOTTOM);
        Log.d(TAG,"presoLyricsAlign: "+mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(),"presoLyricsAlign",Gravity.CENTER_HORIZONTAL));
        Log.d(TAG,"presoLyricsVAlign: "+mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(),"presoLyricsVAlign",Gravity.CENTER_VERTICAL));
        Log.d(TAG, "presentersettings align: "+mainActivityInterface.getPresenterSettings().getPresoLyricsAlign()+"  valign: "+mainActivityInterface.getPresenterSettings().getPresoLyricsVAlign());

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
        // Get the current backgrounds and update the chosen one into the button
        // Do this for the info background preview too
        RequestOptions options = new RequestOptions().override(128, 72).centerInside();
        switch (mainActivityInterface.getPresenterSettings().getBackgroundToUse()) {
            case "img1":
                GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundImage1()).apply(options).into(myView.currentBackground);
                GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundImage1()).apply(options).into(myView.infoBackgroundColor);
                myView.videoBackgroundIcon.hide();
                Log.d(TAG,"should update to img1");
                break;
            case "img2":
                GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundImage2()).apply(options).into(myView.currentBackground);
                GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundImage2()).apply(options).into(myView.infoBackgroundColor);
                myView.videoBackgroundIcon.hide();
                break;
            case "vid1":
                GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundVideo1()).apply(options).into(myView.currentBackground);
                GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundVideo1()).apply(options).into(myView.infoBackgroundColor);
                myView.videoBackgroundIcon.show();
                break;
            case "vid2":
                GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundVideo2()).apply(options).into(myView.currentBackground);
                GlideApp.with(requireContext()).load(mainActivityInterface.getPresenterSettings().getBackgroundVideo2()).apply(options).into(myView.infoBackgroundColor);
                myView.videoBackgroundIcon.show();
                break;
            case "color":
                Drawable drawable = ContextCompat.getDrawable(requireContext(),R.drawable.simple_rectangle);
                if (drawable!=null) {
                    GradientDrawable gradientDrawable = (GradientDrawable) drawable.mutate();
                    gradientDrawable.setColor(mainActivityInterface.getPresenterSettings().getBackgroundColor());
                    GlideApp.with(requireContext()).load(gradientDrawable).apply(options).into(myView.currentBackground);
                    GlideApp.with(requireContext()).load(gradientDrawable).apply(options).into(myView.infoBackgroundColor);
                }
                myView.videoBackgroundIcon.hide();
        }
    }

    public void updateInfoBackground() {
        // Update the info bar background colour
        RequestOptions options = new RequestOptions().override(128, 72).centerInside();
        Drawable drawable = ContextCompat.getDrawable(requireContext(),R.drawable.simple_rectangle);
        if (drawable!=null) {
            GradientDrawable gradientDrawable = (GradientDrawable) drawable.mutate();
            gradientDrawable.setColor(mainActivityInterface.getMyThemeColors().getPresoShadowColor());
            GlideApp.with(requireContext()).load(gradientDrawable).apply(options).into(myView.currentInfoBackground);
        }
    }


    private void setViews() {
        // These settings can be called from any mode, hide what we don't need
        switch (mainActivityInterface.getMode()) {
            case "Performance":
                myView.backgroundLayout.setVisibility(View.GONE);
                myView.logoLayout.setVisibility(View.GONE);
                myView.contentHorizontalAlign.setVisibility(View.GONE);
                myView.contentVerticalAlign.setVisibility(View.GONE);
                myView.blockShadow.setVisibility(View.GONE);
                myView.blockShadowAlpha.setVisibility(View.GONE);
                break;
            case "Stage":
                myView.logoLayout.setVisibility(View.GONE);
                break;
        }

        myView.presoBackgroundAlpha.setValue((int)(mainActivityInterface.getPresenterSettings().getPresoBackgroundAlpha()*100));
        myView.presoBackgroundAlpha.setHint((int)(mainActivityInterface.getPresenterSettings().getPresoBackgroundAlpha()*100)+"%");

        myView.logoSize.setValue((int)(mainActivityInterface.getPresenterSettings().getLogoSize()*100));
        myView.logoSize.setHint((int)(mainActivityInterface.getPresenterSettings().getLogoSize()*100) + "%");

        myView.crossFadeTime.setValue(mainActivityInterface.getPresenterSettings().getPresoTransitionTime());
        myView.crossFadeTime.setHint(mainActivityInterface.getPresenterSettings().getPresoTransitionTime() + "ms");

        myView.rotateDisplay.setValue(mainActivityInterface.getPresenterSettings().getCastRotation());
        myView.rotateDisplay.setHint((int)mainActivityInterface.getPresenterSettings().getCastRotation()+"°");

        myView.horizontalMargin.setValue(mainActivityInterface.getPresenterSettings().getPresoXMargin());
        myView.horizontalMargin.setHint(mainActivityInterface.getPresenterSettings().getPresoXMargin() +"px");
        myView.verticalMargin.setValue(mainActivityInterface.getPresenterSettings().getPresoYMargin());
        myView.verticalMargin.setHint(mainActivityInterface.getPresenterSettings().getPresoYMargin() +"px");

        myView.infoAlign.setSliderPos(gravityToSliderPosition(mainActivityInterface.getPresenterSettings().getPresoInfoAlign()));
        myView.hideInfoBar.setChecked(mainActivityInterface.getPresenterSettings().getHideInfoBar());

        myView.maxFontSize.setValue(mainActivityInterface.getPresenterSettings().getFontSizePresoMax());
        myView.maxFontSize.setHint(((int)mainActivityInterface.getPresenterSettings().getFontSizePresoMax())+"sp");

        myView.showChords.setChecked(mainActivityInterface.getPresenterSettings().getPresoShowChords());

        myView.contentHorizontalAlign.setSliderPos(gravityToSliderPosition(mainActivityInterface.getPresenterSettings().getPresoLyricsAlign()));
        myView.contentVerticalAlign.setSliderPos(gravityToSliderPosition(mainActivityInterface.getPresenterSettings().getPresoLyricsVAlign()));

        myView.blockShadow.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"blockShadow",false));
        myView.blockShadowAlpha.setValue((int)(100*mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),"blockShadowAlpha",0.5f)));
        myView.blockShadowAlpha.setHint((int)(100*mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),"blockShadowAlpha",0.5f)) + "%");
    }
    private void setListeners() {
        myView.currentBackground.setOnClickListener(view -> {
            ImageChooserBottomSheet imageChooserBottomSheet = new ImageChooserBottomSheet(this,"presenterFragmentSettings");
            imageChooserBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"ImageChooserBottomSheet");
        });
        myView.currentInfoBackground.setOnClickListener(view -> {
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
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),
                    "hideInfoBar",b);
            mainActivityInterface.getPresenterSettings().setHideInfoBar(b);
            displayInterface.updateDisplay("initialiseInfoBarRequired");
            displayInterface.updateDisplay("checkSongInfoShowHide");
        });

        myView.maxFontSize.addOnSliderTouchListener(new SliderTouchListener("fontSizePresoMax"));
        myView.maxFontSize.addOnChangeListener(new SliderChangeListener("fontSizePresoMax"));

        myView.showChords.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),
                        "presoShowChords",b);
                mainActivityInterface.getPresenterSettings().setPresoShowChords(b);
                mainActivityInterface.updateFragment("presenterFragmentSongSections",null,null);
                displayInterface.updateDisplay("setSongContent");
                if (mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getItemCount()>0) {
                    mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().notifyItemRangeChanged(0,
                            mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getItemCount());
                }
        });
        myView.contentHorizontalAlign.addOnSliderTouchListener(new SliderTouchListener("presoLyricsAlign"));
        myView.contentHorizontalAlign.addOnChangeListener(new SliderChangeListener("presoLyricsAlign"));
        myView.contentVerticalAlign.addOnSliderTouchListener(new SliderTouchListener("presoLyricsVAlign"));
        myView.contentVerticalAlign.addOnChangeListener(new SliderChangeListener("presoLyricsVAlign"));

        myView.blockShadow.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"blockShadow",b);
            mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(),mainActivityInterface);
        });
        myView.blockShadowAlpha.addOnChangeListener(new SliderChangeListener("blockShadowAlpha"));
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

        @SuppressLint("RestrictedApi")
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {}

        @SuppressLint("RestrictedApi")
        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            // Save the preference and update the screen
            switch (prefName) {
                case "logoSize":
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),
                            prefName, floatToDecPlaces(slider.getValue()/100f));
                    mainActivityInterface.getPresenterSettings().setLogoSize(floatToDecPlaces(slider.getValue()/100f));
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
                    displayInterface.updateDisplay("changeRotation");
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
                    displayInterface.updateDisplay("changeRotation");
                    break;
                case "presoBackgroundAlpha":
                    // The slider goes from 0 to 100
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),
                            "presoBackgroundAlpha", slider.getValue()/100f);
                    mainActivityInterface.getPresenterSettings().setPresoBackgroundAlpha(slider.getValue()/100f);
                    displayInterface.updateDisplay("changeBackground");
                    break;
                case "fontSizePresoMax":
                    // The slider goes from 10 to 100
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),
                            "fontSizePresoMax", slider.getValue());
                    mainActivityInterface.getPresenterSettings().setFontSizePresoMax(slider.getValue());
                    displayInterface.updateDisplay("setSongContent");
                    break;
                case "blockShadowAlpha":
                    // The slider goes from 0 to 100
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),
                            "blockShadowAlpha", slider.getValue()/100f);
                    mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(),
                            mainActivityInterface);
                    break;
            }
        }
    }

    private class SliderChangeListener implements Slider.OnChangeListener {

        private final String prefName;

        SliderChangeListener(String prefName) {
            this.prefName = prefName;
        }

        @SuppressLint("RestrictedApi")
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            // Don't save the preference, but update the text
            int gravity;
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
                    mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(),
                            "presoInfoAlign", gravity);
                    mainActivityInterface.getPresenterSettings().setPresoInfoAlign(gravity);
                    displayInterface.updateDisplay("changeInfoAlignment");
                    break;
                case "presoLyricsAlign":
                    // The slider goes from 0 to 2.  We need to look up the gravity
                    gravity = sliderPositionToGravity(false,(int)slider.getValue());
                    mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(),
                            "presoLyricsAlign", gravity);
                    mainActivityInterface.getPresenterSettings().setPresoLyricsAlign(gravity);
                    displayInterface.updateDisplay("setSongContent");
                    break;
                case "presoLyricsVAlign":
                    // The slider goes from 0 to 2.  We need to look up the gravity
                    gravity = sliderPositionToGravity(true,(int)slider.getValue());
                    mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(),
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
            }
        }
    }
}

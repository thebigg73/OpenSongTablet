package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpLayoutFragment extends DialogFragment {

    static PopUpLayoutFragment newInstance() {
        PopUpLayoutFragment frag;
        frag = new PopUpLayoutFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshSecondaryDisplay(String what);
        void loadSong();
    }

    private static MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private boolean firsttime = true;

    private SwitchCompat toggleChordsButton, toggleAutoScaleButton, blockShadow, boldTextButton;
    private LinearLayout group_maxfontsize;
    private LinearLayout group_manualfontsize;
    private LinearLayout blockShadowAlphaLayout;
    private TextView blockShadowAlphaText;
    private SeekBar setMaxFontSizeProgressBar, setFontSizeProgressBar, presoAlphaProgressBar,
            setXMarginProgressBar, setYMarginProgressBar, presoTitleSizeSeekBar,
            presoAuthorSizeSeekBar, presoCopyrightSizeSeekBar, setRotationProgressBar,
            presoAlertSizeSeekBar, presoTransitionTimeSeekBar, blockShadowAlpha;
    private TextView maxfontSizePreview;
    private TextView fontSizePreview;
    private TextView presoAlphaText;
    private LinearLayout lyrics_title_align;
    private TextView presoAlertText;
    private TextView presoTransitionTimeTextView;
    private TextView rotationTextView;
    private FloatingActionButton lyrics_left_align, lyrics_center_align, lyrics_right_align,
            info_left_align, info_center_align, info_right_align, lyrics_top_valign, lyrics_center_valign, lyrics_bottom_valign;
    private ImageView chooseLogoButton, chooseImage1Button, chooseImage2Button, chooseVideo1Button,
            chooseVideo2Button;
    private CheckBox image1CheckBox, image2CheckBox, video1CheckBox, video2CheckBox;
    private StorageAccess storageAccess;
    private Preferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        final View V = inflater.inflate(R.layout.popup_layout, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.connected_display));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        storageAccess = new StorageAccess();
        preferences = new Preferences();
        SetTypeFace setTypeFace = new SetTypeFace();

        identifyViews(V);

        // Initialise the font handlers
        // Handlers for fonts
        Handler lyrichandler = new Handler();
        Handler chordhandler = new Handler();
        Handler stickyhandler = new Handler();
        Handler presohandler = new Handler();
        Handler presoinfohandler = new Handler();
        Handler customhandler = new Handler();

        setTypeFace.setUpAppFonts(getContext(), preferences, lyrichandler, chordhandler, stickyhandler,
                presohandler, presoinfohandler, customhandler);

        prepareViews();

        setupListeners();

        // Make sure the logo we have is what is displayed (if we have set a new one)
        sendUpdateToScreen("logo");

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void identifyViews(View V) {
        blockShadow = V.findViewById(R.id.blockShadow);
        blockShadowAlphaLayout = V.findViewById(R.id.blockShadowAlphaLayout);
        blockShadowAlpha = V.findViewById(R.id.blockShadowAlpha);
        blockShadowAlphaText = V.findViewById(R.id.blockShadowAlphaText);
        toggleChordsButton = V.findViewById(R.id.toggleChordsButton);
        boldTextButton = V.findViewById(R.id.boldTextButton);
        toggleAutoScaleButton = V.findViewById(R.id.toggleAutoScaleButton);
        group_maxfontsize = V.findViewById(R.id.group_maxfontsize);
        setMaxFontSizeProgressBar = V.findViewById(R.id.setMaxFontSizeProgressBar);
        maxfontSizePreview = V.findViewById(R.id.maxfontSizePreview);
        group_manualfontsize = V.findViewById(R.id.group_manualfontsize);
        presoAlertText = V.findViewById(R.id.presoAlertText);
        setFontSizeProgressBar = V.findViewById(R.id.setFontSizeProgressBar);
        fontSizePreview = V.findViewById(R.id.fontSizePreview);
        lyrics_title_align = V.findViewById(R.id.lyrics_title_align);
        lyrics_left_align = V.findViewById(R.id.lyrics_left_align);
        lyrics_center_align = V.findViewById(R.id.lyrics_center_align);
        lyrics_right_align = V.findViewById(R.id.lyrics_right_align);
        lyrics_top_valign = V.findViewById(R.id.lyrics_top_valign);
        lyrics_center_valign = V.findViewById(R.id.lyrics_center_valign);
        lyrics_bottom_valign = V.findViewById(R.id.lyrics_bottom_valign);
        info_left_align = V.findViewById(R.id.info_left_align);
        info_center_align = V.findViewById(R.id.info_center_align);
        info_right_align = V.findViewById(R.id.info_right_align);
        presoTitleSizeSeekBar = V.findViewById(R.id.presoTitleSizeSeekBar);
        presoAuthorSizeSeekBar = V.findViewById(R.id.presoAuthorSizeSeekBar);
        presoCopyrightSizeSeekBar = V.findViewById(R.id.presoCopyrightSizeSeekBar);
        presoAlertSizeSeekBar = V.findViewById(R.id.presoAlertSizeSeekBar);
        presoTransitionTimeSeekBar = V.findViewById(R.id.presoTransitionTimeSeekBar);
        presoTransitionTimeTextView = V.findViewById(R.id.presoTransitionTimeTextView);
        presoAlphaProgressBar = V.findViewById(R.id.presoAlphaProgressBar);
        presoAlphaText = V.findViewById(R.id.presoAlphaText);
        chooseLogoButton = V.findViewById(R.id.chooseLogoButton);
        chooseImage1Button = V.findViewById(R.id.chooseImage1Button);
        chooseImage2Button = V.findViewById(R.id.chooseImage2Button);
        chooseVideo1Button = V.findViewById(R.id.chooseVideo1Button);
        chooseVideo2Button = V.findViewById(R.id.chooseVideo2Button);
        image1CheckBox = V.findViewById(R.id.image1CheckBox);
        image2CheckBox = V.findViewById(R.id.image2CheckBox);
        video1CheckBox = V.findViewById(R.id.video1CheckBox);
        video2CheckBox = V.findViewById(R.id.video2CheckBox);
        setXMarginProgressBar = V.findViewById(R.id.setXMarginProgressBar);
        setYMarginProgressBar = V.findViewById(R.id.setYMarginProgressBar);
        TextView modes_TextView = V.findViewById(R.id.modes_TextView);
        switch (StaticVariables.whichMode) {
            case "Performance":
            default:
                modes_TextView.setText(getString(R.string.performancemode));
                break;
            case "Stage":
                modes_TextView.setText(getString(R.string.stagemode));
                break;
            case "Presentation":
                modes_TextView.setText(getString(R.string.presentermode));
                break;
        }
        rotationTextView = V.findViewById(R.id.rotationTextView);
        setRotationProgressBar = V.findViewById(R.id.setRotationProgressBar);
    }

    private void prepareViews() {
        // Set the stuff up to what it should be from preferences
        blockShadow.setChecked(preferences.getMyPreferenceBoolean(getContext(),"blockShadow",false));
        setBlockAlphaVisibility();
        blockShadowAlpha.setMax(100);
        float alphaval = preferences.getMyPreferenceFloat(getContext(),"blockShadowAlpha",0.7f);
        blockShadowAlpha.setProgress((int)(alphaval*100.0f));
        String newtext = (int) (alphaval * 100.0f) + " %";
        blockShadowAlphaText.setText(newtext);
        toggleChordsButton.setChecked(preferences.getMyPreferenceBoolean(getContext(),"presoShowChords",false));
        boldTextButton.setChecked(preferences.getMyPreferenceBoolean(getContext(),"presoLyricsBold",false));
        if (StaticVariables.whichMode.equals("Presentation")) {
            toggleAutoScaleButton.setChecked(preferences.getMyPreferenceBoolean(getContext(),"presoAutoScale",true));
            // IV - Autocscale (no manual) when showing chords
            if (preferences.getMyPreferenceBoolean(getContext(), "presoShowChords", false)) {
                toggleAutoScaleButton.setVisibility(View.GONE);
                showorhideView(group_maxfontsize, true);
                showorhideView(group_manualfontsize, false);
            } else {
                showorhideView(group_maxfontsize, toggleAutoScaleButton.isChecked());
                showorhideView(group_manualfontsize, !toggleAutoScaleButton.isChecked());
            }
        } else {
            // IV - Stage and Performance modes do not support manual font size or show alerts
            toggleAutoScaleButton.setVisibility(View.GONE);
            showorhideView(group_maxfontsize, true);
            showorhideView(group_manualfontsize, false);
            presoAlertText.setVisibility(View.GONE);
            presoAlertSizeSeekBar.setVisibility(View.GONE);
        }
        setMaxFontSizeProgressBar.setMax(70);
        int progress = (int)preferences.getMyPreferenceFloat(getContext(),"fontSizePresoMax",40.0f) - 4;
        setMaxFontSizeProgressBar.setProgress(progress - 4);
        newtext = (progress + 4) + " sp";
        maxfontSizePreview.setText(newtext);
        maxfontSizePreview.setTextSize(progress + 4);
        maxfontSizePreview.setTypeface(StaticVariables.typefacePreso);
        setFontSizeProgressBar.setMax(70);
        progress = (int)preferences.getMyPreferenceFloat(getContext(),"fontSizePreso",14.0f) - 4;
        setFontSizeProgressBar.setProgress(progress - 4);
        newtext = (progress + 4) + " sp";
        fontSizePreview.setText(newtext);
        fontSizePreview.setTextSize(progress + 4);
        fontSizePreview.setTypeface(StaticVariables.typefacePreso);
        setUpAlignmentButtons();
        presoTitleSizeSeekBar.setMax(32);
        presoAuthorSizeSeekBar.setMax(32);
        presoCopyrightSizeSeekBar.setMax(32);
        presoAlertSizeSeekBar.setMax(32);
        presoTitleSizeSeekBar.setProgress((int)preferences.getMyPreferenceFloat(getContext(),"presoTitleTextSize", 14.0f));
        presoAuthorSizeSeekBar.setProgress((int)preferences.getMyPreferenceFloat(getContext(),"presoAuthorTextSize", 12.0f));
        presoCopyrightSizeSeekBar.setProgress((int)preferences.getMyPreferenceFloat(getContext(),"presoCopyrightTextSize", 12.0f));
        presoAlertSizeSeekBar.setProgress((int)preferences.getMyPreferenceFloat(getContext(),"presoAlertTextSize", 12.0f));
        alphaval = preferences.getMyPreferenceFloat(getContext(),"presoBackgroundAlpha",0.8f);
        presoAlphaProgressBar.setProgress((int)(alphaval*100.0f));
        newtext = (int) (alphaval * 100.0f) + " %";
        presoAlphaText.setText(newtext);
        presoTransitionTimeSeekBar.setMax(23);
        presoTransitionTimeSeekBar.setProgress(timeToSeekBarProgress());
        presoTransitionTimeTextView.setText(SeekBarProgressToText());
        setupPreviews();
        setCheckBoxes();

        setXMarginProgressBar.setMax(150);
        // IV - Allow bigger margin to give smaller active height suitable for use as bottom 3rd overlay of words over video
        setYMarginProgressBar.setMax(350);
        setXMarginProgressBar.setProgress(preferences.getMyPreferenceInt(getContext(),"presoXMargin",20));
        setYMarginProgressBar.setProgress(preferences.getMyPreferenceInt(getContext(),"presoYMargin",10));
        setRotationProgressBar.setMax(3);
        setRotationProgressBar.setProgress(getRotationProgressValue());
        rotationTextView.setText(getRotationAngleText(getRotationProgressValue()));
    }

    private String getRotationAngleText(int pos) {
        String angle;
        switch (pos) {
            case 0:
            default:
                angle = "0°";
                break;
            case 1:
                angle = "90°";
                break;
            case 2:
                angle = "180°";
                break;
            case 3:
                angle = "270°";
                break;
        }
        return angle;
    }
    private int getRotationProgressValue() {
        float f = preferences.getMyPreferenceFloat(getContext(),"castRotation",0.0f);
        if (f==90.0f) {
            return 1;
        } else if (f==180.0f) {
            return 2;
        } else if (f==270.0f) {
            return 3;
        } else {
            return 0;
        }
    }
    private void saveRotationValue(int progress) {
        float angle;
        switch (progress) {
            case 0:
            default:
                angle = 0.0f;
                break;
            case 1:
                angle = 90.0f;
                break;
            case 2:
                angle = 180.0f;
                break;
            case 3:
                angle = 270.0f;
                break;
        }
        preferences.setMyPreferenceFloat(getContext(),"castRotation",angle);
    }

    private void setupListeners() {
        // Set listeners
        blockShadow.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getContext(),"blockShadow",b);
            setBlockAlphaVisibility();
            sendUpdateToScreen("all");
        });
        blockShadowAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String update = i + " %";
                blockShadowAlphaText.setText(update);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferences.setMyPreferenceFloat(getContext(),"blockShadowAlpha",(float)seekBar.getProgress()/100.0f);
                sendUpdateToScreen("all");
            }
        });
        toggleChordsButton.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getContext(),"presoShowChords",b);
            if (StaticVariables.whichMode.equals("Presentation")) {
                // IV - Autocscale (no manual) when showing chords
                if (b) {
                    toggleAutoScaleButton.setVisibility(View.GONE);
                    showorhideView(group_maxfontsize, true);
                    showorhideView(group_manualfontsize, false);
                } else {
                    toggleAutoScaleButton.setVisibility(View.VISIBLE);
                    showorhideView(group_maxfontsize,toggleAutoScaleButton.isChecked());
                    showorhideView(group_manualfontsize,!toggleAutoScaleButton.isChecked());
                }
            }
            if (mListener!=null) {
                try {
                    mListener.loadSong();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            setUpAlignmentButtons();
        });
        boldTextButton.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getContext(),"presoLyricsBold",b);
            sendUpdateToScreen("all");
        });
        toggleAutoScaleButton.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getContext(),"presoAutoScale",b);
            showorhideView(group_maxfontsize,b);
            showorhideView(group_manualfontsize,!b);
            sendUpdateToScreen("autoscale");
        });
        setMaxFontSizeProgressBar.setOnSeekBarChangeListener(new setMaxFontSizeListener());
        setFontSizeProgressBar.setOnSeekBarChangeListener(new setFontSizeListener());
        lyrics_left_align.setOnClickListener(view -> {
            CustomAnimations.animateFAB(lyrics_left_align, getContext());
            preferences.setMyPreferenceInt(getContext(),"presoLyricsAlign",Gravity.START);
            setUpAlignmentButtons();
            sendUpdateToScreen("all");
        });
        lyrics_center_align.setOnClickListener(view -> {
            CustomAnimations.animateFAB(lyrics_center_align, getContext());
            preferences.setMyPreferenceInt(getContext(),"presoLyricsAlign",Gravity.CENTER);
            setUpAlignmentButtons();
            sendUpdateToScreen("all");
        });
        lyrics_right_align.setOnClickListener(view -> {
            CustomAnimations.animateFAB(lyrics_right_align, getContext());
            preferences.setMyPreferenceInt(getContext(),"presoLyricsAlign",Gravity.END);
            setUpAlignmentButtons();
            sendUpdateToScreen("all");
        });
        lyrics_top_valign.setOnClickListener(view -> {
            CustomAnimations.animateFAB(lyrics_top_valign, getContext());
            preferences.setMyPreferenceInt(getContext(),"presoLyricsVAlign",Gravity.TOP);
            setUpAlignmentButtons();
            sendUpdateToScreen("all");
        });
        lyrics_center_valign.setOnClickListener(view -> {
            CustomAnimations.animateFAB(lyrics_center_valign, getContext());
            preferences.setMyPreferenceInt(getContext(),"presoLyricsVAlign",Gravity.CENTER_VERTICAL);
            setUpAlignmentButtons();
            sendUpdateToScreen("all");
        });
        lyrics_bottom_valign.setOnClickListener(view -> {
            CustomAnimations.animateFAB(lyrics_bottom_valign, getContext());
            preferences.setMyPreferenceInt(getContext(),"presoLyricsVAlign",Gravity.BOTTOM);
            setUpAlignmentButtons();
            sendUpdateToScreen("all");
        });
        info_left_align.setOnClickListener(view -> {
            CustomAnimations.animateFAB(info_left_align, getContext());
            preferences.setMyPreferenceInt(getContext(),"presoInfoAlign",Gravity.START);
            setUpAlignmentButtons();
            sendUpdateToScreen("info");
        });
        info_center_align.setOnClickListener(view -> {
            CustomAnimations.animateFAB(info_center_align, getContext());
            preferences.setMyPreferenceInt(getContext(),"presoInfoAlign",Gravity.CENTER);
            setUpAlignmentButtons();
            sendUpdateToScreen("info");
        });
        info_right_align.setOnClickListener(view -> {
            CustomAnimations.animateFAB(info_right_align, getContext());
            preferences.setMyPreferenceInt(getContext(),"presoInfoAlign",Gravity.END);
            setUpAlignmentButtons();
            sendUpdateToScreen("info");
        });
        presoTransitionTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                presoTransitionTimeTextView.setText(SeekBarProgressToText());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferences.setMyPreferenceInt(getContext(),"presoTransitionTime",seekBarProgressToTime());
            }
        });
        presoTitleSizeSeekBar.setOnSeekBarChangeListener(new presoSectionSizeListener());
        presoAuthorSizeSeekBar.setOnSeekBarChangeListener(new presoSectionSizeListener());
        presoCopyrightSizeSeekBar.setOnSeekBarChangeListener(new presoSectionSizeListener());
        presoAlertSizeSeekBar.setOnSeekBarChangeListener(new presoSectionSizeListener());
        presoAlphaProgressBar.setOnSeekBarChangeListener(new presoAlphaListener());
        setXMarginProgressBar.setOnSeekBarChangeListener(new setMargin_Listener());
        setYMarginProgressBar.setOnSeekBarChangeListener(new setMargin_Listener());
        chooseLogoButton.setOnClickListener(v -> {
            // Open another popup listing the files to choose from
            PresenterMode.whatBackgroundLoaded = "logo";
            chooseFile("image/*",StaticVariables.REQUEST_CUSTOM_LOGO);
        });
        chooseImage1Button.setOnClickListener(v -> {
            // Open another popup listing the files to choose from
            PresenterMode.whatBackgroundLoaded = "image1";
            chooseFile("image/*",StaticVariables.REQUEST_BACKGROUND_IMAGE1);
        });
        chooseImage2Button.setOnClickListener(v -> {
            // Open another popup listing the files to choose from
            PresenterMode.whatBackgroundLoaded = "image2";
            chooseFile("image/*",StaticVariables.REQUEST_BACKGROUND_IMAGE2);
        });
        chooseVideo1Button.setOnClickListener(v -> {
            // Open another popup listing the files to choose from
            PresenterMode.whatBackgroundLoaded = "video1";
            chooseFile("video/*",StaticVariables.REQUEST_BACKGROUND_VIDEO1);
        });
        chooseVideo2Button.setOnClickListener(v -> {
            // Open another popup listing the files to choose from
            PresenterMode.whatBackgroundLoaded = "video2";
            chooseFile("video/*",StaticVariables.REQUEST_BACKGROUND_VIDEO2);
        });
        image1CheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                image2CheckBox.setChecked(false);
                video1CheckBox.setChecked(false);
                video2CheckBox.setChecked(false);
                PresenterMode.whatBackgroundLoaded = "image1";
                preferences.setMyPreferenceString(getContext(),"backgroundTypeToUse","image");
                preferences.setMyPreferenceString(getContext(),"backgroundToUse","img1");
                sendUpdateToScreen("backgrounds");
            }
        });
        image2CheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                image1CheckBox.setChecked(false);
                video1CheckBox.setChecked(false);
                video2CheckBox.setChecked(false);
                PresenterMode.whatBackgroundLoaded = "image2";
                preferences.setMyPreferenceString(getContext(),"backgroundTypeToUse","image");
                preferences.setMyPreferenceString(getContext(),"backgroundToUse","img2");
                sendUpdateToScreen("backgrounds");
            }
        });
        video1CheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                image1CheckBox.setChecked(false);
                image2CheckBox.setChecked(false);
                video2CheckBox.setChecked(false);
                PresenterMode.whatBackgroundLoaded = "video1";
                preferences.setMyPreferenceString(getContext(),"backgroundTypeToUse","video");
                preferences.setMyPreferenceString(getContext(),"backgroundToUse","vid1");
                sendUpdateToScreen("backgrounds");
            }
        });
        video2CheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                image1CheckBox.setChecked(false);
                image2CheckBox.setChecked(false);
                video1CheckBox.setChecked(false);
                PresenterMode.whatBackgroundLoaded = "video2";
                preferences.setMyPreferenceString(getContext(),"backgroundTypeToUse","video");
                preferences.setMyPreferenceString(getContext(),"backgroundToUse","vid2");
                sendUpdateToScreen("backgrounds");
            }
        });
        setRotationProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rotationTextView.setText(getRotationAngleText(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveRotationValue(seekBar.getProgress());
                sendUpdateToScreen("all");
            }
        });
    }

    private void setBlockAlphaVisibility() {
        if (preferences.getMyPreferenceBoolean(getContext(), "blockShadow", false)) {
            blockShadowAlphaLayout.setVisibility(View.VISIBLE);
        } else {
            blockShadowAlphaLayout.setVisibility(View.GONE);
        }
    }

    private int timeToSeekBarProgress() {
        // Min time is 200ms, max is 2000ms
        return (preferences.getMyPreferenceInt(getContext(),"presoTransitionTime",800)/100) - 2;
    }

    private int seekBarProgressToTime() {
        return (presoTransitionTimeSeekBar.getProgress() + 2) * 100;
    }

    private String SeekBarProgressToText() {
        return (((float)presoTransitionTimeSeekBar.getProgress() + 2.0f)/10.0f) + " s";
    }

    private void setUpAlignmentButtons() {


        if ((StaticVariables.whichMode.equals("Stage") && !preferences.getMyPreferenceBoolean(getContext(), "presoShowChords", false)) ||
                StaticVariables.whichMode.equals("Presentation")) {

            int lyralign = preferences.getMyPreferenceInt(getContext(), "presoLyricsAlign", Gravity.CENTER_HORIZONTAL);
            int lyrvalign = preferences.getMyPreferenceInt(getContext(), "presoLyricsVAlign", Gravity.CENTER_VERTICAL);

            if (lyralign == Gravity.START) {
                lyrics_left_align.setBackgroundTintList(ColorStateList.valueOf(0xff444488));
                lyrics_center_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
                lyrics_right_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            } else if (lyralign == Gravity.END) {
                lyrics_left_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
                lyrics_center_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
                lyrics_right_align.setBackgroundTintList(ColorStateList.valueOf(0xff444488));
            } else { // CENTER_HORIZONTAL
                lyrics_left_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
                lyrics_center_align.setBackgroundTintList(ColorStateList.valueOf(0xff444488));
                lyrics_right_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            }
            if (lyrvalign == Gravity.TOP) {
                lyrics_top_valign.setBackgroundTintList(ColorStateList.valueOf(0xff444488));
                lyrics_center_valign.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
                lyrics_bottom_valign.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            } else if (lyrvalign == Gravity.BOTTOM) {
                lyrics_top_valign.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
                lyrics_center_valign.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
                lyrics_bottom_valign.setBackgroundTintList(ColorStateList.valueOf(0xff444488));
            } else { // CENTER_VERTICAL
                lyrics_top_valign.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
                lyrics_center_valign.setBackgroundTintList(ColorStateList.valueOf(0xff444488));
                lyrics_bottom_valign.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            }
            lyrics_title_align.setVisibility(View.VISIBLE);
        } else {
            lyrics_title_align.setVisibility(View.GONE);
        }

        // IV - Info is always shown
        int infalign = preferences.getMyPreferenceInt(getContext(), "presoInfoAlign", Gravity.END);

        if (infalign == Gravity.START) {
            info_left_align.setBackgroundTintList(ColorStateList.valueOf(0xff444488));
            info_center_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            info_right_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
        } else if (infalign == Gravity.END) {
            info_left_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            info_center_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            info_right_align.setBackgroundTintList(ColorStateList.valueOf(0xff444488));
        } else { // CENTER_HORIZONTAL
            info_left_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            info_center_align.setBackgroundTintList(ColorStateList.valueOf(0xff444488));
            info_right_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
        }
    }

    private void setCheckBoxes() {

        switch (preferences.getMyPreferenceString(getContext(),"backgroundToUse","img1")) {
            case "img1":
                image1CheckBox.setChecked(true);
                image1CheckBox.setTextColor(0xff444488);
                image2CheckBox.setChecked(false);
                video1CheckBox.setChecked(false);
                video2CheckBox.setChecked(false);
                break;
            case "img2":
                image1CheckBox.setChecked(false);
                image2CheckBox.setChecked(true);
                image2CheckBox.setTextColor(0xff444488);
                video1CheckBox.setChecked(false);
                video2CheckBox.setChecked(false);
                break;
            case "vid1":
                image1CheckBox.setChecked(false);
                image2CheckBox.setChecked(false);
                video1CheckBox.setChecked(true);
                video1CheckBox.setTextColor(0xff444488);
                video2CheckBox.setChecked(false);
                break;
            case "vid2":
                image1CheckBox.setChecked(false);
                image2CheckBox.setChecked(false);
                video1CheckBox.setChecked(false);
                video2CheckBox.setChecked(true);
                video2CheckBox.setTextColor(0xff444488);
                break;
        }
    }

    private void chooseFile(String mimeType, int requestCode) {
        // This calls an intent to choose a file (image or video)
        Uri uri = storageAccess.getUriForItem(getContext(),preferences,"Backgrounds","","");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT,uri);
        if (mimeType.equals("image/*")) {
            intent.setDataAndType(uri,"image/*");
        } else {
            intent.setDataAndType(uri,"video/*");
        }
        startActivityForResult(intent, requestCode);
    }

    private void showorhideView(View v, boolean show) {
        if (show) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    private void sendUpdateToScreen(String what) {
        if (!firsttime) {
            StaticVariables.infoBarChangeRequired = true;
            if (mListener != null) {
                mListener.refreshSecondaryDisplay(what);
            }
        } else {
            // We have just initialised the variables
            firsttime = false;
            if (mListener != null) {
                mListener.refreshSecondaryDisplay("logo");
            }
        }
    }

    private class setMargin_Listener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            preferences.setMyPreferenceInt(getContext(),"presoXMargin",setXMarginProgressBar.getProgress());
            preferences.setMyPreferenceInt(getContext(),"presoYMargin",setYMarginProgressBar.getProgress());
            sendUpdateToScreen("margins");
        }
    }
    private class setFontSizeListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String newtext = (progress + 4) + " sp";
            fontSizePreview.setText(newtext);
            fontSizePreview.setTextSize(progress + 4);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            float val = seekBar.getProgress()+4.0f;
            preferences.setMyPreferenceFloat(getContext(),"fontSizePreso",val);
            sendUpdateToScreen("manualfontsize");
        }
    }
    private class setMaxFontSizeListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String newtext = (progress + 4) + " sp";
            maxfontSizePreview.setText(newtext);
            maxfontSizePreview.setTextSize(progress + 4);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            float val = seekBar.getProgress() + 4.0f;
            preferences.setMyPreferenceFloat(getContext(),"fontSizePresoMax",val);
            sendUpdateToScreen("maxfontsize");
        }

    }
    private class presoSectionSizeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            preferences.setMyPreferenceFloat(getContext(),"presoTitleTextSize", (float)presoTitleSizeSeekBar.getProgress());
            preferences.setMyPreferenceFloat(getContext(),"presoAuthorTextSize", (float)presoAuthorSizeSeekBar.getProgress());
            preferences.setMyPreferenceFloat(getContext(),"presoCopyrightTextSize", (float)presoCopyrightSizeSeekBar.getProgress());
            preferences.setMyPreferenceFloat(getContext(),"presoAlertTextSize", (float)presoAlertSizeSeekBar.getProgress());
            sendUpdateToScreen("info");
        }
    }
    private class presoAlphaListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            String update = progress + " %";
            presoAlphaText.setText(update);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {

            preferences.setMyPreferenceFloat(getContext(),"presoBackgroundAlpha",((float)seekBar.getProgress() / 100f));
            sendUpdateToScreen("backgrounds");
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

    private void setupPreviews() {
        // This sets the background thumbnails for the images/videos/logo
        Uri img1Uri = storageAccess.fixLocalisedUri(getContext(),preferences,preferences.getMyPreferenceString(getContext(),"backgroundImage1","ost_bg.png"));
        updatePreview(getContext(),chooseImage1Button, img1Uri);
        Uri img2Uri = storageAccess.fixLocalisedUri(getContext(),preferences,preferences.getMyPreferenceString(getContext(),"backgroundImage2","ost_bg.png"));
        updatePreview(getContext(),chooseImage2Button, img2Uri);
        Uri vid1Uri = storageAccess.fixLocalisedUri(getContext(),preferences,preferences.getMyPreferenceString(getContext(),"backgroundVideo1",""));
        updatePreview(getContext(),chooseVideo1Button, vid1Uri);
        Uri vid2Uri = storageAccess.fixLocalisedUri(getContext(),preferences,preferences.getMyPreferenceString(getContext(),"backgroundVideo2",""));
        updatePreview(getContext(),chooseVideo2Button, vid2Uri);
        Uri logoUri = storageAccess.fixLocalisedUri(getContext(),preferences,preferences.getMyPreferenceString(getContext(),"customLogo","ost_logo.png"));
        updatePreview(getContext(),chooseLogoButton, logoUri);
    }

    private void updatePreview (Context c, ImageView view, Uri uri) {
        if (uri==null || !storageAccess.uriExists(getContext(),uri)) {
            view.setBackgroundColor(0xff000000);
        } else {
            view.setBackgroundColor(0x00000000);
            RequestOptions myOptions = new RequestOptions().fitCenter().override(120, 90);
            if (uri.toString().contains("ost_logo")) {
                GlideApp.with(c).load(R.drawable.ost_logo).apply(myOptions).into(view);
            } else if (uri.toString().contains("ost_bg")) {
                GlideApp.with(c).load(R.drawable.preso_default_bg).apply(myOptions).into(view);
            } else {
                GlideApp.with(c).load(uri).apply(myOptions).into(view);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // This gets sent back here from the parent activity
        if (resultData!=null && resultData.getData()!=null) {
            Uri uri = resultData.getData();
            if (getContext()!=null && uri!=null) {
                String uriString = storageAccess.fixUriToLocal(uri);

                if (requestCode == StaticVariables.REQUEST_BACKGROUND_IMAGE1) {
                    preferences.setMyPreferenceString(getContext(), "backgroundImage1", uriString);
                    updatePreview(getContext(),chooseImage1Button,uri);
                    sendUpdateToScreen("backgrounds");

                } else if (requestCode == StaticVariables.REQUEST_BACKGROUND_IMAGE2) {
                    preferences.setMyPreferenceString(getContext(), "backgroundImage2", uriString);
                    updatePreview(getContext(),chooseImage2Button,uri);
                    sendUpdateToScreen("backgrounds");

                } else if (requestCode == StaticVariables.REQUEST_BACKGROUND_VIDEO1) {
                    preferences.setMyPreferenceString(getContext(), "backgroundVideo1", uriString);
                    updatePreview(getContext(),chooseVideo1Button,uri);
                    sendUpdateToScreen("backgrounds");

                } else if (requestCode == StaticVariables.REQUEST_BACKGROUND_VIDEO2) {
                    preferences.setMyPreferenceString(getContext(), "backgroundVideo2", uriString);
                    updatePreview(getContext(),chooseVideo2Button,uri);
                    sendUpdateToScreen("backgrounds");

                } else if (requestCode == StaticVariables.REQUEST_CUSTOM_LOGO) {
                    preferences.setMyPreferenceString(getContext(), "customLogo", uriString);
                    updatePreview(getContext(),chooseLogoButton,uri);
                    sendUpdateToScreen("logo");
                }
            }
        }
    }

}
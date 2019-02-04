package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class PopUpLayoutFragment extends DialogFragment {

    static PopUpLayoutFragment newInstance() {
        PopUpLayoutFragment frag;
        frag = new PopUpLayoutFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshSecondaryDisplay(String what);
        void openFragment();
    }

    public static MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    boolean firsttime = true;
    String newtext = "";

    SwitchCompat toggleChordsButton, toggleAutoScaleButton;
    LinearLayout group_maxfontsize, group_alignment, group_manualfontsize, group_songinfofontsizes,
            group_backgrounds, group_margins;
    SeekBar setMaxFontSizeProgressBar, setFontSizeProgressBar, presoAlphaProgressBar,
            setXMarginProgressBar, setYMarginProgressBar, presoTitleSizeSeekBar,
            presoAuthorSizeSeekBar, presoCopyrightSizeSeekBar,
            presoAlertSizeSeekBar, presoTransitionTimeSeekBar;
    TextView maxfontSizePreview, fontSizePreview, presoAlphaText, lyrics_title_align,
            presoTransitionTimeTextView;
    FloatingActionButton lyrics_left_align, lyrics_center_align, lyrics_right_align,
            info_left_align, info_center_align, info_right_align;
    ImageView chooseLogoButton, chooseImage1Button, chooseImage2Button, chooseVideo1Button,
            chooseVideo2Button;
    CheckBox image1CheckBox, image2CheckBox, video1CheckBox, video2CheckBox;
    StorageAccess storageAccess;
    Preferences preferences;
    SetTypeFace setTypeFace;
    // Handlers for fonts
    Handler lyrichandler, chordhandler, presohandler, presoinfohandler, customhandler, monohandler;
    String backgroundFileType;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        final View V = inflater.inflate(R.layout.popup_layout, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.connected_display));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        storageAccess = new StorageAccess();
        preferences = new Preferences();
        setTypeFace = new SetTypeFace();

        // Load up the presentation preferences
        preferences.loadPresentationPreferences(getActivity());

        identifyViews(V);

        // Initialise the font handlers
        lyrichandler = new Handler();
        chordhandler = new Handler();
        presohandler = new Handler();
        presoinfohandler = new Handler();
        customhandler = new Handler();
        monohandler = new Handler();

        setTypeFace.setUpAppFonts(getActivity(), preferences, lyrichandler, chordhandler,
                presohandler, presoinfohandler, customhandler, monohandler);
        //SetTypeFace.setTypeface(getActivity(), preferences);

        prepareViews();

        setupListeners();

        // Make sure the logo we have is what is displayed (if we have set a new one)
        sendUpdateToScreen("logo");

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    void identifyViews(View V) {
        toggleChordsButton = V.findViewById(R.id.toggleChordsButton);
        toggleAutoScaleButton = V.findViewById(R.id.toggleAutoScaleButton);
        group_maxfontsize = V.findViewById(R.id.group_maxfontsize);
        setMaxFontSizeProgressBar = V.findViewById(R.id.setMaxFontSizeProgressBar);
        maxfontSizePreview = V.findViewById(R.id.maxfontSizePreview);
        group_manualfontsize = V.findViewById(R.id.group_manualfontsize);
        setFontSizeProgressBar = V.findViewById(R.id.setFontSizeProgressBar);
        fontSizePreview = V.findViewById(R.id.fontSizePreview);
        group_alignment = V.findViewById(R.id.group_alignment);
        lyrics_title_align = V.findViewById(R.id.lyrics_title_align);
        lyrics_left_align = V.findViewById(R.id.lyrics_left_align);
        lyrics_center_align = V.findViewById(R.id.lyrics_center_align);
        lyrics_right_align = V.findViewById(R.id.lyrics_right_align);
        info_left_align = V.findViewById(R.id.info_left_align);
        info_center_align = V.findViewById(R.id.info_center_align);
        info_right_align = V.findViewById(R.id.info_right_align);
        group_songinfofontsizes = V.findViewById(R.id.group_songinfofontsizes);
        presoTitleSizeSeekBar = V.findViewById(R.id.presoTitleSizeSeekBar);
        presoAuthorSizeSeekBar = V.findViewById(R.id.presoAuthorSizeSeekBar);
        presoCopyrightSizeSeekBar = V.findViewById(R.id.presoCopyrightSizeSeekBar);
        presoAlertSizeSeekBar = V.findViewById(R.id.presoAlertSizeSeekBar);
        presoTransitionTimeSeekBar = V.findViewById(R.id.presoTransitionTimeSeekBar);
        presoTransitionTimeTextView = V.findViewById(R.id.presoTransitionTimeTextView);
        group_backgrounds = V.findViewById(R.id.group_backgrounds);
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
        group_margins = V.findViewById(R.id.group_margins);
        setXMarginProgressBar = V.findViewById(R.id.setXMarginProgressBar);
        setYMarginProgressBar = V.findViewById(R.id.setYMarginProgressBar);
    }

    void prepareViews() {
        // Set the stuff up to what it should be from preferences
        toggleChordsButton.setChecked(FullscreenActivity.presoShowChords);
        toggleAutoScaleButton.setChecked(FullscreenActivity.presoAutoScale);
        setMaxFontSizeProgressBar.setMax(70);
        setMaxFontSizeProgressBar.setProgress(FullscreenActivity.presoMaxFontSize - 4);
        maxfontSizePreview.setTypeface(FullscreenActivity.presofont);
        setFontSizeProgressBar.setMax(70);
        setFontSizeProgressBar.setProgress(FullscreenActivity.presoFontSize - 4);
        fontSizePreview.setTypeface(FullscreenActivity.presofont);
        setUpAlignmentButtons();
        presoTitleSizeSeekBar.setMax(32);
        presoAuthorSizeSeekBar.setMax(32);
        presoCopyrightSizeSeekBar.setMax(32);
        presoAlertSizeSeekBar.setMax(32);
        presoTitleSizeSeekBar.setProgress((int)FullscreenActivity.presoTitleSize);
        presoAuthorSizeSeekBar.setProgress((int)FullscreenActivity.presoAuthorSize);
        presoCopyrightSizeSeekBar.setProgress((int)FullscreenActivity.presoCopyrightSize);
        presoAlertSizeSeekBar.setProgress((int)FullscreenActivity.presoAlertSize);
        presoAlphaProgressBar.setProgress((int)(FullscreenActivity.presoAlpha * 100.0f));
        newtext = (int) (FullscreenActivity.presoAlpha * 100.0f) + " %";
        presoAlphaText.setText(newtext);
        presoTransitionTimeSeekBar.setMax(23);
        presoTransitionTimeSeekBar.setProgress(timeToSeekBarProgress());
        presoTransitionTimeTextView.setText(SeekBarProgressToText());
        setupPreviews();
        setCheckBoxes();
        setXMarginProgressBar.setMax(50);
        setYMarginProgressBar.setMax(50);
        setXMarginProgressBar.setProgress(FullscreenActivity.xmargin_presentation);
        setYMarginProgressBar.setProgress(FullscreenActivity.ymargin_presentation);
    }

    void setupListeners() {
        // Set listeners
        toggleChordsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.presoShowChords = b;
                preferences.setMyPreferenceBoolean(getActivity(),"presoShowChords",b);
                sendUpdateToScreen("chords");
                setUpAlignmentButtons();
            }
        });
        toggleAutoScaleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.presoShowChords = b;
                preferences.setMyPreferenceBoolean(getActivity(),"presoShowChords",b);
                showorhideView(group_maxfontsize,b);
                showorhideView(group_manualfontsize,!b);
                sendUpdateToScreen("autoscale");
            }
        });
        setMaxFontSizeProgressBar.setOnSeekBarChangeListener(new setMaxFontSizeListener());
        setFontSizeProgressBar.setOnSeekBarChangeListener(new setFontSizeListener());
        lyrics_left_align.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(lyrics_left_align, getActivity());
                FullscreenActivity.presoLyricsAlign = Gravity.START;
                preferences.setMyPreferenceInt(getActivity(),"presoLyricsAlign",FullscreenActivity.presoLyricsAlign);
                setUpAlignmentButtons();
                sendUpdateToScreen("all");
            }
        });
        lyrics_center_align.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(lyrics_center_align, getActivity());
                FullscreenActivity.presoLyricsAlign = Gravity.CENTER;
                preferences.setMyPreferenceInt(getActivity(),"presoLyricsAlign",FullscreenActivity.presoLyricsAlign);
                setUpAlignmentButtons();
                sendUpdateToScreen("all");
            }
        });
        lyrics_right_align.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(lyrics_right_align, getActivity());
                FullscreenActivity.presoLyricsAlign = Gravity.END;
                preferences.setMyPreferenceInt(getActivity(),"presoLyricsAlign",FullscreenActivity.presoLyricsAlign);
                setUpAlignmentButtons();
                sendUpdateToScreen("all");
            }
        });
        info_left_align.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(info_left_align, getActivity());
                FullscreenActivity.presoInfoAlign = Gravity.START;
                preferences.setMyPreferenceInt(getActivity(),"presoInfoAlign",FullscreenActivity.presoInfoAlign);
                setUpAlignmentButtons();
                sendUpdateToScreen("info");
            }
        });
        info_center_align.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(info_center_align, getActivity());
                FullscreenActivity.presoInfoAlign = Gravity.CENTER;
                preferences.setMyPreferenceInt(getActivity(),"presoInfoAlign",FullscreenActivity.presoInfoAlign);
                setUpAlignmentButtons();
                sendUpdateToScreen("info");
            }
        });
        info_right_align.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(info_right_align, getActivity());
                FullscreenActivity.presoInfoAlign = Gravity.END;
                preferences.setMyPreferenceInt(getActivity(),"presoInfoAlign",FullscreenActivity.presoInfoAlign);
                setUpAlignmentButtons();
                sendUpdateToScreen("info");
            }
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
                FullscreenActivity.presoTransitionTime = seekBarProgressToTime();
                Preferences.savePreferences();
            }
        });
        presoTitleSizeSeekBar.setOnSeekBarChangeListener(new presoSectionSizeListener());
        presoAuthorSizeSeekBar.setOnSeekBarChangeListener(new presoSectionSizeListener());
        presoCopyrightSizeSeekBar.setOnSeekBarChangeListener(new presoSectionSizeListener());
        presoAlertSizeSeekBar.setOnSeekBarChangeListener(new presoSectionSizeListener());
        presoAlphaProgressBar.setOnSeekBarChangeListener(new presoAlphaListener());
        setXMarginProgressBar.setOnSeekBarChangeListener(new setMargin_Listener());
        setYMarginProgressBar.setOnSeekBarChangeListener(new setMargin_Listener());
        chooseLogoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open another popup listing the files to choose from
                PresenterMode.whatBackgroundLoaded = "logo";
                backgroundFileType = "image";
                chooseFile();
            }
        });
        chooseImage1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open another popup listing the files to choose from
                PresenterMode.whatBackgroundLoaded = "image1";
                backgroundFileType = "image";
                chooseFile();
            }
        });
        chooseImage2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open another popup listing the files to choose from
                PresenterMode.whatBackgroundLoaded = "image2";
                backgroundFileType = "image";
                chooseFile();
            }
        });
        chooseVideo1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open another popup listing the files to choose from
                PresenterMode.whatBackgroundLoaded = "video1";
                backgroundFileType = "video";
                chooseFile();
            }
        });
        chooseVideo2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open another popup listing the files to choose from
                PresenterMode.whatBackgroundLoaded = "video2";
                backgroundFileType = "video";
                chooseFile();
            }
        });
        image1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    image2CheckBox.setChecked(false);
                    video1CheckBox.setChecked(false);
                    video2CheckBox.setChecked(false);
                    FullscreenActivity.backgroundTypeToUse = "image";
                    FullscreenActivity.backgroundToUse = "img1";
                    PresenterMode.whatBackgroundLoaded = "image1";
                    preferences.setMyPreferenceString(getActivity(),"backgroundTypeToUse","image");
                    preferences.setMyPreferenceString(getActivity(),"backgroundToUse","img1");
                    sendUpdateToScreen("backgrounds");
                }
            }
        });
        image2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    image1CheckBox.setChecked(false);
                    video1CheckBox.setChecked(false);
                    video2CheckBox.setChecked(false);
                    FullscreenActivity.backgroundTypeToUse = "image";
                    FullscreenActivity.backgroundToUse = "img2";
                    PresenterMode.whatBackgroundLoaded = "image2";
                    preferences.setMyPreferenceString(getActivity(),"backgroundTypeToUse","image");
                    preferences.setMyPreferenceString(getActivity(),"backgroundToUse","img2");
                    sendUpdateToScreen("backgrounds");
                }
            }
        });
        video1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    image1CheckBox.setChecked(false);
                    image2CheckBox.setChecked(false);
                    video2CheckBox.setChecked(false);
                    FullscreenActivity.backgroundTypeToUse = "video";
                    FullscreenActivity.backgroundToUse = "vid1";
                    PresenterMode.whatBackgroundLoaded = "video1";
                    preferences.setMyPreferenceString(getActivity(),"backgroundTypeToUse","video");
                    preferences.setMyPreferenceString(getActivity(),"backgroundToUse","vid1");
                    sendUpdateToScreen("backgrounds");
                }
            }
        });
        video2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    image1CheckBox.setChecked(false);
                    image2CheckBox.setChecked(false);
                    video1CheckBox.setChecked(false);
                    FullscreenActivity.backgroundTypeToUse = "video";
                    FullscreenActivity.backgroundToUse = "vid2";
                    PresenterMode.whatBackgroundLoaded = "video2";
                    preferences.setMyPreferenceString(getActivity(),"backgroundTypeToUse","video");
                    preferences.setMyPreferenceString(getActivity(),"backgroundToUse","vid2");
                    sendUpdateToScreen("backgrounds");
                }
            }
        });
    }

    public int timeToSeekBarProgress() {
        // Min time is 200ms, max is 2000ms
        return ((FullscreenActivity.presoTransitionTime * 10)/1000) - 2;
    }

    public int seekBarProgressToTime() {
        return (presoTransitionTimeSeekBar.getProgress() + 2) * 100;
    }

    public String SeekBarProgressToText() {
        return (((float)presoTransitionTimeSeekBar.getProgress() + 2.0f)/10.0f) + " s";
    }

    public void setUpAlignmentButtons() {
        if (FullscreenActivity.presoLyricsAlign == Gravity.START) {
            lyrics_left_align.setBackgroundTintList(ColorStateList.valueOf(0xffff0000));
            lyrics_center_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            lyrics_right_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
        } else if (FullscreenActivity.presoLyricsAlign == Gravity.CENTER ||
                FullscreenActivity.presoLyricsAlign == Gravity.CENTER_HORIZONTAL) {
            lyrics_left_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            lyrics_center_align.setBackgroundTintList(ColorStateList.valueOf(0xffff0000));
            lyrics_right_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
        } else {
            lyrics_left_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            lyrics_center_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            lyrics_right_align.setBackgroundTintList(ColorStateList.valueOf(0xffff0000));
        }
        if (FullscreenActivity.presoInfoAlign == Gravity.START) {
            info_left_align.setBackgroundTintList(ColorStateList.valueOf(0xffff0000));
            info_center_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            info_right_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
        } else if (FullscreenActivity.presoInfoAlign == Gravity.END) {
            info_left_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            info_center_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            info_right_align.setBackgroundTintList(ColorStateList.valueOf(0xffff0000));
        } else {
            info_left_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
            info_center_align.setBackgroundTintList(ColorStateList.valueOf(0xffff0000));
            info_right_align.setBackgroundTintList(ColorStateList.valueOf(0xff555555));
        }

        // If chords are being show, hide the lyrics align, otherwise it is ok to show
        int vis = View.VISIBLE;
        if (FullscreenActivity.presoShowChords) {
            vis = View.GONE;
        }
        lyrics_left_align.setVisibility(vis);
        lyrics_center_align.setVisibility(vis);
        lyrics_right_align.setVisibility(vis);
        lyrics_title_align.setVisibility(vis);
    }

    public void setCheckBoxes() {

        switch (FullscreenActivity.backgroundToUse) {
            case "img1":
                image1CheckBox.setChecked(true);
                image2CheckBox.setChecked(false);
                video1CheckBox.setChecked(false);
                video2CheckBox.setChecked(false);
                break;
            case "img2":
                image1CheckBox.setChecked(false);
                image2CheckBox.setChecked(true);
                video1CheckBox.setChecked(false);
                video2CheckBox.setChecked(false);
                break;
            case "vid1":
                image1CheckBox.setChecked(false);
                image2CheckBox.setChecked(false);
                video1CheckBox.setChecked(true);
                video2CheckBox.setChecked(false);
                break;
            case "vid2":
                image1CheckBox.setChecked(false);
                image2CheckBox.setChecked(false);
                video1CheckBox.setChecked(false);
                video2CheckBox.setChecked(true);
                break;
        }
    }

    public void chooseFile() {
        // This calls an intent to choose a file
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        if (backgroundFileType.equals("video")) {
            intent.setType("video/*");
        } else {
            intent.setType("image/*");
        }
        startActivityForResult(intent, 26);
    }

    public void setButtonBackground(ImageView v, String background) {
        // The default uri is just as it is saved
        Uri uri = Uri.parse(background);

        if (background.equals("ost_logo.png") || background.equals("ost_bg.png")) {
            // The built in logo or background
            uri = storageAccess.getUriForItem(getActivity(), preferences, "Backgrounds", "", background);

        } else if (background.contains("OpenSong/Backgrounds/")) {
            // Decide if the image is a localised OpenSong/Backgrounds file
            background = storageAccess.getPartOfUri(uri, "OpenSong/Backgrounds/");
            background = background.substring("OpenSong/Backgrounds/".length());
            uri = storageAccess.getUriForItem(getActivity(), preferences, "Backgrounds", "", background);
        }
        Log.d("d", "image uri=" + uri);
        Log.d("d", "uriExists=" + storageAccess.uriExists(getActivity(), uri));
        if (storageAccess.uriExists(getActivity(),uri)) {
            v.setBackgroundColor(0x00000000);
            RequestOptions myOptions = new RequestOptions()
                    .override(120,90);
            Glide.with(getActivity()).load(uri).apply(myOptions).into(v);
        }
    }

    public void showorhideView(View v, boolean show) {
        if (show) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    public void sendUpdateToScreen(String what) {
        if (!firsttime) {
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

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            FullscreenActivity.xmargin_presentation = setXMarginProgressBar.getProgress();
            FullscreenActivity.ymargin_presentation = setYMarginProgressBar.getProgress();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            Preferences.savePreferences();
            sendUpdateToScreen("margins");
        }
    }
    private class setFontSizeListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            FullscreenActivity.presoFontSize = progress + 4;
            String newtext = (progress + 4) + " sp";
            fontSizePreview.setText(newtext);
            fontSizePreview.setTextSize(progress + 4);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            FullscreenActivity.presoFontSize = seekBar.getProgress() + 4;
            sendUpdateToScreen("manualfontsize");
        }
    }
    private class setMaxFontSizeListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            FullscreenActivity.presoMaxFontSize = progress + 4;
            String newtext = (progress + 4) + " sp";
            maxfontSizePreview.setText(newtext);
            maxfontSizePreview.setTextSize(progress + 4);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            FullscreenActivity.presoMaxFontSize = seekBar.getProgress() + 4;
            sendUpdateToScreen("maxfontsize");
        }

    }
    private class presoSectionSizeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            FullscreenActivity.presoTitleSize = 1.0f * presoTitleSizeSeekBar.getProgress();
            FullscreenActivity.presoAuthorSize = 1.0f * presoAuthorSizeSeekBar.getProgress();
            FullscreenActivity.presoCopyrightSize = 1.0f * presoCopyrightSizeSeekBar.getProgress();
            FullscreenActivity.presoAlertSize = 1.0f * presoAlertSizeSeekBar.getProgress();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            sendUpdateToScreen("info");
        }
    }
    private class presoAlphaListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            FullscreenActivity.presoAlpha = (float)progress / 100f;
            String update = progress + " %";
            presoAlphaText.setText(update);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            sendUpdateToScreen("backgrounds");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    void setupPreviews() {
        // This sets the background thumbnails for the images/videos/logo
        setButtonBackground(chooseLogoButton,FullscreenActivity.customLogo);
        setButtonBackground(chooseImage1Button,FullscreenActivity.backgroundImage1);
        setButtonBackground(chooseImage2Button,FullscreenActivity.backgroundImage2);
        setButtonBackground(chooseVideo1Button,FullscreenActivity.backgroundVideo1);
        setButtonBackground(chooseVideo2Button,FullscreenActivity.backgroundVideo2);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Uri uri = resultData.getData();

        if (resultCode == -1 && uri != null) {

            // Save the location
            switch (PresenterMode.whatBackgroundLoaded) {
                case "logo":
                    FullscreenActivity.customLogo = uri.toString();
                    preferences.setMyPreferenceString(getActivity(), "customLogo", uri.toString());
                    break;
                case "image1":
                    FullscreenActivity.backgroundImage1 = uri.toString();
                    preferences.setMyPreferenceString(getActivity(), "backgroundImage1", uri.toString());
                    break;
                case "image2":
                    FullscreenActivity.backgroundImage2 = uri.toString();
                    preferences.setMyPreferenceString(getActivity(), "backgroundImage2", uri.toString());
                    break;
                case "video1":
                    FullscreenActivity.backgroundVideo1 = uri.toString();
                    preferences.setMyPreferenceString(getActivity(), "backgroundVideo1", uri.toString());
                    break;
                case "video2":
                    FullscreenActivity.backgroundVideo2 = uri.toString();
                    preferences.setMyPreferenceString(getActivity(), "backgroundVideo2", uri.toString());
                    break;
            }

            // Update the storage text
            setupPreviews();
        }
    }

}
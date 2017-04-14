package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class PopUpLayoutFragment extends DialogFragment {

    static PopUpLayoutFragment newInstance() {
        PopUpLayoutFragment frag;
        frag = new PopUpLayoutFragment();
        return frag;
    }

    SeekBar setXMarginProgressBar;
    SeekBar setYMarginProgressBar;
    SwitchCompat toggleAutoScaleButton;
    SwitchCompat toggleChordsButton;
    SeekBar setMaxFontSizeProgressBar;
    SeekBar setFontSizeProgressBar;
    TextView fontSizePreview;
    TextView maxfontSizePreview;
    Spinner presoFontSpinner;
    SeekBar presoTitleSizeSeekBar;
    SeekBar presoAuthorSizeSeekBar;
    SeekBar presoCopyrightSizeSeekBar;
    SeekBar presoAlertSizeSeekBar;
    LinearLayout margins_LinearLayout;
    LinearLayout chords_LinearLayout;
    LinearLayout scale1_LinearLayout;
    LinearLayout scale2_LinearLayout;
    LinearLayout fonts_LinearLayout;

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
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            title.setText(getActivity().getResources().getString(R.string.connected_display));
            FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doSave();
                }
            });
            FloatingActionButton saveMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.saveMe);
            saveMe.setVisibility(View.GONE);
        } else {
            getDialog().setTitle(getActivity().getResources().getString(R.string.connected_display));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        final View V = inflater.inflate(R.layout.popup_layout, container, false);

        setXMarginProgressBar = (SeekBar) V.findViewById(R.id.setXMarginProgressBar);
        setYMarginProgressBar = (SeekBar) V.findViewById(R.id.setYMarginProgressBar);
        toggleAutoScaleButton = (SwitchCompat) V.findViewById(R.id.toggleAutoScaleButton);
        toggleChordsButton = (SwitchCompat) V.findViewById(R.id.toggleChordsButton);
        setMaxFontSizeProgressBar = (SeekBar) V.findViewById(R.id.setMaxFontSizeProgressBar);
        setFontSizeProgressBar = (SeekBar) V.findViewById(R.id.setFontSizeProgressBar);
        fontSizePreview = (TextView) V.findViewById(R.id.fontSizePreview);
        maxfontSizePreview = (TextView) V.findViewById(R.id.maxfontSizePreview);
        presoFontSpinner = (Spinner) V.findViewById(R.id.presoFontSpinner);
        presoTitleSizeSeekBar = (SeekBar) V.findViewById(R.id.presoTitleSizeSeekBar);
        presoAuthorSizeSeekBar = (SeekBar) V.findViewById(R.id.presoAuthorSizeSeekBar);
        presoCopyrightSizeSeekBar = (SeekBar) V.findViewById(R.id.presoCopyrightSizeSeekBar);
        presoAlertSizeSeekBar = (SeekBar) V.findViewById(R.id.presoAlertSizeSeekBar);
        margins_LinearLayout = (LinearLayout) V.findViewById(R.id.margins_LinearLayout);
        chords_LinearLayout = (LinearLayout) V.findViewById(R.id.chords_LinearLayout);
        scale1_LinearLayout = (LinearLayout) V.findViewById(R.id.scale1_LinearLayout);
        scale2_LinearLayout = (LinearLayout) V.findViewById(R.id.scale2_LinearLayout);
        fonts_LinearLayout = (LinearLayout) V.findViewById(R.id.fonts_LinearLayout);

        SetTypeFace.setTypeface();

        // Hide the appropriate views for Stage and Performance mode
        switch (FullscreenActivity.whichMode) {
            case "Performance":
                margins_LinearLayout.setVisibility(View.VISIBLE);
                chords_LinearLayout.setVisibility(View.VISIBLE);
                scale1_LinearLayout.setVisibility(View.VISIBLE);
                toggleAutoScaleButton.setVisibility(View.GONE);
                scale2_LinearLayout.setVisibility(View.GONE);
                fonts_LinearLayout.setVisibility(View.GONE);

                break;
            case "Stage":
                margins_LinearLayout.setVisibility(View.VISIBLE);
                chords_LinearLayout.setVisibility(View.VISIBLE);
                scale1_LinearLayout.setVisibility(View.VISIBLE);
                toggleAutoScaleButton.setVisibility(View.GONE);
                scale2_LinearLayout.setVisibility(View.GONE);
                fonts_LinearLayout.setVisibility(View.GONE);

                break;
            default:
                margins_LinearLayout.setVisibility(View.VISIBLE);
                chords_LinearLayout.setVisibility(View.VISIBLE);
                scale1_LinearLayout.setVisibility(View.VISIBLE);
                scale2_LinearLayout.setVisibility(View.VISIBLE);
                fonts_LinearLayout.setVisibility(View.VISIBLE);
                break;
        }

        // Set the stuff up to what it should be from preferences
        toggleChordsButton.setChecked(FullscreenActivity.presoShowChords);
        fontSizePreview.setTypeface(FullscreenActivity.presofont);
        String newtext = (FullscreenActivity.presoFontSize) + " sp";
        fontSizePreview.setText(newtext);
        fontSizePreview.setTextSize(FullscreenActivity.presoFontSize);
        maxfontSizePreview.setTypeface(FullscreenActivity.presofont);
        newtext = (FullscreenActivity.presoMaxFontSize) + " sp";
        maxfontSizePreview.setText(newtext);
        maxfontSizePreview.setTextSize(FullscreenActivity.presoMaxFontSize);
        setXMarginProgressBar.setMax(100);
        setYMarginProgressBar.setMax(100);
        setMaxFontSizeProgressBar.setMax(70);
        setFontSizeProgressBar.setMax(70);
        presoTitleSizeSeekBar.setMax(32);
        presoAuthorSizeSeekBar.setMax(32);
        presoCopyrightSizeSeekBar.setMax(32);
        presoAlertSizeSeekBar.setMax(32);
        presoTitleSizeSeekBar.setProgress(FullscreenActivity.presoTitleSize);
        presoAuthorSizeSeekBar.setProgress(FullscreenActivity.presoAuthorSize);
        presoCopyrightSizeSeekBar.setProgress(FullscreenActivity.presoCopyrightSize);
        presoAlertSizeSeekBar.setProgress(FullscreenActivity.presoAlertSize);

        setXMarginProgressBar.setProgress(FullscreenActivity.xmargin_presentation);
        setYMarginProgressBar.setProgress(FullscreenActivity.ymargin_presentation);
        setFontSizeProgressBar.setProgress(FullscreenActivity.presoFontSize - 4);
        setMaxFontSizeProgressBar.setProgress(FullscreenActivity.presoMaxFontSize - 4);

        if (FullscreenActivity.presoAutoScale) {
            setFontSizeProgressBar.setEnabled(false);
            setFontSizeProgressBar.setAlpha(0.5f);
            setMaxFontSizeProgressBar.setEnabled(true);
            setMaxFontSizeProgressBar.setAlpha(1.0f);
            maxfontSizePreview.setAlpha(1.0f);
            toggleAutoScaleButton.setChecked(true);
            fontSizePreview.setAlpha(0.5f);
        } else {
            setFontSizeProgressBar.setEnabled(true);
            setFontSizeProgressBar.setAlpha(1.0f);
            setMaxFontSizeProgressBar.setEnabled(false);
            setMaxFontSizeProgressBar.setAlpha(0.5f);
            maxfontSizePreview.setAlpha(0.5f);
            toggleAutoScaleButton.setChecked(false);
            fontSizePreview.setAlpha(1.0f);
        }

        // Set up the font spinner
        ArrayList<String> font_choices = new ArrayList<>();
        font_choices.add(getResources().getString(R.string.font_default));
        font_choices.add(getResources().getString(R.string.font_monospace));
        font_choices.add(getResources().getString(R.string.font_sans));
        font_choices.add(getResources().getString(R.string.font_serif));
        font_choices.add(getResources().getString(R.string.font_firasanslight));
        font_choices.add(getResources().getString(R.string.font_firasansregular));
        font_choices.add(getResources().getString(R.string.font_kaushanscript));
        font_choices.add(getResources().getString(R.string.font_latolight));
        font_choices.add(getResources().getString(R.string.font_latoregular));
        font_choices.add(getResources().getString(R.string.font_leaguegothic));
        ArrayAdapter<String> choose_fonts = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, font_choices);
        choose_fonts.setDropDownViewResource(R.layout.my_spinner);
        presoFontSpinner.setAdapter(choose_fonts);
        presoFontSpinner.setSelection(FullscreenActivity.mypresofontnum);

        // Set listeners
        presoFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FullscreenActivity.mypresofontnum = position;
                SetTypeFace.setTypeface();
                fontSizePreview.setTypeface(FullscreenActivity.presofont);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        setXMarginProgressBar.setOnSeekBarChangeListener(new setMargin_Listener());
        setYMarginProgressBar.setOnSeekBarChangeListener(new setMargin_Listener());
        setMaxFontSizeProgressBar.setOnSeekBarChangeListener(new setMaxFontSizeListener());
        setFontSizeProgressBar.setOnSeekBarChangeListener(new setFontSizeListener());
        presoTitleSizeSeekBar.setOnSeekBarChangeListener(new presoSectionSizeListener());
        presoAuthorSizeSeekBar.setOnSeekBarChangeListener(new presoSectionSizeListener());
        presoCopyrightSizeSeekBar.setOnSeekBarChangeListener(new presoSectionSizeListener());
        presoAlertSizeSeekBar.setOnSeekBarChangeListener(new presoSectionSizeListener());

        toggleChordsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.presoShowChords = b;
                PresentationService.ExternalDisplay.doUpdate();
            }
        });
        toggleAutoScaleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setFontSizeProgressBar.setEnabled(false);
                    setFontSizeProgressBar.setAlpha(0.5f);
                    fontSizePreview.setAlpha(0.5f);
                    setMaxFontSizeProgressBar.setEnabled(true);
                    setMaxFontSizeProgressBar.setAlpha(1.0f);
                    maxfontSizePreview.setAlpha(1.0f);

                    PresenterMode.autoscale = true;
                    FullscreenActivity.presoAutoScale = true;
                    Preferences.savePreferences();
                    //MyPresentation.doScale();
                    MyPresentation.resetFontSize();
                } else {
                    setFontSizeProgressBar.setEnabled(true);
                    setFontSizeProgressBar.setAlpha(1.0f);
                    fontSizePreview.setAlpha(1.0f);
                    setMaxFontSizeProgressBar.setEnabled(false);
                    setMaxFontSizeProgressBar.setAlpha(0.5f);
                    maxfontSizePreview.setAlpha(0.5f);
                    PresenterMode.autoscale = false;
                    FullscreenActivity.presoAutoScale = false;
                    Preferences.savePreferences();
                    MyPresentation.updateFontSize();
                }
            }
        });

        return V;
    }

    public void doSave() {
        // Grab the variables, save and close
        FullscreenActivity.xmargin_presentation = setXMarginProgressBar.getProgress();
        FullscreenActivity.ymargin_presentation = setYMarginProgressBar.getProgress();
        FullscreenActivity.presoAutoScale = toggleAutoScaleButton.isChecked();
        FullscreenActivity.presoFontSize = setFontSizeProgressBar.getProgress() + 4;
        FullscreenActivity.presoMaxFontSize = setMaxFontSizeProgressBar.getProgress() + 4;
        Preferences.savePreferences();
        dismiss();
    }

    private class setMargin_Listener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            PresenterMode.tempxmargin = setXMarginProgressBar.getProgress();
            PresenterMode.tempymargin = setYMarginProgressBar.getProgress();
            if (FullscreenActivity.whichMode.equals("Presentation")) {
                MyPresentation.changeMargins();
            } else {
                if (FullscreenActivity.isPresenting) {
                    try {
                        PresentationService.ExternalDisplay.changeMargins();
                    } catch (Exception e) {
                        FullscreenActivity.myToastMessage = getActivity().getString(R.string.nodisplays);
                        ShowToast.showToast(getActivity());
                        dismiss();
                    }
                } else {
                    FullscreenActivity.myToastMessage = getActivity().getString(R.string.nodisplays);
                    ShowToast.showToast(getActivity());
                    dismiss();
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            FullscreenActivity.xmargin_presentation = setXMarginProgressBar.getProgress();
            FullscreenActivity.ymargin_presentation = setYMarginProgressBar.getProgress();
            if (FullscreenActivity.whichMode.equals("Presentation")) {
                MyPresentation.changeMargins();
            } else {
                if (FullscreenActivity.isPresenting) {
                    try {
                        PresentationService.ExternalDisplay.changeMargins();
                        PresentationService.ExternalDisplay.getScreenSizes();
                        PresentationService.ExternalDisplay.doUpdate();
                    } catch (Exception e) {
                        FullscreenActivity.myToastMessage = getActivity().getString(R.string.nodisplays);
                        ShowToast.showToast(getActivity());
                        dismiss();
                    }
                } else {
                    FullscreenActivity.myToastMessage = getActivity().getString(R.string.nodisplays);
                    ShowToast.showToast(getActivity());
                    dismiss();
                }

                // Save preferences
                Preferences.savePreferences();
            }
        }
    }

    private class setFontSizeListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            FullscreenActivity.presoFontSize = progress + 4;
            String newtext = (progress + 4) + " sp";
            fontSizePreview.setText(newtext);
            fontSizePreview.setTextSize(progress + 4);
            MyPresentation.updateFontSize();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            FullscreenActivity.presoFontSize = seekBar.getProgress() + 4;
            // Save preferences
            Preferences.savePreferences();
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

            if (!FullscreenActivity.whichMode.equals("Presentation")) {
                if (FullscreenActivity.isPresenting) {
                    try {
                        PresentationService.ExternalDisplay.doUpdate();
                    } catch (Exception e) {
                        FullscreenActivity.myToastMessage = getActivity().getString(R.string.nodisplays);
                        ShowToast.showToast(getActivity());
                        dismiss();
                    }
                } else {
                    FullscreenActivity.myToastMessage = getActivity().getString(R.string.nodisplays);
                    ShowToast.showToast(getActivity());
                    dismiss();
                }
            }
            Preferences.savePreferences();
        }
    }

    private class presoSectionSizeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            FullscreenActivity.presoTitleSize = presoTitleSizeSeekBar.getProgress();
            FullscreenActivity.presoAuthorSize = presoAuthorSizeSeekBar.getProgress();
            FullscreenActivity.presoCopyrightSize = presoCopyrightSizeSeekBar.getProgress();
            FullscreenActivity.presoAlertSize = presoAlertSizeSeekBar.getProgress();
            MyPresentation.updateFontSize();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // Save the preferences
            Preferences.savePreferences();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}

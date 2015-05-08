/*
 * Copyright (c) 2015.
 * The code is provided free of charge.  You can use, modify, contribute and improve it as long as this source is referenced.
 * Commercial use should seek permission.
 */

package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by gareth on 07/05/15.
 */
public class PopUpLayoutFragment extends DialogFragment {

    static PopUpLayoutFragment newInstance() {
        PopUpLayoutFragment frag;
        frag = new PopUpLayoutFragment();
        return frag;
    }

    SeekBar setXMarginProgressBar;
    SeekBar setYMarginProgressBar;
    ToggleButton toggleAutoScaleButton;
    SeekBar setFontSizeProgressBar;
    TextView fontSizePreview;
    Button closeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.alert));
        final View V = inflater.inflate(R.layout.popup_layout, container, false);

        setXMarginProgressBar = (SeekBar) V.findViewById(R.id.setXMarginProgressBar);
        setYMarginProgressBar = (SeekBar) V.findViewById(R.id.setYMarginProgressBar);
        toggleAutoScaleButton = (ToggleButton) V.findViewById(R.id.toggleAutoScaleButton);
        setFontSizeProgressBar = (SeekBar) V.findViewById(R.id.setFontSizeProgressBar);
        fontSizePreview = (TextView) V.findViewById(R.id.fontSizePreview);
        closeLayout = (Button) V.findViewById(R.id.closeLayout);

        // Set the stuff up to what it should be from preferences
        fontSizePreview.setText((FullscreenActivity.presoFontSize - 4) + " sp");
        fontSizePreview.setTextSize(FullscreenActivity.presoFontSize);
        setXMarginProgressBar.setMax(200);
        setYMarginProgressBar.setMax(200);
        setFontSizeProgressBar.setMax(50);

        setXMarginProgressBar.setProgress(FullscreenActivity.xmargin_presentation);
        setYMarginProgressBar.setProgress(FullscreenActivity.ymargin_presentation);
        setFontSizeProgressBar.setProgress(FullscreenActivity.presoFontSize - 4);

        if (FullscreenActivity.presoAutoScale) {
            setFontSizeProgressBar.setEnabled(false);
            setFontSizeProgressBar.setAlpha(0.5f);
            toggleAutoScaleButton.setChecked(true);
            fontSizePreview.setAlpha(0.5f);
        } else {
            setFontSizeProgressBar.setEnabled(true);
            setFontSizeProgressBar.setAlpha(1.0f);
            toggleAutoScaleButton.setChecked(false);
            fontSizePreview.setAlpha(1.0f);
        }

        // Set listeners
        setXMarginProgressBar.setOnSeekBarChangeListener(new setMargin_Listener());
        setYMarginProgressBar.setOnSeekBarChangeListener(new setMargin_Listener());
        setFontSizeProgressBar.setOnSeekBarChangeListener(new setFontSizeListener());
        closeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Grab the variables, save and close
                FullscreenActivity.xmargin_presentation = setXMarginProgressBar.getProgress();
                FullscreenActivity.ymargin_presentation = setYMarginProgressBar.getProgress();
                if (toggleAutoScaleButton.isChecked()) {
                    FullscreenActivity.presoAutoScale = true;
                } else {
                    FullscreenActivity.presoAutoScale = false;
                }
                FullscreenActivity.presoFontSize = setFontSizeProgressBar.getProgress() + 4;

                Preferences.savePreferences();
                dismiss();
            }
        });

        toggleAutoScaleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setFontSizeProgressBar.setEnabled(false);
                    PresenterMode.autoscale = true;
                    FullscreenActivity.presoAutoScale = true;
                    Preferences.savePreferences();
                    MyPresentation.doScale();
                } else {
                    setFontSizeProgressBar.setEnabled(true);
                    PresenterMode.autoscale = false;
                    FullscreenActivity.presoAutoScale = false;
                    Preferences.savePreferences();
                    MyPresentation.updateFontSize();
                }
            }
        });

        return V;
    }

    private class setMargin_Listener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            PresenterMode.tempxmargin = setXMarginProgressBar.getProgress();
            PresenterMode.tempymargin = setYMarginProgressBar.getProgress();
            MyPresentation.changeMargins();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            FullscreenActivity.xmargin_presentation = setXMarginProgressBar.getProgress();
            FullscreenActivity.ymargin_presentation = setYMarginProgressBar.getProgress();
            // Save preferences
            Preferences.savePreferences();
        }
    }

    private class setFontSizeListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            PresenterMode.tempfontsize = progress + 4;
            fontSizePreview.setText((progress+4) + " sp");
            fontSizePreview.setTextSize(progress+4);
            MyPresentation.updateFontSize();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {
            FullscreenActivity.presoFontSize = seekBar.getProgress();
            // Save preferences
            Preferences.savePreferences();
        }
    }

}

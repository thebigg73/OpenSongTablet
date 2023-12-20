package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

public class MidiSlider extends LinearLayout {

    private final Slider slider;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MidiSlider";
    private final TextView sliderName, sliderValue, sliderMidi;
    private final LinearLayout sliderValues;
    private int sliderCC = 0, sliderVal = 0, sliderChannel = 1;

    public MidiSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_midi_slider, this);

        slider = findViewById(R.id.verticalSlider);
        sliderName = findViewById(R.id.sliderName);
        sliderMidi = findViewById(R.id.sliderMidi);
        sliderValue = findViewById(R.id.sliderValue);
        sliderValues = findViewById(R.id.sliderValues);

        slider.setId(View.generateViewId());
        sliderName.setId(View.generateViewId());
        sliderMidi.setId(View.generateViewId());
        sliderValue.setId(View.generateViewId());
        sliderValues.setId(View.generateViewId());
    }

    public void addOnSliderTouchListener(Slider.OnSliderTouchListener onSliderTouchListener) {
        slider.addOnSliderTouchListener(onSliderTouchListener);
    }
    public void addOnChangeListener(Slider.OnChangeListener onChangeListener){
        slider.addOnChangeListener(onChangeListener);
    }
    public void setOnClickListener(OnClickListener onClickListener) {
        sliderValues.setOnClickListener(onClickListener);
    }
    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        sliderValues.setOnLongClickListener(onLongClickListener);
    }
    public void setLabelFormatter(LabelFormatter labelFormatter) {
        slider.setLabelFormatter(labelFormatter);
    }

    public void initialiseSliderValues(String sliderTitle,
                                       int sliderChannel, int sliderCC, int sliderVal) {
        this.sliderChannel = sliderChannel;
        this.sliderCC = sliderCC;
        this.sliderVal = sliderVal;
        sliderName.setText(sliderTitle);
        slider.setValue(sliderVal);
        updateMidiCode();
    }

    public void updateSliderVal(int sliderVal) {
        this.sliderVal = sliderVal;
        updateMidiCode();
    }
    public void updateMidiCode() {
        String text = "(" + sliderChannel + ") CC "+sliderCC+" "+sliderVal;
        sliderMidi.setText(text);
        if (sliderValue!=null) {
            String sliderText = String.valueOf(sliderVal);
            sliderValue.setText(sliderText);
        }
    }

    public String getMidiCode() {
        return "MIDI"+sliderChannel+":CC"+sliderCC+":V"+sliderVal;
    }

    public int getSliderVal() {
        return sliderVal;
    }

    public LinearLayout getSliderValues() {
        return sliderValues;
    }

    public Slider getSlider() {
        return slider;
    }
}

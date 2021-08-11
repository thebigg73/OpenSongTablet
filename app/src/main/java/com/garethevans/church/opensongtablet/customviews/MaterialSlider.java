package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.slider.Slider;

public class MaterialSlider extends LinearLayout {

    private final Slider slider;
    private final TextView titleTextView, valueTextView;

    public MaterialSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_slider, this);

        int[] set = new int[]{android.R.attr.text,
                android.R.attr.hint,
                android.R.attr.valueFrom,
                android.R.attr.valueTo,
                R.attr.stepSize,
                android.R.attr.value,
                R.attr.trackColor};
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        CharSequence text = a.getText(0);
        CharSequence hint = a.getText(1);
        float valueFrom = a.getFloat(2, 0.0f);
        float valueTo = a.getFloat(3,10.0f);
        float stepSize = a.getFloat(4,1.0f);
        float value = a.getFloat(5,0.0f);
        int track = a.getColor(6,0);

        slider = findViewById(R.id.slider);
        titleTextView = findViewById(R.id.titleText);
        valueTextView = findViewById(R.id.valueText);

        if (text!=null) {
            titleTextView.setText(text);
        } else {
            titleTextView.setVisibility(View.GONE);
        }
        if (hint!=null) {
            valueTextView.setText(hint);
        } else {
            valueTextView.setVisibility(View.GONE);
        }

        Log.d("MaterialSlider","from:"+valueFrom+"  to:"+valueTo+"  step:"+stepSize);
        slider.setValueFrom(valueFrom);
        slider.setValueTo(valueTo);
        slider.setStepSize(stepSize);
        slider.setValue(value);

        if (track!=0) {
            slider.setTrackTintList(ColorStateList.valueOf(track));
        }
        a.recycle();
    }

    // The getters
    public float getValue() {
        return slider.getValue();
    }
    public Slider getSlider() {
        return slider;
    }

    // The setters
    public void setValueFrom(float valueFrom) {
        // Check any set value is ok
        if (slider.getValue() < valueFrom) {
            slider.setValue(valueFrom);
        }
        slider.setValueFrom(valueFrom);
    }
    public void setValueTo(float valueTo) {
        // Check any set value is ok
        if (slider.getValue() > valueTo) {
            slider.setValue(valueTo);
        }
        slider.setValueTo(valueTo);
    }
    public void setStepSize(float stepSize) {
        slider.setStepSize(stepSize);
    }
    public void setValue(float value) {
        // Check it is within the bounds!
        if (value > slider.getValueTo()) {
            value = slider.getValueTo();
        } else if (value < slider.getValueFrom()) {
            value = slider.getValueFrom();
        }
        slider.setValue(value);
    }
    public void setHint(String hint) {
        if (hint!=null && !hint.isEmpty()) {
            valueTextView.setVisibility(View.VISIBLE);
            valueTextView.setText(hint);
        } else {
            valueTextView.setVisibility(View.GONE);
        }
    }
    public void setText(String text) {
        titleTextView.setText(text);
    }
    public void setHintTextSize(float textSize) {
        valueTextView.setTextSize(textSize);
    }
    public void addOnSliderTouchListener(Slider.OnSliderTouchListener onSliderTouchListener) {
        slider.addOnSliderTouchListener(onSliderTouchListener);
    }
    public void addOnChangeListener(Slider.OnChangeListener onChangeListener){
        slider.addOnChangeListener(onChangeListener);
    }
}

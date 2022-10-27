package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

public class MaterialSlider extends LinearLayout {

    private final Slider slider;
    private final TextView titleTextView, valueTextView;
    private final float stepSize;
    public MaterialSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_material_slider, this);

        int[] set = new int[]{android.R.attr.text,
                android.R.attr.hint,
                android.R.attr.valueFrom,
                android.R.attr.valueTo,
                R.attr.stepSize,
                android.R.attr.value,
                R.attr.trackColor,
                R.attr.trackHeight,
                R.attr.thumbRadius,
                R.attr.thumbColor
        };
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        CharSequence text = a.getText(0);
        CharSequence hint = a.getText(1);
        float valueFrom = a.getFloat(2, 0.0f);
        float valueTo = a.getFloat(3,10.0f);
        stepSize = a.getFloat(4,1.0f);
        float value = a.getFloat(5,0.0f);
        int track = a.getColor(6,0);
        float height = a.getDimensionPixelSize(7,0);
        float radius = a.getDimensionPixelSize(8,0);
        int thumb = a.getColor(9,0);

        slider = findViewById(R.id.slider);
        titleTextView = findViewById(R.id.titleText);
        valueTextView = findViewById(R.id.valueText);

        slider.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        valueTextView.setId(View.generateViewId());

        if (text==null) {
            text = "";
        }
        setText(text.toString());

        if (hint==null) {
            hint = "";
        }
        setHint(hint.toString());

        slider.setValueFrom(valueFrom);
        slider.setValueTo(valueTo);
        slider.setStepSize(stepSize);
        setValue(value);

        if (track!=0) {
            slider.setTrackTintList(ColorStateList.valueOf(track));
        }
        if (height!=0) {
            slider.setTrackHeight((int)height);
        }
        if (radius!=0) {
            slider.setThumbRadius((int)radius);
            slider.setThumbStrokeWidth(radius);
        }
        if (thumb!=0) {
            slider.setThumbStrokeColor(ColorStateList.valueOf(thumb));
            slider.setThumbTintList(ColorStateList.valueOf(thumb));
        }
        a.recycle();
    }

    // The getters
    public float getValue() {
        return slider.getValue();
    }
    public float getValueTo() {
        return slider.getValueTo();
    }
    public float getValueFrom() {
        return slider.getValueFrom();
    }
    public Slider getSlider() {
        return slider;
    }

    // The setters
    public void setValueFrom(float valueFrom) {
        // Check any set value is okay
        if (slider.getValue() < valueFrom) {
            slider.setValue(valueFrom);
        }
        slider.setValueFrom(valueFrom);
    }
    public void setValueTo(float valueTo) {
        // Check any set value is okay
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
        }
        if (value < slider.getValueFrom()) {
            value = slider.getValueFrom();
        }
        // Check it fits with any set step size
        if (stepSize>1) {
            // Round it
            value = Math.round(value / stepSize) * stepSize;
        }
        try {
            slider.setValue(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setHint(String hint) {
        if (hint!=null && !hint.isEmpty()) {
            valueTextView.post(() -> {
                if (valueTextView.getVisibility()!=View.VISIBLE) {
                    valueTextView.setVisibility(View.VISIBLE);
                }
                valueTextView.setText(hint);
            });

        } else {
            valueTextView.post(() -> {
                if (valueTextView.getVisibility()!=View.GONE) {
                    valueTextView.setVisibility(View.GONE);
                }
            });
        }
    }
    public void setText(String text) {
        titleTextView.setText(text);
        if (text==null || text.isEmpty()) {
            titleTextView.setVisibility(View.GONE);
        } else {
            titleTextView.setVisibility(View.VISIBLE);
        }
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
    public void setLabelFormatter(LabelFormatter labelFormatter) {
        slider.setLabelFormatter(labelFormatter);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        slider.setEnabled(enabled);
    }
}

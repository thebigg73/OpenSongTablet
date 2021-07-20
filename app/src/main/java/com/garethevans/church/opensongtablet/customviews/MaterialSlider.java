package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
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
                android.R.attr.stepSize,
                android.R.attr.value};
        TypedArray a = context.obtainStyledAttributes(attrs, set);
        CharSequence text = a.getText(0);
        CharSequence hint = a.getText(1);
        float valueFrom = a.getFloat(2, 0);
        float valueTo = a.getFloat(3,11);
        float stepSize = a.getFloat(4,1);
        float value = a.getFloat(5,0);

        slider = findViewById(R.id.slider);
        titleTextView = findViewById(R.id.titleText);
        valueTextView = findViewById(R.id.valueText);

        if (text!=null) {
            titleTextView.setText(text);
        }
        if (hint!=null) {
            valueTextView.setText(hint);
        }
        slider.setValueFrom(valueFrom);
        slider.setValueTo(valueTo);
        slider.setStepSize(stepSize);
        slider.setValue(value);

        a.recycle();
    }

    // The getters
    public float getValue() {
        return slider.getValue();
    }

    // The setters
    public void setValue(float value) {
        slider.setValue(value);
    }
    public void setHint(String hint) {
        valueTextView.setText(hint);
    }
    public void setTitle(String title) {
        titleTextView.setText(title);
    }
    public void addOnSliderTouchListener(Slider.OnSliderTouchListener onSliderTouchListener) {
        slider.addOnSliderTouchListener(onSliderTouchListener);
    }
    public void addOnChangeListener(Slider.OnChangeListener onChangeListener){
        slider.addOnChangeListener(onChangeListener);
    }
}

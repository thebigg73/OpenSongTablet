package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

public class MaterialSlider extends LinearLayout {

    private final Slider slider;
    private final TextView titleTextView, valueTextView, bottomHintView;
    private final FloatingActionButton minusFAB, plusFAB;
    private final FrameLayout minusHolder, plusHolder;
    private final float stepSize;
    private boolean adjustButtons;
    public MaterialSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_material_slider, this);

        int[] set = new int[]{android.R.attr.text,  // 0
                android.R.attr.hint,                // 1
                android.R.attr.valueFrom,           // 2
                android.R.attr.valueTo,             // 3
                R.attr.stepSize,                    // 4
                android.R.attr.value,               // 5
                R.attr.trackColor,                  // 6
                R.attr.trackHeight,                 // 7
                R.attr.thumbRadius,                 // 8
                R.attr.thumbColor,                  // 9
                R.attr.adjustable                   // 10
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
        adjustButtons = a.getBoolean(10,false);

        slider = findViewById(R.id.slider);
        titleTextView = findViewById(R.id.titleText);
        valueTextView = findViewById(R.id.valueText);
        bottomHintView = findViewById(R.id.bottomHint);
        minusHolder = findViewById(R.id.minusHolder);
        plusHolder = findViewById(R.id.plusHolder);
        minusFAB = findViewById(R.id.minus);
        plusFAB = findViewById(R.id.plus);

        slider.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        valueTextView.setId(View.generateViewId());
        bottomHintView.setId(View.generateViewId());
        minusHolder.setId(View.generateViewId());
        plusHolder.setId(View.generateViewId());
        minusFAB.setId(View.generateViewId());
        plusFAB.setId(View.generateViewId());

        setAdjustableButtons(adjustButtons);

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
        // If we are using +/- buttons we use the bottomHint
        bottomHintView.post(() -> {
            bottomHintView.setVisibility(adjustButtons && hint!=null && !hint.isEmpty() ? View.VISIBLE:View.GONE);
            bottomHintView.setText(adjustButtons && hint!=null && !hint.isEmpty() ? hint:"");
        });
        valueTextView.post(() -> {
            valueTextView.setVisibility(!adjustButtons && hint!=null && !hint.isEmpty() ? View.VISIBLE:View.GONE);
            valueTextView.setText(!adjustButtons && hint!=null && !hint.isEmpty() ? hint:"");
        });
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

    private void decreaseValue() {
        if (getValue()>getValueFrom()) {
            // Need to add in the OnChange !fromUser in the fragment using this if required
            setValue(getValue()-stepSize);
        }
    }
    private void increaseValue() {
        if (getValue()<getValueTo()) {
            // Need to add in the OnChange !fromUser in the fragment using this if required
            setValue(getValue()+stepSize);
        }
    }

    public void setAdjustableButtons(boolean adjustButtons) {
        this.adjustButtons = adjustButtons;
        minusHolder.setVisibility(adjustButtons ? View.VISIBLE:View.GONE);
        plusHolder.setVisibility(adjustButtons ? View.VISIBLE:View.GONE);
        plusHolder.setVisibility(adjustButtons ? View.VISIBLE:View.GONE);
        valueTextView.setVisibility(adjustButtons ? View.GONE:View.VISIBLE);
        bottomHintView.setVisibility(adjustButtons ? View.VISIBLE:View.GONE);
        minusFAB.setOnClickListener(v -> decreaseValue());
        plusFAB.setOnClickListener(v -> increaseValue());
        minusHolder.setOnClickListener(v -> {
            minusFAB.performClick();
            minusFAB.setPressed(true);
            minusFAB.postDelayed(() -> minusFAB.setPressed(false),500);
        });
        plusHolder.setOnClickListener(v -> {
            plusFAB.performClick();
            plusFAB.setPressed(true);
            plusFAB.postDelayed(() -> plusFAB.setPressed(false),500);
        });
        requestLayout();
    }
}

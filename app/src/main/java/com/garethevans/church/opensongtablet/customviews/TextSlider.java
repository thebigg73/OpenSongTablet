package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.slider.Slider;

public class TextSlider extends LinearLayout {

    private final TextView textLeft;
    private final TextView textRight;
    private final Slider slider;
    private final TextView heading;
    int activeColor;
    int inactiveColor;

    public TextSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_text_slider, this);

        activeColor = context.getResources().getColor(R.color.white);
        inactiveColor = context.getResources().getColor(R.color.lightgrey);
        textLeft = findViewById(R.id.textLeft);
        textRight = findViewById(R.id.textRight);
        slider = findViewById(R.id.slider);
        heading = findViewById(R.id.title);

        textLeft.setId(View.generateViewId());
        textRight.setId(View.generateViewId());
        slider.setId(View.generateViewId());
        heading.setId(View.generateViewId());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextSlider);
        CharSequence leftText = a.getText(R.styleable.TextSlider_textLeft);
        CharSequence rightText = a.getText(R.styleable.TextSlider_textRight);
        int position = a.getInt(R.styleable.TextSlider_value,0);
        CharSequence headingText = a.getText(R.styleable.TextSlider_heading);
        if (headingText!=null) {
            setHeading(headingText.toString());
        } else {
            setHeading(null);
        }

        textLeft.setText(leftText);
        textRight.setText(rightText);
        slider.setStepSize(1);
        slider.setValue(position);
        highlightSelectedText(position);
        slider.addOnChangeListener((slider, value, fromUser) -> highlightSelectedText(value));
        textLeft.setOnClickListener(view -> slider.setValue(0));
        textRight.setOnClickListener(view -> slider.setValue(1));
        a.recycle();
    }

    private void highlightSelectedText(float position) {
        if (position == 0) {
            textLeft.setTextColor(activeColor);
            textRight.setTextColor(inactiveColor);
        } else {
            textLeft.setTextColor(inactiveColor);
            textRight.setTextColor(activeColor);
        }
    }


    // The setters
    public void setTextLeft(String leftText) {
        textLeft.setText(leftText);
    }
    public void setTextRight(String rightText) {
        textRight.setText(rightText);
    }
    public void setSliderPos(int position) {
        highlightSelectedText(position);
        slider.setValue(position);
    }
    public void addOnChangeListener(Slider.OnChangeListener onChangeListener) {
        slider.addOnChangeListener(onChangeListener);
    }
    public void addOnSliderTouchListener(Slider.OnSliderTouchListener onSliderTouchListener) {
        slider.addOnSliderTouchListener(onSliderTouchListener);
    }
    public void setHeading(String headingText) {
        if (headingText==null || headingText.isEmpty()) {
            heading.setVisibility(View.GONE);
        } else {
            heading.setVisibility(View.VISIBLE);
            heading.setText(headingText);
        }
    }

    // The getters
    public String getTextLeft() {
        if (textLeft.getText()==null) {
            return "";
        } else {
            return textLeft.getText().toString();
        }
    }
    public String getTextRight(){
        if (textRight.getText()==null) {
            return "";
        } else {
            return textRight.getText().toString();
        }
    }
    public int getValue() {
        return (int)slider.getValue();
    }
    public String getValueText() {
        if (slider.getValue()==0) {
            return getTextLeft();
        } else {
            return getTextRight();
        }
    }
}

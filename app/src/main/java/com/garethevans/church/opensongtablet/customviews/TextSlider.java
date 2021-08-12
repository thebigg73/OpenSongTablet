package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.slider.Slider;

public class TextSlider extends LinearLayout {

    private final TextView textLeft;
    private final TextView textRight;
    private final Slider slider;

    public TextSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_text_slider, this);

        textLeft = findViewById(R.id.textLeft);
        textRight = findViewById(R.id.textRight);
        slider = findViewById(R.id.slider);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextSlider);
        CharSequence leftText = a.getText(R.styleable.TextSlider_textLeft);
        CharSequence rightText = a.getText(R.styleable.TextSlider_textRight);
        int position = a.getInt(R.styleable.TextSlider_value,0);

        textLeft.setText(leftText);
        textRight.setText(rightText);
        slider.setValue(position);

        a.recycle();
    }

    // The setters
    public void setTextLeft(String leftText) {
        textLeft.setText(leftText);
    }
    public void setTextRight(String rightText) {
        textRight.setText(rightText);
    }
    public void setSliderPos(int position) {
        slider.setValue(position);
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

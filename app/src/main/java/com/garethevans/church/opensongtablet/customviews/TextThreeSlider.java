package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.slider.Slider;

public class TextThreeSlider extends LinearLayout {

    private final TextView label;
    private final TextView textLeft;
    private final TextView textRight;
    private final TextView textCenter;
    private final Slider slider;

    public TextThreeSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_text_three_slider, this);

        label = findViewById(R.id.label);
        textLeft = findViewById(R.id.textLeft);
        textRight = findViewById(R.id.textRight);
        textCenter = findViewById(R.id.textCenter);
        slider = findViewById(R.id.slider);
        slider.setStepSize(1);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextThreeSlider);
        CharSequence leftText = a.getText(R.styleable.TextThreeSlider_textL);
        CharSequence rightText = a.getText(R.styleable.TextThreeSlider_textR);
        CharSequence centerText = a.getText(R.styleable.TextThreeSlider_textC);
        int position = a.getInt(R.styleable.TextThreeSlider_chosen,0);
        CharSequence text = a.getText(R.styleable.TextThreeSlider_label);

        int white = context.getResources().getColor(R.color.white);
        int grey  = context.getResources().getColor(R.color.lightgrey);

        label.setText(text);
        textLeft.setText(leftText);
        textRight.setText(rightText);
        textCenter.setText(centerText);

        textLeft.setOnClickListener(view -> slider.setValue(0));
        textCenter.setOnClickListener(view -> slider.setValue(1));
        textRight.setOnClickListener(view -> slider.setValue(2));
        slider.addOnChangeListener((slider, value, fromUser) -> {
            textLeft.setTextColor(grey);
            textCenter.setTextColor(grey);
            textRight.setTextColor(grey);
            if (value==0) {
                textLeft.setTextColor(white);
            } else if (value==1) {
                textCenter.setTextColor(white);
            } else {
                textRight.setTextColor(white);
            }
        });

        slider.setValue(position);
        a.recycle();
    }

    // The setters
    public void setText(String text) {
        label.setText(text);
    }
    public void setTextLeft(String leftText) {
        textLeft.setText(leftText);
    }
    public void setTextRight(String rightText) {
        textRight.setText(rightText);
    }
    public void setTextCenter(String centerText) {
        textCenter.setText(centerText);
    }
    public void setSliderPos(int position) {
        slider.setValue(position);
    }
    public void addOnSliderTouchListener(Slider.OnSliderTouchListener onSliderTouchListener) {
        slider.addOnSliderTouchListener(onSliderTouchListener);
    }
    public void addOnChangeListener(Slider.OnChangeListener onChangeListener) {
        slider.addOnChangeListener(onChangeListener);
    }
    // The getters
    public String getText() {
        if (label.getText()==null) {
            return "";
        } else {
            return label.getText().toString();
        }
    }
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
    public String getTextCenter(){
        if (textCenter.getText()==null) {
            return "";
        } else {
            return textCenter.getText().toString();
        }
    }
    public int getValue() {
        return (int)slider.getValue();
    }
    public String getValueText() {
        if (slider.getValue()==0) {
            return getTextLeft();
        } else if (slider.getValue()==1) {
            return getTextCenter();
        } else {
            return getTextRight();
        }
    }

}

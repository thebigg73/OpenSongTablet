package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.slider.Slider;

public class TextThreeSlider extends LinearLayout {

    private final MyMaterialTextView label;
    private final MyMaterialTextView textLeft;
    private final MyMaterialTextView textRight;
    private final MyMaterialTextView textCenter;
    private final ImageView imageLeft;
    private final ImageView imageRight;
    private final ImageView imageCenter;
    private final LinearLayout textLine;
    private final LinearLayout imageLine;
    private final Slider slider;
    private final float xxlarge, xlarge, large, medium, small, xsmall;

    public TextThreeSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_text_three_slider, this);

        xxlarge = context.getResources().getDimension(R.dimen.text_xxlarge);
        xlarge = context.getResources().getDimension(R.dimen.text_xlarge);
        large = context.getResources().getDimension(R.dimen.text_large);
        medium = context.getResources().getDimension(R.dimen.text_medium);
        small = context.getResources().getDimension(R.dimen.text_small);
        xsmall = context.getResources().getDimension(R.dimen.text_xsmall);

        textLine = findViewById(R.id.textLine);
        imageLine = findViewById(R.id.imageLine);
        label = findViewById(R.id.label);
        textLeft = findViewById(R.id.textLeft);
        textRight = findViewById(R.id.textRight);
        textCenter = findViewById(R.id.textCenter);
        imageLeft = findViewById(R.id.imageLeft);
        imageRight = findViewById(R.id.imageRight);
        imageCenter = findViewById(R.id.imageCenter);
        slider = findViewById(R.id.slider);
        slider.setStepSize(1);

        textLine.setId(View.generateViewId());
        imageLine.setId(View.generateViewId());
        label.setId(View.generateViewId());
        textLeft.setId(View.generateViewId());
        textRight.setId(View.generateViewId());
        textCenter.setId(View.generateViewId());
        imageLeft.setId(View.generateViewId());
        imageRight.setId(View.generateViewId());
        imageCenter.setId(View.generateViewId());
        slider.setId(View.generateViewId());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextThreeSlider);
        CharSequence leftText = a.getText(R.styleable.TextThreeSlider_textL);
        CharSequence rightText = a.getText(R.styleable.TextThreeSlider_textR);
        CharSequence centerText = a.getText(R.styleable.TextThreeSlider_textC);
        Drawable leftDrawable = a.getDrawable(R.styleable.TextThreeSlider_drawableL);
        Drawable rightDrawable = a.getDrawable(R.styleable.TextThreeSlider_drawableR);
        Drawable centerDrawable = a.getDrawable(R.styleable.TextThreeSlider_drawableC);

        int position = a.getInt(R.styleable.TextThreeSlider_chosen,0);
        CharSequence text = a.getText(R.styleable.TextThreeSlider_label);

        textOrNull(label,text);
        textOrNull(textLeft,leftText);
        textOrNull(textRight,rightText);
        textOrNull(textCenter,centerText);
        imageLeft.setImageDrawable(leftDrawable);
        imageRight.setImageDrawable(rightDrawable);
        imageCenter.setImageDrawable(centerDrawable);

        textLeft.setOnClickListener(view -> slider.setValue(0));
        textCenter.setOnClickListener(view -> slider.setValue(1));
        textRight.setOnClickListener(view -> slider.setValue(2));
        imageLeft.setOnClickListener(view -> slider.setValue(0));
        imageCenter.setOnClickListener(view -> slider.setValue(1));
        imageRight.setOnClickListener(view -> slider.setValue(2));

        slider.addOnChangeListener((slider, value, fromUser) -> {
            setTextSelected(textLeft,value==0);
            setTextSelected(textCenter,value==1);
            setTextSelected(textRight,value==2);

            setDrawable(imageLeft,value==0);
            setDrawable(imageCenter,value==1);
            setDrawable(imageRight,value==2);
        });

        setTextSelected(textLeft,position==0);
        setTextSelected(textCenter,position==1);
        setTextSelected(textRight,position==2);

        slider.setValue(position);
        a.recycle();

        iconOrText();
    }

    // The setters
    public void setText(String text) {
        textOrNull(label,text);
    }
    public void setTextLeft(String leftText) {
        textOrNull(textLeft,leftText);
    }
    public void setTextRight(String rightText) {
        textOrNull(textRight,rightText);
    }
    public void setTextCenter(String centerText) {
        textOrNull(textCenter,centerText);
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
    private void textOrNull(MyMaterialTextView textView, CharSequence charSequence) {
        String text = null;
        if (charSequence!=null) {
            text = charSequence.toString();
        }
        textView.setText(text);
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
    private void iconOrText() {
        if ((textLeft.getText()==null||textLeft.getText().toString().isEmpty()) &&
                (textRight.getText()==null||textRight.getText().toString().isEmpty()) &&
                (textCenter.getText()==null||textCenter.getText().toString().isEmpty())) {
            // Hide the text view and show the image view instead
            imageLine.setVisibility(View.VISIBLE);
            textLine.setVisibility(View.GONE);
        } else {
            // Hide the image view and show the text view
            imageLine.setVisibility(View.GONE);
            textLine.setVisibility(View.VISIBLE);
        }
    }
    public void setDrawable(ImageView imageView, boolean on) {
        if (on) {
            imageView.setAlpha(1f);
        } else {
            imageView.setAlpha(0.4f);
        }
    }
    private void setTextSelected(MyMaterialTextView textView, boolean on) {
        if (on) {
            textView.setAlpha(1f);
        } else {
            textView.setAlpha(0.4f);
        }
    }
}

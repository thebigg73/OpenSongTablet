package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.slider.Slider;
import com.google.android.material.textview.MaterialTextView;

public class TextThreeSlider extends LinearLayout {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "TextThreeSlider";
    private final MaterialTextView label;
    private final MaterialTextView textLeft;
    private final MaterialTextView textRight;
    private final MaterialTextView textCenter;
    private final ImageView imageLeft;
    private final ImageView imageRight;
    private final ImageView imageCenter;
    private final LinearLayout textLine;
    private final LinearLayout imageLine;
    private final Slider slider;
    private final float xxlarge, xlarge, large, medium, small, xsmall;
    private final int white = getResources().getColor(R.color.white);
    private final int lightgrey = getResources().getColor(R.color.lightgrey);

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

        // To cope with KitKat not liking vector assets
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            imageLeft.setImageDrawable(leftDrawable);
            imageRight.setImageDrawable(rightDrawable);
            imageCenter.setImageDrawable(centerDrawable);
        }

        textLeft.setOnClickListener(view -> setSliderPos(0));
        textCenter.setOnClickListener(view -> setSliderPos(1));
        textRight.setOnClickListener(view -> setSliderPos(2));
        imageLeft.setOnClickListener(view -> setSliderPos(0));
        imageCenter.setOnClickListener(view -> setSliderPos(1));
        imageRight.setOnClickListener(view -> setSliderPos(2));

        slider.addOnChangeListener((slider, value, fromUser) -> updateAlphas());
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
        updateAlphas();
    }
    public void addOnSliderTouchListener(Slider.OnSliderTouchListener onSliderTouchListener) {
        slider.addOnSliderTouchListener(onSliderTouchListener);
    }
    public void addOnChangeListener(Slider.OnChangeListener onChangeListener) {
        slider.addOnChangeListener(onChangeListener);
    }
    private void textOrNull(MaterialTextView textView, CharSequence charSequence) {
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

    private void updateAlphas() {
        Log.d(TAG,"updateAlphas() slider.getValue: "+slider.getValue());
        textLeft.setTextColor(slider.getValue()==0 ? white:lightgrey);
        textCenter.setTextColor(slider.getValue()==1 ? white:lightgrey);
        textRight.setTextColor(slider.getValue()==2 ? white:lightgrey);
        imageLeft.setAlpha(slider.getValue()==0 ? 1f:0.4f);
        imageCenter.setAlpha(slider.getValue()==1 ? 1f:0.4f);
        imageRight.setAlpha(slider.getValue()==2 ? 1f:0.4f);
    }
}

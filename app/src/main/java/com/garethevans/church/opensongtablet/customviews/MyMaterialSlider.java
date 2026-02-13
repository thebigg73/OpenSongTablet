package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

public class MyMaterialSlider extends LinearLayout {

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "MyMaterialSlider";
    private Slider slider;
    private MyMaterialSimpleTextView titleTextView, infoTextView;
    private MyMaterialSimpleTextView valueTextView, bottomHintView;
    private MyFloatingActionButton minusFAB, plusFAB;
    private FrameLayout minusHolder, plusHolder;
    private float stepSize;
    private boolean adjustButtons;
    private float xxlarge, xlarge, large, medium, small, xsmall;

    public MyMaterialSlider(Context context) {
        //super(context);
        this(context, null);
        //inflate(context, R.layout.view_material_slider, this);
        //getViews();
    }

    public MyMaterialSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, context.getTheme());
        LayoutInflater.from(themeWrapper).inflate(R.layout.view_material_slider, this, true);

        //inflate(context, R.layout.view_material_slider, this);
        getViews();
        getAttributes(context, attrs);
    }

    private void getViews() {
        slider = findViewById(R.id.slider);
        titleTextView = findViewById(R.id.titleText);
        infoTextView = findViewById(R.id.infoText);
        valueTextView = findViewById(R.id.valueText);
        bottomHintView = findViewById(R.id.bottomHint);
        minusHolder = findViewById(R.id.minusHolder);
        plusHolder = findViewById(R.id.plusHolder);
        minusFAB = findViewById(R.id.minus);
        plusFAB = findViewById(R.id.plus);

        slider.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        infoTextView.setId(View.generateViewId());
        valueTextView.setId(View.generateViewId());
        bottomHintView.setId(View.generateViewId());
        minusHolder.setId(View.generateViewId());
        plusHolder.setId(View.generateViewId());
        minusFAB.setId(View.generateViewId());
        plusFAB.setId(View.generateViewId());
    }

    private void getAttributes(Context context, AttributeSet attrs) {
        /*
       <attr name="trackHeight" format="dimension"/>
        <attr name="thumbRadius" format="dimension"/>
        <attr name="size" />
        <attr name="infoText" format="string"/>"
        <attr name="adjustable" format="boolean"/>
         */

        Palette palette = new Palette(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyMaterialSlider);
        String text = a.getString(R.styleable.MyMaterialSlider_android_text);
        String hint = a.getString(R.styleable.MyMaterialSlider_android_hint);
        float valueFrom = a.getFloat(R.styleable.MyMaterialSlider_android_valueFrom, 0.0f);
        float valueTo = a.getFloat(R.styleable.MyMaterialSlider_android_valueTo,10.0f);
        stepSize = a.getFloat(R.styleable.MyMaterialSlider_android_stepSize,1.0f);
        float value = a.getFloat(R.styleable.MyMaterialSlider_android_value,0.0f);
        int trackColor = a.getColor(R.styleable.MyMaterialSlider_trackColor,palette.secondary);
        int thumbColor = a.getColor(R.styleable.MyMaterialSlider_thumbColor,palette.secondaryVariant);
        int trackHeight = a.getDimensionPixelSize(R.styleable.MyMaterialSlider_trackHeight,0);
        int thumbRadius = a.getDimensionPixelSize(R.styleable.MyMaterialSlider_thumbRadius,0);
        String infoText = a.getString(R.styleable.MyMaterialSlider_infoText);
        adjustButtons = a.getBoolean(R.styleable.MyMaterialSlider_adjustable,false);

        a.recycle();

        xxlarge = context.getResources().getDimension(R.dimen.text_xxlarge);
        xlarge = context.getResources().getDimension(R.dimen.text_xlarge);
        large = context.getResources().getDimension(R.dimen.text_large);
        medium = context.getResources().getDimension(R.dimen.text_medium);
        small = context.getResources().getDimension(R.dimen.text_small);
        xsmall = context.getResources().getDimension(R.dimen.text_xsmall);

        setAdjustableButtons(adjustButtons);
        setSize("medium");

        if (text!=null) {
            setText(text);
        }
        setInfoText(infoText);

        if (hint==null) {
            hint = "";
        }
        setHint(hint);

        if (valueFrom>-1) {
            slider.setValueFrom(valueFrom);
        }
        if (valueTo>-1) {
            slider.setValueTo(valueTo);
        }
        if (stepSize>-1) {
            slider.setStepSize(stepSize);
        }
        if (value>-1) {
            setValue(value);
        }

        if (trackColor!=0) {
            slider.setTrackTintList(ColorStateList.valueOf(trackColor));
        }
        if (thumbColor!=0) {
            slider.setThumbStrokeColor(ColorStateList.valueOf(thumbColor));
            slider.setThumbTintList(ColorStateList.valueOf(thumbColor));
        }

        if (trackHeight!=0) {
            slider.setTrackHeight(trackHeight);
        }
        if (thumbRadius!=0) {
            slider.setThumbRadius(thumbRadius);
            slider.setThumbStrokeWidth(thumbRadius);
        }
        setAdjustableButtons(adjustButtons);

        setPalette(palette);
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
    public void setInfoText(String text) {
        infoTextView.setText(text);
        if (text == null || text.isEmpty()) {
            infoTextView.setVisibility(View.GONE);
        } else {
            infoTextView.setVisibility(View.VISIBLE);
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

    public void setSize(String size) {
        float textSize, hintSize;
        switch(size) {
            case "xxlarge":
                textSize = xxlarge;
                hintSize = xlarge;
                break;
            case "xlarge":
                textSize = xlarge;
                hintSize = large;
                break;
            case "large":
                textSize = large;
                hintSize = medium;
                break;
            case "small":
                textSize = small;
                hintSize = xsmall;
                break;
            case "xsmall":
                textSize = xsmall;
                hintSize = xsmall-1;
                break;
            case "medium":
            default:
                textSize = medium;
                hintSize = small;
                break;
        }
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
        infoTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,hintSize);
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

    public void setPalette(Palette palette) {
        titleTextView.setTextColor(palette.textColor);
        bottomHintView.setTextColor(palette.hintColor);
        infoTextView.setTextColor(palette.hintColor);
        slider.setTrackTintList(ColorStateList.valueOf(palette.secondary));
        slider.setThumbTintList(ColorStateList.valueOf(palette.secondaryVariant));
        slider.setTickTintList(ColorStateList.valueOf(Color.TRANSPARENT));
    }
}

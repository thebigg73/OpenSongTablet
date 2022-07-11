package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.button.MaterialButton;

public class ThemeItemColor extends LinearLayout {

    private final View colorSwatch;
    private final MaterialButton labelText;

    public ThemeItemColor(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_theme_item_color, this);

        colorSwatch = findViewById(R.id.colorSwatch);
        labelText = findViewById(R.id.labelText);
        FrameLayout colorLayout = findViewById(R.id.colorLayout);

        colorSwatch.setId(View.generateViewId());
        labelText.setId(View.generateViewId());
        colorLayout.setId(View.generateViewId());


        int[] set = new int[] {android.R.attr.text};
        TypedArray typedArray = context.obtainStyledAttributes(attrs,set);
        setText(typedArray.getString(0));
        typedArray.recycle();

        labelText.setClickable(false);
    }

    public void setText(String text) {
        labelText.setText(text);
    }

    public void setColor(int color) {
        colorSwatch.setBackgroundColor(color);
    }
}

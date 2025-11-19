package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.core.widget.CompoundButtonCompat;

import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.checkbox.MaterialCheckBox;

public class MyMaterialCheckbox extends MaterialCheckBox {

    private Palette palette;

    public MyMaterialCheckbox(Context context) {
        super(context);
        init(context, null);
    }

    public MyMaterialCheckbox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyMaterialCheckbox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        palette = new Palette(context);
        setColors();
    }

    private void setColors() {
        // 1. Define the ColorStateList using your palette colors
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked}, // Checked state
                        new int[]{-android.R.attr.state_checked} // Unchecked state
                },
                new int[]{
                        palette.textColor, // Checked color (Active/Filled)
                        palette.hintColor  // Unchecked color (Inactive/Outline)
                }
        );

        // 2. Disable Material theme override BEFORE setting the tint.
        // This is crucial to prevent the theme from overriding your custom colors.
        setUseMaterialThemeColors(false);

        // 3. Apply the tint using the compatibility layer for consistency.
        // The AppCompat version of setButtonTintList is preferred when available.
        // Assuming 'this' refers to your CheckBox instance:
        CompoundButtonCompat.setButtonTintList(this, colorStateList);

        // 4. Set the text color separately (this is fine)
        setTextColor(palette.textColor);
    }
}
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
        setUseMaterialThemeColors(false);
        setTextColor(palette.textColor);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked}, // checked
                        new int[]{-android.R.attr.state_checked}  // unchecked
                },
                new int[]{
                        palette.textColor, // Checked color
                        palette.hintColor  // Unchecked color
                }
        );

        CompoundButtonCompat.setButtonTintList(this, colorStateList);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setButtonTintList(colorStateList);
        } else {
            CompoundButtonCompat.setButtonTintList(this, colorStateList);
        }*/

        // Disable default Material theme tint override
        setUseMaterialThemeColors(false);
    }
}
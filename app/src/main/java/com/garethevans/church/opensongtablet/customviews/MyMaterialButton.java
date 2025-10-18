package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.button.MaterialButton;

public class MyMaterialButton extends MaterialButton {
    public MyMaterialButton(@NonNull Context context) {
        this(context,null);
    }

    public MyMaterialButton(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        Palette palette = new Palette(context);
        setPalette(palette);
    }

    public void setPalette(Palette palette) {
        setBackgroundTintList(ColorStateList.valueOf(palette.secondary));
        setTextColor(palette.textColor);
        setIconTint(ColorStateList.valueOf(palette.textColor));
    }
}

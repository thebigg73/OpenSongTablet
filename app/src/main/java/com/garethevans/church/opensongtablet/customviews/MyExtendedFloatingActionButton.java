package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class MyExtendedFloatingActionButton extends ExtendedFloatingActionButton {
    public MyExtendedFloatingActionButton(@NonNull Context context) {
        this(context, null);
    }

    public MyExtendedFloatingActionButton(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        setPalette(new Palette(context));
    }

    public void setPalette(Palette palette) {
        setBackgroundTintList(ColorStateList.valueOf(palette.secondary));
        setIconTint(ColorStateList.valueOf(palette.onPrimary));
        setTextColor(palette.textColor);
    }

}

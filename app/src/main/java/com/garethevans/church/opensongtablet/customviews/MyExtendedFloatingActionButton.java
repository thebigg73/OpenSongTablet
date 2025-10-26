package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class MyExtendedFloatingActionButton extends ExtendedFloatingActionButton {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyExtendedFAB";

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

    public void setScale(float scale) {
        Log.d(TAG,"trying to set scale to "+scale+", but ignoring");
    }
    public void setOpacity(float opacity) {
        Log.d(TAG,"trying to set opacity to "+opacity+", but ignoring");
    }
}

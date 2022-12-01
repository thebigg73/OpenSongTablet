package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

public class MultiSelectDropDown extends TextInputLayout {
    public MultiSelectDropDown(@NonNull Context context) {
        super(context);
    }

    public MultiSelectDropDown(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }
}

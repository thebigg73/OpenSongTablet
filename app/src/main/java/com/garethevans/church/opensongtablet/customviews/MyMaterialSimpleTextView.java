package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;

import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.textview.MaterialTextView;

@SuppressLint("PrivateResource")
public class MyMaterialSimpleTextView extends MaterialTextView {
    public MyMaterialSimpleTextView(@NonNull Context context) {
        this(context,null);
    }

    public MyMaterialSimpleTextView(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        Palette palette = new Palette(context);
        int originalColor = palette.textColor;
        if (attrs != null) {
            // Look for android:textColor in the XML explicitly
            String textColorValue = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "textColor");
            if (textColorValue != null) {
                // Attribute exists in XML, safe to read
                TypedArray a = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.textColor});
                palette.textColor = a.getColor(0, originalColor);
                a.recycle();
            }
            // else: leave palette.textColor unchanged
        }

        setPalette(palette);
    }

    public void setPalette(Palette palette) {
        setTextColor(palette.textColor);
        TextViewCompat.setCompoundDrawableTintList(this,ColorStateList.valueOf(palette.textColor));
    }
}

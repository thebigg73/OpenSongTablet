package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.TypedArrayUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;
import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.textview.MaterialTextView;

@SuppressLint("PrivateResource")
public class MyMaterialSimpleTextView extends MaterialTextView {

    private Palette palette;
    private int xmlDrawableResId = 0;

    public MyMaterialSimpleTextView(@NonNull Context context) {
        this(context, null);
    }

    public MyMaterialSimpleTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public MyMaterialSimpleTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        palette = new Palette(context);

        if (attrs != null) {
            String textColorValue = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "textColor");
            if (textColorValue != null) {
                TypedArray a = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.textColor});
                palette.textColor = a.getColor(0, palette.textColor);
                a.recycle();
            }
        }

        setPalette(palette);
    }

    public void setPalette(@NonNull Palette palette) {
        this.palette = palette;
        setTextColor(palette.textColor);

        if (xmlDrawableResId != 0) {
            android.graphics.drawable.Drawable drawable = ContextCompat.getDrawable(getContext(), xmlDrawableResId);
            if (drawable != null) {
                android.graphics.drawable.Drawable mutableDrawable = drawable.mutate();
                if (mutableDrawable.getBounds().isEmpty()) {
                    mutableDrawable.setBounds(0, 0, mutableDrawable.getIntrinsicWidth(), mutableDrawable.getIntrinsicHeight());
                }
                DrawableCompat.setTint(mutableDrawable, palette.textColor);

                android.graphics.drawable.Drawable[] currentDrawables = getCompoundDrawablesRelative();
                setCompoundDrawablesRelative(
                    mutableDrawable,
                    currentDrawables[1],
                    currentDrawables[2],
                    currentDrawables[3]
                );
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            TextViewCompat.setCompoundDrawableTintList(this, new ColorStateList(
                    new int[][]{new int[0]},
                new int[]{palette.textColor}
            ));
        }
    }
}
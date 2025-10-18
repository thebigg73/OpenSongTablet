package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.ShapeAppearanceModel;

public class MyFloatingActionButton extends FloatingActionButton {
    public MyFloatingActionButton(@NonNull Context context) {
        this(context,null);
    }

    public MyFloatingActionButton(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyFloatingActionButton(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUseCompatPadding(false);
        setScaleType(ScaleType.CENTER_INSIDE);
        int[] set = new int[]{
                androidx.appcompat.R.attr.backgroundTint,
                androidx.appcompat.R.attr.iconTint
        };
        TypedArray typedArray = context.obtainStyledAttributes(attrs,set);
        Palette palette = new Palette(context);

        if (typedArray.hasValue(0)) {
            int b = typedArray.getInt(0,-1);
            if (b!=-1) {
                palette.secondary = b;
            }
        }
        if (typedArray.hasValue(1)) {
            int i = typedArray.getInt(1,-1);
            if (i!=-1) {
                palette.textColor = i;
            }
        }
        typedArray.recycle();

        setPalette(palette);
    }

    public void setPalette(Palette palette) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setImageTintList(ColorStateList.valueOf(palette.textColor));
            setBackgroundTintList(ColorStateList.valueOf(palette.secondary));
        } else {
            setSupportImageTintList(ColorStateList.valueOf(palette.textColor));
            setSupportBackgroundTintList(ColorStateList.valueOf(palette.secondary));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.max(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size); // enforce square shape

        // Ensure shape still matches final square bounds
        setShapeAppearanceModel(
                getShapeAppearanceModel().toBuilder()
                        .setAllCornerSizes(8 * getContext().getResources().getDisplayMetrics().density)
                        .build()
        );
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ShapeAppearanceModel squareShape = getShapeAppearanceModel()
                .toBuilder()
                .setAllCornerSizes(8 * getContext().getResources().getDisplayMetrics().density)
                .build();
        setShapeAppearanceModel(squareShape);
        if (getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
            int margin = Math.round(8 * getContext().getResources().getDisplayMetrics().density);
            lp.setMargins(margin, margin, margin, margin);
            setLayoutParams(lp);
        }
    }
}

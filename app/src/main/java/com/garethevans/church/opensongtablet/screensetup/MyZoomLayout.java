package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.otaliastudios.zoom.ZoomLayout;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyZoomLayout extends ZoomLayout {
    public MyZoomLayout(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyZoomLayout(@NotNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyZoomLayout(@NotNull Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (RuntimeException e) {
                // ignore runtime error
                int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
                setMeasuredDimension(widthSize, heightSize);
            }
    }
}

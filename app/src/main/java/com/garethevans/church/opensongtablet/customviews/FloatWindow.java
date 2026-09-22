package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;

public class FloatWindow extends LinearLayout {

    public FloatWindow(Context context) {
        super(context);
    }

    public FloatWindow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatWindow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 🔑 Use AT_MOST for both width and height.
        // This forces the layout pass to fully calculate all children, margins,
        // and padding without collapsing the container's height box.
        int wrappedWidthSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST);
        int wrappedHeightSpec = MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.AT_MOST);

        super.onMeasure(wrappedWidthSpec, wrappedHeightSpec);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
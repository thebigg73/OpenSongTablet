package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.garethevans.church.opensongtablet.screensetup.Palette;

public class MyDivider extends View {

    public MyDivider(Context context) {
        this(context, null);
    }

    public MyDivider(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyDivider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode()) {
            // Preview color (Light Gray)
            setBackgroundColor(0xFFCCCCCC);
        } else {
            try {
                Palette palette = new Palette(context);
                setBackgroundColor(palette.textColor);
            } catch (Exception e) {
                // Fallback if Palette fails during initialization
                setBackgroundColor(0xFF888888);
            }
        }
        setAlpha(0.2f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Force a 1dp height if height is set to wrap_content
        int heightPixels = (int) (1 * getResources().getDisplayMetrics().density);

        // If height is exactly specified (e.g. 2dp), use that, otherwise use our 1dp
        int resolvedHeight = resolveSizeAndState(heightPixels, heightMeasureSpec, 0);

        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                resolvedHeight);
    }
}
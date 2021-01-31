package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class CoordLayout extends CoordinatorLayout {
    public CoordLayout(@NonNull Context context) {
        super(context);
    }

    public CoordLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

}

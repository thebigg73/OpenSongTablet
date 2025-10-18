package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TableRow;

import com.garethevans.church.opensongtablet.screensetup.Palette;

public class MyDivider extends TableRow {
    public MyDivider(Context context) {
        this(context, null);
    }

    public MyDivider(Context context, AttributeSet attrs) {
        super(context, attrs);
        Palette palette = new Palette(context);
        setBackgroundColor(palette.textColor);
        setAlpha(0.2f);
    }
}

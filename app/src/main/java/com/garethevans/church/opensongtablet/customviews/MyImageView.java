package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;

import com.garethevans.church.opensongtablet.screensetup.Palette;

public class MyImageView extends androidx.appcompat.widget.AppCompatImageView {
    public MyImageView(Context context) {
        this(context,null);
    }

    public MyImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setPalette(new Palette(context));
    }

    public void setPalette(Palette palette) {
        ImageViewCompat.setImageTintList(this,ColorStateList.valueOf(palette.textColor));
    }
}

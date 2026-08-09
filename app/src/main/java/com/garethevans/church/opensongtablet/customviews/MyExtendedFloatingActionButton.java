package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;

import com.garethevans.church.opensongtablet.screensetup.Palette;

public class MyExtendedFloatingActionButton extends TextView {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyExtendedFAB";
    private Palette palette;

    public MyExtendedFloatingActionButton(@NonNull Context context) {
        this(context, null);
    }

    public MyExtendedFloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        palette = new Palette(context);
        setPalette(palette);
    }

    public void setPalette(Palette palette) {
        this.palette = palette;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setBackgroundTintList(new ColorStateList(
                    new int[][]{new int[0]},
                    new int[]{palette.secondary}
            ));
        }
        setIconTint(new ColorStateList(
                new int[][]{new int[0]},
                new int[]{palette.onPrimary}
        ));
        setTextColor(palette.textColor);
    }

    public void setIcon(@Nullable Drawable drawable) {
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable).mutate();
            if (drawable.getBounds().isEmpty()) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            }
            if (palette != null) {
                DrawableCompat.setTint(drawable, palette.onPrimary);
            }
            Drawable[] currentDrawables = getCompoundDrawablesRelative();
            setCompoundDrawablesRelative(drawable, currentDrawables[1], currentDrawables[2], currentDrawables[3]);
        } else {
            setCompoundDrawablesRelative(null, null, null, null);
        }
    }

    public void setIcon(int drawableResId) {
        if (drawableResId != 0) {
            Drawable drawable = ContextCompat.getDrawable(getContext(), drawableResId);
            setIcon(drawable);
        } else {
            setIcon((Drawable) null);
        }
    }

    @Nullable
    public Drawable getIcon() {
        Drawable[] currentDrawables = getCompoundDrawablesRelative();
        return currentDrawables[0];
    }

    public void setIconTint(@Nullable ColorStateList tint) {
        if (tint != null) {
            Drawable[] drawables = getCompoundDrawablesRelative();
            for (Drawable drawable : drawables) {
                if (drawable != null) {
                    Drawable wrapped = DrawableCompat.wrap(drawable).mutate();
                    DrawableCompat.setTintList(wrapped, tint);
                }
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                TextViewCompat.setCompoundDrawableTintList(this, tint);
            }
        }
    }

    public void hide() {
        animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> setVisibility(View.GONE))
                .start();
    }

    public void show() {
        setVisibility(View.VISIBLE);
        setScaleX(0f);
        setScaleY(0f);
        setAlpha(0f);
        animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(200)
                .start();
    }

    public void setScale(float scale) {
        Log.d(TAG, "trying to set scale to " + scale + ", but ignoring");
    }

    public void setOpacity(float opacity) {
        Log.d(TAG, "trying to set opacity to " + opacity + ", but ignoring");
    }
}

/*
package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class MyExtendedFloatingActionButton extends ExtendedFloatingActionButton {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyExtendedFAB";

    public MyExtendedFloatingActionButton(@NonNull Context context) {
        this(context, null);
    }

    public MyExtendedFloatingActionButton(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        setPalette(new Palette(context));
    }

    public void setPalette(Palette palette) {
        //setBackgroundTintList(ColorStateList.valueOf(palette.secondary));
        setBackgroundTintList(new ColorStateList(
                new int[][]{new int[0]},
                new int[]{palette.secondary}
        ));
        //setIconTint(ColorStateList.valueOf(palette.onPrimary));
        setIconTint(new ColorStateList(
                new int[][]{new int[0]},
                new int[]{palette.onPrimary}
        ));
        setTextColor(palette.textColor);
    }

    public void setScale(float scale) {
        Log.d(TAG,"trying to set scale to "+scale+", but ignoring");
    }
    public void setOpacity(float opacity) {
        Log.d(TAG,"trying to set opacity to "+opacity+", but ignoring");
    }
}
*/

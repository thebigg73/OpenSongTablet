package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.screensetup.Palette;

public class MyFloatingActionButton extends FrameLayout {

    private final AppCompatImageButton myFAB;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyFloatingActionButton";
    private Palette palette;

    // Store configuration
    private boolean isFlat;
    private final float targetAlpha;
    private final int targetButtonColor;
    private final int targetIconColor;

    public MyFloatingActionButton(@NonNull Context context) {
        this(context, null);
    }

    public MyFloatingActionButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_myfab, this);

        myFAB = findViewById(R.id.myFAB);
        setClipChildren(false);
        setClipToPadding(false);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyFloatingActionButton);
        try {
            // 1. Booleans & Alphas with safe defaults
            isFlat = a.getBoolean(R.styleable.MyFloatingActionButton_makeFlat, false);
            targetAlpha = a.getFloat(R.styleable.MyFloatingActionButton_android_alpha, 1.0f);

            // 2. Colors (-1 as a default indicator for unassigned colors)
            targetIconColor = a.getColor(R.styleable.MyFloatingActionButton_fabIconColor, -1);
            targetButtonColor = a.getColor(R.styleable.MyFloatingActionButton_fabButtonColor, -1);

            // 3. Icon resolution: Check app:fabIcon first, then fallback to android:src, then default
            int fabIcon = a.getResourceId(R.styleable.MyFloatingActionButton_fabIcon, 0);
            if (fabIcon == 0) {
                fabIcon = a.getResourceId(R.styleable.MyFloatingActionButton_android_src, 0);
            }
            int finalIcon = (fabIcon != 0) ? fabIcon : R.drawable.help_outline;
            myFAB.setImageResource(finalIcon);

            // 4. Size mapping (0 = normal ~56dp, 1 = mini ~40dp)
            int size = a.getInt(R.styleable.MyFloatingActionButton_myFabSize, 0);
            setSize(size);

            // 5. Native padding (Safe standard layout attribute)
            int padding = a.getDimensionPixelSize(R.styleable.MyFloatingActionButton_android_padding, 0);
            if (padding > 0) {
                myFAB.setPadding(padding, padding, padding, padding);
            }

            // 6. Touch padding flag
            boolean addPadding = a.getBoolean(R.styleable.MyFloatingActionButton_addTouchPadding, false);
            if (addPadding) {
                int extraTouchSpace = padding + ((int) (16 * getContext().getResources().getDisplayMetrics().density));
                setPadding(extraTouchSpace, extraTouchSpace, extraTouchSpace, extraTouchSpace);
            }

        } finally {
            a.recycle();
        }

        setClickable(false);
        setFocusable(false);

        // Set default colors
        palette = new Palette(getContext());
        if (targetButtonColor != -1) {
            palette.secondary = targetButtonColor;
        }
        if (targetIconColor != -1) {
            palette.onSurface = targetIconColor;
        }
        setPalette(palette);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        applyState();
    }

    private void applyState() {
        myFAB.setAlpha(targetAlpha);

        if (isFlat) {
            makeFlat();
        }

        if (targetButtonColor != -1) {
            setFABButtonColor(targetButtonColor);
        }
        if (targetIconColor != -1) {
            setFABIconColor(targetIconColor);
        }
    }

    public void hide() {
        if (myFAB!=null) {
            myFAB.post(() ->
            myFAB.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        myFAB.setVisibility(View.GONE);
                        setVisibility(View.GONE);
                    })
                    .start());
        }
    }

    public void show() {
        setVisibility(View.VISIBLE);
        if (myFAB!=null) {
            myFAB.post(() -> {
                myFAB.setVisibility(View.VISIBLE);
                myFAB.setScaleX(0f);
                myFAB.setScaleY(0f);
                myFAB.setAlpha(0f);
                myFAB.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(targetAlpha)
                        .setDuration(200)
                        .start();
            });
        }
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        myFAB.setOnClickListener(onClickListener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        myFAB.setOnLongClickListener(onLongClickListener);
    }

    public void setBackgroundTintList(ColorStateList tintList) {
        ViewCompat.setBackgroundTintList(myFAB, tintList);
    }

    public void setSupportBackgroundTintList(ColorStateList tintList) {
        ViewCompat.setBackgroundTintList(myFAB, tintList);
    }

    public void setSize(int size) {
        float density = getContext().getResources().getDisplayMetrics().density;
        // size 1 = mini (40dp), size 0 = normal (56dp)
        int px = (size == 1) ? (int) (40 * density) : (int) (56 * density);
        ViewGroup.LayoutParams params = myFAB.getLayoutParams();
        if (params != null) {
            params.width = px;
            params.height = px;
            myFAB.setLayoutParams(params);
        }
    }

    public void setImageDrawable(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myFAB.setImageTintList(null);
        }
        myFAB.setImageDrawable(drawable);
        if (palette != null) {
            setFABIconColor(palette.textColor);
        }
    }

    public void setImageDrawable(int drawableInt) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), drawableInt);
        if (drawable != null) {
            myFAB.setImageDrawable(drawable);
            if (palette != null) {
                setFABIconColor(palette.textColor);
            }
        }
    }

    public void setImageResource(int drawableInt) {
        myFAB.setImageResource(drawableInt);
        if (palette != null) {
            setFABIconColor(palette.textColor);
        }
    }

    public Drawable getDrawable() {
        return myFAB.getDrawable();
    }

    public void makeFlat() {
        isFlat = true;
        ViewCompat.setBackgroundTintList(myFAB, null);
        myFAB.setBackgroundColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myFAB.setElevation(0);
        }
        ViewCompat.setElevation(myFAB, 0f);
    }

    public void setFABVisibility(int visibility) {
        myFAB.setVisibility(visibility);
        setVisibility(visibility);
    }

    public void setPalette(Palette palette) {
        this.palette = palette;
        setFABButtonColor(palette.secondary);
        setFABIconColor(palette.onSurface);
    }

    public void setFABButtonColor(int color) {
        ViewCompat.setBackgroundTintList(myFAB, ColorStateList.valueOf(color));
    }

    public void setFABIconColor(int color) {
        if (myFAB.getDrawable() != null) {
            Drawable drawable = DrawableCompat.wrap(myFAB.getDrawable()).mutate();
            DrawableCompat.setTint(drawable, color);
            myFAB.setImageDrawable(drawable);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myFAB.setImageTintList(ColorStateList.valueOf(color));
        }
    }

    public void setImageTintList(ColorStateList colorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myFAB.setImageTintList(colorStateList);
        }
    }
}
/*
package com.garethevans.church.opensongtablet.customviews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.MaterialShapeDrawable;

public class MyFloatingActionButton extends FrameLayout {

    private final FloatingActionButton myFAB;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyFloatingActionButton";
    private Palette palette;

    // Store configuration
    private boolean isFlat;
    private final float targetAlpha;
    private final int targetButtonColor;
    private final int targetIconColor;

    public MyFloatingActionButton(@NonNull Context context) {
        this(context,null);
    }

    public MyFloatingActionButton(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_myfab, this);

        myFAB = findViewById(R.id.myFAB);
        setClipChildren(false);
        setClipToPadding(false);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyFloatingActionButton);
        try {
            // 1. Booleans & Alphas with safe defaults
            isFlat = a.getBoolean(R.styleable.MyFloatingActionButton_makeFlat, false);
            targetAlpha = a.getFloat(R.styleable.MyFloatingActionButton_android_alpha, 1.0f);

            // 2. Colors (-1 as a default indicator for unassigned colors)
            targetIconColor = a.getColor(R.styleable.MyFloatingActionButton_fabIconColor, -1);
            targetButtonColor = a.getColor(R.styleable.MyFloatingActionButton_fabButtonColor, -1);

            // 3. Icon resolution: Check app:fabIcon first, then fallback to android:src, then default
            int fabIcon = a.getResourceId(R.styleable.MyFloatingActionButton_fabIcon, 0);
            if (fabIcon == 0) {
                fabIcon = a.getResourceId(R.styleable.MyFloatingActionButton_android_src, 0);
            }
            int finalIcon = (fabIcon != 0) ? fabIcon : R.drawable.help_outline;
            myFAB.setImageResource(finalIcon);

            // 4. Size enum (Default to normal size = 0)
            int size = a.getInt(R.styleable.MyFloatingActionButton_myFabSize, FloatingActionButton.SIZE_NORMAL);
            myFAB.setSize(size);

            // 5. Native padding (Safe standard layout attribute)
            int padding = a.getDimensionPixelSize(R.styleable.MyFloatingActionButton_android_padding, 0);
            if (padding > 0) {
                myFAB.setPadding(padding, padding, padding, padding);
            }

            // 6. Touch padding flag (Read safely as a boolean without mutating layout bounds during inflation)
            boolean addPadding = a.getBoolean(R.styleable.MyFloatingActionButton_addTouchPadding, false);
            if (addPadding) {
                int extraTouchSpace = padding + ((int) (16 * getContext().getResources().getDisplayMetrics().density));
                setPadding(extraTouchSpace, extraTouchSpace, extraTouchSpace, extraTouchSpace);
            }

        } finally {
            a.recycle(); // Cleanly closed once and only once
        }

        // The framelayout should not consume the clicks as they pass to the child by default
        setClickable(false);
        setFocusable(false);

        setAnimationListeners();

        // Set default colors
        palette = new Palette(getContext());
        if (targetButtonColor!=-1) {
            palette.secondary = targetButtonColor;
        }
        if (targetIconColor!=-1) {
            palette.onSurface = targetIconColor;
        }
        setPalette(palette);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        applyState();
    }

    private void applyState() {
        // 1. Apply Alpha
        myFAB.setAlpha(targetAlpha);

        // 2. Apply Flat logic
        if (isFlat) {
            makeFlat();
        }

        // 3. Apply Color
        if (targetButtonColor != -1) {
            setFABButtonColor(targetButtonColor);
        }
        if (targetIconColor != -1) {
            setFABIconColor(targetIconColor);
        }
    }


    private void setAnimationListeners() {
        myFAB.addOnShowAnimationListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setVisibility(View.VISIBLE);
                super.onAnimationEnd(animation);
            }
        });
        myFAB.addOnHideAnimationListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.GONE);
                super.onAnimationEnd(animation);
            }
        });
    }

    final FloatingActionButton.OnVisibilityChangedListener addVisibilityChanged = new FloatingActionButton.OnVisibilityChangedListener() {
        public void onShown(final MyFloatingActionButton fab) {
            super.onShown(myFAB);
            setVisibility(View.VISIBLE);
        }
        public void onHidden(final FloatingActionButton fab) {
            super.onHidden(fab);
            setVisibility(View.GONE);
        }
    };

    public void hide() {
        // Hide the FAB and then set the frame layout to gone after the animation time
        myFAB.hide(addVisibilityChanged);
    }

    public void show() {
        // Show the FAB and also make the frame layout visible
        setVisibility(View.VISIBLE);
        myFAB.show(addVisibilityChanged);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        myFAB.setOnClickListener(onClickListener);
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        myFAB.setOnLongClickListener(onLongClickListener);
    }

    public void setBackgroundTintList(ColorStateList tintList) {
        myFAB.setBackgroundTintList(tintList);
    }
    public void setSupportBackgroundTintList(ColorStateList tintList) {
        myFAB.setSupportBackgroundTintList(tintList);
    }

    public void setSize(int size) {
        myFAB.setSize(size);
    }

    public void setImageDrawable(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myFAB.setImageTintList(null);
        }
        myFAB.setSupportImageTintList(null);
        myFAB.setImageDrawable(drawable);
        setFABIconColor(palette.textColor);
    }

    public void setImageDrawable(int drawableInt) {
        Drawable drawable = VectorDrawableCompat.create(getResources(),drawableInt, getContext().getTheme());
        if (drawable!=null) {
            DrawableCompat.setTint(drawable, palette.onPrimary);
            myFAB.setImageDrawable(drawable);
            setFABIconColor(palette.textColor);
        }
    }
    public void setImageResource(int drawableInt) {
        myFAB.setImageResource(drawableInt);
        setFABIconColor(palette.textColor);
    }

    public Drawable getDrawable() {
        return myFAB.getDrawable();
    }

    public void makeFlat() {
        isFlat = true;
        // Force the background to be a solid transparent color
        myFAB.setBackgroundTintList(null);
        myFAB.setAnimation(null);

        // Remove the shadow-casting background
        if (myFAB.getBackground() instanceof MaterialShapeDrawable) {
            MaterialShapeDrawable shapeDrawable = (MaterialShapeDrawable) myFAB.getBackground();
            shapeDrawable.setTint(Color.TRANSPARENT);
            shapeDrawable.setStroke(0, Color.TRANSPARENT);
            shapeDrawable.setShadowCompatibilityMode(MaterialShapeDrawable.SHADOW_COMPAT_MODE_NEVER);
        }

        // Final fallback for the icon tinting
        //myFAB.setSupportBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        myFAB.setSupportBackgroundTintList(new ColorStateList(
                new int[][]{new int[0]},
                new int[]{Color.TRANSPARENT}
        ));
        //myFAB.setRippleColor(ColorStateList.valueOf(Color.TRANSPARENT));
        myFAB.setRippleColor(new ColorStateList(
                new int[][]{new int[0]},
                new int[]{Color.TRANSPARENT}
        ));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myFAB.setElevation(0);
        }
        myFAB.setCompatElevation(0f);     // removes shadow across states
        ViewCompat.setElevation(myFAB, 0f);
    }

    public void setFABVisibility(int visibility) {
        myFAB.setVisibility(visibility);
        setVisibility(visibility);
    }

    public void setPalette(Palette palette) {
        this.palette = palette;
        // Tint the button
        setFABButtonColor(palette.secondary);
        // Tint the icon
        setFABIconColor(palette.onSurface);
    }

    public void setFABButtonColor(int color) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //myFAB.setBackgroundTintList(ColorStateList.valueOf(color));
                myFAB.setBackgroundTintList(new ColorStateList(
                        new int[][]{new int[0]},
                        new int[]{color}
                ));
                //myFAB.setRippleColor(ColorStateList.valueOf(color));
                myFAB.setRippleColor(new ColorStateList(
                        new int[][]{new int[0]},
                        new int[]{color}
                ));
            } else {
                //myFAB.setSupportBackgroundTintList(ColorStateList.valueOf(color));
                myFAB.setSupportBackgroundTintList(new ColorStateList(
                        new int[][]{new int[0]},
                        new int[]{color}
                ));
                myFAB.setRippleColor(color);
            }
            //myFAB.setSupportBackgroundTintList(ColorStateList.valueOf(color));
            myFAB.setSupportBackgroundTintList(new ColorStateList(
                    new int[][]{new int[0]},
                    new int[]{color}
            ));
    }

    public void setFABIconColor(int color) {
        // Tint the icon
        Drawable drawable = DrawableCompat.wrap(myFAB.getDrawable()).mutate();

        DrawableCompat.setTint(drawable, color);
        myFAB.setImageDrawable(drawable);
        //myFAB.setSupportImageTintList(ColorStateList.valueOf(color));
        myFAB.setSupportImageTintList(new ColorStateList(
                new int[][]{new int[0]},
                new int[]{color}
        ));
    }

    public void setImageTintList(ColorStateList colorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myFAB.setImageTintList(colorStateList);
        }
        myFAB.setSupportImageTintList(colorStateList);
    }

}
*/

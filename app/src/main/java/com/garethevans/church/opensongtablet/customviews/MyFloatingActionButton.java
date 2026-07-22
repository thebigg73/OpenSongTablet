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
    private boolean isFlat = false;
    private float targetAlpha = 1.0f;
    private int targetButtonColor = -1;
    private int targetIconColor = -1;

    public MyFloatingActionButton(@NonNull Context context) {
        this(context,null);
    }

    public MyFloatingActionButton(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_myfab, this);

        myFAB = findViewById(R.id.myFAB);
        setClipChildren(false);
        setClipToPadding(false);

        // Read configuration.  Store some to set after attached to window (to override default style)
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyFloatingActionButton);
        isFlat = a.getBoolean(R.styleable.MyFloatingActionButton_makeFlat, false);
        targetAlpha = a.getFloat(R.styleable.MyFloatingActionButton_android_alpha, 1.0f);
        targetIconColor = a.getColor(R.styleable.MyFloatingActionButton_fabIconColor, -1);
        targetButtonColor = a.getColor(R.styleable.MyFloatingActionButton_fabButtonColor, -1);

        // These can be dealt with directly
        int srcResId = a.getResourceId(R.styleable.MyFloatingActionButton_android_src, R.drawable.help_outline);
        int fabIcon = a.getResourceId(R.styleable.MyFloatingActionButton_fabIcon, R.drawable.help_outline);
        if (fabIcon==R.drawable.help_outline) {
            fabIcon = srcResId;
        }
        myFAB.setImageResource(fabIcon);

        boolean addPadding = a.getBoolean(R.styleable.MyFloatingActionButton_addTouchPadding, false);
        if (addPadding) {
            int padding = (int) (16 * getContext().getResources().getDisplayMetrics().density);
            setPadding(padding, padding, padding, padding);
        }

        // 3. Get the size enum
        int size = a.getInt(R.styleable.MyFloatingActionButton_myFabSize, FloatingActionButton.SIZE_NORMAL);
        myFAB.setSize(size);

        int padding = a.getDimensionPixelSize(R.styleable.MyFloatingActionButton_android_padding, 0);
        if (padding>0) {
            padding = (int)(getContext().getResources().getDisplayMetrics().density * padding);
            myFAB.setPadding(padding, padding, padding, padding);
        }

        a.recycle();

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
    }

    public void setImageDrawable(int drawableInt) {
        Drawable drawable = VectorDrawableCompat.create(getResources(),drawableInt, getContext().getTheme());
        if (drawable!=null) {
            DrawableCompat.setTint(drawable, palette.onPrimary);
            myFAB.setImageDrawable(drawable);
        }
    }
    public void setImageResource(int drawableInt) {
        myFAB.setImageResource(drawableInt);
    }

    public Drawable getDrawable() {
        return myFAB.getDrawable();
    }

    public void makeFlat() {
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
        myFAB.setSupportBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        myFAB.setRippleColor(ColorStateList.valueOf(Color.TRANSPARENT));

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
                myFAB.setBackgroundTintList(ColorStateList.valueOf(color));
                myFAB.setRippleColor(ColorStateList.valueOf(color));
            } else {
                myFAB.setSupportBackgroundTintList(ColorStateList.valueOf(color));
                myFAB.setRippleColor(color);
            }
            myFAB.setSupportBackgroundTintList(ColorStateList.valueOf(color));

    }

    public void setFABIconColor(int color) {
        // Tint the icon
        Drawable drawable = DrawableCompat.wrap(myFAB.getDrawable()).mutate();

        DrawableCompat.setTint(drawable, color);
        myFAB.setImageDrawable(drawable);
        myFAB.setSupportImageTintList(ColorStateList.valueOf(color));

    }

    public void setImageTintList(ColorStateList colorStateList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myFAB.setImageTintList(colorStateList);
        }
        myFAB.setSupportImageTintList(colorStateList);
    }

}

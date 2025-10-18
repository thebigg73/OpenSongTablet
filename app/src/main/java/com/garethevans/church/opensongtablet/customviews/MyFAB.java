package com.garethevans.church.opensongtablet.customviews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.screensetup.Palette;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MyFAB extends FrameLayout {

    private final FloatingActionButton myFAB;
    private final RelativeLayout myFABHolder;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyFAB";
    private Palette palette;

    public MyFAB(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_myfab, this);
        myFAB = findViewById(R.id.myFAB);
        myFABHolder = findViewById(R.id.myFABHolder);

        myFAB.setId(View.generateViewId());
        myFABHolder.setId(View.generateViewId());

        int[] set = new int[] {androidx.appcompat.R.attr.srcCompat, com.google.android.material.R.attr.fabSize};

        TypedArray typedArray = context.obtainStyledAttributes(attrs,set);
        // Get drawable image
        Drawable drawable = VectorDrawableCompat.create(getResources(),typedArray.getResourceId(0,R.drawable.help_outline),getContext().getTheme());
        int size = typedArray.getInt(1,FloatingActionButton.SIZE_NORMAL);

        myFAB.setImageDrawable(drawable);
        myFAB.setSize(size);

        typedArray.recycle();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyFAB);
        int padding = (int)a.getDimension(0,0);
        myFABHolder.setPadding(padding,padding,padding,padding);
        a.recycle();

        myFABHolder.setOnClickListener(v -> {
            if (myFAB.getVisibility()==View.VISIBLE) {
                myFAB.performClick();
                myFAB.setPressed(true);
                myFAB.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myFAB.setPressed(false);
                    }
                }, 300);
            }
        });

        setAnimationListeners();

        setPalette(new Palette(context));
    }

    private void setAnimationListeners() {
        myFAB.addOnShowAnimationListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                myFABHolder.setVisibility(View.VISIBLE);
                super.onAnimationEnd(animation);
            }
        });
        myFAB.addOnHideAnimationListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                myFABHolder.setVisibility(View.GONE);
                super.onAnimationEnd(animation);
            }
        });
    }

    final FloatingActionButton.OnVisibilityChangedListener addVisibilityChanged = new FloatingActionButton.OnVisibilityChangedListener() {
        public void onShown(final MyFloatingActionButton fab) {
            super.onShown(fab);
            myFABHolder.setVisibility(View.VISIBLE);
        }
        public void onHidden(final FloatingActionButton fab) {
            super.onHidden(fab);
            myFABHolder.setVisibility(View.GONE);
        }
    };

    public void hide() {
        // Hide the FAB and then set the frame layout to gone after the animation time
        myFAB.hide(addVisibilityChanged);
    }

    public void show() {
        // Show the FAB and also make the frame layout visible
        myFABHolder.setVisibility(View.VISIBLE);
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

    public void setSize(int size) {
        myFAB.setSize(size);
    }

    public void setImageDrawable(Drawable drawable) {
        myFAB.setImageDrawable(drawable);
    }

    public void setImageDrawable(int drawableInt) {
        Drawable drawable = VectorDrawableCompat.create(getResources(),drawableInt, getContext().getTheme());
        if (drawable!=null) {
            DrawableCompat.setTint(drawable, palette.onPrimary);
            myFAB.setImageDrawable(drawable);
        }
    }

    public Drawable getDrawable() {
        return myFAB.getDrawable();
    }

    public void setVisibility(int visibility) {
        myFAB.setVisibility(visibility);
        myFABHolder.setVisibility(visibility);
    }

    public void setPalette(Palette palette) {
        myFAB.setBackgroundTintList(ColorStateList.valueOf(palette.secondary));

        // Tint the icon
        Drawable drawable = DrawableCompat.wrap(myFAB.getDrawable()).mutate();
        DrawableCompat.setTint(drawable, palette.onPrimary);
        myFAB.setImageDrawable(drawable);
    }
}

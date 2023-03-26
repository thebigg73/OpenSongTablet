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
import androidx.core.content.ContextCompat;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MyFAB extends FrameLayout {

    private final FloatingActionButton myFAB;
    private final RelativeLayout myFABHolder;
    private final String TAG = "MyFAB";

    public MyFAB(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_myfab, this);
        myFAB = findViewById(R.id.myFAB);
        myFABHolder = findViewById(R.id.myFABHolder);

        myFAB.setId(View.generateViewId());
        myFABHolder.setId(View.generateViewId());

        int[] set = new int[] {android.R.attr.src};

        TypedArray typedArray = context.obtainStyledAttributes(attrs,set);
        // Get drawable image
        Drawable drawable = typedArray.getDrawable(0);
        if (drawable!=null) {
            myFAB.setImageDrawable(drawable);
        }
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

    public void hide() {
        // Hide the FAB and then set the frame layout to gone after the animation time
        myFAB.hide();
    }

    public void show() {
        // Show the FAB and also make the frame layout visible
        myFAB.show();
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
        Drawable drawable = ContextCompat.getDrawable(getContext(),drawableInt);
        myFAB.setImageDrawable(drawable);
    }

    public Drawable getDrawable() {
        return myFAB.getDrawable();
    }

    public void setVisibility(int visibility) {
        myFAB.setVisibility(visibility);
        myFABHolder.setVisibility(visibility);
    }
}

package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;

public class BottomSheetHandle extends FrameLayout {

    private final ImageView handle;
    private boolean isDraggableX = false;
    private float startX;
    private long startTime;
    float handleWidth, handleX;
    boolean clickedOnHandle;

    public BottomSheetHandle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ContextThemeWrapper contextThemeWrapper = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            contextThemeWrapper = new ContextThemeWrapper(context, context.getTheme());
        }
        LayoutInflater.from(contextThemeWrapper).inflate(R.layout.view_bottom_sheet_handle, this, true);
        handle = findViewById(R.id.handle);
        handle.setId(View.generateViewId());
        handle.setFocusable(true);
        handle.setFocusableInTouchMode(true);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetHandle);
        try {
            isDraggableX = a.getBoolean(R.styleable.BottomSheetHandle_isDraggableX, false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a.recycle();
        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        handle.setOnClickListener(onClickListener);
    }

    @Override
    public boolean performClick() {
        handle.performClick();
        return super.performClick();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        handleX = handle.getX();
        if (handleWidth <= 0) {
            handleWidth = handle.getWidth();
        }
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = motionEvent.getRawX();
                if (!clickOnHandle()) {
                    return false;
                }
                startTime = System.currentTimeMillis();
                handleX = handle.getX();
                break;

            case MotionEvent.ACTION_MOVE:
                if (clickedOnHandle && isDraggableX) {
                    float newX = motionEvent.getRawX();
                    if ((newX - startX) > 20 || (newX - startX) < -20) {
                        handle.setX(motionEvent.getRawX() - handle.getWidth() / 2f);
                        handleX = handle.getX();
                    }
                } else {
                    return false;
                }
                break;

            case MotionEvent.ACTION_UP:
                handleX = handle.getX();
                if (clickedOnHandle && System.currentTimeMillis() - startTime < 500) {
                    clickedOnHandle = false;
                    performClick();
                }
                break;
        }
        return true;
    }

    private boolean clickOnHandle() {
        clickedOnHandle = startX >= handleX && startX < (handleX + handleWidth);
        return clickedOnHandle;
    }
}


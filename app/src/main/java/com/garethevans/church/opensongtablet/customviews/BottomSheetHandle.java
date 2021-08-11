package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;

public class BottomSheetHandle extends FrameLayout {

    private final ImageView handle;
    private boolean isDraggableX;
    private float startX;
    private long startTime;
    float handleWidth, handleX;
    boolean clickedOnHandle;

    public BottomSheetHandle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_bottom_sheet_handle, this);
        handle = findViewById(R.id.handle);
        handle.setFocusable(true);
        handle.setFocusableInTouchMode(true);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetHandle);
        isDraggableX = a.getBoolean(R.styleable.BottomSheetHandle_isDraggableX, false);
        a.recycle();
    }

    public void setDraggableX(boolean isDraggableX) {
        this.isDraggableX = isDraggableX;
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


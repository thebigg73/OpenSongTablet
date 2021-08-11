package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;


public class BottomSheetHandle extends FrameLayout {

    private final ImageView handle;
    private final String TAG = "BottomSheetHandle";
    private boolean isDraggableX;

    public BottomSheetHandle(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_bottom_sheet_handle, this);
        handle = findViewById(R.id.handle);
        handle.setFocusable(true);
        handle.setFocusableInTouchMode(true);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetHandle);
        isDraggableX = a.getBoolean(R.styleable.BottomSheetHandle_isDraggableX, false);
        Log.d(TAG, "isDraggableX=" + isDraggableX);
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

    private float startX;
    private long startTime;
    float handleWidth, handleX;
    boolean clickedOnHandle;

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        Log.d(TAG, "onTouch  isDraggableX=" + isDraggableX);

        if (handleWidth <= 0) {
            handleWidth = handle.getWidth();
        }

        handleX = handle.getX();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = motionEvent.getRawX();
                    clickedOnHandle = clickOnHandle();
                    startTime = System.currentTimeMillis();
                    handleX = handle.getX();
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (clickedOnHandle) {
                        float newX = motionEvent.getRawX();
                        Log.d(TAG, "move  newX=" + newX + "  startX=" + startX);

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
        Log.d(TAG,"startX:"+startX+" handleX:"+handleX+" handleWidth:"+handleWidth);
        //return startX >= handleX && startX < (handleX + handleWidth);
        return true;
    }
}


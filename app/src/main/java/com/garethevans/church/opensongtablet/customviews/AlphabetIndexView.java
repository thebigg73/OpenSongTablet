package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.screensetup.Palette;

import java.util.ArrayList;
import java.util.List;

public class AlphabetIndexView extends View {
    private List<String> letters = new ArrayList<>();
    private Paint paint;
    private int selectedPosition = -1;
    private OnIndexTouchListener listener;
    private int activeColor = Color.BLUE;
    private int inactiveColor = Color.GRAY;
    private Palette palette;
    private float textSize = 18;

    public interface OnIndexTouchListener {
        void onIndexTouched(String letter);
    }

    public AlphabetIndexView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        if (palette!=null) {
            paint.setColor(palette.textColor);
        }
        paint.setTextSize(textSize); // Adjust this text size to fit your UI
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    public void setLetters(List<String> letters) {
        this.letters = letters;
        invalidate();
    }

    public void setOnIndexTouchListener(OnIndexTouchListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (letters == null || letters.isEmpty()) return;

        int height = getHeight();
        int width = getWidth();
        int itemHeight = height / letters.size();

        for (int i = 0; i < letters.size(); i++) {
            if (i == selectedPosition) {
                paint.setColor(activeColor);
                paint.setFakeBoldText(true);
            } else {
                paint.setColor(inactiveColor);
                paint.setFakeBoldText(false);
            }
            float xPos = width / 2f;
            float yPos = (itemHeight * i) + (itemHeight / 2f) + (paint.getTextSize() / 2f);
            canvas.drawText(letters.get(i), xPos, yPos, paint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (letters == null || letters.isEmpty()) return false;

        float y = event.getY();
        int position = (int) (y / (getHeight() / (float)letters.size()));

        // Prevent IndexOutOfBounds
        if (position < 0) position = 0;
        if (position >= letters.size()) position = letters.size() - 1;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (selectedPosition != position) {
                    selectedPosition = position;
                    if (listener != null) {
                        listener.onIndexTouched(letters.get(position));
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                selectedPosition = -1;
                invalidate();
                break;
        }
        return true;
    }

    public void setPalette(Palette palette) {
        this.palette = palette;
        this.activeColor = palette.textColor;
        this.inactiveColor = palette.hintColor;
        init();
        invalidate();
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        init();
        invalidate();
    }
}
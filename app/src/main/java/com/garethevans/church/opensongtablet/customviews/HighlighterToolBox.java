package com.garethevans.church.opensongtablet.customviews;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class HighlighterToolBox extends LinearLayout implements View.OnTouchListener {

    private final static float CLICK_DRAG_TOLERANCE = 10; // Often, there will be a slight, unintentional, drag when the user taps the FAB, so we need to account for this.

    private float downRawX, downRawY;
    private float dX, dY;
    private LinearLayout toolSettings, colorsLayout;
    private FloatingActionButton currentTool, saveButton, penFAB, highlighterFAB, eraserFAB,
            undoFAB, redoFAB, deleteFAB, blackFAB, whiteFAB, yellowFAB, redFAB, greenFAB, blueFAB;
    private ImageView dragIcon;
    private MaterialSlider sizeSlider;
    private Drawable penDrawable, highlighterDrawable, eraserDrawable;
    private boolean settingsVisible = false;
    public HighlighterToolBox(Context context) {
        super(context);
        initialise(context);
    }

    public HighlighterToolBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialise(context);
    }

    private void initialise(Context context) {
        setOnTouchListener(this);

        inflate(context, R.layout.view_highlighter_toolbar,this);

        dragIcon = findViewById(R.id.dragIcon);
        toolSettings = findViewById(R.id.toolSettings);
        currentTool = findViewById(R.id.currentTool);
        saveButton = findViewById(R.id.saveButton);
        penFAB = findViewById(R.id.penFAB);
        highlighterFAB = findViewById(R.id.highlighterFAB);
        eraserFAB = findViewById(R.id.eraserFAB);
        undoFAB = findViewById(R.id.undoFAB);
        redoFAB = findViewById(R.id.redoFAB);
        deleteFAB = findViewById(R.id.deleteFAB);
        colorsLayout = findViewById(R.id.colorsLayout);
        blackFAB = findViewById(R.id.color_black);
        whiteFAB = findViewById(R.id.color_white);
        yellowFAB = findViewById(R.id.color_yellow);
        redFAB = findViewById(R.id.color_red);
        greenFAB = findViewById(R.id.color_green);
        blueFAB = findViewById(R.id.color_blue);
        sizeSlider = findViewById(R.id.sizeSlider);

        setDrawables(context);

        setListeners();

    }

    private void setDrawables(Context context) {
        penDrawable = VectorDrawableCompat.create(context.getResources(),R.drawable.pen,context.getTheme());
        highlighterDrawable = VectorDrawableCompat.create(context.getResources(),R.drawable.highlighter,context.getTheme());
        eraserDrawable = VectorDrawableCompat.create(context.getResources(),R.drawable.eraser,context.getTheme());
    }

    public void changeDrawable(String which) {
        switch (which) {
            case "pen":
            default:
                currentTool.setImageDrawable(penDrawable);
                break;
            case "highlighter":
                currentTool.setImageDrawable(highlighterDrawable);
                break;
            case "eraser":
                currentTool.setImageDrawable(eraserDrawable);
                break;
        }
        penFAB.setAlpha(which.equals("pen") ? 1f:0.5f);
        highlighterFAB.setAlpha(which.equals("highlighter") ? 1f:0.5f);
        eraserFAB.setAlpha(which.equals("eraser") ? 1f:0.5f);
    }

    public void setColor(String which, Drawable check) {
        blackFAB.setImageDrawable(which.equals("black") ? check:null);
        whiteFAB.setImageDrawable(which.equals("white") ? check:null);
        yellowFAB.setImageDrawable(which.equals("yellow") ? check:null);
        redFAB.setImageDrawable(which.equals("red") ? check:null);
        greenFAB.setImageDrawable(which.equals("green") ? check:null);
        blueFAB.setImageDrawable(which.equals("blue") ? check:null);
    }

    private void setListeners() {
        currentTool.setOnClickListener(view -> toggleSettings());
        sizeSlider.setLabelFormatter(value -> (int)value + "pt");
        sizeSlider.addOnChangeListener((slider, value, fromUser) -> sizeSlider.setHint((int)value + "pt"));
    }

    public void checkShowcase(Context c, MainActivityInterface mainActivityInterface) {
        // Add showcase info for the main toolbar options
        ArrayList<View> views = new ArrayList<>();
        ArrayList<String> infos = new ArrayList<>();
        ArrayList<Boolean> rects = new ArrayList<>();
        views.add(dragIcon);
        views.add(currentTool);
        views.add(saveButton);
        infos.add(c.getString(R.string.highligher_drag));
        infos.add(c.getString(R.string.highligher_tool_current));
        infos.add(c.getString(R.string.save_changes));
        rects.add(false);
        rects.add(false);
        rects.add(false);
        mainActivityInterface.getShowCase().sequenceShowCase((Activity)c, views,null,infos,rects,"highlighterEdit");
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

        int action = motionEvent.getAction();
        if (action == MotionEvent.ACTION_DOWN) {

            downRawX = motionEvent.getRawX();
            downRawY = motionEvent.getRawY();
            dX = view.getX() - downRawX;
            dY = view.getY() - downRawY;

            return true; // Consumed

        } else if (action == MotionEvent.ACTION_MOVE) {

            int viewWidth = view.getWidth();
            int viewHeight = view.getHeight();

            View viewParent = (View)view.getParent();
            int parentWidth = viewParent.getWidth();
            int parentHeight = viewParent.getHeight();

            float newX = motionEvent.getRawX() + dX;
            newX = Math.max(layoutParams.leftMargin, newX); // Don't allow the FAB past the left hand side of the parent
            newX = Math.min(parentWidth - viewWidth - layoutParams.rightMargin, newX); // Don't allow the FAB past the right hand side of the parent

            float newY = motionEvent.getRawY() + dY;
            newY = Math.max(layoutParams.topMargin, newY); // Don't allow the FAB past the top of the parent
            newY = Math.min(parentHeight - viewHeight - layoutParams.bottomMargin, newY); // Don't allow the FAB past the bottom of the parent

            view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start();

            return true; // Consumed

        } else if (action == MotionEvent.ACTION_UP) {

            float upRawX = motionEvent.getRawX();
            float upRawY = motionEvent.getRawY();

            float upDX = upRawX - downRawX;
            float upDY = upRawY - downRawY;

            if (Math.abs(upDX) < CLICK_DRAG_TOLERANCE && Math.abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
                return performClick();
            }
            else { // A drag
                return true; // Consumed
            }

        }
        else {
            return super.onTouchEvent(motionEvent);
        }
    }

    private void toggleSettings() {
        settingsVisible = !settingsVisible;
        toolSettings.setVisibility(settingsVisible ? View.VISIBLE:View.GONE);
    }

    public void setSaveOnClickListener(OnClickListener onClickListener) {
        saveButton.setOnClickListener(onClickListener);
    }

    public void setPenOnClickListener(OnClickListener onClickListener) {
        penFAB.setOnClickListener(onClickListener);
    }

    public void setHighlighterOnClickListener(OnClickListener onClickListener) {
        highlighterFAB.setOnClickListener(onClickListener);
    }

    public void setEraserOnClickListener(OnClickListener onClickListener) {
        eraserFAB.setOnClickListener(onClickListener);
    }

    public void setUndoOnClickListener(OnClickListener onClickListener) {
        undoFAB.setOnClickListener(onClickListener);
    }

    public void setRedoOnClickListener(OnClickListener onClickListener) {
        redoFAB.setOnClickListener(onClickListener);
    }

    public void setDeleteOnClickListener(OnClickListener onClickListener) {
        deleteFAB.setOnClickListener(onClickListener);
    }

    public void setBlackFABOnClickListener(OnClickListener onClickListener) {
        blackFAB.setOnClickListener(onClickListener);
    }
    public void setWhiteFABOnClickListener(OnClickListener onClickListener) {
        whiteFAB.setOnClickListener(onClickListener);
    }
    public void setYellowFABOnClickListener(OnClickListener onClickListener) {
        yellowFAB.setOnClickListener(onClickListener);
    }
    public void setRedFABOnClickListener(OnClickListener onClickListener) {
        redFAB.setOnClickListener(onClickListener);
    }
    public void setGreenFABOnClickListener(OnClickListener onClickListener) {
        greenFAB.setOnClickListener(onClickListener);
    }
    public void setBlueFABOnClickListener(OnClickListener onClickListener) {
        blueFAB.setOnClickListener(onClickListener);
    }

    public void hideColors(boolean hide) {
        colorsLayout.setVisibility(hide ? View.GONE:View.VISIBLE);
    }

    public void setSizeSlider(int size) {
        sizeSlider.setValue(size);
    }
    public int getSizeSliderValue() {
        return (int)sizeSlider.getValue();
    }

    public void setSizeSliderListener(Slider.OnSliderTouchListener onSliderTouchListener) {
        sizeSlider.addOnSliderTouchListener(onSliderTouchListener);
    }
    public void setUndoFABEnabled(boolean enabled) {
        undoFAB.setEnabled(enabled);
    }
    public void setRedoFABEnabled(boolean enabled) {
        redoFAB.setEnabled(enabled);
    }
}

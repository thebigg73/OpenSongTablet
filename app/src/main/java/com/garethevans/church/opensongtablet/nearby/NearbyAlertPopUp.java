package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NearbyAlertPopUp {

    private PopupWindow popupWindow;
    private MyFloatingActionButton closeButton;
    private FloatWindow floatWindow;
    private int posX;
    private int posY;
    private int stickyWidth;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "NearbyAlertPopUp";

    private final Context c;
    private final MainActivityInterface mainActivityInterface;

    public NearbyAlertPopUp(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }
    public void floatSticky(View viewHolder, String message) {
        // Set up the views
        getPositionAndSize();
        setupViews(message);
        setListeners();
        popupWindow.showAtLocation(viewHolder, Gravity.TOP | Gravity.START, posX, posY);

        // Deal with the moveable element (from the top bar)
        setupDrag();
    }

    private void setupViews(String message) {
        // The popup
        popupWindow = new PopupWindow(c);

        // The main layout (FloatWindow is just a custom linearlayout where I've overridden the performclick
        floatWindow = new FloatWindow(c);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(stickyWidth,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        floatWindow.setLayoutParams(layoutParams);
        floatWindow.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable drawable = (GradientDrawable) ResourcesCompat.getDrawable(c.getResources(),
                R.drawable.popup_sticky,c.getTheme());

        if (drawable!=null) {
            drawable.setColor(mainActivityInterface.getMyThemeColors().getColorOnly(
                    mainActivityInterface.getMyThemeColors().getStickyBackgroundColor()));
        }
        popupWindow.setBackgroundDrawable(null);
        floatWindow.setBackground(drawable);
        floatWindow.setAlpha(mainActivityInterface.getMyThemeColors().getAlphaFloatFromColor(
                mainActivityInterface.getMyThemeColors().getStickyBackgroundColor()));
        floatWindow.setPadding(16,16,16,16);

        // Add the close button
        closeButton = new MyFloatingActionButton(c);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.gravity = Gravity.END;
        closeButton.setLayoutParams(buttonParams);
        closeButton.setSize(FloatingActionButton.SIZE_MINI);
        Drawable closeIcon = ResourcesCompat.getDrawable(c.getResources(),R.drawable.close,c.getTheme());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            closeButton.setElevation(0);
            closeButton.setCompatElevation(0f);     // removes shadow across states
            ViewCompat.setElevation(closeButton, 0f);
            if (closeIcon!=null) {
                closeButton.setStateListAnimator(null);
                closeIcon.setTint(mainActivityInterface.getMyThemeColors().getStickyTextColor());
            }
        }
        closeButton.setImageDrawable(closeIcon);
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        closeButton.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));

        floatWindow.addView(closeButton);

        // Now the TextView for the sticky notes
        MyMaterialSimpleTextView stickyNotes = new MyMaterialSimpleTextView(c);
        stickyNotes.setLayoutParams(new LinearLayout.LayoutParams(stickyWidth,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        stickyNotes.setTextColor(mainActivityInterface.getMyThemeColors().getStickyTextColor());
        stickyNotes.setTypeface(mainActivityInterface.getMyFonts().getStickyFont());
        stickyNotes.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("stickyTextSize",14f));
        stickyNotes.setText(message);
        floatWindow.addView(stickyNotes);
        popupWindow.setContentView(floatWindow);
    }

    private void setListeners() {
        closeButton.setOnClickListener(v -> popupWindow.dismiss());
    }

    private void getPositionAndSize() {
        posX = mainActivityInterface.getPreferences().getMyPreferenceInt("stickyXPosition", -1);
        posY = mainActivityInterface.getPreferences().getMyPreferenceInt("stickyYPosition", -1);
        int w = c.getResources().getDisplayMetrics().widthPixels;
        int h = c.getResources().getDisplayMetrics().heightPixels;
        stickyWidth = mainActivityInterface.getPreferences().getMyPreferenceInt("stickyWidth", 400);

        // Fix the sizes
        if (posX == -1 || posX > w) {
            posX = w - stickyWidth - 32;
        }
        if (posX < 0) {
            posX = 0;
        }
        if (posY == -1 || posY > h) {
            posY = (int) ((float) mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar())*1.2f);
        }
        if (posY < 0) {
            posY = 0;
        }
    }

    private void setupDrag() {
        floatWindow.setOnTouchListener(new View.OnTouchListener() {
            int orgX, orgY;
            int offsetX, offsetY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        orgX = (int) event.getX();
                        orgY = (int) event.getY();
                        floatWindow.performClick();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        offsetX = (int) event.getRawX() - orgX;
                        offsetY = (int) event.getRawY() - orgY;
                        popupWindow.update(offsetX, offsetY, -1, -1, true);
                        break;
                    case MotionEvent.ACTION_UP:
                        mainActivityInterface.getPreferences().setMyPreferenceInt("stickyXPosition", offsetX);
                        mainActivityInterface.getPreferences().setMyPreferenceInt("stickyYPosition", offsetY);
                }
                return true;
            }
        });
    }

    public void closeSticky() {
        if (floatWindow!=null && popupWindow!=null) {
            floatWindow.post(() -> {
                if (popupWindow!=null) {
                    popupWindow.dismiss();
                }
            });
        }
    }

    public void destroyPopup() {
        try {
            if (popupWindow != null) {
                popupWindow.dismiss();
                popupWindow = null;
            }
            if (closeButton != null) {
                closeButton = null;
            }
            if (floatWindow != null) {
                floatWindow = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

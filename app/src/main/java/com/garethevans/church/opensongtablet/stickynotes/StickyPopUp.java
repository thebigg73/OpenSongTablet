package com.garethevans.church.opensongtablet.stickynotes;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StickyPopUp {

    private PopupWindow popupWindow;
    private FloatingActionButton closeButton;
    private FloatWindow floatWindow;
    private int posX;
    private int posY;
    private int stickyWidth;

    private final Context c;
    private final MainActivityInterface mainActivityInterface;

    public StickyPopUp(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }
    public void floatSticky(View viewHolder, boolean forceShow) {
        // Force show is if we manually clicked on the sticky notes page button
        // If the popup is showing already, dismiss it
        // This is called when a song is about to load
        if (popupWindow!=null && popupWindow.isShowing()) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }

        // If no sticky notes exist for the song, navigate to the edit sticky note fragment
        } else if (mainActivityInterface.getSong().getNotes().isEmpty()) {
            mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_sticky_notes),0);

        // Let's display the popup sticky note
        } else {
            // Set up the views
            getPositionAndSize();
            setupViews();
            setListeners();
            popupWindow.showAtLocation(viewHolder, Gravity.TOP | Gravity.START, posX, posY);

            // If we want to autohide the sticky note, set a post delayed handler
            // Not when we manually opened it though
            if (!forceShow) {
                dealWithAutohide();
            }

            // Deal with the moveable element (from the top bar)
            setupDrag();
        }
    }

    private void setupViews() {
        // The popup
        popupWindow = new PopupWindow(c);

        // The main layout (FloatWindow is just a custom linearlayout where I've overridden the performclick
        floatWindow = new FloatWindow(c);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(stickyWidth,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        floatWindow.setLayoutParams(layoutParams);
        floatWindow.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable drawable = (GradientDrawable) ResourcesCompat.getDrawable(c.getResources(),
                R.drawable.popup_sticky,null);
        if (drawable!=null) {
            drawable.setColor(mainActivityInterface.getMyThemeColors().getStickyBackgroundSplitColor());
        }
        popupWindow.setBackgroundDrawable(null);
        floatWindow.setAlpha(mainActivityInterface.getMyThemeColors().getStickyBackgroundSplitAlpha());
        floatWindow.setBackground(drawable);
        floatWindow.setPadding(16,16,16,16);

        // Add the close button
        closeButton = new FloatingActionButton(c);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.gravity = Gravity.END;
        closeButton.setLayoutParams(buttonParams);
        closeButton.setSize(FloatingActionButton.SIZE_MINI);
        closeButton.setImageDrawable(ResourcesCompat.getDrawable(c.getResources(),
                R.drawable.close,null));
        floatWindow.addView(closeButton);

        // Now the TextView for the sticky notes
        TextView stickyNotes = new TextView(c);
        stickyNotes.setLayoutParams(new LinearLayout.LayoutParams(stickyWidth,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        stickyNotes.setTextColor(mainActivityInterface.getMyThemeColors().getStickyTextColor());
        stickyNotes.setTypeface(mainActivityInterface.getMyFonts().getStickyFont());
        stickyNotes.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("stickyTextSize",14f));
        stickyNotes.setText(mainActivityInterface.getSong().getNotes());
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
            floatWindow.post(() -> popupWindow.dismiss());
        }
    }

    private void dealWithAutohide() {
        Handler handler = new Handler();
        long displayTime = mainActivityInterface.getPreferences().getMyPreferenceInt("timeToDisplaySticky",0) * 1000L;
        if (displayTime>0) {
            try {
                handler.postDelayed(this::closeSticky, displayTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

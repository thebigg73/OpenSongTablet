package com.garethevans.church.opensongtablet.abcnotation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.core.content.res.ResourcesCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Virtually identical to the sticky popup, but with its own positions
public class ABCPopup {
    private PopupWindow popupWindow;
    private FloatingActionButton closeButton;
    private FloatWindow floatWindow;
    private int posX;
    private int posY;

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final String TAG = "ABCPopup";

    public ABCPopup(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }
    public void floatABC(View viewHolder, boolean forceShow) {
        // Force show is if we manually clicked on the score page button
        // If the popup is showing already, dismiss it
        // This is called when a song is about to load
        if (popupWindow!=null && popupWindow.isShowing()) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // If no score notes exist for the song, navigate to the edit score fragment
        } else if (mainActivityInterface.getSong().getNotes().isEmpty()) {
            mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_abc),0);

            // Let's display the popup music score
        } else {
            // Set up the views
            getPositionAndSize();
            setupViews();
            setListeners();
            popupWindow.showAtLocation(viewHolder, Gravity.TOP | Gravity.START, posX, posY);

            Log.d(TAG,"showing, posX="+posX+"  posY="+posY);
            // If we want to autohide the score, set a post delayed handler
            // Not when we manually opened it though
            if (!forceShow) {
                dealWithAutohide();
            }

            // Deal with the moveable element (from the top bar)
            setupDrag();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupViews() {
        // The popup
        popupWindow = new PopupWindow(c);

        // The main layout (FloatWindow) is just a custom linearlayout where I've overridden the performclick
        floatWindow = new FloatWindow(c);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100,100);
        floatWindow.setLayoutParams(layoutParams);
        floatWindow.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable drawable = (GradientDrawable) ResourcesCompat.getDrawable(c.getResources(),
                R.drawable.popup_sticky,null);
        if (drawable!=null) {
            drawable.setColor(mainActivityInterface.getMyThemeColors().getColorInt("white"));
        }
        popupWindow.setBackgroundDrawable(null);
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

        // Now the WebView for the music score
        WebView webView = new WebView(c);
        webView.setAlpha(mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha());
        webView.setLayoutParams(new LinearLayout.LayoutParams((int)(mainActivityInterface.getDisplayMetrics()[0] *
                mainActivityInterface.getPreferences().getMyPreferenceFloat("abcPopupWidth",0.95f)),
                LinearLayout.LayoutParams.WRAP_CONTENT));
        webView.getSettings().setJavaScriptEnabled(true);
        mainActivityInterface.getAbcNotation().setWebView(webView, mainActivityInterface,
                    false);
        floatWindow.addView(webView);
        popupWindow.setContentView(floatWindow);
    }

    private void setListeners() {
        closeButton.setOnClickListener(v -> popupWindow.dismiss());
    }

    private void getPositionAndSize() {
        posX = mainActivityInterface.getPreferences().getMyPreferenceInt("stickyXPosition", -1);
        posY = mainActivityInterface.getPreferences().getMyPreferenceInt("stickyYPosition", -1);

        // Fix the sizes
        if (posX < 0) {
            posX = 0;
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

    public void closeScore() {
        if (floatWindow!=null && popupWindow!=null) {
            floatWindow.post(() -> popupWindow.dismiss());
        }
    }

    private void dealWithAutohide() {
        Handler handler = new Handler();
        long displayTime = mainActivityInterface.getPreferences().getMyPreferenceInt("timeToDisplaySticky",0) * 1000L;
        if (displayTime>0) {
            try {
                handler.postDelayed(this::closeScore, displayTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

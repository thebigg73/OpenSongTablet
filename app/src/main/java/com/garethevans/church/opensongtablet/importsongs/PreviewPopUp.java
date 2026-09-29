package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.util.SparseLongArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialButton;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class PreviewPopUp {

    private PopupWindow popupWindow;
    private MyFloatingActionButton closeButton;
    private FloatWindow floatWindow;
    private int posX;
    private int posY;
    private int w,h;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "PreviewPopUp";
    private final ArrayList<View> songViews;
    private final ImportOnlineFragment onlineFragment;

    private final Context c;
    private final MainActivityInterface mainActivityInterface;

    public PreviewPopUp(Context c, ArrayList<View> songViews, ImportOnlineFragment onlineFragment) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        this.songViews = songViews;
        this.onlineFragment = onlineFragment;
    }

    private boolean doingShowPopUp = false;
    public void floatPreview(View viewHolder) {
        // Make sure there is a delay to avoid double action
        if (!doingShowPopUp) {
            doingShowPopUp = true;
            // If the popup is showing already, dismiss it
            if (popupWindow != null && popupWindow.isShowing()) {
                try {
                    popupWindow.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // If no sticky notes exist for the song, navigate to the edit sticky note fragment
            } else {
                Log.d(TAG, "Try to show the preview");
                // Set up the views
                getPositionAndSize();
                setupViews();
                setListeners();

                Log.d(TAG, "About to show popup");
                long showStart = System.currentTimeMillis();

// Post it to the next message queue turn
                viewHolder.post(() -> {
                    popupWindow.showAtLocation(viewHolder, Gravity.TOP | Gravity.START, posX, posY);
                    Log.d(TAG, "Popup shown in: " + (System.currentTimeMillis() - showStart) + "ms");
                });
            }
            mainActivityInterface.getMainHandler().postDelayed(() -> doingShowPopUp = false, 500);
        }
    }

    private void setupViews() {
        // The popup
        popupWindow = new PopupWindow(c);

        // The main layout
        floatWindow = new FloatWindow(c);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(w + 16, h + 16);
        floatWindow.setLayoutParams(layoutParams);
        floatWindow.setOrientation(LinearLayout.VERTICAL);

        GradientDrawable drawable = (GradientDrawable) ResourcesCompat.getDrawable(c.getResources(),
                R.drawable.popup_bg, null);
        if (drawable != null) {
            drawable.setColor(0xFFFFFFFF);
        }
        popupWindow.setBackgroundDrawable(null);
        floatWindow.setBackground(drawable);
        floatWindow.setPadding(16, 16, 16, 16);

        // Add close button
        closeButton = new MyFloatingActionButton(c);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.gravity = Gravity.END;
        closeButton.setLayoutParams(buttonParams);
        closeButton.setSize(FloatingActionButton.SIZE_MINI);

        Drawable closeIcon = ContextCompat.getDrawable(c, R.drawable.close);
        if (closeIcon != null) {
            closeIcon = DrawableCompat.wrap(closeIcon).mutate();
            closeIcon.setColorFilter(0xFF000000, PorterDuff.Mode.SRC_IN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            closeButton.makeFlat();
        }
        closeButton.setImageDrawable(closeIcon);
        closeButton.setImageTintList(null);
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        closeButton.setBackgroundTintList(new ColorStateList(
                new int[][]{new int[0]},
                new int[]{Color.TRANSPARENT}
        ));
        floatWindow.addView(closeButton);

        // Scroll containers
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(c);
        horizontalScrollView.setLayoutParams(new LinearLayout.LayoutParams(w, h));

        ScrollView scrollView = new ScrollView(c);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout linearLayout = new LinearLayout(c);
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setClipChildren(false);
        linearLayout.setClipToPadding(false);

        long start = System.currentTimeMillis();
        for (int x = 0; x < songViews.size(); x++) {
            linearLayout.addView(songViews.get(x));
        }

        scrollView.addView(linearLayout);
        horizontalScrollView.addView(scrollView);
        floatWindow.addView(horizontalScrollView);

        // Add 'Continue' button
        MyMaterialButton continueButton = new MyMaterialButton(c);
        continueButton.setText(c.getString(R.string.continue_text));
        floatWindow.addView(continueButton);
        continueButton.setOnClickListener((view) -> {
            if (onlineFragment!=null) {
                destroyPopup();
                onlineFragment.setupSaveLayout();
            }
        });

        popupWindow.setContentView(floatWindow);
    }

    private void setListeners() {
        closeButton.setOnClickListener(v -> popupWindow.dismiss());
    }

    private void getPositionAndSize() {
        int screenWidth = c.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = c.getResources().getDisplayMetrics().heightPixels;
        w = Math.round((float)c.getResources().getDisplayMetrics().widthPixels*0.85f) - 16;
        h = Math.round((float)c.getResources().getDisplayMetrics().heightPixels*0.85f) - 16;
        posX = (screenWidth - w) / 2;
        posY = (screenHeight - h) / 2;
    }


    public void destroyPopup() {
        if (floatWindow != null && popupWindow != null) {
            floatWindow.post(() -> {
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
            });
        }
    }
}

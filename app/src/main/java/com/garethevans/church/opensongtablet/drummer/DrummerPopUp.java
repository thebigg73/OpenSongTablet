package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.DialogHeader;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class DrummerPopUp {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "DrummerPopup";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final int posX, posY;
    private final Drawable drummerPlay, drummerStop;
    private final float pageButtonAlpha;
    private PopupWindow popupWindow;
    private MyFloatingActionButton closeButton, drummerPlayStop, drummerFill, drummerTransition,
            drummerSettings;
    private FloatWindow floatWindow;

    // Initialise the popup class
    public DrummerPopUp(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        posX = 0;
        posY = (int) ((float) mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar()) * 1.2f);
        drummerPlay = ContextCompat.getDrawable(c, R.drawable.play);
        drummerStop = ContextCompat.getDrawable(c, R.drawable.stop);
        pageButtonAlpha = mainActivityInterface.getMyThemeColors().getPageButtonAlpha();
    }

    // The views and listeners for the popup
    public void floatWindow(View viewHolder) {
        // If the popup is showing already, dismiss it
        if (popupWindow != null && popupWindow.isShowing()) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Set up the views
            setupViews();
            setListeners();

            popupWindow.showAtLocation(viewHolder, Gravity.TOP | Gravity.START, posX, posY);

            // Deal with the moveable element (from the top bar)
            setupDrag();
        }
    }

    private void setupViews() {
        // The popup
        popupWindow = new PopupWindow(c);
        popupWindow.setBackgroundDrawable(null);

        // The main layout (FloatWindow is just a custom linearlayout where I've overridden the performclick
        floatWindow = new FloatWindow(c);
        floatWindow.setAlpha(pageButtonAlpha);

        View myView = View.inflate(c, R.layout.view_drummer_popup, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            myView.findViewById(R.id.layout).setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().secondary));
        } else {
            myView.findViewById(R.id.layout).setBackgroundColor(mainActivityInterface.getPalette().secondary);
        }
        floatWindow.addView(myView);

        DialogHeader dialogHeader = myView.findViewById(R.id.dialogHeader);
        dialogHeader.setText(c.getString(R.string.drummer));
        dialogHeader.setWebHelp(mainActivityInterface, c.getString(R.string.website_drummer));
        closeButton = dialogHeader.getCloseButton();
        drummerPlayStop = myView.findViewById(R.id.drummerPlayStop);
        drummerFill = myView.findViewById(R.id.drummerFill);
        drummerTransition = myView.findViewById(R.id.drummerTransition);
        drummerSettings = myView.findViewById(R.id.drummerSettings);
        drummerPlayStop.setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().secondaryVariant));
        drummerFill.setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().secondaryVariant));
        drummerTransition.setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().secondaryVariant));
        drummerSettings.setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().secondaryVariant));

        if (mainActivityInterface.getDrumViewModel().getDrummer().getIsRunning()) {
            drummerPlayStop.setImageDrawable(drummerStop);
        } else {
            drummerPlayStop.setImageDrawable(drummerPlay);
        }

        popupWindow.setContentView(floatWindow);
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
                }
                return true;
            }
        });
    }

    private void setListeners() {
        closeButton.setOnClickListener(view -> destroyPopup());
        drummerPlayStop.setOnClickListener(view -> {
            if (mainActivityInterface.getDrumViewModel().getDrummer().getIsRunning()) {
                mainActivityInterface.getDrumViewModel().stopDrummer();
                drummerPlayStop.setImageDrawable(drummerPlay);
            } else {
                mainActivityInterface.getDrumViewModel().startDrummer();
                drummerPlayStop.setImageDrawable(drummerStop);
            }
        });
        drummerFill.setOnClickListener(view -> {
            if (mainActivityInterface.getDrumViewModel().getDrummer().getIsRunning()) {
                mainActivityInterface.getDrumViewModel().drummerFill();
            }
        });
        drummerTransition.setOnClickListener(view -> {
            if (mainActivityInterface.getDrumViewModel().getDrummer().getIsRunning()) {
                mainActivityInterface.getDrumViewModel().drummerTransition();
            }
        });
        drummerSettings.setOnClickListener(view -> {
            destroyPopup();
            mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_drummer_settings),0);
        });
    }

    // Close down the popup and completely stop and release all resources
    public void destroyPopup() {
        try {
            closeButton = null;
            floatWindow = null;
            if (popupWindow != null) {
                popupWindow.dismiss();
                popupWindow = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getIsShowing() {
        return popupWindow != null && popupWindow.isShowing();
    }
}

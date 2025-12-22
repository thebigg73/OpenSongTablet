package com.garethevans.church.opensongtablet.abcnotation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.FloatWindow;
import com.garethevans.church.opensongtablet.customviews.InlineAbcWebView;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Virtually identical to the sticky popup, but with its own positions
public class ABCPopup {
    private PopupWindow popupWindow;
    private MyFloatingActionButton closeButton;
    private FloatWindow floatWindow;
    private int posX;
    private int posY;

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "ABCPopup";
    private final Handler handler = new Handler();
    private final Runnable autoCloseScoreRunnable = this::closeScore;
    private InlineAbcObject inlineAbcObject;
    private InlineAbcWebView inlineAbcWebView;
    private View viewHolder;

    public ABCPopup(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        mainActivityInterface.getAbcNotation().setAbcPopup(this);
    }

    public void floatABC(View viewHolder, boolean forceShow) {
        // Force show is if we manually clicked on the score page button
        // If the popup is showing already, dismiss it
        // This is called when a song is about to load
        this.viewHolder = viewHolder;
        if (popupWindow!=null && popupWindow.isShowing()) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // If no score notes exist for the song, navigate to the edit score fragment
        } else if (mainActivityInterface.getSong().getAbc()==null || mainActivityInterface.getSong().getAbc().isEmpty()) {
            mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_abc),0);

            // Let's display the popup music score
        } else {
            mainActivityInterface.getAbcNotation().prepareSongValues(mainActivityInterface.getSong());
            // Set up the views
            getPositionAndSize();
            setupViews();
            setListeners();
            Log.d(TAG,"popUpWindow:"+popupWindow);
            Log.d(TAG,"getting here in the PopUp");

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
        Log.d(TAG, "ABC PopUpViews");
        // The popup
        popupWindow = new PopupWindow(c);

        // Check if the user wants to autotranspose the abc to the song key.  If so do it
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("abcTransposeAuto", true)) {
            mainActivityInterface.getAbcNotation().getABCTransposeFromSongKey();
        }

        float[] splitColors = mainActivityInterface.getMyThemeColors().getAbcColorAndAlphaSplit();

        Log.d(TAG, "color:" + splitColors[0] + "  alpha:" + splitColors[1]);
        // The main layout (FloatWindow) is just a custom linearlayout where I've overridden the performclick
        floatWindow = new FloatWindow(c);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        floatWindow.setLayoutParams(layoutParams);
        floatWindow.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable drawable = (GradientDrawable) ResourcesCompat.getDrawable(c.getResources(),
                R.drawable.popup_sticky, null);
        if (drawable != null) {
            drawable.setColor((int) splitColors[0]);
        }
        popupWindow.setBackgroundDrawable(null);
        floatWindow.setBackground(drawable);
        floatWindow.setAlpha(splitColors[1]);
        floatWindow.setPadding(16, 16, 16, 16);

        // Add the close button
        closeButton = new MyFloatingActionButton(c);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.gravity = Gravity.END;
        closeButton.setLayoutParams(buttonParams);
        closeButton.setSize(FloatingActionButton.SIZE_MINI);
        Drawable closeIcon = ContextCompat.getDrawable(c, R.drawable.close);
        if (closeIcon != null) {
            closeIcon = DrawableCompat.wrap(closeIcon).mutate();          // 🔑 mutate to avoid affecting other instances
            closeIcon.setColorFilter(mainActivityInterface.getMyThemeColors().getStickyTextColor(), PorterDuff.Mode.SRC_IN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            closeButton.setElevation(0);
            closeButton.setCompatElevation(0f);     // removes shadow across states
            ViewCompat.setElevation(closeButton, 0f);
            closeButton.setStateListAnimator(null);
        }
        closeButton.setImageDrawable(closeIcon);
        closeButton.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));

        floatWindow.addView(closeButton);

        // Now the WebView for the music score
        inlineAbcObject = new InlineAbcObject(c, null, 0,
                mainActivityInterface.getMyThemeColors().getColorInt("transparent"));
        inlineAbcObject.setAbcItem(0);
        inlineAbcObject.setIsPopup(true);
        inlineAbcObject.setMainColor(mainActivityInterface.getMyThemeColors().getHexFromIntNoAlpha(mainActivityInterface.getMyThemeColors().getAbcPopupTextColor()));
        inlineAbcObject.setChordColor(mainActivityInterface.getMyThemeColors().getHexFromIntNoAlpha(mainActivityInterface.getMyThemeColors().getAbcPopupTextColor()));

        // Make it visible and touchable as the object hides the WebView in favour of the ImageView
        inlineAbcWebView = new InlineAbcWebView(c);
        inlineAbcWebView.setLayoutParams(new LinearLayout.LayoutParams(
                mainActivityInterface.getAbcNotation().getAbcPopupScreenWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT));
        inlineAbcObject.setInlineAbcWebView(inlineAbcWebView);
        inlineAbcWebView.setVisibility(View.VISIBLE);
        inlineAbcWebView.setAllowTouch(true);
        floatWindow.addView(inlineAbcWebView);
        floatWindow.setAlpha(splitColors[1]);
        popupWindow.setContentView(floatWindow);
        popupWindow.showAtLocation(viewHolder, Gravity.TOP | Gravity.START, posX, posY);
        inlineAbcObject.getInlineAbcWebView();
        mainActivityInterface.getMainHandler().postDelayed(() -> {
            if (inlineAbcObject != null) {
                inlineAbcObject.updateContent();
            }
        }, 1000);
    }

    private void setListeners() {
        closeButton.setOnClickListener(v -> popupWindow.dismiss());
    }

    private void getPositionAndSize() {
        posX = mainActivityInterface.getPreferences().getMyPreferenceInt("abcXPosition", -1);
        posY = mainActivityInterface.getPreferences().getMyPreferenceInt("abcYPosition", -1);

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
                        mainActivityInterface.getPreferences().setMyPreferenceInt("abcXPosition", offsetX);
                        mainActivityInterface.getPreferences().setMyPreferenceInt("abcYPosition", offsetY);
                }
                return true;
            }
        });
    }

    public void closeScore() {
        handler.removeCallbacks(autoCloseScoreRunnable);
        if (floatWindow!=null && popupWindow!=null) {
            floatWindow.post(() -> popupWindow.dismiss());
        }
    }

    private void dealWithAutohide() {
        Handler handler = new Handler();
        long displayTime = mainActivityInterface.getPreferences().getMyPreferenceInt("timeToDisplaySticky",0) * 1000L;
        if (displayTime>0) {
            try {
                handler.postDelayed(autoCloseScoreRunnable, displayTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setMeasured(int width, int height) {
        Log.d(TAG,"setMeasured("+width+","+height+")");
        inlineAbcWebView.post(() -> {
            if (inlineAbcObject!=null) {
                inlineAbcObject.setAbcWidth(width);
                inlineAbcObject.setAbcHeight(height);
                inlineAbcObject.setAbcMeasured(true);
            }

            if (inlineAbcWebView!=null) {
                inlineAbcWebView.setVisibility(View.VISIBLE);
            }
        });
    }
}

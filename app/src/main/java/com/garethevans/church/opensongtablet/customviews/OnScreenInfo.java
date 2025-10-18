package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class OnScreenInfo extends LinearLayout {

    private final LinearLayout info;
    private final LinearLayout capo;
    private final MyMaterialSimpleTextView capoIcon;
    private final MyMaterialSimpleTextView capoInfo;
    private final LinearLayout autoscroll;
    private final MyMaterialSimpleTextView autoscrollIcon;
    private final MyMaterialSimpleTextView autoscrollTime;
    private final MyMaterialSimpleTextView autoscrollTotalTime;
    private final LinearLayout pad;
    private final MyMaterialSimpleTextView padIcon;
    private final MyMaterialSimpleTextView padTime;
    private final MyMaterialSimpleTextView padTotalTime;
    private boolean capoInfoNeeded, capoPulsing, autoHideCapo, autoHidePad, autoHideAutoscroll;
    // IV - Needs to be longer to be seen after song load
    private final int delayTime = 5000;
    private boolean finishedAutoscrollPreDelay = false;

    // The runnables for hiding and showing
    private final Runnable hideCapoRunnable = new Runnable() {
        @Override
        public void run() {
            if (!capoPulsing && capoInfoNeeded) {
                capo.setVisibility(View.GONE);
                capo.clearAnimation();
            }
        }
    };
    private final Runnable showCapoRunnable = new Runnable() {
        @Override
        public void run() {
            if (capoInfoNeeded) {
                capoInfo.post(() -> {
                    capo.setVisibility(View.VISIBLE);
                    capo.clearAnimation();
                });
                capoInfo.removeCallbacks(hideCapoRunnable);
                capoInfo.postDelayed(hideCapoRunnable,delayTime);
            }
        }
    };
    private final Runnable hideAutoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            autoscroll.setVisibility(View.GONE);
        }
    };
    private final Runnable hidePadRunnable = new Runnable() {
        @Override
        public void run() {
            if (autoHidePad) {
                pad.setVisibility(View.GONE);
            }
        }
    };


    public OnScreenInfo(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        MainActivityInterface mainActivityInterface = (MainActivityInterface) context;
        inflate(context, R.layout.view_on_screen_info, this);
        info = findViewById(R.id.info);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            info.setBackgroundTintList(ColorStateList.valueOf(mainActivityInterface.getPalette().secondary));
        } else {
            info.setBackgroundColor(mainActivityInterface.getPalette().secondary);
        }
        capo = findViewById(R.id.capo);
        capoIcon = findViewById(R.id.capoIcon);
        capoInfo = findViewById(R.id.capoInfo);
        autoscroll = findViewById(R.id.autoscroll);
        autoscrollIcon = findViewById(R.id.autoscrollIcon);
        autoscrollTime = findViewById(R.id.autoscrollTime);
        autoscrollTotalTime = findViewById(R.id.autoscrollTotalTime);
        pad = findViewById(R.id.pad);
        padIcon = findViewById(R.id.padIcon);
        padTime = findViewById(R.id.padTime);
        padTotalTime = findViewById(R.id.padTotalTime);
    }

    public void setPreferences(Context c, MainActivityInterface mainActivityInterface) {
        autoHideCapo = mainActivityInterface.getPreferences().getMyPreferenceBoolean("onscreenCapoHide",true);
        autoHidePad  = mainActivityInterface.getPreferences().getMyPreferenceBoolean("onscreenPadHide", true);
        autoHideAutoscroll = mainActivityInterface.getPreferences().getMyPreferenceBoolean("onscreenAutoscrollHide", true);
        updateAlpha(c,mainActivityInterface);
    }

    public void updateAlpha(Context c, MainActivityInterface mainActivityInterface) {
        Drawable drawable = ResourcesCompat.getDrawable(c.getResources(),R.drawable.rounded_dialog_node,c.getTheme());
        if (drawable!=null) {
            info.setBackground(drawable);
        }
        info.setAlpha(mainActivityInterface.getMyThemeColors().getPageButtonAlpha());
        //int textColor = mainActivityInterface.getMyThemeColors().getExtraInfoTextColor();
        //padTime.setTextColor(textColor);
        //padTotalTime.setTextColor(textColor);
        //capoInfo.setTextColor(textColor);
        //autoscrollTime.setTextColor(textColor);
        //autoscrollTotalTime.setTextColor(textColor);
        //TextViewCompat.setCompoundDrawableTintList(autoscrollIcon, ColorStateList.valueOf(textColor));
        //TextViewCompat.setCompoundDrawableTintList(autoscrollTime, ColorStateList.valueOf(textColor));
        //TextViewCompat.setCompoundDrawableTintList(padIcon, ColorStateList.valueOf(textColor));
        //TextViewCompat.setCompoundDrawableTintList(padTime, ColorStateList.valueOf(textColor));
        //TextViewCompat.setCompoundDrawableTintList(capoIcon, ColorStateList.valueOf(textColor));
        //TextViewCompat.setCompoundDrawableTintList(capoInfo, ColorStateList.valueOf(textColor));
    }

    public void dealWithCapo(Context c, MainActivityInterface mainActivityInterface) {
        capoInfoNeeded = !mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter)) &&
                mainActivityInterface.getProcessSong().showingCapo(mainActivityInterface.getSong().getCapo());
        if (capoInfoNeeded) {
            capoInfo.setText(mainActivityInterface.getToolbar().getCapoString());
            capo.setVisibility(View.VISIBLE);
            capoInfo.post(() -> {
                capoInfo.setPivotX(capoInfo.getWidth() / 2f);
                capoInfo.setPivotY(capoInfo.getHeight() / 2f);
                capoPulsing = true;
                mainActivityInterface.getCustomAnimation().pulse(c, capo);
            });
            capoInfo.postDelayed(() -> {
                capo.clearAnimation();
                if (autoHideCapo) {
                    capo.setVisibility(View.GONE);
                }
                capoPulsing = false;
            }, delayTime);
        } else {
            capoPulsing = false;
            capoInfo.setText("");
            capo.clearAnimation();
            capo.setVisibility(View.GONE);
        }
    }

    public void setFinishedAutoscrollPreDelay(boolean finishedAutoscrollPreDelay) {
        this.finishedAutoscrollPreDelay = finishedAutoscrollPreDelay;
    }

    public void showHideViews(MainActivityInterface mainActivityInterface) {
        if (capoInfoNeeded && autoHideCapo) {
            capoInfo.post(showCapoRunnable);
        } else if (capoInfoNeeded) {
            capo.setVisibility(View.VISIBLE);
        } else {
            capo.setVisibility(View.GONE);
        }
        if (mainActivityInterface.getPad().isPadPrepared()) {
            pad.setVisibility(View.VISIBLE);
            pad.removeCallbacks(hidePadRunnable);
            pad.postDelayed(hidePadRunnable, delayTime);
        }
        if (mainActivityInterface.getAutoscroll().getAutoscrollActivated()) {
            autoscroll.setVisibility(View.VISIBLE);
            autoscroll.removeCallbacks(hideAutoScrollRunnable);
            if (finishedAutoscrollPreDelay || !mainActivityInterface.getAutoscroll().getIsAutoscrolling()) {
                autoscroll.postDelayed(hideAutoScrollRunnable, delayTime);
            }
        }
    }
    public LinearLayout getInfo() {
        return info;
    }
    public LinearLayout getPad() {
        return pad;
    }
    public LinearLayout getAutoscroll() {
        return autoscroll;
    }
    public MyMaterialSimpleTextView getAutoscrollTime() {
        return autoscrollTime;
    }
    public MyMaterialSimpleTextView getAutoscrollTotalTime() {
        return autoscrollTotalTime;
    }

    public OnScreenInfo getOnScreenInfo() {
        return this;
    }
    /*public void setFirstShowAutoscroll(boolean firstShowAutoscroll) {
        this.firstShowAutoscroll = firstShowAutoscroll;
    }*/

    public void showCapo(boolean show) {
        if (show && capoInfoNeeded) {
            capo.post(() -> capo.setVisibility(View.VISIBLE));
        } else if (!show) {
            capo.post(() -> capo.setVisibility(View.GONE));
        }
    }
}

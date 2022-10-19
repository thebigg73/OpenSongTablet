package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.textview.MaterialTextView;

public class OnScreenInfo extends LinearLayout {

    private final LinearLayout info;
    private final MaterialTextView capoInfo;
    private final LinearLayout autoscroll;
    private final MaterialTextView autoscrollTime;
    private final MaterialTextView autoscrollTotalTime;
    private final LinearLayout pad;
    private final MaterialTextView padTime;
    private final MaterialTextView padTotalTime;
    private boolean capoInfoNeeded, capoPulsing, autoHideCapo, autoHidePad, autoHideAutoscroll;
    private final int delayTime = 3000;
    private final Runnable hideCapoRunnable = new Runnable() {
        @Override
        public void run() {
            if (!capoPulsing && capoInfoNeeded) {
                capoInfo.setVisibility(View.GONE);
                capoInfo.clearAnimation();
            }
        }
    };
    private final Runnable showCapoRunnable = new Runnable() {
        @Override
        public void run() {
            if (capoInfoNeeded) {
                capoInfo.post(() -> {
                    capoInfo.setVisibility(View.VISIBLE);
                    capoInfo.clearAnimation();
                });
                capoInfo.postDelayed(hideCapoRunnable,delayTime);
            }
        }
    };

    public OnScreenInfo(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_on_screen_info, this);

        info = findViewById(R.id.info);
        capoInfo = findViewById(R.id.capoInfo);
        autoscroll = findViewById(R.id.autoscroll);
        autoscrollTime = findViewById(R.id.autoscrollTime);
        autoscrollTotalTime = findViewById(R.id.autoscrollTotalTime);
        pad = findViewById(R.id.pad);
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
        Drawable drawable = ContextCompat.getDrawable(c,R.drawable.rounded_dialog_node);
        if (drawable!=null) {
            drawable.setColorFilter(mainActivityInterface.getMyThemeColors().getExtraInfoBgSplitColor(),
                    PorterDuff.Mode.SRC_ATOP);
            info.setBackground(drawable);
        }
        info.setAlpha(mainActivityInterface.getMyThemeColors().getExtraInfoBgSplitAlpha());
        int textColor = mainActivityInterface.getMyThemeColors().getExtraInfoTextColor();
        padTime.setTextColor(textColor);
        padTotalTime.setTextColor(textColor);
        capoInfo.setTextColor(textColor);
        autoscrollTime.setTextColor(textColor);
        autoscrollTotalTime.setTextColor(textColor);
        TextViewCompat.setCompoundDrawableTintList(autoscrollTime, ColorStateList.valueOf(textColor));
        TextViewCompat.setCompoundDrawableTintList(padTime, ColorStateList.valueOf(textColor));
        TextViewCompat.setCompoundDrawableTintList(capoInfo, ColorStateList.valueOf(textColor));
    }

    public void dealWithCapo(Context c, MainActivityInterface mainActivityInterface) {
        capoInfoNeeded = !mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter)) &&
                mainActivityInterface.getProcessSong().showingCapo(mainActivityInterface.getSong().getCapo());
        if (capoInfoNeeded) {
            capoInfo.setText(mainActivityInterface.getChordDisplayProcessing().getCapoPosition());
            capoInfo.setVisibility(View.VISIBLE);
            capoInfo.setAlpha(mainActivityInterface.getMyThemeColors().getExtraInfoBgSplitAlpha());
            capoInfo.post(() -> {
                capoInfo.setPivotX(capoInfo.getWidth() / 2f);
                capoInfo.setPivotY(capoInfo.getHeight() / 2f);
                capoPulsing = true;
                mainActivityInterface.getCustomAnimation().pulse(c, capoInfo);
            });
            capoInfo.postDelayed(() -> {
                capoInfo.clearAnimation();
                if (autoHideCapo) {
                    capoInfo.setVisibility(View.GONE);
                }
                capoPulsing = false;
            }, delayTime);
        } else {
            capoPulsing = false;
            capoInfo.setText("");
            capoInfo.clearAnimation();
            capoInfo.setVisibility(View.GONE);
        }
    }

    public void showHideViews(MainActivityInterface mainActivityInterface) {
        if (capoInfoNeeded && autoHideCapo) {
            capoInfo.post(showCapoRunnable);
        } else if (capoInfoNeeded) {
            capoInfo.setVisibility(View.VISIBLE);
        } else {
            capoInfo.setVisibility(View.GONE);
        }

        if (mainActivityInterface.getPad().isPadPrepared()) {
            if (pad.getVisibility()!=View.VISIBLE) {
                pad.setVisibility(View.VISIBLE);
                if (autoHidePad) {
                    pad.postDelayed(() -> pad.setVisibility(View.GONE), delayTime);
                }
            }
        } else {
            pad.setVisibility(View.GONE);
        }
        if (mainActivityInterface.getAutoscroll().getAutoscrollActivated()) {
            if (autoscroll.getVisibility()!=View.VISIBLE) {
                autoscroll.setVisibility(View.VISIBLE);
                if (autoHideAutoscroll) {
                    autoscroll.postDelayed(() -> autoscroll.setVisibility(View.GONE), delayTime);
                }
            }
        } else {
            autoscroll.setVisibility(View.GONE);
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
    public MaterialTextView getAutoscrollTime() {
        return autoscrollTime;
    }
    public MaterialTextView getAutoscrollTotalTime() {
        return autoscrollTotalTime;
    }

}

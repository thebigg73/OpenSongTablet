package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.textview.MaterialTextView;

public class OnScreenInfo extends LinearLayout {

    private final String TAG = "OnScreenInfo";

    private final LinearLayout info;
    private final MaterialTextView capoInfo;
    private final LinearLayout autoscroll;
    private final MaterialTextView autoscrollTime;
    private final MaterialTextView autoscrollTotalTime;
    private final LinearLayout pad;
    private final MaterialTextView padTime;
    private final MaterialTextView padTotalTime;
    private boolean capoInfoNeeded, capoPulsing;
    private final int delayTime = 3000;
    private final Runnable hideCapoRunnable = new Runnable() {
        @Override
        public void run() {
            if (!capoPulsing) {
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

    public void updateAlpha(MainActivityInterface mainActivityInterface) {
        Log.d(TAG,"updating alpha to: "+mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha());
        info.setAlpha(mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha());
    }

    public void dealWithCapo(Context c, MainActivityInterface mainActivityInterface) {
        capoInfoNeeded = !mainActivityInterface.getMode().equals("Presenter") && mainActivityInterface.
                getProcessSong().showingCapo(mainActivityInterface.getSong().getCapo());
        if (capoInfoNeeded) {
            Log.d(TAG, "Trying to show capo info");
            capoInfo.setText(mainActivityInterface.getChordDisplayProcessing().getCapoPosition(c, mainActivityInterface));
            capoInfo.setVisibility(View.VISIBLE);
            capoInfo.setAlpha(mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha());
            capoInfo.post(() -> {
                capoInfo.setPivotX(capoInfo.getWidth() / 2f);
                capoInfo.setPivotY(capoInfo.getHeight() / 2f);
                capoPulsing = true;
                mainActivityInterface.getCustomAnimation().pulse(c, capoInfo);
            });
            capoInfo.postDelayed(() -> {
                capoInfo.clearAnimation();
                capoInfo.setVisibility(View.GONE);
                capoPulsing = false;
            }, delayTime);
        } else {
            capoInfo.setText("");
            capoInfo.clearAnimation();
            capoInfo.setVisibility(View.GONE);
        }
    }

    public void showHideCapo() {
        capoInfo.post(showCapoRunnable);
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

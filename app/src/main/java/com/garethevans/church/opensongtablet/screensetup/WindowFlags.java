package com.garethevans.church.opensongtablet.screensetup;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class WindowFlags {

    private final Window w;
    private int uiOptions;

    public WindowFlags(Window w) {
        this.w = w;
    }

    public void setWindowFlags() {
        View v = w.getDecorView();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        v.setSystemUiVisibility(uiOptions);

        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        w.getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            v.setSystemUiVisibility(uiOptions);
        });
    }
}


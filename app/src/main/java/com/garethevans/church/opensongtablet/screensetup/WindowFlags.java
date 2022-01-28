package com.garethevans.church.opensongtablet.screensetup;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class WindowFlags {

    private final Window w;
    private int uiOptions;
    private final String TAG = "WindowFlags";

    public WindowFlags(Window w) {
        this.w = w;
    }

    public void setWindowFlags() {
        View v = w.getDecorView();
        uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | Window.FEATURE_ACTION_BAR_OVERLAY);
        //w.addFlags(Window.FEATURE_ACTION_BAR_OVERLAY);
        //supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);


        v.setSystemUiVisibility(uiOptions);

        w.getDecorView().setOnSystemUiVisibilityChangeListener(new View
                        .OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        Log.d(TAG,"uivisibilitychange");
                        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        v.setSystemUiVisibility(uiOptions);
                    }
                });
    }
}

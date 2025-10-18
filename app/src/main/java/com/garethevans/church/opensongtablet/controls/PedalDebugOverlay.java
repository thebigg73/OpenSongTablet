package com.garethevans.church.opensongtablet.controls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class PedalDebugOverlay {

    private static final String TAG = "PedalDebug";
    private final Activity activity;
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams params;
    private final TextView textView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isShown = false;

    public PedalDebugOverlay(Activity activity) {
        this.activity = activity;
        this.windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);

        textView = new TextView(activity);
        textView.setBackgroundColor(0xAA000000);
        textView.setTextColor(Color.GREEN);
        textView.setTextSize(14);
        textView.setPadding(20, 20, 20, 20);
        textView.setText("Pedal Debug Overlay Active\nPress any pedal...");

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                android.graphics.PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP;
    }

    /** Show the overlay at the top of the activity window */
    public void show() {
        if (!isShown) {
            windowManager.addView(textView, params);
            isShown = true;
        }
    }

    /** Hide the overlay */
    public void hide() {
        if (isShown) {
            windowManager.removeView(textView);
            isShown = false;
        }
    }

    /** Update overlay text and log key info */
    public void onKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            final String msg = "KeyCode: " + event.getKeyCode()
                    + " (" + KeyEvent.keyCodeToString(event.getKeyCode()) + ")"
                    + "\nScanCode: " + event.getScanCode()
                    + "\nSource: " + event.getSource()
                    + "\nEventTime: " + event.getEventTime();

            Log.d(TAG, msg);
            handler.post(() -> {
                textView.setText(msg);
                textView.setVisibility(View.VISIBLE);
            });

            // Auto-hide after 4 seconds
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(() -> textView.setVisibility(View.GONE), 4000);
        }
    }
}
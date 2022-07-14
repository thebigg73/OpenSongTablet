package com.garethevans.church.opensongtablet.screensetup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.textview.MaterialTextView;

public class ShowToast {

    private final View anchor;
    private final PopupWindow popupWindow;
    private final MaterialTextView textToast;
    private long messageEndTime = 0;
    private final Runnable hidePopupRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public ShowToast(Context c, View anchor) {
        this.anchor = anchor;
        popupWindow = new PopupWindow(c);
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.view_toast,null,false);
        popupWindow.setContentView(view);
        popupWindow.setFocusable(false);
        popupWindow.setBackgroundDrawable(null);
        textToast = view.findViewById(R.id.textToast);
    }

    public void doIt(final String message) {
        try {

            if (message != null && !message.isEmpty()) {
                // Toasts with custom layouts are deprecated and look ugly!
                // Use a more customisable popup window

                // If a message is already showing, then wait
                long delayTime;
                long currTime = System.currentTimeMillis();
                if (currTime > messageEndTime) {
                    // Good to go now
                    delayTime = 0;
                    messageEndTime = currTime + 2000;
                } else {
                    delayTime = messageEndTime - currTime + 500;
                }

                Runnable showRunnable = () -> {
                    if (textToast!=null && popupWindow!=null) {
                        textToast.setText(message);
                        popupWindow.showAtLocation(anchor, Gravity.CENTER, 0, 0);
                        messageEndTime = System.currentTimeMillis() + 2000;
                        new Handler().postDelayed(hidePopupRunnable, 2000);
                    }
                };
                new Handler(Looper.getMainLooper()).postDelayed(showRunnable, delayTime);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

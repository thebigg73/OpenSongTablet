package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

class PopUpSizeAndAlpha {

    static void decoratePopUp(Activity ac, Dialog dialog) {

        try {
            // Get available width and height
            DisplayMetrics metrics = new DisplayMetrics();
            ac.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int height = metrics.heightPixels;
            int width = metrics.widthPixels;

            float myscale = 10.0f;
            float myalpha = 0.8f;
            float mydim = 0.2f;

            switch (FullscreenActivity.whattodo) {
                case "showtheset":
                    myscale = FullscreenActivity.popupScale_Set;
                    myalpha = FullscreenActivity.popupAlpha_Set;
                    mydim = FullscreenActivity.popupDim_Set;
                    break;
            }
            int desired_width = (int) ((float) width * (myscale / 10.0f));
            int desired_height = (int) ((float) height * (myscale / 10.0f));

            Window dw = dialog.getWindow();
            if (dw != null) {
                dialog.getWindow().setLayout(desired_width, desired_height);
                WindowManager.LayoutParams lp = dw.getAttributes();
                lp.alpha = myalpha;
                lp.dimAmount = mydim;
                dw.setAttributes(lp);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

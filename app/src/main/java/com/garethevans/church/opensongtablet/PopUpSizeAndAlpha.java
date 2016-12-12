package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

class PopUpSizeAndAlpha {


    static void decoratePopUp(Activity ac, Dialog dialog) {

        Log.d ("d","PopUpSizeAndAlpha.decoratePopUp called");
        Log.d("d","whattodo="+FullscreenActivity.whattodo);
        try {
            // Get available width and height
            DisplayMetrics metrics = new DisplayMetrics();
            ac.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int height = metrics.heightPixels;
            int width = metrics.widthPixels;

            Log.d("d","width="+width);
            Log.d("d","height="+height);
            float myscale;
            float myalpha;
            float mydim;
            String position;

            switch (FullscreenActivity.whattodo) {
                case "showtheset":
                    myscale = FullscreenActivity.popupScale_Set;
                    myalpha = FullscreenActivity.popupAlpha_Set;
                    mydim = FullscreenActivity.popupDim_Set;
                    position = FullscreenActivity.popupPosition_Set;
                    break;

                default:
                case "default":
                    myscale = FullscreenActivity.popupScale_All;
                    myalpha = FullscreenActivity.popupAlpha_All;
                    mydim = FullscreenActivity.popupDim_All;
                    position = FullscreenActivity.popupPosition_All;
            }

            int desired_width = (int) ((float) width * myscale);
            int desired_height = (int) ((float) height * myscale);

            Log.d("d","myscale="+myscale);

            Window dw = dialog.getWindow();
            Log.d("d","dw="+dw);
            Log.d("d","desired_width="+desired_width);
            Log.d("d","desired_height="+desired_height);
            if (dw != null) {
                dw.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dw.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog.getWindow().setLayout(desired_width, desired_height);
                WindowManager.LayoutParams lp = dw.getAttributes();
                lp.alpha = myalpha;
                lp.dimAmount = mydim;
                dw.setAttributes(lp);
                switch (position) {
                    case "tl":
                        dw.setGravity(Gravity.TOP|Gravity.LEFT);
                        break;

                    case "tc":
                        dw.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
                        break;

                    case "tr":
                        dw.setGravity(Gravity.TOP|Gravity.RIGHT);
                        break;

                    case "l":
                        dw.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
                        break;

                    default:
                    case "c":
                        dw.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
                        break;

                    case "r":
                        dw.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
                        break;

                    case "bl":
                        dw.setGravity(Gravity.BOTTOM|Gravity.LEFT);
                        break;

                    case "bc":
                        dw.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
                        break;

                    case "br":
                        dw.setGravity(Gravity.BOTTOM|Gravity.RIGHT);
                        break;

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

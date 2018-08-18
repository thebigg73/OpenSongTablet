package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

class PopUpSizeAndAlpha {

    static void decoratePopUp(Activity ac, Dialog dialog) {

        if (ac!=null && dialog!=null) {
            try {
                // Get available width and height
                DisplayMetrics metrics = new DisplayMetrics();
                ac.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                int height = metrics.heightPixels;
                int width = metrics.widthPixels;

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

                    case "chordie":
                    case "ultimate-guitar":
                    case "worshipready":
                    case "editsong":
                        myscale = 1.0f;
                        myalpha = FullscreenActivity.popupAlpha_All;
                        mydim = FullscreenActivity.popupDim_All;
                        position = "C";
                        break;

                    case "drawnotes":
                        myscale = 1.0f;
                        myalpha = 1.0f;
                        mydim = 1.0f;
                        position = "C";
                        break;

                    case "abcnotation":
                    case "abcnotation_edit":
                        myscale = 1.0f;
                        myalpha = FullscreenActivity.popupAlpha_All;
                        mydim = FullscreenActivity.popupDim_All;
                        position = "C";
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

                Window dw = dialog.getWindow();
                if (dw != null) {
                    dw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dw.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    dw.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    dialog.getWindow().setLayout(desired_width, desired_height);
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    dw.setBackgroundDrawable(ac.getDrawable(R.drawable.popup_bg));
                } else {
                    dw.setBackgroundDrawable(ac.getResources().getDrawable(R.drawable.popup_bg));
                }*/
                    WindowManager.LayoutParams lp = dw.getAttributes();
                    lp.alpha = myalpha;
                    lp.dimAmount = mydim;
                    dw.setAttributes(lp);
                    switch (position) {
                        case "tl":
                            dw.setGravity(Gravity.TOP | Gravity.START);
                            break;

                        case "tc":
                            dw.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                            break;

                        case "tr":
                            dw.setGravity(Gravity.TOP | Gravity.END);
                            break;

                        case "l":
                            dw.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                            break;

                        default:
                        case "c":
                            dw.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                            break;

                        case "r":
                            dw.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                            break;

                        case "bl":
                            dw.setGravity(Gravity.BOTTOM | Gravity.START);
                            break;

                        case "bc":
                            dw.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                            break;

                        case "br":
                            dw.setGravity(Gravity.BOTTOM | Gravity.END);
                            break;
                    }
                    //setWindowFlags(dw, dw.getDecorView());
                    //setWindowFlagsAdvanced(dw, dw.getDecorView());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Not using the bits below as it messed with the soft keyboard overlapping everything!

    /*private static void setWindowFlags(Window w, View v) {
        //v.setOnSystemUiVisibilityChangeListener(null);
        //v.setOnFocusChangeListener(null);
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }*/

    /*private static void setWindowFlagsAdvanced(final Window w, final View v) {
        v.setOnSystemUiVisibilityChangeListener(null);
        v.setOnFocusChangeListener(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        Runnable testnavbar = () -> {
            //v.setOnSystemUiVisibilityChangeListener(visibility -> restoreTransparentBars(w, v));

            //v.setOnFocusChangeListener((v1, hasFocus) -> restoreTransparentBars(w, v1));
        };

        Handler waitandtest = new Handler();
        waitandtest.postDelayed(testnavbar, 1000);
    }*/

    /*private static void restoreTransparentBars(final Window w, final View v) {
        // Set runnable
        Runnable delhide = () -> {
            // Hide them
            setWindowFlags(w,v);
            setWindowFlagsAdvanced(w,v);
            View rf = w.getCurrentFocus();
            if (rf!=null) {
                rf.clearFocus();
            }
        };

        // Wait for 1000ms then check for Navigation bar visibility
        // If it is there, hide it
        Handler delayhidehandler = new Handler();
        delayhidehandler.postDelayed(delhide, 1000);
    }*/

}

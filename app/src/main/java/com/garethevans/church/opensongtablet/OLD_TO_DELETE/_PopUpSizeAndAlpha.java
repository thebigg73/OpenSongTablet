/*
package com.garethevans.church.opensongtablet.OLD_TO_DELETE;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.garethevans.church.opensongtablet._Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

public class _PopUpSizeAndAlpha {

    public static void decoratePopUp(Activity ac, Dialog dialog, _Preferences preferences) {

        if (ac!=null && dialog!=null) {
            try {
                // Get available width and height
                DisplayMetrics metrics = new DisplayMetrics();
                ac.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                int height = metrics.heightPixels;
                int width = metrics.widthPixels;

                float myalpha = preferences.getMyPreferenceFloat(ac,"popupAlpha",0.8f);
                float myscale = preferences.getMyPreferenceFloat(ac,"popupScale",0.7f);
                float mydim = preferences.getMyPreferenceFloat(ac,"popupDim",0.8f);
                String position = preferences.getMyPreferenceString(ac,"popupPosition","c");

                // Override some of the defaults if required
                switch (StaticVariables.whattodo) {
                    case "chordie":
                    case "ultimate-guitar":
                    case "worshipready":
                    case "editsong":
                    case "rebuildindex":
                    case "drawnotes":
                    case "abcnotation":
                    case "abcnotation_edit":
                        myscale = 1.0f;
                        myalpha = 1.0f;
                        mydim = 1.0f;
                        position = "c";
                        break;
                }

                int desired_width = (int) ((float) width * myscale);
                int desired_height = (int) ((float) height * myscale);

                Window dw = dialog.getWindow();
                if (dw != null) {
                    dw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dw.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    dw.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    dialog.getWindow().setLayout(desired_width, desired_height);

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

}
*/

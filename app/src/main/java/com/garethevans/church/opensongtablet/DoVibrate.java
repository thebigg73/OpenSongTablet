package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.os.Vibrator;

class DoVibrate {
    static void vibrate(Context c, int i) {
        Vibrator vb = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        if (vb != null) {
            vb.vibrate(i);
        }
    }
}
package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Vibrator;

public class DoVibrate {

    public void vibrate(Context c, int i) {
        Vibrator vb = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        if (vb!=null) {
            vb.vibrate(i);
        }
    }
}

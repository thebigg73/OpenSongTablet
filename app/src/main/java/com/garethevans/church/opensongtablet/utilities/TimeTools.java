package com.garethevans.church.opensongtablet.utilities;

import android.view.View;
import android.widget.TextClock;

public class TimeTools {
    public String timeFormatFixer(int secstime) {
        if (secstime<0) {
            return ("0:00");
        } else {
            double mins_float = (float) secstime / 60.0;
            int mins = (int) mins_float;
            int secs = secstime - (mins * 60);
            String time;
            if (secs < 10) {
                time = mins + ":0" + secs;
            } else {
                time = mins + ":" + secs;
            }
            return time;
        }
    }

    public void setFormat(TextClock textClock, float textSize, boolean visible,
                          boolean is24hr, boolean showSeconds) {
        // This deals with the actionbar and presentation clock formatting in one place.
        textClock.post(() -> {
            // Should the clock be shown?
            if (visible) {
                textClock.setVisibility(View.VISIBLE);
            } else {
                textClock.setVisibility(View.GONE);
            }

            // Set the text size
            textClock.setTextSize(textSize);

            // Set the formatting as 12hr or 24hr.  Because it will use our system settings (12 or 24hr)
            // we have to override both formats to the one the user has chosen.
            CharSequence charSequence;
            if (is24hr) {
                charSequence = "HH:mm";
            } else {
                charSequence = "h:mm";
            }
            if (showSeconds) {
                charSequence += ":ss";
            }
            textClock.setFormat12Hour(charSequence);
            textClock.setFormat24Hour(charSequence);
        });
    }
}

package com.garethevans.church.opensongtablet.tools;

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
}

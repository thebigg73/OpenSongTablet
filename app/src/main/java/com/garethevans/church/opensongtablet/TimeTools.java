package com.garethevans.church.opensongtablet;

class TimeTools {

    static String timeFormatFixer(int secstime) {
        // IV - Fixed to prevent returning 0:0-1 for -1
        // IV - Using 20 mins as the limit (possibly the original intention)
        if (secstime > 1200 || secstime < 0) {
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

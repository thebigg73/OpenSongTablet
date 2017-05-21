package com.garethevans.church.opensongtablet;

class TimeTools {

    static String timeFormatFixer(int secstime) {
        double mins_float = (float)secstime/60.0;
        int mins = (int) mins_float;
        int secs = secstime - (mins*60);
        String time;
        if (secs<10) {
            time = mins + ":0" + secs;
        } else {
            time = mins + ":" + secs;
        }
        return time;
    }

}

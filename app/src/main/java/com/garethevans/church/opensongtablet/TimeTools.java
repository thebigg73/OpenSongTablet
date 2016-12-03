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

    static String getPadPosition() {
        int pos = 0;
        if (FullscreenActivity.pad1Playing && FullscreenActivity.whichPad==1) {
            pos = (int) (FullscreenActivity.mPlayer1.getCurrentPosition() / 1000.0f);
        } else if (FullscreenActivity.pad2Playing && FullscreenActivity.whichPad==2) {
            pos = (int) (FullscreenActivity.mPlayer2.getCurrentPosition() / 1000.0f);
        }
        return TimeTools.timeFormatFixer(pos);
    }

    public static Object[] getAutoscrollPosition() {
        Object[] vals = new Object[2];
        FullscreenActivity.time_passed = System.currentTimeMillis();
        int currtimesecs = (int) ((FullscreenActivity.time_passed - FullscreenActivity.time_start)/1000);
        if (currtimesecs<0) {
            currtimesecs = 0;
        }
        vals[0] = TimeTools.timeFormatFixer(currtimesecs);
        if (currtimesecs<FullscreenActivity.autoScrollDelay) {
            vals[1] = 0xffff0000;
        } else {
            vals[1] = 0xffffffff;
        }
        return vals;
    }
}

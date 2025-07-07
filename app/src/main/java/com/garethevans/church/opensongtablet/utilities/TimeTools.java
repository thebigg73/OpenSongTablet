package com.garethevans.church.opensongtablet.utilities;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.TextClock;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;

public class TimeTools {

    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "TimeTools";
    private final MainActivityInterface mainActivityInterface;

    public TimeTools(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }

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

    public int[] getMinsSecsFromSecs(int totalsecs) {
        String time = timeFormatFixer(totalsecs);
        String[] splitTime = time.split(":");
        int[] timeVals = new int[2];
        timeVals[0] = Integer.parseInt(splitTime[0]);
        timeVals[1] = Integer.parseInt(splitTime[1]);

        return timeVals;
    }

    public int totalSecs(int min, int sec) {
        return (min*60) + sec;
    }

    public int totalSecs(int hours, int mins, int secs) {
        return (hours*60*60) + (mins*60) + secs;
    }

    public void setFormat(TextClock textClock, boolean settingsOpen, float textSize, boolean visible,
                          boolean is24hr, boolean showSeconds) {
        // This deals with the actionbar and presentation clock formatting in one place.
        textClock.post(() -> {
            // Should the clock be shown?
            textClock.setVisibility(visible && !settingsOpen ? View.VISIBLE: View.GONE);

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

    public String getTimeStampFromMills(Locale locale, long millis) {
        long seconds = Math.round((float)millis/1000f);
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format(locale,"%02d:%02d:%02d", h,m,s);
    }

    public String getDateFromMillis(Locale locale, long millis) {
        DateFormat formatter = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.DEFAULT, SimpleDateFormat.SHORT, locale);
        return formatter.format(new Date(millis));
    }

    public int getTotalSecsFromColonTimes(String time) {
        int hours = 0;
        int mins = 0;
        int secs = 0;
        if (time==null || time.isEmpty()) {
            return 0;
        } else {
            String[] splitTime = time.split(":");

            if (splitTime.length == 1) {
                secs = Integer.parseInt(splitTime[0]);
            } else if (splitTime.length == 2) {
                mins = Integer.parseInt(splitTime[0]);
                secs = Integer.parseInt(splitTime[1]);
            } else if (splitTime.length == 3) {
                hours = Integer.parseInt(splitTime[0]);
                mins = Integer.parseInt(splitTime[1]);
                secs = Integer.parseInt(splitTime[2]);
            }
            return totalSecs(hours, mins, secs);
        }
    }

    public String getNowIsoTime() {
        // return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.UK).format(new Date());
        return Instant.ofEpochMilli(System.currentTimeMillis()).toString();
    }

    public long getMillisFromIsoTime(String isoTimeStamp) {
        long millis = 0;
        if (isoTimeStamp != null && !isoTimeStamp.isEmpty()) {
            try {
            millis = OffsetDateTime.parse(isoTimeStamp).toInstant().toEpochMilli();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return millis;
    }
    public String getIsoTimeFromMillis(long millis) {
        return Instant.ofEpochMilli(millis).toString();
    }

    public String getIsoTimeFromSongFileMetadata(Song thisSong) {
        return getIsoTimeFromFileMetadata("Songs", thisSong.getFolder(), thisSong.getFilename());
    }
    public String getIsoTimeFromFileMetadata(String folder, String subfolder, String file) {
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(folder, subfolder, file);
        long millis;
        if (mainActivityInterface.getStorageAccess().uriExists(uri)) {
            millis = mainActivityInterface.getStorageAccess().getLastModifiedDate(uri);
        } else {
            millis = 0;
        }
        return String.valueOf(Instant.ofEpochSecond(millis / 1000));
    }
}

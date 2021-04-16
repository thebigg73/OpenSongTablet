package com.garethevans.church.opensongtablet.screensetup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.BatteryManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BatteryStatus extends BroadcastReceiver {

    private boolean isCharging;

    public interface MyInterface {
        void setUpBatteryMonitor();
    }

    public void setUpBatteryMonitor(Context c, MainActivityInterface mainActivityInterface, TextView digitalclock,
                                    TextView batterycharge, ImageView batteryimage, ActionBar ab) {
        // Get clock
        try {
            // Get clock
            updateClock(mainActivityInterface,digitalclock,
                    mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"clockTextSize",9.0f),
                    mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"clock24hFormat",true),
                    mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"clockOn",true));

            // Get battery
            int i = (int) (getBatteryStatus(c) * 100.0f);
            String charge = i + "%";
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"batteryTextOn",true)) {
                batterycharge.setVisibility(View.VISIBLE);
            } else {
                batterycharge.setVisibility(View.GONE);
            }
            batterycharge.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "batteryTextSize",9.0f));
            batterycharge.setText(charge);
            int abh = ab.getHeight();
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"batteryDialOn",true)) {
                batteryimage.setVisibility(View.VISIBLE);
            } else {
                batteryimage.setVisibility(View.INVISIBLE);
            }
            if (abh > 0) {
                setBatteryImage(c,batteryimage,abh,i,mainActivityInterface.getPreferences().getMyPreferenceInt(c,"batteryDialThickness",4));
            }

            // Ask the app to check again in 60s
            Handler batterycheck = new Handler();
            batterycheck.postDelayed(() -> setUpBatteryMonitor(c,mainActivityInterface,digitalclock,batterycharge,batteryimage,ab), 60000);
        } catch (Exception e) {
            // Ooops
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            try {
                MyInterface mListener = (MyInterface) context;
                mListener.setUpBatteryMonitor();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("BatteryMonitor", "Problem setting up the battery monitor");
            }
        }
    }

    public float getBatteryStatus (Context context) {

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level / (float) scale;

            // Are we charging / charged?
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            return batteryPct;

        } else {
            return 0;
        }
    }

    public void setBatteryImage(Context c, ImageView batteryImage, int abheight, int charge, int thickness) {
        BitmapDrawable bmp = batteryImage(c, abheight, charge,thickness);
        batteryImage.setImageDrawable(bmp);
    }

    public BitmapDrawable batteryImage(Context c, int abheight, int charge, int thickness) {

        int size = (int)(abheight*0.75f);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(size,size, conf);

        BitmapDrawable drawable = new BitmapDrawable(c.getResources(), bmp);

        // If less than 15% battery, draw the circle in red
        int color = 0xffffffff;
        if (charge>10 && charge<16) {
            color = 0xffff6600;
        } else if (charge<=10) {
            color = 0xffff0000;
        }

        int bgcolor = 0xff666666;
        if (isCharging) {
            bgcolor = 0xff88ff88;
        }

        Paint bPaint = new Paint();
        bPaint.setDither(true);
        bPaint.setColor(bgcolor);
        bPaint.setAntiAlias(true);
        bPaint.setStyle(Paint.Style.STROKE);
        bPaint.setStrokeJoin(Paint.Join.ROUND);
        bPaint.setStrokeCap(Paint.Cap.ROUND);
        bPaint.setStrokeWidth(thickness);

        Paint mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setColor(color);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(thickness);

        Path circle = new Path();
        RectF box = new RectF(thickness,thickness,size-thickness,size-thickness);
        float sweep = 360 * charge * 0.01f;
        circle.addArc(box, 270, sweep);

        Path circle2 = new Path();
        RectF box2 = new RectF(thickness,thickness,size-thickness,size-thickness);
        float sweep2 = 360;
        circle2.addArc(box2, 270, sweep2);

        Canvas canvas = new Canvas(bmp);
        canvas.drawPath(circle2, bPaint);
        canvas.drawPath(circle, mPaint);

        return drawable;
    }

    public void updateClock(MainActivityInterface mainActivityInterface, TextView digitalclock, float textsize, boolean clockon, boolean is24h) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df;
        if (is24h) {
            df = new SimpleDateFormat("HH:mm", mainActivityInterface.getLocale());
        } else {
            df = new SimpleDateFormat("h:mm", mainActivityInterface.getLocale());
        }
        String formattedTime = df.format(cal.getTime());
        if (clockon) {
            digitalclock.setVisibility(View.VISIBLE);
        } else {
            digitalclock.setVisibility(View.GONE);
        }
        digitalclock.setTextSize(textsize);
        digitalclock.setText(formattedTime);
    }
}

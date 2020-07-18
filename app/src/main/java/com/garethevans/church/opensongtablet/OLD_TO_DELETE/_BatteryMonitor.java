/*
package com.garethevans.church.opensongtablet.OLD_TO_DELETE;

import android.annotation.SuppressLint;
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
import android.util.Log;

import com.garethevans.church.opensongtablet.FullscreenActivity;
import com.garethevans.church.opensongtablet._Preferences;

public class _BatteryMonitor extends BroadcastReceiver {

    private static boolean isCharging;

    public interface MyInterface {
        void setUpBatteryMonitor();
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent!=null) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            if (FullscreenActivity.mContext == null) {
                FullscreenActivity.mContext = context;
            }

            if (FullscreenActivity.mContext != null) {
                try {
                    MyInterface mListener = (MyInterface) FullscreenActivity.mContext;
                    mListener.setUpBatteryMonitor();
                } catch (Exception e) {
                    Log.d("BatteryMonitor", "Problem setting up the battery monitor");
                }
            }
        }
    }

    public static float getBatteryStatus (Context context) {

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

    public static BitmapDrawable batteryImage(Context c, _Preferences preferences, int charge, int abheight) {

        int size = (int)(abheight*0.75f);
        int thickness = preferences.getMyPreferenceInt(c,"batteryDialThickness",4);
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
}*/

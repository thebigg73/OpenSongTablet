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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class BatteryStatus extends BroadcastReceiver {

    private boolean isCharging;

    private float batteryTextSize, charge;
    private int batteryDialThickness;
    private int actionBarHeight = 0;
    private boolean batteryTextOn, batteryDialOn;
    private final ActionBar actionBar;
    private final TextView batteryCharge;
    private final ImageView batteryImage;
    private final Context c;
    private final MainActivityInterface mainActivityInterface;

    public interface MyInterface {
        void setUpBatteryMonitor();
    }

    public BatteryStatus(Context c, ImageView batteryImage, TextView batteryCharge, ActionBar actionBar) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        this.batteryImage = batteryImage;
        this.batteryCharge = batteryCharge;
        this.actionBar = actionBar;
    }

    public void setUpBatteryMonitor() {
        // Get the initial preferences
        updateBatteryPrefs();

        // Set up the intent
        IntentFilter intentFiler = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        c.registerReceiver(this, intentFiler);

        // Get the initial battery charge and set values and colour
        getBatteryStatus();
    }


    public void updateBatteryPrefs() {
        setBatteryTextOn(mainActivityInterface.getPreferences().getMyPreferenceBoolean("batteryTextOn",true));
        setBatteryTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("batteryTextSize",9.0f));
        setBatteryDialOn(mainActivityInterface.getPreferences().getMyPreferenceBoolean("batteryDialOn",true));
        setBatteryDialThickness(mainActivityInterface.getPreferences().getMyPreferenceInt("batteryDialThickness", 4));
    }

    public void setBatteryDialOn(boolean batteryDialOn) {
        this.batteryDialOn = batteryDialOn;
        if (batteryDialOn) {
            batteryImage.setVisibility(View.VISIBLE);
        } else {
            batteryImage.setVisibility(View.GONE);
        }
    }

    public void setBatteryDialThickness(int batteryDialThickness) {
        this.batteryDialThickness = batteryDialThickness;
        setBatteryImage();
    }
    public void setBatteryTextOn(boolean batteryTextOn) {
        this.batteryTextOn = batteryTextOn;
        if (batteryTextOn) {
            batteryCharge.setVisibility(View.VISIBLE);
        } else {
            batteryCharge.setVisibility(View.GONE);
        }
    }
    public void setBatteryTextSize(float batteryTextSize) {
        this.batteryTextSize = batteryTextSize;
        batteryCharge.setTextSize(batteryTextSize);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            charge = level / (float) scale;

            // Are we charging / charged?
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            try {
                getBatteryStatus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getBatteryStatus () {
        if (batteryTextOn && batteryCharge != null) {
            batteryCharge.post(() -> {
                int i = Math.round(charge * 100.0f);
                String chargeText = i + "%";

                batteryCharge.setTextSize(batteryTextSize);
                batteryCharge.setText(chargeText);
            });
        }

        // Get the image
        if (batteryCharge != null && actionBar != null) {
            if (batteryDialOn) {
                actionBarHeight = actionBar.getHeight();
                if (actionBarHeight > 0) {
                    batteryImage.post(this::setBatteryImage);
                }
            }
        }
    }

    public void setBatteryImage() {
        BitmapDrawable bmp = batteryImage((int) (charge * 100f));
        batteryImage.setImageDrawable(bmp);
    }

    public BitmapDrawable batteryImage(int charge) {

        int size = (int)(actionBarHeight*0.75f);
        if (size>0) {
            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Bitmap bmp = Bitmap.createBitmap(size, size, conf);

            BitmapDrawable drawable = new BitmapDrawable(c.getResources(), bmp);

            // If less than 15% battery, draw the circle in red
            int color = 0xffffffff;
            if (charge > 10 && charge < 16) {
                color = 0xffff6600;
            } else if (charge <= 10) {
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
            bPaint.setStrokeWidth(batteryDialThickness);

            Paint mPaint = new Paint();
            mPaint.setDither(true);
            mPaint.setColor(color);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(batteryDialThickness);

            Path circle = new Path();
            RectF box = new RectF(batteryDialThickness, batteryDialThickness,
                    size - batteryDialThickness, size - batteryDialThickness);
            float sweep = 360 * charge * 0.01f;
            circle.addArc(box, 270, sweep);

            Path circle2 = new Path();
            RectF box2 = new RectF(batteryDialThickness, batteryDialThickness,
                    size - batteryDialThickness, size - batteryDialThickness);
            float sweep2 = 360;
            circle2.addArc(box2, 270, sweep2);

            Canvas canvas = new Canvas(bmp);
            canvas.drawPath(circle2, bPaint);
            canvas.drawPath(circle, mPaint);

            return drawable;
        } else {
            return null;
        }
    }


}

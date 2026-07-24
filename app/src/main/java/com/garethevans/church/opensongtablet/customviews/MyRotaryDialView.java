package com.garethevans.church.opensongtablet.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.webkit.internal.ApiFeature;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.screensetup.Palette;

import java.util.ArrayList;

public class MyRotaryDialView extends LinearLayout {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyRotaryDialView";
    private Palette palette;
    private final float dimmedAlphaValue = 0.6f;
    // Listener interface for value changes
    public interface OnValueChangedListener {
        void onValueChanged(int value);
    }

    private OnValueChangedListener valueChangedListener;

    // Configurable bounds and state
    private int valueFrom = 0;
    private int valueTo = 100;
    private int currentValue = 0;
    private boolean showValuesAsText = true;
    private ArrayList<String> valuesAsText = new ArrayList<>();
    private String suffixText = "";
    private boolean tapTempoEnabled = false;
    private boolean alphaDimmed = false;

    // Interaction tracking
    private float angle = 110f;
    private final float minAngle = 110f;   // 7 o'clock
    private final float maxAngle = 430f;    // 5 o'clock

    // UI Sub-components
    private MyMaterialSimpleTextView labelTextView;
    private DialCanvasView dialCanvasView;
    private MyMaterialSimpleTextView valueTextView, btnPlus, btnMinus;

    public MyRotaryDialView(@NonNull Context context) {
        super(context);
        init(context,null);
    }

    public MyRotaryDialView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public MyRotaryDialView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        palette = new Palette(context);

        String text = "";       // The label for the dial
        int size = 1;           // The size of the dial: enum 0=mini, 1=normal, 3=large, 4=xlarge

        // Get any XML attributes
        if (attrs!=null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyRotaryDialView);
            try {
                text = a.getString(R.styleable.MyRotaryDialView_android_text);
                size = a.getInt(R.styleable.MyRotaryDialView_dialSize, 1);
                valueFrom = (int) a.getFloat(R.styleable.MyRotaryDialView_android_valueFrom, 0);
                valueTo = (int) a.getFloat(R.styleable.MyRotaryDialView_android_valueTo, 127);
                currentValue = (int) a.getFloat(R.styleable.MyRotaryDialView_android_value, 0);
                showValuesAsText = a.getBoolean(R.styleable.MyRotaryDialView_showValuesAsText,false);
                suffixText = a.getString(R.styleable.MyRotaryDialView_suffixText);
                if (suffixText == null || suffixText.isEmpty()) {
                    suffixText = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                a.recycle();
            }
        }

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);

        float density = context.getResources().getDisplayMetrics().density;

        // 1. Top Row: Label TextView (spans full width)
        labelTextView = new MyMaterialSimpleTextView(context);
        labelTextView.setGravity(Gravity.CENTER);
        labelTextView.setTextSize(getLabelTextSize(size));
        labelTextView.setText(text);

        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        labelParams.bottomMargin = (int) (4 * density);
        labelTextView.setLayoutParams(labelParams);
        labelTextView.setOnClickListener(view -> {
            performDialClick(); // Force trigger click action
        });
        addView(labelTextView);

        // 2. Middle Row: Rotary Dial Canvas View
        int sizeInPx = (int) (getDialSize(size) * density);
        dialCanvasView = new DialCanvasView(context);
        LinearLayout.LayoutParams dialParams = new LinearLayout.LayoutParams(
                sizeInPx,
                sizeInPx
        );
        dialParams.gravity = Gravity.CENTER_HORIZONTAL;
        dialParams.bottomMargin = (int) (4 * density);
        dialCanvasView.setLayoutParams(dialParams);
        setCurrentValue(currentValue);
        addView(dialCanvasView);

        // 3. Bottom Row: Horizontal layout for [-] [Value] [+]
        LinearLayout controlRow = new LinearLayout(context);
        controlRow.setOrientation(LinearLayout.HORIZONTAL);
        controlRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        controlRow.setLayoutParams(rowParams);

        // Decrement Button (-)
        btnMinus = new MyMaterialSimpleTextView(context);
        btnMinus.setTextSize(getLabelTextSize(size));
        btnMinus.setGravity(Gravity.CENTER);btnMinus.setText(" - ");
        btnMinus.setPadding(4,4,4,4);
        btnMinus.setOnClickListener(v -> {
            setCurrentValue(Math.max(valueFrom,(currentValue - 1)));
            performDialClick(); // Force trigger click action
        });
        controlRow.addView(btnMinus, new LinearLayout.LayoutParams((int)(40 * density), (int)(40 * density)));

        // Value Display TextView
        String textValue;
        valueTextView = new MyMaterialSimpleTextView(context);
        if (showValuesAsText) {
            textValue = getTextFromValue(currentValue) + suffixText;
        } else {
            textValue = currentValue + suffixText;
        }
        valueTextView.setText(textValue);
        valueTextView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                (int)(48 * density),
                LayoutParams.WRAP_CONTENT
        );
        valueTextView.setLayoutParams(textParams);
        valueTextView.setOnClickListener(view -> {
            performDialClick(); // Force trigger click action
        });
        controlRow.addView(valueTextView);

        // Increment Button (+)
        btnPlus = new MyMaterialSimpleTextView(context);
        btnPlus.setGravity(Gravity.CENTER);
        btnPlus.setTextSize(getLabelTextSize(size));
        btnPlus.setText(" + ");
        btnPlus.setPadding(4,4,4,4);
        btnPlus.setOnClickListener(v -> {
            setCurrentValue(Math.min(valueTo,(currentValue + 1)));
            performDialClick(); // Force trigger click action
        });
        controlRow.addView(btnPlus, new LinearLayout.LayoutParams((int)(40 * density), (int)(40 * density)));

        // General click action to fire the current value
        View.OnClickListener standardClickListener = v -> {
            performDialClick();
        };

        // Attach to components so clicking anywhere on the control surface responds
        labelTextView.setOnClickListener(v -> {
            performDialClick();
            // If you want tap tempo to also trigger on label, you can call recordTap() here if needed
        });

        dialCanvasView.setClickable(true);
        dialCanvasView.setOnClickListener(v -> {
            // If it's a tempo dial utilizing tap tempo, recordTap() handles value changes.
            // Otherwise, we fall back to standard click.
            performDialClick();
        });

        valueTextView.setOnClickListener(standardClickListener);

        // Add control row to main vertical layout
        addView(controlRow);
    }

    /**
     * Triggers the dial click listener with the current value.
     */
    public void performDialClick() {
        if (dialClickListener != null) {
            dialClickListener.onDialClick(currentValue);
        }
    }

    // Listener interface for general clicks/taps returning the current value
    public interface OnDialClickListener {
        void onDialClick(int currentValue);
    }

    private OnDialClickListener dialClickListener;

    public void setOnDialClickListener(OnDialClickListener listener) {
        Log.d(TAG,"setOnDialClickListener:"+listener);
        this.dialClickListener = listener;
    }

    public void performDialLongClick() {
        Log.d(TAG,"performDialLongClick()   dialLongClickListener:"+dialLongClickListener);
        if (dialLongClickListener != null) {
            dialLongClickListener.onDialLongClick(alphaDimmed);
        }
    }
    public interface OnDialLongClickListener {
        void onDialLongClick(boolean alphaDimmed);
    }
    private OnDialLongClickListener dialLongClickListener;

    public void setOnDialLongClickListener(OnDialLongClickListener listener) {
        Log.d(TAG,"setOnDialClickListener:"+listener);
        this.dialLongClickListener = listener;
    }

    public void setOnValueChangedListener(OnValueChangedListener listener) {
        this.valueChangedListener = listener;
    }

    public void setValueFrom(int valueFrom) {
        this.valueFrom = valueFrom;
        setCurrentValue(currentValue); // Ensure constraints hold
    }

    public void setValueTo(int valueTo) {
        this.valueTo = valueTo;
        setCurrentValue(currentValue); // Ensure constraints hold
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = Math.max(valueFrom, Math.min(valueTo, currentValue));

        // Update text view representation
        String textValue;
        if (showValuesAsText) {
            textValue = getTextFromValue(currentValue) + suffixText;
        } else {
            textValue = currentValue + suffixText;
        }
        if (valueTextView!=null) {
            valueTextView.setText(textValue);
        }

        // Update visual angle mapping based on value change
        float totalSpan = maxAngle - minAngle;
        float percentage = (float) (currentValue - valueFrom) / (valueTo - valueFrom);
        angle = minAngle + (percentage * totalSpan);

        if (dialCanvasView != null) {
            dialCanvasView.invalidate();
        }

        if (valueChangedListener != null) {
            valueChangedListener.onValueChanged(this.currentValue);
        }
    }

    private float getLabelTextSize(int size) {
        // Size can be 0-3 which corresponds to mini, normal, large, xlarge
        switch (size) {
            case 0:     // mini
                return 12f;
            case 2:     // large
                return 18f;
            case 3:     // xlarge
                return 22f;
            case 1:     // normal
            default:
                return 14f;
        }
    }

    private int getDialSize(int size) {
        // Size can be 0-3 which corresponds to mini, normal, large, xlarge
        switch (size) {
            case 0:  // mini
                return 48;
            case 2:  // large
                return 96;
            case 3:  // xlarge
                return 128;
            case 1: // normal
            default:
                return 64;
        }
    }
    // Inner class handling the custom canvas drawing and touch events for the dial itself
    private class DialCanvasView extends View {
        private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rectF = new RectF();

        public DialCanvasView(Context context) {
            super(context);
            initPaints();
        }

        public void setBackgroundColor(int color) {
            backgroundPaint.setColor(color);
            invalidate();
        }

        public void setIndicatorColor(int color) {
            indicatorPaint.setColor(color);
            invalidate();
        }

        public void setBorderColor(int color) {
            borderPaint.setColor(color);
            invalidate();
        }

        private void initPaints() {
            backgroundPaint.setStyle(Paint.Style.FILL);
            backgroundPaint.setColor(palette.secondary);

            indicatorPaint.setStyle(Paint.Style.STROKE);
            indicatorPaint.setStrokeWidth(8f);
            indicatorPaint.setStrokeCap(Paint.Cap.ROUND);
            indicatorPaint.setColor(palette.textColor);

            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(6f);
            borderPaint.setColor(palette.primary);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            float padding = 20f;
            float size = Math.min(w, h) - (padding * 2f);
            float left = (w - size) / 2f;
            float top = (h - size) / 2f;
            rectF.set(left, top, left + size, top + size);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float radius = (Math.min(getWidth(), getHeight()) / 2f) - 15f;

            if (radius <= 0) return;

            canvas.drawCircle(cx, cy, radius, backgroundPaint);
            canvas.drawCircle(cx, cy, radius, borderPaint);

            double radian = Math.toRadians(angle);
            float innerRadius = radius * 0.55f;
            float outerRadius = radius * 0.85f;

            float startX = cx + (float) (innerRadius * Math.cos(radian));
            float startY = cy + (float) (innerRadius * Math.sin(radian));
            float stopX = cx + (float) (outerRadius * Math.cos(radian));
            float stopY = cy + (float) (outerRadius * Math.sin(radian));

            canvas.drawLine(startX, startY, stopX, stopY, indicatorPaint);
        }

        private float downX, downY;
        private static final float TAP_TOLERANCE = 10f; // Maximum pixels a finger can move to still be considered a tap

        private Handler longPressHandler = new Handler(Looper.getMainLooper());
        private boolean isLongClickTriggered = false;
        private Runnable longPressRunnable = () -> {
            isLongClickTriggered = true;

            // Perform haptic feedback so the user feels the long-press register
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

            // Trigger your exclusion logic here
            toggelAlphaDimmed();
            performDialLongClick();
        };

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getX();
                    downY = event.getY();
                    isLongClickTriggered = false;

                    // Start the 500ms timer for a long press
                    longPressHandler.postDelayed(longPressRunnable, 500);

                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float currentDx = Math.abs(event.getX() - downX);
                    float currentDy = Math.abs(event.getY() - downY);

                    // If the user drags their finger past the tolerance threshold,
                    // cancel the long-press so it doesn't trigger while they are turning the dial!
                    if (currentDx > TAP_TOLERANCE || currentDy > TAP_TOLERANCE) {
                        longPressHandler.removeCallbacks(longPressRunnable);
                    }

                    // Only rotate while dragging around the dial
                    updateAngleFromTouch(event.getX(), event.getY());
                    return true;

                case MotionEvent.ACTION_UP:
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }

                    // Always clear the long-press timer when lifting the finger
                    longPressHandler.removeCallbacks(longPressRunnable);

                    // If the long press DID NOT trigger, handle normal taps/drags
                    if (!isLongClickTriggered) {
                        float dx = Math.abs(event.getX() - downX);
                        float dy = Math.abs(event.getY() - downY);
                        if (dx < TAP_TOLERANCE && dy < TAP_TOLERANCE) {
                            recordTap();
                        }
                        performDialClick();
                    }
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    longPressHandler.removeCallbacks(longPressRunnable);
                    return true;
            }
            return super.onTouchEvent(event);
        }

        private void updateAngleFromTouch(float touchX, float touchY) {
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;

            float x = touchX - cx;
            float y = touchY - cy;
            float degrees = (float) Math.toDegrees(Math.atan2(y, x));
            degrees = (degrees + 360f) % 360f;

            // Use your helper function for dead-zones and bounds
            angle = normalizeToRange(degrees);

            float totalSpan = maxAngle - minAngle;
            if (totalSpan > 0) {
                float currentSpan = angle - minAngle;
                float percentage = currentSpan / totalSpan;
                setCurrentValue((int) (valueFrom + (percentage * (valueTo - valueFrom))));
            }
        }
    }



    /**
     * Maps raw touch degrees (0 to 360) to your custom sweep range (minAngle to maxAngle),
     * handling the dead zone at the bottom automatically.
     */
    private float normalizeToRange(float rawDegrees) {
        // 1. If minAngle or maxAngle crosses the 360/0 degree mark, we normalize everything relative to minAngle.
        // Let's bring rawDegrees into a continuous frame starting from (minAngle - 180) to (minAngle + 180).
        float targetAngle = rawDegrees;

        // If the angle falls into the dead zone between maxAngle and minAngle, snap it to the closest boundary.
        // Since maxAngle can be > 360, let's normalize both bounds to 0-360 for dead-zone comparison.
        float normalizedMin = (minAngle % 360f + 360f) % 360f;
        float normalizedMax = (maxAngle % 360f + 360f) % 360f;

        // Check if our sweep crosses the 0/360 boundary (e.g., min=110, max=430 -> wraps past 360)
        boolean crossesZero = maxAngle >= 360f;

        if (crossesZero) {
            // If rawDegrees is in the lower range (e.g., 0 to 70), treat it as if it's > 360 (e.g., 370)
            if (rawDegrees <= normalizedMax) {
                targetAngle = rawDegrees + 360f;
            }

            // Define the dead zone (the gap between maxAngle and minAngle)
            // In your current case: min=110, max=70(+360). The dead zone is from 70 to 110.
            if (rawDegrees > normalizedMax && rawDegrees < normalizedMin) {
                float distToMax = Math.abs(rawDegrees - normalizedMax);
                float distToMin = Math.abs(rawDegrees - normalizedMin);
                targetAngle = (distToMax < distToMin) ? maxAngle : minAngle; // Snap to closer edge
            }
        } else {
            // Standard non-wrapping range case
            if (rawDegrees < normalizedMin) targetAngle = minAngle;
            if (rawDegrees > normalizedMax) targetAngle = maxAngle;
        }

        return Math.max(minAngle, Math.min(maxAngle, targetAngle));
    }

    public void setText(CharSequence text) {
        if (labelTextView != null) {
            labelTextView.setText(text);
        }
    }

    public String getText() {
        return (labelTextView!=null && labelTextView.getText()!=null) ? labelTextView.getText().toString() : "";
    }

    public void setShowValuesAsText(boolean showValuesAsText) {
        this.showValuesAsText = showValuesAsText;
        setCurrentValue(currentValue);
    }

    public boolean getShowValuesAsText() {
        return showValuesAsText;
    }

    public String getTextFromValue(int value) {
        // Check we have those values!
        if (valuesAsText!=null && valuesAsText.size()>value) {
            return valuesAsText.get(value);
        } else {
            return String.valueOf(value);
        }
    }

    public void setTextValues(ArrayList<String> valuesAsText) {
        this.valuesAsText = valuesAsText;
        // Adjust the max values based on this
        valueTo = valuesAsText.size();
        valueFrom = 0;
        // Check this is in range
        setCurrentValue(currentValue);
    }

    public String getCurrentTextValue() {
        if (valuesAsText!=null && valuesAsText.size()>currentValue) {
            return valuesAsText.get(currentValue);
        } else {
            return null;
        }

    }

    public void setPalette(Palette palette) {
        this.palette = palette;
        labelTextView.setPalette(palette);
        valueTextView.setPalette(palette);
        if (dialCanvasView != null) {
            dialCanvasView.setIndicatorColor(palette.textColor);
            dialCanvasView.setBackgroundColor(palette.secondary);
            dialCanvasView.setBorderColor(palette.primary);
        }
        btnPlus.setPalette(palette);
        btnMinus.setPalette(palette);
    }

    // Tap Tempo tracking interface
    public interface OnTapTempoListener {
        void onTapTempo(int calculatedBpm);
    }

    private OnTapTempoListener tapTempoListener;
    private long lastTapTime = 0;
    private final ArrayList<Long> tapIntervals = new ArrayList<>();
    private final long TAP_TIMEOUT_MS = 2000; // Reset if 2 seconds pass between taps

    private final android.os.Handler tapHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable resetTapRunnable = () -> tapIntervals.clear();

    public void setOnTapTempoListener(OnTapTempoListener listener) {
        this.tapTempoListener = listener;
    }

    /**
     * Call this to record a tap, calculate average BPM based on interval, and update value.
     */
    public void recordTap() {
        if (!tapTempoEnabled) {
            tapIntervals.clear();
            return; // Exit early if tap tempo is turned off for this dial
        }

        long currentTime = System.currentTimeMillis();

        // If too much time has passed since the last tap, clear history
        if (currentTime - lastTapTime > TAP_TIMEOUT_MS) {
            tapIntervals.clear();
        }

        if (lastTapTime != 0) {
            long interval = currentTime - lastTapTime;
            tapIntervals.add(interval);

            // Keep only the last 4 intervals for smooth averaging
            if (tapIntervals.size() > 4) {
                tapIntervals.remove(0);
            }

            // Calculate average interval in milliseconds
            long sum = 0;
            for (long intervalVal : tapIntervals) {
                sum += intervalVal;
            }
            long avgInterval = sum / tapIntervals.size();

            // Convert interval to BPM: 60,000 ms per minute / avg interval
            if (avgInterval > 0) {
                int calculatedBpm = (int) (60000 / avgInterval);

                // Constrain the BPM to the dial's allowed range
                int finalBpm = Math.max(valueFrom, Math.min(valueTo, calculatedBpm));

                // Update the dial view position/text
                setCurrentValue(finalBpm);

                // Always notify the listener with the clamped value
                if (tapTempoListener != null) {
                    tapTempoListener.onTapTempo(finalBpm);
                }
            }
        }

        lastTapTime = currentTime;

        // Reset timeout timer
        tapHandler.removeCallbacks(resetTapRunnable);
        tapHandler.postDelayed(resetTapRunnable, TAP_TIMEOUT_MS);
    }

    public void setTapTempoEnabled(boolean tapTempoEnabled) {
        this.tapTempoEnabled = tapTempoEnabled;
    }
    public boolean getTapTempoEnabled() {
        return tapTempoEnabled;
    }

    public void toggelAlphaDimmed() {
        alphaDimmed = !alphaDimmed;
        setAlpha(alphaDimmed ? dimmedAlphaValue : 1.0f);
    }
    public boolean getAlphaDimmed() {
        return alphaDimmed;
    }

    public void setAlphaDimmed(boolean alphaDimmed) {
        this.alphaDimmed = alphaDimmed;
        setAlpha(alphaDimmed ? dimmedAlphaValue : 1.0f);
    }
}
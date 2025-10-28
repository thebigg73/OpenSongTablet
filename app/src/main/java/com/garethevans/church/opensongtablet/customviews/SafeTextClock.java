package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextClock;

/**
 * A safer version of TextClock that prevents IntentReceiver leaks and
 * automatically handles re-attachment when the window regains visibility.

 * It behaves identically to TextClock for all normal UI usage,
 * but avoids the "WindowContext ... has leaked IntentReceiver" crash.
 */
public class SafeTextClock extends TextClock {

    private final String TAG = "SafeTextClock";
    private boolean detached = false;

    public SafeTextClock(Context context) {
        super(context);
    }

    public SafeTextClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SafeTextClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        detached = false;
        try {
            super.onAttachedToWindow();
        } catch (Exception e) {
            Log.w(TAG, "Attach failed (possibly already registered)", e);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        detached = true;
        try {
            super.onDetachedFromWindow();
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Receiver already unregistered — ignoring", e);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);

        if (visibility == VISIBLE) {
            // App or window became visible again — re-attach the receiver
            if (detached) {
                try {
                    super.onAttachedToWindow();
                    detached = false;
                } catch (Exception e) {
                    Log.w(TAG, "Re-attach failed", e);
                }
            }
        } else {
            // App or window hidden — detach safely
            if (!detached) {
                try {
                    super.onDetachedFromWindow();
                    detached = true;
                } catch (Exception e) {
                    Log.w(TAG, "Cleanup skipped: " + e.getMessage());
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        // Final safety net in case GC happens before explicit detach
        try {
            if (!detached) {
                Log.w(TAG, "Finalize before detach — cleaning up receiver");
                super.onDetachedFromWindow();
            }
        } catch (Exception ignore) {
        } finally {
            super.finalize();
        }
    }
}
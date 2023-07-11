package com.garethevans.church.opensongtablet.animation;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyFAB;

public class CustomAnimation {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "CustomAnimation";

    public void faderAnimation(View v, int time, float startAlpha, float endAlpha) {
        if (v!=null) {
            // Remove any current animations/animation listeners
            // This means it was in the middle of an animation
            if (v.animate() != null) {
                v.animate().cancel();
            }

            // IV - Animate the view to the end alpha
            if (endAlpha>startAlpha) {
                // Fade up - set the initial alpha and make visible so we see the fade in animation
                v.setAlpha(startAlpha);
                v.setVisibility(View.VISIBLE);
                v.animate().alpha(endAlpha).setDuration(time).setInterpolator(new LinearInterpolator()).start();
            } else {
                // Fade down - always from the current alpha
                v.animate().alpha(endAlpha).setDuration(time/2).setInterpolator(new LinearInterpolator()).start();
            }
        }
        // IV - Panic removed as causing issues
        // The use of separate views for fadeIn and fadeOut might make panic unecessary
        // Testing if OK
    }

    public void fadeActionButton(MyFAB fab, float fadeTo) {
        new Handler().postDelayed(() -> {
            fab.setAlpha(1.0f);
            fab.animate().alpha(fadeTo).setDuration(800);
            },400);
    }

    public void pulse(Context c, View v) {
        v.startAnimation(AnimationUtils.loadAnimation(c, R.anim.pulse));
    }

}

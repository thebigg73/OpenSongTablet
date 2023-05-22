package com.garethevans.church.opensongtablet.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

            int finalVisibility;
            boolean fadeIn = endAlpha>startAlpha;

            v.setAlpha(startAlpha);
            v.setVisibility(View.VISIBLE);
            if (fadeIn) {
                finalVisibility = View.VISIBLE;
            } else {
                finalVisibility = View.GONE;
            }
            AnimatorListenerAdapter animatorListenerAdapter;
            animatorListenerAdapter = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    v.setVisibility(finalVisibility);
                }
            };

            // For a correct fade out, the view should already be in the faded in state:
            // - The initial visibility should already be VISIBLE
            // - The alpha should already be 1f (completely faded in), but certainly more than 0;
            boolean fadeOut = (!fadeIn && (v.getVisibility()==View.VISIBLE) && v.getAlpha() > 0) || endAlpha<startAlpha;

            // If either of these are true, we can animate, but if not, just move to the final state

            if (fadeIn) {
                // Good to go - set the initial alpha and visibility to VISIBLE so we see the animation
                //v.setAlpha(startAlpha);
                //v.setVisibility(View.VISIBLE);

                // Animate the content view to the end alpha
                // For fade out, the final step is also to add the listener to change visibility to GONE at the end
                //v.animate().alpha(endAlpha).setDuration(time).setInterpolator(new AccelerateInterpolator()).setListener(animatorListenerAdapter).start();
                //v.animate().alpha(endAlpha).setDuration(time).setInterpolator(new LinearInterpolator()).setListener(animatorListenerAdapter).start();
                v.animate().alpha(endAlpha).setDuration(time).setInterpolator(new LinearInterpolator()).setListener(animatorListenerAdapter).start();

            } else if (fadeOut) {
                v.animate().alpha(endAlpha).setDuration(time/2).setInterpolator(new LinearInterpolator()).setListener(animatorListenerAdapter).start();
            } else {
                // Just set the alpha and visibility as the end (without animation)
                v.setAlpha(endAlpha);
                v.setVisibility(finalVisibility);
            }

            // Set a panic for a short time after animation end
            final Runnable runnable = () -> {
                if (v.getAlpha()!=endAlpha) {
                    v.setAlpha(endAlpha);
                }
                if (v.getVisibility()!=finalVisibility) {
                    v.setVisibility(finalVisibility);
                }
            };

            try {
                v.removeCallbacks(runnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
            v.postDelayed(runnable,(int)(time));
            // GE Additional command to panic check
            v.postDelayed(runnable,(int)(time+500));
        }
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

package com.garethevans.church.opensongtablet.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CustomAnimation {

    private final String TAG = "CustomAnimation";

    public void faderAnimation(final View v, int time, float startAlpha, final float endAlpha) {
        if (v!=null) {
            int finalVisibility;
            AnimatorListenerAdapter animatorListenerAdapter;
            if (endAlpha==0) {
                finalVisibility = View.GONE;
                animatorListenerAdapter = new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        v.setVisibility(View.GONE);
                    }
                };
            } else {
                finalVisibility = View.VISIBLE;
                animatorListenerAdapter = null;
            }

            boolean fadeIn = endAlpha>startAlpha;

            // Remove any current animations/animation listeners
            // This means it was in the middle of an animation
            // Get the current alpha as our new start position
            if (v.animate() != null) {
                startAlpha = v.getAlpha();
                v.animate().cancel();
            }

            // For a correct fade in, the view should already be in the faded out state:
            // - The initial visibility should already be GONE or INVISIBLE
            // - ideally alpha 0 (completely faded out), but certainly less than 1 (partially faded out)
            boolean fadeInOk = fadeIn && (v.getVisibility()==View.GONE || v.getVisibility()==View.INVISIBLE) && v.getAlpha() < 1;

            // For a correct fade out, the view should already be in the faded in state:
            // - The initial visibility should already be VISIBLE
            // - The alpha should already be 1f (completely faded in), but certainly more than 0;
            boolean fadeOutOk = !fadeIn && (v.getVisibility()==View.VISIBLE) && v.getAlpha() > 0;

            // If either of these are true, we can animate, but if not, just move to the final state
            if (fadeInOk || fadeOutOk) {
                // Good to go - set the initial alpha and visibility to VISIBLE so we see the animation
                v.setAlpha(startAlpha);
                v.setVisibility(View.VISIBLE);

                // Animate the content view to the end alpha
                // For fade out, the final step is also to add the listener to change visibility to GONE at the end
                v.animate().alpha(endAlpha).setDuration(time).setListener(animatorListenerAdapter).start();

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
        }
    }

    public void fadeActionButton(FloatingActionButton fab, float fadeTo) {
        new Handler().postDelayed(() -> {
            fab.setAlpha(1.0f);
            fab.animate().alpha(fadeTo).setDuration(800);
            },400);
    }

    public void pulse(Context c, View v) {
        v.startAnimation(AnimationUtils.loadAnimation(c, R.anim.pulse));
    }

}

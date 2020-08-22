package com.garethevans.church.opensongtablet;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

class CustomAnimations {

    // The FAB animation on press
    static void animateFAB(final FloatingActionButton fab, final Context c) {
        Handler h1 = new Handler();
        h1.post(() -> {
            try {
                fab.startAnimation(AnimationUtils.loadAnimation(c, R.anim.fabdown));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Handler h2 = new Handler();
        h2.postDelayed(() -> {
            try {
                fab.startAnimation(AnimationUtils.loadAnimation(c, R.anim.fabup));
            } catch (Exception e) {
                e.printStackTrace();
            }
        },100);
    }

    static void animateFABLong(final FloatingActionButton fab, final Context c) {
        Handler h1 = new Handler();
        h1.post(() -> {
            try {
                fab.startAnimation(AnimationUtils.loadAnimation(c, R.anim.fabdownlong));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Handler h2 = new Handler();
        h2.postDelayed(() -> {
            try {
                fab.startAnimation(AnimationUtils.loadAnimation(c, R.anim.fabuplong));
            } catch (Exception e) {
                e.printStackTrace();
            }
        },100);
    }

    static void animateSwipe(final ImageView img, final int distance, final int velocity) {
        Handler h1 = new Handler();
        h1.post(() -> {
            try {
                ScaleAnimation grow = new ScaleAnimation(0.0f, 1.0f, 1, 1, 0.0f, 0.0f);
                int duration = (int) (((float)distance / (float)velocity) *1000);
                grow.setDuration(duration);
                grow.setRepeatMode(2);
                grow.setRepeatCount(1);
                img.startAnimation(grow);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static class MyAnimationListener implements Animation.AnimationListener {

        final View v;
        final float start;
        final float end;
        final int startvis;
        final int endvis;

        MyAnimationListener (View which, float a, float b) {
            v = which;
            start = a;
            end = b;
            if (a>b) {
                // Fading out
                startvis = View.VISIBLE;
                endvis = View.INVISIBLE;
            } else {
                // Fading in
                startvis = View.VISIBLE;
                endvis = View.VISIBLE;
            }
        }

        @Override
        public void onAnimationStart(Animation animation) {
            v.setAlpha(start);
            v.setVisibility(startvis);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            v.setAlpha(end);
            v.setVisibility(endvis);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    static void faderAnimation(final View v, int time, boolean fadeIn) {
        float startAlpha;
        float endAlpha;
        final int startVisibility;
        final int endVisibility;

        if (fadeIn) {
            startAlpha = 0f;
            endAlpha = 1f;
            startVisibility = View.VISIBLE;
            endVisibility = View.VISIBLE;
        } else {
            startAlpha = 1f;
            endAlpha = 0f;
            startVisibility = View.VISIBLE;
            endVisibility = View.GONE;
        }
        AnimatorSet mAnimationSet = new AnimatorSet();

        final ObjectAnimator fadeView = ObjectAnimator.ofFloat(v, View.ALPHA,  startAlpha, endAlpha);

        fadeView.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                v.setVisibility(startVisibility);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(endVisibility);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        //fadeView.setInterpolator(new LinearInterpolator());
        fadeView.setInterpolator(new AccelerateInterpolator());

        mAnimationSet.setDuration(time);
        mAnimationSet.play(fadeView);
        mAnimationSet.start();
    }

    static void faderAnimationCustomAlpha(final View v, int time, float startAlpha, float endAlpha) {
        final int startVisibility;
        final int endVisibility;

        if (endAlpha>=startAlpha) {
            startVisibility = View.VISIBLE;
            endVisibility = View.VISIBLE;
        } else {
            startVisibility = View.VISIBLE;
            endVisibility = View.GONE;
        }
        AnimatorSet mAnimationSet = new AnimatorSet();

        final ObjectAnimator fadeView = ObjectAnimator.ofFloat(v, View.ALPHA,  startAlpha, endAlpha);

        fadeView.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                v.setVisibility(startVisibility);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(endVisibility);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        fadeView.setInterpolator(new AccelerateInterpolator());

        mAnimationSet.setDuration(time);
        mAnimationSet.play(fadeView);
        mAnimationSet.start();
    }

    static AlphaAnimation setUpAnimation(View v, int presoTransitionTime, float start, float end) {
        AlphaAnimation aa = new AlphaAnimation(start,end);
        if (start>end) {
            // Fade out
            aa.setInterpolator(new AccelerateInterpolator());
        } else {
            // Fade in
            aa.setInterpolator(new DecelerateInterpolator());
        }
        aa.setDuration(presoTransitionTime);
        aa.setAnimationListener(new CustomAnimations.MyAnimationListener(v,start,end));
        return aa;
    }

    static void highlightAction(View v, Context c) {
        try {
            v.startAnimation(AnimationUtils.loadAnimation(c, R.anim.highlight));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void pulse(Context c, View v) {
        v.startAnimation(AnimationUtils.loadAnimation(c, R.anim.pulse));
    }
}

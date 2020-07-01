package com.garethevans.church.opensongtablet.animation;

import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;

import com.garethevans.church.opensongtablet.R;

public class CustomAnimation {

    public void faderAnimation(final View v, int time, boolean fadeIn) {
        int start = 1;
        int end = 0;
        if (fadeIn) {
            start = 0;
            end = 1;
        }
        Animation fader = new AlphaAnimation(start, end);

        if (fadeIn) {
            fader.setInterpolator(new DecelerateInterpolator()); //add this
        } else {
            fader.setInterpolator(new AccelerateInterpolator()); //and this
        }
        fader.setDuration(time);


        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fader);
        v.setAnimation(animation);
    }

    public void pulse(Context c, View v) {
        v.startAnimation(AnimationUtils.loadAnimation(c, R.anim.pulse));
    }
}

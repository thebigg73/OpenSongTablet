package com.garethevans.church.opensongtablet.animation;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CustomAnimation {

    private final String TAG = "CustomAnimation";

    public void faderAnimation(View v, int time, boolean fadeIn) {
        float start = 1;
        float end = 0;
        if (fadeIn) {
            start = 0;
            end = 1;
        }
        v.setVisibility(View.VISIBLE);

        Animation fader = new AlphaAnimation(start, end);
        fader.setInterpolator(new LinearInterpolator());
        fader.setDuration(time);
        
        Log.d(TAG,"v="+v.getId()+"  start="+start+"  end="+end);

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fader);
        animation.setFillAfter(true);

        v.startAnimation(animation);

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

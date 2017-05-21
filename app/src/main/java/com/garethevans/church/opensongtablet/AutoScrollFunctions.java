package com.garethevans.church.opensongtablet;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ScrollView;
import android.widget.TextView;

class AutoScrollFunctions {

    static void getAutoScrollTimes() {
        // Set the autoscroll values
        try {
            FullscreenActivity.autoScrollDuration = Integer.parseInt(FullscreenActivity.mDuration.replaceAll("[\\D]", ""));
        } catch (Exception e) {
            FullscreenActivity.autoScrollDuration = -1;
        }

        try {
            FullscreenActivity.autoScrollDelay = Integer.parseInt(FullscreenActivity.mPreDelay.replaceAll("[\\D]", ""));
        } catch (Exception e) {
            FullscreenActivity.autoScrollDelay = -1;
        }
        FullscreenActivity.usingdefaults = false;
        if (FullscreenActivity.mDuration.isEmpty() && FullscreenActivity.autoscroll_default_or_prompt.equals("default")) {
            FullscreenActivity.autoScrollDuration = FullscreenActivity.default_autoscroll_songlength;
            FullscreenActivity.usingdefaults = true;
        }

        if (FullscreenActivity.mPreDelay.isEmpty() && FullscreenActivity.autoscroll_default_or_prompt.equals("default")) {
            FullscreenActivity.autoScrollDelay = FullscreenActivity.default_autoscroll_predelay;
            FullscreenActivity.usingdefaults = true;
        }
    }

    static void getAutoScrollValues(ScrollView scrollpage, View main_page, View toolbar) {
        // Get the autoScrollDuration;
        if (FullscreenActivity.mDuration.isEmpty() && FullscreenActivity.autoscroll_default_or_prompt.equals("default")) {
            FullscreenActivity.autoScrollDuration = FullscreenActivity.default_autoscroll_songlength;
        } else if (FullscreenActivity.mDuration.isEmpty() && FullscreenActivity.autoscroll_default_or_prompt.equals("prompt")) {
            FullscreenActivity.autoScrollDuration = -1;
        } else {
            try {
                FullscreenActivity.autoScrollDuration = Integer.parseInt(FullscreenActivity.mDuration.replaceAll("[\\D]", ""));
            } catch (Exception e) {
                FullscreenActivity.autoScrollDuration = 0;
            }
        }

        // Get the autoScrollDelay;
        if (FullscreenActivity.mPreDelay.isEmpty() && FullscreenActivity.autoscroll_default_or_prompt.equals("default")) {
            FullscreenActivity.autoScrollDelay = FullscreenActivity.default_autoscroll_predelay;
        } else if (FullscreenActivity.mDuration.isEmpty() && FullscreenActivity.autoscroll_default_or_prompt.equals("prompt")) {
            FullscreenActivity.autoScrollDelay = -1;
        } else {
            try {
                FullscreenActivity.autoScrollDelay = Integer.parseInt(FullscreenActivity.mPreDelay.replaceAll("[\\D]", ""));
            } catch (Exception e) {
                FullscreenActivity.autoScrollDelay = 0;
            }
        }

        if (FullscreenActivity.autoScrollDuration > -1 && FullscreenActivity.autoScrollDelay > -1) {
            // If it duration is less than the predelay, stop!
            if (FullscreenActivity.autoScrollDuration < FullscreenActivity.autoScrollDelay) {
                FullscreenActivity.isautoscrolling = false;
                return;
            } else {
                // Remove the autoScrollDelay
                FullscreenActivity.autoScrollDuration = FullscreenActivity.autoScrollDuration - FullscreenActivity.autoScrollDelay;
            }

            // Ok figure out the size of amount of scrolling needed
            int height = (scrollpage.getChildAt(0).getMeasuredHeight() - (main_page.getHeight() - toolbar.getHeight()));
            if (height >= scrollpage.getScrollY()) {
                FullscreenActivity.total_pixels_to_scroll = height;
            } else {
                FullscreenActivity.total_pixels_to_scroll = 0;
            }

            // Ok how many pixels per 500ms - autoscroll_pause_time
            FullscreenActivity.autoscroll_pixels = ((float) FullscreenActivity.total_pixels_to_scroll /
                    ((float) FullscreenActivity.autoScrollDuration * 1000 / (float) FullscreenActivity.autoscroll_pause_time));

            FullscreenActivity.newPosFloat=0.0f;
            FullscreenActivity.autoScrollDuration = FullscreenActivity.autoScrollDuration + FullscreenActivity.autoScrollDelay;
        }
    }

    static void getAudioLength(Context c) {
        MediaPlayer mediafile = new MediaPlayer();
        if (FullscreenActivity.mLinkAudio!=null && !FullscreenActivity.mLinkAudio.equals("")) {
            try {
                mediafile.setDataSource(c, Uri.parse(FullscreenActivity.mLinkAudio));
                mediafile.prepare();
                FullscreenActivity.audiolength = (int) (mediafile.getDuration() / 1000.0f);
                mediafile.reset();
                mediafile.release();
            } catch (Exception e) {
                FullscreenActivity.audiolength = -1;
                mediafile.reset();
                mediafile.release();
            }
        } else {
            FullscreenActivity.audiolength=-1;
            mediafile.reset();
            mediafile.release();
        }
    }

    static Handler doautoScroll = new Handler();
    static class AutoScrollRunnable implements Runnable {
        ScrollView sv;
        AutoScrollRunnable(ScrollView s) {
            sv = s;
        }

        @Override
        public void run() {
            ObjectAnimator animator;

            animator = ObjectAnimator.ofInt(sv, "scrollY", sv.getScrollY(), (int) FullscreenActivity.newPosFloat);
            Interpolator linearInterpolator = new LinearInterpolator();
            animator.setInterpolator(linearInterpolator);
            animator.setDuration(FullscreenActivity.autoscroll_pause_time);
            if (!FullscreenActivity.isManualDragging) {
                animator.start();
            }
        }
    }

    static Handler doProgressTime = new Handler();
    static class ProgressTimeRunnable implements Runnable {
        TextView tv;
        ProgressTimeRunnable(TextView t) {
            tv = t;
        }

        @Override
        public void run() {
            FullscreenActivity.time_passed = System.currentTimeMillis();
            int currtimesecs = (int) ((FullscreenActivity.time_passed - FullscreenActivity.time_start)/1000);
            String text = TimeTools.timeFormatFixer(currtimesecs);
            if (currtimesecs<FullscreenActivity.autoScrollDelay) {
                tv.setTextColor(0xffff0000);
                tv.setText(text);
            } else {
                tv.setTextColor(0xffffffff);
                tv.setText(text);
            }
        }
    }
}

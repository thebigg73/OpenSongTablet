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
            FullscreenActivity.autoScrollDelay = 0;
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
            int height;
            try {
                height = (scrollpage.getChildAt(0).getMeasuredHeight() - (main_page.getHeight() - toolbar.getHeight()));
            } catch (Exception e) {
                height = 0;
            }
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

        // Try to fix the mLinkAudio file to get the metadata.
        // **RAGE** android security permissions messing with file access......
        String audiofile = FullscreenActivity.mLinkAudio;
        // Strip out the file locator
        if (audiofile.startsWith("file://")) {
            audiofile = audiofile.replace("file://","");
        }
        // If this is a localised file, we need to unlocalise it to enable it to be read
        if (audiofile.startsWith("../OpenSong/")) {
            audiofile = audiofile.replace("../OpenSong/",FullscreenActivity.homedir+"/");
        }
        // Add the file locator back in
        audiofile = "file://" + audiofile;

        if (FullscreenActivity.mLinkAudio!=null && !FullscreenActivity.mLinkAudio.equals("")) {
            try {
                mediafile.setDataSource(c, Uri.parse(audiofile));
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
            final ObjectAnimator animator;
            animator = ObjectAnimator.ofInt(sv, "scrollY", (int) FullscreenActivity.newPosFloat);
            Interpolator linearInterpolator = new LinearInterpolator();
            animator.setInterpolator(linearInterpolator);
            animator.setDuration(FullscreenActivity.autoscroll_pause_time);
            if (!FullscreenActivity.isManualDragging) {
                sv.post(new Runnable() {
                    @Override
                    public void run() {
                        animator.start();
                    }
                });
            }
        }
    }

    static Handler doProgressTime = new Handler();
    static class ProgressTimeRunnable implements Runnable {
        TextView tv;
        TextView tvt;
        TextView tvs;
        ProgressTimeRunnable(TextView t, TextView tt, TextView ts) {
            tv = t;
            tvt = tt;
            tvs = ts;
        }

        @Override
        public void run() {
            if (FullscreenActivity.isautoscrolling) {
                FullscreenActivity.time_passed = System.currentTimeMillis();
                int currtimesecs = (int) ((FullscreenActivity.time_passed - FullscreenActivity.time_start) / 1000);
                String text;
                tv.setTextSize(FullscreenActivity.timerFontSizeAutoScroll);
                tvt.setTextSize(FullscreenActivity.timerFontSizeAutoScroll);
                tvs.setTextSize(FullscreenActivity.timerFontSizeAutoScroll);
                if (currtimesecs < FullscreenActivity.autoScrollDelay) {
                    // Set the time as a backwards count down
                    currtimesecs = FullscreenActivity.autoScrollDelay - currtimesecs;
                    text = TimeTools.timeFormatFixer(currtimesecs);
                    tv.setTextColor(0xffff0000);
                    tv.setText(text);
                } else {
                    text = TimeTools.timeFormatFixer(currtimesecs);
                    tv.setTextColor(0xffffffff);
                    tv.setText(text);
                }
            }
        }
    }
}

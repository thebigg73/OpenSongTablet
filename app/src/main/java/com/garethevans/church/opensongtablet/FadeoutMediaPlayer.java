package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.os.AsyncTask;

class FadeoutMediaPlayer extends AsyncTask<Object,Void,String> {

    Context c;
    int which;
    FadeoutMediaPlayer(Context context, int player) {
        this.c = context;
        this.which = player;
    }

    @Override
    protected String doInBackground(Object... objects) {
        float level1 = FullscreenActivity.padvol;
        int i = 1;
        while (i < 50) {
            i++;
            level1 = level1 * 0.9f;
            float leftVol1 = level1;
            float rightVol1 = level1;
            if (FullscreenActivity.padpan.equals("left")) {
                rightVol1 = 0.0f;
            } else if (FullscreenActivity.padpan.equals("right")) {
                leftVol1 = 0.0f;
            }
            try {
                if (which==1 && FullscreenActivity.mPlayer1!=null) {
                    FullscreenActivity.mPlayer1.setVolume(leftVol1, rightVol1);
                } else if (which==2 && FullscreenActivity.mPlayer2!=null) {
                    FullscreenActivity.mPlayer2.setVolume(leftVol1, rightVol1);
                }
            } catch (Exception e) {
                // Problem!
                e.printStackTrace();
            }

            // Pause before next fade increment
            long nowtime = System.currentTimeMillis();
            long thentime = nowtime + FullscreenActivity.crossFadeTime / 50;
            while (System.currentTimeMillis() < thentime) {
                // Do nothing......
                System.currentTimeMillis();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            if (which==1 && FullscreenActivity.mPlayer1!=null) {
                PadFunctions.getPad1Status();
                if (FullscreenActivity.pad1Playing) {
                    FullscreenActivity.mPlayer1.stop();
                    FullscreenActivity.mPlayer1.reset();
                }
                FullscreenActivity.pad1Playing = false;
                FullscreenActivity.pad1Fading = false;

            } else if (which==2 && FullscreenActivity.mPlayer2!=null) {
                PadFunctions.getPad2Status();
                if (FullscreenActivity.pad2Playing) {
                    FullscreenActivity.mPlayer2.stop();
                    FullscreenActivity.mPlayer2.reset();
                }
                FullscreenActivity.pad2Playing = false;
                FullscreenActivity.pad2Fading = false;

            } else if (which==1) {
                FullscreenActivity.pad1Playing = false;
                FullscreenActivity.pad1Fading = false;
            } else if (which==2) {
                FullscreenActivity.pad2Playing = false;
                FullscreenActivity.pad2Fading = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
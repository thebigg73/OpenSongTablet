package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

class FadeoutMediaPlayer extends AsyncTask<Object,Void,String> {

    @SuppressLint("StaticFieldLeak")
    Context c;
    int which;
    private String pan;
    private float vol;

    FadeoutMediaPlayer(Context context, String padpan, float padvol, int player) {
        this.c = context;
        this.which = player;
        pan = padpan;
        vol = padvol;
    }

    @Override
    protected String doInBackground(Object... objects) {
        float level1 = vol;
        int i = 1;
        while (i < 50) {
            i++;
            level1 = level1 * 0.9f;
            float leftVol1 = level1;
            float rightVol1 = level1;
            if (pan.equals("L")) {
                rightVol1 = 0.0f;
            } else if (pan.equals("R")) {
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
                if (StaticVariables.pad1Playing) {
                    FullscreenActivity.mPlayer1.stop();
                    FullscreenActivity.mPlayer1.reset();
                }
                StaticVariables.pad1Playing = false;
                StaticVariables.pad1Fading = false;

            } else if (which==2 && FullscreenActivity.mPlayer2!=null) {
                PadFunctions.getPad2Status();
                if (StaticVariables.pad2Playing) {
                    FullscreenActivity.mPlayer2.stop();
                    FullscreenActivity.mPlayer2.reset();
                }
                StaticVariables.pad2Playing = false;
                StaticVariables.pad2Fading = false;

            } else if (which==1) {
                StaticVariables.pad1Playing = false;
                StaticVariables.pad1Fading = false;
            } else if (which==2) {
                StaticVariables.pad2Playing = false;
                StaticVariables.pad2Fading = false;
            }

            if (which==0) {
                StaticVariables.pad1Playing = false;
                StaticVariables.pad1Fading = false;
                StaticVariables.pad2Playing = false;
                StaticVariables.pad2Fading = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
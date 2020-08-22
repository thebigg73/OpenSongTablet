package com.garethevans.church.opensongtablet;

import android.os.AsyncTask;

class FadeoutMediaPlayer extends AsyncTask<Object,Void,String> {

    private final int which;
    private final String pan;
    private final float vol;
    private final int time;

    FadeoutMediaPlayer(String padpan, float padvol, int player, int fadetime) {
        this.which = player;
        pan = padpan;
        vol = padvol;
        time = fadetime;
    }

    @Override
    protected String doInBackground(Object... objects) {
        float level1 = vol;
        int i = 1;

        // Prevent double running of fades
        if ((which == 1 && !StaticVariables.pad1Fading) || (which == 2 && !StaticVariables.pad2Fading)) {
            int fadetime;
            while (i < 50) {
                i = i + 1;
                if (i == 50) {
                    level1 = 0.0f;
                } else {
                    level1 *= 0.90f;
                }

                // If both pads are fading decide on the quietest and quick fade it
                if (StaticVariables.pad1Fading && StaticVariables.pad2Fading) {
                    // padInQuickFade flags the pad being quick faded and ensures only it is quick faded
                    if (StaticVariables.padInQuickFade == 0) {
                        if (StaticVariables.pad1FadeVolume < StaticVariables.pad2FadeVolume) {
                            StaticVariables.padInQuickFade = 1;
                        } else {
                            StaticVariables.padInQuickFade = 2;
                        }
                    }
                }
                // If quickfade is running it can be stopped by external set of padInQuickFade to 0
                if (StaticVariables.padInQuickFade == which) {
                    fadetime = 2000;
                } else {
                    fadetime = time;
                }

                float leftVol1 = level1;
                float rightVol1 = level1;
                if (pan.equals("L")) {
                    rightVol1 = 0.0f;
                } else if (pan.equals("R")) {
                    leftVol1 = 0.0f;
                }

                try {
                    if (which == 1) {
                        StaticVariables.pad1Fading = true;
                        StaticVariables.pad1FadeVolume = level1;
                        FullscreenActivity.mPlayer1.setVolume(leftVol1, rightVol1);
                    } else {
                        StaticVariables.pad2Fading = true;
                        StaticVariables.pad2FadeVolume = level1;
                        FullscreenActivity.mPlayer2.setVolume(leftVol1, rightVol1);
                    }
                } catch (Exception e) {
                        // Problem!
                        e.printStackTrace();
                }

                // Pause before next fade increment
                long nowtime = System.currentTimeMillis();
                long thentime = nowtime + (fadetime / 50);
                while (nowtime < thentime) {
                      nowtime = System.currentTimeMillis();
                }
            }

            // At the end of a fade stop and reset the player and cancel fading flags
            try {
                if (which == 1) {
                    FullscreenActivity.mPlayer1.stop();
                    FullscreenActivity.mPlayer1.reset();
                    StaticVariables.pad1Fading = false;
                    StaticVariables.pad1Playing = false;
                } else {
                    FullscreenActivity.mPlayer2.stop();
                    FullscreenActivity.mPlayer2.reset();
                    StaticVariables.pad2Fading = false;
                    StaticVariables.pad2Playing = false;
                }
             } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
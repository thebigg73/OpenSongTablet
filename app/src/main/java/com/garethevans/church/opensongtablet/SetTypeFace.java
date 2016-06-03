package com.garethevans.church.opensongtablet;

import android.app.Activity;

public class SetTypeFace extends Activity {

    public static void setTypeface() {
        switch (FullscreenActivity.mylyricsfontnum) {
            case 1:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface1;
                FullscreenActivity.commentfont = FullscreenActivity.typeface1;
                break;
            case 2:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface2;
                FullscreenActivity.commentfont = FullscreenActivity.typeface2;
                break;
            case 3:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface3;
                FullscreenActivity.commentfont = FullscreenActivity.typeface3;
                break;
            case 4:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface4;
                FullscreenActivity.commentfont = FullscreenActivity.typeface4i;
                break;
            case 5:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface5;
                FullscreenActivity.commentfont = FullscreenActivity.typeface5i;
                break;
            case 6:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface6;
                FullscreenActivity.commentfont = FullscreenActivity.typeface6;
                break;
            case 7:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface7;
                FullscreenActivity.commentfont = FullscreenActivity.typeface7i;
                break;
            case 8:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface8;
                FullscreenActivity.commentfont = FullscreenActivity.typeface8i;
                break;
            case 9:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface9;
                FullscreenActivity.commentfont = FullscreenActivity.typeface9i;
                break;
            default:
                FullscreenActivity.lyricsfont = FullscreenActivity.typeface0;
                FullscreenActivity.commentfont = FullscreenActivity.typeface0;
                break;
        }

        switch (FullscreenActivity.mychordsfontnum) {
            case 1:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface1;
                break;
            case 2:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface2;
                break;
            case 3:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface3;
                break;
            case 4:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface4;
                break;
            case 5:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface5;
                break;
            case 6:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface6;
                break;
            case 7:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface7;
                break;
            case 8:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface8;
                break;
            case 9:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface9;
                break;
            default:
                FullscreenActivity.chordsfont = FullscreenActivity.typeface0;
                break;
        }

        switch (FullscreenActivity.mypresofontnum) {
            case 1:
                FullscreenActivity.presofont = FullscreenActivity.typeface1;
                break;
            case 2:
                FullscreenActivity.presofont = FullscreenActivity.typeface2;
                break;
            case 3:
                FullscreenActivity.presofont = FullscreenActivity.typeface3;
                break;
            case 4:
                FullscreenActivity.presofont = FullscreenActivity.typeface4;
                break;
            case 5:
                FullscreenActivity.presofont = FullscreenActivity.typeface5;
                break;
            case 6:
                FullscreenActivity.presofont = FullscreenActivity.typeface6;
                break;
            case 7:
                FullscreenActivity.presofont = FullscreenActivity.typeface7;
                break;
            case 8:
                FullscreenActivity.presofont = FullscreenActivity.typeface8;
                break;
            case 9:
                FullscreenActivity.presofont = FullscreenActivity.typeface9;
                break;
            default:
                FullscreenActivity.presofont = FullscreenActivity.typeface0;
                break;
        }
    }
}
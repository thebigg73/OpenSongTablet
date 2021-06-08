package com.garethevans.church.opensongtablet.secondarydisplay;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.cast.CastPresentation;

public class ExternalDisplay extends CastPresentation implements MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, SurfaceHolder.Callback {

    // Define the variables and views
    MainActivityInterface mainActivityInterface;
    private Display myscreen;
    private RelativeLayout pageHolder, projectedPage_RelativeLayout;
    private LinearLayout projected_LinearLayout, presentermode_bottombit;
    private ImageView projected_ImageView, projected_Logo, projected_BackgroundImage, projected_SurfaceView_Alpha;
    private TextView presentermode_title, presentermode_author,
            presentermode_copyright, presentermode_ccli, presentermode_alert;
    private LinearLayout bottom_infobar, col1_1, col1_2, col2_2, col1_3, col2_3, col3_3;
    private SurfaceView projected_SurfaceView;
    private SurfaceHolder projected_SurfaceHolder;

    public ExternalDisplay(@NonNull Context context, @NonNull Display display) {
        super(context, display);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //mainActivityInterface.getPresentationCommon().mediaPlayerIsPrepared(projected_SurfaceView);
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp != null) {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.reset();
        }
        try {
            //reloadVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}

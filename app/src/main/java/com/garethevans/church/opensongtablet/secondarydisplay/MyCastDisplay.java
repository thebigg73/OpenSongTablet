package com.garethevans.church.opensongtablet.secondarydisplay;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import com.garethevans.church.opensongtablet.databinding.CastScreenBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.cast.CastPresentation;

public class MyCastDisplay extends CastPresentation  {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final Display display;
    private CastScreenBinding myView;
    private final String TAG = "MyCastDisplay";
    private PresentationCommon presentationCommon;

    public MyCastDisplay(Context c, Display display, MainActivityInterface mainActivityInterface) {
        super(c, display);
        this.c = c;
        this.display = display;
        this.mainActivityInterface = mainActivityInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myView = CastScreenBinding.inflate(getLayoutInflater(),null,false);
        setContentView(myView.getRoot());



    }
/*
    // Calls sent to the PresentationCommon class to deal with
    private void setDefaultColors() {
        presentationCommon.setSongInfoStyles();
    }

    private void setRotation() {
        presentationCommon.setRotation(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"castRotation",0.0f));
    }

    private void setScreenSizes() {
        presentationCommon.setScreenSizes(display);
    }

    private void setDefaultBackgroundImage() {
        presentationCommon.setDefaultBackgroundImage();
    }
    private void matchPresentationToMode() {
        *//*mainActivityInterface.getPresentationCommon().matchPresentationToMode(mainActivityInterface,
                myView.presentermodeBottombit, myView.projectedSurfaceView, myView.projectedBackgroundImage,
                myView.projectedImageView);
        fixBackground();*//*
    }
    private void fixBackground() {
        *//*mainActivityInterface.getPresentationCommon().fixBackground(c,mainActivityInterface,
                myView.projectedBackgroundImage, myView.projectedSurfaceView.getHolder(),myView.projectedSurfaceView);
        // Just in case there is a glitch, make the stuff visible after a time
        Handler panic = new Handler();
        panic.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateAlpha();
                (long) (1.1 * preferences.getMyPreferenceInt(c, "presoTransitionTime", 800)));
            }
        }ExternalDisplay::updateAlpha,
        //panic.postDelayed(PresentationServiceHDMI::updateAlpha, (long) (1.1*preferences.getMyPreferenceInt(c,"presoTransitionTime",800)));
    *//*
    }

    private void updateAlpha() {
        *//*mainActivityInterface.getPresentationCommon().updateAlpha(c,mainActivityInterface,
                myView.projectedBackgroundImage, myView.projectedSurfaceView);*//*
    }

    private void wipeProjectedLayout() {
        *//*Handler h = new Handler();
        h.postDelayed(() -> {
            // IV - Do the work after a transition delay
            try {
                projected_LinearLayout.removeAllViews();
                bottom_infobar.setAlpha(0.0f);
                projected_ImageView.setAlpha(0.0f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        },preferences.getMyPreferenceInt(c, "presoTransitionTime",800));
        CustomAnimations.faderAnimation(bottom_infobar,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);
    *//*
    }

    private void changeMargins() {
        //presentationCommon.changeMargins(c,preferences,projectedPage_RelativeLayout,StaticVariables.cast_presoInfoColor);
    }

    private void normalStartUp() {
        // Animate out the default logo
        setDefaultColors();
        mainActivityInterface.getPresentationCommon().normalStartUp();
        doUpdate();
    }

    public void setSongInfo() {
        if (myView.mainLogo.getVisibility()== View.VISIBLE) {
            myView.mainLogo.setVisibility(View.GONE);
        }
        mainActivityInterface.getPresentationCommon().setSongInfo();
    }

    public void setSongContent() {
        mainActivityInterface.getPresentationCommon().setSongContent();
    }

    private void presenterStartUp() {
        //getDefaultColors();
        // Set up the text styles and fonts for the bottom info bar
        //presenterThemeSetUp();
        //presentationCommon.presenterStartUp(c,preferences,storageAccess,projected_BackgroundImage,projected_SurfaceHolder,projected_SurfaceView);
    }
    static void presenterThemeSetUp() {
        //getDefaultColors();
        // Set the text at the bottom of the page to match the presentation text colour
        *//*presentationCommon.presenterThemeSetUp(c,preferences,presentermode_bottombit, presentermode_title,
                presentermode_author, presentermode_copyright, presentermode_ccli, presentermode_alert);
    *//*
    }
    static void updateFonts() {
        *//*getDefaultColors();
        presenterThemeSetUp(); // Sets the bottom info bar for presentation
        doUpdate(); // Updates the page*//*
    }

    // Video
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        *//*getScreenSizes();
        presentationCommon.prepareMediaPlayer(c, preferences, projected_SurfaceHolder, myscreen, bottom_infobar, projectedPage_RelativeLayout);
        StaticVariables.cast_mediaPlayer.setOnPreparedListener(this);
        StaticVariables.cast_mediaPlayer.setOnCompletionListener(this);
    *//*
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    }
    private void reloadVideo() {
        //presentationCommon.reloadVideo(c,preferences,projected_SurfaceHolder,projected_SurfaceView);
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        //presentationCommon.mediaPlayerIsPrepared(projected_SurfaceView);
    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        *//*if (mp != null) {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.reset();
        }
        try {
            reloadVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }*//*
    }

    // Update the screen content
    private void doUpdate() {
        *//*presentermode_alert.setAlpha(1.0f);
        presentationCommon.doUpdate(c,preferences,storageAccess,processSong,myscreen,presentermode_bottombit,projected_SurfaceView,
                projected_BackgroundImage, pageHolder,projected_Logo,projected_ImageView,projected_LinearLayout,bottom_infobar,projectedPage_RelativeLayout,
                presentermode_title, presentermode_author, presentermode_copyright, presentermode_ccli, presentermode_alert, col1_1, col1_2, col2_2, col1_3, col2_3, col3_3);
    *//*
    }
    private void updateAlert(boolean show, boolean update) {
        //presentationCommon.updateAlert(c, preferences, show, presentermode_alert);
        *//*if (update) {
            doUpdate();
        }*//*
    }
    private void setUpLogo() {
        //presentationCommon.setUpLogo(c,preferences,storageAccess,projected_Logo, StaticVariables.cast_availableWidth_1col,StaticVariables.cast_availableScreenHeight);
    }
    private void showLogoPrep() {
        //presentationCommon.showLogoPrep();
    }
    private void showLogo() {
        //presentationCommon.showLogo(c,preferences,projected_ImageView,projected_LinearLayout,pageHolder, projected_Logo);
    }
    private void hideLogo() {
        //presentationCommon.hideLogo(c,preferences, projected_Logo);
    }
    private void blankUnblankDisplay(boolean unblank) {
        //presentationCommon.blankUnblankDisplay(c,preferences,pageHolder,unblank);
    }*/
}

package com.garethevans.church.opensongtablet.secondarydisplay;

import android.app.Presentation;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;

import com.garethevans.church.opensongtablet.databinding.CastScreenBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class MyHDMIDisplay extends Presentation implements MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, SurfaceHolder.Callback {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final Display display;
    private CastScreenBinding myView;
    private final String TAG = "MyHDMIDisplay";
    private PresentationCommon presentationCommon;

    public MyHDMIDisplay(Context c, Display display, MainActivityInterface mainActivityInterface) {
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

        // Create a new PresentationCommon class for handling for this display
        presentationCommon = new PresentationCommon(c,mainActivityInterface);

        // Pass reference to the views
        presentationCommon.initialiseViews(myView.pageHolder, myView.testLayout, myView.songContent1,
                myView.songContent2, myView.songProjectionInfo1, myView.songProjectionInfo2,
                myView.mainLogo, myView.backgroundImage, myView.imageView1, myView.imageView2,
                myView.surfaceView1, myView.surfaceView2);

        // Set up the appropriate preferences
        setDefaultColors();
        updateCrossFadeTime();
        setSongInfoStyles();
        setRotation();
        setScreenSizes();
        changeMargins();
        setDefaultBackgroundImage();
        matchPresentationToMode();
        setupLogo();
        myView.songProjectionInfo1.postDelayed(this::setSongInfo,2000);
        setSongContent();
        //normalStartUp();
    }

    // Calls sent to the PresentationCommon class to deal with

    // Initialise views and settings
    private void setDefaultColors() {
        presentationCommon.setDefaultColors();
    }
    private void updateCrossFadeTime() {
        presentationCommon.updateCrossFadeTime();
    }
    private void setSongInfoStyles() {
        presentationCommon.setSongInfoStyles();
    }
    private void setRotation() {
        presentationCommon.setRotation(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"castRotation",0.0f));
    }
    private void setScreenSizes() {
        presentationCommon.setScreenSizes(display);
    }
    private void changeMargins() {
        presentationCommon.changeMargins();
    }
    private void setDefaultBackgroundImage() {
        presentationCommon.setDefaultBackgroundImage();
    }
    private void matchPresentationToMode() {
        presentationCommon.matchPresentationToMode();
    }



    // The logo
    private void setupLogo() {
        Log.d(TAG,"setupLogo().  mode="+mainActivityInterface.getMode());
        presentationCommon.showLogo(true, !mainActivityInterface.getMode().equals("Presenter"));
    }
    public void showLogo(boolean show) {
        presentationCommon.showLogo(show,false);
    }


    // The black screen
    public void showBlackScreen(boolean black) {
        presentationCommon.showBlackScreen(black);
    }

    // The song info bar
    public void setSongInfo() {
        presentationCommon.setSongInfo();
    }



    private void updateAlpha() {
        /*mainActivityInterface.getPresentationCommon().updateAlpha(c,mainActivityInterface,
                myView.projectedBackgroundImage, myView.projectedSurfaceView);*/
    }

    private void wipeProjectedLayout() {
        /*Handler h = new Handler();
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
    */
    }



    private void normalStartUp() {
        // Animate out the default logo

        presentationCommon.normalStartUp();
        doUpdate();
    }



    public void setSongContent() {
        presentationCommon.setSongContent();
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
        /*presentationCommon.presenterThemeSetUp(c,preferences,presentermode_bottombit, presentermode_title,
                presentermode_author, presentermode_copyright, presentermode_ccli, presentermode_alert);
    */
    }
    static void updateFonts() {
        /*getDefaultColors();
        presenterThemeSetUp(); // Sets the bottom info bar for presentation
        doUpdate(); // Updates the page*/
    }

    // Video
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        /*getScreenSizes();
        presentationCommon.prepareMediaPlayer(c, preferences, projected_SurfaceHolder, myscreen, bottom_infobar, projectedPage_RelativeLayout);
        StaticVariables.cast_mediaPlayer.setOnPreparedListener(this);
        StaticVariables.cast_mediaPlayer.setOnCompletionListener(this);
    */
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
        /*if (mp != null) {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.reset();
        }
        try {
            reloadVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    // Update the screen content
    public void showSection(int position) {
        presentationCommon.showSection(position);
    }

    private void doUpdate() {
        /*presentermode_alert.setAlpha(1.0f);
        presentationCommon.doUpdate(c,preferences,storageAccess,processSong,myscreen,presentermode_bottombit,projected_SurfaceView,
                projected_BackgroundImage, pageHolder,projected_Logo,projected_ImageView,projected_LinearLayout,bottom_infobar,projectedPage_RelativeLayout,
                presentermode_title, presentermode_author, presentermode_copyright, presentermode_ccli, presentermode_alert, col1_1, col1_2, col2_2, col1_3, col2_3, col3_3);
    */
    }
    private void updateAlert(boolean show, boolean update) {
        //presentationCommon.updateAlert(c, preferences, show, presentermode_alert);
        /*if (update) {
            doUpdate();
        }*/
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
    }
}

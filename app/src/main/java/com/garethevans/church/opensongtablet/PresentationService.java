package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.cast.CastPresentation;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;

// All of the classes after initialisation are the same for the PresentationService and PresentationServiceHDMI files
// They both call functions in the PresentationCommon file
// Both files are needed as they initialise and communicate with the display differently, but after that the stuff is almost entirely identical

public class PresentationService extends CastRemoteDisplayLocalService {

    private ExternalDisplay myPresentation;
    private ProcessSong processSong;
    private void createPresentation(Display display, ProcessSong pS) {
        dismissPresentation();
        processSong = pS;
        myPresentation = new ExternalDisplay(this, display, pS);
        try {
            myPresentation.show();
            FullscreenActivity.isPresenting = true;

        } catch (WindowManager.InvalidDisplayException ex) {
            ex.printStackTrace();
            dismissPresentation();
            FullscreenActivity.isPresenting = false;
        }
    }

    @Override
    public void onCreatePresentation(Display display) {
        if (processSong == null) {
            processSong = new ProcessSong();
        }

        FullscreenActivity.isPresenting = true;
        createPresentation(display, processSong);
    }

    @Override
    public void onDismissPresentation() {
        FullscreenActivity.isPresenting = false;
        dismissPresentation();
    }

    @Override
    public void onDestroy() {
        if (myPresentation!=null) {
            try {
                myPresentation.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void dismissPresentation() {
        if (myPresentation != null) {
            myPresentation.dismiss();
            myPresentation = null;
        }
        FullscreenActivity.isPresenting = false;
    }

    // Indent of common content kept the same across versions
    // This is the cast version (so HDMI commented out)

    static class ExternalDisplay extends CastPresentation
    //class PresentationServiceHDMI extends Presentation
            implements MediaPlayer.OnVideoSizeChangedListener,
            MediaPlayer.OnPreparedListener,
            MediaPlayer.OnCompletionListener, SurfaceHolder.Callback {

        ExternalDisplay(Context context, Display display, ProcessSong pS) {
        //PresentationServiceHDMI(Context context, Display display, ProcessSong pS) {
            super(context, display);
            c = context;
            myscreen = display;
            processSong = pS;
        }

        // Define the variables and views
        private static Display myscreen;
        @SuppressLint("StaticFieldLeak")
        private static RelativeLayout pageHolder, projectedPage_RelativeLayout;
        @SuppressLint("StaticFieldLeak")
        private static LinearLayout projected_LinearLayout, presentermode_bottombit;
        @SuppressLint("StaticFieldLeak")
        private static ImageView projected_ImageView, projected_Logo, projected_BackgroundImage;
        @SuppressLint("StaticFieldLeak")
        private static TextView presentermode_title, presentermode_author,
                presentermode_copyright, presentermode_ccli, presentermode_alert;
        @SuppressLint("StaticFieldLeak")
        private static LinearLayout bottom_infobar, col1_1, col1_2, col2_2, col1_3, col2_3, col3_3;
        @SuppressLint("StaticFieldLeak")
        private static SurfaceView projected_SurfaceView;
        private static SurfaceHolder projected_SurfaceHolder;
        private static StorageAccess storageAccess;
        private static Preferences preferences;
        private static ProcessSong processSong;
        private static PresentationCommon presentationCommon;
        @SuppressLint("StaticFieldLeak")
        private static Context c;
        @SuppressLint("StaticFieldLeak")

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            try {
                setContentView(R.layout.cast_screen);

                storageAccess = new StorageAccess();
                preferences = new Preferences();
                presentationCommon = new PresentationCommon();

                getDefaultColors();

                // Identify the views
                identifyViews();

                c = projectedPage_RelativeLayout.getContext();

                // Set the default background image
                setDefaultBackgroundImage();

                // Based on the mode we are in, hide the appropriate stuff at the bottom of the page
                matchPresentationToMode();

                // Change margins
                changeMargins();

                // Decide on screen sizes
                getScreenSizes();

                // Set up the logo
                setUpLogo();
                if (PresenterMode.logoButton_isSelected) {
                    bottom_infobar.setAlpha(0.0f);
                    showLogoPrep();
                    showLogo();
                }

                // Prepare the display after 2 secs (a chance for stuff to be measured and show the logo
                Handler h = new Handler();
                h.postDelayed(() -> {
                    presenterStartUp();
                    if (!StaticVariables.whichMode.equals("Presentation")) {
                        normalStartUp();
                    }
                }, 2000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // The screen and layout defaults
        private void identifyViews() {
            pageHolder = findViewById(R.id.pageHolder);
            projectedPage_RelativeLayout = findViewById(R.id.projectedPage_RelativeLayout);
            projected_LinearLayout = findViewById(R.id.projected_LinearLayout);
            projected_ImageView = findViewById(R.id.projected_ImageView);
            projected_BackgroundImage = findViewById(R.id.projected_BackgroundImage);
            projected_SurfaceView = findViewById(R.id.projected_SurfaceView);
            projected_SurfaceHolder = projected_SurfaceView.getHolder();
            projected_SurfaceHolder.addCallback(this);
            projected_Logo = findViewById(R.id.projected_Logo);
            presentermode_bottombit = findViewById(R.id.presentermode_bottombit);
            presentermode_title = findViewById(R.id.presentermode_title);
            presentermode_author = findViewById(R.id.presentermode_author);
            presentermode_copyright = findViewById(R.id.presentermode_copyright);
            presentermode_ccli = findViewById(R.id.presentermode_ccli);
            presentermode_alert = findViewById(R.id.presentermode_alert);
            bottom_infobar = findViewById(R.id.bottom_infobar);
            col1_1 = findViewById(R.id.col1_1);
            col1_2 = findViewById(R.id.col1_2);
            col2_2 = findViewById(R.id.col2_2);
            col1_3 = findViewById(R.id.col1_3);
            col2_3 = findViewById(R.id.col2_3);
            col3_3 = findViewById(R.id.col3_3);
        }
        public static void wipeProjectedLayout() {
            Handler h = new Handler();
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
        }

        private static void getScreenSizes() {
            presentationCommon.getScreenSizes(myscreen,bottom_infobar,projectedPage_RelativeLayout, preferences.getMyPreferenceFloat(c,"castRotation",0.0f));
        }
        private void setDefaultBackgroundImage() {
            presentationCommon.setDefaultBackgroundImage(c);
        }
        private static void matchPresentationToMode() {
            if (presentationCommon.matchPresentationToMode(presentermode_bottombit,projected_SurfaceView,projected_BackgroundImage,projected_ImageView)) {
                fixBackground();
            }
        }
        static void changeMargins() {
            presentationCommon.changeMargins(c,preferences,projectedPage_RelativeLayout,StaticVariables.cast_presoInfoColor);
        }
        static void fixBackground() {
            presentationCommon.fixBackground(c,preferences,storageAccess,projected_BackgroundImage,projected_SurfaceHolder,projected_SurfaceView);
            // Just in case there is a glitch, make the stuff visible after a time
            Handler panic = new Handler();
            panic.postDelayed(ExternalDisplay::updateAlpha, (long) (1.1*preferences.getMyPreferenceInt(c,"presoTransitionTime",800)));
            //panic.postDelayed(PresentationServiceHDMI::updateAlpha, (long) (1.1*preferences.getMyPreferenceInt(c,"presoTransitionTime",800)));
        }
        private static void getDefaultColors() {
            presentationCommon.getDefaultColors(c,preferences);
        }
        private static void updateAlpha() {
            presentationCommon.updateAlpha(c,preferences,projected_BackgroundImage,projected_SurfaceView,bottom_infobar);
        }
        private void normalStartUp() {
            // Animate out the default logo
            getDefaultColors();
            presentationCommon.normalStartUp(c,preferences,projected_Logo);
            doUpdate();
        }
        private void presenterStartUp() {
            getDefaultColors();
            // Set up the text styles and fonts for the bottom info bar
            presenterThemeSetUp();
            presentationCommon.presenterStartUp(c,preferences,storageAccess,projected_BackgroundImage,projected_SurfaceHolder,projected_SurfaceView);
        }
        static void presenterThemeSetUp() {
            getDefaultColors();
            // Set the text at the bottom of the page to match the presentation text colour
            presentationCommon.presenterThemeSetUp(c,preferences,presentermode_bottombit, presentermode_title,
                    presentermode_author, presentermode_copyright, presentermode_ccli, presentermode_alert);
        }
        static void updateFonts() {
            getDefaultColors();
            presenterThemeSetUp(); // Sets the bottom info bar for presentation
            doUpdate(); // Updates the page
        }

        // Video
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            getScreenSizes();
            presentationCommon.prepareMediaPlayer(c, preferences, projected_SurfaceHolder, myscreen, bottom_infobar, projectedPage_RelativeLayout);
            StaticVariables.cast_mediaPlayer.setOnPreparedListener(this);
            StaticVariables.cast_mediaPlayer.setOnCompletionListener(this);
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
        private static void reloadVideo() {
            presentationCommon.reloadVideo(c,preferences,projected_SurfaceHolder,projected_SurfaceView);
        }
        @Override
        public void onPrepared(MediaPlayer mp) {
            presentationCommon.mediaPlayerIsPrepared(projected_SurfaceView);
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
                reloadVideo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Update the screen content
        static void doUpdate() {
            presentermode_alert.setAlpha(1.0f);
            presentationCommon.doUpdate(c,preferences,storageAccess,processSong,myscreen,presentermode_bottombit,projected_SurfaceView,
                    projected_BackgroundImage, pageHolder,projected_Logo,projected_ImageView,projected_LinearLayout,bottom_infobar,projectedPage_RelativeLayout,
                    presentermode_title, presentermode_author, presentermode_copyright, presentermode_ccli, presentermode_alert, col1_1, col1_2, col2_2, col1_3, col2_3, col3_3);
        }
        static void updateAlert(boolean show, boolean update) {
            presentationCommon.updateAlert(c, preferences, show, presentermode_alert);
            if (update) {
                doUpdate();
            }
        }
        static void setUpLogo() {
            presentationCommon.setUpLogo(c,preferences,storageAccess,projected_Logo,StaticVariables.cast_availableWidth_1col,StaticVariables.cast_availableScreenHeight);
        }
        static void showLogoPrep() {
            presentationCommon.showLogoPrep();
        }
        static void showLogo() {
            presentationCommon.showLogo(c,preferences,projected_ImageView,projected_LinearLayout,pageHolder, projected_Logo);
        }
        static void hideLogo() {
            presentationCommon.hideLogo(c,preferences, projected_Logo);
        }
        static void blankUnblankDisplay(boolean unblank) {
            presentationCommon.blankUnblankDisplay(c,preferences,pageHolder,unblank);
        }
    }
}

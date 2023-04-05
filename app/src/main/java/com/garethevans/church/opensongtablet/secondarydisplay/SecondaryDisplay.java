package com.garethevans.church.opensongtablet.secondarydisplay;
// TODO
// Info bar not working
// Clicking on the first section crossfades with a blank view?
// Clicking on a section when logo is on shouldn't start the timer, but a logo off with content should

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.annotation.SuppressLint;
import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.CastScreenBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

// This contains all of the screen mirroring logic (previously PresentationCommon and Presentation)
// All common preferences are stored in the Presentation/PresentationSettings.java class
// Colors are stored in ScreenSetup/ThemeColors.java class

// TODO TIDY UP
public class SecondaryDisplay extends Presentation {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final Display display;
    private CastScreenBinding myView;
    private final String TAG = "SecondaryDisplay";
    private ArrayList<View> secondaryViews;
    private ArrayList<Integer> secondaryWidths, secondaryHeights;
    private DisplayMetrics displayMetrics;

    // Default variables
    private float scaleChords, scaleHeadings, scaleComments, lineSpacing;
    private Timer waitUntilTimer;
    private TimerTask waitUntilTimerTask;
    private int waitingOnViewsToDraw;
    private int showWhichBackground = 0;  //0=not set, 1=image1, 2=image2, 3=surface1, 4=surface2
    private int showWhichVideo = 0;
    private final int logoSplashTime = 3000;
    private int defaultPadding;
    private int availableScreenWidth;
    private int availableScreenHeight;
    private int horizontalSize;
    private int verticalSize;

    private boolean firstRun = true;
    private boolean isNewSong;
    private String currentInfoText;
    private boolean waitForVideo;
    private boolean infoBarRequired=false;
    private boolean boldChordHeading;
    private boolean displayChords;
    private boolean invertXY;

    private ViewTreeObserver.OnGlobalLayoutListener testSongInfoVTO;

    MediaPlayer mediaPlayer1=new MediaPlayer(), mediaPlayer2=new MediaPlayer();
    View backgroundToFadeIn, backgroundToFadeOut;
    Surface surface1, surface2;

    public SecondaryDisplay(Context c, Display display) {
        super(c, display);
        this.c = c;
        this.display = display;
        mainActivityInterface = (MainActivityInterface) c;
        // If something is wrong, dismiss the Presentation
        if (c == null || display == null) {
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myView = CastScreenBinding.inflate(getLayoutInflater(),null,false);
        setContentView(myView.getRoot());

        displayMetrics = this.getResources().getDisplayMetrics();

        invertXY = mainActivityInterface.getPresenterSettings().getCastRotation()==90f ||
                mainActivityInterface.getPresenterSettings().getCastRotation()==270f;

        // Initialise the video surfaces
        initialiseVideoSurfaces();

        // Initialise view visibilities
        intialiseViewVisibity();

        // Set the info bars to match the mode
        setInfoStyles();
        changeInfoAlignment();

        // Set the page background color based on the mode
        mainActivityInterface.getMyThemeColors().getDefaultColors();
        updatePageBackgroundColor();
        myView.songProjectionInfo1.setupFonts(mainActivityInterface);
        myView.songProjectionInfo2.setupFonts(mainActivityInterface);

        // Now we can test the song layout and measure what we need
        // Once this is done in the next function, the viewTreeObserver notifies the next steps
        setScreenSizes();
    }

    // Initialise views and settings
    private void initialiseVideoSurfaces() {
        // Set up the surfaceTextures
        mediaPlayer1.setLooping(true);
        mediaPlayer2.setLooping(true);
        mediaPlayer1.setOnPreparedListener(new MyPreparedListener());
        mediaPlayer2.setOnPreparedListener(new MyPreparedListener());
        myView.textureView1.setSurfaceTextureListener(new MySurfaceTextureAvailable(1));
        myView.textureView2.setSurfaceTextureListener(new MySurfaceTextureAvailable(2));
    }
    private void intialiseViewVisibity() {
        // Views that need to be measured need to be VISIBLE or INVISIBLE.
        // This is the test pane for all modes
        myView.testLayout.setVisibility(View.INVISIBLE);

        // All other views (backgroundImage1/2, surfaceTexture1/2, logo, songProjectionInfo1/2, alertBar)
        // need to start in the faded out state - GONE and alpha 0.0f
        setViewAlreadyFadedOut(myView.allContent);
        setViewAlreadyFadedOut(myView.backgroundImage1);
        setViewAlreadyFadedOut(myView.backgroundImage2);
        setViewAlreadyFadedOut(myView.songContent1);
        setViewAlreadyFadedOut(myView.songContent2);
        setViewAlreadyFadedOut(myView.textureView1);
        setViewAlreadyFadedOut(myView.textureView1);
        setViewAlreadyFadedOut(myView.mainLogo);
        setViewAlreadyFadedOut(myView.songProjectionInfo1);
        setViewAlreadyFadedOut(myView.songProjectionInfo2);
        setViewAlreadyFadedOut(myView.alertBar);

        myView.songProjectionInfo1.setIsDisplaying(true);
        myView.songProjectionInfo2.setIsDisplaying(false);

        myView.songContent1.setIsDisplaying(true);
        myView.songContent2.setIsDisplaying(false);
    }

    private void setViewAlreadyFadedOut(View v) {
        // For a fade in animation to work, the view should be GONE and 0f alpha
        v.setVisibility(View.GONE);
        v.setAlpha(0f);
    }
    public void updatePageBackgroundColor() {
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter))) {
            // In Presenter mode, we set the bottom layer as black (to allow black screen)
            // Any video, image or coloured backgrounds get their own layer above this (set elsewhere)
            myView.castFrameLayout.setBackgroundColor(Color.BLACK);
        } else {
            // In Performance/Stage mode, we use the user settings from the theme
            myView.songContent1.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
            myView.songContent2.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
            myView.castFrameLayout.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
        }
    }
    private boolean canShowSong() {
        // Determines if we are allowed to fade in content (no logo or blankscreen)
        return !mainActivityInterface.getPresenterSettings().getLogoOn() &&
                !mainActivityInterface.getPresenterSettings().getBlankscreenOn() &&
                !mainActivityInterface.getPresenterSettings().getBlackscreenOn();
    }

    // Now the screen settings
    public void setScreenSizes() {
        // We need to wait until the view is prepared before rotating and measuring if required
        ViewTreeObserver viewTreeObserver = myView.pageHolder.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                myView.pageHolder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                changeRotation();
            }
        });
    }
    public void changeRotation() {
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        invertXY = mainActivityInterface.getPresenterSettings().getCastRotation() == 90.0f ||
                mainActivityInterface.getPresenterSettings().getCastRotation() == 270.0f;
        if (invertXY) {
            // Switch width for height and vice versa
            horizontalSize = metrics.heightPixels;
            verticalSize = metrics.widthPixels;
        } else {
            horizontalSize = metrics.widthPixels;
            verticalSize = metrics.heightPixels;
        }
        myView.pageHolder.setPivotX(0);
        myView.pageHolder.setPivotY(0);
        myView.pageHolder.setRotation(mainActivityInterface.getPresenterSettings().getCastRotation());
        myView.pageHolder.setX(0);
        myView.pageHolder.setY(0);
        if (mainActivityInterface.getPresenterSettings().getCastRotation()==90f) {
            myView.pageHolder.setTranslationX(metrics.widthPixels);
            myView.pageHolder.setTranslationY(0f);
        } else if (mainActivityInterface.getPresenterSettings().getCastRotation()==180f) {
            myView.pageHolder.setTranslationX(metrics.widthPixels);
            myView.pageHolder.setTranslationY(metrics.heightPixels);
        } else if (mainActivityInterface.getPresenterSettings().getCastRotation()==270f) {
            myView.pageHolder.setTranslationX(0f);
            myView.pageHolder.setTranslationY(metrics.heightPixels);
        } else {
            myView.pageHolder.setTranslationX(0f);
            myView.pageHolder.setTranslationY(0);
        }

        myView.mainLogo.setRotation(mainActivityInterface.getPresenterSettings().getCastRotation());

        // Available size has to take into consideration any padding

        availableScreenWidth = horizontalSize - Math.round((2 * (displayMetrics.density * mainActivityInterface.getPresenterSettings().getPresoXMargin())));
        availableScreenHeight = verticalSize - Math.round((2 * (displayMetrics.density*mainActivityInterface.getPresenterSettings().getPresoYMargin())));

        updateViewSizes(myView.pageHolder);

        // These bits are dependent on the screen size, so are called here initially
        changeMargins();
        changeLogo();
        matchPresentationToMode();
        setSongInfo();
        setSongContent();
        changeBackground();

        // Decide what to do about the logo
        // For PresenterMode, we should show it to show initially on this new display
        // If the logo is switched on, then leave it on, otherwise hide it
        if (firstRun) {
            boolean timedHide = true;
            if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter))) {
                // If the user has the logo switched on, so leave it on (no timedHide)
                if (mainActivityInterface.getPresenterSettings().getLogoOn()) {
                    timedHide = false;
                }
                // If the user is showing a blank screen and no logo (before this display connected)
                // Show it after the splash time
                if (!mainActivityInterface.getPresenterSettings().getLogoOn() &&
                        mainActivityInterface.getPresenterSettings().getBlankscreenOn()) {
                    showBlankScreen();
                }

                // If the user is showing a section (not logo), then show it after the splash time
                if (!mainActivityInterface.getPresenterSettings().getLogoOn() &&
                    mainActivityInterface.getPresenterSettings().getCurrentSection()>-1) {
                    new Handler().postDelayed(()-> showSection(mainActivityInterface.getPresenterSettings().getCurrentSection()),logoSplashTime);
                }

                // If the user is already showing a black screen (before this display connected)
                // Then trigger the black screen after the splash time too
                // This overrides anything other than the logo showing
                if (mainActivityInterface.getPresenterSettings().getBlackscreenOn()) {
                    new Handler().postDelayed(this::showBlackScreen,logoSplashTime);
                }

            } else if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage))) {
                // Prepare the current section 0 ready for the logo hiding after splash
                new Handler().postDelayed(()-> showSection(0),logoSplashTime);

            } else {
               new Handler().postDelayed(this::showAllSections,logoSplashTime);
            }

            // The logo always gets shown on first run
            firstRun = false;
            showLogo(true, timedHide);
        }
    }
    private void changeMargins() {
        myView.pageHolder.setPadding(mainActivityInterface.getPresenterSettings().getPresoXMargin(),
                mainActivityInterface.getPresenterSettings().getPresoYMargin(),
                mainActivityInterface.getPresenterSettings().getPresoXMargin(),
                mainActivityInterface.getPresenterSettings().getPresoYMargin());
    }
    private void updateViewSizes(View view) {
        if (view == myView.pageHolder) {
            FrameLayout.LayoutParams fllp = (FrameLayout.LayoutParams)view.getLayoutParams();
            fllp.width = availableScreenWidth;
            fllp.height = availableScreenHeight;
            view.setLayoutParams(fllp);
        }
    }

    // Set views depending on mode
    public void matchPresentationToMode() {
        // Get the settings that are appropriate.  This is called on first run
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter))) {
            // IV - Note that the Heading, Comment and tab are ignored in song load
            displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean("presoShowChords",false);
            boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean("presoLyricsBold", false);
        } else {
            scaleHeadings = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleHeadings", 0.8f);
            scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleComments", 0.8f);
            displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean("displayChords", true);
            boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean("boldChordHeading", false);
        }
        infoBarRequired = true;
        hideCols2and3();
    }
    private void hideCols2and3() {
        // Only need these in performance mode
        int visiblity = View.GONE;
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
            visiblity = View.VISIBLE;
        }
        myView.songContent1.getCol1().setVisibility(View.VISIBLE);
        myView.songContent1.getCol2().setVisibility(visiblity);
        myView.songContent1.getCol3().setVisibility(visiblity);
        myView.songContent2.getCol1().setVisibility(View.VISIBLE);
        myView.songContent2.getCol2().setVisibility(visiblity);
        myView.songContent2.getCol3().setVisibility(visiblity);
    }

    // The screen background
    public void changeBackground() {
        // There has been an update to the user's background or logo, so pull them in from preferences
        // (already updated in PresenterSettings)
        // This only runs in PresenterMode!  Performance/Stage Mode reflect the device theme
        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter))) {
            // We can use either a drawable (for a solid colour) or a uri (for an image)
            // Get the current background to fade out and set the background to the next
            backgroundToFadeOut = null;
            if (showWhichBackground<2) {
                backgroundToFadeOut = myView.backgroundImage1;
            } else if (showWhichBackground==2) {
                backgroundToFadeOut = myView.backgroundImage2;
            } else if (showWhichBackground==3) {
                backgroundToFadeOut = myView.textureView1;
            } else if (showWhichBackground==4) {
                backgroundToFadeOut = myView.textureView2;
            }

            // If this is the first time, showWhichBackground==0
            backgroundToFadeIn = null;
            waitForVideo = false;
            if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("img") ||
                    mainActivityInterface.getPresenterSettings().getBackgroundToUse().equals("color")) {
                if (showWhichBackground < 2) {
                    showWhichBackground = 2;
                    backgroundToFadeIn = myView.backgroundImage2;
                } else {
                    showWhichBackground = 1;
                    backgroundToFadeIn = myView.backgroundImage1;
                }
            } else if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("vid")) {
                if (showWhichBackground == 3) {
                    showWhichBackground = 4;
                    backgroundToFadeIn = myView.textureView2;
                } else {
                    showWhichBackground = 3;
                    backgroundToFadeIn = myView.textureView1;
                }
            }

            // Set the current background to the new one
            Uri background = mainActivityInterface.getPresenterSettings().getChosenBackground();
            RequestOptions requestOptions = new RequestOptions().centerCrop();

            if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().equals("color") ||
                    (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("img") && background == null)) {
                // Use a solid background color
                assert backgroundToFadeIn instanceof ImageView;
                Drawable drawable = ContextCompat.getDrawable(c, R.drawable.simple_rectangle);
                if (drawable != null) {
                    GradientDrawable solidColor = (GradientDrawable) drawable.mutate();
                    solidColor.setSize(availableScreenWidth, availableScreenHeight);
                    solidColor.setColor(mainActivityInterface.getPresenterSettings().getBackgroundColor());
                    Glide.with(c).load(solidColor).apply(requestOptions).into((ImageView) backgroundToFadeIn);
                }
            } else if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("img")) {
                // Use a static image
                assert backgroundToFadeIn instanceof ImageView;
                if (background.toString().equals("bg.png")) {
                    Drawable defaultImage = ResourcesCompat.getDrawable(c.getResources(), R.drawable.preso_default_bg, null);
                    Glide.with(c).load(defaultImage).apply(requestOptions).into((ImageView) backgroundToFadeIn);
                } else {
                    Glide.with(c).load(background).apply(requestOptions).into((ImageView) backgroundToFadeIn);
                }
            } else if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("vid")) {
                // We need to wait for the video to be prepared
                waitForVideo = true;
            }

            // Now send the animations to fade out the current (if not waiting for a video)
            if (!waitForVideo) {
                crossFadeBackgrounds();
            } else {
                // Prepare the video background
                if (showWhichVideo < 2) {
                    backgroundToFadeIn = myView.textureView1;
                } else {
                    backgroundToFadeIn = myView.textureView2;
                }
                loadVideo();
            }
        }
    }
    private void crossFadeBackgrounds() {
        mainActivityInterface.getCustomAnimation().faderAnimation(backgroundToFadeOut,
                mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                mainActivityInterface.getPresenterSettings().getPresoBackgroundAlpha(), 0f);
        mainActivityInterface.getCustomAnimation().faderAnimation(backgroundToFadeIn,
                mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                0f, mainActivityInterface.getPresenterSettings().getPresoBackgroundAlpha());
    }

    // The logo
    public void changeLogo() {
        // There may have been an update to the user's logo.  Called from change Background in this
        int size = (int)(mainActivityInterface.getPresenterSettings().getLogoSize() * availableScreenWidth);
        ViewGroup.LayoutParams layoutParams = myView.mainLogo.getLayoutParams();
        layoutParams.width = size;
        layoutParams.height = size;
        myView.mainLogo.setLayoutParams(layoutParams);
        RequestOptions requestOptions = new RequestOptions().fitCenter().override(size,size);
        Glide.with(c).load(mainActivityInterface.getPresenterSettings().getLogo()).apply(requestOptions).into(myView.mainLogo);
    }
    public void showLogo(boolean show, boolean timedHide) {
        // Fade in/out the logo based on the setting
        if (show) {
            crossFadeContent(myView.allContent,myView.mainLogo);
            mainActivityInterface.getPresenterSettings().setStartedProjection(false);

        } else {
            crossFadeContent(myView.mainLogo,myView.allContent);
            mainActivityInterface.getPresenterSettings().setStartedProjection(true);
        }

        // Check for the song info
        checkSongInfoShowHide();

        if (timedHide) {
            // This will hide the logo after the logoSplashTime
            myView.mainLogo.postDelayed(() -> {
                mainActivityInterface.getPresenterSettings().setLogoOn(false);
                crossFadeContent(myView.mainLogo,myView.allContent);
                Log.d(TAG,"timed hiding of logo");
                mainActivityInterface.getPresenterSettings().setStartedProjection(true);
            },logoSplashTime);
        }
    }

    // The black or blank screen
    public void showBlackScreen() {
        float start;
        float end;
        if (mainActivityInterface.getPresenterSettings().getBlackscreenOn()) {
            start = 1f;
            end = 0f;
            mainActivityInterface.getPresenterSettings().setStartedProjection(true);

        } else {
            start = 0f;
            end = 1f;
            mainActivityInterface.getPresenterSettings().setStartedProjection(false);

        }
        mainActivityInterface.getCustomAnimation().faderAnimation(myView.pageHolder,
                mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                start,end);
    }
    public void showBlankScreen() {
        float start;
        float end;
        if (mainActivityInterface.getPresenterSettings().getBlankscreenOn()) {
            start = 1f;
            end = 0f;
        } else {
            start = 0f;
            end = 1f;
        }
        // If we are fading out, or fading in and can show the song, do it!
        mainActivityInterface.getPresenterSettings().setStartedProjection(start < end);

        if ((start>end) || canShowSong()) {
            Log.d(TAG, "fadingIn: "+(end>start)+"  fadingOut: "+(start>end)+"  songContent1:getIsDisplaying(): "+myView.songContent1.getIsDisplaying()+"  songContent2:getIsDisplaying(): "+myView.songContent2.getIsDisplaying());
            if (myView.songContent1.getIsDisplaying()) {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent1,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                        start, end);

            } else if (myView.songContent2.getIsDisplaying()) {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent2,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                        start, end);
            }

            // If we are fading out, or fading in but should show the info bar, do it
            if (start>end || infoBarRequired) {
                Log.d(TAG,"start: "+start+"  end: "+end);
                if (myView.songProjectionInfo1.getIsDisplaying()) {
                    Log.d(TAG,"songProjectionInfo1");
                    mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo1,
                            mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                            start, end);

                } else if (myView.songProjectionInfo2.getIsDisplaying()){
                    Log.d(TAG,"songProjectionInfo2");
                    mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo2,
                            mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                            start, end);
                }
            }
        }
    }

    // Declare a new song has loaded()
    public void setIsNewSong() {
        initialiseInfoBarRequired();
        isNewSong = true;
    }

    // The song info bar
    // For Presenter Mode, the bar shows as required under the following conditions:
    // - The first time a song is displayed
    // - For at least the untilWaitTime has elapsed since first presented
    private void setupTimers() {
        infoBarRequired = true;
        cancelInfoTimers();
        waitUntilTimer = new Timer();
        waitUntilTimerTask = new TimerTask() {
            @Override
            public void run() {
                // Switch off infoBarRequired and cancel the timer
                if (mainActivityInterface.getPresenterSettings().getHideInfoBar()) {
                    infoBarRequired = false;
                    try {
                        waitUntilTimer.cancel();
                        waitUntilTimerTask.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "timer over - infoBarRequired=" + infoBarRequired);
                }
            }
        };
        // The time that the info bar is required for
        int untilTimeWait = 20000;
        // If we are in Performance mode, don't do this
        if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
            waitUntilTimer.schedule(waitUntilTimerTask, untilTimeWait);
        }
    }
    private void cancelInfoTimers() {
        // If the info timers are set up, cancel them before we try to set new ones
        if (waitUntilTimer!=null) {
            try {
                waitUntilTimer.cancel();
            } catch (Exception e) {
                Log.d(TAG,"Unable to cancel infobar wait timer");
            }
        }
        if (waitUntilTimerTask!=null) {
            try {
                waitUntilTimerTask.cancel();
            } catch (Exception e) {
                Log.d(TAG,"Unable to cancel info bar wait timertask");
            }
        }
    }
    public void setInfoStyles() {
        myView.testSongInfo.setupLayout(c,mainActivityInterface,false);
        myView.songProjectionInfo1.setupLayout(c,mainActivityInterface,false);
        myView.songProjectionInfo2.setupLayout(c,mainActivityInterface,false);
    }
    public void changeInfoAlignment() {
        myView.songProjectionInfo1.setAlign(mainActivityInterface.getPresenterSettings().getPresoInfoAlign());
        myView.songProjectionInfo2.setAlign(mainActivityInterface.getPresenterSettings().getPresoInfoAlign());
    }
    public void setSongInfo() {
        // This is called when a song is loaded up, or a section is clicked
        // Only do this if there is a change
        Log.d(TAG,"setSongInfo().  isNewSong="+isNewSong);
        if (isNewSong) {
            String title = mainActivityInterface.getSong().getTitle();
            Log.d(TAG,"setSongInfo().  title="+title);

            if (title == null || title.isEmpty()) {
                title = mainActivityInterface.getSong().getFilename();
            }
            String ccliLine = c.getString(R.string.used_by_permision);
            if (!mainActivityInterface.getPresenterSettings().getCcliLicence().isEmpty()) {
                ccliLine += ".  CCLI " +
                        c.getString(R.string.ccli_licence) + " " + mainActivityInterface.
                        getPresenterSettings().getCcliLicence();
            }
            String copyright = mainActivityInterface.getSong().getCopyright();
            if (copyright != null && !copyright.isEmpty()) {
                if (!copyright.contains("©")) {
                    copyright = "©" + copyright;
                }
            } else {
                copyright = "";
            }
            String author = mainActivityInterface.getSong().getAuthor();
            if (author != null && !author.isEmpty()) {
                author = c.getString(R.string.words_and_music_by) + " " + author;
            }
            currentInfoText = title + author + copyright + ccliLine;


            String capo = mainActivityInterface.getSong().getCapo();
            if (!mainActivityInterface.getPresenterSettings().getPresoShowChords() ||
                    !mainActivityInterface.getProcessSong().showingCapo(capo)) {
                capo = null;
            }

            Log.d(TAG,"currentInfoText:"+currentInfoText);

            // Get final strings for VTO
            String finalTitle = title;
            String finalAuthor = author;
            String finalCopyright = copyright;
            String finalCcli = ccliLine;
            String finalCapo = capo;

            // Remove any old VTO
            if (testSongInfoVTO!=null) {
                myView.testSongInfo.getViewTreeObserver().removeOnGlobalLayoutListener(testSongInfoVTO);
                testSongInfoVTO = null;
            }

            // Set the test view values to null
            myView.testSongInfo.setNullValues();
            myView.testSongInfo.requestLayout();

            // Create the new VTO
            testSongInfoVTO = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Check we are all done and proceed if all values are non null/set
                    if (myView.testSongInfo.getValuesNonNull()) {
                        // Measure the view
                        int height = myView.testSongInfo.getHeight();

                        // Now write the actual song info and set the determined height
                        // These will go into the currently hidden info bar
                        if (!myView.songProjectionInfo1.getIsDisplaying()) {
                            myView.songProjectionInfo1.setSongTitle(finalTitle);
                            myView.songProjectionInfo1.setSongAuthor(finalAuthor);
                            myView.songProjectionInfo1.setSongCopyright(finalCopyright);
                            myView.songProjectionInfo1.setSongCCLI(finalCcli);
                            if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
                                myView.songProjectionInfo1.setCapo(finalCapo);
                            } else {
                                myView.songProjectionInfo1.setCapo(null);
                            }
                            myView.songProjectionInfo1.setViewHeight(height);

                        } else {
                            myView.songProjectionInfo2.setSongTitle(finalTitle);
                            myView.songProjectionInfo2.setSongAuthor(finalAuthor);
                            myView.songProjectionInfo2.setSongCopyright(finalCopyright);
                            myView.songProjectionInfo2.setSongCCLI(finalCcli);
                            if (!mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
                                myView.songProjectionInfo2.setCapo(finalCapo);
                            } else {
                                myView.songProjectionInfo2.setCapo(null);
                            }
                            myView.songProjectionInfo2.setViewHeight(height);
                        }
                        myView.testSongInfo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        myView.testSongInfo.getViewTreeObserver().removeOnGlobalLayoutListener(testSongInfoVTO);
                    }
                    myView.testSongInfo.requestLayout();
                }
            };
            // Draw the test song info bar so we can measure it with a VTO
            myView.testSongInfo.getViewTreeObserver().addOnGlobalLayoutListener(testSongInfoVTO);

            // All info should be shown if available
            // Set it to the test view.  Once drawn, it gets measured for height in the VTO
            // It is then written to the correct view
            myView.testSongInfo.setSongTitle(finalTitle);
            myView.testSongInfo.setSongAuthor(finalAuthor);
            myView.testSongInfo.setSongCopyright(finalCopyright);
            myView.testSongInfo.setSongCCLI(finalCcli);
            myView.testSongInfo.setCapo(capo);
            myView.testSongInfo.requestLayout();
        }
    }
    public void initialiseInfoBarRequired() {
        infoBarRequired = true;
    }
    public void checkSongInfoShowHide() {
        View infoToHide = songInfoHideCheck();
        Log.d(TAG,"infoBarRequired:"+infoBarRequired);
        if (infoBarRequired) {
            if (songInfoChanged()) {
                isNewSong = false;
                // Get the info to show, this also changes the isDisplaying() property of both
                View infoToShow = songInfoShowCheck();
                crossFadeContent(infoToHide, infoToShow);
            }
        } else {
                crossFadeContent(infoToHide, null);
        }
    }
    private View songInfoHideCheck() {
        // Fade out can only happen if we no longer require the song info bar
        // Or we have changed the song
        if ((!infoBarRequired) || isNewSong) {
            if (myView.songProjectionInfo1.getIsDisplaying()) {
                return myView.songProjectionInfo1;
            } else if (myView.songProjectionInfo2.getIsDisplaying()) {
                return myView.songProjectionInfo2;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    private View songInfoShowCheck() {
        // If required (new song loaded and not already showing), show the info bar
        if ((canShowSong() &&
                (isNewSong ||
                        mainActivityInterface.getPresenterSettings().getCurrentSection()>-1 ||
                        mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))))) {

            // If the hide option is active set a timer to do the hide
            if (mainActivityInterface.getPresenterSettings().getHideInfoBar()) {
                Log.d(TAG, "set timer for hide");
                setupTimers();
            }

            if (!myView.songProjectionInfo1.getIsDisplaying()) {
                myView.songProjectionInfo1.setIsDisplaying(true);
                myView.songProjectionInfo2.setIsDisplaying(false);
                return myView.songProjectionInfo1;

            } else if (!myView.songProjectionInfo2.getIsDisplaying()) {
                myView.songProjectionInfo1.setIsDisplaying(false);
                myView.songProjectionInfo2.setIsDisplaying(true);
                return myView.songProjectionInfo2;
            } else {
                return null;
            }

        } else {
            return null;
        }
    }
    private boolean songInfoChanged() {
        if (myView.songProjectionInfo1.getIsDisplaying() &&
                myView.songProjectionInfo1.isNewInfo(currentInfoText)) {
            Log.d(TAG,"info1 is new text");
            return true;
        } else if (myView.songProjectionInfo2.getIsDisplaying() &&
                myView.songProjectionInfo2.isNewInfo(currentInfoText)) {
            Log.d(TAG,"info2 is new text");
            return true;
        } else {
            Log.d(TAG,"info1/2 are NOT new text");
            return false;
        }
    }


    // The song content
    public void setSongContent() {
        // Just like we do with the song processing, we draw the sections to the test layout
        // Then measure them, work out the best orientation and scaling
        // Then remove from the test layout and reattach to the song layout.

        Log.d(TAG,"setSongContent()");
        // Clear any existing views from the test layout.  We don't fade out existing song layout until we are ready
        myView.testLayout.removeAllViews();
        secondaryViews = null;
        secondaryViews = new ArrayList<>();
        secondaryWidths = null;
        secondaryWidths = new ArrayList<>();
        secondaryHeights = null;
        secondaryHeights = new ArrayList<>();

        // Decide if this is an XML, PDF or IMG file and proceed accordingly
        if (mainActivityInterface.getSong().getFiletype().equals("XML")) {
            setSectionViews();
        }
    }
    // TODO Update this when prefs change
    public void setSongContentPrefs() {
        boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean("boldChordHeading", false);
        scaleChords = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleChords", 0.8f);
    }

    private void setSectionViews() {
        boolean isPresentation = !mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance));
        secondaryViews = mainActivityInterface.getProcessSong().
                setSongInLayout(mainActivityInterface.getSong(),
                        false, isPresentation);

        // Draw them to the screen test layout for measuring
        waitingOnViewsToDraw = secondaryViews.size();
        for (View view : secondaryViews) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove this listener
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // In case rogue calls get fired, only proceed if we should
                    if (waitingOnViewsToDraw > 0) {
                        waitingOnViewsToDraw--;
                        if (waitingOnViewsToDraw == 0) {
                            // This was the last item, so move on
                            viewsAreReady();
                        }
                    } else {
                        waitingOnViewsToDraw = 0;
                    }
                }
            });
            myView.testLayout.addView(view);
            myView.testLayout.requestLayout();
        }
    }

    private void viewsAreReady() {
        // The views are ready so prepare to create the song page
        for (int x = 0; x < secondaryViews.size(); x++) {
            int width = secondaryViews.get(x).getMeasuredWidth();
            int height = secondaryViews.get(x).getMeasuredHeight();
            secondaryWidths.add(x, width);
            secondaryHeights.add(x, height);

            // Calculate the scale factor for each section individually
            // For each meausured view, get the max x and y scale value
            // Check they are less than the max preferred value
            float max_x = (float) availableScreenWidth / (float) secondaryWidths.get(x);
            float max_y = (float) availableScreenHeight / (float) secondaryHeights.get(x);
            // The text size is 14sp by default.  Compare this to the pref
            float best = Math.min(max_x, max_y);
            if ((best * 14f) > mainActivityInterface.getPresenterSettings().getFontSizePresoMax()) {
                best = mainActivityInterface.getPresenterSettings().getFontSizePresoMax() / 14f;
            }
            secondaryViews.get(x).setPivotX(0f);
            secondaryViews.get(x).setPivotY(0f);
            if (best > 0) {
                secondaryViews.get(x).setScaleX(best);
                secondaryViews.get(x).setScaleY(best);
            }
        }

        // We can now remove the views from the test layout
        myView.testLayout.removeAllViews();

        if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_performance))) {
            showAllSections();
        } else {
            // Only need to show the current section (if it has been chosen)
            if (mainActivityInterface.getSong().getCurrentSection()>=0) {
                showSection(mainActivityInterface.getPresenterSettings().getCurrentSection());
            }
        }
    }

    public void showSection(final int position) {
        Log.d(TAG,"position:"+position);
        try {
            // Decide which view to show.  Do nothing if it is already showing
            boolean stageOk = mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage));
            boolean presenterOk = mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter)) &&
                    mainActivityInterface.getPresenterSettings().getSongSectionsAdapter()!=null;
            boolean image = mainActivityInterface.getSong().getFiletype().equals("IMG");
            boolean pdf = mainActivityInterface.getSong().getFiletype().equals("PDF");
            boolean imageslide = mainActivityInterface.getSong().getFolder().contains("**Image");
            int viewsAvailable;
            if (image) {
                viewsAvailable = 1;
            } else if (pdf || imageslide) {
                viewsAvailable = mainActivityInterface.getSong().getPdfPageCount();
            } else {
                viewsAvailable = mainActivityInterface.getSong().getPresoOrderSongSections().size();
            }
            // TODO need to fix for Stage mode too - getSongSectionsAdapter not initialised

            if ((stageOk || presenterOk || pdf || image || imageslide) && position!=-1) {
                // If we edited the section temporarily, remove this position flag
                if (presenterOk) {
                    mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().setSectionEdited(-1);
                }
                mainActivityInterface.getSong().setCurrentSection(position);
                if (position >= 0 && position < viewsAvailable) {
                    // Check the song info status first
                    checkSongInfoShowHide();

                    // Get the measured height of the song info bar
                    int infoHeight = 0;
                    if (myView.songProjectionInfo1.getIsDisplaying()) {
                        infoHeight = myView.songProjectionInfo1.getViewHeight();
                    } else if (myView.songProjectionInfo2.getIsDisplaying()) {
                        infoHeight = myView.songProjectionInfo2.getViewHeight();
                    }
                    if (infoHeight == 0) {
                        infoHeight = myView.testSongInfo.getViewHeight();
                    }
                    int alertHeight = myView.alertBar.getViewHeight();

                    if (!pdf && !image && !imageslide) {
                        // Remove the view from any parent it might be attached to already (can only have 1)
                            removeViewFromParent(secondaryViews.get(position));

                        // Get the size of the view
                        int width = secondaryWidths.get(position);
                        int height = secondaryHeights.get(position);

                        float max_x = (float) availableScreenWidth / (float) width;
                        float max_y = (float) (availableScreenHeight - infoHeight - alertHeight) / (float) height;

                        float best = Math.min(max_x, max_y);
                        if (best > (mainActivityInterface.getPresenterSettings().getFontSizePresoMax() / 14f)) {
                            best = mainActivityInterface.getPresenterSettings().getFontSizePresoMax() / 14f;
                        }

                        secondaryViews.get(position).setPivotX(0f);
                        secondaryViews.get(position).setPivotY(0f);
                        secondaryViews.get(position).setScaleX(best);
                        secondaryViews.get(position).setScaleY(best);

                        // We can now prepare the new view and animate in/out the views as long as the logo is off
                        // and the blank screen isn't on

                        // Translate the scaled views based on the alignment
                        int newWidth = (int) (width * best);
                        int newHeight = (int) (height * best);
                        translateView(secondaryViews.get(position), newWidth, newHeight, infoHeight, alertHeight);
                    }

                    Bitmap bitmap;
                    if (pdf) {
                        bitmap = mainActivityInterface.getProcessSong().getBitmapFromPDF(mainActivityInterface.getSong().getFolder(),
                                mainActivityInterface.getSong().getFilename(),position,availableScreenWidth,
                                availableScreenHeight - infoHeight - alertHeight,"Y");
                    } else if (image) {
                        bitmap = mainActivityInterface.getProcessSong().getBitmapFromUri(
                                mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                                        mainActivityInterface.getSong().getFolder(),
                                        mainActivityInterface.getSong().getFilename()),
                                0,0);
                    } else if (imageslide) {
                        String[] bits = mainActivityInterface.getSong().getUser3().trim().split("\n");
                        if (bits.length>0 && bits.length>position) {
                            bitmap = mainActivityInterface.getProcessSong().getBitmapFromUri(Uri.parse(bits[position]),0,0);
                        } else {
                            bitmap = null;
                        }

                    } else {
                        bitmap = null;
                    }

                    if (!image && !pdf && !imageslide &&
                            secondaryViews!=null && secondaryViews.size()>position &&
                            secondaryViews.get(position)!=null &&
                            secondaryViews.get(position).getParent()!=null) {
                        ((ViewGroup)secondaryViews.get(position).getParent()).removeView(
                                secondaryViews.get(position));
                    }

                    if (!myView.songContent1.getIsDisplaying()) {
                        myView.songContent1.clearViews();
                        if (image || pdf || imageslide) {
                            myView.songContent1.getCol1().setVisibility(View.GONE);
                            myView.songContent1.getCol2().setVisibility(View.GONE);
                            myView.songContent1.getCol3().setVisibility(View.GONE);
                            myView.songContent1.getImageView().setVisibility(View.VISIBLE);
                            fixGravity(myView.songContent1.getImageView());
                            Glide.with(c).load(bitmap).fitCenter().into(myView.songContent1.getImageView());

                        } else {
                            myView.songContent1.getCol1().setVisibility(View.VISIBLE);
                            myView.songContent1.getImageView().setVisibility(View.GONE);
                            myView.songContent1.getCol1().addView(secondaryViews.get(position));
                        }
                        myView.songContent1.setIsDisplaying(true);
                        myView.songContent2.setIsDisplaying(false);
                        crossFadeContent(myView.songContent2, myView.songContent1);

                    } else if (!myView.songContent2.getIsDisplaying()) {
                        myView.songContent2.clearViews();
                        if (image || pdf || imageslide) {
                            myView.songContent2.getCol1().setVisibility(View.GONE);
                            myView.songContent2.getCol2().setVisibility(View.GONE);
                            myView.songContent2.getCol3().setVisibility(View.GONE);
                            myView.songContent2.getImageView().setVisibility(View.VISIBLE);
                            fixGravity(myView.songContent2.getImageView());
                            Glide.with(c).load(bitmap).fitCenter().into(myView.songContent2.getImageView());

                        } else {
                            myView.songContent2.getCol1().setVisibility(View.VISIBLE);
                            myView.songContent2.getImageView().setVisibility(View.GONE);
                            myView.songContent2.getCol1().addView(secondaryViews.get(position));
                        }
                        myView.songContent1.setIsDisplaying(false);
                        myView.songContent2.setIsDisplaying(true);
                        crossFadeContent(myView.songContent1, myView.songContent2);
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "No song section at this point.");
        }
    }

    private void fixGravity(ImageView imageView) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)imageView.getLayoutParams();
        switch (mainActivityInterface.getPresenterSettings().getPresoLyricsAlign()) {
            case Gravity.START:
            case Gravity.LEFT:
                lp.gravity = Gravity.START;
                break;
            case Gravity.END:
            case Gravity.RIGHT:
                lp.gravity = Gravity.END;
                break;
            case Gravity.CENTER:
            case Gravity.CENTER_HORIZONTAL:
            default:
                lp.gravity = Gravity.CENTER_HORIZONTAL;
                break;
        }
        switch (mainActivityInterface.getPresenterSettings().getPresoLyricsVAlign()) {
            case Gravity.TOP:
                lp.gravity = Gravity.TOP;
                break;
            case Gravity.BOTTOM:
                lp.gravity = Gravity.BOTTOM;
                break;
            case Gravity.CENTER_VERTICAL:
            case Gravity.CENTER:
                lp.gravity = Gravity.CENTER_VERTICAL;
                break;
        }
        lp.width = MATCH_PARENT;
        lp.height = MATCH_PARENT;

        imageView.setLayoutParams(lp);
    }
    private void showAllSections() {
        // Available height needs to remember to leave space for the infobar which is always visible in this mode
        // The bar height is constant
        int infoHeight = Math.max(myView.songProjectionInfo1.getViewHeight(),myView.songProjectionInfo2.getViewHeight());
        int modeHeight = availableScreenHeight - infoHeight;
        isNewSong = true;
        if (!myView.songContent1.getIsDisplaying()) {
             mainActivityInterface.getProcessSong().addViewsToScreen(
                    secondaryViews, secondaryWidths, secondaryHeights, myView.allContent,
                    myView.songContent1, null, availableScreenWidth, modeHeight,
                    myView.songContent1.getCol1(), myView.songContent1.getCol2(),
                    myView.songContent1.getCol3(), true, displayMetrics);

            ViewGroup.LayoutParams lp = myView.songContent1.getLayoutParams();
            lp.width = MATCH_PARENT;
            lp.height = modeHeight;
            myView.songContent1.setLayoutParams(lp);
            myView.songContent1.setIsDisplaying(true);
            myView.songContent2.setIsDisplaying(false);
            crossFadeContent(myView.songContent2,myView.songContent1);
            checkSongInfoShowHide();

        } else if (!myView.songContent2.getIsDisplaying()) {
            mainActivityInterface.getProcessSong().addViewsToScreen(
                    secondaryViews, secondaryWidths, secondaryHeights, myView.allContent,
                    myView.songContent2, null, availableScreenWidth, modeHeight,
                    myView.songContent2.getCol1(), myView.songContent2.getCol2(),
                    myView.songContent2.getCol3(), true, displayMetrics);

            ViewGroup.LayoutParams lp = myView.songContent2.getLayoutParams();
            lp.width = MATCH_PARENT;
            lp.height = modeHeight;
            myView.songContent2.setLayoutParams(lp);
            myView.songContent2.setIsDisplaying(true);
            myView.songContent1.setIsDisplaying(false);
            crossFadeContent(myView.songContent1,myView.songContent2);
            checkSongInfoShowHide();
        }
    }

    private void removeViewFromParent(View view) {
        if (view!=null && view.getParent()!=null) {
            ((ViewGroup)view.getParent()).removeView(view);
        }
    }
    @SuppressLint("RtlHardcoded")
    private void translateView(View view, int newWidth, int newHeight, int infoHeight, int alertHeight) {
        switch (mainActivityInterface.getPresenterSettings().getPresoLyricsAlign()) {
            case Gravity.START:
            case Gravity.LEFT:
                view.setTranslationX(0);
                break;
            case Gravity.END:
            case Gravity.RIGHT:
                view.setTranslationX(availableScreenWidth - newWidth);
                break;
            case Gravity.CENTER:
            case Gravity.CENTER_HORIZONTAL:
            default:
                view.setTranslationX((int)((availableScreenWidth - newWidth) / 2f));
                break;
        }
        switch (mainActivityInterface.getPresenterSettings().getPresoLyricsVAlign()) {
            case Gravity.TOP:
                view.setTranslationY(0);
                break;
            case Gravity.BOTTOM:
                view.setTranslationY(availableScreenHeight - infoHeight - alertHeight - newHeight);
                break;
            case Gravity.CENTER_VERTICAL:
            case Gravity.CENTER:
                view.setTranslationY((int) ((availableScreenHeight - infoHeight - alertHeight - newHeight) / 2f));
                break;
        }
    }

    // If we edited a view from PresenterMode via the bottom sheet for a song section
    public void editView() {
        // The view has been created, so put it here
        // Now update the create views for second screen presenting
        // Create a blank song with just this section
        Song tempSong = new Song();
        String lyrics = mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getNewContent();
        tempSong.setLyrics(lyrics);
        mainActivityInterface.getProcessSong().processSongIntoSections(tempSong,true);

        try {
            View newView = mainActivityInterface.getProcessSong().setSongInLayout(tempSong, false, true).get(0);
            // Replace the old view with this one once it has been measured etc.
            newView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    newView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // Get the sizes
                    int position = mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getSectionEdited();
                    secondaryWidths.set(position,newView.getMeasuredWidth());
                    secondaryHeights.set(position,newView.getMeasuredHeight());
                    // Remove the view from the test layout and add it to the array list
                    myView.testLayout.removeAllViews();
                    secondaryViews.set(position,newView);
                }
            });
            myView.testLayout.addView(newView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // The alert bar
    public void showAlert() {
        myView.alertBar.showAlert(mainActivityInterface);
    }
    public void updateAlert() {
        myView.alertBar.updateAlertSettings(mainActivityInterface);
    }

    // Deal with the display of song content
    private void crossFadeContent(View contentToFadeOut, View contentToFadeIn) {
        if (contentToFadeOut!=null) {
            mainActivityInterface.getCustomAnimation().faderAnimation(contentToFadeOut,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                    contentToFadeOut.getAlpha(), 0f);

        } else {
            Log.d(TAG,"contentToFadeOut==null");
        }

        if (contentToFadeIn!=null) {
            mainActivityInterface.getCustomAnimation().faderAnimation(contentToFadeIn,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                    0f, 1f);
        }
        else {
            Log.d(TAG,"contentToFadeIn==null");
        }
    }

    // Video
    private class MySurfaceTextureAvailable implements TextureView.SurfaceTextureListener {
        private final int which;
        MySurfaceTextureAvailable(int which) {
            this.which=which;
        }
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
            if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_presenter))) {
                if (which==1) {
                    surface1 = new Surface(surfaceTexture);
                    mediaPlayer1.setSurface(surface1);
                } else {
                    surface2 = new Surface(surfaceTexture);
                    mediaPlayer2.setSurface(surface2);
                }
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
        }
    }
    // Load the video ready to play
    private void loadVideo() {
        Uri uri;
        if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().equals("vid1")) {
            uri = mainActivityInterface.getPresenterSettings().getBackgroundVideo1();
        } else {
            uri = mainActivityInterface.getPresenterSettings().getBackgroundVideo2();
        }
        String uriString = mainActivityInterface.getStorageAccess().fixUriToLocal(uri);
        uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(uriString);
        if (uri!=null && mainActivityInterface.getStorageAccess().uriExists(uri)) {
            try {
                if (showWhichVideo<2) {
                    if (mediaPlayer1 != null) {
                        mediaPlayer1.reset();
                    } else {
                        mediaPlayer1 = new MediaPlayer();
                        mediaPlayer1.setLooping(true);
                        mediaPlayer1.setOnPreparedListener(new MyPreparedListener());
                    }
                    mediaPlayer1.setDataSource(c, uri);
                    mediaPlayer1.prepareAsync();
                } else {
                    if (mediaPlayer2!=null) {
                        mediaPlayer2.reset();
                    } else {
                        mediaPlayer2 = new MediaPlayer();
                        mediaPlayer2.setLooping(true);
                        mediaPlayer2.setOnPreparedListener(new MyPreparedListener());
                    }
                    mediaPlayer2.setDataSource(c, uri);
                    mediaPlayer2.prepareAsync();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private class MyPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            // Play and fade in the video
            if (showWhichVideo < 2) {
                mediaPlayer1.start();
                new Handler().postDelayed(() -> {
                    if (mediaPlayer2 != null && mediaPlayer2.isPlaying()) {
                        mediaPlayer2.stop();
                        mediaPlayer2.reset();
                    }
                }, mainActivityInterface.getPresenterSettings().getPresoTransitionTime());
                showWhichVideo = 2;
            } else {
                mediaPlayer2.start();
                new Handler().postDelayed(() -> {
                    if (mediaPlayer1 != null && mediaPlayer1.isPlaying()) {
                        mediaPlayer1.stop();
                        mediaPlayer1.reset();
                    }
                }, mainActivityInterface.getPresenterSettings().getPresoTransitionTime());
                showWhichVideo = 1;
            }
            crossFadeBackgrounds();
        }
    }
}

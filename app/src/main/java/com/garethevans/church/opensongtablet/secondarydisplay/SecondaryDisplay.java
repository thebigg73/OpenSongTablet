package com.garethevans.church.opensongtablet.secondarydisplay;
// TODO
// Info bar not working
// Clicking on the first section crossfades with a blank view?
// Clicking on a section when logo is on shouldn't start the timer, but a logo off with content should

import android.app.Presentation;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.databinding.CastScreenBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.Timer;
import java.util.TimerTask;

// This contains all of the screen mirroring logic (previously PresentationCommon and Presentation)
// All common preferences are stored in the PresentationSettings class
// Colors are stored in ThemeColors class

public class SecondaryDisplay extends Presentation {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final Display display;
    private CastScreenBinding myView;
    private final String TAG = "SecondaryDisplay";

    // Default variables
    private float scaleChords, scaleHeadings, scaleComments, lineSpacing;
    private Timer waitUntilTimer;
    private TimerTask waitUntilTimerTask;
    private int showWhich = 0;
    private int showWhichInfo = 0;
    private int showWhichBackground = 0;  //0=not set, 1=image1, 2=image2, 3=surface1, 4=surface2
    private int showWhichVideo = 0;
    private final int logoSplashTime = 3000;
    private int defaultPadding, availableScreenWidth, availableScreenHeight, horizontalSize, verticalSize,
            availableWidth_1col, availableWidth_2col, availableWidth_3col;

    private boolean firstRun = true, waitForVideo, forceCastUpdate, infoBarChangeRequired,
            infoBarRequiredTime=false, infoBarRequireInitial=true, trimLines, trimSections,
            addSectionSpace, boldChordHeading, displayChords;

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

        // Initialise the video surfaces
        initialiseVideoSurfaces();

        // Initialise view visibilities
        intialiseViewVisibity();

        // Set the info bars to match the mode
        setInfoStyles();
        changeInfoAlignment();

        // Set the page background color based on the mode
        updatePageBackgroundColor();

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
        setViewAlreadyFadedOut(myView.textureView1);
        setViewAlreadyFadedOut(myView.textureView1);
        setViewAlreadyFadedOut(myView.mainLogo);
        setViewAlreadyFadedOut(myView.songProjectionInfo1);
        setViewAlreadyFadedOut(myView.songProjectionInfo2);
        setViewAlreadyFadedOut(myView.alertBar);
    }
    private void setViewAlreadyFadedOut(View v) {
        // For a fade in animation to work, the view should be GONE and 0f alpha
        v.setVisibility(View.GONE);
        v.setAlpha(0f);
    }
    public void updatePageBackgroundColor() {
        if (mainActivityInterface.getMode().equals("Presenter")) {
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
    private void moveToNextSongView() {
        // This allows cross fading of views by getting a reference to the next view
        // These are songContent1 or songContent2
        if (showWhich<2) {
            showWhich = 2;
        } else {
            showWhich = 1;
        }
    }
    private void moveToNextSongInfoView() {
        // This allows cross fading of bottom bar info by getting a reference to the next view
        // These are songProjectionInfo1 or songProjectionInfo2
        if (showWhichInfo<2) {
            showWhichInfo = 2;
        } else {
            showWhichInfo = 1;
        }
    }
    private boolean canShowSong() {
        // Determines if we are allows to fade in content (no logo or blankscreen)
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
        myView.pageHolder.setRotation(mainActivityInterface.getPresenterSettings().getCastRotation());
        myView.mainLogo.setRotation(mainActivityInterface.getPresenterSettings().getCastRotation());
        if (mainActivityInterface.getPresenterSettings().getCastRotation() == 90.0f ||
                mainActivityInterface.getPresenterSettings().getCastRotation() == 270.0f) {
            // Switch width for height and vice versa
            horizontalSize = metrics.heightPixels;
            verticalSize = metrics.widthPixels;
        } else {
            horizontalSize = metrics.widthPixels;
            verticalSize = metrics.heightPixels;
        }

        // Available size has to take into consideration any padding
        availableScreenWidth = horizontalSize - (2 * mainActivityInterface.getPresenterSettings().getPresoXMargin());
        availableScreenHeight = verticalSize - (2 * mainActivityInterface.getPresenterSettings().getPresoYMargin());

        availableWidth_1col = availableScreenWidth;
        availableWidth_2col = (int) ((float) availableScreenWidth / 2.0f);
        availableWidth_3col = (int) ((float) availableScreenWidth / 3.0f);

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
            if (mainActivityInterface.getMode().equals("Presenter")) {
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

            } else {
                // Prepare the current section 0 ready for the logo hiding after splash
                new Handler().postDelayed(()-> showSection(0),logoSplashTime);
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


    // Set views depending on mode
    public void matchPresentationToMode() {
        // Get the settings that are appropriate.  This is called on first run
        switch (mainActivityInterface.getMode()) {
            case "Stage":
            case "Performance":
            default:
                scaleHeadings = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "scaleHeadings", 0.8f);
                scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "scaleComments", 0.8f);
                displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "displayChords", true);
                boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "boldChordHeading", false);
                break;

            case "Presenter":
                scaleHeadings = 0.0f;
                scaleComments = 0.0f;
                displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "presoShowChords", false);
                boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "presoLyricsBold", false);
                break;
        }
        infoBarChangeRequired = true;
        forceCastUpdate = false;
    }


    // The screen background
    public void changeBackground() {
        Log.d(TAG,"changeBackground()");
        // There has been an update to the user's background or logo, so pull them in from preferences
        // (already updated in PresenterSettings)
        // This only runs in PresenterMode!  Performance/Stage Mode reflect the device theme
        if (mainActivityInterface.getMode().equals("Presenter")) {
            // We can use either a drawable (for a solid colour) or a uri (for an image)
            // Get the current background to fade out and set the background to the next
            Log.d(TAG,"Fade out: showWhichBackground="+showWhichBackground);
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
                Log.d(TAG,"changeBackground() - solid color");

                assert backgroundToFadeIn instanceof ImageView;
                Drawable drawable = ContextCompat.getDrawable(c, R.drawable.simple_rectangle);
                if (drawable != null) {
                    GradientDrawable solidColor = (GradientDrawable) drawable.mutate();
                    solidColor.setSize(availableScreenWidth, availableScreenHeight);
                    solidColor.setColor(mainActivityInterface.getPresenterSettings().getBackgroundColor());
                    GlideApp.with(c).load(solidColor).apply(requestOptions).into((ImageView) backgroundToFadeIn);
                }
            } else if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("img")) {
                // Use a static image
                assert backgroundToFadeIn instanceof ImageView;
                if (background.toString().endsWith("ost_bg.png")) {
                    Drawable defaultImage = ResourcesCompat.getDrawable(c.getResources(), R.drawable.preso_default_bg, null);
                    GlideApp.with(c).load(defaultImage).apply(requestOptions).into((ImageView) backgroundToFadeIn);
                } else {
                    GlideApp.with(c).load(background).apply(requestOptions).into((ImageView) backgroundToFadeIn);
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
        Log.d(TAG,"crossFadeBackgrounds()");
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
        GlideApp.with(c).load(mainActivityInterface.getPresenterSettings().getLogo()).apply(requestOptions).into(myView.mainLogo);
    }
    public void showLogo(boolean show, boolean timedHide) {
        // Fade in/out the logo based on the setting
        if (show) {
            crossFadeContent(myView.allContent,myView.mainLogo);
        } else {
            crossFadeContent(myView.mainLogo,myView.allContent);
        }

        // Check for the song info
        checkSongInfoShowHide();

        if (timedHide) {
            // This will hide the logo after the logoSplashTime
            myView.mainLogo.postDelayed(() -> {
                mainActivityInterface.getPresenterSettings().setLogoOn(false);
                crossFadeContent(myView.mainLogo,myView.allContent);
                Log.d(TAG,"timed hiding of logo");
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
        } else {
            start = 0f;
            end = 1f;
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
        if ((start>end) || canShowSong()) {
            if (showWhich < 2) {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent1,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                        start, end);

            } else {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent2,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                        start, end);
            }

            // If we are fading out, or fading in but should show the info bar, do it
            if (start>end || infoBarRequireInitial || infoBarRequiredTime) {
                if (showWhichInfo < 2) {
                    mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo1,
                            mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                            start, end);
                } else {
                    mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo2,
                            mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                            start, end);
                }
            }
        }
    }


    // The song info bar
    // For Presenter Mode, the bar shows is required under the following conditions:
    // - The first time a song is displayed
    // - For at least the untilWaitTime has elapsed since first presented
    // - The alert view can still override if required
    private void setupTimers() {
        infoBarRequiredTime = true;
        waitUntilTimer = new Timer();
        waitUntilTimerTask = new TimerTask() {
            @Override
            public void run() {
                // Switch off the requirement for the infoBarRequiredTime and cancel the timer
                if (mainActivityInterface.getPresenterSettings().getHideInfoBar()) {
                    infoBarRequiredTime = false;
                    try {
                        waitUntilTimer.cancel();
                        waitUntilTimerTask.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "timer over - infoBarRequiredTime=" + infoBarRequiredTime);
                }
            }
        };
        int untilTimeWait = 20000;
        waitUntilTimer.schedule(waitUntilTimerTask, untilTimeWait);
    }

    public void setInfoBarRequired() {
        // This is called when a new song is loaded in the Presenter Mode
        // Once a view is displayed (fadeInOutSong), the timer starts and resets this to false after untilWaitTime
        if (waitUntilTimer!=null) {
            waitUntilTimer.cancel();
            waitUntilTimer = null;
        }
        if (waitUntilTimerTask!=null) {
            waitUntilTimerTask.cancel();
            waitUntilTimerTask = null;
        }
        infoBarRequireInitial = false;
        infoBarRequiredTime = true;
    }

    public void setInfoStyles() {
        myView.songProjectionInfo1.setupLayout(c,mainActivityInterface,false);
        myView.songProjectionInfo2.setupLayout(c,mainActivityInterface,false);
    }
    public void changeInfoAlignment() {
        myView.songProjectionInfo1.setAlign(mainActivityInterface.getPresenterSettings().getPresoInfoAlign());
        myView.songProjectionInfo2.setAlign(mainActivityInterface.getPresenterSettings().getPresoInfoAlign());
    }
    public void setSongInfo() {
        String title = mainActivityInterface.getSong().getTitle();
        String author = mainActivityInterface.getSong().getAuthor();
        String copyright = mainActivityInterface.getSong().getCopyright();
        if (title==null || title.isEmpty()) {
            title = mainActivityInterface.getSong().getFilename();
        }
        Log.d(TAG,"setSongInfo()  title="+title);
        // All info should be shown if available
        if (showWhichInfo<2) {
            myView.songProjectionInfo1.setSongTitle(title);
            myView.songProjectionInfo1.setSongAuthor(author);
            myView.songProjectionInfo1.setSongCopyright(copyright);

        } else {
            myView.songProjectionInfo2.setSongTitle(title);
            myView.songProjectionInfo2.setSongAuthor(author);
            myView.songProjectionInfo2.setSongCopyright(copyright);
        }

    }
    public void initialiseInfoBarRequired() {
        // This sets the info bar as being required for first time shown
        // Once we fade in a section for the first time, we swap these booleans and start the timer
        infoBarRequireInitial = true;
        infoBarRequiredTime = false;
    }
    public void checkSongInfoShowHide() {
        if (showWhichInfo<2) {
            songInfoShowCheck(myView.songProjectionInfo1);
            songInfoHideCheck(myView.songProjectionInfo1);
        } else {
            songInfoShowCheck(myView.songProjectionInfo2);
            songInfoHideCheck(myView.songProjectionInfo2);
        }
    }
    private void songInfoHideCheck(View songInfoView) {
        Log.d(TAG,"songInfoHideCheck()");
        // Fade out can only happen if we no longer require the song info bar
        if (!infoBarRequireInitial && !infoBarRequiredTime) {
            Log.d(TAG,"Should hide view:"+songInfoView+" 1:"+myView.songProjectionInfo1+" 2:"+myView.songProjectionInfo2);
            mainActivityInterface.getCustomAnimation().faderAnimation(songInfoView,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                    songInfoView.getAlpha(), 0f);
        }
        Log.d(TAG,"bottomTitle1:"+myView.songProjectionInfo1.getSongTitle());
        Log.d(TAG,"bottomTitle2:"+myView.songProjectionInfo2.getSongTitle());
    }
    private void songInfoShowCheck(View songInfoView) {
        Log.d(TAG,"songInfoShowCheck()");
        // If required (new song loaded and not already showing), show the info bar
        if (canShowSong() && infoBarRequireInitial && (myView.songContent1.getVisibility()==View.VISIBLE || myView.songContent2.getVisibility()==View.VISIBLE)) {
            Log.d(TAG,"Should show view:"+songInfoView+" 1:"+myView.songProjectionInfo1+" 2:"+myView.songProjectionInfo2);
            mainActivityInterface.getCustomAnimation().faderAnimation(songInfoView,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                    0f,1f);

            // Now remove the requirement for an inital view
            infoBarRequireInitial = false;

            // Set a timer required though for minimum display time
            setupTimers();
        }
    }


    // The song content
    public void setSongContent() {
        // Just like we do with the song processing, we draw the sections to the test layout
        // Then measure them, work out the best orientation and scaling
        // Then remove from the test layout and reattach to the song layout.

        // Clear any existing views from the test layout.  We don't fade out existing song layout until we are ready
        myView.testLayout.removeAllViews();

        // Decide if this is an XML, PDF or IMG file and proceed accordingly
        if (mainActivityInterface.getSong().getFiletype().equals("XML")) {
            mainActivityInterface.setSectionViews(null);
            setSectionViews();
        } else {
            // TODO deal with PDF and images!
        }
    }
    public void setSongContentPrefs() {
        trimLines = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "trimLines", false);
        trimSections = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "trimSections", false);
        addSectionSpace = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "addSectionSpace", true);
        boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "boldChordHeading", false);
        scaleChords = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "scaleChords", 0.8f);
    }
    public void showSection(final int position) {
        // Decide which view to show to
        // Check the view isn't already attached to a parent
        if (position>=0 && position<mainActivityInterface.getSectionViews().size()) {
            if (mainActivityInterface.getSectionViews().get(position).getParent() != null) {
                ((ViewGroup) mainActivityInterface.getSectionViews().get(position).getParent()).removeView(mainActivityInterface.getSectionViews().get(position));
            }
            // Move to next view showWhich 1>2, 2>1
            moveToNextSongView();
            // Set a listener to wait for drawing, then measure and scale
            ViewTreeObserver viewTreeObserver = myView.testLayout.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Measure the height of the layouts
                    int width = mainActivityInterface.getSectionViews().get(position).getMeasuredWidth();
                    int height = mainActivityInterface.getSectionViews().get(position).getMeasuredHeight();

                    // Get the measured height of the song info bar
                    int infoHeight;
                    if (showWhichInfo<2) {
                        infoHeight = myView.songProjectionInfo1.getViewHeight();
                    } else {
                        infoHeight = myView.songProjectionInfo2.getViewHeight();
                    }
                    int alertHeight = myView.alertBar.getViewHeight();

                    float max_x = (float) availableScreenWidth / (float) width;
                    float max_y = (float) (availableScreenHeight - infoHeight - alertHeight) / (float) height;

                    float best = Math.min(max_x, max_y);
                    if (best > (mainActivityInterface.getPresenterSettings().getFontSizePresoMax() / 14f)) {
                        best = mainActivityInterface.getPresenterSettings().getFontSizePresoMax() / 14f;
                    }

                    mainActivityInterface.getSectionViews().get(position).setPivotX(0f);
                    mainActivityInterface.getSectionViews().get(position).setPivotY(0f);
                    mainActivityInterface.getSectionViews().get(position).setScaleX(best);
                    mainActivityInterface.getSectionViews().get(position).setScaleY(best);

                    // Remove from the test layout
                    myView.testLayout.removeAllViews();

                    // Remove this listener
                    viewTreeObserver.removeOnGlobalLayoutListener(this);

                    // We can now prepare the new view and animate in/out the views as long as the logo is off
                    // and the blank screen isn't on
                    Log.d(TAG,"showWhich="+showWhich+"  canShowSong()="+canShowSong());
                    removeViewFromParent(mainActivityInterface.getSectionViews().get(position));

                    // Translate the scaled views based on the alignment
                    int newWidth = (int)(width * best);
                    int newHeight = (int)(height * best);
                    translateView(mainActivityInterface.getSectionViews().get(position), newWidth, newHeight, infoHeight, alertHeight);

                    if (showWhich < 2) {
                        myView.songContent1.removeAllViews();
                        myView.songContent1.addView(mainActivityInterface.getSectionViews().get(position));
                        crossFadeContent(myView.songContent2, myView.songContent1);

                    } else {
                        myView.songContent2.removeAllViews();
                        myView.songContent2.addView(mainActivityInterface.getSectionViews().get(position));
                        crossFadeContent(myView.songContent1, myView.songContent2);

                    }

                    // Check the song info status
                    checkSongInfoShowHide();
                }
            });
            myView.testLayout.addView(mainActivityInterface.getSectionViews().get(position));
        }
    }
    private void setSectionViews() {

        mainActivityInterface.setSectionViews(mainActivityInterface.getProcessSong().
                setSongInLayout(c, mainActivityInterface, mainActivityInterface.getSong().getLyrics(),
                        false, true));

        // Draw them to the screen test layout for measuring
        ViewTreeObserver testObs = myView.testLayout.getViewTreeObserver();
        testObs.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // The views are ready so prepare to create the song page
                for (View v:mainActivityInterface.getSectionViews())  {
                    int width = v.getMeasuredWidth();
                    int height = v.getMeasuredHeight();
                    mainActivityInterface.addSectionSize(width,height);
                }

                // Calculate the scale factor for each section individually
                // For each meausured view, get the max x and y scale value
                // Check they are less than the max preferred value
                for (int x=0; x<mainActivityInterface.getSectionViews().size(); x++) {
                    float max_x = (float)horizontalSize/(float)mainActivityInterface.getSectionWidths().get(x);
                    float max_y = (float)verticalSize/(float)mainActivityInterface.getSectionHeights().get(x);
                    // The text size is 14sp by default.  Compare this to the pref
                    float best = Math.min(max_x,max_y);
                    if (best*14f > mainActivityInterface.getPresenterSettings().getFontSizePresoMax()) {
                        best = mainActivityInterface.getPresenterSettings().getFontSizePresoMax()*14f;
                    }
                    mainActivityInterface.getSectionViews().get(x).setPivotX(0f);
                    mainActivityInterface.getSectionViews().get(x).setPivotY(0f);
                    if (best>0) {
                        mainActivityInterface.getSectionViews().get(x).setScaleX(best);
                        mainActivityInterface.getSectionViews().get(x).setScaleY(best);
                    }
                }

                // We can now remove the views from the test layout and remove this listener
                myView.testLayout.removeAllViews();
                myView.testLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });
        for (View view:mainActivityInterface.getSectionViews()) {
            myView.testLayout.addView(view);
        }

    }
    private void removeViewFromParent(View view) {
        if (view.getParent()!=null) {
            String name = view.getParent().getClass().getName();

            if (name.contains("RelativeLayout")) {
                ((RelativeLayout) view.getParent()).removeAllViews();
            } else if (name.contains("LinearLayout")) {
                ((LinearLayout) view.getParent()).removeAllViews();
            }
        }
    }
    private void translateView(View view, int newWidth, int newHeight, int infoHeight, int alertHeight) {
        Log.d(TAG,"infoHeight="+infoHeight+"  alertHeight="+alertHeight);
        switch (mainActivityInterface.getPresenterSettings().getPresoLyricsAlign()) {
            case Gravity.START:
            case Gravity.LEFT:
                Log.d(TAG,"Start");
                view.setTranslationX(0);
                break;
            case Gravity.END:
            case Gravity.RIGHT:
                Log.d(TAG,"End");
                view.setTranslationX(availableScreenWidth-newWidth);
                break;
            case Gravity.CENTER:
            case Gravity.CENTER_HORIZONTAL:
                Log.d(TAG,"Center horizontal");
                view.setTranslationX((int)((availableScreenWidth-newWidth)/2f));
                break;
        }
        switch (mainActivityInterface.getPresenterSettings().getPresoLyricsVAlign()) {
            case Gravity.TOP:
                Log.d(TAG,"Top");
                view.setTranslationY(0);
                break;
            case Gravity.BOTTOM:
                Log.d(TAG,"Bottom");
                view.setTranslationY(availableScreenHeight-infoHeight-alertHeight-newHeight);
                break;
            case Gravity.CENTER_VERTICAL:
            case Gravity.CENTER:
                Log.d(TAG,"Center vertical");
                view.setTranslationY((int)((availableScreenHeight-infoHeight-alertHeight-newHeight)/2f));
                break;
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
    public void showPerformanceContent() {
        Log.d(TAG,"showPerformanceContent()");
        myView.songProjectionInfo1.setVisibility(View.VISIBLE);
        myView.songProjectionInfo1.setAlpha(1.0f);
        Log.d(TAG,"title="+myView.songProjectionInfo1.getSongTitle());
    }
    public void showPresenterContent() {

    }
    private void crossFadeContent(View contentToFadeOut, View contentToFadeIn) {
        Log.d(TAG,"crossFadeContent()");
        // Fade out is always fine, so do it
        if (contentToFadeOut!=null) {
            Log.d(TAG,"Fading out view");
            mainActivityInterface.getCustomAnimation().faderAnimation(contentToFadeOut,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                    contentToFadeOut.getAlpha(), 0f);
        }

        // Fade in of a content is fine
        if (contentToFadeIn!=null) {
            Log.d(TAG, "Fading in view");
            mainActivityInterface.getCustomAnimation().faderAnimation(contentToFadeIn,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                    0f, 1f);
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
            if (mainActivityInterface.getMode().equals("Presenter")) {
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
        Log.d(TAG,"uri="+uri);
        String uriString = mainActivityInterface.getStorageAccess().fixUriToLocal(uri);
        Log.d(TAG,"uriString:"+uriString);
        uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(c,mainActivityInterface,uriString);
        if (uri!=null && mainActivityInterface.getStorageAccess().uriExists(c,uri)) {
            Log.d(TAG,"uriExists");
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
            Log.d(TAG, "mp=" + mp + "  mediapPlayer1=" + mediaPlayer1 + "  mediaPlayer2=" + mediaPlayer2);
            Log.d(TAG, "showWhichVideo" + showWhichVideo);
            if (showWhichVideo < 2) {
                Log.d(TAG, "mediaPlayer1 starting...");
                mediaPlayer1.start();
                Log.d(TAG, "duration: " + mediaPlayer1.getDuration());
                new Handler().postDelayed(() -> {
                    if (mediaPlayer2 != null && mediaPlayer2.isPlaying()) {
                        mediaPlayer2.stop();
                        mediaPlayer2.reset();
                    }
                }, mainActivityInterface.getPresenterSettings().getPresoTransitionTime());
                showWhichVideo = 2;
            } else {
                Log.d(TAG, "mediaPlayer2 starting...");
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

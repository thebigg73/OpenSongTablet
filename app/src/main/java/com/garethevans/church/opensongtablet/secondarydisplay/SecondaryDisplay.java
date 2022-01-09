package com.garethevans.church.opensongtablet.secondarydisplay;

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
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

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
    private boolean trimLines, trimSections, addSectionSpace, boldChordHeading, displayChords;
    private float scaleChords, scaleHeadings, scaleComments, lineSpacing;

    private int defaultPadding, availableScreenWidth, availableScreenHeight, horizontalSize, verticalSize,
            availableWidth_1col, availableWidth_2col, availableWidth_3col;

    private Timer waitUntilTimer;
    private TimerTask waitUntilTimerTask;
    private long infoBarUntilTime, lyricAfterTime;
    private int showWhich = 0;
    private int showWhichInfo = 0;
    private int showWhichBackground = 0;  //0=not set, 1=image1, 2=image2, 3=surface1, 4=surface2
    private int showWhichVideo = 0;
    private final int logoSplashTime = 3000, panicTimeExtra = 1000, untilTimeWait=10000;

    boolean infoBarRequired;
    boolean infoBarChangeRequired;
    boolean forceCastUpdate;
    boolean panicRequired;
    boolean doUpdateActive;
    boolean infoBarAlertState;
    boolean animateOutActive;
    boolean isVideo;
    boolean isPresenting;
    boolean waitForVideo;

    MediaPlayer mediaPlayer1=new MediaPlayer(), mediaPlayer2=new MediaPlayer();
    View viewToFadeIn, viewToFadeOut;
    SurfaceTexture surfaceTexture1, surfaceTexture2;
    Surface surface1, surface2;
    Uri vidUri, uriToLoad;

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

        // Change the bottom bar set up to match the mode
        setInfoBarToMode();

        // Set the page background color based on the mode
        updatePageBackgroundColor();

        // Set the info bar background colour (a separate layer) based on the mode
        updateInfoBarColor();

        // Set the bottom bar font and alignment
        setInfoStyles();
        changeInfoAlignment();

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
    private void moveToNextView() {
        if (showWhich<2) {
            showWhich = 1;
        } else {
            showWhich = 2;
        }
    }
    private boolean canShowSong() {
        return !mainActivityInterface.getPresenterSettings().getLogoOn() &&
                !mainActivityInterface.getPresenterSettings().getBlankscreenOn();
    }


    // Now the screen settings
    public void setScreenSizes() {
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);

        // We need to wait until the view is prepared before flipping
        ViewTreeObserver viewTreeObserver = myView.pageHolder.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                myView.pageHolder.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                myView.pageHolder.setRotation(mainActivityInterface.getPresenterSettings().getCastRotation());
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
                availableScreenWidth = horizontalSize - (2*mainActivityInterface.getPresenterSettings().getPresoXMargin());
                availableScreenHeight = verticalSize - (2*mainActivityInterface.getPresenterSettings().getPresoYMargin());

                availableWidth_1col = availableScreenWidth;
                availableWidth_2col = (int) ((float) availableScreenWidth / 2.0f);
                availableWidth_3col = (int) ((float) availableScreenWidth / 3.0f);

                // These bits are dependent on the screen size
                changeMargins();
                changeLogo();
                mainActivityInterface.getPresenterSettings().setHideLogoAfterShow(!mainActivityInterface.getMode().equals("Presenter"));
                mainActivityInterface.getPresenterSettings().setLogoOn(true);
                showLogo();
                matchPresentationToMode();

                setInitialView();
                setSongInfo();
                setSongContent();
            }
        });
        myView.pageHolder.setRotation(mainActivityInterface.getPresenterSettings().getCastRotation());
        setContentView(myView.getRoot());
    }
    private void changeMargins() {
        myView.pageHolder.setPadding(mainActivityInterface.getPresenterSettings().getPresoXMargin(),
                mainActivityInterface.getPresenterSettings().getPresoYMargin(),
                mainActivityInterface.getPresenterSettings().getPresoXMargin(),
                mainActivityInterface.getPresenterSettings().getPresoYMargin());
    }


    // Set views depending on mode
    public void matchPresentationToMode() {
        trimLines = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "trimLines", false);
        trimSections = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "trimSections", false);
        addSectionSpace = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "addSectionSpace", true);
        scaleChords = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "scaleChords", 0.8f);
        lineSpacing = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "lineSpacing", 0.1f);

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
    public void setInitialView() {
        // If we are in Performance or Stage mode, hide the logo after the splash time
        switch (mainActivityInterface.getMode()) {
            case "Performance":
            case "Stage":
                myView.mainLogo.postDelayed(() -> {
                    mainActivityInterface.getPresenterSettings().setHideLogoAfterShow(false);
                    mainActivityInterface.getPresenterSettings().setLogoOn(false);
                    showLogo();
                    mainActivityInterface.getPresenterSettings().setLogoOn(false);
                }, logoSplashTime);
                break;
        }
    }


    // The screen background
    public void changeBackground() {
        // There has been an update to the user's background or logo, so pull them in from preferences
        // (already updated in PresenterSettings)
        // This only runs in PresenterMode!  Performance/Stage Mode reflect the device theme
        if (mainActivityInterface.getMode().equals("Presenter")) {
            Log.d(TAG,"changeBackground()");
            // We can use either a drawable (for a solid colour) or a uri (for an image)
            // We decide which isn't null
            // Get the current background to fade out and set the background to the next
            Log.d(TAG,"Fade out: showWhichBackground="+showWhichBackground);
            viewToFadeOut = null;
            if (showWhichBackground==1) {
                viewToFadeOut = myView.backgroundImage1;
            } else if (showWhichBackground==2) {
                viewToFadeOut = myView.backgroundImage2;
            } else if (showWhichBackground==3) {
                viewToFadeOut = myView.textureView1;
            } else if (showWhichBackground==4) {
                viewToFadeOut = myView.textureView2;
            }

            // If this is the first time, showWhichBackground==0
            viewToFadeIn = null;
            waitForVideo = false;
            if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("img") ||
                    mainActivityInterface.getPresenterSettings().getBackgroundToUse().equals("color")) {
                if (showWhichBackground == 1) {
                    showWhichBackground = 2;
                    viewToFadeIn = myView.backgroundImage2;
                } else {
                    showWhichBackground = 1;
                    viewToFadeIn = myView.backgroundImage1;
                }
            } else if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("vid")) {
                if (showWhichBackground == 3) {
                    showWhichBackground = 4;
                    viewToFadeIn = myView.textureView2;
                } else {
                    showWhichBackground = 3;
                    viewToFadeIn = myView.textureView1;
                }
            }
            Log.d(TAG,"Fade in: showWhichBackground="+showWhichBackground);


            // Set the current background to the new one
            Uri background = mainActivityInterface.getPresenterSettings().getChosenBackground();
            RequestOptions requestOptions = new RequestOptions().centerCrop();

            if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().equals("color") ||
                    (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("img") && background == null)) {
                // Use a solid background color
                assert viewToFadeIn instanceof ImageView;
                Drawable drawable = ContextCompat.getDrawable(c, R.drawable.simple_rectangle);
                if (drawable != null) {
                    GradientDrawable solidColor = (GradientDrawable) drawable.mutate();
                    solidColor.setSize(availableScreenWidth, availableScreenHeight);
                    solidColor.setColor(mainActivityInterface.getPresenterSettings().getBackgroundColor());
                    GlideApp.with(c).load(solidColor).apply(requestOptions).into((ImageView) viewToFadeIn);
                }
            } else if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("img")) {
                // Use a static image
                assert viewToFadeIn instanceof ImageView;
                if (background.toString().endsWith("ost_bg.png")) {
                    Drawable defaultImage = ResourcesCompat.getDrawable(c.getResources(), R.drawable.preso_default_bg, null);
                    GlideApp.with(c).load(defaultImage).apply(requestOptions).into((ImageView) viewToFadeIn);
                } else {
                    GlideApp.with(c).load(background).apply(requestOptions).into((ImageView) viewToFadeIn);
                }
            } else if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("vid")) {
                waitForVideo = true;
            }

            // Now send the animations to fade out the current (if not waiting for a video)
            if (!waitForVideo) {
                crossFadeViews();
            } else {
                if (showWhichVideo < 2) {
                    viewToFadeIn = myView.textureView1;
                } else {
                    viewToFadeIn = myView.textureView2;
                }
                loadVideo();
            }
        }
    }
    private void crossFadeViews() {
        Log.d(TAG,"crossFadeViews()");
        mainActivityInterface.getCustomAnimation().faderAnimation(viewToFadeOut,
                mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                mainActivityInterface.getPresenterSettings().getPresoBackgroundAlpha(), 0f);
        mainActivityInterface.getCustomAnimation().faderAnimation(viewToFadeIn,
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
    public void showLogo() {
        // Fade in/out the logo based on the setting
        float start;
        float end;
        if (mainActivityInterface.getPresenterSettings().getLogoOn()) {
            start = 0f;
            end = 1f;
        } else {
            start = 1f;
            end = 0f;
        }
        mainActivityInterface.getCustomAnimation().faderAnimation(myView.mainLogo,
                mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                start,end);

        // Do the opposite of the logo for the content and bottom bar
        fadeInOutSong(!mainActivityInterface.getPresenterSettings().getLogoOn(), true, true);

        if (mainActivityInterface.getPresenterSettings().getHideLogoAfterShow()) {
            // This will hide the logo after the logoSplashTime
            myView.mainLogo.postDelayed(() -> {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.mainLogo,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),1f,0f);
                fadeInOutSong(true,true,true);
                Log.d(TAG,"timed hiding of logo");
                mainActivityInterface.getPresenterSettings().setLogoOn(false);
                mainActivityInterface.getPresenterSettings().setHideLogoAfterShow(false);
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
        if ((start>end) || canShowSong()) {
            if (showWhich < 2) {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent1,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                        start, end);
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo1,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                        start, end);
            } else {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent2,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                        start, end);
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo2,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                        start, end);
            }
        }
    }


    // The song info bar
    // For Presenter Mode, the bar shows is required under the following conditions:
    // - The first time a song is displayed
    // - For at least the untilWaitTime has elapsed since first presented
    // - The alert view can still override if required
    private void setupTimers() {
        waitUntilTimer = new Timer();
        waitUntilTimerTask = new TimerTask() {
            @Override
            public void run() {
                // Switch off the requirement for the infoBar and cancel the timer
                infoBarRequired = false;
                try {
                    waitUntilTimer.cancel();
                    waitUntilTimerTask.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        waitUntilTimer.schedule(waitUntilTimerTask,untilTimeWait);
    }
    private void setInfoBarToMode() {
        // For Performance/Stage mode, we want the small logo to be on in the bottom bar always
        // The alignment of the information should be left and default padding is on
        // Presenter mode can change the alignment and the small logo in the bottom bar is removed
        if (mainActivityInterface.getMode().equals("Presenter")) {
            myView.songProjectionInfo1.minifyLayout(false);
            myView.songProjectionInfo2.minifyLayout(false);
        } else {
            myView.songProjectionInfo1.minifyLayout(true);
            myView.songProjectionInfo2.minifyLayout(true);
        }
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
        infoBarRequired = true;
    }
    public void updateInfoBarColor() {
        int colorWithAlpha;
        if (mainActivityInterface.getMode().equals("Presenter")) {
            // Presenter mode uses the user preference, but with an adjustable alpha value
            // Convert the color to an colour including the alpha component
            colorWithAlpha = ColorUtils.setAlphaComponent(
                    mainActivityInterface.getMyThemeColors().getPresoShadowColor(),
                    (int) (mainActivityInterface.getPresenterSettings().getPresoInfoBarAlpha() * 255));
        } else {
            // Performance/Stage mode has an invisible bottom bar
            colorWithAlpha = 0x00ffffff;
        }
        myView.bottomBarBackground.setBackgroundColor(colorWithAlpha);
    }
    public void setInfoStyles() {
        myView.songProjectionInfo1.setupFonts(c, mainActivityInterface);
        myView.songProjectionInfo2.setupFonts(c, mainActivityInterface);
    }
    public void changeInfoAlignment() {
        myView.songProjectionInfo1.setAlign(mainActivityInterface.getPresenterSettings().getPresoInfoAlign());
        myView.songProjectionInfo2.setAlign(mainActivityInterface.getPresenterSettings().getPresoInfoAlign());
    }
    public void setSongInfo() {
        // Change the current showInfo
        if (showWhichInfo==1) {
            showWhichInfo = 2;
        } else {
            showWhichInfo = 1;
        }

        // All info should be shown if available
        if (showWhichInfo==1) {
            myView.songProjectionInfo1.setSongTitle(mainActivityInterface.getSong().getTitle());
            myView.songProjectionInfo1.setSongAuthor(mainActivityInterface.getSong().getAuthor());
            myView.songProjectionInfo1.setSongCopyright(mainActivityInterface.getSong().getCopyright());

        } else {
            myView.songProjectionInfo2.setSongTitle(mainActivityInterface.getSong().getTitle());
            myView.songProjectionInfo2.setSongAuthor(mainActivityInterface.getSong().getAuthor());
            myView.songProjectionInfo2.setSongCopyright(mainActivityInterface.getSong().getCopyright());
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

        Log.d(TAG,"showSection(): position="+position);

        // Check the view isn't already attached to a parent
        if (position>=0 && position<mainActivityInterface.getSectionViews().size()) {
            if (mainActivityInterface.getSectionViews().get(position).getParent() != null) {
                ((ViewGroup) mainActivityInterface.getSectionViews().get(position).getParent()).removeView(mainActivityInterface.getSectionViews().get(position));
            }
            // Move to next view showWhich 1>2, 2>1
            moveToNextView();
            // Set a listener to wait for drawing, then measure and scale
            ViewTreeObserver viewTreeObserver = myView.testLayout.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Measure the height of the layouts
                    int width = mainActivityInterface.getSectionViews().get(position).getMeasuredWidth();
                    int height = mainActivityInterface.getSectionViews().get(position).getMeasuredHeight();

                    float max_x = (float) availableScreenWidth / (float) width;
                    float max_y = (float) availableScreenHeight / (float) height;

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
                    if (showWhich < 2) {
                        myView.songContent1.removeAllViews();
                        myView.songContent1.addView(mainActivityInterface.getSectionViews().get(position));
                        if (canShowSong()) {
                            mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent1,
                                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                                    0f, 1f);
                            mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent2,
                                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                                    1f,0f);
                        }
                    } else {
                        myView.songContent2.removeAllViews();
                        myView.songContent2.addView(mainActivityInterface.getSectionViews().get(position));
                        if (canShowSong()) {
                            mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent2,
                                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                                    0f,1f);
                            mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent1,
                                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                                    1f,0f);
                        }
                    }
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

                if (showWhich==0) {
                    // First run, so set the first view
                    if (mainActivityInterface.getSectionViews().size()>0) {
                        showSection(0);
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
    private void fadeInOutSong(boolean fadeIn, boolean content, boolean bottomBar) {
        if (content && (waitUntilTimer==null || waitUntilTimerTask==null)) {
            // We are showing the content for the first time.
            // Create the timer which counts down how long the info bar is required for
            setupTimers();
        }
        float start;
        float end;
        if (fadeIn) {
            start = 0f;
            end = 1f;
        } else {
            start = 1f;
            end = 0f;
        }
        if (!fadeIn || canShowSong()) {
            if (showWhich < 2 && content) {

                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent1,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), start, end);
            } else if (content) {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent2,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), start, end);
            }

            if (showWhichInfo < 2 && bottomBar) {
                myView.songProjectionInfo1.setInfoBarRequired(infoBarRequired,
                        mainActivityInterface.getPresenterSettings().getAlertOn());
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo1,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), start, end);
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.bottomBarBackground,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), start, end);
            } else if (bottomBar) {
                myView.songProjectionInfo2.setInfoBarRequired(infoBarRequired,
                        mainActivityInterface.getPresenterSettings().getAlertOn());
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo2,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), start, end);
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.bottomBarBackground,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), start, end);
            }
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
                //Surface surface = new Surface(surfaceTexture);
                if (which==1) {
                //if (surfaceTexture == myView.textureView1.getSurfaceTexture()) {
                    Log.d(TAG, "textureView1: 1");
                    surface1 = new Surface(surfaceTexture);
                    mediaPlayer1.setSurface(surface1);
                } else {
                    Log.d(TAG, "textureView2: 2");
                    surface2 = new Surface(surfaceTexture);
                    mediaPlayer2.setSurface(surface2);
                }
            }
            Log.d(TAG, "surface1:" + myView.textureView1.getSurfaceTexture());
            Log.d(TAG, "surface2:" + myView.textureView2.getSurfaceTexture());
            if (surface1 != null && surface2 != null) {
                // Now update the background
                changeBackground();
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
                    }
                    mediaPlayer1.setDataSource(c, uri);
                    mediaPlayer1.prepareAsync();
                } else {
                    if (mediaPlayer2!=null) {
                        mediaPlayer2.reset();
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
            crossFadeViews();
        }
    }
}

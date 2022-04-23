package com.garethevans.church.opensongtablet.secondarydisplay;
// TODO
// Info bar not working
// Clicking on the first section crossfades with a blank view?
// Clicking on a section when logo is on shouldn't start the timer, but a logo off with content should

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.annotation.SuppressLint;
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
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.databinding.CastScreenBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

// This contains all of the screen mirroring logic (previously PresentationCommon and Presentation)
// All common preferences are stored in the PresentationSettings class
// Colors are stored in ThemeColors class

// TODO TIDY UP
public class SecondaryDisplay extends Presentation {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final Display display;
    private CastScreenBinding myView;
    private final String TAG = "SecondaryDisplay";
    private ArrayList<View> secondaryViews;
    private ArrayList<Integer> secondaryWidths, secondaryHeights;

    // Default variables
    private float scaleChords, scaleHeadings, scaleComments, lineSpacing;
    private Timer waitUntilTimer;
    private TimerTask waitUntilTimerTask;
    private int waitingOnViewsToDraw;
    private int showWhich = 0;
    private int showWhichInfo = 0;
    private int showWhichBackground = 0;  //0=not set, 1=image1, 2=image2, 3=surface1, 4=surface2
    private int showWhichVideo = 0;
    private final int logoSplashTime = 3000;
    private int defaultPadding, availableScreenWidth, availableScreenHeight, horizontalSize, verticalSize,
            availableWidth_1col, availableWidth_2col, availableWidth_3col;

    private boolean firstRun = true, isNewSong, waitForVideo, forceCastUpdate, infoBarChangeRequired,
            infoBarRequiredTime=false, infoBarRequireInitial=true, trimLines, trimSections,
            addSectionSpace, boldChordHeading, displayChords, invertXY;

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
        setViewAlreadyFadedOut(myView.songContent1);
        setViewAlreadyFadedOut(myView.songContent2);
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

        availableScreenWidth = horizontalSize - (2 * mainActivityInterface.getPresenterSettings().getPresoXMargin());
        availableScreenHeight = verticalSize - (2 * mainActivityInterface.getPresenterSettings().getPresoYMargin());

        updateViewSizes(myView.pageHolder);

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

            } else if (mainActivityInterface.getMode().equals("Stage")) {
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
        switch (mainActivityInterface.getMode()) {
            case "Stage":
            case "Performance":
            default:
                scaleHeadings = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleHeadings", 0.8f);
                scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleComments", 0.8f);
                displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean("displayChords", true);
                boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean("boldChordHeading", false);
                break;

            case "Presenter":
                scaleHeadings = 0.0f;
                scaleComments = 0.0f;
                displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean("presoShowChords", false);
                boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean("presoLyricsBold", false);
                break;
        }
        infoBarChangeRequired = true;
        forceCastUpdate = false;
        hideCols2and3();
    }
    private void hideCols2and3() {
        // Only need these in performance mode
        int visiblity = View.GONE;
        if (mainActivityInterface.getMode().equals("Performance")) {
            visiblity = View.VISIBLE;
        }
        myView.songContent1Col2.setVisibility(visiblity);
        myView.songContent1Col3.setVisibility(visiblity);
        myView.songContent2Col2.setVisibility(visiblity);
        myView.songContent2Col3.setVisibility(visiblity);
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
                Log.d(TAG,"invertXY: "+invertXY+ " available width: "+availableScreenWidth);
                assert backgroundToFadeIn instanceof ImageView;
                Drawable drawable = ContextCompat.getDrawable(c, R.drawable.simple_rectangle);
                if (drawable != null) {
                    Log.d(TAG,"view width="+backgroundToFadeIn.getLayoutParams().width);
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
    // For Presenter Mode, the bar shows as required under the following conditions:
    // - The first time a song is displayed
    // - For at least the untilWaitTime has elapsed since first presented
    private void setupTimers() {
        infoBarRequiredTime = true;
        cancelInfoTimers();
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
        // The time that the info bar is required for
        int untilTimeWait = 20000;
        waitUntilTimer.schedule(waitUntilTimerTask, untilTimeWait);
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
        myView.testSongInfo.setupLayout(mainActivityInterface,false);
        myView.songProjectionInfo1.setupLayout(mainActivityInterface,false);
        myView.songProjectionInfo2.setupLayout(mainActivityInterface,false);
    }
    public void changeInfoAlignment() {
        myView.songProjectionInfo1.setAlign(mainActivityInterface.getPresenterSettings().getPresoInfoAlign());
        myView.songProjectionInfo2.setAlign(mainActivityInterface.getPresenterSettings().getPresoInfoAlign());
    }
    public void setSongInfo() {
        isNewSong = true;
        String title = mainActivityInterface.getSong().getTitle();
        if (title == null || title.isEmpty()) {
            title = mainActivityInterface.getSong().getFilename();
        }
        String ccliLine = c.getString(R.string.used_by_permision);
        if (!mainActivityInterface.getPresenterSettings().getCcliLicence().isEmpty()) {
            ccliLine +=  ".  CCLI " +
                    c.getString(R.string.ccli_licence) + " " + mainActivityInterface.
                    getPresenterSettings().getCcliLicence();
        }
        String ccli = mainActivityInterface.getSong().getCcli();
        if (ccli!=null && !ccli.isEmpty()) {
            ccliLine += ".  " + c.getString(R.string.song) + " #" + ccli;
        }
        String copyright = mainActivityInterface.getSong().getCopyright();
        if (copyright!=null && !copyright.isEmpty() && !copyright.contains("©")) {
            copyright = "©"+copyright;
        }
        String author = mainActivityInterface.getSong().getAuthor();
        if (author!=null && !author.isEmpty()) {
            author = c.getString(R.string.words_and_music_by)+" "+author;
        }

        // Get final strings for VTO
        String finalTitle = title;
        String finalAuthor = author;
        String finalCopyright = copyright;
        String finalCcli = ccliLine;

        // Draw the test song info bar so we can measure it with a VTO
        myView.testSongInfo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Measure the view
                int height = myView.testSongInfo.getMeasuredHeight();

                // Now we can remove this VTO
                myView.testSongInfo.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Now write the actual song info and set the determined height
                if (showWhichInfo < 2) {
                    myView.songProjectionInfo1.setSongTitle(finalTitle);
                    myView.songProjectionInfo1.setSongAuthor(finalAuthor);
                    myView.songProjectionInfo1.setSongCopyright(finalCopyright);
                    myView.songProjectionInfo1.setSongCCLI(finalCcli);
                    myView.songProjectionInfo1.setViewHeight(height);
                } else {
                    myView.songProjectionInfo2.setSongTitle(finalTitle);
                    myView.songProjectionInfo2.setSongAuthor(finalAuthor);
                    myView.songProjectionInfo2.setSongCopyright(finalCopyright);
                    myView.songProjectionInfo2.setSongCCLI(finalCcli);
                    myView.songProjectionInfo2.setViewHeight(height);
                }
                // TODO - Should the fade in/out be called here?
            }
        });
        // All info should be shown if available
        // Set it to the test view.  Once drawn, it gets measured for height in the VTO
        // It is then written to the correct view
        myView.testSongInfo.setSongTitle(title);
        myView.testSongInfo.setSongAuthor(author);
        myView.testSongInfo.setSongCopyright(copyright);
        myView.testSongInfo.setSongCCLI(ccli);
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
        // Or we have changed the song
        if ((!infoBarRequireInitial && !infoBarRequiredTime) || isNewSong) {
            Log.d(TAG,"Should hide view:"+songInfoView+" 1:"+myView.songProjectionInfo1+" 2:"+myView.songProjectionInfo2);
            mainActivityInterface.getCustomAnimation().faderAnimation(songInfoView,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                    songInfoView.getAlpha(), 0f);
        }
    }
    private void songInfoShowCheck(View songInfoView) {
        Log.d(TAG,"songInfoShowCheck()");
        // If required (new song loaded and not already showing), show the info bar
        Log.d(TAG,"info1: "+(songInfoView==myView.songProjectionInfo1) + "  info2: "+(songInfoView==myView.songProjectionInfo2));

        if ((canShowSong() && infoBarRequireInitial && mainActivityInterface.getPresenterSettings().getCurrentSection()>-1) ||
                isNewSong) {
            isNewSong = false;
            moveToNextSongInfoView();
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
        secondaryViews = null;
        secondaryViews = new ArrayList<>();
        secondaryWidths = null;
        secondaryWidths = new ArrayList<>();
        secondaryHeights = null;
        secondaryHeights = new ArrayList<>();

        // Decide if this is an XML, PDF or IMG file and proceed accordingly
        if (mainActivityInterface.getSong().getFiletype().equals("XML")) {
            setSectionViews();
        } else {
            // TODO deal with PDF and images!
        }
    }
    public void setSongContentPrefs() {
        trimLines = mainActivityInterface.getPreferences().getMyPreferenceBoolean("trimLines", false);
        trimSections = mainActivityInterface.getPreferences().getMyPreferenceBoolean("trimSections", false);
        addSectionSpace = mainActivityInterface.getPreferences().getMyPreferenceBoolean("addSectionSpace", true);
        boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean("boldChordHeading", false);
        scaleChords = mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleChords", 0.8f);
    }
    private void setSectionViews() {
        boolean isPresentation = !mainActivityInterface.getMode().equals("Performance");
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
                        Log.d(TAG,"Drawn a view.  Now waitingOnViewsToDraw="+waitingOnViewsToDraw);
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
            float max_x = (float) horizontalSize / (float) secondaryWidths.get(x);
            float max_y = (float) verticalSize / (float) secondaryHeights.get(x);
            // The text size is 14sp by default.  Compare this to the pref
            float best = Math.min(max_x, max_y);
            Log.d(TAG,"best="+best);
            if ((best * 14f) > mainActivityInterface.getPresenterSettings().getFontSizePresoMax()) {
                best = mainActivityInterface.getPresenterSettings().getFontSizePresoMax() / 14f;
            }
            secondaryViews.get(x).setPivotX(0f);
            secondaryViews.get(x).setPivotY(0f);
            if (best > 0) {
                secondaryViews.get(x).setScaleX(best);
                secondaryViews.get(x).setScaleY(best);
            }
            Log.d(TAG, "view[" + x + "]: " + width + "x" + height + "  scaleFactor=" + best);
        }

        // We can now remove the views from the test layout
        myView.testLayout.removeAllViews();

        Log.d(TAG, "mode=" + mainActivityInterface.getMode());
        if (mainActivityInterface.getMode().equals("Performance")) {
            Log.d(TAG, "Perfomance mode - need to show everything");
            showAllSections();
        } else {
            // Only need to show the current section (if it has been chosen)
            if (mainActivityInterface.getSong().getCurrentSection()>=0) {
                showSection(mainActivityInterface.getPresenterSettings().getCurrentSection());
            }
        }
    }

    public void showSection(final int position) {
        // Decide which view to show.  Do nothing if it is already showing
        Log.d(TAG,"position="+position+"  getPresenterSettings().getCurrentSection()="+mainActivityInterface.getPresenterSettings().getCurrentSection());
        if (position!=mainActivityInterface.getSong().getCurrentSection() ||
        position == mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getSectionEdited()) {
            // If we edited the section temporarily, remove this position flag
            mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().setSectionEdited(-1);
            mainActivityInterface.getSong().setCurrentSection(position);
            if (position >= 0 && position < secondaryViews.size()) {
                // Check the song info status first
                checkSongInfoShowHide();

                // Remove the view from any parent it might be attached to already (can only have 1)
                removeViewFromParent(secondaryViews.get(position));

                // Get the size of the view
                int width = secondaryWidths.get(position);
                int height = secondaryHeights.get(position);

                // Get the measured height of the song info bar
                int infoHeight;
                if (showWhichInfo < 2) {
                    infoHeight = myView.songProjectionInfo1.getViewHeight();
                } else {
                    infoHeight = myView.songProjectionInfo2.getViewHeight();
                }
                if (infoHeight == 0) {
                    infoHeight = myView.testSongInfo.getViewHeight();
                }
                int alertHeight = myView.alertBar.getViewHeight();

                float max_x = (float) availableScreenWidth / (float) width;
                float max_y = (float) (availableScreenHeight - infoHeight - alertHeight) / (float) height;

                float best = Math.min(max_x, max_y);
                Log.d(TAG, "best=" + best);
                if (best > (mainActivityInterface.getPresenterSettings().getFontSizePresoMax() / 14f)) {
                    best = mainActivityInterface.getPresenterSettings().getFontSizePresoMax() / 14f;
                }

                secondaryViews.get(position).setPivotX(0f);
                secondaryViews.get(position).setPivotY(0f);
                secondaryViews.get(position).setScaleX(best);
                secondaryViews.get(position).setScaleY(best);

                // We can now prepare the new view and animate in/out the views as long as the logo is off
                // and the blank screen isn't on
                Log.d(TAG, "showWhich=" + showWhich + "  canShowSong()=" + canShowSong());

                // Translate the scaled views based on the alignment
                int newWidth = (int) (width * best);
                int newHeight = (int) (height * best);
                translateView(secondaryViews.get(position), newWidth, newHeight, infoHeight, alertHeight);

                if (showWhich < 2) {
                    myView.songContent1Col1.removeAllViews();
                    myView.songContent1Col1.addView(secondaryViews.get(position));
                    crossFadeContent(myView.songContent2, myView.songContent1);

                } else {
                    myView.songContent2Col1.removeAllViews();
                    myView.songContent2Col1.addView(secondaryViews.get(position));
                    crossFadeContent(myView.songContent1, myView.songContent2);

                }
            }
        }
    }
    private void showAllSections() {
        float scaleFactor;
        int widthBeforeScale = 0, heightBeforeScale = 0, widthAfterScale, heightAfterScale;

        Log.d(TAG,"availableWidth="+availableScreenWidth+"  availableHeight="+availableScreenHeight);

        // Available height needs to remember to leave space for the infobar which is always visible in this mode
        // The bar height is constant
        int infoHeight = Math.max(myView.songProjectionInfo1.getMeasuredHeight(),myView.songProjectionInfo2.getMeasuredHeight());
        int modeHeight = availableScreenHeight - infoHeight;

        boolean need23columns = mainActivityInterface.getMode().equals("Performance");
        if (showWhich<2) {
            scaleFactor = mainActivityInterface.getProcessSong().addViewsToScreen(
                    need23columns, secondaryViews, secondaryWidths, secondaryHeights, myView.allContent,
                    myView.songContent2, null, availableScreenWidth, modeHeight,
                    myView.songContent2Col1, myView.songContent2Col2, myView.songContent2Col3);

        } else {
            scaleFactor = mainActivityInterface.getProcessSong().addViewsToScreen(
                    need23columns, secondaryViews, secondaryWidths, secondaryHeights, myView.allContent,
                    myView.songContent1, null, availableScreenWidth, modeHeight,
                    myView.songContent1Col1, myView.songContent1Col2, myView.songContent1Col3);
        }

        for (int x = 0; x < secondaryViews.size(); x++) {
            widthBeforeScale = Math.max(widthBeforeScale, secondaryWidths.get(x));
            heightBeforeScale += secondaryHeights.get(x);
        }

        widthAfterScale = (int) (widthBeforeScale*scaleFactor);
        heightAfterScale = (int) (heightBeforeScale*scaleFactor);

        Log.d(TAG,"widthBeforeScale="+widthBeforeScale+"  scaleFactor="+scaleFactor+"  widthAfterScale="+widthAfterScale);
        Log.d(TAG,"heightBeforeScale="+heightBeforeScale+"  scaleFactor="+scaleFactor+"  heightAfterScale="+heightAfterScale);

        if (showWhich<2) {
            ViewGroup.LayoutParams lp = myView.songContent2.getLayoutParams();
            lp.width = MATCH_PARENT;
            lp.height = MATCH_PARENT;
            myView.songContent2.setLayoutParams(lp);
            crossFadeContent(myView.songContent1,myView.songContent2);
        } else {
            ViewGroup.LayoutParams lp = myView.songContent1.getLayoutParams();
            lp.width = MATCH_PARENT;
            lp.height = MATCH_PARENT;
            myView.songContent1.setLayoutParams(lp);
            crossFadeContent(myView.songContent2,myView.songContent1);
        }
    }
    private void removeViewFromParent(View view) {
        if (view!=null && view.getParent()!=null) {
            ((ViewGroup)view.getParent()).removeView(view);
        }
    }
    @SuppressLint("RtlHardcoded")
    private void translateView(View view, int newWidth, int newHeight, int infoHeight, int alertHeight) {
        Log.d(TAG,"infoHeight="+infoHeight+"  alertHeight="+alertHeight);
        Log.d(TAG,"availableWidth: "+availableScreenWidth+"  newWidth: "+newWidth);
        Log.d(TAG, "availableHeight: " + availableScreenHeight+"  newHeight: "+newHeight);
        Log.d(TAG,"invertXY: "+invertXY);
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
        Log.d(TAG,"translationX="+view.getTranslationX()+"  translationY="+view.getTranslationY());

    }
    // If we edited a view from PresenterMode via the bottom sheet for a song section
    public void editView() {
        // The view has been created, so put it here
        // Now update the create views for second screen presenting
        // Create a blank song with just this section
        Song tempSong = new Song();
        tempSong.setLyrics(mainActivityInterface.getPresenterSettings().getSongSectionsAdapter().getNewContent());
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
    public void showPerformanceContent() {
        Log.d(TAG,"showPerformanceContent()");
        if (showWhichInfo<2) {
            myView.songProjectionInfo1.setVisibility(View.VISIBLE);
            myView.songProjectionInfo1.setAlpha(1.0f);
            Log.d(TAG, "title=" + myView.songProjectionInfo1.getSongTitle());
        } else {
            myView.songProjectionInfo2.setVisibility(View.VISIBLE);
            myView.songProjectionInfo2.setAlpha(1.0f);
            Log.d(TAG, "title=" + myView.songProjectionInfo2.getSongTitle());
        }
    }
    public void showPresenterContent() {

    }
    private void crossFadeContent(View contentToFadeOut, View contentToFadeIn) {
        // Fade out is always fine, so do it
        if (contentToFadeOut!=null) {
            mainActivityInterface.getCustomAnimation().faderAnimation(contentToFadeOut,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                    contentToFadeOut.getAlpha(), 0f);
        }

        // Fade in of a content is fine
        if (contentToFadeIn!=null) {
            mainActivityInterface.getCustomAnimation().faderAnimation(contentToFadeIn,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),
                    0f, 1f);
        }

        // Now get ready for the next views
        moveToNextSongView();
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
        uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(uriString);
        if (uri!=null && mainActivityInterface.getStorageAccess().uriExists(uri)) {
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

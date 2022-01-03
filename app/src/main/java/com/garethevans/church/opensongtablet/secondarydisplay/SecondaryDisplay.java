package com.garethevans.church.opensongtablet.secondarydisplay;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.CastScreenBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

// This contains all of the screen mirroring logic (previously PresentationCommon and Presentation)
// All common preferences are stored in the PresentationSettings class
// Colors are stored in ThemeColors class

public class SecondaryDisplay extends Presentation implements MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, SurfaceHolder.Callback {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final Display display;
    private CastScreenBinding myView;
    private final String TAG = "SecondaryDisplay";

    // Default variables
    private boolean trimLines, trimSections, addSectionSpace, boldChordHeading, displayChords;
    private float scaleChords, scaleHeadings, scaleComments, lineSpacing;

    private int defaultPadding, screenWidth, availableScreenWidth, screenHeight, availableScreenHeight,
            bottombarheight, horizontalSize, verticalSize,
            availableWidth_1col, availableWidth_2col, availableWidth_3col;

    private long infoBarUntilTime, lyricAfterTime;
    private int showWhich = 1;
    private int showWhichInfo = 1;
    private final int logoSplashTime = 3000;

    boolean infoBarChangeRequired, forceCastUpdate, showLogoActive, panicRequired, blankActive, alert_on,
            doUpdateActive, infoBarAlertState, animateOutActive, isVideo, isPresenting;

    MediaPlayer mediaPlayer;
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
        Log.d(TAG,"created");
        myView = CastScreenBinding.inflate(getLayoutInflater(),null,false);
        setContentView(myView.getRoot());

        // Set the appropriate preferences
        mainActivityInterface.getPresenterSettings().getPreferences(c,mainActivityInterface);

        setDefaultColors();
        setSongInfoStyles();
        setScreenSizes();
        changeMargins();
        changeAlignment();
        changeBackground();
        matchPresentationToMode();
        changeLogo();
        showLogo(true, !mainActivityInterface.getMode().equals("Presenter"));
        setInitialView();

        setSongContent();
        myView.songProjectionInfo1.postDelayed(this::setSongInfo,3000);
        normalStartUp();
    }

    // Initialise views and settings
    // First up the colors
    public void setDefaultColors() {
        // The page background
        updatePageBackgroundColor();

        // The song info bar
        updateInfoBarColor();
    }

    public void updateInfoBarColor() {
        // Convert the color to an alpha component
        myView.bottomBarBackground.setBackgroundColor(ColorUtils.setAlphaComponent(
                mainActivityInterface.getMyThemeColors().getPresoShadowColor(),
                (int)(mainActivityInterface.getPresenterSettings().getPresoInfoBarAlpha()*255)));
    }
    public void updatePageBackgroundColor() {
        // Not in PresenterMode!  For that mode, the background colours are kept as transparent
        if (!mainActivityInterface.getMode().equals("Presenter")) {
            myView.songContent1.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
            myView.songContent2.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
            myView.pageHolder.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
        }
    }

    // Now the screen settings
    public void setScreenSizes() {
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);

        updateBottomBarSize();

        defaultPadding = 0;

        // The display will have default margins - figure them out

        int leftpadding = myView.pageHolder.getPaddingLeft();
        int rightpadding = myView.pageHolder.getPaddingRight();
        int toppadding = myView.pageHolder.getPaddingTop();
        int bottompadding = myView.pageHolder.getPaddingBottom();

        myView.pageHolder.setRotation(mainActivityInterface.getPresenterSettings().getCastRotation());

        int horizontalDisplayPadding, verticalDisplayPadding;

        if (mainActivityInterface.getPresenterSettings().getCastRotation() == 90.0f ||
                mainActivityInterface.getPresenterSettings().getCastRotation() == 270.0f) {  // Switch width for height and vice versa
            horizontalSize = metrics.heightPixels;
            verticalSize = metrics.widthPixels;

        } else {
            horizontalSize = metrics.widthPixels;
            verticalSize = metrics.heightPixels;
        }

        horizontalDisplayPadding = horizontalSize - myView.pageHolder.getMeasuredWidth();
        verticalDisplayPadding = verticalSize - myView.pageHolder.getMeasuredHeight();

        screenWidth = horizontalSize - horizontalDisplayPadding;
        screenHeight = verticalSize - verticalDisplayPadding;

        Log.d(TAG,"horizontalSize="+horizontalSize+"  horizontalDisplayPadding="+horizontalDisplayPadding+"  screenWidth="+screenWidth);
        myView.pageHolder.requestLayout();

        availableScreenWidth = screenWidth - leftpadding - rightpadding;
        availableScreenHeight = screenHeight - toppadding - bottompadding - bottombarheight;
        availableWidth_1col = availableScreenWidth;
        availableWidth_2col = (int) ((float) availableScreenWidth / 2.0f);
        availableWidth_3col = (int) ((float) availableScreenWidth / 3.0f);

        myView.pageHolder.post(()-> {


        });
    }
    public void changeMargins() {
        myView.pageHolder.setPadding(mainActivityInterface.getPresenterSettings().getPresoXMargin(),
                mainActivityInterface.getPresenterSettings().getPresoYMargin(),
                mainActivityInterface.getPresenterSettings().getPresoXMargin(),
                mainActivityInterface.getPresenterSettings().getPresoYMargin());
    }


    // Set views depending on mode
    // TODO call this on orientation change
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
                myView.songProjectionInfo1.showMiniLogo(true);
                myView.songProjectionInfo2.showMiniLogo(true);
                myView.surfaceView1.setVisibility(View.GONE);
                myView.surfaceView2.setVisibility(View.GONE);
                myView.backgroundImage.setImageDrawable(null);
                myView.imageView1.setImageDrawable(null);
                myView.imageView2.setImageDrawable(null);
                myView.backgroundImage.setVisibility(View.GONE);
                myView.imageView1.setVisibility(View.GONE);
                myView.imageView2.setVisibility(View.GONE);
                scaleHeadings = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "scaleHeadings", 0.8f);
                scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "scaleComments", 0.8f);
                displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "displayChords", true);
                boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "boldChordHeading", false);
                break;

            case "Presenter":
                myView.songProjectionInfo1.showMiniLogo(false);
                myView.songProjectionInfo2.showMiniLogo(false);
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
        myView.songProjectionInfo1.setVisibility(View.GONE);
        myView.songProjectionInfo2.setVisibility(View.GONE);
        myView.songContent1.setVisibility(View.GONE);
        myView.songContent2.setVisibility(View.GONE);
        myView.imageView1.setVisibility(View.GONE);
        myView.imageView2.setVisibility(View.GONE);
        myView.surfaceView1.setVisibility(View.GONE);
        myView.surfaceView2.setVisibility(View.GONE);
        myView.mainLogo.setVisibility(View.VISIBLE);

        Log.d(TAG,"mode="+mainActivityInterface.getMode());
        switch (mainActivityInterface.getMode()) {
            case "Performance":
            case "Stage":
                myView.mainLogo.postDelayed(() -> {
                    mainActivityInterface.getCustomAnimation().faderAnimation(myView.mainLogo,
                            mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), false);
                    showPerformanceContent();
                }, logoSplashTime);
                break;
            case "Presenter":
                myView.mainLogo.postDelayed(() -> {
                    //setSongInfo();
                    //showPresenterContent();
                    //fadeInOutSong(true,false);
                }, logoSplashTime);
                break;
        }
    }
    private void normalStartUp() {
        // Animate out the default logo
        // TODO
        doUpdate();
    }

    // The screen background
    public void changeBackground() {
        // There has been an update to the user's background or logo, so pull them in from preferences
        // (already updated in PresenterSettings)
        // This only runs in PresenterMode!  Performance/Stage Mode reflect the device theme
        if (mainActivityInterface.getMode().equals("Presenter")) {
            Uri background = mainActivityInterface.getPresenterSettings().getChosenBackground();
            Log.d(TAG, "background=" + background);
            Log.d(TAG, "backgroundToUse=" + mainActivityInterface.getPresenterSettings().getBackgroundToUse());
            Log.d(TAG, "backgroundColor=" + mainActivityInterface.getPresenterSettings().getBackgroundColor());
            RequestOptions requestOptions = new RequestOptions().centerCrop();

            if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().equals("color") ||
                    background == null) {
                // Use a solid background color
                myView.backgroundImage.setVisibility(View.VISIBLE);
                myView.surfaceView1.setVisibility(View.INVISIBLE);
                myView.surfaceView2.setVisibility(View.INVISIBLE);
                myView.backgroundImage.setImageDrawable(null);
                myView.backgroundImage.setBackgroundColor(mainActivityInterface.getPresenterSettings().getBackgroundColor());
            } else if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("img")) {
                // Use a static image
                myView.backgroundImage.setVisibility(View.VISIBLE);
                myView.surfaceView1.setVisibility(View.INVISIBLE);
                myView.surfaceView2.setVisibility(View.INVISIBLE);
                myView.backgroundImage.setBackgroundColor(Color.TRANSPARENT);
                if (background.toString().endsWith("ost_bg.png")) {
                    Drawable defaultImage = ResourcesCompat.getDrawable(c.getResources(), R.drawable.preso_default_bg, null);
                    myView.backgroundImage.setImageDrawable(defaultImage);
                } else {
                    Glide.with(c).load(background).apply(requestOptions).into(myView.backgroundImage);
                }
            } else if (mainActivityInterface.getPresenterSettings().getBackgroundToUse().startsWith("vid")) {
                // TODO update the surfaceView with a video!

            }
            // Now call the changeLogo function to check for updates there too
            changeLogo();
        }
    }

    // The logo
    public void showLogo(boolean show, boolean hideAfterShow) {
        Log.d(TAG,"show="+show+"  hideAfterShow="+hideAfterShow);
        if (show) {
            Log.d(TAG,"showing Logo");
            mainActivityInterface.getCustomAnimation().faderAnimation(myView.mainLogo,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),true);
            fadeInOutSong(false,false);
            fadeInOutSong(false, true);
        } else {
            Log.d(TAG,"hiding Logo");
            mainActivityInterface.getCustomAnimation().faderAnimation(myView.mainLogo,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),false);
            fadeInOutSong(true,false);
            fadeInOutSong(true, true);
        }
        sendAlphaPanic(myView.mainLogo,!show);
        if (show && hideAfterShow) {
            // This will hide the logo after the logoSplashTime
            myView.mainLogo.postDelayed(() -> {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.mainLogo,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),false);
                fadeInOutSong(true,false);
                fadeInOutSong(true, true);
                Log.d(TAG,"timed hiding of logo");
                sendAlphaPanic(myView.mainLogo,true);
            },logoSplashTime);
        }
    }
    private void changeLogo() {
        // There may have been an update to the user's logo.  Called from change Background in this
        int size = (int)(mainActivityInterface.getPresenterSettings().getLogoSize() * horizontalSize);
        Log.d(TAG,"logoSize pref="+mainActivityInterface.getPresenterSettings().getLogoSize());
        Log.d(TAG,"horizontalSize="+horizontalSize);
        Log.d(TAG,"logoWidth="+size);
        ViewGroup.LayoutParams layoutParams = myView.mainLogo.getLayoutParams();
        layoutParams.width = size;
        layoutParams.height = size;
        myView.mainLogo.setLayoutParams(layoutParams);
        RequestOptions requestOptions = new RequestOptions().fitCenter().override(size,size);
        Glide.with(c).load(mainActivityInterface.getPresenterSettings().getLogo()).apply(requestOptions).into(myView.mainLogo);
    }

    // The black screen
    public void showBlackScreen(boolean black) {
        mainActivityInterface.getCustomAnimation().faderAnimation(myView.pageHolder,
                mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), !black);
    }

    // The song info bar
    public void setSongInfoStyles() {
        myView.songProjectionInfo1.setupFonts(c, mainActivityInterface);
        myView.songProjectionInfo2.setupFonts(c, mainActivityInterface);
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

            // Fade in 1
            myView.songProjectionInfo1.setVisibility(View.VISIBLE);
            mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo1,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),true);
            // Fade out 2
            if (myView.songProjectionInfo2.getAlpha()>0) {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo2,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),false);
            }

        } else {
            myView.songProjectionInfo2.setSongTitle(mainActivityInterface.getSong().getTitle());
            myView.songProjectionInfo2.setSongAuthor(mainActivityInterface.getSong().getAuthor());
            myView.songProjectionInfo2.setSongCopyright(mainActivityInterface.getSong().getCopyright());

            // Fade in 2
            myView.songProjectionInfo2.setVisibility(View.VISIBLE);
            mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo2,
                    mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),true);
            // Fade out 1
            if (myView.songProjectionInfo1.getAlpha()>0) {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo1,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),false);
            }
        }
    }
    private void updateBottomBarSize() {
        //bottombarheight = 0;
        if (showWhich==1) {
            myView.songProjectionInfo1.post(() -> bottombarheight = myView.songProjectionInfo1.getMeasuredHeight());
        } else {
            myView.songProjectionInfo2.post(() -> bottombarheight = myView.songProjectionInfo2.getMeasuredHeight());
        }
    }
    public void changeAlignment() {
        myView.songProjectionInfo1.setAlign(mainActivityInterface.getPresenterSettings().getPresoInfoAlign());
        myView.songProjectionInfo2.setAlign(mainActivityInterface.getPresenterSettings().getPresoInfoAlign());
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
        Log.d(TAG,"position: "+position+"  sectionView.size(): "+mainActivityInterface.getSectionViews().size());
        if (position<mainActivityInterface.getSectionViews().size()) {
            if (mainActivityInterface.getSectionViews().get(position).getParent() != null) {
                ((LinearLayout) mainActivityInterface.getSectionViews().get(position).getParent()).removeView(mainActivityInterface.getSectionViews().get(position));
            }

            // Set a listener to wait for drawing, then measure and scale
            ViewTreeObserver viewTreeObserver = myView.testLayout.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Move to the next view
                    if (showWhich == 2) {
                        showWhich = 1;
                    } else {
                        showWhich = 2;
                    }

                    // Measure the height of the layouts
                    int width = mainActivityInterface.getSectionViews().get(position).getMeasuredWidth();
                    int height = mainActivityInterface.getSectionViews().get(position).getMeasuredHeight();

                    Log.d(TAG,position+": width="+width+"  height="+height);
                    Log.d(TAG,position+": screenWidth="+availableScreenWidth+"  screenHeight="+availableScreenHeight);

                    float max_x = (float) availableScreenWidth / (float) width;
                    float max_y = (float) availableScreenHeight / (float) height;

                    float best = Math.min(max_x,max_y);
                    if (best > (mainActivityInterface.getPresenterSettings().getFontSizePresoMax()/14f)) {
                        best = mainActivityInterface.getPresenterSettings().getFontSizePresoMax()/14f;
                    }

                    Log.d(TAG,position+": best="+best);

                    mainActivityInterface.getSectionViews().get(position).setPivotX(0f);
                    mainActivityInterface.getSectionViews().get(position).setPivotY(0f);
                    mainActivityInterface.getSectionViews().get(position).setScaleX(best);
                    mainActivityInterface.getSectionViews().get(position).setScaleY(best);

                    // Remove from the test layout
                    myView.testLayout.removeAllViews();

                    // Remove this listener
                    viewTreeObserver.removeOnGlobalLayoutListener(this);

                    if (showWhich == 1) {
                        myView.songContent1.removeAllViews();
                        myView.songContent1.addView(mainActivityInterface.getSectionViews().get(position));
                        mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent1,
                                mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), true);
                        mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent2,
                                mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), false);
                    } else {
                        myView.songContent2.removeAllViews();
                        myView.songContent2.addView(mainActivityInterface.getSectionViews().get(position));
                        mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent2,
                                mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), true);
                        mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent1,
                                mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), false);
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
                    Log.d(TAG,"width:"+width+"  height:"+height);
                    mainActivityInterface.addSectionSize(width,height);
                }

                // Calculate the scale factor for each section individually
                // For each meausured view, get the max x and y scale value
                // Check they are less than the max preferred value
                for (int x=0; x<mainActivityInterface.getSectionViews().size(); x++) {
                    float max_x = (float)screenWidth/(float)mainActivityInterface.getSectionWidths().get(x);
                    float max_y = (float)screenHeight/(float)mainActivityInterface.getSectionHeights().get(x);
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

    // The view panic which forces the end animation after double the crossfade time (avoids stuck/incomplete)
    private void sendAlphaPanic(View view, boolean isFadingOut) {
        view.postDelayed(() -> {
            if (isFadingOut) {
                view.setAlpha(0.0f);
            } else {
                view.setAlpha(1.0f);
            }
        },mainActivityInterface.getPresenterSettings().getPresoTransitionTime()* 2L);
    }



    // Deal with the display of song content
    public void showPerformanceContent() {
        Log.d(TAG,"showPerformanceContent()");
        myView.songProjectionInfo1.setVisibility(View.VISIBLE);
        myView.songProjectionInfo1.setAlpha(1.0f);
        Log.d(TAG,"title="+myView.songProjectionInfo1.getSongTitle());
        myView.songProjectionInfo1.post(()->{
            updateBottomBarSize();
            Log.d(TAG,"bottombarheight="+bottombarheight);
        });
    }
    public void showPresenterContent() {

    }
    private void fadeInOutSong(boolean fadeIn, boolean content) {
        if (showWhichInfo==1) {
            if (content) {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent1,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),fadeIn);
            } else {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo1,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), fadeIn);
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.bottomBarBackground,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),fadeIn);
            }
        } else {
            if (content) {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songContent2,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),fadeIn);
            } else {
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.songProjectionInfo2,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(), fadeIn);
                mainActivityInterface.getCustomAnimation().faderAnimation(myView.bottomBarBackground,
                        mainActivityInterface.getPresenterSettings().getPresoTransitionTime(),fadeIn);
            }
        }
    }


    public void setIsPresenting(boolean isPresenting) {
        this.isPresenting = isPresenting;
    }
    public boolean getIsPresenting() {
        return isPresenting;
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








}

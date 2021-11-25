package com.garethevans.church.opensongtablet.secondarydisplay;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.core.content.res.ResourcesCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.SongProjectionInfo;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.gms.cast.CastDevice;

public class PresentationCommon {

    // This class is instantiated by any HMDI or Cast displays.
    // This means they get their own variables.

    private Context c;
    private MainActivityInterface mainActivityInterface;

    public PresentationCommon(Context c, MainActivityInterface mainActivityInterface) {
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
    }

    // Default variables
    private boolean trimLines, trimSections, addSectionSpace, boldChordHeading, displayChords;
    private float scaleChords, scaleHeadings, scaleComments, lineSpacing, fontSizePresoMax,
            presoInfoBarAlpha;

    private int defaultPadding, screenWidth, availableScreenWidth, screenHeight, availableScreenHeight,
            bottombarheight,
            availableWidth_1col, availableWidth_2col, availableWidth_3col,
            lyricsCapoColor, lyricsChordsColor, presoFontColor, lyricsBackgroundColor,
            lyricsTextColor, presoInfoColor, presoAlertColor, presoShadowColor, lyricsVerseColor,
            lyricsChorusColor, lyricsPreChorusColor, lyricsBridgeColor, lyricsTagColor,
            lyricsCommentColor, lyricsCustomColor, infoBarChangeDelay, lyricDelay, panicDelay;
    private long infoBarUntilTime, lyricAfterTime, panicAfterTime;
    boolean infoBarChangeRequired, forceCastUpdate, showLogoActive, panicRequired, blankActive, alert_on,
            doUpdateActive, infoBarAlertState, animateOutActive, isVideo, isPresenting;
    MediaPlayer mediaPlayer;
    Uri vidUri, uriToLoad;
    private float castRotation;
    private CastDevice castDevice;
    private final String TAG = "PresentationCommon";
    private String alertText;

    // Views used (to avoid having to be passed these repeatedly!)
    private ImageView mainLogo, miniLogo, imageView1, imageView2, backgroundImage;
    private LinearLayout songContent1, songContent2, testLayout;
    private SongProjectionInfo songProjectionInfo1, songProjectionInfo2;
    private SurfaceView surfaceView1, surfaceView2;
    private RelativeLayout pageHolder;
    private FrameLayout bottomBarBackground;
    private int showWhich = 1, showWhichInfo = 1, crossFadeTime = 800, logoSplashTime = 3000;

    // Set up defaults
    public void setDefaultColors() {
        // The font and section colours
        updateFontColor();

        // The page background
        updatePageBackgroundColor();

        // The song info bar
        updateInfoBarColor();
    }
    public void updateFontColor() {
        lyricsCapoColor = mainActivityInterface.getMyThemeColors().getLyricsCapoColor();
        lyricsChordsColor = mainActivityInterface.getMyThemeColors().getLyricsChordsColor();
        presoFontColor = mainActivityInterface.getMyThemeColors().getPresoFontColor();
        lyricsTextColor = mainActivityInterface.getMyThemeColors().getLyricsTextColor();
        presoInfoColor = mainActivityInterface.getMyThemeColors().getPresoInfoFontColor();
        presoAlertColor = mainActivityInterface.getMyThemeColors().getPresoAlertColor();
        lyricsVerseColor = mainActivityInterface.getMyThemeColors().getLyricsVerseColor();
        lyricsChorusColor = mainActivityInterface.getMyThemeColors().getLyricsChorusColor();
        lyricsPreChorusColor = mainActivityInterface.getMyThemeColors().getLyricsPreChorusColor();
        lyricsBridgeColor = mainActivityInterface.getMyThemeColors().getLyricsBridgeColor();
        lyricsTagColor = mainActivityInterface.getMyThemeColors().getLyricsTagColor();
        lyricsCommentColor = mainActivityInterface.getMyThemeColors().getLyricsCommentColor();
        lyricsCustomColor = mainActivityInterface.getMyThemeColors().getLyricsCustomColor();
    }
    public void updateInfoBarColor() {
        presoShadowColor = mainActivityInterface.getMyThemeColors().getPresoShadowColor();
        presoInfoBarAlpha = mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoInfoBarAlpha",0.5f);
        bottomBarBackground.setBackgroundColor(presoShadowColor);
        bottomBarBackground.setAlpha(presoInfoBarAlpha);
    }
    public void updatePageBackgroundColor() {
        lyricsBackgroundColor = mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor();
        songContent1.setBackgroundColor(lyricsBackgroundColor);
        songContent2.setBackgroundColor(lyricsBackgroundColor);
        pageHolder.setBackgroundColor(lyricsBackgroundColor);
    }

    public void setRotation(float castRotation) {
        this.castRotation = castRotation;
    }
    public void setScreenSizes(Display display) {
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);

        updateBottomBarSize();

        defaultPadding = 0;
        pageHolder.post(()-> {
            // The display will have default margins - figure them out

            int leftpadding = pageHolder.getPaddingLeft();
            int rightpadding = pageHolder.getPaddingRight();
            int toppadding = pageHolder.getPaddingTop();
            int bottompadding = pageHolder.getPaddingBottom();

            pageHolder.setRotation(castRotation);

            int horizontalSize, verticalSize, horizontalDisplayPadding, verticalDisplayPadding;

            if (castRotation == 90.0f || castRotation == 270.0f) {  // Switch width for height and vice versa
                horizontalSize = metrics.heightPixels;
                verticalSize = metrics.widthPixels;

            } else {
                horizontalSize = metrics.widthPixels;
                verticalSize = metrics.heightPixels;
            }

            horizontalDisplayPadding = horizontalSize - pageHolder.getMeasuredWidth();
            verticalDisplayPadding = verticalSize - pageHolder.getMeasuredHeight();

            screenWidth = horizontalSize - horizontalDisplayPadding;
            screenHeight = verticalSize - verticalDisplayPadding;

            Log.d(TAG,"horizontalSize="+horizontalSize+"  horizontalDisplayPadding="+horizontalDisplayPadding+"  screenWidth="+screenWidth);
            pageHolder.requestLayout();

            availableScreenWidth = screenWidth - leftpadding - rightpadding;
            availableScreenHeight = screenHeight - toppadding - bottompadding - bottombarheight;
            availableWidth_1col = availableScreenWidth;
            availableWidth_2col = (int) ((float) availableScreenWidth / 2.0f);
            availableWidth_3col = (int) ((float) availableScreenWidth / 3.0f);

        });
    }
    private void updateBottomBarSize() {
        //bottombarheight = 0;
        if (showWhich==1) {
            songProjectionInfo1.post(() -> bottombarheight = songProjectionInfo1.getMeasuredHeight());
        } else {
            songProjectionInfo2.post(() -> bottombarheight = songProjectionInfo2.getMeasuredHeight());
        }
    }
    public void changeMargins() {
        int presoXMargin = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoXMargin",20) + defaultPadding;
        int presoYMargin = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoyMargin",10) + defaultPadding;
        pageHolder.setPadding(presoXMargin,presoYMargin,presoXMargin,presoYMargin);
    }
    public void setDefaultBackgroundImage() {
        Drawable defaultImage = ResourcesCompat.getDrawable(c.getResources(), R.drawable.preso_default_bg, null);
        backgroundImage.setImageDrawable(defaultImage);
    }
    public void initialiseViews(RelativeLayout pageHolder, LinearLayout testLayout, LinearLayout songContent1,
                                LinearLayout songContent2, FrameLayout bottomBarBackground,
                                SongProjectionInfo songProjectionInfo1, SongProjectionInfo songProjectionInfo2,
                                ImageView mainLogo, ImageView backgroundImage, ImageView imageView1,
                                ImageView imageView2, SurfaceView surfaceView1, SurfaceView surfaceView2) {
        this.pageHolder = pageHolder;
        this.testLayout = testLayout;
        this.songContent1 = songContent1;
        this.songContent2 = songContent2;
        this.backgroundImage = backgroundImage;
        this.imageView1 = imageView1;
        this.imageView2 = imageView2;
        this.surfaceView1 = surfaceView1;
        this.surfaceView2 = surfaceView2;
        this.mainLogo = mainLogo;
        this.bottomBarBackground = bottomBarBackground;
        this.songProjectionInfo1 = songProjectionInfo1;
        this.songProjectionInfo2 = songProjectionInfo2;
    }
    public void updateCrossFadeTime() {
        crossFadeTime = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoTransitionTime",800);
    }


    // Set initial view depending on mode
    public void setInitialView() {
        songProjectionInfo1.setVisibility(View.GONE);
        songProjectionInfo2.setVisibility(View.GONE);
        songContent1.setVisibility(View.GONE);
        songContent2.setVisibility(View.GONE);
        imageView1.setVisibility(View.GONE);
        imageView2.setVisibility(View.GONE);
        surfaceView1.setVisibility(View.GONE);
        surfaceView2.setVisibility(View.GONE);
        mainLogo.setVisibility(View.VISIBLE);

        Log.d(TAG,"mode="+mainActivityInterface.getMode());
        switch (mainActivityInterface.getMode()) {
            case "Performance":
                mainLogo.postDelayed(() -> {
                    mainActivityInterface.getCustomAnimation().faderAnimation(mainLogo, crossFadeTime, false);
                    showPerformanceContent();
                }, logoSplashTime);
                break;
            case "Presenter":
                mainLogo.postDelayed(() -> {
                    setSongInfo();
                    //showPresenterContent();
                    //fadeInOutSong(true,false);
                }, logoSplashTime);
                break;
        }
    }

    // Deal with the display of song content
    public void showPerformanceContent() {
        Log.d(TAG,"showPerformanceContent()");
        songProjectionInfo1.setVisibility(View.VISIBLE);
        songProjectionInfo1.setAlpha(1.0f);
        Log.d(TAG,"title="+songProjectionInfo1.getSongTitle());
        songProjectionInfo1.post(()->{
            updateBottomBarSize();
            Log.d(TAG,"bottombarheight="+bottombarheight);
        });
    }


    public void showPresenterContent() {

    }



    // Deal with the logo
    public void showLogo(boolean show, boolean hideAfterShow) {
        Log.d(TAG,"show="+show+"  hideAfterShow="+hideAfterShow);
        if (show) {
            Log.d(TAG,"showing Logo");
            mainActivityInterface.getCustomAnimation().faderAnimation(mainLogo,crossFadeTime,true);
            fadeInOutSong(false,false);
            fadeInOutSong(false, true);
        } else {
            Log.d(TAG,"hiding Logo");
            mainActivityInterface.getCustomAnimation().faderAnimation(mainLogo,crossFadeTime,false);
            fadeInOutSong(true,false);
            fadeInOutSong(true, true);
        }
        if (show && hideAfterShow) {
            // This will hide the logo after the logoSplashTime
            mainLogo.postDelayed(() -> {
                mainActivityInterface.getCustomAnimation().faderAnimation(mainLogo,crossFadeTime,false);
                fadeInOutSong(true,false);
                fadeInOutSong(true, true);
                Log.d(TAG,"timed hiding of logo");
            },logoSplashTime);
        }
    }

    private void fadeInOutSong(boolean fadeIn, boolean content) {
        if (showWhichInfo==1) {
            if (content) {
                mainActivityInterface.getCustomAnimation().faderAnimation(songContent1,crossFadeTime,fadeIn);
            } else {
                mainActivityInterface.getCustomAnimation().faderAnimation(songProjectionInfo1, crossFadeTime, fadeIn);
            }
        } else {
            if (content) {
                mainActivityInterface.getCustomAnimation().faderAnimation(songContent2,crossFadeTime,fadeIn);
            } else {
                mainActivityInterface.getCustomAnimation().faderAnimation(songProjectionInfo2, crossFadeTime, fadeIn);
            }
        }
    }

    // Deal with the black screen
    public void showBlackScreen(boolean black) {
        mainActivityInterface.getCustomAnimation().faderAnimation(pageHolder,crossFadeTime, !black);
    }


    // Show the sections
    public void showSection(final int position) {
        // Decide which view to show to
        // Check the view isn't already attached to a parent
        Log.d(TAG,"position: "+position+"  sectionView.size(): "+mainActivityInterface.getSectionViews().size());
        if (position<mainActivityInterface.getSectionViews().size()) {
            if (mainActivityInterface.getSectionViews().get(position).getParent() != null) {
                ((LinearLayout) mainActivityInterface.getSectionViews().get(position).getParent()).removeView(mainActivityInterface.getSectionViews().get(position));
            }

            // Set a listener to wait for drawing, then measure and scale
            ViewTreeObserver viewTreeObserver = testLayout.getViewTreeObserver();
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
                    if (best > (fontSizePresoMax/14f)) {
                        best = fontSizePresoMax/14f;
                    }

                    Log.d(TAG,position+": best="+best);

                    mainActivityInterface.getSectionViews().get(position).setPivotX(0f);
                    mainActivityInterface.getSectionViews().get(position).setPivotY(0f);
                    mainActivityInterface.getSectionViews().get(position).setScaleX(best);
                    mainActivityInterface.getSectionViews().get(position).setScaleY(best);

                    // Remove from the test layout
                    testLayout.removeAllViews();

                    // Remove this listener
                    viewTreeObserver.removeOnGlobalLayoutListener(this);

                    if (showWhich == 1) {
                        songContent1.removeAllViews();
                        songContent1.addView(mainActivityInterface.getSectionViews().get(position));
                        mainActivityInterface.getCustomAnimation().faderAnimation(songContent1, crossFadeTime, true);
                        mainActivityInterface.getCustomAnimation().faderAnimation(songContent2, crossFadeTime, false);
                    } else {
                        songContent2.removeAllViews();
                        songContent2.addView(mainActivityInterface.getSectionViews().get(position));
                        mainActivityInterface.getCustomAnimation().faderAnimation(songContent2, crossFadeTime, true);
                        mainActivityInterface.getCustomAnimation().faderAnimation(songContent1, crossFadeTime, false);
                    }

                }
            });
            testLayout.addView(mainActivityInterface.getSectionViews().get(position));
        }
    }


    public void matchPresentationToMode() {
        trimLines = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "trimLines", false);
        trimSections = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "trimSections", false);
        addSectionSpace = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "addSectionSpace", true);
        scaleChords = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "scaleChords", 0.8f);
        lineSpacing = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "lineSpacing", 0.1f);
        fontSizePresoMax = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "fontSizePresoMax", 40f);

        switch (mainActivityInterface.getMode()) {
            case "Stage":
            case "Performance":
            default:
                songProjectionInfo1.showMiniLogo(true);
                songProjectionInfo2.showMiniLogo(true);
                surfaceView1.setVisibility(View.GONE);
                surfaceView2.setVisibility(View.GONE);
                backgroundImage.setImageDrawable(null);
                imageView1.setImageDrawable(null);
                imageView2.setImageDrawable(null);
                backgroundImage.setVisibility(View.GONE);
                imageView1.setVisibility(View.GONE);
                imageView2.setVisibility(View.GONE);
                scaleHeadings = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "scaleHeadings", 0.8f);
                scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "scaleComments", 0.8f);
                displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "displayChords", true);
                boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "boldChordHeading", false);
                break;

            case "Presenter":
                songProjectionInfo1.showMiniLogo(false);
                songProjectionInfo2.showMiniLogo(false);
                scaleHeadings = 0.0f;
                scaleComments = 0.0f;
                displayChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "presoShowChords", false);
                boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "presoLyricsBold", false);
                break;
        }
        infoBarChangeRequired = true;
        forceCastUpdate = false;
    }

    public void setIsPresenting(boolean isPresenting) {
        this.isPresenting = isPresenting;
    }
    public boolean getIsPresenting() {
        return isPresenting;
    }

    public void normalStartUp() {
        // Animate out the default logo
        //mainActivityInterface.getCustomAnimation().faderAnimation(mainLogo,mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoTransitionTime",800),false);
    }


    public void setSongInfoStyles() {
        songProjectionInfo1.setupFonts(c, mainActivityInterface);
        songProjectionInfo2.setupFonts(c, mainActivityInterface);
    }

    public void setSongInfo() {
        // Change the current showInfo
        if (showWhichInfo==1) {
            showWhichInfo = 2;
        } else {
            showWhichInfo = 1;
        }

        Log.d(TAG,"info1.getVisibility()="+songProjectionInfo1.getVisibility());
        Log.d(TAG,"info2.getVisibility()="+songProjectionInfo2.getVisibility());
        Log.d(TAG,"info1.getAlpha()="+songProjectionInfo1.getAlpha());
        Log.d(TAG,"info2.getAlpha()="+songProjectionInfo2.getAlpha());


        // All info should be shown if available
        Log.d(TAG,"setSongInfo()  showWhichInfo:"+showWhichInfo);
        if (showWhichInfo==1) {
            songProjectionInfo1.setSongTitle(mainActivityInterface.getSong().getTitle());
            songProjectionInfo1.setSongAuthor(mainActivityInterface.getSong().getAuthor());
            songProjectionInfo1.setSongCopyright(mainActivityInterface.getSong().getCopyright());

            Log.d(TAG,"title="+mainActivityInterface.getSong().getTitle());

            // Fade in 1
            songProjectionInfo1.setVisibility(View.VISIBLE);
            mainActivityInterface.getCustomAnimation().faderAnimation(songProjectionInfo1,crossFadeTime,true);
            // Fade out 2
            if (songProjectionInfo2.getAlpha()>0) {
                mainActivityInterface.getCustomAnimation().faderAnimation(songProjectionInfo2,crossFadeTime,false);
            }



        } else {
            songProjectionInfo2.setSongTitle(mainActivityInterface.getSong().getTitle());
            songProjectionInfo2.setSongAuthor(mainActivityInterface.getSong().getAuthor());
            songProjectionInfo2.setSongCopyright(mainActivityInterface.getSong().getCopyright());

            // Fade in 2
            songProjectionInfo2.setVisibility(View.VISIBLE);
            mainActivityInterface.getCustomAnimation().faderAnimation(songProjectionInfo2,crossFadeTime,true);
            // Fade out 1
            if (songProjectionInfo1.getAlpha()>0) {
                mainActivityInterface.getCustomAnimation().faderAnimation(songProjectionInfo1,crossFadeTime,false);
            }
        }
        Log.d(TAG,"title1="+songProjectionInfo1.getSongTitle());
        Log.d(TAG,"title2="+songProjectionInfo2.getSongTitle());
        songProjectionInfo1.postDelayed(() -> {
            Log.d(TAG,"info1.getVisibility()="+songProjectionInfo1.getVisibility());
            Log.d(TAG,"info1.getAlpha()="+songProjectionInfo1.getAlpha());
        },crossFadeTime);
        songProjectionInfo2.postDelayed(() -> {
            Log.d(TAG,"info2.getVisibility()="+songProjectionInfo2.getVisibility());
            Log.d(TAG,"info2.getAlpha()="+songProjectionInfo2.getAlpha());
        },crossFadeTime);

    }

    public void setSongContentPrefs() {
        trimLines = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "trimLines", false);
        trimSections = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "trimSections", false);
        addSectionSpace = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "addSectionSpace", true);
        boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c, "boldChordHeading", false);
        scaleChords = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "scaleChords", 0.8f);
    }
    public void setSongContent() {

        // Just like we do with the song processing, we draw the sections to the test layout
        // Then measure them, work out the best orientation and scaling
        // Then remove from the test layout and reattach to the song layout.

        // Clear any existing views from the test layout.  We don't fade out existing song layout until we are ready
        testLayout.removeAllViews();

        // Decide if this is an XML, PDF or IMG file and proceed accordingly
        if (mainActivityInterface.getSong().getFiletype().equals("XML")) {
            mainActivityInterface.setSectionViews(null);
            setSectionViews();
        }
    }


    private void setSectionViews() {

        mainActivityInterface.setSectionViews(mainActivityInterface.getProcessSong().
                setSongInLayout(c, mainActivityInterface, mainActivityInterface.getSong().getLyrics(),
                        false, true));

        // Draw them to the screen test layout for measuring
        ViewTreeObserver testObs = testLayout.getViewTreeObserver();
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
                //screenWidth = pageHolder.getMeasuredWidth();
                //screenHeight = pageHolder.getMeasuredHeight();

                // Calculate the scale factor for each section individually
                // For each meausured view, get the max x and y scale value
                // Check they are less than the max preferred value
                for (int x=0; x<mainActivityInterface.getSectionWidths().size(); x++) {
                    float max_x = (float)screenWidth/(float)mainActivityInterface.getSectionWidths().get(x);
                    float max_y = (float)screenHeight/(float)mainActivityInterface.getSectionHeights().get(x);
                    // The text size is 14sp by default.  Compare this to the pref
                    float best = Math.min(max_x,max_y);
                    if (best*14f > fontSizePresoMax) {
                        best = fontSizePresoMax*14f;
                    }
                    mainActivityInterface.getSectionViews().get(x).setPivotX(0f);
                    mainActivityInterface.getSectionViews().get(x).setPivotY(0f);
                    if (best>0) {
                        mainActivityInterface.getSectionViews().get(x).setScaleX(best);
                        mainActivityInterface.getSectionViews().get(x).setScaleY(best);
                    }
                }

                // We can now remove the views from the test layout and remove this listener
                testLayout.removeAllViews();
                testLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });
        for (View view:mainActivityInterface.getSectionViews()) {
            testLayout.addView(view);
        }

    }


    /*





    void fixBackground(Context c, Preferences preferences, StorageAccess storageAccess, ImageView projected_BackgroundImage,
                       SurfaceHolder projected_SurfaceHolder, SurfaceView projected_SurfaceView) {
        // Images and video backgrounds
        String img1 = preferences.getMyPreferenceString(c,"backgroundImage1","ost_bg.png");
        Uri img1Uri;
        if (img1.equals("ost_bg.png") || img1.startsWith("../")) {
            // This is a localised file, so get the properlocation
            if (img1.equals("ost_bg.png")) {
                img1 = "../Backgrounds/ost_bg.png";
            }
            img1Uri = storageAccess.fixLocalisedUri(c,preferences,img1);
        } else {
            img1Uri = Uri.parse(preferences.getMyPreferenceString(c,"backgroundImage1","ost_bg.png"));
        }
        String img2 = preferences.getMyPreferenceString(c,"backgroundImage2","ost_bg.png");
        Uri img2Uri;
        if (img2.equals("ost_bg.png") || img2.startsWith("../")) {
            // This is a localised file, so get the properlocation
            if (img2.equals("ost_bg.png")) {
                img2 = "../Backgrounds/ost_bg.png";
            }
            img2Uri = storageAccess.fixLocalisedUri(c,preferences,img2);
        } else {
            img2Uri = Uri.parse(preferences.getMyPreferenceString(c,"backgroundImage2","ost_bg.png"));
        }
        String vid1 = preferences.getMyPreferenceString(c,"backgroundVideo1","");
        Uri vid1Uri;
        if (vid1.startsWith("../")) {
            // This is a localised file, so get the properlocation
            vid1Uri = storageAccess.fixLocalisedUri(c,preferences,vid1);
        } else if (vid1.isEmpty()) {
            vid1Uri = null;
        } else {
            vid1Uri = Uri.parse(preferences.getMyPreferenceString(c,"backgroundVideo1",""));
        }
        String vid2 = preferences.getMyPreferenceString(c,"backgroundVideo2","");
        Uri vid2Uri;
        if (vid2.startsWith("../")) {
            // This is a localised file, so get the properlocation
            vid2Uri = storageAccess.fixLocalisedUri(c,preferences,vid2);
        } else if (vid2.isEmpty()) {
            vid2Uri = null;
        } else {
            vid2Uri = Uri.parse(preferences.getMyPreferenceString(c,"backgroundVideo2",""));
        }

        // Decide if user is using video or image for background
        Uri imgUri;
        switch (preferences.getMyPreferenceString(c,"backgroundTypeToUse","image")) {
            case "image":
                projected_BackgroundImage.setVisibility(View.VISIBLE);
                projected_SurfaceView.setVisibility(View.INVISIBLE);
                if (StaticVariables.mediaPlayer != null && StaticVariables.mediaPlayer.isPlaying()) {
                    StaticVariables.mediaPlayer.pause();
                }
                if (preferences.getMyPreferenceString(c,"backgroundToUse","img1").equals("img1")) {
                    imgUri = img1Uri;
                } else {
                    imgUri = img2Uri;
                }

                if (storageAccess.uriExists(c, imgUri)) {
                    if (imgUri != null && imgUri.getLastPathSegment() != null && imgUri.getLastPathSegment().contains("ost_bg.png")) {
                        projected_BackgroundImage.setImageDrawable(StaticVariables.defimage);
                    } else {
                        RequestOptions myOptions = new RequestOptions()
                                .centerCrop();
                        GlideApp.with(c).load(imgUri).apply(myOptions).into(projected_BackgroundImage);
                    }
                    projected_BackgroundImage.setVisibility(View.VISIBLE);
                    CustomAnimations.faderAnimationCustomAlpha(projected_BackgroundImage,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),
                            projected_BackgroundImage.getAlpha(),preferences.getMyPreferenceFloat(c,"presoBackgroundAlpha",0.8f));

                }
                break;
            case "video":
                projected_BackgroundImage.setVisibility(View.INVISIBLE);
                projected_SurfaceView.setVisibility(View.VISIBLE);

                if (preferences.getMyPreferenceString(c,"backgroundToUse","img1").equals("vid1")) {
                    StaticVariables.vidUri = vid1Uri;
                } else {
                    StaticVariables.vidUri = vid2Uri;
                }
                try {
                    Log.d("d", "Trying to load video background");
                    reloadVideo(c,preferences,projected_SurfaceHolder,projected_SurfaceView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                projected_BackgroundImage.setImageDrawable(null);
                projected_BackgroundImage.setVisibility(View.GONE);

                break;
            default:
                projected_BackgroundImage.setImageDrawable(null);
                projected_BackgroundImage.setVisibility(View.GONE);
                break;
        }
    }

    void updateAlpha(Context c, Preferences preferences, ImageView projected_BackgroundImage,
                     ImageView projected_SurfaceView_Alpha) {
        float alpha = preferences.getMyPreferenceFloat(c,"presoBackgroundAlpha",0.8f);
        if (preferences.getMyPreferenceString(c,"backgroundToUse","img1").contains("vid")) {
            projected_SurfaceView_Alpha.setVisibility(View.VISIBLE);
            projected_SurfaceView_Alpha.setAlpha(1.0f-alpha);
            projected_SurfaceView_Alpha.setBackgroundColor(StaticVariables.lyricsBackgroundColor);
        } else {
            projected_BackgroundImage.setAlpha(alpha);
            projected_SurfaceView_Alpha.setVisibility(View.INVISIBLE);
        }
    }

    void presenterThemeSetUp(Context c, Preferences preferences, LinearLayout presentermode_bottombit, TextView presentermode_title,
                             TextView presentermode_author, TextView presentermode_copyright, TextView presentermode_ccli, TextView presentermode_alert) {
        // Set the text at the bottom of the page to match the presentation text colour
        presentermode_title.setTypeface(StaticVariables.typefacePresoInfo);
        presentermode_author.setTypeface(StaticVariables.typefacePresoInfo);
        presentermode_copyright.setTypeface(StaticVariables.typefacePresoInfo);
        presentermode_ccli.setTypeface(StaticVariables.typefacePresoInfo);
        presentermode_alert.setTypeface(StaticVariables.typefacePresoInfo);
        presentermode_title.setTextColor(StaticVariables.presoInfoColor);
        presentermode_author.setTextColor(StaticVariables.presoInfoColor);
        presentermode_copyright.setTextColor(StaticVariables.presoInfoColor);
        presentermode_ccli.setTextColor(StaticVariables.presoInfoColor);
        presentermode_alert.setTextColor(StaticVariables.presoAlertColor);
        presentermode_title.setTextSize(preferences.getMyPreferenceFloat(c,"presoTitleTextSize", 14.0f));
        presentermode_author.setTextSize(preferences.getMyPreferenceFloat(c,"presoAuthorTextSize", 12.0f));
        presentermode_copyright.setTextSize(preferences.getMyPreferenceFloat(c,"presoCopyrightTextSize", 12.0f));
        presentermode_ccli.setTextSize(preferences.getMyPreferenceFloat(c,"presoCopyrightTextSize", 12.0f));
        presentermode_alert.setTextSize(preferences.getMyPreferenceFloat(c,"presoAlertTextSize", 12.0f));
        presentermode_title.setShadowLayer(preferences.getMyPreferenceFloat(c,"presoTitleTextSize", 14.0f) / 2.0f, 4, 4, StaticVariables.presoShadowColor);
        presentermode_author.setShadowLayer(preferences.getMyPreferenceFloat(c,"presoAuthorTextSize", 12.0f) / 2.0f, 4, 4, StaticVariables.presoShadowColor);
        presentermode_copyright.setShadowLayer(preferences.getMyPreferenceFloat(c,"presoCopyrightTextSize", 14.0f) / 2.0f, 4, 4, StaticVariables.presoShadowColor);
        presentermode_ccli.setShadowLayer(preferences.getMyPreferenceFloat(c,"presoCopyrightTextSize", 14.0f) / 2.0f, 4, 4, StaticVariables.presoShadowColor);
        presentermode_alert.setShadowLayer(preferences.getMyPreferenceFloat(c,"presoAlertTextSize", 12.0f) / 2.0f, 4, 4, StaticVariables.presoShadowColor);
        presentermode_title.setGravity(preferences.getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        presentermode_author.setGravity(preferences.getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        presentermode_copyright.setGravity(preferences.getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        presentermode_ccli.setGravity(preferences.getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        // IV - Align alert text the same as lyrics
        presentermode_alert.setGravity(preferences.getMyPreferenceInt(c,"presoLyricsAlign", Gravity.END));
        presentermode_bottombit.setBackgroundColor(ColorUtils.setAlphaComponent(StaticVariables.presoShadowColor,
                (int)(255*preferences.getMyPreferenceFloat(c,"presoInfoBarAlpha",0.5f))));
    }
    void presenterStartUp(final Context c, final Preferences preferences, final StorageAccess storageAccess, final ImageView projected_BackgroundImage,
                          final SurfaceHolder projected_SurfaceHolder, final SurfaceView projected_SurfaceView) {
        // After the fadeout time, set the background and fade in
        Handler h = new Handler();
        h.postDelayed(() -> {
            // Try to set the new background
            fixBackground(c, preferences, storageAccess, projected_BackgroundImage,projected_SurfaceHolder,projected_SurfaceView);
            // IV - fixBackground does a logo fade in
        }, preferences.getMyPreferenceInt(c,"presoTransitionTime",800));
    }
    // The logo stuff, animations and blanking the screen
    void setUpLogo(Context c, Preferences preferences, StorageAccess storageAccess, ImageView projected_Logo, int availableWidth_1col, int availableScreenHeight) {
        // If the customLogo doesn't exist, use the default one
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int imgwidth = 1024;
        int imgheight = 500;
        float xscale;
        float yscale;
        boolean usingcustom = false;
        Uri customLogo = storageAccess.fixLocalisedUri(c, preferences, preferences.getMyPreferenceString(c, "customLogo", "ost_logo.png"));
        if (customLogo!=null && !customLogo.toString().contains("ost_logo") && storageAccess.uriExists(c, customLogo)) {
            InputStream inputStream = storageAccess.getInputStream(c, customLogo);
            // Get the sizes of the custom logo
            BitmapFactory.decodeStream(inputStream, null, options);
            imgwidth = options.outWidth;
            imgheight = options.outHeight;
            if (imgwidth > 0 && imgheight > 0) {
                usingcustom = true;
            }
        }

        xscale = ((float) availableWidth_1col *
                preferences.getMyPreferenceFloat(c, "customLogoSize", 0.5f)) / (float) imgwidth;
        yscale = ((float) availableScreenHeight *
                preferences.getMyPreferenceFloat(c, "customLogoSize", 0.5f)) / (float) imgheight;

        if (xscale > yscale) {
            xscale = yscale;
        }

        int logowidth = (int) ((float) imgwidth * xscale);
        int logoheight = (int) ((float) imgheight * xscale);

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(logowidth, logoheight);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        projected_Logo.setLayoutParams(rlp);
        if (usingcustom) {
            RequestOptions myOptions = new RequestOptions()
                    .fitCenter()
                    .override(logowidth, logoheight);
            GlideApp.with(c).load(customLogo).apply(myOptions).into(projected_Logo);
        } else {
            projected_Logo.setImageDrawable(ResourcesCompat.getDrawable(c.getResources(),R.drawable.ost_logo, c.getTheme()));
        }
        // IV - Logo display removed.  A change meaning showLogo (with all of it's logic) must be explicitly made to display logo
    }
    void showLogoPrep () {
        // IV - Indicates the delayed show call will be active unless overridden
        showLogoActive = true;
        showBackgroundActive = false;
    }
    void showBackgroundPrep () {
        // IV - Indicates the delayed show call will be active unless overridden
        showLogoActive = true;
        showBackgroundActive = true;
    }
    void showLogo(Context c, Preferences preferences, ImageView projected_ImageView, LinearLayout projected_LinearLayout, RelativeLayout pageHolder,
                  ImageView projected_Logo) {
        if (showLogoActive) {
            StaticVariables.panicRequired = false;
            // IV - If the infobar has not completed an 'Until' period, reset
            if (System.currentTimeMillis() < infoBarUntilTime) {
                StaticVariables.infoBarChangeRequired = true;
            }
            // IV - Fade out stale content
            if (projected_ImageView.getAlpha() > 0.0f) {
                CustomAnimations.faderAnimation(projected_ImageView, (int) (0.97 * preferences.getMyPreferenceInt(c, "presoTransitionTime", 800)), false);
            }
            if (projected_LinearLayout.getAlpha() > 0.0f) {
                CustomAnimations.faderAnimation(projected_LinearLayout, (int) (0.97 * preferences.getMyPreferenceInt(c, "presoTransitionTime", 800)), false);
            }
            if (projected_Logo.getAlpha() > 0.0f) {
                CustomAnimations.faderAnimation(projected_Logo, (int) (0.97 * preferences.getMyPreferenceInt(c, "presoTransitionTime", 800)), false);
            }

            Handler h = new Handler();
            h.postDelayed(() -> {
                if (showLogoActive) {
                    // Fade in logo
                    if (!showBackgroundActive) {
                        CustomAnimations.faderAnimation(projected_Logo, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800), true);
                    }
                    // If we are black screen, fade the page back in
                    if (pageHolder.getVisibility() == View.INVISIBLE) {
                        CustomAnimations.faderAnimation(pageHolder,2 * preferences.getMyPreferenceInt(c, "presoTransitionTime", 800), true);
                    }
                }
            }, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800));
            // IV - Another panic!
            Handler h2 = new Handler();
            h2.postDelayed(() -> {
                if (showLogoActive) {
                    if (!showBackgroundActive) {
                        projected_Logo.setAlpha(1.00f);
                    }
                    pageHolder.setAlpha(1.00f);
                }
            }, 5L * preferences.getMyPreferenceInt(c, "presoTransitionTime", 800));
        }
    }
    void hideLogo(Context c, Preferences preferences, ImageView projected_Logo) {
        // IV - On hide after the startup logo, setup to display song info
        if (infoBarUntilTime == 0) {
            StaticVariables.infoBarChangeRequired = true;
        }
        // IV - Makes sure any delayed showLogo calls do not undo the fade!
        showLogoActive = false;
        showBackgroundActive = false;
        // IV - Make sure song Alert display is considered (song / alert state may have changed)
        infoBarAlertState = "";
        CustomAnimations.faderAnimation(projected_Logo,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);
    }
    void blankUnblankDisplay(Context c, Preferences preferences, RelativeLayout pageHolder, boolean unblank) {
        blankActive = !unblank;
        // IV - Make sure song Alert display is considered (song / alert state may have changed)
        infoBarAlertState = "";
        if (!unblank) {
            StaticVariables.panicRequired = false;
            // IV - If the infobar has not completed an 'Until' period, reset
            if (System.currentTimeMillis() < infoBarUntilTime) {
                StaticVariables.infoBarChangeRequired = true;
            }
        }
        CustomAnimations.faderAnimation(pageHolder, (preferences.getMyPreferenceInt(c,"presoTransitionTime",800)),unblank);
        // IV - Another panic!
        Handler h2 = new Handler();
        h2.postDelayed(() -> {
            if (blankActive) {
                pageHolder.setAlpha(0.00f);
            } else {
                pageHolder.setAlpha(1.00f);
            }
        }, 5L * preferences.getMyPreferenceInt(c, "presoTransitionTime", 800));
    }
    private void animateIn(Context c, Preferences preferences, ImageView projected_ImageView, LinearLayout projected_LinearLayout) {
        if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
            CustomAnimations.faderAnimation(projected_ImageView, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800), true);
        } else {
            CustomAnimations.faderAnimation(projected_LinearLayout, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800), true);
        }
    }
    private void animateOut(Context c, Preferences preferences, ImageView projected_Logo, ImageView projected_ImageView,
                            LinearLayout projected_LinearLayout, LinearLayout bottom_infobar) {
        if (projected_Logo.getAlpha() > 0.0f) {
            showLogoActive = false;
            CustomAnimations.faderAnimation(projected_Logo,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);
        }

        if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
            CustomAnimations.faderAnimation(bottom_infobar, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800), false);
        }
        // IV - Song infobar fade and screen sizing processing are handled elsewhere
        // IV - If we are not already doing a lyric fade
        if ((lyricAfterTime - 5) < System.currentTimeMillis()) {
            // IV - Fade out stale content
            // Fade out content a bit quicker, any fading infobar will then always be present during fade (no jump should the info block fade first)
            if (projected_ImageView.getAlpha() > 0.0f) {
                CustomAnimations.faderAnimation(projected_ImageView, (int) (0.97 * preferences.getMyPreferenceInt(c, "presoTransitionTime", 800)), false);
            }
            if (projected_LinearLayout.getAlpha() > 0.0f) {
                CustomAnimations.faderAnimation(projected_LinearLayout, (int) (0.97 * preferences.getMyPreferenceInt(c, "presoTransitionTime", 800)), false);
            }
        }
    }
    // Update the screen content
    void doUpdate(final Context c, final Preferences preferences, final StorageAccess storageAccess, final ProcessSong processSong,
                  final Display myscreen, LinearLayout presentermode_bottombit, final SurfaceView projected_SurfaceView,
                  ImageView projected_BackgroundImage, RelativeLayout pageHolder, ImageView projected_Logo, final ImageView projected_ImageView,
                  final LinearLayout projected_LinearLayout, LinearLayout bottom_infobar, final RelativeLayout projectedPage_RelativeLayout,
                  TextView presentermode_title, TextView presentermode_author, TextView presentermode_copyright, TextView presentermode_ccli, TextView presentermode_alert,
                  final LinearLayout col1_1, final LinearLayout col1_2, final LinearLayout col2_2, final LinearLayout col1_3,
                  final LinearLayout col2_3, final LinearLayout col3_3) {

        if (!doUpdateActive) {
            doUpdateActive = true;

            // IV - Can be called whilst previous call is still running...  Always do fade out.  Only do fade in if we are not animating out due to a later call
            // First up, animate everything away
            StaticVariables.panicRequired = false;
            animateOutActive = true;
            animateOut(c, preferences, projected_Logo, projected_ImageView, projected_LinearLayout, bottom_infobar);

            // If we have forced an update due to switching modes, set that up
            if (StaticVariables.forcecastupdate) {
                matchPresentationToMode(presentermode_bottombit, projected_SurfaceView, projected_BackgroundImage, projected_ImageView);
            }

            // If we had a black screen, fade page back in
            if (pageHolder.getVisibility() == View.INVISIBLE) {
                CustomAnimations.faderAnimation(pageHolder, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800), true);
            }

            // Just in case there is a glitch, make the stuff visible after 5x transition time
            // IV - Panic request is prevented on display of logo or blank by setting panicRequired = false;
            panicDelay = 5L * preferences.getMyPreferenceInt(c, "presoTransitionTime", 800);
            // IV - There can be multiple postDelayed calls running, each call sets a later 'After' time.
            panicAfterTime = System.currentTimeMillis() + panicDelay;
            Handler panic = new Handler();
            panic.postDelayed(() -> {
                // IV - Quick section moves mean multiple panics are active, a time based test ensures action is only taken for the last call
                // If on running the time test fails a newer postDelayed has been made
                // After the panic delay time, make sure the correct view is visible regardless of animations
                if (StaticVariables.panicRequired && !animateOutActive && ((panicAfterTime - 5) < System.currentTimeMillis())) {
                    if (FullscreenActivity.isImage || FullscreenActivity.isPDF || FullscreenActivity.isImageSlide) {
                        projected_ImageView.setVisibility(View.VISIBLE);
                        projected_LinearLayout.setVisibility(View.GONE);
                        projected_ImageView.setAlpha(1.0f);
                    } else if (FullscreenActivity.isVideo) {
                        projected_SurfaceView.setVisibility(View.VISIBLE);
                        projected_LinearLayout.setVisibility(View.GONE);
                        projected_ImageView.setVisibility(View.GONE);
                        //projected_SurfaceView.setAlpha(1.0f);
                    } else {
                        projected_LinearLayout.setVisibility(View.VISIBLE);
                        projected_ImageView.setVisibility(View.GONE);
                        projected_LinearLayout.setAlpha(1.0f);
                    }
                }
            }, panicDelay);

            presenterWriteSongInfo(c, preferences, presentermode_title, presentermode_author, presentermode_copyright, presentermode_ccli, presentermode_alert, bottom_infobar);

            // IV - There can be multiple postDelayed calls running, each call sets a later 'After' time.
            lyricDelay = preferences.getMyPreferenceInt(c, "presoTransitionTime", 800) + infoBarChangeDelay;
            infoBarChangeDelay = 0;
            lyricAfterTime = System.currentTimeMillis() + lyricDelay;

            animateOutActive = false;

            // Now run the next bit post delayed (to wait for the animate out)
            Handler h = new Handler();
            h.postDelayed(() -> {
                // IV - Not if animating out and not if the time test fails as newer postDelayed has been made
                if (!animateOutActive && (lyricAfterTime - 5) < System.currentTimeMillis()) {
                    // Wipe any current views
                    wipeAllViews(projected_LinearLayout,projected_ImageView);

                    // Check the colours colour
                    if (!StaticVariables.whichMode.equals("Presentation")) {
                        // Set the page background to the correct colour for Peformance/Stage modes
                        projectedPage_RelativeLayout.setBackgroundColor(StaticVariables.lyricsBackgroundColor);
                    }

                    // Get the size of the SurfaceView here as any infobar will be visible at this point
                    getScreenSizes(myscreen,bottom_infobar, projectedPage_RelativeLayout,preferences.getMyPreferenceFloat(c,"castRotation",0.0f));

                    StaticVariables.panicRequired = true;
                    // Decide on what we are going to show
                    if (FullscreenActivity.isPDF) {
                        doPDFPage(c,preferences,storageAccess,processSong,projected_ImageView,projected_LinearLayout);
                    } else if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide) {
                        doImagePage(c,preferences,storageAccess,projected_ImageView,projected_LinearLayout);
                    } else {
                        projected_ImageView.setVisibility(View.GONE);
                        projected_ImageView.setAlpha(0.0f);
                        switch (StaticVariables.whichMode) {
                            case "Stage":
                                prepareStageProjected(c,preferences,processSong,storageAccess,col1_1,col1_2,col2_2,col1_3,col2_3,col3_3,
                                        projected_LinearLayout,projected_ImageView);
                                break;
                            case "Performance":
                                prepareFullProjected(c,preferences,processSong,storageAccess,col1_1,col1_2,col2_2,col1_3,col2_3,col3_3,
                                        projected_LinearLayout,projected_ImageView);
                                break;
                            default:
                                preparePresenterProjected(c,preferences,processSong,storageAccess,col1_1,col1_2,col2_2,col1_3,col2_3,col3_3,
                                        projected_LinearLayout,projected_ImageView);
                                break;
                        }
                    }
                }
            }, lyricDelay);
            doUpdateActive = false;
        }
    }
    private void presenterWriteSongInfo(Context c, Preferences preferences, TextView presentermode_title, TextView presentermode_author,
                                       TextView presentermode_copyright, TextView presentermode_ccli, TextView presentermode_alert, LinearLayout bottom_infobar) {
        if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
            // IV - Force consideration of alert state when text after the Until period
            infoBarAlertState = "";
        } else {
            // IV - Overrides for when not hiding song info
            if (!preferences.getMyPreferenceBoolean(c,"presoInfoBarHide",true) && PresenterMode.alert_on.equals("N")) {
                // IV - If we are ending Alert display then force song info display else keep song info by extending the until time
                if (infoBarAlertState.equals("Y")) {
                    StaticVariables.infoBarChangeRequired = true;
                } else {
                    infoBarUntilTime = System.currentTimeMillis() + 10000;
                }
            }

            // IV - Exceutes for first section after a change is requested (for a fresh song info display)
            // IV - AND section changes AFTER the subsequent 'Until' period end (for alert display)
            // IV - NOT for section changes after the first that occur BEFORE the end of the Until period

            if ((StaticVariables.infoBarChangeRequired) || (System.currentTimeMillis() > infoBarUntilTime)) {
                String new_author = "";
                String new_title = "";
                String new_copyright = "";
                String new_ccli = "";

                // IV - Do once for first section after change required
                if (StaticVariables.infoBarChangeRequired) {
                    new_author = StaticVariables.mAuthor.trim();
                    if (!new_author.equals(""))
                        new_author = c.getString(R.string.wordsandmusicby) + " " + new_author;

                    new_copyright = StaticVariables.mCopyright.trim();
                    if (!new_copyright.isEmpty() && (!new_copyright.contains("©")))
                        new_copyright = "© " + new_copyright;

                    new_ccli = preferences.getMyPreferenceString(c, "ccliLicence", "");
                    if (!new_ccli.isEmpty() && (!StaticVariables.mCCLI.isEmpty())) {
                        new_ccli = c.getString(R.string.usedbypermision) + " CCLI " + c.getString(R.string.ccli_licence) + " " + new_ccli;
                    } else {
                        new_ccli = "";
                    }

                    new_title = StaticVariables.mTitle;
                    if (new_title.startsWith("_")) new_title = "";
                    else {
                        // IV - If we have only a title, use without quotes
                        if ((new_author + new_copyright + new_ccli).equals("")) {
                            new_title = StaticVariables.mTitle.trim();
                        } else {
                            new_title = "\"" + StaticVariables.mTitle.trim() + "\"";
                        }
                    }
                }

                // IV - We will need to animate if we pass this test - no false positives
                if (StaticVariables.infoBarChangeRequired || !infoBarAlertState.equals(PresenterMode.alert_on)) {
                    // IV - Fade to 0.01f to keep on screen
                    CustomAnimations.faderAnimationCustomAlpha(bottom_infobar, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800), bottom_infobar.getAlpha(), 0.01f);
                    // IV - Delay lyrics to ensure new infobar is available for correct screen sizing - Set also to provide a good transition
                    infoBarChangeDelay = 200;
                    // IV - Rapid song changes can see multiple handlers running - ensure that none show an alert
                    if (StaticVariables.infoBarChangeRequired) {
                        infoBarAlertState = "N";
                    } else {
                        infoBarAlertState = PresenterMode.alert_on;
                    }

                    // IV - Run the next bit post delayed (to wait for the animate out)
                    Handler h = new Handler();
                    String finalNew_title = new_title;
                    String finalNew_author = new_author;
                    String finalNew_copyright = new_copyright;
                    String finalNew_ccli = new_ccli;
                    h.postDelayed(() -> {
                        // IV - Finish the fade
                        bottom_infobar.setAlpha(0.0f);
                        adjustVisibility(presentermode_author, finalNew_author);
                        adjustVisibility(presentermode_copyright, finalNew_copyright);
                        adjustVisibility(presentermode_ccli, finalNew_ccli);
                        adjustVisibility(presentermode_title, finalNew_title);
                        if (StaticVariables.infoBarChangeRequired) {
                            StaticVariables.infoBarChangeRequired = false;
                            // IV - Make sure song info is seen for at least 10s
                            infoBarUntilTime = System.currentTimeMillis() + 10000;
                            presentermode_alert.setVisibility(View.GONE);
                            // IV - Force consideration of alert state after the Until period
                            infoBarAlertState = "";
                            // IV - Fade in only if something to show
                            if (new StringBuilder().append(finalNew_title).append(finalNew_author).append(finalNew_copyright).append(finalNew_ccli).toString().trim() != "") {
                                CustomAnimations.faderAnimation(bottom_infobar, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800), true);
                            }
                        } else {
                            if (infoBarAlertState.equals("Y")) {
                                // IV - Align alert text the same as lyrics
                                presentermode_alert.setGravity(preferences.getMyPreferenceInt(c, "presoLyricsAlign", Gravity.END));
                                presentermode_alert.setVisibility(View.VISIBLE);
                                h.postDelayed(() -> {
                                    CustomAnimations.faderAnimation(bottom_infobar, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800), true);
                                }, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800));
                            } else {
                                presentermode_alert.setVisibility(View.GONE);
                            }
                        }
                    }, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800));
                }
            }
        }
    }
    private void adjustVisibility(TextView v, String val) {
        v.setText(val);
        if (val==null || val.isEmpty()) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }
    private void doPDFPage(Context c, Preferences preferences, StorageAccess storageAccess, ProcessSong processSong, ImageView projected_ImageView, LinearLayout projected_LinearLayout) {
        Bitmap bmp = processSong.createPDFPage(c, preferences, storageAccess, StaticVariables.availableScreenWidth, StaticVariables.availableScreenHeight, "Y");
        projected_ImageView.setVisibility(View.GONE);
        projected_ImageView.setAlpha(0.0f);
        projected_ImageView.setBackgroundColor(StaticVariables.white);
        // IV - Make sure it starts clear
        projected_ImageView.setImageBitmap(null);
        if (bmp != null) {
            projected_ImageView.setImageBitmap(bmp);
        }
        animateIn(c,preferences,projected_ImageView,projected_LinearLayout);
    }
    private void doImagePage(Context c, Preferences preferences, StorageAccess storageAccess, ImageView projected_ImageView, LinearLayout projected_LinearLayout) {
        projected_ImageView.setVisibility(View.GONE);
        projected_ImageView.setAlpha(0.0f);
        projected_ImageView.setBackgroundColor(StaticVariables.white);
        // IV - Make sure it starts clear
        projected_ImageView.setImageBitmap(null);
        // Process the image location into an URI
        Uri imageUri;
        if (StaticVariables.uriToLoad==null) {
            imageUri = storageAccess.getUriForItem(c, preferences, "Songs", StaticVariables.whichSongFolder, StaticVariables.songfilename);
        } else {
            imageUri = StaticVariables.uriToLoad;
        }

        if (imageUri != null &&  storageAccess.uriExists(c, imageUri)) {
            RequestOptions myOptions = new RequestOptions()
                    .fitCenter().override(projected_LinearLayout.getMeasuredWidth(), projected_LinearLayout.getMeasuredHeight());
            GlideApp.with(c).load(imageUri).apply(myOptions).into(projected_ImageView);
        }
        animateIn(c, preferences,projected_ImageView,projected_LinearLayout);
    }
    private void wipeAllViews(LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        projected_LinearLayout.removeAllViews();
        projected_ImageView.setImageBitmap(null);
    }
    void updateAlert(Context c, Preferences preferences, boolean show, TextView presentermode_alert) {
        // IV - A doUpdate is done elsewhere to handle fades
        if (show) {
            // IV - Set up to ensure no song info display
            infoBarUntilTime = 0;
            StaticVariables.infoBarChangeRequired = false;
            PresenterMode.alert_on = "Y";
            presentermode_alert.setText(preferences.getMyPreferenceString(c,"presoAlertText",""));
            presentermode_alert.setTypeface(StaticVariables.typefacePresoInfo);
            presentermode_alert.setTextSize(preferences.getMyPreferenceFloat(c,"presoAlertTextSize", 12.0f));
            presentermode_alert.setTextColor(StaticVariables.presoAlertColor);
            presentermode_alert.setShadowLayer(preferences.getMyPreferenceFloat(c,"presoAlertTextSize", 12.0f) / 2.0f, 4, 4, StaticVariables.presoShadowColor);
        } else {
            PresenterMode.alert_on = "N";
        }
    }




    // MediaPlayer stuff
    void prepareMediaPlayer(Context c, Preferences preferences, SurfaceHolder projected_SurfaceHolder, Display myscreen, LinearLayout bottom_infobar, RelativeLayout projectedPage_RelativeLayout) {
        // Get the size of the SurfaceView
        getScreenSizes(myscreen,bottom_infobar,projectedPage_RelativeLayout,preferences.getMyPreferenceFloat(c,"castRotation",0.0f));
        StaticVariables.mediaPlayer = new MediaPlayer();
        StaticVariables.mediaPlayer.setDisplay(projected_SurfaceHolder);
        if (preferences.getMyPreferenceString(c,"backgroundTypeToUse","image").equals("video")) {
            try {
                StaticVariables.mediaPlayer.setDataSource(c, StaticVariables.vidUri);
                StaticVariables.mediaPlayer.prepare();

            } catch (Exception e) {
                Log.d("PresentationService", "Error setting data source for video");
            }
        }
    }
    void mediaPlayerIsPrepared(SurfaceView projected_SurfaceView) {
        try {
            // Get the video sizes so we can scale appropriately
            int width = StaticVariables.mediaPlayer.getVideoWidth();
            int height = StaticVariables.mediaPlayer.getVideoHeight();
            float max_xscale = (float) StaticVariables.screenWidth / (float) width;
            float max_yscale = (float) StaticVariables.screenHeight / (float) height;
            if (max_xscale > max_yscale) {
                // Use the y scale
                width = (int) (max_yscale * (float) width);
                height = (int) (max_yscale * (float) height);
            } else {
                // Else use the x scale
                width = (int) (max_xscale * (float) width);
                height = (int) (max_xscale * (float) height);
            }
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
            projected_SurfaceView.setLayoutParams(params);
            StaticVariables.mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void reloadVideo(final Context c, final Preferences preferences, final SurfaceHolder projected_SurfaceHolder, final SurfaceView projected_SurfaceView) {
        if (StaticVariables.mediaPlayer == null) {
            StaticVariables.mediaPlayer = new MediaPlayer();
            try {
                StaticVariables.mediaPlayer.setDisplay(projected_SurfaceHolder);
                StaticVariables.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            StaticVariables.mediaPlayer.reset();
        } catch (Exception e) {
            Log.d("PresentationService", "Error resetting mMediaPlayer");
        }

        if (preferences.getMyPreferenceString(c,"backgroundTypeToUse","image").equals("video")) {
            try {
                Log.d("Presemttion Common","viUri="+StaticVariables.vidUri);
                StaticVariables.mediaPlayer.setDataSource(c, StaticVariables.vidUri);
                StaticVariables.mediaPlayer.setOnPreparedListener(mp -> {
                    try {
                        // Get the video sizes so we can scale appropriately
                        int width = mp.getVideoWidth();
                        int height = mp.getVideoHeight();
                        float max_xscale = (float) StaticVariables.screenWidth / (float) width;
                        float max_yscale = (float) StaticVariables.screenHeight / (float) height;
                        if (max_xscale > max_yscale) {
                            // Use the y scale
                            width = (int) (max_yscale * (float) width);
                            height = (int) (max_yscale * (float) height);

                        } else {
                            // Else use the x scale
                            width = (int) (max_xscale * (float) width);
                            height = (int) (max_xscale * (float) height);
                        }
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
                        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                        projected_SurfaceView.setLayoutParams(params);
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                StaticVariables.mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                    if (mediaPlayer != null) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                        mediaPlayer.reset();
                    }
                    try {
                        reloadVideo(c,preferences,projected_SurfaceHolder,projected_SurfaceView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                StaticVariables.mediaPlayer.prepare();

            } catch (Exception e) {
                Log.d("PresentationService", "Error setting data source for video");
            }
        }
    }




    // Writing the views for PerformanceMode
    private void prepareFullProjected (final Context c, final Preferences preferences, final ProcessSong processSong, final StorageAccess storageAccess,
                 final LinearLayout col1_1, final LinearLayout col1_2, final LinearLayout col2_2, final LinearLayout col1_3,
                 final LinearLayout col2_3, final LinearLayout col3_3, final LinearLayout projected_LinearLayout, final ImageView projected_ImageView) {
        if (StaticVariables.activity!=null) {
            new Thread(() -> {

                // Updating views on the UI
                StaticVariables.activity.runOnUiThread(() -> {
                    // Remove the old view contents
                    try {
                        col1_1.removeAllViews();
                        col1_2.removeAllViews();
                        col2_2.removeAllViews();
                        col1_3.removeAllViews();
                        col2_3.removeAllViews();
                        col3_3.removeAllViews();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    LinearLayout test1_1, test1_2, test2_2, test1_3, test2_3, test3_3;

                    // Prepare the new views to add to 1,2 and 3 colums ready for measuring
                    // Go through each section
                    for (int x = 0; x < StaticVariables.songSections.length; x++) {

                        test1_1 = processSong.projectedSectionView(c, x, 12.0f,
                                storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                                StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                        col1_1.addView(test1_1);

                        if (x < FullscreenActivity.halfsplit_section) {
                            test1_2 = processSong.projectedSectionView(c, x, 12.0f,
                                    storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                                    StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                            col1_2.addView(test1_2);
                        } else {
                            test2_2 = processSong.projectedSectionView(c, x, 12.0f,
                                    storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                                    StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                            col2_2.addView(test2_2);
                        }

                        if (x < FullscreenActivity.thirdsplit_section) {
                            test1_3 = processSong.projectedSectionView(c, x, 12.0f,
                                    storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                                    StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                            col1_3.addView(test1_3);
                        } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                            test2_3 = processSong.projectedSectionView(c, x, 12.0f,
                                    storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                                    StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                            col2_3.addView(test2_3);
                        } else {
                            test3_3 = processSong.projectedSectionView(c, x, 12.0f,
                                    storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                                    StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                            col3_3.addView(test3_3);
                        }
                    }

                    // Now premeasure the views
                    // GE try to catch errors sometimes occuring
                    tryMeasure(col1_1);
                    tryMeasure(col1_2);
                    tryMeasure(col2_2);
                    tryMeasure(col1_3);
                    tryMeasure(col2_3);
                    tryMeasure(col3_3);
                    //col1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    //col1_2.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    //col2_2.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    //col1_3.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    //col2_3.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    //col3_3.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                    // Get the widths and heights of the sections
                    int widthofsection1_1 = col1_1.getMeasuredWidth();
                    int heightofsection1_1 = col1_1.getMeasuredHeight();
                    int widthofsection1_2 = col1_2.getMeasuredWidth();
                    int heightofsection1_2 = col1_2.getMeasuredHeight();
                    int widthofsection2_2 = col2_2.getMeasuredWidth();
                    int heightofsection2_2 = col2_2.getMeasuredHeight();
                    int widthofsection1_3 = col1_3.getMeasuredWidth();
                    int heightofsection1_3 = col1_3.getMeasuredHeight();
                    int widthofsection2_3 = col2_3.getMeasuredWidth();
                    int heightofsection2_3 = col2_3.getMeasuredHeight();
                    int widthofsection3_3 = col3_3.getMeasuredWidth();
                    int heightofsection3_3 = col3_3.getMeasuredHeight();

                    // Now display the song!
                    projected_LinearLayout.removeAllViews();

                    // We know the widths and heights of all of the view (1,2 and 3 columns).
                    // Decide which is best by looking at the scaling

                    int colstouse = 1;
                    // We know the size of each section, so we just need to know which one to display
                    float maxwidth_scale1_1 = ((float) StaticVariables.availableWidth_1col) / (float) widthofsection1_1;
                    float maxwidth_scale1_2 = ((float) StaticVariables.availableWidth_2col) / (float) widthofsection1_2;
                    float maxwidth_scale2_2 = ((float) StaticVariables.availableWidth_2col) / (float) widthofsection2_2;
                    float maxwidth_scale1_3 = ((float) StaticVariables.availableWidth_3col) / (float) widthofsection1_3;
                    float maxwidth_scale2_3 = ((float) StaticVariables.availableWidth_3col) / (float) widthofsection2_3;
                    float maxwidth_scale3_3 = ((float) StaticVariables.availableWidth_3col) / (float) widthofsection3_3;
                    float maxheight_scale1_1 = ((float) StaticVariables.availableScreenHeight) / (float) heightofsection1_1;
                    float maxheight_scale1_2 = ((float) StaticVariables.availableScreenHeight) / (float) heightofsection1_2;
                    float maxheight_scale2_2 = ((float) StaticVariables.availableScreenHeight) / (float) heightofsection2_2;
                    float maxheight_scale1_3 = ((float) StaticVariables.availableScreenHeight) / (float) heightofsection1_3;
                    float maxheight_scale2_3 = ((float) StaticVariables.availableScreenHeight) / (float) heightofsection2_3;
                    float maxheight_scale3_3 = ((float) StaticVariables.availableScreenHeight) / (float) heightofsection3_3;

                    if (maxheight_scale1_1 < maxwidth_scale1_1) {
                        maxwidth_scale1_1 = maxheight_scale1_1;
                    }
                    if (maxheight_scale1_2 < maxwidth_scale1_2) {
                        maxwidth_scale1_2 = maxheight_scale1_2;
                    }
                    if (maxheight_scale2_2 < maxwidth_scale2_2) {
                        maxwidth_scale2_2 = maxheight_scale2_2;
                    }
                    if (maxheight_scale1_3 < maxwidth_scale1_3) {
                        maxwidth_scale1_3 = maxheight_scale1_3;
                    }
                    if (maxheight_scale2_3 < maxwidth_scale2_3) {
                        maxwidth_scale2_3 = maxheight_scale2_3;
                    }
                    if (maxheight_scale3_3 < maxwidth_scale3_3) {
                        maxwidth_scale3_3 = maxheight_scale3_3;
                    }

                    // Decide on the best scaling to use
                    float myfullscale = maxwidth_scale1_1;

                    if (maxwidth_scale1_2 > myfullscale && maxwidth_scale2_2 > myfullscale) {
                        colstouse = 2;
                        myfullscale = Math.min(maxwidth_scale1_2, maxwidth_scale2_2);
                    }

                    if (maxwidth_scale1_3 > myfullscale && maxwidth_scale2_3 > myfullscale && maxwidth_scale3_3 > myfullscale) {
                        colstouse = 3;
                    }

                    // Now we know how many columns we should use, let's do it!
                    float maxscale = preferences.getMyPreferenceFloat(c, "fontSizePresoMax", 40.0f) / 12.0f;

                    switch (colstouse) {
                        case 1:
                            if (maxwidth_scale1_1 > maxscale) {
                                maxwidth_scale1_1 = maxscale;
                            }
                            projectedPerformanceView1col(c, preferences, storageAccess, processSong, maxwidth_scale1_1,
                                    projected_LinearLayout, projected_ImageView);
                            break;

                        case 2:
                            if (maxwidth_scale1_2 > maxscale) {
                                maxwidth_scale1_2 = maxscale;
                            }
                            if (maxwidth_scale2_2 > maxscale) {
                                maxwidth_scale2_2 = maxscale;
                            }
                            projectedPerformanceView2col(c, preferences, storageAccess, processSong, maxwidth_scale1_2, maxwidth_scale2_2,
                                    projected_LinearLayout, projected_ImageView);
                            break;

                        case 3:
                            if (maxwidth_scale1_3 > maxscale) {
                                maxwidth_scale1_3 = maxscale;
                            }
                            if (maxwidth_scale2_3 > maxscale) {
                                maxwidth_scale2_3 = maxscale;
                            }
                            if (maxwidth_scale3_3 > maxscale) {
                                maxwidth_scale3_3 = maxscale;
                            }
                            projectedPerformanceView3col(c, preferences, storageAccess, processSong, maxwidth_scale1_3, maxwidth_scale2_3, maxwidth_scale3_3,
                                    projected_LinearLayout, projected_ImageView);
                            break;
                    }
                });
            }).start();
        }
    }
    private void projectedPerformanceView1col(Context c, Preferences preferences, StorageAccess storageAccess, ProcessSong processSong,
                                              float scale1_1, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {
            LinearLayout lyrics1_1 = processSong.createLinearLayout(c);
            LinearLayout box1_1 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.lyricsTextColor,
                    StaticVariables.lyricsBackgroundColor, defaultPadding);
            float fontsize1_1 = processSong.getProjectedFontSize(scale1_1);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_1.setPadding(0, 0, 0, 0);

            // Prepare the new views to add to 1,2 and 3 colums ready for measuring
            // Go through each section
            for (int x = 0; x < StaticVariables.songSections.length; x++) {
                lyrics1_1 = processSong.projectedSectionView(c, x, fontsize1_1,
                        storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                        StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(StaticVariables.availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
                llp1_1.setMargins(0, 0, 0, 0);
                lyrics1_1.setLayoutParams(llp1_1);
                lyrics1_1.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],
                        StaticVariables.lyricsVerseColor, StaticVariables.lyricsChorusColor,
                        StaticVariables.lyricsPreChorusColor, StaticVariables.lyricsBridgeColor, StaticVariables.lyricsTagColor,
                        StaticVariables.lyricsCommentColor, StaticVariables.lyricsCustomColor));
                box1_1.addView(lyrics1_1);
            }

            // Now add the display
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(StaticVariables.availableScreenWidth,
                    StaticVariables.availableScreenHeight + defaultPadding);
            llp.setMargins(0, 0, 0, 0);
            projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            box1_1.setLayoutParams(llp);
            projected_LinearLayout.addView(box1_1);
            animateIn(c, preferences, projected_ImageView, projected_LinearLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void projectedPerformanceView2col(Context c, Preferences preferences, StorageAccess storageAccess, ProcessSong processSong,
                                              float scale1_2, float scale2_2, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {
            LinearLayout lyrics1_2 = processSong.createLinearLayout(c);
            LinearLayout lyrics2_2 = processSong.createLinearLayout(c);
            LinearLayout box1_2 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.lyricsTextColor,
                    StaticVariables.lyricsBackgroundColor, defaultPadding);
            LinearLayout box2_2 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.lyricsTextColor,
                    StaticVariables.lyricsBackgroundColor, defaultPadding);
            float fontsize1_2 = processSong.getProjectedFontSize(scale1_2);
            float fontsize2_2 = processSong.getProjectedFontSize(scale2_2);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_2.setPadding(0, 0, 0, 0);
            lyrics2_2.setPadding(0, 0, 0, 0);

            // Prepare the new views to add to 1,2 and 3 colums ready for measuring
            // Go through each section
            for (int x = 0; x < StaticVariables.songSections.length; x++) {

                if (x < FullscreenActivity.halfsplit_section) {
                    lyrics1_2 = processSong.projectedSectionView(c, x, fontsize1_2,
                            storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                            StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                    LinearLayout.LayoutParams llp1_2 = new LinearLayout.LayoutParams(StaticVariables.availableWidth_2col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_2.setMargins(0, 0, 0, 0);
                    lyrics1_2.setLayoutParams(llp1_2);
                    lyrics1_2.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],
                            StaticVariables.lyricsVerseColor,StaticVariables.lyricsChorusColor, StaticVariables.lyricsPreChorusColor,
                            StaticVariables.lyricsBridgeColor,StaticVariables.lyricsTagColor,
                            StaticVariables.lyricsCommentColor,StaticVariables.lyricsCustomColor));
                    box1_2.addView(lyrics1_2);
                } else {
                    lyrics2_2 = processSong.projectedSectionView(c, x, fontsize2_2,
                            storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                            StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                    LinearLayout.LayoutParams llp2_2 = new LinearLayout.LayoutParams(StaticVariables.availableWidth_2col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp2_2.setMargins(0, 0, 0, 0);
                    lyrics2_2.setLayoutParams(llp2_2);
                    lyrics2_2.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],
                            StaticVariables.lyricsVerseColor,StaticVariables.lyricsChorusColor, StaticVariables.lyricsPreChorusColor,
                            StaticVariables.lyricsBridgeColor,StaticVariables.lyricsTagColor,
                            StaticVariables.lyricsCommentColor,StaticVariables.lyricsCustomColor));
                    box2_2.addView(lyrics2_2);
                }
            }

            // Now add the display
            LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(StaticVariables.availableWidth_2col + (defaultPadding * 2), StaticVariables.availableScreenHeight + defaultPadding);
            LinearLayout.LayoutParams llp2 = new LinearLayout.LayoutParams(StaticVariables.availableWidth_2col + (defaultPadding * 2), StaticVariables.availableScreenHeight + defaultPadding);
            llp1.setMargins(0, 0, defaultPadding, 0);
            llp2.setMargins(0, 0, 0, 0);
            projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            box1_2.setLayoutParams(llp1);
            box2_2.setLayoutParams(llp2);
            projected_LinearLayout.addView(box1_2);
            projected_LinearLayout.addView(box2_2);
            animateIn(c, preferences, projected_ImageView, projected_LinearLayout);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void projectedPerformanceView3col(Context c, Preferences preferences, StorageAccess storageAccess, ProcessSong processSong,
                                      float scale1_3, float scale2_3, float scale3_3, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {
            LinearLayout lyrics1_3 = processSong.createLinearLayout(c);
            LinearLayout lyrics2_3 = processSong.createLinearLayout(c);
            LinearLayout lyrics3_3 = processSong.createLinearLayout(c);
            LinearLayout box1_3 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.lyricsTextColor,
                    StaticVariables.lyricsBackgroundColor, defaultPadding);
            LinearLayout box2_3 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.lyricsTextColor,
                    StaticVariables.lyricsBackgroundColor, defaultPadding);
            LinearLayout box3_3 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.lyricsTextColor,
                    StaticVariables.lyricsBackgroundColor, defaultPadding);
            float fontsize1_3 = processSong.getProjectedFontSize(scale1_3);
            float fontsize2_3 = processSong.getProjectedFontSize(scale2_3);
            float fontsize3_3 = processSong.getProjectedFontSize(scale3_3);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_3.setPadding(0, 0, 0, 0);
            lyrics2_3.setPadding(0, 0, 0, 0);
            lyrics3_3.setPadding(0, 0, 0, 0);

            // Prepare the new views to add to 1,2 and 3 colums ready for measuring
            // Go through each section
            for (int x = 0; x < StaticVariables.songSections.length; x++) {
                if (x < FullscreenActivity.thirdsplit_section) {
                    lyrics1_3 = processSong.projectedSectionView(c, x, fontsize1_3,
                            storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                            StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                    LinearLayout.LayoutParams llp1_3 = new LinearLayout.LayoutParams(StaticVariables.availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_3.setMargins(0, 0, 0, 0);
                    lyrics1_3.setLayoutParams(llp1_3);
                    lyrics1_3.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],
                            StaticVariables.lyricsVerseColor,StaticVariables.lyricsChorusColor, StaticVariables.lyricsPreChorusColor,
                            StaticVariables.lyricsBridgeColor,StaticVariables.lyricsTagColor,
                            StaticVariables.lyricsCommentColor,StaticVariables.lyricsCustomColor));
                    box1_3.addView(lyrics1_3);
                } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                    lyrics2_3 = processSong.projectedSectionView(c, x, fontsize2_3,
                            storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                            StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                    LinearLayout.LayoutParams llp2_3 = new LinearLayout.LayoutParams(StaticVariables.availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp2_3.setMargins(0, 0, 0, 0);
                    lyrics2_3.setLayoutParams(llp2_3);
                    lyrics2_3.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],
                            StaticVariables.lyricsVerseColor,StaticVariables.lyricsChorusColor, StaticVariables.lyricsPreChorusColor,
                            StaticVariables.lyricsBridgeColor,StaticVariables.lyricsTagColor,
                            StaticVariables.lyricsCommentColor,StaticVariables.lyricsCustomColor));
                    box2_3.addView(lyrics2_3);
                } else {
                    lyrics3_3 = processSong.projectedSectionView(c, x, fontsize3_3,
                            storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                            StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                    LinearLayout.LayoutParams llp3_3 = new LinearLayout.LayoutParams(StaticVariables.availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp3_3.setMargins(0, 0, 0, 0);
                    lyrics3_3.setLayoutParams(llp3_3);
                    lyrics3_3.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],
                            StaticVariables.lyricsVerseColor,StaticVariables.lyricsChorusColor, StaticVariables.lyricsPreChorusColor,
                            StaticVariables.lyricsBridgeColor,StaticVariables.lyricsTagColor,
                            StaticVariables.lyricsCommentColor,StaticVariables.lyricsCustomColor));
                    box3_3.addView(lyrics3_3);
                }
            }

            // Now add the display
            LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(StaticVariables.availableWidth_3col + (defaultPadding * 2), StaticVariables.availableScreenHeight + defaultPadding);
            LinearLayout.LayoutParams llp3 = new LinearLayout.LayoutParams(StaticVariables.availableWidth_3col + (defaultPadding * 2), StaticVariables.availableScreenHeight + defaultPadding);
            llp1.setMargins(0, 0, defaultPadding, 0);
            llp3.setMargins(0, 0, 0, 0);
            projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            box1_3.setLayoutParams(llp1);
            box2_3.setLayoutParams(llp1);
            box3_3.setLayoutParams(llp3);
            projected_LinearLayout.addView(box1_3);
            projected_LinearLayout.addView(box2_3);
            projected_LinearLayout.addView(box3_3);
            animateIn(c, preferences, projected_ImageView, projected_LinearLayout);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    // Writing the views for StageMode
    private void prepareStageProjected(final Context c, final Preferences preferences, final ProcessSong processSong, final StorageAccess storageAccess,
                                       final LinearLayout col1_1, final LinearLayout col1_2, final LinearLayout col2_2, final LinearLayout col1_3,
                                       final LinearLayout col2_3, final LinearLayout col3_3, final LinearLayout projected_LinearLayout, final ImageView projected_ImageView) {

        if (StaticVariables.activity!=null) {
            new Thread(() -> {
                try {
                    // Updating views on the UI
                    //activity.runOnUiThread(new Runnable() {
                    StaticVariables.activity.runOnUiThread(() -> {
                        // Remove the old views
                        col1_1.removeAllViews();
                        col1_2.removeAllViews();
                        col2_2.removeAllViews();
                        col1_3.removeAllViews();
                        col2_3.removeAllViews();
                        col3_3.removeAllViews();

                        LinearLayout test1_1;

                        // Prepare the new view ready for measuring
                        // Go through each section
                        test1_1 = processSong.projectedSectionView(c, StaticVariables.currentSection, 12.0f,
                                storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                                StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                        col1_1.addView(test1_1);

                        // Now premeasure the views
                        col1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                        // Get the widths and heights of the sections
                        int widthofsection1_1 = col1_1.getMeasuredWidth();
                        int heightofsection1_1 = col1_1.getMeasuredHeight();

                        // We know the size of each section, so we just need to know which one to display
                        float maxwidth_scale1_1 = ((float) StaticVariables.availableWidth_1col) / (float) widthofsection1_1;
                        float maxheight_scale1_1 = ((float) StaticVariables.availableScreenHeight) / (float) heightofsection1_1;

                        if (maxheight_scale1_1 < maxwidth_scale1_1) {
                            maxwidth_scale1_1 = maxheight_scale1_1;
                        }

                        // Now we know how many columns we should use, let's do it!
                        float maxscale = preferences.getMyPreferenceFloat(c, "fontSizePresoMax", 40.0f) / 12.0f;

                        if (maxwidth_scale1_1 > maxscale) {
                            maxwidth_scale1_1 = maxscale;
                        }
                        projectedStageView1Col(c, preferences, storageAccess, processSong, maxwidth_scale1_1,
                                projected_LinearLayout, projected_ImageView);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    private void projectedStageView1Col(Context c, Preferences preferences,StorageAccess storageAccess,ProcessSong processSong,
                                        float scale1_1, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {

            LinearLayout lyrics1_1 = processSong.createLinearLayout(c);
            LinearLayout box1_1 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.lyricsTextColor,
                    StaticVariables.lyricsBackgroundColor, defaultPadding);
            float fontsize1_1 = processSong.getProjectedFontSize(scale1_1);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_1.setPadding(0, 0, 0, 0);

            // Add this section
            lyrics1_1 = processSong.projectedSectionView(c, StaticVariables.currentSection,
                    fontsize1_1,
                    storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                    StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
            LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(StaticVariables.availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp1_1.setMargins(0, 0, 0, 0);
            lyrics1_1.setLayoutParams(llp1_1);
            box1_1.addView(lyrics1_1);


            // Now add the display
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(StaticVariables.availableScreenWidth,
                    StaticVariables.availableScreenHeight + defaultPadding);
            llp.setMargins(0, 0, 0, 0);
            projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            box1_1.setLayoutParams(llp);
            projected_LinearLayout.addView(box1_1);
            animateIn(c, preferences, projected_ImageView, projected_LinearLayout);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    // Writing the views for PresenterMode
    private void preparePresenterProjected(final Context c, final Preferences preferences, final ProcessSong processSong, final StorageAccess storageAccess,
                                           final LinearLayout col1_1, final LinearLayout col1_2, final LinearLayout col2_2, final LinearLayout col1_3,
                                           final LinearLayout col2_3, final LinearLayout col3_3, final LinearLayout projected_LinearLayout, final ImageView projected_ImageView) {
        if (StaticVariables.activity != null) {
            new Thread(() -> {
                try {
                    StaticVariables.activity.runOnUiThread(() -> {
                        // Remove the old views
                        col1_1.removeAllViews();
                        col1_2.removeAllViews();
                        col2_2.removeAllViews();
                        col1_3.removeAllViews();
                        col2_3.removeAllViews();
                        col3_3.removeAllViews();

                        LinearLayout test1_1;

                        // Prepare the new view ready for measuring
                        // Go through each section
                        test1_1 = processSong.projectedSectionView(c, StaticVariables.currentSection, 12.0f,
                                storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                                StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
                        col1_1.addView(test1_1);

                        // Now premeasure the views
                        tryMeasure(col1_1);
                        // GE Catch error detected

                        // Get the widths and heights of the sections
                        int widthofsection1_1 = col1_1.getMeasuredWidth();
                        int heightofsection1_1 = col1_1.getMeasuredHeight();

                        // We know the size of each section, so we just need to know which one to display
                        float maxwidth_scale1_1 = ((float) StaticVariables.availableWidth_1col) / (float) widthofsection1_1;
                        float maxheight_scale1_1 = ((float) StaticVariables.availableScreenHeight) / (float) heightofsection1_1;

                        if (maxheight_scale1_1 < maxwidth_scale1_1) {
                            maxwidth_scale1_1 = maxheight_scale1_1;
                        }

                        // Now we know how many columns we should use, let's do it!
                        float maxscale = preferences.getMyPreferenceFloat(c, "fontSizePresoMax", 40.0f) / 12.0f;

                        if (maxwidth_scale1_1 > maxscale) {
                            maxwidth_scale1_1 = maxscale;
                        }
                        projectedPresenterView1Col(c, preferences, storageAccess, processSong, maxwidth_scale1_1,
                                projected_LinearLayout, projected_ImageView);

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    private void projectedPresenterView1Col(Context c, Preferences preferences,StorageAccess storageAccess,ProcessSong processSong,
                                            float scale1_1, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {
            LinearLayout lyrics1_1 = processSong.createLinearLayout(c);
            LinearLayout box1_1 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.lyricsTextColor,
                    StaticVariables.lyricsBackgroundColor, defaultPadding);
            float fontsize1_1 = processSong.getProjectedFontSize(scale1_1);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_1.setPadding(0, 0, 0, 0);
            lyrics1_1.setPadding(0, 0, 0, 0);

            // Add this section
            lyrics1_1 = processSong.projectedSectionView(c, StaticVariables.currentSection,
                    fontsize1_1,
                    storageAccess, preferences,StaticVariables.lyricsTextColor, StaticVariables.lyricsChordsColor,
                    StaticVariables.lyricsCapoColor,StaticVariables.presoFontColor,StaticVariables.presoShadowColor);
            LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(StaticVariables.availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp1_1.setMargins(0, 0, 0, 0);
            lyrics1_1.setLayoutParams(llp1_1);
            box1_1.addView(lyrics1_1);

            // Now add the display
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(StaticVariables.availableScreenWidth,
                    StaticVariables.availableScreenHeight + defaultPadding);
            llp.setMargins(0, 0, 0, 0);
            projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            box1_1.setLayoutParams(llp);
            projected_LinearLayout.addView(box1_1);
            animateIn(c, preferences, projected_ImageView, projected_LinearLayout);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void tryMeasure(View view) {
        try {
            if (view.getLayoutParams()==null) {
                view.setLayoutParams(new ViewGroup.LayoutParams(0,0));
            }
            view.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     */
}
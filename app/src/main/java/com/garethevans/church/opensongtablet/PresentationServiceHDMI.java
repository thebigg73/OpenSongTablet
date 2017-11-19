package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
class PresentationServiceHDMI extends Presentation
        implements TextureView.SurfaceTextureListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    PresentationServiceHDMI(Context c, Display display) {
        super(c, display);
        context = c;
        myscreen = display;
    }

    private static Display myscreen;
    @SuppressLint("StaticFieldLeak")
    private static RelativeLayout pageHolder;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout projected_LinearLayout;
    @SuppressLint("StaticFieldLeak")
    private static RelativeLayout projectedPage_RelativeLayout;
    @SuppressLint("StaticFieldLeak")
    private static ImageView projected_ImageView;
    @SuppressLint("StaticFieldLeak")
    private static ImageView projected_Logo;
    @SuppressLint("StaticFieldLeak")
    private static TextView songinfo_TextView;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout bottom_infobar;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout col1_1;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout col1_2;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout col2_2;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout col1_3;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout col2_3;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout col3_3;
    @SuppressLint("StaticFieldLeak")
    private static TextureView projected_TextureView;
    @SuppressLint("StaticFieldLeak")
    private static ImageView projected_BackgroundImage;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout presentermode_bottombit;
    @SuppressLint("StaticFieldLeak")
    private static TextView presentermode_title;
    @SuppressLint("StaticFieldLeak")
    private static TextView presentermode_author;
    @SuppressLint("StaticFieldLeak")
    private static TextView presentermode_copyright;
    @SuppressLint("StaticFieldLeak")
    private static TextView presentermode_alert;

    @SuppressLint("StaticFieldLeak")
    static Context context;
    private static int availableScreenWidth;
    private static int availableScreenHeight;
    private static int padding;
    private static int availableWidth_1col;
    private static int availableWidth_2col;
    private static int availableWidth_3col;
    private static int[] projectedviewwidth;
    private static int[] projectedviewheight;
    @SuppressWarnings("unused")
    private static float[] projectedSectionScaleValue;

    private static AsyncTask<Object, Void, String> preparefullprojected_async;
    private static AsyncTask<Object, Void, String> preparestageprojected_async;
    private static AsyncTask<Object, Void, String> preparepresenterprojected_async;
    private static AsyncTask<Object, Void, String> projectedstageview1col_async;
    private static AsyncTask<Object, Void, String> projectedpresenterview1col_async;
    private static AsyncTask<Object, Void, String> projectedPerformanceView1Col_async;
    private static AsyncTask<Object, Void, String> projectedPerformanceView2Col_async;
    private static AsyncTask<Object, Void, String> projectedPerformanceView3Col_async;

    //MediaController
    private static MediaPlayer mMediaPlayer;

    // Images and video backgrounds
    private static File img1File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage1);
    private static File img2File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage2);
    private static String vid1File = FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo1;
    private static String vid2File = FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo2;
    private static String vidFile;
    private static File imgFile;
    private static Drawable defimage;
    @SuppressWarnings("unused")
    private static Bitmap myBitmap;
    @SuppressWarnings("unused")
    private static Drawable dr;
    static Surface s;
    private static Animation mypage_fadein;
    private static Animation mypage_fadeout;
    private static Animation background_fadein;
    @SuppressWarnings("unused")
    private static Animation background_fadeout;
    private static Animation image_fadein;
    private static Animation image_fadeout;
    @SuppressWarnings("unused")
    private static Animation video_fadein;
    @SuppressWarnings("unused")
    private static Animation video_fadeout;
    private static Animation logo_fadein;
    private static Animation logo_fadeout;
    private static Animation lyrics_fadein;
    private static Animation lyrics_fadeout;
    private static Animation songinfo_fadein;
    private static Animation songinfo_fadeout;
    private static Animation songtitle_fadein;
    private static Animation songtitle_fadeout;
    private static Animation songauthor_fadein;
    private static Animation songauthor_fadeout;
    private static Animation songcopyright_fadein;
    private static Animation songcopyright_fadeout;
    private static Animation songalert_fadein;
    private static Animation songalert_fadeout;


    @SuppressLint("StaticFieldLeak")
    static Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cast_screen);

        pageHolder = (RelativeLayout) findViewById(R.id.pageHolder);
        projectedPage_RelativeLayout = (RelativeLayout) findViewById(R.id.projectedPage_RelativeLayout);
        projected_LinearLayout = (LinearLayout) findViewById(R.id.projected_LinearLayout);
        projected_ImageView = (ImageView) findViewById(R.id.projected_ImageView);
        projected_BackgroundImage = (ImageView) findViewById(R.id.projected_BackgroundImage);
        projected_TextureView = (TextureView) findViewById(R.id.projected_TextureView);
        projected_Logo = (ImageView) findViewById(R.id.projected_Logo);
        songinfo_TextView = (TextView) findViewById(R.id.songinfo_TextView);
        presentermode_bottombit = (LinearLayout) findViewById(R.id.presentermode_bottombit);
        presentermode_title = (TextView) findViewById(R.id.presentermode_title);
        presentermode_author = (TextView) findViewById(R.id.presentermode_author);
        presentermode_copyright = (TextView) findViewById(R.id.presentermode_copyright);
        presentermode_alert = (TextView) findViewById(R.id.presentermode_alert);
        bottom_infobar = (LinearLayout) findViewById(R.id.bottom_infobar);
        col1_1 = (LinearLayout) findViewById(R.id.col1_1);
        col1_2 = (LinearLayout) findViewById(R.id.col1_2);
        col2_2 = (LinearLayout) findViewById(R.id.col2_2);
        col1_3 = (LinearLayout) findViewById(R.id.col1_3);
        col2_3 = (LinearLayout) findViewById(R.id.col2_3);
        col3_3 = (LinearLayout) findViewById(R.id.col3_3);

        c = projectedPage_RelativeLayout.getContext();

        // Set up the custom background animations (to base on final alpha)
        prepareBackgroundAnimations();

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

        // Prepare the display after 2 secs (a chance for stuff to be measured and show the logo
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!FullscreenActivity.whichMode.equals("Presentation")) {
                    normalStartUp();
                } else {
                    // Switch to the user background and logo
                    presenterStartUp();
                }
            }
        }, 2000);
    }

    // Setup some default stuff
    static void matchPresentationToMode() {
        switch (FullscreenActivity.whichMode) {
            case "Stage":
            case "Performance":
            default:
                songinfo_TextView.setAlpha(0.0f);
                songinfo_TextView.setVisibility(View.VISIBLE);
                presentermode_bottombit.setVisibility(View.GONE);
                projected_TextureView.setVisibility(View.GONE);
                projected_BackgroundImage.setImageDrawable(null);
                projected_BackgroundImage.setVisibility(View.GONE);
                break;

            case "Presentation":
                songinfo_TextView.setVisibility(View.GONE);
                presentermode_bottombit.setVisibility(View.VISIBLE);
                fixBackground();
                break;
        }
        FullscreenActivity.forcecastupdate = false;
    }

    private void prepareBackgroundAnimations() {
        mypage_fadein = CustomAnimations.setUpAnimation(pageHolder, 0.0f, 1.0f);
        mypage_fadeout = CustomAnimations.setUpAnimation(pageHolder, 1.0f, 0.0f);
        background_fadein = CustomAnimations.setUpAnimation(projected_BackgroundImage, 0.0f, 1.0f);
        background_fadeout = CustomAnimations.setUpAnimation(projected_BackgroundImage, 1.0f, 0.0f);
        logo_fadein = CustomAnimations.setUpAnimation(projected_Logo, 0.0f, 1.0f);
        logo_fadeout = CustomAnimations.setUpAnimation(projected_Logo, 1.0f, 0.0f);
        image_fadein = CustomAnimations.setUpAnimation(projected_ImageView, 0.0f, 1.0f);
        image_fadeout = CustomAnimations.setUpAnimation(projected_ImageView, 1.0f, 0.0f);
        video_fadein = CustomAnimations.setUpAnimation(projected_TextureView, 0.0f, 1.0f);
        video_fadeout = CustomAnimations.setUpAnimation(projected_TextureView, 1.0f, 0.0f);
        lyrics_fadein = CustomAnimations.setUpAnimation(projected_LinearLayout, 0.0f, 1.0f);
        lyrics_fadeout = CustomAnimations.setUpAnimation(projected_LinearLayout, 1.0f, 0.0f);
        songinfo_fadein = CustomAnimations.setUpAnimation(songinfo_TextView, 0.0f, 1.0f);
        songinfo_fadeout = CustomAnimations.setUpAnimation(songinfo_TextView, 1.0f, 0.0f);
        songtitle_fadein = CustomAnimations.setUpAnimation(presentermode_title, 0.0f, 1.0f);
        songtitle_fadeout = CustomAnimations.setUpAnimation(presentermode_title, 1.0f, 0.0f);
        songauthor_fadein = CustomAnimations.setUpAnimation(presentermode_author, 0.0f, 1.0f);
        songauthor_fadeout = CustomAnimations.setUpAnimation(presentermode_author, 1.0f, 0.0f);
        songcopyright_fadein = CustomAnimations.setUpAnimation(presentermode_copyright, 0.0f, 1.0f);
        songcopyright_fadeout = CustomAnimations.setUpAnimation(presentermode_copyright, 1.0f, 0.0f);
        songalert_fadein = CustomAnimations.setUpAnimation(presentermode_alert, 0.0f, 1.0f);
        songalert_fadeout = CustomAnimations.setUpAnimation(presentermode_alert, 1.0f, 0.0f);
    }

    @SuppressWarnings("deprecation")
    private void setDefaultBackgroundImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            defimage = getResources().getDrawable(R.drawable.preso_default_bg, null);
        } else {
            defimage = getResources().getDrawable(R.drawable.preso_default_bg);
        }
    }

    // Get and setup screen sizes
    static void changeMargins() {
        songinfo_TextView.setTextColor(FullscreenActivity.lyricsTextColor);
        projectedPage_RelativeLayout.setPadding(FullscreenActivity.xmargin_presentation,
                FullscreenActivity.ymargin_presentation, FullscreenActivity.xmargin_presentation,
                FullscreenActivity.ymargin_presentation);
    }

    @SuppressLint("NewApi")
    private static void getScreenSizes() {
        DisplayMetrics metrics = new DisplayMetrics();
        myscreen.getMetrics(metrics);
        Drawable icon = bottom_infobar.getContext().getDrawable(R.mipmap.ic_round_launcher);
        int bottombarheight = 0;
        if (icon != null) {
            bottombarheight = icon.getIntrinsicHeight();
        }

        int density = metrics.densityDpi;

        padding = 8;

        int screenWidth = metrics.widthPixels;
        int leftpadding = projectedPage_RelativeLayout.getPaddingLeft();
        int rightpadding = projectedPage_RelativeLayout.getPaddingRight();
        availableScreenWidth = screenWidth - leftpadding - rightpadding;

        int screenHeight = metrics.heightPixels;
        int toppadding = projectedPage_RelativeLayout.getPaddingTop();
        int bottompadding = projectedPage_RelativeLayout.getPaddingBottom();
        availableScreenHeight = screenHeight - toppadding - bottompadding - bottombarheight - (padding * 4);
        availableWidth_1col = availableScreenWidth - (padding * 2);
        availableWidth_2col = (int) ((float) availableScreenWidth / 2.0f) - (padding * 3);
        availableWidth_3col = (int) ((float) availableScreenWidth / 3.0f) - (padding * 4);
    }

    // The logo stuff
    @SuppressWarnings("deprecation")
    static void setUpLogo() {
        // If the customLogo doesn't exist, use the default one
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int imgwidth = 1024;
        int imgheight = 500;
        float xscale;
        float yscale;
        boolean usingcustom = false;
        File customLogo = new File(FullscreenActivity.customLogo);
        if (customLogo.exists()) {
            // Get the sizes of the custom logo
            BitmapFactory.decodeFile(FullscreenActivity.customLogo, options);
            imgwidth = options.outWidth;
            imgheight = options.outHeight;
            if (imgwidth > 0 && imgheight > 0) {
                usingcustom = true;
            }
        }

        xscale = ((float) availableWidth_1col * FullscreenActivity.customLogoSize) / (float) imgwidth;
        yscale = ((float) availableScreenHeight * FullscreenActivity.customLogoSize) / (float) imgheight;

        if (xscale > yscale) {
            xscale = yscale;
        }

        int logowidth = (int) ((float) imgwidth * xscale);
        int logoheight = (int) ((float) imgheight * xscale);

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(logowidth, logoheight);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        projected_Logo.setLayoutParams(rlp);

        if (usingcustom) {
            Uri logoUri = Uri.fromFile(customLogo);
            RequestOptions myOptions = new RequestOptions()
                    .override(logowidth,logoheight);
            Glide.with(c).load(logoUri).apply(myOptions).into(projected_Logo);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                projected_Logo.setImageDrawable(c.getResources().getDrawable(R.drawable.ost_logo, c.getTheme()));
            } else {
                projected_Logo.setImageDrawable(c.getResources().getDrawable(R.drawable.ost_logo));
            }
        }
        if (PresenterMode.logoButton_isSelected) {
            projected_Logo.startAnimation(logo_fadein);
        }
    }

    static void showLogo() {
        // Animate out the lyrics if they were visible and animate in the logo
        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
            projected_ImageView.startAnimation(image_fadeout);
        } else {
            projected_LinearLayout.startAnimation(lyrics_fadeout);
        }

        // If we had a black screen, fade that in
        if (pageHolder.getVisibility() == View.INVISIBLE) {
            pageHolder.startAnimation(mypage_fadein);
        }

        presentermode_title.startAnimation(songtitle_fadeout);
        presentermode_author.startAnimation(songauthor_fadeout);
        presentermode_copyright.startAnimation(songcopyright_fadeout);
        projected_Logo.startAnimation(logo_fadein);
    }

    static void hideLogo() {
        // Animate out the logo and animate in the lyrics if they were visible
        // Animate out the lyrics if they were visible and animate in the logo
        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
            projected_ImageView.startAnimation(image_fadein);
        } else {
            projected_LinearLayout.startAnimation(lyrics_fadein);
        }
        presentermode_title.startAnimation(songtitle_fadein);
        presentermode_author.startAnimation(songauthor_fadein);
        presentermode_copyright.startAnimation(songcopyright_fadein);
        projected_Logo.startAnimation(logo_fadeout);
    }

    static void blankDisplay() {
        pageHolder.startAnimation(mypage_fadeout);
    }

    static void unblankDisplay() {
        pageHolder.startAnimation(mypage_fadein);
    }

    // Set up the screen changes
    private void presenterStartUp() {
        // Set up the text styles and fonts for the bottom info bar
        presenterThemeSetUp();

        // After the fadeout time, set the background and fade in
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Try to set the new background
                fixBackground();

                if (FullscreenActivity.backgroundTypeToUse.equals("image")) {
                    projected_BackgroundImage.startAnimation(background_fadein);
                } else if (FullscreenActivity.backgroundTypeToUse.equals("video")) {
                    projected_TextureView.startAnimation(background_fadein);
                }
            }
        }, FullscreenActivity.presoTransitionTime);
    }

    private void normalStartUp() {
        // Animate out the default logo
        projected_Logo.startAnimation(logo_fadeout);
        doUpdate();
    }

    private static void presenterThemeSetUp() {
        // Set the text at the bottom of the page to match the presentation text colour
        presentermode_title.setTypeface(FullscreenActivity.presoInfoFont);
        presentermode_author.setTypeface(FullscreenActivity.presoInfoFont);
        presentermode_copyright.setTypeface(FullscreenActivity.presoInfoFont);
        presentermode_alert.setTypeface(FullscreenActivity.presoInfoFont);
        presentermode_title.setTextColor(FullscreenActivity.presoInfoFontColor);
        presentermode_author.setTextColor(FullscreenActivity.presoInfoFontColor);
        presentermode_copyright.setTextColor(FullscreenActivity.presoInfoFontColor);
        presentermode_alert.setTextColor(FullscreenActivity.presoAlertFontColor);
        presentermode_title.setTextSize(FullscreenActivity.presoTitleSize);
        presentermode_author.setTextSize(FullscreenActivity.presoAuthorSize);
        presentermode_copyright.setTextSize(FullscreenActivity.presoCopyrightSize);
        presentermode_alert.setTextSize(FullscreenActivity.presoAlertSize);
        presentermode_title.setShadowLayer(FullscreenActivity.presoTitleSize / 2.0f, 4, 4, FullscreenActivity.presoShadowColor);
        presentermode_author.setShadowLayer(FullscreenActivity.presoAuthorSize / 2.0f, 4, 4, FullscreenActivity.presoShadowColor);
        presentermode_copyright.setShadowLayer(FullscreenActivity.presoCopyrightSize / 2.0f, 4, 4, FullscreenActivity.presoShadowColor);
        presentermode_alert.setShadowLayer(FullscreenActivity.presoAlertSize / 2.0f, 4, 4, FullscreenActivity.presoShadowColor);
        presentermode_title.setGravity(FullscreenActivity.presoInfoAlign);
        presentermode_author.setGravity(FullscreenActivity.presoInfoAlign);
        presentermode_copyright.setGravity(FullscreenActivity.presoInfoAlign);
        presentermode_alert.setGravity(FullscreenActivity.presoInfoAlign);
        if (PresenterMode.alert_on.equals("Y")) {
            presentermode_alert.setVisibility(View.VISIBLE);
        } else {
            presentermode_alert.setVisibility(View.GONE);
        }
    }

    private static void panicShowViews() {
        // After 3x the transition times, make sure the correct view is visible regardless of animations
        if (FullscreenActivity.whichMode.equals("Presentation")) {
            if (FullscreenActivity.isImage || FullscreenActivity.isPDF || FullscreenActivity.isImageSlide) {
                projected_ImageView.setVisibility(View.VISIBLE);
                projected_LinearLayout.setVisibility(View.GONE);
                projected_ImageView.setAlpha(1.0f);
            } else if (FullscreenActivity.isVideo) {
                projected_TextureView.setVisibility(View.VISIBLE);
                projected_LinearLayout.setVisibility(View.GONE);
                projected_ImageView.setVisibility(View.GONE);
                projected_TextureView.setAlpha(1.0f);
            } else {
                projected_LinearLayout.setVisibility(View.VISIBLE);
                projected_ImageView.setVisibility(View.GONE);
                projected_LinearLayout.setAlpha(1.0f);
            }
        }
    }

    static void doUpdate() {
        // First up, animate everything away
        animateOut();

        // If we have forced an update due to switching modes, set that up
        if (FullscreenActivity.forcecastupdate) {
            matchPresentationToMode();
        }

        // If we had a black screen, fade that in
        if (pageHolder.getVisibility() == View.INVISIBLE) {
            pageHolder.startAnimation(mypage_fadein);
        }

        // Just in case there is a glitch, make the stuff visible after 3x transition time
        Handler panic = new Handler();
        panic.postDelayed(new Runnable() {
            @Override
            public void run() {
                panicShowViews();
            }
        }, 3 * FullscreenActivity.presoTransitionTime);

        // Set the title of the song and author (if available).  Only does this for changes
        if (FullscreenActivity.whichMode.equals("Presentation")) {
            presenterWriteSongInfo();
        } else {
            setSongTitle();
        }

        // Now run the next bit post delayed (to wait for the animate out)
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Wipe any current views
                wipeAllViews();

                // Check the colours colour
                if (!FullscreenActivity.whichMode.equals("Presentation")) {
                    // Set the page background to the correct colour for Peformance/Stage modes
                    projectedPage_RelativeLayout.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);
                    songinfo_TextView.setTextColor(FullscreenActivity.lyricsTextColor);
                }

                // Decide on what we are going to show
                if (FullscreenActivity.isPDF) {
                    doPDFPage();
                } else if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide) {
                    doImagePage();
                } else {
                    switch (FullscreenActivity.whichMode) {
                        case "Stage":
                            prepareStageProjected();
                            break;
                        case "Performance":
                            prepareFullProjected();
                            break;
                        default:
                            preparePresenterProjected();
                            break;
                    }
                }
            }
        }, FullscreenActivity.presoTransitionTime);
    }

    // Change background images/videos
    static void fixBackground() {
        // Decide if user is using video or image for background
        switch (FullscreenActivity.backgroundTypeToUse) {
            case "image":
                projected_BackgroundImage.setVisibility(View.VISIBLE);
                projected_TextureView.setVisibility(View.INVISIBLE);
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
                if (FullscreenActivity.backgroundToUse.equals("img1")) {
                    imgFile = img1File;
                } else {
                    imgFile = img2File;
                }

                if (imgFile.exists()) {
                    if (imgFile.toString().contains("ost_bg.png")) {
                        projected_BackgroundImage.setImageDrawable(defimage);
                    } else {
                        // Process the image location into an URI
                        Uri imageUri = Uri.fromFile(imgFile);
                        RequestOptions myOptions = new RequestOptions()
                                .centerCrop();
                        Glide.with(c).load(imageUri).apply(myOptions).into(projected_BackgroundImage);
                    }
                    projected_BackgroundImage.setVisibility(View.VISIBLE);
                }
                break;
            case "video":
                projected_BackgroundImage.setVisibility(View.INVISIBLE);
                projected_TextureView.setVisibility(View.VISIBLE);

                if (FullscreenActivity.backgroundToUse.equals("vid1")) {
                    vidFile = vid1File;
                } else {
                    vidFile = vid2File;
                }
                if (mMediaPlayer != null) {
                    mMediaPlayer.start();
                }
                myBitmap = null;
                dr = null;
                projected_BackgroundImage.setImageDrawable(null);
                projected_BackgroundImage.setVisibility(View.GONE);
                break;
            default:
                myBitmap = null;
                dr = null;
                projected_BackgroundImage.setImageDrawable(null);
                projected_BackgroundImage.setVisibility(View.GONE);
                break;
        }
        updateAlpha();
    }


    // Change the song info at the bottom of the page
    private static void setSongTitle() {
        String old_title = songinfo_TextView.getText().toString();
        String new_title = FullscreenActivity.mTitle.toString();
        if (!FullscreenActivity.mAuthor.equals("")) {
            new_title = new_title + "\n" + FullscreenActivity.mAuthor;
        }
        if (!old_title.equals(new_title)) {
            // It has changed, so make the text update on the screen
            normalChangeSongInfo(new_title);
        }
    }

    private static void presenterWriteSongInfo() {
        String old_title = presentermode_title.getText().toString();
        String old_author = presentermode_author.getText().toString();
        String old_copyright = presentermode_copyright.getText().toString();
        if (!old_title.equals(FullscreenActivity.mTitle)) {
            presenterFadeOutSongInfo(presentermode_title, songtitle_fadeout, songtitle_fadein, FullscreenActivity.mTitle.toString());
        }
        if (!old_author.equals(FullscreenActivity.mAuthor)) {
            presenterFadeOutSongInfo(presentermode_author, songauthor_fadeout, songauthor_fadein, FullscreenActivity.mAuthor.toString());
        }
        if (!old_copyright.equals(FullscreenActivity.mCopyright)) {
            presenterFadeOutSongInfo(presentermode_copyright, songcopyright_fadeout, songcopyright_fadein, FullscreenActivity.mCopyright.toString());
        }
    }

    private static void normalChangeSongInfo(final String s) {
        songinfo_TextView.startAnimation(songinfo_fadeout);
        // After the transition delay, write the new value and fade it back in
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                songinfo_TextView.setTextColor(FullscreenActivity.lyricsTextColor);
                songinfo_TextView.setText(s);
                songinfo_TextView.startAnimation(songinfo_fadein);
            }
        }, FullscreenActivity.presoTransitionTime);
    }

    private static void presenterFadeOutSongInfo(final TextView tv, Animation out, final Animation in, final String s) {
        if (tv.getAlpha() > 0.0f) {
            tv.startAnimation(out);
        } else {
            tv.setAlpha(0.0f);
        }
        // After the transition time, change the text and fade it back in
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv.setText(s);
                // If this is a pdf or image, hide the song info
                if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
                    presentermode_title.setVisibility(View.GONE);
                    presentermode_author.setVisibility(View.GONE);
                    presentermode_copyright.setVisibility(View.GONE);
                } else {
                    presentermode_title.setVisibility(View.VISIBLE);
                    presentermode_author.setVisibility(View.VISIBLE);
                    presentermode_copyright.setVisibility(View.VISIBLE);
                    presenterFadeInSongInfo(tv, in);
                }
            }
        }, FullscreenActivity.presoTransitionTime);
    }

    private static void presenterFadeInSongInfo(TextView tv, Animation in) {
        tv.startAnimation(in);
    }


    private static void doPDFPage() {
        Bitmap bmp = ProcessSong.createPDFPage(c, availableScreenWidth, availableScreenHeight, "Y");
        projected_ImageView.setVisibility(View.GONE);
        projected_ImageView.setBackgroundColor(0xffffffff);
        projected_ImageView.setImageBitmap(bmp);
        projected_ImageView.setVisibility(View.VISIBLE);
        animateIn();
    }

    private static void doImagePage() {
        projected_ImageView.setVisibility(View.GONE);
        projected_ImageView.setBackgroundColor(0x00000000);
        // Process the image location into an URI
        Uri imageUri = Uri.fromFile(FullscreenActivity.file);
        RequestOptions myOptions = new RequestOptions()
                .fitCenter();
        Glide.with(c).load(imageUri).apply(myOptions).into(projected_ImageView);
        projected_ImageView.setVisibility(View.VISIBLE);
        animateIn();
    }


    // Async stuff to prepare and write the page
    private static void cancelAsyncTask(AsyncTask ast) {
        if (ast != null) {
            try {
                ast.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void prepareStageProjected() {
        cancelAsyncTask(preparestageprojected_async);
        preparestageprojected_async = new PrepareStageProjected();
        try {
            FullscreenActivity.scalingfiguredout = false;
            preparestageprojected_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class PrepareStageProjected extends AsyncTask<Object, Void, String> {
        @SuppressLint("StaticFieldLeak")
        LinearLayout test1_1 = ProcessSong.createLinearLayout(context);

        @Override
        protected void onPreExecute() {
            // Remove all views from the test pane
            try {
                col1_1.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                projectedSectionScaleValue = new float[1];
                projectedviewwidth = new int[1];
                projectedviewheight = new int[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    test1_1 = ProcessSong.projectedSectionView(context, FullscreenActivity.currentSection, 12.0f);
                    col1_1.addView(test1_1);

                    // Now premeasure the view
                    test1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                    projectedviewwidth[0] = test1_1.getMeasuredWidth();
                    projectedviewheight[0] = test1_1.getMeasuredHeight();

                    // Now display the song!
                    //FullscreenActivity.scalingfiguredout = true;
                    projectedStageView1Col();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void projectedStageView1Col() {
        cancelAsyncTask(projectedstageview1col_async);
        projectedstageview1col_async = new ProjectedStageView1Col();
        try {
            projectedstageview1col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProjectedStageView1Col extends AsyncTask<Object, Void, String> {
        LinearLayout lyrics1_1 = ProcessSong.createLinearLayout(context);
        LinearLayout box1_1 = ProcessSong.prepareProjectedBoxView(context, 0, padding);
        float scale;

        @Override
        protected void onPreExecute() {
            try {
                projected_LinearLayout.removeAllViews();
                float max_width_scale = (float) availableWidth_1col / (float) projectedviewwidth[0];
                float max_height_scale = (float) availableScreenHeight / (float) projectedviewheight[0];
                if (max_height_scale > max_width_scale) {
                    scale = max_width_scale;
                } else {
                    scale = max_height_scale;
                }

                float maxscale = FullscreenActivity.presoMaxFontSize / 12.0f;
                if (scale > maxscale) {
                    scale = maxscale;
                }

                projected_LinearLayout.removeAllViews();
                lyrics1_1.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    lyrics1_1 = ProcessSong.projectedSectionView(context, FullscreenActivity.currentSection, ProcessSong.getProjectedFontSize(scale));
                    LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_1.setMargins(0, 0, 0, 0);
                    lyrics1_1.setLayoutParams(llp1_1);
                    //lyrics1_1.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[FullscreenActivity.currentSection]));
                    box1_1.addView(lyrics1_1);

                    // Now add the display
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(availableScreenWidth, availableScreenHeight + padding);
                    llp.setMargins(0, 0, 0, 0);
                    projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    box1_1.setLayoutParams(llp);
                    projected_LinearLayout.addView(box1_1);
                    animateIn();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void preparePresenterProjected() {
        cancelAsyncTask(preparepresenterprojected_async);
        preparepresenterprojected_async = new PreparePresenterProjected();
        try {
            preparepresenterprojected_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class PreparePresenterProjected extends AsyncTask<Object, Void, String> {
        LinearLayout test1_1 = ProcessSong.createLinearLayout(context);

        @Override
        protected void onPreExecute() {
            // Remove all views from the test pane
            try {
                col1_1.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                projectedSectionScaleValue = new float[1];
                projectedviewwidth = new int[1];
                projectedviewheight = new int[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    test1_1 = ProcessSong.projectedSectionView(context, FullscreenActivity.currentSection, 12.0f);
                    col1_1.addView(test1_1);

                    // Now premeasure the view
                    test1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                    projectedviewwidth[0] = test1_1.getMeasuredWidth();
                    projectedviewheight[0] = test1_1.getMeasuredHeight();

                    // Now display the song!
                    //FullscreenActivity.scalingfiguredout = true;
                    projectedPresenterView1Col();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void projectedPresenterView1Col() {
        cancelAsyncTask(projectedpresenterview1col_async);
        projectedpresenterview1col_async = new ProjectedPresenterView1Col();
        try {
            projectedpresenterview1col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProjectedPresenterView1Col extends AsyncTask<Object, Void, String> {
        LinearLayout lyrics1_1 = ProcessSong.createLinearLayout(context);
        LinearLayout box1_1 = ProcessSong.prepareProjectedBoxView(context, 0, padding);
        float scale;

        @Override
        protected void onPreExecute() {
            try {
                projected_LinearLayout.removeAllViews();
                float max_width_scale = (float) availableWidth_1col / (float) projectedviewwidth[0];
                float max_height_scale = (float) availableScreenHeight / (float) projectedviewheight[0];
                if (max_height_scale > max_width_scale) {
                    scale = max_width_scale;
                } else {
                    scale = max_height_scale;
                }

                float maxscale = FullscreenActivity.presoMaxFontSize / 12.0f;
                if (scale > maxscale) {
                    scale = maxscale;
                }

                projected_LinearLayout.removeAllViews();
                lyrics1_1.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    lyrics1_1 = ProcessSong.projectedSectionView(context, FullscreenActivity.currentSection, ProcessSong.getProjectedFontSize(scale));
                    LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_1.setMargins(0, 0, 0, 0);
                    lyrics1_1.setLayoutParams(llp1_1);
                    box1_1.addView(lyrics1_1);

                    // Now add the display
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(availableScreenWidth, availableScreenHeight + padding);
                    llp.setMargins(0, 0, 0, 0);
                    projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    box1_1.setLayoutParams(llp);
                    projected_LinearLayout.addView(box1_1);
                    animateIn();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void displayFullSong() {
        projected_LinearLayout.removeAllViews();

        // We know the widths and heights of all of the view (1,2 and 3 columns).
        // Decide which is best by looking at the scaling

        int colstouse = 1;
        // We know the size of each section, so we just need to know which one to display
        int widthofsection1_1 = projectedviewwidth[0];
        int widthofsection1_2 = projectedviewwidth[1];
        int widthofsection2_2 = projectedviewwidth[2];
        int widthofsection1_3 = projectedviewwidth[3];
        int widthofsection2_3 = projectedviewwidth[4];
        int widthofsection3_3 = projectedviewwidth[5];
        int heightofsection1_1 = projectedviewheight[0];
        int heightofsection1_2 = projectedviewheight[1];
        int heightofsection2_2 = projectedviewheight[2];
        int heightofsection1_3 = projectedviewheight[3];
        int heightofsection2_3 = projectedviewheight[4];
        int heightofsection3_3 = projectedviewheight[5];

        float maxwidth_scale1_1 = ((float) availableWidth_1col) / (float) widthofsection1_1;
        float maxwidth_scale1_2 = ((float) availableWidth_2col) / (float) widthofsection1_2;
        float maxwidth_scale2_2 = ((float) availableWidth_2col) / (float) widthofsection2_2;
        float maxwidth_scale1_3 = ((float) availableWidth_3col) / (float) widthofsection1_3;
        float maxwidth_scale2_3 = ((float) availableWidth_3col) / (float) widthofsection2_3;
        float maxwidth_scale3_3 = ((float) availableWidth_3col) / (float) widthofsection3_3;
        float maxheight_scale1_1 = ((float) availableScreenHeight) / (float) heightofsection1_1;
        float maxheight_scale1_2 = ((float) availableScreenHeight) / (float) heightofsection1_2;
        float maxheight_scale2_2 = ((float) availableScreenHeight) / (float) heightofsection2_2;
        float maxheight_scale1_3 = ((float) availableScreenHeight) / (float) heightofsection1_3;
        float maxheight_scale2_3 = ((float) availableScreenHeight) / (float) heightofsection2_3;
        float maxheight_scale3_3 = ((float) availableScreenHeight) / (float) heightofsection3_3;

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
            if (maxwidth_scale1_2 > maxwidth_scale2_2) {
                myfullscale = maxwidth_scale2_2;
            } else {
                myfullscale = maxwidth_scale1_2;
            }
        }

        if (maxwidth_scale1_3 > myfullscale && maxwidth_scale2_3 > myfullscale && maxwidth_scale3_3 > myfullscale) {
            colstouse = 3;
        }

        // Now we know how many columns we should use, let's do it!
        float maxscale = FullscreenActivity.presoMaxFontSize / 12.0f;

        switch (colstouse) {
            case 1:
                if (maxwidth_scale1_1 > maxscale) {
                    maxwidth_scale1_1 = maxscale;
                }
                projectedPerformanceView1col(maxwidth_scale1_1);
                break;

            case 2:
                if (maxwidth_scale1_2 > maxscale) {
                    maxwidth_scale1_2 = maxscale;
                }
                if (maxwidth_scale2_2 > maxscale) {
                    maxwidth_scale2_2 = maxscale;
                }
                projectedPerformanceView2col(maxwidth_scale1_2, maxwidth_scale2_2);
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
                projectedPerformanceView3col(maxwidth_scale1_3, maxwidth_scale2_3, maxwidth_scale3_3);
                break;
        }
    }

    private static void prepareFullProjected() {
        cancelAsyncTask(preparefullprojected_async);
        preparefullprojected_async = new PrepareFullProjected();
        try {
            FullscreenActivity.scalingfiguredout = false;
            preparefullprojected_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class PrepareFullProjected extends AsyncTask<Object, Void, String> {
        @SuppressLint("StaticFieldLeak")
        LinearLayout test1_1 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout test1_2 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout test2_2 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout test1_3 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout test2_3 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout test3_3 = ProcessSong.createLinearLayout(context);

        @Override
        protected void onPreExecute() {
            // Remove all views from the test panes
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
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                projectedSectionScaleValue = new float[6];
                projectedviewwidth = new int[6];
                projectedviewheight = new int[6];
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // Prepare the new views to add to 1,2 and 3 colums ready for measuring
                    // Go through each section
                    for (int x = 0; x < FullscreenActivity.songSections.length; x++) {

                        test1_1 = ProcessSong.projectedSectionView(context, x, 12.0f);
                        col1_1.addView(test1_1);

                        if (x < FullscreenActivity.halfsplit_section) {
                            test1_2 = ProcessSong.projectedSectionView(context, x, 12.0f);
                            col1_2.addView(test1_2);
                        } else {
                            test2_2 = ProcessSong.projectedSectionView(context, x, 12.0f);
                            col2_2.addView(test2_2);
                        }

                        if (x < FullscreenActivity.thirdsplit_section) {
                            test1_3 = ProcessSong.projectedSectionView(context, x, 12.0f);
                            col1_3.addView(test1_3);
                        } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                            test2_3 = ProcessSong.projectedSectionView(context, x, 12.0f);
                            col2_3.addView(test2_3);
                        } else {
                            test3_3 = ProcessSong.projectedSectionView(context, x, 12.0f);
                            col3_3.addView(test3_3);
                        }
                    }

                    // Now premeasure the views
                    col1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    col1_2.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    col2_2.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    col1_3.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    col2_3.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    col3_3.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                    projectedviewwidth[0] = col1_1.getMeasuredWidth();
                    projectedviewheight[0] = col1_1.getMeasuredHeight();
                    projectedviewwidth[1] = col1_2.getMeasuredWidth();
                    projectedviewheight[1] = col1_2.getMeasuredHeight();
                    projectedviewwidth[2] = col2_2.getMeasuredWidth();
                    projectedviewheight[2] = col2_2.getMeasuredHeight();
                    projectedviewwidth[3] = col1_3.getMeasuredWidth();
                    projectedviewheight[3] = col1_3.getMeasuredHeight();
                    projectedviewwidth[4] = col2_3.getMeasuredWidth();
                    projectedviewheight[4] = col2_3.getMeasuredHeight();
                    projectedviewwidth[5] = col3_3.getMeasuredWidth();
                    projectedviewheight[5] = col3_3.getMeasuredHeight();

                    // Now display the song!
                    //FullscreenActivity.scalingfiguredout = true;
                    displayFullSong();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void projectedPerformanceView1col(float scale1_1) {
        cancelAsyncTask(projectedPerformanceView1Col_async);
        projectedPerformanceView1Col_async = new ProjectedPerformanceView1Col(scale1_1);
        try {
            projectedPerformanceView1Col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProjectedPerformanceView1Col extends AsyncTask<Object, Void, String> {
        LinearLayout lyrics1_1 = ProcessSong.createLinearLayout(context);
        LinearLayout box1_1 = ProcessSong.prepareProjectedBoxView(context, 0, padding);
        float scale1_1;
        float fontsize1_1;

        ProjectedPerformanceView1Col(float s1_1) {
            scale1_1 = s1_1;
            fontsize1_1 = ProcessSong.getProjectedFontSize(scale1_1);
        }

        @Override
        protected void onPreExecute() {
            // Remove all views from the projector
            try {
                projected_LinearLayout.removeAllViews();
                lyrics1_1.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // Prepare the new views to add to 1,2 and 3 colums ready for measuring
                    // Go through each section
                    for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                        lyrics1_1 = ProcessSong.projectedSectionView(context, x, fontsize1_1);
                        LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp1_1.setMargins(0, 0, 0, 0);
                        lyrics1_1.setLayoutParams(llp1_1);
                        lyrics1_1.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        box1_1.addView(lyrics1_1);
                    }

                    // Now add the display
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(availableScreenWidth, availableScreenHeight + padding);
                    llp.setMargins(0, 0, 0, 0);
                    projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    box1_1.setLayoutParams(llp);
                    projected_LinearLayout.addView(box1_1);
                    animateIn();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void projectedPerformanceView2col(float scale1_2, float scale2_2) {
        cancelAsyncTask(projectedPerformanceView2Col_async);
        projectedPerformanceView2Col_async = new ProjectedPerformanceView2Col(scale1_2, scale2_2);
        try {
            projectedPerformanceView2Col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProjectedPerformanceView2Col extends AsyncTask<Object, Void, String> {
        float scale1_2;
        float scale2_2;
        float fontsize1_2;
        float fontsize2_2;
        LinearLayout lyrics1_2 = ProcessSong.createLinearLayout(context);
        LinearLayout lyrics2_2 = ProcessSong.createLinearLayout(context);
        LinearLayout box1_2 = ProcessSong.prepareProjectedBoxView(context, 0, padding);
        LinearLayout box2_2 = ProcessSong.prepareProjectedBoxView(context, 0, padding);

        ProjectedPerformanceView2Col(float s1_2, float s2_2) {
            scale1_2 = s1_2;
            scale2_2 = s2_2;
            fontsize1_2 = ProcessSong.getProjectedFontSize(scale1_2);
            fontsize2_2 = ProcessSong.getProjectedFontSize(scale2_2);
        }

        @Override
        protected void onPreExecute() {
            try {
                // Remove all views from the projector
                projected_LinearLayout.removeAllViews();
                lyrics1_2.setPadding(0, 0, 0, 0);
                lyrics2_2.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // Add the song sections...
                    for (int x = 0; x < FullscreenActivity.songSections.length; x++) {

                        if (x < FullscreenActivity.halfsplit_section) {
                            lyrics1_2 = ProcessSong.projectedSectionView(context, x, fontsize1_2);
                            LinearLayout.LayoutParams llp1_2 = new LinearLayout.LayoutParams(availableWidth_2col, LinearLayout.LayoutParams.WRAP_CONTENT);
                            llp1_2.setMargins(0, 0, 0, 0);
                            lyrics1_2.setLayoutParams(llp1_2);
                            lyrics1_2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                            box1_2.addView(lyrics1_2);
                        } else {
                            lyrics2_2 = ProcessSong.projectedSectionView(context, x, fontsize2_2);
                            LinearLayout.LayoutParams llp2_2 = new LinearLayout.LayoutParams(availableWidth_2col, LinearLayout.LayoutParams.WRAP_CONTENT);
                            llp2_2.setMargins(0, 0, 0, 0);
                            lyrics2_2.setLayoutParams(llp2_2);
                            lyrics2_2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                            box2_2.addView(lyrics2_2);
                        }
                    }
                    // Now add the display
                    LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(availableWidth_2col + (padding * 2), availableScreenHeight + padding);
                    LinearLayout.LayoutParams llp2 = new LinearLayout.LayoutParams(availableWidth_2col + (padding * 2), availableScreenHeight + padding);
                    llp1.setMargins(0, 0, padding, 0);
                    llp2.setMargins(0, 0, 0, 0);
                    projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    box1_2.setLayoutParams(llp1);
                    box2_2.setLayoutParams(llp2);
                    projected_LinearLayout.addView(box1_2);
                    projected_LinearLayout.addView(box2_2);
                    animateIn();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void projectedPerformanceView3col(float scale1_3, float scale2_3, float scale3_3) {
        cancelAsyncTask(projectedPerformanceView3Col_async);
        projectedPerformanceView3Col_async = new ProjectedPerformanceView3Col(scale1_3, scale2_3, scale3_3);
        try {
            projectedPerformanceView3Col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProjectedPerformanceView3Col extends AsyncTask<Object, Void, String> {
        float scale1_3;
        float scale2_3;
        float scale3_3;
        float fontsize1_3;
        float fontsize2_3;
        float fontsize3_3;
        LinearLayout lyrics1_3 = ProcessSong.createLinearLayout(context);
        LinearLayout lyrics2_3 = ProcessSong.createLinearLayout(context);
        LinearLayout lyrics3_3 = ProcessSong.createLinearLayout(context);
        LinearLayout box1_3 = ProcessSong.prepareProjectedBoxView(context, 0, padding);
        LinearLayout box2_3 = ProcessSong.prepareProjectedBoxView(context, 0, padding);
        LinearLayout box3_3 = ProcessSong.prepareProjectedBoxView(context, 0, padding);

        ProjectedPerformanceView3Col(float s1_3, float s2_3, float s3_3) {
            scale1_3 = s1_3;
            scale2_3 = s2_3;
            scale3_3 = s3_3;
            fontsize1_3 = ProcessSong.getProjectedFontSize(scale1_3);
            fontsize2_3 = ProcessSong.getProjectedFontSize(scale2_3);
            fontsize3_3 = ProcessSong.getProjectedFontSize(scale3_3);
        }

        @Override
        protected void onPreExecute() {
            // Remove all views from the projector
            try {
                projected_LinearLayout.removeAllViews();
                lyrics1_3.setPadding(0, 0, 0, 0);
                lyrics2_3.setPadding(0, 0, 0, 0);
                lyrics3_3.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // Add the song sections...
                    for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                        if (x < FullscreenActivity.thirdsplit_section) {
                            lyrics1_3 = ProcessSong.projectedSectionView(context, x, fontsize1_3);
                            LinearLayout.LayoutParams llp1_3 = new LinearLayout.LayoutParams(availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                            llp1_3.setMargins(0, 0, 0, 0);
                            lyrics1_3.setLayoutParams(llp1_3);
                            lyrics1_3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                            box1_3.addView(lyrics1_3);
                        } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                            lyrics2_3 = ProcessSong.projectedSectionView(context, x, fontsize2_3);
                            LinearLayout.LayoutParams llp2_3 = new LinearLayout.LayoutParams(availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                            llp2_3.setMargins(0, 0, 0, 0);
                            lyrics2_3.setLayoutParams(llp2_3);
                            lyrics2_3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                            box2_3.addView(lyrics2_3);
                        } else {
                            lyrics3_3 = ProcessSong.projectedSectionView(context, x, fontsize3_3);
                            LinearLayout.LayoutParams llp3_3 = new LinearLayout.LayoutParams(availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                            llp3_3.setMargins(0, 0, 0, 0);
                            lyrics3_3.setLayoutParams(llp3_3);
                            lyrics3_3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                            box3_3.addView(lyrics3_3);
                        }
                    }

                    // Now add the display
                    LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(availableWidth_3col + (padding * 2), availableScreenHeight + padding);
                    LinearLayout.LayoutParams llp3 = new LinearLayout.LayoutParams(availableWidth_3col + (padding * 2), availableScreenHeight + padding);
                    llp1.setMargins(0, 0, padding, 0);
                    llp3.setMargins(0, 0, 0, 0);
                    projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    box1_3.setLayoutParams(llp1);
                    box2_3.setLayoutParams(llp1);
                    box3_3.setLayoutParams(llp3);
                    projected_LinearLayout.addView(box1_3);
                    projected_LinearLayout.addView(box2_3);
                    projected_LinearLayout.addView(box3_3);
                    animateIn();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static void animateOut() {
        // If the logo is showing, fade it away
        if (projected_Logo.getAlpha() > 0.0f) {
            projected_Logo.startAnimation(logo_fadeout);
        }
        // Fade in the main page
        if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
            projected_ImageView.startAnimation(image_fadeout);
        } else {
            projected_LinearLayout.startAnimation(lyrics_fadeout);
        }
        getScreenSizes();  // Just in case something changed
    }

    private static void animateIn() {
        // Fade in the main page
        if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
            projected_ImageView.startAnimation(image_fadein);
        } else {
            projected_LinearLayout.startAnimation(lyrics_fadein);
        }
    }

    private static void wipeAllViews() {
        projected_LinearLayout.removeAllViews();
        projected_ImageView.setImageBitmap(null);
    }

    private static void updateAlpha() {
        projected_BackgroundImage.setAlpha(FullscreenActivity.presoAlpha);
        projected_TextureView.setAlpha(FullscreenActivity.presoAlpha);
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void reloadVideo() throws IOException {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setSurface(s);
            }

            mMediaPlayer.reset();
            try {
                mMediaPlayer.setDataSource(vidFile);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                Log.e("Presentation window", "media player error");
            }
        }
    }

    static void updateAlert(boolean show) {
        if (show) {
            PresenterMode.alert_on = "Y";
            fadeinAlert();
        } else {
            PresenterMode.alert_on = "N";
            fadeoutAlert();
        }
    }

    private static void fadeinAlert() {
        presentermode_alert.setText(FullscreenActivity.myAlert);
        presentermode_alert.setTypeface(FullscreenActivity.presoInfoFont);
        presentermode_alert.setTextSize(FullscreenActivity.presoAlertSize);
        presentermode_alert.setTextColor(FullscreenActivity.presoAlertFontColor);
        presentermode_alert.setShadowLayer(FullscreenActivity.presoAlertSize / 2.0f, 4, 4, FullscreenActivity.presoShadowColor);
        presentermode_alert.setVisibility(View.VISIBLE);
        getScreenSizes();
        presentermode_alert.startAnimation(songalert_fadein);
    }

    private static void fadeoutAlert() {
        presentermode_alert.startAnimation(songalert_fadeout);
        Handler ha = new Handler();
        ha.postDelayed(new Runnable() {
            @Override
            public void run() {
                presentermode_alert.setVisibility(View.GONE);
                getScreenSizes();
            }
        }, FullscreenActivity.presoTransitionTime * 2);
    }

    static void updateFonts() {
        presenterThemeSetUp(); // Sets the bottom info bar for presentation
        doUpdate(); // Updates the page
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            s = new Surface(surface);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setSurface(s);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            if (FullscreenActivity.backgroundTypeToUse.equals("video")) {
                try {
                    mMediaPlayer.setDataSource(vidFile);
                    mMediaPlayer.prepareAsync();
                } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                reloadVideo();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

// The stuff below included suggested optimisations after update to Android Studio 3.0.  However,
// some users have had problems with ChromeCast support, so I've rolled this page back to 4.0.9,
// but have included the fixes for the new versions of Glide

/*
package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
class PresentationServiceHDMI extends Presentation
            implements TextureView.SurfaceTextureListener,
            MediaPlayer.OnBufferingUpdateListener,
            MediaPlayer.OnVideoSizeChangedListener,
            MediaPlayer.OnPreparedListener,
            MediaPlayer.OnCompletionListener {

    PresentationServiceHDMI(Context c, Display display) {
        super(c, display);
        context = c;
        myscreen = display;
    }

    private static Display myscreen;
    @SuppressLint("StaticFieldLeak")
    private static RelativeLayout pageHolder;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout projected_LinearLayout;
    @SuppressLint("StaticFieldLeak")
    private static RelativeLayout projectedPage_RelativeLayout;
    @SuppressLint("StaticFieldLeak")
    private static ImageView projected_ImageView;
    @SuppressLint("StaticFieldLeak")
    private static ImageView projected_Logo;
    @SuppressLint("StaticFieldLeak")
    private static TextView songinfo_TextView;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout bottom_infobar;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout col1_1;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout col1_2;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout col2_2;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout col1_3;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout col2_3;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout col3_3;
    @SuppressLint("StaticFieldLeak")
    private static TextureView projected_TextureView;
    @SuppressLint("StaticFieldLeak")
    private static ImageView projected_BackgroundImage;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout presentermode_bottombit;
    @SuppressLint("StaticFieldLeak")
    private static TextView presentermode_title;
    @SuppressLint("StaticFieldLeak")
    private static TextView presentermode_author;
    @SuppressLint("StaticFieldLeak")
    private static TextView presentermode_copyright;
    @SuppressLint("StaticFieldLeak")
    private static TextView presentermode_alert;

    @SuppressLint("StaticFieldLeak")
    static Context context;
    private static int availableScreenWidth;
    private static int availableScreenHeight;
    private static int padding;
    private static int availableWidth_1col;
    private static int availableWidth_2col;
    private static int availableWidth_3col;
    private static int[] projectedviewwidth;
    private static int[] projectedviewheight;
    @SuppressWarnings("unused")
    private static float[] projectedSectionScaleValue;

    private static AsyncTask<Object, Void, String> preparefullprojected_async;
    private static AsyncTask<Object, Void, String> preparestageprojected_async;
    private static AsyncTask<Object, Void, String> preparepresenterprojected_async;
    private static AsyncTask<Object, Void, String> projectedstageview1col_async;
    private static AsyncTask<Object, Void, String> projectedpresenterview1col_async;
    private static AsyncTask<Object, Void, String> projectedPerformanceView1Col_async;
    private static AsyncTask<Object, Void, String> projectedPerformanceView2Col_async;
    private static AsyncTask<Object, Void, String> projectedPerformanceView3Col_async;

    //MediaController
    private static MediaPlayer mMediaPlayer;

    // Images and video backgrounds
    private static File img1File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage1);
    private static File img2File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage2);
    private static String vid1File = FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo1;
    private static String vid2File = FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo2;
    private static String vidFile;
    private static Drawable defimage;
    static Surface s;
    private static Animation mypage_fadein;
    private static Animation mypage_fadeout;
    private static Animation background_fadein;
    private static Animation image_fadein;
    private static Animation image_fadeout;
    private static Animation logo_fadein;
    private static Animation logo_fadeout;
    private static Animation lyrics_fadein;
    private static Animation lyrics_fadeout;
    private static Animation songinfo_fadein;
    private static Animation songinfo_fadeout;
    private static Animation songtitle_fadein;
    private static Animation songtitle_fadeout;
    private static Animation songauthor_fadein;
    private static Animation songauthor_fadeout;
    private static Animation songcopyright_fadein;
    private static Animation songcopyright_fadeout;
    private static Animation songalert_fadein;
    private static Animation songalert_fadeout;


    @SuppressLint("StaticFieldLeak")
    static Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cast_screen);

        pageHolder = findViewById(R.id.pageHolder);
        projectedPage_RelativeLayout = findViewById(R.id.projectedPage_RelativeLayout);
        projected_LinearLayout = findViewById(R.id.projected_LinearLayout);
        projected_ImageView = findViewById(R.id.projected_ImageView);
        projected_BackgroundImage = findViewById(R.id.projected_BackgroundImage);
        projected_TextureView = findViewById(R.id.projected_TextureView);
        projected_Logo = findViewById(R.id.projected_Logo);
        songinfo_TextView = findViewById(R.id.songinfo_TextView);
        presentermode_bottombit = findViewById(R.id.presentermode_bottombit);
        presentermode_title = findViewById(R.id.presentermode_title);
        presentermode_author = findViewById(R.id.presentermode_author);
        presentermode_copyright = findViewById(R.id.presentermode_copyright);
        presentermode_alert = findViewById(R.id.presentermode_alert);
        bottom_infobar = findViewById(R.id.bottom_infobar);
        col1_1 = findViewById(R.id.col1_1);
        col1_2 = findViewById(R.id.col1_2);
        col2_2 = findViewById(R.id.col2_2);
        col1_3 = findViewById(R.id.col1_3);
        col2_3 = findViewById(R.id.col2_3);
        col3_3 = findViewById(R.id.col3_3);

        c = projectedPage_RelativeLayout.getContext();

        // Set up the custom background animations (to base on final alpha)
        prepareBackgroundAnimations();

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

        // Prepare the display after 2 secs (a chance for stuff to be measured and show the logo
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!FullscreenActivity.whichMode.equals("Presentation")) {
                    normalStartUp();
                } else {
                    // Switch to the user background and logo
                    presenterStartUp();
                }
            }
        }, 2000);
    }

    // Setup some default stuff
    private static void matchPresentationToMode() {
        switch (FullscreenActivity.whichMode) {
            case "Stage":
            case "Performance":
            default:
                songinfo_TextView.setAlpha(0.0f);
                songinfo_TextView.setVisibility(View.VISIBLE);
                presentermode_bottombit.setVisibility(View.GONE);
                projected_TextureView.setVisibility(View.GONE);
                projected_BackgroundImage.setImageDrawable(null);
                projected_BackgroundImage.setVisibility(View.GONE);
                break;

            case "Presentation":
                songinfo_TextView.setVisibility(View.GONE);
                presentermode_bottombit.setVisibility(View.VISIBLE);
                fixBackground();
                break;
        }
        FullscreenActivity.forcecastupdate = false;
    }

    private void prepareBackgroundAnimations() {
        mypage_fadein = CustomAnimations.setUpAnimation(pageHolder, 0.0f, 1.0f);
        mypage_fadeout = CustomAnimations.setUpAnimation(pageHolder, 1.0f, 0.0f);
        background_fadein = CustomAnimations.setUpAnimation(projected_BackgroundImage, 0.0f, 1.0f);
        //Animation background_fadeout = CustomAnimations.setUpAnimation(projected_BackgroundImage, 1.0f, 0.0f);
        logo_fadein = CustomAnimations.setUpAnimation(projected_Logo, 0.0f, 1.0f);
        logo_fadeout = CustomAnimations.setUpAnimation(projected_Logo, 1.0f, 0.0f);
        image_fadein = CustomAnimations.setUpAnimation(projected_ImageView, 0.0f, 1.0f);
        image_fadeout = CustomAnimations.setUpAnimation(projected_ImageView, 1.0f, 0.0f);
        //Animation video_fadein = CustomAnimations.setUpAnimation(projected_TextureView, 0.0f, 1.0f);
        //Animation video_fadeout = CustomAnimations.setUpAnimation(projected_TextureView, 1.0f, 0.0f);
        lyrics_fadein = CustomAnimations.setUpAnimation(projected_LinearLayout, 0.0f, 1.0f);
        lyrics_fadeout = CustomAnimations.setUpAnimation(projected_LinearLayout, 1.0f, 0.0f);
        songinfo_fadein = CustomAnimations.setUpAnimation(songinfo_TextView, 0.0f, 1.0f);
        songinfo_fadeout = CustomAnimations.setUpAnimation(songinfo_TextView, 1.0f, 0.0f);
        songtitle_fadein = CustomAnimations.setUpAnimation(presentermode_title, 0.0f, 1.0f);
        songtitle_fadeout = CustomAnimations.setUpAnimation(presentermode_title, 1.0f, 0.0f);
        songauthor_fadein = CustomAnimations.setUpAnimation(presentermode_author, 0.0f, 1.0f);
        songauthor_fadeout = CustomAnimations.setUpAnimation(presentermode_author, 1.0f, 0.0f);
        songcopyright_fadein = CustomAnimations.setUpAnimation(presentermode_copyright, 0.0f, 1.0f);
        songcopyright_fadeout = CustomAnimations.setUpAnimation(presentermode_copyright, 1.0f, 0.0f);
        songalert_fadein = CustomAnimations.setUpAnimation(presentermode_alert, 0.0f, 1.0f);
        songalert_fadeout = CustomAnimations.setUpAnimation(presentermode_alert, 1.0f, 0.0f);
    }

    @SuppressWarnings("deprecation")
    private void setDefaultBackgroundImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            defimage = getResources().getDrawable(R.drawable.preso_default_bg, null);
        } else {
            defimage = getResources().getDrawable(R.drawable.preso_default_bg);
        }
    }

    // Get and setup screen sizes
    static void changeMargins() {
        songinfo_TextView.setTextColor(FullscreenActivity.lyricsTextColor);
        projectedPage_RelativeLayout.setPadding(FullscreenActivity.xmargin_presentation,
                FullscreenActivity.ymargin_presentation, FullscreenActivity.xmargin_presentation,
                FullscreenActivity.ymargin_presentation);
    }

    @SuppressLint("NewApi")
    private static void getScreenSizes() {
        DisplayMetrics metrics = new DisplayMetrics();
        myscreen.getMetrics(metrics);
        Drawable icon = bottom_infobar.getContext().getDrawable(R.mipmap.ic_round_launcher);
        int bottombarheight = 0;
        if (icon != null) {
            bottombarheight = icon.getIntrinsicHeight();
        }

        //int density = metrics.densityDpi;

        padding = 8;

        int screenWidth = metrics.widthPixels;
        int leftpadding = projectedPage_RelativeLayout.getPaddingLeft();
        int rightpadding = projectedPage_RelativeLayout.getPaddingRight();
        availableScreenWidth = screenWidth - leftpadding - rightpadding;

        int screenHeight = metrics.heightPixels;
        int toppadding = projectedPage_RelativeLayout.getPaddingTop();
        int bottompadding = projectedPage_RelativeLayout.getPaddingBottom();
        availableScreenHeight = screenHeight - toppadding - bottompadding - bottombarheight - (padding * 4);
        availableWidth_1col = availableScreenWidth - (padding * 2);
        availableWidth_2col = (int) ((float) availableScreenWidth / 2.0f) - (padding * 3);
        availableWidth_3col = (int) ((float) availableScreenWidth / 3.0f) - (padding * 4);
    }

    // The logo stuff
    @SuppressWarnings("deprecation")
    static void setUpLogo() {
        // If the customLogo doesn't exist, use the default one
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int imgwidth = 1024;
        int imgheight = 500;
        float xscale;
        float yscale;
        boolean usingcustom = false;
        File customLogo = new File(FullscreenActivity.customLogo);
        if (customLogo.exists()) {
            // Get the sizes of the custom logo
            BitmapFactory.decodeFile(FullscreenActivity.customLogo, options);
            imgwidth = options.outWidth;
            imgheight = options.outHeight;
            if (imgwidth > 0 && imgheight > 0) {
                usingcustom = true;
            }
        }

        xscale = ((float) availableWidth_1col * FullscreenActivity.customLogoSize) / (float) imgwidth;
        yscale = ((float) availableScreenHeight * FullscreenActivity.customLogoSize) / (float) imgheight;

        if (xscale > yscale) {
            xscale = yscale;
        }

        int logowidth = (int) ((float) imgwidth * xscale);
        int logoheight = (int) ((float) imgheight * xscale);

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(logowidth, logoheight);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        projected_Logo.setLayoutParams(rlp);

        if (usingcustom) {
            Uri logoUri = Uri.fromFile(customLogo);
            RequestOptions myOptions = new RequestOptions()
                    .override(logowidth,logoheight);
            Glide.with(c).load(logoUri).apply(myOptions).into(projected_Logo);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                projected_Logo.setImageDrawable(c.getResources().getDrawable(R.drawable.ost_logo, c.getTheme()));
            } else {
                projected_Logo.setImageDrawable(c.getResources().getDrawable(R.drawable.ost_logo));
            }
        }
        if (PresenterMode.logoButton_isSelected) {
            projected_Logo.startAnimation(logo_fadein);
        }
    }

    static void showLogo() {
        // Animate out the lyrics if they were visible and animate in the logo
        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
            projected_ImageView.startAnimation(image_fadeout);
        } else {
            projected_LinearLayout.startAnimation(lyrics_fadeout);
        }

        // If we had a black screen, fade that in
        if (pageHolder.getVisibility() == View.INVISIBLE) {
            pageHolder.startAnimation(mypage_fadein);
        }

        presentermode_title.startAnimation(songtitle_fadeout);
        presentermode_author.startAnimation(songauthor_fadeout);
        presentermode_copyright.startAnimation(songcopyright_fadeout);
        projected_Logo.startAnimation(logo_fadein);
    }

    static void hideLogo() {
        // Animate out the logo and animate in the lyrics if they were visible
        // Animate out the lyrics if they were visible and animate in the logo
        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
            projected_ImageView.startAnimation(image_fadein);
        } else {
            projected_LinearLayout.startAnimation(lyrics_fadein);
        }
        presentermode_title.startAnimation(songtitle_fadein);
        presentermode_author.startAnimation(songauthor_fadein);
        presentermode_copyright.startAnimation(songcopyright_fadein);
        projected_Logo.startAnimation(logo_fadeout);
    }

    static void blankDisplay() {
        pageHolder.startAnimation(mypage_fadeout);
    }

    static void unblankDisplay() {
        pageHolder.startAnimation(mypage_fadein);
    }

    // Set up the screen changes
    private void presenterStartUp() {
        // Set up the text styles and fonts for the bottom info bar
        presenterThemeSetUp();

        // After the fadeout time, set the background and fade in
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Try to set the new background
                fixBackground();

                if (FullscreenActivity.backgroundTypeToUse.equals("image")) {
                    projected_BackgroundImage.startAnimation(background_fadein);
                } else if (FullscreenActivity.backgroundTypeToUse.equals("video")) {
                    projected_TextureView.startAnimation(background_fadein);
                }
            }
        }, FullscreenActivity.presoTransitionTime);
    }

    private void normalStartUp() {
        // Animate out the default logo
        projected_Logo.startAnimation(logo_fadeout);
        doUpdate();
    }

    private static void presenterThemeSetUp() {
        // Set the text at the bottom of the page to match the presentation text colour
        presentermode_title.setTypeface(FullscreenActivity.presoInfoFont);
        presentermode_author.setTypeface(FullscreenActivity.presoInfoFont);
        presentermode_copyright.setTypeface(FullscreenActivity.presoInfoFont);
        presentermode_alert.setTypeface(FullscreenActivity.presoInfoFont);
        presentermode_title.setTextColor(FullscreenActivity.presoInfoFontColor);
        presentermode_author.setTextColor(FullscreenActivity.presoInfoFontColor);
        presentermode_copyright.setTextColor(FullscreenActivity.presoInfoFontColor);
        presentermode_alert.setTextColor(FullscreenActivity.presoAlertFontColor);
        presentermode_title.setTextSize(FullscreenActivity.presoTitleSize);
        presentermode_author.setTextSize(FullscreenActivity.presoAuthorSize);
        presentermode_copyright.setTextSize(FullscreenActivity.presoCopyrightSize);
        presentermode_alert.setTextSize(FullscreenActivity.presoAlertSize);
        presentermode_title.setShadowLayer(FullscreenActivity.presoTitleSize / 2.0f, 4, 4, FullscreenActivity.presoShadowColor);
        presentermode_author.setShadowLayer(FullscreenActivity.presoAuthorSize / 2.0f, 4, 4, FullscreenActivity.presoShadowColor);
        presentermode_copyright.setShadowLayer(FullscreenActivity.presoCopyrightSize / 2.0f, 4, 4, FullscreenActivity.presoShadowColor);
        presentermode_alert.setShadowLayer(FullscreenActivity.presoAlertSize / 2.0f, 4, 4, FullscreenActivity.presoShadowColor);
        presentermode_title.setGravity(FullscreenActivity.presoInfoAlign);
        presentermode_author.setGravity(FullscreenActivity.presoInfoAlign);
        presentermode_copyright.setGravity(FullscreenActivity.presoInfoAlign);
        presentermode_alert.setGravity(FullscreenActivity.presoInfoAlign);
        if (PresenterMode.alert_on.equals("Y")) {
            presentermode_alert.setVisibility(View.VISIBLE);
        } else {
            presentermode_alert.setVisibility(View.GONE);
        }
    }

    private static void panicShowViews() {
        // After 3x the transition times, make sure the correct view is visible regardless of animations
        if (FullscreenActivity.whichMode.equals("Presentation")) {
            if (FullscreenActivity.isImage || FullscreenActivity.isPDF || FullscreenActivity.isImageSlide) {
                projected_ImageView.setVisibility(View.VISIBLE);
                projected_LinearLayout.setVisibility(View.GONE);
                projected_ImageView.setAlpha(1.0f);
            } else if (FullscreenActivity.isVideo) {
                projected_TextureView.setVisibility(View.VISIBLE);
                projected_LinearLayout.setVisibility(View.GONE);
                projected_ImageView.setVisibility(View.GONE);
                projected_TextureView.setAlpha(1.0f);
            } else {
                projected_LinearLayout.setVisibility(View.VISIBLE);
                projected_ImageView.setVisibility(View.GONE);
                projected_LinearLayout.setAlpha(1.0f);
            }
        }
    }

    static void doUpdate() {
        // First up, animate everything away
        animateOut();

        // If we have forced an update due to switching modes, set that up
        if (FullscreenActivity.forcecastupdate) {
            matchPresentationToMode();
        }

        // If we had a black screen, fade that in
        if (pageHolder.getVisibility() == View.INVISIBLE) {
            pageHolder.startAnimation(mypage_fadein);
        }

        // Just in case there is a glitch, make the stuff visible after 3x transition time
        Handler panic = new Handler();
        panic.postDelayed(new Runnable() {
            @Override
            public void run() {
                panicShowViews();
            }
        }, 3 * FullscreenActivity.presoTransitionTime);

        // Set the title of the song and author (if available).  Only does this for changes
        if (FullscreenActivity.whichMode.equals("Presentation")) {
            presenterWriteSongInfo();
        } else {
            setSongTitle();
        }

        // Now run the next bit post delayed (to wait for the animate out)
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Wipe any current views
                wipeAllViews();

                // Check the colours colour
                if (!FullscreenActivity.whichMode.equals("Presentation")) {
                    // Set the page background to the correct colour for Peformance/Stage modes
                    projectedPage_RelativeLayout.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);
                    songinfo_TextView.setTextColor(FullscreenActivity.lyricsTextColor);
                }

                // Decide on what we are going to show
                if (FullscreenActivity.isPDF) {
                    doPDFPage();
                } else if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide) {
                    doImagePage();
                } else {
                    switch (FullscreenActivity.whichMode) {
                        case "Stage":
                            prepareStageProjected();
                            break;
                        case "Performance":
                            prepareFullProjected();
                            break;
                        default:
                            preparePresenterProjected();
                            break;
                    }
                }
            }
        }, FullscreenActivity.presoTransitionTime);
    }

    // Change background images/videos
    static void fixBackground() {
        // Decide if user is using video or image for background
        switch (FullscreenActivity.backgroundTypeToUse) {
            case "image":
                projected_BackgroundImage.setVisibility(View.VISIBLE);
                projected_TextureView.setVisibility(View.INVISIBLE);
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
                File imgFile;
                if (FullscreenActivity.backgroundToUse.equals("img1")) {
                    imgFile = img1File;
                } else {
                    imgFile = img2File;
                }

                if (imgFile.exists()) {
                    if (imgFile.toString().contains("ost_bg.png")) {
                        projected_BackgroundImage.setImageDrawable(defimage);
                    } else {
                        // Process the image location into an URI
                        Uri imageUri = Uri.fromFile(imgFile);
                        RequestOptions myOptions = new RequestOptions()
                                .centerCrop();
                        Glide.with(c).load(imageUri).apply(myOptions).into(projected_BackgroundImage);
                    }
                    projected_BackgroundImage.setVisibility(View.VISIBLE);
                }
                break;
            case "video":
                projected_BackgroundImage.setVisibility(View.INVISIBLE);
                projected_TextureView.setVisibility(View.VISIBLE);

                if (FullscreenActivity.backgroundToUse.equals("vid1")) {
                    vidFile = vid1File;
                } else {
                    vidFile = vid2File;
                }
                if (mMediaPlayer != null) {
                    mMediaPlayer.start();
                }
                //Bitmap myBitmap = null;
                // dr = null;
                projected_BackgroundImage.setImageDrawable(null);
                projected_BackgroundImage.setVisibility(View.GONE);
                break;
            default:
                //myBitmap = null;
                //dr = null;
                projected_BackgroundImage.setImageDrawable(null);
                projected_BackgroundImage.setVisibility(View.GONE);
                break;
        }
        updateAlpha();
    }


    // Change the song info at the bottom of the page
    private static void setSongTitle() {
        String old_title = songinfo_TextView.getText().toString();
        String new_title = FullscreenActivity.mTitle.toString();
        if (!FullscreenActivity.mAuthor.equals("")) {
            new_title = new_title + "\n" + FullscreenActivity.mAuthor;
        }
        if (!old_title.equals(new_title)) {
            // It has changed, so make the text update on the screen
            normalChangeSongInfo(new_title);
        }
    }

    private static void presenterWriteSongInfo() {
        String old_title = presentermode_title.getText().toString();
        String old_author = presentermode_author.getText().toString();
        String old_copyright = presentermode_copyright.getText().toString();
        if (!old_title.equals(FullscreenActivity.mTitle)) {
            presenterFadeOutSongInfo(presentermode_title, songtitle_fadeout, songtitle_fadein, FullscreenActivity.mTitle.toString());
        }
        if (!old_author.equals(FullscreenActivity.mAuthor)) {
            presenterFadeOutSongInfo(presentermode_author, songauthor_fadeout, songauthor_fadein, FullscreenActivity.mAuthor.toString());
        }
        if (!old_copyright.equals(FullscreenActivity.mCopyright)) {
            presenterFadeOutSongInfo(presentermode_copyright, songcopyright_fadeout, songcopyright_fadein, FullscreenActivity.mCopyright.toString());
        }
    }

    private static void normalChangeSongInfo(final String s) {
        songinfo_TextView.startAnimation(songinfo_fadeout);
        // After the transition delay, write the new value and fade it back in
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                songinfo_TextView.setTextColor(FullscreenActivity.lyricsTextColor);
                songinfo_TextView.setText(s);
                songinfo_TextView.startAnimation(songinfo_fadein);
            }
        }, FullscreenActivity.presoTransitionTime);
    }

    private static void presenterFadeOutSongInfo(final TextView tv, Animation out, final Animation in, final String s) {
        if (tv.getAlpha() > 0.0f) {
            tv.startAnimation(out);
        } else {
            tv.setAlpha(0.0f);
        }
        // After the transition time, change the text and fade it back in
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv.setText(s);
                // If this is a pdf or image, hide the song info
                if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
                    presentermode_title.setVisibility(View.GONE);
                    presentermode_author.setVisibility(View.GONE);
                    presentermode_copyright.setVisibility(View.GONE);
                } else {
                    presentermode_title.setVisibility(View.VISIBLE);
                    presentermode_author.setVisibility(View.VISIBLE);
                    presentermode_copyright.setVisibility(View.VISIBLE);
                    presenterFadeInSongInfo(tv, in);
                }
            }
        }, FullscreenActivity.presoTransitionTime);
    }

    private static void presenterFadeInSongInfo(TextView tv, Animation in) {
        tv.startAnimation(in);
    }


    private static void doPDFPage() {
        Bitmap bmp = ProcessSong.createPDFPage(c, availableScreenWidth, availableScreenHeight, "Y");
        projected_ImageView.setVisibility(View.GONE);
        projected_ImageView.setBackgroundColor(0xffffffff);
        projected_ImageView.setImageBitmap(bmp);
        projected_ImageView.setVisibility(View.VISIBLE);
        animateIn();
    }

    private static void doImagePage() {
        projected_ImageView.setVisibility(View.GONE);
        projected_ImageView.setBackgroundColor(0x00000000);
        // Process the image location into an URI
        Uri imageUri = Uri.fromFile(FullscreenActivity.file);
        RequestOptions myOptions = new RequestOptions()
                .fitCenter();
        Glide.with(c).load(imageUri).apply(myOptions).into(projected_ImageView);
        projected_ImageView.setVisibility(View.VISIBLE);
        animateIn();
    }


    // Async stuff to prepare and write the page
    private static void cancelAsyncTask(AsyncTask ast) {
        if (ast != null) {
            try {
                ast.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void prepareStageProjected() {
        cancelAsyncTask(preparestageprojected_async);
        preparestageprojected_async = new PrepareStageProjected();
        try {
            FullscreenActivity.scalingfiguredout = false;
            preparestageprojected_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class PrepareStageProjected extends AsyncTask<Object, Void, String> {
        @SuppressLint("StaticFieldLeak")
        LinearLayout test1_1 = ProcessSong.createLinearLayout(context);

        @Override
        protected void onPreExecute() {
            // Remove all views from the test pane
            try {
                col1_1.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                projectedSectionScaleValue = new float[1];
                projectedviewwidth = new int[1];
                projectedviewheight = new int[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    test1_1 = ProcessSong.projectedSectionView(context, FullscreenActivity.currentSection, 12.0f);
                    col1_1.addView(test1_1);

                    // Now premeasure the view
                    test1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                    projectedviewwidth[0] = test1_1.getMeasuredWidth();
                    projectedviewheight[0] = test1_1.getMeasuredHeight();

                    // Now display the song!
                    //FullscreenActivity.scalingfiguredout = true;
                    projectedStageView1Col();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void projectedStageView1Col() {
        cancelAsyncTask(projectedstageview1col_async);
        projectedstageview1col_async = new ProjectedStageView1Col();
        try {
            projectedstageview1col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProjectedStageView1Col extends AsyncTask<Object, Void, String> {
        @SuppressLint("StaticFieldLeak")
        LinearLayout lyrics1_1 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout box1_1 = ProcessSong.prepareProjectedBoxView(context, 0, padding);
        float scale;

        @Override
        protected void onPreExecute() {
            try {
                projected_LinearLayout.removeAllViews();
                float max_width_scale = (float) availableWidth_1col / (float) projectedviewwidth[0];
                float max_height_scale = (float) availableScreenHeight / (float) projectedviewheight[0];
                if (max_height_scale > max_width_scale) {
                    scale = max_width_scale;
                } else {
                    scale = max_height_scale;
                }

                float maxscale = FullscreenActivity.presoMaxFontSize / 12.0f;
                if (scale > maxscale) {
                    scale = maxscale;
                }

                projected_LinearLayout.removeAllViews();
                lyrics1_1.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    lyrics1_1 = ProcessSong.projectedSectionView(context, FullscreenActivity.currentSection, ProcessSong.getProjectedFontSize(scale));
                    LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_1.setMargins(0, 0, 0, 0);
                    lyrics1_1.setLayoutParams(llp1_1);
                    //lyrics1_1.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[FullscreenActivity.currentSection]));
                    box1_1.addView(lyrics1_1);

                    // Now add the display
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(availableScreenWidth, availableScreenHeight + padding);
                    llp.setMargins(0, 0, 0, 0);
                    projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    box1_1.setLayoutParams(llp);
                    projected_LinearLayout.addView(box1_1);
                    animateIn();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void preparePresenterProjected() {
        cancelAsyncTask(preparepresenterprojected_async);
        preparepresenterprojected_async = new PreparePresenterProjected();
        try {
            preparepresenterprojected_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class PreparePresenterProjected extends AsyncTask<Object, Void, String> {
        @SuppressLint("StaticFieldLeak")
        LinearLayout test1_1 = ProcessSong.createLinearLayout(context);

        @Override
        protected void onPreExecute() {
            // Remove all views from the test pane
            try {
                col1_1.removeAllViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                projectedSectionScaleValue = new float[1];
                projectedviewwidth = new int[1];
                projectedviewheight = new int[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    test1_1 = ProcessSong.projectedSectionView(context, FullscreenActivity.currentSection, 12.0f);
                    col1_1.addView(test1_1);

                    // Now premeasure the view
                    test1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                    projectedviewwidth[0] = test1_1.getMeasuredWidth();
                    projectedviewheight[0] = test1_1.getMeasuredHeight();

                    // Now display the song!
                    //FullscreenActivity.scalingfiguredout = true;
                    projectedPresenterView1Col();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void projectedPresenterView1Col() {
        cancelAsyncTask(projectedpresenterview1col_async);
        projectedpresenterview1col_async = new ProjectedPresenterView1Col();
        try {
            projectedpresenterview1col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProjectedPresenterView1Col extends AsyncTask<Object, Void, String> {
        @SuppressLint("StaticFieldLeak")
        LinearLayout lyrics1_1 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout box1_1 = ProcessSong.prepareProjectedBoxView(context, 0, padding);
        float scale;

        @Override
        protected void onPreExecute() {
            try {
                projected_LinearLayout.removeAllViews();
                float max_width_scale = (float) availableWidth_1col / (float) projectedviewwidth[0];
                float max_height_scale = (float) availableScreenHeight / (float) projectedviewheight[0];
                if (max_height_scale > max_width_scale) {
                    scale = max_width_scale;
                } else {
                    scale = max_height_scale;
                }

                float maxscale = FullscreenActivity.presoMaxFontSize / 12.0f;
                if (scale > maxscale) {
                    scale = maxscale;
                }

                projected_LinearLayout.removeAllViews();
                lyrics1_1.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    lyrics1_1 = ProcessSong.projectedSectionView(context, FullscreenActivity.currentSection, ProcessSong.getProjectedFontSize(scale));
                    LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_1.setMargins(0, 0, 0, 0);
                    lyrics1_1.setLayoutParams(llp1_1);
                    box1_1.addView(lyrics1_1);

                    // Now add the display
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(availableScreenWidth, availableScreenHeight + padding);
                    llp.setMargins(0, 0, 0, 0);
                    projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    box1_1.setLayoutParams(llp);
                    projected_LinearLayout.addView(box1_1);
                    animateIn();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void displayFullSong() {
        projected_LinearLayout.removeAllViews();

        // We know the widths and heights of all of the view (1,2 and 3 columns).
        // Decide which is best by looking at the scaling

        int colstouse = 1;
        // We know the size of each section, so we just need to know which one to display
        int widthofsection1_1 = projectedviewwidth[0];
        int widthofsection1_2 = projectedviewwidth[1];
        int widthofsection2_2 = projectedviewwidth[2];
        int widthofsection1_3 = projectedviewwidth[3];
        int widthofsection2_3 = projectedviewwidth[4];
        int widthofsection3_3 = projectedviewwidth[5];
        int heightofsection1_1 = projectedviewheight[0];
        int heightofsection1_2 = projectedviewheight[1];
        int heightofsection2_2 = projectedviewheight[2];
        int heightofsection1_3 = projectedviewheight[3];
        int heightofsection2_3 = projectedviewheight[4];
        int heightofsection3_3 = projectedviewheight[5];

        float maxwidth_scale1_1 = ((float) availableWidth_1col) / (float) widthofsection1_1;
        float maxwidth_scale1_2 = ((float) availableWidth_2col) / (float) widthofsection1_2;
        float maxwidth_scale2_2 = ((float) availableWidth_2col) / (float) widthofsection2_2;
        float maxwidth_scale1_3 = ((float) availableWidth_3col) / (float) widthofsection1_3;
        float maxwidth_scale2_3 = ((float) availableWidth_3col) / (float) widthofsection2_3;
        float maxwidth_scale3_3 = ((float) availableWidth_3col) / (float) widthofsection3_3;
        float maxheight_scale1_1 = ((float) availableScreenHeight) / (float) heightofsection1_1;
        float maxheight_scale1_2 = ((float) availableScreenHeight) / (float) heightofsection1_2;
        float maxheight_scale2_2 = ((float) availableScreenHeight) / (float) heightofsection2_2;
        float maxheight_scale1_3 = ((float) availableScreenHeight) / (float) heightofsection1_3;
        float maxheight_scale2_3 = ((float) availableScreenHeight) / (float) heightofsection2_3;
        float maxheight_scale3_3 = ((float) availableScreenHeight) / (float) heightofsection3_3;

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
            if (maxwidth_scale1_2 > maxwidth_scale2_2) {
                myfullscale = maxwidth_scale2_2;
            } else {
                myfullscale = maxwidth_scale1_2;
            }
        }

        if (maxwidth_scale1_3 > myfullscale && maxwidth_scale2_3 > myfullscale && maxwidth_scale3_3 > myfullscale) {
            colstouse = 3;
        }

        // Now we know how many columns we should use, let's do it!
        float maxscale = FullscreenActivity.presoMaxFontSize / 12.0f;

        switch (colstouse) {
            case 1:
                if (maxwidth_scale1_1 > maxscale) {
                    maxwidth_scale1_1 = maxscale;
                }
                projectedPerformanceView1col(maxwidth_scale1_1);
                break;

            case 2:
                if (maxwidth_scale1_2 > maxscale) {
                    maxwidth_scale1_2 = maxscale;
                }
                if (maxwidth_scale2_2 > maxscale) {
                    maxwidth_scale2_2 = maxscale;
                }
                projectedPerformanceView2col(maxwidth_scale1_2, maxwidth_scale2_2);
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
                projectedPerformanceView3col(maxwidth_scale1_3, maxwidth_scale2_3, maxwidth_scale3_3);
                break;
        }
    }

    private static void prepareFullProjected() {
        cancelAsyncTask(preparefullprojected_async);
        preparefullprojected_async = new PrepareFullProjected();
        try {
            FullscreenActivity.scalingfiguredout = false;
            preparefullprojected_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class PrepareFullProjected extends AsyncTask<Object, Void, String> {
        @SuppressLint("StaticFieldLeak")
        LinearLayout test1_1 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout test1_2 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout test2_2 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout test1_3 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout test2_3 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout test3_3 = ProcessSong.createLinearLayout(context);

        @Override
        protected void onPreExecute() {
            // Remove all views from the test panes
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
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                projectedSectionScaleValue = new float[6];
                projectedviewwidth = new int[6];
                projectedviewheight = new int[6];
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // Prepare the new views to add to 1,2 and 3 colums ready for measuring
                    // Go through each section
                    for (int x = 0; x < FullscreenActivity.songSections.length; x++) {

                        test1_1 = ProcessSong.projectedSectionView(context, x, 12.0f);
                        col1_1.addView(test1_1);

                        if (x < FullscreenActivity.halfsplit_section) {
                            test1_2 = ProcessSong.projectedSectionView(context, x, 12.0f);
                            col1_2.addView(test1_2);
                        } else {
                            test2_2 = ProcessSong.projectedSectionView(context, x, 12.0f);
                            col2_2.addView(test2_2);
                        }

                        if (x < FullscreenActivity.thirdsplit_section) {
                            test1_3 = ProcessSong.projectedSectionView(context, x, 12.0f);
                            col1_3.addView(test1_3);
                        } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                            test2_3 = ProcessSong.projectedSectionView(context, x, 12.0f);
                            col2_3.addView(test2_3);
                        } else {
                            test3_3 = ProcessSong.projectedSectionView(context, x, 12.0f);
                            col3_3.addView(test3_3);
                        }
                    }

                    // Now premeasure the views
                    col1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    col1_2.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    col2_2.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    col1_3.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    col2_3.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    col3_3.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                    projectedviewwidth[0] = col1_1.getMeasuredWidth();
                    projectedviewheight[0] = col1_1.getMeasuredHeight();
                    projectedviewwidth[1] = col1_2.getMeasuredWidth();
                    projectedviewheight[1] = col1_2.getMeasuredHeight();
                    projectedviewwidth[2] = col2_2.getMeasuredWidth();
                    projectedviewheight[2] = col2_2.getMeasuredHeight();
                    projectedviewwidth[3] = col1_3.getMeasuredWidth();
                    projectedviewheight[3] = col1_3.getMeasuredHeight();
                    projectedviewwidth[4] = col2_3.getMeasuredWidth();
                    projectedviewheight[4] = col2_3.getMeasuredHeight();
                    projectedviewwidth[5] = col3_3.getMeasuredWidth();
                    projectedviewheight[5] = col3_3.getMeasuredHeight();

                    // Now display the song!
                    //FullscreenActivity.scalingfiguredout = true;
                    displayFullSong();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void projectedPerformanceView1col(float scale1_1) {
        cancelAsyncTask(projectedPerformanceView1Col_async);
        projectedPerformanceView1Col_async = new ProjectedPerformanceView1Col(scale1_1);
        try {
            projectedPerformanceView1Col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProjectedPerformanceView1Col extends AsyncTask<Object, Void, String> {
        @SuppressLint("StaticFieldLeak")
        LinearLayout lyrics1_1 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout box1_1 = ProcessSong.prepareProjectedBoxView(context, 0, padding);
        float scale1_1;
        float fontsize1_1;

        ProjectedPerformanceView1Col(float s1_1) {
            scale1_1 = s1_1;
            fontsize1_1 = ProcessSong.getProjectedFontSize(scale1_1);
        }

        @Override
        protected void onPreExecute() {
            // Remove all views from the projector
            try {
                projected_LinearLayout.removeAllViews();
                lyrics1_1.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... objects) {
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // Prepare the new views to add to 1,2 and 3 colums ready for measuring
                    // Go through each section
                    for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                        lyrics1_1 = ProcessSong.projectedSectionView(context, x, fontsize1_1);
                        LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
                        llp1_1.setMargins(0, 0, 0, 0);
                        lyrics1_1.setLayoutParams(llp1_1);
                        lyrics1_1.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                        box1_1.addView(lyrics1_1);
                    }

                    // Now add the display
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(availableScreenWidth, availableScreenHeight + padding);
                    llp.setMargins(0, 0, 0, 0);
                    projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    box1_1.setLayoutParams(llp);
                    projected_LinearLayout.addView(box1_1);
                    animateIn();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void projectedPerformanceView2col(float scale1_2, float scale2_2) {
        cancelAsyncTask(projectedPerformanceView2Col_async);
        projectedPerformanceView2Col_async = new ProjectedPerformanceView2Col(scale1_2, scale2_2);
        try {
            projectedPerformanceView2Col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProjectedPerformanceView2Col extends AsyncTask<Object, Void, String> {
        float scale1_2;
        float scale2_2;
        float fontsize1_2;
        float fontsize2_2;
        @SuppressLint("StaticFieldLeak")
        LinearLayout lyrics1_2 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout lyrics2_2 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout box1_2 = ProcessSong.prepareProjectedBoxView(context, 0, padding);
        @SuppressLint("StaticFieldLeak")
        LinearLayout box2_2 = ProcessSong.prepareProjectedBoxView(context, 0, padding);

        ProjectedPerformanceView2Col(float s1_2, float s2_2) {
            scale1_2 = s1_2;
            scale2_2 = s2_2;
            fontsize1_2 = ProcessSong.getProjectedFontSize(scale1_2);
            fontsize2_2 = ProcessSong.getProjectedFontSize(scale2_2);
        }

        @Override
        protected void onPreExecute() {
            try {
                // Remove all views from the projector
                projected_LinearLayout.removeAllViews();
                lyrics1_2.setPadding(0, 0, 0, 0);
                lyrics2_2.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // Add the song sections...
                    for (int x = 0; x < FullscreenActivity.songSections.length; x++) {

                        if (x < FullscreenActivity.halfsplit_section) {
                            lyrics1_2 = ProcessSong.projectedSectionView(context, x, fontsize1_2);
                            LinearLayout.LayoutParams llp1_2 = new LinearLayout.LayoutParams(availableWidth_2col, LinearLayout.LayoutParams.WRAP_CONTENT);
                            llp1_2.setMargins(0, 0, 0, 0);
                            lyrics1_2.setLayoutParams(llp1_2);
                            lyrics1_2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                            box1_2.addView(lyrics1_2);
                        } else {
                            lyrics2_2 = ProcessSong.projectedSectionView(context, x, fontsize2_2);
                            LinearLayout.LayoutParams llp2_2 = new LinearLayout.LayoutParams(availableWidth_2col, LinearLayout.LayoutParams.WRAP_CONTENT);
                            llp2_2.setMargins(0, 0, 0, 0);
                            lyrics2_2.setLayoutParams(llp2_2);
                            lyrics2_2.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                            box2_2.addView(lyrics2_2);
                        }
                    }
                    // Now add the display
                    LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(availableWidth_2col + (padding * 2), availableScreenHeight + padding);
                    LinearLayout.LayoutParams llp2 = new LinearLayout.LayoutParams(availableWidth_2col + (padding * 2), availableScreenHeight + padding);
                    llp1.setMargins(0, 0, padding, 0);
                    llp2.setMargins(0, 0, 0, 0);
                    projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    box1_2.setLayoutParams(llp1);
                    box2_2.setLayoutParams(llp2);
                    projected_LinearLayout.addView(box1_2);
                    projected_LinearLayout.addView(box2_2);
                    animateIn();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void projectedPerformanceView3col(float scale1_3, float scale2_3, float scale3_3) {
        cancelAsyncTask(projectedPerformanceView3Col_async);
        projectedPerformanceView3Col_async = new ProjectedPerformanceView3Col(scale1_3, scale2_3, scale3_3);
        try {
            projectedPerformanceView3Col_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ProjectedPerformanceView3Col extends AsyncTask<Object, Void, String> {
        float scale1_3;
        float scale2_3;
        float scale3_3;
        float fontsize1_3;
        float fontsize2_3;
        float fontsize3_3;
        @SuppressLint("StaticFieldLeak")
        LinearLayout lyrics1_3 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout lyrics2_3 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout lyrics3_3 = ProcessSong.createLinearLayout(context);
        @SuppressLint("StaticFieldLeak")
        LinearLayout box1_3 = ProcessSong.prepareProjectedBoxView(context, 0, padding);
        @SuppressLint("StaticFieldLeak")
        LinearLayout box2_3 = ProcessSong.prepareProjectedBoxView(context, 0, padding);
        @SuppressLint("StaticFieldLeak")
        LinearLayout box3_3 = ProcessSong.prepareProjectedBoxView(context, 0, padding);

        ProjectedPerformanceView3Col(float s1_3, float s2_3, float s3_3) {
            scale1_3 = s1_3;
            scale2_3 = s2_3;
            scale3_3 = s3_3;
            fontsize1_3 = ProcessSong.getProjectedFontSize(scale1_3);
            fontsize2_3 = ProcessSong.getProjectedFontSize(scale2_3);
            fontsize3_3 = ProcessSong.getProjectedFontSize(scale3_3);
        }

        @Override
        protected void onPreExecute() {
            // Remove all views from the projector
            try {
                projected_LinearLayout.removeAllViews();
                lyrics1_3.setPadding(0, 0, 0, 0);
                lyrics2_3.setPadding(0, 0, 0, 0);
                lyrics3_3.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            return null;
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                if (!cancelled) {
                    // Add the song sections...
                    for (int x = 0; x < FullscreenActivity.songSections.length; x++) {
                        if (x < FullscreenActivity.thirdsplit_section) {
                            lyrics1_3 = ProcessSong.projectedSectionView(context, x, fontsize1_3);
                            LinearLayout.LayoutParams llp1_3 = new LinearLayout.LayoutParams(availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                            llp1_3.setMargins(0, 0, 0, 0);
                            lyrics1_3.setLayoutParams(llp1_3);
                            lyrics1_3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                            box1_3.addView(lyrics1_3);
                        } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                            lyrics2_3 = ProcessSong.projectedSectionView(context, x, fontsize2_3);
                            LinearLayout.LayoutParams llp2_3 = new LinearLayout.LayoutParams(availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                            llp2_3.setMargins(0, 0, 0, 0);
                            lyrics2_3.setLayoutParams(llp2_3);
                            lyrics2_3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                            box2_3.addView(lyrics2_3);
                        } else {
                            lyrics3_3 = ProcessSong.projectedSectionView(context, x, fontsize3_3);
                            LinearLayout.LayoutParams llp3_3 = new LinearLayout.LayoutParams(availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                            llp3_3.setMargins(0, 0, 0, 0);
                            lyrics3_3.setLayoutParams(llp3_3);
                            lyrics3_3.setBackgroundColor(ProcessSong.getSectionColors(FullscreenActivity.songSectionsTypes[x]));
                            box3_3.addView(lyrics3_3);
                        }
                    }

                    // Now add the display
                    LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(availableWidth_3col + (padding * 2), availableScreenHeight + padding);
                    LinearLayout.LayoutParams llp3 = new LinearLayout.LayoutParams(availableWidth_3col + (padding * 2), availableScreenHeight + padding);
                    llp1.setMargins(0, 0, padding, 0);
                    llp3.setMargins(0, 0, 0, 0);
                    projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    box1_3.setLayoutParams(llp1);
                    box2_3.setLayoutParams(llp1);
                    box3_3.setLayoutParams(llp3);
                    projected_LinearLayout.addView(box1_3);
                    projected_LinearLayout.addView(box2_3);
                    projected_LinearLayout.addView(box3_3);
                    animateIn();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static void animateOut() {
        // If the logo is showing, fade it away
        if (projected_Logo.getAlpha() > 0.0f) {
            projected_Logo.startAnimation(logo_fadeout);
        }
        // Fade in the main page
        if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
            projected_ImageView.startAnimation(image_fadeout);
        } else {
            projected_LinearLayout.startAnimation(lyrics_fadeout);
        }
        getScreenSizes();  // Just in case something changed
    }

    private static void animateIn() {
        // Fade in the main page
        if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
            projected_ImageView.startAnimation(image_fadein);
        } else {
            projected_LinearLayout.startAnimation(lyrics_fadein);
        }
    }

    private static void wipeAllViews() {
        projected_LinearLayout.removeAllViews();
        projected_ImageView.setImageBitmap(null);
    }

    private static void updateAlpha() {
        projected_BackgroundImage.setAlpha(FullscreenActivity.presoAlpha);
        projected_TextureView.setAlpha(FullscreenActivity.presoAlpha);
    }


    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void reloadVideo() throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setSurface(s);
            }
                mMediaPlayer.reset();

            try {
                mMediaPlayer.setDataSource(vidFile);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                Log.e("Presentation window", "media player error");
            }
        }
    }

    static void updateAlert(boolean show) {
        if (show) {
            PresenterMode.alert_on = "Y";
            fadeinAlert();
        } else {
            PresenterMode.alert_on = "N";
            fadeoutAlert();
        }
    }

    private static void fadeinAlert() {
        presentermode_alert.setText(FullscreenActivity.myAlert);
        presentermode_alert.setTypeface(FullscreenActivity.presoInfoFont);
        presentermode_alert.setTextSize(FullscreenActivity.presoAlertSize);
        presentermode_alert.setTextColor(FullscreenActivity.presoAlertFontColor);
        presentermode_alert.setShadowLayer(FullscreenActivity.presoAlertSize / 2.0f, 4, 4, FullscreenActivity.presoShadowColor);
        presentermode_alert.setVisibility(View.VISIBLE);
        getScreenSizes();
        presentermode_alert.startAnimation(songalert_fadein);
    }

    private static void fadeoutAlert() {
        presentermode_alert.startAnimation(songalert_fadeout);
        Handler ha = new Handler();
        ha.postDelayed(new Runnable() {
            @Override
            public void run() {
                presentermode_alert.setVisibility(View.GONE);
                getScreenSizes();
            }
        }, FullscreenActivity.presoTransitionTime * 2);
    }


    static void updateFonts() {
        presenterThemeSetUp(); // Sets the bottom info bar for presentation
        doUpdate(); // Updates the page
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            s = new Surface(surface);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setSurface(s);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            if (FullscreenActivity.backgroundTypeToUse.equals("video")) {
                try {
                    mMediaPlayer.setDataSource(vidFile);
                    mMediaPlayer.prepareAsync();
                } catch (IllegalArgumentException | SecurityException | IllegalStateException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                reloadVideo();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}*/

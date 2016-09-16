package com.garethevans.church.opensongtablet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public final class MyPresentation extends Presentation
        implements TextureView.SurfaceTextureListener, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener{

    static File img1File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage1);
    static File img2File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage2);
    static String vid1File = FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo1;
    static String vid2File = FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo2;
    static String vidFile;
    static File imgFile;

    // Views on the presentation window
    static TextView presoLyrics1;
    static TextView presoLyrics2;
    static TextView presoLyricsOUT;
    static TextView presoLyricsIN;
    static TextView presoAuthor1;
    static TextView presoAuthor2;
    static TextView presoAuthorOUT;
    static TextView presoAuthorIN;
    static int whichPresoLyricsToUse = 1;
    static TextView presoCopyright1;
    static TextView presoCopyright2;
    static TextView presoCopyrightOUT;
    static TextView presoCopyrightIN;
    static TextView presoTitle1;
    static TextView presoTitle2;
    static TextView presoTitleIN;
    static TextView presoTitleOUT;
    static TextView presoAlert;
    static ImageView presoLogo;
    static ImageView presoBGImage;
    static TextureView presoBGVideo;
    static RelativeLayout bottomBit;
    static FrameLayout preso;
    static FrameLayout lyricsHolder;
    static ImageView slideImage1;
    static ImageView slideImage2;
    static ImageView slideImageIN;
    static ImageView slideImageOUT;

    static View lyricsINVScrollHolder;
    static View lyricsOUTVScrollHolder;
    static View lyricsINHScrollHolder;
    static View lyricsOUTHScrollHolder;
    static View lyrics1VScrollHolder;
    static View lyrics2VScrollHolder;
    static View lyrics1HScrollHolder;
    static View lyrics2HScrollHolder;

    int lyricsTextColor = FullscreenActivity.dark_lyricsTextColor;
    int lyricsShadowColor = FullscreenActivity.dark_lyricsBackgroundColor;
    static Drawable defimage;
    static Bitmap myBitmap;
    static Drawable dr;
    static int screenwidth;
    static int textwidth;
    static int screenheight;
    static int textheight;

    //MediaController
    static MediaPlayer mMediaPlayer;

    static Surface s;
    Context context;

    public MyPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedinstancestate) {
        // Notice that we get resources from the context of the Presentation
        //Resources resources = getContext().getResources();
        setContentView(R.layout.projector_screen);
        context = getContext();
        // Get width and height
        DisplayMetrics metrics = new DisplayMetrics();
        Display mDisplay = MyPresentation.this.getDisplay();
        mDisplay.getMetrics(metrics);

        preso = (FrameLayout) findViewById(R.id.preso);
        bottomBit = (RelativeLayout) findViewById(R.id.bottomBit);

        lyricsHolder = (FrameLayout) findViewById(R.id.lyricsHolder);

        presoBGImage = (ImageView) findViewById(R.id.presoBGImage);
        presoBGVideo = (TextureView) findViewById(R.id.presoBGVideo);

        presoBGVideo.setSurfaceTextureListener(this);

        slideImage1 = (ImageView) findViewById(R.id.slideImage1);
        slideImage1.setVisibility(View.GONE);
        slideImage2 = (ImageView) findViewById(R.id.slideImage2);
        slideImage2.setVisibility(View.GONE);
        slideImageIN = slideImage1;
        slideImageOUT = slideImage2;

        fixBackground();

        SetTypeFace.setTypeface();

        presoLyrics1 = (TextView) findViewById(R.id.presoLyrics1);
        presoLyrics1.setTextColor(0xffffffff);
        presoLyrics1.setTypeface(FullscreenActivity.presofont);
        presoLyrics1.setText(" ");
        presoLyrics1.setTextSize(72);
        presoLyrics1.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
        presoLyrics1.setAlpha(0.0f);
        presoLyrics1.setVisibility(View.GONE);
        presoLyrics1.setHorizontallyScrolling(true);
        presoLyrics1.setPivotY(0);
        presoLyrics1.setTranslationY(0);
        presoLyrics1.setY(0);
        presoLyrics2 = (TextView) findViewById(R.id.presoLyrics2);
        presoLyrics2.setTextColor(0xffffffff);
        presoLyrics2.setTypeface(FullscreenActivity.presofont);
        presoLyrics2.setText(" ");
        presoLyrics2.setTextSize(72);
        presoLyrics2.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
        presoLyrics2.setAlpha(0.0f);
        presoLyrics2.setVisibility(View.GONE);
        presoLyrics2.setHorizontallyScrolling(true);
        presoLyrics2.setPivotY(0);
        presoLyrics2.setTranslationY(0);
        presoLyrics2.setY(0);
        presoTitle1 = (TextView) findViewById(R.id.presoTitle1);
        presoTitle1.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
        presoTitle1.setTextColor(lyricsTextColor);
        presoTitle1.setTextSize(FullscreenActivity.presoTitleSize);
        presoTitle1.setTypeface(FullscreenActivity.presofont);
        presoTitle1.setText(" ");
        presoTitle2 = (TextView) findViewById(R.id.presoTitle2);
        presoTitle2.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
        presoTitle2.setTextColor(lyricsTextColor);
        presoTitle2.setTextSize(FullscreenActivity.presoTitleSize);
        presoTitle2.setTypeface(FullscreenActivity.presofont);
        presoTitle2.setText(" ");
        presoAuthor1 = (TextView) findViewById(R.id.presoAuthor1);
        presoAuthor1.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
        presoAuthor1.setTextColor(lyricsTextColor);
        presoAuthor1.setText(" ");
        presoAuthor1.setTextSize(FullscreenActivity.presoAuthorSize);
        presoAuthor1.setTypeface(FullscreenActivity.presofont);
        presoAuthor2 = (TextView) findViewById(R.id.presoAuthor2);
        presoAuthor2.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
        presoAuthor2.setTextColor(lyricsTextColor);
        presoAuthor2.setText(" ");
        presoAuthor2.setTextSize(FullscreenActivity.presoAuthorSize);
        presoAuthor2.setTypeface(FullscreenActivity.presofont);
        presoAlert = (TextView) findViewById(R.id.presoAlert);
        presoAlert.setVisibility(View.INVISIBLE);
        presoAlert.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
        presoAlert.setTextColor(lyricsTextColor);
        presoAlert.setTextSize(FullscreenActivity.presoAlertSize);
        presoAlert.setTypeface(FullscreenActivity.presofont);
        presoAlert.setText("");
        presoCopyright1 = (TextView) findViewById(R.id.presoCopyright1);
        presoCopyright1.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
        presoCopyright1.setTextColor(lyricsTextColor);
        presoCopyright1.setTextSize(FullscreenActivity.presoCopyrightSize);
        presoCopyright1.setTypeface(FullscreenActivity.presofont);
        presoCopyright1.setText(" ");
        presoCopyright2 = (TextView) findViewById(R.id.presoCopyright2);
        presoCopyright2.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
        presoCopyright2.setTextColor(lyricsTextColor);
        presoCopyright2.setTextSize(FullscreenActivity.presoCopyrightSize);
        presoCopyright2.setTypeface(FullscreenActivity.presofont);
        presoCopyright2.setText(" ");
        presoLogo = (ImageView) findViewById(R.id.presoLogo);

        lyrics1VScrollHolder = findViewById(R.id.scrollView9);
        lyrics2VScrollHolder = findViewById(R.id.scrollView10);
        lyrics1HScrollHolder = findViewById(R.id.horizontalScrollView1);
        lyrics2HScrollHolder = findViewById(R.id.horizontalScrollView2);

        lyricsINVScrollHolder = lyrics1VScrollHolder;
        lyricsOUTVScrollHolder = lyrics2VScrollHolder;
        lyricsINHScrollHolder = lyrics1HScrollHolder;
        lyricsOUTHScrollHolder = lyrics2HScrollHolder;

        presoLyrics1.setPadding(FullscreenActivity.xmargin_presentation, FullscreenActivity.ymargin_presentation, FullscreenActivity.xmargin_presentation, 0);
        presoLyrics2.setPadding(FullscreenActivity.xmargin_presentation, FullscreenActivity.ymargin_presentation, FullscreenActivity.xmargin_presentation, 0);
        presoLogo.setPadding(FullscreenActivity.xmargin_presentation, FullscreenActivity.ymargin_presentation, FullscreenActivity.xmargin_presentation, FullscreenActivity.ymargin_presentation);
        bottomBit.setPadding(FullscreenActivity.xmargin_presentation, 0, FullscreenActivity.xmargin_presentation, FullscreenActivity.ymargin_presentation);
        LayoutParams params = preso.getLayoutParams();
        params.width = metrics.widthPixels;
        params.height = metrics.heightPixels;
        preso.setLayoutParams(params);

        presoBGVideo.setSurfaceTextureListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            defimage = getResources().getDrawable(R.drawable.preso_default_bg,null);
        } else {
            defimage = getResources().getDrawable(R.drawable.preso_default_bg);
        }

        // Set a listener for the presoLyrics to listen for size changes
        // This is used for the scale
        presoLyricsIN = presoLyrics1;
        presoAuthorIN = presoAuthor1;
        presoTitleIN = presoTitle1;
        presoCopyrightIN = presoCopyright1;
        presoLyricsIN.setHorizontallyScrolling(true);
        presoLyricsIN.setTextSize(72);

        ViewTreeObserver vto = presoLyricsIN.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                presoLyricsIN.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                // Get the width and height of this text
                //screenwidth = lyricsHolder.getWidth();
                screenwidth = lyricsHolder.getWidth();
                textwidth = presoLyricsIN.getWidth();
                screenheight = lyricsHolder.getHeight();
                textheight = presoLyricsIN.getHeight();
                if (PresenterMode.autoscale) {
                    doScale();
                } else {
                    presoLyricsIN.setTextSize(FullscreenActivity.presoFontSize);
                    updateFontSize();
                }


            }
        });
        updateAlpha();
    }

    public static void UpDatePresentation() {
        // See what has changed and fade those bits out/in
        // Crossfade the views
        if (PresenterMode.blackout.equals("Y")) {
            blackoutPresentation();
        } else if (PresenterMode.logo_on.equals("Y")) {
            fadeInLogo();
        } else if (PresenterMode.song_on.equals("Y")) {
            crossFadeSong();
        }
    }

    public static void doScale() {
        presoLyricsIN.setHorizontallyScrolling(true);

        lyricsINVScrollHolder.setScaleX(1.0f);
        lyricsINVScrollHolder.setScaleY(1.0f);
        lyricsINHScrollHolder.setScaleX(1.0f);
        lyricsINHScrollHolder.setScaleY(1.0f);

        // Get possible xscale value
        float xscale;
        if (textwidth != 0 && screenwidth != 0) {
            xscale = (float) screenwidth / (float) textwidth;
        } else {
            xscale = 1;
        }

        // Get possible yscale value
        float yscale;
        if (textheight != 0 && screenheight != 0) {
            yscale = (float) screenheight / (float) textheight;
        } else {
            yscale = 1;
        }

        // We have to use the smallest scale factor to make sure both fit
        if (xscale > yscale) {
            xscale = yscale;
        }

        presoLyricsIN.setPivotY(0);
        presoLyricsIN.setTranslationY(0);
        presoLyrics1.setPivotY(0);
        presoLyrics1.setTranslationY(0);
        presoLyrics2.setPivotY(0);
        presoLyrics2.setTranslationY(0);
        presoLyricsIN.setY(0);
        presoLyrics1.setY(0);
        presoLyrics2.setY(0);
        presoLyricsIN.setPivotX(textwidth / 2);
        if ((72*xscale)>FullscreenActivity.presoMaxFontSize) {
            presoLyricsIN.setTextSize(FullscreenActivity.presoMaxFontSize);
        } else {
            presoLyricsIN.setTextSize(72 * xscale);
        }

    }

    public static void fadeoutImage1() {
        // If slideImage1 is visible, fade it out
        if (slideImage1.getVisibility() == View.VISIBLE && slideImage1.getAlpha() > 0.0f) {
            slideImage1.setAlpha(1.0f);
            slideImage1.setVisibility(View.VISIBLE);
            slideImage1.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    slideImage1.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            slideImage1.setAlpha(0.0f);
            slideImage1.setVisibility(View.INVISIBLE);
        }
    }

    public static void fadeoutImage2() {
        // If slideImage2 is visible, fade it out
        if (slideImage2.getVisibility() == View.VISIBLE && slideImage2.getAlpha() > 0.0f) {
            slideImage2.setAlpha(1.0f);
            slideImage2.setVisibility(View.VISIBLE);
            slideImage2.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    slideImage2.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            slideImage2.setAlpha(0.0f);
            slideImage2.setVisibility(View.INVISIBLE);
        }
    }

    public static void fadeoutCopyright1() {
        // If presoCopyright1 is visible, fade it out
        if (presoCopyright1.getVisibility() == View.VISIBLE && presoCopyright1.getAlpha() > 0.0f) {
            presoCopyright1.setAlpha(1.0f);
            presoCopyright1.setVisibility(View.VISIBLE);
            presoCopyright1.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoCopyright1.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            presoCopyright1.setAlpha(0.0f);
            presoCopyright1.setVisibility(View.INVISIBLE);
        }
    }

    public static void fadeoutCopyright2() {
        // If presoCopyright2 is visible, it out
        if (presoCopyright2.getVisibility() == View.VISIBLE && presoCopyright2.getAlpha() > 0.0f) {
            presoCopyright2.setAlpha(1.0f);
            presoCopyright2.setVisibility(View.VISIBLE);
            presoCopyright2.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoCopyright2.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            presoCopyright2.setAlpha(0.0f);
            presoCopyright2.setVisibility(View.INVISIBLE);
        }
    }

    public static void fadeoutTitle1() {
        // If presoTitle1 is visible, fade it out
        if (presoTitle1.getVisibility() == View.VISIBLE && presoTitle1.getAlpha() > 0.0f) {
            presoTitle1.setAlpha(1.0f);
            presoTitle1.setVisibility(View.VISIBLE);
            presoTitle1.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoTitle1.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            presoTitle1.setAlpha(0.0f);
            presoTitle1.setVisibility(View.INVISIBLE);
        }
    }

    public static void fadeoutTitle2() {
        // If presoTitle2 is visible, it out
        if (presoTitle2.getVisibility() == View.VISIBLE && presoTitle2.getAlpha() > 0.0f) {
            presoTitle2.setAlpha(1.0f);
            presoTitle2.setVisibility(View.VISIBLE);
            presoTitle2.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoTitle2.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            presoTitle2.setAlpha(0.0f);
            presoTitle2.setVisibility(View.INVISIBLE);
        }
    }

    public static void fadeoutAuthor1() {
        // If presoAuthor1 is visible, fade it out
        if (presoAuthor1.getVisibility() == View.VISIBLE && presoAuthor1.getAlpha() > 0.0f) {
            presoAuthor1.setAlpha(1.0f);
            presoAuthor1.setVisibility(View.VISIBLE);
            presoAuthor1.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoAuthor1.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            presoAuthor1.setAlpha(0.0f);
            presoAuthor1.setVisibility(View.INVISIBLE);
        }
    }

    public static void fadeoutAuthor2() {
        // If presoAuthor2 is visible, it out
        if (presoAuthor2.getVisibility() == View.VISIBLE && presoAuthor2.getAlpha() > 0.0f) {
            presoAuthor2.setAlpha(1.0f);
            presoAuthor2.setVisibility(View.VISIBLE);
            presoAuthor2.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoAuthor2.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            presoAuthor2.setAlpha(0.0f);
            presoAuthor2.setVisibility(View.INVISIBLE);
        }
    }

    public static void fadeinAlert() {
        presoAlert.setText(PresenterMode.myAlert);
        presoAlert.setTypeface(FullscreenActivity.presofont);

        if (PresenterMode.alert_on.equals("Y") && presoAlert.getVisibility() == View.INVISIBLE) {
            presoAlert.setAlpha(0f);
            presoAlert.setVisibility(View.VISIBLE);
            presoAlert.animate().alpha(1f).setDuration(1000).setListener(null);
        } else {
            presoAlert.setAlpha(0f);
            presoAlert.setVisibility(View.VISIBLE);
        }
    }

    public static void fadeoutAlert() {
        presoAlert.setText(PresenterMode.myAlert);
        if (presoAlert.getVisibility() == View.VISIBLE) {
            presoAlert.setAlpha(1f);
            presoAlert.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoAlert.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            presoAlert.setAlpha(0f);
            presoAlert.setVisibility(View.INVISIBLE);
        }
    }

    public static void fadeoutLyrics2() {
        // If presoLyrics are visible, fade them out
        if (presoLyrics2.getVisibility() == View.VISIBLE && presoLyrics2.getAlpha() > 0.0f) {
            presoLyrics2.setAlpha(1.0f);
            presoLyrics2.setVisibility(View.VISIBLE);
            presoLyrics2.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoLyrics2.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            presoLyrics2.setAlpha(0.0f);
            presoLyrics2.setVisibility(View.INVISIBLE);
        }

    }

    public static void fadeoutLyrics1() {
        // If presoLyrics are visible, fade them out
        if (presoLyrics1.getVisibility() == View.VISIBLE && presoLyrics1.getAlpha() > 0.0f) {
            presoLyrics1.setAlpha(1.0f);
            presoLyrics1.setVisibility(View.VISIBLE);
            presoLyrics1.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoLyrics1.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            presoLyrics1.setAlpha(0.0f);
            presoLyrics1.setVisibility(View.INVISIBLE);
        }
    }

    public static void fadeInLogo() {
        // If logo is invisible or faded out, fade it in
        if (presoLogo.getVisibility() == View.INVISIBLE || presoLogo.getVisibility() == View.GONE || presoLogo.getAlpha() == 0f) {
            presoLogo.setAlpha(0.0f);
            presoLogo.setVisibility(View.VISIBLE);
            presoLogo.animate().alpha(1f).setDuration(1000).setListener(null);
        } else {
            presoLogo.setAlpha(1.0f);
            presoLogo.setVisibility(View.VISIBLE);
        }

        // We want to fade out the song details
        fadeoutLyrics1();
        fadeoutLyrics2();
        fadeoutTitle1();
        fadeoutTitle2();
        fadeoutAuthor1();
        fadeoutAuthor2();
        fadeoutCopyright1();
        fadeoutCopyright2();
        fadeoutImage1();
        fadeoutImage2();

        PresenterMode.logo_on = "Y";

    }

    public static void fadeOutLogo() {
        // If presoLogo is visible, fade it out
        if (presoLogo.getVisibility() == View.VISIBLE && presoLogo.getAlpha() > 0.0f) {
            presoLogo.setAlpha(1.0f);
            presoLogo.setVisibility(View.VISIBLE);
            presoLogo.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoLogo.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            presoLogo.setAlpha(0.0f);
            presoLogo.setVisibility(View.INVISIBLE);
        }
    }

    public static void crossFadeSong() {
        // There are two views for each song element (presoLyrics, presoAuthor, presoCopyright, presoAlert, slideImage)
        // This is to allow for smooth crossfading.

        // If the logo is showing, fade it out then hide it
        if (presoLogo.getVisibility() == View.VISIBLE) {
            fadeOutLogo();
        }

        // If the user is on a blank screen, we need to fade back in the background image or video
        if (preso.getVisibility() == View.INVISIBLE || preso.getVisibility() == View.GONE) {
            fadeInPage();
        }

        // Decide which view we are fading in.  By default its the 1st view
        whichPresoLyricsToUse = 1;
        slideImageIN = slideImage1;
        slideImageOUT = slideImage2;
        presoLyricsIN = presoLyrics1;
        presoLyricsOUT = presoLyrics2;
        presoTitleIN = presoTitle1;
        presoTitleOUT = presoTitle2;
        presoAuthorIN = presoAuthor1;
        presoAuthorOUT = presoAuthor2;
        presoCopyrightIN = presoCopyright1;
        presoCopyrightOUT = presoCopyright2;
        lyricsINVScrollHolder = lyrics1VScrollHolder;
        lyricsOUTVScrollHolder = lyrics2VScrollHolder;
        lyricsINHScrollHolder = lyrics1HScrollHolder;
        lyricsOUTHScrollHolder = lyrics2HScrollHolder;

        if (presoLyrics1.getVisibility() == View.VISIBLE) {
            // 1st is on already, so we are fading in the 2nd view
            whichPresoLyricsToUse = 2;
            slideImageIN = slideImage2;
            slideImageOUT = slideImage1;
            presoLyricsIN = presoLyrics2;
            presoLyricsOUT = presoLyrics1;
            presoTitleIN = presoTitle2;
            presoTitleOUT = presoTitle1;
            presoAuthorIN = presoAuthor2;
            presoAuthorOUT = presoAuthor1;
            presoCopyrightIN = presoCopyright2;
            presoCopyrightOUT = presoCopyright1;
            lyricsINVScrollHolder = lyrics2VScrollHolder;
            lyricsOUTVScrollHolder = lyrics1VScrollHolder;
            lyricsINHScrollHolder = lyrics2HScrollHolder;
            lyricsOUTHScrollHolder = lyrics1HScrollHolder;
        }

        // Make sure the visibilities and alphas of the fade in view are ready
        presoLyricsIN.setAlpha(0.0f);
        presoLyricsIN.setVisibility(View.VISIBLE);

        presoLyricsIN.setScaleX(1);
        presoLyricsIN.setScaleY(1);

        if (PresenterMode.buttonPresentText.equals("$$_IMAGE_$$!")) {
            slideImageIN.setAlpha(0.0f);
            slideImageIN.setVisibility(View.VISIBLE);
            presoBGImage.setVisibility(View.INVISIBLE);
            presoBGVideo.setVisibility(View.INVISIBLE);
        }

        // Decide on the font being used
        SetTypeFace.setTypeface();

        presoLyricsIN.setTypeface(FullscreenActivity.presofont);
        presoTitleIN.setTypeface(FullscreenActivity.presofont);
        presoAuthorIN.setTypeface(FullscreenActivity.presofont);
        presoCopyrightIN.setTypeface(FullscreenActivity.presofont);
        presoAlert.setTypeface(FullscreenActivity.presofont);
        presoLyricsIN.setTextSize(72);

        // Make sure the listener is ready for the new text being drawn to deal with scaling
        ViewTreeObserver vto = presoLyricsIN.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                presoLyricsIN.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //presoLyricsIN.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                // Get the width and height of this text
                screenwidth = lyricsHolder.getWidth();
                textwidth = presoLyricsIN.getWidth();
                screenheight = lyricsHolder.getHeight();
                textheight = presoLyricsIN.getHeight();
                if (PresenterMode.autoscale) {
                    doScale();
                } else {
                    presoLyricsIN.setTextSize(FullscreenActivity.presoFontSize);
                    presoLyricsOUT.setTextSize(FullscreenActivity.presoFontSize);
                }
                // Animate the view in
                presoLyricsIN.animate().alpha(1f).setDuration(1000).setListener(null);

                // Animate the other view out - ONLY IF IT IS VISIBLE
                if (presoLyricsOUT.getVisibility() == View.VISIBLE) {
                    presoLyricsOUT.setAlpha(1.0f);
                    presoLyricsOUT.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            presoLyricsOUT.setVisibility(View.INVISIBLE);
                            presoLyricsOUT.setScaleX(1);
                            presoLyricsOUT.setScaleY(1);
                            presoLyricsOUT.setPivotX(textwidth / 2);
                            presoLyricsOUT.setPivotY(0);
                            presoLyricsOUT.setY(0);
                            presoLyricsOUT.setTranslationY(0);
                        }
                    });
                } else {
                    // Just hide the out one if wasn't already visible
                    presoLyricsOUT.setAlpha(0.0f);
                    presoLyricsOUT.setVisibility(View.INVISIBLE);
                    presoLyricsOUT.setScaleX(1);
                    presoLyricsOUT.setScaleY(1);
                    presoLyricsOUT.setPivotX(textwidth / 2);
                    presoLyricsOUT.setPivotY(0);
                    presoLyricsOUT.setY(0);
                    presoLyricsOUT.setTranslationY(0);
                }
            }
        });

        // Set the text of the view that is being faded in
        // This should call the vto once updated
        presoLyricsIN.setText(PresenterMode.buttonPresentText);

        // Now we can do the same to the title, author, copyright and other fields
        // We only need to crossfade if the contents have changed (i.e. a different song).
        // Otherwise just switch them over
        presoTitleIN.setText(PresenterMode.presoTitle);
        presoAuthorIN.setText(PresenterMode.presoAuthor);
        presoCopyrightIN.setText(PresenterMode.presoCopyright);
        presoTitleIN.setTypeface(FullscreenActivity.presofont);
        presoAuthorIN.setTypeface(FullscreenActivity.presofont);
        presoCopyrightIN.setTypeface(FullscreenActivity.presofont);

        if (PresenterMode.buttonPresentText.equals("$$_IMAGE_$$")) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap ThumbImage;

            // Check if the image exists
            File checkImgFile = new File(PresenterMode.imageAddress);
            if (checkImgFile.exists()) {
                //Returns null, sizes are in the options variable
                BitmapFactory.decodeFile(PresenterMode.imageAddress, options);
                int width = options.outWidth;
                int height = options.outHeight;

                if (width > 1024) {
                    width = 1024;
                    float newheight = height / ((float) width / 1024.0f);
                    height = Math.round(newheight);
                }
                if (height > 768) {
                    height = 768;
                    float newwidth = width / ((float) height / 768.0f);
                    width = Math.round(newwidth);
                }

                ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(PresenterMode.imageAddress), width, height);
            } else {
                ThumbImage = null;
            }

            slideImageIN.setImageBitmap(ThumbImage);
            slideImageIN.setAlpha(0.0f);
            slideImageIN.setVisibility(View.VISIBLE);
            presoLyricsIN.setText("");
            presoTitleIN.setText("");
            presoAuthorIN.setText("");
            presoCopyrightIN.setText("");
        }

        if (presoTitleOUT.getVisibility() == View.VISIBLE && presoTitleOUT.getAlpha() > 0 && !presoTitleOUT.getText().toString().equals(PresenterMode.presoTitle)) {
            presoTitleOUT.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoTitleOUT.setVisibility(View.INVISIBLE);
                }
            }).start();
            presoTitleIN.setAlpha(0f);
            presoTitleIN.setVisibility(View.VISIBLE);
            presoTitleIN.animate().alpha(1f).setDuration(1000).setListener(null);
        } else {
            presoTitleOUT.setAlpha(0f);
            presoTitleOUT.setVisibility(View.INVISIBLE);
            presoTitleIN.setAlpha(1f);
            presoTitleIN.setVisibility(View.VISIBLE);
        }

        if (presoAuthorOUT.getVisibility() == View.VISIBLE && presoAuthorOUT.getAlpha() > 0 && !presoAuthorOUT.getText().toString().equals(PresenterMode.presoAuthor)) {
            presoAuthorOUT.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoAuthorOUT.setVisibility(View.INVISIBLE);
                }
            }).start();
            presoAuthorIN.setAlpha(0f);
            presoAuthorIN.setVisibility(View.VISIBLE);
            presoAuthorIN.animate().alpha(1f).setDuration(1000).setListener(null);
        } else {
            presoAuthorOUT.setAlpha(0f);
            presoAuthorOUT.setVisibility(View.INVISIBLE);
            presoAuthorIN.setAlpha(1f);
            presoAuthorIN.setVisibility(View.VISIBLE);
        }

        if (presoCopyrightOUT.getVisibility() == View.VISIBLE && presoCopyrightOUT.getAlpha() > 0 && !presoCopyrightOUT.getText().toString().equals(PresenterMode.presoCopyright)) {
            presoCopyrightOUT.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    presoCopyrightOUT.setVisibility(View.INVISIBLE);
                }
            }).start();
            presoCopyrightIN.setAlpha(0f);
            presoCopyrightIN.setVisibility(View.VISIBLE);
            presoCopyrightIN.animate().alpha(1f).setDuration(1000).setListener(null).start();
        } else {
            presoCopyrightOUT.setAlpha(0f);
            presoCopyrightOUT.setVisibility(View.INVISIBLE);
            presoCopyrightIN.setAlpha(1f);
            presoCopyrightIN.setVisibility(View.VISIBLE);
        }

        if (slideImageOUT.getVisibility() == View.VISIBLE && slideImageOUT.getAlpha() > 0) {
            slideImageOUT.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    slideImageOUT.setVisibility(View.INVISIBLE);
                    slideImageOUT.setImageDrawable(null);
                }
            }).start();
        } else {
            slideImageOUT.setVisibility(View.INVISIBLE);
            slideImageOUT.setAlpha(0.0f);
            slideImageOUT.setImageDrawable(null);
        }

        if (PresenterMode.buttonPresentText.equals("$$_IMAGE_$$")) {
            slideImageIN.setAlpha(0f);
            slideImageIN.setVisibility(View.VISIBLE);
            slideImageIN.animate().alpha(1f).setDuration(1000).setListener(null).start();
        }
    }

    public static void fadeInPage() {
        // Simply fade in preso
        PresenterMode.blackout = "N";
        // Switch the logo button back off in case
        PresenterMode.logo_on = "N";
        presoLogo.setVisibility(View.GONE);
        preso.setAlpha(0f);
        preso.setVisibility(View.VISIBLE);
        preso.animate().alpha(1f).setDuration(1000).setListener(null).start();
    }

    public static void fadeOutPage() {
        // Simply fade out preso
        preso.setAlpha(1f);
        preso.setVisibility(View.VISIBLE);
        preso.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                preso.setVisibility(View.GONE);
                // Empty the string values on the presentation - resets it all
                presoLyrics1.setText(" ");
                presoLyrics2.setText(" ");
                presoTitle1.setText(" ");
                presoTitle2.setText(" ");
                presoAuthor1.setText(" ");
                presoAuthor2.setText(" ");
                presoCopyright1.setText(" ");
                presoCopyright2.setText(" ");
                presoAlert.setText("");
            }
        }).start();
    }

    public static void fixBackground() {
        // Decide if user is using video or image for background
        switch (FullscreenActivity.backgroundTypeToUse) {
            case "image":
                presoBGImage.setVisibility(View.VISIBLE);
                presoBGVideo.setVisibility(View.INVISIBLE);
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
                        presoBGImage.setImageDrawable(defimage);
                    } else {
                        myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        dr = new BitmapDrawable(null,myBitmap);
                        //dr = new BitmapDrawable(myBitmap);
                        presoBGImage.setImageDrawable(dr);
                    }
                    presoBGImage.setVisibility(View.VISIBLE);
                }
                break;
            case "video":
                presoBGImage.setVisibility(View.INVISIBLE);
                presoBGVideo.setVisibility(View.VISIBLE);

                if (FullscreenActivity.backgroundToUse.equals("vid1")) {
                    vidFile = vid1File;
                } else {
                    vidFile = vid2File;
                }
                if (mMediaPlayer != null) {
                    mMediaPlayer.start();
                }
                preso.setBackgroundColor(0xff000000);
                myBitmap = null;
                dr = null;
                presoBGImage.setImageDrawable(null);
                presoBGImage.setVisibility(View.GONE);
                break;
            default:
                preso.setBackgroundColor(0xff000000);
                myBitmap = null;
                dr = null;
                presoBGImage.setImageDrawable(null);
                presoBGImage.setVisibility(View.GONE);
                break;
        }
        updateAlpha();
    }

    public static void blackoutPresentation() {
        if (preso.getVisibility() == View.GONE) {
            fadeInPage();
        } else {
            fadeOutPage();
        }
    }

    public static void updateFontSize() {
        presoLyrics1.setScaleX(1.0f);
        presoLyrics1.setScaleY(1.0f);
        presoLyrics2.setScaleX(1.0f);
        presoLyrics2.setScaleY(1.0f);
        lyricsINVScrollHolder.setScaleX(1.0f);
        lyricsINVScrollHolder.setScaleY(1.0f);
        lyricsOUTVScrollHolder.setScaleX(1.0f);
        lyricsOUTVScrollHolder.setScaleY(1.0f);
        lyricsINHScrollHolder.setScaleX(1.0f);
        lyricsINHScrollHolder.setScaleY(1.0f);
        lyricsOUTHScrollHolder.setScaleX(1.0f);
        lyricsOUTHScrollHolder.setScaleY(1.0f);
        presoLyrics1.setTextSize(FullscreenActivity.presoFontSize);
        presoLyrics2.setTextSize(FullscreenActivity.presoFontSize);
        presoTitle1.setTextSize(FullscreenActivity.presoTitleSize);
        presoTitle2.setTextSize(FullscreenActivity.presoTitleSize);
        presoAuthor1.setTextSize(FullscreenActivity.presoAuthorSize);
        presoAuthor2.setTextSize(FullscreenActivity.presoAuthorSize);
        presoCopyright1.setTextSize(FullscreenActivity.presoCopyrightSize);
        presoCopyright2.setTextSize(FullscreenActivity.presoCopyrightSize);
        presoAlert.setTextSize(FullscreenActivity.presoAlertSize);

    }

    public static void resetFontSize() {
        lyricsINVScrollHolder.setScaleX(1.0f);
        lyricsINVScrollHolder.setScaleY(1.0f);
        lyricsOUTVScrollHolder.setScaleX(1.0f);
        lyricsOUTVScrollHolder.setScaleY(1.0f);
        lyricsINHScrollHolder.setScaleX(1.0f);
        lyricsINHScrollHolder.setScaleY(1.0f);
        lyricsOUTHScrollHolder.setScaleX(1.0f);
        lyricsOUTHScrollHolder.setScaleY(1.0f);
        presoLyricsIN.setScaleX(1.0f);
        presoLyricsIN.setScaleY(1.0f);
        presoLyricsOUT.setScaleX(1.0f);
        presoLyricsOUT.setScaleY(1.0f);
        presoLyricsIN.setTextSize(72);
        presoLyricsOUT.setTextSize(72);
        ViewTreeObserver vto = presoLyricsIN.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                presoLyricsIN.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //presoLyricsIN.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                // Get the width and height of this text
                screenwidth = lyricsHolder.getWidth();
                textwidth = presoLyricsIN.getWidth();
                screenheight = lyricsHolder.getHeight();
                textheight = presoLyricsIN.getHeight();
                if (PresenterMode.autoscale) {
                    doScale();
                } else {
                    presoLyricsIN.setTextSize(FullscreenActivity.presoFontSize);
                    presoLyricsOUT.setTextSize(FullscreenActivity.presoFontSize);
                }
            }
        });
    }

    public static void changeMargins() {
        // Get width and height
        presoLyricsIN.setTextSize(72);
        preso.setPadding(PresenterMode.tempxmargin, PresenterMode.tempymargin, PresenterMode.tempxmargin, PresenterMode.tempymargin);
        if (PresenterMode.autoscale) {
            doScale();
        } else {
            presoLyrics1.setTextSize(FullscreenActivity.presoFontSize);
            presoLyrics2.setTextSize(FullscreenActivity.presoFontSize);
        }
    }

    public static void updateAlpha() {
        presoBGImage.setAlpha(FullscreenActivity.presoAlpha);
        presoBGVideo.setAlpha(FullscreenActivity.presoAlpha);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
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

    public static void reloadVideo() throws IOException {
        if (mMediaPlayer==null) {
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
            Log.e("Presentation window","media player error");
        }
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
        if (mp!=null) {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.reset();
        }
        try {
            reloadVideo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
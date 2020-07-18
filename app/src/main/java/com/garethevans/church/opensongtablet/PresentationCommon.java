/*
package com.garethevans.church.opensongtablet;

// This contains all of the scripts for the PresentationService and PresentationServiceHDMI

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._CustomAnimations;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

import java.io.InputStream;


class PresentationCommon {

    // The screen and layout defaults starting the projected display
    void getScreenSizes(Display myscreen, LinearLayout bottom_infobar, RelativeLayout projectedPage_RelativeLayout, float rotation) {
        DisplayMetrics metrics = new DisplayMetrics();
        myscreen.getRealMetrics(metrics);
        Drawable icon = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            icon = bottom_infobar.getContext().getDrawable(R.mipmap.ic_round_launcher);
        }
        int bottombarheight = 0;
        if (icon != null) {
            bottombarheight = icon.getIntrinsicHeight();
        }

        StaticVariables.cast_padding = 8;
        int leftpadding = projectedPage_RelativeLayout.getPaddingLeft();
        int rightpadding = projectedPage_RelativeLayout.getPaddingRight();
        int toppadding = projectedPage_RelativeLayout.getPaddingTop();
        int bottompadding = projectedPage_RelativeLayout.getPaddingBottom();

        projectedPage_RelativeLayout.setRotation(rotation);
        int originalWidth = metrics.widthPixels;
        int originalHeight = metrics.heightPixels;
        int newWidth, newHeight;
        ViewGroup.LayoutParams lp = projectedPage_RelativeLayout.getLayoutParams();

        if (rotation == 90.0f || rotation == 270.0f) {  // Switch width for height and vice versa
            newWidth = metrics.heightPixels;
            newHeight = metrics.widthPixels;

        } else {
            newWidth = metrics.widthPixels;
            newHeight = metrics.heightPixels;
        }

        StaticVariables.cast_screenWidth = newWidth;
        StaticVariables.cast_screenHeight = newHeight;

        projectedPage_RelativeLayout.setTranslationX((originalWidth - newWidth)/2.0f);
        projectedPage_RelativeLayout.setTranslationY((originalHeight - newHeight)/2.0f);
        lp.height = StaticVariables.cast_screenHeight;
        lp.width = StaticVariables.cast_screenWidth;
        projectedPage_RelativeLayout.requestLayout();

        StaticVariables.cast_availableScreenWidth = StaticVariables.cast_screenWidth - leftpadding - rightpadding;
        StaticVariables.cast_availableScreenHeight = StaticVariables.cast_screenHeight - toppadding - bottompadding - bottombarheight - (StaticVariables.cast_padding * 4);
        StaticVariables.cast_availableWidth_1col = StaticVariables.cast_availableScreenWidth - (StaticVariables.cast_padding * 2);
        StaticVariables.cast_availableWidth_2col = (int) ((float) StaticVariables.cast_availableScreenWidth / 2.0f) - (StaticVariables.cast_padding * 3);
        StaticVariables.cast_availableWidth_3col = (int) ((float) StaticVariables.cast_availableScreenWidth / 3.0f) - (StaticVariables.cast_padding * 4);
    }

    void setDefaultBackgroundImage(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StaticVariables.cast_defimage = c.getResources().getDrawable(R.drawable.preso_default_bg, null);
        } else {
            StaticVariables.cast_defimage = c.getResources().getDrawable(R.drawable.preso_default_bg);
        }
    }
    boolean matchPresentationToMode(TextView songinfo_TextView, LinearLayout presentermode_bottombit,
                                    SurfaceView projected_SurfaceView, ImageView projected_BackgroundImage,
                                    ImageView projected_ImageView) {
        boolean runfixbackground = false;
        switch (StaticVariables.whichMode) {
            case "Stage":
            case "Performance":
            default:
                songinfo_TextView.setAlpha(0.0f);
                songinfo_TextView.setVisibility(View.VISIBLE);
                presentermode_bottombit.setVisibility(View.GONE);
                projected_SurfaceView.setVisibility(View.GONE);
                projected_BackgroundImage.setImageDrawable(null);
                projected_BackgroundImage.setVisibility(View.GONE);
                projected_ImageView.setVisibility(View.GONE);
                break;

            case "Presentation":
                songinfo_TextView.setVisibility(View.GONE);
                presentermode_bottombit.setVisibility(View.VISIBLE);
                runfixbackground = true;
                break;
        }
        StaticVariables.forcecastupdate = false;
        return runfixbackground;
    }
    void changeMargins(Context c, _Preferences preferences, TextView songinfo_TextView, RelativeLayout projectedPage_RelativeLayout, int presoInfoColor) {
        songinfo_TextView.setTextColor(presoInfoColor);
        projectedPage_RelativeLayout.setPadding(preferences.getMyPreferenceInt(c,"presoXMargin",20),
                preferences.getMyPreferenceInt(c,"presoYMargin",10), preferences.getMyPreferenceInt(c,"presoXMargin",20),
                preferences.getMyPreferenceInt(c,"presoYMargin",10));
    }
    void fixBackground(Context c, _Preferences preferences, StorageAccess storageAccess, ImageView projected_BackgroundImage,
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
                if (StaticVariables.cast_mediaPlayer != null && StaticVariables.cast_mediaPlayer.isPlaying()) {
                    StaticVariables.cast_mediaPlayer.pause();
                }
                if (preferences.getMyPreferenceString(c,"backgroundToUse","img1").equals("img1")) {
                    imgUri = img1Uri;
                } else {
                    imgUri = img2Uri;
                }

                if (storageAccess.uriExists(c, imgUri)) {
                    if (imgUri != null && imgUri.getLastPathSegment() != null && imgUri.getLastPathSegment().contains("ost_bg.png")) {
                        projected_BackgroundImage.setImageDrawable(StaticVariables.cast_defimage);
                    } else {
                        RequestOptions myOptions = new RequestOptions()
                                .centerCrop();
                        GlideApp.with(c).load(imgUri).apply(myOptions).into(projected_BackgroundImage);
                    }
                    projected_BackgroundImage.setVisibility(View.VISIBLE);
                    _CustomAnimations.faderAnimationCustomAlpha(projected_BackgroundImage,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),
                            0.0f,preferences.getMyPreferenceFloat(c,"presoBackgroundAlpha",0.8f));

                }
                break;
            case "video":
                projected_BackgroundImage.setVisibility(View.INVISIBLE);
                projected_SurfaceView.setVisibility(View.VISIBLE);

                if (preferences.getMyPreferenceString(c,"backgroundToUse","img1").equals("vid1")) {
                    StaticVariables.cast_vidUri = vid1Uri;
                } else {
                    StaticVariables.cast_vidUri = vid2Uri;
                }
                try {
                    Log.d("d", "Trying to load video background");
                    reloadVideo(c,preferences,projected_SurfaceHolder,projected_SurfaceView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                projected_BackgroundImage.setImageDrawable(null);
                projected_BackgroundImage.setVisibility(View.GONE);

                _CustomAnimations.faderAnimationCustomAlpha(projected_SurfaceView,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),
                        0.0f,preferences.getMyPreferenceFloat(c,"presoBackgroundAlpha",0.8f));

                break;
            default:
                projected_BackgroundImage.setImageDrawable(null);
                projected_BackgroundImage.setVisibility(View.GONE);
                break;
        }
    }
    void getDefaultColors(Context c, _Preferences preferences) {
        switch (StaticVariables.mDisplayTheme) {
            case "dark":
            default:

                StaticVariables.cast_lyricsCapoColor = preferences.getMyPreferenceInt(c, "dark_lyricsCapoColor", StaticVariables.red);
                StaticVariables.cast_lyricsChordsColor = preferences.getMyPreferenceInt(c, "dark_lyricsChordsColor", StaticVariables.yellow);
                StaticVariables.cast_presoFontColor = preferences.getMyPreferenceInt(c, "dark_presoFontColor", StaticVariables.white);
                StaticVariables.cast_lyricsBackgroundColor = preferences.getMyPreferenceInt(c,"dark_lyricsBackgroundColor",StaticVariables.black);
                StaticVariables.cast_lyricsTextColor = preferences.getMyPreferenceInt(c,"dark_lyricsTextColor",StaticVariables.white);
                StaticVariables.cast_presoInfoColor = preferences.getMyPreferenceInt(c,"dark_presoInfoColor", StaticVariables.white);
                StaticVariables.cast_presoAlertColor = preferences.getMyPreferenceInt(c,"dark_presoAlertColor",StaticVariables.red);
                StaticVariables.cast_presoShadowColor = preferences.getMyPreferenceInt(c,"dark_presoShadowColor",StaticVariables.black);
                StaticVariables.cast_lyricsVerseColor = preferences.getMyPreferenceInt(c,"dark_lyricsVerseColor",StaticVariables.black);
                StaticVariables.cast_lyricsChorusColor = preferences.getMyPreferenceInt(c,"dark_lyricsChorusColor",StaticVariables.vdarkblue);
                StaticVariables.cast_lyricsPreChorusColor = preferences.getMyPreferenceInt(c,"dark_lyricsPreChorusColor",StaticVariables.darkishgreen);
                StaticVariables.cast_lyricsBridgeColor = preferences.getMyPreferenceInt(c,"dark_lyricsBridgeColor",StaticVariables.vdarkred);
                StaticVariables.cast_lyricsTagColor = preferences.getMyPreferenceInt(c,"dark_lyricsTagColor",StaticVariables.darkpurple);
                StaticVariables.cast_lyricsCommentColor = preferences.getMyPreferenceInt(c,"dark_lyricsCommentColor",StaticVariables.vdarkgreen);
                StaticVariables.cast_lyricsCustomColor = preferences.getMyPreferenceInt(c,"dark_lyricsCustomColor",StaticVariables.vdarkyellow);
                break;
            case "light":
                StaticVariables.cast_lyricsCapoColor = preferences.getMyPreferenceInt(c, "light_lyricsCapoColor", StaticVariables.red);
                StaticVariables.cast_lyricsChordsColor = preferences.getMyPreferenceInt(c, "light_lyricsChordsColor", StaticVariables.yellow);
                StaticVariables.cast_presoFontColor = preferences.getMyPreferenceInt(c, "light_presoFontColor", StaticVariables.black);
                StaticVariables.cast_lyricsBackgroundColor = preferences.getMyPreferenceInt(c,"light_lyricsBackgroundColor",StaticVariables.white);
                StaticVariables.cast_lyricsTextColor = preferences.getMyPreferenceInt(c,"light_lyricsTextColor",StaticVariables.black);
                StaticVariables.cast_presoInfoColor = preferences.getMyPreferenceInt(c,"light_presoInfoColor", StaticVariables.black);
                StaticVariables.cast_presoAlertColor = preferences.getMyPreferenceInt(c,"light_presoAlertColor",StaticVariables.red);
                StaticVariables.cast_presoShadowColor = preferences.getMyPreferenceInt(c,"light_presoShadowColor",StaticVariables.black);
                StaticVariables.cast_lyricsVerseColor = preferences.getMyPreferenceInt(c,"light_lyricsVerseColor",StaticVariables.white);
                StaticVariables.cast_lyricsChorusColor = preferences.getMyPreferenceInt(c,"light_lyricsChorusColor",StaticVariables.vlightpurple);
                StaticVariables.cast_lyricsPreChorusColor = preferences.getMyPreferenceInt(c,"light_lyricsPreChorusColor",StaticVariables.lightgreen);
                StaticVariables.cast_lyricsBridgeColor = preferences.getMyPreferenceInt(c,"light_lyricsBridgeColor",StaticVariables.vlightcyan);
                StaticVariables.cast_lyricsTagColor = preferences.getMyPreferenceInt(c,"light_lyricsTagColor",StaticVariables.vlightgreen);
                StaticVariables.cast_lyricsCommentColor = preferences.getMyPreferenceInt(c,"light_lyricsCommentColor",StaticVariables.vlightblue);
                StaticVariables.cast_lyricsCustomColor = preferences.getMyPreferenceInt(c,"light_lyricsCustomColor",StaticVariables.lightishcyan);
                break;
            case "custom1":
                StaticVariables.cast_lyricsCapoColor = preferences.getMyPreferenceInt(c, "custom1_lyricsCapoColor", StaticVariables.red);
                StaticVariables.cast_lyricsChordsColor = preferences.getMyPreferenceInt(c, "custom1_lyricsChordsColor", StaticVariables.yellow);
                StaticVariables.cast_presoFontColor = preferences.getMyPreferenceInt(c, "custom1_presoFontColor", StaticVariables.white);
                StaticVariables.cast_lyricsBackgroundColor = preferences.getMyPreferenceInt(c,"custom1_lyricsBackgroundColor",StaticVariables.black);
                StaticVariables.cast_lyricsTextColor = preferences.getMyPreferenceInt(c,"custom1_lyricsTextColor",StaticVariables.white);
                StaticVariables.cast_presoInfoColor = preferences.getMyPreferenceInt(c,"custom1_presoInfoColor", StaticVariables.white);
                StaticVariables.cast_presoAlertColor = preferences.getMyPreferenceInt(c,"custom1_presoAlertColor",StaticVariables.red);
                StaticVariables.cast_presoShadowColor = preferences.getMyPreferenceInt(c,"custom1_presoShadowColor",StaticVariables.black);
                StaticVariables.cast_lyricsVerseColor = preferences.getMyPreferenceInt(c,"custom1_lyricsVerseColor",StaticVariables.black);
                StaticVariables.cast_lyricsChorusColor = preferences.getMyPreferenceInt(c,"custom1_lyricsChorusColor",StaticVariables.black);
                StaticVariables.cast_lyricsPreChorusColor = preferences.getMyPreferenceInt(c,"custom1_lyricsPreChorusColor",StaticVariables.black);
                StaticVariables.cast_lyricsBridgeColor = preferences.getMyPreferenceInt(c,"custom1_lyricsBridgeColor",StaticVariables.black);
                StaticVariables.cast_lyricsTagColor = preferences.getMyPreferenceInt(c,"custom1_lyricsTagColor",StaticVariables.black);
                StaticVariables.cast_lyricsCommentColor = preferences.getMyPreferenceInt(c,"custom1_lyricsCommentColor",StaticVariables.black);
                StaticVariables.cast_lyricsCustomColor = preferences.getMyPreferenceInt(c,"custom1_lyricsCustomColor",StaticVariables.black);
                break;
            case "custom2":
                StaticVariables.cast_lyricsCapoColor = preferences.getMyPreferenceInt(c, "custom2_lyricsCapoColor", StaticVariables.red);
                StaticVariables.cast_lyricsChordsColor = preferences.getMyPreferenceInt(c, "custom2_lyricsChordsColor", StaticVariables.yellow);
                StaticVariables.cast_presoFontColor = preferences.getMyPreferenceInt(c, "custom2_presoFontColor", StaticVariables.black);
                StaticVariables.cast_lyricsBackgroundColor = preferences.getMyPreferenceInt(c,"custom2_lyricsBackgroundColor",StaticVariables.white);
                StaticVariables.cast_lyricsTextColor = preferences.getMyPreferenceInt(c,"custom2_lyricsTextColor",StaticVariables.black);
                StaticVariables.cast_presoInfoColor = preferences.getMyPreferenceInt(c,"custom2_presoInfoColor", StaticVariables.black);
                StaticVariables.cast_presoAlertColor = preferences.getMyPreferenceInt(c,"custom2_presoAlertColor",StaticVariables.red);
                StaticVariables.cast_presoShadowColor = preferences.getMyPreferenceInt(c,"custom2_presoShadowColor",StaticVariables.black);
                StaticVariables.cast_lyricsVerseColor = preferences.getMyPreferenceInt(c,"custom2_lyricsVerseColor",StaticVariables.white);
                StaticVariables.cast_lyricsChorusColor = preferences.getMyPreferenceInt(c,"custom2_lyricsChorusColor",StaticVariables.white);
                StaticVariables.cast_lyricsPreChorusColor = preferences.getMyPreferenceInt(c,"custom2_lyricsPreChorusColor",StaticVariables.white);
                StaticVariables.cast_lyricsBridgeColor = preferences.getMyPreferenceInt(c,"custom2_lyricsBridgeColor",StaticVariables.white);
                StaticVariables.cast_lyricsTagColor = preferences.getMyPreferenceInt(c,"custom2_lyricsTagColor",StaticVariables.white);
                StaticVariables.cast_lyricsCommentColor = preferences.getMyPreferenceInt(c,"custom2_lyricsCommentColor",StaticVariables.white);
                StaticVariables.cast_lyricsCustomColor = preferences.getMyPreferenceInt(c,"custom2_lyricsCustomColor",StaticVariables.white);
                break;
        }
    }
    void updateAlpha(Context c, _Preferences preferences, ImageView projected_BackgroundImage, SurfaceView projected_SurfaceView) {
        projected_BackgroundImage.setAlpha(preferences.getMyPreferenceFloat(c,"presoBackgroundAlpha",0.8f));
        projected_SurfaceView.setAlpha(preferences.getMyPreferenceFloat(c,"presoBackgroundAlpha",0.8f));
    }
    void normalStartUp(Context c, _Preferences preferences, ImageView projected_Logo) {
        // Animate out the default logo
        _CustomAnimations.faderAnimation(projected_Logo,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);
    }
    void presenterThemeSetUp(Context c, _Preferences preferences, LinearLayout presentermode_bottombit, TextView presentermode_title,
                             TextView presentermode_author, TextView presentermode_copyright, TextView presentermode_alert) {
        // Set the text at the bottom of the page to match the presentation text colour
        presentermode_title.setTypeface(StaticVariables.typefacePresoInfo);
        presentermode_author.setTypeface(StaticVariables.typefacePresoInfo);
        presentermode_copyright.setTypeface(StaticVariables.typefacePresoInfo);
        presentermode_alert.setTypeface(StaticVariables.typefacePresoInfo);
        presentermode_title.setTextColor(StaticVariables.cast_presoInfoColor);
        presentermode_author.setTextColor(StaticVariables.cast_presoInfoColor);
        presentermode_copyright.setTextColor(StaticVariables.cast_presoInfoColor);
        presentermode_alert.setTextColor(StaticVariables.cast_presoAlertColor);
        presentermode_title.setTextSize(preferences.getMyPreferenceFloat(c,"presoTitleTextSize", 14.0f));
        presentermode_author.setTextSize(preferences.getMyPreferenceFloat(c,"presoAuthorTextSize", 12.0f));
        presentermode_copyright.setTextSize(preferences.getMyPreferenceFloat(c,"presoCopyrightTextSize", 12.0f));
        presentermode_alert.setTextSize(preferences.getMyPreferenceFloat(c,"presoAlertTextSize", 12.0f));
        presentermode_title.setShadowLayer(preferences.getMyPreferenceFloat(c,"presoTitleTextSize", 14.0f) / 2.0f, 4, 4, StaticVariables.cast_presoShadowColor);
        presentermode_author.setShadowLayer(preferences.getMyPreferenceFloat(c,"presoAuthorTextSize", 12.0f) / 2.0f, 4, 4, StaticVariables.cast_presoShadowColor);
        presentermode_copyright.setShadowLayer(preferences.getMyPreferenceFloat(c,"presoCopyrightTextSize", 14.0f) / 2.0f, 4, 4, StaticVariables.cast_presoShadowColor);
        presentermode_alert.setShadowLayer(preferences.getMyPreferenceFloat(c,"presoAlertTextSize", 12.0f) / 2.0f, 4, 4, StaticVariables.cast_presoShadowColor);
        presentermode_title.setGravity(preferences.getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        presentermode_author.setGravity(preferences.getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        presentermode_copyright.setGravity(preferences.getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        presentermode_alert.setGravity(preferences.getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        if (PresenterMode.alert_on.equals("Y")) {
            presentermode_alert.setVisibility(View.VISIBLE);
        } else {
            presentermode_alert.setVisibility(View.GONE);
        }
        presentermode_bottombit.setBackgroundColor(ColorUtils.setAlphaComponent(StaticVariables.cast_presoShadowColor,100));
    }
    void presenterStartUp(final Context c, final _Preferences preferences, final StorageAccess storageAccess, final ImageView projected_BackgroundImage,
                          final SurfaceHolder projected_SurfaceHolder, final SurfaceView projected_SurfaceView) {
        // After the fadeout time, set the background and fade in
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Try to set the new background
                fixBackground(c, preferences, storageAccess, projected_BackgroundImage,projected_SurfaceHolder,projected_SurfaceView);

                if (preferences.getMyPreferenceString(c,"backgroundTypeToUse","image").equals("image")) {
                    _CustomAnimations.faderAnimation(projected_BackgroundImage,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);

                } else if (preferences.getMyPreferenceString(c,"backgroundTypeToUse","image").equals("video")) {
                    _CustomAnimations.faderAnimation(projected_SurfaceView,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);
                }
            }
        }, preferences.getMyPreferenceInt(c,"presoTransitionTime",800));
    }



    // The logo stuff, animations and blanking the screen
    void setUpLogo(Context c, _Preferences preferences, StorageAccess storageAccess, ImageView projected_Logo, int availableWidth_1col, int availableScreenHeight) {
        // If the customLogo doesn't exist, use the default one
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int imgwidth = 1024;
        int imgheight = 500;
        float xscale;
        float yscale;
        boolean usingcustom = false;
        Uri customLogo = storageAccess.fixLocalisedUri(c, preferences, preferences.getMyPreferenceString(c, "customLogo", "ost_logo.png"));
        if (storageAccess.uriExists(c, customLogo)) {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                projected_Logo.setImageDrawable(c.getResources().getDrawable(R.drawable.ost_logo, c.getTheme()));
            } else {
                projected_Logo.setImageDrawable(c.getResources().getDrawable(R.drawable.ost_logo));
            }
        }
        if (PresenterMode.logoButton_isSelected) {
            _CustomAnimations.faderAnimation(projected_Logo, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800), true);
        }
    }
    void showLogo(Context c, _Preferences preferences, ImageView projected_ImageView, LinearLayout projected_LinearLayout, RelativeLayout pageHolder,
                  LinearLayout bottom_infobar, ImageView projected_Logo) {
        // Animate out the lyrics if they were visible and animate in the logo
        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
            _CustomAnimations.faderAnimation(projected_ImageView,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);

        } else {
            _CustomAnimations.faderAnimation(projected_LinearLayout,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);
        }

        // If we had a black screen, fade that in
        if (pageHolder.getVisibility() == View.INVISIBLE) {
            _CustomAnimations.faderAnimation(pageHolder,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);

        }
        _CustomAnimations.faderAnimation(bottom_infobar,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);
        _CustomAnimations.faderAnimation(projected_Logo,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);
    }
    void hideLogo(Context c, _Preferences preferences, ImageView projected_ImageView, LinearLayout projected_LinearLayout, ImageView projected_Logo,
                  LinearLayout bottom_infobar) {
        // Animate out the logo and animate in the lyrics if they were visible
        // Animate out the lyrics if they were visible and animate in the logo
        if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
            _CustomAnimations.faderAnimation(projected_ImageView,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);
        } else {
            _CustomAnimations.faderAnimation(projected_LinearLayout,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);
        }
        _CustomAnimations.faderAnimation(projected_Logo,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);
        _CustomAnimations.faderAnimation(bottom_infobar,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);
    }
    void blankUnblankDisplay(Context c, _Preferences preferences, RelativeLayout pageHolder, boolean unblank) {
        _CustomAnimations.faderAnimation(pageHolder,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),unblank);
    }
    private void animateIn(Context c, _Preferences preferences, ImageView projected_ImageView, LinearLayout projected_LinearLayout) {
        // Fade in the main page
        if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
            _CustomAnimations.faderAnimation(projected_ImageView,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);

        } else {
            _CustomAnimations.faderAnimation(projected_LinearLayout,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);
        }
    }
    private void animateOut(Context c, _Preferences preferences, Display myscreen, ImageView projected_Logo, ImageView projected_ImageView,
                            LinearLayout projected_LinearLayout, LinearLayout bottom_infobar, RelativeLayout projectedPage_RelativeLayout) {
        // If the logo is showing, fade it away
        if (projected_Logo.getAlpha() > 0.0f) {
            _CustomAnimations.faderAnimation(projected_Logo,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);
        }
        // Fade in the main page
        if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
            _CustomAnimations.faderAnimation(projected_ImageView,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);
        } else {
            _CustomAnimations.faderAnimation(projected_LinearLayout,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);
        }
        getScreenSizes(myscreen,bottom_infobar, projectedPage_RelativeLayout, preferences.getMyPreferenceFloat(c,"castRotation",0.0f));  // Just in case something changed
    }
    private void presenterFadeOutSongInfo(final Context c, final _Preferences preferences, final TextView tv, final String s, final LinearLayout bottom_infobar) {
        if (tv.getAlpha() > 0.0f) {
            _CustomAnimations.faderAnimation(tv,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);
        }
        // After the transition time, change the text and fade it back in
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv.setText(s);
                // If this is a pdf or image, hide the song info
                if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
                    bottom_infobar.setVisibility(View.GONE);
                } else {
                    bottom_infobar.setVisibility(View.VISIBLE);
                    _CustomAnimations.faderAnimation(tv,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);
                }
            }
        }, preferences.getMyPreferenceInt(c,"presoTransitionTime",800));
    }


    // Update the screen content
    void doUpdate(final Context c, final _Preferences preferences, final StorageAccess storageAccess, final ProcessSong processSong,
                  final Display myscreen, final TextView songinfo_TextView, LinearLayout presentermode_bottombit, final SurfaceView projected_SurfaceView,
                  ImageView projected_BackgroundImage, RelativeLayout pageHolder, ImageView projected_Logo, final ImageView projected_ImageView,
                  final LinearLayout projected_LinearLayout, LinearLayout bottom_infobar, final RelativeLayout projectedPage_RelativeLayout,
                  TextView presentermode_title, TextView presentermode_author, TextView presentermode_copyright,
                  final LinearLayout col1_1, final LinearLayout col1_2, final LinearLayout col2_2, final LinearLayout col1_3,
                  final LinearLayout col2_3, final LinearLayout col3_3) {
        // First up, animate everything away
        animateOut(c,preferences,myscreen,projected_Logo,projected_ImageView,projected_LinearLayout,bottom_infobar,projectedPage_RelativeLayout);

        // If we have forced an update due to switching modes, set that up
        if (StaticVariables.forcecastupdate) {
            matchPresentationToMode(songinfo_TextView, presentermode_bottombit, projected_SurfaceView, projected_BackgroundImage, projected_ImageView);
        }

        // If we had a black screen, fade that in
        if (pageHolder.getVisibility() == View.INVISIBLE) {
            _CustomAnimations.faderAnimation(pageHolder,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);

        }

        // Just in case there is a glitch, make the stuff visible after 5x transition time
        Handler panic = new Handler();
        panic.postDelayed(new Runnable() {
            @Override
            public void run() {
                panicShowViews(projected_ImageView,projected_LinearLayout,projected_SurfaceView);
            }
        }, 5 * preferences.getMyPreferenceInt(c,"presoTransitionTime",800));

        // Set the title of the song and author (if available).  Only does this for changes
        if (StaticVariables.whichMode.equals("Presentation")) {
            presenterWriteSongInfo(c,preferences,presentermode_title,presentermode_author,presentermode_copyright,bottom_infobar);
        } else {
            setSongTitle(c,preferences,songinfo_TextView);
        }

        // Now run the next bit post delayed (to wait for the animate out)
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Wipe any current views
                wipeAllViews(projected_LinearLayout,projected_ImageView);

                // Check the colours colour
                if (!StaticVariables.whichMode.equals("Presentation")) {
                    // Set the page background to the correct colour for Peformance/Stage modes
                    projectedPage_RelativeLayout.setBackgroundColor(StaticVariables.cast_lyricsBackgroundColor);
                    songinfo_TextView.setTextColor(StaticVariables.cast_presoInfoColor);
                }

                // Decide on what we are going to show
                if (FullscreenActivity.isPDF) {
                    doPDFPage(c,preferences,storageAccess,processSong,projected_ImageView,projected_LinearLayout);
                } else if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide) {
                    doImagePage(c,preferences,storageAccess,projected_ImageView,projected_LinearLayout);
                } else {
                    projected_ImageView.setVisibility(View.GONE);
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
        }, preferences.getMyPreferenceInt(c,"presoTransitionTime",800));
    }
    private void panicShowViews(ImageView projected_ImageView, LinearLayout projected_LinearLayout, SurfaceView projected_SurfaceView) {
        // After 3x the transition times, make sure the correct view is visible regardless of animations
        if (StaticVariables.whichMode.equals("Presentation")) {
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
    }
    private void presenterWriteSongInfo(Context c, _Preferences preferences, TextView presentermode_title, TextView presentermode_author,
                                        TextView presentermode_copyright, LinearLayout bottom_infobar) {
        String old_title = presentermode_title.getText().toString();
        String old_author = presentermode_author.getText().toString();
        String old_copyright = presentermode_copyright.getText().toString();
        if (!old_title.contentEquals(StaticVariables.mTitle)) {
            presenterFadeOutSongInfo(c, preferences, presentermode_title, StaticVariables.mTitle, bottom_infobar);
        }
        if (!old_author.contentEquals(StaticVariables.mAuthor)) {
            presenterFadeOutSongInfo(c, preferences, presentermode_author, StaticVariables.mAuthor, bottom_infobar);
        }
        if (!old_copyright.contentEquals(StaticVariables.mCopyright)) {
            presenterFadeOutSongInfo(c, preferences, presentermode_copyright, StaticVariables.mCopyright, bottom_infobar);
        }
    }
    private void setSongTitle(Context c, _Preferences preferences, TextView songinfo_TextView) {
        String old_title = songinfo_TextView.getText().toString();
        String new_title = StaticVariables.mTitle;
        if (!StaticVariables.mAuthor.equals("")) {
            new_title = new_title + "\n" + StaticVariables.mAuthor;
        }
        if (!old_title.equals(new_title)) {
            // It has changed, so make the text update on the screen
            normalChangeSongInfo(c,preferences,songinfo_TextView,new_title);
        }
    }
    private void doPDFPage(Context c, _Preferences preferences, StorageAccess storageAccess, ProcessSong processSong, ImageView projected_ImageView, LinearLayout projected_LinearLayout) {
        Bitmap bmp = processSong.createPDFPage(c, preferences, storageAccess, StaticVariables.cast_availableScreenWidth, StaticVariables.cast_availableScreenHeight, "Y");
        projected_ImageView.setBackgroundColor(StaticVariables.white);
        projected_ImageView.setImageBitmap(bmp);
        animateIn(c,preferences,projected_ImageView,projected_LinearLayout);
    }
    private void doImagePage(Context c, _Preferences preferences, StorageAccess storageAccess, ImageView projected_ImageView, LinearLayout projected_LinearLayout) {
        projected_ImageView.setVisibility(View.GONE);
        projected_ImageView.setBackgroundColor(0x00000000);
        // Process the image location into an URI
        Uri imageUri;
        if (StaticVariables.uriToLoad==null) {
            imageUri = storageAccess.getUriForItem(c, preferences, "Songs", StaticVariables.whichSongFolder, StaticVariables.songfilename);
        } else {
            imageUri = StaticVariables.uriToLoad;
        }
        RequestOptions myOptions = new RequestOptions()
                .fitCenter().override(projected_LinearLayout.getMeasuredWidth(),projected_LinearLayout.getMeasuredHeight());
        GlideApp.with(c).load(imageUri).apply(myOptions).into(projected_ImageView);
        animateIn(c, preferences,projected_ImageView,projected_LinearLayout);
    }
    private void wipeAllViews(LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        projected_LinearLayout.removeAllViews();
        projected_ImageView.setImageBitmap(null);
    }
    private void normalChangeSongInfo(final Context c, final _Preferences preferences, final TextView songinfo_TextView, final String s) {
        _CustomAnimations.faderAnimation(songinfo_TextView,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);

        // After the transition delay, write the new value and fade it back in
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                songinfo_TextView.setTextColor(StaticVariables.cast_presoInfoColor);
                songinfo_TextView.setText(s);
                _CustomAnimations.faderAnimation(songinfo_TextView,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);

            }
        }, preferences.getMyPreferenceInt(c,"presoTransitionTime",800));
    }



    // Alert
    void updateAlert(Context c, _Preferences preferences, Display myscreen, LinearLayout bottom_infobar, RelativeLayout projectedPage_RelativeLayout, boolean show, TextView presentermode_alert) {
        if (show) {
            PresenterMode.alert_on = "Y";
            fadeinAlert(c, preferences, myscreen, bottom_infobar, projectedPage_RelativeLayout, presentermode_alert);
        } else {
            PresenterMode.alert_on = "N";
            fadeoutAlert(c, preferences, presentermode_alert);
        }
    }
    private void fadeinAlert(Context c, _Preferences preferences, Display myscreen, LinearLayout bottom_infobar, RelativeLayout projectedPage_RelativeLayout, TextView presentermode_alert) {
        presentermode_alert.setText(preferences.getMyPreferenceString(c,"presoAlertText",""));
        presentermode_alert.setTypeface(StaticVariables.typefacePresoInfo);
        presentermode_alert.setTextSize(preferences.getMyPreferenceFloat(c,"presoAlertTextSize", 12.0f));
        presentermode_alert.setTextColor(StaticVariables.cast_presoAlertColor);
        presentermode_alert.setShadowLayer(preferences.getMyPreferenceFloat(c,"presoAlertTextSize", 12.0f) / 2.0f, 4, 4, StaticVariables.cast_presoShadowColor);

        presentermode_alert.setVisibility(View.VISIBLE);
        getScreenSizes(myscreen,bottom_infobar,projectedPage_RelativeLayout,preferences.getMyPreferenceFloat(c,"castRotation",0.0f));
        _CustomAnimations.faderAnimation(presentermode_alert,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),true);
    }
    private void fadeoutAlert(Context c, _Preferences preferences, TextView presentermode_alert) {
        _CustomAnimations.faderAnimation(presentermode_alert,preferences.getMyPreferenceInt(c,"presoTransitionTime",800),false);
    }




    // MediaPlayer stuff
    void prepareMediaPlayer(Context c, _Preferences preferences, SurfaceHolder projected_SurfaceHolder, Display myscreen, LinearLayout bottom_infobar, RelativeLayout projectedPage_RelativeLayout) {
        // Get the size of the SurfaceView
        getScreenSizes(myscreen,bottom_infobar,projectedPage_RelativeLayout,preferences.getMyPreferenceFloat(c,"castRotation",0.0f));
        StaticVariables.cast_mediaPlayer = new MediaPlayer();
        StaticVariables.cast_mediaPlayer.setDisplay(projected_SurfaceHolder);
        if (preferences.getMyPreferenceString(c,"backgroundTypeToUse","image").equals("video")) {
            try {
                StaticVariables.cast_mediaPlayer.setDataSource(c, StaticVariables.cast_vidUri);
                StaticVariables.cast_mediaPlayer.prepare();

            } catch (Exception e) {
                Log.d("PresentationService", "Error setting data source for video");
            }
        }
    }
    void mediaPlayerIsPrepared(SurfaceView projected_SurfaceView) {
        try {
            // Get the video sizes so we can scale appropriately
            int width = StaticVariables.cast_mediaPlayer.getVideoWidth();
            int height = StaticVariables.cast_mediaPlayer.getVideoHeight();
            float max_xscale = (float) StaticVariables.cast_screenWidth / (float) width;
            float max_yscale = (float) StaticVariables.cast_screenHeight / (float) height;
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
            StaticVariables.cast_mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void reloadVideo(final Context c, final _Preferences preferences, final SurfaceHolder projected_SurfaceHolder, final SurfaceView projected_SurfaceView) {
        if (StaticVariables.cast_mediaPlayer == null) {
            StaticVariables.cast_mediaPlayer = new MediaPlayer();
            try {
                StaticVariables.cast_mediaPlayer.setDisplay(projected_SurfaceHolder);
                StaticVariables.cast_mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            StaticVariables.cast_mediaPlayer.reset();
        } catch (Exception e) {
            Log.d("PresentationService", "Error resetting mMediaPlayer");
        }

        if (preferences.getMyPreferenceString(c,"backgroundTypeToUse","image").equals("video")) {
            try {
                Log.d("Presemttion Common","cast_viUri="+StaticVariables.cast_vidUri);
                StaticVariables.cast_mediaPlayer.setDataSource(c, StaticVariables.cast_vidUri);
                StaticVariables.cast_mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        try {
                            // Get the video sizes so we can scale appropriately
                            int width = mp.getVideoWidth();
                            int height = mp.getVideoHeight();
                            float max_xscale = (float) StaticVariables.cast_screenWidth / (float) width;
                            float max_yscale = (float) StaticVariables.cast_screenHeight / (float) height;
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
                    }
                });
                StaticVariables.cast_mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
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
                    }
                });
                StaticVariables.cast_mediaPlayer.prepare();

            } catch (Exception e) {
                Log.d("PresentationService", "Error setting data source for video");
            }
        }
    }




    // Writing the views for PerformanceMode
    private void prepareFullProjected (final Context c, final _Preferences preferences, final ProcessSong processSong, final StorageAccess storageAccess,
                                       final LinearLayout col1_1, final LinearLayout col1_2, final LinearLayout col2_2, final LinearLayout col1_3,
                                       final LinearLayout col2_3, final LinearLayout col3_3, final LinearLayout projected_LinearLayout, final ImageView projected_ImageView) {
        if (StaticVariables.activity!=null) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    // Updating views on the UI
                    //activity.runOnUiThread(new Runnable() {
                    StaticVariables.activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                                        storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                                        StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                                col1_1.addView(test1_1);

                                if (x < FullscreenActivity.halfsplit_section) {
                                    test1_2 = processSong.projectedSectionView(c, x, 12.0f,
                                            storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                                            StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                                    col1_2.addView(test1_2);
                                } else {
                                    test2_2 = processSong.projectedSectionView(c, x, 12.0f,
                                            storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                                            StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                                    col2_2.addView(test2_2);
                                }

                                if (x < FullscreenActivity.thirdsplit_section) {
                                    test1_3 = processSong.projectedSectionView(c, x, 12.0f,
                                            storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                                            StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                                    col1_3.addView(test1_3);
                                } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                                    test2_3 = processSong.projectedSectionView(c, x, 12.0f,
                                            storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                                            StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                                    col2_3.addView(test2_3);
                                } else {
                                    test3_3 = processSong.projectedSectionView(c, x, 12.0f,
                                            storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                                            StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
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
                            float maxwidth_scale1_1 = ((float) StaticVariables.cast_availableWidth_1col) / (float) widthofsection1_1;
                            float maxwidth_scale1_2 = ((float) StaticVariables.cast_availableWidth_2col) / (float) widthofsection1_2;
                            float maxwidth_scale2_2 = ((float) StaticVariables.cast_availableWidth_2col) / (float) widthofsection2_2;
                            float maxwidth_scale1_3 = ((float) StaticVariables.cast_availableWidth_3col) / (float) widthofsection1_3;
                            float maxwidth_scale2_3 = ((float) StaticVariables.cast_availableWidth_3col) / (float) widthofsection2_3;
                            float maxwidth_scale3_3 = ((float) StaticVariables.cast_availableWidth_3col) / (float) widthofsection3_3;
                            float maxheight_scale1_1 = ((float) StaticVariables.cast_availableScreenHeight) / (float) heightofsection1_1;
                            float maxheight_scale1_2 = ((float) StaticVariables.cast_availableScreenHeight) / (float) heightofsection1_2;
                            float maxheight_scale2_2 = ((float) StaticVariables.cast_availableScreenHeight) / (float) heightofsection2_2;
                            float maxheight_scale1_3 = ((float) StaticVariables.cast_availableScreenHeight) / (float) heightofsection1_3;
                            float maxheight_scale2_3 = ((float) StaticVariables.cast_availableScreenHeight) / (float) heightofsection2_3;
                            float maxheight_scale3_3 = ((float) StaticVariables.cast_availableScreenHeight) / (float) heightofsection3_3;

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
                        }
                    });
                }
            }).start();
        }
    }
    private void projectedPerformanceView1col(Context c, _Preferences preferences, StorageAccess storageAccess, ProcessSong processSong,
                                              float scale1_1, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {
            LinearLayout lyrics1_1 = processSong.createLinearLayout(c);
            LinearLayout box1_1 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.cast_lyricsTextColor,
                    StaticVariables.cast_lyricsBackgroundColor, StaticVariables.cast_padding);
            float fontsize1_1 = processSong.getProjectedFontSize(scale1_1);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_1.setPadding(0, 0, 0, 0);

            // Prepare the new views to add to 1,2 and 3 colums ready for measuring
            // Go through each section
            for (int x = 0; x < StaticVariables.songSections.length; x++) {
                lyrics1_1 = processSong.projectedSectionView(c, x, fontsize1_1,
                        storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                        StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(StaticVariables.cast_availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
                llp1_1.setMargins(0, 0, 0, 0);
                lyrics1_1.setLayoutParams(llp1_1);
                lyrics1_1.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],
                        StaticVariables.cast_lyricsVerseColor, StaticVariables.cast_lyricsChorusColor,
                        StaticVariables.cast_lyricsPreChorusColor, StaticVariables.cast_lyricsBridgeColor, StaticVariables.cast_lyricsTagColor,
                        StaticVariables.cast_lyricsCommentColor, StaticVariables.cast_lyricsCustomColor));
                box1_1.addView(lyrics1_1);
            }

            // Now add the display
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(StaticVariables.cast_availableScreenWidth,
                    StaticVariables.cast_availableScreenHeight + StaticVariables.cast_padding);
            llp.setMargins(0, 0, 0, 0);
            projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            box1_1.setLayoutParams(llp);
            projected_LinearLayout.addView(box1_1);
            animateIn(c, preferences, projected_ImageView, projected_LinearLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void projectedPerformanceView2col(Context c, _Preferences preferences, StorageAccess storageAccess, ProcessSong processSong,
                                              float scale1_2, float scale2_2, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {
            LinearLayout lyrics1_2 = processSong.createLinearLayout(c);
            LinearLayout lyrics2_2 = processSong.createLinearLayout(c);
            LinearLayout box1_2 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.cast_lyricsTextColor,
                    StaticVariables.cast_lyricsBackgroundColor, StaticVariables.cast_padding);
            LinearLayout box2_2 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.cast_lyricsTextColor,
                    StaticVariables.cast_lyricsBackgroundColor, StaticVariables.cast_padding);
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
                            storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                            StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                    LinearLayout.LayoutParams llp1_2 = new LinearLayout.LayoutParams(StaticVariables.cast_availableWidth_2col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_2.setMargins(0, 0, 0, 0);
                    lyrics1_2.setLayoutParams(llp1_2);
                    lyrics1_2.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],
                            StaticVariables.cast_lyricsVerseColor,StaticVariables.cast_lyricsChorusColor, StaticVariables.cast_lyricsPreChorusColor,
                            StaticVariables.cast_lyricsBridgeColor,StaticVariables.cast_lyricsTagColor,
                            StaticVariables.cast_lyricsCommentColor,StaticVariables.cast_lyricsCustomColor));
                    box1_2.addView(lyrics1_2);
                } else {
                    lyrics2_2 = processSong.projectedSectionView(c, x, fontsize2_2,
                            storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                            StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                    LinearLayout.LayoutParams llp2_2 = new LinearLayout.LayoutParams(StaticVariables.cast_availableWidth_2col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp2_2.setMargins(0, 0, 0, 0);
                    lyrics2_2.setLayoutParams(llp2_2);
                    lyrics2_2.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],
                            StaticVariables.cast_lyricsVerseColor,StaticVariables.cast_lyricsChorusColor, StaticVariables.cast_lyricsPreChorusColor,
                            StaticVariables.cast_lyricsBridgeColor,StaticVariables.cast_lyricsTagColor,
                            StaticVariables.cast_lyricsCommentColor,StaticVariables.cast_lyricsCustomColor));
                    box2_2.addView(lyrics2_2);
                }
            }

            // Now add the display
            LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(StaticVariables.cast_availableWidth_2col + (StaticVariables.cast_padding * 2), StaticVariables.cast_availableScreenHeight + StaticVariables.cast_padding);
            LinearLayout.LayoutParams llp2 = new LinearLayout.LayoutParams(StaticVariables.cast_availableWidth_2col + (StaticVariables.cast_padding * 2), StaticVariables.cast_availableScreenHeight + StaticVariables.cast_padding);
            llp1.setMargins(0, 0, StaticVariables.cast_padding, 0);
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
    private void projectedPerformanceView3col(Context c, _Preferences preferences, StorageAccess storageAccess, ProcessSong processSong,
                                              float scale1_3, float scale2_3, float scale3_3, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {
            LinearLayout lyrics1_3 = processSong.createLinearLayout(c);
            LinearLayout lyrics2_3 = processSong.createLinearLayout(c);
            LinearLayout lyrics3_3 = processSong.createLinearLayout(c);
            LinearLayout box1_3 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.cast_lyricsTextColor,
                    StaticVariables.cast_lyricsBackgroundColor, StaticVariables.cast_padding);
            LinearLayout box2_3 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.cast_lyricsTextColor,
                    StaticVariables.cast_lyricsBackgroundColor, StaticVariables.cast_padding);
            LinearLayout box3_3 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.cast_lyricsTextColor,
                    StaticVariables.cast_lyricsBackgroundColor, StaticVariables.cast_padding);
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
                            storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                            StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                    LinearLayout.LayoutParams llp1_3 = new LinearLayout.LayoutParams(StaticVariables.cast_availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_3.setMargins(0, 0, 0, 0);
                    lyrics1_3.setLayoutParams(llp1_3);
                    lyrics1_3.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],
                            StaticVariables.cast_lyricsVerseColor,StaticVariables.cast_lyricsChorusColor, StaticVariables.cast_lyricsPreChorusColor,
                            StaticVariables.cast_lyricsBridgeColor,StaticVariables.cast_lyricsTagColor,
                            StaticVariables.cast_lyricsCommentColor,StaticVariables.cast_lyricsCustomColor));
                    box1_3.addView(lyrics1_3);
                } else if (x >= FullscreenActivity.thirdsplit_section && x < FullscreenActivity.twothirdsplit_section) {
                    lyrics2_3 = processSong.projectedSectionView(c, x, fontsize2_3,
                            storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                            StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                    LinearLayout.LayoutParams llp2_3 = new LinearLayout.LayoutParams(StaticVariables.cast_availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp2_3.setMargins(0, 0, 0, 0);
                    lyrics2_3.setLayoutParams(llp2_3);
                    lyrics2_3.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],
                            StaticVariables.cast_lyricsVerseColor,StaticVariables.cast_lyricsChorusColor, StaticVariables.cast_lyricsPreChorusColor,
                            StaticVariables.cast_lyricsBridgeColor,StaticVariables.cast_lyricsTagColor,
                            StaticVariables.cast_lyricsCommentColor,StaticVariables.cast_lyricsCustomColor));
                    box2_3.addView(lyrics2_3);
                } else {
                    lyrics3_3 = processSong.projectedSectionView(c, x, fontsize3_3,
                            storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                            StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                    LinearLayout.LayoutParams llp3_3 = new LinearLayout.LayoutParams(StaticVariables.cast_availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp3_3.setMargins(0, 0, 0, 0);
                    lyrics3_3.setLayoutParams(llp3_3);
                    lyrics3_3.setBackgroundColor(processSong.getSectionColors(StaticVariables.songSectionsTypes[x],
                            StaticVariables.cast_lyricsVerseColor,StaticVariables.cast_lyricsChorusColor, StaticVariables.cast_lyricsPreChorusColor,
                            StaticVariables.cast_lyricsBridgeColor,StaticVariables.cast_lyricsTagColor,
                            StaticVariables.cast_lyricsCommentColor,StaticVariables.cast_lyricsCustomColor));
                    box3_3.addView(lyrics3_3);
                }
            }

            // Now add the display
            LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(StaticVariables.cast_availableWidth_3col + (StaticVariables.cast_padding * 2), StaticVariables.cast_availableScreenHeight + StaticVariables.cast_padding);
            LinearLayout.LayoutParams llp3 = new LinearLayout.LayoutParams(StaticVariables.cast_availableWidth_3col + (StaticVariables.cast_padding * 2), StaticVariables.cast_availableScreenHeight + StaticVariables.cast_padding);
            llp1.setMargins(0, 0, StaticVariables.cast_padding, 0);
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
    private void prepareStageProjected(final Context c, final _Preferences preferences, final ProcessSong processSong, final StorageAccess storageAccess,
                                       final LinearLayout col1_1, final LinearLayout col1_2, final LinearLayout col2_2, final LinearLayout col1_3,
                                       final LinearLayout col2_3, final LinearLayout col3_3, final LinearLayout projected_LinearLayout, final ImageView projected_ImageView) {

        if (StaticVariables.activity!=null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Updating views on the UI
                        //activity.runOnUiThread(new Runnable() {
                        StaticVariables.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
                                        storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                                        StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                                col1_1.addView(test1_1);

                                // Now premeasure the views
                                col1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                                // Get the widths and heights of the sections
                                int widthofsection1_1 = col1_1.getMeasuredWidth();
                                int heightofsection1_1 = col1_1.getMeasuredHeight();

                                // We know the size of each section, so we just need to know which one to display
                                float maxwidth_scale1_1 = ((float) StaticVariables.cast_availableWidth_1col) / (float) widthofsection1_1;
                                float maxheight_scale1_1 = ((float) StaticVariables.cast_availableScreenHeight) / (float) heightofsection1_1;

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
                            }

                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    private void projectedStageView1Col(Context c, _Preferences preferences, StorageAccess storageAccess, ProcessSong processSong,
                                        float scale1_1, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {

            LinearLayout lyrics1_1 = processSong.createLinearLayout(c);
            LinearLayout box1_1 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.cast_lyricsTextColor,
                    StaticVariables.cast_lyricsBackgroundColor, StaticVariables.cast_padding);
            float fontsize1_1 = processSong.getProjectedFontSize(scale1_1);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_1.setPadding(0, 0, 0, 0);

            // Add this section
            lyrics1_1 = processSong.projectedSectionView(c, StaticVariables.currentSection,
                    fontsize1_1,
                    storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                    StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
            LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(StaticVariables.cast_availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp1_1.setMargins(0, 0, 0, 0);
            lyrics1_1.setLayoutParams(llp1_1);
            box1_1.addView(lyrics1_1);


            // Now add the display
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(StaticVariables.cast_availableScreenWidth,
                    StaticVariables.cast_availableScreenHeight + StaticVariables.cast_padding);
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
    private void preparePresenterProjected(final Context c, final _Preferences preferences, final ProcessSong processSong, final StorageAccess storageAccess,
                                           final LinearLayout col1_1, final LinearLayout col1_2, final LinearLayout col2_2, final LinearLayout col1_3,
                                           final LinearLayout col2_3, final LinearLayout col3_3, final LinearLayout projected_LinearLayout, final ImageView projected_ImageView) {
        if (StaticVariables.activity != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //activity.runOnUiThread(new Runnable() {
                        StaticVariables.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
                                        storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                                        StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
                                col1_1.addView(test1_1);

                                // Now premeasure the views
                                col1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                                // Get the widths and heights of the sections
                                int widthofsection1_1 = col1_1.getMeasuredWidth();
                                int heightofsection1_1 = col1_1.getMeasuredHeight();

                                // We know the size of each section, so we just need to know which one to display
                                float maxwidth_scale1_1 = ((float) StaticVariables.cast_availableWidth_1col) / (float) widthofsection1_1;
                                float maxheight_scale1_1 = ((float) StaticVariables.cast_availableScreenHeight) / (float) heightofsection1_1;

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

                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    private void projectedPresenterView1Col(Context c, _Preferences preferences, StorageAccess storageAccess, ProcessSong processSong,
                                            float scale1_1, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {
            LinearLayout lyrics1_1 = processSong.createLinearLayout(c);
            LinearLayout box1_1 = processSong.prepareProjectedBoxView(c, preferences, StaticVariables.cast_lyricsTextColor,
                    StaticVariables.cast_lyricsBackgroundColor, StaticVariables.cast_padding);
            float fontsize1_1 = processSong.getProjectedFontSize(scale1_1);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_1.setPadding(0, 0, 0, 0);

            // Add this section
            lyrics1_1 = processSong.projectedSectionView(c, StaticVariables.currentSection,
                    fontsize1_1,
                    storageAccess, preferences,StaticVariables.cast_lyricsTextColor, StaticVariables.cast_lyricsChordsColor,
                    StaticVariables.cast_lyricsCapoColor,StaticVariables.cast_presoFontColor,StaticVariables.cast_presoShadowColor);
            LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(StaticVariables.cast_availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp1_1.setMargins(0, 0, 0, 0);
            lyrics1_1.setLayoutParams(llp1_1);
            box1_1.addView(lyrics1_1);

            // Now add the display
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(StaticVariables.cast_availableScreenWidth,
                    StaticVariables.cast_availableScreenHeight + StaticVariables.cast_padding);
            llp.setMargins(0, 0, 0, 0);
            projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            box1_1.setLayoutParams(llp);
            projected_LinearLayout.addView(box1_1);
            animateIn(c, preferences, projected_ImageView, projected_LinearLayout);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
*/

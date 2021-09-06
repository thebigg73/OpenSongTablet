package com.garethevans.church.opensongtablet.secondarydisplay;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;

public class PresentationCommon {
    
    // Default variables
    private int cast_padding, cast_screenWidth, cast_availableScreenWidth, cast_screenHeight, cast_availableScreenHeight,
            cast_availableWidth_1col, cast_availableWidth_2col, cast_availableWidth_3col,
            cast_lyricsCapoColor, cast_lyricsChordsColor, cast_presoFontColor, cast_lyricsBackgroundColor,
            cast_lyricsTextColor, cast_presoInfoColor, cast_presoAlertColor, cast_presoShadowColor, cast_lyricsVerseColor,
            cast_lyricsChorusColor, cast_lyricsPreChorusColor, cast_lyricsBridgeColor, cast_lyricsTagColor,
            cast_lyricsCommentColor, cast_lyricsCustomColor, infoBarChangeDelay, lyricDelay, panicDelay;
    private long infoBarUntilTime, lyricAfterTime, panicAfterTime;
    private Drawable cast_defimage;
    boolean infoBarChangeRequired, forceCastUpdate, showLogoActive, panicRequired, blankActive, alert_on,
            doUpdateActive, infoBarAlertState, animateOutActive, isVideo;
    MediaPlayer cast_mediaPlayer;
    Uri cast_vidUri, uriToLoad;



    /*void normalStartUp(Context c, MainActivityInterface mainActivityInterface, ImageView projected_Logo) {
        // Animate out the default logo
        mainActivityInterface.getCustomAnimation().faderAnimation(projected_Logo,
                mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoTransitionTime",800),false);
    }

    // The screen and layout defaults starting the projected display
    void getScreenSizes(Display myscreen, LinearLayout bottom_infobar, RelativeLayout projectedPage_RelativeLayout, float rotation) {
        DisplayMetrics metrics = new DisplayMetrics();
        myscreen.getRealMetrics(metrics);
        int bottombarheight = 0;
        // IV - Use infobar height (was previously icon height)
        if (bottom_infobar.getVisibility() == View.VISIBLE) {
            bottombarheight = bottombarheight + bottom_infobar.getMeasuredHeight();
        }

        cast_padding = 8;
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

        cast_screenWidth = newWidth;
        cast_screenHeight = newHeight;

        projectedPage_RelativeLayout.setTranslationX((originalWidth - newWidth)/2.0f);
        projectedPage_RelativeLayout.setTranslationY((originalHeight - newHeight)/2.0f);
        lp.height = cast_screenHeight;
        lp.width = cast_screenWidth;
        projectedPage_RelativeLayout.requestLayout();

        cast_availableScreenWidth = cast_screenWidth - leftpadding - rightpadding - (cast_padding * 2);
        cast_availableScreenHeight = cast_screenHeight - toppadding - bottompadding - bottombarheight - (cast_padding * 4);
        cast_availableWidth_1col = cast_availableScreenWidth - (cast_padding * 2);
        cast_availableWidth_2col = (int) ((float) cast_availableScreenWidth / 2.0f) - (cast_padding * 3);
        cast_availableWidth_3col = (int) ((float) cast_availableScreenWidth / 3.0f) - (cast_padding * 4);
    }
    
    void getDefaultColors(Context c, MainActivityInterface mainActivityInterface, ThemeColors themeColors) {
        switch (mainActivityInterface.getMyThemeColors().getThemeName()) {
            case "dark":
            default:
                cast_lyricsCapoColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsCapoColor", themeColors.getColorInt("red"));
                cast_lyricsChordsColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_lyricsChordsColor", themeColors.getColorInt("yellow"));
                cast_presoFontColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "dark_presoFontColor", themeColors.getColorInt("white"));
                cast_lyricsBackgroundColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"dark_lyricsBackgroundColor", themeColors.getColorInt("black"));
                cast_lyricsTextColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"dark_lyricsTextColor", themeColors.getColorInt("white"));
                cast_presoInfoColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"dark_presoInfoColor", themeColors.getColorInt("white"));
                cast_presoAlertColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"dark_presoAlertColor", themeColors.getColorInt("red"));
                cast_presoShadowColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"dark_presoShadowColor", themeColors.getColorInt("black"));
                cast_lyricsVerseColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"dark_lyricsVerseColor", themeColors.getColorInt("black"));
                cast_lyricsChorusColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"dark_lyricsChorusColor", themeColors.getColorInt("vdarkblue"));
                cast_lyricsPreChorusColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"dark_lyricsPreChorusColor", themeColors.getColorInt("darkishgreen"));
                cast_lyricsBridgeColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"dark_lyricsBridgeColor", themeColors.getColorInt("vdarkred"));
                cast_lyricsTagColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"dark_lyricsTagColor", themeColors.getColorInt("darkpurple"));
                cast_lyricsCommentColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"dark_lyricsCommentColor", themeColors.getColorInt("vdarkgreen"));
                cast_lyricsCustomColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"dark_lyricsCustomColor", themeColors.getColorInt("vdarkyellow"));
                break;
            case "light":
                cast_lyricsCapoColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "light_lyricsCapoColor", themeColors.getColorInt("red"));
                cast_lyricsChordsColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "light_lyricsChordsColor", themeColors.getColorInt("yellow"));
                cast_presoFontColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "light_presoFontColor", themeColors.getColorInt("white"));
                cast_lyricsBackgroundColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsBackgroundColor", themeColors.getColorInt("black"));
                cast_lyricsTextColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsTextColor", themeColors.getColorInt("black"));
                cast_presoInfoColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_presoInfoColor", themeColors.getColorInt("black"));
                cast_presoAlertColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_presoAlertColor", themeColors.getColorInt("red"));
                cast_presoShadowColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_presoShadowColor", themeColors.getColorInt("black"));
                cast_lyricsVerseColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsVerseColor", themeColors.getColorInt("white"));
                cast_lyricsChorusColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsChorusColor", themeColors.getColorInt("vlightpurple"));
                cast_lyricsPreChorusColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsPreChorusColor", themeColors.getColorInt("lightgreen"));
                cast_lyricsBridgeColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsBridgeColor", themeColors.getColorInt("vlightcyan"));
                cast_lyricsTagColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsTagColor", themeColors.getColorInt("vlightgreen"));
                cast_lyricsCommentColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsCommentColor", themeColors.getColorInt("vlightblue"));
                cast_lyricsCustomColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"light_lyricsCustomColor", themeColors.getColorInt("lightishcyan"));
                break;
            case "custom1":
                cast_lyricsCapoColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom1_lyricsCapoColor", themeColors.getColorInt("red"));
                cast_lyricsChordsColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom1_lyricsChordsColor", themeColors.getColorInt("yellow"));
                cast_presoFontColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom1_presoFontColor", themeColors.getColorInt("white"));
                cast_lyricsBackgroundColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsBackgroundColor", themeColors.getColorInt("black"));
                cast_lyricsTextColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsTextColor", themeColors.getColorInt("white"));
                cast_presoInfoColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_presoInfoColor", themeColors.getColorInt("white"));
                cast_presoAlertColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_presoAlertColor", themeColors.getColorInt("red"));
                cast_presoShadowColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_presoShadowColor", themeColors.getColorInt("black"));
                cast_lyricsVerseColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsVerseColor", themeColors.getColorInt("black"));
                cast_lyricsChorusColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsChorusColor", themeColors.getColorInt("black"));
                cast_lyricsPreChorusColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsPreChorusColor", themeColors.getColorInt("black"));
                cast_lyricsBridgeColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsBridgeColor", themeColors.getColorInt("black"));
                cast_lyricsTagColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsTagColor", themeColors.getColorInt("black"));
                cast_lyricsCommentColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsCommentColor", themeColors.getColorInt("black"));
                cast_lyricsCustomColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom1_lyricsCustomColor", themeColors.getColorInt("black"));
                break;
            case "custom2":
                cast_lyricsCapoColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom2_lyricsCapoColor", themeColors.getColorInt("red"));
                cast_lyricsChordsColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom2_lyricsChordsColor", themeColors.getColorInt("yellow"));
                cast_presoFontColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "custom2_presoFontColor", themeColors.getColorInt("white"));
                cast_lyricsBackgroundColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsBackgroundColor", themeColors.getColorInt("black"));
                cast_lyricsTextColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsTextColor", themeColors.getColorInt("black"));
                cast_presoInfoColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_presoInfoColor", themeColors.getColorInt("black"));
                cast_presoAlertColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_presoAlertColor", themeColors.getColorInt("red"));
                cast_presoShadowColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_presoShadowColor", themeColors.getColorInt("black"));
                cast_lyricsVerseColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsVerseColor", themeColors.getColorInt("white"));
                cast_lyricsChorusColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsChorusColor", themeColors.getColorInt("white"));
                cast_lyricsPreChorusColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsPreChorusColor", themeColors.getColorInt("white"));
                cast_lyricsBridgeColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsBridgeColor", themeColors.getColorInt("white"));
                cast_lyricsTagColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsTagColor", themeColors.getColorInt("white"));
                cast_lyricsCommentColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsCommentColor", themeColors.getColorInt("white"));
                cast_lyricsCustomColor = mainActivityInterface.getPreferences().getMyPreferenceInt(c,"custom2_lyricsCustomColor", themeColors.getColorInt("white"));
                break;
        }
    }
    
    void setDefaultBackgroundImage(Context c) {
        cast_defimage = ResourcesCompat.getDrawable(c.getResources(), R.drawable.preso_default_bg, null);
    }

    void fixBackground(Context c, MainActivityInterface mainActivityInterface, ImageView projected_BackgroundImage,
                       SurfaceHolder projected_SurfaceHolder, SurfaceView projected_SurfaceView) {
        // Images and video backgrounds
        String img1 = mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundImage1","ost_bg.png");
        Uri img1Uri;
        if (img1.equals("ost_bg.png") || img1.startsWith("../")) {
            // This is a localised file, so get the properlocation
            if (img1.equals("ost_bg.png")) {
                img1 = "../Backgrounds/ost_bg.png";
            }
            img1Uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(c,mainActivityInterface.getPreferences(),img1);
        } else {
            img1Uri = Uri.parse(mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundImage1","ost_bg.png"));
        }
        String img2 = mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundImage2","ost_bg.png");
        Uri img2Uri;
        if (img2.equals("ost_bg.png") || img2.startsWith("../")) {
            // This is a localised file, so get the properlocation
            if (img2.equals("ost_bg.png")) {
                img2 = "../Backgrounds/ost_bg.png";
            }
            img2Uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(c,mainActivityInterface.getPreferences(),img2);
        } else {
            img2Uri = Uri.parse(mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundImage2","ost_bg.png"));
        }
        String vid1 = mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundVideo1","");
        Uri vid1Uri;
        if (vid1.startsWith("../")) {
            // This is a localised file, so get the properlocation
            vid1Uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(c,mainActivityInterface.getPreferences(),vid1);
        } else if (vid1.isEmpty()) {
            vid1Uri = null;
        } else {
            vid1Uri = Uri.parse(mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundVideo1",""));
        }
        String vid2 = mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundVideo2","");
        Uri vid2Uri;
        if (vid2.startsWith("../")) {
            // This is a localised file, so get the properlocation
            vid2Uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(c,mainActivityInterface.getPreferences(),vid2);
        } else if (vid2.isEmpty()) {
            vid2Uri = null;
        } else {
            vid2Uri = Uri.parse(mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundVideo2",""));
        }

        // Decide if user is using video or image for background
        Uri imgUri;
        switch (mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundTypeToUse","image")) {
            case "image":
                projected_BackgroundImage.setVisibility(View.VISIBLE);
                projected_SurfaceView.setVisibility(View.INVISIBLE);
                if (cast_mediaPlayer != null && cast_mediaPlayer.isPlaying()) {
                    cast_mediaPlayer.pause();
                }
                if (mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundToUse","img1").equals("img1")) {
                    imgUri = img1Uri;
                } else {
                    imgUri = img2Uri;
                }

                if (mainActivityInterface.getStorageAccess().uriExists(c, imgUri)) {
                    if (imgUri != null && imgUri.getLastPathSegment() != null && imgUri.getLastPathSegment().contains("ost_bg.png")) {
                        projected_BackgroundImage.setImageDrawable(cast_defimage);
                    } else {
                        RequestOptions myOptions = new RequestOptions()
                                .centerCrop();
                        GlideApp.with(c).load(imgUri).apply(myOptions).into(projected_BackgroundImage);
                    }
                    projected_BackgroundImage.setVisibility(View.VISIBLE);
                    mainActivityInterface.getCustomAnimation().faderAnimationCustomAlpha(projected_BackgroundImage,mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoTransitionTime",800),
                            projected_BackgroundImage.getAlpha(),mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoBackgroundAlpha",0.8f));

                }
                break;
            case "video":
                projected_BackgroundImage.setVisibility(View.INVISIBLE);
                projected_SurfaceView.setVisibility(View.VISIBLE);

                if (mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundToUse","img1").equals("vid1")) {
                    cast_vidUri = vid1Uri;
                } else {
                    cast_vidUri = vid2Uri;
                }
                try {
                    Log.d("d", "Trying to load video background");
                    reloadVideo(c,mainActivityInterface.getPreferences(),projected_SurfaceHolder,projected_SurfaceView);
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
    
    boolean matchPresentationToMode(MainActivityInterface mainActivityInterface, LinearLayout presentermode_bottombit,
                                    SurfaceView projected_SurfaceView, ImageView projected_BackgroundImage,
                                    ImageView projected_ImageView) {
        switch (mainActivityInterface.getMode()) {
            case "Stage":
            case "Performance":
            default:
                presentermode_bottombit.setVisibility(View.VISIBLE);
                projected_SurfaceView.setVisibility(View.GONE);
                projected_BackgroundImage.setImageDrawable(null);
                projected_BackgroundImage.setVisibility(View.GONE);
                projected_ImageView.setVisibility(View.GONE);
                break;

            case "Presentation":
                presentermode_bottombit.setVisibility(View.VISIBLE);
                break;
        }
        infoBarChangeRequired = true;
        forceCastUpdate = false;
        // IV - Always runfixbackground
        return true;
    }

    void presenterThemeSetUp(Context c, MainActivityInterface mainActivityInterface, 
                             
                             LinearLayout presentermode_bottombit, TextView presentermode_title,
                             TextView presentermode_author, TextView presentermode_copyright, 
                             TextView presentermode_ccli, TextView presentermode_alert) {
        // Set the text at the bottom of the page to match the presentation text colour
        presentermode_title.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        presentermode_title.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        presentermode_author.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        presentermode_copyright.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        presentermode_ccli.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        presentermode_alert.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
        presentermode_title.setTextColor(cast_presoInfoColor);
        presentermode_author.setTextColor(cast_presoInfoColor);
        presentermode_copyright.setTextColor(cast_presoInfoColor);
        presentermode_ccli.setTextColor(cast_presoInfoColor);
        presentermode_alert.setTextColor(cast_presoAlertColor);
        presentermode_title.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoTitleTextSize", 14.0f));
        presentermode_author.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoAuthorTextSize", 12.0f));
        presentermode_copyright.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoCopyrightTextSize", 12.0f));
        presentermode_ccli.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoCopyrightTextSize", 12.0f));
        presentermode_alert.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoAlertTextSize", 12.0f));
        presentermode_title.setShadowLayer(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoTitleTextSize", 14.0f) / 2.0f, 4, 4, cast_presoShadowColor);
        presentermode_author.setShadowLayer(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoAuthorTextSize", 12.0f) / 2.0f, 4, 4, cast_presoShadowColor);
        presentermode_copyright.setShadowLayer(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoCopyrightTextSize", 14.0f) / 2.0f, 4, 4, cast_presoShadowColor);
        presentermode_ccli.setShadowLayer(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoCopyrightTextSize", 14.0f) / 2.0f, 4, 4, cast_presoShadowColor);
        presentermode_alert.setShadowLayer(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoAlertTextSize", 12.0f) / 2.0f, 4, 4, cast_presoShadowColor);
        presentermode_title.setGravity(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        presentermode_author.setGravity(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        presentermode_copyright.setGravity(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        presentermode_ccli.setGravity(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoInfoAlign", Gravity.END));
        // IV - Align alert text the same as lyrics
        presentermode_alert.setGravity(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoLyricsAlign", Gravity.END));
        presentermode_bottombit.setBackgroundColor(ColorUtils.setAlphaComponent(cast_presoShadowColor,
                (int)(255*mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoInfoBarAlpha",0.5f))));
    }

    void presenterStartUp(final Context c, final MainActivityInterface mainActivityInterface, final ImageView projected_BackgroundImage,
                          final SurfaceHolder projected_SurfaceHolder, final SurfaceView projected_SurfaceView) {
        // After the fadeout time, set the background and fade in
        Handler h = new Handler();
        h.postDelayed(() -> {
            // Try to set the new background
            fixBackground(c, mainActivityInterface, projected_BackgroundImage,projected_SurfaceHolder, projected_SurfaceView);
            // IV - fixBackground does a logo fade in
        }, mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoTransitionTime",800));
    }
    
    // Change the margins
    void changeMargins(Context c, MainActivityInterface mainActivityInterface, RelativeLayout projectedPage_RelativeLayout) {
        projectedPage_RelativeLayout.setPadding(mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoXMargin",20)+ cast_padding,
                mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoYMargin",10)+cast_padding,
                mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoXMargin",20)+cast_padding,
                mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoYMargin",10)+cast_padding);
    }

    void updateAlpha(Context c, MainActivityInterface mainActivityInterface, ImageView projected_BackgroundImage,
                     ImageView projected_SurfaceView_Alpha) {
        float alpha = mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoBackgroundAlpha",0.8f);
        if (mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundToUse","img1").contains("vid")) {
            projected_SurfaceView_Alpha.setVisibility(View.VISIBLE);
            projected_SurfaceView_Alpha.setAlpha(1.0f-alpha);
            projected_SurfaceView_Alpha.setBackgroundColor(cast_lyricsBackgroundColor);
        } else {
            projected_BackgroundImage.setAlpha(alpha);
            projected_SurfaceView_Alpha.setVisibility(View.INVISIBLE);
        }
    }

    // The logo stuff, animations and blanking the screen
    void setUpLogo(Context c, MainActivityInterface mainActivityInterface, ImageView projected_Logo,
                   int availableWidth_1col, int availableScreenHeight) {
        // If the customLogo doesn't exist, use the default one
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int imgwidth = 1024;
        int imgheight = 500;
        float xscale;
        float yscale;
        boolean usingcustom = false;
        Uri customLogo = mainActivityInterface.getStorageAccess().fixLocalisedUri(c, mainActivityInterface.getPreferences(),
                mainActivityInterface.getPreferences().getMyPreferenceString(c, "customLogo", "ost_logo.png"));
        if (customLogo!=null && !customLogo.toString().contains("ost_logo") && mainActivityInterface.getStorageAccess().uriExists(c, customLogo)) {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c, customLogo);
            // Get the sizes of the custom logo
            BitmapFactory.decodeStream(inputStream, null, options);
            imgwidth = options.outWidth;
            imgheight = options.outHeight;
            if (imgwidth > 0 && imgheight > 0) {
                usingcustom = true;
            }
        }

        xscale = ((float) availableWidth_1col *
                mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "customLogoSize", 0.5f)) / (float) imgwidth;
        yscale = ((float) availableScreenHeight *
                mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "customLogoSize", 0.5f)) / (float) imgheight;

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
        // IV - Indicates the delayed showLogo call will be active unless overridden
        showLogoActive = true;
    }
    void showLogo(Context c,  MainActivityInterface mainActivityInterface, ImageView projected_ImageView,
                  LinearLayout projected_LinearLayout, RelativeLayout pageHolder,
                  ImageView projected_Logo) {
        if (showLogoActive) {
            panicRequired = false;
            // IV - If the infobar has not completed an 'Until' period, reset
            if (System.currentTimeMillis() < infoBarUntilTime) {
                infoBarChangeRequired = true;
            }
            // IV - Fade out stale content
            if (projected_ImageView.getAlpha() > 0.0f) {
                mainActivityInterface.getCustomAnimation().faderAnimation(projected_ImageView,
                        (int) (0.97 * mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800)), false);
            }
            if (projected_LinearLayout.getAlpha() > 0.0f) {
                mainActivityInterface.getCustomAnimation().faderAnimation(projected_LinearLayout,
                        (int) (0.97 * mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800)), false);
            }

            Handler h = new Handler();
            h.postDelayed(() -> {
                if (showLogoActive) {
                    // Fade in logo
                    mainActivityInterface.getCustomAnimation().faderAnimation(projected_Logo,
                            mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800), true);
                    // If we are black screen, fade the page back in
                    if (pageHolder.getVisibility() == View.INVISIBLE) {
                        mainActivityInterface.getCustomAnimation().faderAnimation(pageHolder,2 * mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800), true);
                    }
                }
            }, mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800));
            // IV - Another panic!
            Handler h2 = new Handler();
            h2.postDelayed(() -> {
                if (showLogoActive) {
                    projected_Logo.setAlpha(1.00f);
                    pageHolder.setAlpha(1.00f);
                }
            }, 5 * mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800));
        }
    }
    void hideLogo(Context c, MainActivityInterface mainActivityInterface, ImageView projected_Logo) {
        // IV - On hide after the startup logo, setup to display song info
        if (infoBarUntilTime == 0) {
            infoBarChangeRequired = true;
        }
        // IV - Makes sure any delayed showLogo calls do not undo the fade!
        showLogoActive = false;
        // IV - Make sure song Alert display is considered (song / alert state may have changed)
        infoBarAlertState = false;
        mainActivityInterface.getCustomAnimation().faderAnimation(projected_Logo,
                mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoTransitionTime",800),false);
    }

    void blankUnblankDisplay(Context c, MainActivityInterface mainActivityInterface,
                             RelativeLayout pageHolder, boolean unblank) {
        blankActive = !unblank;
        // IV - Make sure song Alert display is considered (song / alert state may have changed)
        infoBarAlertState = false;
        if (!unblank) {
            panicRequired = false;
            // IV - If the infobar has not completed an 'Until' period, reset
            if (System.currentTimeMillis() < infoBarUntilTime) {
                infoBarChangeRequired = true;
            }
        }
        mainActivityInterface.getCustomAnimation().faderAnimation(pageHolder,
                mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoTransitionTime",800),unblank);
        // IV - Another panic!
        Handler h2 = new Handler();
        h2.postDelayed(() -> {
            if (blankActive) {
                pageHolder.setAlpha(0.00f);
            } else {
                pageHolder.setAlpha(1.00f);
            }
        }, 5 * mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800));
    }

    private void animateIn(Context c, MainActivityInterface mainActivityInterface,
                           ImageView projected_ImageView, LinearLayout projected_LinearLayout) {
        if (mainActivityInterface.getSong().getIsImage() || mainActivityInterface.getSong().getIsImageSlide() ||
                mainActivityInterface.getSong().getIsPDF()) {
            mainActivityInterface.getCustomAnimation().faderAnimation(projected_ImageView,
                    mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800), true);
        } else {
            mainActivityInterface.getCustomAnimation().faderAnimation(projected_LinearLayout,
                    mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800), true);
        }
    }

    private void animateOut(Context c, MainActivityInterface mainActivityInterface, ImageView projected_Logo,
                            ImageView projected_ImageView, LinearLayout projected_LinearLayout, LinearLayout bottom_infobar) {
        if (projected_Logo.getAlpha() > 0.0f) {
            showLogoActive = false;
            mainActivityInterface.getCustomAnimation().faderAnimation(projected_Logo,
                    mainActivityInterface.getPreferences().getMyPreferenceInt(c,"presoTransitionTime",800),false);
        }

        if (mainActivityInterface.getSong().getIsImage() ||
                mainActivityInterface.getSong().getIsImageSlide() ||
                mainActivityInterface.getSong().getIsPDF()) {
            mainActivityInterface.getCustomAnimation().faderAnimation(bottom_infobar,
                     mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800), false);
        }
        // IV - Song infobar fade and screen sizing processing are handled elsewhere
        // IV - If we are not already doing a lyric fade
        if ((lyricAfterTime - 5) < System.currentTimeMillis()) {
            // IV - Fade out stale content
            // Fade out content a bit quicker, any fading infobar will then always be present during fade (no jump should the info block fade first)
            if (projected_ImageView.getAlpha() > 0.0f) {
                mainActivityInterface.getCustomAnimation().faderAnimation(projected_ImageView,
                        (int) (0.97 * mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800)), false);
            }
            if (projected_LinearLayout.getAlpha() > 0.0f) {
                mainActivityInterface.getCustomAnimation().faderAnimation(projected_LinearLayout,
                        (int) (0.97 * mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800)), false);
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
    private void doPDFPage(Context c, MainActivityInterface mainActivityInterface, ImageView projected_ImageView, LinearLayout projected_LinearLayout) {
        Bitmap bmp = mainActivityInterface.getProcessSong().createPDFPage(c, mainActivityInterface, cast_availableScreenWidth, cast_availableScreenHeight, "Y");
        projected_ImageView.setVisibility(View.GONE);
        projected_ImageView.setAlpha(0.0f);
        projected_ImageView.setBackgroundColor(mainActivityInterface.getMyThemeColors().getColorInt("white"));
        projected_ImageView.setImageBitmap(bmp);
        animateIn(c,mainActivityInterface,projected_ImageView,projected_LinearLayout);
    }
    private void doImagePage(Context c, MainActivityInterface mainActivityInterface, ImageView projected_ImageView, LinearLayout projected_LinearLayout) {
        projected_ImageView.setVisibility(View.GONE);
        projected_ImageView.setAlpha(0.0f);
        projected_ImageView.setBackgroundColor(mainActivityInterface.getMyThemeColors().getColorInt("white"));
        // Process the image location into an URI
        Uri imageUri;
        if (uriToLoad==null) {
            imageUri = mainActivityInterface.getStorageAccess().getUriForItem(c,
                    mainActivityInterface.getPreferences(), "Songs", mainActivityInterface.getSong().getFolder(), mainActivityInterface.getSong().getFilename());
        } else {
            imageUri = uriToLoad;
        }
        RequestOptions myOptions = new RequestOptions()
                .fitCenter().override(projected_LinearLayout.getMeasuredWidth(),projected_LinearLayout.getMeasuredHeight());
        GlideApp.with(c).load(imageUri).apply(myOptions).into(projected_ImageView);
        animateIn(c, mainActivityInterface, projected_ImageView, projected_LinearLayout);
    }

    private void wipeAllViews(LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        projected_LinearLayout.removeAllViews();
        projected_ImageView.setImageBitmap(null);
    }
    void updateAlert(Context c, MainActivityInterface mainActivityInterface, boolean show, TextView presentermode_alert) {
        // IV - A doUpdate is done elsewhere to handle fades
        if (show) {
            // IV - Set up to ensure no song info display
            infoBarUntilTime = 0;
            infoBarChangeRequired = false;
            alert_on = true;
            presentermode_alert.setText(mainActivityInterface.getPreferences().getMyPreferenceString(c,"presoAlertText",""));
            presentermode_alert.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());
            presentermode_alert.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoAlertTextSize", 12.0f));
            presentermode_alert.setTextColor(cast_presoAlertColor);
            presentermode_alert.setShadowLayer(mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"presoAlertTextSize", 12.0f) / 2.0f, 4, 4, cast_presoShadowColor);
        } else {
            alert_on = false;
        }
    }

    // MediaPlayer stuff
    void prepareMediaPlayer(Context c, MainActivityInterface mainActivityInterface,
                            SurfaceHolder projected_SurfaceHolder, Display myscreen, LinearLayout bottom_infobar, RelativeLayout projectedPage_RelativeLayout) {
        // Get the size of the SurfaceView
        getScreenSizes(myscreen,bottom_infobar,projectedPage_RelativeLayout,mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"castRotation",0.0f));
        cast_mediaPlayer = new MediaPlayer();
        cast_mediaPlayer.setDisplay(projected_SurfaceHolder);
        if (mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundTypeToUse","image").equals("video")) {
            try {
                cast_mediaPlayer.setDataSource(c, cast_vidUri);
                cast_mediaPlayer.prepare();

            } catch (Exception e) {
                Log.d("PresentationService", "Error setting data source for video");
            }
        }
    }
    void mediaPlayerIsPrepared(SurfaceView projected_SurfaceView) {
        try {
            // Get the video sizes so we can scale appropriately
            int width = cast_mediaPlayer.getVideoWidth();
            int height = cast_mediaPlayer.getVideoHeight();
            float max_xscale = (float) cast_screenWidth / (float) width;
            float max_yscale = (float) cast_screenHeight / (float) height;
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
            cast_mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void reloadVideo(final Context c, MainActivityInterface mainActivityInterface,
                     final SurfaceHolder projected_SurfaceHolder, final SurfaceView projected_SurfaceView) {
        if (cast_mediaPlayer == null) {
            cast_mediaPlayer = new MediaPlayer();
            try {
                cast_mediaPlayer.setDisplay(projected_SurfaceHolder);
                cast_mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            cast_mediaPlayer.reset();
        } catch (Exception e) {
            Log.d("PresentationService", "Error resetting mMediaPlayer");
        }

        if (mainActivityInterface.getPreferences().getMyPreferenceString(c,"backgroundTypeToUse","image").equals("video")) {
            try {
                Log.d("Presentation Common","cast_viUri="+cast_vidUri);
                cast_mediaPlayer.setDataSource(c, cast_vidUri);
                cast_mediaPlayer.setOnPreparedListener(mp -> {
                    try {
                        // Get the video sizes so we can scale appropriately
                        int width = mp.getVideoWidth();
                        int height = mp.getVideoHeight();
                        float max_xscale = (float) cast_screenWidth / (float) width;
                        float max_yscale = (float) cast_screenHeight / (float) height;
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
                cast_mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                    if (mediaPlayer != null) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                        mediaPlayer.reset();
                    }
                    try {
                        reloadVideo(c,mainActivityInterface,projected_SurfaceHolder,projected_SurfaceView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                cast_mediaPlayer.prepare();

            } catch (Exception e) {
                Log.d("PresentationService", "Error setting data source for video");
            }
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

    // Writing the views for PerformanceMode
    private void prepareFullProjected (final Context c, final MainActivityInterface mainActivityInterface,
                                       final LinearLayout col1_1, final LinearLayout col1_2,
                                       final LinearLayout col2_2, final LinearLayout col1_3,
                                       final LinearLayout col2_3, final LinearLayout col3_3,
                                       final LinearLayout projected_LinearLayout,
                                       final ImageView projected_ImageView) {

        new Thread(() -> {

            // Updating views on the UI
            mainActivityInterface.getActivity().runOnUiThread(() -> {
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
                for (int x = 0; x < mainActivityInterface.getSong().getSongSections().size(); x++) {

                    test1_1 = mainActivityInterface.getProcessSong().projectedSectionView(c, x, 12.0f,
                            mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                            cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                    col1_1.addView(test1_1);

                    if (x < mainActivityInterface.getSong().getHalfSplit()) {
                        test1_2 = mainActivityInterface.getProcessSong().projectedSectionView(c, x, 12.0f,
                                mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                                cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                        col1_2.addView(test1_2);
                    } else {
                        test2_2 = mainActivityInterface.getProcessSong().projectedSectionView(c, x, 12.0f,
                                mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                                cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                        col2_2.addView(test2_2);
                    }

                    if (x < mainActivityInterface.getSong().getThirdSplit()) {
                        test1_3 = mainActivityInterface.getProcessSong().projectedSectionView(c, x, 12.0f,
                                mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                                cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                        col1_3.addView(test1_3);
                    } else if (x >= mainActivityInterface.getSong().getThirdSplit() && x <
                            mainActivityInterface.getSong().getTwoThirdSplit()) {
                        test2_3 = mainActivityInterface.getProcessSong().projectedSectionView(c, x, 12.0f,
                                mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                                cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                        col2_3.addView(test2_3);
                    } else {
                        test3_3 = mainActivityInterface.getProcessSong().projectedSectionView(c, x, 12.0f,
                                mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                                cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
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
                float maxwidth_scale1_1 = ((float) cast_availableWidth_1col) / (float) widthofsection1_1;
                float maxwidth_scale1_2 = ((float) cast_availableWidth_2col) / (float) widthofsection1_2;
                float maxwidth_scale2_2 = ((float) cast_availableWidth_2col) / (float) widthofsection2_2;
                float maxwidth_scale1_3 = ((float) cast_availableWidth_3col) / (float) widthofsection1_3;
                float maxwidth_scale2_3 = ((float) cast_availableWidth_3col) / (float) widthofsection2_3;
                float maxwidth_scale3_3 = ((float) cast_availableWidth_3col) / (float) widthofsection3_3;
                float maxheight_scale1_1 = ((float) cast_availableScreenHeight) / (float) heightofsection1_1;
                float maxheight_scale1_2 = ((float) cast_availableScreenHeight) / (float) heightofsection1_2;
                float maxheight_scale2_2 = ((float) cast_availableScreenHeight) / (float) heightofsection2_2;
                float maxheight_scale1_3 = ((float) cast_availableScreenHeight) / (float) heightofsection1_3;
                float maxheight_scale2_3 = ((float) cast_availableScreenHeight) / (float) heightofsection2_3;
                float maxheight_scale3_3 = ((float) cast_availableScreenHeight) / (float) heightofsection3_3;

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
                float maxscale = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "fontSizePresoMax", 40.0f) / 12.0f;

                switch (colstouse) {
                    case 1:
                        if (maxwidth_scale1_1 > maxscale) {
                            maxwidth_scale1_1 = maxscale;
                        }
                        projectedPerformanceView1col(c, mainActivityInterface, maxwidth_scale1_1,
                                projected_LinearLayout, projected_ImageView);
                        break;

                    case 2:
                        if (maxwidth_scale1_2 > maxscale) {
                            maxwidth_scale1_2 = maxscale;
                        }
                        if (maxwidth_scale2_2 > maxscale) {
                            maxwidth_scale2_2 = maxscale;
                        }
                        projectedPerformanceView2col(c, mainActivityInterface, maxwidth_scale1_2, maxwidth_scale2_2,
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
                        projectedPerformanceView3col(c, mainActivityInterface, maxwidth_scale1_3, maxwidth_scale2_3, maxwidth_scale3_3,
                                projected_LinearLayout, projected_ImageView);
                        break;
                }
            });
        }).start();

    }
    private void projectedPerformanceView1col(Context c, MainActivityInterface mainActivityInterface,
                                              float scale1_1, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {
            LinearLayout lyrics1_1 = mainActivityInterface.getProcessSong().createLinearLayout(c,mainActivityInterface);
            LinearLayout box1_1 = mainActivityInterface.getProcessSong().
                    prepareProjectedBoxView(c, mainActivityInterface, cast_lyricsTextColor,
                    cast_lyricsBackgroundColor, cast_padding);
            float fontsize1_1 = mainActivityInterface.getProcessSong().getProjectedFontSize(scale1_1);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_1.setPadding(0, 0, 0, 0);

            // Prepare the new views to add to 1,2 and 3 colums ready for measuring
            // Go through each section
            for (int x = 0; x < StaticVariables.songSections.length; x++) {
                lyrics1_1 = mainActivityInterface.getProcessSong().projectedSectionView(c, x, fontsize1_1,
                        mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                        cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(cast_availableWidth_1col,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                llp1_1.setMargins(0, 0, 0, 0);
                lyrics1_1.setLayoutParams(llp1_1);
                lyrics1_1.setBackgroundColor(mainActivityInterface.getProcessSong().getSectionColors(mainActivityInterface.getSong().getSongSectionTypes().get(x),
                        cast_lyricsVerseColor, cast_lyricsChorusColor,
                        cast_lyricsPreChorusColor, cast_lyricsBridgeColor, cast_lyricsTagColor,
                        cast_lyricsCommentColor, cast_lyricsCustomColor));
                box1_1.addView(lyrics1_1);
            }

            // Now add the display
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(cast_availableScreenWidth,
                    cast_availableScreenHeight + cast_padding);
            llp.setMargins(0, 0, 0, 0);
            projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            box1_1.setLayoutParams(llp);
            projected_LinearLayout.addView(box1_1);
            animateIn(c, mainActivityInterface, projected_ImageView, projected_LinearLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void projectedPerformanceView2col(Context c, MainActivityInterface mainActivityInterface,
                                              float scale1_2, float scale2_2, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {
            LinearLayout lyrics1_2 = mainActivityInterface.getProcessSong().createLinearLayout(c, mainActivityInterface);
            LinearLayout lyrics2_2 = mainActivityInterface.getProcessSong().createLinearLayout(c, mainActivityInterface);
            LinearLayout box1_2 = mainActivityInterface.getProcessSong().prepareProjectedBoxView(c, mainActivityInterface, cast_lyricsTextColor,
                    cast_lyricsBackgroundColor, cast_padding);
            LinearLayout box2_2 = mainActivityInterface.getProcessSong().prepareProjectedBoxView(c, mainActivityInterface, cast_lyricsTextColor,
                    cast_lyricsBackgroundColor, cast_padding);
            float fontsize1_2 = mainActivityInterface.getProcessSong().getProjectedFontSize(scale1_2);
            float fontsize2_2 = mainActivityInterface.getProcessSong().getProjectedFontSize(scale2_2);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_2.setPadding(0, 0, 0, 0);
            lyrics2_2.setPadding(0, 0, 0, 0);

            // Prepare the new views to add to 1,2 and 3 colums ready for measuring
            // Go through each section
            for (int x = 0; x < StaticVariables.songSections.length; x++) {

                if (x < mainActivityInterface.getSong().getHalfSplit()) {
                    lyrics1_2 = mainActivityInterface.getProcessSong().projectedSectionView(c, x, fontsize1_2,
                            mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                            cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                    LinearLayout.LayoutParams llp1_2 = new LinearLayout.LayoutParams(cast_availableWidth_2col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_2.setMargins(0, 0, 0, 0);
                    lyrics1_2.setLayoutParams(llp1_2);
                    lyrics1_2.setBackgroundColor(mainActivityInterface.getProcessSong().getSectionColors(StaticVariables.songSectionsTypes[x],
                            cast_lyricsVerseColor, cast_lyricsChorusColor, cast_lyricsPreChorusColor,
                            cast_lyricsBridgeColor, cast_lyricsTagColor,
                            cast_lyricsCommentColor, cast_lyricsCustomColor));
                    box1_2.addView(lyrics1_2);
                } else {
                    lyrics2_2 = mainActivityInterface.getProcessSong().projectedSectionView(c, x, fontsize2_2,
                            mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                            cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                    LinearLayout.LayoutParams llp2_2 = new LinearLayout.LayoutParams(cast_availableWidth_2col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp2_2.setMargins(0, 0, 0, 0);
                    lyrics2_2.setLayoutParams(llp2_2);
                    lyrics2_2.setBackgroundColor(mainActivityInterface.getProcessSong().getSectionColors(songSectionsTypes[x],
                            cast_lyricsVerseColor, cast_lyricsChorusColor, cast_lyricsPreChorusColor,
                            cast_lyricsBridgeColor, cast_lyricsTagColor,
                            cast_lyricsCommentColor,cast_lyricsCustomColor));
                    box2_2.addView(lyrics2_2);
                }
            }

            // Now add the display
            LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(cast_availableWidth_2col + (cast_padding * 2), cast_availableScreenHeight + cast_padding);
            LinearLayout.LayoutParams llp2 = new LinearLayout.LayoutParams(cast_availableWidth_2col + (cast_padding * 2), cast_availableScreenHeight + cast_padding);
            llp1.setMargins(0, 0, cast_padding, 0);
            llp2.setMargins(0, 0, 0, 0);
            projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            box1_2.setLayoutParams(llp1);
            box2_2.setLayoutParams(llp2);
            projected_LinearLayout.addView(box1_2);
            projected_LinearLayout.addView(box2_2);
            animateIn(c, mainActivityInterface, projected_ImageView, projected_LinearLayout);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void projectedPerformanceView3col(Context c, MainActivityInterface mainActivityInterface,
                                              float scale1_3, float scale2_3, float scale3_3, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {
            LinearLayout lyrics1_3 = mainActivityInterface.getProcessSong().createLinearLayout(c,mainActivityInterface);
            LinearLayout lyrics2_3 = mainActivityInterface.getProcessSong().createLinearLayout(c,mainActivityInterface);
            LinearLayout lyrics3_3 = mainActivityInterface.getProcessSong().createLinearLayout(c,mainActivityInterface);
            LinearLayout box1_3 = mainActivityInterface.getProcessSong().prepareProjectedBoxView(c, mainActivityInterface, cast_lyricsTextColor,
                    cast_lyricsBackgroundColor, cast_padding);
            LinearLayout box2_3 = mainActivityInterface.getProcessSong().prepareProjectedBoxView(c, mainActivityInterface, cast_lyricsTextColor,
                    cast_lyricsBackgroundColor, cast_padding);
            LinearLayout box3_3 = mainActivityInterface.getProcessSong().prepareProjectedBoxView(c, mainActivityInterface, cast_lyricsTextColor,
                    cast_lyricsBackgroundColor, cast_padding);
            float fontsize1_3 = mainActivityInterface.getProcessSong().getProjectedFontSize(scale1_3);
            float fontsize2_3 = mainActivityInterface.getProcessSong().getProjectedFontSize(scale2_3);
            float fontsize3_3 = mainActivityInterface.getProcessSong().getProjectedFontSize(scale3_3);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_3.setPadding(0, 0, 0, 0);
            lyrics2_3.setPadding(0, 0, 0, 0);
            lyrics3_3.setPadding(0, 0, 0, 0);

            // Prepare the new views to add to 1,2 and 3 colums ready for measuring
            // Go through each section
            for (int x = 0; x < StaticVariables.songSections.length; x++) {
                if (x < mainActivityInterface.getSong().getThirdSplit()) {
                    lyrics1_3 = mainActivityInterface.getProcessSong().projectedSectionView(c, x, fontsize1_3,
                            mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                            cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                    LinearLayout.LayoutParams llp1_3 = new LinearLayout.LayoutParams(cast_availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp1_3.setMargins(0, 0, 0, 0);
                    lyrics1_3.setLayoutParams(llp1_3);
                    lyrics1_3.setBackgroundColor(mainActivityInterface.getProcessSong().getSectionColors(mainActivityInterface.getSong().getSongSectionTypes().get(x),
                            cast_lyricsVerseColor, cast_lyricsChorusColor, cast_lyricsPreChorusColor,
                            cast_lyricsBridgeColor, cast_lyricsTagColor,
                            cast_lyricsCommentColor, cast_lyricsCustomColor));
                    box1_3.addView(lyrics1_3);
                } else if (x >= mainActivityInterface.getSong().getThirdSplit() &&
                        x < mainActivityInterface.getSong().getTwoThirdSplit()) {
                    lyrics2_3 = mainActivityInterface.getProcessSong().projectedSectionView(c, x, fontsize2_3,
                            mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                            cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                    LinearLayout.LayoutParams llp2_3 = new LinearLayout.LayoutParams(cast_availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp2_3.setMargins(0, 0, 0, 0);
                    lyrics2_3.setLayoutParams(llp2_3);
                    lyrics2_3.setBackgroundColor(mainActivityInterface.getProcessSong().getSectionColors(mainActivityInterface.getSong().getSongSectionTypes().get(x),
                            cast_lyricsVerseColor, cast_lyricsChorusColor, cast_lyricsPreChorusColor,
                            cast_lyricsBridgeColor, cast_lyricsTagColor,
                            cast_lyricsCommentColor, cast_lyricsCustomColor));
                    box2_3.addView(lyrics2_3);
                } else {
                    lyrics3_3 = mainActivityInterface.getProcessSong().projectedSectionView(c, x, fontsize3_3,
                            mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                            cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                    LinearLayout.LayoutParams llp3_3 = new LinearLayout.LayoutParams(cast_availableWidth_3col, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp3_3.setMargins(0, 0, 0, 0);
                    lyrics3_3.setLayoutParams(llp3_3);
                    lyrics3_3.setBackgroundColor(mainActivityInterface.getProcessSong().getSectionColors(mainActivityInterface.getSong().getSongSectionTypes().get(x),
                            cast_lyricsVerseColor, cast_lyricsChorusColor, cast_lyricsPreChorusColor,
                            cast_lyricsBridgeColor, cast_lyricsTagColor,
                            cast_lyricsCommentColor, cast_lyricsCustomColor));
                    box3_3.addView(lyrics3_3);
                }
            }

            // Now add the display
            LinearLayout.LayoutParams llp1 = new LinearLayout.LayoutParams(cast_availableWidth_3col + (cast_padding * 2), cast_availableScreenHeight + cast_padding);
            LinearLayout.LayoutParams llp3 = new LinearLayout.LayoutParams(cast_availableWidth_3col + (cast_padding * 2), cast_availableScreenHeight + cast_padding);
            llp1.setMargins(0, 0, cast_padding, 0);
            llp3.setMargins(0, 0, 0, 0);
            projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            box1_3.setLayoutParams(llp1);
            box2_3.setLayoutParams(llp1);
            box3_3.setLayoutParams(llp3);
            projected_LinearLayout.addView(box1_3);
            projected_LinearLayout.addView(box2_3);
            projected_LinearLayout.addView(box3_3);
            animateIn(c, mainActivityInterface, projected_ImageView, projected_LinearLayout);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    // Writing the views for StageMode
    private void prepareStageProjected(final Context c, final MainActivityInterface mainActivityInterface,
                                       final LinearLayout col1_1, final LinearLayout col1_2, final LinearLayout col2_2, final LinearLayout col1_3,
                                       final LinearLayout col2_3, final LinearLayout col3_3, final LinearLayout projected_LinearLayout, final ImageView projected_ImageView) {

        if (mainActivityInterface.getActivity()!=null) {
            new Thread(() -> {
                try {
                    // Updating views on the UI
                    //activity.runOnUiThread(new Runnable() {
                    mainActivityInterface.getActivity().runOnUiThread(() -> {
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
                        test1_1 = mainActivityInterface.getProcessSong().projectedSectionView(c, mainActivityInterface.getSong().getCurrentSection(), 12.0f,
                                mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                                cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                        col1_1.addView(test1_1);

                        // Now premeasure the views
                        col1_1.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                        // Get the widths and heights of the sections
                        int widthofsection1_1 = col1_1.getMeasuredWidth();
                        int heightofsection1_1 = col1_1.getMeasuredHeight();

                        // We know the size of each section, so we just need to know which one to display
                        float maxwidth_scale1_1 = ((float) cast_availableWidth_1col) / (float) widthofsection1_1;
                        float maxheight_scale1_1 = ((float) cast_availableScreenHeight) / (float) heightofsection1_1;

                        if (maxheight_scale1_1 < maxwidth_scale1_1) {
                            maxwidth_scale1_1 = maxheight_scale1_1;
                        }

                        // Now we know how many columns we should use, let's do it!
                        float maxscale = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "fontSizePresoMax", 40.0f) / 12.0f;

                        if (maxwidth_scale1_1 > maxscale) {
                            maxwidth_scale1_1 = maxscale;
                        }
                        projectedStageView1Col(c, mainActivityInterface, maxwidth_scale1_1,
                                projected_LinearLayout, projected_ImageView);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    private void projectedStageView1Col(Context c, MainActivityInterface mainActivityInterface,
                                        float scale1_1, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {

            LinearLayout lyrics1_1 = mainActivityInterface.getProcessSong().createLinearLayout(c, mainActivityInterface);
            LinearLayout box1_1 = mainActivityInterface.getProcessSong().prepareProjectedBoxView(c, mainActivityInterface, cast_lyricsTextColor,
                    cast_lyricsBackgroundColor, cast_padding);
            float fontsize1_1 = mainActivityInterface.getProcessSong().getProjectedFontSize(scale1_1);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_1.setPadding(0, 0, 0, 0);

            // Add this section
            lyrics1_1 = mainActivityInterface.getProcessSong().projectedSectionView(c, StaticVariables.currentSection,
                    fontsize1_1,
                    mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                    cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
            LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(cast_availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp1_1.setMargins(0, 0, 0, 0);
            lyrics1_1.setLayoutParams(llp1_1);
            box1_1.addView(lyrics1_1);


            // Now add the display
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(cast_availableScreenWidth,
                    cast_availableScreenHeight + cast_padding);
            llp.setMargins(0, 0, 0, 0);
            projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            box1_1.setLayoutParams(llp);
            projected_LinearLayout.addView(box1_1);
            animateIn(c, mainActivityInterface, projected_ImageView, projected_LinearLayout);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    // Writing the views for PresenterMode
    private void preparePresenterProjected(final Context c, final MainActivityInterface mainActivityInterface,
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
                        test1_1 = mainActivityInterface.getProcessSong().projectedSectionView(c, mainActivityInterface.getSong().getCurrentSection(), 12.0f,
                                mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                                cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
                        col1_1.addView(test1_1);

                        // Now premeasure the views
                        tryMeasure(col1_1);
                        // GE Catch error detected

                        // Get the widths and heights of the sections
                        int widthofsection1_1 = col1_1.getMeasuredWidth();
                        int heightofsection1_1 = col1_1.getMeasuredHeight();

                        // We know the size of each section, so we just need to know which one to display
                        float maxwidth_scale1_1 = ((float) cast_availableWidth_1col) / (float) widthofsection1_1;
                        float maxheight_scale1_1 = ((float) cast_availableScreenHeight) / (float) heightofsection1_1;

                        if (maxheight_scale1_1 < maxwidth_scale1_1) {
                            maxwidth_scale1_1 = maxheight_scale1_1;
                        }

                        // Now we know how many columns we should use, let's do it!
                        float maxscale = mainActivityInterface.getPreferences().getMyPreferenceFloat(c, "fontSizePresoMax", 40.0f) / 12.0f;

                        if (maxwidth_scale1_1 > maxscale) {
                            maxwidth_scale1_1 = maxscale;
                        }
                        projectedPresenterView1Col(c, mainActivityInterface, maxwidth_scale1_1,
                                projected_LinearLayout, projected_ImageView);

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    private void projectedPresenterView1Col(Context c, MainActivityInterface mainActivityInterface,
                                            float scale1_1, LinearLayout projected_LinearLayout, ImageView projected_ImageView) {
        // This is run inside the UI thread from the calling class (prepareFullProjected)
        try {
            LinearLayout lyrics1_1 = mainActivityInterface.getProcessSong().createLinearLayout(c, mainActivityInterface);
            LinearLayout box1_1 = mainActivityInterface.getProcessSong().prepareProjectedBoxView(c, mainActivityInterface, cast_lyricsTextColor,
                    cast_lyricsBackgroundColor, cast_padding);
            float fontsize1_1 = mainActivityInterface.getProcessSong().getProjectedFontSize(scale1_1);

            // Remove all views from the projector
            projected_LinearLayout.removeAllViews();
            lyrics1_1.setPadding(0, 0, 0, 0);
            lyrics1_1.setPadding(0, 0, 0, 0);

            // Add this section
            lyrics1_1 = mainActivityInterface.getProcessSong().projectedSectionView(c, StaticVariables.currentSection,
                    fontsize1_1,
                    mainActivityInterface, cast_lyricsTextColor, cast_lyricsChordsColor,
                    cast_lyricsCapoColor, cast_presoFontColor, cast_presoShadowColor);
            LinearLayout.LayoutParams llp1_1 = new LinearLayout.LayoutParams(cast_availableWidth_1col, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp1_1.setMargins(0, 0, 0, 0);
            lyrics1_1.setLayoutParams(llp1_1);
            box1_1.addView(lyrics1_1);

            // Now add the display
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(cast_availableScreenWidth,
                    cast_availableScreenHeight + cast_padding);
            llp.setMargins(0, 0, 0, 0);
            projected_LinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            box1_1.setLayoutParams(llp);
            projected_LinearLayout.addView(box1_1);
            animateIn(c, mainActivityInterface, projected_ImageView, projected_LinearLayout);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Update the screen content
    void doUpdate(final Context c, final MainActivityInterface mainActivityInterface,
                  LinearLayout presentermode_bottombit, final SurfaceView projected_SurfaceView,
                  ImageView projected_BackgroundImage, RelativeLayout pageHolder,
                  ImageView projected_Logo, final ImageView projected_ImageView,
                  final LinearLayout projected_LinearLayout, LinearLayout bottom_infobar,
                  final RelativeLayout projectedPage_RelativeLayout,
                  TextView presentermode_title, TextView presentermode_author,
                  TextView presentermode_copyright, TextView presentermode_ccli,
                  TextView presentermode_alert,
                  final LinearLayout col1_1, final LinearLayout col1_2,
                  final LinearLayout col2_2, final LinearLayout col1_3,
                  final LinearLayout col2_3, final LinearLayout col3_3) {

        if (!doUpdateActive) {
            doUpdateActive = true;

            // IV - Can be called whilst previous call is still running...  Always do fade out.  Only do fade in if we are not animating out due to a later call
            // First up, animate everything away
            panicRequired = false;
            animateOutActive = true;
            animateOut(c, mainActivityInterface, projected_Logo, projected_ImageView, projected_LinearLayout, bottom_infobar);

            // If we have forced an update due to switching modes, set that up
            if (forceCastUpdate) {
                matchPresentationToMode(mainActivityInterface,presentermode_bottombit, projected_SurfaceView, projected_BackgroundImage, projected_ImageView);
            }

            // If we had a black screen, fade page back in
            if (pageHolder.getVisibility() == View.INVISIBLE) {
                mainActivityInterface.getCustomAnimation().faderAnimation(pageHolder, mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800), true);
            }

            // Just in case there is a glitch, make the stuff visible after 5x transition time
            // IV - Panic request is prevented on display of logo or blank by setting panicRequired = false;
            panicDelay = 5 * mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800);
            // IV - There can be multiple postDelayed calls running, each call sets a later 'After' time.
            panicAfterTime = System.currentTimeMillis() + panicDelay;
            Handler panic = new Handler();
            panic.postDelayed(() -> {
                // IV - Quick section moves mean multiple panics are active, a time based test ensures action is only taken for the last call
                // If on running the time test fails a newer postDelayed has been made
                // After the panic delay time, make sure the correct view is visible regardless of animations
                if (panicRequired && !animateOutActive && ((panicAfterTime - 5) < System.currentTimeMillis())) {
                    if (mainActivityInterface.getSong().getIsImage() ||
                            mainActivityInterface.getSong().getIsPDF() ||
                            mainActivityInterface.getSong().getIsImageSlide()) {
                        projected_ImageView.setVisibility(View.VISIBLE);
                        projected_LinearLayout.setVisibility(View.GONE);
                        projected_ImageView.setAlpha(1.0f);
                    } else if (isVideo) {
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

            presenterWriteSongInfo(c, mainActivityInterface, presentermode_title, presentermode_author, presentermode_copyright, presentermode_ccli, presentermode_alert, bottom_infobar);

            // IV - There can be multiple postDelayed calls running, each call sets a later 'After' time.
            lyricDelay = mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800) + infoBarChangeDelay;
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
                    if (!mainActivityInterface.getMode().equals("Presentation")) {
                        // Set the page background to the correct colour for Peformance/Stage modes
                        projectedPage_RelativeLayout.setBackgroundColor(cast_lyricsBackgroundColor);
                    }

                    // Get the size of the SurfaceView here as any infobar will be visible at this point
                    getScreenSizes(mainActivityInterface.getDisplay(), bottom_infobar, projectedPage_RelativeLayout,
                            mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"castRotation",0.0f));

                    panicRequired = true;
                    // Decide on what we are going to show
                    if (mainActivityInterface.getSong().getIsPDF()) {
                        doPDFPage(c,mainActivityInterface,projected_ImageView,projected_LinearLayout);
                    } else if (mainActivityInterface.getSong().getIsImage() ||
                            mainActivityInterface.getSong().getIsImageSlide()) {
                        doImagePage(c,mainActivityInterface,projected_ImageView,projected_LinearLayout);
                    } else {
                        projected_ImageView.setVisibility(View.GONE);
                        projected_ImageView.setAlpha(0.0f);
                        switch (mainActivityInterface.getMode()) {
                            case "Stage":
                                prepareStageProjected(c,mainActivityInterface,col1_1,col1_2,col2_2,col1_3,col2_3,col3_3,
                                        projected_LinearLayout,projected_ImageView);
                                break;
                            case "Performance":
                                prepareFullProjected(c,mainActivityInterface,col1_1,col1_2,col2_2,col1_3,col2_3,col3_3,
                                        projected_LinearLayout,projected_ImageView);
                                break;
                            default:
                                preparePresenterProjected(c,mainActivityInterface,col1_1,col1_2,col2_2,col1_3,col2_3,col3_3,
                                        projected_LinearLayout,projected_ImageView);
                                break;
                        }
                    }
                }
            }, lyricDelay);
            doUpdateActive = false;
        }
    }
    private void presenterWriteSongInfo(Context c, MainActivityInterface mainActivityInterface,
                                        TextView presentermode_title,
                                        TextView presentermode_author,
                                        TextView presentermode_copyright,
                                        TextView presentermode_ccli,
                                        TextView presentermode_alert,
                                        LinearLayout bottom_infobar) {

        if (FullscreenActivity.isImage || FullscreenActivity.isImageSlide || FullscreenActivity.isPDF) {
            // IV - Force consideration of alert state when text after the Until period
            infoBarAlertState = "";
        } else {

            // IV - Overrides for when not hiding song info
            if (!mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"presoInfoBarHide",true) &&
                    !alert_on) {
                // IV - If we are ending Alert display then force song info display else keep song info by extending the until time
                if (infoBarAlertState) {
                    infoBarChangeRequired = true;
                } else {
                    infoBarUntilTime = System.currentTimeMillis() + 10000;
                }
            }

            // IV - Exceutes for first section after a change is requested (for a fresh song info display)
            // IV - AND section changes AFTER the subsequent 'Until' period end (for alert display)
            // IV - NOT for section changes after the first that occur BEFORE the end of the Until period

            if ((infoBarChangeRequired) || (System.currentTimeMillis() > infoBarUntilTime)) {
                String new_author = "";
                String new_title = "";
                String new_copyright = "";
                String new_ccli = "";

                // IV - Do once for first section after change required
                if (infoBarChangeRequired) {
                    new_author = mainActivityInterface.getSong().getAuthor().trim();
                    if (!new_author.equals(""))
                        new_author = c.getString(R.string.wordsandmusicby) + " " + new_author;

                    new_copyright = mainActivityInterface.getSong().getCopyright().trim();
                    if (!new_copyright.isEmpty() && (!new_copyright.contains("©")))
                        new_copyright = "© " + new_copyright;

                    new_ccli = mainActivityInterface.getPreferences().getMyPreferenceString(c, "ccliLicence", "");
                    if (!new_ccli.isEmpty() && (!mainActivityInterface.getSong().getCcli().isEmpty())) {
                        new_ccli = c.getString(R.string.usedbypermision) + " CCLI " + c.getString(R.string.ccli_licence) + " " + new_ccli;
                    } else {
                        new_ccli = "";
                    }

                    new_title = mainActivityInterface.getSong().getTitle();
                    if (new_title.startsWith("_")) new_title = "";
                    else {
                        // IV - If we have only a title, use without quotes
                        if ((new_author + new_copyright + new_ccli).equals("")) {
                            new_title = mainActivityInterface.getSong().getTitle().trim();
                        } else {
                            new_title = "\"" + mainActivityInterface.getSong().getTitle().trim() + "\"";
                        }
                    }
                }

                // IV - We will need to animate if we pass this test - no false positives
                if (infoBarChangeRequired || infoBarAlertState!=alert_on) {
                    // IV - Fade to 0.01f to keep on screen
                    mainActivityInterface.getCustomAnimation().faderAnimationCustomAlpha(bottom_infobar, mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800), bottom_infobar.getAlpha(), 0.01f);
                    // IV - Delay lyrics to ensure new infobar is available for correct screen sizing - Set also to provide a good transition
                    infoBarChangeDelay = 200;
                    // IV - Rapid song changes can see multiple handlers running - ensure that none show an alert
                    if (infoBarChangeRequired) {
                        infoBarAlertState = false;
                    } else {
                        infoBarAlertState = alert_on;
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
                        if (infoBarChangeRequired) {
                            infoBarChangeRequired = false;
                            // IV - Make sure song info is seen for at least 10s
                            infoBarUntilTime = System.currentTimeMillis() + 10000;
                            presentermode_alert.setVisibility(View.GONE);
                            // IV - Force consideration of alert state after the Until period
                            infoBarAlertState = false;
                        // IV - Fade in only if something to show
                            if (new StringBuilder().append(finalNew_title).append(finalNew_author).append(finalNew_copyright).append(finalNew_ccli).toString().trim() != "") {
                                CustomAnimations.faderAnimation(bottom_infobar, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800), true);
                            } else {
                            if (infoBarAlertState) {
                                // IV - Align alert text the same as lyrics
                                presentermode_alert.setGravity(mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoLyricsAlign", Gravity.END));
                                presentermode_alert.setVisibility(View.VISIBLE);
                            h.postDelayed(() -> {
                                    CustomAnimations.faderAnimation(bottom_infobar, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800), true);
                                }, preferences.getMyPreferenceInt(c, "presoTransitionTime", 800));
                            } else {
                                presentermode_alert.setVisibility(View.GONE);
                            }
                        }
                    }, mainActivityInterface.getPreferences().getMyPreferenceInt(c, "presoTransitionTime", 800));
                }
            }
        }
    }
*/

}

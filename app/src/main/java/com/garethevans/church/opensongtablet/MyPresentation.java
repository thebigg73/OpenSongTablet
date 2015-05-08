package com.garethevans.church.opensongtablet;

import java.io.File;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public final class MyPresentation extends Presentation {

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
	static RelativeLayout bottomBit;
	static FrameLayout preso;
	static VideoView videoBackground;
	static FrameLayout lyricsHolder;

	// static Drawable dr;
	// static Bitmap myBitmap;
	int lyricsTextColor = FullscreenActivity.dark_lyricsTextColor;
	int lyricsShadowColor = FullscreenActivity.dark_lyricsBackgroundColor;
	Typeface lyricsfont;
	//static int myWidth;
	//static int myHeight;
	static Drawable defimage;
	static Bitmap myBitmap;
	static Drawable dr;
	static int screenwidth;
	static int textwidth;
	static int screenheight;
	static int textheight;

	//MediaController
	MediaController mediaController;

	Context context;

	public MyPresentation(Context outerContext, Display display) {
		super(outerContext, display);
		// TODO Auto-generated constructor stub
	}

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

		videoBackground = (VideoView) findViewById(R.id.videoBackground);
		presoBGImage = (ImageView) findViewById(R.id.presoBGImage);

		//MediaController
		mediaController = new MediaController(getContext());
		mediaController.setVisibility(View.GONE);
		mediaController.setAnchorView(videoBackground);

		// Init Video
		videoBackground.setMediaController(mediaController);

		fixBackground();

		if (FullscreenActivity.mylyricsfontnum == 1) {
			lyricsfont = Typeface.MONOSPACE;
		} else if (FullscreenActivity.mylyricsfontnum == 2) {
			lyricsfont = Typeface.SANS_SERIF;
		} else if (FullscreenActivity.mylyricsfontnum == 3) {
			lyricsfont = Typeface.SERIF;
		} else if (FullscreenActivity.mylyricsfontnum == 4) {
			lyricsfont = Typeface.createFromAsset(getContext().getAssets(), "fonts/FiraSansOT-Light.otf");
		} else if (FullscreenActivity.mylyricsfontnum == 5) {
			lyricsfont = Typeface.createFromAsset(getContext().getAssets(), "fonts/FiraSansOT-Regular.otf");
		} else if (FullscreenActivity.mylyricsfontnum == 6) {
			lyricsfont = Typeface.createFromAsset(getContext().getAssets(), "fonts/KaushanScript-Regular.otf");
		} else if (FullscreenActivity.mylyricsfontnum == 7) {
			lyricsfont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato-Lig.ttf");
		} else if (FullscreenActivity.mylyricsfontnum == 8) {
			lyricsfont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Lato-Reg.ttf");
		} else {
			lyricsfont = Typeface.DEFAULT;
		}

		presoLyrics1 = (TextView) findViewById(R.id.presoLyrics1);
		presoLyrics1.setTextColor(0xffffffff);
		presoLyrics1.setTypeface(lyricsfont);
		presoLyrics1.setText(" ");
		presoLyrics1.setTextSize(18);
		presoLyrics1.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoLyrics1.setAlpha(0.0f);
		presoLyrics1.setVisibility(View.GONE);
		presoLyrics2 = (TextView) findViewById(R.id.presoLyrics2);
		presoLyrics2.setTextColor(0xffffffff);
		presoLyrics2.setTypeface(lyricsfont);
		presoLyrics2.setText(" ");
		presoLyrics2.setTextSize(18);
		presoLyrics2.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoLyrics2.setAlpha(0.0f);
		presoLyrics2.setVisibility(View.GONE);
		presoTitle1 = (TextView) findViewById(R.id.presoTitle1);
		presoTitle1.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoTitle1.setTextColor(lyricsTextColor);
		presoTitle1.setTextSize(12);
		presoTitle1.setTypeface(lyricsfont);
		presoTitle1.setText(" ");
		presoTitle2 = (TextView) findViewById(R.id.presoTitle2);
		presoTitle2.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoTitle2.setTextColor(lyricsTextColor);
		presoTitle2.setTextSize(12);
		presoTitle2.setTypeface(lyricsfont);
		presoTitle2.setText(" ");
		presoAuthor1 = (TextView) findViewById(R.id.presoAuthor1);
		presoAuthor1.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoAuthor1.setTextColor(lyricsTextColor);
		presoAuthor1.setText(" ");
		presoAuthor1.setTextSize(10);
		presoAuthor1.setTypeface(lyricsfont);
		presoAuthor2 = (TextView) findViewById(R.id.presoAuthor2);
		presoAuthor2.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoAuthor2.setTextColor(lyricsTextColor);
		presoAuthor2.setText(" ");
		presoAuthor2.setTextSize(10);
		presoAuthor2.setTypeface(lyricsfont);
		presoAlert = (TextView) findViewById(R.id.presoAlert);
		presoAlert.setVisibility(View.INVISIBLE);
		presoAlert.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoAlert.setTextColor(lyricsTextColor);
		presoAlert.setTextSize(10);
		presoAlert.setTypeface(lyricsfont);
		presoAlert.setText("");
		presoCopyright1 = (TextView) findViewById(R.id.presoCopyright1);
		presoCopyright1.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoCopyright1.setTextColor(lyricsTextColor);
		presoCopyright1.setTextSize(10);
		presoCopyright1.setTypeface(lyricsfont);
		presoCopyright1.setText(" ");
		presoCopyright2 = (TextView) findViewById(R.id.presoCopyright2);
		presoCopyright2.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoCopyright2.setTextColor(lyricsTextColor);
		presoCopyright2.setTextSize(10);
		presoCopyright2.setTypeface(lyricsfont);
		presoCopyright2.setText(" ");
		presoLogo = (ImageView) findViewById(R.id.presoLogo);

		presoLyrics1.setPadding(FullscreenActivity.xmargin_presentation, FullscreenActivity.ymargin_presentation, FullscreenActivity.xmargin_presentation, 0);
		presoLyrics2.setPadding(FullscreenActivity.xmargin_presentation, FullscreenActivity.ymargin_presentation, FullscreenActivity.xmargin_presentation, 0);
		presoLogo.setPadding(FullscreenActivity.xmargin_presentation, FullscreenActivity.ymargin_presentation, FullscreenActivity.xmargin_presentation, FullscreenActivity.ymargin_presentation);
		bottomBit.setPadding(FullscreenActivity.xmargin_presentation, 0, FullscreenActivity.xmargin_presentation, FullscreenActivity.ymargin_presentation);
		LayoutParams params = preso.getLayoutParams();
		params.width = metrics.widthPixels;
		params.height = metrics.heightPixels;
		preso.setLayoutParams(params);

		defimage = getResources().getDrawable(R.drawable.preso_default_bg);
		fixBackground();

		// Set a listener for the presoLyrics to listen for size changes
		// This is used for the scale
		presoLyricsIN = presoLyrics1;
		presoAuthorIN = presoAuthor1;
		presoTitleIN = presoTitle1;
		presoCopyrightIN = presoCopyright1;

		ViewTreeObserver vto = presoLyricsIN.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				// Get the width and height of this text
				screenwidth = lyricsHolder.getWidth();
				textwidth = presoLyricsIN.getWidth();
				screenheight = lyricsHolder.getHeight();
				textheight = presoLyricsIN.getHeight();
				if (PresenterMode.autoscale) {
					doScale();
				}
			}

		});
	}

	public static void UpDatePresentation() {
		Log.d("MyPresentation", "upDatePresentation");
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
		} else {
			yscale = xscale;
		}
		presoLyricsIN.setScaleX(xscale);
		presoLyricsIN.setScaleY(yscale);
	}

	public static void fadeoutCopyright1() {
		Log.d("MyPresentation", "fadeoutCopyright1");
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
		Log.d("MyPresentation", "fadeoutCopyright2");
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
		Log.d("MyPresentation", "fadeoutTitle1");
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
		Log.d("MyPresentation", "fadeoutTitle2");
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
		Log.d("MyPresentation", "fadeoutAuthor1");
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
		Log.d("MyPresentation", "fadeoutAuthor2");
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
		Log.d("MyPresentation", "fadeoutLyrics2");
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
		Log.d("MyPresentation", "fadeoutLyrics1");
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
		Log.d("MyPresentation", "fadeinLogo");
		// If logo is invisible or faded out, fade it in
		if (presoLogo.getVisibility() == View.INVISIBLE || presoLogo.getVisibility() == View.INVISIBLE || presoLogo.getAlpha() == 0f) {
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

		PresenterMode.logo_on = "Y";

	}

	public static void fadeOutLogo() {
		Log.d("MyPresentation", "fadeOutLogo");
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
		Log.d("MyPresentation", "crossFadeSong");
		// There are two views for each song element (presoLyrics, presoAuthor, presoCopyright, presoOther)
		// This is to allow for smooth crossfading.

		// If the logo is showing, fade it out then hide it
		if (presoLogo.getVisibility() == View.VISIBLE) {
			fadeOutLogo();
		}

		// If the user is on a blank screen need to fade back in the background image or video
		if (preso.getVisibility() == View.INVISIBLE || preso.getVisibility() == View.GONE) {
			fadeInPage();
		}

		// Decide which view we are fading in.  By default its the 1st view
		whichPresoLyricsToUse = 1;
		presoLyricsIN = presoLyrics1;
		presoLyricsOUT = presoLyrics2;
		presoTitleIN = presoTitle1;
		presoTitleOUT = presoTitle2;
		presoAuthorIN = presoAuthor1;
		presoAuthorOUT = presoAuthor2;
		presoCopyrightIN = presoCopyright1;
		presoCopyrightOUT = presoCopyright2;

		if (presoLyrics1.getVisibility() == View.VISIBLE) {
			// 1st is on already, so we are fading in the 2nd view
			whichPresoLyricsToUse = 2;
			presoLyricsIN = presoLyrics2;
			presoLyricsOUT = presoLyrics1;
			presoTitleIN = presoTitle2;
			presoTitleOUT = presoTitle1;
			presoAuthorIN = presoAuthor2;
			presoAuthorOUT = presoAuthor1;
			presoCopyrightIN = presoCopyright2;
			presoCopyrightOUT = presoCopyright1;
		}

		// Make sure the visibilities and alphas of the fade in view
		presoLyricsIN.setAlpha(0.0f);
		presoLyricsIN.setVisibility(View.VISIBLE);

		// Set the text of the view that is being faded in
		presoLyricsIN.setText(PresenterMode.buttonPresentText);
		presoLyricsIN.setScaleX(1);
		presoLyricsIN.setScaleY(1);

		// Animate the view in
		presoLyricsIN.animate().alpha(1f).setDuration(1000).setListener(null);

		// Animate the other view out - ONLY IF IT IS VISIBLE
		if (presoLyricsOUT.getVisibility() == View.VISIBLE) {
			presoLyricsOUT.setAlpha(1.0f);
			presoLyricsOUT.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					presoLyricsOUT.setVisibility(View.INVISIBLE);
				}
			});
		} else {
			// Just hide the out one if wasn't already visible
			presoLyricsOUT.setAlpha(0.0f);
			presoLyricsOUT.setVisibility(View.INVISIBLE);
		}


		// Now we can do the same to the title, author, copyright and other fields
		// We only need to crossfade if the contents have changed (i.e. a different song).
		// Otherwise just switch them over
		presoTitleIN.setText(PresenterMode.presoTitle);
		presoAuthorIN.setText(PresenterMode.presoAuthor);
		presoCopyrightIN.setText(PresenterMode.presoCopyright);

		if (presoTitleOUT.getVisibility() == View.VISIBLE && presoTitleOUT.getAlpha() > 0 && !presoTitleOUT.getText().toString().equals(PresenterMode.presoTitle)) {
			presoTitleOUT.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					presoTitleOUT.setVisibility(View.INVISIBLE);

					presoTitleIN.setAlpha(0f);
					presoTitleIN.setVisibility(View.VISIBLE);
					presoTitleIN.animate().alpha(1f).setDuration(1000).setListener(null);
				}
			});
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

					presoAuthorIN.setAlpha(0f);
					presoAuthorIN.setVisibility(View.VISIBLE);
					presoAuthorIN.animate().alpha(1f).setDuration(1000).setListener(null);
				}
			});
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

					presoCopyrightIN.setAlpha(0f);
					presoCopyrightIN.setVisibility(View.VISIBLE);
					presoCopyrightIN.animate().alpha(1f).setDuration(1000).setListener(null);
				}
			});
		} else {
			presoCopyrightOUT.setAlpha(0f);
			presoCopyrightOUT.setVisibility(View.INVISIBLE);

			presoCopyrightIN.setAlpha(1f);
			presoCopyrightIN.setVisibility(View.VISIBLE);
		}
	}

	public static void fadeInPage() {
		Log.d("MyPresentation", "fadeInPage");

		// Simply fade in preso
		PresenterMode.blackout = "N";
		// Switch the logo button back off in case
		PresenterMode.logo_on = "N";
		presoLogo.setVisibility(View.GONE);
		preso.setAlpha(0f);
		preso.setVisibility(View.VISIBLE);
		preso.animate().alpha(1f).setDuration(1000).setListener(null);
	}

	public static void fadeOutPage() {
		Log.d("MyPresentation", "fadeOutPage");

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
		});
	}

	public static void fixBackground() {
		Log.d("MyPresentation", "fixBackground");
		File img1File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage1);
		File img2File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage2);
		String vid1File = FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo1;
		String vid2File = FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo2;
		// Decide if user is using video or image for background
		if (FullscreenActivity.backgroundTypeToUse.equals("image")) {
			File imgFile;
			if (FullscreenActivity.backgroundToUse.equals("img1")) {
				imgFile = img1File;
			} else {
				imgFile = img2File;
			}
			//videoBackground.stopPlayback();
			videoBackground.setVideoURI(null);
			videoBackground.setVisibility(View.GONE);
			if (imgFile.exists()) {
				if (imgFile.toString().contains("ost_bg.png")) {
					presoBGImage.setImageDrawable(defimage);
				} else {
					myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
					dr = new BitmapDrawable(myBitmap);
					presoBGImage.setImageDrawable(dr);
				}
				presoBGImage.setVisibility(View.VISIBLE);
			}
		} else if (FullscreenActivity.backgroundTypeToUse.equals("video")) {
			final String vidFile;
			if (FullscreenActivity.backgroundToUse.equals("vid1")) {
				vidFile = vid1File;
			} else {
				vidFile = vid2File;
			}
			Uri videoUri = Uri.parse(vidFile);
			//videoBackground.stopPlayback();
			//videoBackground.setVideoURI(videoUri);
			videoBackground.setVideoPath(vidFile);
			videoBackground.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.e("VideoView", "Error playing video");
					mp.stop();
					mp.reset();
					mp.release();
					return false;
				}
			});
			preso.setBackgroundColor(0xff000000);
			myBitmap = null;
			dr = null;
			presoBGImage.setImageDrawable(null);
			presoBGImage.setVisibility(View.GONE);
			videoBackground.setVisibility(View.VISIBLE);
			videoBackground.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.reset();
					videoBackground.setVideoPath(vidFile);
					videoBackground.start();
				}
			});

			videoBackground.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {
					//mp.setLooping(true);
					videoBackground.start();
				}
			});
		} else {
			videoBackground.stopPlayback();
			videoBackground.setVideoURI(null);
			videoBackground.setVisibility(View.GONE);
			preso.setBackgroundColor(0xff000000);
			myBitmap = null;
			dr = null;
			presoBGImage.setImageDrawable(null);
			presoBGImage.setVisibility(View.GONE);
		}


	}

	public static void blackoutPresentation() {
		Log.d("MyPresentation", "blackoutPresentation");
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
		presoLyrics1.setTextSize(PresenterMode.tempfontsize);
		presoLyrics2.setTextSize(PresenterMode.tempfontsize);
	}

	public static void changeMargins() {
		// Get width and height
		preso.setPadding(PresenterMode.tempxmargin, PresenterMode.tempymargin, PresenterMode.tempxmargin, PresenterMode.tempymargin);
		if (PresenterMode.autoscale) {
			doScale();
		}
	}
}
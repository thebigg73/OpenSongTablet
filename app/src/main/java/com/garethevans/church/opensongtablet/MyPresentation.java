package com.garethevans.church.opensongtablet;

import java.io.File;

import android.annotation.TargetApi;
import android.app.Presentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public final class MyPresentation extends Presentation {

	// Views on the presentation window
	static TextView presoLyrics;
	static TextView presoAuthor;
	static TextView presoCopyright;
	static TextView presoTitle;
	static TextView presoOther;
	static ImageView presoLogo;
	static LinearLayout bottomBit;
	static FrameLayout preso;
	static VideoView videoBackground;
	static Animation animationFadeIn;
	static Animation animationFadeOut;
	static Drawable dr;
	static Bitmap myBitmap;
	int lyricsTextColor = FullscreenActivity.dark_lyricsTextColor;
	int lyricsShadowColor = FullscreenActivity.dark_lyricsBackgroundColor;
	Typeface lyricsfont;
	static int myWidth;
	static int myHeight;

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
		bottomBit = (LinearLayout) findViewById(R.id.bottomBit);
		
		preso.setPadding(FullscreenActivity.xmargin_presentation,FullscreenActivity.ymargin_presentation,FullscreenActivity.xmargin_presentation,FullscreenActivity.ymargin_presentation);
		LayoutParams params=preso.getLayoutParams();
		params.width=metrics.widthPixels;
		params.height=metrics.heightPixels;
		preso.setLayoutParams(params);
		

		videoBackground = (VideoView) findViewById(R.id.videoBackground);
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

		// Temp override
		lyricsfont = Typeface.createFromAsset(getContext().getAssets(), "fonts/FiraSansOT-Regular.otf");

		presoLyrics = (TextView) findViewById(R.id.presoLyrics);
		presoLyrics.setTextColor(0xffffffff);
		presoLyrics.setTypeface(lyricsfont);
		presoLyrics.setTextSize(18);
		presoLyrics.setShadowLayer(25.0f, -5 , 5, lyricsShadowColor);
		presoTitle = (TextView) findViewById(R.id.presoTitle);
		presoTitle.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoTitle.setTextColor(lyricsTextColor);
		presoTitle.setTextSize(20);
		presoTitle.setTypeface(lyricsfont);
		presoAuthor = (TextView) findViewById(R.id.presoAuthor);
		presoAuthor.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoAuthor.setTextColor(lyricsTextColor);
		presoAuthor.setTextSize(16);
		presoAuthor.setTypeface(lyricsfont);
		presoOther = (TextView) findViewById(R.id.presoOther);
		presoOther.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoOther.setTextColor(lyricsTextColor);
		presoOther.setTextSize(16);
		presoOther.setTypeface(lyricsfont);
		presoCopyright = (TextView) findViewById(R.id.presoCopyright);
		presoCopyright.setShadowLayer(25.0f, -5, 5, lyricsShadowColor);
		presoCopyright.setTextColor(lyricsTextColor);
		presoCopyright.setTextSize(16);
		presoCopyright.setTypeface(lyricsfont);
		presoLogo = (ImageView) findViewById(R.id.presoLogo);
		File logoFile = new  File(FullscreenActivity.dirbackgrounds + "/ost_logo.png");
		if(logoFile.exists()){
			Bitmap myBitmap = BitmapFactory.decodeFile(logoFile.getAbsolutePath());
			presoLogo.setImageBitmap(myBitmap);			
		}

	}

	public static void fixBackground() {
		File img1File = new  File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage1);
		File img2File = new  File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage2);
		String vid1File = android.os.Environment.getExternalStorageDirectory().getPath()+"/documents/OpenSong/Backgrounds/"+FullscreenActivity.backgroundVideo1;
		String vid2File = android.os.Environment.getExternalStorageDirectory().getPath()+"/documents/OpenSong/Backgrounds/"+FullscreenActivity.backgroundVideo2;
		// Decide if user is using video or image for background
		if (FullscreenActivity.backgroundTypeToUse.equals("image")) {
			File imgFile;
			if (FullscreenActivity.backgroundToUse.equals("img1")) {
				imgFile = img1File;
			} else {
				imgFile = img2File;				
			}
			videoBackground.stopPlayback();
			videoBackground.setVisibility(View.INVISIBLE);
			if(imgFile.exists()){
				Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
				@SuppressWarnings("deprecation")
				Drawable dr = new BitmapDrawable(myBitmap);
				preso.setBackground(dr);
			}
		} else {
			String vidFile;
			if (FullscreenActivity.backgroundToUse.equals("vid1")) {
				vidFile = vid1File;
			} else {
				vidFile = vid2File;
			}
			Uri videoUri = Uri.parse(vidFile);
			videoBackground.stopPlayback();
			videoBackground.setVideoURI(videoUri);
			videoBackground.setOnErrorListener(new OnErrorListener () {
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
			videoBackground.setVisibility(View.VISIBLE);
			//videoBackground.start();
			videoBackground.setOnPreparedListener(new OnPreparedListener() {

	        @Override
	        public void onPrepared(MediaPlayer mp) {
	            // TODO Auto-generated method stub
	            mp.setLooping(true);
	            videoBackground.start();
	            //mp.start();
	            
	        }
	    });
		}

	}
	
	
	
	public static void UpDatePresentation() {
		// See what has changed and fade those bits out/in
		// Crossfade the views
		crossFadeSong();
	}

	
	
	public static void blackoutPresentation() {
		if (PresentMode.blackout.equals("Y")) {
			fadeOutPage();
		} else {
			fadeInPage();
		}			
	}
	
	
	
	
	
	
	public static void ShowLogo() {
		if (PresentMode.logo_on.equals("Y")) {
			if (presoLyrics.getText().toString().length()>0 || presoLyrics.getVisibility()==View.VISIBLE) {
				presoLyrics.startAnimation(AnimationUtils.loadAnimation(presoLyrics.getContext(),R.anim.project_fadeout));
			}
			if (presoTitle.getText().toString().length()>0 || presoTitle.getVisibility()==View.VISIBLE) {
				presoTitle.startAnimation(AnimationUtils.loadAnimation(presoTitle.getContext(),R.anim.project_fadeout));
			}
			if (presoAuthor.getText().toString().length()>0 || presoAuthor.getVisibility()==View.VISIBLE) {
				presoAuthor.startAnimation(AnimationUtils.loadAnimation(presoAuthor.getContext(),R.anim.project_fadeout));
			}
			if (presoCopyright.getText().toString().length()>0 || presoCopyright.getVisibility()==View.VISIBLE) {
				presoCopyright.startAnimation(AnimationUtils.loadAnimation(presoCopyright.getContext(),R.anim.project_fadeout));
			}
			if (presoOther.getText().toString().length()>0 || presoOther.getVisibility()==View.VISIBLE) {
				presoOther.startAnimation(AnimationUtils.loadAnimation(presoOther.getContext(),R.anim.project_fadeout));
			}
			//After 250ms make them invisible
				Handler delayfadeinredraw = new Handler();
				delayfadeinredraw.postDelayed(new Runnable() {
					@Override
					public void run() {
						presoLyrics.setVisibility(View.INVISIBLE);
						presoLyrics.setText("");
						presoTitle.setVisibility(View.INVISIBLE);
						presoTitle.setText("");
						presoAuthor.setVisibility(View.INVISIBLE);
						presoAuthor.setText("");
						presoCopyright.setVisibility(View.INVISIBLE);
						presoCopyright.setText("");
						presoOther.setVisibility(View.INVISIBLE);
						presoOther.setText("");
					}
					}, 250); // 250ms delay
			
			presoLogo.setVisibility(View.VISIBLE);
			//Fade in the logo
			presoLogo.startAnimation(AnimationUtils.loadAnimation(presoLogo.getContext(),R.anim.project_fadein));
		} else {
			//Fade out the logo
			presoLogo.startAnimation(AnimationUtils.loadAnimation(presoLogo.getContext(),R.anim.project_fadeout));

			//Fade in the other stuff
			presoLyrics.setVisibility(View.VISIBLE);
			presoTitle.setVisibility(View.VISIBLE);
			presoAuthor.setVisibility(View.VISIBLE);
			presoCopyright.setVisibility(View.VISIBLE);
			presoOther.setVisibility(View.VISIBLE);
			presoLyrics.startAnimation(AnimationUtils.loadAnimation(presoLyrics.getContext(),R.anim.project_fadein));
			presoTitle.startAnimation(AnimationUtils.loadAnimation(presoTitle.getContext(),R.anim.project_fadein));
			presoAuthor.startAnimation(AnimationUtils.loadAnimation(presoAuthor.getContext(),R.anim.project_fadein));
			presoCopyright.startAnimation(AnimationUtils.loadAnimation(presoCopyright.getContext(),R.anim.project_fadein));
			presoOther.startAnimation(AnimationUtils.loadAnimation(presoOther.getContext(),R.anim.project_fadein));

			
			//After 250ms make it invisible
			Handler delayfadeinredraw = new Handler();
			delayfadeinredraw.postDelayed(new Runnable() {
				@Override
				public void run() {
					presoLogo.setVisibility(View.INVISIBLE);
				}
				}, 250); // 250ms delay
		}

	}
	
	
	public static void fadeOutSong() {
		// If views are visible and not faded out, fade them out.  If they are invisible, do nothing
		if (presoTitle.getVisibility()==View.VISIBLE && presoTitle.getAlpha()>0) {
			presoTitle.startAnimation(AnimationUtils.loadAnimation(presoTitle.getContext(),R.anim.project_fadeout));			
		}
		if (presoLyrics.getVisibility()==View.VISIBLE && presoLyrics.getAlpha()>0) {
			presoLyrics.startAnimation(AnimationUtils.loadAnimation(presoLyrics.getContext(),R.anim.project_fadeout));			
		}
		if (presoAuthor.getVisibility()==View.VISIBLE && presoAuthor.getAlpha()>0) {
			presoAuthor.startAnimation(AnimationUtils.loadAnimation(presoAuthor.getContext(),R.anim.project_fadeout));			
		}
		if (presoCopyright.getVisibility()==View.VISIBLE && presoCopyright.getAlpha()>0) {
			presoCopyright.startAnimation(AnimationUtils.loadAnimation(presoCopyright.getContext(),R.anim.project_fadeout));			
		}
		if (presoOther.getVisibility()==View.VISIBLE && presoOther.getAlpha()>0) {
			presoOther.startAnimation(AnimationUtils.loadAnimation(presoOther.getContext(),R.anim.project_fadeout));			
		}
		// After 250ms, make the views invisible
		Handler delayinvisible = new Handler();
		delayinvisible.postDelayed(new Runnable() {
			@Override
			public void run() {
				presoLyrics.setVisibility(View.INVISIBLE);
				presoTitle.setVisibility(View.INVISIBLE);
				presoAuthor.setVisibility(View.INVISIBLE);
				presoCopyright.setVisibility(View.INVISIBLE);
				presoOther.setVisibility(View.INVISIBLE);
			}
			}, 250); // 250ms delay		
	}
	
	
	
	public static void fadeOutPage() {
	
		if (FullscreenActivity.backgroundTypeToUse.equals("video")) {
			// Make sure the background of preso is black
			preso.setBackgroundColor(0xff000000);
			// Fade out the video
			videoBackground.startAnimation(AnimationUtils.loadAnimation(videoBackground.getContext(),R.anim.project_fadeout));
		} else {
		preso.startAnimation(AnimationUtils.loadAnimation(preso.getContext(),R.anim.project_fadeout));
		}
		// After 250ms, make the views invisible
		Handler delayinvisible = new Handler();
		delayinvisible.postDelayed(new Runnable() {
			@Override
			public void run() {
				//presoLyrics.setAlpha(0.0f);
				presoLyrics.setVisibility(View.INVISIBLE);
				//presoTitle.setAlpha(0.0f);
				presoTitle.setVisibility(View.INVISIBLE);
				//presoAuthor.setAlpha(0.0f);
				presoAuthor.setVisibility(View.INVISIBLE);
				//presoCopyright.setAlpha(0.0f);
				presoCopyright.setVisibility(View.INVISIBLE);
				//presoOther.setAlpha(0.0f);
				presoOther.setVisibility(View.INVISIBLE);
				//videoBackground.setAlpha(0.0f);
				videoBackground.setVisibility(View.INVISIBLE);
				if (FullscreenActivity.backgroundTypeToUse.equals("image")) {
				//	preso.setAlpha(0.0f);
				}
				preso.setVisibility(View.INVISIBLE);
			}
			}, 250); // 250ms delay
	}
	
	
	public static void fadeInPage() {
		if (FullscreenActivity.backgroundTypeToUse.equals("video")) {
			// Fade in the video
			videoBackground.setVisibility(View.VISIBLE);
			videoBackground.startAnimation(AnimationUtils.loadAnimation(videoBackground.getContext(),R.anim.project_fadein));
		}
		preso.setVisibility(View.VISIBLE);
		fixBackground();
		preso.startAnimation(AnimationUtils.loadAnimation(preso.getContext(),R.anim.project_fadein));
		//presoLogo.setVisibility(View.VISIBLE);
		//presoLogo.setAlpha(1.0f);
		//PresentMode.logo_on="Y";
		//Fade in the other stuff
		presoLyrics.setText("");
		presoTitle.setText("");
		presoAuthor.setText("");
		presoCopyright.setText("");
		presoOther.setText("");
		presoLyrics.setVisibility(View.VISIBLE);
		presoTitle.setVisibility(View.VISIBLE);
		presoAuthor.setVisibility(View.VISIBLE);
		presoCopyright.setVisibility(View.VISIBLE);
		presoOther.setVisibility(View.VISIBLE);
		presoLyrics.startAnimation(AnimationUtils.loadAnimation(presoLyrics.getContext(),R.anim.project_fadein));
		presoTitle.startAnimation(AnimationUtils.loadAnimation(presoTitle.getContext(),R.anim.project_fadein));
		presoAuthor.startAnimation(AnimationUtils.loadAnimation(presoAuthor.getContext(),R.anim.project_fadein));
		presoCopyright.startAnimation(AnimationUtils.loadAnimation(presoCopyright.getContext(),R.anim.project_fadein));
		presoOther.startAnimation(AnimationUtils.loadAnimation(presoOther.getContext(),R.anim.project_fadein));

		
	}
	
	public static void fadeInSong() {
		// If views are invisible or are faded out, fade them in.
		if (presoTitle.getVisibility()==View.INVISIBLE ||  presoTitle.getAlpha()<1) {
			presoTitle.setAlpha(0.0f);
			presoTitle.startAnimation(AnimationUtils.loadAnimation(presoTitle.getContext(),R.anim.project_fadein));			
		} else {
			presoTitle.setVisibility(View.VISIBLE);
			presoTitle.setAlpha(1.0f);
		}
		if (presoLyrics.getVisibility()==View.INVISIBLE || presoLyrics.getAlpha()<1) {
			presoLyrics.setAlpha(0.0f);
			presoLyrics.startAnimation(AnimationUtils.loadAnimation(presoLyrics.getContext(),R.anim.project_fadein));			
		} else {
			presoLyrics.setVisibility(View.VISIBLE);
			presoLyrics.setAlpha(1.0f);
		}
		if (presoAuthor.getVisibility()==View.INVISIBLE || presoAuthor.getAlpha()<1) {
			presoAuthor.setAlpha(0.0f);
			presoAuthor.startAnimation(AnimationUtils.loadAnimation(presoAuthor.getContext(),R.anim.project_fadein));			
		} else {
			presoAuthor.setVisibility(View.VISIBLE);
			presoAuthor.setAlpha(1.0f);
		}
		if (presoCopyright.getVisibility()==View.INVISIBLE || presoCopyright.getAlpha()<1) {
			presoCopyright.setAlpha(0.0f);
			presoCopyright.startAnimation(AnimationUtils.loadAnimation(presoCopyright.getContext(),R.anim.project_fadein));			
		} else {
			presoCopyright.setVisibility(View.VISIBLE);
			presoCopyright.setAlpha(1.0f);
		}
		if (presoOther.getVisibility()==View.INVISIBLE || presoOther.getAlpha()<1) {
			presoOther.setAlpha(0.0f);
			presoOther.startAnimation(AnimationUtils.loadAnimation(presoOther.getContext(),R.anim.project_fadein));			
		} else {
			presoOther.setVisibility(View.VISIBLE);
			presoOther.setAlpha(1.0f);
		}
	}
	
	public static void fadeOutLogo() {
		// If logo is visible and not faded out, fade it out
		if (presoLogo.getVisibility()==View.VISIBLE &&  presoLogo.getAlpha()>00) {
			presoLogo.startAnimation(AnimationUtils.loadAnimation(presoLogo.getContext(),R.anim.project_fadeout));
		}
		// After 250ms, make the logo invisible
		Handler delayinvisible = new Handler();
		delayinvisible.postDelayed(new Runnable() {
			@Override
			public void run() {
				presoLogo.setVisibility(View.INVISIBLE);
			}
			}, 250); // 250ms delay		
		PresentMode.logo_on="N";
	}


	
	public void fadeInLogo() {
		// If logo is invisible or faded out, fade it in
		if (presoLogo.getVisibility()==View.INVISIBLE ||  presoLogo.getAlpha()<1) {
			presoLogo.setAlpha(0.0f);
			presoLogo.setVisibility(View.VISIBLE);
			presoLogo.startAnimation(AnimationUtils.loadAnimation(presoLogo.getContext(),R.anim.project_fadein));			
		} else {
			presoLogo.setAlpha(1.0f);
			presoLogo.setVisibility(View.VISIBLE);
		}
		PresentMode.logo_on="Y";
	}


	public static void crossFadeSong() {
		// If logo is being shown.... fade it out
		if (presoLogo.getVisibility()==View.VISIBLE) {
			fadeOutLogo();
		}
		// If views are visible and not faded out and are needing changed, fade them out. 
		// If invisible, make changes and run fade in.
		if (presoTitle.getVisibility()==View.VISIBLE && presoTitle.getAlpha()>0 && !presoTitle.getText().toString().equals(PresentMode.presoTitle)) {
			presoTitle.startAnimation(AnimationUtils.loadAnimation(presoTitle.getContext(),R.anim.project_fadeout));			
		}
		if (presoLyrics.getVisibility()==View.VISIBLE && presoLyrics.getAlpha()>0 && !presoLyrics.getText().toString().equals(PresentMode.buttonPresentText)) {
			presoLyrics.startAnimation(AnimationUtils.loadAnimation(presoLyrics.getContext(),R.anim.project_fadeout));			
			// After 250ms, change the text as required and fade the changed stuff back in.
			Handler delayscale = new Handler();
			delayscale.postDelayed(new Runnable() {
				@Override
				public void run() {
					//presoLyrics.setScaleX(1);
					}
			}, 250); // 250ms delay		
		}
		if (presoAuthor.getVisibility()==View.VISIBLE && presoAuthor.getAlpha()>0 && !presoAuthor.getText().toString().equals(PresentMode.presoAuthor)) {
			presoAuthor.startAnimation(AnimationUtils.loadAnimation(presoAuthor.getContext(),R.anim.project_fadeout));			
		}
		if (presoCopyright.getVisibility()==View.VISIBLE && presoCopyright.getAlpha()>0 && !presoCopyright.getText().toString().equals(PresentMode.presoCopyright)) {
			presoCopyright.startAnimation(AnimationUtils.loadAnimation(presoCopyright.getContext(),R.anim.project_fadeout));			
		}
		if (presoOther.getVisibility()==View.VISIBLE && presoOther.getAlpha()>0 && !presoOther.getText().toString().equals(PresentMode.presoOther)) {
			presoOther.startAnimation(AnimationUtils.loadAnimation(presoOther.getContext(),R.anim.project_fadeout));			
		}
		

		// After 250ms, change the text as required and fade the changed stuff back in.
		Handler delayfadein = new Handler();
		delayfadein.postDelayed(new Runnable() {
			@Override
			public void run() {
				// Change the text 
				if (!presoTitle.getText().toString().equals(PresentMode.presoTitle)) {
					presoTitle.setAlpha(1.0f);
					presoTitle.setVisibility(View.VISIBLE);
					presoTitle.setText(PresentMode.presoTitle);
					presoTitle.startAnimation(AnimationUtils.loadAnimation(presoTitle.getContext(),R.anim.project_fadein));								
				}

				if (!presoLyrics.getText().toString().equals(PresentMode.buttonPresentText)) {
					presoLyrics.setAlpha(1.0f);
					presoLyrics.setVisibility(View.VISIBLE);
					presoLyrics.setText(PresentMode.buttonPresentText);
					presoLyrics.measure(0, 0);       //must call measure!
					float templyricswidth = presoLyrics.getMeasuredWidth(); //get width
					float templyricsheight = presoLyrics.getMeasuredHeight();
					float tempbottombitheight = bottomBit.getMeasuredHeight();
					float temppresowidth = preso.getMeasuredWidth() - 2*FullscreenActivity.xmargin_presentation;
					float temppresoheight = preso.getMeasuredHeight() - tempbottombitheight - 2*FullscreenActivity.ymargin_presentation;
					float xscale = temppresowidth/templyricswidth;
					float yscale = temppresoheight/templyricsheight;
					
					if (xscale>yscale) {
						presoLyrics.setScaleX(yscale);
						presoLyrics.setScaleY(yscale);
					} else {
						presoLyrics.setScaleX(xscale);
						presoLyrics.setScaleY(xscale);			
					}
					
					
					
					presoLyrics.startAnimation(AnimationUtils.loadAnimation(presoLyrics.getContext(),R.anim.project_fadein));			
				}
				if (!presoAuthor.getText().toString().equals(PresentMode.presoAuthor)) {
					presoAuthor.setAlpha(1.0f);
					presoAuthor.setVisibility(View.VISIBLE);
					presoAuthor.setText(PresentMode.presoAuthor);
					presoAuthor.startAnimation(AnimationUtils.loadAnimation(presoAuthor.getContext(),R.anim.project_fadein));			
				}
				if (!presoCopyright.getText().toString().equals(PresentMode.presoCopyright)) {
					presoCopyright.setAlpha(1.0f);
					presoCopyright.setVisibility(View.VISIBLE);
					presoCopyright.setText(PresentMode.presoCopyright);
					presoCopyright.startAnimation(AnimationUtils.loadAnimation(presoCopyright.getContext(),R.anim.project_fadein));			
				}
				if (!presoOther.getText().toString().equals(PresentMode.presoOther)) {
					presoOther.setAlpha(1.0f);
					presoOther.setVisibility(View.VISIBLE);
					presoOther.setText(PresentMode.presoOther);
					presoOther.startAnimation(AnimationUtils.loadAnimation(presoOther.getContext(),R.anim.project_fadein));			
				}
			}
			}, 250); // 250ms delay		
	}

	public static void changeMargins() {
		// Get width and height
		preso.setPadding(PresentMode.tempxmargin,PresentMode.tempymargin,PresentMode.tempxmargin,PresentMode.tempymargin);
		presoLyrics.measure(0, 0);       //must call measure!
		float templyricswidth = presoLyrics.getMeasuredWidth(); //get width
		float templyricsheight = presoLyrics.getMeasuredHeight();
		float tempbottombitheight = bottomBit.getMeasuredHeight();
		float temppresowidth = preso.getMeasuredWidth() - 2*PresentMode.tempxmargin;
		float temppresoheight = preso.getMeasuredHeight() - tempbottombitheight - 2*PresentMode.tempymargin;
		float xscale = temppresowidth/templyricswidth;
		float yscale = temppresoheight/templyricsheight;
	
		if (xscale>yscale) {
			presoLyrics.setScaleX(yscale);
			presoLyrics.setScaleY(yscale);
		} else {
			presoLyrics.setScaleX(xscale);
			presoLyrics.setScaleY(xscale);			
		}

	}
	
}
package com.garethevans.church.opensongtablet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParserException;

import android.widget.ExpandableListView;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.VideoView;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class PresentMode extends Activity {

	// Set the variables used here
	static SharedPreferences myPreferences;
	Display display;
	Context context;
	static int numdisplays;
	static boolean firsttime = true;

	
	Display[] presentationDisplays;
	DisplayManager displayManager;
	
	
	//Buttons
	public static ImageView image1_button;
	public static ImageView image2_button;
	public static VideoView video1_button;
	public static VideoView video2_button;
	public static ToggleButton image1_switch;
	public static ToggleButton image2_switch;
	public static ToggleButton video1_switch;
	public static ToggleButton video2_switch;
	public static LinearLayout updateProject_button;
	public static LinearLayout blackoutProject_button;
	public static LinearLayout logoProject_button;
	public static LinearLayout setMargins_button;
	public static LinearLayout chooseImage1;
	public static LinearLayout chooseImage2;
	public static LinearLayout chooseVideo1;
	public static LinearLayout chooseVideo2;
	public static int tempxmargin;
	public static int tempymargin;


	static boolean bVideo1IsBeingTouched = false;
	static boolean bVideo2IsBeingTouched = false;
	static Handler mHandler;
	
	// Keep a note of what is shown
	//static String background="image";
	static String song_on="N";
	static String logo_on="Y";
	static String blackout="N";
	
	
	// Song
	public static Drawable bgroundImage1; //saved for access from MyPresentation
	public static Drawable bgroundImage2; //saved for access from MyPresentation
	public static File bgVideo1; //saved for access from MyPresentation
	public static File bgVideo2; //saved for access from MyPresentation//saved for access from MyPresentation

	static int whichvideobgpressd;
	
	static String whatBackgroundLoaded;

	AlertDialog.Builder dialogBuilder;

	static ArrayList<String> tempBackgroundImageFiles;
	static ArrayList<String> tempBackgroundVideoFiles;
	static String[] BackgroundImageFiles;
	static String[] BackgroundVideoFiles;
	
	static String tempLyrics;
	static String[] tempLyricsLineByLine;
	static String[] songSections;
	static String[] songSectionsLabels;
	static String buttonText;
	static String buttonPresentText;
	static Button songButton;
	static Button setButton;
	public static TextView presenterviewtitle;
	DrawerLayout mDrawerLayout;
	ExpandableListView expListViewSong;
	ExpandableListView expListViewOption;
	// private Context main_context;
	public static Context presWindow;

	// Song values
	// Lyrics are previewed in the EditText box before being presented
	public static EditText songProjectText;
	public static String presoAuthor;
	public static String presoTitle;
	public static String presoCopyright;
	public static String presoOther;

	@SuppressWarnings("unused")
	private ActionBarDrawerToggle actionBarDrawerToggle;
	MenuItem set_back;
	MenuItem set_forward;
	@SuppressWarnings("unused")
	private Menu menu;
	LinearLayout projectButton;
	LinearLayout logoButton;
	MediaRouter mMediaRouter;
	private ArrayList<String> listDataHeaderSong;
	private HashMap<String, List<String>> listDataChildSong;
	private ExpandableListAdapter listAdapterSong;
	private boolean addingtoset;
	private ArrayList<String> listDataHeaderOption;
	private HashMap<String, List<String>> listDataChildOption;
	private ExpandableListAdapter listAdapterOption;
	protected int lastExpandedGroupPositionOption;
	protected boolean removingfromset;
	protected int myOptionListClickedItem;
	protected View view;
	protected String linkclicked;
	protected String chord_converting;
	protected View main_page;
	protected View main_lyrics;
	private boolean isPDF;
	
	@SuppressLint("ClickableViewAccessibility")
	@SuppressWarnings({ "unused" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// main_context = this.getApplicationContext();

		// Load up the user preferenceswrap_content
		myPreferences = getPreferences(MODE_PRIVATE);
		Preferences.loadPreferences();

		// Try language locale change
	    if (!FullscreenActivity.languageToLoad.isEmpty()) {
	    	Locale locale = null;
	    	locale = new Locale(FullscreenActivity.languageToLoad);
	 	    Locale.setDefault(locale);
		    Configuration config = new Configuration();
		    config.locale = locale;
		    getBaseContext().getResources().updateConfiguration(config, 
		      getBaseContext().getResources().getDisplayMetrics());
	    }

		// Load the songs
		ListSongFiles.listSongs();

		// Get the song indexes
		ListSongFiles.getCurrentSongIndex();

		// Load the current song and Prepare it
		isPDF = false;
		File checkfile = new File(FullscreenActivity.dir+"/"+FullscreenActivity.songfilename);
		if ((FullscreenActivity.songfilename.contains(".pdf") || FullscreenActivity.songfilename.contains(".PDF")) && checkfile.exists()) {
			// File is pdf
			isPDF = true;
		}
		
		if (!isPDF) {
		try {
			LoadXML.loadXML();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		} else {
			FullscreenActivity.mLyrics = getResources().getString(R.string.pdf_functionnotavailable);
			FullscreenActivity.mTitle = FullscreenActivity.songfilename;
			FullscreenActivity.mAuthor = "";
			Preferences.savePreferences();
		}
		
		PresentPrepareSong.splitSongIntoSections();
		FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
		getActionBar().setTitle(getResources().getText(R.string.presentermode).toString());
		setContentView(R.layout.activity_presenter_view);

		// Identify the buttons
		image1_button = (ImageView) findViewById(R.id.bgimage1);
		image2_button = (ImageView) findViewById(R.id.bgimage2);
		video1_button = (VideoView) findViewById(R.id.bgvideo1);
		video2_button = (VideoView) findViewById(R.id.bgvideo2);
		image1_switch = (ToggleButton) findViewById(R.id.toggleButton1);
		image2_switch = (ToggleButton) findViewById(R.id.toggleButton2);
		video1_switch = (ToggleButton) findViewById(R.id.toggleVideo1);
		video2_switch = (ToggleButton) findViewById(R.id.toggleVideo2);
		blackoutProject_button = (LinearLayout) findViewById(R.id.blackScreen);
		setMargins_button = (LinearLayout) findViewById(R.id.setMargins);
		chooseImage1 = (LinearLayout) findViewById(R.id.chooseImage1);
		chooseImage2 = (LinearLayout) findViewById(R.id.chooseImage2);
		chooseVideo1 = (LinearLayout) findViewById(R.id.chooseVideo1);
		chooseVideo2 = (LinearLayout) findViewById(R.id.chooseVideo2);
		
		if (FullscreenActivity.backgroundToUse.equals("img1")) {
			image1_switch.setChecked(true);
		} else if (FullscreenActivity.backgroundToUse.equals("img2")) {
			image2_switch.setChecked(true);
		} else if (FullscreenActivity.backgroundToUse.equals("vid1")) {
			video1_switch.setChecked(true);
		} else if (FullscreenActivity.backgroundToUse.equals("vid2")) {
			video2_switch.setChecked(true);
		}

		// Set listeners for videoview buttons
		mHandler = new Handler();

		video1_button.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(v.getId()==findViewById(R.id.bgvideo1).getId() && event.getAction() == MotionEvent.ACTION_UP){
					whichvideobgpressd = 1;
					whatBackgroundLoaded = "video1";
				    chooseFile();
			    }
				return true;
			}
		});

		video2_button.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(v.getId()==findViewById(R.id.bgvideo2).getId() && event.getAction() == MotionEvent.ACTION_UP){
					whichvideobgpressd = 2;
					whatBackgroundLoaded = "video2";
				    chooseFile();
			    }
				return true;
			}
		});

		
		// Prepare the image/video background buttons
		File img1File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage1);
		File img2File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage2);
		File vid1File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo1);
		File vid2File = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundVideo2);
		
		if (img1File.isFile()) {
			//Ok file exists.  Try to load it (but beware of errors!
			Bitmap bitmap1 = BitmapFactory.decodeFile(img1File.getAbsolutePath());
			Drawable bgImage1 = new BitmapDrawable(bitmap1);
			image1_button.setImageBitmap(bitmap1);
			
		} else {
			//Ok file doesn't exist.  Use the default icon
			Drawable bgImage1 = getResources().getDrawable(R.drawable.ic_action_picture);
			image1_button.setImageResource(R.drawable.ic_action_picture);
		}

		if (img2File.isFile()) {
			//Ok file exists.  Try to load it (but beware of errors!
			Bitmap bitmap2 = BitmapFactory.decodeFile(img2File.getAbsolutePath());
			Drawable bgImage2 = new BitmapDrawable(bitmap2);
			image2_button.setImageBitmap(bitmap2);
			
		} else {
			//Ok file doesn't exist.  Use the default icon
			Drawable bgImage2 = getResources().getDrawable(R.drawable.ic_action_picture);
			image2_button.setImageResource(R.drawable.ic_action_picture);
		}


		if (vid1File.isFile()) {
			//Ok file exists.  Try to load it (but beware of errors!
			String bgvid1 = android.os.Environment.getExternalStorageDirectory().getPath()+"/documents/OpenSong/Backgrounds/"+FullscreenActivity.backgroundVideo1;
			Uri videoUri = Uri.parse(bgvid1);
			video1_button.setVideoURI(videoUri);
			video1_button.seekTo(100);
		} else {
			// Do nothing - no video, so background image shows instead
		}

		if (vid2File.isFile()) {
			//Ok file exists.  Try to load it (but beware of errors!
			String bgvid2 = android.os.Environment.getExternalStorageDirectory().getPath()+"/documents/OpenSong/Backgrounds/"+FullscreenActivity.backgroundVideo2;
			Uri videoUri = Uri.parse(bgvid2);
			video2_button.setVideoURI(videoUri);
			video2_button.seekTo(100);
		} else {
			// Do nothing - no video, so background image shows instead
		}

		
		songProjectText = (EditText) findViewById(R.id.songProjectText);

		// Set up the navigation drawer
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		expListViewSong = (ExpandableListView) findViewById(R.id.song_list_ex);
		expListViewOption = (ExpandableListView) findViewById(R.id.option_list_ex);

		prepareSongMenu();
		prepareOptionMenu();

		// What happens when the navigation drawers are opened
		actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {

			// Called when a drawer has settled in a completely closed state.
			@Override
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}

			// Called when a drawer has settled in a completely open state.
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
			}
		};


		setupSongButtons();
		setupSetButtons();

		invalidateOptionsMenu();

		projectButton = (LinearLayout) findViewById(R.id.doProject);
		logoButton = (LinearLayout) findViewById(R.id.showLogo);
		
		updateDisplays();
		
	}

	
	
	
	
	public void updateDisplays() {
		// DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
		displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);

//		Display[] presentationDisplays = displayManager
//				.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);

		presentationDisplays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);

		// Get the media router service.
        mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);

		numdisplays = presentationDisplays.length;
		if (numdisplays==0) {
			if (firsttime) {
				firsttime = false;
				FullscreenActivity.myToastMessage = getResources().getText(R.string.nodisplays).toString();
				ShowToast.showToast(PresentMode.this);
			}
			//Disable present and logo button
			projectButton.setClickable(false);
			logoButton.setClickable(false); 
			image1_switch.setClickable(false);
			image2_switch.setClickable(false);
			video1_switch.setClickable(false);
			video2_switch.setClickable(false);
			blackoutProject_button.setClickable(false);
			setMargins_button.setClickable(false);
		} else {
			if (firsttime) {
				firsttime = false;
				FullscreenActivity.myToastMessage = getResources().getText(R.string.extradisplay).toString();
				ShowToast.showToast(PresentMode.this);
			}
			//Activate present and logo button
			projectButton.setClickable(true);
			logoButton.setClickable(true);
		}
		
		if (numdisplays!=0) {
		for (Display display : presentationDisplays) {
			MyPresentation mPresentation = new MyPresentation(this, display);
			mPresentation.show();
		}
		}
	}
	
	
	
    @Override
    protected void onResume() {
        // Be sure to call the super class.
        super.onResume();
        Log.d("debug","onResume called");
        updateDisplays();
    }

    @Override
    protected void onPause() {
        // Be sure to call the super class.
        super.onPause();
        Log.d("debug","onPause called");
		if (numdisplays!=0) {
		for (Display display : presentationDisplays) {
			MyPresentation mPresentation = new MyPresentation(this, display);
			mPresentation.dismiss();
		}
		}
         updateDisplays();
    }

    @Override
    protected void onStop() {
        // Be sure to call the super class.
        super.onStop();
        Log.d("debug","onStart called");
		if (numdisplays!=0) {
		for (Display display : presentationDisplays) {
			MyPresentation mPresentation = new MyPresentation(this, display);
			mPresentation.dismiss();
		}
		}
        // updateDisplays();
    }

    		   
	public void chooseImage1(View view) {
		whatBackgroundLoaded = "image1";
		chooseFile();
	}
	public void chooseImage2(View view) {
		whatBackgroundLoaded = "image2";
		chooseFile();
	}
	public void switchImage1(View view) {
        // Switch off the others
    	image1_switch.setChecked(true);
    	image2_switch.setChecked(false);
    	video1_switch.setChecked(false);
    	video2_switch.setChecked(false);
    	// Set the backgroundTypeToUse to image
    	FullscreenActivity.backgroundTypeToUse="image";
    	// Set the backgroundToUse variable;
    	FullscreenActivity.backgroundToUse = "img1";
    	// Save the changes
    	Preferences.savePreferences();
    	// Call the update projector
    MyPresentation.fixBackground();
	
	}

	public void switchImage2(View view) {
    	image1_switch.setChecked(false);
    	image2_switch.setChecked(true);
    	video1_switch.setChecked(false);
    	video2_switch.setChecked(false);
    	// Set the backgroundTypeToUse to image
    	FullscreenActivity.backgroundTypeToUse="image";
    	// Set the backgroundToUse variable;
    	FullscreenActivity.backgroundToUse = "img2";
    	// Save the changes
    	Preferences.savePreferences();
    	// Call the update projector
    	MyPresentation.fixBackground();
	}

	public void switchVideo1(View view) {
        // Switch off the others
    	image1_switch.setChecked(false);
    	image2_switch.setChecked(false);
    	video1_switch.setChecked(true);
    	video2_switch.setChecked(false);
    	// Set the backgroundTypeToUse to image
    	FullscreenActivity.backgroundTypeToUse="video";
    	// Set the backgroundToUse variable;
    	FullscreenActivity.backgroundToUse = "vid1";
    	// Save the changes
    	Preferences.savePreferences();
    	// Call the update projector
    	MyPresentation.fixBackground();
	}

	public void switchVideo2(View view) {
        // Switch off the others
    	image1_switch.setChecked(false);
    	image2_switch.setChecked(false);
    	video1_switch.setChecked(false);
    	video2_switch.setChecked(true);
    	// Set the backgroundTypeToUse to image
    	FullscreenActivity.backgroundTypeToUse="video";
    	// Set the backgroundToUse variable;
    	FullscreenActivity.backgroundToUse = "vid2";
    	// Save the changes
    	Preferences.savePreferences();
    	// Call the update projector
    	MyPresentation.fixBackground();
	}
	
	
	public void chooseFile() {
		// This bit gives the user a prompt to select a file for the background button
		dialogBuilder = new AlertDialog.Builder(PresentMode.this);
		LinearLayout titleLayout = new LinearLayout(PresentMode.this);
		titleLayout.setOrientation(LinearLayout.VERTICAL);
		TextView m_titleView = new TextView(PresentMode.this);
		m_titleView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		m_titleView.setTextAppearance(PresentMode.this, android.R.style.TextAppearance_Large);
		m_titleView.setTextColor(PresentMode.this.getResources().getColor(android.R.color.white) );
		m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		if (whatBackgroundLoaded.equals("image1")) {
			m_titleView.setText(getResources().getString(R.string.choose_image1));
		} else if (whatBackgroundLoaded.equals("image2")) {
			m_titleView.setText(getResources().getString(R.string.choose_image2));
		} else if (whatBackgroundLoaded.equals("video1")) {
			m_titleView.setText(getResources().getString(R.string.choose_video1));
		} else if (whatBackgroundLoaded.equals("video2")) {
			m_titleView.setText(getResources().getString(R.string.choose_video2));
		}
		titleLayout.addView(m_titleView);
		dialogBuilder.setCustomTitle(titleLayout);
		dialogBuilder.setCancelable(false);
		
		// List the images/videos - use extensions
		if (whatBackgroundLoaded.equals("image1") || whatBackgroundLoaded.equals("image2")) {
			listImages();
			dialogBuilder.setSingleChoiceItems(BackgroundImageFiles, -1,  new DialogInterface.OnClickListener() {
		    	   @Override
		    	   public void onClick(DialogInterface arg0, int arg1) {
		    		   if (whatBackgroundLoaded.equals("image1")) {
		    			   FullscreenActivity.backgroundImage1 = BackgroundImageFiles[arg1];  
		    		   } else {
		    			   FullscreenActivity.backgroundImage2 = BackgroundImageFiles[arg1];  
		    		   }
		    	   }
		    	  });
		} else if (whatBackgroundLoaded.equals("video1") || whatBackgroundLoaded.equals("video2")) {
			listVideos();
			dialogBuilder.setSingleChoiceItems(BackgroundVideoFiles, -1, new DialogInterface.OnClickListener() {
		    	   @Override
		    	   public void onClick(DialogInterface arg0, int arg1) {
		    		   if (whatBackgroundLoaded.equals("video1")) {
		    			   FullscreenActivity.backgroundVideo1 = BackgroundVideoFiles[arg1];  
		    		   } else {
		    			   FullscreenActivity.backgroundVideo2 = BackgroundVideoFiles[arg1];  
		    		   }
		    	    changeImageForButton();
		    	   }
		    	  });
		}
		listVideos();
 
    dialogBuilder.setPositiveButton(getResources().getString(R.string.ok),
			new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					// TODO Auto-generated method stub
		    	    changeImageForButton();													
					}
				});

    dialogBuilder.show();
	}
	
	
	public void setMargins(View view) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(PresentMode.this);

    	alert.setTitle(getResources().getText(R.string.setmargins).toString());

	    LinearLayout marginslayout = new LinearLayout(PresentMode.this);
	    marginslayout.setOrientation(LinearLayout.VERTICAL);
	    TextView x_margin = new TextView(PresentMode.this);
	    TextView y_margin = new TextView(PresentMode.this);
	    x_margin.setText(getResources().getText(R.string.setxmargins));
	    y_margin.setText(getResources().getText(R.string.setymargins));   
    	final SeekBar xmarginseekbar = new SeekBar(PresentMode.this);
    	final SeekBar ymarginseekbar = new SeekBar(PresentMode.this);
    	marginslayout.addView(x_margin);
    	marginslayout.addView(xmarginseekbar);
    	marginslayout.addView(y_margin);
    	marginslayout.addView(ymarginseekbar);
    	alert.setView(marginslayout);
    	xmarginseekbar.setProgress(FullscreenActivity.xmargin_presentation);
       	ymarginseekbar.setProgress(FullscreenActivity.ymargin_presentation);
       	tempxmargin = FullscreenActivity.xmargin_presentation;
       	tempymargin = FullscreenActivity.ymargin_presentation;
       	
       	xmarginseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
       	    public void onStopTrackingTouch(SeekBar seekBar) {
       	        // TODO Auto-generated method stub
       	    	tempxmargin = seekBar.getProgress();
       	    	MyPresentation.changeMargins();
       	    }
       	    public void onStartTrackingTouch(SeekBar seekBar) {
       	        // TODO Auto-generated method stub
       	    }
       	    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
       	        // TODO Auto-generated method stub
       	    }
       	});

      	ymarginseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
       	    public void onStopTrackingTouch(SeekBar seekBar) {
       	        // TODO Auto-generated method stub
       	    	tempymargin = seekBar.getProgress();
       	    	MyPresentation.changeMargins();
       	    }
       	    public void onStartTrackingTouch(SeekBar seekBar) {
       	        // TODO Auto-generated method stub
       	    }
       	    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
       	        // TODO Auto-generated method stub
       	    }
       	});

    	alert.setPositiveButton(getResources().getText(R.string.ok).toString(), new DialogInterface.OnClickListener() {
    	@Override
		public void onClick(DialogInterface dialog, int whichButton) {
    		FullscreenActivity.xmargin_presentation = xmarginseekbar.getProgress();
    		FullscreenActivity.ymargin_presentation = ymarginseekbar.getProgress();
   	    	tempxmargin = xmarginseekbar.getProgress();
   	    	tempymargin = ymarginseekbar.getProgress();
   	    	Preferences.savePreferences();
   	    	MyPresentation.changeMargins();
    	}
    	});

    	alert.setNegativeButton(getResources().getText(R.string.cancel).toString(), new DialogInterface.OnClickListener() {
    	  @Override
		public void onClick(DialogInterface dialog, int whichButton) {
    	    // Cancelled.
     	    	tempxmargin = FullscreenActivity.xmargin_presentation;
       	    	tempymargin = FullscreenActivity.ymargin_presentation;
       	    	MyPresentation.changeMargins();
    	  }
    	});

    	alert.show();

	}
	
	public void changeImageForButton() {
		// Save the preferences
		Preferences.savePreferences();
		File imgFile = null;
		
		if (whatBackgroundLoaded.equals("image1")) {
			imgFile = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage1);
			Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			//@SuppressWarnings("deprecation")
			//Drawable bgImage1 = new BitmapDrawable(bitmap);
			image1_button.setImageBitmap(bitmap);
			
		} else if (whatBackgroundLoaded.equals("image2")) {
			imgFile = new File(FullscreenActivity.dirbackgrounds + "/" + FullscreenActivity.backgroundImage2);
			Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			//@SuppressWarnings("deprecation")
			//Drawable bgImage2 = new BitmapDrawable(bitmap);
			image2_button.setImageBitmap(bitmap);
			
		} else if (whatBackgroundLoaded.equals("video1")) {
			String bgvid1 = android.os.Environment.getExternalStorageDirectory().getPath()+"/documents/OpenSong/Backgrounds/"+FullscreenActivity.backgroundVideo1;
			Uri videoUri = Uri.parse(bgvid1);
			video1_button.setVideoURI(videoUri);
			video1_button.seekTo(100);
		} else if (whatBackgroundLoaded.equals("video2")) {
			String bgvid2 = android.os.Environment.getExternalStorageDirectory().getPath()+"/documents/OpenSong/Backgrounds/"+FullscreenActivity.backgroundVideo2;
			Uri videoUri = Uri.parse(bgvid2);
			video2_button.setVideoURI(videoUri);
			video2_button.seekTo(100);
		}
		Preferences.savePreferences();
		
	}
	
	
	public void listImages() {
					
		File[] tempmyFiles = FullscreenActivity.dirbackgrounds.listFiles();		
		// Go through this list and check if the item is a directory or a file.
		// Add these to the correct array
		int tempnumfiles=0;
		if (tempmyFiles != null && tempmyFiles.length>0) {
			tempnumfiles = tempmyFiles.length;
		} else {
			tempnumfiles = 0;
		}
		
		tempBackgroundImageFiles = new ArrayList<String>();
		
		for (int x=0; x<tempnumfiles; x++) {
			if (tempmyFiles[x] != null && tempmyFiles[x].isFile() && (
					tempmyFiles[x].getName().contains(".jpg")
					|| tempmyFiles[x].getName().contains(".JPG")
					|| tempmyFiles[x].getName().contains(".jpeg")
					|| tempmyFiles[x].getName().contains(".JPEG")
					|| tempmyFiles[x].getName().contains(".gif")
					|| tempmyFiles[x].getName().contains(".GIF")
					|| tempmyFiles[x].getName().contains(".png")
					|| tempmyFiles[x].getName().contains(".PNG"))) {
				tempBackgroundImageFiles.add(tempmyFiles[x].getName());
			} 
		}
		Collections.sort(tempBackgroundImageFiles, String.CASE_INSENSITIVE_ORDER);        

		// Convert arraylist to string array
		BackgroundImageFiles = new String[tempBackgroundImageFiles.size()];
		BackgroundImageFiles = tempBackgroundImageFiles.toArray(BackgroundImageFiles);
		
	}
	

	
	public void listVideos() {
		
		File[] tempmyFiles = FullscreenActivity.dirbackgrounds.listFiles();		
		// Go through this list and check if the item is a directory or a file.
		// Add these to the correct array
		int tempnumfiles=0;
		if (tempmyFiles != null && tempmyFiles.length>0) {
			tempnumfiles = tempmyFiles.length;
		} else {
			tempnumfiles = 0;
		}
		
		tempBackgroundVideoFiles = new ArrayList<String>();
		
		for (int x=0; x<tempnumfiles; x++) {
			if (tempmyFiles[x] != null && tempmyFiles[x].isFile() && (
					tempmyFiles[x].getName().contains(".mp4")
					|| tempmyFiles[x].getName().contains(".MP4")
					|| tempmyFiles[x].getName().contains(".mpg")
					|| tempmyFiles[x].getName().contains(".MPG")
					|| tempmyFiles[x].getName().contains(".mov")
					|| tempmyFiles[x].getName().contains(".MOV")
					|| tempmyFiles[x].getName().contains(".m4v")
					|| tempmyFiles[x].getName().contains(".M4V"))) {
				tempBackgroundVideoFiles.add(tempmyFiles[x].getName());
			} 
		}
		Collections.sort(tempBackgroundVideoFiles, String.CASE_INSENSITIVE_ORDER);        

		// Convert arraylist to string array
		BackgroundVideoFiles = new String[tempBackgroundVideoFiles.size()];
		BackgroundVideoFiles = tempBackgroundVideoFiles.toArray(BackgroundVideoFiles);
		
	}

	
	
	
	
	
	//Listeners for displays
	
	public void blackoutProject(View view) {
		// Tell the app we have blacked out presentation
		if (blackout.equals("N")) {
			blackout="Y";
			// Disable the other buttons and highlight this one
			projectButton.setClickable(false);
			logoButton.setClickable(false); 
			image1_switch.setClickable(false);
			image1_button.setClickable(false);
			image2_switch.setClickable(false);
			image2_button.setClickable(false);
			video1_switch.setClickable(false);
			video1_button.setClickable(false);
			video2_switch.setClickable(false);
			video2_button.setClickable(false);
			setMargins_button.setClickable(false);
			projectButton.setAlpha(0.2f); 
			logoButton.setAlpha(0.2f); 
			chooseImage1.setAlpha(0.2f);
			chooseImage2.setAlpha(0.2f);
			chooseVideo1.setAlpha(0.2f);
			chooseVideo2.setAlpha(0.2f);
			setMargins_button.setAlpha(0.2f);
			blackoutProject_button.setClickable(true);
		} else {
			if (numdisplays>0) {
				projectButton.setClickable(true);
				logoButton.setClickable(true); 
				image1_switch.setClickable(true);
				image1_button.setClickable(true);
				image2_switch.setClickable(true);
				image2_button.setClickable(true);
				video1_switch.setClickable(true);
				video2_switch.setClickable(true);
				video1_button.setClickable(true);
				video2_button.setClickable(true);
				setMargins_button.setClickable(true);

			}
			projectButton.setAlpha(1.0f); 
			logoButton.setAlpha(1.0f); 
			chooseImage1.setAlpha(1.0f);
			chooseImage2.setAlpha(1.0f);
			chooseVideo1.setAlpha(1.0f);
			chooseVideo2.setAlpha(1.0f);
			setMargins_button.setAlpha(1.0f);
			blackoutProject_button.setClickable(true);
			blackout="N";
		}
		MyPresentation.blackoutPresentation();
	}
	
	public void redrawPresenterPage() {
		// Now load the appropriate song folder
		ListSongFiles.listSongs();

		invalidateOptionsMenu();
		// Redraw the Lyrics View
		isPDF = false;
		File checkfile = new File(FullscreenActivity.dir+"/"+FullscreenActivity.songfilename);
		if ((FullscreenActivity.songfilename.contains(".pdf") || FullscreenActivity.songfilename.contains(".PDF")) && checkfile.exists()) {
			// File is pdf
			isPDF = true;
		}
		
		if (!isPDF) {
		try {
			LoadXML.loadXML();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		} else {
			FullscreenActivity.mLyrics = getResources().getString(R.string.pdf_functionnotavailable);
			FullscreenActivity.mTitle = FullscreenActivity.songfilename;
			FullscreenActivity.mAuthor = "";
			Preferences.savePreferences();
		}

		FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
		PresentPrepareSong.splitSongIntoSections();
		setupSongButtons();
	}

	public void listSavedSets(View view) {
		// update the optionList to show the sets stored in the sets folder
		SetActions.updateOptionListSets(PresentMode.this, view);
		Arrays.sort(FullscreenActivity.mySetsFiles);
		Arrays.sort(FullscreenActivity.mySetsDirectories);
		Arrays.sort(FullscreenActivity.mySetsFileNames);
		Arrays.sort(FullscreenActivity.mySetsFolderNames);

		// Set up the new options list
		expListViewOption.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawerlist_menu, FullscreenActivity.mySetsFileNames));
	}

	public void promptNewSet() throws IOException {
		FullscreenActivity.newSetContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
				+ "<set name=\""
				+ FullscreenActivity.settoload
				+ "\">\r\n<slide_groups>\r\n";

		for (int x = 0; x < FullscreenActivity.mSetList.length; x++) {
			// Only add the lines that aren't back to options,
			// save this set, clear this set, load set, edit set, export set or blank line
			if (FullscreenActivity.mSetList[x].equals(getResources().getString(R.string.options_backtooptions)) == false && 
					FullscreenActivity.mSetList[x].equals(getResources().getString(R.string.options_savethisset)) == false && 
					FullscreenActivity.mSetList[x].equals(getResources().getString(R.string.options_clearthisset)) == false &&
					FullscreenActivity.mSetList[x].equals(getResources().getString(R.string.set_edit)) == false &&
					//FullscreenActivity.mSetList[x].equals(getResources().getString(R.string.set_menutitle)) == false && 
					//FullscreenActivity.mSetList[x].equals(getResources().getString(R.string.menu_menutitle)) == false && 
					FullscreenActivity.mSetList[x].equals(getResources().getString(R.string.set_save)) == false &&
					FullscreenActivity.mSetList[x].equals(getResources().getString(R.string.set_clear)) == false &&
					FullscreenActivity.mSetList[x].equals(getResources().getString(R.string.set_export)) == false &&
					FullscreenActivity.mSetList[x].equals(getResources().getString(R.string.set_load)) == false && 
					FullscreenActivity.mSetList[x].length() > 0) {

				// Check if song is in subfolder
				int issonginsubfolder = FullscreenActivity.mSetList[x]
						.indexOf("/");
				if (issonginsubfolder >= 0) {
					// Already has / for subfolder
				} else {
					FullscreenActivity.mSetList[x] = "/" + FullscreenActivity.mSetList[x];
				}
				// Split the string into two
				String[] songparts = null;
				songparts = FullscreenActivity.mSetList[x].split("/");
				// If the path isn't empty, add a forward slash to the end
				if (songparts[0].length() > 0) {
					songparts[0] = songparts[0] + "/";
				}
				if (!songparts[0].contains("Scripture") && !songparts[0].contains("Slide")) {
					// Adding a song
					FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
							+ "  <slide_group name=\""
							+ songparts[1]
									+ "\" type=\"song\" presentation=\"\" path=\""
									+ songparts[0] + "\"/>\n";
				} else if (songparts[0].contains("Scripture")  & !songparts[0].contains("Slide")) {
					// Adding a scripture
					// Load the scripture file up
					// Keep the songfile as a temp
					String tempsongfilename = FullscreenActivity.songfilename;
					File tempdir = FullscreenActivity.dir;
					FullscreenActivity.dir = FullscreenActivity.dirbibleverses;
					FullscreenActivity.songfilename = songparts[1];
					try {
						LoadXML.loadXML();
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

					String scripture_lyrics = FullscreenActivity.mLyrics;

					// Parse the lyrics into individual slides;
					scripture_lyrics = scripture_lyrics.replace("[]","_SPLITHERE_");
					scripture_lyrics = scripture_lyrics.replace("\\n "," ");
					scripture_lyrics = scripture_lyrics.replace("\n "," ");
					scripture_lyrics = scripture_lyrics.replace("\n"," ");
					scripture_lyrics = scripture_lyrics.replace("\\n"," ");
					scripture_lyrics = scripture_lyrics.replace("  "," ");
					scripture_lyrics = scripture_lyrics.replace(". ",". ");

					String[] mySlides = scripture_lyrics.split("_SPLITHERE_");

					FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
							+ "  <slide_group type=\"scripture\" name=\""
							+ songparts[1] + "|" + FullscreenActivity.mAuthor
							+ "\" print=\"true\">\r\n"
							+ "  <title>" + songparts[1] + "</title>\r\n"
							+ "  <slides>\r\n";

					for (int w=1;w<mySlides.length;w++) {
						if (mySlides[w]!=null && mySlides[w].length()>0){
							FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
									+ "  <slide>\r\n"
									+ "  <body>"+mySlides[w].trim()+"</body>\r\n"
									+ "  </slide>\r\n";
						}
					}
					FullscreenActivity.newSetContents = FullscreenActivity.newSetContents + "</slides>\r\n"
							+ "  <subtitle>" + "</subtitle>\r\n"
							+ "  <notes />\r\n"
							+ "</slide_group>\r\n";
					//Put the original songfilename back
					FullscreenActivity.songfilename = tempsongfilename;
					FullscreenActivity.dir = tempdir;									
					try {
						LoadXML.loadXML();
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

				} else if (songparts[0].contains("Slide") && !songparts[0].contains("Scripture")) {
					// Adding a custom slide
					// Load the slide file up
					// Keep the songfile as a temp
					String tempsongfilename = FullscreenActivity.songfilename;
					File tempdir = FullscreenActivity.dir;
					FullscreenActivity.dir = FullscreenActivity.dircustomslides;
					FullscreenActivity.songfilename = songparts[1];
					try {
						LoadXML.loadXML();
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

					String slide_lyrics = FullscreenActivity.mLyrics;

					// Parse the lyrics into individual slides;
					slide_lyrics = slide_lyrics.replace("[]","_SPLITHERE_");
					slide_lyrics = slide_lyrics.replace("\\n "," ");
					slide_lyrics = slide_lyrics.replace("\n "," ");
					slide_lyrics = slide_lyrics.replace("\n"," ");
					slide_lyrics = slide_lyrics.replace("\\n"," ");
					slide_lyrics = slide_lyrics.replace("  "," ");
					slide_lyrics = slide_lyrics.replace(". ",".  ");

					String[] mySlides = slide_lyrics.split("_SPLITHERE_");

					FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
							+ "  <slide_group name=\"" + songparts[1] 
									+ "\" type=\"custom\" print=\"true\""
									+ " seconds=\"" + FullscreenActivity.mUser1 + "\""
									+ " loop=\"" + FullscreenActivity.mUser2 + "\""
									+ " transition=\"" + FullscreenActivity.mUser3 + "\">\r\n"
									+ "<title>"+FullscreenActivity.mTitle+"</title>\r\n"
									+ "<subtitle>" + FullscreenActivity.mCopyright + "</subtitle>\r\n"
									+ "<notes>" + FullscreenActivity.mKeyLine + "</notes>\r\n"
									+ "<slides>\r\n";

					for (int w=1;w<mySlides.length;w++) {
						if (mySlides[w]!=null && mySlides[w].length()>0){
							FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
									+ "  <slide>\r\n"
									+ "  <body>"+mySlides[w].trim()+"</body>\r\n"
									+ "  </slide>\r\n";
						}
					}
					FullscreenActivity.newSetContents = FullscreenActivity.newSetContents + "</slides>\r\n"
							+ "</slide_group>\r\n";
					//Put the original songfilename back
					FullscreenActivity.songfilename = tempsongfilename;
					FullscreenActivity.dir = tempdir;									
					try {
						LoadXML.loadXML();
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

				}
			}
		}
		FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
				+ "</slide_groups>\r\n</set>";

		// Write the string to the file
		FileOutputStream newFile;
		try {
			newFile = new FileOutputStream(FullscreenActivity.dirsets + "/"
					+ FullscreenActivity.settoload, false);
			newFile.write(FullscreenActivity.newSetContents.getBytes());
			newFile.flush();
			newFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	public void setupSongButtons() {
		// Create a new button for each songSection
		LinearLayout buttonList = (LinearLayout) findViewById(R.id.buttonList);
		buttonList.removeAllViews();
		TextView songtitle = new TextView(PresentMode.this);
		TextView songauthor = new TextView(PresentMode.this);
		songtitle.setText(FullscreenActivity.mTitle);
		songtitle.setTextSize(18.0f);
		songauthor.setText(FullscreenActivity.mAuthor);
		songauthor.setTextSize(14.0f);
		buttonList.addView(songtitle);
		buttonList.addView(songauthor);
		for (int x = 0; x < songSections.length; x++) {
			String buttonText = songSectionsLabels[x] + "\n" + songSections[x];
			songButton = new Button(PresentMode.this);
			songButton.setText(buttonText);
			songButton.setBackgroundResource(R.drawable.present_section_button);
			songButton.setTextSize(12.0f);
			songButton.setPadding(10, 10, 10, 10);
			songButton.setMinimumHeight(0);
			songButton.setMinHeight(0);
			songButton.setId(x);
			LayoutParams params = new LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(5, 5, 5, 5);
			songButton.setLayoutParams(params);
			songButton.setOnClickListener(new sectionButtonClick());
			buttonList.addView(songButton);
		}
		presoAuthor = FullscreenActivity.mAuthor.toString();
		presoCopyright = FullscreenActivity.mCopyright.toString();
		presoTitle = FullscreenActivity.mTitle.toString();
		presoOther = "";
	}

	public void setupSetButtons() {
		// Create a new button for each song in the Set
		invalidateOptionsMenu();
		SetActions.prepareSetList();
		
		LinearLayout setList = (LinearLayout) findViewById(R.id.setList);
		setList.removeAllViews();
		for (int x = 0; x < FullscreenActivity.mSet.length; x++) {
			if (!FullscreenActivity.mSet[x].isEmpty()){
			String buttonText = FullscreenActivity.mSet[x];
			setButton = new Button(PresentMode.this);
			setButton.setText(buttonText);
			setButton.setBackgroundResource(R.drawable.present_section_setbutton);
			setButton.setTextSize(12.0f);
			setButton.setPadding(10, 10, 10, 10);
			setButton.setMinimumHeight(0);
			setButton.setMinHeight(0);
			setButton.setId(x);
			setButton.setOnClickListener(new setButtonClick());
			setList.addView(setButton);
			}
		}
	}

	
	
	public void doProject(View v) {
		buttonPresentText = songProjectText.getText().toString();
		blackout="N";
		logo_on="N";
		song_on="Y";
		MyPresentation.UpDatePresentation();
	}

	public void showLogo(View v) {
		blackout="N";

		if (logo_on.equals("Y")) {
			logo_on="N";
			if (numdisplays>0) {
				projectButton.setClickable(true);
				blackoutProject_button.setClickable(true); 
				image1_switch.setClickable(true);
				image1_button.setClickable(true);
				image2_switch.setClickable(true);
				image2_button.setClickable(true);
				video1_switch.setClickable(true);
				video2_switch.setClickable(true);
				video1_button.setClickable(true);
				video2_button.setClickable(true);
				setMargins_button.setClickable(true);

			}
			projectButton.setAlpha(1.0f); 
			blackoutProject_button.setAlpha(1.0f); 
			chooseImage1.setAlpha(1.0f);
			chooseImage2.setAlpha(1.0f);
			chooseVideo1.setAlpha(1.0f);
			chooseVideo2.setAlpha(1.0f);
			setMargins_button.setAlpha(1.0f);
			blackoutProject_button.setClickable(true);
			blackout="N";
		} else {
			logo_on="Y";
			// Disable the other buttons and highlight this one
			projectButton.setClickable(false);
			blackoutProject_button.setClickable(false); 
			image1_switch.setClickable(false);
			image1_button.setClickable(false);
			image2_switch.setClickable(false);
			image2_button.setClickable(false);
			video1_switch.setClickable(false);
			video1_button.setClickable(false);
			video2_switch.setClickable(false);
			video2_button.setClickable(false);
			setMargins_button.setClickable(false);
			projectButton.setAlpha(0.2f); 
			blackoutProject_button.setAlpha(0.2f); 
			chooseImage1.setAlpha(0.2f);
			chooseImage2.setAlpha(0.2f);
			chooseVideo1.setAlpha(0.2f);
			chooseVideo2.setAlpha(0.2f);
			setMargins_button.setAlpha(0.2f);
			logoButton.setClickable(true);

		}
		song_on="N";
		MyPresentation.ShowLogo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.presenter_actions, menu);
		this.menu = menu;		
	    MenuItem set_back = menu.findItem(R.id.set_back);
		MenuItem set_forward = menu.findItem(R.id.set_forward);
		if (FullscreenActivity.setSize>0 && FullscreenActivity.setView.equals("Y")) {
			set_back.setVisible(true);
			set_forward.setVisible(true);
			set_back.getIcon().setAlpha(255);
			set_forward.getIcon().setAlpha(255);

		} else {
			set_back.setVisible(false);
			set_forward.setVisible(false);
		}
		//Now decide if the song being viewed has a song before it.
		//Otherwise disable the back button
		if (FullscreenActivity.indexSongInSet<1) {
			set_back.setEnabled(false);
			set_back.getIcon().setAlpha(30);
		}
		//Now decide if the song being viewed has a song after it.
		//Otherwise disable the forward button
		if (FullscreenActivity.indexSongInSet>=(FullscreenActivity.setSize-1)) {
			set_forward.setEnabled(false);
			set_forward.getIcon().setAlpha(30);
		}
		return super.onCreateOptionsMenu(menu);
	}

	// This bit listens for key presses (for page turn and scroll)
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//Set a runnable to reset swipe back to original value after 1 second

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			// User wants the menu
			if (mDrawerLayout.isDrawerOpen(expListViewOption)) {
				mDrawerLayout.closeDrawer(expListViewOption);
				} 
			if (mDrawerLayout.isDrawerOpen(expListViewSong)) {
				mDrawerLayout.closeDrawer(expListViewSong);
				} 
		}
		if (keyCode == KeyEvent.KEYCODE_BACK) { 
			// Ask the user if they want to exit
			// Give them an are you sure prompt
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						// Yes button clicked
						finish();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						// No button clicked
						break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(PresentMode.this);
			builder.setMessage(
					getResources().getString(R.string.exit))
					.setPositiveButton(
							getResources().getString(R.string.yes),
							dialogClickListener)
					.setNegativeButton(
							getResources().getString(R.string.no),
							dialogClickListener).show();		
			return false;

		} else if (FullscreenActivity.setView.equals("N") && (keyCode==FullscreenActivity.pageturner_NEXT||keyCode==FullscreenActivity.pageturner_PREVIOUS)) {
			// User isn't viewing a song in the current set yet.
			// See if we can automatically load the first song in a set for them
			// The setSize has to be >=7 though!
			SetActions.prepareSetList();
			invalidateOptionsMenu();
			
			if (FullscreenActivity.setSize>0) {
				FullscreenActivity.setView="Y";
				FullscreenActivity.indexSongInSet=0;
				FullscreenActivity.previousSongInSet = "";
				FullscreenActivity.myToastMessage = getResources().getString(R.string.pageturn_attempt);
				ShowToast.showToast(PresentMode.this);
				
				
				FullscreenActivity.songfilename=FullscreenActivity.mSet[0];
				if (FullscreenActivity.setSize>1) {
					FullscreenActivity.nextSongInSet = FullscreenActivity.mSet[1];
				} else {
					FullscreenActivity.nextSongInSet = "";
				}
				invalidateOptionsMenu();
				if (FullscreenActivity.mSetList[0].indexOf("/") >= 0) {
					// Ok so it does!
					FullscreenActivity.linkclicked = FullscreenActivity.mSetList[0];
				} else {
					// Right it doesn't, so add the /
					FullscreenActivity.linkclicked = "/" + FullscreenActivity.mSetList[0];
				}
				String[] songpart = FullscreenActivity.linkclicked.split("/");
				FullscreenActivity.songfilename = songpart[1];
				// If the folder length isn't 0, it is a folder
				if (songpart[0].length() > 0 && !songpart[0].contains("Scripture")) {
					FullscreenActivity.whichSongFolder = songpart[0];
					FullscreenActivity.dir = new File(
							FullscreenActivity.root.getAbsolutePath()
									+ "/documents/OpenSong/Songs/"
									+ songpart[0]);

				} else if (songpart[0].length() > 0 && songpart[0].contains("Scripture")) {
						FullscreenActivity.whichSongFolder = "../OpenSong Scripture/_cache";
						songpart[0] = "../OpenSong Scripture/_cache";
						//FullscreenActivity.dir = FullscreenActivity.dirbibleverses;
					
				} else if (songpart[0].length() > 0 && songpart[0].contains("Slide")) {
					FullscreenActivity.whichSongFolder = "../Slides/_cache";
					songpart[0] = "../Slides/_cache";
					//FullscreenActivity.dir = FullscreenActivity.dirbibleverses;
				
				} else {
					FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
					FullscreenActivity.dir = new File(
							FullscreenActivity.root.getAbsolutePath()
									+ "/documents/OpenSong/Songs");
				}

				// Save the preferences
				Preferences.savePreferences();
				try {
					LoadXML.loadXML();
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
				prepareSongMenu();
				redrawPresenterPage();;
				invalidateOptionsMenu();
				mDrawerLayout.closeDrawer(expListViewOption);
				mDrawerLayout.closeDrawer(expListViewSong);

			} else {
				FullscreenActivity.myToastMessage = getResources().getString(R.string.pageturn_needset);
        	ShowToast.showToast(PresentMode.this);
			}
			

			
		} else if (keyCode == FullscreenActivity.pageturner_NEXT) {
	        // If we are viewing a set, move to the next song.
			if (FullscreenActivity.setSize>1 && FullscreenActivity.setView.equals("Y") && FullscreenActivity.indexSongInSet>=0 && FullscreenActivity.indexSongInSet<(FullscreenActivity.setSize-1)) {
				FullscreenActivity.indexSongInSet += 1;
				doMoveInSet();
				return true;	        		
	        	}

		} else if (keyCode == FullscreenActivity.pageturner_PREVIOUS) {
			// If we are viewing a set, move to the next song.
			if (FullscreenActivity.setSize>1 && FullscreenActivity.setView.equals("Y") && FullscreenActivity.indexSongInSet>=1) {
				FullscreenActivity.indexSongInSet -= 1;
				doMoveInSet();
				return true;	        		
         	}
	}
		return false;
	}

	public void fixSetActionButtons(Menu menu) {
		SetActions.prepareSetList();
		invalidateOptionsMenu();
		if (FullscreenActivity.setSize>=1) {
			prepareOptionMenu();
		}
	}

	public void doMoveInSet() {
		invalidateOptionsMenu();
		FullscreenActivity.linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
		if (FullscreenActivity.linkclicked.indexOf("/") >= 0) {
			// Ok so it does!
		} else {
			// Right it doesn't, so add the /
			FullscreenActivity.linkclicked = "/" + FullscreenActivity.linkclicked;
		}

		// Now split the linkclicked into two song parts 0=folder 1=file
		String[] songpart = FullscreenActivity.linkclicked.split("/");

		// If the folder length isn't 0, it is a folder
		if (songpart[0].length() > 0 && !songpart[0].contains("Scripture") && !songpart[0].contains("Slide")) {
			FullscreenActivity.whichSongFolder = songpart[0];
			FullscreenActivity.dir = new File(
					FullscreenActivity.root.getAbsolutePath()
							+ "/documents/OpenSong/Songs/"
							+ songpart[0]);

		} else if (songpart[0].length() > 0 && songpart[0].contains("Scripture") && !songpart[0].contains("Slide")) {
			FullscreenActivity.whichSongFolder = "../OpenSong Scripture/_cache";
				songpart[0] = "../OpenSong Scripture/_cache";
				//FullscreenActivity.dir = FullscreenActivity.dirbibleverses;

		} else if (songpart[0].length() > 0 && songpart[0].contains("Slide") && !songpart[0].contains("Scripture")) {
			FullscreenActivity.whichSongFolder = "../Slides/_cache";
			songpart[0] = "../Slides/_cache";
			//FullscreenActivity.dir = FullscreenActivity.dircustomslides;

		} else {
			FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
			FullscreenActivity.dir = new File(
					FullscreenActivity.root.getAbsolutePath()
							+ "/documents/OpenSong/Songs");
		}

		// Save the preferences
		Preferences.savePreferences();

		// Match the song folder
		// ListSongFiles.listSongs();
		
		//prepareSongMenu();
		// Look for song in song folder
		findSongInFolder();
		
		mDrawerLayout.closeDrawer(expListViewSong);
		// Redraw the Lyrics View
		FullscreenActivity.songfilename = null;
		FullscreenActivity.songfilename = "";
		FullscreenActivity.songfilename = songpart[1];
		redrawPresenterPage();
		return;
		
	}

	
	
	
	
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// call ActionBarDrawerToggle.onOptionsItemSelected(), if it returns
		// true
		// then it has handled the app icon touch event
		switch (item.getItemId()) {

		case R.id.perform_mode:
			// Switch to performance mode
			FullscreenActivity.whichMode = "Perfomance";
			Preferences.savePreferences();
			Intent performmode = new Intent();
			performmode.setClass(PresentMode.this, FullscreenActivity.class);
			startActivity(performmode);
			finish();
			return true;

		case R.id.action_search:
			  if (mDrawerLayout.isDrawerOpen(expListViewSong)) {
				  mDrawerLayout.closeDrawer(expListViewSong);
			  } else {
			  mDrawerLayout.openDrawer(expListViewSong);
			  }		 
			return true;

		case R.id.action_fullsearch:
			/*// Need to fix this as the SearchViewFilterMode returns to FullscreenActivity
			Intent intent = new Intent();
			intent.setClass(PresentMode.this,SearchViewFilterMode.class);
			startActivity(intent);
			finish();*/
			return true;

		case R.id.action_settings:
			 if (mDrawerLayout.isDrawerOpen(expListViewOption)) {
				 mDrawerLayout.closeDrawer(expListViewOption);
			 } else {
				 mDrawerLayout.openDrawer(expListViewOption);
			 }
			 return true;
			 
		case R.id.set_add:
			if (FullscreenActivity.whichSongFolder
					.equals(FullscreenActivity.mainfoldername)) {
				FullscreenActivity.whatsongforsetwork = "$**_"
						+ FullscreenActivity.songfilename + "_**$";
			} else {
				FullscreenActivity.whatsongforsetwork = "$**_"
						+ FullscreenActivity.whichSongFolder + "/"
						+ FullscreenActivity.songfilename + "_**$";
			}
			// Allow the song to be added, even if it is already there
			FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;
			// Tell the user that the song has been added.
			FullscreenActivity.myToastMessage = "\""
					+ FullscreenActivity.songfilename + "\" "
					+ getResources().getString(R.string.addedtoset);
			ShowToast.showToast(PresentMode.this);

			// Save the set and other preferences
			Preferences.savePreferences();

			SetActions.prepareSetList();
			prepareOptionMenu();

			// Hide the menus - 1 second after opening the Option menu, close it
			// (1000ms total)
			Handler optionMenuFlickClosed = new Handler();
			optionMenuFlickClosed.postDelayed(new Runnable() {
				@Override public void run() {
					mDrawerLayout.closeDrawer(expListViewOption);
					}
				}, 1000); // 1000ms delay
			return true;
			

		case R.id.set_back:
			FullscreenActivity.indexSongInSet -= 1;
			doMoveInSet();
			expListViewOption.setSelection(FullscreenActivity.indexSongInSet);
			return true;

		case R.id.set_forward:
			FullscreenActivity.indexSongInSet += 1;
			doMoveInSet();
			expListViewOption.setSelection(FullscreenActivity.indexSongInSet);
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	public class sectionButtonClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			// Get button id
			int whichview = v.getId();
			EditText songProjectText = (EditText) findViewById(R.id.songProjectText);
			songProjectText.setText(songSections[whichview]);
		}

	}

	public class setButtonClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			// Get button id
			int buttonid = v.getId();
			String buttontext = FullscreenActivity.mSetList[buttonid];
			
			FullscreenActivity.setView = "Y";
			FullscreenActivity.indexSongInSet = buttonid;
			if (buttonid < 1) {
				FullscreenActivity.previousSongInSet = "";
			} else {
				FullscreenActivity.previousSongInSet = FullscreenActivity.mSetList[buttonid - 1];
			}
			if (buttonid == (FullscreenActivity.setSize - 1)) {
				FullscreenActivity.nextSongInSet = "";
			} else {
				FullscreenActivity.previousSongInSet = FullscreenActivity.mSetList[buttonid + 1];
			}
			invalidateOptionsMenu();
			if (buttontext.indexOf("/") >= 0) {
				// Ok so it does!
			} else {
				// Right it doesn't, so add the /
				buttontext = "/" + buttontext;
			}

			// Now split the linkclicked into two song parts 0=folder 1=file
			String[] songpart = buttontext.split("/");

			// If the folder length isn't 0, it is a folder
			if (songpart[0].length() > 0 && !songpart[0].contains("Scripture") && !songpart[0].contains("Slide")) {
				FullscreenActivity.whichSongFolder = songpart[0];
				FullscreenActivity.dir = new File(
						FullscreenActivity.root.getAbsolutePath()
								+ "/documents/OpenSong/Songs/"
								+ songpart[0]);

			} else if (songpart[0].length() > 0 && songpart[0].contains("Scripture") && !songpart[0].contains("Slide")) {
					FullscreenActivity.whichSongFolder = "../OpenSong Scripture/_cache";
					songpart[0] = "../OpenSong Scripture/_cache";
					//FullscreenActivity.dir = FullscreenActivity.dirbibleverses;
				
			} else if (songpart[0].length() > 0 && !songpart[0].contains("Scripture") && songpart[0].contains("Slide")) {
				FullscreenActivity.whichSongFolder = "../Slides/_cache";
				songpart[0] = "../Slides/_cache";
				//FullscreenActivity.dir = FullscreenActivity.dirbibleverses;
			
			} else {
				FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
				FullscreenActivity.dir = new File(
						FullscreenActivity.root.getAbsolutePath()
								+ "/documents/OpenSong/Songs");
			}

			
			mDrawerLayout.closeDrawer(expListViewOption);

			// Redraw the Lyrics View
			FullscreenActivity.songfilename = songpart[1];
			
			// Save the preferences
			Preferences.savePreferences();

			redrawPresenterPage();
			return;
			
		}

	}

	
	
	public void prepareSongMenu() {
		// Initialise Songs menu
		listDataHeaderSong = new ArrayList<String>();
		listDataChildSong = new HashMap<String, List<String>>();

		// Get song folders
		ListSongFiles.listSongFolders();
		listDataHeaderSong.add(getResources().getString(R.string.mainfoldername));
		for (int w=0;w<FullscreenActivity.mSongFolderNames.length-1;w++) {
			listDataHeaderSong.add(FullscreenActivity.mSongFolderNames[w]);
		}        

		for (int s=0;s<FullscreenActivity.mSongFolderNames.length;s++) {
			List<String> song_folders = new ArrayList<String>();
			for (int t=0;t<FullscreenActivity.childSongs[s].length;t++) {
				song_folders.add(FullscreenActivity.childSongs[s][t]);            	
			}
			listDataChildSong.put(listDataHeaderSong.get(s), song_folders);       	
		}

		listAdapterSong = new ExpandableListAdapter(this, listDataHeaderSong, listDataChildSong);
		expListViewSong.setAdapter(listAdapterSong);
		expListViewSong.setFastScrollEnabled(true);

		// Listen for song folders being opened/expanded
		expListViewSong.setOnGroupExpandListener(new OnGroupExpandListener() {
			private int lastExpandedGroupPositionSong;

			@Override
			public void onGroupExpand(int groupPosition) {
				if(groupPosition != lastExpandedGroupPositionSong){
					expListViewSong.collapseGroup(lastExpandedGroupPositionSong);
				}
				lastExpandedGroupPositionSong = groupPosition;
			}
		});

		// Listen for long clicks in the song menu (songs only, not folders) - ADD TO SET!!!!
		expListViewSong.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				addingtoset = true;
				if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					int groupPosition = ExpandableListView.getPackedPositionGroup(id);
					int childPosition = ExpandableListView.getPackedPositionChild(id);

					// Vibrate to indicate something has happened
					Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vb.vibrate(25);

					FullscreenActivity.songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

					if (listDataHeaderSong.get(groupPosition)==FullscreenActivity.mainfoldername) {
						FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs");
						FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
						FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.songfilename + "_**$";
					} else {
						FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs/" + listDataHeaderSong.get(groupPosition));
						FullscreenActivity.whichSongFolder = listDataHeaderSong.get(groupPosition);
						FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename + "_**$";
					}
					// Set the appropriate song filename
					FullscreenActivity.songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

					// Allow the song to be added, even if it is already there
					FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;

					// Tell the user that the song has been added.
					FullscreenActivity.myToastMessage = "\"" + FullscreenActivity.songfilename + "\" " + getResources().getString(R.string.addedtoset);
					ShowToast.showToast(PresentMode.this);

					// Save the set and other preferences
					Preferences.savePreferences();

					// Show the current set
					invalidateOptionsMenu();
					SetActions.prepareSetList();
					setupSetButtons();
					prepareOptionMenu();
					mDrawerLayout.openDrawer(expListViewOption);
					mDrawerLayout.closeDrawer(expListViewSong);
					expListViewOption.expandGroup(0);

					// Hide the menus - 1 second after opening the Option menu,
					// close it (1000ms total)
					Handler optionMenuFlickClosed = new Handler();
					optionMenuFlickClosed.postDelayed(new Runnable() {
						@Override
						public void run() {
							mDrawerLayout.closeDrawer(expListViewOption);
							addingtoset = false;
						}
					}, 1000); // 1000ms delay
				}
				return false;
			}
		});

		// Listen for short clicks in the song menu (songs only, not folders) - OPEN SONG!!!!
		expListViewSong.setOnChildClickListener(new OnChildClickListener() {


			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				if (!addingtoset) {
					// Set the appropriate folder name

					if (listDataHeaderSong.get(groupPosition)==FullscreenActivity.mainfoldername) {
						FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs");
						FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
					} else {
						FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs/" + listDataHeaderSong.get(groupPosition));
						FullscreenActivity.whichSongFolder = listDataHeaderSong.get(groupPosition);
					}
					// Set the appropriate song filename
					FullscreenActivity.songfilename = listDataChildSong.get(listDataHeaderSong.get(groupPosition)).get(childPosition);

					if (FullscreenActivity.setView.equals("Y") && FullscreenActivity.setSize >= 8) {
						// Ok look for the song in the set.
						if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
							FullscreenActivity.whatsongforsetwork = FullscreenActivity.songfilename;
						} else {
							FullscreenActivity.whatsongforsetwork = FullscreenActivity.whichSongFolder + "/"
									+ FullscreenActivity.linkclicked;
						}

						if (FullscreenActivity.mySet.indexOf(FullscreenActivity.whatsongforsetwork) >= 0) {
							// Song is in current set.  Find the song position in the current set and load it (and next/prev)
							// The first song has an index of 6 (the 7th item as the rest are menu items)

							FullscreenActivity.previousSongInSet = "";
							FullscreenActivity.nextSongInSet = "";
							SetActions.prepareSetList();
							setupSetButtons();

							for (int x = 0; x < FullscreenActivity.setSize; x++) {
								if (FullscreenActivity.mSet[x].equals(FullscreenActivity.whatsongforsetwork)) {
									FullscreenActivity.indexSongInSet = x;
									FullscreenActivity.previousSongInSet = FullscreenActivity.mSet[x - 1];
									if (x == FullscreenActivity.setSize - 1) {
										FullscreenActivity.nextSongInSet = "";
									} else {
										FullscreenActivity.nextSongInSet = FullscreenActivity.mSet[x + 1];
									}
								}
							}

						} else {
							// Song isn't in the set, so just show the song
							// Switch off the set view (buttons in action bar)
							FullscreenActivity.setView = "N";
						}
					} else {
						// User wasn't in set view, or the set was empty
						// Switch off the set view (buttons in action bar)
						FullscreenActivity.setView = "N";
					}

					// Set the swipe direction to right to left
					FullscreenActivity.whichDirection = "R2L";

					// Now save the preferences
					Preferences.savePreferences();

					invalidateOptionsMenu();

					// Redraw the Lyrics View
					mDrawerLayout.closeDrawer(expListViewSong);
					mDrawerLayout.closeDrawer(expListViewOption);
					
					redrawPresenterPage();
 

				} else {
					addingtoset = false;
				}
				return false;
			}

		});

	}

	
	
	
	
	
	
	
	
	
	
	
	public void prepareOptionMenu() {
		// preparing list data
		listDataHeaderOption = new ArrayList<String>();
		listDataChildOption = new HashMap<String, List<String>>();

		// Adding headers for option menu data
		listDataHeaderOption.add(getResources().getString(R.string.options_set));
		listDataHeaderOption.add(getResources().getString(R.string.options_song));
		listDataHeaderOption.add(getResources().getString(R.string.options_options));

		// Adding child data
		List<String> options_set = new ArrayList<String>();
		options_set.add(getResources().getString(R.string.options_set_load));
		options_set.add(getResources().getString(R.string.options_set_save));
		options_set.add(getResources().getString(R.string.options_set_clear));
		options_set.add(getResources().getString(R.string.options_set_delete));
		options_set.add(getResources().getString(R.string.options_set_export));
		options_set.add(getResources().getString(R.string.options_set_edit));
		options_set.add("");

		// Parse the saved set
		FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$$**_", "_**$%%%$**_");
		// Break the saved set up into a new String[]
		FullscreenActivity.mSetList = FullscreenActivity.mySet.split("%%%");
		// Restore the set back to what it was
		FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$%%%$**_", "_**$$**_");
		FullscreenActivity.setSize = FullscreenActivity.mSetList.length;
		invalidateOptionsMenu();

		for (int r=0;r<FullscreenActivity.mSetList.length;r++) {
			FullscreenActivity.mSetList[r] = FullscreenActivity.mSetList[r].replace("$**_","");
			FullscreenActivity.mSetList[r] = FullscreenActivity.mSetList[r].replace("_**$","");
			if (!FullscreenActivity.mSetList[r].isEmpty()) {
				options_set.add(FullscreenActivity.mSetList[r]);
			}
		}


		List<String> options_song = new ArrayList<String>();
		options_song.add(getResources().getString(R.string.options_song_edit));
		options_song.add(getResources().getString(R.string.options_song_rename));
		options_song.add(getResources().getString(R.string.options_song_delete));
		options_song.add(getResources().getString(R.string.options_song_new));
		options_song.add(getResources().getString(R.string.options_song_export));

		List<String> options_options = new ArrayList<String>();
		options_options.add(getResources().getString(R.string.options_options_menuswipe));
		options_options.add(getResources().getString(R.string.options_options_fonts));
		options_options.add(getResources().getString(R.string.options_options_pedal));
		options_options.add(getResources().getString(R.string.options_options_help));
		options_options.add(getResources().getString(R.string.options_options_start));

		listDataChildOption.put(listDataHeaderOption.get(0), options_set); // Header, Child data
		listDataChildOption.put(listDataHeaderOption.get(1), options_song);
		listDataChildOption.put(listDataHeaderOption.get(2), options_options);

		listAdapterOption = new ExpandableListAdapter(this, listDataHeaderOption, listDataChildOption);

		// setting list adapter
		expListViewOption.setAdapter(listAdapterOption);

		// Listen for options menus being expanded (close the others and keep a note that this one is open)
		expListViewOption.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				if(groupPosition != lastExpandedGroupPositionOption){
					expListViewOption.collapseGroup(lastExpandedGroupPositionOption);
				}
				lastExpandedGroupPositionOption = groupPosition;
			}
		});


		// Listen for long clicks on songs in current set to remove them
		expListViewOption.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				removingfromset = true;
				if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					int groupPosition = ExpandableListView.getPackedPositionGroup(id);
					int childPosition = ExpandableListView.getPackedPositionChild(id);
					myOptionListClickedItem = position;
					if (myOptionListClickedItem > 7 && groupPosition==0) {
						// Long clicking on the 7th or later options will remove the
						// song from the set (all occurrences)
						// Remove this song from the set. Remember it has tags at the start and end
						Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						vb.vibrate(25);

						// Take away the menu items (7)
						String tempSong = FullscreenActivity.mSetList[childPosition-7];
						FullscreenActivity.mSetList[childPosition-7] = "";

						FullscreenActivity.mySet = "";
						for (int w=0;w<FullscreenActivity.mSetList.length;w++) {
							if (!FullscreenActivity.mSetList[w].isEmpty()) {
								FullscreenActivity.mySet = FullscreenActivity.mySet + "$**_" + FullscreenActivity.mSetList[w] + "_**$";
							}
						}

						// Save set
						Preferences.savePreferences();

						// Reload the set menu
						invalidateOptionsMenu();
						SetActions.prepareSetList();
						setupSetButtons();
						prepareOptionMenu();
						expListViewOption.expandGroup(0);

						// Tell the user that the song has been removed.
						FullscreenActivity.myToastMessage = "\"" + tempSong + "\" "
								+ getResources().getString(R.string.removedfromset);
						ShowToast.showToast(PresentMode.this);

						// Close the drawers again so accidents don't happen!
						mDrawerLayout.closeDrawer(expListViewSong);
						mDrawerLayout.closeDrawer(expListViewOption);
					}
				}
				removingfromset = false;

				return false;
			}
		});

		// Listview on child click listener
		expListViewOption.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

				if (!removingfromset) {
					// Make sure the song menu is closed
					mDrawerLayout.closeDrawer(expListViewSong);

					String chosenMenu = listDataHeaderOption.get(groupPosition);

					// Build a dialogue window and related bits that get modified/shown if needed
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PresentMode.this);
					LinearLayout titleLayout = new LinearLayout(PresentMode.this);
					titleLayout.setOrientation(LinearLayout.VERTICAL);
					TextView m_titleView = new TextView(PresentMode.this);
					m_titleView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
					m_titleView.setTextAppearance(PresentMode.this, android.R.style.TextAppearance_Large);
					m_titleView.setTextColor(PresentMode.this.getResources().getColor(android.R.color.white) );
					m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

					if (chosenMenu==getResources().getString(R.string.options_set)) {

						// Load up a list of saved sets as it will likely be needed
						SetActions.updateOptionListSets(PresentMode.this, view);
						Arrays.sort(FullscreenActivity.mySetsFiles);
						Arrays.sort(FullscreenActivity.mySetsDirectories);
						Arrays.sort(FullscreenActivity.mySetsFileNames);
						Arrays.sort(FullscreenActivity.mySetsFolderNames);

						// First up check for set options clicks
						if (childPosition==0) {
							// Load a set
							Log.d("Set","Load");
							// Pull up a dialogue with a list of all saved sets available
							m_titleView.setText(getResources().getString(R.string.options_set) + " - " + getResources().getString(R.string.options_set_load));
							titleLayout.addView(m_titleView);
							dialogBuilder.setCustomTitle(titleLayout);
							dialogBuilder.setSingleChoiceItems(FullscreenActivity.mySetsFileNames, -1, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									FullscreenActivity.setnamechosen = FullscreenActivity.mySetsFileNames[arg1];
								}
							});
							dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							});
							dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// Load the set up
									FullscreenActivity.settoload = null;
									FullscreenActivity.settoload = FullscreenActivity.setnamechosen;
									try {
										SetActions.loadASet(view);
									} catch (XmlPullParserException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}


									Log.d("loadset","mySet="+FullscreenActivity.mySet);
									// Reset the options menu
									SetActions.prepareSetList();
									prepareOptionMenu();
									// Expand set group
									expListViewOption.expandGroup(0);
									setupSetButtons();
									// Save the new set to the preferences
									Preferences.savePreferences();
								}
							});

							dialogBuilder.show();


						} else if (childPosition==1) {
							// Save current set
							Log.d("Set","Save");
							m_titleView.setText(getResources().getString(R.string.options_set) + " - " + getResources().getString(R.string.options_set_save));
							titleLayout.addView(m_titleView);
							dialogBuilder.setCustomTitle(titleLayout);
							final EditText m_editbox = new EditText(PresentMode.this);
							m_editbox.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
							m_editbox.setTextAppearance(PresentMode.this, android.R.style.TextAppearance_Large);
							m_editbox.setTextColor(PresentMode.this.getResources().getColor(android.R.color.white) );
							m_editbox.setBackgroundColor(PresentMode.this.getResources().getColor(android.R.color.darker_gray));
							m_editbox.setHint(getResources().getString(R.string.options_set_nameofset));
							m_editbox.requestFocus();
							dialogBuilder.setView(m_editbox);
							dialogBuilder.setSingleChoiceItems(FullscreenActivity.mySetsFileNames, -1, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									FullscreenActivity.setnamechosen = FullscreenActivity.mySetsFileNames[arg1];
									m_editbox.setText(FullscreenActivity.setnamechosen);
								}
							});
							dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							});
							dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// Save the set
									FullscreenActivity.settoload = m_editbox.getText().toString();
									try {
										promptNewSet();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

									FullscreenActivity.myToastMessage = getResources().getString(R.string.options_set_save) + " " + FullscreenActivity.settoload + " - " + getResources().getString(R.string.ok);
									ShowToast.showToast(PresentMode.this);

									// Reset the options menu
									prepareOptionMenu();
									// Expand set group
									expListViewOption.expandGroup(0);
								}
							});

							dialogBuilder.show();


						} else if (childPosition==2) {
							// Clear current set
							Log.d("Set","Clear");
							m_titleView.setText(getResources().getString(R.string.options_set) + " - " + getResources().getString(R.string.options_set_clear));
							titleLayout.addView(m_titleView);
							dialogBuilder.setCustomTitle(titleLayout);
							dialogBuilder.setMessage(getResources().getString(R.string.areyousure));

							dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							});
							dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// Clear the set
									FullscreenActivity.mySet = "";
									FullscreenActivity.mSetList = null;
									FullscreenActivity.setView = "N";
									invalidateOptionsMenu();
									SetActions.prepareSetList();

									// Save the new, empty, set
									Preferences.savePreferences();

									FullscreenActivity.myToastMessage = getResources().getString(R.string.options_set_clear) + " " + getResources().getString(R.string.ok);
									ShowToast.showToast(PresentMode.this);

									// Reset the options menu
									prepareOptionMenu();
									// Expand set group
									expListViewOption.expandGroup(0);
									setupSetButtons();
								}
							});

							dialogBuilder.show();



						} else if (childPosition==3) {
							// Delete saved set
							Log.d("Set","Delete");
							// Pull up a dialogue with a list of all saved sets available
							m_titleView.setText(getResources().getString(R.string.options_set) + " - " + getResources().getString(R.string.options_set_delete));
							titleLayout.addView(m_titleView);
							dialogBuilder.setCustomTitle(titleLayout);
							dialogBuilder.setSingleChoiceItems(FullscreenActivity.mySetsFileNames, -1, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									FullscreenActivity.setnamechosen = FullscreenActivity.mySetsFileNames[arg1];
								}
							});
							dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							});
							dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// Load the set up
									FullscreenActivity.settoload = null;
									FullscreenActivity.settoload = FullscreenActivity.setnamechosen;

									File settodelete = new File(FullscreenActivity.dirsets+"/"+FullscreenActivity.settoload);
									if (settodelete.delete()) {
										FullscreenActivity.myToastMessage = FullscreenActivity.settoload + " " + getResources().getString(R.string.sethasbeendeleted);
										ShowToast.showToast(PresentMode.this);
									}										

									// Reset the options menu
									prepareOptionMenu();
									// Expand set group
									expListViewOption.expandGroup(0);
								}
							});

							dialogBuilder.show();





						} else if (childPosition==4) {
							// Export current set
							Log.d("Set","Export");
							// Pull up a dialogue with a list of all saved sets available
							m_titleView.setText(getResources().getString(R.string.options_set) + " - " + getResources().getString(R.string.options_set_export));
							titleLayout.addView(m_titleView);
							dialogBuilder.setCustomTitle(titleLayout);
							dialogBuilder.setSingleChoiceItems(FullscreenActivity.mySetsFileNames, -1, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									FullscreenActivity.setnamechosen = FullscreenActivity.mySetsFileNames[arg1];
								}
							});
							dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							});
							dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// Export the set
									FullscreenActivity.settoload = null;
									FullscreenActivity.settoload = FullscreenActivity.setnamechosen;
									File setFileLocation = new File(FullscreenActivity.dirsets + "/" + FullscreenActivity.settoload);
									Intent intent = new Intent(Intent.ACTION_SEND);
									intent.setType("application/xml");
									intent.putExtra(Intent.EXTRA_TITLE, FullscreenActivity.settoload);
									intent.putExtra(Intent.EXTRA_STREAM,
											Uri.fromFile(setFileLocation));
									startActivity(Intent.createChooser(intent, FullscreenActivity.exportsavedset));
								}

							});

							dialogBuilder.show();



						} else if (childPosition==5) {
							// Edit current set
							Log.d("Set","Edit");
							// Only works for ICS or above

							int currentapiVersion = android.os.Build.VERSION.SDK_INT;
							mDrawerLayout.closeDrawer(expListViewOption);
							if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
								Intent editset = new Intent();
								editset.setClass(PresentMode.this, ListViewDraggingAnimation.class);
								editset.putExtra("PresentMode", true);
								startActivity(editset);
								PresentMode.this.finish();
							} else {
								FullscreenActivity.myToastMessage = getResources().getText(R.string.nothighenoughapi).toString();
								ShowToast.showToast(PresentMode.this);
							}

						} else if (childPosition==6) {
							// Blank entry
							Log.d("Set","Nothing");

						} else {
							// Load song in set
							Log.d("Set","Song");
							FullscreenActivity.setView = "Y";
							// Set item is 7 less than childPosition
							FullscreenActivity.indexSongInSet = childPosition - 7;
							String linkclicked = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet];
							if (FullscreenActivity.indexSongInSet==0) {
								// Already first item
								FullscreenActivity.previousSongInSet = "";
							} else {
								FullscreenActivity.previousSongInSet = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet-1];
							}

							if (FullscreenActivity.indexSongInSet==(FullscreenActivity.setSize-1)) {
								// Last item
								FullscreenActivity.nextSongInSet = "";
							} else {
								FullscreenActivity.nextSongInSet = FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet+1];
							}
							FullscreenActivity.whichDirection = "R2L";
							invalidateOptionsMenu();
							if (linkclicked.indexOf("/") >= 0) {
								// Ok so it does!
							} else {
								// Right it doesn't, so add the /
								linkclicked = "/" + linkclicked;
							}

							// Now split the linkclicked into two song parts 0=folder 1=file
							String[] songpart = linkclicked.split("/");

							// If the folder length isn't 0, it is a folder
							if (songpart[0].length() > 0 && !songpart[0].contains("Scripture") && !songpart[0].contains("Slide")) {
								FullscreenActivity.whichSongFolder = songpart[0];
								FullscreenActivity.dir = new File(
										FullscreenActivity.root.getAbsolutePath()
										+ "/documents/OpenSong/Songs/"
										+ songpart[0]);

							} else if (songpart[0].length() > 0 && songpart[0].contains("Scripture")) {
								FullscreenActivity.whichSongFolder = "../OpenSong Scripture/_cache";
								songpart[0] = "../OpenSong Scripture/_cache";
								//FullscreenActivity.dir = FullscreenActivity.dirbibleverses;

							} else if (songpart[0].length() > 0 && songpart[0].contains("Slide")) {
								FullscreenActivity.whichSongFolder = "../Slides/_cache";
								songpart[0] = "../Slides/_cache";
								//FullscreenActivity.dir = FullscreenActivity.dircustomslides;

							} else {
								FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
								FullscreenActivity.dir = new File(
										FullscreenActivity.root.getAbsolutePath()
										+ "/documents/OpenSong/Songs");
							}

							FullscreenActivity.songfilename = null;
							FullscreenActivity.songfilename = "";
							FullscreenActivity.songfilename = songpart[1];

							// Save the preferences
							Preferences.savePreferences();        				

							redrawPresenterPage();
							//Close the drawer
							mDrawerLayout.closeDrawer(expListViewOption);
						} 




					} else if (chosenMenu==getResources().getString(R.string.options_song)) {
						linkclicked = listDataChildOption.get(listDataHeaderOption.get(groupPosition)).get(childPosition);

						// Now check for song options clicks
						// Only allow 0=edit, 1=rename, 2=delete, 3=new, 4=export
						if (childPosition==0) {
							// Edit
							if (isPDF) {
								// Can't do this action on a pdf!
								FullscreenActivity.myToastMessage = getResources().getString(R.string.pdf_functionnotavailable);
								ShowToast.showToast(PresentMode.this);
							} else {
							Preferences.savePreferences();
							openEditSong();
							}

						} else if (childPosition==1) {
							// Rename
							// This bit gives the user a prompt to change the song name
							// First set the browsing directory back to the main one
							FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath()+"/documents/OpenSong/Songs");
							FullscreenActivity.currentFolder = FullscreenActivity.whichSongFolder;
							FullscreenActivity.newFolder = FullscreenActivity.whichSongFolder;
							FullscreenActivity.whichSongFolder = FullscreenActivity.mainfoldername;
							ListSongFiles.listSongs();

							// This bit gives the user a prompt to create a new song
							AlertDialog.Builder alert = new AlertDialog.Builder(
									PresentMode.this);

							m_titleView.setText(getResources().getString(R.string.renametitle));
							titleLayout.addView(m_titleView);
							alert.setCustomTitle(titleLayout);

							// Get current folder
							int numfolders = FullscreenActivity.mSongFolderNames.length;
							//By default the folder is set to the main one
							int folderposition = 0;
							for (int z=0;z<numfolders;z++) {
								if (FullscreenActivity.mSongFolderNames[z].toString().equals(FullscreenActivity.currentFolder)) {
									// Set this as the folder
									folderposition = z;
									FullscreenActivity.mSongFolderNames[z] = FullscreenActivity.currentFolder;
								}
							}

							alert.setSingleChoiceItems(FullscreenActivity.mSongFolderNames, folderposition,  new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									FullscreenActivity.newFolder = FullscreenActivity.mSongFolderNames[arg1];
								}
							});

							// Set an EditText view to get user input
							final LinearLayout renameSong = new LinearLayout(PresentMode.this);
							renameSong.setOrientation(LinearLayout.VERTICAL);
							final TextView blurb = new TextView(PresentMode.this);
							blurb.setText("\n"+ getResources().getString(R.string.rename));
							blurb.setTextAppearance(PresentMode.this, android.R.style.TextAppearance_Medium);
							blurb.setTextColor(FullscreenActivity.lyricsChordsColor);
							final EditText input = new EditText(PresentMode.this);
							input.setText(FullscreenActivity.songfilename);
							renameSong.addView(blurb);
							renameSong.addView(input);
							alert.setView(renameSong);

							alert.setPositiveButton(getResources().getString(R.string.ok),
									new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String newSongTitle = input.getText()
											.toString();
									// Rename
									String tempCurrentFolder = FullscreenActivity.currentFolder + "/";
									String tempNewFolder = FullscreenActivity.newFolder + "/";

									if (FullscreenActivity.newFolder.equals("(MAIN)")) {
										tempNewFolder = "";
										FullscreenActivity.whichSongFolder = "MAIN";
										FullscreenActivity.newFolder = "MAIN";
									} else {
										FullscreenActivity.whichSongFolder = FullscreenActivity.newFolder;
									}								

									if (FullscreenActivity.currentFolder.equals("MAIN")) {
										tempCurrentFolder = "";
									}

									FullscreenActivity.whichSongFolder = FullscreenActivity.newFolder;

									if (isPDF && (!newSongTitle.contains(".pdf") || !newSongTitle.contains(".PDF"))) {
										newSongTitle = newSongTitle + ".pdf";
									}

									File from = new File(FullscreenActivity.dir + "/" + tempCurrentFolder + FullscreenActivity.songfilename);
									File to = new File(FullscreenActivity.dir + "/" + tempNewFolder + newSongTitle);
									from.renameTo(to);
									FullscreenActivity.songfilename = newSongTitle;

									// Load the songs
									ListSongFiles.listSongs();

									// Get the song indexes
									ListSongFiles.getCurrentSongIndex();

									prepareSongMenu();
									prepareOptionMenu();
									setupSetButtons();
									setupSongButtons();
									redrawPresenterPage();
									mDrawerLayout.closeDrawer(expListViewOption);
								}
							});

							alert.setNegativeButton(
									getResources().getString(R.string.cancel),
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog,
												int whichButton) {
											// Cancelled.
										}
									});

							alert.show();


						} else if (childPosition==2) {
							// Delete
							// Give the user an are you sure prompt!
							DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case DialogInterface.BUTTON_POSITIVE:
										// Yes button clicked
										// Exit set mode
										FullscreenActivity.setView = "N";
										String setFileLocation = FullscreenActivity.dir + "/" + FullscreenActivity.songfilename;
										File filetoremove = new File(setFileLocation);
										// Don't allow the user to delete Love Everlasting
										// This is to stop there being no song to show after
										// deleting the currently viewed one
										if (FullscreenActivity.songfilename.equals("Love everlasting")) {
											FullscreenActivity.myToastMessage = "\"Love everlasting\" "
													+ getResources().getString(
															R.string.shouldnotbedeleted);
											ShowToast.showToast(PresentMode.this);
										} else {
											boolean deleted = filetoremove.delete();
											if (deleted) {
												FullscreenActivity.myToastMessage = "\""
														+ FullscreenActivity.songfilename
														+ "\" "
														+ getResources()
														.getString(
																R.string.songhasbeendeleted);
											} else {
												FullscreenActivity.myToastMessage = getResources().getString(
														R.string.deleteerror_start)
														+ " \""
														+ FullscreenActivity.songfilename
														+ "\" "
														+ getResources()
														.getString(
																R.string.deleteerror_end_song);
											}
											ShowToast.showToast(PresentMode.this);
										}

										// Now save the preferences
										Preferences.savePreferences();
										redrawPresenterPage();

										// Need to reload the song list
										// Match the song folder
										ListSongFiles.listSongs();
										prepareSongMenu();
										mDrawerLayout.closeDrawer(expListViewOption);
										mDrawerLayout.openDrawer(expListViewSong);
										// 1000ms second after opening the Songs menu, close it
										Handler songMenuFlickClosed = new Handler();
										songMenuFlickClosed.postDelayed(new Runnable() {
											@Override
											public void run() {
												mDrawerLayout.closeDrawer(expListViewSong);
											}
										}, 1000); // 1000ms delay

										break;

									case DialogInterface.BUTTON_NEGATIVE:
										// No button clicked
										break;
									}
								}
							};

							if (FullscreenActivity.myLyrics.equals("ERROR!")) {
								// Tell the user they can't edit a song with an error!
								FullscreenActivity.myToastMessage = FullscreenActivity.songdoesntexist;
								ShowToast.showToast(PresentMode.this);
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										PresentMode.this);
								builder.setMessage(
										getResources().getString(R.string.areyousure))
										.setPositiveButton(
												getResources().getString(R.string.yes),
												dialogClickListener)
												.setNegativeButton(
														getResources().getString(R.string.no),
														dialogClickListener).show();
							}

						} else if (childPosition==3) {
							// New
							try {
								promptNew();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}


						} else if (childPosition==4) {
							// Export
							// The current song is the songfile
							// Believe it or not, it works!!!!!
							if (FullscreenActivity.myLyrics.equals("ERROR!")) {
								// Tell the user they can't edit a song with an error!
								FullscreenActivity.myToastMessage = FullscreenActivity.songdoesntexist;
								ShowToast.showToast(PresentMode.this);
							} else {
								File file = new File(FullscreenActivity.dir+"/" + FullscreenActivity.songfilename);
								Intent intent = new Intent(Intent.ACTION_SEND);
								intent.setType("application/xml");
								intent.putExtra(Intent.EXTRA_TITLE, FullscreenActivity.songfilename);
								intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
								startActivity(Intent.createChooser(intent,
										FullscreenActivity.exportcurrentsong));
							}

						}




					} else if (chosenMenu==getResources().getString(R.string.options_options)) {
						// Now check for option options clicks
						if (childPosition==0) {
							// Toggle menu swipe on/off
							if (FullscreenActivity.swipeDrawer.equals("Y")) {
								FullscreenActivity.swipeDrawer = "N";
								mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
								FullscreenActivity.myToastMessage = getResources().getString(
										R.string.drawerswipe)
										+ " " + getResources().getString(R.string.off);
								ShowToast.showToast(PresentMode.this);
							} else {
								FullscreenActivity.swipeDrawer = "Y";
								mDrawerLayout
								.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
								FullscreenActivity.myToastMessage = getResources().getString(
										R.string.drawerswipe)
										+ " " + getResources().getString(R.string.on);
								ShowToast.showToast(PresentMode.this);
							}
							Preferences.savePreferences();


						} else if (childPosition==1) {
							// Change fonts
							Intent intent2 = new Intent();
							intent2.setClass(PresentMode.this, ChangeFonts.class);
							intent2.putExtra("PresentMode", true);
							startActivity(intent2);
							finish();


						} else if (childPosition==2) {
							// Assign foot pedal
							Intent intent = new Intent();
							intent.setClass(PresentMode.this, setPageTurns.class);
							intent.putExtra("PresentMode", true);
							startActivity(intent);
							finish();

						} else if (childPosition==3) {
							// Help (online)
							String url = "https://sites.google.com/site/opensongtabletmusicviewer/home";
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse(url));
							startActivity(i);


						} else if (childPosition==4) {
							// Splash screen
							// First though, set the preference to show the current version
							// Otherwise it won't show the splash screen
							SharedPreferences settings = getSharedPreferences("mysettings",
									Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = settings.edit();
							editor.putInt("showSplashVersion", 0);
							editor.commit();
							Intent intent = new Intent();
							intent.setClass(PresentMode.this, SettingsActivity.class);
							startActivity(intent);
							finish();

						}
					}
				}
				return false;
			}
		});
	}

	
		
	public void openEditSong() {
		Intent editsong = new Intent(this, EditSong.class);
		editsong.putExtra("PresentMode", true);
		startActivity(editsong);
		finish();
	}
	
	public void promptNew() throws IOException {

		// This bit gives the user a prompt to create a new song
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(getResources().getString(R.string.createanewsong));
		alert.setMessage(getResources().getString(R.string.newsongtitleprompt));
		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);
		InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		mgr.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

		alert.setPositiveButton(getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				String newSongTitle = input.getText().toString();
				// Create the file with the song title
				final String basicSong = new String(
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
								+ "<song>\r\n <title>"
								+ newSongTitle
								+ "</title>\r\n"
								+ "  <author></author>\r\n"
								+ "  <copyright></copyright>\r\n"
								+ "  <presentation></presentation>\r\n"
								+ "  <hymn_number></hymn_number>\r\n"
								+ "  <capo print=\"false\"></capo>\r\n"
								+ "  <tempo></tempo>\r\n"
								+ "  <time_sig></time_sig>\r\n"
								+ "  <ccli></ccli>\r\n"
								+ "  <theme></theme>\r\n"
								+ "  <alttheme></alttheme>\r\n"
								+ "  <user1></user1>\r\n"
								+ "  <user2></user2>\r\n"
								+ "  <user3></user3>\r\n"
								+ "  <key></key>\n"
								+ "  <aka></aka>\r\n"
								+ "  <key_line></key_line>\r\n"
								+ "  <lyrics></lyrics>\r\n"
								+ "  <style index=\"default_style\"></style>\r\n"
								+ "</song>");

				// If song already exists - tell the user and do nothing
				boolean isitanokfilename = true;
				for (int check = 0; check < FullscreenActivity.mSongFileNames.length; check++) {
					if (FullscreenActivity.mSongFileNames[check].equals(newSongTitle)) {
						isitanokfilename = false;
					}
				}

				if (isitanokfilename) {
					// Write the string to the file
					FileOutputStream newFile;
					try {
						newFile = new FileOutputStream(FullscreenActivity.dir + "/"
								+ newSongTitle, false);
						newFile.write(basicSong.getBytes());
						newFile.flush();
						newFile.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// Exit set mode
					FullscreenActivity.setView = "N";
					FullscreenActivity.songfilename = newSongTitle;
					FullscreenActivity.myLyrics = "[V1]\n.\n Verse 1\n\n[C]\n.\n Chorus";
					Intent editsong = new Intent(PresentMode.this, EditSong.class);
					Bundle newextras = new Bundle();
					newextras.putString("songfilename", newSongTitle);
					editsong.putExtras(newextras);
					editsong.putExtra("PresentMode", true);
					startActivity(editsong);
					finish();
				} else {
					FullscreenActivity.myToastMessage = getResources().getText(
							R.string.songnamealreadytaken).toString();
					ShowToast.showToast(PresentMode.this);
					FullscreenActivity.myToastMessage = "";
				}
			}
		});

		alert.setNegativeButton(getResources().getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// Cancelled.
			}
		});

		alert.show();

	}
	

	public void findSongInFolder() {
		// Try to open the appropriate Song folder on the left menu
		expListViewSong.expandGroup(0);
		for (int z=0;z<listDataHeaderSong.size()-1;z++) {
			if (listDataHeaderSong.get(z).equals(FullscreenActivity.whichSongFolder)) {
				expListViewSong.expandGroup(z);
			}
		}
	}
























}
package com.garethevans.church.opensongtablet.performance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.SetTypeFace;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.databinding.PerformanceBinding;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.metronome.Metronome;
import com.garethevans.church.opensongtablet.pads.PadFunctions;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.AppActionBar;
import com.garethevans.church.opensongtablet.screensetup.DoVibrate;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.screensetup.ThemeColors;
import com.garethevans.church.opensongtablet.setprocessing.SetActions;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songsandsetsmenu.SongListBuildIndex;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.util.ArrayList;

public class PerformanceFragment extends Fragment {

    private final String TAG = "PerformanceFragment";
    // Helper classes for the heavy lifting
    private StorageAccess storageAccess;
    private Preferences preferences;
    private ProcessSong processSong;
    private LoadSong loadSong;
    private SQLiteHelper sqLiteHelper;
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private CommonSQL commonSQL;
    private ConvertChoPro convertChoPro;
    private ConvertOnSong convertOnSong;
    private ThemeColors themeColors;
    private ShowToast showToast;
    private PerformanceGestures performanceGestures;
    private SetActions setActions;
    private PadFunctions padFunctions;
    private Metronome metronome;
    private DoVibrate doVibrate;
    private SetTypeFace setTypeFace;
    private SongListBuildIndex songListBuildIndex;
    private AppActionBar appActionBar;

    //private ShowCase showCase;

    //private LoadSongInterface loadSongInterface;
    private MainActivityInterface mainActivityInterface;

    // The variables used in the fragment
    private float scaleHeadings, scaleComments, scaleChords, fontSize, fontSizeMin, fontSizeMax,
            lineSpacing;
    private int swipeMinimumDistance, swipeMaxDistanceYError, swipeMinimumVelocity;
    private boolean trimLines, trimSections, addSectionSpace, songAutoScaleColumnMaximise,
            songAutoScaleOverrideFull, songAutoScaleOverrideWidth, boldChordHeading,
            highlightChords,highlightHeadings;
    static boolean wasScaling, R2L, loadNextSong, loadPrevSong;
    private static int screenHeight;
    public static int songViewWidth, songViewHeight, screenWidth;
    private RelativeLayout testPane;
    private ArrayList<View> sectionViews;
    private ArrayList<Integer> sectionWidths, sectionHeights;
    private String autoScale;
    private PerformanceBinding myView;

    // Attaching and destroying
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.registerFragment(this,"Performance");
    }
    @Override
    public void onDetach() {
        super.onDetach();
        if (appActionBar!=null) {
            appActionBar.setPerformanceMode(false);
        }
        mainActivityInterface.registerFragment(null,"Performance");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }

    // The logic to start this fragment
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = PerformanceBinding.inflate(inflater, container, false);
        View root = myView.getRoot();

        // Initialise the helper classes that do the heavy lifting
        initialiseHelpers();
        mainActivityInterface.lockDrawer(false);
        //mainActivityInterface.hideActionBar(false);
        mainActivityInterface.hideActionButton(false);

        //mainActivityInterface.changeActionBarVisible(false,false);
        // Load in preferences
        loadPreferences();

        // Prepare the song menu (will be called again after indexing from the main activity index songs)
        mainActivityInterface.fullIndex();

        doSongLoad(preferences.getMyPreferenceString(requireContext(),"whichFolder",getString(R.string.mainfoldername)),
                preferences.getMyPreferenceString(requireContext(),"songfilename","Welcome to OpenSongApp"));

        // Set listeners for the scroll/scale/gestures
        //setGestureListeners();

        // Show the actionBar and hide it after a time if that's the user's preference
        preferences.setMyPreferenceBoolean(requireContext(),"hideActionBar",false);

        appActionBar.setHideActionBar(preferences.getMyPreferenceBoolean(requireContext(),"hideActionBar",false));
        appActionBar.setPerformanceMode(true);
        appActionBar.showActionBar();

        // Set tutorials
        Handler h = new Handler();
        Runnable r = () -> mainActivityInterface.showTutorial("performanceView");
        h.postDelayed(r,1000);

        return root;
    }

    // Getting the preferences and helpers ready
    private void initialiseHelpers() {
        storageAccess = mainActivityInterface.getStorageAccess();
        preferences = mainActivityInterface.getPreferences();
        loadSong = mainActivityInterface.getLoadSong();
        processSong = mainActivityInterface.getProcessSong();
        sqLiteHelper = mainActivityInterface.getSQLiteHelper();
        convertOnSong = mainActivityInterface.getConvertOnSong();
        convertChoPro = mainActivityInterface.getConvertChoPro();
        themeColors = mainActivityInterface.getMyThemeColors();
        showToast = mainActivityInterface.getShowToast();
        nonOpenSongSQLiteHelper = mainActivityInterface.getNonOpenSongSQLiteHelper();
        commonSQL = mainActivityInterface.getCommonSQL();
        setActions = mainActivityInterface.getSetActions();
        doVibrate = mainActivityInterface.getDoVibrate();
        padFunctions = mainActivityInterface.getPadFunctions();
        metronome = mainActivityInterface.getMetronome();
        setTypeFace = mainActivityInterface.getMyFonts();
        songListBuildIndex = mainActivityInterface.getSongListBuildIndex();
        appActionBar = mainActivityInterface.getAppActionBar();

        //showCase = new ShowCase();
/*
        performanceGestures = new PerformanceGestures(getContext(),preferences,storageAccess,setActions,
                padFunctions,metronome,this,mainActivityInterface,showToast,doVibrate,
                mainActivityInterface.getDrawer(),mainActivityInterface.getMediaPlayer(1),
                mainActivityInterface.getMediaPlayer(2),mainActivityInterface.getAppActionBar(), 0xffff0000);*/
    }
    private void loadPreferences() {
        themeColors.getDefaultColors(getContext(),preferences);
        scaleHeadings = preferences.getMyPreferenceFloat(getActivity(),"scaleHeadings",0.6f);
        scaleChords = preferences.getMyPreferenceFloat(getActivity(),"scaleChords",0.8f);
        scaleComments = preferences.getMyPreferenceFloat(getActivity(),"scaleComments",0.8f);
        trimLines = preferences.getMyPreferenceBoolean(getActivity(),"trimLines",true);
        lineSpacing = preferences.getMyPreferenceFloat(getActivity(),"lineSpacing",0.1f);
        trimSections = preferences.getMyPreferenceBoolean(getActivity(),"trimSections",true);
        boldChordHeading = preferences.getMyPreferenceBoolean(getActivity(), "displayBoldChordsHeadings", false);
        addSectionSpace = preferences.getMyPreferenceBoolean(getActivity(), "addSectionSpace", true);
        autoScale = preferences.getMyPreferenceString(getActivity(),"songAutoScale","W");
        songAutoScaleColumnMaximise = preferences.getMyPreferenceBoolean(getActivity(),"songAutoScaleColumnMaximise",true);
        fontSize = preferences.getMyPreferenceFloat(getActivity(),"fontSize",42.0f);
        fontSizeMax = preferences.getMyPreferenceFloat(getActivity(),"fontSizeMax",50.0f);
        fontSizeMin = preferences.getMyPreferenceFloat(getActivity(),"fontSizeMin",8.0f);
        songAutoScaleOverrideFull = preferences.getMyPreferenceBoolean(getActivity(),"songAutoScaleOverrideFull",true);
        songAutoScaleOverrideWidth = preferences.getMyPreferenceBoolean(getActivity(),"songAutoScaleOverrideWidth",false);
        swipeMinimumDistance = preferences.getMyPreferenceInt(getActivity(),"swipeMinimumDistance",250);
        swipeMaxDistanceYError = preferences.getMyPreferenceInt(getActivity(),"swipeMaxDistanceYError",200);
        swipeMinimumVelocity = preferences.getMyPreferenceInt(getActivity(),"swipeMinimumVelocity",600);
        highlightChords = preferences.getMyPreferenceBoolean(requireContext(),"highlightChords",false);
        highlightHeadings = preferences.getMyPreferenceBoolean(requireContext(),"highlightHeadings",false);
        fontSizeMax = 90.0f;
        songAutoScaleOverrideWidth = false;
        songAutoScaleOverrideFull = false;
        myView.mypage.setBackgroundColor(themeColors.getLyricsBackgroundColor());
    }




    private void resetTitleSizes() {
        mainActivityInterface.updateActionBarSettings("songTitleSize",-1,
                preferences.getMyPreferenceFloat(requireContext(),"songTitleSize",13.0f),true);
        mainActivityInterface.updateActionBarSettings("songAuthorSize",-1,
                preferences.getMyPreferenceFloat(requireContext(),"songAuthorSize",11.0f),true);
    }
    // Displaying the song
    public void doSongLoad(String folder, String filename) {
        // Loading the song is dealt with in this fragment as specific actions are required
        new Thread(() -> {
            // Quick fade the current page
            requireActivity().runOnUiThread(() -> {
                Animation animSlide;
                if (R2L) {
                    animSlide = AnimationUtils.loadAnimation(requireActivity(), R.anim.slide_out_left);
                } else {
                    animSlide = AnimationUtils.loadAnimation(requireActivity(), R.anim.slide_out_right);
                }
                myView.songView.startAnimation(animSlide);
                myView.highlighterView.startAnimation(animSlide);
                myView.imageView.startAnimation(animSlide);
                myView.zoomLayout.moveTo(1,0,0,false);
            });
            // Load up the song
            if (sectionViews!=null) {
                sectionViews.clear();
            }
            // Now reset the song
            mainActivityInterface.setSong(processSong.initialiseSong(commonSQL,folder, filename));

            Log.d(TAG, "mainActivityInterface.getSong().getFolder()="+mainActivityInterface.getSong().getFolder());
            Log.d(TAG, "mainActivityInterface.getSong().getFilename()="+mainActivityInterface.getSong().getFilename());

            mainActivityInterface.setSong(loadSong.doLoadSong(getContext(),mainActivityInterface,
                    storageAccess,preferences,processSong, showToast, mainActivityInterface.getLocale(),
                    songListBuildIndex, sqLiteHelper, commonSQL, mainActivityInterface.getSong(),
                    convertOnSong, convertChoPro, false));

            requireActivity().runOnUiThread(this::prepareSongViews);
            mainActivityInterface.moveToSongInSongMenu();
        }).start();
    }
    private void prepareSongViews() {
        // This is called on UI thread above;
        myView.pageHolder.setBackgroundColor(themeColors.getLyricsBackgroundColor());
        // Get the song in the layout
        sectionViews = processSong.setSongInLayout(getActivity(),preferences,
                mainActivityInterface.getLocale(), trimSections, addSectionSpace, trimLines, lineSpacing,
                themeColors, setTypeFace, scaleHeadings, scaleChords, scaleComments,
                mainActivityInterface.getSong().getLyrics(), boldChordHeading);

        // We now have the 1 column layout ready, so we can set the view observer to measure once drawn
        setUpVTO();

        // Update the toolbar
        mainActivityInterface.updateToolbar(mainActivityInterface.getSong(),null);
    }
    private void setUpVTO() {
        testPane = myView.testPane;
        ViewTreeObserver vto = testPane.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // All views have now been drawn, so measure the arraylist views
                // First up, remove the listener
                sectionWidths = new ArrayList<>();
                sectionHeights = new ArrayList<>();
                for (View v:sectionViews)  {
                    int width = v.getMeasuredWidth();
                    int height = v.getMeasuredHeight();
                    sectionWidths.add(width);
                    sectionHeights.add(height);
                }
                screenWidth = myView.mypage.getMeasuredWidth();
                screenHeight = myView.mypage.getMeasuredHeight();

                scaleFactor = processSong.addViewsToScreen(getActivity(), testPane, myView.pageHolder, myView.songView, screenWidth, screenHeight,
                        myView.col1, myView.col2, myView.col3, autoScale, songAutoScaleOverrideFull,
                        songAutoScaleOverrideWidth, songAutoScaleColumnMaximise, fontSize, fontSizeMin, fontSizeMax,
                        sectionViews, sectionWidths, sectionHeights);



                Animation animSlide;
                if (R2L) {
                    animSlide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
                } else {
                    animSlide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
                }
                // Load up the highlighter file if it exists and the user wants it
                dealWithHighlighterFile(animSlide);

                myView.songView.startAnimation(animSlide);
                testPane.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        for (View view:sectionViews) {
            testPane.addView(view);
        }
        screenGrab = myView.songView;
        ViewTreeObserver screenShotObs = screenGrab.getViewTreeObserver();
        screenShotObs.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (myView!=null && myView.songView!=null) {
                    screenGrab.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // Now take a screenshot if we've passed the first layout pass
                    screenGrab.postDelayed(() -> getActivity().runOnUiThread(() -> getScreenshot()), 2000);
                    screenShotReady = true;
                }
            }
        });
    }

    private void dealWithHighlighterFile(Animation animSlide) {
        // Get the dimensions of the songview once it has drawn
        ViewTreeObserver viewTreeObserver = myView.songView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int w = myView.songView.getMeasuredWidth();
                int h = myView.songView.getMeasuredHeight();
                Log.d(TAG,"w="+w+"  h="+h);

                // Set the highlighter image view to match
                ViewGroup.LayoutParams layoutParams = myView.highlighterView.getLayoutParams();
                layoutParams.width = w;
                layoutParams.height = h;
                myView.highlighterView.setLayoutParams(layoutParams);
                // Load in the bitmap with these dimensions
                Bitmap highlighterBitmap = processSong.getHighlighterFile(requireContext(),preferences,
                        storageAccess,mainActivityInterface.getSong(),w,h);
                Log.d(TAG,"Bitmap="+highlighterBitmap);
                if (highlighterBitmap!=null &&
                        preferences.getMyPreferenceBoolean(requireContext(),"drawingAutoDisplay", true)) {
                    myView.highlighterView.setVisibility(View.VISIBLE);
                    GlideApp.with(requireContext()).load(highlighterBitmap).
                            override(w,h).into(myView.highlighterView);
                    myView.highlighterView.startAnimation(animSlide);
                    // Hide after a certain length of time
                    int timetohide = preferences.getMyPreferenceInt(requireContext(),"timeToDisplayHighlighter",0);
                    Log.d(TAG,"timetohide="+timetohide);
                    if (timetohide !=0) {
                        new Handler().postDelayed(() -> myView.highlighterView.setVisibility(View.GONE),timetohide);
                    }
                } else {
                    myView.highlighterView.setVisibility(View.GONE);
                }
                // Remove the listener now we're done with it
                myView.songView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    // The scale and gesture bits of the code
    //private ScaleGestureDetector scaleDetector;
    static float scaleFactor = 1.0f;
    //private GestureDetector detector;
    @SuppressLint("ClickableViewAccessibility")
    private void setGestureListeners(){
        /*detector = new GestureDetector(getActivity(), new GestureListener(myView.songscrollview,
                myView.horizontalscrollview,swipeMinimumDistance,swipeMaxDistanceYError,swipeMinimumVelocity,
                oktoRegisterGesture(),preferences.getMyPreferenceInt(getContext(),"doubleTapGesture",2),
                performanceGestures));
        myView.mypage.setOnTouchListener(new MyTouchListener());
        myView.songscrollview.setOnTouchListener(new MyTouchListener());
        myView.horizontalscrollview.setOnTouchListener(new MyTouchListener());
        scaleDetector = new ScaleGestureDetector(getActivity(), new PinchToZoomGestureListener(myView.pageHolder));*/
        /*detector = new GestureDetector(getActivity(), new GestureListener(myView.zoomLayout,swipeMinimumDistance,swipeMaxDistanceYError,swipeMinimumVelocity,
                oktoRegisterGesture(),preferences.getMyPreferenceInt(getContext(),"doubleTapGesture",2),
                performanceGestures));*/
    }

    private void prepareSongLoad() {
        // TODO
        /*ArrayList<Song> songsList = sqLiteHelper.getSongsByFilters(getActivity(), commonSQL,
                false,false,false,false,false,
                null,null,null,null,null);

        // Get current index
        int currentPosition = StaticVariables.songsInList.indexOf(StaticVariables.songfilename);
        if (loadNextSong) {
            loadNextSong = false;
            if (currentPosition<StaticVariables.songsInList.size()-1) {
                StaticVariables.songfilename = StaticVariables.songsInList.get(currentPosition+1);
                doSongLoad();
            } else {
                showToast.doIt(getActivity(), getString(R.string.lastsong));
            }
        } else if (loadPrevSong) {
            loadPrevSong = false;
            if (currentPosition>0) {
                StaticVariables.songfilename = StaticVariables.songsInList.get(currentPosition-1);
                doSongLoad();
            } else {
                showToast.doIt(getActivity(), getString(R.string.firstsong));
            }
        }*/
    }
    public void onBackPressed() {
        Log.d(TAG,"On back press!!!");
    }

    private boolean oktoRegisterGesture() {
        //TODO
        return true;
    }

    boolean screenShotReady = false;
    View screenGrab;
    private void getScreenshot() {
        if (screenShotReady) {
            screenGrab.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            Bitmap bitmap = Bitmap.createBitmap(screenGrab.getMeasuredWidth(), screenGrab.getMeasuredHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            screenGrab.layout(0, 0, screenGrab.getMeasuredWidth(), screenGrab.getMeasuredHeight());
            screenGrab.draw(canvas);
            mainActivityInterface.setScreenshot(bitmap);
            /*
            myView.songView.destroyDrawingCache();
            myView.songView.setDrawingCacheEnabled(true);
            myView.songView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);*/

            //GlideApp.with(screenGrab).load(mainActivityInterface.getScreenshot()).into(myView.glideimage);

            //Log.d("d","song.getScreenshot:"+mainActivityInterface.getScreenshot());
        }
    }
}

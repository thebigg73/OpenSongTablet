package com.garethevans.church.opensongtablet.performance;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.garethevans.church.opensongtablet.databinding.FragmentPerformanceBinding;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.metronome.Metronome;
import com.garethevans.church.opensongtablet.pads.PadFunctions;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.DoVibrate;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.screensetup.ThemeColors;
import com.garethevans.church.opensongtablet.setprocessing.SetActions;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songsandsetsmenu.SongListBuildIndex;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.util.ArrayList;

public class PerformanceFragment extends Fragment {

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

    //private ShowCase showCase;

    //private LoadSongInterface loadSongInterface;
    private MainActivityInterface mainActivityInterface;

    // The variables used in the fragment
    private float scaleHeadings, scaleComments, scaleChords, fontSize, fontSizeMin, fontSizeMax,
            lineSpacing;
    private int swipeMinimumDistance, swipeMaxDistanceYError, swipeMinimumVelocity;
    private boolean trimLines, trimSections, addSectionSpace, songAutoScaleColumnMaximise,
            songAutoScaleOverrideFull, songAutoScaleOverrideWidth, boldChordHeading;
    static boolean wasScaling, R2L, loadNextSong, loadPrevSong;
    private static int screenHeight;
    public static int songViewWidth, songViewHeight, screenWidth;
    private RelativeLayout testPane;
    private ArrayList<View> sectionViews;
    private ArrayList<Integer> sectionWidths, sectionHeights;
    private String autoScale;
    private FragmentPerformanceBinding myView;

    private Song song;

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

        myView = FragmentPerformanceBinding.inflate(inflater, container, false);
        View root = myView.getRoot();

        // Initialise the helper classes that do the heavy lifting
        initialiseHelpers();
        mainActivityInterface.lockDrawer(false);
        mainActivityInterface.hideActionBar(false);
        mainActivityInterface.hideActionButton(false);

        mainActivityInterface.changeActionBarVisible(false,false);
        // Load in preferences
        loadPreferences();

        // Prepare the song menu (will be called again after indexing from the main activity index songs)
        mainActivityInterface.fullIndex();

        doSongLoad();

        // Set listeners for the scroll/scale/gestures
        //setGestureListeners();

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
    public void doSongLoad() {
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
                myView.zoomLayout.moveTo(1,0,0,false);
            });
            // Load up the song
            if (sectionViews!=null) {
                sectionViews.clear();
            }
            song = mainActivityInterface.getSong();
            song = processSong.initialiseSong(commonSQL,song.getFolder(), song.getFilename(), song);
            song = loadSong.doLoadSong(getContext(),storageAccess,preferences,processSong,
                    showToast, mainActivityInterface.getLocale(), songListBuildIndex, sqLiteHelper, commonSQL, song, convertOnSong, convertChoPro, false);

            requireActivity().runOnUiThread(this::prepareSongViews);
            mainActivityInterface.moveToSongInSongMenu();
        }).start();
    }
    private void prepareSongViews() {
        // This is called on UI thread above;
        myView.pageHolder.setBackgroundColor(themeColors.getLyricsBackgroundColor());
        // Get the song in the layout
        sectionViews = processSong.setSongInLayout(getActivity(),preferences, mainActivityInterface.getLocale(),
                trimSections, addSectionSpace, trimLines, lineSpacing, themeColors, setTypeFace,
                scaleHeadings, scaleChords, scaleComments, song.getLyrics(), boldChordHeading);

        // We now have the 1 column layout ready, so we can set the view observer to measure once drawn
        setUpVTO();

        // Update the toolbar
        mainActivityInterface.updateToolbar(song,null);
    }
    private void setUpVTO() {
        testPane = myView.testPane;
        ViewTreeObserver vto = testPane.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                testPane.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // All views have now been drawn, so measure the arraylist views
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

                scaleFactor = 1.0f;

                processSong.addViewsToScreen(getActivity(), testPane, myView.pageHolder, myView.songView, screenWidth, screenHeight,
                        myView.col1, myView.col2, myView.col3, autoScale, songAutoScaleOverrideFull,
                        songAutoScaleOverrideWidth, songAutoScaleColumnMaximise, fontSize, fontSizeMin, fontSizeMax,
                        sectionViews, sectionWidths, sectionHeights);

                Animation animSlide;
                if (R2L) {
                    animSlide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
                } else {
                    animSlide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
                }
                myView.songView.startAnimation(animSlide);
                //myView.songscrollview.setLayoutParams(new HorizontalScrollView.LayoutParams(HorizontalScrollView.LayoutParams.WRAP_CONTENT, HorizontalScrollView.LayoutParams.WRAP_CONTENT));
                //myView.horizontalscrollview.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                //scrollButtons.showScrollButtons(myView.songscrollview,myView.upArrow,myView.pageButtonsBottom.downArrow);
            }
        });
        for (View view:sectionViews) {
            testPane.addView(view);
        }
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
        Log.d("PerformanceFragment","On back press!!!");
    }

    private boolean oktoRegisterGesture() {
        //TODO
        return true;
    }


}

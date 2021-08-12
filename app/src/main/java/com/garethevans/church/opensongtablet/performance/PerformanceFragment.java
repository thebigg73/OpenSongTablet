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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.databinding.ModePerformanceBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.stickynotes.StickyPopUp;

public class PerformanceFragment extends Fragment {

    private final String TAG = "PerformanceFragment";
    // Helper classes for the heavy lifting
    private StickyPopUp stickyPopUp;

    private MainActivityInterface mainActivityInterface;

    // The variables used in the fragment
    private float scaleHeadings, scaleComments, scaleChords, fontSize, fontSizeMin, fontSizeMax,
            lineSpacing;
    private boolean trimLines, trimSections, addSectionSpace, songAutoScaleColumnMaximise,
            songAutoScaleOverrideFull, songAutoScaleOverrideWidth, boldChordHeading,
            highlightChords,highlightHeadings;
    static boolean wasScaling, R2L, loadNextSong, loadPrevSong;
    private static int screenHeight;
    public static int songViewWidth, songViewHeight, screenWidth;
    private String autoScale;
    private ModePerformanceBinding myView;
    private Animation animSlideIn, animSlideOut;
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
        dealWithStickyNotes(false,true);
        if (mainActivityInterface.getAppActionBar()!=null) {
            mainActivityInterface.getAppActionBar().setPerformanceMode(false);
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

        Log.d(TAG,"binding");
        myView = ModePerformanceBinding.inflate(inflater, container, false);
        View root = myView.getRoot();

        // Initialise the helper classes that do the heavy lifting
        Log.d(TAG,"binding");
        initialiseHelpers();

        mainActivityInterface.lockDrawer(false);
        //mainActivityInterface.hideActionBar(false);
        mainActivityInterface.hideActionButton(false);

        //mainActivityInterface.changeActionBarVisible(false,false);
        // Load in preferences
        Log.d(TAG,"loadPreferences()");
        loadPreferences();

        // Prepare the song menu (will be called again after indexing from the main activity index songs)
        if (mainActivityInterface.getSongListBuildIndex().getIndexRequired() &&
                !mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
            mainActivityInterface.fullIndex();
        }

        Log.d(TAG,"doSongLoad()");
        doSongLoad(mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"whichSongFolder",getString(R.string.mainfoldername)),
                mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"songfilename","Welcome to OpenSongApp"));

        // Set listeners for the scroll/scale/gestures
        //setGestureListeners();

        // Show the actionBar and hide it after a time if that's the user's preference
        mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"hideActionBar",false);

        mainActivityInterface.getAppActionBar().setHideActionBar(mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"hideActionBar",false));
        mainActivityInterface.getAppActionBar().setPerformanceMode(true);
        mainActivityInterface.getAppActionBar().showActionBar();

        // Set tutorials
        Handler h = new Handler();
        Runnable r = () -> mainActivityInterface.showTutorial("performanceView");
        h.postDelayed(r,1000);

        return root;
    }

    // Getting the preferences and helpers ready
    private void initialiseHelpers() {
        stickyPopUp = new StickyPopUp();
/*
        performanceGestures = new PerformanceGestures(getContext(),preferences,storageAccess,setActions,
                padFunctions,metronome,this,mainActivityInterface,showToast,doVibrate,
                mainActivityInterface.getDrawer(),mainActivityInterface.getMediaPlayer(1),
                mainActivityInterface.getMediaPlayer(2),mainActivityInterface.getAppActionBar(), 0xffff0000);*/
    }
    private void loadPreferences() {
        mainActivityInterface.getMyThemeColors().getDefaultColors(getContext(),mainActivityInterface);
        scaleHeadings = mainActivityInterface.getPreferences().getMyPreferenceFloat(getActivity(),"scaleHeadings",0.6f);
        scaleChords = mainActivityInterface.getPreferences().getMyPreferenceFloat(getActivity(),"scaleChords",0.8f);
        scaleComments = mainActivityInterface.getPreferences().getMyPreferenceFloat(getActivity(),"scaleComments",0.8f);
        trimLines = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(),"trimLines",true);
        lineSpacing = mainActivityInterface.getPreferences().getMyPreferenceFloat(getActivity(),"lineSpacing",0.1f);
        trimSections = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(),"trimSections",true);
        boldChordHeading = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(), "displayBoldChordsHeadings", false);
        addSectionSpace = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(), "addSectionSpace", true);
        autoScale = mainActivityInterface.getPreferences().getMyPreferenceString(getActivity(),"songAutoScale","W");
        songAutoScaleColumnMaximise = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(),"songAutoScaleColumnMaximise",true);
        fontSize = mainActivityInterface.getPreferences().getMyPreferenceFloat(getActivity(),"fontSize",42.0f);
        fontSizeMax = mainActivityInterface.getPreferences().getMyPreferenceFloat(getActivity(),"fontSizeMax",50.0f);
        fontSizeMin = mainActivityInterface.getPreferences().getMyPreferenceFloat(getActivity(),"fontSizeMin",8.0f);
        songAutoScaleOverrideFull = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(),"songAutoScaleOverrideFull",true);
        songAutoScaleOverrideWidth = mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(),"songAutoScaleOverrideWidth",false);
        int swipeMinimumDistance = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(), "swipeMinimumDistance", 250);
        int swipeMaxDistanceYError = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(), "swipeMaxDistanceYError", 200);
        int swipeMinimumVelocity = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(), "swipeMinimumVelocity", 600);
        highlightChords = mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"highlightChords",false);
        highlightHeadings = mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"highlightHeadings",false);
        fontSizeMax = 90.0f;
        songAutoScaleOverrideWidth = false;
        songAutoScaleOverrideFull = false;
        myView.mypage.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
    }




    private void resetTitleSizes() {
        mainActivityInterface.updateActionBarSettings("songTitleSize",-1,
                mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),"songTitleSize",13.0f),true);
        mainActivityInterface.updateActionBarSettings("songAuthorSize",-1,
                mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),"songAuthorSize",11.0f),true);
    }
    // Displaying the song
    public void doSongLoad(String folder, String filename) {
        // Loading the song is dealt with in this fragment as specific actions are required

        // During the load song call, the song is cleared
        // However if first extracts the folder and filename we've just set
        mainActivityInterface.getSong().setFolder(folder);
        mainActivityInterface.getSong().setFilename((filename));

        new Thread(() -> {
            // Quick fade the current page
            requireActivity().runOnUiThread(() -> {
                if (R2L) {
                    animSlideOut = AnimationUtils.loadAnimation(requireActivity(), R.anim.slide_out_left);
                } else {
                    animSlideOut = AnimationUtils.loadAnimation(requireActivity(), R.anim.slide_out_right);
                }
                myView.songSheetTitle.startAnimation(animSlideOut);
                myView.songView.startAnimation(animSlideOut);
                myView.highlighterView.startAnimation(animSlideOut);
                myView.imageView.startAnimation(animSlideOut);
                myView.zoomLayout.scrollTo(0,0);
            });

            // Reset the views
            mainActivityInterface.setSectionViews(null);
            requireActivity().runOnUiThread(() -> mainActivityInterface.setSongSheetTitleLayout(null));

            // Now reset the song
            mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSong(getContext(),mainActivityInterface,
                    mainActivityInterface.getSong(),false));

            requireActivity().runOnUiThread(this::prepareSongViews);
            mainActivityInterface.moveToSongInSongMenu();
        }).start();
    }
    private void prepareSongViews() {
        // This is called on UI thread above;
        // Set the default color
        myView.pageHolder.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());

        // Remove old views
        myView.songSheetTitle.removeAllViews();
        if (mainActivityInterface.getSongSheetTitleLayout()!=null &&
                mainActivityInterface.getSongSheetTitleLayout().getParent()!=null) {
            ((LinearLayout) mainActivityInterface.getSongSheetTitleLayout().getParent()).removeAllViews();
        }

        // Get the song sheet headers
        mainActivityInterface.setSongSheetTitleLayout(mainActivityInterface.getSongSheetHeaders().getSongSheet(requireContext(),
                mainActivityInterface,mainActivityInterface.getSong(),scaleComments,false));
        myView.songSheetTitle.addView(mainActivityInterface.getSongSheetTitleLayout());

        // Now prepare the song sections views so we can measure them for scaling using a view tree observer
        mainActivityInterface.setSectionViews(mainActivityInterface.getProcessSong().
                setSongInLayout(requireContext(),mainActivityInterface, trimSections, addSectionSpace,
                        trimLines, lineSpacing, scaleHeadings, scaleChords, scaleComments,
                        mainActivityInterface.getSong().getLyrics(),boldChordHeading,false));

        // We now have the 1 column layout ready, so we can set the view observer to measure once drawn
        setUpTestViewListener();

        // Update the toolbar with the song (null)
        mainActivityInterface.updateToolbar(null);
    }

    private void setUpTestViewListener() {
        ViewTreeObserver testObs = myView.testPane.getViewTreeObserver();
        testObs.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // The views are ready so prepare to create the song page
                songIsReadyToDisplay();
                // We can now remove this listener
                myView.testPane.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        for (View view:mainActivityInterface.getSectionViews()) {
            myView.testPane.addView(view);
        }
    }
    private void songIsReadyToDisplay(){
        // All views have now been drawn, so measure the arraylist views

        for (View v:mainActivityInterface.getSectionViews())  {
            int width = v.getMeasuredWidth();
            int height = v.getMeasuredHeight();
            mainActivityInterface.addSectionSize(width,height);
        }

        screenWidth = myView.mypage.getMeasuredWidth();
        screenHeight = myView.mypage.getMeasuredHeight();

        scaleFactor = mainActivityInterface.getProcessSong().addViewsToScreen(getContext(),
                mainActivityInterface,
                myView.testPane, myView.pageHolder, myView.songView, myView.songSheetTitle,
                screenWidth, screenHeight,
                myView.col1, myView.col2, myView.col3, autoScale, songAutoScaleOverrideFull,
                songAutoScaleOverrideWidth, songAutoScaleColumnMaximise, fontSize, fontSizeMin, fontSizeMax);

        // Set up the type of animate in
        if (R2L) {
            animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        } else {
            animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
        }

        // Now that the view is being drawn, set a view tree observer to get the sizes once done
        // Then we can switch on the highlighter, sticky notes, etc.
        ViewTreeObserver songViewObs = myView.songView.getViewTreeObserver();
        songViewObs.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (myView != null) {
                    // Get the width and height of the view
                    int w = myView.songView.getMeasuredWidth();
                    int h = myView.songView.getMeasuredHeight();

                    // Now deal with the highlighter file
                    dealWithHighlighterFile(w,h);

                    // Load up the sticky notes if the user wants them
                    dealWithStickyNotes(false,false);

                    // Now take a screenshot (only runs is w!=0 and h!=0)
                    myView.songView.postDelayed(() -> requireActivity().runOnUiThread(() -> getScreenshot(w,h)), 2000);

                    // Now remove this viewtree observer
                    myView.songView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
        myView.songView.startAnimation(animSlideIn);
        myView.songSheetTitle.startAnimation(animSlideIn);
    }
    private void dealWithHighlighterFile(int w, int h) {
        if (!mainActivityInterface.getPreferences().
                getMyPreferenceString(requireContext(),"songAutoScale","W").equals("N")) {
            // Set the highlighter image view to match
            ViewGroup.LayoutParams layoutParams = myView.highlighterView.getLayoutParams();
            layoutParams.width = w;
            layoutParams.height = h;
            myView.highlighterView.setLayoutParams(layoutParams);
            // Load in the bitmap with these dimensions
            Bitmap highlighterBitmap = mainActivityInterface.getProcessSong().
                    getHighlighterFile(requireContext(), mainActivityInterface, w, h);
            if (highlighterBitmap != null &&
                    mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(), "drawingAutoDisplay", true)) {
                myView.highlighterView.setVisibility(View.VISIBLE);
                GlideApp.with(requireContext()).load(highlighterBitmap).
                        override(w, h).into(myView.highlighterView);
                myView.highlighterView.startAnimation(animSlideIn);
                // Hide after a certain length of time
                int timetohide = mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(), "timeToDisplayHighlighter", 0);
                if (timetohide != 0) {
                    new Handler().postDelayed(() -> myView.highlighterView.setVisibility(View.GONE), timetohide);
                }
            } else {
                myView.highlighterView.setVisibility(View.GONE);
            }
        } else {
            myView.highlighterView.setVisibility(View.GONE);
        }
    }
    public void dealWithStickyNotes(boolean forceShow, boolean hide) {
        Log.d(TAG, "dealWithStickyNotes");
        if (hide) {
            if (stickyPopUp!=null) {
                stickyPopUp.closeSticky();
            }
        } else {
            if ((mainActivityInterface != null && mainActivityInterface.getSong() != null &&
                    mainActivityInterface.getSong().getNotes() != null &&
                    !mainActivityInterface.getSong().getNotes().isEmpty() &&
                    mainActivityInterface.getPreferences().
                            getMyPreferenceBoolean(requireContext(), "stickyAuto", true)) || forceShow) {
                // This is called from the MainActivity when we clicked on the page button

                stickyPopUp.floatSticky(requireContext(), mainActivityInterface, myView.pageHolder, forceShow);
            }
        }
    }

    // The scale and gesture bits of the code
    //private ScaleGestureDetector scaleDetector;
    private float scaleFactor = 1.0f;
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

    View screenGrab;
    private void getScreenshot(int w, int h) {
        if (!mainActivityInterface.getPreferences().
                getMyPreferenceString(requireContext(),"songAutoScale","W").equals("N")
                && w!=0 && h!=0) {
            try {
                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                if (myView!=null && myView.songView!=null) {
                    myView.songView.layout(0, 0, w, h);
                    myView.songView.draw(canvas);
                    mainActivityInterface.setScreenshot(bitmap);
                }
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
    }
}

package com.garethevans.church.opensongtablet.performance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.controls.GestureListener;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.customviews.MyZoomLayout;
import com.garethevans.church.opensongtablet.databinding.ModePerformanceBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.pdf.PDFPageAdapter;
import com.garethevans.church.opensongtablet.stage.StageSectionAdapter;
import com.garethevans.church.opensongtablet.stickynotes.StickyPopUp;

import java.util.Locale;

public class PerformanceFragment extends Fragment {

    // TODO TIDY UP
    private final String TAG = "PerformanceFragment";
    // Helper classes for the heavy lifting
    private StickyPopUp stickyPopUp;

    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;

    // The variables used in the fragment
    private boolean songAutoScaleColumnMaximise,
            songAutoScaleOverrideFull, songAutoScaleOverrideWidth;
    private boolean songSheetTitleReady, songViewsReady;
    private int screenHeight;
    public int songViewWidth, songViewHeight, screenWidth, swipeMinimumDistance,
            swipeMaxDistanceYError, swipeMinimumVelocity;
    private String autoScale;
    private ModePerformanceBinding myView;
    private Animation animSlideIn, animSlideOut;
    private GestureDetector gestureDetector;
    private PDFPageAdapter pdfPageAdapter;
    private StageSectionAdapter stageSectionAdapter;

    // Attaching and destroying
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        displayInterface = (DisplayInterface) context;
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

        myView = ModePerformanceBinding.inflate(inflater, container, false);

        // Register this fragment
        mainActivityInterface.registerFragment(this,"Performance");

        // Initialise the helper classes that do the heavy lifting
        initialiseHelpers();

        // Pass view references to the Autoscroll class
        mainActivityInterface.getAutoscroll().initialiseAutoscroll(myView.zoomLayout, myView.recyclerView);

        mainActivityInterface.lockDrawer(false);
        mainActivityInterface.hideActionButton(false);

        //mainActivityInterface.changeActionBarVisible(false,false);
        // Load in preferences
        loadPreferences();

        // Prepare the song menu (will be called again after indexing from the main activity index songs)
        if (mainActivityInterface.getSongListBuildIndex().getIndexRequired() &&
                !mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
            mainActivityInterface.fullIndex();
        }

        doSongLoad(mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"whichSongFolder",getString(R.string.mainfoldername)),
                mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"songfilename","Welcome to OpenSongApp"));

        // Set listeners for the scroll/scale/gestures
        setGestureListeners();

        // Show the actionBar and hide it after a time if that's the user's preference
        mainActivityInterface.getAppActionBar().setPerformanceMode(true);
        mainActivityInterface.showHideActionBar();

        // Set tutorials
        Handler h = new Handler();
        Runnable r = () -> mainActivityInterface.showTutorial("performanceView",null);
        h.postDelayed(r,1000);

        // MainActivity initialisation has firstRun set as true.
        // Check for connected displays now we have loaded preferences, etc
        if (mainActivityInterface.getFirstRun()) {
            displayInterface.checkDisplays();
            mainActivityInterface.setFirstRun(false);
        }

        return myView.getRoot();
    }

    // Getting the preferences and helpers ready
    private void initialiseHelpers() {
        stickyPopUp = new StickyPopUp();
        mainActivityInterface.getPerformanceGestures().setZoomLayout(myView.zoomLayout);
        mainActivityInterface.getPerformanceGestures().setPDFRecycler(myView.recyclerView);
        myView.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
    }
    private void loadPreferences() {
        mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(),mainActivityInterface);
        mainActivityInterface.getMyThemeColors().getDefaultColors(getContext(),mainActivityInterface);
        swipeMinimumDistance = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(), "swipeMinimumDistance", 250);
        swipeMaxDistanceYError = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(), "swipeMaxDistanceYError", 200);
        swipeMinimumVelocity = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(), "swipeMinimumVelocity", 600);
        songAutoScaleOverrideWidth = false;
        songAutoScaleOverrideFull = false;
        if (mainActivityInterface.getMode().equals("Performance")) {
            myView.mypage.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
            myView.waterMark.setVisibility(View.VISIBLE);
        } else {
            // Stage Mode - sections have correct colour, but the background is different - set to colorPrimary
            myView.mypage.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            myView.waterMark.setVisibility(View.GONE);
        }
    }

    // Displaying the song
    public void doSongLoad(String folder, String filename) {
        // Loading the song is dealt with in this fragment as specific actions are required

        // Stop any autoscroll if required
        mainActivityInterface.getAutoscroll().stopAutoscroll();

        // During the load song call, the song is cleared
        // However if first extracts the folder and filename we've just set
        mainActivityInterface.getSong().setFolder(folder);
        mainActivityInterface.getSong().setFilename((filename));

        new Thread(() -> {
            // Quick fade the current page
            if (mainActivityInterface.getDisplayPrevNext().getSwipeDirection().equals("R2L")) {
                animSlideOut = AnimationUtils.loadAnimation(requireActivity(), R.anim.slide_out_left);
            } else {
                animSlideOut = AnimationUtils.loadAnimation(requireActivity(), R.anim.slide_out_right);
            }

            // Now reset the song
            mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSong(getContext(),mainActivityInterface,
                    mainActivityInterface.getSong(),false));

            mainActivityInterface.moveToSongInSongMenu();

            // Now animate out the song and after a delay start the next bit of the processing
            myView.recyclerView.post(() -> {
                if (myView.recyclerView.getVisibility()==View.VISIBLE) {
                    myView.recyclerView.startAnimation(animSlideOut);
                    myView.recyclerView.postDelayed(() -> requireActivity().runOnUiThread(this::prepareSongViews),requireContext().getResources().getInteger(R.integer.slide_out_time));
                }
            });

            myView.pageHolder.post(() -> {
                if (myView.pageHolder.getVisibility()==View.VISIBLE) {
                    myView.pageHolder.startAnimation(animSlideOut);
                    myView.pageHolder.postDelayed(() -> requireActivity().runOnUiThread(this::prepareSongViews),requireContext().getResources().getInteger(R.integer.slide_out_time));
                }
            });
        }).start();
    }
    private void prepareSongViews() {
        // This is called on the UI thread above
        // Reset the song views
        mainActivityInterface.setSectionViews(null);

        // Reset the song sheet titles
        mainActivityInterface.getSongSheetTitleLayout().removeAllViews();
        myView.songSheetTitle.removeAllViews();
        myView.recyclerView.removeAllViews();
        // Set the default color
        myView.pageHolder.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());

        myView.recyclerView.setVisibility(View.GONE);
        myView.zoomLayout.setVisibility(View.GONE);

        int[] screenSizes = mainActivityInterface.getDisplayMetrics();
        screenWidth = screenSizes[0];
        screenHeight = screenSizes[1] - mainActivityInterface.getAppActionBar().getActionBarHeight();

        if (mainActivityInterface.getSong().getFilename().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            // We populate the recyclerView with the pages of the PDF

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                int availWidth = getResources().getDisplayMetrics().widthPixels;
                int availHeight = getResources().getDisplayMetrics().heightPixels - mainActivityInterface.getMyActionBar().getHeight();
                pdfPageAdapter = new PDFPageAdapter(requireContext(), mainActivityInterface, displayInterface,
                        availWidth, availHeight);

                myView.recyclerView.setAdapter(pdfPageAdapter);
                myView.recyclerView.setVisibility(View.VISIBLE);

                // Set up the type of animate in
                if (mainActivityInterface.getDisplayPrevNext().getSwipeDirection().equals("R2L")) {
                    animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
                } else {
                    animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
                }
                myView.recyclerView.startAnimation(animSlideIn);

                // Send the autoscroll information (if required)
                int totalHeight = pdfPageAdapter.getHeight();
                myView.recyclerView.setMaxScrollY(totalHeight - screenHeight);
                mainActivityInterface.getAutoscroll().initialiseSongAutoscroll(requireContext(), totalHeight, screenHeight);

                // Get a null screenshot
                getScreenshot(0,0,0);

                // Set the previous/next if we want to
                mainActivityInterface.getDisplayPrevNext().setPrevNext(requireContext());

            }
        } else if (mainActivityInterface.getSong().getFiletype().equals("XML")) {
            // Now prepare the song sections views so we can measure them for scaling using a view tree observer
            mainActivityInterface.setSectionViews(mainActivityInterface.getProcessSong().
                    setSongInLayout(requireContext(), mainActivityInterface,
                            mainActivityInterface.getSong(), false, false));

            // Prepare the song sheet header if required, if not, make it null
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(), "songSheet", false)) {
                mainActivityInterface.setSongSheetTitleLayout(mainActivityInterface.getSongSheetHeaders().getSongSheet(requireContext(),
                        mainActivityInterface, mainActivityInterface.getSong(), mainActivityInterface.getProcessSong().getScaleComments(), false));
            } else {
                mainActivityInterface.setSongSheetTitleLayout(null);
            }

            // We now have the views ready, we need to draw them so we can measure them
            // Start with the song sheeet title/header
            setUpHeaderListener();
        }

        // Update the toolbar with the song (null)
        mainActivityInterface.updateToolbar(null);
    }

    private void setUpHeaderListener() {
        // If we want headers, the header layout isn't null, so we can draw and listen
        // Add the view and wait for the vto return
        if (mainActivityInterface.getSongSheetTitleLayout() != null && mainActivityInterface.getMode().equals("Performance")) {

            // Check the header isn't already attached to a view
            if (mainActivityInterface.getSongSheetTitleLayout().getParent()!=null) {
                ((ViewGroup) mainActivityInterface.getSongSheetTitleLayout().getParent()).removeAllViews();
            }

            ViewTreeObserver vto = myView.testPane.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @Override
                public void onGlobalLayout() {
                    myView.testPane.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    setUpTestViewListener();
                }
            });

            try {
                myView.testPane.addView(mainActivityInterface.getSongSheetTitleLayout());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // No song sheet title requested, so skip
            setUpTestViewListener();
        }
    }

    private void setUpTestViewListener() {
        myView.testPane.removeAllViews();
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
        // Add the views and wait for the vto
        for (View view : mainActivityInterface.getSectionViews()) {
            myView.testPane.addView(view);
        }
    }

    private void songIsReadyToDisplay() {
        // All views have now been drawn, so measure the arraylist views
        for (int x=0; x<mainActivityInterface.getSectionViews().size(); x++) {
            int width = mainActivityInterface.getSectionViews().get(x).getMeasuredWidth();
            int height = mainActivityInterface.getSectionViews().get(x).getMeasuredHeight();
            mainActivityInterface.addSectionSize(x, width, height);
        }

        myView.testPane.removeAllViews();

        // Set up the type of animate in
        if (mainActivityInterface.getDisplayPrevNext().getSwipeDirection().equals("R2L")) {
            animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        } else {
            animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
        }

        // Decide which mode we are in to determine how the views are rendered
        if (mainActivityInterface.getMode().equals("Stage")) {
            // We are in Stage mode so use the recyclerView
            stageSectionAdapter = new StageSectionAdapter(requireContext(),mainActivityInterface,displayInterface);

            myView.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
            myView.recyclerView.setAdapter(stageSectionAdapter);
            myView.recyclerView.setVisibility(View.VISIBLE);
            if (myView.recyclerView.getItemAnimator()!=null) {
                ((SimpleItemAnimator) myView.recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            }
            // Set up the type of animate in
            if (mainActivityInterface.getDisplayPrevNext().getSwipeDirection().equals("R2L")) {
                animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
            } else {
                animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
            }
            myView.recyclerView.startAnimation(animSlideIn);

            // Send the autoscroll information (if required)
            int totalHeight = stageSectionAdapter.getTotalHeight();
            myView.recyclerView.setMaxScrollY(totalHeight - screenHeight);
            mainActivityInterface.getAutoscroll().initialiseSongAutoscroll(requireContext(), totalHeight, screenHeight);

            // Get a null screenshot
            getScreenshot(0,0,0);

        } else {
            // We are in Performance mode, so use the songView
            myView.zoomLayout.setVisibility(View.VISIBLE);
            myView.zoomLayout.setPageSize(screenWidth, screenHeight);

            scaleFactor = mainActivityInterface.getProcessSong().addViewsToScreen(requireContext(),
                    mainActivityInterface,
                    myView.pageHolder, myView.songView, myView.songSheetTitle,
                    screenWidth, screenHeight,
                    myView.col1, myView.col2, myView.col3);

            // Pass this scale factor to the zoom layout
            myView.zoomLayout.setCurrentScale(scaleFactor);

            float maxWidth = 0;
            float totalHeight = 0;
            for (int x = 0; x < mainActivityInterface.getSectionViews().size(); x++) {
                maxWidth = Math.max(maxWidth, mainActivityInterface.getSectionWidths().get(x));
                totalHeight += mainActivityInterface.getSectionHeights().get(x);
            }
            final int w = (int) (maxWidth*scaleFactor);
            final int h = (int) (totalHeight*scaleFactor);

            myView.zoomLayout.setSongSize(w, h + (int)(mainActivityInterface.getSongSheetTitleLayout().getHeight()*scaleFactor));

            // Because we might have padded the view at the top for song sheet header scaling:
            int topPadding = 0;
            if (myView.songView.getChildCount()>0 && myView.songView.getChildAt(0)!=null) {
                topPadding = myView.songView.getChildAt(0).getPaddingTop();
            }

            // Now deal with the highlighter file
            dealWithHighlighterFile((int)(maxWidth), (int)(totalHeight));

            // Send the autoscroll information (if required)
            mainActivityInterface.getAutoscroll().initialiseSongAutoscroll(requireContext(), h, screenHeight);

            myView.pageHolder.startAnimation(animSlideIn);

            // Try to take a screenshot ready for any highlighter actions that may be called
            int finalTopPadding = topPadding;
            myView.songView.post(() -> {
                try {
                    getScreenshot(w,h, finalTopPadding);
                } catch (Exception | OutOfMemoryError e) {
                    e.printStackTrace();
                }
            });
        }

        // Load up the sticky notes if the user wants them
        dealWithStickyNotes(false, false);

        // IV - Consume any later pending client section change received from Host (-ve value)
        if (mainActivityInterface.getSong().getCurrentSection() < 0) {
            mainActivityInterface.getSong().setCurrentSection(-(1 + mainActivityInterface.getSong().getCurrentSection()));
        }

        // Set the previous/next if we want to
        mainActivityInterface.getDisplayPrevNext().setPrevNext(requireContext());

        // Start the pad (if the pads are activated and the pad is valid)
        mainActivityInterface.getPad().autoStartPad(requireContext());

        // Update any midi commands (if any)
        mainActivityInterface.getMidi().buildSongMidiMessages();

        // Deal with capo information (if required)
        mainActivityInterface.dealWithCapo();

        // Update the secondary display (if present)
        displayInterface.updateDisplay("setSongInfo");
        displayInterface.updateDisplay("setSongContent");
    }

    private void dealWithHighlighterFile(int w, int h) {
        if (!mainActivityInterface.getPreferences().
                getMyPreferenceString(requireContext(),"songAutoScale","W").equals("N")) {
            // Set the highlighter image view to match
            myView.highlighterView.setVisibility(View.INVISIBLE);
            // Once the view is ready at the required size, deal with it
            ViewTreeObserver highlighterVTO = myView.highlighterView.getViewTreeObserver();
            highlighterVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Load in the bitmap with these dimensions
                    Bitmap highlighterBitmap = mainActivityInterface.getProcessSong().
                            getHighlighterFile(requireContext(), mainActivityInterface, 0, 0);
                    if (highlighterBitmap != null &&
                            mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(), "drawingAutoDisplay", true)) {

                        myView.highlighterView.setVisibility(View.VISIBLE);
                        ViewGroup.LayoutParams rlp = myView.highlighterView.getLayoutParams();
                        rlp.width = w;
                        rlp.height = h;
                        myView.highlighterView.setLayoutParams(rlp);
                        RequestOptions requestOptions = new RequestOptions().centerInside();
                        GlideApp.with(requireContext()).load(highlighterBitmap).
                                apply(requestOptions).
                                into(myView.highlighterView);

                        myView.highlighterView.setPivotX(0f);
                        myView.highlighterView.setPivotY(0);
                        myView.highlighterView.setTranslationX(0f);
                        myView.highlighterView.setTranslationY((mainActivityInterface.getSongSheetTitleLayout().getHeight()*scaleFactor) - mainActivityInterface.getSongSheetTitleLayout().getHeight());
                        myView.highlighterView.setScaleX(scaleFactor);
                        myView.highlighterView.setScaleY(scaleFactor);

                        // Hide after a certain length of time
                        int timetohide = mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(), "timeToDisplayHighlighter", 0);
                        if (timetohide != 0) {
                            new Handler().postDelayed(() -> myView.highlighterView.setVisibility(View.GONE), timetohide);
                        }
                    } else {
                        try {
                            myView.highlighterView.post(() -> myView.highlighterView.setVisibility(View.GONE));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        myView.highlighterView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            myView.songView.post(() -> myView.songView.getLayoutParams().height = (int)(h*scaleFactor));
            myView.highlighterView.post(() -> myView.highlighterView.requestLayout());
        } else {
            myView.highlighterView.post(() -> myView.highlighterView.setVisibility(View.GONE));
        }
    }

    public void dealWithStickyNotes(boolean forceShow, boolean hide) {
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
            } }
    }

    public void toggleHighlighter() {
        if (myView.highlighterView.getVisibility()==View.VISIBLE) {
            myView.highlighterView.setVisibility(View.GONE);
        } else {
            myView.highlighterView.setVisibility(View.VISIBLE);
        }
    }

    // The scale and gesture bits of the code
    private float scaleFactor = 1.0f;
    @SuppressLint("ClickableViewAccessibility")
    private void setGestureListeners(){
        // get the gesture detector
        gestureDetector = new GestureDetector(requireContext(), new GestureListener(mainActivityInterface,
                mainActivityInterface.getPerformanceGestures(),swipeMinimumDistance, swipeMaxDistanceYError,swipeMinimumVelocity));

        // Any interaction with the screen should trigger the display prev/next (if required)
        // It should also show the action bar
        myView.zoomLayout.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                mainActivityInterface.getDisplayPrevNext().showAndHide();
                mainActivityInterface.updateOnScreenInfo("showcapo");
                mainActivityInterface.showHideActionBar();
            }
            return gestureDetector.onTouchEvent(motionEvent);
        });

        myView.recyclerView.setGestureDetector(gestureDetector);
    }

    public void pdfScrollToPage(int pageNumber) {
        Log.d(TAG,"trying to scroll to "+pageNumber);
        LinearLayoutManager llm = (LinearLayoutManager) myView.recyclerView.getLayoutManager();
        if (llm!=null) {
            llm.scrollToPosition(pageNumber-1);
        }
    }

    private void getScreenshot(int w, int h, int topPadding) {
        if (!mainActivityInterface.getPreferences().
                getMyPreferenceString(requireContext(),"songAutoScale","W").equals("N")
                && w!=0 && h!=0) {
            try {
                Bitmap bitmap = Bitmap.createBitmap(w, h+topPadding, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                if (myView != null) {
                    myView.songView.layout(0, topPadding, w, h+topPadding);
                    myView.songView.draw(canvas);
                    Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, 0, topPadding, w, h);
                    bitmap.recycle();
                    mainActivityInterface.setScreenshot(croppedBitmap);
                }
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
    }

    public MyZoomLayout getZoomLayout() {
        return myView.zoomLayout;
    }

    public void updateSizes(int width, int height) {
        if (width<0) {
            songViewWidth = screenWidth;
        } else {
            songViewWidth = width;
        }
        songViewHeight = height;
    }


    // Received from MainActivity after a user clicked on a pdf page or a Stage Mode section
    public void performanceShowSection(int position) {
        // Scroll the recyclerView to the top of the page
        myView.recyclerView.scrollToPosition(position);
        // TODO send an update the the secondary display
    }

}

package com.garethevans.church.opensongtablet.performance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
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
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.AlertInfoBottomSheet;
import com.garethevans.church.opensongtablet.controls.GestureListener;
import com.garethevans.church.opensongtablet.customslides.ImageSlideAdapter;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.customviews.RecyclerLayoutManager;
import com.garethevans.church.opensongtablet.databinding.ModePerformanceBinding;
import com.garethevans.church.opensongtablet.interfaces.ActionInterface;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.pdf.PDFPageAdapter;
import com.garethevans.church.opensongtablet.stage.StageSectionAdapter;
import com.garethevans.church.opensongtablet.stickynotes.StickyPopUp;

public class PerformanceFragment extends Fragment {

    private final String TAG = "PerformanceFragment";
    private StickyPopUp stickyPopUp;
    private MainActivityInterface mainActivityInterface;
    private ActionInterface actionInterface;
    private DisplayInterface displayInterface;
    private int screenWidth, screenHeight, swipeMinimumDistance,
            swipeMaxDistanceYError, swipeMinimumVelocity, availableWidth, availableHeight,
            widthBeforeScale, heightBeforeScale, widthAfterScale, heightAfterScale;
    private float scaleFactor = 1.0f;
    private ModePerformanceBinding myView;
    private Animation animSlideIn, animSlideOut;
    private GestureDetector gestureDetector;
    private PDFPageAdapter pdfPageAdapter;
    private ImageSlideAdapter imageSlideAdapter;
    private StageSectionAdapter stageSectionAdapter;
    private RecyclerLayoutManager recyclerLayoutManager;

    // Attaching and destroying
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        actionInterface = (ActionInterface) context;
        displayInterface = (DisplayInterface) context;
        mainActivityInterface.registerFragment(this,"Performance");
    }

    @Override
    public void onResume() {
        super.onResume();
        displayInterface.checkDisplays();
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
        actionInterface.showSticky(false,true);
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

        // Initialise the recyclerview
        if (recyclerLayoutManager==null) {
            recyclerLayoutManager = new RecyclerLayoutManager(requireContext());
            myView.recyclerView.setLayoutManager(recyclerLayoutManager);
        }
        myView.recyclerView.setItemAnimator(null);

        // Pass view references to the Autoscroll class
        mainActivityInterface.getAutoscroll().initialiseAutoscroll(myView.zoomLayout, myView.recyclerView);

        // Allow the song menu and page buttons
        mainActivityInterface.lockDrawer(false);
        mainActivityInterface.hideActionButton(false);

        // Load in preferences
        loadPreferences();

        // Prepare the song menu (will be called again after indexing from the main activity index songs)
        if (mainActivityInterface.getSongListBuildIndex().getIndexRequired() &&
                !mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
            mainActivityInterface.fullIndex();
        }

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

        removeViews();

        doSongLoad(mainActivityInterface.getPreferences().getMyPreferenceString("whichSongFolder",getString(R.string.mainfoldername)),
                mainActivityInterface.getPreferences().getMyPreferenceString("songfilename","Welcome to OpenSongApp"));

        // Check if we need to show an alert
        if (mainActivityInterface.getAlertChecks().showPlayServicesAlert() ||
        mainActivityInterface.getAlertChecks().showBackup() || mainActivityInterface.getAlertChecks().showUpdateInfo()) {
            AlertInfoBottomSheet alertInfoBottomSheet = new AlertInfoBottomSheet();
            alertInfoBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "AlertInfoBottomSheet");
        }

        return myView.getRoot();
    }

    // Getting the preferences and helpers ready
    private void initialiseHelpers() {
        stickyPopUp = new StickyPopUp(requireContext());
        mainActivityInterface.getPerformanceGestures().setZoomLayout(myView.zoomLayout);
        mainActivityInterface.getPerformanceGestures().setRecyclerView(myView.recyclerView);
        myView.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
    }
    private void loadPreferences() {
        mainActivityInterface.getProcessSong().updateProcessingPreferences();
        mainActivityInterface.getMyThemeColors().getDefaultColors();
        swipeMinimumDistance = mainActivityInterface.getPreferences().getMyPreferenceInt("swipeMinimumDistance", 250);
        swipeMaxDistanceYError = mainActivityInterface.getPreferences().getMyPreferenceInt("swipeMaxDistanceYError", 200);
        swipeMinimumVelocity = mainActivityInterface.getPreferences().getMyPreferenceInt("swipeMinimumVelocity", 600);
        if (mainActivityInterface.getMode().equals("Performance")) {
            myView.mypage.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
            myView.waterMark.setVisibility(View.VISIBLE);
        } else {
            // Stage Mode - sections have correct colour, but the background is different - set to colorPrimary
            myView.mypage.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            myView.waterMark.setVisibility(View.GONE);
        }
        mainActivityInterface.updateOnScreenInfo("setpreferences");
    }

    private void removeViews() {
        mainActivityInterface.getSongSheetTitleLayout().removeAllViews();
        myView.col1.removeAllViews();
        myView.col2.removeAllViews();
        myView.col3.removeAllViews();
        myView.testPane.removeAllViews();
        myView.recyclerView.removeAllViews();
        myView.imageView.setImageDrawable(null);
        mainActivityInterface.setSectionViews(null);
    }

    // This stuff loads the song and prepares the views
    public void doSongLoad(String folder, String filename) {
        // Loading the song is dealt with in this fragment as specific actions are required

        // Stop any autoscroll if required
        mainActivityInterface.getAutoscroll().stopAutoscroll();

        // During the load song call, the song is cleared
        // However ii first extracts the folder and filename we've just set
        mainActivityInterface.getSong().setFolder(folder);
        mainActivityInterface.getSong().setFilename((filename));

        new Thread(() -> {
            // Prepare the slide out and in animations based on swipe direction
            setupSlideOut();
            setupSlideIn();

            // Remove any sticky notes
            actionInterface.showSticky(false,true);

            // Now reset the song object (doesn't change what's already drawn on the screen)
            mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSong(
                    mainActivityInterface.getSong(),false));

            mainActivityInterface.moveToSongInSongMenu();

            // Now slide out the song and after a delay start the next bit of the processing
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
    private void setupSlideOut() {
        // Set up the type of animate in
        if (mainActivityInterface.getDisplayPrevNext().getSwipeDirection().equals("R2L")) {
            animSlideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_left);
        } else {
            animSlideOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);
        }
    }
    private void setupSlideIn() {
        // Set up the type of animate in
        if (mainActivityInterface.getDisplayPrevNext().getSwipeDirection().equals("R2L")) {
            animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        } else {
            animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
        }
    }
    private void prepareSongViews() {
        // This is called on the UI thread above
        // Reset the song views
        mainActivityInterface.setSectionViews(null);

        // Reset the song sheet titles and views
        removeViews();

        // Set the default color
        myView.pageHolder.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());

        int[] screenSizes = mainActivityInterface.getDisplayMetrics();
        screenWidth = screenSizes[0];
        screenHeight = screenSizes[1] - mainActivityInterface.getAppActionBar().getActionBarHeight();
        availableWidth = getResources().getDisplayMetrics().widthPixels;
        availableHeight = getResources().getDisplayMetrics().heightPixels - mainActivityInterface.getMyActionBar().getHeight();
        widthBeforeScale = 0;
        heightBeforeScale = 0;

        // Depending on what we are doing, create the content.
        // Options are
        // - PDF        PDF file. Use the recyclerView (not inside zoomLayout).  All sizes from image representations of pages
        // - IMG        Image file.  Use the imageView (inside pageHolder, inside zoomLayout).  All sizes from the bitmap file.
        // - **Image    Custom image slide (multiple images).  Same as PDF
        // - XML        OpenSong song.  Views need to be drawn and measured. Stage mode uses recyclerView, Performance, the pageHolder

        if (mainActivityInterface.getSong().getFiletype().equals("PDF") &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            preparePDFView();

        } else if (mainActivityInterface.getSong().getFiletype().equals("IMG")) {
            prepareIMGView();

        } else if (mainActivityInterface.getSong().getFolder().contains("**Image")) {
            prepareSlideImageView();

        } else if (mainActivityInterface.getSong().getFiletype().equals("XML") ||
                (mainActivityInterface.getSong().getFiletype().equals("PDF") &&
                        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)) {
            prepareXMLView();
        }

        // Update the toolbar with the song (null)
        mainActivityInterface.updateToolbar(null);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void preparePDFView() {
        // If we can't deal with a PDF (old Android), it will default to the info from the database as XML
        // We populate the recyclerView with the pages of the PDF
        myView.recyclerView.setVisibility(View.INVISIBLE);
        myView.pageHolder.setVisibility(View.GONE);
        myView.songView.setVisibility(View.GONE);
        myView.zoomLayout.setVisibility(View.GONE);

        pdfPageAdapter = new PDFPageAdapter(requireContext(), mainActivityInterface, displayInterface,
                availableWidth, availableHeight);

        myView.recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                heightBeforeScale = pdfPageAdapter.getHeight();
                heightAfterScale = heightBeforeScale;
                recyclerLayoutManager.setSizes(pdfPageAdapter.getHeights(), screenHeight);
                myView.recyclerView.setMaxScrollY(heightAfterScale - screenHeight);

                // Do the slide in
                myView.recyclerView.setVisibility(View.VISIBLE);
                myView.recyclerView.startAnimation(animSlideIn);

                // Get a null screenshot
                getScreenshot(0, 0, 0);

                // Deal with song actions to run after display (highlighter, notes, etc)
                dealWithStuffAfterReady();

                myView.recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        myView.recyclerView.setAdapter(pdfPageAdapter);
    }
    private void prepareIMGView() {
        // We use the imageView inside the pageHolder, inside the zoomLayout
        myView.recyclerView.setVisibility(View.GONE);
        myView.pageHolder.setVisibility(View.INVISIBLE);
        myView.songView.setVisibility(View.GONE);
        myView.zoomLayout.setVisibility(View.VISIBLE);
        myView.imageView.setVisibility(View.VISIBLE);

        // Get a bmp from the image
        Bitmap bmp = mainActivityInterface.getProcessSong().getSongBitmap(mainActivityInterface.getSong().getFolder(),
                mainActivityInterface.getSong().getFilename());

        widthBeforeScale = bmp.getWidth();
        heightBeforeScale = bmp.getHeight();

        myView.zoomLayout.setPageSize(screenWidth, screenHeight);

        if (widthBeforeScale>0 && heightBeforeScale>0) {
            scaleFactor = Math.min((float)screenWidth/(float)widthBeforeScale, (float)screenHeight/(float)heightBeforeScale);
        } else {
            scaleFactor = 1f;
        }

        // Pass this scale factor to the zoom layout
        myView.zoomLayout.setCurrentScale(scaleFactor);

        widthAfterScale = (int) (widthBeforeScale*scaleFactor);
        heightAfterScale= (int) (heightBeforeScale*scaleFactor);

        myView.pageHolder.getLayoutParams().width = widthAfterScale;
        myView.pageHolder.getLayoutParams().height = heightAfterScale;
        myView.imageView.getLayoutParams().width = widthAfterScale;
        myView.imageView.getLayoutParams().height = heightAfterScale;

        RequestOptions requestOptions = new RequestOptions().override(widthAfterScale,heightAfterScale);
        GlideApp.with(requireContext()).load(bmp).apply(requestOptions).into(myView.imageView);

        myView.zoomLayout.setSongSize(widthAfterScale, heightAfterScale + (int)(mainActivityInterface.getSongSheetTitleLayout().getHeight()*scaleFactor));

        // Slide in
        myView.pageHolder.post(() -> {
            myView.pageHolder.setVisibility(View.VISIBLE);
            myView.pageHolder.startAnimation(animSlideIn);
        });

        // Deal with song actions to run after display (highlighter, notes, etc)
        dealWithStuffAfterReady();

    }
    private void prepareSlideImageView() {
        // An image slide.  Use the recyclerView with a new arrayAdapter
        myView.pageHolder.setVisibility(View.GONE);
        myView.songView.setVisibility(View.GONE);
        myView.zoomLayout.setVisibility(View.GONE);
        myView.recyclerView.setVisibility(View.INVISIBLE);
        imageSlideAdapter = new ImageSlideAdapter(requireContext(), mainActivityInterface, displayInterface,
                availableWidth, availableHeight);

        // If we have a time for each slide, set the song duration
        if (mainActivityInterface.getSong().getUser1()!=null && !mainActivityInterface.getSong().getUser1().isEmpty()) {
            int time;
            try {
                time = Integer.parseInt(mainActivityInterface.getSong().getUser1());
            } catch (Exception e) {
                time = 0;
            }
            mainActivityInterface.getSong().setAutoscrolllength(""+(time*imageSlideAdapter.getItemCount()));
            mainActivityInterface.getSong().setAutoscrolldelay("0");
        }

        myView.recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                heightBeforeScale = imageSlideAdapter.getHeight();
                heightAfterScale = heightBeforeScale;
                recyclerLayoutManager.setSizes(imageSlideAdapter.getHeights(),screenHeight);
                myView.recyclerView.setMaxScrollY(heightAfterScale - screenHeight);

                // Slide in
                myView.recyclerView.setVisibility(View.VISIBLE);
                myView.recyclerView.startAnimation(animSlideIn);

                // Deal with song actions to run after display (highlighter, notes, etc)
                dealWithStuffAfterReady();

                // Get a null screenshot
                getScreenshot(0,0,0);

                myView.recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        myView.recyclerView.setAdapter(imageSlideAdapter);

    }
    private void prepareXMLView() {
        // If we are old Android and can't show a pdf, tell the user
        if (mainActivityInterface.getSong().getFiletype().equals("PDF") &&
                android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            mainActivityInterface.getShowToast().doIt(getString(R.string.not_high_enough_api));
        }

        mainActivityInterface.setSectionViews(null);

        // Now prepare the song sections views so we can measure them for scaling using a view tree observer
        mainActivityInterface.setSectionViews(mainActivityInterface.getProcessSong().
                setSongInLayout(mainActivityInterface.getSong(), false, false));

        // Prepare the song sheet header if required, if not, make it null
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("songSheet", false)) {
            mainActivityInterface.setSongSheetTitleLayout(mainActivityInterface.getSongSheetHeaders().getSongSheet(
                    mainActivityInterface.getSong(), mainActivityInterface.getProcessSong().getScaleComments(), false));
        } else {
            mainActivityInterface.setSongSheetTitleLayout(null);
        }

        // We now have the views ready, we need to draw them so we can measure them
        // Start with the song sheeet title/header
        // The other views are dealt with after this call
        setUpHeaderListener();
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
            if (view.getParent()!=null) {
                // Still attached - remove it
                ((ViewGroup)view.getParent()).removeView(view);
            }
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

        // Decide which mode we are in to determine how the views are rendered
        if (mainActivityInterface.getMode().equals("Stage")) {
            // We are in Stage mode so use the recyclerView
            myView.recyclerView.setVisibility(View.INVISIBLE);
            myView.pageHolder.setVisibility(View.GONE);
            myView.songView.setVisibility(View.GONE);
            myView.zoomLayout.setVisibility(View.GONE);
            stageSectionAdapter = new StageSectionAdapter(requireContext(), mainActivityInterface, displayInterface);

            myView.recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    heightBeforeScale = stageSectionAdapter.getHeight();
                    heightAfterScale = heightBeforeScale;

                    Log.d(TAG,"heightBeforeScale="+heightBeforeScale);

                    for (float heights:stageSectionAdapter.getHeights()) {
                        Log.d(TAG,"height: "+heights);
                    }

                    Log.d(TAG,"screenHeight="+screenHeight);

                    recyclerLayoutManager.setSizes(stageSectionAdapter.getHeights(),screenHeight);
                    myView.recyclerView.setMaxScrollY(heightAfterScale - screenHeight);

                    // Slide in
                    myView.recyclerView.setVisibility(View.VISIBLE);
                    myView.recyclerView.startAnimation(animSlideIn);

                    dealWithStuffAfterReady();

                    // Get a null screenshot
                    getScreenshot(0,0,0);

                    myView.recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
            myView.recyclerView.setAdapter(stageSectionAdapter);


        } else {
            // We are in Performance mode, so use the songView
            myView.pageHolder.setVisibility(View.INVISIBLE);
            myView.zoomLayout.setVisibility(View.VISIBLE);
            myView.songView.setVisibility(View.VISIBLE);
            myView.imageView.setVisibility(View.GONE);
            myView.recyclerView.setVisibility(View.GONE);
            myView.zoomLayout.setPageSize(screenWidth, screenHeight);

            scaleFactor = mainActivityInterface.getProcessSong().addViewsToScreen(
                    false, mainActivityInterface.getSectionViews(),
                    mainActivityInterface.getSectionWidths(), mainActivityInterface.getSectionHeights(),
                    myView.pageHolder, myView.songView, myView.songSheetTitle,
                    screenWidth, screenHeight,
                    myView.col1, myView.col2, myView.col3);

            // Pass this scale factor to the zoom layout
            myView.zoomLayout.setCurrentScale(scaleFactor);

            for (int x = 0; x < mainActivityInterface.getSectionViews().size(); x++) {
                widthBeforeScale = Math.max(widthBeforeScale, mainActivityInterface.getSectionWidths().get(x));
                heightBeforeScale += mainActivityInterface.getSectionHeights().get(x);
            }

            widthAfterScale = (int) (widthBeforeScale*scaleFactor);
            heightAfterScale = (int) (heightBeforeScale*scaleFactor);

            myView.pageHolder.getLayoutParams().width = widthAfterScale;
            myView.pageHolder.getLayoutParams().height = heightAfterScale;

            myView.zoomLayout.post(() -> {
                myView.zoomLayout.setSongSize(widthAfterScale, heightAfterScale + (int)(mainActivityInterface.getSongSheetTitleLayout().getHeight()*scaleFactor));

                // Because we might have padded the view at the top for song sheet header scaling:
                int topPadding = 0;
                if (myView.songView.getChildCount()>0 && myView.songView.getChildAt(0)!=null) {
                    topPadding = myView.songView.getChildAt(0).getPaddingTop();
                }

                myView.pageHolder.setVisibility(View.VISIBLE);
                myView.pageHolder.startAnimation(animSlideIn);

                dealWithStuffAfterReady();

                // Try to take a screenshot ready for any highlighter actions that may be called
                int finalTopPadding = topPadding;
                getScreenshot(widthAfterScale,heightAfterScale, finalTopPadding);
            });

        }

    }

    // This stuff deals with running song action stuff
    private void dealWithStuffAfterReady() {
        // Send the autoscroll information (if required)
        mainActivityInterface.getAutoscroll().initialiseSongAutoscroll(heightAfterScale, screenHeight);

        // Now deal with the highlighter file
        dealWithHighlighterFile(widthBeforeScale, heightBeforeScale);

        // Load up the sticky notes if the user wants them
        dealWithStickyNotes(false, false);

        // IV - Consume any later pending client section change received from Host (-ve value)
        if (mainActivityInterface.getSong().getCurrentSection() < 0) {
            mainActivityInterface.getSong().setCurrentSection(-(1 + mainActivityInterface.getSong().getCurrentSection()));
        }

        // Set the previous/next if we want to
        mainActivityInterface.getDisplayPrevNext().setPrevNext();

        // Start the pad (if the pads are activated and the pad is valid)
        mainActivityInterface.getPad().autoStartPad();

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
                getMyPreferenceString("songAutoScale","W").equals("N")) {
            // Set the highlighter image view to match
            myView.highlighterView.setVisibility(View.INVISIBLE);
            // Once the view is ready at the required size, deal with it
            ViewTreeObserver highlighterVTO = myView.highlighterView.getViewTreeObserver();
            highlighterVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Load in the bitmap with these dimensions
                    Bitmap highlighterBitmap = mainActivityInterface.getProcessSong().
                            getHighlighterFile(0, 0);
                    if (highlighterBitmap != null &&
                            mainActivityInterface.getPreferences().getMyPreferenceBoolean("drawingAutoDisplay", true)) {

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
                        int timetohide = mainActivityInterface.getPreferences().getMyPreferenceInt("timeToDisplayHighlighter", 0);
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
    private void getScreenshot(int w, int h, int topPadding) {
        if (!mainActivityInterface.getPreferences().
                getMyPreferenceString("songAutoScale","W").equals("N")
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
    public void toggleHighlighter() {
        if (myView.highlighterView.getVisibility()==View.VISIBLE) {
            myView.highlighterView.setVisibility(View.GONE);
        } else {
            myView.highlighterView.setVisibility(View.VISIBLE);
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
                            getMyPreferenceBoolean("stickyAuto", true)) || forceShow) {
                // This is called from the MainActivity when we clicked on the page button
                stickyPopUp.floatSticky(myView.pageHolder, forceShow);
            } }
    }

    // The scale and gesture bits of the code
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
                mainActivityInterface.updateOnScreenInfo("showhide");
                mainActivityInterface.showHideActionBar();
            }
            return gestureDetector.onTouchEvent(motionEvent);
        });

        myView.recyclerView.setGestureDetector(gestureDetector);
    }

    // Received from MainActivity after a user clicked on a pdf page or a Stage Mode section
    public void performanceShowSection(int position) {
        // Scroll the recyclerView to the top of the page
        myView.recyclerView.smoothScrollToPosition(position);
        mainActivityInterface.getPresenterSettings().setCurrentSection(position);
        displayInterface.updateDisplay("showSection");
    }
    public void pdfScrollToPage(int pageNumber) {
        LinearLayoutManager llm = (LinearLayoutManager) myView.recyclerView.getLayoutManager();
        if (llm!=null) {
            llm.scrollToPosition(pageNumber-1);
        }
    }

}

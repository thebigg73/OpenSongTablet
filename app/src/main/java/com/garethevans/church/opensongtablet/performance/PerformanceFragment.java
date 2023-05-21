package com.garethevans.church.opensongtablet.performance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.abcnotation.ABCPopup;
import com.garethevans.church.opensongtablet.appdata.AlertInfoBottomSheet;
import com.garethevans.church.opensongtablet.controls.GestureListener;
import com.garethevans.church.opensongtablet.customslides.ImageSlideAdapter;
import com.garethevans.church.opensongtablet.customviews.RecyclerLayoutManager;
import com.garethevans.church.opensongtablet.databinding.ModePerformanceBinding;
import com.garethevans.church.opensongtablet.interfaces.ActionInterface;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.pdf.PDFPageAdapter;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.garethevans.church.opensongtablet.stage.StageSectionAdapter;
import com.garethevans.church.opensongtablet.stickynotes.StickyPopUp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PerformanceFragment extends Fragment {
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "PerformanceFragment";
    private StickyPopUp stickyPopUp;
    private ABCPopup abcPopup;
    private MainActivityInterface mainActivityInterface;
    private ActionInterface actionInterface;
    private DisplayInterface displayInterface;
    private int swipeMinimumDistance;
    private int swipeMaxDistanceYError;
    private int swipeMinimumVelocity;
    private int availableWidth;
    private int availableHeight;
    private int widthBeforeScale;
    private int heightBeforeScale;
    private long doSongLoadStartTime;
    private final long doSongLoadQOSTime = 200;
    private int widthAfterScale;
    private int heightAfterScale;
    private boolean processingTestView;
    private boolean songChange;
    private boolean metronomeWasRunning;
    private float scaleFactor = 1.0f;
    private ModePerformanceBinding myView;
    private Animation animSlideIn, animSlideOut;
    private GestureDetector gestureDetector;
    private PDFPageAdapter pdfPageAdapter;
    private ImageSlideAdapter imageSlideAdapter;
    private StageSectionAdapter stageSectionAdapter;
    private RecyclerLayoutManager recyclerLayoutManager;
    private final Handler dealWithExtraStuffOnceSettledHandler = new Handler();
    private final Runnable dealWithExtraStuffOnceSettledRunnable = this::dealWithExtraStuffOnceSettled;

    private String mainfoldername="", mode_performance="", mode_presenter="", mode_stage="",
            not_allowed="", image_string="", nearby_large_file_string="";
    private int sendSongDelay = 0, graceTime = 2000;
    private final Handler sendSongAfterDelayHandler = new Handler(),
        autoHideHighlighterHandler = new Handler();
    private final Runnable sendSongAfterDelayRunnable = () -> {
        // IV - The send is always called by the 'if' and will return true if a large file has been sent
        if (mainActivityInterface.getNearbyConnections().sendSongPayload()) {
            mainActivityInterface.getShowToast().doIt(nearby_large_file_string);
        }
        sendSongDelay = 3000;
    };
    private final Runnable autoHideHighlighterRunnable = new Runnable() {
        @Override
        public void run() {
            if (myView!=null) {
                myView.highlighterView.setVisibility(View.GONE);
            }
        }
    };

    private final Handler resetSendSongAfterDelayHandler = new Handler();
    private final Runnable resetSendSongAfterDelayRunnable = () -> {
        sendSongDelay = 0;
        mainActivityInterface.getNearbyConnections().setSendSongDelayActive(false);
    };

    // Attaching and destroying
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        actionInterface = (ActionInterface) context;
        displayInterface = (DisplayInterface) context;
        mainActivityInterface.registerFragment(this, "Performance");
    }

    @Override
    public void onResume() {
        super.onResume();
        displayInterface.checkDisplays();
        if (getContext()!=null && mainActivityInterface!=null) {
            graceTime = mainActivityInterface.getPreferences().getMyPreferenceBoolean("graceTime",true) ? 2000 : 50;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dealWithStickyNotes(false,true);
        if (mainActivityInterface.getToolbar()!=null) {
            mainActivityInterface.getToolbar().setPerformanceMode(false);
        }
        mainActivityInterface.registerFragment(null,"Performance");
    }
    @Override
    public void onDestroyView() {
        try {
            stickyPopUp.closeSticky();
            actionInterface.showSticky(false, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stickyPopUp.destroyPopup();
        myView = null;
        stickyPopUp = null;
        super.onDestroyView();
    }

    // The logic to start this fragment
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = ModePerformanceBinding.inflate(inflater, container, false);
        prepareStrings();

        // Register this fragment
        mainActivityInterface.registerFragment(this,"Performance");
        mainActivityInterface.updateFragment("updateSongMenuSortTitles",this,null);

        // Initialise the helper classes that do the heavy lifting
        initialiseHelpers();

        // Initialise the recyclerview
        if (recyclerLayoutManager==null && getContext()!=null) {
            recyclerLayoutManager = new RecyclerLayoutManager(getContext());
            myView.recyclerView.setLayoutManager(recyclerLayoutManager);
        }
        myView.recyclerView.setItemAnimator(null);

        // Pass view references to the Autoscroll class
        mainActivityInterface.getAutoscroll().initialiseAutoscroll(myView.zoomLayout, myView.recyclerView);

        // Allow the song menu and page buttons
        mainActivityInterface.lockDrawer(false);
        mainActivityInterface.hideActionButton(false);
        //mainActivityInterface.expandActionButton();

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
        mainActivityInterface.getToolbar().setPerformanceMode(true);
        mainActivityInterface.showActionBar();

        // MainActivity initialisation has firstRun set as true.
        // Check for connected displays now we have loaded preferences, etc
        if (mainActivityInterface.getFirstRun()) {
            displayInterface.checkDisplays();
            mainActivityInterface.setFirstRun(false);
        }

        // Tint the watermark to the text colour
        myView.waterMark.setColorFilter(mainActivityInterface.getMyThemeColors().getLyricsTextColor(),
                android.graphics.PorterDuff.Mode.SRC_IN);

        removeViews();

        if (mainActivityInterface.getWhattodo().equals("pendingLoadSet")) {
            mainActivityInterface.setWhattodo("");
            // Check if the current song is in the set
            int position = mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
            mainActivityInterface.loadSongFromSet(Math.max(position, 0));
        } else {
            doSongLoad(mainActivityInterface.getPreferences().getMyPreferenceString("songFolder", mainfoldername),
                    mainActivityInterface.getPreferences().getMyPreferenceString("songFilename", "Welcome to OpenSongApp"));
        }

        // Check if we need to show an alert
        if (mainActivityInterface.getAlertChecks().showPlayServicesAlert() ||
        mainActivityInterface.getAlertChecks().showBackup() || mainActivityInterface.getAlertChecks().showUpdateInfo()) {
                    AlertInfoBottomSheet alertInfoBottomSheet = new AlertInfoBottomSheet();
                    alertInfoBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "AlertInfoBottomSheet");
        } else {
            // Check for the showcase for new users
            // Set tutorials
            Handler h = new Handler();
            Runnable r = () -> mainActivityInterface.showTutorial("performanceView",null);
            h.postDelayed(r,1000);
        }

        // Pass a reference of the zoom layout to the next/prev so we can stop fling scrolls
        mainActivityInterface.getDisplayPrevNext().setZoomLayout(myView.zoomLayout);

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            mainfoldername = getString(R.string.mainfoldername);
            mode_performance = getString(R.string.mode_performance);
            mode_presenter = getString(R.string.mode_presenter);
            mode_stage = getString(R.string.mode_stage);
            not_allowed = getString(R.string.not_allowed);
            image_string= getString(R.string.image);
            nearby_large_file_string = getString(R.string.nearby_large_file);
        }
    }

    // Getting the preferences and helpers ready
    private void initialiseHelpers() {
        if (getContext() != null) {
            stickyPopUp = new StickyPopUp(getContext());
            abcPopup = new ABCPopup(getContext());
            mainActivityInterface.getPerformanceGestures().setZoomLayout(myView.zoomLayout);
            mainActivityInterface.getPerformanceGestures().setRecyclerView(myView.recyclerView);
            myView.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            mainActivityInterface.getHotZones().initialiseHotZones(myView.hotZoneTopLeft, myView.hotZoneTopCenter, myView.hotZoneBottomCenter);
        }
    }
    private void loadPreferences() {
        mainActivityInterface.getProcessSong().updateProcessingPreferences();
        mainActivityInterface.getMyThemeColors().getDefaultColors();
        swipeMinimumDistance = mainActivityInterface.getPreferences().getMyPreferenceInt("swipeMinimumDistance", 250);
        swipeMaxDistanceYError = mainActivityInterface.getPreferences().getMyPreferenceInt("swipeMaxDistanceYError", 200);
        swipeMinimumVelocity = mainActivityInterface.getPreferences().getMyPreferenceInt("swipeMinimumVelocity", 600);
        if (mainActivityInterface.getMode().equals(mode_performance)) {
            myView.mypage.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
            myView.waterMark.setVisibility(View.VISIBLE);
        } else if (mainActivityInterface.getMode().equals(mode_stage)) {
            // Stage Mode - sections have correct colour, but the background is different - set to background colour with a reduced alpha
            int newColor = mainActivityInterface.getMyThemeColors().adjustAlpha(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor(),0.9f);
            myView.mypage.setBackgroundColor(newColor);
        } else {
            // Presenter mode, just use primary color
            myView.mypage.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            myView.waterMark.setVisibility(View.GONE);
        }
        mainActivityInterface.updateOnScreenInfo("setpreferences");
        if (getContext()!=null) {
            myView.inlineSetList.initialisePreferences(getContext(), mainActivityInterface);
        }
        myView.inlineSetList.prepareSet();

        boolean allowPinchToZoom = mainActivityInterface.getPreferences().getMyPreferenceBoolean("allowPinchToZoom",true);
        myView.zoomLayout.setAllowPinchToZoom(allowPinchToZoom);
        myView.recyclerView.setAllowPinchToZoom(allowPinchToZoom);
        mainActivityInterface.getPresenterSettings().setLogoOn(false);
        displayInterface.updateDisplay("showLogo");
    }

    public void tryToImportIntent() {
        // We may have opened the app at this fragment by clicking on an openable file
        // Get the main activity to check and fix backstack to this as home if required
        mainActivityInterface.dealWithIntent(R.id.performanceFragment);
    }
    private void removeViews() {
        if (myView!=null) {
            try {
                mainActivityInterface.getSongSheetTitleLayout().removeAllViews();
                myView.songView.clearViews();
                myView.testPane.removeAllViews();
                myView.recyclerView.removeAllViews();
                myView.imageView.setImageDrawable(null);
                mainActivityInterface.setSectionViews(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void toggleInlineSet() {
        myView.inlineSetList.toggleInlineSet();
    }
    public void orientationInlineSet(int orientation) {
        myView.inlineSetList.orientationChanged(orientation);
    }
    public void updateInlineSetSet() {
        if (myView!=null) {
            myView.inlineSetList.prepareSet();
        }
    }
    public void updateInlineSetItem(int position) {
        try {
            myView.inlineSetList.updateSelected(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateInlineSetMove(int from, int to) {
        myView.inlineSetList.updateInlineSetMove(from,to);
    }
    public void updateInlineSetRemoved(int from) {
        if (myView!=null) {
            try {
                myView.inlineSetList.updateInlineSetRemoved(from);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void updateInlineSetAdded(SetItemInfo setItemInfo) {
        if (myView!=null) {
            try {
                myView.inlineSetList.updateInlineSetAdded(setItemInfo);
            } catch (Exception e) {
                Log.d(TAG, "Couldn't update inline set - might just not be shown currently");
            }
        }
    }
    public void initialiseInlineSetItem(int position) {
        if (myView!=null) {
            try {
                myView.inlineSetList.initialiseInlineSetItem(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // This stuff loads the song and prepares the views
    public void doSongLoad(String folder, String filename) {
        try {
            doSongLoadStartTime = System.currentTimeMillis();
            mainActivityInterface.closeDrawer(true);

            // Make sure we only do this once (reset at the end of 'dealwithstuffafterready')
            if (!processingTestView) {
                processingTestView = true;
                // Loading the song is dealt with in this fragment as specific actions are required

                // IV - Set a boolean indicating song change
                songChange = !mainActivityInterface.getSong().getFilename().equals(filename) || !mainActivityInterface.getSong().getFolder().equals(folder);

                // Remove capo
                mainActivityInterface.updateOnScreenInfo("capoHide");

                // IV - Deal with stop of metronome
                metronomeWasRunning = mainActivityInterface.getMetronome().getIsRunning();
                if (songChange && metronomeWasRunning) {
                    mainActivityInterface.getMetronome().stopMetronome();
                }

                // Stop any autoscroll if required
                boolean autoScrollActivated = mainActivityInterface.getAutoscroll().getAutoscrollActivated();
                mainActivityInterface.getAutoscroll().stopAutoscroll();
                mainActivityInterface.getAutoscroll().setAutoscrollActivated(autoScrollActivated);

                // Stop the highlighter autohide if required
                autoHideHighlighterHandler.removeCallbacks(autoHideHighlighterRunnable);

                // During the load song call, the song is cleared
                // However it first extracts the folder and filename we've just set
                mainActivityInterface.getSong().setFolder(folder);
                mainActivityInterface.getSong().setFilename(filename);

                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                            Handler handler = new Handler(Looper.getMainLooper());

                            // Prepare the slide out and in animations based on swipe direction
                            setupSlideOut();
                            setupSlideIn();

                            // Remove any sticky notes
                            actionInterface.showSticky(false, true);

                            // Now reset the song object (doesn't change what's already drawn on the screen)
                            mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSong(
                                    mainActivityInterface.getSong(), false));

                            handler.post(() -> {
                                if (myView != null) {
                                    // Set the default color
                                    myView.pageHolder.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
                                    // Update the toolbar with the song detail (null).
                                    mainActivityInterface.updateToolbar(null);
                                }
                            });

                            // IV - Reset current values to 0
                            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                                mainActivityInterface.getSong().setPdfPageCurrent(0);
                            } else {
                                mainActivityInterface.getSong().setCurrentSection(0);
                                mainActivityInterface.getPresenterSettings().setCurrentSection(0);
                            }

                            if (mainActivityInterface.getNearbyConnections().hasValidConnections() &&
                                mainActivityInterface.getNearbyConnections().getIsHost()) {
                                // Only the first (with no delay) and last (with delay) of a long sequence of song changes is actually sent
                                // sendSongDelay will be 0 for the first song
                                // IV - Always empty then add to queue (known state)
                                mainActivityInterface.getNearbyConnections().setSendSongDelayActive(sendSongDelay != 0);
                                sendSongAfterDelayHandler.removeCallbacks(sendSongAfterDelayRunnable);
                                sendSongAfterDelayHandler.postDelayed(sendSongAfterDelayRunnable, sendSongDelay);
                                // IV - Always empty then add to queue (known state)
                                resetSendSongAfterDelayHandler.removeCallbacks(resetSendSongAfterDelayRunnable);
                                resetSendSongAfterDelayHandler.postDelayed(resetSendSongAfterDelayRunnable, 3500);
                            }

                            mainActivityInterface.moveToSongInSongMenu();

                            // Now slide out the song and after a delay start the next bit of the processing
                            if (myView != null) {
                                myView.recyclerView.post(() -> {
                                    try {
                                        if (myView.recyclerView.getVisibility() == View.VISIBLE) {
                                            myView.recyclerView.startAnimation(animSlideOut);
                                        }
                                    } catch (Exception e) {
                                        // Continue
                                    }
                                });
                                myView.pageHolder.post(() -> {
                                    try {
                                        if (myView.pageHolder.getVisibility() == View.VISIBLE) {
                                            myView.pageHolder.startAnimation(animSlideOut);
                                        }
                                    } catch (Exception e) {
                                        // Continue
                                    }
                                });
                            }
                            if (getContext()!=null) {
                                handler.postDelayed(this::prepareSongViews, 50 + getContext().getResources().getInteger(R.integer.slide_out_time));
                            }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (myView!=null) {
            // This is called on the UI thread above via the handler from mainLooper()
            // Reset the song views
            mainActivityInterface.setSectionViews(null);
            removeViews();

            // If we are in a set, send that info to the inline set custom view to see if it should draw
            myView.inlineSetList.checkVisibility();

            int[] screenSizes = mainActivityInterface.getDisplayMetrics();
            int screenWidth = screenSizes[0];
            int screenHeight = screenSizes[1];

            int[] viewPadding = mainActivityInterface.getViewMargins();

            availableWidth = screenWidth - viewPadding[0] - viewPadding[1] - myView.inlineSetList.getInlineSetWidth();
            availableHeight = screenHeight - viewPadding[2] - viewPadding[3];

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
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void preparePDFView() {
        // If we can't deal with a PDF (old Android), it will default to the info from the database as XML
        // We populate the recyclerView with the pages of the PDF
        myView.recyclerView.setVisibility(View.INVISIBLE);
        myView.pageHolder.setVisibility(View.GONE);
        myView.songView.setVisibility(View.GONE);
        myView.zoomLayout.setVisibility(View.GONE);

        if (getContext()!=null) {
            pdfPageAdapter = new PDFPageAdapter(getContext(), mainActivityInterface, displayInterface,
                    availableWidth, availableHeight, myView.inlineSetList.getInlineSetWidth());
        }
        myView.recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                myView.recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                heightBeforeScale = pdfPageAdapter.getHeight();
                heightAfterScale = heightBeforeScale;
                recyclerLayoutManager.setSizes(pdfPageAdapter.getHeights(), availableHeight);
                myView.recyclerView.setPadding(myView.inlineSetList.getInlineSetWidth(),0,0,0);
                myView.recyclerView.setMaxScrollY(heightAfterScale - availableHeight);
                //IV - Reset zoom
                myView.recyclerView.toggleScale();

                // Do the slide in
                myView.recyclerView.setVisibility(View.VISIBLE);
                myView.recyclerView.startAnimation(animSlideIn);

                // Get a null screenshot
                getScreenshot(0, 0, 0);

                // Deal with song actions to run after display (highlighter, notes, etc)
                dealWithStuffAfterReady(true);
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

        if (bmp!=null) {
            widthBeforeScale = bmp.getWidth();
            heightBeforeScale = bmp.getHeight();
        } else {
            widthBeforeScale = 1;
            heightBeforeScale = 1;
        }

        myView.zoomLayout.setPageSize(availableWidth, availableHeight);
        //IV - Reset zoom
        myView.zoomLayout.resetLayout();
        myView.zoomLayout.toggleScale();

        if (widthBeforeScale>0 && heightBeforeScale>0) {
            String scaleMethod = mainActivityInterface.getPreferences().getMyPreferenceString("songAutoScale", "W");
            switch (scaleMethod) {
                case "W":
                    scaleFactor = (float) availableWidth / (float) widthBeforeScale;
                    break;
                case "Y":
                    scaleFactor = Math.min((float) availableWidth / (float) widthBeforeScale, (float) availableHeight / (float) heightBeforeScale);
                    break;
                default:
                    scaleFactor = 1f;
            }
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
        if (getContext()!=null) {
            Glide.with(getContext()).load(bmp).apply(requestOptions).into(myView.imageView);
        }
        myView.zoomLayout.setSongSize(widthAfterScale, heightAfterScale + (int)(mainActivityInterface.getSongSheetTitleLayout().getHeight()*scaleFactor));

        // Slide in
        myView.pageHolder.post(() -> {
            myView.pageHolder.setVisibility(View.VISIBLE);
            myView.pageHolder.startAnimation(animSlideIn);
        });

        // Deal with song actions to run after display (highlighter, notes, etc)
        dealWithStuffAfterReady(true);

    }
    private void prepareSlideImageView() {
        // An image slide.  Use the recyclerView with a new arrayAdapter
        myView.pageHolder.setVisibility(View.GONE);
        myView.songView.setVisibility(View.GONE);
        myView.zoomLayout.setVisibility(View.GONE);
        myView.recyclerView.setVisibility(View.INVISIBLE);
        if (getContext()!=null) {
            imageSlideAdapter = new ImageSlideAdapter(getContext(), mainActivityInterface, displayInterface,
                    availableWidth, availableHeight);
        }

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
                myView.recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                heightBeforeScale = imageSlideAdapter.getHeight();
                heightAfterScale = heightBeforeScale;
                recyclerLayoutManager.setSizes(imageSlideAdapter.getHeights(),availableHeight);
                myView.recyclerView.setMaxScrollY(heightAfterScale - availableHeight);
                //IV - Reset zoom
                myView.recyclerView.toggleScale();

                // Slide in
                myView.recyclerView.setVisibility(View.VISIBLE);
                myView.recyclerView.startAnimation(animSlideIn);

                // Deal with song actions to run after display (highlighter, notes, etc)
                dealWithStuffAfterReady(true);

                // Get a null screenshot
                getScreenshot(0,0,0);

            }
        });
        myView.recyclerView.setAdapter(imageSlideAdapter);

    }
    private void prepareXMLView() {
        // If we are old Android and can't show a pdf, tell the user
        if (mainActivityInterface.getSong().getFiletype().equals("PDF") &&
                android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            mainActivityInterface.getShowToast().doIt(not_allowed);
        }

        mainActivityInterface.setSectionViews(null);

        // Now prepare the song sections views so we can measure them for scaling using a view tree observer
        mainActivityInterface.setSectionViews(mainActivityInterface.getProcessSong().
                setSongInLayout(mainActivityInterface.getSong(), false, false));

        // Prepare the song sheet header if required, if not, make it null
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("songSheet", false)) {
            mainActivityInterface.setSongSheetTitleLayout(mainActivityInterface.getSongSheetHeaders().getSongSheet(
                    mainActivityInterface.getSong(), mainActivityInterface.getProcessSong().getScaleComments(), mainActivityInterface.getMyThemeColors().getLyricsTextColor()));
        } else {
            mainActivityInterface.setSongSheetTitleLayout(null);
        }
        // We now have the views ready, we need to draw them so we can measure them
        // Start with the song sheet title/header
        // The other views are dealt with after this call

        // Run this as a post to the root view - otherwise views aren't necessarily fully ready for drawing
        myView.getRoot().post(this::setUpHeaderListener);
    }

    private void setUpHeaderListener() {
        try {
            // If we want headers, the header layout isn't null, so we can draw and listen
            // Add the view and wait for the vto return
            if (mainActivityInterface.getSongSheetTitleLayout() != null &&
                    !mainActivityInterface.getMode().equals(mode_presenter)) {
                // Check the header isn't already attached to a view
                if (mainActivityInterface.getSongSheetTitleLayout().getParent()!=null) {
                    ((ViewGroup) mainActivityInterface.getSongSheetTitleLayout().getParent()).removeAllViews();
                }
                myView.testPaneHeader.removeAllViews();

                mainActivityInterface.getSongSheetTitleLayout().post(() -> {
                    if (myView!=null) {
                        try {
                            setUpTestViewListener();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                try {
                    myView.testPaneHeader.addView(mainActivityInterface.getSongSheetTitleLayout());
                } catch (Exception e) {
                    e.printStackTrace();
                    setUpTestViewListener();
                }

            } else {
                // No song sheet title requested, so skip
                setUpTestViewListener();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Error, so skip
            setUpTestViewListener();
        }
    }

    private void setUpTestViewListener() {
        if (myView!=null) {
            // Add the views and wait for the vto of each to finish
            myView.songView.clearViews();
            myView.testPane.removeAllViews();

            // We will only proceed once all of the views show true as being drawn
            boolean[] viewsDrawn = new boolean[mainActivityInterface.getSectionViews().size()];

            for (int v = 0; v < mainActivityInterface.getSectionViews().size(); v++) {
                final int viewNum = v;
                final View view = mainActivityInterface.getSectionViews().get(viewNum);

                // If views are attached to a parent, remove it from the parent
                if (view.getParent() != null) {
                    // Still attached - remove it
                    ((ViewGroup) view.getParent()).removeView(view);
                }

                // Set a post listener for the view
                view.post(() -> {
                    viewsDrawn[viewNum] = true;
                    // Check if the array is only true
                    boolean isReady = true;
                    for (boolean thisBoolean : viewsDrawn) {
                        if (!thisBoolean) {
                            // Not ready
                            isReady = false;
                            break;
                        }
                    }
                    if (isReady) {
                        songIsReadyToDisplay();
                    }
                });

                // Add the view.  The post above gets called once drawn
                myView.testPane.addView(view);
            }
        }
    }

    private void songIsReadyToDisplay() {
        try {
            // Set the page holder to fullscreen for now
            myView.pageHolder.getLayoutParams().width = availableWidth;
            myView.pageHolder.getLayoutParams().height = availableHeight;
            myView.songSheetTitle.setVisibility(View.VISIBLE);

            // All views have now been drawn, so measure the arraylist views
            for (int x = 0; x < mainActivityInterface.getSectionViews().size(); x++) {
                int width = mainActivityInterface.getSectionViews().get(x).getWidth();
                int height = mainActivityInterface.getSectionViews().get(x).getHeight();
                mainActivityInterface.addSectionSize(x, width, height);
            }

            myView.testPane.removeAllViews();

            // Decide which mode we are in to determine how the views are rendered
            if (mainActivityInterface.getMode().equals(mode_stage)) {
                // We are in Stage mode so use the recyclerView
                myView.recyclerView.setVisibility(View.INVISIBLE);
                myView.pageHolder.setVisibility(View.GONE);
                myView.songView.setVisibility(View.GONE);
                myView.zoomLayout.setVisibility(View.GONE);
                myView.highlighterView.setVisibility(View.GONE);
                if (getContext()!=null) {
                    stageSectionAdapter = new StageSectionAdapter(getContext(), mainActivityInterface,
                            displayInterface, myView.inlineSetList.getInlineSetWidth());
                }

                myView.recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        myView.recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        heightBeforeScale = stageSectionAdapter.getHeight();
                        heightAfterScale = heightBeforeScale;

                        recyclerLayoutManager.setSizes(stageSectionAdapter.getHeights(), availableHeight);
                        myView.recyclerView.setHasFixedSize(false);
                        myView.recyclerView.setMaxScrollY(heightAfterScale - availableHeight);
                        myView.recyclerView.setPadding(myView.inlineSetList.getInlineSetWidth(),0,0,0);

                        endProcessing();

                        // Slide in
                        long QOSAdjustment = doSongLoadQOSTime - (System.currentTimeMillis() - doSongLoadStartTime);
                        //Log.d(TAG, "Song QOS adjustment: " + Math.max(0, QOSAdjustment) + " (" + (doSongLoadQOSTime - QOSAdjustment) + ")");

                        myView.recyclerView.setVisibility(View.VISIBLE);
                        //IV - Reset zoom
                        myView.recyclerView.toggleScale();

                        new Handler().postDelayed(() -> {
                            myView.recyclerView.startAnimation(animSlideIn);

                            dealWithStuffAfterReady(false);

                            // Get a null screenshot
                            getScreenshot(0, 0, 0);
                        }, Math.max(0, QOSAdjustment));
                    }
                });
                myView.recyclerView.post(() -> myView.recyclerView.setAdapter(stageSectionAdapter));


            } else {
                // We are in Performance mode, so use the songView
                myView.pageHolder.setVisibility(View.INVISIBLE);
                myView.zoomLayout.setVisibility(View.VISIBLE);
                myView.songView.setVisibility(View.VISIBLE);
                myView.imageView.setVisibility(View.GONE);
                myView.recyclerView.setVisibility(View.GONE);
                myView.highlighterView.setVisibility(View.GONE);

                myView.zoomLayout.setPageSize(availableWidth, availableHeight);
                //IV - Reset zoom
                myView.zoomLayout.resetLayout();
                myView.zoomLayout.toggleScale();

                myView.pageHolder.getLayoutParams().width = availableWidth;
                myView.pageHolder.getLayoutParams().height = availableHeight;

                //Log.d(TAG,"availableWidth:"+availableWidth);
                float[] scaleInfo = mainActivityInterface.getProcessSong().addViewsToScreen(
                        mainActivityInterface.getSectionViews(),
                        mainActivityInterface.getSectionWidths(), mainActivityInterface.getSectionHeights(),
                        myView.pageHolder, myView.songView, myView.songSheetTitle,
                        availableWidth, availableHeight, myView.songView.getCol1(), myView.songView.getCol2(),
                        myView.songView.getCol3(), false, getResources().getDisplayMetrics());

                // Determine how many columns are scaled
                heightAfterScale = 0;
                if (scaleInfo[0]==1) {
                    /*float[] {1,           // Number of columns
                    1    oneColumnScale,    // Overall best scale
                    2    col1_1Width,       // Column 1 max width
                    3    col1_1Height,      // Column 1 total height
                    4    sectionSpace}      // Section space per view except last column */
                    scaleFactor = scaleInfo[1];
                    widthBeforeScale = (int)scaleInfo[2];
                    heightBeforeScale = (int)scaleInfo[3];
                    widthAfterScale = (int)(widthBeforeScale * scaleFactor);
                    heightAfterScale = (int)(heightBeforeScale * scaleFactor);
                    myView.pageHolder.getLayoutParams().width = widthAfterScale;
                } else if (scaleInfo[0]==2) {
                    /*float[]{2,             // Number of columns
                    1    twoColumnScale,     // Overall best scale
                    2    columnBreak2,       // Break point
                    3    col1_2ScaleBest,    // Best col 1 scale
                    4    col1_2Width,        // Column 1 max width
                    5    col1_2Height,       // Column 1 total height
                    6    col2_2ScaleBest,    // Best col 2 scale
                    7    col2_2Width,        // Column 2 max width
                    8    col2_2Height,       // Column 2 total height
                    9    sectionSpace}       // Section space per view except last column */
                    scaleFactor = Math.max(scaleInfo[3],scaleInfo[6]);
                    widthBeforeScale = availableWidth;
                    heightBeforeScale = (int)Math.max(scaleInfo[5],scaleInfo[8]);
                    widthAfterScale = availableWidth;
                    heightAfterScale = (int)Math.max(scaleInfo[3]*scaleInfo[5],scaleInfo[6]*scaleInfo[8]);
                    myView.pageHolder.getLayoutParams().width = availableWidth;
                    myView.songView.getLayoutParams().width = availableWidth;
                } else if (scaleInfo[0]==3) {
                    /*float[]{3,             // Number of columns
                    1    threeColumnScale,   // Overall best scale
                    2    columnBreak3_a,     // Break point 1
                    3    columnBreak3_b,     // Break point 2
                    4    col1_3ScaleBest,    // Best col 1 scale
                    5    col1_3Width,        // Column 1 max width
                    6    col1_3Height,       // Column 1 total height
                    7    col2_3ScaleBest,    // Best col 2 scale
                    8    col2_3Width,        // Column 2 max width
                    9    col2_3Height,       // Column 2 total height
                    10   col3_3ScaleBest,    // Best col 3 scale
                    11   col3_3Width,        // Column 3 max width
                    12   col3_3Height,       // Column 3 total height
                    13   sectionSpace};      // Section space per view except last in column */
                    widthBeforeScale = availableWidth;
                    heightBeforeScale = (int)Math.max(scaleInfo[6],Math.max(scaleInfo[9],scaleInfo[12]));
                    widthAfterScale = availableWidth;
                    scaleFactor = Math.max(scaleInfo[4],Math.max(scaleInfo[7],scaleInfo[10]));
                    heightAfterScale = (int)Math.max(scaleInfo[4]*scaleInfo[6],Math.max(scaleInfo[7]*scaleInfo[9],scaleInfo[10]*scaleInfo[12]));
                    myView.pageHolder.getLayoutParams().width = availableWidth;
                    myView.songView.getLayoutParams().width = availableWidth;
                }

                heightAfterScale = heightAfterScale + mainActivityInterface.getSongSheetTitleLayout().getHeight();
                myView.pageHolder.getLayoutParams().height = heightAfterScale;
                myView.songView.getLayoutParams().height = heightAfterScale;

                // Pass this scale factor to the zoom layout as the new minimum scale
                myView.zoomLayout.setCurrentScale(scaleFactor);
                myView.zoomLayout.setSongSize(widthAfterScale,heightAfterScale);

                endProcessing();

                // Slide in
                long QOSAdjustment = doSongLoadQOSTime - (System.currentTimeMillis() - doSongLoadStartTime);
                //Log.d(TAG, "Song QOS adjustment: " + Math.max(0, QOSAdjustment) + " (" + (doSongLoadQOSTime - QOSAdjustment) + ")");

                myView.zoomLayout.postDelayed(() -> {
                    try {
                        // The new song sizes were sent to the zoomLayout in ProcessSong
                        int topPadding = 0;

                        // IV - We need to request layout to get songsheet information added ahead of animate in. Odd!
                        myView.pageHolder.requestLayout();
                        myView.pageHolder.setVisibility(View.VISIBLE);
                        myView.pageHolder.startAnimation(animSlideIn);

                        dealWithStuffAfterReady(false);

                        // Try to take a screenshot ready for any highlighter actions that may be called
                        getScreenshot(myView.pageHolder.getWidth(), myView.pageHolder.getHeight()-myView.songSheetTitle.getHeight(), topPadding);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, Math.max(0, QOSAdjustment));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void endProcessing () {
        // Set the load status to the song (used to enable nearby section change  listener)
        mainActivityInterface.getSong().setCurrentlyLoading(false);

        // Set the previous/next
        mainActivityInterface.getDisplayPrevNext().setPrevNext();

        // Release the processing lock
        processingTestView = false;
    }

    // This stuff deals with running song actions
    private void dealWithStuffAfterReady(boolean callEndProcessing) {

        if (callEndProcessing) {
            endProcessing();
        }

        // IV - Consume any later pending client section change received from Host (-ve value)
        if (mainActivityInterface.getNearbyConnections().hasValidConnections() &&
                !mainActivityInterface.getNearbyConnections().getIsHost()) {
            int hostPendingSection = mainActivityInterface.getNearbyConnections().getHostPendingSection();
            if (hostPendingSection != 0) {
                mainActivityInterface.getNearbyConnections().doSectionChange(hostPendingSection);
            }
            mainActivityInterface.getNearbyConnections().resetHostPendingSection();
        }

        // Now deal with the highlighter file
        if (mainActivityInterface.getMode().equals(mode_performance)) {
            dealWithHighlighterFile(widthAfterScale, heightAfterScale);
        }

        // Load up the sticky notes if the user wants them
        dealWithStickyNotes(false, false);

        // Run this only when the user has stopped on a song after 2s.
        // This is important for pad use - the pad will not change while the user rapidly changes songs.
        // This is important for rapid song changes - we only run autoscroll, metronome etc. for the last song.
        // For pads, once settled on a song the user has 2s grace to prep to play the song before cross fade.
        // A good time to change capo!
        dealWithExtraStuffOnceSettledHandler.removeCallbacks((dealWithExtraStuffOnceSettledRunnable));
        dealWithExtraStuffOnceSettledHandler.postDelayed(dealWithExtraStuffOnceSettledRunnable, graceTime);
    }

    private void dealWithExtraStuffOnceSettled() {
        if (getContext()!=null && myView!=null) {
            mainActivityInterface.getHotZones().checkScrollButtonOn(myView.zoomLayout,myView.recyclerView);
            // Send the autoscroll information (if required)
            mainActivityInterface.getAutoscroll().initialiseSongAutoscroll(heightAfterScale, availableHeight);
            if (mainActivityInterface.getAutoscroll().getShouldAutostart()) {
                mainActivityInterface.getAutoscroll().startAutoscroll();
            }

            // Deal with auto start of metronome
            if (mainActivityInterface.getMetronome().getMetronomeAutoStart() &&
                    songChange && metronomeWasRunning) {
                mainActivityInterface.getMetronome().startMetronome();
            }

            // Deal with capo information (if required)
            mainActivityInterface.updateOnScreenInfo("capoShow");
            mainActivityInterface.dealWithCapo();

            // Start the pad (if the pads are activated and the pad is valid)
            mainActivityInterface.getPad().autoStartPad();

            // Update any midi commands (if any)
            if (mainActivityInterface.getMidi().getMidiSendAuto()) {

                int delay = 0;
                // Send BeatBuddy autosong if required
                if (mainActivityInterface.getBeatBuddy().getBeatBuddyAutoLookup()) {
                    delay = mainActivityInterface.getBeatBuddy().tryAutoSend(getContext(),mainActivityInterface,mainActivityInterface.getSong());
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // These are addition to beatbuddy, so sent afterwards
                    mainActivityInterface.getMidi().buildSongMidiMessages();
                    mainActivityInterface.getMidi().sendSongMessages();
                }, delay);
            }


            // Update the secondary display (if present)
            displayInterface.updateDisplay("newSongLoaded");
            displayInterface.updateDisplay("setSongInfo");
            if (mainActivityInterface.getMode().equals(mode_stage) &&
                mainActivityInterface.getSong().getFiletype().equals("XML") &&
                !mainActivityInterface.getSong().getFolder().contains("**Image") &&
                    !mainActivityInterface.getSong().getFolder().contains("**"+image_string)) {
                displayInterface.updateDisplay("setSongContent");
            } else if (!mainActivityInterface.getSong().getFiletype().equals("XML") ||
                mainActivityInterface.getSong().getFolder().contains("**Image")) {
                mainActivityInterface.getPresenterSettings().setCurrentSection(0);
                displayInterface.updateDisplay("showSection");
            } else {
                displayInterface.updateDisplay("setSongContent");
            }

            // Update the view log usage
            mainActivityInterface.getStorageAccess().updateFileUsageLog(mainActivityInterface.getSong());

            // If we opened the app with and intent/file, check if we need to import
            tryToImportIntent();

            mainActivityInterface.updateOnScreenInfo("showhide");
        }
    }
    public void dealWithAbc(boolean forceShow, boolean hide) {
        if (hide) {
            if (abcPopup!=null) {
                abcPopup.closeScore();
            }
        } else {
            if ((mainActivityInterface != null && mainActivityInterface.getSong() != null &&
                    mainActivityInterface.getSong().getAbc() != null &&
                    !mainActivityInterface.getSong().getAbc().isEmpty() &&
                    mainActivityInterface.getPreferences().
                            getMyPreferenceBoolean("abcAuto", false)) || forceShow) {
                // This is called from the MainActivity when we clicked on the page button
                abcPopup.floatABC(myView.pageHolder, forceShow);
            }
        }
    }
    private void dealWithHighlighterFile(int w, int h) {
        try {
            if (getContext()!=null) {
                String highlighterFilename = mainActivityInterface.getProcessSong().
                        getHighlighterFilename(mainActivityInterface.getSong(),
                                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT,-1);
                Uri highlighterUri = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter","",highlighterFilename);

                if (myView!=null && mainActivityInterface.getStorageAccess().uriExists(highlighterUri)) {
                    // Set the highlighter image view to match
                    myView.highlighterView.setVisibility(View.INVISIBLE);
                    // Once the view is ready at the required size, deal with it
                    ViewTreeObserver highlighterVTO = myView.highlighterView.getViewTreeObserver();
                    highlighterVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            try {
                                if (getContext() != null) {
                                    myView.highlighterView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    // Load in the bitmap with these dimensions
                                    // v5 used portrait and landscape views.  However, now if we only have one
                                    // column, we will always load the portrait view
                                    // landscape is now for columns
                                    Bitmap highlighterBitmap = mainActivityInterface.getProcessSong().
                                            getHighlighterFile(0, 0);

                                    if (highlighterBitmap != null &&
                                            mainActivityInterface.getPreferences().getMyPreferenceBoolean("drawingAutoDisplay", true)) {

                                        myView.highlighterView.setVisibility(View.VISIBLE);
                                        ViewGroup.LayoutParams rlp = myView.highlighterView.getLayoutParams();
                                        rlp.width = (int) ((float) w * scaleFactor);
                                        rlp.height = (int) ((float) h * scaleFactor);

                                        myView.highlighterView.setLayoutParams(rlp);
                                        RequestOptions requestOptions = new RequestOptions().centerInside().override(rlp.width, rlp.height);
                                        Glide.with(getContext()).load(highlighterBitmap).
                                                apply(requestOptions).
                                                into(myView.highlighterView);

                                        myView.highlighterView.setPivotX(0f);
                                        myView.highlighterView.setPivotY(0f);

                                        // Hide after a certain length of time
                                        int timetohide = mainActivityInterface.getPreferences().getMyPreferenceInt("timeToDisplayHighlighter", 0);
                                        if (timetohide != 0) {
                                            autoHideHighlighterHandler.postDelayed(
                                                    autoHideHighlighterRunnable, timetohide * 1000L);
                                        }
                                    } else {
                                        myView.highlighterView.post(() -> {
                                            if (myView != null) {
                                                try {
                                                    myView.highlighterView.setVisibility(View.GONE);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    myView.highlighterView.post(() -> {
                        try {
                            myView.highlighterView.requestLayout();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else if (myView!=null) {
                    myView.highlighterView.post(() -> {
                        try {
                            myView.highlighterView.setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        if (mainActivityInterface.getMode().equals(mode_performance)) {
            if (myView.highlighterView.getVisibility() == View.VISIBLE) {
                myView.highlighterView.setVisibility(View.GONE);
            } else {
                myView.highlighterView.setVisibility(View.VISIBLE);
            }
        }
    }
    public void dealWithStickyNotes(boolean forceShow, boolean hide) {
        if (hide) {
            if (stickyPopUp!=null) {
                stickyPopUp.closeSticky();
            }
            if (abcPopup!=null) {
                abcPopup.closeScore();
            }
        } else {
            if ((mainActivityInterface != null && mainActivityInterface.getSong() != null &&
                    mainActivityInterface.getSong().getNotes() != null &&
                    !mainActivityInterface.getSong().getNotes().isEmpty() &&
                    mainActivityInterface.getPreferences().
                            getMyPreferenceBoolean("stickyAuto", true)) || forceShow) {
                // This is called from the MainActivity when we clicked on the page button
                if (myView!=null) {
                    stickyPopUp.floatSticky(myView.pageHolder, forceShow);
                }
            }
        }
    }

    // The scale and gesture bits of the code
    @SuppressLint("ClickableViewAccessibility")
    private void setGestureListeners(){
        // get the gesture detector
        if (getContext()!=null) {
            gestureDetector = new GestureDetector(getContext(), new GestureListener(mainActivityInterface,
                    swipeMinimumDistance, swipeMaxDistanceYError, swipeMinimumVelocity));

            // Any interaction with the screen should trigger the display prev/next (if required)
            // It should also show the action bar
            myView.zoomLayout.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mainActivityInterface.getDisplayPrevNext().showAndHide();
                    mainActivityInterface.updateOnScreenInfo("showhide");
                    mainActivityInterface.showActionBar();
                    mainActivityInterface.getHotZones().checkScrollButtonOn(myView.zoomLayout,myView.recyclerView);
                    // Check for updating send nearby to
                    if (mainActivityInterface.getNearbyConnections().hasValidConnections() &&
                            mainActivityInterface.getNearbyConnections().getIsHost() &&
                            !mainActivityInterface.getPreferences().getMyPreferenceString("songAutoScale", "W").equals("Y")) {
                        // Get the scroll height
                        int height = myView.zoomLayout.getMaxScrollY();
                        // Get the scroll position
                        int scrollPos = myView.zoomLayout.getScrollY();
                        if (height > 0) {
                            mainActivityInterface.getNearbyConnections().sendScrollToPayload((float) scrollPos / (float) height);
                        }
                    }
                }
                return gestureDetector.onTouchEvent(motionEvent);
            });

            myView.recyclerView.setGestureDetector(gestureDetector);
        }
    }

    public void toggleScale() {
        // IV - View may no longer be valid
        if (myView!=null && myView.recyclerView.getVisibility() == View.VISIBLE) {
            // Resets the zoom
            myView.recyclerView.toggleScale();
        } else {
            if (myView != null) {
                // Toggles between different zooms
                myView.zoomLayout.toggleScale();
            }
        }
    }

    public void doNearbyScrollBy(float proportionScroll) {
        // We received from nearby host, so attempt to scroll by this proportion in the zoomLayout
        if (myView.zoomLayout.getVisibility()==View.VISIBLE &&
                !mainActivityInterface.getPreferences().getMyPreferenceString("songAutoScale","W").equals("Y")) {
            myView.zoomLayout.animateScrollBy(mainActivityInterface,
                    Math.abs(proportionScroll),proportionScroll>0);
        } else if (myView.recyclerView.getVisibility()==View.VISIBLE) {
            int height = (int)(proportionScroll*myView.recyclerView.getHeight());
            myView.recyclerView.smoothScrollBy(0,height);
        }
    }

    public void doNearbyScrollTo(float proportionScroll) {
        // We received from nearby host, so attempt to scroll this position (as ratio of height)
        if (myView.zoomLayout.getVisibility()==View.VISIBLE &&
                !mainActivityInterface.getPreferences().getMyPreferenceString("songAutoScale","W").equals("Y")) {
            myView.zoomLayout.doScrollTo(0,(int)(myView.zoomLayout.getMaxScrollY()*proportionScroll));
        } else if (myView.recyclerView.getVisibility()==View.VISIBLE) {
            int height = (int)(proportionScroll*myView.recyclerView.getHeight());
            myView.recyclerView.scrollTo(0,height);
        }
    }

    public void updateSizes(int width, int height) {
        myView.zoomLayout.setSongSize(width,height);
    }

    // Received from MainActivity after a user clicked on a pdf page or a Stage Mode section
    public void performanceShowSection(int position) {
        // Scroll the recyclerView to the position as long as we aren't in an autoscroll
        if (!mainActivityInterface.getAutoscroll().getIsAutoscrolling()) {
            //myView.recyclerView.smoothScrollBy(0,500);

            // IV - Use a snap to top scroller if scrolling to the top of the screen
            if (mainActivityInterface.getPreferences().getMyPreferenceFloat("stageModeScale",0.8f) == 1.0f) {
                myView.recyclerView.smoothScrollTo(getContext(),recyclerLayoutManager, position);
            } else {
                myView.recyclerView.doSmoothScrollTo(recyclerLayoutManager, position);
            }
        }
        mainActivityInterface.getPresenterSettings().setCurrentSection(position);
        displayInterface.updateDisplay("showSection");
        mainActivityInterface.getHotZones().checkScrollButtonOn(myView.zoomLayout,myView.recyclerView);
    }

    // If a nearby host initiated a section change
    public void selectSection(int position) {
        if (mainActivityInterface.getSong().getFiletype().equals("PDF") &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pdfPageAdapter.sectionSelected(position);
        } else if (mainActivityInterface.getMode().equals(mode_stage)) {
            new Handler().postDelayed(()-> {
                stageSectionAdapter.clickOnSection(position);
                performanceShowSection(position);
            },50);
        }
    }

}

package com.garethevans.church.opensongtablet.performance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.abcnotation.ABCPopup;
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
    private int widthAfterScale;
    private int heightAfterScale;
    private int waitingOnViewsToDraw;
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
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mainActivityInterface.onBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
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
        if (mainActivityInterface.getToolbar()!=null) {
            mainActivityInterface.getToolbar().setPerformanceMode(false);
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
        mainActivityInterface.updateFragment("updateSongMenuSortTitles",this,null);

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
        mainActivityInterface.getToolbar().setPerformanceMode(true);
        mainActivityInterface.showActionBar();

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

        // Tint the watermark to the text colour
        myView.waterMark.setColorFilter(mainActivityInterface.getMyThemeColors().getLyricsTextColor(),
                android.graphics.PorterDuff.Mode.SRC_IN);

        removeViews();

        doSongLoad(mainActivityInterface.getPreferences().getMyPreferenceString("songFolder",getString(R.string.mainfoldername)),
                mainActivityInterface.getPreferences().getMyPreferenceString("songFilename","Welcome to OpenSongApp"));

        // Check if we need to show an alert
        if (mainActivityInterface.getAlertChecks().showPlayServicesAlert() ||
        mainActivityInterface.getAlertChecks().showBackup() || mainActivityInterface.getAlertChecks().showUpdateInfo()) {
            AlertInfoBottomSheet alertInfoBottomSheet = new AlertInfoBottomSheet();
            alertInfoBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "AlertInfoBottomSheet");
        }

        // Pass a reference of the zoom layout to the next/prev so we can stop fling scrolls
        mainActivityInterface.getDisplayPrevNext().setZoomLayout(myView.zoomLayout);

        return myView.getRoot();
    }

    // Getting the preferences and helpers ready
    private void initialiseHelpers() {
        stickyPopUp = new StickyPopUp(requireContext());
        abcPopup = new ABCPopup(requireContext());
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
        if (mainActivityInterface.getMode().equals(getString(R.string.mode_performance))) {
            myView.mypage.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
            myView.waterMark.setVisibility(View.VISIBLE);
        } else {
            // Stage Mode - sections have correct colour, but the background is different - set to colorPrimary
            myView.mypage.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            myView.waterMark.setVisibility(View.GONE);
        }
        mainActivityInterface.updateOnScreenInfo("setpreferences");
        myView.inlineSetList.initialisePreferences(requireContext(),mainActivityInterface);
        myView.inlineSetList.prepareSet();

        boolean allowPinchToZoom = mainActivityInterface.getPreferences().getMyPreferenceBoolean("allowPinchToZoom",true);
        myView.zoomLayout.setAllowPinchToZoom(allowPinchToZoom);
        myView.recyclerView.setAllowPinchToZoom(allowPinchToZoom);
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
        myView.inlineSetList.updateInlineSetRemoved(from);
    }
    public void updateInlineSetAdded(SetItemInfo setItemInfo) {
        try {
            myView.inlineSetList.updateInlineSetAdded(setItemInfo);
        } catch (Exception e) {
            Log.d(TAG,"Couldn't update inline set - might just not be shown currently");
        }
    }
    public void initialiseInlineSetItem(int position) {
        myView.inlineSetList.initialiseInlineSetItem(position);
    }

    // This stuff loads the song and prepares the views
    public void doSongLoad(String folder, String filename) {
        // Loading the song is dealt with in this fragment as specific actions are required

        mainActivityInterface.closeDrawer(true);

        // Stop any autoscroll if required
        mainActivityInterface.getAutoscroll().stopAutoscroll();

        // During the load song call, the song is cleared
        // However ii first extracts the folder and filename we've just set
        mainActivityInterface.getSong().setFolder(folder);
        mainActivityInterface.getSong().setFilename((filename));

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());

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
                if (myView.recyclerView.getVisibility() == View.VISIBLE) {
                    myView.recyclerView.startAnimation(animSlideOut);
                }
            });
            myView.pageHolder.post(() -> {
                if (myView.pageHolder.getVisibility()==View.VISIBLE) {
                    myView.pageHolder.startAnimation(animSlideOut);
                }
            });
            handler.postDelayed(this::prepareSongViews,requireContext().getResources().getInteger(R.integer.slide_out_time));
        });
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
            waitingOnViewsToDraw = 0;

            // Reset the song sheet titles and views
            removeViews();

            // Set the default color
            myView.pageHolder.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());

            // Update the toolbar with the song (null).  This also sets the positionInSet in SetActions
            mainActivityInterface.updateToolbar(null);

            // If we are in a set, send that info to the inline set custom view to see if it should draw
            myView.inlineSetList.checkVisibility();

            int[] screenSizes = mainActivityInterface.getDisplayMetrics();
            int[] margins = mainActivityInterface.getWindowFlags().getMargins();
            int screenWidth = screenSizes[0];
            int screenHeight = screenSizes[1];

            availableWidth = screenWidth - margins[0] - margins[2] - myView.inlineSetList.getInlineSetWidth();
            availableHeight = screenHeight - margins[1] - margins[3] - mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar());

            int[] viewPadding = mainActivityInterface.getViewMargins();
            //Log.d(TAG,"LEFT margins[0]:"+margins[0]+"   viewPadding:"+viewPadding[0]);
            //Log.d(TAG,"RIGHT margins[2]:"+margins[2]+"   viewPadding:"+viewPadding[1]);
            //Log.d(TAG,"TOP margins[0]+actionbar:"+(margins[1]+mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar()))+"   viewPadding:"+viewPadding[2]);
            //Log.d(TAG,"BOTTOM margins[0]:"+margins[3]+"   viewPadding:"+viewPadding[3]);

            // TODO FOR FRANK TEST - DELETE IF THIS DOESN'T WORK!
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

        pdfPageAdapter = new PDFPageAdapter(requireContext(), mainActivityInterface, displayInterface,
                availableWidth, availableHeight, myView.inlineSetList.getInlineSetWidth());

        myView.recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                myView.recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                heightBeforeScale = pdfPageAdapter.getHeight();
                heightAfterScale = heightBeforeScale;
                recyclerLayoutManager.setSizes(pdfPageAdapter.getHeights(), availableHeight);
                myView.recyclerView.setPadding(myView.inlineSetList.getInlineSetWidth(),0,0,0);
                myView.recyclerView.setMaxScrollY(heightAfterScale - availableHeight);

                // Do the slide in
                myView.recyclerView.setVisibility(View.VISIBLE);
                myView.recyclerView.startAnimation(animSlideIn);

                // Get a null screenshot
                getScreenshot(0, 0, 0);

                // Deal with song actions to run after display (highlighter, notes, etc)
                dealWithStuffAfterReady();
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

        if (widthBeforeScale>0 && heightBeforeScale>0) {
            scaleFactor = Math.min((float)availableWidth/(float)widthBeforeScale, (float)availableHeight/(float)heightBeforeScale);
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
                myView.recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                heightBeforeScale = imageSlideAdapter.getHeight();
                heightAfterScale = heightBeforeScale;
                recyclerLayoutManager.setSizes(imageSlideAdapter.getHeights(),availableHeight);
                myView.recyclerView.setMaxScrollY(heightAfterScale - availableHeight);

                // Slide in
                myView.recyclerView.setVisibility(View.VISIBLE);
                myView.recyclerView.startAnimation(animSlideIn);

                // Deal with song actions to run after display (highlighter, notes, etc)
                dealWithStuffAfterReady();

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
            mainActivityInterface.getShowToast().doIt(getString(R.string.not_allowed));
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
        if (mainActivityInterface.getSongSheetTitleLayout() != null &&
                !mainActivityInterface.getMode().equals(getString(R.string.mode_presenter))) {
            // Check the header isn't already attached to a view
            if (mainActivityInterface.getSongSheetTitleLayout().getParent()!=null) {
                ((ViewGroup) mainActivityInterface.getSongSheetTitleLayout().getParent()).removeAllViews();
            }

            ViewTreeObserver vto = myView.testPane.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (myView!=null) {
                        try {
                            myView.testPane.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            setUpTestViewListener();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
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
        waitingOnViewsToDraw = mainActivityInterface.getSectionViews().size();

        // Add the views and wait for the vto of each to finish
        myView.songView.clearViews();
        myView.testPane.removeAllViews();

        for (View view : mainActivityInterface.getSectionViews()) {
            if (view.getParent()!=null) {
                // Still attached - remove it
                ((ViewGroup)view.getParent()).removeView(view);
            }
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // In case rogue calls get fired, only proceed if we should
                    if (waitingOnViewsToDraw>0) {
                        waitingOnViewsToDraw --;
                        if (waitingOnViewsToDraw==0) {
                            // This was the last item, so move on
                            songIsReadyToDisplay();
                            waitingOnViewsToDraw = 0;
                        }
                    } else {
                        waitingOnViewsToDraw = 0;
                    }
                }
            });
            myView.testPane.addView(view);
        }
    }
    private void songIsReadyToDisplay() {
        // Set the page holder to fullscreen for now
        try {
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
            if (mainActivityInterface.getMode().equals(getString(R.string.mode_stage))) {
                // We are in Stage mode so use the recyclerView
                myView.recyclerView.setVisibility(View.INVISIBLE);
                myView.pageHolder.setVisibility(View.GONE);
                myView.songView.setVisibility(View.GONE);
                myView.zoomLayout.setVisibility(View.GONE);
                stageSectionAdapter = new StageSectionAdapter(requireContext(), mainActivityInterface,
                        displayInterface, myView.inlineSetList.getInlineSetWidth());

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

                        // Slide in
                        myView.recyclerView.setVisibility(View.VISIBLE);
                        myView.recyclerView.startAnimation(animSlideIn);

                        dealWithStuffAfterReady();

                        // Get a null screenshot
                        getScreenshot(0, 0, 0);

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

                myView.zoomLayout.setPageSize(availableWidth, availableHeight);
                myView.pageHolder.getLayoutParams().width = availableWidth;
                myView.pageHolder.getLayoutParams().height = availableHeight;

                float[] scaleInfo = mainActivityInterface.getProcessSong().addViewsToScreen(
                        false, mainActivityInterface.getSectionViews(),
                        mainActivityInterface.getSectionWidths(), mainActivityInterface.getSectionHeights(),
                        myView.pageHolder, myView.songView, myView.songSheetTitle,
                        availableWidth, availableHeight, myView.songView.getCol1(), myView.songView.getCol2(),
                        myView.songView.getCol3(), false, getResources().getDisplayMetrics());

                // Determine how many colums are scaled
                heightAfterScale = 0;
                if (scaleInfo.length==2) {
                    // 1 column.  [0]=scaleSize, [1]=scaled height
                    scaleFactor = scaleInfo[0];
                    heightAfterScale = (int)scaleInfo[1];
                    for (int x = 0; x < mainActivityInterface.getSectionViews().size(); x++) {
                        widthBeforeScale = Math.max(widthBeforeScale, mainActivityInterface.getSectionWidths().get(x));
                        heightBeforeScale += mainActivityInterface.getSectionHeights().get(x);
                    }
                    widthAfterScale = (int) (widthBeforeScale * scaleFactor);
                    myView.pageHolder.getLayoutParams().width = widthAfterScale;
                } else if (scaleInfo.length==4) {
                    // 2 columns. [0]=col1scale  [1]=col2scale  [2]=sectionnum for col2  [3]=biggest col height
                    scaleFactor = Math.max(scaleInfo[0],scaleInfo[1]);
                    heightAfterScale = (int)scaleInfo[3];
                    myView.pageHolder.getLayoutParams().width = availableWidth;
                    myView.songView.getLayoutParams().width = availableWidth;
                }  else if (scaleInfo.length==6) {
                    // 3 columns. [0]=col1scale  [1]=col2scale  [2]=col3scale
                    // [3]=sectionnum for col2  [4]=sectionbum for col3 [5]=biggest col height
                    scaleFactor = Math.max(scaleInfo[0],Math.max(scaleInfo[1],scaleInfo[2]));
                    heightAfterScale = (int)scaleInfo[5];
                    myView.pageHolder.getLayoutParams().width = availableWidth;
                    myView.songView.getLayoutParams().width = availableWidth;
                }

                heightAfterScale = heightAfterScale + mainActivityInterface.getSongSheetTitleLayout().getHeight();

                myView.pageHolder.getLayoutParams().height = heightAfterScale;
                myView.songView.getLayoutParams().height = heightAfterScale;

                // Pass this scale factor to the zoom layout as the new minimum scale
                myView.zoomLayout.setCurrentScale(scaleFactor);
                myView.zoomLayout.setSongSize(widthAfterScale,heightAfterScale);

                myView.zoomLayout.post(() -> {
                    try {
                        // The new song sizes were sent to the zoomLayout in ProcessSong
                        int topPadding = 0;

                        myView.pageHolder.setVisibility(View.VISIBLE);
                        myView.pageHolder.startAnimation(animSlideIn);

                        dealWithStuffAfterReady();

                        // Try to take a screenshot ready for any highlighter actions that may be called
                        getScreenshot(myView.pageHolder.getWidth(), myView.pageHolder.getHeight()-myView.songSheetTitle.getHeight(), topPadding);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This stuff deals with running song action stuff
    private void dealWithStuffAfterReady() {
        // Run this after the animate in delay to stop animation jitter
        new Handler().postDelayed(() -> {
            // Set the load status to the song (used to enable nearby section change listener)
            mainActivityInterface.getSong().setCurrentlyLoading(false);

            // Send the autoscroll information (if required)
            mainActivityInterface.getAutoscroll().initialiseSongAutoscroll(heightAfterScale, availableHeight);

            // Now deal with the highlighter file
            if (mainActivityInterface.getMode().equals(getString(R.string.mode_performance))) {
                dealWithHighlighterFile(widthBeforeScale, heightBeforeScale);
            }

            // Load up the sticky notes if the user wants them
            dealWithStickyNotes(false, false);

            // IV - Consume any later pending client section change received from Host (-ve value)
            if (mainActivityInterface.getNearbyConnections().hasValidConnections() &&
                    !mainActivityInterface.getNearbyConnections().getIsHost() &&
                    mainActivityInterface.getNearbyConnections().getWaitingForSectionChange()) {
                int pendingSection = mainActivityInterface.getNearbyConnections().getPendingCurrentSection();

                // Reset the flags to off
                mainActivityInterface.getNearbyConnections().setWaitingForSectionChange(false);
                mainActivityInterface.getNearbyConnections().setPendingCurrentSection(-1);

                mainActivityInterface.getNearbyConnections().doSectionChange(pendingSection);
            }

            // Set the previous/next if we want to
            mainActivityInterface.getDisplayPrevNext().setPrevNext();

            // Start the pad (if the pads are activated and the pad is valid)
            mainActivityInterface.getPad().autoStartPad();

            // Update any midi commands (if any)
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("midiSendAuto",false)) {
                mainActivityInterface.getMidi().buildSongMidiMessages();
                mainActivityInterface.getMidi().sendSongMessages();
            }

            // Deal with capo information (if required)
            mainActivityInterface.dealWithCapo();

            // Update the secondary display (if present)
            displayInterface.updateDisplay("newSongLoaded");
            displayInterface.updateDisplay("setSongInfo");
            displayInterface.updateDisplay("setSongContent");

            // Send a call to nearby devices to process the song at their end
            if (mainActivityInterface.getNearbyConnections().hasValidConnections() &&
                    mainActivityInterface.getNearbyConnections().getIsHost()) {
                mainActivityInterface.getNearbyConnections().sendSongPayload();
            }

            // If we opened the app with and intent/file, check if we need to import
            tryToImportIntent();

        }, getResources().getInteger(R.integer.slide_in_time));
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
            if (!mainActivityInterface.getPreferences().
                    getMyPreferenceString("songAutoScale", "W").equals("N")) {
                // Set the highlighter image view to match
                myView.highlighterView.setVisibility(View.INVISIBLE);
                // Once the view is ready at the required size, deal with it
                ViewTreeObserver highlighterVTO = myView.highlighterView.getViewTreeObserver();
                highlighterVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        myView.highlighterView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        // Load in the bitmap with these dimensions
                        Bitmap highlighterBitmap = mainActivityInterface.getProcessSong().
                                getHighlighterFile(0, 0);

                        if (highlighterBitmap != null &&
                                mainActivityInterface.getPreferences().getMyPreferenceBoolean("drawingAutoDisplay", true)) {

                            myView.highlighterView.setVisibility(View.VISIBLE);
                            ViewGroup.LayoutParams rlp = myView.highlighterView.getLayoutParams();
                            rlp.width = (int)((float)w*scaleFactor);
                            rlp.height = (int)((float)h*scaleFactor);

                            myView.highlighterView.setLayoutParams(rlp);
                            RequestOptions requestOptions = new RequestOptions().centerInside().override(rlp.width,rlp.height);
                            GlideApp.with(requireContext()).load(highlighterBitmap).
                                    apply(requestOptions).
                                    into(myView.highlighterView);

                            myView.highlighterView.setPivotX(0f);
                            myView.highlighterView.setPivotY(0f);
                            //myView.highlighterView.setScaleX(scaleFactor);
                            //myView.highlighterView.setScaleY(scaleFactor);

                            // Hide after a certain length of time
                            int timetohide = mainActivityInterface.getPreferences().getMyPreferenceInt("timeToDisplayHighlighter", 0);
                            if (timetohide != 0) {
                                new Handler().postDelayed(() -> myView.highlighterView.setVisibility(View.GONE), timetohide);
                            }
                        } else {
                            myView.highlighterView.post(() -> {
                                if (myView!=null) {
                                    try {
                                        myView.highlighterView.setVisibility(View.GONE);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
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
            } else {
                myView.highlighterView.post(() -> {
                    try {
                        myView.highlighterView.setVisibility(View.GONE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
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
        if (mainActivityInterface.getMode().equals(getString(R.string.mode_performance))) {
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
                mainActivityInterface.showActionBar();
            }
            return gestureDetector.onTouchEvent(motionEvent);
        });

        myView.recyclerView.setGestureDetector(gestureDetector);
    }

    public void toggleScale() {
        if (myView.recyclerView.getVisibility()==View.VISIBLE) {
            // Resets the zoom
            myView.recyclerView.toggleScale();
        } else {
            // Toggles between different zooms
            myView.zoomLayout.toggleScale();
        }
    }

    public void updateSizes(int width, int height) {
        myView.zoomLayout.setSongSize(width,height);
    }

    // Received from MainActivity after a user clicked on a pdf page or a Stage Mode section
    public void performanceShowSection(int position) {
        // Scroll the recyclerView to the position
        myView.recyclerView.smoothScrollTo(requireContext(),recyclerLayoutManager,position);
        mainActivityInterface.getPresenterSettings().setCurrentSection(position);
        displayInterface.updateDisplay("showSection");
    }

    public void scrollToTop() {
        myView.recyclerView.smoothScrollTo(requireContext(),recyclerLayoutManager,0);
        myView.zoomLayout.scrollTo(0,0);
        mainActivityInterface.updateMargins();
    }

    // If a nearby host initiated a section change
    public void selectSection(int position) {
        if (mainActivityInterface.getSong().getFiletype().equals("PDF") &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pdfPageAdapter.sectionSelected(position);
        } else if (mainActivityInterface.getMode().equals(getString(R.string.mode_stage))) {
            stageSectionAdapter.sectionSelected(position);
        }
    }

}

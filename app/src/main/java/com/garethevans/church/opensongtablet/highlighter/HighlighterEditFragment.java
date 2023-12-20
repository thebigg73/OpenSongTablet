package com.garethevans.church.opensongtablet.highlighter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsHighlighterEditBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HighlighterEditFragment extends Fragment {

    private final String TAG = "HighlighterEdit";

    private MainActivityInterface mainActivityInterface;
    private SettingsHighlighterEditBinding myView;
    private Drawable whiteCheck, blackCheck;
    private int buttonActive, buttonInactive, drawingPenSize, drawingHighlighterSize,
            drawingEraserSize, drawingPenColor, drawingHighlighterColor, availableWidth,
            availableHeight, scaledWidth, scaledHeight;
    private String activeTool;
    private Bitmap screenShotBitmap, highlighterBitmap;
    private Uri highlighterUri;

    // The colours used in drawing
    private final int penBlack = 0xff000000;
    private final int penWhite = 0xffffffff;
    private final int penBlue = 0xff0000ff;
    private final int penRed = 0xffff0000;
    private final int penGreen = 0xff00ff00;
    private final int penYellow = 0xffffff00;
    private final int highlighterBlack = 0x66000000;
    private final int highlighterWhite = 0x66ffffff;
    private final int highlighterBlue = 0x660000ff;
    private final int highlighterRed = 0x66ff0000;
    private final int highlighterGreen = 0x6600ff00;
    private final int highlighterYellow = 0x66ffff00;
    private int currentColor;
    private int currentSize;
    private String edit_string="", highlight_string="", website_highlighter_string="",
            portrait_string="", landscape_string="", delete_string="", success_string="",
            not_saved_string="", cancel_string="", error_string="";

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(edit_string + " " + highlight_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsHighlighterEditBinding.inflate(inflater, container, false);

        prepareStrings();

        webAddress = website_highlighter_string;

        // Set up views
        setupViews();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            edit_string = getString(R.string.edit);
            highlight_string = getString(R.string.highlight);
            website_highlighter_string = getString(R.string.website_highlighter);
            portrait_string = getString(R.string.portrait);
            landscape_string = getString(R.string.landscape);
            delete_string = getString(R.string.delete);
            success_string = getString(R.string.success);
            not_saved_string = getString(R.string.not_saved);
            cancel_string = getString(R.string.cancel);
            error_string = getString(R.string.error);
        }
    }
    private void setupViews() {
        if (getContext()!=null) {
            buttonActive = ContextCompat.getColor(getContext(), R.color.colorSecondary);
            buttonInactive = ContextCompat.getColor(getContext(), R.color.colorAltPrimary);
            whiteCheck = VectorDrawableCompat.create(getResources(), R.drawable.check, getContext().getTheme());
            if (whiteCheck != null) {
                whiteCheck.mutate();
            }
            blackCheck = VectorDrawableCompat.create(getResources(), R.drawable.check, getContext().getTheme());
            if (blackCheck != null) {
                blackCheck.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
            }
        }

        mainActivityInterface.setDrawNotes(myView.drawNotes);
        mainActivityInterface.getDrawNotes().resetVars();

        setToolPreferences();

        // Set the drawNotes and imageView to be the same height as the image
        ViewTreeObserver imageVTO = myView.glideImage.getViewTreeObserver();
        imageVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Get the width (match parent) in pixels
                availableWidth = myView.backgroundView.getWidth();
                availableHeight = myView.backgroundView.getHeight();
                Log.d(TAG,"available w x h:"+availableWidth+"x"+availableHeight);
                if (mainActivityInterface.getSong().getFiletype().equals("IMG")) {
                    getImageFile();

                } else if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                    getPDFPage();

                } else {
                    getScreenshot();

                }

                // Sizes and scaling have been figured out, so adjust the size of the song image
                Log.d(TAG,"scaled w x h:"+scaledWidth+"x"+scaledHeight);

                setImageSize();

                // Put the song image into the view
                RequestOptions requestOptions = new RequestOptions().override(scaledWidth,scaledHeight);
                Glide.with(myView.glideImage).load(screenShotBitmap).apply(requestOptions).
                        into(myView.glideImage);

                // Set the original highlighter file if it exists
                mainActivityInterface.getDrawNotes().loadExistingHighlighter(
                        mainActivityInterface, scaledWidth, scaledHeight);

                // Remove the VTO as we're done!
                myView.glideImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        // Set up the bottomSheet
        bottomSheetBar();
    }

    private void getScreenshot() {
        // If we are using a normal XML song and in performance mode, we have a screenshot of the song view
        int w = mainActivityInterface.getScreenshot().getWidth();
        int h = mainActivityInterface.getScreenshot().getHeight();

        Log.d(TAG,"screenshot: "+w+"x"+h);
        // Get the scale
        setScale(w,h);

        // Set the bitmap as a reference to the screenshot
        screenShotBitmap = mainActivityInterface.getScreenshot();
    }

    private void getImageFile() {
        // We are using an image file
        screenShotBitmap = mainActivityInterface.getProcessSong().getSongBitmap(mainActivityInterface.getSong().getFolder(),
                mainActivityInterface.getSong().getFilename());
        int w = screenShotBitmap.getWidth();
        int h = screenShotBitmap.getHeight();

        // Set the scale
        setScale(w,h);
    }

    private void getPDFPage() {
        // Load an image of the currently selected page
        Log.d(TAG,"pageNumber="+mainActivityInterface.getSong().getPdfPageCurrent());
        screenShotBitmap = mainActivityInterface.getProcessSong().getBitmapFromPDF(
                mainActivityInterface.getSong().getFolder(),
                mainActivityInterface.getSong().getFilename(),mainActivityInterface.getSong().getPdfPageCurrent(),
                availableWidth,availableHeight,"Y", true);

        int w = screenShotBitmap.getWidth();
        int h = screenShotBitmap.getHeight();

        // Set the scale
        setScale(w,h);
    }

    private void setScale(int bitmapWidth, int bitmapHeight) {
        float maxScaleX = (float) availableWidth / (float) bitmapWidth;
        float maxScaleY = (float) availableHeight / (float) bitmapHeight;
        float maxScale = Math.min(maxScaleX,maxScaleY);
        scaledWidth = (int) (bitmapWidth * maxScale);
        scaledHeight = (int) (bitmapHeight * maxScale);
    }

    private void setImageSize() {
        // The song screenshot/image
        ViewGroup.LayoutParams layoutParams = myView.glideImage.getLayoutParams();
        layoutParams.width = scaledWidth;
        layoutParams.height = scaledHeight;
        myView.glideImage.setLayoutParams(layoutParams);

        // Get a scaled width and height of the bitmap being drawn as the highlighter
        ViewGroup.LayoutParams layoutParams2 = mainActivityInterface.getDrawNotes().getLayoutParams();
        layoutParams2.width = scaledWidth;
        layoutParams2.height = scaledHeight;
        mainActivityInterface.getDrawNotes().setLayoutParams(layoutParams2);
    }
    private void bottomSheetBar() {
        bottomSheetBehavior = BottomSheetBehavior.from(myView.bottomSheet.bottomSheet);
        bottomSheetBehavior.setHideable(false);
        myView.bottomSheet.bottomSheetTab.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bottomSheetBehavior.setPeekHeight(myView.bottomSheet.bottomSheetTab.getMeasuredHeight());
                myView.bottomSheet.bottomSheetTab.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        //bottomSheetBehavior.setGestureInsetBottomIgnored(true);

        myView.bottomSheet.bottomSheetTab.setOnClickListener(v -> {
            if (mainActivityInterface.getDrawNotes().isEnabled()) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        mainActivityInterface.getDrawNotes().setEnabled(true);
                        myView.dimBackground.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_SETTLING:
                        checkUndos();
                        checkRedos();
                        mainActivityInterface.getDrawNotes().setEnabled(false);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                myView.dimBackground.setVisibility(View.VISIBLE);
            }
        });
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setSkipCollapsed(true);
        checkUndos();
        checkRedos();
    }

    private void setToolPreferences() {
        activeTool = mainActivityInterface.getPreferences().getMyPreferenceString("drawingTool", "pen");
        drawingPenSize = mainActivityInterface.getPreferences().getMyPreferenceInt("drawingPenSize", 20);
        drawingHighlighterSize = mainActivityInterface.getPreferences().getMyPreferenceInt("drawingHighlighterSize", 20);
        drawingEraserSize = mainActivityInterface.getPreferences().getMyPreferenceInt("drawingEraserSize", 20);
        drawingPenColor = mainActivityInterface.getPreferences().getMyPreferenceInt("drawingPenColor", penRed);
        drawingHighlighterColor = mainActivityInterface.getPreferences().getMyPreferenceInt("drawingHighlighterColor", highlighterYellow);
        setActiveTool();
        setColors();
        setSizes();
    }

    private void setActiveTool() {
        Log.d(TAG, "activeTool=" + activeTool);
        setToolButtonActive(myView.bottomSheet.pencilFAB, activeTool.equals("pen"));
        setToolButtonActive(myView.bottomSheet.highlighterFAB, activeTool.equals("highlighter"));
        setToolButtonActive(myView.bottomSheet.eraserFAB, activeTool.equals("eraser"));
        setToolButtonActive(myView.bottomSheet.undoFAB, false);
        setToolButtonActive(myView.bottomSheet.redoFAB, false);
        setToolButtonActive(myView.bottomSheet.deleteFAB, false);
        if (activeTool.equals("pen")) {
            currentColor = drawingPenSize;
            currentSize = drawingPenSize;
            mainActivityInterface.getDrawNotes().setCurrentPaint(drawingPenSize, drawingPenColor);
            mainActivityInterface.getDrawNotes().setErase(false);

        } else if (activeTool.equals("highlighter")) {
            currentColor = drawingHighlighterColor;
            currentSize = drawingHighlighterSize;
            mainActivityInterface.getDrawNotes().setCurrentPaint(drawingHighlighterSize, drawingHighlighterColor);
            mainActivityInterface.getDrawNotes().setErase(false);
        } else {
            currentColor = highlighterBlack;
            currentSize = drawingEraserSize;
            mainActivityInterface.getDrawNotes().setCurrentPaint(drawingEraserSize, highlighterBlack);
            mainActivityInterface.getDrawNotes().setErase(true);
        }
        hideColors();
    }

    private void setToolButtonActive(FloatingActionButton button, boolean active) {
        if (active) {
            button.setBackgroundTintList(ColorStateList.valueOf(buttonActive));
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(buttonInactive));
        }
    }

    private void setColors() {
        if (activeTool.equals("highlighter")) {
            currentColor = drawingHighlighterColor;
        } else {
            currentColor = drawingPenColor;
        }
        setColorActive(myView.bottomSheet.colorBlack, currentColor == penBlack || currentColor == highlighterBlack);
        setColorActive(myView.bottomSheet.colorWhite, currentColor == penWhite || currentColor == highlighterWhite);
        setColorActive(myView.bottomSheet.colorYellow, currentColor == penYellow || currentColor == highlighterYellow);
        setColorActive(myView.bottomSheet.colorRed, currentColor == penRed || currentColor == highlighterRed);
        setColorActive(myView.bottomSheet.colorGreen, currentColor == penGreen || currentColor == highlighterGreen);
        setColorActive(myView.bottomSheet.colorBlue, currentColor == penBlue || currentColor == highlighterBlue);
    }

    private void setColorActive(FloatingActionButton button, boolean active) {
        if (active) {
            button.setImageDrawable(getBestCheck());
        } else {
            button.setImageDrawable(null);
        }
    }

    private void hideColors() {
        if (activeTool.equals("eraser")) {
            myView.bottomSheet.colorsLayout.setVisibility(View.INVISIBLE);
        } else {
            myView.bottomSheet.colorsLayout.setVisibility(View.VISIBLE);
        }
    }

    private Drawable getBestCheck() {
        if (activeTool.equals("pen") && (drawingPenColor == penWhite || drawingPenColor == penYellow)) {
            return blackCheck;
        } else if (activeTool.equals("highlighter") && (drawingHighlighterColor == highlighterWhite ||
                drawingHighlighterColor == highlighterYellow)) {
            return blackCheck;
        } else {
            return whiteCheck;
        }
    }

    private void setSizes() {
        if (activeTool.equals("pen")) {
            myView.bottomSheet.sizeSlider.setValue(drawingPenSize);
        } else if (activeTool.equals("highlighter")) {
            myView.bottomSheet.sizeSlider.setValue(drawingHighlighterSize);
        } else {
            myView.bottomSheet.sizeSlider.setValue(drawingEraserSize);
        }
    }

    private void setListeners() {
        myView.bottomSheet.pencilFAB.setOnClickListener(v -> changeTool("pen"));
        myView.bottomSheet.highlighterFAB.setOnClickListener(v -> changeTool("highlighter"));
        myView.bottomSheet.eraserFAB.setOnClickListener(v -> changeTool("eraser"));
        myView.bottomSheet.undoFAB.setOnClickListener(v -> undo());
        myView.bottomSheet.redoFAB.setOnClickListener(v -> redo());
        myView.bottomSheet.deleteFAB.setOnClickListener(v -> delete());

        myView.bottomSheet.colorBlack.setOnClickListener(v -> changeColor(penBlack, highlighterBlack));
        myView.bottomSheet.colorWhite.setOnClickListener(v -> changeColor(penWhite, highlighterWhite));
        myView.bottomSheet.colorYellow.setOnClickListener(v -> changeColor(penYellow, highlighterYellow));
        myView.bottomSheet.colorRed.setOnClickListener(v -> changeColor(penRed, highlighterRed));
        myView.bottomSheet.colorGreen.setOnClickListener(v -> changeColor(penGreen, highlighterGreen));
        myView.bottomSheet.colorBlue.setOnClickListener(v -> changeColor(penBlue, highlighterBlue));

        myView.dimBackground.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));
        myView.bottomSheet.sizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            // Update the hint text
            myView.bottomSheet.sizeSlider.setHint(String.valueOf(value));
        });
        myView.bottomSheet.sizeSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                // Save the value
                String prefName = null;
                int size = (int) myView.bottomSheet.sizeSlider.getValue();
                switch (activeTool) {
                    case "pen":
                        prefName = "drawingPenSize";
                        break;
                    case "highlighter":
                        prefName = "drawingHighlighterSize";
                        break;
                    case "eraser":
                        prefName = "drawingEraserSize";
                        break;
                }
                if (prefName != null) {
                    mainActivityInterface.getPreferences().setMyPreferenceInt(prefName, size);
                }
                mainActivityInterface.getDrawNotes().setCurrentPaint(size, currentColor);
                mainActivityInterface.getDrawNotes().postInvalidate();
            }
        });
        myView.bottomSheet.saveButton.setOnClickListener(v -> saveFile());
    }

    private void undo() {
        // Check undo then check status
        mainActivityInterface.getDrawNotes().undo();
        checkUndos();
    }

    private void redo() {
        // Check redo then check status
        mainActivityInterface.getDrawNotes().redo();
        checkRedos();
    }

    private void checkUndos() {
        myView.bottomSheet.undoFAB.setEnabled(mainActivityInterface.getDrawNotes().getAllPaths().size() > 0);
    }

    private void checkRedos() {
        myView.bottomSheet.redoFAB.setEnabled(mainActivityInterface.getDrawNotes().getUndoPaths().size() > 0);
    }

    private void delete() {
        // Prompt the user for an 'Are you sure'
        String orientation;
        if (getContext() == null || getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientation = portrait_string;
        } else {
            orientation = landscape_string;
        }
        if (getActivity() != null) {
            AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("deleteHighlighter",
                    delete_string + " " + highlight_string + " (" + orientation + ")", null,
                    "highlighterEditFragment", this, mainActivityInterface.getSong());
            areYouSureBottomSheet.show(getActivity().getSupportFragmentManager(), "are_you_sure");
        }
    }

    public void doDelete(boolean confirmed) {
        if (confirmed && getContext()!=null) {
            // Set the original highlighter file if it exists
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "",
                    mainActivityInterface.getProcessSong().getHighlighterFilename(mainActivityInterface.getSong(), getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT,-1));
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doDelete deleteFile "+uri);
            if (mainActivityInterface.getStorageAccess().deleteFile(uri)) {
                mainActivityInterface.getShowToast().doIt(success_string);
            } else {
                mainActivityInterface.getShowToast().doIt(not_saved_string);
            }
            mainActivityInterface.getDrawNotes().delete();
            checkUndos();
            checkRedos();
        } else {
            mainActivityInterface.getShowToast().doIt(cancel_string);
        }
    }

    private void changeTool(String tool) {
        activeTool = tool;
        mainActivityInterface.getPreferences().setMyPreferenceString("drawingTool", tool);
        setActiveTool();
        setColors();
        setSizes();
        mainActivityInterface.getDrawNotes().postInvalidate();

    }

    private void changeColor(int colorPen, int colorHighlighter) {
        if (activeTool.equals("pen")) {
            drawingPenColor = colorPen;
            currentColor = colorPen;
            mainActivityInterface.getPreferences().setMyPreferenceInt("drawingPenColor", colorPen);
        } else if (activeTool.equals("highlighter")) {
            drawingHighlighterColor = colorHighlighter;
            currentColor = colorHighlighter;
            mainActivityInterface.getPreferences().setMyPreferenceInt("drawingHighlighterColor", colorHighlighter);
        }
        setColors();
        mainActivityInterface.getDrawNotes().setCurrentPaint(currentSize, currentColor);
    }



    private void saveFile() {
        // Get the bitmap of the drawNotes in a new thread
        if (getContext()!=null) {
            int orientation = getContext().getResources().getConfiguration().orientation;
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                Handler handler = new Handler(Looper.getMainLooper());
                String hname = mainActivityInterface.getProcessSong().getHighlighterFilename(mainActivityInterface.getSong(), orientation == Configuration.ORIENTATION_PORTRAIT,-1);
                highlighterUri = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "", hname);
                // Check the uri exists for the outputstream to be valid
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " Create Highlighter/" + hname + "  deleteOld=false");
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(
                        false, highlighterUri, null, "Highlighter", "", hname);

                handler.post(() -> {
                    mainActivityInterface.getDrawNotes().setDrawingCacheEnabled(true);
                    try {
                        highlighterBitmap = mainActivityInterface.getDrawNotes().getDrawingCache();
                    } catch (Exception e) {
                        Log.d(TAG, "Error extracting the drawing");
                    } catch (OutOfMemoryError e) {
                        Log.d(TAG, "Out of memory trying to get the drawing");
                    }
                    if (highlighterUri != null && highlighterBitmap != null) {
                        Log.d(TAG, "newUri=" + highlighterUri);
                        Log.d(TAG, "bitmap=" + highlighterBitmap);
                        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(highlighterUri);
                        Log.d(TAG, "outputStream=" + outputStream);
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " saveFile writeImage " + highlighterUri);
                        mainActivityInterface.getStorageAccess().writeImage(outputStream, highlighterBitmap);
                        mainActivityInterface.getShowToast().doIt(success_string);
                    } else {
                        mainActivityInterface.getShowToast().doIt(error_string);
                    }
                });
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

package com.garethevans.church.opensongtablet.highlighter;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsHighlighterEditBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

import java.io.OutputStream;

public class HighlighterEditFragment extends Fragment {

    private final String TAG = "HighlighterEdit";

    private MainActivityInterface mainActivityInterface;
    private SettingsHighlighterEditBinding myView;
    private Drawable whiteCheck, blackCheck;
    private int drawingPenSize, drawingHighlighterSize, drawingEraserSize,
            drawingPenColor, drawingHighlighterColor, availableWidth,
            availableHeight, scaledWidth, scaledHeight;
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
    private String activeTool;

    private int currentColor;
    private int currentSize;
    private String edit_string = "", highlight_string = "", website_highlighter_string = "",
            portrait_string = "", landscape_string = "", delete_string = "", success_string = "",
            not_saved_string = "", cancel_string = "", error_string = "";

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

        // Check for the toolbox showcase
        if (getContext()!=null && mainActivityInterface!=null) {
            myView.draggableToolbox.checkShowcase(getContext(), mainActivityInterface);
        }

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext() != null) {
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
        if (getContext() != null) {
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
        mainActivityInterface.getDrawNotes().setHighlighterEditFrag(this);

        setToolPreferences();

        // Set the drawNotes and imageView to be the same height as the image
        ViewTreeObserver imageVTO = myView.glideImage.getViewTreeObserver();
        imageVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Get the width (match parent) in pixels
                availableWidth = myView.backgroundView.getWidth();
                availableHeight = myView.backgroundView.getHeight();
                Log.d(TAG, "available w x h:" + availableWidth + "x" + availableHeight);
                if (mainActivityInterface.getSong().getFiletype().equals("IMG")) {
                    getImageFile();

                } else if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                    getPDFPage();

                } else {
                    getScreenshot();

                }

                // Sizes and scaling have been figured out, so adjust the size of the song image
                Log.d(TAG, "scaled w x h:" + scaledWidth + "x" + scaledHeight);

                setImageSize();

                // Put the song image into the view
                RequestOptions requestOptions = new RequestOptions().override(scaledWidth, scaledHeight);
                Glide.with(myView.glideImage).load(screenShotBitmap).apply(requestOptions).
                        into(myView.glideImage);

                // Set the original highlighter file if it exists
                mainActivityInterface.getDrawNotes().loadExistingHighlighter(
                        mainActivityInterface, scaledWidth, scaledHeight);

                // Remove the VTO as we're done!
                myView.glideImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        checkUndos();
        checkRedos();
    }

    private void getScreenshot() {
        // Screenshot file
        screenShotBitmap = BitmapFactory.decodeFile(mainActivityInterface.getScreenshotFile().getAbsoluteFile().getAbsolutePath());
        int w = screenShotBitmap.getWidth();
        int h = screenShotBitmap.getHeight();

        Log.d(TAG, "screenshot: " + w + "x" + h);
        // Get the scale
        setScale(w, h);
    }

    private void getImageFile() {
        // We are using an image file
        screenShotBitmap = mainActivityInterface.getProcessSong().getSongBitmap(mainActivityInterface.getSong().getFolder(),
                mainActivityInterface.getSong().getFilename());
        int w = screenShotBitmap.getWidth();
        int h = screenShotBitmap.getHeight();

        // Set the scale
        setScale(w, h);
    }

    private void getPDFPage() {
        // Load an image of the currently selected page
        Log.d(TAG, "pageNumber=" + mainActivityInterface.getSong().getPdfPageCurrent());
        screenShotBitmap = mainActivityInterface.getProcessSong().getBitmapFromPDF(
                mainActivityInterface.getSong().getFolder(),
                mainActivityInterface.getSong().getFilename(), mainActivityInterface.getSong().getPdfPageCurrent(),
                availableWidth, availableHeight, "Y", true);

        int w = screenShotBitmap.getWidth();
        int h = screenShotBitmap.getHeight();

        // Set the scale
        setScale(w, h);
    }

    private void setScale(int bitmapWidth, int bitmapHeight) {
        float maxScaleX = (float) availableWidth / (float) bitmapWidth;
        float maxScaleY = (float) availableHeight / (float) bitmapHeight;
        float maxScale = Math.min(maxScaleX, maxScaleY);
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
        myView.draggableToolbox.changeDrawable(activeTool);

        if (activeTool.equals("pen")) {
            currentColor = drawingPenColor;
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

    private void setColors() {
        String colorChosen;
        switch (currentColor) {
            case penBlack:
            case highlighterBlack:
            default:
                colorChosen = "black";
                break;
            case penWhite:
            case highlighterWhite:
                colorChosen = "white";
                break;
            case penYellow:
            case highlighterYellow:
                colorChosen = "yellow";
                break;
            case penRed:
            case highlighterRed:
                colorChosen = "red";
                break;
            case penGreen:
            case highlighterGreen:
                colorChosen = "green";
                break;
            case penBlue:
            case highlighterBlue:
                colorChosen = "blue";
                break;
        }
        if (activeTool.equals("highlighter")) {
            currentColor = drawingHighlighterColor;
        } else {
            currentColor = drawingPenColor;
        }
        myView.draggableToolbox.setColor(colorChosen, getBestCheck());
    }

    private void hideColors() {
        myView.draggableToolbox.hideColors(activeTool.equals("eraser"));
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
            myView.draggableToolbox.setSizeSlider(drawingPenSize);
        } else if (activeTool.equals("highlighter")) {
            myView.draggableToolbox.setSizeSlider(drawingHighlighterSize);
        } else {
            myView.draggableToolbox.setSizeSlider(drawingEraserSize);
        }
    }

    private void setListeners() {
        myView.draggableToolbox.setPenOnClickListener(v -> changeTool("pen"));
        myView.draggableToolbox.setHighlighterOnClickListener(v -> changeTool("highlighter"));
        myView.draggableToolbox.setEraserOnClickListener(v -> changeTool("eraser"));
        myView.draggableToolbox.setUndoOnClickListener(v -> undo());
        myView.draggableToolbox.setRedoOnClickListener(v -> redo());
        myView.draggableToolbox.setDeleteOnClickListener(v -> delete());

        myView.draggableToolbox.setBlackFABOnClickListener(v -> changeColor(penBlack, highlighterBlack));
        myView.draggableToolbox.setWhiteFABOnClickListener(v -> changeColor(penWhite, highlighterWhite));
        myView.draggableToolbox.setYellowFABOnClickListener(v -> changeColor(penYellow, highlighterYellow));
        myView.draggableToolbox.setRedFABOnClickListener(v -> changeColor(penRed, highlighterRed));
        myView.draggableToolbox.setGreenFABOnClickListener(v -> changeColor(penGreen, highlighterGreen));
        myView.draggableToolbox.setBlueFABOnClickListener(v -> changeColor(penBlue, highlighterBlue));

        myView.draggableToolbox.setSizeSliderListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                // Save the value
                String prefName = null;
                int size = myView.draggableToolbox.getSizeSliderValue();
                switch (activeTool) {
                    case "pen":
                        prefName = "drawingPenSize";
                        drawingPenSize = size;
                        break;
                    case "highlighter":
                        prefName = "drawingHighlighterSize";
                        drawingHighlighterSize = size;
                        break;
                    case "eraser":
                        prefName = "drawingEraserSize";
                        drawingEraserSize = size;
                        break;
                }
                if (prefName != null) {
                    mainActivityInterface.getPreferences().setMyPreferenceInt(prefName, size);
                }
                mainActivityInterface.getDrawNotes().setCurrentPaint(size, currentColor);
                mainActivityInterface.getDrawNotes().postInvalidate();
            }
        });
        myView.draggableToolbox.setSaveOnClickListener(v -> saveFile());
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

    public void checkUndos() {
        myView.draggableToolbox.setUndoFABEnabled(mainActivityInterface.getDrawNotes().getAllPaths().size() > 0);
    }

    public void checkRedos() {
        myView.draggableToolbox.setRedoFABEnabled(mainActivityInterface.getDrawNotes().getUndoPaths().size() > 0);
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
        if (confirmed && getContext() != null) {
            // Set the original highlighter file if it exists
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "",
                    mainActivityInterface.getProcessSong().getHighlighterFilename(mainActivityInterface.getSong(), getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT, -1));
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " doDelete deleteFile " + uri);
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
        Log.d(TAG,"activeTool:"+activeTool);
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
        if (getContext() != null) {
            int orientation = getContext().getResources().getConfiguration().orientation;
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                String hname = mainActivityInterface.getProcessSong().getHighlighterFilename(mainActivityInterface.getSong(), orientation == Configuration.ORIENTATION_PORTRAIT, -1);
                highlighterUri = mainActivityInterface.getStorageAccess().getUriForItem("Highlighter", "", hname);
                // Check the uri exists for the outputstream to be valid
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " Create Highlighter/" + hname + "  deleteOld=false");
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(
                        false, highlighterUri, null, "Highlighter", "", hname);

                mainActivityInterface.getMainHandler().post(() -> {
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
        mainActivityInterface.getDrawNotes().setHighlighterEditFrag(null);
    }
}

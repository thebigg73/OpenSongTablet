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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.databinding.SettingsHighlighterEditBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;

import java.io.OutputStream;

public class HighlighterEditFragment extends Fragment {

    private final String TAG = "HighlighterEdit";

    private MainActivityInterface mainActivityInterface;
    private SettingsHighlighterEditBinding myView;
    private Drawable whiteCheck, blackCheck;
    private int buttonActive, buttonInactive, drawingPenSize, drawingHighlighterSize,
            drawingEraserSize, drawingPenColor, drawingHighlighterColor;
    private String activeTool;

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

    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsHighlighterEditBinding.inflate(inflater, container, false);
        mainActivityInterface.updateToolbar(getString(R.string.edit) + " " + getString(R.string.highlight));

        // Set up views
        setupViews();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        buttonActive = ContextCompat.getColor(requireContext(), R.color.colorSecondary);
        buttonInactive = ContextCompat.getColor(requireContext(), R.color.colorAltPrimary);
        whiteCheck = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_white_36dp);
        if (whiteCheck != null) {
            whiteCheck.mutate();
        }
        blackCheck = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_white_36dp);
        if (blackCheck != null) {
            blackCheck.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        }

        mainActivityInterface.setDrawNotes(myView.drawNotes);
        mainActivityInterface.getDrawNotes().resetVars();

        setToolPreferences();

        // Set the drawNotes to be the same height as the image
        ViewTreeObserver imageVTO = myView.glideImage.getViewTreeObserver();
        imageVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Get the width (match parent) in pixels
                int viewWidth = myView.glideImage.getWidth();
                int viewHeight = myView.glideImage.getHeight();
                int w = mainActivityInterface.getScreenshot().getWidth();
                int h = mainActivityInterface.getScreenshot().getHeight();
                float scaledX = (float) viewWidth / (float) w;
                float scaledY = (float) viewHeight / (float) h;
                float scale = Math.min(scaledX, scaledY);
                int imgW = (int) (w * scale);
                int imgH = (int) (h * scale);
                ViewGroup.LayoutParams layoutParams = myView.glideImage.getLayoutParams();
                layoutParams.width = imgW;
                layoutParams.height = imgH;
                myView.glideImage.setLayoutParams(layoutParams);
                GlideApp.with(myView.glideImage).
                        load(mainActivityInterface.getScreenshot()).
                        override(imgW, imgH).
                        into(myView.glideImage);

                // Get a scaled width and height of the bitmap being drawn
                ViewGroup.LayoutParams layoutParams2 = myView.drawNotes.getLayoutParams();
                layoutParams2.width = imgW;
                layoutParams2.height = imgH;
                myView.drawNotes.setLayoutParams(layoutParams2);
                // Set the original highlighter file if it exists
                mainActivityInterface.getDrawNotes().loadExistingHighlighter(requireContext(), mainActivityInterface, imgW, imgH);
                myView.glideImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        // Set up the bottomSheet
        bottomSheetBar();
    }

    private void bottomSheetBar() {
        bottomSheetBehavior = BottomSheetBehavior.from(myView.bottomSheet.bottomSheet);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setGestureInsetBottomIgnored(true);


        myView.bottomSheetTab.setOnClickListener(v -> {
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
                        myView.drawNotes.setEnabled(true);
                        myView.dimBackground.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_SETTLING:
                        checkUndos();
                        checkRedos();
                        myView.drawNotes.setEnabled(false);
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
        activeTool = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(), "drawingTool", "pen");
        drawingPenSize = mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(), "drawingPenSize", 20);
        drawingHighlighterSize = mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(), "drawingHighlighterSize", 20);
        drawingEraserSize = mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(), "drawingEraserSize", 20);
        drawingPenColor = mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(), "drawingPenColor", penRed);
        drawingHighlighterColor = mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(), "drawingHighlighterColor", highlighterYellow);
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
            myView.bottomSheet.sizeSlider.setHint(value + "");
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
                    mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(), prefName, size);
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
        if (requireContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientation = getString(R.string.portrait);
        } else {
            orientation = getString(R.string.landscape);
        }
        AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet("deleteHighlighter",
                getString(R.string.delete) + " " + getString(R.string.highlight) + " (" + orientation + ")", null,
                "highlighterEditFragment", this, mainActivityInterface.getSong());
        areYouSureBottomSheet.show(requireActivity().getSupportFragmentManager(), "are_you_sure");
    }

    public void doDelete(boolean confirmed) {
        if (confirmed) {
            // Set the original highlighter file if it exists
            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(requireContext(), mainActivityInterface, "Highlighter", "",
                    mainActivityInterface.getProcessSong().getHighlighterFilename(mainActivityInterface.getSong(), requireContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT));
            if (mainActivityInterface.getStorageAccess().deleteFile(requireContext(), uri)) {
                mainActivityInterface.getShowToast().doIt(getString(R.string.success));
            } else {
                mainActivityInterface.getShowToast().doIt(getString(R.string.not_saved));
            }
            mainActivityInterface.getDrawNotes().delete();
            checkUndos();
            checkRedos();
        } else {
            mainActivityInterface.getShowToast().doIt(getString(R.string.cancel));
        }
    }

    private void changeTool(String tool) {
        activeTool = tool;
        mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(), "drawingTool", tool);
        setActiveTool();
        setColors();
        setSizes();
        mainActivityInterface.getDrawNotes().postInvalidate();

    }

    private void changeColor(int colorPen, int colorHighlighter) {
        if (activeTool.equals("pen")) {
            drawingPenColor = colorPen;
            currentColor = colorPen;
            mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(), "drawingPenColor", colorPen);
        } else if (activeTool.equals("highlighter")) {
            drawingHighlighterColor = colorHighlighter;
            currentColor = colorHighlighter;
            mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(), "drawingHighlighterColor", colorHighlighter);
        }
        setColors();
        mainActivityInterface.getDrawNotes().setCurrentPaint(currentSize, currentColor);
    }

    Uri uri;
    Bitmap bitmap;

    private void saveFile() {
        // Get the bitmap of the drawNotes in a new thread
        int orientation = requireContext().getResources().getConfiguration().orientation;
        new Thread(() -> {
            String hname = mainActivityInterface.getProcessSong().getHighlighterFilename(mainActivityInterface.getSong(), orientation == Configuration.ORIENTATION_PORTRAIT);
            uri = mainActivityInterface.getStorageAccess().getUriForItem(getActivity(), mainActivityInterface,
                    "Highlighter", "", hname);
            // Check the uri exists for the outputstream to be valid
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(getActivity(), mainActivityInterface, false,uri, null,
                    "Highlighter", "", hname);

            requireActivity().runOnUiThread(() -> {
                mainActivityInterface.getDrawNotes().setDrawingCacheEnabled(true);
                try {
                    bitmap = mainActivityInterface.getDrawNotes().getDrawingCache();
                } catch (Exception e) {
                    Log.d(TAG, "Error extracting the drawing");
                } catch (OutOfMemoryError e) {
                    Log.d(TAG, "Out of memory trying to get the drawing");
                }
                if (uri != null && bitmap != null) {
                    Log.d(TAG, "newUri=" + uri);
                    Log.d(TAG, "bitmap=" + bitmap);
                    OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(requireContext(), uri);
                    Log.d(TAG, "outputStream=" + outputStream);
                    mainActivityInterface.getStorageAccess().writeImage(outputStream, bitmap);
                    mainActivityInterface.getShowToast().doIt(getString(R.string.success));
                } else {
                    mainActivityInterface.getShowToast().doIt(getString(R.string.error));
                }
            });
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

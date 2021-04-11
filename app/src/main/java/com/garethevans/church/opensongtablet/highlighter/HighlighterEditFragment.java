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
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.DrawNotes;
import com.garethevans.church.opensongtablet.databinding.SettingsHighlighterEditBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureDialogFragment;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.OutputStream;

public class HighlighterEditFragment extends Fragment {

    private final String TAG = "HighlighterEdit";

    private MainActivityInterface mainActivityInterface;
    private SettingsHighlighterEditBinding myView;
    private Drawable whiteCheck, blackCheck;
    private Preferences preferences;
    private StorageAccess storageAccess;
    private ProcessSong processSong;
    private ShowToast showToast;
    private Song song;
    private DrawNotes drawNotes;
    private int buttonActive, buttonInactive, drawingPenSize, drawingHighlighterSize,
            drawingEraserSize, drawingPenColor, drawingHighlighterColor, size = 0;
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
        myView = SettingsHighlighterEditBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(null,getString(R.string.edit) + " " + getString(R.string.highlight));

        // Set up helpers
        setupHelpers();

        // Set up views
        setupViews();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void setupHelpers() {
        song = mainActivityInterface.getSong();
        preferences = mainActivityInterface.getPreferences();
        storageAccess = mainActivityInterface.getStorageAccess();
        processSong = mainActivityInterface.getProcessSong();
        showToast = mainActivityInterface.getShowToast();
    }

    private void setupViews() {
        buttonActive = ContextCompat.getColor(requireContext(), R.color.colorSecondary);
        buttonInactive = ContextCompat.getColor(requireContext(), R.color.colorAltPrimary);
        whiteCheck = ContextCompat.getDrawable(requireContext(),R.drawable.ic_check_white_36dp);
        if (whiteCheck!=null) {
            whiteCheck.mutate();
        }
        blackCheck = ContextCompat.getDrawable(requireContext(),R.drawable.ic_check_white_36dp);
        if (blackCheck != null) {
            blackCheck.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        }

        drawNotes = myView.drawNotes;
        drawNotes.resetVars();

        GlideApp.with(this).load(mainActivityInterface.getScreenshot()).into(myView.glideImage);

        setToolPreferences();

        // Set the drawNotes to be the same height as the image
        ViewTreeObserver imageVTO = myView.glideImage.getViewTreeObserver();
        imageVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Get the width (match parent) in pixels
                int w = myView.glideImage.getMeasuredWidth();
                // Get a scaled width and height of the bitmap being drawn
                float scaled = (float)mainActivityInterface.getScreenshot().getWidth()/(float)w;
                int h = (int) ((float)mainActivityInterface.getScreenshot().getHeight()*scaled);
                ViewGroup.LayoutParams layoutParams = myView.drawNotes.getLayoutParams();
                layoutParams.width = w;
                layoutParams.height = h;
                myView.drawNotes.setLayoutParams(layoutParams);
                Log.d(TAG, "w=" + w);
                Log.d(TAG, "h=" + h);
                // Set the original highlighter file if it exists
                drawNotes.loadExistingHighlighter(requireContext(), preferences, storageAccess, processSong, song);
                myView.glideImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        // Set up the bottomSheet
        bottomSheetBar();
    }


    private void bottomSheetBar() {
        View bottomSheet = myView.bottomSheet;
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setGestureInsetBottomIgnored(true);

        // Set the peek height to match the drag icon
        ViewTreeObserver vto = myView.handleView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                          @Override
                                          public void onGlobalLayout() {
                                              size = myView.handleView.getHeight();
                                              Log.d(TAG,"size="+size);
                                              bottomSheetBehavior.setPeekHeight(size);
                                              bottomSheetBehavior.setFitToContents(false);
                                              int screenHeight = myView.parent.getMeasuredHeight();
                                              int bottomsheetHeight = bottomSheet.getMeasuredHeight();
                                              int offset = screenHeight-bottomsheetHeight;
                                              bottomSheetBehavior.setExpandedOffset(offset);
                                              bottomSheetBehavior.setHalfExpandedRatio(0.25f);
                                              myView.handleView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                          }
                                      });

        myView.handleView.setOnClickListener(v -> {
            if (drawNotes.isEnabled()) {
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
            public void onSlide (@NonNull View bottomSheet,float slideOffset) {
                myView.dimBackground.setVisibility(View.VISIBLE);
            }
        });

        checkUndos();
        checkRedos();
    }

    private void setToolPreferences() {
        activeTool = preferences.getMyPreferenceString(requireContext(),"drawingTool","pen");
        drawingPenSize = preferences.getMyPreferenceInt(requireContext(), "drawingPenSize",20);
        drawingHighlighterSize = preferences.getMyPreferenceInt(requireContext(),"drawingHighlighterSize",20);
        drawingEraserSize = preferences.getMyPreferenceInt(requireContext(), "drawingEraserSize",20);
        drawingPenColor = preferences.getMyPreferenceInt(requireContext(),"drawingPenColor",penRed);
        drawingHighlighterColor = preferences.getMyPreferenceInt(requireContext(),"drawingHighlighterColor",highlighterYellow);
        setActiveTool();
        setColors();
        setSizes();
    }

    private void setActiveTool() {
        Log.d(TAG,"activeTool="+activeTool);
        setToolButtonActive(myView.pencilFAB,activeTool.equals("pen"));
        setToolButtonActive(myView.highlighterFAB,activeTool.equals("highlighter"));
        setToolButtonActive(myView.eraserFAB,activeTool.equals("eraser"));
        setToolButtonActive(myView.undoFAB,false);
        setToolButtonActive(myView.redoFAB,false);
        setToolButtonActive(myView.deleteFAB,false);
        if (activeTool.equals("pen")) {
            currentColor = drawingPenSize;
            currentSize = drawingPenSize;
            drawNotes.setCurrentPaint(drawingPenSize,drawingPenColor);
            drawNotes.setErase(false);

        } else if (activeTool.equals("highlighter")) {
            currentColor = drawingHighlighterColor;
            currentSize = drawingHighlighterSize;
            drawNotes.setCurrentPaint(drawingHighlighterSize, drawingHighlighterColor);
            drawNotes.setErase(false);
        } else {
            currentColor = highlighterBlack;
            currentSize = drawingEraserSize;
            drawNotes.setCurrentPaint(drawingEraserSize, highlighterBlack);
            drawNotes.setErase(true);
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
        setColorActive(myView.colorBlack,currentColor==penBlack || currentColor==highlighterBlack);
        setColorActive(myView.colorWhite,currentColor==penWhite || currentColor==highlighterWhite);
        setColorActive(myView.colorYellow,currentColor==penYellow || currentColor==highlighterYellow);
        setColorActive(myView.colorRed,currentColor==penRed || currentColor==highlighterRed);
        setColorActive(myView.colorGreen,currentColor==penGreen || currentColor==highlighterGreen);
        setColorActive(myView.colorBlue,currentColor==penBlue || currentColor==highlighterBlue);
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
            myView.colorsLayout.setVisibility(View.INVISIBLE);
        } else {
            myView.colorsLayout.setVisibility(View.VISIBLE);
        }
    }
    private Drawable getBestCheck() {
        if (activeTool.equals("pen") && (drawingPenColor==penWhite || drawingPenColor==penYellow)) {
            return blackCheck;
        } else if (activeTool.equals("highlighter") && (drawingHighlighterColor==highlighterWhite ||
                drawingHighlighterColor==highlighterYellow)) {
            return blackCheck;
        } else {
            return whiteCheck;
        }
    }

    private void setSizes() {
        if (activeTool.equals("pen")) {
            currentSize = drawingPenSize;
        } else if (activeTool.equals("highlighter")) {
            currentSize = drawingHighlighterSize;
        } else {
            currentSize = drawingEraserSize;
        }
        sizeToProgress(currentSize);
    }

    private void sizeToProgress(int size) {
        // Min size = 1
        currentSize = size-1;
        myView.sizeSeekBar.setProgress(size-1);
        String text = size + " px";
        myView.sizeText.setText(text);
    }
    private void progressToSize(int progress) {
        String text = (progress+1) + " px";
        myView.sizeText.setText(text);
        currentSize = progress+1;
        drawNotes.setCurrentPaint(currentSize,currentColor);
    }

    private void setListeners() {
        myView.pencilFAB.setOnClickListener(v -> changeTool("pen"));
        myView.highlighterFAB.setOnClickListener(v -> changeTool("highlighter"));
        myView.eraserFAB.setOnClickListener(v -> changeTool("eraser"));
        myView.undoFAB.setOnClickListener(v -> undo());
        myView.redoFAB.setOnClickListener(v -> redo());
        myView.deleteFAB.setOnClickListener(v -> delete());

        myView.colorBlack.setOnClickListener(v -> changeColor(penBlack,highlighterBlack));
        myView.colorWhite.setOnClickListener(v -> changeColor(penWhite,highlighterWhite));
        myView.colorYellow.setOnClickListener(v -> changeColor(penYellow,highlighterYellow));
        myView.colorRed.setOnClickListener(v -> changeColor(penRed,highlighterRed));
        myView.colorGreen.setOnClickListener(v -> changeColor(penGreen,highlighterGreen));
        myView.colorBlue.setOnClickListener(v -> changeColor(penBlue,highlighterBlue));

        myView.dimBackground.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));
        myView.sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressToSize(progress);
                currentSize = progress+1;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String which = null;
                currentSize = myView.sizeSeekBar.getProgress()+1;
                switch (activeTool) {
                    case "pen":
                        which = "drawingPenSize";
                        drawingPenSize = currentSize;
                        break;
                    case "highlighter":
                        which = "drawingHighlighterSize";
                        drawingHighlighterSize = currentSize;
                        break;
                    case "eraser":
                        which = "drawingEraserSize";
                        drawingEraserSize = currentSize;
                        break;
                }
                progressToSize(myView.sizeSeekBar.getProgress());
                if (which!=null) {
                    preferences.setMyPreferenceInt(requireContext(), which, currentSize);
                }
                drawNotes.setCurrentPaint(currentSize,currentColor);
                drawNotes.postInvalidate();
            }
        });

        myView.saveButton.setOnClickListener(v -> saveFile());
    }

    private void undo() {
        // Check undo then check status
        drawNotes.undo();
        checkUndos();
    }
    private void redo() {
        // Check redo then check status
        drawNotes.redo();
        checkRedos();
    }
    private void checkUndos() {
        myView.undoFAB.setEnabled(drawNotes.getAllPaths().size()>0);
    }
    private void checkRedos() {
        myView.redoFAB.setEnabled(drawNotes.getUndoPaths().size()>0);
    }

    private void delete() {
        // Prompt the user for an 'Are you sure'
        String orientation;
        if (requireContext().getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT) {
            orientation = getString(R.string.portrait);
        } else {
            orientation = getString(R.string.landscape);
        }
        AreYouSureDialogFragment areYouSureDialogFragment = new AreYouSureDialogFragment("deleteHighlighter",
                getString(R.string.delete) + " " + getString(R.string.highlight) + " (" + orientation + ")",null,
                "highlighterEditFragment",this,song);
        areYouSureDialogFragment.show(requireActivity().getSupportFragmentManager(),"areyousure");
    }
    public void doDelete(boolean confirmed) {
        if (confirmed) {
            // Set the original highlighter file if it exists
            Uri uri = storageAccess.getUriForItem(requireContext(),preferences,"Highlighter","",
                    processSong.getHighlighterFilename(song,requireContext().getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT));
            if (storageAccess.deleteFile(requireContext(),uri)) {
                drawNotes.delete();
                checkUndos();
                checkRedos();
                showToast.doIt(requireContext(),getString(R.string.success));
            } else {
                showToast.doIt(requireContext(),getString(R.string.error));
            }
        } else {
            showToast.doIt(requireContext(),getString(R.string.cancel));
        }
    }
    private void changeTool(String tool) {
        activeTool = tool;
        preferences.setMyPreferenceString(requireContext(), "drawingTool", tool);
        setActiveTool();
        setColors();
        setSizes();
        drawNotes.postInvalidate();

    }
    private void changeColor(int colorPen, int colorHighlighter) {
        if (activeTool.equals("pen")) {
            drawingPenColor = colorPen;
            currentColor = colorPen;
            preferences.setMyPreferenceInt(requireContext(), "drawingPenColor",colorPen);
        } else if (activeTool.equals("highlighter")) {
            drawingHighlighterColor = colorHighlighter;
            currentColor = colorHighlighter;
            preferences.setMyPreferenceInt(requireContext(), "drawingHighlighterColor",colorHighlighter);
        }
        setColors();
        drawNotes.setCurrentPaint(currentSize,currentColor);
    }

    Uri uri;
    Bitmap bitmap;
    private void saveFile() {
        // Get the bitmap of the drawNotes in a new thread
        int orientation = requireContext().getResources().getConfiguration().orientation;
        new Thread(() -> {
            String hname = processSong.getHighlighterFilename(song,orientation== Configuration.ORIENTATION_PORTRAIT);
            uri = storageAccess.getUriForItem(getActivity(), preferences,
                    "Highlighter", "", hname);
            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, uri, null,
                    "Highlighter", "", hname);

            requireActivity().runOnUiThread(() -> {
                drawNotes.setDrawingCacheEnabled(true);
                try {
                    bitmap = drawNotes.getDrawingCache();
                } catch (Exception e) {
                    Log.d(TAG,"Error extracting the drawing");
                } catch (OutOfMemoryError e) {
                    Log.d(TAG,"Out of memory trying to get the drawing");
                }
                if (uri!=null && bitmap!=null) {
                    Log.d(TAG,"newUri="+uri);
                    Log.d(TAG,"bitmap="+bitmap);
                    OutputStream outputStream = storageAccess.getOutputStream(requireContext(),uri);
                    Log.d(TAG,"outputStream="+outputStream);
                    storageAccess.writeImage(outputStream, bitmap);
                    showToast.doIt(requireContext(),getString(R.string.success));
                } else {
                    showToast.doIt(requireContext(),getString(R.string.error));
                }
            });
        }).start();
    }
}

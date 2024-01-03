package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.EditSongLyricsBinding;
import com.garethevans.church.opensongtablet.interfaces.EditSongFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

// This fragment purely deals with the lyrics/chords

public class EditSongFragmentLyrics extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private EditSongFragmentInterface editSongFragmentInterface;
    private EditSongLyricsBinding myView;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "EditSongFragmentLyrics";
    private float editTextSize = 11;
    private int cursorPos=0;
    private boolean addUndoStep = true;
    private String success_string="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        editSongFragmentInterface = (EditSongFragmentInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            try {
                prepareStrings();
                setupValues();

                // The stuff that needs to be on the UI
                mainActivityInterface.getMainHandler().post(() -> {
                    // Add listeners
                    setupListeners();

                    myView.lyrics.clearFocus();
                });
            } catch (Exception e) {
                e.printStackTrace();
                mainActivityInterface.getStorageAccess().updateFileActivityLog(e.toString());
            }
        });

        try {
            myView.lyrics.clearFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = EditSongLyricsBinding.inflate(inflater, container, false);

        return myView.getRoot();
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getActivity()!=null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            success_string = getString(R.string.success);
        }
    }
    private void setupValues() {
        if (mainActivityInterface.getTempSong()==null) {
            mainActivityInterface.setTempSong(mainActivityInterface.getSong());
        }
        boolean editAsChordPro = mainActivityInterface.getPreferences().getMyPreferenceBoolean("editAsChordPro",false);
        mainActivityInterface.getTempSong().setEditingAsChoPro(editAsChordPro);
        Log.d(TAG,"editAsChordPro:"+editAsChordPro);
        if (mainActivityInterface.getTempSong()!=null && !mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            mainActivityInterface.getTranspose().checkChordFormat(mainActivityInterface.getTempSong());
        } else if (mainActivityInterface.getTempSong()!=null) {
            // Get the lyrics as choPro
            String choProLyrics = mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(mainActivityInterface.getTempSong().getLyrics());
            // Now detect the chord format from the lyrics
            mainActivityInterface.getTranspose().checkChordFormat(mainActivityInterface.getTempSong());
            // Now set the lyrics back as chordpro
            mainActivityInterface.getTempSong().setLyrics(choProLyrics);
        }


        if (mainActivityInterface.getSong().getFiletype()==null) {
            mainActivityInterface.getStorageAccess().isIMGorPDF(mainActivityInterface.getSong());
        }

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP &&
                mainActivityInterface.getSong() != null &&
                mainActivityInterface.getSong().getFiletype() != null &&
                (mainActivityInterface.getSong().getFiletype().equals("PDF") ||
                mainActivityInterface.getSong().getFiletype().equals("IMG"))) {
            // Show the OCR button
            myView.ocr.post(() -> myView.ocr.setVisibility(View.VISIBLE));
            myView.imageEdit.post(() -> myView.imageEdit.setVisibility(View.VISIBLE));
        } else {
            myView.ocr.post(() -> myView.ocr.setVisibility(View.GONE));
            myView.imageEdit.post(() -> myView.imageEdit.setVisibility(View.GONE));
        }

        int lines = Math.max(20,mainActivityInterface.getTempSong().getLyrics().split("\n").length+1);

        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.lyrics);
        editTextSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("editTextSize",14);
        myView.lyrics.post(() -> {
            myView.lyrics.setTextSize(editTextSize);
            try {
                mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.lyrics, lines);
            } catch (Exception e) {
                mainActivityInterface.getStorageAccess().updateFileActivityLog(e.toString());
                e.printStackTrace();
            }
            myView.lyrics.setText(mainActivityInterface.getTempSong().getLyrics());
        });

        validUndoRedo(mainActivityInterface.getTempSong().getLyricsUndosPos());
    }

    private void setupListeners() {
        myView.lyrics.setOnFocusChangeListener((view, b) -> {
            if (b) {
                // Get the text position and 100ms later set this again
                // Hopefully deals with soft keyboard hiding cursor position
                int cursorPos = myView.lyrics.getSelectionStart();
                myView.lyrics.postDelayed(() -> {
                    myView.lyrics.setSelection(cursorPos);
                    Log.d(TAG,"cursor set to:"+cursorPos);

                    Layout layout = myView.lyrics.getLayout();
                    int line = layout.getLineForOffset(cursorPos);
                    int baseline = layout.getLineBaseline(line);
                    int ascent = layout.getLineAscent(line);

                    Log.d(TAG,"line:"+line+"  baseline:"+baseline+"  ascent:"+ascent);

                    int[] location = new int[2];
                    myView.lyrics.getLocationOnScreen(location);

                    Point point = new Point();
                    point.x = (int) layout.getPrimaryHorizontal(cursorPos);
                    point.y = baseline + ascent - location[1] - myView.lyricsScrollView.getScrollY();

                    myView.lyrics.scrollTo(0,point.y);
                },100);

            }
            //mainActivityInterface.enableSwipe("edit",!b);
        });

        myView.lyrics.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mainActivityInterface.getTempSong().setLyrics(editable.toString());
                // If we are not undoing/redoing add the changes to the undo list
                if (addUndoStep) {
                    int lyricsUndosPos = mainActivityInterface.getTempSong().getLyricsUndosPos() + 1;
                    mainActivityInterface.getTempSong().setLyricsUndos(lyricsUndosPos, editable.toString());
                    mainActivityInterface.getTempSong().setLyricsUndosPos(lyricsUndosPos);
                    mainActivityInterface.getTempSong().setLyricsUndoCursorPos(lyricsUndosPos, myView.lyrics.getSelectionStart());
                    // Enable/disable the undo and redo button
                    validUndoRedo(lyricsUndosPos);
                } else {
                    // We were undoing/redoing, so we didn't do the above.  Now turn this off
                    addUndoStep = true;
                }
                int lines = Math.max(20, mainActivityInterface.getTempSong().getLyrics().split("\n").length);
                mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.lyrics, lines);
            }
        });

        myView.ocr.setOnClickListener(v -> {

            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                mainActivityInterface.getOCR().getTextFromPDF(
                        mainActivityInterface.getSong().getFolder(), mainActivityInterface.getSong().getFilename(),false);
            } else if (mainActivityInterface.getSong().getFiletype().equals("IMG")) {
                mainActivityInterface.getOCR().getTextFromImageFile(
                        mainActivityInterface.getSong().getFolder(),
                        mainActivityInterface.getSong().getFilename());
            }
            Log.d(TAG,"filetype:"+mainActivityInterface.getSong().getFiletype());
            Log.d(TAG,"filename:"+mainActivityInterface.getSong().getFilename());
            mainActivityInterface.navHome();
        });
        myView.imageEdit.setOnClickListener(v -> {
            ImageAdjustBottomSheet imageAdjustBottomSheet = new ImageAdjustBottomSheet(mainActivityInterface.getTempSong());
            imageAdjustBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"ImageAdjustBottomSheet");
        });

        myView.settingsButton.setOnClickListener(v -> {
            cursorPos = myView.lyrics.getSelectionStart();
            myView.lyrics.clearFocus();
            LyricsOptionsBottomSheet lyricsOptionsBottomSheet = new LyricsOptionsBottomSheet(this);
            lyricsOptionsBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"LyricsBottomSheet");
        });

        myView.undoButton.setOnClickListener(v -> undoLyrics());
        myView.redoButton.setOnClickListener(v -> redoLyrics());

        // Scroll listener
        myView.lyricsScrollView.setExtendedFabToAnimate(editSongFragmentInterface.getSaveButton());
        myView.lyricsScrollView.setFabToAnimate(myView.undoButton);
        myView.lyricsScrollView.setFab2ToAnimate(myView.redoButton);
        myView.lyricsScrollView.setFab3ToAnimate(myView.settingsButton);

        // Resize the bottom padding to the soft keyboard height or half the screen height for the soft keyboard (workaround)
        mainActivityInterface.getWindowFlags().adjustViewPadding(mainActivityInterface,myView.resizeForKeyboardLayout);
    }

    private void manualScrollTo () {

        myView.lyrics.postDelayed(() -> {
            int cursorStart = myView.lyrics.getSelectionStart();
            Log.d(TAG, "onClicked().  cursorStart:" + cursorStart);
            if (cursorStart > 0) {
                // Get the text between the start and this position
                String substring = myView.lyrics.getText().toString().substring(0, cursorStart);
                // Get the number of lines to this position
                int linesDown = substring.split("\n").length;
                int totalLines = myView.lyrics.getMinLines();
                float proportionOfView = (float) linesDown / (float) totalLines;
                int heightOfView = myView.lyrics.getMeasuredHeight();
                int scrollYneeded = (int) (heightOfView * proportionOfView);
                if (heightOfView != 0) {
                    int screenHeight = myView.parentView.getMeasuredHeight();
                    screenHeight = Math.min(400,screenHeight/4);
                    // Because selection is >0, the keyboard is open.
                    // Make sure the view has scroll up by this proportion
                    myView.lyricsScrollView.scrollTo(0, scrollYneeded - screenHeight);
                }
            }
        }, 50);
    }

    private void undoLyrics() {
        // If we can go back, undo the changes
        int lyricsUndosPos = mainActivityInterface.getTempSong().getLyricsUndosPos();
        if (lyricsUndosPos>0) {
            lyricsUndosPos = lyricsUndosPos - 1;
            mainActivityInterface.getTempSong().setLyricsUndosPos(lyricsUndosPos);
            addUndoStep = false;
            myView.lyrics.setText(mainActivityInterface.getTempSong().getLyricsUndos().get(lyricsUndosPos));
            int cursorPos = mainActivityInterface.getTempSong().getLyricsUndoCursorPos().get(lyricsUndosPos,0);
            if (cursorPos>=0 && cursorPos<myView.lyrics.getText().length()) {
                myView.lyrics.setSelection(cursorPos);
            }
        }
        validUndoRedo(lyricsUndosPos);
    }
    private void redoLyrics() {
        // If we can go forward, redo the changes
        int lyricsUndosPos = mainActivityInterface.getTempSong().getLyricsUndosPos();
        if (lyricsUndosPos<mainActivityInterface.getTempSong().getLyricsUndos().size()-1) {
            lyricsUndosPos = lyricsUndosPos + 1;
            addUndoStep = false;
            mainActivityInterface.getTempSong().setLyricsUndosPos(lyricsUndosPos);
            myView.lyrics.setText(mainActivityInterface.getTempSong().getLyricsUndos().get(lyricsUndosPos));
            int cursorPos = mainActivityInterface.getTempSong().getLyricsUndoCursorPos().get(lyricsUndosPos,0);
            if (cursorPos>=0 && cursorPos<myView.lyrics.getText().length()) {
                myView.lyrics.setSelection(cursorPos);
            }
        }
        validUndoRedo(lyricsUndosPos);
    }
    private void validUndoRedo(int currentPosition) {
        // Enable/disable the undo button
        boolean undoValid = currentPosition>0;
        myView.undoButton.post(() -> myView.undoButton.setEnabled(undoValid));

        if (undoValid) {
            myView.undoButton.post(() -> myView.undoButton.setVisibility(View.VISIBLE));
        } else {
            myView.undoButton.post(() -> myView.undoButton.setVisibility(View.INVISIBLE));
        }

        // Enable/disable the redo button
        boolean redoValid = currentPosition<mainActivityInterface.getTempSong().getLyricsUndos().size()-1;
        myView.redoButton.post(() ->myView.redoButton.setEnabled(redoValid));
        if (redoValid) {
            myView.redoButton.post(() -> myView.redoButton.setVisibility(View.VISIBLE));
        } else {
            myView.redoButton.post(() -> myView.redoButton.setVisibility(View.INVISIBLE));
        }
    }

    // The stuff below is called from the LyricsOptionsBottomSheet
    public float getEditTextSize() {
        return editTextSize;
    }
    public void setEditTextSize(float editTextSize) {
        this.editTextSize = editTextSize;
        myView.lyrics.setTextSize(editTextSize);
    }
    public void insertSection(String bitToAdd, int moveCursorBy) {
        try {
            // This comes from the bottom sheet
            // Try to get the current text position
            String text = myView.lyrics.getText().toString();
            if (text.length() > cursorPos && cursorPos != -1) {
                text = text.substring(0, cursorPos) + bitToAdd + "\n" + text.substring(cursorPos);
                myView.lyrics.setText(text);
            }
            // Setting the position should open the keyboard
            myView.lyrics.requestFocus();
            myView.lyrics.setSelection(cursorPos + moveCursorBy);
            // Also do this in 1 second time to allow for keyboard opening
            myView.lyrics.postDelayed(() -> myView.lyrics.setSelection(cursorPos + moveCursorBy), 1000);

            mainActivityInterface.getWindowFlags().showKeyboard();
            manualScrollTo();
        } catch (Exception e) {
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+": \n"+e);
        }
    }

    public void transpose(String direction) {
        // If nothing is selected, we select the entire song
        int selectedStart = myView.lyrics.getSelectionStart();
        int selectedEnd = myView.lyrics.getSelectionEnd();
        String selectedText;
        String key = null;
        if (selectedStart >= 0 && selectedEnd > selectedStart) {
            selectedText = myView.lyrics.getText().toString().substring(selectedStart,selectedEnd);
        } else {
            selectedText = myView.lyrics.getText().toString();
        }
        if (selectedText.equals(myView.lyrics.getText().toString())) {
            // We are transposing everything, so we'll pass the key to update
            key = mainActivityInterface.getTempSong().getKey();
        }

        // Get a temp variable as we might need to tweak first
        // This keeps the original as we will replace that later
        String processedSelectedText = selectedText;

        // If we are editing as choPro, we need to convert to OpenSong first
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            processedSelectedText = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(processedSelectedText);
        }

        // Transpose the OpenSong lyrics using a temp song that only has the important stuff
        Song transposeSong = new Song();
        transposeSong.setLyrics(processedSelectedText);
        transposeSong.setDetectedChordFormat(mainActivityInterface.getTempSong().getDetectedChordFormat());
        transposeSong.setDesiredChordFormat(mainActivityInterface.getTempSong().getDesiredChordFormat());
        transposeSong.setKey(key);
        transposeSong = mainActivityInterface.getTranspose().doTranspose(transposeSong,
                direction,1,1,1);

        processedSelectedText = transposeSong.getLyrics();

        // If we were using ChoPro, convert back
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            processedSelectedText =  mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(
                    processedSelectedText);
        }

        // Now get the non selected text
        String textBeforeSelection = "", textAfterSelection = "";
        if (selectedStart>=0 && selectedEnd>selectedStart) {
            textBeforeSelection = myView.lyrics.getText().toString().substring(0,selectedStart);
        }
        if (selectedStart >= 0 && selectedEnd > selectedStart) {
            textAfterSelection = myView.lyrics.getText().toString().substring(selectedEnd,myView.lyrics.getText().length());
            selectedEnd = selectedStart + processedSelectedText.length();
        }

        String newText = textBeforeSelection + processedSelectedText + textAfterSelection;
        myView.lyrics.setText(newText);
        mainActivityInterface.getTempSong().setLyrics(newText);
        if (key!=null) {
            mainActivityInterface.getTempSong().setKey(transposeSong.getKey());
        }

        // Highlight the new text
        if (selectedStart>=0) {
            myView.lyrics.setSelection(selectedStart,selectedEnd);
        }

        addUndoStep = true;
    }

    public void copyChords() {
        // Break the song into sections for copying, not quite the same as used in ProcessSong
        String allLyrics = myView.lyrics.getText().toString();

        // If we are editing as choPro, we need to convert to OpenSong first
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            allLyrics = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(allLyrics);
        }

        String splitIdentifier = "___SPLITHERE___";

        // Add the splitIdentifier to section headers
        allLyrics = allLyrics.replace(" \n[",splitIdentifier+"[");
        allLyrics = allLyrics.replace("\n[",splitIdentifier+"[");

        // Replace all double line breaks with split identifier
        allLyrics = allLyrics.replace("\n \n",splitIdentifier);
        allLyrics = allLyrics.replace("\n\n",splitIdentifier);

        // Now split the lyrics into sections
        String[] sections = allLyrics.split(splitIdentifier);

        if (sections.length>0) {
            // Now send to another bottom sheet for user choice
            LyricsChordCopyBottomSheet lyricsChordCopyBottomSheet = new LyricsChordCopyBottomSheet(this, sections);
            lyricsChordCopyBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "LyricsChordCopyBottomSheet");
        }
        Log.d(TAG,"allLyrics="+allLyrics);
    }

    public void doCopyChords(String oldText, String newText) {
        String textToAdjust = myView.lyrics.getText().toString();
        // If we are editing as choPro, we need to get as OpenSong format
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            textToAdjust = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(textToAdjust);
        }

        // Do the changes
        textToAdjust = textToAdjust.replace(oldText.trim(),newText.trim());

        // If we are editing as choPro, we need to convert back
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            textToAdjust = mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(textToAdjust);
        }

        myView.lyrics.setText(textToAdjust);
    }

    public void convertToOpenSong() {
        // Get the lyrics converted
        String lyrics = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(mainActivityInterface.getTempSong().getLyrics());
        // Set them into the edit box - don't include an undo/redo step, so pretend we're carrying this out
        addUndoStep = false;
        myView.lyrics.setText(lyrics);
    }
    public void convertToChoPro() {
        // Get the lyrics converted
        String lyrics = mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(mainActivityInterface.getTempSong().getLyrics());
        // Set them into the edit box - don't include an undo/redo step, so pretend we're carrying this out
        addUndoStep = false;
        myView.lyrics.setText(lyrics);
    }

    public void autoFix() {
        String lyrics;
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            lyrics = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(mainActivityInterface.getTempSong().getLyrics());
        } else {
            lyrics = mainActivityInterface.getTempSong().getLyrics();
        }

        // Use the text conversion to fix
        lyrics = mainActivityInterface.getConvertTextSong().convertText(lyrics);

        // Put back (includes undo step)
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            lyrics = mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(lyrics);
        }
        myView.lyrics.setText(lyrics);
        mainActivityInterface.getShowToast().doIt(success_string);
    }

}

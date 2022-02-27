package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.EditSongLyricsBinding;
import com.garethevans.church.opensongtablet.interfaces.EditSongFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

// This fragment purely deals with the lyrics/chords

public class EditSongFragmentLyrics extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private EditSongFragmentInterface editSongFragmentInterface;
    private EditSongLyricsBinding myView;
    private final String TAG = "EditSongFragmentLyrics";
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private float editTextSize = 11;
    private int colorOn, colorOff;
    private boolean addUndoStep = true, asChordPro = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        editSongFragmentInterface = (EditSongFragmentInterface) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = requireActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = EditSongLyricsBinding.inflate(inflater, container, false);

        // Put the values in
        setupValues();

        // Add listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupValues() {
        if ((Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP &&
                mainActivityInterface.getSong().getFiletype().equals("PDF")) ||
                mainActivityInterface.getSong().getFiletype().equals("IMG")) {
            // Show the OCR button
            myView.ocr.setVisibility(View.VISIBLE);
        } else {
            myView.ocr.setVisibility(View.GONE);
        }

        // The button colors
        colorOn = getResources().getColor(R.color.colorSecondary);
        colorOff = getResources().getColor(R.color.colorAltPrimary);

        // Set up the bottomSheet
        bottomSheetBar();

        myView.lyrics.clearFocus();
        myView.lyrics.requestFocus();

        myView.bottomSheetLayout.insertSection.setHint("[V]="+getString(R.string.verse) +
                " , [V1]="+getString(R.string.verse)+" 1, [C]="+getString(R.string.chorus) +
                ", [B]="+getString(R.string.bridge)+", [P]="+getString(R.string.prechorus) +
                ", [...]="+getString(R.string.custom));

        myView.lyrics.setText(mainActivityInterface.getTempSong().getLyrics());
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.lyrics);
        editTextSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("editTextSize",14);
        checkTextSize(0);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.lyrics,20);

        validUndoRedo(mainActivityInterface.getTempSong().getLyricsUndosPos());
    }

    private void bottomSheetBar() {
        bottomSheetBehavior = BottomSheetBehavior.from(myView.bottomSheetLayout.bottomSheet);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setGestureInsetBottomIgnored(true);
        myView.bottomSheetTab.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                myView.lyrics.setEnabled(false);
                myView.dimBackground.setVisibility(View.VISIBLE);
                if (!mainActivityInterface.getTempSong().getEditingAsChoPro()) {
                    mainActivityInterface.getTranspose().checkChordFormat(mainActivityInterface.getTempSong());
                } else {
                    String choProLyrics = mainActivityInterface.getTempSong().getLyrics();
                    // Convert to OpenSong to detect the chord format
                    String openSongLyrics = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(choProLyrics);
                    // Set the lyrics back to the temp song
                    mainActivityInterface.getTempSong().setLyrics(openSongLyrics);
                    // Now detect the chord format
                    mainActivityInterface.getTranspose().checkChordFormat(mainActivityInterface.getTempSong());
                    // Now set the lyrics back as chordpro
                    mainActivityInterface.getTempSong().setLyrics(choProLyrics);
                }
                setTransposeDetectedFormat();
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                myView.lyrics.setEnabled(true);
                myView.dimBackground.setVisibility(View.GONE);
            }
        });

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                        myView.lyrics.setEnabled(true);
                        myView.dimBackground.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        myView.lyrics.setEnabled(false);
                        myView.dimBackground.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_SETTLING:
                        myView.lyrics.setEnabled(false);
                        break;
                }
            }

            @Override
            public void onSlide (@NonNull View bottomSheet,float slideOffset) {
                //myView.dimBackground.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupListeners() {
        myView.lyrics.setOnFocusChangeListener((view, b) -> {
            Log.d(TAG,"b="+b);
            mainActivityInterface.enableSwipe("edit",!b);
        });

        myView.lyrics.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.lyrics,20);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mainActivityInterface.getTempSong().setLyrics(editable.toString());
                // If we are not undoing/redoing add the changes to the undo list
                if (addUndoStep) {
                    int lyricsUndosPos = mainActivityInterface.getTempSong().getLyricsUndosPos() + 1;
                    mainActivityInterface.getTempSong().setLyricsUndos(lyricsUndosPos, editable.toString());
                    mainActivityInterface.getTempSong().setLyricsUndosPos(lyricsUndosPos);
                    // Enable/disable the undo and redo button
                    validUndoRedo(lyricsUndosPos);
                } else {
                    // We were undoing/redoing, so we didn't do the above.  Now turn this off
                    addUndoStep = true;
                }
            }
        });

        myView.ocr.setOnClickListener(v -> {
            mainActivityInterface.navHome();
            if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
                mainActivityInterface.getOCR().getTextFromPDF(
                        mainActivityInterface.getSong().getFolder(), mainActivityInterface.getSong().getFilename());
            } else if (mainActivityInterface.getSong().getFiletype().equals("IMG")) {
                mainActivityInterface.getOCR().getTextFromImageFile(
                        mainActivityInterface.getSong().getFolder(),
                        mainActivityInterface.getSong().getFilename());
            }
        });
        myView.bottomSheetLayout.textSizeDown.setOnClickListener(v -> checkTextSize(-1));
        myView.bottomSheetLayout.textSizeUp.setOnClickListener(v -> checkTextSize(+1));
        myView.bottomSheetLayout.insertSection.setOnClickListener(v -> insertSection());

        myView.bottomSheetLayout.convertToOpenSong.setOnClickListener(v -> convertToOpenSong());
        myView.bottomSheetLayout.convertToChoPro.setOnClickListener(v -> convertToChoPro());

        myView.undoButton.setOnClickListener(v -> undoLyrics());
        myView.redoButton.setOnClickListener(v -> redoLyrics());

        myView.bottomSheetLayout.transposeDown.setOnClickListener(v -> transpose("-1"));
        myView.bottomSheetLayout.transposeUp.setOnClickListener(v -> transpose("+1"));

        myView.bottomSheetLayout.autoFix.setOnClickListener(v -> autoFix());

        myView.bottomSheetLayout.formatHelp.setOnClickListener(v -> mainActivityInterface.openDocument("songformat",null));

        // Scroll listener
        myView.nestedScrollView.setExtendedFabToAnimate(editSongFragmentInterface.getSaveButton());
        myView.nestedScrollView.setFabToAnimate(myView.undoButton);
        myView.nestedScrollView.setFab2ToAnimate(myView.redoButton);
    }

    private void checkTextSize(int change) {
        // Adjust it
        editTextSize = editTextSize + change;

        // Max is 24
        if (editTextSize>=24) {
            editTextSize = 24;
            myView.bottomSheetLayout.textSizeUp.setEnabled(false);
            myView.bottomSheetLayout.textSizeUp.setBackgroundColor(colorOff);
        } else {
            myView.bottomSheetLayout.textSizeUp.setEnabled(true);
            myView.bottomSheetLayout.textSizeUp.setBackgroundColor(colorOn);
        }

        // Min is 8
        if (editTextSize<=8) {
            editTextSize = 8;
            myView.bottomSheetLayout.textSizeDown.setEnabled(false);
            myView.bottomSheetLayout.textSizeDown.setBackgroundColor(colorOff);
        } else {
            myView.bottomSheetLayout.textSizeDown.setEnabled(true);
            myView.bottomSheetLayout.textSizeDown.setBackgroundColor(colorOn);
        }

        // Set the text size
        myView.lyrics.setTextSize(editTextSize);

        // Save this to the user preferences
        mainActivityInterface.getPreferences().setMyPreferenceFloat("editTextSize",editTextSize);
    }

    private void insertSection() {
        // Try to get the current text position
        int pos = myView.lyrics.getSelectionStart();
        if (pos<0) {
            pos = 0;
        }
        String text = myView.lyrics.getText().toString();
        text = text.substring(0,pos) + "[]" + text.substring(pos);
        myView.lyrics.setText(text);
        myView.lyrics.setSelection(pos+1);
    }
    public void changelyricFormat() {
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("editAsChordPro",false)) {
            myView.lyrics.setText(mainActivityInterface.getConvertChoPro().
                    fromOpenSongToChordPro(mainActivityInterface.getTempSong().getLyrics()));
        } else {
            myView.lyrics.setText(mainActivityInterface.getConvertChoPro().
                    fromChordProToOpenSong(mainActivityInterface.getTempSong().getLyrics()));
        }
    }
    private void undoLyrics() {
        // If we can go back, undo the changes
        int lyricsUndosPos = mainActivityInterface.getTempSong().getLyricsUndosPos();
        Log.d(TAG,"undo: lyricsUndosPos: "+lyricsUndosPos);
        if (lyricsUndosPos>0) {
            lyricsUndosPos = lyricsUndosPos - 1;
            Log.d(TAG,"undo: lyricsUndosPos: "+lyricsUndosPos);
            mainActivityInterface.getTempSong().setLyricsUndosPos(lyricsUndosPos);
            addUndoStep = false;
            myView.lyrics.setText(mainActivityInterface.getTempSong().getLyricsUndos().get(lyricsUndosPos));
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
        }
        validUndoRedo(lyricsUndosPos);
    }

    private void transpose(String direction) {
        // If we are editing as choPro, we need to convert to OpenSong first
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            String choProLyrics = mainActivityInterface.getTempSong().getLyrics();
            // Convert the lyrics to OpenSong
            mainActivityInterface.getTempSong().setLyrics(mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(choProLyrics));
        }

        // Transpose the OpenSong lyrics
        mainActivityInterface.getTranspose().doTranspose(mainActivityInterface.getTempSong(),
                direction,1,1,1);

        // If we were using ChoPro, convert back
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            String choProLyrics =  mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(
                    mainActivityInterface.getTempSong().getLyrics());
            mainActivityInterface.getTempSong().setLyrics(choProLyrics);
        }
        // Don't add this as an undo/redo as it breaks the key
        addUndoStep = false;
        myView.lyrics.setText(mainActivityInterface.getTempSong().getLyrics());
    }

    private void convertToOpenSong() {
        // Only do this if we are editing as ChordPro
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            mainActivityInterface.getTempSong().setEditingAsChoPro(false);
            myView.bottomSheetLayout.convertToChoPro.setBackgroundTintList(ColorStateList.valueOf(colorOff));
            myView.bottomSheetLayout.convertToOpenSong.setBackgroundTintList(ColorStateList.valueOf(colorOn));
            // Get the lyrics converted
            String lyrics = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(mainActivityInterface.getTempSong().getLyrics());
            // Set them into the edit box - don't include an undo/redo step, so pretend we're carrying this out
            addUndoStep = false;
            myView.lyrics.setText(lyrics);
        }
    }
    private void convertToChoPro() {
        // Only do this if we aren't editing as ChordPro
        if (!mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            mainActivityInterface.getTempSong().setEditingAsChoPro(true);
            myView.bottomSheetLayout.convertToChoPro.setBackgroundTintList(ColorStateList.valueOf(colorOn));
            myView.bottomSheetLayout.convertToOpenSong.setBackgroundTintList(ColorStateList.valueOf(colorOff));
            // Get the lyrics converted
            String lyrics = mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(mainActivityInterface.getTempSong().getLyrics());
            // Set them into the edit box - don't include an undo/redo step, so pretend we're carrying this out
            addUndoStep = false;
            myView.lyrics.setText(lyrics);
        }
    }
    private void validUndoRedo(int currentPosition) {
        // Enable/disable the undo button
        myView.undoButton.setEnabled(currentPosition>0);

        // Enable/disable the redo button
        myView.redoButton.setEnabled(currentPosition<mainActivityInterface.getTempSong().getLyricsUndos().size()-1);
    }

    private void setTransposeDetectedFormat() {
        String text = getString(R.string.chordformat_detected) + ": ";
        switch (mainActivityInterface.getTempSong().getDetectedChordFormat()) {
            case 1:
                myView.bottomSheetLayout.transposeText.setHint(text + getString(R.string.chordformat_1));
                break;
            case 2:
                myView.bottomSheetLayout.transposeText.setHint(text + getString(R.string.chordformat_2));
                break;
            case 3:
                myView.bottomSheetLayout.transposeText.setHint(text + getString(R.string.chordformat_3));
                break;
            case 4:
                myView.bottomSheetLayout.transposeText.setHint(text + getString(R.string.chordformat_4));
                break;
            case 5:
                myView.bottomSheetLayout.transposeText.setHint(text + getString(R.string.chordformat_5));
                break;
            case 6:
                myView.bottomSheetLayout.transposeText.setHint(text + getString(R.string.chordformat_6));
                break;
        }
    }

    private void autoFix() {
        String lyrics;
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            lyrics = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(mainActivityInterface.getTempSong().getLyrics());
        } else {
            lyrics = mainActivityInterface.getTempSong().getLyrics();
        }

        Log.d(TAG,"original: "+lyrics);
        // Use the text conversion to fix
        lyrics = mainActivityInterface.getConvertTextSong().convertText(lyrics);
        Log.d(TAG,"texttidy: "+lyrics);

        // Put back (includes undo step)
        if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
            lyrics = mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(lyrics);
        }
        myView.lyrics.setText(lyrics);
        mainActivityInterface.getShowToast().doIt(getString(R.string.success));
    }


    // The stuff for the bottom sheet

    /*



        activeColor = requireContext().getResources().getColor(R.color.colorSecondary);
        inactiveColor = requireContext().getResources().getColor(R.color.colorPrimary);


    if (editAsChoPro) {
            // Do the conversion
            // Initially set this to false so it triggers
            editAsChoPro = false;
            dealWithEditMode(true);
            setButtonOn(chordProFormat,true);
            setButtonOn(openSongFormat,false);
        } else {
            setButtonOn(chordProFormat,false);
            setButtonOn(openSongFormat,true);
        }


    private void setButtonOn(MaterialButton button, boolean on) {
        if (on) {
            button.setBackgroundTintList(ColorStateList.valueOf(activeColor));
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(inactiveColor));
        }
    }
     */

}

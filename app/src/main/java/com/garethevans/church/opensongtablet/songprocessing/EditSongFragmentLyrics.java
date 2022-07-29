package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private int colorOn, colorOff, cursorPos=0;
    private boolean addUndoStep = true;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        editSongFragmentInterface = (EditSongFragmentInterface) context;
        // Hide the keyboard
        //mainActivityInterface.getSoftKeyboard().hideKeyboard(requireActivity());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the keyboard
        //mainActivityInterface.getSoftKeyboard().hideKeyboard(requireActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hide the keyboard
        //mainActivityInterface.getSoftKeyboard().hideKeyboard(requireActivity());
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


        if ((Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP &&
                mainActivityInterface.getSong().getFiletype().equals("PDF")) ||
                mainActivityInterface.getSong().getFiletype().equals("IMG")) {
            // Show the OCR button
            myView.ocr.setVisibility(View.VISIBLE);
        } else {
            myView.ocr.setVisibility(View.GONE);
        }

        //myView.lyrics.clearFocus();
        //myView.lyrics.requestFocus();

        myView.lyrics.setText(mainActivityInterface.getTempSong().getLyrics());
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.lyrics);
        editTextSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("editTextSize",14);
        myView.lyrics.setTextSize(editTextSize);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.lyrics,20);

        validUndoRedo(mainActivityInterface.getTempSong().getLyricsUndosPos());
    }

    private void setupListeners() {
        myView.lyrics.setOnFocusChangeListener((view, b) -> {
            mainActivityInterface.enableSwipe("edit",!b);
        });

        myView.lyrics.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

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

        myView.settingsButton.setOnClickListener(v -> {
            cursorPos = myView.lyrics.getSelectionStart();
            Log.d(TAG,"cursorPos="+cursorPos);
            LyricsOptionsBottomSheet lyricsOptionsBottomSheet = new LyricsOptionsBottomSheet(this);
            lyricsOptionsBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"LyricsBottomSheet");
        });

        myView.undoButton.setOnClickListener(v -> undoLyrics());
        myView.redoButton.setOnClickListener(v -> redoLyrics());

        // Scroll listener
        myView.nestedScrollView.setExtendedFabToAnimate(editSongFragmentInterface.getSaveButton());
        myView.nestedScrollView.setFabToAnimate(myView.undoButton);
        myView.nestedScrollView.setFab2ToAnimate(myView.redoButton);
        myView.nestedScrollView.setFab3ToAnimate(myView.settingsButton);

        // Resize the bottom padding to the soft keyboard height or half the screen height for the soft keyboard (workaround)
        mainActivityInterface.getWindowFlags().adjustViewPadding(mainActivityInterface,myView.resizeForKeyboardLayout);
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
    private void validUndoRedo(int currentPosition) {
        // Enable/disable the undo button
        myView.undoButton.setEnabled(currentPosition>0);

        // Enable/disable the redo button
        myView.redoButton.setEnabled(currentPosition<mainActivityInterface.getTempSong().getLyricsUndos().size()-1);
    }

    // The stuff below is called from the LyricsOptionsBottomSheet
    public float getEditTextSize() {
        return editTextSize;
    }
    public void setEditTextSize(float editTextSize) {
        this.editTextSize = editTextSize;
        myView.lyrics.setTextSize(editTextSize);
    }
    public void insertSection() {
        // Try to get the current text position
        String text = myView.lyrics.getText().toString();
        text = text.substring(0,cursorPos) + "[]\n" + text.substring(cursorPos);
        myView.lyrics.setText(text);
        myView.lyrics.setSelection(cursorPos+1);
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

    public void transpose(String direction) {
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

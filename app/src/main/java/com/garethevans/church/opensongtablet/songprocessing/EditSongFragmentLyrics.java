package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.EditSongLyricsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

// This fragment purely deals with the lyrics/chords

public class EditSongFragmentLyrics extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private EditSongLyricsBinding myView;
    private final String TAG = "EditSongFragmentLyrics";
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private float editTextSize = 11;
    private int colorOn, colorOff;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = requireActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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
        // The button colors
        colorOn = getResources().getColor(R.color.colorSecondary);
        colorOff = getResources().getColor(R.color.colorAltPrimary);

        // Set up the bottomSheet
        bottomSheetBar();

        myView.lyrics.setText(mainActivityInterface.getTempSong().getLyrics());
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.lyrics);
        editTextSize = mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),"editTextSize",14);
        checkTextSize(0);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.lyrics,20);
    }

    private void bottomSheetBar() {
        bottomSheetBehavior = BottomSheetBehavior.from(myView.bottomSheetLayout.bottomSheet);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setGestureInsetBottomIgnored(true);

        myView.bottomSheetLayout.insertSection.setHint("[V]="+getString(R.string.verse) +
                " , [V1]="+getString(R.string.verse)+" 1, [C]="+getString(R.string.chorus) +
                ", [B]="+getString(R.string.bridge)+", [P]="+getString(R.string.prechorus) +
                ", [...]="+getString(R.string.custom));
        myView.dimBackground.setClickable(true);

        // Set the peek height to match the drag icon
        ViewTreeObserver vto = myView.bottomSheetLayout.handleView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int size = myView.bottomSheetLayout.handleView.getHeight();
                Log.d(TAG,"size="+size);
                bottomSheetBehavior.setPeekHeight(size);
                bottomSheetBehavior.setFitToContents(false);
                int screenHeight = myView.parentView.getMeasuredHeight();
                int bottomsheetHeight = myView.bottomSheetLayout.bottomSheet.getMeasuredHeight();
                int offset = screenHeight-bottomsheetHeight;
                bottomSheetBehavior.setExpandedOffset(offset);
                myView.bottomSheetLayout.handleView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        myView.dimBackground.setOnClickListener(v -> myView.bottomSheetLayout.handleView.performClick());
        myView.bottomSheetLayout.handleView.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                myView.lyrics.setEnabled(false);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                myView.lyrics.setEnabled(true);
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
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_SETTLING:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        myView.lyrics.setEnabled(false);
                        break;
                }
            }

            @Override
            public void onSlide (@NonNull View bottomSheet,float slideOffset) {
                myView.dimBackground.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupListeners() {
        myView.lyrics.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.lyrics,20);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mainActivityInterface.getTempSong().setLyrics(editable.toString());
                mainActivityInterface.showSaveAllowed(mainActivityInterface.songChanged());
            }
        });
        myView.bottomSheetLayout.textSizeDown.setOnClickListener(v -> checkTextSize(-1));
        myView.bottomSheetLayout.textSizeUp.setOnClickListener(v -> checkTextSize(+1));
        myView.bottomSheetLayout.insertSection.setOnClickListener(v -> insertSection());
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
        mainActivityInterface.getPreferences().setMyPreferenceFloat(getContext(),"editTextSize",editTextSize);
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
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"editAsChordPro",false)) {
            myView.lyrics.setText(mainActivityInterface.getConvertChoPro().
                    fromOpenSongToChordPro(requireContext(), mainActivityInterface, mainActivityInterface.getTempSong().getLyrics()));
        } else {
            myView.lyrics.setText(mainActivityInterface.getConvertChoPro().
                    fromChordProToOpenSong(mainActivityInterface.getTempSong().getLyrics()));
        }
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

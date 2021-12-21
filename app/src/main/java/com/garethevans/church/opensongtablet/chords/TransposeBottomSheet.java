package com.garethevans.church.opensongtablet.chords;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetTransposeBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TransposeBottomSheet extends BottomSheetDialogFragment {

    private boolean editSong = false;  // This is set to true when coming here from EditSongFragment

    private BottomSheetTransposeBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final String TAG = "TransposeBottomSheet";

    private String originalKey;

    public TransposeBottomSheet(boolean editSong) {
        // This is called from the EditSongFragment.  Receive temp lyrics and key
        this.editSong = editSong;
    }

    public TransposeBottomSheet() {
        // Null initialised for when we come here from performance/presentation/stage mode
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = BottomSheetTransposeBinding.inflate(inflater,container,false);

        myView.dialogHeading.setText(getString(R.string.transpose));
        myView.dialogHeading.setClose(this);

        // Set up views to match preferences
        setupViews();

        // Set the listeners
        setListeners();

        // Decide if we are using preferred chord format
        usePreferredChordFormat(mainActivityInterface.getPreferences().getMyPreferenceBoolean(getActivity(),"chordFormatUsePreferred",false));

        return myView.getRoot();
    }

    private void setupViews() {
        myView.transposeSlider.setValue(0);

        // Get the key of the song if set
        if (editSong) {
            originalKey = mainActivityInterface.getTempSong().getKey();
        } else {
            originalKey = mainActivityInterface.getSong().getKey();
        }

        if (originalKey.isEmpty()) {
            myView.keyChangeTextView.setText(getTransposeKey("0"));
        } else {
            myView.keyChangeTextView.setText(getTransposeKey(originalKey));
        }

        myView.assumePreferred.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                requireContext(), "chordFormatUsePreferred", false));
        usePreferredChordFormat(myView.assumePreferred.isChecked());

        // Set the detected chordformat
        switch (mainActivityInterface.getSong().getDetectedChordFormat()) {
            case 1:
            default:
                myView.chordFormat1Radio.setChecked(true);
                break;
            case 2:
                myView.chordFormat2Radio.setChecked(true);
                break;
            case 3:
                myView.chordFormat3Radio.setChecked(true);
                break;
            case 4:
                myView.chordFormat4Radio.setChecked(true);
                break;
            case 5:
                myView.chordFormat5Radio.setChecked(true);
                break;
            case 6:
                myView.chordFormat6Radio.setChecked(true);
                break;
        }
    }

    private String getTransposeKey(String newKey) {
        if (originalKey==null || originalKey.isEmpty() || originalKey.equals("0")) {
            return newKey;
        } else {
            return getString(R.string.key) + ": " + originalKey + "\n" +
                    getString(R.string.transpose) + ": " + newKey;
        }
    }
    private void setListeners() {
        //0=-6, 1=-5, 2=-4, 3=-3, 4=-2, 5=-1, 6=0, 7=1, 8=2, 9=3, 10=4, 11=5, 12=6
        myView.transposeSlider.addOnChangeListener((slider, value, fromUser) -> {
            // Update the text
            String newKey;
            if (originalKey==null || originalKey.isEmpty() || originalKey.equals("0")) {
                if (value>0) {
                    newKey = "+" + (int)value;
                } else {
                    newKey = "" + (int) value;
                }
                myView.keyChangeTextView.setText(newKey);
            } else {
                // We need to get the transposed key
                String keyToNum = mainActivityInterface.getTranspose().keyToNumber(originalKey);
                if (value<0) {
                    newKey = mainActivityInterface.getTranspose().transposeNumber(keyToNum,"-1",(int)Math.abs(value));
                    // newKey = mainActivityInterface.getTranspose().transposeKey(keyToNum,"-1",(int)Math.abs(value));
                } else if (value>0) {
                    newKey = mainActivityInterface.getTranspose().transposeNumber(keyToNum,"+1",(int)Math.abs(value));
                    //newKey = mainActivityInterface.getTranspose().transposeKey(keyToNum,"+1",(int)Math.abs(value));
                } else {
                    newKey = originalKey;
                }
                newKey = mainActivityInterface.getTranspose().numberToKey(requireContext(),mainActivityInterface,newKey);
                myView.keyChangeTextView.setText(getTransposeKey(newKey));
            }
        });

        myView.assumePreferred.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(getActivity(),"chordFormatUsePreferred",isChecked);
            usePreferredChordFormat(isChecked);
        });
        myView.chordFormat1Radio.setOnCheckedChangeListener(new ChangeFormat(1));
        myView.chordFormat2Radio.setOnCheckedChangeListener(new ChangeFormat(2));
        myView.chordFormat3Radio.setOnCheckedChangeListener(new ChangeFormat(3));
        myView.chordFormat4Radio.setOnCheckedChangeListener(new ChangeFormat(4));
        myView.chordFormat5Radio.setOnCheckedChangeListener(new ChangeFormat(5));
        myView.chordFormat6Radio.setOnCheckedChangeListener(new ChangeFormat(6));

        myView.doTransposeButton.setOnClickListener(v -> doTranspose());
    }

    private void usePreferredChordFormat(boolean trueorfalse) {
        // Use preferred chord format or detected one
        int formattouse;

        if (trueorfalse) {
            formattouse = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(),"chordFormat",1);
            myView.chooseFormatLinearLayout.setVisibility(View.GONE);
        } else {
            formattouse = mainActivityInterface.getSong().getDetectedChordFormat();
            myView.chooseFormatLinearLayout.setVisibility(View.VISIBLE);
        }

        switch (formattouse) {
            case 1:
                myView.chordFormat1Radio.setChecked(true);
                break;
            case 2:
                myView.chordFormat2Radio.setChecked(true);
                break;
            case 3:
                myView.chordFormat3Radio.setChecked(true);
                break;
            case 4:
                myView.chordFormat4Radio.setChecked(true);
                break;
            case 5:
                myView.chordFormat5Radio.setChecked(true);
                break;
        }
    }

    private void getValues() {
        // Extract the transpose value and the chord format
        if (myView.chordFormat1Radio.isChecked()) {
            mainActivityInterface.getSong().setDetectedChordFormat(1);
        }
        if (myView.chordFormat2Radio.isChecked()) {
            mainActivityInterface.getSong().setDetectedChordFormat(2);
        }
        if (myView.chordFormat3Radio.isChecked()) {
            mainActivityInterface.getSong().setDetectedChordFormat(3);
        }
        if (myView.chordFormat4Radio.isChecked()) {
            mainActivityInterface.getSong().setDetectedChordFormat(4);
        }
        if (myView.chordFormat5Radio.isChecked()) {
            mainActivityInterface.getSong().setDetectedChordFormat(5);
        }
        if (myView.chordFormat6Radio.isChecked()) {
            mainActivityInterface.getSong().setDetectedChordFormat(6);
        }
    }

    private void doTranspose() {
        getValues();
        new Thread(() -> {
            String transposeDirection;
            int transposeTimes = (int)myView.transposeSlider.getValue();

            // Simplify slider to minimum number of transpose steps
            // Why transpose up 11 times, when you can just transpose down once.
            // Giving the option as it makes it easier for the user to select new key
            if (transposeTimes>6) {
                // 7>-5  8>-4 9>-3 10>-2 11>-1 12>0
                transposeTimes = transposeTimes-12;
            } else if (transposeTimes<-6) {
                // -7>5 -8>4 -9>3 -10>2 -11>1 -12>0
                transposeTimes = 12+transposeTimes;
            }

            if (transposeTimes>=0) {
                transposeDirection = "+1";
            } else {
                transposeDirection = "-1";
            }

            transposeTimes = Math.abs(transposeTimes);

            boolean ignoreChordFormat = myView.assumePreferred.isChecked();
            int newChordFormat = 1;
            if (ignoreChordFormat) {
                newChordFormat = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(),"chordFormat",1);
            } else {
                newChordFormat = mainActivityInterface.getSong().getDetectedChordFormat();
            }

            // Do the transpose (song and key)
            mainActivityInterface.getTranspose().doTranspose(requireContext(),mainActivityInterface,mainActivityInterface.getSong(),
                    transposeDirection, transposeTimes, newChordFormat);

            // Now save the changes
            mainActivityInterface.getSaveSong().updateSong(requireContext(), mainActivityInterface);


            requireActivity().runOnUiThread(() -> {
                // Update the song menu
                mainActivityInterface.updateSongMenu(mainActivityInterface.getSong());

                // Load the song again
                mainActivityInterface.doSongLoad(mainActivityInterface.getSong().getFolder(),
                        mainActivityInterface.getSong().getFilename(), true);
            });

            dismiss();
        }).start();
    }

    private class ChangeFormat implements CompoundButton.OnCheckedChangeListener {
        int newFormat;

        ChangeFormat(int newFormat) {
            this.newFormat = newFormat;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                Log.d(TAG,"newChordFormat="+newFormat);
            }
        }
    }
}

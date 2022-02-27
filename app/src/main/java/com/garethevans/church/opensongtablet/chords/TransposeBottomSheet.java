package com.garethevans.church.opensongtablet.chords;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
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
    private int fromFormat, toFormat, prefFormat;
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

        // Detect chord format
        mainActivityInterface.getTranspose().checkChordFormat(mainActivityInterface.getSong());

        // Set up views to match preferences
        setupViews();

        // Set the listeners
        setListeners();

        // Decide if we are using preferred chord format
        usePreferredChordFormat(mainActivityInterface.getPreferences().getMyPreferenceBoolean("chordFormatUsePreferred",false));

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

        // Set up the exposed dropdown for chord formats and set to detected
        buildChordFormatOptions();
        int detectedFormatNum = mainActivityInterface.getSong().getDetectedChordFormat();
        String detectedName = mainActivityInterface.getTranspose().getChordFormatNames().get(detectedFormatNum-1);
        String detectedAppearance = mainActivityInterface.getTranspose().getChordFormatAppearances().get(detectedFormatNum-1);
        myView.chordFormatFrom.setText(detectedName);
        myView.chordFormatTo.setText(detectedName);
        String detectedSummary = detectedName + ": " + detectedAppearance;
        myView.warningFormatMatch.setHint(detectedSummary);


        myView.assumePreferred.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "chordFormatUsePreferred", false));
        prefFormat = mainActivityInterface.getPreferences().getMyPreferenceInt(
                "chordFormat",1);
        String hint = getString(R.string.chordformat_preferred_info) + " " +
                mainActivityInterface.getTranspose().getChordFormatNames().get(prefFormat-1) +
                ": " + mainActivityInterface.getTranspose().getChordFormatAppearances().get(prefFormat-1);
        myView.assumePreferred.setHint(hint);

        usePreferredChordFormat(myView.assumePreferred.getChecked());

        myView.capoChange.setVisibility(View.GONE);
        myView.transposeCapo.setChecked(false);
    }

    private void buildChordFormatOptions() {
        ExposedDropDownArrayAdapter exposedDropDownArrayAdapterFrom = new ExposedDropDownArrayAdapter(requireContext(),
                myView.chordFormatFrom,R.layout.view_exposed_dropdown_item,
                mainActivityInterface.getTranspose().getChordFormatNames());
        ExposedDropDownArrayAdapter exposedDropDownArrayAdapterTo = new ExposedDropDownArrayAdapter(requireContext(),
                myView.chordFormatTo,R.layout.view_exposed_dropdown_item,
                mainActivityInterface.getTranspose().getChordFormatNames());
        myView.chordFormatFrom.setAdapter(exposedDropDownArrayAdapterFrom);
        myView.chordFormatTo.setAdapter(exposedDropDownArrayAdapterTo);

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
                newKey = mainActivityInterface.getTranspose().numberToKey(newKey);
                myView.keyChangeTextView.setText(getTransposeKey(newKey));
            }
        });

        myView.assumePreferred.getSwitch().setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("chordFormatUsePreferred",isChecked);
            usePreferredChordFormat(isChecked);
        });

        myView.doTransposeButton.setOnClickListener(v -> doTranspose());

        myView.chordsFormat.setOnClickListener(view -> {
            mainActivityInterface.navigateToFragment("opensongapp://settings/chords/settings",0);
            dismiss();

        });

        myView.transposeCapo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                myView.capoChange.setVisibility(View.VISIBLE);
            } else {
                myView.capoChange.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void usePreferredChordFormat(boolean trueorfalse) {
        // Use preferred chord format or detected one
        int formattouse;

        if (trueorfalse) {
            formattouse = prefFormat;
            myView.chordFormat.setVisibility(View.GONE);
            if (prefFormat==mainActivityInterface.getSong().getDetectedChordFormat()) {
                myView.warningFormatMatch.setVisibility(View.GONE);
            } else {
                myView.warningFormatMatch.setTextColor(Color.RED);
                myView.warningFormatMatch.setHintColor(Color.RED);
                myView.warningFormatMatch.setVisibility(View.VISIBLE);
            }

        } else {
            formattouse = mainActivityInterface.getSong().getDetectedChordFormat();
            myView.chordFormat.setVisibility(View.VISIBLE);
            myView.warningFormatMatch.setVisibility(View.GONE);
        }

        myView.chordFormatFrom.setText(mainActivityInterface.getTranspose().getChordFormatNames().get(formattouse-1));
        myView.chordFormatTo.setText(mainActivityInterface.getTranspose().getChordFormatNames().get(formattouse-1));
    }

    private void getValues() {
        if (myView.assumePreferred.getChecked()) {
            fromFormat = mainActivityInterface.getPreferences().getMyPreferenceInt(
                    "chordFormat", 1);
            toFormat = fromFormat;
        } else {
            // Overriding the preferred defaults
            fromFormat = mainActivityInterface.getTranspose().getChordFormatNames().indexOf(myView.chordFormatFrom.getText().toString())+1;
            toFormat = mainActivityInterface.getTranspose().getChordFormatNames().indexOf(myView.chordFormatTo.getText().toString())+1;
        }
        // Update the song detected chord format as required by manual change
        mainActivityInterface.getSong().setDetectedChordFormat(fromFormat);
        mainActivityInterface.getSong().setDesiredChordFormat(toFormat);
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

            //If requested transpose the capo fret number
            if (myView.transposeCapo.isSelected()) {
                String capo = mainActivityInterface.getSong().getCapo();
                mainActivityInterface.getSong().setCapo(String.valueOf(((Integer.parseInt("0" + capo) + 12 + (transposeTimes * Integer.parseInt(transposeDirection))) % 12)));
                if (mainActivityInterface.getSong().getCapo().equals("0")) {
                    mainActivityInterface.getSong().setCapo("");
                }
            }
            // doTranspose will write the song including any transposed capo

            // Do the transpose (song and key)
            mainActivityInterface.getTranspose().doTranspose(mainActivityInterface.getSong(),
                    transposeDirection, transposeTimes, fromFormat, toFormat);

            // Now save the changes
            mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong());

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

}

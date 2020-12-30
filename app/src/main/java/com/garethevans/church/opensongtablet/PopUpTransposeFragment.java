package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpTransposeFragment extends DialogFragment {

    static PopUpTransposeFragment newInstance() {
        PopUpTransposeFragment frag;
        frag = new PopUpTransposeFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
    }

    private MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private SeekBar transposeSeekBar;
    private TextView transposeValTextView, keyChange_TextView;
    private RadioButton chordFormat1Radio, chordFormat2Radio, chordFormat3Radio, chordFormat4Radio,
            chordFormat5Radio, chordFormat6Radio;
    private SwitchCompat assumePreferred_SwitchCompat;
    private LinearLayout chooseFormat_LinearLayout;

    private Preferences preferences;
    private StorageAccess storageAccess;
    private Transpose transpose;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        final View V = inflater.inflate(R.layout.popup_transpose, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.transpose));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe,getContext());
            saveMe.setEnabled(false);
            doTranspose();
        });

        preferences = new Preferences();
        storageAccess = new StorageAccess();
        transpose = new Transpose();

        // Initialise views
        identifyViews(V);

        // Set up views to match preferences
        setButtons();

        // Set the listeners
        setListeners();

        // Initialise the transpose values
        StaticVariables.transposeDirection = "";
        StaticVariables.transposeTimes = Math.abs(0);

        // Decide if we are using preferred chord format
        usePreferredChordFormat(preferences.getMyPreferenceBoolean(getContext(),"chordFormatUsePreferred",false));


        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);
        return V;
    }

    private void identifyViews(View V) {
        transposeSeekBar = V.findViewById(R.id.transposeSeekBar);
        transposeValTextView = V.findViewById(R.id.transposeValTextView);
        keyChange_TextView = V.findViewById(R.id.keyChange_TextView);
        chordFormat1Radio = V.findViewById(R.id.chordFormat1Radio);
        chordFormat2Radio = V.findViewById(R.id.chordFormat2Radio);
        chordFormat3Radio = V.findViewById(R.id.chordFormat3Radio);
        chordFormat4Radio = V.findViewById(R.id.chordFormat4Radio);
        chordFormat5Radio = V.findViewById(R.id.chordFormat5Radio);
        chordFormat6Radio = V.findViewById(R.id.chordFormat6Radio);
        assumePreferred_SwitchCompat = V.findViewById(R.id.assumePreferred_SwitchCompat);
        chooseFormat_LinearLayout = V.findViewById(R.id.chooseFormat_LinearLayout);
    }

    private void setButtons() {
        transposeSeekBar.setMax(12);
        transposeSeekBar.setProgress(6);
        transposeValTextView.setText("0");

        // If the song has a key specified, we will add in the text for current and new key
        if (StaticVariables.mKey!=null && !StaticVariables.mKey.equals("")) {
            String keychange = getString(R.string.edit_song_key) + ": " + StaticVariables.mKey + "\n" +
                    getString(R.string.transpose) + ": " + StaticVariables.mKey;
            keyChange_TextView.setText(keychange);
        } else {
            keyChange_TextView.setText("");
            keyChange_TextView.setVisibility(View.GONE);
        }

        // Set the detected chordformat
        Log.d("PopUpTranspose","detectedChordFormat="+StaticVariables.detectedChordFormat);


        switch (StaticVariables.detectedChordFormat) {
            case 1:
                chordFormat1Radio.setChecked(true);
                break;
            case 2:
                chordFormat2Radio.setChecked(true);
                break;
            case 3:
                chordFormat3Radio.setChecked(true);
                break;
            case 4:
                chordFormat4Radio.setChecked(true);
                break;
            case 5:
                chordFormat5Radio.setChecked(true);
                break;
            case 6:
                chordFormat6Radio.setChecked(true);
                break;
        }
    }

    private void setListeners() {
        //0=-6, 1=-5, 2=-4, 3=-3, 4=-2, 5=-1, 6=0, 7=1, 8=2, 9=3, 10=4, 11=5, 12=6
        transposeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = progress-6;
                if (val<0) {
                    StaticVariables.transposeDirection = "-1";
                    StaticVariables.transposeTimes = Math.abs(val);
                    String text = "-"+Math.abs(val);
                    transposeValTextView.setText(text);

                } else if (val>0) {
                    StaticVariables.transposeDirection = "+1";
                    StaticVariables.transposeTimes = Math.abs(val);
                    String text = "+"+Math.abs(val);
                    transposeValTextView.setText(text);
                } else {
                    StaticVariables.transposeDirection = "";
                    StaticVariables.transposeTimes = Math.abs(0);
                    transposeValTextView.setText("0");
                }

                // If the song has a key specified, we will add in the text for current and new key
                if (StaticVariables.mKey!=null && !StaticVariables.mKey.equals("")) {
                    // Get the new key value
                    String keynum = transpose.keyToNumber(StaticVariables.mKey);
                    String transpkeynum = transpose.transposeKey(keynum, StaticVariables.transposeDirection, StaticVariables.transposeTimes);
                    String newkey = transpose.numberToKey(getContext(), preferences, transpkeynum);

                    String keychange = getString(R.string.edit_song_key) + ": " + StaticVariables.mKey + "\n" +
                            getString(R.string.transpose) + ": " + newkey;
                    keyChange_TextView.setText(keychange);
                } else {
                    keyChange_TextView.setText("");
                    keyChange_TextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        assumePreferred_SwitchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(getContext(),"chordFormatUsePreferred",isChecked);
            usePreferredChordFormat(isChecked);
        });
    }

    private void usePreferredChordFormat(boolean trueorfalse) {
        // Use preferred chord format or detected one
        int formattouse;

        if (trueorfalse) {
            formattouse = preferences.getMyPreferenceInt(getContext(),"chordFormat",1);
        } else {
            formattouse = StaticVariables.detectedChordFormat;
        }

        switch (formattouse) {
            case 1:
                chordFormat1Radio.setChecked(true);
                break;
            case 2:
                chordFormat2Radio.setChecked(true);
                break;
            case 3:
                chordFormat3Radio.setChecked(true);
                break;
            case 4:
                chordFormat4Radio.setChecked(true);
                break;
            case 5:
                chordFormat5Radio.setChecked(true);
                break;
        }

        boolean usePreferred = preferences.getMyPreferenceBoolean(getContext(),"chordFormatUsePreferred",true);
        assumePreferred_SwitchCompat.setChecked(usePreferred);

        if (usePreferred) {
            chooseFormat_LinearLayout.setVisibility(View.GONE);
        } else {
            chooseFormat_LinearLayout.setVisibility(View.VISIBLE);
        }
    }

    private void doTranspose() {
        // Extract the transpose value and the chord format

        if (chordFormat1Radio.isChecked()) {
            StaticVariables.detectedChordFormat = 1;
        }
        if (chordFormat2Radio.isChecked()) {
            StaticVariables.detectedChordFormat = 2;
        }
        if (chordFormat3Radio.isChecked()) {
            StaticVariables.detectedChordFormat = 3;
        }
        if (chordFormat4Radio.isChecked()) {
            StaticVariables.detectedChordFormat = 4;
        }
        if (chordFormat5Radio.isChecked()) {
            StaticVariables.detectedChordFormat = 5;
        }
        if (chordFormat6Radio.isChecked()) {
            StaticVariables.detectedChordFormat = 6;
        }

        // Do the transpose
        try {
            transpose.doTranspose(getContext(), storageAccess, preferences, false, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mListener.refreshAll();
        dismiss();
    }

}
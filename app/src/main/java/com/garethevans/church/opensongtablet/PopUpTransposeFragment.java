package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
    private TextView transposeValTextView, keyChange_TextView, capoChange_TextView, detectedChordFormatText;
    private RadioButton chordFormat1Radio, chordFormat2Radio, chordFormat3Radio, chordFormat4Radio,
            chordFormat5Radio, chordFormat6Radio;
    private SwitchCompat transposeCapo_SwitchCompat;
    private SwitchCompat assumePreferred_SwitchCompat;
    private Preferences preferences;
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
        transpose = new Transpose();

        // Initialise views
        identifyViews(V);

        // Set up views to match preferences
        setButtons();

        // Set the listeners
        setListeners();

        // Initialise the transpose values
        StaticVariables.transposeDirection = "-1";
        StaticVariables.transposeTimes = 0;

        // Decide if we are using preferred chord format
        usePreferredChordFormat(preferences.getMyPreferenceBoolean(getContext(),"chordFormatUsePreferred",false));

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);
        return V;
    }

    private void identifyViews(View V) {
        transposeSeekBar = V.findViewById(R.id.transposeSeekBar);
        transposeValTextView = V.findViewById(R.id.transposeValTextView);
        keyChange_TextView = V.findViewById(R.id.keyChange_TextView);
        capoChange_TextView = V.findViewById(R.id.capoChange_TextView);
        chordFormat1Radio = V.findViewById(R.id.chordFormat1Radio);
        chordFormat2Radio = V.findViewById(R.id.chordFormat2Radio);
        chordFormat3Radio = V.findViewById(R.id.chordFormat3Radio);
        chordFormat4Radio = V.findViewById(R.id.chordFormat4Radio);
        chordFormat5Radio = V.findViewById(R.id.chordFormat5Radio);
        chordFormat6Radio = V.findViewById(R.id.chordFormat6Radio);
        transposeCapo_SwitchCompat = V.findViewById(R.id.transposeCapo_SwitchCompat);
        assumePreferred_SwitchCompat = V.findViewById(R.id.assumePreferred_SwitchCompat);
        detectedChordFormatText = V.findViewById(R.id.detectedChordFormatText);
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

        // Set the new chordformat
        Log.d("PopUpTranspose","newChordFormat="+StaticVariables.newChordFormat);


        switch (StaticVariables.newChordFormat) {
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

        String transpCapoFret = getString(R.string.transpose) + " " + getString(R.string.edit_song_capo);

        transposeCapo_SwitchCompat.setText(transpCapoFret);

        // By default hide the hint text under the transpose slider
        // Invisible rather than gone to avoid the views below jumping up/down
        capoChange_TextView.setVisibility(View.INVISIBLE);
        capoChange_TextView.setText(transpCapoFret);

        // Set the switch for capo transpose off
        transposeCapo_SwitchCompat.setChecked(false);
    }

    private void setListeners() {
        //0=-6, 1=-5, 2=-4, 3=-3, 4=-2, 5=-1, 6=0, 7=1, 8=2, 9=3, 10=4, 11=5, 12=6
        transposeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = progress - 6;
                if (val < 0) {
                    StaticVariables.transposeDirection = "-1";
                    StaticVariables.transposeTimes = Math.abs(val);
                    String text = "-"+ Math.abs(val);
                    transposeValTextView.setText(text);

                } else if (val > 0) {
                    StaticVariables.transposeDirection = "+1";
                    StaticVariables.transposeTimes = Math.abs(val);
                    String text = "+"+ Math.abs(val);
                    transposeValTextView.setText(text);
                } else {
                    StaticVariables.transposeDirection = "-1";
                    StaticVariables.transposeTimes = 0;
                    transposeValTextView.setText("0");
                }

                // If the song has a key specified, we will add in the text for current and new key
                if (StaticVariables.mKey!=null && !StaticVariables.mKey.equals("")) {
                    // Get the new key value
                    String keynum = transpose.keyToNumber(StaticVariables.mKey);
                    String transpkeynum = transpose.transposeNumber(keynum, StaticVariables.transposeDirection, StaticVariables.transposeTimes);
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
            // IV -  Set of preference removed - we now use preference only as an aid to selecting
            usePreferredChordFormat(isChecked);
        });
        transposeCapo_SwitchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                capoChange_TextView.setVisibility(View.VISIBLE);
            } else {
                capoChange_TextView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void usePreferredChordFormat(boolean preferred) {
        // Use preferred chord format or detected one
        int formattouse = preferences.getMyPreferenceInt(getContext(),"chordFormat",1);

        if (formattouse == 0) {
            assumePreferred_SwitchCompat.setVisibility(View.GONE);
        } else {
            assumePreferred_SwitchCompat.setVisibility(View.VISIBLE);
        }

        if (StaticVariables.detectedChordFormat==1) chordFormat1Radio.setTypeface(chordFormat1Radio.getTypeface(), Typeface.BOLD);
        if (StaticVariables.detectedChordFormat==2) chordFormat2Radio.setTypeface(chordFormat2Radio.getTypeface(), Typeface.BOLD);
        if (StaticVariables.detectedChordFormat==3) chordFormat3Radio.setTypeface(chordFormat3Radio.getTypeface(), Typeface.BOLD);
        if (StaticVariables.detectedChordFormat==4) chordFormat4Radio.setTypeface(chordFormat4Radio.getTypeface(), Typeface.BOLD);
        if (StaticVariables.detectedChordFormat==5) chordFormat5Radio.setTypeface(chordFormat5Radio.getTypeface(), Typeface.BOLD);
        if (StaticVariables.detectedChordFormat==6) chordFormat6Radio.setTypeface(chordFormat6Radio.getTypeface(), Typeface.BOLD);

        if (preferred && formattouse != 0) {
            formattouse = preferences.getMyPreferenceInt(getContext(),"chordFormat",1);
        } else {
            preferred = false;
            formattouse = StaticVariables.detectedChordFormat;
        }

        chordFormat1Radio.setChecked(formattouse==1);
        chordFormat2Radio.setChecked(formattouse==2);
        chordFormat3Radio.setChecked(formattouse==3);
        chordFormat4Radio.setChecked(formattouse==4);
        chordFormat5Radio.setChecked(formattouse==5);
        chordFormat6Radio.setChecked(formattouse==6);

        assumePreferred_SwitchCompat.setChecked(preferred);

        if (preferred) {
            detectedChordFormatText.setVisibility(View.GONE);

            if (formattouse!=1) chordFormat1Radio.setVisibility(View.GONE);
            else chordFormat1Radio.setVisibility(View.VISIBLE);

            if (formattouse!=2) chordFormat2Radio.setVisibility(View.GONE);
            else chordFormat2Radio.setVisibility(View.VISIBLE);

            if (formattouse!=3) chordFormat3Radio.setVisibility(View.GONE);
            else chordFormat3Radio.setVisibility(View.VISIBLE);

            if (formattouse!=4) chordFormat4Radio.setVisibility(View.GONE);
            else chordFormat4Radio.setVisibility(View.VISIBLE);

            if (formattouse!=5) chordFormat5Radio.setVisibility(View.GONE);
            else chordFormat5Radio.setVisibility(View.VISIBLE);

            if (formattouse!=6) chordFormat6Radio.setVisibility(View.GONE);
            else chordFormat6Radio.setVisibility(View.VISIBLE);
        } else {
            detectedChordFormatText.setVisibility(View.VISIBLE);

            chordFormat1Radio.setVisibility(View.VISIBLE);
            chordFormat2Radio.setVisibility(View.VISIBLE);
            chordFormat3Radio.setVisibility(View.VISIBLE);
            chordFormat4Radio.setVisibility(View.VISIBLE);
            chordFormat5Radio.setVisibility(View.VISIBLE);
            chordFormat6Radio.setVisibility(View.VISIBLE);
        }
    }

    private void doTranspose() {
        // Extract the transpose value and the chord format

        if (chordFormat1Radio.isChecked()) {
            StaticVariables.newChordFormat = 1;
        }
        if (chordFormat2Radio.isChecked()) {
            StaticVariables.newChordFormat = 2;
        }
        if (chordFormat3Radio.isChecked()) {
            StaticVariables.newChordFormat = 3;
        }
        if (chordFormat4Radio.isChecked()) {
            StaticVariables.newChordFormat = 4;
        }
        if (chordFormat5Radio.isChecked()) {
            StaticVariables.newChordFormat = 5;
        }
        if (chordFormat6Radio.isChecked()) {
            StaticVariables.newChordFormat = 6;
        }

        // Do the transpose
        try {
            // If requested transpose the capo fret number
            if (transposeCapo_SwitchCompat.isChecked()) {
                StaticVariables.mCapo = String.valueOf(((Integer.parseInt("0" + StaticVariables.mCapo) + 12 + (StaticVariables.transposeTimes * Integer.parseInt(StaticVariables.transposeDirection))) % 12));
                if (StaticVariables.mCapo.equals("0")) {
                    StaticVariables.mCapo = "";
                }
            }
            // doTranspose will write the song including any transposed capo
            transpose.doTranspose(getContext(), preferences, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mListener.refreshAll();
        dismiss();
    }

}
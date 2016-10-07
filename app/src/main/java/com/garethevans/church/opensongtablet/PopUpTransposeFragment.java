package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

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
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    SeekBar transposeSeekBar;
    TextView transposeValTextView;
    TextView keyChange_TextView;
    RadioGroup detectedChordFormat;
    RadioButton chordFormat1Radio;
    RadioButton chordFormat2Radio;
    RadioButton chordFormat3Radio;
    RadioButton chordFormat4Radio;
    RadioButton chordFormat5Radio;
    Button transposeCancelButton;
    Button transposeOkButton;
    boolean updatekey = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View V = inflater.inflate(R.layout.popup_transpose, container, false);
        getDialog().setTitle(getActivity().getResources().getString(R.string.transpose));

        // Initialise views
        transposeSeekBar = (SeekBar) V.findViewById(R.id.transposeSeekBar);
        transposeValTextView = (TextView) V.findViewById(R.id.transposeValTextView);
        keyChange_TextView = (TextView) V.findViewById(R.id.keyChange_TextView);
        detectedChordFormat = (RadioGroup) V.findViewById(R.id.detectedChordFormat);
        transposeCancelButton = (Button) V.findViewById(R.id.transposeCancelButton);
        transposeOkButton = (Button) V.findViewById(R.id.transposeOkButton);
        chordFormat1Radio = (RadioButton) V.findViewById(R.id.chordFormat1Radio);
        chordFormat2Radio = (RadioButton) V.findViewById(R.id.chordFormat2Radio);
        chordFormat3Radio = (RadioButton) V.findViewById(R.id.chordFormat3Radio);
        chordFormat4Radio = (RadioButton) V.findViewById(R.id.chordFormat4Radio);
        chordFormat5Radio = (RadioButton) V.findViewById(R.id.chordFormat5Radio);

        // If user has said to always used preferred chord format, hide the options

        // If the song has a key specified, we will add in the text for current and new key
        if (FullscreenActivity.mKey!=null & !FullscreenActivity.mKey.equals("")) {
            updatekey=true;
            String keychange = getString(R.string.edit_song_key) + ": " + FullscreenActivity.mKey + "\n" +
                    getString(R.string.transpose) + ": " + FullscreenActivity.mKey;
            keyChange_TextView.setText(keychange);
        } else {
            keyChange_TextView.setText("");
            keyChange_TextView.setVisibility(View.GONE);
        }

        // Initialise the transpose values
        transposeSeekBar.setMax(12);
        transposeSeekBar.setProgress(6);
        transposeValTextView.setText("0");
        FullscreenActivity.transposeDirection = "";
        FullscreenActivity.transposeTimes = Math.abs(0);

        //0=-6, 1=-5, 2=-4, 3=-3, 4=-2, 5=-1, 6=0, 7=1, 8=2, 9=3, 10=4, 11=5, 12=6
        transposeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = progress-6;
                if (val<0) {
                    FullscreenActivity.transposeDirection = "-1";
                    FullscreenActivity.transposeTimes = Math.abs(val);
                    String text = "-"+Math.abs(val);
                    transposeValTextView.setText(text);

                } else if (val>0) {
                    FullscreenActivity.transposeDirection = "+1";
                    FullscreenActivity.transposeTimes = Math.abs(val);
                    String text = "+"+Math.abs(val);
                    transposeValTextView.setText(text);
                } else {
                    FullscreenActivity.transposeDirection = "";
                    FullscreenActivity.transposeTimes = Math.abs(0);
                    transposeValTextView.setText("0");
                }

                // If the song has a key specified, we will add in the text for current and new key
                if (FullscreenActivity.mKey!=null & !FullscreenActivity.mKey.equals("")) {
                    updatekey=true;
                    // Get the new key value
                    String keynum = Transpose.keyToNumber(FullscreenActivity.mKey);
                    String transpkeynum = Transpose.transposeKey(keynum,FullscreenActivity.transposeDirection,FullscreenActivity.transposeTimes);
                    String newkey = Transpose.numberToKey(transpkeynum);

                    String keychange = getString(R.string.edit_song_key) + ": " + FullscreenActivity.mKey + "\n" +
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

        // Set the detected chordformat
        switch (FullscreenActivity.oldchordformat) {
            case "1":
                chordFormat1Radio.setChecked(true);
                break;
            case "2":
                chordFormat2Radio.setChecked(true);
                break;
            case "3":
                chordFormat3Radio.setChecked(true);
                break;
            case "4":
                chordFormat4Radio.setChecked(true);
                break;
            case "5":
                chordFormat5Radio.setChecked(true);
        }

        if (FullscreenActivity.alwaysPreferredChordFormat.equals("Y")) {
            switch (FullscreenActivity.chordFormat) {
                case "1":
                    chordFormat1Radio.setChecked(true);
                    break;
                case "2":
                    chordFormat2Radio.setChecked(true);
                    break;
                case "3":
                    chordFormat3Radio.setChecked(true);
                    break;
                case "4":
                    chordFormat4Radio.setChecked(true);
                    break;
                case "5":
                    chordFormat5Radio.setChecked(true);
                    break;
            }
            V.findViewById(R.id.detectedChordFormatText).setVisibility(View.GONE);
            detectedChordFormat.setVisibility(View.GONE);
            chordFormat1Radio.setVisibility(View.GONE);
            chordFormat2Radio.setVisibility(View.GONE);
            chordFormat3Radio.setVisibility(View.GONE);
            chordFormat4Radio.setVisibility(View.GONE);
            chordFormat5Radio.setVisibility(View.GONE);
        }

        // Listen for Cancel and OK button
        transposeCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        transposeOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Extract the transpose value and the chord format

                if (chordFormat1Radio.isChecked()) {
                    FullscreenActivity.oldchordformat = "1";
                }
                if (chordFormat2Radio.isChecked()) {
                    FullscreenActivity.oldchordformat = "2";
                }
                if (chordFormat3Radio.isChecked()) {
                    FullscreenActivity.oldchordformat = "3";
                }
                if (chordFormat4Radio.isChecked()) {
                    FullscreenActivity.oldchordformat = "4";
                }
                if (chordFormat5Radio.isChecked()) {
                    FullscreenActivity.oldchordformat = "5";
                }

                // Do the transpose
                try {
                    Transpose.doTranspose();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mListener.refreshAll();
                dismiss();
            }
        });

        return V;
    }

    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null) {
            return;
        }

        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    }

}
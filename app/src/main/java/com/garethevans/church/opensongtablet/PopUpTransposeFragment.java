package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

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
    boolean updatekey = false;

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        final View V = inflater.inflate(R.layout.popup_transpose, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.transpose));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                saveMe.setEnabled(false);
                doTranspose();
            }
        });

        // Initialise views
        transposeSeekBar = V.findViewById(R.id.transposeSeekBar);
        transposeValTextView = V.findViewById(R.id.transposeValTextView);
        keyChange_TextView = V.findViewById(R.id.keyChange_TextView);
        detectedChordFormat = V.findViewById(R.id.detectedChordFormat);
        chordFormat1Radio = V.findViewById(R.id.chordFormat1Radio);
        chordFormat2Radio = V.findViewById(R.id.chordFormat2Radio);
        chordFormat3Radio = V.findViewById(R.id.chordFormat3Radio);
        chordFormat4Radio = V.findViewById(R.id.chordFormat4Radio);
        chordFormat5Radio = V.findViewById(R.id.chordFormat5Radio);

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

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        return V;
    }

    public void doTranspose() {
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
            Transpose.doTranspose(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mListener.refreshAll();
        dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
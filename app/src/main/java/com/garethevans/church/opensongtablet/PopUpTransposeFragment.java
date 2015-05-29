package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }


    ListView transposeValues;
    RadioGroup detectedChordFormat;
    RadioButton chordFormat1Radio;
    RadioButton chordFormat2Radio;
    RadioButton chordFormat3Radio;
    RadioButton chordFormat4Radio;
    Button transposeCancelButton;
    Button transposeOkButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View V = inflater.inflate(R.layout.popup_transpose, container, false);

        getDialog().setTitle(getActivity().getResources().getString(R.string.transpose));

        // Initialise views
        transposeValues = (ListView) V.findViewById(R.id.transposeValues);
        detectedChordFormat = (RadioGroup) V.findViewById(R.id.detectedChordFormat);
        transposeCancelButton = (Button) V.findViewById(R.id.transposeCancelButton);
        transposeOkButton = (Button) V.findViewById(R.id.transposeOkButton);
        chordFormat1Radio = (RadioButton) V.findViewById(R.id.chordFormat1Radio);
        chordFormat2Radio = (RadioButton) V.findViewById(R.id.chordFormat2Radio);
        chordFormat3Radio = (RadioButton) V.findViewById(R.id.chordFormat3Radio);
        chordFormat4Radio = (RadioButton) V.findViewById(R.id.chordFormat4Radio);

        // Populate the transpose values
        ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_checked, FullscreenActivity.transposeSteps);
        transposeValues.setAdapter(adapter);
        // Set +1 as the default
        transposeValues.setItemChecked(6, true);

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

                int getTranspVal = transposeValues.getCheckedItemPosition();
                String transpVal = (String) transposeValues.getItemAtPosition(getTranspVal);
                switch (transpVal) {
                    case "-6":
                        FullscreenActivity.transposeTimes = 6;
                        FullscreenActivity.transposeDirection = "-1";
                        break;
                    case "-5":
                        FullscreenActivity.transposeTimes = 5;
                        FullscreenActivity.transposeDirection = "-1";
                        break;
                    case "-4":
                        FullscreenActivity.transposeTimes = 4;
                        FullscreenActivity.transposeDirection = "-1";
                        break;
                    case "-3":
                        FullscreenActivity.transposeTimes = 3;
                        FullscreenActivity.transposeDirection = "-1";
                        break;
                    case "-2":
                        FullscreenActivity.transposeTimes = 2;
                        FullscreenActivity.transposeDirection = "-1";
                        break;
                    case "-1":
                        FullscreenActivity.transposeTimes = 1;
                        FullscreenActivity.transposeDirection = "-1";
                        break;
                    case "+1":
                        FullscreenActivity.transposeTimes = 1;
                        FullscreenActivity.transposeDirection = "+1";
                        break;
                    case "+2":
                        FullscreenActivity.transposeTimes = 2;
                        FullscreenActivity.transposeDirection = "+1";
                        break;
                    case "+3":
                        FullscreenActivity.transposeTimes = 3;
                        FullscreenActivity.transposeDirection = "+1";
                        break;
                    case "+4":
                        FullscreenActivity.transposeTimes = 4;
                        FullscreenActivity.transposeDirection = "+1";
                        break;
                    case "+5":
                        FullscreenActivity.transposeTimes = 5;
                        FullscreenActivity.transposeDirection = "+1";
                        break;
                    case "+6":
                        FullscreenActivity.transposeTimes = 6;
                        FullscreenActivity.transposeDirection = "+1";
                        break;
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
}
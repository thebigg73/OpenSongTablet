package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class PopUpChordFormatFragment extends DialogFragment {

    static PopUpChordFormatFragment newInstance() {
        PopUpChordFormatFragment frag;
        frag = new PopUpChordFormatFragment();
        return frag;
    }

    //Variables
    RadioGroup radioGroup;
    RadioGroup radioGroup2;
    static String numeral;
    static String numeral2;

    SwitchCompat switchAb;
    SwitchCompat switchBb;
    SwitchCompat switchDb;
    SwitchCompat switchEb;
    SwitchCompat switchGb;
    SwitchCompat switchAbm;
    SwitchCompat switchBbm;
    SwitchCompat switchDbm;
    SwitchCompat switchEbm;
    SwitchCompat switchGbm;

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

        View V = inflater.inflate(R.layout.popup_chordformat, container, false);

        TextView title = (TextView) V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.choosechordformat));
        final FloatingActionButton closeMe = (FloatingActionButton) V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                exitChordFormat();
            }
        });
        FloatingActionButton saveMe = (FloatingActionButton) V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        // Load the user preferences
        Preferences.loadPreferences();

        numeral = FullscreenActivity.chordFormat;
        numeral2 = FullscreenActivity.alwaysPreferredChordFormat;

        // Set up the preferred chord buttons
        switchAb = (SwitchCompat) V.findViewById(R.id.switchAb);
        switchBb = (SwitchCompat) V.findViewById(R.id.switchBb);
        switchDb = (SwitchCompat) V.findViewById(R.id.switchDb);
        switchEb = (SwitchCompat) V.findViewById(R.id.switchEb);
        switchGb = (SwitchCompat) V.findViewById(R.id.switchGb);
        switchAbm = (SwitchCompat) V.findViewById(R.id.switchAbm);
        switchBbm = (SwitchCompat) V.findViewById(R.id.switchBbm);
        switchDbm = (SwitchCompat) V.findViewById(R.id.switchDbm);
        switchEbm = (SwitchCompat) V.findViewById(R.id.switchEbm);
        switchGbm = (SwitchCompat) V.findViewById(R.id.switchGbm);

        setSwitches(FullscreenActivity.prefChord_Aflat_Gsharp, switchAb);
        setSwitches(FullscreenActivity.prefChord_Bflat_Asharp, switchBb);
        setSwitches(FullscreenActivity.prefChord_Dflat_Csharp, switchDb);
        setSwitches(FullscreenActivity.prefChord_Eflat_Dsharp, switchEb);
        setSwitches(FullscreenActivity.prefChord_Gflat_Fsharp, switchGb);
        setSwitches(FullscreenActivity.prefChord_Aflatm_Gsharpm, switchAbm);
        setSwitches(FullscreenActivity.prefChord_Bflatm_Asharpm, switchBbm);
        setSwitches(FullscreenActivity.prefChord_Dflatm_Csharpm, switchDbm);
        setSwitches(FullscreenActivity.prefChord_Eflatm_Dsharpm, switchEbm);
        setSwitches(FullscreenActivity.prefChord_Gflatm_Fsharpm, switchGbm);

        radioGroup = (RadioGroup) V.findViewById(R.id.chordFormat);
        radioGroup2 = (RadioGroup) V.findViewById(R.id.chordFormat_decideaction);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.chordFormat1:
                        numeral = "1";
                        break;
                    case R.id.chordFormat2:
                        numeral = "2";
                        break;
                    case R.id.chordFormat3:
                        numeral = "3";
                        break;
                    case R.id.chordFormat4:
                        numeral = "4";
                        break;
                    case R.id.chordFormat5:
                        numeral = "5";
                        break;
                }
            }
        });

        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.chordformat_check:
                        numeral2 = "N";
                        break;
                    case R.id.chordformat_default:
                        numeral2 = "Y";
                        break;
                }
            }
        });

        RadioButton radioButton1 = (RadioButton) V.findViewById(R.id.chordFormat1);
        RadioButton radioButton2 = (RadioButton) V.findViewById(R.id.chordFormat2);
        RadioButton radioButton3 = (RadioButton) V.findViewById(R.id.chordFormat3);
        RadioButton radioButton4 = (RadioButton) V.findViewById(R.id.chordFormat4);
        RadioButton radioButton5 = (RadioButton) V.findViewById(R.id.chordFormat5);
        RadioButton radioButton6 = (RadioButton) V.findViewById(R.id.chordformat_check);
        RadioButton radioButton7 = (RadioButton) V.findViewById(R.id.chordformat_default);

        // Set the appropriate radiobutton
        switch (FullscreenActivity.chordFormat) {
            case "1":
                radioButton1.setChecked(true);
                break;
            case "2":
                radioButton2.setChecked(true);
                break;
            case "3":
                radioButton3.setChecked(true);
                break;
            case "4":
                radioButton4.setChecked(true);
                break;
            case "5":
                radioButton5.setChecked(true);
                break;
        }

        if (FullscreenActivity.alwaysPreferredChordFormat.equals("N")) {
            radioButton6.setChecked(true);
        } else {
            radioButton7.setChecked(true);
        }

        return V;
    }

    public void setSwitches(String what, SwitchCompat myswitch) {
        if (what.equals("b")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                myswitch.setChecked(false);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                myswitch.setChecked(true);
            }
        }
    }

    public void exitChordFormat() {
        if (switchAb.isChecked()) {
            FullscreenActivity.prefChord_Aflat_Gsharp = "#";
        } else {
            FullscreenActivity.prefChord_Aflat_Gsharp = "b";
        }
        if (switchBb.isChecked()) {
            FullscreenActivity.prefChord_Bflat_Asharp = "#";
        } else {
            FullscreenActivity.prefChord_Bflat_Asharp = "b";
        }
        if (switchDb.isChecked()) {
            FullscreenActivity.prefChord_Dflat_Csharp = "#";
        } else {
            FullscreenActivity.prefChord_Dflat_Csharp = "b";
        }
        if (switchEb.isChecked()) {
            FullscreenActivity.prefChord_Eflat_Dsharp = "#";
        } else {
            FullscreenActivity.prefChord_Eflat_Dsharp = "b";
        }
        if (switchGb.isChecked()) {
            FullscreenActivity.prefChord_Gflat_Fsharp = "#";
        } else {
            FullscreenActivity.prefChord_Gflat_Fsharp = "b";
        }
        if (switchAbm.isChecked()) {
            FullscreenActivity.prefChord_Aflatm_Gsharpm = "#";
        } else {
            FullscreenActivity.prefChord_Aflatm_Gsharpm = "b";
        }
        if (switchBbm.isChecked()) {
            FullscreenActivity.prefChord_Bflatm_Asharpm = "#";
        } else {
            FullscreenActivity.prefChord_Bflatm_Asharpm = "b";
        }
        if (switchDbm.isChecked()) {
            FullscreenActivity.prefChord_Dflatm_Csharpm = "#";
        } else {
            FullscreenActivity.prefChord_Dflatm_Csharpm = "b";
        }
        if (switchEbm.isChecked()) {
            FullscreenActivity.prefChord_Eflatm_Dsharpm = "#";
        } else {
            FullscreenActivity.prefChord_Eflatm_Dsharpm = "b";
        }
        if (switchGbm.isChecked()) {
            FullscreenActivity.prefChord_Gflatm_Fsharpm = "#";
        } else {
            FullscreenActivity.prefChord_Gflatm_Fsharpm = "b";
        }

        FullscreenActivity.chordFormat = numeral;
        FullscreenActivity.alwaysPreferredChordFormat = numeral2;
        Preferences.savePreferences();
        dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}

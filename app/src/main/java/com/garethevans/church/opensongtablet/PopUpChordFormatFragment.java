package com.garethevans.church.opensongtablet;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpChordFormatFragment extends DialogFragment {

    static PopUpChordFormatFragment newInstance() {
        PopUpChordFormatFragment frag;
        frag = new PopUpChordFormatFragment();
        return frag;
    }

    private RadioGroup chordFormat;
    private RadioButton chordFormat1, chordFormat2, chordFormat3, chordFormat4, chordFormat5, chordFormat6;
    private SwitchCompat switchAb, switchBb, switchDb, switchEb, switchGb, switchAbm, switchBbm,
            switchDbm, switchEbm, switchGbm, assumePreferred_SwitchCompat;

    private Preferences preferences;

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

        View V = inflater.inflate(R.layout.popup_chordformat, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.choosechordformat));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            exitChordFormat();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();

        // Identify the views
        identifyViews(V);

        // Set the values based on preferences
        setButtons();

        // Set the listeners
        setListeners();

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void identifyViews(View V) {
        switchAb = V.findViewById(R.id.switchAb);
        switchBb = V.findViewById(R.id.switchBb);
        switchDb = V.findViewById(R.id.switchDb);
        switchEb = V.findViewById(R.id.switchEb);
        switchGb = V.findViewById(R.id.switchGb);
        switchAbm = V.findViewById(R.id.switchAbm);
        switchBbm = V.findViewById(R.id.switchBbm);
        switchDbm = V.findViewById(R.id.switchDbm);
        switchEbm = V.findViewById(R.id.switchEbm);
        switchGbm = V.findViewById(R.id.switchGbm);
        assumePreferred_SwitchCompat = V.findViewById(R.id.assumePreferred_SwitchCompat);
        chordFormat = V.findViewById(R.id.chordFormat);
        chordFormat1 = V.findViewById(R.id.chordFormat1);
        chordFormat2 = V.findViewById(R.id.chordFormat2);
        chordFormat3 = V.findViewById(R.id.chordFormat3);
        chordFormat4 = V.findViewById(R.id.chordFormat4);
        chordFormat5 = V.findViewById(R.id.chordFormat5);
        chordFormat6 = V.findViewById(R.id.chordFormat6);
    }

    private void setButtons() {
        setSwitches(preferences.getMyPreferenceBoolean(getContext(),"prefKeyAb",true), switchAb);
        setSwitches(preferences.getMyPreferenceBoolean(getContext(),"prefKeyBb",true), switchBb);
        setSwitches(preferences.getMyPreferenceBoolean(getContext(),"prefKeyDb",false), switchDb);
        setSwitches(preferences.getMyPreferenceBoolean(getContext(),"prefKeyEb",true), switchEb);
        setSwitches(preferences.getMyPreferenceBoolean(getContext(),"prefKeyGb",false), switchGb);
        setSwitches(preferences.getMyPreferenceBoolean(getContext(),"prefKeyAbm",false), switchAbm);
        setSwitches(preferences.getMyPreferenceBoolean(getContext(),"prefKeyBbm",true), switchBbm);
        setSwitches(preferences.getMyPreferenceBoolean(getContext(),"prefKeyDbm",false), switchDbm);
        setSwitches(preferences.getMyPreferenceBoolean(getContext(),"prefKeyEbm",true), switchEbm);
        setSwitches(preferences.getMyPreferenceBoolean(getContext(),"prefKeyGbm",false), switchGbm);
        setSwitches(preferences.getMyPreferenceBoolean(getContext(), "chordFormatUsePreferred",true), assumePreferred_SwitchCompat);

        switch (preferences.getMyPreferenceInt(getContext(),"chordFormat",1)) {
            case 1:
                chordFormat1.setChecked(true);
                break;
            case 2:
                chordFormat2.setChecked(true);
                break;
            case 3:
                chordFormat3.setChecked(true);
                break;
            case 4:
                chordFormat4.setChecked(true);
                break;
            case 5:
                chordFormat5.setChecked(true);
                break;
            case 6:
                chordFormat6.setChecked(true);
                break;
        }
    }

    private void setListeners() {
        switchAb.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"prefKeyAb",!isChecked));
        switchBb.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"prefKeyBb",!isChecked));
        switchDb.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"prefKeyDb",!isChecked));
        switchEb.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"prefKeyEb",!isChecked));
        switchGb.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"prefKeyGb",!isChecked));
        switchAbm.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"prefKeyAbm",!isChecked));
        switchBbm.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"prefKeyBbm",!isChecked));
        switchDbm.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"prefKeyDbm",!isChecked));
        switchEbm.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"prefKeyEbm",!isChecked));
        switchGbm.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"prefKeyGbm",!isChecked));
        assumePreferred_SwitchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"chordFormatUsePreferred",isChecked));
        chordFormat.setOnCheckedChangeListener((group, checkedId) -> {
            final int cf1 = R.id.chordFormat1;
            final int cf2 = R.id.chordFormat2;
            final int cf3 = R.id.chordFormat3;
            final int cf4 = R.id.chordFormat4;
            final int cf5 = R.id.chordFormat5;
            final int cf6 = R.id.chordFormat6;

            switch (checkedId) {
                case cf1:
                    preferences.setMyPreferenceInt(getContext(), "chordFormat", 1);
                    break;
                case cf2:
                    preferences.setMyPreferenceInt(getContext(), "chordFormat", 2);
                    break;
                case cf3:
                    preferences.setMyPreferenceInt(getContext(), "chordFormat", 3);
                    break;
                case cf4:
                    preferences.setMyPreferenceInt(getContext(), "chordFormat", 4);
                    break;
                case cf5:
                    preferences.setMyPreferenceInt(getContext(), "chordFormat", 5);
                    break;
                case cf6:
                    preferences.setMyPreferenceInt(getContext(), "chordFormat", 6);
                    break;
            }
        });
    }

    private void setSwitches(boolean prefb, SwitchCompat myswitch) {
        myswitch.setChecked(!prefb);
    }

    private void exitChordFormat() {
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}

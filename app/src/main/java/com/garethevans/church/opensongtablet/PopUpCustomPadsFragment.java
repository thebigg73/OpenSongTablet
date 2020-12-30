package com.garethevans.church.opensongtablet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class PopUpCustomPadsFragment extends DialogFragment {

    private ArrayList<String> padfiles;
    private Spinner padAb, padA, padBb, padB, padC, padDb, padD, padEb, padE, padF, padGb, padG,
            padAbm, padAm, padBbm, padBm, padCm, padDbm, padDm, padEbm, padEm, padFm, padGbm, padGm;
    private Preferences preferences;

    static PopUpCustomPadsFragment newInstance() {
        PopUpCustomPadsFragment frag;
        frag = new PopUpCustomPadsFragment();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().setCanceledOnTouchOutside(true);
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        View V = inflater.inflate(R.layout.popup_custompads, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.pad));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getContext());
            closeMe.setEnabled(false);
            doSave();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        // Initialise the views
        padAb = V.findViewById(R.id.padAb);
        padA = V.findViewById(R.id.padA);
        padBb = V.findViewById(R.id.padBb);
        padB = V.findViewById(R.id.padB);
        padC = V.findViewById(R.id.padC);
        padDb = V.findViewById(R.id.padDb);
        padD = V.findViewById(R.id.padD);
        padEb = V.findViewById(R.id.padEb);
        padE = V.findViewById(R.id.padE);
        padF = V.findViewById(R.id.padF);
        padGb = V.findViewById(R.id.padGb);
        padG = V.findViewById(R.id.padG);
        padAbm = V.findViewById(R.id.padAbm);
        padAm = V.findViewById(R.id.padAm);
        padBbm = V.findViewById(R.id.padBbm);
        padBm = V.findViewById(R.id.padBm);
        padCm = V.findViewById(R.id.padCm);
        padDbm = V.findViewById(R.id.padDbm);
        padDm = V.findViewById(R.id.padDm);
        padEbm = V.findViewById(R.id.padEbm);
        padEm = V.findViewById(R.id.padEm);
        padFm = V.findViewById(R.id.padFm);
        padGbm = V.findViewById(R.id.padGbm);
        padGm = V.findViewById(R.id.padGm);

        // Set up the spinners
        StorageAccess storageAccess = new StorageAccess();
        preferences = new Preferences();

        padfiles = storageAccess.listFilesInFolder(getContext(), preferences, "Pads", "");
        padfiles.add(0,getString(R.string.pad_auto));
        ArrayAdapter<String> aa = new ArrayAdapter<>(requireContext(),R.layout.my_spinner,padfiles);
        padAb.setAdapter(aa);
        padA.setAdapter(aa);
        padBb.setAdapter(aa);
        padB.setAdapter(aa);
        padC.setAdapter(aa);
        padDb.setAdapter(aa);
        padD.setAdapter(aa);
        padEb.setAdapter(aa);
        padE.setAdapter(aa);
        padF.setAdapter(aa);
        padGb.setAdapter(aa);
        padG.setAdapter(aa);
        padAbm.setAdapter(aa);
        padAm.setAdapter(aa);
        padBbm.setAdapter(aa);
        padBm.setAdapter(aa);
        padCm.setAdapter(aa);
        padDbm.setAdapter(aa);
        padDm.setAdapter(aa);
        padEbm.setAdapter(aa);
        padEm.setAdapter(aa);
        padFm.setAdapter(aa);
        padGbm.setAdapter(aa);
        padGm.setAdapter(aa);

        // Set the spinners to the default values
        // This returns an updated string in case the specified one is no longer valid
        
        // Get the user values and if it isn't found in the array, set it to auto
        setSpinnerVal(padAb,preferences.getMyPreferenceString(getContext(),"customPadAb",""));
        setSpinnerVal(padA,preferences.getMyPreferenceString(getContext(),"customPadA",""));
        setSpinnerVal(padBb,preferences.getMyPreferenceString(getContext(),"customPadBb",""));
        setSpinnerVal(padB,preferences.getMyPreferenceString(getContext(),"customPadB",""));
        setSpinnerVal(padC,preferences.getMyPreferenceString(getContext(),"customPadC",""));
        setSpinnerVal(padDb,preferences.getMyPreferenceString(getContext(),"customPadDb",""));
        setSpinnerVal(padD,preferences.getMyPreferenceString(getContext(),"customPadD",""));
        setSpinnerVal(padEb,preferences.getMyPreferenceString(getContext(),"customPadEb",""));
        setSpinnerVal(padE,preferences.getMyPreferenceString(getContext(),"customPadE",""));
        setSpinnerVal(padF,preferences.getMyPreferenceString(getContext(),"customPadF",""));
        setSpinnerVal(padGb,preferences.getMyPreferenceString(getContext(),"customPadGb",""));
        setSpinnerVal(padG,preferences.getMyPreferenceString(getContext(),"customPadG",""));
        setSpinnerVal(padAbm,preferences.getMyPreferenceString(getContext(),"customPadAbm",""));
        setSpinnerVal(padAm,preferences.getMyPreferenceString(getContext(),"customPadAm",""));
        setSpinnerVal(padBbm,preferences.getMyPreferenceString(getContext(),"customPadBbm",""));
        setSpinnerVal(padBm,preferences.getMyPreferenceString(getContext(),"customPadBm",""));
        setSpinnerVal(padCm,preferences.getMyPreferenceString(getContext(),"customPadCm",""));
        setSpinnerVal(padDbm,preferences.getMyPreferenceString(getContext(),"customPadDbm",""));
        setSpinnerVal(padDm,preferences.getMyPreferenceString(getContext(),"customPadDm",""));
        setSpinnerVal(padEbm,preferences.getMyPreferenceString(getContext(),"customPadEbm",""));
        setSpinnerVal(padEm,preferences.getMyPreferenceString(getContext(),"customPadEm",""));
        setSpinnerVal(padFm,preferences.getMyPreferenceString(getContext(),"customPadFm",""));
        setSpinnerVal(padGbm,preferences.getMyPreferenceString(getContext(),"customPadGbm",""));
        setSpinnerVal(padGm,preferences.getMyPreferenceString(getContext(),"customPadGm",""));

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void setSpinnerVal (Spinner s, String v) {
        try {
            if (v == null || v.isEmpty()) {
                // Set the pad to 'Auto' - this is the first option
                s.setSelection(0);
            } else {
                int pos = padfiles.indexOf(v);
                if (pos < 0) {
                    // User did have a file specified, but it is no longer there
                    pos = 0;
                }
                s.setSelection(pos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSpinnerVal (Spinner s) {
        String v;
        try {
            v = s.getSelectedItem().toString();
            if (v.equals(getString(R.string.pad_auto))) {
                v = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            v = null;
        }
        return v;
    }

    private void doSave() {
        try {
            // Get the spinner values and save them
            preferences.setMyPreferenceString(getContext(),"customPadAb",getSpinnerVal(padAb));
            preferences.setMyPreferenceString(getContext(),"customPadA", getSpinnerVal(padA));
            preferences.setMyPreferenceString(getContext(),"customPadBb",getSpinnerVal(padBb));
            preferences.setMyPreferenceString(getContext(),"customPadB", getSpinnerVal(padB));
            preferences.setMyPreferenceString(getContext(),"customPadC", getSpinnerVal(padC));
            preferences.setMyPreferenceString(getContext(),"customPadDb",getSpinnerVal(padDb));
            preferences.setMyPreferenceString(getContext(),"customPadD", getSpinnerVal(padD));
            preferences.setMyPreferenceString(getContext(),"customPadEb",getSpinnerVal(padEb));
            preferences.setMyPreferenceString(getContext(),"customPadE", getSpinnerVal(padE));
            preferences.setMyPreferenceString(getContext(),"customPadF", getSpinnerVal(padF));
            preferences.setMyPreferenceString(getContext(),"customPadGb",getSpinnerVal(padGb));
            preferences.setMyPreferenceString(getContext(),"customPadG", getSpinnerVal(padG));
            preferences.setMyPreferenceString(getContext(),"customPadAbm",getSpinnerVal(padAbm));
            preferences.setMyPreferenceString(getContext(),"customPadAm", getSpinnerVal(padAm));
            preferences.setMyPreferenceString(getContext(),"customPadBbm",getSpinnerVal(padBbm));
            preferences.setMyPreferenceString(getContext(),"customPadBm", getSpinnerVal(padBm));
            preferences.setMyPreferenceString(getContext(),"customPadCm", getSpinnerVal(padCm));
            preferences.setMyPreferenceString(getContext(),"customPadDbm",getSpinnerVal(padDbm));
            preferences.setMyPreferenceString(getContext(),"customPadDm", getSpinnerVal(padDm));
            preferences.setMyPreferenceString(getContext(),"customPadEbm",getSpinnerVal(padEbm));
            preferences.setMyPreferenceString(getContext(),"customPadEm", getSpinnerVal(padEm));
            preferences.setMyPreferenceString(getContext(),"customPadFm", getSpinnerVal(padFm));
            preferences.setMyPreferenceString(getContext(),"customPadGbm",getSpinnerVal(padGbm));
            preferences.setMyPreferenceString(getContext(),"customPadGm", getSpinnerVal(padGm));
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

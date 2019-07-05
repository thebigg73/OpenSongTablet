package com.garethevans.church.opensongtablet;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

public class PopUpCustomPadsFragment extends DialogFragment {

    private ArrayList<String> padfiles;
    private Spinner padAb, padA, padBb, padB, padC, padDb, padD, padEb, padE, padF, padGb, padG,
            padAbm, padAm, padBbm, padBm, padCm, padDbm, padDm, padEbm, padEm, padFm, padGbm, padGm;
    StorageAccess storageAccess;
    Preferences preferences;

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
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View V = inflater.inflate(R.layout.popup_custompads, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.pad));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                doSave();
            }
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
        storageAccess = new StorageAccess();
        preferences = new Preferences();

        padfiles = storageAccess.listFilesInFolder(getActivity(), preferences, "Pads", "");
        padfiles.add(0,getActivity().getString(R.string.pad_auto));
        ArrayAdapter<String> aa = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,padfiles);
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
        setSpinnerVal(padAb,preferences.getMyPreferenceString(getActivity(),"customPadAb",""));
        setSpinnerVal(padA,preferences.getMyPreferenceString(getActivity(),"customPadA",""));
        setSpinnerVal(padBb,preferences.getMyPreferenceString(getActivity(),"customPadBb",""));
        setSpinnerVal(padB,preferences.getMyPreferenceString(getActivity(),"customPadB",""));
        setSpinnerVal(padC,preferences.getMyPreferenceString(getActivity(),"customPadC",""));
        setSpinnerVal(padDb,preferences.getMyPreferenceString(getActivity(),"customPadDb",""));
        setSpinnerVal(padD,preferences.getMyPreferenceString(getActivity(),"customPadD",""));
        setSpinnerVal(padEb,preferences.getMyPreferenceString(getActivity(),"customPadEb",""));
        setSpinnerVal(padE,preferences.getMyPreferenceString(getActivity(),"customPadE",""));
        setSpinnerVal(padF,preferences.getMyPreferenceString(getActivity(),"customPadF",""));
        setSpinnerVal(padGb,preferences.getMyPreferenceString(getActivity(),"customPadGb",""));
        setSpinnerVal(padG,preferences.getMyPreferenceString(getActivity(),"customPadG",""));
        setSpinnerVal(padAbm,preferences.getMyPreferenceString(getActivity(),"customPadAbm",""));
        setSpinnerVal(padAm,preferences.getMyPreferenceString(getActivity(),"customPadAm",""));
        setSpinnerVal(padBbm,preferences.getMyPreferenceString(getActivity(),"customPadBbm",""));
        setSpinnerVal(padBm,preferences.getMyPreferenceString(getActivity(),"customPadBm",""));
        setSpinnerVal(padCm,preferences.getMyPreferenceString(getActivity(),"customPadCm",""));
        setSpinnerVal(padDbm,preferences.getMyPreferenceString(getActivity(),"customPadDbm",""));
        setSpinnerVal(padDm,preferences.getMyPreferenceString(getActivity(),"customPadDm",""));
        setSpinnerVal(padEbm,preferences.getMyPreferenceString(getActivity(),"customPadEbm",""));
        setSpinnerVal(padEm,preferences.getMyPreferenceString(getActivity(),"customPadEm",""));
        setSpinnerVal(padFm,preferences.getMyPreferenceString(getActivity(),"customPadFm",""));
        setSpinnerVal(padGbm,preferences.getMyPreferenceString(getActivity(),"customPadGbm",""));
        setSpinnerVal(padGm,preferences.getMyPreferenceString(getActivity(),"customPadGm",""));

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
            if (v.equals(Objects.requireNonNull(getActivity()).getString(R.string.pad_auto))) {
                v = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            v = null;
        }
        return v;
    }

    void doSave() {
        try {
            // Get the spinner values and save them
            preferences.setMyPreferenceString(getActivity(),"customPadAb",getSpinnerVal(padAb));
            preferences.setMyPreferenceString(getActivity(),"customPadA", getSpinnerVal(padA));
            preferences.setMyPreferenceString(getActivity(),"customPadBb",getSpinnerVal(padBb));
            preferences.setMyPreferenceString(getActivity(),"customPadB", getSpinnerVal(padB));
            preferences.setMyPreferenceString(getActivity(),"customPadC", getSpinnerVal(padC));
            preferences.setMyPreferenceString(getActivity(),"customPadDb",getSpinnerVal(padDb));
            preferences.setMyPreferenceString(getActivity(),"customPadD", getSpinnerVal(padD));
            preferences.setMyPreferenceString(getActivity(),"customPadEb",getSpinnerVal(padEb));
            preferences.setMyPreferenceString(getActivity(),"customPadE", getSpinnerVal(padE));
            preferences.setMyPreferenceString(getActivity(),"customPadF", getSpinnerVal(padF));
            preferences.setMyPreferenceString(getActivity(),"customPadGb",getSpinnerVal(padGb));
            preferences.setMyPreferenceString(getActivity(),"customPadG", getSpinnerVal(padG));
            preferences.setMyPreferenceString(getActivity(),"customPadAbm",getSpinnerVal(padAbm));
            preferences.setMyPreferenceString(getActivity(),"customPadAm", getSpinnerVal(padAm));
            preferences.setMyPreferenceString(getActivity(),"customPadBbm",getSpinnerVal(padBbm));
            preferences.setMyPreferenceString(getActivity(),"customPadBm", getSpinnerVal(padBm));
            preferences.setMyPreferenceString(getActivity(),"customPadCm", getSpinnerVal(padCm));
            preferences.setMyPreferenceString(getActivity(),"customPadDbm",getSpinnerVal(padDbm));
            preferences.setMyPreferenceString(getActivity(),"customPadDm", getSpinnerVal(padDm));
            preferences.setMyPreferenceString(getActivity(),"customPadEbm",getSpinnerVal(padEbm));
            preferences.setMyPreferenceString(getActivity(),"customPadEm", getSpinnerVal(padEm));
            preferences.setMyPreferenceString(getActivity(),"customPadFm", getSpinnerVal(padFm));
            preferences.setMyPreferenceString(getActivity(),"customPadGbm",getSpinnerVal(padGbm));
            preferences.setMyPreferenceString(getActivity(),"customPadGm", getSpinnerVal(padGm));
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class PopUpCustomPadsFragment extends DialogFragment {

    ArrayList<String> filenames;
    Spinner padAb, padA, padBb, padB, padC, padDb, padD, padEb, padE, padF, padGb, padG,
            padAbm, padAm, padBbm, padBm, padCm, padDbm, padDm, padEbm, padEm, padFm, padGbm, padGm;
    StorageAccess storageAccess;

    static PopUpCustomPadsFragment newInstance() {
        PopUpCustomPadsFragment frag;
        frag = new PopUpCustomPadsFragment();
        return frag;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View V = inflater.inflate(R.layout.popup_custompads, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.pad));
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
        saveMe.setVisibility(View.GONE);

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
        ArrayList<String> padfiles = storageAccess.listFilesInFolder(getActivity(),"Pads","");
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
        FullscreenActivity.customPadAb = setSpinnerVal(padAb,FullscreenActivity.customPadAb);
        FullscreenActivity.customPadA = setSpinnerVal(padA,FullscreenActivity.customPadA);
        FullscreenActivity.customPadBb = setSpinnerVal(padBb,FullscreenActivity.customPadBb);
        FullscreenActivity.customPadB = setSpinnerVal(padB,FullscreenActivity.customPadB);
        FullscreenActivity.customPadC = setSpinnerVal(padC,FullscreenActivity.customPadC);
        FullscreenActivity.customPadDb = setSpinnerVal(padDb,FullscreenActivity.customPadDb);
        FullscreenActivity.customPadD = setSpinnerVal(padD,FullscreenActivity.customPadD);
        FullscreenActivity.customPadEb = setSpinnerVal(padEb,FullscreenActivity.customPadEb);
        FullscreenActivity.customPadE = setSpinnerVal(padE,FullscreenActivity.customPadE);
        FullscreenActivity.customPadF = setSpinnerVal(padF,FullscreenActivity.customPadF);
        FullscreenActivity.customPadGb = setSpinnerVal(padGb,FullscreenActivity.customPadGb);
        FullscreenActivity.customPadG = setSpinnerVal(padG,FullscreenActivity.customPadG);
        FullscreenActivity.customPadAbm = setSpinnerVal(padAbm,FullscreenActivity.customPadAbm);
        FullscreenActivity.customPadAm = setSpinnerVal(padAm,FullscreenActivity.customPadAm);
        FullscreenActivity.customPadBbm = setSpinnerVal(padBbm,FullscreenActivity.customPadBbm);
        FullscreenActivity.customPadBm = setSpinnerVal(padBm,FullscreenActivity.customPadBm);
        FullscreenActivity.customPadCm = setSpinnerVal(padCm,FullscreenActivity.customPadCm);
        FullscreenActivity.customPadDbm = setSpinnerVal(padDbm,FullscreenActivity.customPadDbm);
        FullscreenActivity.customPadDm = setSpinnerVal(padDm,FullscreenActivity.customPadDm);
        FullscreenActivity.customPadEbm = setSpinnerVal(padEbm,FullscreenActivity.customPadEbm);
        FullscreenActivity.customPadEm = setSpinnerVal(padEm,FullscreenActivity.customPadEm);
        FullscreenActivity.customPadFm = setSpinnerVal(padFm,FullscreenActivity.customPadFm);
        FullscreenActivity.customPadGbm = setSpinnerVal(padGbm,FullscreenActivity.customPadGbm);
        FullscreenActivity.customPadGm = setSpinnerVal(padGm,FullscreenActivity.customPadGm);

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    String setSpinnerVal (Spinner s, String v) {
        try {
            if (v == null) {
                // Set the pad to 'Auto' - this is the first option
                s.setSelection(0);
            } else {
                int pos = filenames.indexOf(v);
                if (pos < 0) {
                    // User did have a file specified, but it is no longer there
                    pos = 0;
                    v = null;
                }
                s.setSelection(pos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    String getSpinnerVal (Spinner s) {
        String v;
        try {
            v = s.getSelectedItem().toString();
            if (v != null && v.equals(getActivity().getString(R.string.pad_auto))) {
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
            FullscreenActivity.customPadAb = getSpinnerVal(padAb);
            FullscreenActivity.customPadA = getSpinnerVal(padA);
            FullscreenActivity.customPadBb = getSpinnerVal(padBb);
            FullscreenActivity.customPadB = getSpinnerVal(padB);
            FullscreenActivity.customPadC = getSpinnerVal(padC);
            FullscreenActivity.customPadDb = getSpinnerVal(padDb);
            FullscreenActivity.customPadD = getSpinnerVal(padD);
            FullscreenActivity.customPadEb = getSpinnerVal(padEb);
            FullscreenActivity.customPadE = getSpinnerVal(padE);
            FullscreenActivity.customPadF = getSpinnerVal(padF);
            FullscreenActivity.customPadGb = getSpinnerVal(padGb);
            FullscreenActivity.customPadG = getSpinnerVal(padG);
            FullscreenActivity.customPadAbm = getSpinnerVal(padAbm);
            FullscreenActivity.customPadAm = getSpinnerVal(padAm);
            FullscreenActivity.customPadBbm = getSpinnerVal(padBbm);
            FullscreenActivity.customPadBm = getSpinnerVal(padBm);
            FullscreenActivity.customPadCm = getSpinnerVal(padCm);
            FullscreenActivity.customPadDbm = getSpinnerVal(padDbm);
            FullscreenActivity.customPadDm = getSpinnerVal(padDm);
            FullscreenActivity.customPadEbm = getSpinnerVal(padEbm);
            FullscreenActivity.customPadEm = getSpinnerVal(padEm);
            FullscreenActivity.customPadFm = getSpinnerVal(padFm);
            FullscreenActivity.customPadGbm = getSpinnerVal(padGbm);
            FullscreenActivity.customPadGm = getSpinnerVal(padGm);

            Preferences.savePreferences();
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

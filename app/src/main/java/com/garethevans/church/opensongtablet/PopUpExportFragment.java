package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpExportFragment extends DialogFragment {

    static PopUpExportFragment newInstance() {
        PopUpExportFragment frag;
        frag = new PopUpExportFragment();
        return frag;
    }

    public interface MyInterface {
        void doExport();
    }

    private MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyInterface) context;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    private CheckBox exportImageCheckBox;

    private Preferences preferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }
        View V = inflater.inflate(R.layout.popup_exportselection, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        if (FullscreenActivity.whattodo.equals("customise_exportsong")) {
            title.setText(getString(R.string.exportcurrentsong));
        } else {
            title.setText(getString(R.string.exportsavedset));
        }
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe, getContext());
            saveMe.setEnabled(false);
            doExportPrepare();
        });

        preferences = new Preferences();

        // Initialise the views
        CheckBox exportOpenSongAppSetCheckBox = V.findViewById(R.id.exportOpenSongAppSetCheckBox);
        CheckBox exportOpenSongAppCheckBox = V.findViewById(R.id.exportOpenSongAppCheckBox);
        CheckBox exportDesktopCheckBox = V.findViewById(R.id.exportDesktopCheckBox);
        CheckBox exportTextCheckBox = V.findViewById(R.id.exportTextCheckBox);
        CheckBox exportChordProCheckBox = V.findViewById(R.id.exportChordProCheckBox);
        CheckBox exportOnSongCheckBox = V.findViewById(R.id.exportOnSongCheckBox);
        exportImageCheckBox = V.findViewById(R.id.exportImageCheckBox);
        CheckBox exportPDFCheckBox = V.findViewById(R.id.exportPDFCheckBox);

        // Hide the ones we don't need
        if (FullscreenActivity.whattodo.equals("customise_exportsong")) {
            exportOpenSongAppSetCheckBox.setVisibility(View.GONE);
        } else {
            exportChordProCheckBox.setVisibility(View.GONE);
            exportOnSongCheckBox.setVisibility(View.GONE);
            exportImageCheckBox.setVisibility(View.GONE);
        }

        if (!StaticVariables.whichMode.equals("Performance")) {
            exportImageCheckBox.setVisibility(View.GONE);
        }

        // Set the checkboxes to their last set value
        exportOpenSongAppSetCheckBox.setChecked(preferences.getMyPreferenceBoolean(getContext(),"exportOpenSongAppSet",true));
        exportOpenSongAppCheckBox.setChecked(preferences.getMyPreferenceBoolean(getContext(),"exportOpenSongApp",true));
        // IV - preference name corrections
        exportDesktopCheckBox.setChecked(preferences.getMyPreferenceBoolean(getContext(),"exportDesktop",false));
        exportTextCheckBox.setChecked(preferences.getMyPreferenceBoolean(getContext(),"exportText",true));
        exportChordProCheckBox.setChecked(preferences.getMyPreferenceBoolean(getContext(),"exportChordPro",false));
        exportOnSongCheckBox.setChecked(preferences.getMyPreferenceBoolean(getContext(),"exportOnSong",false));
        exportImageCheckBox.setChecked(preferences.getMyPreferenceBoolean(getContext(),"exportImage",false));
        exportPDFCheckBox.setChecked(preferences.getMyPreferenceBoolean(getContext(),"exportPDF",false));

        // Set the listeners
        exportOpenSongAppSetCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getContext(),"exportOpenSongAppSet",b));
        exportOpenSongAppCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getContext(),"exportOpenSongApp",b));
        exportDesktopCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getContext(),"exportDesktop",b));
        exportTextCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getContext(),"exportText",b));
        exportChordProCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getContext(),"exportChordPro",b));
        exportOnSongCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getContext(),"exportOnSong",b));
        exportImageCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            //StaticVariables.thisSongScale !=null && StaticVariables.thisSongScale.equals("Y") &&
            if (StaticVariables.whichMode.equals("Performance")) {
                preferences.setMyPreferenceBoolean(getContext(),"exportImage",b);
            } else {
                preferences.setMyPreferenceBoolean(getContext(),"exportImage",false);
                exportImageCheckBox.setChecked(false);
                StaticVariables.myToastMessage = requireActivity().getString(R.string.switchtoperformmode);
                ShowToast.showToast(getContext());
            }
        });
        exportPDFCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getContext(),"exportPDF",b));

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void doExportPrepare() {
        if (mListener!=null) {
            mListener.doExport();
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}
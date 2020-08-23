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

import java.util.Objects;

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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_exportselection, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        if (FullscreenActivity.whattodo.equals("customise_exportsong")) {
            title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.exportcurrentsong));
        } else {
            title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.exportsavedset));
        }
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getActivity());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe, getActivity());
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
            //exportPDFCheckBox.setVisibility(View.GONE);  Not using screenshot anymore, so can create
        }

        if (!StaticVariables.whichMode.equals("Performance")) {
            exportImageCheckBox.setVisibility(View.GONE);
            exportPDFCheckBox.setVisibility(View.GONE);
        }

        // Set the checkboxes to their last set value
        exportOpenSongAppSetCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"exportOpenSongAppSet",true));
        exportOpenSongAppCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"exportOpenSongApp",true));
        // IV - preference name corrections
        exportDesktopCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"exportDesktop",false));
        exportTextCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"exportText",true));
        exportChordProCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"exportChordPro",false));
        exportOnSongCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"exportOnSong",false));
        exportImageCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"exportImage",false));
        exportPDFCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"exportPDF",false));

        // Set the listeners
        exportOpenSongAppSetCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(),"exportOpenSongAppSet",b));
        exportOpenSongAppCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(),"exportOpenSongApp",b));
        exportDesktopCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(),"exportDesktop",b));
        exportTextCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(),"exportText",b));
        exportChordProCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(),"exportChordPro",b));
        exportOnSongCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(),"exportOnSong",b));
        exportImageCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            //StaticVariables.thisSongScale !=null && StaticVariables.thisSongScale.equals("Y") &&
            if (StaticVariables.whichMode.equals("Performance")) {
                preferences.setMyPreferenceBoolean(getActivity(),"exportImage",b);
            } else {
                preferences.setMyPreferenceBoolean(getActivity(),"exportImage",false);
                exportImageCheckBox.setChecked(false);
                StaticVariables.myToastMessage = requireActivity().getString(R.string.switchtoperformmode);
                ShowToast.showToast(getActivity());
            }
        });
        exportPDFCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(),"exportPDF",b));

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
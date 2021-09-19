/*
package com.garethevans.church.opensongtablet;


public class PopUpExportFragment extends DialogFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_exportselection, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        if (StaticVariables.whattodo.equals("customise_exportsong")) {
            title.setText(requireActivity().getResources().getString(R.string.exportcurrentsong));
        } else {
            title.setText(requireActivity().getResources().getString(R.string.exportsavedset));
        }
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            _CustomAnimations.animateFAB(closeMe, getActivity());
            closeMe.setEnabled(false);
            dismiss();
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            _CustomAnimations.animateFAB(saveMe, getActivity());
            saveMe.setEnabled(false);
            doExportPrepare();
        });

        preferences = new _Preferences();


        // Hide the ones we don't need
        if (StaticVariables.whattodo.equals("customise_exportsong")) {
            exportOpenSongAppSetCheckBox.setVisibility(View.GONE);
        } else {
            exportChordProCheckBox.setVisibility(View.GONE);
            exportOnSongCheckBox.setVisibility(View.GONE);
            exportImageCheckBox.setVisibility(View.GONE);
            exportPDFCheckBox.setVisibility(View.GONE);
        }

        if (!StaticVariables.whichMode.equals("Performance")) {
            exportImageCheckBox.setVisibility(View.GONE);
            exportPDFCheckBox.setVisibility(View.GONE);
        }

        // Set the checkboxes to their last set value
        exportOpenSongAppSetCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(), "exportOpenSongAppSet", true));
        exportOpenSongAppCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(), "exportOpenSongApp", true));
        // IV - preference name corrections
        exportDesktopCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"exportDesktop",false));
        exportTextCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"exportText",true));
        exportChordProCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(), "exportChordPro", false));
        exportOnSongCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(), "exportOnSong", false));
        exportImageCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(), "exportImage", false));
        exportPDFCheckBox.setChecked(preferences.getMyPreferenceBoolean(getActivity(), "exportPDF", false));

        // Set the listeners
        exportOpenSongAppSetCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(), "exportOpenSongAppSet", b));
        exportOpenSongAppCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(), "exportOpenSongApp", b));
        exportDesktopCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(), "exportDesktop", b));
        exportTextCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(), "exportText", b));
        exportChordProCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(), "exportChordPro", b));
        exportOnSongCheckBox.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(), "exportOnSong", b));
        exportImageCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (StaticVariables.thisSongScale != null && StaticVariables.thisSongScale.equals("Y") && StaticVariables.whichMode.equals("Performance")) {
                preferences.setMyPreferenceBoolean(getActivity(), "exportImage", b);
            } else {
                preferences.setMyPreferenceBoolean(getActivity(), "exportImage", false);
                exportImageCheckBox.setChecked(false);
                StaticVariables.myToastMessage = requireActivity().getString(R.string.toobig);
                _ShowToast.showToast(getActivity());
            }
        });
        exportPDFCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (StaticVariables.thisSongScale != null && StaticVariables.thisSongScale.equals("Y") &&
                    StaticVariables.whichMode.equals("Performance")) {
                preferences.setMyPreferenceBoolean(getActivity(), "exportPDF", b);
            } else {
                preferences.setMyPreferenceBoolean(getActivity(), "exportPDF", false);
                exportPDFCheckBox.setChecked(false);
                StaticVariables.myToastMessage = requireActivity().getString(R.string.toobig);
                _ShowToast.showToast(getActivity());
            }
        });

        _PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog(), preferences);

        return V;
    }

    private void doExportPrepare() {
        if (mListener != null) {
            mListener.doExport();
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}*/

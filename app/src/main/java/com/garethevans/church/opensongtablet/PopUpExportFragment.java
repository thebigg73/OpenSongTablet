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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

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

    CheckBox exportOpenSongAppSetCheckBox;
    CheckBox exportOpenSongAppCheckBox;
    CheckBox exportDesktopCheckBox;
    CheckBox exportTextCheckBox;
    CheckBox exportChordProCheckBox;
    CheckBox exportOnSongCheckBox;
    CheckBox exportImageCheckBox;
    CheckBox exportPDFCheckBox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_exportselection, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        if (FullscreenActivity.whattodo.equals("customise_exportsong")) {
            title.setText(getActivity().getResources().getString(R.string.exportcurrentsong));
        } else {
            title.setText(getActivity().getResources().getString(R.string.exportsavedset));
        }
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
                CustomAnimations.animateFAB(saveMe, getActivity());
                saveMe.setEnabled(false);
                doExportPrepare();
            }
        });

        // Initialise the views
        exportOpenSongAppSetCheckBox = V.findViewById(R.id.exportOpenSongAppSetCheckBox);
        exportOpenSongAppCheckBox = V.findViewById(R.id.exportOpenSongAppCheckBox);
        exportDesktopCheckBox = V.findViewById(R.id.exportDesktopCheckBox);
        exportTextCheckBox = V.findViewById(R.id.exportTextCheckBox);
        exportChordProCheckBox = V.findViewById(R.id.exportChordProCheckBox);
        exportOnSongCheckBox = V.findViewById(R.id.exportOnSongCheckBox);
        exportImageCheckBox = V.findViewById(R.id.exportImageCheckBox);
        exportPDFCheckBox = V.findViewById(R.id.exportPDFCheckBox);

        // Hide the ones we don't need
        if (FullscreenActivity.whattodo.equals("customise_exportsong")) {
            exportOpenSongAppSetCheckBox.setVisibility(View.GONE);
        } else {
            exportChordProCheckBox.setVisibility(View.GONE);
            exportOnSongCheckBox.setVisibility(View.GONE);
            exportImageCheckBox.setVisibility(View.GONE);
            exportPDFCheckBox.setVisibility(View.GONE);
        }

        if (!FullscreenActivity.whichMode.equals("Performance")) {
            exportImageCheckBox.setVisibility(View.GONE);
            exportPDFCheckBox.setVisibility(View.GONE);
            FullscreenActivity.exportImage = false;
            FullscreenActivity.exportPDF = false;
            Preferences.savePreferences();
        }

        // Set the checkboxes to their last set value
        exportOpenSongAppSetCheckBox.setChecked(FullscreenActivity.exportOpenSongAppSet);
        exportOpenSongAppCheckBox.setChecked(FullscreenActivity.exportOpenSongApp);
        exportDesktopCheckBox.setChecked(FullscreenActivity.exportDesktop);
        exportTextCheckBox.setChecked(FullscreenActivity.exportText);
        exportChordProCheckBox.setChecked(FullscreenActivity.exportChordPro);
        exportOnSongCheckBox.setChecked(FullscreenActivity.exportOnSong);
        exportImageCheckBox.setChecked(FullscreenActivity.exportImage);
        exportPDFCheckBox.setChecked(FullscreenActivity.exportPDF);

        // Initialise any previous text created for the export files
        FullscreenActivity.exportOpenSongAppSet_String = "";
        FullscreenActivity.exportOpenSongApp_String = "";
        FullscreenActivity.exportDesktop_String = "";
        FullscreenActivity.exportText_String = "";
        FullscreenActivity.exportChordPro_String = "";
        FullscreenActivity.exportOnSong_String = "";


        // Set the listeners
        exportOpenSongAppSetCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.exportOpenSongAppSet = b;
            }
        });
        exportOpenSongAppCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.exportOpenSongApp = b;
            }
        });
        exportDesktopCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.exportDesktop = b;
            }
        });
        exportTextCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.exportText = b;
            }
        });
        exportChordProCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.exportChordPro = b;
            }
        });
        exportOnSongCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.exportOnSong = b;
            }
        });
        exportImageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (FullscreenActivity.thissong_scale.equals("Y") && FullscreenActivity.whichMode.equals("Performance")) {
                    FullscreenActivity.exportImage = b;
                } else {
                    FullscreenActivity.exportImage = false;
                    exportImageCheckBox.setChecked(false);
                    FullscreenActivity.myToastMessage = getActivity().getString(R.string.toobig);
                    ShowToast.showToast(getActivity());
                }
            }
        });
        exportPDFCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (FullscreenActivity.thissong_scale.equals("Y") && FullscreenActivity.whichMode.equals("Performance")) {
                    FullscreenActivity.exportPDF = b;
                } else {
                    FullscreenActivity.exportPDF = false;
                    exportPDFCheckBox.setChecked(false);
                    FullscreenActivity.myToastMessage = getActivity().getString(R.string.toobig);
                    ShowToast.showToast(getActivity());
                }
            }
        });

        return V;
    }

    public void doExportPrepare() {
        if (mListener!=null) {
            mListener.doExport();
            dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
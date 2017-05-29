package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.TextView;

public class PopUpExtraInfoFragment extends DialogFragment {

    static PopUpExtraInfoFragment newInstance() {
        PopUpExtraInfoFragment frag;
        frag = new PopUpExtraInfoFragment();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
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

    SwitchCompat nextSongOnOff_Switch;
    SwitchCompat nextSongTopBottom_Switch;
    SwitchCompat stickyNotesOnOff_Switch;
    SwitchCompat stickyNotesTopBottom_Switch;

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            title.setText(getActivity().getResources().getString(R.string.extra));
            final FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CustomAnimations.animateFAB(closeMe,getActivity());
                    closeMe.setEnabled(false);
                    mListener.refreshAll();
                    dismiss();
                }
            });
            FloatingActionButton saveMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.saveMe);
            saveMe.setVisibility(View.GONE);
        } else {
            getDialog().setTitle(getActivity().getResources().getString(R.string.extra));
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
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_extrainfo, container, false);

        // Initialise the views
        nextSongOnOff_Switch = (SwitchCompat) V.findViewById(R.id.nextSongOnOff_Switch);
        nextSongTopBottom_Switch = (SwitchCompat) V.findViewById(R.id.nextSongTopBottom_Switch);
        stickyNotesOnOff_Switch = (SwitchCompat) V.findViewById(R.id.stickyNotesOnOff_Switch);
        stickyNotesTopBottom_Switch = (SwitchCompat) V.findViewById(R.id.stickyNotesTopBottom_Switch);

        // Set the default values
        switch (FullscreenActivity.showNextInSet) {
            case "off":
                nextSongOnOff_Switch.setChecked(false);
                nextSongTopBottom_Switch.setVisibility(View.GONE);
                break;

            case "bottom":
            default:
                nextSongOnOff_Switch.setChecked(true);
                nextSongTopBottom_Switch.setChecked(true);
                nextSongTopBottom_Switch.setVisibility(View.VISIBLE);
                break;

            case "top":
                nextSongOnOff_Switch.setChecked(true);
                nextSongTopBottom_Switch.setChecked(false);
                nextSongTopBottom_Switch.setVisibility(View.VISIBLE);
                break;
        }
        switch (FullscreenActivity.toggleAutoSticky) {

            case "N":
                stickyNotesOnOff_Switch.setChecked(false);
                stickyNotesTopBottom_Switch.setVisibility(View.GONE);
                break;

            case "B":
            default:
                stickyNotesOnOff_Switch.setChecked(true);
                stickyNotesTopBottom_Switch.setChecked(true);
                stickyNotesTopBottom_Switch.setVisibility(View.VISIBLE);
                break;

            case "T":
                stickyNotesOnOff_Switch.setChecked(true);
                stickyNotesTopBottom_Switch.setChecked(false);
                stickyNotesTopBottom_Switch.setVisibility(View.VISIBLE);
                break;
        }

        // Set the listeners
        nextSongOnOff_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    nextSongTopBottom_Switch.setVisibility((View.VISIBLE));
                    if (nextSongTopBottom_Switch.isChecked()) {
                        FullscreenActivity.showNextInSet = "bottom";
                    } else {
                        FullscreenActivity.showNextInSet = "top";
                    }
                } else {
                    nextSongTopBottom_Switch.setVisibility((View.GONE));
                    FullscreenActivity.showNextInSet = "off";
                }
                Preferences.savePreferences();
            }
        });
        nextSongTopBottom_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    FullscreenActivity.showNextInSet = "bottom";
                } else {
                    FullscreenActivity.showNextInSet = "top";
                }
                Preferences.savePreferences();
            }
        });
        stickyNotesOnOff_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    stickyNotesTopBottom_Switch.setVisibility((View.VISIBLE));
                    if (stickyNotesTopBottom_Switch.isChecked()) {
                        FullscreenActivity.toggleAutoSticky = "B";
                    } else {
                        FullscreenActivity.toggleAutoSticky = "T";
                    }
                } else {
                    stickyNotesTopBottom_Switch.setVisibility((View.GONE));
                    FullscreenActivity.toggleAutoSticky = "N";
                }
                Preferences.savePreferences();
            }
        });
        stickyNotesTopBottom_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    FullscreenActivity.toggleAutoSticky = "B";
                } else {
                    FullscreenActivity.toggleAutoSticky = "T";
                }
                Preferences.savePreferences();
            }
        });

        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
        mListener.refreshAll();
    }

}
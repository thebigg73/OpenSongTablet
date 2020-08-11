package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private SwitchCompat nextSongOnOff_Switch, nextSongTopBottom_Switch,stickyNotesOnOff_Switch,
            stickyNotesFloat_Switch,stickyNotesTopBottom_Switch, highlightNotesOnOff_Switch,
            stickyBlockInfo;
    private SeekBar stickyNotesTime_SeekBar, highlightTime_SeekBar;
    private TextView stickyNotesTime_TextView, stickNotesTimeInfo_TextView, highlightTime_TextView,
            highlightTimeInfo_TextView;
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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_extrainfo, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getContext().getResources().getString(R.string.extra));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getActivity());
            closeMe.setEnabled(false);
            mListener.refreshAll();
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();

        // Initialise the views
        nextSongOnOff_Switch = V.findViewById(R.id.nextSongOnOff_Switch);
        nextSongTopBottom_Switch = V.findViewById(R.id.nextSongTopBottom_Switch);
        stickyNotesOnOff_Switch = V.findViewById(R.id.stickyNotesOnOff_Switch);
        stickyNotesTopBottom_Switch = V.findViewById(R.id.stickyNotesTopBottom_Switch);
        stickyNotesFloat_Switch = V.findViewById(R.id.stickyNotesFloat_Switch);
        stickyNotesTime_SeekBar = V.findViewById(R.id.stickyNotesTime_SeekBar);
        stickyNotesTime_TextView = V.findViewById(R.id.stickyNotesTime_TextView);
        stickNotesTimeInfo_TextView = V.findViewById(R.id.stickNotesTimeInfo_TextView);
        highlightNotesOnOff_Switch = V.findViewById(R.id.highlightNotesOnOff_Switch);
        highlightTime_TextView = V.findViewById(R.id.highlightTime_TextView);
        highlightTime_SeekBar = V.findViewById(R.id.highlightTime_SeekBar);
        highlightTimeInfo_TextView = V.findViewById(R.id.highlightTimeInfo_TextView);
        // Add new option for showing songsheet style block info
        stickyBlockInfo = V.findViewById(R.id.stickyBlockInfo);

        // Set the default values
        showNextButtons();
        showStickyButtons();
        showHighlightButtons();

        // Set the listeners
        nextSongOnOff_Switch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                if (nextSongTopBottom_Switch.isChecked()) {
                    preferences.setMyPreferenceString(getActivity(),"displayNextInSet","B");
                } else {
                    preferences.setMyPreferenceString(getActivity(),"displayNextInSet","T");
                }
            } else {
                preferences.setMyPreferenceString(getActivity(),"displayNextInSet","N");
            }
            showNextButtons();
        });
        nextSongTopBottom_Switch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                preferences.setMyPreferenceString(getActivity(),"displayNextInSet","B");
            } else {
                preferences.setMyPreferenceString(getActivity(),"displayNextInSet","T");
            }
        });
        stickyNotesOnOff_Switch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                if (stickyNotesFloat_Switch.isChecked()) {
                    preferences.setMyPreferenceString(getActivity(),"stickyAutoDisplay","F");
                } else if (stickyNotesTopBottom_Switch.isChecked()) {
                    preferences.setMyPreferenceString(getActivity(),"stickyAutoDisplay","B");
                } else {
                    preferences.setMyPreferenceString(getActivity(),"stickyAutoDisplay","T");
                }
            } else {
                preferences.setMyPreferenceString(getActivity(),"stickyAutoDisplay","N");
            }
            showStickyButtons();
        });
        stickyNotesFloat_Switch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                preferences.setMyPreferenceString(getActivity(),"stickyAutoDisplay","F");
            } else if (stickyNotesOnOff_Switch.isChecked()) {
                if (stickyNotesTopBottom_Switch.isChecked()) {
                    preferences.setMyPreferenceString(getActivity(),"stickyAutoDisplay","B");
                } else {
                    preferences.setMyPreferenceString(getActivity(),"stickyAutoDisplay","T");
                }
            } else {
                preferences.setMyPreferenceString(getActivity(),"stickyAutoDisplay","N");
            }
            showStickyButtons();
        });
        stickyBlockInfo.setOnCheckedChangeListener((view,checked) -> preferences.setMyPreferenceBoolean(getActivity(),"stickyBlockInfo",checked));


        stickyNotesTopBottom_Switch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                preferences.setMyPreferenceString(getActivity(),"stickyAutoDisplay","B");
            } else {
                preferences.setMyPreferenceString(getActivity(),"stickyAutoDisplay","T");
            }
        });
        stickyNotesTime_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String s;
                if (i==0) {
                    s = getContext().getResources().getString(R.string.on);
                } else {
                    s = i + " s";
                }
                stickyNotesTime_TextView.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferences.setMyPreferenceInt(getActivity(),"timeToDisplaySticky",seekBar.getProgress());
            }
        });
        highlightNotesOnOff_Switch.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getActivity(),"drawingAutoDisplay",b);
            showHighlightButtons();
        });
        highlightTime_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String s;
                if (i==0) {
                    s = getContext().getResources().getString(R.string.on);
                } else {
                    s = i + " s";
                }
                highlightTime_TextView.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferences.setMyPreferenceInt(getActivity(),"timeToDisplayHighlighter",seekBar.getProgress());
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void showNextButtons() {
        switch (preferences.getMyPreferenceString(getActivity(),"displayNextInSet","B")) {
            case "N":
                nextSongOnOff_Switch.setChecked(false);
                nextSongTopBottom_Switch.setVisibility(View.GONE);
                break;

            case "B":
            default:
                nextSongOnOff_Switch.setChecked(true);
                nextSongTopBottom_Switch.setChecked(true);
                nextSongTopBottom_Switch.setVisibility(View.VISIBLE);
                break;

            case "T":
                nextSongOnOff_Switch.setChecked(true);
                nextSongTopBottom_Switch.setChecked(false);
                nextSongTopBottom_Switch.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void showStickyButtons() {

        // GE - To switch on/off the song info block at the top
        stickyBlockInfo.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"stickyBlockInfo",false));

        switch (preferences.getMyPreferenceString(getActivity(),"stickyAutoDisplay","F")) {

            case "N":
                stickyNotesOnOff_Switch.setChecked(false);
                stickyNotesFloat_Switch.setVisibility(View.GONE);
                stickyNotesTime_SeekBar.setVisibility(View.GONE);
                stickyNotesTime_TextView.setVisibility(View.GONE);
                stickNotesTimeInfo_TextView.setVisibility(View.GONE);
                stickyNotesTopBottom_Switch.setVisibility(View.GONE);
                break;

            case "B":
            default:
                stickyNotesOnOff_Switch.setChecked(true);
                stickyNotesTopBottom_Switch.setChecked(true);
                stickyNotesFloat_Switch.setVisibility(View.VISIBLE);
                stickyNotesFloat_Switch.setChecked(false);
                stickyNotesTime_SeekBar.setVisibility(View.GONE);
                stickyNotesTime_TextView.setVisibility(View.GONE);
                stickNotesTimeInfo_TextView.setVisibility(View.GONE);
                stickyNotesTopBottom_Switch.setVisibility(View.VISIBLE);
                break;

            case "T":
                stickyNotesOnOff_Switch.setChecked(true);
                stickyNotesTopBottom_Switch.setChecked(false);
                stickyNotesFloat_Switch.setVisibility(View.VISIBLE);
                stickyNotesFloat_Switch.setChecked(false);
                stickyNotesTime_SeekBar.setVisibility(View.GONE);
                stickyNotesTime_TextView.setVisibility(View.GONE);
                stickNotesTimeInfo_TextView.setVisibility(View.GONE);
                stickyNotesTopBottom_Switch.setVisibility(View.VISIBLE);
                break;

            case "F":
                stickyNotesOnOff_Switch.setChecked(true);
                stickyNotesFloat_Switch.setChecked(true);
                stickyNotesFloat_Switch.setVisibility(View.VISIBLE);
                stickyNotesTime_SeekBar.setVisibility(View.VISIBLE);
                stickyNotesTime_SeekBar.setProgress(preferences.getMyPreferenceInt(getActivity(),"timeToDisplaySticky",5));
                String s;
                if (preferences.getMyPreferenceInt(getActivity(),"timeToDisplaySticky",5)==0) {
                    s = getContext().getResources().getString(R.string.on);
                } else {
                    s = preferences.getMyPreferenceInt(getActivity(),"timeToDisplaySticky",5) + " s";
                }
                stickyNotesTime_TextView.setText(s);
                stickyNotesTime_TextView.setVisibility(View.VISIBLE);
                stickNotesTimeInfo_TextView.setVisibility(View.VISIBLE);
                stickyNotesTopBottom_Switch.setVisibility(View.GONE);
                break;
        }
    }

    private void showHighlightButtons() {
        highlightNotesOnOff_Switch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"drawingAutoDisplay",true));
        if (!preferences.getMyPreferenceBoolean(getActivity(),"drawingAutoDisplay",true)) {
            highlightTime_TextView.setVisibility(View.GONE);
            highlightTimeInfo_TextView.setVisibility(View.GONE);
            highlightTime_SeekBar.setVisibility(View.GONE);
        } else {
            highlightTime_TextView.setVisibility(View.VISIBLE);
            highlightTimeInfo_TextView.setVisibility(View.VISIBLE);
            highlightTime_SeekBar.setVisibility(View.VISIBLE);
        }
        int time = preferences.getMyPreferenceInt(getActivity(),"timeToDisplayHighlighter",0);
        highlightTime_SeekBar.setProgress(time);
        String s;
        if (time==0) {
            s = getContext().getResources().getString(R.string.on);
        } else {
            s = time + " s";
        }
        highlightTime_TextView.setText(s);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
        mListener.refreshAll();
    }

}
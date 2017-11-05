package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.SeekBar;
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

    SwitchCompat nextSongOnOff_Switch, nextSongTopBottom_Switch,stickyNotesOnOff_Switch,
            stickyNotesFloat_Switch,stickyNotesTopBottom_Switch, highlightNotesOnOff_Switch;
    SeekBar stickyNotesTime_SeekBar, highlightTime_SeekBar;
    TextView stickyNotesTime_TextView, stickNotesTimeInfo_TextView, highlightTime_TextView,
            highlightTimeInfo_TextView;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_extrainfo, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.extra));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                mListener.refreshAll();
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

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

        // Set the default values
        showNextButtons();
        showStickyButtons();
        showHighlightButtons();

        // Set the listeners
        nextSongOnOff_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    //nextSongTopBottom_Switch.setVisibility((View.VISIBLE));
                    if (nextSongTopBottom_Switch.isChecked()) {
                        FullscreenActivity.showNextInSet = "bottom";
                    } else {
                        FullscreenActivity.showNextInSet = "top";
                    }
                } else {
                    //nextSongTopBottom_Switch.setVisibility((View.GONE));
                    FullscreenActivity.showNextInSet = "off";
                }
                Preferences.savePreferences();
                showNextButtons();
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
                    //stickyNotesTopBottom_Switch.setVisibility((View.VISIBLE));
                    if (stickyNotesFloat_Switch.isChecked()) {
                        FullscreenActivity.toggleAutoSticky = "F";
                    } else if (stickyNotesTopBottom_Switch.isChecked()) {
                        FullscreenActivity.toggleAutoSticky = "B";
                    } else {
                        FullscreenActivity.toggleAutoSticky = "T";
                    }
                } else {
                    //stickyNotesTopBottom_Switch.setVisibility((View.GONE));
                    FullscreenActivity.toggleAutoSticky = "N";
                }
                Preferences.savePreferences();
                showStickyButtons();
            }
        });
        stickyNotesFloat_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    FullscreenActivity.toggleAutoSticky = "F";
                } else if (stickyNotesOnOff_Switch.isChecked()) {
                    if (stickyNotesTopBottom_Switch.isChecked()) {
                        FullscreenActivity.toggleAutoSticky = "B";
                    } else {
                        FullscreenActivity.toggleAutoSticky = "T";
                    }
                } else {
                    FullscreenActivity.toggleAutoSticky = "N";
                }
                Preferences.savePreferences();
                showStickyButtons();
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
        stickyNotesTime_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                FullscreenActivity.stickyNotesShowSecs = i;
                String s;
                if (i==0) {
                    s = getActivity().getResources().getString(R.string.on);
                } else {
                    s = i + " s";
                }
                stickyNotesTime_TextView.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });
        highlightNotesOnOff_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.toggleAutoHighlight = b;
                Preferences.savePreferences();
                showHighlightButtons();
            }
        });
        highlightTime_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                FullscreenActivity.highlightShowSecs = i;
                String s;
                if (i==0) {
                    s = getActivity().getResources().getString(R.string.on);
                } else {
                    s = i + " s";
                }
                highlightTime_TextView.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });

        return V;
    }

    public void showNextButtons() {
        switch (FullscreenActivity.showNextInSet) {
            case "off":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    nextSongOnOff_Switch.setChecked(false);
                }
                nextSongTopBottom_Switch.setVisibility(View.GONE);
                break;

            case "bottom":
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    nextSongOnOff_Switch.setChecked(true);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    nextSongTopBottom_Switch.setChecked(true);
                }
                nextSongTopBottom_Switch.setVisibility(View.VISIBLE);
                break;

            case "top":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    nextSongOnOff_Switch.setChecked(true);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    nextSongTopBottom_Switch.setChecked(false);
                }
                nextSongTopBottom_Switch.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void showStickyButtons() {

        switch (FullscreenActivity.toggleAutoSticky) {

            case "N":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    stickyNotesOnOff_Switch.setChecked(false);
                }
                stickyNotesFloat_Switch.setVisibility(View.GONE);
                stickyNotesTime_SeekBar.setVisibility(View.GONE);
                stickyNotesTime_TextView.setVisibility(View.GONE);
                stickNotesTimeInfo_TextView.setVisibility(View.GONE);
                stickyNotesTopBottom_Switch.setVisibility(View.GONE);
                break;

            case "B":
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    stickyNotesOnOff_Switch.setChecked(true);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    stickyNotesTopBottom_Switch.setChecked(true);
                }
                stickyNotesFloat_Switch.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    stickyNotesFloat_Switch.setChecked(false);
                }
                stickyNotesTime_SeekBar.setVisibility(View.GONE);
                stickyNotesTime_TextView.setVisibility(View.GONE);
                stickNotesTimeInfo_TextView.setVisibility(View.GONE);
                stickyNotesTopBottom_Switch.setVisibility(View.VISIBLE);
                break;

            case "T":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    stickyNotesOnOff_Switch.setChecked(true);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    stickyNotesTopBottom_Switch.setChecked(false);
                }
                stickyNotesFloat_Switch.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    stickyNotesFloat_Switch.setChecked(false);
                }
                stickyNotesTime_SeekBar.setVisibility(View.GONE);
                stickyNotesTime_TextView.setVisibility(View.GONE);
                stickNotesTimeInfo_TextView.setVisibility(View.GONE);
                stickyNotesTopBottom_Switch.setVisibility(View.VISIBLE);
                break;

            case "F":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    stickyNotesOnOff_Switch.setChecked(true);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    stickyNotesFloat_Switch.setChecked(true);
                }
                stickyNotesFloat_Switch.setVisibility(View.VISIBLE);
                stickyNotesTime_SeekBar.setVisibility(View.VISIBLE);
                stickyNotesTime_SeekBar.setProgress(FullscreenActivity.stickyNotesShowSecs);
                String s;
                if (FullscreenActivity.stickyNotesShowSecs==0) {
                    s = getActivity().getResources().getString(R.string.on);
                } else {
                    s = FullscreenActivity.stickyNotesShowSecs + " s";
                }
                stickyNotesTime_TextView.setText(s);
                stickyNotesTime_TextView.setVisibility(View.VISIBLE);
                stickNotesTimeInfo_TextView.setVisibility(View.VISIBLE);
                stickyNotesTopBottom_Switch.setVisibility(View.GONE);
                break;
        }

    }

    public void showHighlightButtons() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            highlightNotesOnOff_Switch.setChecked(FullscreenActivity.toggleAutoHighlight);
        }
        if (!FullscreenActivity.toggleAutoHighlight) {
            highlightTime_TextView.setVisibility(View.GONE);
            highlightTimeInfo_TextView.setVisibility(View.GONE);
            highlightTime_SeekBar.setVisibility(View.GONE);
        } else {
            highlightTime_TextView.setVisibility(View.VISIBLE);
            highlightTimeInfo_TextView.setVisibility(View.VISIBLE);
            highlightTime_SeekBar.setVisibility(View.VISIBLE);
        }
        highlightTime_SeekBar.setProgress(FullscreenActivity.highlightShowSecs);
        String s;
        if (FullscreenActivity.highlightShowSecs==0) {
            s = getActivity().getResources().getString(R.string.on);
        } else {
            s = FullscreenActivity.highlightShowSecs + " s";
        }
        highlightTime_TextView.setText(s);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
        mListener.refreshAll();
    }

}
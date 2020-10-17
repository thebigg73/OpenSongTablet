package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpScalingFragment extends DialogFragment {

    private MyInterface mListener;
    private LinearLayout maxAutoScale_Group;
    private SeekBar fontsize_seekbar;
    private SeekBar maxAutoScale_seekBar;
    private SeekBar minAutoScale_seekBar;
    private TextView fontsize_TextView;
    private TextView maxAutoScale_TextView;
    private TextView minAutoScale_TextView;
    private SwitchCompat switchAutoScaleOnOff_SwitchCompat;
    private SwitchCompat switchAutoScaleWidthFull_SwitchCompat;
    private SeekBar stagemode_scale_SeekBar;
    private TextView stagemode_scale_TextView;
    private SwitchCompat switchAutoScaleMaxColumns_SwitchCompat;
    private Preferences preferences;

    static PopUpScalingFragment newInstance() {
        PopUpScalingFragment frag;
        frag = new PopUpScalingFragment();
        return frag;
    }

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }
        View V = inflater.inflate(R.layout.popup_scaling, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getResources().getString(R.string.autoscale_toggle));
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
        maxAutoScale_seekBar = V.findViewById(R.id.maxAutoScale_seekBar);
        minAutoScale_seekBar = V.findViewById(R.id.minAutoScale_seekBar);
        LinearLayout fontsize_change_group = V.findViewById(R.id.fontsize_change_group);
        maxAutoScale_Group = V.findViewById(R.id.maxAutoScale_Group);
        fontsize_seekbar = V.findViewById(R.id.fontsize_seekbar);
        maxAutoScale_TextView = V.findViewById(R.id.maxAutoScale_TextView);
        minAutoScale_TextView = V.findViewById(R.id.minAutoScale_TextView);
        fontsize_TextView = V.findViewById(R.id.fontsize_TextView);
        switchAutoScaleOnOff_SwitchCompat = V.findViewById(R.id.switchAutoScaleOnOff_SwitchCompat);
        switchAutoScaleWidthFull_SwitchCompat = V.findViewById(R.id.switchAutoScaleWidthFull_SwitchCompat);
        switchAutoScaleMaxColumns_SwitchCompat = V.findViewById(R.id.switchAutoScaleMaxColumns_SwitchCompat);
        SwitchCompat overrideFull_Switch = V.findViewById(R.id.overrideFull_Switch);
        SwitchCompat overrideWidth_Switch = V.findViewById(R.id.overrideWidth_Switch);
        LinearLayout stagemode_scale = V.findViewById(R.id.stagemode_scale);
        stagemode_scale_SeekBar = V.findViewById(R.id.stagemode_scale_SeekBar);
        stagemode_scale_TextView = V.findViewById(R.id.stagemode_scale_TextView);

        if (!StaticVariables.whichMode.equals("Stage")) {
            stagemode_scale.setVisibility(View.GONE);
        } else {
            stagemode_scale.setVisibility(View.VISIBLE);
        }

        // Set the seekbars to the currently chosen values;
        switch (preferences.getMyPreferenceString(getActivity(),"songAutoScale","W")) {
            case "W":
            default:
                // Width only
                switchAutoScaleOnOff_SwitchCompat.setChecked(true);
                switchAutoScaleWidthFull_SwitchCompat.setChecked(false);
                switchAutoScaleWidthFull_SwitchCompat.setVisibility(View.VISIBLE);
                maxAutoScale_Group.setVisibility(View.VISIBLE);
                break;
            case "Y":
                // Full
                switchAutoScaleOnOff_SwitchCompat.setChecked(true);
                switchAutoScaleWidthFull_SwitchCompat.setChecked(true);
                switchAutoScaleWidthFull_SwitchCompat.setVisibility(View.VISIBLE);
                maxAutoScale_Group.setVisibility(View.VISIBLE);
                break;
            case "N":
                // Off
                switchAutoScaleOnOff_SwitchCompat.setChecked(false);
                switchAutoScaleWidthFull_SwitchCompat.setChecked(false);
                switchAutoScaleWidthFull_SwitchCompat.setVisibility(View.GONE);
                maxAutoScale_Group.setVisibility(View.GONE);
                break;
        }
        switchAutoScaleMaxColumns_SwitchCompat.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"songAutoScaleColumnMaximise",true));
        switchAutoScaleMaxColumns_SwitchCompat.setOnCheckedChangeListener((compoundButton, b) -> preferences.setMyPreferenceBoolean(getActivity(),"songAutoScaleColumnMaximise",b));

        overrideFull_Switch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"songAutoScaleOverrideFull",true));
        overrideWidth_Switch.setChecked(preferences.getMyPreferenceBoolean(getActivity(), "songAutoScaleOverrideWidth", false));
        setupfontsizeseekbar();
        setupmaxfontsizeseekbar();
        setupminfontsizeseekbar();
        setupstagemodescaleseekbar();

        fontsize_change_group.setVisibility(View.VISIBLE);

        fontsize_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatefontsize();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updatefontsize();
            }
        });
        maxAutoScale_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updatemaxfontsize();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updatefontsize();
            }
        });
        minAutoScale_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateminfontsize();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updatefontsize();
            }
        });
        overrideFull_Switch.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getActivity(),"songAutoScaleOverrideFull",isChecked));
        overrideWidth_Switch.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getActivity(), "songAutoScaleOverrideWidth", isChecked));
        switchAutoScaleOnOff_SwitchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                switchAutoScaleWidthFull_SwitchCompat.setEnabled(true);
                switchAutoScaleWidthFull_SwitchCompat.setVisibility(View.VISIBLE);
                maxAutoScale_Group.setVisibility(View.VISIBLE);
                if (switchAutoScaleWidthFull_SwitchCompat.isChecked()) {
                    preferences.setMyPreferenceString(getActivity(),"songAutoScale","Y");
                } else {
                    preferences.setMyPreferenceString(getActivity(),"songAutoScale","W");
                }
            } else {
                preferences.setMyPreferenceString(getActivity(),"songAutoScale","N");
                switchAutoScaleWidthFull_SwitchCompat.setVisibility(View.GONE);
                maxAutoScale_Group.setVisibility(View.GONE);
            }
            maxColsSwitchVisibilty();
        });

        switchAutoScaleWidthFull_SwitchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b && switchAutoScaleOnOff_SwitchCompat.isChecked()) {
                preferences.setMyPreferenceString(getActivity(),"songAutoScale","Y");
                maxAutoScale_Group.setVisibility(View.VISIBLE);
            } else if (!b &&switchAutoScaleOnOff_SwitchCompat.isChecked()) {
                preferences.setMyPreferenceString(getActivity(),"songAutoScale","W");
                maxAutoScale_Group.setVisibility(View.VISIBLE);
            } else {
                preferences.setMyPreferenceString(getActivity(),"songAutoScale","N");
                maxAutoScale_Group.setVisibility(View.GONE);
            }
            maxColsSwitchVisibilty();
        });

        stagemode_scale_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updatestagemodescale(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mListener!=null) {
                    mListener.refreshAll();
                }
            }
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void maxColsSwitchVisibilty() {
        if (preferences.getMyPreferenceString(getActivity(), "songAutoScale", "W").equals("Y")) {
            switchAutoScaleMaxColumns_SwitchCompat.setVisibility(View.VISIBLE);
        } else {
            switchAutoScaleMaxColumns_SwitchCompat.setVisibility(View.GONE);
        }
    }

    private void setupfontsizeseekbar() {
        // Seekbar size is 80 wide
        // Add 4 to all values - min is then 4, max is then 84
        if (preferences.getMyPreferenceFloat(getActivity(),"fontSize",42.0f)<4) {
            preferences.setMyPreferenceFloat(getActivity(),"fontSize",4.0f);
        } else if (preferences.getMyPreferenceFloat(getActivity(),"fontSize",42.0f)>84) {
            preferences.setMyPreferenceFloat(getActivity(),"fontSize",84.0f);
        }

        int progressbar = (int) preferences.getMyPreferenceFloat(getActivity(),"fontSize",42.0f) - 4;
        String text = preferences.getMyPreferenceFloat(getActivity(),"fontSize",42.0f) + " sp";

        fontsize_seekbar.setProgress(progressbar);
        fontsize_TextView.setText(text);
        fontsize_TextView.setTextSize(preferences.getMyPreferenceFloat(getActivity(),"fontSize",42.0f));
    }

    private void updatefontsize() {
        float val = fontsize_seekbar.getProgress() + 4.0f;
        preferences.setMyPreferenceFloat(getActivity(),"fontSize",val);
        String text = (int) val + " sp";
        fontsize_TextView.setText(text);
        fontsize_TextView.setTextSize(val);
    }

    private void setupmaxfontsizeseekbar() {
        // Seekbar size is 60 wide
        // Subtract 20 from all values - min is then 0, max is then 60
        // Actual mMaxFontSize is between 20 and 80 though
        if (preferences.getMyPreferenceFloat(getActivity(),"fontSizeMax",50.0f)<20) {
            preferences.setMyPreferenceFloat(getActivity(),"fontSizeMax",20.0f);
        } else if (preferences.getMyPreferenceFloat(getActivity(),"fontSizeMax",50.0f)>80) {
            preferences.setMyPreferenceFloat(getActivity(),"fontSizeMax",80.0f);
        }

        int progressbar = (int) preferences.getMyPreferenceFloat(getActivity(),"fontSizeMax",50.0f) - 20;
        String text = preferences.getMyPreferenceFloat(getActivity(),"fontSizeMax",50.0f) + " sp";

        maxAutoScale_seekBar.setProgress(progressbar);
        maxAutoScale_TextView.setText(text);
        maxAutoScale_TextView.setTextSize(preferences.getMyPreferenceFloat(getActivity(),"fontSizeMax",50.0f));
    }

    private void setupminfontsizeseekbar() {
        // Seekbar size is 28 wide
        // Subtract 2 from all values - min is then 0, max is then 28
        // Actual mMinFontSize is between 2 and 30 though
        if (preferences.getMyPreferenceFloat(getActivity(),"fontSizeMin",8.0f)<2) {
            preferences.setMyPreferenceFloat(getActivity(),"fontSizeMin",2.0f);
        } else if (preferences.getMyPreferenceFloat(getActivity(),"fontSizeMin",8.0f)>32) {
            preferences.setMyPreferenceFloat(getActivity(),"fontSizeMin",32.0f);
        }

        // Check the min is below the max!
        if (preferences.getMyPreferenceFloat(getActivity(),"fontSizeMin",8.0f)>preferences.getMyPreferenceFloat(getActivity(),"fontSizeMax",50.0f)) {
            int val = (int) preferences.getMyPreferenceFloat(getActivity(),"fontSizeMin",8.0f);
            val -= 1;
            preferences.setMyPreferenceFloat(getActivity(),"fontSizeMin",(float)val);
        }

        int progressbar = (int) preferences.getMyPreferenceFloat(getActivity(),"fontSizeMin",8.0f) - 2;
        String text = (progressbar+2) + " sp";

        minAutoScale_seekBar.setProgress(progressbar);
        minAutoScale_TextView.setText(text);
        minAutoScale_TextView.setTextSize((float)(progressbar+2));
    }

    private void setupstagemodescaleseekbar() {
        int val = (int) (preferences.getMyPreferenceFloat(getActivity(),"stageModeScale", 0.8f) * 100) - 20;
        stagemode_scale_SeekBar.setProgress(val);
        String valtext = (val + 20) + "%";
        stagemode_scale_TextView.setText(valtext);
    }

    private void updatestagemodescale(int i) {
        String valtext = (i+20) + "%";
        stagemode_scale_TextView.setText(valtext);
        preferences.setMyPreferenceFloat(getActivity(),"stageModeScale", (float)(i+20) / 100.0f);
    }

    private void updatemaxfontsize() {
        int val = maxAutoScale_seekBar.getProgress() + 20;
        preferences.setMyPreferenceFloat(getActivity(),"fontSizeMax",(float)val);
        String text = val + " sp";
        maxAutoScale_TextView.setText(text);
        maxAutoScale_TextView.setTextSize((float)val);
        // Check the min is below the max!
        if (preferences.getMyPreferenceFloat(getActivity(),"fontSizeMin",8.0f)>=val) {
            preferences.setMyPreferenceFloat(getActivity(),"fontSizeMin",(float) (val - 1));
            setupminfontsizeseekbar();
        }
    }

    private void updateminfontsize() {
        int val = (minAutoScale_seekBar.getProgress() + 2);
        String text = val + " sp";
        minAutoScale_TextView.setText(text);
        minAutoScale_TextView.setTextSize((float)val);
        // Check the min is below the max!
        if (val>=preferences.getMyPreferenceFloat(getActivity(),"fontSizeMax",50.0f)) {
            preferences.setMyPreferenceFloat(getActivity(),"fontSizeMax",(float)val + 1);
            setupmaxfontsizeseekbar();
        }
        preferences.setMyPreferenceFloat(getActivity(),"fontSizeMin",(float)val);
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

    public interface MyInterface {
        void refreshAll();
    }

}
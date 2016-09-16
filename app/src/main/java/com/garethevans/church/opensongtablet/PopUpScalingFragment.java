package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

//import android.widget.Switch;

public class PopUpScalingFragment extends DialogFragment {

    static PopUpScalingFragment newInstance() {
        PopUpScalingFragment frag;
        frag = new PopUpScalingFragment();
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

    SeekBar autoscale_seekBar;
    LinearLayout fontsize_change_group;
    LinearLayout maxAutoScale_Group;
    SeekBar fontsize_seekbar;
    SeekBar maxAutoScale_seekBar;
    SeekBar minAutoScale_seekBar;
    TextView fontsize_TextView;
    TextView maxAutoScale_TextView;
    TextView minAutoScale_TextView;
    TextView off_TextView;
    TextView on_TextView;
    TextView width_TextView;
    //Switch overrideFull_Switch;
    //Switch overrideWidth_Switch;
    SwitchCompat overrideFull_Switch;
    SwitchCompat overrideWidth_Switch;
    Button closebutton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.options_options_scale));
        View V = inflater.inflate(R.layout.popup_scaling, container, false);

        // Initialise the views
        autoscale_seekBar = (SeekBar) V.findViewById(R.id.autoscale_seekBar);
        maxAutoScale_seekBar = (SeekBar) V.findViewById(R.id.maxAutoScale_seekBar);
        minAutoScale_seekBar = (SeekBar) V.findViewById(R.id.minAutoScale_seekBar);
        fontsize_change_group = (LinearLayout) V.findViewById(R.id.fontsize_change_group);
        maxAutoScale_Group = (LinearLayout) V.findViewById(R.id.maxAutoScale_Group);
        fontsize_seekbar = (SeekBar) V.findViewById(R.id.fontsize_seekbar);
        maxAutoScale_TextView = (TextView) V.findViewById(R.id.maxAutoScale_TextView);
        minAutoScale_TextView = (TextView) V.findViewById(R.id.minAutoScale_TextView);
        fontsize_TextView = (TextView) V.findViewById(R.id.fontsize_TextView);
        off_TextView = (TextView) V.findViewById(R.id.off_TextView);
        on_TextView = (TextView) V.findViewById(R.id.on_TextView);
        width_TextView = (TextView) V.findViewById(R.id.width_TextView);
/*
        overrideFull_Switch = (Switch) V.findViewById(R.id.overrideFull_Switch);
        overrideWidth_Switch = (Switch) V.findViewById(R.id.overrideWidth_Switch);
*/
        overrideFull_Switch = (SwitchCompat) V.findViewById(R.id.overrideFull_Switch);
        overrideWidth_Switch = (SwitchCompat) V.findViewById(R.id.overrideWidth_Switch);
        closebutton = (Button) V.findViewById(R.id.closebutton);

        // Set up listeners
        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Preferences.savePreferences();
                mListener.refreshAll();
            }
        });
        autoscale_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                off_TextView.setTextColor(0xffffffff);
                width_TextView.setTextColor(0xffffffff);
                on_TextView.setTextColor(0xffffffff);
                if (seekBar.getProgress()==1) {
                    FullscreenActivity.toggleYScale = "W";
                    width_TextView.setTextColor(0xffffff00);
                    overrideFull_Switch.setEnabled(false);
                    overrideWidth_Switch.setEnabled(true);
                } else if (seekBar.getProgress()==2) {
                    FullscreenActivity.toggleYScale = "Y";
                    overrideFull_Switch.setEnabled(true);
                    overrideWidth_Switch.setEnabled(true);
                    on_TextView.setTextColor(0xffffff00);
                } else {
                    FullscreenActivity.toggleYScale = "N";
                    off_TextView.setTextColor(0xffffff00);
                    overrideFull_Switch.setEnabled(false);
                    overrideWidth_Switch.setEnabled(false);
                }
                checkforallowfontsizechange();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        off_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoscale_seekBar.setProgress(0);
            }
        });
        width_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoscale_seekBar.setProgress(1);
            }
        });
        on_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoscale_seekBar.setProgress(2);
            }
        });
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
        overrideFull_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FullscreenActivity.override_fullscale = isChecked;
            }
        });
        overrideWidth_Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FullscreenActivity.override_widthscale = isChecked;
            }
        });
        // Set the seekbars to the currently chosen values;
        checkforallowfontsizechange();
        switch (FullscreenActivity.toggleYScale) {
            case "W":
                // Width only
                autoscale_seekBar.setProgress(1);
                break;
            case "Y":
                // Full
                autoscale_seekBar.setProgress(2);
                break;
            default:
                // Off
                autoscale_seekBar.setProgress(0);
        }
        overrideFull_Switch.setChecked(FullscreenActivity.override_fullscale);
        overrideWidth_Switch.setChecked(FullscreenActivity.override_widthscale);
        setupfontsizeseekbar();
        setupmaxfontsizeseekbar();
        setupminfontsizeseekbar();

        return V;
    }

    public void checkforallowfontsizechange() {
        if (FullscreenActivity.toggleYScale.equals("N")) {
            fontsize_change_group.setAlpha(1.0f);
            fontsize_seekbar.setEnabled(true);
            maxAutoScale_Group.setAlpha(0.2f);
            maxAutoScale_seekBar.setEnabled(false);
        } else {
            fontsize_change_group.setAlpha(0.2f);
            fontsize_seekbar.setEnabled(false);
            maxAutoScale_Group.setAlpha(1.0f);
            maxAutoScale_seekBar.setEnabled(true);
        }
    }

    public void setupfontsizeseekbar() {
        // Seekbar size is 80 wide
        // Add 4 to all values - min is then 4, max is then 84
        if (FullscreenActivity.mFontSize<4) {
            FullscreenActivity.mFontSize = 4.0f;
        } else if (FullscreenActivity.mFontSize>84) {
            FullscreenActivity.mFontSize = 84.0f;
        }

        int progressbar = (int) FullscreenActivity.mFontSize - 4;
        String text = FullscreenActivity.mFontSize + " sp";

        fontsize_seekbar.setProgress(progressbar);
        fontsize_TextView.setText(text);
        fontsize_TextView.setTextSize(FullscreenActivity.mFontSize);
    }

    public void updatefontsize() {
        FullscreenActivity.mFontSize = fontsize_seekbar.getProgress() + 4.0f;
        String text = (int) FullscreenActivity.mFontSize + " sp";
        fontsize_TextView.setText(text);
        fontsize_TextView.setTextSize(FullscreenActivity.mFontSize);
    }

    public void setupmaxfontsizeseekbar() {
        // Seekbar size is 60 wide
        // Subtract 20 from all values - min is then 0, max is then 60
        // Actual mMaxFontSize is between 20 and 80 though
        if (FullscreenActivity.mMaxFontSize<20) {
            FullscreenActivity.mMaxFontSize = 20;
        } else if (FullscreenActivity.mMaxFontSize>80) {
            FullscreenActivity.mMaxFontSize = 80;
        }

        int progressbar = FullscreenActivity.mMaxFontSize - 20;
        String text = FullscreenActivity.mMaxFontSize + " sp";

        maxAutoScale_seekBar.setProgress(progressbar);
        maxAutoScale_TextView.setText(text);
        maxAutoScale_TextView.setTextSize(FullscreenActivity.mMaxFontSize);
    }

    public void setupminfontsizeseekbar() {
        // Seekbar size is 28 wide
        // Subtract 2 from all values - min is then 0, max is then 28
        // Actual mMinFontSize is between 2 and 30 though
        if (FullscreenActivity.mMinFontSize<2) {
            FullscreenActivity.mMinFontSize = 2;
        } else if (FullscreenActivity.mMinFontSize>32) {
            FullscreenActivity.mMaxFontSize = 32;
        }

        // Check the min is below the max!
        if (FullscreenActivity.mMinFontSize>FullscreenActivity.mMaxFontSize) {
            FullscreenActivity.mMinFontSize -= 1;
        }

        int progressbar = FullscreenActivity.mMinFontSize - 2;
        String text = FullscreenActivity.mMinFontSize + " sp";

        minAutoScale_seekBar.setProgress(progressbar);
        minAutoScale_TextView.setText(text);
        minAutoScale_TextView.setTextSize(FullscreenActivity.mMinFontSize);
    }

    public void updatemaxfontsize() {
        FullscreenActivity.mMaxFontSize = maxAutoScale_seekBar.getProgress() + 20;
        String text = FullscreenActivity.mMaxFontSize + " sp";
        maxAutoScale_TextView.setText(text);
        maxAutoScale_TextView.setTextSize(FullscreenActivity.mMaxFontSize);
        // Check the min is below the max!
        if (FullscreenActivity.mMinFontSize>=FullscreenActivity.mMaxFontSize) {
            FullscreenActivity.mMinFontSize = FullscreenActivity.mMaxFontSize - 1;
            setupminfontsizeseekbar();
        }

    }

    public void updateminfontsize() {
        FullscreenActivity.mMinFontSize = minAutoScale_seekBar.getProgress() + 2;
        String text = FullscreenActivity.mMinFontSize + " sp";
        minAutoScale_TextView.setText(text);
        minAutoScale_TextView.setTextSize(FullscreenActivity.mMinFontSize);
        // Check the min is below the max!
        if (FullscreenActivity.mMinFontSize>=FullscreenActivity.mMaxFontSize) {
            FullscreenActivity.mMaxFontSize = FullscreenActivity.mMinFontSize + 1;
            setupmaxfontsizeseekbar();
        }
    }

    @Override
    public void onResume() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        super.onResume();
    }

}
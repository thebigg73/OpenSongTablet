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
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpActionBarInfoFragment extends DialogFragment {

    static PopUpActionBarInfoFragment newInstance() {
        PopUpActionBarInfoFragment frag;
        frag = new PopUpActionBarInfoFragment();
        return frag;
    }

    public interface MyInterface {
        void adjustABInfo();
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

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    SeekBar titleTextSize;
    SeekBar authorTextSize;
    SeekBar batteryTextSize;
    SeekBar clockTextSize;
    SwitchCompat batteryDialOnOff;
    SwitchCompat batteryTextOnOff;
    SwitchCompat clock24hrOnOff;
    SwitchCompat clockTextOnOff;
    TextView batteryTextSizeLabel;
    TextView clockTextSizeLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_clockandbattery, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.actionbar));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        // Initialise the views
        titleTextSize = V.findViewById(R.id.titleTextSize);
        authorTextSize = V.findViewById(R.id.authorTextSize);
        batteryTextSize = V.findViewById(R.id.batteryTextSize);
        clockTextSize = V.findViewById(R.id.clockTextSize);
        batteryDialOnOff = V.findViewById(R.id.batteryDialOnOff);
        batteryTextOnOff = V.findViewById(R.id.batteryTextOnOff);
        clock24hrOnOff = V.findViewById(R.id.clock24hrOnOff);
        clockTextOnOff = V.findViewById(R.id.clockTextOnOff);
        batteryTextSizeLabel = V.findViewById(R.id.batteryTextSizeLabel);
        clockTextSizeLabel = V.findViewById(R.id.clockTextSizeLabel);

        // Set initial values
        setTitleTextSize();
        setAuthorTextSize();
        setBatteryTextSize();
        setClockTextSize();
        batteryDialOnOff.setChecked(FullscreenActivity.batteryDialOn);
        batteryTextOnOff.setChecked(FullscreenActivity.batteryOn);
        clockTextOnOff.setChecked(FullscreenActivity.timeOn);
        clock24hrOnOff.setChecked(FullscreenActivity.timeFormat24h);
        hideshowbatterytextsize(FullscreenActivity.batteryOn);
        hideshowclocktextsize(FullscreenActivity.timeOn);

        // Set listeners
        titleTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getTitleTextSize();
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });
        authorTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getAuthorTextSize();
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });
        batteryTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getBatteryTextSize();
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });
        clockTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                getClockTextSize();
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });
        batteryDialOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.batteryDialOn = b;
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }
        });
        batteryTextOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.batteryOn = b;
                hideshowbatterytextsize(b);
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }
        });
        clockTextOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.timeOn = b;
                hideshowclocktextsize(b);
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }
        });
        clock24hrOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.timeFormat24h = b;
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }
        });

        return V;
    }

    public void setTitleTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = (int) FullscreenActivity.ab_titleSize - 6;
        titleTextSize.setProgress(s);
    }
    public void getTitleTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = titleTextSize.getProgress() + 6;
        FullscreenActivity.ab_titleSize = (float) s;
    }

    public void setAuthorTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = (int) FullscreenActivity.ab_authorSize - 6;
        authorTextSize.setProgress(s);
    }
    public void getAuthorTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = authorTextSize.getProgress() + 6;
        FullscreenActivity.ab_authorSize = (float) s;
    }

    public void setBatteryTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = (int) FullscreenActivity.batterySize - 6;
        batteryTextSize.setProgress(s);
    }
    public void getBatteryTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = batteryTextSize.getProgress() + 6;
        FullscreenActivity.batterySize = (float) s;
    }

    public void setClockTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = (int) FullscreenActivity.timeSize - 6;
        clockTextSize.setProgress(s);
    }
    public void getClockTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = clockTextSize.getProgress() + 6;
        FullscreenActivity.timeSize = (float) s;
    }

    public void hideshowclocktextsize (Boolean b) {
        if (b) {
            clockTextSize.setVisibility(View.VISIBLE);
            clockTextSizeLabel.setVisibility(View.VISIBLE);
        } else {
            clockTextSize.setVisibility(View.GONE);
            clockTextSizeLabel.setVisibility(View.GONE);
        }
    }

    public void hideshowbatterytextsize (Boolean b) {
        if (b) {
            batteryTextSize.setVisibility(View.VISIBLE);
            batteryTextSizeLabel.setVisibility(View.VISIBLE);
        } else {
            batteryTextSize.setVisibility(View.GONE);
            batteryTextSizeLabel.setVisibility(View.GONE);
        }
    }

}

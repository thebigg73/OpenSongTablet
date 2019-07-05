package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Objects;

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

    Preferences preferences;

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

    private SeekBar titleTextSize, authorTextSize, batteryTextSize, batteryDialSize, clockTextSize;
    private SwitchCompat clock24hrOnOff;
    private TextView batteryTextSizeLabel, batteryDialSizeLabel, clockTextSizeLabel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_clockandbattery, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.actionbar));
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
        saveMe.hide();

        preferences = new Preferences();

        // Initialise the views
        titleTextSize = V.findViewById(R.id.titleTextSize);
        authorTextSize = V.findViewById(R.id.authorTextSize);
        batteryTextSize = V.findViewById(R.id.batteryTextSize);
        batteryDialSize = V.findViewById(R.id.batteryDialSize);
        clockTextSize = V.findViewById(R.id.clockTextSize);
        SwitchCompat batteryDialOnOff = V.findViewById(R.id.batteryDialOnOff);
        SwitchCompat batteryTextOnOff = V.findViewById(R.id.batteryTextOnOff);
        clock24hrOnOff = V.findViewById(R.id.clock24hrOnOff);
        SwitchCompat clockTextOnOff = V.findViewById(R.id.clockTextOnOff);
        batteryTextSizeLabel = V.findViewById(R.id.batteryTextSizeLabel);
        batteryDialSizeLabel = V.findViewById(R.id.batteryDialSizeLabel);
        clockTextSizeLabel = V.findViewById(R.id.clockTextSizeLabel);

        // Set initial values
        setTitleTextSize();
        setAuthorTextSize();
        setBatteryTextSize();
        setBatteryDialSize();
        setClockTextSize();
        batteryDialOnOff.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"batteryDialOn",true));
        batteryTextOnOff.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"batteryTextOn",true));
        clockTextOnOff.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"clockOn",true));
        clock24hrOnOff.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"clock24hFormat",true));
        hideshowbatterytextsize(preferences.getMyPreferenceBoolean(getActivity(),"batteryTextOn",true));
        hideshowclocktextsize(preferences.getMyPreferenceBoolean(getActivity(),"clockOn",true));
        hideshowbatterylinesize(preferences.getMyPreferenceBoolean(getActivity(),"batteryDialOn",true));

        // Set listeners
        titleTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                preferences.setMyPreferenceFloat(getActivity(),"songTitleSize",getTitleTextSize());
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        authorTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                preferences.setMyPreferenceFloat(getActivity(),"songAuthorSize",getAuthorTextSize());
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        batteryTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                preferences.setMyPreferenceFloat(getActivity(),"batteryTextSize",(float)i + 6);
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        batteryDialSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                preferences.setMyPreferenceInt(getActivity(),"batteryDialThickness",getBatteryDialSize());
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        clockTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                preferences.setMyPreferenceFloat(getActivity(),"clockTextSize",(float)getClockTextSize());
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        batteryDialOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.setMyPreferenceBoolean(getActivity(),"batteryDialOn",b);
                hideshowbatterylinesize(b);
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }
        });
        batteryTextOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.setMyPreferenceBoolean(getActivity(),"batteryTextOn",b);
                hideshowbatterytextsize(b);
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }
        });
        clockTextOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.setMyPreferenceBoolean(getActivity(),"clockOn",b);
                hideshowclocktextsize(b);
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }
        });
        clock24hrOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences.setMyPreferenceBoolean(getActivity(),"clock24hFormat",b);
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }
        });

        Dialog dialog = getDialog();
        if (dialog!=null && getActivity()!=null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),dialog, preferences);
        }
        return V;
    }

    private void setTitleTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = (int) preferences.getMyPreferenceFloat(getActivity(),"songTitleSize",13.0f) - 6;
        titleTextSize.setProgress(s);
    }
    private int getTitleTextSize() {
        // Min size is 6, this is 0 on the seekBar
        return titleTextSize.getProgress() + 6;
    }

    private void setAuthorTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = (int) preferences.getMyPreferenceFloat(getActivity(),"songAuthorSize",11.0f) - 6;
        authorTextSize.setProgress(s);
    }
    private int getAuthorTextSize() {
        // Min size is 6, this is 0 on the seekBar
        return authorTextSize.getProgress() + 6;
    }

    private void setBatteryTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = (int) preferences.getMyPreferenceFloat(getActivity(),"batteryTextSize",9.0f) - 6;
        batteryTextSize.setProgress(s);
    }

    private void setBatteryDialSize() {
        // Min size is 1, this is 0 on the seekBar
        int s = preferences.getMyPreferenceInt(getActivity(),"batteryDialThickness",4) - 1;
        batteryDialSize.setProgress(s);
    }
    private int getBatteryDialSize() {
        // Min size is 1, this is 0 on the seekBar
        return batteryDialSize.getProgress() + 1;
    }

    private void setClockTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = (int) preferences.getMyPreferenceFloat(getActivity(),"clockTextSize",9.0f) - 6;
        clockTextSize.setProgress(s);
    }
    public int getClockTextSize() {
        // Min size is 6, this is 0 on the seekBar
        return clockTextSize.getProgress() + 6;
    }

    private void hideshowclocktextsize(Boolean b) {
        if (b) {
            clockTextSize.setVisibility(View.VISIBLE);
            clockTextSizeLabel.setVisibility(View.VISIBLE);
            clock24hrOnOff.setVisibility(View.VISIBLE);
        } else {
            clockTextSize.setVisibility(View.GONE);
            clockTextSizeLabel.setVisibility(View.GONE);
            clock24hrOnOff.setVisibility(View.GONE);
        }
    }

    private void hideshowbatterytextsize(Boolean b) {
        if (b) {
            batteryTextSize.setVisibility(View.VISIBLE);
            batteryTextSizeLabel.setVisibility(View.VISIBLE);
        } else {
            batteryTextSize.setVisibility(View.GONE);
            batteryTextSizeLabel.setVisibility(View.GONE);
        }
    }

    private void hideshowbatterylinesize(Boolean b) {
        if (b) {
            batteryDialSize.setVisibility(View.VISIBLE);
            batteryDialSizeLabel.setVisibility(View.VISIBLE);
        } else {
            batteryDialSize.setVisibility(View.GONE);
            batteryDialSizeLabel.setVisibility(View.GONE);
        }
    }

}

package com.garethevans.church.opensongtablet;

import android.app.Dialog;
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

public class PopUpActionBarInfoFragment extends DialogFragment {

    static PopUpActionBarInfoFragment newInstance() {
        PopUpActionBarInfoFragment frag;
        frag = new PopUpActionBarInfoFragment();
        return frag;
    }

    public interface MyInterface {
        void adjustABInfo();
        void hideActionBar();
        void showActionBar();
        void loadSong();
    }

    private MyInterface mListener;

    private Preferences preferences;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyInterface) context;
    }

    @Override
    public void onDetach() {
        mListener.adjustABInfo();
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
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

    private SeekBar titleTextSize, authorTextSize, batteryTextSize, batteryDialSize, clockTextSize;
    private SwitchCompat clock24hrOnOff;
    private TextView batteryTextSizeLabel, batteryDialSizeLabel, clockTextSizeLabel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }
        View V = inflater.inflate(R.layout.popup_clockandbattery, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.actionbar));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getContext());
            closeMe.setEnabled(false);
            dismiss();
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
        SwitchCompat displayMenuToggleSwitch = V.findViewById(R.id.displayMenuToggleSwitch);

        // Set the switches up based on preferences
        displayMenuToggleSwitch.setChecked(preferences.getMyPreferenceBoolean(getContext(),"hideActionBar",false));

        // Set initial values
        setTitleTextSize();
        setAuthorTextSize();
        setBatteryTextSize();
        setBatteryDialSize();
        setClockTextSize();
        batteryDialOnOff.setChecked(preferences.getMyPreferenceBoolean(getContext(),"batteryDialOn",true));
        batteryTextOnOff.setChecked(preferences.getMyPreferenceBoolean(getContext(),"batteryTextOn",true));
        clockTextOnOff.setChecked(preferences.getMyPreferenceBoolean(getContext(),"clockOn",true));
        clock24hrOnOff.setChecked(preferences.getMyPreferenceBoolean(getContext(),"clock24hFormat",true));
        hideshowbatterytextsize(preferences.getMyPreferenceBoolean(getContext(),"batteryTextOn",true));
        hideshowclocktextsize(preferences.getMyPreferenceBoolean(getContext(),"clockOn",true));
        hideshowbatterylinesize(preferences.getMyPreferenceBoolean(getContext(),"batteryDialOn",true));

        // Set listeners
        titleTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                preferences.setMyPreferenceFloat(getContext(),"songTitleSize",getTitleTextSize());
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
                preferences.setMyPreferenceFloat(getContext(),"songAuthorSize",getAuthorTextSize());
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
                preferences.setMyPreferenceFloat(getContext(),"batteryTextSize",(float)i + 6);
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
                preferences.setMyPreferenceInt(getContext(),"batteryDialThickness",getBatteryDialSize());
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
                preferences.setMyPreferenceFloat(getContext(),"clockTextSize",(float)getClockTextSize());
                if (mListener!=null) {
                    mListener.adjustABInfo();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        batteryDialOnOff.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getContext(),"batteryDialOn",b);
            hideshowbatterylinesize(b);
            if (mListener!=null) {
                mListener.adjustABInfo();
            }
        });
        batteryTextOnOff.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getContext(),"batteryTextOn",b);
            hideshowbatterytextsize(b);
            if (mListener!=null) {
                mListener.adjustABInfo();
            }
        });
        clockTextOnOff.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getContext(),"clockOn",b);
            hideshowclocktextsize(b);
            if (mListener!=null) {
                mListener.adjustABInfo();
            }
        });
        clock24hrOnOff.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getContext(),"clock24hFormat",b);
            if (mListener!=null) {
                mListener.adjustABInfo();
            }
        });
        displayMenuToggleSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            preferences.setMyPreferenceBoolean(getContext(),"hideActionBar",b);
            if (mListener!=null) {
                if (b) {
                    mListener.hideActionBar();
                } else {
                    mListener.showActionBar();
                }
                mListener.loadSong();
            }
        });

        Dialog dialog = getDialog();
        if (dialog!=null && getContext()!=null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),dialog, preferences);
        }
        return V;
    }

    private void setTitleTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = (int) preferences.getMyPreferenceFloat(getContext(),"songTitleSize",13.0f) - 6;
        titleTextSize.setProgress(s);
    }
    private int getTitleTextSize() {
        // Min size is 6, this is 0 on the seekBar
        return titleTextSize.getProgress() + 6;
    }

    private void setAuthorTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = (int) preferences.getMyPreferenceFloat(getContext(),"songAuthorSize",11.0f) - 6;
        authorTextSize.setProgress(s);
    }
    private int getAuthorTextSize() {
        // Min size is 6, this is 0 on the seekBar
        return authorTextSize.getProgress() + 6;
    }

    private void setBatteryTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = (int) preferences.getMyPreferenceFloat(getContext(),"batteryTextSize",9.0f) - 6;
        batteryTextSize.setProgress(s);
    }

    private void setBatteryDialSize() {
        // Min size is 1, this is 0 on the seekBar
        int s = preferences.getMyPreferenceInt(getContext(),"batteryDialThickness",4) - 1;
        batteryDialSize.setProgress(s);
    }
    private int getBatteryDialSize() {
        // Min size is 1, this is 0 on the seekBar
        return batteryDialSize.getProgress() + 1;
    }

    private void setClockTextSize() {
        // Min size is 6, this is 0 on the seekBar
        int s = (int) preferences.getMyPreferenceFloat(getContext(),"clockTextSize",9.0f) - 6;
        clockTextSize.setProgress(s);
    }
    private int getClockTextSize() {
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

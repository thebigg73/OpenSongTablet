package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsDisplayScalingBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class DisplayScalingFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsDisplayScalingBinding myView;
    private final int minTextSize = 5;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsDisplayScalingBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(getString(R.string.scaling));

        // Set up the views
        setViews();

        // Set up the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setViews() {
        // The switches
        setAutoscaleMode();
        myView.scaleColumns.setChecked(getChecked("songAutoScaleColumnMaximise",true));
        myView.overrideFull.setChecked(getChecked("songAutoScaleOverrideFull",true));
        myView.overrideWidthSwitch.setChecked(getChecked("songAutoScaleOverrideWidth",false));

        // The seekbars
        setSeekBarProgress(myView.manualFontSize,myView.manualFontSizeText,"fontSize",20.0f,1,minTextSize,"px");
        setSeekBarProgress(myView.minFontSize,myView.minFontSizeText,"fontSizeMin",10.0f,1,minTextSize,"px");
        setSeekBarProgress(myView.maxFontSize,myView.maxFontSizeText,"fontSizeMax",50.0f,1,minTextSize,"px");
        setSeekBarProgress(myView.scaleHeading,myView.scaleHeadingText,"scaleHeadings",0.6f,100,0,"%");
        setSeekBarProgress(myView.scaleChords,myView.scaleChordsText,"scaleChords",0.8f,100,0,"%");
        setSeekBarProgress(myView.scaleComments,myView.scaleCommentsText,"scaleChords",0.8f,100,0,"%");
    }

    private void setAutoscaleMode() {
        // Autoscale can be Y(es) W(idth) N(o)
        String mode = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"songAutoScale","W");
        switch (mode) {
            case "Y":
                modeSwitches(true,false);
                visibilityByBoolean(myView.manualFontSizeLayout,false);
                visibilityByBoolean(myView.autoFontSizeLayout,true);
                visibilityByBoolean(myView.overrideFull,true);
                visibilityByBoolean(myView.overrideWidthSwitch,true);
                break;
            case "W":
                modeSwitches(true,true);
                visibilityByBoolean(myView.manualFontSizeLayout,false);
                visibilityByBoolean(myView.autoFontSizeLayout,true);
                visibilityByBoolean(myView.overrideFull,false);
                visibilityByBoolean(myView.overrideWidthSwitch,true);
                break;
            case "N":
                modeSwitches(false,false);
                visibilityByBoolean(myView.manualFontSizeLayout,true);
                visibilityByBoolean(myView.autoFontSizeLayout,false);
                break;
        }
    }
    private void modeSwitches(boolean useAutoScale, boolean widthOnly) {
        myView.useAutoscale.setChecked(useAutoScale);
        myView.scaleWidth.setChecked(widthOnly);
    }
    private void visibilityByBoolean(View view, boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void setSeekBarProgress(SeekBar seekBar, TextView textView, String prefName,
                                    float fallback, int multiplier, int minVal, String unit) {
        // Get the float
        float val = multiplier * mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),prefName,fallback);
        updateText(textView,val,unit);
        seekBar.setProgress((int)val-minVal);
    }
    private void updateText(TextView textView, float size, String unit) {
        if (unit.equals("px")) {
            textView.setTextSize(size);
        }
        String text = (int)size + unit;
        textView.setText(text);
    }

    private void getAutoscaleMode() {
        // Autoscale can be Y(es) W(idth) N(o)
        boolean useAutoscale = myView.useAutoscale.isChecked();
        boolean scaleWidth = myView.scaleWidth.isChecked();
        String val;
        if (useAutoscale && scaleWidth) {
            val = "W";
            visibilityByBoolean(myView.manualFontSizeLayout,false);
            visibilityByBoolean(myView.autoFontSizeLayout,true);
            visibilityByBoolean(myView.overrideFull,false);
            visibilityByBoolean(myView.overrideWidthSwitch,true);
        } else if (useAutoscale) {
            val = "Y";
            visibilityByBoolean(myView.manualFontSizeLayout,false);
            visibilityByBoolean(myView.autoFontSizeLayout,true);
            visibilityByBoolean(myView.overrideFull,true);
            visibilityByBoolean(myView.overrideWidthSwitch,true);
        } else {
            val = "N";
            visibilityByBoolean(myView.manualFontSizeLayout,true);
            visibilityByBoolean(myView.autoFontSizeLayout,false);
        }
        mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),"songAutoScale",val);
    }

    private boolean getChecked(String prefName, boolean fallback) {
        return mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),prefName,fallback);
    }
    private void checkMinMaxSizes() {
        // If the min size is bigger than the max size, then swap them
        int minSize = myView.minFontSize.getProgress();
        int maxSize = myView.maxFontSize.getProgress();
        if (minSize>maxSize) {
            myView.minFontSize.setProgress(maxSize);
            myView.maxFontSize.setProgress(minSize);
        }
    }

    private void setListeners() {
        // The switches
        myView.useAutoscale.setOnCheckedChangeListener((buttonView, isChecked) -> getAutoscaleMode());
        myView.scaleWidth.setOnCheckedChangeListener(((buttonView, isChecked) -> getAutoscaleMode()));
        myView.scaleColumns.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("songAutoScaleColumnMaximise",isChecked));
        myView.overrideFull.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("songAutoScaleOverrideFull",isChecked));
        myView.overrideWidthSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("songAutoScaleOverrideWidth",isChecked));

        // The seekbars
        myView.manualFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateText(myView.manualFontSizeText,(float)progress+minTextSize,"px");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateSeekBar(myView.manualFontSize,myView.manualFontSizeText,"fontSize",
                        minTextSize,1.0f,"px");
            }
        });
        myView.minFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateText(myView.minFontSizeText,(float)progress+minTextSize,"px");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                checkMinMaxSizes();
                updateSeekBar(myView.minFontSize,myView.minFontSizeText,"fontSizeMin",
                        minTextSize,1.0f,"px");
            }
        });
        myView.maxFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateText(myView.maxFontSizeText,(float)progress+minTextSize,"px");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                checkMinMaxSizes();
                updateSeekBar(myView.maxFontSize,myView.maxFontSizeText,"fontSizeMax",
                        minTextSize,1.0f,"px");
            }
        });
        myView.scaleHeading.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateText(myView.scaleHeadingText,(float)progress,"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                checkMinMaxSizes();
                updateSeekBar(myView.scaleHeading,myView.scaleHeadingText,"scaleHeadings",
                        0,100.0f,"%");
            }
        });
        myView.scaleChords.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateText(myView.scaleChordsText,(float)progress,"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                checkMinMaxSizes();
                updateSeekBar(myView.scaleChords,myView.scaleChordsText,"scaleChords",
                        0,100.0f,"%");
            }
        });
        myView.scaleComments.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateText(myView.scaleCommentsText,(float)progress,"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                checkMinMaxSizes();
                updateSeekBar(myView.scaleComments,myView.scaleCommentsText,"scaleComments",
                        0,100.0f,"%");
            }
        });
    }

    private void updateBooleanPreference(String prefName, boolean isChecked) {
        mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),prefName,isChecked);
    }

    private void updateSeekBar(SeekBar seekBar, TextView textView, String prefName, int minVal, float multiplier, String unit) {
        // The actual value is the progress + the minVal
        float val = seekBar.getProgress() + minVal;
        // The float to store could be out of 100, or 1.  Use the multiplier to convert
        mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),prefName, val/multiplier);
        updateText(textView,(float)val,unit);
    }
}

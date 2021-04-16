package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsActionbarBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ActionBarSettingsFragment extends Fragment {

    SettingsActionbarBinding myView;
    MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsActionbarBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.actionbar_display));

        // Set up preferences and view settings
        setupPreferences();

        return myView.getRoot();
    }

    private void setupPreferences() {
        // The song title and author
        myView.songTitle.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),"songTitleSize",13.0f));
        myView.songAuthor.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),"songAuthorSize",11.0f));

        // The seekbars
        myView.titleTextSize.setProgress(prefToSeekBarProgress("songTitleSize",6,13,true));
        myView.authorTextSize.setProgress(prefToSeekBarProgress("songAuthorSize",6,11,true));
        myView.batteyDialSize.setProgress(prefToSeekBarProgress("batteryDialThickness",1,4,false));
        myView.batteryTextSize.setProgress(prefToSeekBarProgress("batteryTextSize",6,9,true));
        myView.timeTextSize.setProgress(prefToSeekBarProgress("clockTextSize",6,9,true));

        // The switches
        myView.autohideActionBar.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"hideActionBar",false));
        showOrHideView(mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"batteryDialOn",true),
                true,myView.batteryDialOnOff,myView.batteryImageLayout);
        showOrHideView(mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"batteryTextOn",true),
                true,myView.batteryTextOnOff,myView.batteryTextLayout);
        showOrHideView(mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"clockOn",true),
                true,myView.clockTextOnOff,myView.timeLayout);
        myView.clock24hrOnOff.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"clock24hFormat",true));
        
        // The listeners
        myView.titleTextSize.setOnSeekBarChangeListener(new MySeekBar("songTitleSize",true,6));
        myView.authorTextSize.setOnSeekBarChangeListener(new MySeekBar("songAuthorSize",true,6));
        myView.batteyDialSize.setOnSeekBarChangeListener(new MySeekBar("batteryDialThickness",false,1));
        myView.batteryTextSize.setOnSeekBarChangeListener(new MySeekBar("batteryTextSize",true,6));
        myView.timeTextSize.setOnSeekBarChangeListener(new MySeekBar("clockTextSize",true,6));
        myView.autohideActionBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateActionBar("hideActionBar",-1,0.0f,!isChecked);
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"hideActionBar",isChecked);
        });
        myView.batteryDialOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showOrHideView(isChecked,false ,myView.batteryDialOnOff, myView.batteryImageLayout);
            updateActionBar("batteryDialOn",-1,0.0f,isChecked);
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"batteryDialOn",isChecked);
        });
        myView.batteryTextOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showOrHideView(isChecked,false ,myView.batteryTextOnOff, myView.batteryTextLayout);
            updateActionBar("batteryTextOn",-1,0.0f,isChecked);
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"batteryTextOn",isChecked);
        });
        myView.clockTextOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showOrHideView(isChecked,false ,myView.clockTextOnOff, myView.timeLayout);
            updateActionBar("clockOn",-1,0.0f,isChecked);
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"clockOn",isChecked);
        });
        myView.clock24hrOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateActionBar("clock24hFormat",-1,0.0f,isChecked);
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"clock24hFormat",isChecked);
        });
    }

    private void showOrHideView(boolean show, boolean setSwitch, SwitchCompat switchCompat, LinearLayout linearLayout) {
        if (show) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
        if (setSwitch) {
            switchCompat.setChecked(show);
        }
    }

    private int prefToSeekBarProgress(String prefName, int min, int def, boolean isfloat) {
        // Min size is what value 0 on the seekbar actually means (it gets added on later)
        if (isfloat) {
            return (int) mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(), prefName, (float) def) - min;
        } else {
            return mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(), prefName, def) - min;
        }
    }
    
    private int progressToInt(int progress, int min) {
        // Add this min value back on the the progress
        return progress + min;
    }

    private class MySeekBar implements SeekBar.OnSeekBarChangeListener {

        private final String prefName;
        private final boolean isfloat;
        private final int min;

        MySeekBar (String prefName, boolean isfloat, int min) {
            this.prefName = prefName;
            this.isfloat = isfloat;
            this.min = min;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (isfloat) {
                float val = (float) progressToInt(progress, min);
                if (prefName.equals("songTitleSize")) {
                    myView.songTitle.setTextSize(val);
                } else if (prefName.equals("songAuthorSize")) {
                    myView.songAuthor.setTextSize(val);
                } else {
                    updateActionBar(prefName, -1, val, false);
                }
            } else {
                updateActionBar(prefName, progressToInt(progress, min), 0.0f, false);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (isfloat) {
                mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),prefName,(float) progressToInt(seekBar.getProgress(), min));
            } else {
                mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(),prefName, progressToInt(seekBar.getProgress(), min));
            }
        }
    }
    private void updateActionBar(String prefName, int intval, float floatval, boolean isvisible) {
        mainActivityInterface.updateActionBarSettings(prefName, intval, floatval, isvisible);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

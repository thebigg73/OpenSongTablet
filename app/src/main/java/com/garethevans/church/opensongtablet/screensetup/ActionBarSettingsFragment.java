package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsActionbarBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ActionBarSettingsFragment extends Fragment {

    private SettingsActionbarBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbarHelp(webAddress);

        // IV - Set the battery/clock visible but not clickable (floatval 0)
        new Handler().postDelayed(() -> {
            if (myView != null) {
                mainActivityInterface.updateActionBarSettings("showBatteryHolder", 0.0f, true);
            }
        },50);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsActionbarBinding.inflate(inflater,container,false);

        if (getContext()!=null) {
            mainActivityInterface.updateToolbar(getString(R.string.actionbar_display));
            webAddress = getString(R.string.website_actionbar);
        }

        // Set up preferences and view settings
        setupPreferences();

        changeVisibilities();

        return myView.getRoot();
    }

    private void setupPreferences() {
        float titleTextSize = checkMin(mainActivityInterface.getPreferences().getMyPreferenceFloat("songTitleSize", 13),6);
        float authorTextSize = checkMin(mainActivityInterface.getPreferences().getMyPreferenceFloat("songAuthorSize", 11),6);
        float batteryTextSize = checkMin(mainActivityInterface.getPreferences().getMyPreferenceFloat("batteryTextSize", 9),6);
        float clockTextSize = checkMin(mainActivityInterface.getPreferences().getMyPreferenceFloat("clockTextSize", 9),6);
        int batteryDialSize = (int)checkMin(mainActivityInterface.getPreferences().getMyPreferenceInt("batteryDialThickness", 4),1);
        int actionBarHideTime = (int)(checkMin(mainActivityInterface.getPreferences().getMyPreferenceInt("actionBarHideTime",1200),1000));

        // The sliders
        myView.titleTextSize.setValue(titleTextSize);
        myView.titleTextSize.setHint((int)titleTextSize+" sp");
        myView.titleTextSize.setLabelFormatter(value -> ((int)value)+" sp");
        myView.titleTextSize.setHintTextSize(titleTextSize);
        myView.authorTextSize.setValue(authorTextSize);
        myView.authorTextSize.setHint((int)authorTextSize+" sp");
        myView.authorTextSize.setLabelFormatter(value -> ((int)value)+" sp");
        myView.authorTextSize.setHintTextSize(authorTextSize);
        myView.batteryDialSize.setValue(batteryDialSize);
        myView.batteryDialSize.setLabelFormatter(value -> ((int)value)+" px");
        myView.batteryTextSize.setValue(batteryTextSize);
        myView.batteryTextSize.setLabelFormatter(value -> ((int)value)+" sp");
        myView.clockTextSize.setValue(clockTextSize);
        myView.clockTextSize.setLabelFormatter(value -> ((int)value)+" sp");
        myView.actionBarHideTime.setValue(actionBarHideTime);
        myView.actionBarHideTime.setHint(actionBarHideTime + " ms");
        myView.actionBarHideTime.setLabelFormatter(value -> ((int)value)+" ms");

        // The switches
        myView.autohideActionBar.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("hideActionBar",false));
        if (myView.autohideActionBar.getChecked()) {
            myView.actionBarHideTime.setVisibility(View.VISIBLE);
        } else {
            myView.actionBarHideTime.setVisibility(View.GONE);
        }

        showOrHideView(mainActivityInterface.getPreferences().getMyPreferenceBoolean("batteryDialOn",true),
                true,myView.batteryDialOnOff,myView.batteryDialSize);
        showOrHideView(mainActivityInterface.getPreferences().getMyPreferenceBoolean("batteryTextOn",true),
                true,myView.batteryTextOnOff,myView.batteryDialSize);
        showOrHideView(mainActivityInterface.getPreferences().getMyPreferenceBoolean("clockOn",true),
                true,myView.clockTextOnOff,myView.timeLayout);
        myView.clock24hrOnOff.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("clock24hFormat",true));
        myView.clockSeconds.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("clockSeconds",false));

        // The listeners
        myView.titleTextSize.addOnChangeListener(new MyOnChangeListener("songTitleSize",true));
        myView.authorTextSize.addOnChangeListener(new MyOnChangeListener("songAuthorSize",true));
        myView.batteryDialSize.addOnChangeListener(new MyOnChangeListener("batteryDialThickness",true));
        myView.batteryTextSize.addOnChangeListener(new MyOnChangeListener("batteryTextSize",true));
        myView.clockTextSize.addOnChangeListener(new MyOnChangeListener("clockTextSize",true));
        myView.titleTextSize.addOnSliderTouchListener(new MyOnSliderTouch("songTitleSize",true));
        myView.authorTextSize.addOnSliderTouchListener(new MyOnSliderTouch("songAuthorSize",true));
        myView.batteryDialSize.addOnSliderTouchListener(new MyOnSliderTouch("batteryDialThickness",false));
        myView.batteryTextSize.addOnSliderTouchListener(new MyOnSliderTouch("batteryTextSize",true));
        myView.clockTextSize.addOnSliderTouchListener(new MyOnSliderTouch("clockTextSize",true));
        myView.actionBarHideTime.addOnChangeListener(new MyOnChangeListener("actionBarHideTime",false));
        myView.actionBarHideTime.addOnSliderTouchListener(new MyOnSliderTouch("actionBarHideTime",false));
        myView.autohideActionBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateActionBar("hideActionBar",0.0f,!isChecked);
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("hideActionBar",isChecked);
            if (isChecked) {
                myView.actionBarHideTime.setVisibility(View.VISIBLE);
            } else {
                myView.actionBarHideTime.setVisibility(View.GONE);
            }
        });
        myView.batteryDialOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            changeVisibilities();
            updateActionBar("batteryDialOn",0.0f,isChecked);
            showOrHideView(isChecked,false ,myView.batteryDialOnOff, myView.batteryDialSize);
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("batteryDialOn",isChecked);
        });
        myView.batteryTextOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            changeVisibilities();
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("batteryTextOn",isChecked);
            showOrHideView(isChecked,false ,myView.batteryTextOnOff, myView.batteryTextSize);
            updateActionBar("batteryTextOn",0.0f,isChecked);
        });
        myView.clockTextOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            changeVisibilities();
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("clockOn",isChecked);
            showOrHideView(isChecked,false ,myView.clockTextOnOff, myView.timeLayout);
            updateActionBar("clockOn",0.0f,isChecked);
        });
        myView.clock24hrOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("clock24hFormat",isChecked);
            updateActionBar("clock24hFormat",0.0f,isChecked);
        });
        myView.clockSeconds.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("clockSeconds",isChecked);
            updateActionBar("clockSeconds",0.0f,isChecked);
        });
    }

    private void showOrHideView(boolean show, boolean setSwitch, SwitchMaterial switchMaterial, LinearLayout linearLayout) {
        if (show) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
        if (setSwitch) {
            switchMaterial.setChecked(show);
        }
    }

    private float checkMin (float value, float min) {
        return Math.max(value,min);
    }

    private class MyOnChangeListener implements Slider.OnChangeListener {

        private final boolean isfloat;
        private final String prefName;

        MyOnChangeListener(String prefName, boolean isfloat) {
            this.prefName = prefName;
            this.isfloat = isfloat;
        }

        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            if (isfloat) {
                if (prefName.equals("songTitleSize")) {
                    myView.titleTextSize.setHint((int)value + " sp");
                    myView.titleTextSize.setHintTextSize(value);
                } else if (prefName.equals("songAuthorSize")) {
                    myView.authorTextSize.setHint((int) value + " sp");
                    myView.authorTextSize.setHintTextSize(value);
                } else {
                    updateActionBar(prefName, value, false);
                }
            } else {
                if (prefName.equals("actionBarHideTime")) {
                    myView.actionBarHideTime.setHint((int)value + " ms");
                }
                updateActionBar(prefName, (int)value, false);
            }
        }
    }
    private class MyOnSliderTouch implements Slider.OnSliderTouchListener {

        private final String prefName;
        private final boolean isfloat;

        MyOnSliderTouch(String prefName, boolean isfloat) {
            this.prefName = prefName;
            this.isfloat = isfloat;
        }

        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) { }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            // Save the preference
            if (isfloat) {
                mainActivityInterface.getPreferences().setMyPreferenceFloat(prefName, slider.getValue());
            } else {
                mainActivityInterface.getPreferences().setMyPreferenceInt(prefName, (int)slider.getValue());
            }
        }
    }

    private void updateActionBar(String prefName, float floatval, boolean isvisible) {
        mainActivityInterface.updateActionBarSettings(prefName, floatval, isvisible);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }

    private void changeVisibilities() {
        mainActivityInterface.getToolbar().batteryholderVisibility(true, false);
        mainActivityInterface.getBatteryStatus().showBatteryStuff(true);
        mainActivityInterface.getToolbar().getBatteryimage().setVisibility(myView.batteryDialOnOff.isChecked() ? View.VISIBLE:View.INVISIBLE);
        mainActivityInterface.getToolbar().getBatterycharge().setVisibility(myView.batteryTextOnOff.isChecked() ? View.VISIBLE:View.INVISIBLE);
        mainActivityInterface.getToolbar().getClock().setVisibility(myView.clockTextOnOff.isChecked() ? View.VISIBLE:View.INVISIBLE);
    }
}

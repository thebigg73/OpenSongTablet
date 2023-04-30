package com.garethevans.church.opensongtablet.autoscroll;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsAutoscrollBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class AutoscrollSettingsFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "AutoscrollSettings";
    private SettingsAutoscrollBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String autoscroll="", website_autoscroll="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(autoscroll);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        myView = SettingsAutoscrollBinding.inflate(inflater, container, false);

        prepareStrings();

        webAddress = website_autoscroll;
        // Set up the views
        setupViews();

        // Set up the listeners
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            autoscroll = getString(R.string.autoscroll);
            website_autoscroll = getString(R.string.website_autoscroll);
        }
    }
    private void setupViews() {
        // Get the defaults from the autoscroll class which controls the preferences
        int[] defTimes = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(
                mainActivityInterface.getAutoscroll().getAutoscrollDefaultSongLength());
        myView.defaultMins.setText(defTimes[0]+"");
        myView.defaultSecs.setText(defTimes[1]+"");
        myView.defaultDelay.setText(mainActivityInterface.getAutoscroll().
                getAutoscrollDefaultSongPreDelay()+"");

        if (mainActivityInterface.getAutoscroll().getAutoscrollUseDefaultTime()) {
            myView.defaultOrAsk.setSliderPos(0);
        } else {
            myView.defaultOrAsk.setSliderPos(1);
        }

        myView.autostartAutoscroll.setChecked(mainActivityInterface.
                getAutoscroll().getAutoscrollAutoStart());
        myView.delay.setText(getStringToInt(mainActivityInterface.getSong().
                getAutoscrolldelay())+"");

        // Get song values
        int[] songTimes = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(
                getStringToInt(mainActivityInterface.getSong().getAutoscrolllength()));
        myView.durationMins.setText(songTimes[0]+"");
        myView.durationSecs.setText(songTimes[1]+"");

        // Check audio link file
        mainActivityInterface.getAutoscroll().checkLinkAudio(myView.linkAudio, myView.durationMins,
                myView.durationSecs, myView.delay,
                getStringToInt(mainActivityInterface.getSong().getAutoscrolldelay()));

        myView.onscreenAutoscrollHide.setChecked(mainActivityInterface.getAutoscroll().
                getOnscreenAutoscrollHide());

        // Set the input types as strict numbers only
        myView.defaultMins.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.defaultSecs.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.defaultDelay.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.durationMins.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.durationSecs.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.delay.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.defaultMins.setDigits("0123456789");
        myView.defaultSecs.setDigits("0123456789");
        myView.defaultDelay.setDigits("0123456789");
        myView.durationMins.setDigits("0123456789");
        myView.durationSecs.setDigits("0123456789");
        myView.delay.setDigits("0123456789");

    }

    private void setupListeners() {
        myView.durationMins.addTextChangedListener(new MyTextWatcher("durationMins"));
        myView.durationSecs.addTextChangedListener(new MyTextWatcher("durationSecs"));
        myView.delay.addTextChangedListener(new MyTextWatcher("delay"));
        myView.defaultMins.addTextChangedListener(new MyTextWatcher("defaultMins"));
        myView.defaultSecs.addTextChangedListener(new MyTextWatcher("defaultSecs"));
        myView.defaultDelay.addTextChangedListener(new MyTextWatcher("defaultDelay"));
        myView.autostartAutoscroll.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getAutoscroll().setAutoscrollAutoStart(b));
        myView.defaultOrAsk.addOnChangeListener((slider, value, fromUser) -> {
            boolean defaultVal = value==0;
            mainActivityInterface.getAutoscroll().setAutoscrollUseDefaultTime(defaultVal);
        });

        // TODO may reinstate these
        // myView.learnAutoscroll.setOnClickListener(v -> learnAutoscroll());
        // myView.nestedScrollView.setFabToAnimate(myView.startStopAutoscroll);
        // myView.startStopAutoscroll.setOnClickListener(v -> startStopAutoscroll());

        myView.onscreenAutoscrollHide.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                    "onscreenAutoscrollHide",b);
            mainActivityInterface.
                    updateOnScreenInfo("setpreferences");
            mainActivityInterface.getAutoscroll().setupAutoscrollPreferences();
        });
    }

    private class MyTextWatcher implements TextWatcher {

        private final String which;

        MyTextWatcher(String which) {
            this.which = which;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void afterTextChanged(Editable editable) {
              switch (which) {
                case "durationMins":
                case "durationSecs":
                case "delay":
                    updateTime(false);
                    break;

                case "defaultMins":
                case "defaultSecs":
                case "defaultDelay":
                    updateTime(true);
                    break;
            }
        }
    }

    private void updateTime(boolean defaults) {
        int mins;
        int secs;
        int total;
        int delay;

        if (defaults) {
            mins = getStringToInt(myView.defaultMins.getText().toString());
            secs = getStringToInt(myView.defaultSecs.getText().toString());
            delay = getStringToInt(myView.defaultDelay.getText().toString());
            total = mainActivityInterface.getTimeTools().totalSecs(mins, secs);
            if (delay >= total) {
                delay = 0;
            }
            mainActivityInterface.getAutoscroll().setAutoscrollDefaultSongPreDelay(delay);
            mainActivityInterface.getAutoscroll().setAutoscrollDefaultSongLength(total);
        } else {
            mins = getStringToInt(myView.durationMins.getText().toString());
            secs = getStringToInt(myView.durationSecs.getText().toString());
            delay = getStringToInt(myView.delay.getText().toString());
            total = mainActivityInterface.getTimeTools().totalSecs(mins, secs);
            if (delay >= total) {
                delay = 0;
            }
            mainActivityInterface.getSong().setAutoscrolldelay(delay+"");
            mainActivityInterface.getSong().setAutoscrolllength(total+"");
            mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
        }
    }

    private int getStringToInt(String string) {
        if (string==null || string.isEmpty()) {
            return 0;
        } else {
            return Integer.parseInt(string);
        }
    }
}

package com.garethevans.church.opensongtablet.metronome;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDown;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownSelection;
import com.garethevans.church.opensongtablet.databinding.SettingsMetronomeBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MetronomeFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsMetronomeBinding myView;
    private ArrayList<String> soundFiles;
    private ArrayList<String> soundNames;
    private ArrayList<String> panNames;
    private ArrayList<String> panLetters;
    private Timer isRunningTimer;
    private TimerTask isRunningTask;
    private Handler isRunningHandler = new Handler();


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsMetronomeBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(getString(R.string.metronome));

        // Set up the values for the exposeddropdowns
        initialiseDropDowns();

        // Set up initial preferences
        setupPreferences();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void initialiseDropDowns() {
        // Get the available sounds by filename and description
        soundFiles = new ArrayList<>();
        soundNames = new ArrayList<>();
        String low = getString(R.string.sound_low);
        String high = getString(R.string.sound_high);
        addSoundItem("","");
        addSoundItem("bass_drum",getString(R.string.sound_bass_drum));
        addSoundItem("bell_high", getString(R.string.sound_bell)+" ("+high+")");
        addSoundItem("bell_low", getString(R.string.sound_bell)+" ("+low+")");
        addSoundItem("click_1_high", getString(R.string.sound_click)+" 1 ("+high+")");
        addSoundItem("click_1_low", getString(R.string.sound_click)+" 1 ("+low+")");
        addSoundItem("click_2_high", getString(R.string.sound_click)+" 2 ("+high+")");
        addSoundItem("click_2_low", getString(R.string.sound_click)+" 2 ("+low+")");
        addSoundItem("digital_high", getString(R.string.sound_digital)+" ("+high+")");
        addSoundItem("digital_low", getString(R.string.sound_digital)+" ("+low+")");
        addSoundItem("hat_high", getString(R.string.sound_hihat)+" ("+high+")");
        addSoundItem("hat_low", getString(R.string.sound_hihat)+" ("+low+")");
        addSoundItem("stick_high", getString(R.string.sound_stick)+" ("+high+")");
        addSoundItem("stick_low", getString(R.string.sound_stick)+" ("+low+")");
        addSoundItem("wood_high", getString(R.string.sound_wood)+" ("+high+")");
        addSoundItem("wood_low", getString(R.string.sound_wood)+" ("+low+")");

        // Get the timesignatures
        ArrayList<String> timeSignatures = new ArrayList<>();
        timeSignatures.add("");
        timeSignatures.add("2/2");
        timeSignatures.add("3/2");
        timeSignatures.add("1/4");
        timeSignatures.add("2/4");
        timeSignatures.add("3/4");
        timeSignatures.add("4/4");
        timeSignatures.add("5/4");
        timeSignatures.add("6/4");
        timeSignatures.add("7/4");
        timeSignatures.add("5/8");
        timeSignatures.add("6/8");
        timeSignatures.add("7/8");

        // Get the tempos
        ArrayList<String> tempos = new ArrayList<>();
        tempos.add("");
        for (int x=40; x<300; x++) {
            tempos.add(""+x);
        }

        // Get the pans
        panNames = new ArrayList<>();
        panLetters = new ArrayList<>();
        panNames.add(getString(R.string.pan_left));
        panNames.add(getString(R.string.pan_center));
        panNames.add(getString(R.string.pan_right));
        panLetters.add("L");
        panLetters.add("C");
        panLetters.add("R");

        // Set the adapters
        ExposedDropDownArrayAdapter soundAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item, soundNames);
        ExposedDropDownArrayAdapter timeSignatureAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item, timeSignatures);
        ExposedDropDownArrayAdapter tempoAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item, tempos);
        ExposedDropDownArrayAdapter panAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item,panNames);

        // Add them to the views
        myView.songTimeSignature.setAdapter(timeSignatureAdapter);
        myView.songTempo.setAdapter(tempoAdapter);
        myView.tickSound.setAdapter(soundAdapter);
        myView.tockSound.setAdapter(soundAdapter);
        myView.metronomePan.setAdapter(panAdapter);

        // Make sure the views scroll to the selected item
        ExposedDropDownSelection exposedDropDownSelection = new ExposedDropDownSelection();
        exposedDropDownSelection.keepSelectionPosition(myView.songTimeSignature, timeSignatures);
        exposedDropDownSelection.keepSelectionPosition(myView.songTempo, tempos);
        exposedDropDownSelection.keepSelectionPosition(myView.tickSound,soundNames);
        exposedDropDownSelection.keepSelectionPosition(myView.tockSound,soundNames);
    }

    private void addSoundItem(String filename, String description) {
        soundFiles.add(filename);
        soundNames.add(description);
    }

    private void setupPreferences() {
        // Get the song values
        myView.songTempo.setText(mainActivityInterface.getSong().getTempo());
        myView.songTimeSignature.setText(mainActivityInterface.getSong().getTimesig());

        // Get the metronome pan value
        switch (mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"metronomePan","C")) {
            case "C":
            default:
                myView.metronomePan.setText(panNames.get(1));
                break;
            case "L":
                myView.metronomePan.setText(panNames.get(0));
                break;
            case "R":
                myView.metronomePan.setText(panNames.get(2));
                break;
        }

        // Set the visual metronome
        myView.visualMetronome.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"metronomeShowVisual",false));

        // Get the max bars required
        myView.maxBars.setHint(getMaxBars(mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(),"metronomeLength",0)));

        // Get the metronome tick and tock sounds
        String tickFile = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"metronomeTickSound","digital_high");
        String tockFile = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"metronomeTockSound","digital_low");
        int positionTick = soundFiles.indexOf(tickFile);
        int positionTock = soundFiles.indexOf(tockFile);
        myView.tickSound.setText(soundNames.get(positionTick));
        myView.tockSound.setText(soundNames.get(positionTock));

        // Get the volumes of the metronome sounds
        float tickVol = mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),"metronomeTickVol",0.8f);
        float tockVol = mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),"metronomeTockVol",0.6f);
        myView.tickVolume.setValue((int)(tickVol*100.0f));
        myView.tockVolume.setValue((int)(tockVol*100.0f));
        myView.tickVolume.setHint(getVolPercentage(tickVol*100.0f));
        myView.tockVolume.setHint(getVolPercentage(tockVol*100.0f));

        // Set the stop or start icon
        setStartStopIcon(mainActivityInterface.getMetronome().getIsRunning());
    }

    private String getVolPercentage(float vol) {
        return (int)(vol) + "%";
    }

    private String getMaxBars(int bars) {
        if (bars==0) {
            return "-";
        } else {
            return ""+bars;
        }
    }
    private void setupListeners() {
        myView.songTempo.addTextChangedListener(new MyTextWatcher("songTempo", myView.songTempo));
        myView.songTimeSignature.addTextChangedListener(new MyTextWatcher("songTimeSignature", myView.songTimeSignature));
        myView.metronomePan.addTextChangedListener(new MyTextWatcher("metronomePan", myView.metronomePan));
        myView.tickSound.addTextChangedListener(new MyTextWatcher("metronomeTickSound", myView.tickSound));
        myView.tockSound.addTextChangedListener(new MyTextWatcher("metronomeTockSound", myView.tockSound));
        myView.tickVolume.addOnSliderTouchListener(new MySliderTouchListener("metronomeTickVol"));
        myView.tickVolume.addOnChangeListener(new MySliderChangeListener("metronomeTickVol"));
        myView.tockVolume.addOnSliderTouchListener(new MySliderTouchListener("metronomeTockVol"));
        myView.tockVolume.addOnChangeListener(new MySliderChangeListener("metronomeTockVol"));
        myView.maxBars.addOnSliderTouchListener(new MySliderTouchListener("metronomeLength"));
        myView.maxBars.addOnChangeListener(new MySliderChangeListener("metronomeLength"));
        myView.startStopButton.setOnClickListener(button -> {
            // Change the button based on what the metronome wasn't doing as it will be in a mo!
            setStartStopIcon(!mainActivityInterface.getMetronome().getIsRunning());
            mainActivityInterface.getMetronome().startMetronome(mainActivityInterface.getActivity(),requireContext(),mainActivityInterface);
        });
        myView.visualMetronome.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"metronomeShowVisual",isChecked);
            mainActivityInterface.getMetronome().setVisualMetronome(requireContext(),mainActivityInterface);
        });

        // Set up a recurring task to check the isRunning status and update the button as required
        isRunningTimer = new Timer();
        isRunningTask = new TimerTask() {
            public void run() {
                isRunningHandler.post(() -> setStartStopIcon(mainActivityInterface.getMetronome().getIsRunning()));
            }
        };
        isRunningTimer.schedule(isRunningTask,0,1000);
    }

    private void restartMetronome() {
        if (mainActivityInterface.getMetronome().getIsRunning()) {
            mainActivityInterface.getMetronome().stopMetronome(mainActivityInterface);
            mainActivityInterface.getMetronome().startMetronome(mainActivityInterface.getActivity(),requireContext(),mainActivityInterface);
        }
    }

    private void setStartStopIcon(boolean isRunning) {
        if (isRunning) {
            // Set the icon to stop
            myView.startStopButton.setImageDrawable(AppCompatResources.getDrawable(requireContext(),R.drawable.ic_stop_white_36dp));
        } else {
            // Set the icon to play
            myView.startStopButton.setImageDrawable(AppCompatResources.getDrawable(requireContext(),R.drawable.ic_play_white_36dp));
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private final String preference;
        private final ExposedDropDown exposedDropDown;

        MyTextWatcher(String preference, ExposedDropDown exposedDropDown) {
            this.preference = preference;
            this.exposedDropDown = exposedDropDown;
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void afterTextChanged(Editable editable) {
            int position;
            switch (preference) {
                case "songTempo":
                    mainActivityInterface.getSong().setTempo(exposedDropDown.getText().toString());
                    restartMetronome();
                    // TODO initiate save song
                    // TODO update the sql or nonopensongsql databases too
                    break;
                case "songTimeSignature":
                    mainActivityInterface.getSong().setTimesig(exposedDropDown.getText().toString());
                    restartMetronome();
                    // TODO initiate save song
                    // TODO update the sql or nonopensongsql databases too
                    break;
                case "metronomePan":
                    String chosen = exposedDropDown.getText().toString();
                    position = panNames.indexOf(chosen);
                    mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),preference,panLetters.get(position));
                    mainActivityInterface.getMetronome().setVolumes(requireContext(),mainActivityInterface);
                    break;
                case "metronomeTickSound":
                case "metronomeTockSound":
                    position = soundNames.indexOf(exposedDropDown.getText().toString());
                    mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),preference,soundFiles.get(position));
                    restartMetronome();
                    break;
            }
        }
    }

    private class MySliderTouchListener implements Slider.OnSliderTouchListener {

        private final String preference;

        MySliderTouchListener(String preference) {
            this.preference = preference;
        }

        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) { }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            switch (preference) {
                case "metronomeTickVol":
                case "metronomeTockVol":
                    float newVol = slider.getValue() / 100.0f;
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(), preference, newVol);
                    mainActivityInterface.getMetronome().setVolumes(requireContext(), mainActivityInterface);
                    break;
                case "metronomeLength":
                    int bars = (int)slider.getValue();
                    mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(), preference, bars);
                    mainActivityInterface.getMetronome().setBarsAndBeats(requireContext(),mainActivityInterface);
                    break;
            }
        }
    }

    private class MySliderChangeListener implements Slider.OnChangeListener {

        private final String preference;

        MySliderChangeListener(String preference) {
            this.preference = preference;
        }

        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            switch (preference) {
                case "metronomeTickVol":
                    myView.tickVolume.setHint(getVolPercentage(value));
                    break;
                case "metronomeTockVol":
                    myView.tockVolume.setHint(getVolPercentage(value));
                    break;
                case "metronomeLength":
                    myView.maxBars.setHint(getMaxBars((int)value));
                    break;
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Remove the timer task that checks for metronome isRunning
        if (isRunningTimer!=null) {
            isRunningTimer.cancel();
            isRunningTimer.purge();
        }
        if (isRunningTask!=null) {
            isRunningTask.cancel();
            isRunningTask = null;
        }
        isRunningHandler = null;
    }
}

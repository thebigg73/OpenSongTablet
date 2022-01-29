package com.garethevans.church.opensongtablet.metronome;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
    private Timer isRunningTimer;
    private TimerTask isRunningTask;
    private Handler isRunningHandler = new Handler(), tapTempoHandlerCheck, tapTempoHandlerReset;
    private Runnable tapTempoRunnableCheck, tapTempoRunnableReset;
    private long old_time = 0L;
    private int total_calc_bpm = 0, total_counts = 0;
    private final String TAG = "MetronomeFragment";

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
        ArrayList<String> signatureBeats = new ArrayList<>();
        signatureBeats.add("");
        for (int x=1; x<17; x++) {
            signatureBeats.add(""+x);
        }
        ArrayList<String> signatureDivisions = new ArrayList<>();
        signatureDivisions.add("");
        signatureDivisions.add("1");
        signatureDivisions.add("2");
        signatureDivisions.add("4");
        signatureDivisions.add("8");

        // Get the tempos
        ArrayList<String> tempos = new ArrayList<>();
        tempos.add("");
        for (int x=40; x<300; x++) {
            tempos.add(""+x);
        }
        String tempoBpm = getString(R.string.tempo) + " (" + getString(R.string.bpm) + ")";
        myView.songTempo.setText(tempoBpm);
        myView.songTempo.setHint(tempoBpm);

        // Set the adapters
        ExposedDropDownArrayAdapter tickAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.tickSound, R.layout.view_exposed_dropdown_item, soundNames);
        ExposedDropDownArrayAdapter tockAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.tockSound, R.layout.view_exposed_dropdown_item, soundNames);
        ExposedDropDownArrayAdapter signatureBeatAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.signatureBeats, R.layout.view_exposed_dropdown_item, signatureBeats);
        ExposedDropDownArrayAdapter signatureDivisionAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.signatureDivisions, R.layout.view_exposed_dropdown_item, signatureDivisions);
        ExposedDropDownArrayAdapter tempoAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.songTempo, R.layout.view_exposed_dropdown_item, tempos);

        // Add them to the views
        myView.signatureBeats.setAdapter(signatureBeatAdapter);
        myView.signatureDivisions.setAdapter(signatureDivisionAdapter);
        myView.songTempo.setAdapter(tempoAdapter);
        myView.tickSound.setAdapter(tickAdapter);
        myView.tockSound.setAdapter(tockAdapter);

    }

    private void addSoundItem(String filename, String description) {
        soundFiles.add(filename);
        soundNames.add(description);
    }

    private void setupPreferences() {
        // Get the song values
        myView.songTempo.setText(mainActivityInterface.getSong().getTempo());
        ArrayList<String> timeSignature = mainActivityInterface.getMetronome().processTimeSignature(mainActivityInterface);
        myView.signatureBeats.setText(timeSignature.get(0));
        myView.signatureDivisions.setText(timeSignature.get(1));

        // Get the metronome pan value
        switch (mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"metronomePan","C")) {
            case "C":
            default:
                myView.metronomePan.setSliderPos(1);
                break;
            case "L":
                myView.metronomePan.setSliderPos(0);
                break;
            case "R":
                myView.metronomePan.setSliderPos(2);
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

        myView.tickVolume.setLabelFormatter(value -> ((int)value)+"%");
        myView.tockVolume.setLabelFormatter(value -> ((int)value)+"%");

        // Set the stop or start icon
        setStartStopIcon(mainActivityInterface.getMetronome().getIsRunning());
    }

    private String getVolPercentage(float vol) {
        return (int)(vol) + "%";
    }

    private String getMaxBars(int bars) {
        if (bars==0) {
            return getString(R.string.on);
        } else {
            return ""+bars;
        }
    }

    private void setupListeners() {
        myView.songTempo.addTextChangedListener(new MyTextWatcher("songTempo", myView.songTempo));
        myView.signatureBeats.addTextChangedListener(new MyTextWatcher("songTimeSignature_beats", myView.signatureBeats));
        myView.signatureDivisions.addTextChangedListener(new MyTextWatcher("songTimeSignature_divisions", myView.signatureDivisions));
        myView.metronomePan.addOnChangeListener((slider, value, fromUser) -> updateMetronomePan());
        myView.tickSound.addTextChangedListener(new MyTextWatcher("metronomeTickSound", myView.tickSound));
        myView.tockSound.addTextChangedListener(new MyTextWatcher("metronomeTockSound", myView.tockSound));
        myView.tickVolume.addOnSliderTouchListener(new MySliderTouchListener("metronomeTickVol"));
        myView.tickVolume.addOnChangeListener(new MySliderChangeListener("metronomeTickVol"));
        myView.tockVolume.addOnSliderTouchListener(new MySliderTouchListener("metronomeTockVol"));
        myView.tockVolume.addOnChangeListener(new MySliderChangeListener("metronomeTockVol"));
        myView.maxBars.addOnSliderTouchListener(new MySliderTouchListener("metronomeLength"));
        myView.maxBars.addOnChangeListener(new MySliderChangeListener("metronomeLength"));

        myView.scrollView.setFabToAnimate(myView.startStopButton);


        myView.startStopButton.setOnClickListener(button -> {
            // Change the button based on what the metronome wasn't doing as it will be in a mo!
            setStartStopIcon(!mainActivityInterface.getMetronome().getIsRunning());
            mainActivityInterface.getMetronome().startMetronome(requireActivity(),requireContext(),mainActivityInterface);
        });
        myView.visualMetronome.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"metronomeShowVisual",isChecked);
            mainActivityInterface.getMetronome().setVisualMetronome(requireContext(),mainActivityInterface);
        });
        myView.tapTempo.setOnClickListener(button -> tapTempo());

        // Set up a recurring task to check the isRunning status and update the button as required
        isRunningTimer = new Timer();
        isRunningTask = new TimerTask() {
            public void run() {
                isRunningHandler.post(() -> setStartStopIcon(mainActivityInterface.getMetronome().getIsRunning()));
            }
        };
        isRunningTimer.schedule(isRunningTask,0,1000);

        // Initialise the tapTempo values
        total_calc_bpm = 0;
        total_counts = 0;
        tapTempoRunnableCheck = () -> {
            // This is called after 2 seconds when a tap is initiated
            // Any previous instance is of course cancelled first
            requireActivity().runOnUiThread(() -> {
                myView.tapTempo.setEnabled(false);
                myView.tapTempo.setText(getString(R.string.reset));
                myView.tapTempo.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                // Waited too long, reset count
                total_calc_bpm = 0;
                total_counts = 0;
            });
            if (tapTempoHandlerReset!=null) {
                tapTempoHandlerReset.removeCallbacks(tapTempoRunnableReset);
            }
            tapTempoHandlerReset = new Handler();
            tapTempoHandlerReset.postDelayed(tapTempoRunnableReset,500);
        };
        tapTempoRunnableReset = () -> {
            // Reset the tap tempo timer
            requireActivity().runOnUiThread(() -> {
                myView.tapTempo.setEnabled(true);
                myView.tapTempo.setText(getString(R.string.tap_tempo));
                myView.tapTempo.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
            });
            // Start the metronome
            mainActivityInterface.getMetronome().startMetronome(requireActivity(),
                    requireContext(),mainActivityInterface);
        };
    }

    private void updateMetronomePan() {
        int value = myView.metronomePan.getValue();
        String pan;
        switch (value) {
            case 0:
                pan = "L";
                break;
            case 1:
            default:
                pan = "C";
                break;
            case 2:
                pan = "R";
                break;
        }
        Log.d(TAG,"pan="+pan);
        mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),"metronomePan",pan);
        mainActivityInterface.getMetronome().setVolumes(requireContext(),mainActivityInterface);
    }
    private void restartMetronome() {
        if (mainActivityInterface.getMetronome().getIsRunning()) {
            mainActivityInterface.getMetronome().stopMetronome(mainActivityInterface);
            mainActivityInterface.getMetronome().startMetronome(requireActivity(),requireContext(),mainActivityInterface);
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

    private void tapTempo() {
        // This function checks the previous tap_tempo time and calculates the bpm
        // Variables for tap tempo

        // When tapping for compound/complex time signatures
        // They sometimes go in double or triple time
        if (mainActivityInterface.getMetronome().getIsRunning()) {
            mainActivityInterface.getMetronome().stopMetronome(mainActivityInterface);
        }

        long new_time = System.currentTimeMillis();
        long time_passed = new_time - old_time;
        int calc_bpm = Math.round((1 / ((float) time_passed / 1000)) * 60);

        // Need to decide on the time sig.
        // If it ends in /2, then double the tempo
        // If it ends in /4, then leave as is
        // If it ends in /8, then half it
        // If it isn't set, set it to default as 4/4
        String timeSig = mainActivityInterface.getSong().getTimesig();
        if (timeSig.isEmpty()) {
            myView.signatureBeats.setText("4");
            myView.signatureDivisions.setText("4");
            mainActivityInterface.getSong().setTimesig("4/4");
        }

        if (time_passed < 1500) {
            total_calc_bpm += calc_bpm;
            total_counts++;
        } else {
            // Waited too long, reset count
            total_calc_bpm = 0;
            total_counts = 0;
        }

        // Based on the time signature, get a meterDivisionFactor
        float meterTimeFactor = mainActivityInterface.getMetronome().meterTimeFactor();
        int av_bpm = Math.round(((float) total_calc_bpm / (float) total_counts) / meterTimeFactor);

        if (av_bpm < 300 && av_bpm >= 40) {
            myView.songTempo.setText(""+av_bpm);
            mainActivityInterface.getSong().setTempo(""+av_bpm);

        } else if (av_bpm <40) {
            myView.songTempo.setText("40");
            mainActivityInterface.getSong().setTempo("40");
        }  else {
            myView.songTempo.setText("299");
            mainActivityInterface.getSong().setTempo("299");
        }

        old_time = new_time;

        // Set a handler to check the button tap.
        // If the counts haven't increased after 1.5 seconds, reset it
        if (tapTempoHandlerCheck!=null) {
            tapTempoHandlerCheck.removeCallbacks(tapTempoRunnableCheck);
        }
        tapTempoHandlerCheck = new Handler();
        tapTempoHandlerCheck.postDelayed(tapTempoRunnableCheck,1500);
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
            // Some of these are saved as preferences, others are saved with the song
            int position;
            switch (preference) {
                case "songTempo":
                    mainActivityInterface.getSong().setTempo(exposedDropDown.getText().toString());
                    mainActivityInterface.getSaveSong().updateSong(requireContext(),mainActivityInterface);
                    restartMetronome();
                    break;
                case "songTimeSignature_beats":
                case "songTimeSignature_divisions":
                    String beats;
                    String divisions;
                    if (preference.endsWith("_beats")) {
                        beats = exposedDropDown.getText().toString();
                        divisions = myView.signatureDivisions.getText().toString();
                    } else {
                        divisions = exposedDropDown.getText().toString();
                        beats = myView.signatureBeats.getText().toString();
                    }
                    String timeSig = beats + "/" + divisions;
                    if (!beats.isEmpty() && !divisions.isEmpty()) {
                        mainActivityInterface.getSong().setTimesig(timeSig);
                    } else {
                        mainActivityInterface.getSong().setTimesig("");
                    }
                    restartMetronome();
                    mainActivityInterface.getSaveSong().updateSong(requireContext(),mainActivityInterface);
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
        // Also remove any tap temp handlers
        if (isRunningTimer!=null) {
            isRunningTimer.cancel();
            isRunningTimer.purge();
        }
        if (isRunningTask!=null) {
            isRunningTask.cancel();
            isRunningTask = null;
        }
        isRunningHandler = null;
        if (tapTempoHandlerCheck!=null) {
            tapTempoHandlerCheck.removeCallbacks(tapTempoRunnableCheck);
            tapTempoHandlerCheck = null;
        }
        if (tapTempoHandlerReset!=null) {
            tapTempoHandlerReset.removeCallbacks(tapTempoRunnableReset);
            tapTempoHandlerReset = null;
        }
    }
}

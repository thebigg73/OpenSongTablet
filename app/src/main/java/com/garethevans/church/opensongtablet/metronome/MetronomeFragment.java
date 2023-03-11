package com.garethevans.church.opensongtablet.metronome;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.garethevans.church.opensongtablet.databinding.SettingsMetronomeBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private String metronome_string="", website_metronome_string="", sound_low_string="",
            sound_high_string="", sound_bass_drum_string="", sound_bell_string, sound_click_string="",
            sound_digital_string="", sound_hihat_string="", sound_stick_string="", tap_tempo_string="",
            sound_wood_string="", tempo_string="", bpm_string="", on_string="", reset_string="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsMetronomeBinding.inflate(inflater,container,false);

        prepareStrings();

        mainActivityInterface.updateToolbar(metronome_string);
        mainActivityInterface.updateToolbarHelp(website_metronome_string);

        // Set up the values for the exposeddropdowns
        initialiseDropDowns();

        // Set up initial preferences
        setupPreferences();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            metronome_string = getString(R.string.metronome);
            website_metronome_string = getString(R.string.website_metronome);
            sound_low_string = getString(R.string.sound_low);
            sound_high_string = getString(R.string.sound_high);
            sound_bass_drum_string = getString(R.string.sound_bass_drum);
            sound_bell_string = getString(R.string.sound_bell);
            sound_click_string = getString(R.string.sound_click);
            sound_digital_string = getString(R.string.sound_digital);
            sound_hihat_string = getString(R.string.sound_hihat);
            sound_stick_string = getString(R.string.sound_stick);
            sound_wood_string = getString(R.string.sound_wood);
            tempo_string = getString(R.string.tempo);
            bpm_string = getString(R.string.bpm);
            on_string = getString(R.string.on);
            reset_string = getString(R.string.reset);
            tap_tempo_string = getString(R.string.tap_tempo);
        }
    }

    private void initialiseDropDowns() {
        // Get the available sounds by filename and description
        soundFiles = new ArrayList<>();
        soundNames = new ArrayList<>();
        String low = sound_low_string;
        String high = sound_high_string;
        addSoundItem("","");
        addSoundItem("bass_drum",sound_bass_drum_string);
        addSoundItem("bell_high", sound_bell_string+" ("+high+")");
        addSoundItem("bell_low", sound_bell_string+" ("+low+")");
        addSoundItem("click_1_high", sound_click_string+" 1 ("+high+")");
        addSoundItem("click_1_low", sound_click_string+" 1 ("+low+")");
        addSoundItem("click_2_high", sound_click_string+" 2 ("+high+")");
        addSoundItem("click_2_low", sound_click_string+" 2 ("+low+")");
        addSoundItem("digital_high", sound_digital_string+" ("+high+")");
        addSoundItem("digital_low", sound_digital_string+" ("+low+")");
        addSoundItem("hat_high", sound_hihat_string+" ("+high+")");
        addSoundItem("hat_low", sound_hihat_string+" ("+low+")");
        addSoundItem("stick_high", sound_stick_string+" ("+high+")");
        addSoundItem("stick_low", sound_stick_string+" ("+low+")");
        addSoundItem("wood_high", sound_wood_string+" ("+high+")");
        addSoundItem("wood_low", sound_wood_string+" ("+low+")");

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
        String tempoBpm = tempo_string + " (" + bpm_string + ")";
        myView.songTempo.setText(tempoBpm);
        myView.songTempo.setHint(tempoBpm);

        // Set the adapters
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter tickAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.tickSound, R.layout.view_exposed_dropdown_item, soundNames);
            ExposedDropDownArrayAdapter tockAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.tockSound, R.layout.view_exposed_dropdown_item, soundNames);
            ExposedDropDownArrayAdapter signatureBeatAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.signatureBeats, R.layout.view_exposed_dropdown_item, signatureBeats);
            ExposedDropDownArrayAdapter signatureDivisionAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.signatureDivisions, R.layout.view_exposed_dropdown_item, signatureDivisions);
            ExposedDropDownArrayAdapter tempoAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.songTempo, R.layout.view_exposed_dropdown_item, tempos);

            // Add them to the views
            myView.signatureBeats.setAdapter(signatureBeatAdapter);
            myView.signatureDivisions.setAdapter(signatureDivisionAdapter);
            myView.songTempo.setAdapter(tempoAdapter);
            myView.tickSound.setAdapter(tickAdapter);
            myView.tockSound.setAdapter(tockAdapter);
        }
    }

    private void addSoundItem(String filename, String description) {
        soundFiles.add(filename);
        soundNames.add(description);
    }

    private void setupPreferences() {
        // Get the song values
        myView.songTempo.setText(mainActivityInterface.getSong().getTempo());
        ArrayList<String> timeSignature = mainActivityInterface.getMetronome().processTimeSignature();
        myView.signatureBeats.setText(timeSignature.get(0));
        myView.signatureDivisions.setText(timeSignature.get(1));

        // Get the metronome pan value
        switch (mainActivityInterface.getPreferences().getMyPreferenceString("metronomePan","C")) {
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
        myView.visualMetronome.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("metronomeShowVisual",false));

        // Get the max bars required
        myView.maxBars.setHint(getMaxBars(mainActivityInterface.getPreferences().getMyPreferenceInt("metronomeLength",0)));

        // Get the metronome tick and tock sounds
        String tickFile = mainActivityInterface.getPreferences().getMyPreferenceString("metronomeTickSound","digital_high");
        String tockFile = mainActivityInterface.getPreferences().getMyPreferenceString("metronomeTockSound","digital_low");
        int positionTick = soundFiles.indexOf(tickFile);
        int positionTock = soundFiles.indexOf(tockFile);
        myView.tickSound.setText(soundNames.get(positionTick));
        myView.tockSound.setText(soundNames.get(positionTock));

        // Get the volumes of the metronome sounds
        float tickVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("metronomeTickVol",0.8f);
        float tockVol = mainActivityInterface.getPreferences().getMyPreferenceFloat("metronomeTockVol",0.6f);
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
            return on_string;
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
            mainActivityInterface.getMetronome().startMetronome();
        });
        myView.visualMetronome.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("metronomeShowVisual",isChecked);
            mainActivityInterface.getMetronome().setVisualMetronome();
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
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    myView.tapTempo.setEnabled(false);
                    myView.tapTempo.setText(reset_string);
                    myView.tapTempo.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    // Waited too long, reset count
                    total_calc_bpm = 0;
                    total_counts = 0;
                });
            });
            if (tapTempoHandlerReset!=null) {
                tapTempoHandlerReset.removeCallbacks(tapTempoRunnableReset);
            }
            tapTempoHandlerReset = new Handler();
            tapTempoHandlerReset.postDelayed(tapTempoRunnableReset,500);
        };
        tapTempoRunnableReset = () -> {
            // Reset the tap tempo timer
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                            myView.tapTempo.setEnabled(true);
                            myView.tapTempo.setText(tap_tempo_string);
                            myView.tapTempo.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                });
            });
            // Start the metronome
            mainActivityInterface.getMetronome().startMetronome();
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
        mainActivityInterface.getPreferences().setMyPreferenceString("metronomePan",pan);
        mainActivityInterface.getMetronome().setVolumes();
    }
    private void restartMetronome() {
        if (mainActivityInterface.getMetronome().getIsRunning()) {
            mainActivityInterface.getMetronome().stopMetronome();
            mainActivityInterface.getMetronome().startMetronome();
        }
    }

    private void setStartStopIcon(boolean isRunning) {
        if (isRunning && getContext()!=null) {
            // Set the icon to stop
            try {
                myView.startStopButton.post(() -> {
                    if (getContext()!=null) {
                        myView.startStopButton.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.stop));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (getContext()!=null) {
            // Set the icon to play
            try {
                myView.startStopButton.post(() -> {
                    if (getContext()!=null) {
                        myView.startStopButton.setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.play));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void tapTempo() {
        // This function checks the previous tap_tempo time and calculates the bpm
        // Variables for tap tempo

        // When tapping for compound/complex time signatures
        // They sometimes go in double or triple time
        if (mainActivityInterface.getMetronome().getIsRunning()) {
            mainActivityInterface.getMetronome().stopMetronome();
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
                    mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong());
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
                    mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong());
                    break;
                case "metronomeTickSound":
                case "metronomeTockSound":
                    position = soundNames.indexOf(exposedDropDown.getText().toString());
                    mainActivityInterface.getPreferences().setMyPreferenceString(preference,soundFiles.get(position));
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
                    mainActivityInterface.getPreferences().setMyPreferenceFloat(preference, newVol);
                    mainActivityInterface.getMetronome().setVolumes();
                    break;
                case "metronomeLength":
                    int bars = (int)slider.getValue();
                    mainActivityInterface.getPreferences().setMyPreferenceInt(preference, bars);
                    mainActivityInterface.getMetronome().setBarsAndBeats();
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
            // IV - Occasional crash on detach - adding this may be the cure?
            isRunningTimer = null;
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

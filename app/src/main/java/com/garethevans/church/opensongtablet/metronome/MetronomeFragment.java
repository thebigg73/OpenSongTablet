package com.garethevans.church.opensongtablet.metronome;

import android.content.Context;
import android.os.Bundle;
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

    private final String TAG = "MetronomeFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsMetronomeBinding myView;
    private ArrayList<String> soundFiles;
    private ArrayList<String> soundNames;
    private Timer isRunningTimer;
    private TimerTask isRunningTask;
    private String metronome_string="", website_metronome_string="", sound_low_string="",
            sound_high_string="", sound_bass_drum_string="", sound_bell_string, sound_click_string="",
            sound_digital_string="", sound_hihat_string="", sound_stick_string="",
            sound_wood_string="", tempo_string="", bpm_string="", on_string="",
            not_set_string="";
    private String webAddress;
    private boolean tapping = false;
    private MetronomeTapTempo metronomeTapTempo;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(metronome_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

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

        webAddress = website_metronome_string;

        // Check if we can show the wearOS switch
        mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeFragment(this);
        mainActivityInterface.getDrumViewModel().getMetronomeWearOS().checkWearOSValid();
        mainActivityInterface.getDrumViewModel().prepareSongValues(mainActivityInterface.getSong());

        // Set up the views and populate them
        initialiseDropDowns();

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
            not_set_string = getString(R.string.is_not_set) + " - " + getString(R.string.use_default);
        }
    }

    private void initialiseDropDowns() {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Get the available sounds by filename and description
            soundFiles = new ArrayList<>();
            soundNames = new ArrayList<>();
            String low = sound_low_string;
            String high = sound_high_string;
            addSoundItem("", "");
            addSoundItem("bass_drum", sound_bass_drum_string);
            addSoundItem("bell_high", sound_bell_string + " (" + high + ")");
            addSoundItem("bell_low", sound_bell_string + " (" + low + ")");
            addSoundItem("click_1_high", sound_click_string + " 1 (" + high + ")");
            addSoundItem("click_1_low", sound_click_string + " 1 (" + low + ")");
            addSoundItem("click_2_high", sound_click_string + " 2 (" + high + ")");
            addSoundItem("click_2_low", sound_click_string + " 2 (" + low + ")");
            addSoundItem("digital_high", sound_digital_string + " (" + high + ")");
            addSoundItem("digital_low", sound_digital_string + " (" + low + ")");
            addSoundItem("hat_high", sound_hihat_string + " (" + high + ")");
            addSoundItem("hat_low", sound_hihat_string + " (" + low + ")");
            addSoundItem("stick_high", sound_stick_string + " (" + high + ")");
            addSoundItem("stick_low", sound_stick_string + " (" + low + ")");
            addSoundItem("wood_high", sound_wood_string + " (" + high + ")");
            addSoundItem("wood_low", sound_wood_string + " (" + low + ")");

            // Get the timesignatures
            ArrayList<String> signatureBeats = new ArrayList<>();
            signatureBeats.add("");
            for (int x = 1; x < 17; x++) {
                signatureBeats.add(String.valueOf(x));
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
            for (int x = 40; x < 300; x++) {
                tempos.add(String.valueOf(x));
            }

            // Set the adapters
            if (getContext() != null) {
                ExposedDropDownArrayAdapter tickAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.tickSound, R.layout.view_exposed_dropdown_item, soundNames);
                ExposedDropDownArrayAdapter tockAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.tockSound, R.layout.view_exposed_dropdown_item, soundNames);
                ExposedDropDownArrayAdapter signatureBeatAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.signatureBeats, R.layout.view_exposed_dropdown_item, signatureBeats);
                ExposedDropDownArrayAdapter signatureDivisionAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.signatureDivisions, R.layout.view_exposed_dropdown_item, signatureDivisions);
                ExposedDropDownArrayAdapter tempoAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.songTempo, R.layout.view_exposed_dropdown_item, tempos);

                // Add them to the views
                mainActivityInterface.getMainHandler().post(() -> {
                    myView.signatureBeats.setAdapter(signatureBeatAdapter);
                    myView.signatureDivisions.setAdapter(signatureDivisionAdapter);
                    myView.songTempo.setAdapter(tempoAdapter);
                    myView.tickSound.setAdapter(tickAdapter);
                    myView.tockSound.setAdapter(tockAdapter);
                });
            }
            setupPreferences();
        });
    }

    private void addSoundItem(String filename, String description) {
        soundFiles.add(filename);
        soundNames.add(description);
    }

    private void setupPreferences() {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Get the song values
            // If we don't have a tempo or time signature, make it 100bpm and 4/4 by default and update the song
            boolean updateSong = false;

            if (mainActivityInterface.getDrumViewModel().getThisBpm()==-1) {
                mainActivityInterface.getSong().setTempo("100");
                updateSong = true;
            }

            if (mainActivityInterface.getDrumViewModel().getThisBeats()==-1 || mainActivityInterface.getDrumViewModel().getThisDivisions()==-1) {
                mainActivityInterface.getSong().setTimesig("4/4");
                updateSong = true;
            }

            if (updateSong) {
                Log.d(TAG,"updating song");
                mainActivityInterface.getDrumViewModel().stopMetronome();
                mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(), false);
                mainActivityInterface.getShowToast().doIt(not_set_string);
                setStartStopIcon(false);
            }

            // Set the default values on the UI
            mainActivityInterface.getMainHandler().post(() -> {
                if (myView!=null) {
                    // Don't trigger a save - pretend we are tapping!
                    tapping = true;
                    myView.songTempo.setHint(tempo_string + " (" + bpm_string + ")");
                    if (mainActivityInterface.getDrumViewModel().getThisBpm()>=40) {
                        myView.songTempo.setText(mainActivityInterface.getSong().getTempo());
                    }
                    if (mainActivityInterface.getDrumViewModel().getThisBeats()>0) {
                        myView.signatureBeats.setText(String.valueOf(mainActivityInterface.getDrumViewModel().getThisBeats()));
                    }
                    if (mainActivityInterface.getDrumViewModel().getThisDivisions()>0) {
                        myView.signatureDivisions.setText(String.valueOf(mainActivityInterface.getDrumViewModel().getThisDivisions()));
                    }

                    // The autostart metronome feature
                    myView.metronomeAutoStart.setChecked(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeAutoStart());

                    // The WearOS switch
                    myView.wearOS.setChecked(mainActivityInterface.getDrumViewModel().getMetronomeWearOS().getMetronomeWearOS());
                }

                // Get the metronome pan value
                switch (mainActivityInterface.getDrumViewModel().getMetronome().getMetronomePan()) {
                        case "L":
                        myView.metronomePan.setSliderPos(0);
                        break;
                    case "R":
                        myView.metronomePan.setSliderPos(2);
                        break;
                    case "C":
                    default:
                        myView.metronomePan.setSliderPos(1);
                        break;
                }

                // Set the visual metronome
                myView.visualMetronome.setChecked(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeShowVisual());

                // Set the audio metronome
                myView.audioMetronome.setChecked(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeAudio());
                myView.audioSettings.setVisibility(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeAudio() ? View.VISIBLE : View.GONE);

                // Get the max bars required
                myView.maxBars.setAdjustableButtons(true);
                myView.maxBars.setValue(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeLength());
                myView.maxBars.setHint(getMaxBars(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeLength()));

                // Set the default metronome switch
                myView.metronomeDefault.setChecked(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeUseDefaults());

                // Get the metronome tick and tock sounds
                myView.tickSound.setText(soundNames.get(soundFiles.indexOf(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeTickSound())));
                myView.tockSound.setText(soundNames.get(soundFiles.indexOf(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeTockSound())));

                // Get the volumes of the metronome sounds
                myView.tickVolume.setValue((int) (mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeTickVol() * 100.0f));
                myView.tockVolume.setValue((int) (mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeTockVol() * 100.0f));
                myView.tickVolume.setHint(getVolPercentage(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeTickVol() * 100.0f));
                myView.tockVolume.setHint(getVolPercentage(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeTockVol() * 100.0f));
                myView.tickVolume.setLabelFormatter(value -> ((int) value) + "%");
                myView.tockVolume.setLabelFormatter(value -> ((int) value) + "%");

                // Set the midiClickTrack option
                myView.midiClickTrackSwitch.setChecked(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeMidi());

                // Set the stop or start icon
                setStartStopIcon(mainActivityInterface.getDrumViewModel().getMetronome().getIsRunning());

                tapping = false;

                // Set up the listeners
                setupListeners();
            });
        });
    }

    private String getVolPercentage(float vol) {
        return (int)(vol) + "%";
    }

    private String getMaxBars(int bars) {
        if (bars==0) {
            return on_string;
        } else {
            return String.valueOf(bars);
        }
    }

    private void setupListeners() {
        // We use the MetronomeTapTempo.class to deal with the tap tempo logic and listeners
        metronomeTapTempo = new MetronomeTapTempo(getContext(), this);
        metronomeTapTempo.initialiseTapTempo(mainActivityInterface.getSong(),
                myView.tapTempo, null, myView.signatureBeats, myView.signatureDivisions,
                myView.songTempo, myView.startStopButton, true);


        // Now set the button listeners on the main UI after 1 second
        mainActivityInterface.getMainHandler().postDelayed(() -> {
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
            myView.midiClickTrackSwitch.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeMidi(b));
            myView.metronomeDefault.setOnCheckedChangeListener((view,b) -> mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeUseDefaults(b));

            myView.scrollView.setFabToAnimate(myView.startStopButton);

            myView.startStopButton.setOnClickListener(button -> {
                // Change the button based on what the metronome wasn't doing as it will be in a mo!
                setStartStopIcon(!mainActivityInterface.getDrumViewModel().getMetronome().getIsRunning());
                mainActivityInterface.getDrumViewModel().toggleMetronome();
            });
            myView.visualMetronome.setOnCheckedChangeListener((compoundButton, isChecked) ->
                    mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeShowVisual(isChecked));
            myView.audioMetronome.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeAudio(isChecked);
                myView.audioSettings.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });
            myView.metronomeAutoStart.setOnCheckedChangeListener((compoundButton, isChecked) ->
                    mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeAutoStart(isChecked));

            myView.wearOS.setOnCheckedChangeListener((compoundButton, isChecked) ->
                    mainActivityInterface.getDrumViewModel().getMetronomeWearOS().setMetronomeWearOS(isChecked));

        }, 1000);
    }

    private void updateMetronomePan() {
        int value = myView.metronomePan.getValue();
        String pan;
        switch (value) {
            case 0:
                pan = "L";
                break;
            case 2:
                pan = "R";
                break;
            case 1:
            default:
                pan = "C";
                break;
        }
        mainActivityInterface.getDrumViewModel().getMetronome().setMetronomePan(pan);
    }
    private void restartMetronome() {
        // Stop the metronome once we check if it was running
        boolean wasRunning = mainActivityInterface.getDrumViewModel().getMetronome().getIsRunning();
        setStartStopIcon(false);
        mainActivityInterface.getDrumViewModel().stopMetronome();
        mainActivityInterface.getDrumViewModel().prepareSongValues(mainActivityInterface.getSong());

        // Now restart it again (if it was running) in 1 sec to allow for saves, etc.
        mainActivityInterface.getMainHandler().postDelayed(() -> {
            if (wasRunning) {
                mainActivityInterface.getDrumViewModel().startMetronome();
                setStartStopIcon(true);
            }
        },1000);
    }

    public void setStartStopIcon(boolean isRunning) {
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
                    if (!tapping) {
                        Log.d(TAG,"songTempo changed");
                        mainActivityInterface.getSong().setTempo(exposedDropDown.getText().toString());
                        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(), false);
                        restartMetronome();
                    }
                    break;
                case "songTimeSignature_beats":
                case "songTimeSignature_divisions":
                    if (!tapping) {
                        Log.d(TAG,"timeSignature changed");
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
                        mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(), false);
                        restartMetronome();
                    }
                    break;
                case "metronomeTickSound":
                case "metronomeTockSound":
                    position = soundNames.indexOf(exposedDropDown.getText().toString());
                    if (position == -1) {
                        position = 0;
                    }
                    if (preference.equals("metronomeTickSound")) {
                        mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeTickSound(soundFiles.get(position));
                    } else {
                        mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeTockSound(soundFiles.get(position));
                    }
                    mainActivityInterface.getDrumViewModel().getDrumSoundManager().updateMetronomeSounds(getContext());
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
            float newVol = slider.getValue() / 100.0f;
            switch (preference) {
                case "metronomeTickVol":
                    mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeTickVol(newVol);
                    break;
                case "metronomeTockVol":
                    mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeTockVol(newVol);
                    break;
                case "metronomeLength":
                    int bars = (int)slider.getValue();
                    mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeLength(bars);
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
                    if (!fromUser) {
                        mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeLength((int)value);
                    }
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

        if (metronomeTapTempo!=null) {
            metronomeTapTempo.cleanUp();
        }
        metronomeTapTempo = null;

        mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeFragment(null);
    }

    // Show or hide the wearOS switch
    public void updateWearOS(boolean show) {
        myView.wearOS.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    // Tapping means we don't save yet
    public void setTapping(boolean tapping) {
        this.tapping = tapping;
    }
}

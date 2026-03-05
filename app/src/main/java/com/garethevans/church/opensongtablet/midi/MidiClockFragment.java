package com.garethevans.church.opensongtablet.midi;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsMidiClockBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MidiClockFragment extends Fragment {

    // This class allows the user to switch on master MIDI clock send
    // It also allows a MIDI click track to be sent

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MidiClockFragment";
    private MainActivityInterface mainActivityInterface;
    private String midi_clock_string="", midi_clock_webpage="", test_string="",
            stop_string="", no_device_string="", start_string="", on_string="";
    private Drawable play_icon, stop_icon;
    private SettingsMidiClockBinding myView;
    private ScheduledExecutorService midiTestExecutor;
    private ScheduledFuture<?> future;
    private final int midiDelay = 500;

    private int count = 1;
    private final Runnable midiTestRunnable = new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (count==1) {
                    mainActivityInterface.getMidi().sendMidi(mainActivityInterface.getMidi().returnBytesFromHexText(mainActivityInterface.getMidi().getMidiClickTickMessageOn()));
                } else {
                    mainActivityInterface.getMidi().sendMidi(mainActivityInterface.getMidi().returnBytesFromHexText(mainActivityInterface.getMidi().getMidiClickTockMessageOn()));
                }
                count ++;
                if (count==5) {
                    count = 1;
                }
                future = midiTestExecutor.schedule(midiTestRunnable,midiDelay, TimeUnit.MILLISECONDS);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(midi_clock_string);
        mainActivityInterface.updateToolbarHelp(midi_clock_webpage);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        prepareStrings();
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsMidiClockBinding.inflate(inflater,container,false);

        prepareStrings();
        setupViews();
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            midi_clock_string = getString(R.string.midi_clock_click);
            midi_clock_webpage = getString(R.string.website_midi_clock);
            test_string = getString(R.string.midi_test);
            stop_string = getString(R.string.stop);
            start_string = getString(R.string.start);
            on_string = getString(R.string.on);
            play_icon = AppCompatResources.getDrawable(getContext(),R.drawable.play);
            stop_icon = AppCompatResources.getDrawable(getContext(),R.drawable.stop);
            no_device_string = getContext().getString(R.string.connections_no_devices);
        }
    }

    private void setupViews() {
        myView.midiClockSend.setChecked(mainActivityInterface.getDrumViewModel().getMidiClock().getMidiClock());
        myView.midiClockShortBurst.setChecked(mainActivityInterface.getDrumViewModel().getMidiClock().getMidiClockBurstMode());
        myView.midiClockStartStop.setChecked(mainActivityInterface.getDrumViewModel().getMidiClock().getMidiClockStartStop());
        myView.midiClickTrackChannel.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value);
            }
        });
        myView.midiClickTrackSend.setChecked(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeMidi());
        myView.midiClickTrackLayout.setVisibility(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeMidi() ? View.VISIBLE:View.GONE);
        myView.midiClickTrackChannel.setValue(mainActivityInterface.getMidi().getMidiClickTrackChannel());
        myView.midiClickTrackChannel.setHint(String.valueOf(mainActivityInterface.getMidi().getMidiClickTrackChannel()));
        myView.midiClickTrackChannel.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value);
            }
        });
        myView.midiClickTrackTick.setValue(mainActivityInterface.getMidi().getMidiClickTrackTick());
        myView.midiClickTrackTick.setHint(String.valueOf(mainActivityInterface.getMidi().getMidiClickTrackTick()));
        myView.midiClickTrackTick.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value);
            }
        });
        myView.midiClickTrackTock.setValue(mainActivityInterface.getMidi().getMidiClickTrackTock());
        myView.midiClickTrackTock.setHint(String.valueOf(mainActivityInterface.getMidi().getMidiClickTrackTock()));
        myView.midiClickTrackTock.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value);
            }
        });
        myView.midiClickTrackTickVolume.setValue(mainActivityInterface.getMidi().getMidiClickTrackTickVolume());
        myView.midiClickTrackTickVolume.setHint(String.valueOf(mainActivityInterface.getMidi().getMidiClickTrackTickVolume()));
        myView.midiClickTrackTickVolume.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value);
            }
        });
        myView.midiClickTrackTockVolume.setValue(mainActivityInterface.getMidi().getMidiClickTrackTockVolume());
        myView.midiClickTrackTockVolume.setHint(String.valueOf(mainActivityInterface.getMidi().getMidiClickTrackTockVolume()));
        myView.midiClickTrackTockVolume.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value);
            }
        });
        // Get the max bars required
        myView.maxBars.setValue(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeLength());
        myView.maxBars.setHint(getMaxBars(mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeLength()));

        myView.midiClickTest.setIcon(play_icon);
    }

    private void setListeners() {
        myView.midiClockSend.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getDrumViewModel().getMidiClock().setMidiClock(b);
            mainActivityInterface.getDrumViewModel().getMidiClock().setIsRunning(b);
            if (b) {
                mainActivityInterface.getDrumViewModel().prepareSongValues(mainActivityInterface.getSong());
                mainActivityInterface.getDrumViewModel().startTimerEngine();
                mainActivityInterface.getShowToast().doIt(midi_clock_string+": "+start_string);
            } else {
                mainActivityInterface.getShowToast().doIt(midi_clock_string+": "+stop_string);
                mainActivityInterface.getDrumViewModel().stopTimerEngine();
            }
        });
        myView.midiClockShortBurst.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getDrumViewModel().stopMidiClock();
            mainActivityInterface.getDrumViewModel().getMidiClock().setMidiClockBurstMode(b);
            mainActivityInterface.getDrumViewModel().getMidiClock().setMidiClock(myView.midiClockSend.isChecked());
            mainActivityInterface.getDrumViewModel().getMidiClock().setIsRunning(myView.midiClockSend.isChecked());
        });
        myView.midiClockStartStop.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getDrumViewModel().stopMidiClock();
            mainActivityInterface.getDrumViewModel().getMidiClock().setMidiClockStartStop(b);
            mainActivityInterface.getDrumViewModel().getMidiClock().setMidiClock(myView.midiClockSend.isChecked());
            mainActivityInterface.getDrumViewModel().getMidiClock().setIsRunning(myView.midiClockSend.isChecked());
        });
        myView.midiClickTrackSend.setOnCheckedChangeListener(((compoundButton, b) -> {
            mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeMidi(b);

            myView.midiClickTrackLayout.setVisibility(b ? View.VISIBLE:View.GONE);
        }));
        myView.midiClickTrackChannel.addOnChangeListener((slider, value, fromUser) -> {
            mainActivityInterface.getMidi().setMidiClickTrackChannel((int)value);
            myView.midiClickTrackChannel.setHint(String.valueOf((int)value));
        });
        myView.midiClickTrackTick.addOnChangeListener((slider, value, fromUser) -> {
            mainActivityInterface.getMidi().setMidiClickTrackTick((int)value);
            myView.midiClickTrackTick.setHint(String.valueOf((int)value));
        });
        myView.midiClickTrackTock.addOnChangeListener((slider, value, fromUser) -> {
            mainActivityInterface.getMidi().setMidiClickTrackTock((int)value);
            myView.midiClickTrackTock.setHint(String.valueOf((int)value));
        });
        myView.midiClickTrackTickVolume.addOnChangeListener((slider, value, fromUser) -> {
            mainActivityInterface.getMidi().setMidiClickTrackTickVolume((int)value);
            myView.midiClickTrackTickVolume.setHint(String.valueOf((int)value));
        });
        myView.midiClickTrackTockVolume.addOnChangeListener((slider, value, fromUser) -> {
            mainActivityInterface.getMidi().setMidiClickTrackTockVolume((int)value);
            myView.midiClickTrackTockVolume.setHint(String.valueOf((int)value));
        });
        myView.midiClickTest.setOnClickListener(view -> {
                    if (myView.midiClickTest.getIcon() == play_icon) {
                        myView.midiClickTest.setIcon(stop_icon);
                        myView.midiClickTest.setText(stop_string);
                        mainActivityInterface.getMidi().setUpMidiTickTock();
                        count = 1;
                        midiTestExecutor = Executors.newSingleThreadScheduledExecutor();
                        midiTestExecutor.schedule(midiTestRunnable, 0, TimeUnit.MILLISECONDS);
                    } else {
                        myView.midiClickTest.setIcon(play_icon);
                        myView.midiClickTest.setText(test_string);
                        stopTest();
                    }
                });
        myView.maxBars.addOnSliderTouchListener(new MySliderTouchListener());
        myView.maxBars.addOnChangeListener(new MySliderChangeListener());

    }

    private class MySliderTouchListener implements Slider.OnSliderTouchListener {

        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            int bars = (int) slider.getValue();
            mainActivityInterface.getDrumViewModel().getMetronome().setMetronomeLength(bars);
        }

    }
    private class MySliderChangeListener implements Slider.OnChangeListener {
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            myView.maxBars.setHint(getMaxBars((int)value));
        }
    }

    private String getMaxBars(int bars) {
        if (bars==0) {
            return on_string;
        } else {
            return String.valueOf(bars);
        }
    }

    private void stopTest() {
        if (midiTestExecutor != null && future != null) {
            future.cancel(true);
            midiTestExecutor.shutdown();
            midiTestExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            Runnable endtest = () -> midiTestExecutor.shutdown();
            try {
                midiTestExecutor.schedule(endtest, midiDelay, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                midiTestExecutor.shutdownNow();
            }
            count = 0;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTest();
    }
}

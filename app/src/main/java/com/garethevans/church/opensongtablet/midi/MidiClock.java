package com.garethevans.church.opensongtablet.midi;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class MidiClock {

    // The class dealing with sending MidiClock

    private final String TAG = "MidiClock";
    private final MainActivityInterface mainActivityInterface;
    private boolean isRunning = false;

    // Because MIDI clock is an intensive process, it has to be manually switched on after boot.
    // We keep a record of this choice here (not a persistent user preference)
    private boolean midiClock;

    // The user can choose to use a burst mode for the MIDI clock (5 seconds)
    // This is a persistent user preference
    private boolean midiClockBurstMode;

    // The user can switch on/off the sending of start and stop with the MIDI clock.
    // By default this is off.  Saved as a persistent user preference
    private boolean midiClockStartStop = false;

    public MidiClock(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        // Get our preferences
        midiClock = false;
        midiClockBurstMode = mainActivityInterface.getPreferences().getMyPreferenceBoolean("midiClockBurstMode",true);
        midiClockStartStop = mainActivityInterface.getPreferences().getMyPreferenceBoolean("midiClockStartStop",false);
    }

    // The temporary choice to send the MIDI clock
    public boolean getMidiClock() {
        return midiClock;
    }
    public void setMidiClock(boolean midiClock) {
        this.midiClock = midiClock;
    }

    // The persistent choice for the MIDI clock burst time.  0=continuous, or 1-10
    public boolean getMidiClockBurstMode() {
        return midiClockBurstMode;
    }
    public void setMidiClockBurstMode(boolean midiClockBurstMode) {
        this.midiClockBurstMode = midiClockBurstMode;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("midiClockBurstMode",midiClockBurstMode);
    };

    // Should MIDI start and stop also be sent with the clock - persistent user preference
    public boolean getMidiClockStartStop() {
        return midiClockStartStop;
    }
    public void setMidiClockStartStop(boolean midiClockStartStop) {
        this.midiClockStartStop = midiClockStartStop;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("midiClockStartStop",midiClockStartStop);
    };

    // Is the MIDI clock running?
    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
    public boolean getIsRunning() {
        return isRunning;
    }

}

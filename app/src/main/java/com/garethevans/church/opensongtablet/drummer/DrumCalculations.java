package com.garethevans.church.opensongtablet.drummer;

import android.util.Log;

import com.garethevans.church.opensongtablet.songprocessing.Song;

public class DrumCalculations {

    // This class will deal with calculations around tempo and time signatures
    // The values can be used for the metronome or the drummer

    private final static String TAG = "DrumCalculations";

    /**
     * Calculates time signature as an integer array based on the string in the song object
     * @param timeSig The value retrieved from song.getTimeSig().  Can be empty or null
     * @return returns the time signature as an array e.g. 4/4 -> {4,4}
     */
    public static int[] getFixedTimeSignature(String timeSig) {
        // The default values of 4/4 if not set, or if an error
        int numerator = 4;
        int denominator = 4;

        if (timeSig != null && timeSig.contains("/")) {
            // Do the conversion based on what we have
            // Split by "/"
            String[] parts = timeSig.split("/");
            if (parts.length == 2) {
                try {
                    numerator = Integer.parseInt(parts[0]);
                    denominator = Integer.parseInt(parts[1]);
                } catch (Exception e) {
                    Log.d(TAG,"Error parsing the timeSig:"+timeSig);
                }
            }
        }
        return new int[]{numerator, denominator};
    }

    /**
     * Return a fixed version of the time signature string for saving, etc.
     * @param timeSig The value retrieved from song.getTimeSig().  Can be empty or null
     * @param useDefault If there is a null or empty value, should we return 100, or -1
     * @return the fixed time signature as an string e.g. "4/4"
     */
    public static String getFixedTimeSignatureString(String timeSig, boolean useDefault) {
        if ((timeSig == null || timeSig.isEmpty())) {
            return useDefault ? "4/4" : "";
        } else {
            int[] bits = getFixedTimeSignature(timeSig);
            return bits[0]+"/"+bits[1];
        }
    }

    /**
     * Calculates bpm as an integer based on the string in the song object.
     * @param tempo The value retrieved from song.getTempo().  Can be empty or null
     * @param useDefault If there is a null or empty value, should we return 100, or -1
     * @return returns the actual tempo as an int e.g. 68, the default of 100 or -1 if not set
     */
    public static int getFixedTempo(String tempo, boolean useDefault) {
        // If we are happy to use the default, set it to 100
        // If not, use a -1 value
        int bpmInt;

        // If the value is null, make it empty for string comparisons
        if (tempo == null) {
            tempo = "";
        }

        // Firstly, OpenSong (desktop) can use descriptions that we need to change
        // Fix desktop text tempo values and make sure we only have numbers
        tempo = tempo.replace("Very Fast", "140").
                replace("Fast", "120").
                replace("Moderate", "100").
                replace("Slow", "80").
                replace("Very Slow", "60").
                replaceAll("\\D", "");

        // Convert the string to an integer
        if (!tempo.isEmpty()) {
            bpmInt = Integer.parseInt(tempo);
        } else if (useDefault) {
            bpmInt = 100;
        } else {
            bpmInt = -1;
        }
        return bpmInt;
    }

    // Return a fixed version of the tempo string for saving
    public static String getFixedTempoString(String tempo, boolean useDefault) {
        if ((tempo == null || tempo.isEmpty())) {
            return useDefault ? "120" : "";
        } else {
            int bpm = getFixedTempo(tempo, useDefault);
            return Integer.toString(bpm);
        }
    }

    /**
     * Calculates pulsesPerStep for the MidiClockEngine.
     * @param denominator The bottom number of the time signature (4, 8, etc.)
     * @return 6 for simple time (16th notes), 8 for compound time (triplets)
     */
    public static int getTotalStepsInBar(int numerator, int denominator) {
        // Multiply the number of beats by the steps assigned to each beat
        // This ensures 3/4 = 12 steps, 4/4 = 16 steps, 6/8 = 12 steps, etc.
        return numerator * getStepsPerPulse(denominator);
    }

    /**
     * Calculates total sequencer steps in one full bar.
     * @param denominator The bottom number (division)
     * @return e.g., 4/4 returns 16 steps, 6/8 returns 6 steps (or 12 for high res)
     */
    public static int getPulsesPerStep(int denominator) {
        if (denominator == 8) {
            // 24 PPQN base.
            // 135 BPM = 24 pulses.
            // 8th note in 6/8 is 0.5 of a beat = 12 pulses.
            // We have 2 sequencer steps per 8th note, so 6 pulses per step.
            return 6;
        } else {
            // 4/4: 24 pulses / 4 steps = 6 pulses per step.
            return 6;
        }
    }

    /**
     * Determines how many steps are in a single beat (pulse).
     * @param denominator The bottom of the time signature (4 or 8)
     * @return 4 for simple time (16th notes), 1 for compound time
     */
    public static int getStepsPerPulse(int denominator) {
        if (denominator == 8) {
            return 2; // Clicks every 2 steps (8th notes)
        } else {
            return 4; // Clicks every 4 steps (quarter notes)
        }
    }

    /**
     * Determines if the tempo and time signature are set and valid
     * @param thisSong The song to check the tempo and time signature from
     * @param useMetronomeDefaults if we should use 4/4 for a null or empty time signature
     * @return true if the tempo and time signature are valid
     */
     public static boolean isTempoTimeSigValid(Song thisSong, boolean useMetronomeDefaults) {
        int bpm = getFixedTempo(thisSong.getTempo(), useMetronomeDefaults);
        int[] timeSig = getFixedTimeSignature(thisSong.getTimesig());
        if ((timeSig[0] == -1 || timeSig[1] == -1) && useMetronomeDefaults) {
            timeSig[0] = 4;
            timeSig[1] = 4;
        }
        return bpm>=40 && bpm<=300 && timeSig[0]>0 && timeSig[1]>0;
    }

    /**
     * Determines the time between beats/flashes for the drummer / metronome
     * @param bpm The song tempo
     * @param denominator The division of the time signature (bottom number)
     * @return long the time in ms for each beat
     */
    public static long getBeatDurationMs(int bpm, int denominator) {
        // 60,000 / BPM = the duration of a standard pulse
        double msPerPulse = 60000.0 / bpm;

        if (denominator == 8) {
            // To match GarageBand: A 6/8 bar at 135 BPM should take
            // the same time as 3 quarter notes at 135 BPM.
            // There are 6 clicks in that time, so each click is 0.5 of a beat.
            return Math.round(msPerPulse * 0.5);
        } else {
            // 4/4: One click per BPM pulse
            return Math.round(msPerPulse);
        }
    }

}

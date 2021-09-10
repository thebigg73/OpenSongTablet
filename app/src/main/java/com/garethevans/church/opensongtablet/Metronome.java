package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.graphics.Color;
import androidx.core.graphics.ColorUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

class Metronome {

    private static Executor METRONOME_THREAD_POOL_EXECUTOR = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    private double bpm;
	private short beat, noteValue;
	private int duration;
	private float metrovol;

	private double beatSound, sound;
    private boolean play = true;

	private final AudioGenerator audioGenerator = new AudioGenerator(8000);
    private double[] soundTickArray, soundTockArray, soundSilenceArray;
	private int currentBeat = 1;
	private int runningBeatCount;
	static private int maxBeatCount;

    // Variables for metronome to work
    // Keeping them as public/static to allow them to be accessed without the dialogfragment
    static MetronomeAsyncTask metroTask;
    static VisualMetronomeAsyncTask visualMetronome;

	private Metronome(String pan, float vol) {
		audioGenerator.createPlayer(pan,vol);
	}

	private void calcSilence() {

        //TEST
        beat = FullscreenActivity.beats;
        noteValue = FullscreenActivity.noteValue;
        bpm = PopUpMetronomeFragment.bpm;

        if (noteValue<1) {
            noteValue=4;
        }
        if (bpm<1) {
            bpm=120;
        }

        // 3/4 3 beats per bar, each quarter notes
        // 3/8 3 beats per bar, each eigth notes
        // 6/8 2 beats per bar. each dotted quarter note
        // bpm is taken as being given for the beat of the time signature

        // Time signatures that I have
        // 2/2   2/4   3/2    3/4   3/8   4/4   5/4   5/8   6/4   6/8   7/4   7/8   1/4

        // IV  - Override for 6/8 compound time signature - 3 sounds (triplet) per beat
        if (beat == 6 && noteValue == 8) {
            bpm = bpm * 3;
        }

        int tick1 = 600;
        double[] tick;
        double[] tock;
		if (StaticVariables.mTimeSig.equals("1/4")) {
            tick = audioGenerator.getSineWave(tick1, 8000, beatSound);
            tock = audioGenerator.getSineWave(tick1, 8000, beatSound);
        } else {
            tick = audioGenerator.getSineWave(tick1, 8000, beatSound);
            tock = audioGenerator.getSineWave(tick1, 8000, sound);
        }
        // IV - Build double interval sound arrays of silence - overwrite start with tick/tock
        // IV - Make intentionally slightly short
        duration = (int) ((((60/bpm)*(8000)) * 2) - 100);
        soundTickArray = new double[duration];
        soundTockArray = new double[duration];
		for(int i = 0; i< tick1; i++) {
			soundTickArray[i] = tick[i];
			soundTockArray[i] = tock[i];
		}
	}

	private void play(String pan, float vol) {
	    calcSilence();
        // IV - We align sounds to beat using the clock.  Calculate based on a 6th of a beat and scale up.
        long time_in_millisecs = (long) (((60.0f / (float) PopUpMetronomeFragment.bpm) * 1000)) / 6;
        // IV  - Override for 6/8 compound time signature to give 3 sounds (triplet) per beat otherwise one sound per beat
        if (FullscreenActivity.beats == 6 && FullscreenActivity.noteValue == 8) {
            time_in_millisecs = time_in_millisecs * 2;
        } else {
            time_in_millisecs = time_in_millisecs * 6;
        }

		// IV - We have a short first beat to compensate for loop start delay
        long nexttime = System.currentTimeMillis() - 200;
		do {
		    try {
                // IV - Sound jitter means sound periods are not exact
                // IV - An adjustment is made to align the next sound start with the beat
                if ((nexttime - System.currentTimeMillis()) > 0) {
                    soundSilenceArray = new double[(int) ((nexttime - System.currentTimeMillis()) * 8)];
                    audioGenerator.writeSound(pan, vol, soundSilenceArray);
                }
                if (currentBeat == 1) {
                    audioGenerator.writeSound(pan, vol, soundTockArray);
                } else {
                    audioGenerator.writeSound(pan, vol, soundTickArray);
                }

                currentBeat++;
                runningBeatCount++;
                if (maxBeatCount > 0 && runningBeatCount >= maxBeatCount) { // This is if the user has specified max metronome time
                    play = false;
                    StaticVariables.metronomeonoff = "off";
                    // IV - This variable is a state indicator set in this function only
                    StaticVariables.clickedOnMetronomeStart = false;
                }
                if (currentBeat > beat) {
                    currentBeat = 1;
                }
                nexttime = nexttime + time_in_millisecs;
            } catch (Exception e) {
		        e.printStackTrace();
		        play = false;
            }
		} while(play);
	}
	
	private void stop() {
		play = false;
		audioGenerator.destroyAudioTrack();
	}

	private void setBpm(int bpm) {
		this.bpm = bpm;
	}

	private void setNoteValue(short noteValue) {
		this.noteValue = noteValue;
	}

	private void setBeat(short beat_set) {
		this.beat = beat_set;
	}

	private void setBeatSound(double sound1) {
		this.beatSound = sound1;
	}

	private void setSound(double sound2) {
		this.sound = sound2;
	}

	private void setVolume(float metrovol_set) {
		this.metrovol = metrovol_set;
	}

	public float getVolume () {
		return metrovol;
	}

	private void setCurrentBeat(int currentBeat_set) {
		this.currentBeat = currentBeat_set;
	}

    static int getTempo(String t) {
        t = t.replace("Very Fast", "140");
        t = t.replace("Fast", "120");
        t = t.replace("Moderate", "100");
        t = t.replace("Slow", "80");
        t = t.replace("Very Slow", "60");
        t = t.replaceAll("[\\D]", "");
        try {
            PopUpMetronomeFragment.bpm = (short) Integer.parseInt(t);
        } catch (NumberFormatException nfe) {
            PopUpMetronomeFragment.bpm = 0;
        }

        if (PopUpMetronomeFragment.bpm<40 || PopUpMetronomeFragment.bpm>299) {
            PopUpMetronomeFragment.bpm = 260;  // These are the 'not set' values
            PopUpMetronomeFragment.tempo = 260;
        } else {
            PopUpMetronomeFragment.tempo = PopUpMetronomeFragment.bpm;
        }

        return PopUpMetronomeFragment.bpm;
    }

    static void setBeatValues() {
        short r = 0;
        if (StaticVariables.mTimeSig.startsWith("1/")) {
            r = 4;
        } else if (StaticVariables.mTimeSig.startsWith("2/")) {
            r = 2;
        } else if (StaticVariables.mTimeSig.startsWith("3/")) {
            r = 3;
        } else if (StaticVariables.mTimeSig.startsWith("4/")) {
            r = 4;
        } else if (StaticVariables.mTimeSig.startsWith("5/")) {
            r = 5;
        } else if (StaticVariables.mTimeSig.startsWith("6/")) {
            r = 6;
        } else if (StaticVariables.mTimeSig.startsWith("7/")) {
            r = 7;
        }
        FullscreenActivity.beats = r;
    }

    static void setNoteValues() {
        short r = 0;
        if (StaticVariables.mTimeSig.endsWith("/2")) {
            r = 2;
        } else if (StaticVariables.mTimeSig.endsWith("/4")) {
            r = 4;
        } else if (StaticVariables.mTimeSig.endsWith("/8")) {
            r = 2; //8
        }
        FullscreenActivity.noteValue = r;
    }

    static void startstopMetronome(Context c, boolean showvisual, int metronomeColor, String pan, float vol, int barlength) {
        if (checkMetronomeValid(c) && StaticVariables.metronomeonoff.equals("off")) {
            // Start the metronome
            StaticVariables.metronomeonoff = "on";
            StaticVariables.whichbeat = "b";
            // This is a state indicator set in this function only
            StaticVariables.clickedOnMetronomeStart = true;
            metroTask = new MetronomeAsyncTask(pan,vol,barlength);
            try {
                metroTask.executeOnExecutor(METRONOME_THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                Log.d("d","Error starting the metronome");
            }
            startstopVisualMetronome(showvisual,metronomeColor);
        } else if (StaticVariables.metronomeonoff.equals("on")) {
            // This a state indicator set in this function only
            StaticVariables.clickedOnMetronomeStart = false;
            stopMetronomeTask();
        }
    }

    static void stopMetronomeTask() {
        // Stop the metronome
        StaticVariables.metronomeonoff = "off";
        if (metroTask!=null) {
            metroTask.stop();
        }
    }

    private static boolean checkMetronomeValid(Context c) {
        boolean validTimeSig = false;
        boolean validBPM = true;
        boolean validMetro = false;

        if (getTempo(StaticVariables.mTempo)==260) {
            validBPM = false;
        }

        String[] arrayvals = c.getResources().getStringArray(R.array.timesig);

        for (String arrayval : arrayvals) {
            if (StaticVariables.mTimeSig.equals(arrayval)) {
                validTimeSig = true;
                break;
            }
        }

        if (validBPM && validTimeSig) {
            validMetro = true;
        }

        return validMetro;
    }

    static void startstopVisualMetronome(boolean showvisual, int metronomeColor) {
        visualMetronome = new VisualMetronomeAsyncTask(showvisual, metronomeColor);
        try {
            visualMetronome.executeOnExecutor(METRONOME_THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Error starting visual metronome");
        }
    }
    private static class VisualMetronomeAsyncTask extends AsyncTask<Void, Integer, String> {

	    VisualMetronomeAsyncTask(boolean showvis, int metronomeColor) {
	        this.metronomeColor = metronomeColor;
	        this.showvisual = showvis;
        }

        // IV - Visual is an on & off for each each beat so use half a beat
        // IV - We align sounds to beat using the clock.  Calculate based on a 6th of a beat and scale up.
        long sixth_time_in_millisecs = (long) (((60.0f / (float) PopUpMetronomeFragment.bpm) * 1000)) / 6 ;
        long time_in_millisecs = sixth_time_in_millisecs * 3;

        long oldtime = System.currentTimeMillis();
        long nexttime = oldtime + time_in_millisecs;
        final boolean showvisual;
        final int metronomeColor;

        @Override
        protected String doInBackground(Void... voids) {
            int beats = FullscreenActivity.beats * 2;
            // IV - Override for 6/8 compound time signature
            if ((beats == 12) && (FullscreenActivity.noteValue == 8)) {
                beats = 4;
            }
            int beatsCount = 1;
            int metronomeColorDarker = ColorUtils.blendARGB(metronomeColor, Color.BLACK, 0.3f);
            publishProgress(metronomeColor);
            while (StaticVariables.metronomeonoff.equals("on")) {
                // Post this activity based on the bpm
                if (System.currentTimeMillis() >= nexttime) {
                    oldtime = nexttime;
                    nexttime = oldtime + time_in_millisecs;
                    beatsCount = beatsCount + 1;
                    if (beatsCount > beats) {
                        beatsCount = 1;
                        publishProgress(metronomeColor);
                    } else {
                        publishProgress(metronomeColorDarker);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... integers) {
            if (showvisual) {
                if (StaticVariables.whichbeat.equals("a")) {
                    StaticVariables.whichbeat = "b";
                    if (StageMode.ab != null) {
                        StageMode.ab.setBackgroundDrawable(new ColorDrawable(0xff232333));
                    }
                } else {
                    StaticVariables.whichbeat = "a";
                    if (StageMode.ab != null) {
                        StageMode.ab.setBackgroundDrawable(new ColorDrawable(integers[0]));
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (StageMode.ab != null) {
                StageMode.ab.setBackgroundDrawable(new ColorDrawable(0xff232333));
            }
        }
    }

    static class MetronomeAsyncTask extends AsyncTask<Void, Void, String> {

        Metronome metronome;
        final String pan;
        final float vol;
        int barsrequired;

        @Override
        protected void onPreExecute() {
            // Figure out how many beats we should use
            maxBeatCount = FullscreenActivity.beats * barsrequired;
        }

        MetronomeAsyncTask(String pan, float vol, int barlength) {
            metronome = new Metronome(pan,vol);
            this.pan = pan;
            this.vol = vol;
            barsrequired = barlength;
        }

        void setNoteValue(short noteVal) {
            if (metronome != null && PopUpMetronomeFragment.bpm >= FullscreenActivity.minBpm &&
                    PopUpMetronomeFragment.bpm <= FullscreenActivity.maxBpm && noteVal > 0) {
                metronome.setNoteValue(noteVal);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setBeat(short beat) {
            if (metronome != null) {
                metronome.setBeat(beat);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setBpm(short bpm) {
            if (metronome != null && bpm >= FullscreenActivity.minBpm && bpm <= FullscreenActivity.maxBpm && FullscreenActivity.noteValue > 0) {
                metronome.setBpm(bpm);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setCurrentBeat(int currentBeat) {
            if (metronome != null) {
                metronome.setCurrentBeat(currentBeat);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setBeatSound(double beatSound) {
            if (metronome != null) {
                metronome.setBeatSound(beatSound);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setSound(double sound) {
            if (metronome != null) {
                metronome.setSound(sound);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setVolume(float metrovol) {
            if (metronome != null)
                metronome.setVolume(metrovol);
        }

        @Override
        protected String doInBackground(Void... voids) {
            setBeat(FullscreenActivity.beats);
            setNoteValue(FullscreenActivity.noteValue);
            setBpm(PopUpMetronomeFragment.bpm);
            setBeatSound(FullscreenActivity.beatSound);
            setSound(FullscreenActivity.sound);
            setVolume(vol);
            setCurrentBeat(1);
            play(pan, vol);
            return null;
        }

        public void stop() {
            if (metronome != null) {
                metronome.stop();
                metronome = null;
            }
        }

        void play(String pan, float vol) {
            if (metronome != null) {
                metronome.play(pan,vol);
                metronome = null;
            }
        }

        @Override
        protected void onCancelled() {
            stop();
        }
    }

    static boolean isMetronomeValid() {
        int t;
        try {
            t = Integer.parseInt(StaticVariables.mTempo.replaceAll("[\\D]", ""));
        } catch (NumberFormatException nfe) {
            t = 39;
        }
        ProcessSong.processTimeSig();
        return t >= 40 && t <= 299 && t != 260 && StaticVariables.mTimeSigValid;

    }
}
package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;

class Metronome {
	
	private double bpm;
	private short beat, noteValue;
	private int silence;
	private float metrovol;

	private double beatSound, sound;
    private boolean play = true;
	
	private final AudioGenerator audioGenerator = new AudioGenerator(8000);
    private double[] soundTickArray, soundTockArray, silenceSoundArray;
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
        beat = getBeat();
        noteValue = getNoteValue();
        bpm = getBpm();

        if (noteValue<1) {
            noteValue=4;
        }
        if (bpm<1) {
            bpm=120;
        }

        // 3/4 3 beats per bar, each quarter notes
        // 3/8 3 beats per bar, each eigth notes = tempo is twice as fast

        // Tempos that I have
        // 2/2   2/4   3/2    3/4   3/8   4/4   5/4   5/8   6/4   6/8   7/4   7/8   1/4
        // First number is the number of beats and is the easiest bit
        // The second number is the length of each beat
        // 2 = half notes, 4 = quarter notes, 8 = eigth notes

        if (beat==6 || beat==9) {
            noteValue = (short)(noteValue/(beat/3));
        } else if (beat==5 || beat==7) {
            noteValue = (short)(noteValue/2);
        }

        int resolutionmeter = (int) (8.0f / (float) noteValue);

        if (resolutionmeter ==0) {
            resolutionmeter =1;
        }

        int tick1 = 600;
        try {
            silence = (int) (((60/bpm)*(4000* resolutionmeter)- tick1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        soundTickArray = new double[tick1];
		soundTockArray = new double[tick1];
		if (silence>10000) {
            silence = 10000;
        }
		silenceSoundArray = new double[this.silence];
        double[] tick;
        double[] tock;
		if (StaticVariables.mTimeSig.equals("1/4")) {
            tick = audioGenerator.getSineWave(tick1, 8000/ resolutionmeter, beatSound/ resolutionmeter);
            tock = audioGenerator.getSineWave(tick1, 8000/ resolutionmeter, beatSound/ resolutionmeter);

        } else {
            tick = audioGenerator.getSineWave(tick1, 8000/ resolutionmeter, beatSound/ resolutionmeter);
            tock = audioGenerator.getSineWave(tick1, 8000/ resolutionmeter, sound/ resolutionmeter);
        }
		for(int i = 0; i< tick1; i++) {
			soundTickArray[i] = tick[i];
			soundTockArray[i] = tock[i];
		}
		for(int i=0;i<silence;i++)
			silenceSoundArray[i] = 0;
	}
	
	private void play(String pan, float vol) {
		calcSilence();
		do {
			if(currentBeat == 1) {
				audioGenerator.writeSound(pan,vol,soundTockArray);
			} else {
				audioGenerator.writeSound(pan,vol,soundTickArray);
			}
			audioGenerator.writeSound(pan,vol,silenceSoundArray);

			currentBeat++;
			runningBeatCount++;
			if (maxBeatCount>0 && runningBeatCount>=maxBeatCount) { // This is if the user has specified max metronome time
			    play=false;
                StaticVariables.metronomeonoff = "off";
            }
			if(currentBeat > beat)
				currentBeat = 1;
		} while(play);
	}
	
	private void stop() {
		play = false;
		audioGenerator.destroyAudioTrack();
	}

	private double getBpm() {
		return PopUpMetronomeFragment.bpm;
	}

	private void setBpm(int bpm) {
		this.bpm = bpm;
	}

	private short getNoteValue() {
		return FullscreenActivity.noteValue;
	}

	private void setNoteValue(short noteValue) {
		this.noteValue = noteValue;
	}

	private short getBeat() {
		return FullscreenActivity.beats;
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

        return (int) PopUpMetronomeFragment.bpm;
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

    static void startstopMetronome(Activity activity, Context c, boolean showvisual, int metronomeColor, String pan, float vol, int barlength) {
        if (checkMetronomeValid(c) && StaticVariables.metronomeonoff.equals("off")) {
            // Start the metronome
            StaticVariables.metronomeonoff = "on";
            StaticVariables.whichbeat = "b";
            metroTask = new MetronomeAsyncTask(pan,vol,barlength);
            try {
                metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                Log.d("d","Error starting the metronome");
            }
            startstopVisualMetronome(showvisual,metronomeColor);

        } else if (checkMetronomeValid(c) && StaticVariables.metronomeonoff.equals("on")) {
            // Stop the metronome
            StaticVariables.metronomeonoff = "off";
            if (metroTask!=null) {
                metroTask.stop();
            }

        } else {
            // Not valid, so open the popup
            StaticVariables.whattodo = "page_metronome";
            if (PopUpMetronomeFragment.mListener!=null) {
                PopUpMetronomeFragment.mListener.openFragment();
            } else {


                
                PopUpMetronomeFragment.MyInterface mListener;
                mListener = (PopUpMetronomeFragment.MyInterface) activity;
                mListener.openFragment();
            }
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
            visualMetronome.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Error starting visual metronome");
        }
    }
    private static class VisualMetronomeAsyncTask extends AsyncTask<Void, Integer, String> {

	    VisualMetronomeAsyncTask(boolean showvis, int metronomeColor) {
	        this.metronomeColor = metronomeColor;
	        this.showvisual = showvis;
        }

        final int beatmultiplier = FullscreenActivity.noteValue;
        final long time_in_millisecs = (long) (((60.0f / (float) PopUpMetronomeFragment.bpm) * (4.0f / (float) beatmultiplier))* 1000);
        long oldtime = System.currentTimeMillis();
        long nexttime = oldtime + time_in_millisecs;
        final boolean showvisual;

        final int metronomeColor;

        @Override
        protected String doInBackground(Void... voids) {
            publishProgress(1);
            while (StaticVariables.metronomeonoff.equals("on")) {
                // Post this activity based on the bpm
                if (System.currentTimeMillis() >= nexttime) {
                    oldtime = nexttime;
                    nexttime = oldtime + time_in_millisecs;
                    publishProgress(1);
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
                        StageMode.ab.setBackgroundDrawable(new ColorDrawable(metronomeColor));
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
        return t >= 40 && t < 299 && StaticVariables.mTimeSigValid;

    }
}
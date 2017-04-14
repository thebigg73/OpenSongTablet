package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;

class Metronome {
	
	private double bpm;
	private short beat;
	private short noteValue;
	private int silence;
	private float metrovol;

	private double beatSound;
	private double sound;
    private boolean play = true;
	
	private AudioGenerator audioGenerator = new AudioGenerator(8000);
    private double[] soundTickArray;
	private double[] soundTockArray;
	private double[] silenceSoundArray;
	private int currentBeat = 1;

    // Variables for metronome to work
    // Keeping them as public/static to allow them to be accessed without the dialogfragment
    static MetronomeAsyncTask metroTask;
    static VisualMetronomeAsyncTask visualMetronome;

	Metronome() {
		audioGenerator.createPlayer();
	}
	
	void calcSilence() {
		//silence = (int) (((60/bpm)*8000)-tick);

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
        // 2/2   2/4   3/2    3/4   3/8   4/4   5/4   5/8   6/4   6/8   7/4   7/8
        // First number is the number of beats and is the easiest bit
        // The second number is the length of each beat
        // 2 = half notes, 4 = quarter notes, 8 = eigth notes


		//resolutionmeter = (int) ((float)noteValue/4.0f);
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
		//msg = new Message();
		//msg.obj = ""+currentBeat;
		double[] tick = audioGenerator.getSineWave(tick1, 8000/ resolutionmeter, beatSound/ resolutionmeter);
		double[] tock = audioGenerator.getSineWave(tick1, 8000/ resolutionmeter, sound/ resolutionmeter);
		for(int i = 0; i< tick1; i++) {
			soundTickArray[i] = tick[i];
			soundTockArray[i] = tock[i];
		}
		for(int i=0;i<silence;i++)
			silenceSoundArray[i] = 0;
	}
	
	public void play() {
		calcSilence();
		do {
			//msg = new Message();
			//msg.obj = ""+currentBeat;
            //mHandler.postDelayed(null,(int)((float)silence/8000.0f));
            //mHandler.sendMessage(msg);
			if(currentBeat == 1) {
				audioGenerator.writeSound(soundTockArray);
			} else {
				audioGenerator.writeSound(soundTickArray);				
			}
			audioGenerator.writeSound(silenceSoundArray);

			//if (bpm <= 120)
			//	mHandler.sendMessage(msg);
			
			
			//if (bpm > 120)
				//mHandler.sendMessage(msg);
			currentBeat++;
			if(currentBeat > beat)
				currentBeat = 1;
		} while(play);
	}
	
	public void stop() {
		play = false;
		audioGenerator.destroyAudioTrack();
	}

	private double getBpm() {
		return PopUpMetronomeFragment.bpm;
	}

	void setBpm(int bpm) {
		this.bpm = bpm;
	}

	private short getNoteValue() {
		return FullscreenActivity.noteValue;
	}

	void setNoteValue(short noteValue) {
		this.noteValue = noteValue;
	}

	private short getBeat() {
		return FullscreenActivity.beats;
	}
	void setBeat(short beat_set) {
		this.beat = beat_set;
	}

	void setBeatSound(double sound1) {
		this.beatSound = sound1;
	}

	void setSound(double sound2) {
		this.sound = sound2;
	}

	public void setVolume (float metrovol_set) {
		this.metrovol = metrovol_set;
	}
	public float getVolume () {
		return metrovol;
	}

	void setCurrentBeat(int currentBeat_set) {
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

        if (PopUpMetronomeFragment.bpm<40 || PopUpMetronomeFragment.bpm>199) {
            PopUpMetronomeFragment.bpm = 200;
            PopUpMetronomeFragment.tempo = 200;
        } else {
            PopUpMetronomeFragment.tempo = PopUpMetronomeFragment.bpm;
        }

        return (int) PopUpMetronomeFragment.bpm;
    }

    static void setBeatValues() {
        short r = 0;
        if (FullscreenActivity.mTimeSig.startsWith("2/")) {
            r = 2;
        } else if (FullscreenActivity.mTimeSig.startsWith("3/")) {
            r = 3;
        } else if (FullscreenActivity.mTimeSig.startsWith("4/")) {
            r = 4;
        } else if (FullscreenActivity.mTimeSig.startsWith("5/")) {
            r = 5;
        } else if (FullscreenActivity.mTimeSig.startsWith("6/")) {
            r = 6;
        } else if (FullscreenActivity.mTimeSig.startsWith("7/")) {
            r = 7;
        }
        FullscreenActivity.beats = r;
    }

    static void setNoteValues() {
        short r = 0;
        if (FullscreenActivity.mTimeSig.endsWith("/2")) {
            r = 2;
        } else if (FullscreenActivity.mTimeSig.endsWith("/4")) {
            r = 4;
        } else if (FullscreenActivity.mTimeSig.endsWith("/8")) {
            r = 8;
        }
        FullscreenActivity.noteValue = r;
    }

    static void startstopMetronome(Activity activity) {
        if (checkMetronomeValid() && FullscreenActivity.metronomeonoff.equals("off")) {
            // Start the metronome
            Runtime.getRuntime().gc();
            FullscreenActivity.metronomeonoff = "on";
            FullscreenActivity.whichbeat = "b";
            metroTask = new MetronomeAsyncTask();
            try {
                metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                Log.d("d","Error starting the metronome");
            }
            startstopVisualMetronome();

        } else if (checkMetronomeValid() && FullscreenActivity.metronomeonoff.equals("on")) {
            // Stop the metronome
            Runtime.getRuntime().gc();
            FullscreenActivity.metronomeonoff = "off";
            if (metroTask!=null) {
                metroTask.stop();
            }

        } else {
            // Not valid, so open the popup
            FullscreenActivity.whattodo = "page_metronome";
            if (PopUpMetronomeFragment.mListener!=null) {
                PopUpMetronomeFragment.mListener.openFragment();
            } else {
                PopUpMetronomeFragment.MyInterface mListener;
                mListener = (PopUpMetronomeFragment.MyInterface) activity;
                mListener.openFragment();
            }
        }
    }

    private static boolean checkMetronomeValid() {
        boolean validTimeSig = false;
        boolean validBPM = true;
        boolean validMetro = false;

        if (getTempo(FullscreenActivity.mTempo)==160) {
            validBPM = false;
        }

        for (int i=0; i<FullscreenActivity.timesigs.length-1; i++) {
            if (FullscreenActivity.mTimeSig.equals(FullscreenActivity.timesigs[i])) {
                validTimeSig = true;
            }
        }

        if (validBPM && validTimeSig) {
            validMetro = true;
        }

        return validMetro;
    }

    static void startstopVisualMetronome() {
        visualMetronome = new VisualMetronomeAsyncTask();
        try {
            visualMetronome.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Error starting visual metronome");
        }
    }
    private static class VisualMetronomeAsyncTask extends AsyncTask<Void, Integer, String> {

        boolean on = false;
        int beatmultiplier = FullscreenActivity.noteValue;
        long time_in_millisecs = (long) (((60.0f / (float) PopUpMetronomeFragment.bpm) * (4.0f / (float) beatmultiplier))* 1000);
        long oldtime = System.currentTimeMillis();
        long nexttime = oldtime + time_in_millisecs;


        @Override
        protected String doInBackground(Void... voids) {
            publishProgress(1);
            while (FullscreenActivity.metronomeonoff.equals("on")) {
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
            if (FullscreenActivity.visualmetronome) {
                if (FullscreenActivity.whichbeat.equals("a")) {
                    FullscreenActivity.whichbeat = "b";
                    if (StageMode.ab != null) {
                        StageMode.ab.setBackgroundDrawable(new ColorDrawable(FullscreenActivity.beatoffcolour));
                    }
                } else {
                    FullscreenActivity.whichbeat = "a";
                    if (StageMode.ab != null) {
                        StageMode.ab.setBackgroundDrawable(new ColorDrawable(FullscreenActivity.metronomeColor));
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (StageMode.ab != null) {
                StageMode.ab.setBackgroundDrawable(new ColorDrawable(FullscreenActivity.beatoffcolour));
            }
        }
    }

    static class MetronomeAsyncTask extends AsyncTask<Void, Void, String> {

        Metronome metronome;

        MetronomeAsyncTask() {
            metronome = new Metronome();
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
            setVolume(FullscreenActivity.metrovol);
            setCurrentBeat(FullscreenActivity.currentBeat);
            play();
            return null;
        }

        public void stop() {
            if (metronome != null) {
                metronome.stop();
                metronome = null;
            }
        }

        public void play() {
            if (metronome != null) {
                metronome.play();
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
            t = Integer.parseInt(FullscreenActivity.mTempo.replaceAll("[\\D]", ""));
        } catch (NumberFormatException nfe) {
            t = 39;
        }
        ProcessSong.processTimeSig();
        if (t>=40 && t<199 && FullscreenActivity.mTimeSigValid) {
            return true;
        } else {
            return false;
        }

    }
}
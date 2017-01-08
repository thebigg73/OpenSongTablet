package com.garethevans.church.opensongtablet;

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
	//private Message msg;
	private int currentBeat = 1;
	//private int currentBeatnum = 1;
	
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

/*
	public double getBeatSound() {
		return beatSound;
	}
*/
	void setBeatSound(double sound1) {
		this.beatSound = sound1;
	}

/*
	public double getSound() {
		return sound;
	}
*/
	void setSound(double sound2) {
		this.sound = sound2;
	}

	public void setVolume (float metrovol_set) {
		this.metrovol = metrovol_set;
	}
	public float getVolume () {
		return metrovol;
	}

/*
	public int getCurrentBeat () {
		return currentBeat;
	}
*/
	void setCurrentBeat(int currentBeat_set) {
		this.currentBeat = currentBeat_set;
	}

}
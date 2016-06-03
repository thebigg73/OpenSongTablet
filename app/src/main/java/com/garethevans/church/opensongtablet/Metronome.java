package com.garethevans.church.opensongtablet;

import android.os.Handler;
import android.os.Message;

public class Metronome {
	
	private double bpm;
	private short beat;
	private short noteValue;
	private int silence;
	private float metrovol;

	private double beatSound;
	private double sound;
	private final int tick = 600; // samples of tick
	private int resolutionmeter;
	private boolean play = true;
	
	private AudioGenerator audioGenerator = new AudioGenerator(8000);
	private Handler mHandler;
	private double[] soundTickArray;
	private double[] soundTockArray;
	private double[] silenceSoundArray;
	private Message msg;
	private int currentBeat = 1;
	//private int currentBeatnum = 1;
	
	public Metronome(Handler handler) {
		audioGenerator.createPlayer();
		this.mHandler = handler;
	}
	
	public void calcSilence() {
		resolutionmeter = (int) ((float)noteValue/4.0f);
		//silence = (int) (((60/bpm)*8000)-tick);
        if (noteValue==0) {
            noteValue=4;
        }
        if (resolutionmeter==0) {
            resolutionmeter=1;
        }
		silence = (int) (((60/bpm)*(8000/resolutionmeter)-tick));
		soundTickArray = new double[this.tick];	
		soundTockArray = new double[this.tick];
		if (silence>10000) {
            silence = 10000;
        }
		silenceSoundArray = new double[this.silence];
		msg = new Message();
		msg.obj = ""+currentBeat;
		double[] tick = audioGenerator.getSineWave(this.tick, 8000/resolutionmeter, beatSound/resolutionmeter);		
		//double[] tick = audioGenerator.getSineWave(this.tick, resolutionmeter, beatSound);		
		double[] tock = audioGenerator.getSineWave(this.tick, 8000/resolutionmeter, sound/resolutionmeter);
		//double[] tock = audioGenerator.getSineWave(this.tick, resolutionmeter, sound);
		for(int i=0;i<this.tick;i++) {
			soundTickArray[i] = tick[i];
			soundTockArray[i] = tock[i];
		}
		for(int i=0;i<silence;i++)
			silenceSoundArray[i] = 0;
	}
	
	public void play() {
		calcSilence();
		do {
			msg = new Message();
			msg.obj = ""+currentBeat;
			mHandler.sendMessage(msg);
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

	public double getBpm() {
		return bpm;
	}

	public void setBpm(int bpm) {
		this.bpm = bpm;
	}

	public short getNoteValue() {
		return noteValue;
	}

	public void setNoteValue(short noteValue) {
		this.noteValue = noteValue;
	}

	public short getBeat() {
		return beat;
	}
	public void setBeat(short beat_set) {
		this.beat = beat_set;
	}

	public double getBeatSound() {
		return beatSound;
	}
	public void setBeatSound(double sound1) {
		this.beatSound = sound1;
	}

	public double getSound() {
		return sound;
	}
	public void setSound(double sound2) {
		this.sound = sound2;
	}

	public void setVolume (float metrovol_set) {
		this.metrovol = metrovol_set;
	}
	public float getVolume () {
		return metrovol;
	}

	public int getCurrentBeat () {
		return currentBeat;
	}
	public void setCurrentBeat (int currentBeat_set) {
		this.currentBeat = currentBeat_set;
	}

}
package com.garethevans.church.opensongtablet;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

class AudioGenerator {

	private final int sampleRate;
	private AudioTrack audioTrack;

	AudioGenerator(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	double[] getSineWave(int samples, int sampleRate, double frequencyOfTone) {
		double[] sample = new double[samples];
		for (int i = 0; i < samples; i++) {
			sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / frequencyOfTone));
		}
		return sample;
	}

	private byte[] get16BitPcm(double[] samples) {
		byte[] generatedSound = new byte[2 * samples.length];
		int index = 0;
		for (double sample : samples) {
			// scale to maximum amplitude
			short maxSample = (short) ((sample * Short.MAX_VALUE));
			// in 16 bit wav PCM, first byte is the low order byte
			generatedSound[index++] = (byte) (maxSample & 0x00ff);
			generatedSound[index++] = (byte) ((maxSample & 0xff00) >>> 8);
		}
		return generatedSound;
	}

	void createPlayer(String pan, float vol) {
		try {

			audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
					sampleRate, AudioFormat.CHANNEL_OUT_STEREO,
					AudioFormat.ENCODING_PCM_16BIT, sampleRate,
					AudioTrack.MODE_STREAM);

		} catch (Exception e) {
			Log.d("audiotrack", "Can't initialise");
		}

		float leftVolume = vol;
		float rightVolume = vol;

		if (pan.equals("L")) {
			rightVolume = 0.0f;
		} else if (pan.equals("R")) {
			leftVolume = 0.0f;
		}

		try {
			audioTrack.setStereoVolume(leftVolume, rightVolume);
			audioTrack.play();
		} catch (Exception e) {
			Log.d("audioTrack", "Can't play it");
		}
	}

	void writeSound(String pan, float vol, double[] samples) {
		byte[] generatedSnd = get16BitPcm(samples);
		if (StaticVariables.metronomeonoff.equals("on") &&
				audioTrack.getState() == AudioTrack.STATE_INITIALIZED &&
				audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
			try {
				audioTrack.write(generatedSnd, 0, generatedSnd.length);
			} catch (Exception e) {
				// This will catch any exception, because they are all descended from Exception
				Log.d("whoops", "error writing sound");
			}
			switch (pan) {
				case "L":
					try {
						audioTrack.setStereoVolume(vol, 0.0f);
					} catch (Exception e) {
						// This will catch any exception, because they are all descended from Exception
						Log.d("whoops", "error setting volume left");
					}
					break;
				case "R":
					try {
						audioTrack.setStereoVolume(0.0f, vol);
					} catch (Exception e) {
						// This will catch any exception, because they are all descended from Exception
						Log.d("whoops", "error setting volume right");
					}
					break;
				default:
					try {
						audioTrack.setStereoVolume(vol, vol);

					} catch (Exception e) {
						// This will catch any exception, because they are all descended from Exception
						Log.d("whoops", "error setting volume both");
					}
					break;
			}
		}
	}

	void destroyAudioTrack() {
		if (audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
			audioTrack.stop();
			audioTrack.release();
		}
	}

}
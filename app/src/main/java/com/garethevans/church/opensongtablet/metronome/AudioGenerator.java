package com.garethevans.church.opensongtablet.metronome;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioGenerator {

    private int sampleRate;
    private AudioTrack audioTrack;

    AudioGenerator(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    double[] getSineWave(int samples, int sampleRate, double frequencyOfTone) {
        double[] sample = new double[samples];
        for (int i = 0; i < samples; i++) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/frequencyOfTone));
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

    public void createPlayer(float volumeLeft, float volumeRight){
        try {
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRate, AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT, sampleRate,
                    AudioTrack.MODE_STREAM);
            audioTrack.setStereoVolume(volumeLeft, volumeRight);
            audioTrack.play();
        } catch (Exception e) {
            Log.d("audiotrack","Can't initialise");
        }
    }

    void writeSound(float volumeLeft, float volumeRight, double[] samples) {
        audioTrack.setStereoVolume(volumeLeft, volumeRight);

        byte[] generatedSnd = get16BitPcm(samples);
        if (audioTrack.getState()==AudioTrack.STATE_INITIALIZED &&
                audioTrack.getPlayState()==AudioTrack.PLAYSTATE_PLAYING) {
            try {
                audioTrack.write(generatedSnd, 0, generatedSnd.length);
            } catch (Exception e) {
                // This will catch any exception, because they are all descended from Exception
                Log.d("whoops","error writing sound");
            }
        }
    }

    void destroyAudioTrack() {
        if (audioTrack!=null && audioTrack.getPlayState()==AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop();
            audioTrack.release();
        }
    }

}
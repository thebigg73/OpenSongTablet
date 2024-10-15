package com.garethevans.church.opensongtablet.utilities;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class ToneGenerator {

    // Create and play sine wave tones

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "ToneGenerator";
    @SuppressWarnings("FieldCanBeLocal")
    private final int volume = 10000;
    private final int bufferSize;
    private final int sampleRate;
    private boolean isPlaying = false;
    private AudioTrack audioTrack;

    public ToneGenerator(Context c) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioManager audioManager = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
            sampleRate = Integer.parseInt(audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            AudioFormat audioFormat = new AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build();

            bufferSize = AudioTrack.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

            audioTrack = new AudioTrack(audioAttributes,
                    audioFormat,
                    bufferSize * 2,
                    AudioTrack.MODE_STREAM,
                    0);

        } else {
            sampleRate = 44100;
            bufferSize = AudioTrack.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                    AudioTrack.MODE_STREAM);
        }
    }

    public void startTone(double chosenFrequency) {
        Log.d(TAG, "chosenFrequency:" + chosenFrequency);
        isPlaying = true;

        short[] samples = new short[bufferSize];
        double twopi = 4.0 * Math.atan(1.0);
        double ph = 0.0;

        // Start audio and then we will write to the audio track
        if (audioTrack != null && audioTrack.getState() != AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.play();
        }

        // synthesis loop
        while (isPlaying) {
            for (int i = 0; i < bufferSize; i++) {
                samples[i] = (short) (volume * (float) Math.sin(ph));
                ph += twopi * chosenFrequency / (float) sampleRate;
            }
            if (audioTrack != null) {
                audioTrack.write(samples, 0, bufferSize);
            }
        }
    }

    public void stopTone() {
        isPlaying = false;
        if (audioTrack != null && audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING) {
            try {
                audioTrack.pause();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void nullAudioTrack() {
        if (audioTrack != null && audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
            try {
                audioTrack.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (audioTrack != null && audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
            try {
                audioTrack.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        audioTrack = null;
    }
}

package com.garethevans.church.opensongtablet.chorddetector;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.projection.MediaProjection;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.util.fft.FFT;

public class PolyphonicChordDetector {

    private static final String TAG = "PolyphonicChordDetector";

    private AudioDispatcher chordDispatcher;
    private Thread chordThread;
    private OnChordDetectedListener listener;

    private final String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    private String lastReportedChord = "-";
    private String candidateChord = "-";
    private int consecutiveFrames = 0;
    private static final int REQUIRED_CONSECUTIVE_FRAMES = 3;

    private int silentOrUnrecognizedFrames = 0;
    private static final int FRAMES_TO_RESET = 8;

    public interface OnChordDetectedListener {
        void onChordDetected(String chordName);
    }

    public void setOnChordDetectedListener(OnChordDetectedListener listener) {
        this.listener = listener;
    }

    // 1. Default Microphone Start
    public void startListening() {
        startListeningInternal(null);
    }

    // 2. Android 10+ Internal Device Audio Start (`AudioPlaybackCapture`)
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void startListening(MediaProjection mediaProjection) {
        startListeningInternal(mediaProjection);
    }

    private void startListeningInternal(MediaProjection mediaProjection) {
        stopListening(); // Clean up any existing thread/dispatcher first

        int sampleRate = 44100;
        int bufferSize = 4096;
        int overlap = 1024;

        Log.d(TAG, "Starting chord detector dispatcher with sampleRate=" + sampleRate + ", bufferSize=" + bufferSize);

        if (mediaProjection != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Log.d(TAG, "Using AudioPlaybackCapture (Internal Device Audio)");
            chordDispatcher = createPlaybackDispatcher(mediaProjection, sampleRate, bufferSize, overlap);
        } else {
            Log.d(TAG, "Using Default Microphone");
            chordDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, overlap);
        }

        if (chordDispatcher == null) {
            Log.e(TAG, "Failed to create AudioDispatcher!");
            return;
        }

        AudioProcessor chordProcessor = createChordProcessor(sampleRate, bufferSize);
        chordDispatcher.addAudioProcessor(chordProcessor);

        chordThread = new Thread(chordDispatcher, "Chord Audio Thread");
        chordThread.start();
        Log.d(TAG, "Chord audio thread successfully started.");
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private AudioDispatcher createPlaybackDispatcher(MediaProjection mediaProjection, int sampleRate, int bufferSize, int overlap) {
        try {
            AudioPlaybackCaptureConfiguration config = new AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                    .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                    .addMatchingUsage(AudioAttributes.USAGE_GAME)
                    .build();

            AudioFormat audioFormat = new AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                    .build();

            AudioRecord audioRecord = new AudioRecord.Builder()
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setAudioPlaybackCaptureConfig(config)
                    .build();

            // 🔑 Wrap AudioRecord into TarsosDSP's AndroidAudioInputStream format
            be.tarsos.dsp.io.android.AndroidAudioInputStream audioStream =
                    new be.tarsos.dsp.io.android.AndroidAudioInputStream(audioRecord, new be.tarsos.dsp.io.TarsosDSPAudioFormat(sampleRate, 16, 1, true, false));

            audioRecord.startRecording();
            return new AudioDispatcher(audioStream, bufferSize, overlap);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AudioPlaybackCapture", e);
            return null;
        }
    }

    private AudioProcessor createChordProcessor(int sampleRate, int bufferSize) {
        return new AudioProcessor() {
            private final FFT fft = new FFT(bufferSize);
            private final float[] amplitudes = new float[bufferSize / 2];
            private final float[] transformBuffer = new float[bufferSize];

            private final float[] noiseFloor = new float[bufferSize / 2];
            private boolean noiseFloorInitialized = false;

            @Override
            public boolean process(AudioEvent audioEvent) {
                float[] audioFloatBuffer = audioEvent.getFloatBuffer();

                float maxVal = 0;
                for (int i = 0; i < audioFloatBuffer.length && i < transformBuffer.length; i++) {
                    transformBuffer[i] = audioFloatBuffer[i];
                    if (Math.abs(transformBuffer[i]) > maxVal) {
                        maxVal = Math.abs(transformBuffer[i]);
                    }
                }

                // Silence gate
                if (maxVal < 0.002f || Float.isNaN(maxVal) || Float.isInfinite(maxVal)) {
                    handleNoChord();
                    return true;
                }

                float gainMultiplier = 2.0f;
                if (maxVal * gainMultiplier < 1.0f) {
                    for (int i = 0; i < transformBuffer.length; i++) {
                        transformBuffer[i] *= gainMultiplier;
                    }
                } else if (maxVal > 1.0f) {
                    for (int i = 0; i < transformBuffer.length; i++) {
                        transformBuffer[i] /= maxVal;
                    }
                }

                try {
                    fft.forwardTransform(transformBuffer);
                    fft.modulus(transformBuffer, amplitudes);
                } catch (Exception e) {
                    Log.e(TAG, "Error during FFT calculation", e);
                    return true;
                }

                if (!noiseFloorInitialized) {
                    for (int i = 0; i < amplitudes.length; i++) {
                        noiseFloor[i] = amplitudes[i];
                    }
                    noiseFloorInitialized = true;
                } else {
                    for (int i = 0; i < amplitudes.length; i++) {
                        noiseFloor[i] = noiseFloor[i] * 0.98f + amplitudes[i] * 0.02f;
                    }
                }

                float[] chromaVector = calculateChroma(amplitudes, noiseFloor, sampleRate, bufferSize);

                boolean hasNaN = false;
                for (float val : chromaVector) {
                    if (Float.isNaN(val) || Float.isInfinite(val)) {
                        hasNaN = true;
                        break;
                    }
                }

                String detectedChord = "-";
                if (!hasNaN) {
                    detectedChord = recognizeChord(chromaVector, lastReportedChord);
                }

                if (detectedChord.equals("-")) {
                    handleNoChord();
                    return true;
                }

                silentOrUnrecognizedFrames = 0;

                // Stabilization / Debounce logic
                if (detectedChord.equals(candidateChord)) {
                    consecutiveFrames++;
                    if (consecutiveFrames >= REQUIRED_CONSECUTIVE_FRAMES) {
                        if (!detectedChord.equals(lastReportedChord)) {
                            lastReportedChord = detectedChord;
                            Log.i(TAG, ">>> Stable Chord Published to UI: " + lastReportedChord);
                            if (listener != null) {
                                listener.onChordDetected(lastReportedChord);
                            }
                        }
                    }
                } else {
                    candidateChord = detectedChord;
                    consecutiveFrames = 1;
                }

                return true;
            }

            private void handleNoChord() {
                silentOrUnrecognizedFrames++;
                if (silentOrUnrecognizedFrames >= FRAMES_TO_RESET) {
                    candidateChord = "-";
                    consecutiveFrames = 0;
                    if (!lastReportedChord.equals("-")) {
                        lastReportedChord = "-";
                        Log.i(TAG, ">>> Chord reset to '-' due to silence/decay.");
                        if (listener != null) {
                            listener.onChordDetected("-");
                        }
                    }
                }
            }

            @Override
            public void processingFinished() {
                Log.d(TAG, "Audio processing finished.");
            }
        };
    }

    private float[] calculateChroma(float[] amplitudes, float[] noiseFloor, int sampleRate, int bufferSize) {
        float[] chroma = new float[12];
        float binWidth = (float) sampleRate / bufferSize;

        int startBin = Math.max(1, (int) (65 / binWidth));
        int endBin = Math.min(amplitudes.length, (int) (1200 / binWidth));

        for (int i = startBin; i < endBin; i++) {
            float freq = i * binWidth;
            if (freq <= 0) continue;

            float cleanAmplitude = amplitudes[i] - (noiseFloor[i] * 1.3f);
            if (cleanAmplitude < 0) {
                continue;
            }

            double midiNote = 69 + 12 * Math.log(freq / 440.0) / Math.log(2);
            int pitchClass = (int) Math.round(midiNote) % 12;
            if (pitchClass < 0) pitchClass += 12;

            chroma[pitchClass] += cleanAmplitude;
        }

        float max = 0;
        for (float val : chroma) {
            if (val > max && !Float.isNaN(val) && !Float.isInfinite(val)) {
                max = val;
            }
        }

        if (max > 0) {
            for (int i = 0; i < chroma.length; i++) {
                chroma[i] /= max;
            }
        }

        return chroma;
    }

    private String recognizeChord(float[] chroma, String currentChord) {
        String bestChord = "-";
        float bestScore = 0.55f;
        float secondBestScore = 0.0f;

        StringBuilder scoreLog = new StringBuilder("Scores -> ");

        for (int root = 0; root < 12; root++) {
            float rootVal = chroma[root];
            float majorThirdVal = chroma[(root + 4) % 12];
            float minorThirdVal = chroma[(root + 3) % 12];
            float fifthVal = chroma[(root + 7) % 12];

            String chordName = noteNames[root];

            // ==================== MAJOR CHORDS ====================
            float majorScore = (rootVal * 1.1f + majorThirdVal * 1.15f + fifthVal * 1.1f) / 3.35f;

            if (chordName.equals("F")) {
                majorScore -= 0.18f;
            }
            if (chordName.equals("G#") || chordName.equals("B")) {
                majorScore += 0.07f;
            }
            if (chordName.equals("G") || chordName.equals("C") || chordName.equals("D") || chordName.equals("A") || chordName.equals("E") || chordName.equals("B") || chordName.equals("G#")) {
                majorScore += 0.05f;
            }
            if (!currentChord.equals("-") && chordName.equals(currentChord)) {
                majorScore += 0.08f;
            }

            if (majorScore > 0.44f) {
                scoreLog.append(String.format("%s: Maj(%.2f) ", chordName, majorScore));
            }

            if (majorScore > bestScore) {
                secondBestScore = bestScore;
                bestScore = majorScore;
                bestChord = chordName;
            } else if (majorScore > secondBestScore) {
                secondBestScore = majorScore;
            }

            // ==================== MINOR CHORDS ====================
            float minorScore = (rootVal * 1.1f + minorThirdVal * 1.15f + fifthVal * 1.1f) / 3.35f;

            String minorChordName = chordName + "m";

            if (minorChordName.equals("Fm")) {
                minorScore -= 0.15f;
            } else if (minorChordName.equals("Dm")) {
                minorScore -= 0.08f;
            }

            if (minorChordName.equals("Em") || minorChordName.equals("Am") || minorChordName.equals("Bm") || minorChordName.equals("G#m")) {
                minorScore += 0.08f;
            }

            if (!currentChord.equals("-") && minorChordName.equals(currentChord)) {
                minorScore += 0.08f;
            }

            if (minorScore > 0.44f) {
                scoreLog.append(String.format("%s(%.2f) ", minorChordName, minorScore));
            }

            if (minorScore > bestScore) {
                secondBestScore = bestScore;
                bestScore = minorScore;
                bestChord = minorChordName;
            } else if (minorScore > secondBestScore) {
                secondBestScore = majorScore; // Wait, secondBestScore tracking fix:
            }
        }

        if (bestChord.equals("-") || (bestScore - secondBestScore < 0.03f && bestScore < 0.57f)) {
            return "-";
        }

        Log.d(TAG,"bestChord:"+bestChord+"  bestScore:"+bestScore);
        return bestChord;
    }

    public void stopListening() {
        Log.d(TAG, "Stopping chord detector listening...");
        if (chordDispatcher != null) {
            try {
                chordDispatcher.stop();
                chordDispatcher = null;
                Log.d(TAG, "Chord dispatcher stopped successfully.");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping chord dispatcher", e);
            }
        }
    }
}
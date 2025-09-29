package com.garethevans.church.opensongtablet.multitrack;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.MainActivity;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSlider;
import com.garethevans.church.opensongtablet.customviews.TrackSlider;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// Testing out the option of a multitrack mixer

@SuppressLint("NewApi")
public class MultiTrackPlayer {

    private final String TAG = "MultiTrackPlayer";
    private final MainActivityInterface mainActivityInterface;
    private final int defaultSampleRate = 44100;
    private final int defaultChannels = 2;
    private final int defaultBytesPerChannel = 2;
    private final int stereoBufferSize = AudioTrack.getMinBufferSize(
            defaultSampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
    );
    public String trackInfoFilename = "trackInfo.json";
    private AudioTrack audioTrack;
    private final AudioTrackPositionTracker audioTrackPositionTracker;
    private boolean isPlaying = false;
    private Uri multiTrackFolderUri;
    private final Context c;
    private ArrayList<TrackSlider> trackSliders;
    private MyMaterialSlider trackProgressView = null;
    private TrackSlider masterSlider = null;
    private int trackLengthSecs = 0;
    private final String masterTrackIdentifier = "___master_track___";
    private float masterGainBoost = 1f;

    // To deal with updating the volume indicators and playback position
    private ScheduledFuture<?> updatePopUpFuture;
    private ScheduledExecutorService updatePopUpScheduler;
    private Runnable updatePopUpRunnable;

    private boolean isFadingIn = false;
    private boolean isFadingOut = false;
    private int fadeSamplePosition = 0;
    private int fadeTotalSamples;
    private final int fadeDurationMs = 100;

    private boolean usingSolos = false;
    private ArrayList<AudioInputTrack> trackList;
    private AudioInputTrack trackMaster;
    public MultiTrackPlayer(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        initiliaseTheAudioTrack();
        // Currently the audioTrack is null, but that's fine, we add that later when it is initialised
        audioTrackPositionTracker = new AudioTrackPositionTracker(null, defaultSampleRate, defaultChannels, defaultBytesPerChannel);
        fadeTotalSamples = audioTrackPositionTracker.getSampleRate() * fadeDurationMs / 1000;
        masterGainBoost = mainActivityInterface.getPreferences().getMyPreferenceFloat("masterGainBoost",masterGainBoost);
    }

    // The following is extra information sent by the PopUpWindow with its childred
    public void setTrackSliders(ArrayList<TrackSlider> trackSliders, TrackSlider masterSlider) {
        this.trackSliders = trackSliders;
        this.masterSlider = masterSlider;
    }

    // The masterTrack stuff
    public AudioInputTrack getTrackMaster() {
        return trackMaster;
    }
    public String getMasterTrackIdentifier() {
        return masterTrackIdentifier;
    }
    public void setMasterGainBoost(float masterGainBoost) {
        this.masterGainBoost = masterGainBoost;
        mainActivityInterface.getPreferences().getMyPreferenceFloat("masterGainBoost",masterGainBoost);
    }

    public void initiliaseTheAudioTrack() {
        // first, create the required objects for new constructor
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        AudioFormat audioFormat = new AudioFormat.Builder()
                .setSampleRate(defaultSampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build();

        audioTrack = new AudioTrack(audioAttributes,
                audioFormat, stereoBufferSize, AudioTrack.MODE_STREAM, 0);

        if (audioTrackPositionTracker!=null) {
            audioTrackPositionTracker.setAudioTrack(audioTrack);
            fadeTotalSamples = audioTrackPositionTracker.getSampleRate() * fadeDurationMs / 1000;
        }

        // Check the audioTracker has the correct encoding if the song hasn't already been processed
        checkAudioTracker();
    }


    // Get track values (required to draw the sliders)
    public String getTrackName(int trackNumber) {
        if (trackNumber >= 0 && trackNumber < trackList.size()) {
            return trackList.get(trackNumber).getTrackName();
        } else {
            return null;
        }
    }
    public int getTrackVolume(int trackNumber) {
        if (trackNumber >= 0 && trackNumber < trackList.size()) {
            return trackList.get(trackNumber).getTrackVolumeInt();
        } else {
            return 100;
        }
    }
    public String getTrackPan(int trackNumber) {
        if (trackNumber >=0 && trackNumber < trackList.size()) {
            return trackList.get(trackNumber).getTrackPanString();
        } else {
            return "C";
        }
    }
    public boolean getTrackMute(int trackNumber) {
        if (trackNumber >= 0 && trackNumber < trackList.size()) {
            return trackList.get(trackNumber).getTrackMute();
        } else {
            return false;
        }
    }
    public boolean getTrackSolo(int trackNumber) {
        if (trackNumber >= 0 && trackNumber < trackList.size()) {
            return trackList.get(trackNumber).getTrackSolo();
        } else {
            return false;
        }
    }

    // Set the track values (set by the sliders)
    public void setVolume(int trackNumber, int volume) {
        if (trackNumber >=0 && trackNumber < trackList.size()) {
            trackList.get(trackNumber).setTrackVolumeInt(volume);
        } else {
            // This is the master gain
            if (trackMaster!=null) {
                trackMaster.setTrackVolumeInt(volume);
            }
        }
    }
    public void setPan(int trackNumber, String pan) {
        if (trackNumber >=0 && trackNumber < trackList.size()) {
            trackList.get(trackNumber).setTrackPanString(pan);
        } else {
            // This is master pan
            if (trackMaster!=null) {
                trackMaster.setTrackPanString(pan);
            }
        }
    }
    public void setMute(int trackNumber, boolean mute) {
        if (trackNumber >= 0 && trackNumber < trackList.size()) {
            trackList.get(trackNumber).setTrackMute(mute);
        }
        // Master doesn't not have a mute
    }
    public void setSolo(int trackNumber, boolean solo) {
        if (trackNumber >= 0 && trackNumber < trackList.size()) {
            trackList.get(trackNumber).setTrackSolo(solo);
        }
        // Master doesn't have a solo
    }
    public void initialiseArrays(Uri multiTrackFolderUri, ArrayList<AudioTrackValues> audioTrackValues, int trackLengthSecs) {
        this.multiTrackFolderUri = multiTrackFolderUri;
        this.trackLengthSecs = trackLengthSecs;
        isPlaying = false;

        trackMaster = null;
        if (audioTrackValues != null) {
            trackList = new ArrayList<>();
            // The audioTrackValues should exist for each track + the master
            for (int i = 0; i < audioTrackValues.size(); i++) {
                AudioTrackValues audioTrackValue = audioTrackValues.get(i);
                String trackName = audioTrackValue.getTrackName();
                int trackVolume = audioTrackValue.getTrackVolume()==null ? 100 : audioTrackValue.getTrackVolume();
                String trackPan = audioTrackValue.getTrackPan()==null ? "C" : audioTrackValue.getTrackPan();
                boolean trackMute = audioTrackValue.getTrackMute() != null && audioTrackValue.getTrackMute();
                boolean trackSolo = audioTrackValue.getTrackSolo() != null && audioTrackValue.getTrackSolo();
                Uri trackUri = audioTrackValue.getTrackUri()==null ? null : Uri.parse(audioTrackValue.getTrackUri());
                InputStream trackStream = mainActivityInterface.getStorageAccess().getInputStream(trackUri);
                AudioInputTrack audioInputTrack = new AudioInputTrack(c, trackName, trackUri, trackStream,
                        audioTrackPositionTracker.getChannels(), trackVolume, trackPan, trackMute, trackSolo);

                if (trackName!=null && trackName.equals(masterTrackIdentifier)) {
                    trackMaster = audioInputTrack;
                } else {
                    trackList.add(audioInputTrack);
                }
            }
        }

        // If we don't have a master in the json file, just make a new one
        if (trackMaster == null) {
            trackMaster = new AudioInputTrack(c,masterTrackIdentifier, null,
                    null, audioTrackPositionTracker.getChannels(),
                    100, "C", false, false);
        }

        // If we haven't processed this song, get the audio file settings
        checkAudioTracker();
    }

    // Prepare the audioTracker (holds the audioTrack info)
    // This will also read any wav headers
    public void checkAudioTracker() {
        // Create the position tracker once on class initiation.
        // After that we simply update it once we have the streams ready
        if (audioTrackPositionTracker!=null && !audioTrackPositionTracker.getAudioInfoSetForSong()) {

            if (trackList != null && !trackList.isEmpty() && trackList.get(0) != null) {
                // We can use this audioTrack and use the streams to get the track info
                audioTrackPositionTracker.setAudioTrack(audioTrack);
                try {
                    WavHeader wavHeader = parseWavHeader(trackList.get(0).getTrackStream());
                    if (wavHeader.sampleRate>0) {
                        audioTrackPositionTracker.setSampleRate(wavHeader.sampleRate);
                    }
                    if (wavHeader.numChannels>0) {
                        audioTrackPositionTracker.setChannels(wavHeader.numChannels);
                    }
                    if (wavHeader.bytesPerChannel>0) {
                        audioTrackPositionTracker.setBytesPerChannel(wavHeader.bytesPerChannel);
                    }
                    long frameSize = (long) (wavHeader.bytesPerChannel) * wavHeader.numChannels;
                    if (frameSize>0) {
                        audioTrackPositionTracker.setFrameSize((int) frameSize);
                    }
                    if (wavHeader.dataStartOffset>=0 && wavHeader.sampleRate>0) {
                        audioTrackPositionTracker.setSeekOffsetMs((wavHeader.dataStartOffset * 1000L) / wavHeader.sampleRate);
                    }
                    if (wavHeader.getDurationMs()>0) {
                        trackLengthSecs = (int)Math.round(((double) wavHeader.getDurationMs() / 1000));
                        if (trackProgressView!=null) {
                            mainActivityInterface.getMainHandler().post(() -> {
                                if (trackProgressView!=null) {
                                    trackProgressView.setValueTo(trackLengthSecs);
                                    trackProgressView.setHint(mainActivityInterface.getTimeTools().timeFormatFixer( + 0) + " / " +
                                            mainActivityInterface.getTimeTools().timeFormatFixer(trackLengthSecs));

                                }
                            });
                        }
                    }
                    audioTrackPositionTracker.setAudioInfoSetForSong(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            audioTrackPositionTracker.setSeekOffsetMs(0);
            fadeTotalSamples = audioTrackPositionTracker.getSampleRate() * fadeDurationMs / 1000;
        }
    }

    public WavHeader parseWavHeader(InputStream inputStream) {
        // Set the defaults
        WavHeader wavHeader = new WavHeader();
        wavHeader.bitsPerChannel = defaultBytesPerChannel * 8;
        wavHeader.bytesPerChannel = defaultBytesPerChannel;
        wavHeader.numChannels = defaultChannels;
        wavHeader.sampleRate = defaultSampleRate;
        wavHeader.dataStartOffset = 0;

        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(inputStream));
            byte[] buffer = new byte[12];

            // === Read RIFF header ===
            dis.readFully(buffer, 0, 12);
            String riff = new String(buffer, 0, 4);
            String wave = new String(buffer, 8, 4);

            if (!"RIFF".equals(riff)) throw new IOException("Not a RIFF file");
            if (!"WAVE".equals(wave)) throw new IOException("Not a WAVE file");

            boolean foundFmt = false;
            boolean foundData = false;

            int totalOffset = 12;

            // === Parse chunks ===
            while (true) {
                byte[] chunkHeader = new byte[8];
                if (dis.read(chunkHeader) < 8) break; // no more chunks
                totalOffset += 8;

                String chunkId = new String(chunkHeader, 0, 4);
                int chunkSize = ((chunkHeader[7] & 0xFF) << 24) | ((chunkHeader[6] & 0xFF) << 16)
                        | ((chunkHeader[5] & 0xFF) << 8) | (chunkHeader[4] & 0xFF);

                if ("fmt ".equals(chunkId)) {
                    byte[] fmtChunk = new byte[chunkSize];
                    dis.readFully(fmtChunk);
                    totalOffset += chunkSize;

                    wavHeader.audioFormat = (fmtChunk[1] << 8 & 0xFF00) | (fmtChunk[0] & 0xFF);
                    wavHeader.numChannels   = (fmtChunk[3] << 8 & 0xFF00) | (fmtChunk[2] & 0xFF);
                    wavHeader.sampleRate    = ((fmtChunk[7] & 0xFF) << 24) | ((fmtChunk[6] & 0xFF) << 16) | ((fmtChunk[5] & 0xFF) << 8) | (fmtChunk[4] & 0xFF);
                    wavHeader.byteRate      = ((fmtChunk[11] & 0xFF) << 24) | ((fmtChunk[10] & 0xFF) << 16) | ((fmtChunk[9] & 0xFF) << 8) | (fmtChunk[8] & 0xFF);
                    wavHeader.blockAlign    = (fmtChunk[13] << 8 & 0xFF00) | (fmtChunk[12] & 0xFF);
                    wavHeader.bitsPerChannel = (fmtChunk[15] << 8 & 0xFF00) | (fmtChunk[14] & 0xFF);
                    wavHeader.bytesPerChannel = wavHeader.bitsPerChannel / 8;

                    foundFmt = true;
                } else if ("data".equals(chunkId)) {
                    wavHeader.dataSize = chunkSize;
                    wavHeader.dataStartOffset = totalOffset;

                    // Stop reading further chunks — we found data
                    foundData = true;
                    break;
                } else {
                    // Skip unknown chunk
                    dis.skipBytes(chunkSize);
                    totalOffset += chunkSize;
                }
            }

            if (!foundFmt) throw new IOException("Missing fmt chunk");
            if (!foundData) throw new IOException("Missing data chunk");

            return wavHeader;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wavHeader;
    }
    public void setAudioInfoSetForSong(boolean audioInfoSetForSong) {
        audioTrackPositionTracker.setAudioInfoSetForSong(audioInfoSetForSong);
    }



    // The transport controls
    public void play() {
        if (!isPlaying) {
            if (audioTrack == null || audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                initiliaseTheAudioTrack();
            }
            isPlaying = true;
            isFadingIn = true;
            isFadingOut = false;
            fadeSamplePosition = 0;

            // Reopen our trackStreams and skip if required
            for (AudioInputTrack track : trackList) {
                track.reopen();
                track.skip(audioTrackPositionTracker.getSkipBytes());
            }

            // Put the set the progress slider
            if (trackProgressView != null) {
                mainActivityInterface.getMainHandler().post(() -> {
                    if (trackProgressView != null) {
                        trackProgressView.setValue((int)(audioTrackPositionTracker.getPlaybackPositionMs()/1000L));
                    }
                });
            }

            // Start the audioTrack player ready for content
            audioTrack.play();

            // Now run the thread that feeds the audioTrack
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                updatePopUpScheduler = Executors.newScheduledThreadPool(1);
                updatePopUpRunnable = () -> {
                    if (audioTrack != null && isPlaying) {
                        updatePopUpViews();
                    }
                };
                updatePopUpFuture = updatePopUpScheduler.scheduleWithFixedDelay(updatePopUpRunnable, 0, 100, TimeUnit.MILLISECONDS);

                try {
                    short[] mixBuffer = new short[1024 * 2]; // stereo output
                    float masterLGain;
                    float masterRGain;
                    float masterGain;

                    while (isPlaying) {

                        Arrays.fill(mixBuffer, (short) 0);

                        // Check the master gain track
                        masterGain = trackMaster.getTrackVolume() * masterGainBoost;
                        masterLGain = masterGain;
                        masterRGain = masterGain;
                        if (trackMaster!=null && trackMaster.getTrackPanString().equals("L")) {
                            masterRGain = 0;
                        } else if (trackMaster!=null && trackMaster.getTrackPanString().equals("R")) {
                            masterLGain = 0;
                        }

                        // Quickly look to see if any tracks are solo
                        usingSolos = false;
                        for (AudioInputTrack track : trackList) {
                            if (track.getTrackSolo()) {
                                usingSolos = true;
                                break;
                            }
                        }

                        for (AudioInputTrack track : trackList) {
                            try {
                                int frames = track.readNext();
                                if (frames <= 0) continue;

                                short[] buf = track.getTrackBuffer();
                                int length = track.getTrackBufferLength();  // total samples, not frames

                                int frameCount = track.getTrackChannels() == 2 ? length / 2 : length;

                                float vol = track.getTrackVolume();
                                float pan = track.getTrackPan();
                                float leftGain = (pan <= 0) ? 1f : 1f - pan;
                                float rightGain = (pan >= 0) ? 1f : 1f + pan;

                                int outL;
                                int outR;

                                for (int i = 0; i < frameCount; i++) {
                                    short left = track.getTrackChannels() == 2 ? buf[i * 2] : buf[i];
                                    short right = track.getTrackChannels() == 2 ? buf[i * 2 + 1] : buf[i];

                                    if (track.getTrackMute()) {
                                        left = 0;
                                        right = 0;
                                    }

                                    if (usingSolos) {
                                        left = track.getTrackSolo() ? left : 0;
                                        right = track.getTrackSolo() ? right : 0;
                                    }

                                    outL = mixBuffer[i * 2] + (int) (left * vol * leftGain * masterLGain);
                                    outR = mixBuffer[i * 2 + 1] + (int) (right * vol * rightGain * masterRGain);

                                    mixBuffer[i * 2] = clamp16(outL);
                                    mixBuffer[i * 2 + 1] = clamp16(outR);
                                }
                            } catch (Exception e) {
                                Log.d(TAG,"The streams were unexpectedly closed");
                                isPlaying = false;
                                return;
                            }
                        }

                        if (audioTrack!=null) {
                            if (isFadingIn || isFadingOut) {
                                applyFade(mixBuffer, mixBuffer.length);
                            }
                            audioTrack.write(mixBuffer, 0, mixBuffer.length);
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "AudioTrack ended - likely window closed/stream badly closed");
                }

                if (audioTrack != null && audioTrack.getState() == AudioTrack.STATE_INITIALIZED && isPlaying) {
                    try {
                        stop();
                    } catch (Exception e) {
                        Log.d(TAG,"Unable to stop the audio track");
                    }
                }
            });
        }
    }
    public void pause() {
        isFadingOut = true;
        isFadingIn = false;
        fadeSamplePosition = 0;

        if (audioTrack!=null && isPlaying && audioTrack.getState()==AudioTrack.STATE_INITIALIZED) {
            int trackProgressMs = (int)(trackProgressView.getValue()*1000L);
            try {
                audioTrack.pause();
                audioTrack.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            audioTrackPositionTracker.setSeekOffsetMs(trackProgressMs);
        }
        isPlaying = false;
        finishScheduledExecutor();
        updatePopUpViews();
    }
    public void stop() {
        pause();
        audioTrackPositionTracker.setSeekOffsetMs(0);
        if (trackProgressView != null) {
            mainActivityInterface.getMainHandler().post(() -> {
                if (trackProgressView!=null) {
                    if (trackProgressView.getValueTo()>0) {
                        trackProgressView.setValue(0);
                    }
                }
            });
        }
    }

    // The track progressBar stuff
    public void setTrackProgressView(MyMaterialSlider trackProgressView) {
        this.trackProgressView = trackProgressView;
        if (trackProgressView!=null) {
            trackProgressView.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                boolean wasPlaying = false;
                boolean userSeeking = false;
                @Override
                public void onStartTrackingTouch(@NonNull Slider slider) {
                    userSeeking = true;
                    wasPlaying = isPlaying;
                    if (wasPlaying && audioTrack!=null) {
                        pause();
                    }
                }

                @Override
                public void onStopTrackingTouch(@NonNull Slider slider) {
                    int seekMs = (int)(slider.getValue() * 1000L);
                    if (audioTrack!=null) {
                        audioTrackPositionTracker.setSeekOffsetMs(seekMs);
                        if (wasPlaying) {
                            play();
                        }
                    }
                    userSeeking = false;
                }
            });
            trackProgressView.addOnChangeListener((slider, value, fromUser) -> trackProgressView.setHint(mainActivityInterface.getTimeTools().timeFormatFixer((int)value) + " / " +
                    mainActivityInterface.getTimeTools().timeFormatFixer(trackLengthSecs)));
            trackProgressView.setValueTo(trackLengthSecs);
            trackProgressView.setLabelFormatter(new LabelFormatter() {
                @NonNull
                @Override
                public String getFormattedValue(float value) {
                    return mainActivityInterface.getTimeTools().timeFormatFixer((int)value);
                }
            });
        }
    }

    private void applyFade(short[] buffer, int length) {
        for (int i = 0; i < length; i++) {
            float gain = 1.0f;
            if (isFadingIn && fadeSamplePosition < fadeTotalSamples) {
                gain = fadeSamplePosition / (float) fadeTotalSamples;
                fadeSamplePosition++;
                if (fadeSamplePosition >= fadeTotalSamples) isFadingIn = false;
            } else if (isFadingOut && fadeSamplePosition < fadeTotalSamples) {
                gain = 1.0f - (fadeSamplePosition / (float) fadeTotalSamples);
                fadeSamplePosition++;
                if (fadeSamplePosition >= fadeTotalSamples) {
                    isFadingOut = false;
                    stop(); // you define this to fully stop and flush
                    return;
                }
            }
            buffer[i] = (short) (buffer[i] * gain);
        }
    }

    private short clamp16(int value) {
        return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, value));
    }

    private float computeRMS(short[] buffer, int len) {
        if (len!=0) {
            try {
                double sum = 0;
                for (int i = 0; i < len; i++) {
                    sum += buffer[i] * buffer[i];
                }
                return (float) Math.sqrt(sum / len) / (Short.MAX_VALUE + 1); // Normalize to 0–1

            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0f;
        }
        return 0f;
    }

    // Call every 100ms, or when paused
    public void updatePopUpViews() {
        float totalLevel = 0;
        for (int i = 0; i < trackList.size(); i++) {
            AudioInputTrack track = trackList.get(i);
            float rms = 0;
            if (!track.getTrackMute() && (!usingSolos || track.getTrackSolo()) && isPlaying) {
                rms = computeRMS(track.getTrackBuffer(), track.getTrackBufferLength());
                rms = rms * track.getTrackVolume();
            }

            totalLevel += rms;

            final float newRMS = rms;
            final int newI = i;
            mainActivityInterface.getMainHandler().post(() -> {
                if (trackSliders!=null && trackSliders.get(newI)!=null) {
                    trackSliders.get(newI).setLevel(trackList.size() * newRMS);
                }
            });
        }

        // Now the master
        final float totalRMS = totalLevel;
        mainActivityInterface.getMainHandler().post(() -> {
            if (masterSlider!=null) {
                masterSlider.setLevel(totalRMS * masterGainBoost * trackMaster.getTrackVolume());
            }
        });

        // Now the playbackHead on the slider
        if (audioTrack!=null && audioTrack.getState()==AudioTrack.STATE_INITIALIZED && isPlaying) {
            int secs = (int)(audioTrackPositionTracker.getPlaybackPositionMs()/1000L);
            mainActivityInterface.getMainHandler().post(() -> {
                if (trackProgressView != null) {
                    trackProgressView.setValue(secs);
                    trackProgressView.setHint(mainActivityInterface.getTimeTools().timeFormatFixer(secs) + " / " +
                            mainActivityInterface.getTimeTools().timeFormatFixer(trackLengthSecs));
                }
            });
            if (secs>trackLengthSecs) {
                Log.d(TAG,"We've reached the end of the song");
                stop();
            }
        }
    }

    public void closeMultitrack() {
        if (trackList!=null && !trackList.isEmpty()) {
            for (AudioInputTrack track : trackList) {
                track.close();
            }
        }
        try {
            if (audioTrack != null) {
                audioTrack.release();
            }
        } catch (Exception e) {
            Log.d(TAG,"Issue releasing the audioTrack - probably wasn't prepared!");
        }

        // Release any views/objects
        trackMaster = null;
        masterSlider = null;
        trackList = null;
        trackSliders = null;
        trackProgressView = null;
    }

    public void saveMultitrackSettings() {
        if (trackList!=null && !trackList.isEmpty() && multiTrackFolderUri != null) {
            // Now build a JSON file with our settings for this multitrack
            ArrayList<AudioTrackValues> audioTrackValues = new ArrayList<>();
            for (int i = 0; i < trackList.size(); i++) {
                AudioTrackValues audioTrackValue = new AudioTrackValues();
                AudioInputTrack track = trackList.get(i);
                audioTrackValue.setTrackName(track.getTrackName());
                audioTrackValue.setTrackVolume(track.getTrackVolumeInt());
                audioTrackValue.setTrackPan(track.getTrackPanString());
                audioTrackValue.setTrackMute(track.getTrackMute());
                audioTrackValue.setTrackSolo(track.getTrackSolo());
                audioTrackValue.setTrackUri(String.valueOf(track.getTrackUri()));
                audioTrackValues.add(audioTrackValue);
            }

            // Now add the master
            AudioTrackValues masterTrackValue = new AudioTrackValues();
            masterTrackValue.setTrackName(masterTrackIdentifier);
            masterTrackValue.setTrackVolume(trackMaster.getTrackVolumeInt());
            masterTrackValue.setTrackPan(trackMaster.getTrackPanString());
            masterTrackValue.setTrackMute(null);
            masterTrackValue.setTrackSolo(null);
            masterTrackValue.setTrackUri(null);
            audioTrackValues.add(masterTrackValue);

            MultiTrackValues multiTrackValues = new MultiTrackValues();
            multiTrackValues.setAudioTrackValues(audioTrackValues);

            String json = MainActivity.gson.toJson(multiTrackValues);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Uri uri = Uri.parse(multiTrackFolderUri + "%2F" + trackInfoFilename);
                if (mainActivityInterface.getStorageAccess().uriExists(uri)) {
                    mainActivityInterface.getStorageAccess().deleteFile(uri);
                }

                uri = null;
                try {
                    uri = DocumentsContract.createDocument(c.getContentResolver(), multiTrackFolderUri, null, trackInfoFilename);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (uri != null) {
                    OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                    mainActivityInterface.getStorageAccess().writeFileFromString(json, outputStream);
                }

            }
        }
    }

    private void finishScheduledExecutor() {
        if (updatePopUpFuture !=null) {
            updatePopUpFuture.cancel(true);
        }
        if (updatePopUpScheduler != null) {
            updatePopUpScheduler.shutdown();
        }
    }
}

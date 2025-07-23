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

import com.garethevans.church.opensongtablet.MainActivity;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSlider;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// Testing out the option of a multitrack mixer

@SuppressLint("NewApi")
public class MultiTrackPlayer {

    private final String TAG = "MultiTrackPlayer";
    private final MainActivityInterface mainActivityInterface;
    private final int SAMPLE_RATE = 44100;
    private final int CHANNELS = 2;
    private final int BYTES_PER_CHANNEL = 2;
    private final int stereoBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
    );
    public String trackInfoFilename = "trackInfo.json";
    private AudioTrack audioTrack;
    private final AudioTrackPositionTracker audioTrackPositionTracker;
    private boolean isPlaying = false;
    private int[] trackVolumes;
    private InputStream[] trackStreams;
    private Uri[] trackUris;
    private boolean[] trackMutes, trackSolos;
    private String[] trackNames, trackPans;
    private Uri multiTrackFolderUri;
    private final Context c;
    private MyMaterialSlider trackProgressView = null;
    private int trackLengthSecs = 0;

    private ScheduledFuture<?> future;
    private ScheduledExecutorService scheduler;
    private Runnable timerRunnable;

    public MultiTrackPlayer(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        initiliaseTheAudioTrack();
        // Currently the audioTrack is null, but that's fine, we add that later when it is initialised
        audioTrackPositionTracker = new AudioTrackPositionTracker(null, SAMPLE_RATE, CHANNELS, BYTES_PER_CHANNEL);
    }

    public void initiliaseTheAudioTrack() {
        // first, create the required objects for new constructor
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        AudioFormat audioFormat = new AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build();

        audioTrack = new AudioTrack(audioAttributes,
                audioFormat, stereoBufferSize, AudioTrack.MODE_STREAM, 0);

        if (audioTrackPositionTracker!=null) {
            audioTrackPositionTracker.setAudioTrack(audioTrack);
        }

        // Check the audioTracker has the correct encoding if the song hasn't already been processed
        checkAudioTracker();
    }

    public void initialiseArrays(Uri multiTrackFolderUri, ArrayList<AudioTrackValues> audioTrackValues, int trackLengthSecs) {
        this.multiTrackFolderUri = multiTrackFolderUri;
        this.trackLengthSecs = trackLengthSecs;
        isPlaying = false;

        if (audioTrackValues != null) {
            trackStreams = new InputStream[audioTrackValues.size()];
            trackUris = new Uri[audioTrackValues.size()];
            trackNames = new String[audioTrackValues.size()];
            trackVolumes = new int[audioTrackValues.size()];
            trackPans = new String[audioTrackValues.size()];
            trackMutes = new boolean[audioTrackValues.size()];
            trackSolos = new boolean[audioTrackValues.size()];

            for (int i = 0; i < audioTrackValues.size(); i++) {
                AudioTrackValues audioTrackValue = audioTrackValues.get(i);
                trackNames[i] = audioTrackValue.getTrackName();
                trackVolumes[i] = audioTrackValue.getTrackVolume();
                trackPans[i] = audioTrackValue.getTrackPan();
                trackMutes[i] = audioTrackValue.getTrackMute();
                trackSolos[i] = audioTrackValue.getTrackSolo();
                trackUris[i] = Uri.parse(audioTrackValue.getTrackUri());
                trackStreams[i] = mainActivityInterface.getStorageAccess().getInputStream(trackUris[i]);
            }

        } else {
            trackStreams = new InputStream[0];
            trackUris = new Uri[0];
            trackNames = new String[0];
            trackVolumes = new int[0];
            trackPans = new String[0];
            trackMutes = new boolean[0];
            trackSolos = new boolean[0];
        }

        // If we haven't processed this song, get the audio file settings
        checkAudioTracker();
    }

    // Get track values (required to draw the sliders)
    public String getTrackName(int trackNumber) {
        if (trackNumber >= 0 && trackNumber < trackNames.length) {
            return trackNames[trackNumber];
        } else {
            return null;
        }
    }

    public int getTrackVolume(int trackNumber) {
        if (trackNumber >= 0 && trackNumber < trackVolumes.length) {
            return trackVolumes[trackNumber];
        } else {
            return 100;
        }
    }

    public String getTrackPan(int trackNumber) {
        if (trackNumber >=0 && trackNumber < trackPans.length) {
            return trackPans[trackNumber];
        } else {
            return "C";
        }
    }

    public boolean getTrackMute(int trackNumber) {
        if (trackNumber >= 0 && trackNumber < trackMutes.length) {
            return trackMutes[trackNumber];
        } else {
            return false;
        }
    }

    public boolean getTrackSolo(int trackNumber) {
        if (trackNumber >= 0 && trackNumber < trackSolos.length) {
            return trackSolos[trackNumber];
        } else {
            return false;
        }
    }



    // Set the track values (set by the sliders)
    public void setVolume(int trackNumber, int volume) {
        if (trackNumber >= 0 && trackNumber < trackVolumes.length) {
            trackVolumes[trackNumber] = volume;
        }
    }
    public void setPan(int trackNumber, String pan) {
        if (trackNumber >= 0 && trackNumber < trackPans.length) {
            trackPans[trackNumber] = pan;
        }
    }

    public void setMute(int trackNumber, boolean mute) {
        if (trackNumber >= 0 && trackNumber < trackMutes.length) {
            trackMutes[trackNumber] = mute;
        }
    }

    public void setSolo(int trackNumber, boolean solo) {
        if (trackNumber >= 0 && trackNumber < trackSolos.length) {
            trackSolos[trackNumber] = solo;
        }
    }


    public void checkAudioTracker() {
        // Create the position tracker once on class initiation.
        // After that we simply update it once we have the streams ready
        if (audioTrackPositionTracker!=null && !audioTrackPositionTracker.getAudioInfoSetForSong()) {

            if (trackStreams != null && trackStreams.length>0 && trackStreams[0] != null) {
                // We can use this audioTrack and use the streams to get the track info
                audioTrackPositionTracker.setAudioTrack(audioTrack);
                try {
                    WavHeader wavHeader = parseWavHeader(trackStreams[0]);
                    if (wavHeader.sampleRate>0) {
                        audioTrackPositionTracker.setSampleRate(wavHeader.sampleRate);
                    }
                    if (wavHeader.channels>0) {
                        audioTrackPositionTracker.setChannels(wavHeader.channels);
                    }
                    if (wavHeader.bytesPerSample>0) {
                        audioTrackPositionTracker.setBytesPerSample(wavHeader.bytesPerSample);
                    }
                    long frameSize = (long) (wavHeader.bitsPerSample / 8) * wavHeader.channels;
                    if (frameSize>0) {
                        audioTrackPositionTracker.setFrameSize((int) frameSize);
                    }
                    if (wavHeader.dataStartOffset>=0 && wavHeader.sampleRate>0) {
                        audioTrackPositionTracker.setSeekOffsetMs((wavHeader.dataStartOffset * 1000) / wavHeader.sampleRate);
                    }
                    audioTrackPositionTracker.setAudioInfoSetForSong(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void setAudioInfoSetForSong(boolean audioInfoSetForSong) {
        audioTrackPositionTracker.setAudioInfoSetForSong(audioInfoSetForSong);
    }

    // The transport controls
    public void play() {
        if (!isPlaying) {
            if (audioTrack==null || audioTrack.getState()!=AudioTrack.STATE_INITIALIZED) {
                initiliaseTheAudioTrack();
            }

            // Because we can release/close streams, we rebuild them on play
            checkStreams();

            // Check if we have setup the correct sample rate, etc.
            checkAudioTracker();

            if (audioTrackPositionTracker.getSeekOffsetMs() > 0) {
                long skipBytes = audioTrackPositionTracker.getSkipBytes();
                for (InputStream trackStream : trackStreams) {
                    try {
                        // Skip byteOffset bytes into the stream
                        long skipped = 0;
                        while (skipped < skipBytes) {
                            long s = trackStream.skip(skipBytes - skipped);
                            if (s <= 0) break;
                            skipped += s;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            isPlaying = true;

            // Check the playback head position
            if (trackProgressView != null) {
                mainActivityInterface.getMainHandler().post(() -> {
                    if (trackProgressView != null) {
                        updatePlaybackHead();
                    }
                });
            }


            // Start the audioTrack player ready for content
            audioTrack.play();

            // Now run the thread that feeds the audioTrack
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                scheduler = Executors.newScheduledThreadPool(1);
                timerRunnable = () -> {
                    if (audioTrack != null && isPlaying) {
                        updatePlaybackHead();
                    }
                };
                future = scheduler.scheduleWithFixedDelay(timerRunnable,0, 1, TimeUnit.SECONDS);

                try {
                    byte[][] buffers = new byte[trackStreams.length][stereoBufferSize];
                    byte[] mixedBuffer = new byte[stereoBufferSize];

                    while (isPlaying) {
                        boolean dataAvailable = false;
                        boolean usingSolos = false;

                        for (boolean trackSolo : trackSolos) {
                            if (trackSolo) {
                                usingSolos = true;
                                break;
                            }
                        }

                        // Read from all tracks
                        for (int i = 0; i < trackStreams.length; i++) {
                            if (trackStreams[i] != null) {
                                int read = trackStreams[i].read(buffers[i], 0, stereoBufferSize);
                                if (read == -1) {
                                    buffers[i] = new byte[stereoBufferSize]; // fill with silence
                                } else {
                                    dataAvailable = true;
                                }
                            } else {
                                isPlaying = false;
                                stop();
                                return;
                            }
                        }

                        if (!dataAvailable) {
                            stop();
                            break;
                        }

                        // Mix stereo: handle every 4 bytes (2 bytes left, 2 bytes right)
                        for (int i = 0; i < stereoBufferSize; i += (BYTES_PER_CHANNEL*CHANNELS)) {
                            int mixedLeft = 0;
                            int mixedRight = 0;

                            for (int j = 0; j < trackStreams.length; j++) {
                                int left = (buffers[j][i] & 0xFF) | (buffers[j][i + 1] << 8);
                                int right = (buffers[j][i + 2] & 0xFF) | (buffers[j][i + 3] << 8);

                                float volumeToUseL = (float) trackVolumes[j] / 100f;
                                float volumeToUseR = (float) trackVolumes[j] / 100f;

                                String panToUse = trackPans[j]==null ? "C" : trackPans[j];

                                switch (panToUse) {
                                    case "L":
                                        volumeToUseR = 0;
                                        break;
                                    case "R":
                                        volumeToUseL = 0;
                                        break;
                                }

                                if (trackMutes[j]) {
                                    volumeToUseL = 0;
                                    volumeToUseR = 0;
                                }
                                if (usingSolos) {
                                    volumeToUseL = trackSolos[j] ? volumeToUseL : 0;
                                    volumeToUseR = trackSolos[j] ? volumeToUseR : 0;
                                }

                                mixedLeft += (int) (left * volumeToUseL);
                                mixedRight += (int) (right * volumeToUseR);
                            }

                            mixedLeft /= trackStreams.length;
                            mixedRight /= trackStreams.length;

                            // Clamp to 16-bit range
                            int finalLeft = Math.max(-32768, Math.min(32767, mixedLeft));
                            int finalRight = Math.max(-32768, Math.min(32767, mixedRight));

                            // Write to output buffer
                            mixedBuffer[i] = (byte) (finalLeft & 0xFF);
                            mixedBuffer[i + 1] = (byte) ((finalLeft >> 8) & 0xFF);
                            mixedBuffer[i + 2] = (byte) (finalRight & 0xFF);
                            mixedBuffer[i + 3] = (byte) ((finalRight >> 8) & 0xFF);
                        }

                        if (audioTrack!=null) {
                            audioTrack.write(mixedBuffer, 0, mixedBuffer.length);
                        } else {
                            isPlaying = false;
                            stop();
                            finishScheduledExecutor();
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG,"AudioTrack ended - likely window closed/stream badly closed");
                }

                if (audioTrack!=null && audioTrack.getState()==AudioTrack.STATE_INITIALIZED) {
                    audioTrack.stop();
                }
            });
        }
    }

    public void pause() {
        finishScheduledExecutor();
        if (audioTrack!=null && isPlaying && audioTrack.getState()==AudioTrack.STATE_INITIALIZED) {
            audioTrack.pause();
            audioTrack.flush();
        }
        isPlaying = false;
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

    public void checkStreams() {
        // Release any old streams
        if (trackStreams != null) {
            for (InputStream stream : trackStreams) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Now rebuild the inputstreams from the uris
        trackStreams = new InputStream[trackUris.length];
        for (int i = 0; i < trackUris.length; i++) {
            trackStreams[i] = mainActivityInterface.getStorageAccess().getInputStream(trackUris[i]);
        }

    }

    public void setTrackProgressView(MyMaterialSlider trackProgressView) {
        this.trackProgressView = trackProgressView;
    }

    public void movePlayheadPositionSecs(int secs) {
        audioTrackPositionTracker.setSeekOffsetMs(secs * 1000L);
        if (audioTrack!=null) {
            audioTrack.setPlaybackHeadPosition((int)audioTrackPositionTracker.getSkipBytes());
        }
    }

    public void setSeekPosition(int seekPositionSecs) {
        audioTrackPositionTracker.setSeekOffsetMs(seekPositionSecs * 1000L);
    }


    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void updatePlaybackHead() {
        if (trackProgressView != null) {
            mainActivityInterface.getMainHandler().post(() -> {
                if (trackProgressView != null) {
                    long now = audioTrackPositionTracker.getPlaybackPositionMs();
                    int secs = Math.round(now / 1000f);
                    if (trackProgressView.getValueTo()>=secs) {
                        trackProgressView.setValue(secs);
                        trackProgressView.setHint(mainActivityInterface.getTimeTools().timeFormatFixer(secs) + " / " +
                                mainActivityInterface.getTimeTools().timeFormatFixer(trackLengthSecs));
                    }
                }
            });
        }
    }

    public void closeMultitrack() {
        try {
            if (audioTrack != null) {
                audioTrack.release();
            }
        } catch (Exception e) {
            Log.d(TAG,"Issue releasing the audioTrack - probably wasn't prepared!");
        }
        try {
            if (trackStreams!=null) {
                for (InputStream stream : trackStreams) {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG,"Issue closing the input streams");
        }
    }

    public void saveMultitrackSettings() {
        if (trackStreams!=null && trackStreams.length > 0 && multiTrackFolderUri != null) {
            // Now build a JSON file with our settings for this multitrack
            ArrayList<AudioTrackValues> audioTrackValues = new ArrayList<>();
            for (int i = 0; i < trackStreams.length; i++) {
                AudioTrackValues audioTrackValue = new AudioTrackValues();
                audioTrackValue.setTrackName(trackNames[i]);
                audioTrackValue.setTrackVolume(trackVolumes[i]);
                audioTrackValue.setTrackPan(trackPans[i]);
                audioTrackValue.setTrackMute(trackMutes[i]);
                audioTrackValue.setTrackSolo(trackSolos[i]);
                audioTrackValue.setTrackUri(trackUris[i].toString());
                audioTrackValues.add(audioTrackValue);
            }
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






    public WavHeader parseWavHeader(InputStream inputStream) {
        WavHeader wavHeader = new WavHeader();
        wavHeader.bitsPerSample = BYTES_PER_CHANNEL;
        wavHeader.bytesPerSample = BYTES_PER_CHANNEL;
        wavHeader.channels = CHANNELS;
        wavHeader.sampleRate = SAMPLE_RATE;

        try {
            DataInputStream dis = new DataInputStream(new BufferedInputStream(inputStream));

            byte[] header = new byte[12];
            dis.readFully(header);
            boolean doContinue = true;
            if (!new String(header, 0, 4).equals("RIFF")) {
                // Headerless wave - no need to continues
                doContinue = false;
            } else if (!new String(header, 8, 4).equals("WAVE")) {
                // Error - don't continue
                doContinue = false;
            }

            if (doContinue) {
                wavHeader = new WavHeader();

                while (true) {
                    byte[] chunkHeader = new byte[8];
                    if (dis.read(chunkHeader) < 8)
                        throw new IOException("Unexpected EOF before data");

                    String chunkId = new String(chunkHeader, 0, 4, StandardCharsets.US_ASCII);
                    int chunkSize = ByteBuffer.wrap(chunkHeader, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();

                    if (chunkId.equals("fmt ")) {
                        byte[] fmtData = new byte[chunkSize];
                        dis.readFully(fmtData);

                        ByteBuffer bb = ByteBuffer.wrap(fmtData).order(ByteOrder.LITTLE_ENDIAN);
                        bb.getShort(); // audioFormat
                        wavHeader.channels = bb.getShort();
                        wavHeader.sampleRate = bb.getInt();
                        bb.getInt(); // byteRate
                        bb.getShort(); // blockAlign
                        wavHeader.bitsPerSample = bb.getShort();
                        wavHeader.bytesPerSample = wavHeader.bitsPerSample / 8;
                    } else if (chunkId.equals("data")) {
                        wavHeader.dataStartOffset = dis.available();
                        // Note: We'll use this offset later to manually skip
                        return wavHeader;
                    } else {
                        // Skip unknown chunk
                        long skipped = dis.skip(chunkSize);
                        while (skipped < chunkSize) {
                            long more = dis.skip(chunkSize - skipped);
                            if (more <= 0) break;
                            skipped += more;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Couldn't check the header of the file");
        }
        return wavHeader;
    }

    private void finishScheduledExecutor() {
        if (future != null) {
            future.cancel(true);
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}

package com.garethevans.church.opensongtablet.multitrack;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.InputStream;

public class AudioInputTrack {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "AudioInputTrack";
    private String trackName;
    private Uri trackUri;
    private InputStream trackStream;
    private int trackChannels;
    private short[] trackBuffer;
    private int trackBufferLength = 0;
    private float trackVolume = 1f;
    private float trackPan = 0f; // -1.0 = full left, 1.0 = full right
    private boolean trackMute, trackSolo;
    private byte[] readBytes;
    private final MainActivityInterface mainActivityInterface;
    private boolean streamClosed = false;

    public AudioInputTrack(Context c, String trackName, Uri trackUri, InputStream trackStream, int trackChannels,
                           int trackVolume, String trackPan, boolean trackMute, boolean trackSolo) {
        mainActivityInterface = (MainActivityInterface) c;
        setTrackName(trackName);
        setTrackUri(trackUri);
        setTrackStream(trackStream);
        setTrackChannels(trackChannels);
        updateTrackBuffer();
        updateReadBytes();
        setTrackPanString(trackPan);
        setTrackVolumeInt(trackVolume);
        setTrackMute(trackMute);
        setTrackSolo(trackSolo);
    }

    public int readNext() {
        if (trackStream!=null && !streamClosed) {
            try {
                int bytesRead = trackStream.read(readBytes); // readBytes is a byte[]
                if (bytesRead <= 0) return 0; // or -1 if EOF

                int samples = bytesRead / 2; // 2 bytes per sample (16-bit PCM)

                trackBufferLength = samples;

                for (int i = 0; i < samples; i++) {
                    int lo = readBytes[2 * i] & 0xFF;
                    int hi = readBytes[2 * i + 1]; // sign-extended
                    trackBuffer[i] = (short) ((hi << 8) | lo);
                }

                return samples;
            } catch (Exception e) {
                Log.d(TAG,trackName+": This stream was closed");
                streamClosed = true;
                return 0;
            }
        } else {
            Log.d(TAG,trackName+": trackStream is null/closed");
            streamClosed = true;
            return 0;
        }
    }


    // The trackName
    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }
    public String getTrackName() {
        return trackName;
    }

    // The trackUri
    public void setTrackUri(Uri trackUri) {
        this.trackUri = trackUri;
    }
    public Uri getTrackUri() {
        return trackUri;
    }

    // The trackStream
    public void setTrackStream(InputStream trackStream) {
        this.trackStream = trackStream;
        streamClosed = false;
    }
    public InputStream getTrackStream() {
        return trackStream;
    }

    // The volume is a float between 0 and 1, but we can set it using an int 0-100
    public void setTrackVolumeInt(int trackVolume) {
        this.trackVolume = (float)trackVolume/100f;
    }
    public float getTrackVolume() {
        return trackVolume;
    }
    public int getTrackVolumeInt() {
        return (int) (trackVolume*100f);
    }

    // The pan is a float between -1 and 1, but we can set it using a string "L", "R", or "C"
    public void setTrackPanString(String trackPan) {
        switch (trackPan) {
            case "L":
                this.trackPan = -1f;
                break;
            case "R":
                this.trackPan = 1f;
                break;
            case "C":
            default:
                this.trackPan = 0f;
                break;
        }
    }
    public float getTrackPan() {
        return trackPan;
    }
    public String getTrackPanString() {
        if (trackPan == -1f) {
            return "L";
        } else if (trackPan == 1f) {
            return "R";
        } else {
            return "C";
        }
    }

    // The mute values
    public void setTrackMute(boolean trackMute) {
        this.trackMute = trackMute;
    }
    public boolean getTrackMute() {
        return trackMute;
    }

    // The solo values
    public void setTrackSolo(boolean trackSolo) {
        this.trackSolo = trackSolo;
    }
    public boolean getTrackSolo() {
        return trackSolo;
    }

    // Get channels and buffers
    public void setTrackChannels(int trackChannels) {
        this.trackChannels = trackChannels;
    }
    public int getTrackChannels() {
        return trackChannels;
    }

    public void updateTrackBuffer() {
        trackBuffer = new short[1024 * trackChannels];
        updateReadBytes();
    }
    public short[] getTrackBuffer() {
        return trackBuffer;
    }

    public int getTrackBufferLength() {
        return trackBufferLength;
    }

    public void updateReadBytes() {
        readBytes = new byte[trackBuffer.length * 2];
    }

    // Close the stream
    public void close() {
        streamClosed = true;
        if (trackStream!=null) {
            try {
                trackStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void reopen() {
        close();
        if (trackUri!=null) {
            try {
                trackStream = mainActivityInterface.getStorageAccess().getInputStream(trackUri);
                streamClosed = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void skip(long bytesToSkip) {
        try {
            long skipped = 0;
            while (skipped < bytesToSkip) {
                long actual = trackStream.skip(bytesToSkip - skipped);
                if (actual <= 0) break; // prevent infinite loop
                skipped += actual;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package com.garethevans.church.opensongtablet.multitrack;

import android.media.AudioTrack;

public class AudioTrackPositionTracker {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "AudioTrackPosition";
    private AudioTrack audioTrack;
    private int sampleRate;
    private int frameSize;
    private int channels;
    private long seekOffsetMs = 0;
    private int bytesPerSample;
    private boolean audioInfoSetForSong = false;

    public AudioTrackPositionTracker(AudioTrack audioTrack, int sampleRate, int channels, int bytesPerSample) {
        this.audioTrack = audioTrack;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.bytesPerSample = bytesPerSample;
        this.frameSize = channels * bytesPerSample;
    }

    public void setAudioTrack(AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
    }

    public boolean getAudioInfoSetForSong() {
        return audioInfoSetForSong;
    }
    public void setAudioInfoSetForSong(boolean audioInfoSetForSong) {
        this.audioInfoSetForSong = audioInfoSetForSong;
    }

    public void setSeekOffsetMs(long ms) {
        this.seekOffsetMs = ms;
    }

    public long getSeekOffsetMs() {
        return seekOffsetMs;
    }

    public long getPlaybackPositionMs() {
        int frames = 0;
        if (audioTrack!=null) {
            frames = audioTrack.getPlaybackHeadPosition();
        }
        return seekOffsetMs + ((frames * 1000L) / sampleRate);
    }

    public long getSkipBytes() {
        if (frameSize==0) {
            updateFrameSize();
        }
        return (long)((sampleRate * seekOffsetMs / 1000.0) * frameSize);
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        updateFrameSize();
    }
    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }
    private void updateFrameSize() {
        frameSize = channels * bytesPerSample;
    }

    public void setBytesPerSample(int bytesPerSample) {
        this.bytesPerSample = bytesPerSample;
    }

    public void setChannels(int channels) {
        this.channels = channels;
        updateFrameSize();
    }
}
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
    private int bytesPerChannel;
    private boolean audioInfoSetForSong = false;

    public AudioTrackPositionTracker(AudioTrack audioTrack, int sampleRate, int channels, int bytesPerChannel) {
        this.audioTrack = audioTrack;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.bytesPerChannel = bytesPerChannel;
        this.frameSize = channels * bytesPerChannel;
    }

    public void setAudioTrack(AudioTrack audioTrack) {
        this.audioTrack = audioTrack;
        audioTrack.setPlaybackHeadPosition(0);
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
        frameSize = channels * bytesPerChannel;
    }

    public void setBytesPerChannel(int bytesPerChannel) {
        this.bytesPerChannel = bytesPerChannel;
    }

    public void setChannels(int channels) {
        this.channels = channels;
        updateFrameSize();
    }
    public int getChannels() {
        return channels;
    }

    public int getSampleRate() {
        return sampleRate;
    }
}
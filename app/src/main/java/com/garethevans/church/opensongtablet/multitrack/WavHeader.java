package com.garethevans.church.opensongtablet.multitrack;

public class WavHeader {
    public int sampleRate;
    public int numChannels;
    public int bitsPerChannel;
    public int bytesPerChannel;
    public int byteRate;
    public int blockAlign;
    public int dataStartOffset;
    public int dataSize;
    public int audioFormat;

    public int getDurationMs() {
        if (byteRate==0 || dataSize==0) {
            return 0;
        } else {
            return (int) ((dataSize * 1000L) / byteRate);
        }
    }
}

package com.garethevans.church.opensongtablet.drummer;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class DrumPlayer {

    // Sample attributes
    private final int NUM_PLAY_CHANNELS = 2;    // The number of channels in the player Stream.
                                                // Stereo Playback, set to 1 for Mono playback

    // Sample Buffer IDs
    private final int BASSDRUM = 0;
    private final int SNAREDRUM = 1;
    private final int CRASHCYMBAL = 2;
    private final int RIDECYMBAL = 3;
    private final int MIDTOM = 4;
    private final int LOWTOM = 5;
    private final int HIHATOPEN = 6;
    private final int HIHATCLOSED = 7;

    // initial pan position for each drum sample
    private final float PAN_BASSDRUM = 0f;         // Dead Center
    private final float PAN_SNAREDRUM = 0.25f;     // A little Right
    private final float PAN_CRASHCYMBAL = -0.75f;  // Mostly Left
    private final float PAN_RIDECYMBAL = 1.0f;     // Hard Right
    private final float PAN_MIDTOM = -0.75f;       // Mostly Left
    private final float PAN_LOWTOM = 0.75f;        // Mostly Right
    private final float PAN_HIHATOPEN = -1.0f;     // Hard Left
    private final float PAN_HIHATCLOSED = -1.0f;   // Hard Left

    // Logging Tag
    private final String TAG = "DrumPlayer";

    public void setupAudioStream() {
        setupAudioStreamNative(NUM_PLAY_CHANNELS);
    }

    public void startAudioStream() {
        startAudioStreamNative();
    }

    public void teardownAudioStream() {
        teardownAudioStreamNative();
    }

    // asset-based samples
    public void loadWavAssets(AssetManager assetMgr) {
        loadWavAsset(assetMgr, "KickDrum.wav", BASSDRUM, PAN_BASSDRUM);
        loadWavAsset(assetMgr, "SnareDrum.wav", SNAREDRUM, PAN_SNAREDRUM);
        loadWavAsset(assetMgr, "CrashCymbal.wav", CRASHCYMBAL, PAN_CRASHCYMBAL);
        loadWavAsset(assetMgr, "RideCymbal.wav", RIDECYMBAL, PAN_RIDECYMBAL);
        loadWavAsset(assetMgr, "MidTom.wav", MIDTOM, PAN_MIDTOM);
        loadWavAsset(assetMgr, "LowTom.wav", LOWTOM, PAN_LOWTOM);
        loadWavAsset(assetMgr, "HiHat_Open.wav", HIHATOPEN, PAN_HIHATOPEN);
        loadWavAsset(assetMgr, "HiHat_Closed.wav", HIHATCLOSED, PAN_HIHATCLOSED);
    }

    public void unloadWavAssets() {
        unloadWavAssetsNative();
    }

    private void loadWavAsset(AssetManager assetMgr, String assetName, int index, float pan) {
        try {
            AssetFileDescriptor assetFD = assetMgr.openFd(assetName);
            InputStream dataStream = assetFD.createInputStream();
            int dataLen = (int)assetFD.getLength();
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(dataLen);
            byte[] dataBytes = bb.array();
            dataStream.read(dataBytes, 0, dataLen);
            loadWavAssetNative(dataBytes, index, pan);
            assetFD.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private native void setupAudioStreamNative(int numChannels);
    private native void startAudioStreamNative();
    private native void teardownAudioStreamNative();

    private native void loadWavAssetNative(byte[] wavBytes, int index, float pan);
    private native void unloadWavAssetsNative();

    native void trigger(int drumIndex);
    native void stopTrigger(int drumIndex);

    native float setPan(int index, float pan);
    native float getPan(int index);

    native void setGain(int index, float gain);
    native float getGain(int index);

    native boolean getOutputReset();
    native void clearOutputReset();
    native void restartStream();


}

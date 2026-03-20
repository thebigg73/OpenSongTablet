package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.webkit.internal.ApiFeature;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DrumSoundManager {
    private SoundPool soundPool;
    private int tickId = -1;
    private int tockId = -1;
    private final String TAG = "DrumSoundManager";
    private final MainActivityInterface mainActivityInterface;
    private Map<String, Integer> soundMap;

    private boolean allLoaded = false;
    private int soundsLoadedCount = 0;
    private int soundsToLoad = 0;
    private String sampleRateAsset = "_48";
    private int openHatStreamId = 0; // Add this to track the active stream

    private DrumKit drumKit;
    private DrumKit cajonKit;

    private final String drumKickVol = "drumKickVol", drumSnareVol = "drumSnareVol", drumRimShotVol = "drumRimShotVol",
            drumStickVol = "drumStickVol", drumHatClosedVol = "drumHatClosedVol", drumHatOpenVol = "drumHatOpenVol",
            drumTomLoVol = "drumTomLoVol", drumTomMidVol = "drumTomMidVol", drumTomHiVol = "drumTomHiVol",
            drumCrashVol = "drumCrashVol", drumRideVol = "drumRideVol", drumRideBellVol = "drumRideBellVol",
            drumSplashVol = "drumSplashVol";

    // Initialise the DrumSoundManager and get a reference for MainActivityInterface
    public DrumSoundManager(Context context) {
        mainActivityInterface = (MainActivityInterface) context;
        determineSampleRate(context);
        buildKits(context);
        initialiseDrumSounds(context);
    }

    public void determineSampleRate(Context context) {
        AudioManager myAudioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String sampleRateStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        int defaultSampleRate = Integer.parseInt(sampleRateStr);
        String framesPerBurstStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        int defaultFramesPerBurst = Integer.parseInt(framesPerBurstStr);
        if (defaultSampleRate==48000) {
            sampleRateAsset = "_48";
        } else {
            sampleRateAsset = "_44";
        }
    }

    public void initialiseDrumSounds(Context context) {
        // Inside initialiseDrumSounds or the constructor
        AudioAttributes audioAttributes = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME) // GAME or ASSISTANCE_SONIFICATION
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // CRITICAL for low latency
                    .setFlags(AudioAttributes.FLAG_LOW_LATENCY) // Hints to the system to prioritize speed
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(32)
                    .setAudioAttributes(audioAttributes)
                    .build();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME) // GAME or ASSISTANCE_SONIFICATION
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) // CRITICAL for low latency
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(32)
                    .setAudioAttributes(audioAttributes)
                    .build();

        } else {
            soundPool = new SoundPool(32, AudioManager.STREAM_MUSIC, 0);
        }


        setupLoadListener();

        // Start loading

        soundMap = new HashMap<>();
        soundsLoadedCount = 0;
        soundsToLoad = 0;
        loadDrumSamples(context);
        loadMetronomeSounds(context, mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeTickSound()+sampleRateAsset+".wav",
                mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeTockSound()+sampleRateAsset+".wav");
    }

    private void setupLoadListener() {
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                soundsLoadedCount++;
                if (soundsLoadedCount >= soundsToLoad) {
                    allLoaded = true;
                    Log.d(TAG, "All drum sounds ready to play.");
                }
            } else {
                Log.e(TAG, "Failed to load sample ID: " + sampleId + " Status: " + status);
            }
        });
    }

    private void loadDrumSamples(Context context) {
        DrumKit kit;

        if (mainActivityInterface.getDrumViewModel().getDrummer().getDrummerStyle().equals("Cajon") ||
            mainActivityInterface.getDrumViewModel().getDrummer().getDrummerStyle().equals("Percussion")) {
            kit = cajonKit;
        } else {
            kit = drumKit;
        }

        AssetManager am = context.getAssets();

        // Get a list of all the unique files
        String[] drummerFiles = null;

        // Load in the standard kit
        for (int i = 0; i < kit.getDrumParts().size(); i++) {
            try (AssetFileDescriptor afd = am.openFd("drummer/" + drumKit.getDrumParts().get(i).getPartFileName())) {
                int id = soundPool.load(afd, 1);
                soundMap.put(drumKit.getDrumParts().get(i).getPartName(), id);
                soundsToLoad ++;
            } catch (IOException e) {
                Log.e(TAG, "Error loading drum sample: " + kit.getDrumParts().get(i).getPartFileName(), e);
            }
        }

        // Load in the cajon kit - will only load in the parts that aren't already in the main kit
        for (int i = 0; i < kit.getDrumParts().size(); i++) {
            try (AssetFileDescriptor afd = am.openFd("drummer/" + cajonKit.getDrumParts().get(i).getPartFileName())) {
                int id = soundPool.load(afd, 1);
                soundMap.put(cajonKit.getDrumParts().get(i).getPartName(), id);
                soundsToLoad ++;
            } catch (IOException e) {
                Log.e(TAG, "Cajon kit didn't have extra file for: " + cajonKit.getDrumParts().get(i).getPartFileName(), e);
            }
        }
    }

    public void loadMetronomeSounds(Context context, String tickFile, String tockFile) {
        AssetManager am = context.getAssets();
        try {
            if (tickId != -1) soundPool.unload(tickId);
            if (tockId != -1) soundPool.unload(tockId);

            AssetFileDescriptor afdTick = am.openFd("metronome/" + tickFile);
            AssetFileDescriptor afdTock = am.openFd("metronome/" + tockFile);

            tickId = soundPool.load(afdTick, 1);
            tockId = soundPool.load(afdTock, 1);

            soundsToLoad = soundsToLoad + 2;
            afdTick.close();
            afdTock.close();
        } catch (IOException e) {
            Log.e(TAG, "Error loading metronome sounds", e);
        }
    }

    public void updateMetronomeSounds(Context c) {
        soundsToLoad = soundsToLoad - 2;
        loadMetronomeSounds(c,
                mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeTickSound()+sampleRateAsset+".wav",
                mainActivityInterface.getDrumViewModel().getMetronome().getMetronomeTockSound()+sampleRateAsset+".wav");
    }

    public void playMetronome(boolean isAccent, float tickLeftVol, float tickRightVol, float tockLeftVol, float tockRightVol) {
        int id = isAccent ? tickId : tockId;
        float leftVol = isAccent ? tickLeftVol : tockLeftVol;
        float rightVol = isAccent ? tickRightVol : tockRightVol;
        // Priority 3 for metronome to ensure it's heard over drums
        if (id != -1 && allLoaded) {
            soundPool.play(id, leftVol, rightVol, 3, 0, 1.0f);
        }
    }

    public void playDrum(String name, float volume) {
        float newVolume = volume / 127f; // Convert 0-127 to 0.0-1.0

        if (!allLoaded || !soundMap.containsKey(name)) return;

        Integer soundId = soundMap.get(name);
        if (soundId == null) {
            Log.e("SOUND_ERROR", "Sound name not found in map: " + name);
            return;
        }
        int priority = (name.contains("Kick") || name.contains("Snare")) ? 2 : 1;
        int currentStreamId = soundPool.play(soundId, newVolume, newVolume, priority, 0, 1.0f);

        // --- CORRECT CHOKE LOGIC ---
        if (name.equals("HatClosed") || name.equals("HatPedal")) {
            // Stop the specific stream that is currently playing the open hat
            if (openHatStreamId != 0) {
                soundPool.stop(openHatStreamId);
                openHatStreamId = 0; // Reset after stopping
            }
        } else if (name.equals("HatOpen")) {
            // Store the stream ID so we can "choke" it later
            openHatStreamId = currentStreamId;
        }
    }

    public void release() {
        if (soundPool!=null) {
            soundPool.release();
        }
    }

    private void buildKits(Context c) {
        if (c!=null) {
            // The standard drum kit
            drumKit = new DrumKit(c.getString(R.string.drum_kit_acoustic));
            ArrayList<DrumPart> drumKitParts = new ArrayList<>();
            drumKitParts.add(buildDrumPart(c,"Kick",R.string.drum_kick,drumKickVol,35));
            drumKitParts.add(buildDrumPart(c,"Snare",R.string.drum_snare,drumSnareVol,38));
            drumKitParts.add(buildDrumPart(c,"RimShot",R.string.drum_rim_shot,drumRimShotVol,37));
            drumKitParts.add(buildDrumPart(c,"Stick",R.string.drum_stick,drumStickVol,40));
            drumKitParts.add(buildDrumPart(c,"HatClosed",R.string.drum_hat_closed,drumHatClosedVol,42));
            drumKitParts.add(buildDrumPart(c,"HatOpen",R.string.drum_hat_open,drumHatOpenVol,46));
            drumKitParts.add(buildDrumPart(c,"TomLo",R.string.drum_tom_lo,drumTomLoVol,45));
            drumKitParts.add(buildDrumPart(c,"TomMid",R.string.drum_tom_mid,drumTomMidVol,47));
            drumKitParts.add(buildDrumPart(c,"TomHi",R.string.drum_tom_hi,drumTomHiVol,50));
            drumKitParts.add(buildDrumPart(c,"Crash",R.string.drum_crash,drumCrashVol,49));
            drumKitParts.add(buildDrumPart(c,"Splash",R.string.drum_splash,drumSplashVol,55));
            drumKitParts.add(buildDrumPart(c,"Ride",R.string.drum_ride,drumRideVol,51));
            drumKitParts.add(buildDrumPart(c,"RideBell",R.string.drum_ride_bell,drumRideBellVol,53));
            drumKit.setDrumParts(drumKitParts);

            // The cajon kit
            cajonKit = new DrumKit(c.getString(R.string.drum_kit_percussion));
            ArrayList<DrumPart> cajonKitParts = new ArrayList<>();
            cajonKitParts.add(buildDrumPart(c,"Cajon_Kick",R.string.drum_kick,drumKickVol,35));
            cajonKitParts.add(buildDrumPart(c,"Cajon_Snare",R.string.drum_snare,drumSnareVol,38));
            cajonKitParts.add(buildDrumPart(c,"Cajon_RimShot",R.string.drum_rim_shot,drumRimShotVol,37));
            cajonKitParts.add(buildDrumPart(c,"Cajon_Stick",R.string.drum_stick,drumStickVol,40));
            cajonKitParts.add(buildDrumPart(c,"Cajon_HatClosed",R.string.drum_hat_closed,drumHatClosedVol,42));
            cajonKitParts.add(buildDrumPart(c,"Cajon_HatOpen",R.string.drum_hat_open,drumHatOpenVol,46));
            cajonKitParts.add(buildDrumPart(c,"Cajon_TomLo",R.string.drum_tom_lo,drumTomLoVol,45));
            cajonKitParts.add(buildDrumPart(c,"Cajon_TomMid",R.string.drum_tom_mid,drumTomMidVol,47));
            cajonKitParts.add(buildDrumPart(c,"Cajon_TomHi",R.string.drum_tom_hi,drumTomHiVol,50));
            cajonKitParts.add(buildDrumPart(c,"Cajon_Crash",R.string.drum_crash,drumCrashVol,49));
            cajonKitParts.add(buildDrumPart(c,"Cajon_Splash",R.string.drum_splash,drumSplashVol,55));
            cajonKitParts.add(buildDrumPart(c,"Cajon_Ride",R.string.drum_ride,drumRideVol,51));
            cajonKitParts.add(buildDrumPart(c,"Cajon_RideBell",R.string.drum_ride_bell,drumRideBellVol,53));
            cajonKit.setDrumParts(cajonKitParts);
        }
    }

    private DrumPart buildDrumPart(Context c, String partName, int stringId, String prefVolName, int partMidi) {
        return new DrumPart(partName,
                c.getString(stringId),
                partName.toLowerCase() + sampleRateAsset + ".wav",
                prefVolName,
                mainActivityInterface.getPreferences().getMyPreferenceFloat(prefVolName, 1.0f),
                partMidi);
    }

    public DrumKit getKit() {
        return drumKit;
    }

}
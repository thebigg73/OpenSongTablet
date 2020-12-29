package com.garethevans.church.opensongtablet.metronome;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.ActionBar;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public class Metronome {

    private int bpm, noteValue, beat;
    private int silence, tempo, beats;
    private float metrovol;
    private String whichbeat;
    int minBpm = 40, maxBpm = 199;

    private boolean metronomeOn, clickedOnMetronomeStart;
    
    private double beatSound, sound;
    private boolean play = true;

    private final AudioGenerator audioGenerator = new AudioGenerator(8000);
    private double[] soundTickArray, soundTockArray, silenceSoundArray;
    private int currentBeat = 1;
    private int runningBeatCount;
    private int maxBeatCount;

    // Variables for metronome to work
    // Keeping them as public/static to allow them to be accessed without the dialogfragment

    Context c;
    ActionBar ab;

    MetronomeAsyncTask metroTask;
    VisualMetronomeAsyncTask visualMetronome;

    public Metronome(Context c, ActionBar ab) {
        this.c = c;
        this.ab = ab;
    }

    private Metronome(String pan, float vol) {
        audioGenerator.createPlayer(pan,vol);
    }

    private void calcSilence(Song song) {

        //TEST
        beat = getBeat();
        noteValue = getNoteValue();
        bpm = getBpm(song);

        if (noteValue<1) {
            noteValue=4;
        }
        if (bpm<1) {
            bpm=120;
        }

        // 3/4 3 beats per bar, each quarter notes
        // 3/8 3 beats per bar, each eigth notes = tempo is twice as fast

        // Tempos that I have
        // 2/2   2/4   3/2    3/4   3/8   4/4   5/4   5/8   6/4   6/8   7/4   7/8   1/4
        // First number is the number of beats and is the easiest bit
        // The second number is the length of each beat
        // 2 = half notes, 4 = quarter notes, 8 = eigth notes

        if (beat==6 || beat==9) {
            noteValue = (short)(noteValue/(beat/3));
        } else if (beat==5 || beat==7) {
            noteValue = (short)(noteValue/2);
        }

        int resolutionmeter = (int) (8.0f / (float) noteValue);

        if (resolutionmeter ==0) {
            resolutionmeter =1;
        }

        int tick1 = 600;
        try {
            silence = ((60/bpm)*(4000* resolutionmeter)- tick1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        soundTickArray = new double[tick1];
        soundTockArray = new double[tick1];
        if (silence>10000) {
            silence = 10000;
        }
        silenceSoundArray = new double[this.silence];
        double[] tick;
        double[] tock;
        if (song.getTimesig().equals("1/4")) {
            tick = audioGenerator.getSineWave(tick1, 8000/ resolutionmeter, beatSound/ resolutionmeter);
            tock = audioGenerator.getSineWave(tick1, 8000/ resolutionmeter, beatSound/ resolutionmeter);

        } else {
            tick = audioGenerator.getSineWave(tick1, 8000/ resolutionmeter, beatSound/ resolutionmeter);
            tock = audioGenerator.getSineWave(tick1, 8000/ resolutionmeter, sound/ resolutionmeter);
        }
        for(int i = 0; i< tick1; i++) {
            soundTickArray[i] = tick[i];
            soundTockArray[i] = tock[i];
        }
        for(int i=0;i<silence;i++)
            silenceSoundArray[i] = 0;
    }

    private void play(Song song, String pan, float vol) {
        calcSilence(song);
        do {
            if(currentBeat == 1) {
                audioGenerator.writeSound(pan,vol,soundTockArray,metronomeOn);
            } else {
                audioGenerator.writeSound(pan,vol,soundTickArray,metronomeOn);
            }
            audioGenerator.writeSound(pan,vol,silenceSoundArray,metronomeOn);

            currentBeat++;
            runningBeatCount++;
            if (maxBeatCount>0 && runningBeatCount>=maxBeatCount) { // This is if the user has specified max metronome time
                play=false;
                metronomeOn = false;
                // IV - This variable is a state indicator set in this function only
                clickedOnMetronomeStart = false;
            }
            if(currentBeat > beat)
                currentBeat = 1;
        } while(play);
    }

    private void stop() {
        play = false;
        audioGenerator.destroyAudioTrack();
    }



    // The getters and setters
    public boolean getClickedOnMetronomeStart() {
        return clickedOnMetronomeStart;
    }
    public void setClickedOnMetronomeStart(boolean clickedOnMetronomeStart) {
        this.clickedOnMetronomeStart = clickedOnMetronomeStart;
    }

    private int getBpm(Song song) {
        return processTempo(song);
    }
    private void setBpm(int bpm) {
        this.bpm = bpm;
    }
    private int processTempo(Song song) {
        String t = song.getMetronomebpm();
        // Check for text version from desktop app
        t = t.replace("Very Fast", "140");
        t = t.replace("Fast", "120");
        t = t.replace("Moderate", "100");
        t = t.replace("Slow", "80");
        t = t.replace("Very Slow", "60");
        t = t.replaceAll("[\\D]", "");
        try {
            bpm = (short) Integer.parseInt(t);
        } catch (NumberFormatException nfe) {
            bpm = 0;
        }

        if (bpm<40 || bpm>299) {
            bpm = 260;  // These are the 'not set' values
        } else {
            tempo = bpm;
        }

        return bpm;
    }

    private int getNoteValue() {
        return noteValue;
    }
    private void setNoteValue(int noteValue) {
        this.noteValue = noteValue;
    }

    private int getBeat() {
        return beat;
    }
    private void setBeat(int beat) {
        this.beat = beat;
    }

    private void setBeatSound(double beatSound) {
        this.beatSound = beatSound;
    }
    private void setSound(double sound) {
        this.sound = sound;
    }

    private void setVolume(float metrovol) {
        this.metrovol = metrovol;
    }
    public float getVolume () {
        return metrovol;
    }

    private void setCurrentBeat(int currentBeat) {
        this.currentBeat = currentBeat;
    }
    private int getCurrentBeat() {
        return currentBeat;
    }



    void setBeatValues(Song song) {
        String bpmString = song.getMetronomebpm();
        short r = 0;
        if (bpmString.startsWith("1/")) {
            r = 4;
        } else if (bpmString.startsWith("2/")) {
            r = 2;
        } else if (bpmString.startsWith("3/")) {
            r = 3;
        } else if (bpmString.startsWith("4/")) {
            r = 4;
        } else if (bpmString.startsWith("5/")) {
            r = 5;
        } else if (bpmString.startsWith("6/")) {
            r = 6;
        } else if (bpmString.startsWith("7/")) {
            r = 7;
        }
        beats = r;
    }

    void setNoteValues(Song song) {
        short r = 0;
        String bpmString = song.getMetronomebpm();
        if (bpmString.endsWith("/2")) {
            r = 2;
        } else if (bpmString.endsWith("/4")) {
            r = 4;
        } else if (bpmString.endsWith("/8")) {
            r = 2; //8
        }
        noteValue = r;
    }

    public void startstopMetronome(Context c, Song song, boolean showvisual, int metronomeColor, String pan, float vol, int barlength) {
        if (checkMetronomeValid(c,song) && !metronomeOn) {
            // Start the metronome
            metronomeOn = true;
            whichbeat = "b";
            // This is a state indicator set in this function only
            clickedOnMetronomeStart = true;
            metroTask = new MetronomeAsyncTask(song,pan,vol,barlength,bpm,noteValue,beats,maxBeatCount,beatSound,sound,minBpm,maxBpm);
            try {
                metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (Exception e) {
                Log.d("d","Error starting the metronome");
            }
            startstopVisualMetronome(showvisual,metronomeColor);
            // IV - A stop perhaps does not need to consider if it is valid
        } else if (metronomeOn) {
            // Stop the metronome
            metronomeOn = false;
            // This a state indicator set in this function only
            clickedOnMetronomeStart = false;
            if (metroTask!=null) {
                metroTask.stop();
            }
            // IV - Do not go to setting page as metronome 'not set' may be valid
        }
    }

    private boolean checkMetronomeValid(Context c, Song song) {
        boolean validTimeSig = false;
        boolean validBPM = true;
        boolean validMetro = false;

        if (getBpm(song)==260) {
            validBPM = false;
        }

        String[] arrayvals = c.getResources().getStringArray(R.array.time_signatures);

        for (String arrayval : arrayvals) {
            if (song.getMetronomebpm().equals(arrayval)) {
                validTimeSig = true;
                break;
            }
        }

        if (validBPM && validTimeSig) {
            validMetro = true;
        }

        return validMetro;
    }

    void startstopVisualMetronome(boolean showvisual, int metronomeColor) {
        visualMetronome = new VisualMetronomeAsyncTask(ab,showvisual, metronomeColor,whichbeat,metronomeOn,bpm,noteValue);
        try {
            visualMetronome.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Error starting visual metronome");
        }
    }
    private static class VisualMetronomeAsyncTask extends AsyncTask<Void, Integer, String> {

        int noteValue;
        long time_in_millisecs;
        long oldtime;
        long nexttime;
        boolean showvisual, metronomeOn;
        int metronomeColor;
        ActionBar ab;
        String whichbeat;

        VisualMetronomeAsyncTask(ActionBar ab, boolean showvis, int metronomeColor, String whichbeat, boolean metronomeOn, int bpm, int noteValue) {
            this.metronomeColor = metronomeColor;
            this.showvisual = showvis;
            this.ab = ab;
            this.whichbeat = whichbeat;
            this.metronomeOn = metronomeOn;
            this.noteValue = noteValue;
            time_in_millisecs = (long) (((60.0f / (float) bpm) * (4.0f / (float) noteValue))* 1000);
            oldtime = System.currentTimeMillis();
            nexttime = oldtime + time_in_millisecs;
        }

        VisualMetronomeAsyncTask(boolean metronomeOn) {
            this.metronomeOn = metronomeOn;
        }


        @Override
        protected String doInBackground(Void... voids) {
            publishProgress(1);
            while (metronomeOn) {
                // Post this activity based on the bpm
                if (System.currentTimeMillis() >= nexttime) {
                    oldtime = nexttime;
                    nexttime = oldtime + time_in_millisecs;
                    publishProgress(1);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... integers) {
            if (showvisual) {
                if (whichbeat.equals("a")) {
                    whichbeat = "b";
                    if (ab != null) {
                        ab.setBackgroundDrawable(new ColorDrawable(0xff232333));
                    }
                } else {
                    StaticVariables.whichbeat = "a";
                    if (ab != null) {
                        ab.setBackgroundDrawable(new ColorDrawable(metronomeColor));
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (ab != null) {
                ab.setBackgroundDrawable(new ColorDrawable(0xff232333));
            }
        }
    }

    static class MetronomeAsyncTask extends AsyncTask<Void, Void, String> {

        Song song;
        Metronome metronome;
        final String pan;
        final float vol;
        int barsrequired;
        int maxBeatCount;
        int beats;
        int bpm;
        int noteValue;
        int minBpm, maxBpm;
        double beatSound, sound;
        int beat;

        MetronomeAsyncTask(Song song, String pan, float vol, int barlength, int bpm, int noteValue, int beats, int maxBeatCount, double beatSound, double sound, int minBpm, int maxBpm) {
            this.song = song;
            metronome = new Metronome(pan,vol);
            this.pan = pan;
            this.vol = vol;
            barsrequired = barlength;
            this.maxBeatCount = maxBeatCount;
            this.beats = beats;
            this.bpm = bpm;
            this.minBpm = minBpm;
            this.maxBpm = maxBpm;
            this.noteValue = noteValue;
            this.beatSound = beatSound;
            this.sound = sound;
        }

        @Override
        protected void onPreExecute() {
            // Figure out how many beats we should use
            maxBeatCount = beats * barsrequired;
        }

        void setNoteValue(int noteVal) {
            if (metronome != null && bpm >= minBpm && bpm <= maxBpm && noteVal > 0) {
                metronome.setNoteValue(noteVal);
                try {
                    metronome.calcSilence(song);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setBeat(int beat) {
            if (metronome != null) {
                metronome.setBeat(beat);
                try {
                    metronome.calcSilence(song);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setBpm(int bpm) {
            if (metronome != null && bpm >= minBpm && bpm <= maxBpm && noteValue > 0) {
                metronome.setBpm(bpm);
                try {
                    metronome.calcSilence(song);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setCurrentBeat(int currentBeat) {
            if (metronome != null) {
                metronome.setCurrentBeat(currentBeat);
                try {
                    metronome.calcSilence(song);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setBeatSound(double beatSound) {
            if (metronome != null) {
                metronome.setBeatSound(beatSound);
                try {
                    metronome.calcSilence(song);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setSound(double sound) {
            if (metronome != null) {
                metronome.setSound(sound);
                try {
                    metronome.calcSilence(song);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setVolume(float metrovol) {
            if (metronome != null)
                metronome.setVolume(metrovol);
        }

        @Override
        protected String doInBackground(Void... voids) {
            setBeat(beats);
            setNoteValue(noteValue);
            setBpm(bpm);
            setBeatSound(beatSound);
            setSound(sound);
            setVolume(vol);
            beat ++;
            if (beat>maxBeatCount) {
                beat = 1;
            }
            setCurrentBeat(beat);
            play(pan, vol);
            return null;
        }

        public void stop() {
            if (metronome != null) {
                metronome.stop();
                metronome = null;
            }
        }

        void play(String pan, float vol) {
            if (metronome != null) {
                metronome.play(song,pan,vol);
                metronome = null;
            }
        }

        @Override
        protected void onCancelled() {
            stop();
        }
    }

    public boolean isMetronomeValid(Song song) {
        int t = getBpm(song);
        return t>=minBpm && t<maxBpm && song.getTimesig()!=null && !song.getTimesig().isEmpty();
    }


}

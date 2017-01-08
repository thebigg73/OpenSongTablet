package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpMetronomeFragment extends DialogFragment {

    static PopUpMetronomeFragment newInstance() {
        PopUpMetronomeFragment frag;
        frag = new PopUpMetronomeFragment();
        return frag;
    }

    public interface MyInterface {
        void pageButtonAlpha(String s);
        void openFragment();
    }

    public static MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    //Spinner popupmetronome_timesig;
    NumberPicker bpm_numberPicker;
    NumberPicker timesig_numberPicker;
    TextView bpmtext;
    Button taptempo_Button;
    SeekBar popupmetronome_volume;
    TextView popupmetronome_volume_text;
    SeekBar popupmetronome_pan;
    TextView popupmetronome_pan_text;
    SwitchCompat visualmetronome;
    Button save_button;
    Button popupmetronome_startstopbutton;
    public static String[] bpmValues;
    int tempo;
    public static short bpm;
    public static String[] timesigvals;

    // Variables for tap tempo
    long new_time = 0;
    long time_passed = 0;
    long old_time = 0;
    int calc_bpm;
    int total_calc_bpm;
    int total_counts = 0;
    int av_bpm;

    // Variables for metronome to work
    // Keeping them as public/static to allow them to be accessed without the dialogfragment
    public static MetronomeAsyncTask metroTask;
    public static VisualMetronomeAsyncTask visualMetronome;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        if (getDialog()==null) {
            dismiss();
        }
        getDialog().setTitle(getActivity().getResources().getString(R.string.metronome));
        getDialog().setCanceledOnTouchOutside(true);
        mListener.pageButtonAlpha("metronome");

        View V = inflater.inflate(R.layout.popup_page_metronome, container, false);

        // Initialise the views
        bpm_numberPicker = (NumberPicker) V.findViewById(R.id.bpm_numberPicker);
        timesig_numberPicker = (NumberPicker) V.findViewById(R.id.timesig_numberPicker);
        bpmtext = (TextView) V.findViewById(R.id.bpmtext);
        taptempo_Button = (Button) V.findViewById(R.id.taptempo_Button);
        popupmetronome_volume = (SeekBar) V.findViewById(R.id.popupmetronome_volume);
        popupmetronome_volume_text = (TextView) V.findViewById(R.id.popupmetronome_volume_text);
        popupmetronome_pan = (SeekBar) V.findViewById(R.id.popupmetronome_pan);
        popupmetronome_pan_text = (TextView) V.findViewById(R.id.popupmetronome_pan_text);
        visualmetronome = (SwitchCompat) V.findViewById(R.id.visualmetronome);
        save_button = (Button) V.findViewById(R.id.save_button);
        popupmetronome_startstopbutton = (Button) V.findViewById(R.id.popupmetronome_startstopbutton);


        // Set up the default values
        if (FullscreenActivity.metronomeonoff.equals("on")) {
            popupmetronome_startstopbutton.setText(getResources().getString(R.string.stop));
        } else {
            popupmetronome_startstopbutton.setText(getResources().getString(R.string.start));
        }
        ProcessSong.processTimeSig();
        tempo = getTempo(FullscreenActivity.mTempo);
        setPan();
        popupmetronome_volume.setProgress(getVolume(FullscreenActivity.metronomevol));
        visualmetronome.setChecked(FullscreenActivity.visualmetronome);
        getTimeSigValues();
        getBPMValues();

        // Set the listeners for changes
        taptempo_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tapTempo();
            }
        });
        popupmetronome_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                FullscreenActivity.metronomevol = (float) i/100.0f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Preferences.savePreferences();
            }
        });
        popupmetronome_pan.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                switch (i) {
                    case 0:
                        FullscreenActivity.metronomepan = "L";
                        break;
                    case 1:
                        FullscreenActivity.metronomepan = "C";
                        break;
                    case 2:
                        FullscreenActivity.metronomepan = "R";
                        break;
                }
                Preferences.savePreferences();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        visualmetronome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.visualmetronome = b;
                Preferences.savePreferences();
            }
        });
        popupmetronome_startstopbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FullscreenActivity.metronomeonoff.equals("off")) {
                    Runtime.getRuntime().gc();
                    popupmetronome_startstopbutton.setText(getResources().getString(R.string.stop));
                    FullscreenActivity.metronomeonoff = "on";
                    FullscreenActivity.whichbeat = "b";
                    metroTask = new MetronomeAsyncTask();
                    metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    startstopVisualMetronome();
                } else {
                    Runtime.getRuntime().gc();
                    popupmetronome_startstopbutton.setText(getResources().getString(R.string.start));
                    FullscreenActivity.metronomeonoff = "off";
                    if (metroTask!=null) {
                        metroTask.stop();
                    }
                }
            }
        });
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopUpEditSongFragment.prepareSongXML();
                try {
                    PopUpEditSongFragment.justSaveSongXML();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Preferences.savePreferences();
                FullscreenActivity.myToastMessage = getResources().getString(R.string.edit_save) + " - " +
                        getResources().getString(R.string.ok);
                ShowToast.showToast(getActivity());
            }
        });
        return V;
    }

    @SuppressLint("SetTextI18n")
    public void tapTempo() {
        // This function checks the previous tap_tempo time and calculates the bpm
        new_time = System.currentTimeMillis();
        time_passed = new_time - old_time;
        calc_bpm = Math.round((1 / ((float) time_passed / 1000)) * 60);

        // Need to decide on the time sig.
        // If it ends in /2, then double the tempo
        // If it ends in /4, then leave as is
        // If it ends in /8, then half it
        // If it isn't set, set it to default as 4/4
        if (FullscreenActivity.mTimeSig.isEmpty()) {
            timesig_numberPicker.setValue(6);
        } else if (FullscreenActivity.mTimeSig.endsWith("/2")) {
            calc_bpm = (int) ((float) calc_bpm * 2.0f);
        } else if (FullscreenActivity.mTimeSig.endsWith("/8")) {
            calc_bpm = (int) ((float) calc_bpm / 2.0f);
        }

        if (time_passed < 2000) {
            total_calc_bpm += calc_bpm;
            total_counts++;
        } else {
            // Waited too long, reset count
            total_calc_bpm = 0;
            total_counts = 0;
        }

        av_bpm = Math.round((float) total_calc_bpm / (float) total_counts);

        if (av_bpm < 200 && av_bpm >= 40) {
            bpmtext.setText(getResources().getString(R.string.bpm));
            bpm_numberPicker.setValue(av_bpm-40);
            FullscreenActivity.mTempo = "" + av_bpm;
        } else if (av_bpm<40) {
            bpm_numberPicker.setValue(160);
            bpmtext.setText("<40 bpm");
        }  else if (av_bpm>199) {
            bpm_numberPicker.setValue(160);
            bpmtext.setText(">199 bpm");
        }

        bpm = (short) av_bpm;
        old_time = new_time;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
    }

    public static int getTempo(String t) {
        t = t.replace("Very Fast", "140");
        t = t.replace("Fast", "120");
        t = t.replace("Moderate", "100");
        t = t.replace("Slow", "80");
        t = t.replace("Very Slow", "60");
        t = t.replaceAll("[\\D]", "");
        try {
            bpm = (short) Integer.parseInt(t);
        } catch (NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
            bpm = 200; // This is the 'Not set' value
        }

        if (bpm<40 || bpm>199) {
            bpm = 160;
        }

        return (int) bpm;
    }

    public void setPan(){
        switch (FullscreenActivity.metronomepan) {
            case "L":
                popupmetronome_pan.setProgress(0);
                popupmetronome_pan_text.setText("L");
                break;
            case "C":
            default:
                popupmetronome_pan.setProgress(1);
                popupmetronome_pan_text.setText("C");
                break;
            case "R":
                popupmetronome_pan.setProgress(2);
                popupmetronome_pan_text.setText("R");
                break;
        }
    }

    public int getVolume(float v) {
        Log.d("d","v="+v);
        return (int) (v*100.0f);
    }

    public void getBPMValues() {
        bpmValues = new String[161];
        // Add the values 40 to 199 bpm
        for (int z=0;z<160;z++) {
            bpmValues[z] = "" + (z+40);
        }
        // Now add the 'Not set' value
        bpmValues[160] = getResources().getString(R.string.notset);

        bpm_numberPicker.setMinValue(0); //from array first value

        //Specify the maximum value/number of NumberPicker
        bpm_numberPicker.setMaxValue(bpmValues.length-1); //to array last value

        //Specify the NumberPicker data source as array elements
        bpm_numberPicker.setDisplayedValues(bpmValues);

        bpm_numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if (i1==160) {
                    // This is the not set value
                    tempo = 161;
                    bpm = 0;
                    FullscreenActivity.mTempo = "";
                } else {
                    FullscreenActivity.mTempo = "" + (i1+40);
                    bpm = (short) (i1+40);
                    Log.d("d","mTempo="+FullscreenActivity.mTempo);
                }
            }
        });
        Log.d("d","tempo="+tempo);
        bpm_numberPicker.setValue(tempo-40);
    }

    public void getTimeSigValues() {
        timesig_numberPicker.setMinValue(0);
        // Max value is 1 bigger as we have still to include the not set value
        String[] oldvals = getResources().getStringArray(R.array.timesig);
        timesigvals = new String[oldvals.length];
        System.arraycopy(oldvals, 0, timesigvals, 0, oldvals.length);
        timesigvals[0] = getResources().getString(R.string.notset);
        timesig_numberPicker.setMaxValue(timesigvals.length-1);
        timesig_numberPicker.setDisplayedValues(timesigvals);

        // Set the defaut value:
        int defpos = 0;

        for (int i=0;i<timesigvals.length-1;i++) {
            if (FullscreenActivity.mTimeSig.equals(timesigvals[i])) {
                defpos = i;
            }
        }
        timesig_numberPicker.setValue(defpos);

        timesig_numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if (i1 == 0) {
                    // First value, which is not set
                    FullscreenActivity.mTimeSig = "";
                    FullscreenActivity.beats = 0;
                    FullscreenActivity.noteValue = 0;
                } else {
                    FullscreenActivity.mTimeSig = timesigvals[i1];
                    setBeatValues();
                    setNoteValues();
                }
            }
        });
    }

    public static void setBeatValues() {
        short r = 0;
        if (FullscreenActivity.mTimeSig.startsWith("2/")) {
            r = 2;
        } else if (FullscreenActivity.mTimeSig.startsWith("3/")) {
            r = 3;
        } else if (FullscreenActivity.mTimeSig.startsWith("4/")) {
            r = 4;
        } else if (FullscreenActivity.mTimeSig.startsWith("5/")) {
            r = 5;
        } else if (FullscreenActivity.mTimeSig.startsWith("6/")) {
            r = 6;
        } else if (FullscreenActivity.mTimeSig.startsWith("7/")) {
            r = 7;
        }
        FullscreenActivity.beats = r;
    }

    public static void setNoteValues() {
        short r = 0;
        if (FullscreenActivity.mTimeSig.endsWith("/2")) {
            r = 2;
        } else if (FullscreenActivity.mTimeSig.endsWith("/4")) {
            r = 4;
        } else if (FullscreenActivity.mTimeSig.endsWith("/8")) {
            r = 8;
        }
        FullscreenActivity.noteValue = r;
    }

/*
    @SuppressWarnings("HandlerLeak")
    public static Handler getHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                */
/*if (FullscreenActivity.visualmetronome) {
                    if (FullscreenActivity.whichbeat.equals("a")) {
                        FullscreenActivity.whichbeat = "b";
                        if (StageMode.ab != null) {
                            StageMode.ab.setBackgroundDrawable(new ColorDrawable(FullscreenActivity.beatoffcolour));
                        }
                    } else {
                        FullscreenActivity.whichbeat = "a";
                        if (StageMode.ab != null) {
                            StageMode.ab.setBackgroundDrawable(new ColorDrawable(FullscreenActivity.metronomeColor));
                        }
                    }
                }*//*

            }
        };
    }
*/

    public static void startstopMetronome(Activity activity) {
        if (checkMetronomeValid() && FullscreenActivity.metronomeonoff.equals("off")) {
            // Start the metronome
            Runtime.getRuntime().gc();
            FullscreenActivity.metronomeonoff = "on";
            FullscreenActivity.whichbeat = "b";
            metroTask = new MetronomeAsyncTask();
            metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            startstopVisualMetronome();

        } else if (checkMetronomeValid() && FullscreenActivity.metronomeonoff.equals("on")) {
            // Stop the metronome
            Runtime.getRuntime().gc();
            FullscreenActivity.metronomeonoff = "off";
            if (metroTask!=null) {
                metroTask.stop();
            }

        } else {
            // Not valid, so open the popup
            FullscreenActivity.whattodo = "page_metronome";
            if (mListener!=null) {
                mListener.openFragment();
            } else {
                MyInterface mListener;
                mListener = (MyInterface) activity;
                mListener.openFragment();
            }
        }
    }

    public static boolean checkMetronomeValid() {
        boolean validTimeSig = false;
        boolean validBPM = true;
        boolean validMetro = false;

        if (getTempo(FullscreenActivity.mTempo)==160) {
            validBPM = false;
        }

        for (int i=0; i<FullscreenActivity.timesigs.length-1; i++) {
            if (FullscreenActivity.mTimeSig.equals(FullscreenActivity.timesigs[i])) {
                validTimeSig = true;
            }
        }

        if (validBPM && validTimeSig) {
            validMetro = true;
        }

        return validMetro;
    }

    public static void startstopVisualMetronome() {
        visualMetronome = new VisualMetronomeAsyncTask();
        visualMetronome.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    public static class VisualMetronomeAsyncTask extends AsyncTask<Void, Integer, String> {

        boolean on = false;
        int beatmultiplier = FullscreenActivity.noteValue;
        long time_in_millisecs = (long) (((60.0f / (float) bpm) * (4.0f / (float) beatmultiplier))* 1000);
        long oldtime = System.currentTimeMillis();
        long nexttime = oldtime + time_in_millisecs;

        @Override
        protected String doInBackground(Void... voids) {
            publishProgress(1);
            while (FullscreenActivity.metronomeonoff.equals("on")) {
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
            Log.d("d","updating progress");
            if (FullscreenActivity.visualmetronome) {
                if (FullscreenActivity.whichbeat.equals("a")) {
                    FullscreenActivity.whichbeat = "b";
                    if (StageMode.ab != null) {
                        StageMode.ab.setBackgroundDrawable(new ColorDrawable(FullscreenActivity.beatoffcolour));
                    }
                } else {
                    FullscreenActivity.whichbeat = "a";
                    if (StageMode.ab != null) {
                        StageMode.ab.setBackgroundDrawable(new ColorDrawable(FullscreenActivity.metronomeColor));
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (StageMode.ab != null) {
                StageMode.ab.setBackgroundDrawable(new ColorDrawable(FullscreenActivity.beatoffcolour));
            }
        }
    }

    public static class MetronomeAsyncTask extends AsyncTask<Void, Void, String> {

        Metronome metronome;

        MetronomeAsyncTask() {
            metronome = new Metronome();
        }

        void setNoteValue(short noteVal) {
            if (metronome != null && bpm >= FullscreenActivity.minBpm && bpm <= FullscreenActivity.maxBpm && noteVal > 0) {
                metronome.setNoteValue(noteVal);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setBeat(short beat) {
            if (metronome != null) {
                metronome.setBeat(beat);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setBpm(short bpm) {
            if (metronome != null && bpm >= FullscreenActivity.minBpm && bpm <= FullscreenActivity.maxBpm && FullscreenActivity.noteValue > 0) {
                metronome.setBpm(bpm);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setCurrentBeat(int currentBeat) {
            if (metronome != null) {
                metronome.setCurrentBeat(currentBeat);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setBeatSound(double beatSound) {
            if (metronome != null) {
                metronome.setBeatSound(beatSound);
                try {
                    metronome.calcSilence();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        void setSound(double sound) {
            if (metronome != null) {
                metronome.setSound(sound);
                try {
                    metronome.calcSilence();
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
            setBeat(FullscreenActivity.beats);
            setNoteValue(FullscreenActivity.noteValue);
            setBpm(bpm);
            setBeatSound(FullscreenActivity.beatSound);
            setSound(FullscreenActivity.sound);
            setVolume(FullscreenActivity.metrovol);
            setCurrentBeat(FullscreenActivity.currentBeat);
            play();
            return null;
        }

        public void stop() {
            if (metronome != null) {
                metronome.stop();
                metronome = null;
            }
        }

        public void play() {
            if (metronome != null) {
                metronome.play();
                metronome = null;
            }
        }

        @Override
        protected void onCancelled() {
            stop();
        }
    }

}

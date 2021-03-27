/*
package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpMetronomeFragment extends DialogFragment {

    private final Handler restartMetronome = new Handler();
    private final Runnable restartMetronomeRunnable = () -> {
        if (StaticVariables.metronomeonoff.equals("off") && StaticVariables.metronomeok) {
            startstopwithoutexit = true;
            this.popupmetronome_startstopbutton.callOnClick();
        }
        restartRequested = false;
    };

    private final Handler doStartStopCheck = new Handler();
    private final Runnable onEverySecond = this::prepareStartStopProgress;

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
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    private NumberPicker bpm_numberPicker, timesig_numberPicker, bar_numberPicker;
    private TextView bpmtext, popupmetronome_volume_text, popupmetronome_pan_text;
    private Button popupmetronome_startstopbutton;
    private SeekBar popupmetronome_pan;

    public static int tempo;
    public static short bpm;
    private static String[] timesigvals;

    private long old_time = 0;
    private int total_calc_bpm;
    private int total_counts = 0;
    private int metronomecolor;

    private boolean restartRequested = false;
    private boolean startstopwithoutexit = false;

    private Preferences preferences;
    private StorageAccess storageAccess;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        if (getDialog()==null) {
            dismiss();
        }

        preferences = new Preferences();
        storageAccess = new StorageAccess();

        switch (StaticVariables.mDisplayTheme) {
            case "dark":
            default:
                metronomecolor = preferences.getMyPreferenceInt(getContext(),"dark_metronomeColor",StaticVariables.darkishred);
                break;
            case "light":
                metronomecolor = preferences.getMyPreferenceInt(getContext(),"light_metronomeColor",StaticVariables.darkishred);
                break;
            case "custom1":
                metronomecolor = preferences.getMyPreferenceInt(getContext(),"custom1_metronomeColor",StaticVariables.darkishred);
                break;
            case "custom2":
                metronomecolor = preferences.getMyPreferenceInt(getContext(),"custom2_metronomeColor",StaticVariables.darkishred);
                break;
        }

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (mListener!=null) {
            mListener.pageButtonAlpha("metronome");
        }

        View V = inflater.inflate(R.layout.popup_page_metronome, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.metronome));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            doSave();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        // Initialise the views
        bpm_numberPicker = V.findViewById(R.id.bpm_numberPicker);
        timesig_numberPicker = V.findViewById(R.id.timesig_numberPicker);
        bar_numberPicker = V.findViewById(R.id.bar_numberPicker);
        bpmtext = V.findViewById(R.id.bpmtext);
        Button taptempo_Button = V.findViewById(R.id.taptempo_Button);
        SeekBar popupmetronome_volume = V.findViewById(R.id.popupmetronome_volume);
        popupmetronome_volume_text = V.findViewById(R.id.popupmetronome_volume_text);
        popupmetronome_pan = V.findViewById(R.id.popupmetronome_pan);
        popupmetronome_pan_text = V.findViewById(R.id.popupmetronome_pan_text);
        SwitchCompat visualmetronome = V.findViewById(R.id.visualmetronome);
        popupmetronome_startstopbutton = V.findViewById(R.id.popupmetronome_startstopbutton);

        // Set up the default values
        popupmetronome_pan_text.setText(preferences.getMyPreferenceString(getContext(),"metronomePan","C"));
        String vol = (int)(preferences.getMyPreferenceFloat(getContext(),"metronomeVol",0.5f) * 100.0f) + "%";
        popupmetronome_volume_text.setText(vol);

        if (StaticVariables.metronomeonoff.equals("on")) {
            popupmetronome_startstopbutton.setText(getString(R.string.stop));
            doStartStopCheck.removeCallbacks(onEverySecond);
            doStartStopCheck.postDelayed(onEverySecond, 1000);
        } else {
            popupmetronome_startstopbutton.setText(getString(R.string.start));
        }
        ProcessSong.processTimeSig();
        tempo = Metronome.getTempo(StaticVariables.mTempo);
        setPan();
        popupmetronome_volume.setProgress(getVolume(preferences.getMyPreferenceFloat(getContext(),"metronomeVol",0.5f)));
        visualmetronome.setChecked(preferences.getMyPreferenceBoolean(getContext(),"metronomeShowVisual",false));
        getTimeSigValues();
        getBPMValues();
        getDurationValues();

        // Set the listeners for changes
        taptempo_Button.setOnClickListener(view -> {
            considerRestart();
            tapTempo();
            StaticVariables.metronomeok = Metronome.isMetronomeValid();
        });
        popupmetronome_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                considerRestart();
                String text = i + "%";
                popupmetronome_volume_text.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float i = (float)seekBar.getProgress()/100.0f;
                preferences.setMyPreferenceFloat(getContext(),"metronomeVol",i);
                StaticVariables.metronomeok = Metronome.isMetronomeValid();
            }
        });
        popupmetronome_pan.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                considerRestart();
                switch (i) {
                    case 0:
                        preferences.setMyPreferenceString(getContext(),"metronomePan","L");
                        popupmetronome_pan_text.setText("L");
                        break;
                    case 1:
                        preferences.setMyPreferenceString(getContext(),"metronomePan","C");
                        popupmetronome_pan_text.setText("C");
                        break;
                    case 2:
                        preferences.setMyPreferenceString(getContext(),"metronomePan","R");
                        popupmetronome_pan_text.setText("R");
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                StaticVariables.metronomeok = Metronome.isMetronomeValid();
            }
        });
        visualmetronome.setOnCheckedChangeListener((compoundButton, b) -> {
            considerRestart();
            preferences.setMyPreferenceBoolean(getContext(),"metronomeShowVisual",b);
            StaticVariables.metronomeok = Metronome.isMetronomeValid();
        });
        popupmetronome_startstopbutton.setOnClickListener(view -> {
            // IV - doSave moved to dismiss
            if (StaticVariables.metronomeonoff.equals("off") && StaticVariables.metronomeok) {
                popupmetronome_startstopbutton.setText(getString(R.string.stop));
                StaticVariables.metronomeonoff = "on";
                StaticVariables.clickedOnMetronomeStart = true;
                doStartStopCheck.removeCallbacks(onEverySecond);
                doStartStopCheck.postDelayed(onEverySecond, 1000);
                StaticVariables.whichbeat = "b";
                Metronome.metroTask = new Metronome.MetronomeAsyncTask(preferences.getMyPreferenceString(getContext(),"metronomePan","C"),
                        preferences.getMyPreferenceFloat(getContext(),"metronomeVol",0.5f),
                        preferences.getMyPreferenceInt(getContext(),"metronomeLength",0));
                try {
                    Metronome.metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } catch (Exception e) {
                    Log.d ("d","Error starting metronmone");
                }
                Metronome.startstopVisualMetronome(preferences.getMyPreferenceBoolean(getContext(),"metronomeShowVisual",false),
                        metronomecolor);

            } else if (StaticVariables.metronomeonoff.equals("on")) {
                Runtime.getRuntime().gc();
                popupmetronome_startstopbutton.setText(getString(R.string.start));
                StaticVariables.metronomeonoff = "off";
                StaticVariables.clickedOnMetronomeStart = false;
                if (Metronome.metroTask!=null) {
                    Metronome.metroTask.stop();
                }
            } else {
                StaticVariables.myToastMessage = getString(R.string.metronome) + " - " + getString(R.string.notset);
                ShowToast.showToast(getContext());
            }
            if (!(startstopwithoutexit)) {
                PopUpMetronomeFragment.this.dismiss();
            } else {
                startstopwithoutexit = false;
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void considerRestart() {
        // IV - Restart a running metronome after a delay
        if (StaticVariables.metronomeonoff.equals("on")) {
            restartRequested = true;
            startstopwithoutexit = true;
            this.popupmetronome_startstopbutton.callOnClick();
        }
        // IV - Restart requests can arrive quickly - renew callback each time
        if (restartRequested) {
            restartMetronome.removeCallbacks(restartMetronomeRunnable);
            restartMetronome.postDelayed(restartMetronomeRunnable, 1500);
        }
    }

    private void doSave() {
        PopUpEditSongFragment.prepareSongXML();
        try {
            if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getContext());
                NonOpenSongSQLite nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getContext(),storageAccess,preferences,nonOpenSongSQLiteHelper.getSongId());
                nonOpenSongSQLiteHelper.updateSong(getContext(),storageAccess,preferences,nonOpenSongSQLite);
            } else {
                PopUpEditSongFragment.justSaveSongXML(getContext(), preferences);
            }
            // IV - removed as we (perhaps) do not need to announce expected behaviour
        } catch (Exception e) {
            e.printStackTrace();
            StaticVariables.myToastMessage = getString(R.string.save) + " - " + getString(R.string.error);
            ShowToast.showToast(getContext());
        }
        dismiss();
    }

    @SuppressLint("SetTextI18n")
    private void tapTempo() {
        // This function checks the previous tap_tempo time and calculates the bpm
        // Variables for tap tempo
        considerRestart();
        long new_time = System.currentTimeMillis();
        long time_passed = new_time - old_time;
        int calc_bpm = Math.round((1 / ((float) time_passed / 1000)) * 60);

        // Need to decide on the time sig.
        // If it ends in /2, then double the tempo
        // If it ends in /4, then leave as is
        // If it ends in /8, then half it
        // If it isn't set, set it to default as 4/4
        if (StaticVariables.mTimeSig.isEmpty()) {
            timesig_numberPicker.setValue(6);
            StaticVariables.mTimeSig = "4/4";
        } else if (StaticVariables.mTimeSig.endsWith("/2")) {
            calc_bpm = (int) ((float) calc_bpm * 2.0f);
        } else if (StaticVariables.mTimeSig.endsWith("/8")) {
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

        int av_bpm = Math.round((float) total_calc_bpm / (float) total_counts);

        if (av_bpm < 300 && av_bpm >= 40) {
            bpmtext.setText(getString(R.string.bpm));
            bpm_numberPicker.setValue(av_bpm -40);
            StaticVariables.mTempo = "" + av_bpm;
        } else if (av_bpm <40) {
            bpm_numberPicker.setValue(40);
            bpmtext.setText("<40 bpm");
        }  else {
            bpm_numberPicker.setValue(260);
            bpmtext.setText(">299 bpm");
        }

        bpm = (short) av_bpm;
        old_time = new_time;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        if (Metronome.metroTask!=null) {
            Metronome.metroTask.cancel(true);
        }
        if (Metronome.visualMetronome!=null) {
            Log.d("d","visualMetronome not null");
        }
        this.dismiss();
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        // IV - doSave in dismiss to ensure save
        doSave();
        // IV - Remove any restart callbacks
        restartMetronome.removeCallbacks(restartMetronomeRunnable);
        doStartStopCheck.removeCallbacks(onEverySecond);
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
    }

    private void setPan(){
        switch (preferences.getMyPreferenceString(getContext(),"metronomePan","C")) {
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

    private int getVolume(float v) {
        return (int) (v*100.0f);
    }

    private void getDurationValues() {
        String[] durationValues = new String[8];
        durationValues[0]=getString(R.string.continuous);
        durationValues[1]="1";
        durationValues[2]="2";
        durationValues[3]="4";
        durationValues[4]="8";
        durationValues[5]="16";
        durationValues[6]="32";
        durationValues[7]="64";
        bar_numberPicker.setMinValue(0);
        bar_numberPicker.setMaxValue(durationValues.length-1);
        bar_numberPicker.setDisplayedValues(durationValues);
        int val = preferences.getMyPreferenceInt(getContext(),"metronomeLength",0);
        int pos = getLengthPosition(val);
        bar_numberPicker.setValue(pos);
        bar_numberPicker.setOnValueChangedListener((numberPicker, i, i1) -> {
            considerRestart();
            i1 = getBarValues(i1);
            preferences.setMyPreferenceInt(getContext(),"metronomeLength",i1);
        });
    }

    private int getBarValues(int val) {
        int newval;
        switch (val) {
            case 0:
            default:
                newval = 0;
                break;

            case 1:
                newval = 1;
                break;

            case 2:
                newval = 2;
                break;

            case 3:
                newval = 4;
                break;

            case 4:
                newval = 8;
                break;

            case 5:
                newval = 16;
                break;

            case 6:
                newval = 32;
                break;

            case 7:
                newval = 64;
                break;
        }
        return newval;
    }
    private int getLengthPosition(int val) {
        int pos;

        switch(val) {
            case 0:
            default:
                pos = 0;
                break;

            case 1:
                pos = 1;
                break;

            case 2:
                pos = 2;
                break;

            case 4:
                pos = 3;
                break;

            case 8:
                pos = 4;
                break;

            case 16:
                pos = 5;
                break;

            case 32:
                pos = 6;
                break;

            case 64:
                pos = 7;
                break;
        }
        return pos;
    }

    private void getBPMValues() {
        String[] bpmValues = new String[261];
        // Add the values 40 to 299 bpm
        for (int z=0;z<260;z++) {
            bpmValues[z] = "" + (z+40);
        }
        // Now add the 'Not set' value
        bpmValues[260] = getString(R.string.notset);

        bpm_numberPicker.setMinValue(0); //from array first value

        //Specify the maximum value/number of NumberPicker
        bpm_numberPicker.setMaxValue(bpmValues.length-1); //to array last value

        //Specify the NumberPicker data source as array elements
        bpm_numberPicker.setDisplayedValues(bpmValues);
        bpm_numberPicker.setValue(tempo-40);
        bpm_numberPicker.setOnValueChangedListener((numberPicker, i, i1) -> {
            considerRestart();
            if (i1==260) {
                // This is the not set value
                tempo = 261;
                bpm = 0;
                StaticVariables.mTempo = "";
            } else {
                StaticVariables.mTempo = "" + (i1+40);
                bpm = (short) (i1+40);
            }
            bpmtext.setText(getString(R.string.bpm));
            StaticVariables.metronomeok = Metronome.isMetronomeValid();

        });
    }

    private void getTimeSigValues() {
        timesig_numberPicker.setMinValue(0);
        // Max value is 1 bigger as we have still to include the not set value
        String[] oldvals = requireContext().getResources().getStringArray(R.array.timesig);
        timesigvals = new String[oldvals.length];
        System.arraycopy(oldvals, 0, timesigvals, 0, oldvals.length);
        timesigvals[0] = requireContext().getString(R.string.notset);
        timesig_numberPicker.setMaxValue(timesigvals.length-1);
        timesig_numberPicker.setDisplayedValues(timesigvals);

        // Set the default value:
        int defpos = 0;

        for (int i=0;i<timesigvals.length;i++) {
            if (StaticVariables.mTimeSig.equals(timesigvals[i])) {
                defpos = i;
            }
        }
        timesig_numberPicker.setValue(defpos);

        timesig_numberPicker.setOnValueChangedListener((numberPicker, i, i1) -> {
            considerRestart();
            if (i1 == 0) {
                // First value, which is not set
                StaticVariables.mTimeSig = "";
                FullscreenActivity.beats = 0;
                FullscreenActivity.noteValue = 0;
            } else {
                StaticVariables.mTimeSig = timesigvals[i1];
                Metronome.setBeatValues();
                Metronome.setNoteValues();
            }
            StaticVariables.metronomeok = Metronome.isMetronomeValid();
        });
    }

    private void prepareStartStopProgress() {
        if (StaticVariables.metronomeonoff.equals("on")) {
            doStartStopCheck.removeCallbacks(onEverySecond);
            doStartStopCheck.postDelayed(onEverySecond, 1000);
        } else {
            popupmetronome_startstopbutton.setText(getString(R.string.start));
        }
    }


}*/

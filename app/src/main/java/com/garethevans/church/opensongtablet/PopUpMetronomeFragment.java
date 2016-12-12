package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class PopUpMetronomeFragment extends DialogFragment {

    static PopUpMetronomeFragment newInstance() {
        PopUpMetronomeFragment frag;
        frag = new PopUpMetronomeFragment();
        return frag;
    }

    public interface MyInterface {
        void pageButtonAlpha(String s);
    }

    private MyInterface mListener;

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

    Spinner popupmetronome_timesig;
    SeekBar popupmetronome_tempo;
    TextView popupmetronome_tempo_text;
    Button decreaseTempo_Button;
    Button increaseTempo_Button;
    Button taptempo_Button;
    SeekBar popupmetronome_volume;
    TextView popupmetronome_volume_text;
    SeekBar popupmetronome_pan;
    TextView popupmetronome_pan_text;
    SwitchCompat visualmetronome;
    Button save_button;
    Button popupmetronome_startstopbutton;
    public static short bpm;
    public static String tempo_text;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        getDialog().setTitle(getActivity().getResources().getString(R.string.metronome));
        getDialog().setCanceledOnTouchOutside(true);
        mListener.pageButtonAlpha("metronome");

        View V = inflater.inflate(R.layout.popup_page_metronome, container, false);

        // Initialise the views
        popupmetronome_timesig = (Spinner) V.findViewById(R.id.popupmetronome_timesig);
        popupmetronome_tempo = (SeekBar) V.findViewById(R.id.popupmetronome_tempo);
        popupmetronome_tempo_text = (TextView) V.findViewById(R.id.popupmetronome_tempo_text);
        decreaseTempo_Button = (Button) V.findViewById(R.id.decreaseTempo_Button);
        increaseTempo_Button = (Button) V.findViewById(R.id.increaseTempo_Button);
        taptempo_Button = (Button) V.findViewById(R.id.taptempo_Button);
        popupmetronome_volume = (SeekBar) V.findViewById(R.id.popupmetronome_volume);
        popupmetronome_volume_text = (TextView) V.findViewById(R.id.popupmetronome_volume_text);
        popupmetronome_pan = (SeekBar) V.findViewById(R.id.popupmetronome_pan);
        popupmetronome_pan_text = (TextView) V.findViewById(R.id.popupmetronome_pan_text);
        visualmetronome = (SwitchCompat) V.findViewById(R.id.visualmetronome);
        save_button = (Button) V.findViewById(R.id.save_button);
        popupmetronome_startstopbutton = (Button) V.findViewById(R.id.popupmetronome_startstopbutton);

        // Set up the spinner options
        ArrayAdapter<CharSequence> adapter_timesigs = ArrayAdapter.createFromResource(getActivity(),
                R.array.timesig,
                R.layout.my_spinner);
        adapter_timesigs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        popupmetronome_timesig.setAdapter(adapter_timesigs);
        ProcessSong.processTimeSig();
        popupmetronome_timesig.setSelection(FullscreenActivity.timesigindex);

        // Set the SeekBars and corresponding text boxes
        int tempo = getTempo(FullscreenActivity.mTempo) - 39;
        String tempo_t = getTempoText(getActivity().getResources().getString(R.string.notset),tempo+39);
        popupmetronome_tempo.setProgress(tempo);
        popupmetronome_tempo_text.setText(tempo_t);
        setPan();
        popupmetronome_volume.setProgress(getVolume(FullscreenActivity.metronomevol));



        return V;
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
            bpm = 39;
        }

        return (int) bpm;
    }

    public static String getTempoText(String notset, int t) {
        if (t==0) {
            tempo_text = notset;
        } else {
            tempo_text = t + "";
        }
        return tempo_text;
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
        return (int) v*100;
    }

}

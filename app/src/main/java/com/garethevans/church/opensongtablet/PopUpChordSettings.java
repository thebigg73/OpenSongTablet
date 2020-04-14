package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class PopUpChordSettings extends DialogFragment {

    static PopUpChordSettings newInstance() {
        PopUpChordSettings frag;
        frag = new PopUpChordSettings();
        return frag;
    }

    public interface MyInterface {
        void refreshAll();
    }

    private MyInterface mListener;
    private Preferences preferences;

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

    private SwitchCompat showChordsSwitch, showCapoChordsSwitch, showBothChordsSwitch;
    private TextView title,scaleChordsTitle_TextView, scaleChords_TextView;
    private SeekBar scaleChords_SeekBar;
    private FloatingActionButton closeMe, saveMe;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_chordsettings, container, false);

        // Initialise the helper classes
        preferences = new Preferences();

        // Initialise the views
        initialiseViews(V);

        // Set the values to defaults
        setDefaults();

        // Update the font sizes
        updateFontSizes();

        // Set up the listeners
        setUpListeners();
        return V;
    }

    private void initialiseViews(View v) {
        title = v.findViewById(R.id.dialogtitle);
        scaleChordsTitle_TextView = v.findViewById(R.id.scaleChordsTitle_TextView);
        scaleChords_TextView = v.findViewById(R.id.scaleChords_TextView);
        scaleChords_SeekBar = v.findViewById(R.id.scaleChords_SeekBar);
        showChordsSwitch = v.findViewById(R.id.showChordsSwitch);
        showCapoChordsSwitch = v.findViewById(R.id.showCapoChordsSwitch);
        showBothChordsSwitch = v.findViewById(R.id.showBothChordsSwitch);
        closeMe = v.findViewById(R.id.closeMe);
        saveMe = v.findViewById(R.id.saveMe);
    }

    private void setDefaults() {
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.chord_settings));
        saveMe.hide();
        scaleChords_SeekBar.setMax(200);
        int progress = (int) (preferences.getMyPreferenceFloat(getActivity(),"scaleChords",1.0f) * 100);
        scaleChords_SeekBar.setProgress(progress);
        String text = progress + "%";
        scaleChords_TextView.setText(text);
        showChordsSwitch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"displayChords",true));
        showCapoChordsSwitch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"displayCapoChords",true));
        showBothChordsSwitch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"displayCapoAndNativeChords",false));
    }

    private void setUpListeners() {
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener!=null) {
                    mListener.refreshAll();
                }
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        scaleChords_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = progress + "%";
                scaleChords_TextView.setText(text);
                //float newsize = 12 * ((float) progress/100.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float num = (float) scaleChords_SeekBar.getProgress() / 100.0f;
                preferences.setMyPreferenceFloat(getActivity(), "scaleChords", num);
            }
        });
        showChordsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeVisibility(isChecked);
                preferences.setMyPreferenceBoolean(getActivity(),"displayChords",isChecked);
            }
        });
        showCapoChordsSwitch.setOnCheckedChangeListener(new SaveSwitchChange("displayCapoChords"));
        showBothChordsSwitch.setOnCheckedChangeListener(new SaveSwitchChange("displayCapoAndNativeChords"));
    }

    private void changeVisibility(boolean show) {
        int vis;
        if (show) {
            vis = View.VISIBLE;
        } else {
            vis = View.GONE;
        }
        showCapoChordsSwitch.setVisibility(vis);
        showBothChordsSwitch.setVisibility(vis);
        scaleChordsTitle_TextView.setVisibility(vis);
        scaleChords_TextView.setVisibility(vis);
        scaleChords_SeekBar.setVisibility(vis);
    }

    private void updateFontSizes() {
        float menuFontSize = preferences.getMyPreferenceFloat(getActivity(), "songMenuAlphaIndexSize", 14.0f);
        ResizeMenuItems resizeMenuItems = new ResizeMenuItems();
        resizeMenuItems.updateTextViewSize(title, menuFontSize, "L", false);
        resizeMenuItems.updateTextViewSize(showChordsSwitch, menuFontSize, "", false);
        resizeMenuItems.updateTextViewSize(showCapoChordsSwitch, menuFontSize, "", false);
        resizeMenuItems.updateTextViewSize(showBothChordsSwitch, menuFontSize, "", false);
        resizeMenuItems.updateTextViewSize(scaleChordsTitle_TextView, menuFontSize, "", false);
        resizeMenuItems.updateTextViewSize(scaleChords_TextView, menuFontSize, "S", false);
    }

    class SaveSwitchChange implements CompoundButton.OnCheckedChangeListener {
        String value;
        SaveSwitchChange(String val) {
            this.value = val;
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            preferences.setMyPreferenceBoolean(getActivity(), value, isChecked);
        }
    }
}
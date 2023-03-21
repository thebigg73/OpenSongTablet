package com.garethevans.church.opensongtablet.stickynotes;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsStickynotesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class StickyNotesFragment extends Fragment {

    SettingsStickynotesBinding myView;
    MainActivityInterface mainActivityInterface;
    private String song_notes_string="", on_string="", success_string="", error_string="",
            error_song_not_saved_string="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsStickynotesBinding.inflate(inflater,container,false);

        prepareStrings();

        mainActivityInterface.updateToolbar(song_notes_string);

        // Set up the views
        setupViews();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            song_notes_string = getString(R.string.song_notes);
            on_string = getString(R.string.on);
            success_string = getString(R.string.success);
            error_song_not_saved_string = getString(R.string.error_song_not_saved);
            error_string = getString(R.string.error);
        }
    }

    private void setupViews() {
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.stickyNotes);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.stickyNotes,8);
        myView.stickyNotes.setText(mainActivityInterface.getSong().getNotes());
        myView.autoShowSticky.setChecked(mainActivityInterface.getPreferences().
                getMyPreferenceBoolean("stickyAuto",true));
        hideTimeVisibility(myView.autoShowSticky.isChecked());
        int time = mainActivityInterface.getPreferences().
                getMyPreferenceInt("timeToDisplaySticky",0);
        myView.timeSlider.setValue((float)time);
        myView.timeSlider.setLabelFormatter(value -> ((int)value)+"s");
        setTimeHint(time);
        int alpha = Math.round(mainActivityInterface.getMyThemeColors().getStickyBackgroundSplitAlpha()*100.0f);
        if (alpha<50) {
            alpha = 50;
        }
        myView.alphaSlider.setValue(alpha);
        setAlphaHint(alpha);
        myView.alphaSlider.setLabelFormatter(value -> ((int)value)+"%");
        float stickyTextSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("stickyTextSize",14f);

        myView.stickyTextSize.setLabelFormatter(value -> ((int)value)+"sp");
        myView.stickyTextSize.setValue(stickyTextSize);
        myView.stickyTextSize.setHintTextSize(stickyTextSize);
        myView.stickyTextSize.setHint((int)stickyTextSize+"sp");
    }

    private void setTimeHint(int time) {
        String val = "s";
        if (time==0) {
            val = on_string;
        } else {
            val = time + val;
        }
        myView.timeSlider.setHint(val);
    }

    private void setAlphaHint(float alpha) {
        String s = (int)alpha + "%";
        myView.alphaSlider.setHint(s);
    }

    private void setupListeners() {
        myView.autoShowSticky.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("stickyAuto",isChecked);
            hideTimeVisibility(isChecked);
        });
        myView.saveButton.setOnClickListener(v -> {
            if (myView.stickyNotes.getText()!=null) {
                mainActivityInterface.getSong().setNotes(myView.stickyNotes.getText().toString());
                if (mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false)) {
                    mainActivityInterface.getShowToast().doIt(success_string);
                } else {
                    mainActivityInterface.getShowToast().doIt(error_song_not_saved_string);
                }
            } else {
                mainActivityInterface.getShowToast().doIt(error_string);
            }
        });
        myView.alphaSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) { }


            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                float val = slider.getValue()/100f;
                int color = ColorUtils.setAlphaComponent(mainActivityInterface.getMyThemeColors().getStickyBackgroundSplitColor(),(int)(val*255f));
                mainActivityInterface.getMyThemeColors().setStickyTextColor(color);
                String theme = mainActivityInterface.getMyThemeColors().getThemeName();
                mainActivityInterface.getPreferences().setMyPreferenceInt(theme+"_stickyBackgroundColor",color);
                mainActivityInterface.getMyThemeColors().splitColorAndAlpha();
            }
        });
        myView.alphaSlider.addOnChangeListener((slider, value, fromUser) -> setAlphaHint(value));
        myView.timeSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {

            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getPreferences().setMyPreferenceInt(
                        "timeToDisplaySticky", Math.round(slider.getValue()));
            }
        });
        myView.timeSlider.addOnChangeListener((slider, value, fromUser) -> setTimeHint(Math.round(value)));

        myView.stickyTextSize.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getPreferences().setMyPreferenceFloat("stickyTextSize",myView.stickyTextSize.getValue());
            }
        });
        myView.stickyTextSize.addOnChangeListener((slider, value, fromUser) -> {
            myView.stickyTextSize.setHintTextSize(value);
            myView.stickyTextSize.setHint(((int)value)+"sp");
        });

    }

    private void hideTimeVisibility(boolean visible) {
        if (visible) {
            myView.timeSlider.setVisibility(View.VISIBLE);
        } else {
            myView.timeSlider.setVisibility(View.GONE);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsStickynotesBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(getString(R.string.song_notes));

        // Set up the views
        setupViews();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        myView.stickyNotes.setText(mainActivityInterface.getSong().getNotes());
        myView.stickyNotes.setLines(8);
        myView.stickyNotes.setMinLines(8);
        myView.autoShowSticky.setChecked(mainActivityInterface.getPreferences().
                getMyPreferenceBoolean(requireContext(),"stickyAuto",true));
        int time = mainActivityInterface.getPreferences().
                getMyPreferenceInt(requireContext(),"timeToDisplaySticky",0);
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
    }

    private void setTimeHint(int time) {
        String val = "s";
        if (time==0) {
            val = getString(R.string.on);
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
        myView.autoShowSticky.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"stickyAuto",isChecked));
        myView.saveButton.setOnClickListener(v -> {
            if (myView.stickyNotes.getText()!=null) {
                mainActivityInterface.getSong().setNotes(myView.stickyNotes.getText().toString());
                if (mainActivityInterface.getSaveSong().updateSong(requireContext(),mainActivityInterface,
                        mainActivityInterface.getSong())) {
                    mainActivityInterface.getShowToast().doIt(getString(R.string.success));
                } else {
                    mainActivityInterface.getShowToast().doIt(getString(R.string.error_song_not_saved));
                }
            } else {
                mainActivityInterface.getShowToast().doIt(getString(R.string.error));
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
                mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(),theme+"_stickyBackgroundColor",color);
                mainActivityInterface.getMyThemeColors().splitColorAndAlpha(mainActivityInterface);
            }
        });
        myView.alphaSlider.addOnChangeListener((slider, value, fromUser) -> setAlphaHint(value));
        myView.timeSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {

            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(),
                        "timeToDisplaySticky", Math.round(slider.getValue()));
            }
        });
        myView.timeSlider.addOnChangeListener((slider, value, fromUser) -> setTimeHint(Math.round(value)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

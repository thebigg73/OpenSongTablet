package com.garethevans.church.opensongtablet.screensetup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsDisplayExtraBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class DisplayExtraFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsDisplayExtraBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsDisplayExtraBinding.inflate(inflater,container,false);
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mainActivityInterface.updateToolbar(getString(R.string.song_display));

        // Set up views
        setViews();

        // Set up listeners
        setListeners();

        return myView.getRoot();
    }

    private void setViews() {
        // Set the checkboxes
        myView.songSheet.setChecked(getChecked("songSheet",false));
        myView.nextInSet.setChecked(getChecked("nextInSet",true));
        myView.prevInSet.setChecked(getChecked("prevInSet",false));
        myView.prevNextSongMenu.setChecked(getChecked("prevNextSongMenu",false));
        myView.boldChordsHeadings.setChecked(getChecked("displayBoldChordsHeadings",false));
        myView.trimSections.setChecked(getChecked("trimSections",true));
        myView.addSectionSpace.setChecked(getChecked("addSectionSpace",true));
        myView.trimLineSpacing.setChecked(getChecked("trimLines",false));
        visibilityByBoolean(myView.trimLineSpacingSlider,myView.trimLineSpacing.getChecked());
        float lineSpacing = mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),"lineSpacing",0.1f);
        int percentage = (int)(lineSpacing * 100);
        myView.trimLineSpacingSlider.setValue(percentage);
        sliderValToText(percentage);
        myView.filterSwitch.setChecked(getChecked("filterSections",false));
        visibilityByBoolean(myView.filterLayout,myView.filterSwitch.isChecked());
        myView.filterShow.setChecked(getChecked("filterShow",false));
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.filters);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.filters,4);
    }

    private boolean getChecked(String prefName, boolean fallback) {
        return mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),prefName,fallback);
    }
    private void visibilityByBoolean(View view, boolean visible) {
        if (visible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }
    private void sliderValToText(float value) {
        String hint = ((int)value) + "%";
        myView.trimLineSpacingSlider.setHint(hint);
    }
    private void setListeners() {
        // The switches
        myView.songSheet.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("songSheet",isChecked,null));
        myView.prevInSet.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("prevInSet",isChecked,null);
            mainActivityInterface.getDisplayPrevNext().updateShow(requireContext(),mainActivityInterface);
        });
        myView.nextInSet.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("nextInSet",isChecked,null);
            mainActivityInterface.getDisplayPrevNext().updateShow(requireContext(),mainActivityInterface);
        });
        myView.prevNextSongMenu.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("prevNextSongMenu", isChecked, null);
            mainActivityInterface.getDisplayPrevNext().updateShow(requireContext(),mainActivityInterface);
        });
        myView.boldChordsHeadings.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("displayBoldChordsHeadings",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(),mainActivityInterface);
        });
        myView.trimSections.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("trimSections",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(),mainActivityInterface);
        });
        myView.addSectionSpace.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("addSectionSpace",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(),mainActivityInterface);
        });
        myView.trimLineSpacing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("trimLines",isChecked,myView.trimLineSpacingSlider);
            mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(),mainActivityInterface);
        });
        myView.filterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("filterSections",isChecked,myView.filterLayout);
            mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(),mainActivityInterface);
        });
        myView.filterShow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("filterShow",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(),mainActivityInterface);
        });

        // The slider
        myView.trimLineSpacingSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) { }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                // Save the new value
                float percentage = slider.getValue()/100f;
                mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),"lineSpacing",percentage);
                mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(),mainActivityInterface);
            }
        });
        myView.trimLineSpacingSlider.addOnChangeListener((slider, value, fromUser) -> sliderValToText(value));

        // The button
        myView.filterSave.setOnClickListener(v -> {
            // Get the text from the edittext
            if (myView.filters.getText()!=null) {
                String s = myView.filters.getText().toString();
                // Split by line in order to trim
                String[] lines = s.split("\n");
                StringBuilder stringBuilder = new StringBuilder();
                for (String line:lines) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        stringBuilder.append(line).append("\n");
                    }
                }
                String newText = stringBuilder.toString().trim();
                // Put the corrected text back in
                myView.filters.setText(newText);
                // Save it
                mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),"filterText",newText);
            }
        });
    }

    private void updateBooleanPreference(String prefName, boolean isChecked, View viewToShowHide) {
        mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),prefName,isChecked);
        mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(),mainActivityInterface);
        if (viewToShowHide!=null) {
            visibilityByBoolean(viewToShowHide,isChecked);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

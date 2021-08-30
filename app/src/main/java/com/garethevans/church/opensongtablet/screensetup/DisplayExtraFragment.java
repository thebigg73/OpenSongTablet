package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsDisplayExtraBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

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
        myView.hideLyricsBox.setChecked(getChecked("hideLyricsBox",true));
        myView.trimSections.setChecked(getChecked("trimSections",true));
        myView.addSectionSpace.setChecked(getChecked("addSectionSpace",true));
        myView.trimLineSpacing.setChecked(getChecked("trimLines",false));
        visibilityByBoolean(myView.trimLineSpacingLayout,myView.trimLineSpacing.isChecked());
        float lineSpacing = mainActivityInterface.getPreferences().getMyPreferenceFloat(requireContext(),"lineSpacing",0.1f);
        int percentage = (int)(lineSpacing * 100);
        myView.trimLineSpacingSeekBar.setProgress(percentage-1);
        progressToText(percentage-1);
        myView.filterSwitch.setChecked(getChecked("filterSections",false));
        visibilityByBoolean(myView.filterLayout,myView.filterSwitch.isChecked());
        myView.filterShow.setChecked(getChecked("filterShow",false));
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
    private void progressToText(int progress) {
        String string = (progress+1) + "%";
        myView.trimLineSpacingText.setText(string);
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
        myView.boldChordsHeadings.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("displayBoldChordsHeadings",isChecked,null));
        myView.hideLyricsBox.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("hideLyricsBox",isChecked,null));
        myView.trimSections.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("trimSections",isChecked,null));
        myView.addSectionSpace.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("addSectionSpace",isChecked,null));
        myView.trimLineSpacing.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("trimLines",isChecked,myView.trimLineSpacingLayout));
        myView.filterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("filterSections",isChecked,myView.filterLayout));
        myView.filterShow.setOnCheckedChangeListener((buttonView, isChecked) -> updateBooleanPreference("filterShow",isChecked,null));

        // The seekbar
        myView.trimLineSpacingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressToText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int percentage = myView.trimLineSpacingSeekBar.getProgress()+1;
                float val = (float)percentage/100.0f;
                mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),"lineSpacing",val);
            }
        });

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

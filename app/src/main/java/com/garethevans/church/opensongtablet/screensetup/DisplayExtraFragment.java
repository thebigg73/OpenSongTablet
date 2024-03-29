package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsDisplayExtraBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class DisplayExtraFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private SettingsDisplayExtraBinding myView;
    private String[] bracketStyles_Names;
    private int[] bracketStyles_Ints;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "DisplayExtraFrag";
    private String song_display_string="", website_song_display_string="", save_string="",
            filters_string="", format_text_normal_string="", format_text_italic_string="",
            format_text_bold_string="", format_text_bolditalic_string="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(song_display_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        displayInterface = (DisplayInterface) mainActivityInterface;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsDisplayExtraBinding.inflate(inflater,container,false);

        prepareStrings();

        if (getActivity()!=null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        webAddress = website_song_display_string;

        // Set up views
        setViews();

        // Set up listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            song_display_string = getString(R.string.song_display);
            website_song_display_string = getString(R.string.website_song_display);
            save_string = getString(R.string.save);
            filters_string = getString(R.string.filters);
            format_text_normal_string = getString(R.string.format_text_normal);
            format_text_italic_string = getString(R.string.format_text_italic);
            format_text_bold_string = getString(R.string.format_text_bold);
            format_text_bolditalic_string = getString(R.string.format_text_bolditalic);
        }
    }
    private void setViews() {
        // Set the checkboxes
        myView.songSheet.setChecked(getChecked("songSheet",false));
        myView.nextInSet.setChecked(getChecked("nextInSet",true));
        myView.prevInSet.setChecked(getChecked("prevInSet",false));
        myView.prevNextSongMenu.setChecked(getChecked("prevNextSongMenu",false));
        myView.prevNextTextButtons.setChecked(getChecked("prevNextTextButtons",true));
        myView.prevNextHide.setChecked(getChecked("prevNextHide",true));
        myView.onscreenAutoscrollHide.setChecked(getChecked("onscreenAutoscrollHide",true));
        myView.onscreenCapoHide.setChecked(getChecked("onscreenCapoHide", true));
        myView.onscreenPadHide.setChecked(getChecked("onscreenPadHide",true));
        myView.boldChordsHeadings.setChecked(getChecked("displayBoldChordsHeadings",false));
        myView.boldChorus.setChecked(getChecked("displayBoldChorus",false));
        myView.showChords.setChecked(getChecked("displayChords",true));
        myView.showLyrics.setChecked(getChecked("displayLyrics",true));
        myView.presoOrder.setChecked(getChecked("usePresentationOrder",false));
        myView.keepMultiline.setChecked(getChecked("multiLineVerseKeepCompact",false));
        myView.trimSections.setChecked(getChecked("trimSections",true));
        myView.addSectionSpace.setChecked(getChecked("addSectionSpace",true));
        myView.trimLineSpacing.setChecked(getChecked("trimLines",false));
        visibilityByBoolean(myView.trimLineSpacingSlider,myView.trimLineSpacing.getChecked());
        float lineSpacing = mainActivityInterface.getPreferences().getMyPreferenceFloat("lineSpacing",0.1f);
        int percentage = (int)(lineSpacing * 100);
        myView.trimLineSpacingSlider.setValue(percentage);
        myView.trimLineSpacingSlider.setLabelFormatter(value -> ((int)value)+"%");
        sliderValToText(percentage);
        myView.trimWordSpacing.setChecked(getChecked("trimWordSpacing", true));
        // TODO Maybe add later
        // myView.addSectionBox.setChecked(getChecked("addSectionBox",false));
        myView.filterSwitch.setChecked(getChecked("filterSections",false));
        visibilityByBoolean(myView.filterLayout,myView.filterSwitch.getChecked());
        myView.filterShow.setChecked(getChecked("filterShow",false));
        String text = save_string + " (" + filters_string + ")";
        myView.filterSave.setText(text);
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.filters);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.filters,4);
        myView.filters.setText(mainActivityInterface.getPreferences().getMyPreferenceString("filterText",""));
        bracketStyles_Names = new String[] {format_text_normal_string,format_text_italic_string,
                format_text_bold_string,format_text_bolditalic_string};
        bracketStyles_Ints = new int[] {Typeface.NORMAL,Typeface.ITALIC,Typeface.BOLD,Typeface.BOLD_ITALIC};
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.bracketsStyle, R.layout.view_exposed_dropdown_item, bracketStyles_Names);
            myView.bracketsStyle.setAdapter(exposedDropDownArrayAdapter);
        }
        myView.bracketsStyle.setText(getBracketStringFromValue(mainActivityInterface.getPreferences().getMyPreferenceInt("bracketsStyle",Typeface.NORMAL)));
        myView.curlyBrackets.setChecked(getChecked("curlyBrackets",true));
        myView.curlyBracketsDevice.setChecked(getChecked("curlyBracketsDevice",false));
        myView.pdfHorizontal.setChecked(getChecked("pdfLandscapeView",true));
    }

    private int getBracketValueFromString(String string) {
        int value = 0;
        for (int x=0; x<bracketStyles_Names.length; x++) {
            if (bracketStyles_Names[x].equals(string)) {
                value = bracketStyles_Ints[x];
            }
        }
        return value;
    }

    private String getBracketStringFromValue(int value) {
        String string = format_text_normal_string;
        for (int x=0; x<bracketStyles_Ints.length; x++) {
            if (bracketStyles_Ints[x]==value) {
                string = bracketStyles_Names[x];
            }
        }
        return string;
    }

    private boolean getChecked(String prefName, boolean fallback) {
        return mainActivityInterface.getPreferences().getMyPreferenceBoolean(prefName,fallback);
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
            mainActivityInterface.getDisplayPrevNext().updateShow();
        });
        myView.nextInSet.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("nextInSet",isChecked,null);
            mainActivityInterface.getDisplayPrevNext().updateShow();
        });
        myView.prevNextSongMenu.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("prevNextSongMenu", isChecked, null);
            mainActivityInterface.getDisplayPrevNext().updateShow();
        });
        myView.prevNextTextButtons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("prevNextTextButtons", isChecked, null);
            mainActivityInterface.getDisplayPrevNext().updateShow();
        });
        myView.prevNextHide.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("prevNextHide", isChecked, null);
            mainActivityInterface.getDisplayPrevNext().updateShow();
        });
        myView.onscreenAutoscrollHide.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("onscreenAutoscrollHide", isChecked, null);
            mainActivityInterface.updateOnScreenInfo("setpreferences");
        });
        myView.onscreenCapoHide.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("onscreenCapoHide", isChecked, null);
            mainActivityInterface.updateOnScreenInfo("setpreferences");
        });
        myView.onscreenPadHide.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("onscreenPadHide", isChecked, null);
            mainActivityInterface.updateOnScreenInfo("setpreferences");
        });
        myView.boldChordsHeadings.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("displayBoldChordsHeadings",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
            displayInterface.updateDisplay("setSongContentPrefs");
        });
        myView.boldChorus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("displayBoldChorus", isChecked, null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
            displayInterface.updateDisplay("setSongContentPrefs");
        });
        myView.showChords.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("displayChords",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
        });
        myView.showLyrics.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("displayLyrics",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
        });
        myView.presoOrder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("usePresentationOrder",isChecked,null);
            mainActivityInterface.getPresenterSettings().setUsePresentationOrder(isChecked);
        });
        myView.keepMultiline.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            updateBooleanPreference("multiLineVerseKeepCompact",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
        }));
        myView.trimSections.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("trimSections",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
        });
        myView.addSectionSpace.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("addSectionSpace",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
        });
        myView.trimLineSpacing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("trimLines",isChecked,myView.trimLineSpacingSlider);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
        });
        myView.trimWordSpacing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("trimWordSpacing",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
        });
        myView.bracketsStyle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int value = getBracketValueFromString(myView.bracketsStyle.getText().toString());
                mainActivityInterface.getPreferences().setMyPreferenceInt("bracketsStyle",value);
                mainActivityInterface.getProcessSong().updateProcessingPreferences();
            }
        });
        myView.curlyBrackets.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            updateBooleanPreference("curlyBrackets",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
            displayInterface.updateDisplay("setSongContentPrefs");
        }));
        myView.curlyBracketsDevice.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            updateBooleanPreference("curlyBracketsDevice",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
            displayInterface.updateDisplay("setSongContentPrefs");
        }));
        // TODO Maybe add later?
        /*myView.addSectionBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("addSectionBox",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
        });*/
        myView.filterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("filterSections",isChecked,myView.filterLayout);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
        });
        myView.filterShow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBooleanPreference("filterShow",isChecked,null);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
        });

        // The slider
        myView.trimLineSpacingSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) { }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                // Save the new value
                float percentage = slider.getValue()/100f;
                mainActivityInterface.getPreferences().setMyPreferenceFloat("lineSpacing",percentage);
                mainActivityInterface.getProcessSong().updateProcessingPreferences();
            }
        });
        myView.trimLineSpacingSlider.addOnChangeListener((slider, value, fromUser) -> sliderValToText(value));

        // The button
        myView.filterSave.setOnClickListener(v -> {
            // Get the text from the edittext
            Log.d(TAG,"myView.filters.getText():"+myView.filters.getText());
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
                mainActivityInterface.getPreferences().setMyPreferenceString("filterText",newText);
            }
        });
        myView.pdfHorizontal.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean("pdfLandscapeView", b));
    }

    private void updateBooleanPreference(String prefName, boolean isChecked, View viewToShowHide) {
        mainActivityInterface.getPreferences().setMyPreferenceBoolean(prefName,isChecked);
        mainActivityInterface.getProcessSong().updateProcessingPreferences();
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

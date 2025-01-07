package com.garethevans.church.opensongtablet.abcnotation;

import android.content.Context;
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
import com.garethevans.church.opensongtablet.databinding.SettingsAbcnotationBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import java.util.Arrays;

public class MusicScoreFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MusicScoreFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsAbcnotationBinding myView;
    private String music_score="", website_music_score="", settings_text="",
        global_text="", song_specific_text="", inline_text="", copied_text;
    private String webAddress;
    private InlineAbcObject inlineAbcObject;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(music_score);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsAbcnotationBinding.inflate(inflater, container, false);
        prepareStrings();
        webAddress = website_music_score;

        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Get the song prefs so we can edit before saving
        mainActivityInterface.getAbcNotation().prepareSongValues(mainActivityInterface.getSong());

        // Set up the views
        setViews();

        // Set up the listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            music_score = getString(R.string.music_score);
            website_music_score = getString(R.string.website_music_score);
            settings_text = getString(R.string.settings);
            global_text = getString(R.string.global);
            song_specific_text = getString(R.string.song_specific);
            inline_text = getString(R.string.abc_inline_text);
            copied_text = getString(R.string.copied);
        }
    }
    private void setViews() {
        // Create an InlineAbcObject
        inlineAbcObject = new InlineAbcObject(getContext(),null,0,
                mainActivityInterface.getMyThemeColors().getColorInt("white"));
        inlineAbcObject.setAbcItem(0);
        inlineAbcObject.setMainColor("#000000");
        inlineAbcObject.setChordColor("#000000");

        // Identify this WebView to the object so it can deal with the logic
        inlineAbcObject.setInlineAbcWebView(myView.abcWebView);

        // Call the webview to load the html file
        inlineAbcObject.getInlineAbcWebView();

        // Make it visible and touchable as the object hides the WebView in favour of the ImageView
        myView.abcWebView.setVisibility(View.VISIBLE);
        myView.abcWebView.setAllowTouch(true);

        // Set up the edit text box
        myView.abcText.setText(mainActivityInterface.getAbcNotation().getSongAbc());
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.abcText);
        myView.abcText.setTextSize(18f);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.abcText,6);

        // Set up the popupWidth
        myView.abcPopupWidth.setValue((int)(100*mainActivityInterface.getAbcNotation().getAbcPopupWidth()));
        myView.abcPopupWidth.setLabelFormatter(value -> ((int)value)+"%");
        myView.abcPopupWidth.setHint((int)myView.abcPopupWidth.getValue()+"%");

        // Set up the include guitar tab option
        if (getContext()!=null) {
            myView.abcIncludeTab.setChecked(mainActivityInterface.getAbcNotation().getAbcIncludeTab());
            ExposedDropDownArrayAdapter tabIntrumentAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.abcInstrumentTab, R.layout.view_exposed_dropdown_item,
                    mainActivityInterface.getAbcNotation().getABCInstruments());
            myView.abcInstrumentTab.setAdapter(tabIntrumentAdapter);
            myView.abcInstrumentTab.setText(mainActivityInterface.getAbcNotation().getAbcInstrumentNice());
            setTheStringValues();
            myView.tabLayout.setVisibility(mainActivityInterface.getAbcNotation().getAbcIncludeTab() ? View.VISIBLE : View.GONE);
        }

        // Set up the inline staff width
        myView.abcInlineWidth.setValue(mainActivityInterface.getAbcNotation().getAbcInlineWidth());
        myView.abcInlineWidth.setHint(mainActivityInterface.getAbcNotation().getAbcInlineWidth()+"px");
        myView.abcInlineWidth.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return (int)value+"px";
            }
        });

        // Set up the transpose options
        myView.autoTranspose.setChecked(mainActivityInterface.getAbcNotation().getAbcAutoTranspose());
        myView.transposeSlider.setEnabled(!myView.autoTranspose.getChecked());
        myView.transposeSlider.setValue(mainActivityInterface.getAbcNotation().getSongAbcTranspose());
        myView.transposeSlider.setHint(showPositiveValue(mainActivityInterface.getAbcNotation().getSongAbcTranspose()));
        myView.transposeSlider.setLabelFormatter(value -> showPositiveValue((int) value));

        // Check for overrides
        if (mainActivityInterface.getProcessSong().getHasAbcOffOverride(mainActivityInterface.getSong())) {
            myView.overrideSettingsAbcSlider.setSliderPos(2);
        } else if (mainActivityInterface.getProcessSong().getHasAbcOnOverride(mainActivityInterface.getSong())) {
            myView.overrideSettingsAbcSlider.setSliderPos(1);
        } else {
            myView.overrideSettingsAbcSlider.setSliderPos(0);
        }

        // Update the settings text as global or song specific
        String global = settings_text + " (" + global_text + ")";
        String local = settings_text + " (" + song_specific_text + ")";
        myView.settingsAbcGlobal.setText(global);
        myView.settingsAbcLocal.setText(local);

        //  Set up autoshow abc notation
        myView.autoshowMusicScore.setChecked(mainActivityInterface.getAbcNotation().getAutoshowMusicScore());
    }

    private void setListeners() {
        myView.editABC.setOnClickListener(v -> doSave());
        myView.copyInlineAbc.setOnClickListener(view -> {
            if (getContext()!=null) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                String inlineText = myView.abcText.getText().toString();
                inlineText = ";#:" + inlineText.replace("\n","\\n");
                android.content.ClipData clip = android.content.ClipData.newPlainText("inlineAbc", inlineText);
                mainActivityInterface.getShowToast().doIt(inline_text+" - " + copied_text + ":\n\n"+inlineText);
                clipboard.setPrimaryClip(clip);
            }
        });
        myView.abcText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Update the songAbc in the object and class
                mainActivityInterface.getAbcNotation().setSongAbc(s.toString());
                inlineAbcObject.setAbcInlineText(s.toString());
                // If we are autotransposing, figure that out too and update the songAbc
                if (myView.autoTranspose.getChecked()) {
                    mainActivityInterface.getAbcNotation().getABCTransposeFromSongKey();
                }
                myView.transposeSlider.setValue(mainActivityInterface.getAbcNotation().getSongAbcTranspose());
                myView.transposeSlider.setHint(showPositiveValue(mainActivityInterface.getAbcNotation().getSongAbcTranspose()));
                // Update the webview with the new values
                inlineAbcObject.updateContent();
            }
        });

        myView.autoTranspose.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            // Update the preference
            mainActivityInterface.getAbcNotation().setAbcAutoTranspose(isChecked);
            if (isChecked) {
                mainActivityInterface.getAbcNotation().getABCTransposeFromSongKey();
                myView.transposeSlider.setValue(mainActivityInterface.getAbcNotation().getSongAbcTranspose());
                myView.transposeSlider.setHint(String.valueOf(mainActivityInterface.getAbcNotation().getSongAbcTranspose()));
            }
            myView.transposeSlider.setEnabled(!isChecked);
            // Update the webview with the new values
            inlineAbcObject.updateContent();
        }));

        myView.autoshowMusicScore.setOnCheckedChangeListener(((buttonView, isChecked) -> mainActivityInterface.getAbcNotation().setAutoshowMusicScore(isChecked)));
        myView.abcPopupWidth.addOnChangeListener((slider, value, fromUser) -> {
            myView.abcPopupWidth.setHint((int) value + "%");
            if (!fromUser) {
                mainActivityInterface.getAbcNotation().setAbcPopupWidth(value/100f);
            }
        });
        myView.abcInlineWidth.addOnChangeListener((slider, value, fromUser) -> {
            myView.abcInlineWidth.setHint((int)value+"px");
            if (!fromUser) {
                mainActivityInterface.getAbcNotation().setAbcInlineWidth((int)value);
            }
            // Update the webview with the new values
            inlineAbcObject.updateContent();
        });
        myView.transposeSlider.addOnChangeListener((slider, value, fromUser) -> {
            myView.transposeSlider.setHint(showPositiveValue((int)value));
            // Update the webview with the new values
            inlineAbcObject.updateContent();
        });
        myView.abcPopupWidth.addOnSliderTouchListener(new MySliderTouchListener("abcPopupWidth"));
        myView.abcInlineWidth.addOnSliderTouchListener(new MySliderTouchListener("abcInlineWidth"));
        myView.transposeSlider.addOnSliderTouchListener(new MySliderTouchListener("abcTranspose"));

        // The music score override
        myView.overrideSettingsAbcSlider.addOnChangeListener((slider, value, fromUser) -> {
            // All options should clear existing override value
            mainActivityInterface.getProcessSong().removeAbcOverrides(mainActivityInterface.getSong(), true);
            // Get rid of any existing sticky_off values
            mainActivityInterface.getProcessSong().removeAbcOverrides(mainActivityInterface.getSong(),false);

            if (value==1) {
                // Add the abc_on override
                mainActivityInterface.getProcessSong().addAbcOverride(
                        mainActivityInterface.getSong(),true);

            } else if (value==2) {
                // Add the abc_off override
                mainActivityInterface.getProcessSong().addAbcOverride(
                        mainActivityInterface.getSong(), false);
            }
            myView.overrideSettingsAbcSlider.updateAlphas();
            mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
        });

        myView.abcIncludeTab.setOnCheckedChangeListener((compoundButton, b) -> {
            myView.tabLayout.setVisibility(b ? View.VISIBLE:View.GONE);
            mainActivityInterface.getAbcNotation().setAbcIncludeTab(b);
            setTheStringValues();
            // Update the webview with the new values
            inlineAbcObject.updateContent();
        });
        myView.abcInstrumentTab.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String prefString = mainActivityInterface.getAbcNotation().getAbcInstrumentPrefFromNice(myView.abcInstrumentTab.getText().toString());
                mainActivityInterface.getAbcNotation().setAbcInstrumentTab(prefString);
                setTheStringValues();
                // Update the webview with the new values
                inlineAbcObject.updateContent();
            }
        });
    }

    private class MySliderTouchListener implements Slider.OnSliderTouchListener {
        private final String prefName;
        MySliderTouchListener(String prefName) {
            this.prefName = prefName;
        }
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {}

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            switch (prefName) {
                case "abcPopupWidth":
                    mainActivityInterface.getAbcNotation().setAbcPopupWidth(myView.abcPopupWidth.getValue()/100f);
                    // Update the webview with the new values
                    inlineAbcObject.updateContent();
                    break;
                case "abcTranspose":
                    // This isn't a preference, but a song specific value
                    mainActivityInterface.getAbcNotation().setSongAbcTranspose((int)myView.transposeSlider.getValue());
                    // Update the webview with the new values
                    inlineAbcObject.updateContent();
                    break;
                case "abcInlineWidth":
                    mainActivityInterface.getAbcNotation().setAbcInlineWidth((int)myView.abcInlineWidth.getValue());
                    // Update the webview with the new values
                    inlineAbcObject.updateContent();
                    break;
            }
        }
    }

    private String showPositiveValue(int value) {
        if (value>0) {
            return "+" + value;
        }
        return String.valueOf(value);
    }

    private void setTheStringValues() {
        String[] stringNotes = mainActivityInterface.getAbcNotation().getAbcInstrumentTuning();
        int numStrings = stringNotes.length;
        boolean showString = mainActivityInterface.getAbcNotation().getAbcIncludeTab();
        Log.d(TAG,"stringNotes:"+ Arrays.toString(stringNotes));
        Log.d(TAG,"numStrings:"+ numStrings);
        Log.d(TAG,"showStrings:"+showString);
        // E,A,DGBe  length=6  [0]=[6-6]=E, [1]=[6-5]=A, [2]=D [3]=G [4]=B [5]=E
        String string1 = stringNotes[stringNotes.length-1];
        String string2 = stringNotes[stringNotes.length-2];
        String string3 = stringNotes[stringNotes.length-3];
        String string4 = stringNotes[stringNotes.length-4];
        String string5 = numStrings>=5 ? stringNotes[stringNotes.length-5] : "";
        String string6 = numStrings>=6 ? stringNotes[stringNotes.length-6] : "";

        Log.d(TAG,"strings:"+string1+" "+string2+" "+string3+" "+string4+" "+string5+" "+string6);
        myView.string1Tuning.setText(string1);
        myView.string2Tuning.setText(string2);
        myView.string3Tuning.setText(string3);
        myView.string4Tuning.setText(string4);
        myView.string5Tuning.setText(string5);
        myView.string6Tuning.setText(string6);

        myView.string1Tuning.setVisibility((showString || !string1.isEmpty()) ? View.VISIBLE:View.GONE);
        myView.string2Tuning.setVisibility((showString || !string2.isEmpty()) ? View.VISIBLE:View.GONE);
        myView.string3Tuning.setVisibility((showString || !string3.isEmpty()) ? View.VISIBLE:View.GONE);
        myView.string4Tuning.setVisibility((showString || !string4.isEmpty()) ? View.VISIBLE:View.GONE);
        myView.string5Tuning.setVisibility((showString && !string5.isEmpty()) ? View.VISIBLE:View.GONE);
        myView.string6Tuning.setVisibility((showString && !string6.isEmpty()) ? View.VISIBLE:View.GONE);

        myView.tabLayout.setVisibility(showString ? View.VISIBLE:View.GONE);
    }

    private void doSave() {
        // Update the abc data
        mainActivityInterface.getAbcNotation().setSongAbc(myView.abcText.getText().toString());
        mainActivityInterface.getAbcNotation().setSongAbcTranspose((int)myView.transposeSlider.getValue());
        mainActivityInterface.getAbcNotation().saveAbcContent(mainActivityInterface,mainActivityInterface.getSong());
        mainActivityInterface.getShowToast().success();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface.setWhattodo("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

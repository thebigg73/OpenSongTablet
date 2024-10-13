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
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsAbcnotationBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class MusicScoreFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsAbcnotationBinding myView;
    private final String TAG = "MusicScoreFragment";
    private String music_score="", website_music_score="", settings_text="",
        global_text="", song_specific_text="";
    private String webAddress;

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
        mainActivityInterface.getAbcNotation().prepareSongValues(mainActivityInterface.getSong(), false);

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
        }
    }
    private void setViews() {
        mainActivityInterface.getAbcNotation().setWebView(myView.abcWebView);
        myView.abcWebView.post(() -> myView.abcWebView.addJavascriptInterface(new JsInterface(), "AndroidApp"));

        myView.abcText.setText(mainActivityInterface.getAbcNotation().getSongAbc());
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.abcText);
        myView.abcText.setTextSize(18f);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.abcText,6);

        myView.sizeSlider.setValue((int)(100*mainActivityInterface.getAbcNotation().getAbcPopupWidth()));
        myView.sizeSlider.setLabelFormatter(value -> ((int)value)+"%");
        myView.sizeSlider.setHint((int)myView.sizeSlider.getValue()+"%");

        myView.zoomSlider.setValue(mainActivityInterface.getAbcNotation().getAbcZoom());
        myView.zoomSlider.setHint(String.valueOf((int)myView.zoomSlider.getValue()));
        myView.zoomSlider.setLabelFormatter(value -> String.valueOf((int)value));

        myView.autoTranspose.setChecked(mainActivityInterface.getAbcNotation().getAbcAutoTranspose());

        myView.transposeSlider.setEnabled(!myView.autoTranspose.getChecked());
        myView.transposeSlider.setValue(mainActivityInterface.getAbcNotation().getSongAbcTranspose());
        myView.transposeSlider.setHint(showPositiveValue(mainActivityInterface.getAbcNotation().getSongAbcTranspose()));
        myView.transposeSlider.setLabelFormatter(value -> showPositiveValue((int)value));

        // Check for overrides
        if (mainActivityInterface.getProcessSong().getHasAbcOffOverride(mainActivityInterface.getSong())) {
            myView.overrideSettingsAbcSlider.setSliderPos(2);
        } else if (mainActivityInterface.getProcessSong().getHasAbcOnOverride(mainActivityInterface.getSong())) {
            myView.overrideSettingsAbcSlider.setSliderPos(1);
        } else {
            myView.overrideSettingsAbcSlider.setSliderPos(0);
        }

        String global = settings_text + " (" + global_text + ")";
        String local = settings_text + " (" + song_specific_text + ")";
        myView.settingsAbcGlobal.setText(global);
        myView.settingsAbcLocal.setText(local);

        myView.autoshowMusicScore.setChecked(mainActivityInterface.getAbcNotation().getAutoshowMusicScore());
    }

    private void setListeners() {
        myView.editABC.setOnClickListener(v -> doSave());
        myView.nestedScrollView.setExtendedFabToAnimate(myView.editABC);
        myView.abcText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Update the songAbc
                mainActivityInterface.getAbcNotation().setSongAbc(s.toString());
                // If we are autotransposing, figure that out too and update the songAbc
                if (myView.autoTranspose.getChecked()) {
                    mainActivityInterface.getAbcNotation().getABCTransposeFromSongKey();
                }
                myView.transposeSlider.setValue(mainActivityInterface.getAbcNotation().getSongAbcTranspose());
                myView.transposeSlider.setHint(showPositiveValue(mainActivityInterface.getAbcNotation().getSongAbcTranspose()));
                // Update the webview with the new values
                mainActivityInterface.getAbcNotation().updateWebView(myView.abcWebView);
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
            mainActivityInterface.getAbcNotation().updateWebView(myView.abcWebView);
        }));

        myView.autoshowMusicScore.setOnCheckedChangeListener(((buttonView, isChecked) -> mainActivityInterface.getAbcNotation().setAutoshowMusicScore(isChecked)));
        myView.sizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            myView.sizeSlider.setHint((int)value+"%");
            // Update the webview with the new values
            mainActivityInterface.getAbcNotation().updateWebView(myView.abcWebView);
        });
        myView.zoomSlider.addOnChangeListener((slider, value, fromUser) -> {
            myView.zoomSlider.setHint(String.valueOf((int)value));
            // Update the webview with the new values
            mainActivityInterface.getAbcNotation().updateWebView(myView.abcWebView);
        });

        myView.transposeSlider.addOnChangeListener((slider, value, fromUser) -> {
            myView.transposeSlider.setHint(showPositiveValue((int)value));
            // Update the webview with the new values
            mainActivityInterface.getAbcNotation().updateWebView(myView.abcWebView);
        });
        myView.sizeSlider.addOnSliderTouchListener(new MySliderTouchListener("abcPopupWidth"));
        myView.zoomSlider.addOnSliderTouchListener(new MySliderTouchListener("abcZoom"));
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
                    mainActivityInterface.getAbcNotation().setAbcPopupWidth(myView.sizeSlider.getValue()/100f);
                    break;
                case "abcZoom":
                    mainActivityInterface.getAbcNotation().setAbcZoom((int)myView.zoomSlider.getValue());
                    mainActivityInterface.getAbcNotation().updateZoom(myView.abcWebView);
                    break;
                case "abcTranspose":
                    // This isn't a preference, but a song specific value
                    mainActivityInterface.getAbcNotation().setSongAbcTranspose((int)myView.transposeSlider.getValue());
                    mainActivityInterface.getAbcNotation().updateWebView(myView.abcWebView);
                    break;
            }
        }
    }

    // This bit is triggered from the Save button
    private class JsInterface {
        @JavascriptInterface
        public void receiveString(String myJsString) {
            Log.d(TAG, "string: " + myJsString);
        }


        @JavascriptInterface
        public void checkKey(String abcText) {
            Log.d(TAG,"checkKey called");
        }
    }

    private String showPositiveValue(int value) {
        if (value>0) {
            return "+" + value;
        }
        return String.valueOf(value);
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

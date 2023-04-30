package com.garethevans.church.opensongtablet.chords;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsChordsFormatBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class ChordFormatFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ChordFormatFrag";

    private SettingsChordsFormatBinding myView;
    private ArrayList<String> chordFormats, chordFormatNames;
    private MainActivityInterface mainActivityInterface;
    private int formattouse;
    private boolean usepreferred;
    private String chord_settings_string="", website_chords_settings_string="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(chord_settings_string);
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
        myView = SettingsChordsFormatBinding.inflate(inflater,container,false);

        prepareStrings();

        webAddress = website_chords_settings_string;

        // Set the initial values
        setValues();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            chord_settings_string = getString(R.string.chord_settings);
            website_chords_settings_string = getString(R.string.website_chords_settings);
        }
    }
    private void setValues() {
        myView.displayChords.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "displayChords", true));
        showHideView(myView.capoChords,myView.displayChords.isChecked());
        showHideView(myView.capoStyle,myView.displayChords.isChecked());
        myView.capoStyle.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "capoInfoAsNumerals", false));
        setCapoChordSlider();
        myView.onscreenCapoHide.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "onscreenCapoHide",true));
        myView.sliderAb.setSliderPos(setSwitchSliderFromPref("prefKey_Ab",true));
        myView.sliderBb.setSliderPos(setSwitchSliderFromPref("prefKey_Bb",true));
        myView.sliderDb.setSliderPos(setSwitchSliderFromPref("prefKey_Db",true));
        myView.sliderEb.setSliderPos(setSwitchSliderFromPref("prefKey_Eb",true));
        myView.sliderGb.setSliderPos(setSwitchSliderFromPref("prefKey_Gb",true));
        myView.sliderAbm.setSliderPos(setSwitchSliderFromPref("prefKey_Abm",false));
        myView.sliderBbm.setSliderPos(setSwitchSliderFromPref("prefKey_Bbm",true));
        myView.sliderDbm.setSliderPos(setSwitchSliderFromPref("prefKey_Dbm",false));
        myView.sliderEbm.setSliderPos(setSwitchSliderFromPref("prefKey_Ebm",true));
        myView.sliderGbm.setSliderPos(setSwitchSliderFromPref("prefKey_Gbm",false));

        usepreferred = mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "chordFormatUsePreferred",false);
        myView.assumePreferred.setChecked(usepreferred);
        showHideView(myView.chooseFormatLinearLayout,myView.assumePreferred.getSwitch().isChecked());
        showHideView(myView.autoChange,myView.assumePreferred.getSwitch().isChecked());
        formattouse = mainActivityInterface.getPreferences().getMyPreferenceInt("chordFormat",1);

        chordFormats = mainActivityInterface.getTranspose().getChordFormatAppearances();
        chordFormatNames = mainActivityInterface.getTranspose().getChordFormatNames();

        if (getContext()!=null) {
            ExposedDropDownArrayAdapter formatAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.choosePreferredFormat, R.layout.view_exposed_dropdown_item, chordFormatNames);
            myView.choosePreferredFormat.setAdapter(formatAdapter);
        }
        myView.choosePreferredFormat.setText(chordFormatNames.get(formattouse-1));
        myView.chosenPreferredFormat.setText(null);
        myView.chosenPreferredFormat.setHint(chordFormats.get(formattouse-1));

        myView.autoChange.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "chordFormatAutoChange", false));

        fixChordPrefText();
    }

    private void fixChordPrefText() {
        if (formattouse==2) {
            setChordPrefText(new String[] {"Ab", "G#", "B", "A#", "Db", "C#", "Eb", "D#", "Gb", "F#",
                    "ab", "g#", "b", "a#", "db", "c#", "eb", "d#", "gb", "f#"});
        } else if (formattouse==3) {
            setChordPrefText(new String[]{"As", "Gis", "B", "Ais", "Des", "Cis", "Es", "Dis", "Ges", "Fis",
                    "as", "gis", "b", "ais", "des", "cis", "es", "dis", "ges", "fis"});
        } else if (formattouse==4) {
            setChordPrefText(new String[] {"LAb","SOL#","SIb","LA#","REb", "DO#", "MIb", "RE#", "SOLb","FA#",
                    "LAbm","SOL#m","SIbm","LA#m","REbm","DO#m","MIbm","RE#m","SOLbm","FA#m"});
        } else if (formattouse==7) {
            setChordPrefText(new String[]{"As", "Gis", "Bes", "Ais", "Des", "Cis", "Es", "Dis", "Ges", "Fis",
                    "as", "gis", "bes", "ais", "des", "cis", "es", "dis", "ges", "fis"});
        } else {
            setChordPrefText(new String[] {"Ab", "G#", "Bb","A#", "Db", "C#", "Eb", "D#", "Gb", "F#",
                    "Abm","G#m","Bbm","A#m","Dbm","C#m","Ebm","D#m","Gbm","F#m"});
        }
        if (formattouse==5 || formattouse==6) {
            myView.prefSliders.setVisibility(View.GONE);
        } else {
            myView.prefSliders.setVisibility(View.VISIBLE);
        }
    }

    private void setChordPrefText(String[] newText) {
        myView.sliderAb.setTextLeft(newText[0]);
        myView.sliderAb.setTextRight(newText[1]);
        myView.sliderBb.setTextLeft(newText[2]);
        myView.sliderBb.setTextRight(newText[3]);
        myView.sliderDb.setTextLeft(newText[4]);
        myView.sliderDb.setTextRight(newText[5]);
        myView.sliderEb.setTextLeft(newText[6]);
        myView.sliderEb.setTextRight(newText[7]);
        myView.sliderGb.setTextLeft(newText[8]);
        myView.sliderGb.setTextRight(newText[9]);

        myView.sliderAbm.setTextLeft(newText[10]);
        myView.sliderAbm.setTextRight(newText[11]);
        myView.sliderBbm.setTextLeft(newText[12]);
        myView.sliderBbm.setTextRight(newText[13]);
        myView.sliderDbm.setTextLeft(newText[14]);
        myView.sliderDbm.setTextRight(newText[15]);
        myView.sliderEbm.setTextLeft(newText[16]);
        myView.sliderEbm.setTextRight(newText[17]);
        myView.sliderGbm.setTextLeft(newText[18]);
        myView.sliderGbm.setTextRight(newText[19]);
    }

    private void setListeners() {
        myView.displayChords.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                    "displayChords", b);
            mainActivityInterface.getProcessSong().updateProcessingPreferences();
            showHideView(myView.capoChords,b);
            showHideView(myView.capoStyle,b);
        });
        myView.capoChords.addOnChangeListener((slider, value, fromUser) -> {
            if (value==2) {
                mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                        "displayCapoAndNativeChords",true);
            } else if (value==1) {
                mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                        "displayCapoChords",true);
            } else {
                mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                        "displayCapoAndNativeChords",false);
                mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                        "displayCapoChords",false);
            }
        });
        myView.capoStyle.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.
                getPreferences().setMyPreferenceBoolean(
                "capoInfoAsNumerals", b));
        myView.onscreenCapoHide.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                    "onscreenCapoHide",b);
            mainActivityInterface.
                    updateOnScreenInfo("setpreferences");
        });
        myView.sliderAb.addOnChangeListener(new MySliderChangeListener("prefKey_Ab"));
        myView.sliderBb.addOnChangeListener(new MySliderChangeListener("prefKey_Bb"));
        myView.sliderDb.addOnChangeListener(new MySliderChangeListener("prefKey_Db"));
        myView.sliderEb.addOnChangeListener(new MySliderChangeListener("prefKey_Eb"));
        myView.sliderGb.addOnChangeListener(new MySliderChangeListener("prefKey_Gb"));
        myView.sliderAbm.addOnChangeListener(new MySliderChangeListener("prefKey_Abm"));
        myView.sliderBbm.addOnChangeListener(new MySliderChangeListener("prefKey_Bbm"));
        myView.sliderDbm.addOnChangeListener(new MySliderChangeListener("prefKey_Dbm"));
        myView.sliderEbm.addOnChangeListener(new MySliderChangeListener("prefKey_Ebm"));
        myView.sliderGbm.addOnChangeListener(new MySliderChangeListener("prefKey_Gbm"));

        myView.assumePreferred.getSwitch().setOnCheckedChangeListener((compoundButton, b) -> {
            usepreferred = b;
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                    "chordFormatUsePreferred", b);
            showHideView(myView.chooseFormatLinearLayout,b);
            showHideView(myView.autoChange,b);
        });
        myView.autoChange.getSwitch().setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                "chordFormatAutoChange", b));
        myView.choosePreferredFormat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                formattouse = chordFormatNames.indexOf(editable.toString())+1;
                mainActivityInterface.getPreferences().setMyPreferenceInt(
                        "chordFormat", formattouse);
                myView.chosenPreferredFormat.setHint(chordFormats.get(formattouse-1));
                fixChordPrefText();
            }
        });
    }

    private int setSwitchSliderFromPref(String prefName, boolean defaultValue) {
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                prefName,defaultValue)) {
            return 0;
        } else {
            return 1;
        }
    }

    private void showHideView(View view, boolean show) {
        if (show) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void setCapoChordSlider() {
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "displayCapoAndNativeChords",false)) {
            myView.capoChords.setSliderPos(2);
        } else if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "displayCapoChords",true)) {
            myView.capoChords.setSliderPos(1);
        } else {
            myView.capoChords.setSliderPos(0);
        }
    }

    private class MySliderChangeListener implements Slider.OnChangeListener{

        private final String prefName;

        MySliderChangeListener(String prefName) {
            this.prefName = prefName;
        }

        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                    prefName, slider.getValue()==0);
        }
    }
}

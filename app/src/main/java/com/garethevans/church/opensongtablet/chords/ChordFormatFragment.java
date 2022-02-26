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

    private SettingsChordsFormatBinding myView;
    private ArrayList<String> chordFormats, chordFormatNames;
    MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsChordsFormatBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.chord_settings));

        // Set the initial values
        setValues();

        // Set the listeners
        setListeners();

        return myView.getRoot();
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

        myView.assumePreferred.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "chordFormatUsePreferred",false));
        showHideView(myView.chooseFormatLinearLayout,myView.assumePreferred.getSwitch().isChecked());
        showHideView(myView.autoChange,myView.assumePreferred.getSwitch().isChecked());
        int formattouse = mainActivityInterface.getPreferences().getMyPreferenceInt("chordFormat",1);

        chordFormats = new ArrayList<>();
        chordFormats.add(getString(R.string.chordformat_1));
        chordFormats.add(getString(R.string.chordformat_2));
        chordFormats.add(getString(R.string.chordformat_3));
        chordFormats.add(getString(R.string.chordformat_4));
        chordFormats.add(getString(R.string.chordformat_5));
        chordFormats.add(getString(R.string.chordformat_6));

        chordFormatNames = new ArrayList<>();
        chordFormatNames.add(getString(R.string.chordformat_1_name));
        chordFormatNames.add(getString(R.string.chordformat_2_name));
        chordFormatNames.add(getString(R.string.chordformat_3_name));
        chordFormatNames.add(getString(R.string.chordformat_4_name));
        chordFormatNames.add(getString(R.string.chordformat_5_name));
        chordFormatNames.add(getString(R.string.chordformat_6_name));

        ExposedDropDownArrayAdapter formatAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                myView.choosePreferredFormat,R.layout.view_exposed_dropdown_item,chordFormatNames);
        myView.choosePreferredFormat.setAdapter(formatAdapter);
        myView.choosePreferredFormat.setText(chordFormatNames.get(formattouse-1));
        myView.chosenPreferredFormat.setText(null);
        myView.chosenPreferredFormat.setHint(chordFormats.get(formattouse-1));

        myView.autoChange.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                "chordFormatAutoChange", false));

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
                int pos = chordFormatNames.indexOf(editable.toString());
                mainActivityInterface.getPreferences().setMyPreferenceInt(
                        "chordFormat", pos+1);
                myView.chosenPreferredFormat.setHint(chordFormats.get(pos));
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

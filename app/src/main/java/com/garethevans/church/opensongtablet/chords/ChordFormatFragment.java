package com.garethevans.church.opensongtablet.chords;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsChordsFormatBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class ChordFormatFragment extends Fragment {

    private SettingsChordsFormatBinding myView;
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
                requireContext(),"displayChords", true));
        showHideView(myView.capoChords,myView.displayChords.isChecked());
        showHideView(myView.capoStyle,myView.displayChords.isChecked());
        myView.capoStyle.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                requireContext(),"capoInfoAsNumerals", false));
        setCapoChordSlider();

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
                requireContext(),"chordFormatUsePreferred",false));
        showHideView(myView.chordFormatLayout,myView.assumePreferred.isChecked());
        int formattouse = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(),"chordFormat",1);
        switch (formattouse) {
            case 0:
                myView.chordFormat0.setChecked(true);
                break;
            case 1:
                myView.chordFormat1.setChecked(true);
                break;
            case 2:
                myView.chordFormat2.setChecked(true);
                break;
            case 3:
                myView.chordFormat3.setChecked(true);
                break;
            case 4:
                myView.chordFormat4.setChecked(true);
                break;
            case 5:
                myView.chordFormat5.setChecked(true);
                break;
            case 6:
                myView.chordFormat6.setChecked(true);
                break;
        }
    }

    private void setListeners() {
        myView.displayChords.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),
                    "displayChords", b);
            showHideView(myView.capoChords,b);
        });
        myView.capoChords.addOnChangeListener((slider, value, fromUser) -> {
            if (value==2) {
                mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                        requireContext(),"displayCapoAndNativeChords",true);
            } else if (value==1) {
                mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                        requireContext(),"displayCapoChords",true);
            } else {
                mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                        requireContext(),"displayCapoAndNativeChords",false);
                mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                        requireContext(),"displayCapoChords",false);
            }
        });
        myView.capoStyle.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.
                getPreferences().setMyPreferenceBoolean(requireContext(),
                "capoInfoAsNumerals", b));
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

        myView.assumePreferred.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                    requireContext(), "chordFormatUsePreferred", b);
            showHideView(myView.chordFormat,b);
        });

        myView.chordFormat0.setOnCheckedChangeListener(new MyRadioListener(0));
        myView.chordFormat1.setOnCheckedChangeListener(new MyRadioListener(1));
        myView.chordFormat2.setOnCheckedChangeListener(new MyRadioListener(2));
        myView.chordFormat3.setOnCheckedChangeListener(new MyRadioListener(3));
        myView.chordFormat4.setOnCheckedChangeListener(new MyRadioListener(4));
        myView.chordFormat5.setOnCheckedChangeListener(new MyRadioListener(5));
    }

    private int setSwitchSliderFromPref(String prefName, boolean defaultValue) {
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),
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
                requireContext(), "displayCapoAndNativeChords",false)) {
            myView.capoChords.setSliderPos(2);
        } else if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                requireContext(), "displayCapoChords",true)) {
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
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),
                    prefName, slider.getValue()==0);
        }
    }

    private class MyRadioListener implements CompoundButton.OnCheckedChangeListener {

        private final int chordFormat;

        MyRadioListener(int chordFormat) {
            this.chordFormat = chordFormat;
        }
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                mainActivityInterface.getPreferences().setMyPreferenceInt(
                        requireContext(),"chordFormat", chordFormat);
            }
        }
    }
}

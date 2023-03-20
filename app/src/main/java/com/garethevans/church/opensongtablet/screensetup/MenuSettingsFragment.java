package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MaterialSlider;
import com.garethevans.church.opensongtablet.databinding.SettingsMenuBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class MenuSettingsFragment extends Fragment {

    SettingsMenuBinding myView;
    MainActivityInterface mainActivityInterface;
    private String off_string;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsMenuBinding.inflate(inflater,container,false);

        if (getContext()!=null) {
            mainActivityInterface.updateToolbar(getString(R.string.menu_settings));
            mainActivityInterface.updateToolbarHelp(getString(R.string.website_menu_settings));
            off_string = getString(R.string.off);
        }

        // Deal with the views
        setupViews();
        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        // Get the user preferences
        boolean showAlphabetical = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean("songMenuAlphaIndexShow",true);
        float songMenuItemSize = mainActivityInterface.getPreferences().
                getMyPreferenceFloat("songMenuItemSize",14.0f);
        float songMenuSubItemSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuSubItemSize",12.0f);
        float songMenuAlphaIndexSize = mainActivityInterface.getPreferences().
                getMyPreferenceFloat("songMenuAlphaIndexSize",12.0f);
        boolean showTickBoxes = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean("songMenuSetTicksShow",true);
        boolean sortByTitles = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles",true);
        boolean songMenuAlphaIndexLevel2 = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuAlphaIndexLevel2",false);

        // Set those values into the views
        myView.songMenuItemSize.setLabelFormatter(value -> (int)value + "sp");

        myView.songMenuItemSize.setValue(songMenuItemSize);
        myView.songMenuItemSize.setHint((int)songMenuItemSize + "sp");
        myView.songMenuItemSize.setHintTextSize(songMenuItemSize);

        myView.songMenuSubItemSize.setLabelFormatter(value -> {
            if (value==7) {
                return off_string;
            } else {
                return (int)value + "sp";
            }
        });
        myView.songMenuSubItemSize.setValue(songMenuSubItemSize);
        if (songMenuSubItemSize==7) {
            // The 'off' option
            myView.songMenuSubItemSize.setHint(off_string);
            myView.songMenuSubItemSize.setHintTextSize(14);
        } else {
            myView.songMenuSubItemSize.setHint((int) songMenuSubItemSize + "sp");
            myView.songMenuSubItemSize.setHintTextSize(songMenuSubItemSize);
        }

        myView.largePopups.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("largePopups",true));

        myView.songAlphabeticalShow.setChecked(showAlphabetical);

        myView.level2Index.setChecked(songMenuAlphaIndexLevel2);

        myView.songAlphabeticalSize.setValue(songMenuAlphaIndexSize);
        myView.songAlphabeticalSize.setHint((int)songMenuAlphaIndexSize+"sp");
        myView.songAlphabeticalSize.setLabelFormatter(value -> (int)value+"sp");
        myView.songAlphabeticalSize.setHintTextSize(songMenuAlphaIndexSize);
        myView.songMenuCheckboxes.setChecked(showTickBoxes);

        if (sortByTitles) {
            myView.songMenuOrder.setSliderPos(1);
        } else {
            myView.songMenuOrder.setSliderPos(0);
        }

        showHideSize(showAlphabetical);
    }

    private void setupListeners() {
        myView.songMenuCheckboxes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songMenuSetTicksShow", isChecked);
            // Try to update the song menu
            mainActivityInterface.updateSongMenu("menuSettingsFragment",null, null);
        });
        myView.songAlphabeticalShow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songMenuAlphaIndexShow",isChecked);
            showHideSize(isChecked);
            // Try to update the song menu
            mainActivityInterface.updateSongMenu("menuSettingsFragment",null, null);
        });
        myView.level2Index.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean("songMenuAlphaIndexLevel2",isChecked));

        myView.songMenuItemSize.addOnChangeListener(new MyChangeSlider(myView.songMenuItemSize));
        myView.songMenuSubItemSize.addOnChangeListener(new MyChangeSlider(myView.songMenuSubItemSize));
        myView.songAlphabeticalSize.addOnChangeListener(new MyChangeSlider(myView.songAlphabeticalSize));

        myView.songMenuItemSize.addOnSliderTouchListener(new MySliderTouch(myView.songMenuItemSize,"songMenuItemSize"));
        myView.songMenuSubItemSize.addOnSliderTouchListener(new MySliderTouch(myView.songMenuSubItemSize,"songMenuSubItemSize"));
        myView.songAlphabeticalSize.addOnSliderTouchListener(new MySliderTouch(myView.songAlphabeticalSize,"songMenuAlphaIndexSize"));

        myView.songMenuOrder.addOnChangeListener((slider, value, fromUser) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songMenuSortTitles",value==1);
            mainActivityInterface.updateSongMenu("",null, null);
        });
        myView.largePopups.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean("largePopups",isChecked));
    }

    private void showHideSize(boolean show) {
        // If we are showing alphabetical lists, we can show the size and level2 layout, if not, hide them
        if (show) {
            myView.level2Index.setVisibility(View.VISIBLE);
            myView.songAlphabeticalSize.setVisibility(View.VISIBLE);
        } else {
            myView.level2Index.setVisibility(View.GONE);
            myView.songAlphabeticalSize.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }

    private class MyChangeSlider implements Slider.OnChangeListener {

        final MaterialSlider materialSlider;
        MyChangeSlider(MaterialSlider materialSlider) {
            this.materialSlider = materialSlider;
        }
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            if (materialSlider==myView.songMenuSubItemSize && value==7) {
                myView.songMenuSubItemSize.setHint(off_string);
                myView.songMenuSubItemSize.setHintTextSize(14);
            } else {
                materialSlider.setHint((int) value + "sp");
                materialSlider.setHintTextSize(value);
            }
        }
    }

    private class MySliderTouch implements Slider.OnSliderTouchListener {
        String prefName;
        MaterialSlider materialSlider;
        MySliderTouch(MaterialSlider materialSlider, String prefName) {
            this.prefName = prefName;
            this.materialSlider = materialSlider;
        }
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {}

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            float myVal = materialSlider.getValue();
            mainActivityInterface.getPreferences().setMyPreferenceFloat(prefName, myVal);
            // Try to update the song menu
            mainActivityInterface.updateSongMenu("menuSettingsFragment",null,null);
        }
    }
}

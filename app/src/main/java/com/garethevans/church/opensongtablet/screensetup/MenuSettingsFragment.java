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
import com.garethevans.church.opensongtablet.databinding.SettingsMenuBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class MenuSettingsFragment extends Fragment {

    SettingsMenuBinding myView;
    MainActivityInterface mainActivityInterface;

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
        }

        // Deal with the views
        setupViews();
        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        boolean showAlphabetical = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean("songMenuAlphaIndexShow",true);
        float itemFontSize = mainActivityInterface.getPreferences().
                getMyPreferenceFloat("songMenuItemSize",12.0f);
        float alphaFontSize = mainActivityInterface.getPreferences().
                getMyPreferenceFloat("songMenuAlphaIndexSize",12.0f);
        boolean showTickBoxes = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean("songMenuSetTicksShow",true);
        boolean sortByTitles = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles",true);
        boolean songMenuAlphaIndexLevel2 = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuAlphaIndexLevel2",false);
        myView.songMenuItemSize.setLabelFormatter(value -> (int)value+"sp");
        myView.songMenuItemSize.setValue(itemFontSize);
        myView.songMenuItemSize.setHint((int)itemFontSize + "sp");
        myView.songMenuItemSize.setHintTextSize(itemFontSize);
        myView.largePopups.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("largePopups",true));
        myView.songAlphabeticalShow.setChecked(showAlphabetical);
        myView.level2Index.setChecked(songMenuAlphaIndexLevel2);
        myView.songAlphabeticalSize.setValue(alphaFontSize);
        myView.songAlphabeticalSize.setHint((int)alphaFontSize+"sp");
        myView.songAlphabeticalSize.setLabelFormatter(value -> (int)value+"sp");
        myView.songAlphabeticalSize.setHintTextSize(alphaFontSize);
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
        myView.songMenuItemSize.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                float myVal = myView.songMenuItemSize.getValue();
                mainActivityInterface.getPreferences().setMyPreferenceFloat("songMenuItemSize", myVal);
                // Try to update the song menu
                mainActivityInterface.updateSongMenu("menuSettingsFragment",null,null);
            }
        });
        myView.songMenuItemSize.addOnChangeListener((slider, value, fromUser) -> {
            myView.songMenuItemSize.setHint((int)value + "sp");
            myView.songMenuItemSize.setHintTextSize(value);
        });
        myView.songAlphabeticalSize.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) { }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                float myVal = myView.songAlphabeticalSize.getValue();
                mainActivityInterface.getPreferences().setMyPreferenceFloat("songMenuAlphaIndexSize", myVal);
                // Try to update the song menu
                mainActivityInterface.updateSongMenu("menuSettingsFragment",null, null);
            }
        });
        myView.songAlphabeticalSize.addOnChangeListener((slider, value, fromUser) -> {
            myView.songAlphabeticalSize.setHint((int)value+"sp");
            myView.songAlphabeticalSize.setHintTextSize(value);
        });
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
}

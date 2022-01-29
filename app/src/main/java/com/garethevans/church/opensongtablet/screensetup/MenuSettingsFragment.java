package com.garethevans.church.opensongtablet.screensetup;

import android.annotation.SuppressLint;
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

        mainActivityInterface.updateToolbar(getString(R.string.menu_settings));

        // Deal with the views
        setupViews();
        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        boolean showAlphabetical = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean(requireContext(),"songMenuAlphaIndexShow",true);
        float fontSize = mainActivityInterface.getPreferences().
                getMyPreferenceFloat(requireContext(),"songMenuAlphaIndexSize",12.0f);
        boolean showTickBoxes = mainActivityInterface.getPreferences().
                getMyPreferenceBoolean(requireContext(),"songMenuSetTicksShow",true);

        myView.songAlphabeticalShow.setChecked(showAlphabetical);
        myView.songAlphabeticalSize.setValue(fontSize);
        myView.songAlphabeticalSize.setHint(fontSize+"px");
        myView.songMenuCheckboxes.setChecked(showTickBoxes);

        showHideSize(showAlphabetical);
    }

    private void setupListeners() {
        myView.songMenuCheckboxes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(), "songMenuSetTicksShow", isChecked);
            // Try to update the song menu
            mainActivityInterface.updateSongMenu("menuSettingsFragment",null, null);
        });
        myView.songAlphabeticalShow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"songMenuAlphaIndexShow",isChecked);
            showHideSize(isChecked);
            // Try to update the song menu
            mainActivityInterface.updateSongMenu("menuSettingsFragment",null, null);
        });
        myView.songAlphabeticalSize.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) { }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                float myVal = myView.songAlphabeticalSize.getValue();
                mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(),"songMenuAlphaIndexSize", myVal);
                // Try to update the song menu
                mainActivityInterface.updateSongMenu("menuSettingsFragment",null, null);
            }
        });
        myView.songAlphabeticalSize.addOnChangeListener((slider, value, fromUser) -> myView.songAlphabeticalSize.setHint(value+"px"));
    }

    private void showHideSize(boolean show) {
        // If we are showing check boxes, we can show the size layout, if not, hide it
        if (show) {
            myView.songAlphabeticalSize.setVisibility(View.VISIBLE);
        } else {
            myView.songAlphabeticalSize.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

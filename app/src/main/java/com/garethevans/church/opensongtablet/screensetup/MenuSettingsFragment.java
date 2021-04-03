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
import com.garethevans.church.opensongtablet.databinding.SettingsMenuBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

public class MenuSettingsFragment extends Fragment {

    SettingsMenuBinding myView;
    Preferences preferences;
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

        mainActivityInterface.updateToolbar(null,getString(R.string.menu_settings));

        // Initialise the helpers
        setHelpers();

        // Deal with the views
        setupViews();
        setupListeners();

        return myView.getRoot();
    }

    private void setHelpers() {
        preferences = mainActivityInterface.getPreferences();
    }

    private void setupViews() {
        boolean showAlphabetical = preferences.
                getMyPreferenceBoolean(requireContext(),"songMenuAlphaIndexShow",true);
        float fontSize = preferences.
                getMyPreferenceFloat(requireContext(),"songMenuAlphaIndexSize",12.0f);
        boolean showTickBoxes = preferences.
                getMyPreferenceBoolean(requireContext(),"songMenuSetTicksShow",true);

        myView.songAlphabeticalShow.setChecked(showAlphabetical);
        myView.songAlphabeticalSize.setProgress(fontFloatToProgress(fontSize));
        myView.songMenuCheckboxes.setChecked(showTickBoxes);

        showHideSize(showAlphabetical);
    }

    private void setupListeners() {
        myView.songMenuCheckboxes.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.setMyPreferenceBoolean(requireContext(),"songMenuSetTicksShow",isChecked));
        myView.songAlphabeticalShow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMyPreferenceBoolean(requireContext(),"songMenuAlphaIndexShow",isChecked);
            showHideSize(isChecked);
        });
        myView.songAlphabeticalSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = myView.songAlphabeticalSize.getProgress();
                preferences.getMyPreferenceFloat(requireContext(),"songMenuAlphaIndexSize", progressToFontFloat(progress));
            }
        });
    }


    private float progressToFontFloat(int progress) {
        // The smallest font size allowed is 6dp, the max 22dp = size 16
        return (float)(progress + 6);
    }

    private int fontFloatToProgress(float fontSize) {
        // The smallest font size allowed is 6dp, the max 22dp = size 16
        return (int)(fontSize - 6);
    }

    private void showHideSize(boolean show) {
        // If we are showing check boxes, we can show the size layout, if not, hide it
        if (show) {
            myView.songAlphabeticalSizeLayout.setVisibility(View.VISIBLE);
        } else {
            myView.songAlphabeticalSizeLayout.setVisibility(View.GONE);
        }
    }
}

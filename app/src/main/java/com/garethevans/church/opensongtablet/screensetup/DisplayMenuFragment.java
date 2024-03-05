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
import com.garethevans.church.opensongtablet.databinding.SettingsDisplayBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class DisplayMenuFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsDisplayBinding myView;
    private String display_string="", theme_dark_string="", theme_light_string="",
            theme_custom1_string="", theme_custom2_string="", deeplink_inlineset_string="",
            deeplink_margins_string="";

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(display_string);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsDisplayBinding.inflate(inflater,container,false);

        prepareStrings();

        // Set defaults
        setDefaults();

        // Set menu listeners
        setListeners();
        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            display_string = getString(R.string.display);
            theme_dark_string = getString(R.string.theme_dark);
            theme_light_string = getString(R.string.theme_light);
            theme_custom1_string = getString(R.string.theme_custom1);
            theme_custom2_string = getString(R.string.theme_custom2);
            deeplink_inlineset_string = getString(R.string.deeplink_inlineset);
            deeplink_margins_string = getString(R.string.deeplink_margins);
        }
    }
    private void setDefaults() {
        // Get the app theme
        String themeName;
        switch (mainActivityInterface.getPreferences().getMyPreferenceString("appTheme","dark")) {
            case "dark":
            default:
                themeName = theme_dark_string;
                break;
            case "light":
                themeName = theme_light_string;
                break;
            case "custom1":
                themeName = theme_custom1_string;
                break;
            case "custom2":
                themeName = theme_custom2_string;
                break;
        }
        myView.themeButton.setHint(themeName);
        myView.hardwareAcceleration.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("hardwareAcceleration",true));
    }

    private void setListeners() {
        myView.themeButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.themeSetupFragment));
        myView.fontButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.fontSetupFragment));
        myView.extraSettings.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.displayExtraFragment));
        myView.connectedDisplay.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.connectedDisplayFragment));
        myView.actionBarSettings.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.actionBarSettingsFragment));
        myView.menuSettings.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.menuSettingsFragment));
        myView.scalingButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.displayScalingFragment));
        myView.inlineSet.setOnClickListener(v -> mainActivityInterface.navigateToFragment(deeplink_inlineset_string,0));
        myView.margins.setOnClickListener(v -> mainActivityInterface.navigateToFragment(deeplink_margins_string,0));
        myView.hardwareAcceleration.setOnCheckedChangeListener((compoundButton, b) -> {
            // Save the preference
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("hardwareAcceleration",b);
            // Update the setting in the main activity
            mainActivityInterface.recreateActivity();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

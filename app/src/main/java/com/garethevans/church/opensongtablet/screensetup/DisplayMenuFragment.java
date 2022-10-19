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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsDisplayBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(getString(R.string.display));

        // Set defaults
        setDefaults();

        // Set menu listeners
        setListeners();
        return myView.getRoot();
    }

    private void setDefaults() {
        // Get the app theme
        String themeName;
        switch (mainActivityInterface.getPreferences().getMyPreferenceString("appTheme","dark")) {
            case "dark":
            default:
                themeName = getString(R.string.theme_dark);
                break;
            case "light":
                themeName = getString(R.string.theme_light);
                break;
            case "custom1":
                themeName = getString(R.string.theme_custom1);
                break;
            case "custom2":
                themeName = getString(R.string.theme_custom2);
                break;
        }
        myView.themeButton.setHint(themeName);
    }

    private void setListeners() {
        myView.themeButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.themeSetupFragment));
        myView.fontButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.fontSetupFragment));
        myView.extraSettings.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.displayExtraFragment));
        myView.connectedDisplay.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.connectedDisplayFragment));
        myView.actionBarSettings.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.actionBarSettingsFragment));
        myView.menuSettings.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.menuSettingsFragment));
        myView.scalingButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.displayScalingFragment));
        myView.inlineSet.setOnClickListener(v -> mainActivityInterface.navigateToFragment(getString(R.string.deeplink_inlineset),0));
        myView.margins.setOnClickListener(v -> mainActivityInterface.navigateToFragment(getString(R.string.deeplink_margins),0));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

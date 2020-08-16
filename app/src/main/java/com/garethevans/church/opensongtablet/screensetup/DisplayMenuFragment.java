package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsDisplayBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

public class DisplayMenuFragment extends Fragment {

    MainActivityInterface mainActivityInterface;
    SettingsDisplayBinding myView;
    Preferences preferences;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsDisplayBinding.inflate(inflater,container,false);

        // Set helpers
        setHelpers();

        // Set defaults
        setDefaults();

        // Set menu listeners
        setListeners();
        return myView.getRoot();
    }

    private void setHelpers() {
        preferences = new Preferences();
    }

    private void setDefaults() {
        // Get the app theme
        String themeName;
        switch (preferences.getMyPreferenceString(getContext(),"appTheme","dark")) {
            case "dark":
            default:
                themeName = getContext().getResources().getString(R.string.dark_theme);
                break;
            case "light":
                themeName = getContext().getResources().getString(R.string.light_theme);
                break;
            case "custom1":
                themeName = getContext().getResources().getString(R.string.custom1_theme);
                break;
            case "custom2":
                themeName = getContext().getResources().getString(R.string.custom2_theme);
                break;
        }
        ((TextView)myView.themeButton.getChildAt(0)).setText(themeName);
    }

    private void setListeners() {
        myView.themeButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.themeSetupFragment));
    }
}

package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsModeBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ModeFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ModeFragment";
    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private SettingsModeBinding myView;
    private String choose_app_mode="", website_app_mode="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(choose_app_mode);
        if (webAddress!=null) {
            mainActivityInterface.updateToolbarHelp(webAddress);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        displayInterface = (DisplayInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsModeBinding.inflate(inflater,container,false);

        myView.getRoot().setBackgroundColor(mainActivityInterface.getPalette().background);

        prepareStrings();

        webAddress = website_app_mode;

        // Highlight the current mode
        highlightMode();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            choose_app_mode = getString(R.string.choose_app_mode);
            website_app_mode = getString(R.string.website_app_mode);
        }
    }
    private void highlightMode() {
        // Compare our mode with the strings in the MainActivity (avoids typos)
        if (mainActivityInterface.getMode().equals(mainActivityInterface.modePresenterString())) {
            myView.presenterMode.showCheckmark(true);
        } else if (mainActivityInterface.getMode().equals(mainActivityInterface.modeStageString())) {
            myView.stageMode.showCheckmark(true);
        } else if (mainActivityInterface.getMode().equals(mainActivityInterface.modeHybridString())) {
            myView.hybridMode.showCheckmark(true);
        } else {
            myView.performanceMode.showCheckmark(true);
        }
    }

    private void setListeners() {
        myView.performanceMode.setOnClickListener(v -> updatePreference(mainActivityInterface.modePerformanceString()));
        myView.stageMode.setOnClickListener(v -> updatePreference(mainActivityInterface.modeStageString()));
        myView.presenterMode.setOnClickListener(v -> updatePreference(mainActivityInterface.modePresenterString()));
        myView.hybridMode.setOnClickListener(v -> updatePreference(mainActivityInterface.modeHybridString()));
    }

    private void updatePreference(String which) {
        mainActivityInterface.getPreferences().setMyPreferenceString("whichMode",which);
        mainActivityInterface.setMode(which);
        // Because we are switching modes, we need to let the new fragment know that this is a first run
        // This means it will refresh settings and connected displays when it triggers
        mainActivityInterface.setFirstRun(true);
        displayInterface.updateDisplay("setSongContentPrefs");
        mainActivityInterface.getMainHandler().postDelayed(() -> mainActivityInterface.navHome(),250);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

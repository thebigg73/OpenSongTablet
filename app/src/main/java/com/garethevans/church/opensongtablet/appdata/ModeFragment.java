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
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

public class ModeFragment extends Fragment {

    MainActivityInterface mainActivityInterface;
    SettingsModeBinding myView;
    Preferences preferences;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsModeBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(null,getString(R.string.choose_app_mode));

        // Set the helpers
        setHelpers();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setHelpers() {
        preferences = mainActivityInterface.getPreferences();
    }

    private void setListeners() {
        myView.performanceMode.setOnClickListener(v -> updatePreference("Performance"));
        // TODO
        //myView.stageMode.setOnClickListener(v -> updatePreference("Stage"));
        //myView.presentationMode.setOnClickListener(v -> updatePreference("Presentation"));
    }

    private void updatePreference(String which) {
        preferences.setMyPreferenceString(requireContext(),"whichMode",which);
        mainActivityInterface.returnToHome(this,null);
    }
}

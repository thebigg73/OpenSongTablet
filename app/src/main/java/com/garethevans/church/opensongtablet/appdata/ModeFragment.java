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

public class ModeFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsModeBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsModeBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(getString(R.string.choose_app_mode));

        // Highlight the current mode
        highlightMode();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void highlightMode() {
        switch (mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),
                "whichMode","Performance")) {
            case "Performance":
                myView.performanceMode.showCheckMark(true);
                break;
            case "Presenter":
                myView.presenterMode.showCheckMark(true);
                break;
            case "Stage":
                myView.stageMode.showCheckMark(true);
                break;
        }
        // TODO disable to non-available modes for now...
        //myView.presentationMode.setEnabled(false);
        myView.stageMode.setEnabled(false);
    }

    private void setListeners() {
        myView.performanceMode.setOnClickListener(v -> updatePreference("Performance"));
        myView.stageMode.setOnClickListener(v -> updatePreference("Stage"));
        myView.presenterMode.setOnClickListener(v -> updatePreference("Presenter"));
        myView.webLink.setOnClickListener(v -> mainActivityInterface.openDocument("mode",null));
    }

    private void updatePreference(String which) {
        mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),"whichMode",which);
        mainActivityInterface.navHome();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

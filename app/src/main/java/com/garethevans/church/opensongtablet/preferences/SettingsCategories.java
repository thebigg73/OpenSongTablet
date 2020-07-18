package com.garethevans.church.opensongtablet.preferences;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsCategoriesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class SettingsCategories extends Fragment {

    SettingsCategoriesBinding myView;
    MainActivityInterface mainActivityInterface;
    Preferences preferences;

    @Override
    public void onAttach(@NonNull Context context) {
        mainActivityInterface = (MainActivityInterface) context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        myView = SettingsCategoriesBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(requireContext().getResources().getString(R.string.settings));

        // Prepare helpers
        prepareHelpers();

        // Set listeners
        setListeners();


        return myView.getRoot();
    }

    private void prepareHelpers() {
        preferences = new Preferences();
    }

    private void setListeners() {
        myView.ccliButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.nav_preference_ccli));
    }
}
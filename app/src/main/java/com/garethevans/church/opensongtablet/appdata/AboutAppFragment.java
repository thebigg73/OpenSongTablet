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
import com.garethevans.church.opensongtablet.databinding.SettingsAboutBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class AboutAppFragment extends Fragment {

    private SettingsAboutBinding myView;
    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsAboutBinding.inflate(inflater, container, false);

        // Update the toolbar
        mainActivityInterface.updateToolbar(getString(R.string.about));

        // Update menu text with version and language
        updateMenuText();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void updateMenuText() {
        mainActivityInterface.getVersionNumber().updateMenuVersionNumber(requireContext(),myView.latestVersion);
        myView.languageButton.setHint(mainActivityInterface.getLocale().getDisplayLanguage());
    }

    private void setListeners() {
        myView.visitWebsite.setOnClickListener(v -> mainActivityInterface.openDocument(getString(R.string.website_address)));
        myView.latestVersion.setOnClickListener(v -> mainActivityInterface.openDocument(getString(R.string.website_latest)));
        myView.manualButton.setOnClickListener(v -> mainActivityInterface.openDocument(getString(R.string.website_user_guide)));
        myView.forumButton.setOnClickListener(v -> mainActivityInterface.openDocument(getString(R.string.website_forum)));
        myView.rateButton.setOnClickListener(v -> mainActivityInterface.openDocument(getString(R.string.website_rate)+requireActivity().getPackageName()));
        myView.paypalButton.setOnClickListener(v -> mainActivityInterface.openDocument(getString(R.string.website_paypal)));
        myView.gitbubButton.setOnClickListener(v -> mainActivityInterface.openDocument(getString(R.string.website_github)));
        myView.languageButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.languageFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

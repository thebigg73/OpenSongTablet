package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsAboutBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

public class AboutAppFragment extends Fragment {

    SettingsAboutBinding myView;
    Preferences preferences;
    VersionNumber versionNumber;
    MainActivityInterface mainActivityInterface;
    String userguide="https://www.opensongapp.com/user-guide", groups="https://groups.google.com/g/opensongapp",
            latest = "https://www.opensongapp.com/latest-updates", paypal="https://www.paypal.me/opensongapp",
            rate = "https://play.google.com/store/apps/details?id=", github="https://github.com/thebigg73/OpenSongTablet";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsAboutBinding.inflate(inflater, container, false);

        // Set Helpers
        setHelpers();

        // Update menu text with version and language
        updateMenuText();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void setHelpers() {
        preferences = new Preferences();
        versionNumber = new VersionNumber();
    }

    private void updateMenuText() {
        versionNumber.updateMenuVersionNumber(getContext(),myView.latestVersion.findViewById(R.id.subText));
        ((TextView)myView.languageButton.findViewById(R.id.subText)).setText(StaticVariables.locale.getDisplayLanguage());
    }

    private void setListeners() {
        myView.latestVersion.setOnClickListener(v -> webLink(latest));
        myView.manualButton.setOnClickListener(v -> webLink(userguide));
        myView.forumButton.setOnClickListener(v -> webLink(groups));
        myView.rateButton.setOnClickListener(v -> webLink(rate+getActivity().getPackageName()));
        myView.paypalButton.setOnClickListener(v -> webLink(paypal));
        myView.gitbubButton.setOnClickListener(v -> webLink(github));
        myView.languageButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(R.id.languageFragment));
    }

    private void webLink(String location) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(location));
            startActivity(intent);
        } catch (Exception e) {
            // Probably no browser installed or no internet permission given.
            e.printStackTrace();
        }
    }
}

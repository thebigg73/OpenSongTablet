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

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "AboutAppFragment";
    private SettingsAboutBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String about="", website="", user_guide="", website_address="", website_latest="",
            website_forum="", website_rate="", packageName="", website_paypal="", website_github="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(about);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsAboutBinding.inflate(inflater, container, false);

        prepareStrings();

        // Update menu text with version and language
        updateMenuText();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            about = getString(R.string.about);
            website = getString(R.string.website);
            user_guide = getString(R.string.user_guide);
            website_address = getString(R.string.website_address);
            website_latest = getString(R.string.website_latest);
            website_forum = getString(R.string.website_forum);
            website_rate = getString(R.string.website_rate);
            website_paypal = getString(R.string.website_paypal);
            website_github =  getString(R.string.website_github);
        }
        if (getActivity()!=null) {
            packageName = getActivity().getPackageName();
        }
    }
    private void updateMenuText() {
        if (getContext()!=null) {
            mainActivityInterface.getVersionNumber().updateMenuVersionNumber(getContext(), myView.latestVersion);
        }
        myView.languageButton.setHint(mainActivityInterface.getLocale().getDisplayLanguage());
        String text = website + " / " + user_guide;
        myView.visitWebsite.setText(text);
    }

    private void setListeners() {
        myView.visitWebsite.setOnClickListener(v -> mainActivityInterface.openDocument(website_address));
        myView.latestVersion.setOnClickListener(v -> mainActivityInterface.openDocument(website_latest));
        myView.forumButton.setOnClickListener(v -> mainActivityInterface.openDocument(website_forum));
        myView.rateButton.setOnClickListener(v -> mainActivityInterface.openDocument(website_rate+packageName));
        myView.paypalButton.setOnClickListener(v -> mainActivityInterface.openDocument(website_paypal));
        myView.gitbubButton.setOnClickListener(v -> mainActivityInterface.openDocument(website_github));
        myView.languageButton.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.languageFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

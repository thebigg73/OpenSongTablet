package com.garethevans.church.opensongtablet.ccli;

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
import com.garethevans.church.opensongtablet.databinding.SettingsCcliBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.TextInputDialogFragment;

public class SettingsCCLI extends Fragment {

    private SettingsCcliBinding myView;
    private MainActivityInterface mainActivityInterface;
    private Preferences preferences;

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
        myView = SettingsCcliBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(null,getString(R.string.settings) + " / " + getString(R.string.ccli));

        // Prepare helpers
        prepareHelpers();

        // Set current Values
        setCurrentValues();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareHelpers() {
        preferences = mainActivityInterface.getPreferences();
    }

    private void setCurrentValues() {
        myView.ccliAutomatic.setChecked(preferences.getMyPreferenceBoolean(requireContext(), "myView.ccliAutomaticmaticLogging", false));
        ((TextView)myView.ccliChurch.findViewById(R.id.subText))
                .setText(preferences.getMyPreferenceString(requireContext(), "myView.ccliChurchName", ""));
        ((TextView)myView.ccliLicence.findViewById(R.id.subText))
                .setText(preferences.getMyPreferenceString(requireContext(), "myView.ccliLicence", ""));
    }

    private void setListeners() {
        myView.ccliChurch.setOnClickListener(v -> showDialog(new TextInputDialogFragment(preferences, this,
                "SettingsCCLI", getString(R.string.ccli_church), getString(R.string.ccli_church),
                "myView.ccliChurchName", preferences.getMyPreferenceString(requireContext(),
                "myView.ccliChurchName", ""))));
        myView.ccliLicence.setOnClickListener(v -> showDialog(new TextInputDialogFragment(preferences, this,
                "SettingsCCLI", getString(R.string.ccli_licence), getString(R.string.ccli_licence),
                "myView.ccliLicence", preferences.getMyPreferenceString(requireContext(),
                "myView.ccliLicence", ""))));
        myView.ccliAutomatic.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(requireContext(),
                "myView.ccliAutomaticmaticLogging", isChecked));
        myView.ccliView.setOnClickListener(v -> showDialog());
        myView.ccliExport.setOnClickListener(view -> mainActivityInterface.doExport("ccliLog"));
        myView.ccliDelete.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("myView.ccliDelete",
                getString(R.string.ccli_reset),null,"SettingsCCLI", this, null));
    }

    private void showDialog(TextInputDialogFragment dialogFragment) {
        dialogFragment.show(requireActivity().getSupportFragmentManager(), "textInputFragment");
    }

    private void showDialog() {
        CCLIDialogFragment dialogFragment = new CCLIDialogFragment();
        dialogFragment.show(requireActivity().getSupportFragmentManager(), "ccliDialog");
    }

    // Called from MainActivity after TextInputDialogFragment save
    public void updateValue(String which, String value) {
        if (which.equals("myView.ccliChurchName")) {
            ((TextView)myView.ccliChurch.findViewById(R.id.subText)).setText(value);
        } else if (which.equals("myView.ccliLicence")){
            ((TextView)myView.ccliLicence.findViewById(R.id.subText)).setText(value);
        }
    }
}
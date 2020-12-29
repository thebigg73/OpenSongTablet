package com.garethevans.church.opensongtablet.ccli;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.PrefTextLinkView;
import com.garethevans.church.opensongtablet.databinding.SettingsCcliBinding;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.TextInputDialogFragment;

public class SettingsCCLI extends Fragment {

    SettingsCcliBinding myView;
    MainActivityInterface mainActivityInterface;
    Preferences preferences;
    SwitchCompat ccliAuto;
    PrefTextLinkView ccliChurch, ccliLicence, ccliView, ccliExport, ccliDelete;
    StorageAccess storageAccess;

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
        preferences = new Preferences();
        storageAccess = new StorageAccess();
    }

    private void setCurrentValues() {
        ccliAuto = myView.ccliAutomatic;
        ccliChurch = myView.ccliChurch;
        ccliLicence = myView.ccliLicence;
        ccliView = myView.ccliView;
        ccliExport = myView.ccliExport;
        ccliDelete = myView.ccliDelete;
        ccliAuto.setChecked(preferences.getMyPreferenceBoolean(requireContext(), "ccliAutomaticLogging", false));
        ((TextView)ccliChurch.findViewById(R.id.subText))
                .setText(preferences.getMyPreferenceString(requireContext(), "ccliChurchName", ""));
        ((TextView)ccliLicence.findViewById(R.id.subText))
                .setText(preferences.getMyPreferenceString(requireContext(), "ccliLicence", ""));
    }

    private void setListeners() {
        ccliChurch.setOnClickListener(v -> showDialog(new TextInputDialogFragment(preferences, this,
                "SettingsCCLI", getString(R.string.ccli_church), getString(R.string.ccli_church),
                "ccliChurchName", preferences.getMyPreferenceString(requireContext(),
                "ccliChurchName", ""))));
        ccliLicence.setOnClickListener(v -> showDialog(new TextInputDialogFragment(preferences, this,
                "SettingsCCLI", getString(R.string.ccli_licence), getString(R.string.ccli_licence),
                "ccliLicence", preferences.getMyPreferenceString(requireContext(),
                "ccliLicence", ""))));
        ccliAuto.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(requireContext(),
                "ccliAutomaticLogging", isChecked));
        ccliView.setOnClickListener(v -> showDialog());
        ccliExport.setOnClickListener(view -> mainActivityInterface.doExport("ccliLog"));
        ccliDelete.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("ccliDelete",
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
        if (which.equals("ccliChurchName")) {
            ((TextView)myView.ccliChurch.findViewById(R.id.subText)).setText(value);
        } else if (which.equals("ccliLicence")){
            ((TextView)myView.ccliLicence.findViewById(R.id.subText)).setText(value);
        }
    }
}
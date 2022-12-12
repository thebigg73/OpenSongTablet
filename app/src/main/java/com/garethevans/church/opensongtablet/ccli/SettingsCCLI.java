package com.garethevans.church.opensongtablet.ccli;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsCcliBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;

public class SettingsCCLI extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SettingsCCLI";

    private SettingsCcliBinding myView;
    private MainActivityInterface mainActivityInterface;

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

        mainActivityInterface.updateToolbar(getString(R.string.ccli));
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_ccli));

        // Set current Values
        setCurrentValues();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }


    private void setCurrentValues() {
        myView.ccliAutomatic.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("ccliAutomaticLogging", false));

        String notSet = getString(R.string.is_not_set);
        String ccliChurchName = mainActivityInterface.getPreferences().getMyPreferenceString("ccliChurchName","");
        String ccliLicence = mainActivityInterface.getPreferences().getMyPreferenceString("ccliLicence","");
        if (ccliChurchName.isEmpty()) {
            ccliChurchName = notSet;
        }
        if (ccliLicence.isEmpty()) {
            ccliLicence = notSet;
        }
        myView.ccliChurch.setHint(ccliChurchName);
        myView.ccliLicence.setHint(ccliLicence);
    }

    private void setListeners() {
        myView.ccliChurch.setOnClickListener(v -> showDialog(new TextInputBottomSheet(this,
                "SettingsCCLI", getString(R.string.ccli_church), getString(R.string.ccli_church),null,
                "ccliChurchName", mainActivityInterface.getPreferences().getMyPreferenceString(
                "ccliChurchName", ""),true)));
        myView.ccliLicence.setOnClickListener(v -> showDialog(new TextInputBottomSheet(this,
                "SettingsCCLI", getString(R.string.ccli_licence), getString(R.string.ccli_licence),null,
                "ccliLicence", mainActivityInterface.getPreferences().getMyPreferenceString(
                "ccliLicence", ""),true)));
        myView.ccliAutomatic.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                "ccliAutomaticLogging", isChecked));
        myView.ccliView.setOnClickListener(v -> showDialog());
        myView.ccliExport.setOnClickListener(view -> exportLog());
        myView.ccliExportCSV.setOnClickListener(view -> exportCSVLog());
        myView.ccliDelete.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("ccliDelete",
                getString(R.string.ccli_reset),null,"SettingsCCLI", this, null));
    }

    private void showDialog(TextInputBottomSheet dialogFragment) {
        dialogFragment.show(requireActivity().getSupportFragmentManager(), "textInputFragment");
    }

    private void showDialog() {
        mainActivityInterface.navigateToFragment(null,R.id.settingsCCLILog);
    }

    private void exportLog() {
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Settings","","ActivityLog.xml");
        Intent intent = mainActivityInterface.getExportActions().setShareIntent(basicMessage(),"text/xml",uri,null);
        intent.putExtra(Intent.EXTRA_SUBJECT, "ActivityLog.xml");
        intent.putExtra(Intent.EXTRA_TITLE, "ActivityLog.xml");
        startActivity(Intent.createChooser(intent, "ActivityLog.xml"));
    }

    private void exportCSVLog() {
        // Set up the default values
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Settings", "", "ActivityLog.xml");
        mainActivityInterface.getCCLILog().getCurrentEntries(uri);

        if (mainActivityInterface.getStorageAccess().doStringWriteToFile("Export","","ActivityLog.csv",mainActivityInterface.getCCLILog().getCCLILogAsCSV())) {
            uri = mainActivityInterface.getStorageAccess().getUriForItem("Export","","ActivityLog.csv");
            Intent intent = mainActivityInterface.getExportActions().setShareIntent(basicMessage(),"text/csv",uri,null);
            intent.putExtra(Intent.EXTRA_SUBJECT, "ActivityLog.csv");
            intent.putExtra(Intent.EXTRA_TITLE, "ActivityLog.csv");
            startActivity(Intent.createChooser(intent, "ActivityLog.csv"));
        }
    }

    private String basicMessage() {
        return getString(R.string.app_name) + ": " + getString(R.string.ccli) + "\n" +
         getString(R.string.ccli_church) + ": " +
                mainActivityInterface.getPreferences().getMyPreferenceString("ccliChurchName","") + "\n" +
        getString(R.string.ccli_licence) + ": " +
                mainActivityInterface.getPreferences().getMyPreferenceString("ccliLicence","")+ "\n\n";
    }

    // Called from MainActivity after TextInputDialogFragment save
    public void updateValue(String which, String value) {
        if (which.equals("ccliChurchName")) {
            myView.ccliChurch.setHint(value);
        } else if (which.equals("ccliLicence")) {
            mainActivityInterface.getPresenterSettings().setCcliLicence(value);
            myView.ccliLicence.setHint(value);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
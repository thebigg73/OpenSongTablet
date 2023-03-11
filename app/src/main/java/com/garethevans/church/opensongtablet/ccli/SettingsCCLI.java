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
    private String ccli_string="", website_ccli_string="", is_not_set_string="",
            ccli_church_string="", ccli_licence_string="", ccli_reset_string="",
            app_name_string="";

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

        prepareStrings();

        mainActivityInterface.updateToolbar(ccli_string);
        mainActivityInterface.updateToolbarHelp(website_ccli_string);

        // Set current Values
        setCurrentValues();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            ccli_string = getString(R.string.ccli);
            website_ccli_string = getString(R.string.website_ccli);
            is_not_set_string = getString(R.string.is_not_set);
            ccli_church_string = getString(R.string.ccli_church);
            ccli_licence_string = getString(R.string.ccli_licence);
            ccli_reset_string = getString(R.string.ccli_reset);
            app_name_string = getString(R.string.app_name);
        }
    }

    private void setCurrentValues() {
        myView.ccliAutomatic.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("ccliAutomaticLogging", false));

        String notSet = is_not_set_string;
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
                "SettingsCCLI", ccli_church_string, ccli_church_string,null,
                "ccliChurchName", mainActivityInterface.getPreferences().getMyPreferenceString(
                "ccliChurchName", ""),true)));
        myView.ccliLicence.setOnClickListener(v -> showDialog(new TextInputBottomSheet(this,
                "SettingsCCLI", ccli_licence_string, ccli_licence_string, null,
                "ccliLicence", mainActivityInterface.getPreferences().getMyPreferenceString(
                "ccliLicence", ""),true)));
        myView.ccliAutomatic.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean(
                "ccliAutomaticLogging", isChecked));
        myView.ccliView.setOnClickListener(v -> showDialog());
        myView.ccliExportCSV.setOnClickListener(view -> exportCSVLog());
        myView.ccliDelete.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("ccliDelete",
                ccli_reset_string,null,"SettingsCCLI", this, null));
    }

    private void showDialog(TextInputBottomSheet dialogFragment) {
        if (getActivity()!=null) {
            dialogFragment.show(getActivity().getSupportFragmentManager(), "textInputFragment");
        }
    }

    private void showDialog() {
        mainActivityInterface.navigateToFragment(null,R.id.settingsCCLILog);
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
        return app_name_string + ": " + ccli_string + "\n" +
         ccli_church_string + ": " +
                mainActivityInterface.getPreferences().getMyPreferenceString("ccliChurchName","") + "\n" +
        ccli_licence_string + ": " +
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
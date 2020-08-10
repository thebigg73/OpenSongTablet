package com.garethevans.church.opensongtablet.ccli;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsCcliBinding;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.PrefTextLinkView;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsCCLI extends Fragment {

    SettingsCcliBinding myView;
    MainActivityInterface mainActivityInterface;
    Preferences preferences;
    TextInputEditText ccliChurch;
    TextInputEditText ccliLicence;
    SwitchCompat ccliAuto;
    PrefTextLinkView ccliView, ccliExport, ccliDelete;
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
        mainActivityInterface.updateToolbar(null,requireContext().getResources().getString(R.string.edit_song_ccli));

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
        ccliChurch = myView.ccliChurch.getEditText();
        ccliLicence = myView.ccliLicence.getEditText();
        ccliView = myView.ccliView;
        ccliExport = myView.ccliExport;
        ccliDelete = myView.ccliDelete;
        ccliAuto.setChecked(preferences.getMyPreferenceBoolean(requireContext(), "ccliAutomaticLogging", false));
        ccliChurch.setText(preferences.getMyPreferenceString(requireContext(), "ccliChurchName", ""));
        ccliLicence.setText(preferences.getMyPreferenceString(requireContext(), "ccliLicence", ""));
    }

    private void setListeners() {
        ccliAuto.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(requireContext(), "ccliAutomaticLogging", isChecked));
        ccliChurch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                preferences.setMyPreferenceString(requireContext(), "ccliChurchName", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ccliLicence.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                preferences.setMyPreferenceString(requireContext(), "ccliLicence", s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ccliView.setOnClickListener(v -> showDialog());
        ccliExport.setOnClickListener(view -> mainActivityInterface.doExport("ccliLog"));
        ccliDelete.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("ccliDelete", getString(R.string.ccli_reset),null,"SettingsCCLI", this, null));
    }

    private void showDialog() {
        CCLIDialogFragment dialogFragment = new CCLIDialogFragment();
        dialogFragment.show(requireActivity().getSupportFragmentManager(), "ccliDialog");
    }

}

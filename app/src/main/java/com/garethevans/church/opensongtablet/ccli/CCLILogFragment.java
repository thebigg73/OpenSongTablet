package com.garethevans.church.opensongtablet.ccli;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsCcliLogBinding;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

public class CCLILogFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private Preferences preferences;
    private StorageAccess storageAccess;
    private CCLILog ccliLog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        com.garethevans.church.opensongtablet.databinding.SettingsCcliLogBinding myView = SettingsCcliLogBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(null,getString(R.string.ccli) + " XML");

        // Initialise helpers
        initialiseHelpers();

        // Set up the default values
        Uri uri = storageAccess.getUriForItem(requireContext(), preferences, "Settings", "", "ActivityLog.xml");
        ccliLog.getCurrentEntries(requireContext(), storageAccess, uri);
        ccliLog.getLogFileSize(requireContext(),storageAccess,uri, myView.logSize);
        String churchName = getString(R.string.ccli_church) + ": " +
                preferences.getMyPreferenceString(requireContext(), "ccliChurchName","");
        myView.churchLicence.setText(churchName);
        String churchLicence = getString(R.string.ccli_licence) + ": " +
                preferences.getMyPreferenceString(requireContext(),"ccliLicence","");
        myView.churchLicence.setText(churchLicence);

        TableLayout tableLayout = ccliLog.getTableLayout(requireContext());

        myView.zoomLayout.addView(tableLayout);


        return myView.getRoot();
    }

    private void initialiseHelpers() {
        preferences = mainActivityInterface.getPreferences();
        storageAccess = mainActivityInterface.getStorageAccess();
        ccliLog = mainActivityInterface.getCCLILog();
    }
}

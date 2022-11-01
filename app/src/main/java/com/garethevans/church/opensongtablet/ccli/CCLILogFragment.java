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
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class CCLILogFragment extends Fragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "CCLILogFrag";

    private MainActivityInterface mainActivityInterface;
    private SettingsCcliLogBinding myView;

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
        myView = SettingsCcliLogBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.ccli) + " XML");

        // Set up the default values
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Settings", "", "ActivityLog.xml");
        mainActivityInterface.getCCLILog().getCurrentEntries(uri);
        mainActivityInterface.getCCLILog().getLogFileSize(uri, myView.logSize);
        String churchName = getString(R.string.ccli_church) + ": " +
                mainActivityInterface.getPreferences().getMyPreferenceString("ccliChurchName","");
        myView.churchDetails.setText(churchName);
        String churchLicence = getString(R.string.ccli_licence) + ": " +
                mainActivityInterface.getPreferences().getMyPreferenceString("ccliLicence","");
        myView.churchDetails.setHint(churchLicence);

        TableLayout tableLayout = mainActivityInterface.getCCLILog().getTableLayout();

        myView.zoomLayout.addView(tableLayout);

        return myView.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

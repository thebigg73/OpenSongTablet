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
    private String ccli_string="", ccli_church_string="", ccli_licence_string="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(ccli_string + " XML");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsCcliLogBinding.inflate(inflater, container, false);

        prepareStrings();

        // Set up the default values
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Settings", "", "ActivityLog.xml");
        mainActivityInterface.getCCLILog().getCurrentEntries(uri);
        mainActivityInterface.getCCLILog().getLogFileSize(uri, myView.logSize);
        String churchName = ccli_church_string + ": " +
                mainActivityInterface.getPreferences().getMyPreferenceString("ccliChurchName","");
        myView.churchDetails.setText(churchName);
        String churchLicence = ccli_licence_string + ": " +
                mainActivityInterface.getPreferences().getMyPreferenceString("ccliLicence","");
        myView.churchDetails.setHint(churchLicence);

        TableLayout tableLayout = mainActivityInterface.getCCLILog().getTableLayout();

        myView.zoomLayout.addView(tableLayout);

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            ccli_string = getString(R.string.ccli);
            ccli_church_string = getString(R.string.ccli_church);
            ccli_licence_string = getString(R.string.ccli_licence);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

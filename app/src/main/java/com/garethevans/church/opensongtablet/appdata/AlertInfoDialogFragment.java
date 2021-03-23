package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.AlertinfoDialogfragmentBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

/*
This file shows the user any appropriate warnings.  These can be
- A reminder to create a backup file of songs
- Information about a recent update
- A warning about not having Google Play Services installed
 */

public class AlertInfoDialogFragment extends DialogFragment {

    MainActivityInterface mainActivityInterface;
    AlertinfoDialogfragmentBinding myView;
    Preferences preferences;
    String updateInfo;
    AlertChecks alertChecks;

    public AlertInfoDialogFragment(Preferences preferences, AlertChecks alertChecks) {
        this.alertChecks = alertChecks;
        this.preferences = preferences;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = AlertinfoDialogfragmentBinding.inflate(inflater,container,false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Show/hide the appropriate alerts
        whatAlerts();

        myView.dialogHeading.findViewById(R.id.close).setOnClickListener(b -> dismiss());

        return myView.getRoot();
    }

    private void whatAlerts() {
        // This decides which alerts are appropriate

        // Check for app updates
        updateInfo = alertChecks.updateInfo(requireContext(),preferences);
        if (updateInfo==null) {
            myView.appUpdated.setVisibility(View.GONE);
        } else {
            myView.appUpdated.setVisibility(View.VISIBLE);
            myView.showUpdates.setText(updateInfo);
            myView.showUpdates.setOnClickListener(b -> webLink("http://www.opensongapp.com/latest-updates"));
            // We've seen the warning, so update the preference
            String val = updateInfo.substring(updateInfo.indexOf("(")+1,updateInfo.indexOf(")"));
            preferences.setMyPreferenceInt(requireContext(), "lastUsedVersion", Integer.parseInt(val));
        }

        // Check for backup status
        if (alertChecks.showBackup(requireContext(),preferences)) {
            myView.timeToBackup.setVisibility(View.VISIBLE);
            // TODO Button logic to trigger the backup prompt
        } else {
            myView.timeToBackup.setVisibility(View.GONE);
        }

        // Check for Google Play Service error
        if (alertChecks.showPlayServicesAlert(requireContext())) {
            Log.d("StageMode", "onresume()  Play store isn't installed");
            myView.playServices.setVisibility(View.VISIBLE);
            myView.playServicesInfo.setOnClickListener(b -> webLink(getString(R.string.play_services_help)));
        } else {
            myView.playServices.setVisibility(View.GONE);
        }
    }



    private void webLink(String link) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(link));
        try {
            startActivity(i);
        } catch (Exception e) {
            Log.d("AlertDialogFragment", "Error showing webView");
        }
    }

}

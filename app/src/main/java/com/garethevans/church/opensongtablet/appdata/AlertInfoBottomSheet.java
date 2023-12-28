package com.garethevans.church.opensongtablet.appdata;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetAlertInfoBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/*
This file shows the user any appropriate warnings.  These can be
- A reminder to create a backup file of songs
- Information about a recent update
- A warning about not having Google Play Services installed
 */

public class AlertInfoBottomSheet extends BottomSheetDialogFragment {

    private MainActivityInterface mainActivityInterface;
    private BottomSheetAlertInfoBinding myView;
    private final String TAG = "AlertInfoBottomSheet";
    private String website_latest="", promptbackup="", deeplink_backup="",
            website_play_services_help="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        mainActivityInterface.getAlertChecks().setIsShowing(true);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetAlertInfoBinding.inflate(inflater,container,false);

        prepareStrings();

        // Show/hide the appropriate alerts
        whatAlerts();

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            website_latest = getString(R.string.website_latest);
            promptbackup = getString(R.string.promptbackup);
            deeplink_backup = getString(R.string.deeplink_backup);
            website_play_services_help = getString(R.string.website_play_services_help);
        }
    }

    private void alertsRequired() {
        boolean required = mainActivityInterface.getAlertChecks().showBackup() &&
                mainActivityInterface.getAlertChecks().showPlayServicesAlert() &&
                mainActivityInterface.getAlertChecks().showUpdateInfo();
        if (!required) {
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void whatAlerts() {
        // This decides which alerts are appropriate
        // Check for app updates
        if (mainActivityInterface.getAlertChecks().showUpdateInfo()) {
            myView.appUpdated.setVisibility(View.VISIBLE);
            myView.showUpdates.setText(mainActivityInterface.getVersionNumber().getFullVersionInfo());
            myView.showUpdates.setOnClickListener(b -> webLink(website_latest));

            // We've seen the warning, so update the preference
            mainActivityInterface.getPreferences().setMyPreferenceInt("lastUsedVersion",
                    mainActivityInterface.getVersionNumber().getVersionCode());

        } else {
            myView.appUpdated.setVisibility(View.GONE);
        }

        // Check for backup status
        if (mainActivityInterface.getAlertChecks().showBackup()) {
            myView.timeToBackup.setVisibility(View.VISIBLE);
            mainActivityInterface.getPreferences().setMyPreferenceInt("runssincebackupdismissed",0);
            String s = promptbackup.
                    replace("10",
                            String.valueOf(mainActivityInterface.getPreferences().getMyPreferenceInt("runssincebackup", 0)));
            myView.backupDescription.setText(s);
            myView.backupNowButton.setOnClickListener(v -> {
                mainActivityInterface.navigateToFragment(deeplink_backup,0);
                dismiss();
            });
        } else {
            myView.timeToBackup.setVisibility(View.GONE);
        }

        // Check for Google Play Service error
        if (mainActivityInterface.getAlertChecks().showPlayServicesAlert()) {
            Log.d(TAG, "onresume()  Play store isn't installed");
            myView.playServices.setVisibility(View.VISIBLE);
            myView.playServicesInfo.setOnClickListener(b -> webLink(website_play_services_help));
            myView.ignorePlayServices.setOnClickListener(b -> {
                mainActivityInterface.getAlertChecks().setIgnorePlayServicesWarning(true);
                myView.playServices.setVisibility(View.GONE);
                alertsRequired();
            });
        } else {
            myView.playServices.setVisibility(View.GONE);
        }
        mainActivityInterface.getAlertChecks().setAlreadySeen(true);
    }

    private void webLink(String link) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(link));
        try {
            startActivity(i);
        } catch (Exception e) {
            Log.d(TAG, "Error showing webView");
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        mainActivityInterface.refreshMenuItems();
        mainActivityInterface.getAlertChecks().setAlreadySeen(true);

        // Check if we need to see the showcase for first use
        mainActivityInterface.getAlertChecks().setIsShowing(false);
        mainActivityInterface.showTutorial("performanceView",null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

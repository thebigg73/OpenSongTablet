package com.garethevans.church.opensongtablet.appdata;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.BuildConfig;
import com.garethevans.church.opensongtablet.databinding.BottomSheetInformationBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class InformationBottomSheet extends BottomSheetDialogFragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "InfoBottomSheet";

    private MainActivityInterface mainActivityInterface;

    private final String title, information, buttonText, deepLink;

    public InformationBottomSheet(String title, String information, String buttonText,
                                  String deepLink) {
        this.title = title;
        this.information = information;
        this.buttonText = buttonText;
        this.deepLink = deepLink;
    }

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
        return dialog;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        com.garethevans.church.opensongtablet.databinding.BottomSheetInformationBinding myView = BottomSheetInformationBinding.inflate(inflater, container, false);
        myView.dialogHeading.setClose(this);
        if (information!=null) {
            myView.dialogHeading.setText(title);
        }

        if (buttonText!=null) {
            myView.actionButton.setText(buttonText);
        } else {
            myView.actionButton.setVisibility(View.GONE);
        }

        if (deepLink!=null) {
            switch (deepLink) {
                case "restart":
                    myView.actionButton.setOnClickListener((view) -> {
                        if (getActivity() != null) {
                            dismiss();
                            Intent intent = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(0);
                        }
                    });
                    break;
                case "locPrefs":
                    // Open the location settings to allow the user to check their settings
                    myView.actionButton.setOnClickListener((view) -> {
                        if (getContext()!=null) {
                            getContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            dismiss();
                        }
                    });
                    break;
                case "appPrefs":
                    // Open the app settings to allow the user to check their settings
                    myView.actionButton.setOnClickListener((view) -> {
                        if (getContext()!=null) {
                            getContext().startActivity(new Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                            dismiss();
                        }
                    });
                    break;
                default:
                    myView.actionButton.setOnClickListener((view) -> {
                        mainActivityInterface.navigateToFragment(deepLink, -1);
                        dismiss();
                    });
                    break;
            }
        } else {
            myView.actionButton.setVisibility(View.GONE);
        }

        myView.infoText.setText(information);

        return myView.getRoot();
    }
}

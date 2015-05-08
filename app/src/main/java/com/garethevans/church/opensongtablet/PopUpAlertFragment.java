/*
 * Copyright (c) 2015.
 * The code is provided free of charge.  You can use, modify, contribute and improve it as long as this source is referenced.
 * Commercial use should seek permission.
 */

package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

/**
 * Created by gareth on 07/05/15.
 */
public class PopUpAlertFragment extends DialogFragment {

    static EditText alertMessage;
    static ToggleButton alertToggle;
    static Button closeButton;

    static PopUpAlertFragment newInstance() {
        PopUpAlertFragment frag;
        frag = new PopUpAlertFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.alert));
        final View V = inflater.inflate(R.layout.popup_alert, container, false);

        alertMessage = (EditText) V.findViewById(R.id.alertMessage);
        alertMessage.setText(FullscreenActivity.myAlert);
        alertToggle = (ToggleButton) V.findViewById(R.id.alertToggleButton);
        closeButton = (Button) V.findViewById(R.id.alertClose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PresenterMode.myAlert = alertMessage.getText().toString().trim();
                FullscreenActivity.myAlert = alertMessage.getText().toString().trim();
                Preferences.savePreferences();
                dismiss();
            }
        });

        // If an alert is currently being shown, make sure the toggle button is on.  If not, off!
        if (PresenterMode.alert_on.equals("Y")) {
            alertToggle.setChecked(true);
        } else {
            alertToggle.setChecked(false);
        }

        // Now set a listener for the toggle changing.
        // This will either switch on the alert, or turn it off.
        alertToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Turn on the alert
                    PresenterMode.myAlert = alertMessage.getText().toString().trim();
                    PresenterMode.alert_on = "Y";
                    MyPresentation.fadeinAlert();
                } else {
                    // Turn off the alert
                    PresenterMode.alert_on = "N";
                    MyPresentation.fadeoutAlert();
                }
                FullscreenActivity.myAlert = alertMessage.getText().toString().trim();
                Preferences.savePreferences();
            }
        });
        return V;
    }
}

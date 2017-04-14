package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class PopUpAlertFragment extends DialogFragment {

    EditText alertMessage;
    SwitchCompat alertToggle;

    static PopUpAlertFragment newInstance() {
        PopUpAlertFragment frag;
        frag = new PopUpAlertFragment();
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            title.setText(getActivity().getResources().getString(R.string.alert));
            FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doClose();
                }
            });
            FloatingActionButton saveMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.saveMe);
            saveMe.setVisibility(View.GONE);
        } else {
            getDialog().setTitle(getActivity().getResources().getString(R.string.alert));
        }
    }

    public void doClose() {
        PresenterMode.myAlert = alertMessage.getText().toString().trim();
        FullscreenActivity.myAlert = alertMessage.getText().toString().trim();
        Preferences.savePreferences();
        dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        final View V = inflater.inflate(R.layout.popup_alert, container, false);

        alertMessage = (EditText) V.findViewById(R.id.alertMessage);
        alertMessage.setText(FullscreenActivity.myAlert);
        alertToggle = (SwitchCompat) V.findViewById(R.id.alertToggleButton);

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

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}

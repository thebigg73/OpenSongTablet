package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
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

    public interface MyInterface {
        void updateAlert(boolean ison);
    }

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
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
    }

    public void doClose() {
        FullscreenActivity.myAlert = alertMessage.getText().toString().trim();
        Preferences.savePreferences();
        dismiss();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        final View V = inflater.inflate(R.layout.popup_alert, container, false);

        TextView title = (TextView) V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.alert));
        final FloatingActionButton closeMe = (FloatingActionButton) V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                doClose();
            }
        });
        FloatingActionButton saveMe = (FloatingActionButton) V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        alertMessage = (EditText) V.findViewById(R.id.alertMessage);
        alertMessage.setText(FullscreenActivity.myAlert);
        alertToggle = (SwitchCompat) V.findViewById(R.id.alertToggleButton);

        // If an alert is currently being shown, make sure the toggle button is on.  If not, off!
        if (PresenterMode.alert_on.equals("Y")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                alertToggle.setChecked(true);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                alertToggle.setChecked(false);
            }
        }

        // Now set a listener for the toggle changing.
        // This will either switch on the alert, or turn it off.
        alertToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FullscreenActivity.myAlert = alertMessage.getText().toString().trim();
                Preferences.savePreferences();
                if (mListener!=null) {
                    mListener.updateAlert(isChecked);
                }
            }
        });
        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}

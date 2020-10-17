package com.garethevans.church.opensongtablet;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class PopUpAlertFragment extends DialogFragment {

    private EditText alertMessage;
    private MyInterface mListener;
    private Preferences preferences;

    static PopUpAlertFragment newInstance() {
        PopUpAlertFragment frag;
        frag = new PopUpAlertFragment();
        return frag;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyInterface) context;
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

    private void doClose() {
        preferences.setMyPreferenceString(getActivity(),"presoAlertText",alertMessage.getText().toString().trim());
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        final View V = inflater.inflate(R.layout.popup_alert, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.alert));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getActivity());
            closeMe.setEnabled(false);
            doClose();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();
        alertMessage = V.findViewById(R.id.alertMessage);
        alertMessage.setText(preferences.getMyPreferenceString(getActivity(),"presoAlertText",""));
        SwitchCompat alertToggle = V.findViewById(R.id.alertToggleButton);

        // If an alert is currently being shown, make sure the toggle button is on.  If not, off!
        alertToggle.setChecked(PresenterMode.alert_on.equals("Y"));

        // Now set a listener for the toggle changing.
        // This will either switch on the alert, or turn it off.
        alertToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mListener != null) {
                preferences.setMyPreferenceString(getActivity(),"presoAlertText",alertMessage.getText().toString().trim());
                mListener.updateAlert(isChecked);
            }
        });
        Dialog dialog = getDialog();
        if (dialog != null && getActivity() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), dialog, preferences);
        }
        return V;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

    public interface MyInterface {
        void updateAlert(boolean ison);
    }

}

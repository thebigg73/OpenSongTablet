/*
package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.OLD_TO_DELETE._CustomAnimations;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._PopUpSizeAndAlpha;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class PopUpAlertFragment extends DialogFragment {

    private EditText alertMessage;
    private MyInterface mListener;
    private _Preferences preferences;

    static PopUpAlertFragment newInstance() {
        PopUpAlertFragment frag;
        frag = new PopUpAlertFragment();
        return frag;
    }

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
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        final View V = inflater.inflate(R.layout.popup_alert, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(requireActivity().getResources().getString(R.string.alert));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                doClose();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new _Preferences();
        alertMessage = V.findViewById(R.id.alertMessage);
        alertMessage.setText(preferences.getMyPreferenceString(getActivity(),"presoAlertText",""));
        SwitchCompat alertToggle = V.findViewById(R.id.alertToggleButton);

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
                if (mListener != null) {
                    mListener.updateAlert(isChecked);
                }
            }
        });
        Dialog dialog = getDialog();
        if (dialog != null && getActivity() != null) {
            _PopUpSizeAndAlpha.decoratePopUp(getActivity(), dialog, preferences);
        }
        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    public interface MyInterface {
        void updateAlert(boolean ison);
    }

}
*/

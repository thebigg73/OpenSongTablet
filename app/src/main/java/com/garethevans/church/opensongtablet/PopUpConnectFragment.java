package com.garethevans.church.opensongtablet;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpConnectFragment extends DialogFragment {

    static PopUpConnectFragment newInstance() {
        PopUpConnectFragment frag;
        frag = new PopUpConnectFragment();
        return frag;
    }

    public interface MyInterface {
        void prepareOptionMenu();
        String getUserNickname();
    }

    private static MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    private TextView title;
    private EditText deviceNameEditText;
    private FloatingActionButton saveMe;
    private Preferences preferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        if (getDialog() == null) {
            dismiss();
        }

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View V = inflater.inflate(R.layout.popup_connect, container, false);

        // Set the title based on the whattodo

        title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.connections_connect));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getContext());
            closeMe.setEnabled(false);
            doSave();
        });
        saveMe = V.findViewById(R.id.saveMe);

        preferences = new Preferences();

        // Initialise the views
        deviceNameEditText = V.findViewById(R.id.deviceNameEditText);
        // IV - Start with the broadcasting device name
        deviceNameEditText.setText(mListener.getUserNickname());

        // Set up save/tick listener
        saveMe.setOnClickListener(view -> {
            // IV - Allows an empty string which will clear this 'override' preference
            String s = deviceNameEditText.getText().toString().trim();
            preferences.setMyPreferenceString(getContext(), "deviceId", s);
            doSave();
        });
        Dialog dialog = getDialog();
        if (dialog!=null && getContext()!=null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),dialog, preferences);
        }
        return V;
    }

    private void doSave() {
        if (mListener!=null) {
            mListener.prepareOptionMenu();
        }
        try {
            dismiss();
        } catch (Exception e) {
            Log.d("d","Error closing fragment");
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        try {
            this.dismiss();
        } catch (Exception e) {
            Log.d("d","Error closing the fragment");
        }
    }
}
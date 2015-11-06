package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class PopUpAreYouSureFragment extends DialogFragment {

    static String dialog;

    static PopUpAreYouSureFragment newInstance(String getdialog) {
        dialog = getdialog;
        PopUpAreYouSureFragment frag;
        frag = new PopUpAreYouSureFragment();
        return frag;
    }

    public interface MyInterface {
        void confirmedAction();
    }

    private MyInterface mListener;

    @Override
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View V = inflater.inflate(R.layout.popup_areyousure, container, false);

        getDialog().setTitle(getActivity().getResources().getString(R.string.areyousure));

        TextView areyousurePrompt = (TextView) V.findViewById(R.id.areyousurePrompt);
        areyousurePrompt.setText(dialog);

        Button areyousureNoButton = (Button) V.findViewById(R.id.areyousureNoButton);
        areyousureNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button areyousureYesButton = (Button) V.findViewById(R.id.renameSongOkButton);
        areyousureYesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tell the listener to do something
                mListener.confirmedAction();
                dismiss();
            }
        });

        return V;
    }

}
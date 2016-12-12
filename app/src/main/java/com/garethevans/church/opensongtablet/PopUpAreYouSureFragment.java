package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
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
        void openFragment();
    }

    private MyInterface mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
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

    public void onStart() {
        super.onStart();

        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View V = inflater.inflate(R.layout.popup_areyousure, container, false);

        getDialog().setTitle(getActivity().getResources().getString(R.string.areyousure));
        getDialog().setCanceledOnTouchOutside(true);

        TextView areyousurePrompt = (TextView) V.findViewById(R.id.areyousurePrompt);
        areyousurePrompt.setText(dialog);

        Button areyousureNoButton = (Button) V.findViewById(R.id.areyousureNoButton);
        areyousureNoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open back up the previous menu
                if (FullscreenActivity.whichMode.equals("Stage")) {
                    switch (FullscreenActivity.whattodo) {
                        case "wipeallsongs":
                            FullscreenActivity.whattodo = "managestorage";
                            mListener.openFragment();
                            break;
                    }
                }
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

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
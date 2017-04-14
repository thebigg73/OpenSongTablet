package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            title.setText(getActivity().getResources().getString(R.string.areyousure));
            FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    noAction();
                }
            });
            FloatingActionButton saveMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.saveMe);
            saveMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    yesAction();
                }
            });
        } else {
            getDialog().setTitle(getActivity().getResources().getString(R.string.areyousure));
        }
    }

    public void noAction() {
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

    public void yesAction() {
        // Tell the listener to do something
        if (mListener!=null) {
            mListener.confirmedAction();
        }

        dismiss();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        final View V = inflater.inflate(R.layout.popup_areyousure, container, false);

        TextView areyousurePrompt = (TextView) V.findViewById(R.id.areyousurePrompt);
        areyousurePrompt.setText(dialog);

        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}
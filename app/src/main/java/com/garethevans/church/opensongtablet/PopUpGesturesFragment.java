package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PopUpGesturesFragment extends DialogFragment {

    static PopUpGesturesFragment newInstance() {
        PopUpGesturesFragment frag;
        frag = new PopUpGesturesFragment();
        return frag;
    }

    public interface MyInterface {

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
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null) {
            return;
        }
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.customgestures));
        View V = inflater.inflate(R.layout.choose_gestures, container, false);

        // Initialise the views

        return V;
    }

}

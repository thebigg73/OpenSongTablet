package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PopUpDisplayOptionsFragment extends DialogFragment {

    static PopUpDisplayOptionsFragment newInstance() {
        PopUpDisplayOptionsFragment frag;
        frag = new PopUpDisplayOptionsFragment();
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
        getDialog().setTitle(getActivity().getResources().getString(R.string.colorchooser));
        View V = inflater.inflate(R.layout.ambilwarna_dialog, container, false);

        // Initialise the views

        return V;
    }

}

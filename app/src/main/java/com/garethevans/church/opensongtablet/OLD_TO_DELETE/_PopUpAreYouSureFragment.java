/*
package com.garethevans.church.opensongtablet.OLD_TO_DELETE;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet._Preferences;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

public class _PopUpAreYouSureFragment extends DialogFragment {

    private static String dialog;

    static _PopUpAreYouSureFragment newInstance(String getdialog) {
        dialog = getdialog;
        _PopUpAreYouSureFragment frag;
        frag = new _PopUpAreYouSureFragment();
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

    private void noAction() {
        // Open back up the previous menu
        if (StaticVariables.whichMode.equals("Stage")) {
            switch (StaticVariables.whattodo) {
                case "wipeallsongs":
                    StaticVariables.whattodo = "managestorage";
                    if (mListener!=null) {
                        mListener.openFragment();
                    }
                    break;
                case "resetcolours":
                    StaticVariables.whattodo = "changetheme";
                    if (mListener!=null) {
                        mListener.openFragment();
                    }
                    break;
            }

        }
        if (StaticVariables.whattodo.equals("saveset")) {
            if (mListener!=null) {
                mListener.openFragment();
            }
        }
        dismiss();
    }

    private void yesAction() {
        // Tell the listener to do something
        if (mListener!=null) {
            mListener.confirmedAction();
        }

        dismiss();

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        final View V = inflater.inflate(R.layout._popup_areyousure, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        if (getActivity()!=null) {
            title.setText(getActivity().getString(R.string.areyousure));
        }
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                noAction();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CustomAnimations.animateFAB(saveMe,getActivity());
                saveMe.setEnabled(false);
                yesAction();
            }
        });

        _Preferences preferences = new _Preferences();

        TextView areyousurePrompt = V.findViewById(R.id.areyousurePrompt);
        areyousurePrompt.setText(dialog);

        _PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}*/

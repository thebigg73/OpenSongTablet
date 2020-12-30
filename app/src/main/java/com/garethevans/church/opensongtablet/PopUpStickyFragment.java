package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PopUpStickyFragment extends DialogFragment {

    static PopUpStickyFragment newInstance() {
        PopUpStickyFragment frag;
        frag = new PopUpStickyFragment();
        return frag;
    }

    public interface MyInterface {
        void openFragment();
        void pageButtonAlpha(String s);
    }

    private MyInterface mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().setCanceledOnTouchOutside(true);
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        mListener.pageButtonAlpha("sticky");

        View V = inflater.inflate(R.layout.popup_page_sticky, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.stickynotes));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getContext());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        Preferences preferences = new Preferences();

        // Initialise the views
        TextView mySticky = V.findViewById(R.id.mySticky);
        Button editsticky = V.findViewById(R.id.editsticky);

        // Add the stickynotes
        mySticky.setText(StaticVariables.mNotes);

        // Set the button listeners
        editsticky.setOnClickListener(view -> {
            FullscreenActivity.whattodo = "editnotes";
            mListener.openFragment();
            dismiss();
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);
        return V;
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}

package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class PopUpBackupPromptFragment extends DialogFragment {

    Preferences preferences;
    int runssincebackup = 0;

    private MyInterface mListener;

    static PopUpBackupPromptFragment newInstance() {
        PopUpBackupPromptFragment frag;
        frag = new PopUpBackupPromptFragment();
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

    @Override
    public void onStart() {
        super.onStart();
        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    private void doClose() {
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View v = inflater.inflate(R.layout.popup_backupprompt, container, false);

        TextView title = v.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.backupnow));
        final FloatingActionButton closeMe = v.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                doClose();
            }
        });
        FloatingActionButton saveMe = v.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        // Reset the counter to 0 (value set when this popup loads) and save to preferences
        preferences = new Preferences();
        preferences.setMyPreferenceInt(getActivity(), "runssincebackup", runssincebackup);

        Button backupNow_Button = v.findViewById(R.id.backupNow_Button);
        Button backupLater_Button = v.findViewById(R.id.backupLater_Button);

        // Set listeners
        backupNow_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.whattodo = "exportosb";
                if (mListener != null) {
                    mListener.openFragment();
                }
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        backupLater_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doClose();
            }
        });

        Dialog dialog = getDialog();
        if (dialog != null && getActivity() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), dialog);
        }
        return v;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        try {
            this.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface MyInterface {
        void openFragment();
    }

}

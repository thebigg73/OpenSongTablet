package com.garethevans.church.opensongtablet;

import android.app.Dialog;
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

import java.util.Objects;

public class PopUpBackupPromptFragment extends DialogFragment {

    private MyInterface mListener;

    static PopUpBackupPromptFragment newInstance() {
        PopUpBackupPromptFragment frag;
        frag = new PopUpBackupPromptFragment();
        return frag;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyInterface) context;
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
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        View v = inflater.inflate(R.layout.popup_backupprompt, container, false);

        TextView title = v.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.backupnow));
        final FloatingActionButton closeMe = v.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getActivity());
            closeMe.setEnabled(false);
            doClose();
        });
        FloatingActionButton saveMe = v.findViewById(R.id.saveMe);
        saveMe.hide();

        // Reset the counter to 0 (value set when this popup loads) and save to preferences
        Preferences preferences = new Preferences();
        int runssincebackup = 0;
        preferences.setMyPreferenceInt(getActivity(), "runssincebackup", runssincebackup);

        Button backupNow_Button = v.findViewById(R.id.backupNow_Button);
        Button backupLater_Button = v.findViewById(R.id.backupLater_Button);

        // Set listeners
        backupNow_Button.setOnClickListener(v1 -> {
            FullscreenActivity.whattodo = "exportosb";
            if (mListener != null) {
                mListener.openFragment();
            }
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        backupLater_Button.setOnClickListener(v12 -> doClose());

        Dialog dialog = getDialog();
        if (dialog != null && getActivity() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), dialog, preferences);
        }
        return v;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
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

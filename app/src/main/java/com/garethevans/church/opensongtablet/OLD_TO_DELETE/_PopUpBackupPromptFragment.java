/*
package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.garethevans.church.opensongtablet.OLD_TO_DELETE._CustomAnimations;
import com.garethevans.church.opensongtablet.OLD_TO_DELETE._PopUpSizeAndAlpha;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

public class PopUpBackupPromptFragment extends DialogFragment {

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

    private void doClose() {
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View v = inflater.inflate(R.layout.popup_backupprompt, container, false);

        TextView title = v.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.backupnow));
        final FloatingActionButton closeMe = v.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                doClose();
            }
        });
        FloatingActionButton saveMe = v.findViewById(R.id.saveMe);
        saveMe.hide();

        // Reset the counter to 0 (value set when this popup loads) and save to preferences
        _Preferences preferences = new _Preferences();
        int runssincebackup = 0;
        preferences.setMyPreferenceInt(getActivity(), "runssincebackup", runssincebackup);

        Button backupNow_Button = v.findViewById(R.id.backupNow_Button);
        Button backupLater_Button = v.findViewById(R.id.backupLater_Button);

        // Set listeners
        backupNow_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticVariables.whattodo = "exportosb";
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
            _PopUpSizeAndAlpha.decoratePopUp(getActivity(), dialog, preferences);
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
*/

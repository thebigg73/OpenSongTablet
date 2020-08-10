package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.AreyousureDialogBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

public class AreYouSureDialogFragment extends DialogFragment {

    MainActivityInterface mainActivityInterface;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        dismiss();
        mainActivityInterface.songMenuActionButtonShow(true);
    }

    String action;
    String what;
    ArrayList<String> arguments;
    String fragName;
    Fragment callingFragment;  // can be null if not needed for MainActivity to refresh the fragment
    Song song;

    public AreYouSureDialogFragment(String what, String action, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song) {
        this.what = what;
        this.action = action;
        this.arguments = arguments;
        this.fragName = fragName;
        this.callingFragment = callingFragment;
        this.song = song;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AreyousureDialogBinding areYouSureBinding = AreyousureDialogBinding.inflate(inflater, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        areYouSureBinding.action.setText(action);
        areYouSureBinding.cancelButton.setOnClickListener(v -> {
            mainActivityInterface.confirmedAction(false,what,arguments,fragName,callingFragment,song);
            dismiss();
        });
        areYouSureBinding.okButton.setOnClickListener(v -> {
            mainActivityInterface.confirmedAction(true,what,arguments,fragName,callingFragment,song);
            dismiss();
        });
        return areYouSureBinding.getRoot();
    }
}

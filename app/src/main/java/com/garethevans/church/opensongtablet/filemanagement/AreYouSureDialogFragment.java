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

import com.garethevans.church.opensongtablet.databinding.AreyousureDialogBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

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

    public AreYouSureDialogFragment(String what, String action, ArrayList<String> arguments){
        this.what = what;
        this.action = action;
        this.arguments = arguments;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AreyousureDialogBinding areYouSureBinding = AreyousureDialogBinding.inflate(inflater, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        areYouSureBinding.action.setText(action);
        areYouSureBinding.cancelButton.setOnClickListener(v -> {
            mainActivityInterface.confirmedAction(false,what,arguments);
            dismiss();
        });
        areYouSureBinding.okButton.setOnClickListener(v -> {
            mainActivityInterface.confirmedAction(true,what,arguments);
            dismiss();
        });
        return areYouSureBinding.getRoot();
    }
}

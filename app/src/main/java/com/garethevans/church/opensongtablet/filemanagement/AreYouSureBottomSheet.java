package com.garethevans.church.opensongtablet.filemanagement;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetAreYouSureBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class AreYouSureBottomSheet extends BottomSheetDialogFragment {

    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    private final String textToShow, what, fragName;
    private final ArrayList<String> arguments;
    private final Fragment callingFragment;  // can be null if not needed for MainActivity to refresh the fragment
    private final Song song;

    public AreYouSureBottomSheet(String what, String textToShow, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song) {
        this.what = what;               // Variable passed to MainActivity to trigger required action
        this.textToShow = textToShow;   // Information displayed about what is about to happen
        this.arguments = arguments;     // Extra info passed back.  Can be null
        this.fragName = fragName;       // The fragment requesting confirmation
        this.callingFragment = callingFragment;
        this.song = song;
    }

    public AreYouSureBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        what = "";
        textToShow = "";
        arguments = new ArrayList<>();
        fragName = "";
        callingFragment = null;
        song = new Song();
        dismiss();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BottomSheetAreYouSureBinding myView = BottomSheetAreYouSureBinding.inflate(inflater, container, false);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        if (what.equals("newSet") && getContext()!=null) {
            myView.dialogHeading.setWebHelp(mainActivityInterface,getString(R.string.website_set_create_new));
        }
        myView.action.setText(textToShow);
        myView.okButton.setOnClickListener(v -> {
            mainActivityInterface.confirmedAction(true,what,arguments,fragName,callingFragment,song);
            dismiss();
        });
        return myView.getRoot();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (what.equals("exit")) {
            mainActivityInterface.setAlreadyBackPressed(false);
        }
    }
}

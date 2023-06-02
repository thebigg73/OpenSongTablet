package com.garethevans.church.opensongtablet.appdata;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetBootupIndexingBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Timer;
import java.util.TimerTask;

public class BootUpIndexBottomSheet extends BottomSheetDialogFragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BootUpIndexBottomSheet";
    private BottomSheetBootupIndexingBinding myView;
    private final BootUpFragment bootUpFragment;
    private String indexing_string="", continue_string="";
    private Timer timer;
    private int countdownNumber = 5;

    public BootUpIndexBottomSheet(BootUpFragment bootUpFragment) {
        this.bootUpFragment = bootUpFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        myView.dialogHeading.setText(indexing_string);
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

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = BottomSheetBootupIndexingBinding.inflate(inflater, container, false);

        prepareStrings();
        setupListeners();
        myView.dialogHeading.setClose(this);
        setTimer();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            indexing_string = getString(R.string.indexing_string);
            continue_string = getString(R.string.continue_text);
            myView.dialogHeading.setText(indexing_string);
        }
    }

    private void setupListeners() {
        myView.continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bootUpFragment.startBootProcess(true);
                dismiss();
            }
        });
        myView.skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bootUpFragment.startBootProcess(false);
                dismiss();
            }
        });
    }

    private void setTimer() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                String message = continue_string + " ("+countdownNumber+")";
                myView.continueButton.setText(message);
                if (countdownNumber==1) {
                    this.cancel();
                    timer.purge();
                    timer = null;
                    myView.continueButton.performClick();
                } else {
                    countdownNumber --;
                }
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask,1000,1000);
    }

}

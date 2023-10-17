package com.garethevans.church.opensongtablet.appdata;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetBootupIndexingBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Timer;
import java.util.TimerTask;

public class BootUpIndexBottomSheet extends BottomSheetDialogFragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BootUpIndexBottomSheet";
    private MainActivityInterface mainActivityInterface;
    private BottomSheetBootupIndexingBinding myView;
    private final BootUpFragment bootUpFragment;
    private String indexing_string="", quick_string="", full_string="";
    private String skip_string="";
    private String indexing_web="";
    private Timer timer;
    private TimerTask timerTask;
    private int countdownNumber = 10;
    boolean actionChosen = false;
    boolean needIndex = false, fullIndex = false;

    public BootUpIndexBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        bootUpFragment = null;
        dismiss();
    }

    public BootUpIndexBottomSheet(BootUpFragment bootUpFragment) {
        this.bootUpFragment = bootUpFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareStrings(getContext());
        if (countdownNumber != 10) {
            // We were in progress of counting down, so resume
            Log.d(TAG,"Try to resume");
            countdownNumber = 10;
            setTimer();
        }
        myView.dialogHeading.setText(indexing_string);
        myView.dialogHeading.setWebHelp(mainActivityInterface, indexing_web);
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        prepareStrings(context);
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = BottomSheetBootupIndexingBinding.inflate(inflater, container, false);

        prepareStrings(getContext());
        setupListeners();
        myView.dialogHeading.setClose(this);

        String text = indexing_string + " (" + quick_string + ")";
        myView.quickIndexButton.setText(text);
        text = indexing_string + " (" + full_string + ")";
        myView.fullIndexButton.setText(text);

        setTimer();

        return myView.getRoot();
    }

    private void prepareStrings(Context c) {
        if (c!=null) {
            indexing_string = c.getString(R.string.index_songs);
            skip_string = c.getString(R.string.skip);
            indexing_web = c.getString(R.string.website_indexing_songs);
            quick_string = getString(R.string.index_songs_quick);
            full_string = getString(R.string.index_songs_full);
        }
    }

    private void setupListeners() {
        myView.quickIndexButton.setOnClickListener(view -> {
            actionChosen = true;
            needIndex = true;
            fullIndex = false;
            // The boot process is called in the onDismiss() method
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        myView.fullIndexButton.setOnClickListener(view -> {
            actionChosen = true;
            needIndex = true;
            fullIndex = true;
            // The boot process is called in the onDismiss() method
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        myView.skipButton.setOnClickListener(view -> {
            actionChosen = true;
            needIndex = false;
            fullIndex = false;
            // The boot process is called in the onDismiss() method
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                String message = skip_string + " ("+countdownNumber+")";
                if (myView!=null) {
                    myView.skipButton.post(() -> myView.skipButton.setText(message));
                }

                if (countdownNumber==0) {
                    this.cancel();
                    if (timer!=null) {
                        timer.purge();
                    }
                    timer = null;
                    if (myView!=null) {
                        myView.skipButton.post(() -> myView.skipButton.performClick());
                    }
                } else {
                    countdownNumber --;
                }
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask,1000,1000);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (bootUpFragment!=null) {
            bootUpFragment.startBootProcess(needIndex,fullIndex);
        }
        // Try to end/cancel any timers
        try {
            if (timerTask!=null) {
                timerTask.cancel();
            }
            if (timer!=null) {
                timer.purge();
            }
            timer = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.garethevans.church.opensongtablet.autoscroll;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetAutoscrollBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AutoscrollBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetAutoscrollBinding myView;
    private MainActivityInterface mainActivityInterface;
    private int duration, delay;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetAutoscrollBinding.inflate(inflater, container, false);
        myView.dialogHeading.setClose(this);

        // Set up the views
        setupViews();

        // Check audio link file
        mainActivityInterface.getAutoscroll().checkLinkAudio(requireContext(),mainActivityInterface,
                myView.linkAudio, myView.songDuration, myView.songDelay,getStringToInt(myView.songDelay.getText().toString()));

        // Set listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        myView.songDelay.setText(getStringToInt(mainActivityInterface.getSong().getAutoscrolldelay())+"");
        myView.songDuration.setText(getStringToInt(mainActivityInterface.getSong().getAutoscrolllength())+"");
        setStartStop();
    }

    private void setupListeners() {
        myView.learnAutoscroll.setOnClickListener(v -> learnAutoscroll());
        myView.startStopAutoscroll.setOnClickListener(v -> startStopAutoscroll());
        myView.songDelay.addTextChangedListener(new MyTextWatcher("songDelay"));
        myView.songDuration.addTextChangedListener(new MyTextWatcher("songDuration"));
    }

    private int getStringToInt(String string) {
        // A chance to check the value is a number.  If not return 0;
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            return 0;
        }
    }

    private void learnAutoscroll() {
        // This sends an action to the performance mode to start the process
    }


    private void startStopAutoscroll() {
        if (mainActivityInterface.getAutoscroll().getIsAutoscrolling()) {
            mainActivityInterface.getAutoscroll().stopAutoscroll();
            } else {
            mainActivityInterface.getAutoscroll().startAutoscroll();
            dismiss();
        }
        setStartStop();
    }

    private void setStartStop() {
        if (mainActivityInterface.getAutoscroll().getIsAutoscrolling()) {
            myView.startStopAutoscroll.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_stop_white_36dp));
            myView.startStopAutoscroll.setText(getString(R.string.stop));
        } else {
            myView.startStopAutoscroll.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_play_white_36dp));
            myView.startStopAutoscroll.setText(getString(R.string.start));
        }
    }
    private class MyTextWatcher implements TextWatcher {

        private final String which;

        MyTextWatcher(String which) {
            this.which = which;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (which) {
                case "songDelay":
                    delay = getStringToInt(myView.songDelay.getText().toString());
                    if (delay>duration) {
                        duration = delay;
                    }
                    mainActivityInterface.getSong().setAutoscrolldelay(delay+"");
                    break;
                case "songDuration":
                    duration = getStringToInt(myView.songDuration.getText().toString());
                    if (duration<delay) {
                        delay = duration;
                    }
                    mainActivityInterface.getSong().setAutoscrolllength(duration+"");
                    break;
            }
            mainActivityInterface.getSaveSong().updateSong(requireContext(), mainActivityInterface,
                    mainActivityInterface.getSong());
        }
    }
}


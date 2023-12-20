package com.garethevans.church.opensongtablet.autoscroll;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
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

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "AutoscrollBottomSheet";

    private BottomSheetAutoscrollBinding myView;
    private MainActivityInterface mainActivityInterface;
    private String deeplink_autoscroll_settings="", website_autoscroll="", default_autoscroll="",
            autoscroll_delay="", ask="", stop="", start="";

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

        prepareStrings();

        myView.dialogHeading.setWebHelp(mainActivityInterface,website_autoscroll);

        // Set up the views
        setupViews();

        // Check audio link file
        mainActivityInterface.getAutoscroll().checkLinkAudio(myView.linkAudio, myView.durationMins,
                myView.durationSecs, myView.delay,getStringToInt(myView.delay.getText().toString()));

        // Set listeners
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            deeplink_autoscroll_settings = getString(R.string.deeplink_autoscroll_settings);
            website_autoscroll = getString(R.string.website_autoscroll);
            default_autoscroll = getString(R.string.default_autoscroll);
            autoscroll_delay = getString(R.string.autoscroll_delay);
            ask = getString(R.string.ask);
            stop = getString(R.string.stop);
            start = getString(R.string.start);
        }
    }
    private void setupViews() {
        int[] songTimes = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(getStringToInt(mainActivityInterface.getSong().getAutoscrolllength()));
        myView.durationMins.setText(String.valueOf(songTimes[0]));
        myView.durationSecs.setText(String.valueOf(songTimes[1]));
        myView.delay.setText(String.valueOf(getStringToInt(mainActivityInterface.getSong().getAutoscrolldelay())));

        myView.durationMins.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.durationSecs.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.delay.setInputType(InputType.TYPE_CLASS_NUMBER);
        myView.durationMins.setDigits("0123456789");
        myView.durationSecs.setDigits("0123456789");
        myView.delay.setDigits("0123456789");

        // Set the defaults
        int[] defTimes = mainActivityInterface.getTimeTools().getMinsSecsFromSecs(mainActivityInterface.getAutoscroll().getAutoscrollDefaultSongLength());
        String hint = default_autoscroll + " " + defTimes[0] + "m " +
                defTimes[1] + "s (" + autoscroll_delay + " " +
                mainActivityInterface.getAutoscroll().getAutoscrollDefaultSongPreDelay() + "s)";
        if (!mainActivityInterface.getAutoscroll().getAutoscrollUseDefaultTime()) {
            hint = ask;
        }
        myView.usingDefault.setHint(hint);

        if (getStringToInt(mainActivityInterface.getSong().getAutoscrolllength())==0) {
            myView.usingDefault.setVisibility(View.VISIBLE);
        } else {
            myView.usingDefault.setVisibility(View.GONE);
        }

        setStartStop();
    }

    private void setupListeners() {
        myView.learnAutoscroll.setOnClickListener(v -> learnAutoscroll());
        myView.startStopAutoscroll.setOnClickListener(v -> startStopAutoscroll());
        myView.delay.addTextChangedListener(new MyTextWatcher());
        myView.durationMins.addTextChangedListener(new MyTextWatcher());
        myView.durationSecs.addTextChangedListener(new MyTextWatcher());
        myView.autoscrollSettings.setOnClickListener(v -> {
            mainActivityInterface.navigateToFragment(deeplink_autoscroll_settings,0);
            dismiss();
        });
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
            if (getContext()!=null) {
                myView.startStopAutoscroll.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.stop));
            }
            myView.startStopAutoscroll.setText(stop);
        } else {
            if (getContext()!=null) {
                myView.startStopAutoscroll.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.play));
            }
            myView.startStopAutoscroll.setText(start);
        }
    }
    private class MyTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void afterTextChanged(Editable editable) {
            int delay = getStringToInt(myView.delay.getText().toString());
            int mins = getStringToInt(myView.durationMins.getText().toString());
            int secs = getStringToInt(myView.durationSecs.getText().toString());
            int total = mainActivityInterface.getTimeTools().totalSecs(mins, secs);
            if (delay >= total) {
                delay = 0;
            }

            mainActivityInterface.getSong().setAutoscrolldelay(String.valueOf(delay));
            mainActivityInterface.getSong().setAutoscrolllength(String.valueOf(total));
            mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
        }
    }

}


package com.garethevans.church.opensongtablet.midi;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetMidiActionBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MidiActionBottomSheet extends BottomSheetDialogFragment {

    public MidiActionBottomSheet(String midiCode) {
        this.midiCode = midiCode;
    }
    private MainActivityInterface mainActivityInterface;
    private BottomSheetMidiActionBinding myView;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String midiCode, TAG = "MidiActionBottomSheet";
    private String website_midi_actions="", success_string="";
    private int on, off, which;

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
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = BottomSheetMidiActionBinding.inflate(inflater,container,false);

        prepareStrings();

        myView.dialogHeading.setClose(this);
        myView.dialogHeading.setWebHelp(mainActivityInterface, website_midi_actions);

        setWhich(1);
        changeHighlight();

        myView.newCode.setText(midiCode);
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.newCode);
        mainActivityInterface.getProcessSong().stretchEditBoxToLines(myView.newCode,4);
        myView.currentCode.setHint(mainActivityInterface.getMidi().getMidiAction(1));

        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            website_midi_actions = getString(R.string.website_midi_actions);
            success_string = getString(R.string.success);

            // Also set the colours
            on = getResources().getColor(R.color.colorSecondary);
            off = getResources().getColor(R.color.colorAltPrimary);
        }
    }

    private void setListeners() {
        myView.action1.setOnClickListener(new MyOnClick(1));
        myView.action2.setOnClickListener(new MyOnClick(2));
        myView.action3.setOnClickListener(new MyOnClick(3));
        myView.action4.setOnClickListener(new MyOnClick(4));
        myView.action5.setOnClickListener(new MyOnClick(5));
        myView.action6.setOnClickListener(new MyOnClick(6));
        myView.action7.setOnClickListener(new MyOnClick(7));
        myView.action8.setOnClickListener(new MyOnClick(8));

        myView.saveMidiCode.setOnClickListener(view -> {
            mainActivityInterface.getMidi().setMidiAction(which,myView.newCode.getText().toString());
            mainActivityInterface.getShowToast().doIt(success_string);
        });
    }

    private void setWhich(int which) {
        this.which = which;
    }

    private void changeHighlight() {
        ViewCompat.setBackgroundTintList(myView.action1, ColorStateList.valueOf(which==1 ? on:off));
        ViewCompat.setBackgroundTintList(myView.action2, ColorStateList.valueOf(which==2 ? on:off));
        ViewCompat.setBackgroundTintList(myView.action3, ColorStateList.valueOf(which==3 ? on:off));
        ViewCompat.setBackgroundTintList(myView.action4, ColorStateList.valueOf(which==4 ? on:off));
        ViewCompat.setBackgroundTintList(myView.action5, ColorStateList.valueOf(which==5 ? on:off));
        ViewCompat.setBackgroundTintList(myView.action6, ColorStateList.valueOf(which==6 ? on:off));
        ViewCompat.setBackgroundTintList(myView.action7, ColorStateList.valueOf(which==7 ? on:off));
        ViewCompat.setBackgroundTintList(myView.action8, ColorStateList.valueOf(which==8 ? on:off));
    }

    private class MyOnClick implements View.OnClickListener {

        int whichButton;
        MyOnClick(int whichButton) {
            this.whichButton = whichButton;
        }

        @Override
        public void onClick(View view) {
            Log.d(TAG,"whichButton:"+whichButton);
            setWhich(whichButton);
            view.post(MidiActionBottomSheet.this::changeHighlight);
            String currentCode = mainActivityInterface.getMidi().getMidiAction(whichButton);
            myView.currentCode.setHint(currentCode);
        }
    }

}

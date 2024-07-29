package com.garethevans.church.opensongtablet.midi;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetMidiSliderSettingsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class MidiControllerBottomSheet extends BottomSheetDialogFragment {

    // This bottom sheet allows the user to select the MIDI controller to assign to the MIDI board slider

    private BottomSheetMidiSliderSettingsBinding myView;
    private MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "MidiControllerBottomS";

    private String title, string_title, string_midi_board_website;
    private int channel, controller;
    private boolean justTitle = false;

    private final BottomSheetDialogFragment bottomSheetDialogFragment;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        getValues(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialog1 -> {
            try {
                BottomSheetDialog d = (BottomSheetDialog) dialog1;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    BottomSheetBehavior.from(bottomSheet).setDraggable(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return dialog;
    }

    // Default contructor for fragment called if there is an issue or badly called
    @SuppressWarnings("unused")
    MidiControllerBottomSheet() {
        this.bottomSheetDialogFragment = null;
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    MidiControllerBottomSheet(BottomSheetDialogFragment bottomSheetDialogFragment, String title, int channel, int controller) {
        this.bottomSheetDialogFragment = bottomSheetDialogFragment;
        Log.d(TAG,"bottomSheetDialogFragment:"+bottomSheetDialogFragment);
        Log.d(TAG,"channel:"+channel);
        Log.d(TAG,"controller:"+controller);
        justTitle = channel == -1 && controller == -1;
        if (title == null) {
            title = "";
        }
        if (channel<1 || channel>16) {
            channel = 1;
        }
        if (controller < 0) {
            controller = 0;
        }
        this.title = title;
        this.channel = channel;
        this.controller = controller;
        Log.d(TAG,"channel:"+channel);
        Log.d(TAG,"controller:"+controller);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getValues(getContext());
        myView = BottomSheetMidiSliderSettingsBinding.inflate(inflater, container, false);
        myView.dialogHeading.setText(string_title);
        myView.dialogHeading.setWebHelp(mainActivityInterface, string_midi_board_website);
        myView.dialogHeading.setClose(this);

        // Set up dropdownvalues
        setupDropdowns();

        // Set up the title, channel and controller (if they exists)
        myView.midiName.setText(title);
        myView.midiChannel.setText(String.valueOf(channel));
        myView.midiController.setText(String.valueOf(controller));

        // The save button
        myView.doSave.setOnClickListener(view -> doSave());
        return myView.getRoot();
    }

    private void getValues(Context c) {
        if (c != null) {
            if (justTitle) {
                string_title = c.getString(R.string.midi_board) + " " + c.getString(R.string.title);
            } else {
                string_title = c.getString(R.string.midi_controller);
            }
            string_midi_board_website = c.getString(R.string.website_midi_board);
        }
    }

    private void setupDropdowns() {
        myView.midiController.setVisibility(justTitle ? View.GONE:View.VISIBLE);
        myView.midiChannel.setVisibility(justTitle ? View.GONE:View.VISIBLE);

        ArrayList<String> midiChannels = new ArrayList<>();
        ArrayList<String> midiControllers = new ArrayList<>();
        for (int x=1; x<=16; x++) {
            midiChannels.add(String.valueOf(x));
        }
        for (int x=0; x<=127; x++) {
            midiControllers.add(String.valueOf(x));
        }
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter channelAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.midiChannel, R.layout.view_exposed_dropdown_item, midiChannels);
            myView.midiChannel.setAdapter(channelAdapter);
            ExposedDropDownArrayAdapter controllerAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.midiController, R.layout.view_exposed_dropdown_item, midiControllers);
            myView.midiController.setAdapter(controllerAdapter);
        }
    }

    private void doSave() {
        if (bottomSheetDialogFragment!=null) {
            if (justTitle) {
                ((MidiBoardBottomSheet) bottomSheetDialogFragment).editTitle(myView.midiName.getText().toString());
            } else {
                ((MidiBoardBottomSheet) bottomSheetDialogFragment).editSlider(myView.midiName.getText().toString(), myView.midiChannel.getText().toString(), myView.midiController.getText().toString());
            }
            dismiss();
        }
    }
}

package com.garethevans.church.opensongtablet.midi;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetInlineMidiBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.EditSongFragmentLyrics;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class InlineMidiBottomSheet extends BottomSheetDialogFragment {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "InlineMidiBottomSheet";
    private MainActivityInterface mainActivityInterface;
    private final EditSongFragmentLyrics editSongFragmentLyrics;
    private BottomSheetInlineMidiBinding myView;
    private String website_inline_midi_string="";
    private String note_on_string="";
    private String note_off_string="";
    private String beat_buddy_string="";
    private String transition_string="";
    private String transition_next_string="";
    private String value_string="";
    private String transition_previous_string="";
    private String transition_exit_string="";
    private String exclusive_transition_string="";
    private String exclusive_transition_next_string="";
    private String exclusive_transition_previous_string="";
    private String exclusive_transition_exit_string="";
    private String half_time_string="";
    private String half_time_exit_string="";
    private String double_time_string="";
    private String double_time_exit_string="";
    private String tempo_string="";
    private String volume_string="";
    private String folder_string="";
    private String song_string="";
    private String folder_song_string="";
    private String start_string="";
    private String stop_string="";
    private String pause_string="";
    private String fill_string="";
    private String accent_string="";
    private String note_string="";
    private String velocity_string="";
    private String inline_midi_string="";
    private String part_string="";
    private String sysex_start_string="";
    private String sysex_stop_string="";

    private ArrayList<String> midiActions, midiChannels, range0_127, range1_127, range0_100,
            range1_32, range40_300;
    private SparseArray<String> value1Hints, value2Hints, value1NumRange, value2NumRange;

    public InlineMidiBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        editSongFragmentLyrics = null;
        dismiss();
    }
    public InlineMidiBottomSheet(EditSongFragmentLyrics editSongFragmentLyrics) {
        this.editSongFragmentLyrics = editSongFragmentLyrics;
    }

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
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        myView.dialogHeading.setWebHelp(mainActivityInterface, website_inline_midi_string);
        myView.dialogHeading.setText(inline_midi_string);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = BottomSheetInlineMidiBinding.inflate(inflater,container,false);
        prepareStrings();
        myView.dialogHeading.setClose(this);
        prepareArrays();
        prepareDropdowns();
        setListeners();
        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            inline_midi_string = getString(R.string.inline_midi);
            website_inline_midi_string = getString(R.string.website_inline_midi);
            String on_string = getString(R.string.on);
            String off_string = getString(R.string.off);
            note_string = getString(R.string.midi_note);
            note_on_string = note_string + " " + on_string;
            note_off_string = note_string + " " + off_string;
            velocity_string = getString(R.string.midi_velocity);
            beat_buddy_string = getString(R.string.beat_buddy) + " ";
            transition_string = getString(R.string.transition);
            String next_string = getString(R.string.next);
            String previous_string = getString(R.string.previous);
            transition_next_string = beat_buddy_string + transition_string + " " + next_string;
            transition_previous_string = beat_buddy_string + transition_string + " " + previous_string;
            String exit_string = getString(R.string.exit);
            transition_exit_string = beat_buddy_string + transition_string + " " + exit_string;
            String exclusive_string = getString(R.string.exclusive);
            exclusive_transition_string = beat_buddy_string + transition_string + " (" + exclusive_string + ")";
            exclusive_transition_next_string = exclusive_transition_string + " " + next_string;
            exclusive_transition_previous_string = exclusive_transition_string + " " + previous_string;
            exclusive_transition_exit_string = exclusive_transition_string + " " + exit_string;
            half_time_string = beat_buddy_string + getString(R.string.half_time);
            half_time_exit_string = half_time_string + " " + exit_string;
            double_time_string = beat_buddy_string + getString(R.string.double_time);
            double_time_exit_string = double_time_string + " " + exit_string;
            tempo_string = beat_buddy_string + getString(R.string.tempo);
            volume_string = beat_buddy_string + getString(R.string.volume);
            folder_string = getString(R.string.folder);
            song_string = getString(R.string.song);
            folder_song_string = beat_buddy_string + folder_string + "/" + song_string;
            start_string = beat_buddy_string + getString(R.string.start);
            stop_string = beat_buddy_string + getString(R.string.stop);
            pause_string = beat_buddy_string + getString(R.string.pause);
            fill_string = beat_buddy_string + getString(R.string.fill);
            accent_string = beat_buddy_string + getString(R.string.accent);
            value_string = getString(R.string.midi_value);
            part_string = getString(R.string.part);
            sysex_start_string = getString(R.string.midi_sysex) + " " + getString(R.string.start);
            sysex_stop_string = getString(R.string.midi_sysex) + " " + getString(R.string.stop);
        }
    }

    private void prepareArrays() {
        midiChannels = new ArrayList<>();
        for (int i = 1; i <= 16; i++) {
            midiChannels.add(String.valueOf(i));
        }

        range0_100 = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            range0_100.add(String.valueOf(i));
        }

        range0_127 = new ArrayList<>();
        for (int i = 0; i <= 127; i++) {
            range0_127.add(String.valueOf(i));
        }

        range1_127 = new ArrayList<>();
        for (int i = 1; i <= 127; i++) {
            range1_127.add(String.valueOf(i));
        }

        range1_32 = new ArrayList<>();
        for (int i = 1; i <= 32; i++) {
            range1_32.add(String.valueOf(i));
        }

        range40_300 = new ArrayList<>();
        for (int i = 40; i <= 300; i++) {
            range40_300.add(String.valueOf(i));
        }

        midiActions = new ArrayList<>();
        value1Hints = new SparseArray<>();
        value2Hints = new SparseArray<>();
        value1NumRange = new SparseArray<>();
        value2NumRange = new SparseArray<>();

        // 0
        midiActions.add("CC");
        value1Hints.put(0,value_string + " 1");
        value2Hints.put(0,value_string + " 2");
        value1NumRange.put(0,"0-127");
        value2NumRange.put(0,"0-127");

        // 1
        midiActions.add("PC");
        value1Hints.put(1,value_string);
        value1NumRange.put(1,"0-127");

        // 2
        midiActions.add("MSB");
        value1Hints.put(2,value_string);
        value1NumRange.put(2,"0-127");

        // 3
        midiActions.add("LSB");
        value1Hints.put(3,value_string);
        value1NumRange.put(3,"0-127");

        // 4
        midiActions.add(note_on_string);
        value1Hints.put(4,note_string);
        value2Hints.put(4,velocity_string);
        value1NumRange.put(4,"0-127");
        value2NumRange.put(4,"0-127");

        // 5
        midiActions.add(note_off_string);
        value1Hints.put(5,note_string);
        value1NumRange.put(5,"0-127");

        // 6
        midiActions.add(beat_buddy_string + transition_string);
        value1Hints.put(6,part_string);
        value1NumRange.put(6,"1-32");

        // 7
        midiActions.add(transition_next_string);

        // 8
        midiActions.add(transition_previous_string);

        // 9
        midiActions.add(transition_exit_string);

        // 10
        midiActions.add(exclusive_transition_string);
        value1Hints.put(10,part_string);
        value1NumRange.put(10,"1-32");

        // 11
        midiActions.add(exclusive_transition_next_string);

        // 12
        midiActions.add(exclusive_transition_previous_string);

        // 13
        midiActions.add(exclusive_transition_exit_string);

        // 14
        midiActions.add(half_time_string);

        // 15
        midiActions.add(half_time_exit_string);

        // 16
        midiActions.add(double_time_string);

        // 17
        midiActions.add(double_time_exit_string);

        // 18
        midiActions.add(tempo_string);
        value1Hints.put(18,tempo_string);
        value1NumRange.put(18,"40-300");

        // 19
        midiActions.add(volume_string);
        value1Hints.put(19,volume_string);
        value1NumRange.put(19,"0-100");

        // 20
        midiActions.add(folder_song_string);
        value1Hints.put(20,folder_string);
        value2Hints.put(20,song_string);
        value1NumRange.put(20,"1-127");
        value2NumRange.put(20,"1-127");

        // 21
        midiActions.add(start_string);

        // 22
        midiActions.add(stop_string);

        // 23
        midiActions.add(pause_string);

        // 24
        midiActions.add(fill_string);

        // 25
        midiActions.add(accent_string);

        // 26
        midiActions.add(sysex_start_string);

        //27
        midiActions.add(sysex_stop_string);
    }
    private void prepareDropdowns() {
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter channelAdapter = new ExposedDropDownArrayAdapter(getContext(), myView.midiChannel, R.layout.view_exposed_dropdown_item, midiChannels);
            myView.midiChannel.setAdapter(channelAdapter);
            myView.midiChannel.setText("1");

            ExposedDropDownArrayAdapter actionAdapter = new ExposedDropDownArrayAdapter(getContext(),myView.midiAction,R.layout.view_exposed_dropdown_item,midiActions);
            myView.midiAction.setAdapter(actionAdapter);
            myView.midiAction.setText(actionAdapter.getItem(0));

        }
        changeDropDowns(0);
    }

    private void changeDropDowns(int actionIndex) {
        if (getContext()!=null) {
            // The actionIndex is the chosen action position
            // Set the hints for the value1 and value2 dropdowns
            String hint1 = value1Hints.get(actionIndex, "");
            String hint2 = value2Hints.get(actionIndex, "");

            myView.midiValue1.setHint(hint1);
            myView.midiValue2.setHint(hint2);

            // Hide the ones not required
            myView.midiValue1.setVisibility(hint1.isEmpty() ? View.GONE : View.VISIBLE);
            myView.midiValue2.setVisibility(hint2.isEmpty() ? View.GONE : View.VISIBLE);

            // Build the new adapters
            String range1 = value1NumRange.get(actionIndex, "");
            String range2 = value2NumRange.get(actionIndex, "");
            if (range1.isEmpty()) {
                myView.midiValue1.setAdapter(null);
            } else {
                ExposedDropDownArrayAdapter adapter1;
                switch (range1) {
                    case "0-100":
                        adapter1 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue1, R.layout.view_exposed_dropdown_item, range0_100);
                        break;

                    default:
                    case "0-127":
                        adapter1 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue1, R.layout.view_exposed_dropdown_item, range0_127);
                        break;

                    case "1-127":
                        adapter1 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue1, R.layout.view_exposed_dropdown_item, range1_127);
                        break;

                    case "40-300":
                        adapter1 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue1, R.layout.view_exposed_dropdown_item, range40_300);
                        break;

                    case "1-32":
                        adapter1 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue1, R.layout.view_exposed_dropdown_item, range1_32);
                        break;
                }
                myView.midiValue1.setAdapter(adapter1);
                myView.midiValue1.setText(adapter1.getItem(0));
            }
            if (range2.isEmpty()) {
                myView.midiValue2.setAdapter(null);
            } else {
                ExposedDropDownArrayAdapter adapter2;
                switch (range2) {
                    case "0-100":
                        adapter2 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue2, R.layout.view_exposed_dropdown_item, range0_100);
                        break;

                    default:
                    case "0-127":
                        adapter2 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue2, R.layout.view_exposed_dropdown_item, range0_127);
                        break;

                    case "1-127":
                        adapter2 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue2, R.layout.view_exposed_dropdown_item, range1_127);
                        break;

                    case "40-300":
                        adapter2 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue2, R.layout.view_exposed_dropdown_item, range40_300);
                        break;

                    case "1-32":
                        adapter2 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue2, R.layout.view_exposed_dropdown_item, range1_32);
                        break;
                }
                myView.midiValue2.setAdapter(adapter2);
                myView.midiValue2.setText(adapter2.getItem(0));
            }

            // Update the code
            updateCode();
        }
    }


    private void setListeners() {
        myView.midiChannel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                updateCode();
            }
        });
        myView.midiAction.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                // Decide which index we selected
                changeDropDowns(midiActions.indexOf(myView.midiAction.getText().toString()));
            }
        });
        myView.midiValue1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                updateCode();
            }
        });
        myView.midiValue2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                updateCode();
            }
        });

        myView.addCommand.setOnClickListener(view -> {
            if (editSongFragmentLyrics!=null) {
                editSongFragmentLyrics.insertSection(myView.messageCode.getText().toString(), 0);
            }
            dismiss();
        });
    }

    private void updateCode() {
        // Build the inline MIDI code
        // Start with the MIDI declaration and channel
        String part1 = ";MIDI" + myView.midiChannel.getText().toString();
        String part2 = "";
        String part3 = "";

        String val1 = "";
        String val2 = "";
        if (myView.midiValue1.getText()!=null && !myView.midiValue1.getText().toString().isEmpty()) {
            val1 = myView.midiValue1.getText().toString();
        }
        if (myView.midiValue2.getText()!=null && !myView.midiValue2.getText().toString().isEmpty()) {
            val2 = myView.midiValue2.getText().toString();
        }

        // Now the rest depends on the action
        int position = midiActions.indexOf(myView.midiAction.getText().toString());
        switch (position) {
            case 0:
                // CC
                part2 = "CC" + val1;
                part3 = val2;
                break;

            case 1:
                // PC
                part2 = "PC" + val1;
                break;

            case 2:
                // MSB
                part2 = "MSB" + val1;
                break;

            case 3:
                // LSB
                part2 = "LSB" + val1;
                break;

            case 4:
                // Note on
                part2 = "NO" + val1;
                part3 = val2;
                break;

            case 5:
                // Note off
                part2 = "NX" + val1;
                break;

            case 6:
                // Transition
                part2 = "BBT" + val1;
                break;

            case 7:
                // Transition next
                part2 = "BBTN";
                break;

            case 8:
                // Transition previous
                part2 = "BBTP";
                break;

            case 9:
                // Transition exit
                part2 = "BBTX";
                break;

            case 10:
                // Exclusive transition
                part2 = "BBTE" + val1;
                break;

            case 11:
                // Exclusive transition next
                part2 = "BBTEN";
                break;

            case 12:
                // Exclusive transition previous
                part2 = "BBTEP";
                break;

            case 13:
                // Exclusive transition exit
                part2 = "BBTEX";
                break;

            case 14:
                // Half time
                part2 = "BBH";
                break;

            case 15:
                // Half time exit
                part2 = "BBHX";
                break;

            case 16:
                // Double time
                part2 = "BBD";
                break;

            case 17:
                // Double time exit
                part2 = "BBDX";
                break;

            case 18:
                // Tempo
                part2 = "BBBPM" + val1;
                break;

            case 19:
                // Volume
                part2 = "BBV" + val1;
                break;

            case 20:
                // Folder/song
                part2 = "BBS" + val1 + "/" + val2;
                break;

            case 21:
                // Start
                part2 = "BBI";
                break;

            case 22:
                // Stop
                part2 = "BBO";
                break;

            case 23:
                // Pause
                part2 = "BBP";
                break;

            case 24:
                // Fill
                part2 = "BBF";
                break;

            case 25:
                // Accent
                part2 = "BBA";
                break;

            case 26:
                // Sysex start
                part1 = ";MIDI";
                part2 = "START";
                break;

            case 27:
                // Sysex stop
                part1 = ";MIDI";
                part2 = "STOP";
                break;

        }

        String midiMessage = part1;
        if (!part2.isEmpty()) {
            midiMessage += ":" + part2;
        }
        if (!part3.isEmpty()) {
            midiMessage += ":" + part3;
        }
        myView.messageCode.setText(midiMessage);
    }
}

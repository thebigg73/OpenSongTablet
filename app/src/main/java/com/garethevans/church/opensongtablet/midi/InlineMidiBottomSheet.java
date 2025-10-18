package com.garethevans.church.opensongtablet.midi;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetInlineMidiBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.EditSongFragmentLyrics;

import java.util.ArrayList;

public class InlineMidiBottomSheet extends BottomSheetCommon {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "InlineMidiBottomSheet";
    private MainActivityInterface mainActivityInterface;
    private final EditSongFragmentLyrics editSongFragmentLyrics;
    private BottomSheetInlineMidiBinding myView;
    private String website_inline_midi_string="";
    private String note_on_string="";
    private String note_off_string="";
    private String on_string;
    private String off_string;
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
    private String guitar_rhythmic_string, guitar_compressor_string, guitar_modulation_string,
            guitar_octaver_string, guitar_amp_string, guitar_wah_string, guitar_boost_string,
            guitar_reverb_string, guitar_delay_string, guitar_hit_string,
            vocal_rhythmic_string, vocal_harmony_string, scale_string,
            key_string, vocal_harmony_key_string, vocal_harmony_scale_string,
            vocal_harmony_boost_string, vocal_harmony_hold_string,
            vocal_vocoder_synth_string, vocal_choir_string, vocal_double_string,
            vocal_hard_tune_string, vocal_modulation_string, vocal_transducer_string,
            vocal_reverb_string, vocal_delay_string, vocal_hit_string,
            step_string, notes_off_string, on_off_string, preset_string;


    private ArrayList<String> midiActions, midiChannels, range0_127, range1_127, range0_100,
            range1_32, range40_300, range1_500, on_off, keys, scales;
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
            on_string = getString(R.string.on);
            off_string = getString(R.string.off);
            on_off_string = on_string + "/" + off_string;
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

            // VoiceLive generic strings
            String voicelive_string = getString(R.string.voicelive);
            String guitar_string = "(" + getString(R.string.guitar) + ")";
            String vocal_string = "(" + getString(R.string.vocal) + ")";
            String harmony_string = getString(R.string.harmony);
            String hit_string = getString(R.string.hit);
            key_string = getString(R.string.key);
            scale_string = getString(R.string.scale);
            preset_string = voicelive_string + " " + getString(R.string.preset);

            // Guitar effect strings
            guitar_rhythmic_string = voicelive_string + " " + guitar_string + " " + getString(R.string.rhythmic);
            guitar_compressor_string = voicelive_string + " " + guitar_string + " " + getString(R.string.compressor);
            guitar_modulation_string = voicelive_string + " " + guitar_string + " " + getString(R.string.modulation);
            guitar_octaver_string = voicelive_string + " " + guitar_string + " " + getString(R.string.octaver);
            guitar_amp_string = voicelive_string + " " + guitar_string + " " + getString(R.string.amp);
            guitar_wah_string = voicelive_string + " " + guitar_string + " " + getString(R.string.wah);
            guitar_boost_string = voicelive_string + " " + guitar_string + " " + getString(R.string.boost);
            guitar_reverb_string = voicelive_string + " " + guitar_string + " " + getString(R.string.reverb);
            guitar_delay_string = voicelive_string + " " + guitar_string + " " + getString(R.string.delay);
            guitar_hit_string = voicelive_string + " " + guitar_string + " " + hit_string;

            // Vocal effect combined strings
            vocal_rhythmic_string = voicelive_string + " " + vocal_string + " " + getString(R.string.rhythmic);
            vocal_harmony_string = voicelive_string + " " + vocal_string + " " + getString(R.string.harmony);
            vocal_harmony_key_string = voicelive_string + " " + vocal_string + " " + harmony_string + " " + key_string;
            vocal_harmony_scale_string = voicelive_string + " " + vocal_string + " " + harmony_string + " " + getString(R.string.scale);
            vocal_harmony_boost_string = voicelive_string + " " + vocal_string + " " + getString(R.string.harmony_vibrato_boost);
            vocal_harmony_hold_string = voicelive_string + " " + vocal_string + " " + getString(R.string.harmony_hold);
            vocal_vocoder_synth_string = voicelive_string + " " + vocal_string + " " + getString(R.string.vocoder_synth);
            vocal_choir_string = voicelive_string + " " + vocal_string + " " + getString(R.string.choir);
            vocal_double_string = voicelive_string + " " + vocal_string + " " + getString(R.string.double_string);
            vocal_hard_tune_string = voicelive_string + " " + vocal_string + " " + getString(R.string.hard_tune);
            vocal_modulation_string = voicelive_string + " " + vocal_string + " " + getString(R.string.modulation);
            vocal_transducer_string = voicelive_string + " " + vocal_string + " " + getString(R.string.transducer);
            vocal_reverb_string = voicelive_string + " " + vocal_string + " " + getString(R.string.reverb);
            vocal_delay_string = voicelive_string + " " + vocal_string + " " + getString(R.string.delay);
            vocal_hit_string = voicelive_string + " " + vocal_string + " " + hit_string;

            // Other voicelive settings
            step_string = voicelive_string + " " + getString(R.string.step);
            notes_off_string = voicelive_string + " " + getString(R.string.panic_stop);

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

        range1_500 = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            range1_500.add(String.valueOf(i));
        }

        range1_32 = new ArrayList<>();
        for (int i = 1; i <= 32; i++) {
            range1_32.add(String.valueOf(i));
        }

        range40_300 = new ArrayList<>();
        for (int i = 40; i <= 300; i++) {
            range40_300.add(String.valueOf(i));
        }

        on_off = new ArrayList<>();
        on_off.add(off_string);
        on_off.add(on_string);

        keys = new ArrayList<>();
        keys.add("A");
        keys.add("A#");
        keys.add("Bb");
        keys.add("B");
        keys.add("C");
        keys.add("C#");
        keys.add("Db");
        keys.add("D");
        keys.add("D#");
        keys.add("Eb");
        keys.add("E");
        keys.add("F");
        keys.add("F#");
        keys.add("Gb");
        keys.add("G");
        keys.add("G#");
        keys.add("Ab");

        scales = new ArrayList<>();
        scales.add("MAJ1");
        scales.add("MAJ2");
        scales.add("MAJ3");
        scales.add("MIN1");
        scales.add("MIN2");
        scales.add("MIN3");
        scales.add("CUST");

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
        midiActions.add(guitar_rhythmic_string);
        value1Hints.put(26,on_off_string);
        value1NumRange.put(26,"off-on");

        // 27
        midiActions.add(guitar_compressor_string);
        value1Hints.put(27,on_off_string);
        value1NumRange.put(27,"off-on");

        // 28
        midiActions.add(guitar_modulation_string);
        value1Hints.put(28,on_off_string);
        value1NumRange.put(28,"off-on");

        // 29
        midiActions.add(guitar_octaver_string);
        value1Hints.put(29,on_off_string);
        value1NumRange.put(29,"off-on");

        // 30
        midiActions.add(guitar_amp_string);
        value1Hints.put(30,on_off_string);
        value1NumRange.put(30,"off-on");

        // 31
        midiActions.add(guitar_wah_string);
        value1Hints.put(31,on_off_string);
        value1NumRange.put(31,"off-on");

        // 32
        midiActions.add(guitar_boost_string);
        value1Hints.put(32,on_off_string);
        value1NumRange.put(32,"off-on");

        // 33
        midiActions.add(guitar_reverb_string);
        value1Hints.put(33,on_off_string);
        value1NumRange.put(33,"off-on");

        // 34
        midiActions.add(guitar_delay_string);
        value1Hints.put(34,on_off_string);
        value1NumRange.put(34,"off-on");

        // 35
        midiActions.add(guitar_hit_string);
        value1Hints.put(35,on_off_string);
        value1NumRange.put(35,"off-on");

        // 36
        midiActions.add(vocal_rhythmic_string);
        value1Hints.put(36,on_off_string);
        value1NumRange.put(36,"off-on");

        // 37
        midiActions.add(vocal_harmony_string);
        value1Hints.put(37,on_off_string);
        value1NumRange.put(37,"off-on");

        // 38
        midiActions.add(vocal_harmony_key_string);
        value1Hints.put(38,key_string);
        value1NumRange.put(38,"keys");

        // 39
        midiActions.add(vocal_harmony_scale_string);
        value1NumRange.put(39,scale_string);
        value1NumRange.put(39,"scales");

        // 40
        midiActions.add(vocal_harmony_boost_string);
        value1Hints.put(40,on_off_string);
        value1NumRange.put(40,"off-on");

        // 41
        midiActions.add(vocal_harmony_hold_string);
        value1Hints.put(41,on_off_string);
        value1NumRange.put(41,"off-on");

        // 42
        midiActions.add(vocal_vocoder_synth_string);
        value1Hints.put(42,on_off_string);
        value1NumRange.put(42,"off-on");

        // 43
        midiActions.add(vocal_choir_string);
        value1Hints.put(43,on_off_string);
        value1NumRange.put(43,"off-on");

        // 44
        midiActions.add(vocal_double_string);
        value1Hints.put(44,on_off_string);
        value1NumRange.put(44,"off-on");

        // 45
        midiActions.add(vocal_hard_tune_string);
        value1Hints.put(45,on_off_string);
        value1NumRange.put(45,"off-on");

        // 46
        midiActions.add(vocal_modulation_string);
        value1Hints.put(46,on_off_string);
        value1NumRange.put(46,"off-on");

        // 47
        midiActions.add(vocal_transducer_string);
        value1Hints.put(47,on_off_string);
        value1NumRange.put(47,"off-on");

        // 48
        midiActions.add(vocal_reverb_string);
        value1Hints.put(48,on_off_string);
        value1NumRange.put(48,"off-on");

        // 49
        midiActions.add(vocal_delay_string);
        value1Hints.put(49,on_off_string);
        value1NumRange.put(49,"off-on");

        // 50
        midiActions.add(vocal_hit_string);
        value1Hints.put(50,on_off_string);
        value1NumRange.put(50,"off-on");

        // 51
        midiActions.add(step_string);
        value1Hints.put(51,on_off_string);
        value1NumRange.put(51,"1-32");

        // 52
        midiActions.add(preset_string);
        value1Hints.put(52,preset_string);
        value1NumRange.put(52,"1-500");

        // 53
        midiActions.add(notes_off_string);

        // 54
        midiActions.add(sysex_start_string);

        // 55
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

                    case "1-500":
                        adapter1 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue1, R.layout.view_exposed_dropdown_item, range1_500);
                        break;

                    case "40-300":
                        adapter1 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue1, R.layout.view_exposed_dropdown_item, range40_300);
                        break;

                    case "1-32":
                        adapter1 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue1, R.layout.view_exposed_dropdown_item, range1_32);
                        break;

                    case "off-on":
                        adapter1 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue1, R.layout.view_exposed_dropdown_item, on_off);
                        break;

                    case "keys":
                        adapter1 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue1, R.layout.view_exposed_dropdown_item, keys);
                        break;

                    case "scales":
                        adapter1 = new ExposedDropDownArrayAdapter(getContext(), myView.midiValue1, R.layout.view_exposed_dropdown_item, scales);
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
                // Guitar rhythm
                part2 = "VLGR" + (val1.equals(off_string) ? "X" : "");
                break;

            case 27:
                // Guitar compressor
                part2 = "VLGC" + (val1.equals(off_string) ? "X" : "");
                break;

            case 28:
                // Guitar modulation
                part2 = "VLGM" + (val1.equals(off_string) ? "X" : "");
                break;

            case 29:
                // Guitar octaver
                part2 = "VLGO" + (val1.equals(off_string) ? "X" : "");
                break;

            case 30:
                // Guitar amp
                part2 = "VLGA" + (val1.equals(off_string) ? "X" : "");
                break;

            case 31:
                // Guitar wah
                part2 = "VLGW" + (val1.equals(off_string) ? "X" : "");
                break;

            case 32:
                // Guitar boost
                part2 = "VLGB" + (val1.equals(off_string) ? "X" : "");
                break;

            case 33:
                // Guitar reverb
                part2 = "VLGRV" + (val1.equals(off_string) ? "X" : "");
                break;

            case 34:
                // Guitar delay
                part2 = "VLGD" + (val1.equals(off_string) ? "X" : "");
                break;

            case 35:
                // Guitar hit
                part2 = "VLGHIT" + (val1.equals(off_string) ? "X" : "");
                break;



            case 36:
                // Vocal rhythmic
                part2 = "VLVR" + (val1.equals(off_string) ? "X" : "");
                break;

            case 37:
                // Vocal harmony
                part2 = "VLVH" + (val1.equals(off_string) ? "X" : "");
                break;

            case 38:
                // Vocal harmony key
                part2 = "VLVHK" + val1;
                break;

            case 39:
                // Vocal harmony scale
                part2 = "VLVHS" + val1;
                break;

            case 40:
                // Vocal harmony vibrato boost
                part2 = "VLVHVB" + (val1.equals(off_string) ? "X" : "");
                break;

            case 41:
                // Vocal harmony hold
                part2 = "VLVHH" + (val1.equals(off_string) ? "X" : "");
                break;

            case 42:
                // Vocal vocoder/synth
                part2 = "VLVV" + (val1.equals(off_string) ? "X" : "");
                break;

            case 43:
                // Vocal choir
                part2 = "VLVCH" + (val1.equals(off_string) ? "X" : "");
                break;

            case 44:
                // Vocal double
                part2 = "VLVDB" + (val1.equals(off_string) ? "X" : "");
                break;

            case 45:
                // Vocal hard tune
                part2 = "VLVHT" + (val1.equals(off_string) ? "X" : "");
                break;

            case 46:
                // Vocal modulation
                part2 = "VLVM" + (val1.equals(off_string) ? "X" : "");
                break;

            case 47:
                // Vocal transducer
                part2 = "VLVTX" + (val1.equals(off_string) ? "X" : "");
                break;

            case 48:
                // Vocal reverb
                part2 = "VLVRV" + (val1.equals(off_string) ? "X" : "");
                break;

            case 49:
                // Vocal delay
                part2 = "VLVD" + (val1.equals(off_string) ? "X" : "");
                break;

            case 50:
                // Vocal hit
                part2 = "VLVHIT" + (val1.equals(off_string) ? "X" : "");
                break;

            case 51:
                // Step
                part2 = "VLS" + val1;
                break;

            case 52:
                // Preset
                part2 = "VLP" + val1;
                break;

            case 53:
                // All notes off
                part2 = "VLNX";
                break;

            case 54:
                // Sysex start
                part1 = ";MIDI";
                part2 = "START";
                break;

            case 55:
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

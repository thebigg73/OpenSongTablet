package com.garethevans.church.opensongtablet.midi;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDown;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.BottomSheetMidiShorthandBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class MidiShortHandBottomSheet extends BottomSheetCommon {

    // This bottom sheet allows the user to build MIDI shorthand code from dropdowns

    private BottomSheetMidiShorthandBinding myView;
    private MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MidiShortHandBottomS";
    private String string_midi_shorthand, string_midi_shorthand_website, string_program_change,
        string_controller_change, string_note_on, string_note_off, string_beatbuddy_transition,
            string_beatbuddy_transition_exit, string_beatbuddy_transition_next, string_tempo,
            string_beatbuddy_transition_previous, string_beatbuddy_transition_exclusive,
            string_beatbuddy_transition_exclusive_exit, string_beatbuddy_transition_exclusive_next,
            string_beatbuddy_transition_exclusive_previous, string_beatbuddy_half_time, string_song,
            string_beatbuddy_half_time_exit, string_beatbuddy_double_time, string_folder,
            string_beatbuddy_double_time_exit, string_beatbuddy_tempo, string_beatbuddy_volume,
            string_beatbuddy_volume_headphones, string_beatbuddy_song, string_beatbuddy_intro,
            string_beatbuddy_outro, string_beatbuddy_pause, string_beatbuddy_accent, string_velocity,
            string_beatbuddy_fill, string_volume, string_note, string_value, command, midiCode,
            string_sysex_start, string_sysex_stop;
    private String guitar_rhythmic_string, guitar_compressor_string, guitar_modulation_string,
            guitar_octaver_string, guitar_amp_string, guitar_wah_string, guitar_boost_string,
            guitar_reverb_string, guitar_delay_string, guitar_hit_string,
            vocal_rhythmic_string, vocal_harmony_string, scale_string,
            key_string, vocal_harmony_key_string, vocal_harmony_scale_string,
            vocal_harmony_boost_string, vocal_harmony_hold_string,
            vocal_vocoder_synth_string, vocal_choir_string, vocal_double_string,
            vocal_hard_tune_string, vocal_modulation_string, vocal_transducer_string,
            vocal_reverb_string, vocal_delay_string, vocal_hit_string,
            step_string, notes_off_string, preset_string, off_string, on_string,
            string_voicelive_midi_channel;

    @SuppressWarnings("FieldCanBeLocal")
    private ArrayList<String> midiChannel, midiCommand, midiCommandDescription, midiValue0_100,
            midiValue0_127, midiValue1_127, midiValue1_128, midiValue40_300, midiNotes,
            midiValue1_32, midiValue1_500, midiValueOff_On, midiValueKeys, midiValueScales;
    private boolean usingVal1=false, usingVal2=false, usingSysEx=false;
    // Calling fragment/dialogfragment
    private final BottomSheetDialogFragment bottomSheetDialogFragment;
    private final Fragment fragment;
    private final String fragTag;
    private String midiName, midiCurrent;

    private ArrayList<MidiInfo> midiInfos;
    private LinearLayoutManager llm;
    private MidiMessagesAdapter midiMessagesAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        getValues(context);
    }

    // Default contructor for fragment called if there is an issue or badly called
    @SuppressWarnings("unused")
    MidiShortHandBottomSheet() {
        this.fragment = null;
        this.bottomSheetDialogFragment = null;
        this.fragTag = null;
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    MidiShortHandBottomSheet(Fragment fragment, BottomSheetDialogFragment bottomSheetDialogFragment, String fragTag, String midiName, String midiCurrent) {
        this.fragment = fragment;
        this.bottomSheetDialogFragment = bottomSheetDialogFragment;
        this.fragTag = fragTag;
        this.midiName = midiName;
        this.midiCurrent = midiCurrent;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getValues(getContext());
        myView = BottomSheetMidiShorthandBinding.inflate(inflater,container,false);
        myView.dialogHeading.setText(string_midi_shorthand);
        myView.dialogHeading.setWebHelp(mainActivityInterface,string_midi_shorthand_website);
        myView.dialogHeading.setClose(this);

        // Set up dropdownvalues
        setupDropdowns();

        // Set up the name (if it exists)
        if (midiName == null) {
            myView.midiName.setVisibility(View.GONE);
            myView.midiNameDivider.setVisibility(View.GONE);
            midiName = "";
        }
        myView.midiName.setText(midiName);

        // Set up the adapter for the recycler
        setupAdapter();
        buildList();

        // Set the listeners
        setupListeners();

        return myView.getRoot();
    }

    private void getValues(Context c) {
        if (c!=null) {
            String beatBuddy = c.getString(R.string.beat_buddy);
            String transition = c.getString(R.string.transition);
            String previous = c.getString(R.string.previous);
            String next = c.getString(R.string.next);
            String on = c.getString(R.string.on);
            String off = c.getString(R.string.off);
            String exit = c.getString(R.string.exit);
            String exclusive = c.getString(R.string.exclusive);
            String halftime = c.getString(R.string.half_time);
            String doubletime = c.getString(R.string.double_time);
            string_tempo = c.getString(R.string.tempo);
            string_volume = c.getString(R.string.volume);
            string_velocity = c.getString(R.string.midi_velocity);
            string_value = c.getString(R.string.midi_value);
            string_note = c.getString(R.string.note);
            string_midi_shorthand = c.getString(R.string.midi_code_new);
            string_midi_shorthand_website = c.getString(R.string.website_midi_shorthand);
            string_program_change = c.getString(R.string.midi_program);
            string_controller_change = c.getString(R.string.midi_controller);
            string_note_on = string_note + " " + on;
            string_note_off = string_note + " " + off;
            string_beatbuddy_transition = beatBuddy + " " + transition;
            string_beatbuddy_transition_exit = beatBuddy + " " + transition + " " + exit;
            string_beatbuddy_transition_next = beatBuddy + " " + transition + " " + next;
            string_beatbuddy_transition_previous = beatBuddy + " " + transition + " " + previous;
            string_beatbuddy_transition_exclusive = beatBuddy + " " + transition + " " + exclusive;
            string_beatbuddy_transition_exclusive_exit = beatBuddy + " " + transition + " " + exclusive + " " + exit;
            string_beatbuddy_transition_exclusive_next = beatBuddy + " " + transition + " " + exclusive + " " + next;
            string_beatbuddy_transition_exclusive_previous = beatBuddy + " " + transition + " " + exclusive + " " + previous;
            string_beatbuddy_half_time = beatBuddy + " " + halftime;
            string_beatbuddy_half_time_exit = beatBuddy + " " + halftime + " " + exit;
            string_beatbuddy_double_time = beatBuddy + " " + doubletime;
            string_beatbuddy_double_time_exit = beatBuddy + " " + doubletime + " " + exit;
            string_beatbuddy_tempo = beatBuddy + " " + string_tempo;
            string_beatbuddy_volume = beatBuddy + " " + string_volume;
            string_beatbuddy_volume_headphones = beatBuddy + " " + c.getString(R.string.volume_headphone);
            string_song = c.getString(R.string.song);
            string_folder = c.getString(R.string.folder);
            string_beatbuddy_song = beatBuddy + " " + string_song;
            string_beatbuddy_intro = beatBuddy + " " + c.getString(R.string.intro);
            string_beatbuddy_outro = beatBuddy + " " + c.getString(R.string.outro);
            string_beatbuddy_pause = beatBuddy + " " + c.getString(R.string.pause);
            string_beatbuddy_fill = beatBuddy + " " + c.getString(R.string.fill);
            string_beatbuddy_accent = beatBuddy + " " + c.getString(R.string.accent);
            string_sysex_start = getString(R.string.midi_sysex) + " " + getString(R.string.start);
            string_sysex_stop = getString(R.string.midi_sysex) + " " + getString(R.string.stop);

            // VoiceLive generic strings
            String voicelive_string = getString(R.string.voicelive);
            string_voicelive_midi_channel = voicelive_string + " MIDI " + c.getString(R.string.midi_channel);
            String guitar_string = "(" + getString(R.string.guitar) + ")";
            String vocal_string = "(" + getString(R.string.vocal) + ")";
            String harmony_string = getString(R.string.harmony);
            String hit_string = getString(R.string.hit);
            key_string = getString(R.string.key);
            scale_string = getString(R.string.scale);
            preset_string = voicelive_string + " " + getString(R.string.preset);
            off_string = getString(R.string.off);
            on_string = getString(R.string.on);

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

    private void setupDropdowns() {
        // Build the arrays (need String arrays for Dropdowns)

        // Build the numerical value arrays (as strings)
        midiChannel = new ArrayList<>();
        midiValue0_100 = new ArrayList<>();
        midiValue0_127 = new ArrayList<>();
        midiValue1_127 = new ArrayList<>();
        midiValue1_128 = new ArrayList<>();
        midiValue1_32 = new ArrayList<>();
        midiValue1_500 = new ArrayList<>();
        midiValueOff_On = new ArrayList<>();
        midiValueKeys = new ArrayList<>();
        midiValueScales = new ArrayList<>();
        midiValue40_300 = new ArrayList<>();
        midiNotes = new ArrayList<>(mainActivityInterface.getMidi().notes);

        for (int x=0; x<=500; x++) {
            if (x>=1 && x<=16) {
                // MIDI channels 1-6
                midiChannel.add(String.valueOf(x));
            }
            if (x>=1 && x<=32) {
                // Values between 1-32
                midiValue1_32.add(String.valueOf(x));
            }
            if (x<=127) {
                // Values between 0-127
                midiValue0_127.add(String.valueOf(x));
                if (x>=1) {
                    // Values between 1-127
                    midiValue1_127.add(String.valueOf(x));
                }
            }
            if (x>=1 && x<=128) {
                // Values between 1-128
                midiValue1_128.add(String.valueOf(x));
            }
            if (x<=100) {
                // Values between 0-100
                midiValue0_100.add(String.valueOf(x));
            }
            if (x>=40 && x<=300) {
                // Values between 40-300
                midiValue40_300.add(String.valueOf(x));
            }
            if (x>=1) {
                midiValue1_500.add(String.valueOf(x));
            }
        }
        midiValueOff_On.add(off_string);
        midiValueOff_On.add(on_string);

        midiValueKeys.add("A");
        midiValueKeys.add("A#");
        midiValueKeys.add("Bb");
        midiValueKeys.add("B");
        midiValueKeys.add("C");
        midiValueKeys.add("C#");
        midiValueKeys.add("Db");
        midiValueKeys.add("D");
        midiValueKeys.add("D#");
        midiValueKeys.add("Eb");
        midiValueKeys.add("E");
        midiValueKeys.add("F");
        midiValueKeys.add("F#");
        midiValueKeys.add("Gb");
        midiValueKeys.add("G");
        midiValueKeys.add("G#");
        midiValueKeys.add("Ab");

        midiValueScales.add("MAJ1");
        midiValueScales.add("MAJ2");
        midiValueScales.add("MAJ3");
        midiValueScales.add("MIN1");
        midiValueScales.add("MIN2");
        midiValueScales.add("MIN3");
        midiValueScales.add("CUST");

        if (getContext()!=null) {
            ExposedDropDownArrayAdapter channelAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.midiChannel, R.layout.view_exposed_dropdown_item, midiChannel);
            myView.midiChannel.setAdapter(channelAdapter);
        }
        myView.midiChannel.setText(String.valueOf(mainActivityInterface.getMidi().getMidiOutputChannel()));

        // The MIDI commands available (Name and shorthand)
        midiCommand = new ArrayList<>();
        midiCommandDescription = new ArrayList<>();
        setMidiCommand(string_program_change,"PC");
        setMidiCommand(string_controller_change,"CC");
        setMidiCommand(string_note_on,"NO");
        setMidiCommand(string_note_off,"NX");
        setMidiCommand("LSB","LSB");
        setMidiCommand("MSB","MSB");
        setMidiCommand(string_beatbuddy_transition,"BBT");
        setMidiCommand(string_beatbuddy_transition_exit,"BBTX");
        setMidiCommand(string_beatbuddy_transition_next,"BBTN");
        setMidiCommand(string_beatbuddy_transition_previous,"BBTP");
        setMidiCommand(string_beatbuddy_transition_exclusive,"BBTE");
        setMidiCommand(string_beatbuddy_transition_exclusive_exit,"BBTEX");
        setMidiCommand(string_beatbuddy_transition_exclusive_next,"BBTEN");
        setMidiCommand(string_beatbuddy_transition_exclusive_previous,"BBTEP");
        setMidiCommand(string_beatbuddy_half_time,"BBH");
        setMidiCommand(string_beatbuddy_half_time_exit,"BBHX");
        setMidiCommand(string_beatbuddy_double_time,"BBD");
        setMidiCommand(string_beatbuddy_double_time_exit,"BBDX");
        setMidiCommand(string_beatbuddy_tempo,"BBBPM");
        setMidiCommand(string_beatbuddy_volume,"BBV");
        setMidiCommand(string_beatbuddy_volume_headphones,"BBVH");
        setMidiCommand(string_beatbuddy_song,"BBS");
        setMidiCommand(string_beatbuddy_intro,"BBI");
        setMidiCommand(string_beatbuddy_outro,"BBO");
        setMidiCommand(string_beatbuddy_pause,"BBP");
        setMidiCommand(string_beatbuddy_accent,"BBA");
        setMidiCommand(string_beatbuddy_fill,"BBF");

        setMidiCommand(guitar_rhythmic_string,"VLGR");
        setMidiCommand(guitar_compressor_string,"VLGC");
        setMidiCommand(guitar_modulation_string,"VLGM");
        setMidiCommand(guitar_octaver_string,"VLGO");
        setMidiCommand(guitar_amp_string,"VLGA");
        setMidiCommand(guitar_wah_string,"VLGW");
        setMidiCommand(guitar_boost_string,"VLGB");
        setMidiCommand(guitar_reverb_string,"VLGRV");
        setMidiCommand(guitar_delay_string,"VLGD");
        setMidiCommand(guitar_hit_string,"VLGHIT");
        setMidiCommand(vocal_rhythmic_string,"VLVR");
        setMidiCommand(vocal_harmony_string,"VLVH");
        setMidiCommand(vocal_harmony_key_string,"VLVHK");
        setMidiCommand(vocal_harmony_scale_string,"VLVHS");
        setMidiCommand(vocal_harmony_boost_string,"VLVHVB");
        setMidiCommand(vocal_harmony_hold_string,"VLVHH");
        setMidiCommand(vocal_vocoder_synth_string,"VLVV");
        setMidiCommand(vocal_choir_string,"VLVCH");
        setMidiCommand(vocal_double_string,"VLVDB");
        setMidiCommand(vocal_hard_tune_string,"VLVHT");
        setMidiCommand(vocal_modulation_string,"VLVM");
        setMidiCommand(vocal_transducer_string,"VLVT");
        setMidiCommand(vocal_reverb_string,"VLVRV");
        setMidiCommand(vocal_delay_string,"VLVD");
        setMidiCommand(vocal_hit_string,"VLVHIT");
        setMidiCommand(notes_off_string,"VLNX");
        setMidiCommand(step_string,"VLS");
        setMidiCommand(preset_string,"VLP");

        setMidiCommand(string_sysex_start,"START");
        setMidiCommand(string_sysex_stop,"STOP");

        if (getContext()!=null) {
            ExposedDropDownArrayAdapter commandAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.midiCommand, R.layout.view_exposed_dropdown_item, midiCommandDescription);
            myView.midiCommand.setAdapter(commandAdapter);
        }
        myView.midiCommand.setText(midiCommandDescription.get(0));

        whichValues();
    }

    private void setMidiCommand(String name, String shorthand) {
        midiCommandDescription.add(name);
        midiCommand.add(shorthand);
    }

    private void setupAdapter() {
        if (getContext()!=null) {
            midiMessagesAdapter = new MidiMessagesAdapter(getContext());
            midiMessagesAdapter.setFromSongMessages(false);
            ItemTouchHelper.Callback callback = new MidiItemTouchHelper(getContext(),midiMessagesAdapter,true);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            midiMessagesAdapter.setTouchHelper(itemTouchHelper);
            llm = new LinearLayoutManager(getContext());
            llm.setOrientation(RecyclerView.VERTICAL);
            myView.recyclerView.post(() -> {
                myView.recyclerView.setLayoutManager(llm);
                myView.recyclerView.setAdapter(midiMessagesAdapter);
                itemTouchHelper.attachToRecyclerView(myView.recyclerView);
            });
        }
    }

    private void buildList() {
        midiInfos = new ArrayList<>();
        if (midiCurrent==null) {
            midiCurrent = "";
        }
        String[] bits = midiCurrent.split("\n");
        for (String command : bits) {
            if (command != null && !command.isEmpty() && getActivity() != null) {
                // Get a human readable version of the midi code
                String hexCode = mainActivityInterface.getMidi().checkForShortHandMIDI(command);
                String readable = mainActivityInterface.getMidi().getReadableStringFromHex(hexCode);
                MidiInfo midiInfo = new MidiInfo();
                midiInfo.midiCommand = command;
                midiInfo.readableCommand = readable;
                midiInfos.add(midiInfo);
            }
        }

        myView.recyclerView.post(() -> {
            midiMessagesAdapter.updateMidiInfos(midiInfos);
            myView.recyclerView.setAdapter(midiMessagesAdapter);
            myView.recyclerView.setVisibility(View.VISIBLE);
            myView.recyclerView.invalidate();
        });
    }

    private void setupListeners() {
        myView.midiCommand.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                whichValues();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        myView.midiChannel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateMIDIString();
                try {
                    mainActivityInterface.getMidi().setMidiOutputChannel(Integer.parseInt(myView.midiChannel.getText().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        myView.midiValue1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateMIDIString();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        myView.midiValue2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateMIDIString();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        myView.addCommandButton.setOnClickListener(view -> {
            Log.d(TAG,"addCommandButton onClick");
            doSave();
        });
        myView.midiTest.setOnClickListener(view -> {
            updateMIDIString();
            String hexSequence = mainActivityInterface.getMidi().checkForShortHandMIDI(midiCode);
            Log.d(TAG,"hexSequence:"+hexSequence);
            mainActivityInterface.getMidi().sendMidiHexSequence(hexSequence);
        });
        myView.midiAdd.setOnClickListener(view -> {
            Log.d(TAG,"midiAdd onClick");
            updateMIDIString();
            MidiInfo midiInfo = new MidiInfo();
            Log.d(TAG,"midiCode:"+midiCode);
            boolean isVoiceLive = false;
            if (midiCode!=null && midiCode.contains(":VL")) {
                String tempChannel = myView.midiChannel.getText().toString();
                if (!tempChannel.replaceAll("\\D","").isEmpty()) {
                    int newChannel = Integer.parseInt(tempChannel.replaceAll("\\D",""));
                    mainActivityInterface.getShowToast().doItBottomSheet(string_voicelive_midi_channel+": " + newChannel,myView.getRoot());
                    mainActivityInterface.getVoiceLive().setVoiceLiveChannel(newChannel);
                    isVoiceLive = true;
                }
            }
            String hexCode = mainActivityInterface.getMidi().checkForShortHandMIDI(midiCode);
            Log.d(TAG,"hexCode:"+hexCode);
            String readable = mainActivityInterface.getMidi().getReadableStringFromHex(hexCode);
            midiInfo.midiCommand = midiCode;
            midiInfo.readableCommand = readable;
            midiInfos.add(midiInfo);
            midiMessagesAdapter.addToEnd(midiInfo);
            if (isVoiceLive) {
                // Refresh the list of items fixing the override MIDI channels
                midiCurrent = midiCurrent + "\n" + midiCode;
                buildList();
            }
        });
    }

    private void whichValues() {
        String commandDesc = myView.midiCommand.getText().toString();
        int index = midiCommandDescription.indexOf(commandDesc);
        usingSysEx = false;
        if (index>=0) {
            command = midiCommand.get(index);
            switch (command) {
                default:
                case "PC":
                case "BBT":
                case "BBTE":
                case "BBBPM":
                case "BBV":
                case "BBVH":
                case "NX":
                case "MSB":
                case "LSB":
                case "VLGR":
                case "VLGC":
                case "VLGM":
                case "VLGO":
                case "VLGA":
                case "VLGW":
                case "VLGB":
                case "VLGRV":
                case "VLGD":
                case "VLGHIT":
                case "VLVR":
                case "VLVH":
                case "VLVHK":
                case "VLVHS":
                case "VLVHVB":
                case "VLVHH":
                case "VLVV":
                case "VLVCH":
                case "VLVDB":
                case "VLVHT":
                case "VLVM":
                case "VLVT":
                case "VLVRV":
                case "VLVD":
                case "VLVHIT":
                case "VLS":
                case "VLP":
                    usingVal1 = true;
                    usingVal2 = false;
                    break;
                case "CC":
                case "NO":
                case "BBS":
                    usingVal1 = true;
                    usingVal2 = true;
                    break;
                case "BBTX":
                case "BBTN":
                case "BBTP":
                case "BBTEX":
                case "BBTEN":
                case "BBTEP":
                case "BBH":
                case "BBHX":
                case "BBD":
                case "BBDX":
                case "BBI":
                case "BBO":
                case "BBP":
                case "BBA":
                case "BBF":
                case "VLNX":
                    usingVal1 = false;
                    usingVal2 = false;
                    break;
                case "START":
                case "STOP":
                    usingVal1 = false;
                    usingVal2 = false;
                    usingSysEx = true;
            }

            // Set up the dropdowns
            String value1Hint = string_value;
            String value2Hint = string_value;
            if (usingVal1) {
                // Decide which values should be shown.  Options are 0-100, 0-127, 1-127, 1-128, 40-300
                switch (command) {
                    default:
                        addArrayListToDropdown(myView.midiValue1, midiValue0_127);
                        break;
                    case "NO":
                    case "NX":
                        addArrayListToDropdown(myView.midiValue1, midiNotes);
                        value1Hint = string_note;
                        value2Hint = string_velocity;
                        break;
                    case "CC":
                        addArrayListToDropdown(myView.midiValue1, midiValue0_127);
                        value1Hint = string_controller_change;
                        break;
                    case "BBV":
                    case "BBVH":
                        addArrayListToDropdown(myView.midiValue1, midiValue0_100);
                        value1Hint = string_volume;
                        break;
                    case "BBT":
                    case "BBTE":
                        addArrayListToDropdown(myView.midiValue1, midiValue1_128);
                        break;
                    case "BBBPM":
                        addArrayListToDropdown(myView.midiValue1, midiValue40_300);
                        value1Hint = string_tempo;
                        break;
                    case "BBS":
                        addArrayListToDropdown(myView.midiValue1, midiValue1_127);
                        value1Hint = string_folder;
                        value2Hint = string_song;
                        break;
                    case "VLGR":
                    case "VLGC":
                    case "VLGM":
                    case "VLGO":
                    case "VLGA":
                    case "VLGW":
                    case "VLGB":
                    case "VLGRV":
                    case "VLGD":
                    case "VLGHIT":
                    case "VLVR":
                    case "VLVH":
                    case "VLVHVB":
                    case "VLVHH":
                    case "VLVV":
                    case "VLVCH":
                    case "VLVDB":
                    case "VLVHT":
                    case "VLVM":
                    case "VLVT":
                    case "VLVRV":
                    case "VLVD":
                    case "VLVHIT":
                        addArrayListToDropdown(myView.midiValue1, midiValueOff_On);
                        value1Hint = on_string + "/" + off_string;
                        break;
                    case "VLVHK":
                        addArrayListToDropdown(myView.midiValue1, midiValueKeys);
                        value1Hint = key_string;
                        break;
                    case "VLVHS":
                        addArrayListToDropdown(myView.midiValue1, midiValueScales);
                        value1Hint = scale_string;
                        break;
                    case "VLS":
                        addArrayListToDropdown(myView.midiValue1, midiValue1_32);
                        value1Hint = step_string;
                        break;
                    case "VLP":
                        addArrayListToDropdown(myView.midiValue1, midiValue1_500);
                        value1Hint = preset_string;
                        break;
                }

            }
            if (usingVal2) {
                if (command.equals("BBS")) {
                    addArrayListToDropdown(myView.midiValue2, midiValue1_127);
                } else {
                    addArrayListToDropdown(myView.midiValue2, midiValue0_127);
                }
            }

            // Set the hint text
            myView.midiValue1.setHint(value1Hint);
            myView.midiValue2.setHint(value2Hint);

            myView.midiChannel.setVisibility(usingSysEx ? View.GONE : View.VISIBLE);
            myView.midiValue1.setVisibility(usingVal1 ? View.VISIBLE : View.GONE);
            myView.midiValue2.setVisibility(usingVal2 ? View.VISIBLE : View.GONE);

            updateMIDIString();
        }
    }

    private void addArrayListToDropdown(ExposedDropDown exposedDropDown, ArrayList<String> arrayList) {
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter adapter = new ExposedDropDownArrayAdapter(getContext(), exposedDropDown, R.layout.view_exposed_dropdown_item, arrayList);
            exposedDropDown.setAdapter(adapter);
            exposedDropDown.setText(arrayList.get(0));
        }
    }
    private void updateMIDIString() {
        // Build the MIDI code using the dropdowns if we are ready
        midiCode = "";
        if (midiCommandDescription!=null && !midiCommandDescription.isEmpty()) {
            if (usingSysEx) {
                midiCode = "MIDI:";
            } else {
                midiCode = "MIDI" + myView.midiChannel.getText().toString() + ":";
            }
            String midiCommandDesc = myView.midiCommand.getText().toString();
            int index = midiCommandDescription.indexOf(midiCommandDesc);
            String midiCommandShorthand = "";
            if (midiCommand!=null && midiCommand.size()>index && index>=0) {
                midiCommandShorthand = midiCommand.get(index);
            }
            midiCode += midiCommandShorthand;
            // Get the next value if required
            if (usingVal1) {
                String val1 = myView.midiValue1.getText().toString();
                if (command.equals("NO") || command.equals("NX")) {
                    // Convert the notes to the position in the array
                    int pos = midiNotes.indexOf(val1);
                    val1 = String.valueOf(pos);
                }
                if (val1.equals(off_string)) {
                    val1 = "X";
                } else if (val1.equals(on_string)) {
                    val1 = "";
                }
                midiCode += val1;
            }
            if (usingVal2) {
                if (midiCommandShorthand.equals("BBS")) {
                    midiCode += "/" + myView.midiValue2.getText().toString();
                } else {
                    midiCode += ":" + myView.midiValue2.getText().toString();
                }
            }
        }
        myView.midiShortHand.setHint(midiCode);
    }

    private void doSave() {
        // Compile all lines of MIDI from the arraylist
        StringBuilder fullMidiCode = new StringBuilder();
        midiInfos = midiMessagesAdapter.getMidiInfos();
        for (MidiInfo midiInfo:midiInfos) {
            fullMidiCode.append(midiInfo.midiCommand).append("\n");
        }

        String title = "";
        if (myView.midiName.getText()!=null) {
            title = myView.midiName.getText().toString();
        }

        if (fragTag.equals("MidiBoardBottomSheet") && bottomSheetDialogFragment!=null) {
            ((MidiBoardBottomSheet)bottomSheetDialogFragment).addCommand(title,fullMidiCode.toString().trim());
            dismiss();
        } else if (fragTag.equals("MidiSongBottomSheet") && bottomSheetDialogFragment!=null) {
            ((MidiSongBottomSheet)bottomSheetDialogFragment).updateSongMessages(fullMidiCode.toString().trim());
            dismiss();
        } else if (fragTag.equals("MidiActionBottomSheet") && bottomSheetDialogFragment!=null) {
            ((MidiActionBottomSheet)bottomSheetDialogFragment).updateAction(fullMidiCode.toString().trim());
            dismiss();
        }
    }
}

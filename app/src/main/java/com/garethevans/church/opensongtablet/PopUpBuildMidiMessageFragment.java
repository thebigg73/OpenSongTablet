package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class PopUpBuildMidiMessageFragment extends DialogFragment {

    private Midi m;
    private ArrayAdapter<String> midiCommandsAdapter, midiNotesAdapter;
    private ArrayAdapter<Integer> midiChannelsAdapter, midiValuesAdapter;
    private Spinner midiCommandsSpinner;
    private Spinner midiValuesSpinner;
    private Spinner midiValue2Spinner;
    private SeekBar midiDelay;
    private TextView valueOrVelocity, noteOrValue, midiMessage, midiDelay_Text;
    private LinearLayout midiActionList;
    private String action = "PC";
    private int channel = 1;
    private int byte2 = 0;
    private int byte3 = 0;

    private Preferences preferences;
    private StorageAccess storageAccess;

    static PopUpBuildMidiMessageFragment newInstance() {
        PopUpBuildMidiMessageFragment frag;
        frag = new PopUpBuildMidiMessageFragment();
        return frag;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }
        preferences = new Preferences();
        storageAccess = new StorageAccess();

        View V = inflater.inflate(R.layout.popup_buildmidicommand, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.midi_commands));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getActivity());
            closeMe.setEnabled(false);
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(saveMe, getActivity());
            saveMe.setEnabled(false);
            doSave();
        });

        // Initialise the midi
        m = new Midi();

        // Initialise the basic views
        midiCommandsSpinner = V.findViewById(R.id.messageType);
        Spinner midiChannelsSpinner = V.findViewById(R.id.myMidiChannel);
        valueOrVelocity = V.findViewById(R.id.valueorvelocity);
        midiValuesSpinner = V.findViewById(R.id.myMidiValue1);
        midiValue2Spinner = V.findViewById(R.id.myMidiValue2);
        noteOrValue = V.findViewById(R.id.noteorvalue);
        midiMessage = V.findViewById(R.id.midiMessage);
        midiActionList = V.findViewById(R.id.midiActionList);
        midiDelay = V.findViewById(R.id.midiDelay);
        midiDelay_Text = V.findViewById(R.id.midiDelay_Text);
        Button testMidiMessage = V.findViewById(R.id.midiTest);
        Button addMidiMessage = V.findViewById(R.id.midiAdd);

        testMidiMessage.setOnClickListener(view -> testTheMidiMessage(midiMessage.getText().toString()));

        addMidiMessage.setOnClickListener(view -> addMidiToList());

        // Initialise the midi messages in the song
        initialiseCurrentMessages();

        // Build the spinner's array adapters

        // By default, we will assume ProgramChange being sent
        setUpMidiCommands();
        midiCommandsSpinner.setAdapter(midiCommandsAdapter);
        midiCommandsSpinner.setSelection(2);
        midiCommandsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                showCorrectValues();
                action = m.getMidiCommand(i); // Get the midicommand (NoteOn, NoteOff, PC, CC, MSB, LSB)
                getHexCodeFromSpinners();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                adapterView.setSelection(2);
                action = "PC";
                getHexCodeFromSpinners();
            }
        });

        setUpMidiChannels();
        midiChannelsSpinner.setAdapter(midiChannelsAdapter);
        midiChannelsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                channel = i+1;
                getHexCodeFromSpinners();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                channel = 1;
                getHexCodeFromSpinners();
            }
        });

        setUpMidiValues();
        setUpMidiNotes();
        setUpMidiDelay();
        showCorrectValues();

        midiValuesSpinner.setAdapter(midiValuesAdapter);
        midiValuesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                byte2 = i;
                getHexCodeFromSpinners();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                byte2 = 0;
                getHexCodeFromSpinners();
            }
        });

        midiValue2Spinner.setAdapter(midiValuesAdapter);
        midiValue2Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                byte3 = i;
                getHexCodeFromSpinners();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                byte3 = 0;
                getHexCodeFromSpinners();
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void doSave() {
        try {
            // Get a string representation of the midi commands
            StringBuilder s = new StringBuilder();
            for (int x=0; x<midiActionList.getChildCount(); x++) {
                if (midiActionList.getChildAt(x).getTag()!=null) {
                    String tag = midiActionList.getChildAt(x).getTag().toString().trim();
                    if (!tag.isEmpty()) {
                        s.append(tag).append("\n");
                    }
                }
            }
            // Get rid of extra linebreak
            s = new StringBuilder(s.toString().trim());

            Log.d("d","s="+s);
            StaticVariables.mMidi = s.toString();
            PopUpEditSongFragment.prepareSongXML();

            if (FullscreenActivity.isPDF || FullscreenActivity.isImage) {
                NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(getActivity());
                NonOpenSongSQLite nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(getActivity(),storageAccess,preferences,nonOpenSongSQLiteHelper.getSongId());
                nonOpenSongSQLiteHelper.updateSong(getActivity(),storageAccess,preferences,nonOpenSongSQLite);
            } else {
                PopUpEditSongFragment.justSaveSongXML(getActivity(), preferences);
            }

            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpMidiCommands() {
        ArrayList<String> midiCommands = new ArrayList<>();
        midiCommands.add(getString(R.string.note) + " " + getString(R.string.on));
        midiCommands.add(getString(R.string.note) + " " + getString(R.string.off));
        midiCommands.add(getString(R.string.midi_program));
        midiCommands.add(getString(R.string.midi_controller));
        midiCommands.add("MSB");
        midiCommands.add("LSB");
        midiCommandsAdapter = new ArrayAdapter<>(requireContext(), R.layout.my_spinner, midiCommands);
    }

    private void setUpMidiChannels() {
        // Remember that midi channel 1-16 are actually 0-15 in code
        ArrayList<Integer> midiChannels = new ArrayList<>();
        int i = 1;
        while (i<=16) {
            midiChannels.add(i);
            i++;
        }
        midiChannelsAdapter = new ArrayAdapter<>(requireContext(), R.layout.my_spinner, midiChannels);
    }

    private void setUpMidiValues() {
        // Returns values 0-127
        ArrayList<Integer> midiValues = new ArrayList<>();
        int i = 0;
        while (i<=127) {
            midiValues.add(i);
            i++;
        }
        midiValuesAdapter = new ArrayAdapter<>(requireContext(),R.layout.my_spinner, midiValues);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setUpMidiNotes() {
        // Return an array adapter with music note representation of values 0-127
        ArrayList<String> midiNotes = new ArrayList<>();
        int i = 0;
        while (i<=127) {
            midiNotes.add(m.getNoteFromInt(i));
            i++;
        }
        midiNotesAdapter = new ArrayAdapter<>(requireContext(),R.layout.my_spinner, midiNotes);
    }

    private void setUpMidiDelay() {
        int myDelay = preferences.getMyPreferenceInt(getContext(),"midiDelay",100);
        int pos = myDelay/100;
        // The seekbar goes from 0 to 5, with each value being 100 (so 0 to 500);
        midiDelay.setMax(5);
        midiDelay.setProgress(pos);
        String text = myDelay + "ms";
        midiDelay_Text.setText(text);
        midiDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // Update the text
                String text = (i*100) + "ms";
                midiDelay_Text.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Save the preference
                int pos = seekBar.getProgress();
                preferences.setMyPreferenceInt(getContext(),"midiDelay",pos*100);
            }
        });
    }
    private void showCorrectValues() {
        String valorvel = getString(R.string.midi_value);
        String noteorval = getString(R.string.midi_value);

        // By default show the middle and last byte
        midiValue2Spinner.setVisibility(View.VISIBLE);
        valueOrVelocity.setVisibility(View.VISIBLE);
        midiValuesSpinner.setVisibility(View.VISIBLE);
        noteOrValue.setVisibility(View.VISIBLE);

        // By default set the values spinner to 0-127 rather than music notes
        midiValuesSpinner.setAdapter(midiValuesAdapter);

        // Now change the headings and what is visible depending on what command we are using
        if (midiCommandsSpinner.getSelectedItemPosition()<2) {
            // This is a note on or off command, so change the text to velocity and use the note heading
            valorvel = getString(R.string.midi_velocity);
            noteorval = getString(R.string.midi_note);
            // Set the values spinner to midi notes
            midiValuesSpinner.setAdapter(midiNotesAdapter);
            if (midiCommandsSpinner.getSelectedItemPosition()==1) {
                // Since this is a note off command, the final byte will always be 0, so hide it
                midiValue2Spinner.setVisibility(View.GONE);
                valueOrVelocity.setVisibility(View.GONE);
            }

        } else if (midiCommandsSpinner.getSelectedItemPosition()==2) {
            // This is a program change, so we can hide the last spinner (not required)
            midiValue2Spinner.setVisibility(View.GONE);
            valueOrVelocity.setVisibility(View.GONE);

        } else if (midiCommandsSpinner.getSelectedItemPosition()>3) {
            // This is MSB or LSB.  We can hide the value 1 stuff as these are set automatically
            midiValuesSpinner.setVisibility(View.GONE);
            noteOrValue.setVisibility(View.GONE);
        }

        valueOrVelocity.setText(valorvel);
        noteOrValue.setText(noteorval);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getHexCodeFromSpinners() {
        String s;
        try {
            s = m.buildMidiString(action, channel, byte2, byte3);
        } catch (Exception e) {
            s = "0x00 0x00 0x00";
        }
        midiMessage.setText(s);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initialiseCurrentMessages() {
        midiActionList.removeAllViews();
        // Add what is there already
        String[] bits = StaticVariables.mMidi.trim().split("\n");
        for (String midiMessage : bits) {
            if (midiMessage != null && !midiMessage.equals("") && !midiMessage.isEmpty() && getActivity() != null) {
                // Get a human readable version of the midi code
                String hr = m.getReadableStringFromHex(midiMessage, getActivity());
                String message = hr + "\n" + "(" + midiMessage + ")";
                midiActionList.addView(getNewTextView(message, midiMessage));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private TextView getNewTextView(String text, String midiMessage) {
        TextView textView = (TextView)getLayoutInflater().inflate(R.layout.my_spinner,null);
        textView.setId(View.generateViewId());
        textView.setText(text);
        textView.setTag(midiMessage);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = 8;
        textView.setLayoutParams(layoutParams);
        textView.setOnClickListener(view -> {
            String tag = view.getTag().toString();
            sendMidiFromList(tag);
        });
        textView.setOnLongClickListener(view -> {
            for (int x=0; x<midiActionList.getChildCount(); x++) {
                if (midiActionList.getChildAt(x)==view) {
                    midiActionList.removeView(view);
                }
            }
            return true;
        });
        return textView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addMidiToList() {
        if (getActivity()!=null) {
            try {
                String s = midiMessage.getText().toString();
                String hr = m.getReadableStringFromHex(s, getActivity());
                String message = hr + "\n" + "(" + s + ")";
                // Get the current number of messages
                midiActionList.addView(getNewTextView(message,s));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendMidiFromList(String midiMessage) {
        Log.d("MidiFragment","message:"+midiMessage);
        //String s = songMidiMessagesToSave.get(i);
        testTheMidiMessage(midiMessage);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void testTheMidiMessage(String mm) {
        // Test the midi message being sent
        // First split by spaces
        boolean success = false;
        try {
            byte[] b = m.returnBytesFromHexText(mm);
            success = m.sendMidi(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!success) {
            StaticVariables.myToastMessage = getString(R.string.midi_error);
            ShowToast.showToast(getActivity());
        }
    }
}
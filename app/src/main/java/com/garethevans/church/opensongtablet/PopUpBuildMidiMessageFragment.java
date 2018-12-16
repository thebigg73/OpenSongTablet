package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class PopUpBuildMidiMessageFragment extends DialogFragment {

    Midi m;
    ArrayList<String> midiCommands, midiNotes, songMidiMessages, songMidiMessagesToSave;
    ArrayList<Integer> midiChannels, midiValues;
    ArrayAdapter<String> midiCommandsAdapter, midiNotesAdapter, midiMessagesAdapter;
    ArrayAdapter<Integer> midiChannelsAdapter, midiValuesAdapter;
    Spinner midiCommandsSpinner, midiChannelsSpinner, midiValuesSpinner, midiValue2Spinner;
    TextView valueOrVelocity, noteOrValue, midiMessage;
    ListView midiActionList;
    Button addMidiMessage, testMidiMessage;
    String action = "PC";
    int channel = 1;
    int byte2 = 0;
    int byte3 = 0;

    static PopUpBuildMidiMessageFragment newInstance() {
        PopUpBuildMidiMessageFragment frag;
        frag = new PopUpBuildMidiMessageFragment();
        return frag;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        View V = inflater.inflate(R.layout.popup_buildmidicommand, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.midi_commands));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, getActivity());
                closeMe.setEnabled(false);
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe, getActivity());
                saveMe.setEnabled(false);
                doSave();
            }
        });

        // Initialise the midi
        m = new Midi();

        // Initialise the basic views
        midiCommandsSpinner = V.findViewById(R.id.messageType);
        midiChannelsSpinner = V.findViewById(R.id.myMidiChannel);
        valueOrVelocity = V.findViewById(R.id.valueorvelocity);
        midiValuesSpinner = V.findViewById(R.id.myMidiValue1);
        midiValue2Spinner = V.findViewById(R.id.myMidiValue2);
        noteOrValue = V.findViewById(R.id.noteorvalue);
        midiMessage = V.findViewById(R.id.midiMessage);
        midiActionList = V.findViewById(R.id.midiActionList);
        testMidiMessage = V.findViewById(R.id.midiTest);
        addMidiMessage = V.findViewById(R.id.midiAdd);

        testMidiMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testTheMidiMessage(midiMessage.getText().toString());
            }
        });

        addMidiMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMidiToList();
            }
        });

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

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    private void doSave() {
        try {
            // Get a string representation of the midi commands
            StringBuilder s = new StringBuilder();
            for (String c:songMidiMessagesToSave) {
                c = c.trim();
                if (!c.isEmpty()) {
                    s.append(c).append("\n");
                }
            }
            s = new StringBuilder(s.toString().trim()); // Get rid of extra line breaks
            Log.d("d","s="+s);
            FullscreenActivity.mMidi = s.toString();
            PopUpEditSongFragment.prepareSongXML();
            PopUpEditSongFragment.justSaveSongXML(getActivity());
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpMidiCommands() {
        midiCommands = new ArrayList<>();
        midiCommands.add(getString(R.string.note) + " " + getString(R.string.on));
        midiCommands.add(getString(R.string.note) + " " + getString(R.string.off));
        midiCommands.add(getString(R.string.midi_program));
        midiCommands.add(getString(R.string.midi_controller));
        midiCommands.add("MSB");
        midiCommands.add("LSB");
        midiCommandsAdapter = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, midiCommands);
    }

    private void setUpMidiChannels() {
        // Remember that midi channel 1-16 are actually 0-15 in code
        midiChannels = new ArrayList<>();
        int i = 1;
        while (i<=16) {
            midiChannels.add(i);
            i++;
        }
        midiChannelsAdapter = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, midiChannels);
    }

    public void setUpMidiValues() {
        // Returns values 0-127
        midiValues = new ArrayList<>();
        int i = 0;
        while (i<=127) {
            midiValues.add(i);
            i++;
        }
        midiValuesAdapter = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,midiValues);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setUpMidiNotes() {
        // Return an array adapter with music note representation of values 0-127
        midiNotes = new ArrayList<>();
        int i = 0;
        while (i<=127) {
            midiNotes.add(m.getNoteFromInt(i));
            i++;
        }
        midiNotesAdapter = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,midiNotes);
    }

    public void showCorrectValues() {
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
    void getHexCodeFromSpinners() {
        String s;
        try {
            s = m.buildMidiString(action, channel, byte2, byte3);
        } catch (Exception e) {
            s = "0x00 0x00 0x00";
        }
        midiMessage.setText(s);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void initialiseCurrentMessages() {
        songMidiMessages = new ArrayList<>();
        songMidiMessagesToSave = new ArrayList<>();
        // Add what is there already
        String bits[] = FullscreenActivity.mMidi.trim().split("\n");
        for (String s : bits) {
            if (s!=null && !s.equals("") && !s.isEmpty()) {
                // Get a human readable version of the midi code
                String hr = m.getReadableStringFromHex(s,getActivity());
                String message = hr + "\n" + "(" + s + ")";
                songMidiMessages.add(message);
                songMidiMessagesToSave.add(s);
            }
        }
        midiMessagesAdapter = new ArrayAdapter<>(getActivity(),R.layout.my_spinner,songMidiMessages);
        midiMessagesAdapter.notifyDataSetChanged();
        midiActionList.setAdapter(midiMessagesAdapter);
        midiActionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sendMidiFromList(i);
            }
        });
        midiActionList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                deleteMidiFromList(i);
                return true;
            }
        });
    }

    void updateCurrentMessages() {
        midiMessagesAdapter.notifyDataSetChanged();
        midiActionList.setAdapter(midiMessagesAdapter);
        midiActionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sendMidiFromList(i);
            }
        });
        midiActionList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                deleteMidiFromList(i);
                return true;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void addMidiToList() {
        try {
            String s = midiMessage.getText().toString();
            String hr = m.getReadableStringFromHex(s, getActivity());
            String message = hr + "\n" + "(" + s + ")";
            songMidiMessagesToSave.add(s);
            songMidiMessages.add(message);
            updateCurrentMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void deleteMidiFromList(int i) {
        try {
            songMidiMessages.remove(i);
            songMidiMessagesToSave.remove(i);
            updateCurrentMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void sendMidiFromList(int i) {
        String s = songMidiMessagesToSave.get(i);
        testTheMidiMessage(s);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void testTheMidiMessage(String mm) {
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
            FullscreenActivity.myToastMessage = getString(R.string.midi_error);
            ShowToast.showToast(getActivity());
        }
    }
}
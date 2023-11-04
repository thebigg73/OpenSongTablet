package com.garethevans.church.opensongtablet.midi;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.midi.MidiDevice;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Midi {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private PedalMidiReceiver pedalMidiReceiver;
    private final ShortHandMidi shortHandMidi;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "Midi";
    private MediaPlayer midiMediaPlayer;
    private String[] messageParts;
    private final String sysexStartCode = "0xF0 0xFA 0xF7";
    private final String sysexStopCode = "0xF0 0xFB 0xF7";

    // Initialise
    public Midi(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        midiDelay = mainActivityInterface.getPreferences().getMyPreferenceInt("midiDelay", 100);
        midiAction1 = mainActivityInterface.getPreferences().getMyPreferenceString("midiAction1", "MIDI10:NO36:100");
        midiAction2 = mainActivityInterface.getPreferences().getMyPreferenceString("midiAction2", "MIDI10:NO38:100");
        midiAction3 = mainActivityInterface.getPreferences().getMyPreferenceString("midiAction3", "MIDI10:NO42:100");
        midiAction4 = mainActivityInterface.getPreferences().getMyPreferenceString("midiAction4", "MIDI10:NO46:100");
        midiAction5 = mainActivityInterface.getPreferences().getMyPreferenceString("midiAction5", "MIDI10:NO48:100");
        midiAction6 = mainActivityInterface.getPreferences().getMyPreferenceString("midiAction6", "MIDI10:NO47:100");
        midiAction7 = mainActivityInterface.getPreferences().getMyPreferenceString("midiAction7", "MIDI10:NO43:100");
        midiAction8 = mainActivityInterface.getPreferences().getMyPreferenceString("midiAction8", "MIDI10:NO55:100");
        midiSendAuto = mainActivityInterface.getPreferences().getMyPreferenceBoolean("midiSendAuto",true);
        shortHandMidi = new ShortHandMidi(c);
    }

    public String getMidiAction(int which) {
        switch (which) {
            case 1:
            default:
                return midiAction1;
            case 2:
                return midiAction2;
            case 3:
                return midiAction3;
            case 4:
                return midiAction4;
            case 5:
                return midiAction5;
            case 6:
                return midiAction6;
            case 7:
                return midiAction7;
            case 8:
                return midiAction8;
        }
    }

    public void setMidiAction(int which, String what) {
        String pref = "midiAction"+which;
        mainActivityInterface.getPreferences().setMyPreferenceString(pref,what);
        switch (which) {
            case 1:
                midiAction1 = what;
                break;
            case 2:
                midiAction2 = what;
                break;
            case 3:
                midiAction3 = what;
                break;
            case 4:
                midiAction4 = what;
                break;
            case 5:
                midiAction5 = what;
                break;
            case 6:
                midiAction6 = what;
                break;
            case 7:
                midiAction7 = what;
                break;
            case 8:
                midiAction8 = what;
                break;
        }
    }

    private ArrayList<String> songMidiMessages = new ArrayList<>();
    private MidiDevice midiDevice;
    private MidiManager midiManager;
    private MidiInputPort midiInputPort;
    private MidiOutputPort midiOutputPort;
    private String midiDeviceName = "", midiDeviceAddress = "";
    private int midiInstrument;
    private String instrumentLetter;
    private boolean usePianoNotes, midiSendAuto;
    private ArrayList<String> midiNotesOnArray, midiNotesOffArray;
    private final String allOff = "7F B0 7B 00 ";
    private long noteOnDelta, noteOffDelta;

    private final String midiFileHeader = "4D 54 68 64 00 00 00 06 00 01 00 01 00 80 ";
    //                                                                            80 = 128 ticks (hex)
    private final String midiFileTrackHeader = "4D 54 72 6B 00 00 00 "; // Need to add count of note data + track out of 4!
    private final String midiFileTrackOut = "00 FF 2F 00";

    private int midiDelay;
    private boolean includeBluetoothMidi;
    public final List<String> notes = Arrays.asList("C0", "C#0", "D0", "D#0", "E0", "F0", "F#0", "G0", "G#0", "A0", "A#0", "B0",
            "C1", "C#1", "D1", "D#1", "E1", "F1", "F#1", "G1", "G#1", "A1", "A#1", "B1",
            "C2", "C#2", "D2", "D#2", "E2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2",
            "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3",
            "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4",
            "C5", "C#5", "D5", "D#5", "E5", "F5", "F#5", "G5", "G#5", "A5", "A#5", "B5",
            "C6", "C#6", "D6", "D#6", "E6", "F6", "F#6", "G6", "G#6", "A6", "A#6", "B6",
            "C7", "C#7", "D7", "D#7", "E7", "F7", "F#7", "G7", "G#7", "A7", "A#7", "B7",
            "C8", "C#8", "D8", "D#8", "E8", "F8", "F#8", "G8", "G#8", "A8", "A#8", "B8",
            "C9", "C#9", "D9", "D#9", "E9", "F9", "F#9", "G9", "G#9", "A9", "A#9", "B9",
            "C10", "C#10", "D10", "D#10", "E10", "F10", "F#10", "G10");
    private final List<String> notesSimple = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B",
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B",
            "C", "C#", "D", "D#", "E");
    private final List<String> midiCommands = Arrays.asList("NoteOn", "NoteOff", "PC", "CC", "MSB", "LSB");

    private String midiAction1, midiAction2, midiAction3, midiAction4, midiAction5, midiAction6, midiAction7, midiAction8;

    private final List<String> guitarStringStartNotes = Arrays.asList("E3", "A3", "D4", "G4", "B4", "E5");
    private final List<String> ukuleleStringStartNotes = Arrays.asList("G5", "C5", "E5", "A5");
    private final List<String> banjo4StringStartNotes = Arrays.asList("D4", "G4", "B4", "D5");
    private final List<String> banjo5StringStartNotes = Arrays.asList("G5", "D4", "G4", "B4", "D5");
    private final List<String> pianoNotesStartNotes = Collections.singletonList("C4");
    private final List<String> mandolinStringStartNotes = Arrays.asList("G4", "D5", "A5", "E6");
    private final List<String> cavaquinhoStringStartNotes = Arrays.asList("D4", "G4", "B4", "D5");

    public void setUsePianoNotes(boolean usePianoNotes) {
        this.usePianoNotes = usePianoNotes;
    }

    public MidiDevice getMidiDevice() {
        return midiDevice;
    }

    public MidiManager getMidiManager() {
        return midiManager;
    }

    @SuppressWarnings("unused")
    public MidiInputPort getMidiInputPort() {
        return midiInputPort;
    }

    public MidiOutputPort getMidiOutputPort() {
        return midiOutputPort;
    }

    public String getMidiDeviceName() {
        return midiDeviceName;
    }

    public String getMidiDeviceAddress() {
        return midiDeviceAddress;
    }

    public boolean getIncludeBluetoothMidi() {
        return includeBluetoothMidi;
    }

    public int getMidiDelay() {
        return midiDelay;
    }

    public boolean getMidiSendAuto() {
        return midiSendAuto;
    }
    public void setMidiSendAuto(boolean midiSendAuto) {
        this.midiSendAuto = midiSendAuto;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("midiSendAuto",midiSendAuto);
    }

    public void setMidiDevice(MidiDevice midiDevice) {
        this.midiDevice = midiDevice;
    }

    public void setMidiManager(MidiManager midiManager) {
        this.midiManager = midiManager;
    }

    public void setMidiInputPort(MidiInputPort midiInputPort) {
        this.midiInputPort = midiInputPort;
    }

    public void setMidiOutputPort(MidiOutputPort midiOutputPort) {
        this.midiOutputPort = midiOutputPort;
    }

    public void setMidiDeviceName(String midiDeviceName) {
        this.midiDeviceName = midiDeviceName;
    }

    public void setMidiDeviceAddress(String midiDeviceAddress) {
        this.midiDeviceAddress = midiDeviceAddress;
    }

    public void setIncludeBluetoothMidi(boolean includeBluetoothMidi) {
        this.includeBluetoothMidi = includeBluetoothMidi;
    }

    public void setMidiDelay(int midiDelay) {
        this.midiDelay = midiDelay;
        mainActivityInterface.getPreferences().setMyPreferenceInt("midiDelay", midiDelay);
    }

    String getMidiCommand(int i) {
        try {
            return midiCommands.get(i);
        } catch (Exception e) {
            return "PC";
        }
    }

    String getNoteFromInt(int i) {
        if (i<notes.size()) {
            return notes.get(i);
        } else {
            return "";
        }
    }

    public String getReadableStringFromHex(String s) {
        // Check for shorthand MIDI
        s = checkForShortHandMIDI(s).trim();
        // This tries to get a readable version of a midi hex line
        // e.g. try to convert 0x90 0x02 0x64 into "Channel 1 Note on Note D0 Velocity 100
        // First then, we need to split the string into sections.
        String message;
        String channel = c.getString(R.string.midi_channel);
        String action = "";
        String noteon = c.getString(R.string.midi_note) + " " + c.getString(R.string.on);
        String noteoff = c.getString(R.string.midi_note) + " " + c.getString(R.string.off);
        String progchange = c.getString(R.string.midi_program);
        String contchange = c.getString(R.string.midi_controller);
        String velocity = c.getString(R.string.midi_velocity);
        String value = c.getString(R.string.midi_value);
        String sysexstart = c.getString(R.string.midi_sysex) + " " + c.getString(R.string.start);
        String sysexstop = c.getString(R.string.midi_sysex) + " " + c.getString(R.string.stop);
        String msb = "MSB";
        String lsb = "LSB";
        messageParts = new String[4];

        if (s.equals(sysexStartCode)) {
            message = sysexstart;
        } else if (s.equals(sysexStopCode)) {
            message = sysexstop;
        } else {
            String[] sections = s.trim().split(" ");
            messageParts[0] = "";
            messageParts[1] = "";
            if (sections.length >= 1 && sections[0] != null && !sections[0].isEmpty()) {
                String s0_0;
                String s0_1;
                try {
                    s0_0 = sections[0].replace("0x", "").substring(0, 1);
                    s0_1 = sections[0].replace("0x", "").substring(1);

                    // The channel is the second digit (in hex) of the first byte
                    messageParts[0] = "" + (getIntFromHexString(s0_1) + 1);
                    messageParts[1] = s0_0;
                    channel = channel + " " + messageParts[0];
                    switch (s0_0) {
                        case "9":
                            action = noteon;
                            break;
                        case "8":
                            action = noteoff;
                            break;
                        case "C":
                            action = progchange;
                            break;
                        case "B":
                            action = contchange;
                            break;
                        default:
                            action = "?";
                            break;
                    }

                } catch (Exception e) {
                    action = "?";
                    messageParts[0] = "";
                    messageParts[1] = "";
                }
            }

            // Now deal with the middle byte (note or program number)
            messageParts[2] = "";
            if (sections.length >= 2 && sections[1] != null && !sections[1].isEmpty()) {
                try {
                    String s1 = sections[1].replace("0x", "").trim();
                    int v1 = getIntFromHexString(s1);
                    if (action.equals(contchange) && v1 == 32) {
                        // This is a LSB message
                        action = lsb;
                        messageParts[2] = "LSB";
                    } else if (action.equals(contchange) && v1 == 0) {
                        // This is a MSB message
                        action = msb;
                        messageParts[2] = "MSB";
                    } else if (action.equals(noteon) || action.equals(noteoff)) {
                        action = action + " " + notes.get(v1);
                        messageParts[2] = notes.get(v1);
                    } else {
                        action = action + " " + v1;
                        messageParts[2] = "" + v1;
                    }
                } catch (Exception e) {
                    action = "?";
                }
            }
            // Now deal with the last byte (velocity or value) - not present for program change
            messageParts[3] = "";
            if (sections.length >= 3 && sections[2] != null && !sections[2].isEmpty()) {
                try {
                    String s2 = sections[2].replace("0x", "").trim();
                    int v2 = getIntFromHexString(s2);
                    messageParts[3] = "" + v2;
                    if (action.startsWith(noteon) || action.startsWith(noteoff)) {
                        action = action + "\n" + velocity + " " + v2;
                    } else {
                        action = action + "\n" + value + " " + v2;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            channel = channel.trim();
            action = action.replace("\n\n", "\n");
            message = channel + "\n" + action;
        }

        return message;
    }

    public String[] getMessageParts() {
        return messageParts;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean sendMidi(byte[] b) {
        boolean success = false;
        if (midiInputPort != null) {
            try {
                midiInputPort.send(b, 0, b.length);
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendMidi(int position) {
        // Send midi from the arrayList
        if (position >= 0 && position < songMidiMessages.size()) {
            sendMidi(returnBytesFromHexText(songMidiMessages.get(position)));
        }
    }

    public void sendSongMessages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                songMidiMessages!=null && songMidiMessages.size()>0) {
            for (int position = 0; position < songMidiMessages.size(); position++) {
                int finalPosition = position;
                new Handler().postDelayed(() -> {
                    if (songMidiMessages.get(finalPosition)!=null &&
                            !songMidiMessages.get(finalPosition).isEmpty()) {
                        sendMidi(finalPosition);
                    }
                }, (long) midiDelay *position);
            }
        }
    }
    public int sendMidiHexSequence(String sequence) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                sequence!=null && !sequence.isEmpty()) {
            String[] messages = sequence.split("\n");
            for (int x=0; x<messages.length; x++) {
                int finalX = x;
                new Handler().postDelayed(() -> {
                    sendMidi(returnBytesFromHexText(messages[finalX]));
                }, (long) midiDelay * x);
            }
            return midiDelay * messages.length;
        } else {
            return 0;
        }
    }

    public void playMidiNotes(String chordCode, String tuning, long timeBetweenNotes, int turnOffNoteTime) {
        // Tuning notes can be set as chords: 0xxxxx for a guitar 6th string

        noteOnDelta = timeBetweenNotes;
        noteOffDelta = timeBetweenNotes;

        // The chord code is the first section before _
        midiNotesOnArray = new ArrayList<>();
        midiNotesOffArray = new ArrayList<>();

        String[] chordBits = chordCode.split("_");
        String[] chordNotes;

        // Try to get a capo or transpose fret number
        int addFret = 0;
        if (chordBits.length>1) {
            String text = chordBits[1].replaceAll("\\D", "");
            if (!text.isEmpty()) {
                addFret = Integer.parseInt(text);
            }
        }
        if (midiInstrument==0 || usePianoNotes) {
            if (usePianoNotes) {
                // Add the instrument program change
                midiNotesOnArray.add(buildMidiString("PC", 0, 0, 0));
                midiNotesOffArray.add(buildMidiString("PC", 0, 0, 0));
                usePianoNotes = false;
            }
            // Piano notes are different
            chordNotes = chordBits[0].split(",");
            int transpose = 48;
            int startPos = 0;
            for (String chordNote : chordNotes) {
                // Now go through the notes array
                // If the note has no number in it, use the simple array
                if (chordNote.replaceAll("\\D", "").isEmpty()) {
                    for (int x = startPos; x < notesSimple.size(); x++) {
                        if (notesSimple.get(x).equals(chordNote)) {
                            startPos = x + 1;
                            midiNotesOnArray.add(buildMidiString("NoteOn", 0, x + transpose, 100));
                            midiNotesOffArray.add(buildMidiString("NoteOff", 0, x + transpose, 0));
                            break;
                        }
                    }
                } else {
                    midiNotesOnArray.add(buildMidiString("NoteOn", 0, notes.indexOf(chordNote), 100));
                    midiNotesOffArray.add(buildMidiString("NoteOff", 0, notes.indexOf(chordNote), 0));
                }
            }
        } else {
            chordNotes = chordBits[0].split("");
            // Add the instrument program change
            midiNotesOnArray.add(buildMidiString("PC", 0, midiInstrument, midiInstrument));
            midiNotesOffArray.add(buildMidiString("PC", 0, midiInstrument, midiInstrument));

            // Now convert these fret numbers to actual notes and add to the array
            List<String> startNotes = getStartNotes(tuning);
            for (int i = 0; i < chordNotes.length; i++) {
                if (chordNotes[i]!=null && !chordNotes[i].isEmpty() && !chordNotes[i].equals("x") &&
                startNotes!=null && startNotes.size()>i) {
                    int fretNum = Integer.parseInt(chordNotes[i]);
                    String openStringNote = startNotes.get(i);

                    // Now go through the notes array the fretNum times
                    int posInNotesArray = notes.indexOf(openStringNote);
                    midiNotesOnArray.add(buildMidiString("NoteOn", 0, posInNotesArray + fretNum + addFret, 100));
                    midiNotesOffArray.add(buildMidiString("NoteOff", 0, posInNotesArray + fretNum + addFret, 0));
                }
            }
        }
        // Now we have the midi information as a string arraylist, convert the strings to the byte array
        // This was originally done via the MidiDriver / billthefarmer library
        // However this was causing crashes on 64 bit devices, so changed
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            /*for (int i = 0; i < midiNotesOnArray.size(); i++) {
                String thisOnMessage = midiNotesOnArray.get(i);
                handler.postDelayed(() -> mainActivityInterface.sendToMidiDriver(returnBytesFromHexText(thisOnMessage)), noteOnDelta);
                noteOnDelta += timeBetweenNotes;
            }
            if (turnOffNoteTime>0) {
                // Prepare the note off messages after a delay if it is bigger than 0
                handler.postDelayed(() -> {
                    for (int i = 0; i < midiNotesOffArray.size(); i++) {
                        String thisOffMessage = midiNotesOffArray.get(i);
                        handler.postDelayed(() -> mainActivityInterface.sendToMidiDriver(returnBytesFromHexText(thisOffMessage)), noteOffDelta);
                        noteOffDelta += 100;
                    }
                }, turnOffNoteTime);
            }*/

            // Write the midi file and play it
            handler.post(this::createMidiFile);
        });
    }

    public void setMidiInstrument(String instrument) {
        instrumentLetter = instrument;
        // Get the General Midi program for the instrument
        switch (instrument) {
            case "g":
            default:
                midiInstrument = 25;
                break;

            case "p":
                midiInstrument = 0;
                break;

            case "u":
            case "m":
            case "c":
                midiInstrument = 24;
                break;

            case "b":
            case "B":
                midiInstrument = 105;
                break;
        }
        //String programChange = buildMidiString("PC", 0, midiInstrument, midiInstrument);
        //mainActivityInterface.sendToMidiDriver(returnBytesFromHexText(programChange));
    }

    public String buildMidiString(String action, int channel, int byte2, int byte3) {
        String s = "";
        String b1 = "0x";                                 // This initialises the hex numbering convention
        String b2 = " 0x" + String.format("%02X", byte2); // Convert numbers 0-127 to hex 2 digits
        String b3 = " 0x" + String.format("%02X", byte3);
        String hexString = Integer.toHexString(channel).toUpperCase(Locale.ROOT);
        String bCommon = b1 + "B" + Integer.toHexString(channel).toUpperCase(Locale.ROOT);
        switch (action) {
            case "NoteOn":
                b1 += "9" + hexString;
                s = b1 + b2 + b3;
                break;

            case "NoteOff":
                b1 += "8" + hexString;
                s = b1 + b2 + " 0x00";
                break;

            case "PC":
                b1 += "C" + hexString;
                s = b1 + b3;
                break;

            case "CC":
                s = bCommon + b2 + b3;
                break;

            case "MSB":
                s = bCommon + " 0x00" + b3;
                break;

            case "LSB":
                s = bCommon + " 0x20" + b3;
                break;
        }
        return s;
    }

    public byte[] returnBytesFromHexText(String lineofhextext) {
        /*
        This function can be called for each line of hex text stored with the song (each line is a command)
        Split the line into an array split by spaces
        Convert each section into an integer which is added to the bytes array
        */
        // First check for shorhand
        lineofhextext = checkForShortHandMIDI(lineofhextext).trim();

        String[] hexbits = lineofhextext.split(" ");
        byte[] bytes = new byte[hexbits.length];
        int i = 0;
        for (String hb : hexbits) {
            try {
                hb = hb.replace("0x", "");
                int z = getIntFromHexString(hb);
                bytes[i] = (byte) z;

            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }
        return bytes;
    }

    private int getIntFromHexString(String s) {
        int i = 0;
        if (s != null && !s.isEmpty()) {
            // Check for MIDI shorthand
            s = checkForShortHandMIDI(s).trim();
            try {
                i = Integer.parseInt(s, 16);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return i;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void disconnectDevice() {
        if (midiDevice != null) {
            try {
                midiDevice.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            midiDevice = null;
        }
        midiDeviceName = "";
        midiDeviceAddress = "";
        if (midiOutputPort != null && pedalMidiReceiver != null) {
            try {
                midiOutputPort.disconnect(pedalMidiReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void enableMidiListener() {
        if (midiDevice != null && midiOutputPort != null) {
            pedalMidiReceiver = new PedalMidiReceiver(this, mainActivityInterface);
            try {
                midiOutputPort.connect(pedalMidiReceiver);
            } catch (Exception e) {
                e.printStackTrace();
                pedalMidiReceiver = null;
            }
        } else {
            mainActivityInterface.getShowToast().doIt(c.getString(R.string.midi_error));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void disableMidiListener() {
        if (midiDevice != null && midiOutputPort != null && pedalMidiReceiver != null) {
            try {
                midiOutputPort.disconnect(pedalMidiReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void buildSongMidiMessages() {
        if (songMidiMessages == null) {
            songMidiMessages = new ArrayList<>();
        } else {
            songMidiMessages.clear();
        }

        String messages = mainActivityInterface.getSong().getMidi();

        if (messages != null) {
            messages = messages.trim();
            String[] bits = messages.split("\n");
            Collections.addAll(songMidiMessages, bits);
        }
    }

    @SuppressWarnings("unused")
    public ArrayList<String> getSongMidiMessages() {
        return songMidiMessages;
    }

    public void removeFromSongMessages(int position) {
        songMidiMessages.remove(position);
    }

    public void addToSongMessages(int position, String command) {
        // if -1, then add to the end, else add where requested
        if (position == -1) {
            songMidiMessages.add(command);
        } else {
            songMidiMessages.add(position, command);
        }
    }

    public void updateSongMessages() {
        StringBuilder s = new StringBuilder();
        for (String message : songMidiMessages) {
            if (!message.trim().isEmpty()) {
                s.append(message).append("\n");
            }
        }
        mainActivityInterface.getSong().setMidi(s.toString().trim());
        if (!mainActivityInterface.getSong().getFilename().equals("Welcome to OpenSongApp")) {
            mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
        }
    }

    public List<String> getStartNotes(String tuning) {
        List<String> startNote = new ArrayList<>();
        switch (instrumentLetter) {
            case "g":
            default:
                if (tuning.equals("standard")) {
                    startNote = guitarStringStartNotes;
                }
                break;
            case "p":
                startNote = pianoNotesStartNotes;
                break;
            case "u":
                startNote = ukuleleStringStartNotes;
                break;
            case "c":
                startNote = cavaquinhoStringStartNotes;
                break;
            case "b":
                startNote = banjo4StringStartNotes;
                break;
            case "B":
                startNote = banjo5StringStartNotes;
                break;
            case "m":
                startNote = mandolinStringStartNotes;
                break;
        }
        return startNote;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void createMidiFile() {
        // Create a temporary midi file
        File midiFile = new File(c.getExternalFilesDir("Midi"),"midiFile.mid");
        try (FileOutputStream fileOutputStream = new FileOutputStream(midiFile,false)){
            // Build the hex pair code
            String hexPairCode = "";

            // Append the header
            hexPairCode += midiFileHeader;

            // Get the number of bytes needed for the midi events(4 per item) + tempo(6) + notesOff(4) + file out(4)
            //int count = (midiNotesOnArray.size()*4)+ 4 + 4;

            // Now build the events
            String timeHex;
            int countTime = 0;
            StringBuilder stringBuilder = new StringBuilder();
            for (String onCommand:midiNotesOnArray) {
                onCommand = onCommand.replace("0x","");
                if (onCommand.length()==5) {
                    // Control change
                    timeHex = "00 ";
                } else {
                    if (countTime>0) {
                        timeHex = "40 ";
                    } else {
                        timeHex = "00 ";
                    }
                    countTime ++;
                }
                stringBuilder.append(timeHex).append(onCommand).append(" ");
            }
            String events = stringBuilder.toString();
            String tempoHex = getTempoByteString(120);

            int numEvents = (events+tempoHex+allOff+midiFileTrackOut).split(" ").length;
            String countHex = String.format("%02X", (0xFF & numEvents)) + " ";

            hexPairCode += midiFileTrackHeader + countHex;
            hexPairCode += tempoHex;
            hexPairCode += stringBuilder.toString();

            // Add a final all off
            hexPairCode += allOff;

            // Add the track out
            hexPairCode += midiFileTrackOut;

            // Write the bytes
            fileOutputStream.write(returnBytesFromHexText(hexPairCode.trim()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (midiMediaPlayer!=null) {
            midiMediaPlayer.release();
            midiMediaPlayer = null;
        }

        midiMediaPlayer = new MediaPlayer();

        midiMediaPlayer.setOnCompletionListener(mp -> {
            midiMediaPlayer.release();
            midiMediaPlayer = null;
        });
        midiMediaPlayer.setOnPreparedListener(mp -> midiMediaPlayer.start());
        Uri uri = Uri.fromFile(midiFile);
        try {
            midiMediaPlayer.setDataSource(c,uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        midiMediaPlayer.prepareAsync();
    }

    public String getTempoByteString(int bpm) {
        // bpm = beats per minute
        // For midi tempo, we need to convert to microseconds per quarter note
        // 120 bpm =  in 1 minute        = 120 quarter notes
        //            in 60 secs         = 120 quarter notes
        //            in 60/120 secs     = 1 quarter note
        //            in (60/120)*100000 = 1 quarter note
        int mspqn = (int) ((60f/(float)bpm) * 1000000);
        String hexVal = String.format("%06X", mspqn);
        String pair1 = hexVal.substring(0,2);
        String pair2 = hexVal.substring(2,4);
        String pair3 = hexVal.substring(4,6);
        // 00 at start for timestamp 0, FF 51 03 is tempo identifier hex code
        hexVal = "00 FF 51 03 " + pair1+" "+pair2+" "+pair3+" ";
        return hexVal;
    }

    public String getTimeSigByteString(String timeSig) {
        /*
        FF 58 04 nn dd cc bb Time Signature
        The time signature is expressed as four numbers.
        nn and dd represent the numerator and denominator of the time signature as it would be notated.
        The denominator is a negative power of two: 2 represents a quarter-note, 3 represents an eighth-note, etc.
        The cc parameter expresses the number of MIDI clocks in a metronome click.
        The bb parameter expresses the number of notated 32nd-notes in a MIDI quarter-note (24 MIDI clocks).
        This was added because there are already multiple programs which allow a user to specify that what MIDI
        thinks of as a quarter-note (24 clocks) is to be notated as, or related to in terms of, something else.

        Therefore, the complete event for 6/8 time, where the metronome clicks every three eighth-notes,
        but there are 24 clocks per quarter-note, 72 to the bar, would be (in hex):
        FF 58 04 06 03 24 08

        That is, 6/8 time (8 is 2 to the 3rd power, so this is 06 03),
        36 MIDI clocks per dotted-quarter (24 hex!),
        and eight notated 32nd-notes per quarter-note.
         */

        // 00 at start for timestamp 0, FF 58 04 is time sig identifier hex code
        String timeSigHex = "00 FF 58 04 ";

        // Get the numerator and denominator
        String[] sigBits = timeSig.split("/");
        int numerator = Integer.parseInt(sigBits[0]);
        timeSigHex += String.format("%02X", numerator)+" ";

        int denominator = Integer.parseInt(sigBits[1]);
        int fixeddenominator;
        switch (denominator) {
            case 2:
            case 4:
            case 8:
            case 16:
                fixeddenominator = denominator;
                break;
            default:
                fixeddenominator = 4;
                break;
        }

        int power = (int)(Math.log(fixeddenominator)/Math.log(2));
        timeSigHex += String.format("%02X", power)+" ";

        // Add midi clock info
        timeSigHex += "24 08 ";
        return timeSigHex;
    }

    public String getMidiFileHeader() {
        return midiFileHeader;
    }

    public String getMidiFileTrackHeader() {
        return midiFileTrackHeader;
    }

    public String getMidiFileTrackOut() {
        return midiFileTrackOut;
    }

    public String getAllOff() {
        return allOff;
    }

    public String checkForShortHandMIDI(String textToCheck) {
        return shortHandMidi.convertShorthandToMIDI(textToCheck);
    }

    public String getSysexStartCode() {
        return sysexStartCode;
    }

    public String getSysexStopCode() {
        return sysexStopCode;
    }

}

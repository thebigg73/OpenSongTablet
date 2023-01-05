package com.garethevans.church.opensongtablet.midi;

import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import org.billthefarmer.mididriver.GeneralMidiConstants;

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
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "Midi";

    // Initialise
    public Midi(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        midiDelay = mainActivityInterface.getPreferences().getMyPreferenceInt("midiDelay", 100);
    }

    private ArrayList<String> songMidiMessages = new ArrayList<>();
    private MidiDevice midiDevice;
    private MidiManager midiManager;
    private MidiInputPort midiInputPort;
    private MidiOutputPort midiOutputPort;
    private String midiDeviceName = "", midiDeviceAddress = "";
    private int midiInstrument;
    private String instrumentLetter;
    private boolean usePianoNotes;
    private ArrayList<String> midiNotesOnArray, midiNotesOffArray;
    private long noteOnDelta, noteOffDelta;

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

    @SuppressWarnings("unused")
    public void setMidiDelay(int midiDelay) {
        this.midiDelay = midiDelay;
    }

    String getMidiCommand(int i) {
        try {
            return midiCommands.get(i);
        } catch (Exception e) {
            return "PC";
        }
    }

    String getNoteFromInt(int i) {
        return notes.get(i);
    }

    String getReadableStringFromHex(String s) {
        // This tries to get a readable version of a midi hex line
        // e.g. try to convert 0x92 0x02 0x64 into "Channel 1 Note on Note D0 Velocity 100
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
        String msb = "MSB";
        String lsb = "LSB";

        String[] sections = s.trim().split(" ");
        if (sections.length >= 1 && sections[0] != null && !sections[0].isEmpty()) {
            String s0_0;
            String s0_1;
            try {
                s0_0 = sections[0].replace("0x", "").substring(0, 1);
                s0_1 = sections[0].replace("0x", "").substring(1);

                // The channel is the second digit (in hex) of the first byte
                channel = channel + " " + (getIntFromHexString(s0_1) + 1);
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
            }
        }

        // Now deal with the middle byte (note or program number)
        if (sections.length >= 2 && sections[1] != null && !sections[1].isEmpty()) {
            try {
                String s1 = sections[1].replace("0x", "").trim();
                int v1 = getIntFromHexString(s1);
                if (action.equals(contchange) && v1 == 32) {
                    // This is a LSB message
                    action = lsb;
                } else if (action.equals(contchange) && v1 == 0) {
                    // This is a MSB message
                    action = msb;
                } else if (action.equals(noteon) || action.equals(noteoff)) {
                    action = action + " " + notes.get(v1);
                } else {
                    action = action + " " + v1;
                }
            } catch (Exception e) {
                action = "?";
            }
        }
        // Now deal with the last byte (velocity or value) - not present for program change
        if (sections.length >= 3 && sections[2] != null && !sections[2].isEmpty()) {
            try {
                String s2 = sections[2].replace("0x", "").trim();
                int v2 = getIntFromHexString(s2);
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
        return message;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    boolean sendMidi(byte[] b) {
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
    void sendMidi(int position) {
        // Send midi from the arrayList
        if (position >= 0 && position < songMidiMessages.size()) {
            sendMidi(returnBytesFromHexText(songMidiMessages.get(position)));
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
                midiNotesOnArray.add(buildMidiString("PC", 0, GeneralMidiConstants.ACOUSTIC_GRAND_PIANO, GeneralMidiConstants.ACOUSTIC_GRAND_PIANO));
                midiNotesOffArray.add(buildMidiString("PC", 0, GeneralMidiConstants.ACOUSTIC_GRAND_PIANO, GeneralMidiConstants.ACOUSTIC_GRAND_PIANO));
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
                if (!chordNotes[i].equals("x")) {
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
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            for (int i = 0; i < midiNotesOnArray.size(); i++) {
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
            }
        });

    }

    public void setMidiInstrument(String instrument) {
        instrumentLetter = instrument;
        // Get the General Midi program for the instrument
        switch (instrument) {
            case "g":
            default:
                midiInstrument = GeneralMidiConstants.ACOUSTIC_GUITAR_STEEL;
                break;

            case "p":
                midiInstrument = GeneralMidiConstants.ACOUSTIC_GRAND_PIANO;
                break;

            case "u":
            case "m":
            case "c":
                midiInstrument = GeneralMidiConstants.ACOUSTIC_GUITAR_NYLON;
                break;

            case "b":
            case "B":
                midiInstrument = GeneralMidiConstants.BANJO;
                break;
        }
        String programChange = buildMidiString("PC", 0, midiInstrument, midiInstrument);
        mainActivityInterface.sendToMidiDriver(returnBytesFromHexText(programChange));
    }

    String buildMidiString(String action, int channel, int byte2, int byte3) {
        String s = "";
        String b1 = "0x";                               // This initialises the hex numbering convention
        String b2 = " 0x" + Integer.toHexString(byte2).toUpperCase(Locale.ROOT); // Convert numbers 0-127 to hex
        String b3 = " 0x" + Integer.toHexString(byte3).toUpperCase(Locale.ROOT);
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

    byte[] returnBytesFromHexText(String lineofhextext) {
        /*
        This function can be called for each line of hex text stored with the song (each line is a command)
        Split the line into an array split by spaces
        Convert each section into an integer which is added to the bytes array
        */

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
        if (s != null) {
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
            mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong());
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
}





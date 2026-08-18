package com.garethevans.church.opensongtablet.midi;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@TargetApi(Build.VERSION_CODES.M)
public class Midi {

    private final Context c;
    private final Activity activity;
    private final MainActivityInterface mainActivityInterface;
    private MidiInputReceiver pedalMidiReceiver;
    private final ShortHandMidi shortHandMidi;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "Midi";
    private MediaPlayer midiMediaPlayer;
    private String[] messageParts;
    private final String sysexStartCode = "0xF0 0x7F 0xFA 0xF7";
    private final String sysexStopCode = "0xF0 0x7F 0xFC 0xF7";
    private int midiInputChannelPedal, midiInputChannelSong, midiOutputChannel;
    private boolean midiInput, midiInputAutoscroll, midiInputMetronome, midiInputPad;
    private int midiClickTrackChannel, midiClickTrackTick = 76, midiClickTrackTock = 77,
            midiClickTrackTickVolume, midiClickTrackTockVolume;
    private String midiClickTickMessageOn="", midiClickTockMessageOn="", midiClickTickMessageOff="", midiClickTockMessageOff="";

    // Because some shorthand MIDI messages send two parts, we need to process them and then send separately
    // They get added to this array that is called and cleared after standard song messages are sent
    // Examples are MIDI1:BBBPM200 - needs two values, etc.
    private ArrayList<String> splitSongMidiMessages = new ArrayList<>();
    // Initialise
    public Midi(Activity activity,
                Context c) {
        this.activity = activity;
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        getUpdatedPreferences();
        shortHandMidi = new ShortHandMidi(c);
    }

    // If we change load in a profile, this is called
    public void getUpdatedPreferences() {
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
        midiInputChannelPedal = mainActivityInterface.getPreferences().getMyPreferenceInt("midiInputChannelPedal",8);
        midiInputChannelSong = mainActivityInterface.getPreferences().getMyPreferenceInt("midiInputChannelSong",9);
        midiOutputChannel = mainActivityInterface.getPreferences().getMyPreferenceInt("midiOutputChannel",1);
        midiInput = mainActivityInterface.getPreferences().getMyPreferenceBoolean("midiInput",false);
        midiInputAutoscroll = mainActivityInterface.getPreferences().getMyPreferenceBoolean("midiInputAutoscroll",false);
        midiInputMetronome = mainActivityInterface.getPreferences().getMyPreferenceBoolean("midiInputMetronome",false);
        midiInputPad = mainActivityInterface.getPreferences().getMyPreferenceBoolean("midiInputPad",false);
        midiClickTrackChannel = mainActivityInterface.getPreferences().getMyPreferenceInt("midiClickTrackChannel",10);
        midiClickTrackTick = mainActivityInterface.getPreferences().getMyPreferenceInt("midiClickTrackTick",76);
        midiClickTrackTock = mainActivityInterface.getPreferences().getMyPreferenceInt("midiClickTrackTock",77);
        midiClickTrackTickVolume = mainActivityInterface.getPreferences().getMyPreferenceInt("midiClickTrackTickVolume",110);
        midiClickTrackTockVolume = mainActivityInterface.getPreferences().getMyPreferenceInt("midiClickTrackTockVolume",110);
        setUpMidiTickTock();
    }

    public String getMidiAction(int which) {
        switch (which) {
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
            case 1:
            default:
                return midiAction1;
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
    private BluetoothDevice bluetoothDevice;
    private BluetoothManager bluetoothManager;
    private MidiManager midiManager;
    private MidiDevice currentMidiDevice;
    private MidiInputPort midiInputPort;
    private MidiOutputPort midiOutputPort;
    private String midiDeviceName = "", midiDeviceAddress = "";
    private int midiInstrument;
    private String instrumentLetter;
    private boolean usePianoNotes, midiSendAuto;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private ArrayList<String> midiNotesOnArray, midiNotesOffArray;
    private final String allOff = "7F B0 7B 00 ";
    @SuppressWarnings("FieldCanBeLocal")
    private long noteOnDelta, noteOffDelta;
    @SuppressWarnings("FieldCanBeLocal")
    private final String uuidBle = "03B80E5A-EDE8-4B33-A751-6CE34EC4C700";

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

    private boolean useDirectGatt = false; // Set to true if running on the A22 / fallback branch
    private BluetoothGatt activeBluetoothGatt = null;
    private BluetoothGattCharacteristic activeGattCharacteristic = null;

    //@RequiresApi(api = Build.VERSION_CODES.M)
    /*public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;

        if (bluetoothDevice == null) return;

        // 1. Determine if this device needs the direct GATT fallback (e.g., Samsung A22)
        useDirectGatt = shouldUseDirectGattFallback();
        registerBondReceiver();

        int bondState = bluetoothDevice.getBondState();

        if (useDirectGatt) {
            Log.d(TAG, "Routing via Direct GATT Fallback");

            // Setup the listener to capture the characteristic and store the gatt reference
            DirectMidiGattCallback.MidiGattReadyListener listener = new DirectMidiGattCallback.MidiGattReadyListener() {
                @Override
                public void onMidiCharacteristicReady(BluetoothGattCharacteristic characteristic) {
                    activeGattCharacteristic = characteristic;
                    Log.d(TAG, "Direct GATT is fully ready for MIDI output!");
                }

                @Override
                public void onDisconnected() {
                    activeBluetoothGatt = null;
                    activeGattCharacteristic = null;
                }
            };

            DirectMidiGattCallback callback = new DirectMidiGattCallback(listener);

            if (bondState == BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "Device already bonded. Connecting GATT directly...");
                // Connect with a small delay to ensure adapter stability
                connectWithRetry(c, bluetoothDevice, callback);
            } else {
                Log.d(TAG, "Device not bonded. Triggering createBond() and connecting...");
                bluetoothDevice.createBond();
                // Wait slightly for the bond request to register, then connect via GATT
                connectWithRetry(c, bluetoothDevice, callback);
            }

        } else {
            Log.d(TAG, "Routing via standard MidiManager pipeline");

            // Ensure GATT variables are cleared when using standard mode
            activeBluetoothGatt = null;
            activeGattCharacteristic = null;

            if (bondState != BluetoothDevice.BOND_BONDED) {
                bluetoothDevice.createBond();
            }

            // Call your existing native MidiManager openBluetoothDevice() method here
            // openStandardMidiDevice(bluetoothDevice);
        }
    }*/
    public void setBluetoothDevice(BluetoothDevice device) {
        this.bluetoothDevice = device;
        if (device == null) return;

        int bondState = device.getBondState();

        if (bondState == BluetoothDevice.BOND_BONDED) {
            Log.d(TAG, "Device already bonded. Proceeding with connection...");
            executeOpenDeviceWorkflow();
        } else {
            Log.d(TAG, "Device not bonded. Triggering createBond() and waiting for user confirmation...");

            // Register receiver BEFORE calling createBond() so we don't miss the broadcast
            try {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                c.registerReceiver(bondReceiver, filter);
                Log.d(TAG, "Bond BroadcastReceiver registered.");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Request pairing
            device.createBond();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void executeOpenDeviceWorkflow() {
        if (bluetoothDevice == null) return;

        // ABSOLUTE SAFETY GUARD: Refuse to open if not bonded
        if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            Log.w(TAG, "Blocked workflow attempt: Device is still not bonded!");
            return;
        }

        if (useDirectGatt) {
            DirectMidiGattCallback.MidiGattReadyListener listener = new DirectMidiGattCallback.MidiGattReadyListener() {
                @Override
                public void onMidiCharacteristicReady(BluetoothGattCharacteristic characteristic) {
                    activeGattCharacteristic = characteristic;
                    Log.d(TAG, "Direct GATT is fully ready for MIDI output!");
                }

                @Override
                public void onDisconnected() {
                    activeBluetoothGatt = null;
                    activeGattCharacteristic = null;
                }
            };
            DirectMidiGattCallback callback = new DirectMidiGattCallback(listener);
            connectWithRetry(c, bluetoothDevice, callback);
        } else {
            activeBluetoothGatt = null;
            activeGattCharacteristic = null;
            openBleMidiDevice(bluetoothDevice);
        }
    }

    public interface MidiDeviceOpenListener {
        void onDeviceOpenedSuccessfully(MidiDevice device);
        void onDeviceOpenFailed();
    }

    public interface OnMidiDeviceReadyListener {
        void onReady(MidiDevice device);
    }

    private OnMidiDeviceReadyListener readyListener;

    public void setOnMidiDeviceReadyListener(OnMidiDeviceReadyListener listener) {
        this.readyListener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initiateConnectionWorkflow() {
        if (bluetoothDevice == null) return;

        // ABSOLUTE SAFETY GUARD: Refuse to open if not bonded
        if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            Log.w(TAG, "Blocked workflow attempt: Device is still not bonded!");
            return;
        }

        if (useDirectGatt) {
            Log.d(TAG, "Routing via Direct GATT Fallback");

            DirectMidiGattCallback.MidiGattReadyListener listener = new DirectMidiGattCallback.MidiGattReadyListener() {
                @Override
                public void onMidiCharacteristicReady(BluetoothGattCharacteristic characteristic) {
                    activeGattCharacteristic = characteristic;
                    Log.d(TAG, "Direct GATT is fully ready for MIDI output!");
                }

                @Override
                public void onDisconnected() {
                    activeBluetoothGatt = null;
                    activeGattCharacteristic = null;
                }
            };

            DirectMidiGattCallback callback = new DirectMidiGattCallback(listener);
            connectWithRetry(c, bluetoothDevice, callback);

        } else {
            Log.d(TAG, "Routing via standard MidiManager pipeline");
            activeBluetoothGatt = null;
            activeGattCharacteristic = null;

            openBleMidiDevice(bluetoothDevice);
        }
    }

    public void connectWithRetry(Context context, BluetoothDevice device, BluetoothGattCallback callback) {
        // 1. Clean up any old instance first
        if (activeBluetoothGatt != null) {
            activeBluetoothGatt.close();
            activeBluetoothGatt = null;
        }

        // 2. 400ms delay lets the native stack clear its previous state
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (device == null) return;

            Log.d("GATT", "Initiating clean connectGatt for: " + device.getAddress());

            // CRITICAL FIX: Assign the returned BluetoothGatt object to your global variable
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activeBluetoothGatt = device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE);
            } else {
                activeBluetoothGatt = device.connectGatt(context, false, callback);
            }
        }, 400);
    }

    private boolean shouldUseDirectGattFallback() {
        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase(Locale.ROOT) : "";
        String model = Build.MODEL != null ? Build.MODEL.toLowerCase(Locale.ROOT) : "";

        // Target devices prone to MidiManager proxy drops (like the Samsung A22)
        boolean isSamsungBudget;
        isSamsungBudget = manufacturer.contains("samsung") && (
                model.contains("a22") ||
                        model.contains("a12") ||
                        model.contains("a13") ||
                        model.contains("a03")
        );

        return isSamsungBudget;
    }

    private boolean isReceiverRegistered = false;
    public void registerBondReceiver() {
        if (!isReceiverRegistered && c != null) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                c.registerReceiver(bondReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                c.registerReceiver(bondReceiver, filter);
            }
            isReceiverRegistered = true;
            Log.d(TAG, "Bond BroadcastReceiver registered.");
        }
    }

    public void unregisterBondReceiver() {
        if (isReceiverRegistered && c != null) {
            try {
                c.unregisterReceiver(bondReceiver);
                Log.d(TAG, "Bond BroadcastReceiver unregistered.");
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Receiver was already unregistered or never registered", e);
            }
            isReceiverRegistered = false;
        }
    }

    /*private final BroadcastReceiver bondReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (device != null && bluetoothDevice != null && device.getAddress().equals(bluetoothDevice.getAddress())) {
                    if (bondState == BluetoothDevice.BOND_BONDED) {
                        Log.d(TAG, "Pairing successful! Opening BLE MIDI device now.");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            openBleMidiDevice(device);
                        }
                    } else if (bondState == BluetoothDevice.BOND_NONE && previousBondState == BluetoothDevice.BOND_BONDING) {
                        Log.e(TAG, "Pairing failed or was rejected.");
                    }
                }
            }
        }
    };*/

    private final BroadcastReceiver bondReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device != null && bluetoothDevice != null && device.getAddress().equals(bluetoothDevice.getAddress())) {
                    int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if (bondState == BluetoothDevice.BOND_BONDED) {
                        Log.d(TAG, "Bond state changed: BONDED successfully! Proceeding with connection...");

                        // Unregister receiver safely
                        try {
                            c.unregisterReceiver(this);
                        } catch (Exception e) {
                            // Receiver might already be unregistered
                        }

                        // Give One UI 1.2 seconds to settle encryption keys after pairing popup closes
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            executeOpenDeviceWorkflow();
                        }, 1200);

                    } else if (bondState == BluetoothDevice.BOND_NONE && previousBondState == BluetoothDevice.BOND_BONDING) {
                        Log.e(TAG, "Bonding failed or was rejected by user.");
                        try {
                            c.unregisterReceiver(this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    @SuppressWarnings("unused")
    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public String getUuidBle() {
        return uuidBle;
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    String getMidiCommand(int i) {
        try {
            return midiCommands.get(i);
        } catch (Exception e) {
            return "PC";
        }
    }

    public String getNoteFromInt(int i) {
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
                    messageParts[0] = String.valueOf((getIntFromHexString(s0_1) + 1));
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
                        messageParts[2] = String.valueOf(v1);
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
                    messageParts[3] = String.valueOf(v2);
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

        if (useDirectGatt && activeGattCharacteristic != null && activeBluetoothGatt != null) {
            Log.d(TAG, "sendMidi: Using Direct GATT fallback path.");
            // ...
        } else if (midiInputPort != null) {
            Log.d(TAG, "sendMidi: Using standard MidiManager input port path.");
            // ...
        } else {
            Log.e(TAG, "sendMidi: Both paths failed! activeGatt: " + activeBluetoothGatt + ", inputPort: " + midiInputPort);
        }

        if (b==null || b.length==0) {
            return true;
        }

        // Check if we are using the Direct GATT fallback (for budget/problematic devices)
        if (useDirectGatt && activeGattCharacteristic != null && activeBluetoothGatt != null) {
            try {
                byte[] rawMidiBytes = b; // e.g., [-63, 0]

                // 1. Create the 2-byte BLE-MIDI header + timestamp overhead
                byte header = (byte) 0x80;    // Header byte: Bit 7 set, Bit 6 cleared
                byte timestamp = (byte) 0x80; // Timestamp byte: Bit 7 set, 13-bit timestamp zeroed for immediate playback

                // 2. Combine overhead with your actual MIDI message
                byte[] bleMidiPacket = new byte[rawMidiBytes.length + 2];
                bleMidiPacket[0] = header;
                bleMidiPacket[1] = timestamp;
                System.arraycopy(rawMidiBytes, 0, bleMidiPacket, 2, rawMidiBytes.length);

                // 3. Write the properly wrapped packet to the characteristic
                activeGattCharacteristic.setValue(bleMidiPacket);
                activeGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                //activeGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                success = activeBluetoothGatt.writeCharacteristic(activeGattCharacteristic);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Otherwise, use the standard modern MidiManager pipeline
        } else if (midiInputPort != null) {
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
            // Check for shorthand
            String message = songMidiMessages.get(position);
            byte[] bytes = returnBytesFromHexText(message);
            sendMidi(bytes);
        }
    }

    public void sendSongMessages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                songMidiMessages!=null && !songMidiMessages.isEmpty()) {
            for (int position = 0; position < songMidiMessages.size(); position++) {
                int finalPosition = position;
                new Handler().postDelayed(() -> {
                    if (songMidiMessages.get(finalPosition)!=null &&
                            !songMidiMessages.get(finalPosition).isEmpty()) {
                        sendMidi(returnBytesFromHexText(songMidiMessages.get(finalPosition)));
                    }
                }, (long) midiDelay *position);
            }
            // If we have additional splitMidiMessages (two parts from shorthand), send now:
            new Handler().postDelayed(this::sendSplitSongMIDIMessages,(long)midiDelay*songMidiMessages.size());
        }
    }
    public void sendSplitSongMIDIMessages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                splitSongMidiMessages!=null && !splitSongMidiMessages.isEmpty()) {
            for (int position = 0; position < splitSongMidiMessages.size(); position++) {
                int finalPosition = position;
                new Handler().postDelayed(() -> {
                    if (splitSongMidiMessages.size()>finalPosition && splitSongMidiMessages.get(finalPosition)!=null &&
                            !splitSongMidiMessages.get(finalPosition).isEmpty()) {
                        try {
                            byte[] bytes = returnBytesFromHexText(splitSongMidiMessages.get(finalPosition));
                            sendMidi(bytes);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, (long) midiDelay *position);
            }
            // Now clear the array
            new Handler().postDelayed(() -> {
                if (splitSongMidiMessages != null) {
                    splitSongMidiMessages.clear();
                }
            }, (long) midiDelay * splitSongMidiMessages.size());
        }
    }
    public int sendMidiHexSequence(String sequence) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                sequence!=null && !sequence.isEmpty()) {
            String[] messages = sequence.split("\n");
            for (int x=0; x<messages.length; x++) {
                int finalX = x;
                mainActivityInterface.getMainHandler().postDelayed(() -> sendMidi(returnBytesFromHexText(messages[finalX])), (long) midiDelay * x);
            }
            return midiDelay * messages.length;
        } else {
            return 0;
        }
    }

    @SuppressWarnings("unused")
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

                    int thisFret;
                    if (fretNum==0) {
                        // Don't add the fret number for open strings
                        thisFret = 0;
                    } else {
                        thisFret = fretNum + addFret - 1;
                    }

                    // Now go through the notes array the fretNum times
                    int posInNotesArray = notes.indexOf(openStringNote);
                    midiNotesOnArray.add(buildMidiString("NoteOn", 0, posInNotesArray + thisFret, 100));
                    midiNotesOffArray.add(buildMidiString("NoteOff", 0, posInNotesArray + thisFret, 0));
                }
            }
        }
        // Now we have the midi information as a string arraylist, convert the strings to the byte array
        // This was originally done via the MidiDriver / billthefarmer library
        // However this was causing crashes on 64 bit devices, so changed
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Write the midi file and play it
            mainActivityInterface.getMainHandler().post(this::createMidiFile);
        });
    }

    public void setMidiInstrument(String instrument) {
        instrumentLetter = instrument;
        // Get the General Midi program for the instrument
        switch (instrument) {
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
            case "g":
            default:
                midiInstrument = 25;
                break;
        }
    }

    public String buildMidiString(String action, int channel, int byte2, int byte3) {
        String s = "";
        String b1 = "0x";                                 // This initialises the hex numbering convention
        String b2 = "";
        String b3 = "";
        if (byte2>=0) {
            b2 = " 0x" + String.format("%02X", byte2); // Convert numbers 0-127 to hex 2 digits
        }
        if (byte3>=0) {
            b3 = " 0x" + String.format("%02X", byte3); // Convert numbers 0-127 to hex 2 digits
        }
        String hexString = Integer.toHexString(channel).toUpperCase(Locale.ROOT);
        String bCommon = b1 + "B" + Integer.toHexString(channel).toUpperCase(Locale.ROOT);
        switch (action) {
            case "NoteOn":
            case "NO":
                b1 += "9" + hexString;
                s = b1 + b2 + b3;
                break;

            case "NoteOff":
            case "NX":
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
        if (lineofhextext.contains("\n")) {
            // This needs to be added to the split arrays and sent later
            String[] splitline = lineofhextext.split("\n");
            for (String split:splitline) {
                split = split.trim();
                if (!split.isEmpty()) {
                    splitSongMidiMessages.add(split);
                }
            }
            // Return an empty array
            return new byte[0];
        }

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
    public void disconnectDevice() {
        if (midiDevice != null) {
            try {
                midiDevice.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            midiDevice = null;
        }
        if (currentMidiDevice != null) {
            try {
                currentMidiDevice.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentMidiDevice = null;
        }


        if (midiOutputPort != null && pedalMidiReceiver != null) {
            try {
                midiOutputPort.disconnect(pedalMidiReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        midiInputPort = null;
        midiOutputPort = null;
        midiDeviceName = "";
        midiDeviceAddress = "";

        tryDisconnectBluetoothLE();
        if (activeBluetoothGatt != null) {
            activeBluetoothGatt.disconnect();
            activeBluetoothGatt.close();
            activeBluetoothGatt = null;
        }
        activeGattCharacteristic = null;
        useDirectGatt = false;
    }

    public void tryDisconnectBluetoothLE() {
        // This unbonds so pairing is reinitialised next time
        unregisterBondReceiver();
        if (bluetoothDevice!=null) {
            try {
                Method m = bluetoothDevice.getClass()
                        .getMethod("removeBond");
                boolean result = (boolean) m.invoke(bluetoothDevice);
                Log.d(TAG, "removeBond() via reflection result = " + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            bluetoothDevice = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void enableMidiListener() {
        if (midiDevice != null && midiOutputPort != null) {
            pedalMidiReceiver = new MidiInputReceiver(c);
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
        splitSongMidiMessages.clear();

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
            case "g":
            default:
                if (tuning.equals("standard")) {
                    startNote = guitarStringStartNotes;
                }
                break;
        }
        return startNote;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void createMidiFile() {
        // Create a temporary midi file
        File midiFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Midi","","midiFile.mid");
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
            if (midiMediaPlayer.isPlaying()) {
                midiMediaPlayer.stop();
            }
            midiMediaPlayer.reset();
            midiMediaPlayer.release();
            midiMediaPlayer = null;
        }

        midiMediaPlayer = new MediaPlayer();

        midiMediaPlayer.setOnCompletionListener(mp -> {
            if (midiMediaPlayer!=null) {
                if (midiMediaPlayer.isPlaying()) {
                    midiMediaPlayer.stop();
                }
                midiMediaPlayer.reset();
                midiMediaPlayer.release();
                midiMediaPlayer = null;
            }
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public String getMidiFileHeader() {
        return midiFileHeader;
    }

    @SuppressWarnings("unused")
    public String getMidiFileTrackHeader() {
        return midiFileTrackHeader;
    }

    @SuppressWarnings("unused")
    public String getMidiFileTrackOut() {
        return midiFileTrackOut;
    }

    @SuppressWarnings("unused")
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

    public void setMidiInputChannelPedal (int midiInputChannelPedal) {
        this.midiInputChannelPedal = midiInputChannelPedal;
        mainActivityInterface.getPreferences().setMyPreferenceInt("midiInputChannelPedal",midiInputChannelPedal);
    }
    public int getMidiInputChannelPedal() {
        return midiInputChannelPedal;
    }
    public void setMidiInputChannelSong (int midiInputChannelSong) {
        this.midiInputChannelSong = midiInputChannelSong;
        mainActivityInterface.getPreferences().setMyPreferenceInt("midiInputChannelSong",midiInputChannelSong);
    }
    public int getMidiInputChannelSong() {
        return midiInputChannelSong;
    }


    public void setMidiOutputChannel (int midiOutputChannel) {
        this.midiOutputChannel = midiOutputChannel;
        mainActivityInterface.getPreferences().setMyPreferenceInt("midiOutputChannel",midiOutputChannel);
    }
    public int getMidiOutputChannel() {
        return midiOutputChannel;
    }

    public void setMidiInput(boolean midiInput) {
        this.midiInput = midiInput;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("midiInput",midiInput);
    }
    public boolean getMidiInput() {
        return midiInput;
    }

    public void setMidiInputAutoscroll(boolean midiInputAutoscroll) {
        this.midiInputAutoscroll = midiInputAutoscroll;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("midiInputAutoscroll",midiInputAutoscroll);
    }
    public boolean getMidiInputAutoscroll() {
        return midiInputAutoscroll;
    }

    public void setMidiInputMetronome(boolean midiInputMetronome) {
        this.midiInputMetronome = midiInputMetronome;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("midiInputMetronome",midiInputMetronome);
    }
    public boolean getMidiInputMetronome() {
        return midiInputMetronome;
    }

    public void setMidiInputPad(boolean midiInputPad) {
        this.midiInputPad = midiInputPad;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("midiInputPad",midiInputPad);
    }
    public boolean getMidiInputPad() {
        return midiInputPad;
    }

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    @SuppressWarnings("unused")
    private boolean isAirTurn(BluetoothDevice device) {
        boolean isAirTurn = false;
        if (device!=null) {
            String deviceName = device.getName();
            if (deviceName != null) {
                isAirTurn = deviceName.toLowerCase().contains("airturn");
            }
        }
        return isAirTurn;
    }

    // Scan for already connected Bluetooth MIDI devices
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setupBluetoothManager() {
        Log.d(TAG,"disconnecting MIDI devices");
        if (activity != null) {
            Object obj = activity.getSystemService(Context.BLUETOOTH_SERVICE);
            if (obj!=null) {
                bluetoothManager = (BluetoothManager) obj;
            } else {
                bluetoothManager = null;
            }
        }

        List<BluetoothDevice> connectedDevices;
        if (bluetoothDevice == null && bluetoothManager != null) {
            BluetoothAdapter adapter = bluetoothManager.getAdapter();

            if (adapter != null && adapter.isEnabled()) {
                try {
                    connectedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
                } catch (Exception e) {
                    e.printStackTrace();
                    connectedDevices = null;
                }
            } else {
                Log.w(TAG, "Bluetooth Adapter is null or disabled. Skipping device search.");
                connectedDevices = null;
            }

            if (midiManager == null) {
                setMidiManager((MidiManager) c.getSystemService(Context.MIDI_SERVICE));
            }
            if (connectedDevices != null && midiManager!=null) {
                for (BluetoothDevice device : connectedDevices) {
                    ParcelUuid[] uuids = device.getUuids();
                    if (uuids != null) {
                        for (ParcelUuid uuid : uuids) {
                            if (uuid.toString().equalsIgnoreCase(uuidBle) && !isAirTurn(device)) {
                                if (midiDevice == null &&
                                        device.getBondState() == BluetoothDevice.BOND_BONDED) {
                                    bluetoothDevice = device;
                                    tryDisconnectBluetoothLE();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Class-level flag to block overlapping calls
    private boolean isOpeningDevice = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initializePortsAndListener(boolean enableListener) {
        if (currentMidiDevice == null) {
            Log.e(TAG, "Cannot initialize ports: currentMidiDevice is null");
            return;
        }

        // Optional 400ms buffer if needed for BLE stack exposure
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                MidiDeviceInfo deviceInfo = currentMidiDevice.getInfo();
                if (deviceInfo != null && deviceInfo.getPorts() != null) {
                    for (MidiDeviceInfo.PortInfo pi : deviceInfo.getPorts()) {
                        if (pi.getType() == MidiDeviceInfo.PortInfo.TYPE_INPUT && midiInputPort == null) {
                            midiInputPort = currentMidiDevice.openInputPort(pi.getPortNumber());
                            Log.d(TAG, "Successfully bound MIDI Input Port: " + pi.getPortNumber());
                        } else if (pi.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT && midiOutputPort == null) {
                            midiOutputPort = currentMidiDevice.openOutputPort(pi.getPortNumber());
                            if (enableListener) {
                                enableMidiListener();
                            }
                            Log.d(TAG, "Successfully bound MIDI Output Port: " + pi.getPortNumber());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 400);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void openBleMidiDevice(android.bluetooth.BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice == null) {
            Log.e(TAG, "BluetoothDevice is null, cannot open MIDI.");
            return;
        }

        // Guard against simultaneous open attempts
        synchronized (this) {
            if (isOpeningDevice) {
                Log.w(TAG, "Open operation already in progress. Ignoring duplicate request for: " + bluetoothDevice.getAddress());
                return;
            }
            isOpeningDevice = true;
        }

        // 1. Force cleanup of any lingering previous connections before opening fresh
        if (currentMidiDevice != null) {
            try {
                currentMidiDevice.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentMidiDevice = null;
        }
        midiInputPort = null;
        midiOutputPort = null;

        midiManager = (MidiManager) c.getSystemService(Context.MIDI_SERVICE);
        if (midiManager == null) {
            Log.e(TAG, "MidiManager not available on this device.");
            isOpeningDevice = false; // Reset lock on early exit
            return;
        }

        Log.d(TAG, "Opening Bluetooth MIDI device via MidiManager for: " + bluetoothDevice.getAddress());

        midiManager.openBluetoothDevice(bluetoothDevice, midiDevice -> {
            // Release the lock now that the asynchronous open callback has returned
            isOpeningDevice = false;

            if (midiDevice == null) {
                Log.e(TAG, "Failed to open Bluetooth MIDI device (returned null proxy).");
                return;
            }

            currentMidiDevice = midiDevice;
            setMidiDevice(midiDevice);
            Log.d(TAG, "Device opened successfully via MidiManager!");

            // 2. Extended delay (800ms) to bypass One UI post-bond socket stabilization lag
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    if (midiInputPort == null) {
                        midiInputPort = currentMidiDevice.openInputPort(0);
                    }
                    if (midiOutputPort == null) {
                        midiOutputPort = currentMidiDevice.openOutputPort(0);
                    }

                    if (midiInputPort != null && midiOutputPort != null) {
                        Log.d(TAG, "Successfully bound MIDI Input & Output Ports!");
                        enableMidiListener();

                        // NOTIFY UI THAT EVERYTHING IS READY
                        if (readyListener != null) {
                            readyListener.onReady(currentMidiDevice);
                        }
                    } else {
                        Log.e(TAG, "Failed to bind one or more MIDI ports. Input: " + midiInputPort + ", Output: " + midiOutputPort);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 800);
        }, new Handler(Looper.getMainLooper()));
    }

    // The MIDI metronome stuff
    public void setUpMidiTickTock() {
        // Build the MIDI hex strings for the tick-tock
        midiClickTickMessageOn = buildMidiString("NoteOn",midiClickTrackChannel,midiClickTrackTick,midiClickTrackTickVolume);
        midiClickTickMessageOff = buildMidiString("NoteOff",midiClickTrackChannel,midiClickTrackTick,0);
        midiClickTockMessageOn = buildMidiString("NoteOn",midiClickTrackChannel,midiClickTrackTock,midiClickTrackTockVolume);
        midiClickTockMessageOff = buildMidiString("NoteOff",midiClickTrackChannel,midiClickTrackTock,0);
    }
    public String getMidiClickTickMessageOn() {
        return midiClickTickMessageOn;
    }
    public String getMidiClickTockMessageOn() {
        return midiClickTockMessageOn;
    }
    public void setMidiClickTrackChannel(int midiClickTrackChannel) {
        this.midiClickTrackChannel = midiClickTrackChannel;
        mainActivityInterface.getPreferences().setMyPreferenceInt("midiClickTrackChannel",midiClickTrackChannel);
        setUpMidiTickTock();
    }
    public void setMidiClickTrackTick(int midiClickTrackTick) {
        this.midiClickTrackTick = midiClickTrackTick;
        mainActivityInterface.getPreferences().setMyPreferenceInt("midiClickTrackTick",midiClickTrackTick);
        setUpMidiTickTock();
    }
    public void setMidiClickTrackTock(int midiClickTrackTock) {
        this.midiClickTrackTock = midiClickTrackTock;
        mainActivityInterface.getPreferences().setMyPreferenceInt("midiClickTrackTock",midiClickTrackTock);
        setUpMidiTickTock();
    }
    public void setMidiClickTrackTickVolume(int midiClickTrackTickVolume) {
        this.midiClickTrackTickVolume = midiClickTrackTickVolume;
        mainActivityInterface.getPreferences().setMyPreferenceInt("midiClickTrackTickVolume",midiClickTrackTickVolume);
        setUpMidiTickTock();
    }
    public void setMidiClickTrackTockVolume(int midiClickTrackTockVolume) {
        this.midiClickTrackTockVolume = midiClickTrackTockVolume;
        mainActivityInterface.getPreferences().setMyPreferenceInt("midiClickTrackTockVolume",midiClickTrackTockVolume);
        setUpMidiTickTock();
    }
    public int getMidiClickTrackChannel() {
        setUpMidiTickTock();
        return midiClickTrackChannel;
    }
    public int getMidiClickTrackTick() {
        setUpMidiTickTock();
        return midiClickTrackTick;
    }
    public int getMidiClickTrackTock() {
        setUpMidiTickTock();
        return midiClickTrackTock;
    }
    public int getMidiClickTrackTickVolume() {
        setUpMidiTickTock();
        return midiClickTrackTickVolume;
    }
    public int getMidiClickTrackTockVolume() {
        setUpMidiTickTock();
        return midiClickTrackTockVolume;
    }

    @SuppressWarnings("unused")
    public void sendMidiTick() {
        if (midiDevice!=null) {
            sendMidiHexSequence(midiClickTickMessageOn + "\n" + midiClickTockMessageOff);
        }
    }
    @SuppressWarnings("unused")
    public void sendMidiTock() {
        if (midiDevice!=null) {
            sendMidiHexSequence(midiClickTockMessageOn + "\n" + midiClickTickMessageOff);
        }
    }

}

package com.garethevans.church.opensongtablet.midi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.graphics.Color;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.databinding.SettingsMidiBinding;
import com.garethevans.church.opensongtablet.filemanagement.SaveSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.MidiAdapterInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;

public class MidiFragment extends Fragment {

    private SettingsMidiBinding myView;
    private Preferences preferences;
    private Midi midi;
    private Song song;
    private MainActivityInterface mainActivityInterface;
    private MidiAdapterInterface midiAdapterInterface;
    private StorageAccess storageAccess;
    private ConvertChoPro convertChoPro;
    private ProcessSong processSong;
    private SQLiteHelper sqLiteHelper;
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private CommonSQL commonSQL;
    private CCLILog ccliLog;
    private SaveSong saveSong;

    private final Handler selected = new Handler();
    private final Runnable runnable = this::displayCurrentDevice;

    private BluetoothLeScanner bluetoothLeScanner;
    private MidiDeviceInfo[] usbDevices;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private ArrayList<String> usbNames, usbManufact, midiCommand, midiChannel,
            midiNote, midiValue, songMidiMessages, songMidiMessagesToSave;
    private MidiMessagesAdapter midiMessagesAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        midiAdapterInterface = (MidiAdapterInterface) context;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsMidiBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(null,getString(R.string.settings) + " / " + getString(R.string.midi));

        // Register this fragment with the main activity to deal with listeners
        mainActivityInterface.registerFragment(this,"MidiFragment");

        // set Helpers
        setHelpers();

        new Thread(() -> requireActivity().runOnUiThread(() -> {
            // Set up the drop downs
            setUpMidiCommands();
            setUpMidiChannels();
            setUpMidiValues();
            setUpMidiNotes();

            // Set known values
            setValues();
            initialiseCurrentMessages();

            // Hide the desired views
            hideShowViews(true, false);

            // Set listeners
            setListeners();
        })).start();

        return myView.getRoot();
    }

    // Set the helper classes
    private void setHelpers() {
        preferences = new Preferences();
        midi = mainActivityInterface.getMidi(mainActivityInterface);
        song = mainActivityInterface.getSong();
        storageAccess = mainActivityInterface.getStorageAccess();
        convertChoPro = mainActivityInterface.getConvertChoPro();
        processSong = mainActivityInterface.getProcessSong();
        sqLiteHelper = mainActivityInterface.getSQLiteHelper();
        nonOpenSongSQLiteHelper = mainActivityInterface.getNonOpenSongSQLiteHelper();
        commonSQL = mainActivityInterface.getCommonSQL();
        ccliLog = mainActivityInterface.getCCLILog();
        saveSong = new SaveSong();
    }

    // Set the values in the field
    private void setUpMidiCommands() {
        midiCommand = new ArrayList<>();
        midiCommand.add(getString(R.string.note) + " " + getString(R.string.on));
        midiCommand.add(getString(R.string.note) + " " + getString(R.string.off));
        midiCommand.add(getString(R.string.midi_program));
        midiCommand.add(getString(R.string.midi_controller));
        midiCommand.add("MSB");
        midiCommand.add("LSB");
        ExposedDropDownArrayAdapter midiCommandAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, midiCommand);
        myView.midiCommand.setAdapter(midiCommandAdapter);
    }
    private void setUpMidiChannels() {
        // Remember that midi channel 1-16 are actually 0-15 in code
        midiChannel = new ArrayList<>();
        int i = 1;
        while (i<=16) {
            midiChannel.add(""+i);
            i++;
        }
        ExposedDropDownArrayAdapter midiChannelAdpter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, midiChannel);
        myView.midiChannel.setAdapter(midiChannelAdpter);
    }
    private void setUpMidiValues() {
        midiValue = new ArrayList<>();
        int i = 0;
        while (i<=127) {
            midiValue.add(""+i);
            i++;
        }
        ExposedDropDownArrayAdapter midiValueAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, midiValue);
        myView.midiController.setAdapter(midiValueAdapter);
        myView.midiValue.setAdapter(midiValueAdapter);
        myView.midiVelocity.setAdapter(midiValueAdapter);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setUpMidiNotes() {
        // Return an array adapter with music note representation of values 0-127
        midiNote = new ArrayList<>();
        int i = 0;
        while (i<=127) {
            midiNote.add(midi.getNoteFromInt(i));
            i++;
        }
        ExposedDropDownArrayAdapter midiNoteAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, midiNote);
        myView.midiNote.setAdapter(midiNoteAdapter);
    }
    private void setValues() {
        displayCurrentDevice();
        myView.enableBluetooth.setChecked(allowBluetoothSearch(midi.getIncludeBluetoothMidi()));
        myView.midiCommand.setText(midiCommand.get(2));     // Default to program change
        myView.midiChannel.setText(midiChannel.get(0));     // Default to 0->1
        myView.midiNote.setText(midiNote.get(60));          // Default to C5 (used instead of midiProgram)
        myView.midiController.setText(midiValue.get(0));    // This is the program number (for midi controller only)
        myView.midiValue.setText(midiValue.get(0));         // This is for the program number/value (alt to midiNote)
        myView.midiVelocity.setText(midiValue.get(99));     // This is the velocity
    }


    // Set the view visibilities
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void hideShowViews(boolean initialise, boolean isSearchingDevices) {
        // If a device is connected, hide the list of found devices
        if (initialise || !isSearchingDevices || midi.getMidiDevice() == null) {
            myView.searchProgressLayout.setVisibility(View.GONE);
        }
        if (midi.getMidiDevice() == null) {
            myView.connectionStatus.setVisibility(View.GONE);
        } else {
            myView.connectionStatus.setVisibility(View.VISIBLE);
            ((TextView) myView.connectedDevice.findViewById(R.id.mainText)).setText(midi.getMidiDeviceName());
            ((TextView) myView.connectedDevice.findViewById(R.id.subText)).setText(midi.getMidiDeviceAddress());
        }
        if (isSearchingDevices) {
            myView.searchProgressLayout.setVisibility(View.VISIBLE);
            myView.progressBar.setVisibility(View.VISIBLE);
        }

        // Now deal with the note, controller, value, velocity drop downs
        if (midiCommand.indexOf(myView.midiCommand.getText().toString())<2) {
            // Note on or off
            // Note on needs the velocity, note off doesn't
            setVisibilites(true,false,false, !myView.midiCommand.getText().toString().contains(getString(R.string.off)));

        } else if (initialise || midiCommand.indexOf(myView.midiCommand.getText().toString())==2) {
            // Program change
            setVisibilites(false, false, true, false);

            // Controller is value 3, LSB/MSB is the remaining (>3)
        } else setVisibilites(false, midiCommand.indexOf(myView.midiCommand.getText().toString()) == 3,true,false);

        getHexCodeFromDropDowns();
    }
    private void setVisibilites(boolean note, boolean controller, boolean value, boolean velocity) {
        if (note) {
            myView.midiNote.setVisibility(View.VISIBLE);
            myView.midiNoteLayout.setVisibility(View.VISIBLE);
        } else {
            myView.midiNote.setVisibility(View.GONE);
            myView.midiNoteLayout.setVisibility(View.GONE);
        }
        if (controller) {
            myView.midiController.setVisibility(View.VISIBLE);
            myView.midiControllerLayout.setVisibility(View.VISIBLE);
        } else {
            myView.midiController.setVisibility(View.GONE);
            myView.midiControllerLayout.setVisibility(View.GONE);
        }
        if (value) {
            myView.midiValue.setVisibility(View.VISIBLE);
            myView.midiValueLayout.setVisibility(View.VISIBLE);
        } else {
            myView.midiValue.setVisibility(View.GONE);
            myView.midiValueLayout.setVisibility(View.GONE);
        }
        if (velocity) {
            myView.midiVelocity.setVisibility(View.VISIBLE);
            myView.midiVelocityLayout.setVisibility(View.VISIBLE);
        } else {
            myView.midiVelocity.setVisibility(View.GONE);
            myView.midiVelocityLayout.setVisibility(View.GONE);
        }
    }
    // Set the view listeners
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setListeners() {
        myView.enableBluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Check we have the permission
                if (!allowBluetoothSearch(true)) {
                    myView.enableBluetooth.setChecked(false);
                    return;
                }
            }
            if (isChecked) {
                // Get scanner.  This is only allowed for Marshmallow or later
                BluetoothManager bluetoothManager = (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);
                if (bluetoothManager!=null) {
                    BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                    bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                }
            }
            midi.setIncludeBluetoothMidi(isChecked);
        });
        myView.searchDevices.setOnClickListener(v -> startScan());
        myView.testMidiDevice.setOnClickListener(v -> sendTestNote());
        myView.disconnectMidiDevice.setOnClickListener(v -> disconnectDevices());
        myView.autoSendBluetooth.setOnCheckedChangeListener(((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"midiSendAuto",false)));
        myView.midiAsPedal.setOnCheckedChangeListener(((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getContext(),"midiAsPedal",false)));
        myView.midiCommand.addTextChangedListener(new MyTextWatcher());
        myView.midiChannel.addTextChangedListener(new MyTextWatcher());
        myView.midiNote.addTextChangedListener(new MyTextWatcher());
        myView.midiController.addTextChangedListener(new MyTextWatcher());
        myView.midiValue.addTextChangedListener(new MyTextWatcher());
        myView.midiVelocity.addTextChangedListener(new MyTextWatcher());
        myView.midiTest.setOnClickListener(v -> testTheMidiMessage(myView.midiCode.getText().toString()));
        myView.midiAdd.setOnClickListener(v -> addMidiToList());
        myView.midiAsPedal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && midi.getMidiDevice()!=null && midi.getMidiOutputPort()!=null) {
                midi.enableMidiListener();
            } else if (!isChecked && midi.getMidiDevice()!=null && midi.getMidiOutputPort()!=null) {
                midi.disableMidiListener();
            }
        });
    }
    private class MyTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void afterTextChanged(Editable s) {
            hideShowViews(false, false);
            getHexCodeFromDropDowns();
        }
    }

    // Check permissions
    private boolean allowBluetoothSearch(boolean switchOn) {
        return mainActivityInterface.requestCoarseLocationPermissions() && mainActivityInterface.requestFineLocationPermissions() && switchOn;
    }


    // Scan for devices (USB or Bluetooth)
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScan() {
        // Try to initialise the midi manager
        midi.setMidiManager((MidiManager) requireActivity().getSystemService(Context.MIDI_SERVICE));
        myView.searchProgressLayout.setVisibility(View.VISIBLE);
        myView.progressBar.setVisibility(View.VISIBLE);
        if (midi.getIncludeBluetoothMidi()) {
            startScanBluetooth();
        } else {
            startScanUSB();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScanUSB() {

        if (midi.getMidiManager()!=null) {
            usbDevices = midi.getMidiManager().getDevices();
            usbNames = new ArrayList<>();
            usbNames.clear();
            usbManufact = new ArrayList<>();
            usbManufact.clear();
            for (MidiDeviceInfo md : usbDevices) {
                String manuf = getString(R.string.unknown);
                String device = getString(R.string.unknown);
                try {
                    device = md.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
                    manuf = md.getProperties().getString(MidiDeviceInfo.PROPERTY_MANUFACTURER);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (device != null) {
                    usbNames.add(device);
                } else {
                    usbNames.add(getString(R.string.unknown));
                }
                if (manuf != null) {
                    usbManufact.add(manuf);
                } else {
                    usbManufact.add(getString(R.string.unknown));
                }
            }
            myView.progressBar.setVisibility(View.GONE);
            myView.searchDevices.setEnabled(true);
            myView.foundDevicesLayout.setVisibility(View.VISIBLE);
            myView.enableBluetooth.setEnabled(true);
            updateDevices(false);

        } else {
            myView.progressBar.setVisibility(View.GONE);
            myView.searchDevices.setEnabled(true);
            myView.foundDevicesLayout.setVisibility(View.GONE);
            myView.enableBluetooth.setEnabled(true);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScanBluetooth() {
        bluetoothDevices = new ArrayList<>();
        bluetoothDevices.clear();
        myView.foundDevicesLayout.removeAllViews();
        myView.devicesText.setVisibility(View.GONE);
        // Stops scanning after a pre-defined scan period.
        Handler mHandler = new Handler();
        long SCAN_PERIOD = 16000;
        mHandler.postDelayed(() -> {
            try {
                bluetoothLeScanner.stopScan(scanCallback);
                myView.progressBar.setVisibility(View.GONE);
                myView.searchDevices.setEnabled(true);
                myView.foundDevicesLayout.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, SCAN_PERIOD);

        //scan specified devices only with ScanFilter
        ScanFilter scanFilter =
                new ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700"))
                        .build();
        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(scanFilter);

        ScanSettings scanSettings = new ScanSettings.Builder().build();

        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
        } else {
            ShowToast.showToast(getContext(), getString(R.string.nothighenoughapi));
            myView.enableBluetooth.setEnabled(true);
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            addBluetoothDevice(result.getDevice());
            updateDevices(true);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                addBluetoothDevice(result.getDevice());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("d", "onScanFailed: " + errorCode);
        }

        private void addBluetoothDevice(BluetoothDevice device) {
            Log.d("d","device="+device);
            if (device != null && !bluetoothDevices.contains(device)) {
                bluetoothDevices.add(device);
                Log.d("d", "name=" + device.getName());
            }
        }
    };
    private void displayCurrentDevice() {
        Log.d("d", "displayCurrentDevice()");
        if (midi.getMidiDevice() != null && midi.getMidiDeviceName() != null && midi.getMidiDeviceAddress() != null) {
            myView.searchProgressLayout.setVisibility(View.GONE);
            myView.connectionStatus.setVisibility(View.VISIBLE);
            ((TextView) myView.connectedDevice.findViewById(R.id.mainText)).setText(midi.getMidiDeviceName());
            ((TextView) myView.connectedDevice.findViewById(R.id.subText)).setText(midi.getMidiDeviceAddress());
        } else {
            myView.searchProgressLayout.setVisibility(View.VISIBLE);
            myView.connectionStatus.setVisibility(View.GONE);
            ((TextView) myView.connectedDevice.findViewById(R.id.mainText)).setText("");
            ((TextView) myView.connectedDevice.findViewById(R.id.subText)).setText("");
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateDevices(boolean bluetoothscan) {
        try {
            myView.enableBluetooth.setEnabled(true);

            // Clear the found devices
            myView.foundDevicesLayout.removeAllViews();

            int size;
            if (bluetoothscan) {
                size = bluetoothDevices.size();
            } else {
                size = usbDevices.length;
            }

            if (size>0) {
                myView.devicesText.setVisibility(View.VISIBLE);
            } else {
                myView.devicesText.setVisibility(View.GONE);
            }

            // For each device, add a new text view
            for (int x = 0; x < size; x++) {
                TextView textView = new TextView(getContext());
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.LTGRAY);
                LinearLayout.LayoutParams  llp= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                llp.setMargins(12,12,12,12);
                textView.setLayoutParams(llp);
                textView.setText(bluetoothDevices.get(x).getName());
                textView.setTextSize(18.0f);
                textView.setPadding(24, 24, 24, 24);
                int finalX = x;
                textView.setOnClickListener(v -> {
                    // Disconnect any other devices
                    midi.disconnectDevice();
                    // Set the new details

                    if (bluetoothscan) {
                        midi.setMidiDeviceName(bluetoothDevices.get(finalX).getName());
                        midi.setMidiDeviceAddress(bluetoothDevices.get(finalX).getAddress());
                    } else {
                        midi.setMidiDeviceName(usbNames.get(finalX));
                        midi.setMidiDeviceAddress(usbManufact.get(finalX));
                    }
                    midi.setMidiManager((MidiManager) requireActivity().getSystemService(Context.MIDI_SERVICE));

                    if (bluetoothscan && midi.getMidiManager() != null) {
                        midi.getMidiManager().openBluetoothDevice(bluetoothDevices.get(finalX), device -> {
                            midi.setMidiDevice(device);
                            setupDevice(device);
                            selected.postDelayed(runnable, 1000);
                        }, null);
                    } else if (midi.getMidiManager()!=null) {
                        midi.getMidiManager().openDevice(usbDevices[finalX], device -> {
                            midi.setMidiDevice(device);
                            setupDevice(device);
                            selected.postDelayed(runnable, 1000);
                        }, null);
                    }
                });
                myView.foundDevicesLayout.addView(textView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Send midi data
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendTestNote() {
        try {
            //byte[] b = midi.returnBytesFromHexText("0x90 0x3C 0x63");
            //midi.sendMidi(b);
            String s1 = midi.buildMidiString("NoteOn", 1, 60, 100);
            Log.d("d","s1="+s1);
            byte[] buffer1 = midi.returnBytesFromHexText(s1);
            boolean sent = midi.sendMidi(buffer1);

            Handler h = new Handler();
            h.postDelayed(() -> {
                String s2 = midi.buildMidiString("NoteOff", 1, 60, 0);
                byte[] buffer2 = midi.returnBytesFromHexText(s2);
                midi.sendMidi(buffer2);
            }, 1000);
            if (sent) {
                ShowToast.showToast(getContext(), getString(android.R.string.ok));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ShowToast.showToast(getContext(), getString(R.string.error));
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void testTheMidiMessage(String mm) {
        // Test the midi message being sent
        // First split by spaces
        boolean success = false;
        try {
            byte[] b = midi.returnBytesFromHexText(mm);
            success = midi.sendMidi(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!success) {
            ShowToast.showToast(getContext(),getString(R.string.midi_error));
        } else {
            ShowToast.showToast(getContext(), getString(android.R.string.ok));
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    // Called back from main activity
    public void sendMidiFromList(int i) {
        String s = songMidiMessagesToSave.get(i);
        testTheMidiMessage(s);
    }


    // Connect or disconnect devices
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupDevice(MidiDevice device) {
        midi.setMidiDevice(device);
        Log.d("d", "Device opened = " + device);
        MidiDeviceInfo midiDeviceInfo = midi.getMidiDevice().getInfo();
        int numInputs = midiDeviceInfo.getInputPortCount();
        int numOutputs = midiDeviceInfo.getOutputPortCount();
        Log.d("d", "Input ports = " + numInputs + ", Output ports = " + numOutputs);

        boolean foundinport = false;  // We will only grab the first one
        boolean foundoutport = false; // We will only grab the first one

        MidiDeviceInfo.PortInfo[] portInfos = midiDeviceInfo.getPorts();
        for (MidiDeviceInfo.PortInfo pi : portInfos) {
            switch (pi.getType()) {
                case MidiDeviceInfo.PortInfo.TYPE_INPUT:
                    if (!foundinport) {
                        Log.d("d", "Input port found = " + pi.getPortNumber());
                        midi.setMidiInputPort(midi.getMidiDevice().openInputPort(pi.getPortNumber()));
                        foundinport = true;
                    }
                    break;
                case MidiDeviceInfo.PortInfo.TYPE_OUTPUT:
                    if (!foundoutport) {
                        Log.d("d", "Output port found = " + pi.getPortNumber());
                        midi.setMidiOutputPort(midi.getMidiDevice().openOutputPort(pi.getPortNumber()));
                        if (myView.midiAsPedal.isChecked()) {
                            midi.enableMidiListener();
                        }
                        foundoutport = true;
                    }
                    break;
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void disconnectDevices() {
        midi.disconnectDevice();
        displayCurrentDevice();
    }


    // Deal with creating midi messages
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getHexCodeFromDropDowns() {
        int commandInt = midiCommand.indexOf(myView.midiCommand.getText().toString());
        String command = midi.getMidiCommand(commandInt);
        int channel = midiChannel.indexOf(myView.midiChannel.getText().toString());
        int noteorcontroller;
        int valueorvelocity;
        if (commandInt<2) {
            // Using midi note
            noteorcontroller = midiNote.indexOf(myView.midiNote.getText().toString());
            valueorvelocity = midiValue.indexOf(myView.midiVelocity.getText().toString());
        } else {
            noteorcontroller = midiValue.indexOf(myView.midiController.getText().toString());
            valueorvelocity = midiValue.indexOf(myView.midiValue.getText().toString());
        }
        String midiString;
        try {
            midiString = midi.buildMidiString(command,channel,noteorcontroller,valueorvelocity);
        } catch (Exception e) {
            midiString = "0x00 0x00 0x00";
        }
        myView.midiCode.setText(midiString);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addMidiToList() {
        try {
            String s = myView.midiCode.getText().toString();
            String hr = midi.getReadableStringFromHex(s, requireContext());
            String message = hr + "\n" + "(" + s + ")";
            songMidiMessagesToSave.add(s);
            songMidiMessages.add(message);
            updateCurrentMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }

        saveSongMessages();
    }
    // Called back from MainActivity
    public void deleteMidiFromList(int i) {
        try {
            songMidiMessages.remove(i);
            songMidiMessagesToSave.remove(i);
            updateCurrentMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
        saveSongMessages();
    }


    // Process song midi messages
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initialiseCurrentMessages() {
        songMidiMessages = new ArrayList<>();
        songMidiMessagesToSave = new ArrayList<>();

        // Add what is there already
        String[] bits = song.getMidi().trim().split("\n");
        for (String s : bits) {
            if (s!=null && !s.equals("") && !s.isEmpty() && getActivity()!=null) {
                // Get a human readable version of the midi code
                String hr = midi.getReadableStringFromHex(s,getActivity());
                String message = hr + "\n" + "(" + s + ")";
                songMidiMessages.add(message);
                songMidiMessagesToSave.add(s);
            }
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        myView.recyclerView.setLayoutManager(linearLayoutManager);

        // specify an adapter (see also next example)
        midiMessagesAdapter = new MidiMessagesAdapter(getContext(),midiAdapterInterface,songMidiMessages);
        myView.recyclerView.setAdapter(midiMessagesAdapter);
        midiMessagesAdapter.notifyDataSetChanged();

    }
    private void updateCurrentMessages() {
        midiMessagesAdapter.notifyDataSetChanged();
        myView.recyclerView.setAdapter(midiMessagesAdapter);
    }


    // Save the song messages
    private void saveSongMessages() {
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
            song.setMidi(s.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Save the song
        saveSong.doSave(getContext(),preferences,storageAccess,convertChoPro,
                processSong,song,sqLiteHelper,nonOpenSongSQLiteHelper,commonSQL,ccliLog,false);
    }
}

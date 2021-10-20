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
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsMidiBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.MidiAdapterInterface;
import com.garethevans.church.opensongtablet.interfaces.MidiItemTouchInterface;

import java.util.ArrayList;
import java.util.List;

public class MidiFragment extends Fragment {

    private SettingsMidiBinding myView;
    private MainActivityInterface mainActivityInterface;
    private MidiAdapterInterface midiAdapterInterface;

    private final Handler selected = new Handler();
    private final Runnable runnable = this::displayCurrentDevice;

    private BluetoothLeScanner bluetoothLeScanner;
    private MidiDeviceInfo[] usbDevices;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private ArrayList<String> usbNames, usbManufact, midiCommand, midiChannel,
            midiNote, midiValue;
    private ArrayList<MidiInfo> midiInfos;
    private MidiMessagesAdapter midiMessagesAdapter;
    private MidiItemTouchInterface midiItemTouchInterface;
    private LinearLayoutManager llm;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        midiAdapterInterface = (MidiAdapterInterface) context;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = requireActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsMidiBinding.inflate(inflater, container, false);

        // Set pan for keyboard
        if (requireActivity().getWindow()!=null) {
            requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        mainActivityInterface.updateToolbar(getString(R.string.midi));

        // Register this fragment with the main activity to deal with listeners
        mainActivityInterface.registerFragment(this,"MidiFragment");

        new Thread(() -> requireActivity().runOnUiThread(() -> {
            // Set up the drop downs
            setUpMidiCommands();
            setUpMidiChannels();
            setUpMidiValues();
            setUpMidiNotes();

            // Set known values
            setValues();
            setupAdapter();
            buildList();
            //initialiseCurrentMessages();

            // Hide the desired views
            hideShowViews(true, false);

            // Set listeners
            setListeners();
        })).start();

        return myView.getRoot();
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
        ExposedDropDownArrayAdapter midiCommandAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item, midiCommand);
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
        ExposedDropDownArrayAdapter midiChannelAdpter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item, midiChannel);
        myView.midiChannel.setAdapter(midiChannelAdpter);
    }
    private void setUpMidiValues() {
        midiValue = new ArrayList<>();
        int i = 0;
        while (i<=127) {
            midiValue.add(""+i);
            i++;
        }
        ExposedDropDownArrayAdapter midiValueAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item, midiValue);
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
            midiNote.add(mainActivityInterface.getMidi().getNoteFromInt(i));
            i++;
        }
        ExposedDropDownArrayAdapter midiNoteAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item, midiNote);
        myView.midiNote.setAdapter(midiNoteAdapter);
    }
    private void setValues() {
        displayCurrentDevice();
        myView.enableBluetooth.setChecked(allowBluetoothSearch(mainActivityInterface.getMidi().getIncludeBluetoothMidi()));
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
        if (initialise || !isSearchingDevices || mainActivityInterface.getMidi().getMidiDevice() == null) {
            myView.searchProgressLayout.setVisibility(View.GONE);
        }
        if (mainActivityInterface.getMidi().getMidiDevice() == null) {
            myView.connectionStatus.setVisibility(View.GONE);
        } else {
            myView.connectionStatus.setVisibility(View.VISIBLE);
            myView.connectedDevice.setText(mainActivityInterface.getMidi().getMidiDeviceName());
            myView.connectedDevice.setHint(mainActivityInterface.getMidi().getMidiDeviceAddress());
        }
        if (isSearchingDevices) {
            myView.searchProgressLayout.setVisibility(View.VISIBLE);
            myView.progressBar.setVisibility(View.VISIBLE);
        }

        // Set the pedal preference
        myView.midiAsPedal.setChecked(mainActivityInterface.getPedalActions().getMidiAsPedal());

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
        } else {
            myView.midiNote.setVisibility(View.GONE);
        }
        if (controller) {
            myView.midiController.setVisibility(View.VISIBLE);
        } else {
            myView.midiController.setVisibility(View.GONE);
        }
        if (value) {
            myView.midiValue.setVisibility(View.VISIBLE);
        } else {
            myView.midiValue.setVisibility(View.GONE);
        }
        if (velocity) {
            myView.midiVelocity.setVisibility(View.VISIBLE);
        } else {
            myView.midiVelocity.setVisibility(View.GONE);
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
            mainActivityInterface.getMidi().setIncludeBluetoothMidi(isChecked);
        });
        myView.searchDevices.setOnClickListener(v -> startScan());
        myView.testMidiDevice.setOnClickListener(v -> sendTestNote());
        myView.disconnectMidiDevice.setOnClickListener(v -> disconnectDevices());
        myView.autoSendBluetooth.setOnCheckedChangeListener(((buttonView, isChecked) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean(getContext(),"midiSendAuto",false)));
        myView.midiAsPedal.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(getContext(),"midiAsPedal",isChecked);
            mainActivityInterface.getPedalActions().setMidiAsPedal(isChecked);
            if (isChecked) {
                mainActivityInterface.getMidi().enableMidiListener(requireContext());
            }
        }));
        myView.midiCommand.addTextChangedListener(new MyTextWatcher());
        myView.midiChannel.addTextChangedListener(new MyTextWatcher());
        myView.midiNote.addTextChangedListener(new MyTextWatcher());
        myView.midiController.addTextChangedListener(new MyTextWatcher());
        myView.midiValue.addTextChangedListener(new MyTextWatcher());
        myView.midiVelocity.addTextChangedListener(new MyTextWatcher());
        myView.midiTest.setOnClickListener(v -> testTheMidiMessage(myView.midiCode.getText().toString()));
        myView.midiAdd.setOnClickListener(v -> addMidiToList());
        myView.midiAsPedal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && mainActivityInterface.getMidi().getMidiDevice()!=null &&
                    mainActivityInterface.getMidi().getMidiOutputPort()!=null) {
                mainActivityInterface.getMidi().enableMidiListener(requireContext());
            } else if (!isChecked && mainActivityInterface.getMidi().getMidiDevice()!=null &&
                    mainActivityInterface.getMidi().getMidiOutputPort()!=null) {
                mainActivityInterface.getMidi().disableMidiListener();
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
        mainActivityInterface.getMidi().setMidiManager((MidiManager) requireActivity().getSystemService(Context.MIDI_SERVICE));
        myView.searchProgressLayout.setVisibility(View.VISIBLE);
        myView.progressBar.setVisibility(View.VISIBLE);
        if (mainActivityInterface.getMidi().getIncludeBluetoothMidi()) {
            startScanBluetooth();
        } else {
            startScanUSB();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScanUSB() {

        if (mainActivityInterface.getMidi().getMidiManager()!=null) {
            usbDevices = mainActivityInterface.getMidi().getMidiManager().getDevices();
            usbNames = new ArrayList<>();
            usbManufact = new ArrayList<>();
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
            mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.error));
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
        if (mainActivityInterface.getMidi().getMidiDevice() != null && mainActivityInterface.getMidi().getMidiDeviceName() != null &&
                mainActivityInterface.getMidi().getMidiDeviceAddress() != null) {
            myView.searchProgressLayout.setVisibility(View.GONE);
            myView.connectionStatus.setVisibility(View.VISIBLE);
            myView.connectedDevice.setText(mainActivityInterface.getMidi().getMidiDeviceName());
            myView.connectedDevice.setHint(mainActivityInterface.getMidi().getMidiDeviceAddress());
        } else {
            myView.searchProgressLayout.setVisibility(View.VISIBLE);
            myView.connectionStatus.setVisibility(View.GONE);
            myView.connectedDevice.setText("");
            myView.connectedDevice.setHint("");
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
                    mainActivityInterface.getMidi().disconnectDevice();
                    // Set the new details

                    if (bluetoothscan) {
                        mainActivityInterface.getMidi().setMidiDeviceName(bluetoothDevices.get(finalX).getName());
                        mainActivityInterface.getMidi().setMidiDeviceAddress(bluetoothDevices.get(finalX).getAddress());
                    } else {
                        mainActivityInterface.getMidi().setMidiDeviceName(usbNames.get(finalX));
                        mainActivityInterface.getMidi().setMidiDeviceAddress(usbManufact.get(finalX));
                    }
                    mainActivityInterface.getMidi().setMidiManager((MidiManager) requireActivity().getSystemService(Context.MIDI_SERVICE));

                    if (bluetoothscan && mainActivityInterface.getMidi().getMidiManager() != null) {
                        mainActivityInterface.getMidi().getMidiManager().openBluetoothDevice(bluetoothDevices.get(finalX), device -> {
                            mainActivityInterface.getMidi().setMidiDevice(device);
                            setupDevice(device);
                            selected.postDelayed(runnable, 1000);
                        }, null);
                    } else if (mainActivityInterface.getMidi().getMidiManager()!=null) {
                        mainActivityInterface.getMidi().getMidiManager().openDevice(usbDevices[finalX], device -> {
                            mainActivityInterface.getMidi().setMidiDevice(device);
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
            String s1 = mainActivityInterface.getMidi().buildMidiString("NoteOn", 1, 60, 100);
            Log.d("d","s1="+s1);
            byte[] buffer1 = mainActivityInterface.getMidi().returnBytesFromHexText(s1);
            boolean sent = mainActivityInterface.getMidi().sendMidi(buffer1);

            Handler h = new Handler();
            h.postDelayed(() -> {
                String s2 = mainActivityInterface.getMidi().buildMidiString("NoteOff", 1, 60, 0);
                byte[] buffer2 = mainActivityInterface.getMidi().returnBytesFromHexText(s2);
                mainActivityInterface.getMidi().sendMidi(buffer2);
            }, 1000);
            if (sent) {
                mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.ok));
            }
        } catch (Exception e) {
            e.printStackTrace();
            mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.error));

        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void testTheMidiMessage(String mm) {
        // Test the midi message being sent
        // First split by spaces
        boolean success = false;
        try {
            byte[] b = mainActivityInterface.getMidi().returnBytesFromHexText(mm);
            success = mainActivityInterface.getMidi().sendMidi(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!success) {
            mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.midi_error));
        } else {
            mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.ok));
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    // Called back from main activity
    public void sendMidiFromList(int i) {
        String s = midiMessagesAdapter.getMidiInfos().get(i).midiCommand;
        testTheMidiMessage(s);
    }


    // Connect or disconnect devices
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupDevice(MidiDevice device) {
        mainActivityInterface.getMidi().setMidiDevice(device);
        Log.d("d", "Device opened = " + device);
        MidiDeviceInfo midiDeviceInfo = mainActivityInterface.getMidi().getMidiDevice().getInfo();
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
                        mainActivityInterface.getMidi().setMidiInputPort(mainActivityInterface.getMidi().getMidiDevice().openInputPort(pi.getPortNumber()));
                        foundinport = true;
                    }
                    break;
                case MidiDeviceInfo.PortInfo.TYPE_OUTPUT:
                    if (!foundoutport) {
                        Log.d("d", "Output port found = " + pi.getPortNumber());
                        mainActivityInterface.getMidi().setMidiOutputPort(mainActivityInterface.getMidi().getMidiDevice().openOutputPort(pi.getPortNumber()));
                        if (myView.midiAsPedal.isChecked()) {
                            mainActivityInterface.getMidi().enableMidiListener(requireContext());
                        }
                        foundoutport = true;
                    }
                    break;
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void disconnectDevices() {
        mainActivityInterface.getMidi().disconnectDevice();
        displayCurrentDevice();
    }


    // Deal with creating midi messages
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getHexCodeFromDropDowns() {
        int commandInt = midiCommand.indexOf(myView.midiCommand.getText().toString());
        String command = mainActivityInterface.getMidi().getMidiCommand(commandInt);
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
            midiString = mainActivityInterface.getMidi().buildMidiString(command,channel,noteorcontroller,valueorvelocity);
        } catch (Exception e) {
            midiString = "0x00 0x00 0x00";
        }
        myView.midiCode.setText(midiString);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addMidiToList() {
        try {
            String command = myView.midiCode.getText().toString();
            String readable = mainActivityInterface.getMidi().getReadableStringFromHex(command, requireContext());
            mainActivityInterface.getMidi().addToSongMessages(-1,command);

            MidiInfo midiInfo = new MidiInfo();
            midiInfo.midiCommand = command;
            midiInfo.readableCommand = readable;
            int position = midiMessagesAdapter.addToEnd(midiInfo);
            midiMessagesAdapter.notifyItemInserted(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mainActivityInterface.getMidi().updateSongMessages();
    }
    // Called back from MainActivity
    public void deleteMidiFromList(int i) {
        try {
            midiMessagesAdapter.removeItem(i);
            midiMessagesAdapter.notifyItemRemoved(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mainActivityInterface.getMidi().updateSongMessages();
    }


    // Process song midi messages
    private void setupAdapter() {
        midiMessagesAdapter = new MidiMessagesAdapter(mainActivityInterface,false);
        ItemTouchHelper.Callback callback = new MidiItemTouchHelper(midiMessagesAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        midiMessagesAdapter.setTouchHelper(itemTouchHelper);
        llm = new LinearLayoutManager(requireContext());
        llm.setOrientation(RecyclerView.VERTICAL);
        myView.recyclerView.post(() -> {
            myView.recyclerView.setLayoutManager(llm);
            myView.recyclerView.setAdapter(midiMessagesAdapter);
            itemTouchHelper.attachToRecyclerView(myView.recyclerView);
        });
    }
    private void buildList() {
        midiInfos = new ArrayList<>();

        String[] bits = mainActivityInterface.getSong().getMidi().trim().split("\n");
        for (String command : bits) {
            if (command!=null && !command.equals("") && !command.isEmpty() && getActivity()!=null) {
                // Get a human readable version of the midi code
                String readable = mainActivityInterface.getMidi().getReadableStringFromHex(command,getActivity());
                MidiInfo midiInfo = new MidiInfo();
                midiInfo.midiCommand = command;
                midiInfo.readableCommand = readable;
                midiInfos.add(midiInfo);
            }
        }

        myView.recyclerView.post(() -> {
            midiMessagesAdapter.updateMidiInfos(midiInfos);
            myView.recyclerView.setVisibility(View.VISIBLE);
        });
    }

/*

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initialiseCurrentMessages() {
        songMidiMessages = new ArrayList<>();
        songMidiMessagesToSave = new ArrayList<>();

        // Add what is there already
        String[] bits = mainActivityInterface.getSong().getMidi().trim().split("\n");
        for (String s : bits) {
            if (s!=null && !s.equals("") && !s.isEmpty() && getActivity()!=null) {
                // Get a human readable version of the midi code
                String hr = mainActivityInterface.getMidi().getReadableStringFromHex(s,getActivity());
                songMidiMessages.add(hr);
                songMidiMessagesToSave.add(s);
            }
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        myView.recyclerView.setLayoutManager(linearLayoutManager);

        // specify an adapter (see also next example)
        midiMessagesAdapter = new MidiMessagesAdapter(mainActivityInterface, true);
        myView.recyclerView.setAdapter(midiMessagesAdapter);

    }
*/

    // Save the song messages
    private void saveSongMessages() {
        mainActivityInterface.getMidi().updateSongMessages();
        try {
            // Get a string representation of the midi commands
            StringBuilder s = new StringBuilder();
            for (MidiInfo midiInfo : midiMessagesAdapter.getMidiInfos()) {
                String command = midiInfo.midiCommand.trim();
                if (!command.isEmpty()) {
                    s.append(command).append("\n");
                }
            }
            s = new StringBuilder(s.toString().trim()); // Get rid of extra line breaks
            Log.d("d","s="+s);
            mainActivityInterface.getSong().setMidi(s.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Save the song
        mainActivityInterface.getSaveSong().updateSong(requireContext(), mainActivityInterface);
    }
}

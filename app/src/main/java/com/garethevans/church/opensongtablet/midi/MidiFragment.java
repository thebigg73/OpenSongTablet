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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsMidiBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;

import java.util.ArrayList;
import java.util.List;

public class MidiFragment extends Fragment {

    private SettingsMidiBinding myView;
    private Preferences preferences;
    private Midi midi;
    private MainActivityInterface mainActivityInterface;

    private final Handler selected = new Handler();
    private final Runnable runnable = this::displayCurrentDevice;

    private BluetoothLeScanner bluetoothLeScanner;
    private MidiDeviceInfo[] usbDevices;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private ArrayList<String> bluetoothNames, usbNames, usbManufact;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsMidiBinding.inflate(inflater, container, false);

        // set Helpers
        setHelpers();

        // Hide the desired views
        hideShowViews(true, false);

        // Set known values
        setValues();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void setHelpers() {
        preferences = new Preferences();
        midi = mainActivityInterface.getMidi(mainActivityInterface);
    }

    private void setValues() {
        displayCurrentDevice();
        myView.enableBluetooth.setChecked(allowBluetoothSearch());
        myView.enableBluetooth.setEnabled(allowBluetoothSearch());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setListeners() {
        myView.enableBluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
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
    }

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

    }

    private boolean allowBluetoothSearch() {
        return mainActivityInterface.requestCoarseLocationPermissions() && midi.getIncludeBluetoothMidi();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScan() {
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
                String manuf = getString(R.string.file_type_unknown);
                String device = getString(R.string.file_type_unknown);
                try {
                    device = md.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
                    manuf = md.getProperties().getString(MidiDeviceInfo.PROPERTY_MANUFACTURER);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (device != null) {
                    usbNames.add(device);
                } else {
                    usbNames.add(getString(R.string.file_type_unknown));
                }
                if (manuf != null) {
                    usbManufact.add(manuf);
                } else {
                    usbManufact.add(getString(R.string.file_type_unknown));
                }
            }
            myView.progressBar.setVisibility(View.GONE);
            myView.searchDevices.setEnabled(true);
            myView.foundDevicesLayout.setVisibility(View.VISIBLE);
            updateDevices(false);
        } else {
            ShowToast.showToast(getContext(), getString(R.string.nothighenoughapi));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScanBluetooth() {
        bluetoothDevices = new ArrayList<>();
        bluetoothNames = new ArrayList<>();
        bluetoothDevices.clear();
        bluetoothNames.clear();
        // Stops scanning after a pre-defined scan period.
        Handler mHandler = new Handler();
        long SCAN_PERIOD = 8000;
        mHandler.postDelayed(() -> {
            bluetoothLeScanner.stopScan(scanCallback);
            Log.d("d", "Scan timeout");
            myView.progressBar.setVisibility(View.GONE);
            myView.searchDevices.setEnabled(true);
            myView.foundDevicesLayout.setEnabled(true);

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
            ShowToast.showToast(getContext(), requireContext().getString(R.string.nothighenoughapi));
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
            if (device != null && !bluetoothDevices.contains(device)) {
                bluetoothNames.add(device.getName());
                bluetoothDevices.add(device);
                Log.d("d", "name=" + device.getName());
                Log.d("d", "device " + device + " added");
                Log.d("d", "bluetootDevices=" + bluetoothDevices);
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
    private void sendTestNote() {
        try {
            String s1 = midi.buildMidiString("NoteOn", 1, 60, 100);
            byte[] buffer1 = midi.returnBytesFromHexText(s1);
            boolean sent = midi.sendMidi(buffer1);

            Handler h = new Handler();
            h.postDelayed(() -> {
                String s2 = midi.buildMidiString("NoteOff", 1, 60, 0);
                byte[] buffer2 = midi.returnBytesFromHexText(s2);
                midi.sendMidi(buffer2);
            }, 1000);
            if (sent) {
                ShowToast.showToast(getContext(), getString(R.string.ok));
            }
        } catch (Exception e) {
            e.printStackTrace();
            ShowToast.showToast(getContext(), getString(R.string.error));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void disconnectDevices() {
        midi.disconnectDevice();
        displayCurrentDevice();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateDevices(boolean bluetoothscan) {
        try {
            // Clear the found devices
            myView.foundDevicesLayout.removeAllViews();

            int size;
            if (bluetoothscan) {
                size = bluetoothDevices.size();
            } else {
                size = usbDevices.length;
            }

            // For each device, add a new text view
            for (int x = 0; x < size; x++) {
                TextView textView = new TextView(getContext());
                textView.setTextColor(Color.WHITE);
                textView.setText(bluetoothNames.get(x));
                textView.setTextSize(18.0f);
                textView.setPadding(12, 12, 12, 12);
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
                            setupDevice(device);
                            selected.postDelayed(runnable, 1000);
                        }, null);
                    }
                    if (bluetoothscan && midi.getMidiManager() != null) {
                        midi.getMidiManager().openBluetoothDevice(bluetoothDevices.get(finalX), device -> {
                            setupDevice(device);
                            selected.postDelayed(runnable, 1000);
                        }, null);
                    } else if (midi.getMidiManager()!=null) {
                        midi.getMidiManager().openDevice(usbDevices[finalX], device -> {
                            setupDevice(device);
                            selected.postDelayed(runnable, 1000);
                        }, null);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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
                        foundoutport = true;
                    }
                    break;
            }
        }
    }
}

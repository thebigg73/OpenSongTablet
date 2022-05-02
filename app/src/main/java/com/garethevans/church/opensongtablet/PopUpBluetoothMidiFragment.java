package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PopUpBluetoothMidiFragment extends DialogFragment {

    static PopUpBluetoothMidiFragment newInstance() {
        PopUpBluetoothMidiFragment frag;
        frag = new PopUpBluetoothMidiFragment();
        return frag;
    }

    private BluetoothLeScanner mBluetoothLeScanner;
    private List<BluetoothDevice> listBluetoothDevice;
    private ArrayList<String> bluetoothNames;
    private ProgressBar progressBar;
    private Button scanStartStop, disconnectDevice;
    private ListView bluetoothDevices;
    private LinearLayout currentDevice;
    private TextView currentDeviceName, currentDeviceAddress;
    private Handler selected;
    private Runnable runnable;
    private Midi m;
    private String[] permissions;
    private Preferences preferences;
    private int midiDelay = 100;

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
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        View V = inflater.inflate(R.layout.popup_mididevices, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.midi_bluetooth));
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
        saveMe.hide();

        preferences = new Preferences();
        midiDelay = preferences.getMyPreferenceInt(requireContext(),"midiDelay",100);

        // Initialise the basic views
        progressBar = V.findViewById(R.id.progressBar);
        scanStartStop = V.findViewById(R.id.scanStartStop);
        bluetoothDevices = V.findViewById(R.id.bluetoothDevices);
        currentDevice = V.findViewById(R.id.currentDevice);
        currentDeviceName = V.findViewById(R.id.currentDeviceName);
        currentDeviceAddress = V.findViewById(R.id.currentDeviceAddress);
        disconnectDevice = V.findViewById(R.id.disconnectDevice);
        Button testDevice = V.findViewById(R.id.testDevice);

        getPermissionsRequired();

        // Initialise the Midi classes
        m = new Midi();

        selected = new Handler();
        runnable = this::displayCurrentDevice;

        displayCurrentDevice();

        disconnectDevice.setOnClickListener(view -> disconnectDevices(true));
        testDevice.setOnClickListener(view -> sendTestNote());
        progressBar.setVisibility(View.GONE);
        scanStartStop.setEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionAllowed();
        }

        scanStartStop.setOnClickListener(view -> {
            try {
                if (permissionAllowed()) {
                    progressBar.setVisibility(View.VISIBLE);
                    bluetoothDevices.setEnabled(false);
                    scanStartStop.setEnabled(false);
                    startScan();
                } else {
                    progressBar.setVisibility(View.GONE);
                    scanStartStop.setEnabled(true);
                    bluetoothDevices.setEnabled(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Get scanner.  This is only allowed for Marshmallow or later
        BluetoothManager bluetoothManager = (BluetoothManager) requireActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }

        PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog(), preferences);

        return V;
    }

    private void getPermissionsRequired() {
        // Gets a string array of all required permissions for Bluetooth MIDI
        if (Build.VERSION.SDK_INT >= 31) {
            // API 31 or higher
            Log.d("BluetoothFrag","permissions: BLUETOOTH_SCAN, BLUETOOTH_CONNECT");
            permissions = new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        } else if (Build.VERSION.SDK_INT <= 28) {
            Log.d("BluetoothFrag","permissions: ACCESS_COARSE_LOCATION");
            permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH};
        } else {
            Log.d("BluetoothFrag","permissions: ACCESS_FINE_LOCATION");
            permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean permissionAllowed() {
        boolean allowed = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allowed = false;
            }
        }
        if (!allowed) {
            ActivityCompat.requestPermissions(requireActivity(), permissions, 1);
        }
        return allowed;
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateDevices(final ArrayList<String> bn, final List<BluetoothDevice> bd) {
        try {

            if (bluetoothDevices != null && permissionAllowed()) {
                ArrayAdapter<String> aa = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, bn);
                aa.notifyDataSetChanged();
                bluetoothDevices.setAdapter(aa);
                bluetoothDevices.setOnItemClickListener((adapterView, view, i, l) -> {
                    disconnectDevices(false);
                    // Display the current device
                    StaticVariables.midiDeviceName = bd.get(i).getName();
                    StaticVariables.midiDeviceAddress = bd.get(i).getAddress();
                    //displayCurrentDevice();
                    StaticVariables.midiManager = (MidiManager) requireActivity().getSystemService(Context.MIDI_SERVICE);
                    if (StaticVariables.midiManager != null) {
                        StaticVariables.midiManager.openBluetoothDevice(bd.get(i),
                                midiDevice -> {
                                    StaticVariables.midiDevice = midiDevice;
                                    Log.d("d", "Device opened = " + midiDevice);
                                    MidiDeviceInfo midiDeviceInfo = midiDevice.getInfo();
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
                                                    StaticVariables.midiInputPort = StaticVariables.midiDevice.openInputPort(pi.getPortNumber());
                                                    foundinport = true;
                                                }
                                                break;
                                            case MidiDeviceInfo.PortInfo.TYPE_OUTPUT:
                                                if (!foundoutport) {
                                                    Log.d("d", "Output port found = " + pi.getPortNumber());
                                                    StaticVariables.midiOutputPort = StaticVariables.midiDevice.openOutputPort(pi.getPortNumber());
                                                    foundoutport = true;
                                                }
                                                break;
                                        }
                                    }
                                    selected.postDelayed(runnable, 1000);
                                }, null);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScan() {
        listBluetoothDevice = new ArrayList<>();
        bluetoothNames = new ArrayList<>();
        listBluetoothDevice.clear();
        bluetoothNames.clear();
        // Stops scanning after a pre-defined scan period.
        Handler mHandler = new Handler();
        long SCAN_PERIOD = 8000;
        mHandler.postDelayed(() -> {
            try {
                if (permissionAllowed()) {
                    mBluetoothLeScanner.stopScan(scanCallback);
                    Log.d("d", "Scan timeout");
                    progressBar.setVisibility(View.GONE);
                    scanStartStop.setEnabled(true);
                    bluetoothDevices.setEnabled(true);
                }
            } catch (Exception e) {
                Log.d("BluetoothMidiFrag", "Popup was closed before handler fired");
            }

        }, SCAN_PERIOD);

        //scan specified devices only with ScanFilter
        ScanFilter scanFilter =
                new ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700"))
                        .build();
        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(scanFilter);

        ScanSettings scanSettings =
                new ScanSettings.Builder().build();

        if (mBluetoothLeScanner != null) {
            mBluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
        } else {
            StaticVariables.myToastMessage = getString(R.string.nothighenoughapi);
            ShowToast.showToast(getActivity());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private final ScanCallback scanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            addBluetoothDevice(result.getDevice());
            updateDevices(bluetoothNames, listBluetoothDevice);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
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

        @RequiresApi(api = Build.VERSION_CODES.M)
        private void addBluetoothDevice(BluetoothDevice device) {
            try {
                if (device != null && !listBluetoothDevice.contains(device)) {
                    if (permissionAllowed()) {
                        bluetoothNames.add(device.getName());
                        listBluetoothDevice.add(device);
                        Log.d("d", "name=" + device.getName());
                        Log.d("d", "device " + device + " added");
                        Log.d("d", "listBluetoothDevice=" + listBluetoothDevice);
                    }
                }
            } catch (SecurityException se) {
                Log.d("BluetoothFrag","permission error");
            } catch (Exception e) {
                Log.d("BluetoothFrag","fragment closed");
            }
        }
    };

    private void displayCurrentDevice() {
        Log.d("d", "displayCurrentDevice()");
        if (StaticVariables.midiDevice != null && StaticVariables.midiDeviceName != null && StaticVariables.midiDeviceAddress != null) {
            currentDevice.setVisibility(View.VISIBLE);
            currentDeviceName.setText(StaticVariables.midiDeviceName);
            currentDeviceAddress.setText(StaticVariables.midiDeviceAddress);
            String d = getString(R.string.connections_disconnect) + " " + StaticVariables.midiDeviceName;
            disconnectDevice.setText(d);
        } else {
            currentDevice.setVisibility(View.GONE);
            StaticVariables.midiDeviceName = "";
            StaticVariables.midiDeviceAddress = "";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendTestNote() {
        try {
            String s1on = m.buildMidiString("NoteOn", 1, 60, 100); // C
            String s2on = m.buildMidiString("NoteOn", 2, 60, 100); // C
            String s3on = m.buildMidiString("NoteOn",10, 42, 100); // Hihat
            String s4on = m.buildMidiString("PC",1,0,1); // Program change
            String s1off = m.buildMidiString("NoteOff", 1, 60, 0);
            String s2off = m.buildMidiString("NoteOff", 2, 60, 0);
            String s3off = m.buildMidiString("NoteOff", 10, 42, 0);
            String s4off = m.buildMidiString("PC",1,0,0);

            byte[] buffer1on = m.returnBytesFromHexText(s1on);
            byte[] buffer2on = m.returnBytesFromHexText(s2on);
            byte[] buffer3on = m.returnBytesFromHexText(s3on);
            byte[] buffer4on = m.returnBytesFromHexText(s4on);
            byte[] buffer1off = m.returnBytesFromHexText(s1off);
            byte[] buffer2off = m.returnBytesFromHexText(s2off);
            byte[] buffer3off = m.returnBytesFromHexText(s3off);
            byte[] buffer4off = m.returnBytesFromHexText(s4off);

            // On and off notes get sent with midiDelay
            m.sendMidi(buffer1on);
            new Handler().postDelayed(() -> m.sendMidi(buffer2on),midiDelay*2L);
            new Handler().postDelayed(() -> m.sendMidi(buffer3on),midiDelay*3L);
            new Handler().postDelayed(() -> m.sendMidi(buffer4on),midiDelay*4L);
            new Handler().postDelayed(() -> m.sendMidi(buffer1off),500+(midiDelay*5L));
            new Handler().postDelayed(() -> m.sendMidi(buffer2off),500+(midiDelay*6L));
            new Handler().postDelayed(() -> m.sendMidi(buffer3off),500+(midiDelay*7L));
            new Handler().postDelayed(() -> m.sendMidi(buffer4off),500+(midiDelay*8L));

            StaticVariables.myToastMessage = getString(R.string.ok);
            ShowToast.showToast(getContext());

        } catch (Exception e) {
            e.printStackTrace();
            StaticVariables.myToastMessage = getString(R.string.error);
            ShowToast.showToast(getContext());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void disconnectDevices(boolean doUpdate) {
        m.disconnectDevice();
        if (doUpdate) {
            displayCurrentDevice();
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // Close any scanner if running
        if (mBluetoothLeScanner != null && permissionAllowed()) {
            try {
                mBluetoothLeScanner.stopScan(scanCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

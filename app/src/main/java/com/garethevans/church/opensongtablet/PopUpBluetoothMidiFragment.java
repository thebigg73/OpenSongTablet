package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
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

        Preferences preferences = new Preferences();

        // Initialise the basic views
        progressBar = V.findViewById(R.id.progressBar);
        scanStartStop = V.findViewById(R.id.scanStartStop);
        bluetoothDevices = V.findViewById(R.id.bluetoothDevices);
        currentDevice = V.findViewById(R.id.currentDevice);
        currentDeviceName = V.findViewById(R.id.currentDeviceName);
        currentDeviceAddress = V.findViewById(R.id.currentDeviceAddress);
        disconnectDevice = V.findViewById(R.id.disconnectDevice);
        Button testDevice = V.findViewById(R.id.testDevice);

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean permissionAllowed() {
        boolean allowed = true;
        int permissionCheck = requireActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            allowed = false;
            if (!requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
        return allowed;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateDevices(final ArrayList<String> bn, final List<BluetoothDevice> bd) {
        try {
            if (bluetoothDevices != null) {
                ArrayAdapter<String> aa = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, bn);
                aa.notifyDataSetChanged();
                bluetoothDevices.setAdapter(aa);
                bluetoothDevices.setOnItemClickListener((adapterView, view, i, l) -> {
                    disconnectDevices(false);
                    // Display the current device
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
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


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startScan() {
        listBluetoothDevice = new ArrayList<>();
        bluetoothNames = new ArrayList<>();
        listBluetoothDevice.clear();
        bluetoothNames.clear();
        // Stops scanning after a pre-defined scan period.
        Handler mHandler = new Handler();
        long SCAN_PERIOD = 8000;
        mHandler.postDelayed(() -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            } else {
                mBluetoothLeScanner.stopScan(scanCallback);
                Log.d("d", "Scan timeout");
                progressBar.setVisibility(View.GONE);
                scanStartStop.setEnabled(true);
                bluetoothDevices.setEnabled(true);
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
            if (device != null && !listBluetoothDevice.contains(device)) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                } else {
                    bluetoothNames.add(device.getName());
                    listBluetoothDevice.add(device);
                    Log.d("d", "name=" + device.getName());
                    Log.d("d", "device " + device + " added");
                    Log.d("d", "listBluetoothDevice=" + listBluetoothDevice);
                }
            }
        }
    };

    private void displayCurrentDevice() {
        Log.d("d","displayCurrentDevice()");
        if (StaticVariables.midiDevice!=null && StaticVariables.midiDeviceName!=null && StaticVariables.midiDeviceAddress!=null) {
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
            String s1 = m.buildMidiString("NoteOn",1,60,100);
            byte[] buffer1 = m.returnBytesFromHexText(s1);
            //byte[] buffer1 = m.buildMidiCommand("NoteOn","C5","127","1",null);
            m.sendMidi(buffer1);

            Handler h = new Handler();
            h.postDelayed(() -> {
                String s2 = m.buildMidiString("NoteOff",1,60,0);
                byte[] buffer2 = m.returnBytesFromHexText(s2);
                //byte[] buffer2 = m.buildMidiCommand("NoteOn","C5","0","1",null);
                m.sendMidi(buffer2);
            },1000);
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
}

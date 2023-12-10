package com.garethevans.church.opensongtablet.midi;

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
import android.graphics.Color;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsMidiBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MidiFragment extends Fragment {

    private SettingsMidiBinding myView;
    private MainActivityInterface mainActivityInterface;
    private final String TAG = "MidiFragment";

    private final Handler selected = new Handler();
    private final Runnable runnable = this::displayCurrentDevice;

    private BluetoothLeScanner bluetoothLeScanner;
    private MidiDeviceInfo[] usbMidiDevices;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private ArrayList<String> usbNames, usbManufact;
    ActivityResultLauncher<String[]> midiScanPermissions;
    private String midi_string="", website_midi_connections_string="", permissions_refused_string="",
            okay_string="", unknown_string="", error_string="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(midi_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

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

        prepareStrings();

        webAddress = website_midi_connections_string;

        // Register this fragment with the main activity to deal with listeners
        mainActivityInterface.registerFragment(this, "MidiFragment");

        // Set up the permission launcher for Nearby
        setPermissions();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                // Set known values
                setValues();

                // Hide the desired views
                hideShowViews(true, false);

                // Set listeners
                setListeners();
            });
        });
        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            midi_string = getString(R.string.midi);
            website_midi_connections_string = getString(R.string.website_midi_connections);
            permissions_refused_string = getString(R.string.permissions_refused);
            unknown_string = getString(R.string.unknown);
            error_string = getString(R.string.midi_error);
            okay_string = getString(R.string.okay);
        }
    }
    private void setPermissions() {
        midiScanPermissions = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
            Log.d(TAG,"Permissions: "+isGranted);
            myView.enableBluetooth.setChecked(mainActivityInterface.getAppPermissions().hasMidiScanPermissions());
            if (!mainActivityInterface.getAppPermissions().hasMidiScanPermissions()) {
                mainActivityInterface.getShowToast().doIt(permissions_refused_string);
            }
        });
    }

    private void setValues() {
        displayCurrentDevice();
        myView.enableBluetooth.setChecked(allowBluetoothSearch(mainActivityInterface.getMidi().getIncludeBluetoothMidi()));
        myView.autoSendMidi.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("midiSendAuto",false));
        myView.midiDelay.setAdjustableButtons(true);
        myView.midiDelay.setHint(mainActivityInterface.getMidi().getMidiDelay() + "ms");
        myView.midiDelay.setValue(mainActivityInterface.getMidi().getMidiDelay());
        myView.midiDelay.setLabelFormatter(value1 -> (int) value1 + "ms");
        myView.midiDelay.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getMidi().setMidiDelay((int)myView.midiDelay.getValue());
                myView.midiDelay.setHint((int)myView.midiDelay.getValue()+"ms");
            }
        });
        myView.midiDelay.addOnChangeListener((slider, value, fromUser) -> {
            mainActivityInterface.getMidi().setMidiDelay((int)myView.midiDelay.getValue());
            myView.midiDelay.setHint((int)myView.midiDelay.getValue()+"ms");
        });
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
    }

    // Set the view listeners
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setListeners() {
        myView.enableBluetooth.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Check we have the permission
                if (!mainActivityInterface.getAppPermissions().hasMidiScanPermissions()) {
                    myView.enableBluetooth.setChecked(false);
                    midiScanPermissions.launch(mainActivityInterface.getAppPermissions().getMidiScanPermissions());
                    return;
                }
            }
            if (isChecked && mainActivityInterface.getAppPermissions().hasMidiScanPermissions()) {
                // Get scanner.  This is only allowed for Marshmallow or later
                BluetoothManager bluetoothManager = null;
                if (getActivity()!=null) {
                    bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
                }
                if (bluetoothManager != null) {
                    BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                    bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                }
            }
            mainActivityInterface.getMidi().setIncludeBluetoothMidi(isChecked);
        });
        myView.searchDevices.setOnClickListener(v -> startScan());
        myView.testMidiDevice.setOnClickListener(v -> sendTestNote());
        myView.disconnectMidiDevice.setOnClickListener(v -> disconnectDevices());
        myView.autoSendMidi.setOnCheckedChangeListener(((buttonView, isChecked) -> mainActivityInterface.getMidi().setMidiSendAuto(isChecked)));
        myView.midiAsPedal.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("midiAsPedal", isChecked);
            mainActivityInterface.getPedalActions().setMidiAsPedal(isChecked);
            if (isChecked) {
                mainActivityInterface.getMidi().enableMidiListener();
            }
        }));
        myView.midiAsPedal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && mainActivityInterface.getMidi().getMidiDevice() != null &&
                    mainActivityInterface.getMidi().getMidiOutputPort() != null) {
                mainActivityInterface.getMidi().enableMidiListener();
            } else if (!isChecked && mainActivityInterface.getMidi().getMidiDevice() != null &&
                    mainActivityInterface.getMidi().getMidiOutputPort() != null) {
                mainActivityInterface.getMidi().disableMidiListener();
            }
        });
    }


    // Check permissions
    private boolean allowBluetoothSearch(boolean switchOn) {
        return switchOn && mainActivityInterface.getAppPermissions().hasMidiScanPermissions();
    }

    // Scan for devices (USB or Bluetooth)
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScan() {
        Log.d(TAG,"startScan");
        if (getActivity()!=null) {
            // Try to initialise the midi manager
            mainActivityInterface.getMidi().setMidiManager((MidiManager) getActivity().getSystemService(Context.MIDI_SERVICE));
            myView.searchProgressLayout.setVisibility(View.VISIBLE);
            myView.progressBar.setVisibility(View.VISIBLE);
            if (mainActivityInterface.getMidi().getIncludeBluetoothMidi() &&
                    mainActivityInterface.getAppPermissions().hasMidiScanPermissions()) {
                startScanBluetooth();
            } else {
                startScanUSB();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScanUSB() {

        if (mainActivityInterface.getMidi().getMidiManager() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                usbMidiDevices = mainActivityInterface.getMidi().getMidiManager().getDevicesForTransport(MidiManager.TRANSPORT_MIDI_BYTE_STREAM).toArray(new MidiDeviceInfo[0]);
            } else {
                usbMidiDevices = mainActivityInterface.getMidi().getMidiManager().getDevices();
            }
            usbNames = new ArrayList<>();
            usbManufact = new ArrayList<>();
            for (MidiDeviceInfo md : usbMidiDevices) {
                String manuf = unknown_string;
                String device = unknown_string;
                Log.d(TAG,"md:"+md);
                try {
                    device = md.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
                    manuf = md.getProperties().getString(MidiDeviceInfo.PROPERTY_MANUFACTURER);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (device != null) {
                    usbNames.add(device);
                } else {
                    usbNames.add(unknown_string);
                }
                if (manuf != null) {
                    usbManufact.add(manuf);
                } else {
                    usbManufact.add(unknown_string);
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

    @SuppressLint("MissingPermission") // Permissions are checked prior to calling this!
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScanBluetooth() {
        // To get here, we know we have permission as we've already checked!
        bluetoothDevices = new ArrayList<>();
        myView.foundDevicesLayout.removeAllViews();
        myView.devicesText.setVisibility(View.GONE);
        // Stops scanning after a pre-defined scan period.
        Handler mHandler = new Handler();
        long SCAN_PERIOD = 10000;
        mHandler.postDelayed(() -> {
            try {
                bluetoothLeScanner.stopScan(scanCallback);
                myView.progressBar.setVisibility(View.GONE);
                myView.searchDevices.setEnabled(true);
                myView.foundDevicesLayout.setEnabled(true);
            } catch (Exception e) {
                Log.d(TAG,"Unable to stop the Bluetooth scan.  Likely closed the fragment!/n");
            }
        }, SCAN_PERIOD);

        // Scan specified BLE devices only with ScanFilter
        ScanFilter scanFilter =
                new ScanFilter.Builder()
                        .setServiceUuid(ParcelUuid.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700"))
                        .build();
        List<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(scanFilter);

        ScanSettings scanSettings = new ScanSettings.Builder().build();
        if (!mainActivityInterface.getAppPermissions().hasMidiScanPermissions()) {
            midiScanPermissions.launch(mainActivityInterface.getAppPermissions().getMidiScanPermissions());
        } else if (bluetoothLeScanner != null) {
            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
        } else {
            mainActivityInterface.getShowToast().doIt(error_string);
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
            Log.d(TAG, "onScanFailed: " + errorCode);
        }

        @SuppressLint("MissingPermission")
        private void addBluetoothDevice(BluetoothDevice device) {
            if (device != null && !bluetoothDevices.contains(device)) {
                Log.d(TAG, "device=" + device);
                bluetoothDevices.add(device);
                if (!mainActivityInterface.getAppPermissions().hasMidiScanPermissions()) {
                    midiScanPermissions.launch(mainActivityInterface.getAppPermissions().getMidiScanPermissions());
                } else {
                    Log.d(TAG, "name=" + device.getName());
                }
            }
        }
    };

    private void displayCurrentDevice() {
        Log.d(TAG, "displayCurrentDevice()");
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

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateDevices(boolean bluetoothscan) {
        if (getActivity()!=null) {
            try {
                myView.enableBluetooth.setEnabled(true);

                // Clear the found devices
                myView.foundDevicesLayout.removeAllViews();

                int size;
                if (bluetoothscan) {
                    size = bluetoothDevices.size();
                } else {
                    size = usbMidiDevices.length;
                }

                if (size > 0) {
                    myView.devicesText.setVisibility(View.VISIBLE);
                } else {
                    myView.devicesText.setVisibility(View.GONE);
                }

                // For each device, add a new text view
                for (int x = 0; x < size; x++) {
                    TextView textView = new TextView(getContext());
                    textView.setTextColor(Color.BLACK);
                    textView.setBackgroundColor(Color.LTGRAY);
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp.setMargins(12, 12, 12, 12);
                    textView.setLayoutParams(llp);
                    if (bluetoothscan && !mainActivityInterface.getAppPermissions().hasMidiScanPermissions()) {
                        midiScanPermissions.launch(mainActivityInterface.getAppPermissions().getMidiScanPermissions());
                    } else {
                        if (bluetoothscan) {
                            textView.setText(bluetoothDevices.get(x).getName());
                        } else {
                            textView.setText(usbNames.get(x));
                        }
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
                            mainActivityInterface.getMidi().setMidiManager((MidiManager) getActivity().getSystemService(Context.MIDI_SERVICE));

                            if (bluetoothscan && mainActivityInterface.getMidi().getMidiManager() != null) {
                                mainActivityInterface.getMidi().getMidiManager().openBluetoothDevice(bluetoothDevices.get(finalX), device -> {
                                    mainActivityInterface.getMidi().setMidiDevice(device);
                                    setupDevice(device);
                                    selected.postDelayed(runnable, 1000);
                                }, null);
                            } else if (mainActivityInterface.getMidi().getMidiManager() != null) {
                                mainActivityInterface.getMidi().getMidiManager().openDevice(usbMidiDevices[finalX], device -> {
                                    mainActivityInterface.getMidi().setMidiDevice(device);
                                    setupDevice(device);
                                    selected.postDelayed(runnable, 1000);
                                }, null);
                            }
                        });
                        myView.foundDevicesLayout.addView(textView);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // Send midi data for testing
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendTestNote() {
        try {
            String s1on = mainActivityInterface.getMidi().buildMidiString("NoteOn", 1, 60, 100); // C
            String s2on = mainActivityInterface.getMidi().buildMidiString("NoteOn", 2, 60, 100); // C
            String s3on = mainActivityInterface.getMidi().buildMidiString("NoteOn",10, 42, 100); // Hihat
            String s4on = mainActivityInterface.getMidi().buildMidiString("PC",1,0,1); // Program change
            String s1off = mainActivityInterface.getMidi().buildMidiString("NoteOff", 1, 60, 0);
            String s2off = mainActivityInterface.getMidi().buildMidiString("NoteOff", 2, 60, 0);
            String s3off = mainActivityInterface.getMidi().buildMidiString("NoteOff", 10, 42, 0);
            String s4off = mainActivityInterface.getMidi().buildMidiString("PC",1,0,0);

            byte[] buffer1on = mainActivityInterface.getMidi().returnBytesFromHexText(s1on);
            byte[] buffer2on = mainActivityInterface.getMidi().returnBytesFromHexText(s2on);
            byte[] buffer3on = mainActivityInterface.getMidi().returnBytesFromHexText(s3on);
            byte[] buffer4on = mainActivityInterface.getMidi().returnBytesFromHexText(s4on);
            byte[] buffer1off = mainActivityInterface.getMidi().returnBytesFromHexText(s1off);
            byte[] buffer2off = mainActivityInterface.getMidi().returnBytesFromHexText(s2off);
            byte[] buffer3off = mainActivityInterface.getMidi().returnBytesFromHexText(s3off);
            byte[] buffer4off = mainActivityInterface.getMidi().returnBytesFromHexText(s4off);

            // On and off notes get sent with midiDelay
            int midiDelay = mainActivityInterface.getMidi().getMidiDelay();
            boolean sent = mainActivityInterface.getMidi().sendMidi(buffer1on);
            new Handler(Looper.getMainLooper()).postDelayed(() -> mainActivityInterface.getMidi().sendMidi(buffer2on),midiDelay*2L);
            new Handler(Looper.getMainLooper()).postDelayed(() -> mainActivityInterface.getMidi().sendMidi(buffer3on),midiDelay*3L);
            new Handler(Looper.getMainLooper()).postDelayed(() -> mainActivityInterface.getMidi().sendMidi(buffer4on),midiDelay*4L);
            new Handler(Looper.getMainLooper()).postDelayed(() -> mainActivityInterface.getMidi().sendMidi(buffer1off),500+(midiDelay*5L));
            new Handler(Looper.getMainLooper()).postDelayed(() -> mainActivityInterface.getMidi().sendMidi(buffer2off),500+(midiDelay*6L));
            new Handler(Looper.getMainLooper()).postDelayed(() -> mainActivityInterface.getMidi().sendMidi(buffer3off),500+(midiDelay*7L));
            new Handler(Looper.getMainLooper()).postDelayed(() -> mainActivityInterface.getMidi().sendMidi(buffer4off),500+(midiDelay*8L));

            if (sent) {
                mainActivityInterface.getShowToast().doIt(okay_string);
            } else {
                mainActivityInterface.getShowToast().doIt(error_string);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mainActivityInterface.getShowToast().doIt(error_string);

        }
    }

    // Connect or disconnect devices
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupDevice(MidiDevice device) {
        mainActivityInterface.getMidi().setMidiDevice(device);
        Log.d(TAG, "Device opened = " + device);
        MidiDeviceInfo midiDeviceInfo = mainActivityInterface.getMidi().getMidiDevice().getInfo();
        int numInputs = midiDeviceInfo.getInputPortCount();
        int numOutputs = midiDeviceInfo.getOutputPortCount();
        Log.d(TAG, "Input ports = " + numInputs + ", Output ports = " + numOutputs);

        boolean foundinport = false;  // We will only grab the first one
        boolean foundoutport = false; // We will only grab the first one

        MidiDeviceInfo.PortInfo[] portInfos = midiDeviceInfo.getPorts();
        for (MidiDeviceInfo.PortInfo pi : portInfos) {
            switch (pi.getType()) {
                case MidiDeviceInfo.PortInfo.TYPE_INPUT:
                    if (!foundinport) {
                        Log.d(TAG, "Input port found = " + pi.getPortNumber());
                        mainActivityInterface.getMidi().setMidiInputPort(mainActivityInterface.getMidi().getMidiDevice().openInputPort(pi.getPortNumber()));
                        foundinport = true;
                    }
                    break;
                case MidiDeviceInfo.PortInfo.TYPE_OUTPUT:
                    if (!foundoutport) {
                        Log.d(TAG, "Output port found = " + pi.getPortNumber());
                        mainActivityInterface.getMidi().setMidiOutputPort(mainActivityInterface.getMidi().getMidiDevice().openOutputPort(pi.getPortNumber()));
                        if (myView.midiAsPedal.getChecked()) {
                            mainActivityInterface.getMidi().enableMidiListener();
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
        myView.progressBar.setVisibility(View.GONE);
        displayCurrentDevice();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothLeScanner != null && mainActivityInterface.getAppPermissions().hasMidiScanPermissions() &&
                Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            try {
                bluetoothLeScanner.stopScan(scanCallback);
            } catch (SecurityException e) {
                Log.d(TAG, "Security exception");
            } catch (Exception e) {
                Log.d(TAG, "Scanner unable to be stopped (maybe already stopped!)");
            }
        }
        mainActivityInterface.registerFragment(null, "MidiFragment");
    }
}

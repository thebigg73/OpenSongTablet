package com.garethevans.church.opensongtablet.midi;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import java.util.UUID;

public class DirectMidiGattCallback extends BluetoothGattCallback {

    private static final String TAG = "DirectMidiGattCallback";

    // Standard BLE-MIDI UUIDs
    private static final UUID MIDI_SERVICE_UUID = UUID.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700");
    private static final UUID MIDI_CHAR_UUID   = UUID.fromString("7772E5DB-3868-4112-A1A9-F2669D106BF3");

    // Interface or listener to pass the ready characteristic back to your UI/Manager
    private final MidiGattReadyListener listener;

    private BluetoothGattCharacteristic midiCharacteristic = null;

    public interface MidiGattReadyListener {
        void onMidiCharacteristicReady(BluetoothGattCharacteristic characteristic);
        void onDisconnected();
    }

    public DirectMidiGattCallback(MidiGattReadyListener listener) {
        this.listener = listener;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server. Starting service discovery...");
                // Discover services on the connected device
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.");
                midiCharacteristic = null;
                if (listener != null) {
                    listener.onDisconnected();
                }
            }
        } else {
            Log.e(TAG, "GATT connection state change error status: " + status);
            gatt.close();
            if (listener != null) {
                listener.onDisconnected();
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            BluetoothGattService service = gatt.getService(MIDI_SERVICE_UUID);
            if (service != null) {
                midiCharacteristic = service.getCharacteristic(MIDI_CHAR_UUID);
                if (midiCharacteristic != null) {
                    // Set write type to match the 'write without response' requirement for low-latency MIDI
                    midiCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    Log.d(TAG, "MIDI Service and Characteristic found successfully!");

                    if (listener != null) {
                        listener.onMidiCharacteristicReady(midiCharacteristic);
                    }
                } else {
                    Log.e(TAG, "MIDI Characteristic not found within service.");
                }
            } else {
                Log.e(TAG, "MIDI Service not found on device.");
            }
        } else {
            Log.e(TAG, "Service discovery failed with status: " + status);
        }
    }
}
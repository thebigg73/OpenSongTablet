/*
package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private static boolean connected;
    BluetoothAdapter mBluetoothAdapter;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       StageMode activity) {
        super();
        FullscreenActivity.mManager = manager;
        FullscreenActivity.mChannel = channel;
        Log.d("d","WiFiDirectBroadcastReceiver called from "+activity);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Log.d("d","action="+action);

        FullscreenActivity.mBluetoothName = getLocalBluetoothName();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled


            } else {
                // Wi-Fi P2P is not enabled

            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            WifiP2pManager.PeerListListener pl = new MyPeerDevices();
            FullscreenActivity.mManager.requestPeers(FullscreenActivity.mChannel, pl);

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing


        }
    }



    @SuppressLint("HardwareIds")
    public String getLocalBluetoothName() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        String name = mBluetoothAdapter.getName();
        if (name == null) {
            System.out.println("Name is null!");
            name = mBluetoothAdapter.getAddress();
        }
        return name;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    static class MyPeerDevices implements WifiP2pManager.PeerListListener {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            FullscreenActivity.wifiP2PDeviceList = wifiP2pDeviceList;
            FullscreenActivity.deviceList = new ArrayList<>();
            for (WifiP2pDevice peer : wifiP2pDeviceList.getDeviceList()) {
                FullscreenActivity.deviceList.add(peer.deviceName);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean connectToDevice(final String s, final Context c) {
        if (FullscreenActivity.wifiP2PDeviceList != null) {
            for (WifiP2pDevice peer : FullscreenActivity.wifiP2PDeviceList.getDeviceList()) {
                //here get the device info
                if (peer != null) {
                    String name = peer.deviceName;
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = peer.deviceAddress;
                    config.wps.setup = WpsInfo.PBC;
                    config.groupOwnerIntent = 15;
                    if (name.equals(s)) {
                        FullscreenActivity.mManager.connect(FullscreenActivity.mChannel, config, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                //success logic
                                FullscreenActivity.myToastMessage = s + " - " +
                                        c.getResources().getString(R.string.success);
                                ShowToast.showToast(c);
                                FullscreenActivity.deviceConnectedTo = s;
                                connected = true;
                            }

                            @Override
                            public void onFailure(int reason) {
                                //failure logic
                                connected = false;
                                FullscreenActivity.myToastMessage = s + " - " +
                                        c.getResources().getString(R.string.error);
                                ShowToast.showToast(c);
                            }
                        });

                    }
                }
            }
        }
        return connected;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void registerService(int port) {
        // Create the NsdServiceInfo object, and populate it.
        FullscreenActivity.serviceInfo  = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        FullscreenActivity.serviceInfo.setServiceName("OpenSongApp");
        FullscreenActivity.serviceInfo.setServiceType("_http._tcp.");
        FullscreenActivity.serviceInfo.setPort(port);
    }

    public static void updateDeviceLists(final Context c, LinearLayout ll) {
        // For each device, add a TextView
        for (int i=0; i<FullscreenActivity.deviceList.size(); i++) {
            final String device = FullscreenActivity.deviceList.get(i);
            Button rb = new Button(c);
            rb.setText(device);
            rb.setTextColor(0xffffffff);
            rb.setTextSize(12.0f);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(8,8,8,8);
            rb.setLayoutParams(llp);
            rb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FullscreenActivity.deviceConnectedTo = "";
                    connectToDevice(device,c);
                }
            });
            ll.addView(rb);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void listAvailableHosts() {Log.d("d", "listAvailableHosts() called");
        FullscreenActivity.mManager.discoverPeers(FullscreenActivity.mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                FullscreenActivity.mWifiP2PListener = new MyPeerDevices();
                FullscreenActivity.mManager.requestPeers(FullscreenActivity.mChannel, FullscreenActivity.mWifiP2PListener);
            }

            @Override
            public void onFailure(int reasonCode) {
                // No peers!
            }
        });
    }

    public static void sendSongLocation(String s) {
        AsyncTask<Object, Void, String> send_songlocation = new SendSongLocation(s);
        send_songlocation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private static class SendSongLocation extends AsyncTask<Object, Void, String> {

        String location;
        Socket socket = new Socket();
        int len;
        byte buf[] = new byte[1024];

        SendSongLocation (String s) {
            location = s;
            Log.d("d","location="+location);
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {

                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = new ByteArrayInputStream(location.getBytes());

                while ((len = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                inputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            //catch logic
                        }
                    }
                }
            }
            return null;
        }

    }

    public static void receiveSongLocation() {
        AsyncTask<Object, Void, String> receive_songlocation = new ReceiveSongLocation();
        receive_songlocation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private static class ReceiveSongLocation extends AsyncTask<Object, Void, String> {

        String receivedLocation;

        @Override
        protected String doInBackground(Object... objects) {
            try {
                ServerSocket serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();

                InputStream inputStream = client.getInputStream();
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                Log.d("d","receivedLocation="+receivedLocation);
                receivedLocation = result.toString("UTF-8");
                inputStream.close();
                result.close();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return receivedLocation;
        }

    }


}
*/

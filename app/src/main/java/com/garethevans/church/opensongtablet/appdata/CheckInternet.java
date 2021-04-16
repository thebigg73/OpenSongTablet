package com.garethevans.church.opensongtablet.appdata;

import android.app.Activity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CheckInternet {

    boolean connected;
    private ConnectedInterface connectedInterface;
    public interface ConnectedInterface {
        void isConnected(boolean connected);
    }

    public void checkConnection(Activity activity) {
        connectedInterface = (ConnectedInterface) activity;
        new Thread(() -> {
            try {
                Socket sock = new Socket();
                sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);  //Google
                sock.close();
                connected = true;
            } catch (IOException e) {
                connected = false;
            }

            // Now return the value
            connectedInterface.isConnected(connected);
        }).start();
    }

}
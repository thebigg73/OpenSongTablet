package com.garethevans.church.opensongtablet.appdata;

import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CheckInternet {

    public void checkConnection(Fragment fragment, int fragId, MainActivityInterface mainActivityInterface) {
        new Thread(() -> {
            boolean connected;
            try {
                Socket sock = new Socket();
                sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);  //Google
                sock.close();
                connected = true;
            } catch (IOException e) {
                connected = false;
            }
            mainActivityInterface.isWebConnected(fragment,fragId,connected);
        }).start();
    }

}
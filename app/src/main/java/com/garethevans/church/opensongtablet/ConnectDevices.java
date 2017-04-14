package com.garethevans.church.opensongtablet;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


class ConnectDevices {

    private static int[] NETWORK_TYPES = {ConnectivityManager.TYPE_WIFI};

    @SuppressWarnings("deprecation")
    static boolean isConnectedToNetwork(Context c) {
        ConnectivityManager connManager =
                (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        for (int networkType : NETWORK_TYPES) {
            NetworkInfo info = connManager.getNetworkInfo(networkType);
            if (info != null && info.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }

}

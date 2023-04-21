package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckInternet {

    private String searchPhrase;
    private String searchSite = "Google";
    private final String TAG = "CheckInternet";

    public boolean isNetworkConnected(Context c, MainActivityInterface mainActivityInterface) {
        boolean onlyUseWiFi = mainActivityInterface.getPreferences().getMyPreferenceBoolean("download_wifi_only",true);
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities!=null && onlyUseWiFi) {
                    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
                } else if (capabilities!=null) {
                    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) |
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
                } else {
                    return false;
                }
            } else {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork!=null && activeNetwork.isRoaming() && onlyUseWiFi) {
                    return false;
                } else {
                    return activeNetwork != null && activeNetwork.isConnected();
                }
            }
        }
        return false;
    }

    public void checkConnection(Fragment fragment, int fragId, MainActivityInterface mainActivityInterface) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            boolean connected;
            try {
                Socket sock = new Socket();
                sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);  //Google
                sock.close();
                connected = true;
            } catch (IOException e) {
                connected = false;
            }
            Log.d(TAG,"connected="+connected);
            mainActivityInterface.isWebConnected(fragment,fragId,connected);
        });
    }

    public void setSearchPhrase(String searchPhrase) {
        this.searchPhrase = searchPhrase;
    }
    public String getSearchPhrase() {
        return searchPhrase;
    }
    public void setSearchSite(String searchSite) {
        this.searchSite = searchSite;
    }
    public String getSearchSite() {
        return searchSite;
    }
}
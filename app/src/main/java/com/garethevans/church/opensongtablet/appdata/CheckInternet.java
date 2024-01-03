package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class CheckInternet {

    private String searchPhrase;
    // IV - Match with first entry in ImportOnlineFragment sources list
    private String searchSite = "UltimateGuitar";
    @SuppressWarnings({"unused","FieldCanBeLocal"})
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
                    // Default to true
                    return true;
                }
            } else {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork!=null && activeNetwork.isRoaming() && onlyUseWiFi) {
                    return false;
                } else {
                    return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                }
            }
        }
        return false;
    }

    public void checkConnection(Context c, Fragment fragment, int fragId, MainActivityInterface mainActivityInterface) {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            boolean connected = false;
            if (c!=null) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager != null) {
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    connected = (networkInfo != null && networkInfo.isConnectedOrConnecting());
                }
            }
            mainActivityInterface.isWebConnected(fragment, fragId, connected);


            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    Socket sock = new Socket();
                    sock.connect(new InetSocketAddress("8.8.8.8", 53), 1500);  //Google
                    sock.close();
                    connected = true;
                } catch (IOException e) {
                    connected = false;
                }
                Log.d(TAG, "connected=" + connected);
                mainActivityInterface.isWebConnected(fragment, fragId, connected);
            } else {
                // Return true for older devices
                mainActivityInterface.isWebConnected(fragment, fragId, true);
            }*/
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
package com.garethevans.church.opensongtablet.webserver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class LocalWiFiHost {

    // Localhost is only available on API 26+
    // Getting the SSID and passphrase is easiest on API 30+
    // Older versions use deprecated methods

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = "LocalWiFiHost";
    private final Handler localWifiHandler = new Handler();
    private WifiManager.LocalOnlyHotspotReservation reservation;
    private WifiConfiguration configuration;
    private String ssid;
    private String password;
    private WebServerFragment webServerFragment;
    private final Context context;
    private final MainActivityInterface mainActivityInterface;
    private boolean running;

    public LocalWiFiHost(Context context) {
        this.context = context;
        mainActivityInterface = (MainActivityInterface) context;
    }

    public void setWebServerFragment(WebServerFragment webServerFragment) {
        this.webServerFragment = webServerFragment;
    }

    private void startLocalWifi() {
        // Initialise the WiFi manager
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            // If this already ran, we need to close it
            stopLocalWifi();
            // Start the local WiFi
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26
                wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
                    @Override
                    public void onStarted(WifiManager.LocalOnlyHotspotReservation receivedreservation) {
                        super.onStarted(receivedreservation);
                        reservation = receivedreservation;
                        setLocalWifiInfos();
                    }
                }, localWifiHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Newer versions of Android get the SSID and password easily
    // Older versions must use deprecated method
    @SuppressWarnings("deprecation")
    public void setLocalWifiInfos() {
        if (reservation!=null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26
            configuration = reservation.getWifiConfiguration();

            if (configuration != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30
                    ssid = reservation.getSoftApConfiguration().getSsid();
                    password = reservation.getSoftApConfiguration().getPassphrase();
                } else { // Deprecated since API 26
                    ssid = configuration.SSID;
                    password = configuration.preSharedKey;
                }
                setQRWebServer();
            }
        }
    }

    public void setQRWebServer() {
        Bitmap bitmap = null;
        QRCodeWriter writer = new QRCodeWriter();
        // Get the IP address
        String ip=mainActivityInterface.getWebServer().getIP();
        Log.d(TAG,"ip:"+ip);
        if (configuration != null) {
            try {
                // The format of a WiFi network QRCode is WIFI:S:MySSID;T:WPA;P:MyPassW0rd;
                String qrCodeContent = "WIFI:S:"+ssid+";T:WPA;P:"+password+";;";
                BitMatrix bitMatrix = writer.encode(qrCodeContent, BarcodeFormat.QR_CODE, 200, 200);

                int w = bitMatrix.getWidth();
                int h = bitMatrix.getHeight();
                int[] pixels = new int[w * h];
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        pixels[y * w + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                    }
                }

                bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                bitmap.setPixels(pixels, 0, w, 0, 0, w, h);

            } catch (Exception e) {
                bitmap = null;
            }
        }
        if (webServerFragment!=null) {
            // Show local WiFi QR code;
            webServerFragment.setQRHotspot(bitmap,ssid,password);
            webServerFragment.updateWebServerIP();
        }
    }

    public void stopLocalWifi() {
        if (reservation != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                reservation.close();
            }
        }
    }

    boolean getRunning() {
        return running;
    }

    void setRunning(boolean running) {
        this.running = running;
        stopLocalWifi();
        // Only start it if we are running API 26+
        if (running && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // API 26
            startLocalWifi();
        }
    }
}

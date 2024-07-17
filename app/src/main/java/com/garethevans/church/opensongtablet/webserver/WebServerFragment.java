package com.garethevans.church.opensongtablet.webserver;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsWebServerBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class WebServerFragment extends Fragment {

    private SettingsWebServerBinding myView;
    private MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "WebServerFragment";
    private String webAddress="", website_web_server_string="", web_server_string="",
            ssid_string="",password_string="";

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(web_server_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
        WebServerFragment webServerFragment = this;
        // Let the localWiFiHost know we can update the QR code
        mainActivityInterface.getLocalWiFiHost().setWebServerFragment(webServerFragment);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsWebServerBinding.inflate(inflater, container, false);

        prepareStrings();

        webAddress = website_web_server_string;

        WebServerFragment webServerFragment = this;
        // Let the localWiFiHost know we can update the QR code
        mainActivityInterface.getLocalWiFiHost().setWebServerFragment(webServerFragment);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myView.runHotspot.setVisibility(View.VISIBLE);
            if (mainActivityInterface.getLocalWiFiHost()!=null) {
                myView.runHotspot.setChecked(mainActivityInterface.getLocalWiFiHost().getRunning());
                myView.localHotspot.setVisibility(mainActivityInterface.getLocalWiFiHost().getRunning() ? View.VISIBLE : View.GONE);
            } else {
                myView.localHotspot.setVisibility(View.GONE);
            }
        }

        // Update the views
        updateViews();

        // Set the listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext() != null) {
            web_server_string = getString(R.string.web_server);
            website_web_server_string = getString(R.string.website_web_server);
            ssid_string = getString(R.string.ssid);
            password_string = getString(R.string.password);
        }
    }

    private void updateViews() {
        // Set the webServer info
        myView.allowWebNavigation.setChecked(mainActivityInterface.getWebServer().getAllowWebNavigation());
        Glide.with(this).load(mainActivityInterface.getWebServer().getIPQRCode()).into(myView.qrCode);
        myView.ipAddress.setText(mainActivityInterface.getWebServer().getIP());
        myView.webServer.setChecked(mainActivityInterface.getWebServer().getRunWebServer());
        myView.webServerInfo.setVisibility(mainActivityInterface.getWebServer().getRunWebServer() ? View.VISIBLE:View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            myView.runHotspot.setChecked(mainActivityInterface.getLocalWiFiHost().getRunning());
            myView.runHotspot.setVisibility(View.VISIBLE);
        } else {
            myView.runHotspot.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        // The web server stuff
        myView.webServer.setOnCheckedChangeListener((compoundButton, b) ->
                mainActivityInterface.getMainHandler().post(() -> {
                    mainActivityInterface.getWebServer().stopWebServer();
                    mainActivityInterface.getWebServer().getIP();
                    mainActivityInterface.getWebServer().setRunWebServer(b);
                    updateViews();
                    myView.webServerInfo.setVisibility(b ? View.VISIBLE : View.GONE);
                }));
        myView.allowWebNavigation.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getWebServer().setAllowWebNavigation(b));
        myView.webServerInfo.setOnClickListener(view -> mainActivityInterface.openDocument(mainActivityInterface.getWebServer().getIP()));
        myView.runHotspot.setOnCheckedChangeListener((compoundButton, b) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG,"mainActivityInterface.getLocalWiFiHost():"+mainActivityInterface.getLocalWiFiHost());
                if (mainActivityInterface.getLocalWiFiHost()!=null) {
                    mainActivityInterface.getLocalWiFiHost().setRunning(b);
                    myView.localHotspot.setVisibility(b ? View.VISIBLE : View.GONE);
                    // Update the webserver address
                    if (myView.webServer.getChecked()) {
                        mainActivityInterface.getWebServer().stopWebServer();
                        mainActivityInterface.getWebServer().getIP();
                        mainActivityInterface.getWebServer().setRunWebServer(true);
                        updateViews();
                    }

                } else {
                    myView.localHotspot.setVisibility(View.GONE);
                    myView.runHotspot.setVisibility(View.GONE);
                }
            }
        });
    }

    public void setQRHotspot(Bitmap bitmap, String ssid, String password) {
        Glide.with(this).load(bitmap).into(myView.hotspotQR);
        String networkInfo = ssid_string + ": " + ssid + "\n" +
                password_string + ": " + password;
        myView.hotspotInfo.setText(networkInfo);
        Log.d(TAG,"newtowrkInfo:"+networkInfo);
        myView.localHotspot.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivityInterface.getLocalWiFiHost().setWebServerFragment(null);
    }
}

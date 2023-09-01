package com.garethevans.church.opensongtablet.webserver;

import android.content.Context;
import android.os.Bundle;
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
    private String webAddress="", website_web_server_string="", web_server_string="";

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(web_server_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
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
        }
    }

    private void updateViews() {
        // Set the webServer info
        Glide.with(this).load(mainActivityInterface.getWebServer().getIPQRCode()).into(myView.qrCode);
        myView.ipAddress.setText(mainActivityInterface.getWebServer().getIP());
        myView.webServer.setChecked(mainActivityInterface.getWebServer().getRunWebServer());
        myView.webServerInfo.setVisibility(mainActivityInterface.getWebServer().getRunWebServer() ? View.VISIBLE:View.GONE);
    }

    private void setListeners() {
        // The web server stuff
        myView.webServer.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getWebServer().setRunWebServer(b);
            myView.webServerInfo.setVisibility(b ? View.VISIBLE:View.GONE);
        });
        myView.webServerInfo.setOnClickListener(view -> mainActivityInterface.openDocument(mainActivityInterface.getWebServer().getIP()));
    }
}

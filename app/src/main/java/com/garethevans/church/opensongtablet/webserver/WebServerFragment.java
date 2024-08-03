package com.garethevans.church.opensongtablet.webserver;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.InformationBottomSheet;
import com.garethevans.church.opensongtablet.databinding.SettingsWebServerBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class WebServerFragment extends Fragment {

    private SettingsWebServerBinding myView;
    private MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "WebServerFragment";
    private String webAddress="", website_web_server_string="", web_server_string="",
            ssid_string="",password_string="", permissions_refused_string="", settings_string="",
            network_error_string="", nearby_string="", location_string="";
    private ActivityResultLauncher<String[]> hotspotPermission;
    private ActivityResultLauncher<String[]> webserverPermission;


    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(web_server_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
        WebServerFragment webServerFragment = this;
        // Let the localWiFiHost know we can update the QR code
        mainActivityInterface.getWebServer().setWebServerFragment(webServerFragment);
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

        setupPermissionLaunchers();

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

    private void setupPermissionLaunchers() {
        webserverPermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
            if (myView!=null) {
                boolean hasPermission = mainActivityInterface.getAppPermissions().hasWebServerPermission();
                boolean userChecked = myView.webServer.getChecked();

                if (hasPermission && userChecked) {
                    // We have permission granted and the user wants it, so do it
                    mainActivityInterface.getMainHandler().post(() -> {
                        mainActivityInterface.getWebServer().stopWebServer();
                        mainActivityInterface.getWebServer().getIP();
                        mainActivityInterface.getWebServer().setRunWebServer(true);
                        updateViews();
                        myView.webServerInfo.setVisibility(View.VISIBLE);
                    });

                } else if (hasPermission) {
                    // We have permission, but don't want to run it
                    mainActivityInterface.getMainHandler().post(() -> {
                        mainActivityInterface.getWebServer().stopWebServer();
                        mainActivityInterface.getWebServer().setRunWebServer(false);
                        updateViews();
                        myView.webServerInfo.setVisibility(View.GONE);
                    });

                } else if (userChecked) {
                    // Permission denied but user tried to turn it on, so switch off
                    mainActivityInterface.getMainHandler().post(() -> {
                        mainActivityInterface.getWebServer().stopWebServer();
                        mainActivityInterface.getWebServer().setRunWebServer(false);
                        updateViews();
                        myView.webServer.setChecked(false);
                        myView.webServerInfo.setVisibility(View.GONE);
                        InformationBottomSheet informationBottomSheet = new InformationBottomSheet(network_error_string,
                                permissions_refused_string, settings_string, "appPrefs");
                        informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "InformationBottomSheet");
                    });
                }
            }
        });
        hotspotPermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
            if (myView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mainActivityInterface.getLocalWiFiHost() != null) {
                boolean hasPermission = mainActivityInterface.getAppPermissions().hasHotSpotPermission();
                boolean ischecked = myView.runHotspot.getChecked();
                if (hasPermission && ischecked) {
                    // Permission granted so do it
                    mainActivityInterface.getMainHandler().post(() -> {
                        mainActivityInterface.getLocalWiFiHost().setRunning(true);
                        myView.localHotspot.setVisibility(View.VISIBLE);
                        // Update the webserver address
                        updateWebServerIP();
                    });
                } else if (hasPermission) {
                    //Permission granted, but we don't want it
                    mainActivityInterface.getMainHandler().post(() -> {
                        mainActivityInterface.getLocalWiFiHost().setRunning(false);
                        myView.localHotspot.setVisibility(View.GONE);
                    });
                } else if (ischecked) {
                    // No permission given, turn it off and let the user know
                    mainActivityInterface.getMainHandler().post(() -> {
                        mainActivityInterface.getLocalWiFiHost().setRunning(false);
                        myView.localHotspot.setVisibility(View.GONE);
                        myView.runHotspot.setChecked(false);
                        String permission_needed;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permission_needed = nearby_string;
                        } else {
                            permission_needed = location_string;
                        }
                        InformationBottomSheet informationBottomSheet = new InformationBottomSheet(permission_needed,
                                permissions_refused_string, settings_string, "appPrefs");
                        informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "InformationBottomSheet");
                    });
                }

            } else if (myView!=null) {
                // Old version of Android
                myView.runHotspot.setVisibility(View.GONE);
                myView.hotspotInfo.setVisibility(View.GONE);
            }
        });
    }
    private void prepareStrings() {
        if (getContext() != null) {
            web_server_string = getString(R.string.web_server);
            website_web_server_string = getString(R.string.website_web_server);
            ssid_string = getString(R.string.ssid);
            password_string = getString(R.string.password);
            permissions_refused_string = getString(R.string.permissions_refused);
            settings_string = getString(R.string.settings);
            network_error_string = getString(R.string.network_error);
            nearby_string = getString(R.string.nearby_devices);
            location_string = getString(R.string.location);
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

    public void updateWebServerIP() {
        mainActivityInterface.getWebServer().stopWebServer();
        mainActivityInterface.getWebServer().getIP();
        mainActivityInterface.getWebServer().setRunWebServer(myView.webServer.getChecked());
        updateViews();
    }

    private void setListeners() {
        // The web server stuff
        myView.webServer.setOnCheckedChangeListener((compoundButton, b) ->
                mainActivityInterface.getMainHandler().post(() -> {
                    if (mainActivityInterface.getAppPermissions().hasWebServerPermission()) {
                        mainActivityInterface.getWebServer().stopWebServer();
                        mainActivityInterface.getWebServer().getIP();
                        mainActivityInterface.getWebServer().setRunWebServer(b);
                        updateViews();
                        myView.webServerInfo.setVisibility(b ? View.VISIBLE : View.GONE);
                    } else {
                        // Request permission
                        mainActivityInterface.getWebServer().stopWebServer();
                        mainActivityInterface.getWebServer().setRunWebServer(false);
                        myView.webServerInfo.setVisibility(View.GONE);
                        webserverPermission.launch(mainActivityInterface.getAppPermissions().getWebServerPermission());
                    }
                }));
        myView.allowWebNavigation.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getWebServer().setAllowWebNavigation(b));
        myView.webServerInfo.setOnClickListener(view -> mainActivityInterface.openDocument(mainActivityInterface.getWebServer().getIP()));
        myView.runHotspot.setOnCheckedChangeListener((compoundButton, b) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (mainActivityInterface.getAppPermissions().hasHotSpotPermission() &&
                        mainActivityInterface.getLocalWiFiHost()!=null) {
                    mainActivityInterface.getLocalWiFiHost().setRunning(b);
                    myView.localHotspot.setVisibility(b ? View.VISIBLE : View.GONE);
                    // Update the webserver address
                    updateWebServerIP();
                } else if (!mainActivityInterface.getAppPermissions().hasHotSpotPermission()) {
                    // No permission, so request it
                    hotspotPermission.launch(mainActivityInterface.getAppPermissions().getLocalHostSpotPermission());
                }
            } else {
                // Not running O+
                myView.localHotspot.setVisibility(View.GONE);
                myView.runHotspot.setVisibility(View.GONE);
            }
        });
    }

    public void setQRHotspot(Bitmap bitmap, String ssid, String password) {
        Glide.with(this).load(bitmap).into(myView.hotspotQR);
        String networkInfo = ssid_string + ": " + ssid + "\n" +
                password_string + ": " + password;
        myView.hotspotInfo.setText(networkInfo);
        myView.localHotspot.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivityInterface.getLocalWiFiHost().setWebServerFragment(null);
        mainActivityInterface.getWebServer().setWebServerFragment(null);
    }
}

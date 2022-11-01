package com.garethevans.church.opensongtablet.appdata;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.View;

import com.garethevans.church.opensongtablet.customviews.MaterialTextView;

public class VersionNumber {
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "VersionNumber";

    private PackageInfo packageInfo;
    private String versionName;
    private int versionCode;

    // Setup the version stuff if it is null
    private void setupPackage(Context c) {
        setPackageInfo(c);
        setVersionName();
        setVersionCode();
        }
    private void setPackageInfo(Context c) {
        try {
            if (packageInfo==null) {
                packageInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            }
        } catch (Exception e) {
            packageInfo = null;
            e.printStackTrace();
        }
    }
    private void setVersionName() {
        if (packageInfo!=null) {
            versionName = packageInfo.versionName;
        } else {
            versionName = "?";
        }
    }
    private void setVersionCode() {
        if (packageInfo!=null) {
            versionCode = packageInfo.versionCode;
        } else {
            versionCode = 0;
        }
    }

    public void updateMenuVersionNumber(Context c, MaterialTextView showVersion) {
        // Update the app version in the menu
        setupPackage(c);
        if (!versionName.equals("?") && versionCode > 0) {
            if (showVersion != null) {
                showVersion.setVisibility(View.VISIBLE);
                showVersion.setText(getFullVersionInfo());
                showVersion.setHint(""+getVersionCode());
            }
        } else {
            if (showVersion != null) {
                showVersion.setVisibility(View.GONE);
            }
        }
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getFullVersionInfo() {
        return "V" + versionName;
    }
}

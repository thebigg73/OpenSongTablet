package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.TextView;

public class VersionNumber {

    public void updateMenuVersionNumber(Context c, TextView showVersion) {
        // Update the app version in the menu
        PackageInfo pinfo;
        int versionNumber = 0;
        String versionName = "?";
        try {
            pinfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            versionNumber = pinfo.versionCode;
            versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }

        if (!versionName.equals("?") && versionNumber > 0) {
            String temptext = "V" + versionName + " (" + versionNumber + ")";
            if (showVersion != null) {
                showVersion.setVisibility(View.VISIBLE);
                showVersion.setText(temptext);
            }
        } else {
            if (showVersion != null) {
                showVersion.setVisibility(View.GONE);
            }
        }
    }
}

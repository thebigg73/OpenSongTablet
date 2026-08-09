package com.garethevans.church.opensongtablet.screensetup;

import android.os.Build;
import android.view.Window;
import androidx.annotation.RequiresApi;
import androidx.core.view.DisplayCutoutCompat;
import androidx.core.graphics.Insets; // <--- Corrected Import
import androidx.core.view.WindowInsetsCompat;

@RequiresApi(Build.VERSION_CODES.M)
public class WindowInsetsCompatHandler {

    private WindowInsetsCompat insetsCompat;

    public void setInsetsCompat(WindowInsetsCompat insetsCompat) {
        this.insetsCompat = insetsCompat;
    }

    public WindowInsetsCompat getInsetsCompat() {
        return insetsCompat;
    }

    public DisplayCutoutCompat getDisplayCutout() {
        return insetsCompat != null ? insetsCompat.getDisplayCutout() : null;
    }

    public Insets getNavBarsInsets() {
        return insetsCompat != null ? insetsCompat.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars()) : Insets.NONE;
    }

    public Insets getStatusBarsInsets() {
        return insetsCompat != null ? insetsCompat.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars()) : Insets.NONE;
    }

    public boolean isImeVisible() {
        return insetsCompat != null && insetsCompat.isVisible(WindowInsetsCompat.Type.ime());
    }
}
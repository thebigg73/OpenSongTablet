package com.garethevans.church.opensongtablet.interfaces;

import android.view.Display;

import com.google.android.gms.cast.CastDevice;

public interface DisplayInterface {
    void updateDisplay(String what);
    void setupDisplay(Display presentationDisplay);
    void setupCastDevice(CastDevice castDevice);
    void presenterShowLogo(boolean show);
    void presenterBlackScreen(boolean black);
    void presenterShowSection(int position);
    void resetHDMI();
}

package com.garethevans.church.opensongtablet.interfaces;

import android.view.Display;

import com.google.android.gms.cast.CastDevice;

public interface DisplayInterface {

    // General display tasks (from any mode)
    void updateDisplay(String what);
    void setupDisplay(Display presentationDisplay);
    void setupCastDevice(CastDevice castDevice);
    void resetHDMI();

    // Presenter Mode tasks
    void presenterShowLogo(boolean show);
    void presenterBlackScreen(boolean black);
    void presenterShowSection(int position);

    // Performance and Stage Mode tasks
    void performanceShowSection(int position);
}

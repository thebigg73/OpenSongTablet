package com.garethevans.church.opensongtablet.interfaces;

import android.view.Display;

import com.google.android.gms.cast.CastDevice;

public interface DisplayInterface {
    void updateDisplays();
    void setupDisplay(Display presentationDisplay);
    void setupCastDevice(CastDevice castDevice);
}

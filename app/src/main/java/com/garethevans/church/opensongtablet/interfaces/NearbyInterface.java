package com.garethevans.church.opensongtablet.interfaces;

import android.content.Context;

public interface NearbyInterface {
    void startDiscovery(Context c, MainActivityInterface mainActivityInterface);
    void startAdvertising(Context c, MainActivityInterface mainActivityInterface);
    void stopDiscovery(Context c);
    void stopAdvertising(Context c);
    void turnOffNearby(Context c);
    void doSendPayloadBytes(Context c,String infoPayload);
}
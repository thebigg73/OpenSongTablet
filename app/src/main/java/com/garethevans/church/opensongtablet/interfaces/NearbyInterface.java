package com.garethevans.church.opensongtablet.interfaces;

import com.garethevans.church.opensongtablet.autoscroll.AutoscrollActions;

public interface NearbyInterface {
    void startDiscovery(AutoscrollActions autoscrollActions);
    void startAdvertising(AutoscrollActions autoscrollActions);
    void stopDiscovery();
    void stopAdvertising();
    void turnOffNearby();
    void doSendPayloadBytes(String infoPayload);
}
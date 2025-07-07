package com.garethevans.church.opensongtablet.interfaces;

public interface NearbyInterface {
    // TODO might not be used at all now
    void startDiscovery();
    void startAdvertising();
    void stopDiscovery();
    void stopAdvertising();
    void turnOffNearby();
    //void doSendPayloadBytes(String infoPayload, boolean clientSend);
}
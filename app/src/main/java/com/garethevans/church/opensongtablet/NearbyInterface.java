package com.garethevans.church.opensongtablet;

interface NearbyInterface {
    void startDiscovery();
    void startAdvertising();
    void stopDiscovery();
    void stopAdvertising();
    void turnOffNearby();
    String getUserNickname();
}
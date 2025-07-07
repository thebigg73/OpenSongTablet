package com.garethevans.church.opensongtablet.interfaces;

public interface NearbyConnectionsManagementInterface {
    void startDiscovery();
    void startAdvertising();
    void stopDiscovery();
    void stopAdvertising();
    void turnOffNearby();
}

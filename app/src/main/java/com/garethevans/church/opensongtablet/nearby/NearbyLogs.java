package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class NearbyLogs {

    /*
    This class deals with Nearby logging
        - This can be devices connected
        - Nearby actions
        - Message queue
     */

    private final MainActivityInterface mainActivityInterface;
    private String connectionLog = "";

    NearbyLogs(Context c) {
        this.mainActivityInterface = (MainActivityInterface) c;
    }

    // Updates the connnection log with this message.  This also updates the connected devices note
    public void updateConnectionLog(String newMessage) {
        if (newMessage != null && mainActivityInterface != null) {
            connectionLog += newMessage + "\n";
            try {
                mainActivityInterface.updateConnectionsLog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public String getConnectionLog() {
        return connectionLog;
    }
    public void setConnectionLog(String connectionLog) {
        this.connectionLog = connectionLog;
    }

}

package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class NearbyMessages {

    // This class deals with the nearby messages.  Sending is done via the NearbySendPayloads class

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "NearbyMessages";
    private final MainActivityInterface mainActivityInterface;

    private String nearbyMessage1, nearbyMessage2, nearbyMessage3, nearbyMessage4, nearbyMessage5,
            nearbyMessage6, nearbyMessage7, nearbyMessage8;
    private boolean nearbyMessageMIDIAction;

    NearbyMessages(Context c) {
        this.mainActivityInterface = (MainActivityInterface) c;
        getUpdatedPreferences();
    }

    public void getUpdatedPreferences() {
        nearbyMessage1 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage1", "");
        nearbyMessage2 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage2", "");
        nearbyMessage3 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage3", "");
        nearbyMessage4 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage4", "");
        nearbyMessage5 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage5", "");
        nearbyMessage6 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage6", "");
        nearbyMessage7 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage7", "");
        nearbyMessage8 = mainActivityInterface.getPreferences().getMyPreferenceString("nearbyMessage8", "");
        nearbyMessageMIDIAction = mainActivityInterface.getPreferences().getMyPreferenceBoolean("nearbyMessageMIDIAction", true);
    }

    // Nearby messages
    public String getNearbyMessage(int which) {
        switch (which) {
            case 1:
                return nearbyMessage1;
            case 2:
                return nearbyMessage2;
            case 3:
                return nearbyMessage3;
            case 4:
                return nearbyMessage4;
            case 5:
                return nearbyMessage5;
            case 6:
                return nearbyMessage6;
            case 7:
                return nearbyMessage7;
            case 8:
                return nearbyMessage8;
        }
        return "";
    }

    public void setNearbyMessage(int which, String nearbyMessage) {
        switch (which) {
            case 1:
                nearbyMessage1 = nearbyMessage;
                break;
            case 2:
                nearbyMessage2 = nearbyMessage;
                break;
            case 3:
                nearbyMessage3 = nearbyMessage;
                break;
            case 4:
                nearbyMessage4 = nearbyMessage;
                break;
            case 5:
                nearbyMessage5 = nearbyMessage;
                break;
            case 6:
                nearbyMessage6 = nearbyMessage;
                break;
            case 7:
                nearbyMessage7 = nearbyMessage;
                break;
            case 8:
                nearbyMessage8 = nearbyMessage;
                break;
        }
        if (which > 0 && which < 9) {
            mainActivityInterface.getPreferences().setMyPreferenceString("nearbyMessage" + which, nearbyMessage);
        }
    }

    public boolean getNearbyMessageMIDIAction() {
        return nearbyMessageMIDIAction;
    }

    public void setNearbyMessageMIDIAction(boolean nearbyMessageMIDIAction) {
        this.nearbyMessageMIDIAction = nearbyMessageMIDIAction;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("nearbyMessageMIDIAction", nearbyMessageMIDIAction);
    }

}

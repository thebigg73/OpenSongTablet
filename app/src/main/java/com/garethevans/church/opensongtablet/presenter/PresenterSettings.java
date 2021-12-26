package com.garethevans.church.opensongtablet.presenter;

public class PresenterSettings {
    // This holds variables used during presentations
    // They are accessed using getters and setters

    private boolean alertOn;

    // The setters
    public void setAlertOn(boolean alertOn) {
        this.alertOn = alertOn;
    }


    // The getters
    public boolean getAlertOn() {
        return alertOn;
    }
}

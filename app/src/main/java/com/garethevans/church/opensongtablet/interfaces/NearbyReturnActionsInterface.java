package com.garethevans.church.opensongtablet.interfaces;

import com.google.android.material.button.MaterialButton;

public interface NearbyReturnActionsInterface {
    void toggleAutoscroll();
    void selectSection(int i);
    void prepareSongMenu();
    void loadSong();
    void goToPreviousItem();
    void goToNextItem();
    void nearbyEnableConnectionButtons();
    void nearbyUpdateCountdownText(boolean advertise, MaterialButton materialButton);
}
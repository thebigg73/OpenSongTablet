package com.garethevans.church.opensongtablet.interfaces;

public interface NearbyReturnActionsInterface {
    void toggleAutoscroll();
    void selectSection(int i);
    void prepareSongMenu();
    void loadSong();
    void goToPreviousItem();
    void goToNextItem();
}
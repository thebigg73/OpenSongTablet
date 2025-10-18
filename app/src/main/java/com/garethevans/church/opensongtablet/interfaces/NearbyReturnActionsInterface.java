package com.garethevans.church.opensongtablet.interfaces;

import com.garethevans.church.opensongtablet.customviews.MyMaterialButton;

public interface NearbyReturnActionsInterface {
    // TODO clear up what we don't need anymore
    void toggleAutoscroll();
    void selectSection(int i);
    void doScrollByProportion(float scrollProportion);
    void doScrollToProportion(float scrollProportion);
    void loadSong(boolean updateSongMenu);
    void goToPreviousPage();
    void goToNextPage();
    void nearbyEnableConnectionButtons();
    void nearbyUpdateCountdownText(boolean advertise, MyMaterialButton materialButton);
}
package com.garethevans.church.opensongtablet.interfaces;

import com.garethevans.church.opensongtablet.customviews.MyMaterialButton;

public interface NearbyReturnActionsInterface {
    void selectSection(int i);
    void doScrollByProportion(float scrollProportion);
    void doScrollToProportion(float scrollProportion);
    void loadSong(boolean updateSongMenu);
    void nearbyEnableConnectionButtons();
    void nearbyUpdateCountdownText(boolean advertise, MyMaterialButton materialButton);
}
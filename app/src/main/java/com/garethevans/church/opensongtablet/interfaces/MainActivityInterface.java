package com.garethevans.church.opensongtablet.interfaces;

public interface MainActivityInterface {
    void hideActionButton(boolean hide);
    void hideActionBar(boolean hide);
    void updateToolbar();
    void showTutorial(String what);
    void indexSongs();
    void initialiseActivity();
}

package com.garethevans.church.opensongtablet.interfaces;

public interface ActionInterface {
    void navigateToFragment(String deepLink, int id);
    void showSticky(boolean forceShow, boolean hide);
    void chooseMenu(boolean showSetMenu);
    void onBackPressed();
    void metronomeToggle();
}

package com.garethevans.church.opensongtablet.interfaces;

public interface ActionInterface {
    void navigateToFragment(String deepLink, int id);
    void chooseMenu(boolean showSetMenu);
    void onBackPressed();
}

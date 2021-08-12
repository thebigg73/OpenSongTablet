package com.garethevans.church.opensongtablet.interfaces;

import androidx.fragment.app.FragmentManager;

import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public interface ActionInterface {
    void navigateToFragment(String deepLink, int id);
    void showSticky(boolean forceShow, boolean hide);
    void chooseMenu(boolean showSetMenu);
    void onBackPressed();
    void metronomeToggle();
    FragmentManager getMyFragmentManager();
    CurrentSet getCurrentSet();
    Song getSong();
}

package com.garethevans.church.opensongtablet.interfaces;

import androidx.fragment.app.FragmentManager;

import com.garethevans.church.opensongtablet.autoscroll.Autoscroll;
import com.garethevans.church.opensongtablet.performance.PerformanceGestures;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.songprocessing.Song;

public interface ActionInterface {
    Preferences getPreferences();
    void navigateToFragment(String deepLink, int id);
    void showSticky(boolean forceShow, boolean hide);
    void showAbc(boolean forceShow, boolean hide);
    void toggleHighlighter();
    void onBackPressed();
    void metronomeToggle();
    FragmentManager getMyFragmentManager();
    CurrentSet getCurrentSet();
    Song getSong();
    Autoscroll getAutoscroll();
    PerformanceGestures getPerformanceGestures();
}

package com.garethevans.church.opensongtablet.interfaces;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.appdata.SetTypeFace;
import com.garethevans.church.opensongtablet.midi.Midi;
import com.garethevans.church.opensongtablet.nearby.NearbyConnections;
import com.garethevans.church.opensongtablet.screensetup.ThemeColors;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

public interface MainActivityInterface {
    void hideActionButton(boolean hide);
    void hideActionBar(boolean hide);
    void updateToolbar(Song song, String what);
    void showTutorial(String what);
    void indexSongs();
    void initialiseActivity();
    void moveToSongInSongMenu();
    void hideKeyboard();
    void navigateToFragment(int id);
    void returnToHome(Fragment fragment, Bundle bundle);
    void songMenuActionButtonShow(boolean show);
    void lockDrawer(boolean lock);
    void closeDrawer(boolean close);
    void doSongLoad();
    void loadSongFromSet();
    void updateKeyAndLyrics(Song song);
    void editSongSaveButtonAnimation(boolean pulse);
    void registerFragment(Fragment frag, String what);
    void displayAreYouSure(String what, String action, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song);
    void confirmedAction(boolean agree, String what, ArrayList<String> arguments, String fragName, Fragment callingFragment, Song song);
    void refreshAll();
    void doExport(String what);
    void refreshSetList();
    void openDialog(DialogFragment frag, String tag);
    void updateFragment(String fragName, Fragment callingFragment, ArrayList<String> arguments);
    void updateSongMenu(String fragName, Fragment callingFragment, ArrayList<String> arguments);
    void updateSong(Song song);
    Song getSong();
    void setOriginalSong(Song originalSong);
    Song getOriginalSong();
    boolean songChanged();
    void updateSetList();
    void stopAutoscroll();
    void startAutoscroll();
    void fadeoutPad();
    void playPad();
    void updateConnectionsLog();
    boolean requestNearbyPermissions();
    boolean requestCoarseLocationPermissions();
    NearbyConnections getNearbyConnections(MainActivityInterface mainActivityInterface);
    Midi getMidi(MainActivityInterface mainActivityInterface);
    DrawerLayout getDrawer();
    ActionBar getAb();
    MediaPlayer getMediaPlayer(int i);
    SetTypeFace getMyFonts();
    ThemeColors getMyThemeColors();
}

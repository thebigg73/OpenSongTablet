package com.garethevans.church.opensongtablet.interfaces;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.appdata.SetTypeFace;
import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.controls.PedalActions;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.midi.Midi;
import com.garethevans.church.opensongtablet.nearby.NearbyConnections;
import com.garethevans.church.opensongtablet.screensetup.ThemeColors;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songprocessing.SongXML;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

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
    boolean requestFineLocationPermissions();
    void registerMidiAction(boolean actionDown, boolean actionUp, boolean actionLong, String note);

    // Get the helpers initialised in the main activity
    NearbyConnections getNearbyConnections(MainActivityInterface mainActivityInterface);
    Midi getMidi(MainActivityInterface mainActivityInterface);
    DrawerLayout getDrawer();
    ActionBar getAb();
    MediaPlayer getMediaPlayer(int i);
    SetTypeFace getMyFonts();
    ThemeColors getMyThemeColors();
    StorageAccess getStorageAccess();
    SongXML getSongXML();
    ConvertChoPro getConvertChoPro();
    ProcessSong getProcessSong();
    Song getSong();
    SQLiteHelper getSQLiteHelper();
    NonOpenSongSQLiteHelper getNonOpenSongSQLiteHelper();
    CommonSQL getCommonSQL();
    CCLILog getCCLILog();
    PedalActions getPedalActions(MainActivityInterface mainActivityInterface);
}
